# [Jaspypt简介](https://www.baeldung.com/jasypt)

1. 概述

    在本文中，我们将研究 [Jaspypt](http://www.jasypt.org/index.html) （Java Simplified Encryption）库。

    Jasypt是一个Java库，它允许开发人员以最少的工作量将基本加密功能添加到项目中，而无需深入了解加密协议的实现细节。

2. 使用简单加密

    假设我们正在构建一个web应用程序，其中用户提交一个帐户私有数据。我们需要将数据存储在数据库中，但存储纯文本是不安全的。

    处理此问题的一种方法是将加密数据存储在数据库中，并在为特定用户检索该数据时对其进行解密。

    要使用非常简单的算法执行加密和解密，我们可以使用Jasypt库中的 [BasicTextEncryptor](http://www.jasypt.org/api/jasypt/1.8/org/jasypt/util/text/BasicTextEncryptor.html) 类：

    ```java
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    String privateData = "secret-data";
    textEncryptor.setPasswordCharArray("some-random-data".toCharArray());
    ```

    然后我们可以使用 encrypt() 方法加密纯文本：

    ```java
    String myEncryptedText = textEncryptor.encrypt(privateData);
    assertNotSame(privateData, myEncryptedText);
    ```

    如果我们想在数据库中存储给定用户的私有数据，我们可以在不违反任何安全限制的情况下存储myEncryptedText。如果要将数据解密回纯文本，可以使用decrypt() 方法：

    ```java
    String plainText = textEncryptor.decrypt(myEncryptedText);
    assertEquals(plainText, privateData);
    ```

    我们看到，解密的数据等于之前加密的纯文本数据。

3. 单向加密

    前面的示例并不是执行身份验证的理想方法，即当我们要存储用户密码时。理想情况下，我们希望加密密码，而无需解密。当用户尝试登录我们的服务时，我们会加密他的密码，并将其与存储在数据库中的加密密码进行比较。这样我们就不需要对纯文本密码进行操作。

    我们可以使用BasicPasswordEncryptor类执行单向加密：

    ```java
    String password = "secret-pass";
    BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
    String encryptedPassword = passwordEncryptor.encryptPassword(password);
    ```

    然后，我们可以将已经加密的密码与执行登录过程的用户的密码进行比较，而无需解密已经存储在数据库中的密码：

    ```java
    boolean result = passwordEncryptor.checkPassword("secret-pass", encryptedPassword);
    assertTrue(result);
    ```

4. 配置加密算法

    我们可以使用更强的加密算法，但我们需要为JVM安装 JCE ，才可运行 JasyptUnitTest.java 中的 givenTextPrivateData_whenDecrypt_thenCompareToEncryptedWithCustomAlgorithm 方法。

    在Jasypt中，我们可以使用StandardPBEStringEncryptor类使用强加密，并使用setAlgorithm（）方法对其进行自定义：

    ```java
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    String privateData = "secret-data";
    encryptor.setPassword("some-random-passwprd");
    encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
    ```

    让我们将加密算法设置为PBEWithMD5AndTripleDES。

    接下来，加密和解密过程看起来与上一个使用BasicTextEncryptor类的过程相同：

    ```java
    String encryptedText = encryptor.encrypt(privateData);
    assertNotSame(privateData, encryptedText);

    String plainText = encryptor.decrypt(encryptedText);
    assertEquals(plainText, privateData);
    ```

5. 使用多线程解密

    当我们在多核机器上操作时，我们希望并行处理解密处理。为了获得良好的性能，我们可以使用 [PooledPBEStringEncryptor](http://www.jasypt.org/api/jasypt/1.9.3/org/jasypt/encryption/pbe/PooledPBEStringEncryptor.html) 和 setPoolSize() API创建一个消化池。每个线程都可以由不同的线程并行使用：

    ```java
    PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    encryptor.setPoolSize(4);
    encryptor.setPassword("some-random-data");
    encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
    ```

    将池大小设置为等于机器的核心数是一个好做法。加密和解密的代码与以前的相同。

6. 在其他框架中的使用

    最后一点，Jasypt库可以与许多其他库集成，当然包括Spring Framework。

    我们只需要创建一个配置，将加密支持添加到Spring应用程序中。如果我们想将敏感数据存储到数据库中，并且我们使用Hibernate作为数据访问框架，我们还可以将Jaspyt与之集成。

    有关这些集成以及与其他一些框架的集成的说明，可以在[Jasypt主页](http://www.jasypt.org/)上的指南部分找到。
