# [使用 Spring 云数据流进行 ETL](https://www.baeldung.com/spring-cloud-data-flow-etl)

1. 概述

    Spring Cloud Data Flow 是一个云原生工具包，用于构建实时数据管道和批处理流程。Spring Cloud Data Flow 可用于一系列数据处理用例，如简单的导入/导出、ETL 处理、事件流和预测分析。

    在本教程中，我们将学习一个使用流管道进行实时提取、转换和加载（ETL）的示例，该管道从 JDBC 数据库中提取数据，将其转换为简单的 POJO 并加载到 MongoDB 中。

2. ETL 和事件流处理

    ETL（提取、转换和加载）通常是指将数据从多个数据库和系统批量加载到一个通用数据仓库的过程。在这种数据仓库中，可以在不影响系统整体性能的情况下进行大量数据分析处理。

    然而，新的趋势正在改变这种方式。ETL 在将数据传输到数据仓库和数据湖方面仍然发挥作用。

    如今，在 Spring Cloud Data Flow 的帮助下，这可以通过事件流架构中的流来完成。

3. Spring 云数据流

    借助 Spring Cloud Data Flow (SCDF)，开发人员可以创建两种类型的数据管道：

    - 使用 Spring Cloud Stream 的长期实时流应用程序
    - 使用 Spring Cloud Task 的短命批处理任务应用程序

    本文将介绍第一种，即基于 Spring Cloud Stream 的长寿命流应用程序。

    1. Spring Cloud Stream 应用程序

        SCDF 流管道由多个步骤组成，其中每个步骤都是使用 Spring Cloud Stream 微框架以 Spring Boot 风格构建的应用程序。这些应用程序由 Apache Kafka 或 RabbitMQ 等消息中间件集成。

        这些应用程序分为源、处理器和汇。与 ETL 流程相比，我们可以说源是 “extract” 部分，处理器是 “transformer” 部分，而汇是 “load” 部分。

        在某些情况下，我们可以在管道的一个或多个步骤中使用应用程序启动器。这意味着我们不需要为某个步骤实现一个新的应用程序，而是配置一个已经实现的现有应用程序启动器。

        应用程序启动器列表可在[此处](https://docs.spring.io/spring-cloud-stream-app-starters/docs/current/reference/htmlsingle/#starters)找到。

    2. Spring 云数据流服务器

        架构的最后一部分是 Spring Cloud Data Flow Server。SCDF 服务器使用 Spring Cloud Deployer 规范完成应用程序和管道流的部署。该规范通过部署到一系列现代运行时（如 Kubernetes、Apache Mesos、Yarn 和 Cloud Foundry）来支持 SCDF 的云原生特性。

        此外，我们还可以将流作为本地部署运行。

        有关 SCDF 架构的更多信息，请点击[此处](https://www.baeldung.com/spring-cloud-data-flow-stream-processing)。

4. 环境设置

    在开始之前，我们需要选择这个复杂部署的各个部分。首先要定义的是 SCDF 服务器。

    对于测试，我们将使用 SCDF Server Local 进行本地开发。对于生产部署，我们可以稍后选择云原生运行时，如 [SCDF Server Kubernetes](https://docs.vmware.com/en/VMware-Spring-Cloud-Data-Flow-for-Kubernetes/index.html)。我们可以在这里找到服务器运行时列表。

    现在，让我们检查一下运行该服务器的系统要求。

    1. 系统要求

        要运行 SCDF 服务器，我们必须定义并设置两个依赖项：

        - 消息中间件和
        - 数据库管理系统。

        对于消息传递中间件，我们将使用 RabbitMQ，并选择 PostgreSQL 作为 RDBMS 来存储我们的管道流定义。

        要运行 RabbitMQ，请在[此处](https://www.rabbitmq.com/download.html)下载最新版本，然后使用默认配置或运行以下 Docker 命令启动 RabbitMQ 实例：

        `docker run --name dataflow-rabbit -p 15672:15672 -p 5672:5672 -d rabbitmq:3-management`

        作为最后一个设置步骤，在默认端口 5432 上安装并运行 PostgreSQL RDBMS。之后，使用以下脚本创建一个 SCDF 可以存储其流定义的数据库：

        `CREATE DATABASE dataflow;`

    2. 本地 Spring 云数据流服务器

        要在本地运行 SCDF 服务器，我们可以选择使用 [docker-compose](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#getting-started-deploying-spring-cloud-dataflow-docker) 启动服务器，也可以将其作为 Java 应用程序启动。

        在此，我们将以 Java 应用程序的形式运行 SCDF Server Local。为了配置应用程序，我们必须将配置定义为 Java 应用程序参数。系统路径中需要 Java 8。

        要托管 jars 和依赖项，我们需要为 SCDF 服务器创建一个主文件夹，并将 SCDF Server Local 发行版下载到该文件夹中。您可以在此处下载最新的 SCDF Server Local 发行版。

        此外，我们还需要创建一个 lib 文件夹，并在其中放入 JDBC 驱动程序。PostgreSQL 驱动程序的最新版本可在[此处](https://jdbc.postgresql.org/download/)下载。

        最后，运行 SCDF 本地服务器：

        ```shell
        $java -Dloader.path=lib -jar spring-cloud-dataflow-server-local-1.6.3.RELEASE.jar \
            --spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/dataflow \
            --spring.datasource.username=postgres_username \
            --spring.datasource.password=postgres_password \
            --spring.datasource.driver-class-name=org.postgresql.Driver \
            --spring.rabbitmq.host=127.0.0.1 \
            --spring.rabbitmq.port=5672 \
            --spring.rabbitmq.username=guest \
            --spring.rabbitmq.password=guest
        ```

        我们可以通过查看此 URL 来检查它是否正在运行：

        <http://localhost:9393/dashboard>

    3. Spring Cloud Data Flow Shell

        SCDF Shell 是一种命令行工具，可让我们轻松组成和部署应用程序和管道。这些 Shell 命令通过 Spring Cloud Data Flow Server [REST API](https://docs.spring.io/spring-cloud-dataflow/docs/current-SNAPSHOT/reference/htmlsingle/#api-guide-overview) 运行。

        将最新版本的 jar 下载到 SCDF home 文件夹中，可在此处获取。下载完成后，运行以下命令（根据需要更新版本）：

        ```shell
        $ java -jar spring-cloud-dataflow-shell-1.6.3.RELEASE.jar
        ____                              ____ _                __
        / ___| _ __  _ __(_)_ __   __ _   / ___| | ___  _   _  __| |
        \___ \| '_ \| '__| | '_ \ / _` | | |   | |/ _ \| | | |/ _` |
        ___) | |_) | |  | | | | | (_| | | |___| | (_) | |_| | (_| |
        |____/| .__/|_|  |_|_| |_|\__, |  \____|_|\___/ \__,_|\__,_|
        ____ |_|    _          __|___/                 __________
        |  _ \  __ _| |_ __ _  |  ___| | _____      __  \ \ \ \ \ \
        | | | |/ _` | __/ _` | | |_  | |/ _ \ \ /\ / /   \ \ \ \ \ \
        | |_| | (_| | || (_| | |  _| | | (_) \ V  V /    / / / / / /
        |____/ \__,_|\__\__,_| |_|   |_|\___/ \_/\_/    /_/_/_/_/_/


        Welcome to the Spring Cloud Data Flow shell. For assistance hit TAB or type "help".
        dataflow:>
        ```

        如果最后一行显示的不是 “dataflow:>”，而是 “server-unknown:>”，则表明您没有在 localhost 上运行 SCDF 服务器。在这种情况下，请运行以下命令连接到另一台主机：

        `server-unknown:>dataflow config server http://{host}`

        现在，Shell 已连接到 SCDF 服务器，我们可以运行命令了。

        我们需要在 Shell 中做的第一件事是导入应用程序启动器。在此处查找 Spring Boot 2.0.x 中 RabbitMQ+Maven 的最新[版本](https://docs.spring.io/spring-cloud-stream-app-starters/docs/current/reference/htmlsingle/#starters)，然后运行以下命令（同样，根据需要更新版本，此处为 “Darwin-SR1”）：

        `$ dataflow:>app import --uri http://bit.ly/Darwin-SR1-stream-applications-rabbit-maven`

        要检查已安装的应用程序，请运行以下 Shell 命令：

        `$ dataflow:> app list`

        结果，我们会看到一个包含所有已安装应用程序的表格。

        此外，SCDF 还提供了一个名为 Flo 的图形界面，我们可以通过以下地址访问：<http://localhost:9393/dashboard> 。 不过，它的使用不在本文讨论范围之内。

5. 组成 ETL 管道

    现在让我们创建流管道。为此，我们将使用 JDBC Source 应用程序启动器从关系数据库中提取信息。

    此外，我们还将创建一个用于转换信息结构的自定义处理器和一个用于将数据加载到 MongoDB 的自定义汇。

    1. 提取--为提取准备关系数据库

        让我们创建一个名称为 crm 的数据库和一个名称为 customer 的表：

        ```sql
        CREATE DATABASE crm;
        CREATE TABLE customer (
            id bigint NOT NULL,
            imported boolean DEFAULT false,
            customer_name character varying(50),
            PRIMARY KEY(id)
        )
        ```

        请注意，我们使用的标志是 imported，它将存储哪条记录已被导入。如有必要，我们也可以在另一个表中存储这些信息。

        现在，让我们插入一些数据：

        `INSERT INTO customer(id, customer_name, imported) VALUES (1, 'John Doe', false);`

    2. 转换--将 JDBC 字段映射到 MongoDB 字段结构

        在转换步骤中，我们将把源表中的字段 customer_name 简单转换为新的字段名。这里还可以进行其他转换，但让我们简短地举例说明。

        为此，我们将创建一个名为 customer-transform 的新项目。最简单的方法是使用 Spring Initializr 网站创建项目。进入网站后，选择一个组和一个工件名称。我们将分别使用 com.customer 和 customer-transform。

        完成后，点击 “Generate Project” 按钮下载项目。然后，解压缩该项目并将其导入您喜欢的集成开发环境，并在 pom.xml 中添加以下依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
        </dependency>
        ```

        现在，我们可以开始编码字段名称转换了。为此，我们将创建 Customer 类作为适配器。该类将通过 setName() 方法接收 customer_name，并通过 getName 方法输出其值。

        @JsonProperty 注解将在从 JSON 反序列化到 Java 时完成转换：

        ```java
        public class Customer {

            private Long id;

            private String name;

            @JsonProperty("customer_name")
            public void setName(String name) {
                this.name = name;
            }

            @JsonProperty("name")
            public String getName() {
                return name;
            }

            // Getters and Setters
        }
        ```

        处理器需要从输入端接收数据，进行转换并将结果绑定到输出通道。让我们创建一个类来完成这项工作：

        ```java
        import org.springframework.cloud.stream.annotation.EnableBinding;
        import org.springframework.cloud.stream.messaging.Processor;
        import org.springframework.integration.annotation.Transformer;

        @EnableBinding(Processor.class)
        public class CustomerProcessorConfiguration {

            @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
            public Customer convertToPojo(Customer payload) {

                return payload;
            }
        }
        ```

        在上述代码中，我们可以看到转换是自动进行的。输入端接收 JSON 格式的数据，Jackson 使用 set 方法将其反序列化为 Customer 对象。

        输出则相反，使用 get 方法将数据序列化为 JSON。

    3. MongoDB 中的加载-下沉

        与转换步骤类似，我们将创建另一个 maven 项目，名称为 customer-mongodb-sink。再次访问 Spring Initializr，在 Group 中选择 com.customer，在 Artifact 中选择 customer-mongodb-sink。然后，在依赖关系搜索框中输入 “MongoDB ”并下载该项目。

        然后，解压缩并将其导入您最喜欢的集成开发环境。

        然后，添加与 customer-transform 项目中相同的额外依赖关系。

        现在，我们将创建另一个 Customer 类，用于在本步骤中接收输入：

        ```java
        import org.springframework.data.mongodb.core.mapping.Document;

        @Document(collection="customer")
        public class Customer {

            private Long id;
            private String name;

            // Getters and Setters
        }
        ```

        为了下沉客户，我们将创建一个监听器类，使用 CustomerRepository 保存客户实体：

        ```java
        @EnableBinding(Sink.class)
        public class CustomerListener {

            @Autowired
            private CustomerRepository repository;

            @StreamListener(Sink.INPUT)
            public void save(Customer customer) {
                repository.save(customer);
            }
        }
        ```

        在本例中，CustomerRepository 是 Spring Data 的 MongoRepository：

        ```java
        import org.springframework.data.mongodb.repository.MongoRepository;
        import org.springframework.stereotype.Repository;

        @Repository
        public interface CustomerRepository extends MongoRepository<Customer, Long> {

        }
        ```

    4. 流定义

        现在，两个自定义应用程序都可以在 SCDF Server 上注册了。为此，请使用 Maven 命令 mvn install 编译这两个项目。

        然后使用 Spring Cloud Data Flow Shell 注册它们：

        ```shell
        app register --name customer-transform --type processor --uri maven://com.customer:customer-transform:0.0.1-SNAPSHOT
        app register --name customer-mongodb-sink --type sink --uri maven://com.customer:customer-mongodb-sink:jar:0.0.1-SNAPSHOT
        ```

        最后，让我们检查应用程序是否存储在 SCDF 中，在 shell 中运行应用程序列表命令：

        `app list`

        结果，我们会在生成的表中看到这两个应用程序。

        1. 流水线领域专用语言 - DSL

            DSL 定义了应用程序之间的配置和数据流。SCDF DSL 很简单。在第一个词中，我们定义应用程序的名称，然后是配置。

            此外，该语法是受 Unix 启发的管道语法（[Pipeline syntax](https://en.wikipedia.org/wiki/Pipeline_(Unix))），使用竖条（也称为 “pipes”）连接多个应用程序：

            `http --port=8181 | log`

            这样就在 8181 端口创建了一个 HTTP 应用程序，它将接收到的任何正文有效载荷发送到日志中。

            现在，让我们看看如何创建 JDBC 源的 DSL 流定义。

        2. JDBC 源流定义

            JDBC 源的关键配置是查询和更新。查询将选择未读记录，而更新将更改标志以防止当前记录被重读。

            此外，我们还将定义 JDBC 源轮询的固定延迟时间为 30 秒，轮询最多 1000 行。最后，我们将定义连接配置，如驱动程序、用户名、密码和连接 URL：

            ```bash
            jdbc 
                --query='SELECT id, customer_name FROM public.customer WHERE imported = false'
                --update='UPDATE public.customer SET imported = true WHERE id in (:id)'
                --max-rows-per-poll=1000
                --fixed-delay=30 --time-unit=SECONDS
                --driver-class-name=org.postgresql.Driver
                --url=jdbc:postgresql://localhost:5432/crm
                --username=postgres
                --password=postgres
            ```

            更多 JDBC 源配置属性请点击[此处](https://docs.spring.io/spring-cloud-stream-app-starters/docs/Celsius.SR2/reference/htmlsingle/#spring-cloud-stream-modules-jdbc-source)。

        3. 客户 MongoDB Sink 流定义

            由于我们没有在 customer-mongodb-sink 的 application.properties 中定义连接配置，因此我们将通过 DSL 参数进行配置。

            我们的应用程序完全基于 MongoDataAutoConfiguration。您可以在这里查看其他可能的配置。基本上，我们将定义 Spring.data.mongodb.uri.Net 和 Spring.data.MongoDataAutoConfiguration：

            `customer-mongodb-sink --spring.data.mongodb.uri=mongodb://localhost/main`

        4. 创建和部署流

            首先，要创建最终的流定义，请回到 Shell 并执行以下命令（不带换行符，插入换行符是为了便于阅读）：

            ```bash
            stream create --name jdbc-to-mongodb 
            --definition "jdbc 
            --query='SELECT id, customer_name FROM public.customer WHERE imported=false' 
            --fixed-delay=30 
            --max-rows-per-poll=1000 
            --update='UPDATE customer SET imported=true WHERE id in (:id)' 
            --time-unit=SECONDS 
            --password=postgres 
            --driver-class-name=org.postgresql.Driver 
            --username=postgres 
            --url=jdbc:postgresql://localhost:5432/crm | customer-transform | customer-mongodb-sink 
            --spring.data.mongodb.uri=mongodb://localhost/main"
            ```

            该流 DSL 定义了一个名为 jdbc-to-mongodb 的流。接下来，我们将根据流的名称部署该流：

            `stream deploy --name jdbc-to-mongodb`

            最后，我们将在日志输出中看到所有可用日志的位置：

            ```log
            Logs will be in {PATH_TO_LOG}/spring-cloud-deployer/jdbc-to-mongodb/jdbc-to-mongodb.customer-mongodb-sink

            Logs will be in {PATH_TO_LOG}/spring-cloud-deployer/jdbc-to-mongodb/jdbc-to-mongodb.customer-transform

            Logs will be in {PATH_TO_LOG}/spring-cloud-deployer/jdbc-to-mongodb/jdbc-to-mongodb.jdbc
            ```

6. 结论

    在本文中，我们看到了使用 Spring Cloud Data Flow 的 ETL 数据管道的完整示例。

    最值得一提的是，我们看到了应用程序启动器的配置，使用 Spring Cloud Data Flow Shell 创建了 ETL 流水线，并为读取、转换和写入数据实施了自定义应用程序。
