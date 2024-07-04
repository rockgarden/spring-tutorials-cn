# Java集合Cookbooks和示例

## Java中数组和列表之间的转换

1. 概述

    在这个快速教程中，我们将学习如何使用核心Java库、Guava和Apache Commons Collections在数组和列表之间进行转换。

    [将图元数组转换为列表](https://www.baeldung.com/java-primitive-array-to-list)

    了解如何将原语数组转换为相应类型的对象列表。

    [在Java中将集合转换为ArrayList](https://www.baeldung.com/java-convert-collection-arraylist)

    在Java中给定集合的情况下构建ArrayList的简短教程。

    [如何在Java中将列表转换为Map](https://www.baeldung.com/java-list-to-map)

    了解在Java中使用核心功能和一些流Map将List转换为Map的不同方法

2. 将列表转换为数组

    1. 使用普通Java

        让我们从使用普通Java从List转换为Array开始：

        givenUsingCoreJava_whenListConvertedToArray_thenCorrect()

        请注意，我们使用该方法的首选方法是`toArray(new T[0])`，而不是`toArray(new T[size])`。正如Aleksey Shipilév在他的[博客文章](https://shipilev.net/blog/2016/arrays-wisdom-ancients/#_conclusion)中所证明的那样，它似乎更快、更安全、更干净。

    2. 使用Guava

        现在，让我们使用Guava API进行相同的转换：

        givenUsingGuava_whenListConvertedToArray_thenCorrect()

3. 将数组转换为列表

    1. 使用普通Java

        让我们从用于将数组转换为列表的普通Java解决方案开始：

        givenUsingCoreJava_whenArrayConvertedToList_thenCorrect()

        请注意，这是一个固定大小的列表，仍然由阵列支持。如果需要标准ArrayList，我们可以简单地实例化一个：

        `List<Integer> targetList = new ArrayList<Integer>(Arrays.asList(sourceArray));`

    2. 使用Guava

        现在，让我们使用Guava API进行相同的转换：

        givenUsingGuava_whenArrayConvertedToList_thenCorrect()

    3. 使用公共集合

        最后，让我们使用[Apache Commons Collections](http://commons.apache.org/proper/commons-collections/javadocs/) CollectionUtils.addAll API在空列表中填充数组的元素：

        givenUsingCommonsCollections_whenArrayConvertedToList_thenCorrect()

## Relevant Articles

- [x] [Converting Between an Array and a List in Java](https://www.baeldung.com/convert-array-to-list-and-list-to-array)
- [Converting Between an Array and a Set in Java](https://www.baeldung.com/convert-array-to-set-and-set-to-array)
- [Convert a Map to an Array, List or Set in Java](https://www.baeldung.com/convert-map-values-to-array-list-set)
- [Converting a List to String in Java](https://www.baeldung.com/java-list-to-string)
- [How to Convert List to Map in Java](https://www.baeldung.com/java-list-to-map)
- [Converting a Collection to ArrayList in Java](https://www.baeldung.com/java-convert-collection-arraylist)
- [Java 8 Collectors toMap](https://www.baeldung.com/java-collectors-tomap)
- [Converting Iterable to Collection in Java](https://www.baeldung.com/java-iterable-to-collection)
- [Converting Iterator to List](https://www.baeldung.com/java-convert-iterator-to-list)
- More articles: [[next -->]](../core-java-collections-conversions-2)

## Code

所有这些示例和代码片段的实现都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-collections-conversions)上找到。这是一个基于Maven的项目，因此应该很容易导入和按原样运行。
