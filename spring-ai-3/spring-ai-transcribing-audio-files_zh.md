# [使用 Spring AI 和 OpenAI 转录音频文件](https://www.baeldung.com/spring-ai-transcribing-audio-files)

人工智能 · Spring AI · OpenAI  

1. 概述

    企业经常需要从各类音频内容中提取有意义的数据，例如：为客服通话生成文字记录以进行情感分析、为视频生成字幕，或为会议生成[纪要](https://en.wikipedia.org/wiki/Minutes)。然而，手动转录音频文件既耗时又昂贵。

    为实现自动化，[OpenAI](https://platform.openai.com/docs/overview) 提供了强大的[语音转文本模型](https://platform.openai.com/docs/guides/speech-to-text)，能够准确转录多种语言的音频文件。

    在本教程中，我们将探索如何使用 Spring AI 调用 OpenAI 的语音转文本模型来转录音频文件。

    要跟随本教程操作，您需要一个 OpenAI API 密钥。

2. 项目设置

    在开始实现音频转录器之前，我们需要添加必要的依赖并正确配置应用程序。

    1. 依赖项

        首先，在项目的 `pom.xml` 文件中添加 Spring AI 的 OpenAI Starter 依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
            <version>1.0.0-M7</version>
        </dependency>
        ```

        由于当前版本 `1.0.0-M7` 是一个里程碑版本，我们还需要在 `pom.xml` 中添加 Spring Milestones 仓库：

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

        此仓库用于发布里程碑版本，而不是标准的 Maven Central 仓库。

    2. 配置 OpenAI 属性

        接下来，在 `application.yaml` 文件中配置 OpenAI API 密钥和语音转文本模型：

        ```yaml
        spring:
        ai:
            openai:
            api-key: ${OPENAI_API_KEY}
            audio:
                transcription:
                options:
                    model: whisper-1
                    language: en
        ```

        我们使用 `${}` 属性占位符从环境变量加载 API 密钥。

        这里，我们通过模型 ID `whisper-1` 指定 OpenAI 的 [Whisper](https://openai.com/index/whisper/) 模型。需要注意的是，OpenAI 还提供更先进、质量更高的语音转文本模型，如 `gpt-4o-transcribe` 和 `gpt-4o-mini-transcribe`，但当前版本的 Spring AI 尚不支持这些模型。

        此外，我们指定 `en` 作为音频文件的语言。根据需求，也可以指定其他 [ISO-639-1](https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes) 格式的输入语言。如果不指定，模型将尝试自动检测音频中的语言。

        配置完成后，Spring AI 会自动创建一个 `OpenAiAudioTranscriptionModel` 类型的 Bean，供我们与指定模型交互。

3. 构建音频转录器

    配置完成后，让我们创建一个 `AudioTranscriber` 服务类。我们将注入 Spring AI 自动创建的 `OpenAiAudioTranscriptionModel` Bean。

    首先，定义两个简单的记录类，用于表示请求和响应的有效载荷：

    ```java
    record TranscriptionRequest(MultipartFile audioFile, @Nullable String context) {}

    record TranscriptionResponse(String transcription) {}
    ```

    `TranscriptionRequest` 包含待转录的 `audioFile` 和一个可选的 `context`（上下文提示），用于辅助模型进行转录。需要注意的是，OpenAI 目前支持 `mp3`、`mp4`、`mpeg`、`mpga`、`m4a`、`wav` 和 `webm` 格式的音频文件。

    同样，`TranscriptionResponse` 仅包含生成的转录文本。

    现在，实现核心功能：

    ```java
    TranscriptionResponse transcribe(TranscriptionRequest transcriptionRequest) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(
        transcriptionRequest.audioFile().getResource(),
        OpenAiAudioTranscriptionOptions
            .builder()
            .prompt(transcriptionRequest.context())
            .build()
        );
        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
        return new TranscriptionResponse(response.getResult().getOutput());
    }
    ```

    这里，我们在 `AudioTranscriber` 类中添加了一个 `transcribe()` 方法。

    我们使用音频文件资源和可选的上下文提示创建一个 `AudioTranscriptionPrompt` 对象，然后调用自动装配的 `OpenAiAudioTranscriptionModel` Bean 的 `call()` 方法。

    最后，从响应中提取转录文本，并将其包装在 `TranscriptionResponse` 记录中返回。

    目前，语音转文本模型对音频文件大小的限制为 25 MB。但默认情况下，Spring Boot 将上传文件大小限制为 1 MB。让我们在 `application.yaml` 文件中增加此限制：

    ```yaml
    spring:
    servlet:
        multipart:
        max-file-size: 25MB
        max-request-size: 25MB
    ```

    我们将最大文件大小和请求大小设置为 25MB，这应该足以满足大多数音频转录请求。

4. 测试音频转录器

    完成服务层实现后，让我们在其上暴露一个 REST API：

    ```java
    @PostMapping("/transcribe")
    ResponseEntity<TranscriptionResponse> transcribe(
    @RequestParam("audioFile") MultipartFile audioFile,
    @RequestParam("context") String context
    ) {
        TranscriptionRequest transcriptionRequest = new TranscriptionRequest(audioFile, context);
        TranscriptionResponse response = audioTranscriber.transcribe(transcriptionRequest);
        return ResponseEntity.ok(response);
    }
    ```

    接着，使用 HTTPie CLI 调用上述 API 端点：

    ```bash
    http -f POST :8080/transcribe audioFile@baeldung-audio-description.mp3 context="Short description about Baeldung"
    ```

    这里，我们调用 `/transcribe` API，并发送音频文件及其上下文提示。为演示目的，我们准备了一个简要介绍 Baeldung 的音频文件，该文件位于代码库的 `src/test/resources/audio` 文件夹中。

    让我们看看返回的响应：

    ```json
    {
    "transcription": "Baeldung is a top-notch educational platform that specializes in Java, Spring, and related technologies. It offers a wealth of tutorials, articles, and courses that help developers master programming concepts. Known for its clear examples and practical guides, Baeldung is a go-to resource for developers looking to level up their skills."
    }
    ```

    如我们所见，API 正确返回了所提供音频文件的转录文本。

    请注意，提供上下文提示有助于模型正确转录“Baeldung”这个名称。如果没有此上下文，Whisper 模型会将其转录为“Baildung”。

5. 结论

    在本文中，我们探索了如何在 Spring AI 中使用 OpenAI 转录音频文件。

    我们逐步完成了必要的配置，并使用 OpenAI 的 Whisper 语音转文本模型实现了一个音频转录器。我们还测试了应用程序，并观察到提供上下文提示可以显著提高生成转录的准确性，尤其是在处理特定领域名称时。
