# 在Maven中设置Java版本

在这个快速教程中，我们将展示如何在Maven中设置Java版本。

在继续之前，我们可以查看Maven的默认JDK版本。运行mvn -v命令将显示Maven所运行的Java版本。

1. 使用编译器插件

    我们可以在[编译器插件](https://www.baeldung.com/maven-compiler-plugin)中指定所需的Java版本。

    1. 编译器插件

        第一个选择是在编译器插件属性中设置版本。

        ```xml
        <properties>
            <maven.compiler.target>1.8</maven.compiler.target>
            <maven.compiler.source>1.8</maven.compiler.source>
        </properties>
        ```

        Maven编译器接受该命令的-target和-source版本。如果我们想使用Java 8的语言特性，-source应设置为1.8。

        另外，为使编译后的类与JVM 1.8兼容，-target值也应是1.8。

        另外，我们也可以直接配置编译器插件。

        ```xml
        <plugins>
            <plugin>    
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
        ```

        maven-compiler-plugin还有其他配置属性，除了-source和-target版本外，我们还可以对编译过程进行更多控制。

    2. Java 9及其他

        此外，从JDK 9版本开始，我们可以使用一个新的-release命令行选项。这个新的参数将自动配置编译器，使其产生的类文件与给定平台版本的实现相链接。

        默认情况下，-source和-target选项并不能保证交叉编译。

        这意味着我们不能在旧版本的平台上运行我们的应用程序。此外，为了编译和运行旧版Java的程序，我们还需要指定-bootclasspath选项。

        为了正确地进行交叉编译，新的-release选项取代了三个标志。-source、-target和-bootclasspath。

        在转换了我们的例子之后，我们可以为插件的属性声明如下。

        ```xml
        <properties>
            <maven.compiler.release>17</maven.compiler.release>
        </properties>
        ```

        而对于从3.6版本开始的maven-compiler-plugin，我们可以这样写。

        ```xml
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.0</version>
            <configuration>
                <release>7</release>
            </configuration>
        </plugin>
        ```

        注意，我们可以在一个新的`<release>`属性中添加Java版本。在这个例子中，我们为Java 7编译了我们的应用程序。

        更重要的是，我们不需要在机器上安装JDK 7。Java 9已经包含了所有用于将新的语言特性与JDK 7连接起来的信息。

2. Spring Boot规范

    Spring Boot应用程序在pom.xml文件的属性标签内指定JDK版本。

    首先，我们需要添加spring-boot-starter-parent作为我们项目的父级。

    这个父级POM允许我们配置默认插件和多个属性，包括Java版本 - 默认情况下，Java版本是1.8。

    然而，我们可以通过指定java.version属性来覆盖父类的默认版本。

    ```xml
    <properties>
        <java.version>9</java.version>
    </properties>
    ```

    通过设置java.version属性，我们声明源和目标Java版本都等于1.9。

    最重要的是，我们应该记住，这个属性是Spring Boot的规范。此外，从Spring Boot 2.0开始，Java 8是最低版本。

    这意味着我们不能为旧的JDK版本使用或配置Spring Boot。

3. 总结

    这个快速教程展示了在我们的Maven项目中设置Java版本的可能方式。

    - 使用`<java.version>`只有在Spring Boot应用中才能实现。
    - 对于简单情况，maven.compiler.source和maven.compiler.target属性应该是最合适的。
    - 最后，为了对编译过程有更多控制，可以使用maven-compiler-plugin配置设置。

## Relevant Articles

- [x] [Setting the Java Version in Maven](https://www.baeldung.com/maven-java-version)
