# [使用Liquibase安全地进化数据库模式](https://www.baeldung.com/liquibase-refactor-schema-of-java-app)

1. 一览表

    在这个快速教程中，我们将学习如何使用[Liquibase](http://www.liquibase.org/)来进化Java Web应用程序的数据库模式。首先，我们将检查一个通用的Java应用程序，然后我们将专注于一些与Spring和Hibernate集成良好的有趣选项。

    使用Liquibase时，我们可以使用一系列更改日志文件来描述数据库模式的演变。尽管这些文件可以以多种格式编写，如SQL、XML、JSON和YAML，但在这里我们将只关注XML格式来描述DB的更改。因此，随着我们随着时间的推移更改其模式，我们可以为我们的DB拥有许多更改日志文件。此外，所有这些文件在名为master.xml的单个根更改日志文件中都有一个引用，Liquibase将使用该文件。

    我们可以使用[include](https://docs.liquibase.com/change-types/include.html)和[includeAll](https://docs.liquibase.com/change-types/includeall.html)属性在此根更改日志下嵌套其他更改日志文件。

    让我们从在我们的pom.xml中包含最新的liquibase-core依赖项开始：

    ```xml
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
        <version>4.27.0</version>
    </dependency>
    ```

2. 数据库变更日志

    首先，让我们来看看一个简单的XML更改日志文件，以了解其结构。在本例中，我们创建了一个名为20170503041524_added_entity_Car.xml的文件，该文件添加了一个名为car的新表，其列为id、make、brand和price：

    ```xml
    <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    ...
    <!--Added the entity Car-->
    <changeSet id="20170503041524-1" author="user">
        <createTable tableName="car">
            <column name="id" type="bigint" autoIncrement="${autoIncrement}">
                <constraints primaryKey="true" nullable="false" />
            </column>
            <column name="make" type="varchar(255)">
                <constraints nullable="true" />
            </column>
            <column name="brand" type="varchar(255)">
                <constraints nullable="true" />
            </column>
            <column name="price" type="double">
                <constraints nullable="true" />
            </column>
        </createTable>
    </changeSet>
    </databaseChangeLog>
    ```

    让我们忽略所有命名空间定义一秒钟，专注于changeSet标签。值得注意的是，id和作者属性如何识别更改集。这确保了任何给定的更改只应用一次。此外，我们也可以直接看到变化的作者。

    现在，要使用此更改日志，我们需要在master.xml中定义该文件：

    ```xml
    <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

        <include file="classpath:config/liquibase/changelog/00000000000000_initial_schema.xml" relativeToChangelogFile="false" />
    
        <include file="classpath:config/liquibase/changelog/20170503041524_added_entity_Car.xml" relativeToChangelogFile="false" />

    </databaseChangeLog>
    ```

    现在，Liquibase使用master.xml文件来跟踪已应用于数据库的更改集。为了减少冲突，我们在包含更改集的文件前加上数据时间戳。日期时间戳允许Liquibase确定哪些更改集已经应用，哪些更改集仍需要应用。这只是我们使用的惯例，可以根据我们的需要进行更改。

    一旦应用了更改集，我们通常无法进一步编辑相同的更改集。因此，对于任何给定的模式修改，我们必须创建一个新的更改集文件，这将为我们的数据库版本提供一致性。

    现在让我们看看如何将其连接到我们的Spring应用程序中，并确保它在应用程序启动时运行。

3. 用Spring Bean 运行Liquibase

    我们在应用程序启动时运行更改的第一个选项是通过Spring bean。

    当然，还有很多其他方法，但如果我们要处理Spring应用程序，这是一个很好、简单的方法：

    ```java
    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:config/liquibase/master.xml");
        liquibase.setDataSource(dataSource());
        return liquibase;
    }
    ```

    或者，更完整的配置文件需要额外的设置：

    ```java
    @Bean
    public SpringLiquibase liquibase(@Qualifier("taskExecutor") TaskExecutor taskExecutor,
            DataSource dataSource, LiquibaseProperties liquibaseProperties) {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start asynchronously
        SpringLiquibase liquibase = new AsyncSpringLiquibase(taskExecutor, env);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:config/liquibase/master.xml");
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            log.debug("Configuring Liquibase");
        }
        return liquibase;
    }
    ```

    再次，我们在项目的类路径上创建了master.xml文件。因此，如果位置不同，我们可能想要提供正确的路径。

4. 将Liquibase与Spring Boot一起使用

    使用[Spring Boot](https://contribute.liquibase.com/extensions-integrations/directory/integration-docs/springboot/springboot/)时，我们不需要为Liquibase定义bean，因为包括liquibase-core依赖项将为我们定义。但我们需要专门将master.xml放入insrc/main/resources/config/liquibase/master.xml，以便Liquibase可以在启动时读取并自动运行。

    或者，我们也可以通过设置liquibase.change-log属性来更改默认的更改日志文件位置：

    `liquibase.change-log=classpath:liquibase-changeLog.xml`

5. 在Spring Boot中禁用Liquibase

    有时，我们可能不想在启动时运行Liquibase迁移。因此，要禁用Liquibase，我们可以使用spring.liquibase.enabled属性：

    `spring.liquibase.enabled=false`

    这样，Liquibase配置不会被应用，数据库的模式保持不变。

    我们应该记住，Spring Boot 2.x之前的应用程序需要使用liquibase.enabled属性：

    `liquibase.enabled=false`

6. 使用Maven插件生成更改日志

    手动编写更改日志文件可能既耗时又容易出错。相反，我们可以使用Liquibase Maven插件从现有数据库中生成一个，为自己节省大量工作。

    1. 插件配置

        让我们在我们的pom.xml中使用最新的liquibase-maven-plugin依赖项：

        ```xml
        <plugin>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-maven-plugin</artifactId>
        <version>4.27.0</version>
            ...
        <configuration>
            ...
            <driver>org.h2.Driver</driver>
            <url>jdbc:h2:file:./target/h2db/db/carapp</url>
            <username>carapp</username>
            <password />
            <outputChangeLogFile>src/main/resources/liquibase-outputChangeLog.xml</outputChangeLogFile>
        </configuration>
        </plugin>
        ```

        上面，我们只是描述了数据库驱动程序及其相关配置和更改日志文件的输出。

    2. 从现有数据库生成变更日志

        让我们使用mvn命令从现有数据库生成更改日志：

        `mvn liquibase:generateChangeLog`

        现在，我们可以使用生成的更改日志文件来创建初始DB模式或填充数据。

        这将在配置的位置创建一个更改日志文件：

        ```xml
        <databaseChangeLog ...>
        <changeSet id="00000000000001" author="jhipster">
            <createTable tableName="jhi_persistent_audit_event">
                <column name="event_id" type="bigint" autoIncrement="${autoIncrement}">
                    <constraints primaryKey="true" nullable="false" />
                </column>
                <column name="principal" type="varchar(50)">
                    <constraints nullable="false" />
                </column>
                <column name="event_date" type="timestamp" />
                <column name="event_type" type="varchar(255)" />
            </createTable>
            <createTable tableName="jhi_persistent_audit_evt_data">
                <column name="event_id" type="bigint">
                    <constraints nullable="false" />
                </column>
                <column name="name" type="varchar(150)">
                    <constraints nullable="false" />
                </column>
                <column name="value" type="varchar(255)" />
            </createTable>
            <addPrimaryKey columnNames="event_id, name" tableName="jhi_persistent_audit_evt_data" />
            <createIndex indexName="idx_persistent_audit_event" tableName="jhi_persistent_audit_event" unique="false">
                <column name="principal" type="varchar(50)" />
                <column name="event_date" type="timestamp" />
            </createIndex>
            <createIndex indexName="idx_persistent_audit_evt_data" tableName="jhi_persistent_audit_evt_data" unique="false">
                <column name="event_id" type="bigint" />
            </createIndex>
            <addForeignKeyConstraint baseColumnNames="event_id" baseTableName="jhi_persistent_audit_evt_data" constraintName="fk_evt_pers_audit_evt_data" referencedColumnNames="event_id" referencedTableName="jhi_persistent_audit_event" />
        </changeSet>

        <changeSet id="20170503041524-1" author="jhipster">
            <createTable tableName="car">
                <column name="id" type="bigint" autoIncrement="${autoIncrement}">
                    <constraints primaryKey="true" nullable="false" />
                </column>
                <column name="make" type="varchar(255)">
                    <constraints nullable="true" />
                </column>
                <column name="brand" type="varchar(255)">
                    <constraints nullable="true" />
                </column>
                <column name="price" type="double">
                    <constraints nullable="true" />
                </column>
                <!-- jhipster-needle-liquibase-add-column - JHipster will add columns here, do not remove-->
            </createTable>
        </changeSet>
        </databaseChangeLog>
        ```

    3. 从两个数据库之间的差异生成更改日志

        接下来，使用diff命令，我们可以生成一个包含两个现有数据库差异的changeLog文件：

        `mvn liquibase:diff`

        然后，我们可以使用此文件对齐两个数据库。为了正常工作，我们需要使用其他属性配置liquibase插件：

        ```properties
        changeLogFile=src/main/resources/config/liquibase/master.xml
        url=jdbc:h2:file:./target/h2db/db/carapp
        username=carapp
        password=
        driver=com.mysql.jdbc.Driver
        referenceUrl=jdbc:h2:file:./target/h2db/db/carapp2
        referenceDriver=org.h2.Driver
        referenceUsername=tutorialuser2
        referencePassword=tutorialmy5ql2
        ```

        再次，这将生成一个更改日志文件：

        ```xml
        <databaseChangeLog ...>
            <changeSet author="John" id="1439227853089-1">
                <dropColumn columnName="brand" tableName="car"/>
            </changeSet>
        </databaseChangeLog>
        ```

        这是发展我们的数据库的一种超级强大的方法。例如，我们可以允许Hibernate自动生成一个新的开发模式，然后将其用作旧模式的参考点。

7. 使用Liquibase休眠插件

    如果我们的应用程序使用Hibernate，我们可以使用liquibase-hibernate5来生成changeLog文件。

    1. 插件配置

        首先，让我们在pom.xml中添加最新版本的依赖项：

        ```xml
        <plugin>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-maven-plugin</artifactId>
        <version>4.27.0</version>
        <dependencies>
            ...
            <dependency>
                <groupId>org.liquibase.ext</groupId>
                <artifactId>liquibase-hibernate5</artifactId>
                <version>3.6</version>
            </dependency>
            ...
        </dependencies>
        ...
        </plugin>
        ```

    2. 从数据库和持久性实体之间的差异生成更改日志

        现在，我们可以使用此插件根据现有数据库（例如生产）和我们新的持久性实体之间的差异生成更改日志文件。

        因此，为了使事情变得简单，一旦实体被修改，我们就可以针对旧的DB模式生成更改，获得一种干净、强大的方法来在生产中发展我们的模式。

        以下是我们在pom.xml中插件配置中的liquibase属性：

        ```xml
        <configuration>
        <changeLogFile>src/main/resources/config/liquibase/master.xml</changeLogFile>
        <diffChangeLogFile>src/main/resources/config/liquibase/changelog/${maven.build.timestamp}_changelog.xml</diffChangeLogFile>
        <driver>org.h2.Driver</driver>
        <url>jdbc:h2:file:./target/h2db/db/carapp</url>
        <defaultSchemaName />
        <username>carapp</username>
        <password />
        <referenceUrl>hibernate:spring:com.car.app.domain?dialect=org.hibernate.dialect.H2Dialect
            &hibernate.physical_naming_strategy=org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
            &hibernate.implicit_naming_strategy=org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
        </referenceUrl>
        <verbose>true</verbose>
        <logging>debug</logging>
        </configuration>
        ```

        值得注意的是，referenceUrl正在使用软件包扫描，因此需要方言参数。

8. 使用JPA Buddy插件在IntelliJ IDEA中生成更改日志

    如果我们使用的是非Hibernate ORM（例如EclipseLink或OpenJPA），或者我们不想添加额外的依赖项，如liquibase-hibernate插件，我们可以使用JPA Buddy。这个IntelliJ IDEA插件将有用的Liquibase功能集成到IDE中。

    为了生成差分更改日志，我们安装插件，然后从JPA结构面板调用操作。我们选择要与目标（数据库或Liquibase快照）进行比较的来源（数据库、JPA实体或Liquibase快照）。

    JPA Buddy将生成更改日志。

    与liquibase-hibernate插件相比，JPA Buddy的另一个优势是能够覆盖Java和数据库类型之间的默认映射。此外，它与Hibernate自定义类型和JPA转换器正常工作。

9. 结论

    在本文中，我们说明了使用Liquibase的几种方法，并以安全成熟的方式发展和重构Java应用程序的DB模式。

    一如既往，我们可以在 [GitHub](/jhipster-8-modules/jhipster-8-microservice/car-app) 上找到这些示例的完整实现。
