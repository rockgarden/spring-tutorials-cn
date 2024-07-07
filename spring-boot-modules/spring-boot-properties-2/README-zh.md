# Spring Boot Properties

This module contains articles about Properties in Spring Boot.

## 用Spring从YAML文件中注入一个Map

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

    简而言之，[@Value](https://www.baeldung.com/spring-value-annotation)允许我们通过其键直接注入一个特定的属性值。然而，@ConfigurationProperties注解将多个属性绑定到一个特定的对象，并通过映射的对象提供对属性的访问。

    一般来说，Spring建议在注入配置数据时使用@ConfigurationProperties而不是@Value。@ConfigurationProperties提供了一种很好的方式来集中和分组配置属性于一个结构化的对象中，我们可以在以后注入其他Bean中。

5. 总结

    在这篇简短的文章中，我们讨论了如何在Spring Boot中从YAML文件中注入一个Map。然后我们强调了@ConfigurationProperties和@Value之间的区别。

## 在Spring Boot中将YAML转为对象列表

1. 概述

    在这个简短的教程中，我们将仔细研究如何在Spring Boot中把YAML列表映射成一个List。
    我们将从如何在YAML中定义列表的一些背景开始。
    然后，我们将深入了解如何将YAML列表绑定到对象的列表上。
2. 关于YAML中的列表的快速回顾

    简而言之，YAML是一种人类可读的数据序列化标准，它提供了一种简洁明了的方法来编写配置文件。YAML的好处是它支持多种数据类型，如Lists、Maps和标量类型。
    YAML 列表中的元素是用"-"字符定义的，而且它们都有相同的缩进程度：

    ```yaml
    yamlconfig:
    list:
        - item1
        - item2
        - item3
        - item4
    ```

    作为比较，基于属性的等价物使用索引：

    ```properties
    yamlconfig.list[0]=item1
    yamlconfig.list[1]=item2
    yamlconfig.list[2]=item3
    yamlconfig.list[3]=item4
    ```

    想了解更多的例子，可以看看我们关于如何使用[YAML和属性文件定义list和map](https://www.baeldung.com/spring-yaml-vs-properties#lists-and-maps)的文章。

    事实上，与属性文件相比，YAML的层次性显著增强了可读性。YAML的另一个有趣的特性是可以为[不同的Spring配置文件定义不同的属性](https://www.baeldung.com/spring-yaml#spring-yaml-file)。从引导版本2.4.0开始，属性文件也可以这样做。
    值得一提的是，Spring Boot为YAML配置提供了开箱即用的支持。根据设计，Spring Boot在启动时从application.yml加载配置属性，而不需要任何额外的工作。
3. 将YAML列表绑定到对象的简单列表

    Spring Boot提供了[@ConfigurationProperties](https://www.baeldung.com/configuration-properties-in-spring-boot)注释，以简化将外部配置数据映射到对象模型的逻辑。
    在本节中，我们将使用@ConfigurationProperties将YAML列表绑定到`list<Object>`中。
    我们首先在application.yml中定义一个简单的列表：

    ```yaml
    application:
    profiles:
        - dev
        - test
        - prod
        - 1
        - 2
    ```

    然后，我们将创建一个简单的ApplicationProps POJO来保存将我们的YAML列表绑定到对象列表的逻辑：

    ```java
    @Component
    @ConfigurationProperties(prefix = "application")
    public class ApplicationProps {
        private List<Object> profiles;
        // getter and setter
    }
    ```

    ApplicationProps类需要用@ConfigurationProperties进行修饰，以表达将具有指定前缀的所有YAML属性映射到ApplicationProps对象的意图。
    要绑定概要文件列表，我们只需要定义一个list类型的字段，其余部分由@ConfigurationProperties注释处理。

    请注意，我们使用@Component将ApplicationProps类注册为普通的Springbean。因此，我们可以像任何其他Springbean一样，将它注入到其他类中。
    最后，我们将ApplicationProps bean注入到一个测试类中，并验证我们的概要文件YAML列表是否正确地注入为list＜Object＞：

    ```java
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
    @EnableConfigurationProperties(value = ApplicationProps.class)
    class YamlSimpleListUnitTest {
        @Autowired
        private ApplicationProps applicationProps;
        @Test
        public void whenYamlList_thenLoadSimpleList() {
            assertThat(applicationProps.getProfiles().get(0)).isEqualTo("dev");
            assertThat(applicationProps.getProfiles().get(4).getClass()).isEqualTo(Integer.class);
            assertThat(applicationProps.getProfiles().size()).isEqualTo(5);
        }
    }
    ```

4. 将YAML列表绑定到复杂列表

    现在，让我们更深入地了解如何将嵌套的YAML列表注入到复杂的结构化列表中。
    首先，让我们将一些嵌套列表添加到application.yml中：

    ```yaml
    application:
    // ...
    props: 
        -
        name: YamlList
        url: http://yamllist.dev
        description: Mapping list in Yaml to list of objects in Spring Boot
        -
        ip: 10.10.10.10
        port: 8091
        -
        email: support@yamllist.dev
        contact: http://yamllist.dev/contact
    users:
        -
        username: admin
        password: admin@10@
        roles:
            - READ
            - WRITE
            - VIEW
            - DELETE
        -
        username: guest
        password: guest@01
        roles:
            - VIEW
    ```

    在本例中，我们将props属性绑定到`List<Map<String，Object>>`。类似地，我们将把用户映射到一个用户对象列表中。
    由于props条目的每个元素都持有不同的键，因此我们可以将其作为映射列表注入。请务必查看我们关于[如何在Spring Boot中从YAML文件注入映射](https://www.baeldung.com/spring-yaml-inject-map)的文章。
    然而，在用户的情况下，所有项目共享相同的密钥，因此为了简化其映射，我们可能需要创建一个专用的User类来将密钥封装为字段：

    ```java
    public class ApplicationProps {
        // ...
        private List<Map<String, Object>> props;
        private List<User> users;
        // getters and setters
        public static class User {
            private String username;
            private String password;
            private List<String> roles;
            // getters and setters
        }
    }
    ```

    现在我们验证嵌套的YAML列表是否正确映射：

    ```java
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
    @EnableConfigurationProperties(value = ApplicationProps.class)
    class YamlComplexListsUnitTest {
        @Autowired
        private ApplicationProps applicationProps;
        @Test
        public void whenYamlNestedLists_thenLoadComplexLists() {
            assertThat(applicationProps.getUsers().get(0).getPassword()).isEqualTo("admin@10@");
            assertThat(applicationProps.getProps().get(0).get("name")).isEqualTo("YamlList");
            assertThat(applicationProps.getProps().get(1).get("port").getClass()).isEqualTo(Integer.class);
        }
    }
    ```

5. 结论

    在本文中，我们学习了如何将YAML列表映射到Java列表中。
    我们还检查了如何将复杂列表绑定到自定义POJO。

## Relevant Articles

- [A Quick Guide to Spring @Value](https://www.baeldung.com/spring-value-annotation)
- [Using Spring @Value With Defaults](https://www.baeldung.com/spring-value-defaults)
- [How to Inject a Property Value Into a Class Not Managed by Spring?](https://www.baeldung.com/inject-properties-value-non-spring-class)
- [@PropertySource with YAML Files in Spring Boot](https://www.baeldung.com/spring-yaml-propertysource)
- [Inject Arrays and Lists From Spring Properties Files](https://www.baeldung.com/spring-inject-arrays-lists)
- [x] [Inject a Map from a YAML File with Spring](https://www.baeldung.com/spring-yaml-inject-map)
- [x] [YAML to List of Objects in Spring Boot](https://www.baeldung.com/spring-boot-yaml-list)
- More articles: [[<-- prev]](../spring-boot-properties)

## Code

像往常一样，本文的完整源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-properties-2)上找到。
