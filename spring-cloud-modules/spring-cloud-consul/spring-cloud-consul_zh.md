# [Spring Cloud Consul 快速指南](https://www.baeldung.com/spring-cloud-consul)

1. 概述

    [Spring Cloud Consul](https://cloud.spring.io/spring-cloud-consul/) 项目可为 Spring Boot 应用程序提供与 [Consul](https://www.consul.io/intro/) 的轻松集成。

    Consul 是一种提供组件的工具，用于解决微服务架构中一些最常见的难题：

    - 服务发现 - 自动注册和取消注册服务实例的网络位置
    - 健康检查 - 检测服务实例是否正常运行
    - 分布式配置 - 确保所有服务实例使用相同的配置

    在本文中，我们将了解如何配置 Spring Boot 应用程序以使用这些功能。

2. 前提条件

    首先，建议快速了解一下 Consul 及其所有功能。

    在本文中，我们将使用运行在 localhost:8500 上的 Consul 代理。有关如何安装 Consul 和运行代理的更多详情，请参阅此[链接](https://developer.hashicorp.com/consul/docs/install)。

    首先，我们需要在 pom.xml 中添加 spring-cloud-starter-consul-all 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-all</artifactId>
        <version>3.1.1</version>
    </dependency>
    ```

3. 服务发现

    让我们编写第一个 Spring Boot 应用程序，并与运行中的 Consul 代理连接：

    ```java
    @SpringBootApplication
    public class ServiceDiscoveryApplication {
        public static void main(String[] args) {
            new SpringApplicationBuilder(ServiceDiscoveryApplication.class)
            .web(true).run(args);
        }
    }
    ```

    默认情况下，Spring Boot 会尝试连接到 localhost:8500 上的 Consul 代理。要使用其他设置，我们需要更新 application.yml 文件：

    ```yaml
    spring:
    cloud:
        consul:
        host: localhost
        port: 8500
    ```

    然后，如果我们在浏览器中访问 Consul 代理的网站 <http://localhost:8500>，就会看到我们的应用程序已在 Consul 中正确注册，标识符为 "`${spring.application.name}:${profiles separated by comma}:${server.port}`"。

    要自定义此标识符，我们需要用另一个表达式更新 spring.cloud.discovery.instanceId 属性：

    ```yml
    spring:
    application:
        name: myApp
    cloud:
        consul:
        discovery:
            instanceId: ${spring.application.name}:${random.value}
    ```

    如果我们再次运行应用程序，就会发现它是使用标识符 "MyApp" 加上一个随机值注册的。我们需要在本地计算机上运行多个应用程序实例。

    最后，要禁用服务发现，我们需要将 spring.cloud.consul.discovery.enabled 属性设置为 false。

    1. 查找服务

        我们已经在 Consul 中注册了应用程序，但客户端如何才能找到服务端点呢？我们需要一个发现客户端服务，以便从 Consul 获取正在运行的可用服务。

        Spring 为此提供了一个 DiscoveryClient API，我们可以使用 @EnableDiscoveryClient 注解启用它：

        main/.spring.cloud.consul.discovery/DiscoveryClientApplication.java

        然后，我们可以将 DiscoveryClient Bean 注入控制器并访问实例：

        main/.spring.cloud.consul.discovery/DiscoveryClientController.java

        最后，我们将定义应用程序端点：

        main/.spring.cloud.consul.discovery/DiscoveryClientController.java:discoveryPing();ping()

        "myApp/ping" 路径是带有服务端点的 Spring 应用程序名称。Consul 将提供所有名为 "myApp" 的可用应用程序。

4. 健康检查

    Consul 会定期检查服务端点的健康状况。

    默认情况下，Spring 实现的健康端点会在应用程序正常运行时返回 200 OK。如果我们想自定义端点，就必须更新 application.yml：

    ```yml
    spring:
    cloud:
        consul:
        discovery:
            healthCheckPath: /my-health-check
            healthCheckInterval: 20s
    ```

    这样，Consul 将每隔 20 秒轮询一次 "/my-health-check" 端点。

    让我们定义自定义健康检查服务，返回 FORBIDDEN 状态：

    main/.spring.cloud.consul.discovery/DiscoveryClientController.java:myCustomCheck()

    如果我们访问 Consul 代理网站，就会发现我们的应用程序失败了。要解决这个问题，"/my-health-check" 服务应返回 HTTP 200 OK 状态代码。

5. 分布式配置

    该功能允许在所有服务之间同步配置。Consul 会关注任何配置变更，然后触发所有服务的更新。

    首先，我们需要在 pom.xml 中添加 spring-cloud-starter-consul-config 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-config</artifactId>
        <version>3.1.1</version>
    </dependency>
    ```

    我们还需要将 Consul 和 Spring 应用程序名称的设置从 application.yml 文件移到 Spring 首先加载的 bootstrap.yml 文件中。

    然后，我们需要启用 Spring Cloud Consul Config：

    ```yml
    spring:
    application:
        name: myApp
    cloud:
        consul:
        host: localhost
        port: 8500
        config:
            enabled: true
    ```

    Spring Cloud Consul Config 将在 Consul 中的 "/config/myApp" 处查找属性。因此，如果我们有一个名为 "my.prop" 的属性，就需要在 Consul 代理站点中创建该属性。

    我们可以进入 "KEY/VALUE" 部分创建属性，然后在 "Create Key" 表单中输入 "/config/myApp/my/prop"，并将 "Hello World"作为值。最后点击 "Create" 按钮。

    请注意，如果我们使用的是 Spring 配置文件，则需要在 Spring 应用程序名称旁边添加配置文件。例如，如果我们使用的是 dev 配置文件，Consul 中的最终路径将是"/config/myApp,dev"。

    现在，让我们看看注入属性的控制器是什么样的：

    main/.spring.cloud.consul.properties/DistributedPropertiesController.java

    还有 MyProperties 类：

    main/.spring.cloud.consul.properties/MyProperties.java

    如果我们运行应用程序，字段值和属性都与 Consul 中的 "Hello World" 值相同。

    1. 更新配置

        在不重启 Spring Boot 应用程序的情况下更新配置如何？

        如果我们回到 Consul 代理站点，用另一个值（如 "New Hello World"）更新属性"/config/myApp/my/prop"，那么字段值将不会改变，字段属性也将如预期更新为 "New Hello World"。

        这是因为 MyProperties 类中的字段属性具有 @RefreshScope 注解。所有带有 @RefreshScope 注解的 Bean 都会在配置更改后被刷新。

        在现实生活中，我们不应该直接在 Consul 中保存属性，而是应该将它们持久地存储在某个地方。我们可以使用配置服务器（[Config Server](https://www.baeldung.com/spring-cloud-configuration)）来做到这一点。

6. 总结

    在本文中，我们已经了解了如何设置 Spring Boot 应用程序，使其与 Consul 配合使用以实现服务发现、自定义健康检查规则和共享分布式配置。

    我们还介绍了客户端调用这些已注册服务的多种方法。
