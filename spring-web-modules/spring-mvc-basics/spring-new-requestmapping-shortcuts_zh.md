# [Spring@RequestMapping新的捷径注解](https://www.baeldung.com/spring-new-requestmapping-shortcuts)

Spring 4.3[引入](https://jira.spring.io/browse/SPR-13442)了一些非常酷的方法级组成的注解，以便在典型的Spring MVC项目中顺利处理@RequestMapping。

1. 新注解

    通常情况下，如果我们想用传统的@RequestMapping注解来实现URL处理程序，那么它应该是这样的。

    `@RequestMapping(value = "/get/{id}", method = RequestMethod.GET)`

    新的方法使得它可以简单地缩短为。

    `@GetMapping("/get/{id})`

    Spring目前支持五种内置注解，用于处理不同类型的HTTP请求方法，包括GET、POST、PUT、DELETE和PATCH。这些注解是

    - @GetMapping
    - @PostMapping
    - @PutMapping
    - @DeleteMapping
    - @PatchMapping

    从命名规则中我们可以看到，每个注解都是为了处理各自传入的请求方法类型，例如，@GetMapping用于处理GET类型的请求方法，@PostMapping用于处理POST类型的请求方法，等等。

2. 工作原理

    上述所有的注解都已经在内部用@RequestMapping和方法元素中的相应值进行了注解。

    例如，如果我们看一下@GetMapping注解的源代码，我们可以看到它已经用RequestMethod.GET注解了，具体方式如下。

    ```java
    @Target({ java.lang.annotation.ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @RequestMapping(method = { RequestMethod.GET })
    public @interface GetMapping {
        // abstract codes
    }
    ```

    所有其他的注解都是以同样的方式创建的，即@PostMapping是用RequestMethod.POST注解的，@PutMapping是用RequestMethod.PUT注解的，等等。

    注释的完整源代码可[在此](https://github.com/spring-projects/spring-framework/tree/master/spring-web/src/main/java/org/springframework/web/bind/annotation)获得。

3. 实施

    让我们尝试使用这些注解来构建一个快速的REST应用程序。

    请注意，由于我们将使用Maven来构建项目，并使用Spring MVC来创建我们的应用程序，因此我们需要在pom.xml中添加必要的依赖项。

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>5.3.4.RELEASE</version>
    </dependency>
    ```

    现在，我们需要创建一个控制器来映射传入的请求URL。在这个控制器中，我们将逐一使用所有这些注解。

    参见：[RequestMappingShortcutsController.java](./src/main/java/com/baeldung/web/controller/RequestMappingShortcutsController.java)。

    - @GetMapping
    - @PostMapping
    - @PutMapping
    - @DeleteMapping
    - @PatchMapping

    需要注意的几点：

    - 我们使用了必要的注解来处理适当的带有URI的传入HTTP方法。例如，@GetMapping来处理"/get"URI，@PostMapping来处理"/post"URI等等。
    - 由于我们正在制作一个基于REST的应用程序，我们将返回一个带有200响应代码的常量字符串（每个请求类型都是唯一的）来简化应用程序。在这种情况下，我们使用了Spring的@ResponseBody注解。
    - 如果我们必须处理任何URL路径变量，我们可以用更少的方式来处理，就像以前使用@RequestMapping一样。

4. 测试应用程序

    为了测试该应用程序，我们需要使用JUnit创建几个测试案例。我们将使用SpringJUnit4ClassRunner来启动测试类。我们将创建五个不同的测试用例来测试每个注解和我们在控制器中声明的每个处理器。

    参见：[RequestMapingShortcutsIntegrationTest.java](./src/test/java/com/baeldung/web/controller/RequestMapingShortcutsIntegrationTest.java)

    - giventUrl_whenGetRequest_thenFindGetResponse()
    - givenUrl_whenPostRequest_thenFindPostResponse()

    另外，我们也可以随时使用任何普通的REST客户端，例如PostMan、RESTClient等，来测试我们的应用程序。在这种情况下，我们在使用其余的客户端时，需要稍微注意选择正确的HTTP方法类型。否则，它将抛出405错误状态。

5. 总结

    在这篇文章中，我们快速介绍了使用传统Spring MVC框架进行快速Web开发时的不同@RequestMapping快捷方式。我们可以利用这些快捷方式创建一个干净的代码库。
