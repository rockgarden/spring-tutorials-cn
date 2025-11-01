# [Java中面向对象的编程概念](https://www.baeldung.com/java-oop)

1. 概述

    本文将介绍 Java 中的面向对象编程（OOP）概念。我们将讨论类、对象、抽象、封装、继承和多态性。

2. 类

    类是所有对象的起点，我们可以将其视为创建对象的模板。一个类通常包含成员字段、成员方法和一个特殊的构造方法。

    我们将使用构造函数来创建类的对象：

    Car.java

    请注意，一个类可能有不止一个构造函数。

3. 对象

    对象由类创建，称为类的实例。我们使用类的构造函数从类中创建对象：

    `Car veyron = new Car("Bugatti", "Veyron", "crimson");`

    在这里，我们创建了 Car 类实例。

4. 抽象

    抽象就是隐藏复杂的实现并公开更简单的接口。

    如果我们想想一台典型的计算机，我们只能看到外部接口，这是与计算机进行交互的最基本要素，而内部芯片和电路则不为用户所知。

    在 OOP 中，抽象意味着隐藏程序的复杂实现细节，只公开使用实现所需的应用程序接口。在 Java 中，我们通过使用接口和抽象类来实现抽象。

5. 封装

    封装是向 API 消费者隐藏对象的状态或内部表示，并提供与对象绑定的可公开访问的读写访问方法。这允许隐藏特定信息并控制对内部实现的访问。

    例如，一个类中的成员字段对其他类是隐藏的，可以使用成员方法访问它们。一种方法是将所有数据字段设置为私有，只有使用公共成员方法才能访问：

    ```java
    public class Car {
        // ...
        private int speed;
        public int getSpeed() {}
        public void setSpeed(int speed) {}
        // ...
    }
    ```

    在这里，字段 speed 使用私有访问修饰符封装，只能使用公共 getSpeed() 和 setSpeed() 方法访问。

6. 继承

    继承是一种机制，它允许一个类通过继承另一个类来获得该类的所有属性。我们将继承的类称为子类，将被继承的类称为超类或父类。

    在 Java 中，我们通过扩展父类来实现这一点。这样，子类就获得了父类的所有属性：

    `public class Car extends Vehicle {}`

    当我们扩展一个类时，就形成了一种 IS-A 关系。汽车 IS-A 车辆。因此，它具有车辆的所有特性。

    我们可能会问，为什么需要继承？要回答这个问题，让我们考虑一个汽车制造商，他生产不同类型的汽车，如轿车、公共汽车、有轨电车和卡车。

    为了方便工作，我们可以将所有车辆类型的共同特征和属性捆绑到一个模块（Java 中是一个类）中。我们可以让各个类型继承和重用这些属性：

    `public class Vehicle {}`

    现在，车辆类型 Car 将继承自父车辆类。

    Java 支持单层继承和多层继承。这意味着一个类不能直接从多个类扩展，但可以使用层次结构：

    `public class ArmoredCar extends Car {}`

    在这里，ArmouredCar 扩展了 Car，而 Car 扩展了 Vehicle。因此，ArmouredCar 继承了 Car 和 Vehicle 的属性。

    虽然我们从父类继承，但开发人员也可以覆盖父类的方法实现。这就是所谓的方法覆盖。

    在上述车辆类的示例中，有一个 honk() 方法。扩展了 Vehicle 类的 Car 类可以覆盖该方法，并以自己想要的方式实现按喇叭的效果：

    ```java
    public class Car extends Vehicle {  
        //...
        @Override
        public void honk() {}
    }
    ```

    请注意，这也被称为运行时多态性。

7. 多态性

    多态性([polymorphism](https://www.baeldung.com/cs/polymorphism))是一种 OOP 语言根据输入类型以不同方式处理数据的能力。在 Java 中，这可以是相同的方法名称具有不同的方法签名并执行不同的功能：

    ```java
    public class TextFile extends GenericFile { 
        //...
        public String read() {}
        public String read(int limit) {}
        public String read(int start, int stop) {}
    }
    ```

    在这个示例中，我们可以看到 read() 方法有三种不同的形式，具有不同的功能。这种类型的多态性是静态或编译时多态性，也称为方法重载。

    还有一种运行时或动态多态性，即子类重载父类的方法：

    ```java
    public class GenericFile {
        public String getFileInfo() {
            return "Generic File Impl";
        }
    }
    ```

    子类可以扩展 GenericFile 类并覆盖 getFileInfo() 方法：

    ```java
    public class ImageFile extends GenericFile {
        //... 获取器和设置器
        public String getFileInfo() {
            return "Image File Impl";
        }
    }
    ```

8. 结论

    在本文中，我们学习了 Java OOP 的基本概念。
