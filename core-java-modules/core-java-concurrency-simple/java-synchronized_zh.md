# [Java 中 synchronized 关键字指南](https://www.baeldung.com/java-synchronized)

Java 并发定义 | Java 并发基础 | Java 关键字

1. 概述

    在本文中，我们将学习如何在 Java 中使用 `synchronized` 块。

    简而言之，在多线程环境中，当两个或多个线程同时尝试更新可变的共享数据时，就会发生**竞态条件（race condition）**。Java 提供了一种机制，通过同步线程对共享数据的访问来避免竞态条件。

    被 `synchronized` 标记的代码块称为**同步块**，确保在任意时刻只有一个线程可以执行该代码块。

2. 为什么需要同步？

    考虑一个典型的竞态条件场景：我们计算总和，多个线程同时执行 `calculate()` 方法：

    ```java
    public class SynchronizedMethods {

        private int sum = 0;

        public void calculate() {
            setSum(getSum() + 1);
        }

        // 标准的 setter 和 getter 方法
    }
    ```

    然后编写一个简单测试：

    ```java
    @Test
    public void givenMultiThread_whenNonSyncMethod() {
        ExecutorService service = Executors.newFixedThreadPool(3);
        SynchronizedMethods summation = new SynchronizedMethods();

        IntStream.range(0, 1000)
        .forEach(count -> service.submit(summation::calculate));
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);

        assertEquals(1000, summation.getSum());
    }
    ```

    我们使用一个包含 3 个线程的 `ExecutorService` 池，执行 1000 次 `calculate()` 方法。

    如果串行执行，预期输出应为 1000，但多线程执行几乎每次都失败，实际输出不一致：

    ```log
    java.lang.AssertionError: expected:<1000> but was:<965>
    at org.junit.Assert.fail(Assert.java:88)
    at org.junit.Assert.failNotEquals(Assert.java:834)
    ...
    ```

    当然，这个结果并不意外。

    避免竞态条件的一个简单方法是使用 `synchronized` 关键字使操作线程安全。

3. synchronized 关键字

    我们可以在不同层级使用 `synchronized` 关键字：

    - 实例方法
    - 静态方法
    - 代码块

    当我们使用 `synchronized` 块时，Java 内部使用一个**监视器（monitor）**（也称监视器锁或内置锁）来实现同步。这些监视器绑定到某个对象，因此同一个对象的所有同步块，在任意时刻只能有一个线程执行。

    1. 同步实例方法

        我们可以在方法声明中添加 `synchronized` 关键字，使其成为同步方法：

        ```java
        public synchronized void synchronisedCalculate() {
            setSum(getSum() + 1);
        }
        ```

        注意：一旦方法被同步，测试用例将通过，实际输出为 1000：

        ```java
        @Test
        public void givenMultiThread_whenMethodSync() {
            ExecutorService service = Executors.newFixedThreadPool(3);
            SynchronizedMethods method = new SynchronizedMethods();

            IntStream.range(0, 1000)
                .forEach(count -> service.submit(method::synchronisedCalculate));
            service.awaitTermination(1000, TimeUnit.MILLISECONDS);

            assertEquals(1000, method.getSum());
        }
        ```

        **实例方法**的同步是基于**类实例对象**的，这意味着每个类实例在同一时间只能有一个线程执行该同步方法。

    2. 同步静态方法

        静态方法也可以像实例方法一样同步：

        ```java
        public static synchronized void syncStaticCalculate() {
            staticSum = staticSum + 1;
        }
        ```

        这些方法是基于**类对象（Class object）**进行同步的。由于每个 JVM 中每个类只有一个 Class 对象，因此无论该类有多少实例，每个类在同一时间只能有一个线程执行其静态同步方法。

        测试如下：

        ```java
        @Test
        public void givenMultiThread_whenStaticSyncMethod() {
            ExecutorService service = Executors.newCachedThreadPool();

            IntStream.range(0, 1000)
            .forEach(count ->
                service.submit(SynchronizedMethods::syncStaticCalculate));
            service.awaitTermination(100, TimeUnit.MILLISECONDS);

            assertEquals(1000, SynchronizedMethods.staticSum);
        }
        ```

    3. 方法内的同步代码块

        有时我们并不想同步整个方法，而只想同步其中一部分代码。这可以通过在代码块上使用 `synchronized` 实现：

        ```java
        public void performSynchronisedTask() {
            synchronized (this) {
                setCount(getCount() + 1);
            }
        }
        ```

        测试修改后的代码：

        ```java
        @Test
        public void givenMultiThread_whenBlockSync() {
            ExecutorService service = Executors.newFixedThreadPool(3);
            SynchronizedBlocks synchronizedBlocks = new SynchronizedBlocks();

            IntStream.range(0, 1000)
                .forEach(count ->
                    service.submit(synchronizedBlocks::performSynchronisedTask));
            service.awaitTermination(100, TimeUnit.MILLISECONDS);

            assertEquals(1000, synchronizedBlocks.getCount());
        }
        ```

        注意：我们向 `synchronized` 块传入了参数 `this`，这就是**监视器对象**。块内的代码基于该监视器对象进行同步。简单来说，每个监视器对象在同一时间只允许一个线程执行其同步块内的代码。

        如果方法是静态的，我们应传入类名（如 `SynchronisedBlocks.class`），此时**类对象**将作为该同步块的监视器：

        ```java
        public static void performStaticSyncTask(){
            synchronized (SynchronisedBlocks.class) {
                setStaticCount(getStaticCount() + 1);
            }
        }
        ```

        测试静态方法中的同步块：

        ```java
        @Test
        public void givenMultiThread_whenStaticSyncBlock() {
            ExecutorService service = Executors.newCachedThreadPool();

            IntStream.range(0, 1000)
            .forEach(count ->
                service.submit(SynchronizedBlocks::performStaticSyncTask));
            service.awaitTermination(100, TimeUnit.MILLISECONDS);

            assertEquals(1000, SynchronizedBlocks.getStaticCount());
        }
        ```

    4. 可重入性（Reentrancy）

        `synchronized` 方法和代码块背后的锁是**可重入的**。这意味着当前线程在持有锁的情况下，可以多次重新获取同一把锁：

        ```java
        Object lock = new Object();
        synchronized (lock) {
            System.out.println("第一次获取锁");

            synchronized (lock) {
                System.out.println("再次进入");

                synchronized (lock) {
                    System.out.println("又一次进入");
                }
            }
        }
        ```

        如上所示，在同步块内部，我们可以反复获取同一个监视器锁。

4. 结论

    在本简短文章中，我们探讨了使用 `synchronized` 关键字实现线程同步的不同方式。

    我们还学习了竞态条件如何影响应用程序，以及同步机制如何帮助我们避免此类问题。
