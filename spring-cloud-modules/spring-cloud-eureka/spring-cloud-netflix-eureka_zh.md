# [Spring Cloud Netflix 简介 - Eureka](https://www.baeldung.com/spring-cloud-netflix-eureka)

1. 概述

    在本教程中，我们将通过 “Spring Cloud Netflix Eureka” 介绍客户端服务发现。

    客户端服务发现允许服务相互查找和通信，而无需硬编码主机名和端口。这种架构中唯一的 `fixed point` 是服务注册表，每个服务都必须在注册表中注册。

    这样做的一个缺点是，所有客户端都必须执行一定的逻辑才能与这个固定点进行交互。这就需要在实际请求之前进行一次额外的网络往返。

    有了 Netflix Eureka，每个客户端都可以同时充当服务器，将自己的状态复制给连接的对等设备。换句话说，客户端在服务注册表中检索所有已连接对等服务的列表，并通过负载平衡算法向其他服务发出所有进一步请求。

    要获知客户端的存在，它们必须向注册表发送心跳信号。

    为了实现本教程的目标，我们将实现三个微服务：

    - 服务注册中心（Eureka 服务器）
    - 在注册中心注册的 REST 服务（Eureka 客户端）
    - 一个网络应用程序，作为注册表感知客户端（Spring Cloud Netflix Feign Client）消费 REST 服务。

2. Eureka服务器

    为服务注册表实施 Eureka 服务器非常简单，只需

    - 在依赖项中添加 Spring-cloud-starter-netflix-eureka-server
    - 在 [@SpringBootApplication](https://www.baeldung.com/spring-boot-application-configuration) 中通过注释 @EnableEurekaServer 启用 Eureka 服务器
    - 配置一些属性

    让我们一步一步来。

    首先，我们将创建一个新的Maven项目，并将依赖项放入其中。注意，我们要将 spring-cloud-starter-parent 导入本教程中描述的所有项目：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-parent</artifactId>
                <version>2023.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

    我们可以在 Spring 的项目[文档](https://spring.io/projects/spring-cloud#learn)中查看最新的 Spring Cloud 版本。

    然后，我们将创建主应用程序类：

    spring-cloud-eureka-server/main/.spring.cloud.eureka.server/EurekaServerApplication.java

    最后，我们将以 YAML 格式配置属性，因此 application.yml 将成为我们的配置文件：

    ```yml
    server:
    port: 8761
    eureka:
    client:
        registerWithEureka: false
        fetchRegistry: false
    ```

    这里我们要配置应用端口；Eureka 服务器的默认端口是 8761。我们要告诉内置的 Eureka 客户端不要注册自己，因为我们的应用程序应该充当服务器。

    现在，我们将浏览器指向 <http://localhost:8761> 以查看 Eureka 面板，稍后我们将在这里检查已注册的实例。

    目前，我们可以看到一些基本指标，如状态和健康指标。

3. Eureka客户端

    要让 @SpringBootApplication 具备发现感知能力，我们必须在类路径中加入 Spring Discovery Client（例如 spring-cloud-starter-netflix-eureka-client）。

    然后，我们需要在 @Configuration 中注解 @EnableDiscoveryClient 或 @EnableEurekaClient。请注意，如果classpath 上有 Spring-cloud-starter-netflix-eureka-client 依赖关系，则此注解为可选注解。

    后者告诉 Spring Boot 明确使用 Spring Netflix Eureka 进行服务发现。为了给我们的客户端应用程序添加一些示例生活，我们还将在 pom.xml 中包含 spring-boot-starter-web 包，并实现一个 REST 控制器。

    但首先，我们要添加依赖项。同样，我们可以让 spring-cloud-starter-parent 依赖项为我们计算工件版本：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ```

    在这里，我们将实现主应用程序类：

    spring-cloud-eureka-client/main/.spring.cloud.eureka.client/EurekaClientApplication.java

    以及 GreetingController 接口：

    spring-cloud-eureka-client/main/.spring.cloud.eureka.client/GreetingController.java

    我们也可以简单地在 EurekaClientApplication 类中声明映射，而不是使用接口。不过，如果我们想在服务器和客户端之间共享该接口，它还是很有用的。

    接下来，我们必须在 application.yml 中设置一个已配置的 Spring 应用程序名称，以便在已注册应用程序列表中唯一标识我们的客户端。

    我们可以让 Spring Boot 为我们随机选择一个端口，因为稍后我们将使用其名称访问该服务。

    最后，我们必须告诉客户端注册表的位置：

    ```yml
    spring:
    application:
        name: spring-cloud-eureka-client
        server:
        port: 0
        eureka:
        client:
        serviceUrl:
        defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
    instance:
        preferIpAddress: true
    ```

    我们决定这样设置我们的 Eureka 客户端，因为这样的服务以后很容易扩展。

    现在我们运行客户端，再次将浏览器指向 <http://localhost:8761> ，在 Eureka 控制面板上查看其注册状态。通过使用控制面板，我们可以进行进一步的配置，比如出于管理目的将已注册客户端的主页与控制面板链接起来。

4. Feign客户端

    为了用三个依赖的微服务最终完成我们的项目，现在我们将使用 Spring Netflix Feign Client 实现一个 REST 消费型 Web 应用程序。

    将 Feign 视为使用接口与端点通信的发现感知 [Spring RestTemplate](https://www.baeldung.com/rest-template)。这些接口将在运行时自动实现，它使用的是服务名而不是服务URL。

    如果没有 Feign，我们就必须在控制器中自动连接一个 EurekaClient 实例，这样我们就可以通过服务名以 Application 对象的形式接收服务信息。

    我们将使用该应用程序获取该服务所有实例的列表，选择一个合适的实例，然后使用 InstanceInfo 获取主机名和端口。这样，我们就可以使用任何 http 客户端执行标准请求了：

    ```java
    @Autowired
    private EurekaClient eurekaClient;

    @RequestMapping("/get-greeting-no-feign")
    public String greeting(Model model) {

        InstanceInfo service = eurekaClient
        .getApplication(spring-cloud-eureka-client)
        .getInstances()
        .get(0);

        String hostName = service.getHostName();
        int port = service.getPort();

        // ...
    }
    ```

    还可以使用 RestTemplate 通过名称访问 Eureka 客户端服务，但这不在本文讨论范围之内。

    为了建立我们的 Feign Client 项目，我们将在其 pom.xml 中添加以下四个依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-feign</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    ```

    Feign Client 位于 spring-cloud-starter-feign 软件包中。要启用它，我们必须在 @Configuration 中注释 @EnableFeignClients。要使用它，我们只需用 `@FeignClient(“service-name”)`  注解一个接口，并将其自动连接到控制器中即可。

    创建此类 Feign Client 的一个好方法是创建带有 @RequestMapping 注解方法的接口，并将它们放入一个单独的模块中。这样，它们就可以在服务器和客户端之间共享。在服务器端，我们可以将它们作为 @Controller 来实现，而在客户端，它们可以被扩展并注释为 @FeignClient。

    此外，还需要在项目中包含 spring-cloud-starter-eureka 软件包，并用 @EnableEurekaClient 对主应用程序类进行注解，以启用该软件包。

    spring-boot-starter-web 和 spring-boot-starter-thymeleaf 依赖项用于显示包含从 REST 服务获取的数据的视图。

    这就是我们的 Feign 客户端接口：[GreetingClient.java](./spring-cloud-eureka-feign-client/src/main/java/com/baeldung/spring/cloud/feign/client/GreetingClient.java)

    在这里，我们将实现主应用程序类，该类同时还充当控制器：[FeignClientApplication.java](./spring-cloud-eureka-feign-client/src/main/java/com/baeldung/spring/cloud/feign/client/FeignClientApplication.java)

    这将是我们视图的 HTML 模板：

    ```html
    <!DOCTYPE html>
    <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <title>Greeting Page</title>
        </head>
        <body>
            <h2 th:text="${greeting}"/>
        </body>
    </html>
    ```

    application.yml 配置文件与上一步几乎相同：

    ```yml
    spring:
    application:
        name: spring-cloud-eureka-feign-client
    server:
    port: 8080
    eureka:
    client:
        serviceUrl:
        defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
    ```

    现在我们可以构建并运行该服务了。最后，我们将浏览器指向 <http://localhost:8080/get-greeting> ，它应该会显示类似下面的内容：

    `Hello from SPRING-CLOUD-EUREKA-CLIENT!`

5. TransportException：Cannot Execute Request on Any Known Server

    在运行 Eureka 服务器时，我们经常会遇到以下异常情况：

    `com.netflix.discovery.shared.transport.TransportException: Cannot execute request on any known server`

    基本上，出现这种情况是因为在 application.properties 或 application.yml 中进行了错误的配置。Eureka 为客户端提供了两个可配置的属性：

    - registerWithEureka：如果我们将此属性设置为 “true”，那么当服务器启动时，内置客户端将尝试向 Eureka 服务器注册。
    - fetchRegistry： 如果我们将此属性设置为 “true”，内置客户端将尝试获取 Eureka 注册表。

    现在，当我们启动 Eureka 服务器时，我们不想让内置客户端向服务器进行注册配置。

    如果我们将上述属性标记为 “true”（或干脆不配置，因为它们默认为 “true”），那么在启动服务器时，内置客户端就会尝试向 Eureka 服务器注册，并尝试获取注册表，而注册表尚未可用。结果，我们就会收到传输异常（TransportException）。

    因此，我们绝不应在Eureka服务器应用程序中将这些属性配置为 “true”。应用程序.yml 中的正确设置应为

    ```yaml
    eureka:
    client:
        registerWithEureka: false
        fetchRegistry: false
    ```

6. 结论

    在本文中，我们学习了如何使用 Spring Netflix Eureka 服务器实现服务注册表，并在其中注册一些 Eureka 客户端。

    由于步骤 3 中的 Eureka 客户端监听的是随机选择的端口，因此如果没有注册表中的信息，它就不知道自己的位置。有了 Feign 客户端和注册表，即使位置发生变化，我们也能找到并使用 REST 服务。

    最后，我们看到了在微服务架构中使用服务发现的全貌。
