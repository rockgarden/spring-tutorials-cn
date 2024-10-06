# [Spring JDBC批量插入](https://www.baeldung.com/spring-jdbc-batch-inserts)

1. 一览表

    在本教程中，我们将学习如何使用Spring JDBC批处理支持有效地将大量数据插入到目标RDBMS中，我们将比较使用批处理插入和多个单个插入的性能。

2. 了解批处理

    一旦我们的应用程序与数据库建立了连接，我们就可以一次性执行多个SQL语句，而不是逐个发送每个语句。因此，我们大大减少了通信开销。

    实现这一目标的一个选项是使用Spring JDBC API，这是以下章节的重点。

    1. 支持数据库

        尽管JDBC API提供了批处理功能，但不能保证我们使用的底层JDBC驱动程序实际上已经实现了这些API并支持此功能。

        Spring提供了一个名为JdbcUtils.supportsBatchUpdates（）的实用方法，该方法将JDBC连接作为参数，并简单地返回true或false。然而，在大多数情况下，使用JdbcTemplate API时，Spring已经为我们检查了它，否则会恢复到正常行为。

    2. 可能影响整体绩效的因素

        在插入大量数据时，我们应该考虑几个方面：

        - 我们创建的与数据库服务器对话的连接数量
        - 我们正在插入的表格
        - 我们为执行单个逻辑任务而发出的数据库请求数量

        通常，为了克服第一点，我们使用连接池。这有助于重复使用现有的连接，而不是创建新的连接。

        另一个重要点是目标表。准确地说，我们索引的列越多，性能就越差，因为数据库服务器需要在每个新行后调整索引。

        最后，我们可以使用批处理支持来减少插入大量条目的往返次数。

        然而，我们应该意识到，并非所有JDBC驱动程序/数据库服务器都为批处理操作提供相同的效率水平，即使它们支持它。例如，虽然Oracle、Postgres、SQL Server和DB2等数据库服务器提供了显著的收益，但MySQL在没有任何额外配置的情况下提供了较差的收益。

3. Spring JDBC批量插入

    在本例中，我们将使用Postgres 14作为我们的数据库服务器。因此，我们需要将相应的postgresql JDBC驱动程序添加到我们的依赖项中：

    ```xml
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    ```

    然后，为了使用Spring的JDBC抽象，让我们也添加spring-boot-starter-jdbc依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    ```

    出于演示目的，我们将探索2种不同的方法：首先，我们将对每个记录进行定期插入，然后我们将尝试利用批处理支持。无论哪种情况，我们都会使用单笔交易。

    让我们先从我们简单的产品表开始：

    ```sql
    CREATE TABLE product (
        id              SERIAL PRIMARY KEY,
        title           VARCHAR(40),
        created_ts      timestamp without time zone,
        price           numeric
    );
    ```

    以下是相应的模型产品类别：

    ```java
    public class Product {
        private long id;
        private String title;
        private LocalDateTime createdTs;
        private BigDecimal price;
    
        // standard setters and getters
    }
    ```

    1. 配置数据源

        通过将以下配置添加到我们的应用程序中。属性Spring Boot为我们创建了DataSource和JdbcTemplate bean：

        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/sample-baeldung-db
        spring.datasource.username=postgres
        spring.datasource.password=root
        spring.datasource.driver-class-name=org.postgresql.Driver
        ```

    2. 准备常规插入物

        我们首先创建一个简单的存储库界面来保存产品列表：

        ```java
        public interface ProductRepository {
            void saveAll(List<Product> products);
        }
        ```

        然后，第一个实现只需迭代产品，并在同一事务中逐一插入它们：

        ```java
        @Repository
        public class SimpleProductRepository implements ProductRepository {

            private JdbcTemplate jdbcTemplate;

            public SimpleProductRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            }

            @Override
            @Transactional
            public void saveAll(List<Product> products) {
            for (Product product : products) {
                jdbcTemplate.update("INSERT INTO PRODUCT (TITLE, CREATED_TS, PRICE) " +
                "VALUES (?, ?, ?)",
                product.getTitle(),
                Timestamp.valueOf(product.getCreatedTs()),
                product.getPrice());
            }
            }

        }
        ```

        现在，我们需要一个服务类ProductService，该服务类生成给定数量的产品对象并启动插入过程。首先，我们有一个方法，使用一些预定义的值以随机方式生成给定数量的产品实例：

        ```java
        public class ProductService {

            private ProductRepository productRepository;
            private Random random;
            private Clock clock;

            // constructor for the dependencies

            private List<Product> generate(int count) {
                final String[] titles = { "car", "plane", "house", "yacht" };
                final BigDecimal[] prices = {
                new BigDecimal("12483.12"),
                new BigDecimal("8539.99"),
                new BigDecimal("88894"),
                new BigDecimal("458694")
                };

                final List<Product> products = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    Product product = new Product();
                    product.setCreatedTs(LocalDateTime.now(clock));
                    product.setPrice(prices[random.nextInt(4)]);
                    product.setTitle(titles[random.nextInt(4)]);
                    products.add(product);
                }
                return products;
            }
        }
        ```

        其次，我们在ProductService类中添加了另一种方法，该方法将生成的产品实例插入并插入它们：

        ```java
        @Transactional
        public long createProducts(int count) {
            List<Product> products = generate(count);
            long startTime = clock.millis();
            productRepository.saveAll(products);
            return clock.millis() - startTime;
        }
        ```

        要使ProductService成为Spring bean，让我们也添加以下配置：

        ```java
        @Configuration
        public class AppConfig {

            @Bean
            public ProductService simpleProductService(SimpleProductRepository simpleProductRepository) {
            return new ProductService(simpleProductRepository, new Random(), Clock.systemUTC());
            }
        }
        ```

        正如我们所看到的，这个ProductService bean使用SimpleProductRepository来执行定期插入。

    3. 准备批量插入物

        现在，是时候看到Spring JDBC批量支持的实施了。首先，让我们开始为我们的ProductRepository类创建另一个批量实现：

        ```java
        @Repository
        public class BatchProductRepository implements ProductRepository {

            private JdbcTemplate jdbcTemplate;

            public BatchProductRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            }

            @Override
            @Transactional
            public void saveAll(List<Product> products) {
            jdbcTemplate.batchUpdate("INSERT INTO PRODUCT (TITLE, CREATED_TS, PRICE) " +
                "VALUES (?, ?, ?)",
                products,
                100,
                (PreparedStatement ps, Product product) -> {
                ps.setString(1, product.getTitle());
                ps.setTimestamp(2, Timestamp.valueOf(product.getCreatedTs()));
                ps.setBigDecimal(3, product.getPrice());
                });
            }
        }
        ```

        需要注意的是，对于这个例子，我们使用批处理大小为100。这意味着Spring将每100个插入件进行批处理，并分别发送。换句话说，它将帮助我们减少100倍的往返次数。

        通常，建议的批处理大小为50-100，但它在很大程度上取决于我们的数据库服务器配置和每个批处理包的大小。

        例如，MySQL Server具有名为max_allowed_packet的配置属性，每个网络包的限制为64MB。在设置批处理大小时，我们需要小心不要超过我们的数据库服务器限制。

        现在，我们在AppConfig类中添加了一个额外的ProductService bean配置：

        ```java
        @Bean
        public ProductService batchProductService(BatchProductRepository batchProductRepository) {
        return new ProductService(batchProductRepository, new Random(), Clock.systemUTC());
        }
        ```

4. 性能对比

    是时候举例说明并看看基准了。为了简单起见，我们通过实现Spring提供的CommandLineRunner接口来准备一个命令行Spring Boot应用程序。我们为这两种方法多次运行示例：

    ```java
    @SpringBootApplication
    public class SpringJdbcBatchPerformanceApplication implements CommandLineRunner {

        @Autowired
        @Qualifier("batchProductService")
        private ProductService batchProductService;
        @Autowired
        @Qualifier("simpleProductService")
        private ProductService simpleProductService;

        public static void main(String[] args) {
        SpringApplication.run(SpringJdbcBatchPerformanceApplication.class, args);
        }

        @Override
        public void run(String... args) throws Exception {
        int[] recordCounts = {1, 10, 100, 1000, 10_000, 100_000, 1000_000};

        for (int recordCount : recordCounts) {
            long regularElapsedTime = simpleProductService.createProducts(recordCount);
            long batchElapsedTime = batchProductService.createProducts(recordCount);

            System.out.println(String.join("", Collections.nCopies(50, "-")));
            System.out.format("%-20s%-5s%-10s%-5s%8sms\n", "Regular inserts", "|", recordCount, "|", regularElapsedTime);
            System.out.format("%-20s%-5s%-10s%-5s%8sms\n", "Batch inserts", "|", recordCount, "|", batchElapsedTime);
            System.out.printf("Total gain: %d %s\n", calculateGainInPercent(regularElapsedTime, batchElapsedTime), "%");
        }

        }

        int calculateGainInPercent(long before, long after) {
        return (int) Math.floor(100D * (before - after) / before);
        }
    }
    ```

    这是我们的基准测试结果：

    ```txt
    --------------------------------------------------
    Regular inserts     |    1         |          14ms
    Batch inserts       |    1         |           8ms
    Total gain: 42 %
    --------------------------------------------------
    Regular inserts     |    10        |           4ms
    Batch inserts       |    10        |           1ms
    Total gain: 75 %
    --------------------------------------------------
    Regular inserts     |    100       |          29ms
    Batch inserts       |    100       |           6ms
    Total gain: 79 %
    --------------------------------------------------
    Regular inserts     |    1000      |         175ms
    Batch inserts       |    1000      |          24ms
    Total gain: 86 %
    --------------------------------------------------
    Regular inserts     |    10000     |         861ms
    Batch inserts       |    10000     |         128ms
    Total gain: 85 %
    --------------------------------------------------
    Regular inserts     |    100000    |        5098ms
    Batch inserts       |    100000    |        1126ms
    Total gain: 77 %
    --------------------------------------------------
    Regular inserts     |    1000000   |       47738ms
    Batch inserts       |    1000000   |       13066ms
    Total gain: 72 %
    --------------------------------------------------
    ```

    结果看起来很有希望。

    然而，这还不是全部。Postgres、MySQL和SQL Server等一些数据库支持多值插入。它有助于减少插入语句的总体大小。让我们看看这总体上是如何运作的：

    ```sql
    -- REGULAR INSERTS TO INSERT 4 RECORDS
    INSERT INTO PRODUCT
    (TITLE, CREATED_TS, PRICE)
    VALUES
    ('test1', LOCALTIMESTAMP, 100.10);

    INSERT INTO PRODUCT
    (TITLE, CREATED_TS, PRICE)
    VALUES
    ('test2', LOCALTIMESTAMP, 101.10);
    
    INSERT INTO PRODUCT
    (TITLE, CREATED_TS, PRICE)
    VALUES
    ('test3', LOCALTIMESTAMP, 102.10);

    INSERT INTO PRODUCT
    (TITLE, CREATED_TS, PRICE)
    VALUES
    ('test4', LOCALTIMESTAMP, 103.10);

    -- EQUIVALENT MULTI-VALUE INSERT
    INSERT INTO PRODUCT
    (TITLE, CREATED_TS, PRICE)
    VALUES
    ('test1', LOCALTIMESTAMP, 100.10),
    ('test2', LOCALTIMESTAMP, 101.10),
    ('test3', LOCALTIMESTAMP, 102.10),
    ('test4', LOCALTIMESTAMP, 104.10);
    ```

    要在Postgres数据库中利用此功能，在我们的application.properties文件中设置spring.datasource.hikari.data-source-properties.reWriteBatchedInserts=true就足够了。基础的JDBC驱动程序开始将我们的常规插入语句重写为批量插入的多值语句。

    此配置特定于Postgres。其他支持数据库可能有不同的配置要求。

    让我们在启用此功能的情况下重新运行我们的应用程序，看看区别：

    ```txt
    --------------------------------------------------
    Regular inserts     |    1         |          15ms
    Batch inserts       |    1         |          10ms
    Total gain: 33 %
    --------------------------------------------------
    Regular inserts     |    10        |           3ms
    Batch inserts       |    10        |           2ms
    Total gain: 33 %
    --------------------------------------------------
    Regular inserts     |    100       |          42ms
    Batch inserts       |    100       |          10ms
    Total gain: 76 %
    --------------------------------------------------
    Regular inserts     |    1000      |         141ms
    Batch inserts       |    1000      |          19ms
    Total gain: 86 %
    --------------------------------------------------
    Regular inserts     |    10000     |         827ms
    Batch inserts       |    10000     |         104ms
    Total gain: 87 %
    --------------------------------------------------
    Regular inserts     |    100000    |        5093ms
    Batch inserts       |    100000    |         981ms
    Total gain: 80 %
    --------------------------------------------------
    Regular inserts     |    1000000   |       50482ms
    Batch inserts       |    1000000   |        9821ms
    Total gain: 80 %
    --------------------------------------------------
    ```

    我们可以看到，当我们拥有相对较大的数据集时，启用此功能可以提高整体性能。

5. 结论

    在本文中，我们创建了一个简单的示例，以展示我们如何从Spring JDBC批量支持插入物中受益。我们将常规插入物与批量插入物进行了比较，获得了大约80-90%的性能收益。当然，在使用批处理功能时，我们还需要考虑对JDBC驱动程序的支持及其效率。

    此外，我们了解到，一些数据库/驱动程序提供了多值插入功能，以进一步提高性能，我们看到了如何在Postgres的情况下使用它。
