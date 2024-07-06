# [移除 SpringFox Swagger-UI 中的基本错误控制器](https://www.baeldung.com/spring-swagger-remove-error-controller)

1. 概述

    在本教程中，我们将学习在 Spring Boot 应用程序中配置 Swagger 的多种方法，以隐藏 BasicErrorController 暴露的路径。

2. 项目目标

    本文将不涉及创建基本配置以开始使用 Spring Boot 和 Swagger-UI。我们可以使用一个已经配置好的项目，或者按照《使用 Spring REST API 设置 Swagger 2》指南创建基本配置。

3. 问题

    如果我们的代码包含一个 BasicErrorController，Swagger 默认会将其所有端点也包含在生成的文档中。我们需要提供自定义配置来移除不需要的控制器。

    例如，假设我们想提供标准 RestController 的 API 文档：

    ```java
    @RestController
    @RequestMapping("good-path")
    public class RegularRestController {
    @ApiOperation(value = "This method is used to get the author name.")
    @GetMapping("/getAuthor")
    public String getAuthor() {
        return "Name Surname";
    }
    // Other similar methods
    }
    ```

    另外，假设我们的代码包含一个扩展 BasicErrorController 的类：

    ```java
    @Component
    @RequestMapping("my-error-controller")
    public class MyErrorController extends BasicErrorController {
        // basic constructor
    }
    ```

    我们可以看到，my-error-controller 已包含在生成的文档中。

4. 解决方案

    在本节中，我们将介绍四种不同的解决方案，以从 Swagger 文档中排除资源。

    1. 使用 basePackage() 排除

        通过指定要记录的控制器的基础包，我们可以排除不需要的资源。

        只有当错误控制器包与标准控制器包不同时，这种方法才有效。
        在 Spring Boot 中，只要提供一个 [Docket](http://springfox.github.io/springfox/javadoc/2.7.0/index.html?springfox/documentation/spring/web/plugins/Docket.html) Bean 就足够了：

        ```java
        @Configuration
        public class SwaggerConfiguration {
            @Bean
            public Docket api() {
                return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("com.baeldung.swaggerconf.controller"))
                    .build();
            }
        }
        ```

        有了这个自定义配置，Swagger 将只检查指定包内的 REST Controller 方法。因此，举例来说，如果我们的 BasicErrorController 定义在 "com.baeldung.swaggerconf.error "包中，它将不会被考虑。

    2. 使用注释排除

        或者，我们也可以指出，Swagger 只能生成使用特定 Java 注释装饰的类的文档。

        在本例中，我们将其设置为 RestController.class：

        ```java
        @Bean
        public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
            .build();
        }
        ```

        在这种情况下，BasicErrorController 将被排除在 Swagger 文档之外，因为它没有使用 @RestController 注解进行装饰。而我们要记录的 RegularRestController 上有该注解。

    3. 使用 Regex 在路径上排除

        另一种方法是在自定义路径上指定 Regex。在这种情况下，只有映射到"/good-path "前缀的资源才会被记录：

        ```java
        @Bean
        public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
            .select()
            .paths(regex("/good-path/.*"))
            .build();
        }
        ```

    4. 使用 @ApiIgnore 排除

        最后，我们可以使用注解 @ApiIgnore 将特定类从 Swagger 中排除：

        ```java
        @Component
        @RequestMapping("my-error-controller")
        @ApiIgnore 
        public class MyErrorController extends BasicErrorController {}
        ```

5. 结论

    在本文中，我们介绍了在 Spring Boot 应用程序中配置 Swagger 以隐藏 BasicErrorController 资源的四种不同方法。
