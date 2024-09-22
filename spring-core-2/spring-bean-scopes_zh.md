# [Spring Bean Scopes快速指南](https://www.baeldung.com/spring-bean-scopes)

1. 一览表

    在这个快速教程中，我们将了解Spring框架中的不同类型的bean范围。

    在我们使用它的上下文中，bean的范围定义了该bean的生命周期和可见性。

    最新版本的Spring框架定义了6种类型的范围：

    - singleton
    - prototype
    - request
    - session
    - application
    - websocket

    提到的最后四个范围，请求、会话、应用程序和websocket，仅在web-aware应用程序中可用。

2. 单例范围

    当我们定义具有单例作用域的bean时，容器会创建该bean的单个实例；对该bean名称的所有请求都将返回相同的对象，该对象被缓存。对对象的任何修改都将反映在对bean的所有引用中。如果没有指定其他范围，则此范围为默认值。

    让我们创建一个人实体来示例范围的概念：

    ```java
    public class Person {
        private String name;
        // standard constructor, getters and setters
    }
    ```

    之后，我们使用@Scope注释定义了具有单个作用域的bean：

    ```java
    @Bean
    @Scope("singleton")
    public Person personSingleton() {
        return new Person();
    }
    ```

    我们还可以通过以下方式使用常量代替字符串值：

    `@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)`

    现在我们可以继续编写一个测试，该测试表明引用同一bean的两个对象将具有相同的值，即使其中只有一个对象更改其状态，因为它们都引用了同一bean实例：

    ```java
    private static final String NAME = "John Smith";

    @Test
    public void givenSingletonScope_whenSetName_thenEqualNames() {
        ApplicationContext applicationContext = 
        new ClassPathXmlApplicationContext("scopes.xml");

        Person personSingletonA = (Person) applicationContext.getBean("personSingleton");
        Person personSingletonB = (Person) applicationContext.getBean("personSingleton");

        personSingletonA.setName(NAME);
        Assert.assertEquals(NAME, personSingletonB.getName());

        ((AbstractApplicationContext) applicationContext).close();
    }
    ```

    本例中的scopes.xml文件应包含所用bean的xml定义：

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/beans 
        http://www.springframework.org/schema/beans/spring-beans.xsd">

        <bean id="personSingleton" class="org.baeldung.scopes.Person" scope="singleton"/>    
    </beans>
    ```

3. 原型范围

    具有原型范围的bean每次从容器请求时都会返回不同的实例。它是通过在bean定义中将值原型设置为@Scope注释来定义的：

    ```java
    @Bean
    @Scope("prototype")
    public Person personPrototype() {
        return new Person();
    }
    ```

    我们也可以像单子作用域那样使用常数：

    `@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)`

    我们现在将编写一个与以前类似的测试，该测试显示两个请求具有原型范围的相同bean名称的对象。它们将有不同的状态，因为它们不再指代同一豆实例：

    ```java
    private static final String NAME = "John Smith";
    private static final String NAME_OTHER = "Anna Jones";

    @Test
    public void givenPrototypeScope_whenSetNames_thenDifferentNames() {
        ApplicationContext applicationContext = 
        new ClassPathXmlApplicationContext("scopes.xml");

        Person personPrototypeA = (Person) applicationContext.getBean("personPrototype");
        Person personPrototypeB = (Person) applicationContext.getBean("personPrototype");

        personPrototypeA.setName(NAME);
        personPrototypeB.setName(NAME_OTHER);

        Assert.assertEquals(NAME, personPrototypeA.getName());
        Assert.assertEquals(NAME_OTHER, personPrototypeB.getName());

        ((AbstractApplicationContext) applicationContext).close();
    }
    ```

    scopes.xml文件与上一节中显示的文件相似，同时添加了带有原型范围的bean的xml定义：

    `<bean id="personPrototype" class="org.baeldung.scopes.Person" scope="prototype"/>`

4. 网络感知范围

    如前所述，有四个额外的范围，仅在web-aware应用程序上下文中可用。我们在实践中较少使用这些。

    请求范围为单个HTTP请求创建bean实例，而会话范围为HTTP会话创建bean实例。

    应用程序范围为ServletContext的生命周期创建bean实例，websocket范围为特定的WebSocket会话创建bean实例。

    让我们创建一个用于实例化bean的类：

    ```java
    public class HelloMessageGenerator {
        private String message;
        // standard getter and setter
    }
    ```

    1. 请求范围

        我们可以使用@Scope注释来定义具有请求范围的bean：

        ```java
        @Bean
        @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public HelloMessageGenerator requestScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        proxyMode属性是必要的，因为在Web应用程序上下文的实例化时，没有活动请求。Spring创建要注入为依赖项的代理，并在请求中需要时实例化目标bean。

        我们还可以使用@RequestScope组成的注释，作为上述定义的快捷方式：

        ```java
        @Bean
        @RequestScope
        public HelloMessageGenerator requestScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        接下来，我们可以定义一个对requestScopedBean注入引用的控制器。为了测试网络特定范围，我们需要访问两次相同的请求。

        如果我们每次运行请求时显示消息，我们可以看到该值被重置为空，即使它后来在方法中更改了。这是因为每个请求都会返回一个不同的bean实例。

        ```java
        @Controller
        public class ScopesController {
            @Resource(name = "requestScopedBean")
            HelloMessageGenerator requestScopedBean;

            @RequestMapping("/scopes/request")
            public String getRequestScopeMessage(final Model model) {
                model.addAttribute("previousMessage", requestScopedBean.getMessage());
                requestScopedBean.setMessage("Good morning!");
                model.addAttribute("currentMessage", requestScopedBean.getMessage());
                return "scopesExample";
            }
        }
        ```

    2. 会话范围

        我们可以以类似的方式定义具有会话范围的bean：

        ```java
        @Bean
        @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public HelloMessageGenerator sessionScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        还有一个专用的复合注释，我们可以用它来简化bean定义：

        ```java
        @Bean
        @SessionScope
        public HelloMessageGenerator sessionScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        接下来，我们定义一个引用sessionScopedBean的控制器。同样，我们需要运行两个请求，以显示会话的消息字段值相同。

        在这种情况下，首次提出请求时，值消息为空。然而，一旦更改，该值将保留用于后续请求，因为整个会话都会返回相同的bean实例。

        ```java
        @Controller
        public class ScopesController {
            @Resource(name = "sessionScopedBean")
            HelloMessageGenerator sessionScopedBean;

            @RequestMapping("/scopes/session")
            public String getSessionScopeMessage(final Model model) {
                model.addAttribute("previousMessage", sessionScopedBean.getMessage());
                sessionScopedBean.setMessage("Good afternoon!");
                model.addAttribute("currentMessage", sessionScopedBean.getMessage());
                return "scopesExample";
            }
        }
        ```

    3. 应用范围

        应用程序范围为ServletContext的生命周期创建bean实例。

        这与单个范围相似，但在豆的范围方面有一个非常重要的区别。

        当bean是应用程序范围时，bean的同一实例在同一ServletContext中运行的多个基于servlet的应用程序之间共享，而单例范围的bean仅范围为单个应用程序上下文。

        让我们用应用程序范围创建bean：

        ```java
        @Bean
        @Scope(
        value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public HelloMessageGenerator applicationScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        与请求和会话范围类似，我们可以使用一个较短的版本：

        ```java
        @Bean
        @ApplicationScope
        public HelloMessageGenerator applicationScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        现在让我们创建一个引用此bean的控制器：

        ```java
        @Controller
        public class ScopesController {
            @Resource(name = "applicationScopedBean")
            HelloMessageGenerator applicationScopedBean;

            @RequestMapping("/scopes/application")
            public String getApplicationScopeMessage(final Model model) {
                model.addAttribute("previousMessage", applicationScopedBean.getMessage());
                applicationScopedBean.setMessage("Good afternoon!");
                model.addAttribute("currentMessage", applicationScopedBean.getMessage());
                return "scopesExample";
            }
        }
        ```

        在这种情况下，一旦在applicationScopedBean中设置，值消息将保留给所有后续请求、会话，甚至将访问此bean的不同servlet应用程序，前提是它在sameServletContext中运行。

    4. WebSocket范围

        最后，让我们用websocket范围创建bean：

        ```java
        @Bean
        @Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
        public HelloMessageGenerator websocketScopedBean() {
            return new HelloMessageGenerator();
        }
        ```

        首次访问时，WebSocket范围的bean存储在WebSocket会话属性中。在整个WebSocket会话期间，每当访问bean时，都会返回相同的bean实例。

        我们也可以说它表现出单人行为，但仅限于WebSocket会话。

5. 结论

    在本文中，我们讨论了Spring提供的不同bean范围及其预期用途。
