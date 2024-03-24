# 了解 Kafka 主题和分区

[Spring](https://www.baeldung.com/category/spring)

[Kafka](https://www.baeldung.com/tag/kafka) [消息传递](https://www.baeldung.com/tag/messaging)

1. 简介

    在本教程中，我们将探讨 Kafka 主题和分区以及它们之间的关系。

2. 什么是 Kafka 主题

    主题是事件序列的存储机制。从本质上讲，主题是一种持久的日志文件，它按照事件发生的时间顺序保存事件。因此，每个新事件总是被添加到日志的末尾。此外，事件是不可变的。因此，我们无法在事件添加到主题后对其进行更改。

    Kafka 主题的一个用例是记录一个房间的一系列温度测量值。一旦记录了一个温度值，如下午 5:02 时的 25 摄氏度，就不能更改，因为它已经发生了。此外，下午 5:06 时的温度值不可能先于下午 5:02 时记录的温度值。因此，将每次温度测量视为一个事件，Kafka 主题将是存储该数据的合适选择。

3. 什么是 Kafka 分区

    Kafka 使用主题分区来提高可扩展性。在对主题进行分区时，Kafka 会将其分成若干部分，并将每个部分存储在其分布式系统的不同节点中。分区的数量由我们或集群默认配置决定。

    Kafka 保证同一主题分区内事件的顺序。但默认情况下，它并不保证所有分区中事件的顺序。

    例如，为了提高性能，我们可以将主题分为两个不同的分区，并在消费者端读取它们。在这种情况下，消费者会按照事件到达同一分区的顺序读取事件。相反，如果 Kafka 将两个事件发送到不同的分区，我们就无法保证消费者按照事件产生的相同顺序读取事件。

    为了改善事件的排序，我们可以为事件对象设置一个[事件键](https://www.baeldung.com/java-kafka-message-key)。这样，具有相同键的事件就会被分配到同一个分区，而分区是有序的。这样，具有相同键的事件就会按照产生的顺序到达用户端。

4. 消费者组

    消费者组是从一个主题读取数据的一组消费者。Kafka 将所有分区划分给组内的消费者，其中任何给定的分区总是被一个组成员消耗一次。不过，这种划分可能是不平衡的，这意味着一个消费者可以被分配多个分区。

    例如，我们设想一个主题有三个分区，一个消费者小组有两个消费者应该阅读这三个分区。因此，一种可能的划分方式是，第一个消费者获得分区一和分区二，第二个消费者只获得分区三。

    在 [KIP-500](https://cwiki.apache.org/confluence/display/KAFKA/KIP-500%3A+Replace+ZooKeeper+with+a+Self-Managed+Metadata+Quorum) 更新中，Kafka 引入了一种名为 KRaft 的新[共识算法](https://www.baeldung.com/cs/consensus-algorithms-distributed-systems)。当我们向一个组中添加消费者或从一个组中移除消费者时，[KRaft](https://www.baeldung.com/kafka-shift-from-zookeeper-to-kraft) 会按比例重新平衡剩余消费者之间的分区。因此，它能保证没有一个分区没有分配到消费者。

5. 配置应用程序

    在本节中，我们将创建用于配置主题、消费者和生产者服务的类。

    1. 主题配置

        首先，让我们为主题创建配置类：

        src/.kafka.topicsandpartitions/KafkaTopicConfig.java

        KafkaTopicConfig 类注入了两个 Spring Bean。KafkaAdmin Bean 用它应该运行的网络地址启动 Kafka 集群，而 NewTopic Bean 用一个分区创建了一个名为 celcius-scale-topic 的主题。

    2. 消费者和生产者配置

        我们需要为主题注入生产者和消费者配置所需的类。

        首先，让我们创建生产者配置类：

        src/.kafka.topicsandpartitions/KafkaProducerConfig.java

        KafkaProducerConfig 注入了两个 Spring Bean。ProducerFactory 告诉我们 Kafka 应该如何序列化事件，以及生产者应该监听哪个服务器。消费者服务类将使用 KafkaTemplate 来创建事件。

    3. Kafka 生产者服务

        最后，完成初始配置后，我们就可以创建驱动程序应用程序了。让我们先创建生产者应用程序：

        src/.kafka.topicsandpartitions/ThermostatService.java

        ThermostatService 包含一个名为 measureCelsiusAndPublish 的方法。该方法在 [25, 35] 范围内生成随机温度测量值，并发布到摄氏度尺度主题 Kafka 话题中。为此，我们使用 Random 类的 doubles() 方法创建随机数流。然后，我们使用 kafkaTemplate 的 send() 方法发布事件。

6. 生成和消费事件

    在本节中，我们将了解如何配置 Kafka 消费者，以便使用嵌入式 Kafka 代理从主题中读取事件。

    1. 创建消费者服务

        要消费事件，我们需要一个或多个消费者类。让我们为 celcius-scale-topic 创建一个消费者：

        src/.kafka.topicsandpartitions/TemperatureConsumer.java

        我们的 consumer1() 方法使用 @KafkaListener 注解来启动消费者。topics 参数是要消费的主题列表，而 groupId 参数则标识了消费者所属的消费者组。

        为使稍后的结果可视化，我们使用 [ConcurrentHashMap](https://www.baeldung.com/concurrenthashmap-reading-and-writing) 来存储消费的事件。键对应消费者的名称，而值则包含消费者消费的分区。

    2. 创建测试类

        现在，让我们创建集成测试类：

        ```java
        @SpringBootTest(classes = ThermostatApplicationKafkaApp.class)
        @EmbeddedKafka(partitions = 2, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
        public class KafkaTopicsAndPartitionsIntegrationTest {
            @ClassRule
            public static EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, true, "multitype");

            @Autowired
            private ThermostatService service;

            @Autowired
            private TemperatureConsumer consumer;

            @Test
            public void givenTopic_andConsumerGroup_whenConsumersListenToEvents_thenConsumeItCorrectly() throws Exception {
                service.measureCelsiusAndPublish(10000);
                Thread.sleep(1000);
                System.out.println(consumer.consumedRecords);
            }
        }
        ```

        我们使用嵌入式 Kafka 代理与 Kafka 一起[运行测试](https://www.baeldung.com/spring-boot-kafka-testing)。@EmbeddedKafka 注解使用参数 brokerProperties 来配置代理运行的 URL 和端口。然后，我们在 EmbeddedKafkaBroker 字段中使用 [JUnit 规则](https://www.baeldung.com/junit-4-rules) 启动嵌入式代理。

        最后，在测试方法中，我们调用恒温器服务生成 10,000 个事件。

        我们将使用 Thread.sleep() 在事件产生后等待 1 秒钟。这将确保消费者在代理中正确设置，以便开始处理消息。

        让我们看看运行测试时的输出示例：

        `{consumer-1=[0, 1]}`

        这意味着同一个消费者处理了分区 0 和分区 1 中的所有事件，因为我们只有一个消费者和一个消费者组。如果不同消费者组中有更多消费者，结果可能会有所不同。

7. 结论

    在本文中，我们了解了 Kafka 主题和分区的定义，以及它们之间的关系。

    我们还展示了消费者使用嵌入式 Kafka 代理从主题的两个分区中读取事件的场景。

    与往常一样，示例代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-kafka-2) 上获取。
