# [比较Spring Boot中的嵌入式Servlet容器](https://www.baeldung.com/spring-boot-servlet-containers)

1. 简介

    云原生应用和微服务的不断普及，使得对嵌入式Servlet容器的需求不断增加。Spring Boot允许开发人员使用现有的3个最成熟的容器来轻松构建应用程序或服务： Tomcat、Undertow和Jetty。

    在本教程中，我们将展示一种方法，使用启动时和一些负载下获得的指标来快速比较容器的实现。

2. 依赖性

    我们对每个可用的容器实现的设置总是要求我们在 pom.xml 中声明对 spring-boot-starter-web 的依赖。

    一般来说，我们要把我们的父类指定为spring-boot-starter-parent，然后包括我们想要的启动器：

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.4.0</version>
        <relativePath/>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
    ```

    1. Tomcat

        使用Tomcat时不需要更多的依赖，因为在使用spring-boot-starter-web时默认包含它。

    2. Jetty

        为了使用Jetty，我们首先需要将spring-boot-starter-tomcat排除在spring-boot-starter-web之外。

        然后，我们简单地声明一个对 spring-boot-starter-jetty 的依赖关系：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        ```

    3. Undertow

        Undertow的设置与Jetty相同，只是我们使用spring-boot-starter-undertow作为我们的依赖：

        ```xml
        ......
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-undertow</artifactId>
        </dependency>
        ```

    4. 执行器

        我们将使用Spring Boot的Actuator作为一种方便的方式来强调系统和查询指标。

        请看这篇[文章](https://www.baeldung.com/spring-boot-actuators)，了解关于Actuator的细节。我们只需在pom中添加一个依赖项，使其可用：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        ```

    5. Apache Bench

        [Apache Bench](http://httpd.apache.org/docs/2.2/programs/ab.html)是一个开源的负载测试工具，与Apache网络服务器捆绑在一起。

        Windows用户可以从这里[链接](https://httpd.apache.org/docs/current/platform/windows.html#down)的第三方供应商之一下载Apache。如果Apache已经安装在你的Windows机器上，你应该可以在apache/bin目录下找到ab.exe。

        如果你是在Linux机器上，可以用apt-get来安装ab：

        `$ apt-get install apache2-utils`

        > macOS 默认已经安装，查看版本`apachectl -v`。

3. 启动指标

    1. 采集

        为了收集我们的启动指标，我们将注册一个事件处理程序，在Spring Boot的ApplicationReadyEvent上启动。

        我们将通过直接与Actuator组件使用的MeterRegistry合作，以编程方式提取我们感兴趣的指标：compare/StartupEventHandler.java 

        通过在我们的事件处理程序中记录启动时的有趣指标，我们避免了手动查询Actuator REST端点或运行独立的JMX控制台的需要。

        3.2. 选择

        Actuator有大量的指标是开箱即用的。我们选择了3个指标，这些指标有助于在服务器启动后对关键的运行时特性有一个高层次的了解：

        - jvm.memory.used - 自启动以来JVM使用的总内存
        - jvm.classes.load - 已加载的类的总数量
        - jvm.threads.live - 活动线程的总数。在我们的测试中，这个值可以看作是 "静态(at rest)"的线程数。

4. 运行时指标

    1. 采集

        除了提供启动指标外，当我们运行Apache Bench时，我们将使用执行器暴露的/metrics端点作为目标URL，以便将应用程序置于负载之下。

        为了在负载下测试一个真正的应用程序，我们可能会使用我们的应用程序提供的端点。

        运行 ComparisonApplication.java。

        ```log
        21:07:07.298 [main] INFO  c.b.compare.StartupEventHandler - Startup Metric >> jvm.memory.used=7790216
        21:07:07.298 [main] INFO  c.b.compare.StartupEventHandler - Startup Metric >> jvm.classes.loaded=10990
        21:07:07.298 [main] INFO  c.b.compare.StartupEventHandler - Startup Metric >> jvm.threads.live=28
        ```

        一旦服务器启动，我们将得到一个命令提示符并执行ab：

        `ab -n 10000 -c 10 http://localhost:8080/actuator/metrics`

        在上面的命令中，我们指定了使用10个并发线程的总共10,000个请求。

        > MacBookPro M1pro：20000个请求将超时，上限在 16400 左右。

    2. 选择

        Apache Bench能够非常迅速地给我们提供一些有用的信息，包括连接时间和在一定时间内得到服务的请求的百分比。

        对于我们的目的，我们主要关注的是每秒钟的请求数(rps)和每请求时间(tpr)平均值。

5. 结果

    在启动时，我们发现Tomcat、Jetty和Undertow的内存占用相当，Undertow需要的内存略多于其他两个，Jetty需要的内存最少。

    在我们的基准测试中，我们发现Tomcat、Jetty和Undertow的性能相当，但Undertow显然是最快的，而Jetty只是稍微慢一点。

    | Metric                        | Tomcat | Jetty | Undertow |
    |-------------------------------|--------|-------|----------|
    | jvm.memory.used (MB)          | 168    | 155   | 164      |
    | jvm.classes.loaded            | 9869   | 9784  | 9787     |
    | jvm.threads.live              | 25     | 17    | 19       |
    | Requests per second           | 1542   | 1627  | 1650     |
    | Average time per request (ms) | 6.483  | 6.148 | 6.059    |

    请注意，这些指标自然是裸体项目的代表；你自己的应用程序的指标肯定会有所不同。

6. 基准测试讨论

    开发适当的基准测试来对服务器的实现进行彻底的比较可能会变得很复杂。为了提取最相关的信息，关键是要清楚地了解什么对有关的用例是重要的。

    值得注意的是，本例中收集的基准测量值是使用一个非常具体的工作负载，包括对一个执行器端点的HTTP GET请求。

    预计不同的工作负载可能会导致不同的容器实施的相对测量结果。如果需要更强大或更精确的测量，那么制定一个更接近生产用例的测试计划将是一个非常好的主意。

    此外，一个更复杂的基准测试解决方案，如[JMeter](https://www.baeldung.com/jmeter)或[Gatling](https://www.baeldung.com/introduction-to-gatling)，可能会产生更有价值的见解。

7. 选择一个容器

    选择正确的容器实现可能应该基于许多因素，这些因素不能仅仅用少数指标来整齐地概括。舒适度、功能、可用的配置选项和政策往往同样重要，甚至更重要。

8. 结语

    在这篇文章中，我们考察了Tomcat、Jetty和Undertow的嵌入式Servlet容器实现。我们通过查看Actuator组件所暴露的指标，检查了每个容器在默认配置下的启动时的运行特性。

    我们针对运行中的系统执行了一个假想的工作负载，然后用Apache Bench测量了性能。
