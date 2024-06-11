# [Spring和Spring Boot之间的比较](https://www.baeldung.com/spring-vs-spring-boot)

1. 概述

    在本教程中，我们将探讨标准Spring框架和Spring Boot之间的差异。

    我们将重点讨论Spring的各个模块，如MVC和Security，在核心Spring中使用时与在Boot中使用时有何不同。

    进一步阅读：

    [配置Spring Boot Web应用程序](https://www.baeldung.com/spring-boot-application-configuration)

    一些对Spring Boot应用比较有用的配置。

    [从Spring迁移到Spring Boot](/MigratingFromSpringToSpringBoot.md)

    看看如何正确地从Spring迁移到Spring Boot。

2. 什么是Spring？

    简单地说，Spring框架为开发Java应用程序提供了全面的基础设施支持。

    它包含了一些很好的功能，如依赖注入，以及开箱即用的模块，如：

    - Spring JDBC
    - Spring MVC
    - Spring Security
    - Spring AOP
    - Spring ORM
    - Spring Test

    这些模块可以极大地减少应用程序的开发时间。

    例如，在Java Web开发的早期，我们需要写大量的模板代码来向数据源插入一条记录。通过使用Spring JDBC模块的JDBCTemplate，我们可以把它减少到几行代码，只需进行一些配置。

3. 什么是Spring Boot？

    Spring Boot基本上是Spring框架的一个扩展，它消除了设置Spring应用程序所需的模板配置。

    它对Spring平台采取了一种观点，为更快、更有效的开发生态系统铺平了道路。

    以下是Spring Boot的一些功能：

    - 有主见的(Opinionated) "启动器starter" 依赖，以简化构建和应用配置
    - 嵌入式服务器以避免应用部署的复杂性
    - 指标、健康检查和外部化配置
    - 自动配置Spring功能--只要有可能

    让我们一步一步地熟悉这两个框架。

4. Maven的依赖性

    首先，让我们看看使用Spring创建Web应用所需的最小依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
    </dependency>
    ```

    与Spring不同，Spring Boot只需要一个依赖项就能启动和运行Web应用程序：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ```

    所有其他的依赖项都会在构建时自动添加到最终的归档文件中。

    另一个好例子是测试库。我们通常使用Spring Test、JUnit、Hamcrest和Mockito库的集合。在Spring项目中，我们应该将所有这些库作为依赖项添加。

    另外，在Spring Boot中，我们只需要测试的启动依赖，就可以自动包含这些库。

    Spring Boot为不同的Spring模块提供了一系列的启动依赖项。一些最常用的是：

    - spring-boot-starter-data-jpa
    - spring-boot-starter-security
    - spring-boot-starter-test
    - spring-boot-starter-web
    - Spring-boot-starter-thymeleaf

    关于启动器的完整列表，也可以查看[Spring文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-starter)。

5. MVC配置

    让我们探讨一下使用Spring和Spring Boot创建JSP Web应用所需的配置。

    Spring需要定义dispatcher servlet、mappings和其他支持性配置。我们可以使用web.xml文件或Initializer类来完成这项工作：

    ```java
    public class MyWebAppInitializer implements WebApplicationInitializer {

        @Override
        public void onStartup(ServletContext container) {
            AnnotationConfigWebApplicationContext context
            = new AnnotationConfigWebApplicationContext();
            context.setConfigLocation("com.baeldung");
    
            container.addListener(new ContextLoaderListener(context));
    
            ServletRegistration.Dynamic dispatcher = container
            .addServlet("dispatcher", new DispatcherServlet(context));
            
            dispatcher.setLoadOnStartup(1);
            dispatcher.addMapping("/");
        }
    }
    ```

    我们还需要在@Configuration类中添加@EnableWebMvc注解，并定义一个视图解析器来解析从控制器返回的视图：

    ```java
    @EnableWebMvc
    @Configuration
    public class ClientWebConfig implements WebMvcConfigurer { 
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver bean
            = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/view/");
        bean.setSuffix(".jsp");
        return bean;
    }
    }
    ```

    相比之下，Spring Boot只需要几个属性就可以在我们添加网络启动器后使事情顺利进行：

    ```properties
    spring.mvc.view.prefix=/WEB-INF/jsp/
    spring.mvc.view.suffix=.jsp
    ```

    上述所有的Spring配置都是通过添加Boot web starter，通过一个叫做[自动配置](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-auto-configuration.html)的过程自动包含的。

    这意味着Spring Boot会查看应用程序中存在的依赖关系、属性和Bean，并根据这些来启用配置。

    当然，如果我们想添加自己的自定义配置，那么Spring Boot的自动配置就会退缩。

    1. 配置模板引擎

        现在我们来学习如何在Spring和Spring Boot中配置Thymeleaf模板引擎。

        在Spring中，我们需要添加[thymeleaf-spring5](https://mvnrepository.com/artifact/org.thymeleaf/thymeleaf-spring5)的依赖关系和一些视图解析器的配置：

        ```java
        @Configuration
        @EnableWebMvc
        public class MvcWebConfig implements WebMvcConfigurer {

            @Autowired
            private ApplicationContext applicationContext;

            @Bean
            public SpringResourceTemplateResolver templateResolver() {
                SpringResourceTemplateResolver templateResolver = 
                new SpringResourceTemplateResolver();
                templateResolver.setApplicationContext(applicationContext);
                templateResolver.setPrefix("/WEB-INF/views/");
                templateResolver.setSuffix(".html");
                return templateResolver;
            }

            @Bean
            public SpringTemplateEngine templateEngine() {
                SpringTemplateEngine templateEngine = new SpringTemplateEngine();
                templateEngine.setTemplateResolver(templateResolver());
                templateEngine.setEnableSpringELCompiler(true);
                return templateEngine;
            }

            @Override
            public void configureViewResolvers(ViewResolverRegistry registry) {
                ThymeleafViewResolver resolver = new ThymeleafViewResolver();
                resolver.setTemplateEngine(templateEngine());
                registry.viewResolver(resolver);
            }
        }
        ```

        Spring Boot 1只需要依赖spring-boot-starter-thymeleaf就可以在Web应用中启用Thymeleaf支持。由于Thymeleaf3.0的新功能，我们还必须在Spring Boot 2的Web应用中添加thymleaf-layout-dialect作为依赖。或者，我们可以选择添加一个spring-boot-starter-thymeleaf依赖项，它将为我们处理所有这些问题。

        一旦依赖关系到位，我们就可以将模板添加到src/main/resources/templates文件夹中，Spring Boot就会自动显示这些模板。

6. Spring安全配置

    为了简单起见，我们将看看如何使用这些框架启用默认的HTTP Basic认证。

    让我们先看看使用Spring启用安全性所需的依赖和配置。

    Spring需要标准的spring-security-web和spring-security-config两个依赖项来设置应用程序中的安全。

    接下来我们需要添加一个创建SecurityFilterChain Bean的类，并使用@EnableWebSecurity注解：

    ```java
    @Configuration
    @EnableWebSecurity
    public class CustomWebSecurityConfigurerAdapter {
    
        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
            .withUser("user1")
                .password(passwordEncoder()
                .encode("user1Pass"))
            .authorities("ROLE_USER");
        }
    
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
            return http.build();
        }
        
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
    ```

    这里我们使用inMemoryAuthentication来设置认证。

    Spring Boot也需要这些依赖关系才能工作，但我们只需要定义spring-boot-starter-security的依赖关系，因为这将自动把所有相关的依赖关系添加到classpath中。

    Spring Boot中的安全配置与上述配置相同。

    要了解如何在Spring和Spring Boot中实现JPA配置，我们可以查看我们的文章[《A Guide to JPA with Spring》](https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa)。

7. 应用引导

    在Spring和Spring Boot中引导应用程序的基本区别在于servlet。Spring使用web.xml或SpringServletContainerInitializer作为其引导入口点。

    另一方面，Spring Boot只使用Servlet 3的功能来引导一个应用程序。让我们来详细谈谈这个问题。

    1. Spring是如何引导的？

        Spring既支持传统的web.xml引导方式，也支持最新的Servlet 3+方式。

        我们来看看web.xml方法的步骤：

        - Servlet容器（服务器）读取web.xml。
        - web.xml中定义的DispatcherServlet被容器实例化。
        - DispatcherServlet通过读取`WEB-INF/{servletName}-servlet.xml`创建WebApplicationContext。
        - 最后，DispatcherServlet注册了应用上下文中定义的bean。

        下面是Spring如何使用Servlet 3+的方法进行引导的：

        - 容器搜索实现ServletContainerInitializer的类并执行。
        - SpringServletContainerInitializer会找到所有实现WebApplicationInitializer的类。
        - WebApplicationInitializer用XML或@Configuration类创建上下文。
        - WebApplicationInitializer用之前创建的上下文创建DispatcherServlet。

    2. Spring Boot是如何进行引导的？

        Spring Boot应用程序的入口点是用@SpringBootApplication注释的类：

        ```java
        @SpringBootApplication
        public class Application {
            public static void main(String[] args) {
                SpringApplication.run(Application.class, args);
            }
        }
        ```

        默认情况下，Spring Boot使用一个嵌入式容器来运行应用程序。在这种情况下，Spring Boot使用公共静态void main入口点来启动一个嵌入式Web服务器。

        它还负责将Servlet、Filter和ServletContextInitializer Bean从应用上下文绑定到嵌入式Servlet容器。

        Spring Boot的另一个特点是，它自动扫描同一包中的所有类或Main类的子包中的组件。

        此外，Spring Boot还提供了在外部容器中作为网络档案部署(deploying it as a web archive)的选项。在这种情况下，我们必须扩展SpringBootServletInitializer：

        ```java
        @SpringBootApplication
        public class Application extends SpringBootServletInitializer {}
        ```

        在这里，外部Servlet容器会寻找在Web存档的META-INF文件中定义的Main类，而SpringBootServletInitializer将负责绑定Servlet、Filter和ServletContextInitializer。

8. 打包和部署

    最后，让我们看看如何打包和部署一个应用程序。这两个框架都支持Maven和Gradle等常见的包管理技术；但是，在部署方面，这些框架有很大的不同。

    例如，[Spring Boot Maven插件](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)在Maven中提供Spring Boot支持。它还允许打包可执行的jar或war档案，并 "in-place" 运行一个应用程序。

    在部署方面，Spring Boot比Spring的一些优势包括：

    - 提供嵌入式容器支持
    - 提供使用java -jar命令独立运行jars的功能
    - 可以选择排除依赖关系，以避免在外部容器中部署时发生潜在的jar冲突
    - 部署时可选择指定活动配置文件
    - 为集成测试随机生成端口

9. 总结

    在这篇文章中，我们了解了Spring和Spring Boot的区别。

    用几句话来说，我们可以说Spring Boot只是Spring本身的一个扩展，使开发、测试和部署更加方便。
