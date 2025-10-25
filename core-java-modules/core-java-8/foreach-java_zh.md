# [Java forEach 循环使用指南](https://www.baeldung.com/foreach-java)

核心 Java（≥ Java 8）· Java 循环

1. 概述

    Java 8 引入了 `forEach()` 方法，为程序员提供了一种简洁的方式来遍历集合。

    在本教程中，我们将学习如何在集合上使用 `forEach()` 方法、它接受何种参数，以及它与增强型 for 循环（enhanced for-loop）有何不同。

2. forEach() 基础

    在 Java 中，`Collection` 接口继承自 `Iterable` 接口。从 Java 8 开始，`Iterable` 新增了一个 API：

    ```java
    void forEach(Consumer<? super T> action)
    ```

    简而言之，`forEach` 的 [Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Iterable.html#forEach(java.util.function.Consumer)) 说明：**“对 Iterable 中的每个元素执行给定操作，直到所有元素处理完毕或操作抛出异常。”**

    因此，通过 `forEach()`，我们可以遍历集合并对每个元素执行指定操作。

    例如，以下是一个使用增强型 for 循环打印字符串集合的代码：

    ```java
    List<String> names = List.of("Larry", "Steve", "James", "Conan", "Ellen");

    for (String name : names) {
        LOG.info(name);
    }
    ```

    使用 `forEach()` 可以改写为：

    ```java
    names.forEach(name -> {
        LOG.info(name);
    });
    ```

    这里，我们在集合上调用 `forEach()`，并将每个名字记录到日志中。

3. 在集合中使用 forEach() 方法

    `forEach()` 方法契合 Java 的函数式编程范式，使代码更具**声明性**（declarative）。

    1. 遍历 List

        `forEach()` 可直接用于 `List`：

        ```java
        List<String> names = List.of("Larry", "Steve", "James", "Conan", "Ellen");
        names.forEach(name -> logger.info(name));
        ```

        上述代码将集合中的所有元素输出到控制台。

    2. 使用 forEach() 遍历 Map

        `Map` 并非 `Iterable`，但它提供了自己的 `forEach()` 变体，接受一个 `BiConsumer` 参数。

        Java 8 为 `Map.forEach()` 引入了 `BiConsumer`（而非 `Consumer`），以便能同时对键和值执行操作。

        创建一个示例 Map：

        ```java
        Map<Integer, String> namesMap = new HashMap<>();
        namesMap.put(1, "Larry");
        namesMap.put(2, "Steve");
        namesMap.put(3, "James");
        ```

        使用 `Map` 的 `forEach()` 遍历：

        ```java
        namesMap.forEach((key, value) -> LOG.info(key + " " + value));
        ```

        这里我们使用 `BiConsumer` 同时处理键和值。

    3. 通过 entrySet() 遍历 Map

        我们也可以对 `Map` 的 `entrySet()` 调用 `Iterable` 的 `forEach()`：

        ```java
        namesMap.entrySet().forEach(entry ->
            LOG.info(entry.getKey() + " " + entry.getValue())
        );
        ```

        因为 `Map` 的条目以 `Set<Map.Entry>` 形式存储，而 `Set` 实现了 `Iterable`，所以可以使用标准 `forEach()`。

    4. 使用 forEach() 进行并行操作

        对于大型集合，结合并行流（parallel stream）使用 `forEach()` 可利用多核 CPU 提升性能：

        ```java
        List<String> names = List.of("Larry", "Steve", "James", "Conan", "Ellen");
        names.parallelStream().forEach(LOG::info);
        ```

        上述代码并行执行，但需注意：**并行会增加资源消耗**，且不保证元素处理顺序。

4. forEach() 的常见误用

    尽管 `forEach()` 使用便捷，但它存在一些限制。

    1. 不能直接用于数组

        数组本身没有 `forEach()` 方法：

        ```java
        String[] foodItems = {"rice", "beans", "egg"};
        foodItems.forEach(food -> logger.info(food)); // 编译错误！
        ```

        解决方法：先将数组转为流：

        ```java
        Arrays.stream(foodItems).forEach(food -> logger.info(food));
        ```

        因为 `Stream` 提供了 `forEach()`，所以转换后即可使用。

    2. 不能修改集合本身

        在 `forEach()` 中修改正在遍历的集合会导致异常：

        ```java
        List<String> names = List.of("Larry", "Steve", "James", "Conan", "Ellen");
        names.forEach(name -> {
            if (name.equals("Larry")) {
                names.remove(name); // 抛出 ConcurrentModificationException
            }
        });
        ```

        与传统 for 循环不同，`forEach()` **禁止在迭代过程中修改集合结构**。

    3. 不支持 break 或 continue

        `forEach()` 无法使用 `break` 或 `continue`：

        ```java
        names.forEach(name -> {
            if (name.equals("Steve")) {
                break; // 编译错误！
            }
            logger.info(name);
        });
        ```

        Lambda 表达式中不能使用这些控制流关键字。

    4. 不支持计数器

        无法在 `forEach()` 中直接修改外部计数器：

        ```java
        int count = 0;
        names.forEach(name -> {
            count++; // 编译错误！
        });
        ```

        因为 Lambda 要求捕获的局部变量必须是 **effectively final**（实质上不可变）。

        **替代方案**：使用 `AtomicInteger` 等线程安全的可变容器：

        ```java
        AtomicInteger count = new AtomicInteger(0);
        names.forEach(name -> count.incrementAndGet());
        ```

    5. 无法访问前驱或后继元素

        传统 for 循环可通过索引访问相邻元素：

        ```java
        for (int i = 0; i < names.size(); i++) {
            String current = names.get(i);
            String previous = (i > 0) ? names.get(i - 1) : "None";
            String next = (i < names.size() - 1) ? names.get(i + 1) : "None";
            LOG.info("Current: {}, Previous: {}, Next: {}", current, previous, next);
        }
        ```

        而 `forEach()` **不暴露元素索引**，因此无法直接获取前后元素。

5. forEach() 与传统 for 循环对比

    两者都能遍历集合和数组，但 `forEach()` **灵活性较低**。

    传统 for 循环允许我们显式控制循环变量、条件和步长：

    ```java
    for (int i = 0; i < names.size(); i++) {
        LOG.info(names.get(i));
    }
    ```

    还可灵活调整条件，例如跳过最后一个元素：

    ```java
    for (int i = 0; i < names.size() - 1; i++) {
        LOG.info(names.get(i));
    }
    ```

    这种细粒度控制在 `forEach()` 中无法实现。

    此外：

    - `forEach()` **不允许修改集合本身**；
    - 传统 for 循环在小心操作下**允许修改集合**（如通过索引删除）。

6. forEach() 与增强型 for 循环对比

    从功能上看，两者都能遍历集合元素，但本质不同：

    - **增强型 for 循环** 是 **外部迭代器（External Iterator）**；
    - **forEach()** 是 **内部迭代器（Internal Iterator）**。

    1. 内部迭代器：forEach()

        内部迭代器在后台管理迭代过程，程序员只需关注“**对每个元素做什么**”。

        例如：

        ```java
        names.forEach(name -> LOG.info(name));
        ```

        这里，我们只提供一个 Lambda 表达式说明操作内容，迭代逻辑由集合内部自动处理。

    2. 外部迭代器：for 循环

        外部迭代器要求程序员控制“**如何迭代**”。

        虽然增强型 for 循环隐藏了 `iterator()`、`hasNext()`、`next()` 等细节，但底层仍依赖这些方法。这意味着迭代逻辑由**外部代码驱动**，而非集合自身。

        ```java
        for (String name : names) {
            LOG.info(name);
        }
        ```

        尽管写法简洁，但本质上仍是外部控制迭代过程。
7. 结论

    本文展示了 `forEach()` 循环相比传统 for 循环更加简洁便利。

    我们学习了 `forEach()` 的工作原理、它接受的参数类型（如 `Consumer` 或 `BiConsumer`），以及如何对集合中的每个元素执行操作。

    同时，我们也认识到其局限性：**不支持 break/continue、不能修改集合、无法访问索引或相邻元素**。

    因此，在选择循环方式时：

    - 若追求简洁、声明式风格，且无需复杂控制逻辑，**优先使用 `forEach()`**；
    - 若需要索引、计数、跳过、修改集合或访问相邻元素，则**应使用传统 for 循环**。

    合理选择，方能写出既高效又可维护的代码。
