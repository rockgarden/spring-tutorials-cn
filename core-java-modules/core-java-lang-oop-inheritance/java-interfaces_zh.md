# [Java 接口](https://www.baeldung.com/java-interfaces)

1. 概述

   在本教程中，我们将讨论 Java 中的接口。我们还将看到 Java 是如何使用接口来实现多态性和多重继承的。

2. 什么是 Java 中的接口？

   在 Java 中，接口是一种抽象的类型，它包含了方法和常量变量的集合。它是 Java 的核心概念之一，用来实现抽象、多态和多继承。

   让我们看看 Java 中接口的一个简单例子：

   ![Electronic.java](./src/main/java/com/baeldung/interfaces/Electronic.java)

   我们可以通过使用 implements 关键字在一个 Java 类中实现一个接口。

   接下来，我们也来创建一个计算机类，实现我们刚刚创建的电子接口：

   ![Computer.java](./src/main/java/com/baeldung/interfaces/Computer.java)

   1. 创建接口的规则

      在一个接口中，我们允许使用：

      - constants variables 常量变量
      - abstract methods 抽象方法
      - static methods 静态方法
      - default methods 默认方法

      我们还应该记住：

      - 我们不能直接对接口进行实例化；
      - 一个接口可以是空的，里面没有方法或变量；
      - 不能在接口定义中使用 `final` 关键字，否则会导致编译错误；
      - 所有的接口声明都应该有`public`或`default`访问修饰词；`abstract`修饰词将由编译器自动添加；
      - 接口方法不能是`protected` 或 `final`的；
      - 在 Java 9 之前，接口方法不能是`private`的；但是，Java 9 引入了在接口中定义私有方法的可能性；
      - 根据定义，接口变量是`public`、`static` 和 `final` 的；我们不允许改变它们的可见性或修饰符。

3. 我们通过使用它们可以实现什么？

   1. 行为上的功能

      我们使用接口来增加某些行为功能，这些功能可以被不相关的类所使用。例如，Comparable、Comparator 和 Cloneable 是可以由不相关的类实现的 Java 接口。下面是一个比较器接口的例子，它被用来比较 Employee 类的两个实例：

      ![Employee.java](./src/main/java/com/baeldung/interfaces/Employee.java)

      ![EmployeeSalaryComparator.java](./src/main/java/com/baeldung/interfaces/EmployeeSalaryComparator.java)

   2. 多重继承

      Java 类支持单一的继承。然而，通过使用接口，我们也能够实现多重继承。

      例如，在下面的例子中，我们注意到汽车类实现了飞翔和转换接口。通过这样做，它继承了飞翔和变换的方法：

      ```java
      public interface Transform {
          void transform();
      }

      public interface Fly {
          void fly();
      }

      public class Car implements Fly, Transform {

          @Override
          public void fly() {
              System.out.println("I can Fly!!");
          }

          @Override
          public void transform() {
              System.out.println("I can Transform!!");
          }
      }
      ```

   3. 多态性

      让我们先问一个问题：什么是多态性？它是指一个对象在运行时采取不同形式的能力。更具体地说，它是在运行时执行与特定对象类型有关的覆盖方法。

      在 Java 中，我们可以通过接口来实现多态性。例如，"形状" 接口可以有不同的形式--它可以是一个圆形或一个方形。

      让我们先来定义 Shape 接口：

      ![Shape.java](./src/main/java/com/baeldung/interfaces/polymorphysim/Shape.java)

      现在我们也来创建 Circle 类：

      ![Circle.java](./src/main/java/com/baeldung/interfaces/polymorphysim/Circle.java)

      还有方形类：

      ![Square.java](./src/main/java/com/baeldung/interfaces/polymorphysim/Square.java)

      最后，是时候使用我们的 Shape 接口和它的实现来看看多态性的作用了。让我们实例化一些 Shape 对象，将它们添加到 List 中，最后，在一个循环中打印它们的名字：

      ![MainTestClass.java](./src/main/java/com/baeldung/interfaces/polymorphysim/MainTestClass.java)

4. 接口中的默认方法

   Java 7 及以下版本的传统接口不提供向后兼容性。

   这意味着，如果你有在 Java 7 或更早写的遗留代码，而你决定给一个现有的接口添加一个抽象方法，那么所有实现该接口的类都必须覆盖新的抽象方法。否则，代码就会中断。

   Java 8 通过引入默认方法解决了这个问题，该方法是可选的，可以在接口层实现。

5. 接口继承规则

   为了实现通过接口的多重继承，我们必须记住一些规则。让我们来详细了解一下这些规则。

   1. 接口扩展另一个接口

      当一个接口扩展另一个接口时，它继承了该接口的所有抽象方法。让我们先创建两个接口，HasColor 和 Shape：

      ```java
      public interface HasColor {
          String getColor();
      }

      public interface Box extends HasColor {
          int getHeight()
      }

      ```

      在上面的例子中，Box 使用关键字 extends 继承了 HasColor。通过这样做，Box 接口继承了 getColor。因此，Box 接口现在有两个方法：getColor 和 getHeight。

   2. 抽象类实现一个接口

      当一个抽象类实现了一个接口，它就继承了所有的抽象和缺省方法。让我们来看看 Transform 接口和实现它的抽象类 Vehicle：

      ![Transform.java](./src/main/java/com/baeldung/interfaces/multiinheritance/Transform.java)

      ![Vehicle.java](./src/main/java/com/baeldung/interfaces/multiinheritance/Vehicle.java)

      在这个例子中，Vehicle 类继承了两个方法：抽象的 transform 方法和默认的 printSpecs 方法。

6. 功能性接口

   Java 从早期就有很多功能接口，如 Comparable（从 Java 1.2 开始）和 Runnable（从 Java 1.0 开始）。

   Java 8 引入了新的函数式接口，如 Predicate、Consumer 和 Function。

7. 总结

   在本教程中，我们对 Java 接口进行了概述，并谈到了如何使用接口来实现多态性和多重继承。
