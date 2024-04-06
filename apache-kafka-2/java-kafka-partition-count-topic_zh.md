# [获取 Kafka 中一个主题的分区计数](https://www.baeldung.com/java-kafka-partition-count-topic)

1. 简介

    在本教程中，我们将介绍检索 [Kafka Topic](https://www.baeldung.com/spring-kafka) 分区总数的不同方法。在简短介绍什么是 Kafka 分区以及为什么我们可能需要检索此信息后，我们将编写执行此操作的 Java 代码。然后，我们将了解如何使用 CLI 获取此信息。

2. Kafka 分区

    一个 Kafka Topic 可以分为多个分区。拥有多个分区的目的是能够并发地从同一个主题中消费消息。由于消费者数量多于现有分区是没有用的，因此一个主题中的 Kafka 分区数量代表了消费的最大并行度。因此，提前知道一个给定主题有多少个分区，对于正确确定各自消费者的大小很有帮助。

3. 使用 Java 检索分区数

    要使用 Java 检索特定主题的分区数，我们可以使用 [KafkaProducer.partitionFor(topic)](https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html#partitionsFor-java.lang.String-) 方法。该方法将返回给定主题的分区元数据：

    ```java
    Properties producerProperties = new Properties();
    // producerProperties.put("key","value") ... 
    KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties)
    List<PartitionInfo> info = producer.partitionsFor(TOPIC);
    Assertions.assertEquals(3, info.size());
    ```

    该方法返回的 PartitionInfo 列表的大小将正好等于为特定主题配置的分区数。

    如果我们无法访问 Producer，可以使用 Kafka AdminClient 以稍微复杂一点的方式实现相同的结果：

    ```java
    Properties props = new Properties();
    // props.put("key","value") ...
    AdminClient client = AdminClient.create(props)){
    DescribeTopicsResult describeTopicsResult = client.describeTopics(Collections.singletonList(TOPIC));
    Map<String, KafkaFuture> values = describeTopicsResult.values();
    KafkaFuture topicDescription = values.get(TOPIC);
    Assertions.assertEquals(3, topicDescription.get().partitions().size());
    ```

    在这种情况下，我们依赖 KafkaClient.describeTopic(topic) 方法，该方法会返回一个 DescribeTopicsResult 对象，其中包含未来要执行的任务的映射。在这里，我们只检索我们需要的 Topic 的 TopicDescription，最后检索分区的数量。

4. 使用 CLI 检索分区编号

    使用 CLI 检索给定 Topic 的分区数有几种方法。

    首先，我们可以依靠每次安装 Kafka 时附带的 shell 脚本来运行：

    `$ kafka-topics --describe --bootstrap-server localhost:9092 --topic topic_name`

    此命令将输出指定主题的完整描述：

    `Topic:topic_name        PartitionCount:3        ReplicationFactor:1     Configs: ...`

    另一种方法是使用 [Kafkacat](https://docs.confluent.io/platform/current/clients/kafkacat-usage.html)，这是一种非基于 JVM 的 Kafka 消费者和生产者。在元数据列表模式（-L）下，这个 shell 工具可以显示 Kafka 集群的当前状态，包括所有主题和分区。要显示特定主题的元数据信息，我们可以运行以下命令：

    `$ kafkacat -L -b localhost:9092 -t topic_name`

    该命令的输出将是

    ```shell
    Metadata for topic topic_name (from broker 1: mybroker:9092/1):
    topic "topic_name" with 3 partitions:
        partition 0, leader 3, replicas: 1,2,3, isrs: 1,2,3
        partition 1, leader 1, replicas: 1,2,3, isrs: 1,2,3
        partition 2, leader 1, replicas: 1,2, isrs: 1,2
    ```

    我们可以看到，这个 shell 实用程序命令还显示了特定主题及其分区的有用详细信息。

5. 总结

    在这个简短的教程中，我们看到了如何使用 Java 和 CLI 来检索特定 Kafka Topic 的分区总数。

    我们首先了解了为什么检索此信息非常有用，然后使用了 KafkaProducer 和 KafkaAdmin。最后，我们使用了 Kafka 脚本实用程序和 KafkaCat 的 shell 命令。
