# Java String Operations

本模块包含关于字符串操作的文章。

## 在Java中检查空或空白的字符串

1. 简介

    在本教程中，我们将讨论在Java中检查空字符串或空白字符串的一些方法。有一些本地语言的方法，也有一些库的方法。

2. 空字符串与空白字符串

    当然，知道一个字符串是空的还是空白的是很常见的，但让我们确保我们的定义是一致的。

    如果一个字符串是空的或者是一个没有任何长度的字符串，我们认为它是空的。如果一个字符串只由空白组成，那么我们称它为空白。

    对于Java来说，空白是指字符，比如空格、制表符等等。我们可以查看[Character.isWhitespace](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Character.html#isWhitespace(char))的例子。

3. 空字符串

    1. 使用Java 6及以上版本

        如果我们至少是在Java 6上，那么检查空字符串的最简单方法就是[String#isEmpty](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#isEmpty())：

        ```java
        boolean isEmptyString(String string) {
            return string.isEmpty();
        }

        // 为了使它也是空安全的，我们需要增加一个额外的检查：

        boolean isEmptyString(String string) {
            return string == null || string.isEmpty()；
        }
        ```

    2. 与Java 5及以下版本

        String#isEmpty是在Java 6中引入的。对于Java 5及以下版本，我们可以用String#length代替：

        ```java
        boolean isEmptyString(String string) {
            return string == null || string.length() == 0;
        }
        ```

        事实上，String#isEmpty只是String#length的一个快捷方式。

4. 空白字符串

    String#isEmpty和String#length都可以用来检查空字符串。

    如果我们还想检测空白字符串，我们可以在String#trim的帮助下实现。在进行检查之前，它会删除所有前导和尾部的空白：

    ```java
    boolean isBlankString(String string) {
        return string == null || string.trim().isEmpty();
    }
    ```

    准确地说，String#trim将删除所有[Unicode代码小于或等于U+0020](https://en.wikipedia.org/wiki/List_of_Unicode_characters#Control_codes)的前导和尾部字符。

    还有，请记住，**字符串是不可变的**，所以调用trim实际上不会改变底层字符串。

    除了上述方法外，从Java 11开始，我们还可以使用[isBlank()](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#isBlank())方法来代替修剪：

    ```java
    boolean isBlankString(String string) {
        return string == null || string.isBlank();
    }
    ```

    isBlank()方法也更有效率一些，因为它不会在堆上创建一个新的String。因此，如果我们在Java 11或以上版本，这是首选的方法。

5. Bean验证

    检查空白字符串的另一种方法是正则表达式。例如，这在[Java Bean验证](https://www.baeldung.com/javax-validation)中就很方便：

    ```java
    @Pattern(regexp = "\\A(?!\\s*\\Z).+")
    String someString;
    ```

    给出的正则表达式确保空或空白字符串不会被验证。

6. 使用Apache Commons

    如果可以添加依赖项，我们可以使用[Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)。这有大量的Java帮助工具。

    如果我们使用Maven，我们需要在pom中添加commons-lang3依赖项：org.apache.commons.commons-lang3

    在其他方面，这给了我们[StringUtils](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html)。

    这个类带有[isEmpty](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html#isEmpty-java.lang.CharSequence-)、[isBlank](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html#isBlank-java.lang.CharSequence-)等方法：

    `StringUtils.isBlank(string)`

    这个调用与我们自己的isBlankString方法的作用相同。它是空的，也会检查空白处。

7. 使用Guava

    另一个带来某些字符串相关工具的著名库是谷歌的[Guava](https://github.com/google/guava)。从23.1版本开始，Guava有两种风格：Android和jre。Android风味的目标是Android和Java 7，而JRE风味的目标是Java 8。

    如果我们不以Android为目标，我们可以直接在pom中添加[JRE风味](https://mvnrepository.com/artifact/com.google.guava/guava/31.0.1-jre/jar)：com.google.guava.guava

    Guavas的Strings类自带方法[Strings.isNullOrEmpty](https://google.github.io/guava/releases/27.1-jre/api/docs/com/google/common/base/Strings.html#isNullOrEmpty-java.lang.String-)：

    `Strings.isNullOrEmpty(string)`

    它检查一个给定的字符串是否为空或空，但它不会检查纯白的字符串。

8. 总结

    有几种方法可以检查一个字符串是否为空。通常，我们还想检查一个字符串是否为空，也就是说，它只由空白字符组成。

    最方便的方法是使用Apache Commons Lang，它提供了诸如StringUtils.isBlank这样的帮助器。如果我们想坚持使用纯Java，我们可以使用String#trim与String#isEmpty或String#length的组合。对于Bean Validation，可以使用正则表达式来代替。

## Relevant Articles

- [Concatenating Strings In Java](https://www.baeldung.com/java-strings-concatenation)
- [x] [Checking for Empty or Blank Strings in Java](https://www.baeldung.com/java-blank-empty-strings)
- [String Initialization in Java](https://www.baeldung.com/java-string-initialization)
- [String toLowerCase and toUpperCase Methods in Java](https://www.baeldung.com/java-string-convert-case)
- [Java String equalsIgnoreCase()](https://www.baeldung.com/java-string-equalsignorecase)
- [Case-Insensitive String Matching in Java](https://www.baeldung.com/java-case-insensitive-string-matching)
- [L-Trim and R-Trim Alternatives in Java](https://www.baeldung.com/java-trim-alternatives)
- [Encode a String to UTF-8 in Java](https://www.baeldung.com/java-string-encode-utf-8)
- [Guide to Character Encoding](https://www.baeldung.com/java-char-encoding)
- [Convert Hex to ASCII in Java](https://www.baeldung.com/java-convert-hex-to-ascii)
- More articles: [[<-- prev]](../core-java-string-operations)

## Code

请确保在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-string-operations-2)上查看所有这些例子。
