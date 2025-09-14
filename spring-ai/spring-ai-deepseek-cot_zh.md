# [使用 DeepSeek 模型与 Spring AI 构建 AI 聊天机器人](https://www.baeldung.com/spring-ai-deepseek-cot)

人工智能 · Spring AI · 大语言模型 · OpenAI  

1. 概述

    现代 Web 应用程序正越来越多地集成大语言模型（LLM）以构建智能解决方案。

    **DeepSeek** 是一家中国人工智能研究公司，开发了强大的 LLM，并凭借其 **[DeepSeek-V3](https://api-docs.deepseek.com/news/news1226)** 和 **[DeepSeek-R1](https://api-docs.deepseek.com/news/news250120)** 模型近期在 AI 领域引起轰动。其中，DeepSeek-R1 模型在输出答案的同时，会暴露其**思维链（Chain of Thought, CoT）**，让我们得以一窥 AI 模型如何解读和处理所给提示。

    在本教程中，我们将探索如何将 DeepSeek 模型与 Spring AI 集成，并构建一个能够进行多轮文本对话的简单聊天机器人。

2. 依赖与配置

    有多种方式可将 DeepSeek 模型集成到我们的应用中，本节将讨论几种流行选项，我们可以根据自身需求选择最合适的一种。

    1. 使用 OpenAI API

        DeepSeek 模型完全兼容 [OpenAI API](https://platform.openai.com/docs/api-reference/introduction)，可通过任何 OpenAI 客户端或库访问。

        首先，在项目的 `pom.xml` 文件中添加 Spring AI 的 OpenAI Starter 依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        ```

        由于当前版本 `1.0.0-M6` 是里程碑版本，我们还需在 `pom.xml` 中添加 Spring Milestones 仓库：

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

        该仓库用于发布里程碑版本，而非标准的 Maven Central 仓库。无论选择哪种配置方式，我们都必须添加此里程碑仓库。

        接着，在 `application.yaml` 文件中配置 DeepSeek API [密钥](https://platform.deepseek.com/api_keys)和聊天[模型](https://api-docs.deepseek.com/quick_start/pricing)：

        ```yaml
        spring:
        ai:
            openai:
            api-key: ${DEEPSEEK_API_KEY}
            chat:
                options:
                model: deepseek-reasoner
            base-url: https://api.deepseek.com
            embedding:
                enabled: false
        ```

        此外，我们指定了 DeepSeek API 的基础 URL，并禁用了嵌入功能（因为 DeepSeek 目前未提供兼容的嵌入模型）。

        配置上述属性后，Spring AI 会自动创建一个 `ChatModel` 类型的 Bean，供我们与指定模型交互。稍后我们将用它定义聊天机器人所需的其他 Bean。

    2. 使用 Amazon Bedrock Converse API

        另一种方式是通过 **[Amazon Bedrock Converse API](https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference-call.html)** 将 DeepSeek R1 模型集成到应用中。

        要跟随此配置步骤，我们需要一个有效的 AWS [账户](https://aws.amazon.com/resources/create-account/)。DeepSeek-R1 模型可通过 [Amazon Bedrock Marketplace](https://aws.amazon.com/bedrock/marketplace/) 获取，并使用 [Amazon SageMaker](https://aws.amazon.com/sagemaker/) 托管。可参考此[部署指南](https://docs.aws.amazon.com/sagemaker/latest/dg/deploy-model.html)进行设置。

        首先，在 `pom.xml` 中添加 Bedrock Converse Starter 依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bedrock-converse-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        ```

        接着，为与 Amazon Bedrock 交互，我们需要在 `application.yaml` 中配置 AWS 凭证（用于身份验证）以及托管 DeepSeek 模型的区域：

        ```yaml
        spring:
        ai:
            bedrock:
            aws:
                region: ${AWS_REGION}
                access-key: ${AWS_ACCESS_KEY}
                secret-key: ${AWS_SECRET_KEY}
            converse:
                chat:
                options:
                    model: arn:aws:sagemaker:REGION:ACCOUNT_ID:endpoint/ENDPOINT_NAME
        ```

        我们使用 `${}` 属性占位符从环境变量加载配置值。

        同时，我们需指定托管 DeepSeek 模型的 SageMaker 端点 URL ARN。请务必将 `REGION`、`ACCOUNT_ID` 和 `ENDPOINT_NAME` 占位符替换为实际值。

        最后，为与模型交互，我们需要为应用中配置的 IAM 用户分配以下 IAM 策略：

        ```json
        {
        "Version": "2012-10-17",
        "Statement": [
            {
            "Effect": "Allow",
            "Action": "bedrock:InvokeModel",
            "Resource": "arn:aws:bedrock:REGION:ACCOUNT_ID:marketplace/model-endpoint/all-access"
            }
        ]
        }
        ```

        同样，请记得在资源 ARN 中将 `REGION` 和 `ACCOUNT_ID` 占位符替换为实际值。

    3. 使用 Ollama 本地部署

        为方便本地开发和测试，我们可以通过 **Ollama**（一个开源工具，允许在本地机器运行 LLM）来运行 DeepSeek 模型。

        在项目的 `pom.xml` 文件中导入必要依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        ```

        Ollama Starter 依赖帮助我们与 Ollama 服务建立连接。

        接着，在 `application.yaml` 中配置聊天模型：

        ```yaml
        spring:
        ai:
            ollama:
            chat:
                options:
                model: deepseek-r1
            init:
                pull-model-strategy: when_missing
            embedding:
                enabled: false
        ```

        这里我们指定了 [deepseek-r1](https://ollama.com/library/deepseek-r1) 模型，当然也可以尝试其他[可用模型](https://ollama.com/search?q=deepseek)。

        此外，我们将 `pull-model-strategy` 设置为 `when_missing`，确保 Spring AI 在模型本地不存在时自动拉取。

        Spring AI 默认会连接运行在本地主机 `11434` 端口的 Ollama 服务。但我们可以通过 `spring.ai.ollama.base-url` 属性覆盖连接 URL。或者，也可以使用 Testcontainers 来设置 Ollama 服务。

        同样，Spring AI 会自动为我们创建 `ChatModel` Bean。如果由于某些原因，我们的类路径中同时存在 OpenAI API、Bedrock Converse 和 Ollama 三种依赖，我们可以通过限定符 `openAiChatModel`、`bedrockProxyChatModel` 或 `ollamaChatModel` 来引用特定的 Bean。

3. 构建聊天机器人

    现在我们已讨论了各种配置选项，接下来使用配置好的 DeepSeek 模型构建一个简单的聊天机器人。

    1. 定义聊天机器人 Bean

        首先，定义聊天机器人所需的 Bean：

        ```java
        @Bean
        ChatMemory chatMemory() {
            return new InMemoryChatMemory();
        }

        @Bean
        ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
            return ChatClient
            .builder(chatModel)
            .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
            .build();
        }
        ```

        首先，我们使用 `InMemoryChatMemory` 实现定义了一个 `ChatMemory` Bean，用于在内存中存储聊天历史，以维持对话上下文。

        接着，我们使用 `ChatModel` 和 `ChatMemory` Bean 创建了一个 `ChatClient` Bean。`ChatClient` 类是我们与已配置的 DeepSeek 模型交互的主要入口点。

    2. 创建自定义 StructuredOutputConverter

        如前所述，DeepSeek-R1 模型的响应包含其思维链（CoT），响应格式如下：

        ```log
        <think>
        Chain of Thought
        </think>
        Answer
        ```

        遗憾的是，由于这种独特格式，当前版本 Spring AI 中所有的结构化输出转换器在尝试将响应解析为 Java 类时都会失败并抛出异常。

        因此，我们创建自己的 `StructuredOutputConverter` 实现，分别解析 AI 模型的答案和思维链：

        ```java
        record DeepSeekModelResponse(String chainOfThought, String answer) {
        }

        class DeepSeekModelOutputConverter implements StructuredOutputConverter<DeepSeekModelResponse> {
            private static final String OPENING_THINK_TAG = "<think>";
            private static final String CLOSING_THINK_TAG = "</think>";

            @Override
            public DeepSeekModelResponse convert(@NonNull String text) {
                if (!StringUtils.hasText(text)) {
                    throw new IllegalArgumentException("文本不能为空");
                }
                int openingThinkTagIndex = text.indexOf(OPENING_THINK_TAG);
                int closingThinkTagIndex = text.indexOf(CLOSING_THINK_TAG);

                if (openingThinkTagIndex != -1 && closingThinkTagIndex != -1 && closingThinkTagIndex > openingThinkTagIndex) {
                    String chainOfThought = text.substring(openingThinkTagIndex + OPENING_THINK_TAG.length(), closingThinkTagIndex);
                    String answer = text.substring(closingThinkTagIndex + CLOSING_THINK_TAG.length());
                    return new DeepSeekModelResponse(chainOfThought, answer);
                } else {
                    logger.debug("响应中未找到 <think> 标签，将整个文本视为答案。");
                    return new DeepSeekModelResponse(null, text);
                }
            }
        }
        ```

        这里，我们的转换器从 AI 模型的响应中提取 `chainOfThought` 和 `answer`，并以 `DeepSeekModelResponse` 记录的形式返回。

        如果 AI 响应不包含 `<think>` 标签，我们将整个响应视为答案。这确保了与其他不包含 CoT 的 DeepSeek 模型的兼容性。

    3. 实现服务层

        配置完成后，我们创建 `ChatbotService` 类。我们将注入之前定义的 `ChatClient` Bean，以与指定的 DeepSeek 模型交互。

        首先，定义两个简单的记录类，用于表示聊天请求和响应：

        ```java
        record ChatRequest(@Nullable UUID chatId, String question) {}

        record ChatResponse(UUID chatId, String chainOfThought, String answer) {}
        ```

        `ChatRequest` 包含用户的问题和一个可选的 `chatId`，用于标识正在进行的对话。

        同样，`ChatResponse` 包含 `chatId`，以及聊天机器人的 `chainOfThought` 和 `answer`。

        现在，实现核心功能：

        ```java
        ChatResponse chat(ChatRequest chatRequest) {
            UUID chatId = Optional
            .ofNullable(chatRequest.chatId())
            .orElse(UUID.randomUUID());
            DeepSeekModelResponse response = chatClient
            .prompt()
            .user(chatRequest.question())
            .advisors(advisorSpec ->
                advisorSpec
                    .param("chat_memory_conversation_id", chatId))
            .call()
            .entity(new DeepSeekModelOutputConverter());
            return new ChatResponse(chatId, response.chainOfThought(), response.answer());
        }
        ```

        如果传入的请求不包含 `chatId`，我们会生成一个新的。这允许用户开启新对话或继续现有对话。

        我们将用户的问题传递给 `chatClient` Bean，并将 `chat_memory_conversation_id` 参数设置为解析出的 `chatId`，以维持对话历史。

        最后，我们创建 `DeepSeekModelOutputConverter` 类的实例，并将其传递给 `entity()` 方法，将 AI 模型的响应解析为 `DeepSeekModelResponse` 记录。然后，从中提取 `chainOfThought` 和 `answer`，连同 `chatId` 一起返回。

    4. 与聊天机器人交互

        服务层实现完成后，我们在其上暴露一个 REST API：

        ```java
        @PostMapping("/chat")
        ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
            ChatResponse chatResponse = chatbotService.chat(chatRequest);
            return ResponseEntity.ok(chatResponse);
        }
        ```

        让我们使用 **HTTPie CLI** 调用上述 API 端点，开启一段新对话：

        ```bash
        http POST :8080/chat question="What was the name of Superman's adoptive mother?"
        ```

        这里，我们向聊天机器人发送一个简单问题，看看会收到什么响应：

        ```json
        {
            "chatId": "1e3c151f-cded-4f10-a5fc-c52c5952411c",
            "chain0fThought": "Alright, so I need to figure out the name of Superman's adoptive mother. I'm not entirely sure, but I remember that Superman's story involves him being found and adopted by humans. Let me try to recall the details. I think Superman's real parents are from Krypton, and when he was a baby, they sent him to Earth in a small spaceship before their planet was destroyed. On Earth, he was found by a couple who couldn't have children. I believe the couple's last name is Kent, so his adoptive father is Jonathan Kent, and his mother would be Martha Kent. That sounds familiar from the Superman lore I've come across. Wait, but I'm not 100% certain. Maybe I should think about different versions or media where Superman appears. In the comics, movies, or TV shows, is the name consistent? For example, in the classic Superman movies, like the ones with Christopher Reeve, I think they refer to his adoptive parents as Jonathan and Martha Kent. In the more recent movies, like Man of Steel, they also use those names. So it seems consistent across different media. Is there any variation where the names are different? I can't recall any. Maybe in some alternate universe stories, but in the main DC Universe, it's Martha Kent. So I think the answer is Martha Kent. ",
            "answer": "The name of Superman's adoptive mother is Martha Kent. She and her husband, Jonathan Kent, found Superman when he was a baby and raised him on their farm in Smallville, Kansas. This is consistent across various Superman stories in comics, movies, and TV shows."
        }
        ```

        响应包含一个唯一的 `chatId`，以及聊天机器人的 `chainOfThought` 和答案。通过 `chainOfThought` 属性，我们可以清晰看到 AI 模型如何推理并处理给定提示。

        让我们使用上述响应中的 `chatId` 发送一个后续问题，继续这段对话：

        ```bash
        http POST :8080/chat question="Which bald billionaire hates him?" chatId="1e3c151f-cded-4f10-a5fc-c52c5952411c"
        ```

        看看聊天机器人能否维持对话上下文并给出相关回答：

        ```json
        {
            "chatId": "1e3c151f-cded-4f10-a5fc-c52c5952411c" ,
            "chain0fThought": "Alright, the user is asking about Superman's adoptive mother, which I've already answered as Martha Kent. Now, the next question is, 'Which bald billionaire hates him?' Hmm, I need to figure out who that is. Well, in the Superman universe, the main bald billionaire who is an antagonist is Lex Luthor. Lex is a classic villain, often depicted as bald and wealthy. He's known for his intellect and his schemes against Superman. So, connecting the dots, he user is probably referring to Lex Luthor. They might be thinking of a specific storyline or movie where Lex is prominent. It's also possible they're recalling a particular adaptation where Lex's baldness and wealth are emphasized. There aren't many other bald billionaires in Superman's rogues gallery, so Lex is the most likely answer.",
            "answer": "The bald billionaire who hates Superman is Lex Luthor. He is a classic and well-known adversary of Superman, often depicted as a wealthy, bald businessman with a strong animosity toward the Man of Steel."
        }
        ```

        如我们所见，聊天机器人确实维持了对话上下文。`chatId` 保持不变，表明后续答案是同一对话的延续。

4. 结论

    在本文中，我们探索了如何在 Spring AI 中使用 DeepSeek 模型。

    我们讨论了多种集成 DeepSeek 模型的选项，包括直接使用与其兼容的 OpenAI API、通过 Amazon Bedrock Converse API 集成，以及使用 Ollama 搭建本地测试环境。

    随后，我们构建了一个支持多轮文本对话的简单聊天机器人，并使用自定义的 `StructuredOutputConverter` 实现，从 AI 模型的响应中提取思维链和答案。
