# [在 Spring Boot 中从 Swagger 文档中隐藏端点](https://www.baeldung.com/spring-swagger-hiding-endpoints)

1. 概述

    在创建 Swagger 文档时，我们经常需要隐藏端点，以免暴露给最终用户。最常见的情况是端点尚未准备就绪。此外，我们可能有一些不想公开的私有端点。

    在这篇短文中，我们将探讨如何从 Swagger API 文档中隐藏端点。为此，我们将在控制器类中使用注解。

2. 使用 @ApiIgnore 隐藏端点

    @ApiIgnore 注解允许我们隐藏端点。让我们在控制器中为端点添加此注解：

    ```java
    @ApiIgnore
    @ApiOperation(value = "This method is used to get the author name.")
    @GetMapping("/getAuthor")
    public String getAuthor() {
        return "Umang Budhwar";
    }
    ```

3. 使用 @ApiOperation 隐藏端点

    另外，我们还可以使用 @ApiOperation 隐藏单个端点：

    ```java
    @ApiOperation(value = "This method is used to get the current date.", hidden = true)
    @GetMapping("/getDate")
    public LocalDate getDate() {
        return LocalDate.now();
    }
    ```

    请注意，我们需要将 hidden 属性设置为 true，以使 Swagger 忽略此端点。

4. 使用 @ApiIgnore 隐藏所有端点

    不过，有时我们需要隐藏控制器类的所有端点。我们可以通过使用 @ApiIgnore 对控制器类进行注解来实现这一目的：

    ```java
    @ApiIgnore
    @RestController
    public class RegularRestController {}
    ```

    需要注意的是，这将从文档中隐藏控制器本身。

5. 使用 @Hidden 隐藏端点

    如果使用 OpenAPI v3，我们可以使用 @Hidden 注解隐藏端点：

    ```java
    @Hidden
    @GetMapping("/getAuthor")
    public String getAuthor() {
        return "Umang Budhwar";
    }
    ```

6. 使用 @Hidden 隐藏所有端点

    同样，我们可以用 @Hidden 来注解控制器，以隐藏所有端点：

    ```java
    @Hidden
    @RestController
    public class RegularRestController {}
    ```

    这也将从文档中隐藏控制器。

    注意：只有在使用 OpenAPI 时才能使用 @Hidden。Swagger v3 中对该注解的支持仍在进行中。

7. 结束语

    在本教程中，我们了解了如何从 Swagger 文档中隐藏端点。我们讨论了如何隐藏单个端点和所有控制器类端点。
