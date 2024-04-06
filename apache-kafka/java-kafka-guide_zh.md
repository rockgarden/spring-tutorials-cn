# Kafka指南

Apache Kafka 是一个分布式流处理系统。通过以下指南了解如何在应用程序中使用它。

原理

- [Apache Kafka 简介](../apache-kafka-2/apache-kafka_zh.md)
- [使用Apache Kafka进行数据建模](../algorithms-modules/apache-kafka-data-modeling_zh.md)
- [了解 Kafka 主题和分区](../spring-kafka-2/kafka-topics-partitions_zh.md)

- [使用消费者API创建Kafka监听器](../apache-kafka-2/kafka-create-listener-consumer-api_zh.md)
- [Apache Kafka 中 GroupId 和 ConsumerId 的区别](../spring-kafka-3/apache-kafka-groupid-vs-consumerid_zh.md)
- [Java中的KafkaStreams简介](java-kafka-streams_zh.md)
- [Kafka流与Kafka消费者](java-kafka-streams-vs-kafka-consumer_zh.md)

- [Kafka中的提交偏移量](../apache-kafka-3/kafka-commit-offsets_zh.md)

- [使用Java创建Kafka主题](kafka-topic-creation_zh.md)
- [获取 Kafka 主题中的消息数量](../spring-kafka/java-kafka-count-topic-messages_zh.md)
- [获取 Kafka 中某个 Topic 的分区计数](../apache-kafka-2/java-kafka-partition-count-topic_zh.md)
- [获取 Kafka Topic 中的最后 N 条消息](../apache-kafka-2/java-apache-kafka-get-last-n-messages_zh.md)

- [在Kafka中向特定分区发送数据](../spring-kafka/kafka-send-data-partition_zh.md)
- [向 Kafka 发送消息是否需要密钥？](../apache-kafka-2/java-kafka-message-key_zh.md)
- [确保 Kafka 中的消息排序：策略和配置](../apache-kafka-2/kafka-message-ordering_zh.md)
- [使用 Apache Kafka 读取多个消息](../apache-kafka-2/kafka-read-multiple-messages_zh.md)
- [向Kafka消息添加自定义标题](../apache-kafka-2/java-kafka-custom-headers_zh.md)

- [使用Java在Kafka中进行精确一次处理](kafka-exactly-once_zh.md)
- [Apache ActiveMQ vs. Kafka](apache-activemq-vs-kafka_zh.md)
- [Kafka配置中的引导服务器](../apache-kafka-2/java-kafka-bootstrap-server_zh.md)

- [使用 Kafka Consumer API 从开始读取数据](../apache-kafka-2/java-kafka-consumer-api-read_zh.md)
- [Kafka连接器介绍](kafka-connectors-guide_zh.md)
- [使用 MQTT 和 MongoDB 的 Kafka 连接示例](kafka-connect-mqtt-mongodb_zh.md)
- [使用 Kafka MockProducer](kafka-mockproducer_zh.md)
- [使用 Kafka MockConsumer](kafka-mockconsumer_zh.md)

- [Apache Kafka中的自定义序列化器](kafka-custom-serializer_zh.md)
- [用Flink和Kafka构建数据管道](kafka-flink-data-pipeline_zh.md)

- [使用 Spring 的 Apache Kafka 简介](../spring-kafka/spring-kafka_zh.md)

- 在Spring Boot中动态管理Kafka监听器

>> Redpanda 简介
>> 理解 Java 中的 Kafka InstanceAlreadyExistsException
>> 用 Java 查看 Kafka 头文件
>> 使用 Spring Modulith 实现事件外部化
>> 如何在 Spring-Kafka 中捕获反序列化错误？
>> 使用 Spring 为 Kafka 创建死信队列
>> Spring Kafka 可信包功能
>> 管理 Kafka 消费者组
>> 在 Kafka 中分割流
>> 如何将 Kafka 消费者订阅到多个主题
>> 将 JSON 文件数据导入 Kafka Topic
>> 使用 AsyncAPI 和 Springwolf 记录 Spring 事件驱动 API
>> Spring Kafka： 在同一个主题上配置多个监听器
>> 在 Kafka 消费者中实现重试
>> Kafka 从 ZooKeeper 到 Kraft 的转变
>> 流平台中的消息传递语义
>> 使用 Spring Boot 的 Kafka 流
>> 连接到在 Docker 中运行的 Apache Kafka
>> 使用 Spring Boot 配置 Kafka SSL
>> 使用 Kafka 发送大消息
>> 在 Apache Kafka 中监控消费者滞后情况
>> 使用 Docker 设置 Apache Kafka 指南
>> 清理 Apache Kafka Topic 指南
>> 在 Apache Kafka 中配置消息保留期
>> 测试 Kafka 和 Spring Boot
>> 使用 Kafka、Spark Streaming 和 Cassandra 构建数据管道
>> Java 中的 KafkaStreams 简介

- [检查 Apache Kafka 服务器是否正在运行的指南](../apache-kafka-2/apache-kafka-check-server-is-running_zh.md)

## [reference-tag](https://www.baeldung.com/tag/kafka)

- [x] [Intro to Apache Kafka with Spring](https://www.baeldung.com/spring-kafka)

>> Dynamically Managing Kafka Listeners in Spring Boot

- Commit Offsets in Kafka

>> Introduction to Redpanda

- [x] [Difference Between GroupId and ConsumerId in Apache Kafka](https://www.baeldung.com/apache-kafka-groupid-vs-consumerid)

>> Understanding Kafka InstanceAlreadyExistsException in Java
>> View Kafka Headers in Java
>> Event Externalization with Spring Modulith
>> How to Catch Deserialization Errors in Spring-Kafka?

- [Read Multiple Messages with Apache Kafka](https://www.baeldung.com/kafka-read-multiple-messages)
- [Sending Data to a Specific Partition in Kafka](https://www.baeldung.com/kafka-send-data-partition)

>> Dead Letter Queue for Kafka With Spring

- [Creating a Kafka Listener Using the Consumer API](https://www.baeldung.com/kafka-create-listener-consumer-api)

>> Spring Kafka Trusted Packages Feature
>> Manage Kafka Consumer Groups

- Ensuring Message Ordering in Kafka: Strategies and Configurations

>> Splitting Streams in Kafka
>> How to Subscribe a Kafka Consumer to Multiple Topics

- [Introduction to Apache Kafka](https://www.baeldung.com/apache-kafka)

>> JSON File Data Into Kafka Topic

- bootstrap-server in Kafka Configuration

- [Understanding Kafka Topics and Partitions](https://www.baeldung.com/kafka-topics-partitions)

>> Documenting Spring Event-Driven API Using AsyncAPI and Springwolf

- Get Partition Count for a Topic in Kafka

- Read Data From the Beginning Using Kafka Consumer API
- Is a Key Required as Part of Sending Messages to Kafka?

- Get Last N Messages in Apache Kafka Topic
- Add Custom Headers to a Kafka Message

>> Spring Kafka: Configure Multiple Listeners on Same Topic
>> Implementing Retry in Kafka Consumer
>> Kafka’s Shift from ZooKeeper to Kraft
>> Message Delivery Semantics in Streaming Platforms

- Guide to Check if Apache Kafka Server Is Running

- Get the Number of Messages in an Apache Kafka Topic
- [Apache ActiveMQ vs. Kafka](https://www.baeldung.com/apache-activemq-vs-kafka)

>> Kafka Streams With Spring Boot
>> Connect to Apache Kafka Running in Docker
>> Configuring Kafka SSL Using Spring Boot

- Custom Serializers in Apache Kafka

>> Send Large Messages With Kafka
>> Monitor the Consumer Lag in Apache Kafka

- [x] [Kafka Topic Creation Using Java](https://www.baeldung.com/kafka-topic-creation)
- [x] [Kafka Streams vs. Kafka Consumer](https://www.baeldung.com/java-kafka-streams-vs-kafka-consumer)

>> Guide to Setting Up Apache Kafka Using Docker
>> Guide to Purging an Apache Kafka Topic
>> Configuring Message Retention Period in Apache Kafka

- Data Modeling with Apache Kafka

>> Testing Kafka and Spring Boot

- Using Kafka MockProducer
- Using Kafka MockConsumer

 Building a Data Pipeline with Kafka, Spark Streaming and Cassandra

- [x] Introduction to Kafka Connectors
- Exactly Once Processing in Kafka with Java
- Building a Data Pipeline with Flink and Kafka
- [x] [Introduction to KafkaStreams in Java](https://www.baeldung.com/java-kafka-streams)
