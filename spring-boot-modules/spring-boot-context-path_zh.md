# [Spring Boot更改上下文路径](https://www.baeldung.com/spring-boot-context-path)

1. 一览表

    默认情况下，Spring Boot在根上下文路径（“/”）上提供内容。

    虽然通常更喜欢惯例而不是配置是个好主意，但在某些情况下，我们确实希望有一个自定义路径。

    在本快速教程中，我们将介绍配置它的不同方法。

2. 设置属性

    就像许多其他配置选项一样，Spring Boot中的上下文路径可以通过设置属性server.servlet.context-path来更改。

    请注意，这适用于Spring Boot 2.x。对于Boot 1.x，属性是server.context-path。

    设置此属性的方法有多种，所以让我们逐一查看它们。

    1. 使用application.properties / yml

        更改上下文路径的最直接方法是在application.properties/yml文件中设置属性：

        `server.servlet.context-path=/baeldung`

        与其将属性文件放在src/main/resources中，我们也可以将其保存在当前工作目录中（类路径之外）。

    2. Java系统属性

        在上下文初始化之前，我们还可以将上下文路径设置为Java系统属性：

        ```java
        public static void main(String[] args) {
            System.setProperty("server.servlet.context-path", "/baeldung");
            SpringApplication.run(Application.class, args);
        }
        ```

    3. 操作系统环境变量

        Spring Boot也可以依赖于操作系统环境变量。在基于Unix的系统上，我们可以编写：

        `$ export SERVER_SERVLET_CONTEXT_PATH=/baeldung`

        在Windows上，设置环境变量的命令是：

        `> set SERVER_SERVLET_CONTEXT_PATH=/baeldung`

        上述环境变量适用于Spring Boot 2.x.x。如果我们有1.x.x，变量是SERVER_CONTEXT_PATH。

    4. 命令行参数

        我们也可以通过命令行参数动态设置属性：

        `$ java -jar app.jar --server.servlet.context-path=/baeldung`

3. 使用Java配置

    现在让我们通过用配置bean填充bean工厂来设置上下文路径。

    使用Spring Boot 2，我们可以使用WebServerFactoryCustomizer：

    ```java
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
    webServerFactoryCustomizer() {
        return factory -> factory.setContextPath("/baeldung");
    }
    ```

    使用Spring Boot 1，我们可以创建一个EmbeddedServletContainerCustomizer的实例：

    ```java
    @Bean
    public EmbeddedServletContainerCustomizer
    embeddedServletContainerCustomizer() {
        return container -> container.setContextPath("/baeldung");
    }
    ```

4. 配置的优先顺序

    有了这么多选项，我们最终可能会为同一属性提供多个配置。

    以下是降序的[优先级顺序](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)，Spring Boot用于选择有效配置：

    - Java配置
    - 命令行参数
    - Java系统属性
    - 操作系统环境变量
    - 当前目录中的应用程序.属性
    - 类路径中的application.properties（src/main/resources或打包的jar文件）

5. 结论

    在这篇简短的文章中，我们介绍了在Spring Boot应用程序中设置上下文路径或任何其他配置属性的不同方法。
