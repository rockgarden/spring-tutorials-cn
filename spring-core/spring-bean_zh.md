# [什么是Spring Bean？](https://www.baeldung.com/spring-bean)

1. 概述

    Bean 是 Spring 框架的一个关键概念。因此，理解这一概念对于掌握该框架并有效使用它至关重要。

    遗憾的是，对于 Spring Bean 到底是什么这个简单的问题并没有明确的答案。有些解释过于浅显，以至于忽略了全局，而另一些解释则过于模糊。

    本教程将从官方文档的描述入手，尝试阐明这一主题。

2. Bean 定义

    下面是 Spring Framework [文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#beans-introduction)中关于 Bean 的定义：

    在 Spring 中，构成应用程序主干并由 Spring IoC 容器管理的对象称为 Bean。Bean 是由 Spring IoC 容器实例化、装配和管理的对象。

    这个定义简洁明了，直奔主题，但没有详细说明一个重要元素：Spring IoC 容器。让我们仔细看看它是什么，以及它带来的好处。

3. 反转控制

    简单地说，控制反转（[IoC](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)）是一个对象定义其依赖关系而不创建依赖关系的过程。该对象将构建此类依赖关系的工作委托给 IoC 容器。

    在深入了解 IoC 之前，让我们先从几个领域类的声明开始。

    1. 领域类

        假设我们有一个类的声明

        ```java
        public class Company {
            private Address address;
            public Company(Address address) {
                this.address = address;
            }
            // getter, setter and other properties
        }
        ```

        该类需要一个地址类型的协作者：

        ```java
        public class Address {
            private String street;
            private int number;
            public Address(String street, int number) {
                this.street = street;
                this.number = number;
            }
            // getters and setters
        }
        ```

    2. 传统方法

        通常，我们使用类的构造函数创建对象：

        ```java
        Address address = new Address("High Street", 1000);
        Company company = new Company(address);
        ```

        这种方法无可厚非，但如果能以更好的方式管理依赖关系岂不更好？

        想象一下，一个应用程序中有几十个甚至上百个类。有时我们想在整个应用程序中共享一个类的实例，有时我们需要为每个用例创建一个单独的对象，等等。

        管理如此多的对象简直就是一场噩梦。这时，反转控制功能就派上用场了。

        一个对象可以从 IoC 容器中获取其依赖关系，而不是自己构建依赖关系。我们需要做的就是为容器提供适当的配置元数据。

    3. Bean配置

        首先，让我们用 @Component 注解来装饰公司类：

        ```java
        @Component
        public class Company {
            // this body is the same as before
        }
        ```

        下面是一个向 IoC 容器提供 Bean 元数据的配置类：

        ```java
        @Configuration
        @ComponentScan(basePackageClasses = Company.class)
        public class Config {
            @Bean
            public Address getAddress() {
                return new Address("High Street", 1000);
            }
        }
        ```

        该配置类生成了一个 Address 类型的 Bean。它还带有 @ComponentScan 注解，指示容器在包含 Company 类的包中查找 Bean。

        当 Spring IoC 容器构建这些类型的对象时，所有对象都被称为 Spring Bean，因为它们是由 IoC 容器管理的。

    4. 运行中的 IoC

        由于我们在配置类中定义了 Bean，因此需要 AnnotationConfigApplicationContext 类的实例来构建容器：

        `ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);`

        快速测试验证我们的 Bean 是否存在及其属性值：

        ```java
        Company company = context.getBean("company", Company.class);
        assertEquals("High Street", company.getAddress().getStreet());
        assertEquals(1000, company.getAddress().getNumber());
        ```

        结果证明 IoC 容器已正确创建并初始化了 Bean。

4. 结论

    本文简要介绍了 Spring Bean 及其与 IoC 容器的关系。
