# 规则引擎模块

本模块包含有关规则引擎的文章。与特定规则引擎相关的文章会出现在相关子模块中。

- [规则引擎模块](#规则引擎模块)
  - [Java中的规则引擎列表](#java中的规则引擎列表)
  - [Code](#code)

## Java中的规则引擎列表

1. 概述

    在本文中，我们将介绍一些最流行的 Java 规则引擎。

    在关键任务应用程序中，在源代码中维护业务逻辑的过程可能会变得过于复杂。业务规则可将业务逻辑从源代码中分离出来，从而简化开发和维护工作。

    在 Java 世界中，大多数规则引擎库都执行了 JSR94 标准，即 Java Rule API Engine。

2. Drools

    [Drools](https://www.drools.org/) 是一种业务规则管理系统（BRMS）解决方案。Drools 可以与业务流程管理工具 jBPM 集成，实现流程、事件活动、任务等的标准化。

    如果您想了解更多信息，请点击此处阅读 [Drools](https://www.baeldung.com/drools) 简介以及与 [Spring集成](https://www.baeldung.com/drools-spring-integration)的文章。

3. OpenL Tablets

    [OpenL Tablets](http://openl-tablets.org/) 是基于 Excel 决策表的业务规则管理系统和业务规则引擎。由于该框架使用的表格格式是商业用户所熟悉的，因此它在商业用户和开发人员之间架起了一座桥梁。

    下面以一个包含决策表的 Excel 文件为例，说明该框架是如何工作的。首先，让我们导入依赖于 [org.openl.core](https://search.maven.org/classic/#search%7Cga%7C1%7Cg%3A%22org.openl%22%20AND%20a%3A%22org.openl.core%22) 和 [org.openl.rules](https://search.maven.org/classic/#search%7Cga%7C1%7Cg%3A%22org.openl.rules%22%20AND%20a%3A%22org.openl.rules%22) 模块的依赖项：

    ```xml
    <dependency>
        <groupId>org.openl</groupId>
        <artifactId>org.openl.core</artifactId>
        <version>5.26.5</version>
    </dependency>
    <dependency>
        <groupId>org.openl.rules</groupId>
        <artifactId>org.openl.rules</artifactId>
        <version>5.26.5</version>
    </dependency>
    ```

    现在是用户 POJO：

    openltablets.model\User.java

    还有一个枚举，表示应用规则的结果：

    openltablets.model\Greeting.java

    Case类用变量包装了用户对象，这些变量会产生结果：

    openltablets.model\Case.java

    接口 IRule 包含 Excel 文件注入的规则：

    openltablets.rules\IRule.java

    Response 类处理应用规则的返回：

    openltablets.rules\Response.java

    主类触发规则的执行：

    openltablets.rules\Main.java

    **MacOS Run**
    - Error：`org/openl/runtime/EngineFactory has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to 52.0`
    - 处理：必须指定jdk11: `/usr/bin/env /Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home/bin/java -cp /var/folders/3z/zm1dwk351qdbn4xv1hxgr8f40000gn/T/cp_6htzdi6x6uz6y9ck74hbr8fx6.jar com.baeldung.openltablets.rules.Main`

4. 简易规则

    Easy Rules 是一个简单的 Java 规则引擎，提供了一个基于 POJO 的轻量级框架来定义业务。它可以使用复合模式从原始规则创建复杂规则。

    与大多数传统规则引擎不同，该框架不使用 XML 文件或任何特定领域语言文件来将规则与应用程序隔离。它使用基于注解的类和方法将业务逻辑注入应用程序。

    Easy Rules 可以方便开发人员创建和维护业务逻辑与应用程序本身完全分离的应用程序。另一方面，由于该框架没有实现 JSR94 标准，业务逻辑必须直接编码到 Java 代码中。

    这里我们提供一个 "Hello, world" 的示例。让我们根据 easy-rules-core 模块导入所需的依赖项：

    `<groupId>org.jeasy</groupId><artifactId>easy-rules-core</artifactId>`

    接下来，我们创建一个定义规则的类：

    easyrules\HelloWorldRule.java

    最后，我们创建主类：

    easyrules\Launcher.java

5. 规则书

    RuleBook 是一个 Java 框架，它利用 Java 8 lambdas 和责任链模式（Chain of Responsibility Pattern），使用简单的 BDD 方法定义规则。

    与大多数规则引擎一样，RuleBook 使用了 "Facts" 的概念，即提供给规则的数据。RuleBook 允许规则修改 "Facts" 的状态，然后再由下一级规则读取和修改。对于那些读入一种类型的数据（事实）并输出另一种类型的结果的规则，RuleBook 具有决策功能。

    RuleBook 可以使用 Java DSL 与 Spring 集成。

    在此，我们使用 RuleBook 提供了一个简单的 "Hello, world "示例。让我们添加依赖于 rulebook-core 模块的依赖关系：

    ```xml
    <dependency>
        <groupId>com.deliveredtechnologies</groupId>
        <artifactId>rulebook-core</artifactId>
        <version>0.12</version>
    </dependency>
    ```

    现在，我们创建规则：

    rulebook\HelloWorldRule.java

    最后是主类：

    rulebook\Launcher.java

6. 结论

    在这篇短文中，我们讨论了一些著名的库，它们为业务逻辑抽象提供了引擎。

## Code

本文中的示例可在我们的 [GitHub](https://github.com/eugenp/tutorials/tree/master/rule-engines-modules) 代码库中找到。
