# Core Java Arrays - Guides

本模块包含关于Java中数组的完整指南

## Java中的数组参考指南

1. 简介

    在本教程中，我们将深入研究Java语言中的一个核心概念--数组。
    我们首先会看到什么是数组，然后是如何使用它们；总的来说，我们会涵盖如何：

    - 开始使用数组
    - 读取和写入数组元素
    - 循环使用数组
    - 将数组转化为其他对象，如List或Streams
    - 排序、搜索和组合数组

2. 什么是数组？

    首先，我们需要定义什么是数组？根据[Java文档](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html)，数组是一个包含固定数量的相同类型的值的对象。数组的元素是有索引的，这意味着我们可以用数字（称为索引）来访问它们。
    我们可以把数组看作是一个有编号的单元格列表，每个单元格都是一个持有一个值的变量。在Java中，编号从0开始。
    有原始类型的数组和对象类型的数组。这意味着我们可以使用int、float、boolean......的数组，但也可以使用String、Object和自定义类型的数组。
3. 设置一个数组

    现在数组已经定义好了，让我们深入了解它们的用途。
    我们将涵盖很多教我们如何使用数组的主题。我们将学习一些基础知识，如如何声明和初始化数组，但我们也会涉及更高级的主题，如排序和搜索数组。
    让我们先来看看声明和初始化。
    1. 声明

        我们将从声明开始。在Java中，有两种声明数组的方法：
        `int[] anArray;`
        或：
        `int anOtherArray[];`
        前者比后者使用得更广泛。
    2. 初始化

        现在，是时候看看如何初始化数组了。同样有多种方法来初始化数组。我们将在这里看到主要的几种，但是这篇[文章](https://www.baeldung.com/java-initialize-array)详细介绍了数组的初始化。
        让我们从一个简单的方法开始：
        `int[] anArray = new int[10];`

        通过使用这个方法，我们初始化了一个包含10个int元素的数组。注意，我们需要指定数组的大小。
        当使用这个方法时，我们将每个元素初始化为默认值，这里是0。 当初始化Object的数组时，元素默认为空。
        现在我们将看到另一种方法，在创建数组时，我们有可能直接给数组设置值：
        `int[] anArray = new int[] {1, 2, 3, 4, 5};`

        这里，我们初始化了一个包含数字1到5的五元数组。当使用这种方法时，我们不需要指定数组的长度，它是元素的数量，然后在大括号之间声明。
4. 访问元素

    现在让我们看看如何访问一个数组的元素。我们可以通过要求一个数组单元的位置来实现这个目的。
    例如，这个小代码段将打印10到控制台：

    ```java
    anArray[0] = 10;
    System.out.println(anArray[0]);
    ```

    注意我们是如何使用索引来访问数组单元的。括号内的数字是我们要访问的数组的具体位置。
    当访问一个单元时，如果传递的索引是负数或者超过了最后一个单元，Java会抛出一个ArrayIndexOutOfBoundException。

    那么我们应该注意不要使用负的索引，或大于或等于数组大小的索引。
5. 遍历数组

    一个一个地访问元素可能很有用，但是我们可能想在一个数组中进行迭代。让我们看看如何实现这一点。
    第一种方法是使用for循环：

    ```java
    int[] anArray = new int[] {1, 2, 3, 4, 5};
    for (int i = 0; i < anArray.length; i++) {
        System.out.println(anArray[i]);
    }
    ```

    这应该会把数字1到5打印到控制台。正如我们所看到的，我们利用了length属性。这是一个公共属性，给了我们数组的大小。
    当然，我们也可以使用其他的循环机制，如while或do while。但是，对于Java集合来说，可以使用foreach循环来循环数组：

    ```java
    int[] anArray = new int[] {1, 2, 3, 4, 5};
    for (int element : anArray) {
        System.out.println(element);
    }
    ```

    这个例子等同于前面的例子，但我们去掉了指数的模板代码。foreach循环在以下情况下是一种选择：

    - 我们不需要修改数组（在一个元素中放入另一个值，不会修改数组中的元素）
    - 我们不需要索引来做其他事情

6. 变量

    当涉及到数组的创建和操作时，我们已经涵盖了基础知识。现在，我们将从varargs开始，深入探讨更高级的话题。作为提醒，varargs是用来向一个方法传递任意数量的参数的：

    `void varargsMethod(String... varargs) {}`

    这个方法可以接受从0到任意数量的String参数。一篇关于varargs的文章可以在[这里](https://www.baeldung.com/java-varargs)找到。
    在这里我们要知道的是，在方法主体里面，varargs参数变成了一个数组。但是，我们也可以直接传递一个数组作为参数。让我们通过重用上面声明的方法实例来看看如何做：

    ```java
    String[] anArray = new String[] {"Milk", "Tomato", "Chips"};
    varargsMethod(anArray);
    ```

    将表现得和以下一样

    `varargsMethod("Milk", "Tomato", "Chips");`

7. 将数组转换为列表

    数组是很好的，但有时用List来处理会更方便。在这里我们将看到如何将一个数组转化为一个列表。
    我们首先要做的是，通过创建一个空的列表，然后在数组上迭代，将其元素添加到列表中：

    ```java
    int[] anArray = new int[] {1, 2, 3, 4, 5};
    List<Integer> aList = new ArrayList<>();
    for (int element : anArray) {
        aList.add(element);
    }
    ```

    但还有另一种方法，更简洁一点：

    ```java
    Integer[] anArray = new Integer[] {1, 2, 3, 4, 5};
    List<Integer> aList = Arrays.asList(anArray);
    ```

    静态方法Arrays.asList接受一个varargs参数，并以传递的值创建一个列表。不幸的是，这个方法有一些缺点：

    - 不可能使用原始类型的数组
    - 我们不能从创建的列表中添加或删除元素，因为它会抛出一个UnsupportedOperationException。

8. 从一个数组到一个流

    我们现在可以将数组转化为列表，但从Java 8开始，我们可以访问[Stream API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html#stream(double%5B%5D))，我们可能想将我们的数组转化为Stream。为此，Java为我们提供了Arrays.stream方法：

    ```java
    String[] anArray = new String[] {"Milk", "Tomato", "Chips"};
    Stream<String> aStream = Arrays.stream(anArray);
    ```

    当向该方法传递一个对象数组时，它将返回一个匹配类型的Stream（例如，`Stream<Integer>`用于一个Integer数组）。当传递一个原始数组时，它将返回相应的原始流。
    也可以只在数组的一个子集上创建流：

    `Stream<String> anotherStream = Arrays.stream(anArray, 1, 3);`

    这将创建一个只有 "Tomato"和 "Chips"字符串的`Stream<String>`（第一个索引是包容的，而第二个是排他的）。
9. 对数组进行排序

    现在让我们看看如何对数组进行排序，也就是将其元素按照一定的顺序重新排列。Arrays类为我们提供了[排序](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html#sort(byte%5B%5D))方法。有点像stream方法，sort有很多重载。
    有一些重载可以用来排序：

    - 原始类型的数组：按升序进行排序
    - 对象数组（这些对象必须实现Comparable接口）：按照自然顺序排序（依靠Comparable的compareTo方法）。
    - 通用数组：根据一个给定的比较器进行排序。

    此外，还可以只对数组的特定部分进行排序（向方法传递开始和结束索引）。
    排序方法背后的算法是快速排序和合并排序，分别用于原始数组和其他数组。
    让我们通过一些例子来看看这一切是如何工作的：

    ```java
    int[] anArray = new int[] {5, 2, 1, 4, 8};
    Arrays.sort(anArray); // anArray is now {1, 2, 4, 5, 8}
    Integer[] anotherArray = new Integer[] {5, 2, 1, 4, 8};
    Arrays.sort(anotherArray); // anotherArray is now {1, 2, 4, 5, 8}
    String[] yetAnotherArray = new String[] {"A", "E", "Z", "B", "C"};
    Arrays.sort(yetAnotherArray, 1, 3, 
        Comparator.comparing(String::toString).reversed()); // yetAnotherArray is now {"A", "Z", "E", "B", "C"}
    ```

10. 在数组中搜索

    搜索一个数组是非常简单的，我们可以在数组上循环，在数组元素中搜索我们的元素：

    ```java
    int[] anArray = new int[] {5, 2, 1, 4, 8};
    for (int i = 0; i < anArray.length; i++) {
        if (anArray[i] == 4) {
            System.out.println("Found at index " + i);
            break;
        }
    }
    ```

    这里我们搜索了数字4，并在索引3处找到了它。
    如果我们有一个排序的数组，我们可以使用另一种解决方案：二进制搜索。二进制搜索的原理在这篇[文章](https://www.baeldung.com/java-binary-search)中已经解释过了。
    幸运的是，Java为我们提供了Arrays.binarySearch方法。我们必须给它一个数组和一个要搜索的元素。
    如果是一个普通的数组，我们还必须给它一个比较器，这个比较器首先是用来对数组进行排序的。也有可能在数组的一个子集上调用这个方法。
    让我们看一个二进制搜索方法的使用例子：

    ```java
    int[] anArray = new int[] {1, 2, 3, 4, 5};
    int index = Arrays.binarySearch(anArray, 4);
    System.out.println("Found at index " + index);
    ```

    由于我们在第四个单元格中存储了数字4，这将返回索引3作为结果。注意，我们使用了一个已经排序的数组。
11. 连接数组

    最后，让我们看看如何串联两个数组。我们的想法是创建一个数组，其长度为要连接的两个数组之和。然后，我们必须把第一个数组的元素加上去，然后再加上第二个数组的元素：

    ```java
    int[] anArray = new int[] {5, 2, 1, 4, 8};
    int[] anotherArray = new int[] {10, 4, 9, 11, 2};
    int[] resultArray = new int[anArray.length + anotherArray.length];
    for (int i = 0; i < resultArray.length; i++) {
        resultArray[i] = (i < anArray.length ? anArray[i] : anotherArray[i - anArray.length]);
    }
    ```

    我们可以看到，当索引仍然小于第一个数组的长度时，我们从该数组中添加元素。然后我们再从第二个数组中添加元素。我们可以利用Arrays.setAll方法来避免写一个循环：

    ```java
    int[] anArray = new int[] {5, 2, 1, 4, 8};
    int[] anotherArray = new int[] {10, 4, 9, 11, 2};
    int[] resultArray = new int[anArray.length + anotherArray.length];
    Arrays.setAll(resultArray, i -> (i < anArray.length ? anArray[i] : anotherArray[i - anArray.length]));
    ```

    这个方法将根据给定的函数来设置所有的数组元素。这个函数将一个索引与一个结果联系起来。
    这里有第三个选项，可以合并到数组中： System.arraycopy。这个方法需要一个源数组，一个源位置，一个目标数组，一个目标位置和一个定义了要复制的元素数量的int：

    ```java
    System.arraycopy(anArray, 0, resultArray, 0, anArray.length);
    System.arraycopy(anotherArray, 0, resultArray, anArray.length, anotherArray.length);
    ```

    我们可以看到，我们复制了第一个数组，然后是第二个数组（在第一个数组的最后一个元素之后）。
12. 总结

    在这篇详细的文章中，我们已经介绍了Java中数组的基本和一些高级用法。
    我们看到，Java通过[Arrays实用类](https://www.baeldung.com/java-util-arrays)提供了很多处理数组的方法。在[Apache Commons](https://www.baeldung.com/apache-commons-collection-utils)或[Guava](https://www.baeldung.com/guava-collections)等库中也有操作数组的实用类。

## java.util.Arrays 类指南

1. 介绍

    在本教程中，我们将了解 java.util.Arrays 这个从 Java 1.2 开始就属于 Java 的实用程序类。

    使用 Arrays，我们可以创建、比较、排序、搜索、流式传输和转换数组。

2. 创建数组

    让我们来看看创建数组的几种方法：copyOf、copyOfRange 和 fill。

    1. 复制和复制范围

        要使用 copyOfRange，我们需要原始数组以及要复制的开始索引（包含）和结束索引（不包含）：

        ```java
        String[] intro = new String[] { "once", "upon", "a", "time" };
        String[] abridgement = Arrays.copyOfRange(storyIntro, 0, 3); 
        assertArrayEquals(new String[] { "once", "upon", "a" }, abridgement); 
        assertFalse(Arrays.equals(intro, abridgement));
        ```

        要使用 copyOf，我们需要引入一个目标数组大小，然后返回一个长度与之相当的新数组：

        ```java
        String[] revised = Arrays.copyOf(intro, 3);
        String[] expanded = Arrays.copyOf(intro, 5);

        assertArrayEquals(Arrays.copyOfRange(intro, 0, 3), revised);
        assertNull(expanded[4]);
        ```

        注意，如果我们的目标大小大于原始大小，copyOf 会在数组中填充空值。

    2. 填充

        另一种创建定长数组的方法是 fill，当我们需要一个所有元素都相同的数组时，它非常有用：

        ```java
        String[] stutter = new String[3];
        Arrays.fill(stutter, "once");

        assertTrue(Stream.of(stutter)
        .allMatch(el -> "once".equals(el));
        ```

        查看 setAll，创建元素不同的数组。

        请注意，我们需要事先自己实例化数组，而不是像 String[] filled = Arrays.fill("once", 3);- 因为此功能是在语言中使用泛型之前引入的。

3. 比较

    现在我们来看看比较数组的方法。

    代码参见：arrays/ArraysUnitTest.java

    1. equals 和 deepEquals

        我们可以使用 equals 进行简单的数组大小和内容比较。 如果我们将空值添加为元素之一，则内容检查失败：

        whenEqualsContent_thenMatch()

        当我们有嵌套数组或多维数组时，我们可以使用 deepEquals 不仅检查顶层元素，而且执行递归检查：

        whenNestedArrays_thenDeepEqualsPass()

        请注意，deepEquals 通过了，而 equals 却失败了。

        这是因为每次遇到数组时，deepEquals 最终都会调用自己（子数组对象），而 equals 只是比较**子数组的引用**。

        此外，这也使得调用具有自引用的数组变得非常危险！

    2. hashCode 和 deepHashCode

        hashCode 的实现为我们提供了equals/hashCode 契约的另一部分，建议用于 Java 对象。 我们使用 hashCode 根据数组内容计算整数：

        ```java
        Object[] looping = new Object[]{ intro, intro };
        int hashBefore = Arrays.hashCode(looping);
        int deepHashBefore = Arrays.deepHashCode(looping);
        ```

        现在，我们将原始数组中的一个元素设置为 null，然后重新计算哈希值：

        ```java
        intro[3] = null;
        int hashAfter = Arrays.hashCode(looping);
        ```

        另外，deepHashCode 会检查嵌套数组的元素数和内容是否匹配。 如果我们使用 deepHashCode 重新计算：

        `int deepHashAfter = Arrays.deepHashCode(looping);`

        现在，我们可以看到这两种方法的区别了：

        ```java
        assertEquals(hashAfter, hashBefore);
        assertNotEquals(deepHashAfter, deepHashBefore);
        ```

        deepHashCode 是我们在数组上处理 HashMap 和 HashSet 等数据结构时使用的底层计算方法。

        参见 whenNestedArrayNullElement_thenEqualsFailDeepHashPass()

4. 排序和搜索

    接下来，让我们看看数组的排序和搜索。

    1. 排序

        如果我们的元素是基元或实现了 Comparable，我们就可以使用排序来执行内联排序：

        whenSort_thenArraySorted()

        需要注意的是，sort 会改变原始引用，这就是我们在这里执行复制的原因。

        对于不同的数组元素类型，sort 会使用不同的算法。[原始类型使用双支点 quicksort 算法](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html#sort(byte%5B%5D))，而[对象类型使用 Timsort 算法](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html#sort(java.lang.Object%5B%5D))。对于随机排序的数组，两者的平均值都是 $O(n log(n))$。

        从 Java 8 开始，parallelSort 可用于并行排序合并。 它提供了一种使用多个 Arrays.sort 任务的并发排序方法。

    2. 二进制搜索

        在未排序的数组中搜索是线性的，但如果我们有一个已排序的数组，那么我们可以在 $O(log n)$ 的时间内完成搜索，这就是二进制搜索（binarySearch）所能做到的：

        whenBinarySearch_thenFindElements()

        如果我们不提供一个比较器作为第三个参数，那么 binarySearch 将依赖于我们的元素类型是 Comparable 类型。

        同样要注意的是，如果我们的数组没有首先排序，那么 binarySearch 就不会像我们期望的那样工作！

5. 流

    如前所述，数组在 Java 8 中进行了更新，包含了使用流 API 的方法，如 parallelSort（如上所述）、stream 和 setAll。

    1. 流

        通过 stream，我们可以完全访问数组的流 API：

        whenStreamBadIndex_thenException()

        我们可以为数据流提供包容性和排他性索引，但如果索引失序、为负数或超出范围，我们就应该想到 ArrayIndexOutOfBoundsException。

6. 转换

    最后，toString、asList 和 setAll 为我们提供了几种转换数组的不同方法。

    1. toString 和 deepToString

        使用 toString 可以获得原始数组的可读版本：

        whenToString_thenFormattedArrayString()

        同样，我们必须使用深层版本来打印嵌套数组的内容：

        whenNestedArrayDeepString_thenFormattedArraysString()

    2. asList

        在所有数组方法中，最方便我们使用的是 asList。我们可以用一种简单的方法将数组转换为列表：

        whenAsList_thenImmutableArray()

        然而，返回的 List 将是固定长度，因此我们无法添加或删除元素。

        还要注意的是，奇怪的是，java.util.Arrays 有自己的 ArrayList 子类，asList 也会返回它。这在调试时可能很有欺骗性！

    3. setAll

        通过 setAll，我们可以使用功能接口设置数组的所有元素。生成器的实现将位置索引作为参数：

        whenSetAllToUpper_thenAppliedToAllElements()

        当然，异常处理也是使用 lambdas 时比较棘手的部分之一。所以请记住，在这里，如果 lambda 抛出异常，那么 Java 不会定义数组的最终状态。

7. 并行前缀

    Java 8 引入的另一个数组新方法是 parallelPrefix。使用 parallelPrefix，我们可以对输入数组的每个元素进行累加操作。

    1. 平行前缀

        如果操作符像下面的示例一样执行加法运算，[1, 2, 3, 4] 的结果将是 [1, 3, 6, 10]：

        ```java
        int[] arr = new int[] { 1, 2, 3, 4};
        Arrays.parallelPrefix(arr, (left, right) -> left + right);
        assertThat(arr, is(new int[] { 1, 3, 6, 10}));
        ```

        此外，我们还可以为操作指定一个子范围：

        whenPrefixAddWithRange_thenRangeAdded()

        请注意，该方法是并行执行的，因此累积操作应该是无副作用和[关联的](https://en.wikipedia.org/wiki/Associative_property)。

        对于非关联函数

        nonassociativeFunc()

        使用 parallelPrefix 会产生不一致的结果：

        whenPrefixNonAssociative_thenError()

    2. 性能

        并行前缀计算通常比顺序循环更高效，尤其是对于大型阵列。在使用 JMH 的英特尔至强计算机（6 核）上运行微基准测试时，我们可以看到性能有了很大提高：

        ```log
        Benchmark                      Mode        Cnt       Score   Error        Units
        largeArrayLoopSum             thrpt         5        9.428 ± 0.075        ops/s
        largeArrayParallelPrefixSum   thrpt         5       15.235 ± 0.075        ops/s

        Benchmark                     Mode         Cnt       Score   Error        Units
        largeArrayLoopSum             avgt          5      105.825 ± 0.846        ops/s
        largeArrayParallelPrefixSum   avgt          5       65.676 ± 0.828        ops/s
        ```

        以下是基准代码：

        jmh.infra.Blackhole/ParallelPrefixBenchmark.java

        - [ ] How to run?

8. 结论

    在本文中，我们学习了如何使用 java.util.Arrays 类创建、搜索、排序和转换数组。

    该类在最近发布的 Java 版本中得到了扩展，在 [Java 8](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html) 中包含了流生成和消耗方法，在 [Java 9](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html) 中包含了mismatch方法。

## Relevant Articles

- [x] [Arrays in Java: A Reference Guide](https://www.baeldung.com/java-arrays-guide)
- [x] [Guide to the java.util.Arrays Class](https://www.baeldung.com/java-util-arrays)
- [ ] [What is Ljava.lang.Object;?](https://www.baeldung.com/java-tostring-array)
- [ ] [Guide to ArrayStoreException](https://www.baeldung.com/java-arraystoreexception)
- [ ] [Creating a Generic Array in Java](https://www.baeldung.com/java-generic-array)
- [ ] [Maximum Size of Java Arrays](https://www.baeldung.com/java-arrays-max-size)

## Code

这篇文章的完整代码可以在我们的[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-arrays-guides)上找到。
