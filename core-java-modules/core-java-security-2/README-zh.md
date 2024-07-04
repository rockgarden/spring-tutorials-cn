# 核心Java安全

本模块包含有关 Java 核心安全的文章

- [Java 验证与授权服务 (JAAS) 指南](https://www.baeldung.com/java-authentication-authorization-service)
- [Java 中的 MD5 加密](http://www.baeldung.com/java-md5)
- [x] [Java中的密码散列](#用java对密码进行散列)
- [Java 中的 SHA-256 和 SHA3-256 散列](https://www.baeldung.com/sha-256-hashing-java)
- [Java 中的校验和](https://www.baeldung.com/java-checksums)
- [如何读取 PEM 文件以获取公钥和私钥](https://www.baeldung.com/java-read-pem-file-keys)
- [在 Java 中获取可信证书列表](https://www.baeldung.com/java-list-trusted-certificates)
- [安全上下文基础知识：用户、主体和负责人](https://www.baeldung.com/security-context-basics)
- [java.security.egd JVM 选项](https://www.baeldung.com/java-security-egd)
- 更多文章： [[<-- prev]](../core-java-security/README-zh.md) [[next -->]](../core-java-security-3/README-zh.md)

## 用Java对密码进行散列

1. 概述

    在本教程中，我们将讨论密码散列的重要性。

    我们将快速了解它是什么、为什么重要，以及在 Java 中进行散列的一些安全和不安全的方法。

2. 什么是散列？

    散列是使用称为加密散列函数的数学函数从给定消息生成字符串或散列值的过程。

    虽然有多种散列函数，但专为密码散列而设计的散列函数需要具备四个主要特性才能保证安全：

    - 它应该是确定性的：用相同的散列函数处理相同的信息，应该总是产生相同的散列结果
    - 不可逆：从哈希值生成信息是不切实际的
    - 它具有高熵特性：对信息的微小改动都会产生截然不同的哈希值
    - 抗碰撞：两个不同的信息不会产生相同的哈希值

    具备这四种特性的散列函数是密码散列的理想选择，因为这四种特性大大增加了从散列函数逆向生成密码的难度。

    此外，密码散列函数的速度应该较慢。在这种攻击中，黑客会试图通过每秒散列和比较数十亿（[或数万亿](https://www.wired.com/2014/10/snowdens-first-emails-to-poitras/)）个潜在密码来猜测密码。

    符合所有这些标准的散列函数有 PBKDF2、BCrypt 和 SCrypt。但首先，让我们来看看一些较老的算法，以及为什么不再推荐使用这些算法。

3. 不推荐： MD5

    我们的第一个哈希函数是 MD5 消息加密算法，该算法开发于 1992 年。

    Java 的 MessageDigest 使其计算简单，在其他情况下仍然有用。

    然而，在过去几年中，MD5 被发现[不具备第四个密码散列属性](https://blog.avira.com/md5-the-broken-algorithm/)，因为它在计算上很容易产生碰撞。更重要的是，MD5 是一种快速算法，因此对暴力攻击毫无用处。

    因此，不推荐使用 MD5。

4. 不推荐： SHA-512

    接下来，我们来看看 SHA-512，它是安全散列算法系列的一部分，该系列始于 1993 年的 SHA-0。

    1. 为什么选择 SHA-512？

        随着计算机功能的增强，以及我们发现新的漏洞，研究人员衍生出新版本的 SHA。新版本的长度逐渐加长，有时研究人员会发布新版本的底层算法。

        SHA-512 代表了第三代算法中最长的密钥。

        虽然现在有更安全的 SHA 版本，[但 SHA-512 是在 Java 中实现的最强版本](https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html)。

    2. 在 Java 中实现

        现在，让我们看看如何在 Java 中实现 SHA-512 哈希算法。

        首先，我们必须了解盐的概念。简单地说，这是为每个新散列生成的随机序列。

        通过引入这种随机性，我们可以增加哈希值的熵，并保护我们的数据库免受预先编译的哈希值列表（即彩虹表）的影响。

        新的哈希函数大致如下

        ```txt
        salt <- generate-salt;
        hash <- salt + ':' + sha512(salt + password)
        ```

    3. 生成盐

        为了引入盐，我们将使用 java.security 中的 [SecureRandom](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/security/SecureRandom.html) 类：

        ```java
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        ```

        然后，我们将使用 [MessageDigest](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/security/MessageDigest.html) 类使用盐配置 SHA-512 哈希函数：

        ```java
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        ```

        这样，我们就可以使用摘要方法生成散列密码了：

        `byte[] hashedPassword = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));`

    4. 为什么不推荐使用？

        在使用盐的情况下，SHA-512仍然是一个[不错的选择](https://en.wikipedia.org/wiki/Secure_Hash_Algorithms)，但还有更强更慢的选择。

        此外，我们将介绍的其余选项都有一个重要特性：可配置强度。

5. PBKDF2、BCrypt 和 SCrypt

    PBKDF2、BCrypt 和 SCrypt 是三种推荐算法。

    1. 为什么推荐这些算法？

        每种算法的速度都很慢，而且每种算法都具有可配置强度的出色特性。

        这意味着随着计算机强度的增加，我们可以通过改变输入来降低算法的速度。

    2. 用 Java 实现 PBKDF2

        现在，盐是密码散列的基本原理，因此我们也需要一个用于 PBKDF2 的盐。

        接下来，我们将创建一个 [PBEKeySpec](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/javax/crypto/spec/PBEKeySpec.html) 和一个 [SecretKeyFactory](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/javax/crypto/SecretKeyFactory.html)，并使用 PBKDF2WithHmacSHA1 算法对其进行实例化：

        ```java
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        ```

        第三个参数（65536）实际上是强度参数。它表示该算法的迭代次数，增加了生成哈希值所需的时间。

        最后，我们可以使用 SecretKeyFactory 生成哈希值：

        `byte[] hash = factory.generateSecret(spec).getEncoded();`

    3. 在 Java 中实现 BCrypt 和 SCrypt

        事实证明，虽然某些 Java 库支持 BCrypt 和 SCrypt，但 Java 还不支持 BCrypt 和 SCrypt。

        Spring Security 就是其中之一。

6. 使用 Spring Security 进行密码加密

    虽然 Java 本身支持 PBKDF2 和 SHA 哈希算法，但它不支持 BCrypt 和 SCrypt 算法。

    幸运的是，Spring Security 通过 PasswordEncoder 接口支持所有这些推荐算法：

    - Pbkdf2PasswordEncoder 为我们提供了 PBKDF2
    - BCryptPasswordEncoder 为我们提供了 BCrypt，而
    - SCryptPasswordEncoder 提供 SCrypt

    PBKDF2、BCrypt 和 SCrypt 的密码编码器都支持配置所需的密码哈希强度。

    即使没有基于 Spring Security 的应用程序，我们也可以直接使用这些编码器。或者，如果我们正在使用 Spring Security 保护我们的网站，那么我们可以通过其 DSL 或依赖注入配置所需的密码编码器。

    与上述示例不同的是，这些加密算法会在内部为我们生成盐。算法会将盐存储在输出哈希值中，以便以后验证密码时使用。

7. 结论

    至此，我们已经深入了解了密码散列的概念和用途。

    此外，我们还了解了一些历史悠久的哈希函数，以及在用 Java 进行编码之前已经实现的一些哈希函数。

    最后，我们看到 Spring Security 提供了密码加密类，实现了一系列不同的哈希函数。

## Code

一如既往，代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-security-2) 上获取。

## Relevant Articles

- [Guide To The Java Authentication And Authorization Service (JAAS)](https://www.baeldung.com/java-authentication-authorization-service)
- [MD5 Hashing in Java](http://www.baeldung.com/java-md5)
- [Hashing a Password in Java](https://www.baeldung.com/java-password-hashing)
- [SHA-256 and SHA3-256 Hashing in Java](https://www.baeldung.com/sha-256-hashing-java)
- [Checksums in Java](https://www.baeldung.com/java-checksums)
- [How to Read PEM File to Get Public and Private Keys](https://www.baeldung.com/java-read-pem-file-keys)
- [Get a List of Trusted Certificates in Java](https://www.baeldung.com/java-list-trusted-certificates)
- [Security Context Basics: User, Subject and Principal](https://www.baeldung.com/security-context-basics)
- [The java.security.egd JVM Option](https://www.baeldung.com/java-security-egd)
- More articles: [[<-- prev]](../core-java-security/README-zh.md) [[next -->]](../core-java-security-3/README-zh.md)
