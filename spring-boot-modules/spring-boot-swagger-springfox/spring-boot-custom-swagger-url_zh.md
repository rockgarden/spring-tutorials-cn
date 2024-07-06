# [更改 Swagger-UI URL 前缀](https://www.baeldung.com/spring-boot-custom-swagger-url)

1. 概述

    作为一名优秀的开发人员，我们知道文档对于构建 REST API 至关重要，因为文档可以帮助 API 的用户无缝工作。如今，大多数 Java 开发人员都在使用 Spring Boot。目前，[Springfox](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api) 和 [SpringDoc](https://www.baeldung.com/spring-rest-openapi-documentation) 这两个工具简化了 [Swagger](https://en.wikipedia.org/wiki/Swagger_(software)) API 文档的生成和维护。

    在本教程中，我们将讨论如何更改这些工具默认提供的 Swagger-UI URL 前缀。

2. 使用 Springdoc 更改 Swagger UI URL 前缀

    首先，我们可以看看[如何使用 OpenAPI 3.0 设置 REST API 文档](https://www.baeldung.com/spring-rest-openapi-documentation)。

    首先，根据上述文章，我们需要添加一个条目来添加 SpringDoc 的依赖关系：

    ```xml
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.0.2</version>
    </dependency>
    ```

    swagger-ui 的默认 URL 将是 <http://localhost:8080/swagger-ui.html> 。

    现在让我们看看自定义 swagger-UI URL 的两种方法。我们将从 /myproject 开始。

    1. 使用 application.properties 文件

        由于我们已经熟悉了 Spring 中的许多不同属性，因此需要在 application.properties 文件中添加以下属性：

        ```properites
        springdoc.swagger-ui.disable-swagger-default-url=true
        springdoc.swagger-ui.path=/myproject
        ```

    2. 使用配置类

        我们也可以在配置文件中进行这一修改：

        ```java
        @Component
        public class SwaggerConfiguration implements ApplicationListener<ApplicationPreparedEvent> {

            @Override
            public void onApplicationEvent(final ApplicationPreparedEvent event) {
                ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
                Properties props = new Properties();
                props.put("springdoc.swagger-ui.path", swaggerPath());
                environment.getPropertySources()
                .addFirst(new PropertiesPropertySource("programmatically", props));
            }

            private String swaggerPath() {
                return "/myproject"; // TODO: implement your logic here.
            }
        }
        ```

        在这种情况下，我们需要在应用程序启动前注册监听器：

        ```java
        public static void main(String[] args) {
            SpringApplication application = new SpringApplication(SampleApplication.class);
            application.addListeners(new SwaggerConfiguration());
            application.run(args);
        }
        ```

3. 使用 Springfox 更改 Swagger UI URL 前缀

    我们可以看看如何通过[使用 Swagger 设置示例](https://www.baeldung.com/swagger-set-example-description)和描述以及[使用 Springfox 设置带有 Spring REST API 的 Swagger 2 来设置 REST API 文档](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)。

    首先，根据上述文章，我们需要添加一个条目，以添加 Springfox 的依赖关系：

    ```xml
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
        <version>3.0.0</version>
    </dependency>
    ```

    现在，假设我们要将此 URL 更改为 <http://localhost:8080/myproject/swagger-ui/index.html> 。让我们回顾一下可以帮助我们实现这一目标的两种方法。

    1. 使用 application.properties 文件

        与上述 SpringDoc 的示例类似，在 application.properties 文件中添加以下属性可帮助我们成功更改 URL：

        `springfox.documentation.swagger-ui.base-url=myproject`

    2. 在配置中使用 Docket Bean

        ```java
        @Bean
        public Docket api() {
            return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build();
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/myproject", "/");
        }
        ```

4. 添加重定向控制器

    我们还可以在 API 端点添加重定向逻辑。在这种情况下，使用 SpringDoc 还是 Springfox 并不重要：

    ```java
    @Controller
    public class SwaggerController {

    @RequestMapping("/myproject")
    public String getRedirectUrl() {
        return "redirect:swagger-ui/";
        }
    }
    ```

5. 结论

    在本文中，我们学习了如何使用 Springfox 和 SpringDoc 更改 REST API 文档的默认 swagger-ui URL。
