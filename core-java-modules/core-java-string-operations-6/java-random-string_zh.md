# Java生成随机字符串

[Java字符串](https://www.baeldung.com/category/java/java-string)

[Apache Commons Lang](https://www.baeldung.com/tag/apache-commons-lang) [随机](https://www.baeldung.com/tag/random)

1. 简介

    在本教程中，我们将学习如何在Java中生成一个随机字符串，首先使用标准的Java库，然后使用Java 8的变体，最后使用[Apache Commons Lang](http://commons.apache.org/proper/commons-lang/)库。

    进一步阅读：

    [Java中的高效词频计算器](https://www.baeldung.com/java-word-frequency)

    探索 Java 中计算单词的各种方法，看看它们的性能如何。

    [Java - 随机长数、浮点数、整数和双数](https://www.baeldung.com/java-generate-random-long-float-integer-double)

    了解如何在 Java 中生成随机数--既可以是无界随机数，也可以是给定区间内的随机数。

    [Java 字符串池指南](https://www.baeldung.com/java-string-pool)

    了解 JVM 如何优化 Java 字符串池中分配给字符串存储的内存量。

2. 用普通Java生成随机无界字符串

    让我们从简单的开始，生成一个以7个字符为界限的随机字符串：

    RandomStringsUnitTest.java: givenUsingPlainJava_whenGeneratingRandomStringUnbounded_thenCorrect()

    请记住，新的字符串将不会是任何remotely的字母数字。

    进一步阅读：

    [Java中的高效词频计算器](https://www.baeldung.com/java-word-frequency)

    探索在Java中计算单词的各种方法，看看它们的表现如何。

    [Java - 随机长数、浮点数、整数和双数](https://www.baeldung.com/java-generate-random-long-float-integer-double)

    了解如何在Java中生成随机数--既包括无界的，也包括给定区间内的。

    [Java 字符串池指南](https://www.baeldung.com/java-string-pool)

    了解JVM如何优化分配给Java字符串池中的字符串存储的内存量。

3. 用普通的Java生成随机的有约束的字符串

    接下来让我们看看如何创建一个更有约束的随机字符串；我们将使用小写字母和设定的长度生成一个随机字符串：

    RandomStringsUnitTest.java: givenUsingPlainJava_whenGeneratingRandomStringBounded_thenCorrect()

4. 用Java 8生成随机字母字符串

    现在让我们使用JDK 8中添加的Random.ints来生成一个字母字符串：

    RandomStringsUnitTest.java: givenUsingJava8_whenGeneratingRandomAlphabeticString_thenCorrect()

5. 用Java 8生成随机字母数字字符串

    然后我们可以扩大我们的字符集，以得到一个字母数字字符串：

    RandomStringsUnitTest.java: givenUsingJava8_whenGeneratingRandomAlphanumericString_thenCorrect()

    我们使用上面的过滤方法，将65和90之间的Unicode字符排除在外，以避免超出范围的字符。

6. 用Apache Commons Lang生成有边界的随机字符串

    Apache的Commons Lang库对随机字符串的生成有很大帮助。让我们来看看只用字母生成一个有边界的字符串：

    RandomStringsUnitTest.java: givenUsingApache_whenGeneratingRandomStringBounded_thenCorrect()

    因此，这个例子没有Java例子中的所有低级代码，而是用一个简单的单行代码完成。

7. 用Apache Commons Lang生成字母字符串

    这是另一个非常简单的例子，这次是一个只有字母字符的有界字符串，但没有向API传递布尔标志：

    RandomStringsUnitTest.java: givenUsingApache_whenGeneratingRandomAlphabeticString_thenCorrect()

8. 用Apache Commons Lang生成字母数字字符串

    最后，我们有同样的随机有界字符串，但这次是数字：

    RandomStringsUnitTest.java: givenUsingApache_whenGeneratingRandomAlphanumericString_thenCorrect()

    就这样，我们用普通的Java、Java 8的变体或Apache Commons Library创建有界和无界的字符串。

9. 总结

    通过不同的实现方法，我们能够使用普通Java、Java 8变体或Apache Commons库来生成有界和无界的字符串。

    在这些Java例子中，我们使用了java.util.Random，但有一点值得一提的是，它在密码学上是不安全的。对于安全敏感的应用，可以考虑使用[java.security.SecureRandom](https://www.baeldung.com/java-secure-random)代替。

    本文中所有方法的代码都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-string-operations-6)上找到。
