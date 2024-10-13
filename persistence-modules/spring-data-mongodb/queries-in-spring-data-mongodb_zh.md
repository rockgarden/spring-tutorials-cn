# [Spring Data MongoDB中的查询指南](https://www.baeldung.com/queries-in-spring-data-mongodb)

1. 一览表

    本教程将专注于在Spring Data MongoDB中构建不同类型的查询。

    我们将研究使用查询和标准类、自动生成的查询方法、JSON查询和QueryDSL来查询文档。

2. 文档查询

    使用Spring Data查询MongoDB的更常见方法之一是使用Query和Crireare类，它们非常密切地反映了本机运算符。

    1. Is

        这只是一个使用平等的标准。让我们看看它是如何工作的。

        在以下示例中，我们将寻找名为Eric的用户。

        让我们来看看我们的数据库：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 55
            }
        ]
        ```

        现在让我们来看看查询代码：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Eric"));
        List<User> users = mongoTemplate.find(query, User.class);
        ```

        不出所料，这个逻辑返回：

        ```json
        {
            "_id" : ObjectId("55c0e5e5511f0a164a581907"),
            "_class" : "org.baeldung.model.User",
            "name" : "Eric",
            "age" : 45
        }
        ```

    2. Regex

        正则表示式是一种更灵活、更强大的查询类型。这使用MongoDB $regex创建了一个标准，该标准返回适合此字段正则表示式的所有记录。

        它的工作原理与startWith和endingWith操作相似。

        在本例中，我们将查找所有名称以A开头的用户。

        以下是数据库的状态：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581909"),
                "_class" : "org.baeldung.model.User",
                "name" : "Alice",
                "age" : 35
            }
        ]
        ```

        现在让我们创建查询：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex("^A"));
        List<User> users = mongoTemplate.find(query,User.class);
        ```

        这运行并返回2条记录：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581909"),
                "_class" : "org.baeldung.model.User",
                "name" : "Alice",
                "age" : 35
            }
        ]
        ```

        这是另一个快速示例，这次寻找所有名字以c结尾的用户：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex("c$"));
        List<User> users = mongoTemplate.find(query, User.class);
        ```

        所以结果将是：

        ```json
        {
            "_id" : ObjectId("55c0e5e5511f0a164a581907"),
            "_class" : "org.baeldung.model.User",
            "name" : "Eric",
            "age" : 45
        }
        ```

    3. Lt和gt

        这些运算符使用$lt（小于）和$gt（大于）运算符创建标准。

        让我们举一个快速的例子，我们正在寻找20至50岁的所有用户。

        数据库是：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 55
            }
        ]
        ```

        查询代码：

        ```java
        Query query = new Query();
        query.addCriteria(Criteria.where("age").lt(50).gt(20));
        List<User> users = mongoTemplate.find(query,User.class);
        ```

        以及所有年龄在20岁以上和50岁以下的用户的结果：

        ```json
        {
            "_id" : ObjectId("55c0e5e5511f0a164a581907"),
            "_class" : "org.baeldung.model.User",
            "name" : "Eric",
            "age" : 45
        }
        ```

    4. Sort

        排序用于指定结果的排序顺序。

        下面的示例返回所有用户，按年龄升序排序。

        首先，以下是现有数据：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581909"),
                "_class" : "org.baeldung.model.User",
                "name" : "Alice",
                "age" : 35
            }
        ]
        ```

        执行排序后：

        ```java
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.ASC, "age"));
        List<User> users = mongoTemplate.find(query,User.class);
        ```

        以下是查询结果，按年龄很好地排序：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581909"),
                "_class" : "org.baeldung.model.User",
                "name" : "Alice",
                "age" : 35
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            }
        ]
        ```

    5. Pageable

        让我们来看看一个使用分页的快速示例。

        以下是数据库的状态：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581909"),
                "_class" : "org.baeldung.model.User",
                "name" : "Alice",
                "age" : 35
            }
        ]
        ```

        现在，这是查询逻辑，只需要求一个大小为2的页面：

        ```java
        final Pageable pageableRequest = PageRequest.of(0, 2);
        Query query = new Query();
        query.with(pageableRequest);
        ```

        结果，正如预期的那样，这2份文件：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581907"),
                "_class" : "org.baeldung.model.User",
                "name" : "Eric",
                "age" : 45
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            }
        ]
        ```

3. 生成的查询方法

    现在，让我们来探索Spring Data通常提供的更常见的查询类型，即方法名称自动生成的查询。

    要利用这些类型的查询，我们唯一需要做的是在存储库界面上声明该方法：

    ```java
    public interface UserRepository
    extends MongoRepository<User, String>, QueryDslPredicateExecutor<User> {
        ...
    }
    ```

    1. FindByX

        我们将从简单开始，通过探索findBy类型的查询。在这种情况下，我们将使用“查找名称”：

        `List<User> findByName(String name);`

        就像上一节2.1一样，查询将有相同的结果，找到所有具有给定名称的用户：

        `List<User> users = userRepository.findByName("Eric");`

    2. 开始和结束

        在第2.2节中，我们探索了一个基于正则格式的查询。开头和结尾当然不那么强大，但尽管如此还是相当有用的，特别是如果我们不必实际实现它们。

        以下是操作的简要示例：

        `List<User> findByNameStartingWith(String regexp);`

        `List<User> findByNameEndingWith(String regexp);`

        当然，实际使用这个的例子非常简单：

        `List<User> users = userRepository.findByNameStartingWith("A");`

        `List<User> users = userRepository.findByNameEndingWith("c");`

        结果完全相同。

    3. 之间

        与第2.3节类似，这将返回所有年龄介于ageGT和ageLT之间的用户：

        `List<User> findByAgeBetween(int ageGT, int ageLT);`

        调用该方法将导致找到完全相同的文档：

        `List<User> users = userRepository.findByAgeBetween(20, 50);`

    4. 喜欢和订购

        这次让我们来看看一个更高级的示例，为生成的查询组合了两种类型的修饰符。

        我们将寻找所有名字包含字母A的用户，我们还将按年龄按升序对结果进行排序：

        `List<User> users = userRepository.findByNameLikeOrderByAgeAsc("A");`

        对于我们在第2.4节中使用的数据库，结果将是：

        ```json
        [
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581908"),
                "_class" : "org.baeldung.model.User",
                "name" : "Antony",
                "age" : 33
            },
            {
                "_id" : ObjectId("55c0e5e5511f0a164a581909"),
                "_class" : "org.baeldung.model.User",
                "name" : "Alice",
                "age" : 35
            }
        ]
        ```

4. JSON查询方法

    如果我们无法在方法名称或标准的帮助下表示查询，我们可以做一些更低级的事情，使用@Query注释。

    使用此注释，我们可以将原始查询指定为Mongo JSON查询字符串。

    1. FindBy

        让我们从简单开始，先看看我们如何按方法类型表示发现：

        ```java
        @Query("{ 'name' : ?0 }")
        List<User> findUsersByName(String name);
        ```

        此方法应按名称返回用户。占位符？0引用该方法的第一个参数。

        `List<User> users = userRepository.findUsersByName("Eric");`

    2. $regex

        我们还可以查看正则表示式驱动的查询，当然，该查询的结果与第2.2节和3.2节相同：

        ```java
        @Query("{ 'name' : { $regex: ?0 } }")
        List<User> findUsersByRegexpName(String regexp);
        ```

        用法也完全相同：

        `List<User> users = userRepository.findUsersByRegexpName("^A");`

        `List<User> users = userRepository.findUsersByRegexpName("c$");`

    3. `$lt`和`$gt`

        现在让我们实现lt和gt查询：

        ```java
        @Query("{ 'age' : { $gt: ?0, $lt: ?1 } }")
        List<User> findUsersByAgeBetween(int ageGT, int ageLT);
        ```

        现在该方法有2个参数，我们在原始查询中按索引引用每个参数，？0和？1：

        `List<User> users = userRepository.findUsersByAgeBetween(20, 50);`

5. 查询DSL查询

    MongoRepository对[QueryDSL](http://www.querydsl.com/)项目有很好的支持，因此我们也可以在这里利用这个漂亮、类型安全的API。

    1. Maven

        首先，让我们确保我们在pom中定义了正确的Maven依赖项：

        ```xml
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-mongodb</artifactId>
            <version>5.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-apt</artifactId>
            <version>5.1.0</version>
        </dependency>
        ```

    2. Q类

        QueryDSL使用Q类来创建查询，但由于我们真的不想手动创建这些查询，我们需要以某种方式生成它们。

        我们将使用apt-maven-plugin来做到这一点：

        ```xml
        <plugin>    
            <groupId>com.mysema.maven</groupId>
            <artifactId>apt-maven-plugin</artifactId>
            <version>1.1.3</version>
            <executions>
                <execution>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>target/generated-sources/java</outputDirectory>
                        <processor>
                        org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor
                        </processor>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        ```

        让我们来看看用户类，特别关注@QueryEntity注释：

        ```java
        @QueryEntity
        @Document
        public class User {

            @Id
            private String id;
            private String name;
            private Integer age;
        
            // standard getters and setters
        }
        ```

        在运行Maven生命周期的进程目标（或之后的任何其他目标）后，apt插件将在target/generated-sources/java/{your package structure}下生成新类：

        ```java
        /**
         * QUser is a Querydsl query type for User
         */
        @Generated("com.mysema.query.codegen.EntitySerializer")
        public class QUser extends EntityPathBase<User> {

            private static final long serialVersionUID = ...;

            public static final QUser user = new QUser("user");

            public final NumberPath<Integer> age = createNumber("age", Integer.class);

            public final StringPath id = createString("id");

            public final StringPath name = createString("name");

            public QUser(String variable) {
                super(User.class, forVariable(variable));
            }

            public QUser(Path<? extends User> path) {
                super(path.getType(), path.getMetadata());
            }

            public QUser(PathMetadata<?> metadata) {
                super(User.class, metadata);
            }
        }
        ```

        正是因为这个类，我们才不需要创建查询。

        作为旁注，如果我们使用Eclipse，引入此插件将在pom中生成以下警告：

        > 您需要使用 JDK 或在 classpath 上安装 tools.jar。如果在 eclipse 构建过程中出现这种情况，请确保也在 JDK 下运行 eclipse(com.mysema.maven:apt-maven-plugin:1.1.3:process:default:generate-sources)

        Maven安装工作正常，并生成了QUser类，但pom中突出显示了一个插件。

        快速修复方法是在eclipse.ini中手动指向JDK：

        ```ini
        ...
        -vm
        {path_to_jdk}\jdk{your_version}\bin\javaw.exe
        ```

    3. 新存储库

        现在，我们需要在我们的存储库中实际启用QueryDSL支持，这可以通过简单地扩展QueryDslPredicateExecutor接口来完成：

        ```java
        public interface UserRepository extends 
        MongoRepository<User, String>, QuerydslPredicateExecutor<User>
        ```

    4. Eq

        启用支持后，让我们现在实现与之前说明的相同查询。

        我们将从简单的平等开始：

        ```java
        QUser qUser = new QUser("user");
        Predicate predicate = qUser.name.eq("Eric");
        List<User> users = (List<User>) userRepository.findAll(predicate);
        ```

    5. StartingWith和EndingWith

        同样，让我们实现之前的查询，并找到名称以A开头的用户：

        ```java
        QUser qUser = new QUser("user");
        Predicate predicate = qUser.name.startsWith("A");
        List<User> users = (List<User>) userRepository.findAll(predicate);
        ```

        以及以c结尾：

        ```java
        QUser qUser = new QUser("user");
        Predicate predicate = qUser.name.endsWith("c");
        List<User> users = (List<User>) userRepository.findAll(predicate);
        ```

        结果与第2.2、3.2和4.2节相同。

    6. Between

        下一个查询将返回年龄在20至50岁之间的用户，与前几节相似：

        ```java
        QUser qUser = new QUser("user");
        Predicate predicate = qUser.age.between(20, 50);
        List<User> users = (List<User>) userRepository.findAll(predicate);
        ```

6. 结论

    在本文中，我们探讨了使用Spring Data MongoDB进行查询的多种方式。

    退后一步，看看我们必须查询MongoDB的所有强大方法，从有限控制到原始查询的完全控制，这很有意思。
