# [Spring调度注释](https://www.baeldung.com/spring-scheduling-annotations)

1. 概述

    当单线程执行还不够时，我们可以使用 org.springframework.scheduling.annotation 包中的注解。

    在本快速教程中，我们将探讨 Spring 调度注解。

2. @EnableAsync

    使用该注解，我们可以在 Spring 中启用异步功能。

    我们必须与 @Configuration 配合使用：

    ```java
    @Configuration
    @EnableAsync
    class VehicleFactoryConfig {}
    ```

    既然已经启用了异步调用，我们就可以使用 @Async 来定义支持异步调用的方法了。

3. 启用调度

    使用此注解，我们可以在应用程序中启用调度。

    我们还必须将其与 @Configuration 结合使用：

    ```java
    @Configuration
    @EnableScheduling
    class VehicleFactoryConfig {}
    ```

    因此，我们现在可以使用 @Scheduled 定期运行方法。

4. @Async

    我们可以定义要在不同线程上执行的方法，从而异步运行这些方法。

    为此，我们可以用 @Async 来注解方法：

    ```java
    @Async
    void repairCar() {
        // ...
    }
    ```

    如果我们将此注解应用于一个类，那么所有方法都将被异步调用。

    请注意，我们需要通过 @EnableAsync 或 XML 配置来启用异步调用，以使此注解生效。

    有关 @Async 的更多信息，请参阅[本文](https://www.baeldung.com/spring-async)。

5. @Scheduled

    如果我们需要定期执行一个方法，可以使用此注解：

    ```java
    @Scheduled(fixedRate = 10000)
    void checkVehicle() {
        // ...
    }
    ```

    我们可以使用它以固定的时间间隔执行方法，也可以使用类似于 cron 的表达式对其进行微调。

    @Scheduled 利用了 Java 8 的重复注解功能，这意味着我们可以多次使用它来标记一个方法：

    ```java
    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 * * * * MON-FRI")
    void checkVehicle() {
        // ...
    }
    ```

    请注意，注释为 @Scheduled 的方法应具有 void 返回类型。

    此外，我们必须通过 @EnableScheduling 或 XML 配置等方式启用调度，才能使该注解生效。

    有关调度的更多信息，请阅读[本文](https://www.baeldung.com/spring-scheduled-tasks)。

6. @Schedules

    我们可以使用此注解指定多个 @Scheduled 规则：

    ```java
    @Schedules({
    @Scheduled(fixedRate = 10000),
    @Scheduled(cron = "0 * * * * MON-FRI")
    })
    void checkVehicle() {
        // ...
    }
    ```

    请注意，自 Java 8 以来，我们可以通过上述重复注解功能实现同样的功能。

7. 结论

    在本文中，我们概述了最常见的 Spring 调度注解。
