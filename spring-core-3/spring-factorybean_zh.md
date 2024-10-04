# [如何使用Spring FactoryBean？](https://www.baeldung.com/spring-factorybean)

1. 概述

    在Spring的Bean容器中，有两种Bean：普通Bean和工厂Bean。Spring直接使用前者，而后者可以自己产生对象，由框架来管理。

    而简单地说，我们可以通过实现org.springframework.beans.factory.FactoryBean接口来构建一个工厂Bean。

2. FactoryBean的基础知识

    1. 实现一个工厂Bean

        让我们先看看FactoryBean接口：

        ```java
        public interface FactoryBean {
            T getObject() throws Exception;
            Class<?> getObjectType();
            boolean isSingleton();
        }
        ```

        我们来讨论一下这三个方法：

        - getObject() - 返回一个由工厂生产的对象，这个对象将被Spring容器使用。
        - getObjectType() - 返回这个FactoryBean产生的对象的类型。
        - isSingleton() - 表示该FactoryBean产生的对象是否是一个单子。

        现在，让我们来实现一个FactoryBean的例子。我们将实现一个ToolFactory，产生Tool类型的对象：

        factorybean/tool.java

        ToolFactory本身：

        factorybean/ToolFactory.java

        我们可以看到，ToolFactory是一个FactoryBean，它可以产生Tool对象。

    2. 用基于XML的配置使用FactoryBean

        现在让我们看一下如何使用我们的ToolFactory。

        我们将开始用基于XML的配置构建一个工具--factorybean-spring-ctx.xml。

        接下来，我们可以测试工具对象是否被正确注入：

        ```java
        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(locations = { "classpath:factorybean-spring-ctx.xml" })
        public class FactoryBeanXmlConfigIntegrationTest {
            @Autowired
            private Tool tool;

            @Test
            public void testConstructWorkerByXml() {
                assertThat(tool.getId(), equalTo(1));
            }
        }
        ```

        测试结果显示，我们设法将ToolFactory生成的工具对象与我们在factorybean-spring-ctx.xml中配置的属性一起注入。

        测试结果还显示，Spring容器使用了FactoryBean产生的对象，而不是自己进行依赖注入。

        尽管Spring容器使用FactoryBean的getObject()方法的返回值作为Bean，但你也可以使用FactoryBean本身。

        要访问FactoryBean，你只需要在Bean的名字前加一个"&"。

        让我们试着获取工厂Bean和它的factoryId属性：

        test/factorybean/FactoryBeanXmlConfigIntegrationTest.java

    3. 在基于Java的配置中使用FactoryBean

        在基于Java的配置中使用FactoryBean与基于XML的配置有些不同，你必须明确调用FactoryBean的getObject()方法。

        让我们把上一小节中的例子转换成基于Java的配置例子：

        ```java
        @Configuration
        public class FactoryBeanAppConfig {
        
            @Bean(name = "tool")
            public ToolFactory toolFactory() {
                ToolFactory factory = new ToolFactory();
                factory.setFactoryId(7070);
                factory.setToolId(2);
                return factory;
            }

            @Bean
            public Tool tool() throws Exception {
                return toolFactory().getObject();
            }
        }
        ```

        然后，我们测试工具对象是否被正确注入：

        test/factorybean/FactoryBeanJavaConfigIntegrationTest.java

        测试结果显示与之前基于XML的配置测试效果相似。

3. 初始化的方法

    有时你需要在FactoryBean被设置后但在getObject()方法被调用前执行一些操作，比如属性检查。

    你可以通过实现InitializingBean接口或使用@PostConstruct注解来实现。

    关于使用这两种解决方案的更多细节已在另一篇文章中介绍过： [Guide To Running Logic on Startup in Spring](https://www.baeldung.com/running-setup-logic-on-startup-in-spring).

4. AbstractFactoryBean

    Spring提供了AbstractFactoryBean作为FactoryBean实现的一个简单模板超类。有了这个基类，我们现在可以更方便地实现一个创建单子或原型对象的工厂Bean。

    让我们来实现一个SingleToolFactory和一个NonSingleToolFactory，以展示如何将AbstractFactoryBean用于单子和原型类型：

    factorybean/AbstractFactoryBean.java

    现在是非单体的实现：

    factorybean/NonSingleToolFactory.java

    还有，这些工厂豆的XML配置：

    factorybean-abstract-spring-ctx.xml

    现在我们可以测试Worker对象的属性是否按照我们的期望被注入：

    test/factorybean/AbstractFactoryBeanIntegrationTestAbstractFactoryBeanTest.java

    从测试中我们可以看到，SingleToolFactory产生了单子对象，而NonSingleToolFactory产生了原型对象。

    注意，在SingleToolFactory中不需要设置singleton属性，因为在AbstractFactory中，singleton属性的默认值是true。

5. 总结

    使用FactoryBean可以很好地封装复杂的构造逻辑，或者让Spring中配置高度可配置的对象变得更容易。

    所以在这篇文章中，我们介绍了如何实现我们的FactoryBean，如何在基于XML的配置和基于Java的配置中使用它，以及FactoryBean的一些其他杂项，如FactoryBean和AbstractFactoryBean的初始化等基本内容。
