# [Spring使用Thymeleaf简介](https://www.baeldung.com/thymeleaf-in-spring-mvc)

1. 一览表

    Thymeleaf是一个Java模板引擎，用于处理和创建HTML、XML、JavaScript、CSS和文本。

    在本教程中，我们将讨论如何将Thymeleaf与Spring一起使用，以及Spring MVC应用程序的视图层中的一些基本用例。

    该库非常可扩展，其自然的模板能力确保了我们可以在没有后端的情况下原型模板。与JSP等其他流行的模板引擎相比，这使得开发速度非常快。

2. 将Thymeleaf与Spring融合

    首先，让我们看看与Spring集成所需的配置。整合需要百里香叶-弹簧库。

    我们将将以下依赖项添加到我们的Maven POM文件中：

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

    请注意，对于Spring 4项目，我们必须使用thymeleaf-spring4库，而不是thymeleaf-spring5。

    SpringTemplateEngine类执行所有配置步骤。

    我们可以在Java配置文件中将此类配置为bean：

    ```java
    @Bean
    @Description("Thymeleaf Template Resolver")
    public ServletContextTemplateResolver templateResolver() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        return templateResolver;
    }

    @Bean
    @Description("Thymeleaf Template Engine")
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setTemplateEngineMessageSource(messageSource());
        return templateEngine;
    }
    ```

    templateResolver bean属性前缀和后缀分别表示webapp目录中视图页面的位置及其文件扩展名。

    Spring MVC中的ViewResolver接口将控制器返回的视图名称映射到实际视图对象。ThymeleafViewResolver实现了ViewResolver接口，用于在给定视图名称时确定要渲染的Thymeleaf视图。

    集成的最后一步是将ThymeleafViewResolver添加为bean：

    ```java
    @Bean
    @Description("Thymeleaf View Resolver")
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setOrder(1);
        return viewResolver;
    }
    ```

3. Spring Boot中的Thymeleaf

    Spring Boot通过添加spring-boot-starter-thymeleaf依赖项为Thymeleaf提供自动配置：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
        <version>2.3.3.RELEASE</version>
    </dependency>
    ```

    不需要显式配置。默认情况下，HTML文件应放置在资源/模板位置。

4. 显示来自消息源的值（属性文件）

    我们可以使用th:text=”#{key}”标签属性来显示属性文件中的值。

    为了使这发挥作用，我们需要将属性文件配置为messageSource bean：

    ```java
    @Bean
    @Description("Spring Message Resolver")
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }
    ```

    这是Thymeleaf HTML代码，用于显示与key welcome.message相关的值：

    `<span th:text="#{welcome.message}" />`

5. 显示模型属性

    1. 简单的属性

        我们可以使用th:text=”${attributename}”标签属性来显示模型属性的值。

        让我们在控制器类中添加一个名为serverTime的模型属性：

        `model.addAttribute("serverTime", dateFormat.format(new Date()));`

        这是显示serverTime属性值的HTML代码：

        `Current time is <span th:text="${serverTime}" />`

    2. 集合属性

        如果模型属性是对象的集合，我们可以使用th:each标签属性来迭代它。

        让我们用两个字段ID和name来定义一个学生模型类：

        ```java
        public class Student implements Serializable {
            private Integer id;
            private String name;
            // standard getters and setters
        }
        ```

        现在，我们将在控制器类中添加一个学生列表作为模型属性：

        ```java
        List<Student> students = new ArrayList<Student>();
        // logic to build student data
        model.addAttribute("students", students);
        ```

        最后，我们可以使用Thymeleaf模板代码来迭代学生列表并显示所有字段值：

        ```jsp
        <tbody>
            <tr th:each="student: ${students}">
                <td th:text="${student.id}" />
                <td th:text="${student.name}" />
            </tr>
        </tbody>
        ```

6. 有条件评估

    1. 如果和除非

        如果满足条件，我们使用th:if=”${condition}”属性来显示视图的一部分。如果条件不满足，我们使用theth:unless=”${condition}”属性来显示视图的一部分。

        让我们在学生模型中添加一个性别字段：

        ```java
        public class Student implements Serializable {
            private Integer id;
            private String name;
            private Character gender;

            // standard getters and setters
        }

        ```

        假设此字段有两个可能的值（M或F）来表示学生的性别。

        如果我们希望显示“男性”或“女性”一词，而不是单个字符，我们可以使用此Thymeleaf代码来做到这一点：

        ```jsp
        <td>
            <span th:if="${student.gender} == 'M'" th:text="Male" /> 
            <span th:unless="${student.gender} == 'M'" th:text="Female" />
        </td>
        ```

    2. 开关和外壳

        我们使用th:switch和th:case属性来使用switch语句结构有条件地显示内容。

        让我们使用th:switch和th:case属性重写之前的代码：

        ```jsp
        <td th:switch="${student.gender}">
            <span th:case="'M'" th:text="Male" /> 
            <span th:case="'F'" th:text="Female" />
        </td>
        ```

7. 处理用户输入

    我们可以使用th:action=”@{url}”和th:object=”${object}”属性来处理表单输入。我们使用th:action来提供表单操作URL，th:object来指定提交的表单数据将绑定的对象。

    单个字段使用th:field=”*{name}”属性进行映射，其中name是对象的匹配属性。

    对于学生班，我们可以创建一个输入表单：

    ```jsp
    <form action="#" th:action="@{/saveStudent}" th:object="${student}" method="post">
        <table border="1">
            <tr>
                <td><label th:text="#{msg.id}" /></td>
                <td><input type="number" th:field="*{id}" /></td>
            </tr>
            <tr>
                <td><label th:text="#{msg.name}" /></td>
                <td><input type="text" th:field="*{name}" /></td>
            </tr>
            <tr>
                <td><input type="submit" value="Submit" /></td>
            </tr>
        </table>
    </form>
    ```

    在上述代码中，/saveStudent是表单操作URL，学生是保存提交的表单数据的对象。

    saveStudent方法处理表格提交：

    ```java
    @RequestMapping(value = "/saveStudent", method = RequestMethod.POST)
    public String saveStudent(Model model, @ModelAttribute("student") Student student) {
        // logic to process input data
    }
    ```

    @RequestMapping注释将控制器方法与表单中提供的URL映射。注释方法saveStudent()对提交的表格执行必要的处理。最后，@ModelAttribute注释将表单字段绑定到学生对象。

8. 显示验证错误

    我们可以使用#fields.hasErrors（）函数来检查字段是否存在任何验证错误。我们使用#fields.errors()函数来显示特定字段的错误。字段名称是这两个函数的输入参数。

    让我们来看看HTML代码，以迭代和显示表单中每个字段的错误：

    ```jsp
    <ul>
        <li th:each="err : ${#fields.errors('id')}" th:text="${err}" />
        <li th:each="err : ${#fields.errors('name')}" th:text="${err}" />
    </ul>
    ```

    上述函数接受通配符字符*或常量all来表示所有字段，而不是字段名称。我们使用th:each属性来迭代每个字段中可能存在的多个错误。

    这是之前使用通配符*重写的HTML代码：

    ```jsp
    <ul>
        <li th:each="err : ${#fields.errors('*')}" th:text="${err}" />
    </ul>
    ```

    在这里，我们使用常数全部：

    ```jsp
    <ul>
        <li th:each="err : ${#fields.errors('all')}" th:text="${err}" />
    </ul>
    ```

    同样，我们可以使用全局常量在Spring中显示全局错误。

    这是显示全局错误的HTML代码：

    ```jsp
    <ul>
        <li th:each="err : ${#fields.errors('global')}" th:text="${err}" />
    </ul>
    ```

    此外，我们可以使用th:errors属性来显示错误消息。

    之前在表单中显示错误的代码可以使用th:errors属性重写：

    ```jsp
    <ul>
        <li th:errors="*{id}" />
        <li th:errors="*{name}" />
    </ul>
    ```

9. 使用转换

    我们使用双括号语法`{{}}`来格式化数据以进行显示。这使用了上下文文件的转换服务豆中为该类型字段配置的格式化程序。

    让我们看看如何在学生课堂中格式化姓名字段：

    ```jsp
    <tr th:each="student: ${students}">
        <td th:text="${{student.name}}" />
    </tr>
    ```

    上述代码使用NameFormatter类，通过覆盖WebMvcConfigurer接口中的addFormatters()方法进行配置。

    为此，我们的@Configuration类覆盖了WebMvcConfigurerAdapter类：

    ```java
    @Configuration
    public class WebMVCConfig extends WebMvcConfigurerAdapter {
        // ...
        @Override
        @Description("Custom Conversion Service")
        public void addFormatters(FormatterRegistry registry) {
            registry.addFormatter(new NameFormatter());
        }
    }
    ```

    NameFormatter类实现了Spring Formatter接口。

    我们还可以使用#conversions实用程序来转换对象以进行显示。实用程序函数的语法是#conversions.convert（对象，类），其中对象转换为类类型。

    以下是在删除分数部分时显示学生对象百分比字段的方法：

    ```jsp
    <tr th:each="student: ${students}">
        <td th:text="${#conversions.convert(student.percentage, 'Integer')}" />
    </tr>
    ```

10. 结论

    在本文中，我们看到了如何在Spring MVC应用程序中集成和使用Thymeleaf。

    我们还看到了如何显示字段、接受输入、显示验证错误和转换数据以进行显示的示例。
