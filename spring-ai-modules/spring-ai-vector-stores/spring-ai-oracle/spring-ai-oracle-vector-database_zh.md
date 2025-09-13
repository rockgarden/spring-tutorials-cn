# [使用 Oracle 向量数据库与 Spring AI](https://www.baeldung.com/spring-ai-oracle-vector-database)

人工智能 · Spring AI · Spring Boot · 向量数据库  

1. 概述

    在传统数据库中，我们通常依赖精确关键词匹配或基础模式匹配来实现搜索功能。虽然这对简单应用已足够，但这种方法无法完全理解自然语言查询背后的语义和上下文。

    **向量存储（Vector Stores）** 通过将数据存储为捕捉其语义的数值向量来解决这一局限。语义相近的词会被聚类在一起，从而支持**相似性搜索**——即使结果中不包含查询中的确切关键词，数据库仍能返回相关结果。

    **[Oracle Database 23ai](https://www.oracle.com/database/23ai/)** 将向量存储能力集成到其现有生态系统中，使我们无需额外部署独立向量数据库即可构建 AI 应用。使用同一个数据库，我们可以同时实现传统结构化数据管理和向量相似性搜索。

    在本教程中，我们将探索如何将 Oracle 向量数据库与 Spring AI 集成。首先，我们将实现原生相似性搜索以查找语义相关的内容；随后，我们将在此基础上构建一个**检索增强生成（RAG）聊天机器人**。

2. 项目设置

    在深入实现之前，我们需要添加必要的依赖并正确配置应用程序。

    1. 依赖项

        首先，在项目的 `pom.xml` 文件中添加以下依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-oracle</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-advisors-vector-store</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
            <version>1.0.0</version>
        </dependency>
        ```

        - `spring-ai-starter-vector-store-oracle`：用于连接并操作 Oracle 向量数据库。
        - `spring-ai-advisors-vector-store`：为 RAG 实现提供向量存储顾问支持。
        - `spring-ai-starter-model-openai`：用于调用 OpenAI 的聊天补全和嵌入模型。

        由于项目中使用了多个 Spring AI Starter，我们还需在 `pom.xml` 中引入 Spring AI 的物料清单（BOM），以统一管理版本：

        ```xml
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.ai</groupId>
                    <artifactId>spring-ai-bom</artifactId>
                    <version>1.0.0</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
        ```

        添加后，我们可从 Starter 依赖中移除 `version` 标签。BOM 能有效避免版本冲突，确保 Spring AI 各组件相互兼容。

    2. 配置 AI 模型与向量存储属性

        为了将文本数据转换为 Oracle 向量数据库可存储和搜索的向量，我们需要一个**嵌入模型**。此外，构建 RAG 聊天机器人还需一个**聊天补全模型**。

        本演示中，我们将使用 OpenAI 提供的模型。在 `application.yaml` 中配置 API 密钥和模型：

        ```yaml
        spring:
        ai:
            openai:
            api-key: ${OPENAI_API_KEY}
            embedding:
                options:
                model: text-embedding-3-large
            chat:
                options:
                model: gpt-4o
        ```

        我们使用 `${}` 占位符从环境变量加载 API 密钥。

        同时指定 [text-embedding-3-large](https://platform.openai.com/docs/models/text-embedding-3-large) 为嵌入模型，[gpt-4o](https://platform.openai.com/docs/models/gpt-4o) 为聊天补全模型。配置完成后，Spring AI 会自动创建 `ChatModel` 类型的 Bean，供后续使用。

        当然，我们也可以替换为其他模型或提供商，具体选择不影响本演示。

        接下来，为在向量数据库中存储和搜索数据，需先初始化其模式：

        ```yaml
        spring:
        ai:
            vectorstore:
            oracle:
                initialize-schema: true
        ```

        此处将 `spring.ai.vectorstore.oracle.initialize-schema` 设为 `true`，指示 Spring AI 在应用启动时自动创建默认向量存储模式，便于本地开发和测试。

        **注意**：生产环境中，应使用 Flyway 等数据库迁移工具手动定义模式。

3. 填充 Oracle 向量数据库

    配置完成后，我们建立一个在应用启动时自动填充 Oracle 向量数据库的工作流。

    1. 从外部 API 获取语录记录

        本演示中，我们将使用 [Breaking Bad Quotes API](https://breakingbadquotes.xyz/) 获取语录。

        创建 `QuoteFetcher` 工具类：

        ```java
        class QuoteFetcher {
            private static final String BASE_URL = "https://api.breakingbadquotes.xyz/v1/quotes/";
            private static final int DEFAULT_COUNT = 150;

            static List<Quote> fetch() {
                return fetch(DEFAULT_COUNT);
            }

            static List<Quote> fetch(int count) {
                return RestClient
                .create()
                .get()
                .uri(URI.create(BASE_URL + count))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
            }
        }

        record Quote(String quote, String author) {
        }
        ```

        使用 [RestClient](https://www.baeldung.com/spring-boot-restclient) 调用外部 API，默认获取 150 条语录，并通过 `ParameterizedTypeReference` 将响应反序列化为 `Quote` 记录列表。

    2. 将文档存储到向量数据库

        为在应用启动时填充 Oracle 向量数据库，创建实现 `ApplicationRunner` 接口的 `VectorStoreInitializer` 类：

        ```java
        @Component
        class VectorStoreInitializer implements ApplicationRunner {
            private final VectorStore vectorStore;

            // 标准构造函数

            @Override
            public void run(ApplicationArguments args) {
                List<Document> documents = QuoteFetcher
                .fetch()
                .stream()
                .map(quote -> {
                    Map<String, Object> metadata = Map.of("author", quote.author());
                    return new Document(quote.quote(), metadata);
                })
                .toList();
                vectorStore.add(documents);
            }
        }
        ```

        在 `VectorStoreInitializer` 中，我们自动注入 Spring AI 创建的 `VectorStore` 实例。

        在 `run()` 方法中，使用 `QuoteFetcher` 获取语录列表，将每条语录映射为 `Document`，并将作者字段设为元数据。

        最后，调用 `vectorStore.add(documents)` 存储所有文档。Spring AI 会在存储前自动将纯文本内容转换为向量表示。

4. 使用 Testcontainers 搭建本地测试环境

    为便于本地开发和测试，我们将使用 **[Testcontainers](https://www.baeldung.com/tag/testcontainers)** 启动 Oracle 向量数据库（需已安装并运行 [Docker](https://www.baeldung.com/ops/docker-guide)）。

    首先，在 `pom.xml` 中添加测试依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>oracle-free</artifactId>
        <scope>test</scope>
    </dependency>
    ```

    - `spring-ai-spring-boot-testcontainers`：提供 Spring Boot 与 Testcontainers 的集成支持。
    - `oracle-free`：Testcontainers 的 Oracle 数据库模块。

    这些依赖提供了启动 Oracle 向量数据库临时 Docker 实例所需的类。

    接着，创建 `@TestConfiguration` 类定义 Testcontainers Bean：

    ```java
    @TestConfiguration(proxyBeanMethods = false)
    class TestcontainersConfiguration {
        @Bean
        @ServiceConnection
        OracleContainer oracleContainer() {
            return new OracleContainer("gvenzl/oracle-free:23-slim");
        }
    }
    ```

    创建 `OracleContainer` Bean 时，我们指定最[新稳定版的 Oracle 数据库精简镜像](https://hub.docker.com/r/gvenzl/oracle-free/tags)。

    使用 [@ServiceConnection](https://www.baeldung.com/spring-boot-built-in-testcontainers#using-serviceconnection-for-dynamic-properties) 注解，动态注册连接 Docker 容器所需的所有数据源属性。

    现在，我们可通过在测试类上添加 `@Import(TestcontainersConfiguration.class)` 注解来使用该配置。

5. 执行相似性搜索

    本地测试环境搭建完成，且 Oracle 向量数据库已填充 Breaking Bad 语录，接下来探索如何执行相似性搜索。

    1. 基础相似性搜索

        首先执行基础相似性搜索，查找匹配不同 Breaking Bad 主题的语录：

        ```java
        private static final int MAX_RESULTS = 5;

        @Autowired
        private VectorStore vectorStore;

        @ParameterizedTest
        @ValueSource(strings = { "Sarcasm", "Regret", "Violence and Threats", "Greed, Power, and Money" })
        void whenSearchingBreakingBadTheme_thenRelevantQuotesReturned(String theme) {
            SearchRequest searchRequest = SearchRequest
            .builder()
            .query(theme)
            .topK(MAX_RESULTS)
            .build();

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            assertThat(documents)
            .hasSizeGreaterThan(0)
            .hasSizeLessThanOrEqualTo(MAX_RESULTS)
            .allSatisfy(document -> {
                assertThat(document.getText())
                    .isNotBlank();
                assertThat(String.valueOf(document.getMetadata().get("author")))
                    .isNotBlank();
            });
        }
        ```

        这里，我们使用 [@ValueSource](https://www.baeldung.com/parameterized-tests-junit-5#1-simple-values) 将 Breaking Bad 系列的主要主题传入测试方法。然后创建 `SearchRequest`，以主题为查询内容，并通过 `topK(MAX_RESULTS)` 限制结果为最相似的前五条语录。

        调用 `vectorStore.similaritySearch(searchRequest)` 时，Spring AI 会先将查询转换为向量表示，再查询数据库。

        返回的文档将包含与给定主题语义相关的语录，即使它们未包含确切关键词。

    2. 使用元数据过滤

        除基础相似性搜索外，Oracle 向量数据库还支持基于保存的元数据过滤搜索结果。这在需要缩小搜索范围、对数据子集进行语义搜索时非常有用。

        再次搜索与给定主题相关的语录，但按特定作者过滤：

        ```java
        @ParameterizedTest
        @CsvSource({
            "Walter White, Pride",
            "Walter White, Control",
            "Jesse Pinkman, Abuse and foul language",
            "Mike Ehrmantraut, Wisdom",
            "Saul Goodman, Law"
        })
        void whenSearchingCharacterTheme_thenRelevantQuotesReturned(String author, String theme) {
            SearchRequest searchRequest = SearchRequest
            .builder()
            .query(theme)
            .topK(MAX_RESULTS)
            .filterExpression(String.format("author == '%s'", author))
            .build();

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            assertThat(documents)
            .hasSizeGreaterThan(0)
            .hasSizeLessThanOrEqualTo(MAX_RESULTS)
            .allSatisfy(document -> {
                assertThat(document.getText())
                    .isNotBlank();
                assertThat(String.valueOf(document.getMetadata().get("author")))
                    .contains(author);
            });
        }
        ```

        这里，我们使用 `@CsvSource` 注解，通过角色-主题组合查找语录。

        与之前一样构建 `SearchRequest`，但这次使用 `filterExpression()` 方法将结果限制为特定作者的语录。

6. 构建 RAG 聊天机器人

    虽然原生相似性搜索本身已很强大，但我们可在此基础上构建一个智能、上下文感知的 RAG 聊天机器人。

    1. 定义提示模板

        为更好地引导 LLM 行为，我们定义一个自定义提示模板。在 `src/main/resources` 目录下创建 `prompt-template.st` 文件：

        ```txt
        You are a chatbot built for analyzing quotes from the 'Breaking Bad' television series.
        Given the quotes in the CONTEXT section, answer the query in the USER_QUESTION section.
        The response should follow the guidelines listed in the GUIDELINES section.

        CONTEXT:
        <question_answer_context>

        USER_QUESTION:
        <query>

        GUIDELINES:
        - Base your answer solely on the information found in the provided quotes.
        - Provide concise, direct answers without mentioning "based on the context" or similar phrases.
        - When referencing specific quotes, mention the character who said them.
        - If the question cannot be answered using the context, respond with "The provided quotes do not contain information to answer this question."
        - If the question is unrelated to the Breaking Bad show or the quotes provided, respond with "This question is outside the scope of the available Breaking Bad quotes."
        ```

        这里，我们明确定义了聊天机器人的角色，并提供了一组行为准则。

        模板中使用了两个尖括号 `< >` 包裹的占位符。Spring AI 会自动将其替换为从向量数据库检索的上下文和用户问题。

    2. 配置 ChatClient Bean

        接着，定义 `ChatClient` 类型的 Bean，作为与配置的聊天补全模型交互的主要入口：

        ```java
        private static final int MAX_RESULTS = 10;

        @Bean
        PromptTemplate promptTemplate(
        @Value("classpath:prompt-template.st") Resource promptTemplate) {
            String template = promptTemplate.getContentAsString(StandardCharsets.UTF_8);
            return PromptTemplate
            .builder()
            .renderer(StTemplateRenderer
                .builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build())
            .template(template)
            .build();
        }

        @Bean
        ChatClient chatClient(
        ChatModel chatModel,
        VectorStore vectorStore,
        PromptTemplate promptTemplate) {
            return ChatClient
            .builder(chatModel)
            .defaultAdvisors(
                QuestionAnswerAdvisor
                .builder(vectorStore)
                .promptTemplate(promptTemplate)
                .searchRequest(SearchRequest
                    .builder()
                    .topK(MAX_RESULTS)
                    .build())
                .build()
            )
            .build();
        }
        ```

        首先，使用 `@Value` 注解读取提示模板内容，定义 `PromptTemplate` Bean，并配置其使用尖括号作为分隔符。

        接着，使用 `PromptTemplate`、`ChatModel` 和 `VectorStore` Bean 定义 `ChatClient` Bean。通过 `defaultAdvisors()` 方法注册 `QuestionAnswerAdvisor`，该组件实现了 RAG 模式。

        在顾问中，我们配置 `SearchRequest` 以检索最相关的前 10 条语录。Spring AI 会在调用 LLM 前将这些语录注入提示模板。

    3. 执行 RAG 操作

        现在，`ChatClient` Bean 已配置完成，让我们看看如何与其交互以回答自然语言问题：

        ```java
        @Autowired
        private ChatClient chatClient;

        private static final String OUT_OF_SCOPE_MESSAGE = "此问题超出可用《绝命毒师》语录范围。";
        private static final String NO_INFORMATION_MESSAGE = "提供的语录中无相关信息可回答此问题。";

        @ParameterizedTest
        @ValueSource(strings = {
            "剧中如何刻画导师与学生之间的动态关系？",
            "哪些角色通过语录表现出不安全感？",
            "剧中是否有不适合年轻观众的成熟主题语录？"
        })
        void whenQuestionsRelatedToBreakingBadAsked_thenRelevantAnswerReturned(String userQuery) {
            String response = chatClient
            .prompt(userQuery)
            .call()
            .content();

            assertThat(response)
            .isNotBlank()
            .doesNotContain(OUT_OF_SCOPE_MESSAGE, NO_INFORMATION_MESSAGE);
        }
        ```

        当我们将 `userQuery` 传递给 `prompt()` 方法时，配置的 `QuestionAnswerAdvisor` 会在后台执行 RAG 工作流：查询 Oracle 向量数据库获取相关语录，将其注入提示模板，并将组合后的提示发送给 LLM 生成回答。

        我们验证响应非空，且不包含模板中定义的备用消息。

7. 结论

    在本文中，我们探索了如何将 Oracle 向量数据库与 Spring AI 集成。

    我们完成了必要的配置，实现了向量存储的两大核心能力：**相似性搜索** 和 **RAG**。借助 Testcontainers，我们搭建了本地 Oracle 向量数据库测试环境。

    首先，我们在应用启动时从 Breaking Bad Quotes API 获取语录并填充向量存储。接着，实现了对存储数据的相似性搜索，以检索匹配剧集常见主题的语录。

    最后，我们构建了一个 RAG 聊天机器人，利用相似性搜索检索的语录作为上下文，回答用户提问。

    通过 Spring AI 与 Oracle 向量数据库的结合，我们能够轻松构建语义感知、上下文丰富的 AI 应用，为用户提供更智能、更自然的交互体验。
