# Resilience4j 指南

1. 概述

    在本教程中，我们将讨论 [Resilience4j](http://resilience4j.github.io/resilience4j/) 库。

    该库通过管理远程通信容错来帮助实现弹性系统。

    该库受到 [Hystrix](https://www.baeldung.com/introduction-to-hystrix) 的启发，但提供了更方便的 API 和许多其他功能，如 Rate Limiter（阻止过于频繁的请求）、Bulkhead（避免过多并发请求）等。

2. Maven 设置

    首先，我们需要将目标模块添加到 pom.xml（例如，这里我们添加了断路器）：

    ```xml
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-circuitbreaker</artifactId>
        <version>2.1.0</version>
    </dependency>
    ```

    在此，我们使用 resilience4j-circuitbreaker 模块。所有模块及其最新版本均可在 Maven Central 上找到。

    在接下来的章节中，我们将介绍库中最常用的模块。

3. 断路器

    请注意，对于该模块，我们需要上图所示的 resilience4j-circuitbreaker 依赖关系。

    [断路器模式](https://martinfowler.com/bliki/CircuitBreaker.html)可帮助我们在远程服务宕机时防止出现一连串故障。

    在多次尝试失败后，我们可以认为该服务unavailable/overloaded，并急切地拒绝所有后续请求。这样，我们就可以为可能失败的调用节省系统资源。

    让我们看看如何使用 Resilience4j 来实现这一目标。

    首先，我们需要定义要使用的设置。最简单的方法是使用默认设置：

    ```java
    CircuitBreakerRegistry circuitBreakerRegistry
    = CircuitBreakerRegistry.ofDefaults();
    ```

    也可以使用自定义参数：

    ```java
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
    .failureRateThreshold(20)
    .withSlidingWindow(5)
    .build();
    ```

    在这里，我们将故障率阈值设为 20%，并设置最少 5 次呼叫尝试。

    然后，我们创建一个 CircuitBreaker 对象，并通过它调用远程服务：

    ```java
    interface RemoteService {
        int process(int i);
    }

    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
    CircuitBreaker circuitBreaker = registry.circuitBreaker("my");
    Function<Integer, Integer> decorated = CircuitBreaker
    .decorateFunction(circuitBreaker, service::process);
    ```

    最后，让我们通过 JUnit 测试看看它是如何工作的。

    我们将尝试调用服务 10 次。我们应能验证调用至少尝试了 5 次，然后在 20% 的调用失败后立即停止：

    ```java
    when(service.process(any(Integer.class))).thenThrow(new RuntimeException());

    for (int i = 0; i < 10; i++) {
        try {
            decorated.apply(i);
        } catch (Exception ignore) {}
    }

    verify(service, times(5)).process(any(Integer.class));
    ```

    1. 断路器的状态和设置

        断路器可以处于三种状态之一：

        - CLOSED - 一切正常，不涉及短路问题
        - OPEN - 远程服务器宕机，所有向其发出的请求均被短路
        - HALF_OPEN - 自进入 OPEN 状态以来已过了一定时间，CircuitBreaker 允许请求检查远程服务是否已恢复在线

        我们可以配置以下设置：

        - 故障率阈值，超过此阈值时，断路器会打开并开始短路调用
        - 等待时长，用于定义断路器在切换到半开状态前应保持多长时间的打开状态
        - 断路器半开或关闭时环形缓冲区的大小
        - 自定义 CircuitBreakerEventListener，用于处理断路器事件
        - 自定义谓词，用于评估异常是否应算作故障，从而提高故障率

4. 速率限制器

    与上一节类似，该功能也需要依赖 resilience4j-ratelimiter。

    顾名思义，该功能允许限制对某些服务的访问。其 API 与 CircuitBreaker 的非常相似--有注册表、配置和限制器类。

    下面是一个示例：

    ```java
    RateLimiterConfig config = RateLimiterConfig.custom().limitForPeriod(2).build();
    RateLimiterRegistry registry = RateLimiterRegistry.of(config);
    RateLimiter rateLimiter = registry.rateLimiter("my");
    Function<Integer, Integer> decorated
    = RateLimiter.decorateFunction(rateLimiter, service::process);
    ```

    现在，对 decorated 服务块的所有调用都符合速率限制器配置（如有必要）。

    我们可以配置以下参数

    - 限制刷新周期
    - 刷新周期的权限限制
    - 默认的权限等待时间

5. 隔墙

    在这里，我们首先需要依赖 resilience4j-bulkhead。

    我们可以限制特定服务的并发调用次数。

    下面我们来看一个使用 Bulkhead API 配置最大并发调用次数的示例：

    ```java
    BulkheadConfig config = BulkheadConfig.custom().maxConcurrentCalls(1).build();
    BulkheadRegistry registry = BulkheadRegistry.of(config);
    Bulkhead bulkhead = registry.bulkhead("my");
    Function<Integer, Integer> decorated
    = Bulkhead.decorateFunction(bulkhead, service::process);
    ```

    为了测试此配置，我们将调用一个模拟服务方法。

    然后，我们确保 Bulkhead 不允许任何其他调用：

    ```java
    CountDownLatch latch = new CountDownLatch(1);
    when(service.process(anyInt())).thenAnswer(invocation -> {
        latch.countDown();
        Thread.currentThread().join();
        return null;
    });

    ForkJoinTask<?> task = ForkJoinPool.commonPool().submit(() -> {
        try {
            decorated.apply(1);
        } finally {
            bulkhead.onComplete();
        }
    });
    latch.await();
    assertThat(bulkhead.tryAcquirePermission()).isFalse();
    ```

    我们可以配置以下设置：

    - bulkhead 允许的并行执行最大数量
    - 线程尝试进入饱和隔板时的最长等待时间；

6. 重试

    为实现这一功能，我们需要在项目中添加 resilience4j-retry 库。

    我们可以使用重试 API 自动重试失败的调用：

    ```java
    RetryConfig config = RetryConfig.custom().maxAttempts(2).build();
    RetryRegistry registry = RetryRegistry.of(config);
    Retry retry = registry.retry("my");
    Function<Integer, Void> decorated
    = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });
    ```

    现在，让我们模拟在远程服务调用过程中出现异常的情况，并确保库自动重试失败的调用：

    ```java
    when(service.process(anyInt())).thenThrow(new RuntimeException());
    try {
        decorated.apply(1);
        fail("Expected an exception to be thrown if all retries failed");
    } catch (Exception e) {
        verify(service, times(2)).process(any(Integer.class));
    }
    ```

    我们还可以配置以下内容：

    - 最大尝试次数
    - 重试前的等待时间
    - 用于修改失败后等待时间间隔的自定义函数
    - 自定义谓词，用于评估异常是否会导致重试调用

7. 缓存

    缓存模块需要依赖 resilience4j-cache。

    初始化与其他模块略有不同：

    ```java
    javax.cache.Cache cache = ...; // Use appropriate cache here
    Cache<Integer, Integer> cacheContext = Cache.of(cache);
    Function<Integer, Integer> decorated
    = Cache.decorateSupplier(cacheContext, () -> service.process(1));
    ```

    此处的缓存是由所使用的 [JSR-107](https://www.baeldung.com/jcache) 缓存实现完成的，Resilience4j 提供了应用该实现的方法。

    请注意，没有用于装饰函数（如 Cache.decorationFunction(Function)）的 API，该 API 仅支持 Supplier 和 Callable 类型。

8. 时间限制器

    对于该模块，我们必须添加 resilience4j-timelimiter 依赖关系。

    使用 TimeLimiter 可以限制调用远程服务的时间。

    为了演示，让我们设置一个超时为 1 毫秒的 TimeLimiter：

    ```java
    long ttl = 1;
    TimeLimiterConfig config
    = TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(ttl)).build();
    TimeLimiter timeLimiter = TimeLimiter.of(config);
    ```

    接下来，让我们验证 Resilience4j 调用 Future.get() 时是否达到预期超时：

    ```java
    Future futureMock = mock(Future.class);
    Callable restrictedCall
    = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> futureMock);
    restrictedCall.call();

    verify(futureMock).get(ttl, TimeUnit.MILLISECONDS);`
    ```

    我们还可以将它与 CircuitBreaker 结合使用：

    ```java
    Callable chainedCallable
    = CircuitBreaker.decorateCallable(circuitBreaker, restrictedCall);
    ```

9. 附加模块

    Resilience4j 还提供了许多附加模块，便于与流行的框架和库集成。

    其中比较著名的集成包括：

    - Spring Boot - resilience4j-spring-boot 模块
    - Ratpack - resilience4j-ratpack 模块
    - Retrofit - resilience4j-retrofit模块
    - Vertx--resilience4j-vertx模块
    - Dropwizard--resilience4j-metrics模块
    - Prometheus - resilience4j-prometheus 模块

10. 结论

    在本文中，我们介绍了 Resilience4j 库的各个方面，并学习了如何使用它来解决服务器间通信中的各种容错问题。
