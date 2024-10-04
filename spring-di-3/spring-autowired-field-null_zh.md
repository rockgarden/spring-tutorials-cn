# [Spring @Autowired Field Null – 常见原因和解决方案](https://www.baeldung.com/spring-autowired-field-null)

1. 一览表

    在本教程中，我们将看到导致 @Autowired 字段出现[NullPointerException](https://www.baeldung.com/java-14-nullpointerexception)的常见错误。我们还将解释如何解决这个问题。

2. 问题的陈述

    首先，让我们用空的doWork方法定义一个Spring组件：

    ```java
    @Component
    public class MyComponent {
        public void doWork() {}
    }
    ```

    然后，让我们定义一下我们的服务类别。我们将使用Spring功能在我们的服务中注入MyComponent bean，以便我们可以在服务方法中调用doWork方法：

    ```java
    public class MyService {

        @Autowired
        MyComponent myComponent;
        
        public String serve() {
            myComponent.doWork();
            return "success";
        }
    }
    ```

    现在，让我们添加一个控制器，该控制器将实例化服务并调用服务方法：

    ```java
    @Controller
    public class MyController {

        public String control() {
            MyService userService = new MyService();
            return userService.serve();
        }
    }
    ```

    乍一看，我们的代码可能看起来非常好。然而，在运行应用程序后，调用我们控制器的控制方法将导致以下异常：

    ```log
    java.lang.NullPointerException: null
    at com.baeldung.autowiring.service.MyService.serve(MyService.java:14)
    at com.baeldung.autowiring.controller.MyController.control(MyController.java:12)
    ```

    这里发生了什么？当我们在控制器中调用MyService构造函数时，我们创建了一个不由Spring管理的对象。Spring不知道这个MyService对象的存在，无法在其中注入MyComponent bean。因此，我们创建的MyService对象中的MyComponent实例将保持空，导致我们尝试调用此对象上的方法时获得的NullPointerException。

3. 解决方案

    为了解决这个问题，我们必须将控制器中使用的MyService实例变成Spring管理的Bean。

    首先，让我们告诉Spring为我们的MyService类生成一个Bean。我们有各种可能性来实现这一目标。最简单的方法是用@Component注释或其任何衍生物来装饰MyService类。例如，我们可以做以下事情：

    ```java
    @Service
    public class MyService {

        @Autowired
        MyComponent myComponent;
        
        public String serve() {
            myComponent.doWork();
            return "success";
        }
    }
    ```

    实现相同目标的另一种替代方案是在@Configuration文件中添加@Bean方法：

    ```java
    @Configuration
    public class MyServiceConfiguration {
        @Bean
        MyService myService() {
            return new MyService();
        }
    }
    ```

    然而，将MyService类转换为Spring管理的bean是不够的。现在，我们必须在控制器内自动接线，而不是在控制器上调用新连接。让我们看看控制器的固定版本是什么样子的：

    ```java
    @Controller
    public class MyController {

        @Autowired
        MyService myService;
        
        public String control() {
            return myService.serve();
        }
    }
    ```

    现在，调用控制方法将按预期返回服务方法的结果。

4. 结论

    在本文中，我们看到了一个非常常见的错误，当我们无意中将Spring注入与我们通过调用其构造函数创建的对象混合时，该错误会导致NullPointerException。我们通过逃避这种责任mic-mac解决了问题，并将我们过去管理自己的对象变成了Spring-managed Bean。
