# 核心Java9

本模块包含有关 Project Jigsaw 和 Java 9 引入的 Java Platform Module System (JPMS) 的文章。

- [拼图项目简介](http://www.baeldung.com/project-jigsaw-java-modularity)
- [x] [Java9模块化指南](#java9模块化指南)
- [Java 9 java.lang.Module API](https://www.baeldung.com/java-9-module-api)
- [Java 9 非法反射访问警告](https://www.baeldung.com/java-illegal-reflective-access)
- [Java 模块化和单元测试](https://www.baeldung.com/java-modularity-unit-testing)

## Java9模块化指南

1. 概述

    Java 9 在包之上引入了一个新的抽象层，正式名称为 Java 平台模块系统（JPMS），简称 "模块"。

    在本教程中，我们将介绍新系统并讨论其各个方面。

    我们还将构建一个简单的项目来演示本指南中将要学习的所有概念。

2. 什么是模块？

    首先，在了解如何使用模块之前，我们需要了解什么是模块Module。

    模块是一组密切相关的软件包和资源，以及一个新的模块描述符文件。

    换句话说，它是一个 "Java包的包" 的抽象概念，可以让我们的代码变得更加可重用。

    1. Packages包

        模块内部的包与我们从 Java 诞生之初就开始使用的 Java 包完全相同。

        当我们创建一个模块时，就像以前在其他项目中所做的那样，我们在内部用包组织代码。

        除了组织代码，包还用于确定哪些代码可以在模块之外公开访问。我们将在本文后面花更多时间讨论这个问题。

    2. 资源

        每个模块负责其资源，如媒体或配置文件。

        以前，我们会将所有资源放到项目的根级，然后手动管理哪些资源属于应用程序的不同部分。

        有了模块，我们可以将所需的图像和 XML 文件与需要的模块一起发送，从而使我们的项目更易于管理。

    3. 模块描述符

        创建模块时，我们会包含一个描述符文件，该文件定义了新模块的几个方面：

        - 名称 - 模块的名称
        - 依赖关系 - 该模块依赖的其他模块列表
        - 公共包 - 我们希望从模块外部访问的所有包的列表
        - 提供的服务 - 我们可以提供可被其他模块使用的服务实现
        - 已消费服务 - 允许当前模块成为服务的消费者
        - 反射权限 - 明确允许其他类使用反射功能访问包中的私有成员
        模块命名规则与软件包命名规则类似（允许使用圆点，不允许使用破折号）。使用项目式（my.module）或 Reverse-DNS 式（com.baeldung.mymodule）命名非常常见。在本指南中，我们将使用项目风格。

        我们需要列出所有希望公开的软件包，因为默认情况下所有软件包都是模块私有的。

        反射也是如此。默认情况下，我们不能对从其他模块导入的类使用反射。

        稍后，我们将举例说明如何使用模块描述符文件。

    4. 模块类型

        新模块系统中有四种模块类型：

        - 系统模块 - 运行上述 list-modules 命令时列出的模块。它们包括 Java SE 和 JDK 模块。
        - 应用程序模块(Application Modules) - 这些模块是我们决定使用模块时通常要构建的模块。它们在已编译的 module-info.class 文件中被命名和定义，该文件包含在已组装的 JAR 中。
        - 自动模块(Automatic Modules) - 我们可以通过在模块路径中添加现有的 JAR 文件来包含非官方模块。模块的名称将从 JAR 的名称导出。自动模块可以完全读取路径中加载的所有其他模块。
        - 未命名模块(Unnamed Module) - 当一个类或 JAR 被加载到类路径而不是模块路径时，它将被自动添加到未命名模块中。它是一个万能模块，用于保持与以前编写的 Java 代码的向后兼容性。
    5. 分发

        模块有两种发布方式：作为 JAR 文件或作为 "exploded" 编译项目。当然，这与其他任何 Java 项目都是一样的，因此也就不足为奇了。

        我们可以创建由 "main application" 和多个库模块组成的多模块项目。

        但我们必须小心，因为每个 JAR 文件只能有一个模块。

        在设置构建文件时，我们需要确保将项目中的每个模块都捆绑为单独的 jar 文件。

3. 默认模块

    安装 Java 9 时，我们可以看到 JDK 现在有了新的结构。

    他们将原来的所有软件包都移到了新的模块系统中。

    我们可以在命令行中键入以下内容，查看这些模块是什么：

    `java --list-modules`

    这些模块分为四大类：java、javafx、jdk 和 Oracle。

    java 模块是核心 SE 语言规范的实现类。

    javafx 模块是 FX UI 库。

    JDK 本身需要的任何内容都保存在 jdk 模块中。

    最后，任何与 Oracle 相关的内容都放在 oracle 模块中。

4. 模块声明

    要建立一个模块，我们需要在软件包的根目录下放置一个名为 module-info.java 的特殊文件。

    该文件被称为模块描述符，包含构建和使用新模块所需的全部数据。

    我们通过声明来构建模块，声明体要么为空，要么由模块指令组成：

    `module myModuleName { // 所有指令都是可选的 }`

    我们用 module 关键字作为模块声明的开头，并在后面加上模块名称。

    模块将在此声明下运行，但我们通常需要更多信息。

    这就是模块指令的作用所在。

    1. Requires

        我们的第一个指令是 requirements。该模块指令允许我们声明模块依赖关系：

        ```java
        module my.module {
            requires module.name;
        }
        ```

        现在，my.module 在运行时和编译时都依赖 module.name。

        使用该指令时，我们的模块可以访问从依赖关系中导出的所有公共类型。

    2. Requires Static 静态需求

        有时，我们编写的代码会引用另一个模块，但我们库的用户永远不会想使用它。

        例如，我们可能会编写一个实用程序，当另一个日志模块存在时，它可以漂亮地打印出我们的内部状态。但是，并不是我们程序库的每个用户都需要这种功能，而且他们也不想包含一个额外的日志记录程序库。

        在这种情况下，我们需要使用可选的依赖关系。通过使用 requirements static 指令，我们创建了一个仅在编译时使用的依赖关系：

        ```java
        module my.module {
            requires static module.name;
        }
        ```

    3. Requires Transitive

        我们通常使用库来简化生活。

        但是，我们需要确保任何引入我们代码的模块也会引入这些额外的 "transitive" 依赖关系，否则它们将无法工作。

        幸运的是，我们可以使用 requirements 传递指令来强制任何下游消费者也读取我们所需的依赖关系：

        ```java
        module my.module {
            requires transitive module.name;
        }
        ```

        现在，当开发者要求使用 my.module 时，他们就不必同时使用 requires module.name 也能让我们的模块正常工作了。

    4. exports

        默认情况下，模块不会向其他模块公开任何 API。这种强大的封装性是创建模块系统的主要动机之一。

        我们的代码安全性大大提高，但现在我们需要明确地向世界开放我们的应用程序接口（API），这样才能保证其可用性。

        我们使用 exports 指令公开命名包的所有公共成员：

        ```java
        module my.module {
            exports com.my.package.name;
        }
        ```

        现在，当有人要求使用 my.module 时，他们可以访问 com.my.package.name 包中的公共类型，但不能访问任何其他包。

    5. exports...to

        我们可以使用 exports...to 向全世界开放我们的公共类。

        但是，如果我们不想让全世界都访问我们的 API 怎么办？

        我们可以使用 exports...to 指令限制哪些模块可以访问我们的 API。

        与 exports 指令类似，我们将一个软件包声明为已导出。不过，我们也列出了允许导入该包的模块。让我们看看这看起来像什么：

        ```java
        module my.module {
            export com.my.package.name to com.specific.package;
        }
        ```

    6. Uses

        服务是特定接口或抽象类的实现，可被其他类使用。

        我们使用 uses 指令指定模块使用的服务。

        请注意，我们使用的类名是服务的接口或抽象类，而不是实现类：

        ```java
        module my.module {
            uses class.name;
        }
        ```

        这里我们需要注意的是，require 指令和 uses 指令是不同的。

        我们可能需要一个提供了我们想要使用的服务的模块，但该服务实现了其传递依赖关系中的一个接口。

        为了以防万一，我们不会强迫模块要求所有传递依赖模块，而是使用 uses 指令将所需接口添加到模块路径中。

    7. Provides … With

        模块也可以是其他模块可以使用的服务提供者。

        指令的第一部分是 provides 关键字。我们在这里输入接口或抽象类的名称。

        接下来是 with 指令，我们在这里提供实现接口或扩展抽象类的实现类名称。

        下面是组合起来的样子：

        ```java
        module my.module {
            provides MyInterface with MyInterfaceImpl;
        }
        ```

    8. Open

        我们在前面提到，封装是设计此模块系统的一个驱动因素。

        在 Java 9 之前，可以使用反射来检查包中的每个类型和成员，甚至是私有成员。没有什么是真正封装的，这会给库的开发人员带来各种各样的问题。

        由于 Java 9 强化了封装，我们现在必须明确授予其他模块对我们的类进行反射的权限。

        如果我们想继续像旧版本的 Java 那样允许完全反射，我们可以简单地开放整个模块：

        `open module my.module {}`

    9. Opens

        如果我们需要允许对私有类型进行反射，但又不想暴露所有代码，我们可以使用 opens 指令来暴露特定的包。

        但请记住，这将向整个世界开放软件包，所以请确保这是你想要的：

        ```java
        module my.module {
            opens com.my.package；
        }
        ```

    10. Opens … To

        好吧，反射有时很不错，但我们仍然希望从封装中获得尽可能多的安全性。在本例中，我们可以使用 opens...to 指令，有选择性地将我们的包开放给预先批准的模块列表：

        ```java
        module my.module {
            opens com.my.package to moduleOne, moduleTwo, etc.;
        }
        ```

5. 命令行选项

    到目前为止，Maven 和 Gradle 已经添加了对 Java 9 模块的支持，因此您不需要大量手动构建项目。不过，了解如何通过命令行使用模块系统仍然很有价值。

    我们将在下面的完整示例中使用命令行，以帮助我们在头脑中巩固整个系统的工作原理。

    - module-path - 我们使用 -module-path 选项指定模块路径。这是包含模块的一个或多个目录的列表。
    - add-reads - 我们可以使用 -add-reads 命令行，而不是依赖模块声明文件。
    - add-exports - 命令行替代 exports 指令。
    - add-opens - 替换模块声明文件中的 open 子句。
    - add-modules - 将模块列表添加到默认模块集中
    - list-modules - 打印所有模块及其版本字符串的列表
    - patch-module - 在模块中添加或覆盖类
    - illegal-access=permit|warn|deny - 通过显示单个全局警告来放松强封装、显示所有警告或错误失败。默认为允许。

6. 可见性

    我们应该花点时间谈谈代码的可见性。

    很多库都依赖于反射来发挥其魔力（我想到了 JUnit 和 Spring）。

    在 Java 9 中，默认情况下我们只能访问导出包中的公有类、方法和字段。即使我们使用反射来访问非公有成员并调用 setAccessible(true)，也无法访问这些成员。

    我们可以使用 open、opens 和 opens...to 选项为反射授予运行时访问权限。注意，这是运行时访问！

    我们无法针对私有类型进行编译，而且无论如何也不需要这样做。

    如果我们必须访问某个模块进行反射，而我们又不是该模块的所有者（即我们不能使用 opens...to 指令），那么可以使用命令行 -add-opens 选项，允许自己的模块在运行时对锁定模块进行反射访问。

    唯一需要注意的是，你必须能访问用于运行模块的命令行参数，这样才能做到这一点。

7. 把所有东西放在一起

    既然我们已经知道了什么是模块以及如何使用模块，那么就让我们来构建一个简单的项目来演示我们刚刚学到的所有概念。

    为了保持简单，我们不会使用 Maven 或 Gradle。相反，我们将使用命令行工具来构建模块。

    1. 设置我们的项目

        首先，我们需要设置项目结构。我们将创建几个目录来组织文件。

        首先创建项目文件夹

        ```zsh
        mkdir module-project
        cd module-project
        ```

        这是整个项目的基础，所以要在这里添加文件，如 Maven 或 Gradle 构建文件、其他源代码目录和资源。

        我们还将在此放置一个目录，用于存放所有项目特定的模块。

        接下来，我们创建一个模块目录：

        mkdir simple-modules
        我们的项目结构：见 src/simple-modules

    2. 第一个模块

        现在我们已经有了基本的结构，让我们添加第一个模块。

        在 simple-modules 目录下新建一个名为 hello.modules 的目录。

        我们可以给它起任何名字，但要遵守软件包命名规则（如用句号分隔单词等）。如果愿意，我们甚至可以使用主软件包的名称作为模块名称，但通常情况下，我们希望使用与创建该模块的 JAR 相同的名称。

        在新模块下，我们可以创建想要的包。在本例中，我们将创建一个包结构：

        com.baeldung.modules.hello

        然后，在该包中创建一个名为 HelloModules.java 的新类。我们将保持代码的简洁：

        HelloModules.java

        最后，在 hello.modules 根目录下添加模块描述符；module-info.java。

        为使示例简单明了，我们只需导出 com.baeldung.modules.hello 包的所有公共成员。

    3. 我们的第二个模块

        我们的第一个模块很棒，但它什么也做不了。

        现在我们可以创建第二个模块来使用它。

        在 simple-modules 目录下创建另一个名为 main.app 的模块目录。这次我们将从模块描述符开始：module-info.java。

        我们不需要向外界公开任何东西。相反，我们只需依赖第一个模块，这样就可以访问它导出的公有类。

        现在，我们可以创建一个使用它的应用程序。

        创建一个新的包结构：com.baeldung.modules.main。

        现在，创建一个名为 MainApp.java 的新类文件。

        这就是我们演示模块所需的全部代码。下一步是通过命令行构建并运行这段代码。

    4. 构建模块

        为了构建我们的项目，我们可以创建一个简单的 bash 脚本，并将其放在项目的根目录下。

        创建一个名为 compile-simple-modules.sh 的文件。

        该命令包括两个部分，即 javac 和 find 命令。

        find 命令只是输出 simple-modules 目录下所有 .java 文件的列表。然后，我们就可以将该列表直接输入 Java 编译器。

        与旧版本的 Java 相比，我们唯一需要做的就是提供一个 module-source-path 参数，以告知编译器它正在构建模块。

        运行此命令后，我们将得到一个包含两个已编译模块的 outDir 文件夹。

    5. 运行代码

        现在我们终于可以运行代码来验证模块是否正常工作了。

        在项目根目录下创建另一个文件：run-simple-module-app.sh。

        要运行模块，我们必须至少提供模块路径和主类。如果一切正常，您应该看到

        ```zsh
        >$ ./run-simple-module-app.sh 
        Hello, Modules!
        ```

    6. 添加服务

        既然我们对如何构建模块有了基本的了解，那就把它变得复杂一些吧。

        我们将看看如何使用 provides...with 和 uses 指令。

        首先，在 hello.modules 模块中定义一个名为 HelloInterface.java 的新文件。

        为了方便起见，我们将用现有的 HelloModules.java 类实现该接口。

        这就是我们创建服务所需要做的全部工作。

        现在，我们需要告诉全世界，我们的模块提供了这项服务。

        在 module-info.java 中添加以下内容：

        `provides com.baeldung.modules.hello.HelloInterface with com.baeldung.modules.hello.HelloModules;`

        正如我们所看到的，我们声明了接口和实现接口的类。

        接下来，我们需要使用这项服务。在我们的 main.app 模块中，让我们在 module-info.java 中添加以下内容：

        `uses com.baeldung.modules.hello.HelloInterface;`

        最后，在我们的主方法中，我们可以通过 ServiceLoader 使用该服务。

        编译并运行：MacOS

        ```zsh
        % cd ../core-java-9-jigsaw
        % chmod ugo+x compile-simple-modules.sh
        % ./compile-simple-modules.sh
        % sh ./run-simple-module-app.sh
        ```

        我们使用这些指令可以更明确地说明代码的使用方式。

        我们可以将实现放在私有包中，而将接口放在公共包中。

        这样，我们的代码就更安全了，而额外的开销却很少。

        继续尝试使用其他指令，进一步了解模块及其工作原理。

8. 为未命名模块添加模块

    未命名模块的概念与默认包类似。因此，它不被视为真正的模块，但可以被视为默认模块。

    如果一个类不是已命名模块的成员，那么它将自动被视为未命名模块的一部分。

    有时，为了确保模块图中有特定的平台、库或服务提供商模块，我们需要在默认根集中添加模块。例如，当我们尝试使用 Java 9 编译器运行 Java 8 程序时，可能需要添加模块。

    一般来说，将命名模块添加到默认根模块集的选项是 `–add-modules <module>(,<module>)*` 其中 <module> 是模块名称。

    例如，要访问所有 java.xml.bind 模块，语法为

    `--add-modules java.xml.bind`

    要在 Maven 中使用此语法，我们可以在 maven 编译器中嵌入相同的语法

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.0</version>
        <configuration>
            <source>9</source>
            <target>9</target>
            <compilerArgs>
                <arg>--add-modules</arg>
                <arg>java.xml.bind</arg>
            </compilerArgs>
        </configuration>
    </plugin>
    ```

9. 总结

    在这篇详尽的指南中，我们重点介绍了新 Java 9 模块系统的基础知识。

    我们首先讨论了什么是模块。

    接着，我们介绍了如何发现 JDK 中包含哪些模块。

    我们还详细介绍了模块声明文件。

    最后，我们讨论了构建模块所需的各种命令行参数。

    最后，我们将所有知识付诸实践，创建了一个基于模块系统的简单应用程序。

## Code

要查看这些代码和更多内容，请务必访问 [Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-9-jigsaw)。
