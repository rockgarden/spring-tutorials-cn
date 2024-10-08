# [带有Spring Data JPA规范的连接表](https://www.baeldung.com/spring-jpa-joining-tables)

1. 一览表

    在本简短的教程中，我们将讨论Spring Data JPA规范的高级功能，该功能允许我们在创建查询时加入表。

    让我们从JPA规范及其用法的简要回顾开始。

2. JPA规格

    Spring Data JPA引入了规范接口，允许我们使用可重复使用的组件创建动态查询。

    对于本文中的代码示例，我们将使用作者和图书类：

    ```java
    @Entity
    public class Author {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String firstName;

        private String lastName;

        @OneToMany(cascade = CascadeType.ALL)
        private List<Book> books;

        // getters and setters
    }
    ```

    为了为作者实体创建动态查询，我们可以使用规范接口的实现：

    ```java
    public class AuthorSpecifications {

        public static Specification<Author> hasFirstNameLike(String name) {
            return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.<String>get("firstName"), "%" + name + "%");
        }

        public static Specification<Author> hasLastName(String name) {
            return (root, query, cb) ->
            cb.equal(root.<String>get("lastName"), name);
        }
    }
    ```

    最后，我们需要AuthorRepository来扩展JpaSpecificationExecutor：

    ```java
    @Repository
    public interface AuthorsRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
    }
    ```

    因此，我们现在可以将两个规范链在一起，并用它们创建查询：

    ```java
    @Test
    public void whenSearchingByLastNameAndFirstNameLike_thenOneAuthorIsReturned() {

        Specification<Author> specification = hasLastName("Martin")
        .and(hasFirstNameLike("Robert"));

        List<Author> authors = repository.findAll(specification);

        assertThat(authors).hasSize(1);
    }
    ```

3. 带有JPA规范的连接表

    我们可以从我们的数据模型中观察到，作者实体与图书实体共享一对多关系：

    ```java
    @Entity
    public class Book {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;

        // getters and setters
    }
    ```

    标准查询API允许我们在创建规范时加入两个表。因此，我们将能够在我们的查询中包含来自图书实体的字段：

    ```java
    public static Specification<Author> hasBookWithTitle(String bookTitle) {
        return (root, query, criteriaBuilder) -> {
            Join<Book, Author> authorsBook = root.join("books");
            return criteriaBuilder.equal(authorsBook.get("title"), bookTitle);
        };
    }
    ```

    现在让我们把这个新规范与之前创建的规范结合起来：

    ```java
    @Test
    public void whenSearchingByBookTitleAndAuthorName_thenOneAuthorIsReturned() {

        Specification<Author> specification = hasLastName("Martin")
        .and(hasBookWithTitle("Clean Code"));

        List<Author> authors = repository.findAll(specification);

        assertThat(authors).hasSize(1);
    }
    ```

    最后，让我们看看生成的SQL，看看JOIN子句：

    ```sql
    select
    author0_.id as id1_1_,
    author0_.first_name as first_na2_1_,
    author0_.last_name as last_nam3_1_
    from
    author author0_
    inner join author_books books1_ on author0_.id = books1_.author_id
    inner join book book2_ on books1_.books_id = book2_.id
    where
    author0_.last_name = ?
    and book2_.title = ?
    ```

4. 结论

    在本文中，我们学习了如何使用JPA规范根据其关联实体之一查询表。

    Spring Data JPA的规范带来了一种流畅、动态和可重复使用的创建查询的方式。
