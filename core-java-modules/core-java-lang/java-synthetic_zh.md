# [Java中的合成结构](https://www.baeldung.com/java-synthetic)

1. 概述

    在本教程中，我们将了解 Java 的合成结构，这些代码由编译器引入，用于透明地处理对成员的访问，否则这些成员将因可见性不足或缺少引用而无法访问。

    注意：从JDK 11开始，不再生成合成方法和构造函数，因为它们被[nest-base的访问控制](https://www.baeldung.com/java-nest-based-access-control)所取代。

2. Java中的合成

    我们能找到的关于合成的最佳定义直接来自《Java语言规范》（[JLS 13.1.7](https://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html)）。

    任何由Java编译器引入的结构体，如果在源代码中没有相应的结构体，就必须被标记为合成，但默认的构造体、类的初始化方法以及Enum类的value和valueOf方法除外。

    有不同种类的编译构造，即字段、构造函数和方法。另一方面，尽管嵌套类可以被编译器改变（即匿名类），但它们不被认为是合成的。

3. 合成字段

    让我们从一个简单的嵌套类开始。

    SyntheticFieldDemo.java

    当编译时，任何内部类都会包含一个引用顶层类的合成字段。巧合的是，这也是让我们有可能从一个嵌套类中访问包围类成员的原因。

    为了确保这一点，我们将实现一个测试，通过反射获得嵌套类的字段，并使用isSynthetic()方法检查它们。

    SyntheticUnitTest.givenSyntheticField_whenIsSynthetic_thenTrue()

    我们还可以通过javap命令运行反汇编程序来验证这一点。在这两种情况下，输出显示了一个名为this$0的合成字段。

4. 合成方法

    接下来，我们要给我们的嵌套类添加一个私有字段。

    SyntheticMethodDemo.java

    在这种情况下，编译将生成变量的访问器。没有这些方法，就不可能从包围的实例中访问一个私有字段。

    再一次，我们可以用同样的技术来检查这一点，它显示了两个名为access$0和access$1的合成方法。

    SyntheticUnitTest.givenSyntheticMethod_whenIsSynthetic_thenTrue()

    请注意，为了生成代码，必须实际读取或写入该字段，否则，这些方法将被优化掉。这就是为什么我们还添加了一个getter和一个setter的原因。

    如上所述，从JDK 11开始，这些合成方法不再被生成。

    1. 桥接方法

        合成方法的一个特例是桥接方法，它处理泛型的类型消除。

        例如，让我们考虑一个简单的比较器。

        BridgeMethodDemo.java

        尽管compare()在源码中需要两个Integer参数，但由于类型擦除的原因，一旦编译，它将需要两个Object参数。

        为了处理这个问题，编译器创建了一个合成桥，来处理这些参数的转换问题。

        ```java
        public int compare(Object o1, Object o2) {
            return compare((Integer) o1, (Integer) o2);
        }
        ```

        除了之前的测试外，这次我们还将从Method类中调用isBridge()。

        SyntheticUnitTest.givenBridgeMethod_whenIsBridge_thenTrue()

5. 合成构造函数

    最后，我们将添加一个私有构造函数。

    SyntheticConstructorDemo.java

    这一次，一旦我们运行测试或反汇编程序，就会发现实际上有两个构造函数，其中一个是合成的。

    SyntheticUnitTest.givenSyntheticConstructor_whenIsSynthetic_thenTrue()

    与合成字段类似，这个生成的构造函数对于从其包围的实例中实例化一个具有私有构造函数的嵌套类是必不可少的。

    如上所述，从 JDK 11 开始，合成构造函数不再生成。

6. 结语

    在这篇文章中，我们讨论了由Java编译器生成的合成构造。为了测试它们，我们使用了反射，你可以在这里了解[更多信息](https://www.baeldung.com/java-reflection)。
