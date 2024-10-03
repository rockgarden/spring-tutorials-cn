# [Spring boot Mustache指南](https://www.baeldung.com/spring-boot-mustache)

1. 一览表

    在本文中，我们将重点介绍使用Mustache模板在Spring Boot应用程序中生成HTML内容。

    这是一个用于创建动态内容的无逻辑模板引擎，因其简单性而广受欢迎。

    如果您想发现基础知识，请查看我们对[Mustache](https://www.baeldung.com/mustache)文章的介绍。

2. Maven依赖性

    为了能够将Mustache与Spring Boot一起使用，我们需要将专用的Spring Boot启动器添加到pom.xml中：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mustache</artifactId>
    </dependency>
    <dependency> 
        <groupId>org.springframework.boot</groupId> 
        <artifactId>spring-boot-starter-web</artifactId> 
    </dependency>
    ```

    此外，我们需要spring-boot-starter-web依赖项。

3. 创建模板

    让我们展示一个例子，并使用Spring-Boot创建一个简单的MVC应用程序，该应用程序将在网页上提供文章。

    让我们为文章内容编写第一个模板：

    ```html
    <div class="starter-template">
        {{#articles}}
        <h1>{{title}}</h1>
        <h3>{{publishDate}}</h3>
        <h3>{{author}}</h3>
        <p>{{body}}</p>
        {{/articles}}
    </div>
    ```

    我们将保存此HTML文件，例如article.html，并在我们的index.html中引用它：

    ```html
    <div class="container">
        {{>layout/article}}
    </div>
    ```

    在这里，布局是一个子目录，文章是模板文件的文件名。

    请注意，默认的mustache模板文件扩展名现在是.mustache。我们可以用属性覆盖此配置：

    `spring.mustache.suffix:.html`

4. 控制者

    现在让我们编写服务文章的控制器：

    ```java
    @GetMapping("/article")
    public ModelAndView displayArticle(Map<String, Object> model) {

        List<Article> articles = IntStream.range(0, 10)
        .mapToObj(i -> generateArticle("Article Title " + i))
        .collect(Collectors.toList());

        model.put("articles", articles);

        return new ModelAndView("index", model);
    }
    ```

    控制器返回页面上要呈现的文章列表。在文章模板中，以#开头和/结尾的标签文章负责列表。

    这将迭代传递的模型，并分别渲染每个元素，就像在HTML表中一样：

    `{{#articles}}...{{/articles}}`

    generateArticle（）方法创建一个包含一些随机数据的文章实例。

    请注意，控制器返回的文章模型中的密钥应与文章模板标签的密钥相同。

    现在，让我们测试一下我们的应用程序：

    ```java
    @Test
    public void givenIndexPage_whenContainsArticle_thenTrue() {

        ResponseEntity<String> entity 
        = this.restTemplate.getForEntity("/article", String.class);
    
        assertEquals(entity.getStatusCode(), HttpStatus.OK));
        assertTrue(entity.getBody()
        .contains("Article Title 0"));
    }
    ```

    我们还可以通过以下方式部署应用程序来测试应用程序：

    `mvn spring-boot:run`

    一旦部署，我们可以点击localhost:8080/article，我们将列出我们的文章。

5. 处理默认值

    在Mustache环境中，如果我们不为占位符提供值，MustacheException将抛出一条消息“No method or field with name ”variable-name …”。

    为了避免此类错误，最好为所有占位符提供默认的全局值：

    ```java
    @Bean
    public Mustache.Compiler mustacheCompiler(
    Mustache.TemplateLoader templateLoader, 
    Environment environment) {

        MustacheEnvironmentCollector collector
        = new MustacheEnvironmentCollector();
        collector.setEnvironment(environment);

        return Mustache.compiler()
        .defaultValue("Some Default Value")
        .withLoader(templateLoader)
        .withCollector(collector);
    }
    ```

6. 带有弹簧MVC的Mustache

    现在，如果我们决定不使用Spring Boot，让我们讨论一下如何与Spring MVC集成。首先，让我们添加依赖项：

    ```xml
    <dependency>
        <groupId>com.github.sps.mustache</groupId>
        <artifactId>mustache-spring-view</artifactId>
        <version>1.4</version>
    </dependency>
    ```

    最新版本可以在这里[找到](https://mvnrepository.com/artifact/com.github.sps.mustache/mustache-spring-view)。

    接下来，我们需要配置MustacheViewResolver，而不是Spring的InternalResourceViewResolver：

    ```java
    @Bean
    public ViewResolver getViewResolver(ResourceLoader resourceLoader) {
        MustacheViewResolver mustacheViewResolver
            = new MustacheViewResolver();
        mustacheViewResolver.setPrefix("/WEB-INF/views/");
        mustacheViewResolver.setSuffix("..mustache");
        mustacheViewResolver.setCache(false);
        MustacheTemplateLoader mustacheTemplateLoader
            = new MustacheTemplateLoader();
        mustacheTemplateLoader.setResourceLoader(resourceLoader);
        mustacheViewResolver.setTemplateLoader(mustacheTemplateLoader);
        return mustacheViewResolver;
    }
    ```

    我们只需要配置后缀，我们的模板存储，前缀我们模板的扩展，以及负责加载模板的模板加载器。

7. 结论

    在这个快速教程中，我们研究了将Mustache模板与Spring Boot一起使用，在用户界面中渲染元素集合，并为变量提供默认值以避免错误。

    最后，我们讨论了如何使用MustacheViewResolver将其与Spring集成。
