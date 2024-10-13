# [Spring Data MongoDB – 索引、注释和转换器](https://www.baeldung.com/spring-data-mongodb-index-annotations-converter)

1. 一览表

    在本教程中，我们将探索Spring Data MongoDB的一些核心功能——索引、常见注释和转换器。我们将使用Spring Boot来获取传递依赖项。

2. 索引

    1. @Indexed

        此注释将字段标记为MongoDB中的索引：

        ```java
        @QueryEntity
        @Document
        public class User {
            @Indexed
            private String name;

            ... 
        }
        ```

        现在名称字段已索引——让我们来看看MongoDB shell中的索引：

        `db.user.getIndexes();`

        以下是我们得到的东西：

        ```json
        [
            {
                "v" : 1,
                "key" : {
                    "_id" : 1
                },
                "name" : "_id_",
                "ns" : "test.user"
            }
        ]
        ```

        我们可能会惊讶地发现，任何地方都没有名字字段的迹象！

        这是因为，从Spring Data MongoDB 3.0开来，自动索引创建默认关闭。

        然而，我们可以通过在MongoConfig中显式覆盖autoIndexCreation（）方法来更改该行为：

        ```java
        public class MongoConfig extends AbstractMongoClientConfiguration {

            // rest of the config goes here

            @Override
            protected boolean autoIndexCreation() {
                return true;
            }
        }
        ```

        让我们再次查看MongoDB shell中的索引：

        ```json
        [
            {
                "v" : 1,
                "key" : {
                    "_id" : 1
                },
                "name" : "_id_",
                "ns" : "test.user"
            },
            {
                "v" : 1,
                "key" : {
                    "name" : 1
                },
                "name" : "name",
                "ns" : "test.user"
            }
        ]
        ```

        正如我们所看到的，这次我们有两个索引——其中一个是_id——默认情况下是由于@Id注释而创建的，第二个是我们的名称字段。

        在Spring boot spring.data.mongodb.auto-index-creation属性中将此属性设置为true，将在应用程序启动时[启用索引创建](https://docs.spring.io/spring-data/mongodb/docs/current-SNAPSHOT/reference/html/#mapping.index-creation)。

    2. 以编程方式创建索引

        我们还可以通过编程方式创建一个索引：

        ```java
        mongoOps.indexOps(User.class).
        ensureIndex(new Index().on("name", Direction.ASC));
        ```

        我们现在为字段名称创建了索引，结果将与上一节相同。

    3. 复合指数

        MongoDB支持复合索引，其中单个索引结构包含对多个字段的引用。

        让我们看看一个使用复合索引的快速示例：

        ```java
        @QueryEntity
        @Document
        @CompoundIndexes({
            @CompoundIndex(name = "email_age", def = "{'email.id' : 1, 'age': 1}")
        })
        public class User {
            //
        }
        ```

        我们用电子邮件和年龄字段创建了一个复合索引。现在让我们来看看实际的索引：

        ```json
        {
            "v" : 1,
            "key" : {
                "email.id" : 1,
                "age" : 1
            },
            "name" : "email_age",
            "ns" : "test.user"
        }
        ```

        请注意，DBRef字段不能用@Index标记——该字段只能是复合索引的一部分。

3. 常见注释

    1. @Transient

        正如我们预期的那样，这个简单的注释将字段排除在数据库中持久化：

        ```java
        public class User {
            @Transient
            private Integer yearOfBirth;
            // standard getter and setter
        }
        ```

        让我们用设置字段yearOfBirth插入用户：

        ```java
        User user = new User();
        user.setName("Alex");
        user.setYearOfBirth(1985);
        mongoTemplate.insert(user);
        ```

        现在，如果我们查看数据库的状态，我们会看到存档的年份没有保存：

        ```json
        {
            "_id" : ObjectId("55d8b30f758fd3c9f374499b"),
            "name" : "Alex",
            "age" : null
        }
        ```

        因此，如果我们查询并检查：

        `mongoTemplate.findOne(Query.query(Criteria.where("name").is("Alex")), User.class).getYearOfBirth()`

        结果将为空。

    2. @Field

        @Field表示JSON文档中字段使用的键：

        ```java
        @Field("email")
        private EmailAddress emailAddress;
        ```

        现在，emailAddress将使用密钥电子邮件保存在数据库中：

        ```java
        User user = new User();
        user.setName("Brendan");
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setValue("a@gmail.com");
        user.setEmailAddress(emailAddress);
        mongoTemplate.insert(user);
        ```

        以及数据库的状态：

        ```json
        {
            "_id" : ObjectId("55d076d80bad441ed114419d"),
            "name" : "Brendan",
            "age" : null,
            "email" : {
                "value" : "a@gmail.com"
            }
        }
        ```

    3. @PersistenceConstructor和@Value

        @PersistenceConstructor将构造函数标记为持久性逻辑使用的主要构造函数，即使是受软件包保护的构造函数。构造函数参数按名称映射到revetedDBObject中的键值。

        让我们来看看我们用户类的构造函数：

        ```java
        @PersistenceConstructor
        public User(String name, @Value("#root.age ?: 0") Integer age, EmailAddress emailAddress) {
            this.name =  name;
            this.age = age;
            this.emailAddress =  emailAddress;
        }
        ```

        注意这里使用标准Spring @Value注释。正是在此注释的帮助下，我们可以在用于构建域对象之前使用Spring表达式来转换从数据库中检索到的密钥值。这是一个非常强大和非常有用的功能。

        在我们的示例中，如果未设置年龄，默认设置为0。

        现在让我们看看它是如何工作的：

        ```java
        User user = new User();
        user.setName("Alex");
        mongoTemplate.insert(user);
        ```

        我们的数据库将看起来：

        ```json
        {
            "_id" : ObjectId("55d074ca0bad45f744a71318"),
            "name" : "Alex",
            "age" : null
        }
        ```

        因此，年龄字段为空，但当我们查询文档并检索年龄时：

        `mongoTemplate.findOne(Query.query(Criteria.where("name").is("Alex")), User.class).getAge();`

        结果将是0。

4. 转换器

    现在让我们来看看Spring Data MongoDB中的另一个非常有用的功能——转换器，特别是MongoConverter。

    这用于在存储和查询这些对象时处理所有Java类型到DBObjects的映射。

    我们有两个选择——我们可以使用MappingMongoConverter——或者早期版本的SimpleMongoConverter（这在Spring Data MongoDB M3中被弃用，其功能已移至MappingMongoConverter）。

    或者我们可以编写我们自己的自定义转换器。要做到这一点，我们需要实现转换器接口，并在MongoConfig中注册实现。

    让我们来看看一个快速的例子。正如我们在这里的一些JSON输出中看到的那样，数据库中保存的所有对象都有自动保存的字段_class。然而，如果我们想在持久性期间跳过该特定字段，我们可以使用MappingMongoConverter来做到这一点。

    首先——这是自定义转换器的实现：

    ```java
    @Component
    public class UserWriterConverter implements Converter<User, DBObject> {
        @Override
        public DBObject convert(User user) {
            DBObject dbObject = new BasicDBObject();
            dbObject.put("name", user.getName());
            dbObject.put("age", user.getAge());
            if (user.getEmailAddress() != null) {
                DBObject emailDbObject = new BasicDBObject();
                emailDbObject.put("value", user.getEmailAddress().getValue());
                dbObject.put("email", emailDbObject);
            }
            dbObject.removeField("_class");
            return dbObject;
        }
    }
    ```

    请注意，我们如何通过专门直接删除此处的字段来轻松实现不持久_class的目标。

    现在我们需要注册自定义转换器：

    ```java
    private List<Converter<?,?>> converters = new ArrayList<Converter<?,?>>();

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new UserWriterConverter());
        return new MongoCustomConversions(converters);
    }
    ```

    如果需要，我们当然可以通过XML配置实现相同的结果：

    ```xml
    <bean id="mongoTemplate" 
    class="org.springframework.data.mongodb.core.MongoTemplate">
        <constructor-arg name="mongo" ref="mongo"/>
        <constructor-arg ref="mongoConverter" />
        <constructor-arg name="databaseName" value="test"/>
    </bean>

    <mongo:mapping-converter id="mongoConverter" base-package="org.baeldung.converter">
        <mongo:custom-converters base-package="com.baeldung.converter" />
    </mongo:mapping-converter>
    ```

    现在，当我们保存新用户时：

    ```java
    User user = new User();
    user.setName("Chris");
    mongoOps.insert(user);
    ```

    数据库中生成的文档不再包含类信息：

    ```json
    {
        "_id" : ObjectId("55cf09790bad4394db84b853"),
        "name" : "Chris",
        "age" : null
    }
    ```

5. 结论

    在本教程中，我们涵盖了使用Spring Data MongoDB的一些核心概念——索引、常见注释和转换器。
