# [Java 中的多态](https://www.baeldung.com/java-polymorphism)

核心 Java 定义

1. 概述

    所有面向对象编程（OOP）语言都必须具备四大基本特性：**抽象（Abstraction）**、**封装（Encapsulation）**、**继承（Inheritance）** 和 **多态（[Polymorphism](https://www.baeldung.com/cs/polymorphism)）**。

    本文将重点介绍 Java 中两种核心的多态形式：

    - **静态多态（Static / Compile-time Polymorphism）**
    - **动态多态（Dynamic / Runtime Polymorphism）**

    其中：

    - 静态多态在[编译时](https://www.baeldung.com/cs/compile-load-execution-time)确定；
    - 动态多态在[运行时](https://www.baeldung.com/cs/runtime-vs-compile-time)由 JVM 决定具体调用哪个方法。

2. 静态多态（方法重载）

    [静态多态](https://en.wikipedia.org/wiki/Template_metaprogramming#Static_polymorphism)主要通过 **方法重载（Method Overloading）** 实现——即在同一个类中定义多个**同名但参数不同**的方法。

    例如，在一个文件管理器应用中，`TextFile` 类可以提供多个 `read()` 方法：

    ```java
    public class TextFile extends GenericFile {
        // 无参版本
        public String read() {
            return this.getContent().toString();
        }

        // 限制读取长度
        public String read(int limit) {
            return this.getContent().toString().substring(0, limit);
        }

        // 指定起止位置
        public String read(int start, int stop) {
            return this.getContent().toString().substring(start, stop);
        }
    }
    ```

    在编译阶段，编译器会根据调用时传入的**参数数量和类型**，选择匹配的方法。这种机制避免了运行时的虚方法表（vtable）查找，因此效率更高。

    > 静态多态 = 编译时绑定 = 方法重载

3. 动态多态（方法重写）

    动态多态通过 **方法重写（Method Overriding）** 实现，依赖于**继承**和**运行时绑定（late binding）**。

    假设我们有一个基类 `GenericFile`：

    ```java
    public class GenericFile {
        private String name;

        public String getFileInfo() {
            return "Generic File Impl";
        }
    }
    ```

    其子类 `ImageFile` 重写了该方法：

    ```java
    public class ImageFile extends GenericFile {
        private int height, width;

        @Override
        public String getFileInfo() {
            return "Image File Impl";
        }
    }
    ```

    当我们这样使用时：

    ```java
    public static void main(String[] args) {
        GenericFile genericFile = new ImageFile("SampleImageFile", 200, 100,
            new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB).toString().getBytes(), "v1.0.0");

        logger.info("File Info: \n" + genericFile.getFileInfo());
    }
    ```

    尽管变量类型是 `GenericFile`，但实际对象是 `ImageFile`。JVM 在运行时会调用 **子类重写后的方法**，输出：

    ```log
    File Info:
    Image File Impl
    ```

    > 动态多态 = 运行时绑定 = 方法重写 + 向上转型（Upcasting）

4. Java 中其他体现多态的特性

    除了上述两种主要形式，Java 还在多个层面体现多态思想。

    1. 类型强制转换（Coercion）

        编译器自动进行隐式类型转换以避免类型错误。例如：

        ```java
        String str = "string" + 2;  // int 2 被自动转为 String
        ```

    2. 运算符重载（Operator Overloading）

        虽然 Java **不支持用户自定义运算符重载**，但语言本身对 `+` 运算符进行了多态处理：

        ```java
        String str = "2" + 2;  // 字符串拼接 → "22"
        int sum = 2 + 2;       // 数值相加 → 4
        ```

        上下文（操作数类型）决定了 `+` 的行为。

    3. 多态参数（Variable Shadowing）

        在方法内部声明与类成员同名的局部变量，会导致**变量隐藏（hiding）**：

        ```java
        public class TextFile extends GenericFile {
            private String content;

            public void setContentDelimiter() {
                int content = 100;  // 局部变量 content 隐藏了成员变量
                this.content = this.content + content; // 使用 this 明确引用成员
            }
        }
        ```

        建议使用 `this` 关键字明确区分成员变量与局部变量。

    4. 子类型多态（Subtype Polymorphism）

        这是动态多态的核心应用：**父类引用指向子类对象**，并调用子类重写的方法。

        例如，处理多种文件类型：

        ```java
        GenericFile[] files = {
            new ImageFile("img.jpg", 800, 600, ..., "v1.0"),
            new TextFile("doc.txt", "Hello", "v1.0")
        };

        for (GenericFile file : files) {
            System.out.println(file.getFileInfo()); // 自动调用各自子类实现
        }
        ```

        **关键机制：**

        - **向上转型（Upcasting）**：`GenericFile f = new ImageFile();`
        - **后期绑定（Late Binding）**：JVM 在运行时决定调用哪个方法

        若需调用子类特有方法，可进行**向下转型（Downcasting）**：

        ```java
        if (file instanceof ImageFile) {
            ImageFile img = (ImageFile) file;
            System.out.println(img.getHeight());
        }
        ```

5. 多态带来的问题

    尽管多态强大，但也存在潜在风险。

    1. 向下转型的类型安全问题

        错误的向下转型会导致 `ClassCastException`：

        ```java
        GenericFile file = new GenericFile();
        ImageFile img = (ImageFile) file; // 编译通过，但运行时报错！
        ```

        **解决方案：**

        - 使用 `instanceof` 检查类型（但频繁使用可能暗示设计问题）
        - 或用 `try-catch` 捕获异常

        > 注意：`instanceof` 和 RTTI（运行时类型信息）检查有性能开销，应谨慎使用。

    2. 脆弱基类问题（Fragile Base Class Problem）

        对父类的“安全”修改可能导致子类行为异常。

        **示例：**

        ```java
        // 原始父类
        class GenericFile {
            void writeContent(String content) { /* ... */ }
            void toString(String str) { str.toString(); }
        }

        class TextFile extends GenericFile {
            @Override
            void writeContent(String content) {
                toString(content); // 调用父类 toString
            }
        }
        ```

        若后来修改父类：

        ```java
        // 修改后的父类
        class GenericFile {
            void toString(String str) {
                writeContent(str); // 递归调用！
            }
        }
        ```

        此时 `TextFile.writeContent()` 会陷入无限递归，导致 StackOverflowError。

        **缓解策略：**

        - 对关键方法使用 `final` 禁止重写；
        - 优先使用 **组合（Composition）** 而非继承；
        - 提供清晰的文档和契约（如使用接口）。

6. 结论

    本文深入探讨了 Java 中多态的核心概念：

    - **静态多态**（方法重载）提升代码灵活性；
    - **动态多态**（方法重写 + 向上转型）是实现可扩展、可维护系统的关键。

    然而，多态也带来类型安全和设计脆弱性等挑战。合理使用 `final`、避免过度依赖 `instanceof`、优先组合而非继承，是构建健壮面向对象系统的重要原则。

    > 多态不是万能的，但它是 OOP 的灵魂——用得好，代码优雅；用得不当，隐患丛生。
