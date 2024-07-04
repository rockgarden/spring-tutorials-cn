# 核心Java语言OOP-其他

本模块包含有关 Java 面向对象编程 (OOP) 的文章

- [Java中的面向对象编程概念](#java中面向对象的编程概念)
- [Java中的静态绑定和动态绑定](https://www.baeldung.com/java-static-dynamic-binding)
- [ ] [Java中作为参数传递机制的"逐值传递"](#在java中作为参数传递机制的逐值传递)
- [检查对象的所有变量是否为空](https://www.baeldung.com/java-check-all-variables-object-null)
- [Java中的得墨忒耳定律](https://www.baeldung.com/java-demeter-law)

## Java中面向对象的编程概念

1. 概述

    本文将介绍 Java 中的面向对象编程（OOP）概念。我们将讨论类、对象、抽象、封装、继承和多态性。

2. 类

    类是所有对象的起点，我们可以将其视为创建对象的模板。一个类通常包含成员字段、成员方法和一个特殊的构造方法。

    我们将使用构造函数来创建类的对象：

    Car.java

    请注意，一个类可能有不止一个构造函数。有关类的更多信息，请参阅我们的[类文章](https://www.baeldung.com/java-classes-objects#classes)。

3. 对象

    对象由类创建，称为类的实例。我们使用类的构造函数从类中创建对象：

    `Car veyron = new Car("Bugatti", "Veyron", "crimson");`

    在这里，我们创建了 Car 类实例。有关对象的更多信息，请参阅[对象文章](https://www.baeldung.com/java-classes-objects#objects)。

4. 抽象

    抽象就是隐藏复杂的实现并公开更简单的接口。

    如果我们想想一台典型的计算机，我们只能看到外部接口，这是与计算机进行交互的最基本要素，而内部芯片和电路则不为用户所知。

    在 OOP 中，抽象意味着隐藏程序的复杂实现细节，只公开使用实现所需的应用程序接口。在 Java 中，我们通过使用接口和抽象类来实现抽象

    有关抽象的更多信息，请参阅我们的[抽象类](https://www.baeldung.com/java-abstract-class)和[接口文章](https://www.baeldung.com/java-interfaces)。

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

    在这里，字段 speed 使用私有访问修饰符封装，只能使用公共 getSpeed() 和 setSpeed() 方法访问。有关访问修饰符的更多信息，请参阅访问[修饰符文章](https://www.baeldung.com/java-access-modifiers)。

6. 继承

    继承是一种机制，它允许一个类通过继承另一个类来获得该类的所有属性。我们将继承的类称为子类，将被继承的类称为超类或父类。

    在 Java 中，我们通过扩展父类来实现这一点。这样，子类就获得了父类的所有属性：

    `public class Car extends Vehicle {}`

    当我们扩展一个类时，就形成了一种 [IS-A](https://www.baeldung.com/java-inheritance-composition) 关系。汽车 IS-A 车辆。因此，它具有车辆的所有特性。

    我们可能会问，为什么需要继承？要回答这个问题，让我们考虑一个汽车制造商，他生产不同类型的汽车，如轿车、公共汽车、有轨电车和卡车。

    为了方便工作，我们可以将所有车辆类型的共同特征和属性捆绑到一个模块（Java 中是一个类）中。我们可以让各个类型继承和重用这些属性：

    `public class Vehicle {}`

    现在，车辆类型 Car 将继承自父车辆类。

    Java 支持单层继承和多层继承。这意味着一个类不能直接从多个类扩展，但可以使用层次结构：

    `public class ArmoredCar extends Car {}`

    在这里，ArmouredCar 扩展了 Car，而 Car 扩展了 Vehicle。因此，ArmouredCar 继承了 Car 和 Vehicle 的属性。

    虽然我们从父类继承，但开发人员也可以覆盖父类的方法实现。这就是所谓的[方法覆盖](https://www.baeldung.com/java-method-overload-override#method-overriding)。

    在上述车辆类的示例中，有一个 honk() 方法。扩展了 Vehicle 类的 Car 类可以覆盖该方法，并以自己想要的方式实现按喇叭的效果：

    ```java
    public class Car extends Vehicle {  
        //...
        @Override
        public void honk() {}
    }
    ```

    请注意，这也被称为运行时多态性。有关继承的更多信息，请参阅[Java 继承](https://www.baeldung.com/java-inheritance)和[继承与组成](https://www.baeldung.com/java-inheritance-composition)的文章。

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

    有关多态性的更多信息，请参阅[Java中的多态性](https://www.baeldung.com/java-polymorphism)文章。

8. 结论

    在本文中，我们学习了 Java OOP 的基本概念。

## 在Java中作为参数传递机制的"逐值传递"

1. 简介

    向方法传递参数的两种最普遍的模式是 "按值传递(Pass-by-Value)" 和 "按参考传递(Pass-by-Reference)"。不同的编程语言以不同的方式使用这些概念。就Java而言，一切都是严格的逐值传递。

    在本教程中，我们将说明Java如何传递各种类型的参数。

2. 逐值传递与逐指传递

    让我们先来看看向函数传递参数的一些不同机制。

    - 值 value
    - 参考 reference
    - 结果 result
    - 值-结果 value-result
    - 名称 name

    现代编程语言中最常见的两种机制是 "逐值传递" 和 "逐指传递"。在我们继续之前，让我们先讨论一下这些。

    1. 逐值传递

        当一个参数是逐值传递时，调用者和被调用者方法对两个不同的变量进行操作，这些变量是彼此的副本。对一个变量的任何改变都不会修改另一个。

        这意味着在调用一个方法时，传递给被调用方法的参数将是原始参数的克隆。在被调用者方法中做的任何修改都不会对调用者方法中的原始参数产生影响。

    2. 通过引用的传递

        当一个参数是通过引用的时候，调用者和被调用者在同一个对象上操作。

        这意味着，当一个变量被传递给参考时，该对象的唯一标识符被发送到方法中。对参数的实例成员的任何改变都会导致对原始值的这种改变。

3. Java中的参数传递

    任何编程语言的基本概念都是 "值values" 和 "引用references"。在Java中，原始变量存储实际值，而非原始变量存储引用变量，这些变量指向它们所引用的对象的地址。值和引用都存储在堆栈内存中。

    Java中的参数总是按值传递的。在方法调用过程中，每个参数的副本，无论是值还是引用，都会在堆栈内存中创建，然后传递给方法。

    如果是基元(primitives)，值被简单地复制到堆栈内存中，然后传递给被调用的方法；如果是非基元(non-primitives)，堆栈内存中的引用指向驻留在堆中的实际数据。当我们传递一个对象时，堆栈内存中的引用被复制，新的引用被传递给方法。

    现在让我们在一些代码例子的帮助下看看这个动作。

    1. 传递原始类型

        Java编程语言有八个[原始数据类型](https://www.baeldung.com/java-primitives)。原始变量直接存储在堆栈内存中。每当任何原始数据类型的变量被作为参数传递时，实际参数被复制到形式参数，这些形式参数在堆栈内存中积累了自己的空间。

        这些形式参数的寿命只持续到该方法运行时，在返回时，这些形式参数会从堆栈中清除并被丢弃。

        让我们试着借助一个代码例子来理解它。

        parameterpassing/PrimitivesUnitTest.java

        让我们试着通过分析这些值是如何存储在内存中的，来理解上述程序中的断言。

        - 主方法中的变量 "x" 和 "y" 是原始类型，它们的值直接存储在堆栈内存中。
        - 当我们调用方法modify()时，这些变量的每一个副本都被创建并存储在堆栈内存中的不同位置。
        - 对这些副本的任何修改都只影响它们，而使原始变量不被改变。

        ![pass by value primitives](./pic/baeldung_-_pass_by_value_-_passing_primitives.jpg)

    2. 传递对象引用

        在Java中，所有的对象都动态地存储在引擎盖下的Heap空间中。这些对象从被称为引用变量的参考中被引用。

        一个Java对象，与Primitives相反，是分两个阶段存储的。引用变量被存储在堆栈内存中，而它们所引用的对象则被存储在堆内存中。

        每当一个对象被作为参数传递时，就会创建一个引用变量的精确副本，它与原始引用变量一样，指向堆内存中的对象的相同位置。

        因此，每当我们在方法中对同一对象做任何改变时，这种改变就会反映在原始对象上。然而，如果我们为传递的引用变量分配一个新的对象，那么它将不会反映在原始对象中。

        让我们借助于一个代码例子来理解这个问题。

        parameterpassing/NonPrimitivesUnitTest.java

        让我们分析一下上述程序中的断言。我们在modify()方法中传递了具有相同值1的对象a和b。最初，这些对象引用是指向堆(heap)空间中两个不同的对象位置。

        ![pass by value initial](./pic/baeldung_-_pass_by_value_-_passing_primitives_-_initial.jpg)

        当这些引用a和b被传递到modify()方法中时，它创建了这些引用a1和b1的镜像副本，它们指向相同的旧对象。

        ![_before_method_ca](./pic/baeldung_-_pass_by_value_-_passing_primitives_-_before_method_ca.jpg)

        在modify()方法中，当我们修改引用a1时，它改变了原始对象。然而，对于引用b1，我们已经分配了一个新的对象。所以它现在是指向堆内存中的一个新对象。

        对b1所做的任何改变都不会反映在原始对象中。

        ![_after_method_cal](./pic/baeldung_-_pass_by_value_-_passing_primitives_-_after_method_cal.jpg)

4. 总结

    在这篇文章中，我们看了在基元和非基元的情况下是如何处理参数传递的。

    我们了解到，Java中的参数传递始终是逐值传递。然而，根据我们处理的是基元还是对象，情况会发生变化。

    - 对于基元类型，参数是逐值传递的。
    - 对于对象类型，对象引用是逐值传递的。

## 原文

- [Object-Oriented-Programming Concepts in Java](https://www.baeldung.com/java-oop)
- [Static and Dynamic Binding in Java](https://www.baeldung.com/java-static-dynamic-binding)
- [x] [Pass-By-Value as a Parameter Passing Mechanism in Java](https://www.baeldung.com/java-pass-by-value-or-pass-by-reference)
- [Check If All the Variables of an Object Are Null](https://www.baeldung.com/java-check-all-variables-object-null)
- [Law of Demeter in Java](https://www.baeldung.com/java-demeter-law)

## Code

本文中使用的代码片段可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-oop-others)上找到。
