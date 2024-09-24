# [如何在Spring Boot中更改默认端口](https://www.baeldung.com/spring-boot-change-port)

1. 一览表

    Spring Boot为许多配置属性提供了合理的默认值。但有时我们需要根据特定案例的价值观来定制这些。

    一个常见的用例是更改嵌入式服务器的默认端口。

    在这个快速教程中，我们将介绍实现这一目标的几种方法。

2. 使用属性文件

    自定义Spring Boot最快、最简单的方法是覆盖默认属性的值。

    对于服务器端口，我们想要更改的属性是server.port。

    默认情况下，嵌入式服务器在端口8080上启动。

    因此，让我们看看如何在application.properties文件中提供不同的值：

    `server.port=8081`

    现在，服务器将在端口8081上启动。

    如果我们使用application.yml文件，我们也可以做同样的事情：

    ```yml
    server:
    port : 8081
    ```

    如果这两个文件放在Maven应用程序的src/main/resources目录中，Spring Boot会自动加载。

    1. 特定环境的端口

        如果我们在不同的环境中部署了一个应用程序，我们可能希望它在每个系统上的不同端口上运行。

        通过将属性文件方法与Spring配置文件相结合，我们可以轻松实现这一点。具体来说，我们可以为每个环境创建一个属性文件。

        例如，我们将有一个包含以下内容的application-dev.properties文件：

        `server.port=8081`

        然后，我们将添加另一个具有不同端口的application-qa.properties文件：

        `server.port=8082`

        现在，在大多数情况下，属性文件配置应该足够了。然而，这个目标还有其他选择，所以让我们也探索一下它们。

3. 程序化配置

    我们可以通过在启动应用程序时设置特定属性或自定义嵌入式服务器配置来以编程方式配置端口。

    首先，让我们看看如何在主@SpringBootApplication类中设置属性：

    ```java
    @SpringBootApplication
    public class CustomApplication {
        public static void main(String[] args) {
            SpringApplication app = new SpringApplication(CustomApplication.class);
            app.setDefaultProperties(Collections
            .singletonMap("server.port", "8083"));
            app.run(args);
        }
    }
    ```

    接下来，要自定义服务器配置，我们必须实现WebServerFactoryCustomizer接口：

    ```java
    @Component
    public class ServerPortCustomizer
    implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
        @Override
        public void customize(ConfigurableWebServerFactory factory) {
            factory.setPort(8086);
        }
    }
    ```

    请注意，这适用于Spring Boot 2.x版本。

    对于Spring Boot 1.x，我们可以同样实现EmbeddedServletContainerCustomizer接口。

4. 使用命令行参数

    当将应用程序打包和运行为jar时，我们可以使用java命令设置server.port参数：

    `java -jar spring-5.jar --server.port=8083`

    或者通过使用等效的语法：

    `java -jar -Dserver.port=8083 spring-5.jar`

5. 评估顺序

    最后，让我们来看看Spring Boot对这些方法进行评估的顺序。

    基本上，配置优先级是

    - 嵌入式服务器配置
    - 命令行参数
    - 属性文件
    - 主@SpringBootApplication配置

6. 结论

    在本文中，我们看到了如何在Spring Boot应用程序中配置服务器端口。
