# [Spring Boot：自定义Jackson ObjectMapper](https://www.baeldung.com/spring-boot-customize-jackson-objectmapper)

1. 一览表

    使用JSON格式时，Spring Boot将使用ObjectMapper实例对响应进行序列化和反序列化请求。

    在本教程中，我们将看看配置序列化和反序列化选项的最常见方法。

    要了解更多关于Jackson的信息，请务必查看我们的[Jackson教程](https://www.baeldung.com/jackson)。

2. 默认配置

    默认情况下，Spring Boot配置将禁用以下内容：

    - MapperFeature.DEFAULT_VIEW_INCLUSION
    - DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
    - SerializationFeature.WRITE_DATES_AS_TIMESTAMPS

    让我们从一个简短的例子开始：

    - 客户会向我们的`/coffee?name=Lavazza?`发送GET请求吗
    - 控制器将返回一个新的咖啡对象。
    - Spring将使用ObjectMapper将我们的POJO序列化为JSON。

    我们将使用String和LocalDateTime对象来示例自定义选项：

    ```java
    public class Coffee {

        private String name;
        private String brand;
        private LocalDateTime date;

        //getters and setters
    }
    ```

    我们还将定义一个简单的REST控制器来演示序列化：

    ```java
    @GetMapping("/coffee")
    public Coffee getCoffee(@RequestParam(name = "brand", required = false) String brand,
        @RequestParam(name = "name", required = false) String name) {
        return new Coffee()
            .setBrand(brand)
            .setDate(FIXED_DATE)
            .setName(name);
    }
    ```

    默认情况下，这将是调用GET时的响应<http://lolcahost:8080/coffee?brand=Lavazza>：

    ```java
    {
        "name": null,
        "brand": "Lavazza",
        "date": "2020-11-16T10:21:35.974"
    }
    ```

    我们希望排除空值，并具有自定义日期格式（dd-MM-yyyy HH:mm）。这是我们最终的回应：

    ```java
    {
        "brand": "Lavazza",
        "date": "04-11-2020 10:34"
    }
    ```

    使用Spring Boot时，我们可以选择自定义默认ObjectMapper或覆盖它。我们将在下一节中介绍这两个选项。

3. 自定义默认对象映射器

    在本节中，我们将了解如何自定义Spring Boot使用的默认ObjectMapper。

    1. 应用程序属性和自定义杰克逊模块

        配置映射器最简单的方法是通过应用程序属性。

        以下是配置的一般结构：

        `spring.jackson.<category_name>.<feature_name>=true,false`

        例如，以下是我们将添加的内容，以禁用SerializationFeature.WRITE_DATES_AS_TIMESTAMPS：

        `spring.jackson.serialization.write-dates-as-timestamps=false`

        除了上述功能类别外，我们还可以配置属性包含：

        `spring.jackson.default-property-inclusion=always, non_null, non_absent, non_default, non_empty`

        配置环境变量是最简单的方法。这种方法的缺点是，我们无法自定义高级选项，例如为LocalDateTime设置自定义日期格式。

        此时，我们将得到以下结果：

        ```json
        {
            "brand": "Lavazza",
            "date": "2020-11-16T10:35:34.593"
        }
        ```

        为了实现我们的目标，我们将使用我们的自定义日期格式注册一个新的JavaTimeModule：

        ```java
        @Configuration
        @PropertySource("classpath:coffee.properties")
        public class CoffeeRegisterModuleConfig {
            @Bean
            public Module javaTimeModule() {
                JavaTimeModule module = new JavaTimeModule();
                module.addSerializer(LOCAL_DATETIME_SERIALIZER);
                return module;
            }
        }
        ```

        此外，配置属性文件coffee.properties将包含以下内容：

        `spring.jackson.default-property-inclusion=non_null`

        Spring Boot将自动注册任何类型的bean com.fasterxml.jackson.databind.Module。这是我们的最终结果：

        ```json
        {
            "brand": "Lavazza",
            "date": "16-11-2020 10:43"
        }
        ```

    2. Jackson2ObjectMapperBuilder定制器

        这个功能界面的目的是允许我们创建配置bean。

        它们将应用于通过Jackson2ObjectMapperBuilder创建的默认ObjectMapper：

        ```java
        @Bean
        public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
            return builder -> builder.serializationInclusion(JsonInclude.Include.NON_NULL)
            .serializers(LOCAL_DATETIME_SERIALIZER);
        }
        ```

        配置bean按特定顺序应用，我们可以使用@Order注释进行控制。如果我们想从不同的配置或模块配置ObjectMapper，这种优雅的方法是合适的。

4. 覆盖默认配置

    如果我们想要完全控制配置，有几个选项将禁用自动配置，并只允许应用我们的自定义配置。

    让我们仔细看看这些选项。

    1. 对象映射器

        覆盖默认配置的最简单方法是定义ObjectMapper bean并将其标记为@Primary：

        ```java
        @Bean
        @Primary
        public ObjectMapper objectMapper() {
            JavaTimeModule module = new JavaTimeModule();
            module.addSerializer(LOCAL_DATETIME_SERIALIZER);
            return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(module);
        }
        ```

        当我们想要完全控制序列化过程，并且不想允许外部配置时，我们应该使用这种方法。

    2. Jackson2ObjectMapperBuilder

        另一种干净的方法是定义Jackson2ObjectMapperBuilder bean。

        Spring Boot在构建ObjectMapper时实际上默认使用此构建器，并将自动拾取定义的构建器：

        ```java
        @Bean
        public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
            return new Jackson2ObjectMapperBuilder().serializers(LOCAL_DATETIME_SERIALIZER)
            .serializationInclusion(JsonInclude.Include.NON_NULL);
        }
        ```

        默认情况下，它将配置两个选项：

        - 禁用 MapperFeature.DEFAULT_VIEW_INCLUSION
        - 禁用 DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES

        根据Jackson2ObjectMapperBuilder[文档](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/converter/json/Jackson2ObjectMapperBuilder.html)，如果一些模块出现在类路径上，它也会注册它们：

        - jackson-datatype-jdk8：支持其他Java 8类型，如可选
        - jackson-datatype-jsr310：支持Java 8日期和时间API类型
        - jackson-datatype-joda：支持Joda-Time类型
        - jackson-module-kotlin：支持Kotlin类和数据类

        这种方法的优点是，Jackson2ObjectMapperBuilder提供了一种简单直观的构建ObjectMapper的方法。

    3. MappingJackson2Http消息转换器

        我们只需定义一个类型为MappingJackson2HttpMessageConverter的bean，Spring Boot将自动使用它：

        ```java
        @Bean
        public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().serializers(LOCAL_DATETIME_SERIALIZER)
            .serializationInclusion(JsonInclude.Include.NON_NULL);
            return new MappingJackson2HttpMessageConverter(builder.build());
        }
        ```

        请务必查看我们的Spring Http消息转换器[文章](https://www.baeldung.com/spring-httpmessageconverter-rest)以了解更多信息。

5. 测试配置

    为了测试我们的配置，我们将使用TestRestTemplate并将对象序列化为字符串。

    通过这种方式，我们可以验证我们的Coffee对象是否在没有空值的情况下进行序列化，并采用自定义日期格式：

    ```java
    @Test
    public void whenGetCoffee_thenSerializedWithDateAndNonNull() {
        String formattedDate = DateTimeFormatter.ofPattern(CoffeeConstants.dateTimeFormat).format(FIXED_DATE);
        String brand = "Lavazza";
        String url = "/coffee?brand=" + brand;

        String response = restTemplate.getForObject(url, String.class);
        
        assertThat(response).isEqualTo("{\"brand\":\"" + brand + "\",\"date\":\"" + formattedDate + "\"}");
    }
    ```

6. 结论

    在本文中，我们查看了使用Spring Boot时配置JSON序列化选项的几种方法。

    我们看到了两种不同的方法：配置默认选项或覆盖默认配置。
