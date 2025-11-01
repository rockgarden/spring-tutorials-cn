# [sun.misc.Unsafe 使用指南](https://www.baeldung.com/java-unsafe)

1. 概述

    本文将介绍 JRE 提供的一个非常有趣的类 —— `sun.misc.Unsafe`。  
    该类提供了底层机制，**原本仅设计供 Java 核心库内部使用，而非普通开发者**。

    它主要为 Java 核心库提供低级别的内存和线程操作能力。

2. 获取 Unsafe 实例

    要使用 `Unsafe` 类，首先需要获取其实例。但由于该类专为内部使用设计，**直接调用其静态方法 `getUnsafe()` 会抛出 `SecurityException`**。

    不过，我们可以通过反射绕过限制：

    ```java
    Field f = Unsafe.class.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    Unsafe unsafe = (Unsafe) f.get(null);
    ```

3. 使用 Unsafe 实例化类

    假设我们有一个简单类，其构造函数会初始化一个字段：

    ```java
    class InitializationOrdering {
        private long a;

        public InitializationOrdering() {
            this.a = 1;
        }

        public long getA() {
            return this.a;
        }
    }
    ```

    正常通过 `new` 创建对象时，`getA()` 返回 `1`：

    ```java
    InitializationOrdering o1 = new InitializationOrdering();
    assertEquals(o1.getA(), 1);
    ```

    但若使用 `Unsafe.allocateInstance()`，**仅分配内存，不调用构造函数**：

    ```java
    InitializationOrdering o3 = (InitializationOrdering) unsafe.allocateInstance(InitializationOrdering.class);
    assertEquals(o3.getA(), 0); // 返回 long 的默认值 0
    ```

4. 修改私有字段

    假设有一个包含私有“秘密值”的类：

    ```java
    class SecretHolder {
        private int SECRET_VALUE = 0;

        public boolean secretIsDisclosed() {
            return SECRET_VALUE == 1;
        }
    }
    ```

    通过 `Unsafe.putInt()`，我们可以直接修改私有字段的值：

    ```java
    SecretHolder secretHolder = new SecretHolder();
    Field f = secretHolder.getClass().getDeclaredField("SECRET_VALUE");
    unsafe.putInt(secretHolder, unsafe.objectFieldOffset(f), 1);
    assertTrue(secretHolder.secretIsDisclosed());
    ```

    这展示了 `Unsafe` 可绕过封装，直接操作对象内存布局。

5. 抛出异常（绕过检查型异常限制）

    通过 `Unsafe.throwException()`，我们可以抛出**任何异常**（包括检查型异常），而无需在方法签名中声明或捕获：

    ```java
    @Test(expected = IOException.class)
    public void givenUnsafeThrowException_whenThrowCheckedException_thenNotNeedToCatchIt() {
        unsafe.throwException(new IOException()); // IOException 是检查型异常
    }
    ```

    编译器不会强制要求处理该异常，因为 `Unsafe` 调用被视为“非标准 Java 代码”。

6. 堆外内存（Off-Heap Memory）

    当 JVM 堆内存不足时，频繁的垃圾回收（GC）会影响性能。理想情况下，我们可以使用**堆外内存**（不受 GC 管理）。

    `Unsafe.allocateMemory()` 允许我们在 JVM 堆外分配大块内存：

    ```java
    class OffHeapArray {
        private static final int BYTE = 1;
        private long size;
        private long address;

        public OffHeapArray(long size) throws Exception {
            this.size = size;
            address = getUnsafe().allocateMemory(size * BYTE);
        }

        public void set(long i, byte value) {
            getUnsafe().putByte(address + i * BYTE, value);
        }

        public int get(long idx) {
            return getUnsafe().getByte(address + idx * BYTE);
        }

        public void freeMemory() {
            getUnsafe().freeMemory(address);
        }

        // ... getUnsafe() 方法略
    }
    ```

    使用示例：

    ```java
    long SUPER_SIZE = (long) Integer.MAX_VALUE * 2;
    OffHeapArray array = new OffHeapArray(SUPER_SIZE);

    int sum = 0;
    for (int i = 0; i < 100; i++) {
        array.set((long) Integer.MAX_VALUE + i, (byte) 3);
        sum += array.get((long) Integer.MAX_VALUE + i);
    }
    assertEquals(sum, 300);
    array.freeMemory(); // 必须手动释放！
    ```

    > 堆外内存**不会被 GC 自动回收**，必须显式调用 `freeMemory()` 释放，否则会造成内存泄漏。

7. Compare-And-Swap（CAS）操作

    `java.util.concurrent` 包中的高效并发类（如 `AtomicInteger`）底层正是基于 `Unsafe` 的 CAS 操作。

    CAS 利用 CPU 的原子指令实现**无锁算法**（lock-free），性能远高于传统同步机制。

    下面是一个基于 `compareAndSwapLong()` 的无锁计数器：

    ```java
    class CASCounter {
        private volatile long counter = 0;
        private long offset;
        private Unsafe unsafe;

        public CASCounter() throws Exception {
            unsafe = getUnsafe();
            offset = unsafe.objectFieldOffset(CASCounter.class.getDeclaredField("counter"));
        }

        public void increment() {
            long before = counter;
            while (!unsafe.compareAndSwapLong(this, offset, before, before + 1)) {
                before = counter; // 若值被其他线程修改，则重试
            }
        }

        public long getCounter() {
            return counter;
        }
    }
    ```

    测试多线程下的正确性：

    ```java
    int NUM_OF_THREADS = 1_000;
    int NUM_OF_INCREMENTS = 10_000;
    CASCounter casCounter = new CASCounter();

    ExecutorService service = Executors.newFixedThreadPool(NUM_OF_THREADS);
    IntStream.range(0, NUM_OF_THREADS)
    .forEach(i -> service.submit(() ->
        IntStream.range(0, NUM_OF_INCREMENTS)
            .forEach(j -> casCounter.increment())
    ));

    assertEquals(NUM_OF_INCREMENTS * NUM_OF_THREADS, casCounter.getCounter());
    ```

8. Park / Unpark

    `Unsafe` 提供了两个用于线程调度的底层方法：

    - `park()`：阻塞当前线程（类似 `Object.wait()`，但更底层，直接调用 OS 原语）。
    - `unpark(Thread thread)`：唤醒被 `park()` 阻塞的线程。

    这些方法被 JVM 用于实现 `LockSupport`、线程池等机制，在线程转储（thread dump）中经常可见。

9. 结论

    本文探讨了 `sun.misc.Unsafe` 类及其核心功能：

    - 绕过构造函数创建对象
    - 直接访问和修改私有字段
    - 分配和管理堆外内存
    - 实现基于 CAS 的无锁并发算法
    - 控制线程的阻塞与唤醒

    > **警告**：`Unsafe` 是**非官方、非公开 API**，在不同 JDK 版本中可能被移除或行为改变（例如 JDK 9+ 中已限制访问）。生产环境中应尽量避免使用，除非你清楚其风险并有充分理由（如高性能框架开发）。

    ---

    > **替代方案**：现代 Java 开发应优先使用标准 API，如 `VarHandle`（JDK 9 引入）、`ByteBuffer.allocateDirect()`（堆外内存）、`AtomicXXX`（CAS 操作）等。
