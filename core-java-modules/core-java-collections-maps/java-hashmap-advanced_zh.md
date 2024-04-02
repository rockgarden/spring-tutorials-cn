# [Hood下的Java HashMap](https://www.baeldung.com/java-hashmap-advanced)

[Java Map](https://www.baeldung.com/category/java/java-collections/java-map)

[Java HashMap](https://www.baeldung.com/tag/java-hashmap)

1. 概述

    在这篇文章中，我们将更详细地探讨Java集合框架中最流行的Map接口的实现，继续我们[介绍](https://www.baeldung.com/java-hashmap)文章中的内容。
    在我们开始实现之前，有必要指出，主要的List和Set集合接口都扩展了Collection，但Map没有。
    简单地说，HashMap是按键来存储数值的，并提供了用于添加、检索和以各种方式操作存储数据的API。这个实现是基于哈希图的原理，乍听起来有点复杂，但实际上非常容易理解。
    键值对被存储在所谓的桶中，这些桶共同构成了所谓的表，这实际上是一个内部数组。
    一旦我们知道一个对象被存储或将要被存储的键，存储和检索操作就会在恒定的时间内发生，在一个尺寸良好的哈希图中是O(1)。

    为了理解哈希图是如何工作的，我们需要了解哈希图所采用的存储和检索机制。我们将大量关注这些。
    最后，哈希图相关的问题在面试中相当常见，所以这是一个准备面试或准备面试的可靠方法。
2. put()API

    为了在哈希图中存储一个值，我们调用put API，它需要两个参数；一个键和相应的值：

    `V put(K key, V value);`

    当一个值被添加到Map的一个键下时，键对象的hashCode()API被调用以检索所谓的初始哈希值。
    为了看到这个动作，让我们创建一个将作为键的对象。我们将只创建一个属性作为哈希码来模拟哈希的第一阶段：

    ```java
    public class MyKey {
        private int id;
        @Override
        public int hashCode() {
            System.out.println("Calling hashCode()");
            return id;
        }
        // constructor, setters and getters 
    }
    ```

    我们现在可以使用这个对象来映射哈希图中的一个值：

    map/MapUnitTest.java whenHashCodeIsCalledOnPutAndGet_thenCorrect()

    在上面的代码中没有发生什么，但要注意控制台的输出。的确，hashCode方法被调用了：
    `Calling hashCode()`

    接下来，哈希图的hash()API被内部调用，用初始哈希值计算最终的哈希值。
    这个最终的哈希值最终会归结为内部数组中的一个索引，或者我们称之为桶的位置。
    HashMap的哈希函数看起来像这样：

    ```java
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    ```

    我们应该注意的是，这里只是使用了键对象的哈希代码来计算最终的哈希值。
    而在put函数里面，最终的哈希值是这样使用的：

    ```java
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
    ```

    请注意，一个内部的putVal函数被调用，并将最终的哈希值作为第一个参数。
    有人可能会问，既然我们已经用它来计算哈希值了，为什么还要在这个函数中使用key呢？
    原因是哈希Map将键和值都作为Map.Entry对象存储在桶的位置。
    正如之前所讨论的，所有的Java集合框架接口都扩展了Collection接口，但Map没有。比较一下我们前面看到的Map接口的声明和Set接口的声明：

    `public interface Set<E> extends Collection<E>`

    原因是，Map并不像其他集合那样确切地存储单一元素，而是一个键值对的集合。
    所以Collection接口的通用方法，如add、toArray，在Map上就没有意义了。

    我们在过去三段中所涉及的概念是最受欢迎的Java集合框架面试问题之一。所以，这是值得理解的。
    哈希图的一个特殊属性是，它接受空值和空键：

    MapUnitTest.java givenNullKeyAndVal_whenAccepts_thenCorrect()

    当在put操作中遇到空键时，它将被自动分配一个最终的哈希值为0，这意味着它成为底层数组的第一个元素。
    这也意味着，当键为空时，没有散列操作，因此，键的hashCode API没有被调用，最终避免了空指针异常。
    在put操作中，当我们使用一个之前已经使用过的key来存储一个值时，会返回与该key相关的之前的值：

    MapUnitTest.java givenExistingKey_whenPutReturnsPrevValue_thenCorrect()

    否则，它将返回空值：

    MapUnitTest.java givenNewKey_whenPutReturnsNull_thenCorrect()

    当put返回null时，也可能意味着与键相关的前一个值是null，而不一定是一个新的键-值映射：

    MapUnitTest.java givenNullVal_whenPutReturnsNull_thenCorrect()

    containsKey API可以用来区分这种情况。

3. 获取API

    要检索一个已经存储在哈希图中的对象，我们必须知道它被存储在哪个键下。我们调用get API并向其传递key对象：

    MapUnitTest.java  whenGetWorks_thenCorrect()

    在内部，使用同样的散列原则。钥匙对象的hashCode()API被调用以获得初始哈希值：

    MapUnitTest.java whenHashCodeIsCalledOnPutAndGet_thenCorrect()

    这一次，MyKey的hashCode API被调用了两次；一次用于put，一次用于get。

    然后通过调用内部的hash()API对这个值进行重构，以获得最终的哈希值。
    正如我们在上一节看到的，这个最终的哈希值最终归结为一个桶的位置或内部数组的索引。
    然后，存储在该位置的值对象被检索出来并返回给调用函数。

    当返回的值为空时，可能意味着键对象与哈希图中的任何值都没有关联：

    MapUnitTest.java givenUnmappedKey_whenGetReturnsNull_thenCorrect()

    或者它可能仅仅意味着键被明确地映射到一个空实例：

    MapUnitTest.java givenNullVal_whenRetrieves_thenCorrect()

    为了区分这两种情况，我们可以使用containsKey API，我们向其传递键，如果并且只有在哈希Map中为指定的键创建了映射，它才会返回true：

    MapUnitTest.java whenContainsDistinguishesNullValues_thenCorrect()

    对于上述测试中的两种情况，get API调用的返回值都是空的，但我们能够区分出哪种情况。
4. HashMap中的集合视图

    HashMap提供了三个视图，使我们能够把它的键和值当作另一个集合。我们可以获得Map的所有键的集合：

    MapUnitTest.java givenHashMap_whenRetrievesKeyset_thenCorrect()

    这个集合是由Map本身支持的。所以对集合所做的任何改变都会反映在Map上：

    MapUnitTest.java givenKeySet_whenChangeReflectsInMap_thenCorrect()

    我们还可以获得一个值的集合视图：

    MapUnitTest.java givenHashMap_whenRetrievesValues_thenCorrect()

    就像钥匙集一样，这个集合中的任何变化都会反映在底层Map中。
    最后，我们可以获得Map中所有条目的集合视图：

    MapUnitTest.java givenHashMap_whenRetrievesEntries_thenCorrect()

    请记住，哈希图特别包含无序的元素，因此我们在for each循环中测试条目的键和值时假设任何顺序。

    很多时候，你会像上一个例子那样在循环中使用集合视图，更确切地说，是使用其迭代器。
    只要记住，上述所有视图的迭代器都是快速失败的。
    如果在迭代器创建之后，在地图上做了任何结构性的修改，就会抛出一个并发的修改异常：

    ```java
    @Test(expected = ConcurrentModificationException.class)
    public void givenIterator_whenFailsFastOnModification_thenCorrect() 
    ```

    唯一允许的结构性修改是通过迭代器本身进行的删除操作：

     MapUnitTest.java givenIterator_whenRemoveWorks_thenCorrect()

    关于这些集合视图，需要记住的最后一点是迭代的性能。与其对应的链接哈希图和树状图相比，哈希图在这方面的表现相当差。
    散列图的迭代在最坏的情况下是O(n)，其中n是其容量和条目数之和。
5. 哈希图的性能

    散列图的性能受两个参数的影响： 初始容量和负载系数(Initial Capacity and Load Factor.)。容量是指桶的数量或底层数组的长度，初始容量只是创建时的容量。
    负载因子或LF，简而言之，是衡量哈希图在添加一些值后，在调整其大小之前应该有多满。

    默认的初始容量是16，默认的负载因子是0.75。我们可以用初始容量和LF的自定义值来创建一个哈希图：

    ```java
    Map<String,String> hashMapWithCapacity=new HashMap<>(32);
    Map<String,String> hashMapWithCapacityAndLF=new HashMap<>(32, 0.5f);
    ```

    Java团队设置的默认值对于大多数情况来说是很好的优化。但是，如果你需要使用自己的值，这也是非常可以的，你需要了解对性能的影响，这样你就知道自己在做什么。
    当哈希图条目的数量超过LF和容量的乘积时，就会发生重新洗牌，即创建另一个内部数组，其大小是初始数组的两倍，所有条目都被移到新数组的新桶位置。
    一个低的初始容量减少了空间成本，但增加了重洗的频率。重洗显然是一个非常昂贵的过程。因此，作为一条规则，如果你预计会有很多条目，你应该设置一个相当高的初始容量。
    反过来说，如果你把初始容量设置得太高，你将在迭代时间上付出代价。正如我们在上一节看到的那样。
    因此，一个高的初始容量适合于大量的条目，加上很少或没有迭代。
    低的初始容量适合于条目少而迭代次数多的情况。
6. HashMap中的碰撞

    碰撞，或者更具体地说，HashMap中的哈希代码碰撞，是指两个或更多的键对象产生相同的最终哈希值，从而指向同一个桶的位置或数组索引的情况。

    这种情况可能发生，因为根据equals和hashCode契约，Java中两个不相等的对象可以有相同的哈希代码。
    它也可能发生，因为底层数组的有限大小，也就是说，在调整大小之前。这个数组越小，碰撞的几率就越大。
    也就是说，值得一提的是，Java实现了一种哈希代码碰撞解决技术，我们将用一个例子来说明。
    请记住，是键的哈希值决定了该对象将被存储在哪个桶中。因此，如果任何两个键的哈希代码发生碰撞，它们的条目仍然会被存储在同一个桶中。
    而在默认情况下，该实现使用一个链接列表作为桶的实现。
    在发生碰撞的情况下，最初的恒定时间O(1)put和get操作将以线性时间O(n)发生。这是因为在找到具有最终哈希值的桶的位置后，这个位置上的每个键将使用等价API与提供的键对象进行比较。
    为了模拟这种碰撞解决技术，让我们稍微修改一下我们先前的密钥对象：

    ```java
    public class MyKey {
        ...
        public MyKey(int id, String name) {
            this.id = id;
            this.name = name;
        }
        // standard getters and setters
        ... 
        // toString override for pretty logging
        @Override
        public boolean equals(Object obj) {
            System.out.println("Calling equals() for key: " + obj);
            // generated implementation
        }
    }
    ```

    注意我们是如何简单地返回id属性作为哈希代码的--从而迫使碰撞发生。
    另外，请注意我们在equals和hashCode的实现中加入了日志语句--这样我们就能准确地知道逻辑被调用的时间。
    现在让我们继续存储和检索一些在某一时刻发生碰撞的对象：

    map/MapUnitTest.java whenCallsEqualsOnCollision_thenCorrect()

    在上面的测试中，我们创建了三个不同的键--一个有唯一的id，另外两个有相同的id。由于我们使用id作为初始哈希值，在用这些键存储和检索数据的过程中肯定会发生碰撞。
    除此之外，由于我们前面看到的碰撞解决技术，我们期望我们的每一个存储值都能被正确检索，因此在最后三行的断言。
    当我们运行测试时，它应该通过，表明碰撞得到解决，我们将使用产生的日志来确认碰撞确实发生了：

    ```log
    ...
    Calling equals() for key: MyKey [name=secondKey, id=2]
    ...
    Calling equals() for key: MyKey [name=secondKey, id=2]
    ```

    注意，在存储操作中，k1和k2被成功地映射到它们的值中，只使用了哈希代码。
    然而，k3的存储就没那么简单了，系统检测到其桶的位置已经包含了k2的映射。因此，使用等价比较来区分它们，并创建了一个链接列表来包含这两个映射。
    任何其他的后续映射，其键值哈希到相同的桶的位置，将遵循相同的路线，并最终取代链接列表中的一个节点，或者被添加到列表的头部，如果等价比较对所有现有的节点返回错误。
    同样，在检索过程中，k3和k2被等价比较，以确定其值应该被检索的正确键。

    最后，从Java 8开始，在碰撞解决中，当一个给定的桶的碰撞数量超过一定的阈值时，链接列表被动态地替换为平衡二进制搜索树。
    这一变化提供了性能上的提升，因为在发生碰撞的情况下，存储和检索发生在O(log n)。
    这一部分在技术面试中非常常见，特别是在基本的存储和检索问题之后。
7. 总结

    在这篇文章中，我们已经探讨了Java Map接口的HashMap实现。
