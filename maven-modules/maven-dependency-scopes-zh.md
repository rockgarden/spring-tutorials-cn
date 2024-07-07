# Maven的依赖范围

1. 概述

    Maven是Java生态系统中最受欢迎的构建工具之一，其核心功能之一是依赖管理。

    在本教程中，我们将描述并探讨有助于管理Maven项目中的横向依赖关系的机制--依赖作用域。

2. 横向依赖

    Maven中有两种类型的依赖：直接依赖和传递性依赖。

    直接依赖是指我们明确包含在项目中的那些依赖。

    可以使用`<dependency>`标签来包含这些依赖。

    另一方面，transitive依赖是直接依赖所要求的。Maven会自动在我们的项目中包含所需的transitive依赖。

    我们可以使用mvn dependency:tree命令列出项目中的所有依赖项，包括传递依赖项。

3. 依赖作用域

    依赖作用域可以帮助限制依赖关系的反式性。它们还能为不同的构建任务修改classpath。Maven有六个默认的依赖作用域。

    重要的是要了解每个作用域--除import外--都会对transitive依赖产生影响。

    1. 编译 Compile

        这是没有提供其他作用域时的默认作用域。

        在所有构建任务中，项目的 classpath 上都有这个作用域的依赖关系。它们也会被传播到依赖的项目中。

        更重要的是，这些依赖关系也是相互影响的。

    2. 已提供 provided

        我们使用这个作用域来标记应该在运行时由 JDK 或容器提供的依赖关系。

        这个作用域的一个很好的用例是部署在某个容器中的 Web 应用程序，该容器本身已经提供了一些库。例如，这可能是一个已经在运行时提供Servlet API的Web服务器。

        在我们的项目中，我们可以用所提供的作用域来定义这些依赖项：

        ```xml
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        ```

        所提供的依赖项仅在编译时和项目的测试classpath中可用。这些依赖关系也不是传播的。

    3. 运行时 Runtime

        这个范围的依赖关系在运行时是需要的。但是我们在编译项目代码的时候不需要它们。正因为如此，标有运行时范围的依赖将出现在运行时和测试classpath中，但它们将在编译classpath中消失。

        JDBC 驱动程序是应该使用运行时范围的依赖关系的一个好例子：

        ```xml
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.28</version>
            <scope>runtime</scope>
        </dependency>
        ```

    4. 测试 Test

        我们用这个范围来表示依赖性在应用程序的标准运行时不需要，但只用于测试目的。

        测试依赖性不是传递性的，只存在于测试和执行classpaths中。

        这个范围的标准用例是向我们的应用程序添加一个测试库，如JUnit：

        ```xml
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
        ```

    5. 系统 System

        系统作用域与所提供的作用域非常相似。主要区别在于，system 要求我们直接指向系统上的一个特定 jar。

        值得一提的是，system scope已经被[废弃](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#system-dependencies)了。

        需要记住的是，如果依赖项不存在或者位于与systemPath指向不同的地方，用system scope依赖项构建项目在不同的机器上可能会失败：

        ```xml
        <dependency>
            <groupId>com.baeldung</groupId>
            <artifactId>custom-dependency</artifactId>
            <version>1.3.2</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/custom-dependency-1.3.2.jar</systemPath>
        </dependency>
        ```

    6. 导入 import

        它只适用于依赖类型pom。

        import表示该依赖应被其POM中声明的所有有效依赖所替换。

        这里，下面的custom-project依赖将被替换成custom-project的pom.xml `<dependencyManagement>` 部分中声明的所有依赖。

        ```xml
        <dependency>
            <groupId>com.baeldung</groupId>
            <artifactId>custom-project</artifactId>
            <version>1.3.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ```

4. 范围和传递性

    每个依赖关系的范围都以自己的方式影响跨性依赖关系。这意味着，不同的过渡性依赖可能会以不同的作用域出现在项目中。

    然而，具有提供和测试作用域的依赖关系将永远不会被包含在主项目中。

    让我们详细看看这意味着什么：

    - 对于编译作用域，所有具有运行时作用域的依赖将被拉入项目中的运行时作用域，而所有具有编译作用域的依赖将被拉入项目中的编译作用域。
    - 对于提供的作用域，运行时和编译作用域的依赖将被拉入项目中的提供的作用域。
    - 对于测试作用域，运行时和编译作用域的交叉依赖将被拉入项目中的测试作用域。
    - 对于运行时作用域，运行时和编译作用域的相互依赖将被拉入项目中的运行时作用域。
5. 总结

    在这篇简短的文章中，我们着重介绍了Maven的依赖作用域、它们的目的以及它们的操作细节。

    要想更深入地了解Maven，[文档](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)是一个很好的起点。

## Relevant Articles

- [x] [Maven Dependency Scopes](https://www.baeldung.com/maven-dependency-scopes)
