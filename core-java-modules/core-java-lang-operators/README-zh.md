# 核心 Java 操作符

## Java 中的钻石操作符指南

1. 概述

    本文将介绍 Java 中的菱形运算符，以及[泛型](https://www.baeldung.com/java-generics)和集合 API 如何影响了菱形运算符的演变。

2. 原始类型

    在 Java 1.5 之前，Collections API 仅支持原始类型--在构造集合时，无法对类型参数进行参数化：

    ```java
    List cars = new ArrayList();
    cars.add(new Object());
    cars.add("car");
    cars.add(new Integer(1));
    ```

    这样就可以添加任何类型，但在运行时可能会出现构造异常。

3. 泛型

    Java 1.5 引入了 "泛型"（Generics），允许我们在声明和构造对象时为类的类型参数（包括集合 API 中的类参数）设置参数：

    `List<String> cars = new ArrayList<String>();`

    此时，我们必须在构造函数中指定参数化的类型，这可能有点难以理解：

    `Map<String, List<Map<String, Map<String, Integer>>>> cars = new HashMap<String, List<Map<String, Map<String, Integer>>>>();`

    采用这种方法的原因是，为了向后兼容，原始类型仍然存在，因此编译器需要区分这些原始类型和泛型：

    ```java
    List<String> generics = new ArrayList<String>();
    List<String> raws = new ArrayList();
    ```

    尽管编译器仍然允许我们在构造函数中使用原始类型，但它会提示我们一条警告信息：

    `ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized`

4. 钻石操作符

    在 Java 1.7 中引入的钻石运算符增加了类型推断功能，并在使用泛型时减少了赋值的繁琐程度：

    `List<String> cars = new ArrayList<>();`

    Java 1.7 编译器的类型推断功能会根据调用确定最合适的构造函数声明。

    下面是处理车辆和发动机的接口和类层次结构：

    ```java
    public interface Engine { }
    public class Diesel implements Engine { }
    public interface Vehicle<T extends Engine> { }
    public class Car<T extends Engine> implements Vehicle<T> { }
    ```

    让我们使用 diamond 运算符创建一个新的 Car 实例：

    `Car<Diesel> myCar = new Car<>();`

    在内部，编译器知道 Diesel 实现了 Engine 接口，然后能够通过推断类型来确定合适的构造函数。

5. 结论

    简而言之，菱形操作符为编译器增加了类型推断功能，减少了泛型赋值中的繁琐。

## 相关文章

- [x] [Guide to the Diamond Operator in Java](https://www.baeldung.com/java-diamond-operator)
- [Ternary Operator in Java](https://www.baeldung.com/java-ternary-operator)
- [The Modulo Operator in Java](https://www.baeldung.com/modulo-java)
- [Java instanceof Operator](https://www.baeldung.com/java-instanceof)
- [A Guide to Increment and Decrement Unary Operators in Java](https://www.baeldung.com/java-unary-operators)
- [Java Compound Operators](https://www.baeldung.com/java-compound-operators)
- [The XOR Operator in Java](https://www.baeldung.com/java-xor-operator)
- [Java Bitwise Operators](https://www.baeldung.com/java-bitwise-operators)
- [Bitwise & vs Logical && Operators](https://www.baeldung.com/java-bitwise-vs-logical-and)
- [Finding an Object’s Class in Java](https://www.baeldung.com/java-finding-class)

## Code

本教程的一些示例可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-operators) 项目中找到，请随时下载并使用。
