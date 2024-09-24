# [如何定义Spring Boot过滤器？](https://www.baeldung.com/spring-boot-add-filter)

1. 一览表

    在本快速教程中，我们将探索如何在Spring Boot的帮助下定义自定义过滤器并指定其调用顺序。

2. 定义过滤器和调用顺序

    让我们从创建两个过滤器开始：

    - TransactionFilter – 启动和提交交易
    - RequestResponseLoggingFilter – 记录请求和响应

    为了创建过滤器，我们只需要实现过滤器接口：

    ```java
    @Component
    @Order(1)
    public class TransactionFilter implements Filter {

        @Override
        public void doFilter(
        ServletRequest request, 
        ServletResponse response, 
        FilterChain chain) throws ServletException {
    
            HttpServletRequest req = (HttpServletRequest) request;
            LOG.info(
            "Starting a transaction for req : {}", 
            req.getRequestURI());
    
            chain.doFilter(request, response);
            LOG.info(
            "Committing a transaction for req : {}", 
            req.getRequestURI());
        }

        // other methods 
    }

    @Component
    @Order(2)
    public class RequestResponseLoggingFilter implements Filter {

        @Override
        public void doFilter(
        ServletRequest request, 
        ServletResponse response, 
        FilterChain chain) throws ServletException {
    
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            LOG.info(
            "Logging Request  {} : {}", req.getMethod(), 
            req.getRequestURI());
            chain.doFilter(request, response);
            LOG.info(
            "Logging Response :{}", 
            res.getContentType());
        }

        // other methods
    }
    ```

    为了使Spring识别过滤器，我们需要将其定义为带有@Component注释的bean。

    此外，为了使过滤器按正确的顺序触发，我们需要使用@Order注释。

    1. 使用URL模式进行过滤

        在上述示例中，我们的过滤器默认为我们应用程序中的所有URL注册。然而，我们有时可能希望过滤器仅适用于某些URL模式。

        在这种情况下，我们必须从过滤器类定义中删除@Component注释，并使用FilterRegistrationBean注册过滤器：

        ```java
        @Bean
        public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter(){
            FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean
            = new FilterRegistrationBean<>();

            registrationBean.setFilter(new RequestResponseLoggingFilter());
            registrationBean.addUrlPatterns("/users/*");
            registrationBean.setOrder(2);
                
            return registrationBean;    
        }
        ```

        请注意，在这种情况下，我们需要使用setOrder()方法显式设置顺序。

        现在，过滤器将仅适用于与/users/*模式匹配的路径。

        要为过滤器设置URL模式，我们可以使用addUrlPatterns（）或setUrlPatterns（）方法。

3. 一个快速的例子

    现在让我们创建一个简单的端点，并向它发送HTTP请求：

    ```java
    @RestController
    @RequestMapping("/users")
    public class UserController {
        @GetMapping()
        public List<User> getAllUsers() {
            // ...
        }
    }
    ```

    点击此API的应用程序日志是：

    ```log
    23:54:38 INFO  com.spring.demo.TransactionFilter - Starting Transaction for req :/users
    23:54:38 INFO  c.s.d.RequestResponseLoggingFilter - Logging Request  GET : /users
    ...
    23:54:38 INFO  c.s.d.RequestResponseLoggingFilter - Logging Response :application/json;charset=UTF-8
    23:54:38 INFO  com.spring.demo.TransactionFilter - Committing Transaction for req :/users
    ```

    这确认过滤器是按照所需的顺序调用的。

4. 结论

    在这篇简短的文章中，我们总结了如何在Spring Boot web应用程序中定义自定义过滤器。
