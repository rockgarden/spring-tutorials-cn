# [Spring Boot：自定义whitelabel错误页面](https://www.baeldung.com/spring-boot-custom-error-page)

1. 一览表

    在本教程中，我们将学习如何禁用和自定义Spring Boot应用程序的默认错误页面，因为适当的错误处理描绘了专业性和高质量的工作。

2. 禁用whitelabel错误页面

    首先，我们将了解如何通过将server.error.whitelabel.enabled属性设置为false来完全禁用白标错误页面：

    `server.error.whitelabel.enabled=false`

    将此条目添加到application.properties文件中将禁用错误页面，并显示来自底层应用程序容器的简洁页面，例如Tomcat。

    我们可以通过排除ErrorMvcAutoConfiguration bean来实现相同的结果。我们可以通过将此条目添加到属性文件中来做到这一点：

    ```properties
    spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration

    #for Spring Boot 2.0
    #spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
    ```

    或者我们可以将此注释添加到主类中：

    `@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})`

    上面提到的所有方法都将禁用whitelabel错误页面。这给我们留下了一个问题，那就是谁来处理这个错误？

    嗯，如上所述，它通常是底层应用程序容器。好消息是，我们可以通过显示自定义错误页面来进一步自定义内容，而不是所有默认值。这是下一节的重点。

3. 显示自定义错误页面

    我们首先需要创建一个自定义的HTML错误页面。

    由于我们使用的是Thymeleaf模板引擎，我们将将文件保存为error.html：

    ```jsp
    <!DOCTYPE html>
    <html>
    <body>
    <h1>Something went wrong! </h1>
    <h2>Our Engineers are on it</h2>
    <a href="/">Go Home</a>
    </body>
    </html>
    ```

    如果我们将此文件保存在资源/模板目录中，它将被默认的Spring Boot的BasicErrorController自动拾取。

    这就是我们显示自定义错误页面所需要的一切。通过一些样式，我们现在将为我们的用户提供一个看起来更漂亮的错误页面。

    我们还可以通过使用我们希望它使用的HTTP状态代码来命名文件，以更具体性；例如，在资源/模板/错误中将文件保存为404.html意味着它将明确用于404错误。

    1. 自定义错误控制器

        到目前为止，限制在错误发生时，我们无法运行自定义逻辑。为了实现这一点，我们必须创建一个错误控制器bean来替换默认的错误控制器bean。

        为此，我们必须创建一个实现ErrorController接口的类。此外，我们需要设置server.error.path属性，以返回一个自定义路径，以便在出现错误时调用：

        ```java
        @Controller
        public class MyErrorController implements ErrorController  {
            @RequestMapping("/error")
            public String handleError() {
                //do something like logging
                return "error";
            }
        }
        ```

        在上面的片段中，我们还用@Controller对类进行了注释，并为指定为属性server.error.path的路径创建了映射：

        `server.error.path=/error`

        通过这种方式，控制器可以处理对/错误路径的调用。

        在handleError()中，我们将返回我们之前创建的自定义错误页面。如果我们现在触发404错误，将显示我们的自定义页面。

        让我们进一步增强handleError()，以显示不同错误类型的特定错误页面。

        例如，我们可以为404和500错误类型专门设计好的页面。然后，我们可以使用错误的HTTP状态代码来确定要显示的合适错误页面：

        ```java
        @RequestMapping("/error")
        public String handleError(HttpServletRequest request) {
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

            if (status != null) {
                Integer statusCode = Integer.valueOf(status.toString());
            
                if(statusCode == HttpStatus.NOT_FOUND.value()) {
                    return "error-404";
                }
                else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    return "error-500";
                }
            }
            return "error";
        }
        ```

        例如，对于404错误，我们将看到error-404.html页面。

4. 结论

    有了这些信息，我们现在可以更优雅地处理错误，并向用户展示一个美观的页面。
