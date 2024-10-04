# [Spring Boot注解](https://www.baeldung.com/spring-boot-annotations)

1. 概述

    Spring Boot通过其自动配置功能使配置Spring更加容易。

    在这个快速教程中，我们将探讨org.springframework.boot.autoconfigure和org.springframework.boot.autoconfigure.condition包的注释。

2. @SpringBootApplication

    我们使用这个注解来标记Spring Boot应用程序的主类：

    ```java
    @SpringBootApplication
    class VehicleFactoryApplication {
        public static void main(String[] args) {
            SpringApplication.run（VehicleFactoryApplication.class，args）；
        }
    }
    ```

    @SpringBootApplication封装了@Configuration、@EnableAutoConfiguration和@ComponentScan注解及其默认属性。

3. @EnableAutoConfiguration

    @EnableAutoConfiguration，正如它的名字一样，启用自动配置。这意味着Spring Boot会在其classpath上寻找自动配置Bean并自动应用它们。

    注意，我们必须将此注解与@Configuration一起使用：

    ```java
    @Configuration
    @EnableAutoConfiguration
    class VehicleFactoryConfig {}
    ```

4. 自动配置条件

    通常情况下，当我们编写自定义自动配置时，我们希望Spring有条件地使用它们。我们可以通过本节中的注解实现这一点。

    我们可以将本节中的注解放在@Configuration类或@Bean方法上。

    在接下来的章节中，我们将只介绍每个条件背后的基本概念。更多信息，请访问这篇[文章](https://www.baeldung.com/spring-boot-custom-auto-configuration)。

    1. @ConditionalOnClass和@ConditionalOnMissingClass

        使用这些条件，Spring将只在注解参数中的类存在/不存在的情况下使用标记的自动配置Bean：

        ```java
        @Configuration
        @ConditionalOnClass(DataSource.class)
        class MySQLAutoconfiguration { }
        ```

    2. @ConditionalOnBean和@ConditionalOnMissingBean

        当我们想根据特定Bean的存在或不存在来定义条件时，我们可以使用这些注解：

        ```java
        @Bean
        @ConditionalOnBean(name = "dataSource")
        LocalContainerEntityManagerFactoryBean entityManagerFactory() { }
        ```

    3. @ConditionalOnProperty

        通过这个注解，我们可以对属性的值设置条件：

        ```java
        @Bean
        @ConditionalOnProperty(
            name = "usemysql", 
            havingValue = "local"
        )
        DataSource dataSource() { }
        ```

    4. @ConditionalOnResource

        我们可以让Spring只在特定资源存在的情况下使用一个定义：

        ```java
        @ConditionalOnResource(resources = "classpath:mysql.properties")
        Properties additionalProperties() { }
        ```

    5. @ConditionalOnWebApplication 和 @ConditionalOnNotWebApplication

        通过这些注解，我们可以根据当前应用程序是否是Web应用程序来创建条件：

        ```java
        @ConditionalOnWebApplication
        HealthCheckController healthCheckController() { }
        ```

    6. @ConditionalExpression

        我们可以在更复杂的情况下使用这个注解。当SpEL表达式被评估为真时，Spring将使用标记的定义：

        ```java
        @Bean
        @ConditionalOnExpression("${usemysql} && ${mysqlserver == 'local'}")
        DataSource dataSource() { }
        ```

        4.7. @Conditional

        对于更复杂的条件，我们可以创建一个评估自定义条件的类。我们用@Conditional告诉Spring要使用这个自定义条件：

        ```java
        @Conditional(HibernateCondition.class)
        Properties additionalProperties() { }
        ```

5. 总结

    在这篇文章中，我们看到了关于如何对自动配置过程进行微调以及为自定义自动配置豆提供条件的概述。
