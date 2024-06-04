# [Spring Security - 来自 JWT 的映射授权](https://www.baeldung.com/spring-security-map-authorities-jwt)

1. 简介

    在本教程中，我们将展示如何将 [JWT](https://www.baeldung.com/java-json-web-tokens-jjwt)（JSON 网络令牌）请求自定义映射到 Spring Security 的授权。

2. 背景介绍

    正确配置的基于 Spring Security 的应用程序收到请求时，会经过一系列步骤，这些步骤本质上有两个目标：

    - 验证请求，以便应用程序知道谁在访问它
    - 决定通过身份验证的请求是否可以执行相关操作

    对于使用 [JWT 作为主要安全机制](https://www.baeldung.com/spring-security-oauth-jwt)的应用程序来说，授权环节包括：

    - 从 JWT 有效负载（通常是范围或 scp 请求）中提取请求值
    - 将这些请求映射到一组 GrantedAuthority 对象中

    一旦安全引擎设置了这些授权，它就可以评估是否有任何访问限制适用于当前请求，并决定是否可以继续。

3. 默认映射

    在开箱即用的情况下，Spring 使用一种直接的策略将请求转换为 GrantedAuthority 实例。首先，它会提取作用域或 scp 权利要求，并将其拆分成一个字符串列表。接下来，它会为每个字符串创建一个新的 SimpleGrantedAuthority，使用前缀 SCOPE_，后跟 scope 值。

    为了说明这一策略，让我们创建一个简单的端点，让我们可以检查应用程序可用的身份验证实例的一些关键属性：

    main/.openid.oidc.jwtauthorities.web.controllersUserRestController.java:getPrincipalInfo(JwtAuthenticationToken principal)

    在这里，我们使用了 JwtAuthenticationToken 参数，因为我们知道，在使用基于 JWT 的身份验证时，这将是 Spring Security 创建的实际身份验证实现。我们从其 name 属性、可用的 GrantedAuthority 实例和 JWT 的原始属性中提取信息，创建结果。

    现在，假设我们调用了这个端点，并传递了包含以下有效载荷的经过编码和签名的 JWT：

    ```json
    {
    "aud": "api://f84f66ca-591f-4504-960a-3abc21006b45",
    "iss": "https://sts.windows.net/2e9fde3a-38ec-44f9-8bcd-c184dc1e8033/",
    "iat": 1648512013,
    "nbf": 1648512013,
    "exp": 1648516868,
    "email": "psevestre@gmail.com",
    "family_name": "Sevestre",
    "given_name": "Philippe",
    "name": "Philippe Sevestre",
    "scp": "profile.read",
    "sub": "eXWysuqIJmK1yDywH3gArS98PVO1SV67BLt-dvmQ-pM",
    ... more claims omitted
    }
    ```

    响应应该是一个 JSON 对象，包含三个属性：

    ```json
    {
    "tokenAttributes": {
        // ... token claims omitted
    },
    "name": "0047af40-473a-4dd3-bc46-07c3fe2b69a5",
    "authorities": [
        "SCOPE_profile",
        "SCOPE_email",
        "SCOPE_openid"
    ]
    }
    ```

    通过创建 SecurityFilterChain，我们可以使用这些作用域来限制对应用程序某些部分的访问：

    ```java
    @Bean
    SecurityFilterChain customJwtSecurityChain(HttpSecurity http) throws Exception {
        // @formatter:off
        return http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                    .jwtAuthenticationConverter(customJwtAuthenticationConverter(accountService)))).build();
        // @formatter:on
    }
    ```

    请注意，我们有意避免使用 WebSecurityConfigureAdapter。如前[所述](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)，该类将在 Spring Security 5.7 版本中被弃用，因此最好尽快开始使用新方法。

    另外，我们也可以使用方法级注解和 SpEL 表达式来实现相同的结果：

    ```java
    @GetMapping("/authorities")
    @PreAuthorize("hasAuthority('SCOPE_profile.read')")
    public Map<String,Object> getPrincipalInfo(JwtAuthenticationToken principal) {
        // ... same code as before
    }
    ```

    最后，对于更复杂的情况，我们还可以直接访问当前的 JwtAuthenticationToken，这样就可以直接访问所有授予的权限。

4. 自定义 SCOPE_ 前缀

    作为如何更改 Spring Security 默认请求映射行为的第一个示例，让我们来看看如何将 SCOPE_ 前缀更改为其他前缀。如文档所述，这项任务涉及两个类：

    - JwtAuthenticationConverter：将原始 JWT 转换为 AbstractAuthenticationToken
    - JwtGrantedAuthoritiesConverter：从原始 JWT 中提取 GrantedAuthority 实例集合。

    在内部，JwtAuthenticationConverter 使用 JwtGrantedAuthoritiesConverter 将 GrantedAuthority 对象及其他属性填充到 JwtAuthenticationToken 中。

    更改此前缀的最简单方法是提供我们自己的 JwtAuthenticationConverter Bean，并将 JwtGrantedAuthoritiesConverter 配置为我们自己选择的版本：

    ```java
    @Configuration
    @EnableConfigurationProperties(JwtMappingProperties.class)
    @EnableMethodSecurity
    public class SecurityConfig {
        // ... fields and constructor omitted
        @Bean
        public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
            JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
            if (StringUtils.hasText(mappingProps.getAuthoritiesPrefix())) {
                converter.setAuthorityPrefix(mappingProps.getAuthoritiesPrefix().trim());
            }
            return converter;
        }
        
        @Bean
        public JwtAuthenticationConverter customJwtAuthenticationConverter() {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter();
            return converter;
        }
    ```

    在这里，JwtMappingProperties 只是一个 @ConfigurationProperties 类，我们将用它来外部化映射属性。虽然在本代码段中没有显示，但我们将使用构造函数注入来初始化 mappingProps 字段，并使用从任何已配置的 PropertySource 中填充的实例，从而为我们在部署时更改其值提供足够的灵活性。

    这个 @Configuration 类有两个 @Bean 方法：jwtGrantedAuthoritiesConverter() 创建所需的转换器，用于创建 GrantedAuthority 集合。在本例中，我们使用的是使用配置属性中设置的前缀配置的 JwtGrantedAuthoritiesConverter。

    接下来是 customJwtAuthenticationConverter()，我们在这里构建 JwtAuthenticationConverter，配置为使用自定义转换器。在这里，Spring Security 会将其作为标准自动配置过程的一部分，并替换默认转换器。

    现在，一旦我们将 baeldung.jwt.mapping.authorities-prefix 属性设置为某个值，例如 MY_SCOPE，并调用 /user/authorities，我们就会看到自定义的权限：

    ```json
    {
    "tokenAttributes": {
        // ... token claims omitted 
    },
    "name": "0047af40-473a-4dd3-bc46-07c3fe2b69a5",
    "authorities": [
        "MY_SCOPE_profile",
        "MY_SCOPE_email",
        "MY_SCOPE_openid"
    ]
    }
    ```

5. 在安全结构中使用自定义前缀

    需要注意的是，通过更改权限前缀，我们将影响任何依赖于权限名称的授权规则。例如，如果我们将前缀改为 MY_PREFIX_，那么任何假定默认前缀的 @PreAuthorize 表达式都将不再有效。同样的情况也适用于基于 HttpSecurity 的授权结构。

    不过，解决这个问题很简单。首先，让我们在 @Configuration 类中添加一个 @Bean 方法，返回配置的前缀。由于该配置是可选的，因此我们必须确保在没有给定默认值的情况下返回默认值：

    ```java
    @Bean
    public String jwtGrantedAuthoritiesPrefix() {
    return mappingProps.getAuthoritiesPrefix() != null ?
        mappingProps.getAuthoritiesPrefix() : 
        "SCOPE_";
    }
    ```

    现在，我们可以在 SpEL 表达式中使用 `@<bean-name>` [语法](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)来引用此 Bean。下面是我们如何使用 @PreAuthorize 的前缀 bean：

    ```java
    @GetMapping("/authorities")
    @PreAuthorize("hasAuthority(@jwtGrantedAuthoritiesPrefix + 'profile.read')")
    public Map<String,Object> getPrincipalInfo(JwtAuthenticationToken principal) {
        // ... method implementation omitted
    }
    ```

    在定义 SecurityFilterChain 时，我们也可以使用类似的方法：

    ```java
    @Bean
    SecurityFilterChain customJwtSecurityChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/user/**")
            .hasAuthority(mappingProps.getAuthoritiesPrefix() + "profile");
        })
        // ... other customizations omitted
        .build();
    }
    ```

6. 自定义委托人名称

    有时，Spring 映射到身份验证 name 属性的标准子声明会带有一个不太有用的值。Keycloak 生成的 JWT 就是一个很好的例子：

    ```java
    {
    // ... other claims omitted
    "sub": "0047af40-473a-4dd3-bc46-07c3fe2b69a5",
    "scope": "openid profile email",
    "email_verified": true,
    "name": "User Primo",
    "preferred_username": "user1",
    "given_name": "User",
    "family_name": "Primo"
    }

    在本例中，sub 附带了一个内部标识符，但我们可以看到 preferred_username claim 有一个更友好的值。我们可以通过设置 JwtAuthenticationConverter 的 principalClaimName 属性，并将其设置为所需的权利要求名称，从而轻松修改 JwtAuthenticationConverter 的行为：

    ```java
    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());

        if (StringUtils.hasText(mappingProps.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(mappingProps.getPrincipalClaimName());
        }
        return converter;
    }
    ```

    现在，如果我们将 baeldung.jwt.mapping.authorities-prefix 属性设置为 “preferred_username”，/user/authorities 的结果就会相应改变：

    ```java
    {
    "tokenAttributes": {
        // ... token claims omitted 
    },
    "name": "user1",
    "authorities": [
        "MY_SCOPE_profile",
        "MY_SCOPE_email",
        "MY_SCOPE_openid"
    ]
    }
    ```

7. 作用域名称映射

    有时，我们可能需要将 JWT 中接收到的作用域名称映射为内部名称。例如，同一个应用程序需要使用不同授权服务器生成的令牌，这取决于它部署的环境。

    我们可能会想扩展 JwtGrantedAuthoritiesConverter，但由于这是一个最终类，我们不能使用这种方法。相反，我们必须编写自己的转换器类，并将其注入到 JwtAuthorizationConverter 中。这个增强的映射器 MappingJwtGrantedAuthoritiesConverter 实现了 `Converter<Jwt,Collection<GrantedAuthority>>`，看起来与原始映射器非常相似：

    main/.openid.oidc.jwtauthorities.config/MappingJwtGrantedAuthoritiesConverter.java

    在这里，该类的关键是映射步骤，我们使用提供的作用域映射将原始作用域转换为映射后的作用域。此外，任何没有可用映射的传入作用域都将被保留。

    最后，我们在 @Configuration 的 jwtGrantedAuthoritiesConverter() 方法中使用这个增强的转换器：

    ```java
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        MappingJwtGrantedAuthoritiesConverter converter = new MappingJwtGrantedAuthoritiesConverter(mappingProps.getScopes());

        if (StringUtils.hasText(mappingProps.getAuthoritiesPrefix())) {
            converter.setAuthorityPrefix(mappingProps.getAuthoritiesPrefix());
        }
        if (StringUtils.hasText(mappingProps.getAuthoritiesClaimName())) {
            converter.setAuthoritiesClaimName(mappingProps.getAuthoritiesClaimName());
        }
        return converter;
    }
    ```

8. 使用自定义 JwtAuthenticationConverter

    在这种情况下，我们将完全控制 JwtAuthenticationToken 的生成过程。我们可以使用这种方法返回该类的扩展版本，其中包含从数据库中恢复的附加数据。

    取代标准 JwtAuthenticationConverter 的方法有两种。第一种是创建一个 @Bean 方法来返回我们自定义的转换器，我们在前面的章节中已经使用过这种方法。不过，这意味着我们的自定义版本必须扩展 Spring 的 JwtAuthenticationConverter，这样自动配置过程才能选择它。

    第二种方法是使用基于 HttpSecurity 的 DSL 方法，在这种方法中，我们可以提供自定义转换器。我们将使用 oauth2ResourceServer 自定义器来实现这一点，它允许我们插入任何实现更通用接口 `Converter<Jwt, AbstractAuthorizationToken>` 的转换器：

    ```java
    @Bean
    SecurityFilterChain customJwtSecurityChain(HttpSecurity http) throws Exception {
        // @formatter:off
        return http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                    .jwtAuthenticationConverter(customJwtAuthenticationConverter(accountService)))).build();
        // @formatter:on
    }
    ```

    我们的 CustomJwtAuthenticationConverter 使用 AccountService（可在线获取），根据用户名声称值检索账户对象。然后，它使用该对象创建一个 CustomJwtAuthenticationToken，并为账户数据提供一个额外的访问方法：

    ```java
    public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

        // ...private fields and construtor omitted
        @Override
        public AbstractAuthenticationToken convert(Jwt source) {
            
            Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(source);
            String principalClaimValue = source.getClaimAsString(this.principalClaimName);
            Account acc = accountService.findAccountByPrincipal(principalClaimValue);
            return new AccountToken(source, authorities, principalClaimValue, acc);
        }
    }
    ```

    现在，让我们修改 /user/authorities 处理程序，以使用增强型身份验证：

    ```java
    @GetMapping("/authorities")
    public Map<String,Object> getPrincipalInfo(JwtAuthenticationToken principal) {

        // ... create result map as before (omitted)
        if (principal instanceof AccountToken) {
            info.put( "account", ((AccountToken)principal).getAccount());
        }
        return info;
    }
    ```

    采用这种方法的一个好处是，我们现在可以在应用程序的其他部分轻松使用增强的身份验证对象。例如，我们可以在 [SpEL](https://www.baeldung.com/spring-expression-language) 表达式中直接通过内置变量身份验证访问账户信息：

    ```java
    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("authentication.account.accountNumber == #accountNumber")
    public Account getAccountById(@PathVariable("accountNumber") String accountNumber, AccountToken authentication) {
        return authentication.getAccount();
    }
    ```

    在这里，@PreAuthorize 表达式强制要求路径变量中传递的 accountNumber 属于用户。与 Spring Data JPA 结合使用时，这种方法尤其有用，如官方文档所述。

9. 测试提示

    到目前为止给出的示例都假定我们有一个正常运行的身份提供者（IdP），它可以签发基于 JWT 的访问令牌。一个不错的选择是使用我们在[这里](https://www.baeldung.com/keycloak-embedded-in-spring-boot-app)已经介绍过的嵌入式 Keycloak 服务器。我们的《[Keycloak 快速使用指南](https://www.baeldung.com/spring-boot-keycloak)》中还提供了其他配置说明。

    请注意，这些说明涵盖了如何注册 OAuth 客户端。对于实时测试，Postman 是支持授权代码流的好工具。这里的重要细节是如何正确配置有效重定向 URI 参数。由于 Postman 是一个桌面应用程序，它使用位于 <https://oauth.pstmn.io/v1/callback> 的辅助网站来获取授权代码。因此，我们必须确保在测试期间能够连接互联网。如果无法连接，我们可以使用安全性较低的密码授权流来代替。

    无论选择哪种 IdP 和客户端，我们都必须配置资源服务器，使其能够正确验证接收到的 JWT。对于标准 OIDC 提供商，这意味着要为 spring.security.oauth2.resourceserver.jwt.issuer-uri 属性提供一个合适的值。然后，Spring 将使用 .well-known/openid-configuration 文档获取所有配置细节。

    在我们的例子中，Keycloak 领域的发行者 URI 是 <http://localhost:8083/auth/realms/baeldung。我们可以将浏览器指向> <http://localhost:8083/auth/realms/baeldung/.well-known/openid-configuration> 获取完整文档。

10. 结论

    在本文中，我们展示了自定义 Spring Security 从 JWT 获取映射授权的不同方法。
