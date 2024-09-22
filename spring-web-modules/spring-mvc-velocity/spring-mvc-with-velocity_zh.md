# [带有Velocity的Spring MVC快速指南](https://www.baeldung.com/spring-mvc-with-velocity)

1. 介绍

    [Velocity](http://velocity.apache.org/)是Apache软件基金会的模板引擎，可以处理普通文本文件、SQL、XML、Java代码和许多其他类型。

    在本文中，我们将重点关注将Velocity与典型的Spring MVC Web应用程序一起使用。

2. Maven附属机构

    让我们从启用Velocity支持开始——具有以下依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.velocity</groupId>
        <artifactId>velocity</artifactId>
        <version>1.7</version>
    </dependency>

    <dependency>
        <groupId>org.apache.velocity</groupId>
        <artifactId>velocity-tools</artifactId>
        <version>2.0</version>
    </dependency>
    ```

3. 配置

    1. 网络配置

        如果我们不想使用web.xml，让我们使用Java和初始化器配置我们的Web项目：

        ```java
        public class MainWebAppInitializer implements WebApplicationInitializer {

            @Override
            public void onStartup(ServletContext sc) throws ServletException {
                AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
                root.register(WebConfig.class);

                sc.addListener(new ContextLoaderListener(root));

                ServletRegistration.Dynamic appServlet = 
                sc.addServlet("mvc", new DispatcherServlet(new GenericWebApplicationContext()));
                appServlet.setLoadOnStartup(1);
            }
        }
        ```

        或者，我们当然可以使用传统的web.xml：

        ```jsp
        <web-app ...>
            <display-name>Spring MVC Velocity</display-name>
            <servlet>
                <servlet-name>mvc</servlet-name>
            <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
                <init-param>
                <param-name>contextConfigLocation</param-name>
                <param-value>/WEB-INF/mvc-servlet.xml</param-value>
            </init-param>
            <load-on-startup>1</load-on-startup>
            </servlet>

            <servlet-mapping>
                <servlet-name>mvc</servlet-name>
            <url-pattern>/*</url-pattern>
            </servlet-mapping>
        
            <context-param>
                <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/spring-context.xml</param-value>
            </context-param>

            <listener>
                <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
            </listener>
        </web-app>
        ```

        请注意，我们在“/*”路径上映射了我们的servlet。

    2. Velocity配置

        现在让我们来一下一个简单的Spring配置——再次，从Java开始：

        ```java
        @Configuration
        @EnableWebMvc
        @ComponentScan(basePackages= {
        "com.baeldung.mvc.velocity.controller",
        "com.baeldung.mvc.velocity.service" })
        public class WebConfig extends WebMvcConfigurerAdapter {

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                .addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
            }
        
            @Override
            public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
                configurer.enable();
            }

            @Bean
            public ViewResolver viewResolver() {
                VelocityLayoutViewResolver bean = new VelocityLayoutViewResolver();
                bean.setCache(true);
                bean.setPrefix("/WEB-INF/views/");
                bean.setLayoutUrl("/WEB-INF/layouts/layout.vm");
                bean.setSuffix(".vm");
                return bean;
            }
            
            @Bean
            public VelocityConfigurer velocityConfig() {
                VelocityConfigurer velocityConfigurer = new VelocityConfigurer();
                velocityConfigurer.setResourceLoaderPath("/");
                return velocityConfigurer;
            }
        }
        ```

        让我们快速看一下配置的XML版本：

        ```xml
        <beans ...>
            <context:component-scan base-package="com.baeldung.mvc.velocity.*" />
            <context:annotation-config />
            <bean id="velocityConfig"
            class="org.springframework.web.servlet.view.velocity.VelocityConfigurer">
                <property name="resourceLoaderPath">
                    <value>/</value>
                </property>
            </bean>
            <bean id="viewResolver"
            class="org.springframework.web.servlet.view.velocity.VelocityLayoutViewResolver">
                <property name="cache" value="true" />
                <property name="prefix" value="/WEB-INF/views/" />
                <property name="layoutUrl" value="/WEB-INF/layouts/layout.vm" />
                <property name="suffix" value=".vm" />
            </bean>
        </beans>
        ```

        在这里，我们告诉Spring在哪里寻找注释的bean定义：

        `<context:component-scan base-package="com.baeldung.mvc.velocity.*" />`

        我们表示，我们将在项目中使用以下行的注释驱动配置：

        `<context:annotation-config />`

        通过创建“velocityConfig”和“viewResolver”bean，我们告诉VelocityConfigurer在哪里查找模板，VelocityLayoutViewResolver在哪里查找视图和布局。

4. 速度模板

    最后，让我们创建我们的模板——从一个通用的标题开始：

    ![header.vm](./src/main/webapp/WEB-INF/fragments/header.vm)

    和页脚：

    ![footer.vm](./src/main/webapp/WEB-INF/fragments/footer.vm)

    让我们为我们的网站定义一个通用布局，我们将使用上述片段，并在以下代码中进行解析：

    ![layout.vm](./src/main/webapp/WEB-INF/layouts/layout.vm)

    您可以检查$screen_content变量是否包含页面的内容。

    最后，我们将为主要内容创建一个模板：

    ![list.vm](./src/main/webapp/WEB-INF/views/list.vm)

5. 控制器侧

    我们创建了一个简单的控制器，该控制器返回一个教程列表，作为我们布局的内容，以填充：

    ```java
    @Controller
    @RequestMapping("/")
    public class MainController {

        @Autowired
        private ITutorialsService tutService;

        @RequestMapping(value ="/", method = RequestMethod.GET)
        public String defaultPage() {
            return "index";
        }

        @RequestMapping(value ="/list", method = RequestMethod.GET)
        public String listTutorialsPage(Model model) { 
            List<Tutorial> list = tutService.listTutorials();
            model.addAttribute("tutorials", list);
            return "index";
        }
    }
    ```

    最后，我们可以在本地访问这个简单的示例——例如：localhost:8080/spring-mvc-velocity/

6. 结论

    在这个简单的教程中，我们使用Velocity模板引擎配置了Spring MVC Web应用程序。
