# [Java BouncyCastle简介](https://www.baeldung.com/java-bouncy-castle)

1. 概述

    [BouncyCastle](https://www.bouncycastle.org/)是一个Java库，它补充了默认的Java加密扩展（JCE）。

    在这篇介绍性文章中，我们将展示如何使用BouncyCastle执行加密操作，例如加密和签名。

2. Maven配置

    在我们开始使用该库之前，我们需要将所需的依赖项添加到我们的 `pom.xml` 文件中：

    ```xml
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcpkix-jdk18on</artifactId>
        <version>1.76</version>
    </dependency>
    ```

    请注意，我们始终可以在 [Maven Central Repository](https://search.maven.org/) 中查找最新的依赖版本。

3. 设置无限制强度管辖策略文件

    标准的 Java 安装在加密功能方面是有限制的，这是因为政策法规禁止使用超过一定长度的密钥，例如 AES 的最大密钥长度为 128。

    要克服这个限制，我们需要配置无限制强度管辖策略文件。

    为此，我们首先需要通过[此链接](https://www.oracle.com/java/technologies/javase-jce8-downloads.html)下载包。然后，我们需要将压缩文件解压到我们选择的目录中——该目录包含两个 jar 文件：

    - `local_policy.jar`
    - `US_export_policy.jar`

    最后，我们需要找到 `{JAVA_HOME}/lib/security` 文件夹，并用我们提取出的策略文件替换现有的文件。

    注意：在 Java 9 及以上版本中，不再需要下载策略文件包，只需将 `crypto.policy` 属性设置为 unlimited 即可：

    ```java
    Security.setProperty("crypto.policy", "unlimited");
    ```

    完成之后，我们需要检查配置是否正常工作：

    ```java
    int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
    System.out.println("Max Key Size for AES : " + maxKeySize);
    ```

    输出结果应为：

    ```log
    Max Key Size for AES : 2147483647
    ```

    根据 `getMaxAllowedKeyLength()` 方法返回的最大密钥长度，我们可以确定无限制强度策略文件已正确安装。

    如果返回值等于 128，则需要确保我们将这些文件安装到了运行代码所使用的 JVM 中。

4. 加密操作

    1. 准备证书和私钥

        在我们深入实现加密功能之前，我们首先需要创建一个证书和一个私钥。

        为了测试目的，我们可以使用以下资源：

        - [`Baeldung.cer`](https://github.com/eugenp/tutorials/tree/master/libraries/src/main/resources)
        - [`Baeldung.p12`（密码为 “password”）](https://github.com/eugenp/tutorials/tree/master/libraries/src/main/resources)

        其中，`Baeldung.cer` 是一个使用国际 X.509 公钥基础设施标准的数字证书，而 `Baeldung.p12` 是一个受密码保护的 [PKCS12](https://tools.ietf.org/html/rfc7292) 密钥库，包含私钥。

        下面是如何在 Java 中加载它们：

        ```java
        Security.addProvider(new BouncyCastleProvider());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(
                new FileInputStream("Baeldung.cer"));

        char[] keystorePassword = "password".toCharArray();
        char[] keyPassword = "password".toCharArray();

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream("Baeldung.p12"), keystorePassword);

        PrivateKey key = (PrivateKey) keystore.getKey("baeldung", keyPassword);
        ```

        首先，我们使用 `addProvider()` 方法动态地将 `BouncyCastleProvider` 添加为安全提供程序。

        也可以通过静态方式完成：编辑 `{JAVA_HOME}/jre/lib/security/java.security` 文件，并添加以下一行：

        ```txt
        security.provider.N=org.bouncycastle.jce.provider.BouncyCastleProvider
        ```

        一旦提供者正确安装，我们就可以使用 `CertificateFactory` 类的 `getInstance()` 方法创建一个 `CertificateFactory` 对象。

        `getInstance()` 方法接受两个参数；证书类型 `"X.509"` 和安全提供者 `"BC"`。

        随后，`certFactory` 实例通过 `generateCertificate()` 方法生成一个 `X509Certificate` 对象。

        同样地，我们创建了一个 `PKCS12 Keystore` 对象，并调用了 `load()` 方法。

        `getKey()` 方法返回与给定别名关联的私钥。

        请注意，一个 PKCS12 密钥库包含一组私钥，每个私钥可以有特定的密码，这就是为什么我们需要一个全局密码来打开密钥库，以及一个特定密码来检索私钥的原因。

        证书和私钥对主要用于非对称加密操作：

        - 加密
        - 解密
        - 签名
        - 验证

    2. 生成证书和密钥库

        如果我们想生成另一个证书，就需要生成一个新的密钥库，并将其与该证书关联。

        要生成证书，我们需要运行以下命令：

        1. 生成一个长度为 2048 的私钥：

            ```bash
            openssl genrsa -out private-key.pem 2048
            ```

        2. 使用该私钥生成一个证书签名请求：

            ```bash
            openssl req -new -sha256 -key private-key.pem -out certificate-signed-request.csr
            ```

        3. 生成一个适用于 Web 服务器的自签名证书：

            ```bash
            openssl req -x509 -sha256 -days 365 -key private-key.pem -in certificate-signed-request.csr -out Baeldung.cer
            ```

        4. 生成 PKCS12 格式的密钥库：

            ```bash
            openssl pkcs12 -export -name baeldung -out Baeldung.p12 -inkey private-key.pem -in Baeldung.cer
            ```

        成功生成证书后，请将其添加到资源文件夹中。请确保代码中引用了正确的证书和密钥库名称。

    3. CMS/PKCS7 加密与解密

        在非对称加密中，每次通信都需要一个公钥证书和一个私钥。

        接收方绑定到一个在所有发送方之间公开共享的证书。

        简单来说，发送方需要接收方的证书来加密消息，而接收方则需要对应的私钥来解密消息。

        让我们看看如何使用加密证书实现 `encryptData()` 函数：

        ```java
        public static byte[] encryptData(byte[] data, X509Certificate encryptionCertificate)
                throws CertificateEncodingException, CMSException, IOException {
            byte[] encryptedData = null;
            if (null != data && null != encryptionCertificate) {
                CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
                JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(encryptionCertificate);
                cmsEnvelopedDataGenerator.addRecipientInfoGenerator(transKeyGen);
                CMSTypedData msg = new CMSProcessableByteArray(data);
                OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                        .setProvider("BC").build();
                CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(msg, encryptor);
                encryptedData = cmsEnvelopedData.getEncoded();
            }
            return encryptedData;
        }
        ```

        我们使用接收方的证书创建了一个 `JceKeyTransRecipientInfoGenerator` 对象。

        然后，我们创建了一个新的 `CMSEnvelopedDataGenerator` 对象，并将接收方信息生成器添加进去。

        之后，我们使用 `JceCMSContentEncryptorBuilder` 类创建了一个 `OutputEncryptor` 对象，使用的是 AES CBC 算法。

        加密器稍后用于生成一个封装加密消息的 `CMSEnvelopedData` 对象。

        最后，返回信封的编码表示形式作为字节数组。

        现在，我们来看看 `decryptData()` 方法的实现：

        ```java
        public static byte[] decryptData(byte[] encryptedData, PrivateKey decryptionKey) throws CMSException {
            byte[] decryptedData = null;
            if (null != encryptedData && null != decryptionKey) {
                CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);
                Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients();
                KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recipients.iterator().next();
                JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(decryptionKey);
                return recipientInfo.getContent(recipient);
            }
            return decryptedData;
        }
        ```

        首先，我们使用加密数据字节数组初始化了一个 `CMSEnvelopedData` 对象，然后我们使用 `getRecipients()` 方法检索消息的所有预期接收者。

        在这个例子中，我们只验证了一个接收者，但在通用情况下，必须遍历 `getSigners()` 返回的接收者集合并分别检查每一个。

        最后，我们创建了一个与接收方私钥相关联的 `JceKeyTransRecipient` 对象。

        `recipientInfo` 实例包含了解密后的消息，但除非我们拥有对应的接收方密钥，否则无法检索它。

        最终，给定接收方密钥作为参数，`getContent()` 方法返回从该接收方关联的 `EnvelopedData` 中提取的原始字节数组。

        下面我们写一个简单的测试以确保一切按预期工作：

        ```java
        String secretMessage = "My password is 123456Seven";
        System.out.println("Original Message : " + secretMessage);
        byte[] stringToEncrypt = secretMessage.getBytes();
        byte[] encryptedData = encryptData(stringToEncrypt, certificate);
        System.out.println("Encrypted Message : " + new String(encryptedData));
        byte[] rawData = decryptData(encryptedData, privateKey);
        String decryptedMessage = new String(rawData);
        System.out.println("Decrypted Message : " + decryptedMessage);
        ```

        输出结果如下：

        ```log
        Original Message : My password is 123456Seven
        Encrypted Message : 0*H...
        Decrypted Message : My password is 123456Seven
        ```

    4. CMS/PKCS7 签名与验证

        签名和验证是验证数据真实性的加密操作。

        让我们看看如何使用数字证书对秘密消息进行签名：

        ```java
        public static byte[] signData(byte[] data, X509Certificate signingCertificate, PrivateKey signingKey) throws Exception {
            byte[] signedMessage = null;
            List<X509Certificate> certList = new ArrayList<X509Certificate>();
            CMSTypedData cmsData = new CMSProcessableByteArray(data);
            certList.add(signingCertificate);
            Store certs = new JcaCertStore(certList);
            CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(signingKey);
            cmsGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC")
                            .build()).build(contentSigner, signingCertificate));
            cmsGenerator.addCertificates(certs);
            CMSSignedData cms = cmsGenerator.generate(cmsData, true);
            signedMessage = cms.getEncoded();
            return signedMessage;
        }
        ```

        首先，我们将输入嵌入到 `CMSTypedData` 中，然后创建了一个新的 `CMSSignedDataGenerator` 对象。

        我们使用 SHA256withRSA 作为签名算法，并使用我们的签名密钥创建了一个 `ContentSigner` 对象。

        之后，使用 `contentSigner` 和签名证书创建了一个 `SigningInfoGenerator` 对象。

        在将 `SignerInfoGenerator` 和签名证书添加到 `CMSSignedDataGenerator` 实例之后，我们最终使用 `generate()` 方法创建了一个 CMS 签名数据对象，该对象也携带了 CMS 签名。

        现在我们已经看到了如何对数据进行签名，接下来我们看看如何验证已签名的数据：

        ```java
        public static boolean verifSignedData(byte[] signedData) throws Exception {
            X509Certificate signCert = null;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
            ASN1InputStream asnInputStream = new ASN1InputStream(inputStream);
            CMSSignedData cmsSignedData = new CMSSignedData(
                    ContentInfo.getInstance(asnInputStream.readObject()));
            SignerInformationStore signers = cmsSignedData.getCertificates().getSignerInfos();
            SignerInformation signer = signers.getSigners().iterator().next();
            Collection<X509CertificateHolder> certCollection = certs.getMatches(signer.getSID());
            X509CertificateHolder certHolder = certCollection.iterator().next();
            return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certHolder));
        }
        ```

        同样，我们基于签名数据字节数组创建了一个 `CMSSignedData` 对象，然后我们使用 `getSignerInfos()` 方法检索与签名相关联的所有签名者。

        在这个示例中，我们只验证了一个签名者，但在通用情况下，必须遍历 `getSigners()` 返回的签名者集合并分别检查每一个。

        最后，我们使用 `build()` 方法创建了一个 `SignerInformationVerifier` 对象，并将其传递给 `verify()` 方法。

        如果给定的对象能够成功验证签名者的签名，则 `verify()` 方法返回 `true`。

        这里是一个简单的示例：

        ```java
        byte[] signedData = signData(rawData, certificate, privateKey);
        Boolean check = verifSignData(signedData);
        System.out.println(check);
        ```

        输出结果：

        ```log
        true
        ```

5. 结论

    在本文中，我们学习了如何使用 BouncyCastle 库执行基本的加密操作，如加密和签名。

    在现实世界中，我们通常希望先签名再加密我们的数据，这样只有接收方才能使用私钥解密它，并根据数字签名验证其真实性。
