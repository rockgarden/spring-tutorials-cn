# [Spring Data JPA删除和关系](https://www.baeldung.com/spring-data-jpa-delete)

1. 一览表

    在本教程中，我们将看看如何在Spring Data JPA中完成删除。

2. 样本实体

    正如我们从[Spring Data JPA参考文档](https://docs.spring.io/spring-data/jpa/reference/repositories/definition.html)中了解的那样，存储库接口为我们提供了对实体的一些基本支持。

    假设我们有一个实体，比如一本书：

    ```java
    @Entity
    public class Book {

        @Id
        @GeneratedValue
        private Long id;
        private String title;

        // standard constructors

        // standard getters and setters
    }
    ```

    然后，我们可以扩展Spring Data JPA的CrudRepository，让我们可以访问Book上的CRUD操作：

    ```java
    @Repository
    public interface BookRepository extends CrudRepository<Book, Long> {}
    ```

3. 从存储库中删除

    除其他外，CrudRepository包含两种方法：deleteById和deleteAll。

    让我们直接从我们的BookRepository测试这些方法：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = {Application.class})
    public class DeleteFromRepositoryUnitTest {

        @Autowired
        private BookRepository repository;

        Book book1;
        Book book2;
        List<Book> books;

        // data initialization

        @Test
        public void whenDeleteByIdFromRepository_thenDeletingShouldBeSuccessful() {
            repository.deleteById(book1.getId());
            assertThat(repository.count()).isEqualTo(1);
        }

        @Test
        public void whenDeleteAllFromRepository_thenRepositoryShouldBeEmpty() {
            repository.deleteAll();
            assertThat(repository.count()).isEqualTo(0);
        }
    }
    ```

    尽管我们正在使用CrudRepository，但请注意，这些相同的方法存在于其他Spring Data JPA接口上，如JpaRepository或PagingAndSortingRepository。

4. 派生删除查询

    我们还可以推导出删除实体的查询方法。编写它们有一套规则，但让我们专注于最简单的例子。

    派生删除查询必须以deleteBy开头，后跟选择标准名称。必须在方法调用中提供这些标准。

    比方说，我们想按标题删除书籍。使用命名惯例，我们将从deleteBy和列表标题作为标准开始：

    ```java
    @Repository
    public interface BookRepository extends CrudRepository<Book, Long> {
        long deleteByTitle(String title);
    }
    ```

    类型为long的返回值表示该方法删除了多少条记录。

    让我们写一个测试，并确保它是正确的：

    ```java
    @Test
    @Transactional
    public void whenDeleteFromDerivedQuery_thenDeletingShouldBeSuccessful() {
        long deletedRecords = repository.deleteByTitle("The Hobbit");
        assertThat(deletedRecords).isEqualTo(1);
    }
    ```

    在JPA中持久和删除对象需要交易。这就是为什么在使用这些派生删除查询时，我们应该使用@Transactional注释，以确保事务正在运行。这一点在带有[Spring文档的ORM](https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#orm)中得到了详细的解释。

5. 自定义删除查询

    派生查询的方法名可能相当长，并且仅限于单个表。

    当我们需要更复杂的东西时，我们可以一起使用@Query和@Modifying一起编写自定义查询。

    让我们检查一下我们之前衍生方法的等效代码：

    ```java
    @Modifying
    @Query("delete from Book b where b.title=:title")
    void deleteBooks(@Param("title") String title);
    ```

    同样，我们可以通过一个简单的测试来验证它有效：

    ```java
    @Test
    @Transactional
    public void whenDeleteFromCustomQuery_thenDeletingShouldBeSuccessful() {
        repository.deleteBooks("The Hobbit");
        assertThat(repository.count()).isEqualTo(1);
    }
    ```

    上面介绍的两种解决方案相似，并取得了相同的结果。然而，他们采取了略有不同的方法。

    @Query方法针对数据库创建单个JPQL查询。相比之下，deleteBy方法执行读取查询，然后逐一删除每个项目。

6. 在关系中删除

    现在让我们看看当我们与其他实体建立关系时会发生什么。

    假设我们有一个类别实体，该实体与图书实体具有OneToMany关联：

    ```java
    @Entity
    public class Category {

        @Id
        @GeneratedValue
        private Long id;
        private String name;

        @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Book> books;

        // standard constructors

        // standard getters and setters
    }
    ```

    CategoryRepository可以只是一个扩展CrudRepository的空接口：

    ```java
    @Repository
    public interface CategoryRepository extends CrudRepository<Category, Long> {}
    ```

    我们还应该修改图书实体，以反映这一关联：

    ```java
    @ManyToOne
    private Category category;
    ```

    现在让我们添加两个类别，并将它们与我们目前拥有的书籍联系起来。

    现在，如果我们尝试删除类别，书籍也会被删除：

    ```java
    @Test
    public void whenDeletingCategories_thenBooksShouldAlsoBeDeleted() {
        categoryRepository.deleteAll();
        assertThat(bookRepository.count()).isEqualTo(0);
        assertThat(categoryRepository.count()).isEqualTo(0);
    }
    ```

    不过，这不是双向的，这意味着如果我们删除书籍，类别仍然存在：

    ```java
    @Test
    public void whenDeletingBooks_thenCategoriesShouldAlsoBeDeleted() {
        bookRepository.deleteAll();
        assertThat(bookRepository.count()).isEqualTo(0);
        assertThat(categoryRepository.count()).isEqualTo(2);
    }
    ```

    我们可以通过更改关系的属性来更改这种行为，例如CascadeType。

7. 结论

    在本文中，我们看到了在Spring Data JPA中删除实体的不同方法。

    我们查看了CrudRepository提供的删除方法，以及使用@Query注释的派生查询或自定义查询。

    我们还看到了删除是如何在关系中完成的。
