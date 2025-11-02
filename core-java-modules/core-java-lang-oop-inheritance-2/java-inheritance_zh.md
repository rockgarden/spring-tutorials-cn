# [Java中的继承指南](https://www.baeldung.com/java-inheritance)

1. 概述

    面向对象编程（OOP）的核心原则之一——**继承**（inheritance）——使我们能够复用现有代码或扩展现有类型。

    简单来说，在 Java 中，一个类可以继承另一个类以及多个接口，而一个接口可以继承其他接口。

    在本文中，我们将从继承的需求出发，探讨继承在类和接口中是如何工作的。

    接着，我们将介绍变量/方法名称以及访问修饰符如何影响被继承的成员。

    最后，我们将探讨“继承一个类型”究竟意味着什么。

2. 继承的需求  

    假设你是一家汽车制造商，向客户提供多种汽车型号。尽管不同的车型可能提供不同的功能（例如天窗或防弹玻璃），但它们都会包含一些通用的组件和功能，比如发动机和车轮。

    与其从零开始为每个车型单独设计，不如创建一个基础设计，然后在此基础上扩展出各种专用版本，这样做更为合理。

    同样地，通过继承，我们可以创建一个包含基本功能和行为的类，然后通过创建继承该基类的子类来实现其专用版本。接口也可以通过扩展现有接口来实现类似的效果。

    我们会注意到，对于被其他类型继承的类型，有多种术语用来指代它，具体如下：

    - **基类型**（base type）也被称为 **超类型**（super type）或 **父类型**（parent type）  
    - **派生类型**（derived type）则被称为 **扩展类型**（extended type）、**子类型**（sub type）或 **子类**（child type）
      - **sub type（子类型）**：更侧重**类型系统**和**多态性**的概念，强调“Liskov 替换原则”下的类型兼容关系（即子类型对象可替换父类型引用）。这是面向对象理论中的标准术语。
      - **child type（子类 / 子类型）**：更侧重**继承结构**中的**父子关系**，常用于描述类层次（class hierarchy），尤其在与 “parent class” 对应时，译为“子类”更自然。

3. 类的继承

    1. 扩展一个类

        一个类可以继承另一个类并定义额外的成员。

        让我们从定义一个基类Car开始：

        ![Car.java](./src/main/java/com/baeldung/inheritance/Car.java)

        ArmoredCar类可以通过在其声明中使用关键字extends来继承Car类的成员：

        ```java
        public class ArmoredCar extends Car {
            int bulletProofWindows;
            void remoteStartCar() {
            // this vehicle can be started by using a remote control
            }
        }
        ```

        我们现在可以说，ArmoredCar类是Car的子类，而后者是ArmoredCar的超类。

        Java中的类支持单继承；ArmoredCar类不能扩展多个类。

        另外，请注意，在没有extends关键字的情况下，一个类隐含地继承了java.lang.Object类。

        一个子类继承了超类中的非静态保护成员和公共成员(non-static protected and public members)。此外，如果两个类在同一个包中，具有默认（package-private）访问权的成员也被继承。

        另一方面，一个类的私有和静态成员不被继承。

    2. 从子类中访问父类成员

        要访问继承的属性或方法，我们可以简单地直接使用它们：

        ```java
        public class ArmoredCar extends Car {
            public String registerModel() {
                return model; // 未定义
            }
        }
        ```

        注意，我们不需要对超类的引用来访问其成员。

4. 接口继承

    1. 实现多个接口

        尽管类只能继承一个类，但它们可以实现多个接口。

        想象一下，我们在上一节中定义的ArmoredCar是需要一个超级间谍的。所以汽车制造公司想到了增加飞行和漂浮功能：

        ![Floatable.java](./src/main/java/com/baeldung/inheritance/Floatable.java)

        ![Flyable.java](./src/main/java/com/baeldung/inheritance/Flyable.java)

        ![ArmoredCar.java](./src/main/java/com/baeldung/inheritance/ArmoredCar.java)

        在上面的例子中，我们注意到使用了关键字 implements 来继承一个接口。

    2. 多重继承的问题

        Java允许使用接口进行多重继承。

        在Java 7之前，这并不是一个问题。接口只能定义抽象的方法，也就是没有任何实现的方法。因此，如果一个类用相同的方法签名实现了多个接口，这并不是一个问题。实现类最终只有一个方法需要实现。

        让我们看看这个简单的等式是如何随着Java 8在接口中引入默认方法而发生变化的。

        从Java 8开始，接口可以选择为其方法定义默认的实现（接口仍然可以定义抽象的方法）。这意味着，如果一个类实现了多个接口，而这些接口定义了具有相同签名的方法，那么子类将继承不同的实现。这听起来很复杂，是不允许的。

        Java不允许继承在不同接口中定义的同一方法的多个实现。

        下面是一个例子：

        ```java
        public interface Floatable {
            default void repair() {
                System.out.println("Repairing Floatable object");
            }
        }
        public interface Flyable {
            default void repair() {
                System.out.println("Repairing Flyable object");
            }
        }
        public class ArmoredCar extends Car implements Floatable, Flyable {
            // this won't compile
        }
        ```

        如果我们确实想实现这两个接口，我们就必须重写 repair() 方法。

        如果前面的例子中的接口定义了同名的变量，比如说持续时间，那么如果不在变量名称前加上接口名称，我们就无法访问它们：

        ```java
        public interface Floatable {
            int duration = 10;
        }
        public interface Flyable {
            int duration = 20;
        }
        public class ArmoredCar extends Car implements Floatable, Flyable {
            public void aMethod() {
                System.out.println(duration); // won't compile
                System.out.println(Floatable.duration); // outputs 10
                System.out.println(Flyable.duration); // outputs 20
            }
        }
        ```

    3. 接口扩展其他接口

        一个接口可以扩展多个接口。下面是一个例子：

        ![Floatable.java](./src/main/java/com/baeldung/inheritance/Floatable.java)

        ![Flyable.java](./src/main/java/com/baeldung/inheritance/Flyable.java)

        ![SpaceTraveller.java](./src/main/java/com/baeldung/inheritance/SpaceTraveller.java)

        一个接口通过使用关键字extends来继承其他接口。类使用关键字 implements 来继承一个接口。

5. 继承类型

    当一个类继承另一个类或接口时，除了继承它们的成员外，它还继承它们的类型。这也适用于一个继承其他接口的接口。

    这是一个非常强大的概念，它允许开发者对一个接口（基类或接口）进行编程，而不是对其实现进行编程。

    例如，想象一下这样一种情况：一个组织维护着其员工所拥有的汽车的列表。当然，所有员工可能拥有不同的汽车型号。那么，我们怎样才能引用不同的汽车实例呢？这里有一个解决方案：

    ![Employee.java](./src/main/java/com/baeldung/inheritance/Employee.java)

    因为Car的所有派生类都继承了Car的类型，派生类的实例可以通过使用Car类的变量来引用：

    ```java
    Employee e1 = new Employee("Shreya", new ArmoredCar());
    Employee e2 = new Employee("Paul", new SpaceCar());
    Employee e3 = new Employee("Pavni", new BMW());
    ```

6. 隐藏的类成员

    1. 隐藏的实例成员

        如果超类和子类都定义了一个同名的变量或方法会怎样？别担心，我们仍然可以同时访问它们。然而，我们必须向Java表明我们的意图，在变量或方法前加上关键字this或super。

        this关键字指的是它所使用的实例。super关键字（似乎很明显）指的是父类实例：

        inheritance/ArmoredCar.java: getAValue()

        ```java
        public class ArmoredCar extends Car {
            private String model;
            public String getAValue() {
                return super.model;   // returns value of model defined in base class Car
                // return this.model;   // will return value of model defined in ArmoredCar
                // return model;   // will return value of model defined in ArmoredCar
            }
        }
        ```

        很多开发者使用this和super关键字来明确说明他们所指的是哪个变量或方法。然而，对所有成员使用它们会使我们的代码看起来很杂乱。

    2. 隐藏的静态成员

        当我们的基类和子类用相同的名字定义静态变量和方法时会发生什么？我们可以像访问实例变量那样，在派生类中访问基类中的静态成员吗？

        让我们通过一个例子来了解一下：

        inheritance/Car.java: String msg()

        ```java
        public class ArmoredCar extends Car {
            public static String msg() {
                return super.msg(); // this won't compile.
            }
        }
        ```

        不，我们不能。静态成员属于一个类而不是实例。所以我们不能在msg()中使用非静态的super关键字。

        由于静态成员属于一个类，我们可以将前面的调用修改如下：

        `return Car.msg();`

        考虑下面的例子，基类和派生类都定义了一个具有相同签名的静态方法msg()：

        inheritance/Car.java: String msg()

        ```java
        public class Car {
            public static String msg() {
                return "Car";
            }
        }
        ```

        inheritance/ArmoredCar.java: String msg()

        ```java
        public class ArmoredCar extends Car {
            public static String msg() {
                return "ArmoredCar";
            }
        }
        ```

        下面是我们如何调用它们：

        ```java
        Car first = new ArmoredCar();
        ArmoredCar second = new ArmoredCar();
        ```

        对于前面的代码，first.msg()将输出 "Car"， second.msg()将输出 "ArmoredCar"。被调用的静态信息取决于用于引用ArmoredCar实例的变量的类型。

7. 总结

    在这篇文章中，我们涵盖了Java语言的一个核心方面--继承。

    我们看到了Java是如何支持类的单继承和接口的多继承的，并讨论了该机制在语言中的错综复杂的工作方式。
