# 核心 Java 8 Cookbooks 和示例

- [Java 正则表达式性能概述](https://www.baeldung.com/java-regex-performance)
- [Java正则表达式API指南](http://www.baeldung.com/regular-expressions-java)
- [x] [Java RegExps中的转义字符指南](#regexps中的转义字符指南)
- [将 Regex 模式预编译为模式对象](https://www.baeldung.com/java-regex-pre-compile)
- [Java 匹配器 find() 和 matches() 的区别](https://www.baeldung.com/java-matcher-find-vs-matches)
- [Java 中的正则表达式 \s 和 \s+](https://www.baeldung.com/java-regex-s-splus)
- [用 Java Regex 验证电话号码](https://www.baeldung.com/java-regex-validate-phone-numbers)
- [如何计算 Regex 的匹配数？](https://www.baeldung.com/java-count-regex-matches)
- [用 Java 查找字符串中的所有数字](https://www.baeldung.com/java-find-numbers-in-string)
- [了解 Pattern.quote 方法](https://www.baeldung.com/java-pattern-quote)
- 更多文章： [[下-->]](/core-java-modules/core-java-regex-2)

## RegExps中的转义字符指南

1. 概述

    Java 中的正则表达式 API（java.util.regex）被广泛用于模式匹配。

    在本文中，我们将重点讨论正则表达式中的转义字符(Escaping Characters)，并展示如何在 Java 中实现这一功能。

    测试代码：EscapingCharsUnitTest.java

2. 特殊正则表达式字符

    根据 Java 正则表达式 API 文档，正则表达式中存在一组特殊字符，也称为元字符。

    当我们想让这些字符保持原样，而不是解释它们的特殊含义时，就需要转义它们。通过转义，我们可以强制将这些字符作为普通字符，与给定的正则表达式匹配字符串。

    通常需要转义的元字符有

    `<([{\^-=$!|]})?*+.>`

    让我们来看一个简单的代码示例，在这个示例中，我们将输入字符串与正则表达式中表达的模式进行匹配。

    该测试表明，对于给定的输入字符串 foof，当匹配到模式 foo.（以点字符结尾的 foo）时，返回值为 true，表示匹配成功。

    givenRegexWithDot_whenMatchingStr_thenMatches()

    你可能会问，为什么输入字符串中没有点（.）字符，却能匹配成功？

    答案很简单。点（.）是一个元字符--点在这里的特殊意义在于，可以有 "任何字符" 代替它。因此，很明显，匹配器是如何确定找到了匹配字符的。

    比方说，我们不想用点（.）字符的独特含义来处理它。相反，我们希望将其解释为点号。这就意味着，在前面的示例中，我们不想让输入字符串中的模式 foo.出现匹配。

    我们该如何处理这种情况呢？答案是：我们需要转义点（.）字符，这样它的特殊含义就会被忽略。

3. 转义字符

    根据正则表达式的 Java API 文档，有两种方法可以转义具有特殊含义的字符。换句话说，就是强制将它们视为普通字符。

    让我们来看看它们是什么：

    - 在元字符前加上反斜线 `(\)`
    - 用 `\Q` 和 `\E` 包围元字符
    这就意味着，在我们前面看到的例子中，如果我们想转义点字符，就需要在点字符前加上反斜杠字符。或者，我们也可以将点字符放在 \Q 和 \E 之间。

    1. 使用反斜杠转义

        这是我们可以用来在正则表达式中转义元字符的技术之一。但是，我们知道反斜杠字符在 Java 字符串字面量中也是一个转义字符。因此，在使用反斜杠字符作为任何字符（包括字符本身）的前缀时，我们需要将其加倍。

        因此，在我们的示例中，我们需要更改正则表达式，如本测试所示：

        givenRegexWithDotEsc_whenMatchingStr_thenNotMatching()

        在这里，点字符被转义，因此匹配器只是将其视为点，并尝试查找以点结尾的模式（即 foo.）。

        在这种情况下，由于输入字符串中没有与该模式匹配的字符，因此会返回 false。

    2. 使用 \Q & \E 进行转义

        另外，我们还可以使用 \Q 和 \E 来转义特殊字符。\Q 表示需要转义直到 \E 的所有字符，而 \E 表示我们需要结束用 \Q 开始的转义。

        这就意味着，介于 \Q 和 \E 之间的字符都将被转义。

        在这里显示的测试中，String 类的 split() 使用提供给它的正则表达式进行匹配。

        我们的要求是将输入字符串中的管道 (|) 字符分割成单词。因此，我们使用正则表达式模式来实现这一目的。

        管道字符是一个元字符，需要在正则表达式中转义。

        在这里，转义是通过将管道字符放在 \Q 和 \E 之间来完成的：

        givenRegexWithPipeEscaped_whenSplitStr_thenSplits()

4. Pattern.quote(String S) 方法

    java.util.regex.Pattern 类中的 Pattern.Quote(String S) 方法将给定的正则表达式模式字符串转换为字面模式字符串。这意味着输入字符串中的所有元字符都被视为普通字符。

    使用这种方法比使用 \Q & \E 更为方便，因为它可以将给定的 String 包起来。

    让我们来看看这种方法的实际应用：

    givenRegexWithPipeEscQuoteMeth_whenSplitStr_thenSplits()

    在这个快速测试中，Pattern.quote() 方法用于转义给定的 regex 模式并将其转换为字符串字面。换句话说，它为我们转义了 regex 模式中的所有元字符。它所做的工作与 \Q & \E 类似。

    Pattern.quote()方法转义了管道字符，split()将其解释为一个字符串字面，并以此分割输入内容。

    我们可以看到，这是一种更简洁的方法，开发人员也不必记住所有的转义序列。

    我们需要注意的是，Pattern.quote 用一个转义序列包围了整个代码块。如果我们想单独转义字符，就需要使用标记替换算法。

5. 其他示例

    让我们看看 java.util.regex.Matcher 的 replaceAll() 方法是如何工作的。

    如果我们需要用另一个字符串替换给定字符串的所有出现，我们可以通过向该方法传递正则表达式来使用该方法。

    假设我们有一个包含多个 $ 字符的输入。我们希望得到的结果是用 £ 替换 $ 字符后的字符串。

    这个测试演示了如何在不转义的情况下传递模式 $：

    void givenRegexWithDollar_whenReplacing_thenNotReplace()

    该测试断言 $ 没有被 £ 正确替换。

    现在，如果我们转义 regex 模式，替换就会正确进行，测试就会通过，如代码片段所示：

    givenRegexWithDollarEsc_whenReplacing_thenReplace()

    请注意这里的 \\$ ，它通过转义 $ 字符实现了技巧，并成功匹配了模式。

6. 结论

    本文介绍了 Java 正则表达式中的转义字符。

    我们讨论了为什么正则表达式需要转义，以及实现转义的不同方法。

## Code

与往常一样，您可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-regex) 上找到与本文相关的源代码。
