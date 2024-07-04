# Core Java IO

## Java创建文件

1. 概述

   在本快速教程中，我们将学习如何在 Java 中创建一个新文件--首先使用 NIO 中的 Files 和 Path 类，然后使用 Java File 和 FileOutputStream 类、[Google Guava](https://github.com/google/guava)，最后使用 [Apache Commons IO](http://commons.apache.org/proper/commons-io/) 库。

   代码参见：CreateFileUnitTest.java

2. 设置

   在示例中，我们将为文件名定义一个常量：FILE_NAME

   我们还将添加一个清理步骤，以确保每次测试前文件不存在，并在每次测试运行后删除文件：

   cleanUpFiles()

3. 使用 NIO Files.createFile()

   让我们从使用 Java NIO 软件包中的 Files.createFile() 方法开始：

   givenUsingNio_whenCreatingFile_thenCorrect()

   正如您所看到的，代码仍然非常简单；我们现在使用的是新的 Path 接口，而不是旧的 File 接口。

   值得注意的是，新的 API 很好地利用了异常。如果文件已经存在，我们不再需要检查返回代码。取而代之的是 FileAlreadyExistsException 异常：

   ```log
   java.nio.file.FileAlreadyExistsException: <code class="language-java">src/test/resources/fileToCreate.txt`
   at sun.n.f.WindowsException.translateToIOException(WindowsException.java:81)
   ```

4. 使用 File.createNewFile()

   现在让我们看看如何使用 java.io.File 类来完成同样的操作：

   givenUsingFile_whenCreatingFile_thenCorrect()

   请注意，文件必须不存在，此操作才能成功。如果文件确实存在，则 createNewFile() 操作将返回 false。

5. 使用 FileOutputStream

   另一种创建新文件的方法是使用 java.io.FileOutputStream：

   givenUsingFileOutputStream_whenCreatingFile_thenCorrect()

   在这种情况下，当我们实例化 FileOutputStream 对象时，会创建一个新文件。如果给定名称的文件已经存在，它将被覆盖。但是，如果现有文件是一个目录，或者由于某种原因无法创建新文件，那么我们将收到 FileNotFoundException 异常。

   此外，请注意我们使用了 try-with-resources 语句，以确保流已正确关闭。

6. 使用 Guava

   创建新文件的 Guava 解决方案也是快速的单行程序：

   givenUsingGuava_whenCreatingFile_thenCorrect()

7. 使用 Apache Commons IO

   Apache Commons 库提供了 FileUtils.touch() 方法，该方法实现了与 Linux 中 "touch "工具相同的行为。

   因此，它可以在文件系统中创建一个新的空文件，甚至是一个文件及其完整路径：

   givenUsingCommonsIo_whenCreatingFile_thenCorrect()

   请注意，这与前面的示例略有不同：如果文件已经存在，操作不会失败，只是什么也不做。

   这就是在 Java 中创建新文件的 4 种快速方法。

8. 总结

   在本文中，我们介绍了在 Java 中创建文件的不同解决方案。我们使用了 JDK 中的类和外部库。

## Relevant Articles

- [x] [Java – Create a File](https://www.baeldung.com/java-how-to-create-a-file)
- [Check If a Directory Is Empty in Java](https://www.baeldung.com/java-check-empty-directory)
- [Check If a File or Directory Exists in Java](https://www.baeldung.com/java-file-directory-exists)
- [Copy a Directory in Java](https://www.baeldung.com/java-copy-directory)
- [Java Files Open Options](https://www.baeldung.com/java-file-options)
- [Creating Temporary Directories in Java](https://www.baeldung.com/java-temp-directories)
- [Reading a Line at a Given Line Number From a File in Java](https://www.baeldung.com/java-read-line-at-number)
- [Find the Last Modified File in a Directory with Java](https://www.baeldung.com/java-last-modified-file)
- [Get a Filename Without the Extension in Java](https://www.baeldung.com/java-filename-without-extension)
- [Writing byte[] to a File in Java](https://www.baeldung.com/java-write-byte-array-file)
- [[<-- Prev]](/core-java-modules/core-java-io-2) [More -->)](/core-java-modules/core-java-io-4)

## Code

示例代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-io-3) 上获取。
