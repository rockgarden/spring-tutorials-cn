# [用Maven和可执行的War/Jar运行Spring Boot应用程序](https://www.baeldung.com/spring-boot-run-maven-vs-executable-jar)

1. 简介

    在本教程中，我们将探讨通过mvn spring-boot:run命令启动Spring Boot网络应用，与通过java -jar命令将其编译成jar/war包后运行的区别。

    在本教程中，我们将假设对Spring Boot重新打包目标的配置很熟悉。有关这一主题的更多细节，请阅读《[用Spring Boot创建一个Fat Jar应用程序](https://www.baeldung.com/deployable-fat-jar-spring-boot)》。

2. Spring Boot Maven插件

    在编写Spring Boot应用程序时，[Spring Boot Maven插件](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)是推荐用于构建、测试和打包代码的工具。

    这个插件有很多方便的功能，比如说：

    - 它为我们解决了正确的依赖版本
    - 它能将我们所有的依赖项（包括嵌入式应用服务器，如果需要的话）打包在一个可运行的胖jar/war中，并且还能
    - 为我们管理classpath配置，所以我们可以跳过java -jar命令中的长的-cp选项
    - 实现一个自定义的ClassLoader来定位和加载所有现在嵌套在包内的外部jar库
    - 自动找到main()方法并将其配置在清单中，这样我们就不必在java -jar命令中指定main类。

3. 用Maven运行代码，以爆炸的形式出现

    当我们在开发网络应用时，我们可以利用Spring Boot Maven插件的另一个非常有趣的功能：在嵌入式应用服务器中自动部署网络应用的能力。

    我们只需要一个依赖项，让插件知道我们想用Tomcat来运行我们的代码：spring-boot-starter-web。

    现在，当在我们的项目根目录下执行`mvn spring-boot:run`命令时，该插件会读取pom配置并理解我们需要一个Web应用容器。

    执行mvn spring-boot:run命令会触发Apache Tomcat的下载并初始化Tomcat的启动：

    $ mvn spring-boot:run

    ```log
    ...
    [INFO] --------------------< com.baeldung:spring-boot-ops >--------------------
    [INFO] Building spring-boot-ops 0.0.1-SNAPSHOT
    [INFO] --------------------------------[ war ]---------------------------------
    [INFO]
    [INFO] >>> spring-boot-maven-plugin:2.1.3.RELEASE:run (default-cli) > test-compile @ spring-boot-ops >>>
    Downloading from central: https://repo.maven.apache.org/maven2/org/apache/tomcat/embed/tomcat-embed-core/9.0.16/tomcat-embed-core-9.0.16.pom
    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/tomcat/embed/tomcat-embed-core/9.0.16/tomcat-embed-core-9.0.16.pom (1.8 kB at 2.8 kB/s)
    ...
    [INFO] --- spring-boot-maven-plugin:2.1.3.RELEASE:run (default-cli) @ spring-boot-ops ---
    ...
    11:33:36.648 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
    11:33:36.649 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/9.0.16]
    ...
    11:33:36.952 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
    ...
    11:33:48.223 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-8080"]
    11:33:48.289 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) with context path ''
    11:33:48.292 [main] INFO  org.baeldung.boot.Application - Started Application in 22.454 seconds (JVM running for 37.692)
    ```

    当日志中显示包含'Started Application'的一行时，我们的网络应用就可以通过浏览器查询了，地址是<http://localhost:8080/>

4. 将代码作为独立打包的应用程序运行

    一旦我们通过了开发阶段，将我们的应用程序推向生产，我们就需要对我们的应用程序进行打包。

    不幸的是，如果我们使用的是jar包，Maven的基本打包目标不包括任何外部依赖。这意味着我们只能将其作为更大项目中的一个库。

    为了规避这一限制，我们需要利用Maven Spring Boot插件重新打包目标，将我们的jar/war作为一个独立的应用程序运行。

    1. 配置

        通常情况下，我们只需要配置构建插件：spring-boot-maven-plugin。

        由于我们的示例项目包含不止一个主类，我们必须告诉Java要运行哪个类，可以通过配置插件：

        ```java
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <configuration>
                        <mainClass>com.baeldung.webjar.WebjarsdemoApplication</mainClass>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        ```

        或设置start-class属性：

        ```xml
        <properties>
            <start-class>com.baeldung.webjar.WebjarsdemoApplication</start-class>
        </properties>
        ```

    2. 运行应用程序

        现在我们可以用两个简单的命令运行我们的示例战争：

        ```bash
        mvn clean package spring-boot:repackage
        java -jar target/spring-boot-artifacts.war
        ```

        关于如何运行jar文件的更多细节可以在我们的文章《[用命令行参数运行JAR应用程序](https://www.baeldung.com/java-run-jar-with-arguments)》中找到。

    3. war文件内部

        为了更好地理解上述命令如何运行一个完整的服务器应用程序，我们可以看一下我们的spring-boot-ops.war。

        如果我们将其解压并窥探其内部，我们会发现通常的suspects：

        - META-INF，包括自动生成的MANIFEST.MF
        - WEB-INF/classes，包含我们编译的类
        - WEB-INF/lib，包含我们的war依赖和嵌入式Tomcat jar文件。

        这还不是全部，还有一些专门针对我们的胖子包配置的文件夹：

        - WEB-INF/lib-provided，包含运行嵌入式时需要的外部库，但在部署时不需要。
        - org/springframework/boot/loader，其中包含Spring Boot自定义类加载器。这个库负责加载我们的外部依赖项，并使它们在运行时可以访问。

    4. war清单内部

        如前所述，Maven Spring Boot插件找到了主类，并生成了运行java命令所需的配置。

        生成的MANIFEST.MF有一些额外的行：

        ```txt
        Start-Class: com.baeldung.webjar.WebjarsdemoApplication
        Main-Class: org.springframework.boot.loader.WarLauncher
        ```

        特别是，我们可以观察到，最后一行指定了要使用的Spring Boot类加载器启动器。

    5. Jar文件内部

        由于采用了默认的打包策略，无论我们是否使用Spring Boot Maven插件，我们的war打包方案都没有什么不同。

        为了更好地体会该插件的优势，我们可以尝试将pom打包配置改为jar，并再次运行mvn clean package。

        现在我们可以观察到，我们的fat jar与之前的war文件的组织方式有些不同：

        - 我们所有的类和资源文件夹现在都位于BOOT-INF/classes下。
        - BOOT-INF/lib存放所有的外部库。

        如果没有这个插件，lib文件夹就不会存在，而BOOT-INF/classes的所有内容都会位于包的根部。

    6. Jar Manifest的内部

        MANIFEST.MF也发生了变化，特点是增加了这些行：

        ```txt
        Spring-Boot-Classes: BOOT-INF/classes/
        Spring-Boot-Lib: BOOT-INF/lib/
        Spring-Boot-Version: 2.1.3.RELEASE
        Main-Class: org.springframework.boot.loader.JarLauncher
        ```

        Spring-Boot-Classes和Spring-Boot-Lib特别有趣，因为它们告诉我们类加载器将在哪里找到类和外部库。

5. 如何选择

    在分析工具时，我们必须考虑到这些工具的创建目的。我们是想减轻开发工作，还是想确保顺利部署和可移植性？让我们看一下受这种选择影响最大的阶段。

    1. 开发

        作为开发者，我们常常把大部分时间花在编码上，而不需要花很多时间来设置环境以在本地运行代码。在简单的应用程序中，这通常不是一个问题。但对于更复杂的项目，我们可能需要设置环境变量，启动服务器，并填充数据库。

        每次我们想运行应用程序时，配置正确的环境是非常不切实际的，尤其是当有多个服务需要同时运行时。

        这就是用Maven运行代码的好处。我们已经在本地检查了整个代码库，所以我们可以利用pom配置和资源文件。我们可以设置环境变量，生成内存数据库，甚至下载正确的服务器版本，用一个命令部署我们的应用程序。

        即使在多模块代码库中，每个模块需要不同的变量和服务器版本，我们也能通过Maven配置文件轻松运行正确的环境。

    2. 生产

        我们越是走向生产，话题就越是转移到稳定性和安全性上。这就是为什么我们不能把用于开发机的过程应用于有活客户的服务器。

        在这个阶段，通过Maven运行代码是不好的做法，原因有很多：

        - 首先，我们需要安装Maven。
        - 然后，只是因为我们需要编译代码，我们需要完整的Java开发工具包（JDK）。
        - 接下来，我们必须把代码库复制到我们的服务器上，把我们所有的专有代码留在纯文本中。
        - mvn命令必须执行生命周期的所有阶段（查找源代码、编译和运行）。
        - 由于上一点，我们也会浪费CPU，在云服务器的情况下，还会浪费金钱。
        - Maven催生了多个Java进程，每个进程都在使用内存（默认情况下，它们各自使用的内存量与父进程相同）。
        - 最后，如果我们有多个服务器需要部署，上述所有情况都会在每个服务器上重复。

        这些只是一些原因，为什么将应用程序作为一个包来运输对生产来说更实用。

6. 总结

    在本文中，我们探讨了通过Maven和通过java -jar命令运行代码的区别。我们还快速浏览了一些实际案例场景。
