# [将Java记录与JPA一起使用](https://www.baeldung.com/spring-jpa-java-records)

1. 一览表

    在本教程中，我们将探索如何将Java记录与JPA一起使用。我们将首先探索为什么记录不能在实体中使用。

    然后，我们将看看如何将记录与JPA一起使用。我们还将研究如何在Spring Boot应用程序中使用Spring Data JPA的记录。

2. 记录与实体

    [记录](https://www.baeldung.com/java-record-keyword)是不可变的，用于存储数据。它们包含字段、all-args构造函数、getters、toString和equals/hashCode方法。由于它们是不变的，所以它们没有设置器。由于其简洁的语法，它们经常在Java应用程序中用作数据传输对象（DTO）。

    实体是映射到数据库表的类。它们用于表示数据库中的条目。它们的字段被映射到数据库表中的列。

    1. 记录不能是实体

        实体由JPA提供商处理。JPA提供商负责创建数据库表，将实体映射到表，并将实体持久化到数据库中。在Hibernate等流行的JPA提供商中，实体使用代理创建和管理。

        代理是在运行时生成并扩展实体类的类。这些代理依赖于实体类来拥有无args构造函数和设置器。由于记录没有这些，它们不能用作实体。

        2.2.使用JPA记录的其他方法

        由于在Java应用程序中使用记录的方便和安全，因此以其他方式与JPA一起使用可能是有益的。

        在JPA中，我们可以通过以下方式使用记录：

        - 将查询结果转换为记录
        - 使用记录作为DTO在图层之间传输数据
        - 将实体转换为记录。
        - 将记录用作复合主键的@IdClass

3. 项目设置

    我们将使用Spring Boot创建一个使用JPA和Spring Data JPA的简单应用程序。然后，我们将看看在与数据库交互时使用记录的几种方法。

    1. 依赖性

        让我们从将Spring Data JPA依赖项添加到我们的项目中开始：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>3.0.4</version>
        </dependency>
        ```

        除了Spring Data JPA之外，我们还需要配置一个数据库。我们可以使用任何SQL数据库。例如，我们可以使用[内存中的H2数据库](https://www.baeldung.com/spring-boot-h2-database)。

    2. 实体和记录

        让我们创建一个实体，用于与数据库交互。我们将创建一个图书实体，该实体将映射到数据库中的图书表：

        ```java
        @Entity
        @Table(name = "book")
        public class Book {
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
            private String title;
            private String author;
            private String isbn;

            // constructors, getters, setters
        }
        ```

        让我们也创建一个与图书实体对应的记录：

        `public record BookRecord(Long id, String title, String author, String isbn) {}`

        接下来，我们将看看在应用程序中使用记录而不是实体的几种方法。

4. 使用JPA记录

    JPA API提供了几种与数据库交互的方式，其中可以使用记录。让我们来看看其中的几个。

    1. Criteria Builder

        让我们先看看如何将记录与CriterariaBuilder一起使用。我们将进行查询，返回数据库中的所有书籍：

        ```java
        public class QueryService {
            @PersistenceContext
            private EntityManager entityManager;

            public List<BookRecord> findAllBooks() {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<BookRecord> query = cb.createQuery(BookRecord.class);
                Root<Book> root = query.from(Book.class);
                query.select(cb.construct(BookRecord.class, root.get("id"), root.get("title"), root.get("author"), root.get("isbn")));
                return entityManager.createQuery(query).getResultList();
            }
        }
        ```

        在上述代码中，我们使用CriteriaBuilder创建一个返回BookRecord的CriteraQuery。

        让我们来看看上述代码中的一些步骤：

        - 我们使用CriteriaBuilder.createQuery（）方法创建CriterareQuery。我们传递了想要返回的记录类作为参数
        - 然后我们使用CriteriaQuery.from()方法创建一个Root。我们传递实体类作为参数。这就是我们指定要查询的表的方式
        - 然后，我们使用Criterary.select（）方法来指定一个选择子句。我们使用CriterateBuilder.construct（）方法将查询结果转换为记录。我们将要传递给记录构造函数的记录类和实体的字段作为参数
        - 最后，我们使用EntityManager.createQuery（）方法从CritrateryQuery创建TypedQuery。然后我们使用TypedQuery.getResultList（）方法来获取查询结果

        这将创建一个选择查询，以获取数据库中的所有书籍。然后，它将使用construct（）方法将每个结果转换为BookRecord，当我们调用getResultList（）方法时，它将返回一个记录列表，而不是实体列表。

        通过这种方式，我们可以使用实体类来创建查询，但对应用程序的其余部分使用记录。

    2. Typed查询

        与CriteriaBuilder类似，我们可以使用类型化查询来返回记录而不是实体。让我们在我们的QueryService中添加一个方法，使用类型化查询获取单本书作为记录：

        ```java
        public BookRecord findBookByTitle(String title) {
            TypedQuery<BookRecord> query = entityManager
                .createQuery("SELECT new com.baeldung.recordswithjpa.records.BookRecord(b.id, b.title, b.author, b.isbn) " +
                            "FROM Book b WHERE b.title = :title", BookRecord.class);
            query.setParameter("title", title);
            return query.getSingleResult();
        }
        ```

        TypedQuery允许我们将查询结果转换为任何类型，只要该类型有一个构造函数，该构造函数的参数与查询结果相同。

        在上述代码中，我们使用EntityManager.createQuery（）方法来创建TypedQuery。我们传递查询字符串和记录的类作为参数。然后，我们使用TypedQuery.setParameter（）方法来设置查询的参数。最后，我们使用TypedQuery.getSingleResult（）方法来获取查询的结果，该结果将是一个BookRecord对象。

    3. Native查询

        我们还可以使用本机查询来获取查询结果作为记录。然而，本机查询不允许我们将结果转换为任何类型。相反，我们需要使用映射将结果转换为记录。首先，让我们在我们的实体中定义一个映射：

        ```java
        @SqlResultSetMapping(
        name = "BookRecordMapping",
        classes = @ConstructorResult(
            targetClass = BookRecord.class,
            columns = {
            @ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "title", type = String.class),
            @ColumnResult(name = "author", type = String.class),
            @ColumnResult(name = "isbn", type = String.class)
            }
        )
        )
        @Entity
        @Table(name = "book")
        public class Book {
            // ...
        }
        ```

        映射将以以下方式工作：

        - @SqlResultSetMapping注释的名称属性指定了映射的名称。
        - @ConstructorResult注释指定我们想要使用记录的构造函数来转换结果。
        - @ConstructorResult注释的targetClass属性指定了记录的类。
        - @ColumnResult注释指定列名和列类型。这些列值将传递给记录的构造函数。

        然后，我们可以在本机查询中使用此映射，将结果作为记录获取：

        ```java
        public List<BookRecord> findAllBooksUsingMapping() {
            Query query = entityManager.createNativeQuery("SELECT * FROM book", "BookRecordMapping");
            return query.getResultList();
        }
        ```

        这将创建一个返回数据库中所有书籍的本机查询。它将使用映射将结果转换为aBookRecord，当我们调用getResultList（）方法时，它将返回一个记录列表，而不是实体列表。

5. 将记录与Spring Data JPA一起使用

    Spring Data JPA为JPA API提供了一些改进。它使我们能够通过几种方式将记录与Spring Data JPA存储库一起使用。让我们看看如何将记录与Spring Data JPA存储库一起使用。

    1. 从实体到记录的自动映射

        Spring Data Repositories允许我们使用记录作为存储库中方法的返回类型。这将自动将实体映射到记录。只有当记录的字段与实体完全相同时，才有可能。让我们来看看一个例子：

        ```java
        public interface BookRepository extends JpaRepository<Book, Long> {
            List<BookRecord> findBookByAuthor(String author);
        }
        ```

        由于BookRecord的字段与Book实体相同，Spring Data JPA将自动将实体映射到记录，并在我们调用findBookByAuthor（）方法时返回记录列表，而不是实体列表。

    2. 使用带有@Query的记录

        与TypedQuery类似，我们可以在Spring Data JPA存储库中使用带有@Query注释的记录。让我们来看看一个例子：

        ```java
        public interface BookRepository extends JpaRepository<Book, Long> {
            @Query("SELECT new com.baeldung.jpa.records.BookRecord(b.id, b.title, b.author, b.isbn) FROM Book b WHERE b.id = :id")
            BookRecord findBookById(@Param("id") Long id);
        }
        ```

        Spring Data JPA将自动将查询结果转换为BookRecord，并在我们调用findBookById（）方法时返回单个记录而不是实体。

    3. 自定义存储库实现

        如果自动映射不是一个选项，我们也可以定义一个自定义存储库实现，允许我们定义自己的映射。让我们从创建一个CustomBookRecord类开始，该类将用作存储库中方法的返回类型：

        `public record CustomBookRecord(Long id, String title) {}`

        请注意，CustomBookRecord类的字段与Book实体不相同。它只有id和标题字段。

        然后，我们可以创建一个自定义存储库实现，该实现将使用CustomBookRecord类：

        ```java
        public interface CustomBookRepository {
            List<CustomBookRecord> findAllBooks();
        }
        ```

        在实现存储库时，我们可以定义用于将查询结果映射到CustomBookRecord类的方法：

        ```java
        @Repository
        public class CustomBookRepositoryImpl implements CustomBookRepository {
            private final JdbcTemplate jdbcTemplate;

            public CustomBookRepositoryImpl(JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
            }

            public List<CustomBookRecord> findAllBooks() {
                return jdbcTemplate.query("SELECT id, title FROM book", (rs, rowNum) -> new CustomBookRecord(rs.getLong("id"), rs.getString("title")));
            }
        }
        ```

        在上述代码中，我们使用JdbcTemplate.query（）方法执行查询，并使用lambda表达式将结果映射到CustomBookRecord中，该表达式是RowMapper接口的实现。

6. 将记录用作@Embeddables

    随着最近的[更新](https://in.relation.to/2023/03/30/orm-62-final/)，Hibernate现在支持将Java记录映射为可嵌入。我们可以使用Java记录来表示一组我们想要嵌入实体类中的相关属性：

    ```java
    @Embeddable
    public record Author (
        String firstName,
        String lastName
    ) {}
    ```

    在本例中，作者是标记为@Embeddable的Java记录。它有两个字段：名字和姓氏。我们可以在实体类中使用此记录来表示作者。要在我们的实体中使用此记录，我们应该用@Embedded注释标记它：

    ```java
    @Entity
    @Table(name = "embeadable_author_book")
    public class EmbeddableBook {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;
        @Embedded
        private Author author;
        private String isbn;
        //...
    }
    ```

    在6.2版本之前，Hibernate 6需要一些额外的工作才能使用Records。作者字段需要额外的注释@EmbeddableInstantiator（AuthorInstallator.class），我们应该提供EmbeddableInstantiator接口的实现：

    ```java
    public class AuthorInstallator implements EmbeddableInstantiator {

        public boolean isInstance(Object object, SessionFactoryImplementor sessionFactory) {
            return object instanceof Author;
        }

        public boolean isSameClass(Object object, SessionFactoryImplementor sessionFactory) {
            return object.getClass().equals(Author.class);
        }
        
        @Override
        public Object instantiate(final ValueAccess valueAccess, final SessionFactoryImplementor sessionFactoryImplementor) {
            final String firstName = valueAccess.getValue(0, String.class);
            final String secondName = valueAccess.getValue(1, String.class);
            return new Author(firstName, secondName);
        }
    }
    ```

    此外，Hibernate还支持使用结构化SQL类型来持久化结构。作者记录可以用@Struct注释进行标记，允许将记录映射到结构化SQL类型。当我们想要将记录映射到数据库中与结构相对应的复杂类型时，这非常有用。

7. 使用记录作为@IdClass

    在JPA中，实体可能有一个复合主密钥。通常，我们使用@IdClass注释来定义一个类，该类将用作实体的主键。自[最新](https://in.relation.to/2024/03/20/orm-65cr1/)的Hibernate更新以来，我们也可以使用记录作为实体的@IdClass。

    1. 使用记录定义@IdClass

        让我们来看看一个例子：

        `public record BookId(Long id, Long isbn) {}`

        在上述代码中，我们定义了一个BookId记录，该记录有两个字段：id和isbn。

    2. 在实体中使用@IdClass

        接下来，让我们使用BookId记录作为ComposideBook实体的@IdClass：

        ```java
        @Entity
        @Table(name = "book")
        @IdClass(BookId.class)
        public class CompositeBook {
            @Id
            private Long id;
            @Id
            private Long isbn;
            private String title;
            private String author;

            // constructors, getters, setters
        }
        ```

        在上述代码中，我们使用@IdClass注释将BookId记录链接到CompositeBook实体。id和isbn字段用@Id标记，可以使用BookId对象进行设置。

    3. 在存储库中使用记录

        我们也可以将BookId记录用作存储库中的主键：

        ```java
        @Repository
        public interface CompositeBookRepository extends JpaRepository<CompositeBook, BookId> {
        }
        ```

        在上述代码中，我们使用BookId记录作为存储库中CompositeBook实体的主键。

    4. 使用记录来获取数据

        由于我们将BookId记录添加到存储库中，我们可以使用它来获取或更新数据。让我们编写一个测试，将CompositeBook对象保存到数据库中，然后使用BookId记录获取它：

        ```java
        @SpringBootTest
        public class CompositeBookRepositoryIntegrationTest {
            @Autowired
            private CompositeBookRepository compositeBookRepository;

            @Test
            public void givenCompositeBook_whenSave_thenSaveToDatabase() {
                CompositeBook compositeBook = new CompositeBook(1L, 1234567890L, "Book Title", "Author Name");
                compositeBookRepository.save(compositeBook);
                
                CompositeBook savedBook = compositeBookRepository.findById(new BookId(1L, 1234567890L)).orElse(null);
                assertNotNull(savedBook);
                assertEquals("Book Title", savedBook.getTitle());
                assertEquals("Author Name", savedBook.getAuthor());
            }
        }
        ```

        正如我们所看到的，我们可以使用存储库的findById（）方法中的BookId记录从数据库中获取CompositeBook对象。

8. 结论

    在本文中，我们研究了如何将记录与JPA和Spring Data JPA一起使用。我们已经看到了如何使用CriteriarBuilder、TypedQuery和本机查询将记录与JPA API一起使用。我们还看到了如何使用自动映射、自定义查询、自定义存储库实现和作为@IdClass将记录与Spring Data JPA存储库一起使用。
