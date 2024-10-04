# [Spring中的构造函数依赖性注入](https://www.baeldung.com/constructor-injection-in-spring)

1. 简介

    可以说，现代软件设计中最重要的开发原则之一是依赖注入（DI），它很自然地从另一个至关重要的原则中产生。模块化。

    这个快速教程将探讨Spring中一种特殊的DI技术，即基于构造函数的依赖注入，简单地说，就是在实例化时将所需的组件传递给类。

    为了开始学习，我们需要在pom.xml中导入spring-text依赖。

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.2.8.RELEASE</version>
    </dependency>
    ```

    然后我们需要设置一个配置文件。这个文件可以是一个POJO，也可以是一个XML文件，根据偏好而定。

2. 基于注解的配置(Annotation Based Configuration)

    Java配置文件看起来类似于带有一些额外注解的Java对象。

    constructordi/Config.java

    这里我们使用注解来通知Spring运行时，这个类提供了Bean定义（@Bean注解），并且包com.baeldung.spring需要执行上下文扫描，以获取更多的Bean。接下来，我们定义一个Car类。

    ```java
    @Component
    public class Car {
        @Autowired
        public Car(Engine engine, Transmission transmission) {
            this.engine = engine;
            this.transmission = transmission;
        }
    }
    ```

    Spring在进行包扫描时将遇到我们的汽车类，并将通过调用@Autowired注解的构造函数来初始化其实例。

    通过调用配置类的@Bean注解方法，我们将获得引擎和变速器的实例。最后，我们需要使用我们的POJO配置来引导一个ApplicationContext。

    ```java
    ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    Car car = context.getBean(Car.class);
    ```

3. 隐式构造函数注入(Implicit Constructor Injection)

    从Spring 4.3开始，只有一个构造函数的类可以省略@Autowired注解。这是一个很好的小便利和模板删除。

    除此之外，从4.3开始，我们还可以在@Configuration注解的类中利用基于构造函数的注入。此外，如果这样的类只有一个构造函数，我们也可以省略@Autowired注解。

4. 基于XML的配置(XML Based Configuration)

    用基于构造函数的依赖注入来配置Spring运行时的另一种方法是使用XML配置文件。

    resources/constructordi.xml

    请注意，constructor-arg可以接受一个字面值或对另一个bean的引用，而且可以提供一个可选的显式索引和类型。我们可以使用Type和index属性来解决模棱两可的问题（例如，如果一个构造函数接受相同类型的多个参数）。

    > name属性也可以用于xml到java变量的匹配，但这样你的代码在编译时必须打开调试标志。

    在这种情况下，我们需要使用ClassPathXmlApplicationContext来引导我们的Spring应用上下文。

    ```java
    ApplicationContext context = new ClassPathXmlApplicationContext("baeldung.xml");
    Car car = context.getBean(Car.class);
    ```

5. 优点和缺点(Pros and Cons)

    构造函数注入与字段注入相比有几个优点。

    第一个优点是可测试性。假设我们要对一个使用字段注入的Spring Bean进行单元测试。

    ```java
    public class UserService {
        // Field-Based
        @Autowired 
        private UserRepository userRepository;
    }
    ```

    在构建UserService实例的过程中，我们不能初始化userRepository的状态。实现这一目标的唯一方法是[通过反射API](https://www.baeldung.com/java-reflection)，这完全破坏了封装。而且，与简单的构造器调用相比，所产生的代码将不太安全。

    此外，通过字段注入，我们不能执行类级的不变性，所以有可能出现UserService实例没有正确初始化userRepository的情况。因此，我们可能会遇到随机的NullPointerExceptions在这里和那里。另外，有了构造函数注入，构建不可变的组件就更容易了。

    此外，从OOP的角度来看，使用构造函数来创建对象实例更加自然。

    另一方面，构造函数注入的主要缺点是它的冗长性，特别是当一个bean有少量的依赖关系时。有时，这可能是一种变相的祝福，因为我们可能会更努力地保持最小的依赖关系数量。

6. 结语

    这篇简短的文章展示了使用Spring框架使用基于构造函数的依赖注入的两种不同方式的基础知识。
