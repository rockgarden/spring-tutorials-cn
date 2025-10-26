# [Java 中的 ‘public’ 访问修饰符](https://www.baeldung.com/java-public-keyword)

核心 Java · 访问修饰符 · 定义 · Java 关键字

1. 概述

    在本文中，我们将深入探讨 `public` 修饰符，并讨论在类和成员中何时以及如何使用它。

    此外，我们还将说明使用 `public` 数据字段的弊端。

2. 何时使用 `public` 访问修饰符

    `public` 类和接口，以及 `public` 成员，共同定义了一个 **API** —— 这是我们代码中可供他人查看和使用以控制对象行为的部分。

    然而，过度使用 `public` 修饰符会违反面向对象编程（OOP）的**封装原则**，并带来若干缺点：

    - **API 膨胀**：使客户端更难理解和使用；
    - **难以演进**：因为客户端依赖这些公开部分，任何未来的修改都可能破坏他们的代码。

3. `public` 接口和类

    1. `public` 接口

        一个 `public` 接口定义了一种规范，可以拥有一个或多个实现。这些实现可以由我们提供，也可以由其他人编写。

        例如，Java API 提供了 `Connection` 接口来定义数据库连接操作，而具体实现则由各数据库厂商提供。在运行时，我们会根据项目配置获得所需的连接：

        ```java
        Connection connection = DriverManager.getConnection(url);
        ```

        `getConnection` 方法返回一个特定技术实现的实例。

    2. `public` 类

        我们定义 `public` 类，以便客户端可以通过实例化或静态引用使用其成员：

        ```java
        assertEquals(0, new BigDecimal(0).intValue()); // 实例成员
        assertEquals(2147483647, Integer.MAX_VALUE);   // 静态成员
        ```

        此外，我们还可以通过可选的 `abstract` 修饰符设计用于继承的 `public` 类。使用 `abstract` 时，该类就像一个骨架，包含字段和已实现的方法供具体子类复用，同时包含抽象方法要求子类必须实现。

        例如，Java 集合框架提供了 `AbstractList` 类作为自定义列表的基础：

        ```java
        public class ListOfThree<E> extends AbstractList<E> {

            @Override
            public E get(int index) {
                // 自定义实现
            }

            @Override
            public int size() {
                // 自定义实现
            }
        }
        ```

        这样，我们只需实现 `get()` 和 `size()` 方法，而 `indexOf()`、`containsAll()` 等方法已由父类提供。

    3. 嵌套的 `public` 类和接口

        与顶层 `public` 类和接口类似，嵌套的 `public` 类和接口也定义了 API 数据类型。它们特别适用于以下两种场景：

        - 向 API 用户表明：外层顶级类型与其内部嵌套类型存在逻辑关联，通常一起使用；
        - 使代码库更紧凑，避免为每个类型创建单独的源文件（若声明为顶层类则需如此）。

        Java 核心 API 中的一个典型例子是 `Map.Entry` 接口：

        ```java
        for (Map.Entry<String, String> entry : mapObject.entrySet()) { }
        ```

        将 `Map.Entry` 设计为嵌套接口，强调了它与 `java.util.Map` 的紧密关系，同时也避免了在 `java.util` 包中额外创建一个文件。

        更多细节请参阅我们关于[嵌套类](https://www.baeldung.com/java-nested-classes)的文章。

4. `public` 方法

    `public` 方法允许用户执行预定义的操作。例如，`String` API 中的 `toLowerCase()` 方法就是 `public` 的：

    ```java
    assertEquals("alex", "ALEX".toLowerCase());
    ```

    如果一个方法不使用任何实例字段，我们可以安全地将其声明为 `static`。`Integer` 类中的 `parseInt()` 方法就是一个 `public static` 方法的例子：

    ```java
    assertEquals(1, Integer.parseInt("1"));
    ```

    构造函数通常也是 `public` 的，以便实例化和初始化对象，但在某些设计模式（如单例模式）中，构造函数可能是 `private` 的。

5. `public` 字段

    `public` 字段允许直接修改对象的状态。**经验法则：不应使用 `public` 字段**。原因如下：

    1. 线程安全性

        对非 `final` 字段或 `final` 但可变的对象字段使用 `public` 修饰符是**非线程安全**的。我们无法控制不同线程中对其引用或状态的修改。

        有关编写线程安全代码的更多内容，请参阅我们的[线程安全](https://www.baeldung.com/java-thread-safety)文章。

    2. 无法在修改时执行额外操作

        对于非 `final` 的 `public` 字段，我们完全无法控制其值的设置，因为客户端可以直接赋值。

        更好的做法是将字段设为 `private`，并通过 `public` 的 setter 方法进行访问：

        ```java
        public class Student {
            private int age;

            public void setAge(int age) {
                if (age < 0 || age > 150) {
                    throw new IllegalArgumentException("年龄必须在 0 到 150 之间");
                }
                this.age = age;
            }
        }
        ```

    3. 难以更改数据类型

        无论是可变还是不可变的 `public` 字段，都已成为客户端契约的一部分。未来若想更改其内部数据表示，将非常困难，因为客户端可能需要重构他们的代码。

        通过将字段设为 `private` 并提供访问器（getter/setter），我们可以在保持对外接口不变的同时，灵活地更改内部实现：

        ```java
        public class Student {
            private StudentGrade grade; // 新的数据表示

            public void setGrade(int grade) {
                this.grade = new StudentGrade(grade);
            }

            public int getGrade() {
                return this.grade.getGrade().intValue();
            }
        }   
        ```

        **唯一例外**：可以使用 `public static final` 修饰不可变对象来定义常量：

        ```java
        public static final String SLASH = "/";
        ```

6. 结论

    在本教程中，我们了解到 `public` 修饰符用于定义 API。

    同时，我们也说明了过度使用该修饰符会限制我们对实现进行改进的能力。

    最后，我们讨论了为何将字段声明为 `public` 是一种不良实践。
