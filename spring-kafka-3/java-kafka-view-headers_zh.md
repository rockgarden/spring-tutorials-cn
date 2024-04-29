# [用 Java 查看 Kafka 头信息](https://www.baeldung.com/java-kafka-view-headers)

1. 简介

    Apache Kafka 是一个分布式流平台，允许我们发布和订阅记录流（通常称为消息）。此外，Kafka 头提供了一种将元数据附加到 Kafka 消息的方法，从而在消息处理过程中实现额外的上下文和灵活性。

    在本教程中，我们将深入研究常用的 Kafka 标头，并学习如何使用 Java 查看和提取它们。

2. Kafka 标头概述

    Kafka 头代表附加到 Kafka 消息上的键值对，提供了一种在主要消息内容旁边包含补充元数据的方法。

    例如，Kafka 头通过提供数据，将消息导向特定的处理管道或消费者，从而促进消息路由。此外，标头还可以根据应用程序的处理逻辑，携带定制的应用程序元数据。

3. Kafka 默认头

    Kafka 会自动在 Kafka 生产者发送的消息中包含几个默认头。此外，这些标头还提供了有关消息的重要元数据和上下文。在本节中，我们将深入探讨几个常用的标头及其在 Kafka 消息处理领域的意义。

    1. 生产者标头

        在 Kafka 中生成消息时，生产者会自动包含几个默认头，例如

        - [KafkaHeaders.TOPIC](https://www.baeldung.com/kafka-topics-partitions) - 该标头包含消息所属主题的名称。
        - [KafkaHeaders.KEY](https://www.baeldung.com/kafka-send-data-partition#2-key-based-approach) - 如果消息带有密钥，Kafka 会自动包含一个名为 "key" 的头，其中包含序列化的密钥字节。
        - KafkaHeaders.PARTITION - Kafka 添加一个名为 "partition" 的头，以指示消息所属的分区 ID。
        - KafkaHeaders.TIMESTAMP - Kafka 为每条消息附加一个名为 "timestamp"（时间戳）的标头，表示消息由生产者生成的时间戳。

    2. 消费者头

        以 RECEIVED_ 为前缀的头信息由 Kafka 消费者在接收消息时添加，以提供消息接收过程的元数据：

        - KafkaHeaders.RECEIVED_TOPIC - 该标头含接收消息的主题名称。
        - KafkaHeaders.RECEIVED_KEY - 这个头允许消费者访问与消息相关的密钥。
        - KafkaHeaders.RECEIVED_PARTITION - Kafka 添加了这个头，以指示消息被分配到的分区的 ID。
        - KafkaHeaders.RECEIVED_TIMESTAMP - 这个头反映了消费者收到消息的时间。
        - KafkaHeaders.OFFSET - 偏移量表示消息在分区日志中的位置。

4. 使用报文头消费报文

    首先，我们实例化一个 KafkaConsumer 对象。KafkaConsumer 负责订阅 Kafka 主题并从中获取消息。实例化 KafkaConsumer 后，我们订阅要从中获取消息的 Kafka 主题。通过订阅一个主题，消费者可以接收在该主题上发布的消息。

    一旦消费者订阅了主题，我们就开始从 Kafka 抓取记录。在这个过程中，KafkaConsumer 会从订阅的主题中检索消息以及相关的标题。

    下面的代码示例演示了如何消费带有标头的消息：

    ```java
    @KafkaListener(topics = "my-topic")
    public void listen(String message, @Headers Map<String, Object> headers) {
        System.out.println("Received message: " + message);
        System.out.println("Headers:");
        headers.forEach((key, value) -> System.out.println(key + ": " + value));
    }
    ```

    当收到来自指定主题（如 "my-topic"）的消息时，Kafka 监听器容器会调用 listen() 方法。@Headers 注解表示该参数应填入接收到的消息的头信息。

    下面是一个输出示例：

    ```log
    Received message: Hello Baeldung!
    Headers:
    kafka_receivedMessageKey: null
    kafka_receivedPartitionId: 0
    kafka_receivedTopic: my-topic
    kafka_offset: 123
    ... // other headers
    ```

    要访问特定的头，我们可以使用头映射的 get() 方法，提供所需头的键值。下面是一个访问主题名称的示例：

    `String topicName = headers.get(KafkaHeaders.TOPIC);`

    topicName 应该返回 my-topic。

    此外，在消费消息时，如果我们已经知道处理所需的头信息，我们可以直接提取它们作为方法参数。这种方法提供了一种更简洁、更有针对性的方式来访问特定的头信息值，而无需遍历所有头信息。

    下面是一个代码示例，演示了如何消费带有头信息的消息，直接提取特定的头作为方法参数：

    ```java
    @KafkaListener(topics = "my-topic")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        System.out.println("Received message: " + message);
        System.out.println("Partition: " + partition);
    }
    ```

    在 listen() 方法中，我们使用 @Header 注解直接提取 RECEIVED_PARTITION 头信息。通过该注解，我们可以指定要提取的标头及其相应的类型。将头信息的值直接注入方法参数（在本例中为 partition），可以在方法主体中直接访问。

    下面是输出结果：

    ```log
    Received message: Hello Baeldung!
    Partition: 0
    ```

5. 结论

    在本文中，我们探讨了 Kafka 头信息在 Apache Kafka 消息处理中的重要性。我们探讨了生产者和消费者都会自动包含的默认头信息。此外，我们还学习了如何提取和使用这些标头。
