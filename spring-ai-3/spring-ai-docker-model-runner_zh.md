# [使用 Docker Model Runner 与 Spring AI](https://www.baeldung.com/spring-ai-docker-model-runner)

Docker · Spring AI · 大语言模型（LLM）· OpenAI  

1. 引言

    **[Docker Model Runner](https://docs.docker.com/ai/model-runner/)**（在撰写本文时）随 Docker Desktop 4.40 版本首次面向搭载 Apple Silicon 芯片的 Mac 用户推出，它通过简化大语言模型（LLM）的部署与管理，彻底革新了本地 AI 开发体验。它解决了诸如复杂环境配置、高昂云端推理成本以及数据隐私等常见挑战。

    通过提供 **OpenAI 兼容的推理 API**，Model Runner 可与 Spring AI 等框架无缝集成，让开发者轻松在本地构建 AI 驱动的应用程序。在本教程中，我们将学习如何设置 Docker Model Runner，并创建一个连接它的 Spring AI 应用。完成后，你将拥有一个功能完整的本地 AI 应用，运行强大的大语言模型。

2. Docker Model Runner

    Docker Model Runner 是一个专为简化在 Docker 容器内部署和运行大语言模型而设计的工具。它是一个 AI 推理引擎，支持来自多个提供商的丰富模型。

    以下是 Docker Model Runner 的核心特性：

    - **简化模型部署**：模型以标准的 **OCI（Open Container Initiative）制品** 形式发布在 Docker Hub 的 [ai命名空间](https://hub.docker.com/u/ai)下，开发者可直接在 Docker Desktop 中拉取、运行和管理 AI 模型。
    - **广泛模型支持**：支持来自多个提供商（如 Mistral、LLaMA、Phi-4 等）的多种大语言模型，确保模型选择的灵活性。
    - **本地推理**：模型在本地运行，增强数据隐私性，同时消除对云端推理的依赖。
    - **OpenAI 兼容 API**：提供标准化 API，可轻松集成现有 AI 框架，显著降低开发开销。

3. 环境设置

    本节将介绍使用 Docker Model Runner 的前置条件，以及创建 Spring AI 应用所需的 Maven 依赖。

    1. 前置条件

        要使用 Docker Model Runner，你需要：

        - **Docker Desktop 4.40 或更高版本**：安装在 Apple Silicon 芯片的 Mac 上。
        - **Java 21 或更高版本**：用于 Spring AI 开发。
        - **兼容 Model Runner 的 LLM**：如 LLaMA 或 Gemma 3。

    2. Maven 依赖

        在 `pom.xml` 中添加以下依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-spring-boot-testcontainers</artifactId>
            <version>1.0.0-M6</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.8</version>
            <scope>test</scope>
        </dependency>
        ```

4. 启用并配置 Docker Model Runner

    本节介绍启用 Docker Model Runner 并拉取特定模型的两种方法。

    1. 通过指定 TCP 端口启用 Model Runner

        首先，启用 Model Runner 并将其暴露在指定 TCP 端口（如 12434）：

        ```bash
        docker desktop enable model-runner --tcp 12434
        ```

        这将配置 Model Runner 在 `http://localhost:12434/engines` 上监听请求。

        在 Spring AI 应用中，需配置 `api-key`、`model` 和 `base-url` 指向 Model Runner 端点：

        ```yaml
        spring.ai.openai.api-key=${OPENAI_API_KEY}
        spring.ai.openai.base-url=http://localhost:12434/engines
        spring.ai.openai.chat.options.model=ai/gemma3
        ```

    2. 使用 Testcontainers 启用 Model Runner

        也可以不指定端口，直接启用 Model Runner：

        ```bash
        docker desktop enable model-runner
        ```

        这将在 Docker 默认内部网络中运行 Model Runner。接着，使用 [Testcontainers](https://www.baeldung.com/spring-boot-testcontainers-integration-test) 动态配置 `base-url`、`api-key` 和 `model`：

        ```java
        @TestConfiguration(proxyBeanMethods = false)
        class TestcontainersConfiguration {
            @Bean
            DockerModelRunnerContainer socat() {
                return new DockerModelRunnerContainer("alpine/socat:1.8.0.1");
            }

            @Bean
            DynamicPropertyRegistrar properties(DockerModelRunnerContainer dmr) {
                return (registrar) -> {
                registrar.add("spring.ai.openai.base-url", dmr::getOpenAIEndpoint);
                registrar.add("spring.ai.openai.api-key", () -> "test-api-key");
                registrar.add("spring.ai.openai.chat.options.model", () -> "ai/gemma3");
                };
            }
        }
        ```

        上述 `TestcontainersConfiguration` 类是一个 Spring Boot `@TestConfiguration`，专为与 Testcontainers 集成测试设计。它定义了两个 Bean：

        - `DockerModelRunnerContainer`：启动一个基于 `alpine/socat:1.8.0.1` 镜像的容器，用于代理或模拟 AI 服务端点。
        - `DynamicPropertyRegistrar`：动态注册 Spring AI 属性，包括从容器获取的 API 基础 URL、测试用 API 密钥（`test-api-key`）和模型标识符（`ai/gemma3`）。

        `@TestConfiguration(proxyBeanMethods = false)` 注解确保轻量级 Bean 创建，避免代理开销。此配置使测试能在无外部依赖的情况下模拟 AI 服务环境，`socat` 容器负责将流量转发到内部服务 `model-runner.docker.internal`。

    3. 拉取并验证 Gemma 3 模型

        启用 Model Runner 后（任选上述一种方式），拉取 Gemma 3 模型：

        ```bash
        docker model pull ai/gemma3
        ```

        然后，确认模型已本地可用：

        ```bash
        docker model list
        ```

        该命令将列出所有本地可用模型，包括 `ai/gemma3`。

5. 与 Spring AI 集成

    现在，创建一个简单控制器与模型交互：

    ```java
    @RestController
    class ModelRunnerController {
        private final ChatClient chatClient;

        public ModelRunnerController(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @GetMapping("/chat")
        public String chat(@RequestParam("message") String message) {
            return this.chatClient.prompt()
            .user(message)
            .call()
            .content();
        }
    }
    ```

    1. 使用指定 TCP 端口测试 Model Runner

        配置 OpenAI 客户端指向正确端点并使用已拉取模型后，启动应用并测试 `/chat` 端点：

        ```bash
        curl "http://localhost:8080/chat?message=What%20is%20the%20future%20of%20AI%20development?"
        ```

        响应将由运行在 Model Runner 中的 Gemma 3 模型生成。

    2. 使用 Testcontainers 测试 Model Runner

        创建 `ModelRunnerApplicationTest` 类，导入 `TestcontainersConfiguration` 并调用示例控制器：

        ```java
        @Import(TestcontainersConfiguration.class)
        class ModelRunnerApplicationTest {
            // ...

            @Test
            void givenMessage_whenCallChatController_thenSuccess() {
                // given
                String userMessage = "Hello, how are you?";

                // when
                ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/chat?message=" + userMessage, String.class);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody()).isNotEmpty();
            }
        }
        ```

        `@Import(TestcontainersConfiguration.class)` 导入了之前定义的配置类，启动 `alpine/socat` 容器并动态注册 Spring AI 属性（如 `base-url`、`api-key`、`model`），确保测试环境使用 Testcontainers 管理的模拟 AI 服务端点。

6. 结论

    Docker Model Runner 为在 Docker 生态中构建生成式 AI 应用的开发者，提供了一个**开发者友好、注重隐私、成本高效**的本地大语言模型运行方案。在本文中，我们探索了 Docker Model Runner 的能力，并演示了其与 Spring AI 的集成方式。
