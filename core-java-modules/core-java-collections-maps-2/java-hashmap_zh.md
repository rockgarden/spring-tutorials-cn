# [Java HashMap指南](https://www.baeldung.com/java-hashmap)

[Java Map](https://www.baeldung.com/category/java/java-collections/java-map)

[Java HashMap](https://www.baeldung.com/tag/java-hashmap)

1. 概述

    在这篇文章中，我们将看到如何在Java中使用HashMap，并且我们将看看它的内部工作情况。
    与HashMap非常相似的一个类是Hashtable。请参考我们的其他几篇文章，了解更多关于[java.util.Hashtable](https://www.baeldung.com/java-hash-table)类本身以及[HashMap和Hashtable之间的区别](https://www.baeldung.com/hashmap-hashtable-differences)。
2. 基本用法

    我们先来看看HashMap是一个Map是什么意思。Map是一种键值映射，这意味着每个键都正好映射到一个值，我们可以使用键从Map中检索到相应的值。
    有人可能会问，为什么不简单地将值添加到一个列表中呢？为什么我们需要一个HashMap呢？简单的原因是性能。如果我们想在一个列表中找到一个特定的元素，时间复杂度是O(n)，如果列表被排序了，使用例如二进制搜索，时间复杂度将是O(log n)。
    HashMap的优势在于，插入和检索一个值的时间复杂度平均为O(1)。我们将在后面看一下如何实现这个目标。我们先来看看如何使用HashMap。

    1. 设置

        让我们创建一个简单的类，我们将在整个文章中使用它：
        map/Product.java

    2. 放置

        我们现在可以创建一个HashMap，键为String类型，元素为Product类型：

        `Map<String, Product> productsByName = new HashMap<>();`

        并将产品添加到我们的HashMap中：

        ```java
        Product eBike = new Product("E-Bike", "A bike with a battery");
        Product roadBike = new Product("Road bike", "A bike for competition");
        productsByName.put(eBike.getName(), eBike);
        productsByName.put(roadBike.getName(), roadBike);
        ```

    3. 获取

        我们可以通过键从地图中检索一个值：

        ```java
        Product nextPurchase = productsByName.get("E-Bike");
        assertEquals("A bike with a battery", nextPurchase.getDescription());
        ```

        如果我们试图为地图中不存在的键找到一个值，我们会得到一个空值：

        ```java
        Product nextPurchase = productsByName.get("Car");
        assertNull(nextPurchase);
        ```

        如果我们用相同的键插入第二个值，我们将只得到该键的最后一个插入的值：

        ```java
        Product newEBike = new Product("E-Bike", "A bike with a better battery");
        productsByName.put(newEBike.getName(), newEBike);
        assertEquals("A bike with a better battery", productsByName.get("E-Bike").getDescription());
        ```

    4. 空值作为键

        HashMap也允许我们将空值作为一个键：

        ```java
        Product defaultProduct = new Product("Chocolate", "At least buy chocolate");
        productsByName.put(null, defaultProduct);
        Product nextPurchase = productsByName.get(null);
        assertEquals("At least buy chocolate", nextPurchase.getDescription());
        ```

    5. 具有相同键的值

        此外，我们可以用不同的键来插入同一个对象两次：

        ```java
        productsByName.put(defaultProduct.getName(), defaultProduct);
        assertSame(productsByName.get(null), productsByName.get("Chocolate"));
        ```

    6. 删除一个值

        我们可以从 HashMap 中删除一个键值映射：

        ```java
        productsByName.remove("E-Bike");
        assertNull(productsByName.get("E-Bike"));
        ```

    7. 检查一个键或值是否存在于Map中

        为了检查一个键是否存在于地图中，我们可以使用containsKey()方法：
        `productsByName.containsKey("E-Bike");`

        或者，要检查一个值是否存在于地图中，我们可以使用containsValue()方法：
        `productsByName.containsValue(eBike);`

        在我们的例子中，这两个方法的调用都将返回true。虽然它们看起来非常相似，但这两个方法调用在性能上有一个重要区别。检查一个键是否存在的复杂度是O(1)，而检查一个元素的复杂度是O(n)，因为需要在Map的所有元素上循环。
    8. 遍历哈希图

        有三种基本的方法来遍历HashMap中的所有键值对。
        我们可以在所有键的集合上进行迭代：

        ```java
        for(String key : productsByName.keySet()) {
            Product product = productsByName.get(key);
        }
        ```

        或者我们可以遍历所有条目的集合：

        ```java
        for(Map.Entry<String, Product> entry : productsByName.entrySet()) {
            Product product =  entry.getValue();
            String key = entry.getKey();
            //do something with the key and value
        }
        ```

        最后，我们可以对所有的值进行迭代：
        `List<Product> products = new ArrayList<>(productsByName.values());`

3. 键值

    我们可以在我们的HashMap中使用任何类作为键。然而，为了使Map正常工作，我们需要为equals()和hashCode()提供一个实现。比方说，我们想有一个以产品为键，以价格为值的Map：

    ```java
    HashMap<Product, Integer> priceByProduct = new HashMap<>();
    priceByProduct.put(eBike, 900);
    ```

    让我们实现 equals() 和 hashCode() 方法：

    map/Product.java: equals() hashCode()

    请注意，hashCode()和equals()只需要被重写给我们想用作Map键的类，而不是只用作Map中的值的类。我们将在本文的第5节中看到这一点的原因。
4. 从Java 8开始的额外方法

    Java 8为HashMap增加了几个函数式方法。在这一节中，我们将看看这些方法中的一些。
    对于每个方法，我们将看两个例子。第一个例子展示了如何使用新方法，第二个例子展示了如何在早期的Java版本中实现同样的方法。

    由于这些方法都很直接，我们就不看更详细的例子了。
    1. forEach()
        forEach方法是以函数式的方式来遍历地图中的所有元素：

        map/Product.java: forEach()

        我们的文章[《Java 8 forEach指南》](https://www.baeldung.com/foreach-java)更详细地介绍了forEach循环。
    2. getOrDefault()
        使用getOrDefault()方法，我们可以从映射中获得一个值，或者在没有给定键的映射的情况下返回一个默认元素：

        map/Product.java: getOrDefault()
    3. putIfAbsent()
        通过这个方法，我们可以添加一个新的映射，但前提是还没有给定键的映射：

        map/Product.java: putIfAbsent()

        我们的文章[《用Java 8合并两个地图》](https://www.baeldung.com/java-merge-maps)对这个方法进行了仔细的研究。
    4. merge()
        通过merge()，如果存在一个映射，我们可以修改给定键的值，否则可以添加一个新的值：

        map/Product.java: merge()

    5. compute()

        通过 compute() 方法，我们可以计算出给定键的值：

        map/Product.java: compute()

        值得注意的是，merge()和compute()的方法非常相似。compute()方法接受两个参数：Key和用于重映射的BiFunction。而merge()方法接受三个参数：key，如果key还不存在，要添加到map中的 default value ，以及一个用于重映射的BiFunction。
5. HashMap内部

    在本节中，我们将看看HashMap的内部工作原理，以及使用HashMap而不是简单的列表等有哪些好处。
    正如我们所看到的，我们可以通过键从HashMap中检索一个元素。一种方法是使用一个列表，遍历所有的元素，当我们找到一个与键匹配的元素时返回。这种方法的时间和空间复杂性都是O(n)。
    使用HashMap，我们可以实现放和取操作的平均时间复杂度为O(1)，空间复杂度为O(n)。让我们来看看这是如何做到的。

    1. 哈希代码和等价物
        HashMap不是在其所有元素上进行迭代，而是试图根据键来计算一个值的位置。
        Naive的做法是有一个列表，它可以包含尽可能多的元素，就像有可能有的键一样。举个例子，假设我们的键是一个小写的字符。那么有一个大小为26的列表就足够了，如果我们想访问键值为'c'的元素，我们就会知道它在第3位，我们可以直接检索它。
        然而，如果我们有一个更大的键空间，这种方法就不太有效了。例如，我们假设我们的键是一个整数。在这种情况下，列表的大小将必须是2,147,483,647。在大多数情况下，我们的元素也会少得多，所以分配的内存有很大一部分是未使用的。
        HashMap将元素存储在所谓的桶中，桶的数量被称为容量。
        当我们在地图中放入一个值时，键的hashCode()方法被用来确定该值将被存储在哪个桶中。
        为了检索该值，HashMap以同样的方式计算桶(buckets)--使用hashCode()。然后它遍历在该桶中发现的对象，并使用key的equals()方法来找到完全匹配的对象。
    2. Keys的不可更改性
        在大多数情况下，我们应该使用不可变的键。或者至少，我们必须意识到使用可变键的后果。
        让我们看看当我们的键在我们用它在地图中存储一个值之后发生了什么变化。
        对于这个例子，我们将创建MutableKey：

        map/ProductUnitTest.java: MutableKey {}

        下面进行测试：

        ```java
        MutableKey key = new MutableKey("initial");
        Map<MutableKey, String> items = new HashMap<>();
        items.put(key, "success");
        key.setName("changed");
        assertNull(items.get(key));
        ```

        正如我们所看到的，一旦key发生变化，我们就不再能够得到相应的值，相反，返回的是null。这是因为HashMap在错误的桶中搜索。

        如果我们对HashMap的内部工作方式没有很好的理解，上述测试案例可能会让人感到惊讶。
    3. 碰撞

        要想正常工作，相等的键必须有相同的哈希值，然而，不同的键可以有相同的哈希值。如果两个不同的键有相同的哈希值，属于它们的两个值将被存储在同一个桶中。在一个桶内，值被存储在一个列表中，并通过在所有元素上循环来检索。这方面的成本是O(n)。
        从Java 8开始（见JEP 180），如果一个桶中包含8个或更多的值，那么存储在一个桶中的值的数据结构将从列表变为平衡树，如果在某一时刻，桶中只剩下6个值，那么它将变回一个列表。这样就提高了性能，达到了O(log n)。
    4. 容量和负载系数

        为了避免许多桶中有多个值，如果75%的桶（负载因子）成为非空的，容量就会翻倍。负载因子的默认值是75%，而默认的初始容量是16。两者都可以在构造函数中设置。
    5. 放和取操作的总结

        让我们总结一下put和get操作的工作原理。
        当我们向地图中添加一个元素时，HashMap会计算该桶。如果该桶已经包含了一个值，该值就会被添加到属于该桶的列表（或树）中。如果负载因子变得比地图的最大负载因子大，那么容量就会增加一倍。
        当我们想从地图中获取一个值时，HashMap会计算桶并从列表（或树）中获取具有相同键的值。
6. 总结

    在这篇文章中，我们看到了如何使用HashMap以及它的内部工作方式。与ArrayList一样，HashMap是Java中最经常使用的数据结构之一，所以掌握如何使用它以及它在内部如何工作的知识是非常方便的。我们的文章《[引擎盖下的Java HashMap](https://www.baeldung.com/java-hashmap-advanced)》更详细地介绍了HashMap的内部结构。
