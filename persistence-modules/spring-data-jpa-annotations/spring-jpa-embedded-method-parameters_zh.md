# [Spring JPA @Embedded和@EmbeddedId](https://www.baeldung.com/spring-jpa-embedded-method-parameters)

1. 一览表

    在本教程中，我们将介绍使用@EmbeddedId注释和“findBy”方法来查询基于复合密钥的JPA实体。

    因此，我们将使用@EmbeddeId和@Embeddable注释来表示JPA实体中的复合键。我们还需要使用Spring JpaRepository来实现我们的目标。

    我们将专注于通过部分主键查询对象。

2. 需要@Embeddable和@EmbeddedId

    在软件中，我们遇到了许多用例，我们需要一个复合主键来定义表中的条目。复合主键是使用多个列来唯一地识别表中的行的键。

    我们通过在类上使用@Embeddable注释来表示Spring Data中的复合主键。然后，通过在@Embeddable类型的字段上使用@EmbeddedId注释，将此键作为复合主键嵌入到表的相应实体类中。

3. 示例：

    考虑一个图书表，其中图书记录有一个由作者和姓名组成的复合主键。有时，我们可能想通过主键的一部分来查找书籍。例如，用户可能只想搜索特定作者的书籍。我们将学习如何用JPA做到这一点。

    我们的主要应用程序将由@Embeddable BookId和@Entity Book with @EmbeddedId BookId组成。

    1. @Embeddable

        让我们在本节中定义我们的BookId类。作者和名称将指定一个唯一的BookId——类isSerializable，并实现equals和hashCode方法：

        ```java
        @Embeddable
        public class BookId implements Serializable {

            private String author;
            private String name;

            // standard getters and setters
        }
        ```

    2. @Entity和@EmbeddedId

        我们的图书实体有@EmbeddedId BookId和其他与书籍相关的字段。BookId告诉JPA，Book实体有一个复合密钥：

        ```java
        @Entity
        public class Book {

            @EmbeddedId
            private BookId id;
            private String genre;
            private Integer price;

            //standard getters and setters
        }
        ```

    3. JPA存储库和方法命名

        让我们通过使用实体Book和BookId扩展JpaRepository来快速定义我们的JPA存储库接口：

        ```java
        @Repository
        public interface BookRepository extends JpaRepository<Book, BookId> {

            List<Book> findByIdName(String name);

            List<Book> findByIdAuthor(String author);
        }
        ```

        我们使用id变量的字段名称的一部分来推导我们的Spring Data查询方法。因此，JPA将部分主键查询解释为：

        ```java
        findByIdName -> directive "findBy" field "id.name"
        findByIdAuthor -> directive "findBy" field "id.author"
        ```

4. 结论

    JPA可用于有效地映射复合密钥，并通过派生查询查询它们。

    在本文中，我们看到了运行部分id字段搜索的小例子。我们查看了表示复合主键的@Embeddable注释和在实体中插入复合键的@EmbeddedId注释。

    最后，我们看到了如何使用JpaRepository findBy派生方法使用部分id字段进行搜索。
