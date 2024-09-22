# [带有Spring MVC的自定义错误页面](https://www.baeldung.com/custom-error-page-spring-mvc)

1. 一览表

    任何Web应用程序的一个常见要求都是自定义错误页面。

    例如，假设您正在Tomcat上运行一个香草Spring MVC应用程序。用户在浏览器中输入无效的URL，并显示一个不那么用户友好的蓝白堆栈跟踪——不理想。

    在本教程中，我们将为一些HTTP错误代码设置自定义错误页面。

    工作假设是，读者对Spring MVC工作相当自如。

    本文聚焦于Spring MVC。我们的文章“[自定义白标错误页面](https://www.baeldung.com/spring-boot-custom-error-page)”描述了如何在Spring Boot中创建自定义错误页面。

2. 简单的步骤

    让我们从我们将要遵循的简单步骤开始：

    - 在web.xml中指定单个 URL /errors，该 URL /errors 映射到每次产生错误时都会处理错误的方法
    - 创建一个名为ErrorController的控制器，并带有mapping /errors
    - 在运行时找出HTTP错误代码，并根据HTTP错误代码显示消息。例如，如果生成了404错误，那么用户应该会看到“Resource not found”这样的消息，而对于500错误，用户应该会看到“Sorry! An Internal Server Error was generated at our end”

3. web.xml

    我们首先将以下行添加到我们的web.xml中：

    ```jsp
    <error-page>
        <location>/errors</location>
    </error-page>
    ```

    请注意，此功能仅在大于3.0的Servlet版本中可用。

    应用程序中生成的任何错误都与HTTP错误代码相关联。例如，假设用户在浏览器中输入URL /invalidUrl，但在Spring中没有定义这样的RequestMapping。然后，由底层Web服务器生成的404 HTTP代码。我们刚刚添加到web.xml中的行告诉Spring执行映射到URL/errors的方法中编写的逻辑。

    这里有一个简短的旁注——不幸的是，相应的Java Servlet配置没有用于设置错误页面的API——因此在这种情况下，我们实际上需要web.xml。

4. 控制者

    继续，我们现在创建我们的ErrorController。我们创建了一种统一方法，可以拦截错误并显示错误页面：

    ```java
    @Controller
    public class ErrorController {
        @RequestMapping(value = "errors", method = RequestMethod.GET)
        public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
            ModelAndView errorPage = new ModelAndView("errorPage");
            String errorMsg = "";
            int httpErrorCode = getErrorCode(httpRequest);

            switch (httpErrorCode) {
                case 400: {
                    errorMsg = "Http Error Code: 400. Bad Request";
                    break;
                }
                case 401: {
                    errorMsg = "Http Error Code: 401. Unauthorized";
                    break;
                }
                case 404: {
                    errorMsg = "Http Error Code: 404. Resource not found";
                    break;
                }
                case 500: {
                    errorMsg = "Http Error Code: 500. Internal Server Error";
                    break;
                }
            }
            errorPage.addObject("errorMsg", errorMsg);
            return errorPage;
        }
        
        private int getErrorCode(HttpServletRequest httpRequest) {
            return (Integer) httpRequest
            .getAttribute("javax.servlet.error.status_code");
        }
    }
    ```

5. 前端

    出于演示目的，我们将保持错误页面非常简单和紧凑。此页面将仅包含在白屏上显示的消息。创建一个名为errorPage.jsp的jsp文件：

    ```jsp
    <%@ taglib uri="<http://java.sun.com/jsp/jstl/core>" prefix="c"%>
    <%@ page session="false"%>
    <html>
    <head>
        <title>Home</title>
    </head>
    <body>
        <h1>${errorMsg}</h1>
    </body>
    </html>
    ```

6. 测试

    我们将模拟任何应用程序中最常见的两个错误：HTTP 404错误和HTTP 500错误。

    运行服务器并前往localhost:8080/spring-mvc-xml/invalidUrl。由于此URL不存在，我们期望看到带有消息“Http Error Code : 404. Resource not found”。

    让我们看看当其中一个处理程序方法抛出NullPointerException时会发生什么。我们在ErrorController中添加了以下方法：

    ```java
    @RequestMapping(value = "500Error", method = RequestMethod.GET)
    public void throwRuntimeException() {
        throw new NullPointerException("Throwing a null pointer exception");
    }
    ```

    转到localhost:8080/spring-mvc-xml/500Error。您应该会看到一个带有“Http Error Code : 500. Internal Server Error”。

7. 结论

    我们看到了如何使用Spring MVC为不同的HTTP代码设置错误页面。我们创建了一个错误页面，根据HTTP错误代码动态显示错误消息。
