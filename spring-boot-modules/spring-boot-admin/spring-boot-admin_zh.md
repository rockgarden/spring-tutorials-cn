# [Spring Boot 管理指南](https://www.baeldung.com/spring-boot-admin)

1. 概述

    [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) 是一个网络应用程序，用于管理和监控 Spring Boot 应用程序。每个应用程序都被视为客户端并注册到管理服务器。在幕后，Spring Boot Actuator 端点发挥着神奇的作用。

    在本文中，我们将介绍配置 Spring Boot Admin 服务器的步骤，以及应用程序如何成为客户端。

2. 管理服务器设置

    首先，我们需要创建一个简单的 Spring Boot Web 应用程序，并添加以下 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
        <version>3.1.5</version>
    </dependency>
    ```

    之后，@EnableAdminServer 将可用，因此我们将把它添加到主类中，如下例所示：

    ```java
    @EnableAdminServer
    @SpringBootApplication
    public class SpringBootAdminServerApplication(exclude = AdminServerHazelcastAutoConfiguration.class) {
        public static void main(String[] args) {
            SpringApplication.run(SpringBootAdminServerApplication.class, args);
        }
    }
    ```

    至此，我们就可以启动服务器并注册客户端应用程序了。

3. 设置客户端

    设置好管理服务器后，我们就可以将第一个 Spring Boot 应用程序注册为客户端了。我们必须添加以下 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-client</artifactId>
        <version>3.1.5</version>
    </dependency>
    ```

    接下来，我们需要配置客户端以了解管理服务器的基本 URL。为此，我们只需添加以下属性：

    `spring.boot.admin.client.url=http://localhost:8080`

    从 Spring Boot 2 开始，除 health 和 info 以外的端点默认不公开。

    让我们公开所有端点：

    ```properties
    management.endpoints.web.exposure.include=*
    management.endpoint.health.show-details=always
    ```

4. 安全配置

    Spring Boot Admin 服务器可以访问应用程序的敏感端点，因此建议我们在管理员和客户端应用程序中添加一些安全配置。

    首先，我们将重点配置管理员服务器的安全性。我们必须添加以下 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-server-ui</artifactId>
        <version>1.5.7</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <version>3.1.5</version>
    </dependency>
    ```

    这将启用安全性并为管理应用程序添加登录界面。确保获得最新版本的管理应用程序。

    接下来，我们将添加一个安全配置类，如下所示：

    ```java
    @Configuration
    @EnableWebSecurity
    public class WebSecurityConfig {

        private final AdminServerProperties adminServer;

        public WebSecurityConfig(AdminServerProperties adminServer) {
            this.adminServer = adminServer;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
            successHandler.setTargetUrlParameter("redirectTo");
            successHandler.setDefaultTargetUrl(this.adminServer.getContextPath() + "/");

            http.authorizeHttpRequests(req -> req.requestMatchers(this.adminServer.getContextPath() + "/assets/**")
                    .permitAll()
                    .requestMatchers(this.adminServer.getContextPath() + "/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
                .formLogin(formLogin -> formLogin.loginPage(this.adminServer.getContextPath() + "/login")
                    .successHandler(successHandler))
                .logout((logout) -> logout.logoutUrl(this.adminServer.getContextPath() + "/logout"))
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                        new AntPathRequestMatcher(this.adminServer.getContextPath() + "/instances", HttpMethod.POST.toString()),
                        new AntPathRequestMatcher(this.adminServer.getContextPath() + "/instances/*", HttpMethod.DELETE.toString()),
                        new AntPathRequestMatcher(this.adminServer.getContextPath() + "/actuator/**")))
                .rememberMe(rememberMe -> rememberMe.key(UUID.randomUUID()
                        .toString())
                    .tokenValiditySeconds(1209600));
            return http.build();
        }
    }
    ```

    这是一个简单的安全配置，但添加后，我们会发现客户端无法再向服务器注册。

    为了将客户端注册到新的安全服务器上，我们必须在客户端的属性文件中添加更多配置：

    ```properties
    spring.boot.admin.client.username=admin
    spring.boot.admin.client.password=admin
    ```

    现在我们已经确保了管理服务器的安全。在生产系统中，我们要监控的应用程序自然也是安全的。因此，我们也要为客户端添加安全性--在管理服务器的用户界面上，我们会发现客户端信息已经不可用了。

    我们必须添加一些元数据，并将其发送到管理服务器。服务器将使用这些信息连接客户端的端点：

    ```properties
    spring.security.user.name=client
    spring.security.user.password=client
    spring.boot.admin.client.instance.metadata.user.name=${spring.security.user.name}
    spring.boot.admin.client.instance.metadata.user.password=${spring.security.user.password}
    ```

    当然，通过 HTTP 发送凭据并不安全，因此需要通过 HTTPS 进行通信。

5. 监控和管理功能

    Spring Boot Admin 可以配置为只显示我们认为有用的信息。我们只需更改默认配置并添加自己需要的指标即可：

    `spring.boot.admin.routes.endpoints=env, metrics, trace, jolokia, info, configprops`

    随着我们的深入，我们会发现还有其他一些功能可以探索。我们将讨论使用 Jolokia 进行 JMX Bean 管理，以及 Loglevel 管理。

    Spring Boot Admin 还支持使用 Hazelcast 进行集群复制。我们只需添加以下 Maven 依赖项，剩下的就交给自动配置吧：

    ```xml
    <dependency>
        <groupId>com.hazelcast</groupId>
        <artifactId>hazelcast</artifactId>
        <version>4.0.3</version>
    </dependency>
    ```

    如果我们需要 Hazelcast 的持久实例，我们将使用自定义配置：

    ```java
    @Configuration
    public class HazelcastConfig {

        @Bean
        public Config hazelcast() {
            MapConfig eventStoreMap = new MapConfig("spring-boot-admin-event-store")
            .setInMemoryFormat(InMemoryFormat.OBJECT)
            .setBackupCount(1)
            .setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.NONE))
            .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

            MapConfig sentNotificationsMap = new MapConfig("spring-boot-admin-application-store")
            .setInMemoryFormat(InMemoryFormat.OBJECT)
            .setBackupCount(1)
            .setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU))
            .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

            Config config = new Config();
            config.addMapConfig(eventStoreMap);
            config.addMapConfig(sentNotificationsMap);
            config.setProperty("hazelcast.jmx", "true");

            config.getNetworkConfig()
            .getJoin()
            .getMulticastConfig()
            .setEnabled(false);
            TcpIpConfig tcpIpConfig = config.getNetworkConfig()
            .getJoin()
            .getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            tcpIpConfig.setMembers(Collections.singletonList("127.0.0.1"));
            return config;
        }
    }
    ```

6. 通知

    接下来，让我们讨论一下在注册客户端出现问题时从管理服务器接收通知的可能性。以下是可供配置的通知：

    - Email
    - PagerDuty
    - OpsGenie
    - Hipchat
    - Slack
    - Let’s Chat

    1. 邮件通知

        我们首先要为管理服务器配置邮件通知。为此，我们必须添加邮件启动器依赖项，如下所示：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
            <version>2.4.0</version>
        </dependency>
        ```

        之后，我们必须添加一些邮件配置：

        ```properties
        spring.mail.host=smtp.example.com
        spring.mail.username=smtp_user
        spring.mail.password=smtp_password
        spring.boot.admin.notify.mail.to=admin@example.com
        ```

        现在，每当注册客户端的状态从 UP 变为 OFFLINE 或其他状态时，就会向上述配置的地址发送一封电子邮件。其他通知器的配置与此类似。

    2. Hipchat 通知

        正如我们将看到的，与 Hipchat 的集成非常简单，只需设置几个必须的属性：

        ```properties
        spring.boot.admin.notify.hipchat.auth-token=<generated_token>
        spring.boot.admin.notify.hipchat.room-id=<room-id>
        spring.boot.admin.notify.hipchat.url=https://yourcompany.hipchat.com/v2/
        ```

        有了这些定义，我们就会发现在 Hipchat 聊天室中，每当客户端状态发生变化时，我们就会收到通知。

    3. 自定义通知配置

        我们可以使用一些强大的工具配置自定义通知系统。我们可以使用提醒通知器发送预定通知，直到客户状态发生变化。

        或者，我们想向一组经过筛选的客户发送通知。为此，我们可以使用过滤通知器：

        ```java
        @Configuration
        public class NotifierConfiguration {
            private final InstanceRepository repository;
            private final ObjectProvider<List<Notifier>> otherNotifiers;

            public NotifierConfiguration(InstanceRepository repository, 
            ObjectProvider<List<Notifier>> otherNotifiers) {
                this.repository = repository;
                this.otherNotifiers = otherNotifiers;
            }

            @Bean
            public FilteringNotifier filteringNotifier() {
                CompositeNotifier delegate = 
                new CompositeNotifier(this.otherNotifiers.getIfAvailable(Collections::emptyList));
                return new FilteringNotifier(delegate, this.repository);
            }

            @Bean
            public LoggingNotifier notifier() {
                return new LoggingNotifier(repository);
            }

            @Primary
            @Bean(initMethod = "start", destroyMethod = "stop")
            public RemindingNotifier remindingNotifier() {
                RemindingNotifier remindingNotifier = new RemindingNotifier(filteringNotifier(), repository);
                remindingNotifier.setReminderPeriod(Duration.ofMinutes(5));
                remindingNotifier.setCheckReminderInverval(Duration.ofSeconds(60));
                return remindingNotifier;
            }
        }
        ```

7. 结论

    本教程介绍了使用 Spring Boot Admin 监控和管理 Spring Boot 应用程序的简单步骤。

    通过自动配置，我们只需添加一些次要配置，最后就能拥有一个完全正常工作的管理服务器。
