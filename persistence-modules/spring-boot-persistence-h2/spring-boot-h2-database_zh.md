# [使用 H2 数据库的 Spring Boot](https://www.baeldung.com/spring-boot-h2-database)

1. 概述

    在本教程中，我们将探讨如何在 Spring Boot 中使用 H2。与其他数据库一样，Spring Boot 生态系统对 H2 数据库提供了全面的内在支持。

2. 依赖关系

    让我们从 h2 和 spring-boot-starter-data-jpa 依赖开始：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    ```

3. 数据库配置

    默认情况下，Spring Boot 会配置应用程序使用用户名 sa 和空密码连接到内存存储。

    不过，我们可以通过在 application.properties 文件中添加以下属性来更改这些参数：

    ```properties
    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=password
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    ```

    或者，我们也可以在 application.yaml 文件中添加相应的属性，使用 YAML 来配置应用程序的数据库：

    ```yaml
    spring:
    datasource:
        url: jdbc:h2:mem:mydb
        username: sa
        password: password
        driverClassName: org.h2.Driver
    jpa:
        database-platform: org.hibernate.dialect.H2Dialect
    ```

    根据设计，内存数据库是易失性的，会导致应用程序重启后数据丢失。

    我们可以通过使用基于文件的存储来改变这种行为。为此，我们需要更新 spring.datasource.url 属性：

    `spring.datasource.url=jdbc:h2:file:/data/demo`

    同样，在 application.yaml 中，我们可以为基于文件的存储添加相同的属性：

    ```yaml
    spring:
    datasource:
        url: jdbc:h2:file:/data/demo
    ```

    数据库也可以在[其他模式下运行](http://www.h2database.com/html/features.html#connection_modes)。

4. 数据库操作

    在 Spring Boot 中使用 H2 执行 [CRUD](https://www.baeldung.com/cs/crud-operations) 操作与使用其他 SQL 数据库相同。

    1. 数据源初始化

        我们可以使用基本的 SQL 脚本来初始化数据库。为了演示这一点，让我们在 src/main/resources 目录下添加一个 data.sql 文件：

        ```sql
        INSERT INTO countries (id, name) VALUES (1, 'USA');
        INSERT INTO countries (id, name) VALUES (2, 'France');
        INSERT INTO countries (id, name) VALUES (3, 'Brazil');
        INSERT INTO countries (id, name) VALUES (4, 'Italy');
        INSERT INTO countries (id, name) VALUES (5, 'Canada');
        ```

        在此，脚本将使用一些示例数据填充模式中的国家表。

        Spring Boot 会自动获取该文件，并在嵌入式内存数据库（如我们配置的 H2 实例）中运行。这是为测试或初始化目的提供数据库种子的好方法。

        我们可以将 spring.sql.init.mode 属性设置为从不，从而禁用这一默认行为。此外，还可以配置[多个 SQL 文件](https://www.baeldung.com/spring-boot-sql-import-files#spring-jdbc-support)来加载初始数据。

        我们关于[加载初始数据](https://www.baeldung.com/spring-boot-data-sql-and-schema-sql)的文章对此有更详细的介绍。

    2. Hibernate 和 data.sql

        默认情况下，data.sql 脚本在 Hibernate 初始化之前执行。这使基于脚本的初始化与其他数据库迁移工具（如 [Flyway](https://www.baeldung.com/database-migrations-with-flyway) 和 [Liquibase](https://www.baeldung.com/liquibase-refactor-schema-of-java-app) ）保持一致。由于我们每次都要重新创建由 Hibernate 生成的模式，因此需要设置一个额外的属性：

        `spring.jpa.defer-datasource-initialization=true`

        这将修改 Spring Boot 的默认行为，并在 Hibernate 生成模式后填充数据。此外，我们还可以使用 schema.sql 脚本，在使用 data.sql 填充数据之前，在 Hibernate 生成的模式基础上进行构建。不过，我们不建议混合使用不同的模式生成机制。

5. 访问 H2 控制台

    H2 数据库有一个嵌入式 GUI 控制台，用于浏览数据库内容和运行 SQL 查询。默认情况下，Spring 未启用 H2 控制台。

    要启用它，我们需要在 application.properties 中添加以下属性：

    `spring.h2.console.enabled=true`

    如果使用 YAML 配置，则需要在 application.yaml 中添加该属性：

    ```yaml
    spring:
    h2:
        console.enabled: true
    ```

    然后，在启动应用程序后，我们可以导航到 <http://localhost:8080/h2-console> ，这时会出现一个登录页面。

    在登录页面，我们将提供与 application.properties 中相同的凭据。

    连接后，我们将看到一个综合网页，页面左侧列出了所有表格，还有一个用于运行 SQL 查询的文本框。

    网页控制台具有自动完成功能，可提示 SQL 关键字。控制台的轻量级特性使其在直观检查数据库或直接执行原始 SQL 时非常方便。

    此外，我们还可以在项目的 application.properties 中指定以下属性，并在其中指定所需的值，从而进一步配置控制台：

    ```properties
    spring.h2.console.path=/h2-console
    spring.h2.console.settings.trace=false
    spring.h2.console.settings.web-allow-others=false
    ```

    同样，在使用 YAML 配置时，我们可以添加上述属性：

    ```yaml
    spring:
    h2:
        console:
        path: /h2-console
        settings.trace: false
        settings.web-allow-others: false
    ```

    在上面的代码段中，我们将控制台路径设置为 /h2-console，这是相对于我们正在运行的应用程序的地址和端口而言的。因此，如果我们的应用程序在 <http://localhost:9001> 上运行，控制台将在 <http://localhost:9001/h2-console> 上可用。

    此外，我们将 spring.h2.console.settings.trace 设为 false，以防止跟踪输出，还可以将 spring.h2.console.settings.web-allow-others 设为 false，禁止远程访问。

6. H2 数据库 URL 选项

    让我们探索一些 URL 选项，进一步自定义 H2 数据库：

    - db_close_delay = -1： 只要 Java 虚拟机（JVM）在运行，该选项就能确保数据库保持打开状态。它可以防止数据库在最后一个连接关闭时自动关闭。默认情况下，数据库会在最后一个连接终止时关闭。默认值为零。不过，必须使用 shutdown 命令关闭数据库，以避免潜在的内存泄漏。
    - DB_CLOSE_ON_EXIT = FALSE：默认情况下，H2 会在 JVM 关闭时关闭数据库。将此选项设为 FALSE，即使在 JVM 关闭后，数据库也会保持打开状态。在数据库需要为关闭后的进程（如记录关闭活动）保持开放的情况下，这可能很有用。
    - AUTO_RECONNECT=TRUE：这将使数据库在连接丢失时自动重新连接。默认值为 FALSE。在网络问题可能导致断开连接的环境中，启用该选项可能会有所帮助。
    - MODE=PostgreSQL： 该选项将 H2 数据库设置为模拟 PostgreSQL 数据库的行为。它为 MySQL、ORACLE 等不同数据库系统提供了兼容模式。

    下面是一个包含部分选项的 H2 数据库 URL 示例：

    `spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;MODE=PostgreSQL;`

    此 URL 配置 H2 数据库，使其在 JVM 运行时保持打开，在 JVM 关闭后保持打开，在连接丢失时自动重新连接，并以 PostgreSQL 兼容模式运行。

7. 结论

    H2 数据库与 Spring Boot 完全兼容。我们已经了解了如何配置它以及如何使用 H2 控制台管理运行中的数据库。
