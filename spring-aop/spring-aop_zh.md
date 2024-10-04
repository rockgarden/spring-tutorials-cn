# [Spring AOP简介](https://www.baeldung.com/spring-aop)

1. 介绍

    在本教程中，我们将介绍带有Spring的AOP（面向方向的编程），并学习如何在实际场景中使用这个强大的工具。

    使用Spring AOP开发时，也可以利用AspectJ的注释，但在本文中，我们将重点关注基于Spring AOP XML的核心配置。

2. 一览表

    AOP是一种编程范式，旨在通过允许交叉问题分离来增加模块化。它通过在不修改代码本身的情况下向现有代码添加其他行为来完成此操作。

    相反，我们可以分别声明新代码和新行为。

    Spring的AOP框架帮助我们实施这些交叉问题。

3. Maven附属机构

    让我们从在pom.xml中添加Spring的AOP库依赖项开始：

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
    </dependencies>
    ```

4. AOP概念和术语

    让我们简单回顾一下AOP特有的概念和术语：

    ![程序执行](pic/Program_Execution.jpg)

    1. 商业目标

        业务对象是一个具有正常业务逻辑的正常类。让我们来看看一个简单的业务对象示例，我们只需添加两个数字：

        ```java
        public class SampleAdder {
            public int add(int a, int b) {
                return a + b;
            }
        }
        ```

        请注意，该类是具有业务逻辑的普通类，没有任何与Spring相关的注释。

    2. Aspect

        一个方面是跨越多个类别的关注的模块化。统一日志记录可以成为这种交叉关注的一个例子。

        让我们看看我们如何定义一个简单的方面：

        ```java
        public class AdderAfterReturnAspect {
            private Logger logger = LoggerFactory.getLogger(this.getClass());
            public void afterReturn(Object returnValue) throws Throwable {
                logger.info("value return was {}",  returnValue);
            }
        }
        ```

        在上述示例中，我们定义了一个简单的Java类，该类有一个名为afterReturn的方法，该方法需要一个对象类型的参数并记录该值。请注意，即使是我们的AdderAfterReturnAspect也是一个标准类，没有任何Spring注释。

        在接下来的章节中，我们将看看如何将这一方面连接到我们的业务对象。

    3. Joinpoint

        连接点是程序执行过程中的一个点，例如执行方法或处理异常。

        在Spring AOP中，JoinPoint总是代表方法执行。

    4. Pointcut

        Pointcut是一个谓词，有助于匹配Aspect在特定JoinPoint上应用的建议。

        我们经常将建议与Pointcut表达式相关联，它在与Pointcut匹配的任何Joinpoint上运行。

    5. Advice

        建议是在特定Joinpoint上由某个方面采取的行动。不同类型的建议包括“around”、“before”和“after”。

        在Spring中，建议被建模为拦截器，在Joinpoint周围保持一个拦截器链。

    6. 接线业务Object和Aspect

        现在，让我们看看如何通过退货后建议将业务对象连接到一个方面。

        以下是我们将在 `<beans>` 标签中的标准Spring配置中的配置摘录：

        ```xml
        <bean id="sampleAdder" class="org.baeldung.logger.SampleAdder" />
        <bean id="doAfterReturningAspect" 
        class="org.baeldung.logger.AdderAfterReturnAspect" />
        <aop:config>
            <aop:aspect id="aspects" ref="doAfterReturningAspect">
            <aop:pointcut id="pointCutAfterReturning" expression=
                "execution(* org.baeldung.logger.SampleAdder+.*(..))"/>
            <aop:after-returning method="afterReturn"
                returning="returnValue" pointcut-ref="pointCutAfterReturning"/>
            </aop:aspect>
        </aop:config>
        ```

        正如我们所看到的，我们定义了一个名为simpleAdder的简单bean，它代表业务对象的实例。此外，我们创建了一个名为AdderAfterReturnAspect的Aspect实例。

        当然，XML不是我们在这里唯一的选择；如前所述，AspectJ注释也完全支持。

    7. 概览配置

        我们可以使用标签aop:config来定义与AOP相关的配置。在配置标签中，我们定义了表示一个方面的类。然后我们引用了“doAfterReturningAspect”，这是我们创建的方面bean。

        接下来，我们使用pointcut标签定义Pointcut。上述示例中使用的点切是`execution(* org.baeldung.logger.SampleAdder+.*(..))`，这意味着对SampleAdder类中接受任意数量的参数并返回任何值类型的方法应用建议。

        然后我们定义了我们想要应用的建议。在上述示例中，我们应用了返回后的建议。我们通过执行使用属性方法定义的afterReturn方法，在Aspect AdderAfterReturnAspect中定义了这一点。

        Aspect中的这个建议需要一个对象类型的参数。该参数让我们有机会在目标方法调用之前和/或之后采取行动。在这种情况下，我们只是记录该方法的返回值。

        Spring AOP支持使用基于注释的配置的多种类型的建议。

        ![pringAop-applicationContext.xml](./src/main/resources/com.baeldung.logger/springAop-applicationContext.xml)

5. 结论

    在本文中，我们说明了AOP中使用的概念。我们还查看了使用Spring的AOP模块的示例。
