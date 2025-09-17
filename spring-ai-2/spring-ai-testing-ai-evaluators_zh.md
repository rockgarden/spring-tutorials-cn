# [使用 Spring AI 评估器测试 LLM 响应](https://www.baeldung.com/spring-ai-testing-ai-evaluators)

人工智能 | Spring AI | LLM 测试 | Testcontainers

1. 概述

    现代 Web 应用正越来越多地集成大型语言模型（LLMs），用于构建聊天机器人、虚拟助手等解决方案。

    然而，尽管 LLMs 功能强大，它们容易产生“幻觉”，其响应可能并不总是相关、恰当或事实准确。

    评估 LLM 响应的一种解决方案是使用另一个 LLM（最好是独立的模型）作为评估器。

    为此，Spring AI 定义了 `Evaluator` 接口，并提供了两个实现类，分别用于检查 LLM 响应的**相关性**和**事实准确性**：`RelevancyEvaluator` 和 `FactCheckingEvaluator`。

    在本教程中，我们将探索如何使用 Spring AI 评估器来测试 LLM 响应。我们将使用 Spring AI 提供的两个基本实现，对一个基于检索增强生成（RAG）的聊天机器人响应进行评估。

2. 构建 RAG 聊天机器人

    在测试 LLM 响应之前，我们需要先构建一个待测试的聊天机器人。本示例中，我们将构建一个简单的 RAG 聊天机器人，它基于一组文档回答用户问题。

    我们将使用开源工具 **[Ollama](https://github.com/ollama/ollama)**，在本地拉取并运行聊天补全模型和嵌入模型。

    1. 依赖项

        首先，在项目的 `pom.xml` 中添加必要依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
            <version>1.0.0-M5</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-markdown-document-reader</artifactId>
            <version>1.0.0-M5</version>
        </dependency>
        ```

        - `ollama-spring-boot-starter`：用于连接 Ollama 服务。
        - `markdown-document-reader`：用于将 `.md` 文件转换为可存入向量存储的文档对象。

        由于当前版本 `1.0.0-M5` 是里程碑版本，我们还需在 `pom.xml` 中添加 Spring Milestones 仓库：

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

        > ⚠️ 注意：里程碑版本发布在 Spring Milestones 仓库，而非标准的 Maven Central。

        为避免版本冲突，建议引入 Spring AI 的 BOM（物料清单）：

        ```xml
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.ai</groupId>
                    <artifactId>spring-ai-bom</artifactId>
                    <version>1.0.0-M5</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
        ```

        引入 BOM 后，可从依赖项中移除 `<version>` 标签，确保所有 Spring AI 组件版本兼容。

    2. 配置聊天补全模型与嵌入模型

        在 `application.yaml` 中配置模型：

        ```yaml
        spring:
        ai:
            ollama:
            chat:
                options:
                model: llama3.3
            embedding:
                options:
                model: nomic-embed-text
            init:
                pull-model-strategy: when_missing
        ```

        - 聊天模型：Meta 提供的 [llama3.3](https://ollama.com/library/llama3.3)
        - 嵌入模型：Nomic AI 提供的 [nomic-embed-text](https://ollama.com/library/nomic-embed-text)
        - `pull-model-strategy: when_missing`：本地不存在时自动拉取模型

        配置后，Spring AI 会自动创建 `ChatModel` 和 `EmbeddingModel` 的 Bean。

        接着，我们定义聊天机器人所需的其他 Bean：

        ```java
        @Bean
        public VectorStore vectorStore(EmbeddingModel embeddingModel) {
            return SimpleVectorStore
            .builder(embeddingModel)
            .build();
        }

        @Bean
        public ChatClient contentGenerator(ChatModel chatModel, VectorStore vectorStore) {
            return ChatClient.builder(chatModel)
            .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
            .build();
        }
        ```

        - `SimpleVectorStore`：内存实现的向量存储（生产环境建议使用 ChromaDB 等真实向量数据库）。
        - `QuestionAnswerAdvisor`：根据用户问题从向量库检索相关文档，并作为上下文提供给聊天模型。

    3. 填充内存向量存储

        我们在 `src/main/resources/documents/` 目录下放置了一个 `leave-policy.md` 文件，包含请假政策示例内容。

        为在[应用启动](https://www.baeldung.com/running-setup-logic-on-startup-in-spring#7-spring-boot-applicationrunner)时自动加载文档，创建 `VectorStoreInitializer` 类：

        ```java
        @Component
        class VectorStoreInitializer implements ApplicationRunner {
            private final VectorStore vectorStore;
            private final ResourcePatternResolver resourcePatternResolver;

            // 标准构造函数

            @Override
            public void run(ApplicationArguments args) {
                List<Document> documents = new ArrayList<>();
                Resource[] resources = resourcePatternResolver.getResources("classpath:documents/*.md");
                Arrays.stream(resources).forEach(resource -> {
                    MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig());
                    documents.addAll(markdownDocumentReader.read());
                });
                vectorStore.add(new TokenTextSplitter().split(documents));
            }
        }
        ```

        流程说明：

        - 使用 [ResourcePatternResolver](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/ResourcePatternResolver.html) 扫描 `documents/` 目录下所有 `.md` 文件。
        - 使用 `MarkdownDocumentReader` 将文件转换为 `Document` 对象。
        - 使用 `TokenTextSplitter` 将文档切分为小块后存入向量存储。
        - Spring AI 会自动调用嵌入模型将文本转换为向量，无需手动操作。

3. 使用 Testcontainers 配置 Ollama

    为方便本地开发与测试，我们将使用 **Testcontainers** 启动 Ollama 服务（需提前安装并运行 Docker）。

    1. 测试依赖

        在 `pom.xml` 中添加测试依赖：

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

    2. 定义 Testcontainers Bean

        创建 `@TestConfiguration` 类：

        ```java
        @TestConfiguration(proxyBeanMethods = false)
        class TestcontainersConfiguration {
            @Bean
            public OllamaContainer ollamaContainer() {
                return new OllamaContainer("ollama/ollama:0.5.7");
            }

            @Bean
            public DynamicPropertyRegistrar dynamicPropertyRegistrar(OllamaContainer ollamaContainer) {
                return registry -> {
                    registry.add("spring.ai.ollama.base-url", ollamaContainer::getEndpoint);
                };
            }
        }
        ```

        - 使用 `OllamaContainer` 启动指定版本的 Ollama Docker 容器。
        - 通过 `DynamicPropertyRegistrar` 动态设置 `base-url`，使应用连接到容器。

        在测试类上添加 `@Import(TestcontainersConfiguration.class)` 即可启用该配置。

4. 使用 Spring AI 评估器

    现在，我们已构建好 RAG 聊天机器人并配置了本地测试环境，接下来使用 Spring AI 的评估器测试其响应。

    1. 配置评估模型

        评估质量取决于评估模型的质量。我们选用当前业界领先的开源评估模型：**[bespoke-minicheck](https://ollama.com/library/bespoke-minicheck)**（由 Bespoke Labs 训练，在 [LLM-AggreFact](https://huggingface.co/datasets/lytang/LLM-AggreFact) 排行榜排名第一，仅输出 yes/no）。

        在 `application.yaml` 中配置：

        ```yaml
        com:
        baeldung:
            evaluation:
            model: bespoke-minicheck
        ```

        创建专用的评估用 `ChatClient`：

        ```java
        @Bean
        public ChatClient contentEvaluator(
        OllamaApi olamaApi,
        @Value("${com.baeldung.evaluation.model}") String evaluationModel
        ) {
            ChatModel chatModel = OllamaChatModel.builder()
            .ollamaApi(olamaApi)
            .defaultOptions(OllamaOptions.builder()
                .model(evaluationModel)
                .build())
            .modelManagementOptions(ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build())
            .build();
            return ChatClient.builder(chatModel)
            .build();
        }
        ```

        > 💡 说明：由于 `OllamaAutoConfiguration` 仅允许通过 `spring.ai.ollama.chat.options.model` 配置一个模型（我们已用于生成模型），因此需手动为评估模型创建独立的 `ChatClient`。

    2. 使用 RelevancyEvaluator 评估响应相关性

        `RelevancyEvaluator` 用于判断 LLM 响应是否与用户问题及检索到的上下文相关。

        创建 Bean：

        ```java
        @Bean
        public RelevancyEvaluator relevancyEvaluator(
            @Qualifier("contentEvaluator") ChatClient chatClient) {
            return new RelevancyEvaluator(chatClient.mutate());
        }
        ```

        测试示例：

        ```java
        String question = "我最多能请多少天病假？";
        ChatResponse chatResponse = contentGenerator.prompt()
        .user(question)
        .call()
        .chatResponse();

        String answer = chatResponse.getResult().getOutput().getContent();
        List<Document> documents = chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        EvaluationRequest evaluationRequest = new EvaluationRequest(question, documents, answer);

        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertThat(evaluationResponse.isPass()).isTrue(); // 应通过

        // 测试不相关答案
        String nonRelevantAnswer = "狮子是丛林之王";
        evaluationRequest = new EvaluationRequest(question, documents, nonRelevantAnswer);
        evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertThat(evaluationResponse.isPass()).isFalse(); // 应失败
        ```

    3. 使用 FactCheckingEvaluator 评估事实准确性

        `FactCheckingEvaluator` 用于验证 LLM 响应是否与检索到的上下文在事实上一致。

        创建 Bean：

        ```java
        @Bean
        public FactCheckingEvaluator factCheckingEvaluator(
            @Qualifier("contentEvaluator") ChatClient chatClient) {
            return new FactCheckingEvaluator(chatClient.mutate());
        }
        ```

        测试示例：

        ```java
        String question = "我最多能请多少天病假？";
        ChatResponse chatResponse = contentGenerator.prompt()
        .user(question)
        .call()
        .chatResponse();

        String answer = chatResponse.getResult().getOutput().getContent();
        List<Document> documents = chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        EvaluationRequest evaluationRequest = new EvaluationRequest(question, documents, answer);

        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);
        assertThat(evaluationResponse.isPass()).isTrue(); // 应通过

        // 测试错误答案
        String wrongAnswer = "你不能请假。赶紧回去工作！";
        evaluationRequest = new EvaluationRequest(question, documents, wrongAnswer);
        evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);
        assertThat(evaluationResponse.isPass()).isFalse(); // 应失败
        ```

        > 📌 注意：若将上述 `wrongAnswer` 传给 `RelevancyEvaluator`，评估可能通过——因为虽然内容错误，但主题仍与“病假”相关。

5. 结论

    在本文中，我们探索了如何使用 Spring AI 的 `Evaluator` 接口测试 LLM 响应：

    1. 构建了一个基于文档的简单 RAG 聊天机器人。
    2. 使用 Testcontainers 在本地搭建 Ollama 测试环境。
    3. 利用 `RelevancyEvaluator` 和 `FactCheckingEvaluator` 分别评估响应的**相关性**与**事实准确性**。

    这套方法为 LLM 应用的质量保障提供了自动化、可扩展的测试方案，有助于在生产环境中减少幻觉和错误输出。
