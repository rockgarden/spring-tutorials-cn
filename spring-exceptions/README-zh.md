# Spring Exceptions

This module contains articles about Spring `Exception`s

## NoSuchBeanDefinitionException

1. 概述

    在本教程中，我们将讨论Spring org.springframework.beans.factory.NoSuchBeanDefinitionException。

    这是BeanFactory在试图解析Spring Context中根本没有定义的Bean时抛出的一个常见异常。

    我们将说明这个问题的可能原因和可用的解决方案。

    当然，异常也会在我们最不希望发生的时候发生，所以请看一下[Spring中异常的完整列表和解决方案](https://www.baeldung.com/spring-exceptions)。

    [Spring 异常教程](https://www.baeldung.com/spring-exceptions)

    一些Spring中最常见的异常，并举例说明--为什么会发生，如何快速解决。

    [Spring BeanCreationException](https://www.baeldung.com/spring-beancreationexception)

    处理Spring BeanCreationException的不同原因的快速实用指南。

2. Cause: No Qualifying Bean of Type [] Found for Dependency

    这种异常最常见的原因是试图注入一个没有定义的Bean。

    例如，BeanB在一个合作者BeanA中进行布线：

    ```java
    @Component
    public class BeanA {

        @Autowired
        private BeanB dependency;
        //...
    }
    ```

    现在，如果依赖关系BeanB没有在Spring Context中定义，bootstrap过程就会因为没有这样的Bean定义异常而失败：

    ```log
    org.springframework.beans.factory.NoSuchBeanDefinitionException: 
    No qualifying bean of type [com.baeldung.packageB.BeanB]
    found for dependency: 
    expected at least 1 bean which qualifies as
    autowire candidate for this dependency. 
    Dependency annotations: 
    {@org.springframework.beans.factory.annotation.Autowired(required=true)}
    ```

    Spring明确指出了原因：预计至少有1个Bean符合此依赖关系的autowire候选者的资格。

    BeanB可能不存在于上下文中--如果Bean被classpath扫描自动拾取，并且BeanB被正确注释为Bean（@Component、@Repository、@Service、@Controller等）--的一个原因是它可能被定义在一个不被Spring扫描的包中：

    ```java
    package com.baeldung.packageB;
    @Component
    public class BeanB { ...}
    ```

    而classpath扫描可以按以下方式配置：

    ```java
    @Configuration
    @ComponentScan("com.baeldung.packageA")
    public class ContextWithJavaConfig {
        ...
    }
    ```

    如果Bean没有被自动扫描，而是手动定义，那么BeanB就根本没有在当前的Spring Context中被定义。

3. Cause: Field […] in […] Required a Bean of Type […] That Could Not Be Found

    在上述场景的Spring Boot应用程序中，我们会得到一个不同的消息。

    让我们举一个同样的例子，BeanB在BeanA中被连接，但它没有被定义：

    ```java
    @Component
    public class BeanA {

        @Autowired
        private BeanB dependency;
        //...
    }
    ```

    如果我们尝试运行这个简单的应用程序，它试图加载BeanA：

    ```java
    @SpringBootApplication
    public class NoSuchBeanDefinitionDemoApp {

        public static void main(String[] args) {
            SpringApplication.run(NoSuchBeanDefinitionDemoApp.class, args);
        }
    }
    ```

    应用程序将无法启动，并出现此错误信息：

    ```log
    ***************************
    APPLICATION FAILED TO START
    ***************************

    Description:

    Field dependency in com.baeldung.springbootmvc.nosuchbeandefinitionexception.BeanA required a bean of type 'com.baeldung.springbootmvc.nosuchbeandefinitionexception.BeanB' that could not be found.

    Action:

    Consider defining a bean of type 'com.baeldung.springbootmvc.nosuchbeandefinitionexception.BeanB' in your configuration.
    ```

    这里com.baeldung.springbootmvc.nosuchbeandefinitionexception是BeanA、BeanB和NoSuchBeanDefinitionDemoApp的包。

    这个例子的片段可以在这个[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-mvc)项目中找到。

4. Cause: No Qualifying Bean of Type […] Is Defined

    异常的另一个原因是上下文中存在两个Bean定义，而不是一个。

    比方说，一个接口IBeanB由两个BeanB1和BeanB2实现：

    ```java
    @Component
    public class BeanB1 implements IBeanB {
        //
    }
    @Component
    public class BeanB2 implements IBeanB {
        //
    }
    ```

    现在，如果BeanA自动连接这个接口，Spring将不知道要注入两个实现中的哪一个：

    ```java
    @Component
    public class BeanA {
        @Autowired
        private IBeanB dependency;
        ...
    }
    ```

    同样，这将导致BeanFactory抛出NoSuchBeanDefinitionException：

    ```log
    Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: 
    No qualifying bean of type 
    [com.baeldung.packageB.IBeanB] is defined: 
    expected single matching bean but found 2: beanB1,beanB2
    ```

    同样地，Spring清楚地指出了布线失败的原因：期望单一匹配的Bean但发现了2个。

    然而，请注意，在这种情况下，被抛出的确切异常不是NoSuchBeanDefinitionException，而是一个子类：NoUniqueBeanDefinitionException。这个新的异常是在[Spring 3.2.1](https://jira.springsource.org/browse/SPR-10194)中引入的，正是为了这个原因--区分没有找到Bean定义和在上下文中找到几个定义的原因。

    在这个变化之前，这就是上面的异常：

    ```log
    Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: 
    No qualifying bean of type [com.baeldung.packageB.IBeanB] is defined: 
    expected single matching bean but found 2: beanB1,beanB2
    ```

    解决这个问题的一个办法是使用@Qualifier注解来准确地指定我们要连接的Bean的名字：

    ```java
    @Component
    public class BeanA {

        @Autowired
        @Qualifier("beanB2")
        private IBeanB dependency;
        ...
    }
    ```

    现在Spring有足够的信息来决定注入哪个Bean--BeanB1或BeanB2（BeanB2的默认名称为beanB2）。

5. Cause: No Bean Named […] Is Defined

    当从Spring上下文中按名称请求一个未定义的Bean时，也可能抛出NoSuchBeanDefinitionException：

    ```java
    @Component
    public class BeanA implements InitializingBean {

        @Autowired
        private ApplicationContext context;

        @Override
        public void afterPropertiesSet() {
            context.getBean("someBeanName");
        }
    }
    ```

    在这种情况下，没有 "someBeanName "的Bean定义，导致了以下异常：

    `Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'someBeanName' is defined`

    同样，Spring清楚而简洁地指出了失败的原因： 没有定义名为X的bean。

6. Cause: Proxied Beans

    当上下文中的Bean使用JDK动态代理机制被代理时，代理将不会扩展目标Bean（但它将实现相同的接口）。

    正因为如此，如果Bean是由一个接口注入的，它将被正确地连入。然而，如果Bean是由实际的类注入的，Spring将不会找到与该类相匹配的Bean定义，因为代理实际上并没有扩展该类。

    一个很常见的原因是Spring的事务性支持，即用@Transactional注释的Bean。

    例如，如果ServiceA注入ServiceB，而这两个服务都是事务性的，通过类的定义进行注入是不行的：

    ```java
    @Service
    @Transactional
    public class ServiceA implements IServiceA{

        @Autowired
        private ServiceB serviceB;
        ...
    }

    @Service
    @Transactional
    public class ServiceB implements IServiceB{
        ...
    }
    ```

    同样的两个服务，这次通过接口正确注入，就可以了：

    ```java
    @Service
    @Transactional
    public class ServiceA implements IServiceA{

        @Autowired
        private IServiceB serviceB;
        ...
    }

    @Service
    @Transactional
    public class ServiceB implements IServiceB{
        ...
    }
    ```

7. 总结

    本文讨论了常见的NoSuchBeanDefinitionException的可能原因的例子--重点是如何在实践中解决这些异常。

## Relevant articles

- [Spring BeanCreationException](https://www.baeldung.com/spring-beancreationexception)
- [Spring DataIntegrityViolationException](https://www.baeldung.com/spring-dataIntegrityviolationexception)
- [Spring BeanDefinitionStoreException](https://www.baeldung.com/spring-beandefinitionstoreexception)
- [x] [Spring NoSuchBeanDefinitionException](https://www.baeldung.com/spring-nosuchbeandefinitionexception)
- [Guide to Spring NonTransientDataAccessException](https://www.baeldung.com/nontransientdataaccessexception)
- [Hibernate Mapping Exception – Unknown Entity](https://www.baeldung.com/hibernate-mappingexception-unknown-entity)

## Code

所有这些异常例子的实现都可以在[GitHub项目](https://github.com/eugenp/tutorials/tree/master/spring-exceptions)中找到。这是一个基于Eclipse的项目，所以应该很容易导入并按原样运行。
