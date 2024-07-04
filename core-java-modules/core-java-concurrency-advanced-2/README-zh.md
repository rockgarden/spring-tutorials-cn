# 核心Java并发高级示例

本模块包含有关核心 Java 多线程高级主题的文章。

- Java 中的 Semaphores](https://www.baeldung.com/java-semaphore)
- [ ] [Java中的守护线程](#java中的守护线程)
- Java 中基于优先级的作业调度](https://www.baeldung.com/java-priority-job-schedule)
- Java Thread.yield() 简介](https://www.baeldung.com/java-thread-yield)
- 使用 2 个线程打印偶数和奇数](https://www.baeldung.com/java-even-odd-numbers-with-2-threads)
- Java CyclicBarrier 与 CountDownLatch](https://www.baeldung.com/java-cyclicbarrier-countdownlatch)
- Java 中的 Fork/Join 框架指南](https://www.baeldung.com/java-fork-join)
- Java 中的 ThreadLocalRandom 指南](https://www.baeldung.com/java-thread-local-random)
- 向 Java 线程传递参数](https://www.baeldung.com/java-thread-parameters)

[[<--上一页]](/core-java-concurrency-advanced/README-zh.md) [[下一页-->]](/core-java-concurrency-advanced-3)

## Java中的守护线程

1. 概述

    在这篇短文中，我们将了解 Java 中的守护线程，并看看它们可以用来做什么。我们还将解释守护线程与用户线程的区别。

2. 守护进程线程与用户线程的区别

    Java 提供两种线程：用户线程和守护进程线程。

    用户线程是高优先级线程。JVM 会等待用户线程完成任务后再终止它。

    另一方面，守护线程是低优先级线程，其唯一作用是为用户线程提供服务。

    由于守护线程的目的是为用户线程提供服务，而且只在用户线程运行时才需要守护线程，因此当所有用户线程都执行完毕后，守护线程不会阻止 JVM 退出。

    这就是通常存在于守护进程中的无限循环不会造成问题的原因，因为一旦所有用户线程都执行完毕，包括最终代码块在内的任何代码都不会被执行。因此，不建议在 I/O 任务中使用守护线程。

    不过，这条规则也有例外。守护进程线程中设计不当的代码会阻止 JVM 退出。例如，在运行中的守护进程线程上调用 Thread.join()，可能会阻止应用程序的关闭。

3. 守护线程的用途

    守护线程可用于后台支持任务，如垃圾回收、释放未使用对象的内存以及从缓存中删除不需要的条目。大多数 JVM 线程都是守护线程。

4. 创建守护进程线程

    要将线程设置为守护线程，我们只需调用 Thread.setDaemon()。在本例中，我们将使用扩展了线程类的 NewThread 类：

    ```java
    NewThread daemonThread = new NewThread();
    daemonThread.setDaemon(true);
    daemonThread.start();
    ```

    任何线程都会继承创建它的线程的守护进程状态。由于主线程是用户线程，因此在 main 方法中创建的任何线程默认都是用户线程。

    setDaemon()方法只能在创建了线程对象且线程尚未启动的情况下调用。如果试图在线程运行时调用 setDaemon()，将抛出 IllegalThreadStateException 异常：

    givenUserThread_whenSetDaemonWhileRunning_thenIllegalThreadStateException()

5. 检查线程是否是守护进程线程

    最后，要检查线程是否是守护进程线程，我们只需调用 isDaemon() 方法即可：

    whenCallIsDaemon_thenCorrect()
6. 结论

    在这个快速教程中，我们已经了解了什么是守护进程线程，以及它们在一些实际场景中的用途。

## Code

完整版代码可在 GitHub 上获取。
