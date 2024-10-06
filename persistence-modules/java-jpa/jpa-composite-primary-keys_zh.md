# [JPA中的复合主键](https://www.baeldung.com/jpa-composite-primary-keys)

1. 介绍

    在本教程中，我们将了解JPA中的复合主键和相应的注释。

2. 复合主键

    复合主键，也称为复合键，是两个或多列的组合，以形成表的主键。

    在JPA中，我们有两个选项来定义复合键：@IdClass和@EmbeddedId注释。

    为了定义复合主键，我们应该遵循一些规则：

    - 复合主键类必须是公开的。
    - 它必须有一个无arg构造函数。
    - 它必须定义equals（）和hashCode（）方法。
    - 它必须是可序列化的。

3. IdClass注释

    假设我们有一个名为“Account”的表，它有两个列，即accountNumber和accountType，构成复合键。现在我们必须用JPA绘制地图。

    根据JPA规范，让我们用这些主键字段创建一个AccountId类：

    ```java
    public class AccountId implements Serializable {
        private String accountNumber;

        private String accountType;

        // default constructor

        public AccountId(String accountNumber, String accountType) {
            this.accountNumber = accountNumber;
            this.accountType = accountType;
        }

        // equals() and hashCode()
    }
    ```

    接下来，让我们将AccountId类与实体帐户相关联。

    为了做到这一点，我们需要用@IdClass注释注释实体。我们还必须在实体帐户中声明AccountId类中的字段，并用@Id注释它们：

    ```java
    @Entity
    @IdClass(AccountId.class)
    public class Account {
        @Id
        private String accountNumber;

        @Id
        private String accountType;

        // other fields, getters and setters
    }
    ```

4. EmbeddedId注释

    @EmbeddedId是@IdClass注释的替代。

    让我们考虑另一个例子，即我们必须保留一本书的一些信息，标题和语言作为主要关键字段。

    在这种情况下，主键类BookId必须用@Embeddable进行注释：

    ```java
    @Embeddable
    public class BookId implements Serializable {
        private String title;
        private String language;

        // default constructor

        public BookId(String title, String language) {
            this.title = title;
            this.language = language;
        }

        // getters, equals() and hashCode() methods
    }
    ```

    然后我们需要使用@EmbeddedId将此类嵌入到图书实体中：

    ```java
    @Entity
    public class Book {
        @EmbeddedId
        private BookId bookId;

        // constructors, other fields, getters and setters
    }
    ```

5. @IdClass vs @EmbeddedId

    正如我们所看到的，两者之间的表面区别在于，使用@IdClass，我们必须指定列两次，一次在AccountId中，一次在Account中；然而，使用@EmbeddedId，我们没有。

    不过，还有其他一些权衡。

    例如，这些不同的结构会影响我们编写的JPQL查询。

    使用@IdClass，查询更简单一些：

    `SELECT account.accountNumber FROM Account account`

    使用@EmbeddedId，我们必须做一次额外的遍历：

    `SELECT book.bookId.title FROM Book book`

    此外，在我们使用无法修改的复合密钥类的地方，@IdClass可能非常有用。

    如果我们要单独访问复合密钥的部分内容，我们可以使用@IdClass，但在我们经常使用完整标识符作为对象的地方，首选@EmbeddedId。

6. 使用复合主键计数查询

    我们可以使用JPQL和CriteraryQuery来计算使用嵌入式ID作为主键的实体。

    1. 使用JPQL

        我们可以编写一个JPQL查询来对实体进行计数，并使用实体manager.getSingleResult（）方法执行它，返回图书实体的计数：

        ```java
        long countBookByEmbeddedIdUsingJPQL() {
            String jpql = "SELECT count(b) FROM Book b";
            Query query = em.createQuery(jpql);
            return (long) query.getSingleResult();
        }
        ```

        我们可以通过单元测试来验证结果：

        ```java
        @Test
        public void givenBookWithEmbeddedId_whenCountCalled_thenReturnsCount() {
            IntStream.rangeClosed(1, 10)
            .forEach(i -> {
                BookId bookId = new BookId("Book" + i, "English");
                Book book = new Book(bookId);
                book.setDescription("Novel and Historical Fiction" + i);
                persist(book);
            });
            assertEquals(10, countBookByEmbeddedIdUsingJPQL());
            assertEquals(10, countBookByEmbeddedIdUsingCriteriaQuery());
        }
        ```

    2. 使用CriteriaQuery

        我们可以使用CriteriaQuery使用嵌入式ID来计算实体。首先，我们构建根查询，然后使用getSingleResult()返回根查询的计数：

        ```java
        long countBookByEmbeddedIdUsingCriteriaQuery() {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<Book> root = criteriaQuery.from(Book.class);
            criteriaQuery.select(criteriaBuilder.count(root));

            return em.createQuery(criteriaQuery).getSingleResult();
        }
        ```

        我们可以通过单元测试来验证结果：

        ```java
        @Test
        public void givenBookWithEmbeddedId_whenCountCalled_thenReturnsCount() {
            IntStream.rangeClosed(1, 10)
            .forEach(i -> {
                BookId bookId = new BookId("Book" + i, "English");
                Book book = new Book(bookId);
                book.setDescription("Novel and Historical Fiction" + i);
                persist(book);
            });
            assertEquals(10, countBookByEmbeddedIdUsingJPQL());
            assertEquals(10, countBookByEmbeddedIdUsingCriteriaQuery());
        }
        ```

7. 结论

    在这篇简短的文章中，我们探讨了JPA中的复合主键。
