# [Spring Webflux和CORS](https://www.baeldung.com/spring-webflux-cors)

1. 一览表

    在[上一篇文章](https://www.baeldung.com/spring-cors)中，我们了解了跨源资源共享（CORS）规范以及如何在Spring内使用它。

    在本快速教程中，我们将使用[Spring的5 WebFlux框架](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html)设置类似的CORS配置。

    首先，我们将看看如何在基于注释的API上启用该机制。

    然后，我们将分析如何在整个项目上将其作为全局配置启用，或使用特殊的WebFilter。

2. 在注释元素上启用CORS

    Spring提供@CrossOrigin注释，以启用控制器类和/或处理程序方法上的CORS请求。

    1. 在请求处理程序方法上使用@CrossOrigin

        让我们将此注释添加到我们的映射请求方法中：

        ```java
        @CrossOrigin
        @PutMapping("/cors-enabled-endpoint")
        public Mono<String> corsEnabledEndpoint() {
            // ...
        }
        ```

        我们将使用WebTestClient（正如我们在第4节中解释的那样，测试本[帖子](https://www.baeldung.com/spring-5-functional-web)）以分析我们从该端点获得的响应：

        ```java
        ResponseSpec response = webTestClient.put()
        .uri("/cors-enabled-endpoint")
        .header("Origin", "http://any-origin.com")
        .exchange();

        response.expectHeader()
        .valueEquals("Access-Control-Allow-Origin", "*");
        ```

        此外，我们可以尝试预检请求，以确保CORS配置按预期工作：

        ```java
        ResponseSpec response = webTestClient.options()
        .uri("/cors-enabled-endpoint")
        .header("Origin", "http://any-origin.com")
        .header("Access-Control-Request-Method", "PUT")
        .exchange();

        response.expectHeader()
        .valueEquals("Access-Control-Allow-Origin", "*");
        response.expectHeader()
        .valueEquals("Access-Control-Allow-Methods", "PUT");
        response.expectHeader()
        .exists("Access-Control-Max-Age");
        ```

        @CrossOrigin注释具有以下默认配置：

        - 允许所有来源（解释响应标头中的 ‘*’ 值）
        - 允许所有标题
        - 允许由处理程序方法映射的所有HTTP方法
        - 证书未启用
        - “max-age”值为1800秒（30分钟）

        然而，这些值中的任何一个都可以使用注释的参数被覆盖。

    2. 在控制器上使用@CrossOrigin

        类级也支持此注释，它将影响其所有方法。

        如果类级配置不适合我们所有的方法，我们可以对两个元素进行注释以获得预期结果：

        ```java
        @CrossOrigin(value = { "http://allowed-origin.com" },
        allowedHeaders = { "Baeldung-Allowed" },
        maxAge = 900
        )
        @RestController
        public class CorsOnClassController {

            @PutMapping("/cors-enabled-endpoint")
            public Mono<String> corsEnabledEndpoint() {
                // ...
            }

            @CrossOrigin({ "http://another-allowed-origin.com" })
            @PutMapping("/endpoint-with-extra-origin-allowed")
            public Mono<String> corsEnabledWithExtraAllowedOrigin() {
                // ...
            }

            // ...
        }
        ```

3. 在全局配置上启用CORS

    我们还可以通过覆盖aWebFluxConfigurer实现的addCorsMappings（）方法来定义全局CORS配置。

    此外，实现需要@EnableWebFlux注释才能在普通的Spring应用程序中导入Spring WebFlux配置。如果我们使用Spring Boot，那么我们只有在想要覆盖自动配置时才需要此注释：

    ```java
    @Configuration
    @EnableWebFlux
    public class CorsGlobalConfiguration implements WebFluxConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry corsRegistry) {
            corsRegistry.addMapping("/**")
            .allowedOrigins("http://allowed-origin.com")
            .allowedMethods("PUT")
            .maxAge(3600);
        }
    }
    ```

    因此，我们正在为该特定路径模式启用跨源请求处理。

    默认配置与@CrossOrigin相似，但只允许GET、HEAD和POST方法。

    我们还可以将此配置与本地配置相结合：

    - 对于多值(multiple-value)属性，生成的CORS配置将是每个规范的加法
    - 另一方面，对于单一值(single-value)，本地值将优先于全局值

    然而，使用这种方法对功能端点无效。

4. 使用WebFilter启用CORS

    在功能端点上启用CORS的最佳方法是使用WebFilter。

    正如我们在这篇[文章](https://www.baeldung.com/spring-webflux-filters)中看到的那样，我们可以使用WebFilters来修改请求和响应，同时保持端点的实现完好无损。

    Spring提供内置的CorsWebFilter，以便轻松处理跨源配置：

    ```java
    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://allowed-origin.com"));
        corsConfig.setMaxAge(8000L);
        corsConfig.addAllowedMethod("PUT");
        corsConfig.addAllowedHeader("Baeldung-Allowed");

        UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
    ```

    这对注释处理程序也很有效，但它不能与更精细的@CrossOrigin配置相结合。

    我们必须记住，CorsConfiguration没有默认配置。

    因此，除非我们指定所有相关属性，否则CORS的实现将非常具有限制性。

    设置默认值的简单方法是在对象上使用applyPermitDefaultValues（）方法。

5. 结论

    总之，我们通过非常简短的例子学习了如何在基于webflux的服务上启用CORS。

    我们看到了不同的方法，因此我们现在要做的就是分析哪种方法最适合我们的要求。
