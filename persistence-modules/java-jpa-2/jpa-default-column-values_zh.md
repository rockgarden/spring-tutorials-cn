# [JPA中的默认列值](https://www.baeldung.com/jpa-default-column-values)

1. 一览表

    在本教程中，我们将研究JPA中的默认列值。

    我们将学习如何将它们设置为实体中的默认属性，以及直接在SQL表定义中。

2. 在创建实体时

    设置默认列值的第一种方法是直接将其设置为实体属性值：

    ```java
    @Entity
    public class User {
        @Id
        private Long id;
        private String firstName = "John Snow";
        private Integer age = 25;
        private Boolean locked = false;
    }
    ```

    现在，每次我们使用新运算符创建实体时，它都会设置我们提供的默认值：

    ```java
    @Test
    void givenUser_whenUsingEntityValue_thenSavedWithDefaultValues() {
        User user = new User();
        user = userRepository.save(user);

        assertEquals(user.getName(), "John Snow");
        assertEquals(user.getAge(), 25);
        assertFalse(user.getLocked());
    }
    ```

    这个解决方案有一个缺点。

    当我们查看SQL表定义时，我们不会在其中看到任何默认值：

    ```sql
    create table user
    (
        id     bigint not null constraint user_pkey primary key,
        name   varchar(255),
        age    integer,
        locked boolean
    );
    ```

    因此，如果我们用空覆盖它们，实体将被保存，没有任何错误：

    ```java
    @Test
    void givenUser_whenNameIsNull_thenSavedNameWithNullValue() {
        User user = new User();
        user.setName(null);
        user.setAge(null);
        user.setLocked(null);
        user = userRepository.save(user);

        assertNull(user.getName());
        assertNull(user.getAge());
        assertNull(user.getLocked());
    }
    ```

3. 在模式定义中

    要直接在SQL表定义中创建默认值，我们可以使用@Column注释并设置itscolumnDefinition参数：

    ```java
    @Entity
    public class User {
        @Id
        Long id;

        @Column(columnDefinition = "varchar(255) default 'John Snow'")
        private String name;

        @Column(columnDefinition = "integer default 25")
        private Integer age;

        @Column(columnDefinition = "boolean default false")
        private Boolean locked;
    }
    ```

    使用此方法，默认值将出现在SQL表定义中：

    ```sql
    create table user
    (
        id     bigint not null constraint user_pkey primary key,
        name   varchar(255) default 'John Snow',
        age    integer      default 35,
        locked boolean      default false
    );
    ```

    实体将使用默认值正确保存：

    ```java
    @Test
    void givenUser_whenUsingSQLDefault_thenSavedWithDefaultValues() {
        User user = new User();
        user = userRepository.save(user);

        assertEquals(user.getName(), "John Snow");
        assertEquals(user.getAge(), 25);
        assertFalse(user.getLocked());
    }
    ```

    请记住，通过使用此解决方案，我们在首次保存实体时无法将给定列设置为空。如果我们不提供任何值，默认值将自动设置。

4. 使用@ColumnDefault

    @ColumnDefault注释提供了另一种为列指定默认值的方法。它用于在JPA级别设置默认值，该值反映在生成的SQL模式中。

    以下是如何使用@ColumnDefault来定义默认值的示例：

    ```java
    @Entity
    @Table(name="users_entity")
    public class UserEntity {
        @Id
        private Long id;

        @ColumnDefault("John Snow")
        private String name;

        @ColumnDefault("25")
        private Integer age;

        @ColumnDefault("false")
        private Boolean locked;
    }
    ```

    当使用Hibernate作为JPA提供程序时，@ColumnDefault注释将反映在SQL模式生成中，从而产生如下的表创建SQL：

    ```sql
    create table users_entity
    (
        age integer default 25,
        locked boolean default false,
        id bigint not null,
        name varchar(255) default 'John Snow',
        primary key (id)
    )
    ```

    然而，在插入仅包含id列的行时，只有当我们在INSERT语句中省略它们时，名称、年龄和锁定的默认值才会自动应用：

    `INSERT INTO users_entity (id) VALUES (?);`

    如果我们在INSERT语句中包含所有列，未设置的字段将保持为空，数据库将不会应用默认值。

    让我们通过省略其他字段来指定INSERT查询：

    ```java
    void givenUser_whenSaveUsingQuery_thenSavedWithDefaultValues() {
        EntityManager entityManager = // initialize entity manager

        Query query = entityManager.createNativeQuery("INSERT INTO users_entity (id) VALUES(?) ");
        query.setParameter(1, 2L);
        query.executeUpdate();

        user = userEntityRepository.find(2L);
        assertEquals(user.getName(), "John Snow");
        assertEquals(25, (int) user.getAge());
        assertFalse(user.getLocked());
    }
    ```

5. 使用@PrePersist

    此外，我们可以使用@PrePersist回调来处理默认值。这种方法允许我们在实体持久之前以编程方式设置默认值。如果我们想确保即使实体没有正确初始化或某些字段为空，也要确保设置默认值，这非常有用。

    以下是使用@PrePersist的示例：

    ```java
    @Entity
    public class UserEntity {
        @Id
        private Long id;

        private String name;
        private Integer age;
        private Boolean locked;

        @PrePersist
        public void prePersist() {
            if (name == null) {
                name = "John Snow";
            }
            if (age == null) {
                age = 25;
            }
            if (locked == null) {
                locked = false;
            }
        }
    }
    ```

    在将实体保存到数据库之前，@PrePersist方法会设置默认值。这确保了如果没有提供所有默认值，则应用它们。

    通过利用@PrePersist，无论字段是否显式设置为空，我们的实体总是应用默认值：

    ```java
    @Test
    void givenUser_whenSaveUsingPrePersist_thenSavedWithDefaultValues() {
        UserEntity user = new UserEntity();
        userEntityRepository.save(user, 3L);

        user = userEntityRepository.find(3L);
        assertEquals(user.getName(), "John Snow");
        assertEquals(25, (int) user.getAge());
        assertFalse(user.getLocked());
    }
    ```

6. 结论

    在这篇短文中，我们学习了如何在JPA中设置默认列值。
