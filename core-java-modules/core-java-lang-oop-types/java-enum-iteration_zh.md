# [在Java中遍历枚举值](https://www.baeldung.com/java-enum-iteration)

1. 概述

    在 Java 中，枚举是一种数据类型，可帮助我们将一组预定义的常量分配给变量。

    在本快速教程中，我们将学习在 Java 中遍历枚举的不同方法。

2. 遍历枚举值

    让我们先定义一个枚举，以便创建一些简单的代码示例：

    enums.iteration/DaysOfWeekEnum.java

    枚举没有用于迭代的方法，如 forEach() 或 iterator()。相反，我们可以使用由 values() 方法返回的 Enum 值数组。

    1. 使用 for 循环进行迭代

        首先，我们可以简单地使用老式 for 循环：

        `for (DaysOfWeekEnum day : DaysOfWeekEnum.values()) {}`

    2. 使用流迭代

        我们还可以使用 java.util.Stream 对 Enum 值执行操作。

        要创建一个流，我们有两种选择。第一种是使用 Stream.of：

        `Stream.of(DaysOfWeekEnum.values());`

        第二种是使用 Arrays.stream：

        `Arrays.stream(DaysOfWeekEnum.values());`

        让我们扩展 DaysOfWeekEnum 类，创建一个使用 Stream 的示例：

        enums.iteration/DaysOfWeekEnum.java\stream()

        现在，我们将编写一个示例来打印非工作日：

        `DaysOfWeekEnum.stream().filter(d -> d.getTypeOfDay().equals("off")).forEach(System.out::println);`

    3. 使用 forEach() 进行迭代

        在 Java 8 中，forEach() 方法被添加到 Iterable 接口中。因此，所有 Java 集合类都有 forEach() 方法的实现。为了在枚举中使用这些方法，我们首先需要将枚举转换为合适的集合。我们可以使用 Arrays.asList() 生成一个 ArrayList，然后使用 forEach() 方法对其进行循环：

        `Arrays.asList(DaysOfWeekEnum.values()).forEach(day -> System.out.println(day));`

    4. 使用 EnumSet 进行遍历

        EnumSet 是一种专门的集合实现，我们可以将其用于枚举类型：

        `EnumSet.allOf(DaysOfWeekEnum.class).forEach(day -> System.out.println(day));`

    5. 使用枚举值数组列表

        我们还可以将枚举值添加到列表中。这样，我们就可以像操作其他程序一样操作 List：

        ```java
        List<DaysOfWeekEnum> days = new ArrayList<>();
        days.add(DaysOfWeekEnum.FRIDAY);
        days.remove(DaysOfWeekEnum.SATURDAY);
        ```

        我们还可以使用 Arrays.asList() 创建 ArrayList。

        但是，由于 ArrayList 由 Enum 值数组支持，它将是不可变的，因此我们无法从列表中添加或删除项目。下面代码中的移除操作将失败，并出现 UnsupportedOperationException 异常：

        ```java
        List<DaysOfWeekEnum> days = Arrays.asList(DaysOfWeekEnum.values());
        days.remove(0);
        ```

3. 结论

    在本文中，我们讨论了在 Java 中使用 forEach、Stream 和 for 循环遍历枚举的各种方法。

    如果我们需要执行并行操作，Stream 是一个不错的选择。否则，使用哪种方法并无限制。
