# [Spring MVC + Thymeleaf 3.0：新功能](https://www.baeldung.com/spring-thymeleaf-3)

1. 介绍

    [Thymeleaf](http://www.thymeleaf.org/)是一个Java模板引擎，用于处理和创建HTML、XML、JavaScript、CSS和纯文本。对于百里香叶和春天的介绍，请看这篇[文章](https://www.baeldung.com/thymeleaf-in-spring-mvc)。

    在本文中，我们将讨论Thymeleaf 3.0在Spring MVC中与Thymeleaf应用程序的新功能。版本3具有新功能和许多引擎盖下的改进。更具体地说，我们将涵盖自然处理和Javascript内联的主题。

    Thymeleaf 3.0包括三种新的文本模板模式：TEXT、JAVASCRIPT和CSS——它们分别用于处理纯JavaScript和CSS模板。

2. Maven附属机构

    首先，让我们看看将Thymeleaf与Spring集成所需的配置；我们的依赖项需要thymeleaf-spring库：

    ```xml
    <dependency>
        <groupId>org.thymeleaf</groupId>
        <artifactId>thymeleaf</artifactId>
        <version>3.0.11.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.thymeleaf</groupId>
        <artifactId>thymeleaf-spring5</artifactId>
        <version>3.0.11.RELEASE</version>
    </dependency>
    ```

    请注意，对于Spring 4项目，必须使用thymeleaf-spring4库，而不是thymeleaf-spring5。

3. Java Thymeleaf配置

    首先，我们需要配置新的模板引擎、视图和模板解析器。为了做到这一点，我们需要更新Java配置类，创建。

    为了做到这一点，我们需要更新[此处](https://www.baeldung.com/thymeleaf-in-spring-mvc)创建的Java配置类。除了新型解析器外，我们的模板还正在实现Spring接口ApplicationContextAware：

    ```java
    @Configuration
    @EnableWebMvc
    @ComponentScan({ "com.baeldung.thymeleaf" })
    public class WebMVCConfig implements WebMvcConfigurer, ApplicationContextAware {

        private ApplicationContext applicationContext;

        // Java setter

        @Bean
        public ViewResolver htmlViewResolver() {
            ThymeleafViewResolver resolver = new ThymeleafViewResolver();
            resolver.setTemplateEngine(templateEngine(htmlTemplateResolver()));
            resolver.setContentType("text/html");
            resolver.setCharacterEncoding("UTF-8");
            resolver.setViewNames(ArrayUtil.array("*.html"));
            return resolver;
        }
        
        @Bean
        public ViewResolver javascriptViewResolver() {
            ThymeleafViewResolver resolver = new ThymeleafViewResolver();
            resolver.setTemplateEngine(templateEngine(javascriptTemplateResolver()));
            resolver.setContentType("application/javascript");
            resolver.setCharacterEncoding("UTF-8");
            resolver.setViewNames(ArrayUtil.array("*.js"));
            return resolver;
        }
        
        @Bean
        public ViewResolver plainViewResolver() {
            ThymeleafViewResolver resolver = new ThymeleafViewResolver();
            resolver.setTemplateEngine(templateEngine(plainTemplateResolver()));
            resolver.setContentType("text/plain");
            resolver.setCharacterEncoding("UTF-8");
            resolver.setViewNames(ArrayUtil.array("*.txt"));
            return resolver;
        }
    }
    ```

    正如我们上面所观察到的，我们创建了三个不同的视图解析器——一个用于HTML视图，一个用于Javascript文件，一个用于纯文本文件。Thymeleaf将通过检查文件扩展名来区分它们——分别是.html、.js和.txt。

    我们还创建了静态ArrayUtil类，以便使用方法array()创建带有视图名称的所需String[]数组。

    在本类的下一部分，我们需要配置模板引擎：

    ```java
    private ISpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }
    ```

    最后，我们需要创建三个独立的模板解析器：

    ```java
    private ITemplateResolver htmlTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.HTML);
        return resolver;
    }
        
    private ITemplateResolver javascriptTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/js/");
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.JAVASCRIPT);
        return resolver;
    }
        
    private ITemplateResolver plainTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/txt/");
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.TEXT);
        return resolver;
    }
    ```

    请注意，对于测试，最好使用非缓存模板，这就是为什么建议使用setCacheable(false)方法。

    Javascript模板将存储在/WEB-INF/js/文件夹中，纯文本文件存储在/WEB-INF/txt/文件夹中，最后HTML文件的路径是/WEB-INF/html。

4. Spring控制器配置

    为了测试我们的新配置，我们创建了以下Spring控制器：

    ```java
    @Controller
    public class InliningController {
        @RequestMapping(value = "/html", method = RequestMethod.GET)
        public String getExampleHTML(Model model) {
            model.addAttribute("title", "Baeldung");
            model.addAttribute("description", "Thymeleaf tutorial");
            return "inliningExample.html";
        }
        
        @RequestMapping(value = "/js", method = RequestMethod.GET)
        public String getExampleJS(Model model) {
            model.addAttribute("students", StudentUtils.buildStudents());
            return "studentCheck.js";
        }
        
        @RequestMapping(value = "/plain", method = RequestMethod.GET)
        public String getExamplePlain(Model model) {
            model.addAttribute("username", SecurityContextHolder.getContext()
            .getAuthentication().getName());
            model.addAttribute("students", StudentUtils.buildStudents());
            return "studentsList.txt";
        }
    }
    ```

    在HTML文件示例中，我们将展示如何使用新的内联功能，使用和不使用HTML标签转换。

    对于JS示例，我们将生成一个AJAX请求，该请求将加载包含学生信息的js文件。请注意，我们在本文的StudentUtils类中使用简单的buildStudents()方法。

    最后，在明文示例中，我们将以文本文件的形式显示学生信息。使用纯文本模板模式的典型示例可用于发送纯文本电子邮件。

    作为附加功能，我们将使用SecurityContextHolder来获取登录的用户名。

5. Html/Js/文本示例文件

    本教程的最后一部分是创建三种不同类型的文件，并测试新Thymeleaf功能的使用。让我们从HTML文件开始：

    ```jsp
    <!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Inlining example</title>
    </head>
    <body>
        <p>Title of tutorial: [[${title}]]</p>
        <p>Description: [(${description})]</p>
    </body>
    </html>
    ```

    在这个文件中，我们使用了两种不同的方法。为了显示标题，我们使用转义语法，这将删除所有HTML标签，导致只显示文本。在描述的情况下，我们使用未转义语法来保留HTML标签。最终结果如下：

    ```jsp
    <p>Title of tutorial: Baeldung</p>
    <p>Description: <strong>Thymeleaf</strong> tutorial</p>
    ```

    当然，我们的浏览器将通过以粗体样式显示Thymeleaf一词来解析。

    接下来，我们继续测试js模板功能：

    ```js
    var count = [[${students.size()}]];
    alert("Number of students in group: " + count);
    ```

    JAVASCRIPT模板模式中的属性将是JavaScript未转义的。它将导致创建js警报。我们使用jQuery AJAX在listStudents.html文件中加载此警报：

    ```js
    <script>
        $(document).ready(function() {
            $.ajax({
                url : "/spring-thymeleaf/js",
                });
            });
    </script>
    ```

    我们想要测试的最后一个功能，但不是最小的功能是纯文本文件生成。我们创建了包含以下内容的studentsList.txt文件：

    ```txt
    Dear [(${username})],

    This is the list of our students:
    [# th:each="s : ${students}"]
    - [(${s.name})]. ID: [(${s.id})]
    [/]
    Thanks,
    The Baeldung University
    ```

    请注意，与标记模板模式一样，标准方言仅包括一个可处理元素（[# ...]）和一组可处理属性（th:text、th:utext、th:if、th:unless、th:each等）。结果将是一个文本文件，例如，我们可能会在电子邮件中使用，正如第3节末尾提到的。

    如何测试？我们建议先玩浏览器，然后再检查现有的JUnit测试。

6. Spring Boot中的百里香叶

    Spring Boot通过添加spring-boot-starter-thymeleaf依赖项为Thymeleaf提供自动配置：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
        <version>2.3.3.RELEASE</version>
    </dependency>
    ```

    不需要显式配置。默认情况下，HTML文件应放置在资源/模板位置。

7. 结论

    在本文中，我们讨论了Thymeleaf框架中实现的新功能，重点是3.0版本。

    最后，如果您计划将项目从版本2迁移到最新版本，请查看此处的[迁移指南](http://www.thymeleaf.org/doc/articles/thymeleaf3migration.html)。请注意，您现有的Thymeleaf模板几乎100%与Thymeleaf 3.0兼容，因此您只需要对配置进行一些修改。
