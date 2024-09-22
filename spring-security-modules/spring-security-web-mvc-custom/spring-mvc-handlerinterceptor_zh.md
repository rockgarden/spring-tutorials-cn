# [Spring MVC处理程序拦截器简介](https://www.baeldung.com/spring-mvc-handlerinterceptor)

1. 一览表

    在本教程中，我们将专注于了解Spring MVC HandlerInterceptor以及如何正确使用它。

2. Spring MVC处理器

    为了了解Spring拦截器的工作原理，让我们退后一步，看看HandlerMapping。

    HandlerMapping的目的是将处理程序方法映射到URL。这样，DispatcherServlet将能够在处理请求时调用它。

    事实上，DispatcherServlet使用HandlerAdapter来实际调用该方法。

    简而言之，拦截器拦截并处理请求。它们有助于避免重复的处理程序代码，如日志记录和授权检查。

    既然我们了解了整体背景，让我们看看如何使用HandlerInterceptor执行一些预处理和后处理操作。

3. Maven附属机构

    为了使用拦截器，我们需要在pom.xml中包含spring-web依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>6.1.9</version>
    </dependency>
    ```

4. Spring处理拦截器

    简单地说，Spring拦截器是一个扩展HandlerInterceptorAdapter类或实现HandlerInterceptor接口的类。

    HandlerInterceptor包含三种主要方法：

    - prehandle（）-在实际处理程序执行之前调用
    - postHandle() – 处理程序执行后调用
    - afterCompletion（）-在完成请求并生成视图后调用

    这三种方法为进行各种预处理和后处理提供了灵活性。

    在我们继续之前，一个简短的说明：要跳过理论并直接跳到示例，请直接跳到第5节。

    这是一个简单的preHandle（）实现：

    ```java
    @Override
    public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler) throws Exception {
        // your code
        return true;
    }
    ```

    请注意，该方法返回一个布尔值。它告诉Spring进一步处理请求（真）或不（假）。

    接下来，我们有一个postHandle（）的实现：

    ```java
    @Override
    public void postHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    ModelAndView modelAndView) throws Exception {
        // your code
    }
    ```

    拦截器在处理请求后立即调用此方法，但在生成视图之前。

    例如，我们可能会使用此方法将登录用户的头像添加到模型中。

    我们需要实现的最后一个方法是afterCompletion（）：

    ```java
    @Override
    public void afterCompletion(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler, Exception ex) {
        // your code
    }
    ```

    此方法允许我们在完成请求处理后执行自定义逻辑。

    此外，值得一提的是，我们可以注册多个自定义拦截器。为此，我们可以使用[DefaultAnnotationHandlerMapping](https://docs.spring.io/spring-framework/docs/4.3.7.RELEASE_to_4.3.8.RELEASE/Spring%20Framework%204.3.8.RELEASE/org/springframework/web/servlet/mvc/annotation/DefaultAnnotationHandlerMapping.html)。

5. 自定义记录器拦截器

    在本例中，我们将专注于登录我们的Web应用程序。

    首先，我们的班级需要实现HandlerInterceptor：

    ```java
    public class LoggerInterceptor implements HandlerInterceptor {
        ...
    }
    ```

    我们还需要在拦截器中启用日志记录：

    `private static Logger log = LoggerFactory.getLogger(LoggerInterceptor.class);`

    这允许Log4J显示日志，并指示哪个类当前正在将信息记录到指定输出中。

    接下来，让我们专注于我们的自定义拦截器实现。

    1. preHandle（）方法

        顾名思义，拦截器在处理请求之前会调用preHandle（）。

        默认情况下，此方法返回true，将请求进一步发送到处理程序方法。然而，我们可以通过返回false来告诉Spring停止执行。

        我们可以使用钩子来记录有关请求参数的信息，例如请求的来自哪里。

        在我们的示例中，我们使用一个简单的Log4J记录器来记录此信息：

        ```java
        @Override
        public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws Exception {
            log.info("[preHandle][" + request + "]" + "[" + request.getMethod()
            + "]" + request.getRequestURI() + getParameters(request));
            return true;
        }
        ```

        正如我们所看到的，我们正在记录一些关于请求的基本信息。

        当然，如果我们在这里遇到密码，我们需要确保我们没有记录它。一个简单的选择是用星星替换密码和任何其他敏感类型的数据。

        以下是如何做到这一点的快速实施：

        ```java
        private String getParameters(HttpServletRequest request) {
            StringBuffer posted = new StringBuffer();
            Enumeration<?> e = request.getParameterNames();
            if (e != null) {
                posted.append("?");
            }
            while (e.hasMoreElements()) {
                if (posted.length() > 1) {
                    posted.append("&");
                }
                String curr = (String) e.nextElement();
                posted.append(curr + "=");
                if (curr.contains("password")
                || curr.contains("pass")
                || curr.contains("pwd")) {
                    posted.append("*****");
                } else {
                    posted.append(request.getParameter(curr));
                }
            }
            String ip = request.getHeader("X-FORWARDED-FOR");
            String ipAddr = (ip == null) ? getRemoteAddr(request) : ip;
            if (ipAddr!=null && !ipAddr.equals("")) {
                posted.append("&_psip=" + ipAddr);
            }
            return posted.toString();
        }
        ```

        最后，我们的目标是获取HTTP请求的源IP地址。

        这里有一个简单的实现：

        ```java
        private String getRemoteAddr(HttpServletRequest request) {
            String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
            if (ipFromHeader != null && ipFromHeader.length() > 0) {
                log.debug("ip from proxy - X-FORWARDED-FOR : " + ipFromHeader);
                return ipFromHeader;
            }
            return request.getRemoteAddr();
        }
        ```

    2. postHandle（）方法

        拦截器在处理程序执行后，但在DispatcherServlet渲染视图之前调用此方法。

        我们可以用它来为ModelAndView添加其他属性。另一个用例是计算请求的处理时间。

        就我们而言，我们只需在DispatcherServlet渲染视图之前记录我们的请求：

        ```java
        @Override
        public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView modelAndView) throws Exception {
            log.info("[postHandle][" + request + "]");
        }
        ```

    3. afterCompletion（）方法

        视图渲染后，我们可以使用此方法获取请求和响应数据：

        ```java
        @Override
        public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
            if (ex != null){
                ex.printStackTrace();
            }
            log.info("[afterCompletion][" + request + "][exception: " + ex + "]");
        }
        ```

6. 配置

    既然我们已经把所有碎片(pieces)放在一起了，让我们添加我们的自定义拦截器。

    要做到这一点，我们需要覆盖addInterceptors()方法：

    ```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggerInterceptor());
    }
    ```

    我们可以通过编辑XML Spring配置文件来实现相同的配置：

    ```xml
    <mvc:interceptors>
        <bean id="loggerInterceptor" class="com.baeldung.web.interceptor.LoggerInterceptor"/>
    </mvc:interceptors>
    ```

    当此配置处于活动状态时，拦截器将处于活动状态，应用程序中的所有请求都将被正确记录。

    请注意，如果配置了多个Spring拦截器，则preHandle()方法按配置顺序执行，而postHandle()和afterCompletion()方法则按相反顺序调用。

    请记住，如果我们使用Spring Boot而不是香草Spring，我们不需要用@EnableWebMvc注释我们的配置类。

7. 结论

    本文简要介绍了使用Spring MVC处理程序拦截器拦截HTTP请求。
