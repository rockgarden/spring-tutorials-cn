# Java 剖析器指南

<https://www.baeldung.com/java-profilers>

1. 概述

    有时，仅仅编写能运行的代码是不够的。我们可能想知道内部发生了什么，例如内存是如何分配的、使用一种编码方法比使用另一种编码方法的后果、并发执行的影响、需要改进性能的地方等。为此，我们可以使用剖析器。

    Java Profiler 是一种在 JVM 层面监控 Java 字节码构造和操作的工具。这些代码构造和操作包括对象创建、迭代执行（包括递归调用）、方法执行、线程执行和垃圾回收。

    在本教程中，我们将探讨主要的 Java Profilers： [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html)、[YourKit](https://www.yourkit.com/java/profiler/)、[Java VisualVM](https://visualvm.github.io/)、[Netbeans Profiler](https://netbeans.apache.org/kb/docs/java/profiler-intro.html) 和 [IntelliJ Profiler](https://lp.jetbrains.com/intellij-idea-profiler/)。

2. JProfiler

    JProfiler 是许多开发人员的首选。通过直观的用户界面，JProfiler 提供了查看系统性能、内存使用情况、潜在内存泄露和线程剖析的界面。

    有了这些信息，我们就能很容易地看到底层系统中需要优化、消除或更改的地方。

    该产品需要购买许可证，但也提供免费试用。

    与大多数剖析器一样，我们可以将该工具用于本地和远程应用程序。这意味着无需在远程计算机上安装任何设备，就能对远程计算机上运行的 Java 应用程序进行剖析。

    JProfiler 还能对 SQL 和 NoSQL 数据库进行高级剖析。它为剖析 JDBC、JPA/Hibernate、MongoDB、Casandra 和 HBase 数据库提供了特定支持。

    如果我们希望了解与数据库交互的调用树，并查看可能泄露的连接，JProfiler 可以很好地处理这些问题。

    实时内存（Live Memory）是 JProfiler 的一项功能，它允许我们查看应用程序当前的内存使用情况。我们可以查看对象声明和实例的内存使用情况，也可以查看整个调用树的内存使用情况。

    对于分配调用树，我们可以选择查看实时对象、垃圾收集对象或两者的调用树。我们还可以决定该分配树是针对特定类或包，还是针对所有类。

    JProfiler 支持与 Eclipse、NetBeans 和 IntelliJ 等流行集成开发环境的集成。甚至还可以从快照导航到源代码。

3. YourKit

    YourKit Java Profiler 可在许多不同的平台上运行，并为每个支持的操作系统（Windows、MacOS、Linux、Solaris、FreeBSD 等）提供单独的安装。

    与 JProfiler 一样，YourKit 也具有可视化线程、垃圾回收、内存使用和内存泄露的核心功能，并支持通过 ssh 通道进行本地和远程剖析。

    YourKit提供用于商业用途的付费许可证（包括免费试用版）和用于非商业用途的低价或免费许可证。

    当我们想剖析抛出的异常时，YourKit 也能派上用场。我们可以轻松找出抛出的异常类型，以及每种异常发生的次数。

    YourKit 有一个有趣的 CPU 剖析功能，可以对代码的某些区域进行重点剖析，例如线程中的方法或子树。这个功能非常强大，因为它可以通过 what-if 功能进行有条件的剖析。

    我们还可以使用YourKit剖析SQL和NoSQL数据库调用。它甚至还提供了实际执行查询的视图。

    虽然这不是技术上的考虑因素，但 YourKit 的许可模式使它成为多用户或分布式团队以及单个许可购买的不错选择。

4. Java VisualVM

    Java VisualVM 是一款适用于 Java 应用程序的简化而强大的剖析工具。这是一款免费的开源剖析器。

    在 JDK 8 之前，该工具一直与 Java Development Kit (JDK) 捆绑，但在 JDK 9 中被移除，现在作为独立工具发布： [VisualVM 下载](https://visualvm.github.io/download.html)。

    它的运行依赖于 JDK 中提供的其他独立工具，如 JConsole、jstat、jstack、jinfo 和 jmap。

    Java VisualVM 的一个有趣优势是，我们可以将其扩展为插件，开发新的功能。然后，我们可以将这些插件添加到 Java VisualVM 的内置更新中心。

    Java VisualVM支持本地和远程剖析，以及内存和CPU剖析。连接到远程应用程序需要提供凭证（主机名/IP 和必要的密码），但不支持 ssh 隧道。我们还可以选择启用即时更新的实时剖析（通常每 2 秒更新一次）。

    利用 Java VisualVM 的快照功能，我们可以拍摄剖析会话的快照，以便日后进行分析。

    VSCode 插件：安装 **GraalVM Tools for Java** 扩展，与项目一起启动 VisualVM 和/或在 VS 代码中控制 VisualVM 会话。支持在 VS Code 编辑器中打开 VisualVM 结果的源代码。

    > 注：须要下载 [GraalVM](https://github.com/graalvm/graalvm-ce-builds/releases)

5. NetBeans Profiler

    NetBeans Profiler 捆绑在 Oracle 的开源 NetBeans IDE 中。

    虽然该剖析器与 Java VisualVM 有很多相似之处，但当我们希望在一个程序（集成开发环境 + 剖析器）中实现所有功能时，它是一个不错的选择。上面讨论的所有其他剖析器都提供插件，以加强集成开发环境的集成。

    Netbeans Profiler 也是轻量级开发和剖析的不错选择。它为配置和控制剖析会话以及显示结果提供了一个窗口。它提供了一个独特的功能，即了解垃圾回收发生的频率。

6. IntelliJ Profiler

    IntelliJ Profiler 是一款简单但功能强大的 CPU 和内存分配剖析工具。它结合了两种流行的 Java 剖析器的功能： [JFR](https://www.baeldung.com/java-flight-recorder-monitoring) 和 Async profiler。

    虽然它也有一些高级功能，但主要侧重于易用性。IntelliJ Profiler 让我们无需任何配置，只需点击几下就能开始使用，同时还提供了一些有用的功能来协助我们的日常开发工作。

    作为 IntelliJ IDEA Ultimate 的一部分，IntelliJ Profiler 只需单击一下即可附加到进程中，我们可以在快照和源代码之间浏览，就像它们是一体的一样。它的其他功能，如微分火焰图，可以让我们直观地评估不同方法的性能，并快速高效地深入了解运行时操作：

    IntelliJ Profiler 可在 Windows、Linux 和 macOS 上运行。

7. 其他可靠的剖析器

    这里值得一提的还有 [Java Mission Control](http://www.oracle.com/technetwork/java/javaseproducts/mission-control/java-mission-control-1998576.html)、[New Relic](https://newrelic.com/) 和 [Prefix](https://stackify.com/prefix/)（来自 [Stackify](https://stackify.com/)）。这些产品的总体市场份额较低，但绝对值得一提。例如，Stackify 的 Prefix 是一款出色的轻量级剖析工具，不仅适合剖析 Java 应用程序，也适合剖析其他网络应用程序。

8. 总结

    在本文中，我们讨论了剖析和 Java 剖析器。我们了解了每种剖析器的功能，以及选择其中一种剖析器的可能性。

    有许多 Java 剖析器可用，其中一些具有独特的特性。正如我们在本文中所看到的，选择使用哪种 Java 剖析器主要取决于开发人员对工具的选择、所需的分析水平以及剖析器的功能。
