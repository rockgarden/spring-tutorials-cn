# [Spring组件扫描](https://www.baeldung.com/spring-component-scanning)

1. 概述

    在本教程中，我们将介绍Spring中的组件扫描。在使用Spring时，我们可以对类进行注释，以便将它们变成Springbean。此外，我们可以告诉Spring在哪里搜索这些带注释的类，因为在这个特定的运行中，并非所有的类都必须成为bean。

    当然，组件扫描有一些默认设置，但我们也可以自定义搜索包。

    首先，让我们看看默认设置。

2. @ComponentScan无参数

    1. 在Spring应用程序中使用@ComponentScan

        在Spring中，我们使用@ComponentScan注释和@Configuration注释来指定要扫描的包，不带参数的@ComponentScan告诉Spring扫描当前包及其所有子包。

        假设我们在 com.baeldung.componentscan.springapp 软件包中有以下@Configuration：

        ```java
        @Configuration
        @ComponentScan
        public class SpringComponentScanApp {
            private static ApplicationContext applicationContext;

            @Bean
            public ExampleBean exampleBean() {
                return new ExampleBean();
            }

            public static void main(String[] args) {
                applicationContext = 
                new AnnotationConfigApplicationContext(SpringComponentScanApp.class);

                for (String beanName : applicationContext.getBeanDefinitionNames()) {
                    System.out.println(beanName);
                }
            }
        }
        ```

        此外，我们在com.baeldung.componentscan.springapp.animals package中有Cat和Dog组件：

        ```java
        package com.baeldung.componentscan.springapp.animals;
        // ...
        @Component
        public class Cat {}
        package com.baeldung.componentscan.springapp.animals;
        // ...
        @Component
        public class Dog {}
        ```

        最后，我们在com.baldeng.componentscan.springapp中有Rose组件。鲜花套餐：

        ```java
        package com.baeldung.componentscan.springapp.flowers;
        // ...
        @Component
        public class Rose {}
        ```

        main（）方法的输出将包含com.baldeng.componentscan的所有bean。springapp包及其子包：

        ```txt
        springComponentScanApp
        cat
        dog
        rose
        exampleBean
        ```

        注意，主应用程序类也是一个bean，因为它用@Configuration注释，它是一个@Component。

        我们还应该注意，主应用程序类和配置类不一定相同。如果它们不同，那么我们将主应用程序类放在哪里并不重要。只有配置类的位置很重要，因为组件扫描默认从其包开始。

        最后，请注意，在我们的示例中，@ComponentScan等效于：

        `@ComponentScan(basePackages = "com.baeldung.componentscan.springapp")`

        basePackages参数是用于扫描的包或包数组。

    2. 在Spring Boot应用程序中使用@ComponentScan

        SpringBoot的诀窍在于，许多事情都是隐式发生的。我们使用@SpringBootApplication注释，但它是三个注释的组合：

        - @Configuration
        - @EnableAutoConfiguration
        - @ComponentScan

        让我们在com.baldeng.componentscan.springbootapp包中创建一个类似的结构，这一次的主要应用将是：

        ```java
        package com.baeldung.componentscan.springbootapp;
        // ...
        @SpringBootApplication
        public class SpringBootComponentScanApp {
            private static ApplicationContext applicationContext;

            @Bean
            public ExampleBean exampleBean() {
                return new ExampleBean();
            }

            public static void main(String[] args) {
                applicationContext = SpringApplication.run(SpringBootComponentScanApp.class, args);
                checkBeansPresence(
                "cat", "dog", "rose", "exampleBean", "springBootComponentScanApp");
            }

            private static void checkBeansPresence(String... beans) {
                for (String beanName : beans) {
                    System.out.println("Is " + beanName + " in ApplicationContext: " + 
                    applicationContext.containsBean(beanName));
                }
            }
        }
        ```

        所有其他包和类都保持不变，我们只需将它们复制到附近的com.baeldung.componentscan.springbootapp包。

        SpringBoot扫描包的方式与前面的示例类似。让我们检查一下输出：

        ```log
        Is cat in ApplicationContext: true
        Is dog in ApplicationContext: true
        Is rose in ApplicationContext: true
        Is exampleBean in ApplicationContext: true
        Is springBootComponentScanApp in ApplicationContext: true
        ```

        我们只是在第二个示例中检查bean是否存在（而不是打印出所有bean），原因是输出太大。

        这是因为隐式@EnableAutoConfiguration注释，它使SpringBoot依赖pom.xml中的依赖项自动创建许多bean。

3. 带参数的@ComponentScan

    现在，让我们自定义扫描路径。例如，假设我们要排除Rose bean。

    1. @ComponentScan特定软件包

        我们可以用几种不同的方法来做到这一点。首先，我们可以更改基本包：

        ```java
        @ComponentScan(basePackages = "com.baeldung.componentscan.springapp.animals")
        @Configuration
        public class SpringComponentScanApp {...}
        ```

        现在输出将是：

        ```log
        springComponentScanApp
        cat
        dog
        exampleBean
        ```

        让我们看看这背后的原因：

        - springComponentScanApp是作为参数传递给AnnotationConfigApplicationContext的配置创建的
        - exampleBean是配置内配置的bean
        - cat和dog位于指定的com.baeldung.componentscan.springapp.animals包中

        上面列出的所有定制也适用于Spring Boot。我们可以将@ComponentScan与@SpringBootApplication结合使用，结果将是相同的：

        ```java
        @SpringBootApplication
        @ComponentScan(basePackages = "com.baeldung.componentscan.springbootapp.animals")
        ```

    2. @ComponentScan多包

        Spring提供了一种方便的方法来指定多个包名称。为此，我们需要使用字符串数组。

        数组的每个字符串表示一个包名称：

        `@ComponentScan(basePackages = {"com.baeldung.componentscan.springapp.animals", "com.baeldung.componentscan.springapp.flowers"})`

        或者，自4.1.1春季以来，我们可以使用逗号、分号或空格分隔包裹列表：

        ```java
        @ComponentScan(basePackages = "com.baeldung.componentscan.springapp.animals;com.baeldung.componentscan.springapp.flowers")
        @ComponentScan(basePackages = "com.baeldung.componentscan.springapp.animals,com.baeldung.componentscan.springapp.flowers")
        @ComponentScan(basePackages = "com.baeldung.componentscan.springapp.animals com.baeldung.componentscan.springapp.flowers")
        ```

    3. @ComponentScan（排除）

        另一种方法是使用过滤器，指定要排除的类的模式：

        ```java
        @ComponentScan(excludeFilters = 
        @ComponentScan.Filter(type=FilterType.REGEX,
            pattern="com\\.baeldung\\.componentscan\\.springapp\\.flowers\\..*"))
        ```

        我们还可以选择不同的过滤器类型，因为注释支持多种灵活的选项来过滤扫描的类：

        ```java
        @ComponentScan(excludeFilters = 
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = Rose.class))
        ```

4. 默认包

    我们应该避免将@Configuration类放在[默认包](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-structuring-your-code.html)中（即根本不指定包）。如果这样做，Spring会扫描类路径中所有jar中的所有类，这会导致错误，应用程序可能无法启动。

5. TEST

    **ERROR**: `Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'cat' for bean class [com.baeldung.componentscan.springbootapp.animals.Cat] conflicts with existing, non-compatible bean definition of same name and class [com.baeldung.componentscan.springapp.animals.Cat]`

    说明：运行 SpringBootDiApplication.java 报错，未限定扫描范围，`@ComponentScan(basePackages = "com.baeldung.componentscan.springbootapp")`。
