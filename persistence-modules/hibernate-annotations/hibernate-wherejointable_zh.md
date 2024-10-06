# [Hibernate @WhereJoinTable 注释](https://www.baeldung.com/hibernate-wherejointable)

1. 一览表

    使用对象关系映射工具，如Hibernate，可以轻松将我们的数据读取到对象中，但可能会使复杂的数据模型难以形成我们的查询。

    多对多关系总是具有挑战性的，但当我们希望根据关系本身的某些属性获得相关实体时，可能会更具挑战性。

    在本教程中，我们将看看如何使用Hibernate的@WhereJoinTable注释解决这个问题。

2. 基本的@ManyToMany关系

    让我们从一个简单的@ManyToMany关系开始。我们需要域模型实体、关系实体和一些示例测试数据。

    1. 域名模型

        让我们想象一下，我们有两个简单的实体，用户和组，它们被关联为@ManyToMany：

        ```java
        @Entity(name = "users")
        public class User {

            @Id
            @GeneratedValue
            private Long id;
            private String name;

            @ManyToMany
            private List<Group> groups = new ArrayList<>();

            // standard getters and setters

        }

        @Entity
        public class Group {

            @Id
            @GeneratedValue
            private Long id;
            private String name;

            @ManyToMany(mappedBy = "groups")
            private List<User> users = new ArrayList<>();

            // standard getters and setters

        }
        ```

        正如我们所看到的，我们的用户实体可以是多个组实体的成员。同样，一个组实体可以包含多个用户实体。

    2. 关系实体

        对于@ManyToMany关联，我们需要一个单独的数据库表，称为关系表。关系表需要包含至少两列：相关用户和组实体的主键。

        只有两个主键列，我们的Hibernate映射就可以表示此关系表。

        然而，如果我们需要在关系表中放置其他数据，我们还应该为多对多关系本身定义一个关系实体。

        让我们创建UserGroupRelation类来做到这一点：

        ```java
        @Entity(name = "r_user_group")
        public class UserGroupRelation implements Serializable {

            @Id
            @Column(name = "user_id", insertable = false, updatable = false)
            private Long userId;

            @Id
            @Column(name = "group_id", insertable = false, updatable = false)
            private Long groupId;

        }
        ```

        在这里，我们命名了实体r_user_group，以便我们稍后可以引用它。

        对于我们的额外数据，假设我们想为每个组存储每个用户的角色。因此，我们将创建UserGroupRole枚举：

        ```java
        public enum UserGroupRole {
            MEMBER, MODERATOR
        }
        ```

        接下来，我们将向UserGroupRelation添加一个角色属性：

        ```java
        @Enumerated(EnumType.STRING)
        private UserGroupRole role;
        ```

        最后，为了正确配置它，我们需要在用户组集合上添加@JoinTable注释。在这里，我们将使用用户组关系的实体名称r_user_group指定连接表名称：

        ```java
        @ManyToMany
        @JoinTable(
            name = "r_user_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
        )
        private List<Group> groups = new ArrayList<>();
        ```

    3. 样本数据

        对于我们的集成测试，让我们定义一些样本数据：

        ```java
        public void setUp() {
            session = sessionFactory.openSession();
            session.beginTransaction();

            user1 = new User("user1");
            user2 = new User("user2");
            user3 = new User("user3");

            group1 = new Group("group1");
            group2 = new Group("group2");

            session.save(group1);
            session.save(group2);

            session.save(user1);
            session.save(user2);
            session.save(user3);

            saveRelation(user1, group1, UserGroupRole.MODERATOR);
            saveRelation(user2, group1, UserGroupRole.MODERATOR);
            saveRelation(user3, group1, UserGroupRole.MEMBER);

            saveRelation(user1, group2, UserGroupRole.MEMBER);
            saveRelation(user2, group2, UserGroupRole.MODERATOR);
        }

        private void saveRelation(User user, Group group, UserGroupRole role) {

            UserGroupRelation relation = new UserGroupRelation(user.getId(), group.getId(), role);
            
            session.save(relation);
            session.flush();
            session.refresh(user);
            session.refresh(group);
        }
        ```

        正如我们所看到的，用户1和用户2分为两组。此外，我们应该注意，虽然用户1是组1的主持人，但同时它在组2上具有成员角色。

3. 获取@ManyToMany关系

    现在我们已经正确配置了我们的实体，让我们获取用户实体的组。

    1. 简单的获取

        为了获取组，我们可以简单地在活动休眠会话中调用用户的getGroups（）方法：

        `List<Group> groups = user1.getGroups();`

        我们为小组的产出将是：

        `[Group [name=group1], Group [name=group2]]`

        但我们如何获得组角色只是主持人的用户组？

    2. 关系实体上的自定义过滤器

        我们可以使用@WhereJoinTable注释直接获取过滤的组。

        让我们将一个新属性定义为moderatorGroups，并在上面放置@WhereJoinTable注释。当我们通过此属性访问相关实体时，它将仅包含我们用户为MODERATOR的组。

        我们需要添加一个SQL where子句，以按MODERATOR角色过滤组：

        ```java
        @WhereJoinTable(clause = "role='MODERATOR'")
        @ManyToMany
        @JoinTable(
            name = "r_user_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
        )
        private List<Group> moderatorGroups = new ArrayList<>();
        ```

        因此，我们可以很容易地获得具有指定SQL的组，其中子句适用：

        `List<Group> groups = user1.getModeratorGroups();`

        我们的输出将是用户仅扮演主持人角色的组：

        `[Group [name=group1]]`

4. 结论

    在本教程中，我们学习了如何使用Hibernate的@WhereJoinTable注释根据关系表的属性过滤@ManyToMany集合。
