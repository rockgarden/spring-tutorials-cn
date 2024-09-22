# [如何在Spring MVC中设置JSON内容类型](https://www.baeldung.com/spring-mvc-set-json-content-type)

1. 介绍

    内容类型表示如何解释请求/响应中存在的数据。每当控制器收到网络请求时，它都会消耗或生成一些媒体类型。在这个请求响应模型中，可以消费/生成几种媒体类型，JSON就是其中之一。

    在本快速教程中，我们将探索使用spring boot在Spring MVC中设置内容类型的不同方法。

2. @Spring的RequestMapping

    简单地说，@RequestMapping是一个重要的注释，将Web请求映射到Spring控制器。它具有各种属性，包括HTTP方法、请求参数、标头和媒体类型。

    一般来说，媒体类型分为两类：消耗品和可生产。除此之外，我们还可以在Spring中定义自定义媒体类型。主要目的是将主要映射限制在我们的请求处理程序的媒体类型列表中。

    1. 消耗型媒体类型

        使用consumes属性，我们可以指定控制器将从客户端接受的媒体类型。我们也可以提供一份媒体类型的列表。让我们定义一个简单的端点：

        ```java
        @RequestMapping(value = "/greetings", method = RequestMethod.POST, consumes="application/json")
        public void addGreeting(@RequestBody ContentType type, Model model) {
            // code here
        }
        ```

        如果客户端指定了无法按资源消耗的媒体类型，系统将生成HTTP“415 Unsupported Media Type”错误。

    2. 可产媒体类型

        与consume属性相反，produce指定了资源可以生成并发回给客户端的媒体类型。毫无疑问，我们可以使用一个选项列表。如果资源无法生成请求的资源，系统将生成HTTP“406 Not Acceptable”错误。

        让我们从一个API公开JSON字符串的简单示例开始。

        这是我们的终点：

        ```java
        @GetMapping(
        value = "/greetings-with-response-body", 
        produces="application/json"
        ) 
        public String getGreetingWhileReturnTypeIsString() { 
            return "{\"test\": \"Hello\"}";
        }
        ```

        我们将使用CURL来测试这个：

        ```bash
        curl http://localhost:8080/greetings-with-response-body
        ```

        上述命令产生响应：

        ```log
        { "test": "Hello" }
        ```

3. Rest Controllers With Spring Boot

    如果我们使用 Spring Boot 来处理休眠控制器，那么通过一个注解就可以处理多种事情，让我们的生活变得更加轻松。@RestController 注解将 @Controller 和 @ResponseBody 注解合二为一。这将应用于该类中定义的所有端点。

    1. 使用@RestController注释

        Jackson ObjectMapper类从字符串、流或文件解析JSON。如果Jackson在类路径上，Spring应用程序中的任何控制器都会默认呈现JSON响应。

        我们将添加一个单元测试来验证响应：

        ```java
        @Test
        public void givenReturnTypeIsString_whenJacksonOnClasspath_thenDefaultContentTypeIsJSON()
        throws Exception {

            // Given
            String expectedMimeType = "application/json";
            
            // Then
            String actualMimeType = this.mockMvc.perform(MockMvcRequestBuilders.get("/greetings-with-response-body", 1))
            .andReturn().getResponse().getContentType();

            Assert.assertEquals(expectedMimeType, actualMimeType);
        }
        ```

    2. 使用响应实体

        与@ResponseBody相反，ResponseEntity是一种代表整个HTTP响应的通用类型。因此，我们可以控制任何进入其中的东西：状态代码、标题和正文。

        让我们定义一个新的端点：

        ```java
        @GetMapping(
        value = "/greetings-with-response-entity",
        produces = "application/json"
        )
        public ResponseEntity<String> getGreetingWithResponseEntity() {
            final HttpHeaders httpHeaders= new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<String>("{\"test\": \"Hello with ResponseEntity\"}", httpHeaders, HttpStatus.OK);
        }
        ```

        在我们浏览器的开发人员控制台中，我们可以看到以下响应：

        ```log
        {"test": "Hello with ResponseEntity"}
        ```

        我们将使用测试用例来验证响应的内容类型：

        ```java
        @Test
        public void givenReturnTypeIsResponseEntity_thenDefaultContentTypeIsJSON() throws Exception {

            // Given
            String expectedMimeType = "application/json";
            
            // Then
            String actualMimeType = this.mockMvc.perform(MockMvcRequestBuilders.get("/greetings-with-response-entity", 1))
            .andReturn().getResponse().getContentType();

            Assert.assertEquals(expectedMimeType, actualMimeType);
        }
        ```

    3. 使用`Map<String，Object>`返回类型

        最后但并非最不重要的是，我们还可以通过将返回类型从字符串更改为映射来设置内容类型。此Map返回类型需要编排，并返回一个JSON对象。

        这是我们的新终点：

        ```java
        @GetMapping(
        value = "/greetings-with-map-return-type",
        produces = "application/json"
        )
        public Map<String, Object> getGreetingWhileReturnTypeIsMap() {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("test", "Hello from map");
            return map;
        }
        ```

        让我们看看这个在行动中：

        ```bash
        curl <http://localhost:8080/greetings-with-map-return-type>
        ```

        curl命令返回JSON响应：

        ```log
        { "test": "Hello from map" }
        ```

4. 结论

    在本文中，我们学习了如何使用Spring boot设置Spring MVC中的内容类型，首先是类路径中的默认Json映射器，然后使用ResponseEntity，最后将返回类型从String更改为Map。
