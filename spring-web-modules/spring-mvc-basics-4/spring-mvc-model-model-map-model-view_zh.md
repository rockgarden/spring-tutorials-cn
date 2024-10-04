# [Spring MVC中的模型、ModelMap和ModelView](https://www.baeldung.com/spring-mvc-model-model-map-model-view)

1. 一览表

    在本文中，我们将研究Spring MVC提供的核心org.springframework.ui.Model、org.springframework.ui.ModelMap andorg.springframework.web.servlet.ModelAndView的使用。

2. Maven附属机构

    让我们从pom.xml文件中的spring-boot-starter-web依赖项开始：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.0.2</version>
    </dependency>
    ```

    最新版本的spring-boot-starter-web依赖项可以在这里找到。

    而且，如果我们使用Thymeleaf作为我们的观点，我们应该将此依赖项添加到pom.xml中：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
        <version>3.0.2</version>
    </dependency>
    ```

3. 模型

    让我们从最基本的概念开始——模型。

    简单地说，该模型可以提供用于渲染视图的属性。

    为了向视图提供可用数据，我们只需将此数据添加到其模型对象中。此外，带有属性的映射可以与模型实例合并：

    ```java
    @GetMapping("/showViewPage")
    public String passParametersWithModel(Model model) {
        Map<String, String> map = new HashMap<>();
        map.put("spring", "mvc");
        model.addAttribute("message", "Baeldung");
        model.mergeAttributes(map);
        return "view/viewPage";
    }
    ```

4. 模型Map

    就像上面的模型接口一样，ModelMap也用于传递值以渲染视图。

    ModelMap的优点是，它使我们能够传递一个值集合，并将这些值视为在地图中：

    ```java
    @GetMapping("/printViewPage")
    public String passParametersWithModelMap(ModelMap map) {
        map.addAttribute("welcomeMessage", "welcome");
        map.addAttribute("message", "Baeldung");
        return "view/viewPage";
    }
    ```

5. 模型和视图

    将值传递给视图的最终接口是ModelAndView。

    此接口允许我们在一次返回中传递Spring MVC所需的所有信息：

    ```java
    @GetMapping("/goToViewPage")
    public ModelAndView passParametersWithModelAndView() {
        ModelAndView modelAndView = new ModelAndView("view/viewPage");
        modelAndView.addObject("message", "Baeldung");
        return modelAndView;
    }
    ```

6. 观点

    我们放在这些模型中的所有数据都由视图使用——通常，用于渲染网页的模板视图。

    如果我们有一个由控制器的方法作为视图的Thymeleaf模板文件。通过模型传递的参数将可以从thymeleaf HTML代码中访问：

    ```jsp
    <!DOCTYPE HTML>
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <title>Title</title>
    </head>
    <body>
        <div>Web Application. Passed parameter : <span th:text="${message}"></span></div>
    </body>
    </html>
    ```

    此处传递的参数通过语法${message}使用，该语法被称为占位符。Thymeleaf模板引擎将用通过模型传递的同名属性的实际值替换此占位符。

7. 结论

    在这个快速教程中，我们讨论了Spring MVC与Spring Boot的三个核心概念——模型、模型Map和模型和视图。我们还查看了视图如何利用这些值的示例。
