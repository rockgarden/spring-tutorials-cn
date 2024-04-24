# [在 Kafka 中拆分数据流](https://www.baeldung.com/kafka-splitting-streams)

1. 简介

    在本教程中，我们将探讨如何在 Kafka 流中动态路由消息。当消息的目标主题取决于其内容时，动态路由就特别有用，它能让我们根据有效载荷中的特定条件或属性来引导消息。这种条件路由在物联网事件处理、用户活动跟踪和欺诈检测等多个领域都有实际应用。

    我们将讨论从单个 Kafka 主题消费消息并有条件地将其路由到多个目标主题的问题。主要重点是如何使用 Kafka Streams 库在 Spring Boot 应用程序中进行设置。

2. Kafka Streams 路由技术

    Kafka Streams 中的消息动态路由并不局限于一种方法，而是可以使用多种技术来实现。每种技术都有其独特的优势、挑战和对不同场景的适用性：

    - KStream 条件分支：KStream.split().branch() 方法是根据谓词隔离流的传统方法。虽然这种方法很容易实现，但它在扩展条件数量时有局限性，可能会变得不那么容易管理。
    - 使用 KafkaStreamBrancher 进行分支： 该功能出现在 Spring Kafka 2.2.4 版本中。它为在 Kafka Stream 中创建分支提供了一种更优雅、更易读的方式，消除了对 "神奇数字" 的需求，并允许更流畅的流操作链。
    - 使用 TopicNameExtractor 进行动态路由： 另一种主题路由方法是使用 TopicNameExtractor。这允许在运行时根据消息键、值甚至整个记录上下文进行更动态的主题选择。不过，这需要提前创建主题。这种方法可以对主题选择进行更细粒度的控制，并能更好地适应复杂的用例。
    - 自定义处理器： 对于需要复杂路由逻辑或多个链式操作的场景，我们可以在 Kafka Streams 拓扑中应用自定义处理器节点。这种方法最灵活，但实现起来也最复杂。

    在本文中，我们将重点介绍前三种方法的实现：KStream 条件分支、KafkaStreamBrancher 分支和 TopicNameExtractor 动态路由。

3. 设置环境

    在我们的场景中，我们有一个物联网传感器网络，向名为 iot_sensor_data 的集中式 Kafka 主题流式传输各种类型的数据，如温度、湿度和运动。每个传入消息都包含一个 JSON 对象，其中一个名为 sensorType 的字段表示传感器发送的数据类型。我们的目的是将这些消息动态路由到每种类型传感器数据的专用主题。

    首先，让我们建立一个正在运行的 Kafka 实例。我们可以通过创建 docker-compose.yml 文件，使用 Docker 和 [Docker Compose](https://www.baeldung.com/ops/docker-compose) 设置 Kafka、[Zookeeper](https://www.baeldung.com/java-zookeeper) 和 [Kafka UI](https://github.com/provectus/kafka-ui)：

    ```yml
    version: '3.8'
    services:
    zookeeper:
        image: confluentinc/cp-zookeeper:latest
        environment:
        ZOOKEEPER_CLIENT_PORT: 2181
        ZOOKEEPER_TICK_TIME: 2000
        ports:
        - 22181:2181
    kafka:
        image: confluentinc/cp-kafka:latest
        depends_on:
        - zookeeper
        ports:
        - 9092:9092
        environment:
        KAFKA_BROKER_ID: 1
        KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
        KAFKA_LISTENERS: "INTERNAL://:29092,EXTERNAL://:9092"
        KAFKA_ADVERTISED_LISTENERS: "INTERNAL://kafka:29092,EXTERNAL://localhost:9092"
        KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT"
        KAFKA_INTER_BROKER_LISTENER_NAME: "INTERNAL"
        KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    kafka_ui:
        image: provectuslabs/kafka-ui:latest
        depends_on:
        - kafka
        ports:
        - 8082:8080
        environment:
        KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
        KAFKA_CLUSTERS_0_NAME: local
        KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
    kafka-init-topics:
        image: confluentinc/cp-kafka:latest
        depends_on:
        - kafka
        command: "bash -c 'echo Waiting for Kafka to be ready... && \
                cub kafka-ready -b kafka:29092 1 30 && \
                kafka-topics --create --topic iot_sensor_data --partitions 1 --replication-factor 1 --if-not-exists --bootstrap-server kafka:29092'"
    ```

    在这里，我们设置了所有必要的环境变量和服务之间的依赖关系。此外，我们还在 kafka-init-topics 服务中使用特定命令创建了 iot_sensor_data 主题。

    现在，我们可以通过执行 docker-compose up -d 在 Docker 内运行 Kafka。

    接下来，我们必须在 pom.xml 文件中添加 Kafka Streams 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-streams</artifactId>
        <version>3.6.1</version>`
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.1.2</version>
    </dependency>
    ```

    第一个依赖项是 org.apache.kafka.kafka-streams 软件包，它提供 Kafka Streams 功能。随后的 Maven 软件包 org.springframework.kafka.spring-kafka 则为 Kafka 与 Spring Boot 的配置和集成提供了便利。

    另一个重要方面是配置 Kafka 代理的地址。这通常需要在应用程序的属性文件中指定代理的详细信息。让我们将此配置与其他属性一起添加到我们的 application.properties 文件中：

    ```porperties
    spring.kafka.bootstrap-servers=localhost:9092
    spring.kafka.streams.application-id=baeldung-streams
    spring.kafka.consumer.group-id=baeldung-group
    spring.kafka.streams.properties[default.key.serde]=org.apache.kafka.common.serialization.Serdes$StringSerde
    kafka.topics.iot=iot_sensor_data
    ```

    接下来，让我们定义一个示例数据类 IotSensorData：

    main/.spring.kafka.kafkasplitting/IotSensorData.java

    最后，我们需要为 Kafka 中类型化消息的序列化和反序列化配置 Serde：

    main/.spring.kafka.kafkasplitting/KafkaStreamsConfig.java:iotSerde()

4. 在 Kafka 流中实现动态路由

    设置好环境并安装好所需的依赖项后，让我们来关注在 Kafka Streams 中实现动态路由逻辑。

    动态消息路由是事件驱动应用程序的重要组成部分，因为它能让系统适应各种类型的数据流和条件，而无需修改代码。

    1. KStream 条件分支

        Kafka Streams 中的分支功能允许我们根据某些条件将单个数据流拆分成多个数据流。这些条件以谓词的形式提供，在每个消息通过流时对其进行评估。

        在 Kafka Streams 的最新版本中，branch() 方法已被弃用，取而代之的是更新的 split().branch() 方法，该方法旨在提高 API 的整体可用性和灵活性。尽管如此，我们仍可以同样的方式将 KStream 根据某些谓词分割成多个流。

        在此，我们定义了使用 split().branch() 方法进行动态主题路由的配置：

        ```java
        @Bean
        public KStream<String, IotSensorData> iotStream(StreamsBuilder streamsBuilder) {
        KStream<String, IotSensorData> stream = streamsBuilder.stream(iotTopicName, Consumed.with(Serdes.String(), iotSerde()));
        stream.split()
            .branch((key, value) -> "temp".equals(value.getSensorType()), Branched.withConsumer((ks) -> ks.to(iotTopicName + "_temp")))
            .branch((key, value) -> "move".equals(value.getSensorType()), Branched.withConsumer((ks) -> ks.to(iotTopicName + "_move")))
            .branch((key, value) -> "hum".equals(value.getSensorType()), Branched.withConsumer((ks) -> ks.to(iotTopicName + "_hum")))
            .noDefaultBranch();
        return stream;
        }
        ```

        在上面的示例中，我们根据 sensorType 属性将来自 iot_sensor_data 主题的初始流拆分成多个流，并相应地将它们路由到其他主题。

        如果可以根据消息内容生成目标主题名称，我们就可以在 to 方法中使用 lambda 函数来实现更动态的主题路由：

        ```java
        @Bean
        public KStream<String, IotSensorData> iotStreamDynamic(StreamsBuilder streamsBuilder) {
            KStream<String, IotSensorData> stream = streamsBuilder.stream(iotTopicName, Consumed.with(Serdes.String(), iotSerde()));
            stream.split()
            .branch((key, value) -> value.getSensorType() != null, 
                Branched.withConsumer(ks -> ks.to((key, value, recordContext) -> "%s_%s".formatted(iotTopicName, value.getSensorType()))))
            .noDefaultBranch();
            return stream;
        }
        ```

        如果能根据信息内容生成主题名称，那么这种方法就能提供更大的灵活性，根据信息内容动态路由信息。

    2. 使用 KafkaStreamBrancher 路由

        KafkaStreamBrancher 类提供了一个构建器风格的 API，允许更轻松地连锁分支条件，使代码更具可读性和可维护性。

        它的主要优点是消除了与管理分支流数组相关的复杂性，这也是原始 KStream.branch 方法的工作方式。取而代之的是，KafkaStreamBrancher 让我们可以定义每个分支，以及应该对该分支进行的操作，从而不再需要神奇的数字或复杂的索引来识别正确的分支。由于引入了 split().branch() 方法，这种方法与前面讨论的方法密切相关。

        让我们将这种方法应用到流中：

        ```java
        @Bean
        public KStream<String, IotSensorData> kStream(StreamsBuilder streamsBuilder) {
            KStream<String, IotSensorData> stream = streamsBuilder.stream(iotTopicName, Consumed.with(Serdes.String(), iotSerde()));
            new KafkaStreamBrancher<String, IotSensorData>()
            .branch((key, value) -> "temp".equals(value.getSensorType()), (ks) -> ks.to(iotTopicName + "_temp"))
            .branch((key, value) -> "move".equals(value.getSensorType()), (ks) -> ks.to(iotTopicName + "_move"))
            .branch((key, value) -> "hum".equals(value.getSensorType()), (ks) -> ks.to(iotTopicName + "_hum"))
            .defaultBranch(ks -> ks.to("%s_unknown".formatted(iotTopicName)))
            .onTopOf(stream);
            return stream;
        }
        ```

        我们应用 Fluent API 将消息路由到特定主题。 同样，通过将内容作为主题名称的一部分，我们可以使用单个 branch() 方法调用将消息路由到多个主题：

        ```java
        @Bean
        public KStream<String, IotSensorData> iotBrancherStream(StreamsBuilder streamsBuilder) {
            KStream<String, IotSensorData> stream = streamsBuilder.stream(iotTopicName, Consumed.with(Serdes.String(), iotSerde()));
            new KafkaStreamBrancher<String, IotSensorData>()
            .branch((key, value) -> value.getSensorType() != null, (ks) ->
                ks.to((key, value, recordContext) -> String.format("%s_%s", iotTopicName, value.getSensorType())))
            .defaultBranch(ks -> ks.to("%s_unknown".formatted(iotTopicName)))
            .onTopOf(stream);
            return stream;
        }
        ```

        通过为分支逻辑提供更高层次的抽象，KafkaStreamBrancher 不仅使代码更简洁，还增强了代码的可管理性，特别是对于具有复杂路由要求的应用程序。

    3. 使用 TopicNameExtractor 进行动态主题路由选择

        管理 Kafka 流中条件分支的另一种方法是使用 TopicNameExtractor，顾名思义，它可以动态提取流中每条消息的主题名称。与之前讨论过的 split().branch() 和 KafkaStreamBrancher 方法相比，这种方法在某些用例中更为直接。

        以下是在 Spring Boot 应用程序中使用 TopicNameExtractor 的配置示例：

        ```java
        @Bean
        public KStream<String, IotSensorData> kStream(StreamsBuilder streamsBuilder) {
            KStream<String, IotSensorData> stream = streamsBuilder.stream(iotTopicName, Consumed.with(Serdes.String(), iotSerde()));
            TopicNameExtractor<String, IotSensorData> sensorTopicExtractor = (key, value, recordContext) -> "%s_%s".formatted(iotTopicName, value.getSensorType());
            stream.to(sensorTopicExtractor);
            return stream;
        }
        ```

        虽然 TopicNameExtractor 方法精通将记录路由到特定主题的主要功能，但与 split().branch() 和 KafkaStreamBrancher 等其他方法相比，它有一些局限性。具体来说，TopicNameExtractor 没有提供在同一路由步骤中执行其他转换（如映射或过滤）的选项。

5. 结论

    在本文中，我们了解了使用 Kafka Streams 和 Spring Boot 进行动态主题路由的不同方法。

    我们首先探索了现代分支机制，如 split().branch() 方法和 KafkaStreamBrancher 类。此外，我们还考察了 TopicNameExtractor 提供的动态主题路由功能。

    每种技术都有其优势和挑战。例如，在处理众多条件时，split().branch() 可能会很麻烦，而 TopicNameExtractor 提供了结构化流程，但限制了某些内联数据处理。因此，掌握每种方法的细微差别对于创建有效的路由实现至关重要。
