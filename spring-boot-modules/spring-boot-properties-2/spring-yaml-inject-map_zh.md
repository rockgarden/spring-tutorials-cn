# [用Spring从YAML文件中注入一个Map](https://www.baeldung.com/spring-yaml-inject-map)

1. 概述

    在这个快速教程中，我们将学习如何在Spring Boot中从YAML文件中注入一个Map。

    首先，我们先了解一下Spring框架中YAML文件的情况。然后，我们将通过一个实际的例子来演示如何将YAML属性绑定到Map上。

2. Spring框架中的YAML文件

    使用[YAML](https://yaml.org/)文件来存储外部配置数据是Spring开发者的一种常见做法。基本上，[Spring支持YAML](https://www.baeldung.com/spring-yaml-vs-properties)文件作为属性的替代品，并在引擎中使用[SnakeYAML](https://bitbucket.org/asomov/snakeyaml/src)来解析它们。

    不多说了，让我们看看一个典型的YAML文件是什么样子：

    ```yml
    server:
    port: 8090
    application:
        name: myapplication
        url: http://myapplication.com
    ```

    正如我们所看到的，YAML文件是不言自明的，而且更容易被人阅读。事实上，YAML提供了一种花哨而简洁(fancy and concise)的方式来存储分层的配置数据。

    默认情况下，Spring Boot在应用启动时从application.properties或application.yml读取配置属性。但是，我们可以使用[@PropertySource来加载一个自定义的YAML文件](https://www.baeldung.com/spring-yaml-propertysource)。

    现在我们已经熟悉了什么是YAML文件，让我们看看如何在Spring Boot中把YAML属性作为一个Map注入。

3. 如何从YAML文件中注入一个Map

    Spring Boot通过提供一个名为@ConfigurationProperties的方便注解，将数据外部化提升到了一个新的水平。引入这个注解是为了轻松地将配置文件中的外部属性直接注入到Java对象中。

    在本节中，我们将重点讨论如何使用@ConfigurationProperties注解将YAML属性绑定到bean类中。

    首先，我们将在application.yml中定义一些键-值属性：

    ```yml
    server:
    application:
        name: InjectMapFromYAML
        url: http://injectmapfromyaml.dev
        description: How To Inject a map from a YAML File in Spring Boot
    config:
        ips:
        - 10.10.10.10
        - 10.10.10.11
        - 10.10.10.12
        - 10.10.10.13
        filesystem:
        - /dev/root
        - /dev/md2
        - /dev/md4
    users: 
        root:
        username: root
        password: rootpass
        guest:
        username: guest
        password: guestpass
    ```

    在这个例子中，我们将尝试把应用映射成一个简单的`Map<String, String>`。同样地，我们将把配置细节注入到`Map<String, List<String>`中，把用户注入到带有String键的Map中，把属于用户定义的类（Credential）的对象作为值。

    然后我们将创建一个Bean类，ServerProperties，来封装将配置属性绑定到Maps的逻辑：

    ```java
    @Component
    @ConfigurationProperties(prefix = "server")
    public class ServerProperties {
        private Map<String, String> application;
        private Map<String, List<String>> config;
        private Map<String, Credential> users;
        // getters and setters
        public static class Credential {
            private String username;
            private String password;            
            // getters and setters
        }
    }
    ```

    正如我们所见，我们用@ConfigurationProperties装饰了ServerProperties类。这样，我们告诉Spring将所有带有指定前缀的属性映射到ServerProperties的一个对象上。

    回顾一下，我们的[应用程序也需要启用配置属性](https://www.baeldung.com/spring-enable-config-properties)，尽管这在大多数Spring Boot应用程序中都是[自动完成](https://www.baeldung.com/spring-enable-config-properties#purpose)的。

    最后，我们将测试我们的YAML属性是否作为Maps被正确注入：

    test/properties.yamlmap/MapFromYamlIntegrationTest.java

4. @ConfigurationProperties vs @Value

    现在让我们对@ConfigurationProperties和@Value做一个快速比较。

    尽管这两个注解都可以用来从配置文件中注入属性，但它们是完全不同的。这两个注解的主要区别在于，每一个注解都有不同的目的。

    简而言之，@Value允许我们通过其键直接注入一个特定的属性值。然而，@ConfigurationProperties注解将多个属性绑定到一个特定的对象，并通过映射的对象提供对属性的访问。

    一般来说，Spring建议在注入配置数据时使用@ConfigurationProperties而不是@Value。@ConfigurationProperties提供了一种很好的方式来集中和分组配置属性于一个结构化的对象中，我们可以在以后注入其他Bean中。

5. 总结

    在这篇简短的文章中，我们讨论了如何在Spring Boot中从YAML文件中注入一个Map。然后我们强调了@ConfigurationProperties和@Value之间的区别。
