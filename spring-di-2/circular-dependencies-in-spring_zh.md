# [Spring中的循环依赖关系](https://www.baeldung.com/circular-dependencies-in-spring)

1. 什么是循环依赖？

    当一个Bean A依赖于另一个Bean B，而Bean B也依赖于Bean A的时候，就会出现循环依赖关系：

    Bean A → Bean B → Bean A

    当然，我们可以有更多的豆子被暗示：

    Bean A → Bean B → Bean C → Bean D → Bean E → Bean A

    进一步阅读：

    [Guice vs Spring - 依赖注入](https://www.baeldung.com/guice-spring-dependency-injection)

    了解Guice和Spring在依赖注入方面的异同。

    [将Spring Bean注入到非托管对象中](https://www.baeldung.com/spring-inject-bean-into-unmanaged-objects)

    了解如何将Spring Bean注入到普通对象中。

2. 在Spring中发生了什么

    当Spring上下文加载所有的Bean时，它试图按照它们完全工作所需的顺序来创建Bean。

    假设我们没有一个循环依赖关系。我们的情况是这样的：

    Bean A → Bean B → Bean C

    Spring将创建Bean C，然后创建Bean B（并将Bean C注入其中），再创建Bean A（并将Bean B注入其中）。

    但是在循环依赖的情况下，Spring无法决定哪一个Bean应该先被创建，因为它们是相互依赖的。在这种情况下，Spring会在加载上下文时引发BeanCurrentlyInCreationException。

    在Spring中，当使用构造函数注入时就会发生这种情况。如果我们使用其他类型的注入，我们不应该有这样的问题，因为依赖关系将在需要时被注入，而不是在上下文加载时。

3. 一个快速的例子

    让我们定义两个相互依赖的Bean（通过构造函数注入）：

    ```java
    @Component
    public class CircularDependencyA {

        private CircularDependencyB circB;

        @Autowired
        public CircularDependencyA(CircularDependencyB circB) {
            this.circB = circB;
        }
    }

    @Component
    public class CircularDependencyB {

        private CircularDependencyA circA;

        @Autowired
        public CircularDependencyB(CircularDependencyA circA) {
            this.circA = circA;
        }
    }
    ```

    现在我们可以为测试编写一个配置类（我们称之为TestConfig），指定扫描组件的基础包。

    让我们假设我们的Bean被定义在包 "com.baeldung.circulardependency" 中：

    test/circulardependency/TestConfig.java

    最后，我们可以写一个JUnit测试来检查循环依赖性。

    测试可以是空的，因为循环依赖将在上下文加载时被检测到：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = { TestConfig.class })
    public class CircularDependencyIntegrationTest {

        @Test
        public void givenCircularDependency_whenConstructorInjection_thenItFails() {
            // Empty test; we just want the context to load
        }
    }
    ```

    如果我们尝试运行这个测试，我们会得到这个异常：

    ```log
    BeanCurrentlyInCreationException: Error creating bean with name 'circularDependencyA':
    Requested bean is currently in creation: Is there an unresolvable circular reference?
    ```

4. 变通方法

    现在我们将展示一些最流行的处理这个问题的方法。

    1. 重新设计

        当我们有一个循环的依赖关系时，很可能我们有一个设计问题，责任没有被很好地分开。我们应该尝试适当地重新设计组件，使其层次结构设计合理，没有必要出现循环依赖。

        然而，有很多可能的原因，我们可能无法进行重新设计，例如遗留代码、已经测试过的代码无法修改、没有足够的时间或资源进行完整的重新设计等等。如果我们不能重新设计组件，我们可以尝试一些变通方法。

    2. 使用@Lazy

        打破这个循环的一个简单方法是告诉Spring懒散地初始化其中一个Bean。因此，它不会完全初始化Bean，而是创建一个代理，将其注入另一个Bean中。注入的Bean只有在第一次需要时才会被完全创建。

        为了在我们的代码中尝试这一点，我们可以改变CircularDependencyA：

        ```java
        @Component
        public class CircularDependencyA {

            private CircularDependencyB circB;

            @Autowired
            public CircularDependencyA(@Lazy CircularDependencyB circB) {
                this.circB = circB;
            }
        }

        如果我们现在运行这个测试，我们会看到这次错误没有发生。

    3. 使用Setter/Field注入

        最流行的解决方法之一，也是Spring文档所建议的，就是使用setter注入。

        简单地说，我们可以通过改变Bean的连接方式来解决这个问题--使用setter注入（或字段注入），而不是构造函数注入。这样一来，Spring就会创建Bean，但在需要时才会注入依赖关系。

        因此，让我们改变我们的类，使用setter注入，并在CircularDependencyB中添加另一个字段（消息），这样我们就可以做一个适当的单元测试：

        ```java
        @Component
        public class CircularDependencyA {

            private CircularDependencyB circB;

            @Autowired
            public void setCircB(CircularDependencyB circB) {
                this.circB = circB;
            }

            public CircularDependencyB getCircB() {
                return circB;
            }
        }

        @Component
        public class CircularDependencyB {

            private CircularDependencyA circA;

            private String message = "Hi!";

            @Autowired
            public void setCircA(CircularDependencyA circA) {
                this.circA = circA;
            }

            public String getMessage() {
                return message;
            }
        }
        ```

        现在我们要对我们的单元测试做一些修改：

        test/circulardependency/CircularDependencyIntegrationTest.java

        让我们仔细看一下这些注解。

        @Bean告诉Spring框架，这些方法必须被用来检索要注入的Bean的实现。

        通过@Test注解，测试将从上下文中获得CircularDependencyA Bean，并断言其CircularDependencyB已被正确注入，检查其消息属性的值。

    4. 使用@PostConstruct

        另一种打破循环的方法是在其中一个Bean上使用@Autowired注入一个依赖关系，然后使用@PostConstruct注释的方法来设置另一个依赖关系。

        我们的Bean可以有这样的代码：

        ```java
        @Component
        public class CircularDependencyA {

            @Autowired
            private CircularDependencyB circB;

            @PostConstruct
            public void init() {
                circB.setCircA(this);
            }

            public CircularDependencyB getCircB() {
                return circB;
            }
        }

        @Component
        public class CircularDependencyB {

            private CircularDependencyA circA;

            private String message = "Hi!";

            public void setCircA(CircularDependencyA circA) {
                this.circA = circA;
            }

            public String getMessage() {
                return message;
            }
        }
        ```

        而且我们可以运行之前的测试，所以我们检查循环依赖异常仍然没有被抛出，而且依赖被正确注入。

    5. 实现ApplicationContextAware和InitializingBean

        如果其中一个Bean实现了ApplicationContextAware，那么这个Bean就可以访问Spring上下文，并且可以从那里提取其他Bean。

        通过实现InitializingBean，我们表明这个Bean在其所有属性被设置后必须做一些动作。在这种情况下，我们要手动设置我们的依赖关系。

        下面是我们的Bean的代码：

        - circulardependency/CircularDependencyA.java
        - circulardependency/CircularDependencyB.java

        再一次，我们可以运行之前的测试，看到异常没有被抛出，测试按预期进行。

5. 总结

    在Spring中，有很多方法来处理循环依赖关系。

    我们首先应该考虑重新设计我们的Bean，这样就不需要循环依赖关系了。这是因为循环依赖通常是一种可以改进的设计的症状。

    但如果我们的项目绝对需要循环依赖，我们可以按照这里建议的一些变通方法。

    首选的方法是使用setter injections。但也有其他的替代方法，一般是基于停止Spring管理Bean的初始化和注入，以及使用不同的策略自己完成。
