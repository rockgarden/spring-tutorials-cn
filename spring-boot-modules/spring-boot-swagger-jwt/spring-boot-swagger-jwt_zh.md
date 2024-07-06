# [使用 Spring Boot 和 Swagger UI 设置 JWT](https://www.baeldung.com/spring-boot-swagger-jwt)

1. 简介

    在本简短教程中，我们将了解如何配置 Swagger UI，使其在调用我们的 API 时包含 JSON Web 令牌 (JWT)。

2. Maven 依赖项

    在本例中，我们将使用 [springdoc-openapi-ui](https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-ui/1.7.0)，它包含了开始使用 Swagger 和 Swagger UI 所需的所有依赖项。让我们将其添加到 pom.xml 文件中：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-ui</artifactId>
        <version>1.7.0</version>
    </dependency>
    ```

3. Swagger 配置

    首先，我们需要配置 JWT SecurityScheme：

    ```java
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }
    ```

    然后，我们配置 OpenAPI Bean，使其包含 API 信息和安全方案：

    ```java
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().
                addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes
                ("Bearer Authentication", createAPIKeyScheme()))
            .info(new Info().title("My REST API")
                .description("Some custom description of API.")
                .version("1.0").contact(new Contact().name("Sallo Szrajbman")
                    .email( "www.baeldung.com").url("salloszraj@gmail.com"))
                .license(new License().name("License of API")
                    .url("API license URL")));
    }
    ```

4. REST 控制器

    在我们的 ClientsRestController 中，让我们编写一个简单的 getClients 端点来返回客户列表：

    ```java
    @RestController(value = "/clients")
    @Tag(name = "Clients")
    public class ClientsRestController {
        @Operation(summary = "This method is used to get the clients.")
        @GetMapping
        public List<String> getClients() {
            return Arrays.asList("First Client", "Second Client");
        }
    }
    ```

5. Swagger UI

    启动应用程序时，我们可以访问 <http://localhost:8080/swagger-ui.html> URL 上的 Swagger UI。

    会出现带有授权(Authorize)按钮的 Swagger 用户界面。

    当我们点击授权按钮时，Swagger UI 会要求我们输入 JWT。

    我们需要输入令牌并点击授权，从那时起，向我们的 API 发出的所有请求都将自动在 HTTP 头信息中包含令牌。

6. 使用 JWT 发送 API 请求

    向我们的 API 发送请求时，"Authorization（授权" 标头中包含了我们的令牌值。

7. 总结

    在本文中，我们了解了 Swagger UI 如何提供自定义配置来设置 JWT，这在处理应用程序授权时很有帮助。在 Swagger UI 中授权后，所有请求都将自动包含我们的 JWT。
