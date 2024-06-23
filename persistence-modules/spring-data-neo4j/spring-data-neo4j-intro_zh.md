# [Spring Data Neo4j 简介](https://www.baeldung.com/spring-data-neo4j-intro)

1. 概述

    Neo4j 是一种流行的图形数据库管理系统，旨在使用基于图形的模型来存储、管理和查询数据。我们将学习如何配置项目并使用 Spring Data Neo4j 的各种组件与 Neo4j 数据库交互。

    在本教程中，我们将学习如何使用 Spring Data Neo4j。

2. 项目设置

    让我们从设置项目开始。我们将创建一个 Spring Boot 应用程序，其中包含与数据库交互的实体和存储库。然后，我们将了解如何配置项目以连接到数据库并测试存储库。

    1. 依赖关系

        首先，让我们为项目添加所需的依赖项。我们将添加 Spring Boot Starter Data Neo4j 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-neo4j</artifactId>
            <version>2.7.14</version>
        </dependency>
        ```

    2. Neo4j 线束

        为了测试目的，我们将使用 Neo4j Harness：

        ```xml
        <dependency>
            <groupId>org.neo4j.test</groupId>
            <artifactId>neo4j-harness</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
        ```

        Neo4j Harness 是一个允许我们以嵌入模式运行 Neo4j 数据库的库。这对于在不安装 Neo4j 数据库的情况下进行内存测试非常有用。

    3. 配置Neo4J连接

        要连接到数据库，我们需要配置URI、用户名和密码。让我们在 application.properties 文件中配置这些属性：

        ![application.properties](/src/main/resources/application.properties)

        上述属性有助于使用用户名 neo4j 和密码 password 连接到本地 Neo4j 数据库。

        接下来，让我们配置数据库的方言。为此，我们需要创建一个 org.neo4j.ogm.config.Configuration 类型的 bean：

        ![Neo4jConfig](/src/main/java/com/baeldung/spring/data/neo4j/config/Neo4jConfig.java)

        在此，我们将方言设置为 NEO4J_5。这将引导 Spring Data Neo4J 使用 Neo4J 5 规范生成类型和查询。

3. 代码示例

    现在，我们已经完成了项目设置，让我们来看看如何使用 Spring Data Neo4j 与数据库交互。我们将了解 Spring Data Neo4j 的各个组件以及如何使用它们。

    1. Neo4J 实体

        让我们从创建将在存储库中使用的实体开始。

        首先，我们将创建一个 Book 实体：

        ![Book](/src/main/java/com/baeldung/spring/data/neo4j/domain/Book.java)

        让我们来看看上述实体中使用的注解：

        - @Node 将类标记为数据库中的一个节点。我们将节点的标签作为值传递给了注解。
        - 我们需要用 @Id 注解来标记节点的主键。
        - 如果实体中的字段名与数据库中的属性名不同，我们可以使用 @Property 注解来指定数据库中的属性名。
        - @Relationship 注解用于指定两个节点之间的关系。我们需要指定关系的类型和关系的方向。在本例中，我们指定关系为 WRITTEN_BY，方向为 OUTGOING。

        同样，让我们创建作者实体：

        ![Author](/src/main/java/com/baeldung/spring/data/neo4j/domain/Author.java)

        正如我们所见，Author 实体与 Book 实体类似。唯一不同的是，Author 实体有一个 Book 实体的列表，并且关系的方向是 INCOMING。

    2. Neo4J 存储库

        接下来，我们将创建与数据库交互的存储库，从BookRepository开始：

        ![BookRepository](/src/main/java/com/baeldung/spring/data/neo4j/repository/BookRepository.java)

        这些方法与 Spring Data JPA 储藏库中的方法类似。findOneByTitle() 方法将根据书名查找图书，而 findAllByYear() 方法将查找在给定年份出版的所有图书。

    3. 自定义查询

        我们还可以使用 @Query 注解来编写自定义查询。

        让我们在 AuthorRepository 中创建一个自定义查询，查找某作者在给定年份之后出版的所有书籍：

        ![AuthorRepository](/src/main/java/com/baeldung/spring/data/neo4j/repository/AuthorRepository.java)

        为了创建查询，我们使用 MATCH 关键字来匹配数据库中的节点。我们指定节点的标签以及它们之间的关系。最后，我们使用 WHERE 关键字指定查询的条件。

        要向查询传递参数，我们可以使用 $ 符号。如果查询中的参数名称与方法中的参数名称不同，我们可以使用 @Param 注解来指定参数名称。

4. 测试

    现在我们有了实体和资源库，让我们为资源库编写一些集成测试来测试它们。我们将使用 Neo4j Harness 依赖项，以嵌入模式运行 Neo4j 数据库进行测试。

    1. 配置Neo4j Harness

        配置Neo4j Harness可分为三个步骤：

        - 启动嵌入式数据库
        - 设置使用嵌入式数据库的配置
        - 运行测试后清理数据库

        让我们在一个测试类中设置嵌入式服务器：

        ![BookAndAuthorRepositoryIntegrationTest.java](/src/test/java/com/baeldung/spring/data/neo4j/BookAndAuthorRepositoryIntegrationTest.java)

        在上述类中，我们创建了一个静态 @BeforeAll 方法来启动嵌入式数据库。我们还创建了一个静态 @AfterAll 方法，用于在运行测试后停止数据库。

        让我们深入了解一下服务器初始化：

        - 我们使用 newInProcessBuilder() 方法获取一个生成器实例。
        - withDisabledServer() 属性会创建一个没有 HTTP 访问权限的服务器，因为我们不需要在服务器上启用外部流量。
        - 作为选项，我们可以使用 withFixture() 在创建服务器时运行初始 [Cypher](https://www.baeldung.com/java-neo4j#cypher-query-language) 查询。
        - 在这里，我们提供了在服务器启动时创建两本书和一个作者的查询。这有助于在需要时进行初始数据设置。

        接下来，我们创建一个 @DynamicPropertySource 方法来设置嵌入式数据库的属性。我们为数据库设置了 URI、用户名和密码。这将覆盖 application.properties 文件中提供的属性。

    2. 测试存储库

        接下来，我们在BookAndAuthorRepositoryIntegrationTest.java中添加测试方法来测试资源库。

        注解 @DataNeo4jTest 用于配置 Spring Data Neo4j 的测试类。它加载 Spring 配置并为 Spring Data Neo4j Bean 创建测试片段。

        接下来，我们有三个测试方法来测试 findOneByTitle()、findAllByYear() 和 findBooksAfterYear() 方法。

5. 结论

    在本文中，我们学习了如何使用 Spring Data Neo4j 与 Neo4j 数据库交互。我们看到了如何创建实体和存储库，以及如何编写自定义查询。最后，我们为存储库编写了一些集成测试。
