# [Spring中的Advice类型介绍](https://www.baeldung.com/spring-aop-advice-tutorial)

可以在 Spring 中创建的不同类型的 AOP Advice 。

Advice 是方面在特定连接点采取的行动。 不同类型的建议包括“around”、“before”和“after” Advice。

方面的主要目的是支持横切关注点，例如日志记录、分析、缓存和事务管理。

## 启用 Advice

使用 Spring，您可以使用 AspectJ 注释声明建议，但您必须首先将 `@EnableAspectJAutoProxy` 注释应用于您的配置类，这将支持处理标记有 AspectJ 的 @Aspect 注释的组件。

在 Spring Boot 项目中，我们不必显式使用 `@EnableAspectJAutoProxy`。 如果 Aspect 或 Advice 在类路径上，则有一个专用的 [AopAutoConfiguration](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/aop/AopAutoConfiguration.html) 启用 Spring 的 AOP 支持。

## Before Advice

顾名思义，这个建议是在连接点之前执行的。 除非抛出异常，否则它不会阻止它建议的方法的继续执行。

考虑以下方面，它在调用之前简单地记录方法名称：pointcutadvice.LoggingAspect.java

```java
@Component
@Aspect
...
    @Pointcut("@target(org.springframework.stereotype.Repository)")
    public void repositoryMethods() {};

    @Before("repositoryMethods()")
    public void logMethodCall(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        logger.info("Before " + methodName);
    }
...
```

`logMethodCall` 建议将在 `repositoryMethods` 切入点定义的任何存储库方法之前执行。

## After Advice

使用 `@After` 注释声明的 After 通知在匹配的方法执行后执行，无论是否引发异常。

在某些方面，它类似于 finally 块。 如果您需要仅在正常执行后触发通知，则应使用 `@AfterReturning` 注释声明的返回通知。 如果您希望仅在目标方法抛出异常时触发您的通知，您应该使用抛出通知，通过使用 `@AfterThrowing` 注释声明。

假设我们希望在创建 Foo 的新实例时通知某些应用程序组件。 我们可以从 FooDao 发布一个事件，但这违反了单一责任原则。

相反，我们可以通过定义以下方面来实现这一点：pointcutadvice.PublishingAspect.java

```java
...
    @Pointcut("repositoryMethods() && firstLongParamMethods()")
    public void entityCreationMethods() {
    }

    @AfterReturning(value = "entityCreationMethods()", returning = "entity")
    public void logMethodCall(JoinPoint jp, Object entity) throws Throwable {
        eventPublisher.publishEvent(new FooCreationEvent(entity));
    }
...
```

请注意，首先，通过使用 `@AfterReturning` 注解，我们可以访问目标方法的返回值。 其次，通过声明 JoinPoint 类型的参数，我们可以访问目标方法调用的参数。

接下来创建一个监听器，它将简单地记录[事件](https://www.baeldung.com/spring-events)： pointcutadvice.events.FooCreationEventListener.java

## Around Advice

环绕建议围绕一个连接点，例如方法调用。

这是最有力的建议。 环绕通知可以在方法调用之前和之后执行自定义行为。 它还负责选择是继续到连接点还是通过提供自己的返回值或抛出异常来缩短建议的方法执行。

为了演示它的使用，假设我们要测量方法执行时间。 让我们为此创建一个方面：pointcutadvice.PerformanceAspect.java

当执行与 repositoryClassMethods 切入点匹配的任何连接点时，将触发此建议。

该建议采用 ProceedingJointPoint 类型的一个参数。 该参数使我们有机会在目标方法调用之前采取行动。 在这种情况下，我们只需保存方法开始时间。

其次，通知返回类型是 Object，因为目标方法可以返回任何类型的结果。 如果目标方法为 void，则返回 null。 在目标方法调用之后，我们可以测量时间，记录下来，并将方法的结果值返回给调用者。

## 概述

在本文中，我们了解了 Spring 中不同类型的建议及其声明和实现。我们使用基于模式的方法和 AspectJ 注释定义了各个方面。我们还提供了几种可能的建议应用。
