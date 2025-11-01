# [Java 中的文件加密与解密](https://www.baeldung.com/java-cipher-input-output-stream)

Java IO | 安全

1. 概述

    在本教程中，我们将学习如何使用 JDK 自带的 API 对文件进行加密和解密。

2. 先编写测试（TDD 风格）

    我们采用测试驱动开发（TDD）的方式，首先编写一个集成测试（因为涉及文件操作）。

    由于仅使用 JDK 内置功能，无需额外依赖。

    测试逻辑如下：

    - 使用新生成的密钥对内容进行加密（本例使用对称加密算法 [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)）；
    - 假设 `FileEncrypterDecrypter` 类会将加密结果写入名为 `baz.enc` 的文件；
    - 然后使用相同的密钥解密该文件；
    - 验证解密后的内容是否与原始内容一致。

    ```java
    @Test
    public void whenEncryptingIntoFile_andDecryptingFileAgain_thenOriginalStringIsReturned() {
        String originalContent = "foobar";
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();

        FileEncrypterDecrypter fileEncrypterDecrypter
        = new FileEncrypterDecrypter(secretKey, "AES/CBC/PKCS5Padding");
        fileEncrypterDecrypter.encrypt(originalContent, "baz.enc");

        String decryptedContent = fileEncrypterDecrypter.decrypt("baz.enc");
        assertThat(decryptedContent, is(originalContent));

        new File("baz.enc").delete(); // 清理临时文件
    }
    ```

    > 注意：我们在构造函数中指定了完整的转换字符串 `"AES/CBC/PKCS5Padding"`，其格式为 `算法/模式/填充`。JDK 默认支持多种转换组合，但并非所有组合在当前标准下仍被视为安全。

3. 加密实现

    在 `FileEncrypterDecrypter` 构造函数中初始化 `Cipher`，以便在转换字符串错误时尽早失败：

    ```java
    FileEncrypterDecrypter(SecretKey secretKey, String transformation) {
        this.secretKey = secretKey;
        this.cipher = Cipher.getInstance(transformation);
    }
    ```

    加密方法如下：

    ```java
    void encrypt(String content, String fileName) {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV(); // 自动为 CBC 模式生成 IV

        try (FileOutputStream fileOut = new FileOutputStream(fileName);
            CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
            fileOut.write(iv);               // 先写入 IV（16 字节）
            cipherOut.write(content.getBytes()); // 再写入加密后的内容
        }
    }
    ```

    这里利用了 JDK 提供的 `CipherOutputStream`，它能自动将写入的数据加密后传递给底层输出流。

    **关键点**：

    - 在 CBC 模式下，**必须使用初始化向量（[IV](https://en.wikipedia.org/wiki/Initialization_vector)）** 以确保相同明文每次加密结果不同；
    - IV **不是密钥**，无需保密，通常与密文一起存储（如文件开头）；
    - 此处 `cipher.init()` 会自动生成一个随机 IV，可通过 `cipher.getIV()` 获取。

4. 解密实现

    解密时需先读取文件开头的 IV，再用它初始化 `Cipher`：

    ```java
    String decrypt(String fileName) {
        String content;

        try (FileInputStream fileIn = new FileInputStream(fileName)) {
            // 读取前 16 字节作为 IV
            byte[] fileIv = new byte[16];
            fileIn.read(fileIv);

            // 使用 IV 初始化解密模式
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));

            // 使用 CipherInputStream 自动解密
            try (CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
                InputStreamReader inputReader = new InputStreamReader(cipherIn);
                BufferedReader reader = new BufferedReader(inputReader)) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                content = sb.toString();
            }
        }
        return content;
    }
    ```

    `CipherInputStream` 会透明地处理解密过程：从底层输入流读取密文，自动解密后提供给上层读取器。

5. 结论

    我们展示了如何使用 JDK 标准类（如 `Cipher`、`CipherOutputStream` 和 `CipherInputStream`）实现基本的文件加解密功能。

    重要提示：

    - 实际应用中需考虑：密钥安全存储、IV 长度验证、字符编码（建议显式指定 UTF-8）、异常处理、大文件流式处理等；
    - 推荐使用更安全的模式如 **AES/GCM**（提供认证加密），而非 CBC；
    - JDK 支持的加密算法列表可在[此处查阅](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/Cipher.html)。
