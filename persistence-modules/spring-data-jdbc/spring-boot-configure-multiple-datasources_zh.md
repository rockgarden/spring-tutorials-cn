# [在 Spring Boot 中配置和使用多个数据源](https://www.baeldung.com/spring-boot-configure-multiple-datasources)

1. 概述

    Spring Boot 应用程序的典型应用场景是将数据存储在单个关系数据库中。但有时我们需要访问多个数据库。

    在本教程中，我们将学习如何使用 Spring Boot 配置和使用多个数据源。

    要了解如何处理单个数据源，请查看我们的 [Spring Data JPA 介绍](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)。

2. 默认行为

    让我们回忆一下在 application.yml 中声明 Spring Boot 数据源的样子：

    ```yml
    spring:
    datasource:
        url: ...
        username: ...
        password: ...
        driverClassname: ...
    ```

    在内部，Spring 会将这些设置映射到 org.springframework.boot.autoconfigure.jdbc.DataSourceProperties 的实例。

    让我们来看看其实现：

    ```java
    @ConfigurationProperties(prefix = "spring.datasource")
    public class DataSourceProperties implements BeanClassLoaderAware, InitializingBean {

        // ...

        /**
         * Fully qualified name of the JDBC driver. Auto-detected based on the URL by default.
         */
        private String driverClassName;

        /**
         * JDBC URL of the database.
         */
        private String url;

        /**
         * Login username of the database.
         */
        private String username;

        /**
         * Login password of the database.
         */
        private String password;

        // ...

    }
    ```

    我们应该指出 @ConfigurationProperties 注解可将配置属性自动映射到 Java 对象。

3. 扩展默认值

    因此，要使用多个数据源，我们需要在 Spring 应用程序上下文中声明具有不同映射的多个 Bean。

    我们可以通过使用配置类来实现这一点：

    ```java
    @Configuration
    public class TodoDatasourceConfiguration {
        @Bean
        @ConfigurationProperties("spring.datasource.todos")
        public DataSourceProperties todosDataSourceProperties() {
            return new DataSourceProperties();
        }
    }

    @Configuration
    public class TopicDatasourceConfiguration {
        @Bean
        @ConfigurationProperties("spring.datasource.topics")
        public DataSourceProperties topicsDataSourceProperties() {
            return new DataSourceProperties();
        }
    }
    ```

    数据源的配置必须如下所示：

    ```yml
    spring:
    datasource:
        todos:
            url: ...
            username: ...
            password: ...
            driverClassName: ...
        topics:
            url: ...
            username: ...
            password: ...
            driverClassName: ...
    ```

    然后，我们可以使用 DataSourceProperties 对象创建数据源：

    ```java
    @Bean
    public DataSource todosDataSource() {
        return todosDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
    }

    @Bean
    public DataSource topicsDataSource() {
        return topicsDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
    }
    ```

4. Spring Data JDBC

    使用 Spring Data JDBC 时，我们还需要为每个数据源配置一个 JdbcTemplate 实例：

    ```java
    @Bean
    public JdbcTemplate todosJdbcTemplate(@Qualifier("todosDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public JdbcTemplate topicsJdbcTemplate(@Qualifier("topicsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    ```

    然后，我们还可以通过指定 @Qualifier 来使用它们：

    ```java
    @Autowired
    @Qualifier("topicsJdbcTemplate")
    JdbcTemplate jdbcTemplate;
    ```

5. Spring Data JPA

    使用 Spring Data JPA 时，我们希望使用类似下面的存储库，其中 Todo 是实体：

    `public interface TodoRepository extends JpaRepository<Todo, Long> {}`

    因此，我们需要为每个数据源声明 EntityManager 工厂：

    ```java
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = Todo.class,
        entityManagerFactoryRef = "todosEntityManagerFactory",
        transactionManagerRef = "todosTransactionManager"
        )
    public class TodoJpaConfiguration {

        @Bean
        public LocalContainerEntityManagerFactoryBean todosEntityManagerFactory(
        @Qualifier("todosDataSource") DataSource dataSource,
        EntityManagerFactoryBuilder builder) {
            return builder
                .dataSource(dataSource)
                .packages(Todo.class)
                .build();
        }

        @Bean
        public PlatformTransactionManager todosTransactionManager(
        @Qualifier("todosEntityManagerFactory") LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
            return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
        }

    }
    ```

    让我们来看看我们应该注意的一些限制。

    我们需要拆分软件包，以便为每个数据源提供一个 @EnableJpaRepositories。

    不幸的是，要获得 EntityManagerFactoryBuilder 的注入，我们需要将其中一个数据源声明为 @Primary。

    这是因为 EntityManagerFactoryBuilder 是在 org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration 中声明的，而该类需要注入单一数据源。通常，框架的某些部分可能不希望配置多个数据源。

6. 配置 Hikari 连接池

    如果我们要配置 [Hikari](https://www.baeldung.com/spring-boot-hikari)，只需在数据源定义中添加 @ConfigurationProperties：

    ```java
    @Bean
    @ConfigurationProperties("spring.datasource.todos.hikari")
    public DataSource todosDataSource() {
        return todosDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
    }
    ```

    然后，我们可以在 application.properties 文件中插入以下几行：

    ```properties
    spring.datasource.todos.hikari.connectionTimeout=30000
    spring.datasource.todos.hikari.idleTimeout=600000
    spring.datasource.todos.hikari.maxLifetime=1800000
    ```

7. 结论

    在本文中，我们学习了如何使用 Spring Boot 配置多个数据源。

    我们看到，我们需要进行一些配置，而且在偏离标准时可能会有陷阱，但最终还是可以做到的。
