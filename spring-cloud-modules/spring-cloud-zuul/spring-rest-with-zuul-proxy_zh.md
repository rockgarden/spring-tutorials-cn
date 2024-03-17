# 使用 Zuul 代理的 Spring REST

[REST](https://www.baeldung.com/category/rest) [Spring云](../spring-cloud_zh.md)

[代理](https://www.baeldung.com/tag/proxy) [Zuul](https://www.baeldung.com/tag/zuul)

1. 概述

    在本文中，我们将探讨分别部署的前端应用程序与 REST API 之间的通信。

    我们的目标是绕过浏览器的 CORS 和同源策略限制，允许用户界面调用 API，即使它们不共享同一个源。

    基本上，我们将创建两个独立的应用程序--一个用户界面应用程序和一个简单的 REST API，我们将在用户界面应用程序中使用 Zuul 代理来代理对 REST API 的调用。

    Zuul 是 Netflix 基于 JVM 的路由器和服务器端负载平衡器。Spring Cloud 与嵌入式 Zuul 代理进行了很好的集成，我们将在此使用 Zuul 代理。

    进一步阅读：

    - [使用 Zuul 和 Eureka 实现负载平衡的示例](https://www.baeldung.com/zuul-load-balancing)

      了解使用 Netflix Zuul 进行负载平衡的情况。

    - [使用 Springfox 为 Spring REST API 设置 Swagger 2](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)

      了解如何使用 Swagger 2 记录 Spring REST API。

    - [Spring REST 文档介绍](https://www.baeldung.com/spring-rest-docs)

      本文将介绍 Spring REST Docs，这是一种测试驱动机制，用于为 RESTful 服务生成既准确又可读的文档。

2. Maven 配置

    首先，我们需要在 UI 应用程序的 pom.xml 中添加对 Spring Cloud 支持 Zuul 的依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
        <version>2.2.0.RELEASE</version>
    </dependency>
    ```

3. Zuul 属性

    接下来，我们需要配置 Zuul，因为我们使用的是 Spring Boot，所以要在 application.yml 中进行配置：

    ```yml
    zuul:
    routes:
        foos:
        path: /foos/**
        url: http://localhost:8081/spring-zuul-foos-resource/foos
    ```

    请注意：

    - 我们正在代理我们的资源服务器 Foos。
    - 用户界面上所有以"/foos/"开头的请求都将被路由到我们的 Foos 资源服务器 <http://loclahost:8081/spring-zuul-foos-resource/foos/。>

4. 应用程序接口

    我们的 API 应用程序是一个简单的 Spring Boot 应用程序。

    在本文中，我们将考虑在运行于 8081 端口的服务器中部署 API。

    首先，让我们为将要使用的资源定义基本 DTO：

    参见：spring-zuul-foos-resource/.web.dto/Foo.java

    还有一个简单的控制器：

    ```java
    @RestController
    public class FooController {
        @GetMapping("/foos/{id}")
        public Foo findById(
        @PathVariable long id, HttpServletRequest req, HttpServletResponse res) {
            return new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4));
        }
    }
    ```

5. 用户界面应用程序

    我们的用户界面应用程序也是一个简单的 Spring Boot 应用程序。

    在本文中，我们将考虑在运行于 8080 端口的服务器中部署 API。

    让我们从使用了一点 AngularJS 的主 index.html 开始：

    参见：spring-zuul-ui/src/main/resources/templates/index.html

    这里最重要的一点是我们如何使用相对 URL 访问 API！

    请记住，API 应用程序与 UI 应用程序并不部署在同一服务器上，因此相对 URL 不应该起作用，而且在没有代理的情况下也不会起作用。

    不过，有了代理，我们就可以通过 Zuul 代理访问 Foo 资源，当然，Zuul 代理会将这些请求路由到 API 实际部署的地方。

    最后，实际启用 Boot 应用程序：

    参见：spring-zuul-ui/.config/UiApplication.java

    除了简单的 Boot 注释外，请注意我们还为 Zuul 代理使用了 enable 类型的注解，这非常酷、简洁明了。

6. 测试路由

    现在，让我们测试 UI 应用程序，参见：

    spring-zuul-ui/test/.web/LiveTest.java whenSendRequestToFooResource_thenOK()

7. 自定义 Zuul 过滤器

    有多种可用的 Zuul 过滤器，我们也可以创建自己的自定义过滤器：

    参见：spring-zuul-ui/.spring.cloud.zuul.filter/CustomZuulErrorFilter.java

    这个简单的过滤器只是在请求中添加了一个名为 "Test" 的头--当然，我们也可以根据需要在此添加复杂的请求。

8. 测试自定义 Zuul 过滤器

    最后，让我们测试一下自定义过滤器是否正常工作--首先，我们要修改 Foos 资源服务器上的 FooController：

    参见：spring-zuul-foos-resource/.web.dto/FooController.java

    现在，让我们来测试一下：

    spring-zuul-ui/test/.web/LiveTest.java whenSendRequest_thenHeaderAdded()

9. 结论

    在本文中，我们重点介绍了如何使用 Zuul 将 UI 应用程序中的请求路由到 REST API。我们成功地绕过了 CORS 和同源策略，还设法定制和增强了传输中的 HTTP 请求。

    本教程的完整实现可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-zuul) 项目中找到。
