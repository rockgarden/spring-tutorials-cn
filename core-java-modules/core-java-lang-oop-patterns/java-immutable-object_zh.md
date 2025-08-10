# [Java中的不可变对象](https://www.baeldung.com/java-immutable-object)

1. 概述

    在本教程中，我们将学习是什么使得对象不可变、如何在 Java 中实现不可变以及这样做有什么好处。

    测试代码：ImmutableObjectsUnitTest.java

2. 什么是不可变对象？

    不可变对象是一种在完全创建后内部状态保持不变的对象。

    这意味着，不可变对象的公共应用程序接口（API）可以保证它在整个生命周期中的行为方式都是相同的。

    如果我们看一下 String 类，就会发现即使它的 API 似乎通过 replace 方法为我们提供了一种可变行为，原始 String 也不会发生变化：

    ```java
    String name = "baeldung";
    String newName = name.replace("dung", "----");

    assertEquals("baeldung", name);
    assertEquals("bael----", newName);
    ```

    API 给我们提供的是只读方法，绝不应包含改变对象内部状态的方法。

3. Java 中的 final 关键字

    在尝试实现 Java 中的不变性之前，我们应该先谈谈 final 关键字。

    在 Java 中，变量默认情况下是可变的，这意味着我们可以更改它们所持有的值。

    在声明变量时使用 final 关键字，Java 编译器就不会让我们更改该变量的值。相反，它会在编译时报错：

    ```java
    final String name = "baeldung";
    name = "bael...";
    ```

    请注意，final 只禁止我们更改变量的引用，并不保护我们通过使用其公共 API 来更改引用对象的内部状态：

    ```java
    final List<String> strings = new ArrayList<>();
    assertEquals(0, strings.size());
    strings.add("baeldung");
    assertEquals(0, strings.size());
    ```

    第二个 assertEquals 将失败，因为向列表中添加元素会改变其大小，因此它不是不可变对象。

4. Java 中的不可变性

    既然我们知道了如何避免变量内容发生变化，我们就可以用它来构建不可变对象的 API。

    构建不可变对象的 API 要求我们保证，无论我们如何使用其 API，其内部状态都不会发生变化。

    朝着正确方向迈出的一步就是在声明属性时使用 final：

    ![Money](./src/main/java/com/baeldung/immutableobjects/Money.java)

    请注意，Java 保证 amount 的值不会改变，所有原始类型变量都是如此。

    但是，在我们的示例中，我们只保证货币不会发生变化，所以我们必须依靠货币 API 来保护自己免受变化的影响。

    大多数情况下，我们需要对象的属性来保存自定义值，而初始化不可变对象内部状态的地方就是它的构造函数。

    正如我们之前所说，为了满足不可变 API 的要求，我们的 Money 类只有只读方法。

    使用反射 API，我们可以打破不可变性并更改[不可变对象](https://stackoverflow.com/questions/20945049/is-a-java-string-really-immutable)。但是，反射违反了不可变对象的公共 API，通常我们应该避免这样做。

5. 优点

    由于不可变对象的内部状态在时间上保持不变，因此我们可以在多个线程之间安全地共享它。

    我们还可以自由地使用它，引用它的对象都不会发现任何不同，可以说不可变对象是没有副作用的。

6. 结论

    不可变对象不会及时改变其内部状态，是线程安全和无副作用的。由于这些特性，不可变对象在处理多线程环境时也特别有用。
