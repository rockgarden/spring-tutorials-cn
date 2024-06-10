# [Spring Cloud Netflix Zuul 中的速率限制](https://www.baeldung.com/spring-cloud-zuul-rate-limit)

1. 简介

    [Spring Cloud Netflix Zuul](https://github.com/spring-cloud/spring-cloud-netflix) 是一个封装 [Netflix Zuul](https://github.com/Netflix/zuul) 的开源网关。它为 Spring Boot 应用程序添加了一些特定功能。遗憾的是，开箱即不提供速率限制功能。

    在本教程中，我们将探索 [Spring Cloud Zuul RateLimit](https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit)，它增加了对速率限制请求的支持。

2. Maven 配置

    除了 Spring Cloud Netflix Zuul 依赖关系外，我们还需要在应用程序的 pom.xml 中添加 Spring Cloud Zuul RateLimit：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
    </dependency>
    <dependency>
        <groupId>com.marcosbarbero.cloud</groupId>
        <artifactId>spring-cloud-zuul-ratelimit</artifactId>
        <version>2.2.0.RELEASE</version>
    </dependency>
    ```

3. 控制器示例

    首先，让我们创建几个 REST 端点，在这些端点上应用速率限制。

    下面是一个带有两个端点的简单 Spring Controller 类：

    [GreetingController](./spring-zuul-rate-limiting/src/main/java/com/baeldung/spring/cloud/zuulratelimitdemo/controller/GreetingController.java)

    正如我们所看到的，没有专门用于限制端点速率的代码。这是因为我们将在 application.yml 文件中的 Zuul 属性中进行配置。这样，我们的代码就可以保持解耦。

4. Zuul 属性

    其次，让我们在 [application.yml](./spring-zuul-rate-limiting/src/main/resources/application.yml) 文件中添加以下 Zuul 属性。

    在 zuul.routes 下，我们提供了端点的详细信息。在 zuul.ratelimit.policy-list 下，我们为端点提供了速率限制配置。limit 属性指定了端点在刷新周期内可被调用的次数。

    如图所示，我们为 serviceSimple 端点添加了每 60 秒 5 次请求的速率限制。相比之下，serviceAdvanced 的速率限制为每 2 秒 1 次请求。

    类型配置指定了我们要遵循的速率限制方法。以下是可能的值：

    - origin - 基于用户起源请求的速率限制
    - url - 基于下游服务请求路径的速率限制
    - user - 基于经过验证的用户名或 “anonymous” 的速率限制
    - No value - 作为每个服务的全局配置。要使用这种方法，只需不设置参数 "type"

5. 测试速率限制

    1. 在速率限制范围内请求

        接下来，让我们测试速率限制：

        [GreetingControllerIntegrationTest.java](./spring-zuul-rate-limiting/src/test/java/com/baeldung/spring/cloud/zuulratelimitdemo/controller/GreetingControllerIntegrationTest.java):whenRequestNotExceedingCapacity_thenReturnOkResponse()

        在此，我们对 /greeting/simple 端点进行了一次调用。由于请求未超出速率限制，因此请求成功。

        另一个关键点是，每次响应都会返回头信息，为我们提供有关速率限制的更多信息。对于上述请求，我们会收到以下标头：

        X-RateLimit-Limit-rate-limit-application_serviceSimple_127.0.0.1: 5
        X-RateLimit-Remaining-rate-limit-application_serviceSimple_127.0.0.1: 4
        X-RateLimit-Reset-rate-limit-application_serviceSimple_127.0.0.1: 60000

        换句话说：

        - X-RateLimit-Limit-[key]： 为端点配置的限制值
        - X-RateLimit-Remaining-[key]：呼叫端点的剩余尝试次数
        - X-RateLimit-Reset-[key]：为端点配置的刷新间隔的剩余毫秒数

        此外，如果我们立即再次调用同一个端点，我们可以得到

        X-RateLimit-Limit-rate-limit-application_serviceSimple_127.0.0.1: 5
        X-RateLimit-Remaining-rate-limit-application_serviceSimple_127.0.0.1: 3
        X-RateLimit-Reset-rate-limit-application_serviceSimple_127.0.0.1: 57031

        请注意剩余尝试次数和剩余毫秒数的减少。

    2. 请求超出速率限制

        让我们看看超过速率限制时会发生什么：

        [GreetingControllerIntegrationTest.java](./spring-zuul-rate-limiting/src/test/java/com/baeldung/spring/cloud/zuulratelimitdemo/controller/GreetingControllerIntegrationTest.java)
        :whenRequestExceedingCapacity_thenReturnTooManyRequestsResponse()

        在这里，我们连续两次快速调用 /greeting/advanced 端点。由于我们已将速率限制配置为每 2 秒一次请求，因此第二次调用将失败。因此，错误代码 429（请求过多）会返回给客户端。

        以下是达到速率限制时返回的标头：

        X-RateLimit-Limit-rate-limit-application_serviceAdvanced_127.0.0.1: 1
        X-RateLimit-Remaining-rate-limit-application_serviceAdvanced_127.0.0.1: 0
        X-RateLimit-Reset-rate-limit-application_serviceAdvanced_127.0.0.1: 268

        之后，我们休眠 2 秒。这是为端点配置的刷新间隔。最后，我们再次触发端点并得到成功响应。

6. 自定义密钥生成器

    我们可以使用自定义密钥生成器自定义响应头中发送的密钥。这非常有用，因为应用程序可能需要控制类型属性所提供选项之外的密钥策略。

    例如，可以通过创建自定义 RateLimitKeyGenerator 实现来实现这一点。我们可以添加更多限定符或完全不同的内容：

    ```java
    @Bean
    public RateLimitKeyGenerator rateLimitKeyGenerator(RateLimitProperties properties, 
    RateLimitUtils rateLimitUtils) {
        return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils) {
            @Override
            public String key(HttpServletRequest request, Route route, 
            RateLimitProperties.Policy policy) {
                return super.key(request, route, policy) + "_" + request.getMethod();
            }
        };
    }
    ```

    上述代码将 REST 方法名称附加到 key 上。例如

    `X-RateLimit-Limit-rate-limit-application_serviceSimple_127.0.0.1_GET: 5`

    另一个关键点是，RateLimitKeyGenerator Bean 将由 spring-cloud-zuul-ratelimit 自动配置。

7. 自定义错误处理

    该框架支持各种速率限制数据存储实现。例如，提供了 Spring Data JPA 和 Redis。默认情况下，故障会使用 DefaultRateLimiterErrorHandler 类记录为错误。

    当我们需要以不同方式处理错误时，可以定义一个自定义的 RateLimiterErrorHandler Bean：

    ```java
    @Bean
    public RateLimiterErrorHandler rateLimitErrorHandler() {
        return new DefaultRateLimiterErrorHandler() {
            @Override
            public void handleSaveError(String key, Exception e) {
                // implementation
            }

            @Override
            public void handleFetchError(String key, Exception e) {
                // implementation
            }

            @Override
            public void handleError(String msg, Exception e) {
                // implementation
            }
        };
    }
    ```

    与 RateLimitKeyGenerator Bean 类似，RateLimiterErrorHandler Bean 也将自动配置。

8. 结论

    在本文中，我们了解了如何使用 Spring Cloud Netflix Zuul 和 Spring Cloud Zuul RateLimit 对 API 进行速率限制。
