# [Spring Cloud - 确保服务安全](https://www.baeldung.com/spring-cloud-securing-services)

1. 概述

    在上一篇文章 “[Spring Cloud - 引导](https://www.baeldung.com/spring-cloud-bootstrapping)” 中，我们构建了一个基本的 Spring Cloud 应用程序。本文将介绍如何确保其安全。

    我们将使用 Spring Session 和 Redis 来共享会话。这种方法设置简单，易于扩展到许多业务场景。如果你不熟悉 Spring Session，请查看本文。

    共享会话使我们能够在网关服务中登录用户，并将该身份验证传播到系统中的任何其他服务。

    如果你对 Redis 或 Spring Security 不熟悉，最好先快速复习一下这些主题。虽然这篇文章的大部分内容都可以复制粘贴到应用程序中，但了解引擎盖下发生了什么是无法替代的。

    如需了解 Redis，请阅读[本教程](https://www.baeldung.com/spring-data-redis-tutorial)。要了解 Spring Security，请阅读 [spring-security-login](https://www.baeldung.com/spring-security-login)、[role-and-privilege-for-spring-security-registration](https://www.baeldung.com/role-and-privilege-for-spring-security-registration) 和 [spring-security-session](https://www.baeldung.com/spring-security-session)。要全面了解 Spring Security，请阅读 [learn-spring-security-the-master class](http://courses.baeldung.com/p/learn-spring-security-the-master-class)。

2. Maven 设置

    首先，让我们为系统中的每个模块添加 spring-boot-starter-security 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    ```

    因为我们使用了 Spring 依赖关系管理，所以可以省略 spring-boot-starter 依赖关系的版本。

    第二步，让我们修改带有 spring-session、spring-boot-starter-data-redis 依赖项的每个应用程序的 pom.xml：

    ```xml
    <dependency>
        <groupId>org.springframework.session</groupId>
        <artifactId>spring-session-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    ```

    只有四个应用程序将与 Spring Session 关联：发现、网关、图书服务和评级服务。

    接下来，在与主应用程序文件相同目录下的所有三个服务中添加会话配置类：

    ```java
    @EnableRedisHttpSession
    public class SessionConfig
    extends AbstractHttpSessionApplicationInitializer {
    }
    ```

    请注意，对于网关服务，我们需要使用不同的注解，即 @EnableRedisWebSession。

    最后，将这些属性添加到 git 仓库中的三个 *.properties 文件中：

    ```properties
    spring.redis.host=localhost
    spring.redis.port=6379
    ```

    现在让我们进入服务的具体配置。

3. 确保配置服务的安全

    配置服务包含敏感信息，通常与数据库连接和 API 密钥有关。我们不能泄露这些信息，因此让我们直接进入并确保该服务的安全。

    让我们在配置服务的 src/main/resources 中的 application.properties 文件中添加安全属性：

    ```properties
    eureka.client.serviceUrl.defaultZone=
    http://discUser:discPassword@localhost:8082/eureka/
    security.user.name=configUser
    security.user.password=configPassword
    security.user.role=SYSTEM
    ```

    这将设置我们的服务使用发现登录。此外，我们还通过 application.properties 文件配置了安全性。

    现在让我们配置发现服务。

4. 确保发现服务的安全

    我们的发现服务拥有关于应用程序中所有服务位置的敏感信息。它还会注册这些服务的新实例。

    如果恶意客户端获得访问权限，他们就会了解我们系统中所有服务的网络位置，并能在我们的应用程序中注册自己的恶意服务。因此，确保发现服务的安全至关重要。

    1. 安全配置

        让我们添加一个安全过滤器来保护其他服务将使用的端点：

        [SecurityConfig](./discovery/src/main/java/com/baeldung/spring/cloud/bootstrap/discovery/SecurityConfig.java)

        这将为我们的服务设置一个 “SYSTEM” 用户。这是一个基本的 Spring 安全配置，但有一些变化。让我们来看看这些变化：

        - .sessionCreationPolicy（会话创建策略）- 告知 Spring 在用户登录此过滤器时始终创建一个会话
        - .requestMatchers（请求匹配器）--限制此过滤器适用于哪些端点

        我们刚刚设置的安全过滤器配置了一个隔离的身份验证环境，该环境只与发现服务有关。

    2. 保护 Eureka 控制面板

        由于我们的发现应用程序有一个很好的用户界面来查看当前注册的服务，因此让我们使用第二个安全过滤器将其公开，并将此安全过滤器与应用程序其他部分的身份验证绑定在一起。请记住，没有 @Order() 标记意味着这是最后一个要评估的安全过滤器：

        ```java
        @Configuration
        public static class AdminSecurityConfig {

            public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                auth.inMemoryAuthentication();
            }

            protected void configure(HttpSecurity http) throws Exception {
                http.sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                    .httpBasic(basic -> basic.disable())
                    .authorizeRequests()
                    .requestMatchers(HttpMethod.GET, "/").hasRole("ADMIN")
                    .requestMatchers("/info", "/health").authenticated()
                    .anyRequest().denyAll()
                    .and().csrf(csrf -> csrf.disable());
            }
        }
        ```

        在 SecurityConfig 类中添加此配置类。这将创建第二个安全过滤器，控制对用户界面的访问。这个过滤器有一些不同寻常的特性，让我们来看看：

        - httpBasic().disable()--告诉 Spring Security 禁用该过滤器的所有身份验证程序
        - 会话创建策略（sessionCreationPolicy）--我们将其设置为 “NEVER”，以表示我们要求用户在访问受该过滤器保护的资源前已通过身份验证

        该过滤器永远不会设置用户会话，而是依赖 Redis 来填充共享安全上下文。因此，它依赖于另一项服务（网关）来提供身份验证。

    3. 使用配置服务进行身份验证

        在发现项目中，让我们在 src/main/resources 的 bootstrap.properties 中添加两个属性：

        ```properties
        spring.cloud.config.username=configUser
        spring.cloud.config.password=configPassword
        ```

        这些属性将允许发现服务在启动时与配置服务进行身份验证。

        让我们更新 Git 仓库中的 discovery.properties

        ```properties
        eureka.client.serviceUrl.defaultZone=
        http://discUser:discPassword@localhost:8082/eureka/
        eureka.client.register-with-eureka=false
        eureka.client.fetch-registry=false
        ```

        我们为发现服务添加了基本身份验证凭据，使其能够与配置服务通信。此外，我们还配置了Eureka，让它以独立模式运行，告诉我们的服务不要自己注册。

        让我们把文件提交到 git 仓库。否则，更改将无法被检测到。

5. 确保网关服务的安全

    我们的网关服务是应用程序中唯一要向全世界公开的部分。因此，它需要安全保护，以确保只有经过身份验证的用户才能访问敏感信息。

    1. 安全配置

        让我们创建一个与发现服务类似的 SecurityConfig 类，并用以下内容覆盖其方法：

        [SecurityConfig](./gateway/src/main/java/com/baeldung/spring/cloud/bootstrap/gateway/SecurityConfig.java)

        这项配置非常简单。我们用表单登录声明了一个安全过滤器，以确保各种端点的安全。

        /eureka/** 上的安全性是为了保护我们将从网关服务中为 Eureka 状态页面提供的一些静态资源。如果您正在与文章一起构建项目，请将 Github 上网关项目中的 resource/static 文件夹复制到您的项目中。

        现在，我们必须添加 @EnableRedisWebSession：

        ```java
        @Configuration
        @EnableRedisWebSession
        public class SessionConfig {}
        ```

        Spring Cloud Gateway 过滤器会自动抓取登录后重定向的请求，并将会话密钥作为 cookie 添加到头信息中。这将在登录后向任何后备服务传播身份验证。

    2. 使用配置和发现服务进行身份验证

        让我们在网关服务 src/main/resources 中的 bootstrap.properties 文件中添加以下身份验证属性：

        ```properties
        spring.cloud.config.username=configUser
        spring.cloud.config.password=configPassword
        eureka.client.serviceUrl.defaultZone=
        http://discUser:discPassword@localhost:8082/eureka/
        ```

        接下来，更新 Git 仓库中的 gateway.properties

        ```properties
        management.security.sessions=always
        spring.redis.host=localhost
        spring.redis.port=6379
        ```

        我们已将会话管理添加为始终生成会话，因为我们只有一个安全过滤器，可以在属性文件中进行设置。接下来，我们添加 Redis 主机和服务器属性。

        我们可以从配置 git 仓库中的 gateway.properties 文件移除 serviceUrl.defaultZone 属性。这个值在引导文件中被复制了。

        将文件提交到 Git 仓库，否则将无法检测到更改。

6. 确保图书服务的安全

    图书服务服务器将保存由不同用户控制的敏感信息。必须确保该服务的安全，以防止系统中受保护信息的泄漏。

    1. 安全配置

        为了确保图书服务的安全，我们将从网关中复制 SecurityConfig 类，并用以下内容覆盖该方法：

        [SecurityConfig](./zipkin-log-svc-book/src/main/java/com/baeldung/spring/cloud/bootstrap/svcbook/SecurityConfig.java)

    2. 属性

        将这些属性添加到图书服务 src/main/resources 中的 bootstrap.properties 文件：

        ```properties
        spring.cloud.config.username=configUser
        spring.cloud.config.password=configPassword
        eureka.client.serviceUrl.defaultZone=
        http://discUser:discPassword@localhost:8082/eureka/
        ```

        让我们在 git 仓库的 book-service.properties 文件中添加属性：

        `management.security.sessions=never`

        我们可以删除配置 git 仓库中 book-service.properties 文件的 serviceUrl.defaultZone 属性。这个值在引导文件中是重复的。

        记得提交这些改动，以便 book-service 能接收它们。

7. 确保评级服务的安全

    还需要确保评级服务的安全。

    1. 安全配置

        为了确保评级服务的安全，我们将复制网关的 SecurityConfig 类，并用以下内容覆盖该方法：

        [SecurityConfig](./zipkin-log-svc-rating/src/main/java/com/baeldung/spring/cloud/bootstrap/svcrating/SecurityConfig.java)

        我们可以删除网关服务中的 configureGlobal() 方法。

    2. 属性

        将这些属性添加到评级服务 src/main/resources 中的 bootstrap.properties 文件：

        ```properties
        spring.cloud.config.username=configUser
        spring.cloud.config.password=configPassword
        eureka.client.serviceUrl.defaultZone=
        http://discUser:discPassword@localhost:8082/eureka/
        ```

        让我们在 git 仓库中的 rating-service.properties 文件中添加属性：

        `management.security.sessions=never`

        我们可以删除配置 git 仓库中 rating-service.properties 文件的 serviceUrl.defaultZone 属性。这个值在引导文件中是重复的。

        记住要提交这些更改，以便评级服务将其接收。

8. 运行和测试

    启动 Redis 和应用程序的所有服务：config、discovery、gateway、book-service 和 rating-service。现在开始测试！

    首先，让我们在网关项目中创建一个测试类，并为测试创建一个方法：

    ```java
    public class GatewayApplicationLiveTest {
        @Test
        public void testAccess() {
            ...
        }
    }
    ```

    接下来，让我们设置测试，并在测试方法中添加以下代码片段，以验证我们可以访问不受保护的 /book-service/books 资源：

    ```java
    TestRestTemplate testRestTemplate = new TestRestTemplate();
    String testUrl = "http://localhost:8080";

    ResponseEntity<String> response = testRestTemplate
    .getForEntity(testUrl + "/book-service/books", String.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertNotNull(response.getBody());
    ```

    运行此测试并验证结果。如果出现失败，请确认整个应用程序已成功启动，并且配置已从配置 git 仓库加载。

    现在，让我们在测试方法的末尾添加以下代码，测试用户在以未认证用户身份访问受保护资源时是否会被重定向登录：

    ```java
    response = testRestTemplate
    .getForEntity(testUrl + "/home/index.html", String.class);
    Assert.assertEquals(HttpStatus.FOUND, response.getStatusCode());
    Assert.assertEquals("http://localhost:8080/login", response.getHeaders()
    .get("Location").get(0));
    ```

    再次运行测试，确认测试成功。

    接下来，让我们实际登录，然后使用会话访问受用户保护的结果：

    ```java
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("username", "user");
    form.add("password", "password");
    response = testRestTemplate
    .postForEntity(testUrl + "/login", form, String.class);
    ```

    现在，让我们从 cookie 中提取会话，并将其传播到下面的请求中：

    ```java
    String sessionCookie = response.getHeaders().get("Set-Cookie")
    .get(0).split(";")[0];
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", sessionCookie);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);
    ```

    并请求受保护的资源：

    ```java
    response = testRestTemplate.exchange(testUrl + "/book-service/books/1",
    HttpMethod.GET, httpEntity, String.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertNotNull(response.getBody());
    ```

    再次运行测试以确认结果。

    现在，让我们尝试使用同一会话访问管理部分：

    ```java
    response = testRestTemplate.exchange(testUrl + "/rating-service/ratings/all",
    HttpMethod.GET, httpEntity, String.class);
    Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    ```

    再次运行测试，不出所料，我们被限制以普通用户身份访问管理区。

    下一个测试将验证我们能否以管理员身份登录并访问受管理员保护的资源：

    ```java
    form.clear();
    form.add("username", "admin");
    form.add("password", "admin");
    response = testRestTemplate
    .postForEntity(testUrl + "/login", form, String.class);

    sessionCookie = response.getHeaders().get("Set-Cookie").get(0).split(";")[0];
    headers = new HttpHeaders();
    headers.add("Cookie", sessionCookie);
    httpEntity = new HttpEntity<>(headers);

    response = testRestTemplate.exchange(testUrl + "/rating-service/ratings/all",
    HttpMethod.GET, httpEntity, String.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertNotNull(response.getBody());
    ```

    我们的测试越来越大了！但我们可以看到，通过以管理员身份登录，我们可以访问管理员资源。

    我们的最后一项测试是通过网关访问发现服务器。为此，请在测试末尾添加以下代码：

    ```java
    response = testRestTemplate.exchange(testUrl + "/discovery",
    HttpMethod.GET, httpEntity, String.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    ```

    最后运行一次测试，确认一切正常。成功了

    你错过了吗？因为我们登录了网关服务，并查看了图书、评级和发现服务的内容，而无需在四个不同的服务器上登录！

    通过使用 Spring Session 在服务器之间传播我们的身份验证对象，我们可以在网关上登录一次，然后使用该身份验证访问任意数量后备服务上的控制器。

9. 结论

    云计算的安全性无疑变得更加复杂。但在 Spring Security 和 Spring Session 的帮助下，我们可以轻松解决这一关键问题。

    现在，我们的云应用程序已经具备了围绕服务的安全性。使用 Spring Cloud Gateway 和 Spring Session，我们可以只在一个服务中登录用户，并将身份验证传播到整个应用程序。这意味着我们可以轻松地将应用程序分成适当的域，并根据我们的需要确保每个域的安全。
