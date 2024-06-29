# [用Java中的Keycloak搜索用户](https://www.baeldung.com/java-keycloak-search-users)

1. 概述

    Keycloak 是一个第三方身份和访问管理解决方案，可以帮助我们将身份验证和授权集成到应用程序中。

    在本教程中，我们将举例说明如何在 Keycloak 中搜索用户。

2. Keycloak 配置

    首先，我们需要配置 Keycloak。让我们创建一个名为 baeldung 的初始管理员用户，密码为 secretPassword。其次，我们需要一个工作域。让我们使用启动 Keycloak 时已经存在的主域。

    最后，我们需要一个可以在 Spring Boot 应用程序中使用的客户端。在本例中，我们使用默认创建的 admin-cli 客户端。

    我们需要域中的一些用户，以便稍后搜索他们。让我们创建一个用户名为 "user1"、电子邮件地址为 "<user1@test.com>"、名称为 "First User1" 的用户。现在，我们可以多重复几次这种模式，总共可以创建 10 个用户。

3. Keycloak 管理客户端

    Keycloak 有一个 [REST API](https://www.keycloak.org/docs/latest/server_development/index.html#admin-rest-api)，可用于访问管理控制台用户界面上的所有功能。我们可以在任何客户端上使用该 API，但 Keycloak 提供的 [Java 客户端](https://www.keycloak.org/docs/latest/server_development/index.html#example-using-java)让使用更加方便。在第一个示例中，我们将在 Spring Boot 应用程序中使用该 Java 客户端。

    首先，让我们在 pom.xml 中添加 keycloak-admin-client 依赖项：

    ```xml
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-admin-client</artifactId>
        <version>21.0.1</version>
    </dependency>
    ```

    确保客户端的版本与服务器的[版本一致](https://www.keycloak.org/docs/latest/upgrading/#upgrading-the-keycloak-admin-client)非常重要。不匹配的版本可能无法正常工作。

    现在，让我们在应用程序中配置客户端。为此，我们需要创建一个 Keycloak Bean：

    ```java
    @Bean
    Keycloak keycloak() {
        return KeycloakBuilder.builder()
        .serverUrl("http://localhost:8080")
        .realm("master")
        .clientId("admin-cli")
        .grantType(OAuth2Constants.PASSWORD)
        .username("baeldung")
        .password("secretPassword")
        .build();
    }
    ```

    在此构建器中，我们配置了服务器 URL，设置了前面提到的域和客户端名称，并提供了正确的凭据。

    让我们创建一个服务，使用此 Bean 访问 Keycloak 服务器：

    ```java
    @Service
    public class AdminClientService {

        @Autowired
        Keycloak keycloak;

        @PostConstruct
        void searchUsers() {
            // ...
        }
    }
    ```

    之后，让我们启动应用程序。现在我们可以开始搜索用户了。

    1. 按用户名搜索

        Java 客户端提供了一种非常方便的按用户名搜索用户的方法。首先，我们需要引用要使用的域，然后访问用户并使用 [searchByUsername()](https://www.keycloak.org/docs-api/21.0.1/javadocs/org/keycloak/admin/client/resource/UsersResource.html#searchByUsername(java.lang.String,java.lang.Boolean)) 方法。让我们创建一个搜索用户并记录结果的方法：

        ```java
        private static final String REALM_NAME = "master";

        void searchByUsername(String username, boolean exact) {
            logger.info("Searching by username: {} (exact {})", username, exact);
            List<UserRepresentation> users = keycloak.realm(REALM_NAME)
            .users()
            .searchByUsername(username, exact);

            logger.info("Users found by username {}", users.stream()
            .map(user -> user.getUsername())
            .collect(Collectors.toList()));
        }
        ```

        现在，我们可以在带有不同参数的 searchUsers() 方法中使用它：

        ```java
        void searchUsers() {
            searchByUsername("user1", true);
            searchByUsername("user", false);
            searchByUsername("1", false);
        }
        ```

        第一次搜索查找与 "user1" 用户名完全匹配的用户。第二个搜索不要求完全匹配，因此会返回所有用户名包含 "user" 一词的用户。第三种方法与之类似，也是查找包含数字 "1" 的用户名。由于我们有 10 个用户，他们的用户名从 user1 到 user10 不等，因此日志包含以下结果：

        ```log
        12:20:22.295 [main] INFO  c.b.k.adminclient.AdminClientService - Searching users in Keycloak 21.0.1
        12:20:22.296 [main] INFO  c.b.k.adminclient.AdminClientService - Searching by username: user1 (exact true)
        12:20:22.341 [main] INFO  c.b.k.adminclient.AdminClientService - Users found by username [user1]
        ...
        ```

    2. 通过电子邮件搜索

        如前所述，根据用户名进行过滤非常容易。幸运的是，使用电子邮件地址作为过滤器与此非常相似。让我们使用 [searchByEmail()](https://www.keycloak.org/docs-api/21.0.1/javadocs/org/keycloak/admin/client/resource/UsersResource.html#searchByEmail(java.lang.String,java.lang.Boolean)) 方法，该方法接受电子邮件地址和一个布尔参数，用于精确匹配：

        ```java
        void searchByEmail(String email, boolean exact) {
            logger.info("Searching by email: {} (exact {})", email, exact);
            
            List<UserRepresentation> users = keycloak.realm(REALM_NAME)
            .users()
            .searchByEmail(email, exact);

            logger.info("Users found by email {}", users.stream()
            .map(user -> user.getEmail())
            .collect(Collectors.toList()));
        }
        ```

        让我们用 "<user2@test.com>" 地址进行测试，寻找完全匹配的结果。这次只有一个结果：

        ```log
        12:24:16.130 [main] INFO  c.b.k.adminclient.AdminClientService - Searching by email: user2@test.com (exact true)
        12:24:16.141 [main] INFO  c.b.k.adminclient.AdminClientService - Users found by email [user2@test.com]
        ```

    3. 按自定义属性搜索

        在 Keycloak 中，用户也可以有自定义属性，而不仅仅是简单的用户名和电子邮件。让我们为 user1 添加一个名为 DOB 的自定义属性，表示出生日期，值为 "2000-01-05"。

        现在，我们可以在管理员客户端使用 [searchByAttributes()](https://www.keycloak.org/docs-api/21.0.1/javadocs/org/keycloak/admin/client/resource/UsersResource.html#searchByAttributes(java.lang.String)) 方法：

        ```java
        void searchByAttributes(String query) {
            logger.info("Searching by attributes: {}", query);
            
            List<UserRepresentation> users = keycloak.realm(REALM_NAME)
            .users()
            .searchByAttributes(query);

            logger.info("Users found by attributes {}", users.stream()
            .map(user -> user.getUsername() + " " + user.getAttributes())
            .collect(Collectors.toList()));
        }
        ```

        让我们使用 "DOB:2000-01-05" 查询来列出具有匹配属性的用户：

        ```log
        13:19:51.091 [main] INFO  c.b.k.adminclient.AdminClientService - Searching by attributes: DOB:2000-01-05
        13:19:51.103 [main] INFO  c.b.k.adminclient.AdminClientService - Users found by attributes [user1 {DOB=[2000-01-05]}]
        ```

    4. 按组搜索

        用户也可以属于一个组，我们也可以通过该组进行筛选。让我们创建一个名为 "Test Group" 的组，并向其中添加一些成员。现在，我们可以通过管理员客户端获取该组的成员：

        ```java
        void searchByGroup(String groupId) {
            logger.info("Searching by group: {}", groupId);

            List<UserRepresentation> users = keycloak.realm(REALM_NAME)
            .groups()
            .group(groupId)
            .members();

            logger.info("Users found by group {}", users.stream()
            .map(user -> user.getUsername())
            .collect(Collectors.toList()));
        }
        ```

        不过，需要注意的是，我们在这里使用的是组 ID 而不是名称。这样，我们就能列出 Keycloak 组的成员：

        ```log
        14:35:09.275 [main] INFO  c.b.k.adminclient.AdminClientService - Searching by group: c67643fb-514e-488a-a4b4-5c0bdf2e7477
        14:35:09.290 [main] INFO  c.b.k.adminclient.AdminClientService - Users found by group [user1, user2, user3, user4, user5]
        ```

    5. 按角色搜索

        按角色搜索与前一种方法非常相似，因为我们可以列出拥有特定角色的用户，就像列出某个组的成员一样。为此，我们需要为某些用户分配一个角色。让我们创建一个名为 "user" 的角色，并将其分配给 "user1"。现在，我们可以实现搜索功能了：

        ```java
        void searchByRole(String roleName) {
            logger.info("Searching by role: {}", roleName);

            List<UserRepresentation> users = keycloak.realm(REALM_NAME)
            .roles()
            .get(roleName)
            .getUserMembers();

            logger.info("Users found by role {}", users.stream()
            .map(user -> user.getUsername())
            .collect(Collectors.toList()));
        }
        ```

        让我们看看哪些用户拥有此角色：

        ```log
        12:03:23.788 [main] INFO  c.b.k.adminclient.AdminClientService - Searching by role: user
        12:03:23.802 [main] INFO  c.b.k.adminclient.AdminClientService - Users found by role [user1]
        ```

4. 自定义 REST 端点

    正如我们所看到的，Keycloak 默认提供了有用的搜索功能，但在某些情况下，我们可能需要一些不同或更复杂的功能。但也有解决方法。我们可以通过向 Keycloak 添加新的 [API 端点](https://www.keycloak.org/docs/latest/server_development/#_extensions_rest)来实现自己的自定义功能。

    假设我们想找到某个特定群组中的用户，同时也想找到某个特定角色的用户。例如，我们可以找到 "软件开发部" 组中所有具有 "项目经理" 角色的用户。

    首先，我们需要一个从 Keycloak 的 [RealmResourceProvider](https://www.keycloak.org/docs-api/21.0.1/javadocs/org/keycloak/services/resource/RealmResourceProvider.html) 扩展而来的新类。让我们在这里实现自定义功能：

    ![KeycloakUserApiProvider.java](/src/main/java/com/baeldung/keycloak/customendpoint/KeycloakUserApiProvider.java)

    之后，我们需要通过配置 [RealmResourceProviderFactory](https://www.keycloak.org/docs-api/21.0.1/javadocs/org/keycloak/services/resource/RealmResourceProviderFactory.html) 来告诉 Keycloak 使用该类：

    ![KeycloakUserApiProviderFactory.java](/src/main/java/com/baeldung/keycloak/customendpoint/KeycloakUserApiProviderFactory.java)

    最后，我们需要在 META-INF 文件夹中创建一个文件来注册该类。该文件中只有一行包含类的限定名称。让我们创建 `src/main/resources/META-INF/services/org.keycloak.services.resource.RealmResourceProviderFactory` 文件，使其包含我们类的名称：

    `com.baeldung.keycloak.customendpoint.KeycloakUserApiProviderFactory`

    让我们构建项目并将 jar 复制到 [Keycloak 中的 providers 程序文件夹](https://www.keycloak.org/server/configuration-provider#_installing_and_uninstalling_a_provider)，然后运行构建命令更新服务器的提供程序注册表：

    `kc build`

    我们会得到以下输出，这意味着我们的自定义提供程序已被识别并注册：

    正在更新配置并安装自定义提供程序（如果有）。请稍候。

    ```log
    Updating the configuration and installing your custom providers, if any. Please wait.

    Server configuration updated and persisted. Run the following command to review the configuration:
    kc.bat show-config
    ```

    让我们再次启动 Keycloak 实例，访问 <http://localhost:8080/realms/master/users-by-group-and-role-name?groupName=Test%20Group&roleName=user> 上的自定义 API 端点。

    该 API 端点会成功返回满足两个条件的用户，即属于 "测试组 "且角色为 "user"：

    ```json
    [
        {
            "id": "2c59a20f-df38-4d14-8ff9-067ea30f7937",
            "createdTimestamp": 1678099525313,
            "username": "user1",
            "enabled": true,
            "emailVerified": true,
            "firstName": "First",
            "lastName": "User",
            "email": "user1@test.com"
        }
    ]
    ```

5. 结论

    在本文中，我们使用 Keycloak 管理客户端将 Spring Boot 应用程序与 Keycloak 集成，以管理用户。这提供了一种访问现有功能的简单方法。然后，我们创建了一个自定义 REST 端点，以便使用我们的自定义功能扩展 Keycloak，从而实现任何必要的自定义逻辑。
