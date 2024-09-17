# [Spring的RequestBody和ResponseBody注释](https://www.baeldung.com/spring-request-response-body)

1. 简介

    在本快速教程中，我们将简要介绍 Spring @RequestBody 和 @ResponseBody 注解。

2. @RequestBody

    简单地说，@RequestBody 注解将 HttpRequest 主体映射到传输对象或域对象，从而将入站的 HttpRequest 主体自动反序列化为 Java 对象。

    首先，让我们来看看 Spring 控制器方法：

    ```java
    @PostMapping(“/request”)
    public ResponseEntity postController(@RequestBody LoginForm loginForm) {
        exampleService.fakeAuthenticate(loginForm);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    ```

    假设指定了适当的类型，Spring 会自动将 JSON 反序列化为 Java 类型。

    默认情况下，我们用 @RequestBody 注解注解的类型必须与客户端控制器发送的 JSON 相对应：

    ```java
    public class LoginForm {
        private String username;
        private String password;
        // ...
    }
    ```

    在这里，我们用来表示 HttpRequest 主体的对象映射到了我们的 LoginForm 对象。

    让我们用 CURL 来测试一下：

    ```bash
    curl -i \
    -H "Accept: application/json" \
    -H "Content-Type:application/json" \
    -X POST --data 
        '{"username": "johnny", "password": "password"}' 
        "https://localhost:8080/spring-boot-rest/post/request"
    ```

    这就是我们使用 @RequestBody 注解创建 Spring REST API 和 Angular 客户端所需的全部内容。

3. @ResponseBody

    @ResponseBody 注解告诉控制器，返回的对象将自动序列化为 JSON 并传回 HttpResponse 对象。

    假设我们有一个自定义的 Response 对象：

    ```java
    public class ResponseTransfer {
        private String text;
        // standard getters/setters
    }
    ```

    接下来，就可以实现相关的控制器了：

    ```java
    @Controller
    @RequestMapping("/post")
    public class ExamplePostController {

        @Autowired
        ExampleService exampleService;

        @PostMapping("/response")
        @ResponseBody
        public ResponseTransfer postResponseController(
        @RequestBody LoginForm loginForm) {
            return new ResponseTransfer("Thanks For Posting!!!");
        }
    }
    ```

    在浏览器的开发人员控制台或使用 Postman 等工具，我们可以看到以下响应：

    `{“text”: “Thanks For Posting!!!”}`

    请记住，我们不需要用 @ResponseBody 注解来注解 @RestController 注解的控制器，因为 Spring 默认会这样做。

    1. 设置内容类型

        当我们使用 @ResponseBody 注解时，我们仍然可以显式地设置方法返回的内容类型。

        为此，我们可以使用 @RequestMapping 的 produces 属性。请注意，@PostMapping、@GetMapping 等注解为该参数定义了别名。

        现在让我们添加一个发送 JSON 响应的新端点：

        ```java
        @PostMapping(value = "/content", produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseBody
        public ResponseTransfer postResponseJsonContent(
        @RequestBody LoginForm loginForm) {
            return new ResponseTransfer("JSON Content!");
        }
        ```

        在示例中，我们使用了 MediaType.APPLICATION_JSON_VALUE 常量。或者，我们也可以直接使用 application/json。

        接下来，让我们实现一个新方法，映射到相同的 /content 路径，但返回的是 XML 内容：

        ```java
        @PostMapping(value = "/content", produces = MediaType.APPLICATION_XML_VALUE)
        @ResponseBody
        public ResponseTransfer postResponseXmlContent(
        @RequestBody LoginForm loginForm) {
            return new ResponseTransfer("XML Content!");
        }
        ```

        现在，根据请求头中发送的 Accept 参数值，我们将得到不同的响应。

        让我们来看看实际操作：

        ```bash
        curl -i \ 
        -H "Accept: application/json" \ 
        -H "Content-Type:application/json" \ 
        -X POST --data 
            '{"username": "johnny", "password": "password"}' 
            "https://localhost:8080/spring-boot-rest/post/content"
        ```

        CURL 命令返回 JSON 响应：

        ```bash
        HTTP/1.1 200
        Content-Type: application/json
        Transfer-Encoding: chunked
        Date: Thu, 20 Feb 2020 19:43:06 GMT

        {"text":"JSON Content!"}
        ```

        现在，我们来修改 Accept 参数：

        ```bash
        curl -i \
        -H "Accept: application/xml" \
        -H "Content-Type:application/json" \
        -X POST --data
            '{"username": "johnny", "password": "password"}' 
            "https://localhost:8080/spring-boot-rest/post/content"
        ```

        不出所料，这次我们得到的是 XML 内容：

        ```bash
        HTTP/1.1 200
        Content-Type: application/xml
        Transfer-Encoding: chunked
        Date: Thu, 20 Feb 2020 19:43:19 GMT

        <ResponseTransfer><text>XML Content!</text></ResponseTransfer>
        ```

4. 结论

    我们为 Spring 应用程序构建了一个简单的 Angular 客户端，演示了如何使用 @RequestBody 和 @ResponseBody 注解。

    此外，我们还演示了如何在使用 @ResponseBody 时设置内容类型。
