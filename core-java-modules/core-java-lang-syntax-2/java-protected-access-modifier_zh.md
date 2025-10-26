# [Java 中的 ‘protected’ 访问修饰符](https://www.baeldung.com/java-protected-access-modifier)

核心 Java · 访问修饰符 · 定义 · Java 关键字

1. 概述

    在 Java 编程语言中，字段、构造函数、方法和类都可以使用访问修饰符进行标记。在本教程中，我们将探讨 `protected` 访问修饰符。

2. `protected` 关键字

    与只能在声明类内部访问的 `private` 元素不同，`protected` 关键字允许**同一包内的类**以及**子类**（无论子类是否在同一个包中）访问这些成员。

    通过使用 `protected` 关键字，我们可以决定哪些方法和字段应被视为**包内或类继承体系的内部实现细节**，哪些应对外部代码开放。

3. 声明 `protected` 字段、方法和构造函数

    首先，我们创建一个名为 `FirstClass` 的类，其中包含一个 `protected` 字段、方法和构造函数：

    ```java
    public class FirstClass {
        protected String name;

        protected FirstClass(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }
    }
    ```

    通过使用 `protected` 修饰符，我们允许以下两类代码访问这些成员：

    - 与 `FirstClass` **处于同一包**中的类；
    - **继承自 `FirstClass` 的子类**（即使子类在不同包中）。

4. 访问 `protected` 字段、方法和构造函数

    1. 从同一包中访问

        现在，我们在与 `FirstClass` 相同的包中创建一个 `GenericClass`，尝试访问这些 `protected` 成员：

        ```java
        public class GenericClass {
            public static void main(String[] args) {
                FirstClass first = new FirstClass("random name");
                System.out.println("FirstClass name is " + first.getName());
                first.name = "new name";
            }
        }
        ```

        由于调用类 `GenericClass` 与 `FirstClass` 在**同一个包**中，因此可以正常访问所有 `protected` 字段、方法和构造函数。

    2. 从不同包中访问

        现在，我们尝试从一个**不同包**中的类访问这些成员：

        ```java
        public class SecondGenericClass {
            public static void main(String[] args) {
                FirstClass first = new FirstClass("random name"); // ❌
                System.out.println("FirstClass name is " + first.getName()); // ❌
                first.name = "new name"; // ❌
            }
        }
        ```

        正如预期，我们会遇到**编译错误**：

        ```log
        The constructor FirstClass(String) is not visible
        The method getName() from the type FirstClass is not visible
        The field FirstClass.name is not visible
        ```

        这是因为 `SecondGenericClass` 既**不在同一个包中**，也**不是 `FirstClass` 的子类**，因此无法访问其 `protected` 成员。

    3. 从子类中访问

        现在，我们创建一个位于**不同包**中但继承自 `FirstClass` 的子类：

        ```java
        public class SecondClass extends FirstClass {
            public SecondClass(String name) {
                super(name);
                System.out.println("SecondClass name is " + this.getName());
                this.name = "new name";
            }
        }
        ```

        正如预期，我们可以顺利访问所有 `protected` 字段、方法和构造函数，因为 `SecondClass` 是 `FirstClass` 的**子类**。

5. `protected` 内部类

    前面我们展示了 `protected` 字段、方法和构造函数的用法。还有一种特殊情况：**`protected` 内部类**。

    我们在 `FirstClass` 中定义一个静态内部类：

    ```java
    package com.baeldung.core.modifiers;

    public class FirstClass {
        // ...

        protected static class InnerClass {
            // 空类
        }
    }
    ```

    这是一个 `static` 内部类，因此无需依赖 `FirstClass` 的实例即可创建。但由于它是 `protected` 的，**只有同一包中的代码或子类才能访问它**。

    1. 从同一包中访问

        我们在 `GenericClass` 中尝试实例化该内部类：

        ```java
        public class GenericClass {
            public static void main(String[] args) {
                // ...
                FirstClass.InnerClass innerClass = new FirstClass.InnerClass();
            }
        }
        ```

        由于 `GenericClass` 与 `FirstClass` 在同一包中，因此可以**正常实例化** `InnerClass`。

    2. 从不同包中访问

        现在，我们在 `SecondGenericClass`（位于不同包）中尝试实例化：

        ```java
        public class SecondGenericClass {
            public static void main(String[] args) {
                // ...
                FirstClass.InnerClass innerClass = new FirstClass.InnerClass(); // ❌
            }
        }
        ```

        结果出现编译错误：

        ```log
        The type FirstClass.InnerClass is not visible
        ```

        这符合预期。

    3. 从子类中访问

        我们再尝试在子类 `SecondClass` 中实例化 `InnerClass`：

        ```java
        public class SecondClass extends FirstClass {
            public SecondClass(String name) {
                // ...
                FirstClass.InnerClass innerClass = new FirstClass.InnerClass(); // ❌
            }
        }
        ```

        令人意外的是，这里也出现了编译错误：

        ```log
        The constructor FirstClass.InnerClass() is not visible
        ```

        **原因分析**：

        - `InnerClass` 被声明为 `protected static class`；
        - 其默认构造函数是隐式 `protected` 的；
        - 虽然 `SecondClass` 是 `FirstClass` 的子类，但它不是 `InnerClass` 的子类；
        - 而且 `SecondClass` 位于不同包中。

        因此，`SecondClass` 无法访问 `InnerClass` 的 `protected` 构造函数。

        **解决方案**：
        如果我们希望子类能够实例化该内部类，可以显式地为其添加一个 `public` 构造函数：

        ```java
        protected static class InnerClass {
            public InnerClass() {
                // 显式声明 public 构造函数
            }
        }
        ```

        这样，`SecondClass` 就可以成功实例化 `InnerClass`，不再报错。

6. 结论

    在本篇简明教程中，我们讨论了 Java 中的 `protected` 访问修饰符。
    通过它，我们可以精确控制哪些数据和方法仅对同一包内的类或子类可见，从而在封装性与继承灵活性之间取得良好平衡。
