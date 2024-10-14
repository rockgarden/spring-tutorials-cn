# [使用嵌入式MongoDB进行Spring Boot集成测试](https://www.baeldung.com/spring-boot-embedded-mongodb)

1. 一览表

    在本教程中，我们将学习如何将Flapdoodle的嵌入式MongoDB解决方案与Spring Boot一起使用，以顺利运行MongoDB集成测试。

    MongoDB是一个流行的NoSQL文档数据库。由于高可扩展性、内置分片和出色的社区支持，它通常被许多开发人员视为“NoSQL存储”。

    与任何其他持久性技术一样，能够轻松地测试与我们应用程序其他部分的数据库集成至关重要。值得庆幸的是，Spring Boot允许我们轻松编写此类测试。

2. Maven附属机构

    首先，让我们为我们的Boot项目设置Maven父项。

    多亏了父级，我们不需要手动定义每个Maven依赖项的版本。

    我们自然会使用Spring Boot：

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <relativePath /> <!-- lookup parent from repository -->
    </parent>
    ```

    您可以在这里找到最新的Boot版本。

    由于我们添加了Spring Boot父项，我们可以在不指定其版本的情况下添加所需的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    ```

    spring-boot-starter-data-mongodb将启用Spring对MongoDB的支持：

    ```xml
    <dependency>
        <groupId>de.flapdoodle.embed</groupId>
        <artifactId>de.flapdoodle.embed.mongo</artifactId>
        <scope>test</scope>
    </dependency>
    ```

    de.flapdoodle.embed.mongo为集成测试提供嵌入式MongoDB。

3. 使用嵌入式MongoDB进行测试

    本节涵盖两种场景：Spring Boot测试和手动测试。

    1. Spring Boot测试

        添加de.flapdoodle.embed.mongo依赖项后，Spring Boot将在运行测试时自动尝试下载并启动嵌入式MongoDB。

        每个版本只会下载一次软件包，以便后续测试运行得更快。

        在这个阶段，我们应该能够启动并通过样本JUnit 5集成测试：

        ```java
        @DataMongoTest
        @ExtendWith(SpringExtension.class)
        public class MongoDbSpringIntegrationTest {
            @DisplayName("given object to save"
                + " when save object using MongoDB template"
                + " then object is saved")
            @Test
            public void test(@Autowired MongoTemplate mongoTemplate) {
                // given
                DBObject objectToSave = BasicDBObjectBuilder.start()
                    .add("key", "value")
                    .get();

                // when
                mongoTemplate.save(objectToSave, "collection");

                // then
                assertThat(mongoTemplate.findAll(DBObject.class, "collection")).extracting("key")
                    .containsOnly("value");
            }
        }
        ```

        正如我们所看到的，嵌入式数据库由Spring自动启动，它也应该记录在控制台中：

        `...Starting MongodbExampleApplicationTests on arroyo with PID 10413...`

    2. 手动配置测试

        Spring Boot将自动启动和配置嵌入式数据库，然后为我们注入MongoTemplate实例。然而，有时我们可能需要手动配置嵌入式Mongo数据库（例如，在测试特定DB版本时）。

        以下片段展示了我们如何手动配置嵌入式MongoDB实例。这大致相当于之前的春季测试：

        ```java
        class ManualEmbeddedMongoDbIntegrationTest {
            private static final String CONNECTION_STRING = "mongodb://%s:%d";

            private MongodExecutable mongodExecutable;
            private MongoTemplate mongoTemplate;

            @AfterEach
            void clean() {
                mongodExecutable.stop();
            }

            @BeforeEach
            void setup() throws Exception {
                String ip = "localhost";
                int port = 27017;

                ImmutableMongodConfig mongodConfig = MongodConfig
                    .builder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(ip, port, Network.localhostIsIPv6()))
                    .build();

                MongodStarter starter = MongodStarter.getDefaultInstance();
                mongodExecutable = starter.prepare(mongodConfig);
                mongodExecutable.start();
                mongoTemplate = new MongoTemplate(MongoClients.create(String.format(CONNECTION_STRING, ip, port)), "test");
            }

            @DisplayName("given object to save"
                + " when save object using MongoDB template"
                + " then object is saved")
            @Test
            void test() throws Exception {
                // given
                DBObject objectToSave = BasicDBObjectBuilder.start()
                    .add("key", "value")
                    .get();

                // when
                mongoTemplate.save(objectToSave, "collection");

                // then
                assertThat(mongoTemplate.findAll(DBObject.class, "collection")).extracting("key")
                    .containsOnly("value");
            }
        }
        ```

        请注意，我们可以快速创建MongoTemplate bean，配置为使用我们手动配置的嵌入式数据库，并将其注册在Spring容器中，例如，只需创建一个带有@Bean方法的@TestConfiguration，该方法将返回 `new MongoTemplate(MongoClients.create(connectionString, “test”))`。

        更多示例可以在官方的Flapdoodle的[GitHub存储库](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo)上找到。

    3. 记录

        在运行集成测试时，我们可以通过添加这两个属性tosrc/test/resources/application.properties文件来配置MongoDB的日志消息：

        ```properties
        logging.level.org.springframework.boot.autoconfigure.mongo.embedded=on
        logging.level.org.mongodb=on
        ```

        例如，要禁用日志记录，我们只需将值设置为关闭：

        ```properties
        logging.level.org.springframework.boot.autoconfigure.mongo.embedded=off
        logging.level.org.mongodb=off
        ```

    4. 在生产中使用真实数据库

        由于我们使用`<scope>test</scope>`添加了de.flapdoodle.embed.mongo依赖项，因此在生产中运行时无需禁用嵌入式数据库。我们所要做的就是指定MongoDB连接详细信息（例如，主机和端口），我们就可以走了。

        要在测试之外使用嵌入式数据库，我们可以使用Spring配置文件，该配置文件将根据活动配置文件注册正确的MongoClient（嵌入式或生产）。

        我们还需要将生产依赖范围更改为`<scope>runtime</scope>`。

4. 嵌入式测试争议

    一开始，使用嵌入式数据库可能看起来是个好主意。事实上，当我们想测试我们的应用程序在以下领域是否正确时，这是一个很好的方法：

    - 对象<->文档映射配置
    - 自定义持久生命周期事件侦听器（参考AbstractMongoEventListener）
    - 直接与持久性层工作的任何代码的逻辑

    不幸的是，使用嵌入式服务器不能被视为“full integration testing”。Flapdoodle的嵌入式MongoDB不是官方的MongoDB产品。因此，我们无法确定它的行为与生产环境中的完全相同。

    如果我们想在尽可能接近生产的环境中运行通信测试，更好的解决方案是使用Docker等环境容器。

5. 结论

    Spring Boot使运行验证正确文档映射和数据库集成的测试变得非常简单。通过添加正确的Maven依赖项，我们可以立即在Spring Boot集成测试中使用MongoDB组件。

    我们需要记住，嵌入式MongoDB服务器不能被视为“real”服务器的替代品。
