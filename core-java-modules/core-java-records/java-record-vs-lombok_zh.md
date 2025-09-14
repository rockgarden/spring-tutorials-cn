# [Java Record 与 Lombok 对比](https://www.baeldung.com/java-record-vs-lombok)

核心 Java · Lombok · Record  

1. 概述

    Java 的 `record` 关键字是 Java 14 引入的一项新语义特性，非常适合用于创建小型不可变对象。而 **Lombok** 是一个 Java 库，可通过生成字节码自动实现一些常见模式。虽然两者都能减少样板代码，但它们是不同的工具，应根据具体场景选择更适合的方案。

    本文将探讨多种使用场景，包括 Java Record 的一些局限性。每个示例中，我们将展示 Lombok 如何发挥作用，并对比两种解决方案。

2. 小型不可变对象

    第一个示例，我们使用 `Color` 对象。一个颜色由三个整数值组成，分别代表红、绿、蓝通道。此外，颜色对象应提供其十六进制表示（例如 RGB(255,0,0) 对应 #FF0000）。同时，我们希望两个颜色对象在 RGB 值相同时被视为相等。

    在这种情况下，使用 `record` 是非常自然的选择：

    ```java
    public record ColorRecord(int red, int green, int blue) {

        public String getHexString() {
            return String.format("#%02X%02X%02X", red, green, blue);
        }
    }
    ```

    同样，Lombok 通过 `@Value` 注解也能创建不可变对象：

    ```java
    @Value
    public class ColorValueObject {
        int red;
        int green;
        int blue;

        public String getHexString() {
            return String.format("#%02X%02X%02X", red, green, blue);
        }
    }
    ```

    但从 Java 14 开始，对于这类场景，Record 是更符合语言设计趋势的首选。

3. 透明数据载体

    根据 [JDK 增强提案 JEP 395](https://openjdk.org/jeps/395)，Record 是作为“透明不可变数据载体”的类。因此，**Record 无法隐藏其成员字段**。例如，我们无法让上文的 `ColorRecord` 只暴露 `hexString`，而完全隐藏三个整型字段。

    而 Lombok 允许我们自定义 getter 的名称、访问级别和返回类型。我们可以这样调整 `ColorValueObject`：

    ```java
    @Value
    @Getter(AccessLevel.NONE)
    public class ColorValueObject {
        int red;
        int green;
        int blue;

        public String getHexString() {
            return String.format("#%02X%02X%02X", red, green, blue);
        }
    }
    ```

    因此，如果我们需要的是**不可变数据对象**，Record 是理想选择。

    但如果希望**隐藏字段，仅暴露基于字段的操作方法**，Lombok 会更合适。

4. 包含大量字段的类

    我们已看到 Record 在创建小型不可变对象时非常便捷。但如果数据模型字段较多呢？以 `Student` 数据模型为例：

    ```java
    public record StudentRecord(
    String firstName,
    String lastName,
    Long studentId,
    String email,
    String phoneNumber,
    String address,
    String country,
    int age) {
    }
    ```

    可以预见，实例化 `StudentRecord` 时代码将难以阅读和理解，尤其当某些字段可选时：

    ```java
    StudentRecord john = new StudentRecord(
    "John", "Doe", null, "john@doe.com", null, null, "England", 20);
    ```

    为解决此类问题，Lombok 提供了**构建者（Builder）设计模式**的实现。

    只需为类添加 `@Builder` 注解：

    ```java
    @Getter
    @Builder
    public class StudentBuilder {
        private String firstName;
        private String lastName;
        private Long studentId;
        private String email;
        private String phoneNumber;
        private String address;
        private String country;
        private int age;
    }
    ```

    现在，使用 `StudentBuilder` 创建相同对象：

    ```java
    StudentBuilder john = StudentBuilder.builder()
    .firstName("John")
    .lastName("Doe")
    .email("john@doe.com")
    .country("England")
    .age(20)
    .build();
    ```

    对比可见，使用构建者模式代码更清晰易读。

    **结论**：Record 更适合字段较少的对象；对于字段众多的对象，由于缺乏创建模式支持，Lombok 的 `@Builder` 是更优选择。

5. 可变数据

    Java Record **仅适用于不可变数据**。如果需要可变对象，可以使用 Lombok 的 `@Data`：

    ```java
    @Data
    @AllArgsConstructor
    public class ColorData {

        private int red;
        private int green;
        private int blue;

        public String getHexString() {
            return String.format("#%02X%02X%02X", red, green, blue);
        }

    }
    ```

    某些框架（如 Hibernate）要求对象具备 setter 方法或默认构造函数。在定义 `@Entity` 时，必须使用 Lombok 注解或纯 Java 实现。

6. 继承

    Java Record **不支持继承**，既不能被其他类继承，也不能继承其他类。

    而 Lombok 的 `@Value` 对象虽然为 `final`，但可以继承其他类：

    ```java
    @Value
    public class MonochromeColor extends ColorData {

        public MonochromeColor(int grayScale) {
            super(grayScale, grayScale, grayScale);
        }
    }
    ```

    此外，`@Data` 对象既可以继承其他类，也可以被继承。

    **结论**：如需继承功能，应选择 Lombok 的解决方案。

7. 结论

    本文展示了 Lombok 与 Java Record 是用途不同的工具。Lombok 更加灵活，适用于 Record 存在限制的场景。

    在实际开发中：

    - 优先使用 **Record** 创建**小型、不可变、透明数据载体**。
    - 在需要**构建者模式、可变对象、继承、字段隐藏**等场景时，选择 **Lombok**。

    两者并非互斥，而是互补。根据项目需求合理搭配，才能最大化开发效率与代码可维护性。
