# [Cipher 类使用指南](https://www.baeldung.com/java-cipher-class)

核心 Java

1. 概述

    简而言之，**加密**是将消息编码，使得只有授权用户才能理解或访问该消息的过程。

    原始消息（称为**明文**）通过加密算法（即**密码算法**，cipher）进行加密，生成**密文**。只有拥有相应权限的用户才能通过解密操作读取原始内容。

    本文将详细介绍 Java 中提供加密与解密功能的核心类——`Cipher` 类。

2. Cipher 类

    Java 加密扩展（JCE）是 Java 加密架构（JCA）的一部分，为应用程序提供用于数据加解密及私有数据哈希的密码算法。

    [`Cipher`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/Cipher.html) 类位于 `javax.crypto` 包中，是 JCE 框架的核心，提供了加密和解密的功能。

    1. Cipher 实例化

        要创建 `Cipher` 对象，需调用其静态方法 `getInstance()`，并传入所需的**转换名称（transformation）**。也可以选择性地指定安全提供者（provider）。

        下面是一个示例类，演示如何实例化 `Cipher`：

        ```java
        public class Encryptor {
            public byte[] encryptMessage(byte[] message, byte[] keyBytes)
            throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                //...
            }
        }
        ```

        这里的转换字符串 `"AES/ECB/PKCS5Padding"` 告诉 `getInstance` 方法：创建一个使用 **[AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) 算法**、**[ECB](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation) 模式** 和 **PKCS5 [填充方案](https://en.wikipedia.org/wiki/Padding_(cryptography))** 的 `Cipher` 实例。

        也可以仅指定算法名称：

        ```java
        Cipher cipher = Cipher.getInstance("AES");
        ```

        此时，Java 会使用当前安全提供者默认的模式和填充方式。

        注意：
        - 若转换名称为 `null`、空字符串、格式无效，或提供者不支持该转换，`getInstance` 会抛出 `NoSuchAlgorithmException`。
        - 若指定了不支持的填充方案，则抛出 `NoSuchPaddingException`。

    2. 线程安全性

        `Cipher` 类是有状态的，且**内部没有同步机制**。调用 [`init()`](https://github.com/openjdk/jdk/blob/1aa653957619acfdb5f08ce0f3a1ad1a17cfa127/src/java.base/share/classes/javax/crypto/Cipher.java#L1235) 或 [`update()`](https://github.com/openjdk/jdk/blob/1aa653957619acfdb5f08ce0f3a1ad1a17cfa127/src/java.base/share/classes/javax/crypto/Cipher.java#L1820) 等方法会改变其内部状态。

        因此，**`Cipher` 类不是线程安全的**。建议为每次加密或解密操作创建独立的 `Cipher` 实例。

    3. 密钥（Keys）

        [Key](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/Key.html) 接口表示用于加密操作的密钥。密钥是不透明的容器，包含编码后的密钥数据、编码格式及其对应的加密算法。

        密钥通常通过以下方式获得：

        - 密钥生成器（[KeyGenerator](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/KeyGenerator.html)）
        - 证书（Certificate）
        - 使用密钥工厂（[KeyFactory](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/KeyFactory.html)）从密钥规范（[KeySpec](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/spec/KeySpec.html)）构建

        例如，从字节数组创建对称密钥：

        ```java
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        ```

    4. Cipher 初始化

        调用 `init()` 方法可使用 `Key`（或 [`Certificate`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/cert/Certificate.html)）和操作模式（opmode）初始化 `Cipher` 对象。

        可选参数包括：

        - 随机数源（默认使用最高优先级提供者的 [`SecureRandom`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/SecureRandom.html)，或系统默认源）
        - 算法特定参数（如 [`IvParameterSpec`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/spec/IvParameterSpec.html) 用于指定初始化向量 [IV](https://en.wikipedia.org/wiki/Initialization_vector)）

        支持的操作模式包括：

        - `ENCRYPT_MODE`：加密模式
        - `DECRYPT_MODE`：解密模式
        - `WRAP_MODE`：密钥封装模式(initialize cipher object to [key-wrapping](https://en.wikipedia.org/wiki/Key_Wrap) mode)
        - `UNWRAP_MODE`：密钥解封装模式(initialize cipher object to [key-unwrapping](https://en.wikipedia.org/wiki/Key_Wrap) mode)

        初始化示例：

        ```java
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        ```

        `init()` 方法在以下情况会抛出 `InvalidKeyException`：

        - 密钥长度或编码格式无效
        - 密钥无法提供算法所需的必要参数（如 IV）
        - 密钥长度超过 JCE 管辖策略文件（[jurisdiction policy files](https://docs.oracle.com/javase/9/security/java-cryptography-architecture-jca-reference-guide.htm#JSSEC-GUID-EFA5AC2D-644E-4CD9-8523-C6D3936D5FB1)）允许的最大值

        使用证书的示例：

        ```java
        public byte[] encryptMessage(byte[] message, Certificate certificate)
        throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, certificate);
            // ...
        }
        ```

        此时，`Cipher` 会自动从证书中调用 `getPublicKey()` 获取公钥用于加密。

    5. 加密与解密

        初始化完成后，调用 `doFinal()` 方法执行加密或解密操作，返回包含结果的字节数组。

        > **重要**：`doFinal()` 会将 `Cipher` 对象重置为上次 `init()` 后的初始状态，使其可重复用于下一次加解密。

        加密方法示例：

        ```java
        public byte[] encryptMessage(byte[] message, byte[] keyBytes)
        throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
            BadPaddingException, IllegalBlockSizeException {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(message);
        }
        ```

        解密只需将模式改为 `DECRYPT_MODE`：

        ```java
        public byte[] decryptMessage(byte[] encryptedMessage, byte[] keyBytes)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedMessage);
        }
        ```

    6. 安全提供者（Providers）

        JCE 采用[**提供者架构**](https://en.wikipedia.org/wiki/Provider_model)，允许像 [**Bouncy Castle**](https://www.bouncycastle.org/) 这样的第三方加密库作为安全提供者无缝集成，并支持新增算法。

        **静态添加 Bouncy Castle 提供者**：
        编辑 `<JAVA_HOME>/jre/lib/security/java.security` 文件，在末尾添加：

        ```security
        security.provider.7=org.bouncycastle.jce.provider.BouncyCastleProvider
        ```

        > 注意：编号 `N` 应比列表中最后一个提供者编号大 1。

        **动态添加（推荐）**：

        ```java
        Security.addProvider(new BouncyCastleProvider());
        ```

        指定提供者创建 `Cipher`：

        ```java
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");
        ```

        其中 `"BC"` 表示使用 Bouncy Castle 提供者。可通过 `Security.getProviders()` 获取已注册的提供者列表。

3. 加解密测试示例

    下面是一个完整的测试用例，演示 AES 加解密过程：

    ```java
    @Test
    public void whenIsEncryptedAndDecrypted_thenDecryptedEqualsOriginal()
    throws Exception {

        String encryptionKeyString = "thisisa128bitkey"; // 16 字节 = 128 位
        String originalMessage = "This is a secret message";
        byte[] keyBytes = encryptionKeyString.getBytes(StandardCharsets.UTF_8);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // 加密
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(originalMessage.getBytes(StandardCharsets.UTF_8));

        // 解密
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(encrypted);

        assertThat(originalMessage).isEqualTo(new String(decrypted, StandardCharsets.UTF_8));
    }
    ```

    > ⚠️ 注意：示例中使用了 **ECB 模式**，仅用于演示。**ECB 不安全**，实际应用中应使用 **CBC、GCM 等带 IV 的模式**。

4. 结论

    本文详细介绍了 Java 中的 `Cipher` 类，并提供了使用示例。更多细节可参考官方 `Cipher` 类文档及[《Java 加密架构（JCA）参考指南》](https://docs.oracle.com/javase/9/security/java-cryptography-architecture-jca-reference-guide.htm)。
