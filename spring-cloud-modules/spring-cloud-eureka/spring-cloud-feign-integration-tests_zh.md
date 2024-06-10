# [使用 Spring Cloud Netflix 和 Feign 进行集成测试](https://www.baeldung.com/spring-cloud-feign-integration-tests)

1. 概述

    在本文中，我们将探讨 Feign 客户端的集成测试。

    我们将创建一个基本的 [Open Feign Client](https://www.baeldung.com/spring-cloud-openfeign)，并在 [WireMock](https://www.baeldung.com/introduction-to-wiremock) 的帮助下编写一个简单的集成测试。

    之后，我们将为客户端添加一个 [Ribbon](https://www.baeldung.com/spring-cloud-rest-client-with-netflix-ribbon) 配置，并为其创建一个集成测试。最后，我们将配置一个 [Eureka](https://www.baeldung.com/spring-cloud-netflix-eureka) 测试容器，并测试这一设置，以确保我们的整个配置都能按预期运行。

2. Feign 客户端

    要设置 Feign 客户端，我们应首先添加 Spring Cloud OpenFeign Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    ```

    之后，让我们为模型创建一个 Book 类：

    ```java
    public class Book {
        private String title;
        private String author;
    }
    ```

    最后，创建我们的 Feign Client 接口：

    ```java
    @FeignClient(name = "books-service")
    public interface BooksClient {
        @RequestMapping("/books")
        List<Book> getBooks();
    }
    ```

    现在，我们有了一个从 REST 服务中检索图书列表的 Feign Client。现在，让我们继续编写一些集成测试。

3. 线模拟

    1. 设置 WireMock 服务器

        如果要测试 BooksClient，我们需要一个提供 /books 端点的模拟服务。我们的客户端将调用该模拟服务。为此，我们将使用 WireMock。

        因此，让我们添加 WireMock Maven 依赖项：

        ```xml
        <dependency>
            <groupId>org.wiremock</groupId>
            <artifactId>wiremock-standalone</artifactId>
            <scope>test</scope>
        </dependency>
        ```

        并配置 mock 服务器：

        ```java
        @TestConfiguration
        @ActiveProfiles("test")
        public class WireMockConfig {

            @Bean(initMethod = "start", destroyMethod = "stop")
            public WireMockServer mockBooksService() {
                return new WireMockServer(80);
            }

            @Bean(initMethod = "start", destroyMethod = "stop")
            public WireMockServer mockBooksService2() {
                return new WireMockServer(81);
            }
        }
        ```

        现在我们有两个正在运行的模拟服务器，分别接受 80 和 81 端口的连接。

    2. 设置模拟

        让我们在 [application-test.yml](./spring-cloud-eureka-feign-client-integration-test/src/test/resources/application-test.yml) 中添加指向 WireMockServer 端口的属性 book-service url。

        我们还要为 /books 端点准备一个模拟响应 [get-books-response.json](./spring-cloud-eureka-feign-client-integration-test/src/test/resources/payload/get-books-response.json)。

        现在让我们为 /books 端点上的 GET 请求配置模拟响应：

        [BookMocks.java](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/BookMocks.java)

        至此，所有必要的配置都已就绪。让我们继续编写第一个测试。

4. 我们的第一个集成测试

    让我们创建一个集成测试 [BooksClientIntegrationTest](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/BooksClientIntegrationTest.java)。

    至此，我们已经为 SpringBootTest 配置了一个 WireMockServer，当 BooksClient 调用 /books 端点时，它就会返回预定义的书籍列表。

    最后，让我们添加测试方法：

    ```java
    @Test
    public void whenGetBooks_thenBooksShouldBeReturned() {
        assertFalse(booksClient.getBooks().isEmpty());
    }

    @Test
    public void whenGetBooks_thenTheCorrectBooksShouldBeReturned() {
        assertTrue(booksClient.getBooks()
        .containsAll(asList(
            new Book("Dune", "Frank Herbert"),
            new Book("Foundation", "Isaac Asimov"))));
    }
    ```

5. 与 Spring Cloud LoadBalancer 集成

    现在，让我们通过添加 Spring Cloud LoadBalancer 提供的负载平衡功能来改进我们的客户端。

    我们只需在客户端接口中移除硬编码的服务 URL，改用服务名称 book-service 来引用服务即可：

    ```java
    @FeignClient(name= "books-service")
    public interface BooksClient {
    ...
    ```

    接下来，添加 maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    ```

    现在让我们再次运行 BooksClientIntegrationTest。它应该会通过，从而确认新设置按预期运行。

    1. 动态端口配置

        如果不想硬编码服务器端口，我们可以配置 WireMock 在启动时使用动态端口。

        为此，让我们创建另一个测试配置 [TestConfig](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/TestConfig.java)

        该配置设置了两个 WireMock 服务器，每个服务器都运行在运行时动态分配的不同端口上。此外，它还用这两个模拟服务器配置了 Ribbon 服务器列表。

    2. 负载平衡测试

        现在我们已经配置好了 Ribbon 负载均衡器，让我们确保 BooksClient 能正确地在两个模拟服务器之间交替运行：[LoadBalancerBooksClientIntegrationTest](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/LoadBalancerBooksClientIntegrationTest.java)

6. Eureka集成

    到目前为止，我们已经了解了如何测试使用 Spring Cloud LoadBalancer 进行负载平衡的客户端。但如果我们的设置使用了像 Eureka 这样的服务发现系统呢？我们应该编写一个集成测试，确保我们的 BooksClient 在这种情况下也能按预期运行。

    为此，我们将运行一个 Eureka 服务器作为测试容器。然后，我们启动并在 Eureka 容器中注册一个模拟图书服务。最后，一旦安装完成，我们就可以运行测试了。

    在继续之前，让我们添加测试容器和 Netflix Eureka 客户端 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    ```

    1. 测试容器设置

        让我们创建一个将启动 Eureka 服务器的 TestContainer 配置：[EurekaContainerConfig](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/EurekaContainerConfig.java)

        我们可以看到，上面的初始化程序启动了容器。然后，它会暴露 Eureka 服务器正在监听的 8761 端口。

        最后，在 Eureka 服务启动后，我们需要更新 eureka.client.serviceUrl.defaultZone 属性。这将定义用于服务发现的 Eureka 服务器地址。

    2. 注册模拟服务器

        既然 Eureka 服务器已经启动并运行，我们就需要注册一个模拟图书服务。我们只需创建一个 RestController 即可：[MockBookServiceConfig](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/MockBookServiceConfig.java)

        为了注册这个控制器，我们现在要做的就是确保 application-eureka-test.yml 中的 spring.application.name 属性为 books-service，与 BooksClient 接口中使用的服务名称相同。

        > 注意：既然 netflix-eureka-client 库已列入我们的依赖关系列表，那么 Eureka 将默认用于服务发现。因此，如果我们希望之前不使用 Eureka 的测试继续通过，就需要手动将 eureka.client.enabled 设为 false。这样，即使库在路径上，BooksClient 也不会尝试使用 Eureka 来定位服务，而是使用 Ribbon 配置。

    3. 集成测试

        我们再次获得了所有需要的配置文件，让我们把它们放在一个测试中：

        [ServiceDiscoveryBooksClientIntegrationTest](./spring-cloud-eureka-feign-client-integration-test/src/test/java/com/baeldung/spring/cloud/client/LoadBalancerBooksClientIntegrationTest.java)

        在这个测试中会发生一些事情。让我们逐一查看。

        首先，EurekaContainerConfig 中的上下文初始化程序启动了 Eureka 服务。

        然后，SpringBootTest 启动书籍服务应用程序，该应用程序将公开 MockBookServiceConfig 中定义的控制器。

        由于 Eureka 容器和网络应用的启动可能需要几秒钟，我们需要等待 books-service 注册完成。这发生在测试的 setUp 过程中。

        最后，测试方法将验证 BooksClient 与 Eureka 配置的结合是否正确。

7. 结论

    在本文中，我们探讨了为 Spring Cloud Feign 客户端编写集成测试的不同方法。我们从一个基本客户端开始，并在 WireMock 的帮助下对其进行了测试。之后，我们开始使用 Ribbon 添加负载平衡。我们编写了一个集成测试，并确保我们的 Feign Client 能与 Ribbon 提供的客户端负载平衡正常工作。最后，我们添加了 Eureka 服务发现功能。我们再次确保我们的客户端仍能按预期运行。
