# [用 Spring Data JPA 生成数据库模式](https://www.baeldung.com/spring-data-jpa-generate-db-schema)

1. 概述

    创建持久层时，我们需要将 SQL 数据库模式与我们在代码中创建的对象模型相匹配。这需要大量的手动工作。

    在本教程中，我们将学习如何根据代码中的实体模型生成和导出数据库模式。

    首先，我们将介绍用于生成模式的 JPA 配置属性。然后，我们将探讨如何在 Spring Data JPA 中使用这些属性。

    最后，我们将讨论使用 Hibernate 原生 API 生成 DDL 的替代方法。

2. JPA 模式生成

    JPA 2.1 引入了数据库模式生成标准。因此，从该版本开始，我们可以通过一组预定义的配置属性来控制如何生成和导出数据库模式。

    1. 脚本操作

        为了控制我们将生成哪些 DDL 命令，JPA 引入了脚本动作配置选项：

        `javax.persistence.schema-generation.scripts.action`

        我们可以从四个不同的选项中进行选择：

        - none - 不生成任何 DDL 命令
        - create - 只生成数据库创建命令
        - drop - 仅生成数据库 drop 命令
        - drop-and-create - 生成数据库删除命令和创建命令

    2. 脚本目标

        对于每个指定的脚本操作，我们都需要定义相应的目标配置：

        ```properties
        jakarta.persistence.schema-generation.scripts.create-target
        jakarta.persistence.schema-generation.scripts.drop-target
        ```

        本质上，脚本目标定义了包含模式创建或下拉命令的文件位置。因此，举例来说，如果我们选择 "drop-and-create" 作为脚本操作，就需要指定两个目标。

    3. 模式源

        最后，为了从实体模型中生成模式 DDL 命令，我们应该在选择元数据选项时包含模式源配置：

        ```properties
        jakarta.persistence.schema-generation.create-source=metadata
        jakarta.persistence.schema-generation.drop-source=metadata
        ```

        在下一节中，我们将了解如何使用 Spring Data JPA 利用标准 JPA 属性自动生成数据库模式。

3. 使用 Spring Data JPA 生成模式

    1. 模型

        假设我们正在实现一个用户账户系统，其中有一个名为 "账户" 的[实体](https://www.baeldung.com/jpa-entities)：

        main/.jpa.schemageneration.model/Account.java

        每个账户可以有多个账户设置，因此我们将使用[一对多](https://www.baeldung.com/hibernate-one-to-many)的映射：

        main/.jpa.schemageneration.model/AccountSetting.java

    2. Spring Data JPA 配置

        要生成数据库模式，我们需要将模式生成属性传递给正在使用的持久化提供程序。为此，我们将在配置文件中设置以 spring.jpa.properties 为前缀的本地 JPA 属性：

        ```properties
        spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create
        spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=create.sql
        spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-source=metadata
        ```

        然后，Spring Data JPA 会在创建 EntityManagerFactory Bean 时将这些属性传递给持久化提供程序。

    3. create.sql 文件

        因此，在应用程序启动时，上述配置将根据实体映射元数据生成数据库创建命令。此外，DDL 命令将导出到 create.sql 文件中，该文件创建在我们的主项目文件夹中：

        ```sql
        create table account_settings (
            id bigint not null,
            name varchar(255) not null,
            value varchar(255) not null,
            account_id bigint not null,
            primary key (id)
        )

        create table accounts (
            id bigint not null,
            email_address varchar(255),
            name varchar(100) not null,
            primary key (id)
        )

        alter table account_settings
        add constraint FK54uo82jnot7ye32pyc8dcj2eh
        foreign key (account_id)
        references accounts (id)
        ```

4. 使用 Hibernate API 生成模式

    如果使用的是 Hibernate，我们可以使用其本地 API SchemaExport 来生成模式 DDL 命令。同样，Hibernate API 也会使用我们的应用程序实体模型来生成和导出数据库模式。

    让我们先下载依赖项：

    ```xml
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-ant</artifactId>
        <version>6.4.2.Final</version>
    </dependency>
    ```

    有了 Hibernate 的 SchemaExport，我们就可以明确地使用 drop、createOnly 和 create 方法：

    ```java
    MetadataSources metadataSources = new MetadataSources(serviceRegistry);
    metadataSources.addAnnotatedClass(Account.class);
    metadataSources.addAnnotatedClass(AccountSettings.class);
    Metadata metadata = metadataSources.buildMetadata();

    SchemaExport schemaExport = new SchemaExport();
    schemaExport.setFormat(true);
    schemaExport.setOutputFile("create.sql");
    schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
    ```

    运行此代码后，我们的数据库创建命令将导出到主项目文件夹中的 create.sql 文件中。

5. 模式生成选项

    尽管模式生成可以节省我们的开发时间，但我们只应在基本情况下使用它。

    例如，我们可以用它来快速启动开发或测试数据库。

    相反，对于数据库迁移等更复杂的情况，我们应该使用更精细的工具，如 [Liquibase](https://www.baeldung.com/liquibase-refactor-schema-of-java-app) 或 [Flyway](https://www.baeldung.com/database-migrations-with-flyway)。

6. 总结

    在本文中，我们学习了如何在 JPA 模式生成属性的帮助下生成和导出数据库模式。然后，我们讨论了如何使用 Hibernate 的原生 API SchemaExport 实现同样的结果。
