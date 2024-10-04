# [Spring基于XML的注入](https://www.baeldung.com/spring-xml-injection)

1. 介绍

    在本基本教程中，我们将学习如何使用Spring Framework进行基于XML的简单bean配置。

2. 依赖注入-概述

    依赖注入是一种由外部容器提供对象依赖的技术。

    假设我们有一个应用程序类，它依赖于实际处理业务逻辑的服务：

    ```java
    public class IndexApp {
        private IService service;
        // standard constructors/getters/setters
    }
    ```

    现在假设IService是一个接口：

    ```java
    public interface IService {
        public String serve();
    }
    ```

    此接口可以有多个实现。

    让我们快速看一下一个潜在的实现：

    ```java
    public class IndexService implements IService {
        @Override
        public String serve() {
            return "Hello World";
        }
    }
    ```

    在这里，IndexApp是一个高级组件，依赖于名为IService的低级组件。

    从本质上讲，我们正在将IndexApp与IService的特定实现脱钩，IService可能会根据各种因素而有所不同。

3. 依赖注入-在行动中

    让我们看看如何注入依赖性。

    1. 使用属性

        让我们看看如何使用基于XML的配置将依赖项连接在一起：

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

        可以看出，我们正在创建一个IndexService的实例，并为其分配一个ID。默认情况下，Bean是单例。此外，我们正在创建一个IndexApp的实例。

        在这个Bean里，我们用设置器方法注入另一个Bean。

    2. 使用构造函数

        我们可以使用构造函数注入依赖项，而不是通过设置器方法注入bean：

        ```xml
        <bean
        id="indexApp" 
        class="com.baeldung.di.spring.IndexApp">
            <constructor-arg ref="indexService" />
        </bean>
        ```

    3. 使用静态工厂

        我们还可以注射工厂退回的Bean。让我们创建一个简单的工厂，根据提供的数字返回IService的实例：

        ```java
        public class StaticServiceFactory {
            public static IService getNumber(int number) {
                // ...
            }
        }
        ```

        现在让我们看看如何使用上述实现，使用基于XML的配置将bean注入IndexApp：

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

        在上述示例中，我们正在调用静态getService方法，使用工厂方法创建带有idmessageService的bean，我们将其注入IndexApp。

    4. 使用工厂方法

        让我们考虑一个实例工厂，该工厂根据提供的数字返回IService实例。这一次，该方法不是静态的：

        ```java
        public class InstanceServiceFactory {
            public IService getNumber(int number) {
                // ...
            }
        }
        ```

        现在让我们看看如何使用上述实现，使用XML配置将bean注入IndexApp：

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

        在上述示例中，我们使用工厂方法在InstanceServiceFactory实例上调用getService方法，以创建具有id messageService的bean，我们在IndexApp中注入该bean。

4. 测试

    这就是我们访问配置的bean的方式：

    ```java
    @Test
    public void whenGetBeans_returnsBean() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("...");
        IndexApp indexApp = applicationContext.getBean("indexApp", IndexApp.class);
        assertNotNull(indexApp);
    }
    ```

5. 结论

    在这个快速教程中，我们举例说明了如何使用Spring Framework使用基于XML的配置注入依赖关系。
