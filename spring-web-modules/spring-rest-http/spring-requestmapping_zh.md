# [Spring RequestMapping](https://www.baeldung.com/spring-requestmapping)

1. 概述

    在本教程中，我们将重点介绍 Spring MVC 中的一个主要注解： @RequestMapping 注解。

    简单地说，该注解用于将 Web 请求映射到 Spring Controller 方法。

2. @RequestMapping 基础知识

    我们从一个简单的例子开始：使用一些基本条件将 HTTP 请求映射到方法。假设 Spring 默认在根[上下文路径](https://www.baeldung.com/spring-boot-context-path)（“/”）上提供内容。本文中的所有 CURL 请求都依赖于默认的根上下文路径。

    1. @RequestMapping - 通过路径

        ```java
        @RequestMapping(value = "/ex/foos", method = RequestMethod.GET)
        @ResponseBody
        public String getFoosBySimplePath() {
            return "Get some Foos";
        }
        ```

        要使用简单的 curl 命令测试此映射，请运行

        `curl -i http://localhost:8080/ex/foos`

    2. @RequestMapping - HTTP 方法

        HTTP 方法参数没有默认值。因此，如果我们不指定一个值，它就会映射到任何 HTTP 请求。

        下面是一个简单的示例，与前面的示例类似，但这次映射的是 HTTP POST 请求：

        ```java
        @RequestMapping(value = "/ex/foos", method = POST)
        @ResponseBody
        public String postFoos() {
            return "Post some Foos";
        }
        ```

        通过 curl 命令测试 POST：

        `curl -i -X POST http://localhost:8080/ex/foos`

3. 请求映射和 HTTP 头信息

    1. 使用头信息属性的 @RequestMapping

        通过为请求指定一个头文件，可以进一步缩小映射范围：

        ```java
        @RequestMapping(value = "/ex/foos", headers = "key=val", method = GET)
        @ResponseBody
        public String getFoosWithHeader() {
            return "Get some Foos with Header";
        }
        ```

        为了测试操作，我们将使用 curl 头信息支持：

        `curl -i -H "key:val" http://localhost:8080/ex/foos`

        甚至可以通过 @RequestMapping 的 headers 属性使用多个头：

        ```java
        @RequestMapping(
            value = "/ex/foos", 
            headers = { "key1=val1", "key2=val2" }, method = GET)
        @ResponseBody
        public String getFoosWithHeaders() {
            return "Get some Foos with Header";
        }
        ```

        我们可以用以下命令来测试

        `curl -i -H "key1:val1" -H "key2:val2" http://localhost:8080/ex/foos`

        请注意，在 curl 语法中，头关键字和头值之间用冒号隔开，这与 HTTP 规范相同，而在 Spring 中则使用等号。

    2. @RequestMapping 消耗和生成

        映射控制器方法产生的媒体类型值得特别注意。

        我们可以通过上面介绍的 @RequestMapping 头信息属性，根据请求的 Accept 头信息来映射请求：

        ```java
        @RequestMapping(
            value = "/ex/foos",
            method = GET,
            headers = "Accept=application/json"
        )
        @ResponseBody
        public String getFoosAsJsonFromBrowser() {
            return "Get some Foos with Header Old";
        }
        ```

        这种定义 Accept 头信息的匹配方式非常灵活--它使用 contains 代替 equals，因此像下面这样的请求仍然可以正确映射：

        ```bash
        curl -H "Accept:application/json,text/html" 
        http://localhost:8080/ex/foos
        ```

        从 Spring 3.1 开始，@RequestMapping 注解专门为此提供了 produces 和 consumes 属性：

        ```java
        @RequestMapping(
            value = "/ex/foos",
            method = RequestMethod.GET,
            produces = "application/json"
        )
        @ResponseBody
        public String getFoosAsJsonFromREST() {
            return "Get some Foos with Header New";
        }
        ```

        另外，从 Spring 3.1 开始，带有 headers 属性的旧类型映射将自动转换为新的生产机制，因此结果将完全相同。

        通过 curl 也能以同样的方式使用该机制：

        ```bash
        curl -H "Accept:application/json"
        http://localhost:8080/ex/foos
        ```

        此外，produces 还支持多值：

        ```java
        @RequestMapping(
            value = "/ex/foos",
            method = GET,
            produces = { "application/json", "application/xml" }
        )
        ```

        请记住，这两种指定 Accept 头信息的新旧方法基本上是相同的映射，因此 Spring 不允许它们同时使用。

        同时使用这两种方法将导致：

        ```log
        Caused by: java.lang.IllegalStateException: Ambiguous mapping found.
        Cannot map 'fooController' bean method
        java.lang.String
        org.baeldung.spring.web.controller
            .FooController.getFoosAsJsonFromREST()
        to
        { [/ex/foos],
        methods=[GET],params=[],headers=[],
        consumes=[],produces=[application/json],custom=[]
        }:
        There is already 'fooController' bean method
        java.lang.String
        org.baeldung.spring.web.controller
            .FooController.getFoosAsJsonFromBrowser()
        mapped.
        ```

        最后要说明的是新的生产和消费机制，其行为与大多数其他注解不同： 在类型级指定时，方法级注解不会补充类型级信息，而是覆盖类型级信息。

        当然，如果你想深入了解如何使用 Spring 构建 REST API，请查看新的 Spring REST [课程](https://www.baeldung.com/rest-with-spring-course)。

4. 使用路径变量的 RequestMapping

    映射 URI 的部分内容可以通过 @PathVariable 注解绑定到变量中。

    1. 单个 @PathVariable

        一个使用单一路径变量的简单示例

        ```java
        @RequestMapping(value = "/ex/foos/{id}", method = GET)
        @ResponseBody
        public String getFoosBySimplePathWithPathVariable(
        @PathVariable("id") long id) {
            return "Get a specific Foo with id=" + id;
        }
        ```

        可以使用 curl 进行测试：

        `curl http://localhost:8080/ex/foos/1`

        如果方法参数的名称与路径变量的名称完全一致，则可以通过使用不带值的 @PathVariable 来简化：

        ```java
        @RequestMapping(value = “/ex/foos/{id}”, method = GET)
        @ResponseBody
        public String getFoosBySimplePathWithPathVariable(
        @PathVariable String id) {
            return “Get a specific Foo with id=” + id；
        }
        ```

        请注意，@PathVariable 受益于自动类型转换，因此我们也可以将 id 声明为

        `@PathVariable long id`

    2. 多个 @PathVariable

        更复杂的 URI 可能需要将 URI 的多个部分映射到多个值：

        ```java
        @RequestMapping(value = "/ex/foos/{fooid}/bar/{barid}", method = GET)
        @ResponseBody
        public String getFoosBySimplePathWithPathVariables
        (@PathVariable long fooid, @PathVariable long barid) {
            return "Get a specific Bar with id=" + barid +
            " from a Foo with id=" + fooid;
        }
        ```

        用同样的方法很容易用 curl 进行测试：

        `curl http://localhost:8080/ex/foos/1/bar/2`

    3. 使用 Regex 的 @PathVariable

        在映射 @PathVariable 时也可以使用正则表达式。

        例如，我们将限制映射只接受 ID 的数值：

        ```java
        @RequestMapping(value = "/ex/bars/{numericId:[\\d]+}", method = GET)
        @ResponseBody
        public String getBarsBySimplePathWithPathVariable(
        @PathVariable long numericId) {
            return "Get a specific Bar with id=" + numericId;
        }
        ```

        这意味着以下 URI 将匹配：

        <http://localhost:8080/ex/bars/1>

        但这不会：

        <http://localhost:8080/ex/bars/abc>

5. 带有请求参数的请求映射

    @RequestMapping 允许使用 @RequestParam 注解轻松映射 URL 参数。

    我们现在将一个请求映射到一个 URI：

    <http://localhost:8080/ex/bars?id=100>

    ```java
    @RequestMapping(value = "/ex/bars", method = GET)
    @ResponseBody
    public String getBarBySimplePathWithRequestParam(
    @RequestParam("id") long id) {
        return "Get a specific Bar with id=" + id;
    }
    ```

    然后，我们使用控制器方法签名中的 @RequestParam(“id”) 注解提取 id 参数的值。

    要发送带有 id 参数的请求，我们将使用 curl 中的参数支持：

    `curl -i -d id=100 http://localhost:8080/ex/bars`

    在这个示例中，参数是直接绑定的，无需先声明。

    对于更高级的情况，@RequestMapping 可以选择性地定义参数，作为缩小请求映射范围的另一种方法：

    ```java
    @RequestMapping(value = "/ex/bars", params = "id", method = GET)
    @ResponseBody
    public String getBarBySimplePathWithExplicitRequestParam(
    @RequestParam("id") long id) {
        return "Get a specific Bar with id=" + id;
    }
    ```

    允许更灵活的映射。可以设置多个参数值，但并非必须使用所有参数：

    ```java
    @RequestMapping(
    value = "/ex/bars",
    params = { "id", "second" },
    method = GET)
    @ResponseBody
    public String getBarBySimplePathWithExplicitRequestParams(
    @RequestParam("id") long id) {
        return "Narrow Get a specific Bar with id=" + id;
    }
    ```

    当然，也可以请求一个 URI，例如

    <http://localhost:8080/ex/bars?id=100&second=something>

    的请求总是会被映射到最佳匹配项，即定义了 id 和第二个参数的较窄匹配项。

6. 请求映射拐角案例

    1. @RequestMapping - 映射到同一控制器方法的多个路径

        虽然单个 @RequestMapping 路径值通常用于单个控制器方法（只是良好实践，并非硬性规定），但在某些情况下，可能需要将多个请求映射到同一方法。

        在这种情况下，@RequestMapping 的 value 属性确实接受多个映射，而不仅仅是一个映射：

        ```java
        @RequestMapping(
        value = { "/ex/advanced/bars", "/ex/advanced/foos" }, 
        method = GET)
        @ResponseBody
        public String getFoosOrBarsByPath() {
            return "Advanced - Get some Foos or Bars";
        }
        ```

        现在，这两个 curl 命令都应该使用同一个方法：

        ```bash
        curl -i http://localhost:8080/ex/advanced/foos
        curl -i http://localhost:8080/ex/advanced/bars
        ```

    2. @RequestMapping - 将多个 HTTP 请求方法映射到同一个控制器方法

        使用不同 HTTP verb 的多个请求可以映射到同一个控制器方法：

        ```java
        @RequestMapping(
        value = "/ex/foos/multiple",
        method = { RequestMethod.PUT, RequestMethod.POST }
        )
        @ResponseBody
        public String putAndPostFoos() {
            return "Advanced - PUT and POST within single method";
        }
        ```

        通过 curl，这两个方法现在都会使用同一个方法：

        ```bash
        curl -i -X POST http://localhost:8080/ex/foos/multiple
        curl -i -X PUT http://localhost:8080/ex/foos/multiple
        ```

    3. @RequestMapping - 所有请求的回退

        要为使用特定 HTTP 方法的所有请求（例如 GET）实现一个简单的回退：

        ```java
        @RequestMapping(value = "*", method = RequestMethod.GET)
        @ResponseBody
        public String getFallback() {
            return "Fallback for GET Requests";
        }
        ```

        甚至是所有请求：

        ```java
        @RequestMapping(
        value = "*", 
        method = { RequestMethod.GET, RequestMethod.POST ... })
        @ResponseBody
        public String allFallback() {
            return "Fallback for All Requests";
        }
        ```

    4. 映射不明确错误

        当 Spring 对不同控制器方法的两个或多个请求映射进行相同评估时，就会出现映射不明确的错误。当一个请求映射具有相同的 HTTP 方法、URL、参数、标头和媒体类型时，该请求映射就是相同的。

        例如，这是一个模棱两可的映射：

        ```java
        @GetMapping(value = "foos/duplicate" )
        public String duplicate() {
            return "Duplicate";
        }

        @GetMapping(value = "foos/duplicate" )
        public String duplicateEx() {
            return "Duplicate";
        }
        ```

        抛出的异常通常会有如下错误信息：

        ```log
        Caused by: java.lang.IllegalStateException: Ambiguous mapping.
        Cannot map 'fooMappingExamplesController' method
        public java.lang.String org.baeldung.web.controller.FooMappingExamplesController.duplicateEx()
        to {[/ex/foos/duplicate],methods=[GET]}:
        There is already 'fooMappingExamplesController' bean method
        public java.lang.String org.baeldung.web.controller.FooMappingExamplesController.duplicate() mapped.
        ```

        仔细阅读错误信息后发现，Spring 无法映射 org.baeldung.web.controller.FooMappingExamplesController.duplicateEx() 方法，因为它与已映射的 org.baeldung.web.controller.FooMappingExamplesController.duplicate() 方法的映射冲突。

        下面的代码片段不会导致映射不明确的错误，因为这两个方法返回的内容类型不同：

        ```java
        @GetMapping(value = "foos/duplicate", produces = MediaType.APPLICATION_XML_VALUE)
        public String duplicateXml() {
            return "<message>Duplicate</message>";
        }

        @GetMapping(value = "foos/duplicate", produces = MediaType.APPLICATION_JSON_VALUE)
        public String duplicateJson() {
            return "{\"message\":\"Duplicate\"}";
        }
        ```

        这种差异允许我们的控制器根据请求中提供的 Accepts 头信息返回正确的数据表示。

        另一种解决方法是更新分配给两个相关方法中任何一个的 URL。

7. 新的请求映射快捷方式

    Spring Framework 4.3 引入了一些[新的 HTTP 映射注解](https://www.baeldung.com/spring-new-requestmapping-shortcuts)，它们都基于 @RequestMapping：

    - @GetMapping
    - @PostMapping
    - @PutMapping
    - @DeleteMapping
    - @PatchMapping

    这些新注解可以提高代码的可读性并减少代码的冗长度。

    让我们通过创建一个支持 CRUD 操作的 RESTful API 来看看这些新注解的作用：

    ```java
    @GetMapping("/{id}")
    public ResponseEntity<?> getBazz(@PathVariable String id){
        return new ResponseEntity<>(new Bazz(id, "Bazz"+id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> newBazz(@RequestParam("name") String name){
        return new ResponseEntity<>(new Bazz("5", name), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBazz(
    @PathVariable String id,
    @RequestParam("name") String name) {
        return new ResponseEntity<>(new Bazz(id, name), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBazz(@PathVariable String id){
        return new ResponseEntity<>(new Bazz(id), HttpStatus.OK);
    }
    ```

    有关这些内容的深入探讨，请参见[此处](https://www.baeldung.com/spring-new-requestmapping-shortcuts)。

8. Spring 配置

    Spring MVC 配置非常简单，因为我们的 FooController 定义在以下包中：

    ```java
    package org.baeldung.spring.web.controller;
    @Controller
    public class FooController { ... }
    ```

    我们只需要一个 @Configuration 类来启用完整的 MVC 支持并配置控制器的类路径扫描：

    ```java
    @Configuration
    @EnableWebMvc
    @ComponentScan({ "org.baeldung.spring.web.controller" })
        public class MvcConfig {
            //
    }
    ```

9. 结论

    本文重点介绍了 Spring 中的 @RequestMapping 注解，讨论了一个简单的用例、HTTP 头信息的映射、使用 @PathVariable 绑定 URI 的部分内容，以及如何使用 URI 参数和 @RequestParam 注解。

    如果您想了解如何在 Spring MVC 中使用另一个核心注解，可以点击[此处](https://www.baeldung.com/spring-mvc-and-the-modelattribute-annotation)了解 @ModelAttribute 注解。
