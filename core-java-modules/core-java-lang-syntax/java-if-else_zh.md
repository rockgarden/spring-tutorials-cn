# [Java 中的 if-else 语句](https://www.baeldung.com/java-if-else)

1. 概述

    在本教程中，我们将学习如何在 Java 中使用 if-else 语句。

    if-else 语句是最基本的控制结构，也是编程中最常见的决策语句。

    它允许我们在满足特定条件时才执行某段代码。

2. if-else 的语法

    if 语句始终需要一个布尔表达式作为其条件参数。

    ```java
    if (condition) {
        // 当 condition 为 true 时执行
    } else {
        // 当 condition 为 false 时执行
    }
    ```

    if 语句后可以跟一个可选的 else 语句，当布尔表达式为 false 时，else 块中的代码将被执行。

3. if 语句示例

    我们先从一个非常基础的例子开始。

    假设我们只希望在 count 变量大于 1 时执行某些操作：

    ```java
    if (count > 1) {
        System.out.println("Count is higher than 1");
    }
    ```

    只有当条件成立时，才会打印出 “Count is higher than 1”。

    请注意，从技术上讲，如果代码块中只有一行语句，我们可以省略大括号。但为了提高代码可读性，即使只有一行，也应始终使用大括号。

    当然，我们也可以在代码块中添加更多语句：

    ```java
    if (count > 1) {
        System.out.println("Count is higher than 1");
        System.out.println("Count is equal to: " + count);
    }
    ```

4. if-else 语句示例

    接下来，我们可以将 if 和 else 结合使用，在两种操作之间进行选择：

    ```java
    if (count > 2) {
        System.out.println("Count is higher than 2");
    } else {
        System.out.println("Count is lower or equal than 2");
    }
    ```

    请注意，else 不能单独使用，它必须与 if 配对。

5. if-else if-else 语句示例

    最后，我们来看一个组合使用 if/else if/else 的例子。

    我们可以使用这种结构在三种或更多选项之间进行选择：

    ```java
    if (count > 2) {
        System.out.println("Count is higher than 2");
    } else if (count <= 0) {
        System.out.println("Count is less or equal than zero");
    } else {
        System.out.println("Count is either equal to one, or two");
    }
    ```

6. 总结

    在本篇简短的文章中，我们学习了 if-else 语句是什么，以及如何使用它来控制 Java 程序中的执行流程。
