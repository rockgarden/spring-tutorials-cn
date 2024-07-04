# 核心Java集合ArrayList

## Java ArrayList指南

1. 概述

    在这篇文章中，我们要看一下Java集合框架中的ArrayList类。我们将讨论它的属性、常见的使用情况，以及它的优点和缺点。
    ArrayList存在于Java核心库中，所以你不需要任何额外的库。为了使用它，只需添加以下导入语句：

    `import java.util.ArrayList;`

    List表示一个有序的数值序列，其中一些数值可能出现不止一次。
    ArrayList是建立在数组之上的List实现之一，它能够在你添加/删除元素时动态地增长和缩小。可以通过从零开始的索引轻松访问元素。这个实现有以下特性：

    - 随机访问需要O(1)时间
    - 添加元素需要摊销后的恒定时间O(1)
    - 插入/删除需要O(n)时间
    - 对于未排序的数组，搜索需要O(n)时间，对于排序的数组，搜索需要O(log n)时间。

2. 创建一个ArrayList

    ArrayList有几个构造函数，我们将在本节中介绍它们。
    首先，注意ArrayList是一个泛型类，所以你可以用任何你想要的类型来设置参数，编译器会确保，例如，你不能把Integer值放在Strings的集合中。另外，当你从一个集合中检索元素时，你不需要对它们进行转换。
    其次，使用通用接口List作为变量类型是一个很好的做法，因为它与特定的实现相分离。
    1. 默认的无机关构造函数

        ```java
        List<String> list = new ArrayList<>();
        assertTrue(list.isEmpty());
        ```

        我们只是创建一个空的ArrayList实例。
    2. 接受初始容量的构造函数

        `List<String> list = new ArrayList<>(20);`

        这里你指定了一个底层数组的初始长度。这可能有助于你在添加新项时避免不必要的大小调整。
    3. 接受集合的构造函数

        ```java
        Collection<Integer> numbers 
        = IntStream.range(0, 10).boxed().collect(toSet());
        List<Integer> list = new ArrayList<>(numbers);
        assertEquals(10, list.size());
        assertTrue(numbers.containsAll(list));
        ```

        注意，Collection实例的元素被用来填充底层数组。
3. 向ArrayList添加元素

    你可以在最后或特定位置插入一个元素：

    ```java
    List<Long> list = new ArrayList<>();
    list.add(1L);
    list.add(2L);
    list.add(1, 3L);
    assertThat(Arrays.asList(1L, 3L, 2L), equalTo(list));
    ```

    你也可以一次插入一个集合或几个元素：

    ```java
    List<Long> list = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
    LongStream.range(4, 10).boxed()
    .collect(collectingAndThen(toCollection(ArrayList::new), ys -> list.addAll(0, ys)));
    assertThat(Arrays.asList(4L, 5L, 6L, 7L, 8L, 9L, 1L, 2L, 3L), equalTo(list));
    ```

4. 遍历数组列表

    有两种类型的迭代器可用： Iterator和ListIterator。
    前者让你有机会在一个方向上遍历列表，而后者则允许你在两个方向上遍历它。
    这里我们只向你展示ListIterator：

    ```java
    List<Integer> list = new ArrayList<>(
    IntStream.range(0, 10).boxed().collect(toCollection(ArrayList::new))
    );
    ListIterator<Integer> it = list.listIterator(list.size());
    List<Integer> result = new ArrayList<>(list.size());
    while (it.hasPrevious()) {
        result.add(it.previous());
    }
    Collections.reverse(list);
    assertThat(result, equalTo(list));
    ```

    你也可以使用迭代器搜索、添加或删除元素。
5. 搜索数组列表

    我们将演示如何使用一个集合进行搜索：

    ```java
    List<String> list = LongStream.range(0, 16)
    .boxed()
    .map(Long::toHexString)
    .collect(toCollection(ArrayList::new));
    List<String> stringsToSearch = new ArrayList<>(list);
    stringsToSearch.addAll(list);
    ```

    1. 搜索一个未排序的列表

        为了找到一个元素，你可以使用 indexOf() 或 lastIndexOf() 方法。它们都接受一个对象并返回int值：

        ```java
        assertEquals(10, stringsToSearch.indexOf("a"));
        assertEquals(26, stringsToSearch.lastIndexOf("a"));
        ```

        如果你想找到满足一个谓词的所有元素，你可以使用 Java 8 Stream API（在这里阅读更多关于它的[信息](https://www.baeldung.com/java-8-streams)）使用 Predicate 这样来过滤集合：

        ```java
        Set<String> matchingStrings = new HashSet<>(Arrays.asList("a", "c", "9"));
        List<String> result = stringsToSearch
        .stream()
        .filter(matchingStrings::contains)
        .collect(toCollection(ArrayList::new));
        assertEquals(6, result.size());
        ```

        也可以使用一个for循环或一个迭代器：

        ```java
        Iterator<String> it = stringsToSearch.iterator();
        Set<String> matchingStrings = new HashSet<>(Arrays.asList("a", "c", "9"));
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            String s = it.next();
            if (matchingStrings.contains(s)) {
                result.add(s);
            }
        }
        ```

    2. 搜索一个已排序的列表

        如果你有一个排序的数组，那么你可以使用二进制搜索算法，这种算法比线性搜索更快：

        ```java
        List<String> copy = new ArrayList<>(stringsToSearch);
        Collections.sort(copy);
        int index = Collections.binarySearch(copy, "f");
        assertThat(index, not(equalTo(-1)));
        ```

        注意，如果没有找到一个元素，那么将返回-1。
6. 从ArrayList中删除元素

    为了移除一个元素，你应该找到它的索引，然后通过remove()方法进行移除。这个方法的重载版本，接受一个对象，搜索它并执行移除第一次出现的相等元素：

    ```java
    List<Integer> list = new ArrayList<>(
    IntStream.range(0, 10).boxed().collect(toCollection(ArrayList::new))
    );
    Collections.reverse(list);
    list.remove(0);
    assertThat(list.get(0), equalTo(8));
    list.remove(Integer.valueOf(0));
    assertFalse(list.contains(0));
    ```

    但在处理整数等盒式类型时要小心。为了删除一个特定的元素，你应该首先框定int值，否则，一个元素将被按其索引删除。
    你也可以使用前面提到的Stream API来删除几个项目，但我们不会在这里展示它。为了这个目的，我们将使用一个迭代器：

    ```java
    Set<String> matchingStrings
    = HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f"));
    Iterator<String> it = stringsToSearch.iterator();
    while (it.hasNext()) {
        if (matchingStrings.contains(it.next())) {
            it.remove();
        }
    }
    ```

7. 总结

    在这篇快速文章中，我们看了一下Java中的ArrayList。
    我们展示了如何创建ArrayList实例，如何使用不同的方法来添加、查找或删除元素。

## Relevant Articles

- [x] [Guide to the Java ArrayList](https://www.baeldung.com/java-arraylist)
- [Add Multiple Items to an Java ArrayList](https://www.baeldung.com/java-add-items-array-list)
- [ClassCastException: Arrays$ArrayList cannot be cast to ArrayList](https://www.baeldung.com/java-classcastexception-arrays-arraylist)
- [Multi Dimensional ArrayList in Java](https://www.baeldung.com/java-multi-dimensional-arraylist)
- [Removing an Element From an ArrayList](https://www.baeldung.com/java-arraylist-remove-element)
- [The Capacity of an ArrayList vs the Size of an Array in Java](https://www.baeldung.com/java-list-capacity-array-size)
- [Case-Insensitive Searching in ArrayList](https://www.baeldung.com/java-arraylist-case-insensitive-search)
- [Storing Data Triple in a List in Java](https://www.baeldung.com/java-list-storing-triple)
- [Convert an ArrayList of Object to an ArrayList of String Elements](https://www.baeldung.com/java-object-list-to-strings)
- [Initialize an ArrayList with Zeroes or Null in Java](https://www.baeldung.com/java-arraylist-with-zeroes-or-null)

## Code

像往常一样，你可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-collections-array-list)上找到所有的代码样本。
