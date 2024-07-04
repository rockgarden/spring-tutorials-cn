# 核心Java并发高级示例

本模块包含有关核心 Java 多线程高级主题的文章。

- [ ] [Java线程池简介](#java中的线程池简介)
- [Java 中的 CountDownLatch 指南](https://www.baeldung.com/java-countdown-latch)
- [java.util.concurrent.Locks 指南](https://www.baeldung.com/java-concurrent-locks)
- [Java 中的 ThreadLocal 简介](https://www.baeldung.com/java-threadlocal)
- [Java 中的 LongAdder 和 LongAccumulator](https://www.baeldung.com/java-longadder-and-longaccumulator)
- [Java 中的餐饮哲学家问题](https://www.baeldung.com/java-dining-philoshophers)
- [Java Phaser 指南](https://www.baeldung.com/java-phaser)
- [Java 中的原子变量简介](https://www.baeldung.com/java-atomic-variables)
- [Java 中的循环屏障](https://www.baeldung.com/java-cyclic-barrier)
- 更多文章： [[下一页-->]](/core-java-modules/core-java-concurrency-advanced-2)

## Java中的线程池简介

1. 概述

    本教程将介绍 Java 中的线程池。我们将从标准 Java 库中的不同实现开始，然后介绍 Google 的 Guava 库。

2. 线程池

    在 Java 中，线程被映射到系统级线程，也就是操作系统的资源。如果我们无节制地创建线程，这些资源可能会很快耗尽。

    操作系统还会在线程之间进行上下文切换--以模拟并行性。简单地说，我们产生的线程越多，每个线程用于实际工作的时间就越少。

    线程池模式有助于在多线程应用程序中节省资源，并将并行性控制在一定的预定义范围内。

    使用线程池时，我们以并行任务的形式编写并发代码，并将其提交给线程池的一个实例执行。这个实例会控制几个重复使用的线程来执行这些任务。

    ![2016-08-10_10-16-52-1024x572](pic/2016-08-10_10-16-52-1024x572.png)

    该模式允许我们控制应用程序创建的线程数量及其生命周期。我们还能安排任务的执行，并将接收到的任务保留在队列中。

3. Java 中的线程池

    1. 执行器、执行器和执行器服务

        Executors 辅助类包含多个用于创建预配置线程池实例的方法。这些类是一个很好的开始。如果我们不需要进行任何自定义微调，就可以使用它们。

        我们使用 Executor 和 ExecutorService 接口来处理 Java 中不同的线程池实现。通常，我们应将代码与线程池的实际实现解耦，并在整个应用程序中使用这些接口。

        1. 执行器

            Executor 接口有一个执行方法，用于提交 Runnable 实例以供执行。

            让我们看一个快速示例，了解如何使用 Executors API 获取由单线程池和无界队列支持的 Executor 实例，以便按顺序执行任务。

            在这里，我们运行一个简单的任务，在屏幕上打印 "Hello World"。我们将以 lambda（Java 8 的一个特性）的形式提交任务，它被推断为 Runnable：

            ```java
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> System.out.println("Hello World"));
            ```

        2. 执行器服务

            ExecutorService 接口包含大量用于控制任务进度和管理服务终止的方法。使用该接口，我们可以提交任务以供执行，还可以使用返回的 Future 实例控制任务的执行。

            现在，我们将创建一个 ExecutorService，提交一项任务，然后使用返回的 Future 的 get 方法等待提交的任务完成并返回值：

            ```java
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            Future<String> future = executorService.submit(() -> "Hello World");
            // some operations
            String result = future.get();
            ```

            当然，在现实生活中，我们通常不会立即调用 future.get()，而是推迟到真正需要计算值时再调用。

            在这里，我们重载了提交方法，使其可以使用 Runnable 或 Callable。这两种方法都是功能接口，我们可以将它们作为 lambdas 传递（从 Java 8 开始）。

            Runnable 的单一方法不会抛出异常，也不会返回值。Callable 接口可能更方便，因为它允许我们抛出异常并返回值。

            最后，要让编译器推断 Callable 类型，只需从 lambda 返回一个值即可。

            有关使用 ExecutorService 接口和期货的更多示例，请参阅《[Java ExecutorService 指南](https://www.baeldung.com/java-executor-service-tutorial)》。

    2. 线程池执行器

        ThreadPoolExecutor 是一种可扩展的线程池实现，具有大量用于微调的参数和钩子。

        我们在此讨论的主要配置参数是 corePoolSize、maximumPoolSize 和 keepAliveTime。

        核心线程池由固定数量的核心线程组成，这些线程始终保持在池内。它还包括一些过度线程，这些线程可能会被生成，然后在不再需要时被终止。

        corePoolSize 参数是将被实例化并保存在池中的核心线程数量。当有新任务进入时，如果所有核心线程都很忙且内部队列已满，则允许池增长到 maximumPoolSize。

        keepAliveTime 参数是允许过多线程（实例化的线程数超过 corePoolSize）处于空闲状态的时间间隔。默认情况下，ThreadPoolExecutor 只考虑删除非核心线程。为了对核心线程应用相同的移除策略，我们可以使用 [allowCoreThreadTimeOut(true)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html#allowCoreThreadTimeOut(boolean)) 方法。

        这些参数涵盖了广泛的使用情况，但最典型的配置是在执行器静态方法中预定义的。

        1. newFixedThreadPool

            newFixedThreadPool 方法创建了一个线程池执行器（ThreadPoolExecutor），其 corePoolSize 和 maximumPoolSize 参数值相等，keepAliveTime 为零。这意味着该线程池中的线程数始终保持不变：

            CoreThreadPoolIntegrationTest.java\whenUsingFixedThreadPool_thenCoreAndMaximumThreadSizeAreTheSame()

            在这里，我们实例化了一个线程数固定为 2 的 ThreadPoolExecutor，这意味着如果同时运行的任务数总是小于或等于 2，它们就会立即被执行。否则，其中一些任务可能会被放入队列等待轮到它们。

            我们创建了三个 Callable 任务，它们通过休眠 1000 毫秒来模仿繁重的工作。前两个任务将立即运行，第三个任务将在队列中等待。我们可以在提交任务后立即调用 getPoolSize() 和 getQueue().size() 方法来验证。

        2. Executors.newCachedThreadPool()

            我们可以使用 Executors.newCachedThreadPool() 方法创建另一个预配置的 ThreadPoolExecutor。该方法根本不会接收线程数。我们将 corePoolSize 设置为 0，将 maximumPoolSize 设置为 Integer.MAX_VALUE。最后，keepAliveTime 为 60 秒：

            CoreThreadPoolIntegrationTest.java\whenUsingCachedThreadPool_thenPoolSizeGrowsUnbounded()

            这些参数值意味着缓存线程池可以无限制地增长，以容纳任意数量的已提交任务。但是，如果不再需要这些线程，它们将在 60 秒闲置后被处理掉。一个典型的使用案例是，当我们的应用程序中存在大量短期任务时。

            队列大小始终为零，因为内部使用的是 SynchronousQueue 实例。在同步队列中，插入和移除操作总是同时进行。因此，队列实际上从未包含任何内容。

        3. Executors.newSingleThreadExecutor()

            Executors.newSingleThreadExecutor() API 创建了另一种包含单线程的典型 ThreadPoolExecutor。单线程执行器是创建事件循环的理想选择。corePoolSize 和 maximumPoolSize 参数均为 1，keepAliveTime 为 0。

            上例中的任务将按顺序运行，因此任务完成后标志值将为 2：

            whenUsingSingleThreadPool_thenTasksExecuteSequentially()

            此外，该 ThreadPoolExecutor 使用不可变封装器装饰，因此创建后无法重新配置。请注意，这也是我们无法将其转换为 ThreadPoolExecutor 的原因。

    3. 调度线程池执行器

        ScheduledThreadPoolExecutor 扩展了 ThreadPoolExecutor 类，同时还实现了 ScheduledExecutorService 接口和几个附加方法：

        - schedule 方法允许我们在指定延迟后运行一次任务。
        - scheduleAtFixedRate 方法允许我们在指定的初始延迟后运行任务，然后在一定周期内重复运行。周期参数是任务开始时间之间的测量时间，因此执行率是固定的。
        - scheduleWithFixedDelay 方法与 scheduleAtFixedRate 类似，都是重复运行给定的任务，但指定的延迟时间是上一个任务结束到下一个任务开始之间的时间。执行率可能会根据运行任何给定任务所需的时间而变化。
        我们通常使用 Executors.newScheduledThreadPool() 方法创建一个 ScheduledThreadPoolExecutor，它具有给定的 corePoolSize、无限制的 maximumPoolSize 和零 keepAliveTime。

        下面是如何在 500 毫秒内调度任务的执行：

        ```java
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
        executor.schedule(() -> {
            System.out.println("Hello World");
        }, 500, TimeUnit.MILLISECONDS);
        ```

        下面的代码展示了如何在延迟 500 毫秒后运行任务，然后每 100 毫秒重复一次。调度任务后，我们使用 CountDownLatch 锁等待任务启动三次。然后使用 Future.cancel() 方法取消任务：

        whenSchedulingTaskWithFixedPeriod_thenTaskExecutesMultipleTimes()

    4. ForkJoinPool

        ForkJoinPool 是 Java 7 中引入的 fork/join 框架的核心部分。它解决了递归算法中产生多个任务的常见问题。我们使用简单的 ThreadPoolExecutor 会很快耗尽线程，因为每个任务或子任务都需要自己的线程才能运行。

        在 fork/join 框架中，任何任务都可以产生（fork）若干子任务，并使用 join 方法等待它们完成。fork/join 框架的好处是，它不会为每个任务或子任务创建一个新线程，而是执行工作偷取(work-stealing)算法。我们在《[Java中的fork/join框架指南](https://www.baeldung.com/java-fork-join)》中对该框架进行了详细介绍。

        让我们来看一个使用 ForkJoinPool 遍历节点树并计算所有叶子值之和的简单示例。下面是由一个节点、一个 int 值和一组子节点组成的树的简单实现：

        threadpool/TreeNode.java

        现在，如果我们想并行求和树中的所有值，就需要实现 `RecursiveTask<Integer>` 接口。每个任务接收自己的节点，并将其值与其子节点的值之和相加。为了计算子节点值的总和，任务实现需要执行以下操作：

        - 流式处理子节点集
        - 映射该流，为每个元素创建一个新的 CountingTask
        - 通过分叉运行每个子任务
        - 通过调用每个分叉任务的 join 方法来收集结果
        - 使用 Collectors.summingInt 收集器对结果求和

        threadpool/CountingTask.java

        在实际树上运行计算的代码非常简单：whenUsingForkJoinPool_thenSumOfTreeElementsIsCalculatedCorrectly()

4. 线程池在 Guava 中的实现

    Guava 是 Google 的一个流行工具库。它有许多有用的并发类，包括 ExecutorService 的几个方便的实现。这些实现类无法直接实例化或子类化，因此创建其实例的唯一入口是 MoreExecutors 辅助类。

    1. 将 Guava 添加为 Maven 依赖项

        我们在 Maven pom 文件中添加以下依赖项，以便在项目中包含 Guava 库。在 Maven 中央资源库中查找最新版本的 Guava 库：com.google.guava/guava。

    2. 直接执行器和直接执行器服务

        有时，我们希望根据某些条件在当前线程或线程池中运行任务。我们倾向于使用单一的 Executor 接口，只需切换实现即可。虽然在当前线程中运行任务的 Executor 或 ExecutorService 的实现并不难，但这仍然需要编写一些模板代码。

        值得庆幸的是，Guava 为我们提供了预定义的实例。

        下面是一个在同一线程中执行任务的示例。虽然所提供的任务会休眠 500 毫秒，但它会阻塞当前线程，执行调用完成后，结果立即可用：

        GuavaThreadPoolIntegrationTest.java\whenExecutingTaskWithDirectExecutor_thenTheTaskIsExecutedInTheCurrentThread()

        directExecutor() 方法返回的实例实际上是一个静态单例，因此使用该方法不会对对象创建造成任何开销。

        与 MoreExecutors.newDirectExecutorService() 方法相比，我们更倾向于使用该方法，因为该 API 会在每次调用时创建一个完整的执行器服务实现。

    3. 退出执行程序服务

        另一个常见问题是在线程池仍在运行任务时关闭虚拟机。即使有取消机制，也不能保证任务会乖乖地在执行器服务关闭时停止工作。这可能会导致 JVM 在任务继续工作时无限期地挂起。

        为了解决这个问题，Guava 引入了一系列退出执行器服务。它们基于与 JVM 一起终止的守护进程线程。

        这些服务还会使用 Runtime.getRuntime().addShutdownHook() 方法添加一个关闭钩子，并在放弃挂起的任务之前阻止虚拟机终止一段配置的时间。

        在下面的示例中，我们提交了包含无限循环的任务，但我们使用了退出执行器服务，并配置了 100 毫秒的时间，以便在虚拟机终止时等待任务。

        ExitingExecutorServiceExample.java\main()

        如果没有 exitingExecutorService，该任务将导致虚拟机无限期挂起。

    4. 监听装饰器

        监听装饰器允许我们封装 ExecutorService，并在任务提交时接收 ListenableFuture 实例，而不是简单的 Future 实例。ListenableFuture 接口扩展了 Future，并多了一个 addListener 方法。该方法允许添加在未来完成时调用的监听器。

        我们很少会直接使用 ListenableFuture.addListener() 方法。但它对 Futures 实用工具类中的大多数辅助方法都至关重要。

        例如，通过 Futures.allAsList() 方法，我们可以将多个 ListenableFuture 实例合并为一个 ListenableFuture，并在所有futures合并成功后完成：

        whenJoiningFuturesWithAllAsList_thenCombinedFutureCompletesAfterAllFuturesComplete()

5. 结论

    本文讨论了线程池模式及其在标准 Java 库和 Google Guava 库中的实现。

## Code

本文的源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-concurrency-advanced) 上获取。
