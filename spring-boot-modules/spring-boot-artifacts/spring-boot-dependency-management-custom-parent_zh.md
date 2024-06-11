# [使用自定义父类的Spring Boot依赖性管理](https://www.baeldung.com/spring-boot-dependency-management-custom-parent)

1. 概述

    Spring Boot提供了父级POM，以方便创建Spring Boot应用程序。

    但是，如果我们已经有一个父类可以继承，那么使用父类POM可能并不总是可取的。

    在这个快速教程中，我们将看看如何在没有父级启动器的情况下仍能使用Boot。

2. 没有父级POM的Spring Boot

    父级pom.xml负责依赖性和插件管理。由于这个原因，从它那里继承在应用程序中提供了有价值的支持，所以在创建Boot应用程序时，它通常是首选行动方案。你可以在我们之前的文章中找到更多关于如何在父级启动器的基础上建立一个应用程序的细节。

    但在实践中，我们可能会受到设计规则或其他偏好的限制而使用不同的父级。

    幸运的是，Spring Boot提供了一种从父级启动器继承的替代方案，它仍然可以为我们提供一些优势。

    如果我们不使用父级POM，我们仍然可以通过添加scope=import的spring-boot-dependencies工件从依赖性管理中获益：

    ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.4.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

    接下来，我们可以开始简单地开始添加Spring的依赖关系，并利用Spring Boot的功能：spring-boot-starter-web。

    另一方面，如果没有父级POM，我们就不能再从插件管理中获益。这意味着我们需要明确添加spring-boot-maven-plugin：

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    ```

3. 覆盖依赖关系的版本

    如果我们想对某个依赖使用不同于Boot管理的版本，我们需要在声明spring-boot-dependencies之前，在dependencyManagement部分进行声明：

    ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-jpa</artifactId>
                <version>2.4.0</version>
            </dependency>
        </dependencies>
        // ...
    </dependencyManagement>
    ```

    相比之下，仅仅在dependencyManagement标签之外为依赖关系声明版本将不再有效。

4. 总结

    在这个快速教程中，我们已经看到了如何在没有父级pom.xml的情况下使用Spring Boot。
