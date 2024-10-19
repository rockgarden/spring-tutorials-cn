# [Java中的无限循环](https://www.baeldung.com/infinite-loops-java)

无限循环是指在不满足终止条件的情况下无休止地循环的指令序列。创建无限循环可能是一个编程错误，但也可能是基于应用程序的行为而有意为之。

1. 使用while

    让我们从while循环开始。在这里，我们将使用布尔字面的true来编写while循环的条件。

    ```java
    public void infiniteLoopUsingWhile() {
        while (true) {
            // do something
        }
    }
    ```

2. 使用for

    现在，让我们使用for循环来创建一个无限循环。

    ```java
    public void infiniteLoopUsingFor() {
        for (;;) {
            // do something
        }
    }
    ```

3. 使用do-while

    一个无限循环也可以使用Java中不太常见的do-while循环来创建。这里，循环条件在第一次执行后被评估。

    ```java
    public void infiniteLoopUsingDoWhile() {
        do {
            // do something
        } while (true);
    }
    ```
