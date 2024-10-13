# [Spring Data Cassandra简介](https://www.baeldung.com/spring-data-cassandra-tutorial)

1. 一览表

    本文是对与Cassandra和Spring Data合作的实用介绍。

    我们将从基础知识开始，完成配置和编码，最终构建一个完整的Spring Data Cassandra模块。

2. Maven附属机构

    让我们从使用Maven定义pom.xml中的依赖项开始：

    ```xml
    <dependency>
        <groupId>com.datastax.cassandra</groupId>
        <artifactId>cassandra-driver-core</artifactId>
        <version>2.1.9</version>
    </dependency>
    ```

3. Cassandra的配置

    我们将在整个过程中使用Java风格的配置来配置Cassandra集成。

    1. 主配置（Spring）

        我们将为此使用Java配置风格。让我们从主配置类开始——当然，通过类级@Configuration注释驱动：

        ```java
        @Configuration
        public class CassandraConfig extends AbstractCassandraConfiguration {

            @Override
            protected String getKeyspaceName() {
                return "testKeySpace";
            }

            @Bean
            public CassandraClusterFactoryBean cluster() {
                CassandraClusterFactoryBean cluster = 
                new CassandraClusterFactoryBean();
                cluster.setContactPoints("127.0.0.1");
                cluster.setPort(9142);
                return cluster;
            }

            @Bean
            public CassandraMappingContext cassandraMapping() 
            throws ClassNotFoundException {
                return new BasicCassandraMappingContext();
            }
        }
        ```

        注意新bean – BasicCassandraMappingContext – 具有默认实现。这是在对象和持久格式之间映射持久实体所必需的。

        并且，由于默认实现功能足够强大，我们可以直接使用它。

    2. 主配置（Spring Boot）

        让我们通过application.properties进行Cassandra配置：

        ```properties
        spring.data.cassandra.keyspace-name=testKeySpace
        spring.data.cassandra.port=9142
        spring.data.cassandra.contact-points=127.0.0.1
        ```

        我们完成了！这就是我们使用Spring Boot时所需要的一切。

    3. Cassandra连接属性

        我们必须配置三个强制性设置才能为Cassandra客户端设置连接。

        我们必须设置Cassandra服务器作为contactPoints运行的主机名。端口只是服务器中请求的监听端口。KeyspaceName是定义节点上数据复制的命名空间，该名称空间基于Cassandra相关概念。

4. Cassandra存储库

    我们将使用CassandraRepository作为数据访问层。这遵循了Spring数据存储库抽象，该抽象侧重于抽象跨不同持久机制实现数据访问层所需的代码。

    1. 创建CassandraRepository

        让我们创建CassandraRepository，用于配置：

        ```java
        @Repository
        public interface BookRepository extends CassandraRepository<Book> {
            //
        }
        ```

    2. CassandraRepository的配置

        现在，我们可以扩展第3.1节中的配置，添加@EnableCassandraRepositories类级注释，以标记CassandraConfig第4.1节中创建的Cassandra存储库：

        ```java
        @Configuration
        @EnableCassandraRepositories(
        basePackages = "com.baeldung.spring.data.cassandra.repository")
        public class CassandraConfig extends AbstractCassandraConfiguration {
            //
        }
        ```

5. 实体

    让我们快速看一下实体——我们将要使用的模型类。该类是注释的，并在嵌入式模式下为元数据Cassandra数据表创建定义附加参数。

    使用@Table注释，bean直接映射到Cassandra数据表。此外，每个属性被定义为一种主键或简单列：

    ```java
    @Table
    public class Book {
        @PrimaryKeyColumn(
        name = "isbn",
        ordinal = 2,
        type = PrimaryKeyType.CLUSTERED,
        ordering = Ordering.DESCENDING)
        private UUID id;
        @PrimaryKeyColumn(
        name = "title", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String title;
        @PrimaryKeyColumn(
        name = "publisher", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String publisher;
        @Column
        private Set<String> tags = new HashSet<>();
        // standard getters and setters
    }
    ```

6. 使用嵌入式服务器进行测试

    1. Maven附属机构

        如果您想在嵌入式模式下运行Cassandra（无需手动安装单独的Cassandra服务器），您需要将cassandra-unit相关的依赖项添加到pom.xml中：

        ```xml
        <dependency>
            <groupId>org.cassandraunit</groupId>
            <artifactId>cassandra-unit-spring</artifactId>
            <version>2.1.9.2</version>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                <groupId>org.cassandraunit</groupId>
                <artifactId>cassandra-unit</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.cassandraunit</groupId>
            <artifactId>cassandra-unit-shaded</artifactId>
            <version>2.1.9.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.hectorclient</groupId>
            <artifactId>hector-core</artifactId>
            <version>2.0-0</version>
        </dependency>
        ```

        可以使用嵌入式Cassandra服务器来测试此应用程序。主要优点是您不想明确地安装Cassandra。

        这个嵌入式服务器也与Spring JUnit Tests兼容。在这里，我们可以将SpringJUnit4ClassRunner与嵌入式服务器一起使用@RunWith注释设置。因此，在没有外部Cassandra服务运行的情况下，可以实现完整的测试套件。

        ```java
        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = CassandraConfig.class)
        public class BookRepositoryIntegrationTest {
            //
        }
        ```

    2. 启动和停止服务器

        如果您正在运行外部Cassandra服务器，您可以忽略此部分。

        我们必须为整个测试套件启动一次服务器，因此服务器启动方法标有@BeforeClassannotation：

        ```java
        @BeforeClass
        public static void startCassandraEmbedded() {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            Cluster cluster = Cluster.builder()
            .addContactPoints("127.0.0.1").withPort(9142).build();
            Session session = cluster.connect();
        }
        ```

        接下来，我们必须确保服务器在完成测试套件执行后停止：

        ```java
        @AfterClass
        public static void stopCassandraEmbedded() {
            EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        }
        ```

    3. 清洁数据表

        在每次测试执行之前删除并创建数据表是一个很好的做法，以避免早期测试执行中因操作数据而导致的意外结果。

        现在，我们可以在服务器启动时创建数据表：

        ```java
        @Before
        public void createTable() {
            adminTemplate.createTable(
            true, CqlIdentifier.cqlId(DATA_TABLE_NAME),
            Book.class, new HashMap<String, Object>());
        }
        ```

        并在每个测试用例执行后删除：

        ```java
        @After
        public void dropTable() {
            adminTemplate.dropTable(CqlIdentifier.cqlId(DATA_TABLE_NAME));
        }
        ```

7. 使用CassandraRepository访问数据

    我们可以直接使用上面创建的BookRepository来持久化、操作和获取Cassandra数据库中的数据。

    1. 保存一本新书

        我们可以把一本新书留给我们的书店：

        ```java
        Book javaBook = new Book(
        UUIDs.timeBased(), "Head First Java", "O'Reilly Media",
        ImmutableSet.of("Computer", "Software"));
        bookRepository.save(ImmutableSet.of(javaBook));
        ```

        然后，我们可以在数据库中检查插入的书籍的可用性：

        ```java
        Iterable<Book> books = bookRepository.findByTitleAndPublisher(
        "Head First Java", "O'Reilly Media");
        assertEquals(javaBook.getId(), books.iterator().next().getId());
        ```

    2. 更新一本现有书籍

        Lat从插入一本新书开始：

        ```java
        Book javaBook = new Book(
        UUIDs.timeBased(), "Head First Java", "O'Reilly Media",
        ImmutableSet.of("Computer", "Software"));
        bookRepository.save(ImmutableSet.of(javaBook));
        ```

        让我们根据书名来获取这本书：

        ```java
        Iterable<Book> books = bookRepository.findByTitleAndPublisher(
        "Head First Java", "O'Reilly Media");
        ```

        然后让我们更改一下书名：

        ```java
        javaBook.setTitle("Head First Java Second Edition");
        bookRepository.save(ImmutableSet.of(javaBook));
        ```

        最后，让我们检查一下数据库中是否更新了标题：

        ```java
        Iterable<Book> books = bookRepository.findByTitleAndPublisher(
        "Head First Java Second Edition", "O'Reilly Media");
        assertEquals(
        javaBook.getTitle(), updateBooks.iterator().next().getTitle());
        ```

    3. 删除现有书籍

        插入一本新书：

        ```java
        Book javaBook = new Book(
        UUIDs.timeBased(), "Head First Java", "O'Reilly Media",
        ImmutableSet.of("Computer", "Software"));
        bookRepository.save(ImmutableSet.of(javaBook));
        ```

        然后删除新输入的书：

        `bookRepository.delete(javaBook);`

        现在我们可以检查删除：

        ```java
        Iterable<Book> books = bookRepository.findByTitleAndPublisher(
        "Head First Java", "O'Reilly Media");
        assertNotEquals(javaBook.getId(), books.iterator().next().getId());
        ```

        这将导致从代码中抛出NoSuchElementException，以确保书籍被删除。

    4. 找到所有书籍

        先插入一本新书：

        ```java
        Book javaBook = new Book(
        UUIDs.timeBased(), "Head First Java", "O'Reilly Media",
        ImmutableSet.of("Computer", "Software"));
        Book dPatternBook = new Book(
        UUIDs.timeBased(), "Head Design Patterns","O'Reilly Media",
        ImmutableSet.of("Computer", "Software"));
        bookRepository.save(ImmutableSet.of(javaBook));
        bookRepository.save(ImmutableSet.of(dPatternBook));
        ```

        查找所有书籍：

        `Iterable<Book> books = bookRepository.findAll();`

        然后我们可以检查数据库中可用书籍的数量：

        ```java
        int bookCount = 0;
        for (Book book : books) bookCount++;
        assertEquals(bookCount, 2);
        ```

8. 结论

    我们使用CassandraRepository数据访问机制的最常见方法，对Cassandra与Spring数据进行了基本的实践介绍。
