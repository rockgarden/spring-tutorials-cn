# [使用Hibernate生成UUID作为主键](https://www.baeldung.com/java-hibernate-uuid-primary-key)

1. 介绍

    UUID是数据库中使用的一种相对常见的[主键类型](https://www.baeldung.com/uuid-vs-sequential-id-as-primary-key)。它实际上在全球范围内是独一无二的，这使得它成为分布式系统中ID类型的好选择。

    在本教程中，我们将看看如何利用Hibernate和JPA为我们的实体生成UUID。

2. JPA/Jakarta规范

    首先，我们将看看JPA为解决这个问题提供了什么。

    自2022年发布的3.1.0版本以来，JPA规范为开发人员提供了一个newGenerationType.UUID，我们可以在@GeneratedValue注释中使用：

    ```java
    @Entity
    class Reservation {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        private String status;

        private String number;

        // getters and setters
    }
    ```

    GenerationType指示，实体的UUID应由持久性提供程序自动为我们生成。

    特别是Hibernate，支持6.2版本的JPA 3.1.0。所以，至少拥有[Hibernate 6.2](https://hibernate.org/orm/releases/6.2/)，这将有效：

    ```java
    @Test
    public void whenGeneratingUUIDUsingNewJPAGenerationType_thenHibernateGeneratedUUID() throws IOException {
        Reservation reservation = new Reservation();
        reservation.setStatus("created");
        reservation.setNumber("12345");
        UUID saved = (UUID) session.save(reservation);
        Assertions.assertThat(saved).isNotNull();
    }
    ```

    然而，在RFC 4122中，定义了四种类型/版本的UUID。JPA规范将UUID版本的选择留给持久性提供程序。因此，不同的持久性提供程序可能会生成不同版本的UUID。

    默认情况下，Hibernate生成第4版本的UUID：

    ```java
    @Test
    public void whenGeneratingUUIDUsingNewJPAGenerationType_thenHibernateGeneratedUUIDOfVersion4() throws IOException {
        Reservation reservation = new Reservation();
        reservation.setStatus("new");
        reservation.setNumber("012");
        UUID saved = (UUID) session.save(reservation);
        Assertions.assertThat(saved).isNotNull();
        Assertions.assertThat(saved.version()).isEqualTo(4);
    }
    ```

    就[RFC 4122](https://www.ietf.org/rfc/rfc4122.txt)而言，Hibernate能够创建两个版本的UUID——1和4。我们稍后将了解如何生成基于时间的（版本1）UUID。

3. 在Hibernate6.2之前

    在某些项目中，可能无法从JPA规范2.x跳转到JPA（或雅加达）规范3.1.0。然而，如果我们有Hibernate版本4或5，我们仍然能够生成UUID。为此，我们有两种方法。

    首先，我们可以通过在@GenericGenerator注释中指定org.hibernate.id.UUIDGenerator类来实现这一点：

    ```java
    @Entity
    class Sale {

        @Id
        @GeneratedValue(generator = "uuid-hibernate-generator")
        @GenericGenerator(name = "uuid-hibernate-generator", strategy = "org.hibernate.id.UUIDGenerator")
        private UUID id;

        private boolean completed;

        //getters and setters
    }
    ```

    行为将与Hibernate6.2相同：

    ```java
    @Test
    public void whenGeneratingUUIDUsingGenericConverter_thenAlsoGetUUIDGeneratedVersion4() throws IOException {
        Sale sale = new Sale();
        sale.setCompleted(true);
        UUID saved = (UUID) session.save(sale);
        Assertions.assertThat(saved).isNotNull();
        Assertions.assertThat(saved.version()).isEqualTo(4);
    }
    ```

    然而，这种方法相当冗长，我们只需使用theorg.hibernate.annotations.UuidGenerator注释就可以获得相同的行为：

    ```java
    @Entity
    class Sale {

        @Id
        @UuidGenerator
        private UUID id;

        private boolean completed;

        // getters and setters 
    }
    ```

    此外，在指定@UuidGenerator时，我们可以选择要生成的UUID的具体版本。这是由样式参数定义的。让我们看看这个参数可以取的值：

    - 随机-根据随机数生成UUID（RFC版本4）
    - 时间-生成基于时间的UUID（RFC第1版）
    - 自动——这是默认选项，与随机相同

    让我们看看如何控制Hibernate生成的UUID版本：

    ```java
    @Entity
    class WebSiteUser {

        @Id
        @UuidGenerator(style = UuidGenerator.Style.TIME)
        private UUID id;

        private LocalDate registrationDate;

        // getters and setters
    }
    ```

    现在，正如我们可能检查的那样，Hibernate将生成基于时间的（版本1）UUID：

    ```java
    @Test
    public void whenGeneratingTimeBasedUUID_thenUUIDGeneratedVersion1() throws IOException {
        WebSiteUser user = new WebSiteUser();
        user.setRegistrationDate(LocalDate.now());
        UUID saved = (UUID) session.save(user);
        Assertions.assertThat(saved).isNotNull();
        Assertions.assertThat(saved.version()).isEqualTo(1);
    }
    ```

4. 字符串作为UUID

    此外，如果我们使用String作为Java ID类型，Hibernate足够智能，可以为我们生成UUID：

    ```java
    @Entity
    class Element {

        @Id
        @UuidGenerator
        private String id;

        private String name;
    }
    ```

    正如我们所看到的，Hibernate可以同时处理字符串和UUID Java类型：

    ```java
    @Test
    public void whenGeneratingUUIDAsString_thenUUIDGeneratedVersion1() throws IOException {
        Element element = new Element();
        element.setName("a");
        String saved = (String) session.save(element);
        Assertions.assertThat(saved).isNotEmpty();
        Assertions.assertThat(UUID.fromString(saved).version()).isEqualTo(4);
    }
    ```

    在这里，我们应该注意，当我们将列设置为类型为java.util.UUID时，Hibernate会尝试将其映射到数据库中相应的UUID类型。这种类型的数据库可能因数据库而异。

    因此，确切的类型实际上取决于冬眠方言集。例如，如果我们使用PostgreSQL，那么PostgreSQL中的相应类型将是UUID。如果我们使用Microsoft SQL Server，那么相应的类型将是UNIQUEIDENTIFIER。然而，如果我们使用String作为Java ID类型，那么Hibernate会将其映射到一些SQL文本类型中，如TEXT或VARCHAR。

5. 结论

    在本文中，我们学习了使用Hibernate生成UUID的不同方法。

    在雅加达3.1.0规范和Hibernate 6.2之前，有几种选择。从Hibernate 6.2开始，我们可以使用新的JPA GenerationType.UUID独立于持久性提供程序生成UUID。

    然而，JPA规范没有指定生成的UUID的版本。如果我们想要指定一个具体版本，我们需要使用Hibernate特定的类，我们仍然有两个选项——版本1或版本4。
