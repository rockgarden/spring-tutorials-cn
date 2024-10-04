# [Spring的Bean名字](https://www.baeldung.com/spring-bean-names)

1. 一览表

    当我们有多个相同类型的实现时，命名Spring bean是非常有帮助的。这是因为如果我们的Bean没有独特的名称，Spring注入Bean将是模棱两可的。

    通过控制Bean的命名，我们可以告诉Spring我们想要将哪种Bean注入目标对象。

    在本文中，我们将讨论春Bean命名策略，并探索如何为一种Bean种命名。

2. 默认Bean命名策略

    Spring为创建bean提供了多个注释。我们可以在不同级别使用这些注释。例如，我们可以在bean类上放置一些注释，在创建bean的方法上放置其他注释。

    首先，让我们看看Spring的默认命名策略在运行。当我们只是指定没有任何值的注释时，Spring如何命名我们的bean？

    1. 班级级注释

        让我们从类级使用的注释的默认命名策略开始。为了命名一个Bean，Spring使用类名，并将第一个字母转换为小写字母。

        让我们来看看一个例子：

        ```java
        @Service
        public class LoggingService {
        }
        ```

        在这里，Spring为LoggingService类创建一个bean，并使用“loggingService”名称注册它。

        相同的默认命名策略适用于用于创建Spring bean的所有类级注释，如@Component、@Service和@Controller。

    2. 方法级注释

        Spring提供了@Bean和@Qualifier等注释，用于创建bean的方法。

        让我们看看一个例子来了解@Bean注释的默认命名策略：

        ```java
        @Configuration
        public class AuditConfiguration {
            @Bean
            public AuditService audit() {
                return new AuditService();
            }
        }
        ```

        在此配置类中，Spring以“audit”名称注册了AuditService类型的bean，因为当我们在方法上使用@Bean注释时，Spring将方法名称用作bean名称。

        我们还可以在方法上使用@Qualifier注释，我们将在下面看到一个示例。

3. Bean的定制命名

    当我们需要在同一Spring上下文中创建多个相同类型的Bean时，我们可以为Bean指定自定义名称，并使用这些名称来引用它们。

    所以，让我们看看如何给我们的Spring bean起一个自定义名称：

    ```java
    @Component("myBean")
    public class MyCustomComponent {
    }
    ```

    这一次，Spring将创建名为“myBean”的MyCustomComponent类型的bean。

    由于我们明确地为bean命名，Spring将使用此名称，然后可用于引用或访问bean。

    与@Component（“myBean”）类似，我们可以使用其他注释指定名称，如@Service（“myService”）、@Controller（“myController”）和@Bean（“myCustomBean”），然后Spring将用给定名称注册该bean。

4. 用@Bean和@Qualifier命名Bean

    1. 带有值的 @Bean

        如前所述，@Bean 注解应用于方法级别，默认情况下，Spring 将方法名称用作 Bean 名称。

        这个默认 Bean 名称可以被覆盖--我们可以使用 @Bean 注解指定值：

        ```java
        @Configuration
        public class MyConfiguration {
            @Bean("beanComponent")
            public MyCustomComponent myComponent() {
                return new MyCustomComponent();
            }
        }
        ```

        在这种情况下，当我们想要获得MyCustomComponent类型的bean时，我们可以使用“beanComponent”名称来引用此bean。

        Spring @Bean注释通常在配置类方法中声明。它可以通过直接调用它们来引用同类中的其他@Bean方法。

    2. @有价值的限定符

        我们还可以使用@Qualifier注释来命名bean。

        首先，让我们创建一个将由多个类实现的动物界面：

        ```java
        public interface Animal {
            String name();
        }
        ```

        现在，让我们定义一个实现类Cat，并添加具有“cat”值的@Qualifier注释：

        ```java
        @Component
        @Qualifier("cat")
        public class Cat implements Animal {
            @Override
            public String name() {
                return "Cat";
            }
        }
        ```

        让我们添加另一个Animal的实现，并用@Qualifier和值“dog”进行注释：

        ```java
        @Component
        @Qualifier("dog")
        public class Dog implements Animal {
            @Override
            public String name() {
                return "Dog";
            }
        }
        ```

        现在，让我们写一个班级宠物秀，我们可以在其中注入两个不同的动物实例：

        ```java
        @Service
        public class PetShow {
            private final Animal dog;
            private final Animal cat;

            public PetShow (@Qualifier("dog")Animal dog, @Qualifier("cat")Animal cat) { 
            this.dog = dog; 
            this.cat = cat; 
            }
            public Animal getDog() { 
            return dog; 
            }
            public Animal getCat() { 
            return cat; 
            }
        }
        ```

        在PetShow类中，我们通过在构造函数参数上使用@Qualifier注释，在每个注释的值属性中注入了Animal类型的两个实现，并在每个注释的值属性中注入了合格的bean名称。每当我们使用这个限定名称时，Spring都会将具有该限定名称的bean注入目标bean。

5. 验证Bean名称

    到目前为止，我们已经看到了不同的例子来演示给春Bean起名字。现在的问题是，我们如何验证或测试这一点？

    让我们来看看一个单元测试来验证行为：

    ```java
    @ExtendWith(SpringExtension.class)
    public class SpringBeanNamingUnitTest {
        private AnnotationConfigApplicationContext context;

        @BeforeEach
        void setUp() {
            context = new AnnotationConfigApplicationContext();
            context.scan("com.baeldung.springbean.naming");
            context.refresh();
        }
        

        @Test
        void givenMultipleImplementationsOfAnimal_whenFieldIsInjectedWithQualifiedName_thenTheSpecificBeanShouldGetInjected() {
            PetShow petShow = (PetShow) context.getBean("petShow");
            assertThat(petShow.getCat().getClass()).isEqualTo(Cat.class);
            assertThat(petShow.getDog().getClass()).isEqualTo(Dog.class);
        }
    }
    ```

    在此JUnit测试中，我们正在setUp方法中初始化AnnotationConfigApplicationContext，该方法用于获取bean。

    然后，我们只需使用标准断言来验证Spring beans的类。

6. 结论

    在这篇短文中，我们研究了默认和自定义Spring bean命名策略。

    我们还了解了自定义Spring bean命名在我们需要管理相同类型的多个bean的用例中是如何有用的。
