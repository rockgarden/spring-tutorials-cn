# [Spring 中的程序化事务管理](https://www.baeldung.com/spring-programmatic-transaction-management)

1. 一览表

    Spring的@Transactional注释提供了一个很好的声明API来标记交易边界。

    在幕后，一个方面负责创建和维护交易，因为它们在@Transactional注释的每次出现中都定义。这种方法使我们的核心业务逻辑与交易管理等[交叉问题脱钩](https://en.wikipedia.org/wiki/Cross-cutting_concern)。

    在本教程中，我们将看到这并不总是最好的方法。我们将探索Spring提供的编程替代方案，如TransactionTemplate，以及我们使用它们的原因。

2. 天堂里的麻烦

    假设我们在一个简单的服务中混合了两种不同类型的I/O：

    ```java
    @Transactional
    public void initialPayment(PaymentRequest request) {
        savePaymentRequest(request); // DB
        callThePaymentProviderApi(request); // API
        updatePaymentState(request); // DB
        saveHistoryForAuditing(request); // DB
    }
    ```

    在这里，我们有一些数据库调用，以及一个可能昂贵的REST API调用。乍一看，使整个方法具有事务性可能是有意义的，因为我们可能想使用一个实体管理器来原子执行整个操作。

    然而，如果由于任何原因，外部API响应的时间比平时长，我们可能很快就会耗尽数据库连接！

    1. 现实的严酷本质

        以下是我们调用初始付款方法时发生的事情：

        - 事务方面创建了一个新的实体管理器并启动了一个新的事务，因此它从连接池中借用了oneConnection。
        - 在第一次数据库调用后，它会调用外部API，同时保持借用的连接。
        - 最后，它使用该连接来执行剩余的数据库调用。

        如果API调用在一段时间的响应速度非常慢，此方法将在等待响应时囤积借来的连接。

        想象一下，在此期间，我们接到了对初始支付方法的突发调用。在这种情况下，所有连接都可能等待API调用的响应。这就是为什么我们可能会耗尽数据库连接——因为后端服务缓慢！

        在事务环境中将数据库I/O与其他类型的I/O混合不是一个好主意。因此，解决此类问题的第一个解决方案是将这些类型的I/O完全分开。如果出于任何原因，我们无法将它们分开，我们仍然可以使用Spring API来手动管理事务。

3. 使用交易模板

    TransactionTemplate提供了一组基于回调的API来手动管理事务。为了使用它，我们应该首先使用PlatformTransactionManager初始化它。

    我们可以使用依赖注入来设置此模板：

    ```java
    // test annotations
    class ManualTransactionIntegrationTest {

        @Autowired
        private PlatformTransactionManager transactionManager;

        private TransactionTemplate transactionTemplate;

        @BeforeEach
        void setUp() {
            transactionTemplate = new TransactionTemplate(transactionManager);
        }

        // omitted
    }
    ```

    PlatformTransactionManager帮助模板创建、提交或回滚交易。

    使用Spring Boot时，PlatformTransactionManager类型的适当bean将自动注册，因此我们只需要注入它。否则，我们应该手动注册PlatformTransactionManager bean。

    1. 示例域模型

        从现在开始，为了演示，我们将使用简化的支付域模型。

        在这个简单的域中，我们有一个付款实体来封装每次付款的详细信息：

        ```java
        @Entity
        public class Payment {

            @Id
            @GeneratedValue
            private Long id;

            private Long amount;

            @Column(unique = true)
            private String referenceNumber;

            @Enumerated(EnumType.STRING)
            private State state;

            // getters and setters

            public enum State {
                STARTED, FAILED, SUCCESSFUL
            }
        }
        ```

        此外，我们将在每个测试用例之前初始化TransactionTemplate实例：

        ```java
        @DataJpaTest
        @ActiveProfiles("test")
        @Transactional(propagation = NOT_SUPPORTED) // we're going to handle transactions manually
        public class ManualTransactionIntegrationTest {

            @Autowired 
            private PlatformTransactionManager transactionManager;

            @Autowired 
            private EntityManager entityManager;

            private TransactionTemplate transactionTemplate;

            @BeforeEach
            public void setUp() {
                transactionTemplate = new TransactionTemplate(transactionManager);
            }

            // tests
        }
        ```

    2. 结果Transactions

        TransactionTemplate提供了一个名为execute的方法，该方法可以在事务中运行任何给定的代码块，然后返回一些结果：

        ```java
        @Test
        void givenAPayment_WhenNotDuplicate_ThenShouldCommit() {
            Long id = transactionTemplate.execute(status -> {
                Payment payment = new Payment();
                payment.setAmount(1000L);
                payment.setReferenceNumber("Ref-1");
                payment.setState(Payment.State.SUCCESSFUL);

                entityManager.persist(payment);

                return payment.getId();
            });

            Payment payment = entityManager.find(Payment.class, id);
            assertThat(payment).isNotNull();
        }
        ```

        在这里，我们将一个新的付款实例保留到数据库中，然后返回其自动生成的ID。

        与声明方法类似，模板可以保证我们的原子性。

        如果交易中的一项操作无法完成，它会回滚所有操作：

        ```java
        @Test
        void givenTwoPayments_WhenRefIsDuplicate_ThenShouldRollback() {
            try {
                transactionTemplate.execute(status -> {
                    Payment first = new Payment();
                    first.setAmount(1000L);
                    first.setReferenceNumber("Ref-1");
                    first.setState(Payment.State.SUCCESSFUL);

                    Payment second = new Payment();
                    second.setAmount(2000L);
                    second.setReferenceNumber("Ref-1"); // same reference number
                    second.setState(Payment.State.SUCCESSFUL);

                    entityManager.persist(first); // ok
                    entityManager.persist(second); // fails

                    return "Ref-1";
                });
            } catch (Exception ignored) {}

            assertThat(entityManager.createQuery("select p from Payment p").getResultList()).isEmpty();
        }
        ```

        由于第二个引用编号是重复的，数据库拒绝第二个持久操作，导致整个事务回滚。因此，数据库不包含交易后的任何付款。

        也可以通过在[TransactionStatus](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/TransactionStatus.html)上调用setRollbackOnly()来手动触发回滚：

        ```java
        @Test
        void givenAPayment_WhenMarkAsRollback_ThenShouldRollback() {
            transactionTemplate.execute(status -> {
                Payment payment = new Payment();
                payment.setAmount(1000L);
                payment.setReferenceNumber("Ref-1");
                payment.setState(Payment.State.SUCCESSFUL);

                entityManager.persist(payment);
                status.setRollbackOnly();

                return payment.getId();
            });

            assertThat(entityManager.createQuery("select p from Payment p").getResultList()).isEmpty();
        }
        ```

    3. 没有结果的Transactions

        如果我们不打算从事务中返回任何东西，我们可以使用[TransactionCallbackWithoutResult](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionCallbackWithoutResult.html)回调类：

        ```java
        @Test
        void givenAPayment_WhenNotExpectingAnyResult_ThenShouldCommit() {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Payment payment = new Payment();
                    payment.setReferenceNumber("Ref-1");
                    payment.setState(Payment.State.SUCCESSFUL);

                    entityManager.persist(payment);
                }
            });

            assertThat(entityManager.createQuery("select p from Payment p").getResultList()).hasSize(1);
        }
        ```

    4. 自定义Transactions配置

        到目前为止，我们使用具有默认配置的TransactionTemplate。尽管大多数时候这个默认值已经足够了，但仍然可以更改配置设置。

        让我们来设置交易隔离级别：

        ```java
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        ```

        同样，我们可以改变交易传播行为：

        `transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);`

        或者我们可以为交易设置超时，以秒为单位：

        `transactionTemplate.setTimeout(1000);`

        甚至有可能从只读事务的优化中受益：

        `transactionTemplate.setReadOnly(true);`

        一旦我们创建了带有配置的事务模板，所有事务都将使用该配置来执行。因此，如果我们需要多个配置，我们应该创建多个模板实例。

4. 使用PlatformTransactionManager

    除了TransactionTemplate外，我们还可以使用更低级别的API，如[PlatformTransactionManager](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/PlatformTransactionManager.html)来手动管理交易。有趣的是，@Transactional和TransactionTemplate都使用此API来内部管理其事务。

    1. 配置Transactions

        在使用此API之前，我们应该定义我们的交易将是什么样子。

        让我们用可重复的读取事务隔离级别设置三秒的超时：

        ```java
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        definition.setTimeout(3);
        ```

        事务定义与事务模板配置相似。然而，我们只需一个PlatformTransactionManager就可以使用多个定义。

    2. 维护Transactions

        配置好事务后，我们就可以通过编程来管理事务了：

        ```java
        @Test
        void givenAPayment_WhenUsingTxManager_ThenShouldCommit() {

            // transaction definition

            TransactionStatus status = transactionManager.getTransaction(definition);
            try {
                Payment payment = new Payment();
                payment.setReferenceNumber("Ref-1");
                payment.setState(Payment.State.SUCCESSFUL);

                entityManager.persist(payment);
                transactionManager.commit(status);
            } catch (Exception ex) {
                transactionManager.rollback(status);
            }

            assertThat(entityManager.createQuery("select p from Payment p").getResultList()).hasSize(1);
        }
        ```

5. 结论

    在本文中，我们首先看到了何时应该选择程序化交易管理而不是声明式方法。

    然后，通过引入两个不同的API，我们学习了如何手动创建、提交或回滚任何给定的事务。
