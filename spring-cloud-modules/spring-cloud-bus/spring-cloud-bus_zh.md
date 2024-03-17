# Spring云总线

[Jakarta EE](https://www.baeldung.com/category/java-ee) [Spring Cloud](../spring-cloud_zh.md)

Jakarta EE 可以用来开发微服务吗？答案是肯定的！

> 为 Jakarta EE 和 Java EE 开发人员揭开微服务的神秘面纱

1. 概述
    本文将介绍新的 Spring Cloud Bus 项目。Spring Cloud Bus 使用轻量级消息代理来链接分布式系统节点。其主要用途是广播配置更改或其他管理信息。我们可以将其视为分布式[执行器](https://www.baeldung.com/spring-boot-actuators)。

    该项目使用 AMQP 代理作为传输方式，但也可以使用 Apache Kafka 或 Redis 代替 RabbitMQ。目前还不支持其他传输方式。

    在本教程中，我们将使用 RabbitMQ 作为我们的主要传输方式 - 我们自然已经运行了它。

2. 先决条件

    在开始之前，建议已完成 [Spring 云配置快速入门](../spring-cloud-config/spring-cloud-configuration_zh.md)。我们将使用现有的云配置服务器和客户端来扩展它们，并添加有关配置更改的自动通知。

    1. RabbitMQ

        让我们从 RabbitMQ 开始，我们建议将 RabbitMQ 作为 docker [镜像](https://hub.docker.com/_/rabbitmq/)运行。设置起来非常简单--要在本地运行 RabbitMQ，我们需要安装 Docker，并在 Docker 安装成功后运行以下命令：

        `docker pull rabbitmq:3-management`

        此命令将 RabbitMQ docker 映像拉到一起，其中默认安装并启用了管理插件。

        接下来，我们可以运行 RabbitMQ：

        `docker run -d --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management`

        执行命令后，我们就可以访问网络浏览器并打开 <http://localhost:15672>，这将显示管理控制台登录表单。默认用户名是："guest"；密码是："guest"。RabbitMQ 也将监听端口 5672。

3. 将执行器添加到云配置客户端

    我们应该同时运行云配置服务器和云配置客户端。要刷新配置更改，每次都需要重启客户端，这并不理想。

    让我们停止配置客户端，并用 @RefreshScope 对 ConfigClient 控制器类进行注解：

    spring-cloud-bus-client/SpringCloudConfigClientApplication.java

    最后，更新 pom.xml 文件并添加 Actuator：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-actuator</artifactId>
        <version>2.2.6.RELEASE</version>
    </dependency>
    ```

    默认情况下，执行器添加的所有敏感端点都是安全的。这包括"/refresh"端点。为简单起见，我们将通过更新 application.yml 关闭安全机制：

    ```yml
    management：
    security：
        enabled: false
    ```

    此外，从 Spring Boot 2 开始，执行器端点默认[不公开](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-endpoints)。要使它们可供访问，我们需要在 application.yml 中添加以下内容：

    ```yml
    management：
    endpoints：
        web：
        exposure：
            include： "*"
    ```

    让我们先启动客户端，在 GitHub 的属性文件中将用户角色从现有的"Developer"更新为"Programmer"。配置服务器会直接显示更新后的值，但客户端不会。为了让客户端看到新文件，我们只需向"/refresh"端点发送一个空的 POST 请求，这个端点是由执行器添加的：

    `$> curl -X POST http://localhost:8080/actuator/refresh`

    我们将得到显示更新属性的 JSON 文件：

    ```json
    [
    "user.role"
    ]
    ```

    最后，我们可以检查用户角色是否已更新：

    ```bash
    $> curl http://localhost:8080/whoami/Mr_Pink
    Hello Mr_Pink! You are a(n) Programmer and your password is 'd3v3L'.
    ```

    通过调用"/refresh"端点，用户角色已成功更新。客户端无需重启即可更新配置。

4. Spring 云总线

    通过使用 Actuator，我们可以刷新客户端。但是，在云环境中，我们需要访问每个客户端，并通过访问执行器端点重新加载配置。

    为了解决这个问题，我们可以使用 Spring 云总线。

    1. 客户端

        我们需要更新云配置客户端，使其能够订阅 RabbitMQ 交换：

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
            <version>2.2.1.RELEASE</version>
        </dependency>
        ```

        要完成配置客户端更改，我们需要在 application.yml 文件中添加 RabbitMQ 详情并启用云总线：

        ```yml
        ---
        spring:
        rabbitmq:
            host: localhost
            port: 5672
            username: guest
            password: guest
        cloud:
          bus:
              enabled: true
            refresh:
                enabled: true
        ```

        请注意，我们使用的是默认用户名和密码。在实际生产应用中需要更新。本教程使用默认用户名和密码即可。

        现在，客户端将有另一个端点"/bus-refresh"。调用该端点将导致

        - 从配置服务器获取最新配置，并更新由 @RefreshScope 注解的配置
        - 向 AMQP 交换中心发送消息，通知刷新事件
        - 所有订阅的节点也将更新它们的配置

        这样，我们就不需要去各个节点触发配置更新了。

    2. 服务器

        最后，让我们为配置服务器添加两个依赖项，以实现配置更改的完全自动化。

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-monitor</artifactId>
            <version>2.2.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
            <version>3.0.4.RELEASE</version>
        </dependency>
        ```

        我们将使用 spring-cloud-config-monitor 监控配置更改，并使用 RabbitMQ 作为传输广播事件。

        我们只需更新 application.properties，并提供 RabbitMQ 的详细信息：

        ```yml
        spring.rabbitmq.host=localhost
        spring.rabbitmq.port=5672
        spring.rabbitmq.username=guest
        spring.rabbitmq.password=guest
        ```

        4.3. GitHub Webhook
        一切就绪。一旦服务器收到配置更改通知，它将以消息的形式广播到 RabbitMQ。客户端将监听消息，并在发送配置更改事件时更新其配置。但是，服务器将如何得知修改信息呢？

        我们需要配置一个 GitHub Webhook。让我们进入 GitHub，打开存放配置属性的仓库。现在，让我们选择 "设置" 和 "Webhook"。点击添加 Webhook 按钮。

        Payload URL 是配置服务器"/monitor"端点的 URL。在我们的例子中，URL 将如下所示：
        <http://root:s3cr3t@REMOTE_IP:8888/monitor>

        我们只需将下拉菜单中的内容类型更改为"application/json"。接下来，请将 Secret 留空，然后点击添加 webhook 按钮--之后，一切就准备就绪了。

5. 测试

    让我们确保所有应用程序都在运行。如果我们返回并检查客户端，将显示 user.role 为 "Programmer"，user.password 为 "d3v3L"：

    ```bash
    $> curl http://localhost:8080/whoami/Mr_Pink
    Hello Mr_Pink! You are a(n) Programmer and your password is 'd3v3L'.
    ```

    以前，我们必须使用"/refresh"端点重新加载配置更改。让我们打开属性文件，将 user.role 改回 Developer，然后推送更改：

    `user.role=Programmer`

    现在检查客户端，我们会看到

    ```bash
    $> curl http://localhost:8080/whoami/Mr_Pink
    Hello Mr_Pink! You are a(n) Developer and your password is 'd3v3L'.
    ```

    配置客户端在没有重启和显式刷新的情况下几乎同时更新了配置。我们可以回到 GitHub，打开最近创建的 Webhook。在最下方，有最近的交付。我们可以选择列表顶部的一个（假设这是第一次更改，反正只有一个），然后检查发送到配置服务器的 JSON。

    我们还可以检查配置和服务器日志，并会看到以下条目：

    `o.s.cloud.bus.event.RefreshListener: Received remote refresh request. Keys refreshed []`

6. 结论

    在本文中，我们采用了现有的 Spring 云配置服务器和客户端，并添加了执行器端点来刷新客户端配置。接下来，我们使用 Spring Cloud Bus 广播配置更改并自动更新客户端。我们还配置了 GitHub Webhook 并测试了整个设置。

    讨论过程中使用的代码可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-bus) 上找到。
