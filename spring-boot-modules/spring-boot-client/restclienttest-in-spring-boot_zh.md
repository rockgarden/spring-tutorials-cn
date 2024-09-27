# [Spring Boot中@RestClientTest的快速指南](https://www.baeldung.com/restclienttest-in-spring-boot)

1. 介绍

    本文是对@RestClientTest注释的快速介绍。

    新的注释有助于简化和加快Spring应用程序中REST客户端的测试。

2. Spring Boot Pre-1.4中的REST客户端支持

    Spring Boot是一个方便的框架，它为许多自动配置的Spring bean提供了典型的设置，让您可以少专注于Spring应用程序的配置，而专注于代码和业务逻辑。

    但在1.3版本中，当我们想要创建或测试REST服务客户端时，我们得不到很多帮助。它对REST客户端的支持不是很深。

    为REST API创建客户端-通常使用RestTemplate实例。通常，在使用前必须进行配置，其配置可能会有所不同，因此Spring Boot不提供任何通用配置的RestTemplate bean。

    测试REST客户端也是如此。在Spring Boot 1.4.0之前，测试Spring REST客户端的程序与任何其他基于Spring的应用程序没有太大区别。您将创建一个MockRestServiceServer实例，将其绑定到正在测试的RestTemplate实例，并为其提供对请求的模拟响应，如下所在：

    ```java
    RestTemplate restTemplate = new RestTemplate();

    MockRestServiceServer mockServer =
      MockRestServiceServer.bindTo(restTemplate).build();
    mockServer.expect(requestTo("/greeting"))
      .andRespond(withSuccess());

    // Test code that uses the above RestTemplate ...

    mockServer.verify();
    ```

    您还必须初始化Spring容器，并确保仅将所需的组件加载到上下文中，以加快上下文加载时间（从而加快测试执行时间）。

3. Spring Boot 1.4+ 中新的REST客户端功能

    在Spring Boot 1.4中，该团队做出了坚定的努力来简化和加快REST客户端的创建和测试。

    所以，让我们来看看新功能。

    1. 将Spring Boot添加到您的项目中

        首先，您需要确保您的项目使用的是Spring Boot 1.4.x或更高版本：

        ```xml
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <relativePath/> <!-- lookup parent from repository -->
        </parent>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <scope>test</scope>
            </dependency>
        </dependencies>
        ```

    2. RestTemplateBuilder

        Spring Boot带来了自动配置的RestTemplateBuilder来简化RestTemplates的创建，以及匹配的@RestClientTest注释来测试使用RestTemplateBuilder构建的客户端。以下是使用RestTemplateBuilder自动注入的创建简单REST客户端的方法：

        ```java
        @Service
        public class DetailsServiceClient {

            private final RestTemplate restTemplate;

            public DetailsServiceClient(RestTemplateBuilder restTemplateBuilder) {
                restTemplate = restTemplateBuilder.build();
            }

            public Details getUserDetails(String name) {
                return restTemplate.getForObject("/{name}/details",
                  Details.class, name);
            }
        }
        ```

        请注意，我们没有明确地将RestTemplateBuilder实例连接到构造函数。这要归功于一个名为隐式构造函数注入的新Spring功能，本文对此进行了[讨论](https://www.baeldung.com/whats-new-in-spring-4-3)。

        RestTemplateBuilder提供了注册消息转换器、错误处理程序、URI模板处理程序、基本授权的便捷方法，以及使用您需要的任何其他定制器。

    3. @RestClientTest

        要测试使用RestTemplateBuilder构建的REST客户端，您可以使用带有@RestClientTest注释的SpringRunner执行测试类。此注释禁用全自动配置，仅应用与REST客户端测试相关的配置，即Jackson或GSON自动配置和@JsonComponent beans，但不是常规@Component beans。

        @RestClientTest确保自动配置Jackson和GSON支持，并将预配置的RestTemplateBuilder和MockRestServiceServer实例添加到上下文中。测试中的bean用@RestClientTest注释的值或组件属性指定：

        ```java
        @RunWith(SpringRunner.class)
        @RestClientTest(DetailsServiceClient.class)
        public class DetailsServiceClientTest {

            @Autowired
            private DetailsServiceClient client;

            @Autowired
            private MockRestServiceServer server;

            @Autowired
            private ObjectMapper objectMapper;

            @Before
            public void setUp() throws Exception {
                String detailsString = 
                  objectMapper.writeValueAsString(new Details("John Smith", "john"));
                
                this.server.expect(requestTo("/john/details"))
                  .andRespond(withSuccess(detailsString, MediaType.APPLICATION_JSON));
            }

            @Test
            public void whenCallingGetUserDetails_thenClientMakesCorrectCall() 
            throws Exception {

                Details details = this.client.getUserDetails("john");

                assertThat(details.getLogin()).isEqualTo("john");
                assertThat(details.getName()).isEqualTo("John Smith");
            }
        }
        ```

        首先，我们需要通过添加 @RunWith(SpringRunner.class) 注释来确保使用SpringRunner运行此测试。

        那么，有什么新鲜事吗？

        首先——@RestClientTest注释允许我们指定正在测试的确切服务——在我们的案例中，它是DetailsServiceClient类。此服务将被加载到测试上下文中，而其他所有内容都会被过滤掉。

        这允许我们在测试中自动连接DetailsServiceClient实例，并将其他所有内容留在外部，从而加快了上下文的加载速度。

        第二——由于MockRestServiceServer实例也为@RestClientTest注释测试配置（并为我们绑定到DetailsServiceClient实例），我们可以简单地注入并使用它。

        最后——对@RestClientTest的JSON支持允许我们注入Jackson的ObjectMapper实例来准备MockRestServiceServer的模拟答案值。

        剩下的就是执行对我们服务的调用并验证结果。

4. 结论

    在本文中，我们讨论了新的@RestClientTest注释，该注释允许轻松快速地测试使用Spring构建的REST客户端。
