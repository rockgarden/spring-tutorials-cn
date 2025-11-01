# [Java 接口中的私有方法](https://www.baeldung.com/java-interface-private-methods)

核心 Java（≥ Java 8）| Java 接口

1. 概述

    从 **Java 9** 开始，可以在 Java 接口中添加[私有方法](http://openjdk.java.net/jeps/213)。在本篇简短教程中，我们将讨论如何定义这些方法及其优势。

2. 在接口中定义私有方法

    私有方法可以是静态的，也可以是非静态的。这意味着在接口中，我们可以创建私有方法，以封装来自默认方法和静态公共方法的代码逻辑。

    首先，我们来看如何在默认接口方法中使用私有方法：

    ```java
    public interface Foo {

        default void bar() {
            System.out.print("Hello");
            baz();
        }

        private void baz() {
            System.out.println(" world!");
        }
    }
    ```

    `bar()` 方法可以通过其默认方法体调用私有方法 `baz()`。

    接下来，我们在 `Foo` 接口中添加一个静态定义的私有方法：

    ```java
    public interface Foo {

        static void buzz() {
            System.out.print("Hello");
            staticBaz();
        }

        private static void staticBaz() {
            System.out.println(" static world!");
        }
    }
    ```

    在接口内部，其他静态方法可以使用这些私有静态方法。

    最后，我们在一个具体类中调用已定义的默认方法和静态方法：

    ```java
    public class CustomFoo implements Foo {

        public static void main(String... args) {
            Foo customFoo = new CustomFoo();
            customFoo.bar();
            Foo.buzz();
        }
    }
    ```

    输出结果为：  
    调用 `bar()` 方法时打印 “Hello world!”，  
    调用 `buzz()` 方法时打印 “Hello static world!”。

3. 接口中私有方法的优势

    在定义了私有方法之后，我们来谈谈它们的优势。

    如前一节所述，接口可以使用私有方法向实现该接口的类**隐藏实现细节**。因此，私有方法在接口中的主要优势之一就是**封装性**。

    另一个优势是（与一般的私有方法一样）：对于功能相似的方法，接口中可以减少代码重复，并提高代码的可重用性。

4. 结论

    在本教程中，我们介绍了如何在接口中定义私有方法，以及如何在静态和非静态上下文中使用它们。
