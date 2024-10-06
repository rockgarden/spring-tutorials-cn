# [JPA实体生命周期事件](https://www.baeldung.com/jpa-entity-lifecycle-events)

1. 介绍

    与JPA合作时，在实体的生命周期内，我们可以收到一些事件的通知。在本教程中，我们将讨论JPA实体生命周期事件，以及当这些事件发生时，我们如何使用注释来处理回调和执行代码。

    我们将首先在实体本身上注释方法，然后继续使用实体侦听器。

2. JPA实体生命周期事件

    JPA指定了七个可选生命周期事件，这些事件被称为：

    - 在持续之前调用一个新实体 – @PrePersist
    - 持续存在后，需要一个新的实体 – @PostPersist
    - 在实体被移除之前 – @PreRemove
    - 实体被删除后 – @PostRemove
    - 更新操作前 – @PreUpdate
    - 实体更新后 – @PostUpdate
    - 实体加载后 – @PostLoad

    使用生命周期事件注释有两种方法：在实体中注释方法和使用注释回调方法创建实体听众。我们也可以同时使用两者。无论它们在哪里，回调方法都需要具有无效返回类型。

    因此，如果我们创建一个新实体并调用存储库的保存方法，我们的@PrePersist注释方法将被调用，然后记录插入数据库，最后，我们的@PostPersist方法被调用。如果我们使用@GeneratedValue自动生成主密钥，我们可以预期该密钥在@PostPersist方法中可用。

    对于@PostPersist、@PostRemove和@PostUpdate操作，文档提到，这些事件可以在操作发生后、刷新后或交易结束时发生。

    我们应该注意，只有当数据实际更改时，才会调用@PreUpdate回调——即当有实际的SQL更新语句需要运行时。无论是否真的有任何变化，都会调用@PostUpdate回调。

    如果我们的任何用于持久化或删除实体的回调抛出异常，该事务将被回滚。

3. 注释实体

    让我们直接在我们的实体中使用回调注释开始。在我们的示例中，当用户记录更改时，我们将留下日志跟踪，因此我们将在回调方法中添加简单的日志记录语句。

    此外，我们希望确保在用户从数据库中加载后，我们组装用户的全名。我们将通过使用@PostLoad注释方法来做到这一点。

    我们将从定义我们的用户实体开始：

    ```java
    @Entity
    public class User {
        private static Log log = LogFactory.getLog(User.class);

        @Id
        @GeneratedValue
        private int id;
        
        private String userName;
        private String firstName;
        private String lastName;
        @Transient
        private String fullName;

        // Standard getters/setters
    }
    ```

    接下来，我们需要创建一个用户存储库界面：

    ```java
    public interface UserRepository extends JpaRepository<User, Integer> {
        public User findByUserName(String userName);
    }
    ```

    现在，让我们回到我们的用户类并添加我们的回调方法：

    ```java
    @PrePersist
    public void logNewUserAttempt() {
        log.info("Attempting to add new user with username: " + userName);
    }

    @PostPersist
    public void logNewUserAdded() {
        log.info("Added user '" + userName + "' with ID: " + id);
    }

    @PreRemove
    public void logUserRemovalAttempt() {
        log.info("Attempting to delete user: " + userName);
    }

    @PostRemove
    public void logUserRemoval() {
        log.info("Deleted user: " + userName);
    }

    @PreUpdate
    public void logUserUpdateAttempt() {
        log.info("Attempting to update user: " + userName);
    }

    @PostUpdate
    public void logUserUpdate() {
        log.info("Updated user: " + userName);
    }

    @PostLoad
    public void logUserLoad() {
        fullName = firstName + " " + lastName;
    }
    ```

    当我们运行测试时，我们会看到一系列来自注释方法的日志记录语句。此外，当我们从数据库中加载用户时，我们可以可靠地期望用户的全名被填充。

4. 注释一个EntityListener

    我们现在将扩展我们的示例，并使用单独的EntityListener来处理我们的更新回调。如果我们有一些操作想要应用于所有实体，我们可能会更喜欢这种方法，而不是将方法放在我们的实体中。

    让我们创建我们的AuditTrailListener来记录用户表上的所有活动：

    ```java
    public class AuditTrailListener {
        private static Log log = LogFactory.getLog(AuditTrailListener.class);

        @PrePersist
        @PreUpdate
        @PreRemove
        private void beforeAnyUpdate(User user) {
            if (user.getId() == 0) {
                log.info("[USER AUDIT] About to add a user");
            } else {
                log.info("[USER AUDIT] About to update/delete user: " + user.getId());
            }
        }
        
        @PostPersist
        @PostUpdate
        @PostRemove
        private void afterAnyUpdate(User user) {
            log.info("[USER AUDIT] add/update/delete complete for user: " + user.getId());
        }
        
        @PostLoad
        private void afterLoad(User user) {
            log.info("[USER AUDIT] user loaded from database: " + user.getId());
        }
    }
    ```

    正如我们从示例中看到的，我们可以对一个方法应用多个注释。

    现在，我们需要回到我们的用户实体，并将@EntityListener注释添加到类中：

    ```java
    @EntityListeners(AuditTrailListener.class)
    @Entity
    public class User {
        //...
    }
    ```

    而且，当我们运行测试时，每个更新操作都会收到两组日志消息，并在用户从数据库中加载后收到一条日志消息。

5. 结论

    在本文中，我们了解了什么是JPA实体生命周期回调，以及何时调用它们。我们查看了注释，并讨论了使用它们的规则。我们还尝试在实体类和实体Listener类中使用它们。
