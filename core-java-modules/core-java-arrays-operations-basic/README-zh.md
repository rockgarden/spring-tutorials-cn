# Java数组-基本操作

本模块包含有关Java数组基础知识的文章。这些文章没有假定以前有与数组打交道的背景知识。

## 在Java中初始化数组

1. 概述

    在这个快速教程中，我们将研究初始化数组的不同方法，以及它们之间的微妙差别。

    [Java中的数组：参考指南](https://www.baeldung.com/java-arrays-guide)

    一个简单而完整的参考指南，用于理解和使用Java中的数组。

    [Java中的数组操作](https://www.baeldung.com/java-common-array-operations)

    了解我们如何在Java中处理常见的数组操作。

    [一条语句的Java列表初始化](https://www.baeldung.com/java-init-list-one-line)

    在这个快速教程中，我们将研究如何使用单行代码初始化一个列表。

2. 一次一个元素

    让我们从一个简单的、基于循环的方法开始：

    ```java
    for (int i = 0; i < array.length; i++) {
        array[i] = i + 2;
    }
    ```

    我们还将看到我们如何对一个多维数组进行一次元素的初始化：

    ```java
    for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 5; j++) {
            array[i][j] = j + 1;
        }
    }
    ```

3. 在声明的时候

    现在让我们在声明时初始化一个数组：

    `String array[] = new String[] { "Toyota", "Mercedes", "BMW", "Volkswagen", "Skoda" };`

    在实例化数组时，我们不必指定其类型：

    `int array[] = { 1, 2, 3, 4, 5 };`

    注意，使用这种方法不可能在声明后初始化数组；如果试图这样做，会导致编译错误。
4. 使用Arrays.fill()

    java.util.Arrays类有几个名为fill()的方法，它们接受不同类型的参数并以相同的值填充整个数组：

    ```java
    long array[] = new long[5];
    Arrays.fill(array, 30);
    ```

    该方法还有几个备选方案，它们将数组的范围设置为一个特定的值：

    ```java
    int array[] = new int[5];
    Arrays.fill(array, 0, 3, -50);
    ```

    注意，该方法接受数组，第一个元素的索引，元素的数量，以及值。
5. 使用Arrays.copyOf()

    Arrays.copyOf()方法通过复制另一个数组创建一个新数组。该方法有许多重载，接受不同类型的参数。
    让我们看一个简单的例子：

    ```java
    int array[] = { 1, 2, 3, 4, 5 };
    int[] copy = Arrays.copyOf(array, 5);
    ```

    这里有几个注意事项：

    - 该方法接受源数组和将要创建的副本的长度。
    - 如果长度大于要复制的数组的长度，那么额外的元素将使用其默认值被初始化。
    - 如果源数组没有被初始化，那么会抛出一个NullPointerException。
    - 最后，如果源数组的长度是负数，那么会抛出一个NegativeArraySizeException。

6. 使用Arrays.setAll()

    Arrays.setAll()方法使用生成器函数设置一个数组的所有元素：

    ```java
    int[] array = new int[20];
    Arrays.setAll(array, p -> p > 9 ? 0 : p);
    // [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    ```

    如果生成器函数为空，那么就会抛出一个NullPointerException。
7. 使用ArrayUtils.clone()

    最后，让我们利用Apache Commons Lang 3中的ArrayUtils.clone() API，它通过创建另一个数组的直接拷贝来初始化一个数组：

    ```java
    char[] array = new char[] {'a', 'b', 'c'};
    char[] copy = ArrayUtils.clone(array);
    ```

    注意，这个方法对所有原始类型都是重载的。
8. 总结

    在这篇简短的文章中，我们探讨了在Java中初始化数组的不同方法。

## Relevant Articles

- [x] [Initializing Arrays in Java](https://www.baeldung.com/java-initialize-array)
- [Array Operations in Java](https://www.baeldung.com/java-common-array-operations)
- [Adding an Element to a Java Array vs an ArrayList](https://www.baeldung.com/java-add-element-to-array-vs-list)
- [Check if a Java Array Contains a Value](https://www.baeldung.com/java-array-contains-value)
- [Removing an Element from an Array in Java](https://www.baeldung.com/java-array-remove-element)
- [Removing the First Element of an Array](https://www.baeldung.com/java-array-remove-first-element)
- [Extending an Array’s Length](https://www.baeldung.com/java-array-add-element-at-the-end)
- [Initializing a Boolean Array in Java](https://www.baeldung.com/java-initializing-boolean-array)
- [Find the Index of an Element in a Java Array](https://www.baeldung.com/java-array-find-index)
- [Comparing Two Byte Arrays in Java](https://www.baeldung.com/java-comparing-byte-arrays)

## Code

一如既往，完整版的代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-arrays-operations-basic)上找到。
