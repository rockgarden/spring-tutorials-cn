# [在Spring MVC中使用FreeMarker的介绍](https://www.baeldung.com/freemarker-in-spring-mvc-tutorial)

1. 一览表

    FreeMarker是Apache软件基金会基于Java的模板引擎。与其他模板引擎一样，FreeMarker旨在支持遵循MVC模式的应用程序中的HTML网页。本教程说明了如何配置FreeMarker在Spring MVC中使用，作为JSP的替代品。

    此外，这并非旨在详细了解FreeMarker的广泛功能。有关FreeMarker使用和语法的更多信息，请访问其[网站](http://freemarker.incubator.apache.org/)。

2. Maven附属机构

    由于这是一个基于Maven的项目，我们首先将所需的依赖项添加到pom.xml中：

    ```xml
    <dependency>
        <groupId>org.freemarker</groupId>
        <artifactId>freemarker</artifactId>
        <version>2.3.23</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context-support</artifactId>
        <version>${spring.version}</version>
    </dependency>
    ```

3. 配置

    现在让我们深入了解项目的配置。这是一个基于注释的Spring项目，因此我们不会演示基于XML的配置。

    1. Spring网络配置

        让我们创建一个类来配置Web组件。为此，我们需要用@EnableWebMvc、@Configuration和@ComponentScan对类进行注释。

        ```java
        @EnableWebMvc
        @Configuration
        @ComponentScan({"com.baeldung.freemarker"})
        public class SpringWebConfig extends WebMvcConfigurerAdapter {
            // All web configuration will go here.
        }
        ```

    2. 配置ViewResolver

        Spring MVC Framework提供ViewResolver接口，将视图名称映射到实际视图。我们将创建一个FreeMarkerViewResolver的实例，该实例属于spring-webmvc依赖项。

        该对象需要配置运行时使用的所需值。例如，我们将配置视图解析器，将FreeMarker用于以.ftl结尾的视图：

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

        此外，请注意我们如何在这里控制缓存模式——仅应用于调试和开发。

    3. FreeMarker模板路径配置

        接下来，我们将设置模板路径，该路径指示模板在Web上下文中的位置：

        ```java
        @Bean
        public FreeMarkerConfigurer freemarkerConfig() {
            FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
            freeMarkerConfigurer.setTemplateLoaderPath("/WEB-INF/views/ftl/");
            return freeMarkerConfigurer;
        }
        ```

    4. Spring控制器配置

        现在，我们可以使用Spring控制器来处理FreeMarker模板以进行显示。这只是一个传统的弹簧控制器：

        ```java
        @RequestMapping(value = "/cars", method = RequestMethod.GET)
        public String init(@ModelAttribute("model") ModelMap model) {
            model.addAttribute("carList", carList);
            return "index";
        }
        ```

        之前定义的FreeMarkerViewResolver和路径配置将负责将视图nameindex转换为适当的FreeMarker视图。

4. FreeMarker HTML模板

    1. 创建简单的HTML模板视图

        现在是时候使用FreeMarker创建一个HTML模板了。在我们的示例中，我们在模型中添加了一个汽车列表。FreeMarker可以通过迭代其内容来访问该列表并显示它。

        当对/cars URI提出请求时，Spring将使用提供的模型处理模板。在我们的模板中，#list指令表示FreeMarker应该循环模型中的carList对象，使用car引用当前元素并渲染该块中的内容。

        以下代码还包括FreeMarker表达式，以引用carList中每个元素的属性；或者例如，为了显示当前car元素的make属性，我们使用表达式${car.make}。

        ```html
        <div id="header">
        <h2>FreeMarker Spring MVC Hello World</h2>
        </div>
        <div id="content">
        <fieldset>
            <legend>Add Car</legend>
            <form name="car" action="add" method="post">
            Make : <input type="text" name="make" /><br/>
            Model: <input type="text" name="model" /><br/>
            <input type="submit" value="Save" />
            </form>
        </fieldset>
        <br/>
        <table class="datatable">
            <tr>
            <th>Make</th>
            <th>Model</th>
            </tr>
            <#list model["carList"] as car>
            <tr>
                <td>${car.make}</td>
                <td>${car.model}</td>
            </tr>
            </#list>
        </table>
        </div>
        ```

        使用CSS对输出进行样式后，处理后的FreeMarker模板会生成一个表格和汽车列表。

5. SpringBoot

    如果我们使用Spring Boot，我们可以简单地导入spring-boot-starter-freemarker依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-freemarker</artifactId>
        <version>2.3.4.RELEASE</version>
    </dependency>
    ```

    然后，我们只需要在src/main/resources/templates下添加模板文件。Spring Boot负责其他默认配置，如FreeMarkerConfigurer和FreeMarkerViewResolver。

6. 结论

    在本文中，我们讨论了如何将FreeMarker集成到Spring MVC应用程序中。FreeMarker的功能远远超出了我们展示的，因此请访问Apache FreeMarker网站以获取有关其使用的更多详细信息。
