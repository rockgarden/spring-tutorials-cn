# [在Spring Boot中测试](https://www.baeldung.com/spring-boot-testing)

1. 一览表

    在本教程中，我们将看看如何使用Spring Boot中的框架支持编写测试。我们将涵盖可以隔离运行的单元测试，以及在执行测试之前引导Spring上下文的集成测试。

2. 项目设置

    本文中我们将要使用的应用程序是一个API，它为员工资源提供一些基本操作。这是一个典型的分层架构——API调用从控制器到服务到永不动层进行处理。

3. Maven附属机构

    让我们先添加我们的测试依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <version>3.1.5</version>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    ```

    spring-boot-starter-test是包含我们测试所需的大多数元素的主要依赖项。

    H2 DB是我们的内存数据库。它消除了为测试目的配置和启动实际数据库的需要。

    1. JUnit4

        从Spring Boot 2.4开始，JUnit 5的复古发动机已从spring-boot-starter-test中删除。如果我们仍然想使用JUnit 4编写测试，我们需要添加以下Maven依赖项：

        ```xml
        <dependency>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.hamcrest</groupId>
                    <artifactId>hamcrest-core</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        ```

4. 与@SpringBootTest的集成测试

    顾名思义，集成测试的重点是集成应用程序的不同层。这也意味着不涉及模拟。

    理想情况下，我们应该将集成测试与单元测试分开，不应与单元测试一起运行。我们可以通过使用不同的配置文件来仅运行集成测试来做到这一点。这样做的几个原因可能是集成测试很耗时，可能需要一个实际的数据库来执行。

    然而，在本文中，我们不会关注这一点，而是将使用内存中的H2持久存储。

    集成测试需要启动一个容器来执行测试用例。因此，这需要一些额外的设置——所有这些都在Spring Boot中很容易：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Application.class)
    @AutoConfigureMockMvc
    @TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
    public class EmployeeRestControllerIntegrationTest {

        @Autowired
        private MockMvc mvc;

        @Autowired
        private EmployeeRepository repository;

        // write test cases here
    }
    ```

    当我们需要引导整个容器时，@SpringBootTest注释很有用。注释通过创建将用于我们的测试的应用程序上下文来工作。

    我们可以使用@SpringBootTest的webEnvironment属性来配置我们的运行时环境；我们在这里使用WebEnvironment.MOCK，以便容器将在模拟servlet环境中运行。

    接下来，@TestPropertySource注释有助于配置特定于我们测试的属性文件的位置。请注意，使用@TestPropertySource加载的属性文件将覆盖现有的application.properties文件。

    application-integrationtest.properties包含配置持久存储的详细信息：

    ```properties
    spring.datasource.url = jdbc:h2:mem:test
    spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.H2Dialect
    ```

    如果我们想针对MySQL运行集成测试，我们可以在属性文件中更改上述值。

    集成测试的测试用例可能与控制器层单元测试相似：

    ```java
    @Test
    public void givenEmployees_whenGetEmployees_thenStatus200() throws Exception {

        createTestEmployee("bob");

        mvc.perform(get("/api/employees")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
            .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].name", is("bob")));
    }
    ```

    与控制器层单元测试的区别在于，这里没有任何模拟，并且将执行端到端场景。

5. 使用@TestConfiguration测试配置

    正如我们在上一节中看到的那样，使用@SpringBootTest注释的测试将引导完整的应用程序上下文，这意味着我们可以@Autowire通过组件扫描捕获的任何bean进入我们的测试：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest
    public class EmployeeServiceImplIntegrationTest {

        @Autowired
        private EmployeeService employeeService;

        // class code ...
    }
    ```

    然而，我们可能希望避免引导真实应用程序上下文，而是使用特殊的测试配置。我们可以通过@TestConfiguration注释来实现这一点。有两种使用注释的方法。要么在同一测试类的静态内部类上，我们想@Autowire the bean：

    ```java
    @RunWith(SpringRunner.class)
    public class EmployeeServiceImplIntegrationTest {

        @TestConfiguration
        static class EmployeeServiceImplTestContextConfiguration {
            @Bean
            public EmployeeService employeeService() {
                return new EmployeeService() {
                    // implement methods
                };
            }
        }

        @Autowired
        private EmployeeService employeeService;
    }
    ```

    或者，我们可以创建一个单独的测试配置类：

    ```java
    @TestConfiguration
    public class EmployeeServiceImplTestContextConfiguration {
        
        @Bean
        public EmployeeService employeeService() {
            return new EmployeeService() { 
                // implement methods 
            };
        }
    }
    ```

    使用@TestConfiguration注释的配置类被排除在组件扫描之外，因此我们需要在每个想要@Autowire的测试中明确导入它。我们可以用@Import注释来做到这一点：

    ```java
    @RunWith(SpringRunner.class)
    @Import(EmployeeServiceImplTestContextConfiguration.class)
    public class EmployeeServiceImplIntegrationTest {

        @Autowired
        private EmployeeService employeeService;

        // remaining class code
    }
    ```

6. 与@MockBean一起嘲笑

    我们的服务层代码取决于我们的存储库：

    ```java
    @Service
    public class EmployeeServiceImpl implements EmployeeService {

        @Autowired
        private EmployeeRepository employeeRepository;

        @Override
        public Employee getEmployeeByName(String name) {
            return employeeRepository.findByName(name);
        }
    }
    ```

    然而，为了测试服务层，我们不需要了解或关心持久层是如何实现的。理想情况下，我们应该能够编写和测试我们的服务层代码，而无需在完整的持久层中接线(wiring)。

    为了实现这一点，我们可以使用Spring Boot Test提供的模拟支持。

    让我们先看看测试类的骨架：

    ```java
    @RunWith(SpringRunner.class)
    public class EmployeeServiceImplIntegrationTest {

        @TestConfiguration
        static class EmployeeServiceImplTestContextConfiguration {
            @Bean
            public EmployeeService employeeService() {
                return new EmployeeServiceImpl();
            }
        }

        @Autowired
        private EmployeeService employeeService;

        @MockBean
        private EmployeeRepository employeeRepository;

        // write test cases here
    }
    ```

    要检查服务类，我们需要创建并作为@Bean提供服务类的实例，以便我们可以在测试类中@Autowire它。我们可以使用@TestConfiguration注释来实现此配置。

    这里的另一件有趣的事情是使用@MockBean。它为EmployeeRepository创建一个模拟，可用于绕过对实际EmployeeRepository的调用：

    ```java
    @Before
    public void setUp() {
        Employee alex = new Employee("alex");

        Mockito.when(employeeRepository.findByName(alex.getName()))
        .thenReturn(alex);
    }
    ```

    由于设置已经完成，测试用例将更简单：

    ```java
    @Test
    public void whenValidName_thenEmployeeShouldBeFound() {
        String name = "alex";
        Employee found = employeeService.getEmployeeByName(name);
    
        assertThat(found.getName())
            .isEqualTo(name);
    }
    ```

7. 与@DataJpaTest的集成测试

    我们将与一个名为Employee的实体合作，该实体的属性是ID和名称：

    ```java
    @Entity
    @Table(name = "person")
    public class Employee {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Size(min = 3, max = 20)
        private String name;

        // standard getters and setters, constructors
    }
    ```

    这是我们使用Spring Data JPA的存储库：

    ```java
    @Repository
    public interface EmployeeRepository extends JpaRepository<Employee, Long> {
        public Employee findByName(String name);
    }
    ```

    这就是持久层代码。现在让我们去写我们的测试类。

    首先，让我们创建测试班的骨架：

    ```java
    @RunWith(SpringRunner.class)
    @DataJpaTest
    public class EmployeeRepositoryIntegrationTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private EmployeeRepository employeeRepository;

        // write test cases here

    }
    ```

    @RunWith（SpringRunner.class）在Spring Boot测试功能和JUnit之间提供了一个桥梁。每当我们在JUnit测试中使用任何Spring Boot测试功能时，都需要此注释。

    @DataJpaTest提供了测试持久性层所需的一些标准设置：

    - 配置H2，一个内存数据库
    - 设置Hibernate、Spring Data和DataSource
    - 执行@EntityScan
    - 打开SQL日志记录

    为了执行DB操作，我们需要一些已经在数据库中的记录。为了设置此数据，我们可以使用TestEntityManager。

    Spring Boot TestEntityManager是标准JPA EntityManager的替代品，它提供了编写测试时常用的方法。

    EmployeeRepository是我们将要测试的组件。

    现在让我们来写我们的第一个测试案例：

    ```java
    @Test
    public void whenFindByName_thenReturnEmployee() {
        // given
        Employee alex = new Employee("alex");
        entityManager.persist(alex);
        entityManager.flush();

        // when
        Employee found = employeeRepository.findByName(alex.getName());

        // then
        assertThat(found.getName()).isEqualTo(alex.getName());
    }
    ```

    在上述测试中，我们使用TestEntityManager在DB中插入员工，并通过查找名称API读取。

    assertThat(...)部分来自[Assertj库](https://www.baeldung.com/introduction-to-assertj)，该库与Spring Boot捆绑在一起。

8. 使用@WebMvcTest进行单元测试

    我们的控制器取决于服务层；为了简单起见，我们只包括一种方法：

    ```java
    @RestController
    @RequestMapping("/api")
    public class EmployeeRestController {

        @Autowired
        private EmployeeService employeeService;

        @GetMapping("/employees")
        public List<Employee> getAllEmployees() {
            return employeeService.getAllEmployees();
        }
    }
    ```

    由于我们只关注控制器代码，因此在我们的单元测试中模拟服务层代码是很自然的：

    ```java
    @RunWith(SpringRunner.class)
    @WebMvcTest(EmployeeRestController.class)
    public class EmployeeRestControllerIntegrationTest {

        @Autowired
        private MockMvc mvc;

        @MockBean
        private EmployeeService service;

        // write test cases here
    }
    ```

    为了测试控制器，我们可以使用@WebMvcTest。它将为我们的单元测试自动配置Spring MVC基础设施。

    在大多数情况下，@WebMvcTest将仅限于引导单个控制器。我们还可以将其与@MockBean一起使用，为任何所需的依赖项提供模拟实现。

    @WebMvcTest还自动配置MockMvc，它提供了一种无需启动完整的HTTP服务器即可轻松测试MVC控制器的强大方法。

    话虽如此，让我们写下我们的测试案例：

    ```java
    @Test
    public void givenEmployees_whenGetEmployees_thenReturnJsonArray()
    throws Exception {
        
        Employee alex = new Employee("alex");

        List<Employee> allEmployees = Arrays.asList(alex);

        given(service.getAllEmployees()).willReturn(allEmployees);

        mvc.perform(get("/api/employees")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(alex.getName())));
    }
    ```

    get(...)方法调用可以被与HTTP动词等HTTP对应的其他方法所取代，如put（）、post（）等。请注意，我们也在请求中设置内容类型。

    MockMvc很灵活，我们可以使用它创建任何请求。

9. 自动配置测试

    Spring Boot自动配置注释的惊人功能之一是，它有助于加载代码库的完整应用程序和测试特定层的部分。

    除了上述注释外，以下是一些广泛使用的注释列表：

    - @WebFluxTest：我们可以使用@WebFluxTest注释来测试Spring WebFlux控制器。它经常与@MockBean一起使用，为所需的依赖项提供模拟实现。
    - @JdbcTest：我们可以使用@JdbcTest注释来测试JPA应用程序，但它适用于只需要aDataSource的测试。注释配置了内存中嵌入式数据库和JdbcTemplate。
    - @JooqTest：要测试jOOQ相关的测试，我们可以使用@JooqTest注释，该注释配置了DSLContext。
    - @DataMongoTest：为了测试MongoDB应用程序，@DataMongoTest是一个有用的注释。默认情况下，如果驱动程序通过依赖项可用，它会配置内存中嵌入式MongoDB，配置aMongoTemplate，扫描@Document类，并配置Spring Data MongoDB存储库。
    - @DataRedisTest使测试Redis应用程序变得更容易。它扫描@RedisHash类，并默认配置Spring Data Redis存储库。
    - @DataLdapTest配置内存中嵌入式LDAP（如果可用），配置LdapTemplate，扫描@Entry类，并默认配置Spring Data LDAP存储库。
    - @RestClientTest：我们通常使用@RestClientTest注释来测试REST客户端。它自动配置不同的依赖项，如Jackson、GSON和Jsonb支持；配置RestTemplateBuilder；并默认添加对MockRestServiceServer的支持。
    - @JsonTest：仅使用测试JSON序列化所需的bean初始化Spring应用程序上下文。

    您可以在我们的文章 [优化Spring集成测试](https://www.baeldung.com/spring-tests) 中阅读更多有关这些注释以及如何进一步优化集成测试的信息。

10. 结论

    在本文中，我们深入了解了Spring Boot中的测试支持，并展示了如何有效地编写单元测试。
