# [用Spring Boot创建一个胖jar应用程序](https://www.baeldung.com/deployable-fat-jar-spring-boot)

1. 简介

    近年来，令人振奋的发展之一是不断简化网络应用的部署方式。

    跳过所有无聊的中间历史步骤，我们来到了今天--我们不仅可以省去繁琐的Servlet和XML模板，而且主要是省去了服务器本身。

    本文将集中讨论如何从Spring Boot应用程序中创建一个 "fat jar"--基本上是创建一个易于部署和运行的单一工件。

    Boot提供了开箱即用的无容器部署功能：我们所要做的就是在pom.xml中添加几个配置：

    - spring-boot-starter-web
    - spring-boot-maven-plugin

2. 构建和运行

    有了这个配置，我们现在可以简单地用标准的mvn clean install来构建项目--这里没有什么特别的。

    我们用下面的命令运行它：`java -jar <artifact-name>` - 非常简单和直观。

    正确的进程管理超出了本文的范围，但有一个简单的方法可以让进程在我们退出服务器时仍然运行，那就是使用nohup命令：`nohup java -jar <artifact-name>`。

    停止spring-boot项目也和停止普通进程没有区别，无论我们是简单的cntrl+c还是`kill <pid>`。

3. Fat Jar / Fat War

    在幕后，spring-boot将所有项目的依赖关系与项目类一起打包在最终的工件中（因此称为 "fat"jar）。一个嵌入式的Tomcat服务器也被内置。

    因此，产生的工件是完全独立的，易于使用标准的Unix工具（scp、sftp...等）进行部署，并且可以在任何有JVM的服务器上运行。

    默认情况下，Boot会创建一个jar文件，但如果我们把pom.xml中的打包属性改为war，Maven就会自然而然地构建一个war。

    当然，这既可以作为独立程序执行，也可以部署到Web容器中。

4. 进一步配置

    大多数情况下不需要额外的配置，一切都 "正常"，但在某些特定情况下，我们可能需要明确告诉spring-boot主类是什么。一种方法是添加一个属性：`<start-class>`。

    如果我们==没有继承spring-boot-starter-parent==，我们就需要在Maven插件中这样做：

    ```xml
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>2.4.0</version>
        <configuration>
            <mainClass>org.baeldung.boot.Application</mainClass>
            <layout>ZIP</layout>
        </configuration>
    </plugin>
    ```

    在一些罕见的情况下，我们可能需要做的另一件事是指示Maven解压一些依赖：

    ```xml
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <requiresUnpack>
                <dependency>
                    <groupId>org.jruby</groupId>
                    <artifactId>jruby-complete</artifactId>
                </dependency>
            </requiresUnpack>
        </configuration>
    </plugin>
    ```

5. 总结

    在这篇文章中，我们研究了使用spring-boot构建的 "fat" jar进行无服务器部署。
