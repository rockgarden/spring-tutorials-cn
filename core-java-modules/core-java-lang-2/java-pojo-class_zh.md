# [什么是 Pojo 类？](https://www.baeldung.com/java-pojo-class)

1. 概述

    在这个简短的教程中，我们将研究 "Plain Old Java Object（老式 Java 对象）"或简称 POJO 的定义。

    我们将了解 POJO 与 JavaBean 的比较，以及将 POJO 转化为 JavaBean 有何帮助。

2. 普通 Java 对象

    1. 什么是 POJO？

        当我们谈论 POJO 时，我们所描述的是一种不引用任何特定框架的简单类型。POJO 的属性和方法没有命名约定。

        让我们创建一个基本的雇员 POJO。它将有三个属性：名、姓和开始日期：

        main/.pojo/EmployeePojo.java

        这个类可以被任何 Java 程序使用，因为它与任何框架无关。

        但是，在构造、访问或修改类的状态时，我们并没有遵循任何真正的约定。

        这种惯例的缺乏会导致两个问题：

        首先，它增加了代码编写者的学习曲线，使他们难以理解如何使用它。

        其次，它可能会限制框架的能力，使其更倾向于使用约定而非配置、了解如何使用类并增强其功能。

        为了探讨第二点，让我们使用反射来处理 EmployeePojo。因此，我们将开始发现它的一些局限性。

    2. 使用 POJO 进行反射

        让我们在项目中添加 commons-beanutils 依赖项：

        ```xml
        <dependency>
            <groupId>commons-beanutils</groupId>
            <artifactId>commons-beanutils</artifactId>
            <version>1.9.4</version>
        </dependency>
        ```

        现在，让我们检查 POJO 的属性：

        ```java
        List<String> propertyNames =
        PropertyUtils.getPropertyDescriptors(EmployeePojo.class).stream()
            .map(PropertyDescriptor::getDisplayName)
            .collect(Collectors.toList());
        ```

        如果我们将属性名称打印到控制台，我们只会看到

        `[start]`

        在这里，我们只看到 start 是类的一个属性。PropertyUtils 没有找到其他两个属性。

        如果使用其他库（如 [Jackson](https://www.baeldung.com/jackson)）来处理 EmployeePojo，我们也会看到同样的结果。

        理想情况下，我们会看到所有属性：firstName、lastName 和 startDate。好消息是，许多 Java 库都默认支持 JavaBean 命名约定。

3. JavaBean

    1. 什么是 JavaBean？

        JavaBean 仍然是一种 POJO，但在实现方式上引入了一套严格的规则：

        - Access levels-我们的属性是私有的，我们暴露了获取器和设置器
        - Method names-我们的获取器和设置器遵循 getX 和 setX 惯例（在布尔的情况下，isX 可用于获取器）。
        - Default Constructor-必须有一个无参数构造函数，这样就可以在不提供参数的情况下创建实例，例如在反序列化过程中
        - Serializable-实现可序列化接口后，我们就可以储存状态。

    2. 将 EmployeePojo 转换为 JavaBean

        那么，让我们尝试将 EmployeePojo 转换为 JavaBean：

        main/.pojo/EmployeeBean.java

    3. 使用 JavaBean 进行反射

        当我们使用反射检查 Bean 时，现在我们可以得到完整的属性列表：

        `[firstName, lastName, startDate]`

4. 使用 JavaBean 时的权衡

    因此，我们已经展示了 JavaBean 可以提供帮助的一种方式。请记住，每一种设计选择都是有取舍的。

    在使用 JavaBeans 时，我们还应注意一些潜在的缺点：

    - 可变性(Mutability)-我们的 JavaBean 由于其 setter 方法而具有可变性--这可能会导致并发性或一致性问题
    - 模板(Boilerplate)-我们必须为所有属性引入获取器，并为大多数属性引入设置器，但其中大部分可能是不必要的
    - 零参数构造函数(Zero-argument Constructor)-我们通常需要在构造函数中加入参数，以确保对象在有效状态下实例化，但 JavaBean 标准要求我们提供零参数构造函数。

    考虑到这些权衡，多年来框架也适应了其他 Bean 约定。

5. 结论

    在本教程中，我们比较了 POJO 和 JavaBean。

    首先，我们了解到 POJO 是一种 Java 对象，与特定框架无关，而 JavaBean 是一种特殊类型的 POJO，有一套严格的约定。

    然后，我们看到了一些框架和库是如何利用 JavaBean 命名约定来发现类的属性的。
