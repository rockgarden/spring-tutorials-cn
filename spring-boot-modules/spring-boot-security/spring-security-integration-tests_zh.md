# [Spring Boot集成测试的Spring安全性](https://www.baeldung.com/spring-security-integration-tests)

1. 介绍

    无需独立集成环境即可执行集成测试的能力是任何软件堆栈的宝贵功能。Spring Boot与Spring Security的无缝集成使测试与安全层交互的组件变得简单。

    在本快速教程中，我们将探索使用@MockMvcTest和@SpringBootTest来执行启用安全的集成测试。

2. 依赖性

    让我们首先引入我们示例所需的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    ```

    spring-boot-starter-web、spring-boot-starter-security和spring-boot-starter-test启动器为我们提供了对Spring MVC、Spring Security和Spring Boot测试实用程序的访问权限。

    此外，我们将引入spring-security-test，以访问我们将要使用的@WithMockUser注释。

3. 网络安全配置

    我们的网络安全配置将很简单。只有经过身份验证的用户才能访问与`/private/**`匹配的路径。匹配`/public/**`的路径将适用于任何用户：

    ```java
    @Configuration
    public class WebSecurityConfigurer {

        @Bean
        public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
            UserDetails user = User.withUsername("spring")
                .password(passwordEncoder.encode("secret"))
                .roles("USER")
                .build();
            return new InMemoryUserDetailsManager(user);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(request -> request.requestMatchers(new AntPathRequestMatcher("/private/**"))
                    .hasRole("USER"))
                .authorizeHttpRequests(request -> request.requestMatchers(new AntPathRequestMatcher("/public/**"))
                    .permitAll())
                .httpBasic(Customizer.withDefaults())
                .build();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
    ```

4. 方法安全配置

    除了我们在WebSecurityConfigurer中定义的基于URL路径的安全性外，我们还可以通过提供额外的配置文件来配置基于方法的安全性：

    ```java
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class MethodSecurityConfigurer
    extends GlobalMethodSecurityConfiguration {
    }
    ```

    此配置支持Spring Security的 pre/post 注释。如果需要额外的支持，其他属性也可用。有关Spring Method Security的更多信息，请查看我们关于该主题的[文章](https://www.baeldung.com/spring-security-method-security)。

5. 使用@WebMvcTest测试控制器

    当将@WebMvcTest注释方法与Spring Security一起使用时，MockMvc会自动配置测试我们的安全配置所需的必要过滤器链。

    由于MockMvc是为我们配置的，因此我们可以使用@WithMockUser进行测试，而无需任何额外的配置：

    ```java
    @RunWith(SpringRunner.class)
    @WebMvcTest(SecuredController.class)
    public class SecuredControllerWebMvcIntegrationTest {

        @Autowired
        private MockMvc mvc;

        // ... other methods

        @WithMockUser(value = "spring")
        @Test
        public void givenAuthRequestOnPrivateService_shouldSucceedWith200() throws Exception {
            mvc.perform(get("/private/hello").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        }
    }
    ```

    请注意，使用@WebMvcTest将告诉Spring Boot只实例化Web层，而不是整个上下文。因此，使用@WebMvcTest的控制器测试将比其他方法运行得更快。

6. 使用@SpringBootTest测试控制器

    当使用@SpringBootTest注释使用Spring Security测试控制器时，在设置MockMvc时需要明确配置过滤器链。

    使用SecurityMockMvcConfigurer提供的静态springSecurity方法是首选的方法：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
    public class SecuredControllerSpringBootIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        private MockMvc mvc;

        @Before
        public void setup() {
            mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        }

        // ... other methods

        @WithMockUser("spring")
        @Test
        public void givenAuthRequestOnPrivateService_shouldSucceedWith200() throws Exception {
            mvc.perform(get("/private/hello").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        }
    }
    ```

7. 使用@SpringBootTest测试安全方法

    @SpringBootTest不需要任何额外的配置来测试安全方法。我们可以简单地直接调用方法，并根据需要使用@WithMockUser：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest
    public class SecuredMethodSpringBootIntegrationTest {

        @Autowired
        private SecuredService service;

        @Test(expected = AuthenticationCredentialsNotFoundException.class)
        public void givenUnauthenticated_whenCallService_thenThrowsException() {
            service.sayHelloSecured();
        }

        @WithMockUser(username="spring")
        @Test
        public void givenAuthenticated_whenCallServiceWithSecured_thenOk() {
            assertThat(service.sayHelloSecured()).isNotBlank();
        }
    }
    ```

8. 使用@SpringBootTest和TestRestTemplate进行测试

    在为安全REST端点编写集成测试时，TestRestTemplate是一个方便的选择。

    在请求安全端点之前，我们可以自动连接模板并设置凭据：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
    public class SecuredControllerRestTemplateIntegrationTest {

        @Autowired
        private TestRestTemplate template;

        // ... other methods

        @Test
        public void givenAuthRequestOnPrivateService_shouldSucceedWith200() throws Exception {
            ResponseEntity<String> result = template.withBasicAuth("spring", "secret")
            .getForEntity("/private/hello", String.class);
            assertEquals(HttpStatus.OK, result.getStatusCode());
        }
    }
    ```

    TestRestTemplate是灵活的，并提供了许多有用的安全相关选项。有关TestRestTemplate的更多详细信息，请查看我们关于该主题的[文章](https://www.baeldung.com/spring-boot-testresttemplate)。

9. 结论

    在本文中，我们研究了执行安全集成测试的几种方法。

    我们研究了如何使用MVC控制器和REST端点以及安全方法。
