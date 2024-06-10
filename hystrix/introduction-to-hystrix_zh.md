# [Hystrix 简介](https://www.baeldung.com/introduction-to-hystrix)

1. 概述

    典型的分布式系统由许多共同协作的服务组成。

    这些服务很容易出现故障或响应延迟。如果一个服务出现故障，可能会影响其他服务，从而影响性能，并可能导致应用程序的其他部分无法访问，最坏的情况是导致整个应用程序瘫痪。

    当然，也有一些解决方案可以帮助提高应用程序的弹性和容错能力，Hystrix 就是这样一个框架。

    Hystrix 框架库通过提供容错和延迟容错，帮助控制服务之间的交互。它通过隔离故障服务和阻止故障级联效应来提高系统的整体恢复能力。

    在本系列文章中，我们将首先介绍 Hystrix 如何在服务或系统发生故障时提供帮助，以及 Hystrix 在这些情况下可以实现哪些功能。

2. 简单示例

    Hystrix 提供容错和延迟的方法是隔离和封装对远程服务的调用。

    在这个简单的示例中，我们在 HystrixCommand 的 run() 方法中封装了一个调用：

    main/.hystrix/CommandHelloWorld.java

    然后我们执行如下调用：

    test/.hystrix/HystrixTimeoutManualTest.java:givenInputBobAndDefaultSettings_whenCommandExecuted_thenReturnHelloBob()

3. Maven 设置

    要在 Maven 项目中使用 Hystrix，我们需要在项目 pom.xml 中加入 Netflix 的 hystrix-core 和 rxjava-core 依赖关系：

    ```xml
    <dependency>
        <groupId>com.netflix.hystrix</groupId>
        <artifactId>hystrix-core</artifactId>
        <version>1.5.4</version>
    </dependency>
    ```

    在这里可以找到最新版本。

    ```xml
    <dependency>
        <groupId>com.netflix.rxjava</groupId>
        <artifactId>rxjava-core</artifactId>
        <version>0.20.7</version>
    </dependency>
    ```

    您可以在此处找到该库的最新版本。

4. 设置远程服务

    让我们先模拟一个真实世界的例子。

    在下面的示例中，类 RemoteServiceTestSimulator 代表远程服务器上的一个服务。它有一个方法，该方法会在给定时间后响应一条信息。我们可以想象，这种等待是对远程系统耗时过程的模拟，从而导致对调用服务的响应延迟：

    main/.hystrix/RemoteServiceTestSimulator.java

    下面是我们调用 RemoteServiceTestSimulator 的示例客户端。

    对服务的调用被隔离并封装在 HystrixCommand 的 run() 方法中。这种封装提供了我们上面提到的弹性：

    main/.hystrix/RemoteServiceTestCommand.java

    通过调用 RemoteServiceTestCommand 对象实例的 execute() 方法来执行调用。

    下面的测试演示了如何做到这一点：

    test/.hystrix/HystrixTimeoutManualTest.java:givenSvcTimeoutOf100AndDefaultSettings_whenRemoteSvcExecuted_thenReturnSuccess()

    到目前为止，我们已经了解了如何在 HystrixCommand 对象中封装远程服务调用。下面我们来看看如何处理远程服务开始恶化的情况。

5. 使用远程服务和防御编程

    1. 超时防御编程

        为远程服务的调用设置超时是一般的编程做法。

        让我们先来看看如何在 HystrixCommand 上设置超时，以及如何通过短路来帮助超时：

        test/.hystrix/HystrixTimeoutManualTest.java:givenSvcTimeoutOf5000AndExecTimeoutOf10000_whenRemoteSvcExecuted_thenReturnSuccess()

        在上述测试中，我们将超时设置为 500 毫秒，以延迟服务的响应。我们还将 HystrixCommand 的执行超时设置为 10,000 毫秒，从而为远程服务响应留出足够的时间。

        现在让我们看看当执行超时小于服务超时调用时会发生什么：

        ```java
        @Test(expected = HystrixRuntimeException.class)
        public void givenSvcTimeoutOf15000AndExecTimeoutOf5000_whenRemoteSvcExecuted_thenExpectHre()
        throws InterruptedException {

            HystrixCommand.Setter config = HystrixCommand
            .Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest5"));

            HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
            commandProperties.withExecutionTimeoutInMilliseconds(5_000);
            config.andCommandPropertiesDefaults(commandProperties);

            new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(15_000)).execute();
        }
        ```

        请注意我们是如何降低门槛并将执行超时设置为 5,000 毫秒的。

        我们希望服务在 5,000 毫秒内响应，而我们将服务设置为在 15,000 毫秒后响应。如果你在执行测试时注意到这一点，测试将在 5000 毫秒后退出，而不是等待 15000 毫秒，并且会抛出 HystrixRuntimeException。

        这说明 Hystrix 等待响应的时间不会超过配置的超时时间。这有助于提高受 Hystrix 保护的系统的响应速度。

        在下面的章节中，我们将探讨设置线程池大小以防止线程耗尽的问题，并讨论其好处。

    2. 使用有限线程池进行防御性编程

        为服务调用设置超时并不能解决与远程服务相关的所有问题。

        当远程服务开始响应缓慢时，典型的应用程序会继续调用该远程服务。

        应用程序不知道远程服务是否健康，而且每次请求都会产生新的线程。这将导致使用已经很吃力的服务器上的线程。

        我们不希望发生这种情况，因为我们需要这些线程用于其他远程调用或在服务器上运行的进程，而且我们还希望避免 CPU 使用率飙升。

        让我们看看如何在 HystrixCommand 中设置线程池大小：

        ```java
        @Test
        public void givenSvcTimeoutOf500AndExecTimeoutOf10000AndThreadPool_whenRemoteSvcExecuted
        _thenReturnSuccess() throws InterruptedException {

            HystrixCommand.Setter config = HystrixCommand
            .Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupThreadPool"));

            HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
            commandProperties.withExecutionTimeoutInMilliseconds(10_000);
            config.andCommandPropertiesDefaults(commandProperties);
            config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
            .withMaxQueueSize(10)
            .withCoreSize(3)
            .withQueueSizeRejectionThreshold(10));

            assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success"));
        }
        ```

        在上述测试中，我们设置了最大队列大小、核心队列大小和队列拒绝大小。当最大线程数达到 10 且任务队列达到 10 时，Hystrix 将开始拒绝请求。

        核心大小是线程池中始终保持活力的线程数量。

    3. 采用短路断路器模式的防御性编程

        不过，我们仍可对远程服务调用进行改进。

        让我们考虑一下远程服务开始失效的情况。

        我们不想继续向它发出请求，浪费资源。理想情况下，我们希望在一定时间内停止发出请求，以便让服务有时间恢复，然后再恢复请求。这就是所谓的短路断路器模式。

        让我们看看 Hystrix 是如何实现这种模式的：

        ```java
        @Test
        public void givenCircuitBreakerSetup_whenRemoteSvcCmdExecuted_thenReturnSuccess()
        throws InterruptedException {

            HystrixCommand.Setter config = HystrixCommand
            .Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupCircuitBreaker"));

            HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
            properties.withExecutionTimeoutInMilliseconds(1000);
            properties.withCircuitBreakerSleepWindowInMilliseconds(4000);
            properties.withExecutionIsolationStrategy
            (HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
            properties.withCircuitBreakerEnabled(true);
            properties.withCircuitBreakerRequestVolumeThreshold(1);

            config.andCommandPropertiesDefaults(properties);
            config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
            .withMaxQueueSize(1)
            .withCoreSize(1)
            .withQueueSizeRejectionThreshold(1));

            assertThat(this.invokeRemoteService(config, 10_000), equalTo(null));
            assertThat(this.invokeRemoteService(config, 10_000), equalTo(null));
            assertThat(this.invokeRemoteService(config, 10_000), equalTo(null));

            Thread.sleep(5000);

            assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success"));

            assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success"));

            assertThat(new RemoteServiceTestCommand(config, new RemoteServiceTestSimulator(500)).execute(),
            equalTo("Success"));
        }

        public String invokeRemoteService(HystrixCommand.Setter config, int timeout)
        throws InterruptedException {

            String response = null;

            try {
                response = new RemoteServiceTestCommand(config,
                new RemoteServiceTestSimulator(timeout)).execute();
            } catch (HystrixRuntimeException ex) {
                System.out.println("ex = " + ex);
            }

            return response;
        }
        ```

        在上述测试中，我们设置了不同的断路器属性。其中最重要的是

        - 断路器休眠窗口（CircuitBreakerSleepWindow），设置为 4,000 毫秒。这配置了断路器窗口，并定义了恢复远程服务请求的时间间隔
        - 断路器请求量阈值（CircuitBreakerRequestVolumeThreshold），设置为 1，它定义了在考虑故障率之前所需的最小请求量。

        有了上述设置，我们的 HystrixCommand 将在两次请求失败后跳闸。即使我们将服务延迟设置为 500 毫秒，第三个请求也不会触发远程服务，Hystrix 将短路，我们的方法将返回空值作为响应。

        随后，我们将添加 Thread.sleep(5000)，以超过我们设置的睡眠窗口限制。这将导致 Hystrix 关闭电路，随后的请求将成功通过。

6. 结论

    总之，Hystrix 的设计目的是

    - 对通常通过网络访问的服务的故障和延迟提供保护和控制
    - 阻止因部分服务宕机而导致的连锁故障
    - 快速故障和快速恢复
    - 尽可能从容降级
    - 实时监控并向指挥中心发出故障警报

    在下一篇文章中，我们将介绍如何将 Hystrix 的优势与 Spring 框架相结合。
