# [Spring @Qualifier 注解详解](https://www.baeldung.com/spring-qualifier-annotation)

Spring+ · @Autowired · Spring 注解 · Spring 核心基础  

1. 概述

    在本教程中，我们将探讨 Spring 的 `@Qualifier` 注解的作用、它能解决哪些问题，以及如何正确使用它。

    我们还将解释它与 `@Primary` 注解的区别，以及与“按名称自动装配”的不同之处。

    > **延伸阅读：**
    >
    > - Spring @Primary 注解 — 学习如何使用 `@Primary` 在自动装配时指定首选 Bean。
    > - [Spring 中的依赖注入：@Autowired、@Resource 和 @Inject](https://www.baeldung.com/spring-annotations-resource-inject-autowire) — 对比这三个依赖注入相关注解的异同。
    > - [@Lookup 注解在 Spring 中的使用](https://www.baeldung.com/spring-lookup-annotation) — 学习如何在 Spring 中使用 `@Lookup` 实现过程式注入。

2. 自动装配需要消除歧义

    `@Autowired` 注解是显式声明依赖注入需求的一种优秀方式。虽然它非常实用，但在某些情况下，仅靠该注解不足以让 Spring 确定应注入哪个 Bean。

    默认情况下，Spring 会**按类型（by type）** 解析自动装配的依赖项。

    如果容器中存在多个相同类型的 Bean，框架将抛出 `NoUniqueBeanDefinitionException`，表明有多个候选 Bean 可供注入。

    设想以下场景：有两个可能的候选 Bean，Spring 需要从中选择一个注入到某个实例中：

    ```java
    @Component("fooFormatter")
    public class FooFormatter implements Formatter {
        public String format() {
            return "foo";
        }
    }

    @Component("barFormatter")
    public class BarFormatter implements Formatter {
        public String format() {
            return "bar";
        }
    }

    @Component
    public class FooService {
        @Autowired
        private Formatter formatter;
    }
    ```

    如果我们尝试将 `FooService` 加载到上下文中，Spring 会抛出 `NoUniqueBeanDefinitionException`，因为它不知道该注入哪个 Bean。为避免此问题，有多种解决方案，`@Qualifier` 注解是其中之一。

3. @Qualifier 注解

    通过使用 `@Qualifier` 注解，我们可以明确指定需要注入哪个 Bean，从而消除歧义。

    让我们回顾前面的例子，看看如何通过添加 `@Qualifier` 注解来解决问题：

    ```java
    @Component
    public class FooService {
        @Autowired
        @Qualifier("fooFormatter")
        private Formatter formatter;
    }
    ```

    通过添加 `@Qualifier` 注解并指定具体实现的名称（如本例中的 `"fooFormatter"`），我们可以在 Spring 发现多个相同类型 Bean 时避免歧义。

    需要注意的是，此处使用的限定符名称应与 `@Component` 注解中声明的名称一致。

    > **提示**：我们也可以直接在实现类上使用 `@Qualifier` 注解，而不必在 `@Component` 中指定名称，效果相同：

    ```java
    @Component
    @Qualifier("fooFormatter")
    public class FooFormatter implements Formatter {
        //...
    }

    @Component
    @Qualifier("barFormatter")
    public class BarFormatter implements Formatter {
        //...
    }
    ```

4. @Qualifier 与 @Primary 的对比

    还有一个名为 `@Primary` 的注解，也可用于在依赖注入存在歧义时指定优先注入的 Bean。

    该注解用于在存在多个相同类型 Bean 时定义“默认首选项”。除非另有指定，否则 Spring 将注入标记了 `@Primary` 的 Bean。

    示例：

    ```java
    @Configuration
    public class Config {
        @Bean
        public Employee johnEmployee() {
            return new Employee("John");
        }

        @Bean
        @Primary
        public Employee tonyEmployee() {
            return new Employee("Tony");
        }
    }
    ```

    在此例中，两个方法都返回 `Employee` 类型。Spring 将注入 `tonyEmployee()` 方法返回的 Bean，因为它标记了 `@Primary`。这在我们希望为某类型指定默认注入 Bean 时非常有用。

    如果在某些注入点需要另一个 Bean，我们必须显式指定，例如通过 `@Qualifier` 注解。比如，我们可以使用 `@Qualifier` 指定注入 `johnEmployee()` 方法返回的 Bean。

    > **重要提示**：如果同时使用了 `@Qualifier` 和 `@Primary`，`@Qualifier` 优先级更高。简单来说，`@Primary` 定义默认行为，而 `@Qualifier` 是精确指定。

    再看一个使用 `@Primary` 的例子（基于初始示例）：

    ```java
    @Component
    @Primary
    public class FooFormatter implements Formatter {
        //...
    }

    @Component
    public class BarFormatter implements Formatter {
        //...
    }
    ```

    这里，`@Primary` 注解直接加在其中一个实现类上，从而消除了注入时的歧义。

5. @Qualifier 与“按名称自动装配”的对比

    当存在多个 Bean 时，另一种决定注入哪个 Bean 的方式是**按字段名称自动装配**。这是 Spring 在没有其他提示时的默认行为。

    基于我们最初的示例：

    ```java
    @Component
    public class FooService {
        @Autowired
        private Formatter fooFormatter;
    }
    ```

    此时，Spring 会判断应注入 `FooFormatter` Bean，因为字段名 `fooFormatter` 与 `@Component("fooFormatter")` 中指定的名称匹配。

6. 结论

    本文中，我们介绍了在依赖注入时需要消除歧义的场景，重点讲解了 `@Qualifier` 注解的使用方法，并将其与 `@Primary` 注解及“按名称自动装配”机制进行了对比。

    合理使用这些机制，可以帮助我们构建更清晰、更可控的 Spring 应用程序依赖结构。
