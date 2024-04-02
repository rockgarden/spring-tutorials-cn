# Java 核心集合

本模块包含有关 Java 集合的文章

## Java中的迭代器指南

1. 迭代器简介

    迭代器是我们遍历集合的众多方法之一，与其他方法一样，它也有优缺点。

    迭代器最早出现在 Java 1.2 中，用来替代枚举和：

    - 引入了改进的方法名
    - [可以从正在迭代的集合中移除元素](https://www.baeldung.com/java-concurrentmodificationexception)
    - 不保证迭代顺序
    在本教程中，我们将回顾简单的迭代器接口，学习如何使用其不同的方法。

    我们还将查看更强大的 ListIterator 扩展，它增加了一些有趣的功能。

2. 迭代器接口

    首先，我们需要从集合中获取一个迭代器；这可以通过调用 iterator() 方法来实现。

    为简单起见，我们将从列表中获取迭代器实例：

    ```java
    List<String> items = ...
    Iterator<String> iter = items.iterator();
    ```

    Iterator 接口有三个核心方法：

    1. hasNext()

        hasNext() 方法可用于检查是否至少还有一个元素可以迭代。

        它被设计为 while 循环中的一个条件：

        `while (iter.hasNext()) { ... }`

    2. next()

        next() 方法可用于跨步获取下一个元素：

        `String next = iter.next();`

        在尝试调用 next() 之前，最好先使用 hasNext()。

        [除非特定实现提供了迭代顺序](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Collection.html#iterator())，否则集合的迭代器并不保证以任何特定顺序迭代。

    3. 删除

        最后，如果我们想从集合中移除当前元素，可以使用 remove：

        iter.remove();

        这是一种在对集合进行迭代时移除元素的安全方法，不会有发生并发修改异常（[ConcurrentModificationException](https://www.baeldung.com/java-concurrentmodificationexception)）的风险。

    4. 完整迭代器示例

        现在，我们可以将它们结合起来，看看如何将这三种方法一起用于集合过滤：

        ```java
        while (iter.hasNext()) {
            String next = iter.next();
            System.out.println(next);
            if( "TWO".equals(next)) {
                iter.remove();
            }
        }
        ```

        这就是我们通常使用迭代器的方法，我们提前检查是否有另一个元素，然后检索它，再对它执行一些操作。

    5. 使用 Lambda 表达式迭代

        正如我们在前面的示例中看到的，当我们只想遍历所有元素并对其执行某些操作时，使用迭代器是非常啰嗦的。

        从 Java 8 开始，我们有了 forEachRemaining 方法，它允许使用 lambda 来处理剩余的元素：

        `iter.forEachRemaining(System.out::println);`

3. ListIterator 接口

    ListIterator 是一种扩展，它为遍历列表添加了新功能：

    `ListIterator<String> listIterator = items.listIterator(items.size());`

    注意我们如何提供一个起始位置，在本例中就是列表的末尾。

    1. hasPrevious() 和 previous()

        ListIterator 可用于反向遍历，因此它提供了 hasNext() 和 next() 的等价函数：

        ```java
        while(listIterator.hasPrevious()) {
            String previous = listIterator.previous();
        }
        ```

    2. nextIndex() 和 previousIndex()

        此外，我们还可以遍历索引而不是实际元素：

        ```java
        String nextWithIndex = items.get(listIterator.nextIndex());
        String previousWithIndex = items.get(listIterator.previousIndex());
        ```

        如果我们需要知道当前正在修改的对象的索引，或者我们想保留已删除元素的记录，这可能会非常有用。

    3. add()

        add 方法，顾名思义，允许我们在 next() 返回的元素之前和 previous() 返回的元素之后添加一个元素：

        `listIterator.add("FOUR");`

    4. set()

        最后一个值得一提的方法是 set()，它允许我们替换调用 next() 或 previous() 时返回的元素：

        ```java
        String next = listIterator.next();
        if( "ONE".equals(next)) {
            listIterator.set("SWAPPED");
        }
        ```

        需要注意的是，只有在没有调用 add() 或 remove() 之前，才能执行此操作。

    5. 完整 ListIterator 示例

        现在我们可以将它们全部组合起来，组成一个完整的示例：

        IteratorGuide.java

        在这个示例中，我们首先从 List 中获取 ListIterator，然后通过索引（不会增加迭代器内部的当前元素）或调用 next 获取下一个元素。

        然后，我们可以用 set 替换一个特定项，用 add 插入一个新项。

        迭代结束后，我们可以向后修改其他元素，或者直接从下至上打印。

4. 结论

    迭代器接口允许我们在遍历集合的同时修改集合，而简单的 for/while 语句则很难做到这一点。这反过来又为我们提供了一个很好的模式，我们可以在许多方法中使用它，这些方法只需要处理集合，同时保持良好的内聚性和低耦合性。

## 相关文章

- [Introduction to the Java ArrayDeque](https://www.baeldung.com/java-array-deque)
- [An Introduction to Java.util.Hashtable Class](https://www.baeldung.com/java-hash-table)
- [Thread Safe LIFO Data Structure Implementations](https://www.baeldung.com/java-lifo-thread-safe)
- [Time Complexity of Java Collections](https://www.baeldung.com/java-collections-complexity)
- [A Guide to EnumMap](https://www.baeldung.com/java-enum-map)
- [x] [A Guide to Iterator in Java](https://www.baeldung.com/java-iterator)
- [Defining a Char Stack in Java](https://www.baeldung.com/java-char-stack)
- [Guide to the Java Queue Interface](https://www.baeldung.com/java-queue)
- [An Introduction to Synchronized Java Collections](https://www.baeldung.com/java-synchronized-collections)
- [Convert an Array of Primitives to a List](https://www.baeldung.com/java-primitive-array-to-list)
- More articles: [[next -->]](/core-java-modules/core-java-collections-2)

## Code

最后，我们可以一如既往地在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-collections) 上获取完整的源代码。
