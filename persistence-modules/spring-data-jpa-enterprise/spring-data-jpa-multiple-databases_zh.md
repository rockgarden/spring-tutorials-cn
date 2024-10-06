# [Spring JPA – 多个数据库](https://www.baeldung.com/spring-data-jpa-multiple-databases)

1. 一览表

    在本教程中，我们将为具有多个数据库的Spring Data JPA系统实现一个简单的Spring配置。

2. 实体

    首先，让我们创建两个简单的实体，每个实体都位于一个单独的数据库中。

    这是第一个用户实体：

    ```java
    package com.baeldung.multipledb.model.user;

    @Entity
    @Table(schema = "users")
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private int id;

        private String name;

        @Column(unique = true, nullable = false)
        private String email;

        private int age;
    }
    ```

    这是第二个实体，产品：

    ```java
    package com.baeldung.multipledb.model.product;

    @Entity
    @Table(schema = "products")
    public class Product {

        @Id
        private int id;

        private String name;

        private double price;
    }
    ```

    我们可以看到，这两个实体也被放置在独立的软件包中。当我们进入配置时，这一点将很重要。

3. JPA存储库

    接下来，让我们来看看我们的两个JPA存储库，UserRepository：

    ```java
    package com.baeldung.multipledb.dao.user;

    public interface UserRepository
    extends JpaRepository<User, Integer> { }
    ```

    和产品存储库：

    ```java
    package com.baeldung.multipledb.dao.product;

    public interface ProductRepository
    extends JpaRepository<Product, Integer> { }
    ```

    再次注意我们如何在不同的软件包中创建这两个存储库。

4. 使用Java配置JPA

    现在我们将进入实际的Spring配置。我们将首先设置两个配置类——一个用于用户，另一个用于产品。

    在每个配置类中，我们需要为用户定义以下接口：

    - DataSource
    - EntityManagerFactory (userEntityManager)
    - TransactionManager (userTransactionManager)

    让我们从查看用户配置开始：

    ```java
    @Configuration
    @PropertySource({ "classpath:persistence-multiple-db.properties" })
    @EnableJpaRepositories(
        basePackages = "com.baeldung.multipledb.dao.user",
        entityManagerFactoryRef = "userEntityManager",
        transactionManagerRef = "userTransactionManager"
    )
    public class PersistenceUserConfiguration {
        @Autowired
        private Environment env;

        @Bean
        @Primary
        public LocalContainerEntityManagerFactoryBean userEntityManager() {
            LocalContainerEntityManagerFactoryBean em
            = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(userDataSource());
            em.setPackagesToScan(
            new String[] { "com.baeldung.multipledb.model.user" });

            HibernateJpaVendorAdapter vendorAdapter
            = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("hibernate.hbm2ddl.auto",
            env.getProperty("hibernate.hbm2ddl.auto"));
            properties.put("hibernate.dialect",
            env.getProperty("hibernate.dialect"));
            em.setJpaPropertyMap(properties);

            return em;
        }

        @Primary
        @Bean
        public DataSource userDataSource() {
    
            DriverManagerDataSource dataSource
            = new DriverManagerDataSource();
            dataSource.setDriverClassName(
            env.getProperty("jdbc.driverClassName"));
            dataSource.setUrl(env.getProperty("user.jdbc.url"));
            dataSource.setUsername(env.getProperty("jdbc.user"));
            dataSource.setPassword(env.getProperty("jdbc.pass"));

            return dataSource;
        }

        @Primary
        @Bean
        public PlatformTransactionManager userTransactionManager() {
    
            JpaTransactionManager transactionManager
            = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(
            userEntityManager().getObject());
            return transactionManager;
        }
    }
    ```

    注意我们如何通过使用@Primary注释bean定义来使用userTransactionManager作为我们的主事务管理器。每当我们打算隐式或显式地注入事务管理器，而不指定名称，这都很有帮助。

    接下来，让我们讨论一下持久性产品配置，我们在其中定义了类似的bean：

    ```java
    @Configuration
    @PropertySource({ "classpath:persistence-multiple-db.properties" })
    @EnableJpaRepositories(
        basePackages = "com.baeldung.multipledb.dao.product", 
        entityManagerFactoryRef = "productEntityManager", 
        transactionManagerRef = "productTransactionManager"
    )
    public class PersistenceProductConfiguration {
        @Autowired
        private Environment env;

        @Bean
        public LocalContainerEntityManagerFactoryBean productEntityManager() {
            LocalContainerEntityManagerFactoryBean em
            = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(productDataSource());
            em.setPackagesToScan(
            new String[] { "com.baeldung.multipledb.model.product" });

            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("hibernate.hbm2ddl.auto",
            env.getProperty("hibernate.hbm2ddl.auto"));
            properties.put("hibernate.dialect",
            env.getProperty("hibernate.dialect"));
            em.setJpaPropertyMap(properties);

            return em;
        }

        @Bean
        public DataSource productDataSource() {
    
            DriverManagerDataSource dataSource
            = new DriverManagerDataSource();
            dataSource.setDriverClassName(
            env.getProperty("jdbc.driverClassName"));
            dataSource.setUrl(env.getProperty("product.jdbc.url"));
            dataSource.setUsername(env.getProperty("jdbc.user"));
            dataSource.setPassword(env.getProperty("jdbc.pass"));

            return dataSource;
        }

        @Bean
        public PlatformTransactionManager productTransactionManager() {
    
            JpaTransactionManager transactionManager
            = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(
            productEntityManager().getObject());
            return transactionManager;
        }
    }
    ```

5. 简单测试

    最后，让我们测试一下我们的配置。

    为此，我们将创建每个实体的实例，并确保创建它：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest
    @EnableTransactionManagement
    public class JpaMultipleDBIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        @Transactional("userTransactionManager")
        public void whenCreatingUser_thenCreated() {
            User user = new User();
            user.setName("John");
            user.setEmail("john@test.com");
            user.setAge(20);
            user = userRepository.save(user);

            assertNotNull(userRepository.findOne(user.getId()));
        }

        @Test
        @Transactional("userTransactionManager")
        public void whenCreatingUsersWithSameEmail_thenRollback() {
            User user1 = new User();
            user1.setName("John");
            user1.setEmail("john@test.com");
            user1.setAge(20);
            user1 = userRepository.save(user1);
            assertNotNull(userRepository.findOne(user1.getId()));

            User user2 = new User();
            user2.setName("Tom");
            user2.setEmail("john@test.com");
            user2.setAge(10);
            try {
                user2 = userRepository.save(user2);
            } catch (DataIntegrityViolationException e) {
            }

            assertNull(userRepository.findOne(user2.getId()));
        }

        @Test
        @Transactional("productTransactionManager")
        public void whenCreatingProduct_thenCreated() {
            Product product = new Product();
            product.setName("Book");
            product.setId(2);
            product.setPrice(20);
            product = productRepository.save(product);

            assertNotNull(productRepository.findOne(product.getId()));
        }
    }
    ```

6. Spring Boot中的多个数据库

    Spring Boot可以简化上述配置。

    默认情况下，Spring Boot将使用前缀byspring.datasource的配置属性实例化其默认DataSource。*：

    ```properties
    spring.datasource.jdbcUrl = [url]
    spring.datasource.username = [username]
    spring.datasource.password = [password]
    ```

    我们现在希望继续使用相同的方式来配置第二个数据源，但属性命名空间不同：

    ```properties
    spring.second-datasource.jdbcUrl = [url]
    spring.second-datasource.username = [username]
    spring.second-datasource.password = [password]
    ```

    由于我们希望Spring Boot自动配置来接收这些不同的属性（并实例化两个不同的数据源），我们将定义两个类似于前几节的配置类：

    ```java
    @Configuration
    @PropertySource({"classpath:persistence-multiple-db-boot.properties"})
    @EnableJpaRepositories(
    basePackages = "com.baeldung.multipledb.dao.user",
    entityManagerFactoryRef = "userEntityManager",
    transactionManagerRef = "userTransactionManager")
    public class PersistenceUserAutoConfiguration {

        @Primary
        @Bean
        @ConfigurationProperties(prefix="spring.datasource")
        public DataSource userDataSource() {
            return DataSourceBuilder.create().build();
        }
        // userEntityManager bean 

        // userTransactionManager bean
    }

    @Configuration
    @PropertySource({"classpath:persistence-multiple-db-boot.properties"})
    @EnableJpaRepositories(
    basePackages = "com.baeldung.multipledb.dao.product", 
    entityManagerFactoryRef = "productEntityManager", 
    transactionManagerRef = "productTransactionManager")
    public class PersistenceProductAutoConfiguration {
    
        @Bean
        @ConfigurationProperties(prefix="spring.second-datasource")
        public DataSource productDataSource() {
            return DataSourceBuilder.create().build();
        }
    
        // productEntityManager bean 

        // productTransactionManager bean
    }
    ```

    现在，我们已经根据引导自动配置惯例定义了持久性-多倍数-启动.属性中的数据源属性。

    有趣的部分是使用@ConfigurationProperties注释数据源bean创建方法。我们只需要指定相应的配置前缀。在此方法中，我们使用DataSourceBuilder，Spring Boot将自动处理其余部分。

    但配置的属性如何注入到DataSource配置中？

    在DataSourceBuilder上调用build（）方法时，它将调用其私有bind（）方法：

    ```java
    public T build() {
        Class<? extends DataSource> type = getType();
        DataSource result = BeanUtils.instantiateClass(type);
        maybeGetDriverClassName();
        bind(result);
        return (T) result;
    }
    ```

    这种私有方法执行了大部分自动配置魔术，将已解析的配置绑定到actualDataSource实例：

    ```java
    private void bind(DataSource result) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(this.properties);
        ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("url", "jdbc-url");
        aliases.addAliases("username", "user");
        Binder binder = new Binder(source.withAliases(aliases));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result));
    }
    ```

    虽然我们不必自己接触任何这些代码，但了解Spring Boot自动配置引擎盖下发生了什么仍然很有用。

    除此之外，事务管理器和实体管理器bean配置与标准Spring应用程序相同。

7. 结论

    本文对如何配置我们的Spring Data JPA项目以使用多个数据库进行了实际概述。
