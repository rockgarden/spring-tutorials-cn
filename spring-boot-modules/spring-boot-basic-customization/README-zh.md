# Spring Boot Basic Customization

## Spring Boot配置一个主类

1. 概述

    本快速教程提供了通过Maven和Gradle定义Spring Boot应用程序入口的不同方法。

    Spring Boot应用程序的主类是一个包含公共静态void main()方法的类，用于启动Spring ApplicationContext。默认情况下，如果没有明确指定主类，Spring会在编译时在classpath中搜索一个主类，如果没有找到或找到多个主类，则无法启动。

    与传统的Java应用程序不同，本教程中讨论的主类不会作为Main-Class元数据属性出现在结果JAR或WAR文件的META-INF/MANIFEST.MF中。

    Spring Boot希望工件的Main-Class元数据属性被设置为org.springframework.boot.loader.JarLauncher（或WarLauncher），这意味着直接将我们的主类传递给java命令行将无法正确启动Spring Boot应用程序。

    一个清单的例子是这样的：

    ```txt
    Manifest-Version: 1.0
    Start-Class: com.baeldung.DemoApplication
    Main-Class: org.springframework.boot.loader.JarLauncher
    ```

    相反，我们需要在清单中定义Start-Class属性，由JarLauncher评估以启动应用程序。

    让我们看看如何使用Maven和Gradle来控制这个属性。

2. 2.Maven

    主类可以在pom.xml的属性部分定义为start-class元素：

    ```xml
    <properties>
        <!-- The main class to start by executing "java -jar" -->
        <start-class>com.baeldung.DemoApplication</start-class>
    </properties>
    ```

    注意，只有当我们在pom.xml中把spring-boot-starter-parent作为`<parent>`时，这个属性才会被评估。

    另外，主类可以在pom.xml的plugin部分定义为spring-boot-maven-plugin的mainClass元素：

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>             
                <configuration>    
                    <mainClass>com.baeldung.DemoApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```

3. Gradle

    如果我们使用Spring Boot的Gradle插件，有一些从org.springframework.boot继承的配置，我们可以在这里指定我们的主类。

    在项目的Gradle文件中，mainClassName可以在springBoot配置块中定义。BootRun和bootJar任务会接收到这里做出的改变：

    ```groovy
    springBoot {
        mainClassName = 'cpm.baeldung.DemoApplication'
    }
    ```

    另外，主类也可以定义为bootJar Gradle任务的mainClassName属性：

    ```groovy
    bootJar {
        mainClassName = 'cpm.baeldung.DemoApplication'
    }
    ```

    或者作为bootJar任务的manifest属性：

    ```groovy
    bootJar {
        manifest {
        attributes 'Start-Class': 'com.baeldung.DemoApplication'
        }
    }
    ```

    请注意，bootJar 配置块中指定的主类只影响任务本身生成的 JAR。这一变化并不影响其他Spring Boot Gradle任务的行为，如bootRun。

    作为奖励，如果Gradle应用程序插件被应用到项目中，mainClassName可以被定义为一个全局属性：

    `mainClassName = 'com.baeldung.DemoApplication'`

4. 使用CLI

    我们也可以通过命令行界面指定一个主类。

    `java -cp bootApp.jar -Dloader.main=com.baeldung.DemoApplication org.springframework.boot.loader.PropertiesLauncher`

5. 总结

    指定Spring Boot应用程序的入口点的方法不止一种。要知道，所有这些配置只是修改JAR或WAR文件清单的不同方式。

## Relevant Articles

- [How to Change the Default Port in Spring Boot](https://www.baeldung.com/spring-boot-change-port)
- [Using Custom Banners in Spring Boot](https://www.baeldung.com/spring-boot-custom-banners)
- [Create a Custom FailureAnalyzer with Spring Boot](https://www.baeldung.com/spring-boot-failure-analyzer)
- [Spring Boot: Customize Whitelabel Error Page](https://www.baeldung.com/spring-boot-custom-error-page)
- [x] [Spring Boot: Configuring a Main Class](https://www.baeldung.com/spring-boot-main-class)
- [How to Define a Spring Boot Filter?](https://www.baeldung.com/spring-boot-add-filter)
- [Guide to the Favicon in Spring Boot](https://www.baeldung.com/spring-boot-favicon)

## Code

这个Maven配置的例子可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-basic-customization)上找到。
