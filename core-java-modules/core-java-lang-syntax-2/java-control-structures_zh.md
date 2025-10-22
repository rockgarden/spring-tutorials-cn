# [Java中的控制结构](https://www.baeldung.com/java-control-structures)

1. 概述

    在最基本的意义上，一个程序是一个指令的列表。控制结构是可以改变我们通过这些指令的路径的编程块。

    在本教程中，我们将探讨Java中的控制结构。

    有三种控制结构。

    - 条件性分支，我们用它来在两条或多条路径中进行选择。在Java中有三种类型：if/else/else if，三元运算符和switch。
    - 循环，用于迭代多个值/对象并重复运行特定的代码块。Java中的基本循环类型是for、while和do while。
    - 分支语句，用于改变循环中的控制流。在Java中有两种类型：break 和 continue。

2. If/Else/Else If

    if/else语句是最基本的控制结构，但也可以认为是编程中决策的基础。

    虽然if可以单独使用，但最常见的使用场景是用if/else在两条路径中进行选择。

    ```java
    if (count > 2) {
        System.out.println("Count is higher than 2");
    } else {
        System.out.println("Count is lower or equal than 2");
    }
    ```

    理论上说，我们可以无限地连锁或嵌套if/else块，但这将损害代码的可读性，这就是为什么不建议这样做。

    我们将在本文的其余部分探讨替代语句。

3. 三元运算符

    我们可以使用三元运算符作为一个速记表达式，它的作用类似于if/else语句。

    我们可以用三元组对if/else语句进行重构，如下。

    `System.out.println(count > 2 ? "Count is higher than 2" : "Count is lower or equal than 2");`

    虽然三元组可以使我们的代码更有可读性，但它并不总是if/else的好替代品。

4. 转换

    如果我们有多种情况可以选择，我们可以使用switch语句。

    让我们再看一个简单的例子。

    ```java
    int count = 3;
    switch (count) {
    case 0:
        System.out.println("Count is equal to 0");
        break;
    case 1:
        System.out.println("Count is equal to 1");
        break;
    default:
        System.out.println("Count is either negative, or higher than 1");
        break;
    }
    ```

    三个或更多的if/else语句会让人难以阅读。作为可能的变通方法之一，我们可以使用switch，如上所示。

    而且还要记住，switch有范围和输入限制，我们在使用它之前需要记住。

5. 循环

    当我们需要连续多次重复相同的代码时，我们会使用loops。

    让我们看一个简单的例子，比较for和while类型的循环。

    ```java
    for (int i = 1; i <= 50; i++) {
        methodToRepeat();
    }

    int whileCounter = 1;
    while (whileCounter <= 50) {
        methodToRepeat();
        whileCounter++;
    }
    ```

    上面的两个代码块将调用methodToRepeat 50次。

6. 中断

    我们需要使用break来提前退出一个循环。

    让我们看一个快速的例子。

    ```java
    List<String> names = getNameList();
    String name = "John Doe";
    int index = 0;
    for ( ; index < names.length; index++) {
        if (names[index].equals(name)) {
            break;
        }
    }
    ```

    在这里，我们在一个名字的列表中寻找一个名字，一旦找到它，我们就想停止寻找。

    循环通常会进行到结束，但我们在这里用break来绕过它，提前退出。

7. 继续

    简单地说，continue意味着跳过我们所处的循环的其余部分。

    ```java
    List<String> names = getNameList();
    String name = "John Doe";
    String list = "";
    for (int i = 0; i < names.length; i++) { 
        if (names[i].equals(name)) {
            continue;
        }
        list += names[i];
    }
    ```

    这里，我们跳过将重复的名字追加到列表中。

    正如我们在这里看到的，break和continue在迭代时可以很方便，尽管它们通常可以用return语句或其他逻辑重写。

8. 总结

    在这篇简短的文章中，我们了解了什么是控制结构，以及如何使用它们来管理我们的Java程序中的流控制。
