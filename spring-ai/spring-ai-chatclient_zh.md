# [Spring AI中的ChatClient Fluent API](https://www.baeldung.com/spring-ai-chatclient)

1. 一览表

    在本教程中，我们将探索ChatClient的流畅API，这是Spring AI模块版本1.0.0 M1的功能。

    Spring AI模块的ChatClient接口可与AI模型进行通信，允许用户发送提示并接收结构化响应。它遵循构建器模式，提供类似于WebClient、RestClient和JdbcClient的API。

2. 通过ChatClient执行提示

    我们可以在Spring Boot中使用客户端作为自动配置的bean，或以编程方式创建实例。

    首先，让我们将spring-ai-openai-spring-boot-starter依赖项添加到我们的pom.xml中：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
    ```

    有了这个，我们可以将ChatClient.Builder实例注入我们的Spring管理组件中：

    ```java
    @RestController
    @RequestMapping("api/articles")
    class BlogsController {

        private final ChatClient chatClient;
    
        public BlogsController(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        // ...
    }
    ```

    现在，让我们创建一个简单的端点，接受问题作为查询参数，并将提示转发给人工智能：

    ```java
    @GetMapping("v1")
    String askQuestion(@RequestParam(name = "question") String question) {
        return chatClient.prompt()
        .user(question)
        .call()
        .chatResponse()
        .getResult()
        .getOutput()
        .getContent();
    }
    ```

    正如我们所看到的，流畅的ChatClient允许我们从用户的输入字符串中轻松创建提示请求，调用API，并以文本的形式检索响应内容。

    此外，如果我们只对作为字符串的响应主体感兴趣，而不需要状态代码或标题等元数据，我们可以使用content（）方法对最后四个步骤进行分组来简化代码。让我们重构代码并添加此改进：

    ```java
    @GetMapping("v1")
    String askQuestion(@RequestParam(name = "question") String question) {
        return chatClient.prompt()
        .user(question)
        .call()
        .content();
    }
    ```

    如果我们现在发送GET请求，我们将收到一个没有定义结构的响应，类似于通过浏览器访问时ChatGPT的默认输出：

    ```log
    GET http://localhost:8080/api/articles/v1?question=suggest 10 articles for learning Quarkus

    HTTP/1.1 200
    › (Headers) Content-Type: text/plain;charset=UTF-8...

    Certainly! Quarkus is a modern, Kubernetes-native Java framework tailored for GraalVM and OpenJDK HotSpot. Here
        are 10 articles that can help you get started and deepen your understanding of Quarkus:

    1. **Introduction to Quarkus: Supersonic Subatomic Java**:
        - A beginner-friendly article that provides an overview of what Quarkus is, its benefits, and basic setup.

    2. **Getting Started with Quarkus and Creating Your First Application**:
        - Step-by-step guide on setting up a Quarkus project, running a simple application, and understanding the 
            project structure
    ```

3. 映射响应到特定格式

    正如我们所看到的，ChatClient界面简化了将用户查询转发到聊天模型并发送回响应的过程。然而，在大多数情况下，我们希望模型的输出以结构化格式，然后可以序列化为JSON。

    API公开了一个实体（）方法，这允许我们为模型的输出定义特定的数据结构。让我们修改我们的代码，以确保它返回文章对象列表，每个对象都包含一个标题和一组标签：

    ```java
    record Article(String title, Set<String> tags) {
    }

    @GetMapping("v2")
    List<Article> askQuestionAndRetrieveArticles(@RequestParam(name = "question") String question) {
        return chatClient.prompt()
        .user(question)
        .call()
        .entity(new ParameterizedTypeReference<List<Article>>() {});
    }
    ```

    如果我们现在执行请求，我们将期望端点在有效的JSON列表中返回文章建议：

    ```log
    GET http://localhost:8080/api/articles/v2?question=suggest 10 articles for learning Quarkus
        Show Request
    НТТР/1.1 200
        > (Headers) Content-Type: application/json...
    [
        {
        "title": "Getting Started with Quarkus: A Beginner's Guide",
        "tags": [
            "Quarkus",
            "Introduction"
            ]
        },
        {
        "title": "Building Microservices with Quarkus"
        "tags": [
            "Quarkus"
            "Microservices"
            ]
        }
    ]
    ```

4. 提供额外的上下文

    我们已经学习了如何使用Spring AI模块创建提示，将它们发送到AI模型，并接收结构化响应。然而，我们的REST API返回的文章建议是虚构的，在我们的网站上可能不存在。

    为了解决这个问题，ChatClient利用检索增强生成（RAG）模式，将从源的数据检索与生成模型相结合，以提供更准确的响应。我们将使用矢量存储来利用RAG，并加载与我们的用例相关的文档。

    首先，我们将创建一个VectorStore，并在类初始化期间从本地文件加载增强数据：

    ```java
    @RestController
    @RequestMapping("api/articles")
    public class BlogsController {

        private final ChatClient chatClient;
        private final VectorStore vectorStore;

        public BlogsController(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) throws IOException {
            this.chatClient = chatClientBuilder.build();
            this.vectorStore = new SimpleVectorStore(embeddingModel);
            initContext();
        }

        void initContext() throws IOException {
            List<Document> documents = Files.readAllLines(Path.of("src/main/resources/articles.txt"))
            .stream()
            .map(Document::new)
            .toList();

            vectorStore.add(documents);
        }
    
        // ...
    }
    ```

    正如我们所看到的，我们阅读了articles.txt中的所有条目，并为此文件的每一行创建了一个新文档。不用说，我们不必依赖文件——如果需要，我们可以使用任何数据源。

    之后，我们将通过在QuestionAnswerAdvisor中包装VectorStore向模型提供增强数据：

    ```java
    @GetMapping("v3")
    List<Article> askQuestionWithContext(@RequestParam(name = "question") String question) {
        return chatClient.prompt()
        .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
        .user(question)
        .call()
        .entity(new ParameterizedTypeReference<List<Article>>() {});
    }
    ```

    因此，我们的应用程序现在仅从增强上下文中返回数据：

    ```log
    GET http://localhost:8080/api/articles/v3?question=suggest 10 articles for learning Quarkus
    Show Request
    HTTP/1.1 200
    > (Headers) ..Content-Type: application/json..
    [
        {
            "title": "Building Cloud-Native Java Applications with Quarkus",
            "tags": [
                "Java"
                "Cloud-Native",
                "Quarkus"
                ]
        },
    ]
    ```

5. 结论

    在这篇文章中，我们探索了Spring AI的ChatClient。我们首先向模型发送简单的用户查询，并将其响应读取为纯文本。然后，我们通过以特定的结构化格式检索模型的响应来增强我们的解决方案。

    最后，我们学习了如何用一系列文档加载模型的上下文，以根据我们自己的数据提供准确的响应。我们使用VectorStore和QuestionAnswerAdvisor来实现这一点。
