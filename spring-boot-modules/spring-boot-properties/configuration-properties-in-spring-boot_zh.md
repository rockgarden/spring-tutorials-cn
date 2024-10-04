# [Spring Boot中的@ConfigurationProperties指南](https://www.baeldung.com/configuration-properties-in-spring-boot)

1. 简介

    Spring Boot 有许多有用的功能，包括外部化配置和对属性文件中定义的属性的轻松访问。

    现在，我们将更详细地探讨@ConfigurationProperties注解。

2. 设置

    本教程使用一个相当标准的设置。我们首先在pom.xml中添加spring-boot-starter-parent作为父节点：

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.0.0</version>
        <relativePath/>
    </parent>
    ```

    为了能够验证文件中定义的属性，我们还需要一个JSR-380的实现，hibernate-validator是其中之一，由spring-boot-starter-validation依赖提供。

    让我们把它也添加到我们的pom.xml中：

    ```xml
    <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    ```

    "[Hibernate验证器入门](http://hibernate.org/validator/documentation/getting-started/)"页面有更多细节。

3. 简单的属性

    官方文档建议我们将配置属性隔离到独立的POJO中。

    所以我们先来做这个：

    ```java
    @Configuration
    @ConfigurationProperties(prefix = "mail")
    public class ConfigProperties { }
    ```

    我们使用@Configuration，这样Spring就会在应用上下文中创建一个Spring Bean。

    @ConfigurationProperties对所有具有相同前缀的分层属性效果最好；因此，我们添加一个前缀为mail。

    Spring框架使用标准的Java Bean设置器，所以我们必须为每个属性声明设置器。

    注意：如果我们不在POJO中使用@Configuration，那么我们需要在Spring应用主类中添加@EnableConfigurationProperties(ConfigProperties.class)来将属性绑定到POJO中：

    ```java
    @SpringBootApplication
    @EnableConfigurationProperties(ConfigProperties.class)
    public class EnableConfigurationDemoApplication {
        public static void main(String[] args) {
            SpringApplication.run(EnableConfigurationDemoApplication.class, args);
        }
    }
    ```

    这就是了! Spring会自动绑定我们的属性文件中定义的任何属性，这些属性的前缀是mail，并且与ConfigProperties类中的一个字段名称相同。

    Spring对绑定属性使用了一些宽松的规则。因此，下面的变化都被绑定到属性hostName上：

    ```txt
    mail.hostName
    mail.hostname
    mail.host_name
    mail.host-name
    mail.HOST_NAME
    ```

    因此，我们可以使用以下属性文件来设置所有字段：

    ```properties
    # Simple properties
    mail.hostname=host@mail.com
    mail.port=9000
    mail.from=mailer@mail.com
    ```

    1. Spring Boot 2.2

        从Spring Boot 2.2开始，Spring通过classpath扫描找到并注册@ConfigurationProperties类。对@ConfigurationProperties的扫描需要通过添加@ConfigurationPropertiesScan注解来明确选择。因此，我们不必用@Component（和其他元注解，如@Configuration）来注解这些类，甚至也不必使用@EnableConfigurationProperties：

        ```java
        @ConfigurationProperties(prefix = "mail") 
        @ConfigurationPropertiesScan 
        public class ConfigProperties { }
        ```

        由@SpringBootApplication启用的classpath扫描器找到了ConfigProperties类，尽管我们没有用@Component来注解这个类。

        此外，我们可以使用[@ConfigurationPropertiesScan](https://docs.spring.io/spring-boot/docs/2.2.0.RELEASE/api/org/springframework/boot/context/properties/ConfigurationPropertiesScan.html)注解来扫描自定义位置的配置属性类：

        ```java
        @SpringBootApplication
        @ConfigurationPropertiesScan("com.baeldung.configurationproperties")
        public class EnableConfigurationDemoApplication { 
            public static void main(String[] args) {   
                SpringApplication.run(EnableConfigurationDemoApplication.class, args); 
            } 
        }
        ```

        这样，Spring将只在com.baeldung.properties包中寻找配置属性类。

4. 嵌套属性

    我们可以在Lists、Maps和Classes中拥有嵌套属性。

    让我们创建一个新的Credentials类来用于一些嵌套属性：

    ```java
    public class Credentials {
        private String authMethod;
        private String username;
        private String password;
        // standard getters and setters
    }
    ```

    我们还需要更新ConfigProperties类，以使用一个List、一个Map和Credentials类：

    ```java
    public class ConfigProperties {
        private String hostname;
        private int port;
        private String from;
        private List<String> defaultRecipients;
        private Map<String, String> additionalHeaders;
        private Credentials credentials;
        // standard getters and setters
    }
    ```

    下面的属性文件将设置所有的字段：

    ```properties
    #Simple properties
    mail.hostname=mailer@mail.com
    mail.port=9000
    mail.from=mailer@mail.com

    #List properties
    mail.defaultRecipients[0]=admin@mail.com
    mail.defaultRecipients[1]=owner@mail.com

    #Map Properties
    mail.additionalHeaders.redelivery=true
    mail.additionalHeaders.secure=true

    #Object properties
    mail.credentials.username=john
    mail.credentials.password=password
    mail.credentials.authMethod=SHA1
    ```

5. 在@Bean方法上使用@ConfigurationProperties

    我们还可以在@Bean注释的方法上使用@ConfigurationProperties注释。

    当我们想把属性绑定到不受我们控制的第三方组件上时，这种方法可能特别有用。

    让我们创建一个简单的Item类，我们将在下一个例子中使用：

    ```java
    public class Item {
        private String name;
        private int size;
        // standard getters and setters
    }
    ```

    现在让我们看看如何在@Bean方法上使用@ConfigurationProperties来将外部化属性绑定到Item实例上：

    ```java
    @Configuration
    public class ConfigProperties {
        @Bean
        @ConfigurationProperties(prefix = "item")
        public Item item() {
            return new Item();
        }
    }
    ```

    因此，任何item-prefixed属性都将被映射到Spring上下文所管理的Item实例。

6. 属性验证

    @ConfigurationProperties使用JSR-380格式提供了属性的验证。这允许各种整洁的东西。

    例如，让我们把hostName属性变成强制性的：

    ```java
    @NotBlank
    private String hostName;
    ```

    接下来，让我们把authMethod属性的长度定为1到4个字符：

    ```java
    @Length(max = 4, min = 1)
    private String authMethod;
    ```

    然后将端口属性从1025到65536：

    ```java
    @Min(1025)
    @Max(65536)
    private int port;
    ```

    最后，from属性必须符合电子邮件地址格式：

    ```java
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$")
    private String from;
    ```

    这可以帮助我们减少代码中大量的if-else条件，并使代码看起来更干净、更简明。

    如果这些验证中的任何一个失败了，那么主程序就会以IllegalStateException的形式启动失败。

    Hibernate验证框架使用标准的Java bean getters和setters，所以我们为每个属性声明getters和setters是很重要的。

7. 属性转换

    @ConfigurationProperties支持多种类型的转换，将属性绑定到其相应的bean上。

    1. 持续时间

        我们先来看看将属性转换为Duration对象。

        这里我们有两个Duration类型的字段：

        ```java
        @ConfigurationProperties(prefix = "conversion")
        public class PropertyConversion {
            private Duration timeInDefaultUnit;
            private Duration timeInNano;
            ...
        }
        ```

        这就是我们的属性文件：

        ```properties
        conversion.timeInDefaultUnit=10
        conversion.timeInNano=9ns
        ```

        结果，字段timeInDefaultUnit的值是10毫秒，timeInNano的值是9纳秒。

        支持的单位有ns、us、ms、s、m、h和d，分别代表纳秒、微秒、毫秒、秒、分钟、小时和天。

        默认单位是毫秒，这意味着如果我们不在数值旁边指定单位，Spring将把数值转换为毫秒。

        我们也可以用@DurationUnit来覆盖默认单位：

        ```java
        @DurationUnit(ChronoUnit.DAYS)
        private Duration timeInDays;
        ```

        这是相应的属性：

        `conversion.timeInDays=2`

    2. 数据大小

        同样，Spring Boot @ConfigurationProperties支持DataSize类型的转换。

        让我们添加三个DataSize类型的字段：

        ```java
        private DataSize sizeInDefaultUnit;
        private DataSize sizeInGB;
        @DataSizeUnit(DataUnit.TERABYTES)
        private DataSize sizeInTB;
        ```

        这些是相应的属性：

        ```properties
        conversion.sizeInDefaultUnit=300
        conversion.sizeInGB=2GB
        conversion.sizeInTB=4
        ```

        在这种情况下，sizeInDefaultUnit值将是300字节，因为默认单位是字节。

        支持的单位是B、KB、MB、GB和TB。我们也可以使用@DataSizeUnit来覆盖默认单位。

    3. 自定义转换器

        我们也可以添加我们自己的自定义转换器来支持将一个属性转换为一个特定的类类型。

        让我们添加一个简单的雇员类：

        ```java
        public class Employee {
            private String name;
            private double salary;
        }
        ```

        然后我们将创建一个自定义的转换器来转换这个属性：

        `conversion.employee=john,2000`

        我们将把它转换为一个Employee类型的文件：

        `private Employee employee;`

        我们将需要实现转换器接口，然后使用@ConfigurationPropertiesBinding注解来注册我们的自定义转换器：

        ```java
        @Component
        @ConfigurationPropertiesBinding
        public class EmployeeConverter implements Converter<String, Employee> {
            @Override
            public Employee convert(String from) {
                String[] data = from.split(",");
                return new Employee(data[0], Double.parseDouble(data[1]));
            }
        }
        ```

8. 不可变的 @ConfigurationProperties 绑定

    从Spring Boot 2.2开始，我们可以使用@ConstructorBinding注解来绑定我们的配置属性。

    这实质上意味着@ConfigurationProperties注释的类现在可以是[不可变](https://www.baeldung.com/java-immutable-object)的。

    但从Spring Boot 3开始，这个注解就不需要了：

    ```java
    @ConfigurationProperties(prefix = "mail.credentials")
    public class ImmutableCredentials {
        private final String authMethod;
        private final String username;
        private final String password;
        public ImmutableCredentials(String authMethod, String username, String password) {
            this.authMethod = authMethod;
            this.username = username;
            this.password = password;
        }
        public String getAuthMethod() {
            return authMethod;
        }
        public String getUsername() {
            return username;
        }
        public String getPassword() {
            return password;
        }
    }
    ```

    正如我们所看到的，当使用@ConstructorBinding时，我们需要向构造函数提供我们想绑定的所有参数。

    请注意，ImmutableCredentials的所有字段都是最终的。另外，没有setter方法。

    此外，需要强调的是，为了使用构造函数绑定，我们需要用@EnableConfigurationProperties或@ConfigurationPropertiesScan明确启用我们的配置类。

9. Java 16的记录

    Java 16引入了记录类型作为[JEP 395](https://openjdk.java.net/jeps/395)的一部分。记录是作为不可变数据的透明载体的类。这使它们成为配置持有者和DTO的完美候选者。事实上，我们可以在Spring Boot中把Java记录定义为配置属性。例如，前面的例子可以改写为：

    ```java
    @ConstructorBinding
    @ConfigurationProperties(prefix = "mail.credentials")
    public record ImmutableCredentials(String authMethod, String username, String password) {
    }
    ```

    显然，与所有那些嘈杂的getters和setters相比，它更简洁。

    此外，从Spring Boot 2.6开始，对于单构造函数记录，我们可以放弃@ConstructorBinding注解。但是，如果我们的记录有多个构造函数，则仍应使用@ConstructorBinding来确定用于属性绑定的构造函数。

10. 总结

    在这篇文章中，我们探讨了@ConfigurationProperties注解，并强调了它提供的一些有用的功能，如放松的绑定和Bean验证。
