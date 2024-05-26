# [Spring 安全与 OpenID Connect](https://www.baeldung.com/spring-security-openid-connect)

请注意，本文已更新为新的 Spring Security OAuth 2.0 协议栈。不过，使用传统协议栈的教程仍然可用。

1. 概述

    在本教程中，我们将重点介绍如何使用 Spring Security 设置 OpenID Connect (OIDC)。

    我们将介绍该规范的各个方面，然后了解 Spring Security 为在 OAuth 2.0 客户端上实现该规范所提供的支持。

2. OpenID Connect 快速介绍

    [OpenID Connect](https://openid.net/connect/) 是建立在 [OAuth 2.0](https://tools.ietf.org/html/rfc6749) 协议之上的身份层。

    因此，在深入学习 OIDC（尤其是授权码流程）之前，了解 OAuth 2.0 非常重要。

    OIDC 规范套件非常广泛。它包括核心功能和其他一些可选功能，以不同的组别呈现。以下是主要功能：

    - 核心--验证和使用 Claims 来交流终端用户信息
    - 发现--规定客户端如何动态确定 OpenID 提供商的信息
    - 动态注册--规定客户端如何向提供商注册
    - 会话管理--规定如何管理 OIDC 会话

    除此以外，这些文件还区分了为该规范提供支持的 OAuth 2.0 验证服务器，将其称为 OpenID 提供者（OPs），将使用 OIDC 的 OAuth 2.0 客户端称为依赖方（RPs）。我们将在本文中使用这一术语。

    值得注意的是，客户端可以通过在授权请求中添加 openid 范围来请求使用此扩展。

    最后，在本教程中，我们还需要知道，OP 会以 JWT（称为 ID 令牌）的形式发送最终用户信息。

    现在，我们已经准备好深入 OIDC 的世界了。

3. 项目设置

    在专注于实际开发之前，我们必须向 OpenID 提供商注册一个 OAuth 2.0 客户端。

    在本例中，我们将使用 Google 作为 OpenID 提供者。我们可以按照以下[说明](https://developers.google.com/identity/protocols/OpenIDConnect#appsetup)在其平台上注册客户端应用程序。请注意，openid 范围是默认存在的。

    我们在此过程中设置的重定向 URI 是我们服务中的一个端点：<http://localhost:8081/login/oauth2/code/google。>

    我们应从此进程中获取一个客户端 ID 和一个客户端密文。

    1. Maven 配置

        我们首先将这些依赖项添加到项目 pom 文件中：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
            <version>2.2.6.RELEASE</version>
        </dependency>
        ```

        启动器工件聚合了所有与 Spring Security Client 相关的依赖项，包括

        - 用于 OAuth 2.0 登录和客户端功能的 spring-security-oauth2-client 依赖项
        - 支持 JWT 的 JOSE 库

        像往常一样，我们可以使用 Maven Central 搜索引擎找到该工件的最新版本。

4. 使用 Spring Boot 进行基本配置

    首先，我们要配置应用程序，以便使用刚刚在 Google 创建的客户端注册。

    使用 Spring Boot 可以让这一切变得非常简单，因为我们只需定义两个应用程序属性即可：

    ```yml
    spring:
    security:
        oauth2:
        client:
            registration: 
            google: 
                client-id: <client-id>
                client-secret: <secret>
    ```

    现在让我们启动应用程序并尝试访问端点。我们会看到，我们被重定向到 OAuth 2.0 客户端的 Google 登录页面。

    这看起来非常简单，但其实暗藏玄机。接下来，我们将探讨 Spring Security 如何实现这一点。

    之前，在我们的《[WebClient 和 OAuth 2 支持](https://www.baeldung.com/spring-webclient-oauth2#springsecurity-internals)》一文中，我们分析了 Spring Security 如何处理 OAuth 2.0 授权服务器和客户端的内部机制。

    在那篇文章中，我们看到除了客户端 ID 和客户端密钥之外，我们还必须提供额外的数据才能成功配置 ClientRegistration 实例。

    那么，这是如何做到的呢？

    谷歌是众所周知的提供商，因此框架提供了一些预定义属性来简化工作。

    我们可以在 CommonOAuth2Provider 枚举中查看这些配置。

    对于 Google，枚举类型定义了以下属性

    - 将使用的默认作用域
    - 授权端点
    - 令牌端点
    - 用户信息（UserInfo）端点，这也是 OIDC 核心规范的一部分。

    1. 访问用户信息

        Spring Security 为在 OIDC 提供商处注册的用户 Principal（即 OidcUser 实体）提供了一种有用的表示方法。

        除了基本的 OAuth2AuthenticatedPrincipal 方法外，该实体还提供了一些有用的功能：

        - 检索 ID 令牌值及其包含的权利要求
        - 获取 UserInfo 端点提供的 Claims
        - 生成两组数据的集合

        我们可以在控制器中轻松访问该实体：

        ```java
        @GetMapping("/oidc-principal")
        public OidcUser getOidcUserPrincipal(
        @AuthenticationPrincipal OidcUser principal) {
            return principal;
        }
        ```

        或者，我们可以在 bean 中使用 SecurityContextHolder：

        ```java
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser principal = ((OidcUser) authentication.getPrincipal());
            
            // ...
        }
        ```

        如果我们检查委托人，就会看到很多有用的信息，如用户的姓名、电子邮件、个人照片和所在地。

5. 运行中的 OIDC

    到目前为止，我们已经了解了如何使用 Spring Security 轻松实现 OIDC 登录解决方案。

    我们看到了将用户身份验证过程委托给 OpenID Provider 所带来的好处，OpenID Provider 甚至可以以可扩展的方式提供详细的有用信息。

    但事实上，到目前为止，我们还不需要处理任何与 OIDC 相关的问题。这意味着 Spring 正在为我们完成大部分工作。

    因此，让我们来看看幕后发生了什么，以便更好地了解本规范是如何付诸实施的，并能从中获得最大收益。

    1. 登录过程

        为了看清楚这个过程，让我们启用 RestTemplate 日志来查看服务正在执行的请求：

        ```yml
        logging:
        level:
            org.springframework.web.client.RestTemplate: DEBUG
        ```

        如果我们现在调用一个安全端点，我们会看到服务正在执行常规的 OAuth 2.0 授权代码流。这是因为，正如我们所说，该规范建立在 OAuth 2.0 的基础之上。

        其中有一些不同之处。

        首先，根据我们使用的提供程序和配置的作用域，我们可能会看到服务正在调用开头提到的 UserInfo 端点。

        也就是说，如果授权响应检索到个人资料、电子邮件、地址或电话范围中的至少一个，框架就会调用 UserInfo 端点来获取更多信息。

        尽管一切都表明 Google 应检索配置文件和电子邮件范围（因为我们在授权请求中使用了它们），但 OP 却检索了它们的自定义对应信息 <https://www.googleapis.com/auth/userinfo.email> 和 <https://www.googleapis.com/auth/userinfo.profile>，因此 Spring 不会调用该端点。

        这意味着我们获取的所有信息都是 ID 令牌的一部分。

        我们可以通过创建和提供自己的 OidcUserService 实例来适应这种行为：

        main/.openid.oidc.login.config/OAuth2LoginSecurityConfig.java

        我们将观察到的第二个区别是对 JWK Set URI 的调用。正如我们在 [JWS 和 JWK 一文](https://www.baeldung.com/spring-security-oauth2-jws-jwk) 中所解释的，这是用来验证 JWT 格式的 ID 令牌签名的。

        接下来，我们将详细分析 ID 令牌。

    2. ID 令牌

        当然，OIDC 规范涵盖并适应许多不同的情况。在本例中，我们使用的是授权码流程，协议指出访问令牌和 ID 令牌都将作为令牌端点响应的一部分进行检索。

        如前所述，OidcUser 实体包含 ID 令牌中的 Claims 和实际的 JWT 格式令牌，可以使用 jwt.io 对其进行检查。

        除此之外，Spring 还提供了许多方便的获取器，以简洁的方式获取规范定义的标准 Claims。

        我们可以看到，ID 令牌包含一些必须的 Claims：

        - 格式化为 URL 的发行者标识符（例如 " <https://accounts.google.com> "）。
        - 主体 id，即发行方所包含的最终用户的参考信息
        - 令牌的到期时间
        - 令牌签发时间
        - 受众，包含我们配置的 OAuth 2.0 客户 ID

        它还包含许多[OIDC 标准声明](https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims)，如我们之前提到的那些（姓名、地域、图片、电子邮件）。

        由于这些都是标准，我们可以预期许多提供商至少会检索其中的一些字段，从而促进更简单解决方案的开发。

    3. 权利要求和范围

        我们可以想象，OP 检索到的 Claim 与我们（或 Spring Security）配置的作用域相对应。

        OIDC 定义了一些作用域，可用于请求 OIDC 定义的 Claims：

        - profile，可用于请求默认的配置文件声称（如名称、首选用户名、图片等）
        - 电子邮件，用于访问电子邮件和电子邮件验证的权利要求
        - 地址
        - 电话，用于请求电话号和电话号验证声明

        尽管 Spring 尚不支持，但该规范允许在授权请求中指定单个 Claims。

6. Spring 对 OIDC 发现的支持

    正如我们在介绍中解释的那样，OIDC 除了其核心目的外，还包括许多不同的功能。

    我们将在本节和下文中分析的功能在 OIDC 中是可选的。因此，重要的是要明白，有些 OP 可能不支持这些功能。

    本规范定义了一种发现机制，用于 RP 发现 OP 并获取与之交互所需的信息。

    简而言之，OP 提供一个包含标准元数据的 JSON 文档。这些信息必须由发行者位置（/.owned/openid-configuration）的知名端点提供。

    Spring 正是利用了这一点，允许我们只用一个简单的属性（即发行者位置）来配置 ClientRegistration。

    让我们直接进入一个示例来清楚地了解这一点。

    我们将定义一个自定义的 ClientRegistration 实例：

    ```yml
    spring:
    security:
        oauth2:
        client:
            registration: 
            custom-google: 
                client-id: <client-id>
                client-secret: <secret>
            provider:
            custom-google:
                issuer-uri: https://accounts.google.com
    ```

    现在，我们可以重启应用程序并检查日志，以确认应用程序在启动过程中调用了 openid-configuration 端点。

    我们甚至可以浏览该端点，查看 Google 提供的信息：

    <https://accounts.google.com/.well-known/openid-configuration>

    例如，我们可以看到服务必须使用的授权、令牌和用户信息端点，以及支持的作用域。

    这里需要特别注意的是，如果服务启动时发现端点不可用，我们的应用程序将无法成功完成启动过程。

7. OpenID Connect 会话管理

    本规范通过定义以下内容对核心功能进行补充：

    - 在 OP 上持续监控终端用户登录状态的不同方法，以便 RP 注销已注销 OpenID 提供商的终端用户
    - 作为客户端注册的一部分，RP 可以向 OP 注册注销 URI，以便在最终用户注销 OP 时获得通知
    - 一种机制，用于通知 OP 终端用户已注销网站，并可能也想注销 OP。

    当然，并非所有 OP 都支持所有这些项目，其中一些解决方案只能通过用户代理（User-Agent）在前端实现。

    在本教程中，我们将重点介绍 Spring 为列表中最后一项（RP 启动的注销）提供的功能。

    此时，如果我们登录应用程序，通常可以访问每个端点。

    如果我们注销（调用 /logout 端点）并在注销后向安全资源发出请求，我们会发现无需再次登录即可获得响应。

    但事实上并非如此。如果我们检查浏览器调试控制台中的网络选项卡，就会发现当我们第二次点击安全端点时，会被重定向到 OP 授权端点。由于我们仍在那里登录，因此流程以透明方式完成，几乎瞬间就到达了安全端点。

    当然，在某些情况下这可能不是我们想要的行为。让我们看看如何实施 OIDC 机制来解决这个问题。

    1. OpenID 提供程序配置

        在本例中，我们将配置并使用一个 Okta 实例作为 OpenID 提供程序。我们不会详细介绍如何创建实例，但可以按照[本指南](https://help.okta.com/en/prod/Content/Topics/Apps/apps-about-oidc.htm)的步骤进行，同时牢记 Spring Security 的默认回调端点将是 /login/oauth2/code/okta。

        在应用程序中，我们可以使用属性定义客户端注册数据：

        ```yml
        spring:
        security:
            oauth2:
            client:
                registration: 
                okta: 
                    client-id: <client-id>
                    client-secret: <secret>
                provider:
                okta:
                    issuer-uri: https://dev-123.okta.com
        ```

        OIDC 指出，OP 注销端点可以在发现文档中指定为 end_session_endpoint 元素。

    2. 注销成功处理程序配置

        接下来，我们需要通过提供自定义的 LogoutSuccessHandler 实例来配置 HttpSecurity 注销逻辑：

        ```java
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                            .requestMatchers("/home").permitAll()
                            .anyRequest().authenticated())
                .oauth2Login(AbstractAuthenticationFilterConfigurer::permitAll)
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()));
            return http.build();
        }
        ```

        现在，让我们看看如何使用 Spring Security 提供的一个特殊类 OidcClientInitiatedLogoutSuccessHandler 来创建一个 LogoutSuccessHandler：

        ```java
        @Autowired
        private ClientRegistrationRepository clientRegistrationRepository;

        private LogoutSuccessHandler oidcLogoutSuccessHandler() {
            OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(
                this.clientRegistrationRepository);

            oidcLogoutSuccessHandler.setPostLogoutRedirectUri(
            URI.create("http://localhost:8081/home"));

            return oidcLogoutSuccessHandler;
        }
        ```

        因此，我们需要在 OP 客户端配置面板中将此 URI 设置为有效的注销重定向 URI。

        显然，OP 注销配置包含在客户端注册设置中，因为我们配置处理程序所使用的只是上下文中的 ClientRegistrationRepository Bean。

        那么，现在会发生什么呢？

        登录应用程序后，我们可以向 Spring Security 提供的 /logout 端点发送请求。

        如果我们查看浏览器调试控制台中的网络日志，就会发现在最终访问我们配置的重定向 URI 之前，我们被重定向到了一个 OP 注销端点。

        下次访问应用程序中需要身份验证的端点时，我们必须再次登录 OP 平台才能获得权限。

8. 总结

    总之，在本文中，我们了解了 OpenID Connect 提供的大量解决方案，以及如何使用 Spring Security 实现其中的一些方案。
