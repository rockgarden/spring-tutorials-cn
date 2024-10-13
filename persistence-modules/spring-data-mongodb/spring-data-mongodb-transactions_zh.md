# [Spring Data MongoDB交易](https://www.baeldung.com/spring-data-mongodb-transactions)

1. 一览表

    从4.0版本开始，MongoDB支持多文档ACID事务。而且，Spring Data Lovelace现在为这些原生MongoDB事务提供支持。

    在本教程中，我们将讨论Spring Data MongoDB对同步和反应性事务的支持。

    我们还将查看Spring Data TransactionTemplate，以支持非本机事务。

2. 设置MongoDB 4.0

    首先，我们需要设置最新的MongoDB来尝试新的原生事务支持。

    要开始，我们必须从MongoDB[下载中心](https://www.mongodb.com/download-center?initial=true#atlas)下载最新版本。

    接下来，我们将使用命令行启动mongod服务：

    `mongod --replSet rs0`

    最后，启动副本集——如果尚未启动：

    `mongo --eval "rs.initiate()"`

    请注意，MongoDB目前支持通过副本集进行事务。

3. Maven配置

    接下来，我们需要将以下依赖项添加到我们的pom.xml中：

    ```xml
    <dependency> 
        <groupId>org.springframework.data</groupId> 
        <artifactId>spring-boot-starter-data-mongodb</artifactId> 
        <version>2.7.11</version> 
    </dependency>
    ```

4. MongoDB配置

    现在，让我们来看看我们的配置：

    ```java
    @Configuration
    @EnableMongoRepositories(basePackages = "com.baeldung.repository")
    public class MongoConfig extends AbstractMongoClientConfiguration{

        @Bean
        MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
            return new MongoTransactionManager(dbFactory);
        }

        @Override
        protected String getDatabaseName() {
            return "test";
        }

        @Override
        public MongoClient mongoClient() {
            final ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/test");
            final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
            return MongoClients.create(mongoClientSettings);
        }
    }
    ```

    请注意，我们需要在配置中注册MongoTransactionManager，以启用原生MongoDB事务，因为它们默认禁用。

5. 同步交易

    完成配置后，我们只需要用@Transactional注释我们的方法来使用原生MongoDB事务。

    注释方法中的所有内容都将在一次事务中执行：

    ```java
    @Test
    @Transactional
    public void whenPerformMongoTransaction_thenSuccess() {
        userRepository.save(new User("John", 30));
        userRepository.save(new User("Ringo", 35));
        Query query = new Query().addCriteria(Criteria.where("name").is("John"));
        List<User> users = mongoTemplate.find(query, User.class);

        assertThat(users.size(), is(1));
    }
    ```

    请注意，我们不能在多文档事务中使用listCollections命令——例如：

    ```java
    @Test(expected = MongoTransactionException.class)
    @Transactional
    public void whenListCollectionDuringMongoTransaction_thenException() {
        if (mongoTemplate.collectionExists(User.class)) {
            mongoTemplate.save(new User("John", 30));
            mongoTemplate.save(new User("Ringo", 35));
        }
    }
    ```

    当我们使用collectionExists（）方法时，这个例子抛出了MongoTransactionException。

6. 交易模板

    我们看到了Spring Data如何支持新的MongoDB原生事务。此外，Spring Data还提供非原生选项。

    我们可以使用Spring Data TransactionTemplate执行非原生事务：

    ```java
    @Test
    public void givenTransactionTemplate_whenPerformTransaction_thenSuccess() {
        mongoTemplate.setSessionSynchronization(SessionSynchronization.ALWAYS);                                     

        TransactionTemplate transactionTemplate = new TransactionTemplate(mongoTransactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                mongoTemplate.insert(new User("Kim", 20));
                mongoTemplate.insert(new User("Jack", 45));
            };
        });

        Query query = new Query().addCriteria(Criteria.where("name").is("Jack")); 
        List<User> users = mongoTemplate.find(query, User.class);

        assertThat(users.size(), is(1));
    }
    ```

    我们需要将SessionSynchronization设置为ALWAYS，以使用非原生Spring数据事务。

7. 被动交易

    最后，我们将看看Spring Boot对MongoDB反应事务的Spring Data支持。

    我们需要在pom.xml中添加一些依赖项，以便与反应式MongoDB配合使用：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        <version>2.7.11</version>
    </dependency>

    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <version>3.5.4</version>
        <scope>test</scope>
    </dependency>
    ```

    在Maven Central上可以使用spring-boot-starter-data-mongodb和reactor-test依赖项。

    当然，我们需要配置我们的Reactive MongoDB：

    ```java
    @Configuration
    @EnableReactiveMongoRepositories(basePackages = "com.baeldung.reactive.repository")
    public class MongoReactiveConfig extends AbstractReactiveMongoConfiguration {

        @Override
        public MongoClient reactiveMongoClient() {
            return MongoClients.create();
        }

        @Override
        protected String getDatabaseName() {
            return "reactive";
        }

        @Bean
        public TransactionalOperator transactionalOperator(
        ReactiveTransactionManager reactiveTransactionManager) {
            return TransactionalOperator.create(reactiveTransactionManager);
        }
    }
    ```

    要在reactive MongoDB中使用事务，我们可以将TransactionalOperator和ReactiveMongoTemplate类组合如下：

    ```java
    @Autowired
    private TransactionalOperator transactionalOperator;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Test
    public void whenPerformTransaction_thenSuccess() {
        User user1 = new User("Jane", 23);
        User user2 = new User("John", 34);

        Mono<User> saveEntity1 = mongoTemplate.save(user1);
        Mono<User> saveEntity2 = mongoTemplate.save(user2);

        saveEntity1.then(saveEntity2).then().as(transactionalOperator::transactional);
    }
    ```

8. 结论

    在这篇文章中，我们学习了如何使用Spring Data的本地和非原生MongoDB事务。
