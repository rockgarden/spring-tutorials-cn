# [使用WebFlux进行Spring Security OAuth登录](https://www.baeldung.com/spring-oauth-login-webflux)

1. 一览表

    从5.1.x GA开始，Spring Security增加了对WebFlux的OAuth支持。

    我们将讨论如何配置我们的WebFlux应用程序以使用OAuth2登录支持。我们还将讨论如何使用WebClient访问OAuth2安全资源。

    Webflux的OAuth登录配置与标准Web MVC应用程序的配置相似。有关此的更多详细信息，也请查看我们关于Spring OAuth2Login元素的[文章](https://www.baeldung.com/spring-security-5-oauth2-login)。

2. Maven配置

    首先，我们将创建一个简单的Spring Boot应用程序，并将这些依赖项添加到我们的pom.xml中：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-oauth2-client</artifactId>
    </dependency>
    ```

    maven Central上提供spring-boot-starter-security、spring-boot-starter-webflux和spring-security-oauth2-client依赖项。

3. 主控制器

    接下来，我们将添加一个简单的控制器，在主页上显示用户名：

    ```java
    @RestController
    public class MainController {
        @GetMapping("/")
        public Mono<String> index(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
        return oauth2User
            .map(OAuth2User::getName)
            .map(name -> String.format("Hi, %s", name));
        }
    }
    ```

    请注意，我们将显示从OAuth2客户端UserInfo端点获得的用户名。

4. 使用谷歌登录

    现在，我们将配置我们的应用程序，以支持使用谷歌登录。

    首先，我们需要在[谷歌开发者控制台](https://console.developers.google.com/)上创建一个新项目

    现在，我们需要添加OAuth2凭据（Create Credentials > OAuth Client ID）。

    接下来，我们将将此添加到“授权重定向URI”中：

    <http://localhost:8080/login/oauth2/code/google>

    然后，我们需要配置我们的application.yml来使用客户端ID和Secret：

    ```yml
    spring:
    security:
        oauth2:
        client:
            registration:
            google:
                client-id: YOUR_APP_CLIENT_ID
                client-secret: YOUR_APP_CLIENT_SECRET
    ```

    由于我们的路径上有spring-security-oauth2-client，我们的应用程序将得到保护。

    用户将被重定向到使用谷歌登录，然后才能访问我们的主页。

5. 使用授权提供商登录

    我们还可以将应用程序配置为从自定义授权服务器登录。

    在以下示例中，我们将使用上一篇[文章](https://www.baeldung.com/rest-api-spring-oauth2-angularjs)中的授权服务器。

    这一次，我们需要配置更多属性，而不仅仅是客户端ID和客户端密钥：

    ```yml
    spring:
    security:
        oauth2:
        client:
            registration:
            custom:
                client-id: fooClientIdPassword
                client-secret: secret
                scopes: read,foo
                authorization-grant-type: authorization_code
                redirect-uri-template: <http://localhost:8080/login/oauth2/code/custom>
            provider:
            custom:
                authorization-uri: <http://localhost:8081/spring-security-oauth-server/oauth/authorize>
                token-uri: <http://localhost:8081/spring-security-oauth-server/oauth/token>
                user-info-uri: <http://localhost:8088/spring-security-oauth-resource/users/extra>
                user-name-attribute: user_name
    ```

    在这种情况下，我们还需要为OAuth2客户端指定范围、授予类型和重定向URI。我们还将提供授权服务器的授权和令牌URI。

    最后，我们还需要配置UserInfo端点，以便能够获得用户身份验证详细信息。

6. 安全配置

    默认情况下，Spring Security保护所有路径。因此，如果我们只有一个OAuth客户端，我们将被重定向到授权此客户端并登录。

    如果注册了多个OAuth客户端，那么将自动创建一个登录页面来选择登录方法。

    如果我们喜欢，我们可以更改它，并提供详细的安全配置：

    ```java
    @EnableWebFluxSecurity
    public class SecurityConfig {
        @Bean
        public SecurityWebFilterChain configure(ServerHttpSecurity http) throws Exception {
            return http.authorizeExchange(auth -> auth
                    .pathMatchers("/about").permitAll()
                    .anyExchange().authenticated())
                    .oauth2Login(Customizer.withDefaults())
                    .build();
        }
    }
    ```

    在本例中，我们已经确保了除“/about”以外的所有路径。

7. 网络客户端

    我们还可以做的不仅仅是使用OAuth2对用户进行身份验证。我们可以使用WebClient使用OAuth2AuthorizedClient访问OAuth2安全资源。

    现在，让我们配置我们的WebClient：

    ```java
    @Bean
    public WebClient webClient(ReactiveClientRegistrationRepository clientRegistrationRepo,
    ServerOAuth2AuthorizedClientRepository authorizedClientRepo) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
        new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepo, authorizedClientRepo);
        return WebClient.builder().filter(filter).build();
    }
    ```

    然后，我们可以检索OAuth2安全资源：

    ```java
    @Autowired
    private WebClient webClient;

    @GetMapping("/foos/{id}")
    public Mono<Foo> getFooResource(@RegisteredOAuth2AuthorizedClient("custom")
    OAuth2AuthorizedClient client, @PathVariable final long id){
        return webClient
        .get()
        .uri("<http://localhost:8088/spring-security-oauth-resource/foos/{id}>", id)
        .attributes(oauth2AuthorizedClient(client))
        .retrieve()
        .bodyToMono(Foo.class);
    }
    ```

    请注意，我们使用OAuth2AuthorizedClient的AccessToken检索了远程资源Foo。

8. 结论

    在这篇短文中，我们学习了如何配置WebFlux应用程序以使用OAuth2登录支持，以及如何使用WebClient访问OAuth2安全资源。
