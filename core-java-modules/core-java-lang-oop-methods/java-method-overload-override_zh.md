# [Java中的方法重载和重写](https://www.baeldung.com/java-method-overload-override)

1. 概述

    方法重载和重载是Java编程语言的关键概念，因此，它们值得深入研究。

    在这篇文章中，我们将学习这些概念的基础知识，并看看它们在哪些情况下可以发挥作用。

2. 方法重载

    方法重载是一种强大的机制，它使我们能够定义有凝聚力的类API。为了更好地理解为什么方法重载是一个如此有价值的特性，让我们看一个简单的例子。

    假设我们写了一个naive的实用类，实现了两个数字相乘、三个数字相乘的不同方法，以此类推。

    如果我们给这些方法起了误导性或模糊的名字，比如multiply2()、multiply3()、multiply4()，那么这将是一个设计不良的类API。这里就是方法重载发挥作用的地方。

    简单地说，我们可以通过两种不同的方式实现方法重载：

    - 实现两个或多个名字相同但接受不同数量参数的方法
    - 实现两个或多个名字相同但接受不同类型参数的方法。

    1. 不同的参数数

        Multiplier类简要地展示了如何通过简单地定义两个接受不同参数数的实现来重载multiply()方法：

        methodoverloadingoverriding.util/Multiplier.java

    2. 不同类型的参数

        同样地，我们可以通过让multiply()方法接受不同类型的参数来重载它：

        methodoverloadingoverriding.util/Multiplier.java

        此外，用两种类型的方法重载来定义Multiplier类是合法的。

        然而，值得注意的是，不可能有两个仅在返回类型上不同的方法实现。

        为了理解这个原因--让我们考虑下面这个例子：

        ```java
        public int multiply(int a, int b) { 
            return a * b; 
        }
        public double multiply(int a, int b) { 
            return a * b; 
        }
        ```

        在这种情况下，由于方法调用的模糊性，代码根本无法编译--编译器不知道应该调用multiply()的哪个实现。

    3. 类型推广

        方法重载提供的一个很好的功能是所谓的类型推广(Type Promotion)，也就是拓宽原始转换。

        简单地说，当传递给重载方法的参数类型和特定的方法实现之间不匹配时，一个给定的类型被隐含地提升到另一个类型。

        为了更清楚地了解类型推广是如何工作的，请考虑以下multiply()方法的实现：

        ```java
        public double multiply(int a, long b) {
            return a * b;
        }
        public int multiply(int a, int b, int c) {
            return a * b * c;
        }
        ```

        现在，调用有两个int参数的方法将导致第二个参数被提升为long，因为在这种情况下，没有一个匹配的有两个int参数的方法实现。

        让我们看看一个快速的单元测试来演示类型推广：

        ```java
        @Test
        public void whenCalledMultiplyAndNoMatching_thenTypePromotion() {
            assertThat(multiplier.multiply(10, 10)).isEqualTo(100.0);
        }
        ```

        相反，如果我们用一个匹配的实现来调用这个方法，类型推广就不会发生：

        ```java
        @Test
        public void whenCalledMultiplyAndMatching_thenNoTypePromotion() {
            assertThat(multiplier.multiply(10, 10, 10)).isEqualTo(1000);
        }
        ```

        下面是适用于方法重载的类型推广规则的总结：

        - byte可以被提升为short、int、long、float或double
        - short可以被提升为int、long、float或double
        - char可以被提升为int、long、float或double
        - int可以被提升为long、float或double
        - long可以被提升为float或double
        - float可以被提升为double

    4. 静态绑定

        将特定的方法调用与方法的主体联系起来的能力被称为绑定。

        在方法重载的情况下，绑定是在编译时静态进行的，因此它被称为静态绑定。

        编译器可以通过简单地检查方法的签名来有效地在编译时设置绑定。

3. 方法重写

    方法覆盖允许我们在子类中为基类中定义的方法提供细粒度的实现。

    虽然方法覆盖是一个强大的功能--考虑到这是使用继承的逻辑结果，是OOP的最大支柱之一--但何时何地利用它，应该根据每个使用情况仔细分析。

    现在让我们看看如何通过创建一个简单的、基于继承的（"is-a"）关系来使用方法重写。

    这里是基类：

    methodoverloadingoverriding.model/Vehicle.java

    这里是一个臆造的子类：

    methodoverloadingoverriding.model/Car.java

    在上面的层次结构中，我们简单地重载了accelerate()方法，以便为子类型Car提供一个更完善的实现。

    在这里，我们可以清楚地看到，如果一个应用程序使用Vehicle类的实例，那么它也可以与Car类的实例一起工作，因为accelerate()方法的两个实现具有相同的签名和相同的返回类型。

    让我们写几个单元测试来检查Vehicle和Car类：

    methodoverloadingoverriding/MethodOverridingUnitTest.java

    现在，让我们看看一些单元测试，看看没有被重写的run()和stop()方法如何为Car和Vehicle返回相等的值：

    MethodOverridingUnitTest.java: givenVehicleCarInstances_whenCalledRun_thenEqual()

    MethodOverridingUnitTest.java: givenVehicleCarInstances_whenCalledStop_thenEqual()

    在我们的案例中，我们可以访问两个类的源代码，所以我们可以清楚地看到，在一个基本的Vehicle实例上调用accelerate()方法和在Car实例上调用accelerate()方法将对相同的参数返回不同的值。

    因此，下面的测试证明了对汽车实例调用了重载方法：

    ```java
    @Test
    public void whenCalledAccelerateWithSameArgument_thenNotEqual() {
        assertThat(vehicle.accelerate(100))
        .isNotEqualTo(car.accelerate(100));
    }
    ```

    1. 类型可替代性

        OOP的一个核心原则是类型可替代性，它与Liskov替代原则（LSP）密切相关。

        简单地说，LSP指出，如果一个应用程序能与一个给定的基本类型一起工作，那么它也应该能与它的任何子类型一起工作。这样一来，类型的可替代性就得到了适当的保留。

        方法重写的最大问题是，派生类中的一些特定方法实现可能并不完全遵守LSP，因此无法保留类型可替代性。

        当然，让一个被覆盖的方法接受不同类型的参数并返回不同的类型也是有效的，但是要完全遵守这些规则：

        - 如果基类中的方法接受给定类型的参数，被覆盖的方法应该接受相同的类型或超类型（又称禁忌方法参数）。
        - 如果基类中的方法返回无效，被覆盖的方法应该返回无效。
        - 如果基类中的一个方法返回一个基元，被覆盖的方法应该返回相同的基元。
        - 如果基类中的方法返回某种类型，被覆盖的方法应该返回相同的类型或子类型（又称共变(covariant)返回类型）。
        - 如果基类中的方法抛出一个异常，被覆盖的方法必须抛出相同的异常或基类异常的一个子类型
    2. 动态绑定

        考虑到方法覆盖只能通过继承来实现，其中有一个基类和子类的层次结构，编译器不能在编译时确定调用什么方法，因为基类和子类都定义了相同的方法。

        因此，编译器需要检查对象的类型以知道应该调用什么方法。

        由于这种检查是在运行时进行的，方法重写是动态绑定的一个典型例子。

4. 结语

    在本教程中，我们学习了如何实现方法重载和方法重写，并探讨了一些它们有用的典型情况。
