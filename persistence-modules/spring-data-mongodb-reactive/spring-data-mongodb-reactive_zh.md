# [带有MongoDB的Spring Data Reactive存储库](https://www.baeldung.com/spring-data-mongodb-reactive)

1. 介绍

    在本教程中，我们将了解如何通过MongoDB的Spring Data Reactive Repositories使用Reactive Programming来配置和实现数据库操作。

    我们将了解ReactiveCrudRepository、ReactiveMongoRepository以及ReactiveMongoTemplate的基本用法。

    尽管这些实现使用无反应编程，但这不是本教程的主要重点。

2. 环境

    为了使用Reactive MongoDB，我们需要将依赖项添加到我们的pom.xml中。

    我们还将添加一个嵌入式MongoDB进行测试：

    ```xml
    <dependencies>
        // ...
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>de.flapdoodle.embed</groupId>
            <artifactId>de.flapdoodle.embed.mongo</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ```

3. 配置

    为了激活反应性支持，我们需要使用@EnableReactiveMongoRepositories以及一些基础设施设置：

    ```java
    @EnableReactiveMongoRepositories
    public class MongoReactiveApplication
    extends AbstractReactiveMongoConfiguration {

        @Bean
        public MongoClient mongoClient() {
            return MongoClients.create();
        }

        @Override
        protected String getDatabaseName() {
            return "reactive";
        }
    }
    ```

    请注意，如果我们使用独立的MongoDB安装，上述内容是必要的。但是，当我们在示例中使用带有嵌入式MongoDB的Spring Boot时，上述配置没有必要。

4. 创建文档

    对于以下示例，让我们创建一个帐户类，并使用@Document进行注释，以在数据库操作中使用：

    ```java
    @Document
    public class Account {

        @Id
        private String id;
        private String owner;
        private Double value;
    
        // getters and setters
    }
    ```

5. 使用反应存储库

    我们已经熟悉存储库编程模型，已经定义了CRUD方法，并支持其他一些常见的东西。

    现在，使用反应模型，我们得到了相同的方法和规范集，只是我们将以反应的方式处理结果和参数。

    1. ReactiveCrud存储库

        我们可以以与阻止CrudRepository相同的方式使用此存储库：

        ```java
        @Repository
        public interface AccountCrudRepository
        extends ReactiveCrudRepository<Account, String> {

            Flux<Account> findAllByValue(String value);
            Mono<Account> findFirstByOwner(Mono<String> owner);
        }
        ```

        我们可以传递不同类型的参数，如plain（String）、wrapped（Optional、Stream）或reactive（Mono、Flux），正如我们在findFirstByOwner（）方法中看到的。

    2. ReactiveMongo存储库

        还有ReactiveMongoRepository接口，它继承自ReactiveCrudRepository，并添加了一些新的查询方法：

        ```java
        @Repository
        public interface AccountReactiveRepository
        extends ReactiveMongoRepository<Account, String> { }
        ```

        使用ReactiveMongoRepository，我们可以通过示例进行查询：

        ```java
        Flux<Account> accountFlux = repository
        .findAll(Example.of(new Account(null, "owner", null)));
        ```

        因此，我们将获得每个与通过的示例相同的帐户。

        随着我们的存储库的创建，他们已经定义了执行一些我们不需要实现的数据库操作的方法：

        ```java
        Mono<Account> accountMono
        = repository.save(new Account(null, "owner", 12.3));
        Mono<Account> accountMono2 = repository
        .findById("123456");
        ```

    3. RxJava3Crud存储库

        使用RxJava3CrudRepository，我们的行为与ReactiveCrudRepository相同，但具有来自RxJava的结果和参数类型：

        ```java
        @Repository
        public interface AccountRxJavaRepository
        extends RxJava3CrudRepository<Account, String> {

            Observable<Account> findAllByValue(Double value);
            Single<Account> findFirstByOwner(Single<String> owner);
        }
        ```

    4. 测试我们的基本操作

        为了测试我们的存储库方法，我们将使用测试订阅者：

        ```java
        @Test
        public void givenValue_whenFindAllByValue_thenFindAccount() {
            repository.save(new Account(null, "Bill", 12.3)).block();
            Flux<Account> accountFlux = repository.findAllByValue(12.3);

            StepVerifier
            .create(accountFlux)
            .assertNext(account -> {
                assertEquals("Bill", account.getOwner());
                assertEquals(Double.valueOf(12.3) , account.getValue());
                assertNotNull(account.getId());
            })
            .expectComplete()
            .verify();
        }

        @Test
        public void givenOwner_whenFindFirstByOwner_thenFindAccount() {
            repository.save(new Account(null, "Bill", 12.3)).block();
            Mono<Account> accountMono = repository
            .findFirstByOwner(Mono.just("Bill"));

            StepVerifier
            .create(accountMono)
            .assertNext(account -> {
                assertEquals("Bill", account.getOwner());
                assertEquals(Double.valueOf(12.3) , account.getValue());
                assertNotNull(account.getId());
            })
            .expectComplete()
            .verify();
        }

        @Test
        public void givenAccount_whenSave_thenSaveAccount() {
            Mono<Account> accountMono = repository.save(new Account(null, "Bill", 12.3));

            StepVerifier
            .create(accountMono)
            .assertNext(account -> assertNotNull(account.getId()))
            .expectComplete()
            .verify();
        }
        ```

6. ReactiveMongoTemplate

    除了存储库方法外，我们还有ReactiveMongoTemplate。

    首先，我们需要将ReactiveMongoTemplate注册为bean：

    ```java
    @Configuration
    public class ReactiveMongoConfig {

        @Autowired
        MongoClient mongoClient;

        @Bean
        public ReactiveMongoTemplate reactiveMongoTemplate() {
            return new ReactiveMongoTemplate(mongoClient, "test");
        }
    }
    ```

    然后，我们可以将此bean注入我们的服务中，以执行数据库操作：

    ```java
    @Service
    public class AccountTemplateOperations {

        @Autowired
        ReactiveMongoTemplate template;

        public Mono<Account> findById(String id) {
            return template.findById(id, Account.class);
        }
    
        public Flux<Account> findAll() {
            return template.findAll(Account.class);
        } 
        public Mono<Account> save(Mono<Account> account) {
            return template.save(account);
        }
    }
    ```

    ReactiveMongoTemplate还有许多与我们拥有的域无关的方法，您可以在[文档](https://docs.spring.io/spring-data/mongodb/docs/current/api/org/springframework/data/mongodb/core/ReactiveMongoTemplate.html)中查看它们。

7. 结论

    在这个简短的教程中，我们介绍了使用Spring Data Reactive Repositories框架的MongoDB使用反应式编程的存储库和模板。
