# [在Java中比较字符串](https://www.baeldung.com/java-compare-strings)

1. 概述

    在这篇文章中，我们将讨论Java中比较字符串的不同方法。

    由于字符串是Java中最常用的数据类型之一，这自然是一个非常常用的操作。

2. 用字符串类比较字符串

   参见 stringcomparison/StringComparisonUnitTest.java

    1. 使用"=="比较运算符

        使用"=="运算符来比较文本值是Java初学者最常犯的错误之一。这是不正确的，因为"=="只检查两个字符串的引用是否相等，也就是说它们是否引用了同一个对象。

        让我们来看看这种行为的例子：

        whenUsingComparisonOperator_ThenComparingStrings()

        在上面的例子中，第一个断言是真的，因为这两个变量指向同一个字符串字面。

        另一方面，第二个断言是假的，因为string1是用一个literal创建的，而string3是用new操作符创建的--因此它们引用了不同的对象。

    2. 使用equals()

        String类重写了从Object继承的equals()。这个方法逐个比较两个字符串的字符，忽略它们的地址。

        如果它们的长度相同，而且字符的顺序相同，则认为它们是相等的：

        whenUsingEqualsMethod_ThenComparingStrings()

        在这个例子中，string1、string2和string4变量是相等的，因为无论它们的地址如何，它们都有相同的大小写和值。

        对于string3，该方法返回错误，因为它是区分大小写的。

        另外，如果两个字符串中的任何一个是空的，那么该方法就会返回false。

    3. 使用equalsIgnoreCase()

        equalsIgnoreCase()方法返回一个布尔值。顾名思义，该方法在比较字符串时忽略了字符的大小写：

        whenUsingEqualsIgnoreCase_ThenComparingStrings()

    4. 使用compareTo()方法

        compareTo()方法返回一个int类型的值，并根据字典或自然排序对两个字符串逐个进行字符比较。

        如果两个字符串相等，该方法返回0，如果第一个字符串在参数之前，则返回一个负数，如果第一个字符串在参数字符串之后，则返回一个大于0的数字。

        让我们看一个例子：whenUsingCompareTo_ThenComparingStrings()

    5. 使用compareToIgnoreCase()

        compareToIgnoreCase()与前面的方法类似，只是它忽略了大小写：whenUsingCompareToIgnoreCase_ThenComparingStrings()

3. 用Objects类进行字符串比较

    Objects是一个实用类，包含一个静态的equals()方法，在这种情况下很有用--比较两个字符串。

    参见 stringcomparison/StringComparisonUnitTest.java

    如果两个字符串相等，该方法首先使用它们的地址进行比较，即"=="，返回真。因此，如果两个参数都是空的，它返回真，如果正好有一个参数是空的，它返回假。

    否则，它就会简单地调用所传递的参数类型的类的equals()方法--在我们的例子中是String的类equals()方法。这个方法是区分大小写的，因为它在内部调用String类的equals()方法。

    让我们来测试一下：whenUsingObjectsEqualsMethod_ThenComparingStrings()

4. 用Apache Commons进行字符串比较

    Apache Commons库包含一个名为StringUtils的实用类，用于与字符串相关的操作；它也有一些非常有益于字符串比较的方法。

    参见 stringcomparison/StringComparisonUnitTest.java

    1. 使用 equals() 和 equalsIgnoreCase()

        StringUtils类的equals()方法是String类方法equals()的增强版，它也可以处理null值：

        whenUsingEqualsOfApacheCommons_ThenComparingStrings()

        StringUtils的equalsIgnoreCase()方法返回一个布尔值。它的工作原理与equals()相似，只是它忽略了字符串中字符的大小写：

        whenUsingEqualsIgnoreCaseOfApacheCommons_ThenComparingStrings()

    2. 使用 equalsAny() 和 equalsAnyIgnoreCase()

        equalsAny()方法的第一个参数是一个字符串，第二个参数是一个多args类型的CharSequence。如果任何其他给定的字符串与第一个字符串敏感地匹配，该方法返回true。

        否则，返回false：whenUsingEqualsAnyOf_ThenComparingStrings()

        equalsAnyIgnoreCase()方法的工作原理与equalsAny()方法相似，但也忽略了大小写：

        whenUsingEqualsAnyIgnoreCase_ThenComparingStrings()

    3. 使用compare()和compareIgnoreCase()方法

        StringUtils类中的compare()方法是String类中compareTo()方法的防空版本，通过考虑一个空值小于一个非空值来处理空值。两个空值被认为是相等的。

        此外，这个方法可以用来对有空项的字符串列表进行排序：

        whenUsingCompare_thenComparingStringsWithNulls()

        compareIgnoreCase()方法的行为类似，只是它忽略了大小写：

        whenUsingCompareIgnoreCase_ThenComparingStringsWithNulls()

        这两个方法也可以与nullIsLess选项一起使用。这是第三个布尔参数，决定空值是否应该被视为较小。

        如果nullIsLess为真，一个空值就比另一个String低，如果nullIsLess为假，就比它高。

        让我们来试试：

        whenUsingCompareWithNullIsLessOption_ThenComparingStrings()

        带有第三个布尔参数的compareIgnoreCase()方法工作类似，只是忽略了大小写。

5. 总结

    在这个快速教程中，我们讨论了比较字符串的不同方法。
