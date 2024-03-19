# Java多行字符串

[Java字符串](https://www.baeldung.com/category/java/java-string)

1. 概述

    在本教程中，我们将学习如何在Java中声明多行字符串。

    现在，Java 15已经发布，我们可以使用新的本地特性，即文本块。

    如果我们不能使用这一特性，我们还将回顾其他方法。

2. 文本块

    我们可以通过用""来声明字符串来使用Text Blocks。(三个双引号)：

    multiline/MultiLineString.java: textBlocks()

    这是迄今为止声明多行字符串的最方便的方法。的确，我们不必处理行间分隔符或缩进空间，正如我们的[专门文章](https://www.baeldung.com/java-text-blocks)中指出的那样。

    这个功能在Java 15中可用，但如果我们[启用预览功能](https://www.baeldung.com/java-preview-features)，Java 13和14也可用。

    在下面的章节中，我们将回顾其他的方法，如果我们使用以前的Java版本或者Text Blocks不适用的话，这些方法是合适的。

3. 获取行的分隔符

    每个操作系统都可以有自己的方式来定义和识别新的行。

    在Java中，获取操作系统的行分隔符非常容易：

    `String newLine = System.getProperty("line.separator");`

    我们将在下面的章节中使用这个newLine来创建多行字符串。

4. 字符串连接

    字符串连接是一种简单的本地方法，可以用来创建多行字符串：

    multiline/MultiLineString.java: stringConcatenation()

    使用+运算符是实现同样事情的另一种方式。

    Java编译器以同样的方式翻译concat()和+运算符：

    ```java
    public String stringConcatenation() {
        return "Get busy living"
                + newLine
                + "or"
                + newLine
                + "get busy dying."
                + newLine
                + "--Stephen King";
    }
    ```

5. 字符串连接

    Java 8引入了[String#join](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#join(java.lang.CharSequence,java.lang.CharSequence...))，它接受一个分隔符和一些字符串作为参数。

    它返回一个最终的字符串，所有输入的字符串用分隔符连接在一起：

    multiline/MultiLineString.java: stringJoin()

6. 字符串生成器

    StringBuilder是一个用于构建字符串的辅助类。[StringBuilder](https://www.baeldung.com/java-string-builder-string-buffer)是在Java 1.5中引入的，作为StringBuffer的替代品。

    它是在循环中建立巨大的字符串的一个好选择：

    multiline/MultiLineString.java: stringBuilder()

7. 字符串书写器

    StringWriter是另一个我们可以利用的方法

    StringWriter是另一种方法，我们可以利用它来创建一个多行字符串。我们在这里不需要newLine，因为我们使用PrintWriter。

    println函数会自动添加新行：

    multiline/MultiLineString.java: stringWriter()

8. Guava Joiner

    仅仅为这样一个简单的任务使用一个外部库并没有什么意义。但是，如果项目已经将该库用于其他目的，我们可以利用它。

    例如，谷歌的Guava库非常流行。

    Guava有一个[Joiner](https://www.baeldung.com/guava-joiner-and-splitter-tutorial)类，能够建立多行字符串：

    MultiLineString.java: guavaJoiner()

9. 从文件加载

    Java完全按照文件的原样读取文件。这意味着，如果我们在一个文本文件中有一个多行字符串，当我们读取文件时就会有同样的字符串。在Java中，有很多方法可以从文件中读取。

    实际上，将长字符串与代码分开是一个好的做法：

    MultiLineString.java: loadFromFile()

10. 使用IDE功能

    许多现代IDE支持多行复制/粘贴。Eclipse和IntelliJ IDEA就是这种IDE的例子。我们可以简单地复制我们的多行字符串，并在这些IDE中把它粘贴在两个双引号内。

    显然，这种方法对运行时的字符串创建不起作用，但这是一种获得多行字符串的快速而简单的方法。

11. 总结

    在这篇文章中，我们学习了几种在Java中建立多行字符串的方法。

    好消息是，Java 15通过Text Blocks对多行字符串有本地支持。

    所有其他回顾的方法都可以在Java 15或以前的任何版本中使用。

    本文中所有方法的代码都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-strings)上找到。
