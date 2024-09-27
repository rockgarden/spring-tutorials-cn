# [Spring Boot 安全性自动配置](https://www.baeldung.com/spring-boot-security-autoconfiguration)

1. 概述

    在本教程中，我们将了解 Spring Boot 对安全的看法。

    简而言之，我们将重点关注默认安全配置，以及如何在需要时禁用或自定义它。

2. 默认安全设置

    为了在 Spring Boot 应用程序中添加安全性，我们需要添加安全启动依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    ```

    这还将包括 SecurityAutoConfiguration 类，其中包含 initial/default 安全配置。

    请注意，我们在这里没有指定版本，因为我们假定项目已经使用 Boot 作为父版本。

    默认情况下，应用程序将启用身份验证。此外，内容协商用于确定应使用 basic 还是 formLogin。

    有一些预定义的属性：

    ```txt
    spring.security.user.name
    spring.security.user.password
    ```

    如果不使用预定义属性 spring.security.user.password 配置密码并启动应用程序，系统将随机生成默认密码并打印在控制台日志中：

    `Using default security password: c8be15de-4488-4490-9dc6-fab3f91435c6`

    有关更多默认值，请参阅 [Spring Boot 通用应用程序属性](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html)参考页面的安全属性部分。

3. 禁用自动配置

    要放弃安全自动配置并添加我们的配置，我们需要排除 SecurityAutoConfiguration 类。

    我们可以通过一个简单的排除来做到这一点：

    ```java
    @SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
    public class SpringBootSecurityApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringBootSecurityApplication.class, args);
        }
    }
    ```

    或者，我们可以在 application.properties 文件中添加一些配置：

    `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration`

    不过，在某些特殊情况下，这种设置并不足够。

    例如，几乎每个 Spring Boot 应用程序都是通过类路径中的 Actuator 启动的。这会造成问题，因为另一个自动配置类需要我们刚刚排除的那个类。因此，应用程序将无法启动。

    为了解决这个问题，我们需要排除该类；而且，针对 Actuator 的情况，我们还需要排除 ManagementWebSecurityAutoConfiguration。

    1. 禁用与超越安全自动配置

        禁用自动配置与超越自动配置之间有很大区别。

        禁用自动配置就像从头开始添加 Spring Security 依赖关系和整个设置。这在几种情况下都很有用：

        - 将应用程序安全与自定义安全提供程序整合
        - 将已存在安全设置的遗留 Spring 应用程序迁移到 Spring Boot

        但大多数情况下，我们并不需要完全禁用安全自动配置。

        这是因为 Spring Boot 的配置允许我们通过添加新的/自定义配置类来超越自动配置的安全性。这通常比较简单，因为我们只是自定义现有的安全设置，以满足我们的需求。

4. 配置 Spring Boot 安全性

    如果我们选择了禁用安全自动配置的路径，我们自然需要提供自己的配置。

    正如我们之前所讨论的，这是默认的安全配置。然后，我们通过修改属性文件对其进行自定义。

    例如，我们可以通过添加自己的密码来覆盖默认密码：

    `spring.security.user.password=password`

    如果我们想要更灵活的配置，例如多用户和多角色配置，我们需要使用完整的 @Configuration 类：

    ```java
    @Configuration
    @EnableWebSecurity
    public class BasicConfiguration {

        @Bean
        public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
            UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();

            UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("USER", "ADMIN")
                .build();

            return new InMemoryUserDetailsManager(user, admin);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(request -> request.anyRequest()
                    .authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            return encoder;
        }
    }
    ```

    如果我们禁用默认安全配置，则 @EnableWebSecurity 注解至关重要。

    如果缺少它，应用程序将无法启动。

    另外，请注意，在使用 Spring Boot 2 时，我们需要使用 PasswordEncoder 来设置密码。有关详细信息，请参阅我们的 Spring Security 5 默认密码编码器[指南](https://www.baeldung.com/spring-security-5-default-password-encoder)。

    现在，我们应该通过几个快速的实时测试来验证我们的安全配置是否正确应用：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = RANDOM_PORT)
    public class BasicConfigurationIntegrationTest {

        TestRestTemplate restTemplate;
        URL base;
        @LocalServerPort int port;

        @Before
        public void setUp() throws MalformedURLException {
            restTemplate = new TestRestTemplate("user", "password");
            base = new URL("http://localhost:" + port);
        }

        @Test
        public void whenLoggedUserRequestsHomePage_ThenSuccess()
        throws IllegalStateException, IOException {
            ResponseEntity<String> response =
            restTemplate.getForEntity(base.toString(), String.class);
    
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("Baeldung"));
        }

        @Test
        public void whenUserWithWrongCredentials_thenUnauthorizedPage() 
        throws Exception {
    
            restTemplate = new TestRestTemplate("user", "wrongpassword");
            ResponseEntity<String> response =
            restTemplate.getForEntity(base.toString(), String.class);
    
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertTrue(response.getBody().contains("Unauthorized"));
        }
    }
    ```

    事实上，Spring Security 背后是 Spring Boot Security，因此可以通过 Spring Boot Security 完成的任何安全配置或 Spring Boot Security 支持的任何集成也都可以在 Spring Boot 中实现。

5. Spring Boot OAuth2 自动配置（使用传统堆栈）

    Spring Boot 有专门的 OAuth2 自动配置支持。

    Spring Boot 1.x 附带的 Spring Security OAuth 支持在以后的启动版本中被移除，取而代之的是 [Spring Security 5](https://docs.spring.io/spring-security/site/docs/5.2.12.RELEASE/reference/html/oauth2.html) 捆绑的一流 OAuth 支持。我们将在下一节了解如何使用。

    对于传统堆栈（使用 Spring Security OAuth），我们首先需要添加一个 Maven 依赖项来开始设置应用程序：

    ```xml
    <dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
    </dependency>
    ```

    该依赖关系包括一组能够触发 OAuth2AutoConfiguration 类中定义的自动配置机制的类。

    现在，根据应用程序的范围，我们有多种选择可以继续。

    1. OAuth2 授权服务器自动配置

        如果我们想让应用程序成为 OAuth2 提供者，可以使用 @EnableAuthorizationServer。

        启动时，我们会在日志中注意到自动配置类将为我们的授权服务器生成一个客户端 ID 和一个客户端密文，当然还有一个用于基本身份验证的随机密码：

        ```properties
        Using default security password: a81cb256-f243-40c0-a585-81ce1b952a98
        security.oauth2.client.client-id = 39d2835b-1f87-4a77-9798-e2975f36972e
        security.oauth2.client.client-secret = f1463f8b-0791-46fe-9269-521b86c55b71
        ```

        这些凭证可用于获取访问令牌：

        ```bash
        curl -X POST -u 39d2835b-1f87-4a77-9798-e2975f36972e:f1463f8b-0791-46fe-9269-521b86c55b71 \
            -d grant_type=client_credentials
            -d username=user
            -d password=a81cb256-f243-40c0-a585-81ce1b952a98 \
            -d scope=write  http://localhost:8080/oauth/token
        ```

        我们的另一篇[文章](https://www.baeldung.com/rest-api-spring-oauth2-angular-legacy#server)提供了更多相关细节。

    2. 其他 Spring Boot OAuth2 自动配置设置

        Spring Boot OAuth2 还涵盖其他一些用例：

        - [Resource Server](https://www.baeldung.com/rest-api-spring-oauth2-angular-legacy#resource) - @EnableResourceServer
        - [Client Application](https://www.baeldung.com/sso-spring-security-oauth2-legacy) - @EnableOAuth2Sso 或 @EnableOAuth2Client

        如果我们需要我们的应用程序成为这些类型中的一种，我们只需在应用程序属性中添加一些配置即可，详情请参阅链接。

        所有 OAuth2 特定属性均可在 [Spring Boot 通用应用程序属性](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html) 中找到。

6. Spring Boot OAuth2 自动配置（使用新堆栈）

    要使用新堆栈，我们需要根据要配置的内容（授权服务器、资源服务器或客户端应用程序）添加依赖项。

    让我们逐一查看。

    1. OAuth2 授权服务器支持

        正如我们所看到的，Spring Security OAuth 栈提供了将授权服务器设置为 Spring 应用程序的可能性。但该项目已被弃用，Spring 目前不支持自己的授权服务器。相反，建议使用现有的成熟提供商，如 Okta、Keycloak 和 ForgeRock。

        不过，Spring Boot 可以让我们轻松配置这些提供商。有关 Keycloak 配置的示例，我们可以参考《[在 Spring Boot 中使用 Keycloak 的快速指南](https://www.baeldung.com/spring-boot-keycloak)》（A Quick Guide to Using Keycloak With Spring Boot）或《[在 Spring Boot 应用程序中嵌入 Keycloak](https://www.baeldung.com/keycloak-embedded-in-spring-boot-app)》（Keycloak Embedded in a Spring Boot Application）。

    2. OAuth2 资源服务器支持

        要包含对资源服务器的支持，我们需要添加此依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>    
        </dependency>
        ```

        此外，在安全配置中，我们需要包含 [oauth2ResourceServer()](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) DSL：

        ```java
        @Configuration
        public class JWTSecurityConfig {
            @Bean
            public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                ...
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
                ...
            }
        }
        ```

        我们的《[使用 Spring Security 5 的 OAuth 2.0 资源服务器](https://www.baeldung.com/spring-security-oauth-resource-server)》对此主题进行了深入探讨。

    3. OAuth2 客户端支持

        与我们配置资源服务器的方式类似，客户端应用程序也需要其依赖项和 DSL。

        下面是 OAuth2 客户端支持的具体依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
        </dependency>
        ```

        Spring Security 5 还通过其 [oath2Login()](https://www.baeldung.com/spring-security-5-oauth2-login) DSL 提供了一流的登录支持。

        有关新堆栈中 SSO 支持的详细信息，请参阅我们的文章《[使用 Spring Security OAuth2 实现简单单点登录](https://www.baeldung.com/sso-spring-security-oauth2)》。

7. 总结

    在本文中，我们重点介绍了 Spring Boot 提供的默认安全配置。我们了解了如何禁用或覆盖安全自动配置机制。然后，我们了解了如何应用新的安全配置。

    OAuth2 的源代码可以在我们的 OAuth2 GitHub 代码库中找到，包括[传统](https://github.com/Baeldung/spring-security-oauth/tree/master/oauth-legacy)堆栈和[新](https://github.com/Baeldung/spring-security-oauth/tree/master/oauth-rest)堆栈。
