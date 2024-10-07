# [Spring Data JPA @修改注释](https://www.baeldung.com/spring-data-jpa-modifying-annotation)

1. 介绍

    在这个简短的教程中，我们将学习如何使用Spring Data JPA @Query注释创建更新查询。我们将通过使用@Modifying注释来实现这一点。

    首先，为了刷新我们的记忆，我们可以阅读如何使用Spring Data JPA进行查询。之后，我们将深入研究@Query和@Modifying注释的使用。最后，我们将讨论在使用修改查询时如何管理持久性上下文的状态。

2. 在Spring Data JPA中查询

    首先，让我们总结一下Spring Data JPA为查询数据库中的数据提供的三种机制：

    - 查询方法
    - @Query注释
    - 自定义存储库实现

    让我们创建一个用户类和一个匹配的Spring Data JPA存储库来说明这些机制：

    ```java
    @Entity
    @Table(name = "users", schema = "users")
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        private String name;
        private LocalDate creationDate;
        private LocalDate lastLoginDate;
        private boolean active;
        private String email;

    }

    public interface UserRepository extends JpaRepository<User, Integer> {}
    ```

    查询方法机制允许我们通过从方法名称中推出查询来操作数据：

    ```java
    List<User> findAllByName(String name);
    void deleteAllByCreationDateAfter(LocalDate date);
    ```

    在本例中，我们可以找到按用户姓名检索用户的查询，或删除在特定日期后具有创建日期的用户的查询。

    至于@Query注释，它为我们提供了在@Query注释中编写特定JPQL或SQL查询的机会：

    ```java
    @Query("select u from User u where u.email like '%@gmail.com'")
    List<User> findUsersWithGmailAddress();
    ```

    在此代码片段中，我们可以看到一个查询，检索具有@gmail.com电子邮件地址的用户。

    第一个机制使我们能够检索或删除数据。至于第二个机制，它允许我们执行几乎任何查询。然而，为了更新查询，我们必须添加@Modifying注释。这将是本教程的主题。

3. 使用@Modifying注释

    [@Modifying注释](https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/Modifying.html)用于增强@Query注释，这样我们不仅可以执行SELECT查询，还可以执行INSERT、UPDATE、DELETE甚至DL查询。

    现在让我们玩玩这个注释。

    首先，让我们看看@Modifying UPDATE查询的示例：

    ```java
    @Modifying
    @Query("update User u set u.active = false where u.lastLoginDate < :date")
    void deactivateUsersNotLoggedInSince(@Param("date") LocalDate date);
    ```

    在这里，我们正在停用自给定日期以来尚未登录的用户。

    让我们尝试另一个，我们将删除停用的用户：

    ```java
    @Modifying
    @Query("delete User u where u.active = false")
    int deleteDeactivatedUsers();
    ```

    正如我们所看到的，这种方法返回一个整数。这是Spring Data JPA @Modifying查询的一个功能，它为我们提供了更新的实体数量。

    我们应该注意，使用@Query执行删除查询的工作方式与Spring Data JPA的deleteBy名称衍生查询方法不同。后者首先从数据库中获取实体，然后逐一删除它们。这意味着生命周期方法@PreRemove将在这些实体上调用。然而，在前者中，对数据库执行单个查询。

    最后，让我们用DDL查询将已删除的列添加到我们的用户表中：

    ```java
    @Modifying
    @Query(value = "alter table USERS.USERS add column deleted int(1) not null default 0", nativeQuery = true)
    void addDeletedColumn();
    ```

    不幸的是，使用修改查询会使底层持久性上下文过时。然而，有可能处理好这种情况。这是下一节的主题。

    1. 不使用@Modifying注释的结果

        让我们看看当我们不将@Modifying注释放在删除查询上时会发生什么。

        出于这个原因，我们需要创建另一种方法：

        ```java
        @Query("delete User u where u.active = false")
        int deleteDeactivatedUsersWithNoModifyingAnnotation();
        ```

        注意缺失的注释。

        当我们执行上述方法时，我们会得到一个InvalidDataAccessApiUsage异常：

        ```log
        org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.hql.internal.QueryExecutionRequestException: 
        Not supported for DML operations [delete com.baeldung.boot.domain.User u where u.active = false]
        (...)
        ```

        错误消息非常清晰；DML操作不支持查询。

4. 管理持久性上下文

    如果我们的修改查询更改了持久性上下文中包含的实体，那么此上下文就会过时。管理这种情况的一种方法是[清除持久性上下文](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html#clear--)。通过这样做，我们确保持久性上下文下次将从数据库中获取实体。

    然而，我们不必在EntityManager上显式调用clear（）方法。我们只需使用@Modifying注释中的[clearAutomatically](https://codingexplained.com/coding/java/spring-framework/updating-entities-with-update-query-spring-data-jpa)属性：

    `@Modifying(clearAutomatically = true)`

    通过这种方式，我们确保在查询执行后清除持久性上下文。

    然而，如果我们的持久性上下文包含未清除的更改，则清除它将意味着删除未保存的更改。幸运的是，在这种情况下，我们可以使用注释的另一个属性，flushAutomatically：

    `@Modifying(flushAutomatically = true)`

    现在，在我们的查询执行之前，EntityManager被刷新了。

5. 结论

    这篇关于@Modifying注释的简短文章到此结束。我们学习了如何使用此注释来执行更新查询，如INSERT、UPDATE、DELE，甚至DDL。在那之后，我们讨论了如何使用clearAutomatically和flushAutomatically属性来管理持久上下文的状态。
