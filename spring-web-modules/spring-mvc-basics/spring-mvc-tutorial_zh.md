# [Spring MVC Tutorial](https://www.baeldung.com/spring-mvc-tutorial)

1. 概述

    这是一个简单的Spring MVC教程，展示了如何设置Spring MVC项目，包括基于Java的配置和XML的配置。

    Spring MVC项目的Maven依赖项在Spring MVC依赖项一文中有详细描述。

2. 什么是Spring MVC？

    顾名思义，它是Spring框架中处理模型-视图-控制器或MVC模式的一个模块。它结合了MVC模式的所有优点和Spring的便利性。

    Spring[使用DispatcherServlet实现了MVC与前置控制器模式](https://www.baeldung.com/spring-controllers#Overview)。

    简而言之，DispatcherServlet充当主控制器，将请求路由到它们的目的地。模型只不过是我们应用程序的数据，而视图则由[各种模板引擎](https://www.baeldung.com/spring-template-engines)中的任何一个来表示。

3. 使用Java配置Spring MVC

    为了通过Java配置类启用Spring MVC支持，我们只需添加@EnableWebMvc注解。

    这将设置我们对MVC项目所需的基本支持，如注册控制器和映射、类型转换器、验证支持、消息转换器和异常处理。

    如果我们想定制这个配置，我们需要实现WebMvcConfigurer接口。

    ```java
    @EnableWebMvc
    @Configuration
    public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/view/");
        bean.setSuffix(".jsp");
        return bean;
    }
    ```

    在这个例子中，我们注册了一个ViewResolver bean，它将从/WEB-INF/view目录中返回.jsp视图。

    这里非常重要的是，我们可以注册视图控制器，使用ViewControllerRegistry在URL和视图名称之间创建一个直接的映射。这样一来，两者之间就不需要任何控制器了。

    如果我们想同时定义和扫描控制器类，我们可以在包含控制器的包中添加@ComponentScan注解。

    ```java
    @EnableWebMvc
    @Configuration
    @ComponentScan(basePackages = { "com.baeldung.web.controller" })
    public class WebConfig implements WebMvcConfigurer {
        // ...
    }
    ```

    为了启动一个加载该配置的应用程序，我们还需要一个初始化类。

    ```java
    public class MainWebAppInitializer implements WebApplicationInitializer {
        @Override
        public void onStartup(final ServletContext sc) throws ServletException {

            AnnotationConfigWebApplicationContext root = 
            new AnnotationConfigWebApplicationContext();

            root.scan("com.baeldung");
            sc.addListener(new ContextLoaderListener(root));

            ServletRegistration.Dynamic appServlet = 
            sc.addServlet("mvc", new DispatcherServlet(new GenericWebApplicationContext()));
            appServlet.setLoadOnStartup(1);
            appServlet.addMapping("/");
        }
    }
    ```

    > 注意，对于早于Spring 5的版本，我们必须使用 WebMvcConfigurerAdapter 类而不是接口。

4. 使用XML配置的Spring MVC

    取代上面的Java配置，我们也可以使用一个纯粹的XML配置。

    ```xml
    <context:component-scan base-package="com.baeldung.web.controller" />
    <mvc:annotation-driven />    

    <bean id="viewResolver" 
        class="org.springframework.web.servlet.view.InternalResourceViewResolver">
            <property name="prefix" value="/WEB-INF/view/" />
            <property name="suffix" value=".jsp" />
        </bean>

        <mvc:view-controller path="/" view-name="index" />

    </beans>
    ```

    如果我们想使用一个纯粹的XML配置，我们还需要添加一个web.xml文件来引导应用程序。关于这种方法的更多细节，请查看我们[之前的文章](https://www.baeldung.com/spring-xml-vs-java-config)。

5. 控制器和视图

    让我们来看一个基本控制器的示例：

    ```java
    @Controller
    public class SampleController {
        @GetMapping("/sample")
        public String showForm() {
            return "sample";
        }
    }
    ```

    相应的JSP资源就是simple.jsp文件，基于JSP的视图文件位于项目的 /WEB-INF/view 文件夹下，因此只能通过Spring基础架构访问，而不能通过直接URL访问。

6. 带引导的Spring MVC

    Spring Boot是对Spring平台的一个补充，它使启动和创建独立的生产级应用程序变得非常容易。Boot并不是为了取代Spring，而是为了更快更容易地使用它。

    1. Spring Boot Starters

        新框架提供了方便的启动依赖项，这些依赖项是依赖描述符，可以为特定功能引入所有必要的技术。

        这样做的好处是，我们不再需要为每个依赖项指定一个版本，而是允许启动器为我们管理依赖项。

        开始的最快方法是添加 [spring-boot-starter-parent](https://search.maven.org/classic/#search%7Cga%7C1%7Ca%3A%22spring-boot-starter-parent%22) pom.xml：

        ```xml
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.7.2</version>
        </parent>
        ```

        这将负责依赖关系管理。

    2. Spring Boot Entry Point

        使用Spring Boot构建的每个应用程序只需要定义主入口点。

        这通常是一个带有main方法的Java类，用@SpringBootApplication注释。

        此注释添加了以下其他注释：

        - @Configuration将类标记为bean定义的源。
        - @EnableAutoConfiguration告诉框架根据类路径上的依赖关系自动添加bean。
        - @ComponentScan扫描与Application类或更低级别相同包中的其他配置和bean。

        使用Spring Boot，我们可以使用Thymeleaf或JSP设置前端，而不需要使用第3节中定义的ViewResolver.xml，启用Thymeleaf，无需额外配置。

        Boot应用程序的[源代码](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-bootstrap)一如既往地在GitHub上提供。

        最后，如果您想开始使用Spring Boot，请查看我们的[参考简介](https://www.baeldung.com/spring-boot-start)。

7. 运行

    运行 Application.java，当项目在本地启动完成，可以通过<http://localhost:8080/spring-mvc-basics/sample>访问sample.jsp。

    > 注意：老代码，需要指定 JavaRuntime <maven.compiler.target>1.8</maven.compiler.target>
