# 核心Java JVM电子书和示例

本模块包含有关使用 Java 虚拟机 (JVM) 的文章。

- [Java 中对象的内存布局](<https://www.baeldung.com/java-memory-layout>)
- [测量 JVM 中对象的大小](<https://www.baeldung.com/jvm-measuring-object-sizes>)
- [ ] [为JVM应用程序添加关闭钩子](#为jvm应用程序添加关闭钩子)
- [JVM 中的布尔和布尔[] 内存布局](<https://www.baeldung.com/jvm-boolean-memory-layout>)
- [数组长度在 JVM 中的存储位置](<https://www.baeldung.com/java-jvm-array-length>)
- [Java 中对象的内存地址](<https://www.baeldung.com/java-object-memory-address>)
- [列出特定类加载器中加载的所有类](<https://www.baeldung.com/java-list-classes-class-loader>)
- [JVM 中的常量池简介](<https://www.baeldung.com/jvm-constant-pool>)
- [列出 JVM 中加载的所有类](<https://www.baeldung.com/jvm-list-all-classes-loaded>)
- [静态字段和垃圾回收](<https://www.baeldung.com/java-static-fields-gc>)
- 更多文章： [[<-- prev]](/core-java-modules/core-java-jvm) [[next -->]](/core-java-modules/core-java-jvm-3)

## 为JVM应用程序添加关闭钩子

1. 概述

    启动一项服务通常很容易。不过，有时我们需要制定一个计划来优雅地关闭服务。

    在本教程中，我们将了解 JVM 应用程序终止的不同方式。然后，我们将使用 Java API 来管理 JVM 关闭钩子。请[参考本文](https://www.baeldung.com/runtime-getruntime-halt-vs-system-exit-in-java)，了解在 Java 应用程序中关闭 JVM 的更多信息。

2. 关闭 JVM

    可以通过两种不同的方式关闭 JVM：

    - 受控过程
    - 突然方式
    受控进程会在以下任一情况下关闭 JVM

    - 最后一个非[守护进程](https://www.baeldung.com/java-daemon-thread)线程终止。例如，当主线程退出时，JVM 启动关闭进程
    - 操作系统发出中断信号。例如，按下 Ctrl + C 或注销操作系统
    - 从 Java 代码中调用 System.exit()
    虽然我们都在努力实现优雅的关机，但有时 JVM 可能会以意想不到的方式突然关闭。在以下情况下，JVM 会突然关闭

    - 操作系统发出 kill 信号。例如，发出 `kill -9 <jvm_pid>` 命令
    - 从 Java 代码中调用 Runtime.getRuntime().halt()
    - 主机操作系统意外死机，例如电源故障或操作系统慌乱时
3. 关闭钩子

    JVM 允许在完成关机之前运行注册函数。这些函数通常用于释放资源或其他类似的内务管理任务。在 JVM 术语中，这些函数被称为关机钩子。

    关机钩子基本上是已初始化但未启动的线程。当 JVM 开始关闭进程时，它会以未指定的顺序启动所有已注册的钩子。运行完所有钩子后，JVM 将停止。

    测试代码：ShutdownHookUnitTest.java

    1. 添加挂钩

        为了添加关机钩子，我们可以使用 [Runtime.getRuntime().addShutdownHook()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)) 方法：

        givenAHook_WhenShutsDown_ThenHookShouldBeExecuted()

        在这里，我们只是在 JVM 自行关闭之前向标准输出端打印一些内容。如果我们像下面这样关闭 JVM

        ```zsh
        > System.exit(129);
        In the middle of a shutdown
        ```

        那么我们就会看到钩子实际上将信息打印到了标准输出中。

        JVM 负责启动钩子线程。因此，如果给定的钩子已经启动，Java 将抛出异常：

        addingAHook_WhenThreadAlreadyStarted_ThenThrowsAnException()

        显然，我们也不能多次注册钩子：

        addingAHook_WhenAlreadyExists_ThenAnExceptionWouldBeThrown()

    2. 删除挂钩

        Java 提供了一个孪生移除方法，用于在注册某个关闭钩子后将其移除：

        removeAHook_WhenItIsAlreadyRegistered_ThenWouldDeRegisterTheHook()
        当关机钩子被成功移除时，[removeShutdownHook()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runtime.html#removeShutdownHook(java.lang.Thread)) 方法返回 true。

    3. 注意事项

        JVM 仅在正常终止的情况下运行关闭钩子。因此，当外力突然杀死 JVM 进程时，JVM 将没有机会执行关闭钩子。此外，从 Java 代码中停止 JVM 也会产生同样的效果：

        ```java
        Thread haltedHook = new Thread(() -> System.out.println("Halted abruptly"));
        Runtime.getRuntime().addShutdownHook(haltedHook);
        Runtime.getRuntime().halt(129);
        ```

        [halt](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runtime.html#halt(int)) 方法会强制终止当前运行的 JVM。因此，已注册的关闭钩子将没有机会执行。

4. 总结

    在本教程中，我们了解了 JVM 应用程序可能终止的不同方式。然后，我们使用了一些运行时 API 来注册和注销关闭钩子。

## Code

与往常一样，示例代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-jvm-2) 上获取。
