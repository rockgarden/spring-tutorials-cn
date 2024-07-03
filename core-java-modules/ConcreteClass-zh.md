# Java中的具体类

1. 简介

    在这个快速指南中，我们将讨论Java中的 "concrete class"一词。

    首先，我们将定义这个术语。然后，我们将看到它与接口和抽象类有什么不同。

2. 什么是具体类？

    具体类是一个我们可以使用new关键字来创建实例的类。

    换句话说，它是其蓝图的完整实现。一个具体的类是完整的。

    想象一下，比如说，一个汽车类：

    ```java
    public class Car {
        public String honk() {
            return "beep!";
        }

        public String drive() {
            return "vroom";
        }
    }
    ```

    因为它的所有方法都被实现了，我们称它为一个具体的类，我们可以实例化它：

    `Car car = new Car();`

    JDK中的一些具体类的例子是HashMap、HashSet、ArrayList和LinkedList。

3. Java抽象类与具体类

    不过，并非所有的Java类型都实现了它们的所有方法。这种灵活性，也被称为抽象性(abstraction)，允许我们用更普遍的术语来思考我们试图建模的领域。

    在Java中，我们可以使用接口和抽象类来实现抽象性。

    让我们通过比较具体的类和这些其他的类来更好地了解它们。

    1. 接口

        一个接口是一个类的蓝图(blueprint)。或者，换句话说，它是一个未实现的方法签名的集合：

        ```java
        interface Driveable {
            void honk();
            void drive();
        }
        ```

        注意，它使用了接口关键字而不是类。

        因为Driveable有未实现的方法，我们不能用new关键字将其实例化。

        但是，像Car这样的具体类可以实现这些方法。

        JDK提供了许多接口，如Map、List和Set。

    2. 抽象类

        抽象类是一个拥有未实现方法的类，尽管它实际上可以同时拥有两种方法：

        ```java
        public abstract class Vehicle {
            public abstract String honk();

            public String drive() {
                return "zoom";
            }
        }
        ```

        注意，我们用关键字abstract来标记抽象类。

        同样，由于Vehicle有一个未实现的方法honk，我们将不能使用new关键字。

        JDK中的一些抽象类的例子是AbstractMap和AbstractList。

    3. 具体类

        相比之下，具体类没有任何未实现的方法。无论实现是否被继承，只要每个方法都有一个实现，这个类就是具体的。

        具体的类可以像我们前面的Car例子那样简单。它们也可以实现接口并扩展抽象类：

        ```java
        public class FancyCar extends Vehicle implements Driveable {
            public String honk() { 
                return "beep";
            }
        }
        ```

        FancyCar类提供了一个honk的实现，它从Vehicle继承了drive的实现。

        因此，它没有未实现的方法。因此，我们可以用new关键字创建一个FancyCar类实例。

        `FancyCar car = new FancyCar();`

        或者，简单地说，所有不是抽象的类，我们都可以称之为具体类。

4. 总结

    在这个简短的教程中，我们了解了具体类和它们的规范。

    此外，我们还展示了接口与具体类和抽象类之间的区别。

## Relevant Articles

- [x] [Concrete Class in Java](https://www.baeldung.com/java-concrete-class)
