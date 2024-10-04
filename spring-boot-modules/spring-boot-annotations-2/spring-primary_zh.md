# [Spring@初级注释](https://www.baeldung.com/spring-primary)

1. 一览表

    在本快速教程中，我们将讨论Spring的@Primary注释，该注释是在框架3.0版本中引入的。

    简单地说，当有多个相同类型的豆子时，我们使用@Primary来提高对豆子的偏好。

    让我们详细描述一下这个问题。

2. 为什么需要@Primary？

    在某些情况下，我们需要注册多个相同类型的豆子。

    在本例中，我们有员工类型的JohnEmployee（）和TonyEmployee（）bean：

    ```java
    @Configuration
    public class Config {

        @Bean
        public Employee JohnEmployee() {
            return new Employee("John");
        }

        @Bean
        public Employee TonyEmployee() {
            return new Employee("Tony");
        }
    }
    ```

    如果我们尝试运行应用程序，Spring会抛出NoUniqueBeanDefinitionException。

    为了访问相同类型的豆子，我们通常使用@Qualifier（“beanName”）注释。

    我们和@Autowired一起在注射点应用它。在我们的案例中，我们在配置阶段选择豆子，因此@Qualifier不能在这里应用。我们可以通过点击链接了解更多关于@Qualifier注释的信息。

    为了解决这个问题，Spring提供了@Primary注释。

3. 将@Primary与@Bean一起使用

    让我们来看看配置类：

    ```java
    @Configuration
    public class Config {

        @Bean
        public Employee JohnEmployee() {
            return new Employee("John");
        }

        @Bean
        @Primary
        public Employee TonyEmployee() {
            return new Employee("Tony");
        }
    }
    ```

    我们用@Primary标记TonyEmployee（）bean。Spring将优先注入TonyEmployee（）bean，而不是JohnEmployee（）。

    现在，让我们启动应用程序上下文，并从中获取员工豆：

    ```java
    AnnotationConfigApplicationContext context
    = new AnnotationConfigApplicationContext(Config.class);

    Employee employee = context.getBean(Employee.class);
    System.out.println(employee);
    ```

    在我们运行应用程序后：

    `Employee{name='Tony'}`

    从输出中，我们可以看到TonyEmployee（）实例在自动接线时有一个首选项。

4. 使用@Primary和@Component

    我们可以直接在豆子上使用@Primary。让我们来看看以下场景：

    ```java
    public interface Manager {
        String getManagerName();
    }
    ```

    我们有一个经理接口和两个子类豆，部门经理：

    ```java
    @Component
    public class DepartmentManager implements Manager {
        @Override
        public String getManagerName() {
            return "Department manager";
        }
    }
    ```

    以及总经理bean：

    ```java
    @Component
    @Primary
    public class GeneralManager implements Manager {
        @Override
        public String getManagerName() {
            return "General manager";
        }
    }
    ```

    它们都覆盖了管理器接口的getManagerName（）。此外，请注意，我们用@Primary标记了总经理bean。

    这一次，@Primary只有在我们启用组件扫描时才有意义：

    ```java
    @Configuration
    @ComponentScan(basePackages="org.baeldung.primary")
    public class Config {
    }
    ```

    让我们创建一个服务，在找到正确的bean时使用依赖注入：

    ```java
    @Service
    public class ManagerService {

        @Autowired
        private Manager manager;

        public Manager getManager() {
            return manager;
        }
    }
    ```

    在这里，beans DepartmentManager和GeneralManager都有资格进行自动接线。

    当我们用@Primary标记总经理bean时，它将被选择用于依赖注入：

    ```java
    ManagerService service = context.getBean(ManagerService.class);
    Manager manager = service.getManager();
    System.out.println(manager.getManagerName());
    ```

    输出是“General manager””。

5. 关于Bean命名的澄清

    此外，在使用@Primary注释时，确保每个bean名称在上下文中都是唯一的，这一点很重要。Spring使用方法名称作为默认bean名称。虽然@Primary指示在模棱两可的情况下，默认应该注入哪个bean，但它不允许在同一配置类中具有相同名称的多个bean。

    为了避免这种冲突，我们需要确保配置类中的每个方法都有一个唯一的名称。此外，我们可以使用名称属性直接在@Bean注释中明确指定名称：

    ```java
    @Bean(name = "mercedesCar")
    public Car car1() {
        return new Car("Mercedes");
    }

    @Bean(name = "bmwCar")
    @Primary
    public Car car2() {
        return new Car("BMW");
    }
    ```

    在这种情况下，豆子被命名为mercedesCar和bmwCar，以确保独特性并避免冲突。

6. 结论

    在这篇文章中，我们了解了Spring的@Primary注释。通过代码示例，我们展示了@Primary的必要性和用例。
