# [Spring Pointcut 表达式](https://www.baeldung.com/spring-aop-pointcut-tutorial)

连接点是程序执行的一个步骤，例如方法的执行或异常的处理。 在 Spring AOP 中，一个连接点总是代表一个方法执行。 切入点是匹配连接点的谓词，切入点表达式语言是一种以编程方式描述切入点的方式。

1. 用法

    切入点表达式可以显示为 `@Pointcut` 注释的值：

    ```java
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryClassMethods() {}
    ```

    方法声明称为切入点签名。 它提供了一个名称，通知注释可以使用该名称来引用该切入点：

    ```java
    @Around("repositoryClassMethods()")
    public Object measureMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        ...
    }
    ```

    切入点表达式也可以作为 aop:pointcut 标签的表达式属性的值出现：

    ```xml
    <aop:config>
        <aop:pointcut id="anyDaoMethod" 
        expression="@target(org.springframework.stereotype.Repository)"/>
    </aop:config>
    ```

2. 切入点指示符

    切入点表达式以切入点指示符 (Pointcut Designators, PCD) 开头，这是一个告诉 Spring AOP 匹配什么的关键字。 有几个切入点指示符，例如方法的执行、类型、方法参数或注释。

    1. execution

        主要的 Spring PCD 是执行，它匹配方法执行连接点：

        `@Pointcut("execution(public String com.baeldung.pointcutadvice.dao.FooDao.findById(Long))")`

        此示例切入点将与 FooDao 类的 findById 方法的执行完全匹配。 这可行，但它不是很灵活。 假设我们想要匹配 FooDao 类的所有方法，这些方法可能具有不同的签名、返回类型和参数。 为此，我们可以使用通配符：

        `@Pointcut("execution(* com.baeldung.pointcutadvice.dao.FooDao.*(..))")`

        这里，第一个通配符匹配任何返回值，第二个匹配任何方法名称，并且 (..) 模式匹配任意数量的参数（零个或多个）。

    2. within

        实现与上一节相同结果的另一种方法是使用 inside PCD，它将匹配限制为某些类型的连接点：

        `@Pointcut("within(com.baeldung.pointcutadvice.dao.FooDao)")`

        我们还可以匹配 com.baeldung 包或子包中的任何类型：

        `@Pointcut("within(com.baeldung..*)")`

    3. this and target

        这将匹配限制到 bean 引用是给定类型的实例的连接点，而 target 限制匹配到目标对象是给定类型的实例的连接点。前者在 Spring AOP 创建基于 [CGLIB](https://www.baeldung.com/cglib) 的代理时工作，后者在创建基于 JDK 的代理时使用。假设目标类实现了一个接口：

        `public class FooDao implements BarDao { ... }`

        在这种情况下，Spring AOP 将使用基于 JDK 的代理，我们应该使用目标 PCD，因为代理对象将是 Proxy 类的实例并实现 BarDao 接口：

        `@Pointcut("target(com.baeldung.pointcutadvice.dao.BarDao)")`

        另一方面，如果 FooDao 没有实现任何接口，或者 proxyTargetClass 属性设置为 true，那么代理对象将是 FooDao 的子类，我们可以使用这个 PCD：

        `@Pointcut("this(com.baeldung.pointcutadvice.dao.FooDao)")`

    4. args

        我们可以使用这个 PCD 来匹配特定的方法参数：  

        `@Pointcut("execution(* *..find*(Long))")`

        这个切入点匹配任何以 find 开头并且只有一个 Long 类型参数的方法。

        如果我们想匹配具有任意数量参数的方法，但仍然具有 Long 类型的第一个参数，我们可以使用以下表达式：  

        `@Pointcut("execution(* *..find*(Long,..))")`

    5. @target

        @target PCD（不要与上述 target PCD 混淆）将匹配限制为执行对象的类具有给定类型的注释(`@Repository`)的连接点：

        `@Pointcut("@target(org.springframework.stereotype.Repository)")`

    6. @args

        此 PCD 将匹配限制为连接点，其中传递的实际参数的运行时类型具有给定类型的注释。

        假设我们要跟踪所有接受带有 @Entity 注释的 bean 的方法：

        ```java
        @Pointcut("@args(com.baeldung.pointcutadvice.annotations.Entity)")
        public void methodsAcceptingEntities() {}
        ```

        要访问参数，我们应该为通知提供一个 JoinPoint 参数：

        ```java
        @Before("methodsAcceptingEntities()")
        public void logMethodAcceptionEntityAnnotatedBean(JoinPoint jp) {
            logger.info("Accepting beans with @Entity annotation: " + jp.getArgs()[0]);
        }
        ```

    7. @within

        此 PCD 将匹配限制为具有给定注释的类型内的连接点：

        `@Pointcut("@within(org.springframework.stereotype.Repository)")`

        等效于：

        `@Pointcut("within(@org.springframework.stereotype.Repository *)")`

    8. @annotation

        此 PCD 将匹配限制为连接点的主题具有给定注释的连接点。 例如，我们可以创建一个@Loggable 注解：

        ```java
        @Pointcut("@annotation(com.baeldung.pointcutadvice.annotations.Loggable)")
        public void loggableMethods() {}
        ```

        然后我们可以记录由该注释标记的方法的执行：

        ```java
        @Before("loggableMethods()")
        public void logMethod(JoinPoint jp) {
            String methodName = jp.getSignature().getName();
            logger.info("Executing method: " + methodName);
        }
        ```

3. 组合切入点表达式

    切入点表达式可以使用 &&、|| 和 ! 运算符：

    ```java
    @Pointcut("@target(org.springframework.stereotype.Repository)")
    public void repositoryMethods() {}

    @Pointcut("execution(* *..create*(Long,..))")
    public void firstLongParamMethods() {}

    @Pointcut("repositoryMethods() && firstLongParamMethods()")
    public void entityCreationMethods() {}
    ```
