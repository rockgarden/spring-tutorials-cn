# [Servlet 3异步支持Spring MVC和Spring Security](https://www.baeldung.com/spring-mvc-async-security)

1. 介绍

    在本快速教程中，我们将重点介绍Servlet 3对异步请求的支持，以及Spring MVC和Spring Security如何处理这些请求。

    Web应用程序中异步性最基本的动机是处理长期运行的请求。在大多数用例中，我们需要确保Spring Security主体被传播到这些线程。

    当然，Spring Security在MVC范围之外与@Async集成，并处理HTTP请求。

2. Maven附属机构

    为了在Spring MVC中使用异步集成，我们需要将以下依赖项包含在ourpom.xml中：

    ```xml
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-web</artifactId>
        <version>6.1.5</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-config</artifactId>
        <version>6.1.5</version>
    </dependency>
    ```

3. SpringMVC和@Async

    根据官方[文档](https://spring.io/blog/2012/12/17/spring-security-3-2-m1-highlights-servlet-3-api-support/#servlet3-async)，Spring Security与[WebAsyncManager](http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/context/request/async/WebAsyncManager.html)集成。

    第一步是确保我们的springSecurityFilterChain被设置为处理异步请求。我们可以在Java配置中完成，通过将以下行添加到我们的Servlet配置类：

    `dispatcher.setAsyncSupported(true);`

    或者在XML配置中：

    ```xml
    <filter>
        <filter-name>springSecurityFilterChain</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
        <async-supported>true</async-supported>
    </filter>
    <filter-mapping>
        <filter-name>springSecurityFilterChain</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>ASYNC</dispatcher>
    </filter-mapping>
    ```

    我们还需要在servlet配置中启用异步支持的参数：

    ```xml
    <servlet>
        ...
        <async-supported>true</async-supported>
        ...
    </servlet>
    ```

    现在，我们准备发送带有SecurityContext的异步请求。

    Spring Security中的内部机制将确保在另一个线程中提交响应导致用户注销时，我们的SecurityContext不再被清除。

4. 用例

    让我们用一个简单的例子来看看这个在行动中：

    ```java
    @Override
    public Callable<Boolean> checkIfPrincipalPropagated() {
        Object before 
        = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Before new thread: " + before);

        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Object after 
                = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                log.info("New thread: " + after);
                return before == after;
            }
        };
    }
    ```

    我们想检查Spring SecurityContext是否被传播到新线程。

    如日志所示，上述方法将自动执行其可调用，并包含SecurityContext：

    ```log
    web - 2017-01-02 10:42:19,011 [http-nio-8081-exec-3] INFO
    o.baeldung.web.service.AsyncService - Before new thread:
    org.springframework.security.core.userdetails.User@76507e51:
    Username: temporary; Password: [PROTECTED]; Enabled: true;
    AccountNonExpired: true; credentialsNonExpired: true;
    AccountNonLocked: true; Granted Authorities: ROLE_ADMIN

    web - 2017-01-02 10:42:19,020 [MvcAsync1] INFO
    o.baeldung.web.service.AsyncService - New thread:
    org.springframework.security.core.userdetails.User@76507e51:
    Username: temporary; Password: [PROTECTED]; Enabled: true;
    AccountNonExpired: true; credentialsNonExpired: true;
    AccountNonLocked: true; Granted Authorities: ROLE_ADMIN
    ```

    如果不设置要传播的SecurityContext，第二个请求将以空值结束。

    还有其他重要的用例，可以将异步请求与传播的SecurityContext一起使用：

    - 我们想发出多个可以并行运行的外部请求，可能需要相当多的时间才能执行
    - 我们在本地有一些重要的处理要做，我们的外部请求可以与此并行执行
    - 其他代表着火和忘记的场景，例如发送电子邮件

    请注意，如果我们的多个方法调用以前以同步方式链在一起，那么将这些方法转换为异步方法可能需要同步结果。

5. 结论

    在这个简短的教程中，我们说明了在经过身份验证的上下文中处理异步请求的Spring支持。

    从编程模型的角度来看，新功能看起来非常简单。但肯定有一些方面确实需要更深入的了解。
