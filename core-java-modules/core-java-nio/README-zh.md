# Core Java NIO

## NIO2文件API简介

1. 概述

    在本文中，我们将重点介绍 Java 平台中用于执行基本文件操作的新 I/O API - NIO2。

    NIO2 中的文件 API 是 Java 7 中 Java 平台的主要新功能领域之一，特别是新文件系统 API 的一个子集，与 Path API 并列。

2. 设置

    设置您的项目以使用文件 API，只需进行以下导入即可：

    import java.nio.file.*;

    由于本文中的代码示例可能会在不同的环境中运行，因此让我们来处理一下用户的主目录，它将在所有操作系统中有效：

    `private static String HOME = System.getProperty("user.home");`

    Files 类是 java.nio.file 包的主要入口之一。该类为文件和目录的读取、写入和操作提供了丰富的 API。Files 类的方法作用于 Path 对象的实例。

    代码参见：FileIntegrationTest.java

3. 检查文件或目录

    我们可以用一个 Path 实例来代表文件系统中的一个文件或一个目录。它所指向的文件或目录是否存在，是否可以访问，都可以通过文件操作来确认。

    为简单起见，除非另有明确说明，否则在使用文件一词时，我们既指文件也指目录。

    要检查文件是否存在，我们可以使用 exists API：

    givenExistentPath_whenConfirmsFileExists_thenCorrect()

    要检查文件是否存在，我们使用 notExists API：

    givenNonexistentPath_whenConfirmsFileNotExists_thenCorrect()

    我们还可以使用 isRegularFile API 来检查文件是 myfile.txt 这样的普通文件还是目录：

    givenDirPath_whenConfirmsNotRegularFile_thenCorrect()

    还有检查文件权限的静态方法。要检查文件是否可读，我们使用 isReadable API：

    givenExistentDirPath_whenConfirmsReadable_thenCorrect()

    要检查是否可写，我们使用 isWritable API：

    givenExistentDirPath_whenConfirmsWritable_thenCorrect()

    同样，检查文件是否可执行：

    givenExistentDirPath_whenConfirmsExecutable_thenCorrect()

    有了两个路径后，我们可以检查它们是否都指向底层文件系统中的同一个文件：

    givenSameFilePaths_whenConfirmsIsame_thenCorrect()

4. 创建文件

    文件系统 API 为创建文件提供了单行操作。要创建普通文件，我们使用 createFile API，并向其传递一个代表要创建文件的路径对象。

    除了文件名外，路径中的所有名称元素都必须存在，否则我们将收到 IOException 异常：

    givenFilePath_whenCreatesNewFile_thenCorrect()

    在上述测试中，当我们第一次检查路径时，发现路径不存在，而在执行 createFile 操作后，发现路径是存在的。

    要创建目录，我们使用 createDirectory API：

    givenDirPath_whenCreatesNewDir_thenCorrect()

    此操作要求路径中的所有名称元素都存在，如果不存在，我们也会收到 IOException：

    givenDirPath_whenFailsToCreateRecursively_thenCorrect()

    不过，如果我们想通过一次调用创建一个层次结构的目录，我们可以使用 createDirectories 方法。与前一个操作不同的是，当遇到路径中缺少名称元素时，它不会抛出 IOException，而是以递归方式创建，直到最后一个元素：

    givenDirPath_whenCreatesRecursively_thenCorrect()

5. 创建临时文件

    许多应用程序在运行时都会在文件系统中创建大量临时文件。因此，大多数文件系统都有一个专门的目录来存储此类应用程序生成的临时文件。

    新的文件系统 API 为此提供了特定的操作。createTempFile API 可执行此操作。它需要一个路径对象、一个文件前缀和一个文件后缀：

    givenFilePath_whenCreatesTempFile_thenCorrect()

    这些参数足以满足需要此操作的要求。不过，如果需要指定文件的特定属性，还可以使用第四个变量参数。

    上述测试在 HOME 目录中创建了一个临时文件，并分别预置和追加了所提供的前缀和后缀字符串。最后我们将得到一个文件名，如 log_8821081429012075286.txt。长数字字符串由系统生成。

    但是，如果我们不提供前缀和后缀，那么文件名将只包含长数字字符串和默认的 .tmp 扩展名：

    givenPath_whenCreatesTempFileWithDefaults_thenCorrect()

    最后，如果我们既不提供路径、前缀，也不提供后缀，那么整个操作都将使用默认值。创建文件的默认位置是文件系统提供的临时文件目录：

    givenNoFilePath_whenCreatesTempFileInTempDir_thenCorrect()

    通过使用 createTempDirectory 而不是 createTempFile，可以将上述所有操作调整为创建目录而不是普通文件。

6. 删除文件

    要删除文件，我们需要使用 delete API。为清晰起见，以下测试首先确保文件不存在，然后创建文件并确认其已存在，最后删除文件并确认其不再存在：

    givenPath_whenDeletes_thenCorrect()

    但是，如果文件系统中不存在文件，删除操作就会失败，并出现 IOException：

    givenInexistentFile_whenDeleteFails_thenCorrect()

    我们可以通过使用 deleteIfExists 来避免这种情况，因为它会在文件不存在的情况下静默失败。当多个线程执行此操作时，这一点非常重要，我们不希望仅仅因为某个线程比当前线程更早执行操作而出现失败消息：

    givenInexistentFile_whenDeleteIfExistsWorks_thenCorrect()

    在处理目录而非普通文件时，我们应该记住，删除操作默认情况下不递归。因此，如果一个目录不是空的，就会出现 IOException 异常：

    givenPath_whenFailsToDeleteNonEmptyDir_thenCorrect()

7. 复制文件

    您可以使用复制 API 复制文件或目录：

    givenFilePath_whenCopiesToNewLocation_thenCorrect()

    除非指定 REPLACE_EXISTING 选项，否则如果目标文件存在，复制将失败：

    givenPath_whenCopyFailsDueToExistingFile_thenCorrect()

    不过，复制目录时，内容不会递归复制。这意味着，如果 /baeldung 包含 /articles.db 和 /authors.db 文件，将 /baeldung 复制到新位置将创建一个空目录。

8. 移动文件

    您可以使用 move API 移动文件或目录。它在大多数方面与复制操作类似。如果说复制操作类似于基于图形用户界面的系统中的复制和粘贴操作，那么移动操作则类似于剪切和粘贴操作：

    givenFilePath_whenMovesToNewLocation_thenCorrect()

    如果目标文件存在，除非指定 REPLACE_EXISTING 选项，否则移动操作将失败，就像我们在复制操作中所做的一样：

    givenFilePath_whenMoveFailsDueToExistingFile_thenCorrect()

9. 结论

    在本文中，我们了解了作为 Java 7 一部分发布的新文件系统 API (NIO2) 中的文件 API，并看到了大部分重要文件操作的实际应用。

## Relevant Articles

- [Determine File Creation Date in Java](https://www.baeldung.com/java-file-creation-date)
- [Find the Number of Lines in a File Using Java](https://www.baeldung.com/java-file-number-of-lines)
- [A Guide To NIO2 Asynchronous File Channel](https://www.baeldung.com/java-nio2-async-file-channel)
- [A Guide To NIO2 FileVisitor](https://www.baeldung.com/java-nio2-file-visitor)
- [Guide to Java FileChannel](https://www.baeldung.com/java-filechannel)
- [A Guide To NIO2 File Attribute APIs](https://www.baeldung.com/java-nio2-file-attribute)
- [Introduction to the Java NIO2 File API](https://www.baeldung.com/java-nio-2-file-api)
- [Java NIO2 Path API](https://www.baeldung.com/java-nio-2-path)
- [Guide to Java NIO2 Asynchronous Channel APIs](https://www.baeldung.com/java-nio-2-async-channels)
- [A Guide to NIO2 Asynchronous Socket Channel](https://www.baeldung.com/java-nio2-async-socket-channel)
- [[More -->]](/core-java-modules/core-java-nio-2)

## Code

本文中使用的代码示例可在本文的 [Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-nio) 项目中找到。
