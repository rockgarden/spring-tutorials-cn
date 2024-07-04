# [测试 Spring OAuth2 访问控制](https://www.baeldung.com/spring-oauth-testing-access-control)

1. 概述

    在本教程中，我们将探讨在具有 OAuth2 安全性的 Spring 应用程序中使用模拟身份测试访问控制规则的选项。

    我们将使用 Spring-security-test 和 [spring-addons](https://github.com/ch4mpy/spring-addons) 中的 MockMvc 请求后处理器、WebTestClient 突变器(mutators)和测试注解。

2. 为什么使用 Spring-附加组件？

    在 OAuth2 领域，spring-security-test 只提供请求后处理器和突变器，它们分别需要 MockMvc 或 WebTestClient 请求的上下文。这对 @Controllers 来说还不错，但要测试 @Service 或 @Repository 上的方法安全性（@PreAuthorize、@PostFilter 等）就有问题了。

    使用 @WithJwt 或 @WithOidcLogin 等注解，我们可以在 servlet 和反应式应用程序中对任何类型的 @Component 进行单元测试时模拟安全上下文。这就是为什么我们会在部分测试中使用 spring-addons-oauth2-test：它为大多数 Spring OAuth2 身份验证实现提供了此类注解。

3. 我们要测试什么？

    配套的 GitHub 资源库包含两个[资源服务器](https://www.baeldung.com/spring-security-oauth-resource-server)，它们共享以下功能：

    - 使用 JWT 解码器（而非不透明令牌内省）确保安全
    - 需要 ROLE_AUTHORIZED_PERSONNEL 授权才能访问 /secured-route 和 /secured-method
    - 如果身份验证缺失或无效（过期、发证人错误等），则返回 401；如果拒绝访问（角色缺失），则返回 403
    - 使用 Java 配置（对 servlet 和反应式应用程序分别使用 requestMatcher 和 pathMatcher）和方法安全性定义访问控制
    - 使用安全上下文中的身份验证数据构建响应有效载荷

    为了说明 servlet 和反应式测试 API 之间的细微差别，一个是 servlet（[代码](/servlet-resource-server/src/main/java/com/baeldung/ServletResourceServerApplication.java)），另一个是反应式应用程序（[代码](/reactive-resource-server/src/main/java/com/baeldung/ReactiveResourceServerApplication.java)）。

    在本文中，我们将重点测试单元测试和集成测试中的访问控制规则，并根据模拟的用户身份断言响应的 HTTP 状态与预期相符，或者在单元测试 @Component 而非 @Controller 时抛出异常，例如使用 @PreAuthorize 和 @PostFilter 等方法确保 @Service 或 @Repository 安全。

    在没有授权服务器的情况下，所有测试都能通过，但如果我们要启动被测资源服务器并使用 Postman 等工具进行查询，就需要有一个授权服务器。我们提供了一个本地 Keycloak 实例的 Docker Compose 文件，可以快速上手：

    - 管理控制台可从以下地址获取： <http://localhost:8080/admin/master/console/#/baeldung>
    - 管理员账户为 admin / admin
    - 已经创建了一个 baeldung realm，其中包含一个保密客户端（baeldung_confidential / secret）和两个用户（authorized 和 forbidden，均以 secret 作为密文）；

4. 使用模拟身份验证进行单元测试

    所谓 "单元测试"，我们指的是在独立于任何其他依赖关系（我们将对其进行模拟）的情况下对单个 @Component 进行测试。被测试的 @Component 可以是 @WebMvcTest 或 @WebFluxTest 中的 @Controller，也可以是普通 JUnit 测试中任何其他安全的 @Service、@Repository 等。

    MockMvc 和 WebTestClient 会忽略授权头，因此无需提供有效的访问令牌。当然，我们可以实例化或模拟任何认证实现，并在每次测试开始时手动创建安全上下文，但这样做太繁琐了。相反，我们将使用 spring-security-test MockMvc 请求后处理器、WebTestClient 突变器或 spring-addons 注释，用我们选择的模拟身份验证实例填充测试安全上下文。

    我们将使用 @WithMockUser 来查看它是否构建了一个 UsernamePasswordAuthenticationToken 实例，这经常是一个问题，因为 OAuth2 运行时配置在安全上下文中放置了其他类型的身份验证：

    - JwtAuthenticationToken 用于带有 JWT 解码器的资源服务器
    - 带有访问令牌内省（opaqueToken）的资源服务器的 BearerTokenAuthentication
    - 用于使用 oauth2Login 的客户端的 OAuth2AuthenticationToken
    - 如果我们决定在自定义身份验证转换器中返回另一个身份验证实例，而不是 Spring 默认的身份验证实例，那就完全没问题了。因此，从技术上讲，OAuth2 身份验证转换器可以返回一个 UsernamePasswordAuthenticationToken 实例，并在测试中使用 @WithMockUser，但这是一个非常不自然的选择，我们不会在此使用。

    1. 重要说明

        MockMvc 预处理器和 WebTestClient 突变器不会使用安全配置中定义的 Bean 来构建测试的身份验证实例。因此，使用 SecurityMockMvcRequestPostProcessors.jwt() 或 SecurityMockServerConfigurers.mockJwt() 定义 OAuth2 声明不会对身份验证名称和授权产生任何影响。我们必须使用专用方法自行设置名称和权限。

        相反，spring-addons 注释背后的工厂会扫描测试上下文以查找身份验证转换器，如果找到了就会使用它。因此，在使用 @WithJwt 时，重要的是将任何自定义的 JwtAuthenticationConverter 作为 bean 公开（而不是在 security conf 中将其作为 lambda 内联）：

        ```java
        @Configuration
        @EnableMethodSecurity
        @EnableWebSecurity
        static class SecurityConf {
            @Bean
            SecurityFilterChain filterChain(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> authenticationConverter) throws Exception {
                http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwtResourceServer -> jwtResourceServer.jwtAuthenticationConverter(authenticationConverter)));
                ...
            }

            @Bean
            JwtAuthenticationConverter authenticationConverter(Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter) {
                final var authenticationConverter = new JwtAuthenticationConverter();
                authenticationConverter.setPrincipalClaimName(StandardClaimNames.PREFERRED_USERNAME);
                authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
                return authenticationConverter;
            }
        }
        ```

        值得注意的是，身份验证转换器是作为 @Bean 公开的，它被明确注入到安全过滤链中。这样，@WithJwt 背后的工厂就可以使用它来根据请求构建身份验证，这与运行时使用真实令牌的方式完全相同。

        还要注意的是，在高级情况下，如果身份验证转换器返回的不是 JwtAuthenticationToken（或带有令牌内省功能的资源服务器中的 BearerTokenAuthentication），则只有 Spring 附加组件的测试注解才会构建预期的身份验证类型。

    2. 测试设置

        对于 @Controller 单元测试，对于 servlet 应用程序，我们应该用 @WebMvcTest 来装饰测试类；对于反应式应用程序，我们应该用 @WebFluxTest 来装饰测试类。

        Spring 会自动为我们提供 MockMvc 或 WebTestClient，在编写控制器单元测试时，我们将模拟 MessageService。

        在 servlet 应用程序中，一个空的 @Controller 单元测试就是这样的：

        ```java
        @WebMvcTest(controllers = GreetingController.class)
        class GreetingControllerTest {

            @MockBean
            MessageService messageService;

            @Autowired
            MockMvc mockMvc;

            //...
        }
        ```

        在反应式应用程序中，这是空的 @Controller 单元测试：

        ```java
        @WebFluxTest(controllers = GreetingController.class)
        class GreetingControllerTest {
            private static final AnonymousAuthenticationToken ANONYMOUS = 
            new AnonymousAuthenticationToken("anonymous", "anonymousUser", 
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

            @MockBean
            MessageService messageService;

            @Autowired
            WebTestClient webTestClient;

            //...
        }
        ```

        现在，让我们看看如何断言 HTTP 状态代码符合我们之前设定的规范。

    3. 使用 MockMvc 后处理器进行单元测试

        为了用 JwtAuthenticationToken 填充测试安全上下文（这是使用 JWT 解码器的资源服务器的默认验证类型），我们将使用 MockMvc 请求的 jwt 后处理器：

        `import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;`

        让我们来看看几个使用 MockMvc 的请求示例，以及根据端点和模拟身份验证对响应状态的断言：

        ```java
        @Test
        void givenRequestIsAnonymous_whenGetGreet_thenUnauthorized() throws Exception {
            mockMvc.perform(get("/greet").with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized());
        }
        ```

        在上文中，我们确保匿名请求无法获得问候语，并正确返回 401。

        现在，让我们来看看如何根据端点安全规则和分配给测试 JwtAuthenticationToken 的权限，对请求返回 200 Ok 或 403 Forbidden：

        ```java
        @Test
        void givenUserIsGrantedWithRoleAuthorizedPersonnel_whenGetSecuredRoute_thenOk() throws Exception {
            var secret = "Secret!";
            when(messageService.getSecret()).thenReturn(secret);

            mockMvc.perform(get("/secured-route").with(SecurityMockMvcRequestPostProcessors.jwt()
            .authorities(new SimpleGrantedAuthority("ROLE_AUTHORIZED_PERSONNEL"))))
                .andExpect(status().isOk())
                .andExpect(content().string(secret));
        }

        @Test
        void givenUserIsNotGrantedWithRoleAuthorizedPersonnel_whenGetSecuredRoute_thenForbidden() throws Exception {
            mockMvc.perform(get("/secured-route").with(SecurityMockMvcRequestPostProcessors.jwt()
            .authorities(new SimpleGrantedAuthority("admin"))))
                .andExpect(status().isForbidden());
        }
        ```

    4. 使用 WebTestClient 互变器进行单元测试

        在反应式资源服务器中，安全上下文中的身份验证类型与 servlet 中的相同： JwtAuthenticationToken。因此，我们将为 WebTestClient 使用 mockJwt 突变体：

        `import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;`

        与 MockMvc 后处理器不同，WebTestClient 没有匿名突变器。不过，您可以轻松定义匿名身份验证实例，并通过通用的 mockAuthentication 突变子使用它：

        ```java
        private static final AnonymousAuthenticationToken ANONYMOUS = new AnonymousAuthenticationToken(
            "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        @Test
        void givenRequestIsAnonymous_whenGetGreet_thenUnauthorized() throws Exception {
            webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(ANONYMOUS))
                .get()
                .uri("/greet")
                .exchange()
                .expectStatus()
                .isUnauthorized();
        }

        @Test
        void givenUserIsAuthenticated_whenGetGreet_thenOk() throws Exception {
            var greeting = "Whatever the service returns";
            when(messageService.greet()).thenReturn(Mono.just(greeting));

            webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(List.of(
            new SimpleGrantedAuthority("admin"), 
            new SimpleGrantedAuthority("ROLE_AUTHORIZED_PERSONNEL")))
                .jwt(jwt -> jwt.claim(StandardClaimNames.PREFERRED_USERNAME, "ch4mpy")))
                .get()
                .uri("/greet")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(greeting);

            verify(messageService, times(1)).greet();
        }

        @Test
        void givenUserIsNotGrantedWithRoleAuthorizedPersonnel_whenGetSecuredRoute_thenForbidden() throws Exception {
            webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("admin")))
                .get()
                .uri("/secured-route")
                .exchange()
                .expectStatus()
                .isForbidden();
        }
        ```

    5. 使用 Spring-Addons 的注解对控制器进行单元测试

        我们可以在 servlet 和反应式应用程序中以完全相同的方式使用测试注解。

        我们只需在 spring-addons-oauth2-test 上添加一个依赖项：

        ```xml
        <dependency>
            <groupId>com.c4-soft.springaddons</groupId>
            <artifactId>spring-addons-oauth2-test</artifactId>
            <version>7.6.12</version>
            <scope>test</scope>
        </dependency>
        ```

        该库带有大量注释，涵盖以下用例：

        - 在测试基于角色的访问控制时，@WithMockAuthentication 已足够频繁：它的目的是将权限作为参数，但也接受用户名和实现类型，以模拟 Authentication 和 Principal。
        - @WithJwt 用于测试带有 JWT 解码器的资源服务器。它依赖于一个身份验证工厂，该工厂从安全配置中选取 `Converter<Jwt，? extends AbstractAthenticationToken>`（或在反应式应用程序中选取 `Converter<Jwt，? extends Mono<? extends AbstractAuthenticationToken>>`），并在测试类路径上选取 JSON 有效负载。这样，我们就能完整地处理请求，并为相同的 JWT 有效负载提供与运行时相同的身份验证实例。
        - @WithOpaqueToken 的工作原理与 @WithJwt 相同，但针对的是具有令牌内省功能的资源服务器：它依赖于选择 OpaqueTokenAuthenticationConverter（或 ReactiveOpaqueTokenAuthenticationConverter）的工厂。
        - 当我们要测试的是带有登录功能的 OAuth2 客户端时，@WithOAuth2Login 和 @WithOidcLogin 将是我们的选择。

        在开始测试之前，我们将定义一些 JSON 文件作为测试资源。其目的是模拟代表性用户（角色或人物）访问令牌的 JSON 有效负载（或自省响应）。我们可以使用 <https://jwt.io> 等工具复制真实令牌的有效载荷。

        Ch4mpy 将是我们的测试用户，具有 AUTHORIZED_PERONNEL 角色：

        ```json
        {
        "iss": "https://localhost:8443/realms/master",
        "sub": "281c4558-550c-413b-9972-2d2e5bde6b9b",
        "iat": 1695992542,
        "exp": 1695992642,
        "preferred_username": "ch4mpy",
        "realm_access": {
            "roles": [
            "admin",
            "ROLE_AUTHORIZED_PERSONNEL"
            ]
        },
        "email": "ch4mp@c4-soft.com",
        "scope": "openid email"
        }
        ```

        然后，我们将定义第二个不带 AUTHORIZED_PERONNEL 角色的用户：

        ```json
        {
        "iss": "https://localhost:8443/realms/master",
        "sub": "2d2e5bde6b9b-550c-413b-9972-281c4558",
        "iat": 1695992551,
        "exp": 1695992651,
        "preferred_username": "tonton-pirate",
        "realm_access": {
            "roles": [
            "uncle",
            "skipper"
            ]
        },
        "email": "tonton-pirate@c4-soft.com",
        "scope": "openid email"
        }
        ```

        现在，我们可以从测试体中移除身份模拟，用注解来装饰测试方法。出于演示目的，我们将同时使用 @WithMockAuthentication 和 @WithJwt，但在实际测试中使用一个就足够了。当我们只需要定义权限或名称时，我们可能会选择第一种方法，而当我们需要掌握许多声明时，我们可能会选择第二种方法：

        ```java
        @Test
        @WithAnonymousUser
        void givenRequestIsAnonymous_whenGetSecuredMethod_thenUnauthorized() throws Exception {
            api.perform(get("/secured-method"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockAuthentication({ "admin", "ROLE_AUTHORIZED_PERSONNEL" })
        void givenUserIsGrantedWithRoleAuthorizedPersonnel_whenGetSecuredMethod_thenOk() throws Exception {
            final var secret = "Secret!";
            when(messageService.getSecret()).thenReturn(secret);

            api.perform(get("/secured-method"))
                .andExpect(status().isOk())
                .andExpect(content().string(secret));
        }

        @Test
        @WithMockAuthentication({ "admin" })
        void givenUserIsNotGrantedWithRoleAuthorizedPersonnel_whenGetSecuredMethod_thenForbidden() throws Exception {
            api.perform(get("/secured-method"))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithJwt("ch4mpy.json")
        void givenUserIsCh4mpy_whenGetSecuredMethod_thenOk() throws Exception {
            final var secret = "Secret!";
            when(messageService.getSecret()).thenReturn(secret);

            api.perform(get("/secured-method"))
                .andExpect(status().isOk())
                .andExpect(content().string(secret));
        }

        @Test
        @WithJwt("tonton-pirate.json")
        void givenUserIsTontonPirate_whenGetSecuredMethod_thenForbidden() throws Exception {
            api.perform(get("/secured-method"))
                .andExpect(status().isForbidden());
        }
        ```

        注解无疑非常适合 BDD 范式：

        - 先决条件（Given）在文本上下文中（注释装饰测试）
        - 测试体中只有测试代码执行（When）和结果断言（Then）。

    6. 对 @Service 或 @Repository 安全方法进行单元测试

        测试 @Controller 时，在请求 MockMvc 后处理器（或 WebTestClient 突变器）和注解之间做出选择主要是团队偏好的问题，但要对 MessageService::getSecret 访问控制进行单元测试，spring-security-test 不再是一个选项，我们需要 spring-addons 注释。

        下面是 JUnit 设置：

        - 使用 @ExtendWith(SpringExtension.class) 激活 Spring 自动布线功能
        - 导入并自动连接 MessageService 以获得一个仪器实例
        - 如果使用 @WithJwt，我们需要导入包含 JwtAuthenticationConverter 和 AuthenticationFactoriesTestConf 的配置。否则，用 @EnableMethodSecurity 装饰测试就足够了。

        每次用户缺少 ROLE_AUTHORIZED_PERSONNEL 授权时，我们都会断言 MessageService 引发异常。

        下面是 servlet 应用程序中 @Service 的完整单元测试：

        ![MessageServiceUnitTest](/servlet-resource-server/src/test/java/com/baeldung/MessageServiceUnitTest.java)

        反应式应用程序中 @Service 的单元测试也没什么不同：

        ![MessageServiceUnitTest](/reactive-resource-server/src/test/java/com/baeldung/MessageServiceUnitTest.java)

    7. JUnit 5 @参数化测试

        JUnit 5 允许定义多次运行参数值不同的测试。该参数可以是放入安全上下文的模拟身份验证。

        @WithMockAuthentication 可独立于 Spring 上下文构建身份验证实例，因此在参数化测试中使用非常方便：

        ```java
        @ParameterizedTest
        @AuthenticationSource({
                @WithMockAuthentication(authorities = { "admin", "ROLE_AUTHORIZED_PERSONNEL" }, name = "ch4mpy"),
                @WithMockAuthentication(authorities = { "uncle", "PIRATE" }, name = "tonton-pirate") })
        void givenUserIsAuthenticated_whenGetGreet_thenOk(@ParameterizedAuthentication Authentication auth) throws Exception {
            final var greeting = "Whatever the service returns";
            when(messageService.greet()).thenReturn(greeting);

            api.perform(get("/greet"))
                .andExpect(status().isOk())
                .andExpect(content().string(greeting));

            verify(messageService, times(1)).greet();
        }
        ```

        上述代码需要注意以下几点：

        - 使用 @ParameterizedTest 代替 @Test
        - 用 @AuthenticationSource 装饰测试，其中包含一个要使用的 @WithMockAuthentication 的数组
        - 为测试方法添加 @ParameterizedAuthentication 参数

        因为 @WithJwt 使用应用程序上下文中的 bean 来构建身份验证实例，所以我们还需要做一些工作：

        ```java
        @TestInstance(Lifecycle.PER_CLASS)
        class MessageServiceUnitTest {

            @Autowired
            WithJwt.AuthenticationFactory authFactory;

            private Stream<AbstractAuthenticationToken> allIdentities() {
                final var authentications = authFactory.authenticationsFrom("ch4mpy.json", "tonton-pirate.json").toList();
                return authentications.stream();
            }

            @ParameterizedTest
            @MethodSource("allIdentities")
            void givenUserIsAuthenticated_whenGreet_thenReturnGreetingWithPreferredUsernameAndAuthorities(@ParameterizedAuthentication Authentication auth) {
                final var jwt = (JwtAuthenticationToken) auth;
                final var expected = "Hello %s! You are granted with %s.".formatted(jwt.getTokenAttributes().get(StandardClaimNames.PREFERRED_USERNAME), auth.getAuthorities());
                assertEquals(expected, messageService.greet());
            }
        }
        ```

        在使用 @ParameterizedTest 和 @WithJwt 时，我们的核对表如下：

        - 用 @TestInstance(Lifecycle.PER_CLASS) 来装饰测试类
        - 自动连接 WithJwt.AuthenticationFactory
        - 定义一个方法，返回要使用的身份验证流，每种身份验证都使用身份验证工厂
        - 使用 @ParameterizedTest 代替 @Test
        - 用 @MethodSource 引用上面定义的方法来装饰测试
        - 为测试方法添加 @ParameterizedAuthentication 参数

5. 使用模拟授权进行集成测试

    我们将使用 @SpringBootTest 编写 Spring Boot 集成测试，以便 Spring 将实际组件连接在一起。为了继续使用模拟身份，我们将使用 MockMvc 或 WebTestClient。测试本身和使用模拟身份填充测试安全上下文的选项与单元测试相同。只有测试设置发生了变化：

    - 不再有组件 mock 或参数匹配器
    - 我们将使用 @SpringBootTest(webEnvironment = WebEnvironment.MOCK)，而不是 @WebMvcTest 或 @WebFluxTest。MOCK 环境最适合使用 MockMvc 或 WebTestClient 进行模拟授权
    - 使用 @AutoConfigureMockMvc 或 @AutoConfigureWebTestClient 显式装饰测试类，以便注入 MockMvc 或 WebTestClient。

    下面是 Spring Boot servlet 集成测试的骨架：

    ```java
    @SpringBootTest(webEnvironment = WebEnvironment.MOCK)
    @AutoConfigureMockMvc
    class ServletResourceServerApplicationTests {
        @Autowired
        MockMvc api;
        // Test structure and mocked identities options are the same as seen before in unit tests
    }
    ```

    在反应式应用程序中，这也是等价的：

    ```java
    @SpringBootTest(webEnvironment = WebEnvironment.MOCK)
    @AutoConfigureWebTestClient
    class ReactiveResourceServerApplicationTests {
        @Autowired
        WebTestClient api;
        // Test structure and mocked identities options are the same as seen before in unit tests
    }
    ```

    当然，这种集成测试节省了对模拟、参数捕获器等的配置，但也比单元测试更慢、更脆弱。我们应该谨慎使用这种测试，也许它的覆盖率比 @WebMvcTest 或 @WebFluxTest 更低，但它能确保自动布线和组件间通信正常工作。

6. 更进一步

    到目前为止，我们测试了使用 [JWT 解码器保护的资源服务器](https://www.baeldung.com/spring-security-oauth-resource-server#jwt-server)，其安全上下文中有 JwtAuthenticationToken 实例。我们只对模拟 HTTP 请求进行了自动测试，过程中未涉及任何授权服务器。

    1. 使用任何类型的 OAuth2 验证进行测试

        如前所述，Spring OAuth2 安全上下文可以包含其他类型的身份验证，在这种情况下，我们应该在测试中使用其他注解、请求后处理器或突变器：

        - 默认情况下，具有[令牌内省(introspection)](https://www.baeldung.com/spring-security-oauth-resource-server#opaque-server)功能的资源服务器在其安全上下文中包含 BearerTokenAuthentication 实例，因此测试应使用 @WithOpaqueToken、opaqueToken() 或 mockOpaqueToken()
        - 使用 [oauth2Login() 的客户端](https://www.baeldung.com/spring-security-5-oauth2-login)通常在其安全上下文中有一个 OAuth2AuthenticationToken，我们会使用 @WithOAuth2Login、@WithOidcLogin、oauth2Login()、oidcLogin()、mockOAuth2Login() 或 mockOidcLogin()
        - 假设我们使用 http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(...)或其他方法明确配置了自定义身份验证类型。在这种情况下，我们可能需要提供自己的单元测试工具，而使用 spring-addons 实现作为[示例](https://github.com/ch4mpy/spring-addons/blob/5.x/spring-addons-oauth2-test/src/main/java/com/c4_soft/springaddons/security/oauth2/test/annotations/OpenId.java)并不复杂。在同一个 Github 代码库中还包含带有自定义身份验证和专用测试注解的[示例](https://github.com/ch4mpy/spring-addons/tree/master/samples/tutorials)

        6.2. 运行示例应用程序

        示例项目包含运行于 <https://localhost:8443> 的 Keycloak 实例的主域属性。使用任何其他 OIDC 授权服务器只需调整发行者-uri 属性和 Java 配置中的授权映射器：更改 realmRoles2AuthoritiesConverter Bean，以映射新授权服务器将角色放入的私有权利主张中的授权。

        有关 Keycloak 设置的更多详情，请参阅[官方入门指南](https://www.keycloak.org/guides#getting-started)。单机版 [zip](https://www.keycloak.org/getting-started/getting-started-zip) 分发指南可能最容易上手。

        要使用自签证书通过 [TLS](https://www.keycloak.org/server/enabletls) 设置本地 Keycloak 实例，这个 [GitHub repo](https://github.com/ch4mpy/self-signed-certificate-generation) 会非常有用。

        授权服务器至少要有

        - 两个已声明的用户，其中一个被授予 ROLE_AUTHORIZED_PERSONNEL，另一个则没有；
        - 一个声明的客户端，启用授权代码流，以便 Postman 等工具代表这些用户获取访问令牌；

7. 结论

    在本文中，我们探讨了在 servlet 和反应式应用程序中使用模拟身份对 Spring OAuth2 访问控制规则进行单元测试和集成测试的两种方法：

    - 来自 spring-security-test 的 MockMvc 请求后处理器和 WebTestClient 突变器
    - 来自 [spring-addons-oauth2-test](https://central.sonatype.com/artifact/com.c4-soft.springaddons/spring-addons-oauth2-test/6.1.0) 的 OAuth2 测试注解

    我们还看到，我们可以使用 MockMvc 请求后处理器、WebTestClient 突变器或注解来测试 @Controllers。不过，只有后者能让我们在测试其他类型的组件时设置安全上下文。
