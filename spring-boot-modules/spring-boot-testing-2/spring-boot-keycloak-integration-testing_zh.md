# [使用测试容器进行 Spring Boot - Keycloak 集成测试](https://www.baeldung.com/spring-boot-keycloak-integration-testing)

1. 简介

    集成测试对于验证应用程序是否正常运行至关重要。此外，我们还应该正确测试身份验证，因为这是一个敏感的部分。测试容器允许我们在测试阶段启动 Docker 容器，针对实际技术栈运行测试。

    在本文中，我们将了解如何使用 Testcontainers 针对实际的 Keycloak 实例设置集成测试。

2. 使用 Keycloak 设置 Spring 安全性

    我们需要设置 Spring Security、Keycloak 配置以及 Testcontainers。

    1. 设置 Spring Boot 和 Spring Security

        让我们从设置 Spring Security 的安全性开始。我们需要 Spring-boot-starter-security 依赖项。因此，让我们将其添加到 pom 中：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        ```

        我们将使用 spring-boot 的父 pom。因此，我们不需要在依赖项管理中指定库的版本。

        接下来，让我们创建一个简单的控制器来返回用户：

        ![UserController.java](/src/main/java/com/baeldung/keycloaktestcontainers/controller/UserController.java)

        至此，我们就有了一个安全控制器，可以响应"/users/me"上的请求。启动应用程序时，Spring Security 会为用户"user"生成一个密码，该密码在应用程序日志中可见。

    2. 配置 Keycloak

        启动本地 Keycloak 的最简单方法是使用 Docker。因此，让我们运行一个已经配置好管理员账户的 Keycloak 容器：

        `docker run -p 8081:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:17.0.1 start-dev`

        让我们打开浏览器，访问 URL <http://localhost:8081>，以访问问 Keycloak 控制台：

        接下来，创建我们的领域(realm)。我们称之为 baeldung。

        我们需要添加一个客户端(client)，命名为 baeldung-api。

        最后，使用用户(Users)菜单添加一个Jane Doe用户。

        创建用户后，我们必须为其分配一个密码。选择 s3cr3t，取消选中temporary 按钮。

        现在我们已经用 baeldung-api 客户端和 Jane Doe 用户建立了 Keycloak 领域。

        接下来，我们将配置 Spring 使用 Keycloak 作为身份提供者。

    3. 将两者结合起来

        首先，我们将把身份控制委托给 Keycloak 服务器。为此，我们将使用 spring-boot-starter-oauth2-resource-server 库。它将允许我们通过 Keycloak 服务器验证 JWT 令牌。因此，让我们把它添加到 pom 中：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>
        ```

        让我们继续配置 Spring Security 以添加 OAuth 2 资源服务器支持：

        ![WebSecurityConfiguration.java](/src/main/java/com/baeldung/keycloaktestcontainers/configuration/WebSecurityConfiguration.java)

        我们正在设置一个新的过滤链，它将应用于所有传入请求。它将根据我们的 Keycloak 服务器验证绑定的 JWT 令牌。

        由于我们正在构建一个只进行承载验证的无状态应用程序，因此我们将使用 NullAuthenticatedSessionStrategy 作为会话策略。此外，@ConditionalOnProperty 允许我们通过将 keycloak.enabled 属性设置为 false 来[禁用 Keycloak 配置](https://www.baeldung.com/spring-keycloak-security-disable)。

        最后，让我们在 application.properties 文件中添加连接到 Keycloak 所需的配置：

        ```properties
        keycloak.enabled=true
        spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/auth/realms/baeldung-api
        ```

        现在我们的应用程序安全了，每次请求都会查询 Keycloak 以验证身份验证。

3. 为 Keycloak 设置测试容器

    1. 导出域配置

        Keycloak 容器启动时没有任何配置。因此，我们必须在容器启动时以 JSON 文件的形式导入配置。让我们从当前运行的实例中导出(Export)该文件。

        遗憾的是，Keycloak 无法通过管理界面导出用户。我们可以登录容器并使用 kc.sh 导出命令。在我们的例子中，手动编辑 realm-export.json 文件并添加无名氏会更简单。让我们在最后一个大括号之前添加此配置：

        ```json
        "users": [
        {
            "username": "janedoe",
            "email": "jane.doe@baeldung.com",
            "firstName": "Jane",
            "lastName": "Doe",
            "enabled": true,
            "credentials": [
            {
                "type": "password",
                "value": "s3cr3t"
            }
            ],
            "clientRoles": {
            "account": [
                "view-profile",
                "manage-account"
            ]
            }
        }
        ]
        ```

        将 realm-export.json 文件添加到项目的 src/test/resources/keycloak 文件夹中。我们将在启动 Keycloak 容器时使用它。

    2. 设置测试容器

        让我们添加 testcontainers 依赖项和 testcontainers-keycloak，它允许我们启动 Keycloak 容器：

        ```xml
        <dependency>
            <groupId>com.github.dasniko</groupId>
            <artifactId>testcontainers-keycloak</artifactId>
            <version>2.1.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.16.3</version>
        </dependency>
        ```

        接下来，让我们创建一个类，所有测试都将源于该类。我们用它来配置由 Testcontainers 启动的 Keycloak 容器：

        ```java
        @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
        public abstract class KeycloakTestContainers {

            static {
                keycloak = new KeycloakContainer().withRealmImportFile("keycloak/realm-export.json");
                keycloak.start();
            }
        }
        ```

        静态声明并启动我们的容器将确保它在所有测试中都能一次实例化并启动。我们将使用 KeycloakContainer 对象中的 withRealmImportFile 方法指定启动时要导入的 realm 配置。

    3. Spring Boot 测试配置

        Keycloak 容器使用随机端口。因此，启动后我们需要覆盖 application.properties 中定义的 spring.security.oauth2.resourceserver.jwt.issuer-uri 配置。为此，我们将使用方便的 [@DynamicPropertySource](https://www.baeldung.com/spring-dynamicpropertysource) 注解：

        ```java
        @DynamicPropertySource
        static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/baeldung");
        }
        ```

4. 创建集成测试

    现在我们有了负责启动 Keycloak 容器和配置 Spring 属性的主测试类，让我们创建一个调用 User 控制器的[集成测试](https://www.baeldung.com/integration-testing-in-spring)。

    1. 获取访问令牌

        首先，让我们在抽象类 IntegrationTest(KeycloakTestContainers.java) 中添加一个方法 getJaneDoeBearer()，用 Jane Doe 的凭据请求一个令牌：

        ```java
        URI authorizationURI = new URIBuilder(keycloak.getAuthServerUrl() + "/realms/baeldung/protocol/openid-connect/token").build();
        WebClient webclient = WebClient.builder().build();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", Collections.singletonList("password"));
        formData.put("client_id", Collections.singletonList("baeldung-api"));
        formData.put("username", Collections.singletonList("jane.doe@baeldung.com"));
        formData.put("password", Collections.singletonList("s3cr3t"));

        String result = webclient.post()
        .uri(authorizationURI)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(formData))
        .retrieve()
        .bodyToMono(String.class)
        .block();
        ```

        在这里，我们使用 Webflux 的 WebClient 发布一个表单，其中包含获取访问令牌所需的不同参数。

        最后，我们将解析 Keycloak 服务器的响应，从中提取令牌。具体来说，我们会生成一个经典的身份验证字符串，其中包含承载器关键字，之后是令牌的内容，可在标头中使用：

        ```java
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return "Bearer " + jsonParser.parseMap(result)
        .get("access_token")
        .toString();
        ```

    2. 创建集成测试

        让我们针对配置好的 Keycloak 容器快速设置集成测试。我们将使用 RestAssured 和 Hamcrest 进行测试。让我们添加 Rest-assured 依赖项：

        ```xml
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
        ```

        现在我们可以使用抽象 IntegrationTest 类创建测试：

        ![UserControllerManualTest.java](/src/test/java/com/baeldung/keycloaktestcontainers/UserControllerManualTest.java)

        这样，我们从 Keycloak 获取的访问令牌就被添加到了请求的授权标头中。

5. 结论

    在本文中，我们针对 Testcontainers 管理的实际 Keycloak 设置了集成测试。我们导入了一个领域配置，以便每次启动测试阶段时都有一个预先配置好的环境。
