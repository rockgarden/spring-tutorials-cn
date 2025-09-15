# [使用 Spring AI 与 PGVector 实现语义搜索](https://www.baeldung.com/spring-ai-pgvector-semantic-search)

人工智能 · Spring AI · Java · 搜索 · PostgreSQL 向量数据库  

1. 概述

    搜索是软件中的基础概念，旨在从海量数据集中查找相关信息，即在项目集合中定位特定项。

    在本教程中，我们将探索如何使用 **Spring AI**、**PGVector** 和 **Ollama** 实现语义搜索功能。

2. 背景知识

    语义搜索是一种高级搜索技术，它基于词语的语义含义查找最相关的结果。要构建语义搜索应用，我们需要理解以下几个关键概念：

    - **词嵌入（Word Embeddings）**：一种词的数值表示方法，使语义相近的词在向量空间中距离相近。词嵌入将词语转换为机器学习模型可处理的数值向量。
    - **语义相似度（Semantic Similarity）**：衡量两段文本在语义上相似程度的指标，用于比较词语、句子或文档的含义。
    - **向量空间模型（Vector Space Model）**：一种数学模型，将文本文档表示为高维空间中的向量。每个词对应一个向量，词之间的相似度通过向量间距离计算。
    - **余弦相似度（Cosine Similarity）**：计算两个非零向量之间夹角余弦值的相似度度量方法，用于衡量向量空间模型中两个向量的相似程度。

    现在，让我们动手构建一个演示应用。

3. 前置条件

    首先，确保机器上已安装 **Docker**，用于运行 PGVector 和 Ollama。

    然后，在 Spring 应用中添加 Spring AI Ollama 和 PGVector 依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
    </dependency>
    ```

    我们还将添加 Spring Boot 的 [Docker Compose](https://www.baeldung.com/docker-compose-support-spring-boot) 支持，用于管理 Ollama 和 PGVector 的 Docker 容器：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-docker-compose</artifactId>
        <version>3.1.1</version>
    </dependency>
    ```

    接着，在 `docker-compose.yml` 文件中定义这两个服务：

    ```yaml
    services:
    postgres:
        image: pgvector/pgvector:pg17
        environment:
        POSTGRES_DB: vectordb
        POSTGRES_USER: postgres
        POSTGRES_PASSWORD: postgres
        ports:
        - "5434:5432"
        healthcheck:
        test: ["CMD-SHELL", "pg_isready -U postgres"]
        interval: 10s
        timeout: 5s
        retries: 5

    ollama:
        image: ollama/ollama:latest
        ports:
        - "11435:11434"
        volumes:
        - ollama_data:/root/.ollama
        healthcheck:
        test: ["CMD", "curl", "-f", "http://localhost:11435/api/health"]
        interval: 10s
        timeout: 5s
        retries: 10

    volumes:
    ollama_data:
    ```

4. 应用配置

    接下来，配置 Spring Boot 应用以使用 Ollama 和 PGVector 服务。在 `application.yml` 中定义以下属性，特别注意 `ollama` 和 `vectorstore` 部分：

    ```yaml
    spring:
    ai:
        ollama:
        init:
            pull-model-strategy: when_missing
            chat:
            include: true
        embedding:
            options:
            model: nomic-embed-text
        vectorstore:
        pgvector:
            initialize-schema: true
            dimensions: 768
            index-type: hnsw
    docker:
        compose:
        file: docker-compose.yml
        enabled: true
    datasource:
        url: jdbc:postgresql://localhost:5434/vectordb
        username: postgres
        password: postgres
        driver-class-name: org.postgresql.Driver
    jpa:
        database-platform: org.hibernate.dialect.PostgreSQLDialect
    ```

    - 我们选用 [nomic-embed-text](https://ollama.com/library/nomic-embed-text) 作为 Ollama 的嵌入模型。若本地未下载，Spring AI 会自动拉取。
    - PGVector 配置确保向量存储正确初始化：
    - `initialize-schema: true`：自动初始化数据库模式。
    - `dimensions: 768`：设置向量维度，匹配常见嵌入模型。
    - `index-type: hnsw`：使用“分层可导航小世界”（HNSW）索引，优化近似最近邻搜索效率。

5. 实现语义搜索

    基础设施准备就绪后，我们实现一个简单的语义搜索应用。本用例为“智能图书搜索引擎”，允许用户根据内容搜索书籍。

    我们先使用 PGVector 实现基础搜索功能，再结合 Ollama 提供更具上下文感知的增强响应。

    首先定义表示图书实体的 `Book` 类：

    ```java
    public record Book(String title, String author, String description) {
    }
    ```

    在搜索前，需将图书数据导入 PGVector 存储。以下方法添加示例图书数据：

    ```java
    void run() {
        var books = List.of(
                new Book("The Great Gatsby", "F. Scott Fitzgerald", "The Great Gatsby is a 1925 novel by American writer F. Scott Fitzgerald. Set in the Jazz Age on Long Island, near New York City, the novel depicts first-person narrator Nick Carraway's interactions with mysterious millionaire Jay Gatsby and Gatsby's obsession to reunite with his former lover, Daisy Buchanan."),
                new Book("To Kill a Mockingbird", "Harper Lee", "To Kill a Mockingbird is a novel by the American author Harper Lee. It was published in 1960 and was instantly successful. In the United States, it is widely read in high schools and middle schools."),
                new Book("1984", "George Orwell", "Nineteen Eighty-Four: A Novel, often referred to as 1984, is a dystopian social science fiction novel by the English novelist George Orwell. It was published on 8 June 1949 by Secker & Warburg as Orwell's ninth and final book completed in his lifetime."),
                new Book("The Catcher in the Rye", "J. D. Salinger", "The Catcher in the Rye is a novel by J. D. Salinger, partially published in serial form in 1945–1946 and as a novel in 1951. It was originally intended for adults but is often read by adolescents for its themes of angst, alienation, and as a critique on superficiality in society."),
                new Book("Lord of the Flies", "William Golding", "Lord of the Flies is a 1954 novel by Nobel Prize-winning British author William Golding. The book focuses on a group of British")
        );

        List<Document> documents = books.stream()
                .map(book -> new Document(book.toString()))
                .toList();

        vectorStore.add(documents);
    }
    ```

    数据导入完成后，即可实现语义搜索功能。

    1. 基础语义搜索

        目标是实现一个语义搜索 API，允许用户根据内容查找图书。

        定义 `BookSearchController`，与 PGVector 交互执行相似度搜索：

        ```java
        @RequestMapping("/books")
        class BookSearchController {
            final VectorStore vectorStore;
            final ChatClient chatClient;

            BookSearchController(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
                this.vectorStore = vectorStore;
                this.chatClient = chatClientBuilder.build();
            }
            // ...
        }
        ```

        创建 `POST /search` 端点，接收用户查询并返回匹配图书列表：

        ```java
        @PostMapping("/search")
        List<String> semanticSearch(@RequestBody String query) {
            return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(3)
                .build())
            .stream()
            .map(Document::getText)
            .toList();
        }
        ```

        注意：我们使用了 `VectorStore#similaritySearch`，对之前导入的图书执行语义搜索。

        启动应用后，即可进行搜索。使用 cURL 搜索包含“1984”的图书：

        ```bash
        curl -X POST --data "1984" http://localhost:8080/books/search
        ```

        响应返回三本图书：一本精确匹配，两本部分匹配：

        ```json
        [
        "Book[title=1984, author=George Orwell, description=Nineteen Eighty-Four: A Novel, often referred to as 1984, is a dystopian social science fiction novel by the English novelist George Orwell.]",
        "Book[title=The Catcher in the Rye, author=J. D. Salinger, description=The Catcher in the Rye is a novel by J. D. Salinger, partially published in serial form in 1945–1946 and as a novel in 1951.]",
        "Book[title=To Kill a Mockingbird, author=Harper Lee, description=To Kill a Mockingbird is a novel by the American author Harper Lee.]"
        ]
        ```

    2. 使用 Ollama 增强语义搜索

        我们可以集成 Ollama，生成改写后的响应，提供额外上下文以提升搜索结果质量，步骤如下：

        1. 从搜索结果中提取前三本匹配图书的描述。
        2. 将这些描述输入 Ollama，生成更自然、更具上下文感知的响应。
        3. 返回包含摘要和改写信息的响应，提供更清晰、更相关的洞察。

        在 `BookSearchController` 中创建新方法，使用 Ollama 生成查询的改写版本：

        ```java
        @PostMapping("/enhanced-search")
        String enhancedSearch(@RequestBody String query) {
            String context = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(3)
                .build())
            .stream()
            .map(Document::getText)
            .reduce("", (a, b) -> a + b + "\n");

            return chatClient.prompt()
            .system(context)
            .user(query)
            .call()
            .content();
        }
        ```

        现在，通过向 `/books/enhanced-search` 端点发送 POST 请求测试增强语义搜索功能：

        ```bash
        curl -X POST --data "1984" http://localhost:8080/books/enhanced-search

        1984 is a classic dystopian novel written by George Orwell. Here's an excerpt from the book:

        "He loved Big Brother. He even admired him. After all, who wouldn't? Big Brother was all-powerful, all-knowing, and infinitely charming. And now that he had given up all his money in bank accounts with his names on them, and his credit cards, and his deposit slips, he felt free."

        This excerpt sets the tone for the novel, which depicts a totalitarian society where the government exercises total control over its citizens. The protagonist, Winston Smith, is a low-ranking member of the ruling Party who begins to question the morality of their regime.

        Would you like to know more about the book or its themes?
        ```

        与基础语义搜索返回三本独立图书描述不同，Ollama 综合了搜索结果中最相关的信息。本例中，《1984》是最相关匹配项，因此 Ollama 聚焦于提供详细摘要，而非列出无关图书。这种方式模拟了类人搜索助手，使结果更具吸引力和洞察力。

6. 结论

    在本文中，我们探索了如何使用 Spring AI、PGVector 和 Ollama 实现语义搜索。我们对比了两个端点：一个对图书目录执行语义搜索，另一个将搜索结果输入 Ollama 大语言模型进行增强处理。

    通过结合向量数据库的高效相似度搜索与大语言模型的自然语言理解能力，我们构建了一个既快速又智能的搜索系统，为用户提供更相关、更人性化的搜索体验。
