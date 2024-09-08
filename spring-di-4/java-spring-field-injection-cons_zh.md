# [为什么不建议进行现场注射？](https://www.baeldung.com/java-spring-field-injection-cons)

1. 概述

    当我们在集成开发环境中运行代码分析工具时，它可能会对带有 @Autowired 注解的字段发出 “Field injection is not recommended” 的警告。

    在本教程中，我们将探讨为什么不推荐字段注入，以及我们可以使用哪些替代方法。

2. 依赖注入

    对象使用其依赖对象而无需定义或创建依赖对象的过程称为[依赖注入](https://www.baeldung.com/spring-dependency-injection)。它是 Spring 框架的核心功能之一。

    我们可以通过以下三种方式注入依赖对象：

    - 构造器注入
    - 设置器注入
    - 字段注入

    第三种方法是使用 [@Autowired](https://www.baeldung.com/spring-autowire) 注解将依赖对象直接注入类中。虽然这可能是最简单的方法，但我们必须明白它可能会导致一些潜在的问题。

    此外，即使是 Spring [官方文档](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)也不再将字段注入作为 DI 选项之一。

3. 空安全

    如果未正确初始化依赖关系，字段注入会带来 NullPointerException 的风险。

    让我们使用字段注入定义 EmailService 类并添加 EmailValidator 依赖关系：

    ```java
    @Service
    public class EmailService {
        @Autowired
        private EmailValidator emailValidator;
    }
    ```

    现在，让我们添加 process() 方法：

    ```java
    public void process(String email) {
        if(!emailValidator.isValid(email)){
            throw new IllegalArgumentException(INVALID_EMAIL);
        }
        // ...
    }
    ```

    只有当我们提供 EmailValidator 依赖关系时，EmailService 才能正常工作。然而，在使用字段注入时，我们没有提供直接实例化 EmailService 所需的依赖关系的方法。

    此外，我们还可以使用默认构造函数创建 EmailService 实例：

    ```java
    EmailService emailService = new EmailService();
    emailService.process("test@baeldung.com");
    ```

    执行上述代码会导致 NullPointerException，因为我们没有提供其强制依赖项 EmailValidator。

    现在，我们可以使用构造函数注入来降低 NullPointerException 的风险：

    ```java
    private final EmailValidator emailValidator;
    public EmailService(final EmailValidator emailValidator) {
        this.emailValidator = emailValidator;
    }
    ```

    通过这种方法，我们公开了所需的依赖关系。此外，我们现在要求客户提供必须的依赖项。换句话说，如果不提供 EmailValidator 实例，就无法创建新的 EmailService 实例。

4. 不变性

    使用字段注入，我们无法创建不可变类。

    我们需要在声明或通过构造函数实例化最终字段。此外，一旦构造函数被调用，Spring 就会执行自动布线。因此，我们无法使用字段注入来自动连接最终字段。

    由于依赖关系是可变的，我们无法确保它们在初始化后保持不变。此外，在运行应用程序时，重新分配非最终字段可能会产生意想不到的副作用。

    或者，我们可以对强制依赖关系使用构造器注入，对可选依赖关系使用设置器注入。这样，我们就能确保所需的依赖关系保持不变。

5. 设计问题

    现在，我们来讨论一下字段注入可能存在的设计问题。

    1. 违反单一责任

        作为 [SOLID 原则](https://www.baeldung.com/solid-principles) 的一部分，[单一责任原则](https://www.baeldung.com/java-single-responsibility-principle) 规定每个类只能有一个责任。换句话说，一个类只应对一个动作负责，因此，只有一个原因需要更改。

        当我们使用字段注入时，最终可能会违反单一责任原则。我们很容易添加超出必要的依赖关系，并创建一个身兼多职的类。

        另一方面，如果我们使用构造函数注入，我们就会发现，如果一个构造函数有多个依赖关系，我们可能会遇到设计问题。此外，如果构造函数中有超过 7 个参数，甚至集成开发环境也会发出警告。

    2. 循环依赖

        简单地说，当两个或多个类相互依赖时，就会出现[循环依赖](https://www.baeldung.com/circular-dependencies-in-spring)。由于存在这些依赖关系，因此无法构造对象，执行时可能会出现运行时错误或无限循环。

        使用字段注入会导致循环依赖被忽视：

        ```java
        @Component
        public class DependencyA {
            @Autowired
            private DependencyB dependencyB;
        }

        @Component
        public class DependencyB {
            @Autowired
            private DependencyA dependencyA;
        }
        ```

        由于依赖关系是在需要时注入的，而不是在上下文加载时，因此 Spring 不会抛出 BeanCurrentlyInCreationException 异常。

        通过构造器注入，我们可以在编译时检测到循环依赖关系，因为它们会产生无法解决的错误。

        此外，如果我们的代码中存在循环依赖关系，这可能表明我们的设计出了问题。因此，如果可能的话，我们应该考虑重新设计应用程序。

        不过，自 Spring Boot 2.6 版本起，默认情况下[不再允许循环依赖](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes#circular-references-prohibited-by-default)。

6. 测试

    单元测试揭示了字段注入方法的一个主要缺点。

    假设我们想编写一个单元测试来检查 EmailService 中定义的 process() 方法是否正常工作。

    首先，我们要模拟 EmailValidation 对象。然而，由于我们是通过字段注入的方式插入 EmailValidator 的，因此无法直接用模拟版本替换它：

    ```java
    EmailValidator validator = Mockito.mock(EmailValidator.class);
    EmailService emailService = new EmailService();
    ```

    此外，在 EmailService 类中提供 setter 方法会带来额外的漏洞，因为其他类（不仅仅是测试类）也可能调用该方法。

    不过，我们可以通过反射来实例化我们的类。例如，我们可以使用 Mockito：

    ```java
    @Mock
    private EmailValidator emailValidator;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    ```

    在此，Mockito 将尝试使用 @InjectMocks 注解注入模拟。但是，如果字段注入策略失败，Mockito 不会报告失败。

    另一方面，使用构造函数注入，我们可以在不进行反射的情况下提供所需的依赖关系：

    ```java
    private EmailValidator emailValidator;
    private EmailService emailService;
    @BeforeEach
    public void setup() {
        this.emailValidator = Mockito.mock(EmailValidator.class);
        this.emailService = new EmailService(emailValidator);
    }
    ```

7. 结论

    在本文中，我们了解了不推荐字段注入的原因。

    总之，我们可以使用构造器注入来代替字段注入，并使用设置器注入来代替可选依赖项。
