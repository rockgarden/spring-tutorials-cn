# 计算字符串中一个字符的出现次数

[Java字符串](https://www.baeldung.com/category/java/java-string)

1. 概述

    在Java中，有许多方法可以计算字符串中出现的字符数。
    在这个快速教程中，我们将重点介绍几个如何计算字符的例子--首先是使用核心Java库，然后是使用其他库和框架，如Spring和Guava。

    进一步阅读：

    [使用indexOf查找字符串中一个词的所有出现次数](https://www.baeldung.com/java-indexof-find-string-occurrences)

    了解如何通过使用indexOf方法来解决"大海捞针"的问题，在一个较大的文本字符串中找到一个词的所有出现的地方。

    [Guava CharMatcher](https://www.baeldung.com/guava-string-charmatcher)

    使用Guava CharMatcher来处理字符串--移除特殊字符、验证、修剪、折叠、替换和计数以及其他超级有用的API。

    [用Apache Commons Lang 3处理字符串](https://www.baeldung.com/string-processing-commons-lang)

    快速介绍如何使用Apache Commons库和StringUtils来处理字符串。

2. 使用核心Java库

    1. 势在必行的方法

        有些开发者可能更喜欢使用核心Java。有很多方法可以计算字符串中一个字符的出现次数。
        让我们从一个simple/naive的方法开始：

        test/.countingchars/CountCharsExampleUnitTest.java:givenString_whenUsingLoop_thenCountChars()

    2. 使用递归

        一个不太明显但仍然有趣的解决方案是使用递归：

        test/.countingchars/CountCharsExampleUnitTest.java:useRecursion()

        我们可以用以下方式调用这个递归方法：useRecursionToCountChars("elephant", 'e', 0)。
    3. 使用正则表达式

        另一种方法是使用正则表达式：

        test/.countingchars/CountCharsExampleUnitTest.java:givenString_whenUsingReqExp_thenCountChars

        请注意，这个解决方案在技术上是正确的，但却是次优的，因为使用非常强大的正则表达式来解决寻找字符串中某个字符出现的次数这样一个简单的问题是矫枉过正。
    4. 使用Java 8的功能

        Java 8中的新功能在这里可以发挥很大的作用。
        让我们使用流和lambdas来实现计数：

        test/.countingchars/CountCharsExampleUnitTest.java:givenString_whenUsingJava8Features_thenCountChars

        所以，这显然是一个使用核心库的更干净、更可读的解决方案。

3. 使用外部库

    现在让我们来看看一些利用外部库的实用程序的解决方案。
    1. 使用StringUtils

        一般来说，使用现有的解决方案总是比自己发明的要好。commons.lang.StringUtils类为我们提供了countMatches()方法，它可以用来计算给定字符串中的字符甚至子字符串。
        首先，我们需要包括适当的依赖性：

        ```xml
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>
        ```

        现在让我们用countMatches()来计算 "elephant" 字符串字面中e字符的数量：

        ```java
        int count = StringUtils.countMatches("elephant", "e");
        assertEquals(2, count);
        ```

    2. 使用Guava

        Guava也可以在计算字符方面有所帮助。我们需要定义依赖关系：

        ```xml
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>31.0.1-jre</version>
        </dependency>
        ```

        让我们看看Guava如何快速帮助我们计算字符：

        ```java
        int count = CharMatcher.is('e').countIn("elephant");
        assertEquals(2, count);
        ```

    3. 使用Spring

        自然，在我们的项目中加入Spring框架只是为了计算字符并没有意义。
        然而，如果我们的项目中已经有了它，我们只需要使用 countOccurencesOf() 方法：

        ```java
        int count = StringUtils.countOccurrencesOf("elephant", "e");
        assertEquals(2, count);
        ```

4. 总结

    在这篇文章中，我们集中讨论了计算字符串中的字符的各种方法。其中一些是纯粹用Java设计的；一些需要额外的库。
    我们的建议是使用StringUtils、Guava或Spring中已有的实用程序。然而，如果只想使用普通的Java，本文提供了一些用Java 8完成的可能性。

    这些例子的完整源代码可以在这个[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-string-algorithms)项目中找到。
