# [Spring MVC中的会话属性](https://www.baeldung.com/spring-mvc-session-attributes)

1. 一览表

    在开发Web应用程序时，我们通常需要在多个视图中引用相同的属性。例如，我们可能有需要显示在多个页面上的购物车内容。

    存储这些属性的好位置是在用户的会话中。

    在本教程中，我们将专注于一个简单的示例，并研究使用会话属性的2种不同策略：

    - 使用范围代理
    - 使用@SessionAttributes注释

2. Maven设置

    我们将使用Spring Boot启动器来引导我们的项目，并带来所有必要的依赖项。

    我们的设置需要parent声明、web starter和thymeleaf starter。

    我们还将包括spring test starter，以便在我们的单元测试中提供一些额外的效用：

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <relativePath/>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ```

3. 示例用例

    我们的示例将实现一个简单的“TODO”应用程序。我们将有一个用于创建TodoItem实例的表单和一个显示所有TodoItems的列表视图。

    如果我们使用该表单创建TodoItem，则随后对表单的访问将预先填充最近添加的TodoItem的值。我们将使用此功能来演示如何“remember”存储在会话范围内的表单值。

    我们的2个模型类被实现为简单的POJO：

    ```java
    public class TodoItem {

        private String description;
        private LocalDateTime createDate;

        // getters and setters
    }

    public class TodoList extends ArrayDeque<TodoItem>{}
    ```

    我们的 TodoList 类扩展了 ArrayDeque，这样我们就可以通过 peekLast 方法方便地访问最近添加的项目。

    我们需要 2 个控制器类： 我们将研究的每种策略都需要一个控制器类。它们会有细微的差别，但核心功能都会在这两个类中体现。每个类都将有 3 个 @RequestMappings：

    - @GetMapping("/form") – 此方法将负责初始化表单和渲染表单视图。如果TodoList不是空的，该方法将预先填充表单中最近添加的TodoItem。
    - @PostMapping（"/form"）-此方法将负责将提交的TodoItem添加到TodoList中并重定向到列表URL。
    - @GetMapping("/todos.html") – 此方法将简单地将TodoList添加到模型中，以显示并呈现列表视图。

4. 使用范围代理

    1. 设置

        在此设置中，我们的 TodoList 被配置为由代理支持的会话作用域 @Bean。@Bean 是一个代理，这意味着我们可以将其注入到我们的单例作用域 @Controller 中。

        由于上下文初始化时没有会话，Spring 将创建 TodoList 的代理，并将其作为依赖注入。当请求需要时，TodoList 的目标实例将被实例化。

        首先，我们在 @Configuration 类中定义 Bean：

        ```java
        @Bean
        @Scope(
        value = WebApplicationContext.SCOPE_SESSION,
        proxyMode = ScopedProxyMode.TARGET_CLASS)
        public TodoList todos() {
            return new TodoList();
        }
        ```

        接下来，我们将bean声明为@Controller的依赖项，并像注入任何其他依赖项一样注入它：

        ```java
        @Controller
        @RequestMapping("/scopedproxy")
        public class TodoControllerWithScopedProxy {

            private TodoList todos;

            // constructor and request mappings
        }
        ```

        最后，在请求中使用bean只需要调用其方法：

        ```java
        @GetMapping("/form")
        public String showForm(Model model) {
            if (!todos.isEmpty()) {
                model.addAttribute("todo", todos.peekLast());
            } else {
                model.addAttribute("todo", new TodoItem());
            }
            return "scopedproxyform";
        }
        ```

    2. 单元测试

        为了使用范围代理测试我们的实现，我们首先配置了一个SimpleThreadScope。这将确保我们的单元测试准确模拟我们正在测试的代码的运行时条件。

        首先，我们定义了一个TestConfig和一个CustomScopeConfigurer：

        ```java
        @Configuration
        public class TestConfig {
            @Bean
            public CustomScopeConfigurer customScopeConfigurer() {
                CustomScopeConfigurer configurer = new CustomScopeConfigurer();
                configurer.addScope("session", new SimpleThreadScope());
                return configurer;
            }
        }
        ```

        现在，我们可以从测试表单的初始请求包含未初始化的TodoItem开始：

        ```java
        @RunWith(SpringRunner.class)
        @SpringBootTest
        @AutoConfigureMockMvc
        @Import(TestConfig.class)
        public class TodoControllerWithScopedProxyIntegrationTest {

            // ...

            @Test
            public void whenFirstRequest_thenContainsUnintializedTodo() throws Exception {
                MvcResult result = mockMvc.perform(get("/scopedproxy/form"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("todo"))
                .andReturn();

                TodoItem item = (TodoItem) result.getModelAndView().getModel().get("todo");
        
                assertTrue(StringUtils.isEmpty(item.getDescription()));
            }
        }
        ```

        我们还可以确认我们的提交问题重定向，后续表单请求预先填充了新添加的TodoItem：

        ```java
        @Test
        public void whenSubmit_thenSubsequentFormRequestContainsMostRecentTodo() throws Exception {
            mockMvc.perform(post("/scopedproxy/form")
            .param("description", "newtodo"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

            MvcResult result = mockMvc.perform(get("/scopedproxy/form"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("todo"))
            .andReturn();
            TodoItem item = (TodoItem) result.getModelAndView().getModel().get("todo");
        
            assertEquals("newtodo", item.getDescription());
        }
        ```

    3. 讨论

        使用作用域代理策略的一个主要特点是，它对请求映射方法签名没有影响。与 @SessionAttributes 策略相比，该策略可保持较高的可读性。

        回顾一下控制器默认具有单例作用域可能会有所帮助。

        这就是为什么我们必须使用代理，而不是简单地注入一个非代理的会话作用域 Bean 的原因。我们不能将作用域较小的 Bean 注入到作用域较大的 Bean 中。

        在这种情况下，如果尝试这样做，就会触发异常，信息包含当前线程的作用域'session'未激活。

        如果我们愿意使用会话作用域定义控制器，就可以避免指定 proxyMode。但这样做也有缺点，尤其是如果控制器的创建成本很高，因为必须为每个用户会话创建一个控制器实例。

        请注意，TodoList 可供其他组件注入。这可能是优点，也可能是缺点，具体取决于用例。如果让整个应用程序都能使用 bean 会有问题，那么可以使用 @SessionAttributes 将实例的作用域设置为控制器，我们将在下一个示例中看到这一点。

5. 使用@SessionAttributes注释

    1. 设置

        在此设置中，我们没有将TodoList定义为Spring管理的@Bean。相反，我们将其声明为@ModelAttribute，并指定@SessionAttributes注释，将其范围到控制器的会话中。

        首次访问我们的控制器时，Spring将实例实例并将其放置在模型中。由于我们也在@SessionAttributes中声明bean，Spring将存储实例。

        首先，我们通过在控制器上提供方法来声明我们的bean，并用@ModelAttribute注释方法：

        ```java
        @ModelAttribute("todos")
        public TodoList todos() {
            return new TodoList();
        }
        ```

        接下来，我们通知控制器使用@SessionAttributes将我们的TodoList视为会话范围：

        ```java
        @Controller
        @RequestMapping("/sessionattributes")
        @SessionAttributes("todos")
        public class TodoControllerWithSessionAttributes {
            // ... other methods
        }
        ```

        最后，为了在请求中使用bean，我们在@RequestMapping的方法签名中提供对它的引用：

        ```java
        @GetMapping("/form")
        public String showForm(
        Model model,
        @ModelAttribute("todos") TodoList todos) {
            if (!todos.isEmpty()) {
                model.addAttribute("todo", todos.peekLast());
            } else {
                model.addAttribute("todo", new TodoItem());
            }
            return "sessionattributesform";
        }
        ```

        在@PostMapping方法中，我们在返回我们的RedirectView之前注入RedirectAttributes并调用addFlashAttribute。与我们的第一个例子相比，这是在实施方面的一个重要区别：

        ```java
        @PostMapping("/form")
        public RedirectView create(
        @ModelAttribute TodoItem todo,
        @ModelAttribute("todos") TodoList todos,
        RedirectAttributes attributes) {
            todo.setCreateDate(LocalDateTime.now());
            todos.add(todo);
            attributes.addFlashAttribute("todos", todos);
            return new RedirectView("/sessionattributes/todos.html");
        }
        ```

        Spring 在重定向场景中使用专门的模型 RedirectAttributes 实现，以支持 URL 参数编码。在重定向过程中，存储在 Model 上的任何属性通常只有在 URL 中包含这些属性时才会被框架使用。

        通过使用 addFlashAttribute，我们可以告诉框架，我们希望 TodoList 能在重定向中存活，而无需在 URL 中编码。

    2. 单元测试

        表单视图控制器方法的单元测试与我们在第一个示例中看到的测试相同。然而，@PostMapping的测试略有不同，因为我们需要访问闪存属性来验证行为：

        ```java
        @Test
        public void whenTodoExists_thenSubsequentFormRequestContainsesMostRecentTodo() throws Exception {
            FlashMap flashMap = mockMvc.perform(post("/sessionattributes/form")
            .param("description", "newtodo"))
            .andExpect(status().is3xxRedirection())
            .andReturn().getFlashMap();

            MvcResult result = mockMvc.perform(get("/sessionattributes/form")
            .sessionAttrs(flashMap))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("todo"))
            .andReturn();
            TodoItem item = (TodoItem) result.getModelAndView().getModel().get("todo");
        
            assertEquals("newtodo", item.getDescription());
        }
        ```

    3. 讨论

        在会话中存储属性的 @ModelAttribute 和 @SessionAttributes 策略是一种直接的解决方案，不需要额外的上下文配置或 Spring 管理的 @Bean 。

        与第一个示例不同的是，我们必须在 @RequestMapping 方法中注入 TodoList。

        此外，我们还必须在重定向场景中使用flash属性。

6. 结论

    在本文中，我们研究了使用范围代理和@SessionAttributes作为在Spring MVC中使用会话属性的2种策略。请注意，在这个简单的例子中，存储在会话中的任何属性只会在会话的生命周期内生存。

    如果我们需要在服务器重新启动或会话超时之间保留属性，我们可以考虑使用Spring Session来透明地处理保存信息。请查看我们关于春季会议的文章以了解更多信息。
