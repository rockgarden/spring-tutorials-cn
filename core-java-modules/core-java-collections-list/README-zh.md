# 核心Java集合列表

本模块包含关于Java List集合的文章

## 在Java中删除列表中的所有空值

这个快速教程将展示如何使用普通Java、Guava、Apache Commons Collections和较新的Java 8 lambda支持，从List中移除所有空元素。
这篇文章是Baeldung网站 "Java - Back to Basic "系列的一部分。

1. 使用普通Java从列表中删除空值

    Java集合框架为移除List中的所有空元素提供了一个简单的解决方案--一个基本的while循环：

    ```java
    @Test
    public void givenListContainsNulls_whenRemovingNullsWithPlainJava_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, null);
        while (list.remove(null));
        assertThat(list, hasSize(1));
    }
    ```

    另外，我们也可以使用以下简单的方法：

    ```java
    @Test
    public void givenListContainsNulls_whenRemovingNullsWithPlainJavaAlternative_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, null);
        list.removeAll(Collections.singleton(null));
        assertThat(list, hasSize(1));
    }
    ```

    注意，这两种解决方案都会修改源列表。
2. 使用Google Guava从列表中删除空值

    我们也可以使用Guava和一个更实用的方法，通过谓词来删除空值：

    ```java
    @Test
    public void givenListContainsNulls_whenRemovingNullsWithGuavaV1_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, null);
        Iterables.removeIf(list, Predicates.isNull());
        assertThat(list, hasSize(1));
    }
    ```

    另外，如果我们不想修改源列表，Guava将允许我们创建一个新的过滤列表：

    ```java
    @Test
    public void givenListContainsNulls_whenRemovingNullsWithGuavaV2_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, null, 2, 3);
        List<Integer> listWithoutNulls = Lists.newArrayList(
        Iterables.filter(list, Predicates.notNull()));
        assertThat(listWithoutNulls, hasSize(3));
    }
    ```

3. 使用Apache Commons集合从列表中删除空值

    现在让我们来看看使用Apache Commons集合库的一个简单解决方案，使用类似的函数式：

    ```java
    @Test
    public void givenListContainsNulls_whenRemovingNullsWithCommonsCollections_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, 2, null, 3, null);
        CollectionUtils.filter(list, PredicateUtils.notNullPredicate());
        assertThat(list, hasSize(3));
    }
    ```

    注意，这个方案也会修改原始列表。
4. 使用Lambdas从列表中删除空值（Java 8）

    最后--让我们看一下Java 8的解决方案，使用Lambdas来过滤List；过滤过程可以并行或串行进行：

    ```java
    @Test
    public void givenListContainsNulls_whenFilteringParallel_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, 2, null, 3, null);
        List<Integer> listWithoutNulls = list.parallelStream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    @Test
    public void givenListContainsNulls_whenFilteringSerial_thenCorrect() {
        List<Integer> list = Lists.newArrayList(null, 1, 2, null, 3, null);
        List<Integer> listWithoutNulls = list.stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    public void givenListContainsNulls_whenRemovingNullsWithRemoveIf_thenCorrect() {
        List<Integer> listWithoutNulls = Lists.newArrayList(null, 1, 2, null, 3, null);
        listWithoutNulls.removeIf(Objects::isNull);
        assertThat(listWithoutNulls, hasSize(3));
    }
    ```

    就是这样--一些快速且非常有用的解决方案，可以从列表中去除所有空元素。
5. 总结

    在这篇文章中，我们探索了使用Java、Guava或Lambdas从List中移除空元素的不同方法。

## 在Java中从列表中删除所有重复的内容

1. 简介

    在这个快速教程中，我们将学习如何清理List中的重复元素。首先，我们将使用普通的Java，然后是Guava，最后是一个基于Java 8 Lambda的解决方案。
    本教程是Baeldung网站 "Java - Back to Basic" 系列的一部分。
2. 使用普通Java删除列表中的重复元素

    我们可以用标准的Java集合框架通过Set轻松地从List中移除重复的元素：

    ```java
    public void 
    givenListContainsDuplicates_whenRemovingDuplicatesWithPlainJava_thenCorrect() {
        List<Integer> listWithDuplicates = Lists.newArrayList(5, 0, 3, 1, 2, 3, 0, 0);
        List<Integer> listWithoutDuplicates = new ArrayList<>(
        new HashSet<>(listWithDuplicates));
        assertThat(listWithoutDuplicates, hasSize(5));
        assertThat(listWithoutDuplicates, containsInAnyOrder(5, 0, 3, 1, 2));
    }
    ```

    正如我们所见，原始列表保持不变。
    在上面的例子中，我们使用了HashSet的实现，它是一个无序的集合。因此，清理后的 listWithoutDuplicates 的顺序可能与原始 listWithDuplicates 的顺序不同。
    如果我们需要保留顺序，我们可以使用LinkedHashSet代替：

    ```java
    public void 
    givenListContainsDuplicates_whenRemovingDuplicatesPreservingOrderWithPlainJava_thenCorrect() {
        List<Integer> listWithDuplicates = Lists.newArrayList(5, 0, 3, 1, 2, 3, 0, 0);
        List<Integer> listWithoutDuplicates = new ArrayList<>(
        new LinkedHashSet<>(listWithDuplicates));
        assertThat(listWithoutDuplicates, hasSize(5));
        assertThat(listWithoutDuplicates, containsInRelativeOrder(5, 0, 3, 1, 2));
    }
    ```

    进一步阅读：

    [Java集合面试题](https://www.baeldung.com/java-collections-interview-questions)

    一组实用的集合相关的Java面试问题

    [Java合并多个集合](https://www.baeldung.com/java-combine-multiple-collections)

    在Java中组合多个集合的快速实用指南

    [如何用Java查找列表中的一个元素](https://www.baeldung.com/find-list-element-java)

    看一看在Java中寻找列表中的元素的一些快速方法

3. 用Guava删除列表中的重复内容

    我们也可以用Guava做同样的事情：

    ```java
    public void 
    givenListContainsDuplicates_whenRemovingDuplicatesWithGuava_thenCorrect() {
        List<Integer> listWithDuplicates = Lists.newArrayList(5, 0, 3, 1, 2, 3, 0, 0);
        List<Integer> listWithoutDuplicates 
        = Lists.newArrayList(Sets.newHashSet(listWithDuplicates));
        assertThat(listWithoutDuplicates, hasSize(5));
        assertThat(listWithoutDuplicates, containsInAnyOrder(5, 0, 3, 1, 2));
    }
    ```

    在这里，原始列表也保持不变。
    同样，清理后的列表中元素的顺序可能是随机的。
    如果我们使用LinkedHashSet的实现，我们将保留初始的顺序：

    ```java
    public void 
    givenListContainsDuplicates_whenRemovingDuplicatesPreservingOrderWithGuava_thenCorrect() {
        List<Integer> listWithDuplicates = Lists.newArrayList(5, 0, 3, 1, 2, 3, 0, 0);
        List<Integer> listWithoutDuplicates 
        = Lists.newArrayList(Sets.newLinkedHashSet(listWithDuplicates));
        assertThat(listWithoutDuplicates, hasSize(5));
        assertThat(listWithoutDuplicates, containsInRelativeOrder(5, 0, 3, 1, 2));
    }
    ```

4. 使用Java 8的Lambdas从列表中删除重复的内容

    最后，让我们看看一个新的解决方案，使用Java 8中的Lambdas。我们将使用Stream API中的distinct()方法，它根据equals()方法返回的结果，返回一个由不同元素组成的流。
    此外，对于有序的流，独特元素的选择是稳定的。这意味着，对于重复的元素，在相遇顺序中首先出现的元素被保留：

    ```java
    public void 
    givenListContainsDuplicates_whenRemovingDuplicatesWithJava8_thenCorrect() {
        List<Integer> listWithDuplicates = Lists.newArrayList(5, 0, 3, 1, 2, 3, 0, 0);
        List<Integer> listWithoutDuplicates = listWithDuplicates.stream()
        .distinct()
        .collect(Collectors.toList());
        assertThat(listWithoutDuplicates, hasSize(5));
        assertThat(listWithoutDuplicates, containsInAnyOrder(5, 0, 3, 1, 2));
    }
    ```

    就这样，我们有了三种快速清理列表中所有重复项的方法。

5. 总结

在这篇文章中，我们演示了使用普通Java、Google Guava和Java 8从列表中删除重复项是多么容易。

## Relevant Articles

- [Java – Get Random Item/Element From a List](http://www.baeldung.com/java-random-list-element)
- [x] [Removing all Nulls from a List in Java](http://www.baeldung.com/java-remove-nulls-from-list)
- [x] [Removing all duplicates from a List in Java](http://www.baeldung.com/java-remove-duplicates-from-list)
- [How to TDD a List Implementation in Java](http://www.baeldung.com/java-test-driven-list)
- [Iterating Backward Through a List](http://www.baeldung.com/java-list-iterate-backwards)
- [Remove the First Element from a List](http://www.baeldung.com/java-remove-first-element-from-list)
- [How to Find an Element in a List with Java](http://www.baeldung.com/find-list-element-java)
- [Finding Max/Min of a List or Collection](http://www.baeldung.com/java-collection-min-max)
- [Remove All Occurrences of a Specific Value from a List](https://www.baeldung.com/java-remove-value-from-list)
- [[Next -->]](/core-java-modules/core-java-collections-list-2)

## Code

所有这些例子和片段的实现都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-collections-list)项目中找到。这是一个基于Maven的项目，所以它应该很容易导入和运行。
