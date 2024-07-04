# Core Java IO APIs

This module contains articles about core Java input/output(IO) APIs.

## Java输出流指南

1. 概述

    在本教程中，我们将探讨 Java 类 OutputStream 的详细信息。OutputStream 是一个抽象类。它是所有表示字节输出流的类的超类。

    接下来，我们将详细探讨 "输出" 和 "流" 等词的含义。

    代码参见：
    - outputstream/OutputStreamExamples.java
    - outputstream/OutputStreamExamplesUnitTest.java

2. Java IO 简介

    OutputStream 是 Java IO API 的一部分，它定义了在 Java 中执行 I/O 操作所需的类。这些类都打包在 java.io 命名空间中。这是 Java 1.0 版本以来的核心包之一。

    从 Java 1.4 开始，我们还在 java.nio 命名空间中打包了 Java NIO，它支持非阻塞输入和输出操作。不过，本文的重点是作为 Java IO 一部分的 ObjectStream。

    有关 Java IO 和 Java NIO 的详细信息，请参见[此处](https://docs.oracle.com/javase/8/docs/technotes/guides/io/index.html)。

    1. 输入和输出

        Java IO 基本上提供了一种从源读取数据并将数据写入目标的机制。这里，输入表示源，输出表示目的地。

        这些来源和目的地可以是任何文件、管道或网络连接。

    2. 流

        Java IO 提供了流的概念，基本上代表了连续的数据流。流可以支持多种不同类型的数据，如字节、字符、对象等。

        此外，流所代表的是与源或目标的连接。因此，它们分别以 InputStream 或 OutputStream 的形式出现。

3. 输出流的接口

    OutputStream 实现了一系列接口，这些接口为其子类提供了一些独特的特征。让我们快速浏览一下它们。

    1. 可关闭

        接口 Closeable 提供了一个名为 close() 的方法，用于关闭数据源或数据目标。每个 OutputStream 的实现都必须提供该方法的实现。在这里，它们可以执行释放资源的操作。

    2. 自动关闭

        接口 AutoCloseable 也提供了一个名为 close() 的方法，其行为与 Closeable 中的方法类似。不过，在这种情况下，方法 close() 会在退出 try-with-resource 代码块时被自动调用。

        有关 try-with-resource 的更多详情，请点击[此处](https://www.baeldung.com/java-try-with-resources)。

    3. 可刷新

        接口 Flushable 提供了一个名为 flush() 的方法，用于将数据刷新到目的地。

        OutputStream 的特定实现可能会选择对之前写入的字节进行缓冲以进行优化，但调用 flush() 会使其立即向目标写入数据。

4. 输出流中的方法

    OutputStream 有几个方法，每个实现类都必须为各自的数据类型实现这些方法。

    除了从 Closeable 和 Flushable 接口继承的 close() 和 flush() 方法外，还有其他方法。

    1. write(int b)

        我们可以使用该方法将一个特定字节写入 OutputStream。由于参数 "int" 由四个字节组成，因此按照约定，只写入第一个低阶字节，其余三个高阶字节将被忽略：

        fileOutputStreamByteSingle(String file, String data)

        如果我们以 "Hello World!" 的数据调用此方法，得到的结果是一个包含以下文本的文件：

        W

        我们可以看到，这是索引为第六位的字符串的第七个字符。

    2. write(byte[] b, int off, int length)

        这个 write() 方法的重载版本用于将字节数组的子序列写入 OutputStream。

        它可以从参数指定的字节数组中写入 "length" 字节数，从 "off" 决定的偏移量开始写入 OutputStream：

        fileOutputStreamByteSubSequence(String file, String data)

        如果我们现在使用与之前相同的数据调用此方法，输出文件中将出现以下文本：

        World

        这是从索引 5 开始的数据子串，包含 5 个字符。

    3. write(byte[] b)

        这是 write() 方法的另一个重载版本，可以将参数指定的整个字节数组写入 OutputStream。

        其效果与调用 write(b, 0, b.lengh) 相同：

        fileOutputStreamByteSequence(String file, String data)

        当我们现在使用相同的数据调用此方法时，输出文件中就会出现整个字符串：

        Hello World!

5. OutputStream 的直接子类

    现在，我们将讨论 OutputStream 的一些已知直接子类，它们各自代表其定义的 OutputStream 的特定数据类型。

    除了实现从 OutputStream 继承的方法外，它们还定义了自己的方法。

    我们将不再详述这些子类。

    1. 文件输出流

        顾名思义，FileOutputStream 是一种将数据写入文件的 OutputStream。FileOutputStream 和其他 OutputStream 一样，可以写入原始字节流。

        在上一节中，我们已经研究了 FileOutputStream 的不同方法。

    2. 字节数组输出流

        字节数组输出流（ByteArrayOutputStream）是 OutputStream 的一种实现，可以将数据写入字节数组。ByteArrayOutputStream 向缓冲区写入数据时，缓冲区会不断增大。

        我们可以将缓冲区的默认初始大小保持为 32 字节，也可以使用其中一个可用的构造函数设置特定的大小。

        这里需要注意的是，close() 方法实际上没有任何作用。即使调用了 close() 方法，也可以安全地调用 ByteArrayOutputStream 中的其他方法。

    3. 过滤输出流

        OutputStream 主要是将字节流写入目的地，但也可以在写入之前转换数据。FilterOutputStream 是所有此类类的超类，它们执行特定的数据转换。FilterOutputStream 总是与现有的 OutputStream 一起构造的。

        FilterOutputStream 的一些示例包括 BufferedOutputStream、CheckedOutputStream、CipherOutputStream、DataOutputStream、DeflaterOutputStream、DigestOutputStream、InflaterOutputStream 和 PrintStream。

    4. 对象输出流

        ObjectOutputStream 可以将 Java 对象的原始数据类型和图形写入目的地。我们可以使用现有的 OutputStream 构建 ObjectOutputStream，以便向特定目标（如 File）写入数据。

        请注意，对象必须实现 Serializable 才能将 ObjectOutputStream 写入目的地。有关 Java 序列化的更多详情，请点击[此处](https://www.baeldung.com/java-serialization)。

    5. 管道式输出流

        PipedOutputStream 可用于创建通信管道。PipedOutputStream 可以写入数据，而连接的 PipedInputStream 可以读取数据。

        PipedOutputStream 有一个构造函数，用于连接 PipedInputStream。或者，我们也可以稍后使用 PipedOutputStream 中提供的名为 connect() 的方法进行连接。

6. 输出流缓冲

    输入和输出操作通常涉及磁盘访问、网络活动等相对昂贵的操作。经常执行这些操作会降低程序的效率。

    我们在 Java 中使用 "缓冲数据流(buffered streams)" 来处理这些情况。BufferedOutputStream 将数据写入一个缓冲区，当缓冲区满了或调用 flush() 方法时，缓冲区就会被刷新到目的地。

    BufferedOutputStream 扩展了前面讨论过的 FilterOutputStream，并封装了现有的 OutputStream，以便将数据写入目的地：

    bufferedOutputStream(String file, String... data)

    需要注意的关键点是，对每个数据参数的每次 write() 调用都只会写入缓冲区，而不会导致对文件的潜在昂贵调用。

    在上面的例子中，如果我们以 "Hello"、"World!" 这样的数据调用此方法，只有当代码从 try-with-resources 块退出并调用 BufferedOutputStream 上的 close() 方法时，数据才会被写入文件。

    这样，输出文件中的文本如下：

    Hello World！

7. 使用 OutputStreamWriter 写入文本

    如前所述，字节流代表原始数据，可能是一堆文本字符。现在我们可以获取字符数组，然后自己将其转换为字节数组：

    `byte[] bytes = data.getBytes();`

    Java 提供了方便的类来弥补这一差距。就 OutputStream 而言，这个类就是 OutputStreamWriter。OutputStreamWriter 封装了一个 OutputStream，可以直接将字符写入所需的目标。

    我们还可以选择性地为 OutputStreamWriter 提供编码字符集：

    outputStreamWriter(String file, String data)

    现在我们可以看到，在使用 FileOutputStream 之前，我们不必将字符数组转换为字节数组。OutputStreamWriter 会很方便地为我们完成这项工作。

    毫不奇怪，当我们使用 "Hello World!"这样的数据调用上述方法时，结果会生成一个文本如下的文件：

    Hello World！

8. 总结

    在本文中，我们讨论了 Java 抽象类 OutputStream。我们了解了它实现的接口和提供的方法。

    然后，我们讨论了 Java 中 OutputStream 的一些子类。最后我们讨论了缓冲和字符流。

## 缓冲阅读器指南

1. 概述

    [BufferedReader](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/BufferedReader.html) 是一个简化从字符输入流中读取文本的类。它对字符进行缓冲，以便高效读取文本数据。

    在本教程中，我们将学习如何使用 BufferedReader 类。

2. 何时使用 BufferedReader

    一般来说，如果我们想从文件、套接字或其他任何输入源读取文本，BufferedReader 就会派上用场。

    简而言之，它能让我们通过读取字符块并将其存储在内部缓冲区中，从而最大限度地减少 I/O 操作的次数。当缓冲区中有数据时，阅读器将从缓冲区中读取数据，而不是直接从底层流中读取数据。

    1. 缓冲另一个阅读器

        与大多数 Java I/O 类一样，BufferedReader 实现了装饰器模式，这意味着它在构造函数中期待一个阅读器。这样，我们就可以灵活地扩展一个具有缓冲功能的阅读器实例：

        `BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/input.txt"));`

        但是，如果缓冲对我们来说并不重要，我们可以直接使用 FileReader：

        `FileReader reader = new FileReader("src/main/resources/input.txt");`

        除了缓冲功能，BufferedReader 还提供了一些不错的辅助函数，用于逐行读取文件。因此，尽管直接使用 FileReader 可能看起来更简单，但 BufferedReader 可以帮上大忙。

    2. 缓冲流

        一般来说，我们可以配置 BufferedReader，将任何类型的输入流作为底层源。我们可以使用 InputStreamReader 并将其封装在构造函数中：

        `BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));`

        在上面的示例中，我们从 System.in 读取数据，这通常与键盘输入相对应。同样，我们也可以通过输入流从套接字、文件或任何可以想象到的文本输入类型中读取数据。唯一的前提条件是有一个合适的 InputStream 实现。

    3. 缓冲阅读器与扫描器

        作为替代方案，我们可以使用 Scanner 类来实现与 BufferedReader 相同的功能。

        然而，这两个类之间存在明显的差异，根据我们的使用情况，它们对我们来说可能更方便，也可能不那么方便：

        - BufferedReader 是同步的（线程安全），而 Scanner 不是。
        - Scanner 可以使用正则表达式解析原始类型和字符串
        - BufferedReader 允许改变缓冲区的大小，而 Scanner 的缓冲区大小是固定的
        - BufferedReader 的默认缓冲区大小更大
        - Scanner 会隐藏 IOException，而缓冲阅读器会强制我们处理它
        - BufferedReader 通常比 Scanner 更快，因为它只读取数据而不进行解析

        考虑到这些因素，如果我们要解析文件中的单个标记，那么 Scanner 会比 BufferedReader 感觉更自然一些。但是，一次只读一行是 BufferedReader 的优势所在。

        如果需要，我们也有关于 [Scanner](https://www.baeldung.com/java-scanner) 的指南。

3. 使用 BufferedReader 阅读文本

    让我们来回顾一下正确构建、使用和销毁 BufferReader 以读取文本文件的整个过程。

    参见代码：bufferedreader/BufferedReaderExample.java

    1. 初始化缓冲阅读器

        首先，让我们使用 BufferedReader(Reader) 构造函数创建一个 BufferedReader。

        像这样封装 FileReader 是一种很好的方法，可以将缓冲作为一个方面添加到其他阅读器中。

        默认情况下，这将使用 8 KB 的缓冲区。不过，如果我们想缓冲更小或更大的数据块，可以使用 BufferedReader(Reader, int) 构造函数：

        `BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/input.txt"), 16384);`

        这将把缓冲区大小设置为 16384 字节（16 KB）。

        最佳缓冲区大小取决于输入流的类型和运行代码的硬件等因素。因此，要获得理想的缓冲区大小，我们必须通过实验自己寻找。

        最好使用 2 的幂次作为缓冲区大小，因为大多数硬件设备的块大小都是 2 的幂次。

        最后，还有一种方便的方法，即使用 java.nio API 中的 [Files](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html) 辅助类创建 BufferedReader：

        `BufferedReader reader = Files.newBufferedReader(Paths.get("src/main/resources/input.txt"))`

        如果我们想读取文件，像这样创建缓冲读取器是一种不错的方法，因为我们不必先手动创建 FileReader，然后再封装它。

    2. 逐行读取

        接下来，让我们使用 readLine 方法读取文件内容：

        readAllLines(BufferedReader reader)

        我们可以使用 Java 8 中引入的 lines 方法更简单地完成与上述相同的操作：

        readAllLinesWithStream(BufferedReader reader)

    3. 关闭流

        使用缓冲阅读器后，我们必须调用其 close() 方法来释放与之相关的系统资源。如果我们使用 try-with-resources 块，则会自动完成这项工作：

        ```java
        try (BufferedReader reader = 
            new BufferedReader(new FileReader("src/main/resources/input.txt"))) {
            return readAllLines(reader);
        }
        ```

4. 其他有用的方法

    现在让我们重点介绍 BufferedReader 中的各种实用方法。

    参见代码：bufferedreader/BufferedReaderExample.java

    1. 读取单个字符

        我们可以使用 read() 方法读取单个字符。让我们逐个字符读取整个内容，直到流的结束：

        readAllCharsOneByOne(BufferedReader reader)

        这将读取字符（以 ASCII 值返回），将其转换为 char 并追加到结果中。我们将重复上述操作，直到数据流结束，即 read() 方法的响应值为-1。

    2. 读取多个字符

        如果我们想一次读取多个字符，可以使用 read(char[] cbuf, int off, int len) 方法：

        readMultipleChars(BufferedReader reader)

        在上述代码示例中，我们将最多读取 5 个字符到字符数组中，并从中构造一个字符串。如果在读取过程中没有读取到任何字符（即读取到数据流的末尾），我们将直接返回一个空字符串。

    3. 跳过字符

        我们还可以通过调用 skip(long n) 方法跳过指定数量的字符：BufferedReaderUnitTest.java

        givenBufferedReader_whenSkipUnderscores_thenOk()

        在上面的示例中，我们从输入字符串中读取了由两个下划线分隔的数字。为了构造一个只包含数字的字符串，我们调用了 skip 方法跳过下划线。

    4. 标记和重置

        我们可以使用 mark(int readAheadLimit) 和 reset() 方法标记数据流中的某个位置，并在稍后返回。作为一个有点勉强的例子，让我们使用 mark() 和 reset() 忽略流开头的所有空格：BufferedReaderUnitTest.java

        givenBufferedReader_whenSkipsWhitespacesAtBeginning_thenOk()

        在上例中，我们使用 mark() 方法标记刚读取的位置。将其值设为 1 意味着代码只记住向前一个字符的标记。它在这里非常方便，因为一旦我们看到第一个非空格字符，我们就可以返回并重新读取该字符，而无需重新处理整个数据流。如果没有标记，我们就会丢失最终字符串中的 L。

        请注意，由于 mark() 会引发 UnsupportedOperationException 异常，因此将 markSupported() 与调用 mark() 的代码联系起来是很常见的。不过，我们在这里实际上并不需要它。因为对于 BufferedReader 来说，markSupported() 总是返回 true。

        当然，我们也可以用其他更优雅的方法来实现上述功能，事实上，mark 和 reset 并不是非常典型的方法。不过，当需要展望未来时，它们肯定会派上用场。

5. 总结

    在这个快速教程中，我们已经学会了如何使用 BufferedReader 在实际示例中读取字符输入流。

## Relevant Articles

- [x] [Guide to Java OutputStream](https://www.baeldung.com/java-outputstream)
- [A Guide to the Java FileReader Class](https://www.baeldung.com/java-filereader)
- [The Java File Class](https://www.baeldung.com/java-io-file)
- [Java FileWriter](https://www.baeldung.com/java-filewriter)
- [Comparing getPath(), getAbsolutePath(), and getCanonicalPath() in Java](https://www.baeldung.com/java-path)
- [Quick Use of FilenameFilter](https://www.baeldung.com/java-filename-filter)
- [x] [Guide to BufferedReader](https://www.baeldung.com/java-buffered-reader)
- [Difference Between FileReader and BufferedReader in Java](https://www.baeldung.com/java-filereader-vs-bufferedreader)
- [Java: Read Multiple Inputs on Same Line](https://www.baeldung.com/java-read-multiple-inputs-same-line)
- [Write Console Output to Text File in Java](https://www.baeldung.com/java-write-console-output-file)

## Code

最后，我们可以在 [Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-io-apis) 上获取示例的源代码。
