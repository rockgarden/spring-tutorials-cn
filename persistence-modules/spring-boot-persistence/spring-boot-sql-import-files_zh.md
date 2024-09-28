# [使用多个 SQL 导入文件的 Spring Boot](https://www.baeldung.com/spring-boot-sql-import-files)

1. 概述

    Spring Boot 允许我们将样本数据导入数据库--主要是为集成测试准备数据。开箱即用有两种可能。我们可以使用 import.sql（支持 Hibernate）或 data.sql（支持 Spring JDBC）文件加载数据。

    不过，有时我们希望将一个大的 SQL 文件拆分成几个小文件，例如，为了提高可读性或在模块之间共享一些带有初始数据的文件。

    在本教程中，我们将展示如何同时使用 Hibernate 和 Spring JDBC。

2. 支持 Hibernate

    我们可以通过 Spring.jpa.properties.hibernate.hbm2ddl.import_files 属性定义要加载的包含示例数据的文件。该属性可在测试资源文件夹内的 application.properties 文件中设置。

    这是在我们想为 JUnit 测试加载样本数据的情况下。该值必须是以逗号分隔的要导入的文件列表：

    `spring.jpa.properties.hibernate.hbm2ddl.import_files=import_active_users.sql,import_inactive_users.sql`

    此配置将从两个文件中加载示例数据：import_active_users.sql 和 import_inactive_users.sql。这里需要提及的是，我们必须使用前缀 spring.jpa.properties 将值（JPA 配置）传递给 EntityManagerFactory。

    接下来，我们将展示如何利用 Spring JDBC 支持来实现这一点。

3. Spring JDBC 支持

    初始数据的配置和 Spring JDBC 支持与 Hibernate 非常相似。我们必须使用 spring.sql.init.data-locations 属性：

    `spring.sql.init.data-locations=import_active_users.sql,import_inactive_users.sql`

    设置上述值后，结果与 Hibernate 支持的结果相同。不过，这种解决方案的一大优势是可以使用 Ant 风格模式定义值：

    `spring.sql.init.data-locations=import_*_users.sql`

    上述值告诉 Spring 搜索名称与 import_*_users.sql 模式匹配的所有文件，并导入其中的数据。

    此属性在 Spring Boot 2.5.0 中引入；在 Spring Boot 的早期版本中，我们需要使用 spring.datasource.data 属性。

4. 总结

    在这篇短文中，我们展示了如何配置 Spring Boot 应用程序从自定义 SQL 文件加载初始数据。

    最后，我们展示了两种可能性--Hibernate 和 Spring JDBC。它们都能很好地工作，至于选择哪一种，则取决于开发人员。
