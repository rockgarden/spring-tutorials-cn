# [用Flink和Kafka构建数据管道](https://www.baeldung.com/kafka-flink-data-pipeline)

1. 概述

    [Apache Flink](https://flink.apache.org/) 是一个流处理框架，可以轻松地与 Java 结合使用。Apache Kafka 是一个支持高容错性的分布式流处理系统。

    在本教程中，我们将了解如何使用这两种技术构建[数据管道](https://www.baeldung.com/cs/data-pipelines)。

2. 安装

    要安装和配置 Apache Kafka，请参考[官方指南](https://kafka.apache.org/quickstart)。安装完成后，我们可以使用以下命令创建名为 flink_input 和 flink_output 的新主题：

    ```bash
    bin/kafka-topics.sh --create \
    --zookeeper localhost:2181 \
    --replication-factor 1 --partitions 1 \
    --topic flink_output

    bin/kafka-topics.sh --create \
    --zookeeper localhost:2181 \
    --replication-factor 1 --partitions 1 \
    --topic flink_input
    ```

    在本教程中，我们将使用 Apache Kafka 的默认配置和默认端口。

3. Flink 的使用

    Apache Flink 允许使用实时流处理技术。该框架允许将多个第三方系统作为流源或流汇(source/sink)来使用。

    Flink 中有多种可用的连接器：

    - 阿帕奇卡夫卡（源/汇）
    - Apache Cassandra（汇）
    - 亚马逊 Kinesis 流（源/汇）
    - Elasticsearch （汇）
    - Hadoop FileSystem（汇）
    - RabbitMQ （源/汇）
    - Apache NiFi（源/汇）
    - Twitter 流 API（源）

    要在项目中添加 Flink，我们需要包含以下 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-core</artifactId>
        <version>1.16.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-kafka</artifactId>
        <version>1.16.1</version>
    </dependency>
    ```

    添加这些依赖项后，我们就可以在 Kafka 主题之间进行消费和生产。

4. Kafka 字符串消费者

    要使用 Flink 从 Kafka 消费数据，我们需要提供一个主题和一个 Kafka 地址。我们还应该提供一个组 ID，用来保存偏移量，这样我们就不会总是从头开始读取整个数据。

    让我们创建一个静态方法，让 FlinkKafkaConsumer 的创建变得更容易：

    main/.flink.connector/Consumer.java:createStringConsumerForTopic()

    该方法接收一个 topic、kafkaAddress 和 kafkaGroup，然后创建一个 FlinkKafkaConsumer，它将以字符串的形式从给定的 topic 中消费数据，因为我们使用了 SimpleStringSchema 来解码数据。

    类名中的数字 011 指的是 Kafka 版本。

5. Kafka 字符串生产者

    要向 Kafka 生成数据，我们需要提供想要使用的 Kafka 地址和主题。同样，我们可以创建一个静态方法，帮助我们为不同的主题创建生产者：

    ```java
    public static FlinkKafkaProducer011<String> createStringProducer(
    String topic, String kafkaAddress){
        return new FlinkKafkaProducer011<>(kafkaAddress,
        topic, new SimpleStringSchema());
    }
    ```

    这个方法只需要 topic 和 kafkaAddress 作为参数，因为当我们向 Kafka topic 生产时，不需要提供组 id。

6. 字符串流处理

    当消费者和生产者完全正常工作后，我们就可以尝试处理来自 Kafka 的数据，然后将结果保存回 Kafka。可用于流处理的函数的完整列表可以在这里找到。

    在本例中，我们将对每个 Kafka 条目中的单词进行大写处理，然后将其写回 Kafka。

    为此，我们需要创建一个自定义 MapFunction：

    main/.flink.operator/WordsCapitalizer.java

    创建函数后，我们就可以在流处理中使用它了：

    main/.flink/FlinkDataPipeline.java:capitalize()

    应用程序将从 flink_input 主题读取数据，对流执行操作，然后将结果保存到 Kafka 中的 flink_output 主题。

    我们已经了解了如何使用 Flink 和 Kafka 处理字符串。但经常需要对自定义对象执行操作。我们将在接下来的章节中了解如何做到这一点。

7. 自定义对象反序列化

    下面的类表示一条简单的消息，包含发送方和接收方的信息：

    mian/.flink.model/InputMessage.java

    之前，我们使用 SimpleStringSchema 从 Kafka 反序列化消息，但现在我们想直接将数据反序列化为自定义对象。

    为此，我们需要一个自定义的反序列化模式（DeserializationSchema）：

    main/.flink.schema/InputMessageDeserializationSchema.java

    在此，我们假定消息在 Kafka 中保存为 JSON 格式。

    由于我们有一个 LocalDateTime 类型的字段，因此需要指定 JavaTimeModule，它负责将 LocalDateTime 对象映射为 JSON。

    Flink 模式不能包含不可序列化的字段，因为所有操作符（如模式或函数）都会在作业开始时序列化。

    Apache Spark 也存在类似问题。解决这一问题的已知方法之一是将字段初始化为静态，就像我们在上面的 ObjectMapper 中做的那样。这不是最漂亮的解决方案，但相对简单，而且能完成任务。

    isEndOfStream 方法可用于特殊情况，即只在接收到特定数据之前处理数据流。但在我们的案例中并不需要。

8. 自定义对象序列化

    现在，假设我们希望我们的系统可以创建消息备份。我们希望该过程是自动的，而且每次备份都应由一整天内发送的报文组成。

    此外，备份邮件应该有一个唯一的 ID。

    为此，我们可以创建以下类：

    main/.flink.model/Backup.java

    请注意，UUID 生成机制并不完美，因为它允许重复。不过，就本示例而言，这已经足够了。

    我们希望将备份对象以 JSON 格式保存到 Kafka，因此需要创建序列化模式（SerializationSchema）：

    main/.flink.model/BackupSerializationSchema.java

9. 为消息添加时间戳

    由于我们要为每天的所有消息创建备份，因此消息需要一个时间戳。

    Flink 提供了三种不同的时间特征：EventTime、ProcessingTime 和 IngestionTime。

    在我们的例子中，我们需要使用消息发送的时间，因此我们将使用 EventTime。

    要使用 EventTime，我们需要一个 TimestampAssigner，它将从我们的输入数据中提取时间戳：

    main/.flink.operator/InputMessageTimestampAssigner.java

    我们需要将 LocalDateTime 转换为 EpochSecond，因为这是 Flink 所期望的格式。分配时间戳后，所有基于时间的操作都将使用 sentAt 字段中的时间进行操作。

    由于 Flink 希望时间戳的单位是毫秒，而 toEpochSecond() 返回的时间单位是秒，因此我们需要将其乘以 1000，这样 Flink 才能正确创建窗口。

    Flink 定义了水印的概念。水印在数据未按发送顺序到达时非常有用。水印定义了允许处理元素的最大延迟时间。

    时间戳低于水印的元素根本不会被处理。

10. 创建时间窗口

    为了确保我们的备份只收集一天内发送的消息，我们可以在流上使用 timeWindowAll 方法，该方法会将消息分割成多个窗口。

    不过，我们仍然需要汇总每个窗口中的信息，并将其作为备份返回。

    为此，我们需要一个自定义的 AggregateFunction：

    main/.flink.operator/BackupAggregator.java

11. 聚合备份

    在分配了适当的时间戳并实现了 AggregateFunction 后，我们终于可以接收 Kafka 输入并对其进行处理了：

    main/.flink/FlinkDataPipeline.java:createBackup()

12. 结论

    在本文中，我们介绍了如何使用 Apache Flink 和 Apache Kafka 创建一个简单的数据管道。
