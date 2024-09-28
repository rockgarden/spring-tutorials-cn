# [使用 Spring Boot 配置 Hikari 连接池](https://www.baeldung.com/spring-boot-hikari)

1. 概述

    Hikari 是一种提供连接池机制的 JDBC 数据源实现。与其他实现相比，它具有轻量级和更[好的性能](https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#checkered_flag-jmh-benchmarks)。

    本快速教程将介绍如何配置 Spring Boot 3 应用程序以使用 Hikari DataSource。

2. 使用 Spring Boot 3.x 配置 Hikari

    如[参考手册](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.datasource.connection-pool)所述，Hikari 是 Spring Boot 3 中默认的数据源实现。

    Spring-boot-starter-data-jpa 和 spring-boot-starter-jdbc 中自动包含了对 Hikari 的依赖。因此，如果我们想在基于 Spring Boot 3.x 的应用程序中使用 Hikari，就不需要做任何事情。

    但是，如果我们想使用最新版本，就需要在 pom.xml 中明确添加 Hikari 依赖关系：

    ```xml
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.1.0</version>
    </dependency>
    ```

3. 调整 Hikari 配置参数

    与其他数据源实现相比，Hikari 的优势之一在于它提供了大量的配置参数。

    我们可以使用前缀 spring.datasource.hikari 并附加 Hikari 参数的名称来指定这些参数的值：

    ```properties
    spring.datasource.hikari.connectionTimeout=30000
    spring.datasource.hikari.idleTimeout=600000
    spring.datasource.hikari.maxLifetime=1800000
    ...
    ```

    在上述配置中，我们将连接超时设置为 30,000 毫秒，空闲超时设置为 600,000 毫秒，最大生命周期设置为 1,800,000 毫秒。这些参数值可根据我们应用的具体要求进行调整。

    [Hikari GitHub 网站](https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby)和 [Spring 文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#application-properties.data.spring.datasource.hikari)中提供了所有可用 Hikari 参数的完整列表和详细说明。

4. 使用 JMX 监控 Hikari

    Hikari 支持通过 [JMX](https://www.baeldung.com/java-management-extensions)（Java 管理扩展）监控其连接池。JMX 是一种 Java 技术，可在运行时管理和监控应用程序。通过为 Hikari 启用 JMX，我们可以深入了解连接池的健康状况和性能。

    要为 Hikari 启用 JMX，我们需要在 application.properties 或 application.yml 文件中添加以下配置属性：

    `spring.datasource.hikari.registerMbeans=true`

    有了这项配置，Hikari 就能通过 JMX 公开其管理豆。可以使用 JMX 工具和库访问这些 Bean，以监控和分析连接池。目前有几种流行的 JMX 客户端，如 JConsole、VisualVM 和带有 JMX 输出程序的 Prometheus。

    通过连接到托管应用程序的 Java 进程并使用 JMX 客户端，我们可以访问 Hikari 提供的各种指标和属性。其中一些关键指标包括

    - 连接数：当前活动连接数
    - 池利用率：使用中的连接百分比
    - 连接获取时间：从池中获取一个连接所需的时间
    - 连接创建时间：创建一个新连接所需的时间
    - 连接超时计数：超时的连接获取尝试次数

    这些指标可以帮助我们了解连接池的行为，并识别任何潜在的性能瓶颈或问题。我们可以设置监控和警报系统来跟踪这些指标，并在必要时采取适当措施。

5. 总结

    在本文中，我们介绍了如何利用 Spring Boot 的自动配置功能，为 Spring Boot 3.x 应用程序配置 Hikari DataSource。此外，我们还强调了启用 Hikari JMX 监控和管理连接池的重要性，这将为我们提供有价值的见解并控制其行为。
