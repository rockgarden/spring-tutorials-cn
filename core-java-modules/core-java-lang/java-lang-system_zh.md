# [java.lang.System快速指南](https://www.baeldung.com/java-lang-system)

快速了解java.lang.System类及其特点和核心功能。

1. IO

    System是java.lang的一部分，它的主要功能之一是让我们访问标准的I/O流。

    简单地说，它暴露了三个字段，每个字段都有一个。

    - out
    - err
    - in

    1. System.out

        System.out指向标准输出流，将其暴露为PrintStream，我们可以用它来打印文本到控制台。

        `System.out.print("some inline message");`

        System的一个高级用法是调用System.setOut，我们可以用它来定制System.out要写到的位置。

        重定向到一个文本文件：`System.setOut(new PrintStream("filename.txt"));`

    2. System.err

        System.err与System.out很像。两个字段都是PrintStream的实例，都是用于向控制台打印信息。

        但System.err代表标准错误，我们专门用它来输出错误信息。

        `System.err.print("some inline error message");`

        控制台通常会以不同于输出流的方式渲染错误流。

        欲了解更多信息，请查看[PrintStream](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/PrintStream.html)文档。

    3. System.in

        System.in指向标准的in，将其暴露为一个InputStream，我们可以用它来读取控制台的输入。

        虽然有点麻烦，但我们还是可以做到的。

        system/UserCredentials.readUsername(int length) throws IOException {}

        通过调用System.in.read，应用程序停止并等待来自标准in的输入。无论下一个长度是多少字节，都将从流中读取并存储在字节数组中。

        用户输入的任何其他内容都会留在流中，等待另一次调用读取。

        当然，在这么低的水平上操作是很有挑战性和容易出错的，所以我们可以用BufferedReader把它清理一下。

        system/UserCredentials.readUsername()

        如上，readLine将从System.in中读取数据，直到用户点击返回，这有点接近我们可能期望的结果。

        注意，在这种情况下，我们特意没有关闭流。关闭标准的in意味着在程序的生命周期内不能再被读取了。

        最后，System.in的一个高级用法是调用System.setIn将其重定向到一个不同的InputStream。

2. 实用方法

    系统为我们提供了许多方法来帮助我们处理一些事情，比如。

    - Accessing the console
    - Copying arrays
    - Observing date and time
    - Exiting the JRE
    - Accessing runtime properties
    - Accessing environment variables, and
    - Administering garbage collection

    1. 访问控制台

        Java 1.6引入了另一种与控制台交互的方式，而不是简单地直接使用System.out和in。

        我们可以通过调用System.console来访问它。

        system/UserCredentials.readUsernameWithPrompt()

        注意，根据底层操作系统和我们启动Java运行当前程序的方式，console可能会返回null，所以使用前一定要检查。

        请查看[控制台](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Console.html)文档以了解更多的用途。

    2. 复制数组

        System.arraycopy是一种老式的C语言方式，可以将一个数组复制到另一个数组。

        大多数情况下，arraycopy的目的是将一个完整的数组复制到另一个数组中。

        SystemArrayCopyUnitTest.java

        然而，我们可以指定两个数组的起始位置，以及要复制多少个元素。

        例如，我们想从a中复制2个元素，从a[1]开始到b中，从b[3]开始。

        SystemArrayCopyUnitTest.java

        另外，记住arraycopy将抛出。

        - NullPointerException 如果其中一个数组是空的
        - IndexOutOfBoundsException，如果拷贝引用的任何一个数组超出了其范围。
        - 如果复制导致类型不匹配，ArrayStoreException。

    3. 观察日期和时间

        在System中，有两个与时间相关的方法。一个是currentTimeMillis，另一个是nanoTime。

        currentTimeMillis返回自Unix Epoch（即1970年1月1日上午12:00 UTC）以来的毫秒数。

        DateTimeService.nowPlusOneHour()

        DateTimeService.nowPrettyPrinted()

        nanoTime返回相对于JVM启动的时间。我们可以多次调用它来标记应用程序中的时间流逝。

        SystemNanoUnitTest.java

        注意，由于nanoTime是如此精细，由于[数字溢出的可能性](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/System.html#nanoTime())，做endTime - startTime < 10000比做endTime < startTime更安全。

    4. 退出程序

        如果我们想以编程方式退出当前执行的程序，System.exit就可以做到这一点。

        为了调用exit，我们需要指定一个退出代码，它将被发送到启动程序的控制台或shell中。

        根据Unix的惯例，状态为0意味着正常退出，而非0意味着发生了一些错误。

        SystemExitDemo.java

        注意，对于现在的大多数程序来说，需要调用这个是很奇怪的。

    5. 访问运行时属性

        系统通过getProperty提供对运行时属性的访问。

        我们可以用setProperty和clearProperty来管理它们。

        SystemPropertiesUnitTest.java

        通过-D指定的属性可以通过getProperty访问。

        我们也可以提供一个默认值。

        而System.getProperties提供了一个所有系统属性的集合。

        我们可以在其中进行任何属性操作。

    6. 访问环境变量

        System还通过getenv提供了对环境变量的只读访问。

        例如，如果我们想访问PATH环境变量，我们可以这样做。

        EnvironmentVariables.getPath()

    7. 管理垃圾收集

        通常情况下，垃圾收集工作对我们的程序是不透明的。但有时，我们可能想向JVM提出一个直接的建议。

        System.runFinalization是一个方法，它允许我们建议JVM运行其finalize程序。

        System.gc是一个方法，它允许我们建议JVM运行它的垃圾收集程序。

        由于这两个方法的契约并不能保证最终化或垃圾收集的运行，所以它们的用处很窄。

        然而，它们可以作为一种优化来行使，比如说当一个桌面应用程序被最小化时调用gc。

        ChatWindow.windowStateChanged()

3. 总结

    在这篇文章中，我们看到了System提供的一些字段和方法。完整的列表可以在[官方的System文档](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/System.html)中找到。
