# Java时间测量

本模块包含有关 Java 时间测量的文章。

- [Guide to the Java Clock Class](http://www.baeldung.com/java-clock)
- [Measure Elapsed Time in Java](http://www.baeldung.com/java-measure-elapsed-time)
- [Overriding System Time for Testing in Java](https://www.baeldung.com/java-override-system-time)
- [x] [Java Timer](#java计时器)

## Java计时器

1. 定时器 - 基础知识

    Timer 和 TimerTask 是我们用来在后台线程中调度任务的 java util 类。基本上，TimerTask 是要执行的任务，而 [Timer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Timer.html) 是调度器。

    示例代码：JavaTimerLongRunningUnitTest.java

2. 调度一次任务

    1. 在给定延迟后

        首先，让我们借助计时器运行一个任务：

        givenUsingTimer_whenSchedulingTaskOnce_thenCorrect()

        这将在一定延迟后执行任务，我们将该延迟作为 schedule() 方法的第二个参数。

        请注意，如果我们将此作为 JUnit 测试运行，则应添加 Thread.sleep(delay * 2) 调用，以便让计时器的线程在 Junit 测试停止执行前运行任务。

    2. 在给定日期和时间

        现在让我们来看看 Timer#schedule(TimerTask, Date) 方法，它的第二个参数是 Date 而不是 long。这样我们就可以在某一时刻而不是延迟后安排任务。

        这一次，假设我们有一个旧的传统数据库，并希望将其数据迁移到一个具有更好模式的新数据库中。

        我们可以创建一个 DatabaseMigrationTask 类来处理这一迁移：

        DatabaseMigrationTask.java

        为了简单起见，我们用字符串列表来表示两个数据库。简单地说，我们的迁移就是将第一个列表中的数据放入第二个列表中。

        示例代码：DatabaseMigrationTaskUnitTest.java

        要在所需的时刻执行迁移，我们必须使用重载版本的 schedule() 方法：

        givenDatabaseMigrationTask_whenTimerScheduledForNowPlusTwoSeconds_thenDataMigratedAfterTwoSeconds()

        我们可以看到，我们向 schedule() 方法提供了迁移任务和执行日期。

        然后，迁移将在 twoSecondsLater 所指示的时间执行，在此之前，迁移不会发生。

3. 安排可重复任务

    在介绍了如何安排任务的单次执行后，我们来看看如何处理可重复任务。

    定时器类再次提供了多种可能性。我们可以将重复设置为遵守固定延迟或固定速率。

    固定延迟意味着执行将在上次执行开始后的一段时间后开始，即使它被延迟了（因此它本身也被延迟了）。

    假设我们想每两秒安排一次任务，第一次执行需要一秒，第二次执行需要两秒，但延迟一秒。那么第三次执行将在第五秒开始：

    ```log
    0s     1s    2s     3s           5s
    |--T1--|
    |-----2s-----|--1s--|-----T2-----|
    |-----2s-----|--1s--|-----2s-----|--T3--|
    ```

    另一方面，固定速率意味着每次执行都将遵守初始计划，无论之前的执行是否被延迟。

    让我们重复前面的例子。在固定速率下，第二个任务将在三秒后启动（因为延迟），但第三个任务将在四秒后启动（遵守每两秒执行一次的初始计划）：

    ```log
    0s     1s    2s     3s    4s
    |--T1--|       
    |-----2s-----|--1s--|-----T2-----|
    |-----2s-----|-----2s-----|--T3--|
    ```

    既然我们已经介绍了这两个原则，让我们看看如何使用它们。

    要使用固定延迟调度，schedule() 方法还有两个重载，每个重载都需要一个额外的参数，说明以毫秒为单位的周期。

    为什么是**两个重载**？因为仍有可能在某个时刻或某个延迟后启动任务。

    至于固定速率调度，我们有两个 scheduleAtFixedRate() 方法，它们也以毫秒为周期。同样，我们有一个方法用于在给定日期和时间启动任务，另一个方法用于在给定延迟后启动任务。

    值得一提的是，如果一个任务的执行时间超过了周期，那么无论我们使用的是固定延迟还是固定速率，都会延迟整个执行链。

    测试代码：NewsletterTaskUnitManualTest.java

    1. 使用固定延迟

        现在，让我们设想一下，我们想要实现一个新闻通讯系统，每周向我们的追随者发送一封电子邮件。在这种情况下，重复性任务似乎最为理想。

        因此，让我们安排每秒发送一次时事通讯，这基本上就是垃圾邮件，但由于发送是假的，所以我们可以放心大胆地去做。

        首先，我们将设计一个 NewsletterTask：

        NewsletterTask.java

        每次执行任务时，任务都会打印其计划时间，我们使用 TimerTask#scheduledExecutionTime() 方法收集该时间。

        那么，如果我们想在固定延迟模式下每秒调度一次该任务，该怎么办呢？我们必须使用前面提到的重载版 schedule()：

        `new Timer().schedule(new NewsletterTask(), 0, 1000);`

        当然，我们只对少数情况进行测试：

        ```log
        Email sent at: 2023-09-10T10:58:41.765
        The duration of sending the mail will took: 6
        Email sent at: 2023-09-10T10:58:47.781
        The duration of sending the mail will took: 2
        ...
        ```

        我们可以看到，每次执行之间至少间隔一秒，但有时会延迟一毫秒。造成这种现象的原因是我们决定使用固定延迟重复。

    2. 使用固定速率

        现在，如果我们使用固定速率重复呢？那么我们就必须使用 scheduledAtFixedRate() 方法：

        `new Timer().scheduleAtFixedRate(new NewsletterTask(), 0, 1000);`

        这一次，执行时间没有被之前的延迟：

        ```log
        Email sent at: 2023-09-10T11:00:20.718
        The duration of sending the mail will took: 1
        Email sent at: 2023-09-10T11:00:21.718
        The duration of sending the mail will took: 6
        ...
        ```

        从这里我们可以看出固定费率和计划费率的区别。

    3. 安排每日任务

        接下来，让我们每天运行一次任务：

        JavaTimerLongRunningUnitTest.java\givenUsingTimer_whenSchedulingDailyTask_thenCorrect()

4. 取消定时器和定时任务

    可以通过几种方式取消任务的执行。

    1. 在运行过程中取消定时器任务

        第一种方法是在 TimerTask 本身的 run() 方法实现中调用 TimerTask.cancel() 方法：

        givenUsingTimer_whenCancelingTimerTask_thenCorrect()

    2. 取消计时器

        另一种方法是在定时器对象上调用 Timer.cancel() 方法：

        givenUsingTimer_whenCancelingTimer_thenCorrect()

    3. 在运行中停止 TimerTask 的线程

        我们还可以在任务的运行方法中停止线程，从而取消整个任务：

        givenUsingTimer_whenStoppingThread_thenTimerTaskIsCancelled()

        注意运行实现中的 TODO 指令；为了运行这个简单的示例，我们需要实际停止线程。

        在现实世界的自定义线程实现中，应该支持停止线程，但在本例中，我们可以忽略弃用，使用线程类本身的简单停止 API。

5. 计时器与 ExecutorService

    我们还可以充分利用 ExecutorService 来调度定时器任务，而不是使用定时器。

    下面是一个在指定时间间隔运行重复任务的快速示例：

    givenUsingExecutorService_whenSchedulingRepeatedTask_thenCorrect()

    那么，定时器和 ExecutorService 解决方案的主要区别是什么呢？

    - 定时器会对系统时钟的变化很敏感；而 ScheduledThreadPoolExecutor 则不会。
    - 定时器只有一个执行线程；而 ScheduledThreadPoolExecutor 可以配置任意数量的线程。
    - 在 TimerTask 中抛出的运行时异常会杀死线程，因此后续的计划任务不会继续运行；而在 ScheduledThreadExecutor 中，当前任务会被取消，但其他任务会继续运行。

6. 总结

    在本文中，我们介绍了使用 Java 内置的简单而灵活的 Timer 和 TimerTask 基础架构来快速调度任务的多种方法。当然，如果我们需要，Java 世界中还有更复杂、更完整的解决方案，例如 [Quartz](http://quartz-scheduler.org/) 库，但这是一个很好的开始。

## Code

这些示例的实现可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-time-measurements) 上找到。

## 相关文章

- [Guide to the Java Clock Class](http://www.baeldung.com/java-clock)
- [Measure Elapsed Time in Java](http://www.baeldung.com/java-measure-elapsed-time)
- [Overriding System Time for Testing in Java](https://www.baeldung.com/java-override-system-time)
- [Java Timer](http://www.baeldung.com/java-timer-and-timertask)
