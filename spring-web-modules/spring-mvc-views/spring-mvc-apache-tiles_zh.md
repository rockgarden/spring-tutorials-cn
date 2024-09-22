# [Apache Tiles与Spring MVC的集成](https://www.baeldung.com/spring-mvc-apache-tiles)

1. 一览表

    [Apache Tiles](https://tiles.apache.org/)是一个免费的开源模板框架，纯粹基于复合设计模式。

    复合设计模式是一种结构模式，它将对象组成树结构，以表示整个部分层次结构，这种模式统一地对待单个对象和对象的组成。换句话说，在Tiles中，页面是通过组装称为Tile的子视图组合来构建的。

    与其他框架相比，该框架的优势包括：

    - 可重复使用性
    - 易于配置
    - 低性能开销

    在本文中，我们将重点关注Apache Tiles与Spring MVC的集成。

2. 依赖配置

    第一步是在pom.xml中添加必要的依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.tiles</groupId>
        <artifactId>tiles-jsp</artifactId>
        <version>3.0.8</version>
    </dependency>
    ```

3. 瓷砖布局文件

    现在我们需要定义模板定义，特别是根据每一页，我们将覆盖该特定页面的模板定义：

    ```jsp
    <tiles-definitions>
        <definition name="template-def" 
            template="/WEB-INF/views/tiles/layouts/defaultLayout.jsp">  
            <put-attribute name="title" value="" />  
            <put-attribute name="header" 
            value="/WEB-INF/views/tiles/templates/defaultHeader.jsp" />  
            <put-attribute name="menu" 
            value="/WEB-INF/views/tiles/templates/defaultMenu.jsp" />  
            <put-attribute name="body" value="" />  
            <put-attribute name="footer" 
            value="/WEB-INF/views/tiles/templates/defaultFooter.jsp" />  
        </definition>  
        <definition name="home" extends="template-def">  
            <put-attribute name="title" value="Welcome" />  
            <put-attribute name="body" 
            value="/WEB-INF/views/pages/home.jsp" />  
        </definition>  
    </tiles-definitions>
    ```

4. 应用程序配置和其他类

    作为配置的一部分，我们将创建三个特定的java类，称为ApplicationInitializer、ApplicationController和ApplicationConfiguration：

    - ApplicationInitializer初始化并检查ApplicationConfiguration类中指定的必要配置
    - ApplicationConfiguration类包含将Spring MVC与Apache Tiles框架集成的配置
    - ApplicationController类与tiles.xml文件同步工作，并根据传入的请求重定向到必要的页面

    让我们看看每个classes在行动：

    ```java
    @Controller
    @RequestMapping("/")
    public class TilesController {
        @RequestMapping(
        value = { "/"}, 
        method = RequestMethod.GET)
        public String homePage(ModelMap model) {
            return "home";
        }
        @RequestMapping(
        value = { "/apachetiles"}, 
        method = RequestMethod.GET)
        public String productsPage(ModelMap model) {
            return "apachetiles";
        }
        @RequestMapping(
        value = { "/springmvc"},
        method = RequestMethod.GET)
        public String contactUsPage(ModelMap model) {
            return "springmvc";
        }
    }

    public class WebInitializer implements WebApplicationInitializer {
    public void onStartup(ServletContext container) throws ServletException {

            AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
            
            ctx.register(TilesApplicationConfiguration.class);

            container.addListener(new ContextLoaderListener(ctx));

            ServletRegistration.Dynamic servlet = container.addServlet(
            "dispatcher", new DispatcherServlet(ctx));
            servlet.setLoadOnStartup(1);
            servlet.addMapping("/");
        }
    }
    ```

    有两个重要的类在Spring MVC应用程序中配置瓷砖方面发挥着关键作用。它们是TilesConfigurer和TilesViewResolver：

    - TilesConfigurer通过提供瓷砖配置文件的路径来帮助将瓷砖框架与Spring框架链接
    - TilesViewResolver是Spring API提供的用于解析瓷砖视图的适配器类之一

    最后，在ApplicationConfiguration类中，我们使用TilesConfigurer和TilesViewResolver类来实现集成：

    ```java
    @Configuration
    @EnableWebMvc
    @ComponentScan(basePackages = "com.baeldung.spring.controller.tiles")
    public class TilesApplicationConfiguration implements WebMvcConfigurer {
        @Bean
        public TilesConfigurer tilesConfigurer() {
            TilesConfigurer tilesConfigurer = new TilesConfigurer();
            tilesConfigurer.setDefinitions(
            new String[] { "/WEB-INF/views/**/tiles.xml" });
            tilesConfigurer.setCheckRefresh(true);
            return tilesConfigurer;
        }
        
        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            TilesViewResolver viewResolver = new TilesViewResolver();
            registry.viewResolver(viewResolver);
        }
        
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/static/**")
            .addResourceLocations("/static/");
        }
    }
    ```

5. 瓷砖模板文件

    到目前为止，我们已经完成了Apache Tiles框架的配置，并定义了整个应用程序中使用的模板和特定瓷砖。

    在此步骤中，我们需要创建在tiles.xml中定义的特定模板文件。

    请找到可以用作构建特定页面基础的布局片段：

    ```jsp
    <html>
        <head>
            <meta
            http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
            <title><tiles:getAsString name="title" /></title>
            <link href="<c:url value='/static/css/app.css' />"
                rel="stylesheet">
            </link>
        </head>
        <body>
            <div class="flex-container">
                <tiles:insertAttribute name="header" />
                <tiles:insertAttribute name="menu" />
            <article class="article">
                <tiles:insertAttribute name="body" />
            </article>
            <tiles:insertAttribute name="footer" />
            </div>
        </body>
    </html>
    ```

6. 结论

    这就结束了Spring MVC与Apache Tiles的集成。
