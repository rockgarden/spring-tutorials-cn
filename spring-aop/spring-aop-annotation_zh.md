# [实现自定义 Spring AOP 注释](https://www.baeldung.com/spring-aop-annotation)

我们将使用 Spring 中的 AOP 支持来实现自定义 AOP 注释。我们可以将其应用于 Spring bean 以在运行时向它们注入额外的行为。

我们将实现的 AOP 类型是注解驱动的。如果我们使用过 Spring [@Transactional](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html) 注解，我们可能已经对此很熟悉了：

```java
@Transactional
public void orderGoods(Order order) {
   // A series of database calls to be performed in a transaction
}
```

这里的关键是非侵入性(The key here is non-invasiveness.)。通过使用注释元数据，我们的核心业务逻辑不会被我们的事务代码污染。这使得推理、重构和隔离测试变得更容易。

有时，开发 Spring 应用程序的人可以将其视为“Spring Magic”，而无需详细考虑它是如何工作的。实际上，发生的事情并不是特别复杂。但是，一旦我们完成了本文中的步骤，我们将能够创建自己的自定义注解，以便理解和利用 AOP。

首先，让我们添加我们的 Maven 依赖项。对于这个例子，我们将使用 Spring Boot，因为它的约定优于配置的方法让我们能够尽快启动并运行：spring-boot-starter-parent、spring-boot-starter-aop

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.2.RELEASE</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
</dependencies>
```

请注意，我们已经包含了 AOP 启动器，它引入了我们开始实现方面所需的库。

1. 创建自定义注解

    我们要创建的注释将用于记录方法执行所需的时间。让我们创建我们的注解：

    ```java
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogExecutionTime {}
    ```

    尽管实现相对简单，但值得注意的是这两个元注释的用途。

    [@Target](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/annotation/Target.html) 注解告诉我们注解适用的地方。这里我们使用的是 ElementType.Method，这意味着它只适用于方法。如果我们尝试在其他任何地方使用注解，那么我们的代码将无法编译。这种行为是有道理的，因为我们的注释将用于记录方法执行时间。

    [@Retention](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/annotation/Retention.html) 只是说明注解在运行时是否对 JVM 可用。默认情况下它不是，所以 Spring AOP 将无法看到注解。这就是它被重新配置的原因。

2. 创建自定义方面

    现在我们有了注释，让我们创建我们的方面。这只是将封装我们的横切关注点的模块，在我们的例子中是方法执行时间日志记录。它只是一个类，用 [@Aspect](https://www.eclipse.org/aspectj/doc/released/aspectj5rt-api/org/aspectj/lang/annotation/Aspect.html?is-external=true) 注释：

    ```java
    @Aspect
    @Component
    public class ExampleAspect {}
    ```

    我们还包含了 [@Component](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html) 注释，因为我们的类也需要是一个 Spring bean 才能被检测到。本质上，这是我们将实现我们希望自定义注解注入的逻辑的类。

3. 创建切入点和建议

    现在，让我们创建切入点和建议。这将是一个存在于我们方面的带注释的方法：

    ```java
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
    ```

    从技术上讲，这并没有改变任何东西的行为，但是仍然有很多事情需要分析。

    首先，我们用 [@Around](https://www.eclipse.org/aspectj/doc/released/aspectj5rt-api/org/aspectj/lang/annotation/Around.html?is-external=true) 注释了我们的方法。这是我们的建议，围绕建议意味着我们在方法执行之前和之后添加额外的代码。还有其他类型的建议，例如之前和之后，但它们将超出本文的范围。

    接下来，我们的 @Around 注释有一个切入点参数。我们的切入点只是说，“将这个建议应用于任何带有 @LogExecutionTime 注释的方法。” 还有很多其他类型的切入点，但如果作用域，它们将再次被排除在外。

    logExecutionTime() 方法本身就是我们的建议 Advice。有一个参数，[ProceedingJoinPoint](https://www.eclipse.org/aspectj/doc/next/runtime-api/org/aspectj/lang/ProceedingJoinPoint.html)。在例子中，这将是一个使用 @LogExecutionTime 注释的执行方法。

    最后，当我们的注解方法最终被调用时，会发生的是我们的通知将首先被调用。然后由我们的建议决定下一步该做什么。在我们的例子中，我们的建议除了调用 `proceed()` 之外什么都不做，它只是调用原始的带注释的方法。

4. 记录我们的执行时间

    现在我们已经有了我们的骨架，我们需要做的就是在我们的建议中添加一些额外的逻辑。除了调用原始方法之外，这将是记录执行时间的内容。让我们将这个额外的行为添加到我们的建议中：

    ```java
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
        return proceed;
    }
    ```

    同样，我们在这里没有做任何特别复杂的事情。我们刚刚记录了当前时间，执行了方法，然后将花费的时间打印到控制台。我们还记录了方法签名，它是为使用连接点实例而提供的。如果我们愿意，我们还可以访问其他信息，例如方法参数。

    现在，让我们尝试用 @LogExecutionTime 注释一个方法，然后执行它来看看会发生什么。请注意，这必须是 Spring Bean 才能正常工作：

    ```java
    @LogExecutionTime
    public void serve() throws InterruptedException {
        Thread.sleep(2000);
    }
    ```

    执行后，我们应该会看到控制台记录了以下内容：

    `void org.baeldung.Service.serve() executed in 2030ms`

5. 结论

    在本文中，我们利用 Spring Boot AOP 创建了自定义注解，我们可以将其应用于 Spring Bean，在运行时为它们注入额外的行为。
