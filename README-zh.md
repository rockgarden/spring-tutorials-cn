# Java 和 Spring 教程

该项目是**一系列小型且重点突出的教程**——每个教程都涵盖了 Java 生态系统中一个明确定义的开发领域。
其中一个重点当然是 Spring Framework - Spring、Spring Boot 和 Spring Security。
除了 Spring 之外，这里的模块还涵盖了 Java 的许多方面。

## 基于配置文件的隔离

我们正在使用 maven 构建配置文件来隔离资源库中的大量单个项目。

目前，绝大多数模块都需要 JDK8 才能正确构建和运行。

这些项目大致分为三个列表：第一、第二和重型。

然后，根据我们要执行的测试对它们进行进一步划分。

此外，还有 2 个配置文件专门用于 JDK9 及以上版本的构建（**需要 JDK 17**）。

我们还有一个父级配置文件，仅用于构建父级模块。

因此，我们总共有 9 个配置文件：

| Profile                    | Includes                    | Type of test enabled |
| -------------------------- | --------------------------- | -------------------- |
| default-first              | First set of projects       | *UnitTest            |
| integration-lite-first     | First set of projects       | *IntegrationTest     |
| default-second             | Second set of projects      | *UnitTest            |
| integration-lite-second    | Second set of projects      | *IntegrationTest     |
| default-heavy              | Heavy/long running projects | *UnitTest            |
| integration-heavy          | Heavy/long running projects | *IntegrationTest     |
| default-jdk9-and-above     | JDK9 and above projects     | *UnitTest            |
| integration-jdk9-and-above | JDK9 and above projects     | *IntegrationTest     |
| parents                    | Set of parent modules       | None                 |

## 构建项目

虽然不应该经常需要一次构建整个存储库，因为我们通常只关心一个特定的模块。

但是，如果我们愿意，如果我们想构建整个存储库并仅启用单元测试，我们可以从存储库的根目录调用以下命令：

`mvn clean install -Pdefault-first,default-second,default-heavy`

或者如果我们想在启用集成测试的情况下构建整个存储库，我们可以这样做：

`mvn clean install -Pintegration-lite-first,integration-lite-second,integration-heavy`

类似地，对于 JDK9 及以上项目，命令是：

`mvn clean install -Pdefault-jdk9-and-above`

and

`mvn clean install -Pintegration-jdk9-and-above`

### 构建单个模块

要构建特定模块，请运行以下命令： 在模块目录下运行 `mvn clean install` 命令。

可能出现的情况是，你的模块是父模块的一部分，如`parent-boot-1`,`parent-spring-5`等，这时你需要先构建父模块，然后才能构建你的模块。

我们创建了一个 `parents` 配置文件，你可以只用它来构建父模块，只需运行该配置文件即可：

`mvn clean install -Pparents`

### 从版本库根目录构建模块

要从版本库根目录构建特定模块，请运行以下命令： 在根目录下运行 `mvn clean install --pl asm,atomikos -Pdefault-first` 命令。

这里的 `asm` 和 `atomikos` 是我们要构建的模块，而 `default-first` 是这些模块所在的 maven 配置文件。

## 运行 Spring Boot 模块

要运行 Spring Boot 模块，请在模块目录中运行命令：`mvn spring-boot:run`。

## 使用集成开发环境

该版本库包含大量模块。
在使用单个模块时，无需导入所有模块（或构建所有模块），只需在 Eclipse 或 IntelliJ 中导入该特定模块即可。

### VSCode

- [ ] **问题** 主 pom.xml (parent-modules) 中的 java version 无法在单独加载 子项目时起作用。
      现在子项目中重新指定：`<java.version>11</java.version>`

## 运行测试

模块中的命令 `mvn clean install` 将运行该模块中的单元测试。
对于 Spring 模块，如果存在，这也将运行 `SpringContextTest`。

要运行集成测试，请使用以下命令：

`mvn clean install -Pintegration-lite-first` or

`mvn clean install -Pintegration-lite-second` or

`mvn clean install -Pintegration-heavy` or

`mvn clean install -Pintegration-jdk9-and-above`

取决于我们的模块所在的列表。
