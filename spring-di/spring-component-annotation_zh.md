# [Spring @Component注释](https://www.baeldung.com/spring-component-annotation)

1. 一览表

    在本教程中，我们将全面了解Spring @Component注释和相关领域。我们将看到与一些核心Spring功能集成的不同方式，以及如何利用其许多好处。

2. Spring应用上下文

    在我们理解@Component的价值之前，我们首先需要了解一下[SpringApplicationContext](https://www.baeldung.com/spring-application-context)。

    Spring ApplicationContext是Spring持有其识别的自动管理和分发的对象实例的地方。这些被称为Bean。

    Spring的一些主要特点是Bean类管理和依赖注入的机会。

    使用反转控制原理，Spring从我们的应用程序中收集bean实例，并在适当的时候使用它们。我们可以向Spring显示bean依赖项，而无需处理这些对象的设置和实例化。

    使用@Autowired等注释将Spring管理的bean注入我们的应用程序的能力是在Spring中创建强大且可扩展的代码的驱动力。

    那么，我们如何告诉Spring关于我们希望它为我们管理的Bean？我们应该利用Spring的自动Bean检测，在课堂上使用定型注释。

3. @Component

    @Component是一个注释，允许Spring自动检测我们的自定义bean。

    换句话说，无需编写任何显式代码，Spring将：

    - 扫描我们的应用程序，以获取带有@Component注释的类
    - 实例化它们，并向它们注入任何指定的依赖项
    - 在需要的地方注射它们

    然而，大多数开发人员更喜欢使用更专业的定型注释来提供此功能。

    1. Spring定型注释

        Spring提供了一些专门的定型注释：@Controller、@Service和@Repository。它们都提供与@Component相同的功能。

        它们的行为都是一样的，因为它们都是由@Component作为每个元注释的组合注释。它们就像@Component别名，在Spring自动检测或依赖注入之外具有专门的用途和含义。

        理论上，如果我们愿意，我们可以专门使用@Component来满足我们的bean自动检测需求。另一方面，我们还可以使用@Component[编写专业注释](https://www.baeldung.com/java-custom-annotation)。

        然而，Spring的其他领域专门寻找Spring的专业注释，以提供额外的自动化优势。因此，我们可能大部分时间都应该坚持使用既定的专业。

        假设我们在Spring Boot项目中都有这些案例的示例：

        ```java
        @Controller
        public class ControllerExample {
        }

        @Service
        public class ServiceExample {
        }

        @Repository
        public class RepositoryExample {
        }

        @Component
        public class ComponentExample {
        }

        @Target({ElementType.TYPE})
        @Retention(RetentionPolicy.RUNTIME)
        @Component
        public @interface CustomComponent {
        }

        @CustomComponent
        public class CustomComponentExample {
        }
        ```

        我们可以编写一个测试，证明每个测试都由Spring自动检测，并添加到ApplicationContext中：

        ```java
        @SpringBootTest
        @ExtendWith(SpringExtension.class)
        public class ComponentUnitTest {

            @Autowired
            private ApplicationContext applicationContext;

            @Test
            public void givenInScopeComponents_whenSearchingInApplicationContext_thenFindThem() {
                assertNotNull(applicationContext.getBean(ControllerExample.class));
                assertNotNull(applicationContext.getBean(ServiceExample.class));
                assertNotNull(applicationContext.getBean(RepositoryExample.class));
                assertNotNull(applicationContext.getBean(ComponentExample.class));
                assertNotNull(applicationContext.getBean(CustomComponentExample.class));
            }
        }
        ```

    2. @ComponentScan

        在我们完全依赖@Component之前，我们必须明白这只是一个简单的注释。注释的目的是将beans与其他对象（如域对象）区分。

        然而，Spring使用@ComponentScan注释将它们收集到其ApplicationContext中。

        如果我们正在编写Spring Boot应用程序，了解@SpringBootApplication是一个包含@ComponentScan的复合注释是有帮助的。只要我们的@SpringBootApplication类位于我们项目的根部，它就会扫描我们默认定义的每个@Component。

        但是，如果我们的@SpringBootApplication类不能位于项目的根部，或者我们想扫描外部源，我们可以明确[配置](https://www.baeldung.com/spring-component-scanning#component-scan)@ComponentScan来查看我们指定的任何软件包，只要它存在于类路径上。

        让我们定义一个范围外的@Component bean：

        ```java
        package com.baeldung.component.scannedscope;
        @Component
        public class ScannedScopeExample {}
        ```

        接下来，我们可以通过明确的指令将其包含到我们的@ComponentScan注释中：

        ```java
        package com.baeldung.component.inscope;

        @SpringBootApplication
        @ComponentScan({"com.baeldung.component.inscope", "com.baeldung.component.scannedscope"})
        public class ComponentApplication {
            //public static void main(String[] args) {...}
        }
        ```

        最后，我们可以测试它是否存在：

        ```java
        @Test
        public void givenScannedScopeComponent_whenSearchingInApplicationContext_thenFindIt() {
            assertNotNull(applicationContext.getBean(ScannedScopeExample.class));
        }
        ```

        当我们想要扫描项目中包含的外部依赖项时，这种情况更有可能发生。

    3. @Component限制

        在某些情况下，当我们无法使用@Component时，我们希望特定对象成为Spring管理的bean。

        让我们在项目外的软件包中定义一个带有@Component注释的对象：

        ```java
        package com.baeldung.component.outsidescope;

        @Component
        public class OutsideScopeExample {}
        ```

        这是一个测试，证明ApplicationContext不包含外部组件：

        ```java
        @Test
        public void givenOutsideScopeComponent_whenSearchingInApplicationContext_thenFail() {
            assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(OutsideScopeExample.class));
        }
        ```

        此外，我们可能无法访问源代码，因为它来自第三方来源，并且我们无法添加@Component注释。或者，也许我们想有条件地使用一个bean实现而不是另一个bean实现，这取决于我们运行的环境。自动检测通常就足够了，但当没有时，我们可以使用@Bean。

4. @Component vs @Bean

    @Bean也是Spring在运行时用来收集beans的注释，但在类级别不使用。相反，我们用@Bean注释方法，以便Spring可以将方法的结果存储为Spring bean。

    我们将首先创建一个没有注释的POJO：

    ```java
    public class BeanExample {}
    ```

    在我们的用@Configuration注释的类中，我们可以创建一个bean生成方法：

    ```java
    @Bean
    public BeanExample beanExample() {
        return new BeanExample();
    }
    ```

    BeanExample可能代表一个本地类，也可能是一个外部类。没关系，因为我们需要返回它的实例。

    然后，我们可以编写一个测试来验证Spring是否拿起了Bean：

    ```java
    @Test
    public void givenBeanComponents_whenSearchingInApplicationContext_thenFindThem() {
        assertNotNull(applicationContext.getBean(BeanExample.class));
    }
    ```

    由于@Component和@Bean之间的差异，我们应该注意一些重要含义。

    - @Component 是类级别的注解，而 @Bean 则是方法级别的注解，因此 @Component 只有在类的源代码可以编辑时才可以使用。@Bean 可以一直使用，但它比较啰嗦。
    - @Component 与 Spring 的自动检测兼容，但 @Bean 需要手动实例化类。
    - 使用 @Bean 可以将 bean 的实例化与其类定义分离。因此，我们可以使用它将第三方类转化为 Spring Bean。这也意味着我们可以引入逻辑来决定使用 bean 的几种可能实例选项中的哪一种。

5. 结论

    我们刚刚探讨了 Spring @Component 注解和其他相关主题。首先，我们讨论了各种 Spring 定型注解，它们只是 @Component 的专门版本。

    然后我们了解到，除非 @ComponentScan 能找到它，否则 @Component 不会做任何事情。

    最后，由于没有源代码，我们无法在类上使用 @Component，因此我们学习了如何使用 @Bean 注解来代替它。
