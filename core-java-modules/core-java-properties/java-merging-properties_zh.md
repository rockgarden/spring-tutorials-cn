# [合并java.util.Properties对象](https://www.baeldung.com/java-merging-properties)

[Java](https://www.baeldung.com/category/java)

1. 简介

    在这个简短的教程中，我们将重点讨论如何将两个或多个 Java 属性对象合并为一个。

    我们将探讨三种解决方案，首先从使用迭代的示例开始。接下来，我们将学习使用 putAll() 方法，最后，我们将学习使用 Java 8 Streams 的更现代的方法。

    要了解如何开始使用 Java 属性，请查看我们的[介绍性文章](java-properties_zh.md)。

2. 快速回顾属性的使用

    首先，让我们回顾一下属性的一些关键概念。

    我们通常在应用程序中使用属性来定义配置值。在 Java 中，我们使用简单的键/值对来表示这些值。此外，在每个键值对中，键和值都是字符串值。

    通常，我们使用 java.util.Properties 类来表示和管理这些值对。值得注意的是，该类继承自 Hashtable。

    要了解有关 Hashtable 数据结构的更多信息，请阅读 [Java.util.Hashtable](https://www.baeldung.com/java-hash-table) 简介。

    1. 设置属性

        为了保持简单，我们将在示例中以编程方式设置属性：

        test/.properties/MergePropertiesUnitTest.java:propertiesA()

        在上面的示例中，我们创建了一个 Properties 对象，并使用 setProperty() 方法设置了两个属性。这在内部调用了 Hashtable 类中的 put() 方法，但确保对象是字符串值。

        注意，强烈不建议直接使用 put() 方法，因为它允许调用者插入键或值不是字符串的条目。

3. 使用迭代合并属性

    现在我们来看看如何使用迭代合并两个或多个属性对象：

    test/.properties/MergePropertiesUnitTest.java:mergePropertiesByIteratingKeySet()

    让我们把这个示例分成几个步骤：

    - 首先，我们创建一个属性对象来保存所有合并的属性
    - 接着，我们循环查看要合并的属性对象
    - 然后，我们调用 stringPropertyNames() 方法获取一组属性名称
    - 然后循环查看所有属性名称，并获取每个名称的属性值
    - 最后，将属性值设置到步骤 1 中创建的变量中

4. 使用 putAll() 方法

    现在我们来看看使用 putAll() 方法合并属性的另一种常见解决方案：

    test/.properties/MergePropertiesUnitTest.java:mergePropertiesByUsingPutAll()

    在第二个示例中，我们同样首先创建一个名为 mergedProperties 的 Properties 对象，用于保存所有合并的属性。同样，我们会遍历要合并的 Properties 对象，但这次我们使用 putAll() 方法将每个 Properties 对象添加到 mergedProperties 变量中。

    putAll() 方法是从 Hashtable 继承而来的另一个方法。通过该方法，我们可以将指定 Properties 中的所有映射复制到新的 Properties 对象中。

    值得一提的是，我们不鼓励在任何类型的Map中使用 putAll()，因为最终可能会出现键或值不是字符串的条目。

5. 使用流 API 合并属性

    最后，我们来看看如何使用 Stream API 合并多个属性对象：

    test/.properties/MergePropertiesUnitTest.mergePropertiesByUsingStreamApi()

    在上一个示例中，我们从属性列表中创建了一个流，然后使用 collect 方法将流中的值序列还原为一个新的集合。第一个参数是一个 Supplier 函数，用于创建一个新的结果容器，在我们的例子中就是一个新的 Properties 对象。

    Java 8 中引入了流 API，我们将为您提供该 API 的[入门指南](https://www.baeldung.com/java-8-streams-introduction)。

6. 总结

    在这个简短的教程中，我们介绍了合并两个或多个 Properties 对象的三种不同方法。
