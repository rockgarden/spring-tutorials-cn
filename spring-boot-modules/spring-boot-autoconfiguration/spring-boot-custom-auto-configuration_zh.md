# [使用 Spring Boot 创建自定义自动配置](https://www.baeldung.com/spring-boot-custom-auto-configuration)

1. 概述

    简单地说，Spring Boot 自动配置可帮助我们根据类路径上的依赖关系自动配置 Spring 应用程序。

    这样就无需定义自动配置类中包含的某些 Bean，从而使开发变得更快、更简单。

    在下一节中，我们将了解如何创建自定义 Spring Boot 自动配置。

2. Maven 依赖项

    让我们从依赖关系开始：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    ```

    Spring-boot-starter-data-jpa 和 mysql-connector-java 的最新版本可从 Maven Central 下载。

3. 创建自定义自动配置

    为了创建自定义自动配置，我们需要创建一个注释为 @Configuration 的类并将其注册。

    让我们为 MySQL 数据源创建一个自定义配置：

    ```java
    @Configuration
    public class MySQLAutoconfiguration {...}
    ```

    接下来，我们需要将该类注册为自动配置候选类。

    为此，我们要在标准文件 resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 中添加该类的名称：

    `com.baeldung.autoconfiguration.MySQLAutoconfiguration`

    如果我们希望我们的自动配置类优先于其他候选类，我们可以添加 @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE) 注解。

    我们使用标有 @Conditional 注解的类和 Bean 来设计自动配置，这样我们就可以替换自动配置或其特定部分。

    请注意，只有当我们没有在应用程序中定义自动配置的 Bean 时，自动配置才会生效。如果我们定义了自己的 Bean，它将覆盖默认的 Bean。

    1. Class 条件

        类条件允许我们使用 @ConditionalOnClass 注解指定，如果存在指定的类，则包含配置 Bean；或者使用 @ConditionalOnMissingClass 注解指定，如果不存在指定的类，则包含配置 Bean。

        让我们指定 MySQLConfiguration 仅在 DataSource 类存在时加载，在这种情况下，我们可以假设应用程序将使用数据库：

        ```java
        @Configuration
        @ConditionalOnClass(DataSource.class)
        public class MySQLAutoconfiguration {...}
        ```

    2. Bean 条件

        如果我们只想在指定 Bean 存在或不存在的情况下包含一个 Bean，我们可以使用 @ConditionalOnBean 和 @ConditionalOnMissingBean 注解。

        为了了解这一点，让我们在配置类中添加一个 entityManagerFactory Bean。

        首先，我们将指定，只有在名为 dataSource 的 Bean 存在且名为 entityManagerFactory 的 Bean 尚未定义的情况下，我们才要创建此 Bean：

        ```java
        @Bean
        @ConditionalOnBean(name = "dataSource")
        @ConditionalOnMissingBean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean em
            = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan("com.baeldung.autoconfiguration.example");
            em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            if (additionalProperties() != null) {
                em.setJpaProperties(additionalProperties());
            }
            return em;
        }
        ```

        我们还将配置一个 transactionManager Bean，只有当我们尚未定义 JpaTransactionManager 类型的 Bean 时，它才会加载：

        ```java
        @Bean
        @ConditionalOnMissingBean(type = "JpaTransactionManager")
        JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory);
            return transactionManager;
        }
        ```

    3. properties 条件

        我们使用 @ConditionalOnProperty 注解来指定是否根据 Spring Environment 属性的存在和值加载配置。

        首先，让我们为配置添加一个属性源文件，它将决定从哪里读取属性：

        ```java
        @PropertySource("classpath:mysql.properties")
        public class MySQLAutoconfiguration {...}
        ```

        我们可以配置用于创建数据库连接的主 DataSource Bean，使其仅在名为 usemysql 的属性存在时加载。

        我们可以使用属性 havingValue 来指定必须匹配的 usemysql 属性的某些值。

        现在，让我们使用默认值定义 dataSource Bean，如果我们将 usemysql 属性设置为本地，它将连接到名为 myDb 的本地数据库：

        ```java
        @Bean
        @ConditionalOnProperty(
        name = "usemysql", 
        havingValue = "local")
        @ConditionalOnMissingBean
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();

            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://localhost:3306/myDb?createDatabaseIfNotExist=true");
            dataSource.setUsername("mysqluser");
            dataSource.setPassword("mysqlpass");

            return dataSource;
        }
        ```

        如果将 usemysql 属性设置为自定义，我们将使用数据库 URL、用户和密码的自定义属性值配置 dataSource Bean：

        ```java
        @Bean(name = "dataSource")
        @ConditionalOnProperty(
        name = "usemysql", 
        havingValue = "custom")
        @ConditionalOnMissingBean
        public DataSource dataSource2() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();

            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(env.getProperty("mysql.url"));
            dataSource.setUsername(env.getProperty("mysql.user") != null 
            ? env.getProperty("mysql.user") : "");
            dataSource.setPassword(env.getProperty("mysql.pass") != null 
            ? env.getProperty("mysql.pass") : "");
                
            return dataSource;
        }
        ```

        mysql.properties 文件将包含 usemysql 属性：

        `usemysql=local`

        使用 MySQLAutoconfiguration 的应用程序可能需要覆盖默认属性。在这种情况下，只需在 mysql.properties 文件中为 mysql.url、mysql.user 和 mysql.pass 属性以及 usemysql=custom 行添加不同的值即可。

    4. 资源条件

        添加 @ConditionalOnResource 注解意味着，只有当指定资源存在时，配置才会加载。

        让我们定义一个名为 additionalProperties() 的方法，只有当资源文件 mysql.properties 存在时，该方法才会返回一个包含 Hibernate 特定属性的 Properties 对象，供实体管理器工厂 Bean 使用：

        ```java
        @ConditionalOnResource(
        resources = "classpath:mysql.properties")
        @Conditional(HibernateCondition.class)
        Properties additionalProperties() {
            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty("hibernate.hbm2ddl.auto", 
            env.getProperty("mysql-hibernate.hbm2ddl.auto"));
            hibernateProperties.setProperty("hibernate.dialect", 
            env.getProperty("mysql-hibernate.dialect"));
            hibernateProperties.setProperty("hibernate.show_sql", 
            env.getProperty("mysql-hibernate.show_sql") != null 
            ? env.getProperty("mysql-hibernate.show_sql") : "false");
            return hibernateProperties;
        }
        ```

        我们可以将 Hibernate 特有的属性添加到 mysql.properties 文件中：

        ```properties
        mysql-hibernate.dialect=org.hibernate.dialect.MySQLDialect
        mysql-hibernate.show_sql=true
        mysql-hibernate.hbm2ddl.auto=create-drop
        ```

    5. 自定义条件

        假设我们不想使用 Spring Boot 中的任何可用条件。

        我们也可以通过扩展 SpringBootCondition 类并覆盖 getMatchOutcome() 方法来定义自定义条件。

        让我们为 additionalProperties() 方法创建一个名为 HibernateCondition 的条件，该条件将验证类路径上是否存在 HibernateEntityManager 类：

        ```java
        static class HibernateCondition extends SpringBootCondition {

            private static String[] CLASS_NAMES
            = { "org.hibernate.ejb.HibernateEntityManager", 
                "org.hibernate.jpa.HibernateEntityManager" };

            @Override
            public ConditionOutcome getMatchOutcome(ConditionContext context, 
            AnnotatedTypeMetadata metadata) {
        
                ConditionMessage.Builder message
                = ConditionMessage.forCondition("Hibernate");
                return Arrays.stream(CLASS_NAMES)
                .filter(className -> ClassUtils.isPresent(className, context.getClassLoader()))
                .map(className -> ConditionOutcome
                    .match(message.found("class")
                    .items(Style.NORMAL, className)))
                .findAny()
                .orElseGet(() -> ConditionOutcome
                    .noMatch(message.didNotFind("class", "classes")
                    .items(Style.NORMAL, Arrays.asList(CLASS_NAMES))));
            }
        }
        ```

        然后，我们可以将该条件添加到 additionalProperties() 方法中：

        ```java
        @Conditional(HibernateCondition.class)
        Properties additionalProperties() {...}
        ```

    6. 应用程序条件

        我们还可以指定配置只能在网络上下文 inside/outside 加载。为此，我们可以添加 @ConditionalOnWebApplication 或 @ConditionalOnNotWebApplication 注解。

4. 测试自动配置

    让我们创建一个非常简单的示例来测试自动配置。

    我们将使用 Spring Data 创建一个名为 MyUser 的实体类和一个 MyUserRepository 接口：

    ```java
    @Entity
    public class MyUser {
        @Id
        private String email;
        // standard constructor, getters, setters
    }

    public interface MyUserRepository 
    extends JpaRepository<MyUser, String> {...}
    ```

    为了启用自动配置，我们可以使用 @SpringBootApplication 或 @EnableAutoConfiguration 注解：

    ```java
    @SpringBootApplication
    public class AutoconfigurationApplication {
        public static void main(String[] args) {
            SpringApplication.run(AutoconfigurationApplication.class, args);
        }
    }
    ```

    接下来，让我们编写一个保存 MyUser 实体的 JUnit 测试：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(
    classes = AutoconfigurationApplication.class)
    @EnableJpaRepositories(
    basePackages = { "com.baeldung.autoconfiguration.example" })
    public class AutoconfigurationLiveTest {

        @Autowired
        private MyUserRepository userRepository;

        @Test
        public void whenSaveUser_thenOk() {
            MyUser user = new MyUser("user@email.com");
            userRepository.save(user);
        }
    }
    ```

    由于我们没有定义数据源配置，应用程序将使用我们创建的自动配置连接到名为 myDb 的 MySQL 数据库。

    连接字符串包含 createDatabaseIfNotExist=true 属性，因此数据库无需存在。不过，需要创建用户 mysqluser，或通过 mysql.user 属性指定的用户（如果存在）。

    我们可以检查应用程序日志，查看是否使用了 MySQL 数据源：

    `web - 2017-04-12 00:01:33,956 [main] INFO  o.s.j.d.DriverManagerDataSource - Loaded JDBC driver: com.mysql.cj.jdbc.Driver`

5. 禁用自动配置类

    比方说，我们想从加载中排除自动配置。

    我们可以在配置类中添加带有 exclude 或 excludeName 属性的 @EnableAutoConfiguration 注解：

    ```java
    @Configuration
    @EnableAutoConfiguration(
    exclude={MySQLAutoconfiguration.class})
    public class AutoconfigurationApplication {...}
    ```

    我们还可以设置 spring.autoconfigure.exclude 属性：

    `spring.autoconfigure.exclude=com.baeldung.autoconfiguration.MySQLAutoconfiguration`

6. 结论

    在本文中，我们展示了如何创建自定义 Spring Boot 自动配置。

    可以使用自动配置文件 mvn clean install -Pautoconfiguration 运行 JUnit 测试。
