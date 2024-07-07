# Maven编译器插件

1. 概述

    本快速教程介绍了编译器插件，这是Maven构建工具的核心插件之一。

    关于其他核心插件的概述，请参考[这篇文章](https://www.baeldung.com/core-maven-plugins)。

2. 插件目标

    编译器插件用于编译Maven项目的源代码。该插件有两个目标，它们已经与默认生命周期的特定阶段绑定：

    - compile - 编译主源文件
    - testCompile - 编译测试源文件

    下面是POM中的编译器插件：

    ```xml
    <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.7.0</version>
        <configuration>
            ...
        </configuration>
    </plugin>
    ```

3. 配置

    默认情况下，编译器插件编译的源代码与Java 5兼容，生成的类也可以在Java 5下工作，而不考虑使用的JDK。我们可以在配置元素中修改这些设置：

    ```xml
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <-- other customizations -->
    </configuration>
    ```

    为了方便，我们可以将Java版本设置为POM的属性：

    ```xml
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>
    ```

    有时我们想给javac编译器传递参数。这时，compilerArgs参数就派上用场了。

    例如，我们可以指定以下配置，让编译器对未选中的操作发出警告：

    ```xml
    <configuration>
        <!-- other configuration -->
        <compilerArgs>
            <arg>-Xlint:unchecked</arg>
        </compilerArgs>
    </configuration>
    ```

    当编译这个类时：

    maven.java9/Data.java

    我们会在控制台看到一个未被选中的警告：

    ```log
    [WARNING] ... Data.java:[7,29] unchecked conversion
    required: java.util.List<java.lang.String>
    found:    java.util.ArrayList
    ```

    由于编译器插件的两个目标都被自动绑定到Maven默认生命周期的各个阶段，我们可以用命令mvn compile和mvn test-compile执行这些目标。

4. Java 9的更新

    1. 配置

        在Java 8之前，我们使用版本号为1.x，其中x代表Java的版本，如1.8代表Java 8。

        对于Java 9及以上版本，我们可以直接使用版本号：9

        ```xml
        <configuration>
            <source>9</source>
            <target>9</target>
        </configuration>
        ```

        同样地，我们可以使用属性来定义版本，如：

        ```xml
        <properties>
            <maven.compiler.source>9</maven.compiler.source>
            <maven.compiler.target>9</maven.compiler.target>
        </properties>
        ```

        Maven在3.5.0中增加了对Java 9的支持，所以我们至少需要这个版本。我们还需要至少3.8.0的maven-compiler-plugin：

        ```xml
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.8.0</version>
                    <configuration>
                        <source>9</source>
                        <target>9</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
        ```

    2. 构建

        现在是时候测试我们的配置了。

        首先，让我们创建一个MavenCompilerPlugin类，在该类中我们要从另一个模块导入一个包。

        一个简单的包是javax.xml.XMLConstants.XML_NS_PREFIX：

        maven.java9/MavenCompilerPlugin.java

        接下来，让我们来编译它：

        `mvn -q clean compile exec:java -Dexec.mainClass="com.baeldung.maven.java9.MavenCompilerPlugin"`

        > TODO: 配置多个JAVA RUNTIME

        不过，当使用Java 9的默认值时，我们会得到一个错误：

        ```log
        [ERROR] COMPILATION ERROR :
        [ERROR] .../MavenCompilerPlugin.java:[3,20]
        package javax.xml is not visible
        (package javax.xml is declared in module java.xml,
        but module com.baeldung.maven.java9 does not read it)
        [ERROR] .../MavenCompilerPlugin.java:[3,1]
        static import only from classes and interfaces
        [ERROR] .../MavenCompilerPlugin.java:[7,62]
        cannot find symbol
        symbol:   variable XML_NS_PREFIX
        location: class com.baeldung.maven.java9.MavenCompilerPlugin
        ```

        这个错误来自于这个包在一个单独的模块中，我们在构建时还没有包含这个模块。

        解决这个问题的最简单方法是创建一个module-info.java类，并说明我们需要java.xml模块：

        ```java
        module com.baeldung.maven.java9 {
            requires java.xml;
        }
        ```

        现在我们可以再试一下。

        而我们的输出将是：`The XML namespace prefix is: xml`

5. 总结

    在这篇文章中，我们了解了编译器插件，并介绍了如何使用它。我们还了解了Maven对Java 9的支持。

## Relevant Articles

- [x] [Maven Compiler Plugin](https://www.baeldung.com/maven-compiler-plugin)

## Code

本教程的完整源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/maven-modules/compiler-plugin-java-9)上找到。
