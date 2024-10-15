# [使用Spring Boot为MongoDB自动生成字段](https://www.baeldung.com/spring-boot-mongodb-auto-generated-field)

1. 一览表

    在本教程中，我们将学习如何在Spring Boot中为MongoDB实现顺序自动生成的字段。

    当我们使用MongoDB作为Spring Boot应用程序的数据库时，我们不能在我们的模型中使用@GeneratedValue注释，因为它不可用。因此，我们需要一种方法来产生与使用JPA和SQL数据库相同的效果。

    这个问题的一般解决方案很简单。我们将创建一个集合（表），该集合将存储其他集合的生成序列。在创建新记录时，我们将使用它来获取下一个值。

2. 依赖性

    让我们将以下spring-boot启动器添加到我们的pom.xml中：

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
    </dependencies>
    ```

    依赖项的最新版本由spring-boot-starter-parent管理。

3. 集合

    正如概述中所讨论的，我们将创建一个集合，该集合将存储其他集合的自动增量序列。我们将称这个集合为database_sequences。它可以使用mongo shell或MongoDB Compass来创建。让我们创建一个相应的模型类：

    ```java
    @Document(collection = "database_sequences")
    public class DatabaseSequence {
        @Id
        private String id;
        private long seq;
        //getters and setters omitted
    }
    ```

    然后，让我们创建一个用户集合和一个相应的模型对象，该对象将存储正在使用我们系统的人的详细信息：

    ```java
    @Document(collection = "users")
    public class User {
        @Transient
        public static final String SEQUENCE_NAME = "users_sequence";
        @Id
        private BigInteger id;
        private String email;
        //getters and setters omitted
    }
    ```

    在上面创建的用户模型中，我们添加了一个静态字段SEQUENCE_NAME，这是对用户集合自动增量序列的唯一引用。此外，为了成功自动生成ObjectId，类中Id属性或字段的类型必须是String、ObjectId或BigInteger。

    我们还用@Transient注释它，以防止它与模型的其他属性一起持续存在。

4. 创造新记录

    到目前为止，我们已经创建了所需的收藏和模型。现在，我们将创建一个服务，该服务将生成自动增量值，该值可以用作我们实体的id。

    让我们创建一个具有generateSequence（）的SequenceGeneratorService：

    ```java
    public long generateSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(query(where("_id").is(seqName)),
        new Update().inc("seq",1), options().returnNew(true).upsert(true),
        DatabaseSequence.class);
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
    ```

    现在，我们可以在创建新记录时使用generateSequence（）：

    ```java
    User user = new User();
    user.setId(sequenceGenerator.generateSequence(User.SEQUENCE_NAME));
    user.setEmail("john.doe@example.com");
    userRepository.save(user);
    ```

    为了列出所有用户，我们将使用用户存储库：

    ```java
    List<User> storedUsers = userRepository.findAll();
    storedUsers.forEach(System.out::println);
    ```

    就像现在这样，每次创建模型的新实例时，我们都必须设置id字段。我们可以通过为Spring Data MongoDB生命周期事件创建一个侦听器来规避此过程。

    为此，我们将创建一个扩展`AbstractMongoEventListener<User>`的UserModelListener，然后我们将覆盖onBeforeConvert（）：

    ```java
    @Override
    public void onBeforeConvert(BeforeConvertEvent<User> event) {
        if (event.getSource().getId().intValue() < 1) {
            event.getSource().setId(BigInteger.valueOf(
                sequenceGenerator.generateSequence(User.SEQUENCE_NAME)));
        }
    }
    ```

    现在，每次我们保存新用户时，ID都会自动设置。

5. 结论

    总之，我们已经看到了如何为id字段生成顺序、自动增量的值，并模拟与SQL数据库中相同的行为。

    默认情况下，Hibernate使用类似的方法来生成自动增量值。
