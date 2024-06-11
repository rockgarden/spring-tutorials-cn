# [Spring Boot repackage和Maven包之间的区别](https://www.baeldung.com/spring-boot-repackage-vs-mvn-package)

1. 概述

    Apache Maven是一个广泛使用的项目依赖性管理工具和项目构建工具。

    在过去的几年里，Spring Boot已经成为一个相当流行的构建应用程序的框架。还有一个Spring Boot Maven插件，在Apache Maven中提供Spring Boot支持。

    我们知道，当我们想用Maven将应用程序打包成JAR或WAR工件时，可以使用mvn打包。然而，Spring Boot Maven插件带有一个repackage目标，它也在一个mvn命令中被调用。

    有时，这两条mvn命令会让人困惑。在本教程中，我们将讨论mvn package和spring-boot:repackage的区别。

2. 一个Spring Boot应用实例

    首先，我们将创建一个简单的Spring Boot应用程序作为例子：demo/DemoApplication.java

    为了验证我们的应用程序是否已经启动并运行，让我们创建一个简单的REST端点：demo/DemoRestController.java

3. Maven的软件包目标

    我们只需要spring-boot-starter-web依赖项来构建我们的Spring Boot应用程序，见 pom.xml。

    Maven的打包目标是将编译后的代码以可分发的格式打包，本例中是JAR格式：

    $ mvn package

    ```log
    [INFO] Scanning for projects...
    [INFO] ------< com.baeldung.spring-boot-modules:spring-boot-artifacts-2 >------
    [INFO] Building spring-boot-artifacts-2 1.0.0-SNAPSHOT
    [INFO] --------------------------------[ jar ]---------------------------------
    ... 
    [INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ spring-boot-artifacts-2 ---
    [INFO] Building jar: ... /target/spring-boot-artifacts-2.jar
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    ...
    ```

    在执行了mvn package命令后，我们可以在目标目录下找到构建的JAR文件`spring-boot-artifacts-2-1.0.0-SNAPSHOT.jar`。让我们[检查一下创建的JAR文件的内容](https://www.baeldung.com/java-view-jar-contents#reviewing-the-jar-command)：

    > 先注释掉 pom.xml 中的 build，即不采用spring-boot:repackage。

    ```bash
    $ jar tf target/spring-boot-artifacts-2-1.0.0-SNAPSHOT.jar
    META-INF/
    META-INF/MANIFEST.MF
    com/
    com/baeldung/
    com/baeldung/demo/
    META-INF/maven/
    META-INF/maven/com.baeldung.spring-boot-modules/
    META-INF/maven/com.baeldung.spring-boot-modules/spring-boot-artifacts-2/
    application.yml
    com/baeldung/demo/DemoApplication.class
    com/baeldung/demo/DemoRestController.class
    META-INF/maven/com.baeldung.spring-boot-modules/spring-boot-artifacts-2/pom.xml
    META-INF/maven/com.baeldung.spring-boot-modules/spring-boot-artifacts-2/pom.properties
    ```

    正如我们在上面的输出中所看到的，由mvn package命令创建的JAR文件只包含了我们项目源代码中的资源和已编译的Java类。

    我们可以在另一个项目中使用这个JAR文件作为依赖。但是，我们不能用java -jar JAR_FILE执行这个JAR文件，即使它是一个Spring Boot应用程序。这是因为运行时的依赖性没有被打包。例如，我们没有一个Servlet容器来启动Web上下文。

    要使用简单的java -jar命令启动我们的Spring Boot应用，我们需要构建一个胖胖的JAR。Spring Boot Maven插件可以帮助我们做到这一点。

4. Spring Boot Maven插件的重新打包目标

    现在，让我们弄清楚Spring-boot:repackage的作用。

    1. 添加Spring Boot Maven插件

        为了执行repackage目标，我们需要在pom.xml中添加Spring Boot Maven插件：spring-boot-maven-plugin。

    2. 执行 spring-boot:repackage 目标

        现在，让我们清理之前构建的JAR文件，并让spring-boot:repackage试一试：

        ```bash
        $ mvn clean spring-boot:repackage
        ...
        [INFO] --- spring-boot-maven-plugin:2.3.3.RELEASE:repackage (default-cli) @ spring-boot-artifacts-2 ---
        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD FAILURE
        [INFO] ------------------------------------------------------------------------
        ...
        [ERROR] Failed to execute goal org.springframework.boot:spring-boot-maven-plugin:2.3.3.RELEASE:repackage (default-cli) 
        on project spring-boot-artifacts-2: Execution default-cli of goal 
        org.springframework.boot:spring-boot-maven-plugin:2.3.3.RELEASE:repackage failed: Source file must not be null -> [Help 1]
        ...
        ```

        哎呀，这可不行。这是因为spring-boot:repackage目标将现有的JAR或WAR归档文件作为源文件，并将所有项目运行时的依赖性与项目类一起重新打包到最终的工件中。通过这种方式，重新打包的工件可以使用命令行java -jar JAR_FILE.jar来执行。

        因此，在执行spring-boot:repackage目标之前，我们需要首先构建JAR文件：

        `$ mvn clean package spring-boot:repackage`

    3. 重新包装后的JAR文件的内容

        现在，如果我们检查目标目录，我们会看到重新打包的JAR文件和原始JAR文件：

        ```bash
        $ ls -1 target/*jar*
        target/spring-boot-artifacts-2.jar
        target/spring-boot-artifacts-2.jar.original
        ```

        让我们检查一下重新包装后的JAR文件的内容：

        `$ jar tf target/spring-boot-artifacts-2.jar`

        如果我们检查一下上面的输出，它比mvn package命令构建的JAR文件长很多。

        `jar tf target/*.jar >> detail.txt` 输出成文件。

        在这里，在重新打包的JAR文件中，我们不仅有我们项目中编译的Java类，还有启动Spring Boot应用程序所需的所有运行时库。例如，一个嵌入式tomcat库被打包到BOOT-INF/lib目录中。

        接下来，让我们启动我们的应用程序并检查它是否工作：

        `java -jar target/spring-boot-artifacts-2.jar`

        `$ curl http://localhost:8080/welcome`

        `Welcome to Baeldung Spring Boot Demo!`

        很好! 我们已经得到了预期的响应。我们的应用程序正在正常运行。

    4. 在Maven的打包生命周期中执行spring-boot:repackage目标

        我们可以在pom.xml中配置Spring Boot Maven插件，在Maven生命周期的打包阶段重新打包工件。换句话说，当我们执行mvn package时，spring-boot:repackage将被自动执行。

        配置非常简单。我们只需将repackage目标添加到一个执行元素中：pom.xml `<build>`

        现在，让我们再运行一次mvn clean package：

        `$ mvn clean package`

        输出显示重新打包的目标已经被执行。如果我们检查文件系统，我们会发现重新打包的JAR文件已经创建：

        `$ ls -lh target/*jar*`

5. 总结

    在这篇文章中，我们已经讨论了mvn package和spring-boot:repackage的区别。

    此外，我们还学习了如何在Maven生命周期的打包阶段执行spring-boot:repackage。
