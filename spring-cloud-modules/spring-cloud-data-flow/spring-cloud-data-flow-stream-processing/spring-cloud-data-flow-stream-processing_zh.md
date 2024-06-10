# [使用 Spring Cloud Data Flow 开始使用流处理](https://www.baeldung.com/spring-cloud-data-flow-stream-processing)

1. 简介

    Spring Cloud Data Flow 是一种用于可组合数据微服务的云原生编程和运行模型。

    利用 [Spring Cloud Data Flow](https://cloud.spring.io/spring-cloud-dataflow/)，开发人员可以为数据摄取、实时分析和数据导入/导出等常见用例创建和协调数据管道。

    这些数据管道有两种类型，即流式数据管道和批量数据管道。

    在第一种情况下，通过消息中间件消费或生成无限制的数据量。在第二种情况下，短期任务处理有限的数据集，然后终止。

    本文将重点讨论流式处理。

2. 架构概述

    这类架构的关键组件包括应用程序、数据流服务器、Skipper 服务器和目标运行时。

    除了这些关键组件外，架构中通常还有数据流Shell（Data Flow Shell）和消息代理（Message Broker）。

    让我们来详细了解一下所有这些组件。

    1. 应用程序

        通常，流式数据管道包括从外部系统消费事件、数据处理和聚合持久化。在 Spring Cloud 术语中，这些阶段通常被称为源（Source）、处理器（Processor）和汇（Sink）：

        - 源：是消费事件的应用程序
        - 处理器：从源消耗数据，对数据进行处理，并将处理后的数据发送给管道中的下一个应用程序
        - 汇：从源或处理器消耗数据，并将数据写入所需的持久层

        这些应用程序可以通过两种方式打包：

        - 托管在 maven 资源库、文件、http 或任何其他 Spring 资源实现中的 Spring Boot uber-jar（本文将使用此方法）
        - Docker

        Spring Cloud Data Flow 团队已经为常见用例（如 jdbc、hdfs、http、路由器）提供了许多源、处理器和汇应用程序，并已准备就绪。

    2. 运行时

        这些应用程序的运行还需要运行时。支持的运行时有

        - Cloud Foundry
        - Kubernetes
        - 用于开发的本地服务器（本文将使用该服务器）

    3. 数据流服务器

        数据流服务器是负责将应用程序部署到运行时的组件。每个目标运行时都有一个 Data Flow Server 可执行 jar。

        数据流服务器负责解释：

        - 流 DSL，用于描述通过多个应用程序的数据逻辑流。
        - 描述应用程序与运行时映射关系的部署清单。

    4. Skipper 服务器

        Skipper 服务器负责

        - 将数据流部署到一个或多个平台。
        - 使用基于状态机的蓝绿更新策略，在一个或多个平台上升级和回滚数据流。
        - 存储每个数据流清单文件的历史记录。

    5. 数据流Shell

        数据流Shell是数据流服务器的客户端。通过 Shell，我们可以执行与服务器交互所需的 DSL 命令。

        例如，描述从 http 源到 jdbc 汇Shell DSL 可以写成 “http | jdbc”。DSL 中的这些名称会在数据流服务器（Data Flow Server）上注册，并映射到可托管在 Maven 或 Docker 资源库中的应用程序构件上。

        Spring 还提供了一个名为 Flo 的图形界面，用于创建和监控流数据管道。不过，其使用不在本文讨论范围之内。

    6. 消息代理

        正如我们在上一节的示例中看到的，我们在数据流的定义中使用了管道符号。管道符号代表两个应用程序通过消息中间件进行通信。

        这意味着我们需要在目标环境中运行一个消息代理。

        支持的两个消息中间件代理有

        - Apache Kafka
        - RabbitMQ

        现在，我们对架构组件有了一个大致的了解，是时候构建我们的第一个流处理管道了。

3. 安装消息代理

    正如我们所看到的，管道中的应用程序需要一个消息中间件来进行通信。本文将使用 RabbitMQ。

    有关安装的全部细节，您可以按照官方网站上的说明进行操作。

4. 本地数据流服务器和 Skipper 服务器

    使用以下命令下载 Spring Cloud Data Flow Server 和 Spring Cloud Skipper Server：

    ```shell
    wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dataflow-server/2.11.2/spring-cloud-dataflow-server-2.11.2.jar
    
    wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-skipper-server/2.11.2/spring-cloud-skipper-server-2.11.2.jar
    ```

    现在，您需要启动组成服务器的应用程序：

    Skipper ：

    在下载 Skipper 的目录中，使用 java -jar 运行服务器，如下所示：

    `java -jar spring-cloud-skipper-server-2.11.2.jar`

    应用程序将在 7577 端口启动。

    Dataflow

    在另一个终端窗口和下载 Data Flow 的目录中，使用 java -jar 运行服务器，如下所示：

    `java -jar spring-cloud-dataflow-server-2.11.2.jar`

    应用程序将在 9393 端口启动。

5. 数据流外壳

    使用以下命令下载 Spring Cloud Data Shell：

    `wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dataflow-shell/2.11.2/spring-cloud-dataflow-shell-2.11.2.jar`

    现在使用以下命令启动 Spring Cloud Data Flow Shell：

    `java -jar spring-cloud-dataflow-shell-2.11.2.jar`

    shell 运行后，我们可以在提示符中键入 help 命令，查看可执行命令的完整列表。

6. 源应用程序

    现在，我们将在 Initializr 上创建一个简单的应用程序，并添加名为 Spring-cloud-starter-stream-rabbit 的 Stream Rabbit 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    </dependency>
    ```

    然后，我们将在 Spring Boot 主类中添加 @EnableBinding(Source.class) 注解：

    ```java
    @EnableBinding(Source.class)
    @SpringBootApplication
    public class SpringDataFlowTimeSourceApplication {
        
        public static void main(String[] args) {
            SpringApplication.run(
            SpringDataFlowTimeSourceApplication.class, args);
        }
    }
    ```

    现在，我们需要定义必须处理的数据源。该数据源可以是任何可能无穷无尽的工作负载（物联网传感器数据、全天候事件处理、在线交易数据摄取）。

    在我们的示例应用中，我们使用 Poller 每 10 秒产生一个事件（简单来说就是一个新的时间戳）。

    @InboundChannelAdapter 注解将消息发送到源的输出通道，并使用返回值作为消息的有效载荷：

    ```java
    @Bean
    @InboundChannelAdapter(
    value = Source.OUTPUT, 
    poller = @Poller(fixedDelay = "10000", maxMessagesPerPoll = "1")
    )
    public MessageSource<Long> timeMessageSource() {
        return () -> MessageBuilder.withPayload(new Date().getTime()).build();
    }
    ```

    我们的数据源已准备就绪。

7. 处理器应用程序

    接下来，我们将创建一个应用程序并添加 Stream Rabbit 依赖关系。

    然后，我们将在 Spring Boot 主类中添加 @EnableBinding(Processor.class) 注解：

    ```java
    @EnableBinding(Processor.class)
    @SpringBootApplication
    public class SpringDataFlowTimeProcessorApplication {

        public static void main(String[] args) {
            SpringApplication.run(
            SpringDataFlowTimeProcessorApplication.class, args);
        }
    }
    ```

    接下来，我们需要定义一个方法来处理来自源应用程序的数据。

    要定义转换器，我们需要使用 @Transformer 注解来注解该方法：

    ```java
    @Transformer(inputChannel = Processor.INPUT, 
    outputChannel = Processor.OUTPUT)
    public Object transform(Long timestamp) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:yy");
        String date = dateFormat.format(timestamp);
        return date;
    }
    ```

    它将 “input” 通道中的时间戳转换为格式化的日期，并将其发送到 “output” 通道。

8. 水槽应用程序

    最后一个要创建的应用程序是 Sink 应用程序。

    再次进入 Spring Initializr，选择一个组和一个工件名称。下载项目后，添加 Stream Rabbit 依赖关系。

    然后在 Spring Boot 主类中添加 @EnableBinding(Sink.class) 注解：

    ```java
    @EnableBinding(Sink.class)
    @SpringBootApplication
    public class SpringDataFlowLoggingSinkApplication {

        public static void main(String[] args) {
            SpringApplication.run(
                SpringDataFlowLoggingSinkApplication.class, args);
        }
    }
    ```

    现在，我们需要一个方法来拦截来自处理器应用程序的消息。

    为此，我们需要在方法中添加 @StreamListener(Sink.INPUT) 注解：

    ```java
    @StreamListener(Sink.INPUT)
    public void loggerSink(String date) {
        logger.info("Received: " + date);
    }
    ```

    该方法只是将时间戳转换成格式化的日期打印到日志文件中。

9. 注册流应用程序

    Spring Cloud Data Flow Shell 允许我们使用应用程序注册命令在应用程序注册表中注册流应用程序。

    我们必须提供一个唯一的名称、应用程序类型和可解析为应用程序工件的 URI。对于类型，可指定“source”、“processor”或“sink”。

    使用 maven 方案提供 URI 时，格式应符合以下要求：

    `maven://<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>`

    要注册之前创建的 Source、Processor 和 Sink 应用程序，请转到 Spring Cloud Data Flow Shell 并在提示符下发出以下命令：

    ```shell
    app register --name time-source --type source --uri file://local machine path to/time-source-0.0.1-SNAPSHOT.jar

    app register --name time-processor --type processor --uri file://local machine path to/time-processor-0.0.1-SNAPSHOT.jar

    app register --name log-sink --type sink --uri file://local machine path to/log-sink-0.0.1-SNAPSHOT.jar
    ```

10. 创建和部署流

    要创建新的流定义，请访问 Spring Cloud Data Flow Shell 并执行以下 shell 命令：

    `stream create --name time-to-log --definition 'time-source | time-processor | log-sink'`

    这将根据 DSL 表达式 “time-source | time-processor | log-sink ”定义一个名为 time-to-log 的流。

    然后执行以下 shell 命令来部署流：

    `stream deploy --name time-to-log`

    数据流服务器会将 time-source、time-processor 和 log-sink 解析为 maven 坐标，并使用这些坐标启动数据流的 time-source、time-processor 和 log-sink 应用程序。

    如果数据流部署正确，你将在数据流服务器日志中看到模块已启动并绑定在一起。

11. 查看结果

    在本示例中，源以消息形式每秒发送当前时间戳，处理器对其进行格式化，日志汇使用日志框架输出格式化后的时间戳。

    日志文件位于数据流服务器日志输出中显示的目录内，如上图所示。要查看结果，我们可以尾随日志。

12. 结论

    本文展示了如何使用 Spring Cloud Data Flow 构建用于流处理的数据管道。

    此外，我们还了解了源程序、处理器和汇应用程序在流中的作用，以及如何使用 Data Flow Shell 将此模块插入和绑定到数据流服务器中。
