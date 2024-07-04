# Core Java IO

## 如何用Java读取文件

1. 概述

    在本教程中，我们将探讨 Java 中读取文件的不同方法。

    首先，我们将学习如何使用标准 Java 类从 classpath、URL 或 JAR 文件加载文件。

    其次，我们将了解如何使用 BufferedReader、Scanner、StreamTokenizer、DataInputStream、SequenceInputStream 和 FileChannel 读取内容。我们还将讨论如何读取 UTF-8 编码的文件。

    最后，我们将探讨在 Java 7 和 Java 8 中加载和读取文件的新技术。

    [Java - 创建文件](https://www.baeldung.com/java-how-to-create-a-file)

    如何使用 JDK 6、JDK 7 的 NIO 或 Commons IO 在 Java 中创建文件。

    [Java - 写入文件](https://www.baeldung.com/java-write-to-file)

    使用 Java 向文件写入数据的多种方法。

    参见代码：JavaReadFromFileUnitTest.java

2. 设置

    1. 输入文件

        在本文的大多数示例中，我们将读取一个文本文件，文件名为 fileTest.txt，其中包含一行内容：

        `Hello, world!`

        在少数示例中，我们会使用不同的文件；在这种情况下，我们会明确提及文件及其内容。

    2. 辅助方法

        我们将使用一组仅包含核心 Java 类的测试示例，并在测试中使用带有 [Hamcrest](https://www.baeldung.com/hamcrest-collections-arrays) 匹配器的断言。

        测试将共享一个共同的 readFromInputStream 方法，该方法可将 InputStream 转换为字符串，以便更轻松地断言结果。

        请注意，还有其他方法可以实现相同的结果。我们可以参考这篇[文章](https://www.baeldung.com/convert-input-stream-to-a-file)，了解一些替代方法。

3. 从分类路径读取文件

    1. 使用标准 Java

        本节介绍如何读取类路径上的文件。我们将读取 src/main/resources 下的 "fileTest.txt"：

        givenFileNameAsAbsolutePath_whenUsingClasspath_thenFileData()

        在上述代码片段中，我们使用 getResourceAsStream 方法在当前类中加载文件，并传递了要加载文件的绝对路径。

        同样的方法也可用于 ClassLoader 实例：

        ```java
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("fileTest.txt");
        String data = readFromInputStream(inputStream);
        ```

        我们使用 getClass().getClassLoader() 获得当前类的 classLoader。

        主要区别在于，在 ClassLoader 实例上使用 getResourceAsStream 时，路径被视为绝对路径，从 classpath 的根开始。

        而对 Class 实例使用时，路径可能是包的相对路径，也可能是绝对路径，前端的斜线就是暗示。

        当然，需要注意的是，在实际操作中，打开的流应该总是关闭的，例如我们示例中的 InputStream：

        ```java
        InputStream inputStream = null;
        try {
            File file = new File(classLoader.getResource("fileTest.txt").getFile());
            inputStream = new FileInputStream(file);
            
            //...
        }     
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ```

    2. 使用 commons-io 库

        另一种常用的方法是使用 [commons-io](https://www.baeldung.com/apache-commons-io) 软件包中的 FileUtils 类：

        givenFileName_whenUsingFileUtils_thenFileData()

        在这里，我们将文件对象传递给 FileUtils 类的 readFileToString() 方法。该实用程序类无需编写任何模板代码来创建 InputStream 实例和读取数据，即可加载内容。

        该库还提供 IOUtils 类：

        givenFileName_whenUsingIOUtils_thenFileData()

        在这里，我们将 FileInputStream 对象传递给 IOUtils 类的 toString() 方法。该实用程序类的操作方法与前一个类相同，都是创建 InputStream 实例并读取数据。

4. 使用缓冲阅读器读取数据

    现在，让我们关注解析文件内容的不同方法。

    我们将从使用 BufferedReader 从文件读取数据的简单方法开始：

    whenReadWithBufferedReader_thenCorrect()

    请注意，当到达文件末尾时，readLine() 将返回空值。

5. 使用 Java NIO 从文件读取数据

    在 JDK7 中，NIO 包进行了重大更新。

    让我们来看一个使用 Files 类和 readAllLines 方法的示例。readAllLines 方法接受一个 Path。

    Path 类可视为 java.io.File 的升级版，并增加了一些操作。

    1. 读取小文件

        以下代码展示了如何使用新的 Files 类读取一个小文件：

        whenReadSmallFileJava7_thenCorrect()

        请注意，如果我们需要二进制数据，也可以使用 readAllBytes() 方法。

    2. 读取大文件

        如果我们想用 Files 类读取大文件，可以使用 BufferedReader。

        以下代码使用新的 Files 类和 BufferedReader 读取文件：

        void whenReadLargeFileJava7_thenCorrect()

    3. 使用 Files.lines() 读取文件

        JDK8 在 Files 类中提供了 lines() 方法。它返回一个字符串元素流。

        下面我们来看一个如何将数据读入字节并使用 UTF-8 字符集解码的示例。

        以下代码使用新的 Files.lines() 方法读取文件：

        givenFilePath_whenUsingFilesLines_thenFileData()

        使用流和 IO 通道（如文件操作）时，我们需要使用 close() 方法显式关闭流。

        正如我们所见，文件 API 提供了另一种将文件内容读入字符串的简便方法。

6. 使用扫描仪读取

    接下来，让我们使用扫描器读取文件。这里我们将使用空白作为分隔符：

    whenReadWithScanner_thenCorrect()

    请注意，默认分隔符是空白，但扫描器可以使用多个分隔符。

    [Scanner](https://www.baeldung.com/java-scanner) 类在从控制台读取内容时，或在内容包含已知分隔符的原始值时（例如：用空格分隔的整数列表）非常有用。

7. 使用 StreamTokenizer 读取

    现在，让我们使用 [StreamTokenizer](https://www.baeldung.com/java-streamtokenizer) 将文本文件读取为标记。

    令牌转换器的工作原理是首先确定下一个令牌是什么，字符串还是数字。为此，我们需要查看 tokenizer.ttype 字段。

    然后，我们将根据该类型读取实际的标记：

    - tokenizer.nval - 如果类型是数字
    - tokenizer.sval - 如果类型是字符串

    在本例中，我们将使用一个不同的输入文件，其中只包含

    `Hello 1`

    下面的代码将从文件中读取字符串和数字：

    whenReadWithStreamTokenizer_thenCorrectTokens()

    请注意文件结尾标记是如何使用的。

    这种方法适用于将输入流解析为标记。

8. 使用 DataInputStream 读取

    我们可以使用 DataInputStream 从文件中读取二进制或原始数据类型。

    下面的测试使用 DataInputStream 读取文件：

    whenReadWithDataInputStream_thenCorrect()

9. 使用 FileChannel 读取

    如果要读取大文件，FileChannel 可能比标准 IO 更快。

    下面的代码使用 FileChannel 和 RandomAccessFile 从文件中读取数据字节：

    whenReadWithFileChannel_thenCorrect()

10. 读取 UTF-8 编码文件

    现在让我们看看如何使用 BufferedReader 读取 UTF-8 编码的文件。在本例中，我们将读取一个包含汉字的文件：

    whenReadUTFEncodedFile_thenCorrect()

11. 从 URL 读取内容

    要从 URL 读取内容，我们将在示例中使用"/"URL：

    givenURLName_whenUsingURL_thenFileData()

    还有其他连接 URL 的方法。这里我们使用了标准 SDK 中的 URL 和 URLConnection 类。

12. 从 JAR 中读取文件

    要读取位于 JAR 文件中的文件，我们需要一个内含文件的 JAR。在我们的示例中，我们将从 "hamcrest-library-1.3.jar" 文件中读取 "LICENSE.txt"：

    givenFileName_whenUsingJarFile_thenFileData()

    在此，我们要加载 Hamcrest 库中的 LICENSE.txt，因此我们将使用有助于获取资源的 Matcher 类。同样的文件也可以使用类加载器加载。

13. 结论

    正如我们所看到的，使用普通 Java 加载文件并从中读取数据有多种可能性。

    我们可以从 classpath、URL 或 jar 文件等不同位置加载文件。

    然后，我们可以使用 BufferedReader 逐行读取，使用 Scanner 使用不同的分隔符读取，使用 StreamTokenizer 将文件读取为标记，使用 DataInputStream 读取二进制数据和原始数据类型，使用 SequenceInput Stream 将多个文件连接为一个流，使用 FileChannel 更快地从大文件中读取数据，等等。

## Relevant Articles

- [x] [How to Read a File in Java](https://www.baeldung.com/reading-file-in-java)
- [Read a File into an ArrayList](https://www.baeldung.com/java-file-to-arraylist)
- [Java – Directory Size](https://www.baeldung.com/java-folder-size)
- [File Size in Java](https://www.baeldung.com/java-file-size)
- [Zipping and Unzipping in Java](https://www.baeldung.com/java-compress-and-uncompress)
- [How to Get the File Extension of a File in Java](https://www.baeldung.com/java-file-extension)
- [Getting a File’s Mime Type in Java](https://www.baeldung.com/java-file-mime-type)
- [How to Avoid the Java FileNotFoundException When Loading Resources](https://www.baeldung.com/java-classpath-resource-cannot-be-opened)
- [Java – Rename or Move a File](https://www.baeldung.com/java-how-to-rename-or-move-a-file)
- [[More -->]](/core-java-modules/core-java-io-2)

## Code

我们可以在以下 [GitHub](https://www.baeldung.com/reading-file-in-java) 代码库中找到本文的源代码。
