# [使用Flyway进行数据库迁移](https://www.baeldung.com/database-migrations-with-flyway)

1. 介绍

    在本教程中，我们将探索Flyway的关键概念，以及如何使用此框架可靠、轻松地持续重塑应用程序的数据库模式。此外，我们将介绍一个使用Maven Flyway插件管理内存H2数据库的示例。

    Flyway使用迁移将数据库从一个版本更新到下一个版本。我们可以在SQL中使用特定于数据库的语法编写迁移，也可以在Java中编写高级数据库转换的迁移。

    迁移可以是版本化的，也可以是可重复的。前者有一个独特的版本，并且正好应用一次。后者没有版本。相反，每次校验和发生变化时，它们都会（重新）应用。

    在一次迁移运行中，可重复的迁移总是在执行待发布的版本迁移后最后应用。可重复的迁移按照描述的顺序进行应用。对于单个迁移，所有语句都在单个数据库事务中运行。

    在本教程中，我们将主要关注如何使用Maven插件执行数据库迁移。

2. Flyway Maven插件

    要安装Flyway Maven插件，让我们将以下插件定义添加到我们的pom.xml中：

    ```xml
    <plugin>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-maven-plugin</artifactId>
        <version>10.17.0</version>
    </plugin>
    ```

    最新版本的插件可在Maven Central上获得。

    我们可以通过四种不同的方式配置这个Maven插件。在接下来的章节中，我们将对每个选项进行审查。

    请参考[文档](https://flywaydb.org/documentation/usage/maven/migrate)，以获取所有可配置属性的列表。

    1. 插件配置

        我们可以直接通过pom.xml插件定义中的`<configuration>`标签配置插件：

        ```xml
        <plugin>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-maven-plugin</artifactId>
            <version>10.17.0</version>
            <configuration>
                <user>databaseUser</user>
                <password>databasePassword</password>
                <schemas>
                    <schema>schemaName</schema>
                </schemas>
                ...
            </configuration>
        </plugin>
        ```

    2. Maven属性

        我们还可以通过在pom中将可配置属性指定为Maven属性来配置插件：

        ```xml
        <project>
            ...
            <properties>
                <flyway.user>databaseUser</flyway.user>
                <flyway.password>databasePassword</flyway.password>
                <flyway.schemas>schemaName</flyway.schemas>
                ...
            </properties>
            ...
        </project>
        ```

    3. 外部配置文件

        另一种选择是在单独的.conf文件中提供插件配置：

        ```conf
        flyway.user=databaseUser
        flyway.password=databasePassword
        flyway.schemas=schemaName
        ...
        ```

        默认配置文件名称是flyway.conf，默认情况下，它从以下方式加载配置文件：

        - installDir/conf/flyway.conf
        - userhome/flyway.conf
        - workingDir/flyway.conf

        编码由flyway.encoding属性指定（UTF-8是默认属性）。

        如果我们使用任何其他名称（例如customConfig.conf）作为配置文件，那么在调用Maven命令时，我们必须明确指定：

        `$ mvn -Dflyway.configFiles=customConfig.conf`

    4. 系统属性

        最后，在命令行调用Maven时，所有配置属性也可以指定为系统属性：

        ```bash
        $ mvn -Dflyway.user=databaseUser -Dflyway.password=databasePassword 
        -Dflyway.schemas=schemaName
        ```

        以下是以多种方式指定配置时的优先级：

        1. 系统属性
        2. 外部配置文件
        3. Maven属性
        4. 插件配置

3. 迁移示例

    在本节中，我们将介绍使用Maven插件将数据库模式迁移到内存H2数据库的必要步骤。我们使用外部文件来配置Flyway。

    1. 更新POM

        首先，让我们添加H2作为依赖项：

        ```xml
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
        ```

        再次，我们可以在Maven Central上查看最新版本的驱动程序。如前所述，我们还添加了Flyway插件。

    2. 使用外部文件配置Flyway

        接下来，我们在$PROJECT_ROOT中创建myFlywayConfig.conf，内容如下：

        ```conf
        flyway.user=databaseUser
        flyway.password=databasePassword
        flyway.schemas=app-db
        flyway.url=jdbc:h2:mem:DATABASE
        flyway.locations=filesystem:db/migration
        ```

        上述配置指定我们的迁移脚本位于db/migration目录中。它使用数据库用户和数据库密码连接到内存中的H2实例。

        应用程序数据库模式是app-db。

        当然，我们分别用我们自己的数据库用户名、数据库密码和数据库URL替换flyway.user、flyway.password和flyway.url。

    3. 定义第一次迁移

        Flyway遵守以下迁移脚本的命名惯例：

        `<Prefix><Version>__<Description>.sql`

        地点：

        - <Prefix>  – 默认前缀为V，我们可以使用flyway.sqlMigrationPrefix属性在上述配置文件中更改。
        - <Version> – 迁移版本号。主要版本和次要版本可以用下划线隔开。迁移版本应始终以1开头。
        - <Description> – 迁移的文本描述。双下划线将描述与版本号分开。

        示例：V1_1_0__my_first_migration.sql

        因此，让我们在$PROJECT_ROOT中创建一个目录db/migration，使用名为V1_0__create_employee_schema.sql的迁移脚本，其中包含创建员工表的SQL指令：

        ```sql
        CREATE TABLE IF NOT EXISTS `employee` (
            `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
            `name` varchar(20),
            `email` varchar(50),
            `date_of_birth` timestamp
        )ENGINE=InnoDB DEFAULT CHARSET=UTF8;
        ```

    4. 执行迁移

        接下来，我们从$PROJECT_ROOT调用以下Maven命令来执行数据库迁移：

        `$ mvn clean flyway:migrate -Dflyway.configFiles=myFlywayConfig.conf`

        这应该会导致我们的第一次成功迁移。

        数据库模式现在应该看起来像这样：

        ```txt
        employee:
        +----+------+-------+---------------+
        | id | name | email | date_of_birth |
        +----+------+-------+---------------+
        ```

        我们可以重复定义和执行步骤来进行更多的迁移。

    5. 定义并执行第二次迁移

        让我们通过创建一个名为V2_0_create_department_schema.sql的第二个迁移文件，看看第二次迁移是什么样子的，其中包含以下两个查询：

        ```sql
        CREATE TABLE IF NOT EXISTS `department` (

        `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
        `name` varchar(20)

        )ENGINE=InnoDB DEFAULT CHARSET=UTF8; 

        ALTER TABLE `employee` ADD `dept_id` int AFTER `email`;
        ```

        然后，我们将执行与第一次类似的迁移。

        现在，我们的数据库模式已经更改，为员工添加新列和新表：

        ```txt
        employee:
        +----+------+-------+---------+---------------+
        | id | name | email | dept_id | date_of_birth |
        +----+------+-------+---------+---------------+

        department:
        +----+------+
        | id | name |
        +----+------+
        ```

        最后，我们可以通过调用以下Maven命令来验证这两个迁移是否确实成功：

        `$ mvn flyway:info -Dflyway.configFiles=myFlywayConfig.conf`

4. 在IntelliJ IDEA中生成版本迁移

    手动编写迁移需要大量时间；相反，我们可以根据我们的JPA实体生成它们。我们可以通过使用名为[JPA Buddy](https://www.baeldung.com/jpa-buddy-post)的IntelliJ IDEA插件来实现这一点。

    要生成差异化版本迁移，只需安装插件并从JPA结构面板调用操作。

    我们只需选择要比较的源（数据库或JPA实体）与目标（数据库或数据模型快照）。然后，JPA Buddy将生成动画中显示的迁移。

    JPA Buddy的另一个优势是能够定义Java和数据库类型之间的映射。此外，它与Hibernate自定义类型和JPA转换器正常工作。

5. 在Spring Boot中禁用Flyway

    在某些情况下，有时我们可能需要禁用Flyway迁移。

    例如，在测试期间根据实体生成数据库模式是常见的做法。在这种情况下，我们可以在测试配置文件下禁用Flyway。

    让我们看看Spring Boot有多容易。

    1. Spring Boot 1.x

        我们只需要在我们的application-test.properties文件中设置flyway.enabled属性：

        `flyway.enabled=false`

    2. Spring Boot 2.x，3.x

        在较新版本的Spring Boot中，此属性已更改为spring.flyway.enabled：

        `spring.flyway.enabled=false`

    3. Empty FlywayMigrationStrategy

        如果我们只想在启动时禁用自动Flyway迁移，但仍然能够手动触发迁移，那么使用上述属性不是一个好选择。

        这是因为在这种情况下，Spring Boot将不再自动配置Flyway bean。因此，我们必须自己提供，这不太方便。

        因此，如果这是我们的用例，我们可以启用Flyway，并实现一个空的[FlywayMigrationStrategy](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/flyway/FlywayMigrationStrategy.html)：

        ```java
        @Configuration
        public class EmptyMigrationStrategyConfig {

            @Bean
            public FlywayMigrationStrategy flywayMigrationStrategy() {
                return flyway -> {
                    // do nothing  
                };
            }
        }
        ```

        这将有效地禁用应用程序启动时的Flyway迁移。

        然而，我们仍然可以手动触发迁移：

        ```java
        @RunWith(SpringRunner.class)
        @SpringBootTest
        public class ManualFlywayMigrationIntegrationTest {

            @Autowired
            private Flyway flyway;

            @Test
            public void skipAutomaticAndTriggerManualFlywayMigration() {
                flyway.migrate();
            }
        }
        ```

6. Flyway是如何工作的

    为了跟踪我们已经应用了哪些迁移以及何时应用了迁移，它在我们的模式中添加了一个特殊的簿记表。此元数据表还跟踪迁移校验和，以及迁移是否成功。

    该框架执行以下步骤来适应不断变化的数据库模式：

    1. 它检查数据库模式以定位其元数据表（默认为SCHEMA_VERSION）。如果元数据表不存在，它将创建一个。
    2. 它扫描应用程序类路径以查找可用的迁移。
    3. 它将迁移与元数据表进行比较。如果版本号低于或等于标记为当前的版本，则会忽略它。
    4. 它将任何剩余的迁移标记为待定的迁移。这些是根据版本号排序的，并按顺序执行。
    5. 随着每次迁移的应用，元数据表也会相应地更新。

7. 命令

    Flyway支持以下基本命令来管理数据库迁移：

    - Info：打印数据库模式的当前状态/版本。它打印了哪些迁移正在等待，哪些迁移已应用，应用迁移的状态，以及它们何时应用。
    - Migrate：将数据库模式迁移到当前版本。它扫描类路径以查找可用的迁移，并应用待处理的迁移。
    - Baseline：基线是现有数据库，不包括所有迁移，包括基线版本。Baseline有助于在现有数据库中从Flyway开始。然后可以正常应用较新的迁移。
    - Validate：根据可用的迁移验证当前数据库模式。
    - Repair:：修复元数据表。
    - Clean：删除配置模式中的所有对象。当然，我们永远不应该在任何生产数据库上使用clean。

8. 结论

    在本文中，我们了解了Flyway的工作原理，以及如何使用此框架可靠地重塑我们的应用程序数据库。
