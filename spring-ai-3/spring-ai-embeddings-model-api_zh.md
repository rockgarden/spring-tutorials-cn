# [Spring AI 中嵌入模型 API 指南](https://www.baeldung.com/spring-ai-embeddings-model-api)

人工智能    Spring +    Spring AI  

1. 概述

    将人工智能集成到应用程序中通常涉及处理文本数据。该领域的一项关键技术是**嵌入模型（Embedding Model）**，它将文本信息转换为应用程序可处理的向量表示（嵌入）。

    在本教程中，我们将探索 Spring AI 中的嵌入模型 API。这个强大的 API 提供了抽象层，使我们能够轻松采用不同的嵌入模型，仅需极少的工作量，即可帮助应用程序理解文本语义。

2. 嵌入简介

    为了训练 AI 模型理解文本和图像的语义含义，我们通常将这些数据类型转换为高维向量表示——即**嵌入（Embeddings）**。

    AI 模型通过计算嵌入之间的相似度来理解它们的关系。当两个嵌入的相似度得分越高，说明它们所代表文本的语义越相近。

3. 嵌入模型 API

    Spring AI 提供了一组 API，简化了嵌入模型的使用。这些 API 以接口形式封装了所有实现细节。

    1. EmbeddingModel

        嵌入模型是一种经过训练的机器学习模型，可将段落、图像等不同对象转换为高维向量空间。

        不同提供商（如 BERT）提供不同的模型。Spring AI 的嵌入 API 通过 `EmbeddingModel` 接口封装了采用嵌入模型的细节：

        ```java
        public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {
            EmbeddingResponse call(EmbeddingRequest request);

            // 构造器及其他方法
        }
        ```

        `call()` 方法接收一个包含数据源的 `EmbeddingRequest`，将其发送给模型提供商，并返回包含 `Embedding` 的 `EmbeddingResponse`。

    2. EmbeddingRequest

        `EmbeddingRequest` 包含待转换为嵌入的文本列表。除文本外，还可包含特定于所选 `EmbeddingModel` 的附加选项：

        ```java
        public class EmbeddingRequest implements ModelRequest<List<String>> {
            private final List<String> inputs;
            private final EmbeddingOptions options;

            // 构造器及其他方法
        }
        ```

    3. EmbeddingResponse

        `EmbeddingResponse` 封装了来自嵌入模型提供商的响应，包含 `Embedding` 对象列表及额外元数据（如 token 使用情况）：

        ```java
        public class EmbeddingResponse implements ModelResponse<Embedding> {
            private final List<Embedding> embeddings;
            private final EmbeddingResponseMetadata metadata;

            // 构造器及其他方法
        }
        ```

    4. Embedding

        `Embedding` 包含以浮点数组形式表示的向量。维度取决于所选嵌入模型，通常从几百维到几千维不等：

        ```java
        public class Embedding implements ModelResult<float[]> {
            private final float[] embedding;
            private final Integer index;
            private final EmbeddingResultMetadata metadata;

            // 构造器及其他方法
        }
        ```

4. 与 OpenAI 集成

    Spring AI 支持 OpenAI 作为嵌入模型集成选项之一。本节中，我们将采用 OpenAI 并创建一个 Spring 服务，用于将文本转换为嵌入。

    1. Maven 依赖

        首先，在 `pom.xml` 中添加以下 Spring AI OpenAI 依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>1.0.0-M6</version>
        </dependency>
        ```

    2. OpenAI 配置

        要完成 OpenAI 与 Spring AI 的集成，需在配置中提供用于认证的 API 密钥：

        ```yaml
        spring:
        ai:
            openai:
            api-key: "<YOUR-API-KEY>"
        ```

    3. EmbeddingModel 的自动配置

        Spring AI 支持自动配置 `EmbeddingModel`。我们只需在 `application.yml` 中添加一个属性，定义所使用的嵌入模型：

        ```yaml
        spring:
        ai:
            openai:
            embedding:
                options:
                model: "text-embedding-3-small"
        ```

        `model` 属性配置了我们要使用的嵌入模型。OpenAI 目前提供三种不同模型。

        定义该模型后，我们只需将 `EmbeddingModel` 注入 Spring Boot 服务，无需指定任何 OpenAI 细节——所有实现均依赖 Spring AI 嵌入 API：

        ```java
        @Service
        public class EmbeddingService {
            private final EmbeddingModel embeddingModel;

            public EmbeddingService(EmbeddingModel embeddingModel) {
                this.embeddingModel = embeddingModel;
            }

            public EmbeddingResponse getEmbeddings(String... texts) {
                EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(texts), null);
                return embeddingModel.call(request);
            }
        }
        ```

        自动配置为我们提供了便利，同时隐藏了具体实现细节。这使我们只需更新 `application.yml` 即可轻松切换不同实现。

    4. EmbeddingModel 的手动配置

        虽然自动配置很方便，但在某些场景下（如应用需使用多个嵌入模型或不同提供商的模型）缺乏灵活性。

        此时，我们可以在配置类中手动定义嵌入模型：

        ```java
        @Configuration
        public class EmbeddingConfig {
            @Bean
            public OpenAiApi openAiApi(@Value("${spring.ai.openai.api-key}") String apiKey) {
                return OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
            }

            @Bean
            public OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi) {
                OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model("text-embedding-3-small")
                .build();
                return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
            }
        }
        ```

        示例中，我们首先使用注入的 API 密钥创建了 OpenAI 客户端 `OpenAiApi`，然后基于该客户端创建了 OpenAI 嵌入模型。

        相应地，我们稍作调整服务，注入具体的 `OpenAiEmbeddingModel` 实现而非 `EmbeddingModel` 接口：

        ```java
        @Service
        public class ManualEmbeddingService {
            private final OpenAiEmbeddingModel openAiEmbeddingModel;

            public ManualEmbeddingService(OpenAiEmbeddingModel openAiEmbeddingModel) {
                this.openAiEmbeddingModel = openAiEmbeddingModel;
            }

            public EmbeddingResponse getEmbeddings(String... texts) {
                EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(texts), null);
                return openAiEmbeddingModel.call(request);
            }
        }
        ```

5. 测试嵌入服务

    基于前文的自动配置服务实现，我们暴露一个 REST 端点用于测试嵌入服务：

    ```java
    @RestController
    public class EmbeddingController {
        private final EmbeddingService embeddingService;

        public EmbeddingController(EmbeddingService embeddingService) {
            this.embeddingService = embeddingService;
        }

        @PostMapping("/embeddings")
        public ResponseEntity<EmbeddingResponse> getEmbeddings(@RequestBody String text) {
            EmbeddingResponse response = embeddingService.getEmbeddings(text);
            return ResponseEntity.ok(response);
        }
    }
    ```

    通过 curl 向该端点发送包含文本的请求：

    ```bash
    curl -X POST http://localhost:8080/embeddings -H "Content-Type: text/plain" -d "Hello world"
    ```

    我们将获得如下响应（为简洁已截断）：

    ```json
    {
        "metadata": {
            "model": "text-embedding-3-small",
            "usage": {
                "promptTokens": 2,
                "completionTokens": 0,
                "totalTokens": 2,
                "nativeUsage": {
                    "prompt_tokens": 48,
                    "total_tokens": 48
                }
            },
            "empty": true
        },
        "result": {
            "index": 0,
            "metadata": {
                "modalityType": "TEXT",
                "documentId": "",
                "mimeType": {
                    "type": "text",
                    "subtype": "plain",
                    "parameters": {},
                    "charset": null,
                    "concrete": true,
                    "wildcardSubtype": false,
                    "subtypeSuffix": null,
                    "wildcardType": false
                },
                "documentData": null
            },
            "output": [
                -0.0020785425,
                -0.049085874,
                ...
        ]
        }
    }
    ```

    请注意，这不是完整响应（因向量很长已截断），我们仅展示 JSON 中两个主要顶层节点：`metadata` 和 `result`。

    - `metadata` 提供模型信息及转换过程中的资源使用情况。`model` 表示所选 OpenAI 模型，`totalTokens` 显示转换消耗的 token 数量。
    - `result` 包含嵌入结果。其中 `output` 是一个浮点数组，即嵌入模型根据我们提供的文本生成的嵌入向量。

6. 结论

    Spring AI 的嵌入模型 API 提供了抽象层，并支持如 OpenAI 等模型提供商，使我们能够轻松将其集成到 Java 应用程序中。

    在本文中，我们以 OpenAI 为参考嵌入模型，分别演示了为简化开发的自动配置和为灵活需求的手动配置。嵌入 API 使我们能够将文本转换为嵌入向量，为语义搜索、推荐系统等高级功能奠定基础。
