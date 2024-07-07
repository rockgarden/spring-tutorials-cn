# 作为Maven属性的命令行参数

1. 概述

    在这个简短的教程中，我们将了解如何使用命令行向Maven传递参数。

2. Maven属性

    Maven属性是数值占位符。首先，我们需要在pom.xml文件的属性标签下定义它们：

    ```xml
    <properties>
        <maven.compiler.source>1.7</maven.compiler.source>
        <maven.compiler.target>1.7</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <start-class>com.example.Application</start-class>
        <commons.version>2.5</commons.version>
    </properties>
    ```

    然后，我们可以在其他标签内使用它们。例如，在这种情况下，我们将在我们的commons-io依赖中使用 "commons.version" 值：

    ```xml
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>{commons.version}</version>
    </dependency>
    ```

    事实上，我们可以在pom.xml的任何地方使用这些属性，例如在构建、包或插件部分。

3. 定义属性的占位符

    有时，我们在开发时并不知道一个属性值。在这种情况下，我们可以使用语法${some_property}留下一个占位符而不是一个值，Maven会在运行时覆盖占位符的值。让我们为COMMON_VERSION_CMD设置一个占位符：

    ```xml
    <properties>
        <maven.compiler.source>1.7</maven.compiler.source>
        <commons.version>2.5</commons.version>
        <version>${COMMON_VERSION_CMD}</version>
    </properties>
    ```

4. 向Maven传递一个参数

    现在，让我们像往常一样从终端运行Maven，比如说使用package命令。但在这种情况下，我们还要在属性名后面加上符号-D：

    `mvn package -DCOMMON_VERSION_CMD=2.5`

    Maven将使用作为参数传递的值（2.5）来替换我们pom.xml中设置的COMMON_VERSION_CMD属性。这并不局限于package命令--我们可以在任何Maven命令中一起传递参数，比如安装、测试或构建。

5. 总结

    在这篇文章中，我们研究了如何从命令行向Maven传递参数。通过使用这种方法，我们可以动态地提供属性，而不是修改pom.xml或任何静态配置。

## Relevant Articles

- [x] [Command Line Arguments as Maven Properties](https://www.baeldung.com/maven-arguments)
