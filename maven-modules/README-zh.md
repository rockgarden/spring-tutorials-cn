# Apache Maven

This module contains articles about Apache Maven. Please refer to its submodules.

## Apache Maven标准目录布局

Apache Maven是最受欢迎的Java项目构建工具之一。除了分散依赖关系和资源库外，促进项目间统一的目录结构也是其重要方面之一。

在这篇短文中，我们将探讨典型Maven项目的标准目录布局。

1. 目录布局

    一个典型的Maven项目有一个pom.xml文件和一个基于定义的惯例的目录结构。

    ```txt
    └───maven-project
        ├───pom.xml
        ├───README.txt
        ├───NOTICE.txt
        ├───LICENSE.txt
        └───src
            ├───main
            │   ├───java
            │   ├───resources
            │   ├───filters
            │   └───webapp
            ├───test
            │   ├───java
            │   ├───resources
            │   └───filters
            ├───it
            ├───site
            └───assembly
    ```

    默认的目录布局可以用项目描述符来覆盖，但这是不常见的，也是不鼓励的。

2. 根目录

    该目录是每个Maven项目的根目录。

    让我们仔细看看根目录下通常有哪些标准文件和子目录。

    - maven-project/pom.xml--定义Maven项目构建周期中需要的依赖和模块
    - maven-project/LICENSE.txt - 项目的许可信息
    - maven-project/README.txt - 项目概要
    - maven-project/NOTICE.txt - 关于项目中使用的第三方库的信息
    - maven-project/src/main - 包含源代码和资源，成为工件的一部分
    - maven-project/src/test - 包含所有测试代码和资源
    - maven-project/src/it - 通常保留给Maven Failsafe插件使用的集成测试
    - maven-project/src/site - 使用Maven网站插件创建的网站文档
    - maven-project/src/assembly - 用于打包二进制文件的汇编配置

3. src/main目录

    正如其名，src/main是Maven项目中最重要的目录。任何应该成为工件一部分的东西，无论是jar还是war，都应该出现在这里。

    - src/main/java - 神器的Java源代码
    - src/main/resources - 配置文件和其他文件，如i18n文件、每个环境的配置文件和XML配置。
    - src/main/webapp - 用于Web应用程序，包含JavaScript、CSS、HTML文件、视图模板和图片等资源
    - src/main/filters --包含在构建阶段向资源文件夹中的配置属性注入数值的文件。

4. src/test目录

    src/test目录是应用程序中每个组件的测试所在的地方。

    注意，这些目录或文件都不会成为工件的一部分。我们来看看它的子目录。

    - src/test/java - 用于测试的Java源代码
    - src/test/resources - 测试使用的配置文件和其他文件
    - src/test/filters - 包含在测试阶段向资源文件夹中的配置属性注入数值的文件

## Maven打包类型

打包类型是任何Maven项目的一个重要方面。它指定项目生成的工件类型。通常，构建生成jar、war、pom或其他可执行文件。

Maven提供了许多默认的打包类型，还提供了定义自定义打包类型的灵活性。

在本教程中，我们将深入了解Maven打包类型。首先，我们将查看Maven中的构建生命周期。然后，我们将讨论每种包装类型，它们代表什么，以及它们对项目生命周期的影响。最后，我们将了解如何定义自定义包装类型。

1. 默认包装类型

    Maven提供了许多默认的打包类型，包括jar、war、ear、pom、rar、ejb和maven-plugin。每种打包类型都遵循由阶段组成的构建生命周期。通常，每个阶段都是一系列目标，并执行特定任务。

    不同的包装类型在特定阶段可能有不同的目标。例如，在jar打包类型的打包阶段，执行maven-jar-plugin的jar目标。相反，对于war项目，maven-war-plugin的war目标在同一阶段执行。

    1. jar

        Java archive（或jar）是最流行的打包类型之一。具有这种打包类型的项目生成一个扩展名为.jar的压缩zip文件。它可能包括纯Java类、接口、资源和元数据文件。

        首先，让我们看看为jar构建阶段绑定的一些默认目标：

        - resources: resources
        - compiler: compile
        - resources: testResources
        - compiler: testCompile
        - surefire: test
        - jar: jar
        - install: install
        - deploy: deploy

        毫不拖延，让我们定义jar项目的包装类型：

        `<packaging>jar</packaging>`

        如果没有指定任何内容，Maven会假设包装类型是jar。

    2. war

        简单地说，web应用程序存档（或war）包含与web应用程序相关的所有文件。它可能包括Javaservlet、JSP、HTML页面、部署描述符和相关资源。总的来说，war与jar有着相同的目标绑定，但有一个例外——战争的打包阶段有一个不同的目标，那就是war。

        毫无疑问，jar和war是Java社区中最流行的打包类型。这两者之间的[详细差异](#jar和war打包的区别)。

        让我们定义web应用程序的打包类型：

        `<packaging>war</packaging>`

        其他打包类型ejb、par和rar也有类似的生命周期，但每个打包类型都有不同的打包目标。

        ejb:ejb或par:par或rar:rar

    3. ear

        企业应用程序存档（或ear）是包含J2EE应用程序的压缩文件。它由一个或多个模块组成，这些模块可以是web模块（打包为war文件）或EJB模块（打包成jar文件），也可以是两者。

        换言之，ear是jar和war的超集，需要一个应用程序服务器来运行应用程序，而war只需要一个web容器或web服务器来部署它。区分web服务器和应用程序服务器的方面，以及[Java中流行的服务器](https://www.baeldung.com/java-servers)是什么，对于Java开发人员来说都是重要的概念。

        让我们定义ear的默认目标绑定：

        - ear: generate-application-xml
        - resources: resources
        - ear: ear
        - install: install
        - deploy: deploy

        以下是我们如何定义此类项目的包装类型：

        `<packaging>ear</packaging>`

    4. pom

        在所有包装类型中，pom是最简单的一种。它有助于创建聚合器和父项目。

        聚合器或多模块项目组装来自不同来源的子模块。这些子模块是常规的Maven项目，遵循自己的构建生命周期。聚合器POM在modules元素下具有子模块的所有引用。

        父项目允许您定义POM之间的继承关系。父POM共享某些配置、插件和依赖项及其版本。父级的大多数元素都由其子级继承-例外包括artifactId、name和前提条件。

        因为没有要处理的资源，也没有要编译或测试的代码。因此，pom项目的工件会自己生成，而不是生成任何可执行文件。

        让我们定义多模块项目的打包类型：

        `<packaging>pom</packaging>`

        此类项目的生命周期最简单，只有两个步骤：安装和部署。

    5. maven-plugin

        Maven提供了各种有用的插件。然而，在某些情况下，默认插件不够。在这种情况下，该工具提供了根据项目需要创建[maven插件](https://www.baeldung.com/maven-plugin)的灵活性。

        要创建插件，请设置项目的打包类型：

        `<packaging>maven-plugin</packaging>`

        maven插件的生命周期类似于jar的生命周期，但有两个例外：

        - plugin：描述符绑定到生成资源阶段
        - plugin：addPluginArtifactMetadata添加到包阶段

        对于这种类型的项目，需要maven-plugin-api 依赖关系。

    6. ejb

        Enterprise Java Beans（或[ejb](https://www.baeldung.com/ejb-intro)）有助于创建可扩展的分布式服务器端应用程序。EJB通常提供应用程序的业务逻辑。典型的EJB体系结构由三个组件组成：Enterprise Java Beans（EJBs）、EJB container和应用程序服务器。

        现在，让我们定义EJB项目的打包类型：

        `<package>ejb</package>`

        ejb打包类型也具有与jar打包类似的生命周期，但具有不同的打包目标。这类项目的包目标是ejb:ejb。

        具有ejb打包类型的项目需要一个maven ejb插件来执行生命周期目标。Maven提供对EJB2和EJB3的支持。如果未指定版本，则使用默认版本2。

    7. rar

        Resource adapter 资源适配器（或rar）是一个存档文件，它是将资源适配器部署到应用程序服务器的有效格式。基本上，它是一个将Java应用程序连接到企业信息系统（Enterprise information system EIS）的系统级驱动程序。

        以下是资源适配器的打包类型声明：

        `<package>rar</package>`

        每个资源适配器归档文件由两部分组成：包含源代码的jar文件和充当部署描述符的ra.xml。

        同样，生命周期阶段与jar或war打包相同，只有一个例外：打包阶段执行rar目标，该目标由一个maven-rar-plugin组成，用于打包归档文件。

2. 其他包装类型

    到目前为止，我们已经研究了Maven提供的各种默认打包类型。现在，让我们假设我们希望我们的项目生成一个带有.zip扩展名的工件。在这种情况下，默认的包装类型对我们没有帮助。

    Maven还通过插件提供了更多的打包类型。借助这些插件，我们可以定义自定义打包类型及其构建生命周期。其中一些类型包括：

    - msi
    - rpm
    - tar
    - tar.bz2
    - tar.gz
    - tbz
    - zip

    要定义自定义类型，我们必须定义其包装类型及其生命周期中的阶段。为此，在src/main/resources/META-INF/plush目录下创建components.xml文件：

    ```xml
    <component>
    <role>org.apache.maven.lifecycle.mapping.LifecycleMapping</role>
    <role-hint>zip</role-hint>
    <implementation>org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping</implementation>
    <configuration>
        <phases>
            <process-resources>org.apache.maven.plugins:maven-resources-plugin:resources</process-resources>
            <package>com.baeldung.maven.plugins:maven-zip-plugin:zip</package>
            <install>org.apache.maven.plugins:maven-install-plugin:install</install>
            <deploy>org.apache.maven.plugins:maven-deploy-plugin:deploy</deploy>
        </phases>
    </configuration>
    </component>
    ```

    到目前为止，Maven对我们的新包装类型及其生命周期一无所知。为了使其可见，让我们在项目的pom文件中添加插件，并将扩展名设置为true：

    ```xml
    <plugins>
        <plugin>
            <groupId>com.baeldung.maven.plugins</groupId>
            <artifactId>maven-zip-plugin</artifactId>
            <extensions>true</extensions>
        </plugin>
    </plugins>
    ```

    现在，该项目将可用于扫描，系统也将查看插件和components.xml文件。

    除了所有这些类型之外，Maven还通过外部项目和插件提供了许多其他打包类型。例如，nar（native archive）、swf和swc是生成Adobe Flash和Flex内容的项目的打包类型。对于这样的项目，我们需要一个定义自定义打包的插件和一个包含该插件的存储库。

## Maven快照版本库与发布版本库对比

1. 概述

    在本教程中，我们将解释 Maven 快照仓库与发布仓库之间的区别。

2. Maven 资源库

    Maven 资源库包含一系列预编译的工件，我们可以在应用程序中将其作为依赖项使用。在传统的 Java 应用程序中，这些通常是 .jar 文件。

    一般来说，有两种类型的资源库：本地和远程。

    本地版本库是 Maven 在其构建计算机上创建的版本库。它通常位于 $HOME/.m2/repository 目录下。

    当我们构建应用程序时，Maven 会在本地资源库中搜索依赖项。如果找不到某个依赖项，Maven 会在远程资源库（在 settings.xml 或 pom.xml 文件中定义）中搜索。此外，它还会将该依赖项复制到本地版本库，以供将来使用。

    远程资源库是一个包含构件的外部资源库。一旦 Maven 从远程资源库下载了工件，它就会倾向于在本地资源库中查找该工件，以限制工件的下载。

    此外，我们还可以根据工件类型将版本库区分为快照版本库和发布版本库。

3. 快照资源库

    快照(Snapshot)版本库是用于增量、未发布的工件版本的版本库。

    快照版本是尚未发布的版本。一般的想法是在发布版本之前有一个快照版本。它允许我们增量部署相同的暂存版本，而不要求项目升级它们正在使用的构件版本。这些项目可以使用相同的版本来获取更新的快照版本。

    例如，在发布 1.0.0 版本之前，我们可以拥有它的快照版本。快照版本的后缀是 SNAPSHOT（例如 1.0.0-SNAPSHOT）。

    1. 部署工件

        持续开发通常使用快照版本。通过快照版本，我们可以部署一个工件，其编号由时间戳和构建编号组成。

        假设我们有一个正在开发的项目，其版本为 SNAPSHOT：

        ```xml
        <groupId>com.baeldung</groupId>
        <artifactId>maven-snapshot-repository</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        ```

        我们将把项目部署到自托管的 Nexus 资源库中。

        首先，让我们定义要部署工件的发布仓库信息。我们可以使用发布管理插件：

        ```xml
        <distributionManagement>
            <snapshotRepository>
                <id>nexus</id>
                <name>nexus-snapshot</name>
                <url>http://localhost:8081/repository/maven-snapshots/</url>
            </snapshotRepository>
        </distributionManagement>
        ```

        然后，我们将使用 mvn deploy 命令部署我们的项目。

        部署后，实际工件版本将包含一个时间戳值，而不是 SNAPSHOT 值。例如，当我们部署 1.0.0-SNAPSHOT 时，实际值将包含当前时间戳和构建号（如 1.0.0-20220709.063105-3）。

        时间戳值是在工件部署过程中计算得出的。Maven 会生成校验和，并以相同的时间戳上传工件文件。

        maven-metadata.xml 文件包含快照版本的精确信息及其与最新时间戳值的链接：

        ```xml
        <metadata modelVersion="1.1.0">
            <groupId>com.baeldung</groupId>
            <artifactId>maven-snapshot-repository</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <versioning>
                <snapshot>
                    <timestamp>20220709.063105</timestamp>
                    <buildNumber>3</buildNumber>
                </snapshot>
                <lastUpdated>20220709063105</lastUpdated>
                <snapshotVersions>
                    <snapshotVersion>
                        <extension>jar</extension>
                        <value>1.0.0-20220709.063105-3</value>
                        <updated>20220709063105</updated>
                    </snapshotVersion>
                    <snapshotVersion>
                        <extension>pom</extension>
                        <value>1.0.0-20220709.063105-3</value>
                        <updated>20220709063105</updated>
                    </snapshotVersion>
                </snapshotVersions>
            </versioning>
        </metadata>
        ```

        元数据文件有助于管理从快照版本到时间戳值的转换。

        每次在同一快照版本下部署项目时，Maven 都会生成包含新时间戳值和新构建编号的版本。

    2. 下载工件

        在下载快照工件之前，Maven 会下载其相关的 maven-metadata.xml 文件。这样，Maven 就能根据时间戳值和版本号检查是否有更新的版本。

        检索此类构件时，仍可使用 SNAPSHOT 版本。

        要从版本库中下载工件，首先，我们需要定义一个依赖关系版本库：

        ```xml
        <repositories>
            <repository>
                <id>nexus</id>
                <name>nexus-snapshot</name>
                <url>http://localhost:8081/repository/maven-snapshots/</url>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
                <releases>
                    <enabled>false</enabled>
                </releases>
            </repository>
        </repositories>
        ```

        快照版本默认未启用。我们需要手动启用它们：

        ```xml
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        ```

        启用快照后，我们就可以定义检查 SNAPSHOT 工件更新版本的频率。不过，默认更新策略设置为每天一次。我们可以通过设置不同的更新策略来覆盖这一行为：

        ```xml
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
        ```

        我们可以在 updatePolicy 元素中加入四种不同的值：

        - always - 每次检查更新版本
        - daily（默认值）- 每天检查一次更新版本
        - interval:mm - 根据以分钟为单位设置的时间间隔检查更新版本
        - never - 从不尝试获取更新版本（与本地已有版本相比）

        此外，我们可以在命令中通过 -U 参数强制更新所有快照工件，而不是定义 updatePolicy：

        `mvn install -U`

        此外，如果依赖项已经下载，且校验和与本地版本库中的校验和相同，则不会重新下载。

        接下来，我们可以在项目中添加一个工件的快照版本：

        ```xml
        <dependencies>
            <dependency>
                <groupId>com.baeldung</groupId>
                <artifactId>maven-snapshot-repository</artifactId>
                <version>1.0.0-SNAPSHOT</version>
            </dependency>
        </dependencies>
        ```

        在开发阶段使用快照版本可以避免工件出现多个版本。我们可以使用相同的 SNAPSHOT 版本，其构建将包含特定时间的代码快照。

4. 发布仓库

    发布仓库包含工件的最终版本（发布）。简而言之，发布工件代表其内容不应被修改的工件。

    所有在 settings.xml 或 pom.xml 文件中定义的版本库都默认启用发布版本库。

    1. 部署工件

        现在，让我们在本地 Nexus 版本库中部署项目。假设我们已经完成开发，准备发布项目：

        ```xml
        <groupId>com.baeldung</groupId>
        <artifactId>maven-release-repository</artifactId>
        <version>1.0.0</version>
        ```

        一旦我们删除了项目版本中的 SNAPSHOT 字样，在部署过程中就会自动选择发布版本库，而不是快照版本库。

        此外，如果我们想在同一版本下重新部署工件，可能会出现错误： "版本库不允许更新资产"。一旦我们部署了已发布的工件版本，就无法更改其内容。因此，要解决这个问题，我们只需发布下一个版本即可。

    2. 下载构件

        Maven 默认从 [Maven Central Repository](https://repo1.maven.org/maven2) 中查找组件。该资源库默认使用发布版本策略。

        发布版本库只解析已发布的工件。换句话说，它应该只包含已发布的工件版本，其内容在未来不会改变。

        如果我们想下载已发布的工件，就需要定义该资源库：

        ```xml
        <repository>
            <id>nexus</id>
            <name>nexus-release</name>
            <url>http://localhost:8081/repository/maven-releases/</url>
        </repository>
        ```

        最后，我们只需将发布版本添加到项目中即可：

        ```xml
        <dependencies>
            <dependency>
                <groupId>com.baeldung</groupId>
                <artifactId>maven-release-repository</artifactId>
                <version>1.0.0</version>
            </dependency>
        </dependencies>
        ```

5. 总结

在本教程中，我们了解了 Maven 快照存储库和发布存储库的区别。总之，对于仍在开发中的项目，我们应该使用快照版本库，而对于准备投入生产的项目，我们应该使用发布版本库。

## Relevant Articles

- [x] [Apache Maven Standard Directory Layout](https://www.baeldung.com/maven-directory-structure)
- [x] [Maven Packaging Types](https://www.baeldung.com/maven-packaging-types)
- [x] [Maven Snapshot Repository vs Release Repository](https://www.baeldung.com/maven-snapshot-release-repository)

## Code

Baeldung上的所有代码示例都是用Maven构建的。请务必查看我们在[GitHub](https://github.com/eugenp/tutorials/tree/master/maven-modules)上的各种Maven配置。
