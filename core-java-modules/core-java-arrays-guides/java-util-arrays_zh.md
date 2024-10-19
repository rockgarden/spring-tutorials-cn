# [java.util.Arrays 类指南](https://www.baeldung.com/java-util-arrays)

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
