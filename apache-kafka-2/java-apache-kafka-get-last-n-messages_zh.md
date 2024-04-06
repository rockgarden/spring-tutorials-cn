# [在 Apache Kafka 主题中获取最后 N 条消息](https://www.baeldung.com/java-apache-kafka-get-last-n-messages)

1. 简介

    在这个简短的教程中，我们将了解如何从 Apache Kafka Topic 中获取最后 N 条消息。

    在文章的第一部分，我们将重点介绍执行此操作所需的先决条件。在第二部分中，我们将使用 [Kafka Java API](https://kafka.apache.org/documentation/#api) 库构建一个使用 Java 读取消息的小工具。最后，我们将提供简短的指导，以便使用 [KafkaCat](https://github.com/edenhill/kcat) 从命令行实现相同的结果。

2. 前提条件

    从一个 Kafka Topic 中检索最后 N 条消息，就像从一个定义明确的偏移量开始消费消息一样简单。Kafka Topic 中的偏移量表示消费者的当前位置。在上一篇文章中，我们看到了如何利用 [consumer.seekToEnd()](https://kafka.apache.org/21/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#seekToEnd-java.util.Collection-) 方法获取 [Apache Kafka Topic 中特定数量的消息](https://www.baeldung.com/java-kafka-count-topic-messages)。

    考虑到相同的功能，我们可以通过简单的减法直观地计算出正确的偏移量：offset = lastOffset - N。

    不过，如果我们使用事务生产者生成记录，这种方法就不起作用了。在这种情况下，偏移量会跳过一些数字，以适应 Kafka 主题事务记录（提交/回滚等）。使用事务生产者的一种常见情况是，我们需要[精确地处理一次 Kafka 消息](https://www.baeldung.com/kafka-exactly-once)。简单地说，如果我们在（lastOffset - N）处开始读取消息，我们可能会消耗少于 N 条消息，因为一些偏移量[被事务记录消耗掉了](https://issues.apache.org/jira/browse/KAFKA-10009)。

3. 用 Java 获取 Kafka 主题中的最后 N 条消息

    首先，我们需要创建一个生产者（Producer）和一个消费者（Consumer）：

    ```java
    Properties producerProperties = new Properties();
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
    producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "ConsumerGroup1");

    KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties);
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
    ```

    现在让我们生成一些消息：

    ```java
    final String TOPIC1 = "baeldung-topic";
    int messagesInTopic = 100;
    for (int i = 0; i < messagesInTopic; i++) {
        producer.send(new ProducerRecord(TOPIC1, null, MESSAGE_KEY, String.valueOf(i))).get();
    }
    ```

    为了简单明了起见，假设我们只需要为消费者注册一个分区：

    ```java
    TopicPartition partition = new TopicPartition(TOPIC1, 0);
    List<TopicPartition> partitions = new ArrayList<>();
    partitions.add(partition);
    consumer.assign(partitions);
    ```

    如前所述，我们需要将偏移量定位在正确的位置，然后就可以开始轮询了：

    ```java
    int messagesToRetrieve = 10;
    consumer.seekToEnd(partitions);
    long startIndex = consumer.position(partition) - messagesToRetrieve;
    consumer.seek(partition, startIndex);
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMinutes(1));
    ```

    如果网络速度特别慢，或者要检索的消息数量特别多，我们可能需要延长轮询持续时间。在这种情况下，我们需要考虑内存中的大量记录可能会导致资源短缺的问题。

    最后，让我们检查一下是否真的检索到了正确数量的消息：

    ```java
    for (ConsumerRecord<String, String> record : records) {
        assertEquals(MESSAGE_KEY, record.key());
        assertTrue(Integer.parseInt(record.value()) >= (messagesInTopic - messagesToRetrieve));
        recordsReceived++;
    }
    assertEquals(messagesToRetrieve, recordsReceived);
    ```

4. 使用 KafkaCat 获取 Kafka 主题中的最近 N 条消息

    KafkaCat (kcat) 是一个命令行工具，我们可以用它来测试和调试 Kafka Topic。Kafka 本身提供了大量脚本和 shell 工具来完成相同的操作。尽管如此，KafkaCat 的简洁性和易用性还是让它成为了进行检索 Apache Kafka Topic 中最后 N 条消息等操作的事实标准。安装后，只需运行以下简单命令，就能检索 Kafka Topic 中产生的最新 N 条消息：

    `$ kafkacat -C -b localhost:9092 -t topic-name -o -<N> -e`

    - -C表示我们需要消耗消息
    - -b表示Kafka代理的位置
    - -t 表示主题名称
    - -o 表示我们需要从该偏移量开始读取。带负号表示我们需要从末尾开始读取 N 条消息。
    - -e选项在读取最后一条消息时退出

    联系上文讨论的情况，从名为 "baeldung-topic" 的主题中读取最后 10 条消息的命令是

    `$ kafkacat -C -b localhost:9092 -t baeldung-topic -o -10 -e`

5. 结论

    在这个简短的教程中，我们已经了解了如何从 Kafka Topic 中获取最新的 N 条消息。在第一部分中，我们使用了 Java Kafka API 库。在第二部分中，我们使用了名为 KafkaCat 的命令行实用程序。
