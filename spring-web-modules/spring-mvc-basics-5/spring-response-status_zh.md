# [使用 Spring @ResponseStatus 设置 HTTP 状态代码](https://www.baeldung.com/spring-response-status)

1. 简介

    在 Spring MVC 中，我们有很多方法来设置 HTTP 响应的状态代码。

    在这个简短的教程中，我们将看到最直接的方法：使用 @ResponseStatus 注解。

2. 控制器方法

    当端点成功返回时，Spring 会提供 HTTP 200（OK）响应。

    如果我们想指定控制器方法的响应状态，可以使用 @ResponseStatus 来标记该方法。对于所需的响应状态，它有两个可互换的参数：代码(code)和值(value)。例如，[我们可以表示服务器拒绝冲泡咖啡，因为它是一个茶壶](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/418)：

    ```java
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    void teaPot() {}
    ```

    当我们想提示错误时，可以通过原因参数提供错误信息：

    ```java
    @ResponseStatus(HttpStatus.BAD_REQUEST, reason = "Some parameters are invalid")
    void onIllegalArgumentException(IllegalArgumentException exception) {}
    ```

    注意，当我们设置 reason 时，Spring 会调用 HttpServletResponse.sendError()。因此，它将向客户端发送 HTML 错误页面，这使得它不适合 REST 端点。

    另外请注意，Spring 仅在标记的方法成功完成（未抛出异常）时才使用 @ResponseStatus。

3. 使用错误处理程序

    我们有三种方法使用 @ResponseStatus 将异常转换为 HTTP 响应状态：

    - 使用 @ExceptionHandler
    - 使用 @ControllerAdvice
    - 标记异常类

    要使用前两种解决方案，我们必须定义一个错误处理程序方法。您可以在这篇[文章](https://www.baeldung.com/exception-handling-for-rest-with-spring)中阅读有关此主题的更多信息。

    我们可以使用 @ResponseStatus 来处理这些错误处理程序方法，就像上一节中使用常规 MVC 方法一样。

    当我们不需要动态错误响应时，最直接的解决方案是第三种：用 @ResponseStatus 标记异常类：

    ```java
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    class CustomException extends RuntimeException {}
    ```

    当 Spring 捕捉异常时，它会使用我们在 @ResponseStatus 中提供的设置。

    请注意，当我们用 @ResponseStatus 标记异常类时，无论是否设置了原因，Spring 都会调用 HttpServletResponse.sendError()。

    还要注意的是，Spring 会对子类使用相同的配置，除非我们也用 @ResponseStatus 标记它们。

4. 总结

    在本文中，我们了解了如何使用 @ResponseStatus 来设置不同场景下的 HTTP 响应代码，包括错误处理。
