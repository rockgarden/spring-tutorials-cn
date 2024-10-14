# [Spring Data Couchbase简介](https://www.baeldung.com/spring-data-couchbase)

1. 介绍

    在本关于Spring Data的教程中，我们将讨论如何使用Spring Data存储库和模板抽象为Couchbase文档设置持久性层，以及准备Couchbase使用视图和/或索引支持这些抽象所需的步骤。

2. Maven附属机构

    首先，我们将以下Maven依赖项添加到我们的pom.xml文件中：

    ```xml
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-couchbase</artifactId>
        <version>5.0.3</version>
    </dependency>
    ```

    请注意，通过包含此依赖项，我们会自动获得本机Couchbase SDK的兼容版本，因此我们不需要明确包含它。

    为了增加对JSR-303 bean验证的支持，我们还包括以下依赖项：

    ```xml
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
    </dependency>
    ```

    Spring Data Couchbase通过传统的日期和日历类以及Joda时间库支持日期和时间持久性，我们包括以下内容：

    ```xml
    <dependency>
        <groupId>joda-time</groupId>
        <artifactId>joda-time</artifactId>
        <version>2.12.2</version>
    </dependency>
    ```

3. 配置

    接下来，我们需要通过指定Couchbase集群的一个或多个节点以及存储文档的存储桶的名称和密码来配置Couchbase环境。

    1. Java配置

        对于Java类配置，我们只是扩展AbstractCouchbaseConfiguration类：

        ```java
        @Configuration
        @EnableCouchbaseRepositories(basePackages = { "com.baeldung.spring.data.couchbase" })
        public class MyCouchbaseConfig extends AbstractCouchbaseConfiguration {

            public static final String NODE_LIST = "localhost";
            public static final String BUCKET_NAME = "baeldung";
            public static final String BUCKET_USERNAME = "baeldung";
            public static final String BUCKET_PASSWORD = "baeldung";

            @Override
            public String getConnectionString() {
                return NODE_LIST;
            }

            @Override
            public String getUserName() {
                return BUCKET_USERNAME;
            }

            @Override
            public String getPassword() {
                return BUCKET_PASSWORD;
            }

            @Override
            public String getBucketName() {
                return BUCKET_NAME;
            }

            @Override
            public QueryScanConsistency getDefaultConsistency() {
                return QueryScanConsistency.REQUEST_PLUS;
            }

            @Bean
            public LocalValidatorFactoryBean localValidatorFactoryBean() {
                return new LocalValidatorFactoryBean();
            }

            @Bean
            public ValidatingCouchbaseEventListener validatingCouchbaseEventListener() {
                return new ValidatingCouchbaseEventListener(localValidatorFactoryBean());
            }
        }
        ```

        如果您的项目需要对Couchbase环境进行更多自定义，您可以通过覆盖thegetEnvironment（）方法来提供一个：

        ```java
        @Override
        protected CouchbaseEnvironment getEnvironment() {...}
        ```

4. 数据模型

    让我们创建一个表示JSON文档的实体类来持久化。我们首先用@Document注释类，然后用@Id注释字符串字段，以表示Couchbase文档键。

    您可以使用Spring Data中的@Id注释或原生Couchbase SDK中的@Id注释。请注意，如果您在同一类中在两个不同的字段上使用两个@Id注释，则使用Spring Data @Id注释注释的字段将优先，并用作文档键。

    为了表示JSON文档的属性，我们添加了用@Field注释的私有成员变量。我们使用@NotNull注释来按要求标记某些字段：

    ```java
    @Document
    public class Person {
        @Id
        private String id;

        @Field
        @NotNull
        private String firstName;
        
        @Field
        @NotNull
        private String lastName;
        
        @Field
        @NotNull
        private DateTime created;
        
        @Field
        private DateTime updated;
        
        // standard getters and setters
    }
    ```

    请注意，用@Id注释的属性仅代表文档密钥，不一定是存储的JSON文档的一部分，除非它也用@Field注释，如：

    ```java
    @Id
    @Field
    private String id;
    ```

    如果您想将实体类中的字段命名与JSON文档中存储的字段不同，只需限定其@Field注释，如以下示例所示：

    ```java
    @Field("fname")
    private String firstName;
    ```

    以下是一个示例，展示了持久的Person文档的外观：

    ```json
    {
        "firstName": "John",
        "lastName": "Smith",
        "created": 1457193705667
        "_class": "com.baeldung.spring.data.couchbase.model.Person"
    }
    ```

    请注意，Spring Data会自动为每个文档添加一个属性，其中包含实体的完整类名称。默认情况下，此属性名为“_class”，尽管您可以通过覆盖typeKey（）方法在Couchbase配置类中覆盖该属性。

    例如，如果您想指定一个名为“dataType”的字段来存储类名，您将将此添加到您的Couchbase配置类中：

    ```java
    @Override
    public String typeKey() {
        return "dataType";
    }
    ```

    覆盖typeKey（）的另一个常见原因是，如果您使用的是不支持下划线前缀的字段的Couchbase Mobile版本。在这种情况下，您可以像上一个示例一样选择自己的备用类型字段，或者您可以使用Spring提供的备用类型字段：

    ```java
    @Override
    public String typeKey() {
        // use "javaClass" instead of "_class"
        return MappingCouchbaseConverter.TYPEKEY_SYNCGATEWAY_COMPATIBLE;
    }
    ```

5. Couchbase存储库

    Spring Data Couchbase提供与JPA等其他Spring Data模块相同的内置查询和派生查询机制。

    我们通过扩展`CrudRepository<String,Person>`并添加可推导查询方法来声明Person类的存储库接口：

    ```java
    public interface PersonRepository extends CrudRepository<Person, String> {
        List<Person> findByFirstName(String firstName);
        List<Person> findByLastName(String lastName);
    }
    ```

6. 通过索引支持N1QL

    如果使用Couchbase 4.0或更高版本，则默认使用N1QL引擎处理自定义查询（除非其相应的存储库方法用@View注释，以指示使用下一节所述的备份视图）。

    要增加对N1QL的支持，您必须在存储桶上创建一个主索引。您可以使用cbq命令行查询处理器（有关如何为您的环境启动cbq工具，请参阅您的Couchbase文档）并发出以下命令来创建索引：

    `CREATE PRIMARY INDEX ON baeldung USING GSI;`

    在上述命令中，GSI代表全局二级索引，这是一种特别适合优化支持OLTP系统的临时N1QL查询的索引类型，如果没有另行说明，则是默认索引类型。

    与基于视图的索引不同，GSI索引不会在集群中的所有索引节点上自动复制，因此，如果您的集群包含多个索引节点，则需要在集群中的每个节点上创建每个GSI索引，并且您必须在每个节点上提供不同的索引名称。

    您还可以创建一个或多个次要索引。当您这样做时，Couchbase将根据需要使用它们，以优化其查询处理。

    例如，要在firstName字段上添加索引，请在cbq工具中发出以下命令：

    `CREATE INDEX idx_firstName ON baeldung(firstName) USING GSI;`

7. 备份视图

    对于每个存储库接口，您需要在目标存储桶中创建一个Couchbase设计文档和一个或多个视图。设计文档名称必须是实体类名称的下骆驼大小写版本（例如“person”）。

    无论您运行的是哪个版本的Couchbase Server，您都必须创建一个名为“all”的备份视图，以支持内置的“findAll”存储库方法。这是我们Person类的“all”视图的地图函数：

    ```java
    function (doc, meta) {
        if(doc._class == "com.baeldung.spring.data.couchbase.model.Person") {
            emit(meta.id, null);
        }
    }
    ```

    使用4.0之前的Couchbase版本时，每个自定义存储库方法都必须有一个备份视图（在4.0或更高版本中，使用备份视图是可选的）。

    视图支持的自定义方法必须用@View注释，如以下示例所示：

    ```java
    @View
    List<Person> findByFirstName(String firstName);
    ```

    支持视图的默认命名惯例是使用方法名称该部分的lowerCamelCase版本，并遵循“find”关键字（例如“byFirstName”）。

    以下是您如何为“byFirstName”视图编写地图函数：

    ```java
    function (doc, meta) {
        if(doc._class == "com.baeldung.spring.data.couchbase.model.Person"
        && doc.firstName) {
            emit(doc.firstName, null);
        }
    }
    ```

    您可以覆盖此命名惯例，并通过用相应的备份视图的名称限定每个@View注释来使用您自己的视图名称。例如：

    ```java
    @View("myCustomView")
    List<Person> findByFirstName(String lastName);
    ```

8. 服务层

    对于我们的服务层，我们定义了一个接口和两个实现：一个使用Spring数据存储库抽象，另一个使用Spring数据模板抽象。这是我们的PersonService界面：

    ```java
    public interface PersonService {
        Optional<Person> findOne(String id);
        List<Person> findAll();
        List<Person> findByFirstName(String firstName);
        List<Person> findByLastName(String lastName);
        void create(Person person);
        void update(Person person);
        void delete(Person person);
    }
    ```

    1. 存储库服务

        以下是使用我们上面定义的存储库的实现：

        ```java
        @Service
        @Qualifier("PersonRepositoryService")
        public class PersonRepositoryService implements PersonService {

            @Autowired
            private PersonRepository repo; 

            public Optional<Person> findOne(String id) {
                return repo.findById(id);
            }

            public List<Person> findAll() {
                List<Person> people = new ArrayList<Person>();
                Iterator<Person> it = repo.findAll().iterator();
                while(it.hasNext()) {
                    people.add(it.next());
                }
                return people;
            }

            public List<Person> findByFirstName(String firstName) {
                return repo.findByFirstName(firstName);
            }

            public void create(Person person) {
                person.setCreated(DateTime.now());
                repo.save(person);
            }

            public void update(Person person) {
                person.setUpdated(DateTime.now());
                repo.save(person);
            }

            public void delete(Person person) {
                repo.delete(person);
            }
        }
        ```

    2. 模板服务

        CouchbaseTemplate对象在我们的Spring上下文中可用，并可能注入到服务类中。在spring data couchbase 5.0.3中，删除了通过视图查找文档的方法。您可以使用findByQuery来代替。

        以下是使用模板抽象的实现：

        ```java
        @Service
        @Qualifier("PersonTemplateService")
        public class PersonTemplateService implements PersonService {

            private static final String DESIGN_DOC = "person";

            private CouchbaseTemplate template;

            @Autowired
            public void setCouchbaseTemplate(CouchbaseTemplate template) {
                this.template = template;
            }

            public Optional<Person> findOne(String id) {
                return Optional.of(template.findById(Person.class).one(id));
            }

            public List<Person> findAll() {
                return template.findByQuery(Person.class).all();
            }

            public List<Person> findByFirstName(String firstName) {
                return template.findByQuery(Person.class).matching(where("firstName").is(firstName)).all();
            }

            public List<Person> findByLastName(String lastName) {
                return template.findByQuery(Person.class).matching(where("lastName").is(lastName)).all();
            }

            public void create(Person person) {
                person.setCreated(DateTime.now());
                template.insertById(Person.class).one(person);
            }

            public void update(Person person) {
                person.setUpdated(DateTime.now());
                template.removeById(Person.class).oneEntity(person);
            }

            public void delete(Person person) {
                template.removeById(Person.class).oneEntity(person);
            }
        }
        ```

9. 结论

    我们已经展示了如何配置项目以使用Spring Data Couchbase模块，以及如何编写一个简单的实体类及其存储库接口。我们编写了一个简单的服务接口，并提供了一个使用存储库的实现，另一个使用Spring Data模板API的实现。

    欲了解更多信息，请访问Spring Data Couchbase项目[网站](https://spring.io/projects/spring-data-couchbase)。
