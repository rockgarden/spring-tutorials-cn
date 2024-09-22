# [调试Spring MVC 404"没有找到HTTP请求的映射"错误](https://www.baeldung.com/spring-mvc-404-error)

1. 简介

    Spring MVC是使用Front Controller Pattern构建的传统应用程序。[DispatcherServlet](https://www.baeldung.com/spring-dispatcherservlet)充当前端控制器，负责路由和请求处理。

    与任何web应用程序或网站一样，当找不到请求的资源时，SpringMVC会返回HTTP 404响应代码。在本教程中，我们将研究SpringMVC中404错误的常见原因。

2. 404响应的可能原因-错误的URI

    假设我们有一个GreetingController，它映射到/greeting并呈现greeting.jsp：

    ```java
    @Controller
    public class GreetingController {

        @RequestMapping(value = "/greeting", method = RequestMethod.GET)
        public String get(ModelMap model) {
            model.addAttribute("message", "Hello, World!");
            return "greeting";
        }
    }
    ```

    相应的视图呈现消息变量的值：

    ```html
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <html>
        <head>
            <title>Greeting</title>
        </head>
        <body>
            <h2>${message}</h2>
        </body>
    </html>
    ```

    正如预期的那样，向/问候发送GET请求是有效的：

    `curl http://localhost:8080/greeting`

    我们将看到一个带有“Hello World”消息的HTML页面。

    看到404的最常见原因之一是使用了错误的URI。例如，向/greetings而不是/greeting发出GET请求是错误的。

    在这种情况下，我们会在服务器日志中看到一条警告消息：

    ```log
    [http-nio-8080-exec-6] WARN  o.s.web.servlet.PageNotFound - 
    No mapping found for HTTP request with URI [/greetings] in DispatcherServlet with name 'mvc'
    ```

    客户端将看到一个错误页面：

    ```html
    <html>
        <head>
            <title>Home</title>
        </head>
        <body>
            <h1>Http Error Code : 404. Resource not found</h1>
        </body>
    </html>
    ```

    为了避免这种情况，我们需要确保正确输入了URI。

3. 404响应的可能原因-错误的Servlet映射

    如前所述，DispatcherServlet是SpringMVC中的前端控制器。因此，就像在标准的基于servlet的应用程序中一样，我们需要使用web.xml文件为servlet创建映射。

    我们在servlet标记内定义servlet，并将其映射到servlet-mapping标记内的URI。我们需要确保 url-pattern 的值是正确的，因为在servlet映射到“/*”很常见-请注意后面的星号：

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <web-app ...>
        <!-- Additional config omitted -->
        <servlet>
            <servlet-name>mvc</servlet-name>
            <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
            <load-on-startup>1</load-on-startup>
        </servlet>
        <servlet-mapping>
            <servlet-name>mvc</servlet-name>
            <url-pattern>/*</url-pattern>
        </servlet-mapping>
        <!-- Additional config omitted -->
    </web-app>
    ```

    现在，如果我们请求/问候，我们会在服务器日志中看到一条警告：

    ```log
    WARN  o.s.web.servlet.PageNotFound - No mapping found for HTTP request with URI 
    [/WEB-INF/view/greeting.jsp] in DispatcherServlet with name 'mvc'
    ```

    这一次，错误陈述找不到greeting.jsp，用户会看到一个空白页面。

    要修复此错误，我们需要将DispatcherServlet映射到“/”（不带尾随星号）：修复映射后，一切都应该正常工作。请求/问候现在显示消息“你好，世界！”。

    问题背后的原因是，如果我们将DispatcherServlet映射到/*，那么我们会告诉应用程序，到达应用程序的每个请求都将由Dispatcher Servlet提供服务。然而，这不是一种正确的方法，因为DispatcherServlet无法做到这一点。相反，SpringMVC期望ViewResolver的实现为JSP文件等视图提供服务。
