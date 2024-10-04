# [禁用 Spring 数据自动配置](https://www.baeldung.com/spring-data-disable-auto-config)

1. 简介

    在本快速教程中，我们将探讨在 Spring Boot 中禁用数据库自动配置的两种不同方法。这在[测试时](https://www.baeldung.com/spring-boot-exclude-auto-configuration-test)非常有用。

    我们将举例说明 Redis、MongoDB 和 Spring Data JPA。

    我们将首先了解基于注解的方法，然后了解属性文件方法。

2. 使用注解禁用

    让我们从 [MongoDB](https://www.baeldung.com/spring-data-mongodb-tutorial) 示例开始。我们来看看需要排除的类：

    ```java
    @SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
    })
    ```

    同样，我们来看看如何禁用 [Redis](https://www.baeldung.com/spring-data-redis-tutorial) 的自动配置：

    ```java
    @SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoryAutoConfiguration.class
    })
    ```

    最后，我们来看看如何禁用 Spring Data JPA 的自动配置：

    ```java
    @SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    })
    ```

3. 使用属性文件禁用

    我们还可以使用属性文件禁用自动配置。

    我们先用 MongoDB 来探讨一下：

    ```properties
    spring.autoconfigure.exclude= \
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration, \
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
    ```

    现在我们来禁用 Redis：

    ```properties
    spring.autoconfigure.exclude= \
    org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration, \
    org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
    ```

    最后，我们将禁用 Spring Data JPA：

    ```properties
    spring.autoconfigure.exclude= \
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, \
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration, \
    org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
    ```

4. 测试

    测试时，我们将检查[应用程序上下文](https://www.baeldung.com/spring-web-contexts)中是否不存在自动配置类的 Spring Bean。

    我们先测试 MongoDB。我们将验证 MongoTemplate Bean 是否不存在：

    ```java
    @Test(expected = NoSuchBeanDefinitionException.class)
    public void givenAutoConfigDisabled_whenStarting_thenNoAutoconfiguredBeansInContext() {
        context.getBean(MongoTemplate.class); 
    }
    ```

    现在我们将检查 JPA. 对于 JPA，DataSource Bean 将不存在：

    ```java
    @Test(expected = NoSuchBeanDefinitionException.class)
    public void givenAutoConfigDisabled_whenStarting_thenNoAutoconfiguredBeansInContext() {
        context.getBean(DataSource.class);
    }
    ```

    最后，对于 Redis，我们将检查应用程序上下文中的 RedisTemplate Bean：

    ```java
    @Test(expected = NoSuchBeanDefinitionException.class)
    public void givenAutoConfigDisabled_whenStarting_thenNoAutoconfiguredBeansInContext() {
        context.getBean(RedisTemplate.class);
    }
    ```

5. 结论

    在这篇简短的文章中，我们了解了如何禁用 Spring Boot 对不同数据库的自动配置。
