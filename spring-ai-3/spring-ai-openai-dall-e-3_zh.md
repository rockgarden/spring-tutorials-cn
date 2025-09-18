# [使用 OpenAI DALL·E 3 在 Java 中生成 AI 图像](https://www.baeldung.com/spring-ai-openai-dall-e-3)

人工智能 · Spring AI · OpenAI  

1. 概述

    人工智能正在改变我们构建 Web 应用的方式。AI 一个令人兴奋的应用场景是：**根据文本描述生成图像**。OpenAI 的 **DALL·E 3** 是目前流行的文生图模型，可帮助我们实现这一目标。

    在本教程中，我们将探索如何使用 **Spring AI** 调用 [OpenAI 的 DALL·E 3](https://openai.com/index/dall-e-3/) 模型生成图像。

    > 📌 **前提条件**：您需要一个有效的 OpenAI API 密钥。

2. 项目搭建

    在开始生成 AI 图像前，我们需要添加 Spring Boot Starter 依赖并正确配置应用。

    1. 依赖项

        首先，在项目的 `pom.xml` 文件中添加 `spring-ai-openai-spring-boot-starter`：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>1.0.0-M3</version>
        </dependency>
        ```

        由于当前版本 `1.0.0-M3` 是里程碑版本（Milestone），我们还需在 `pom.xml` 中添加 Spring Milestones 仓库：

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

        > ⚠️ 此仓库用于发布里程碑版本，而非标准的 Maven Central。

        该 Starter 依赖为我们提供了与 OpenAI 服务交互、使用 DALL·E 3 模型生成图像所需的全部类。

    2. 配置 OpenAI API 密钥

        要与 OpenAI 服务交互，需在 `application.yaml` 中配置 API 密钥：

        ```yaml
        spring:
        ai:
            openai:
            api-key: ${OPENAI_API_KEY}
        ```

        我们使用 `${}` 占位符从环境变量加载密钥值。

        配置有效 API 密钥后，Spring AI 将自动为我们创建一个 `ImageModel` Bean。我们将在服务层中自动装配它，并发送图像生成请求。

    3. 配置默认图像选项

        接下来，我们配置一些默认图像生成参数：

        ```yaml
        spring:
        ai:
            openai:
            image:
                options:
                model: dall-e-3
                size: 1024x1024
                style: vivid
                quality: standard
                response-format: url
        ```

        - **model**: 指定使用 `dall-e-3` 模型。
        - **size**: 设置图像尺寸为 `1024x1024`（正方形）。其他可选尺寸：`1792x1024` 或 `1024x1792`。
        - **style**: 设为 `vivid` —— 生成超现实、戏剧性图像。另一选项 `natural` 用于生成更自然、写实风格的图像。
        - **quality**: 设为 `standard`，适用于大多数场景。如需更高细节和一致性，可设为 `hd`（生成时间更长）。
        - **response-format**: 设为 `url` —— 生成图像通过有效期 60 分钟的 URL 访问。也可设为 `b64_json` 以 Base64 字符串形式返回图像。

        稍后我们将学习如何动态覆盖这些默认选项。

3. 使用 DALL·E 3 生成 AI 图像

    项目配置完成后，我们创建 `ImageGenerator` 类，自动装配 `ImageModel` Bean 用于生成图像：

    ```java
    public String generate(String prompt) {
        ImagePrompt imagePrompt = new ImagePrompt(prompt);
        ImageResponse imageResponse = imageModel.call(imagePrompt);
        return resolveImageContent(imageResponse);
    }

    private String resolveImageContent(ImageResponse imageResponse) {
        Image image = imageResponse.getResult().getOutput();
        return Optional
        .ofNullable(image.getUrl())
        .orElseGet(image::getB64Json);
    }
    ```

    - `generate()` 方法接收一个 `prompt` 字符串，即我们希望生成图像的文本描述。
    - 创建 `ImagePrompt` 对象并传入提示语，调用 `imageModel.call()` 发送请求。
    - `imageResponse` 包含图像的 URL 或 Base64 字符串（取决于 `response-format` 配置）。
    - `resolveImageContent()` 辅助方法用于根据配置返回正确的图像内容（URL 或 Base64）。

4. 覆盖默认图像选项

    有时我们需要动态覆盖 `application.yaml` 中的默认配置。

    我们通过重载 `generate()` 方法实现：

    ```java
    public String generate(ImageGenerationRequest request) {
        ImageOptions imageOptions = OpenAiImageOptions
        .builder()
        .withUser(request.username())
        .withHeight(request.height())
        .withWidth(request.width())
        .build();
        ImagePrompt imagePrompt = new ImagePrompt(request.prompt(), imageOptions);

        ImageResponse imageResponse = imageModel.call(imagePrompt);
        return resolveImageContent(imageResponse);
    }

    record ImageGenerationRequest(
        String prompt,
        String username,
        Integer height,
        Integer width
    ) {}
    ```

    - 创建 `ImageGenerationRequest` 记录类，除提示语外，还包含用户名、图像高度和宽度。
    - 使用这些值构建 `ImageOptions` 实例，并传入 `ImagePrompt` 构造器。
    > 💡 注意：`OpenAiImageOptions` 类没有 `size` 属性，需分别设置 `height` 和 `width`。
    - [user](https://platform.openai.com/docs/guides/safety-best-practices/end-user-ids#end-user-ids) 选项用于关联特定终端用户，是防止滥用的安全最佳实践。
    - 按需，我们也可覆盖 `style`、`quality`、`response-format` 等其他选项。

5. 测试 ImageGenerator 类

    现在，让我们测试 `ImageGenerator` 类：

    ```java
    String prompt = """
        一幅卡通画：一头戴着墨镜、在城市街道上吃葡萄的黑帮驴子。
    """;
    String response = imageGenerator.generate(prompt);
    ```

    我们将提示语传入 `generate()` 方法，稍等片刻后，将收到包含图像 URL 或 Base64 字符串的响应（取决于配置）。

    > **提示语生成结果**：一幅卡通画：一头戴着墨镜、在城市街道上吃葡萄的黑帮驴子。

    如图所示，生成的图像精准匹配了我们的文本描述，充分展现了 DALL·E 3 理解自然语言并转化为视觉内容的强大能力。

6. 结论

    在本文中，我们探索了如何使用 **Spring AI** 根据文本描述生成 AI 图像，底层使用的是 OpenAI 的 **DALL·E 3** 模型。

    我们完成了必要的配置，开发了图像生成服务类，并学习了默认选项及其动态覆盖方法。

    通过在 Java 应用中集成 DALL·E 3，我们无需自行训练和托管模型，即可轻松为应用添加强大的图像生成功能。
