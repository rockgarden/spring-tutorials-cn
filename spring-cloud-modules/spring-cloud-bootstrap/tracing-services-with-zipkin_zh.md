# [Spring Cloud - 使用 Zipkin 跟踪服务](https://www.baeldung.com/tracing-services-with-zipkin)

1. 概述

    在本文中，我们将把 Zipkin 添加到 [Spring云项目](https://www.baeldung.com/spring-cloud-securing-services) 中。Zipkin 是一个开源项目，它提供了发送、接收、存储和可视化跟踪的机制。这使我们能够关联服务器之间的活动，并更清晰地了解我们的服务中到底发生了什么。

    本文并非分布式跟踪或 Spring 云的入门文章。如果您想了解有关分布式跟踪的更多信息，请阅读我们的 [Spring sleuth](https://www.baeldung.com/spring-cloud-sleuth-single-application) 简介。

    > 注：Zipkin 项目已弃用自定义服务器。现在已无法运行与 Spring Cloud 或 Spring Boot 兼容的自定义 Zipkin 服务器。运行 Zipkin 服务器的最佳方法是在 docker 容器中运行。

    在第 2 节中，我们将介绍如何设置 Zipkin 以构建自定义服务器（已废弃）

    在第 3 节中，我们将介绍首选的默认服务器构建、设置、服务配置和运行流程。

2. 自定义服务器构建的 Zipkin 服务

    我们的 Zipkin 服务将作为所有跨度的存储空间。每个跨度都会被发送到该服务，并被收集成痕迹，以便将来识别。

    1. 设置

        我们创建一个新的 Spring Boot 项目，并将这些依赖项添加到 pom.xml 中：

        ```xml
        <dependency>
            <groupId>io.zipkin.java</groupId>
            <artifactId>zipkin-server</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.java</groupId>
            <artifactId>zipkin-autoconfigure-ui</artifactId>
            <scope>runtime</scope>
        </dependency>
        ```

        参考：我们可以在 Maven Central 上找到最新版本（zipkin-server、zipkin-autoconfigure-ui）。依赖项的版本继承自 spring-boot-starter-parent。

    2. 启用 Zipkin 服务器

        要启用 Zipkin 服务器，我们必须在主应用程序类中添加一些注解：

        ```java
        @SpringBootApplication
        @EnableZipkinServer
        public class ZipkinApplication {...}
        ```

        新注解 @EnableZipkinServer 将设置该服务器监听传入的跨度，并作为我们的用户界面进行查询。

    3. 配置

        首先，让我们在 src/main/resources 中创建一个名为 bootstrap.properties 的文件。请记住，从配置服务器获取配置时需要该文件。

        让我们在其中添加这些属性：

        ```properties
        spring.cloud.config.name=zipkin
        spring.cloud.config.discovery.service-id=config
        spring.cloud.config.discovery.enabled=true
        spring.cloud.config.username=configUser
        spring.cloud.config.password=configPassword

        eureka.client.serviceUrl.defaultZone=
        http://discUser:discPassword@localhost:8082/eureka/
        ```

        现在，让我们在配置仓库中添加一个配置文件，Windows 下的配置仓库位于 c:\Users\{username}/，*nix 下的配置仓库位于 /home/{username}/。

        在该目录下添加一个名为 zipkin.properties 的文件，并添加以下内容：

        ```properties
        spring.application.name=zipkin
        server.port=9411
        eureka.client.region=default
        eureka.client.registryFetchIntervalSeconds=5
        logging.level.org.springframework.web=debug
        ```

        记住提交此目录中的更改，以便配置服务检测到更改并加载文件。

    4. 运行

        现在让我们运行应用程序并导航至 <http://localhost:9411> 。我们将看到 Zipkin 的主页。

        好极了！现在，我们可以为要跟踪的服务添加一些依赖项和配置了。

    5. 服务配置

        资源服务器的设置基本相同。在接下来的章节中，我们将详细介绍如何设置[图书服务](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-bootstrap/zipkin-log-svc-book)。随后，我们将解释将这些更新应用到[评级服务](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-bootstrap/zipkin-log-svc-rating)和[网关服务](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-bootstrap/gateway)所需的修改。

        1. 设置

           要开始向 Zipkin 服务器发送 spans，我们将在 pom.xml 文件中添加以下依赖项：

           ```xml
           <dependency> 
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-zipkin</artifactId> 
           </dependency>
           ```

           参考：我们可以在 Maven Central 上找到最新版本（spring-cloud-starter-zipkin）。

        2. Spring 配置

           我们需要添加一些配置，以便 book-service 使用 Eureka 找到我们的 Zipkin 服务。我们打开 BookServiceApplication.java，并在文件中添加以下代码：

           ```java
           @Autowired
           private EurekaClient eurekaClient;
           
           @Autowired
           private SpanMetricReporter spanMetricReporter;
           
           @Autowired
           private ZipkinProperties zipkinProperties;
           
           @Value("${spring.sleuth.web.skipPattern}")
           private String skipPattern;

           // ... the main method goes here

           @Bean
           public ZipkinSpanReporter makeZipkinSpanReporter() {
               return new ZipkinSpanReporter() {
                   private HttpZipkinSpanReporter delegate;
                   private String baseUrl;

                   @Override
                   public void report(Span span) {
                       InstanceInfo instance = eurekaClient.getNextServerFromEureka("zipkin", false);
                       if (baseUrl == null || !instance.getHomePageUrl().equals(baseUrl)) {
                           baseUrl = instance.getHomePageUrl();
                       }
                       delegate = new HttpZipkinSpanReporter(new RestTemplate(), 
                       baseUrl, zipkinProperties.getFlushInterval(), spanMetricReporter);
                       if (!span.name.matches(skipPattern)) delegate.report(span);
                   }
               };
           }
           ```

           上述配置注册了一个自定义 ZipkinSpanReporter，该 ZipkinSpanReporter 从 Eureka 获取 URL。这段代码还会跟踪现有的 URL，只有在 URL 发生变化时才更新 HttpZipkinSpanReporter。这样，无论我们把 Zipkin 服务器部署到哪里，都能在不重启服务的情况下找到它。

           我们还导入了 Spring Boot 加载的默认 Zipkin 属性，并用它们来管理我们的自定义报告器。

        3. 配置

           现在，让我们在配置库中的 book-service.properties 文件中添加一些配置：

           ```properties
           spring.sleuth.sampler.percentage=1.0
           spring.sleuth.web.skipPattern=(^cleanup.*)
           ```

           Zipkin 的工作原理是对服务器上的操作进行采样。通过将 spring.sleuth.sampler.percentage 设置为 1.0，我们将采样率设置为 100%。跳过模式是一个简单的 regex，用于排除名称匹配的 spans。

           跳过模式将阻止报告所有以 “cleanup” 开头的跨度。这是为了阻止来自 Spring 会话代码库的扫描。

           请对评级服务重复上述操作。

           最后，对于网关服务，请在 gateway.properties 文件中复制并粘贴相同的属性。这将配置网关服务不发送有关 favicon 或 spring 会话的 spans。

3. 使用默认服务器构建进行配置

    如前所述，这是运行 Zipkin 的建议方式。

    1. 设置 Zipkin 模块

        在你的 IDE 项目中，创建一个 “Zipkin” 文件夹，并添加内容如下的 docker-compose.yml 文件：

        ```yml
        version: "3.9"
        services:
        zipkin:
        image: openzipkin/zipkin
        ports:
        - 9411:9411
        ```

        或者，点击此处查看官方[文档](https://github.com/openzipkin/zipkin#quick-start)

    2. 服务配置

        现在，让我们在配置库中的 book-service.properties 文件中添加一些配置：

        ```properties
        spring.sleuth.sampler.percentage=1.0
        spring.sleuth.web.skipPattern=(^cleanup.*)
        ```

        Zipkin 的工作原理是对服务器上的操作进行采样。通过将 spring.sleuth.sampler.percentage 设置为 1.0，我们将采样率设置为 100%。跳过模式是一个简单的 regex，用于排除名称匹配的 spans。

        跳过模式将阻止报告所有以 “cleanup” 开头的跨度。这是为了阻止来自 Spring 会话代码库的跨度。

    3. 评级服务

        让我们按照上述 book-service 部分的相同步骤，对 rating-service 的相应文件进行修改。

    4. 网关服务

        同样，让我们按照 book-service 的相同步骤操作。但在向 gateway.properties 添加配置时，请添加以下内容：

        ```properties
        spring.sleuth.sampler.percentage=1.0
        spring.sleuth.web.skipPattern=(^cleanup.*|.+favicon.*)
        ```

        这将配置网关服务不发送有关 favicon 或 Spring 会话的跨距。

    5. 运行

        首先，我们使用 `docker compose up -d` 启动 redis 服务器、[config](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-bootstrap/config)、[discovery](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-bootstrap/discovery)、gateway、book、rating 和 zipkin docker 镜像。然后，我们导航到 <http://localhost:8080/book-service/books> 地址。然后，我们打开一个新标签页，导航到 <http://localhost:9411> 。在这个页面中，我们选择 book-service，然后按下 `Find Traces` 按钮。我们应该会看到搜索结果中出现一个痕迹。最后，我们可以点击打开该跟踪。

        在跟踪页面上，我们可以看到按服务细分的请求。前两个跨度由网关创建，最后一个跨度由图书服务创建。这显示了该请求在图书服务上的处理时间（18.379 毫秒）和在网关上的处理时间（87.961 毫秒）。

4. 结论

    我们已经看到将 Zipkin 集成到云应用程序中是多么容易。

    这让我们对通信如何在应用程序中传输有了一些必要的了解。随着应用程序复杂性的增加，Zipkin 可以为我们提供急需的信息，让我们了解请求在哪里花费时间。这可以帮助我们确定哪些地方的速度变慢了，并指出我们的应用程序哪些地方需要改进。
