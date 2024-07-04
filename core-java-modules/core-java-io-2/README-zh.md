# Core Java IO

## 如何使用Java高效读取大文件

1. 概述

    本教程将介绍如何用Java高效地读取一个大文件中的所有行。

    进一步阅读：

    [Java - 将输入流写入文件](https://www.baeldung.com/convert-input-stream-to-a-file)

    如何使用Java、Guava和Commons IO库将InputStream写入文件。

    [Java - 将文件转换为输入流](https://www.baeldung.com/convert-file-to-input-stream)

    如何从Java文件打开一个InputStream--使用纯Java、Guava和Apache Commons IO库。

2. 在内存中读取

    读取文件行数的标准方法是在内存中读取--Guava和Apache Commons IO都提供了快速读取的方法：

    `Files.readLines(new File(path), Charsets.UTF_8);`

    `FileUtils.readLines(new File(path));`

    这种方法的问题是所有文件行都保存在内存中--如果文件足够大，这将很快导致OutOfMemoryError。

    例如，读取一个~1Gb的文件：

    ```java
    @Test
    public void givenUsingGuava_whenIteratingAFile_thenWorks() throws IOException {
        String path = ...
        Files.readLines(new File(path), Charsets.UTF_8);
    }
    ```

    开始时会消耗少量内存：（消耗~0 Mb）

    ```log
    [main] INFO org.baeldung.java.CoreJavaIoUnitTest - Total Memory: 128 Mb
    [main] INFO org.baeldung.java.CoreJavaIoUnitTest - Free Memory: 116 Mb
    ```

    然而，在整个文件处理完毕后，我们在最后看到：（~2 Gb消耗）

    ```log
    [main] INFO org.baeldung.java.CoreJavaIoUnitTest - Total Memory: 2666 Mb
    [main] INFO org.baeldung.java.CoreJavaIoUnitTest - Free Memory： 490 Mb
    ```

    这意味着进程消耗了大约2.1Gb的内存--原因很简单--文件的行现在都存储在内存中。

    显而易见，将文件内容保存在内存中将很快耗尽可用内存--无论内存实际有多少。

    更重要的是，我们通常不需要同时将文件中的所有行都保存在内存中--相反，我们只需要能够遍历每一行，做一些处理，然后将其丢弃。因此，这正是我们要做的--在内存中不保留所有行的情况下遍历这些行。

3. 流式浏览文件

    现在，让我们探讨一下逐段读取给定文件的不同方法。

    1. 使用扫描器

        在这里，我们将使用一个java.util.Scanner来运行文件内容并逐行检索：

        ```java
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // System.out.println(line);
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
        ```

        该解决方案将遍历文件中的所有行，允许处理每一行而不保留对它们的引用。总之，在内存中不保留行的情况下： (~150 Mb consumed)

        ```log
        [main] INFO  org.baeldung.java.CoreJavaIoUnitTest - Total Memory: 763 Mb
        [main] INFO  org.baeldung.java.CoreJavaIoUnitTest - Free Memory: 605 Mb
        ```

    2. 使用缓冲阅读器

        另一种解决方案是使用[BufferedReader](https://www.baeldung.com/java-buffered-reader)类。

        通常，该类提供了一种方便的方法来缓冲字符，以简化读取文件的过程。

        为此，它提供了readLine()方法，可以逐行读取给定文件的内容。

        因此，让我们来看看实际操作：

        ```java
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while (br.readLine() != null) {
                // do something with each line
            }
        }
        ```

        BufferedReader通过逐块读取文件并将这些块缓存在内部缓冲区中来减少I/O操作的次数。

        与Scanner相比，它的性能更好，因为它只专注于数据检索而不进行解析。

    3. 使用Files.newBufferedReader()

        另外，我们也可以使用Files.newBufferedReader()方法来实现同样的目的：

        ```java
        try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(fileName))) {
        while (br.readLine() != null) {
            // do something with each line
        }

        ```

        正如我们所看到的，该方法提供了另一种返回BufferedReader实例的方法。

    4. 使用SeekableByteChannel

        SeekableByteChannel提供了一个读取和操作给定文件的通道。它的性能比标准的I/O类更快，因为它由一个自动调整大小的字节数组支持。

        那么，让我们来看看它的实际应用：

        ```java
        try (SeekableByteChannel ch = java.nio.file.Files.newByteChannel(Paths.get(fileName), StandardOpenOption.READ)) {
            ByteBuffer bf = ByteBuffer.allocate(1000);
            while (ch.read(bf) > 0) {
                bf.flip();
                // System.out.println(new String(bf.array()));
                bf.clear();
            }
        }
        ```

        如上所示，该接口带有 read() 方法，用于将字节序列读入 ByteBuffer 表示的缓冲区。

        通常情况下，flip()方法会使缓冲区重新为写入做好准备。另一方面，clear()，顾名思义，重置并清除缓冲区。

        这种方法的唯一缺点是我们需要使用allocate()方法明确指定缓冲区的大小。

    5. 使用流API

        同样，我们可以使用[Stream API](https://www.baeldung.com/java-8-streams)来读取和处理文件内容。

        在这里，我们将使用Files类，该类提供了lines()方法来返回String元素流：

        ```java
        try (Stream<String> lines = java.nio.file.Files.lines(Paths.get(fileName))) {
            lines.forEach(line -> {
                // do something with each line
            });
        }
        ```

        请注意，文件是懒散处理的，这意味着在给定时间内只有部分内容存储在内存中。

4. 使用Apache Commons IO进行流处理

    通过使用Commons IO库提供的自定义LineIterator也可以实现同样的效果：

    ```java
    LineIterator it = FileUtils.lineIterator(theFile, "UTF-8");
    try {
        while (it.hasNext()) {
            String line = it.nextLine();
            // do something with line
        }
    } finally {
        LineIterator.closeQuietly(it);
    }
    ```

    由于整个文件并不完全在内存中--这也将导致相当保守的内存消耗数字：（消耗~150 Mb）

    ```log
    [main] INFO  o.b.java.CoreJavaIoIntegrationTest - Total Memory: 752 Mb
    [main] INFO  o.b.java.CoreJavaIoIntegrationTest - Free Memory: 564 Mb
    ```

5. 结论

    这篇文章展示了如何在不耗尽可用内存的情况下迭代处理大文件中的行，这在处理这些大文件时非常有用。

## Java写入文件

1. 概述

    在本教程中，我们将探索使用 Java 写入文件的不同方法。我们将使用 BufferedWriter、PrintWriter、FileOutputStream、DataOutputStream、RandomAccessFile、FileChannel 和 Java 7 Files 实用程序类。

    我们还将讨论在写入文件时锁定文件的问题，并讨论写入文件的一些最终收获。

    [Java - 向文件追加数据](https://www.baeldung.com/java-append-to-file)

    向文件追加数据的快速实用指南。

    [Java 中的 FileNotFoundException](https://www.baeldung.com/java-filenotfound-exception)

    Java 中的 FileNotFoundException 快速实用指南。

    [如何用 Java 复制文件](https://www.baeldung.com/java-copy-file)

    了解在 Java 中复制文件的一些常见方法。

2. 使用 BufferedWriter 写入

    让我们从简单的开始，使用 BufferedWriter 将字符串写入新文件：

    whenWriteStringUsingBufferedWritter_thenCorrect()

    然后，我们可以在现有文件中追加一个字符串：

    whenAppendStringUsingBufferedWritter_thenOldContentShouldExistToo()

3. 使用 PrintWriter 写入

    接下来，让我们看看如何使用 PrintWriter 将格式化文本写入文件：

    givenWritingStringToFile_whenUsingPrintWriter_thenCorrect()

    请注意，我们不仅向文件写入了原始字符串，还使用 printf 方法写入了一些格式化文本。

    我们可以使用 FileWriter、BufferedWriter 或 System.out 创建写入器。

4. 使用 FileOutputStream 写入

    现在让我们看看如何使用 FileOutputStream 将二进制数据写入文件。

    下面的代码使用 FileOutputStream 将字符串转换为字节，并将字节写入文件：

    givenWritingStringToFile_whenUsingFileOutputStream_thenCorrect()

5. 使用 DataOutputStream 写入

    接下来，让我们看看如何使用 DataOutputStream 将字符串写入文件：

    givenWritingToFile_whenUsingDataOutputStream_thenCorrect()

6. 使用 RandomAccessFile 写入

    现在我们来说明如何在现有文件中写入和编辑，而不是直接写入一个全新的文件或追加到现有文件中。简而言之：我们需要随机访问。

    RandomAccessFile 使我们能够在文件中的特定位置写入，给定偏移量（从文件开头开始），单位为字节。

    这段代码将写入一个整数值，偏移量从文件开头给定：

    writeToPosition()

    如果我们想读取存储在特定位置的 int，可以使用此方法：

    readFromPosition()

    为了测试我们的函数，让我们写入一个整数，编辑它，最后读回它：

    whenWritingToSpecificPositionInFile_thenCorrect()

7. 使用 FileChannel 写入

    如果我们处理的是大文件，FileChannel 可能比标准 IO 更快。以下代码使用 FileChannel 将字符串写入文件：

    givenWritingToFile_whenUsingFileChannel_thenCorrect()

8. 写入文件类

    Java 7 引入了一种新的文件系统工作方式和一个新的实用程序类： 文件。

    使用 Files 类，我们可以创建、移动、复制和删除文件和目录。它还可以用来读取和写入文件：

    givenUsingJava7_whenWritingToFile_thenCorrect()

9. 写入临时文件

    现在让我们尝试写入临时文件。下面的代码创建了一个临时文件，并向其中写入了一个字符串：

    whenWriteToTmpFile_thenCorrect()

    正如我们所看到的，有趣且与众不同的只是创建临时文件的过程。在此之后，写入文件的过程都是一样的。

10. 写入前锁定文件

    最后，在写入文件时，我们有时需要额外确保没有其他人同时写入该文件。基本上，我们需要在写入时锁定文件。

    让我们使用 FileChannel 来尝试在写入文件前锁定文件：

    whenTryToLockFile_thenItShouldBeLocked()

    请注意，如果在我们尝试获取锁时文件已被锁定，则会抛出 OverlappingFileLockException 异常。

11. 注释

    在探索了这么多写入文件的方法后，让我们来讨论一些重要的注意事项：

    - 如果我们试图从一个不存在的文件中读取数据，将抛出 FileNotFoundException 异常。
    - 如果我们尝试向不存在的文件写入内容，文件将首先被创建，不会抛出异常。
    - 使用流后，关闭流是非常重要的，因为它不会隐式关闭，以释放与之相关的任何资源。
    - 在输出流中，close() 方法会在释放资源前调用 flush()，强制将缓冲字节写入流。

    纵观常见的使用方法，我们可以看到，例如，PrintWriter 用于写入格式化文本，FileOutputStream 用于写入二进制数据，DataOutputStream 用于写入原始数据类型，RandomAccessFile 用于写入特定位置，FileChannel 用于更快地写入较大的文件。这些类中的某些应用程序接口允许更多的功能，但这是一个很好的开始。

12. 总结

    本文介绍了使用 Java 向文件写入数据的多种选择。

## Relevant Articles

- [Create a File in a Specific Directory in Java](https://www.baeldung.com/java-create-file-in-directory)
- [x] [How to Read a Large File Efficiently with Java](https://www.baeldung.com/java-read-lines-large-file)
- [x] [Java – Write to File](https://www.baeldung.com/java-write-to-file)
- [FileNotFoundException in Java](https://www.baeldung.com/java-filenotfound-exception)
- [Delete the Contents of a File in Java](https://www.baeldung.com/java-delete-file-contents)
- [List Files in a Directory in Java](https://www.baeldung.com/java-list-directory-files)
- [Java – Append Data to a File](https://www.baeldung.com/java-append-to-file)
- [How to Copy a File with Java](https://www.baeldung.com/java-copy-file)
- [Create a Directory in Java](https://www.baeldung.com/java-create-directory) 
- [Java IO vs NIO](https://www.baeldung.com/java-io-vs-nio)
- [[<-- Prev]](/core-java-modules/core-java-io)[[More -->]](/core-java-modules/core-java-io-3)

## Code

所有这些示例和代码片段的实现都可以在我们的[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-io-2)项目中找到--这是一个基于Maven的项目，因此很容易导入和运行。
