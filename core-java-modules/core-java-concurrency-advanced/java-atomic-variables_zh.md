# [Java 中原子变量简介](https://www.baeldung.com/java-atomic-variables)

Java 并发定义 | Java 并发基础

1. 引言

    简而言之，在涉及并发时，共享的可变状态极易引发问题。如果对共享可变对象的访问未得到妥善管理，应用程序将很快出现难以检测的并发错误。

    在本文中，我们将回顾使用锁来处理并发访问的方法，探讨锁的一些缺点，并最终引入原子变量作为替代方案。

2. 锁机制

    让我们先看以下类：

    ```java
    public class Counter {
        int counter;

        public void increment() {
            counter++;
        }
    }
    ```

    在单线程环境中，该代码运行完美；但一旦允许多个线程同时写入，就会出现结果不一致的问题。

    这是因为看似原子操作的简单自增语句（`counter++`），实际上由三个操作组成：读取当前值、加一、将新值写回内存。

    如果两个线程同时尝试读取并更新该值，可能导致更新丢失。

    管理对象访问的一种方法是使用锁。这可以通过在 `increment` 方法签名中添加 `synchronized` 关键字实现。`synchronized` 关键字确保同一时间只有一个线程可以进入该方法（更多关于锁和同步的内容，请参阅《Java 中 synchronized 关键字指南》）：

    ```java
    public class SafeCounterWithLock {
        private int counter;

        public synchronized void increment() {
            counter++;
        }
    }
    ```

    使用锁确实解决了问题，但性能会受到影响。

    当多个线程尝试获取锁时，只有一个线程成功，其余线程会被阻塞或挂起。

    挂起线程再恢复的过程开销很大，会降低系统整体效率。

    在像计数器这样的小程序中，上下文切换所花费的时间可能远超实际代码执行时间，从而显著降低整体效率。

3. 原子操作

    有一类研究专注于为并发环境创建无锁算法。这些算法利用底层原子机器指令（如比较并交换 CAS）来保证数据完整性。

    典型的 CAS 操作涉及三个操作数：

    - **M**：操作的内存位置
    - **A**：变量当前的预期值
    - **B**：需要设置的新值

    CAS 操作仅在内存位置 M 的当前值等于 A 时，才原子性地将 M 更新为 B；否则不执行任何操作。

    无论是否更新成功，CAS 都会返回 M 的当前值。这将“读取值、比较值、更新值”三个步骤合并为一个机器级原子操作。

    当多个线程通过 CAS 尝试更新同一值时，其中一个线程胜出并完成更新。但与锁不同，其他线程不会被挂起，而是被告知未能成功更新值。这些线程可以继续执行其他工作，完全避免了上下文切换。

    另一个后果是，核心程序逻辑变得更加复杂。这是因为我们必须处理 CAS 操作失败的情况。根据具体用例，我们可以不断重试直到成功，或者直接放弃并继续执行后续操作。

4. Java 中的原子变量

    Java 中最常用的原子变量类包括：[AtomicInteger](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)、[AtomicLong](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicLong.html)、[AtomicBoolean](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicBoolean.html) 和 [AtomicReference](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicReference.html)。它们分别表示可原子更新的 `int`、`long`、`boolean` 和对象引用。这些类主要提供以下方法：

    - **get()** – 从内存中获取值，确保其他线程的修改可见；等同于读取一个 `volatile` 变量。
    - **incrementAndGet()** – 原子地将当前值加一并返回新值。
    - **set()** – 将值写入内存，确保其他线程可见；等同于写入一个 `volatile` 变量。
    - **lazySet()** – 最终将值写入内存，可能与其他相关内存操作重排序。典型用例是为便于垃圾回收而置空不再使用的引用。通过延迟执行 volatile 写入可获得更好的性能。
    - **compareAndSet()** – 与第 3 节描述相同，成功时返回 `true`，否则返回 `false`。
    - **weakCompareAndSet()** – 与第 3 节描述相同，但语义更弱，不会创建 happens-before 顺序。这意味着它可能无法看到其他变量的更新。自 [Java 9](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html#weakCompareAndSet(int,int)) 起，所有原子类实现中已弃用此方法，推荐使用 `weakCompareAndSetPlain()`。因为 `weakCompareAndSet()` 的内存语义实际上是“plain”的，但名称却暗示了“volatile”语义，容易造成混淆。为避免混淆，官方弃用了该方法，并新增了四种具有不同内存语义的方法，如 `weakCompareAndSetPlain()` 或 [weakCompareAndSetVolatile()](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html#weakCompareAndSetVolatile(int,int))。

    下面示例展示了一个使用 `AtomicInteger` 实现的线程安全计数器：

    ```java
    public class SafeCounterWithoutLock {
        private final AtomicInteger counter = new AtomicInteger(0);

        int getValue() {
            return counter.get();
        }

        void increment() {
            counter.incrementAndGet();
        }
    }
    ```

    如你所见，我们使用了 `incrementAndGet()` 方法，它等效于一个同步代码块：获取当前值 → 加一 → 将新值赋给计数器变量 → 最终写回内存。

5. 结论

    在本快速教程中，我们介绍了一种避免锁机制相关缺点的并发处理替代方案。同时，我们也了解了 Java 原子变量类提供的主要方法。
