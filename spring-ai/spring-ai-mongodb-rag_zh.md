# [使用MongoDB和Spring AI构建RAG应用程序](https://www.baeldung.com/spring-ai-mongodb-rag)

1. 一览表

    人工智能技术的使用正在成为现代发展的关键技能。在本文中，我们将构建一个RAG Wiki应用程序，该应用程序可以根据存储的文档回答问题。

    我们将使用Spring AI将我们的应用程序与[MongoDB Vector数据库](https://www.mongodb.com/products/platform/atlas-vector-search)和LLM集成。

2. RAG应用程序

    当自然语言生成需要依赖上下文数据时，我们使用检索增强生成（Retrieval-Augmented Generation, RAG）应用程序。RAG应用程序的一个关键组成部分是矢量数据库，它在有效管理和检索这些数据方面发挥着至关重要的作用：

    ![RAG应用程序中的文档人口和提示流](pic/RAG-app-arch-1.png)

    我们使用嵌入模型来处理源文档。嵌入模型将文档中的文本转换为高维矢量。这些矢量捕获了内容的语义含义，使我们能够根据上下文比较和检索类似内容，而不仅仅是关键字匹配。然后我们将文档存储在矢量存储中。

    一旦我们保存了文档，我们就可以通过以下方式根据它们发送提示：

    - 首先，我们使用嵌入模型来处理问题，将其转换为捕获其语义含义的矢量。
    - 接下来，我们执行相似性搜索，将问题的矢量与存储在矢量存储中的文档矢量进行比较。
    - 从最相关的文件中，我们为这个问题构建了一个背景。
    - 最后，我们将问题及其上下文发送给LLM，LLM构建了与查询相关的响应，并由提供的上下文丰富。

3. MongoDB Atlas矢量搜索

    在本教程中，我们将使用MongoDB Atlas Search作为我们的矢量存储。它提供了矢量搜索功能，满足了我们在这个项目中的需求。为了测试目的设置MongoDB Atlas Search的本地实例，我们将使用mongodb-atlas-local docker容器。让我们创建一个docker-compose.yml文件：

    ```yml
    version: '3.1'

    services:
    my-mongodb:
        image: mongodb/mongodb-atlas-local:7.0.9
        container_name: my-mongodb
        environment:
        - MONGODB_INITDB_ROOT_USERNAME=wikiuser
        - MONGODB_INITDB_ROOT_PASSWORD=password
        ports:
        - 27017:27017
    ```

4. 依赖项和配置

    让我们从添加必要的依赖项开始。由于我们的应用程序将提供HTTP API，我们将包含spring-boot-starter-web依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
    ```

    此外，我们将使用开放的AI API客户端连接到LLM，因此让我们也添加其依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
    ```

    最后，我们将添加MongoDB Atlas Store依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mongodb-atlas-store-spring-boot-starter</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
    ```

    现在，让我们为我们的应用程序添加配置属性：

    ```yml
    spring:
    data:
        mongodb:
        uri: mongodb://wikiuser:password@localhost:27017/admin
        database: wiki
    ai:
        vectorstore:
        mongodb:
            collection-name: vector_store
            initialize-schema: true
            path-name: embedding
            indexName: vector_index
        openai:
            api-key: ${OPENAI_API_KEY}
            chat:
                options:
                model: gpt-3.5-turbo
    ```

    我们已经指定了MongoDB URL和数据库，我们还通过设置集合名称、嵌入字段名称和矢量索引名称来配置矢量存储。多亏了initialize-schema属性，所有这些工件都将由Spring AI框架自动创建。

    最后，我们添加了[开放的AI API密钥](https://platform.openai.com/api-keys)和[模型版本](https://platform.openai.com/docs/models)。

5. 将文档保存到矢量存储

    现在，我们正在将数据保存到我们的矢量存储中。我们的应用程序将负责根据现有文档为用户问题提供答案——本质上是一种维基(WiKi)。

    让我们添加一个模型，该模型将存储文件的内容和文件路径：

    ```java
    public class WikiDocument {
        private String filePath;
        private String content;
        // standard getters and setters
    }
    ```

    下一步，我们将实现WikiDocumentsRepository。在此存储库中，我们封装所有持久性逻辑，即saveWikiDocument方式：

    ![WikiDocumentsRepository.java](/src/main/java/com/baeldung/springai/rag/mongodb/repository/WikiDocumentsRepository.java)

    在这里，我们注入了[VectorStore](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)接口bean，它将由spring-ai-mongodb-atlas-store-spring-boot-starter提供的MongoDBAtlasVectorStore实现。在saveWikiDocument方法中，我们创建一个文档实例，并填充内容和元数据。

    然后我们使用TokenTextSplitter将文档分解成更小的块，并将其保存在我们的矢量存储中。现在让我们创建一个WikiDocumentsServiceImpl：

    ![WikiDocumentsServiceImpl.java](/src/main/java/com/baeldung/springai/rag/mongodb/service/WikiDocumentsServiceImpl.java)

    在服务层，我们检索文件内容，创建WikiDocument实例，并将其发送到存储库以进行持久化。

    在控制器中，我们将简单地将文件路径传递给服务层，如果文档成功保存，我们将返回201状态代码：

    ```java
    @RestController
    @RequestMapping("wiki")
    public class WikiDocumentsController {
        private final WikiDocumentsServiceImpl wikiDocumentsService;

        // constructors

        @PostMapping
        public ResponseEntity<Void> saveDocument(@RequestParam String filePath) {
            wikiDocumentsService.saveWikiDocument(filePath);
            return ResponseEntity.status(201).build();
        }
    }
    ```

    现在，让我们开始我们的应用程序，看看我们的流程是如何运作的。让我们添加Spring Boot测试依赖项，这将允许我们设置测试Web上下文：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
    ```

    现在，我们将引导测试应用程序实例 RAGMongoDBApplicationManualTest，其中包含两个文档调用POST端点的方法 givenMongoDBVectorStore_whenCallingPostDocumentEndpoint_thenExpectedResponseCodeShouldBeReturned()。

    这两个呼叫都应该返回201状态代码，因此添加了文档。我们可以使用MongoDB Compass来确认文档已成功保存到矢量存储：
    在[MongoDB Compass](https://www.mongodb.com/docs/compass/current/install/)中查看存储的文档。

    ![Mongo-1.png](pic/Mongo-1-1024x857.webp)

    正如我们所看到的—两份文件都被保存了。我们可以看到原始内容以及嵌入数组。

6. 相似性搜索

    让我们添加相似性搜索功能。我们将在我们的存储库(WikiDocumentsRepository.java)中添加一个findSimilarDocuments方法。

    我们从VectorStore调用了[相似性搜索](https://docs.spring.io/spring-ai/reference/api/vectordbs/mongodb.html#_performing_similarity_search)方法。除了搜索文本外，我们还指定了结果限制和相似性阈值。相似性阈值参数允许我们控制文档内容应该与我们的搜索文本匹配的程度。

    在服务层中，我们将代理对存储库的调用：

    ```java
    public List<WikiDocument> findSimilarDocuments(String searchText) {
        return wikiDocumentsRepository.findSimilarDocuments(searchText);
    }
    ```

    在控制器中，让我们添加一个GET端点，该端点接收搜索文本作为参数并将其传递给服务：

    ```java
    @RestController
    @RequestMapping("/wiki")
    public class WikiDocumentsController {
        @GetMapping
        public List<WikiDocument> get(@RequestParam("searchText") String searchText) {
            return wikiDocumentsService.findSimilarDocuments(searchText);
        }
    }
    ```

    添加 givenMongoDBVectorStoreWithDocuments_whenMakingSimilaritySearch_thenExpectedDocumentShouldBePresent() 测试方法调用我们的新GET端点，看看相似性搜索是如何工作的：

    我们使用文档中不完全匹配(exact match)的搜索文本调用端点。不过，我们仍然检索到了内容相似的文档，并确认其中包含了我们存储在 rag-documentation.md 文件中的文本。

7. 提示端点

    让我们开始构建提示流程，这是我们应用程序的核心功能。我们将从AdvisorConfiguration开始：

    ```java
    @Configuration
    public class AdvisorConfiguration {
        @Bean
        public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
            return new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults());
        }
    }
    ```

    我们创建了一个QuestionAnswerAdvisor bean，负责构建提示请求，包括初始问题。此外，它将附加矢量存储的相似性搜索响应作为问题的上下文。

    现在，让我们将搜索端点"/search"添加到我们的WikiDocumentsController中：

    ![WikiDocumentsController.java](/src/main/java/com/baeldung/springai/rag/mongodb/controller/WikiDocumentsController.java)

    在这里，我们通过将用户的输入添加到提示中并附加我们的`QuestionAnswerAdvisor`来构建一个提示请求。

    最后，通过givenMongoDBVectorStoreWithDocumentsAndLLMClient_whenAskQuestionAboutRAG_thenExpectedResponseShouldBeReturned()测试方法调用我们的端点"/wiki/search?question={question}"，看看它告诉我们关于RAG应用程序的信息。

    我们向我们的端点发送了“Explain the RAG applications”的问题，并记录了API响应：

    ```txt
    b.s.r.m.RAGMongoDBApplicationManualTest : Based on the context provided, the RAG AI Application is a tool
    used for storing documentation and enabling users to search for specific information efficiently...
    ```

    正如我们所看到的，端点根据我们之前保存在矢量数据库中的文档文件返回有关RAG应用程序的信息。

    现在让我们试着问一些我们知识库中肯定没有的东西：

    添加 givenMongoDBVectorStoreWithDocumentsAndLLMClient_whenAskUnknownQuestion_thenExpectedResponseShouldBeReturned() 发送了“Explain the Economic theory”的问题。
    现在我们问了经济理论，答案如下：

    ```txt
    b.s.r.m.RAGMongoDBApplicationManualTest : I'm sorry, but the economic theory is not directly related to the information provided about owls and the RAG AI Application.
    If you have a specific question about economic theory, please feel free to ask.
    ```

    这一次，我们的应用程序没有找到任何相关文件，也没有使用任何其他来源来提供答案。

8. 结论

    在本文中，我们使用Spring AI框架成功实现了RAG应用程序，这是集成各种AI技术的绝佳工具。此外，事实证明，MongoDB是处理矢量存储的有力选择。

    有了这种强大的组合，我们可以为各种目的构建基于人工智能的现代应用程序，包括聊天机器人、自动维基系统和搜索引擎。
