# [Java 接口中的静态方法与默认方法](https://www.baeldung.com/java-static-default-methods)

核心 Java | Java 接口

1. 概述

    Java 8 引入了多项全新特性，包括 [Lambda](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) 表达式、函数式接口、方法引用、Stream 流、Optional 类，以及**接口中的静态方法和默认方法**。

    在本教程中，我们将学习如何在接口中使用静态方法和默认方法，并讨论它们在哪些场景下特别有用。

2. 为什么接口需要默认方法

    与普通接口方法一样，默认方法隐式为 public，无需显式声明 `public` 修饰符。

    但与普通接口方法不同的是，默认方法需在方法签名前加上 `default` 关键字，并提供具体实现。

    来看一个简单示例：

    ```java
    public interface MyInterface {

        // 普通接口方法

        default void defaultMethod() {
            // 默认方法的实现
        }
    }
    ```

    Java 8 引入默认方法的原因显而易见。

    在典型的基于抽象的设计中，一个接口可能有一个或多个实现类。如果向接口中新增一个或多个方法，所有实现类都必须实现这些新方法，否则设计将直接崩溃。

    默认接口方法是解决此问题的有效方式。它允许我们在接口中添加新方法，而这些方法会自动在所有实现类中可用，**无需修改实现类**。

    这样，就能在不重构实现类的前提下，优雅地保持向后兼容性。

3. 默认接口方法的实际应用

    为了更好地理解默认方法的功能，我们来看一个简单示例。

    假设我们有一个简单的 `Vehicle` 接口和一个实现类（为简化，仅保留一个实现）：

    ```java
    public interface Vehicle {

        String getBrand();

        String speedUp();

        String slowDown();

        default String turnAlarmOn() {
            return "Turning the vehicle alarm on.";
        }

        default String turnAlarmOff() {
            return "Turning the vehicle alarm off.";
        }
    }
    ```

    实现类如下：

    ```java
    public class Car implements Vehicle {

        private String brand;

        // 构造函数 / getter 方法

        @Override
        public String getBrand() {
            return brand;
        }

        @Override
        public String speedUp() {
            return "The car is speeding up.";
        }

        @Override
        public String slowDown() {
            return "The car is slowing down.";
        }
    }
    ```

    最后，编写一个主类，创建 `Car` 实例并调用其方法：

    ```java
    public static void main(String[] args) {
        Vehicle car = new Car("BMW");
        System.out.println(car.getBrand());
        System.out.println(car.speedUp());
        System.out.println(car.slowDown());
        System.out.println(car.turnAlarmOn());
        System.out.println(car.turnAlarmOff());
    }
    ```

    请注意，`Vehicle` 接口中的默认方法 `turnAlarmOn()` 和 `turnAlarmOff()` 在 `Car` 类中**自动可用**。

    此外，如果将来我们决定向 `Vehicle` 接口添加更多默认方法，应用程序仍可正常运行，无需强制实现类提供新方法的实现。

    接口默认方法最常见的用途是：**在不破坏现有实现类的前提下，逐步为某类型增加新功能**。

    另外，我们还可以利用默认方法围绕已有的抽象方法提供额外功能：

    ```java
    public interface Vehicle {

        // 其他接口方法

        double getSpeed();

        default double getSpeedInKMH(double speed) {
            // 单位换算逻辑
        }
    }
    ```

4. 多接口继承规则

    默认方法是一项非常实用的特性，但也有一些注意事项。由于 Java 允许类实现多个接口，因此需要了解：当一个类实现的多个接口定义了**同名的默认方法**时会发生什么。

    为更好地理解这一场景，我们定义一个新的 `Alarm` 接口，并重构 `Car` 类：

    ```java
    public interface Alarm {

        default String turnAlarmOn() {
            return "Turning the alarm on.";
        }

        default String turnAlarmOff() {
            return "Turning the alarm off.";
        }
    }
    ```

    现在，`Car` 类同时实现 `Vehicle` 和 `Alarm` 接口：

    ```java
    public class Car implements Vehicle, Alarm {
        // ...
    }
    ```

    此时，代码将**无法编译**，因为多接口继承引发了冲突（即“菱形问题”）。`Car` 类同时继承了两套同名的默认方法，编译器无法确定应调用哪一套。

    为解决此歧义，我们必须**显式提供方法的实现**：

    ```java
    @Override
    public String turnAlarmOn() {
        // 自定义实现
    }

    @Override
    public String turnAlarmOff() {
        // 自定义实现
    }
    ```

    我们也可以让类选择使用其中一个接口的默认实现。

    例如，使用 `Vehicle` 接口的默认方法：

    ```java
    @Override
    public String turnAlarmOn() {
        return Vehicle.super.turnAlarmOn();
    }

    @Override
    public String turnAlarmOff() {
        return Vehicle.super.turnAlarmOff();
    }
    ```

    同样，也可以使用 `Alarm` 接口的默认方法：

    ```java
    @Override
    public String turnAlarmOn() {
        return Alarm.super.turnAlarmOn();
    }

    @Override
    public String turnAlarmOff() {
        return Alarm.super.turnAlarmOff();
    }
    ```

    甚至可以同时调用两个接口的默认方法：

    ```java
    @Override
    public String turnAlarmOn() {
        return Vehicle.super.turnAlarmOn() + " " + Alarm.super.turnAlarmOn();
    }

    @Override
    public String turnAlarmOff() {
        return Vehicle.super.turnAlarmOff() + " " + Alarm.super.turnAlarmOff();
    }
    ```

5. 接口中的静态方法

    除了默认方法，Java 8 还允许在接口中定义和实现**静态方法**。

    由于静态方法不属于特定对象，因此它们**不是实现类 API 的一部分**，必须通过**接口名**调用。

    为理解接口中静态方法的工作方式，我们重构 `Vehicle` 接口，添加一个静态工具方法：

    ```java
    public interface Vehicle {

        // 普通 / 默认接口方法

        static int getHorsePower(int rpm, int torque) {
            return (rpm * torque) / 5252;
        }
    }
    ```

    在接口中定义静态方法与在类中定义完全相同。此外，静态方法可以在其他静态方法和默认方法中被调用。

    假设我们要计算某车辆发动机的马力，只需调用该方法：

    ```java
    Vehicle.getHorsePower(2500, 480);
    ```

    引入接口静态方法的初衷是：提供一种简单机制，将相关方法集中到一处，**提高设计的[内聚性](https://en.wikipedia.org/wiki/Cohesion_(computer_science))**，而无需创建对象。

    这与抽象类的功能类似。主要区别在于：抽象类可以拥有构造函数、状态（字段）和行为。

    此外，接口中的静态方法使我们能够将相关的工具方法组织在一起，而无需创建仅用于存放静态方法的“人工”工具类。

6. 结论

    本文深入探讨了 Java 8 中接口的静态方法和默认方法。乍看之下，这一特性可能显得有些“不够纯粹”，尤其从面向对象纯粹主义者的视角来看——理想情况下，接口不应封装具体行为，而应仅用于定义某类型的公共 API。

    然而，在**维护现有代码的向后兼容性**方面，静态方法和默认方法是一种非常有价值的折中方案。
