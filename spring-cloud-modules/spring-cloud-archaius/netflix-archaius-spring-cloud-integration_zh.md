# [使用 Spring Cloud 的 Netflix Archaius 简介](https://www.baeldung.com/netflix-archaius-spring-cloud-integration)

1. 概述

    Netflix [Archaius](https://github.com/Netflix/archaius) 是一个功能强大的配置管理库。

    简单地说，它是一个框架，可用于从许多不同来源收集配置属性，并提供快速、线程安全的访问。

    除此之外，该库还允许在运行时动态更改属性，使系统无需重启应用程序就能获得这些变化。

    在本入门教程中，我们将设置一个简单的 Spring Cloud Archaius 配置，解释在引擎盖下发生了什么，最后，我们将了解 Spring 如何扩展基本设置。

2. Netflix Archaius 功能

    我们知道，Spring Boot 已经提供了[管理外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)的工具，那么为什么还要费心建立一个不同的机制呢？

    那么，Archaius 提供了一些其他配置框架没有考虑到的便捷而有趣的功能。其中一些要点如下

    - 动态和类型属性
    - 在属性突变时调用的回调机制
    - 随时可用的动态配置源实现，如 URL、JDBC 和 Amazon DynamoDB
    - 可由 Spring Boot Actuator 或 JConsole 访问的 JMX MBean，以检查和操作属性
    - 动态属性验证

    这些优势在很多情况下都很有用。

    因此，Spring Cloud 开发了一个库，可以轻松配置 “Spring Environment Bridge”，这样 Archaius 就可以从 Spring 环境读取属性。

3. 依赖关系

    让我们将 spring-cloud-starter-netflix-archaius 添加到应用程序中，它将为我们的项目添加所有必要的依赖项。

    我们还可以选择将 spring-cloud-netflix 添加到依赖关系管理（dependencyManagement）部分，并依赖于它对工件版本的说明：

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-archaius</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-netflix</artifactId>
                <version>2.0.1.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

    注意：我们可以检查 Maven Central 以验证我们使用的是最新版本的启动库。

4. 使用方法

    添加所需的依赖后，我们就可以访问框架管理的属性了：

    ```java
    DynamicStringProperty dynamicProperty 
    = DynamicPropertyFactory.getInstance()
    .getStringProperty("baeldung.archaius.property", "default value");
    String propertyCurrentValue = dynamicProperty.get();
    ```

    让我们通过一个简短的示例来了解开箱即用的功能。

    1. 快速示例

        默认情况下，它动态管理应用程序类路径中名为 config.properties 的文件中定义的所有属性。

        因此，让我们将其添加到资源文件夹中，并添加一些任意属性：

        ```properties
        # config.properties
        baeldung.archaius.properties.one=one FROM:config.properties
        ```

        现在，我们需要一种在任何特定时刻检查属性值的方法。在这种情况下，我们将创建一个 RestController，以 JSON 响应的形式检索属性值：

        ```java
        @RestController
        public class ConfigPropertiesController {

        private DynamicStringProperty propertyOneWithDynamic
            = DynamicPropertyFactory.getInstance()
            .getStringProperty("baeldung.archaius.properties.one", "not found!");

            @GetMapping("/property-from-dynamic-management")
            public String getPropertyValue() {
                return propertyOneWithDynamic.getName() + ": " + propertyOneWithDynamic.get();
            }
        }
        ```

        让我们试试看。我们可以向该端点发送请求，服务将按照预期检索存储在 config.properties 中的值。

        到目前为止还没什么问题，对吧？好了，让我们在不重启服务的情况下，继续更改 classpath 文件中的属性值。结果，一分钟左右后，调用端点就能获取新值。很酷吧？

        接下来，我们将尝试了解引擎盖下发生了什么。

5. 它是如何工作的？

    首先，让我们来了解一下全貌。

    Archaius 是 [Apache Commons Configuration](http://commons.apache.org/proper/commons-configuration/) 库的扩展，增加了一些不错的功能，如动态源轮询框架、高吞吐量和线程安全实现。

    随后，spring-cloud-netflix-archaius 库开始发挥作用，它合并了所有不同的属性源，并利用这些源自动配置 Archaius 工具。

    1. Netflix Archaius 库

        Netflix Archaius 库定义了复合配置，即从不同来源获取的各种配置的集合。

        此外，其中一些配置源可能支持在运行时轮询更改。Archaius 提供了接口和一些预定义的实现来配置这些类型的源。

        配置源集合是分层的，因此如果一个属性存在于多个配置中，最终值将是最顶层的配置。

        最后，配置管理器（ConfigurationManager）会处理全系统的配置和部署上下文。它可以安装最终的复合配置，也可以检索已安装的配置进行修改。

    2. Spring 云支持

        Spring Cloud Archaius 库的主要任务是将所有不同的配置源合并为并发复合配置（ConcurrentCompositeConfiguration），并使用配置管理器（ConfigurationManager）进行安装。

        该库定义配置源的优先顺序是

        1. 上下文中定义的任何 Apache 通用配置 AbstractConfiguration Bean
        2. Autowired Spring ConfigurableEnvironment 中定义的所有源代码
        3. 默认的 Archaius 源，我们在上面的示例中看到了这些源
        4. Apache 的 SystemConfiguration 和 EnvironmentConfiguration 源

        Spring Cloud 库提供的另一个有用功能是定义了一个 Actuator Endpoint，用于监控属性并与之交互。其用法不在本教程的讨论范围之内。

6. 调整和扩展 Archaius 配置

    既然我们已经对 Archaius 的工作原理有了更好的了解，那么我们就可以分析如何根据我们的应用程序调整配置，或者如何使用我们的配置源扩展功能了。

    1. Archaius 支持的配置属性

        如果我们希望 Archaius 考虑与 config.properties 类似的其他配置文件，可以定义 archaius.configurationSource.additionalUrls 系统属性。

        该值会被解析为以逗号分隔的 URL 列表，因此，例如，我们可以在启动应用程序时添加该系统属性：

        ```properties
        -Darchaius.configurationSource.additionalUrls=
        "classpath:other-dir/extra.properties,
        file:///home/user/other-extra.properties"
        ```

        Archaius 将按照指定顺序首先读取 config.properties 文件，然后读取其他文件。因此，后面文件中定义的属性将优先于前面的属性。

        我们还可以使用其他一些系统属性来配置 Archaius 默认配置的各个方面：

        - archaius.configurationSource.defaultFileName：classpath 中的默认配置文件名
        - archaius.fixedDelayPollingScheduler.initialDelayMills：读取配置源之前的初始延迟
        - archaius.fixedDelayPollingScheduler.delayMills：两次读取配置源之间的延迟；默认值为 1 分钟

    2. 使用 Spring 添加其他配置源

        我们如何添加不同的配置源来由所述框架管理？如何管理优先级高于 Spring Environment 中定义的动态属性？

        回顾我们在第 4.2 节中提到的内容，我们可以认识到，Spring 定义的复合配置中的最高配置是上下文中定义的 AbstractConfiguration Bean。

        因此，我们只需使用 Archaius 提供的一些功能在 Spring Context 中添加 Apache 抽象类的实现，Spring 的自动配置就会自发地将其添加到托管配置属性中。

        为了简单起见，我们将以配置一个与默认 config.properties 类似的属性文件为例，但不同的是，该文件的优先级高于 Spring 环境和应用程序的其他属性：

        ```java
        @Bean
        public AbstractConfiguration addApplicationPropertiesSource() {
            URL configPropertyURL = (new ClassPathResource("other-config.properties")).getURL();
            PolledConfigurationSource source = new URLConfigurationSource(configPropertyURL);
            return new DynamicConfiguration(source, new FixedDelayPollingScheduler());
        }
        ```

        幸运的是，我们几乎不费吹灰之力就能设置多个配置源。它们的配置不在本入门教程的讨论范围之内。

7. 总结

    总之，我们已经了解了 Archaius 及其提供的一些很酷的配置管理功能。

    此外，我们还了解了 Spring Cloud 自动配置库如何发挥作用，让我们可以方便地使用该库的 API。
