# [配置一个Spring Boot Web应用程序](https://www.baeldung.com/spring-boot-application-configuration)

1. 概述

    Spring Boot可以做很多事情；在本教程中，我们将介绍Boot中几个比较有趣的配置选项。

    [用Spring Boot创建自定义启动器](https://www.baeldung.com/spring-boot-custom-starter)

    关于创建自定义Spring Boot启动器的快速实用指南。

    [在Spring Boot中进行测试](https://www.baeldung.com/spring-boot-testing)

    了解Spring Boot如何支持测试，以有效地编写单元测试。

2. 端口号

    在主要的独立应用程序中，主要的HTTP端口默认为8080；我们可以很容易地配置Boot以使用不同的端口：`server.port=8083`

    而对于，基于YAML的配置：

    ```yaml
    server:
        port: 8083
    ```

    我们也可以通过编程来定制服务器的端口：

    ```java
    @Component
    public class CustomizationBean implements
    WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        @Override
        public void customize(ConfigurableServletWebServerFactory container) {
            container.setPort(8083);
        }
    }
    ```

3. 上下文路径

    默认情况下，上下文路径是"/"。如果这并不理想，你需要改变它--变成像/app_name这样的东西，这里有一个快速而简单的方法，可以通过属性来实现它：`server.servlet.contextPath=/springbootapp`

    而对于基于YAML的配置：

    ```yaml
    server:
        servlet:
            contextPath:/springbootapp
    ```

    最后--改变也可以通过编程来完成：

    ```java
    @Component
    public class CustomizationBean
    implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        @Override
        public void customize(ConfigurableServletWebServerFactorycontainer) {
            container.setContextPath("/springbootapp");
        }
    }
    ```

4. 白标错误页面

    如果你没有在配置中指定任何自定义实现，Spring Boot会自动注册一个BasicErrorController Bean。

    不过，这个默认的控制器当然也可以配置：

    ```java
    public class MyCustomErrorController implements ErrorController {
        private static final String PATH = "/error";
        @GetMapping(value=PATH)
        public String error() {
            return "Error haven";
        }
    }
    ```

5. 自定义错误信息

    Boot默认提供/error映射，以合理的方式处理错误。

    如果你想配置更具体的错误页面，对统一的Java DSL有很好的支持，可以定制错误处理：

    ```java
    @Component
    public class CustomizationBean
    implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        @Override
        public void customize(ConfigurableServletWebServerFactorycontainer) {        
            container.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/400"));
            container.addErrorPages(new ErrorPage("/errorHaven"));
        }
    }
    ```

    在这里，我们特别处理了Bad Request，以匹配/400路径，而所有其他路径都匹配普通路径。

    还有一个非常简单的/errorHaven实现：common.error.controller/ErrorController.java

    ```java
    @GetMapping("/errorHaven")
    String errorHeaven() {
        return "You have reached the haven of errors!!!";
    }
    ```

6. 以编程方式关闭一个Boot应用程序

    你可以在SpringApplication的帮助下以编程方式关闭一个Boot应用程序。它有一个静态exit()方法，需要两个参数：ApplicationContext和ExitCodeGenerator：

    ```java
    @Autowired
    public void shutDown(ExecutorServiceExitCodeGenerator exitCodeGenerator) {
        SpringApplication.exit(applicationContext, exitCodeGenerator);
    }
    ```

    正是通过这个实用方法，我们可以关闭应用程序。

7. 配置日志级别

    你可以轻松地调整Boot应用程序中的日志级别；从1.2.0版本开始，你可以在主属性文件中配置日志级别：

    ```properties
    logging.level.org.springframework.web: DEBUG
    logging.level.org.hibernate: ERROR
    ```

    就像标准的Spring应用一样--你可以通过在classpath中添加自定义的XML或属性文件并在pom中定义库来激活不同的日志系统，如Logback、log4j、log4j2等等。

8. 注册一个新的Servlet

    如果你在嵌入式服务器的帮助下部署应用程序，你可以在Boot应用程序中注册新的Servlet，将它们作为常规配置中的Bean公开：main/SpringBootApplication.java

    ```java
    @Bean
    public HelloWorldServlet helloWorld() {
        return new HelloWorldServlet()；
    }
    ```

    或者，你也可以使用ServletRegistrationBean：main/SpringBootApplication.java

    ```java
    @Bean
    public SpringHelloServletRegistrationBean servletRegistrationBean() {
    
        SpringHelloServletRegistrationBean bean = new SpringHelloServletRegistrationBean(
        new SpringHelloWorldServlet(), "/springHelloWorld/*");
        bean.setLoadOnStartup(1);
        bean.addInitParameter("message", "SpringHelloWorldServlet special message");
        return bean;
    }
    ```

9. 在Boot应用程序中配置Jetty或Undertow

    Spring Boot启动器通常使用Tomcat作为默认的嵌入式服务器。如果需要改变这一点--你可以排除Tomcat的依赖关系，改用Jetty或Undertow：

    配置Jetty

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-tomcat</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jetty</artifactId>
    </dependency>
    ```

    ```java
    @Bean
    public JettyEmbeddedServletContainerFactory  jettyEmbeddedServletContainerFactory() {
        JettyEmbeddedServletContainerFactory jettyContainer = 
        new JettyEmbeddedServletContainerFactory();
        jettyContainer.setPort(9000);
        jettyContainer.setContextPath("/springbootapp");
        return jettyContainer;
    }
    ```

    配置Undertow

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-tomcat</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-undertow</artifactId>
    </dependency>
    ```

    ```java
    @Bean
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = 
        new UndertowEmbeddedServletContainerFactory();
        
        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
            @Override
            public void customize(io.undertow.Undertow.Builder builder) {
                builder.addHttpListener(8080, "0.0.0.0");
            }
        });
        return factory;
    }
    ```

10. 总结

    在这篇短文中，我们介绍了一些比较有趣和有用的Spring Boot配置选项。

    当然，在参考文档中还有很多很多的选项，可以根据自己的需要配置和调整Boot应用程序，这些只是我发现的一些比较有用的选项。
