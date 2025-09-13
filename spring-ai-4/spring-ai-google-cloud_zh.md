# [Google Cloud 与 Spring AI](https://www.baeldung.com/spring-ai-google-cloud)

人工智能 · 云服务 · Spring AI  

1. 概述

    **Spring AI** 是一个应用程序框架，为各种大语言模型（LLM）提供统一接口，帮助我们将它们集成到 Spring Boot 应用程序中。

    在本教程中，我们将探索如何将 Spring AI 与 **Google Cloud Vertex AI 平台** 集成，并采用多种模型为应用程序提供聊天和文本嵌入能力。

2. 前置条件

    我们需要在 `pom.xml` 中添加 Spring AI Vertex AI Gemini 和嵌入（embedding）相关依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-vertex-ai-embedding</artifactId>
    </dependency>
    ```

    这些 Starter 模型依赖会根据 `application.yml` 中的配置自动设置 Vertex AI 模型。

    第一步，我们必须在 Google Cloud 控制台中启用 **Vertex AI API**，以便向 Vertex AI 发起 API 调用。

    启用后，还需在已安装 Google Cloud CLI 的控制台中运行两条命令。

    第一条命令设置后续所有 CLI 命令的默认项目：

    ```bash
    gcloud config set project <PROJECT-ID>
    ```

    `PROJECT-ID` 参数是启用 Vertex AI 的 Google Cloud 项目的唯一标识符。

    第二条命令用于身份验证，并获取访问 Vertex AI API 所需的 OAuth2 访问令牌：

    ```bash
    gcloud auth application-default login <YOUR-ACCOUNT>
    ```

    执行后会弹出浏览器窗口，提示使用 Google Cloud 账户登录。登录成功后，系统会将 OAuth2 访问令牌保存在本地。

3. 聊天功能

    Gemini 是 Vertex AI 提供的聊天模型。本节中，我们将 Gemini 集成到 Spring Boot 应用程序中。

    1. 配置

        我们需要在 `application.yml` 中添加一些属性，以将聊天模型与 Spring AI 集成：

        ```yaml
        spring:
        ai:
            vertex:
            ai:
                gemini:
                project-id: <YOUR-GOOGLE-CLOUD-PROJECT-ID>
                location: "europe-west1"
                model: "gemini-2.0-flash-lite"
        ```

        - `project-id`：指定应用程序使用的 Google Cloud 项目资源（包括身份验证和计费）。
        - `model`：指定要集成的 Gemini 聊天模型。Gemini 提供多种模型可供选择。

    2. 服务

        创建一个简单的 `ChatService`，接收提示文本作为输入参数：

        ```java
        @Component
        @SessionScope
        public class ChatService {
            private final ChatClient chatClient;

            public ChatService(ChatModel chatModel, ChatMemory chatMemory) {
                this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
            }

            public String chat(String prompt) {
                return chatClient.prompt()
                .user(userMessage -> userMessage.text(prompt))
                .call()
                .content();
            }
        }
        ```

        在此服务中，我们注入了自动配置的 Gemini `ChatModel` 以创建 `ChatClient` 实例。

        由于 LLM 本身是无状态的，无法记住之前的对话内容，因此我们还注入了一个 `ChatMemory` 实例，以便提供类对话体验。

        我们还需要一个 `ChatController` 用于测试：

        ```java
        @RestController
        public class ChatController {
            private final ChatService chatService;

            public ChatController(ChatService chatService) {
                this.chatService = chatService;
            }

            @PostMapping("/chat")
            public ResponseEntity<String> chat(@RequestBody @NotNull String prompt) {
                String response = chatService.chat(prompt);
                return ResponseEntity.ok(response);
            }
        }
        ```

        该控制器接收请求体中的字符串，并通过 `ChatService` 将提示发送给 Gemini 聊天模型。

    3. 测试运行

        现在，我们可以通过 Postman 向该端点发送提示进行测试，应能收到 Gemini 的响应。

4. 文本嵌入（Text Embedding）

    文本嵌入是将自然语言文本转换为高维向量表示的过程。嵌入的典型应用场景包括基于语义相似性的搜索。

    1. 配置

        我们需要为文本嵌入使用不同的模型。在 `application.yml` 中添加以下属性：

        ```yaml
        spring:
        ai:
            vertex:
            ai:
                embedding:
                project-id: <YOUR-GOOGLE-CLOUD-PROJECT-ID>
                location: "europe-west1"
                text:
                    options:
                    model: "gemini-embedding-001"
        ```

        与聊天模型类似，我们需要定义 `project-id` 和 `location` 属性，其值可沿用前文聊天配置中的设置。

    2. 服务

        现在，我们的应用已配置好，可将 `EmbeddingModel` 注入服务中。定义一个 `TextEmbeddingService` 类，用于将文本转换为嵌入向量：

        ```java
        @Service
        public class TextEmbeddingService {
            private final EmbeddingModel embeddingModel;

            public TextEmbeddingService(EmbeddingModel embeddingModel) {
                this.embeddingModel = embeddingModel;
            }

            public EmbeddingResponse getEmbedding(String... texts) {
                EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(texts), null);
                return embeddingModel.call(request);
            }
        }
        ```

        同时创建一个 `TextEmbeddingController` 用于测试：

        ```java
        @RestController
        public class TextEmbeddingController {
            private final TextEmbeddingService textEmbeddingService;

            public TextEmbeddingController(TextEmbeddingService textEmbeddingService) {
                this.textEmbeddingService = textEmbeddingService;
            }

            @PostMapping("/embedding/text")
            public ResponseEntity<EmbeddingResponse> getEmbedding(@RequestBody @NotNull String text) {
                EmbeddingResponse response = textEmbeddingService.getEmbedding(text);
                return ResponseEntity.ok(response);
            }
        }
        ```

    3. 测试运行

        现在，我们可以测试文本嵌入服务了。向该端点发送一些文本，观察返回结果：

        请求完成后，端点会返回元数据，最重要的是在 `output` 属性中包含的嵌入向量。

5. 多模态嵌入（Multimodal Embedding）

    除了文本，Vertex AI 还能将图像等多媒体内容转换为嵌入向量。

    多模态嵌入服务**无需额外配置**，只需沿用前文的文本嵌入配置即可。

    1. 服务

        创建 `MultiModalEmbeddingService`，用于将不同图像转换为嵌入向量：

        ```java
        @Service
        public class MultiModalEmbeddingService {
            private final DocumentEmbeddingModel documentEmbeddingModel;

            public MultiModalEmbeddingService(DocumentEmbeddingModel documentEmbeddingModel) {
                this.documentEmbeddingModel = documentEmbeddingModel;
            }

            public EmbeddingResponse getEmbedding(MimeType mimeType, Resource resource) {
                Document document = new Document(new Media(mimeType, resource), Map.of());
                DocumentEmbeddingRequest request = new DocumentEmbeddingRequest(List.of(document));
                return documentEmbeddingModel.call(request);
            }
        }
        ```

        我们需要提供图像的 `Resource` 及其 MIME 类型。目前，Vertex AI 支持 BMP、GIF、JPG 和 PNG 格式的图像。

        创建一个控制器，接收请求中的图像文件，从内容类型中提取 MIME 类型，并将图像资源和 MIME 类型传递给 `MultiModalEmbeddingService`：

        ```java
        @RestController
        public class MultiModalEmbeddingController {
            private final MultiModalEmbeddingService embeddingService;

            public MultiModalEmbeddingController(MultiModalEmbeddingService embeddingService) {
                this.embeddingService = embeddingService;
            }

            @PostMapping("/embedding/image")
            public ResponseEntity<EmbeddingResponse> getEmbedding(@RequestParam("image") @NotNull MultipartFile imageFile) {
                EmbeddingResponse response = embeddingService.getEmbedding(
                MimeType.valueOf(imageFile.getContentType()),
                imageFile.getResource());
                return ResponseEntity.ok(response);
            }
        }
        ```

    2. 测试运行

        这次，我们向控制器端点发送一张图像而非文本：

        请求完成后，我们会收到与文本嵌入类似的响应，图像嵌入向量同样位于响应的 `output` 属性中。

6. 结论

    Spring AI 简化了大语言模型与应用程序的集成，使我们能够以最小的开发成本快速采用和切换不同的 LLM。

    在本文中，我们探索了如何在 Spring Boot 应用程序中配置 Vertex AI，学习了如何应用 Gemini 聊天模型和嵌入模型，将文本和图像转换为嵌入向量，以便进一步处理和分析。
