# [在 Spring Boot 中禁用 Keycloak 安全性](https://www.baeldung.com/spring-keycloak-security-disable)

1. 概述

    Keycloak 是一个免费的开源身份和访问管理程序，目前经常用于我们的软件栈中。在测试阶段，禁用它以专注于业务测试可能会很有用。我们的测试环境中可能也没有 Keycloak 服务器。

    在本教程中，我们将禁用 Keycloak 启动器设置的配置。我们还将了解在项目中启用 Spring Security 时如何对其进行修改。

2. 在非 Spring 安全环境中禁用 Keycloak

    我们先来看看如何在不使用 Spring Security 的应用程序中禁用 Keycloak。

    1. 应用程序设置

        首先，让我们在项目中添加 spring-boot-starter-oauth2-client 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
        </dependency>
        ```

        此外，我们还需要添加 spring-boot-starter-oauth2-resource-server 依赖项。它将允许我们通过 Keycloak 服务器验证 JWT 令牌。因此，让我们把它添加到 pom 中：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        ```

        接下来，我们将在 application.properties 中添加 Keycloak 服务器的配置：

        ```properties
        spring.security.oauth2.client.registration.keycloak.client-id=login-app
        spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
        spring.security.oauth2.client.registration.keycloak.scope=openid
        spring.security.oauth2.client.provider.keycloak.issuer-uri=
            http://localhost:8080/realms/SpringBootKeycloak
        spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username

        spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/SpringBootKeycloak
        ```

        最后，让我们添加一个 UserController 来检索用户：

        ![UserController](/src/main/java/com/baeldung/disablingkeycloak/UserController.java)

    2. 禁用 Keycloak

        现在我们的应用程序已经就绪，让我们编写一个简单的测试来获取用户：

        ```java
        @Test
        public void givenUnauthenticated_whenGettingUser_shouldReturnUser() {
            ResponseEntity<User> responseEntity = restTemplate.getForEntity("/users/1", User.class);

            assertEquals(HttpStatus.SC_OK, responseEntity.getStatusCodeValue());
            assertNotNull(responseEntity.getBody()
                .getFirstname());
        }
        ```

        这个测试会失败，因为我们没有向 restTemplate 提供任何身份验证，或者因为 Keycloak 服务器不可用。

        Keycloak 适配器实现了 Spring 对 Keycloak 安全性的[自动配置](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.auto-configuration)。自动配置依赖于类路径中存在的类或属性值。具体来说，@ConditionalOnProperty 注解对于这种特殊需求非常方便。

        要禁用 Keycloak 安全性，我们需要通知适配器不要加载相应的配置。我们可以通过如下属性赋值来实现这一目的：

        keycloak.enabled=false

        如果我们再次启动测试，现在无需任何身份验证即可成功。

3. 在 Spring 安全环境中禁用 Keycloak

    我们经常将 Keycloak 与 Spring Security 结合使用。在这种情况下，仅禁用 Keycloak 配置是不够的，我们还需要修改 Spring Security 配置，以允许匿名请求到达控制器。

    1. 应用程序设置

        首先，让我们在项目中添加 Spring-boot-starter-security 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        ```

        接下来，我们创建一个 SecurityFilterChain Bean 来定义 Spring Security 所需的配置：

        ![KeycloakSecurityConfig](/src/main/java/com/baeldung/disablingkeycloak/KeycloakSecurityConfig.java)

        在此，我们将 Spring Security 配置为只允许来自已通过身份验证的用户的请求。

    2. 禁用 Keycloak

        除了像之前那样禁用 Keycloak 外，我们现在还需要禁用 Spring Security。

        我们可以使用 配置文件 告诉 Spring 在测试期间是否激活 Keycloak 配置：

        ```java
        @Configuration
        @EnableWebSecurity
        @Profile("tests")
        public class KeycloakSecurityConfig {}
        ```

        不过，更优雅的方法是重用 keycloak.enable 属性，类似于 Keycloak 适配器：

        ```java
        @Configuration
        @EnableWebSecurity
        @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = true)
        public class KeycloakSecurityConfig {}
        ```

        因此，只有当 keycloak.enable 属性为 true 时，Spring 才会启用 Keycloak 配置。如果该属性缺失，matchIfMissing 会默认启用它。

        由于我们使用的是 Spring Security 启动器，因此仅禁用 Spring Security 配置是不够的。事实上，根据 Spring 的默认配置原则，启动器会创建一个默认的安全层。

        让我们创建一个配置类来禁用它：

        ![DisableSecurityConfiguration](/src/main/java/com/baeldung/disablingkeycloak/DisableSecurityConfiguration.java)

        我们仍在使用 keycloak.enable 属性，但这次 Spring 会在其值设为 false 时启用配置。

4. 结论

    在本文中，我们介绍了如何在 Spring 环境中禁用 Keycloak 安全性，无论是否使用 Spring Security。
