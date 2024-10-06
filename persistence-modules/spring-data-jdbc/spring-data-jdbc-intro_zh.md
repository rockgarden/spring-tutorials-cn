# [Spring Data JDBC简介](https://www.baeldung.com/spring-data-jdbc-intro)

1. 一览表

    Spring Data JDBC是一个持久性框架，不像Spring Data JPA那样复杂。它不提供缓存、延迟加载、写入或JPA的许多其他功能。尽管如此，它有自己的ORM，并提供了我们与Spring Data JPA一起使用的大多数功能，如映射实体、存储库、查询注释和JdbcTemplate。

    需要记住的一件重要事情是，Spring Data JDBC不提供模式生成。因此，我们负责明确地创建模式。

2. 将Spring Data JDBC添加到项目中

    Spring Data JDBC可用于带有JDBC依赖启动器的Spring Boot应用程序。不过，这个依赖启动器不会带来数据库驱动程序。该决定必须由开发商做出。让我们为Spring Data JPA添加依赖启动器：

    ```xml
    <dependency> 
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>
    ```

    在这个例子中，我们使用的是H2数据库。正如我们前面提到的，Spring Data JDBC不提供模式生成。在这种情况下，我们可以创建一个自定义的schema.sql文件，该文件将包含用于创建模式对象的SQL DDL命令。Spring Boot将自动选择此文件，并用于创建数据库对象。

3. 添加实体

    与其他Spring Data项目一样，我们使用注释将POJO映射到数据库表中。在Spring Data JDBC中，实体需要有一个@Id。Spring Data JDBC使用@Id注释来识别实体。

    与Spring Data JPA类似，Spring Data JDBC默认使用命名策略，将Java实体映射到关系数据库表，并将属性映射到列名。默认情况下，实体和属性的骆驼大小写名称分别映射到表格和列的蛇形大小写名称。例如，一个名为AddressBook的Java实体被映射到一个名为address_book的数据库表。

    此外，我们可以使用@Table和@Column注释，用表和列明确映射实体和属性。例如，下面我们定义了我们将在本例中使用的实体：

    ```java
    public class Person {
        @Id
        private long id;
        private String firstName;
        private String lastName;
        // constructors, getters, setters
    }
    ```

    我们不需要在Person类中使用注释@Table或@Column。Spring Data JDBC的默认命名策略隐式在实体和表之间进行所有映射。

4. 声明JDBC存储库

    Spring Data JDBC使用的语法与Spring Data JPA相似。我们可以通过扩展存储库、CrudRepository或PagingAndSortingRepository接口来创建Spring Data JDBC存储库。通过实现CrudRepository，我们收到了最常用的方法的实现，如保存、删除和findById等。

    让我们创建一个JDBC存储库，我们将在示例中使用：

    ```java
    @Repository
    public interface PersonRepository extends CrudRepository<Person, Long> {
    }
    ```

    如果我们需要分页和排序功能，最好的选择是扩展PagingAndSortingRepository接口。

5. 自定义JDBC存储库

    尽管CrudRepository有内置方法，但我们需要为特定情况创建方法。

    现在，让我们用非修改查询和修改查询来自定义我们的PersonRepository：

    ```java
    @Repository
    public interface PersonRepository extends CrudRepository<Person, Long> {

        List<Person> findByFirstName(String firstName);

        @Modifying
        @Query("UPDATE person SET first_name = :name WHERE id = :id")
        boolean updateByFirstName(@Param("id") Long id, @Param("name") String name);
    }
    ```

    从2.0版本开始，Spring Data JDBC支持[查询方法](https://docs.spring.io/spring-data/relational/reference/jdbc/query-methods.html)。也就是说，如果我们命名包含关键字的查询方法，例如findByFirstName，Spring Data JDBC将自动生成查询对象。

    然而，对于修改查询，我们使用@Modifying注释来注释修改实体的查询方法。此外，我们用@Query注释来装饰它。

    在@Query注释中，我们添加了SQL命令。在Spring Data JDBC中，我们用纯SQL编写查询。我们不使用任何像JPQL这样的更高级别的查询语言。因此，应用程序与数据库供应商紧密耦合。

    出于这个原因，更改为不同的数据库也变得更加困难。

    我们需要记住的一件事是，Spring Data JDBC不支持用索引编号引用参数。我们只能通过名称引用参数。

6. 填充数据库

    最后，我们需要在数据库中填充用于测试我们上面创建的Spring Data JDBC存储库的数据。因此，我们将创建一个数据库 seeder，该数据将插入虚拟数据。让我们在这个例子中添加数据库seeder的实现：

    ```java
    @Component
    public class DatabaseSeeder {

        @Autowired
        private JdbcTemplate jdbcTemplate;
        public void insertData() {
            jdbcTemplate.execute("INSERT INTO Person(first_name,last_name) VALUES('Victor', 'Hugo')");
            jdbcTemplate.execute("INSERT INTO Person(first_name,last_name) VALUES('Dante', 'Alighieri')");
            jdbcTemplate.execute("INSERT INTO Person(first_name,last_name) VALUES('Stefan', 'Zweig')");
            jdbcTemplate.execute("INSERT INTO Person(first_name,last_name) VALUES('Oscar', 'Wilde')");
        }
    }
    ```

    如上所示，我们使用Spring JDBC来执行INSERT语句。特别是，Spring JDBC处理与数据库的连接，并允许我们使用JdbcTemplates执行SQL命令。这个解决方案非常灵活，因为我们可以完全控制已执行的查询。

7. 结论

    总之，Spring Data JDBC提供了一个与使用Spring JDBC一样简单的解决方案——它背后没有魔力。尽管如此，它还提供了我们习惯使用Spring Data JPA的大部分功能。

    与Spring Data JPA相比，Spring Data JDBC的最大优势之一是在访问数据库时提高了性能。这是因为Spring Data JDBC直接与数据库通信。在查询数据库时，Spring Data JDBC不包含大部分Spring Data魔术。

    使用Spring Data JDBC时最大的缺点之一是对数据库供应商的依赖。如果我们决定将数据库从MySQL更改为Oracle，我们可能不得不处理具有不同方言的数据库所产生的问题。
