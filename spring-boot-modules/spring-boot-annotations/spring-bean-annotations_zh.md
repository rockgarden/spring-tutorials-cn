# [Spring Bean 注释](https://www.baeldung.com/spring-bean-annotations)

1. 概述

    在本教程中，我们将讨论用于定义不同类型 Bean 的最常用 Spring Bean 注释。

    在 Spring 容器中配置 Bean 有几种方法。首先，我们可以使用 XML 配置声明它们。我们还可以在配置类中使用 @Bean 注解声明 Bean。

    最后，我们可以使用 org.springframework.stereotype 包中的注解之一来标记类，剩下的就交给组件扫描来处理。

2. 组件扫描

    如果启用了组件扫描，Spring 就能自动扫描包中的 Bean。

    @ComponentScan 通过注解配置来配置扫描哪些包中的类。我们可以使用 basePackages 或 value 参数（value 是 basePackages 的别名）直接指定基础包名称：

    ```java
    @Configuration
    @ComponentScan(basePackages = "com.baeldung.annotations")
    class VehicleFactoryConfig {}
    ```

    此外，我们还可以使用 basePackageClasses 参数指向基础包中的类：

    ```java
    @Configuration
    @ComponentScan(basePackageClasses = VehicleFactoryConfig.class)
    class VehicleFactoryConfig {}
    ```

    这两个参数都是数组，因此我们可以为每个参数提供多个软件包。

    如果没有指定参数，扫描将从 @ComponentScan 注解类所在的同一包中进行。

    @ComponentScan 利用了 Java 8 的重复注解功能，这意味着我们可以多次用它来标记一个类：

    ```java
    @Configuration
    @ComponentScan(basePackages = "com.baeldung.annotations")
    @ComponentScan(basePackageClasses = VehicleFactoryConfig.class)
    class VehicleFactoryConfig {}
    ```

    另外，我们还可以使用 @ComponentScans 来指定多个 @ComponentScan 配置：

    ```java
    @Configuration
    @ComponentScans({ 
        @ComponentScan(basePackages = "com.baeldung.annotations"), 
        @ComponentScan(basePackageClasses = VehicleFactoryConfig.class)
    })
    class VehicleFactoryConfig {}
    ```

    使用 XML 配置时，配置组件扫描也同样简单：

    `<context:component-scan base-package="com.baeldung" />` 

3. 组件

    @Component 是一个类级注解。在组件扫描过程中，Spring Framework 会自动检测带有 @Component 注解的类：

    ```java
    @Component
    class CarUtility {
        // ...
    }
    ```

    默认情况下，该类的 bean 实例名称与类名相同，且首字母小写。此外，我们还可以使用此注解的可选值参数指定不同的名称。

    由于 @Repository、@Service、@Configuration 和 @Controller 都是 @Component 的元注解，因此它们共享相同的 Bean 命名行为。在组件扫描过程中，Spring 也会自动选择它们。

4. @Repository

    DAO 或 Repository 类通常代表应用程序中的数据库访问层，应使用 @Repository 进行注解：

    ```java
    @Repository
    class VehicleRepository {
        // ...
    }
    ```

    使用该注解的一个好处是，它可以自动翻译持久性异常。在使用持久化框架（如 Hibernate）时，使用 @Repository 注解的类中抛出的本地异常将自动翻译为 Spring 的 DataAccessExeption 子类。

    要启用异常翻译，我们需要声明自己的 PersistenceExceptionTranslationPostProcessor Bean：

    ```java
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
    ```

    请注意，在大多数情况下，Spring 会自动完成上述步骤。

    或者通过 XML 配置：

    `<bean class= "org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>`

5. @Service

    应用程序的业务逻辑通常位于服务层，因此我们将使用 @Service 注解来表示某个类属于服务层：

    ```java
    @Service
    public class VehicleService {
        // ...
    }
    ```

6. 控制器

    @Controller 是一个类级注解，它告诉 Spring 框架该类在 Spring MVC 中充当控制器：

    ```java
    @Controller
    public class VehicleController {
        // ...
    }
    ```

7. 配置

    配置类可以包含用 @Bean 注解的 bean 定义方法：

    ```java
    @Configuration
    class VehicleFactoryConfig {
        @Bean
        Engine engine() {
            return new Engine();
        }
    }
    ```

8. 定型注解与 AOP

    当我们使用 Spring 定型注解时，很容易创建一个指向具有特定定型的所有类的快捷方式。

    例如，假设我们要测量 DAO 层方法的执行时间。我们将利用 @Repository 定型创建以下方面（使用 AspectJ 注释）：

    ```java
    @Aspect
    @Component
    public class PerformanceAspect {
        @Pointcut("within(@org.springframework.stereotype.Repository *)")
        public void repositoryClassMethods() {};

        @Around("repositoryClassMethods()")
        public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) 
        throws Throwable {
            long start = System.nanoTime();
            Object returnValue = joinPoint.proceed();
            long end = System.nanoTime();
            String methodName = joinPoint.getSignature().getName();
            System.out.println(
            "Execution of " + methodName + " took " + 
            TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
            return returnValue;
        }
    }
    ```

    在这个示例中，我们创建了一个 pointcut，用于匹配注释为 @Repository 的类中的所有方法。然后，我们使用 @Around 建议来定位该快捷方式，并确定拦截方法调用的执行时间。

    此外，使用这种方法，我们还可以为每个应用层添加日志记录、性能管理、审计和其他行为。

9. 结论

    在本文中，我们研究了 Spring 定型注解，并讨论了它们各自代表的语义类型。

    我们还学习了如何使用组件扫描来告诉容器在哪里可以找到注解的类。

    最后，我们了解了这些注解如何带来简洁、分层的设计以及应用程序关注点之间的分离。这些注解还使配置变得更小，因为我们不再需要手动明确定义 Bean。
