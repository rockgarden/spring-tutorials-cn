# [Spring Data 注解](https://www.baeldung.com/spring-data-annotations)

1. 简介

    Spring Data 提供了对数据存储技术的抽象。因此，我们的业务逻辑代码可以更加独立于底层持久化实现。此外，Spring 还简化了数据存储中与实现相关的细节的处理。

    在本教程中，我们将了解 Spring Data、Spring Data JPA 和 Spring Data MongoDB 项目中最常见的注解。

2. 常见的 Spring 数据注解

    1. @Transactional

        当我们想配置方法的事务行为时，可以使用以下注解：

        ```java
        @Transactional
        void pay() {}
        ```

        如果我们在类级别应用此注解，那么它将对类中的所有方法都有效。不过，我们可以将其应用于特定方法，从而覆盖其效果。

    2. @NoRepositoryBean

        有时，我们想创建版本库接口，目的只是为子版本库提供通用方法。

        当然，我们不希望 Spring 为这些版本库创建 Bean，因为我们不会在任何地方注入它们。@NoRepositoryBean 正是这样做的：当我们标记 org.springframework.data.repository.Repository 的子接口时，Spring 不会创建一个 Bean。

        例如，如果我们希望在所有存储库中使用 `Optional<T> findById(ID id)` 方法，我们可以创建一个基础存储库：

        ```java
        @NoRepositoryBean
        interface MyUtilityRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {
            Optional<T> findById(ID id);
        }
        ```

        此注解不会影响子接口；因此，Spring 将为以下存储库接口创建一个 Bean：

        ```java
        @Repository
        interface PersonRepository extends MyUtilityRepository<Person, Long> {}
        ```

        请注意，Spring Data 第 2 版包含了此方法，取代了旧版本的 findOne(ID id)，因此上述示例不再必要。

    3. @Param

        我们可以使用 @Param 为查询传递命名参数：

        ```java
        @Query("FROM Person p WHERE p.name = :name")
        Person findByName(@Param("name") String name);
        ```

        请注意，我们使用 :name 语法引用参数。

    4. @Id

        @Id 将模型类中的一个字段标记为主键：

        ```java
        class Person {
            @Id
            Long id;
            // ...
        }
        ```

        由于它与实现无关，因此模型类很容易与多个数据存储引擎一起使用。

    5. @Transient

        我们可以使用此注解将模型类中的字段标记为暂态字段。因此，数据存储引擎不会读取或写入这个字段的值：

        ```java
        class Person {
            // ...
            @Transient
            int age;
            // ...
        }
        ```

        与 @Id 一样，@Transient 也是独立于实现的，这使得它可以方便地与多个数据存储实现一起使用。

    6. @CreatedBy, @LastModifiedBy, @CreatedDate, @LastModifiedDate

        有了这些注解，我们就可以对模型类进行审核： Spring 会自动在注解字段中填入创建对象的负责人、最后修改对象的负责人、创建日期和最后修改日期：

        ```java
        public class Person {
            // ...
            @CreatedBy
            User creator;
            
            @LastModifiedBy
            User modifier;
            
            @CreatedDate
            Date createdAt;
            
            @LastModifiedDate
            Date modifiedAt;
            // ...
        }
        ```

        请注意，如果我们想让 Spring 填充 principals，我们还需要使用 Spring Security。

3. Spring Data JPA 注释

    1. @Query

        通过 @Query，我们可以为存储库方法提供 JPQL 实现：

        ```java
        @Query("SELECT COUNT(*) FROM Person p")
        long getPersonCount();
        ```

        此外，我们还可以使用命名参数：

        ```java
        @Query("FROM Person p WHERE p.name = :name")
        Person findByName(@Param("name") String name);
        ```

        此外，如果将 nativeQuery 参数设置为 true，我们还可以使用本地 SQL 查询：

        ```java
        @Query(value = "SELECT AVG(p.age) FROM person p", nativeQuery = true)
        int getAverageAge();
        ```

    2. @Procedure

        通过 Spring Data JPA，我们可以轻松地从存储库调用存储过程。

        首先，我们需要使用标准的 JPA 注释在实体类上声明存储库：

        ```java
        @NamedStoredProcedureQueries({
            @NamedStoredProcedureQuery(
                name = "count_by_name",
                procedureName = "person.count_by_name",
                parameters = {
                    @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "name",
                        type = String.class),
                    @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "count",
                        type = Long.class)
                    }
            )
        })

        class Person {}
        ```

        之后，我们就可以用在 name 参数中声明的名称在版本库中引用它了：

        ```java
        @Procedure(name = "count_by_name")
        long getCountByName(@Param("name") String name);
        ```

    3. @Lock

        我们可以在执行版本库查询方法时配置锁定模式：

        ```java
        @Lock(LockModeType.NONE)
        @Query("SELECT COUNT(*) FROM Person p")
        long getPersonCount();
        ```

        可用的锁定模式：

        - READ，读取
        - WRITE，写
        - OPTIMISTIC，乐观
        - OPTIMISTIC_FORCE_INCREMENT，乐观增量
        - PESSIMISTIC_READ，悲观读取
        - PESSIMISTIC_WRITE，悲观写入
        - PESSIMISTIC_FORCE_INCREMENT，悲观_强制_递增
        - NONE，无

    4. @Modifying

        如果使用 @Modifying 对存储库方法进行注解，我们就可以使用该方法修改数据：

        ```java
        @Modifying
        @Query("UPDATE Person p SET p.name = :name WHERE p.id = :id")
        void changeName(@Param("id") long id, @Param("name") String name);
        ```

    5. @EnableJpaRepositories

        要使用 JPA 资源库，我们必须向 Spring 表明这一点。我们可以使用 @EnableJpaRepositories 来做到这一点。

        请注意，我们必须与 @Configuration 一起使用此注解：

        ```java
        @Configuration
        @EnableJpaRepositories
        class PersistenceJPAConfig {}
        ```

        Spring 将在此 @Configuration 类的子包中查找存储库。

        我们可以使用 basePackages 参数来改变这种行为：

        ```java
        @Configuration
        @EnableJpaRepositories(basePackages = "com.baeldung.persistence.dao")
        class PersistenceJPAConfig {}
        ```

        另外请注意，如果 Spring Boot 在类路径上找到 Spring Data JPA，它会自动执行此操作。

4. Spring Data Mongo 注释

    Spring Data 让使用 MongoDB 变得更加容易。在接下来的章节中，我们将探索 Spring Data MongoDB 的最基本功能。

    如需了解更多信息，请访问我们有关 Spring Data MongoDB 的[文章](https://www.baeldung.com/spring-data-mongodb-tutorial)。

    1. @Document

        此注解将一个类标记为我们要持久化到数据库的域对象：

        ```java
        @Document
        class User {}
        ```

        它还允许我们选择要使用的集合名称：

        ```java
        @Document(collection = "user")
        class User {}
        ```

        请注意，该注解等同于 Mongo 在 JPA 中的 @Entity。

    2. @Field

        通过 @Field，我们可以配置 MongoDB 持久化文档时要使用的字段名：

        ```java
        @Document
        class User {
            // ...
            @Field("email")
            String emailAddress;
            // ...
        }
        ```

        请注意，此注解等同于 Mongo 在 JPA 中的 @Column。

    3. @Query

        通过 @Query，我们可以在 MongoDB 存储库方法上提供查找器查询：

        ```java
        @Query("{ 'name' : ?0 }")
        List<User> findUsersByName(String name);
        ```

    4. @EnableMongoRepositories

        要使用 MongoDB 资源库，我们必须向 Spring 表明这一点。我们可以使用 @EnableMongoRepositories 来做到这一点。

        请注意，我们必须将此注解与 @Configuration 一起使用：

        ```java
        @Configuration
        @EnableMongoRepositories
        class MongoConfig {}
        ```

        Spring 将在此 @Configuration 类的子包中查找存储库。我们可以使用 basePackages 参数来改变这种行为：

        ```java
        @Configuration
        @EnableMongoRepositories(basePackages = "com.baeldung.repository")
        class MongoConfig {}
        ```

        另外请注意，如果 Spring Boot 在类路径上找到 Spring Data MongoDB，它会自动执行此操作。

5. 总结

    在本文中，我们了解了使用 Spring 处理一般数据所需的最重要注解。此外，我们还研究了最常见的 JPA 和 MongoDB 注释。

    与往常一样，GitHub 上的示例包括常见的 [JPA](https://github.com/eugenp/tutorials/tree/master/persistence-modules/spring-jpa) 注释和 [MongoDB](https://github.com/eugenp/tutorials/tree/master/persistence-modules/spring-data-mongodb) 注释。
