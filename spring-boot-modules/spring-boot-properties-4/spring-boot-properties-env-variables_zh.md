# [在Spring Boot的属性文件中使用环境变量](https://www.baeldung.com/spring-boot-properties-env-variables)

1. 一览表

    在本教程中，我们将讨论如何在Spring Boot的application.properties和application.yml中使用环境变量。然后，我们将学习如何引用代码中的这些属性。

    进一步阅读：

    [带有Spring和Spring Boot的属性](https://www.baeldung.com/properties-with-spring)

    关于如何在Spring中处理属性文件和属性值的教程。

    [在Spring Boot中使用application.yml vs application.properties](https://www.baeldung.com/spring-boot-yaml-vs-properties)

    Spring Boot同时支持.properties和YAML。我们探索了注入属性之间的差异，以及如何提供多种配置。

    [Spring Boot中的环境变量前缀](https://www.baeldung.com/spring-boot-env-variable-prefixes)

    了解如何在Spring Boot中为环境变量使用前缀。

2. 在application.properties文件中使用环境变量

    让我们定义一个名为JAVA_HOME的[全局环境变量](https://www.baeldung.com/linux/environment-variables)，其值为“C:\Program Files\Java\jdk-11.0.14”。

    要在Spring Boot的application.properties中使用此变量，我们需要用大括号包围它：

    `java.home=${JAVA_HOME}`

    我们也可以以同样的方式使用系统属性。例如，在Windows上，默认定义了操作系统属性：

    `environment.name=${OS}`

    也可以组合几个变量值。让我们定义另一个环境变量HELLO_BAELDUNG，值为“Hello Baeldung”。我们现在可以连接我们的两个变量：

    `baeldung.presentation=${HELLO_BAELDUNG}. Java is installed in the folder: ${JAVA_HOME}`

    [属性](https://www.baeldung.com/properties-with-spring)baeldung.presentation现在包含以下文本：“Hello Baeldung. Java is installed in the folder: C:\Program Files\Java\jdk-11.0.14“。

    这样，我们的属性根据环境有不同的价值。

3. 在代码中使用我们的环境特定属性

    鉴于我们启动了[Spring上下文](https://www.baeldung.com/spring-web-contexts)，我们现在将看到如何将属性值注入代码中。

    1. 用@Value注入值

        首先，我们可以使用[@Value](https://www.baeldung.com/spring-value-annotation)注释。@Value处理 setter、[constructor](https://www.baeldung.com/constructor-injection-in-spring)和 field [injections](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)：

        ```java
        @Value("${baeldung.presentation}")
        private String baeldungPresentation;
        ```

    2. 从Spring的环境中获取它

        我们还可以通过Spring's Environment获得财产的价值。我们需要[autowire](https://www.baeldung.com/spring-autowire)：

        ```java
        @Autowired
        private Environment environment;
        ```

        多亏了getProperty()方法，现在可以检索属性值：

        `environment.getProperty("baeldung.presentation")`

    3. 使用@ConfigurationProperties的组属性

        如果我们想将属性分组在一起，[@ConfigurationProperties](https://www.baeldung.com/configuration-properties-in-spring-boot)注释非常有用。我们将定义一个[组件](https://www.baeldung.com/spring-component-annotation)，该组件将收集具有给定前缀的所有属性，在我们的案例中是baeldung。然后，我们可以为每个属性定义一个[设置器](https://www.baeldung.com/java-why-getters-setters)。设置器的名称是属性名称的其余部分。就我们而言，我们只有一个，叫做演示：

        ```java
        @Component
        @ConfigurationProperties(prefix = "baeldung")
        public class BaeldungProperties {

            private String presentation;

            public String getPresentation() {
                return presentation;
            }

            public void setPresentation(String presentation) {
                this.presentation = presentation;
            }
        }
        ```

        我们现在可以自动连接BaeldungProperties对象：

        ```java
        @Autowired
        private BaeldungProperties baeldungProperties;
        ```

        最后，要获取特定属性的值，我们需要使用相应的获取器：

        `baeldungProperties.getPresentation()`

4. 在application.yml文件中使用环境变量

    就像application.properties一样，application.yml是一个配置文件，它定义了应用程序的各种属性和设置。要使用环境变量，我们需要在属性占位符中声明其名称。

    让我们看看一个带有属性占位符和变量名称的application.yml文件示例：

    ```yml
    spring:
    datasource:
        url: ${DATABASE_URL}
    ```

    上面的示例显示，我们正试图在Spring Boot应用程序中导入数据库URL。${DATABASE_URL}表达式提示Spring Boot查找名为DATABASE_URL的环境变量。

    要在application.yml中定义环境变量，我们必须以美元符号开头，后跟开头的是开头的花括号、环境变量的名称和结尾的花括号。所有这些组合构成了属性占位符和环境变量名称。

    此外，我们可以在代码中使用特定环境的属性，就像我们使用application.properties一样。我们可以使用@Value注释注入值。此外，我们可以使用环境类。最后，我们可以使用@ConfigurationProperties注释。

5. 结论

    在本文中，我们学习了如何根据环境定义具有不同值的属性，并在代码中使用它们。此外，我们看到了如何在application.properties和application.yml文件中定义环境变量。最后，我们查看了将定义的属性注入示例代码的示例。
