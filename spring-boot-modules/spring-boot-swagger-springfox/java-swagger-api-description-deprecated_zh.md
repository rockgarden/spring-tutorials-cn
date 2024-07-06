# [Swagger @Api 描述已被弃用](https://www.baeldung.com/java-swagger-api-description-deprecated)

1. 概述

    描述 RESTful API 在文档中扮演着重要角色。用于记录 REST API 的一个常用工具是 Swagger 2。然而，用于添加描述的一个有用属性已被弃用。在本教程中，我们将使用 Swagger 2 和 OpenAPI 3 找到解决过时的描述属性的方法，并展示如何使用它们来描述 Spring Boot REST API 应用程序。

2. API 描述

    默认情况下，Swagger 会为 REST API 类名生成空描述。因此，我们必须指定一个合适的注解来描述 REST API。我们可以使用 [Swagger 2](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api) 中的 @Api 注解或 [OpenAPI 3](https://www.baeldung.com/spring-rest-openapi-documentation) 中的 @Tag 注解。

3. Swagger 2

    要在 Spring Boot REST API 中使用 Swagger 2，我们可以使用 Springfox 库。我们需要在 pom.xml 文件中添加 springfox-boot-starter 依赖项：

    ```xml
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-boot-starter</artifactId>
        <version>3.0.0</version>
    </dependency>
    ```

    Springfox 库提供了 @Api 注解，用于将类配置为 Swagger 资源。在此之前，@Api 注解提供了一个用于自定义 API 文档的描述属性：

    `@Api(value = "", description = "")`

    不过，如前所述，description 属性已被弃用。幸运的是，我们还有另一种选择。我们可以使用 tags 属性：

    `@Api(value = "", tags = {"tag_name"})`

    在 Swagger 1.5 中，我们可以使用 @SwaggerDefinition 注解来定义标记。但是，Swagger 2 不再支持该注解。因此，在 Swagger 2 中，我们在 Docket Bean 中定义标记和描述：

    ![SwaggerConfiguration](/src/main/java/com/baeldung/apiswagger/config/SwaggerConfiguration.java)

    在这里，我们使用 Docket Bean 中的标签类创建标签。这样，我们就可以在控制器中引用标签了：

    ```java
    @RestController
    @RequestMapping("/api/book")
    @Api(tags = {SwaggerConfiguration.BOOK_TAG})
    public class BookController {}
    ```

4. OpenAPI 3

    OpenAPI 3 是 OpenAPI 规范的最新版本。它是 OpenAPI 2（Swagger 2）的后续版本。使用 OpenAPI 3 描述 API 时，我们可以使用 @Tag 注解。此外，@Tag 注解还提供了描述和外部链接。让我们定义 BookController 类：

    ```java
    @RestController
    @RequestMapping("/api/book")
    @Tag(name = "book service", description = "the book API with description tag annotation")
    public class BookController {}
    ```

5. 结论

    在这篇简短的文章中，我们介绍了如何在 Spring Boot 应用程序中为 REST API 添加描述。我们研究了如何使用 Swagger 2 和 OpenAPI 3 来实现这一目标。
