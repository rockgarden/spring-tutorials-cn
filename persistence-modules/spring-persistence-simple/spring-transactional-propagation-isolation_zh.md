# [Spring @Transactional中的事务传播和隔离](https://www.baeldung.com/spring-transactional-propagation-isolation)

1. 介绍

    在本教程中，我们将介绍@Transactional注释，以及其隔离和传播设置。

2. 什么是@Transactional？

    我们可以使用@Transactional在数据库事务中包装方法。

    它允许我们为交易设置传播、隔离、超时、只读和回滚条件。我们还可以指定交易经理。

    1. @Transactional实施细节

        Spring创建代理，或操作类字节码，以管理事务的创建、提交和回滚。在代理的情况下，Spring在内部方法调用中忽略@Transactional。

        简单地说，如果我们有一个像callMethod这样的方法，并将其标记为@Transactional，Spring将围绕invocation@Transactional方法包装一些事务管理代码，称为：

        ```java
        createTransactionIfNecessary();
        try {
            callMethod();
            commitTransactionAfterReturning();
        } catch (exception) {
            completeTransactionAfterThrowing();
            throw exception;
        }
        ```

    2. 如何使用@Transactional

        我们可以将注释放在接口、类的定义上，或直接放在方法上。它们根据优先级顺序相互覆盖；从最低到最高，我们有：接口、超类、类、接口方法、超类方法和类方法。

        Spring将类级注释应用于该类的所有公共方法，我们没有用@Transactional注释。

        然而，如果我们将注释放在私有或受保护的方法上，Spring将忽略它，不会出错。

        让我们从一个接口示例开始：

        ```java
        @Transactional
        public interface TransferService {
            void transfer(String user1, String user2, double val);
        }
        ```

        通常不建议在界面上设置@Transactional；然而，对于带有Spring Data的@Repository等情况，这是可以接受的。我们可以将注释放在类定义上，以覆盖接口/超类的事务设置：

        ```java
        @Service
        @Transactional
        public class TransferServiceImpl implements TransferService {
            @Override
            public void transfer(String user1, String user2, double val) {
                // ...
            }
        }
        ```

        现在让我们直接在方法上设置注释来覆盖它：

        ```java
        @Transactional
        public void transfer(String user1, String user2, double val) {
            // ...
        }
        ```

3. 交易传播(Transaction Propagation)

    传播定义了我们业务逻辑的交易边界。Spring根据我们的传播设置来启动和暂停交易。

    Spring调用TransactionManager::getTransaction，根据传播获取或创建事务。它支持所有类型的TransactionManager的一些传播，但其中有一些只受TransactionManager的特定实现支持。

    让我们来了解一下不同的传播及其工作原理。

    1. REQUIRED传播

        必需的是默认的传播。Spring检查是否有活动交易，如果没有活动交易，则会创建一个新的交易。否则，业务逻辑将附加到当前活动事务中：

        ```java
        @Transactional(propagation = Propagation.REQUIRED)
        public void requiredExample(String user) {
            // ...
        }
        ```

        此外，由于REQUIRED是默认的传播，我们可以通过删除它来简化代码：

        ```java
        @Transactional
        public void requiredExample(String user) {
            // ...
        }
        ```

        让我们来看看事务创建如何为所需传播工作的伪代码：

        ```java
        if (isExistingTransaction()) {
            if (isValidateExistingTransaction()) {
                validateExisitingAndThrowExceptionIfNotValid();
            }
            return existing;
        }
        return createNewTransaction();
        ```

    2. SUPPORTS传播

        对于支持，Spring首先检查是否存在活动事务。如果存在交易，那么将使用现有交易。如果没有交易，则以非交易的方式执行：

        ```java
        @Transactional(propagation = Propagation.SUPPORTS)
        public void supportsExample(String user) {
            // ...
        }
        ```

        让我们来看看支持事务创建的伪代码：

        ```java
        if (isExistingTransaction()) {
            if (isValidateExistingTransaction()) {
                validateExisitingAndThrowExceptionIfNotValid();
            }
            return existing;
        }
        return emptyTransaction;
        ```

    3. MANDATORY传播

        当传播是强制性的，如果有活动交易，那么它将被使用。如果没有活动事务，那么Spring会抛出一个异常：

        ```java
        @Transactional(propagation = Propagation.MANDATORY)
        public void mandatoryExample(String user) {
            // ...
        }
        ```

        让我们再看看伪代码：

        ```java
        if (isExistingTransaction()) {
            if (isValidateExistingTransaction()) {
                validateExisitingAndThrowExceptionIfNotValid();
            }
            return existing;
        }
        throw IllegalTransactionStateException;
        ```

    4. NEVER传播

        对于具有NEVER传播的事务逻辑，如果有活动事务，Spring会抛出一个异常：

        ```java
        @Transactional(propagation = Propagation.NEVER)
        public void neverExample(String user) {
            // ...
        }
        ```

        让我们看看交易创建如何为NEVER传播工作的伪代码：

        ```java
        if (isExistingTransaction()) {
            throw IllegalTransactionStateException;
        }
        return emptyTransaction;
        ```

    5. NOT_SUPPORTED传播

        如果当前事务存在，首先Spring会暂停它，然后在没有事务的情况下执行业务逻辑：

        ```java
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void notSupportedExample(String user) {
            // ...
        }
        ```

        JTATransactionManager支持开箱即用的真实交易暂停。其他人通过保留对现有引用，然后从线程上下文中清除它来模拟暂停

    6. REQUIRES_NEW传播

        当传播为REQUIRE_NEW时，Spring会暂停当前事务（如果存在），然后创建一个新的事务：

        ```java
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void requiresNewExample(String user) {
            // ...
        }
        ```

        与NOT_SUPPORTED类似，我们需要JTATransactionManager来进行实际交易暂停。

        伪代码看起来像这样：

        ```java
        if (isExistingTransaction()) {
            suspend(existing);
            try {
                return createNewTransaction();
            } catch (exception) {
                resumeAfterBeginException();
                throw exception;
            }
        }
        return createNewTransaction();
        ```

    7. NESTED传播

        对于NESTED传播，Spring检查事务是否存在，如果有，它会标记一个保存点。这意味着，如果我们的业务逻辑执行抛出异常，那么事务将回滚到此保存点。如果没有活动事务，它就像必填一样工作。

        DataSourceTransactionManager支持这种开箱即用的传播。JTATransactionManager的一些实现也可能支持这一点。

        [JpaTransactionManager](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/orm/jpa/JpaTransactionManager.html)仅支持NESTED用于JDBC连接。然而，如果我们将nestedTransactionAllowed标志设置为true，如果我们的JDBC驱动程序支持保存点，它也适用于JPA事务中的JDBC访问代码。

        最后，让我们将传播设置为嵌套：

        ```java
        @Transactional(propagation = Propagation.NESTED)
        public void nestedExample(String user) {
            // ...
        }
        ```

4. 事务隔离

    分离是常见的ACID特性之一：原子性、稠度、分离性和耐用性。隔离描述了并发交易所应用的更改如何相互可见。

    每个隔离级别都可防止对交易产生零或更多并发副作用：

    - 脏(Dirty)读取：阅读并发交易的未承诺更改
    - 不可重复读取：如果并发事务更新同一行并提交，则在重新读取行时获得不同的值
    - 幻影(Phantom)读取：如果另一个事务在范围内添加或删除范围内的一些行并提交，在重新执行范围查询后获取不同的行

    我们可以通过@Transactional::isolation来设置事务的隔离级别。它在Spring:DEFAULT、READ_UNCOMMITTED、READ_COMMITTED、REPEATABLE_READ、SERIALIZABLE中有这五个枚举。

    1. Spring隔离管理

        默认隔离级别为DEFAULT。因此，当Spring创建新事务时，隔离级别将是我们RDBMS的默认隔离。因此，如果我们更改数据库，我们应该小心。

        我们还应该考虑我们调用具有不同隔离方法的方法链的情况。在正常流程中，隔离仅在创建新事务时适用。因此，如果出于任何原因，我们不想允许方法在不同的隔离下执行，我们必须将TransactionManager::setValidateExistingTransaction设置为true。

        然后，交易验证的伪代码将是：

        ```java
        if (isolationLevel != ISOLATION_DEFAULT) {
            if (currentTransactionIsolationLevel() != isolationLevel) {
                throw IllegalTransactionStateException
            }
        }
        ```

        现在让我们深入了一下不同的隔离水平及其影响。

    2. READ_UNCOMMITTED隔离

        READ_UNCOMMITTED是最低的隔离级别，允许最同时访问。

        因此，它遭受了上述所有三种并发副作用。具有此隔离的事务会读取其他并发事务的未提交数据。此外，不可重复的和幻象读取都可能发生。因此，我们可以在重新读取行或重新执行范围查询时获得不同的结果。

        我们可以为方法或类设置隔离级别：

        ```java
        @Transactional(isolation = Isolation.READ_UNCOMMITTED)
        public void log(String message) {
            // ...
        }
        ```

        Postgres不支持READ_UNCOMMITTED隔离，而是回退到READ_COMMITED。此外，甲骨文不支持或允许READ_UNCOMMITTED。

    3. READ_COMMITTED隔离

        第二级隔离，READ_COMMITTED，防止脏读。

        其余的并发副作用仍然可能发生。因此，并发交易中未提交的更改不会对我们产生影响，但如果交易提交更改，我们的结果可能会因重新查询而更改。

        在这里，我们设置了隔离级别：

        ```java
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public void log(String message){
            // ...
        }
        ```

        READ_COMMITTED是Postgres、SQL Server和Oracle的默认级别。

    4. REPEATABLE_READ隔离

        第三级隔离，REPEATABLE_READ，防止肮脏和不可重复的读取。因此，我们不会受到并发交易中未承诺的更改的影响。

        此外，当我们重新查询一行时，我们不会得到不同的结果。然而，在重新执行范围查询时，我们可能会收到新添加或删除的行。

        此外，这是防止丢失更新的最低要求级别。当两个或两个以上并发事务读取并更新同一行时，就会发生丢失的更新。REPEATABLE_READ完全不允许同时访问一行。因此，丢失的更新不可能发生。

        以下是设置方法隔离级别的方法：

        ```java
        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void log(String message){
            // ...
        }
        ```

        REPEATABLE_READ是Mysql的默认级别。甲骨文不支持REPEATABLE_READ。

    5. SERIALIZABLE隔离

        可序列是最高级别的隔离。它防止了所有提到的并发副作用，但可能会导致最低的并发访问率，因为它按顺序执行并发调用。

        换句话说，一组可序列化事务的并发执行与串行执行它们的结果相同。

        现在让我们看看如何将SERIALIZABLE设置为隔离级别：

        ```java
        @Transactional(isolation = Isolation.SERIALIZABLE)
        public void log(String message){
            // ...
        }
        ```

5. 结论

    在本文中，我们详细探讨了@Transaction的传播属性。然后我们了解了并发的副作用和隔离水平。
