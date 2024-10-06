# [JPA实体和可序列化接口](https://www.baeldung.com/jpa-entities-serializable)

1. 介绍

    在本教程中，我们将讨论JPA实体和Java Serializable接口如何融合。首先，我们将看看java.io.Serializable接口，以及我们为什么需要它。之后，我们将看看JPA规范和Hibernate作为其最受欢迎的实现。

2. 什么是可序列化接口？

    Serializable是核心Java中为数不多的标记接口之一。标记接口是没有方法或常量的特殊情况接口。

    对象序列化是将Java对象转换为字节流的过程。然后，我们可以通过线传输这些字节流，或将它们存储在持久内存中。反序列化是相反的过程，我们使用字节流并将其转换为Java对象。要允许对象序列化（或反序列化），类必须实现可序列化接口。否则，我们将遇到java.io.NotSerializableException。序列化广泛用于RMI、JPA和EJB等技术。

3. JPA和可序列化

    让我们看看JPA规范对Serializable有什么规定，以及它与Hibernate有什么关系。

    1. JPA规范

        JPA的核心部分之一是实体类。我们将此类类标记为实体（使用@Entity注释或XML描述符）。我们的实体类必须满足几个要求，根据[JPA规范](https://download.oracle.com/otn-pub/jcp/persistence-2_1-fr-eval-spec/JavaPersistence.pdf)，我们最关心的要求是：

        > 如果实体实例要作为分离对象（例如，通过远程接口）通过值传递，则实体类必须实现可序列化接口。

        在实践中，如果我们的目标是离开JVM的域，则需要序列化。

        每个实体类由持久字段和属性组成。该规范要求实体的字段可以是Java原语、Java可序列化类型或用户定义的可序列化类型。

        实体类还必须有一个主键。主键可以是原始的（单个持久字段）或复合的。多种规则适用于复合密钥，其中之一是复合密钥需要可序列化。

        让我们使用Hibernate、H2内存内数据库和以UserId为复合键的用户域对象创建一个简单的示例：

        ```java
        @Entity
        public class User {
            @EmbeddedId UserId userId;
            String email;
            
            // constructors, getters and setters
        }

        @Embeddable
        public class UserId implements Serializable{
            private String name;
            private String lastName;
            
            // getters and setters
        }
        ```

        我们可以使用集成测试来测试我们的域定义：

        ```java
        @Test
        public void givenUser_whenPersisted_thenOperationSuccessful() {
            UserId userId = new UserId();
            userId.setName("John");
            userId.setLastName("Doe");
            User user = new User(userId, "johndoe@gmail.com");

            entityManager.persist(user);

            User userDb = entityManager.find(User.class, userId);
            assertEquals(userDb.email, "johndoe@gmail.com");
        }
        ```

        如果我们的UserId类没有实现可序列化接口，我们将收到一个MappingException，并带有具体消息，即我们的复合键必须实现该接口。

    2. Hibernate @JoinColumn 注释

        Hibernate官方[文档](https://hibernate.org/orm/documentation/)在描述Hibernate中的映射时，指出，当我们使用@JoinColumn注释中的referencedColumnName时，引用字段必须是可序列化的。通常，此字段是另一个实体中的主键。在极少数情况下，复杂实体类，我们的引用必须是可序列化的。

        让我们扩展上一个用户类，其中电子邮件字段不再是字符串，而是独立实体。此外，我们将添加一个帐户类，该类将引用用户，并具有字段类型。每个用户可以拥有多个不同类型的帐户。我们将通过电子邮件映射帐户，因为按电子邮件地址搜索更自然：

        ```java
        @Entity
        public class User {
            @EmbeddedId private UserId userId;
            private Email email;
        }

        @Entity
        public class Email implements Serializable {
            @Id
            private long id;
            private String name;
            private String domain;
        }

        @Entity
        public class Account {
            @Id
            private long id;
            private String type;
            @ManyToOne
            @JoinColumn(referencedColumnName = "email")
            private User user;
        }
        ```

        为了测试我们的模型，我们将编写一个测试，为用户创建两个帐户，并通过电子邮件对象进行查询：

        ```java
        @Test
        public void givenAssociation_whenPersisted_thenMultipleAccountsWillBeFoundByEmail() {
            // object creation

            entityManager.persist(user);
            entityManager.persist(account);
            entityManager.persist(account2);

            List<Account> userAccounts = entityManager.createQuery("select a from Account a join fetch a.user where a.user.email = :email", Account.class)
            .setParameter("email", email)
            .getResultList();
            
            assertEquals(userAccounts.size(), 2);
        }
        ```

        注意：用户是H2数据库中的保留词，不能用于实体名称。

        如果电子邮件类没有实现可序列化接口，我们将再次获得MappingException，但这次有一个有点神秘的消息：“Could not determine type”。

    3. 将实体暴露在Presentation层中

        当使用HTTP通过电线发送对象时，我们通常会为此目的创建特定的DTO（数据传输对象）。通过创建DTO，我们将内部域对象与外部服务分离。如果我们想在没有DTO的情况下将实体直接暴露在演示层中，那么实体必须是可序列化的。

        我们使用HttpSession对象来存储相关数据，这些数据有助于我们识别多个页面访问网站的用户。当优雅关闭或集群环境中将会话数据传输到另一个网络服务器时，Web服务器可以将会话数据存储在磁盘上。如果一个实体是此过程的一部分，那么它必须是可序列化的。否则，我们将遇到NotSerializableException。

4. 结论

    在本文中，我们涵盖了Java序列化的基础知识，并了解了它如何在JPA中发挥作用。首先，我们讨论了JPA规范中关于可序列化的要求。在那之后，我们研究了Hibernate作为JPA最受欢迎的实现。最后，我们涵盖了JPA实体如何与Web服务器一起工作。
