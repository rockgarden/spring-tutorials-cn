# Java字符串算法

本模块包含有关字符串相关算法的文章。

## 如何删除一个字符串的最后一个字符？

1. 概述

    在这个快速教程中，我们将探讨去除字符串最后一个字符的不同技术。
2. 使用String.substring()

    最简单的方法是使用String类的内置substring()方法。
    为了删除一个给定字符串的最后一个字符，我们必须使用两个参数： 0作为起始索引，以及倒数第二个字符的索引。我们可以通过调用String的length()方法，并从结果中减去1来实现。
    然而，这个方法并不是空的，如果我们使用一个空字符串，这将会失败。
    为了解决空字符串的问题，我们可以把这个方法包装在一个辅助类中：

    ```java
    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
        ? null 
        : (s.substring(0, s.length() - 1));
    }
    ```

    我们可以重构代码，并使用Java 8：

    ```java
    public static String removeLastCharOptional(String s) {
        return Optional.ofNullable(s)
        .filter(str -> str.length() != 0)
        .map(str -> str.substring(0, str.length() - 1))
        .orElse(s);
        }
    ```

3. 使用StringUtils.substring()

    我们可以使用Apache Commons Lang3库中的StringUtils类，而不是重新发明轮子，它提供了有用的String操作。其中之一是一个无效安全的substring()方法，它可以处理异常。
    为了包括StringUtils，我们必须更新我们的pom.xml文件：

    ```xml
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.12.0</version>
    </dependency>
    ```

    StringUtils.substring()需要三个参数：一个给定的字符串，第一个字符的索引（在我们的例子中它总是0），以及倒数第二个字符的索引。同样，我们可以简单地使用length()方法并减去1：

    ```java
    String TEST_STRING = "abcdef";
    StringUtils.substring(TEST_STRING, 0, TEST_STRING.length() - 1);
    ```

    然而，这个操作并不是空安全的。不过对于空字符串，它可以正常工作。
4. 使用StringUtils.chop()

    StringUtils类提供了chop()方法，它能很好地处理所有的边缘情况：empty和null字符串。
    它非常容易使用，而且只需要一个参数：字符串。它的唯一目的是删除最后一个字符，仅此而已：

    `StringUtils.chop(TEST_STRING);`

5. 使用正则表达式

    我们还可以通过很好地使用正则表达式来删除String中的最后一个字符（或任何数量的字符）。
    例如，我们可以使用String类本身的replaceAll()方法，它需要两个参数：正则表达式和替换的String：

    `TEST_STRING.replaceAll(".$", "");`

    请注意，由于我们在String上调用了一个方法，所以这个操作不是空安全的。

    另外，replaceAll()和regex表达式乍看之下可能很复杂。我们可以在这里阅读更多关于regex的内容，但是为了使逻辑更容易操作，我们可以把它封装在一个辅助类中：

    ```java
    public static String removeLastCharRegex(String s) {
        return (s == null) ? null : s.replaceAll(".$", "");
    }
    ```

    > 注意，如果一个字符串以换行结束，那么上述方法将失败，因为regex中的"."可以匹配任何字符，除了行结束符。

    最后，让我们用Java 8重新写一下实现：

    ```java
    public static String removeLastCharRegexOptional(String s) {
        return Optional.ofNullable(s)
        .map(str -> str.replaceAll(".$", ""))
        .orElse(s);
    }
    ```

6. 总结

    在这篇简短的文章中，我们讨论了只删除一个字符串的最后一个字符的不同方法，有些是手动的，有些是开箱即用。
    如果我们需要更多的灵活性，并且需要删除更多的字符，我们可以使用正则表达式这种更高级的解决方案。

## Relevant Articles

- [x] [How to Remove the Last Character of a String?](https://www.baeldung.com/java-remove-last-character-of-string)
- [Add a Character to a String at a Given Position](https://www.baeldung.com/java-add-character-to-string)
- [Java Check a String for Lowercase/Uppercase Letter, Special Character and Digit](https://www.baeldung.com/java-lowercase-uppercase-special-character-digit-regex)
- [Remove or Replace part of a String in Java](https://www.baeldung.com/java-remove-replace-string-part)
- [Replace a Character at a Specific Index in a String in Java](https://www.baeldung.com/java-replace-character-at-index)
- [Join Array of Primitives with Separator in Java](https://www.baeldung.com/java-join-primitive-array)
- [Pad a String with Zeros or Spaces in Java](https://www.baeldung.com/java-pad-string)
- [Remove Leading and Trailing Characters from a String](https://www.baeldung.com/java-remove-trailing-characters)
- [Counting Words in a String with Java](https://www.baeldung.com/java-word-counting)
- [Finding the Difference Between Two Strings in Java](https://www.baeldung.com/java-difference-between-two-strings)
- More articles: [[<-- prev]](../core-java-string-algorithms)

## Code

一如既往，文章中使用的代码可以在GitHub上找到。
