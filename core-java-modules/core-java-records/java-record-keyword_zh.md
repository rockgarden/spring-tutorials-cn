# Java Record 关键字详解

Core Java

>= Java 11  Definition  Java Keyword  Record

1. 引言

    在许多 Java 应用程序中，**在对象之间传递不可变数据**是最常见但也最繁琐的任务之一。

    在 Java 14 之前，我们需要手动创建包含大量样板代码（boilerplate）的类——这些代码不仅容易出错，还模糊了类的真实意图。

    自 Java 14 起，我们可以使用 **record（记录）** 来解决这些问题。

    在本教程中，我们将学习 record 的基础知识，包括其设计目的、自动生成的方法，以及如何对其进行定制。

2. 设计目的

    我们经常需要编写仅用于**承载数据**的类，比如数据库查询结果、服务返回信息等。

    在多数情况下，这些数据应该是**不可变的**——因为不可变性可以保证数据有效性，无需同步控制。

    为此，我们通常需要为数据类编写以下内容：

    - 每个数据字段：`private final`
    - 每个字段的 getter 方法
    - 一个接收所有字段参数的公共构造函数
    - `equals()` 方法：当所有字段值相同时返回 `true`
    - `hashCode()` 方法：当所有字段值相同时返回相同哈希值
    - `toString()` 方法：包含类名和每个字段名及其值

    例如，一个简单的 `Person` 数据类：

    ```java
    public class Person {
        private final String name;
        private final String address;

        public Person(String name, String address) {
            this.name = name;
            this.address = address;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, address);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Person)) return false;
            Person other = (Person) obj;
            return Objects.equals(name, other.name)
                && Objects.equals(address, other.address);
        }

        @Override
        public String toString() {
            return "Person [name=" + name + ", address=" + address + "]";
        }

        // 标准 getter 方法
        public String getName() { return name; }
        public String getAddress() { return address; }
    }
    ```

    虽然实现了目标，但有两个明显问题：

    ✅ **问题一：大量样板代码**  
    每个数据类都要重复编写字段、构造函数、`equals`/`hashCode`/`toString`。即使 IDE 能自动生成，新增字段时仍需手动更新 `equals` 等方法。

    ✅ **问题二：意图不清晰**  
    大量代码掩盖了类的本质：它只是一个包含 `name` 和 `address` 两个字符串字段的数据载体。

    更好的方式是：**显式声明这是一个数据类**。

3. 基础用法

    从 JDK 14 开始，我们可以用 **record** 替代重复的数据类。Record 是一种**不可变数据类**，只需声明字段的类型和名称，其余均由编译器自动生成：

    - `private final` 字段
    - 公共构造函数
    - getter 方法
    - `equals()`、`hashCode()`、`toString()`

    创建 `Person` record：

    ```java
    public record Person(String name, String address) {}
    ```

    1. 构造函数

        Record 会自动生成一个公共构造函数，参数顺序与字段声明一致：

        ```java
        public Person(String name, String address) {
            this.name = name;
            this.address = address;
        }
        ```

        使用方式与普通类相同：

        ```java
        Person person = new Person("John Doe", "100 Linda Ln.");
        ```

    2. Getter 方法

        Record 自动生成与字段同名的 getter 方法（无 `get` 前缀）：

        ```java
        @Test
        public void givenValidNameAndAddress_whenGetNameAndAddress_thenExpectedValuesReturned() {
            String name = "John Doe";
            String address = "100 Linda Ln.";
            Person person = new Person(name, address);

            assertEquals(name, person.name());    // 注意：是 name()，不是 getName()
            assertEquals(address, person.address());
        }
        ```

    3. equals 方法

        自动生成的 `equals()` 方法会比较两个对象是否为同一类型，且所有字段值是否相等：

        ```java
        @Test
        public void givenSameNameAndAddress_whenEquals_thenPersonsEqual() {
            Person person1 = new Person("John Doe", "100 Linda Ln.");
            Person person2 = new Person("John Doe", "100 Linda Ln.");
            assertTrue(person1.equals(person2)); // true
        }
        ```

        只要任一字段不同，`equals()` 就返回 `false`。

    4. hashCode 方法

        `hashCode()` 与 `equals()` 保持一致：**所有字段值相同时，哈希值也相同**（忽略[哈希碰撞](https://en.wikipedia.org/wiki/Birthday_problem)）：

        ```java
        @Test
        public void givenSameNameAndAddress_whenHashCode_thenPersonsEqual() {
            Person person1 = new Person("John Doe", "100 Linda Ln.");
            Person person2 = new Person("John Doe", "100 Linda Ln.");
            assertEquals(person1.hashCode(), person2.hashCode());
        }
        ```

        ⚠️ 注意：字段值不同时，哈希值**通常不同**，但不保证（符合 `hashCode` 合约）。

    5. toString 方法

        自动生成的 `toString()` 格式为：`记录名[字段名=值, ...]`

        ```java
        Person person = new Person("John Doe", "100 Linda Ln.");
        System.out.println(person.toString());
        // 输出：Person[name=John Doe, address=100 Linda Ln.]
        ```

4. 构造函数定制

    虽然 record 会自动生成构造函数，但我们仍可对其进行定制——主要用于**参数校验**，且应尽量保持简单。

    1. 紧凑构造函数（Compact Constructor）

        用于校验字段值，无需显式赋值：

        ```java
        public record Person(String name, String address) {
            public Person {
                Objects.requireNonNull(name);    // 校验非空
                Objects.requireNonNull(address);
            }
        }
        ```

    2. 自定义构造函数（带参）

        可定义其他参数列表的构造函数：

        ```java
        public record Person(String name, String address) {
            public Person(String name) {
                this(name, "Unknown"); // 调用主构造函数
            }
        }
        ```

    3. 完全重写主构造函数

        若需完全自定义主构造函数（参数列表与字段一致），必须**手动初始化所有字段**：

        ```java
        public record Person(String name, String address) {
            public Person(String name, String address) {
                this.name = name;
                this.address = address;
                // 可添加额外逻辑
            }
        }
        ```

        > ❌ 禁止：紧凑构造函数 + 主构造函数重载

        同时定义紧凑构造函数和参数相同的主构造函数会导致编译错误：

        ```java
        public record Person(String name, String address) {
            public Person { // 紧凑构造函数
                Objects.requireNonNull(name);
                Objects.requireNonNull(address);
            }

            public Person(String name, String address) { // ❌ 编译错误！
                this.name = name;
                this.address = address;
            }
        }
        ```

5. 静态变量与方法

    与普通类一样，record 也支持静态成员。

    1. 静态变量

        ```java
        public record Person(String name, String address) {
            public static String UNKNOWN_ADDRESS = "Unknown";
        }
        ```

        使用：`Person.UNKNOWN_ADDRESS`

    2. 静态方法

        ```java
        public record Person(String name, String address) {
            public static Person unnamed(String address) {
                return new Person("Unnamed", address);
            }
        }
        ```

        使用：`Person.unnamed("100 Linda Ln.")`

6. 总结

    在本文中，我们深入探讨了 Java 14 引入的 `record` 关键字，包括其核心概念与使用细节。

    通过使用 record 及其编译器自动生成的方法，我们可以：

    ✅ **大幅减少样板代码**  
    ✅ **提升不可变类的可靠性**  
    ✅ **清晰表达“数据载体”的设计意图**
