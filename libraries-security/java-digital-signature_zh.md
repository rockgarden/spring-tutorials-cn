# [Java中的数字签名](https://www.baeldung.com/java-digital-signature)

1. 概述

    在本教程中，我们将学习数字签名机制，以及如何使用Java加密体系结构（[JCA](https://docs.oracle.com/en/java/javase/11/security/java-cryptography-architecture-jca-reference-guide.html)）实现它。我们将研究KeyPair、MessageDigest、Cipher、KeyStore、Certificate和Signature JCA API。

    我们将首先了解什么是数字签名，如何生成密钥对，以及如何从证书颁发机构（CA）认证公钥。之后，我们将了解如何使用低级和高级JCA API实现数字签名。

2. 什么是数字签名？

    1. 数字签名定义

        数字签名是一种确保：

        - 完整性：消息在传输过程中未被更改
        - 真实性：信息的作者确实是他们声称的人
        - 不可抵赖性：消息的作者以后不能否认他们是消息的来源

    2. 发送带有数字签名的消息

        从技术上讲，数字签名是消息的加密哈希（摘要、校验和）。这意味着我们从消息生成一个散列，并根据所选算法用私钥对其进行加密。

        然后发送消息、加密的哈希、相应的公钥和算法。这被归类为带有数字签名的消息。

    3. 接收和检查数字签名

        为了检查数字签名，消息接收方从接收到的消息生成一个新的哈希，使用公钥解密接收到的加密哈希，并对其进行比较。如果匹配，则称数字签名已验证。

        我们应该注意，我们只加密消息散列，而不是消息本身。换句话说，数字签名并不试图对消息保密。我们的数字签名只能证明消息在传输过程中没有被更改。

        当签名被验证时，我们确信只有私钥的所有者才能是消息的作者。

3. 数字证书和公钥身份

    证书是将身份与给定公钥相关联的文档。证书由称为证书颁发机构（CA）的第三方实体签署。

    我们知道，如果我们用发布的公钥解密的哈希与实际的哈希匹配，那么消息就会被签名。然而，我们如何知道公钥真正来自正确的实体？这可以通过使用数字证书来解决。

    数字证书包含公钥，并且本身由另一实体签名。该实体的签名本身可以由另一个实体进行验证，依此类推。我们最终得到了我们所称的证书链。每个顶级实体验证下一个实体的公钥。最顶层的实体是自签名的，这意味着他的公钥是由他自己的私钥签名的。

    X.509是最常用的证书格式，它以二进制格式（DER）或文本格式（PEM）提供。JCA已经通过X509Certificate类为此提供了一个实现。

4. 密钥对管理

    由于数字签名使用私钥和公钥，因此我们将分别使用JCA类PrivateKey和PublicKey对消息进行签名和检查。

    1. 获取密钥对

        要创建私钥和公钥的密钥对，我们将使用Java[密钥工具](https://www.baeldung.com/keytool-intro)。

        让我们使用genkeypair命令生成密钥对：

        ```bash
        keytool -genkeypair -alias senderKeyPair -keyalg RSA -keysize 2048 \
        -dname "CN=Baeldung" -validity 365 -storetype PKCS12 \
        -keystore sender_keystore.p12 -storepass changeit
        ```

        这将为我们创建一个私钥及其对应的公钥。公钥被包装到一个X.509自签名证书中，该证书又被包装成一个单元素证书链。我们将证书链和私钥存储在密钥库文件sender_Keystore中。p12，我们可以使用KeyStore API处理它。

        这里，我们使用了PKCS12密钥存储格式，因为它是Java专有JKS格式的标准和推荐格式。此外，我们应该记住密码和别名，因为我们将在下一小节加载Keystore文件时使用它们。

    2. 加载用于签名的私钥

        为了签署消息，我们需要PrivateKey的实例。

        使用KeyStore API和以前的KeyStore文件sender_KeyStore。p12，我们可以获取PrivateKey对象：

        ```java
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("sender_keystore.p12"), "changeit");
        PrivateKey privateKey = 
        (PrivateKey) keyStore.getKey("senderKeyPair", "changeit");
        ```

    3. 公开密钥

        在发布公钥之前，我们必须首先决定是使用自签名证书还是CA签名证书。

        当使用自签名证书时，我们只需要从密钥库文件导出它。我们可以使用exportcert命令执行此操作：

        ```bash
        keytool -exportcert -alias senderKeyPair -storetype PKCS12 \
        -keystore sender_keystore.p12 -file \
        sender_certificate.cer -rfc -storepass changeit
        ```

        否则，如果要使用CA签名的证书，则需要创建证书签名请求（CSR）。我们使用certreq命令执行此操作：

        ```bash
        keytool -certreq -alias senderKeyPair -storetype PKCS12 \
        -keystore sender_keystore.p12 -file -rfc \
        -storepass changeit > sender_certificate.csr
        ```

        CSR文件sender_certificate。然后将csr发送给证书颁发机构进行签名。完成后，我们将收到一个封装在X.509证书中的签名公钥，可以是二进制（DER）格式，也可以是文本（PEM）格式。这里，我们将rfc选项用于PEM格式。

        我们从CA sender_certificate收到的公钥。cer，现在已由CA签署，可供客户使用。

    4. 加载公钥进行验证

        接收方可以访问公钥，可以使用importcert命令将其加载到密钥库中：

        ```bash
        keytool -importcert -alias receiverKeyPair -storetype PKCS12 \
        -keystore receiver_keystore.p12 -file \
        sender_certificate.cer -rfc -storepass changeit
        ```

        和以前一样，使用KeyStore API，我们可以获得PublicKey实例：

        ```java
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("receiver_keytore.p12"), "changeit");
        Certificate certificate = keyStore.getCertificate("receiverKeyPair");
        PublicKey publicKey = certificate.getPublicKey();
        ```

        现在，我们在发送方有了一个PrivateKey实例，在接收方有了PublicKey的实例，我们可以开始签名和验证过程了。

        **ERROR**: `Exception in thread "main" java.io.FileNotFoundException: sender_keystore.p12 (No such file or directory)`

        - 解决：必须在 FileInputStream() 中给出文件访问路径 /path/receiver_keytore.p12。

5. 具有MessageDigest和Cipher类的数字签名

    正如我们所看到的，数字签名是基于哈希和加密的。

    通常，我们使用带有[SHA](https://www.baeldung.com/sha-256-hashing-java)或[MD5](https://www.baeldung.com/java-md5)的MessageDigest类进行散列，使用Cipher类进行加密。

    现在，让我们开始实现数字签名机制。

    1. 生成消息哈希

        消息可以是字符串、文件或任何其他数据。让我们来看一个简单文件的内容：

        `byte[] messageBytes = Files.readAllBytes(Paths.get("message.txt"));`

        现在，使用MessageDigest，让我们使用digest方法生成哈希：

        ```java
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = md.digest(messageBytes);
        ```

        这里，我们使用了SHA-256算法，这是最常用的算法。其他替代方案为MD5、SHA-384和SHA-512。

    2. 加密生成的哈希

        要加密消息，我们需要算法和私钥。这里我们将使用RSA算法。DSA算法是另一个选项。

        让我们创建一个密码实例并对其进行初始化以进行加密。然后，我们将调用doFinal（）方法来加密先前的哈希消息：

        ```java
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] digitalSignature = cipher.doFinal(messageHash);
        ```

        签名可以保存到文件中，以便以后发送：

        `Files.write(Paths.get("digital_signature_1"), digitalSignature);`

        此时，消息、数字签名、公钥和算法都被发送，接收方可以使用这些信息来验证消息的完整性。

        **ERROR**: `java.security.InvalidKeyException: OAEP cannot be used to sign or verify signatures`

        - 解决：当 Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING") 时，标准 JCE 的 init 方法不支持 (throws this error if the mode is DECRYPT_MODE, the key is an RSAPublicKey and the padding type is not PAD_NONE or PAD_PKCS1)，可通过加载三方库provider解决，如 `Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())`，或者升级 JDK > 8 ？。

    3. 验证签名

        当我们收到消息时，我们必须验证其签名。为此，我们解密接收到的加密哈希，并将其与我们对接收到的消息所做的哈希进行比较。

        让我们看看收到的数字签名：

        `byte[] encryptedMessageHash = Files.readAllBytes(Paths.get("digital_signature_1"));`

        为了解密，我们创建了一个密码实例。然后我们调用doFinal方法：

        ```java
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedMessageHash = cipher.doFinal(encryptedMessageHash);
        ```

        接下来，我们从收到的消息生成一个新的消息哈希：

        ```java
        byte[] messageBytes = Files.readAllBytes(Paths.get("message.txt"));

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] newMessageHash = md.digest(messageBytes);
        ```

        最后，我们检查新生成的消息哈希是否与解密消息哈希匹配：

        `boolean isCorrect = Arrays.equals(decryptedMessageHash, newMessageHash);`

        在这个例子中，我们使用了文本文件消息。txt来模拟我们想要发送的消息，或者我们收到的消息正文的位置。通常，我们希望在签名的旁边收到我们的消息。

6. 使用签名类的数字签名

    到目前为止，我们已经使用低级API构建了自己的数字签名验证过程。这有助于我们了解它的工作原理，并允许我们对其进行定制。

    然而，JCA已经以Signature类的形式提供了一个专用API。

    1. 签署消息

        为了开始签名过程，我们首先创建Signature类的实例。为此，我们需要一个签名算法。然后使用私钥初始化签名：

        ```java
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        ```

        我们选择的签名算法，本例中的SHA256withRSA，是哈希算法和加密算法的组合。其他替代方案包括SHA1 withRSA、SHA1 withDSA和MD5 withRSA等。

        接下来，我们继续对消息的字节数组进行签名：

        ```java
        byte[] messageBytes = Files.readAllBytes(Paths.get("message.txt"));
        signature.update(messageBytes);
        byte[] digitalSignature = signature.sign();
        ```

        我们可以将签名保存到文件中，以便以后传输：

        `Files.write(Paths.get("digital_signature_2"), digitalSignature);`

    2. 验证签名

        为了验证收到的签名，我们再次创建签名实例：

        `Signature signature = Signature.getInstance("SHA256withRSA");`

        接下来，我们通过调用initVerify方法初始化Signature对象以进行验证，该方法采用公钥：

        signature.initVerify(publicKey);

        然后，我们需要通过调用update方法将接收到的消息字节添加到签名对象：

        ```java
        byte[] messageBytes = Files.readAllBytes(Paths.get("message.txt"));
        signature.update(messageBytes);
        ```

        最后，我们可以通过调用verify方法来检查签名：

        `boolean isCorrect = signature.verify(receivedSignature);`

7. 结论

    在本文中，我们首先研究了数字签名的工作原理以及如何为数字证书建立信任。然后，我们使用Java Cryptography Architecture中的MessageDigest、Cipher和signature类实现了数字签名。

    我们详细了解了如何使用私钥对数据进行签名，以及如何使用公钥验证签名。
