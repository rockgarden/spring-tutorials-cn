# [使用自定义Spring MVC的处理程序拦截器来管理会话](https://www.baeldung.com/spring-mvc-custom-handler-interceptor)

1. 介绍

    在本教程中，我们将重点介绍Spring MVC HandlerInterceptor。

    更具体地说，我们将展示使用拦截器的更高级用例——我们将通过设置自定义计数器和手动跟踪会话来模拟会话超时逻辑。

    如果您想在Spring中阅读HandlerInterceptor的基础知识，请查看[本文](./spring-mvc-handlerinterceptor_zh.md)。

2. Maven附属机构

    要使用拦截器，您需要在pom.xml文件的依赖项部分中包含以下部分：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>5.3.13</version>
    </dependency>
    ```

    此依赖项仅涵盖Spring Web，因此不要忘记为完整（最小）Web应用程序添加spring-core和spring-context。

3. 会话超时的自定义实现

    在本例中，我们将为系统中的用户配置最大非活动时间。在那之后，他们将自动从应用程序中注销。

    这个逻辑只是一个概念的证明——我们当然可以使用会话超时轻松地实现相同的结果——但结果不是这里的重点，拦截器的使用是重点。

    因此，我们希望确保如果用户不活跃，会话将失效。例如，如果用户忘记注销，非活动时间计数器将阻止未经授权的用户访问该帐户。为了做到这一点，我们需要为非活动时间设置常数：

    `private static final long MAX_INACTIVE_SESSION_TIME = 5 * 10000;`

    出于测试目的，我们将其设置为50秒；不要忘记，它以毫秒为单位计算。

    现在，我们需要在应用程序中跟踪每个会话，因此我们需要包含此Spring接口：

    ```java
    @Autowired
    private HttpSession session;
    ```

    让我们继续使用preHandle()方法。

    1. preHandle()

        在此方法中，我们将包括以下操作：

        - 设置计时器来检查请求的处理时间
        - 检查用户是否已登录（使用本文中的UserInterceptor方法）
        - 如果用户的非活动会话时间超过最大允许值，则自动注销

        让我们来看看实施情况：

        ```java
        @Override
        public boolean preHandle(
        HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
            log.info("Pre handle method - check handling start time");
            long startTime = System.currentTimeMillis();
            request.setAttribute("executionTime", startTime);
        }
        ```

        在代码的这一部分，我们设置了处理执行的开始时间。从这一刻起，我们将计算几秒数来完成每个请求的处理。在下一部分中，我们将提供会话时间的逻辑，前提是有人在HTTP会话期间登录：

        ```java
        if (UserInterceptor.isUserLogged()) {
            session = request.getSession();
            log.info("Time since last request in this session: {} ms",
            System.currentTimeMillis() - request.getSession().getLastAccessedTime());
            if (System.currentTimeMillis() - session.getLastAccessedTime()
            > MAX_INACTIVE_SESSION_TIME) {
                log.warn("Logging out, due to inactive session");
                SecurityContextHolder.clearContext();
                request.logout();
                response.sendRedirect("/spring-rest-full/logout");
            }
        }
        return true;
        ```

        首先，我们需要从请求中获取会话。

        接下来，我们做一些控制台日志记录，关于谁登录了，以及自用户在我们的应用程序中执行任何操作以来已经过了多长时间。我们可能会使用session.getLastAccessedTime()来获取此信息，从当前时间中减去它，并与我们的MAX_INACTIVE_SESSION_TIME进行比较。

        如果时间比我们允许的长，我们会清除上下文，注销请求，然后（可选）发送重定向作为对默认注销视图的响应，该视图在Spring Security配置文件中声明。

        为了完成处理时间示例的计数器，我们还实现了postHandle()方法，该方法在下一个小节中进行了描述。

    2. postHandle()

        此方法的实现只是为了获取信息，处理当前请求需要多长时间。正如您在上一个代码片段中看到的，我们在Spring模型中设置了执行时间。现在是时候使用它了：

        ```java
        @Override
        public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView model) throws Exception {
            log.info("Post handle method - check execution time of handling");
            long startTime = (Long) request.getAttribute("executionTime");
            log.info("Execution time for handling the request was: {} ms",
            System.currentTimeMillis() - startTime);
        }
        ```

        实现很简单——我们检查执行时间，并从当前系统时间中减去它。只要记得将模型的值铸成长。

        现在我们可以正确地记录执行时间。

4. 拦截器的配置

    要将新创建的Interceptor添加到Spring配置中，我们需要在实现WebMvcConfigurer的WebConfig类中覆盖addInterceptors()方法：

    ```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionTimerInterceptor());
    }
    ```

    我们可以通过编辑XML Spring配置文件来实现相同的配置：

    ```xml
    <mvc:interceptors>
        <bean id="sessionTimerInterceptor" class="com.baeldung.web.interceptor.SessionTimerInterceptor"/>
    </mvc:interceptors>
    ```

    此外，我们需要添加侦听器，以便自动创建ApplicationContext：

    ```java
    public class ListenerConfig implements WebApplicationInitializer {
        @Override
        public void onStartup(ServletContext sc) throws ServletException {
            sc.addListener(new RequestContextListener());
        }
    }
    ```

5. 结论

    本教程展示了如何使用Spring MVC的HandlerInterceptor拦截Web请求，以便手动进行会话管理/超时。
