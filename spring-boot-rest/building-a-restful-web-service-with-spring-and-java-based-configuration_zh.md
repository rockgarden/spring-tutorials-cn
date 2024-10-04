# [使用 Spring 和 Java Config 构建 REST API](https://www.baeldung.com/building-a-restful-web-service-with-spring-and-java-based-configuration)

1. 概述

    在本教程中，我们将学习如何在 Spring 中设置 REST，包括控制器和 HTTP 响应代码、配置有效载荷编译和内容协商。

2. 了解 Spring 中的 REST

    Spring 框架支持两种创建 RESTful 服务的方法：

    - 使用带有模型和视图的 MVC
    - 使用 HTTP 消息转换器

    ModelAndView 方法历史更悠久、文档更完善，但也更繁琐、配置更繁重。它试图将 REST 范式塞进旧模型中，这并非没有问题。Spring 团队理解这一点，并从 Spring 3.0 开始提供一流的 REST 支持。

    基于 HttpMessageConverter 和注解的新方法更加轻量级，也更易于实现。配置非常简单，它为我们期望的 RESTful 服务提供了合理的默认值。

3. Java 配置

    ```java
    @Configuration
    @EnableWebMvc
    public class WebConfig{
        //
    }
    ```

    新的 @EnableWebMvc 注解做了一些有用的事情；特别是在 REST 的情况下，它会检测类路径上是否存在 Jackson 和 JAXB 2，并自动创建和注册默认的 JSON 和 XML 转换器。注解的功能等同于 XML 版本：

    `<mvc:annotation-driven />`

    这是一种快捷方式，虽然在很多情况下都很有用，但并不完美。当我们需要更复杂的配置时，可以移除注解并直接扩展 WebMvcConfigurationSupport。

    1. 使用 Spring Boot

        如果我们使用 @SpringBootApplication 注解，并且类路径上有 spring-webmvc 库，那么 @EnableWebMvc 注解就会自动添加，并带有[默认的自动配置](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-web-applications.html#boot-features-spring-mvc-auto-configuration)。

        我们仍然可以通过在 @Configuration 注解类上实现 WebMvcConfigurer 接口来为该配置添加 MVC 功能。我们还可以使用 WebMvcRegistrationsAdapter 实例来提供我们自己的 RequestMappingHandlerMapping、RequestMappingHandlerAdapter 或 ExceptionHandlerExceptionResolver 实现。

        最后，如果我们想放弃 Spring Boot 的 MVC 功能并声明自定义配置，可以使用 @EnableWebMvc 注解来实现。

4. 测试 Spring 上下文

    从 Spring 3.1 开始，我们为 @Configuration 类提供了一流的测试支持：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(
    classes = {WebConfig.class, PersistenceConfig.class},
    loader = AnnotationConfigContextLoader.class)
    public class SpringContextIntegrationTest {
    @Test
    public void contextLoads(){
        // When
    }
    }
    ```

    我们使用 @ContextConfiguration 注解指定了 Java 配置类。新的 AnnotationConfigContextLoader 会从 @Configuration 类中加载 Bean 定义。

    请注意，WebConfig 配置类没有包含在测试中，因为它需要在 Servlet 上下文中运行，而 Servlet 上下文并未提供。

    1. 使用 Spring Boot

        Spring Boot 提供了多个注解，以便以更直观的方式为我们的测试设置 Spring ApplicationContext。

        我们可以只加载应用程序配置的特定部分，也可以模拟整个上下文启动过程。

        例如，如果我们想在不启动服务器的情况下创建整个上下文，就可以使用 @SpringBootTest 注解。

        有了这些，我们就可以添加 @AutoConfigureMockMvc 来注入一个 MockMvc 实例并发送 HTTP 请求：

        ```java
        @RunWith(SpringRunner.class)
        @SpringBootTest
        @AutoConfigureMockMvc
        public class FooControllerAppIntegrationTest {
            @Autowired
            private MockMvc mockMvc;
            @Test
            public void whenTestApp_thenEmptyResponse() throws Exception {
                this.mockMvc.perform(get("/foos")
                    .andExpect(status().isOk())
                    .andExpect(...);
            }
        }
        ```

        为了避免创建整个上下文并只测试 MVC 控制器，我们可以使用 @WebMvcTest：

        ```java
        @RunWith(SpringRunner.class)
        @WebMvcTest(FooController.class)
        public class FooControllerWebLayerIntegrationTest {

            @Autowired
            private MockMvc mockMvc;

            @MockBean
            private IFooService service;

            @Test()
            public void whenTestMvcController_thenRetrieveExpectedResult() throws Exception {
                // ...
                this.mockMvc.perform(get("/foos")
                    .andExpect(...));
            }
        }
        ```

        我们可以在 [“Spring Boot 中的测试” 一文](https://www.baeldung.com/spring-boot-testing)中找到这方面的详细信息。

5. 控制器

    @RestController 是 RESTful API 整个 Web 层的核心组件。在本文中，控制器将模拟一个简单的 REST 资源 Foo：

    ```java
    @RestController
    @RequestMapping("/foos")
    class FooController {

        @Autowired
        private IFooService service;

        @GetMapping
        public List<Foo> findAll() {
            return service.findAll();
        }

        @GetMapping(value = "/{id}")
        public Foo findById(@PathVariable("id") Long id) {
            return RestPreconditions.checkFound(service.findById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Long create(@RequestBody Foo resource) {
            Preconditions.checkNotNull(resource);
            return service.create(resource);
        }

        @PutMapping(value = "/{id}")
        @ResponseStatus(HttpStatus.OK)
        public void update(@PathVariable( "id" ) Long id, @RequestBody Foo resource) {
            Preconditions.checkNotNull(resource);
            RestPreconditions.checkNotNull(service.getById(resource.getId()));
            service.update(resource);
        }

        @DeleteMapping(value = "/{id}")
        @ResponseStatus(HttpStatus.OK)
        public void delete(@PathVariable("id") Long id) {
            service.deleteById(id);
        }

    }
    ```

    正如我们所见，我们使用的是一个简单明了的 Guava 风格 RestPreconditions 工具：

    ```java
    public class RestPreconditions {
        public static <T> T checkFound(T resource) {
            if (resource == null) {
                throw new MyResourceNotFoundException();
            }
            return resource;
        }
    }
    ```

    控制器的实现是非公开的，因为它并不需要公开。

    通常，控制器是依赖链中的最后一个。它接收来自 Spring 前端控制器（DispatcherServlet）的 HTTP 请求，并将其委托给服务层。如果没有需要通过直接引用来注入或操作控制器的用例，那么我们最好不要将其声明为公有。

    请求映射简单明了。与任何控制器一样，映射的实际值和 HTTP 方法决定了请求的目标方法。@RequestBody 会将方法的参数绑定到 HTTP 请求的正文，而 @ResponseBody 也会将响应和返回类型绑定到正文。

    @RestController 是在类中包含 @ResponseBody 和 @Controller 注解的简写。

    它们还能确保使用正确的 HTTP 转换器对资源进行 marshalled 和 unmarshalled。内容协商（Content negotiation）将主要根据接受头（Accept header）来选择使用哪个活动转换器，但也可能使用其他 HTTP 头来确定表示方式。

6. 映射 HTTP 响应代码

    HTTP 响应的状态代码是 REST 服务最重要的部分之一，这个问题很快就会变得非常复杂。正确处理这些代码可能决定服务的成败。

    1. 未映射请求

        如果 Spring MVC 收到一个没有映射的请求，它会认为该请求是不允许的，并向客户端返回 405 METHOD NOT ALLOWED。

        在向客户端返回 405 时，包含 Allow HTTP 标头以指定允许的操作也是一种很好的做法。这是 Spring MVC 的标准行为，不需要任何额外配置。

    2. 有效的映射请求

        对于任何有映射的请求，如果没有指定其他状态代码，Spring MVC 会认为该请求有效，并响应 200 OK。

        正因为如此，控制器为创建、更新和删除操作声明了不同的 @ResponseStatus，但没有为获取操作声明 @ResponseStatus，因为获取操作确实应该返回默认的 200 OK。

    3. 客户端错误

        在客户端出错的情况下，自定义异常会被定义并映射到相应的错误代码。

        只要从网络层的任何一层抛出这些异常，就能确保 Spring 在 HTTP 响应中映射出相应的状态代码：

        ```java
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public class BadRequestException extends RuntimeException {
            //
        }
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public class ResourceNotFoundException extends RuntimeException {
           //
        }
        ```

        这些异常是 REST API 的一部分，因此，我们只应在与 REST 相对应的适当层中使用它们；例如，如果存在 DAO/DAL 层，就不应直接使用这些异常。

        还要注意的是，这些异常并不是经过检查的异常，而是符合 Spring 实践和习惯做法的运行时异常。

    4. 使用 @ExceptionHandler

        在特定状态代码上映射自定义异常的另一种方法是在控制器中使用 @ExceptionHandler 注解。这种方法的问题在于，该注解仅适用于定义该注解的控制器。这意味着我们需要在每个控制器中单独声明它们。

        当然，在 Spring 和 Spring Boot 中还有更多[处理错误的方法](https://www.baeldung.com/exception-handling-for-rest-with-spring)，它们提供了更大的灵活性。

7. 其他 Maven 依赖项

    除了[标准 Web 应用程序](https://www.baeldung.com/spring-with-maven#mvc)所需的 Spring-webmvc 依赖项外，我们还需要为 REST API 设置内容 marshalling 和 unmarshalling：

    ```xml
    <dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.9.8</version>
    </dependency>
    <dependency>
        <groupId>javax.xml.bind</groupId>
        <artifactId>jaxb-api</artifactId>
        <version>2.3.1</version>
        <scope>runtime</scope>
    </dependency>
    </dependencies>
    ```

    我们将使用这些库将 REST 资源的表示转换为 JSON 或 XML。

    1. 使用 Spring Boot

        如果我们要检索 JSON 格式的资源，Spring Boot 提供了对不同库的支持，即 Jackson、Gson 和 JSON-B。

        我们只需在类路径中包含任意一个映射库，就可以进行自动配置。

        通常，如果我们开发的是网络应用程序，我们只需添加 spring-boot-starter-web 依赖项，并依靠它将所有必要的工件包含到我们的项目中：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        ```

        Spring Boot 默认使用 Jackson。

        如果我们想将资源序列化为 XML 格式，就必须在依赖项中添加 Jackson XML 扩展（jackson-dataformat-xml），或者通过在资源上使用 @XmlRootElement 注解来使用 JAXB 实现（JDK 默认提供）。

8. 结论

本文介绍了如何使用 Spring 和基于 Java 的配置来实现和配置 REST 服务。

在本系列的下一篇文章中，我们将重点介绍 [API 的可发现性](https://www.baeldung.com/restful-web-service-discoverability)、高级内容协商以及资源的其他表示方法。
