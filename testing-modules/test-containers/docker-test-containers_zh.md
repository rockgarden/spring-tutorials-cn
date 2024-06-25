# [Java 测试中的 Docker 测试容器](https://www.baeldung.com/docker-test-containers)

1. 简介

    在本教程中，我们将学习 Java [TestContainers](https://java.testcontainers.org/) 库。它允许我们在测试中使用 Docker 容器。因此，我们可以编写依赖于外部资源的自包含集成测试。

    我们可以在测试中使用任何拥有 docker 镜像的资源。例如，数据库、网络浏览器、网络服务器和消息队列都有镜像。因此，我们可以在测试中将它们作为容器运行。

2. 需求

    TestContainers 库可用于 Java 8 及更高版本。此外，它与 JUnit Rules API 兼容。

    > 本示例需要用 Java 11

    首先，让我们定义核心功能的 maven 依赖关系：

    ```xml
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.19.6</version>
    </dependency>
    ```

    还有专门的容器模块。在本教程中，我们将使用 PostgreSQL 和 Selenium。

    让我们添加相关依赖项：

    ```xml
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.6</version>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>selenium</artifactId>
        <version>1.19.6</version>
    </dependency>
    ```

    我们可以在 Maven Central 上找到最新版本。

    此外，我们还需要 Docker 来运行容器。有关安装说明，请参阅 Docker [文档](https://docs.docker.com/install/)。

    确保您能在测试环境中运行 Docker 容器。

    > 通过 BrowserWebDriverContainer 自动创建？

3. 使用方法

    让我们配置一个通用的容器规则：

    ```java
    @ClassRule
    public static GenericContainer simpleWebServer
    = new GenericContainer("alpine:3.2")
    .withExposedPorts(80)
    .withCommand("/bin/sh", "-c", "while true; do echo "
        + "\"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 80; done");
    ```

    我们通过指定一个 docker 镜像名称来构建一个 GenericContainer 测试规则。然后，我们使用构建器方法对其进行配置：

    - 我们使用 withExposedPorts 从容器中暴露一个端口
    - withCommand 定义了一个容器命令。它将在容器启动时执行。

    该规则使用 @ClassRule 进行注解。因此，它会在该类中的任何测试运行之前启动 Docker 容器。所有方法执行完毕后，容器将被销毁。

    如果使用 @Rule 注解，GenericContainer 规则将为每个测试方法启动一个新容器。当测试方法结束时，它将停止容器。

    我们可以使用 IP 地址和端口与容器中运行的进程通信：

    ```java
    @Test
    public void givenSimpleWebServerContainer_whenGetReuqest_thenReturnsResponse()
    throws Exception {
        String address = "http://" 
        + simpleWebServer.getContainerIpAddress() 
        + ":" + simpleWebServer.getMappedPort(80);
        String response = simpleGetRequest(address);
        assertEquals(response, "Hello World!");
    }
    ```

4. 使用模式

    测试容器有多种使用模式。我们看到了运行 GenericContainer 的示例。

    TestContainers 库也有具有专门功能的规则定义。它们适用于 MySQL、PostgreSQL 等常见数据库容器，以及网络客户端等其他容器。

    虽然我们可以像运行通用容器一样运行它们，但这些专用规则提供了扩展的便利方法。

    1. 数据库

        假设我们需要一个数据库服务器来进行数据访问层集成测试。我们可以借助 TestContainers 库在容器中运行数据库。

        例如，我们使用 PostgreSQLContainer 规则启动 PostgreSQL 容器。然后，我们就能使用辅助方法了。这些方法包括用于数据库连接的 getJdbcUrl、getUsername 和 getPassword：

        ![PostgreSqlContainerLiveTest.java](/src/test/java/com/baeldung/testconainers/PostgreSqlContainerLiveTest.java)

        也可以将 PostgreSQL 作为通用容器运行。但配置连接会更加困难。

    2. 网络驱动程序

        另一种有用的情况是使用网络浏览器运行容器。通过 BrowserWebDriverContainer 规则，可以在 docker-selenium 容器中运行 Chrome 和 Firefox 浏览器。然后，我们使用 RemoteWebDriver 对它们进行管理。

        这对于自动化网络应用程序的用户界面/验收测试非常有用：

        ![WebDriverContainerLiveTest.java](/src/test/java/com/baeldung/testconainers/WebDriverContainerLiveTest.java)

    3. Docker Compose

        如果测试需要更复杂的服务，我们可以在 docker-compose 文件中指定它们：

        ```yml
        simpleWebServer:
        image: alpine:3.2
        command: ["/bin/sh", "-c", "while true; do echo 'HTTP/1.1 200 OK\n\nHello World!' | nc -l -p 80; done"]
        ```

        然后，我们使用 DockerComposeContainer 规则。该规则将启动并运行组成文件中定义的服务。

        我们使用 getServiceHost 和 getServicePost 方法建立服务的连接地址：

        ![DockerComposeContainerLiveTest.java](/src/test/java/com/baeldung/testconainers/DockerComposeContainerLiveTest.java)

5. 结论

    我们看到了如何使用 TestContainers 库。它简化了集成测试的开发和运行。

    我们使用 GenericContainer 规则来处理给定 docker 镜像的容器。然后，我们了解了 PostgreSQLContainer、BrowserWebDriverContainer 和 DockerComposeContainer 规则。它们为特定用例提供了更多的功能。
