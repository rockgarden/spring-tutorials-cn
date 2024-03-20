# [在Java中检查一个字符串是否是回文](https://www.baeldung.com/java-palindrome)

[Java字符串](https://www.baeldung.com/category/java/java-string)

1. 简介

    在这篇文章中，我们将看到如何用Java来检查一个给定的字符串是否是一个回文。
    回文是指一个单词、短语、数字或其他的字符序列，其前后读法相同，如 "madam" 或 "racecar"。
2. 解决方案

    在下面的章节中，我们将讨论检查一个给定的字符串是否是宫格的各种方法。
    1. 一个简单的方法

        首先通过正则表达式"\\s+"去除字符串中的空格，而后同时开始向前和向后迭代给定的字符串，一次一个字符。如果有匹配的，循环继续；否则，循环退出：

        .palindrom/Palindrome.java: isPalindrome()

    2. 反转字符串

        有几种不同的实现适合这个用例：我们可以在检查重音时利用StringBuilder和StringBuffer类的API方法，或者我们可以在没有这些类的情况下反转String。
        让我们先看看没有辅助API的代码实现：

        .palindrom/Palindrome.java: isPalindromeReverseTheString(String text)

        在上面的片段中，我们简单地从最后一个字符开始迭代给定的字符串，并将每个字符追加到下一个字符，一直到第一个字符，从而反转了给定的字符串。
        最后，我们测试给定的字符串和反转的字符串之间是否相等。
        同样的行为也可以用API方法实现。
        让我们看一个快速演示：

        .palindrom/Palindrome.java: isPalindromeUsingStringBuilder(String text)

        也可用StringBuffer实现，但即使在使用来自单个线程的StringBuffer，同步也会对性能产生很大的负面影响。

        .palindrom/Palindrome.java: isPalindromeUsingStringBuffer(String text)

        在代码片段中，我们从StringBuilder和StringBuffer API中调用reverse()方法来反转给定的String并测试是否相等。

    3. 使用Stream API

        我们也可以使用IntStream来提供一个解决方案：

        .palindrom/Palindrome.java: isPalindromeUsingIntStream(String text)

        在上面的片段中，我们验证了字符串两端的字符对都不符合谓词条件。

    4. 使用递归

        递归是解决这类问题的一种非常流行的方法。在所演示的例子中，我们递归地遍历给定的字符串，并测试出它是否是一个宫格：

        .palindrom/Palindrome.java: isPalindromeRecursive(String text)

        .palindrom/Palindrome.java: recursivePalindrome(String text, int forward, int backward)

3. 总结

    在这个快速教程中，我们看到了如何找出一个给定的字符串是否是一个回文。

    这些例子的完整源代码可以在这个[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-string-algorithms)项目中找到。
