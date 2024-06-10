# [使用各种数据库配置的 Netflix Archaius](https://www.baeldung.com/netflix-archaius-database-configurations)

1. 概述

    Netflix Archaius 提供了连接多种数据源的库和功能。

    在本教程中，我们将学习如何获取配置：

    - 使用 JDBC API 连接数据库
    - 从存储在 DynamoDB 实例中的配置中获取配置
    - 将 Zookeeper 配置为动态分布式配置

2. 通过 JDBC 连接使用 Netflix Archaius

    正如我们在入门教程中所解释的，只要我们想让 Archaius 处理配置，就需要创建一个 Apache 的 AbstractConfiguration Bean。

    Spring Cloud Bridge 将自动捕获该 Bean，并将其添加到 Archaius 的复合配置栈中。

    1. 依赖关系

        使用 JDBC 连接数据库所需的所有功能都包含在核心库中，因此除了我们在入门教程中提到的依赖关系外，我们不需要任何额外的依赖关系：

        ```xml
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-netflix-archaius</artifactId>
            </dependency>
        </dependencies>

        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-netflix</artifactId>
                    <version>2.0.1.RELEASE</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
        ```

        我们可以检查 Maven Central 以验证我们使用的是最新版本的启动库。

    2. 如何创建配置 Bean

        在本例中，我们需要使用 JDBCConfigurationSource 实例创建 AbstractConfiguration Bean。

        为了说明如何从 JDBC 数据库获取值，我们必须指定

        - 一个 javax.sql.Datasource 对象
        - 一个 SQL 查询字符串，该字符串将检索至少两列配置键及其相应值
        - 分别表示属性键和值的两列

        让我们继续创建这个 Bean：

        ```java
        @Autowired
        DataSource dataSource;

        @Bean
        public AbstractConfiguration addApplicationPropertiesSource() {
            PolledConfigurationSource source =
            new JDBCConfigurationSource(dataSource,
                "select distinct key, value from properties",
                "key",
                "value");
            return new DynamicConfiguration(source, new FixedDelayPollingScheduler());
        }
        ```

    3. 试用

        为了保持简单，同时又有一个可操作的示例，我们将使用一些初始数据建立一个 H2 内存数据库实例。

        为此，我们将首先添加必要的依赖关系：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>2.0.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.197</version>
            <scope>runtime</scope>
        </dependency>
        ```

        注：我们可以在 Maven Central 中查看 h2 和 spring-boot-starter-data-jpa 库的最新版本。

        接下来，我们将声明包含属性的 JPA 实体：

        ```java
        @Entity
        public class Properties {
            @Id
            private String key;
            private String value;
        }
        ```

        我们将在资源中包含一个 data.sql 文件，为内存数据库填充一些初始值：

        ```sql
        insert into properties
        values('baeldung.archaius.properties.one', 'one FROM:jdbc_source');
        ```

        最后，要检查任意给定点的属性值，我们可以创建一个端点来检索 Archaius 管理的值：

        ```java
        @RestController
        public class ConfigPropertiesController {

            private DynamicStringProperty propertyOneWithDynamic = DynamicPropertyFactory
            .getInstance()
            .getStringProperty("baeldung.archaius.properties.one", "not found!");

            @GetMapping("/properties-from-dynamic")
            public Map<String, String> getPropertiesFromDynamic() {
                Map<String, String> properties = new HashMap<>();
                properties.put(propertyOneWithDynamic.getName(), propertyOneWithDynamic.get());
                return properties;
            }
        }
        ```

        如果数据在任何时候发生变化，Archaius 将在运行时检测到并开始检索新值。

        当然，在接下来的示例中也可以使用这个端点。

3. 如何使用 DynamoDB 实例创建配置源

    与上一节一样，我们将创建一个功能齐全的项目，以正确分析 Archaius 如何使用 DynamoDB 实例作为配置源来管理属性。

    1. 依赖关系

        让我们在 pom.xml 文件中添加以下库：

        ```xml
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-java-sdk-dynamodb</artifactId>
            <version>1.11.414</version>
        </dependency>
        <dependency>
            <groupId>com.github.derjust</groupId>
            <artifactId>spring-data-dynamodb</artifactId>
            <version>5.0.3</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.archaius</groupId>
            <artifactId>archaius-aws</artifactId>
            <version>0.7.6</version>
        </dependency>
        ```

        我们可以查看 Maven Central 以获取最新的依赖版本，但对于 Archaius-aws 而言，我们建议使用 [Spring Cloud Netflix](https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-dependencies/pom.xml) 库支持的版本。

        aws-java-sdk-dynamodb 依赖项允许我们设置 DynamoDB 客户端以连接数据库。

        使用 spring-data-dynamodb 库，我们将设置 DynamoDB 存储库。

        最后，我们将使用 archaius-aws 库创建 AbstractConfiguration。

    2. 使用 DynamoDB 作为配置源

        这次，我们将使用 DynamoDbConfigurationSource 对象创建 AbstractConfiguration：

        ```java
        @Autowired
        AmazonDynamoDB amazonDynamoDb;

        @Bean
        public AbstractConfiguration addApplicationPropertiesSource() {
            PolledConfigurationSource source = new DynamoDbConfigurationSource(amazonDynamoDb);
            return new DynamicConfiguration(
            source, new FixedDelayPollingScheduler());
        }
        ```

        默认情况下，Archaius 会在 Dynamo 数据库中搜索一个名为 “archaiusProperties” 的表，其中包含一个 “key” 和一个 “value” 属性，并将其用作源。

        如果我们想覆盖这些值，就必须声明以下系统属性：

        - com.netflix.config.dynamo.tableName
        - com.netflix.config.dynamo.keyAttributeName
        - com.netflix.config.dynamo.valueAttributeName

    3. 创建功能完善的示例

        与[本 DynamoDB 指南](https://www.baeldung.com/spring-data-dynamodb#DynamoDB)中一样，我们将首先安装一个本地 DynamoDB 实例，以便轻松测试功能。

        我们还将按照指南中的说明创建之前 “autowired” 的 AmazonDynamoDB 实例。

        为了用一些初始数据填充数据库，我们将首先创建一个 DynamoDBTable 实体来映射数据：

        ```java
        @DynamoDBTable(tableName = "archaiusProperties")
        public class ArchaiusProperties {

            @DynamoDBHashKey
            @DynamoDBAttribute
            private String key;

            @DynamoDBAttribute
            private String value;

            // ...getters and setters...
        }
        ```

        接下来，我们将为该实体创建一个 CrudRepository：

        `public interface ArchaiusPropertiesRepository extends CrudRepository<ArchaiusProperties, String> {}`

        最后，我们将使用该存储库和 AmazonDynamoDB 实例创建表格并插入数据：

        ```java
        @Autowired
        private ArchaiusPropertiesRepository repository;

        @Autowired
        AmazonDynamoDB amazonDynamoDb;

        private void initDatabase() {
            DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDb);
            CreateTableRequest tableRequest = mapper
            .generateCreateTableRequest(ArchaiusProperties.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            TableUtils.createTableIfNotExists(amazonDynamoDb, tableRequest);

            ArchaiusProperties property = new ArchaiusProperties("baeldung.archaius.properties.one", "one FROM:dynamoDB");
            repository.save(property);
        }
        ```

        我们可以在创建 DynamoDbConfigurationSource 之前调用此方法。

        现在我们已经准备好运行应用程序了。

4. 如何设置动态 Zookeeper 分布式配置

    正如我们之前在[介绍 Zookeeper 的文章](https://www.baeldung.com/java-zookeeper)中所看到的，该工具的优点之一是可以用作分布式配置存储。

    如果将它与 Archaius 结合使用，我们就能获得一个灵活、可扩展的配置管理解决方案。

    1. 依赖关系

        让我们按照 [Spring Cloud 的官方说明](http://cloud.spring.io/spring-cloud-zookeeper/single/spring-cloud-zookeeper.html#spring-cloud-zookeeper-install)来设置 Apache Zookeeper 的更稳定版本。

        唯一不同的是，我们只需要 Zookeeper 提供的部分功能，因此我们可以使用 spring-cloud-starter-zookeeper-config 依赖项，而不是官方指南中使用的依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-config</artifactId>
            <version>2.0.0.RELEASE</version>
            <exclusions>
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.13</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-log4j12</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        ```

        同样，我们可以在 Maven Central 中检查 spring-cloud-starter-zookeeper-config 和 zookeeper 依赖项的最新版本。

        请确保避免使用 zookeeper 测试版。

    2. Spring Cloud 的自动配置

        正如官方文档中解释的那样，包含 spring-cloud-starter-zookeeper-config 依赖项就足以设置 Zookeeper 属性源。

        默认情况下，只有一个源是自动配置的，即搜索 config/application Zookeeper 节点下的属性。因此，该节点被用作不同应用程序之间的共享配置源。

        此外，如果我们使用 spring.application.name 属性指定应用程序名称，则会自动配置另一个源，这次是搜索 `config/<app_name>` 节点中的属性。

        这些父节点下的每个节点名称将表示一个属性键，其数据将是属性值。

        幸运的是，由于 Spring Cloud 将这些属性源添加到上下文中，Archaius 会自动管理它们。无需以编程方式创建 AbstractConfiguration。

    3. 准备初始数据

        在这种情况下，我们还需要一个本地 Zookeeper 服务器来将配置存储为节点。我们可以按照 [Apache 的指南](https://zookeeper.apache.org/doc/r3.1.2/zookeeperStarted.html#sc_InstallingSingleMode)设置一个运行在 2181 端口的独立服务器。

        要连接到 Zookeeper 服务并创建一些初始数据，我们将使用 Apache 的 Curator 客户端：

        ```java
        @Component
        public class ZookeeperConfigsInitializer {

            @Autowired
            CuratorFramework client;

            @EventListener
            public void appReady(ApplicationReadyEvent event) throws Exception {
                createBaseNodes();
                if (client.checkExists().forPath("/config/application/baeldung.archaius.properties.one") == null) {
                    client.create()
                    .forPath("/config/application/baeldung.archaius.properties.one",
                    "one FROM:zookeeper".getBytes());
                } else {
                    client.setData()
                    .forPath("/config/application/baeldung.archaius.properties.one",
                    "one FROM:zookeeper".getBytes());
                }
            }

            private void createBaseNodes() throws Exception {
                if (client.checkExists().forPath("/config") == null) {
                    client.create().forPath("/config");
                }
                if (client.checkExists().forPath("/config/application") == null) {
                    client.create().forPath("/config/application");
                }
            }
        }
        ```

        我们可以检查日志，查看属性来源，以验证 Netflix Archaius 是否在属性发生变化时刷新了属性。

5. 结论

    在本文中，我们了解了如何使用 Netflix Archaius 设置高级配置源。我们必须考虑到它还支持其他源，如 Etcd、Typesafe、AWS S3 文件和 JClouds。
