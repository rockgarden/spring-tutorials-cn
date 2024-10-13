# [使用Spring Data Cassandra记录查询](https://www.baeldung.com/spring-data-cassandra-logging-queries)

1. 一览表

    Apache Cassandra是一个可扩展的分布式NoSQL数据库。Cassandra在节点之间流式传输数据，并提供无单点故障的持续可用性。事实上，Cassandra能够以卓越的性能处理大量数据

    在开发使用数据库的应用程序时，能够记录和调试已执行的查询非常重要。在本教程中，我们将研究如何在使用Apache Cassandra和Spring Boot时记录查询和语句。

    在我们的示例中，我们将使用Spring Data存储库抽象和Testcontainers库。我们将了解如何通过Spring配置配置Cassandra查询日志。此外，我们将探索Datastax请求记录器。我们可以配置此内置组件以进行更高级的日志记录。

2. 设置测试环境

    为了演示查询日志记录，我们需要设置一个测试环境。首先，我们将使用Apache Cassandra的Spring Data设置测试数据。接下来，我们将使用Testcontainers库来运行Cassandra数据库容器进行集成测试。

    1. 卡桑德拉存储库

        Spring Data使我们能够基于常见的Spring接口创建Cassandra存储库。首先，让我们从定义一个简单的DAO类开始：

        ```java
        @Table
        public class Person {

            @PrimaryKey
            private UUID id;
            private String firstName;
            private String lastName;

            public Person(UUID id, String firstName, String lastName) {
                this.id = id;
                this.firstName = firstName;
                this.lastName = lastName;
            }

            // getters, setters, equals and hash code
        }
        ```

        然后，我们将通过扩展CassandraRepository接口，为我们的DAO定义一个Spring数据存储库：

        ```java
        @Repository
        public interface PersonRepository extends CassandraRepository<Person, UUID> {}
        ```

        最后，我们将在application.properties文件中添加两个属性：

        ```properties
        spring.data.cassandra.schema-action=create_if_not_exists
        spring.data.cassandra.local-datacenter=datacenter1
        ```

        因此，Spring Data将自动为我们创建注释表。

        我们应该注意，不建议生产系统使用create_if_not_exists选项。

        作为替代方案，可以通过从标准根类路径加载[schema.sql](https://www.baeldung.com/spring-boot-data-sql-and-schema-sql)脚本来创建表。

    2. 卡桑德拉容器

        作为下一步，让我们在特定端口上配置和公开Cassandra容器：

        @Container
        public static final CassandraContainer cassandra =
        (CassandraContainer) new CassandraContainer("cassandra:3.11.2").withExposedPorts(9042);
        拷贝
        在使用容器进行集成测试之前，我们需要[覆盖](https://www.baeldung.com/spring-tests-override-properties)Spring Data所需的[测试属性](https://www.baeldung.com/spring-tests-override-properties)，以便与它建立连接：

        ```java
        TestPropertyValues.of(
        "spring.data.cassandra.keyspace-name=" + KEYSPACE_NAME,
        "spring.data.cassandra.contact-points=" + cassandra.getContainerIpAddress(),
        "spring.data.cassandra.port=" + cassandra.getMappedPort(9042)
        ).applyTo(configurableApplicationContext.getEnvironment());

        createKeyspace(cassandra.getCluster());
        ```

        最后，在创建任何objects/tables之前，我们需要创建一个Cassandra键空间。密钥空间类似于RDBMS中的数据库。

    3. 集成测试

        现在，我们已经有了一切可以开始编写集成测试。

        我们有兴趣记录选择、插入和删除查询。因此，我们将编写几个测试，这些测试将触发这些不同类型的查询。

        首先，我们将编写一个保存和更新个人记录的测试。我们期望此测试执行两个插入和一个选择数据库查询：

        ```java
        @Test
        void givenExistingPersonRecord_whenUpdatingIt_thenRecordIsUpdated() {
            UUID personId = UUIDs.timeBased();
            Person existingPerson = new Person(personId, "Luka", "Modric");
            personRepository.save(existingPerson);
            existingPerson.setFirstName("Marko");
            personRepository.save(existingPerson);
            List<Person> savedPersons = personRepository.findAllById(List.of(personId));
            assertThat(savedPersons.get(0).getFirstName()).isEqualTo("Marko");
        }
        ```

        然后，我们将编写一个保存和删除现有人员记录的测试。我们期望此测试执行一个插入、删除和选择数据库查询：

        ```java
        @Test
        void givenExistingPersonRecord_whenDeletingIt_thenRecordIsDeleted() {
            UUID personId = UUIDs.timeBased();
            Person existingPerson = new Person(personId, "Luka", "Modric");
            personRepository.delete(existingPerson);
            List<Person> savedPersons = personRepository.findAllById(List.of(personId));
            assertThat(savedPersons.isEmpty()).isTrue();
        }
        ```

        默认情况下，我们不会观察控制台中记录的任何数据库查询。

3. Spring Data CQL日志

    使用Apache Cassandra 2.0或更高版本的Spring Data，可以在application.properties中设置CqlTemplate类的日志级别：

    `logging.level.org.springframework.data.cassandra.core.cql.CqlTemplate=DEBUG`

    因此，通过将日志级别设置为DEBUG，我们启用了所有已执行的查询和准备语句的日志记录：

    ```log
    2021-09-25 12:41:58.679 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Executing CQL statement [CREATE TABLE IF NOT EXISTS person
    (birthdate date, firstname text, id uuid, lastname text, lastpurchaseddate timestamp, lastvisiteddate timestamp, PRIMARY KEY (id));]
    2021-09-25 12:42:01.204 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Preparing statement [INSERT INTO person (birthdate,firstname,id,lastname,lastpurchaseddate,lastvisiteddate)
    VALUES (?,?,?,?,?,?)] using org.springframework.data.cassandra.core.CassandraTemplate$PreparedStatementHandler@4d16975b
    2021-09-25 12:42:01.253 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Executing prepared statement [INSERT INTO person (birthdate,firstname,id,lastname,lastpurchaseddate,lastvisiteddate) VALUES (?,?,?,?,?,?)]
    2021-09-25 12:42:01.279 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Preparing statement [INSERT INTO person (birthdate,firstname,id,lastname,lastpurchaseddate,lastvisiteddate)
    VALUES (?,?,?,?,?,?)] using org.springframework.data.cassandra.core.CassandraTemplate$PreparedStatementHandler@539dd2d0
    2021-09-25 12:42:01.290 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Executing prepared statement [INSERT INTO person (birthdate,firstname,id,lastname,lastpurchaseddate,lastvisiteddate) VALUES (?,?,?,?,?,?)]
    2021-09-25 12:42:01.351 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Preparing statement [SELECT * FROM person WHERE id IN (371bb4a0-1ded-11ec-8cad-934f1aec79e6)]
    using org.springframework.data.cassandra.core.CassandraTemplate$PreparedStatementHandler@3e61cffd
    2021-09-25 12:42:01.370 DEBUG 17856 --- [           main] o.s.data.cassandra.core.cql.CqlTemplate:
    Executing prepared statement [SELECT * FROM person WHERE id IN (371bb4a0-1ded-11ec-8cad-934f1aec79e6)]
    ```

    不幸的是，使用此解决方案，我们不会看到语句中使用的绑定值的输出。

4. Datastax请求跟踪器

    DataStax请求跟踪器是一个会话范围的组件，可以收到每个Cassandra请求的结果的通知。

    Apache Cassandra的DataStax Java驱动程序附带一个可选的请求跟踪器实现，可以记录所有请求。

    1. Noop请求跟踪器

        默认请求跟踪器实现称为NoopRequestTracker。因此，它什么也做不了：

        `System.setProperty("datastax-java-driver.advanced.request-tracker.class", "NoopRequestTracker");`

        要设置不同的跟踪器，我们应该指定一个在Cassandra配置中或通过系统属性实现RequestTracker的类。

    2. 请求记录器

        RequestLogger是RequestTracker的内置实现，记录每个请求。

        我们可以通过设置特定的DataStax Java驱动程序系统属性来启用它：

        ```java
        System.setProperty("datastax-java-driver.advanced.request-tracker.class", "RequestLogger");
        System.setProperty("datastax-java-driver.advanced.request-tracker.logs.success.enabled", "true");
        System.setProperty("datastax-java-driver.advanced.request-tracker.logs.slow.enabled", "true");
        System.setProperty("datastax-java-driver.advanced.request-tracker.logs.error.enabled", "true");
        ```

        在本例中，我们启用了所有成功、缓慢和失败的请求的日志记录。

        现在，当我们运行测试时，我们将观察日志中所有执行的数据库查询：

        ```log
        2021-09-25 13:06:31.799  INFO 11172 --- [        s0-io-4] c.d.o.d.i.core.tracker.RequestLogger:
        [s0|90232530][Node(endPoint=localhost/[0:0:0:0:0:0:0:1]:49281, hostId=c50413d5-03b6-4037-9c46-29f0c0da595a, hashCode=68c305fe)]
        Success (6 ms) [6 values] INSERT INTO person (birthdate,firstname,id,lastname,lastpurchaseddate,lastvisiteddate)
        VALUES (?,?,?,?,?,?) [birthdate=NULL, firstname='Luka', id=a3ad6890-1df0-11ec-a295-7d319da1858a, lastname='Modric', lastpurchaseddate=NULL, lastvisiteddate=NULL]
        2021-09-25 13:06:31.811  INFO 11172 --- [        s0-io-4] c.d.o.d.i.core.tracker.RequestLogger:
        [s0|778232359][Node(endPoint=localhost/[0:0:0:0:0:0:0:1]:49281, hostId=c50413d5-03b6-4037-9c46-29f0c0da595a, hashCode=68c305fe)]
        Success (4 ms) [6 values] INSERT INTO person (birthdate,firstname,id,lastname,lastpurchaseddate,lastvisiteddate)
        VALUES (?,?,?,?,?,?) [birthdate=NULL, firstname='Marko', id=a3ad6890-1df0-11ec-a295-7d319da1858a, lastname='Modric', lastpurchaseddate=NULL, lastvisiteddate=NULL]
        2021-09-25 13:06:31.847  INFO 11172 --- [        s0-io-4] c.d.o.d.i.core.tracker.RequestLogger:
        [s0|1947131919][Node(endPoint=localhost/[0:0:0:0:0:0:0:1]:49281, hostId=c50413d5-03b6-4037-9c46-29f0c0da595a, hashCode=68c305fe)]
        Success (5 ms) [0 values] SELECT * FROM person WHERE id IN (a3ad6890-1df0-11ec-a295-7d319da1858a)
        ```

        我们将看到所有请求都记录在com.datastax.oss.driver.internal.core.tracker.RequestLogger类别下。

        此外，语句中使用的所有绑定值也会默认记录。

    3. 约束值

        内置的RequestLogger是一个高度可定制的组件。我们可以使用以下系统属性配置绑定值的输出：

        ```java
        System.setProperty("datastax-java-driver.advanced.request-tracker.logs.show-values", "true");
        System.setProperty("datastax-java-driver.advanced.request-tracker.logs.max-value-length", "100");
        System.setProperty("datastax-java-driver.advanced.request-tracker.logs.max-values", "100");
        ```

        如果值的格式化表示比最大值长度属性定义的值长，则值的格式化表示将被截断。

        使用最大值属性，我们可以定义要记录的绑定值的最大数量。

    4. 附加选项

        在我们的第一个例子中，我们启用了慢速请求的日志记录。我们可以使用阈值属性将成功请求归类为缓慢：

        `System.setProperty("datastax-java-driver.advanced.request-tracker.logs.slow.threshold ", "1 second");`

        默认情况下，所有失败的请求都会记录堆栈跟踪。如果我们禁用它们，我们将在日志中只看到异常的字符串表示：

        `System.setProperty("datastax-java-driver.advanced.request-tracker.logs.show-stack-trace", "true");`

        成功和缓慢的请求使用INFO日志级别。另一方面，失败的请求使用ERROR级别。

5. 结论

    在本文中，我们探讨了将Apache Cassandra与Spring Boot一起使用时的查询和语句记录。

    在示例中，我们涵盖了Apache Cassandra在Spring Data中配置日志级别。我们看到Spring Data会记录查询，但不会记录绑定值。最后，我们探索了Datastax请求跟踪器。这是一个高度可定制的组件，我们可以用来记录Cassandra查询及其绑定值。

    与往常一样，源代码可在 [GitHub](https://github.com/Baeldung/datastax-cassandra/tree/main/spring-cassandra) 上获取。
