# Java集合烹饪书Cookbooks和示例

## 用 Java 比较两个哈希映射

1. 概述

    在本教程中，我们将探讨在 Java 中比较两个 HashMap 的不同方法。

    我们将讨论检查两个 HashMap 是否相似的多种方法。我们还将使用 Java 8 Stream API 和 Guava 来获取不同 HashMaps 之间的详细差异。

2. 使用 Map.equals()

    代码示例：HashMapComparisonUnitTest.java

    首先，我们将使用 Map.equals() 来检查两个 HashMaps 是否有相同的条目：

    whenCompareTwoHashMapsUsingEquals_thenSuccess()

    在这里，我们创建了三个 HashMap 对象并添加了条目。然后，我们使用 Map.equals() 检查两个 HashMap 是否有相同的条目。

    Map.equals() 的工作方式是使用 Object.equals() 方法比较键和值。这意味着只有当键和值对象都正确实现 equals() 时，它才能工作。

    例如，当值类型是数组时，Map.equals() 就不起作用，因为数组的 equals() 方法比较的是标识(identity)而不是数组的内容：

    whenCompareTwoHashMapsWithArrayValuesUsingEquals_thenFail()

3. 使用 Java 流 API

    代码示例：HashMapComparisonUnitTest.java

    我们还可以使用 Java 8 Stream API 实现自己的方法来比较 HashMaps：areEqual()

    为了简单起见，我们实现了 areEqual() 方法，现在我们可以用它来比较 `HashMap<String, String>` 对象：

    whenCompareTwoHashMapsUsingStreamAPI_thenSuccess()

    但我们也可以定制自己的方法 areEqualWithArrayValue() 来处理数组值，方法是使用 Arrays.equals() 来比较两个数组。

    与 Map.equals() 不同，我们自己的方法可以成功比较具有数组值的 HashMaps：

    whenCompareTwoHashMapsWithArrayValuesUsingStreamAPI_thenSuccess()

4. 比较 HashMap 的键和值

    接下来，让我们看看如何比较两个 HashMap 键及其对应的值。

    1. 比较 HashMap 键值

        首先，我们可以通过比较两个 HashMap 的 KeySet() 来检查它们是否有相同的键：

        whenCompareTwoHashMapKeys_thenSuccess()

    2. 比较 HashMap 值

        接下来，我们将了解如何逐个比较 HashMap 值。

        我们将使用流 API 实现一个简单的方法来检查两个 HashMap 中哪些键的值相同：

        areEqualKeyValues()

        现在我们可以使用 areEqualKeyValues() 比较两个不同的 HashMaps，详细查看哪些键值相同，哪些键值不同：

        whenCompareTwoHashMapKeyValuesUsingStreamAPI_thenSuccess()

5. 使用 Guava 获取地图差异

    最后，我们将了解如何使用 Guava Maps.difference() 获取两个 HashMaps 之间的详细差异。

    该方法会返回一个 [MapDifference](https://google.github.io/guava/releases/20.0/api/docs/com/google/common/collect/MapDifference.html) 对象，该对象有许多有用的方法来分析地图之间的差异。让我们来看看其中的一些方法。

    1. MapDifference.entriesDiffering()

        首先，我们将使用 MapDifference.entriesDiffering() 获取每个 HashMap 中具有不同值的公共键：

        givenDifferentMaps_whenGetDiffUsingGuava_thenSuccess()

        entriesDiffering() 方法会返回一个新的 Map，其中包含公共键集和作为值集的 ValueDifference 对象。

        每个 ValueDifference 对象都有一个 leftValue() 和 rightValue() 方法，分别返回两个 Map 中的值。

    2. MapDifference.entriesOnlyOnRight() 和 MapDifference.entriesOnlyOnLeft()

        然后，我们可以使用 MapDifference.entriesOnlyOnRight() 和 MapDifference.entriesOnlyOnLeft() 获取只存在于一个 HashMap 中的条目：

        givenDifferentMaps_whenGetEntriesOnOneSideUsingGuava_thenSuccess()

    3. MapDifference.entriesInCommon()

        接下来，我们将使用 MapDifference.entriesInCommon() 获取常用条目：

        givenDifferentMaps_whenGetCommonEntriesUsingGuava_thenSuccess()

    4. 自定义 Maps.difference() 行为

        由于 Maps.difference() 默认使用 equals() 和 hashCode() 来比较条目，因此对于未正确实现这两种方法的对象，它将不起作用：

        givenSimilarMapsWithArrayValue_whenCompareUsingGuava_thenFail()

        但是，我们可以使用等价（Equivalence）自定义比较中使用的方法。

        例如，我们将为 String[] 类型定义 Equivalence，以比较 HashMaps 中的 String[] 值：

        givenSimilarMapsWithArrayValue_whenCompareUsingGuavaEquivalence_thenSuccess()

6. 结论

    在本文中，我们讨论了在 Java 中比较 HashMap 的不同方法。我们学习了检查两个 HashMap 是否相等的多种方法，以及如何获得详细的差异。

## Relevant Articles

- [Java TreeMap vs HashMap](https://www.baeldung.com/java-treemap-vs-hashmap)
- [x] [Comparing Two HashMaps in Java](https://www.baeldung.com/java-compare-hashmaps)
- [The Map.computeIfAbsent() Method](https://www.baeldung.com/java-map-computeifabsent)
- [Collections.synchronizedMap vs. ConcurrentHashMap](https://www.baeldung.com/java-synchronizedmap-vs-concurrenthashmap)
- [Java HashMap Load Factor](https://www.baeldung.com/java-hashmap-load-factor)
- [Converting Java Properties to HashMap](https://www.baeldung.com/java-convert-properties-to-hashmap)
- More articles: [[<-- prev]](/core-java-modules/core-java-collections-maps-2)

## Code

完整的源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-collections-maps-3) 上获取。
