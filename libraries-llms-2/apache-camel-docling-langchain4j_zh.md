# 使用 Apache Camel、Docling 和 LangChain4j 构建智能文档处理系统

**最后更新：** 2026年9月2日
**作者：** Olayemi Michael
**审阅：** Milos Simic
**标签：** 人工智能、Apache Camel、LangChain

1. 概述

    当文档内容处于上下文窗口内时，大语言模型（LLM）可以回答有关文档内容的问题。然而，许多现实世界中的文档以 PDF 等格式存储，AI 应用很难直接处理这些格式。Docling 能够将这些文档转换为结构化格式，从而实现高效解析。

    在本教程中，我们将使用 Apache Camel、Docling 和 LangChain4j 构建一个文档处理系统。此外，我们还将使用嵌入式 Undertow 服务器定义一个简单的问答 API。

2. 问题陈述

    许多现实世界的文档包含标题、表格和图像等结构信息，这些信息在文本提取过程中很难保留。因此，AI 流水线可能会丢失有价值的上下文，从而降低检索质量和生成回复的准确性。

    结构化表示通过保留文档的组织结构和关系来解决这一问题。Markdown、HTML 和 JSON 等结构化格式保留了大部分文档的语义结构，使其更易于处理和解释。

    IBM Docling 弥补了这一差距，它将非结构化或半结构化文档转换为下游 AI 应用可以高效消费的结构化表示。我们可以将生成的 Markdown 存储在磁盘上。随后，应用程序可以加载它并将其包含在 LLM 的提示词（Prompt）中。这避免了应用程序在处理每个请求时都重复转换同一份文档。

3. 项目设置

    让我们使用 Apache Camel 和 LangChain4j 初始化一个 Java 项目。

    1. Maven 依赖

        首先，让我们在 `pom.xml` 中添加 `camel-core`、`camel-main` 和 `langchain4j` 依赖项：

        ```xml
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-core</artifactId>
            <version>4.20.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-main</artifactId>
            <version>4.20.0</version>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
            <version>1.11.8</version>
        </dependency>
        ```

        `camel-core` 依赖提供了诸如 `RouteBuilder` 之类的类，用于使用 Java DSL 定义 Camel 路由；`camel-main` 为独立 Camel 应用提供了轻量级运行时；`langchain4j` 则提供了与 LLM 交互的核心 API。

        接下来，让我们在 `pom.xml` 中添加 `camel-docling`、`camel-langchain4j-chat` 和 `langchain4j-open-ai` 依赖项：

        ```xml
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-docling</artifactId>
            <version>4.20.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-langchain4j-chat</artifactId>
            <version>4.20.0</version>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai</artifactId>
            <version>1.11.8</version>
        </dependency>
        ```

        其中，`camel-docling` 将 Apache Camel 与 Docling 服务器集成，允许路由提交文档以转换为 Docling 的结构化文档表示。`camel-langchain4j-chat` 使 Camel 路由能够将提示词发送给 LangChain4j 聊天模型并处理生成的响应。`langchain4j-open-ai` 依赖提供了将 LangChain4j 连接到 OpenAI 兼容聊天模型所需的实现。

        所有 Apache Camel 依赖项（包括 `camel-docling`）均使用相同的版本，以确保兼容性并避免意外的运行时错误。

        让我们还在 `pom.xml` 中添加 `camel-undertow` 依赖项：

        ```xml
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-undertow</artifactId>
            <version>4.20.0</version>
        </dependency>
        ```

        它使 Camel 能够使用嵌入式 [Undertow](https://www.baeldung.com/jboss-undertow) 服务器公开 HTTP 端点。

        最后，让我们在项目的根目录下创建一个名为 `documents` 的目录，并将 `policies.pdf` 文件放入其中。

    2. Docker Compose 文件

        在本地运行 Docling 最简单的方法是使用 `docling-serve`，它通过 REST API 公开 Docling 的文档转换功能。为简单起见，我们在 Docker 容器中运行 `docling-serve`。

        首先，在项目根目录中创建一个新文件 `docker-compose.yml` 并添加以下配置：

        ```yaml
        services:
        docling-serve:
            image: quay.io/docling-project/docling-serve:latest
            container_name: docling-serve
            ports:
            - "5001:5001"
            environment:
            - DOCLING_STORAGE_DIR=/data
            - DOCLING_PORT=5001
            volumes:
            - ./data/docling:/data
            restart: unless-stopped
        ```

        该配置从 `quay.io/docling-project/docling-serve` 镜像启动一个 `docling-serve` 容器。如果本地没有该镜像，Docker 会自动从注册表中拉取。它还将主机上的 5001 端口映射到容器内的 5001 端口，允许我们的应用程序与 Docling 服务器通信。

        最后，通过运行以下命令启动 Docling 服务器：

        ```bash
        docker compose up
        ```

    3. 应用程序入口点

        让我们创建一个 `CamelDoclingApplication` 类作为应用程序的入口点：

        ```java
        public class CamelDoclingApplication {

            public static void main(String[] args) throws Exception {
                OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(System.getenv().getOrDefault("LLM_BASE_URL", "http://127.0.0.1:8000/v1"))
                .apiKey(System.getenv().getOrDefault("LLM_API_KEY", "mlx"))
                .modelName(System.getenv().getOrDefault("LLM_MODEL", "mlx-community/Qwen3.8-27B-4bit"))
                .timeout(Duration.ofSeconds(Long.parseLong(System.getenv().getOrDefault("LLM_TIMEOUT_SECONDS", "300"))))
                .maxRetries(0)
                .build();
                Main main = new Main();
                main.bind("chatModel", model);
                main.run(args);
            }
        }
        ```

        这里使用与 OpenAI Chat Completions API 兼容的本地 MLX-VLM 服务。`baseUrl` 必须填写服务地址及 `/v1` 前缀，`modelName` 必须与服务 `/v1/models` 返回的模型 ID 完全一致。默认值对应以下启动命令：

        ```bash
        mlx_vlm.server --model mlx-community/Qwen3.8-27B-4bit --port 8000 --host 127.0.0.1
        ```

        本地服务不校验 API Key，因此示例使用占位值 `mlx`。如需切换服务地址、模型或 API Key，可通过 `LLM_BASE_URL`、`LLM_MODEL` 和 `LLM_API_KEY` 环境变量覆盖默认值。例如：

        ```bash
        export LLM_BASE_URL=http://127.0.0.1:8000/v1
        export LLM_MODEL=mlx-community/Qwen3.8-27B-4bit
        export LLM_API_KEY=mlx
        export LLM_TIMEOUT_SECONDS=300
        ```

        如果要改用 LangChain4j 提供的 OpenAI 兼容演示端点，可在启动应用前设置以下环境变量。请在项目模块目录中、同一个终端会话里运行：

        ```bash
        export LLM_BASE_URL=http://langchain4j.dev/demo/openai/v1
        export LLM_API_KEY=demo
        export LLM_MODEL=gpt-4o-mini
        export LLM_TIMEOUT_SECONDS=300

        mvn exec:java -Dexec.mainClass=com.baeldung.camellangchain4jdocling.CamelDoclingApplication
        ```

        如果应用已经在运行，请先按 `Ctrl+C` 停止，再设置环境变量并重新启动。使用演示端点时不需要关闭本地 MLX 服务；PDF 转换仍需要 Docling 服务正常运行。

        本地大模型处理较长文档可能超过客户端默认的 60 秒请求超时。示例将超时设为 300 秒，可通过 `LLM_TIMEOUT_SECONDS` 调整；同时关闭 LangChain4j 的自动重试，避免一次超时后重复发送长请求。如果使用需要认证的 OpenAI 兼容服务，请将 `LLM_API_KEY` 设置为有效凭证。

        `Main` 实例为独立 Camel 应用提供了轻量级运行时。我们将聊天模型绑定到 Camel 注册表中，名称为 `chatModel`，以便 Camel 组件和路由在消息处理期间引用。

4. 将 PDF 文档转换为 Markdown

    现在 Docling 服务器已经运行，让我们创建第一个 Camel 路由，将 `policies.pdf` 转换为 Markdown：

    ```java
    class ConversionRoute extends RouteBuilder {

        @Override
        public void configure() {

            from("file:documents?include=.*\\.(pdf|docx|pptx|html|md)&noop=true&idempotent=true")
            .routeId("document-analysis-workflow")
            .log("Processing document: ${header.CamelFileName}")
            .to("docling:CONVERT_TO_MARKDOWN?useDoclingServe=true&doclingServeUrl=http://localhost:5001&contentInBody=true")
            .setProperty("convertedMarkdown", body())
            .setHeader(Exchange.FILE_NAME, simple("${header.CamelFileName.replaceFirst('\\.[^.]+$', '')}.md"))
            .to("file:output"); 
        }
    }
    ```

    这里，我们监控 `documents` 目录中支持的文档类型。`noop=true` 选项使原始文件在处理后保留在原位，而 `idempotent=true` 防止同一文件被多次处理。注意，Camel 文件组件已经提供了 Docling 组件处理文档所需的信息。虽然我们可以显式地将消息体替换为文档路径，但这其实是多余的，因为无需此操作转换也能正常工作。

    接下来，`docling:CONVERT_TO_MARKDOWN` 端点将文档发送到本地运行的 `docling-serve` 实例，该实例将其转换为 Markdown 并在消息体中返回生成的内容。

    最后，我们将转换后的 Markdown 存储为交换属性（Exchange Property），通过将原始扩展名替换为 `.md` 来重命名输出文件，并使用 Camel 文件组件将其写入 `output` 目录。

    如果 `documents` 目录中有多个文档，Camel 会独立处理每个文档，并在 `output` 目录中生成相应的 Markdown 文件。

    让我们通过将 `ConversionRoute` 注册到 Camel 运行时来运行应用程序：

    ```java
    Main main = new Main();
    main.bind("chatModel", model);
    main.configure()
    .addRoutesBuilder(new ConversionRoute());
    main.run(args);
    ```

    在这里，我们通过 `Main` 实例将 `ConversionRoute` 注册到 Camel 中。

5. 将 Markdown 文档传递给 LangChain4j

    既然我们已经将文档转换为 Markdown，接下来将其发送给聊天模型并要求其分析文档：

    ```java
    class ConversionRoute extends RouteBuilder {

        @Override
        public void configure() {

            from("file:documents?include=.*\\.(pdf|docx|pptx|html|md)&noop=true&idempotent=true")
            .routeId("document-analysis-workflow")
            // ... (前序步骤)
            .setBody(simple("""
                You are a helpful document analysis assistant. Please analyze
                the following document and provide:
                1. A brief summary (2-3 sentences)
                2. Key topics and main points
                3. Any important findings or conclusions

                Document content:
                ${exchangeProperty.convertedMarkdown}
                """))
            .to("langchain4j-chat:analysis?chatModel=#chatModel")
            .setHeader(
                Exchange.FILE_NAME,
                simple("${header.CamelFileName.replaceFirst('\\.[^.]+$', '')}-analysis.md")
            )
            .to("file:analysis");
        }
    }
    ```

    在此路由中，我们构建了一个包含 Docling 生成的 Markdown 的提示词，并指示模型对文档进行总结和分析。`${exchangeProperty.convertedMarkdown}` 表达式会在提示词发送给模型之前，插入先前存储在路由中的 Markdown 内容。

    接下来，`langchain4j-chat` 组件将提示词发送给由 `#chatModel` 引用的聊天模型。Camel 从其注册表中解析此引用，即我们之前绑定的 `OpenAiChatModel` 实例。`langchain4j-chat:analysis` 中的 `analysis` 是端点名称，主要用于区分此端点与其他端点。

    最后，我们通过在原始文件名后追加 `-analysis` 来重命名输出文件，并使用文件组件将模型的响应写入 `analysis` 目录。

6. 交互式问答 API

    现在，是时候定义一个问答 API 了，该 API 使用转换后的 Markdown 文档作为知识源。

    让我们创建一个通过 HTTP 端点接收问题的 Camel 路由：

    ```java
    public class QuestionAndAnswerRoute extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            from("undertow:http://0.0.0.0:8080/api/ask?httpMethodRestrict=POST").routeId("document-qa-api")
            .setProperty("question", bodyAs(String.class))
            .pollEnrich("file:output?fileName=policies.md&noop=true&idempotent=false")
            .setProperty("markdown", bodyAs(String.class))
            .setBody(simple("""
                You are a helpful document assistant.
                Answer ONLY using the document below.
                DOCUMENT
                =========
                ${exchangeProperty.markdown}
                QUESTION
                =========
                ${exchangeProperty.question}
                """))

            .to("langchain4j-chat:analysis?chatModel=#chatModel")
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"));
        }
    }
    ```

    这段代码做了什么？

    我们通过嵌入式 Undertow 服务器公开了 `POST /api/ask` 端点。请求体包含用户的问题，我们在稍后替换消息体之前将其存储为交换属性。

    `pollEnrich` 企业集成模式（EIP）从 `output` 目录读取预先生成的 Markdown 文档，并将其内容放入当前交换中。该路由不是每次收到请求时都转换 PDF，而是读取 Markdown 文件。通过重用预处理后的文档而不是为每个问题调用 Docling，这保持了 HTTP 请求的轻量化。

    此外，我们设置 `idempotent=false`，以便文件组件可以在每次 API 请求时读取同一个 Markdown 文件。默认情况下，`noop=true` 会启用 `idempotent=true`，这会阻止同一文件被多次消费。

    然后，我们构建一个包含文档和用户问题的提示词，再将其发送给 `langchain4j-chat` 组件。

    最后，模型的响应作为 HTTP 响应体返回，内容类型为 `text/plain`。

    让我们更新应用程序入口点以注册 `QuestionAndAnswerRoute`：

    ```java
    // ...
    main.configure()
    .addRoutesBuilder(new QuestionAndAnswerRoute());
    // ...
    ```

    应用程序启动后，我们可以运行以下 curl 命令向 API 发送问题：

    ```bash
    curl -X POST \
    http://localhost:8080/api/ask \
    -H "Content-Type: text/plain" \
    -d "How many lifecycle stages does a task move through?"
    ```

    预期的 API 响应如下：

    > A task moves through four lifecycle stages: PENDING, IN_PROGRESS, REVIEW, and DONE.
    > （任务经历四个生命周期阶段：PENDING、IN_PROGRESS、REVIEW 和 DONE。）

    API 成功地根据我们提供的文档回答了问题。

7. 结论

    在本文中，我们学习了如何集成 Apache Camel、Docling 和 LangChain4j 来构建智能文档处理应用。我们使用 Docling 将非结构化文档转换为结构化表示，使用 Apache Camel 编排转换工作流，并通过 LangChain4j 使用 LLM 分析转换后的内容。

    此外，我们还公开了一个简单的问答 API，该 API 使用转换后的文档作为上下文来生成有据可依的回复。

    通过将文档转换为结构化表示一次并在后续交互中重用生成的 Markdown，我们简化了文档处理工作流，避免了重复转换，并使 LLM 更容易处理文档内容。

8. Run Code

文档转换示例，documents/ 中的 PDF 会被处理，结果写入 output/。路由随后还会调用演示 LLM 生成分析文件，因此若演示服务不可用，Markdown 转换可能已完成，但分析文件可能不会生成。

```bash
cd /Volumes/mini/git/spring-tutorials-cn/libraries-llms-2
docker compose up -d
mvn exec:java -Dexec.mainClass=com.baeldung.camellangchain4jdocling.CamelDoclingApplication
```

启动后可以测试问答接口：

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: text/plain" \
  -d "How many lifecycle stages does a task move through?"
```

> 模型配置使用 LangChain4j 的公开演示端点，需能访问网络；若不可用（如 Incorrect API key provided），需要在 CamelDoclingApplication.java 配置自己的模型服务。
