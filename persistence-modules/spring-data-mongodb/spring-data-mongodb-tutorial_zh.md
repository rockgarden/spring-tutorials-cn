# [Spring Data MongoDB简介](https://www.baeldung.com/spring-data-mongodb-tutorial)

1. 一览表

    本文将对Spring Data MongoDB进行快速而实用的介绍。

    我们将使用MongoTemplate和MongoRepository来介绍基础知识，并举例说明每个操作。

2. Mongo模板和MongoRepository

    MongoTemplate遵循Spring中的标准模板模式，并为底层持久性引擎提供现成的基本API。

    存储库遵循以Spring Data为中心的方法，并根据所有Spring Data项目中众所周知的访问模式，提供更灵活和复杂的API操作。

    对于两者，我们需要从定义依赖项开始——例如，在pom.xml中，使用Maven：

    ```xml
    <dependency>	
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
        <version>3.1.5</version>
    </dependency>
    ```

3. MongoTemplate的配置

    1. XML配置

        让我们从Mongo模板的简单XML配置开始：

        ```xml
        <mongo:mongo-client id="mongoClient" host="localhost" />
        <mongo:db-factory id="mongoDbFactory" dbname="test" mongo-client-ref="mongoClient" />
        ```

        我们首先需要定义负责创建Mongo实例的工厂bean。

        接下来，我们需要实际定义（并配置）模板bean：

        ```xml
        <bean id="mongoTemplate" class="org.springframework.data.mongodb.core.MongoTemplate"> 
            <constructor-arg ref="mongoDbFactory"/> 
        </bean>
        ```

        最后，我们需要定义一个后处理器来翻译@Repository注释类中抛出的任何MongoExceptions：

        ```xml
        <bean class=
        "org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>
        ```

    2. Java配置

        现在让我们通过扩展 MongoDB 配置 AbstractMongoConfiguration的基类，使用Java配置创建一个类似的配置：

        ```java
        @Configuration
        public class MongoConfig extends AbstractMongoClientConfiguration {

            @Override
            protected String getDatabaseName() {
                return "test";
            }
        
            @Override
            public MongoClient mongoClient() {
                ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/test");
                MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
                
                return MongoClients.create(mongoClientSettings);
            }
        
            @Override
            public Collection getMappingBasePackages() {
                return Collections.singleton("com.baeldung");
            }
        }
        ```

        请注意，我们不需要在之前的配置中定义MongoTemplate bean，因为它已经在AbstractMongoClientConfiguration中定义了。

        我们也可以在不扩展AbstractMongoClientConfiguration的情况下从头开始使用我们的配置：

        ```java
        @Configuration
        public class SimpleMongoConfig {

            @Bean
            public MongoClient mongo() {
                ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/test");
                MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
                
                return MongoClients.create(mongoClientSettings);
            }

            @Bean
            public MongoTemplate mongoTemplate() throws Exception {
                return new MongoTemplate(mongo(), "test");
            }
        }
        ```

4. MongoRepository的配置

    1. XML配置

        要使用自定义存储库（扩展MongoRepository），我们需要从第3.1节继续配置，并设置存储库：

        ```xml
        <mongo:repositories 
        base-package="com.baeldung.repository" mongo-template-ref="mongoTemplate"/>
        ```

    2. Java配置

        同样，我们将在3.2节中已经创建的配置的基础上构建，并在组合中添加一个新的注释：

        `@EnableMongoRepositories(basePackages = "com.baeldung.repository")`

    3. 创建存储库

        配置后，我们需要创建一个存储库——扩展现有的MongoRepository接口：

        `public interface UserRepository extends MongoRepository<User, String> {...}`

        现在我们可以自动连接此用户存储库，并使用MongoRepository的操作或添加自定义操作。

5. 使用MongoTemplate

    1. 插入

        让我们从插入操作和空数据库开始：

        `{}`

        现在，如果我们插入一个新用户：

        ```java
        User user = new User();
        user.setName("Jon");
        mongoTemplate.insert(user, "user");
        ```

        数据库将看起来像这样：

        ```json
        {
            "_id" : ObjectId("55b4fda5830b550a8c2ca25a"),
            "_class" : "com.baeldung.model.User",
            "name" : "Jon"
        }
        ```

    2. 保存-插入

        保存操作具有保存或更新语义：如果存在id，则执行更新，如果没有，则执行插入。

        让我们来看看第一个语义——插入。

        以下是数据库的初始状态：

        `{}`

        当我们现在保存一个新用户时：

        ```java
        User user = new User();
        user.setName("Albert");
        mongoTemplate.save(user, "user");
        ```

        实体将被插入数据库中：

        ```json
        {
            "_id" : ObjectId("55b52bb7830b8c9b544b6ad5"),
            "_class" : "com.baeldung.model.User",
            "name" : "Albert"
        }
        ```

        接下来，我们将通过更新语义来查看相同的操作——保存。

    3. 保存 – 更新

        现在让我们来看看在现有实体上运行的更新语义保存：

        ```json
        {
            "_id" : ObjectId("55b52bb7830b8c9b544b6ad5"),
            "_class" : "com.baeldung.model.User",
            "name" : "Jack"
        }
        ```

        当我们保存现有用户时，我们会更新它：

        ```java
        user = mongoTemplate.findOne(
        Query.query(Criteria.where("name").is("Jack")), User.class);
        user.setName("Jim");
        mongoTemplate.save(user, "user");
        ```

        数据库将如下：

        ```json
        {
            "_id" : ObjectId("55b52bb7830b8c9b544b6ad5"),
            "_class" : "com.baeldung.model.User",
            "name" : "Jim"
        }
        ```

        我们可以看到，在这个特定示例中，保存使用更新的语义，因为我们使用了带有given_id的对象。

    4. 先更新

        updateFirst更新与查询匹配的第一个文档。

        让我们从数据库的初始状态开始：

        ```json
        [
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Alex"
            },
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614c"),
                "_class" : "com.baeldung.model.User",
                "name" : "Alex"
            }
        ]
        ```

        当我们现在运行更新时：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Alex"));
        Update update = new Update();
        update.set("name", "James");
        mongoTemplate.updateFirst(query, update, User.class);
        ```

        只有第一个条目才会更新：

        ```json
        [
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "James"
            },
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614c"),
                "_class" : "com.baeldung.model.User",
                "name" : "Alex"
            }
        ]
        ```

    5. 更新多

        UpdateMulti更新与给定查询匹配的所有文档。

        首先，以下是进行updateMulti之前的数据库状态：

        ```json
        [
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Eugen"
            },
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614c"),
                "_class" : "com.baeldung.model.User",
                "name" : "Eugen"
            }
        ]
        ```

        现在让我们运行updateMulti操作：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Eugen"));
        Update update = new Update();
        update.set("name", "Victor");
        mongoTemplate.updateMulti(query, update, User.class);
        ```

        两个现有对象都将在数据库中更新：

        ```json
        [
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Victor"
            },
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614c"),
                "_class" : "com.baeldung.model.User",
                "name" : "Victor"
            }
        ]
        ```

    6. 查找和修改

        此操作的工作方式与updateMulti类似，但它在修改对象之前返回。

        首先，这是调用findAndModify之前的数据库状态：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Markus"
        }
        ```

        让我们来看看实际的操作代码：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Markus"));
        Update update = new Update();
        update.set("name", "Nick");
        User user = mongoTemplate.findAndModify(query, update, User.class);
        ```

        返回的用户对象与数据库中的初始状态具有相同的值。

        然而，这是数据库中的新状态：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Nick"
        }
        ```

    7. 提高

        上载工作于查找和修改，否则创建语义：如果文档匹配，则更新它，或者通过结合查询和更新对象来创建新文档。

        让我们从数据库的初始状态开始：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Markus"
        }
        ```

        现在让我们来运行upsert：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Markus"));
        Update update = new Update();
        update.set("name", "Nick");
        mongoTemplate.upsert(query, update, User.class);
        ```

        以下是操作后数据库的状态：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Nick"
        }
        ```

    8. 移除

        在调用remove之前，我们将查看数据库的状态：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Benn"
        }
        ```

        现在让我们运行删除：

        `mongoTemplate.remove(user, "user");`

        结果将如预期：

        ```log
        {
        }
        ```

6. 使用MongoRepository

    1. 插入

        首先，在运行插入之前，我们将看到数据库的状态：

        `{}`

        现在我们将插入一个新用户：

        ```java
        User user = new User();
        user.setName("Jon");
        userRepository.insert(user);
        ```

        这是数据库的最终状态：

        ```json
        {
            "_id" : ObjectId("55b4fda5830b550a8c2ca25a"),
            "_class" : "com.baeldung.model.User",
            "name" : "Jon"
        }
        ```

        注意该操作的工作原理与MongoTemplate API中的插入相同。

    2. 保存-插入

        同样，保存的工作方式与MongoTemplate API中的保存操作相同。

        让我们从查看操作的插入语义开始。

        以下是数据库的初始状态：

        `{}`

        现在我们执行保存操作：

        ```java
        User user = new User();
        user.setName("Aaron");
        userRepository.save(user);
        ```

        这导致用户被添加到数据库中：

        ```json
        {
            "_id" : ObjectId("55b52bb7830b8c9b544b6ad5"),
            "_class" : "com.baeldung.model.User",
            "name" : "Aaron"
        }
        ```

        再次注意保存如何与插入语义一起工作，因为我们正在插入一个新对象。

    3. 保存 – 更新

        现在让我们来看看相同的操作，但更新了语义。

        首先，以下是运行新保存之前的数据库状态：

        ```json
        {
            "_id" : ObjectId("55b52bb7830b8c9b544b6ad5"),
            "_class" : "com.baeldung.model.User",
            "name" : "Jack"81*6
        }
        ```

        现在我们执行操作：

        ```java
        user = mongoTemplate.findOne(
        Query.query(Criteria.where("name").is("Jack")), User.class);
        user.setName("Jim");
        userRepository.save(user);
        ```

        最后，以下是数据库的状态：

        ```json
        {
            "_id" : ObjectId("55b52bb7830b8c9b544b6ad5"),
            "_class" : "com.baeldung.model.User",
            "name" : "Jim"
        }
        ```

        再次注意保存如何与更新语义一起工作，因为我们正在使用一个现有对象。

    4. 删除

        以下是调用删除之前的数据库状态：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Benn"
        }
        ```

        让我们运行删除：

        `userRepository.delete(user);`

        这是我们的结果：

        `{}`

    5. 找到一个

        接下来，这是调用findOne时的数据库状态：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Chris"
        }
        ```

        现在让我们执行findOne：

        `userRepository.findOne(user.getId())`

        结果将返回现有数据：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Chris"
        }
        ```

    6. 存在

        调用前的数据库状态存在：

        ```json
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Harris"
        }
        ```

        现在让我们运行存在，这当然会返回true：

        `boolean isExists = userRepository.exists(user.getId());`

    7. 用排序找到所有

        调用findAll之前的数据库状态：

        ```json
        [
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Brendan"
            },
            {
            "_id" : ObjectId("67b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Adam"
            }
        ]
        ```

        现在让我们用Sort运行findAll：

        `List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));`

        结果将按名字升序排序：

        ```json
        [
            {
                "_id" : ObjectId("67b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Adam"
            },
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Brendan"
            }
        ]
        ```

    8. 使用Pageable查找所有

        调用findAll之前的数据库状态：

        ```json
        [
            {
                "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Brendan"
            },
            {
                "_id" : ObjectId("67b5ffa5511fee0e45ed614b"),
                "_class" : "com.baeldung.model.User",
                "name" : "Adam"
            }
        ]
        ```

        现在让我们用分页请求执行findAll：

        ```java
        Pageable pageableRequest = PageRequest.of(0, 1);
        Page<User> page = userRepository.findAll(pageableRequest);
        List<User> users = pages.getContent();
        ```

        生成的用户列表将只有一个用户：

        ```java
        {
            "_id" : ObjectId("55b5ffa5511fee0e45ed614b"),
            "_class" : "com.baeldung.model.User",
            "name" : "Brendan"
        }
        ```

7. 注释

    最后，让我们来一下Spring Data用来驱动这些API操作的简单注释。

    字段级@Id注释可以装饰任何类型，包括长和字符串：

    ```java
    @Id
    private String id;
    ```

    如果@Id字段的值不是空的，则按原位存储在数据库中；否则，转换器将假设我们希望在数据库中存储ObjectId（ObjectId、String或BigInteger工作）。

    接下来我们将看看@Document：

    ```java
    @Document
    public class User {
        //
    }
    ```

    此注释只是将类标记为需要持久化到数据库的域对象，同时允许我们选择要使用的集合的名称。

8. 结论

    本文快速而全面地介绍了通过MongoTemplate API以及使用MongoRepository将MongoDB与Spring Data一起使用。
