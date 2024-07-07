# 在Spring Boot中使用application.yml与application.properties

1. 概述

    Spring Boot的一个常见做法是使用外部配置来定义我们的属性。这使我们能够在不同的环境中使用相同的应用程序代码。
    我们可以使用属性文件、YAML文件、环境变量和命令行参数。
    在这个简短的教程中，我们将探讨属性和YAML文件的主要区别。
2. 属性配置

    默认情况下，Spring Boot可以访问application.properties文件中设置的配置，该文件使用键值格式：

    ```properties
    spring.datasource.url=jdbc:h2:dev
    spring.datasource.username=SA
    spring.datasource.password=password
    ```

    这里每一行都是一个单独的配置，所以我们需要通过使用相同的键的前缀来表达分层的数据。而在这个例子中，每个键都属于spring.datasource。

    1. 属性中的占位符

        在我们的值中，我们可以使用${}语法的占位符来引用其他键、系统属性或环境变量的内容：

        ```properties
        app.name=MyApp
        app.description=${app.name} is a Spring Boot application
        ```

    2. 列表结构

        如果我们有相同种类的属性但数值不同，我们可以用数组索引来表示列表结构：

        ```properties
        application.servers[0].ip=127.0.0.1
        application.servers[0].path=/path1
        application.servers[1].ip=127.0.0.2
        application.servers[1].path=/path2
        application.servers[2].ip=127.0.0.3
        application.servers[2].path=/path3
        ```

    3. 多个配置文件

        从2.4.0版本开始，Spring Boot支持创建多文档属性文件。简单地说，我们可以把一个物理文件分成多个逻辑文件。
        这使我们能够为我们需要声明的每个配置文件定义一个文件，都在同一个文件中：

        ```properties
        logging.file.name=myapplication.log
        bael.property=defaultValue
        #---
        spring.config.activate.on-profile=dev
        spring.datasource.password=password
        spring.datasource.url=jdbc:h2:dev
        spring.datasource.username=SA
        bael.property=devValue
        #---
        spring.config.activate.on-profile=prod
        spring.datasource.password=password
        spring.datasource.url=jdbc:h2:prod
        spring.datasource.username=prodUser
        bael.property=prodValue
        ```

        注意我们使用'#----'符号来表示我们要分割文件的地方。
        在这个例子中，我们有两个spring部分，标记了不同的配置文件。另外，我们可以在根层有一套共同的属性--在这种情况下，logging.file.name属性在所有配置文件中都是一样的。
    4. 跨越多个文件的配置文件

        作为同一文件中不同配置文件的替代方案，我们可以在不同文件中存储多个配置文件。在2.4.0版本之前，这是唯一可用于属性文件的方法。
        我们通过将配置文件的名称放在文件名中来实现这一点--例如，application-dev.yml 或 application-dev.properties。
3. YAML 配置

    1. YAML 格式

        除了Java属性文件外，我们还可以在Spring Boot应用程序中使用基于YAML的配置文件。YAML 是一种方便的格式，用于指定分层的配置数据。
        现在让我们从我们的属性文件中抽取同样的例子，并将其转换为YAML：

        ```yaml
        spring:
            datasource:
                password: password
                url: jdbc:h2:dev
                username: SA
        ```

        这可能比其属性文件的替代方案更易读，因为它不包含重复的前缀。
    2. 列表结构

        YAML 有一个更简洁的格式来表达列表：

        ```yaml
        application:
            servers:
            -   ip: '127.0.0.1'
                path: '/path1'
            -   ip: '127.0.0.2'
                path: '/path2'
            -   ip: '127.0.0.3'
                path: '/path3'
        ```

    3. 多个配置文件

        与属性文件不同，YAML在设计上支持多文档文件，这样一来，无论我们使用哪个版本的Spring Boot，都可以在同一个文件中存储多个配置文件。
        但在这种情况下，规范表明我们必须使用三个破折号来表示新文件的开始：

        ```yaml
        logging:
        file:
            name: myapplication.log
        ---
        spring:
        config:
            activate:
            on-profile: staging
        datasource:
            password: 'password'
            url: jdbc:h2:staging
            username: SA
        bael:
        property: stagingValue
        ```

        注意：我们通常不希望在项目中同时包含标准的application.properties和application.yml文件，因为这可能导致意想不到的结果。
        例如，如果我们把上面显示的属性（在application.yml文件中）和第2.3节中描述的属性结合起来，那么bael.property将被分配为defaultValue，而不是配置文件的特定值。这只是因为application.properties后来被加载，覆盖了到那时为止分配的值。
4. Spring Boot的使用

    现在我们已经定义了我们的配置，让我们看看如何访问它们。
    1. 值注解

        我们可以使用@Value注解注入我们的属性值：

        ```java
        @Value("${key.something}")
        private String injectedProperty;
        ```

        在这里，属性key.something通过字段注入被注入到我们的一个对象中。
    2. 环境抽象

        我们也可以使用环境API获得一个属性的值：

        ```java
        @Autowired
        private Environment env;
        public String getSomeKey(){
            return env.getProperty("key.something");
        }
        ```

    3. ConfigurationProperties注解

        最后，我们还可以使用@ConfigurationProperties注解，将我们的属性绑定到类型安全的结构化对象上：

        ```java
        @ConfigurationProperties(prefix = "mail")
        public class ConfigProperties {
            String name;
            String description;
        ...
        ```

5. 总结

    在这篇文章中，我们看到了properties和yml Spring Boot配置文件之间的一些区别。我们还看到了它们的值是如何引用其他属性的。最后，我们研究了如何将这些值注入到我们的运行时中。

## Relevant Articles

- [How to Define a Map in YAML for a POJO?](https://www.baeldung.com/yaml-map-pojo)
- [x] [Using application.yml vs application.properties in Spring Boot](https://www.baeldung.com/spring-boot-yaml-vs-properties)
- [Load Spring Boot Properties From a JSON File](https://www.baeldung.com/spring-boot-json-properties)
- [IntelliJ – Cannot Resolve Spring Boot Configuration Properties Error](https://www.baeldung.com/intellij-resolve-spring-boot-configuration-properties)
- [Log Properties in a Spring Boot Application](https://www.baeldung.com/spring-boot-log-properties)
- [Using Environment Variables in Spring Boot’s application.properties](https://www.baeldung.com/spring-boot-properties-env-variables)
- [Loading Multiple YAML Configuration Files in Spring Boot](https://www.baeldung.com/spring-boot-load-multiple-yaml-configuration-files)
- More articles: [[<-- prev]](../spring-boot-properties-2/README-zh.md)

## Code

一如既往，所有的代码实例都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-properties-3)上找到。
