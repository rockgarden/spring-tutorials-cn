# [Spring的模板引擎](https://www.baeldung.com/spring-template-engines)

1. 一览表

    Spring Web框架是围绕MVC（模型-视图-控制器）模式构建的，这使得在应用程序中更容易分离问题。这允许使用不同的视图技术，从成熟的JSP技术到各种模板引擎。

    在本文中，我们将看看可以与Spring一起使用的主要模板引擎、它们的配置和使用示例。

2. Spring视图技术

    鉴于Spring MVC应用程序中的担忧是干净分离的，从一个视图技术切换到另一个视图技术主要是一个配置问题。

    要渲染每个视图类型，我们需要定义一个与每种技术对应的ViewResolver bean。这意味着我们可以以通常返回JSP文件的方式从@Controller映射方法返回视图名称。

    在接下来的章节中，我们将介紹更传统的技术，如Java服务器页面，以及可以与Spring一起使用的主要模板引擎：Thymeleaf、Groovy、FreeMarker、Jade。

    对于每一种，我们将在标准Spring应用程序和使用Spring Boot构建的应用程序中进行必要的配置。

3. Java服务器页面

    JSP是Java应用程序中最受欢迎的视图技术之一，它得到了Spring out-of-the-box的支持。对于渲染JSP文件，常用的ViewResolver bean类型是InternalResourceViewResolver：

    ```java
    @EnableWebMvc
    @Configuration
    public class ApplicationConfiguration implements WebMvcConfigurer {
        @Bean
        public ViewResolver jspViewResolver() {
            InternalResourceViewResolver bean = new InternalResourceViewResolver();
            bean.setPrefix("/WEB-INF/views/");
            bean.setSuffix(".jsp");
            return bean;
        }
    }
    ```

    接下来，我们可以开始在/WEB-INF/views位置创建JSP文件：

    ```jsp
    <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
    <html>
        <head>
            <meta http-equiv="Content-Type" 
            content="text/html; charset=ISO-8859-1">
            <title>User Registration</title>
        </head>
        <body>
            <form:form method="POST" modelAttribute="user">
                <form:label path="email">Email: </form:label>
                <form:input path="email" type="text"/>
                <form:label path="password">Password: </form:label>
                <form:input path="password" type="password" />
                <input type="submit" value="Submit" />
            </form:form>
        </body>
    </html>
    ```

    如果我们将文件添加到Spring Boot应用程序中，那么我们可以在application.properties文件中定义以下属性，而不是在ApplicationConfiguration类中：

    - spring.mvc.view.prefix: /WEB-INF/views/
    - spring.mvc.view.suffix: .jsp

    基于这些属性，Spring Boot将自动配置必要的ViewResolver。

4. Thymeleaf

    Thymeleaf是一个Java模板引擎，可以处理HTML、XML、文本、JavaScript或CSS文件。与其他模板引擎不同，Thymeleaf允许将模板用作原型，这意味着它们可以被视为静态文件。

    1. Maven附属机构

        要将百里香叶与Spring集成，我们需要添加Thymeleaf和Thymeleaf-spring4依赖项：

        ```xml
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf</artifactId>
            <version>3.1.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf-spring5</artifactId>
            <version>3.1.2.RELEASE</version>
        </dependency>
        ```

        如果我们有一个Spring 4项目，那么我们需要添加thymeleaf-spring4。

    2. Spring配置

        接下来，我们需要添加需要SpringTemplateEngine bean的配置，以及指定视图文件位置和类型的TemplateResolver bean。

        SpringResourceTemplateResolver与Spring的资源解析机制集成：

        ```java
        @Configuration
        @EnableWebMvc
        public class ThymeleafConfiguration {

            @Bean
            public SpringTemplateEngine templateEngine() {
                SpringTemplateEngine templateEngine = new SpringTemplateEngine();
                templateEngine.setTemplateResolver(thymeleafTemplateResolver());
                return templateEngine;
            }

            @Bean
            public SpringResourceTemplateResolver thymeleafTemplateResolver() {
                SpringResourceTemplateResolver templateResolver 
                = new SpringResourceTemplateResolver();
                templateResolver.setPrefix("/WEB-INF/views/");
                templateResolver.setSuffix(".html");
                templateResolver.setTemplateMode("HTML5");
                return templateResolver;
            }
        }
        ```

        此外，我们需要一个ThymeleafViewResolver类型的ViewResolver豆：

        ```java
        @Bean
        public ThymeleafViewResolver thymeleafViewResolver() {
            ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
            viewResolver.setTemplateEngine(templateEngine());
            return viewResolver;
        }
        ```

    3. 百里香叶模板

        现在我们可以在WEB-INF/views位置添加HTML文件：

        ```jsp
        <html>
            <head>
                <meta charset="ISO-8859-1" />
                <title>User Registration</title>
            </head>
            <body>
                <form action="#" th:action="@{/register}"
                th:object="${user}" method="post">
                    Email:<input type="text" th:field="*{email}" />
                    Password:<input type="password" th:field="*{password}" />
                    <input type="submit" value="Submit" />
                </form>
            </body>
        </html>
        ```

        Thymeleaf模板在语法上与HTML模板非常相似。

        在Spring应用程序中使用Thymeleaf时，一些可用的功能是：

        - 支持定义表单行为
        - 绑定数据模型的绑定表单输入
        - 表单输入的验证
        - 显示来自消息源的值
        - 渲染模板片段

        您可以在我们的文章[《Thymeleaf in Spring MVC》](https://www.baeldung.com/thymeleaf-in-spring-mvc)中了解更多关于使用百里香模板的信息。

    4. Thymeleaf in Spring Boot

        Spring Boot将通过添加spring-boot-starter-thymeleaf依赖项为Thymeleaf提供自动配置：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
            <version>2.5.6</version>
        </dependency>
        ```

        不需要显式配置。默认情况下，HTML文件应放置在资源/模板位置。

5. FreeMarker

    [FreeMarker](http://freemarker.org/)是由Apache软件基金会构建的基于Java的模板引擎。它可用于生成网页，也可用于生成源代码、XML文件、配置文件、电子邮件和其他基于文本的格式。

    生成是根据使用FreeMarker模板语言编写的模板文件完成的。

    1. Maven附属机构

        要开始使用我们项目中的模板，我们需要freemarker依赖项：

        ```xml
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>2.3.23</version>
        </dependency>
        ```

        对于Spring集成，我们还需要spring-context-support依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>5.2.8.RELEASE</version>
        </dependency>
        ```

    2. Spring配置

        将FreeMarker与Spring MVC集成需要定义一个FreemarkerConfigurer bean，该bean指定模板文件的位置：

        ```java
        @Configuration
        @EnableWebMvc
        public class FreemarkerConfiguration {

            @Bean 
            public FreeMarkerConfigurer freemarkerConfig() { 
                FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer(); 
                freeMarkerConfigurer.setTemplateLoaderPath("/WEB-INF/views/");
                return freeMarkerConfigurer; 
            }
        }
        ```

        接下来，我们需要定义一个合适的FreeMarkerViewResolver类型的ViewResolver豆：

        ```java
        @Bean
        public FreeMarkerViewResolver freemarkerViewResolver() {
            FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
            resolver.setCache(true);
            resolver.setPrefix("");
            resolver.setSuffix(".ftl");
            return resolver;
        }
        ```

    3. FreeMarker模板

        我们可以在WEB-INF/views位置使用FreeMarker创建HTML模板：

        ```jsp
        <#import "/spring.ftl" as spring/>
        <html>
            <head>
                <meta charset="ISO-8859-1" />
                <title>User Registration</title>
            </head>
            <body>
                <form action="register" method="post">
                    <@spring.bind path="user" />
                    Email: <@spring.formInput "user.email"/>
                    Password: <@spring.formPasswordInput "user.password"/>
                    <input type="submit" value="Submit" />
                </form>
            </body>
        </html>
        ```

        在上述示例中，我们导入了Spring定义的一组宏，用于在FreeMarker中处理表单，包括将表单输入绑定到数据模型。

        此外，FreeMarker模板语言包含大量标签、指令和表达式，用于处理集合、流量控制结构、逻辑运算符、格式化和解析字符串、数字和许多其他功能。

    4. Spring Boot中的FreeMarker

        在Spring Boot应用程序中，我们可以使用spring-boot-starter-freemarker依赖项来简化所需的配置：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-freemarker</artifactId>
            <version>3.1.5</version>
        </dependency>
        ```

        此启动器添加了必要的自动配置。我们只需要开始将模板文件放在资源/模板文件夹中。

6. Groovy

    也可以使用[Groovy Markup Template Engine](http://groovy-lang.org/templating.html#_the_markuptemplateengine)生成Spring MVC视图。该引擎基于构建器语法，可用于生成任何文本格式。

    1. Maven附属机构

        需要将groovy-templates依赖项添加到我们的pom.xml中：

        ```xml
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-templates</artifactId>
            <version>2.4.12</version>
        </dependency>
        ```

    2. Spring配置

        标记模板引擎与Spring MVC的集成需要定义GroovyMarkupConfigurer bean和GroovyMarkupViewResolver类型的ViewResolver：

        ```java
        @Configuration
        @EnableWebMvc
        public class GroovyConfiguration {

            @Bean
            public GroovyMarkupConfigurer groovyMarkupConfigurer() {
                GroovyMarkupConfigurer configurer = new GroovyMarkupConfigurer();
                configurer.setResourceLoaderPath("/WEB-INF/views/");
                return configurer;
            }
            
            @Bean
            public GroovyMarkupViewResolver thymeleafViewResolver() {
                GroovyMarkupViewResolver viewResolver = new GroovyMarkupViewResolver();
                viewResolver.setSuffix(".tpl");
                return viewResolver;
            }
        }
        ```

    3. Groovy标记模板

        模板是用Groovy语言编写的，有几个特点：

        - 它们被编译成字节码
        - 它们包含对片段和布局的支持
        - 他们为国际化提供支持
        - 渲染速度很快

        让我们为我们的“用户注册”表单创建一个Groovy模板，其中包括数据绑定：

        ```groovy
        yieldUnescaped '<!DOCTYPE html>'
        html(lang:'en') {
            head {
                meta('http-equiv':'"Content-Type" ' +
                'content="text/html; charset=utf-8"')
                title('User Registration')
            }
            body {
                form (id:'userForm', action:'register', method:'post') {
                    label (for:'email', 'Email')
                    input (name:'email', type:'text', value:user.email?:'')
                    label (for:'password', 'Password')
                    input (name:'password', type:'password', value:user.password?:'')
                    div (class:'form-actions') {
                        input (type:'submit', value:'Submit')
                    }
                }
            }
        }
        ```

    4. Spring Boot中的Groovy模板引擎

        Spring Boot包含Groovy模板引擎的自动配置，通过包含spring-boot-starter-groovy-templates依赖项来添加：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-groovy-templates</artifactId>
            <version>2.5.6</version>
        </dependency>
        ```

        模板的默认位置是/resources/templates。

7. Jade4j

    [Jade4j](https://github.com/neuland/jade4j)是Javascript的Pug模板引擎（原名Jade）的Java实现。Jade4j模板可用于生成HTML文件。

    1. Maven附属机构

        对于Spring集成，我们需要spring-jade4j依赖项：

        ```xml
        <dependency>
            <groupId>de.neuland-bfi</groupId>
            <artifactId>spring-jade4j</artifactId>
            <version>1.2.5</version>
        </dependency>
        ```

    2. Spring配置

        要将Jade4j与Spring一起使用，我们必须定义一个配置模板位置的SpringTemplateLoader bean，以及一个JadeConfiguration bean：

        ```java
        @Configuration
        @EnableWebMvc
        public class JadeTemplateConfiguration {

            @Bean
            public SpringTemplateLoader templateLoader() {
                SpringTemplateLoader templateLoader 
                = new SpringTemplateLoader();
                templateLoader.setBasePath("/WEB-INF/views/");
                templateLoader.setSuffix(".jade");
                return templateLoader;
            }
        
            @Bean
            public JadeConfiguration jadeConfiguration() {
                JadeConfiguration configuration 
                = new JadeConfiguration();
                configuration.setCaching(false);
                configuration.setTemplateLoader(templateLoader());
                return configuration;
            }
        }
        ```

        接下来，我们需要通常的ViewResolver bean，在这种情况下是JadeViewResolver类型：

        ```java
        @Bean
        public ViewResolver viewResolver() {
            JadeViewResolver viewResolver = new JadeViewResolver();
            viewResolver.setConfiguration(jadeConfiguration());
            return viewResolver;
        }
        ```

    3. Jade4j模板

        Jade4j模板的特点是易于使用的空格敏感语法：

        ```jade
        doctype html
        html
        head
            title User Registration
        body
            form(action="register" method="post" )
            label(for="email") Email:
            input(type="text" name="email")
            label(for="password") Password:
            input(type="password" name="password")
            input(type="submit" value="Submit")
        ```

        该项目还提供了非常有用的[交互式文档](http://naltatis.github.io/jade-syntax-docs/)，您可以在编写模板时查看模板的输出。

        Spring Boot不提供Jade4j启动器，因此在Boot项目中，我们必须添加与上述定义相同的Spring配置。

8. 其他模板引擎

    除了迄今为止描述的模板引擎外，还有很多可用的引擎可以使用。

    让我们简要回顾一下其中的一些。

    Velocity是一个较旧的模板引擎，它非常复杂，但有一个缺点是，自4.3版本以来，Spring已经弃用它，并在Spring 5.0.1中完全删除。

    JMustache是一个模板引擎，可以通过使用spring-boot-starter-mustache依赖项轻松集成到Spring Boot应用程序中。

    Pebble在其库中包含对Spring和Spring Boot的支持。

    也可以使用其他模板库，如Handlebars或React，在JSR-223脚本引擎（如Nashorn）上运行。

9. 结论

    在本文中，我们介绍了Spring Web应用程序中一些最受欢迎的模板引擎。
