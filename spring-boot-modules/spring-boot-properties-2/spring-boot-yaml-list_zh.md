# [在Spring Boot中将YAML转为对象列表](https://www.baeldung.com/spring-boot-yaml-list)

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
