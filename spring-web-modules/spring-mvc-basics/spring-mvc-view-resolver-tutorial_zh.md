# [Spring MVC中ViewResolver指南](https://www.baeldung.com/spring-mvc-view-resolver-tutorial)

所有MVC框架都提供了一种处理视图的方法。

Spring通过视图解析器实现这一点，它使您能够在浏览器中渲染模型，而无需将实现与特定的视图技术绑定。

ViewResolver将视图名称映射到实际视图。

Spring框架附带了很多视图解析器，例如InternalResourceViewResolver、BeanNameViewResolver等。

这是一个简单的教程，演示如何设置最常见的视图解析器以及如何在同一配置中使用多个ViewResolver。

1. Spring Web配置

    让我们从web配置开始；我们将用@EnableWebMvc、@Configuration和@ComponentScan对其进行注释：

    ```java
    @EnableWebMvc
    @Configuration
    @ComponentScan("com.baeldung.web")
    public class WebConfig implements WebMvcConfigurer {
        // All web configuration will go here
    }
    ```

    在这里，我们将在配置中设置视图解析器。

2. 添加InternalResourceViewResolver

    此ViewResolver允许我们为视图名称设置前缀或后缀等属性，以生成最终视图页面URL：

    ```java
    @Bean
    public ViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/view/");
        bean.setSuffix(".jsp");
        return bean;
    }
    ```

    为了简化示例，我们不需要控制器来处理请求。

    我们只需要一个简单的jsp页面，放在配置中定义的/WEB-INF/view文件夹中：simple.jsp。

3. 添加BeanNameViewResolver

    这是ViewResovler的一个实现，它将视图名称解释为当前应用程序上下文中的bean名称。每个这样的视图都可以在XML或Java配置中定义为bean。

    首先，我们将BeanNameViewResolver添加到以前的配置中：

    ```java
    @Bean
    public BeanNameViewResolver beanNameViewResolver(){
        return new BeanNameViewResolver();
    }
    ```

    一旦定义了ViewResolver，我们需要定义View类型的bean，以便DispatcherServlet可以执行它来呈现视图：

    ```java
    @Bean
    public View sample() {
        return new JstlView("/WEB-INF/view/sample.jsp");
    }
    ```

    下面是控制器类中对应的处理程序方法：

    ```java
    @GetMapping("/sample")
    public String showForm() {
        return "sample";
    }
    ```

    从控制器方法中，视图名称返回为“sample”，这意味着该处理程序方法中的视图解析为带有 `/WEB-INF/view/sample.jsp` URL的JstlView类。

4. 链接ViewResolver并定义订单优先级

    Spring MVC还支持多视图解析器。

    这允许您在某些情况下覆盖特定视图。我们可以通过向配置中添加多个解析器来简单地链接视图解析器。

    完成后，我们需要为这些解析器定义一个顺序。order属性用于定义链中调用的顺序。顺序属性（最大顺序号）越高，视图解析器在链中的位置越晚。

    要定义顺序，我们可以在视图解析器的配置中添加以下代码行：

    `bean.setOrder(0);`

    注意顺序优先级，因为InternalResourceViewResolver应该具有更高的顺序，因为它旨在表示一个非常明确的映射。如果其他解析器的顺序更高，则可能永远不会调用InternalResourceViewResolver。

5. 使用SpringBoot

    使用Spring Boot时，WebMvcAutoConfiguration会在应用程序上下文中自动配置InternalResourceViewResolver和BeanNameViewResolverbean。

    此外，为模板引擎添加相应的启动程序，会减少我们必须进行的手动配置。

    例如，通过向pom添加[spring-boot-starter-thymeleaf](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-thymeleaf)依赖项。xml，启用Thymeleaf，无需额外配置：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
        <version>${spring-boot-starter-thymeleaf.version}</version>
    </dependency>
    ```

    这个启动器依赖项在我们的应用程序上下文中使用名称ThymeleafViewResolver配置ThymeleafViewResolver bean。我们可以通过提供一个同名的bean来覆盖自动配置的ThymeleafViewResolver。

    Thymeleaf视图解析器通过用前缀和后缀包围视图名称来工作。前缀和后缀的默认值为‘classpath:/templates/’和‘.html’。

    Spring Boot还提供了一个选项，可以通过设置Spring.thmeleaf来更改前缀和后缀的默认值，spring.thymeleaf.prefix前缀属性和spring.thymeleaf.suffix后缀属性。

    同样，我们有[groovy-templates](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-groovy-templates)、[freemarker](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-freemarker)和[mustache](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-mustache)模板引擎的启动依赖项，我们可以使用Spring Boot来获得相应的视图解析器的自动配置。

    DispatcherServlet使用它在应用程序上下文中发现的所有视图解析器，并尝试每一个，直到得到一个结果，因此，如果我们计划添加我们自己的视图解析器，这些视图解析器的排序就变得非常重要。
