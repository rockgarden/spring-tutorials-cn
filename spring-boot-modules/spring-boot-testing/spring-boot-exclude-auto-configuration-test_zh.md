# [在Spring Boot Tests中排除自动配置类](https://www.baeldung.com/spring-boot-exclude-auto-configuration-test)

1. 一览表

    在本快速教程中，我们将讨论如何从Spring Boot测试中排除自动配置类。

    [Spring Boot的自动配置](https://www.baeldung.com/spring-boot-custom-auto-configuration)功能非常方便，因为它为我们处理了很多设置。然而，如果我们不希望某个自动配置干扰我们对模块的测试，这在测试过程中也可能是一个问题。

    这方面的一个常见例子是安全自动配置，我们也将将其用于我们的示例。

2. 测试示例

    首先，我们将看看我们的测试示例。

    我们将有一个安全的Spring Boot应用程序，带有一个简单的主页。

    当我们尝试在未经身份验证的情况下访问主页时，响应是“401 UNAUTHORIZED”。

    让我们在使用[REST-assured](https://www.baeldung.com/rest-assured-tutorial)进行调用的测试中看到这一点：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
    public class AutoConfigIntegrationTest {

        @Test
        public void givenNoAuthentication_whenAccessHome_thenUnauthorized() {
            int statusCode = RestAssured.get("http://localhost:8080/").statusCode();
            assertEquals(HttpStatus.UNAUTHORIZED.value(), statusCode);
        }
        
    }
    ```

    另一方面，我们可以通过身份验证成功访问主页：

    ```java
    @Test
    public void givenAuthentication_whenAccessHome_thenOK() {
        int statusCode = RestAssured.given().auth().basic("john", "123")
        .get("http://localhost:8080/")
        .statusCode();
        assertEquals(HttpStatus.OK.value(), statusCode);
    }
    ```

    在以下章节中，我们将尝试不同的方法将SecurityAutoConfiguration类排除在测试配置之外。

3. 使用@EnableAutoConfiguration

    有多种方法可以从测试配置中排除特定的自动配置类。

    首先，让我们看看如何使用@EnableAutoConfiguration(exclude={CLASS_NAME})注释：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
    @EnableAutoConfiguration(exclude=SecurityAutoConfiguration.class)
    public class ExcludeAutoConfigIntegrationTest {

        @Test
        public void givenSecurityConfigExcluded_whenAccessHome_thenNoAuthenticationRequired() {
            int statusCode = RestAssured.get("http://localhost:8080/").statusCode();
            
            assertEquals(HttpStatus.OK.value(), statusCode);
        }
    }
    ```

    在本例中，我们使用排除属性排除了SecurityAutoConfiguration类，但我们可以对任何[自动配置类](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-auto-configuration-classes.html)进行同样的操作。

    现在，我们可以运行在没有身份验证的情况下访问主页的测试，它将不再失败。

4. 使用@TestPropertySource

    接下来，我们可以使用@TestPropertySource来注入属性“spring.autoconfigure.exclude”：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
    @TestPropertySource(properties =
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
    public class ExcludeAutoConfigIntegrationTest {...}
    ```

    请注意，我们需要为属性指定完整的类名称（package name+simple name）。

5. 使用配置文件

    我们还可以使用配置文件为我们的测试设置属性“spring.autoconfigure.exclude”：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
    @ActiveProfiles("test")
    public class ExcludeAutoConfigIntegrationTest {...}
    ```

    并在application-test.properties中包含所有“test”配置文件特定属性：

    `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration`

6. 使用自定义测试配置

    最后，我们可以使用单独的配置应用程序进行测试：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
    public class ExcludeAutoConfigIntegrationTest {...}
    ```

    并从@SpringBootApplication（exclude={CLASS_NAME}）中排除自动配置类：

    ```java
    @SpringBootApplication(exclude=SecurityAutoConfiguration.class)
    public class TestApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class, args);
        }
    }
    ```

7. 结论

    在本文中，我们探讨了从Spring Boot测试中排除自动配置类的不同方法。
