# [Java链接表指南](https://www.baeldung.com/java-linkedlist)

1. 简介

    [LinkedList](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedList.html) 是 [List](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html) 和 [Deque](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Iterator.html) 接口的双链表实现。它实现了所有可选的列表操作，并允许所有元素（包括空）。

    Deque: 双端队列（Double Ended Queue）。

2. 特性

    下面是 LinkedList 最重要的属性：

    - 索引到列表的操作将从列表的开始或结束（以更接近指定索引者为准）开始遍历列表
    - 不[同步](http://stackoverflow.com/a/1085745/2486904)
    - 它的[迭代器](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Iterator.html)和[列表迭代器](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ListIterator.html)都是[快失效](http://stackoverflow.com/questions/17377407/what-is-fail-safe-fail-fast-iterators-in-java-how-they-are-implemented)的（这意味着在迭代器创建后，如果列表被修改，将抛出并发修改异常（[ConcurrentModificationException](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ConcurrentModificationException.html)））。
    - 每个元素都是一个节点，它保留了对下一个和上一个元素的引用
    - 保持插入顺序

    虽然 LinkedList 不是同步的，但我们可以通过调用 [Collections.synchronizedList](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Collections.html#synchronizedList(java.util.List)) 方法来获取同步版本，例如

    `List list = Collections.synchronizedList(new LinkedList(...));`

3. 与 ArrayList 的比较

    虽然两者都实现了 List 接口，但它们的语义不同，这肯定会影响到使用哪一个的决定。

    1. 结构

        ArrayList 是一种由数组支持的基于索引的数据结构。它提供对其元素的随机访问，性能相当于 O(1)。

        另一方面，LinkedList 将数据存储为元素列表，每个元素都与上一个和下一个元素相关联。在这种情况下，搜索一个项目的执行时间等于 O(n)。

    2. 操作

        在关联列表中，项目的插入、添加和删除操作速度更快，因为当一个元素被添加到集合内部的某个任意位置时，无需调整数组的大小或更新索引，只有周围元素的引用会发生变化。

    3. 内存使用

        关联列表比数组列表消耗更多内存，因为关联列表中的每个节点都存储了两个引用，一个是上一个元素的引用，另一个是下一个元素的引用，而数组列表只存储数据及其索引。

4. 使用方法

    下面的一些代码示例展示了如何使用 LinkedList：

    1. 创建

        `LinkedList<Object> linkedList = new LinkedList<>();`

    2. 添加元素

        LinkedList 实现了 List 和 Deque 接口，除了标准的 add() 和 addAll() 方法外，你还可以找到 addFirst() 和 addLast()，它们分别用于在开始或结束时添加元素。

    3. 删除元素

        与添加元素类似，此 list 实现也提供 removeFirst() 和 removeLast()。

        此外，还有一个方便的方法 removeFirstOccurence() 和 removeLastOccurence()，可返回布尔值（如果集合包含指定元素，则返回 true）。

    4. 队列操作

        Deque 接口提供了类似队列的行为（实际上 Deque 扩展了 Queue 接口）：

        ```java
        linkedList.poll();
        linkedList.pop();
        ```

        这些方法会获取第一个元素并将其从列表中删除。

        poll() 和 pop() 的区别在于，如果列表为空，pop 会抛出 NoSuchElementException()，而 poll 会返回 null。此外，还提供了 pollFirst() 和 pollLast() 应用程序接口。

        下面是推送 API 的工作原理示例：

        `linkedList.push(Object o);`

        将元素作为集合的首部插入。

        LinkedList还有很多其他方法，对于已经使用过Lists的用户来说，其中大部分方法应该都很熟悉。Deque提供的其他方法可能会成为 "标准 "方法的替代方法。

        完整的文档可以在[这里](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedList.html)找到。

5. 结论

    ArrayList 通常是默认的 List 实现。

    然而，在某些使用情况下，使用 LinkedList 会更合适，例如，对于恒定的插入/删除时间（例如，频繁的插入/删除/更新）的偏好，会超过恒定的访问时间和有效的内存使用。
