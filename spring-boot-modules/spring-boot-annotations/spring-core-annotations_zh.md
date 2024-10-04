# [Spring 核心注解](https://www.baeldung.com/spring-core-annotations)

1. 概述

    我们可以使用 org.springframework.beans.factory.annotation 和 org.springframework.context.annotation 包中的注解来利用 Spring DI 引擎的功能。

    我们通常称这些注解为 “Spring 核心注解”，本教程将对其进行回顾。

2. DI 相关注解

    1. @Autowired

        我们可以使用 @Autowired 来标记 Spring 将解析和注入的依赖关系。我们可以在构造函数、设置器或字段注入中使用此注解。

        构造函数注入：

        ```java
        class Car {
            Engine engine;

            @Autowired
            Car(Engine engine) {
                this.engine = engine;
            }
        }
        ```

        设置器注入：

        ```java
        class Car {
            Engine engine;

            @Autowired
            void setEngine(Engine engine) {
                this.engine = engine;
            }
        }
        ```

        字段注入：

        ```java
        class Car {
            @Autowired
            Engine engine;
        }
        ```

        @Autowired 有一个名为 required 的布尔参数，默认值为 true。当 Spring 找不到合适的 Bean 进行布线时，它会调整 Spring 的行为。当值为 true 时，将抛出异常，否则，什么也不会被连接。

        请注意，如果使用构造函数注入，所有构造函数参数都是必须的。

        从 4.3 版开始，除非我们声明了至少两个构造函数，否则无需用 @Autowired 明确注解构造函数。

    2. @Bean

        @Bean 是一个工厂方法，用于实例化 Spring Bean：

        ```java
        @Bean
        Engine engine() {
            return new Engine();
        }
        ```

        当需要返回类型的新实例时，Spring 会调用这些方法。

        生成的 Bean 具有与工厂方法相同的名称。如果我们想使用不同的名称，可以使用此注解的 name 或 value 参数（参数 value 是参数 name 的别名）：

        ```java
        @Bean("engine")
        Engine getEngine() {
            return new Engine();
        }
        ```

        请注意，所有使用 @Bean 注解的方法都必须位于 @Configuration 类中。

    3. 标注

        我们使用 @Qualifier 和 @Autowired 来提供我们希望在模棱两可的情况下使用的 Bean id 或 Bean 名称。

        例如，下面两个 Bean 实现了相同的接口：

        ```java
        class Bike implements Vehicle {}
        class Car implements Vehicle {}
        ```

        如果 Spring 需要注入 Vehicle Bean，最终会出现多个匹配的定义。在这种情况下，我们可以使用 @Qualifier 注解显式地提供 Bean 的名称。

        使用构造器注入：

        ```java
        @Autowired
        Biker(@Qualifier("bike") Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        ```

        使用设置器注入：

        ```java
        @Autowired
        void setVehicle(@Qualifier("bike") Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        ```

        另外：

        ```java
        @Autowired
        @Qualifier("bike")
        void setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        ```

        使用字段注入：

        ```java
        @Autowired
        @Qualifier("bike")
        Vehicle vehicle;
        ```

    4. @Value

        我们可以使用 @Value 向 Bean 注入属性值。它与构造函数注入、设置函数注入和字段注入兼容。

        构造函数注入：

        ```java
        Engine(@Value("8") int cylinderCount) {
            this.cylinderCount = cylinderCount;
        }
        ```

        设置器注入：

        ```java
        @Autowired
        void setCylinderCount(@Value("8") int cylinderCount) {
            this.cylinderCount = cylinderCount;
        }
        ```

        另外：

        ```java
        @Value("8")
        void setCylinderCount(int cylinderCount) {
            this.cylinderCount = cylinderCount;
        }
        ```

        字段注入：

        ```java
        @Value("8")
        int cylinderCount;
        ```

        当然，注入静态值并不实用。因此，我们可以在 @Value 中使用占位符字符串来连接外部资源（例如 .properties 或 .yaml 文件）中定义的值。

        让我们假设以下 .properties 文件：

        `engine.fuelType=petrol`

        我们可以用下面的方法注入 engine.fuelType 的值：

        ```java
        @Value("${engine.fuelType}")
        String fuelType;
        ```

        我们甚至可以在 SpEL 中使用 @Value。

    5. @DependsOn

        我们可以使用此注解让 Spring 在注解的 Bean 之前初始化其他 Bean。通常，这种行为是自动的，基于 Bean 之间显式的依赖关系。

        只有当依赖关系是隐式的，例如 JDBC 驱动程序加载或静态变量初始化时，我们才需要使用此注解。

        我们可以在依赖类上使用 @DependsOn 来指定依赖豆的名称。注解的值参数需要一个包含依赖 bean 名称的数组：

        ```java
        @DependsOn("engine")
        class Car implements Vehicle {}
        ```

        或者，如果我们使用 @Bean 注解定义了一个 Bean，那么工厂方法也应使用 @DependsOn 注解：

        ```java
        @Bean
        @DependsOn("fuel")
        Engine engine() {
            return new Engine();
        }
        ```

    6. @Lazy

        当我们想懒散地初始化 Bean 时，就会使用 [@Lazy](https://www.baeldung.com/spring-lazy-annotation)。默认情况下，Spring 会在应用程序上下文启动/引导时急切地创建所有单例 Bean。

        不过，在某些情况下，我们需要在请求时创建 Bean，而不是在应用程序启动时。

        这个注解会根据我们的具体放置位置而有不同的表现。我们可以把它放在

        - 带有 @Bean 注解的 Bean 工厂方法，以延迟方法调用（从而创建 Bean）
        - 一个 @Configuration 类，所有包含的 @Bean 方法都将受到影响
        - 一个 @Component 类，它不是一个 @Configuration 类，这个 Bean 将被懒散地初始化
        - 一个 @Autowired 构造函数、设置函数或字段，用于懒散地加载依赖关系本身（通过代理）。

        该注解有一个名为 value 的参数，默认值为 true。它可用于覆盖默认行为。

        例如，当全局设置为懒惰时，标记 Bean 为急迫加载，或在标有 @Lazy 的 @Configuration 类中配置特定的 @Bean 方法为急迫加载：

        ```java
        @Configuration
        @Lazy
        class VehicleFactoryConfig {
            @Bean
            @Lazy(false)
            Engine engine() {
                return new Engine();
            }
        }
        ```

        如需进一步阅读，请访问[本文](https://www.baeldung.com/spring-lazy-annotation)。

    7. @Lookup

        使用 @Lookup 注解的方法会告诉 Spring，当我们调用该方法时，它将返回该方法返回类型的实例。

        有关注解的详细信息，请参阅[本文](https://www.baeldung.com/spring-lookup)。

    8. @Primary

        有时，我们需要定义多个相同类型的 Bean。在这种情况下，注入将不会成功，因为 Spring 不知道我们需要哪个 Bean。

        我们已经看到了处理这种情况的一种方法：用 @Qualifier 标记所有布线点，并指定所需的 Bean 的名称。

        但是，大多数情况下我们只需要一个特定的 Bean，而很少需要其他 Bean。我们可以使用 @Primary 来简化这种情况：如果我们用 @Primary 标记最常用的 Bean，那么它就会被选中用于未限定的注入点：

        ```java
        @Component
        @Primary
        class Car implements Vehicle {}

        @Component
        class Bike implements Vehicle {}

        @Component
        class Driver {
            @Autowired
            Vehicle vehicle;
        }

        @Component
        class Biker {
            @Autowired
            @Qualifier("bike")
            Vehicle vehicle;
        }
        ```

        在前面的示例中，Car 是主要车辆。因此，在 Driver 类中，Spring 注入了一个 Car Bean。当然，在 Biker Bean 中，vehicle 字段的值将是一个 Bike 对象，因为它是限定的。

    9. @Scope

        我们使用 @Scope 来定义 @Component 类或 @Bean 定义的作用域。它可以是单例、原型、请求、会话、globalSession 或某些自定义作用域。

        例如：

        ```java
        @Component
        @Scope("prototype")
        class Engine {}
        ```

3. 上下文配置注解

    我们可以使用本节描述的注解配置应用程序上下文。

    1. @ 配置文件

        如果我们希望 Spring 仅在特定配置文件处于活动状态时才使用 @Component 类或 @Bean 方法，可以使用 @Profile 对其进行标记。我们可以用注解的值参数配置配置文件的名称：

        ```java
        @Component
        @Profile("sportDay")
        class Bike implements Vehicle {}
        ```

    2. @Import

        通过此注解，我们可以在不进行组件扫描的情况下使用特定的 @Configuration 类。我们可以用 @Import 的值参数来提供这些类：

        ```java
        @Import(VehiclePartSupplier.class)
        class VehicleFactoryConfig {}
        ```

    3. @ImportResource

        我们可以使用此注解导入 XML 配置。我们可以使用 locations 参数或其别名 value 参数指定 XML 文件的位置：

        ```java
        @Configuration
        @ImportResource("classpath:/annotations.xml")
        class VehicleFactoryConfig {}
        ```

    4. 属性源

        使用此注解，我们可以为应用程序设置定义属性文件：

        ```java
        @Configuration
        @PropertySource("classpath:/annotations.properties")
        class VehicleFactoryConfig {}
        ```

        @PropertySource 利用了 Java 8 的重复注释功能，这意味着我们可以用它多次标记一个类：

        ```java
        @Configuration
        @PropertySource("classpath:/annotations.properties")
        @PropertySource("classpath:/vehicle-factory.properties")
        class VehicleFactoryConfig {}
        ```

    5. @PropertySources

        我们可以使用此注解指定多个 @PropertySource 配置：

        ```java
        @Configuration
        @PropertySources({
            @PropertySource("classpath:/annotations.properties"),
            @PropertySource("classpath:/vehicle-factory.properties")
        })
        class VehicleFactoryConfig {}
        ```

        请注意，自 Java 8 以来，我们可以通过上述重复注释功能实现同样的功能。

4. 结论

    在本文中，我们概述了最常见的 Spring 核心注解。我们还了解了如何配置 bean 接线和应用程序上下文，以及如何为组件扫描标记类。
