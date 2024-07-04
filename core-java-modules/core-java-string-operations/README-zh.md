# Java字符串操作

## 在Java中比较字符串

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

## 在Java中分割一个字符串

1. 简介

    分割字符串是一个非常频繁的操作；本快速教程主要介绍我们可以在Java中简单地使用一些API来完成这个操作。
2. String.split()

    让我们从核心库开始--String类本身提供了一个split()方法--它非常方便，足以满足大多数情况的需要。它只是根据分隔符来分割给定的String，并返回一个字符串数组。
    让我们看看一些例子。我们先来看看用逗号分割的情况：

    `String[] splitted = "peter,james,thomas".split(",");`

    让我们通过一个空格来分割：

    `String[] splitted = "car jeep scooter".split(" ");`

    让我们也用一个点来分割：

    `String[] splitted = "192.168.1.178".split("\\.")`

    现在让我们通过regex来分割多个字符--逗号、空格和连字符：

    `String[] splitted = "b a, e, l.d u, n g".split("\\s+|,\\s*|\\.\\s*"));`

3. StringUtils.split()

    Apache的通用语言包提供了一个StringUtils类--它包含一个null-safe split()方法，使用空格作为默认分隔符进行分割：

    `String[] splitted = StringUtils.split("car jeep scooter");`

    此外，它还忽略了额外的空格：

    `String[] splitted = StringUtils.split("car   jeep  scooter");`

4. Splitter.split()

    最后，在Guava中也有一个不错的Splitter流畅的API：

    ```java
    List<String> resultList = Splitter.on(',')
    .trimResults()
    .omitEmptyStrings()
    .splitToList("car,jeep,, scooter");
    ```

5. 分割和修剪

    有时一个给定的字符串包含一些前导、尾部或分隔符周围的额外空间。让我们看看如何一次性处理分割输入和修剪结果。
    比方说，我们有这样一个输入：

    `String input = " car , jeep, scooter ";`

    为了去除分隔符之前和/或之后的额外空格，我们可以使用regex进行分割和修剪：

    `String[] splitted = input.trim().split("\\s*,\\s*");`

    这里，trim()方法删除了输入字符串中的前导和尾部空格，而regex本身处理了分隔符周围的额外空格。
    我们可以通过使用Java 8的流特性实现同样的结果：

    ```java
    String[] splitted = Arrays.stream(input.split(","))
    .map(String::trim)
    .toArray(String[]::new);
    ```

6. 总结

    一般来说，String.split()已经足够了。然而，对于更复杂的情况，我们可以利用Apache的基于commons-lang的StringUtils类，或者干净而灵活的Guava APIs。

## toString()方法

1. 概述

    Java中的每个类都是Object类的子类，无论是直接还是间接。由于Object类包含一个toString()方法，我们可以在任何实例上调用toString()并获得其字符串表示。
    在本教程中，我们将看看toString()的默认行为，并学习如何改变其行为。

2. 默认行为

    每当我们打印一个对象引用时，它都会在内部调用toString()方法。所以，如果我们没有在类中定义toString()方法，那么就会调用Object#toString()。

    ```java
    public String toString() {
        return getClass().getName()+"@"+Integer.toHexString(hashCode());
    }
    ```

    为了看看这个方法是如何工作的，让我们创建一个客户对象，我们将在整个教程中使用：

    tostring/Customer.java

    现在，如果我们试图打印我们的Customer对象，Object#toString()将被调用，输出结果将类似于：
    `com.baeldung.tostring.Customer@6d06d69c`

3. 重写默认行为

    看一下上面的输出，我们可以看到它并没有给我们关于Customer对象的内容的很多信息。一般来说，我们对知道一个对象的哈希码不感兴趣，而是对我们对象的属性内容感兴趣。
    通过覆盖toString()方法的默认行为，我们可以使方法调用的输出更加有意义。
    现在，让我们看一下使用对象的几个不同场景，看看我们如何覆盖这个默认行为。

4. 原始类型和字符串

    我们的客户对象有字符串和原始属性。我们需要覆盖toString()方法来实现更有意义的输出：
    tostring/CustomerPrimitiveToString.java

    让我们看看现在调用toString()时得到什么：

    CustomerPrimitiveToStringUnitTest.java\givenPrimitive_whenToString_thenCustomerDetails()

5. 复杂的Java对象

    现在让我们考虑一个场景，我们的Customer对象也包含一个订单属性，该属性是Order类型的。我们的订单类同时拥有字符串和原始数据类型字段。
    所以，让我们再次覆盖toString()：
    tostring/CustomerComplexObjectToString.java

    由于订单是一个复杂的对象，如果我们只是打印我们的客户对象，而没有覆盖订单类中的toString()方法，它将把订单打印成`Order@<hashcode>`。
    为了解决这个问题，我们也在Order中覆盖toString()：
    tostring/Order.java

    现在，让我们看看当我们对包含订单属性的客户对象调用toString()方法时会发生什么：

    CustomerComplexObjectToStringUnitTest.java\givenComplex_whenToString_thenCustomerDetails()

6. 对象的数组

    接下来，让我们把我们的客户改为有一个订单数组。如果我们只是打印我们的客户对象，而不对我们的订单对象进行特殊处理，它将把订单打印成`Order;@<hashcode>`。
    为了解决这个问题，我们对订单字段使用[Arrays.toString()](https://www.baeldung.com/java-array-to-string)：
    tostring/CustomerArrayToString.java

    让我们看看调用上述toString()方法的结果：

    CustomerArrayToStringUnitTest.java\givenArray_whenToString_thenCustomerDetails()

7. 封装器、集合和StringBuffers

    当一个对象完全由[包装器](https://www.baeldung.com/java-wrapper-classes)、[集合](https://www.baeldung.com/java-collections)或[StringBuffers](https://www.baeldung.com/java-collections)组成时，不需要自定义toString()实现，因为这些对象已经用有意义的表示方法重写了toString()方法：

    tostring/CustomerWrapperCollectionToString.java

    让我们再次看看调用toString()的结果：

    CustomerWrapperCollectionToStringUnitTest.java\givenWrapperCollectionStrBuffer_whenToString_thenCustomerDetails()

8. 总结

    在这篇文章中，我们研究了创建我们自己的toString()方法的实现。

## Relevant Articles

- [x] [Comparing Strings in Java](https://www.baeldung.com/java-compare-strings)
- [Check If a String Is Numeric in Java](https://www.baeldung.com/java-check-string-number)
- [Get Substring from String in Java](https://www.baeldung.com/java-substring)
- [Split a String in Java](https://www.baeldung.com/java-split-string)
- [Common String Operations in Java](https://www.baeldung.com/java-string-operations)
- [x] [Java toString() Method](https://www.baeldung.com/java-tostring)
- [String Operations with Java Streams](https://www.baeldung.com/java-stream-operations-on-strings)
- [Adding a Newline Character to a String in Java](https://www.baeldung.com/java-string-newline)
- [Check If a String Contains a Substring](https://www.baeldung.com/java-string-contains-substring)
- [Java Base64 Encoding and Decoding](https://www.baeldung.com/java-base64-encode-and-decode)
- More articles: [[next -->]](../core-java-string-operations-2)

## Code

一如既往，这些例子的源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-string-operations)上找到。
