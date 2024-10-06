# [从 Spring Boot 显示 Hibernate/JPA SQL 语句](https://www.baeldung.com/sql-logging-spring-boot)

1. 概述

    Spring JDBC 和 JPA 提供了对本地 JDBC API 的抽象，使开发人员无需使用本地 SQL 查询。不过，我们经常需要查看这些自动生成的 SQL 查询及其执行顺序，以便进行调试。

    在本快速教程中，我们将了解在 Spring Boot 中记录这些 SQL 查询的不同方法。

2. 记录 JPA 查询

    1. 转到标准输出

        将查询转储到标准输出的最简单方法是在 application.properties 中添加以下内容：

        `spring.jpa.show-sql=true`

        要美化或漂亮打印 SQL，我们可以添加：

        `spring.jpa.properties.hibernate.format_sql=true`

        通过上述配置，日志将被打印出来：

        ```log
        2024-03-26T23:30:42.680-04:00 DEBUG 9477 --- [main] org.hibernate.SQL: 
            select
                c1_0.id,
                c1_0.budget,
                c1_0.end_date,
                c1_0.name,
                c1_0.start_date 
            from
                campaign c1_0 
            where
                c1_0.start_date between ? and ?
        ```

        虽然这种方法非常简单，但并不推荐使用，因为它会直接将所有内容卸载到标准输出，而不会对日志框架进行任何优化。

        此外，它不会记录准备语句的参数。

    2. 通过日志记录器

        现在让我们看看如何通过在属性文件中配置日志记录器来记录 SQL 语句：

        ```log
        logging.level.org.hibernate.SQL=DEBUG
        logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
        ```

        第一行记录 SQL 查询，第二行记录准备语句参数。

        在此配置中，pretty-print 属性也将起作用。

        通过设置这些属性，日志将被发送到配置的 appender。默认情况下，Spring Boot 使用日志回溯（logback）和标准的输出附加器。

        如果我们想记录带有绑定参数的查询，可以在 application.properties 文件中添加一个属性来实现：

        `logging.level.org.hibernate.orm.jdbc.bind=TRACE`

        上述属性将 JDBC 绑定的日志记录级别设置为 TRACE，这样就能记录绑定参数的详细信息：

        ```log
        org.hibernate.SQL : select c1_0.id,c1_0.budget,c1_0.end_date,c1_0.name,c1_0.start_date from campaign c1_0 where c1_0.start_date between ? and ?
        org.hibernate.orm.jdbc.bind : binding parameter [1] as [DATE] - [2024-04-26]
        org.hibernate.orm.jdbc.bind : binding parameter [2] as [DATE] - [2024-04-05]
        ```

3. 记录 JdbcTemplate 查询

    要在使用 JdbcTemplate 时配置语句日志，我们需要两个附加属性：

    ```properties
    logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG
    logging.level.org.springframework.jdbc.core.StatementCreatorUtils=TRACE
    ```

    与 JPA 日志配置类似，第一行用于记录语句，第二行用于记录准备语句的参数。使用上述配置后，SQL 日志将与绑定参数一起打印：

    ```log
    2024-03-26T23:45:44.505-04:00 DEBUG 18067 --- [main] o.s.jdbc.core.JdbcTemplate: Executing prepared SQL statement [SELECT id FROM CAMPAIGN WHERE name = ?]
    2024-03-26T23:45:44.513-04:00 TRACE 18067 --- [main] o.s.jdbc.core.StatementCreatorUtils: Setting SQL statement parameter value: column index 1, parameter value [sdfse1], value class [java.lang.String], SQL type unknown
    ```

4. 记录所有类型的查询

    使用拦截器是记录各种 SQL 查询的最佳方法。在这种方法中，我们可以拦截 JDBC 调用，对其进行格式化，然后以自定义格式记录 SQL 查询。

    数据源代理库（[datasource-proxy library](https://github.com/jdbc-observations/datasource-proxy)）是拦截 SQL 查询并记录其日志的流行框架之一。我们需要在 pom.xml 文件中添加它的依赖关系：

    ```xml
    <dependency>
        <groupId>com.github.gavlyukovskiy</groupId>
        <artifactId>datasource-proxy-spring-boot-starter</artifactId>
        <version>1.9.1</version>
    </dependency>
    ```

    此外，我们还需要更新属性，以启用数据源代理的日志记录：

    `logging.level.net.ttddyy.dsproxy.listener=debug`

    现在，有了这样的设置，它就会打印出漂亮的日志，其中包含查询和参数等详细信息：

    ```log
    Name:dataSource, Connection:15, Time:1, Success:True
    Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
    Query:["select c1_0.id,c1_0.budget,c1_0.end_date,c1_0.name,c1_0.start_date from campaign c1_0 where c1_0.start_date between ? and ?"]
    Params:[(2024-04-26,2024-04-05)]
    ```

    需要注意的一个要点是，拦截器方法会记录来自 JPA、JPQL 和准备语句的所有查询。因此，这是记录带有绑定参数的 SQL 查询的最佳方法。

5. 它是如何工作的？

    生成SQL语句和设置参数的Spring/Hibernate类已经包含了记录它们的代码。

    不过，这些日志语句的级别分别设置为 DEBUG 和 TRACE，低于 Spring Boot 的默认级别 - INFO。

    通过添加这些属性，我们只需将这些日志记录器设置为所需的级别。

6. 总结

    在这篇短文中，我们了解了在 Spring Boot 中记录 SQL 查询的几种方法。我们还了解了 SQL 查询的日志绑定参数。最后，我们讨论了为什么拦截器方法是记录 SQL 查询和绑定参数的最佳方法。

    如果我们选择[配置多个附加器](https://logback.qos.ch/manual/appenders.html)，还可以将 SQL 语句和其他日志语句分隔到不同的日志文件中，以保持整洁。
