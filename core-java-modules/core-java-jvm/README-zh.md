# JVM Cookbooks和示例

本模块包含有关使用 Java 虚拟机 (JVM) 的文章。

- [JVM 中的方法内嵌](https://www.baeldung.com/jvm-method-inlining)
- [JVM 日志伪造](https://www.baeldung.com/jvm-log-forging)
- [Java Instrumentation 指南](https://www.baeldung.com/java-instrumentation)
- [x] [Java中的类加载器](#java中的类加载器)
- [ ] [System.exit()指南](#systemexit指南)
- [ ] [System.gc()指南](#systemgc指南)
- [Java 中的 Runtime.getRuntime().halt() 与 System.exit()](https://www.baeldung.com/java-runtime-halt-vs-system-exit)
- [如何获取 Java 中对象的大小](http://www.baeldung.com/java-size-of-object)
- [什么原因导致 java.lang.OutOfMemoryError：无法创建新的本地线程](https://www.baeldung.com/java-outofmemoryerror-unable-to-create-new-native-thread)
- [在 Java 中查看类文件的字节码](https://www.baeldung.com/java-class-view-bytecode)
- 更多文章： [[下-->]](/core-java-modules/core-java-jvm-2)

## System.exit()指南

1. 概述

    在本教程中，我们将了解 System.exit 在 Java 中的含义。

    我们将了解它的用途、使用位置和使用方法。我们还将了解在不同状态代码下调用它的区别。

2. 什么是 System.exit？

    System.exit 是一个 void 方法。它获取一个退出代码，并将其传递给调用的脚本或程序。

    退出代码为 0 表示正常退出：`System.exit(0);`

    我们可以传递任何整数作为该方法的参数。非零状态代码被视为异常退出。

    调用 System.exit 方法会终止当前运行的 JVM 并退出程序。该方法不会正常返回。

    这意味着 System.exit 之后的后续代码实际上是不可访问的，而编译器却不知道。

    ```java
    System.exit(0);
    System.out.println("This line is unreachable");
    ```

    用 System.exit(0) 关闭程序不是个好主意。这样做的结果与从主方法退出的结果相同，而且还会阻止后续行的执行，同时调用 System.exit 的线程也会阻塞，直到 JVM 终止。如果关闭钩子向该线程提交任务，就会导致死锁。

3. 为什么需要它？

    System.exit 的典型用例是当出现异常情况时，我们需要立即退出程序。

    此外，如果我们必须从主方法以外的地方终止程序，System.exit 也是一种实现方法。

4. 何时需要？

    脚本通常会依赖它所调用命令的退出代码。如果这样的命令是 Java 应用程序，那么 System.exit 就可以方便地发送退出代码。

    例如，我们可以不抛出异常，而是返回一个异常退出代码，然后由调用脚本进行解释。

    或者，我们可以使用 System.exit 来调用我们注册的任何关闭钩子。这些钩子可用于清理所持有的资源，并从其他[非守护进程](https://www.baeldung.com/java-daemon-thread)线程安全退出。

5. 一个简单示例

    在这个示例中，我们尝试读取一个文件，如果该文件存在，我们就打印其中的一行。如果文件不存在，我们将在 catch 块中使用 System.exit 退出程序。

    ```java
    try {
        BufferedReader br = new BufferedReader(new FileReader("file.txt"));
        System.out.println(br.readLine());
        br.close();
    } catch (IOException e) {
        System.exit(2);
    } finally {
        System.out.println("Exiting the program");
    }
    ```

    在这里，我们必须注意，如果找不到文件，finally 块不会被执行。因为 catch 块上的 System.exit 会退出 JVM，不允许执行 finally 块。

6. 选择状态代码

    我们可以传递任何整数作为状态代码，但一般的做法是，状态代码为 0 的 System.exit 为正常退出，其他为异常退出。

    请注意，这只是一种 "良好做法"，并不是编译器会在意的严格规则。

    此外，值得注意的是，当我们从命令行调用 Java 程序时，状态代码也会被考虑在内。

    在下面的示例中，当我们尝试执行 SystemExitExample.class 时，如果它通过调用 System.exit 以非零状态代码退出 JVM，则不会打印下面的 echo。

    `java SystemExitExample && echo "I will not be printed"`
    为了使我们的程序能够与其他标准工具通信，我们可以考虑遵循相关系统用于通信的标准代码。

    Linux 文档项目（The Linux Documentation Project）编写的《[具有特殊含义的退出代码](https://tldp.org/LDP/abs/html/exitcodes.html)》（Exit Codes With Special Meanings）文档提供了一份保留代码列表。 它还建议在特定情况下使用哪些代码。

7. 结束语

    在本教程中，我们讨论了 System.exit 如何工作、何时使用以及如何使用。

    在使用应用程序服务器和其他常规应用程序时，使用[异常处理](https://www.baeldung.com/java-exceptions)或简单的返回语句退出程序是一种很好的做法。使用 System.exit 方法更适合基于脚本的应用程序或解释状态代码的应用程序。

## System.gc()指南

1. 概述

    在本教程中，我们将研究 java.lang 包中的 System.gc() 方法。

    众所周知，明确调用 System.gc() 是一种不好的做法。让我们试着了解一下原因，以及调用该方法是否有任何有用的用例。

2. 垃圾回收

    Java 虚拟机会在有迹象表明需要执行垃圾回收时决定这样做。不同的[GC实现](https://www.baeldung.com/jvm-garbage-collectors)有不同的指示。它们基于不同的启发式方法。不过，有几个时刻 GC 是肯定要执行的：

    - 旧一代（Tenured 空间）已满，触发主要/完全 GC
    - 新一代（Eden + Survivor0 + Survivor1 空间）已满，触发次要 GC
    唯一与 GC 实现无关的是对象是否有资格被垃圾回收。

    现在，我们来看看 System.gc() 方法本身。

3. System.gc()

    调用该方法非常简单：

    `System.gc()`

    [Oracle官方](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#gc())文档指出

    调用 gc 方法表明 Java 虚拟机将努力回收未使用的对象，以便将它们当前占用的内存用于快速重用。

    但并不保证会触发实际的 GC。

    System.gc()[会触发主要的GC](https://www.oracle.com/java/technologies/javase/gc-tuning-6.html#other_considerations)。因此，根据你的垃圾回收器实现，有可能在stop-the-world花费一些时间。这样，我们就有了一个不可靠的工具，并可能带来显著的性能损失。

    显式垃圾回收调用的存在对每个人来说都是一个严重的问题。

    我们可以使用 `-XX:DisableExplicitGC` JVM 标志来阻止 System.gc() 执行任何工作。

    1. 性能调整

        值得注意的是，在抛出 OutOfMemoryError 之前，JVM 会执行一次完整的 GC。因此，明确调用 System.gc() 并不能避免失败。

        现在的垃圾回收器非常聪明。它们掌握了内存使用情况和其他统计数据的所有知识，能够做出正确的决定。因此，我们应该信任它们。

        如果出现内存问题，我们可以更改[一系列设置](https://docs.oracle.com/javase/9/gctuning/JSGCT.pdf)来调整应用程序--从选择不同的垃圾回收器开始，到设置所需的应用程序时间/垃圾回收器时间比率，最后到设置内存段的固定大小。

        还有一些方法可以[减轻显式调用导致的全GC影响](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/cms-6.html)。我们可以使用以下标志之一

        `-XX:+ExplicitGCInvokesConcurrent`

        或

        `-XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses`
        如果我们真的希望应用程序正常运行，就应该解决真正的内存问题。

4. 使用示例

    1. 场景

        让我们编写一个测试程序。我们想找到调用 System.gc() 可能有用的情况。

        小规模垃圾回收比大规模垃圾回收发生得更频繁。因此，我们应该把重点放在后者上。如果单个对象 "幸存" 了几次收集，并且仍然可以从 GC 根目录中找到它，那么它就会被转移到保有空间。

        假设我们有一个庞大的对象集合，这些对象都存活了一段时间。然后，在某个时刻，我们要清除对象集合。也许这正是运行 System.gc() 的好时机？

    2. 演示应用程序

        我们将创建一个简单的控制台应用程序来模拟上述场景：

        systemgc/DemoApplication.java

    3. 运行演示

        让我们使用一些额外的标志运行应用程序：

        `-XX:+PrintGCDetails -Xloggc:gclog.log -Xms100M -Xmx500M -XX:+UseConcMarkSweepGC`

        MacOS:

        ```bash
        /usr/bin/env /Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/bin/java -XX:+PrintGCDetails -Xloggc:gclog.log -Xms100M -Xmx500M -XX:+UseConcMarkSweepGC -Dvisualvm.display.name=core-java-jvm%PID -Dvisualvm.id=1696579986780 -cp /var/folders/3z/zm1dwk351qdbn4xv1hxgr8f40000gn/T/cp_62lff7lee3za22ijljlf8hw54.jar com.baeldung.systemgc.DemoApplication
        ```

        前两个标志用于记录 GC 信息。接下来的两个标志是设置初始堆大小和最大堆大小。我们希望保持较小的堆大小，以迫使 GC 更加活跃。最后，我们决定使用 CMS（并发标记和清扫垃圾收集器）。是时候运行我们的应用程序了！

        首先，让我们尝试填充保有空间。输入 fill。

        我们可以[查看gclog.log文件](https://www.baeldung.com/java-verbose-gc)，看看发生了什么。我们会看到大约 15 个集合。单个集合的日志行如下

        我们可以看到，内存已被填满。

        接下来，让我们输入 gc 强制 System.gc()。我们可以看到内存使用量没有明显变化：

        ```log
        197.057: [GC (Allocation Failure) 197.057: [ParNew: 67498K->40K(75840K), 0.0016945 secs] 
        168754K->101295K(244192K), 0.0017865 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
        8.621: [GC (Allocation Failure) 8.622: [ParNew: 28416K->3520K(31936K), 0.0025944 secs] 28416K->3738K(102976K), 0.0027093 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
        ```

        再运行几次后，我们会发现内存大小保持在同一水平。

        让我们输入 invalidate 来清除缓存。gclog.log 文件中应该不会再出现任何日志行。

        我们可以再试着填满缓存几次，但都没有出现 GC。这时候我们就可以智取垃圾回收器了。现在，在强制 GC 之后，我们会看到这样一行内容

        ```log
        238.810: [Full GC (System.gc()) 238.810: [CMS: 101255K->101231K(168352K); 0.2634318 secs] 
        120693K->101231K(244192K), [Metaspace: 32186K->32186K(1079296K)], 0.2635908 secs] 
        [Times: user=0.27 sys=0.00, real=0.26 secs]
        ```

        我们释放了大量内存！但现在真的有必要吗？发生了什么？

        根据这个例子，当我们释放大对象或使缓存失效时，调用 System.gc() 似乎很有吸引力。

5. 其他用法

    明确调用 System.gc() 方法可能有用的原因很少。

    一个可能的原因是在服务器启动后清理内存--我们启动的服务器或应用程序需要做大量的准备工作。之后，会有大量对象需要最终确定。不过，这些准备工作之后的清理工作不应该由我们负责。

    另一个问题是内存泄漏分析--这更像是一种调试实践，而不是我们希望在生产代码中保留的东西。如果调用 System.gc()，发现堆空间仍然很大，这可能是[内存泄漏](https://www.baeldung.com/java-memory-leaks)的迹象。

6. 总结

    在本文中，我们研究了 System.gc() 方法以及它在哪些情况下可能有用。

    当涉及到应用程序的正确性时，我们绝不能依赖它。在大多数情况下，GC 比我们更聪明，如果出现内存问题，我们应该考虑调整虚拟机，而不是明确调用。

## 相关文章

- [Method Inlining in the JVM](https://www.baeldung.com/jvm-method-inlining)
- [JVM Log Forging](https://www.baeldung.com/jvm-log-forging)
- [Guide to Java Instrumentation](https://www.baeldung.com/java-instrumentation)
- [A Guide to System.exit()](https://www.baeldung.com/java-system-exit)
- [Guide to System.gc()](https://www.baeldung.com/java-system-gc)
- [Runtime.getRuntime().halt() vs System.exit() in Java](https://www.baeldung.com/java-runtime-halt-vs-system-exit)
- [How to Get the Size of an Object in Java](http://www.baeldung.com/java-size-of-object)
- [What Causes java.lang.OutOfMemoryError: unable to create new native thread](https://www.baeldung.com/java-outofmemoryerror-unable-to-create-new-native-thread)
- [View Bytecode of a Class File in Java](https://www.baeldung.com/java-class-view-bytecode)
- More articles: [[next -->]](/core-java-modules/core-java-jvm-2)

## Code

和往常一样，这些示例的源代码可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-jvm) 上找到。

运行 Instrumentation 的代码： <https://www.baeldung.com/java-instrumentation> 文章：

1. 构建模块
2. 运行模块 3 次，构建 3 个 jar：
    mvn install -PbuildAgentLoader
    mvn install -PbuildApplication
    mvn install -PbuildAgent
3. 用目标文件夹中生成的 jar 的准确名称更新文章中的命令
4. 使用系统中代理的路径更新 AgentLoader 类中的路径
