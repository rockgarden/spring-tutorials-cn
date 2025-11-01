# [Java 中的 RSA](https://www.baeldung.com/java-rsa)

安全定义 | 安全算法

1. 引言

    RSA（[Rivest–Shamir–Adleman](https://www.baeldung.com/cs/rsa-public-key-format)）是一种**非对称加密算法**。它与 DES 或 AES 等对称算法不同：RSA 使用**一对密钥**——

    - **公钥（Public Key）**：可公开分享，用于**加密数据**；
    - **私钥（Private Key）**：必须严格保密，用于**解密数据**。

    在本教程中，我们将学习如何在 Java 中生成、存储和使用 RSA 密钥。

2. 生成 RSA 密钥对

    在加密之前，首先需要生成 RSA 密钥对。可使用 `java.security` 包中的 `KeyPairGenerator`：

    ```java
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048); // 指定密钥长度为 2048 位
    KeyPair pair = generator.generateKeyPair();
    ```

    生成的密钥对包含 2048 位的公钥和私钥（当前推荐的最小安全长度）。

    接着，提取公钥和私钥：

    ```java
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();
    ```

    我们将使用**公钥加密**，**私钥解密**。

3. 将密钥存储到文件

    将密钥长期保存在内存中并不现实。通常密钥会长期不变，因此更合理的做法是将其保存到文件中。

    **保存公钥到文件**：

    ```java
    try (FileOutputStream fos = new FileOutputStream("public.key")) {
        fos.write(publicKey.getEncoded()); // getEncoded() 返回标准编码格式（X.509）
    }
    ```

    **从文件读取公钥**：

    ```java
    File publicKeyFile = new File("public.key");
    byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
    ```

    说明：

    - `getEncoded()` 默认使用 **X.509 标准**编码公钥；
    - 因此读取时需使用 `X509EncodedKeySpec`；
    - 私钥的处理方式类似，但默认使用 **PKCS#8** 编码，应使用 `PKCS8EncodedKeySpec`。

    **私钥存储示例**：

    ```java
    // 保存私钥
    try (FileOutputStream fos = new FileOutputStream("private.key")) {
        fos.write(privateKey.getEncoded()); // 默认为 PKCS#8 格式
    }

    // 读取私钥
    byte[] privateKeyBytes = Files.readAllBytes(new File("private.key").toPath());
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
    ```

    **重要安全提示**：私钥文件必须严格保护，访问权限应尽可能受限。一旦私钥泄露，整个加密体系将失效。

4. 字符串加解密

    现在我们对字符串进行加解密操作。

    **加密**：

    ```java
    String secretMessage = "Baeldung secret message";
    // 需要一个 Cipher 对象，并使用之前生成的公钥将其初始化为加密模式：
    Cipher encryptCipher = Cipher.getInstance("RSA");
    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
    // 调用 doFinal 方法来加密消息。请注意，该方法仅接受字节数组作为参数，因此我们需要先将字符串转换为字节数组：
    byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

    // 为便于传输或存储，可进行 Base64 编码
    String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    ```

    **解密**：

    ```java
    // 需要另一个 Cipher 实例。这次我们将它初始化为解密模式，并使用私钥：
    Cipher decryptCipher = Cipher.getInstance("RSA");
    decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
    // 调用 doFinal 方法进行解密：
    byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
    String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

    assertEquals(secretMessage, decryptedMessage);
    ```

5. 文件加解密

    RSA 也可用于加密整个文件（但**仅适用于小文件**，原因见下文）。

    **加密文件**：

    ```java
    // 创建一个包含文本内容的临时文件：
    Path tempFile = Files.createTempFile("temp", "txt");
    Files.writeString(tempFile, "some secret message", StandardCharsets.UTF_8);
    // 将其内容转换为字节数组：
    byte[] fileBytes = Files.readAllBytes(tempFile);
    // 使用加密用的 Cipher：
    Cipher encryptCipher = Cipher.getInstance("RSA");
    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encryptedFileBytes = encryptCipher.doFinal(fileBytes);
    // 用新的加密内容覆盖原文件：
    try (FileOutputStream stream = new FileOutputStream(tempFile.toFile())) {
        stream.write(encryptedFileBytes);
    }
    ```

    **解密文件**：

    ```java
    byte[] encryptedFileBytes = Files.readAllBytes(tempFile);
    Cipher decryptCipher = Cipher.getInstance("RSA");
    // 使用私钥将 Cipher 初始化为解密模式：
    decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decryptedFileBytes = decryptCipher.doFinal(encryptedFileBytes);

    try (FileOutputStream stream = new FileOutputStream(tempFile.toFile())) {
        stream.write(decryptedFileBytes);
    }

    String fileContent = Files.readString(tempFile, StandardCharsets.UTF_8);
    Assertions.assertEquals("some secret message", fileContent);
    ```

    **重要限制**：
    - RSA 是**非对称算法**，加密速度慢，且**单次加密数据长度受限**（2048 位密钥最多加密约 245 字节）。
    - **不适用于大文件加密**！
    - 实际应用中，通常使用 RSA 加密一个**对称密钥（如 AES 密钥）**，再用该对称密钥加密大文件（混合加密）。

6. 总结

    本文介绍了如何在 Java 中生成 RSA 密钥对，并使用它们对字符串和小文件进行加密与解密。

    **最佳实践建议**：

    - 使用 **2048 位或更长**的密钥；
    - 私钥必须安全存储（如加密存储、访问控制）；
    - 大数据加密应采用 **RSA + AES 混合方案**；
    - 考虑使用更安全的填充模式，如 `RSA/ECB/OAEPWithSHA-256AndMGF1Padding`，而非默认的 PKCS#1 v1.5（易受某些攻击）。
