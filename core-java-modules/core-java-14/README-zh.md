# Core Java 14

## Java14记录关键字

1. 简介

    在对象之间传递不可变数据是许多 Java 应用程序中最常见但又最普通的任务之一。

    在 Java 14 之前，这需要创建一个带有模板字段和方法的类，而这些模板字段和方法很容易出现琐碎的错误和模糊的意图。

    Java 14 发布后，我们现在可以使用记录来解决这些问题。

    在本教程中，我们将了解记录的基本原理，包括其用途、生成方法和自定义技术。

    [Java 14 记录与Lombok](https://www.baeldung.com/java-record-vs-lombok)

    了解 Java 14 Record 与 Lombok 之间的异同。

    [Java 14 中的新功能](https://www.baeldung.com/java-14-new-features)

    探索 Java 14 的各种 JEP。

    [Java 14 中的 @Serial 注解指南](https://www.baeldung.com/java-14-serial-annotation)

    了解如何在 Java 14 中应用 @Serial 注解来协助对类的可序列化属性进行编译时检查。

2. 目的

    通常，我们编写类只是为了保存数据，如数据库结果、查询结果或来自服务的信息。

    在很多情况下，这些数据是[不可变](https://www.baeldung.com/java-immutable-object)的，因为不可变性可以[确保数据的有效性](https://www.baeldung.com/java-immutable-object#benefits-of-immutability)，而无需同步。

    为了实现这一目标，我们创建了具有以下功能的数据类：

    - 每个数据的private, final字段
    - 每个字段的获取器(getter)
    - 为每个字段提供相应参数的公共构造函数(public constructor)
    - equals 方法，当所有字段匹配时，该方法对同类对象返回 true
    - hashCode 方法，当所有字段匹配时返回相同的值
    - toString 方法，包含类的名称、每个字段的名称及其相应的值

    例如，我们可以创建一个简单的 Person 数据类，其中包含姓名和地址：

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
            if (this == obj) {
                return true;
            } else if (!(obj instanceof Person)) {
                return false;
            } else {
                Person other = (Person) obj;
                return Objects.equals(name, other.name)
                && Objects.equals(address, other.address);
            }
        }
        @Override
        public String toString() {
            return "Person [name=" + name + ", address=" + address + "]";
        }
        // standard getters
    }
    ```

    虽然这实现了我们的目标，但有两个问题：

    - 有很多模板(boilerplate)代码
    - 我们模糊了类的目的：表示一个有姓名和地址的人

    在第一种情况下，我们必须为每个数据类重复同样乏味的过程，单调地为每个数据创建一个新字段；创建 equals、hashCode 和 toString 方法；以及创建一个接受每个字段的构造函数。

    虽然集成开发环境可以自动生成许多这样的类，但它们无法在我们添加新字段时自动更新我们的类。例如，如果我们添加了一个新字段，就必须更新我们的 equals 方法以纳入该字段。

    在第二种情况下，额外的代码掩盖了我们的类只是一个拥有姓名和地址两个字符串字段的数据类。

    更好的方法是明确声明我们的类是一个数据类。

3. 基础知识

    从 JDK 14 开始，我们可以用记录来替换重复的数据类。记录是不可变的数据类，只需要字段的类型和名称。

    equals、hashCode 和 toString 方法以及私有字段、最终字段和公共构造函数都由 Java 编译器生成。

    要创建 Person 记录，我们将使用 record 关键字：

    `public record Person (String name, String address) {}`

    参考代码：record/PersonUnitTest.java

    1. 构造函数

        使用记录，我们会生成一个公共构造函数，每个字段都有一个参数。

        以我们的 Person 记录为例，等价的构造函数是

        `public Person(String name, String address) {}`

        这个构造函数的使用方法与类相同，可以从记录中实例化对象：

        `Person person = new Person("John Doe", "100 Linda Ln.");`
    2. 获取器

        我们还可以免费获得名称与字段名称一致的公共获取器方法。

        在我们的 Person 记录中，这意味着一个 name() 和 address() 获取器：

        givenValidNameAndAddress_whenGetNameAndAddress_thenExpectedValuesReturned()

    3. 等于

        此外，我们还生成了一个 equals 方法。

        如果提供的对象类型相同且所有字段的值都匹配，该方法将返回 true：

        givenSameNameAndAddress_whenEquals_thenPersonsEqual()

        如果两个 Person 实例的任何字段不同，equals 方法将返回 false。

    4. 哈希代码

        与 equals 方法类似，我们也会生成一个相应的 hashCode 方法。

        如果两个 Person 对象的所有字段值都匹配，我们的 hashCode 方法就会为这两个对象返回相同的值（[生日悖论](https://en.wikipedia.org/wiki/Birthday_problem)导致的碰撞除外）：

        givenSameNameAndAddress_whenHashCode_thenPersonsEqual()

        如果任何字段值不同，哈希代码值很可能也会不同。但 hashCode() 合约并不保证这一点。

    5. toString

        最后，我们还收到了一个 toString 方法，该方法会生成一个字符串，其中包含记录名称、每个字段的名称以及方括号中的相应值。

        因此，实例化一个姓名为 "John Doe"、地址为 "100 Linda Ln." 的 Person 会得到以下 toString 结果：

        `Person[name=John Doe, address=100 Linda Ln.]`

4. 构造函数

    虽然已为我们生成了公共构造函数，但我们仍然可以自定义构造函数的实现。

    这种定制的目的是用于验证，应尽可能保持简单。

    例如，我们可以使用下面的构造函数实现来确保提供给 Person 记录的姓名和地址不是空值：

    ```java
    public record Person(String name, String address) {
        public Person {
            Objects.requireNonNull(name);
            Objects.requireNonNull(address);
        }
    }
    ```

    我们还可以通过提供不同的参数列表来创建具有不同参数的新构造函数：

    ```java
    public record Person(String name, String address) {
        public Person(String name) {
            this(name, "Unknown");
        }
    }
    ```

    与类构造函数一样，字段可以使用 this 关键字引用（例如，this.name 和 this.address），参数与字段的名称（即名称和地址）相匹配。

    请注意，使用与生成的公共构造函数相同的参数创建构造函数也是有效的，但这需要手动初始化每个字段：

    ```java
    public record Person(String name, String address) {
        public Person(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }
    ```

    此外，声明一个紧凑的构造函数和一个参数列表与生成的构造函数相匹配的构造函数会导致编译错误。

    因此，以下代码将无法编译：

    ```java
    public record Person(String name, String address) {
        public Person {
            Objects.requireNonNull(name);
            Objects.requireNonNull(address);
        }
        
        public Person(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }
    ```

5. 静态变量和方法

    与普通 Java 类一样，我们也可以在记录中包含静态变量和方法。

    我们使用与类相同的语法声明静态变量：

    `public static String UNKNOWN_ADDRESS = "Unknown";`

    同样，我们使用与类相同的语法声明静态方法：

    `public static Person unnamed(String address) {}`

    这样，我们就可以使用记录的名称来引用静态变量和静态方法：

    ```java
    Person.UNKNOWN_ADDRESS
    Person.unnamed("100 Linda Ln.");
    ```

6. 结论

    在本文中，我们研究了 Java 14 中引入的记录关键字，包括其基本概念和复杂性。

    使用记录及其编译器生成的方法，我们可以减少模板代码，提高不可变类的可靠性。

## Relevant articles

- [Guide to the @Serial Annotation in Java 14](https://www.baeldung.com/java-14-serial-annotation)
- [Java Text Blocks](https://www.baeldung.com/java-text-blocks)
- [Pattern Matching for instanceof in Java 14](https://www.baeldung.com/java-pattern-matching-instanceof)
- [Helpful NullPointerExceptions in Java 14](https://www.baeldung.com/java-14-nullpointerexception)
- [Foreign Memory Access API in Java 14](https://www.baeldung.com/java-foreign-memory-access)
- [Java 14 Record Keyword](https://www.baeldung.com/java-record-keyword)
- [New Features in Java 14](https://www.baeldung.com/java-14-new-features)
- [ ] [Java 14 Record vs. Lombok](https://www.baeldung.com/java-record-vs-lombok)
- [Record vs. Final Class in Java](https://www.baeldung.com/java-record-vs-final-class)
- [Custom Constructor in Java Records](https://www.baeldung.com/java-records-custom-constructor)

## Code

本文的代码和示例可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-14) 上找到。

- [ ] Run RecordVsLombokUnitTest failure!
