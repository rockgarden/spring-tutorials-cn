# [使用 Spring Boot 创建自定义启动器](https://www.baeldung.com/spring-boot-custom-starter)

1. 概述

    Spring Boot 核心开发人员为大多数流行的开源项目提供了启动器，但我们并不局限于这些。

    我们还可以编写自己的自定义启动器。如果我们有一个内部库供组织内部使用，那么如果要在 Spring Boot 上下文中使用该库，为其编写启动器也是一个不错的做法。

    这些启动器可以让开发人员避免冗长的配置，快速启动开发。然而，由于后台发生了很多事情，有时很难理解注解或在 pom.xml 中包含依赖关系是如何实现这么多特性的。

    在本文中，我们将揭开 Spring Boot 魔法的神秘面纱，看看幕后发生了什么。然后，我们将利用这些概念为自己的自定义库创建一个启动器。

2. 揭开 Spring Boot 自动配置的神秘面纱

    1. 自动配置类

        Spring Boot 启动时，会在类路径中查找名为 spring.factories 的文件。该文件位于 META-INF 目录中。让我们看看 [spring-boot-autoconfigure](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/resources/META-INF/spring.factories) 项目中该文件的一个片段：

        ```factories
        # Auto Configure
        org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
        org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
        org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,\
        org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,\
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
        ```

        该文件将名称映射到 Spring Boot 将尝试运行的不同配置类。因此，根据此代码段，Spring Boot 将尝试运行 RabbitMQ、Cassandra、MongoDB 和 Hibernate 的所有配置类。

        这些类是否会实际运行将取决于类路径上是否存在依赖类。例如，如果在类路径上找到 MongoDB 的类，[MongoAutoConfiguration](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/mongo/MongoAutoConfiguration.java) 就会运行，所有与 Mongo 相关的 Bean 都会被初始化。

        这种有条件的初始化是通过 [@ConditionalOnClass](http://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/condition/ConditionalOnClass.html) 注解启用的。让我们看看 MongoAutoConfiguration 类的代码片段，了解其用法：

        ```java
        @Configuration
        @ConditionalOnClass(MongoClient.class)
        @EnableConfigurationProperties(MongoProperties.class)
        @ConditionalOnMissingBean(type = "org.springframework.data.mongodb.MongoDbFactory")
        public class MongoAutoConfiguration {
            // configuration code
        }
        ```

        现在，如果 [MongoClient](http://mongodb.github.io/mongo-java-driver/) 在类路径中可用，该配置类将如何运行，用默认配置设置初始化的 MongoClient 填充 Spring Bean 工厂。

    2. 来自 application.properties 文件的自定义属性

        Spring Boot 使用一些预先配置的默认值来初始化 Bean。要覆盖这些默认设置，我们通常会在 application.properties 文件中以特定名称声明它们。Spring Boot 容器会自动获取这些属性。

        让我们看看它是如何工作的。

        在 MongoAutoConfiguration 的代码片段中，使用 [MongoProperties](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/mongo/MongoProperties.java) 类声明了 @EnableConfigurationProperties 注解，该类是自定义属性的容器：

        ```java
        @ConfigurationProperties(prefix = "spring.data.mongodb")
        public class MongoProperties {

            private String host;

            // other fields with standard getters and setters
        }
        ```

        前缀加上字段名就构成了 application.properties 文件中的属性名称。因此，要设置 MongoDB 的主机，我们只需在属性文件中写入以下内容：

        `spring.data.mongodb.host = localhost`

        同样，类中其他字段的值也可以通过属性文件来设置。

3. 创建自定义启动器

    根据第 2 节中的概念，要创建自定义启动器，我们需要编写以下组件：

    - 库的自动配置类以及用于自定义配置的属性类。
    - 一个启动器 pom，用于引入库和自动配置项目的依赖关系。

    为了演示，我们创建了一个[简单的问候语库](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-custom-starter)，它将接收一天中不同时间段的问候语信息作为配置参数，并输出问候语信息。我们还将创建一个 Spring Boot 示例应用程序，以演示 autoconfigure 和启动器模块的用法。

    1. 自动配置模块

        我们将把自动配置模块称为 greeter-spring-boot-autoconfigure。该模块将包含两个主要类，即 GreeterProperties（通过 application.properties 文件设置自定义属性）和 GreeterAutoConfiguartion（为 greeter 库创建 Bean）。

        让我们看看这两个类的代码：

        ```java
        @ConfigurationProperties(prefix = "baeldung.greeter")
        public class GreeterProperties {

            private String userName;
            private String morningMessage;
            private String afternoonMessage;
            private String eveningMessage;
            private String nightMessage;

            // standard getters and setters

        }

        @Configuration
        @ConditionalOnClass(Greeter.class)
        @EnableConfigurationProperties(GreeterProperties.class)
        public class GreeterAutoConfiguration {

            @Autowired
            private GreeterProperties greeterProperties;

            @Bean
            @ConditionalOnMissingBean
            public GreetingConfig greeterConfig() {

                String userName = greeterProperties.getUserName() == null
                ? System.getProperty("user.name") 
                : greeterProperties.getUserName();
                
                // ..

                GreetingConfig greetingConfig = new GreetingConfig();
                greetingConfig.put(USER_NAME, userName);
                // ...
                return greetingConfig;
            }

            @Bean
            @ConditionalOnMissingBean
            public Greeter greeter(GreetingConfig greetingConfig) {
                return new Greeter(greetingConfig);
            }
        }
        ```

        我们还需要在 src/main/resources/META-INF 目录中添加一个 spring.factories 文件，内容如下：

        ```factorie
        org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
        com.baeldung.greeter.autoconfigure.GreeterAutoConfiguration
        ```

        在应用程序启动时，如果类路径中存在 Greeter 类，GreeterAutoConfiguration 类将运行。如果运行成功，它将通过 GreeterProperties 类读取属性，用 GreeterConfig 和 Greeter Bean 填充 Spring 应用上下文。

        [@ConditionalOnMissingBean](http://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean.html) 注解将确保这些 Bean 只有在不存在的情况下才会被创建。这样，开发人员就可以通过在 @Configuration 类中定义自己的 Bean 来完全覆盖自动配置的 Bean。

    2. 创建 pom.xml

        现在让我们创建启动 pom，它将引入自动配置模块和 greeter 库的依赖关系。

        根据命名规则，所有非 Spring Boot 核心团队管理的启动器都应以库名开头，后缀为 -spring-boot-starter 。因此，我们将把启动器称为 greeter-spring-boot-starter：

        ```xml
        <project ...>
            <modelVersion>4.0.0</modelVersion>

            <groupId>com.baeldung</groupId>
            <artifactId>greeter-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>

            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <greeter.version>0.0.1-SNAPSHOT</greeter.version>
                <spring-boot.version>2.2.6.RELEASE</spring-boot.version>
            </properties>

            <dependencies>

                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter</artifactId>
                    <version>${spring-boot.version}</version>
                </dependency>

                <dependency>
                    <groupId>com.baeldung</groupId>
                    <artifactId>greeter-spring-boot-autoconfigure</artifactId>
                    <version>${project.version}</version>
                </dependency>

                <dependency>
                    <groupId>com.baeldung</groupId>
                    <artifactId>greeter</artifactId>
                    <version>${greeter.version}</version>
                </dependency>

            </dependencies>

        </project>
        ```

    3. 使用启动程序

        让我们创建将使用启动器的 greeter-spring-boot-sample-app。我们需要在 pom.xml 中将其添加为依赖关系：

        ```xml
        <dependency>
            <groupId>com.baeldung</groupId>
            <artifactId>greeter-spring-boot-starter</artifactId>
            <version>${greeter-starter.version}</version>
        </dependency>
        ```

        Spring Boot 会自动配置一切，我们将拥有一个可以注入和使用的 Greeter Bean。

        我们还可以在 application.properties 文件中使用 baeldung.greeter 前缀定义 GreeterProperties，从而更改其中一些默认值：

        ```properties
        baeldung.greeter.userName=Baeldung
        baeldung.greeter.afternoonMessage=Woha\ Afternoon
        ```

        最后，让我们在应用程序中使用 Greeter Bean：

        ```java
        @SpringBootApplication
        public class GreeterSampleApplication implements CommandLineRunner {

            @Autowired
            private Greeter greeter;

            public static void main(String[] args) {
                SpringApplication.run(GreeterSampleApplication.class, args);
            }

            @Override
            public void run(String... args) throws Exception {
                String message = greeter.greet();
                System.out.println(message);
            }
        }
        ```

4. 总结

    在这篇快速教程中，我们重点介绍了如何推出自定义 Spring Boot 启动程序，以及这些启动程序和自动配置机制如何在后台工作，从而省去大量手动配置工作。
