# [Java for 循环](https://www.baeldung.com/java-for-loop)

在这篇文章中，我们将看看Java语言的一个核心方面--使用for循环重复执行一条或一组语句。

1. 简单的for循环

    for 循环是一种控制结构，它允许我们通过增加和评估一个循环计数器来重复某些操作。

    在第一次迭代之前，循环计数器被初始化，然后进行条件评估，接着进行步骤定义（通常是简单的增量）。

    for循环的语法是。

    ```txt
    for (initialization; Boolean-expression; step) 
        statement;
    ```

    让我们在一个简单的例子中看到它。

    ```java
    for (int i = 0; i < 5; i++) {
        System.out.println("Simple for loop: i = " + i);
    }
    ```

    for语句中使用的初始化、布尔表达式和步骤是可选的。下面是一个无限for循环的例子。

    ```java
    for ( ; ; ) {
        // Infinite for loop
    }
    ```

    1. 带标签的for循环

        我们也可以有标记的for循环。如果我们有嵌套的for循环，这很有用，这样我们就可以从特定的for循环中break/continue下去。

        ```java
        aa: for (int i = 1; i <= 3; i++) {
            if (i == 1)
                continue;
            bb: for (int j = 1; j <= 3; j++) {
                if (i == 2 && j == 2) {
                    break aa;
                }
                System.out.println(i + " " + j);
            }
        }
        ```

2. 增强型for循环

    从Java 5开始，我们有了第二种for循环，叫做增强型for，它使我们更容易遍历数组或集合中的所有元素。

    增强型for循环的语法是。

    ```txt
    for(Type item : items)
        statement;
    ```

    由于这种循环与标准for循环相比是简化的，所以我们在初始化循环时只需要声明两件事。

    - 我们目前正在迭代的元素的句柄handle
    - 我们正在迭代的源数组/集合(array/collection)

    因此，我们可以这样说。对于 items 中的每个元素，将该元素分配给 item 变量，然后运行循环的主体。

    让我们看一下这个简单的例子。

    ```java
    int[] intArr = { 0,1,2,3,4 };
    for (int num : intArr) {
        System.out.println("Enhanced for-each loop: i = " + num);
    }
    ```

    我们可以用它来遍历各种Java数据结构。

    给定一个 `List<String>` list对象 - 我们可以对其进行迭代。

    ```java
    for (String item : list) {
        System.out.println(item);
    }
    ```

    我们同样可以对一个 `Set<String>` 集合进行迭代。

    ```java
    for (String item : set) {
        System.out.println(item);
    }
    ```

    而且，给定一个 `Map<String,Integer>` map，我们也可以迭代它。

    ```java
    for (Entry<String, Integer> entry : map.entrySet()) {
        System.out.println(
            "Key: " + entry.getKey() + 
            " - " + 
            "Value: " + entry.getValue());
    }
    ```

    1. Iterable.forEach()

        从Java 8开始，我们可以用一种稍微不同的方式来利用for-each循环。我们现在在Iterable接口中有一个专门的forEach()方法，它接受一个代表我们要执行的动作的lambda表达式。

        在内部，它只是将工作委托给了标准循环。

        ```java
        default void forEach(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            for (T t : this) {
                action.accept(t);
            }
        }
        ```

        让我们看一下这个例子。

        ```java
        List<String> names = new ArrayList<>();
        names.add("Larry");
        names.add("Steve");
        names.add("James");
        names.add("Conan");
        names.add("Ellen");
        names.forEach(name -> System.out.println(name));
        ```

3. 总结

    在这个快速教程中，我们探讨了Java的for循环。
