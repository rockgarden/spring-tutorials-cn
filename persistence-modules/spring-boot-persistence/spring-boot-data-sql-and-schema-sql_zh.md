# [用Spring Boot加载初始数据的快速指南](https://www.baeldung.com/spring-boot-data-sql-and-schema-sql)

1. 概述

    Spring Boot使管理我们的数据库变化变得非常容易。如果我们不使用默认配置，它将在我们的包中搜索实体，并自动创建相应的表。

    但有时我们需要对数据库的更改进行更精细的控制。这时我们就可以使用Spring中的data.sql和schema.sql文件。

    [Spring Boot与H2数据库](https://www.baeldung.com/spring-boot-h2-database)

    了解如何配置以及如何使用Spring Boot中的H2数据库。

    [用Spring Data JPA生成数据库模式](https://www.baeldung.com/spring-data-jpa-generate-db-schema)

    JPA提供了一个从我们的实体模型生成DDL的标准。在这里，我们探讨如何在Spring Data中做到这一点，并与本地Hibernate进行比较。

2. data.sql文件

    让我们在此假设我们正在使用JPA，并在我们的项目中定义一个简单的国家实体：

    boot.domain/Country.java

    如果我们运行我们的应用程序，Spring Boot将为我们创建一个空表，但不会用任何东西填充它。

    一个简单的方法是创建一个名为data.sql的文件：resources/data.sql

    当我们在classpath上运行这个文件时，Spring会接收它并使用它来填充数据库。

3. schema.sql文件

    有时，我们不想依赖默认的模式创建机制。

    在这种情况下，我们可以创建一个自定义的schema.sql文件：resources/schema.sql

    Spring会接收这个文件并使用它来创建模式。

    请注意，基于脚本的初始化，即通过schema.sql和data.sql和Hibernate的初始化一起进行，会引起一些问题。

    我们可以选择禁用Hibernate自动创建模式：

    `spring.jpa.hibernate.ddl-auto=none`

    这将确保基于脚本的初始化是直接使用schema.sql和data.sql进行的。

    如果我们仍然想让Hibernate的自动模式生成与基于脚本的模式创建和数据填充结合起来，我们就必须使用

    `spring.jpa.defer-datasource-initialization=true`

    这将确保在执行Hibernate模式创建后，再读取schema.sql以了解任何额外的模式变化，并执行data.sql以填充数据库。

    另外，基于脚本的初始化默认只对嵌入式数据库执行，要想总是使用脚本初始化数据库，我们必须使用：

    `spring.sql.init.mode=always`

    关于使用SQL脚本初始化数据库，请参考Spring官方[文档](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.using-basic-sql-scripts)。

4. 使用Hibernate控制数据库的创建

    Spring提供了一个JPA特定的属性，Hibernate使用它来生成DDL：spring.jpa.hibernate.ddl-auto。

    Hibernate的标准属性值是创建、更新、创建-删除、验证和无：

    - create - Hibernate首先丢弃现有的表，然后创建新的表。
    - update - 基于映射（注解或XML）创建的对象模型与现有模式进行比较，然后Hibernate根据差异更新模式。它永远不会删除现有的表或列，即使它们不再被应用程序所需要。
    - creat-drop - 类似于create，此外，Hibernate将在所有操作完成后删除数据库；通常用于单元测试
    - validate - Hibernate只验证表和列是否存在；否则，它将抛出一个异常。
    - none - 这个值有效地关闭了DDL的生成。

    如果没有检测到模式管理器，Spring Boot内部将此参数值默认为创建-删除(create-drop)，否则在所有其他情况下都是无。

    我们必须仔细设置这个值，或者使用其他机制来初始化数据库。

5. 自定义数据库模式的创建

    默认情况下，Spring Boot会自动创建嵌入式DataSource的模式。

    如果我们需要控制或定制这种行为，我们可以使用属性[spring.sql.init.mode](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/api/org/springframework/boot/sql/init/DatabaseInitializationMode.html)。该属性取三个值之一：

    - always - 始终初始化数据库
    - embedded - 如果使用的是嵌入式数据库，总是初始化。如果没有指定该属性值，这是默认的。
    - never - 永远不初始化数据库

    值得注意的是，如果我们使用的是非嵌入式数据库，比方说MySQL或PostGreSQL，并且想要初始化其模式，我们就必须将此属性设置为always。

    这个属性是在Spring Boot 2.5.0中引入的；如果我们使用以前版本的Spring Boot，我们需要使用spring.datasource.initialization-mode。

6. @Sql

    Spring还提供了@Sql注解--一种声明性的方式来初始化和填充我们的测试模式。

    让我们看看如何使用@Sql注解来创建一个新表，并为我们的集成测试加载带有初始数据的表：

    springbootinitialload.tests/SpringBootInitialLoadIntegrationTest.java

    下面是@Sql注解的属性：

    - config - SQL脚本的本地配置。我们将在下一节详细介绍。
    - executionPhase - 我们还可以指定何时执行脚本，可以是BEFORE_TEST_METHOD，也可以是AFTER_TEST_METHOD。
    - statements - 我们可以声明要执行的内联SQL语句。
    - scripts - 我们可以声明要执行的SQL脚本文件的路径。这是对value属性的一个别名。

    @Sql注解可以在类级或方法级使用。

    我们将通过注解该方法来加载一个特定测试案例所需的额外数据：SpringBootInitialLoadIntegrationTest.testLoadDataForTestCase()

7. @SqlConfig

    我们可以通过使用@SqlConfig注解来配置我们解析和运行SQL脚本的方式。

    @SqlConfig可以在类的层面上声明，在那里它可以作为一个全局配置。或者我们可以用它来配置一个特定的@Sql注解。

    让我们看一个例子，我们指定了我们的SQL脚本的编码以及执行脚本的交易模式：

    `config = @SqlConfig(encoding = "utf-8", transactionMode = TransactionMode.ISOLATED)`

    我们再来看看@SqlConfig的各种属性：

    - blockCommentStartDelimiter - 在SQL脚本文件中确定块注释开始的分隔符
    - blockCommentEndDelimiter - 在SQL脚本文件中表示块注释结束的定界符
    - commentPrefix - 在SQL脚本文件中用于识别单行注释的前缀
    - dataSource - javax.sql.DataSource Bean的名称，脚本和语句将针对它运行。
    - encoding - SQL脚本文件的编码，默认为平台编码
    - errorMode - 当运行脚本遇到错误时将使用的模式
    - separator - 用来分隔各个语句的字符串；默认为"-"
    - transactionManager - 将用于交易的PlatformTransactionManager的bean名称
    - transactionMode - 在事务中执行脚本时将使用的模式。

8. @SqlGroup

    Java 8及以上版本允许使用重复注解。我们也可以对@Sql注解利用这一特性。对于Java 7及以下版本，有一个容器注解--@SqlGroup。

    使用@SqlGroup注解，我们将声明多个@Sql注解：

    springbootinitialload.tests/SpringBootSqlGroupAnnotationIntegrationTest.java

9. 总结

    在这篇快速文章中，我们看到了如何利用schema.sql和data.sql文件来设置初始模式并将其填充到数据中。

    我们还看了如何使用@Sql、@SqlConfig和@SqlGroup注解来为测试加载测试数据。

    请记住，这种方法更适合于基本的和简单的场景，任何高级的数据库处理都需要更高级和精炼的工具，如Liquibase或Flyway。
