# [使用 Hugging Face 模型与 Spring AI 和 Ollama](https://www.baeldung.com/spring-ai-ollama-hugging-face-models)

人工智能 · Spring AI · LLM

1. 概述

    人工智能正在改变我们构建 Web 应用程序的方式。[Hugging Face](https://huggingface.co/) 是一个流行平台，提供大量[开源](https://www.baeldung.com/cs/open-source-explained)和预训练的大型语言模型（LLM）。

    我们可以使用 Ollama（一个开源工具）在本地机器上运行 LLM。它支持运行来自 Hugging Face 的 [GGUF](https://huggingface.co/docs/hub/en/gguf) 格式模型。

    在本教程中，我们将探索如何结合使用 Hugging Face 模型、Spring AI 和 Ollama。我们将使用聊天补全模型构建一个简单的聊天机器人，并使用嵌入模型实现语义搜索。

2. 依赖项

    首先，在项目的 `pom.xml` 文件中添加必要的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        <version>1.0.0-M6</version>
    </dependency>
    ```

    Ollama Starter 依赖项帮助我们与 Ollama 服务建立连接。我们将使用它来拉取并运行聊天补全和嵌入模型。

    由于当前版本 `1.0.0-M5` 是里程碑版本，我们还需要在 `pom.xml` 中添加 Spring Milestones 仓库：

    ```xml
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
    ```

    此仓库用于发布里程碑版本，而非标准的 Maven Central 仓库。

3. 使用 Testcontainers 设置 Ollama

    为便于本地开发和测试，我们将使用 Testcontainers 来设置 Ollama 服务。

    1. 测试依赖项

        首先，在 `pom.xml` 中添加必要的测试依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>ollama</artifactId>
            <scope>test</scope>
        </dependency>
        ```

        我们导入了 Spring AI Testcontainers 依赖项和 Testcontainers 的 Ollama 模块。

    2. 定义 Testcontainers Bean

        接下来，创建一个 `@TestConfiguration` 类来定义 Testcontainers Bean：

        ```java
        @TestConfiguration(proxyBeanMethods = false)
        class TestcontainersConfiguration {
            @Bean
            public OllamaContainer ollamaContainer() {
                return new OllamaContainer("ollama/ollama:0.5.4");
            }

            @Bean
            public DynamicPropertyRegistrar dynamicPropertyRegistrar(OllamaContainer ollamaContainer) {
                return registry -> {
                    registry.add("spring.ai.ollama.base-url", ollamaContainer::getEndpoint);
                };
            }
        }
        ```

        创建 `OllamaContainer` Bean 时，我们指定了最新稳定版本的 Ollama 镜像。

        然后，我们定义了一个 `DynamicPropertyRegistrar` Bean，用于配置 Ollama 服务的 `base-url`，以便应用程序可以连接到已启动的 Ollama 容器。

    3. 在开发过程中使用 Testcontainers

        虽然 Testcontainers 主要用于集成测试，但我们也可以在本地开发过程中使用它。

        为此，我们在 `src/test/java` 目录中创建一个单独的主类：

        ```java
        public class TestApplication {
            public static void main(String[] args) {
                SpringApplication.from(Application::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
            }
        }
        ```

        我们创建了 `TestApplication` 类，并在其 `main()` 方法中启动主 `Application` 类，并附加 `TestcontainersConfiguration` 类。

        此设置帮助我们在运行 Spring Boot 应用程序时，自动连接到通过 Testcontainers 启动的 Ollama 服务。

4. 使用聊天补全模型

    现在我们已设置好本地 Ollama 容器，让我们使用聊天补全模型构建一个简单的聊天机器人。

    1. 配置聊天模型和聊天机器人 Bean

        首先，在 `application.yaml` 文件中配置聊天补全模型：

        ```yaml
        spring:
        ai:
            ollama:
            init:
                pull-model-strategy: when_missing
            chat:
                options:
                model: hf.co/microsoft/Phi-3-mini-4k-instruct-gguf
        ```

        要配置 Hugging Face 模型，我们使用格式 `hf.co/{username}/{repository}`。这里我们指定了由 Microsoft 提供的 [Phi-3-mini-4k-instruct](https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf) 模型的 GGUF 版本。

        这不是强制要求，我们建议您在本地搭建代码库并尝试更多[聊天补全模型](https://huggingface.co/models?library=gguf&other=text-generation-inference&sort=downloads)。

        此外，我们将 `pull-model-strategy` 设置为 `when_missing`，这确保 Spring AI 在模型本地不存在时自动拉取指定模型。

        配置有效模型后，Spring AI 会自动创建一个 `ChatModel` 类型的 Bean，供我们与聊天补全模型交互。

        让我们用它来定义聊天机器人所需的其他 Bean：

        ```java
        @Configuration
        class ChatbotConfiguration {
            @Bean
            public ChatMemory chatMemory() {
                return new InMemoryChatMemory();
            }

            @Bean
            public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
                return ChatClient
                .builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
            }
        }
        ```

        首先，我们定义了一个 `ChatMemory` Bean，使用 `InMemoryChatMemory` 实现。它通过在内存中存储聊天历史来维护对话上下文。

        接着，我们使用 `ChatMemory` 和 `ChatModel` Bean 创建了一个 `ChatClient` 类型的 Bean，这是我们与聊天补全模型交互的主要入口。

    2. 实现聊天机器人

        配置完成后，让我们创建一个 `ChatbotService` 类。我们将注入之前定义的 `ChatClient` Bean 来与模型交互。

        首先，定义两个简单的记录类来表示聊天请求和响应：

        ```java
        record ChatRequest(@Nullable UUID chatId, String question) {}

        record ChatResponse(UUID chatId, String answer) {}
        ```

        `ChatRequest` 包含用户的问题和一个可选的 `chatId`，用于标识正在进行的对话。

        同样，`ChatResponse` 包含 `chatId` 和聊天机器人的回答。

        现在，实现核心功能：

        ```java
        public ChatResponse chat(ChatRequest chatRequest) {
            UUID chatId = Optional
            .ofNullable(chatRequest.chatId())
            .orElse(UUID.randomUUID());
            String answer = chatClient
            .prompt()
            .user(chatRequest.question())
            .advisors(advisorSpec ->
                advisorSpec
                    .param("chat_memory_conversation_id", chatId))
            .call()
            .content();
            return new ChatResponse(chatId, answer);
        }
        ```

        如果传入的请求不包含 `chatId`，我们会生成一个新的。这允许用户开启新对话或继续现有对话。

        我们将用户的问题传递给 `chatClient` Bean，并设置 `chat_memory_conversation_id` 参数为解析后的 `chatId`，以维护对话历史。

        最后，我们返回聊天机器人的回答以及 `chatId`。

    3. 与聊天机器人交互

        现在我们已实现服务层，让我们在其之上暴露一个 REST API：

        ```java
        @PostMapping("/chat")
        public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
            ChatResponse chatResponse = chatbotService.chat(chatRequest);
            return ResponseEntity.ok(chatResponse);
        }
        ```

        我们将使用上述 API 端点与聊天机器人交互。

        让我们使用 HTTPie CLI 开启一个新对话：

        ```bash
        http POST :8080/chat question="Who wanted to kill Harry Potter?"
        ```

        我们向聊天机器人发送一个简单问题，看看会得到什么响应：

        ```json
        {
        "chatId": "7b8a36c7-2126-4b80-ac8b-f9eedebff28a",
        "answer": "Lord Voldemort, also known as Tom Riddle, wanted to kill Harry Potter because of a prophecy that foretold a boy born at the end of July would have the power to defeat him."
        }
        ```

        响应包含一个唯一的 `chatId` 和聊天机器人对问题的回答。

        让我们使用上述响应中的 `chatId` 发送一个后续问题，以继续对话：

        ```bash
        http POST :8080/chat chatId="7b8a36c7-2126-4b80-ac8b-f9eedebff28a" question="Who should he have gone after instead?"
        ```

        看看聊天机器人是否能保持对话上下文并提供相关回答：

        ```json
        {
        "chatId": "7b8a36c7-2126-4b80-ac8b-f9eedebff28a",
        "answer": "Based on the prophecy's criteria, Voldemort could have targeted Neville Longbottom instead, as he was also born at the end of July to parents who had defied Voldemort three times."
        }
        ```

        如我们所见，聊天机器人确实保持了对话上下文，因为它引用了我们在上一条消息中讨论的预言。

        `chatId` 保持不变，表明后续回答是同一对话的延续。

5. 使用嵌入模型

    从聊天补全模型转向，我们现在将使用嵌入模型在一小部分语录数据集上实现语义搜索。

    我们将从外部 API 获取语录，将其存储在内存向量存储中，并执行语义搜索。

    1. 从外部 API 获取语录记录

        为演示目的，我们将使用 [QuoteSlate API](https://quoteslate.vercel.app/) 获取语录。

        让我们为此创建一个 `QuoteFetcher` 工具类：

        ```java
        class QuoteFetcher {
            private static final String BASE_URL = "https://quoteslate.vercel.app";
            private static final String API_PATH = "/api/quotes/random";
            private static final int DEFAULT_COUNT = 50;

            public static List<Quote> fetch() {
                return RestClient
                .create(BASE_URL)
                .get()
                .uri(uriBuilder ->
                    uriBuilder
                        .path(API_PATH)
                        .queryParam("count", DEFAULT_COUNT)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
            }
        }

        record Quote(String quote, String author) {}
        ```

        使用 `RestClient`，我们调用 QuoteSlate API，默认获取 50 条语录，并使用 `ParameterizedTypeReference` 将 API 响应反序列化为 `Quote` 记录列表。

    2. 配置并填充内存向量存储

        现在，在 `application.yaml` 中配置嵌入模型：

        ```yaml
        spring:
        ai:
            ollama:
            embedding:
                options:
                model: hf.co/nomic-ai/nomic-embed-text-v1.5-GGUF
        ```

        我们使用由 `nomic-ai` 提供的 [nomic-embed-text-v1.5](https://huggingface.co/nomic-ai/nomic-embed-text-v1.5-GGUF) 模型的 GGUF 版本。同样，您可以自由尝试[其他嵌入模型](https://huggingface.co/models?library=gguf&other=text-embeddings-inference&sort=downloads)。

        指定有效模型后，Spring AI 会自动为我们创建一个 `EmbeddingModel` 类型的 Bean。

        让我们用它来创建一个向量存储 Bean：

        ```java
        @Bean
        public VectorStore vectorStore(EmbeddingModel embeddingModel) {
            return SimpleVectorStore
            .builder(embeddingModel)
            .build();
        }
        ```

        为演示目的，我们创建了一个 `SimpleVectorStore` 类的 Bean。这是一个内存实现，使用 `java.util.Map` 类模拟向量存储。

        现在，为了在应用程序启动时用语录填充向量存储，我们创建一个实现 `ApplicationRunner` 接口的 `VectorStoreInitializer` 类：

        ```java
        @Component
        class VectorStoreInitializer implements ApplicationRunner {
            private final VectorStore vectorStore;

            // standard constructor

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

        在 `VectorStoreInitializer` 中，我们自动装配了一个 `VectorStore` 实例。

        在 `run()` 方法中，我们使用 `QuoteFetcher` 工具类获取 `Quote` 记录列表。然后，我们将每条语录映射为一个 `Document`，并将作者字段配置为元数据。

        最后，我们将所有文档存储到向量存储中。当我们调用 `add()` 方法时，Spring AI 会自动将我们的纯文本内容转换为向量表示后再存储到向量存储中，无需我们显式使用 `EmbeddingModel` Bean 进行转换。

    3. 测试语义搜索

        向量存储填充完成后，让我们验证语义搜索功能：

        ```java
        private static final int MAX_RESULTS = 3;

        @ParameterizedTest
        @ValueSource(strings = {"Motivation", "Happiness"})
        void whenSearchingQuotesByTheme_thenRelevantQuotesReturned(String theme) {
            SearchRequest searchRequest = SearchRequest
            .builder()
            .query(theme)
            .topK(MAX_RESULTS)
            .build();
            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            assertThat(documents)
            .hasSizeBetween(1, MAX_RESULTS)
            .allSatisfy(document -> {
                String title = String.valueOf(document.getMetadata().get("author"));
                assertThat(title)
                    .isNotBlank();
            });
        }
        ```

        这里，我们使用 `@ValueSource` 向测试方法传入一些常见的语录主题。然后，我们创建一个 `SearchRequest` 对象，以主题作为查询，`MAX_RESULTS` 作为期望结果数量。

        接着，我们使用 `searchRequest` 调用 `vectorStore` Bean 的 `similaritySearch()` 方法。与 `VectorStore` 的 `add()` 方法类似，Spring AI 会在查询向量存储前将我们的查询转换为其向量表示。

        返回的文档将包含与给定主题语义相关的语录，即使它们不包含确切的关键词。

6. 结论

    在本文中，我们探索了如何将 Hugging Face 模型与 Spring AI 结合使用。

    通过 Testcontainers，我们设置了 Ollama 服务，创建了一个本地测试环境。

    首先，我们使用聊天补全模型构建了一个简单的聊天机器人。然后，我们使用嵌入模型实现了语义搜索。
