# [在Java中分割一个字符串](https://www.baeldung.com/java-split-string)

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
