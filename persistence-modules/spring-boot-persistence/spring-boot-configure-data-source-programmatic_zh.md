# [在Spring Boot中以编程方式配置数据源](https://www.baeldung.com/spring-boot-configure-data-source-programmatic)

1. 一览表

    Spring Boot使用有主见的算法来扫描和配置数据源。这使我们能够轻松地在默认情况下获得完全配置的数据源实现。

    此外，Spring Boot自动配置一个快如闪电的[连接池](https://www.baeldung.com/java-connection-pooling)，[HikariCP](https://www.baeldung.com/hikaricp)、[Apache Tomcat](https://www.baeldung.com/spring-boot-tomcat-connection-pool)或[Commons DBCP](https://commons.apache.org/proper/commons-dbcp/)，具体取决于类路径上的哪个。

    虽然Spring Boot的自动数据源配置在大多数情况下效果很好，但有时我们需要更高级别的控制，因此我们必须设置自己的数据源实现，因此跳过自动配置过程。

    在本教程中，我们将学习如何在Spring Boot中以编程方式配置数据源。

2. Maven

    总体而言，以编程方式创建数据源实现是直接的。

    为了了解如何做到这一点，我们将实现一个简单的存储库层，该层将对一些JPA实体执行CRUD操作。

    让我们来看看我们演示项目的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.4.1</version>
        <scope>runtime</scope>
    </dependency>
    ```

    如上所示，我们将使用内存中的H2数据库实例来执行存储库层。通过这样做，我们可以测试编程配置的数据源，而无需执行昂贵的数据库操作。

    此外，让我们确保在Maven Central上查看最新版本的spring-boot-starter-data-jpa。

3. 以编程方式配置数据源

    现在，如果我们坚持使用Spring Boot的自动数据源配置，并以当前状态运行项目，它将按预期工作。

    Spring Boot将为我们做所有重型基础设施管道。这包括创建H2数据源实现，该实现将由HikariCP、Apache Tomcat或Commons DBCP自动处理，并设置内存数据库实例。

    此外，我们甚至不需要创建一个application.properties文件，因为Spring Boot也会提供一些默认的数据库设置。

    正如我们之前提到的，有时我们需要更高级别的自定义，因此我们必须以编程方式配置我们自己的数据源实现。

    实现这一目标的最简单方法是定义DataSource工厂方法，并将其放置在带有@Configuration注释的类中：

    ```java
    @Configuration
    public class DataSourceConfig {
        
        @Bean
        public DataSource getDataSource() {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("org.h2.Driver");
            dataSourceBuilder.url("jdbc:h2:mem:test");
            dataSourceBuilder.username("SA");
            dataSourceBuilder.password("");
            return dataSourceBuilder.build();
        }
    }
    ```

    在这种情况下，我们使用方便的[DataSourceBuilder](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/jdbc/DataSourceBuilder.html)类，这是[Joshua Bloch构建器模式](https://www.pearson.com/us/higher-education/program/Bloch-Effective-Java-3rd-Edition/PGM1763855.html)的不流畅版本，以编程方式创建我们的自定义DataSource对象。

    这种方法真的很好，因为构建器使使用一些常见属性轻松配置数据源。它也使用底层连接池。

4. 使用application.properties文件外化数据源配置

    当然，也可以部分外部化我们的数据源配置。例如，我们可以在工厂方法中定义一些基本的数据源属性：

    ```java
    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username("SA");
        dataSourceBuilder.password("");
        return dataSourceBuilder.build();
    }
    ```

    然后，我们可以在application.properties文件中指定一些额外的：

    ```java
    spring.datasource.url=jdbc:h2:mem:test
    spring.datasource.driver-class-name=org.h2.Driver
    ```

    在外部源中定义的属性，例如上述application.properties文件，或通过[@ConfigurationProperties](https://www.baeldung.com/configuration-properties-in-spring-boot)注释的类，将覆盖Java API中定义的属性。

    很明显，通过这种方法，我们将不再将数据源配置设置存储在一个地方。

    另一方面，它允许我们很好地将编译时和运行时配置设置彼此分开。

    这真的很好，因为它允许我们轻松设置配置绑定点。通过这种方式，我们可以包含来自其他来源的不同数据源设置，而无需重构我们的Bean工厂方法。

5. 测试数据源配置

    测试我们的自定义数据源配置非常简单。整个过程归结为创建JPA实体，定义基本存储库接口，并测试存储库层。

    1. 创建JPA实体

        让我们从定义我们的示例JPA实体类开始，它将对用户进行建模：

        ```java
        @Entity
        @Table(name = "users")
        public class User {

            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            private long id;
            private String name;
            private String email;

            // standard constructors / setters / getters / toString
            
        }
        ```

    2. 一个简单的存储库层

        接下来，我们需要实现一个基本的存储库层，它允许我们在上述定义的用户实体类的实例上执行CRUD操作。

        由于我们使用Spring Data JPA，我们不必从头开始创建自己的DAO实现。我们只需要扩展CrudRepository接口来获得一个有效的存储库实现：

        ```java
        @Repository
        public interface UserRepository extends CrudRepository<User, Long> {}
        ```

    3. 测试存储层

        最后，我们需要检查我们编程配置的数据源是否实际工作。我们可以通过集成测试轻松完成：

        ```java
        @RunWith(SpringRunner.class)
        @DataJpaTest
        public class UserRepositoryIntegrationTest {

            @Autowired
            private UserRepository userRepository;
        
            @Test
            public void whenCalledSave_thenCorrectNumberOfUsers() {
                userRepository.save(new User("Bob", "bob@domain.com"));
                List<User> users = (List<User>) userRepository.findAll();
                
                assertThat(users.size()).isEqualTo(1);
            }    
        }
        ```

        UserRepositoryIntegrationTest类不言自明。它只是练习存储库接口的两种CRUD方法来持久和查找实体。

        请注意，无论我们决定以编程方式配置数据源实现，还是将其拆分为Java配置方法和application.properties文件，我们都应该始终获得一个有效的数据库连接。

    4. 运行示例应用程序

        最后，我们可以使用标准的main（）方法运行演示应用程序：

        ```java
        @SpringBootApplication
        public class Application {

            public static void main(String[] args) {
                SpringApplication.run(Application.class, args);
            }

            @Bean
            public CommandLineRunner run(UserRepository userRepository) throws Exception {
                return (String[] args) -> {
                    User user1 = new User("John", "john@domain.com");
                    User user2 = new User("Julie", "julie@domain.com");
                    userRepository.save(user1);
                    userRepository.save(user2);
                    userRepository.findAll().forEach(user -> System.out.println(user);
                };
            }
        }
        ```

        我们已经测试了存储库层，因此我们确信我们的数据源已成功配置。因此，如果我们运行示例应用程序，我们应该在控制台输出中看到存储在数据库中的用户实体列表。

6. 结论

    在本文中，我们学习了如何在Spring Boot中以编程方式配置DataSource实现。
