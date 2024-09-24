# [Spring Boot中的验证](https://www.baeldung.com/spring-boot-bean-validation)

1. 一览表

    在验证用户输入时，Spring Boot为这个开箱即用的常见但关键任务提供了强大的支持。

    尽管Spring Boot支持与自定义验证器无缝集成，但执行验证的实际标准是[Hibernate Validator](http://hibernate.org/validator/)，[Bean Validation框架](https://www.baeldung.com/javax-validation)的参考实现。

    在本教程中，我们将研究如何在Spring Boot中验证域对象。

    进一步阅读：

    [Spring Boot中的自定义验证消息源](https://www.baeldung.com/spring-custom-validation-message-source)

    了解如何在Spring Boot中注册自定义MessageSource以验证消息。

    [Bean验证中@NotNull、@NotEmpty和@NotBlank约束之间的区别](https://www.baeldung.com/java-bean-validation-not-null-empty-blank)

    了解Java中@NotNull、@NotEmpty和@NotBlank bean验证注释的语义，以及它们的差异。

2. Maven属地

    在这种情况下，我们将学习如何通过构建基本的REST控制器在Spring Boot中验证域对象。

    控制器将首先接受一个域对象，然后使用Hibernate Validator验证它，最后将其保留到内存中的H2数据库中。

    该项目的依赖关系相当标准：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency> 
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency> 
    <dependency> 
        <groupId>com.h2database</groupId> 
        <artifactId>h2</artifactId>
        <version>2.1.214</version> 
        <scope>runtime</scope>
    </dependency>
    ```

    如上所示，我们在pom.xml文件中包含spring-boot-starter-web，因为我们需要它来创建REST控制器。此外，让我们确保在Maven Central上检查最新版本的spring-boot-starter-jpa和H2数据库。

    从Boot 2.3开始，我们还需要明确添加spring-boot-starter-validation依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    ```

3. 一个简单的域类

    由于我们项目的依赖项已经到位，接下来我们需要定义一个示例JPA实体类，其作用仅为建模用户。

    让我们来看看这门课：

    ```java
    @Entity
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;
        
        @NotBlank(message = "Name is mandatory")
        private String name;
        
        @NotBlank(message = "Email is mandatory")
        private String email;
        
        // standard constructors / setters / getters / toString
            
    }
    ```

    我们的用户实体类的实现确实非常贫乏，但它简而言之，展示了如何使用Bean Validation的约束来限制名称和电子邮件字段。

    为了简单起见，我们只使用[@NotBlank](https://www.baeldung.com/java-bean-validation-not-null-empty-blank)约束来限制目标字段。此外，我们用消息属性指定了错误消息。

    因此，当Spring Boot验证类实例时，受限字段不得为空，其修剪长度必须大于零。

    此外，除了@NotBlank之外，[Bean Validation](https://www.baeldung.com/javax-validation)还提供了许多其他方便的约束。这允许我们对受约束的类应用和组合不同的验证规则。有关更多信息，请阅读[官方的bean验证文档](https://beanvalidation.org/2.0/)。

    由于我们将使用[Spring Data JPA](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)将用户保存到内存中的H2数据库，我们还需要定义一个简单的存储库接口，以便在用户对象上具有基本的CRUD功能：

    ```java
    @Repository
    public interface UserRepository extends CrudRepository<User, Long> {}
    ```

4. 实现REST控制器

    当然，我们需要实现一个层，允许我们获取分配给用户对象的约束字段的值。

    因此，我们可以根据验证结果验证它们并执行一些进一步的任务。

    Spring Boot通过实现REST控制器使这个看似复杂的过程变得非常简单。

    让我们来看看REST控制器的实现：

    ```java
    @RestController
    public class UserController {

        @PostMapping("/users")
        ResponseEntity<String> addUser(@Valid @RequestBody User user) {
            // persisting the user
            return ResponseEntity.ok("User is valid");
        }
        
        // standard constructors / other methods
        
    }
    ```

    在[Spring REST上下文](https://www.baeldung.com/rest-with-spring-series)中，addUser（）方法的实现是相当标准的。

    当然，最相关的部分是使用@Valid注释。

    当Spring Boot找到用@Valid注释的参数时，它会自动引导默认的JSR 380实现——Hibernate Validator——并验证该参数。

    当目标参数无法通过验证时，Spring Boot会抛出[MethodArgumentNotValidException](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html)异常。

5. @ExceptionHandler注释

    虽然让Spring Boot验证自动传递给addUser（）方法的用户对象真的很方便，但这个过程缺少的方面是我们如何处理验证结果。

    [@ExceptionHandler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ExceptionHandler.html)注释允许我们通过一种方法处理指定类型的异常。

    因此，我们可以用它来处理验证错误：

    ```java
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
    MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
    ```

    我们指定了MethodArgumentNotValidException异常作为[要处理的异常](https://www.baeldung.com/exception-handling-for-rest-with-spring)。因此，当指定的用户对象无效时，Spring Boot将调用此方法。

    该方法存储地图中每个无效字段的名称和验证后错误消息。接下来，它将地图作为JSON表示发回客户端，以便进一步处理。

    简单地说，REST控制器允许我们轻松处理对不同端点的请求，验证用户对象，并以JSON格式发送响应。

    该设计足够灵活，可以通过几个Web层处理控制器响应，从[Thymeleaf](https://www.baeldung.com/spring-boot-crud-thymeleaf)等模板引擎到[Angular](https://angular.io/)等全功能的JavaScript框架。

6. 测试REST控制器

    我们可以通过[集成测试](https://www.baeldung.com/spring-boot-testing)轻松测试REST控制器的功能。

    让我们开始模拟/自动连接(mocking/autowiring) UserRepository接口实现，以及UserController实例和[MockMvc](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/test/web/servlet/MockMvc.html)对象：

    ```java
    @RunWith(SpringRunner.class)
    @WebMvcTest
    @AutoConfigureMockMvc
    public class UserControllerIntegrationTest {

        @MockBean
        private UserRepository userRepository;
        
        @Autowired
        UserController userController;

        @Autowired
        private MockMvc mockMvc;

        //...
        
    }
    ```

    由于我们只测试网络层，我们使用[@WebMvcTest](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/web/servlet/WebMvcTest.html)注释。它允许我们使用[MockMvcRequestBuilders](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/request/MockMvcRequestBuilders.html)和[MockMvcResultMatchers](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/test/web/servlet/result/MockMvcResultMatchers.html)类实现的一组静态方法轻松测试请求和响应。

    现在让我们用请求正文中传递的有效和无效的用户对象来测试addUser()方法：

    ```java
    @Test
    public void whenPostRequestToUsersAndValidUser_thenCorrectResponse() throws Exception {
        MediaType textPlainUtf8 = new MediaType(MediaType.TEXT_PLAIN, Charset.forName("UTF-8"));
        String user = "{\"name\": \"bob\", \"email\" : \"<bob@domain.com>\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
        .content(user)
        .contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content()
            .contentType(textPlainUtf8));
    }

    @Test
    public void whenPostRequestToUsersAndInValidUser_thenCorrectResponse() throws Exception {
        String user = "{\"name\": \"\", \"email\" : \"<bob@domain.com>\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
        .content(user)
        .contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.name", Is.is("Name is mandatory")))
        .andExpect(MockMvcResultMatchers.content()
            .contentType(MediaType.APPLICATION_JSON_UTF8));
    }
    ```

    此外，我们可以使用免费的API生命周期测试应用程序（如[Postman](https://www.getpostman.com/)）来测试REST控制器API。

7. 运行示例应用程序

    最后，我们可以使用标准的main()方法运行示例项目：

    ```java
    @SpringBootApplication
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
        
        @Bean
        public CommandLineRunner run(UserRepository userRepository) throws Exception {
            return (String[] args) -> {
                User user1 = new User("Bob", "bob@domain.com");
                User user2 = new User("Jenny", "jenny@domain.com");
                userRepository.save(user1);
                userRepository.save(user2);
                userRepository.findAll().forEach(System.out::println);
            };
        }
    }
    ```

    不出所料，我们应该在控制台中看到几个用户对象打印出来。

    带有有效用户对象的对 <http://localhost:8080/users> 端点的POST请求将返回字符串“User is valid”。

    同样，带有用户对象且没有名称和电子邮件值的POST请求将返回以下响应：

    ```json
    {
    "name":"Name is mandatory",
    "email":"Email is mandatory"
    }
    ```

8. 结论

    在本文中，我们学习了在Spring Boot中执行验证的基础知识。
