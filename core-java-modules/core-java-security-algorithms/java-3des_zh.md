# [Java 中的 3DES](https://www.baeldung.com/java-3des)

安全

1. 引言

    3DES（Triple Data Encryption Algorithm，[三重数据加密算法](https://en.wikipedia.org/wiki/Triple_DES)）是一种对称密钥分组密码，它对每个数据块连续应用三次 DES 加密算法。

    在本教程中，我们将学习如何在 Java 中生成 3DES 密钥，并使用它对字符串和文件进行加密与解密。

2. 生成密钥

    生成 3DES 密钥需要几个步骤。首先，我们需要创建一个用于加解密过程的密钥。在示例中，我们使用一个由随机字母和数字组成的 24 字节密钥：

    ```java
    byte[] secretKey = "9mng65v8jf4lxn93nabf981m".getBytes();
    ```

    > 注意：密钥绝不应公开共享。

    接着，我们将该密钥封装到 `SecretKeySpec` 中，并指定加密算法：

    ```java
    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "TripleDES");
    ```

    这里我们使用的是 `TripleDES`，它是 Java [安全标准算法](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html)之一。

    此外，我们还需要提前生成一个初始化向量（[IV](https://en.wikipedia.org/wiki/Initialization_vector)）。我们使用一个 8 字节的随机字母数字数组：

    ```java
    byte[] iv = "a76nb5h9".getBytes();
    ```

    然后将其封装为 `IvParameterSpec`：

    ```java
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    ```

3. 字符串加密

    现在我们可以对简单的字符串进行加密了。首先定义要加密的字符串：

    ```java
    String secretMessage = "Baeldung secret message";
    ```

    接下来，创建一个 Cipher 对象，并使用加密模式、密钥和初始化向量进行初始化：

    ```java
    Cipher encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
    ```

    > 注意：我们使用的是 TripleDES 算法，配合 [CBC](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation)（密码分组链接）模式和 [PKCS#5 填充方案](https://en.wikipedia.org/wiki/Padding_(cryptography))。

    使用该 `Cipher`，调用 `doFinal` 方法即可加密消息。由于该方法只接受字节数组，我们需要先将字符串转换为字节：

    ```java
    byte[] secretMessagesBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessagesBytes);
    ```

    现在消息已成功加密。如果要将其存储到数据库或通过 [REST API](https://www.baeldung.com/rest-with-spring-series) 传输，建议使用 [Base64 编码](https://www.baeldung.com/java-base64-encode-and-decode)，使其更易读且便于处理：

    ```java
    String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    ```

4. 字符串解密

    接下来，我们逆转加密过程，将密文还原为原始明文。为此，需要创建一个新的 `Cipher` 实例，但这次初始化为解密模式：

    ```java
    Cipher decryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
    decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
    ```

    然后调用 `doFinal` 方法进行解密：

    ```java
    byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
    ```

    将解密后的字节数组转换回字符串：

    ```java
    String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
    ```

    最后，验证解密结果是否与原始消息一致：

    ```java
    Assertions.assertEquals(secretMessage, decryptedMessage);
    ```

5. 文件加解密

    我们也可以对整个文件进行加密。例如，先创建一个包含文本内容的临时文件：

    ```java
    String originalContent = "Secret Baeldung message";
    Path tempFile = Files.createTempFile("temp", "txt");
    writeString(tempFile, originalContent);
    ```

    将文件内容读取为字节数组：

    ```java
    byte[] fileBytes = Files.readAllBytes(tempFile);
    ```

    使用与字符串加密相同的加密器进行加密：

    ```java
    Cipher encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
    byte[] encryptedFileBytes = encryptCipher.doFinal(fileBytes);
    ```

    将加密后的内容写回文件：

    ```java
    try (FileOutputStream stream = new FileOutputStream(tempFile.toFile())) {
        stream.write(encryptedFileBytes);
    }
    ```

    解密过程类似，只需将 `Cipher` 初始化为解密模式：

    ```java
    encryptedFileBytes = Files.readAllBytes(tempFile);
    Cipher decryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
    decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
    byte[] decryptedFileBytes = decryptCipher.doFinal(encryptedFileBytes);
    ```

    将解密后的内容写回文件：

    ```java
    try (FileOutputStream stream = new FileOutputStream(tempFile.toFile())) {
        stream.write(decryptedFileBytes);
    }
    ```

    最后验证文件内容是否与原始内容一致：

    ```java
    String fileContent = readString(tempFile);
    Assertions.assertEquals(originalContent, fileContent);
    ```

6. 总结

    本文介绍了如何在 Java 中创建 3DES 密钥，并使用它对字符串和文件进行加密与解密。

    > **重要提示**：  
    > 尽管 3DES 曾被广泛使用，但 NIST 已于 2017 年宣布弃用 3DES（除特定遗留场景外），并推荐使用更安全、更高效的 **AES** 算法。在新项目中，应优先选择 AES 而非 3DES。
