# [使用Spring ResponseEntity来操作HTTP响应](https://www.baeldung.com/spring-response-entity)

1. 介绍

    使用Spring，我们通常有很多方法来实现相同的目标，包括微调HTTP响应。

    在这个简短的教程中，我们将了解如何使用ResponseEntity设置HTTP响应的正文、状态和标题。

2. 响应实体

    ResponseEntity代表整个HTTP响应：状态代码、标头和正文。因此，我们可以用它来完全配置HTTP响应。

    如果我们想使用它，我们必须从端点返回它；Spring会处理剩下的事情。

    ResponseEntity是一种通用类型。因此，我们可以使用任何类型作为响应主体：

    ```java
    @GetMapping("/hello")
    ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello World!", HttpStatus.OK);
    }
    ```

    由于我们以编程方式指定响应状态，我们可以为不同场景返回不同的状态代码：

    ```java
    @GetMapping("/age")
    ResponseEntity<String> age(@RequestParam("yearOfBirth") int yearOfBirth) {
    
        if (isInFuture(yearOfBirth)) {
            return new ResponseEntity<>(
                "Year of birth cannot be in the future", 
                HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
            "Your age is " + calculateAge(yearOfBirth), 
            HttpStatus.OK);
    }
    ```

    此外，我们可以设置HTTP标头：

    ```java
    @GetMapping("/customHeader")
    ResponseEntity<String> customHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Custom-Header", "foo");
            
        return new ResponseEntity<>(
            "Custom header set", headers, HttpStatus.OK);
    }
    ```

    此外，ResponseEntity提供了两个嵌套构建器接口：HeadersBuilder及其子接口BodyBuilder。因此，我们可以通过ResponseEntity的静态方法访问他们的功能。

    最简单的情况是带有正文和HTTP 200响应代码的响应：

    ```java
    @GetMapping("/hello")
    ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World!");
    }
    ```

    对于最流行的[HTTP状态代码](https://www.baeldung.com/cs/http-status-codes)，我们获得静态方法：

    ```java
    BodyBuilder accepted();
    BodyBuilder badRequest();
    BodyBuilder created(java.net.URI location);
    HeadersBuilder<?> noContent();
    HeadersBuilder<?> notFound();
    BodyBuilder ok();
    ```

    此外，我们可以使用BodyBuilder状态（HttpStatus状态）和BodyBuilder状态（int状态）方法来设置任何HTTP状态。

    最后，使用`ResponseEntity<T> BodyBuilder.body(T body)`，我们可以设置HTTP响应主体：

    ```java
    @GetMapping("/age")
    ResponseEntity<String> age(@RequestParam("yearOfBirth") int yearOfBirth) {
        if (isInFuture(yearOfBirth)) {
            return ResponseEntity.badRequest()
                .body("Year of birth cannot be in the future");
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body("Your age is " + calculateAge(yearOfBirth));
    }
    ```

    我们还可以设置自定义标题：

    ```java
    @GetMapping("/customHeader")
    ResponseEntity<String> customHeader() {
        return ResponseEntity.ok()
            .header("Custom-Header", "foo")
            .body("Custom header set");
    }
    ```

    由于BodyBuilder.body()返回ResponseEntity而不是BodyBuilder，它应该是最后一次调用。

    请注意，使用HeaderBuilder，我们无法设置响应主体的任何属性。

    在从控制器返回`ResponseEntity<T>`对象时，我们在处理请求时可能会收到异常或错误，并希望将错误相关信息返回给用户，表示为其他类型，比方说E。

    Spring 3.2通过新的@ControllerAdvice注释支持全局@ExceptionHandler，该注释处理此类场景。有关详细信息，请参阅我们现有的[文章](https://www.baeldung.com/exception-handling-for-rest-with-spring)。

    虽然ResponseEntity非常强大，但我们不应该过度使用它。在简单的情况下，还有其他选项可以满足我们的需求，它们会导致更干净的代码。

3. 备选方案

    1. @ResponseBody

        在经典的Spring MVC应用程序中，端点通常返回渲染的HTML页面。有时我们只需要返回实际数据；例如，当我们使用AJAX的端点时。

        在这种情况下，我们可以用@ResponseBody标记请求处理程序方法，Spring将方法的结果值视为HTTP响应主体本身。

        欲了解更多信息，[本文](https://www.baeldung.com/spring-request-response-body)是一个很好的开始。

    2. @ResponseStatus

        当端点成功返回时，Spring提供HTTP 200（OK）响应。如果端点抛出异常，Spring会寻找一个异常处理程序来告诉要使用哪个HTTP状态。

        我们可以使用@ResponseStatus标记这些方法，因此，Spring返回具有自定义HTTP状态。

        有关更多示例，请访问我们关于[自定义状态代码](https://www.baeldung.com/spring-response-status)的文章。

    3. 直接操作响应

        Spring还允许我们直接访问jakarta.servlet.http.HttpServletResponse对象；我们只需要将其声明为方法参数：

        ```java
        @GetMapping("/manual")
        void manual(HttpServletResponse response) throws IOException {
            response.setHeader("Custom-Header", "foo");
            response.setStatus(200);
            response.getWriter().println("Hello World!");
        }
        ```

        由于Spring提供了基础实现之上的抽象和附加功能，我们不应该以这种方式操作响应。

4. 结论

    在本文中，我们讨论了在Spring中操作HTTP响应的多种方法，并研究了它们的利弊。
