# [使用 Kafka 消费者 API 从头开始读取数据](https://www.baeldung.com/java-kafka-consumer-api-read)

1. 简介

    在本教程中，我们将学习如何使用 Kafka Consumer API 从 Kafka 主题的开头读取数据。

2. 设置

    在开始之前，让我们先设置依赖关系，初始化 Kafka 集群连接，并向 Kafka 发布一些消息。

    Kafka 提供了一个方便的 Java 客户端库，我们可以用它在 Kafka 集群上执行各种操作。

    1. 依赖关系

        首先，让我们在项目的 pom.xml 文件中添加 Kafka 客户端 Java 库的 Maven 依赖：

        ```xml
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>3.4.0</version>
        </dependency>
        ```

    2. 集群和主题初始化

        在整个指南中，我们将假设 Kafka 集群以默认配置在本地系统上运行。

        其次，我们需要创建一个 Kafka 主题，用于发布和消费消息。让我们参考 Kafka 主题创建指南，创建一个名为 "baeldung" 的 Kafka 主题。

        现在，我们已经创建了一个主题，Kafka 集群也已开始运行，让我们向 Kafka 发布一些消息吧。

    3. 发布消息

        最后，让我们向 Kafka 主题 "baeldung" 发布几条虚拟消息。

        为了发布消息，让我们创建一个 KafkaProducer 实例，并使用由 Properties 实例定义的基本配置：

        ```java
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties);
        ```

        我们使用 KafkaProducer.send(ProducerRecord) 方法向 Kafka 主题 "baeldung" 发布消息：

        ```java
        for (int i = 1; i <= 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("baeldung", String.valueOf(i));
            producer.send(record);
        }
        ```

        在这里，我们向 Kafka 集群发布了十条消息。我们将用它们来演示我们的消费者实现。

3. 从头开始消费消息

    到目前为止，我们已经初始化了 Kafka 集群，并向 Kafka 主题发布了一些示例消息。接下来，让我们看看如何从头开始读取消息。

    为了演示这一点，我们首先初始化一个 KafkaConsumer 实例，其中包含一组由 Properties 实例定义的特定消费者属性。然后，我们使用创建的 KafkaConsumer 实例来消费消息，并再次回溯到分区偏移的起点。

    让我们来详细看看这些步骤中的每一步。

    1. 消费者属性

        要从 Kafka 主题的开头消费消息，我们要创建一个 KafkaConsumer 实例，并随机生成一个消费者组 ID。为此，我们将消费者的 "group.id" 属性设置为随机生成的 UUID：

        ```java
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        ```

        当我们为消费者生成一个新的消费者组 ID 时，消费者将始终属于一个由 "group.id" 属性标识的新消费者组。一个新的消费者组不会有任何与之相关的偏移量。在这种情况下，Kafka 提供了一个属性 "auto.offset.reset"（自动偏移重置），用于指示当 Kafka 中没有初始偏移或服务器上不再存在当前偏移时应采取的措施。

        auto.offset.reset 属性接受以下值：

        - 最早： 该值会自动将偏移量重置为最早的偏移量
        - 最新： 该值会自动将偏移量重置为最新偏移量
        - 无： 如果没有找到消费者组的先前偏移量，该值会向消费者抛出一个异常
        - anything else：如果设置了前三个值以外的其他值，则会向消费者抛出异常。

        由于我们想从 Kafka 主题的开头读取数据，因此将 "auto.offset.reset" 属性的值设置为 "earliest"（最早）：

        `consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");`

        现在，让我们使用消费者属性创建一个 KafkaConsumer 实例：

        `KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);`

        我们使用这个 KafkaConsumer 实例从主题的开头消费消息。

    2. 消费消息

        要消费消息，我们首先要订阅消费者，以便从主题 "baeldung" 中消费消息：

        `consumer.subscribe(Arrays.asList("baeldung"));`

        接下来，我们使用 KafkaConsumer.poll(Duration duration) 方法轮询主题 "baeldung" 的新消息，直到 Duration 参数指定的时间为止：

        ```java
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        for (ConsumerRecord<String, String> record : records) {
            logger.info(record.value());
        }
        ```

        这样，我们就读取了从 "baeldung" 主题开始的所有信息。

        此外，为了重置现有消费者以从主题的开头读取，我们使用了 `KafkaConsumer.seekToBeginning(Collection<TopicPartition> partitions)` 方法。该方法接受一个 TopicPartition 集合，并将消费者的偏移量指向分区的起始位置：

        `consumer.seekToBeginning(consumer.assignment());`

        在这里，我们将 KafkaConsumer.assignment() 的值传递给 seekToBeginning() 方法。KafkaConsumer.assignment() 方法会返回当前分配给消费者的分区集。

        最后，再次轮询同一个消费者获取消息时，就会从分区的开头读取所有消息：

        ```java
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        for (ConsumerRecord<String, String> record : records) {
            logger.info(record.value());
        }
        ```

4. 结论

    在本文中，我们学习了如何使用 Kafka 消费者 API 从 Kafka 主题的开头读取消息。

    我们首先了解了一个新的消费者如何从 Kafka 主题的开头读取消息，以及它的实现。然后，我们再看看已经在消费的消费者如何寻找偏移量，从头开始读取消息。
