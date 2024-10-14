# [Spring Data MongoDB – 配置连接](https://www.baeldung.com/spring-data-mongodb-connection)

1. 一览表

    在本教程中，我们将学习在Spring Boot应用程序中配置MongoDB连接的不同方法。我们将使用Spring Data MongoDB项目提供的强大功能。通过利用Spring Data MongoDB项目，我们可以访问一组丰富的工具和功能，这些工具和功能简化了在Spring环境中使用MongoDB数据库的过程。

    通过深入研究Spring的灵活配置选项，我们将探索建立数据库连接的各种方法。通过实践示例，我们将为每种方法创建单独的应用程序，使我们能够根据我们的具体要求选择最合适的配置方法。

2. 测试我们的连接

    在我们开始构建应用程序之前，我们将创建一个测试类。让我们从几个我们将重复使用的常数开始：

    ```java
    public class MongoConnectionApplicationLiveTest {
        private static final String HOST = "localhost";
        private static final String PORT = "27017";
        private static final String DB = "baeldung";
        private static final String USER = "admin";
        private static final String PASS = "password";
        // test cases
    }
    ```

    我们的测试包括运行我们的应用程序，然后尝试在名为“项目”的集合中插入文档。插入文档后，我们应该从数据库中收到一个“_id”，我们将认为测试成功。现在让我们为此创建一个助手方法：

    ```java
    private void assertInsertSucceeds(ConfigurableApplicationContext context) {
        String name = "A";

        MongoTemplate mongo = context.getBean(MongoTemplate.class);
        Document doc = Document.parse("{\"name\":\"" + name + "\"}");
        Document inserted = mongo.insert(doc, "items");

        assertNotNull(inserted.get("_id"));
        assertEquals(inserted.get("name"), name);
    }
    ```

    我们的方法从应用程序接收Spring上下文，以便我们可以检索MongoTemplate实例。接下来，我们将使用Document.parse（）从字符串构建一个简单的JSON文档。

    这样，我们不需要创建存储库或文档类。然后，插入后，我们将断言插入文档中的属性是我们预期的。

    需要注意的是，我们需要运行一个真正的MongoDB实例。为此，我们可以将MongoDB作为docker容器运行。

3. 通过属性配置连接

    为了在我们的Spring Boot应用程序中配置MongoDB连接，我们通常使用属性。在属性中，我们定义了基本的连接详细信息，如数据库主机、端口、身份验证凭据和数据库名称。我们将在以下小节中详细看到这些属性。

    1. 使用application.properties

        我们的第一个例子是配置连接的最常见方式。我们只需要在应用程序中提供我们的数据库信息。属性：

        ```properties
        spring.data.mongodb.host=localhost
        spring.data.mongodb.port=27017
        spring.data.mongodb.database=baeldung
        spring.data.mongodb.username=admin
        spring.data.mongodb.password=password
        ```

        所有可用的属性都位于Spring Boot的MongoProperties类中。我们还可以使用这个类来检查默认值。我们可以通过应用程序参数在属性文件中定义任何配置。

        在我们的应用程序类中，我们需要排除EmbeddedMongoAutoConfiguration类才能启动和运行：

        ```java
        @SpringBootApplication(exclude={EmbeddedMongoAutoConfiguration.class})
        public class SpringMongoConnectionViaPropertiesApp {
            public static void main(String... args) {
                SpringApplication.run(SpringMongoConnectionViaPropertiesApp.class, args);
            }
        }
        ```

        此配置是我们连接到数据库实例所需的全部配置。@SpringBootApplication注释包括@EnableAutoConfiguration。它负责发现我们的应用程序是基于我们的类路径的MongoDB应用程序。

        为了测试它，我们可以使用SpringApplicationBuilder来获取应用程序上下文的引用。然后，为了断言我们的连接是有效的，我们将使用之前创建的assertInsertSucceeds方法：

        ```java
        @Test
        public void whenPropertiesConfig_thenInsertSucceeds() {
            SpringApplicationBuilder app = new SpringApplicationBuilder(SpringMongoConnectionViaPropertiesApp.class);
            app.run();
            assertInsertSucceeds(app.context());
        }
        ```

        最后，我们的应用程序使用我们的application.properties文件成功连接。

    2. 使用命令行参数覆盖属性

        使用命令行参数运行应用程序时，我们可以覆盖属性文件。当使用java命令、mvn命令或IDE配置运行时，这些会传递给应用程序。提供这些的方法将取决于我们正在使用的命令。

        让我们看看使用mvn运行我们的Spring Boot应用程序的示例：

        `mvn spring-boot:run -Dspring-boot.run.arguments='--spring.data.mongodb.port=7017 --spring.data.mongodb.host=localhost'`

        要使用它，我们将我们的属性指定为spring-boot.run.arguments参数的值。我们使用相同的属性名称，但前缀为两个破折号。自Spring Boot 2以来，多个属性应用空格隔开。最后，运行命令后，应该不会有任何错误。

        以这种方式配置的选项总是优先于属性文件。当我们需要在不更改属性文件的情况下更改应用程序参数时，此选项很有用。例如，如果我们的凭据已经更改，我们无法再连接。

        为了在测试中模拟这一点，我们可以在运行应用程序之前设置系统属性。我们还可以使用属性方法覆盖我们的应用程序属性：

        ```java
        @Test
        public void givenPrecedence_whenSystemConfig_thenInsertSucceeds() {
            System.setProperty("spring.data.mongodb.host", HOST);
            System.setProperty("spring.data.mongodb.port", PORT);
            System.setProperty("spring.data.mongodb.database", DB);
            System.setProperty("spring.data.mongodb.username", USER);
            System.setProperty("spring.data.mongodb.password", PASS);

            SpringApplicationBuilder app = new SpringApplicationBuilder(SpringMongoConnectionViaPropertiesApp.class)
            .properties(
                "spring.data.mongodb.host=oldValue",
                "spring.data.mongodb.port=oldValue",
                "spring.data.mongodb.database=oldValue",
                "spring.data.mongodb.username=oldValue",
                "spring.data.mongodb.password=oldValue"
            );
            app.run();

            assertInsertSucceeds(app.context());
        }
        ```

        因此，我们属性文件中的旧值不会影响我们的应用程序，因为系统属性具有更优先权。当我们需要在不更改代码的情况下使用新的连接详细信息重新启动应用程序时，这非常有用。

    3. 使用连接URI属性

        也可以使用单个属性，而不是单个主机、端口等：

        `spring.data.mongodb.uri="mongodb://admin:password@localhost:27017/baeldung"`

        此属性包含初始属性中的所有值，因此我们不需要指定所有五个。让我们来检查一下基本格式：

        `mongodb://<username>:<password>@<host>:<port>/<database>`

        更具体地说，URI中的数据库部分是默认的[auth DB](https://www.mongodb.com/docs/master/reference/connection-string/#std-label-connections-standard-connection-string-format)。最重要的是，thespring.data.mongodb.uri属性不能与主机、端口和凭据的单个属性一起指定。否则，我们在运行应用程序时将收到以下错误：

        ```java
        @Test
        public void givenConnectionUri_whenAlsoIncludingIndividualParameters_thenInvalidConfig() {
            System.setProperty(
            "spring.data.mongodb.uri", 
            "mongodb://" + USER + ":" + PASS + "@" + HOST + ":" + PORT + "/" + DB
            );

            SpringApplicationBuilder app = new SpringApplicationBuilder(SpringMongoConnectionViaPropertiesApp.class)
            .properties(
                "spring.data.mongodb.host=" + HOST,
                "spring.data.mongodb.port=" + PORT,
                "spring.data.mongodb.username=" + USER,
                "spring.data.mongodb.password=" + PASS
            );

            BeanCreationException e = assertThrows(BeanCreationException.class, () -> {
                app.run();
            });

            Throwable rootCause = e.getRootCause();
            assertTrue(rootCause instanceof IllegalStateException);
            assertThat(rootCause.getMessage()
            .contains("Invalid mongo configuration, either uri or host/port/credentials/replicaSet must be specified"));
        }
        ```

        最后，这个配置选项不仅更短，而且有时是必需的。这是因为一些选项只能通过连接字符串使用，例如使用[mongodb+srv](https://www.mongodb.com/developer/products/mongodb/srv-connection-strings/)连接到副本集。因此，我们将仅将此更简单的配置属性用于以下示例。

4. 使用MongoClient设置Java

    MongoClient代表我们与MongoDB数据库的连接，并且总是在引擎盖下创建，但我们也可以以编程方式设置它。尽管这种方法更详细，但有一些优点。让我们在接下来的几个小节中看一下它们。

    1. 通过AbstractMongoClientConfiguration连接

        在第一个示例中，我们将从应用程序类中的Spring Data MongoDB扩展AbstractMongoClientConfiguration类：

        ```java
        @SpringBootApplication
        public class SpringMongoConnectionViaClientApp extends AbstractMongoClientConfiguration {
            // main method
        }
        ```

        接下来，我们将注入我们需要的属性：

        ```java
        @Value("${spring.data.mongodb.uri}")
        private String uri;

        @Value("${spring.data.mongodb.database}")
        private String db;
        ```

        澄清一下，这些属性可以硬编码。此外，他们可以使用与预期的Spring数据变量不同的名称。最重要的是，这次我们使用的是URI，而不是单个连接属性，这些属性不能混合。因此，我们不能为此应用程序重复使用我们的应用程序。属性，我们应该将其移动到其他地方。

        AbstractMongoClientConfiguration要求我们覆盖getDatabaseName（）。这是因为URI中不需要数据库名称：

        ```java
        protected String getDatabaseName() {
            return db;
        }
        ```

        此时，由于我们使用的是默认的Spring Data变量，我们已经能够连接到我们的数据库。此外，如果数据库不存在，MongoDB会创建数据库。让我们来测试一下：

        ```java
        @Test
        public void whenClientConfig_thenInsertSucceeds() {
            SpringApplicationBuilder app = new SpringApplicationBuilder(SpringMongoConnectionViaClientApp.class);
            app.web(WebApplicationType.NONE)
            .run(
                "--spring.data.mongodb.uri=mongodb://" + USER + ":" + PASS + "@" + HOST + ":" + PORT + "/" + DB,
                "--spring.data.mongodb.database=" + DB
            );

            assertInsertSucceeds(app.context());
        }
        ```

        最后，我们可以覆盖mongoClient（），以获得比传统配置的优势。此方法将使用我们的URI变量来构建MongoDB客户端。这样，我们可以直接参考它。例如，这使我们能够列出连接中可用的所有数据库：

        ```java
        @Override
        public MongoClient mongoClient() {
            MongoClient client = MongoClients.create(uri);
            ListDatabasesIterable<Document> databases = client.listDatabases();
            databases.forEach(System.out::println);
            return client;
        }
        ```

        如果我们想要完全控制MongoDB客户端的创建，以这种方式配置连接是有用的。

    2. 创建自定义MongoClientFactoryBean

        在下一个示例中，我们将创建一个MongoClientFactoryBean。这一次，我们将创建一个名为custom.uri的属性来保持我们的连接配置：

        ```java
        @SpringBootApplication
        public class SpringMongoConnectionViaFactoryApp {

            // main method

            @Bean
            public MongoClientFactoryBean mongo(@Value("${custom.uri}") String uri) {
                MongoClientFactoryBean mongo = new MongoClientFactoryBean();
                ConnectionString conn = new ConnectionString(uri);
                mongo.setConnectionString(conn);

                MongoClient client = mongo.getObject();
                client.listDatabaseNames()
                .forEach(System.out::println);
                return mongo;
            }
        }
        ```

        使用这种方法，我们不需要扩展AbstractMongoClientConfiguration。我们还控制着MongoClient的创作。例如，通过调用mongo.setSingleton（false），我们每次调用mongo.getObject（）时都会获得一个新客户端，而不是单例。

    3. 使用MongoClientSettingsBuilderCustomizer设置连接详细信息

        在上一个示例中，我们将使用MongoClientSettingsBuilderCustomizer：

        ```java
        @SpringBootApplication
        public class SpringMongoConnectionViaBuilderApp {

            // main method

            @Bean
            public MongoClientSettingsBuilderCustomizer customizer(@Value("${custom.uri}") String uri) {
                ConnectionString connection = new ConnectionString(uri);
                return settings -> settings.applyConnectionString(connection);
            }
        }
        ```

        我们使用此类来自定义部分连接，但其余部分仍有自动配置。当我们只需要以编程方式设置几个属性时，这很有帮助。

5. 结论

    在本文中，我们研究了Spring Data MongoDB带来的不同工具。我们用它们以不同的方式建立联系。此外，我们构建了测试用例，以保证我们的配置按预期工作。最后，我们看到了配置优先级如何影响我们的连接属性。
