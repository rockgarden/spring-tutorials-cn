# 在 Kafka 中向特定分区发送数据

[Data](https://www.baeldung.com/category/data)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 简介

    Apache Kafka 是一个分布式流平台，擅长处理海量实时数据流。Kafka 将数据组织成[主题](https://www.baeldung.com/kafka-topics-partitions)，并进一步将主题划分成分区。每个分区作为一个独立的通道，实现并行处理和容错。

    在本教程中，我们将深入探讨将数据发送到 Kafka 中特定分区的技术。我们将探讨这种方法的优点、实施方法和潜在挑战。

2. 了解 Kafka 分区

    现在，让我们来探讨 Kafka 分区的基本概念。

    1. 什么是 Kafka 分区

        当生产者向 Kafka 主题发送消息时，Kafka 会使用指定的分区策略将这些消息组织到分区中。分区是一个基本单元，代表了线性、有序的消息序列。消息一旦产生，就会根据所选的分区策略被分配到一个特定的分区。随后，信息会被附加到该分区中日志的末尾。

    2. 并行性和用户组

        一个 Kafka 主题可分为多个分区，一个[消费者组](https://www.baeldung.com/kafka-manage-consumer-groups)可分配到这些分区的一个子集。组内的每个消费者都会独立处理来自其分配分区的消息。这种并行处理机制提高了整体吞吐量和可扩展性，使 Kafka 能够高效处理大量数据。

    3. 排序和处理保证

        在单个分区内，Kafka 会确保按照接收到的相同顺序处理消息。这为依赖[消息顺序](https://www.baeldung.com/kafka-message-ordering#1-producer-and-consumer-timing)的应用程序（如金融交易或事件日志）提供了顺序处理保证。不过，请注意，由于网络延迟和其他操作因素，接收消息的顺序可能与最初发送消息的顺序不同。

        在不同分区之间，Kafka 并不强加有保证的顺序。来自不同分区的消息可能会被并发处理，从而带来事件顺序变化的可能性。在设计依赖严格的消息顺序的应用程序时，必须考虑这一特性。

    4. 容错和高可用性

        分区还有助于 Kafka 实现出色的容错性。每个分区都可以在多个代理之间复制。如果代理发生故障，复制的分区仍可被访问，并确保对数据的持续访问。

        Kafka 集群可以无缝地将消费者重定向到健康的代理，从而保持数据的可用性和系统的高可靠性。

3. 为什么要将数据发送到特定分区

    本节将探讨将数据发送到特定分区的原因。

    1. 数据亲和性

        数据亲和性是指有意将相关数据归入同一分区。通过将相关数据发送到特定分区，我们可以确保数据一起处理，从而提高处理效率。

        例如，考虑一种情况，我们可能希望确保客户的订单位于同一分区，以便进行订单跟踪和分析。确保来自特定客户的所有订单都在同一个分区中，可以简化跟踪和分析流程。

    2. 负载平衡

        此外，在分区之间均匀地分配数据有助于确保最佳的资源利用率。在分区间平均分配数据有助于优化 Kafka 集群内的资源利用率。通过根据负载情况向分区发送数据，我们可以防止出现资源瓶颈，并确保每个分区都能接收到可管理的均衡工作量。

    3. 优先级

        在某些情况下，并非所有数据都具有相同的优先级或紧迫性。Kafka 的分区功能可将关键数据引导到专用分区进行快速处理，从而实现关键数据的优先级排序。与不太重要的数据相比，这种优先级排序可确保高优先级消息得到及时关注和更快处理。

4. 发送到特定分区的方法

    Kafka 提供了各种将消息分配到分区的策略，从而提供了数据分布和处理的灵活性。下面是一些可用于将消息发送到特定分区的常用方法。

    1. 粘性分区器

        在 Kafka 2.4 及以上版本中，粘性分区器（sticky partitioner）的目的是将没有键的消息集中在同一个分区中。然而，这种行为并不是绝对的，它与批处理设置（如 batch.size 和 linger.ms）相互影响。

        为了优化消息传递，Kafka 会在将消息发送到代理之前将其分组为多个批次。batch.size 设置（默认为 16,384 字节）控制着最大批次大小，影响着消息在粘性分区器下的同一分区中停留的时间。

        linger.ms 配置（默认值：0 毫秒）会在发送批次前引入延迟，从而可能延长无密钥报文的粘性行为。

        在下面的测试案例中，假设默认批处理配置保持不变。我们将发送三条信息，但不显式分配密钥。我们应该希望它们最初被分配到同一个分区：

        ```java
        kafkaProducer.send("default-topic", "message1");
        kafkaProducer.send("default-topic", "message2");
        kafkaProducer.send("default-topic", "message3");

        await().atMost(2, SECONDS)
        .until(() -> kafkaMessageConsumer.getReceivedMessages()
            .size() >= 3);

        List<ReceivedMessage> records = kafkaMessageConsumer.getReceivedMessages();

        Set<Integer> uniquePartitions = records.stream()
        .map(ReceivedMessage::getPartition)
        .collect(Collectors.toSet());

        Assert.assertEquals(1, uniquePartitions.size());
        ```

    2. 基于键的方法

        在基于密钥的方法中，Kafka 会将具有相同密钥的消息导向同一个分区，从而优化相关数据的处理。这是通过哈希函数实现的，确保了消息键与分区的确定性映射。

        在本测试案例中，具有相同密钥 partitionA 的消息应始终位于同一分区。让我们用下面的代码片段来说明基于密钥的分区：

        ```java
        kafkaProducer.send("order-topic", "partitionA", "critical data");
        kafkaProducer.send("order-topic", "partitionA", "more critical data");
        kafkaProducer.send("order-topic", "partitionB", "another critical message");
        kafkaProducer.send("order-topic", "partitionA", "another more critical data");

        await().atMost(2, SECONDS)
        .until(() -> kafkaMessageConsumer.getReceivedMessages()
            .size() >= 4);

        List<ReceivedMessage> records = kafkaMessageConsumer.getReceivedMessages();
        Map<String, List<ReceivedMessage>> messagesByKey = groupMessagesByKey(records);

        messagesByKey.forEach((key, messages) -> {
            int expectedPartition = messages.get(0)
            .getPartition();
            for (ReceivedMessage message : messages) {
                assertEquals("Messages with key '" + key + "' should be in the same partition", message.getPartition(), expectedPartition);
            }
        });
        ```

        此外，在基于密钥的方法中，共享相同密钥的消息会按照它们在特定分区内产生的顺序被一致接收。这保证了分区内消息顺序的保持，尤其是相关消息。

        在本测试用例中，我们按特定顺序生成了密钥为 partitionA 的消息，测试主动验证了这些消息在分区内的接收顺序是一致的：

        ```java
        kafkaProducer.send("order-topic", "partitionA", "message1");
        kafkaProducer.send("order-topic", "partitionA", "message3");
        kafkaProducer.send("order-topic", "partitionA", "message4");

        await().atMost(2, SECONDS)
        .until(() -> kafkaMessageConsumer.getReceivedMessages()
            .size() >= 3);

        List<ReceivedMessage> records = kafkaMessageConsumer.getReceivedMessages();

        StringBuilder resultMessage = new StringBuilder();
        records.forEach(record -> resultMessage.append(record.getMessage()));
        String expectedMessage = "message1message3message4";

        assertEquals("Messages with the same key should be received in the order they were produced within a partition", 
        expectedMessage, resultMessage.toString());
        ```

    3. 自定义分区

        为了实现精细控制，Kafka 允许定义自定义分区器。这些类实现了分区器接口，使我们能够根据消息内容、元数据或其他因素编写逻辑，以确定目标分区。

        在本节中，我们将在向 Kafka 主题分派订单时，根据客户类型创建自定义分区逻辑。具体来说，优质客户的订单将被定向到一个分区，而普通客户的订单将被定向到另一个分区。

        首先，我们创建一个名为 CustomPartitioner 的类，继承自 Kafka Partitioner 接口。在这个类中，我们用自定义逻辑覆盖 partition() 方法，以确定每条消息的目标分区：

        scr/main/.partitioningstrategy/CustomPartitioner.java

        接下来，要在 Kafka 中应用这个自定义分区器，我们需要在生产者配置中设置 PARTITIONER_CLASS_CONFIG 属性。Kafka 将根据 CustomPartitioner 类中定义的逻辑，使用该分区器来确定每条消息的分区。

        方法 setProducerToUseCustomPartitioner() 用于设置 Kafka 生产者使用 CustomPartitioner：

        scr/test/.partitioningstrategy/KafkaApplicationIntegrationTest.java:setProducerToUseCustomPartitioner()

        然后，我们构建一个测试用例，以确保自定义分区逻辑正确地将高级和普通客户订单路由到各自的分区：

        scr/test/.partitioningstrategy/KafkaApplicationIntegrationTest.java:givenCustomPartitioner_whenSendingMessages_shouldConsumeOnlyFromSpecificPartition()

    4. 直接分区分配

        在主题间手动迁移数据或在分区间调整数据分布时，直接分区分配有助于控制消息的位置。Kafka 还提供了使用 ProductRecord 构造函数直接向特定分区发送消息的功能，该构造函数接受分区编号。通过指定分区编号，我们可以为每条消息明确指定目标分区。

        在本测试用例中，我们指定了 send() 方法的第二个参数，以接收分区编号：

        scr/test/.partitioningstrategy/KafkaApplicationIntegrationTest.java:givenCustomPartitioner_whenSendingMessages_shouldRouteToCorrectPartition()

5. 从特定分区消费

    要从 Kafka 消费者端的特定分区中消费数据，我们可以使用 KafkaConsumer.assign() 方法指定要订阅的分区。这可以对消费进行细粒度控制，但需要手动管理分区偏移量。

    下面是一个使用 assign() 方法从特定分区消费消息的例子：

    scr/test/.partitioningstrategy/KafkaApplicationIntegrationTest.java:givenCustomPartitioner_whenSendingMessages_shouldConsumeOnlyFromSpecificPartition()

6. 潜在挑战和注意事项

    向特定分区发送信息时，有可能出现分区间负载分配不均的情况。如果用于分区的逻辑没有在所有分区中统一分配消息，就会出现这种情况。此外，扩展 Kafka 集群（包括添加或删除代理）可能会触发分区重新分配。在重新分配过程中，代理可能会移动分区，从而可能扰乱消息的顺序或导致暂时的不可用。

    因此，我们应该使用 Kafka 工具或指标定期监控每个分区的负载。例如，Kafka 管理客户端（[Kafka Admin Client](https://www.baeldung.com/java-kafka-consumer-lag)）和 Micrometer 可以帮助我们深入了解分区的健康状况和性能。我们可以使用管理员客户端检索有关主题、分区及其当前状态的信息，并使用 Micrometer 进行指标监控。

    此外，预计需要主动调整分区策略或横向扩展 Kafka 集群，以有效管理特定分区上增加的负载。我们还可以考虑增加分区数量或调整关键范围，以实现更均衡的分布。

7. 结论

    总之，在 Apache Kafka 中向特定分区发送消息的功能为优化数据处理和提高整体系统效率提供了强大的可能性。

    在本教程中，我们探讨了将消息定向到特定分区的各种方法，包括基于密钥的方法、自定义分区和直接分区分配。每种方法都具有不同的优势，允许我们根据应用程序的具体要求进行定制。

    一如既往，示例的源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-kafka) 上获取。
