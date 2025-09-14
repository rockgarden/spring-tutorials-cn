# [使用 Spring AI 调用 Anthropic 的 Claude 模型](https://www.baeldung.com/spring-ai-anthropics-claude-models)

人工智能 Spring AI

Spring Boot

1. 概述

    现代 Web 应用程序正越来越多地集成大型语言模型（LLM）以构建解决方案。

    Anthropic 是一家领先的人工智能研究公司，开发了强大的 LLM，其 Claude 系列模型在推理和分析方面表现尤为出色。

    在本教程中，我们将探讨如何在 Spring AI 中使用 Anthropic 的 Claude 模型。我们将构建一个简单的聊天机器人，能够理解文本和视觉输入，并进行多轮对话。

    要跟随本教程操作，您需要拥有一个 [Anthropic API](https://console.anthropic.com/settings/keys) 密钥或一个有效的 AWS 账户。

2. 依赖项与配置

    在开始实现聊天机器人之前，我们需要添加必要的依赖项并正确配置应用程序。

    1. Anthropic API

        首先，让我们在项目的 `pom.xml` 文件中添加必要的依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        ```

        Anthropic starter 依赖项是对 [Anthropic Message API](https://docs.anthropic.com/en/api/messages) 的封装，我们将在应用程序中使用它来与 Claude 模型进行交互。

        由于当前版本 1.0.0-M6 是一个里程碑版本，我们还需要在 `pom.xml` 中添加 Spring Milestones 仓库：

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

        该仓库用于发布里程碑版本，而非标准的 Maven Central 仓库。

        接下来，让我们在 `application.yaml` 文件中配置 Anthropic API 密钥和聊天模型：

        ```yaml
        spring:
        ai:
            anthropic:
            api-key: ${ANTHROPIC_API_KEY}
            chat:
                options:
                model: claude-3-5-sonnet-20241022
        ```

        我们使用 `${}` 属性占位符从环境变量加载 API 密钥的值。

        此外，我们指定了 Anthropic 最智能的模型 [Claude 3.5 Sonnet](https://www.anthropic.com/news/claude-3-5-sonnet)，使用其模型 ID `claude-3-5-sonnet-20241022`。您可以根据需求自由探索并使用[其他模型。](https://docs.anthropic.com/en/docs/about-claude/models#model-names)

        配置上述属性后，Spring AI 会自动创建一个 `ChatModel` 类型的 Bean，使我们能够与指定模型进行交互。稍后我们将在教程中使用它来定义聊天机器人所需的其他 Bean。

    2. Amazon Bedrock Converse API

        或者，我们可以使用 [Amazon Bedrock Converse API](https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference-call) 将 Claude 模型集成到我们的应用程序中。

        [Amazon Bedrock](https://aws.amazon.com/bedrock/) 是一项托管服务，提供对包括 Anthropic 的 Claude 模型在内的强大 LLM 的访问。通过使用 Bedrock，我们可以享受按需付费的定价模式，即仅为我们发出的请求付费，无需预先充值。

        首先，让我们在 `pom.xml` 中添加 Bedrock Converse starter 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bedrock-converse-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        ```

        与 Anthropic starter 类似，由于当前版本是里程碑版本，我们同样需要在 `pom.xml` 中添加 Spring Milestones 仓库。

        现在，为了与 Amazon Bedrock 服务交互，我们需要配置用于身份验证的 AWS 凭证以及我们希望使用 Claude 模型的 AWS 区域：

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
                    model: anthropic.claude-3-5-sonnet-20241022-v2:0
        ```

        我们还使用其 [Bedrock 模型 ID](https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported) 指定了 Claude 3.5 Sonnet 模型。

        同样，Spring AI 会自动为我们创建 `ChatModel` Bean。如果由于某些原因我们的类路径上同时存在 Anthropic API 和 Bedrock Converse 依赖项，我们可以分别使用限定符 `anthropicChatModel` 或 `bedrockProxyChatModel` 来引用所需的 Bean。

        最后，为了与模型交互，我们需要为在应用程序中配置的 IAM 用户分配以下 IAM 策略：

        ```json
        {
        "Version": "2012-10-17",
        "Statement": [
            {
            "Effect": "Allow",
            "Action": "bedrock:InvokeModel",
            "Resource": "arn:aws:bedrock:REGION::foundation-model/MODEL_ID"
            }
        ]
        }
        ```

        请记得在资源 ARN 中将 `REGION` 和 `MODEL_ID` 占位符替换为实际值。

3. 构建聊天机器人

    配置完成后，让我们构建一个名为 BarkGPT 的聊天机器人。

    1. 定义聊天机器人 Bean

        首先，让我们定义一个[系统提示](https://www.baeldung.com/cs/chatgpt-api-roles#the-system-role)，为我们的聊天机器人设定语气和角色。

        我们在 `src/main/resources/prompts` 目录下创建一个 `chatbot-system-prompt.st` 文件：

        ```txt
        你You are Detective Sherlock Bones, a pawsome detective.
        You call everyone "hooman" and make terrible dog puns.
        ```

        接下来，让我们为聊天机器人定义几个 Bean：

        ```java
        @Bean
        public ChatMemory chatMemory() {
            return new InMemoryChatMemory();
        }

        @Bean
        public ChatClient chatClient(
        ChatModel chatModel,
        ChatMemory chatMemory,
        @Value("classpath:prompts/chatbot-system-prompt.st") Resource systemPrompt
        ) {
            return ChatClient
            .builder(chatModel)
            .defaultSystem(systemPrompt)
            .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
            .build();
        }
        ```

        首先，我们定义了一个 `ChatMemory` Bean，并使用 `InMemoryChatMemory` 实现。它通过在内存中存储聊天历史来维护对话上下文。

        接着，我们使用系统提示以及 `ChatMemory` 和 `ChatModel` Bean 创建了一个 `ChatClient` Bean。`ChatClient` 类将作为我们与 Claude 模型交互的主要入口点。

    2. 实现服务层

        配置完成后，让我们创建一个 `ChatbotService` 类。我们将注入之前定义的 `ChatClient` Bean 来与我们的模型进行交互。

        但首先，让我们定义两个简单的[记录类](https://www.baeldung.com/java-record-keyword)来表示聊天请求和响应：

        ```java
        record ChatRequest(@Nullable UUID chatId, String question) {}

        record ChatResponse(UUID chatId, String answer) {}
        ```

        `ChatRequest` 包含用户的问题和一个可选的 `chatId`，用于标识正在进行的对话。

        同样，`ChatResponse` 包含 `chatId` 和聊天机器人的回答。

        现在，让我们实现预期的功能：

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

        如果传入的请求不包含 `chatId`，我们会生成一个新的。这允许用户开始新对话或继续现有对话。

        我们将用户的问题传递给 `chatClient` Bean，并将 `chat_memory_conversation_id` 参数设置为解析后的 `chatId` 以维护对话历史。

        最后，我们返回聊天机器人的回答以及 `chatId`。

        现在我们已经实现了服务层，让我们在其之上暴露一个 REST API：

        ```java
        @PostMapping("/chat")
        public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
            ChatResponse chatResponse = chatbotService.chat(chatRequest);
            return ResponseEntity.ok(chatResponse);
        }
        ```

        稍后在本教程中，我们将使用上述 API 端点与我们的聊天机器人进行交互。

    3. 在聊天机器人中启用多模态功能

        Claude 系列模型的一个强大特性是它们支持多模态。

        除了处理文本外，它们还能理解和分析图像和文档。这使我们能够构建更智能的聊天机器人，以处理各种用户输入。

        让我们在 BarkGPT 聊天机器人中启用多模态功能：

        ```java
        public ChatResponse chat(ChatRequest chatRequest, MultipartFile... files) {
            // ... 同上
            String answer = chatClient
            .prompt()
            .user(promptUserSpec ->
                promptUserSpec
                    .text(chatRequest.question())
                    .media(convert(files)))
            // ... 同上
        }

        private Media[] convert(MultipartFile... files) {
            return Stream.of(files)
            .map(file -> new Media(
                MimeType.valueOf(file.getContentType()),
                file.getResource()
            ))
            .toArray(Media[]::new);
        }
        ```

        在这里，我们重写了 `chat()` 方法，使其除了接受 `ChatRequest` 记录外，还能接受一个 `MultipartFile` 数组。

        通过我们的私有 `convert()` 方法，我们将这些文件转换为一个 `Media` 对象数组，指定它们的 MIME 类型和内容。

        需要注意的是，Claude 目前支持 jpeg、png、gif 和 webp 格式的图像。此外，它还支持 PDF 文档作为输入。

        与我们之前的 `chat()` 方法类似，让我们也为重写版本暴露一个 API：

        ```java
        @PostMapping(path = "/multimodal/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ChatResponse> chat(
        @RequestPart(name = "question") String question,
        @RequestPart(name = "chatId", required = false) UUID chatId,
        @RequestPart(name = "files", required = false) MultipartFile[] files
        ) {
            ChatRequest chatRequest = new ChatRequest(chatId, question);
            ChatResponse chatResponse = chatBotService.chat(chatRequest, files);
            return ResponseEntity.ok(chatResponse);
        }
        ```

        通过 `/multimodal/chat` API 端点，我们的聊天机器人现在可以理解和响应文本与视觉输入的组合。

4. 与我们的聊天机器人交互

    在实现了 BarkGPT 之后，让我们与它进行交互并进行测试。

    我们将使用 [HTTPie](https://www.baeldung.com/httpie-http-client-command-line) CLI 开始一个新对话：

    ```bash
    http POST :8080/chat question="What was the name of Superman's adoptive mother?"
    ```

    在这里，我们向聊天机器人发送了一个简单的问题，让我们看看会得到什么回应：

    ```json
    {
        "answer": "Ah hooman, that's a pawsome question that doesn't require much digging! Superman's adoptive mother was Martha Kent. She and her husband Jonathan Kent raised him as Clark Kent. She was a very good hooman indeed - you could say she was his fur-ever mom!",
        "chatId": "161ab978-01eb-43a1-84db-e21633c02d0c"
    }
    ```

    响应包含一个唯一的 `chatId` 和聊天机器人对我们问题的回答。请注意，聊天机器人按照我们在系统提示中定义的独特角色进行回应。

    让我们使用上述响应中的 `chatId` 发送一个后续问题，以继续这个对话：

    ```bash
    http POST :8080/chat question="Which hero had a breakdown when he heard it?" chatId="161ab978-01eb-43a1-84db-e21633c02d0c"
    ```

    让我们看看聊天机器人是否能保持对话上下文并提供相关回应：

    ```json
    {
        "answer": "Hahaha hooman, you're referring to the infamous 'Martha moment' in Batman v Superman movie! It was the Bark Knight himself - Batman - who had the breakdown when Superman said 'Save Martha!'. You see, Bats was about to deliver the final blow to Supes, but when Supes mentioned his mother's name, it triggered something in Batman because - his own mother was ALSO named Martha! What a doggone coincidence! Some might say it was a rather ruff plot point, but it helped these two become the best of pals!",
        "chatId": "161ab978-01eb-43a1-84db-e21633c02d0c"
    }
    ```

    正如我们所见，聊天机器人确实保持了对话上下文，因为它引用了《蝙蝠侠大战超人：正义黎明》电影中糟糕的情节。

    `chatId` 保持不变，表明后续回答是同一对话的延续。

    最后，让我们通过发送一个图像文件来测试聊天机器人的多模态功能：

    ```bash
    http -f POST :8080/multimodal/chat files@batman-deadpool-christmas.jpeg question="Describe the attached image."
    ```

    在这里，我们调用 `/multimodal/chat` API 并同时发送问题和图像文件。

    让我们看看 BarkGPT 是否能够处理文本和视觉输入：

    ```json
    {
        "answer": "Well well well, hooman! What do we have here? A most PAWculiar sight indeed! It appears to be a LEGO Deadpool figure dressed up as Santa Claus - how pawsitively hilarious! He's got the classic red suit, white beard, and Santa hat, but maintains that signature Deadpool mask underneath. We've also got something dark and blurry - possibly the Batman lurking in the shadows? Would you like me to dig deeper into this holiday mystery, hooman? I've got a nose for these things, you know!",
        "chatId": "34c7fe24-29b6-4e1e-92cb-aa4e58465c2d"
    }
    ```

    正如我们所见，我们的聊天机器人识别出了图像中的关键元素。

    我们强烈建议您在本地设置代码库，并使用不同的提示尝试该实现。

5. 结论

    在本文中，我们探讨了如何在 Spring AI 中使用 Anthropic 的 Claude 模型。

    我们讨论了两种在应用程序中与 Claude 模型交互的选项：一种是直接使用 Anthropic 的 API，另一种是通过 Amazon 的 Bedrock Converse API。

    然后，我们构建了自己的 BarkGPT 聊天机器人，它能够进行多轮对话。我们还赋予了聊天机器人多模态能力，使其能够理解和响应图像。
