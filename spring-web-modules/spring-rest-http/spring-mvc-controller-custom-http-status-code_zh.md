# [从Spring控制器返回自定义状态代码](https://www.baeldung.com/spring-mvc-controller-custom-http-status-code)

1. 一览表

    这篇快速文章将演示从Spring MVC控制器返回自定义[HTTP状态代码](https://www.baeldung.com/cs/http-status-codes)的几种方法。

    这通常很重要，以便更清楚地表达向客户端请求的结果，并使用HTTP协议的完整丰富语义。例如，如果请求出现问题，为每种可能的问题发送特定的错误代码将允许客户端向用户显示适当的错误消息。

2. 返回自定义状态代码

    Spring提供了从其控制器类中返回自定义状态代码的几种主要方法：

    - 使用响应实体
    - 在异常类上使用@ResponseStatus注释，以及
    - 使用@ControllerAdvice和@ExceptionHandler注释。

    这些选项不是相互排斥的；远非如此，它们实际上可以相辅相成。

    本文将涵盖前两种方式（ResponseEntity和@ResponseStatus）。如果您想了解更多关于使用@ControllerAdvice和@ExceptionHandler的信息，您可以在[这里](https://www.baeldung.com/exception-handling-for-rest-with-spring#controlleradvice)阅读。

    1. 通过响应实体返回状态代码

        在标准的Spring MVC控制器中，我们将定义一个简单的映射：

        ```java
        @RequestMapping(value = "/controller", method = RequestMethod.GET)
        @ResponseBody
        public ResponseEntity sendViaResponseEntity() {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        ```

        收到“/controller”的GET请求后，Spring将返回带有406代码的响应（不可接受）。我们为这个例子任意选择了特定的响应代码。您可以返回任何HTTP状态代码（完整列表可以在[这里](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes)找到）。

    2. 通过异常返回状态代码

        我们将在控制器中添加第二个方法，以演示如何使用异常来返回状态代码：

        ```java
        @RequestMapping(value = "/exception", method = RequestMethod.GET)
        @ResponseBody
        public ResponseEntity sendViaException() {
            throw new ForbiddenException();
        }
        ```

        在收到“/exception”的GET请求后，Spring将抛出一个ForbiddenException。这是一个自定义异常，我们将在单独的类中定义：

        ```java
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public class ForbiddenException extends RuntimeException {}
        ```

        此例外不需要代码。所有工作都由@ResponseStatus注释完成。

        在这种情况下，当抛出异常时，抛出异常的控制器返回响应代码为403（禁止）的响应。如有必要，您还可以在注释中添加一条消息，该消息将与响应一起返回。

        在这种情况下，班级将看起来像这样：

        ```java
        @ResponseStatus(value = HttpStatus.FORBIDDEN, reason="To show an example of a custom message")
        public class ForbiddenException extends RuntimeException {}
        ```

        需要注意的是，虽然从技术上讲，可以让异常返回任何状态代码，但在大多数情况下，对错误代码（4XX和5XX）使用例外只是合乎逻辑的。

3. 结论

    教程展示了如何从Spring MVC控制器返回自定义状态代码。
