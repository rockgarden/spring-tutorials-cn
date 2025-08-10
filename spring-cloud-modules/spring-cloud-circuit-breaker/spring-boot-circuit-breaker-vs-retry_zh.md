# [Spring Boot 中熔断器（Circuit Breaker）与重试（Retry）的区别](https://www.baeldung.com/spring-boot-circuit-breaker-vs-retry)

Spring Boot

Resilience4j    Spring Retry

1. 概述

    在分布式系统和微服务架构中，优雅地处理故障对于保持系统的可靠性与性能至关重要。**熔断器（Circuit Breaker）** 和 **重试（Retry）** 是两种核心的容错模式，它们都能提升系统稳定性，但目的和应用场景截然不同。

    本文将深入探讨这两种模式的工作机制、使用场景，并通过 **Resilience4j** 在 [Spring Boot](https://www.baeldung.com/spring-boot-resilience4j) 中实现它们。

2. 什么是重试（Retry）？

    重试模式是一种简单而强大的机制，用于处理分布式系统中的**短暂性故障**。当某个操作失败时，重试模式会尝试多次执行该操作，期望临时问题能够自行恢复。

    1. 重试的关键特性

        - **重复尝试**：核心思想是将失败的操作重新执行指定次数。
        - **退避策略（Backoff Strategies）**：高级重试机制包含退避策略，例如**指数退避（exponential backoff）**，以避免对系统造成过载。
        - **适用于临时故障**：最适合处理间歇性网络问题、服务短暂不可用或资源瞬时不足等情况。

    2. 重试实现示例

        以下是一个使用 Resilience4j 实现重试机制的简单示例：

        ```java
        @Test
        public void whenRetryWithExponentialBackoffIsUsed_thenItRetriesAndSucceeds() {
            IntervalFunction intervalFn = IntervalFunction.ofExponentialBackoff(1000, 2);
            RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(intervalFn)
                .build();

            Retry retry = Retry.of("paymentRetry", retryConfig);

            when(paymentService.process(1)).thenThrow(new RuntimeException("First Failure"))
                .thenThrow(new RuntimeException("Second Failure"))
                .thenReturn("Success");

            Callable<String> decoratedCallable = Retry.decorateCallable(
            retry, () -> paymentService.processPayment(1)
            );

            try {
                String result = decoratedCallable.call();
                assertEquals("Success", result);
            } catch (Exception ignored) {
            }

            verify(paymentService, times(3)).processPayment(1);
        }
        ```

        在这个例子中：

        - 重试机制最多尝试 5 次
        - 使用指数退避策略，在每次尝试之间引入延迟，降低系统过载风险
        - 操作在两次失败后成功

3. 什么是熔断器（Circuit Breaker）模式？

    [熔断器模式](https://www.baeldung.com/cs/microservices-circuit-breaker-pattern)是一种更高级的故障处理方式。它能防止应用程序反复尝试执行**极有可能失败的操作**，从而避免级联故障，提升系统整体稳定性。

    1. 熔断器的关键特性

        - **状态管理**：熔断器有三种主要状态：
        - **Closed（关闭）**：正常运行，允许请求通过
        - **Open（打开）**：阻断所有请求，防止进一步失败
        - **Half-Open（半开）**：允许少量测试请求，检查系统是否已恢复
        - **失败阈值**：监控滑动窗口内的失败请求比例，当失败率超过设定阈值时，“跳闸”进入 Open 状态
        - **防止级联故障**：阻止对故障服务的重复调用，保护整个系统不被拖垮

    2. 熔断器实现示例

        以下是一个展示熔断器状态转换的简单示例：

        ```java
        @Test
        public void whenCircuitBreakerTransitionsThroughStates_thenBehaviorIsVerified() {
            CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

            CircuitBreaker circuitBreaker = CircuitBreaker.of("paymentCircuitBreaker", circuitBreakerConfig);

            AtomicInteger callCount = new AtomicInteger(0);

            when(paymentService.processPayment(anyInt())).thenAnswer(invocationOnMock -> {
                callCount.incrementAndGet();
                throw new RuntimeException("Service Failure");
            });

            Callable<String> decoratedCallable = CircuitBreaker.decorateCallable(
            circuitBreaker, () -> paymentService.processPayment(1)
            );

            for (int i = 0; i < 10; i++) {
                try {
                    decoratedCallable.call();
                } catch (Exception ignored) {
                }
            }

            assertEquals(5, callCount.get());
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

            callCount.set(0);
            circuitBreaker.transitionToHalfOpenState();

            assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
            reset(paymentService);
            when(paymentService.processPayment(anyInt())).thenAnswer(invocationOnMock -> {
                callCount.incrementAndGet();
                return "Success";
            });

            for (int i = 0; i < 3; i++) {
                try {
                    decoratedCallable.call();
                } catch (Exception ignored) {
                }
            }

            assertEquals(3, callCount.get());
            assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        }
        ```

        在这个例子中：

        - 当失败率达到 50%（5 次调用中失败 3 次），熔断器“跳闸”
        - 经过 5 次失败后，熔断器进入 Open 状态，后续请求被立即拒绝
        - 经过一段时间后进入 Half-Open 状态，允许 3 次测试请求
        - 如果测试成功，熔断器回到 Closed 状态，恢复正常

4. 核心区别：重试 vs 熔断器

    | 方面 | 重试（Retry） | 熔断器（Circuit Breaker） |
    |------|----------------|---------------------------|
    | **主要目标** | 多次尝试执行操作 | 阻止对故障服务的重复调用 |
    | **故障假设** | 假设是短暂性故障 | 假设是系统性或持续性故障 |
    | **状态管理** | 无状态，不断重试 | 有状态（Closed/Open/Half-Open） |
    | **最佳使用场景** | 间歇性、可恢复的错误 | 持续性或系统性故障 |

5. 何时使用哪种模式？

    选择使用重试还是熔断器，取决于系统遇到的故障类型。这两种模式是互补的。

    ✅ **使用重试当：**

    - 处理短暂的网络问题
    - 服务暂时不可用是预期情况
    - 几次重试后很可能快速恢复

    ✅ **使用熔断器当：**

    - 防止长时间的服务故障影响
    - 避免微服务中的级联故障
    - 构建自愈系统架构

    > 💡 **实际应用中，两者常结合使用**：例如，在熔断器处于 **Closed 或 Half-Open** 状态时才允许重试，避免在服务已崩溃时仍不断重试。

6. 最佳实践

    为了最大化这两种模式的效果，请遵循以下建议：

    1. **监控指标**：持续监控失败率、重试次数和熔断器状态，以优化配置。
    2. **组合使用**：用重试处理短暂错误，用熔断器应对系统性故障。
    3. **设置合理阈值**：过于激进的阈值可能阻碍恢复或延迟故障检测。
    4. **使用成熟库**：使用如 **Resilience4j** 或 **Spring Cloud Circuit Breaker** 这样的库，它们底层集成了 Resilience4j 和 Spring Retry，简化实现。

7. Spring Boot 集成

    Spring Boot 通过其生态系统为熔断器和重试模式提供了全面支持，主要通过 **Spring Cloud Circuit Breaker** 项目和 **Spring Retry** 模块实现。

    主要特性：

    - **自动配置（Auto-configuration）**：根据类路径中的依赖自动配置相关 Bean，减少样板代码。
    - **可插拔架构（Pluggable Architecture）**：可在 Resilience4j、Hystrix、Sentinel 等实现之间切换，无需修改业务逻辑。
    - **配置灵活**：支持通过 `application.yml` 或 Java 代码进行全局或特定服务的配置。
    - **与 Spring 生态无缝集成**：与 `RestTemplate`、`WebClient`、Spring Cloud 组件等无缝协作。
    - **监控与指标**：通过 Spring Boot Actuator 提供内置的监控能力，便于跟踪熔断器状态和重试行为。

    这种集成方式符合 Spring Boot “约定优于配置”的设计理念，同时保留了必要的灵活性。

8. 结论

    重试和熔断器是分布式系统中不可或缺的两种容错模式：

    - **重试** 关注**即时恢复**
    - **熔断器** 提供**系统级保护**

    通过理解它们的差异和适用场景，我们可以设计出更可靠、更具弹性的系统。

    借助 **Resilience4j** 和 **Spring Cloud Circuit Breaker**，Spring Boot 提供了一个强大的平台，让我们可以轻松实现这些模式。采用这些容错策略，能够构建出即使在恶劣条件下也能优雅应对故障的应用程序，确保用户获得流畅的体验。
