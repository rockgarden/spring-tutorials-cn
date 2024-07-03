# [在 Spring 云网关中处理响应体](https://www.baeldung.com/spring-cloud-gateway-response-body)

1. 简介

    在本教程中，我们将了解如何使用 Spring Cloud Gateway 检查和/或修改响应体，然后将其发送回客户端。

2. Spring Cloud Gateway 快速回顾

    Spring Cloud Gateway（简称 SCG）是 Spring Cloud 系列的一个子项目，它提供了一个构建在反应式 Web 栈之上的 API 网关。我们已经在[前面的教程](https://www.baeldung.com/?s=%22spring+cloud+gateway%22)中介绍了它的基本用法，所以这里就不再赘述。

    相反，这次我们将重点关注围绕 API 网关设计解决方案时经常出现的一种特殊使用场景：如何在将后端响应有效载荷发送回客户端之前对其进行处理？

    下面列出了我们可能会用到这种功能的一些情况：

    - 保持与现有客户端的兼容性，同时允许后端不断发展
    - 屏蔽某些字段，以符合 PCI 或 GDPR 等法规的要求

    从更实际的角度来看，满足这些要求意味着我们需要实施一个过滤器来处理后台响应。由于过滤器是 SCG 的核心概念，因此我们只需实施一个自定义过滤器，应用所需的转换即可支持响应处理。

    此外，一旦我们创建了过滤器组件，就可以将其应用于任何已声明的路由。

3. 实现数据擦除过滤器

    为了更好地说明响应体操作是如何工作的，让我们创建一个简单的过滤器来屏蔽基于 JSON 的响应中的值。例如，给定一个 JSON，其中有一个名为 "ssn" 的字段：

    ```json
    {
    "name" : "John Doe",
    "ssn" : "123-45-9999",
    "account" : "9999888877770000"
    }
    ```

    我们希望用一个固定值替换它们的值，从而防止数据泄漏：

    ```json
    {
    "name" : "John Doe",
    "ssn" : "****",
    "account" : "9999888877770000"
    }
    ```

    1. 实现 GatewayFilterFactory

        顾名思义，GatewayFilterFactory 是给定时间过滤器的工厂。启动时，Spring 会查找任何实现此接口的 [@Component](https://www.baeldung.com/spring-component-annotation)-注释类。然后，它会创建一个可用过滤器注册表，供我们在声明路由时使用：

        ```yml
        spring:
        cloud:
            gateway:
            routes:
            - id: rewrite_with_scrub
                uri: ${rewrite.backend.uri:http://example.com}
                predicates:
                - Path=/v1/customer/**
                filters:
                - RewritePath=/v1/customer/(?<segment>.*),/api/$\{segment}
                - ScrubResponse=ssn,***
        ```

        请注意，在使用这种基于配置的方法定义路由时，必须根据 SCG 的预期命名约定来命名我们的工厂： FilterNameGatewayFilterFactory。考虑到这一点，我们将把工厂命名为 ScrubResponseGatewayFilterFactory。

        SCG 已经有几个实用类，我们可以用它们来实现这个工厂。在这里，我们将使用一个开箱即用的过滤器常用的类： `AbstractGatewayFilterFactory<T>` 是一个模板化的基类，其中 T 代表与我们的过滤器实例相关联的配置类。在我们的例子中，我们只需要两个配置属性：

        - fields：用于匹配字段名称的正则表达式
        - replacement：替换原始值的字符串

        我们必须实现的关键方法是 apply()。SCG 会为每个使用我们的过滤器的路由定义调用该方法。例如，在上面的配置中，由于只有一个路由定义，因此只调用一次 apply()。

        在我们的例子中，实现方法非常简单：

        ```java
        @Override
        public GatewayFilter apply(Config config) {
            return modifyResponseBodyFilterFactory
            .apply(c -> c.setRewriteFunction(JsonNode.class, JsonNode.class, new Scrubber(config)));
        }
        ```

        本例中之所以如此简单，是因为我们使用了另一个内置过滤器 ModifyResponseBodyGatewayFilterFactory。我们使用构造函数注入来获取该工厂的实例，并在 apply() 中委托它创建 GatewayFilter 实例。

        这里的关键点是使用 apply() 方法的变体，该变体不是获取配置对象，而是期望获取配置的 Consumer。同样重要的是，该配置是一个 ModifyResponseBodyGatewayFilterFactory。该配置对象提供了我们在代码中调用的 setRewriteFunction() 方法。

    2. 使用 setRewriteFunction()

        现在，让我们深入了解一下 setRewriteFunction()。

        该方法需要三个参数：两个类（in 和 out）和一个可以将输入类型转换为输出类型的函数。在我们的例子中，我们没有转换类型，所以输入和输出都使用同一个类： JsonNode。该类来自 [Jackson](https://www.baeldung.com/jackson) 库，位于用于表示 JSON 中不同节点类型（如对象节点、数组节点等）的类的最顶层。使用 JsonNode 作为输入/输出类型，我们可以处理任何有效的 JSON 有效负载，而这正是我们在本例中需要的。

        对于转换器类，我们传递一个 Scrubber 的实例，它在 apply() 方法中实现了所需的 RewriteFunction 接口：

        ```java
        public static class Scrubber implements RewriteFunction<JsonNode,JsonNode> {
            // ... fields and constructor omitted
            @Override
            public Publisher<JsonNode> apply(ServerWebExchange t, JsonNode u) {
                return Mono.just(scrubRecursively(u));
            }
            // ... scrub implementation omitted
        }
        ```

        传递给 apply() 的第一个参数是当前的 ServerWebExchange，通过它我们可以访问到目前为止的请求处理上下文。我们不会在这里使用它，但知道我们有这个能力是件好事。下一个参数是接收到的正文，已在类中转换为已告知的内容。

        预期的返回结果是一个有信息的外类实例的 [Publisher](https://www.baeldung.com/reactor-core)。因此，只要我们不进行任何阻塞性 I/O 操作，就可以在重写函数内部进行一些复杂的工作。

    3. 洗涤器的实现

        既然我们已经知道了重写函数的契约，那么最后就来实现我们的洗涤器(Scrubber)逻辑吧。在这里，我们假设有效载荷相对较小，因此不必担心存储接收对象所需的内存。

        它的实现只是在所有节点上递归行走，寻找与配置模式相匹配的属性，并替换相应的掩码值：

        ```java
        public static class Scrubber implements RewriteFunction<JsonNode,JsonNode> {
            // ... fields and constructor omitted
            private JsonNode scrubRecursively(JsonNode u) {
                if ( !u.isContainerNode()) {
                    return u;
                }
                
                if (u.isObject()) {
                    ObjectNode node = (ObjectNode)u;
                    node.fields().forEachRemaining((f) -> {
                        if ( fields.matcher(f.getKey()).matches() && f.getValue().isTextual()) {
                            f.setValue(TextNode.valueOf(replacement));
                        }
                        else {
                            f.setValue(scrubRecursively(f.getValue()));
                        }
                    });
                }
                else if (u.isArray()) {
                    ArrayNode array = (ArrayNode)u;
                    for ( int i = 0 ; i < array.size() ; i++ ) {
                        array.set(i, scrubRecursively(array.get(i)));
                    }
                }
                
                return u;
            }
        }
        ```

4. 测试

    我们在示例代码中包含了两个测试：一个简单的单元测试和一个集成测试。第一个测试只是一个普通的 JUnit 测试，用于检查刷新器的正确性。集成测试更有趣，因为它说明了 SCG 开发中的有用技术。

    首先，需要提供一个可以发送消息的实际后台。一种可能性是使用 Postman 或类似的外部工具，这对典型的 CI/CD 方案会带来一些问题。相反，我们将使用 JDK 鲜为人知的 HttpServer 类，它实现了一个简单的 HTTP 服务器。

    ```java
    @Bean
    public HttpServer mockServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0),0);
        server.createContext("/customer", (exchange) -> {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            
            byte[] response = JSON_WITH_FIELDS_TO_SCRUB.getBytes("UTF-8");
            exchange.sendResponseHeaders(200,response.length);
            exchange.getResponseBody().write(response);
        });
        
        server.setExecutor(null);
        server.start();
        return server;
    }
    ```

    该服务器将处理 /customer 请求，并返回我们测试中使用的固定 JSON 响应。请注意，返回的服务器已经启动，并将通过随机端口监听传入请求。我们还指示服务器创建一个新的默认执行器，以管理用于处理请求的线程。

    其次，我们以编程方式创建了一个路由 @Bean，其中包含我们的过滤器。这相当于使用配置属性创建路由，但允许我们完全控制测试路由的各个方面：

    ```java
    @Bean
    public RouteLocator scrubSsnRoute(
    RouteLocatorBuilder builder, 
    ScrubResponseGatewayFilterFactory scrubFilterFactory, 
    SetPathGatewayFilterFactory pathFilterFactory, 
    HttpServer server) {
        int mockServerPort = server.getAddress().getPort();
        ScrubResponseGatewayFilterFactory.Config config = new ScrubResponseGatewayFilterFactory.Config();
        config.setFields("ssn");
        config.setReplacement("*");
        
        SetPathGatewayFilterFactory.Config pathConfig = new SetPathGatewayFilterFactory.Config();
        pathConfig.setTemplate("/customer");
        
        return builder.routes()
        .route("scrub_ssn",
            r -> r.path("/scrub")
            .filters( 
                f -> f
                    .filter(scrubFilterFactory.apply(config))
                    .filter(pathFilterFactory.apply(pathConfig)))
            .uri("http://localhost:" + mockServerPort ))
        .build();
    }
    ```

    最后，由于这些 Bean 现在是 @TestConfiguration 的一部分，我们可以将它们与 [WebTestClient](https://www.baeldung.com/spring-5-webclient#workingwebtestclient) 一起注入到实际测试中。实际测试将使用 WebTestClient 来驱动旋转 SCG 和后端：

    ```java
    @Test
    public void givenRequestToScrubRoute_thenResponseScrubbed() {
        client.get()
        .uri("/scrub")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
            .is2xxSuccessful()
        .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
            .json(JSON_WITH_SCRUBBED_FIELDS);
    }
    ```

5. 结论

    在本文中，我们展示了如何使用 Spring Cloud Gateway 库访问后端服务的响应体并对其进行修改。
