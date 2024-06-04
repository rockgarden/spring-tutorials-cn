# [Spring Cloud 断路器快速指南](https://www.baeldung.com/spring-cloud-circuit-breaker)

1. 概述

    在本教程中，我们将介绍 Spring Cloud Circuit Breaker 项目，并学习如何使用它。

    首先，我们将了解 Spring Cloud Circuit Breaker 除了现有的断路器实现外，还提供了哪些功能。接下来，我们将学习如何使用 Spring Boot [自动配置机制](https://www.baeldung.com/spring-boot-custom-auto-configuration)将一个或多个断路器集成到我们的应用程序中。

    请注意，我们在《[Hystrix 简介](https://www.baeldung.com/introduction-to-hystrix)》、《[Spring Cloud Netflix Hystrix](https://www.baeldung.com/spring-cloud-netflix-hystrix)》和《[Resilience4j指南](https://www.baeldung.com/resilience4j)》中提供了更多有关断路器及其工作原理的信息。

2. Spring Cloud 断路器

    直到最近，Spring Cloud 才为我们提供了一种在应用程序中添加断路器的方法。这是通过使用 Netflix Hystrix 作为 Spring Cloud Netflix 项目的一部分。

    Spring Cloud Netflix 项目实际上只是 Hystrix 的一个基于注解的封装库。因此，这两个库是紧密耦合的。这意味着我们无法在不更改应用程序的情况下切换到另一个断路器实现。

    Spring Cloud Circuit Breaker 项目解决了这个问题。它为不同的断路器实现提供了一个抽象层。这是一个可插拔的架构。因此，我们可以根据所提供的abstraction/interface进行编码，并根据需要切换到另一种实现。

    在我们的示例中，我们将只关注 Resilience4J 的实现。不过，这些技术也可用于其他插件。

3. 自动配置

    为了在应用程序中使用特定的断路器实现，我们需要添加相应的 Spring 启动器。在本例中，我们使用 spring-cloud-starter-circuitbreaker-resilience4j：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        <version>1.0.2.RELEASE</version>
    </dependency>
    ```

    如果在 classpath 中看到其中一个启动器，自动配置机制就会配置必要的断路器 bean。

    如果我们想禁用 Resilience4J 自动配置，可以将 spring.cloud.circuitbreaker.resilience4j.enabled 属性设置为 false。

4. 一个简单的断路器示例

    让我们使用 Spring Boot 创建一个网络应用，探索 Spring Cloud Circuit Breaker 库的工作原理。

    我们将创建一个返回相册列表的简单网络服务。假设原始列表由第三方服务提供。为简单起见，我们将使用 Jsonplaceholder 提供的外部虚拟 API 来检索列表：

    <https://jsonplaceholder.typicode.com/albums>

    1. 创建断路器

        让我们创建第一个断路器。首先，我们将注入 CircuitBreakerFactory Bean 的实例：

        [main/.circuitbreaker/AlbumService.java](./src/main/java/com/baeldung/circuitbreaker/AlbumService.java)

        现在，我们可以使用 CircuitBreakerFactory#create 方法轻松创建断路器。该方法将断路器标识符作为参数：

        `CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");`

    2. 用断路器封装任务

        为了封装并运行受断路器保护的任务，我们需要调用以 Supplier 为参数的 run 方法。

        ```java
        public String getAlbumList() {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
            String url = "https://jsonplaceholder.typicode.com/albums";
            return circuitBreaker.run(() -> restTemplate.getForObject(url, String.class));
        }
        ```

        断路器为我们运行我们的方法，并提供容错功能。

        有时，我们的外部服务可能需要很长时间才能做出响应，或者出现意外异常，或者外部服务或主机不存在。在这种情况下，我们可以提供一个回退作为运行方法的第二个参数：

        main/.circuitbreaker/AlbumService.java:getAlbumList()

        回退的 lambda 会接收 Throwable 作为描述错误的输入。这意味着我们可以根据触发回退的异常类型向调用者提供不同的回退结果。

        在这种情况下，我们不会考虑异常。我们只会返回一个缓存的相册列表。

        如果外部调用以异常结束，且未提供回退，Spring 将抛出 NoFallbackAvailableException 异常。

    3. 构建控制器

        现在，让我们完成示例，创建一个简单的控制器，调用服务方法并通过浏览器显示结果：

        [main/.circuitbreaker/Controller.java](./src/main/java/com/baeldung/circuitbreaker/Controller.java)

        最后，让我们调用 REST 服务并查看结果：

        `[GET] http://localhost:8080/albums`

5. 全局自定义配置

    通常，默认配置是不够的。因此，我们需要根据用例创建具有自定义配置的断路器。

    为了覆盖默认配置，我们需要在 @Configuration 类中指定自己的 Bean 和属性。

    在这里，我们要为所有断路器定义一个全局配置。因此，我们需要定义一个 `Customizer<CircuitBreakerFactory> Bean`。因此，让我们使用 Resilience4JCircuitBreakerFactory 实现。

    首先，我们将根据 [Resilience4j教程](https://www.baeldung.com/resilience4j) 定义断路器和时间限制器配置类：

    ```java
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
    .failureRateThreshold(50)
    .waitDurationInOpenState(Duration.ofMillis(1000))
    .slidingWindowSize(2)
    .build();
    TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
    .timeoutDuration(Duration.ofSeconds(4))
    .build();
    ```

    接下来，让我们使用 Resilience4JCircuitBreakerFactory.configureDefault 方法，将配置嵌入 Customizer Bean 中：

    ```java
    @Configuration
    public class Resilience4JConfiguration {
        @Bean
        public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
            // the circuitBreakerConfig and timeLimiterConfig objects
            return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .timeLimiterConfig(timeLimiterConfig)
            .circuitBreakerConfig(circuitBreakerConfig)
            .build());
        } 
    }
    ```

6. 特定自定义配置

    当然，我们的应用程序中可能有多个断路器。因此，在某些情况下，我们需要对每个断路器进行特定配置。

    同样，我们可以定义一个或多个自定义器 Bean。然后，我们可以使用 Resilience4JCircuitBreakerFactory.configure 方法为每个断路器提供不同的配置：

    main/.circuitbreaker/SpringApp.java:specificCustomConfiguration1()

    这里我们提供了第二个参数，即我们要配置的断路器的 ID。

    通过向同一方法提供断路器 id 列表，我们还可以用相同的配置设置多个断路器：

    main/.circuitbreaker/SpringApp.specificCustomConfiguration2()

7. 替代实现

    我们已经了解了如何使用 Resilience4j 实现与 Spring Cloud Circuit Breaker 一起创建一个或多个断路器。

    不过，我们还可以在应用程序中利用 Spring Cloud Circuit Breaker 支持的其他实现：

    - [Hystrix](https://www.baeldung.com/spring-cloud-netflix-hystrix)
    - [Sentinel](https://github.com/alibaba/Sentinel)
    - [Spring Retry](https://www.baeldung.com/spring-retry)

    值得一提的是，我们可以在应用程序中混合和匹配不同的断路器实现。我们并不局限于一个库。

    上述库的功能比我们在此探讨的要多。不过，Spring Cloud Circuit Breaker 只是对断路器部分的抽象。例如，除了本文使用的 CircuitBreaker 和 TimeLimiter 模块外，Resilience4j 还提供了其他模块，如 RateLimiter、Bulkhead 和 Retry。

8. 结论

    在本文中，我们发现了 Spring Cloud Circuit Breaker 项目。

    首先，我们了解了 Spring Cloud Circuit Breaker 是什么，以及它如何允许我们在应用程序中添加断路器。

    接下来，我们利用 Spring Boot 的自动配置机制来展示如何定义和集成断路器。此外，我们还演示了 Spring Cloud 断路器如何通过简单的 REST 服务工作。

    最后，我们学习了如何一起配置和单独配置所有断路器。
