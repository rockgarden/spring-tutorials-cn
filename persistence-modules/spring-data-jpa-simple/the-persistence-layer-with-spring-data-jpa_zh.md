# [Spring数据JPA简介](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)

1. 一览表

    本教程将重点介绍Spring Data JPA到Spring项目中，并完全配置持久性层。有关使用基于Java的配置和项目的基本Maven pom设置Spring上下文的分步介绍，请参阅[本文](https://www.baeldung.com/bootstraping-a-web-application-with-spring-and-java-based-configuration)。

2. Spring数据生成的DAO——不再有DAO实现

    正如我们在之前的文章中讨论的那样，[DAO层](https://www.baeldung.com/simplifying-the-data-access-layer-with-spring-and-java-generics)通常由许多可以而且应该简化的模板代码组成。这种简化有很多优点：减少我们需要定义和维护的工件数量、数据访问模式的一致性以及配置的一致性。

    Spring Data将这种简化更进一步，并使完全删除DAO实现成为可能。DAO的接口现在是我们唯一需要明确定义的工件。

    要开始利用JPA的Spring Data编程模型，DAO接口需要扩展JPA specificRepository接口JpaRepository。这将使Spring Data能够找到此接口，并自动为其创建实现。

    通过扩展接口，我们获得了标准DAO中可用的标准数据访问最相关的CRUD方法。

3. 自定义访问方法和查询

    如前所述，通过实现其中一个存储库接口，DAO将已经定义和实现一些基本的CRUD方法（和查询）。

    为了定义更具体的访问方法，Spring JPA支持相当多的选项：

    - 只需在界面中定义一个新方法
    - 使用@Query注释提供实际的JPQL查询
    - 在Spring Data中使用更高级的规范和Querydsl支持
    - 通过JPA命名查询定义自定义查询

    [第三个选项](http://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/)，规格和Querydsl支持，类似于JPA标准，但使用更灵活、更方便的API。这使得整个操作更具可读性和可重复使用性。在处理大量固定查询时，此API的优势将变得更加明显，因为我们有可能通过较少的可重复使用块来更简洁地表达这些优势。

    最后一个选项的缺点是，它要么涉及XML，要么用查询给域类带来负担。

    1. 自动自定义查询

        当Spring Data创建新的存储库实现时，它会分析接口定义的所有方法，并尝试从方法名称自动生成查询。虽然这有一些限制，但它是一种非常强大和优雅的方式，可以毫不费力地定义新的自定义访问方法。

        让我们来看看一个例子。如果实体有一个名称字段（以及Java Bean标准getName和setName方法），我们将在DAO接口中定义findByName方法。这将自动生成正确的查询：

        ```java
        public interface IFooDAO extends JpaRepository<Foo, Long> {
            Foo findByName(String name);
        }
        ```

        这是一个相对简单的例子。查询创建机制支持[一组更大的关键字](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html#repositories.query-methods.query-creation)。

        如果解析器无法将属性与域对象字段匹配，我们将看到以下异常：

        `java.lang.IllegalArgumentException: No property nam found for type class com.baeldung.jpa.simple.model.Foo`

    2. 手动自定义查询

        现在让我们来看看我们将通过@Query注释定义的自定义查询：

        ```java
        @Query("SELECT f FROM Foo f WHERE LOWER(f.name) = LOWER(:name)")
        Foo retrieveByName(@Param("name") String name);
        ```

        对于对查询创建进行更精细的控制，例如使用命名参数或修改现有查询，参考是一个很好的开始。

4. Transaction配置

    春季管理的DAO的实际实施确实被隐藏了，因为我们没有直接与之合作。然而，这是一个足够简单的实现，SimpleJpaRepository，它使用注释定义事务语义。

    更明确地说，这在类级别使用只读@Transactional注释，然后对非只读方法进行覆盖。其余事务语义是默认的，但这些语义可以很容易地按方法手动覆盖。

    1. 例外翻译还Alive，很好

        现在的问题是：由于Spring Data JPA不依赖于旧的ORM模板（JpaTemplate、HibernateTemplate），并且它们自Spring 5以来已被删除，我们仍然会将JPA例外转换为Spring的DataAccessException层次结构吗？

        答案是，当然，我们是。通过在DAO上使用@Repository注释，仍然可以启用异常翻译。此注释使Spring bean后处理器能够使用容器中发现的所有PersistenceExceptionTranslator实例通知所有@Repository bean，并像以前一样提供异常翻译。

        让我们用集成测试来验证异常翻译：

        ```java
        @Test(expected = DataIntegrityViolationException.class)
        public void whenInvalidEntityIsCreated_thenDataException() {
            service.create(new Foo());
        }
        ```

        请记住，例外翻译是通过代理完成的。为了使Spring能够围绕DAO类创建代理，这些不能被宣布为最终的。

5. Spring Data JPA存储库配置

    为了激活Spring JPA存储库支持，我们可以使用@EnableJpaRepositories注释并指定包含DAO接口的软件包：

    ```java
    @EnableJpaRepositories(basePackages = "com.baeldung.jpa.simple.repository") 
    public class PersistenceConfig {...}
    ```

    我们可以用XML配置做同样的事情：

    <jpa:repositories base-package="com.baeldung.jpa.simple.repository" />

6. Java或XML配置

    在上一篇文章中，我们已经非常详细地讨论了如何在[Spring中配置JPA](https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa)。Spring Data还利用了Spring对JPA @PersistenceContext注释的支持。它使用这个将EntityManager连接到负责创建实际DAO实现的Spring工厂bean，JpaRepositoryFactoryBean。

    除了已经讨论的配置外，如果我们使用XML，我们还需要包含Spring Data XML Config：

    ```java
    @Configuration
    @EnableTransactionManagement
    @ImportResource("classpath*:*springDataConfig.xml")
    public class PersistenceJPAConfig {...}
    ```

7. Maven依赖性

    除了JPA的Maven配置外，如上[一篇文章](https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa)，我们将添加spring-data-jpa依赖项：

    ```xml
    <dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-jpa</artifactId>
    </dependency>
    ```

8. 使用Spring Boot

    我们还可以使用Spring Boot Starter Data JPA依赖项，它将自动为我们配置DataSource。

    我们需要确保我们想要使用的数据库存在于类路径中。在我们的示例中，我们添加了H2内存数据库：

    ```xml
    <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
    </dependency>
    ```

    因此，只需执行这些依赖项，我们的应用程序就可以启动并运行，我们可以将其用于其他数据库操作。

    标准Spring应用程序的显式配置现在包含在Spring Boot自动配置中。

    当然，我们可以通过添加我们自定义的显式配置来修改自动配置。

    Spring Boot提供了使用application.properties文件中的属性来执行此操作的简单方法。让我们看看更改连接URL和凭据的示例：

    ```properties
    spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
    spring.datasource.username=sa
    spring.datasource.password=sa
    ```

9. Spring数据JPA的有用工具

    所有主要的Java IDE都支持Spring Data JPA。让我们看看Eclipse和IntelliJ IDEA有哪些有用的工具。

    如果您使用Eclipse作为IDE，您可以安装[Dali Java持久工具](https://www.eclipse.org/webtools/dali/downloads.php)插件。这为JPA实体提供了ER图，DDL生成以初始化模式和基本的逆向工程功能。此外，您可以使用Eclipse Spring Tool Suite（STS）。它将有助于验证Spring Data JPA存储库中的查询方法名称。

    如果您使用IntelliJ IDEA，有两种选择。

    IntelliJ IDEA Ultimate启用ER图、用于测试JPQL语句的JPA控制台和有价值的检查。然而，这些功能在社区版中不可用。

    为了提高IntelliJ的生产力，您可以安装[JPA Buddy](https://plugins.jetbrains.com/plugin/15075-jpa-buddy)插件，该插件提供了许多功能，包括生成JPA实体、Spring Data JPA存储库、DTO、初始化DDL脚本、Flyway版本化迁移、Liquibase更改日志等。此外，JPA Buddy为逆向工程提供了一个高级工具。

    最后，JPA Buddy插件适用于社区版和终极版。

10. 结论

    在本文中，我们使用基于XML和Java的配置，介绍了Spring 5、JPA 2和Spring Data JPA（Spring Data伞式项目的一部分）的持久层的配置和实现。

    我们讨论了定义更高级自定义查询以及事务语义和使用新jpa命名空间的配置的方法。最终结果是使用Spring进行新的、优雅的数据访问，几乎没有实际的实施工作。
