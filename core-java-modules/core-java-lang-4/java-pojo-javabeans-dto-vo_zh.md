# [POJO、JavaBeans、DTO 和 VO 之间的区别](https://www.baeldung.com/java-pojo-javabeans-dto-vo)

1. 概述

    在本教程中，我们将学习什么是数据传输对象（DTO）、值对象（VO）、普通 Java 对象（POJO）和 JavaBean。我们将研究它们之间的区别，并了解何时使用哪种类型。

2. 普通旧 Java 对象

    [POJO](https://www.baeldung.com/java-pojo-class) 也称为 Plain Old Java Object，是一种没有任何特定框架引用的普通 Java 对象。它是一个术语，用来指简单、轻量级的 Java 对象。

    POJO 不对属性和方法使用任何命名约定。

    让我们定义一个有三个属性的基本 EmployeePOJO 对象：

    main/.employee/EmployeePOJO.java

    我们可以看到，上述 Java 对象定义了表示雇员的结构，并且不依赖于任何框架。

3. JavaBean

    1. 什么是 JavaBean？

        JavaBean主要类似于 POJO，有一套严格的实现规则。

        这些规则规定，它应该是可序列化的，有一个空构造函数，并允许使用遵循 getX() 和 setX() 约定的方法访问变量。

    2. 作为 JavaBean 的 POJO

        由于 JavaBean 本质上就是 POJO，因此让我们通过实现必要的 bean 规则将 EmployeePOJO 转换为 JavaBean：

        main/.employee/EmployeeBean.java

        在这里，为了将 POJO 转换为 JavaBean，我们实现了 Serializable 接口，将属性标记为私有，并使用 getter/setter 方法访问属性。

4. DTO

    1. DTO 模式

        [DTO](https://www.baeldung.com/java-dto-pattern) 也称为数据传输对象（Data Transfer Object），它封装了在进程或网络之间传输数据的值。

        这有助于减少调用方法的数量。通过在一次调用中包含多个参数或值，我们可以减少远程操作中的网络开销。

        这种模式的另一个优点是对序列化逻辑的封装。它允许程序以特定格式存储和传输数据。

        DTO 没有任何明确的行为。通过将领域模型与表现层解耦，它基本上有助于使代码松散耦合。

    2. 如何使用 DTO？

        DTO 具有没有任何业务逻辑的扁平结构。它们使用与 POJO 相同的格式。DTO 只包含存储、访问器和与序列化或解析相关的方法。

        DTO 基本上映射到领域模型，从而将数据发送到方法或服务器。

        让我们创建 EmployeeDTO，它将创建一名雇员所需的所有详细信息分组。我们将通过单个请求将这些数据发送到服务器，从而优化与 API 的交互：

        main/.employee/EmployeeDTO.java

        上述 DTO 与不同的服务交互并处理数据流。这种 DTO 模式可用于任何服务，不受任何框架限制。

5. VO

    VO 也称为值对象，是一种特殊类型的对象，可以保存 java.lang.Integer 和 java.lang.Long 等值。

    VO 应始终覆盖 equals() 和 hashCode() 方法。VO 通常封装数字、日期、字符串等小对象。它们遵循值语义，即直接更改对象的值，并传递副本而不是引用。

    让值对象不可变是一种很好的做法。值的改变只发生在创建新对象时，而不是更新旧对象本身的值时。这有助于理解 "两个等价创建的值对象应保持等价" 这一隐含契约。

    让我们定义 EmployeeVO 并覆盖 equals() 和 hashCode() 方法：

    main/.employee/EmployeeVO.java

6. 结论

    在本文中，我们了解了 POJO、JavaBean、DTO 和值对象的定义。我们还了解了一些框架和库如何利用 JavaBean 命名约定，以及如何将 POJO 转换为 JavaBean。我们还了解了 DTO 模式和值对象以及它们在不同场景中的用法。

    接下来，Java 14 记录通过抽象 getters、setters、equals 和 hashcode 增强了可读性，并提供了开箱即用的不变性。您可以在我们的[文章](https://www.baeldung.com/java-15-new#records-jep-384)中阅读更多相关信息。
