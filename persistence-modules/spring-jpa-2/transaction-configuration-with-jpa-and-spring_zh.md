# [与Spring和JPA的交易](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)

1. 一览表

    本教程将讨论配置Spring Transactions的正确方法、如何使用@Transactional注释和常见的陷阱。

    基本上，有两种不同的方法来配置事务、注释和AOP，每种方法都有自己的优势。我们将在这里讨论更常见的注释配置。

2. 配置交易

    Spring 3.1引入了@EnableTransactionManagement注释，我们可以在@Configuration类中使用该注释来启用事务支持：

    ```java
    @Configuration
    @EnableTransactionManagement
    public class PersistenceJPAConfig{

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        //...
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
    }
    ```

    但是，如果我们使用Spring Boot项目，并且在类路径上有spring-data-*或spring-tx依赖项，那么默认情况下将启用事务管理。

3. 使用XML配置事务

    对于3.1之前的版本，或者如果Java不是选项，以下是使用注释驱动和命名空间支持的XML配置：

    ```xml
    <bean id="txManager" class="org.springframework.orm.jpa.JpaTransactionManager">
    <property name="entityManagerFactory" ref="myEmf" />
    </bean>
    <tx:annotation-driven transaction-manager="txManager" />
    ```

4. @Transactional注释

    配置了事务，我们现在可以在类或方法级别使用@Transactional注释bean：

    ```java
    @Service
    @Transactional
    public class FooService {
        //...
    }
    ```

    注释也支持进一步的配置：

    - 交易的传播类型
    - 交易的隔离级别
    - 交易包裹的操作超时
    - 只读标志——给持久性提供程序的提示，即事务应该是只读的
    - 交易的回滚规则

    请注意，默认情况下，回滚发生在运行时，只有未选中的例外情况。选中的异常不会触发事务的回滚。当然，我们可以使用rollbackFor和noRollbackFor注释参数来配置此行为。

5. 潜在的陷阱

    1. 交易和代理

        在高层次上，Spring为所有用@Transactional注释的类创建代理，无论是在类上还是在任何方法上。代理允许框架在运行方法之前和之后注入事务逻辑，主要用于启动和提交事务。

        重要的是要记住，如果事务bean正在实现接口，默认情况下，代理将是Java动态代理。这意味着只有通过代理传入的外部方法调用才会被拦截。任何自我调用调用都不会启动任何事务，即使该方法有@Transactional注释。

        使用代理的另一个注意事项是，只有公共方法才应该用@Transactional进行注释。任何其他能看性的方法将简单地无声地忽略注释，因为这些不是代理的。

    2. 改变隔离级别

        `courseDao.createWithRuntimeException(course);`

        我们还可以更改交易隔离级别：

        `@Transactional(isolation = Isolation.SERIALIZABLE)`

        请注意，这实际上是在Spring 4.1中引入的；如果我们在Spring 4.1之前运行上述示例，将导致：

        `org.springframework.transaction.InvalidIsolationLevelException: Standard JPA does not support custom isolation levels – use a special JpaDialect for your JPA implementation`

    3. 只读交易

        只读标志通常会产生混乱，特别是在使用JPA时。来自Javadoc：

        `This just serves as a hint for the actual transaction subsystem; it will not necessarily cause failure of write access attempts. A transaction manager which cannot interpret the read-only hint will not throw an exception when asked for a read-only transaction.`

        事实是，当设置只读标志时，我们无法确定是否会发生插入或更新。这种行为与供应商无关，而JPA与供应商无关。

        同样重要的是要明白，只读标志仅在交易中相关。如果操作发生在事务上下文之外，则该标志会被忽略。一个简单的例子是调用注释的方法：

        `@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)`

        从非事务上下文来看，交易将不会被创建，只读标志将被忽略。

    4. 交易日志

        微调事务包中的日志记录是了解交易相关问题的有用方法。Spring中的相关软件包是“org.springframework.transaction”，应配置为TRACE的日志记录级别。

    5. 交易回滚

        @Transactional注释是指定方法上事务语义的元数据。我们有两种方法来回滚交易：声明式和程序式。

        在声明式方法中，我们用@Transactional注释注释方法。@Transactional注释使用属性rollbackFor或rollbackForClassName来回滚事务，并使用属性noRollbackFor或noRollbackForClassName来避免对列出的异常进行回滚。

        声明式方法中的默认回滚行为将回滚运行时异常。

        让我们看看一个简单的示例，使用声明式方法回滚运行时异常或错误的事务：

        ```java
        @Transactional
        public void createCourseDeclarativeWithRuntimeException(Course course) {
            courseDao.create(course);
            throw new DataIntegrityViolationException("Throwing exception for demoing Rollback!!!");
        }
        ```

        接下来，我们将使用声明式方法为列出的检查异常回滚事务。回滚在我们的示例中是在SQLException上：

        ```java
        @Transactional(rollbackFor = { SQLException.class })
        public void createCourseDeclarativeWithCheckedException(Course course) throws SQLException {
            courseDao.create(course);
            throw new SQLException("Throwing exception for demoing rollback");
        }
        ```

        让我们看看在声明式方法中对属性noRollbackFor的简单使用，以防止对所列异常的事务回滚：

        ```java
        @Transactional(noRollbackFor = { SQLException.class })
        public void createCourseDeclarativeWithNoRollBack(Course course) throws SQLException {
            courseDao.create(course);
            throw new SQLException("Throwing exception for demoing rollback");
        }
        ```

        在编程方法中，我们使用TransactionAspectSupport回滚交易：

        ```java
        public void createCourseDefaultRatingProgramatic(Course course) {
            try {
            courseDao.create(course);
            } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        ```

        声明式回滚策略应该比程序化回滚策略受到青睐。

6. 结论

    在本文中，我们介绍了使用Java和XML的事务语义的基本配置。我们还学习了如何使用@Transactional，以及交易策略的最佳实践。
