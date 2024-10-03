# [Spring Boot Gradle 插件](https://www.baeldung.com/spring-boot-gradle-plugin)

1. 概述

    Spring Boot Gradle 插件可以帮助我们管理 Spring Boot 依赖关系，并在使用 Gradle 作为构建工具时打包和运行应用程序。

    在本教程中，我们将讨论如何添加和配置该插件，然后了解如何构建和运行 Spring Boot 项目。

2. 构建文件配置

    首先，我们需要在 build.gradle 文件的插件部分添加 Spring Boot 插件：

    ```gradle
    plugins {
        id "org.springframework.boot" version "2.0.1.RELEASE"
    }
    ```

    如果我们使用的 Gradle 版本早于 2.1，或者我们需要动态配置，我们可以这样添加：

    ```gradle
    buildscript {
        ext {
            springBootVersion = '2.0.1.RELEASE'
        }
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath(
            "org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        }
    }
    apply plugin: 'org.springframework.boot'
    ```

3. 打包应用程序

    我们可以使用构建命令将应用程序打包成可执行压缩文件（jar 或 war 文件）：

    `./gradlew build`

    这样，生成的可执行文件将被放置在 build/libs 目录中。

    如果要生成可执行 jar 文件，还需要应用 java 插件：

    `apply plugin: 'java'`

    另一方面，如果我们需要 war 文件，我们将应用 war 插件：

    `apply plugin: 'war'`

    构建应用程序会生成 Spring Boot 1.x 和 2.x 的可执行文件，但对于每个版本，Gradle 会触发不同的任务。

    接下来，让我们仔细看看每个 Boot 版本的构建过程。

    1. Spring Boot 2.x

        在 Boot 2.x 中，bootJar 和 bootWar 任务负责打包应用程序。

        bootJar 任务负责创建可执行 jar 文件。一旦应用了 java 插件，它就会自动创建。

        让我们看看如何直接执行 bootJar 任务：

        `./gradlew bootJar`

        同样，bootWar 会生成可执行 war 文件，并在应用 war 插件后自动创建。

        我们可以使用以下命令执行 bootWar 任务

        `./gradlew bootWar`

        注意，对于 Spring Boot 2.x，我们需要使用 Gradle 4.0 或更高版本。

        我们还可以配置这两个任务。例如，使用 mainClassName 属性设置主类：

        ```gradle
        bootJar {
            mainClassName = 'com.baeldung.Application'
        }
        ```

        或者，我们也可以使用 Spring Boot DSL 中的相同属性：

        ```gradle
        springBoot {
            mainClassName = 'com.baeldung.Application'
        }
        ```

    2. Spring Boot 1.x

        在 Spring Boot 1.x 中，bootRepackage 负责创建可执行归档文件（jar 或 war 文件，具体取决于配置）。

        我们可以使用以下命令直接执行 bootRepackage 任务

        `./gradlew bootRepackage`

        与 Boot 2.x 版本类似，我们可以在 build.gradle 中为 bootRepackage 任务添加配置：

        ```gradle
        bootRepackage {
            mainClass = 'com.example.demo.Application'
        }
        ```

        我们还可以通过将 enabled 选项设置为 false 来禁用 bootRepackage 任务：

        ```gradle
        bootRepackage {
            enabled = false
        }
        ```

4. 运行应用程序

    构建应用程序后，我们只需在生成的可执行 jar 文件上使用 java -jar 命令即可运行它：

    `java -jar build/libs/demo.jar`

    Spring Boot Gradle 插件还为我们提供了 bootRun 任务，让我们无需先构建应用程序就能运行它：

    `./gradlew bootRun`

    bootRun 任务可以在 build.gradle 中简单配置。

    例如，我们可以定义主类：

    ```gradle
    bootRun {
        main = "com.example.demo.Application
    }
    ```

5. 与其他插件的关系

    1. 依赖关系管理插件

        Spring Boot 1.x 会自动应用依赖关系管理插件。它会导入 Spring Boot 依赖 BOM，其作用类似于 Maven 的依赖管理。

        但从 Spring Boot 2.x 开始，如果我们需要这项功能，就需要在 build.gradle 中显式地应用它：

        `apply plugin: 'io.spring.dependency-management'`

    2. Java 插件

        当我们应用 java 插件时，Spring Boot Gradle 插件会执行多种操作，例如

        - 创建 bootJar 任务，用于生成可执行 jar 文件
        - 创建 bootRun 任务，直接运行应用程序
        - 禁用 jar 任务

    3. War 插件

        同样，当我们应用 war 插件时，会导致

        - 创建 bootWar 任务，我们可以用它来生成可执行 war 文件
        - 禁用 war 任务

6. 总结

在本快速教程中，我们了解了 Spring Boot Gradle 插件及其不同任务。

此外，我们还讨论了它与其他插件的交互方式。
