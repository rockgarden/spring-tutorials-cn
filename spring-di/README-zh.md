# Spring Dependency Injection

This module contains articles about dependency injection with Spring

- [The Spring @Qualifier Annotation](https://www.baeldung.com/spring-qualifier-annotation)
- [泛型的Autowiring](#泛型的autowiring)
- [Guice vs Spring – Dependency Injection](https://www.baeldung.com/guice-spring-dependency-injection)
- [将原型Bean注入Spring中的单例中](#将原型bean注入spring中的单例中)
- [Controlling Bean Creation Order with @DependsOn Annotation](https://www.baeldung.com/spring-depends-on)
- [Unsatisfied Dependency in Spring](#unsatisfied-dependency-in-spring)
- [Spring中基于XML的注入](#spring中基于xml的注入)

## 泛型的Autowiring

1. 概述

    在本教程中，我们将看到如何通过[泛型](https://www.baeldung.com/java-generics)注入Spring Bean。

2. 在Spring 3.2中自动连接泛型。

    Spring从3.2版本开始支持泛型的注入。

    假设我们有一个名为Vehicle的抽象类和一个名为Car的具体子类：

    - dependencyinjectiontypes.model/Vehicle.java
    - dependencyinjectiontypes.model/Car.java

    假设我们想把一个Vehicle类型的对象列表注入某个处理程序类中：

    ```java
    @Autowired
    private List<Vehicle> vehicles;
    ```

    Spring会将所有的Vehicle实例Bean自动连接到这个列表中。我们如何通过Java或XML配置来实例化这些Bean并不重要。

    我们也可以使用限定词来只获取Vehicle类型的特定Bean。然后我们创建@CarQualifier，并用@Qualifier来注解它：

    dependencyinjectiontypes.annotation/CarQualifier.java

    现在我们可以在我们的列表中使用这个注解，只获得一些特定的车辆：

    ```java
    @Autowired
    @CarQualifier
    private List<Vehicle> vehicles;
    ```

    在这种情况下，我们可以创建几个Vehicle Bean，但Spring将只把那些带有@CarQualifier的Vehicle注入到上面的列表中：

    ```java
    public class CustomConfiguration {
        @Bean
        @CarQualifier
        public Car getMercedes() {
            return new Car("E280", "Mercedes", "Diesel");
        }
    }
    ```

3. Spring 4.0中的Autowiring泛型。

    假设我们有另一个名为Motorcycle的Vehicle子类：

    dependencyinjectiontypes.model/Motorcycle.java

    现在，如果我们只想把汽车Bean注入到我们的列表中，而不是摩托车Bean，我们可以通过使用特定的子类作为类型参数来做到这一点：

    ```java
    @Autowired
    private List<Car> vehicles;
    ```

    自4.0版本以来，Spring允许我们使用通用类型作为限定符，而不需要显式注解。

    在Spring 4.0之前，上面的代码无法与Vehicle的多个子类的Bean一起工作。如果没有明确的限定符，我们会收到一个NonUniqueBeanDefinitionException。

4. ResolvableType

    泛型自动连接功能在幕后借助于ResolvableType类来工作。

    它是在Spring 4.0中引入的，用于封装Java类型，处理对超类型、接口、泛型参数的访问，并最终解析为一个类：

    ```java
    ResolvableType vehiclesType = ResolvableType.forField(getClass().getDeclaredField("vehicles"));
    System.out.println(vehiclesType);

    ResolvableType type = vehiclesType.getGeneric();
    System.out.println(type);

    Class<?> aClass = type.resolve();
    System.out.println(aClass);
    ```

    上述代码的输出将显示相应的简单和通用类型：

    ```log
    java.util.List<com.example.model.Vehicle>
    com.example.model.Vehicle
    class com.example.model.Vehicle
    ```

5. 总结

    通用类型的注入是一个强大的功能，它节省了开发人员分配显式限定符的努力，使代码更干净，更容易理解。

## 将原型Bean注入Spring中的单例中

1. 概述

    在这篇快速文章中，我们将展示将原型Bean注入单子实例的不同方法。我们将讨论每个方案的使用情况和优劣势。

    默认情况下，Spring Bean是单子。当我们试图连接不同作用域的Bean时，问题就来了。例如，一个原型Bean变成一个单子。这就是所谓的作用域Bean注入问题。

    要了解更多关于Bean Scopes的信息，[这篇文章](https://www.baeldung.com/spring-bean-scopes)是一个好的开始。

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

## Unsatisfied Dependency in Spring

1. 概述

    在这个快速教程中，我们将解释Spring的UnsatisfiedDependencyException，它的原因以及如何避免它。

2. 不满意的依赖性Exception的原因

    顾名思义，当某些Bean或属性的依赖性没有得到满足时，就会抛出UnsatisfiedDependencyException。

    这可能发生在Spring应用程序试图连接一个Bean而无法解决其中一个强制性的依赖关系时。

3. 应用实例

    假设我们有一个服务类PurchaseDeptService，它依赖于InventoryRepository：

    ```java
    @Service
    public class PurchaseDeptService {
        public PurchaseDeptService(InventoryRepository repository) {
            this.repository = repository;
        }
    }

    public interface InventoryRepository {
    }

    @Repository
    public class ShoeRepository implements InventoryRepository {
    }

    @SpringBootApplication
    public class SpringDependenciesExampleApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringDependenciesExampleApplication.class, args);
        }
    }
    ```

    现在，我们假设所有这些类都位于同一个名为com.baeldung.dependency.exception.app的包中。

    当我们运行这个Spring Boot应用程序时，一切正常。

    让我们看看如果我们跳过一个配置步骤，会遇到什么样的问题。

4. 组件注解缺失

    现在让我们从ShoeRepository类中删除@Repository注解：

    `public class ShoeRepository implements InventoryRepository {}`

    当我们再次启动我们的应用程序时，我们会看到以下错误信息： `UnsatisfiedDependencyException: Error creating bean with name ‘purchaseDeptService': Unsatisfied dependency expressed through constructor parameter 0`

    Spring没有被指示连接一个ShoeRepository Bean并将其添加到应用程序上下文中，所以它无法注入它并抛出了这个异常。

    在ShoeRepository上添加@Repository注解可以解决这个问题。

5. 包未被扫描

    现在让我们把我们的ShoeRepository（连同InventoryRepository）放入一个单独的包，名为com.baeldung.dependency.exception.repository。

    再一次，当我们运行我们的应用程序时，它抛出了UnsatisfiedDependencyException。

    为了解决这个问题，我们可以在父包上配置包扫描，并确保所有相关的类都包括在内：

    ```java
    @SpringBootApplication
    @ComponentScan(basePackages = {"com.baeldung.dependency.exception"})
    public class SpringDependenciesExampleApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringDependenciesExampleApplication.class, args);
        }
    }
    ```

6. 非唯一性的依赖解决

    假设我们添加另一个InventoryRepository的实现--DressRepository：

    ```java
    @Repository
    public class DressRepository implements InventoryRepository {
    }
    ```

    现在当我们运行我们的应用程序时，它将再次抛出UnsatisfiedDependencyException。

    然而，这次的情况有所不同。正如它所发生的，当有一个以上的bean满足它的时候，依赖性就不能被解决。

    为了解决这个问题，我们可能要添加@Qualifier来区分存储库：

    ```java
    @Qualifier("dresses")
    @Repository
    public class DressRepository implements InventoryRepository {
    }

    @Qualifier("shoes")
    @Repository
    public class ShoeRepository implements InventoryRepository {
    }
    ```

    另外，我们要给PurchaseDeptService构造函数依赖性添加一个限定词：

    ```java
    public PurchaseDeptService(@Qualifier("dresses") InventoryRepository repository) {
        this.repository = repository;
    }
    ```

    这将使DressRepository成为唯一可行的选择，Spring将把它注入PurchaseDeptService中。

7. 结语

    在这篇文章中，我们看到了遇到[UnsatisfiedDependencyException](https://www.baeldung.com/spring-beancreationexception)的几种最常见的情况，然后我们学会了如何解决这些问题。

    我们也有一个关于Spring BeanCreationException的更一般的教程。

## Spring中基于XML的注入

1. 简介

    在这个基础教程中，我们将学习如何用Spring框架进行简单的基于XML的bean配置。

2. 概述

    让我们先在pom.xml中添加Spring的库依赖性。

    `<groupId>org.springframework</groupId><artifactId>spring-context</artifactId>`

    最新版本的Spring依赖性可以在这里找到。

3. 依赖性注入--概述

    [依赖性注入](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)是一种技术，对象的依赖性由外部容器提供。

    比方说，我们有一个应用类，它依赖于一个实际处理业务逻辑的服务。

    参见 di.spring/IndexApp.java

    现在我们说IService是一个接口。

    参见 di.spring/IService.java

    这个接口可以有多种实现。

    让我们快速看一下一个潜在的实现。

    参见 di.spring/IndexService.java

    在这里，IndexApp 是一个高层组件，它依赖于名为 IService 的低层组件。

    实质上，我们正在将IndexApp与IService的特定实现解耦，IService可以根据各种因素而变化。

4. 依赖性注入--在行动中

    让我们看看我们如何注入一个依赖关系。

    1. 使用属性

        让我们看看如何使用基于XML的配置将依赖关系连接起来。

        ```xml
        <bean 
        id="indexService" 
        class="com.baeldung.di.spring.IndexService" />
            
        <bean 
        id="indexApp" 
        class="com.baeldung.di.spring.IndexApp" >
            <property name="service" ref="indexService" />
        </bean>    
        ```

        可以看出，我们正在创建一个IndexService的实例，并给它分配了一个id。默认情况下，这个bean是一个单例。

        此外，我们还创建了一个IndexApp的实例；在这个Bean中，我们使用setter方法注入其他Bean。

    2. 使用构造器

        我们可以使用构造函数来注入依赖关系，而不是通过setter方法来注入Bean。

        ```xml
        <bean 
        id="indexApp" 
        class="com.baeldung.di.spring.IndexApp">
            <constructor-arg ref="indexService" />
        </bean>    
        ```

    3. 使用静态工厂

        我们也可以注入一个由工厂返回的bean。让我们创建一个简单的工厂，根据提供的数字返回一个IService的实例。

        参见 di.spring/StaticServiceFactory.java

        现在让我们看看如何使用上述实现，使用基于XML的配置将一个bean注入到IndexApp中。

        ```xml
        <bean id="messageService"
        class="com.baeldung.di.spring.StaticServiceFactory"
        factory-method="getService">
            <constructor-arg value="1" />
        </bean>   

        <bean id="indexApp" class="com.baeldung.di.spring.IndexApp">
            <property name="service" ref="messageService" />
        </bean>
        ```

        在上面的例子中，我们使用工厂方法调用静态的getService方法来创建一个id为messageService的bean，并注入到IndexApp中。

    4. 使用工厂方法

        让我们考虑一个实例工厂，根据提供的数字返回一个IService的实例。这一次，这个方法不是静态的。

        参见 di.spring/InstanceServiceFactory.java

        现在让我们看看如何使用上述实现，用 XML 配置将一个 Bean 注入 IndexApp。

        ```xml
        <bean id="indexServiceFactory" 
        class="com.baeldung.di.spring.InstanceServiceFactory" />
        <bean id="messageService"
        class="com.baeldung.di.spring.InstanceServiceFactory"
        factory-method="getService" factory-bean="indexServiceFactory">
            <constructor-arg value="1" />
        </bean>  
        <bean id="indexApp" class="com.baeldung.di.spring.IndexApp">
            <property name="service" ref="messageService" />
        </bean>
        ```

        在上面的例子中，我们在InstanceServiceFactory的一个实例上使用factory-method调用getService方法来创建一个id为messageService的bean，并将其注入IndexApp中。

5. 测试

    这就是我们如何访问配置好的bean。

    ```java
    @Test
    public void whenGetBeans_returnsBean() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("...");
        IndexApp indexApp = applicationContext.getBean("indexApp", IndexApp.class);
        assertNotNull(indexApp);
    }
    ```

6. 总结

    在这个快速教程中，我们举例说明了如何使用Spring框架的基于XML的配置来注入依赖关系。

## Relevant Articles

- [The Spring @Qualifier Annotation](https://www.baeldung.com/spring-qualifier-annotation)
- [x] [Spring Autowiring of Generic Types](https://www.baeldung.com/spring-autowire-generics)
- [Guice vs Spring – Dependency Injection](https://www.baeldung.com/guice-spring-dependency-injection)
- [x] [Injecting Prototype Beans into a Singleton Instance in Spring](https://www.baeldung.com/spring-inject-prototype-bean-into-singleton)
- [Controlling Bean Creation Order with @DependsOn Annotation](https://www.baeldung.com/spring-depends-on)
- [x] [Unsatisfied Dependency in Spring](https://www.baeldung.com/spring-unsatisfied-dependency)
- [x] [XML-Based Injection in Spring](https://www.baeldung.com/spring-xml-injection)
- More articles: [[next -->]](../spring-di-2/README-zh.md)

## Code

这些例子的实现可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-di)项目中找到--这是一个基于Maven的项目，所以应该很容易导入并按原样运行。
