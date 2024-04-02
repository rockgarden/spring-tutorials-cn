# [用Java初始化HashMap](https://www.baeldung.com/java-initialize-hashmap)

[Java Map](https://www.baeldung.com/category/java/java-collections/java-map)

[Java HashMap](https://www.baeldung.com/tag/java-hashmap) [Map Entry](https://www.baeldung.com/tag/map-entry)

1. 概述

    在本教程中，我们将学习在 Java 中初始化 HashMap 的各种方法。

    我们将使用 Java 8 和 Java 9。

2. 静态 HashMap 的静态初始化器

    > 示例代码文件参见：map.initialize/MapInitializer.java

    我们可以使用静态代码块初始化 HashMap：

    articleMapOne

    这种初始化方式的优点是map是可变的，但只能用于静态。因此，可以根据需要添加和删除条目。

    让我们继续测试它：

    MapInitializerUnitTest.java: givenStaticMap_whenUpdated_thenCorrect()

    我们还可以使用双括号语法(double-brace)初始化 Map：

    ```java
    Map<String, String> doubleBraceMap = new HashMap<String, String>() {{
        put("key1", "value1")；
        put("key2", "value2")；
    }};
    ```

    请注意，我们必须尽量避免使用这种初始化技术，因为它在每次使用时都会创建一个匿名的额外类，持有对外层对象的隐藏引用，并可能导致内存泄漏问题。

3. 使用 Java 集合

    > 示例代码文件参见：map.initialize/MapInitializer.java

    如果我们需要创建一个只有一个入口的单例不可变映射，Collections.singletonMap() 就会变得非常有用：

    createSingletonMap()

    请注意，这里的 Map 是不可变的，如果我们试图添加更多条目，它将抛出 java.lang.UnsupportedOperationException 异常。

    我们还可以使用 Collections.emptyMap() 创建不可变的空 map：

    `Map<String, String> emptyMap = Collections.emptyMap();`

4. Java 8 的方法

    本节将介绍使用 Java 8 Stream API 初始化地图的方法。

    > 示例代码文件参见：map.initialize/MapInitializer.java

    1. 使用 Collectors.toMap()

        让我们使用一个二维(two-dimensional) String 数组流，并将它们收集到一个 map 中：

        createMapUsingStreamStringArray()

        请注意，这里 Map 的键和值的数据类型是相同的。

        为了使其更通用，我们可以使用对象数组执行相同的操作：

        createMapUsingStreamObjectArray()

        结果，我们创建了一个键为字符串、值为整数的映射。

    2. 使用 Map.Entry 流

        这里我们将使用 Map.Entry 的实例。这是另一种使用不同键和值类型的方法。

        首先，让我们使用 Entry 接口的 SimpleEntry 实现：

        createMapUsingStreamSimpleEntry()

        现在，让我们使用 SimpleImmutableEntry 实现创建地图：

        createMapUsingStreamSimpleImmutableEntry()

    3. 初始化不可变Map

        在某些用例中，我们需要初始化不可变映射。这可以通过在 Collectors.collectingAndThen() 中封装 Collectors.toMap() 来实现：

        createMapUsingStreamSimpleImmutableEntry()

        请注意，我们应避免使用 Streams 进行此类初始化，因为这会造成巨大的性能开销，而且仅仅为了初始化 map 就会创建大量垃圾对象。

5. Java 9 方法

    Java 9 在 Map 接口中提供了多种工厂方法，可简化不可变映射的创建和初始化。

    让我们来了解一下这些工厂方法。

    1. Map.of()

        该工厂方法不包含参数、单参数和变量参数：

        ```java
        Map<String, String> emptyMap = Map.of();
        Map<String, String> singletonMap = Map.of("key1", "value");
        Map<String, String> map = Map.of("key1","value1", "key2", "value2");
        ```

        请注意，此方法最多只能支持 10 个键值对。

    2. Map.ofEntries()

        该方法与 Map.of() 类似，但对键值对的数量没有限制：

        ```java
        Map<String, String> map = Map.ofEntries(
        new AbstractMap.SimpleEntry<String, String>("name", "John"),
        new AbstractMap.SimpleEntry<String, String>("city", "budapest"),
        new AbstractMap.SimpleEntry<String, String>("zip", "000000"),
        new AbstractMap.SimpleEntry<String, String>("home", "1231231231")
        );
        ```

        请注意，工厂方法生成的是不可变的Map，因此任何突变都会导致 UnsupportedOperationException 异常。

        此外，它们不允许使用空键或重复键。

        现在，如果我们在初始化后需要一个可变或增长的Map，我们可以创建 Map 接口的任何实现，并在构造函数中传递这些不可变的地图：

        ```java
        Map<String, String> map = new HashMap<String, String> (
        Map.of("key1","value1", "key2", "value2"));
        Map<String, String> map2 = new HashMap<String, String> (
        Map.ofEntries(
            new AbstractMap.SimpleEntry<String, String>("name", "John"),
            new AbstractMap.SimpleEntry<String, String>("city", "budapest")));
        ```

6. 使用 Guava

    我们已经了解了使用核心 Java 的方法，下面让我们使用 Guava 库来初始化一个地图：

    `Map<String, String> articles = ImmutableMap.of("Title", "My New Article", "Title2", "Second Article");`

    这将创建一个不可变的映射，下面将创建一个可变的映射：

    `Map<String, String> articles = Maps.newHashMap(ImmutableMap.of("Title", "My New Article", "Title2", "Second Article"));`

    [ImmutableMap.of()](https://guava.dev/releases/23.0/api/docs/com/google/common/collect/ImmutableMap.html#of--) 方法也有重载版本，最多可接受 5 对键值参数。下面是一个包含 2 对参数的示例：

    ImmutableMap.of("key1", "value1", "key2", "value2");

7. 结论

    在本文中，我们探讨了初始化 Map 的各种方法，尤其是创建空、单例、不可变和可变 Map 的方法。正如我们所看到的，自 Java 9 以来，该领域有了巨大的改进。

    一如既往，示例源代码位于 [Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-collections-maps-2) 项目中。Java 9 示例位于[此处](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-9)，Guava 示例位于[此处](https://github.com/eugenp/tutorials/tree/master/guava-modules/guava-collections-map)。
