# [Spring人工智能简介](https://www.baeldung.com/spring-ai)

1. 概述

    Spring Framework 通过 Spring AI 项目正式启用了人工智能生成提示功能。在本教程中，我们将对 Spring Boot 应用程序中的 AI 生成集成进行深入浅出的介绍，并熟悉基本的 AI 概念。我们还将了解 Spring AI 如何与模型交互，并创建一个应用程序来演示其功能。

2. Spring AI 主要概念

    在开始之前，让我们回顾一下一些关键的领域术语和概念。

    Spring AI 最初专注于处理语言输入和生成语言输出的模型。该项目背后的理念是为开发人员提供一个抽象接口，为将生成式人工智能 API 作为独立组件纳入应用程序奠定基础。

    其中一个抽象接口就是 AiClient 接口，它有两个基本实现：OpenAI 和 Azure OpenAI：

    ```java
    public interface AiClient {
        default String generate(String message);
        AiResponse generate(Prompt prompt);
    }
    ```

    AiClient 为生成函数提供了两个选项。简化的生成函数 generate(String message) 使用 String 作为输入和输出，可用于避免 Promt 和 AiResponse 类的额外复杂性。

    现在让我们来仔细看看它们的区别。

    1. 高级提示和 AiResponse

        在人工智能领域，提示是指提供给人工智能的文本信息。它由上下文和问题组成，该模型用于生成答案。

        从 Spring AI 项目的角度来看，Prompt 是一个参数化信息列表：

        ```java
        public class Prompt {
            private final List<Message> messages;
            // constructors and utility methods 
        }

        public interface Message {
            String getContent();
            Map<String, Object> getProperties();
            MessageType getMessageType();
        }
        ```

        提示使开发人员能够对文本输入进行更多控制。一个很好的例子就是使用预定义文本和占位符构建的提示模板。然后，我们可以使用传递给消息构造函数的 `Map<String, Object>` 值填充它们：

        `Tell me a {adjective} joke about {content}.`

        消息接口还包含有关人工智能模型可以处理的消息类别的高级信息。例如，OpenAI 实现了对话角色之间的区分，并通过 MessageType 进行有效映射。在其他模型中，它可以反映消息格式或其他一些自定义属性。更多详情，请参阅官方[文档](https://docs.spring.io/spring-ai/reference/api/prompt.html#_message)：

        ```java
        public class AiResponse {
            private final List<Generation> generations;
            // getters and setters
        }

        public class Generation {
            private final String text;
            private Map<String, Object> info;
        }
        ```

        AiResponse 由 Generation 对象列表组成，每个对象都包含来自相应提示符的输出。此外，Generation 对象还提供 AI 响应的元数据信息。

        不过，由于 Spring AI 项目仍处于测试阶段，并非所有功能都已完成并记录在案。我们可以通过 GitHub [存储库](https://github.com/spring-projects-experimental/spring-ai)中的问题来了解进展情况。

3. 开始使用 Spring AI 项目

    首先，AiClient 需要 API 密钥才能与 OpenAI 平台进行通信。为此，我们将在 API 密钥页面上创建一个[令牌](https://platform.openai.com/account/api-keys)。

    Spring AI 项目定义了配置属性 spring.ai.openai.api-key。我们可以在 application.yml 文件中进行设置：

    ```yaml
    spring:
    ai:
        openai.api-key: ${OPEN_AI_KEY}
    ```

    下一步是配置依赖库。Spring AI 项目在 Spring Milestone Repository 中提供了工件。

    因此，我们需要添加版本库定义：

    ```xml
    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>https://repo.spring.io/snapshot</url>
            <releases>
                <enabled>false</enabled>
            </releases>
        </repository>
    </repositories>
    ```

    之后，我们就可以导入 open-ai-spring-boot-starter了：

    ```xml
    <dependency>
        <groupId>org.springframework.experimental.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>0.7.1-SNAPSHOT</version>
    </dependency>
    ```

    请记住，Spring AI 项目正在积极发展，因此请查看官方 GitHub 页面以获取最新版本。

    现在，让我们将这一概念付诸实践。

4. Spring AI 实践

    让我们编写一个简单的 REST API 进行演示。它由两个端点组成，可以返回我们想要的任何主题和流派的诗歌：

    - /ai/cathaiku - 将实现基本的 generate() 方法，并返回一个包含有关猫的俳句的纯字符串值。
    - `/ai/poetry?theme={{theme}}&genre={{genre}}` - 将演示 PromtTemplate 和 AiResponse 类的功能

    1. 在 Spring Boot 应用程序中注入 AiClient

        为了简单起见，让我们从猫俳句端点开始。通过 @RestController 注解，我们将设置 PoetryController 并添加 GET 方法映射：

        ![PoetryController.java](/src/main/java/com/baeldung/springai/web/PoetryController.java)

        接下来，按照 DDD 概念，服务层将定义所有领域逻辑。调用 generate() 方法时，我们只需将 AiClient 注入 PoetryService。现在，我们可以定义字符串提示，并在其中指定生成俳句的请求：

        ![PoetryServiceImpl.java](/src/main/java/com/baeldung/springai/service/impl/PoetryServiceImpl.java)

        端点已启动并准备好接收请求。响应将包含一个纯字符串：

        ```txt
        Cat prowls in the night,
        Whiskers twitch with keen delight,
        Silent hunter's might.
        ```

        目前看来效果不错，但目前的解决方案存在一些缺陷。首先，纯字符串响应并不是 REST 合约的最佳解决方案。

        此外，一直用相同的提示来查询 ChatGPT 也没有太大价值。因此，我们的下一步是添加参数值：主题和流派。这时，PromtTemplate 将为我们提供最佳服务。

    2. 使用 PromptTemplate 配置查询

        从本质上讲，PromptTemplate 的工作方式与 StringBuilder 和 dictionary 的组合非常相似。与 /cathaiku 端点类似，我们首先要定义提示的基本字符串。相比之下，这次我们将通过名称来定义用实际值填充的占位符：

        ```java
        String promptString = """
            Write me {genre} poetry about {theme}
            """;
        PromptTemplate promptTemplate = new PromptTemplate(promptString);
        promptTemplate.add("genre", genre);
        promptTemplate.add("theme", theme);
        ```

        接下来，我们可能要对端点输出进行标准化。为此，我们将引入[简单记录类](https://www.baeldung.com/java-record-keyword) PoetryDto，它将包含诗歌标题、名称和流派：

        `public record PoetryDto (String title, String poetry, String genre, String theme){}`

        下一步是在 BeanOutputParser 类中注册 PoetryDto；它提供了序列化和反序列化 OpenAI API 输出的功能。

        然后，我们将把该解析器提供给 promtTemple，从现在起，我们的消息将被序列化到 DTO 对象中。

        最后，我们的生成函数将如下所示：

        PoetryServiceImpl.java/getPoetryByGenreAndTheme()

        ```java
        @Override
        public PoetryDto getPoetryByGenreAndTheme(String genre, String theme) {
            BeanOutputParser<PoetryDto> poetryDtoBeanOutputParser = new BeanOutputParser<>(PoetryDto.class);

            String promptString = """
                Write me {genre} poetry about {theme}
                {format}
            """;

            PromptTemplate promptTemplate = new PromptTemplate(promptString);
            promptTemplate.add("genre", genre);
            promptTemplate.add("theme", theme);
            promptTemplate.add("format", poetryDtoBeanOutputParser.getFormat());
            promptTemplate.setOutputParser(poetryDtoBeanOutputParser);

            AiResponse response = aiClient.generate(promptTemplate.create());

            return poetryDtoBeanOutputParser.parse(response.getGeneration().getText());
        }
        ```

        现在，客户端收到的响应看起来好多了，更重要的是，它符合 REST API 标准和最佳实践：

        ```txt
        {
            "title": "Dancing Flames",
            "poetry": "In the depths of night, flames dance with grace,
            Their golden tongues lick the air with fiery embrace.
            A symphony of warmth, a mesmerizing sight,
            In their flickering glow, shadows take flight.
            Oh, flames so vibrant, so full of life,
            Burning with passion, banishing all strife.
            They consume with ardor, yet do not destroy,
            A paradox of power, a delicate ploy.
            They whisper secrets, untold and untamed,
            Their radiant hues, a kaleidoscope unnamed.
            In their gentle crackling, stories unfold,
            Of ancient tales and legends untold.
            Flames ignite the heart, awakening desire,
            They fuel the soul, setting it on fire.
            With every flicker, they kindle a spark,
            Guiding us through the darkness, lighting up the dark.
            So let us gather 'round, bask in their warm embrace,
            For in the realm of flames, magic finds its place.
            In their ethereal dance, we find solace and release,
            And in their eternal glow, our spirits find peace.",
            "genre": "Liric",
            "theme": "Flames"
        }
        ```

5. 错误处理

    Spring AI 项目通过 OpenAiHttpException 类为 OpenAPI 错误提供了一个抽象。遗憾的是，它没有为每种错误类型提供单独的类映射。不过，有了这种抽象，我们就能在一个处理程序中使用 RestControllerAdvice 处理所有异常。

    下面的代码使用了 Spring 6 框架的 ProblemDetail 标准。要进一步熟悉该标准，请查看官方[文档](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)：

    ![ExceptionTranslator.java](/src/main/java/com/baeldung/springai/web/ExceptionTranslator.java)

    现在，如果 OpenAPI 响应包含错误，我们将进行处理：

    ```txt
    {
        "type": "about:blank",
        "title": "Open AI client raised exception",
        "status": 401,
        "detail": "Incorrect API key provided: sk-XG6GW***************************************wlmi. 
        You can find your API key at https://platform.openai.com/account/api-keys.",
        "instance": "/ai/cathaiku"
    }
    ```

    可能出现的异常状态的完整列表请参见官方[文档](https://platform.openai.com/docs/guides/error-codes/api-errors)页面。

6. 结论

    在本文中，我们熟悉了 Spring AI 项目及其在 REST API 方面的功能。尽管在撰写本文时，spring-ai-starter 仍在积极开发中，并且只能访问快照版本，但它为生成式人工智能集成到 Spring Boot 应用程序中提供了一个可靠的接口。

    在本文中，我们介绍了与 Spring AI 的基本集成和高级集成，包括 AiClient 如何在引擎盖下工作。作为概念验证，我们实现了一个生成诗歌的基本 REST 应用程序。除了生成端点的基本示例，我们还提供了一个使用 Spring AI 高级功能的示例： PromtTemplate、AiResponse 和 BeanOutputParser。此外，我们还实现了错误处理功能。
