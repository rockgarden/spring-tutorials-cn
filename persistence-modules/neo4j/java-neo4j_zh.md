# [使用 Java 的 Neo4J 指南](https://www.baeldung.com/java-neo4j)

1. 简介

    本文介绍的 [Neo4j](https://neo4j.com/) 是目前市场上最成熟、功能最齐全的图形数据库之一。图数据库在处理数据建模任务时，认为生活中的许多事物都可以用节点（V）和它们之间的连接（E）的集合来表示。

2. 嵌入式 Neo4j

    开始使用 Neo4j 的最简单方法是使用嵌入式版本，其中 Neo4j 与应用程序运行在同一个 JVM 中。

    首先，我们需要添加一个 Maven 依赖：

    ```xml
    <dependency>
        <groupId>org.neo4j</groupId>
        <artifactId>neo4j</artifactId>
        <version>5.8.0</version>
    </dependency>
    ```

    接下来，让我们创建一个 DatabaseManagementService：

    ```java
    DatabaseManagementService managementService = new 
    DatabaseManagementServiceBuilder(new File("data/cars").toPath())
    .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds( 60 ) )
    .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true ).build();
    ```

    这包括一个嵌入式数据库。最后，我们创建一个 GraphDatabaseService：

    `GraphDatabaseService graphDb = managementService.database( DEFAULT_DATABASE_NAME );`

    现在可以开始真正的操作了！首先，我们需要在图中创建一些节点，为此，我们需要启动一个事务，因为除非启动了事务，否则 Neo4j 将拒绝任何破坏性操作：

    `Transaction transaction = graphDb.beginTx();`

    我们执行的每个操作（如 createNode/execute）都应在已创建事务的上下文中运行，并使用该对象。

    一旦事务开始执行，我们就可以开始添加节点了：

    ```java
    Node car = transaction.createNode(Label.label("Car"));
    car.setProperty("make", "tesla");
    car.setProperty("model", "model3");

    Node owner = transaction.createNode(Label.label("Person"));
    owner.setProperty("firstName", "baeldung");
    owner.setProperty("lastName", "baeldung");
    ```

    在这里，我们添加了一个带有 make 和 model 属性的节点 Car 以及带有 firstName 和 lastName 属性的节点 Person。

    现在我们可以添加关系了：

    `owner.createRelationshipTo(car, RelationshipType.withName("owner"));`

    上面的语句添加了一条连接两个带有车主标签的节点的边。我们可以通过运行一个用 Neo4j 的 Cypher 语言编写的查询来验证这种关系：

    ```java
    Result result = transaction.execute(
    "MATCH (c:Car) <-[owner]- (p:Person) " +
    "WHERE c.make = 'tesla'" +
    "RETURN p.firstName, p.lastName");
    ```

    在这里，我们要求找到所有型号为特斯拉的汽车的车主，并返回他/她的名字和姓氏。不出所料，返回结果是 `{p.firstName=baeldung, p.lastName=baeldung}`。

3. Cypher 查询语言

    Neo4j 提供了一种功能强大且相当直观的查询语言，它支持数据库的所有功能。让我们来看看如何完成标准的创建、检索、更新和删除任务。

    1. 创建节点

        创建关键字可用于创建节点和关系。

        ```sql
        CREATE (self:Company {name:"Baeldung"})
        RETURN self
        ```

        在这里，我们创建了一个只有一个属性名称的公司。节点定义用括号标出，其属性用大括号括起来。在本例中，self 是节点的别名，Company 是节点标签。

    2. 创建关系

        可以在一次查询中创建一个节点以及与该节点的关系：

        ```java
        Result result = transaction.execute(
        "CREATE (baeldung:Company {name:\"Baeldung\"}) " +
        "-[:owns]-> (tesla:Car {make: 'tesla', model: 'modelX'})" +
        "RETURN baeldung, tesla");
        ```

        在这里，我们创建了 baeldung 和 tesla 节点，并在它们之间建立了所有权关系。当然，我们也可以创建与已有节点的关系。

    3. 检索数据

        MATCH 关键字用于查找数据，与 RETURN 结合使用可控制返回哪些数据点。可以利用 WHERE 子句只过滤出那些具有我们所需的属性的节点。

        让我们找出拥有特斯拉 ModelX 的公司名称：

        ```java
        Result result = transaction.execute(
        "MATCH (company:Company)-[:owns]-> (car:Car)" +
        "WHERE car.make='tesla' and car.model='modelX'" +
        "RETURN company.name");
        ```

    4. 更新节点

        SET 关键字可用于更新节点属性或标签。让我们为特斯拉添加里程数：

        ```java
        Result result = transaction.execute("MATCH (car:Car)" +
        "WHERE car.make='tesla'" +
        " SET car.milage=120" +
        " SET car :Car:Electro" +
        " SET car.model=NULL" +
        " RETURN car");
        ```

        在这里，我们添加了一个名为 milage 的新属性，修改标签为 Car 和 Electro，最后删除了所有的 model 属性。

    5. 删除节点

        DELETE 关键字可用于从图中永久删除节点或关系：

        ```java
        transaction.execute("MATCH (company:Company)" +
        " WHERE company.name='Baeldung'" +
        " DELETE company");
        ```

        在这里，我们删除了一家名为 Baeldung 的公司。

    6. 参数绑定

        在上述示例中，我们硬编码了参数值，这并不是最佳做法。幸运的是，Neo4j 提供了将变量绑定到查询的工具：

        ```java
        Map<String, Object> params = new HashMap<>();
        params.put("name", "baeldung");
        params.put("make", "tesla");
        params.put("model", "modelS");

        Result result = transaction.execute("CREATE (baeldung:Company {name:$name}) " +
        "-[:owns]-> (tesla:Car {make: $make, model: $model})" +
        "RETURN baeldung, tesla", params);
        ```

4. Java 驱动程序

    到目前为止，我们一直在关注与嵌入式Neo4j实例的交互，然而，在生产中，我们很可能想要运行一个独立的服务器，并通过提供的驱动程序与之连接。首先，我们需要在maven pom.xml中添加另一个依赖项：

    ```xml
    <dependency>
        <groupId>org.neo4j.driver</groupId>
        <artifactId>neo4j-java-driver</artifactId>
        <version>5.6.0</version>
    </dependency>
    ```

    > 运行环境：openjdk-17-jdk

    为了模拟生产设置，我们将使用测试容器，在 docker 容器中启动 neo4j 服务器。为此，我们将使用 neo4j 测试容器的依赖关系。

    ```xml
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>neo4j</artifactId>
        <version>${testcontainers.version}</version>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.jetbrains</groupId>
                <artifactId>annotations</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.commons</groupId>
                <artifactId>commons-compress</artifactId>
            </exclusion>
            <exclusion>
                <groupId>javax.xml.bind</groupId>
                <artifactId>jaxb-api</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    ```

    现在，我们可以使用 neo4j 容器建立连接：

    ```java
    boolean containerReuseSupported = TestcontainersConfiguration.getInstance().environmentSupportsReuse();

    Neo4jContainer neo4jServer = new Neo4jContainer<>(imageName).withReuse(containerReuseSupported);
    Driver driver = GraphDatabase.driver(
    neo4jServer.getBoltUrl(), AuthTokens.basic("neo4j", "12345678"));
    ```

    > 注意：Neo4j > 5.20 码长度最少为 8 个字符，否则报错 `Invalid value for password. The minimum password length is 8 characters.`。

    然后，创建一个会话：

    `Session session = driver.session();`

    最后，我们可以运行一些查询：

    ```java
    session.run("CREATE (baeldung:Company {name:\"Baeldung\"}) " +
    "-[:owns]-> (tesla:Car {make: 'tesla', model: 'modelX'})" +
    "RETURN baeldung, tesla");
    ```

    完成所有工作后，我们需要关闭会话和驱动程序：

    ```java
    session.close();
    driver.close();
    ```

5. JDBC 驱动程序

    还可以通过 JDBC 驱动程序与 Neo4j 交互。这是我们的 pom.xml 的另一个依赖项：

    ```xml
    <dependency>
        <groupId>org.neo4j</groupId>
        <artifactId>neo4j-jdbc-driver</artifactId>
        <version>4.0.9</version>
    </dependency>
    ```

    您可以通过此链接下载该驱动程序的最新版本。

    接下来，让我们建立一个 JDBC 连接，该连接将连接到 neo4j 服务器的现有实例，该实例将在上一节介绍的测试容器中运行：

    ```java
    String uri = "jdbc:neo4j:" + neo4jServer.getBoltUrl() + "/?user=neo4j,password=" + DEFAULT_PASSWORD + ",scheme=basic";
    Connection con = DriverManager.getConnection(uri);
    ```

    这里的 con 是一个普通的 JDBC 连接，可用于创建和执行语句或准备语句：

    ```java
    {
    try (Statement stmt = con.
    stmt.execute("CREATE (baeldung:Company {name:\"Baeldung\"}) "
    + "-[:owns]-> (tesla:Car {make: 'tesla', model: 'modelX'})"
    + "RETURN baeldung, tesla")

        ResultSet rs = stmt.executeQuery(
        "MATCH (company:Company)-[:owns]-> (car:Car)" +
        "WHERE car.make='tesla' and car.model='modelX'" +
        "RETURN company.name");

        while (rs.next()) {
            rs.getString("company.name");
        }
    }
    ```

6. 对象图映射

    对象图映射（Object-Graph-Mapping 或 OGM）是一种技术，能让我们将域 POJO 作为 Neo4j 数据库中的实体使用。让我们来看看它是如何工作的。第一步，像往常一样，我们在 pom.xml 中添加新的依赖项：

    ```xml
    <dependency>
        <groupId>org.neo4j</groupId>
        <artifactId>neo4j-ogm-core</artifactId>
        <version>4.0.5</version>
    </dependency>

    <dependency> 
        <groupId>org.neo4j</groupId>
        <artifactId>neo4j-ogm-embedded-driver</artifactId>
        <version>3.2.39</version>
    </dependency>
    ```

    您可以检查 OGM Core Link 和 OGM Embedded Driver Link，以查看这些库的最新版本。

    其次，我们使用 OGM 注释(@NodeEntity)来注解 POJO：

    ![Company](/src/main/java/com/baeldung/neo4j/domain/Company.java)

    ![Car](/src/main/java/com/baeldung/neo4j/domain/Car.java)

    @NodeEntity 通知 Neo4j 在生成的图中需要用一个节点来表示这个对象。@Relationship 表示需要与代表相关类型的节点创建关系。在本例中，公司拥有一辆汽车。

    请注意，Neo4j 要求每个实体都有一个主键，默认使用名为 id 的字段。如果使用 @Id @GeneratedValue 作为注释，也可以使用其他名称的字段。

    然后，我们需要创建一个配置，用于引导 Neo4j 的 OGM。为此，我们将使用测试容器来模拟 Neo4j 服务器：

    ```java
    Configuration.Builder baseConfigurationBuilder = new Configuration.Builder()
    .uri(NEO4J_URL)
    .verifyConnection(true)
    .withCustomProperty(CONFIG_PARAMETER_BOLT_LOGGING, Logging.slf4j())
    .credentials("neo4j", Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_PASSWORD)).orElse("").trim());
    ```

    根据上述配置，我们将配置传递给会话工厂的驱动程序：

    ```java
    Driver driver = new org.neo4j.ogm.drivers.bolt.driver.BoltDriver();
    driver.configure(baseConfigurationBuilder.build());
    ```

    然后，我们使用创建的驱动程序和注释 POJO 所在的包名初始化 SessionFactory：

    `SessionFactory factory = new SessionFactory(getDriver(), "com.baeldung.neo4j.domain");`

    最后，我们可以创建一个会话并开始使用它：

    ```java
    Session session = factory.openSession();
    Car tesla = new Car("tesla", "modelS");
    Company baeldung = new Company("baeldung");

    baeldung.setCar(tesla);
    session.save(baeldung);
    ```

    在这里，我们启动了一个会话，创建了我们的 POJO，并要求 OGM 会话持久化它们。Neo4j OGM 运行时会透明地将对象转换为一组 Cypher 查询，从而在数据库中创建适当的节点和边。

    如果这个过程看起来很熟悉，那是因为它确实很熟悉！这正是 JPA 的工作方式，唯一的区别在于对象是被转换成持久化到 RDBMS 的行，还是持久化到图数据库的一系列节点和边。

7. 附：

    Docker run 命令：

    ```shell
    docker run \
    --publish=7474:7474 --publish=7687:7687 \
    --volume=$HOME/neo4j/data:/data \
    --volume=$HOME/neo4j/logs:/logs \
    --volume=$HOME/neo4j/conf:/conf \
    --volume=$HOME/neo4j/import:/import \
    --env NEO4J_AUTH=neo4j/12345678 \
    neo4j:5
    ```
