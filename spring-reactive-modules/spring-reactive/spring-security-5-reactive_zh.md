# [面向反应式应用的Spring Security 5](https://www.baeldung.com/spring-security-5-reactive)

1. 简介

    在这篇文章中，我们将探讨[Spring Security 5框架](https://spring.io/projects/spring-security)的新功能，以确保反应式应用的安全。这个版本与Spring 5和Spring Boot 2保持一致。

    在这篇文章中，我们不会详述反应式应用本身，这是Spring 5框架的一个新特性。请务必查看文章《[Reactor Core介绍](https://www.baeldung.com/reactor-core)》以了解更多细节。

2. Maven设置

    我们将使用Spring Boot启动器将我们的项目与所有需要的依赖项一起启动。

    基本设置需要一个父声明、Web启动器和安全启动器依赖。我们还需要Spring Security测试框架。

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.1</version>
        <relativePath/>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ```

3. 项目设置

    1. 引导反应式应用程序

        我们不会使用标准的@SpringBootApplication配置，而是配置基于Netty的web服务器。Netty是一个基于NIO的异步框架，是反应式应用程序的良好基础。

        @EnableWebFlux注释启用应用程序的标准Spring Web Reactive配置：

        ```java
        @ComponentScan(basePackages = {"com.baeldung.security"})
        @EnableWebFlux
        public class SpringSecurity5Application {
            public static void main(String[] args) {
                try (AnnotationConfigApplicationContext context 
                = new AnnotationConfigApplicationContext(
                    SpringSecurity5Application.class)) {
                    context.getBean(NettyContext.class).onClose().block();
                }
            }
        ```

        在这里，我们创建一个新的应用程序上下文，并通过调用 .onClose().block() 链等待Netty关闭。

        关闭Netty后，将使用 try-with-resources 块自动关闭上下文。

        我们还需要创建基于Netty的HTTP服务器、HTTP请求的处理程序以及服务器和处理程序之间的适配器：

        ```java
        @Bean
        public NettyContext nettyContext(ApplicationContext context) {
            HttpHandler handler = WebHttpHandlerBuilder
            .applicationContext(context).build();
            ReactorHttpHandlerAdapter adapter 
            = new ReactorHttpHandlerAdapter(handler);
            HttpServer httpServer = HttpServer.create("localhost", 8080);
            return httpServer.newHandler(adapter).block();
        }
        ```

    2. Spring安全配置类

        对于我们的基本SpringSecurity配置，我们将创建一个配置类SecurityConfig。

        要在Spring Security 5中启用WebFlux支持，我们只需要指定@EnableWebFluxSecurity注释：

        reactive.security/SecurityConfig.java

        现在，我们可以利用ServerHttpSecurity类来构建安全配置。

        这个类是Spring5的一个新特性。它类似于HttpSecuritybuilder，但它只对WebFlux应用程序启用。

        ServerHttpSecurity已经预先配置了一些正常的默认值，因此我们可以完全跳过此配置。但对于初学者，我们将提供以下最小配置：

        ```java
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http) {
            return http.authorizeExchange()
            .anyExchange().authenticated()
            .and().build();
        }
        ```

        此外，我们还需要一个用户详细信息服务。Spring Security为我们提供了一个方便的模拟用户生成器和用户详细信息服务的内存实现：

        SecurityConfig.MapReactiveUserDetailsService()

        由于我们处于被动状态，用户详细信息服务也应该是被动的。如果我们查看ReactiveUserDetailsService接口，我们将看到它的findByUsername方法实际上返回了Mono发布者：

        ```java
        public interface ReactiveUserDetailsService {
            Mono<UserDetails> findByUsername(String username);
        }
        ```

        现在，我们可以运行应用程序并观察常规的HTTP基本身份验证表单。

4. 风格化的登录表格

    Spring Security 5的一个小但引人注目的改进是使用Bootstrap 4 CSS框架的新风格化登录表。登录表单中的样式表链接到CDN，所以我们只有在连接到互联网时才会看到这种改进。

    为了使用新的登录表单，让我们在ServerHttpSecurity构建器中添加相应的formLogin()构建器方法。

    reactive.security/SecurityConfig.securityWebFilterChain()

    如果我们现在打开应用程序的主页面，我们会发现它看起来比我们从以前的Spring Security版本中习惯的默认表单好得多。

    请注意，这并不是一个可用于生产的表单，但它是我们应用程序的一个很好的引导。

    如果我们现在登录，然后转到 <http://localhost:8080/logout> URL，我们会看到注销确认表单，它也是有风格的。

5. 反应式控制器的安全性

    为了看到认证表单背后的东西，让我们实现一个简单的反应式控制器来迎接用户。

    reactive.security/GreetingController.greet()

    登录后，我们会看到问候语。让我们添加另一个反应式处理程序，只有管理员才能访问。

    reactive.security/GreetingController.greetAdmin()

    现在让我们在我们的用户详情服务中创建第二个角色为ADMIN的用户：

    SecurityConfig.MapReactiveUserDetailsService()

    我们现在可以为管理员的URL添加一个匹配器规则，要求用户拥有ROLE_ADMIN权限。

    注意，我们必须把匹配器放在 .anyExchange() 链调用之前。这个调用适用于所有其他尚未被其他匹配器覆盖的URL。

    见 SecurityConfig.securityWebFilterChain()

    ```java
    UserDetails admin = User.withDefaultPasswordEncoder()
        .username("admin")
        .password("password")
        .roles("ADMIN")
        .build();
    ```

    如果我们现在用用户或管理员登录，我们会看到他们都观察到最初的问候语，因为我们已经让所有认证用户都可以访问它。

    但只有管理员用户可以进入 <http://localhost:8080/admin> URL，看到她的问候语。

6. 反应式方法安全

    我们已经看到了如何确保URL的安全，但是方法呢？

    要为反应式方法启用基于方法的安全，我们只需要在我们的SecurityConfig类中添加@EnableReactiveMethodSecurity注解。

    ```java
    @EnableReactiveMethodSecurity
    public class SecurityConfig {}
    ```

    现在让我们创建一个反应式问候服务，内容如下。

    reactive.security/GreetingService.java

    我们可以把它注入到控制器中，去 <http://localhost:8080/greetingService> ，看看它是否真的能工作。

    GreetingController.greetingService()

    但是如果我们现在用ADMIN角色在服务方法 GreetingService.greet() 上添加@PreAuthorize注解，那么greet服务的URL就不会被普通用户访问。

7. 在测试中Mocking用户

    让我们看看测试我们的反应式Spring应用有多容易。

    首先，我们将创建一个具有注入应用上下文的测试。

    ```java
    @ContextConfiguration(classes = SpringSecurity5Application.class)
    public class SecurityTest {
        @Autowired
        ApplicationContext context;
        // ...
    }
    ```

    现在我们将建立一个简单的反应式网络测试客户端，这是Spring 5测试框架的一个特点。

    SecurityIntegrationTest.setup()

    这使我们能够快速检查未经授权的用户是否从我们应用程序的主页面重定向到登录页面。

    SecurityIntegrationTest.whenNoCredentials_thenRedirectToLogin()

    如果我们现在给一个测试方法添加@WithMockUser注解，我们就可以为这个方法提供一个认证用户。

    这个用户的登录名和密码将分别是user和password，而角色是USER。当然，这些都可以通过@WithMockUser注解的参数来配置。

    现在我们可以检查授权用户是否看到问候语。

    SecurityIntegrationTest.whenHasCredentials_thenSeesGreeting()

    @WithMockUser注解从Spring Security 4开始可用。然而，这在Spring Security 5中也得到了更新，以涵盖反应式端点和方法。
