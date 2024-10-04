# [将原型Bean注入Spring中的单例中](https://www.baeldung.com/spring-inject-prototype-bean-into-singleton)

1. 概述

    在这篇快速文章中，我们将展示将原型Bean注入单子实例的不同方法。我们将讨论每个方案的使用情况和优劣势。

    默认情况下，Spring Bean是单子。当我们试图连接不同作用域的Bean时，问题就来了。例如，一个原型Bean变成一个单子。这就是所谓的作用域Bean注入问题。

2. 原型Bean注入问题

    为了描述这个问题，我们来配置以下Bean：

    ```java
    @Configuration
    public class AppConfig {

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public PrototypeBean prototypeBean() {
            return new PrototypeBean();
        }

        @Bean
        public SingletonBean singletonBean() {
            return new SingletonBean();
        }
    }
    ```

    请注意，第一个Bean有一个原型作用域，另一个是一个单例。

    现在，让我们将原型作用域的Bean注入到单子中--然后通过getPrototypeBean()方法公开它：

    ```java
    public class SingletonBean {

        // ..

        @Autowired
        private PrototypeBean prototypeBean;

        public SingletonBean() {
            logger.info("Singleton instance created");
        }

        public PrototypeBean getPrototypeBean() {
            logger.info(String.valueOf(LocalTime.now()));
            return prototypeBean;
        }
    }
    ```

    然后，让我们加载ApplicationContext并获得两次单例Bean：

    ```java
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context 
        = new AnnotationConfigApplicationContext(AppConfig.class);
        
        SingletonBean firstSingleton = context.getBean(SingletonBean.class);
        PrototypeBean firstPrototype = firstSingleton.getPrototypeBean();
        
        // get singleton bean instance one more time
        SingletonBean secondSingleton = context.getBean(SingletonBean.class);
        PrototypeBean secondPrototype = secondSingleton.getPrototypeBean();

        isTrue(firstPrototype.equals(secondPrototype), "The same instance should be returned");
    }
    ```

    下面是控制台的输出：

    ```log
    Singleton Bean created
    Prototype Bean created
    11:06:57.894
    // should create another prototype bean instance here
    11:06:58.895
    ```

    这两个Bean只被初始化了一次，在应用程序上下文的启动阶段。

3. 注入ApplicationContext

    我们也可以直接将ApplicationContext注入到Bean中。

    要实现这一点，要么使用@Autowire注解，要么实现ApplicationContextAware接口：

    ```java
    public class SingletonAppContextBean implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        public PrototypeBean getPrototypeBean() {
            return applicationContext.getBean(PrototypeBean.class);
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) 
        throws BeansException {
            this.applicationContext = applicationContext;
        }
    }
    ```

    每次调用getPrototypeBean()方法，PrototypeBean的一个新实例将从ApplicationContext返回。

    然而，这种方法有严重的缺点。它与控制倒置的原则相矛盾，因为我们直接从容器中请求依赖关系。

    另外，我们在SingletonAppcontextBean类中从applicationContext中获取原型Bean。这意味着将代码与Spring框架耦合起来。

4. 方法注入

    解决这个问题的另一个方法是用@Lookup注解进行方法注入：

    ```java
    @Component
    public class SingletonLookupBean {

        @Lookup
        public PrototypeBean getPrototypeBean() {
            return null;
        }
    }
    ```

    Spring将重写带有@Lookup注释的getPrototypeBean()方法。然后它将Bean注册到应用上下文中。每当我们请求getPrototypeBean()方法时，它会返回一个新的PrototypeBean实例。

    它将使用CGLIB来生成负责从应用程序上下文中获取PrototypeBean的字节码。

5. javax.inject API

    该设置以及所需的依赖性在这篇[Spring wiring](/spring-di-2/README-zh.md#wiring-in-spring-autowired-resource-and-inject)文章中有所描述。

    下面是Singleton Bean：

    ```java
    public class SingletonProviderBean {

        @Autowired
        private Provider<PrototypeBean> myPrototypeBeanProvider;

        public PrototypeBean getPrototypeInstance() {
            return myPrototypeBeanProvider.get();
        }
    }
    ```

    我们使用Provider接口来注入原型Bean。对于每个getPrototypeInstance()方法的调用，myPrototypeBeanProvider.get()方法会返回一个新的PrototypeBean实例。

6. 范围代理

    默认情况下，Spring持有一个对真实对象的引用来执行注入。在这里，我们创建一个代理对象来连接真实对象和依赖对象。

    每次调用代理对象上的方法时，代理自己决定是创建一个新的真实对象的实例还是重新使用现有的实例。

    为了设置这一点，我们修改Appconfig类，添加一个新的@Scope注解：

    ```java
    @Scope(
        value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, 
        proxyMode = ScopedProxyMode.TARGET_CLASS)
    ```

    默认情况下，Spring使用CGLIB库来直接子类化对象。为了避免使用CGLIB，我们可以用ScopedProxyMode.INTERFACES配置代理模式，以使用JDK动态代理来代替。

7. ObjectFactory接口

    Spring提供了[`ObjectFactory<T>`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/ObjectFactoryCreatingFactoryBean.html)接口来按需生产给定类型的对象：

    ```java
    public class SingletonObjectFactoryBean {

        @Autowired
        private ObjectFactory<PrototypeBean> prototypeBeanObjectFactory;

        public PrototypeBean getPrototypeInstance() {
            return prototypeBeanObjectFactory.getObject();
        }
    }
    ```

    让我们来看看getPrototypeInstance()方法；getObject()为每个请求返回一个全新的PrototypeBean实例。在这里，我们对原型的初始化有更多的控制。

    另外，ObjectFactory是框架的一部分；这意味着为了使用这个选项，可以避免额外的设置。

8. 使用java.util.Function在运行时创建一个bean

    另一个选项是在运行时创建原型Bean实例，这也允许我们向实例添加参数。

    为了看看这个例子，让我们给我们的PrototypeBean类添加一个名字字段：

    ```java
    public class PrototypeBean {
        private String name;
        
        public PrototypeBean(String name) {
            this.name = name;
            logger.info("Prototype instance " + name + " created");
        }

        //...   
    }
    ```

    接下来，我们将通过使用java.util.Function接口，将一个Bean工厂注入到我们的Singleton Bean中：

    ```java
    public class SingletonFunctionBean {
        
        @Autowired
        private Function<String, PrototypeBean> beanFactory;
        
        public PrototypeBean getPrototypeInstance(String name) {
            PrototypeBean bean = beanFactory.apply(name);
            return bean;
        }

    }
    ```

    最后，我们必须在我们的配置中定义工厂bean、原型bean和单例baen：

    ```java
    @Configuration
    public class AppConfig {
        @Bean
        public Function<String, PrototypeBean> beanFactory() {
            return name -> prototypeBeanWithParam(name);
        } 

        @Bean
        @Scope(value = "prototype")
        public PrototypeBean prototypeBeanWithParam(String name) {
        return new PrototypeBean(name);
        }
        
        @Bean
        public SingletonFunctionBean singletonFunctionBean() {
            return new SingletonFunctionBean();
        }
        //...
    }
    ```

9. 测试

    现在让我们写一个简单的JUnit测试，用ObjectFactory接口来锻炼这个案例：

    ```java
    @Test
    public void givenPrototypeInjection_WhenObjectFactory_ThenNewInstanceReturn() {

        AbstractApplicationContext context
        = new AnnotationConfigApplicationContext(AppConfig.class);

        SingletonObjectFactoryBean firstContext
        = context.getBean(SingletonObjectFactoryBean.class);
        SingletonObjectFactoryBean secondContext
        = context.getBean(SingletonObjectFactoryBean.class);

        PrototypeBean firstInstance = firstContext.getPrototypeInstance();
        PrototypeBean secondInstance = secondContext.getPrototypeInstance();

        assertTrue("New instance expected", firstInstance != secondInstance);
    }
    ```

    成功启动测试后，我们可以看到，每次调用getPrototypeInstance()方法时，都会创建一个新的原型Bean实例。

10. 总结

    在这个简短的教程中，我们学到了几种将原型Bean注入单子实例的方法。
