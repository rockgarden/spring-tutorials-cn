# [Hibernate @NotNull vs @Column(nullable = false)](https://www.baeldung.com/hibernate-notnull-vs-nullable)

1. 介绍

    乍一看，@NotNull和@Column（nullable = false）注释似乎都有相同的目的，并且可以互换使用。然而，正如我们很快就会看到的那样，这并不完全正确。

    尽管，当在JPA实体上使用时，它们基本上都阻止了在底层数据库中存储空值，但这两种方法之间存在显著差异。

    在本快速教程中，我们将比较@NotNull和@Column（nullable = false）约束。

2. 依赖性

    对于所有呈现的示例，我们将使用一个简单的Spring Boot应用程序。

    以下是pom.xml文件的相关部分，显示所需的依赖项：

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
    </dependencies>
    ```

    1. 样本实体

        我们还定义一个非常简单的实体，我们将在本教程中使用：

        ```java
        @Entity
        public class Item {

            @Id
            @GeneratedValue
            private Long id;

            private BigDecimal price;
        }
        ```

3. @NotNull注释

    @NotNull注释在[Bean Validation](https://www.baeldung.com/javax-validation)规范中定义。这意味着它的使用不仅限于实体。相反，我们也可以在任何其他bean上使用@NotNull。

    不过，让我们坚持使用我们的用例，并将@NotNull注释添加到项目的价格字段中：

    ```java
    @Entity
    public class Item {

        @Id
        @GeneratedValue
        private Long id;

        @NotNull
        private BigDecimal price;
    }
    ```

    现在，让我们尝试以空价格持续存在项目：

    ```java
    @SpringBootTest
    public class ItemIntegrationTest {

        @Autowired
        private ItemRepository itemRepository;

        @Test
        public void shouldNotAllowToPersistNullItemsPrice() {
            itemRepository.save(new Item());
        }
    }
    ```

    让我们看看Hibernate的输出：

    ```log
    2019-11-14 12:31:15.070 ERROR 10980 --- [ main] o.h.i.ExceptionMapperStandardImpl : 
    HHH000346: Error during managed flush [Validation failed for classes 
    [com.baeldung.h2db.springboot.models.Item] during persist time for groups 
    [javax.validation.groups.Default,] List of constraint violations:[
    ConstraintViolationImpl{interpolatedMessage='must not be null', propertyPath=price, rootBeanClass=class 
    com.baeldung.h2db.springboot.models.Item, 
    messageTemplate='{javax.validation.constraints.NotNull.message}'}]]
    
    (...)
    
    Caused by: javax.validation.ConstraintViolationException: Validation failed for classes 
    [com.baeldung.h2db.springboot.models.Item] during persist time for groups 
    [javax.validation.groups.Default,] List of constraint violations:[
    ConstraintViolationImpl{interpolatedMessage='must not be null', propertyPath=price, rootBeanClass=class 
    com.baeldung.h2db.springboot.models.Item, 
    messageTemplate='{javax.validation.constraints.NotNull.message}'}]
    ```

    正如我们所看到的，在这种情况下，我们的系统抛出了javax.validation.ConstraintViolationException。

    需要注意的是，Hibernate没有触发SQL插入语句。因此，无效数据没有保存到数据库中。

    这是因为在将查询发送到数据库之前，预坚持实体生命周期事件触发了bean验证。

    1. 模式生成

        在上一节中，我们介绍了@NotNull验证的工作原理。

        现在让我们来了解一下，如果我们让Hibernate为我们生成数据库模式会发生什么。

        出于这个原因，我们将在application.properties文件中设置几个属性：

        ```properties
        spring.jpa.hibernate.ddl-auto=create-drop
        spring.jpa.show-sql=true
        ```

        如果我们现在启动应用程序，我们将看到DDL语句：

        ```java
        create table item (
        id bigint not null,
            price decimal(19,2) not null,
            primary key (id)
        )
        ```

        令人惊讶的是，Hibernate自动将非空约束添加到价格列定义中。

        那怎么可能？

        事实证明，Hibernate开箱即用，将应用于实体的bean验证注释转换为DDL模式元数据。

        这非常方便，而且很有意义。如果我们将@NotNull应用于实体，我们很可能也想让相应的数据库列不为空。

        然而，如果出于任何原因，我们想要禁用此Hibernate功能，我们只需将thibernate.validator.apply_to_ddl属性设置为false。

        为了测试这一点，让我们更新我们的应用程序。属性：

        ```properties
        spring.jpa.hibernate.ddl-auto=create-drop
        spring.jpa.show-sql=true
        spring.jpa.properties.hibernate.validator.apply_to_ddl=false
        ```

        让我们运行应用程序并查看DDL语句：

        ```sql
        create table item (
        id bigint not null,
            price decimal(19,2),
            primary key (id)
        )
        ```

        不出所料，这次Hibernate没有在价格栏中添加非空约束。

4. @Column（nullable = false）注释

    @Column注释被定义为[Java Persistence API](https://jcp.org/en/jsr/detail?id=338)规范的一部分。

    它主要用于DDL模式元数据生成。这意味着，如果我们让Hibernate自动生成数据库模式，它将对特定数据库列应用非空约束。

    让我们用@Column（nullable = false）更新我们的项目实体，看看这在操作中是如何工作的：

    ```java
    @Entity
    public class Item {

        @Id
        @GeneratedValue
        private Long id;

        @Column(nullable = false)
        private BigDecimal price;
    }
    ```

    我们现在可以尝试保持空价格值：

    ```java
    @SpringBootTest
    public class ItemIntegrationTest {

        @Autowired
        private ItemRepository itemRepository;

        @Test
        public void shouldNotAllowToPersistNullItemsPrice() {
            itemRepository.save(new Item());
        }
    }
    ```

    这是Hibernate输出的片段：

    ```log
    Hibernate:

        create table item (
        id bigint not null,
            price decimal(19,2) not null,
            primary key (id)
        )

    (...)

    Hibernate:
        insert
        into
            item
            (price, id)
        values
            (?, ?)
    2019-11-14 13:23:03.000  WARN 14580 --- [main] o.h.engine.jdbc.spi.SqlExceptionHelper   :
    SQL Error: 23502, SQLState: 23502
    2019-11-14 13:23:03.000 ERROR 14580 --- [main] o.h.engine.jdbc.spi.SqlExceptionHelper   :
    NULL not allowed for column "PRICE"
    ```

    首先，我们可以注意到，正如我们预期的那样，Hibernate生成了非空约束的价格列。

    此外，它能够创建SQL插入查询并传递。因此，是底层数据库触发了错误。

    1. 验证

        几乎所有的来源都强调@Column（nullable = false）仅用于模式DDL生成。

        然而，Hibernate能够根据可能的空值对实体进行验证，即使相应的字段仅用@Column（nullable = false）进行注释。

        为了激活此Hibernate功能，我们需要显式地将hibernate.check_nullability属性设置为true：

        ```properties
        spring.jpa.show-sql=true
        spring.jpa.properties.hibernate.check_nullability=true
        ```

        现在让我们再次执行我们的测试用例并检查输出：

        ```log
        org.springframework.dao.DataIntegrityViolationException:
        not-null property references a null or transient value : com.baeldung.h2db.springboot.models.Item.price;
        nested exception is org.hibernate.PropertyValueException:
        not-null property references a null or transient value : com.baeldung.h2db.springboot.models.Item.price
        ```

        这一次，我们的测试用例抛出了org.hibernate.PropertyValueException。

        重要的是要注意，在这种情况下，Hibernate没有向数据库发送插入SQL查询。

5. 摘要

    在本文中，我们描述了@NotNull和@Column（nullable – false）注释是如何工作的。

    尽管两者都阻止我们在数据库中存储空值，但它们采取了不同的方法。

    作为经验法则，我们应该更喜欢@NotNull注释而不是@Column（nullable = false）注释。通过这种方式，我们确保在Hibernate向数据库发送任何插入或更新SQL查询之前进行验证。

    此外，通常最好依靠Bean验证中定义的标准规则，而不是让数据库处理验证逻辑。

    但是，即使我们让Hibernate生成数据库模式，它也会将@NotNull注释转换为数据库约束。然后，我们只能确保hibernate.validator.apply_to_ddl属性设置为true。
