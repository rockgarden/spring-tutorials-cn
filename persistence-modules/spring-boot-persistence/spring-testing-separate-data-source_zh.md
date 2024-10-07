# [为测试配置单独的Spring数据源](https://www.baeldung.com/spring-testing-separate-data-source)

1. 一览表

    在测试依赖持久性层（如JPA）的Spring应用程序时，我们可能希望设置一个测试数据源，以使用与我们用于运行应用程序的数据库不同的更小、更快的数据库，以便更轻松地运行我们的测试。

    在Spring中配置数据源需要定义DataSource类型的bean。我们可以手动执行此操作，也可以使用Spring Boot，通过标准应用程序属性。

    在本快速教程中，我们将学习几种配置单独数据源以在Spring中进行测试的方法。

2. Maven附属机构

    我们将使用Spring JPA和测试创建一个Spring Boot应用程序，因此我们需要以下依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
    ```

    最新版本的spring-boot-starter-data-jpa、h2和spring-boot-starter-test可以从Maven Central下载。

    现在让我们来看看配置数据源进行测试的几种不同方法。

3. 在Spring Boot中使用标准属性文件

    Spring Boot在运行应用程序时自动拾取的标准属性文件称为application.properties。它位于src/main/resources文件夹中。

    如果我们想使用不同的属性进行测试，我们可以通过在src/test/resources中放置另一个同名的文件来覆盖主文件夹中的属性文件。

    src/test/resources文件夹中的application.properties文件应包含配置数据源所需的标准键值对。这些属性的前缀是spring.datasource。

    例如，让我们配置一个H2内存数据库作为测试的数据源：

    ```properties
    spring.datasource.driver-class-name=org.h2.Driver
    spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
    spring.datasource.username=sa
    spring.datasource.password=sa
    ```

    Spring Boot将使用这些属性来自动配置DataSource bean。

    让我们使用Spring JPA定义一个非常简单的GenericEntity和存储库：

    ```java
    @Entity
    public class GenericEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;
        private String value;

        //standard constructors, getters, setters
    }

    public interface GenericEntityRepository
    extends JpaRepository<GenericEntity, Long> { }
    ```

    接下来，让我们为存储库编写一个JUnit测试。为了在Spring Boot应用程序中进行测试来获取我们定义的标准数据源属性，我们必须用@SpringBootTest注释它：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = Application.class)
    public class SpringBootJPAIntegrationTest {

        @Autowired
        private GenericEntityRepository genericEntityRepository;

        @Test
        public void givenGenericEntityRepository_whenSaveAndRetreiveEntity_thenOK() {
            GenericEntity genericEntity = genericEntityRepository
            .save(new GenericEntity("test"));
            GenericEntity foundEntity = genericEntityRepository
            .findOne(genericEntity.getId());
    
            assertNotNull(foundEntity);
            assertEquals(genericEntity.getValue(), foundEntity.getValue());
        }
    }
    ```

4. 使用自定义属性文件

    如果我们不想使用标准application.properties文件和键，或者如果我们不使用Spring Boot，我们可以定义一个带有自定义键的自定义.properties文件，然后在@Configuration类中读取此文件，根据它包含的值创建DataSource bean。

    此文件将放在应用程序正常运行模式的src/main/resources文件夹中，并放在src/test/resources中，以便被测试接收。

    让我们创建一个名为persistence-generic-entity.properties的文件，该文件使用H2内存数据库进行测试，并将其放置在src/test/resources文件夹中：

    ```properties
    jdbc.driverClassName=org.h2.Driver
    jdbc.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
    jdbc.username=sa
    jdbc.password=sa
    ```

    接下来，我们可以在@Configuration类中根据这些属性定义DataSource bean，该类将ourpersistence-generic-entity.properties加载为属性源：

    ```java
    @Configuration
    @EnableJpaRepositories(basePackages = "org.baeldung.repository")
    @PropertySource("persistence-generic-entity.properties")
    @EnableTransactionManagement
    public class H2JpaConfig {
        // ...
    }
    ```

    有关此配置的更详细示例，我们可以通读上一篇关于使用内存数据库进行自包含测试的文章中的“JPA配置”部分。

    然后，我们可以创建一个类似于之前的JUnit测试，除了它会加载我们的配置类：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = {Application.class, H2JpaConfig.class})
    public class SpringBootH2IntegrationTest {
        // ...
    }
    ```

5. 使用Spring Profiles

    我们配置单独的数据源进行测试的另一种方法是利用Spring Profiles来定义仅在测试配置文件中可用的DataSource bean。

    为此，我们可以像以前一样使用.properties文件，或者我们可以在类本身中写入值。

    让我们为@Configuration类中的测试配置文件定义一个DataSource bean，该bean将由我们的测试加载：

    ```java
    @Configuration
    @EnableJpaRepositories(basePackages = {
    "org.baeldung.repository",
    "org.baeldung.boot.repository"
    })
    @EnableTransactionManagement
    public class H2TestProfileJPAConfig {

        @Bean
        @Profile("test")
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("sa");

            return dataSource;
        }
        
        // configure entityManagerFactory
        // configure transactionManager
        // configure additional Hibernate properties
    }
    ```

    然后，在JUnit测试类中，我们需要通过添加@ActiveProfiles注释来指定要使用测试配置文件：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = {
    Application.class,
    H2TestProfileJPAConfig.class})
    @ActiveProfiles("test")
    public class SpringBootProfileIntegrationTest {
        // ...
    }
    ```

6. 结论

    在这篇简短的文章中，我们探索了在Spring中配置单独的数据源以进行测试的几种方法。
