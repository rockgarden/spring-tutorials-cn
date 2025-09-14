# [使用 Groq 聊天服务与 Spring AI](https://www.baeldung.com/spring-ai-groq-chat)

人工智能 · Spring AI · 大语言模型（LLM）  

1. 概述

    在本教程中，我们将学习如何使用 **Spring AI** 集成基于 LPU 的 **[Groq AI 推理引擎](https://groq.com/)** 所提供的模型，构建一个聊天机器人。

    Groq 通过 [REST API](https://console.groq.com/docs/api-reference#chat-create) 向应用程序提供服务。此外，Groq 还为多种编程语言（如 Python 和 JavaScript）提供了 SDK。在 Python 生态中，流行框架如 LangChain 和 LiteLLM 也支持 Groq；JavaScript 生态中，[Vercel AI SDK](https://ai-sdk.dev/providers/ai-sdk-providers/groq#groq-provider) 也提供了 Groq 集成模块。

    由于 Groq 完全兼容 OpenAI 客户端库，应用程序可以轻松从 OpenAI 切换到 Groq AI 服务。因此，**Spring AI 的 OpenAI 聊天客户端只需少量配置更改即可连接 Groq**。值得注意的是，Spring AI 目前并未为 Groq 提供独立的专用库。

2. 前置条件

    Spring AI 框架通过对应的 Starter 库支持集成多种 LLM 服务。同样，要集成 Groq 服务，Spring Boot 应用必须导入 Spring AI OpenAI Starter 库：

    ```xml
    <dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0-M6</version>
    </dependency>
    ```

    通常，我们推荐使用 **[Spring Initializr](https://start.spring.io/)** 工具来最优地导入所需库。

    最后，我们必须在 **[Groq Cloud](https://console.groq.com/home)** 上注册并创建一个 API 密钥，用于 Spring AI 配置：

    > **Groq API 密钥**  
    > 用户注册 Groq Cloud 后，将获得免费订阅额度以开始使用 Groq API。但 Groq 会对 API 请求施加**模型特定的[速率限制](https://console.groq.com/docs/rate-limits)**，以确保公平使用和系统可用性。

3. 关键 Spring AI 组件与配置

    要有效利用 Spring AI 的 OpenAI 库访问 Groq 服务 API，需了解其核心类，如 [OpenAiChatModel](https://docs.spring.io/spring-ai/docs/current-SNAPSHOT/api/org/springframework/ai/openai/OpenAiChatModel.html) 和 [OpenAiChatOptions](https://docs.spring.io/spring-ai/docs/current-SNAPSHOT/api/org/springframework/ai/openai/OpenAiChatOptions.html)：

    - `OpenAiChatModel` 是客户端类，接收 `Prompt` 对象并调用底层服务。
    - 本文中，我们将演示如何连接 Groq 服务。
    - `OpenAiChatOptions` 类用于指定 Groq 上可用的模型名称、temperature、maxTokens 等关键属性。当需要覆盖聊天客户端默认属性时，可将其作为参数传递给 `OpenAiChatModel#call()` 方法。
    - 此外，Spring AI 还提供通用类如 `Prompt` 和 `ChatResponse`，分别用于构建聊天提示和接收响应。

    要自动配置 `OpenAiChatModel`，我们可在 `application.yml` 或 `application.properties` 中，在 `spring.ai.openai.chat` 命名空间下指定[配置](https://docs.spring.io/spring-ai/reference/api/chat/groq-chat.html#_configuration_properties)。

    **最少必须覆盖的配置是 `spring.ai.openai.chat.base-url`，将其指向 Groq API 端点：`https://api.groq.com/openai`。**

    同时，必须设置 Groq API 密钥，对应属性为 `spring.ai.openai.chat.api-key`。**最佳实践**：建议从环境变量或安全凭据库中读取密钥，再注入到配置中。

    我们也可以在 `spring.ai.openai.base-url` 和 `spring.ai.openai.api-key` 中设置 URL 和密钥，但 **`spring.ai.openai.chat` 命名空间下的配置优先级更高**。

4. 自动配置 Groq 客户端

    本节将展示 `OpenAiAutoConfiguration` 类如何根据配置文件创建 `OpenAiChatModel` Bean。

    首先，在 `application-groq.properties` 文件中添加关键 OpenAI 配置：

    ```properties
    spring.application.name=spring-ai-groq-demo
    spring.ai.openai.base-url=https://api.groq.com/openai
    spring.ai.openai.api-key=gsk_XXXX

    spring.ai.openai.chat.base-url=https://api.groq.com/openai
    spring.ai.openai.chat.api-key=gsk_XXXX
    spring.ai.openai.chat.options.temperature=0.7
    spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
    ```

    如前所述，我们在属性文件中配置了 Groq API 端点、API 密钥和一个大语言模型。值得注意的是，我们也在 `spring.ai.openai` 命名空间下配置了 API 端点和密钥——因为部分 Spring AI Bean 依赖这些基础配置，缺失会导致应用启动失败。

    接着，定义一个自定义的 `GroqChatService` 类，负责调用 Groq 服务：

    ```java
    @Service
    public class GroqChatService {
        @Autowired
        private OpenAiChatModel groqClient;

        public String chat(String prompt) {
            return groqClient.call(prompt);
        }

        public ChatOptions getChatOptions() {
            return groqClient.getDefaultOptions();
        }
    }
    ```

    `OpenAiAutoConfiguration` Bean 会根据配置文件和预设默认值实例化 `OpenAiChatModel`。在服务类中，我们自动注入该 Bean。`GroqChatService#chat()` 方法使用其 `call()` 方法调用 Groq 服务。`GroqChatService#getChatOptions()` 方法则返回包含聊天客户端配置的 [ChatOptions](https://docs.spring.io/spring-ai/docs/current-SNAPSHOT/api/org/springframework/ai/chat/prompt/ChatOptions.html) 对象。

    最后，通过 JUnit 测试验证 Groq 聊天客户端：

    ```java
    void whenCallOpenAIClient_thenReturnResponseFromGroq() {
        String prompt = """
        Context:
        Support Ticket #98765:
        Product: XYZ Wireless Mouse
        Issue Description: The mouse connects intermittently to my laptop.
        I've tried changing batteries and reinstalling drivers,
        but the cursor still freezes randomly for a few seconds before resuming normal movement.
        It affects productivity significantly.
        Question:
        Based on the support ticket, what is the primary technical issue
        the user is experiencing with their 'XYZ Wireless Mouse'?;
        """;
        String response = groqChatService.chat(prompt);
        logger.info("Response from Groq:{}", response);

        assertThat(response.toLowerCase()).isNotNull()
        .isNotEmpty()
        .containsAnyOf("laptop", "mouse", "connect");

        ChatOptions openAiChatOptions = groqChatService.getChatOptions();
        String model = openAiChatOptions.getModel();
        Double temperature = openAiChatOptions.getTemperature();

        assertThat(openAiChatOptions).isInstanceOf(OpenAiChatOptions.class);
        assertThat(model).isEqualTo("llama-3.3-70b-versatile");
        assertThat(temperature).isEqualTo(Double.valueOf(0.7));
    }
    ```

    测试类中自动注入了 `groqChatService` Bean。测试方法调用 `groqChatService#chat()`，传入包含问题和上下文的提示（此处上下文模拟从向量数据库检索的客服工单信息）。Groq 服务返回的答案如下：

    > The primary technical issue the user is experiencing with their  
    > 'XYZ Wireless Mouse' is intermittent connectivity, resulting in the  
    > cursor freezing randomly for a few seconds before resuming normal movement.

    最后，测试验证了聊天选项（如模型和温度）与配置文件中的值一致。

5. 自定义 Groq 客户端

    目前为止，我们使用配置文件中的 Spring AI 配置自动配置了聊天客户端。但在实际应用中，我们通常需要动态自定义属性（如模型、温度等）。

    首先，在 Spring 配置类中定义自定义 `OpenAiChatModel` Bean：

    ```java
    @Configuration(proxyBeanMethods = false)
    public class ChatAppConfiguration {
        @Value("${groq.api-key}")
        private String GROQ_API_KEY;

        @Value("${groq.base-url}")
        private String GROQ_API_URL;

        @Bean
        public OpenAiChatModel customGroqChatClient() {
            OpenAiApi groqOpenAiApi = new OpenAiApi.Builder()
            .apiKey(GROQ_API_KEY)
            .baseUrl(GROQ_API_URL)
            .build();
            return OpenAiChatModel.builder()
            .openAiApi(groqOpenAiApi)
            .build();
        }
    }
    ```

    `ChatAppConfiguration#customGroqChatClient()` 方法使用底层 [OpenAiApi](https://docs.spring.io/spring-ai/docs/current-SNAPSHOT/api/org/springframework/ai/openai/api/OpenAiApi.html) 类构建 `OpenAiChatModel` Bean。我们从属性文件读取 API 密钥和 URL，也可修改该类以支持从下游系统动态加载配置。Spring Boot 启动后，该聊天客户端将以 `customGroqChatClient` 名称作为 Bean 注入容器。

    接着，定义一个 Spring Boot 服务类，自动注入我们创建的自定义 `OpenAiChatModel` Bean：

    ```java
    @Service
    public class CustomGroqChatService {
        @Autowired
        private OpenAiChatModel customGroqChatClient;

        public String chat(String prompt, String model, Double temperature) {
            ChatOptions chatOptions = OpenAiChatOptions.builder()
            .model(model)
            .temperature(temperature)
            .build();
            return customGroqChatClient.call(new Prompt(prompt, chatOptions))
            .getResult()
            .getOutput()
            .getText();
        }
    }
    ```

    在 `chat()` 方法中，我们将模型、温度等配置设置到 `ChatOptions` 对象中，然后将其与提示一起传递给 `customGroqChatClient#call()` 方法，最后从 [ChatResponse](https://docs.spring.io/spring-ai/docs/current-SNAPSHOT/api/org/springframework/ai/chat/model/ChatResponse.html) 对象中提取响应文本。

    现在，通过 JUnit 测试验证自定义 Groq 客户端：

    ```java
    void whenCustomGroqClientCalled_thenReturnResponse() {
        String prompt = """
        Context:
        The Eiffel Tower is one of the most famous landmarks
        in Paris, attracting millions of visitors each year.
        Question:
        In which city is the Eiffel Tower located?
        """;
        String response = customGroqChatService.chat(prompt, "llama-3.1-8b-instant", 0.8);

        assertThat(response)
        .isNotNull()
        .isNotEmpty()
        .contains("Paris");
        logger.info("Response from custom Groq client: {}", response);
    }
    ```

    测试调用自动注入的 `customGroqChatService` Bean 的 `chat()` 方法，传入提示（包含上下文和问题）、模型及温度。`CustomGroqChatService#chat()` 方法返回答案，我们验证其准确回答了“埃菲尔铁塔位于哪个城市？”的问题。

    Groq 返回的响应如下：

    > The Eiffel Tower is located in Paris, France.

6. 结论

    本文介绍了如何将 **Groq 推理引擎** 与 **Spring AI 的 OpenAI 库** 集成。此外，该库还支持使用 Groq 的工具功能注册并调用外部工具执行操作。

    但需注意，**Groq 目前不支持多模态模型**，因此 Spring AI 也无法提供相应功能。本质上，Groq 并未完全兼容 OpenAI 协议，因此在使用其 API 时需了解这些限制。

    尽管如此，借助 Spring AI 的抽象层，我们仍能以极低的迁移成本快速接入 Groq 的高性能 LPU 推理服务，为应用注入强大的 AI 能力。
