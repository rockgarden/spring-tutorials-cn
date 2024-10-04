# [Spring和Spring Boot的属性](https://www.baeldung.com/properties-with-spring)

1. 概述

    本教程将展示如何通过Java配置和@PropertySource在Spring中设置和使用属性。

    我们还将看到属性如何在Spring Boot中工作。

    [Spring表达式语言指南](https://www.baeldung.com/spring-expression-language)

    本文探讨了Spring表达式语言（SpEL），这是一种强大的表达式语言，支持在运行时查询和操作对象图。

    [配置一个Spring Boot Web应用](https://www.baeldung.com/spring-boot-application-configuration)

    一些对Spring Boot应用程序比较有用的配置。

    [Spring Boot中的@ConfigurationProperties指南](https://www.baeldung.com/configuration-properties-in-spring-boot)

    在Spring Boot中的@ConfigurationProperties注释的快速和实用指南。

2. 通过注解注册一个属性文件

    Spring 3.1还引入了新的@PropertySource注解，作为向环境添加属性源的便捷机制。

    我们可以将这个注解与@Configuration注解结合起来使用：

    properties.spring/PropertiesWithJavaConfig.java

    `@PropertySource("classpath:foo.properties")`

    另一种非常有用的注册新属性文件的方法是使用占位符，这允许我们在运行时动态地选择正确的文件：

    ```java
    @PropertySource({ 
    "classpath:persistence-${envTarget:mysql}.properties"
    })
    ```

    1. 定义多个属性位置

        根据Java 8的惯例，@PropertySource注解是可重复的。因此，如果我们使用Java 8或更高版本，我们可以使用这个注解来定义多个属性位置：PropertiesWithJavaConfig.java

        当然，我们也可以使用@PropertySources注解，指定一个@PropertySource的数组。这在任何支持的Java版本中都适用，而不仅仅是在Java 8或更高版本中：

        ```java
        @PropertySources({
            @PropertySource("classpath:foo.properties"),
            @PropertySource("classpath:bar.properties")
        })
        ```

        在这两种情况下，值得注意的是，如果出现了属性名称的碰撞，最后读取的源码会优先。

3. 使用/注入属性

    用 @Value 注解，注入一个属性是很直接的：

    ```java
    @Value( "${jdbc.url}" )
    private String jdbcUrl;
    ```

    我们还可以为该属性指定一个默认值：

    ```java
    @Value( "${jdbc.url:aDefaultUrl}" )
    private String jdbcUrl;
    ```

    Spring 3.1中新增的PropertySourcesPlaceholderConfigurer解决了bean定义属性值和@Value注解中的${...}占位符。

    最后，我们可以使用环境API获取属性的值：

    ```java
    @Autowired
    private Environment env;
    ...
    dataSource.setUrl(env.getProperty("jdbc.url"));
    ```

4. 使用Spring Boot的属性

    在我们进入属性的高级配置选项之前，让我们花些时间看看Spring Boot中新的属性支持。

    一般来说，与标准的Spring相比，这种新的支持涉及的配置较少，这当然是Boot的主要目标之一。

    1. application.properties：默认的属性文件

        Boot将其典型的约定俗成的配置方法应用于属性文件。这意味着我们可以简单地将application.properties文件放在src/main/resources目录下，它将被自动检测到。然后我们就可以像平常一样从里面注入任何加载的属性。

        因此，通过使用这个默认文件，我们不需要明确地注册一个PropertySource，甚至不需要提供一个属性文件的路径。

        如果需要的话，我们也可以在运行时使用环境属性来配置一个不同的文件：

        `java -jar app.jar --spring.config.location=classpath:/another-location.properties`

        从Spring Boot 2.3开始，我们还可以为配置文件指定通配符位置。

        例如，我们可以将spring.config.location属性设置为config/*/：

        `java -jar app.jar --spring.config.location=config/*/`

        这样，Spring Boot就会在我们的jar文件之外寻找与config/*/目录模式匹配的配置文件。当我们有多个配置属性的来源时，这就很方便了。

        从2.4.0版本开始，Spring Boot支持使用多文档属性文件，与[YAML](https://yaml.org/spec/1.2/spec.html#id2760395)的设计类似：

        ```properties
        baeldung.customProperty=defaultValue
        #---
        baeldung.customProperty=overriddenValue
        ```

        请注意，对于属性文件，三横线符号前有一个注释字符（#）。

    2. 特定环境的属性文件

        如果我们需要针对不同的环境，Boot里有一个内置的机制。

        我们可以简单地在 src/main/resources 目录中定义一个 application-environment.properties 文件，然后用相同的环境名称设置一个 Spring profile。

        例如，如果我们定义一个 "staging" 环境，这意味着我们必须定义一个staging profile，然后定义application-staging.properties。

        这个env文件将被加载，并将优先于默认的属性文件。请注意，默认文件仍然会被加载，只是当有属性碰撞时，环境特定的属性文件会优先。

    3. 测试专用的属性文件

        当我们的应用程序处于测试状态时，我们也可能有要求使用不同的属性值。

        Spring Boot通过在测试运行期间查看我们的src/test/resources目录来为我们处理这个问题。同样，默认的属性仍然可以正常注入，但如果发生冲突，则会被这些属性所覆盖。

    4. @TestPropertySource 注释

        如果我们需要对测试属性进行更精细的控制，那么我们可以使用 @TestPropertySource 注解。

        这允许我们为特定的测试环境设置测试属性，优先于默认的属性源：

        properties.testproperty/FilePropertyInjectionUnitTest.java

        如果我们不想使用一个文件，我们可以直接指定名称和值：

        ```java
        @RunWith(SpringRunner.class)
        @TestPropertySource(properties = {"foo=bar"})
        public class PropertyInjectionUnitTest {}
        ```

        我们还可以使用@SpringBootTest注解的属性参数来实现类似的效果：

        properties.testproperty/SpringBootPropertyInjectionIntegrationTest.java

        ```java
        @SpringBootTest(
        properties = {"foo=bar"}, classes = SpringBootPropertiesTestApplication.class)
        ```

    5. 分层的属性

        如果我们有分组的属性，我们可以利用@ConfigurationProperties注解，它将把这些属性分层映射成Java对象图。

        让我们来看看用于配置数据库连接的一些属性：

        ```properties
        database.url=jdbc:postgresql:/localhost:5432/instance
        database.username=foo
        database.password=bar
        ```

        然后让我们使用注解将它们映射到数据库对象中：configurationproperties/Database.java

        `@ConfigurationProperties(prefix = "database")`

        Spring Boot再次应用其约定俗成的配置方法，自动在属性名和其对应的字段之间进行映射。我们所需要提供的只是属性前缀。

        如果你想更深入地了解配置属性，请看我们的[深度文章](https://www.baeldung.com/configuration-properties-in-spring-boot)。

    6. 替代方案： YAML文件

        Spring也支持YAML文件。

        所有相同的命名规则适用于测试专用、环境专用和默认属性文件。唯一的区别是文件的扩展名和对我们classpath上的[SnakeYAML](https://bitbucket.org/asomov/snakeyaml)库的依赖。

        YAML 特别适合于分层的属性存储；下面这个属性文件：

        ```yaml
        database:
        url: jdbc:postgresql:/localhost:5432/instance
        username: foo
        password: bar
        secret: foo
        ```

        还值得一提的是，YAML文件不支持@PropertySource注解，所以如果我们需要使用这个注解，就会限制我们使用属性文件。

        另一点值得注意的是，在2.4.0版本中，Spring Boot改变了从多文档YAML文件加载属性的方式。以前，它们被添加的顺序是基于配置文件的激活顺序。然而，在新版本中，框架遵循了我们之前指出的.properties文件的相同排序规则；文件中较低位置声明的属性将简单地覆盖较高位置的属性(properties declared lower in the file will simply override those higher up)。

        此外，在这个版本中，配置文件不再能从特定的配置文件文件中激活，使结果更清晰、更可预测。

    7. 导入额外的配置文件

        在2.4.0版本之前，Spring Boot允许使用spring.config.location和spring.config.extra-location属性包括额外的配置文件，但它们有某些限制。例如，它们必须在启动应用程序之前定义（作为环境或系统属性，或使用命令行参数），因为它们是在流程的早期使用。

        在提到的版本中，我们可以在application.properties或application.yml文件中使用spring.config.import属性来轻松包含额外的文件。这个属性支持一些有趣的功能：

        - 添加多个文件或目录
        - 文件可以从classpath或外部目录中加载
        - 指示如果没有找到一个文件，启动过程是否应该失败，或者它是一个可选文件
        - 导入无扩展名的文件

        让我们看看一个有效的例子：

        ```properties
        spring.config.import=classpath:additional-application.properties,
        classpath:additional-application[.yml],
        optional:file:./external.properties,
        classpath:additional-application-properties/
        ```

        注意：在这里，我们使用换行符`/`来格式化这个属性，只是为了清晰明了。

        Spring会把导入当作一个新的文件，紧接着插入到导入声明下面。

    8. 来自命令行参数的属性

        除了使用文件，我们还可以直接在命令行中传递属性：

        `java -jar app.jar --property="value"`

        我们也可以通过系统属性来实现，这些属性是在-jar命令之前而不是之后提供的：

        `java -Dproperty.name="value" -jar app.jar`

    9. 来自环境变量的属性

        Spring Boot也会检测环境变量，把它们当作属性：

        ```bash
        export name=value
        java -jar app.jar
        ```

    10. 属性值的随机化

        如果我们不想要确定的属性值，我们可以使用[RandomValuePropertySource](https://docs.spring.io/spring-boot/docs/1.5.7.RELEASE/api/org/springframework/boot/context/config/RandomValuePropertySource.html)来随机化属性的值：

        ```properties
        random.number=${random.int}
        random.long=${random.long}
        random.uuid=${random.uuid}
        ```

    11. 其他类型的属性源

        Spring Boot支持大量的属性源，实现了深思熟虑的排序，以允许合理的覆盖。值得参考的是[官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)，它比本文的范围更深入。

5. 使用Raw Beans的配置--PropertySourcesPlaceholderConfigurer

    除了用方便的方法将属性引入Spring，我们还可以手动定义和注册属性配置Bean。

    使用PropertySourcesPlaceholderConfigurer可以让我们完全控制配置，但缺点是比较啰嗦，而且大多数时候是不必要的。

    让我们看看我们如何使用Java配置来定义这个Bean：

    ```java
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(){
        PropertySourcesPlaceholderConfigurer pspc
        = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = new ClassPathResource[ ]
        { new ClassPathResource( "foo.properties" ) };
        pspc.setLocations( resources );
        pspc.setIgnoreUnresolvablePlaceholders( true );
        return pspc;
    }
    ```

6. 父子背景下的属性

    这个问题一次又一次地出现了： 当我们的Web应用有一个父级和一个子级上下文时，会发生什么？父上下文可能有一些共同的核心功能和Bean，然后是一个（或多个）子上下文，可能包含特定于服务的Bean。

    在这种情况下，定义属性文件并将其包含在这些上下文中的最佳方式是什么？以及如何从Spring中最好地检索这些属性？

    我们将给出一个简单的分解。

    如果文件是在父上下文(Parent context)中定义的：

    - @Value 在子上下文中起作用： 是
    - @Value 在父级上下文中起作用： 是
    - environment.getProperty 在子环境中： 是
    - environment.getProperty 在父级上下文中： 是

    如果文件被定义在子上下文(Child context)中：

    - @Value 在子环境中起作用： 是
    - @Value 在父语境中工作： NO
    - environment.getProperty 在子环境中发挥作用： 是
    - environment.getProperty 在父级上下文中： NO

7. 总结

    本文展示了几个在Spring中使用属性和属性文件的例子。
