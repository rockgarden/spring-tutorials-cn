# [Spring Boot CLI 简介](https://www.baeldung.com/spring-boot-cli)

1. 简介

    Spring Boot CLI 是一种命令行抽象，可让我们轻松运行以 Groovy 脚本表达的 Spring 微服务。它还为这些服务提供了简化和增强的依赖关系管理。

    本文将快速介绍如何配置 Spring Boot CLI 和执行简单的终端命令来运行预配置的微服务。

    本文将使用 Spring Boot CLI 2.0.0.RELEASE。最新版本的 Spring Boot CLI 可在 Maven Central 找到。

2. 设置 Spring Boot CLI

    设置 Spring Boot CLI 的最简单方法之一是使用 SDKMAN。有关 SDKMAN 的设置和安装说明，请点击[此处](https://sdkman.io/install)。

    安装 SDKMAN 后，运行以下命令自动安装和配置 Spring Boot CLI：

    `$ sdk install springboot`

    要验证安装，请运行以下命令

    `$ spring --version`

    我们也可以从源代码编译安装 Spring Boot CLI，Mac 用户可以使用 [Homebrew](https://brew.sh/) 或 [MacPorts](https://www.macports.org/) 中的预编译包。有关所有安装选项，请参阅官方[文档](https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started-installing-spring-boot.html#getting-started-installing-the-cli)。

3. 常用终端命令

    Spring Boot CLI 开箱即提供了几个有用的命令和功能。其中最有用的功能之一是 Spring Shell，它可以用必要的 Spring 前缀封装命令。

    要启动嵌入式 shell，我们运行

    `Spring Shell`

    在这里，我们可以直接输入所需的命令，而无需预先输入 spring 关键字（因为我们现在是在 spring shell 中）。

    例如，输入

    `version`

    最重要的命令之一是告诉 Spring Boot CLI 运行一个 Groovy 脚本：

    `run [SCRIPT_NAME].groovy`

    Spring Boot CLI 会自动推断依赖关系，或者根据正确提供的注释进行推断。之后，它将启动嵌入式 Web 容器和应用程序。

    让我们进一步了解如何在 Spring Boot CLI 中使用 Groovy 脚本！

4. 基本 Groovy 脚本

    Groovy 和 Spring 与 Spring Boot CLI 相结合，可以在单文件 Groovy 部署中快速编写功能强大、性能卓越的微服务脚本。

    支持多脚本应用程序通常需要额外的构建工具，如 Maven 或 Gradle。

    下面我们将介绍 Spring Boot CLI 的一些最常见用例，更复杂的设置将在其他文章中介绍。

    有关所有 Spring 支持的 Groovy 注释列表，请查看官方[文档](https://docs.spring.io/spring-boot/docs/current/reference/html/cli-using-the-cli.html)。

    1. @Grab

        @Grab 注解和 Groovy 类似 Java 的导入子句可以轻松实现依赖管理和注入。

        事实上，大多数注解都会抽象、简化并自动包含必要的导入语句。这样，我们就可以花更多时间来考虑架构和我们要部署的服务的底层逻辑。

        让我们来看看如何使用 @Grab 注解：

        ```groovy
        package org.test

        @Grab("spring-boot-starter-actuator")

        @RestController
        class ExampleRestController{
        //...
        }
        ```

        正如我们所见，spring-boot-starter-actuator 已预先配置好，因此无需自定义应用程序或环境属性、XML 或其他程序配置，就能进行简洁的脚本部署。

        [这里](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-dependency-versions.html)有 @Grab 参数的完整列表，每个参数都指定了一个要下载和导入的库。

    2. @Controller、@RestController 和 @EnableWebMvc

        为了进一步加快部署，我们还可以利用 Spring Boot CLI 提供的 “grab hints” 来自动推断要导入的正确依赖关系。

        下面我们将介绍一些最常见的用例。

        例如，我们可以使用熟悉的 @Controller 和 @Service 注解来快速搭建标准 MVC 控制器和服务的脚手架：

        ```java
        @RestController
        class Example {

            @Autowired
            private MyService myService;

            @GetMapping("/")
            public String helloWorld() {
                return myService.sayWorld();
            }
        }

        @Service
        class MyService {
            public String sayWorld() {
                return "World!";
            }
        }
        ```

        Spring Boot CLI 支持 Spring Boot 的所有默认配置。因此，我们的 Groovy 应用程序将自动从其默认位置访问静态资源。

    3. @EnableWebSecurity

        要在应用程序中添加 Spring Boot 安全选项，我们可以使用 @EnableWebSecurity 注解，Spring Boot CLI 会自动下载该注解。

        下面，我们将使用 spring-boot-starter-security 依赖关系来抽象这一过程的一部分，它在引擎盖下利用了 @EnableWebSecurity 注解：

        ```groovy
        package bael.security
        @Grab("spring-boot-starter-security")
        @RestController
        class SampleController {
            @RequestMapping("/")
            public def example() {
                [message: "Hello World!"]
            }
        }
        ```

        有关如何保护资源和处理安全问题的更多详情，请查阅官方文档。

    4. @Test

        要设置一个简单的 JUnit 测试，我们可以添加 @Grab('junit') 或 @Test 注解：

        ```groovy
        package bael.test
        @Grab('junit')
        class Test {
            //...
        }
        ```

        这样我们就能轻松执行 JUnit 测试。

    5. 数据源和 JdbcTemplate

        无需明确使用 @Grab 注解，即可指定包括 DataSource 或 JdbcTemplate 在内的持久化数据选项：

        ```groovy
        package bael.data
        @Grab('h2')
        @Configuration
        @EnableWebMvc
        @ComponentScan('bael.data')
        class DataConfig {
            @Bean
            DataSource dataSource() {
                return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2).build();
            }
        }
        ```

        通过简单地使用熟悉的 Spring Bean 配置约定，我们获取了 H2 嵌入式数据库并将其设置为数据源。

5. 自定义配置

    使用 Spring Boot CLI 配置 Spring Boot 微服务有两种主要方法：

    - 我们可以在终端命令中添加参数
    - 我们可以使用自定义的 YAML 文件来提供应用程序配置

    Spring Boot 会自动在 /config 目录中搜索 application.yml 或 application.properties

    ```txt
    ├── app
        ├── app.groovy
        ├── config
            ├── application.yml
        ...
    ```

    我们还可以设置

    ```txt
    ├── app
        ├── example.groovy
        ├── example.yml
        ...
    ```

    有关应用程序属性的完整[列表](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)，请访问 Spring。

6. 结束语

    Spring Boot CLI 快速入门到此结束！更多详情，请查看官方[文档](https://docs.spring.io/spring-boot/docs/current/reference/html/cli-using-the-cli.html)。
