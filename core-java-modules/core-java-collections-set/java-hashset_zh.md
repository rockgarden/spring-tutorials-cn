# [Java中的哈希集指南](http://www.baeldung.com/java-hashset)

[Java Collections](https://www.baeldung.com/category/java/java-collections)

[Java Set](https://www.baeldung.com/tag/java-set)

1. 概述

    在这篇文章中，我们将深入探讨HashSet。它是最流行的Set实现之一，也是Java集合框架的一个组成部分。
2. 哈希集的介绍

    HashSet是Java集合API中的一个基本数据结构。
    让我们回顾一下这个实现的最重要的方面：

    - 它存储唯一的元素并允许空值
    - 它是由HashMap支持的
    - 它不维护插入的顺序
    - 它不是线程安全的
    请注意，当创建HashSet的一个实例时，这个内部HashMap会被初始化：

    ```java
    public HashSet() {
        map = new HashMap<>();
    }
    ```

    如果你想更深入地了解HashMap的工作原理，你可以在这里阅读专注于它的[文章](https://www.baeldung.com/java-hashmap)。
3. API

    在这一节中，我们将回顾一下最常用的方法，并看一下一些简单的例子。
    1. add()

        add()方法可以用来向一个集合添加元素。该方法的契约规定，只有当一个元素还没有出现在一个集合中时才会被添加。如果一个元素被添加，该方法返回true，否则返回false。
        我们可以将一个元素添加到HashSet中，例如：

        ```java
        @Test
        public void whenAddingElement_shouldAddElement() {
            Set<String> hashset = new HashSet<>(); 
            assertTrue(hashset.add("String Added"));
        }
        ```

        从实现的角度来看，add方法是一个极其重要的方法。实现细节说明了HashSet如何在内部工作，并利用HashMap的put方法：

        ```java
        public boolean add(E e) {
            return map.put(e, PRESENT) == null;
        }
        ```

        map变量是对内部的、支持的HashMap的引用：

        `private transient HashMap<E, Object> map;`

        最好先熟悉一下[哈希代码](https://www.baeldung.com/java-hashcode)，以便详细了解基于哈希的数据结构中的元素是如何组织的。
        总结一下：

        - HashMap是一个默认容量为16个元素的桶的数组--每个bucket对应一个不同的哈希代码值
        - 如果各种对象具有相同的哈希码值，它们就会被存储在一个桶中
        - 如果达到了负载系数，就会创建一个新的数组，其大小是前一个数组的两倍，所有的元素都会被重新洗牌并重新分配到新的相应的bucket中。
        - 为了检索一个值，我们对一个键进行哈希处理，对其进行修改，然后进入相应的桶，在有多个对象的情况下，在潜在的链接列表中搜索。
    2. contains()

        contains方法的目的是检查一个元素是否存在于一个给定的HashSet中。如果找到该元素，它返回true，否则返回false。
        我们可以在HashSet中检查一个元素：

        ```java
        @Test
        public void whenCheckingForElement_shouldSearchForElement() {
            Set<String> hashsetContains = new HashSet<>();
            hashsetContains.add("String Added");
            assertTrue(hashsetContains.contains("String Added"));
        }
        ```

        每当一个对象被传递到这个方法，哈希值就会被计算出来。然后，相应的桶的位置被解析和遍历。
    3. remove()

        该方法将指定的元素从集合中移除，如果它存在的话。如果一个集合包含指定的元素，该方法返回true。
        让我们看看一个工作实例：

        ```java
        @Test
        public void whenRemovingElement_shouldRemoveElement() {
            Set<String> removeFromHashSet = new HashSet<>();
            removeFromHashSet.add("String Added");
            assertTrue(removeFromHashSet.remove("String Added"));
        }
        ```

    4. clear()

        当我们打算从一个集合中移除所有的项目时，我们使用这个方法。底层实现只是简单地清除了底层HashMap中的所有元素。
        让我们来看看这个方法的实际应用：

        ```java
        @Test
        public void whenClearingHashSet_shouldClearHashSet() {
            Set<String> clearHashSet = new HashSet<>();
            clearHashSet.add("String Added");
            clearHashSet.clear(); 
            assertTrue(clearHashSet.isEmpty());
        }
        ```

    5. size()

        这是API中的一个基本方法。它被大量使用，因为它有助于识别HashSet中存在的元素数量。底层实现只是将计算委托给HashMap的size()方法。
        让我们来看看这个方法的作用：

        ```java
        @Test
        public void whenCheckingTheSizeOfHashSet_shouldReturnThesize() {
            Set<String> hashSetSize = new HashSet<>();
            hashSetSize.add("String Added");  
            assertEquals(1, hashSetSize.size());
        }
        ```

    6. isEmpty()

        我们可以使用这个方法来计算HashSet的一个给定实例是否为空。如果该集合不包含任何元素，该方法返回true：

        ```java
        @Test
        public void whenCheckingForEmptyHashSet_shouldCheckForEmpty() {
            Set<String> emptyHashSet = new HashSet<>();
            assertTrue(emptyHashSet.isEmpty());
        }
        ```

    7. iterator()

        该方法返回Set中元素的一个迭代器。这些元素的访问没有特定的顺序，而且迭代器是快速失效的。
        我们可以在这里观察随机的迭代顺序：

        ```java
        @Test
        public void whenIteratingHashSet_shouldIterateHashSet() {
            Set<String> hashset = new HashSet<>();
            hashset.add("First");
            hashset.add("Second");
            hashset.add("Third");
            Iterator<String> itr = hashset.iterator();
            while(itr.hasNext()){
                System.out.println(itr.next());
            }
        }
        ```

        如果在迭代器创建后的任何时候，除了通过迭代器自己的移除方法外，集合被修改，迭代器会抛出一个ConcurrentModificationException。
        让我们看看这个动作：

        ```java
        @Test(expected = ConcurrentModificationException.class)
        public void whenModifyingHashSetWhileIterating_shouldThrowException() {
            Set<String> hashset = new HashSet<>();
            hashset.add("First");
            hashset.add("Second");
            hashset.add("Third");
            Iterator<String> itr = hashset.iterator();
            while (itr.hasNext()) {
                itr.next();
                hashset.remove("Second");
            }
        }
        ```

        另外，如果我们使用迭代器的移除方法，那么我们就不会遇到这个异常：

        ```java
        @Test
        public void whenRemovingElementUsingIterator_shouldRemoveElement() {
            Set<String> hashset = new HashSet<>();
            hashset.add("First");
            hashset.add("Second");
            hashset.add("Third");
            Iterator<String> itr = hashset.iterator();
            while (itr.hasNext()) {
                String element = itr.next();
                if (element.equals("Second"))
                    itr.remove();
            }
            assertEquals(2, hashset.size());
        }
        ```

        迭代器的故障快速行为不能被保证，因为在非同步并发修改的情况下，不可能做出任何硬性保证。
        失败快速迭代器在尽力的基础上抛出 ConcurrentModificationException。因此，写一个依靠这个异常来保证其正确性的程序是错误的。
4. HashSet是如何保持唯一性的？

    当我们把一个对象放入HashSet时，它使用对象的哈希码值来确定一个元素是否已经在集合中。
    每个哈希码值都对应于某个bucket的位置，其中可以包含各种元素，对于这些元素，计算出来的哈希值是一样的。但是两个具有相同hashCode的对象可能不相等。
    所以，同一bucket内的对象将使用equals()方法进行比较。
5. 哈希集的性能

    HashSet的性能主要受两个参数影响--它的初始容量和负载因子。
    向一个集合添加一个元素的预期时间复杂度是O(1)，在最坏的情况下（只有一个桶存在）可以下降到O(n) - 因此，保持正确的HashSet的容量是至关重要的。
    一个重要的说明：从JDK 8开始，[最坏情况下的时间复杂性是O(log^*^n)](http://openjdk.java.net/jeps/180)。
    负载因子描述了什么是最大的填充水平，超过这个水平，一个集合就需要调整大小。
    我们也可以用初始容量和负载因子的自定义值来创建一个HashSet：

    ```java
    Set<String> hashset = new HashSet<>();
    Set<String> hashset = new HashSet<>(20);
    Set<String> hashset = new HashSet<>(20, 0.5f);
    ```

    在第一种情况下，使用默认值--初始容量为16，负载系数为0.75。在第二种情况下，我们覆盖了默认容量，在第三种情况下，我们覆盖了这两个值。
    一个低的初始容量减少了空间的复杂性，但增加了重洗的频率，这是一个昂贵的过程。
    另一方面，高的初始容量增加了迭代的成本和初始内存消耗。

    作为一个经验法则：

    - 一个高的初始容量适合于大量的条目，并且很少或没有迭代。
    - 低初始容量适合于条目少而迭代多的情况。

    因此，在这两者之间取得正确的平衡是非常重要的。通常情况下，默认的实现是经过优化的，并且工作得很好，如果我们觉得有必要调整这些参数以适应要求，我们需要谨慎地进行调整。
6. 结语

    在这篇文章中，我们概述了HashSet的效用，它的目的以及它的基础工作。我们看到了它在可用性方面的效率，因为它具有恒定的时间性能和避免重复的能力。
    我们研究了API中的一些重要方法，它们是如何帮助我们这些开发者使用HashSet来发挥其潜力的。
