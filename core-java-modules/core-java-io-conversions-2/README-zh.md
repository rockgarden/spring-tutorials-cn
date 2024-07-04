# 核心Java-IO转换（第二部分）

本模块包含有关核心Java输入/输出（IO）转换的文章。

## 如何将InputStream转换为Base64字符串

1. 概述

    [Base64](https://www.baeldung.com/java-base64-encode-and-decode)是一种文本编码方案，它为应用程序和平台之间的二进制数据提供可移植性。Base64可以用来将二进制数据存储在数据库的字符串列中，从而避免了混乱的文件操作。当与[数据URI方案](https://en.wikipedia.org/wiki/Data_URI_scheme)相结合时，Base64可以用来在网页和电子邮件中嵌入图像，以符合HTML和多用途互联网邮件扩展（MIME）标准。

    在这个简短的教程中，我们将演示Java Streaming IO函数和内置的Java Base64类来加载二进制数据作为InputStream，然后将其转换为字符串。

2. 设置

    让我们来看看代码的依赖性和我们需要的测试数据。

    1. 依赖性

        我们将使用Apache IOUtilsd库，通过在我们的pom.xml中添加它的依赖性来方便访问测试数据文件：

        ```xml
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.11.0</version>
        </dependency>
        ```

    2. 测试数据

        这里需要一个二进制的测试数据文件。所以我们要在我们的标准src/test/resources文件夹中添加一个logo.png图片文件。

3. 将InputStream转换为Base64字符串

    Java在java.util.Base64类中内置了对Base64编码和解码的支持。所以我们将使用那里的静态方法来完成繁重的工作。

    Base64.encode()方法期望的是一个字节数组，而我们的图片是在一个文件中。因此，我们需要首先将文件转换为InputStream，然后逐个字节地将流读入一个数组。

    我们使用Apache commons-io包中的IOUtils.toByteArray()方法，以方便替代只用Java的繁琐方法。

    首先，我们将写一个简单的方法来生成一个 "poor man's'" 的校验和：

    ```java
    int calculateChecksum(byte[] bytes) {
        int checksum = 0; 
        for (int index = 0; index < bytes.length; index++) {
            checksum += bytes[index]; 
        }
        return checksum; 
    }
    ```

    我们将用它来比较两个数组，验证它们是否匹配。

    接下来的几行将打开文件，将其转换为字节数组，然后将其Base64编码为一个字符串：

    ```java
    InputStream sourceStream  = getClass().getClassLoader().getResourceAsStream("logo.png");
    byte[] sourceBytes = IOUtils.toByteArray(sourceStream);

    String encodedString = Base64.getEncoder().encodeToString(sourceBytes); 
    assertNotNull(encodedString);
    ```

    这个字符串看起来像一个随机的字符块。事实上，它不是随机的，正如我们在验证步骤中看到的：

    ```java
    byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
    assertNotNull(decodedBytes);
    assertTrue(decodedBytes.length == sourceBytes.length);
    assertTrue(calculateChecksum(decodedBytes) == calculateChecksum(sourceBytes));
    ```

4. 总结

    在这篇文章中，我们演示了将InputStream编码为Base64字符串，并成功将该字符串解码为二进制数组。

## Relevant Articles

- [Java InputStream to String](https://www.baeldung.com/convert-input-stream-to-string)
- [Java String to InputStream](https://www.baeldung.com/convert-string-to-input-stream)
- [Java – Write an InputStream to a File](https://www.baeldung.com/convert-input-stream-to-a-file)
- [Converting a BufferedReader to a JSONObject](https://www.baeldung.com/java-bufferedreader-to-jsonobject)
- [Reading a CSV File into an Array](https://www.baeldung.com/java-csv-file-array)
- [How to Write to a CSV File in Java](https://www.baeldung.com/java-csv)
- [x] [How to Convert InputStream to Base64 String](https://www.baeldung.com/java-inputstream-to-base64-string)
- More articles: [[<-- prev]](/core-java-modules/core-java-io-conversions)

## Code

像往常一样，本文介绍的代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-io-conversions-2)上找到。
