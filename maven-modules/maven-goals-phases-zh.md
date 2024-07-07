# Maven目标和阶段

1. 概述

    在本教程中，我们将探讨不同的Maven构建生命周期及其阶段。

    我们还将讨论目标和阶段之间的核心关系。

2. Maven构建生命周期

    Maven构建遵循特定的生命周期来部署和分发目标项目。

    有三个内置的生命周期。

    - default：主要的生命周期，因为它负责项目部署
    - clean：清理项目，删除之前构建产生的所有文件。
    - site：创建项目的网站文档。

    每个生命周期由一连串的阶段组成。默认的构建生命周期由23个阶段组成，因为它是主要的构建生命周期。

    另一方面，清洁生命周期由3个阶段组成，而站点生命周期则由4个阶段组成。

3. Maven阶段

    Maven阶段代表Maven构建生命周期中的一个阶段。每个阶段都负责一项具体任务。

    以下是默认构建生命周期中最重要的几个阶段。

    - validate验证：检查构建所需的所有信息是否可用
    - compile编译：编译源代码
    - test-compile: 编译测试源代码
    - test测试：运行单元测试
    - package打包：将编译的源代码打包成可分发的格式（jar，war，...）。
    - integration-test集成测试：如果需要的话，处理并部署软件包，以运行集成测试
    - install安装：将软件包安装到本地资源库中
    - deploy部署：将软件包复制到远程存储库中

    关于每个生命周期的阶段的完整列表，请查看[Maven参考](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference)。

    各个阶段是按特定顺序执行的。这意味着，如果我们用命令运行一个特定的阶段。

    `mvn <PHASE>`

    它不仅会执行指定的阶段，还会执行前面的所有阶段。

    例如，如果我们运行部署阶段，这是默认构建生命周期的最后一个阶段，它也将执行部署阶段之前的所有阶段，也就是整个默认生命周期。

    `mvn deploy`

4. Maven目标

    每个阶段都是一个目标序列，每个目标都负责一个特定的任务。

    当我们运行一个阶段时，所有与该阶段绑定的目标都会按顺序执行。

    以下是一些阶段和与之绑定的默认目标。

    - compiler:compile - 来自编译器插件的编译目标被绑定到编译阶段。
    - compiler:testCompile 被绑定到测试-编译阶段。
    - surefire:test 被绑定到测试阶段
    - install:install 被绑定到安装阶段
    - jar:jar 和 war:war 被绑定到打包阶段。

    我们可以使用命令列出所有绑定到特定阶段的目标和它们的插件。

    `mvn help:describe -Dcmd=PHASENAME`

    例如，要列出绑定在编译阶段的所有目标，我们可以运行。

    `mvn help:describe -Dcmd=compile`

    然后我们会得到样本输出。

    ```log
    compile' is a phase corresponding to this plugin:
    org.apache.maven.plugins:maven-compiler-plugin:3.1:compile
    ```

    如上所述，这意味着编译器插件的编译目标被绑定到编译阶段。

5. Maven插件

    一个Maven插件是一组目标；但是，这些目标不一定都绑定在同一个阶段。

    例如，下面是Maven Failsafe插件的一个简单配置，它负责运行集成测试。

    ```xml
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>${maven.failsafe.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    ```

    我们可以看到，Failsafe插件在这里配置了两个主要目标。

    - integration-test：运行集成测试
    - verify: verify 所有集成测试是否通过

    我们可以使用下面的命令来列出某个特定插件的所有目标。

    `mvn <PLUGIN>:help`

    例如，要列出Failsafe插件的所有目标，我们可以运行。

    `mvn failsafe:help`

    而输出结果将是：

    ```log
    This plugin has 3 goals:

    failsafe:help
    Display help information on maven-failsafe-plugin.
    Call mvn failsafe:help -Ddetail=true -Dgoal=<goal-name> to display parameter
    details.

    failsafe:integration-test
    Run integration tests using Surefire.

    failsafe:verify
    Verify integration tests ran using Surefire.
    ```

    要运行一个特定的目标而不执行其整个阶段（以及前面的阶段），我们可以使用命令。

    `mvn <PLUGIN>:<GOAL>`

    例如，要运行Failsafe插件的集成测试目标，我们需要运行。

    `mvn failsafe:integration-test`

6. 构建Maven项目

    要构建一个Maven项目，我们需要通过运行其中一个阶段来执行一个生命周期。

    `mvn deploy`

    这将执行整个默认生命周期。或者，我们也可以在安装阶段停止。

    `mvn install`

    但通常情况下，我们会在新的构建之前运行清洁生命周期，以清理项目。

    `mvn clean install`

    我们也可以只运行插件的一个特定目标。

    `mvn compiler:compile`

    注意，如果我们试图在不指定阶段或目标的情况下构建一个Maven项目，我们会得到一个错误。

    `[ERROR] No goals have been specified for this build. You must specify a valid lifecycle phase or a goal`

## Relevant Articles

- [x] [Maven Goals and Phases](https://www.baeldung.com/maven-goals-phases)
