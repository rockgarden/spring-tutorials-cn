# [在 Spring Boot 中使用 Keycloak 的快速指南](https://www.baeldung.com/spring-boot-keycloak)

1. 概述

    在本教程中，我们将讨论设置 Keycloak 服务器并使用 Spring Security OAuth2.0 将 Spring Boot 应用程序连接到 Keycloak 服务器的基础知识。

2. 什么是 Keycloak？

    Keycloak 是面向现代应用程序和服务的开源身份和访问管理解决方案。

    Keycloak提供的功能包括单点登录（SSO）、身份中介和社交登录、用户联盟、客户端适配器、管理控制台和账户管理控制台。

    在本教程中，我们将使用 Keycloak 的管理控制台，使用 Spring Security OAuth2.0 设置和连接 Spring Boot。

3. 设置 Keycloak 服务器

    在本节中，我们将设置和配置 Keycloak 服务器。

    1. 下载并安装 Keycloak

        有多种发行版可供选择。不过，在本教程中，我们将使用独立版本。

        让我们从官方源下载 [Keycloak-22.0.3 独立服务器发行版](https://www.keycloak.org/archive/downloads-22.0.3.html)。

        下载完独立服务器发行版后，我们就可以解压并在终端启动 Keycloak：

        ```shell
        unzip keycloak-22.0.3.zip
        cd keycloak-22.0.3
        bin/kc.sh start-dev
        ```

        运行这些命令后，Keycloak 将启动服务。一旦我们看到一行包含 `Keycloak 22.0.3 [...] started` 的内容，我们就知道它已经启动完成了。

        现在打开浏览器，访问 <http://localhost:8080>。我们将被重定向到 <http://localhost:8080/auth> 以创建管理登录。

        创建名为 initial1 的初始管理员用户，密码为 zaq1!QAZ。点击"Create"后，我们将看到"User Created"的信息。

        现在我们可以进入管理控制台。在登录页面，我们将输入初始管理员用户凭证。

    2. 创建域

        登录成功后，我们将进入控制台，并打开默认的主域。

        在这里，我们将重点创建一个自定义域。

        让我们导航到左上角，找到"Create realm"按钮。

        在下一屏幕中，让我们添加一个名为 SpringBootKeycloak 的新 Realm。

        单击"Create"按钮后，新领域将被创建，我们将被重定向到该领域。接下来的所有操作都将在这个新的 SpringBootKeycloak 领域中执行。

    3. 创建客户端

        现在，我们将导航到客户端(Clients)页面。如下图所示，Keycloak 已经内置了客户端。

        我们还需要在应用程序中添加一个新客户端，因此点击"Create"。我们将调用新客户端 login-app。

        在下一步操作中，为了本教程的目的，我们将保留所有默认设置，有效重定向 URIs 字段除外。该字段应包含使用该客户端进行身份验证的应用程序 URL。

        稍后，我们将创建一个运行于 8081 端口的 Spring Boot 应用程序，它将使用此客户端。因此，我们使用的重定向 URL 是 <http://localhost:8081/*>。

    4. 创建角色和用户

        Keycloak 使用基于角色的访问；因此，每个用户都必须有一个角色。

        为此，我们需要导航到"Realm Roles"页面。

        然后添加 user 角色。

        现在我们有了一个可以分配给用户的角色，但由于还没有用户，让我们去用户页面添加一个。

        我们将添加一个名为 user1 的用户。

        创建用户后，将显示一个包含其详细信息的页面。

        现在我们可以进入凭证(Credentials)选项卡。我们将把初始密码设置为 xsw2@WS。

        最后，我们将进入角色映射(Role Mappings)选项卡。我们将为 user1 分配用户角色。

        最后，我们需要正确设置客户端作用域(Client Scopes)，以便 KeyCloak 将验证用户的所有角色都传递给令牌。因此，我们需要导航到客户端作用域页面，然后将 microprofile-jwt 设置为"default"。

        > 注意：Keycloak 25 配置方式改变。

4. 使用 Keycloak 的 API 生成访问令牌

    Keycloak 提供了用于生成和刷新访问令牌的 REST API。我们可以轻松使用该 API 创建自己的登录页面。

    首先，我们需要向以下 URL 发送 POST 请求，从 Keycloak 获取访问令牌：

    <http://localhost:8080/realms/SpringBootKeycloak/protocol/openid-connect/token>

    请求内容应为 x-www-form-urlencoded 格式：

    ```txt
    client_id:<your_client_id>
    username:<your_username>
    password:<your_password>
    grant_type:password
    ```

    我们将收到一个访问令牌和一个刷新令牌。

    访问令牌应在每次请求 Keycloak 保护的资源时使用，只需将其放在授权头中即可：

    ```json
    headers: {
        'Authorization': 'Bearer' + access_token
    }
    ```

    访问令牌过期后，我们可以通过向上述相同 URL 发送 POST 请求来刷新访问令牌，但请求中应包含刷新令牌，而不是用户名和密码：

    ```json
    {
        'client_id': 'your_client_id',
        'refresh_token': refresh_token_from_previous_request,
        'grant_type': 'refresh_token'
    }
    ```

    Keycloak 会用新的 access_token 和 refresh_token 对此做出响应。

5. 创建和配置 Spring Boot 应用程序

    在本节中，我们将创建一个 Spring Boot 应用程序，并将其配置为 OAuth 客户端，以便与 Keycloak 服务器交互。

    1. 依赖关系

        我们使用 Spring Security OAuth2.0 Client 连接到 Keycloak 服务器。

        首先，让我们在 Spring Boot 应用程序的 pom.xml 中声明 spring-boot-starter-oauth2-client 依赖关系：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
        </dependency>
        ```

        此外，由于我们需要在 Spring Boot 中使用 Spring Security，因此必须添加此依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        ```

        为了将身份验证控制委托给 Keycloak 服务器，我们将使用 spring-boot-starter-oauth2-resource-server 库。它将允许我们通过 Keycloak 服务器验证 JWT 令牌。因此，让我们把它添加到 pom 中：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        ```

        现在，Spring Boot 应用程序可以与 Keycloak 交互了。

    2. Keycloak 配置

        我们将 Keycloak 客户端视为 OAuth 客户端。因此，我们需要配置 Spring Boot 应用程序以使用 OAuth 客户端。

        ClientRegistration 类包含客户端的所有基本信息。Spring 自动配置会查找模式为 spring.security.oauth2.client.registration.[registrationId] 的属性，并使用 OAuth 2.0 或 OpenID Connect ([OIDC](https://www.baeldung.com/spring-security-openid-connect#introduction)) 注册客户端。

        让我们来配置客户端注册配置：

        ```properties
        spring.security.oauth2.client.registration.keycloak.client-id=login-app
        spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
        spring.security.oauth2.client.registration.keycloak.scope=openid
        ```

        我们在 client-id 中指定的值与我们在管理控制台中命名的客户端相匹配。

        Spring Boot 应用程序需要与 OAuth 2.0 或 OIDC 提供程序交互，以处理不同授予类型的实际请求逻辑。因此，我们需要配置 OIDC 提供程序。它可以根据 `schema spring.security.oauth2.client.provider.[provider name]` 的属性值自动配置。

        让我们来配置 OIDC 提供程序：

        ```properties
        spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8080/realms/SpringBootKeycloak
        spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
        ```

        我们还记得，我们是在 8080 端口启动 Keycloak 的，因此在 issuer-uri 中指定了路径。该属性标识了授权服务器的基本 URI。我们输入在 Keycloak 管理控制台中创建的领域名称。此外，我们还可以将 user-name-attribute 定义为 preferred_username，以便在控制器的 Principal 中填充合适的用户。

        最后，让我们添加针对 Keycloak 服务器验证 JWT 令牌所需的配置：

        `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/SpringBootKeycloak`

    3. 配置类

        我们通过创建 SecurityFilterChain Bean 来配置 HttpSecurity。此外，我们还需要使用 http.oauth2Login() 启用 OAuth2 登录。

        让我们来看看创建安全配置所需的步骤。我们将根据以下目标配置应用程序的安全设置：

        - 对于以 `customers/*` 开头的 URL，只允许具有 USER 角色的个人访问。允许通过身份验证的用户不受限制地访问所有其他 URL，但如果他们没有 USER 角色，则不包括 `customers/*` 开头的 URL。

        以下代码实现了上述安全要求：

        ```java
        @Bean
        public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth
            .requestMatchers(new AntPathRequestMatcher("/customers*", HttpMethod.OPTIONS.name()))
            .permitAll()
            .requestMatchers(new AntPathRequestMatcher("/customers*"))
            .hasRole("user")
            .requestMatchers(new AntPathRequestMatcher("/"))
            .permitAll()
            .anyRequest()
            .authenticated());
            http.oauth2ResourceServer((oauth2) -> oauth2
            .jwt(Customizer.withDefaults()));
            http.oauth2Login(Customizer.withDefaults())
            .logout(logout -> logout.addLogoutHandler(keycloakLogoutHandler).logoutSuccessUrl("/"));
            return http.build();
        }
        ```

        在上面的代码中，oauth2Login() 方法将 [OAuth2LoginAuthenticationFilter](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/oauth2/client/web/OAuth2LoginAuthenticationFilter.html) 添加到过滤器链中。该过滤器拦截请求并应用 OAuth 2 身份验证所需的逻辑。oauth2ResourceServer 方法根据我们的 Keycloak 服务器验证 JWT 令牌的绑定。它还会执行前面讨论过的约束。

        Keycloak 会返回一个包含所有相关信息的令牌。为了让 Spring Security 根据用户分配的角色做出决策，我们必须解析令牌并提取相关详细信息。不过，Spring Security 通常会在每个角色名称前添加"ROLES_"前缀，而 Keycloak 发送的是纯角色名称。为了解决这个问题，我们创建了一个辅助方法，为从 Keycloak 获取的每个角色添加"ROLE_"前缀。

        ```java
        @Bean
        Collection generateAuthoritiesFromClaim(Collection roles) {
            return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(
                Collectors.toList());
        }
        ```

        现在，我们可以开始解析令牌了。首先，我们必须检查令牌是 OidcUserAuthority 还是 OAuth2UserAuthority 的实例。由于 Keycloak 令牌可以是两种类型，因此我们需要实现一个解析逻辑。下面的代码会检查令牌类型并决定解析机制。

        ```java
        boolean isOidc = authority instanceof OidcUserAuthority;
            if (isOidc) { 
            /// Parsing code here
            }
        ```

        默认情况下，令牌是 OidcUserAuthority 的实例。

        如果令牌是一个 OidcUserAuthority 实例，则可将其配置为包含组或领域访问下的角色。因此，我们必须同时检查这两种情况，以提取角色，如下文代码所示、

        ```java
        if (userInfo.hasClaim(REALM_ACCESS_CLAIM)) {
            var realmAccess = userInfo.getClaimAsMap(REALM_ACCESS_CLAIM);
            var roles = (Collection) realmAccess.get(ROLES_CLAIM);
            mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
        } else if (userInfo.hasClaim(GROUPS)) {
            Collection roles = (Collection) userInfo.getClaim(GROUPS);
            mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
        }
        ```

        但是，如果令牌是一个 OAuth2UserAuthority 实例，我们就需要对其进行如下解析：

        ```java
        var oauth2UserAuthority = (OAuth2UserAuthority) authority;
        Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
        if (userAttributes.containsKey(REALM_ACCESS_CLAIM)) {
            Map<String, Object> realmAccess = (Map<String, Object>) userAttributes.get(REALM_ACCESS_CLAIM);
            Collection roles = (Collection) realmAccess.get(ROLES_CLAIM);
            mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
        }
        ```

        完整代码如下，供您参考：

        ![SecurityConfig](/src/main/java/com/baeldung/keycloak/SecurityConfig.java)

        最后，我们需要处理 Keycloak 的注销。为此，我们添加了 KeycloakLogoutHandler 类：

        ![KeycloakLogoutHandler](/src/main/java/com/baeldung/keycloak/KeycloakLogoutHandler.java)

        KeycloakLogoutHandler 类实现了 LogoutHandler 类，并向 Keycloak 发送注销请求。

        现在，通过身份验证后，我们就可以访问内部客户页面了。

    4. Thymeleaf 网页

        我们使用 Thymeleaf 制作网页。

        我们有三个页面

        - external.html - 面向公众的外部网页
        - customers.html--面向内部的网页，其访问权限仅限于角色为 user 的认证用户
        - layout.html - 一个简单的布局，由两个片段组成，分别用于对外页面和对内页面

        Thymeleaf 模板的代码可在 [Github](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-keycloak/src/main/resources/templates) 上获取。

    5. 控制器

        网页控制器将内部和外部 URL 映射到相应的 Thymeleaf 模板：

        ```java
        @GetMapping(path = "/")
        public String index() {
            return "external";
        }
            
        @GetMapping(path = "/customers")
        public String customers(Principal principal, Model model) {
            addCustomers();
            model.addAttribute("customers", customerDAO.findAll());
            model.addAttribute("username", principal.getName());
            return "customers";
        }
        ```

        对于路径 /customers，我们将从资源库中检索所有客户，并将结果作为属性添加到模型中。稍后，我们将在 Thymeleaf 中遍历结果。

        为了能够显示用户名，我们还要注入 Principal。

        需要注意的是，我们在这里只是将客户作为原始数据来显示，仅此而已。

6. 演示

    现在我们准备测试应用程序。要运行 Spring Boot 应用程序，我们可以通过 IDE（如 Spring Tool Suite (STS)）轻松启动它，或者在终端运行以下命令：

    `mvn clean spring-boot:run`

    访问 <http://localhost:8081> 时，我们会看到面向外部的 Keycloak 页面。

    现在，我们点击客户进入内网，这是敏感信息的位置。

    请注意，我们已被重定向到通过 Keycloak 进行身份验证，以查看我们是否被授权查看这些内容。

    以 user1 登录后，Keycloak 将验证我们是否有用户角色的授权，然后我们将被重定向到受限制的客户页面。

    现在我们完成了连接 Spring Boot 和 Keycloak 的设置，并演示了它是如何工作的。

    我们可以看到，Spring Boot 无缝地处理了调用 Keycloak 授权服务器的整个过程。我们无需调用 Keycloak API 自己生成访问令牌，甚至无需在请求受保护资源时明确发送授权头。

7. 结论

    在本文中，我们配置了 Keycloak 服务器，并将其用于 Spring Boot 应用程序。

    我们还学习了如何设置 Spring Security 并将其与 Keycloak 结合使用。
