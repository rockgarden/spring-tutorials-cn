# [向 Kafka 发送消息是否需要密钥？](https://www.baeldung.com/java-kafka-message-key)

1. 简介

    在本教程中，我们将首先了解 Kafka 消息中密钥的意义。然后，我们将学习如何将带有密钥的消息发布到 Kafka 主题中。

2. Kafka 消息中密钥的意义

    我们知道，Kafka 会按照我们生成记录的顺序有效地存储记录流。

    当我们向 Kafka 主题发布消息时，消息会以轮循方式在可用的分区中分发。因此，在一个 Kafka 主题中，消息的顺序在分区内得到保证，但在跨分区时则无法保证。

    当我们将带有密钥的消息发布到 Kafka 主题时，所有带有相同密钥的消息都会被 Kafka 保证存储在同一个分区中。因此，如果我们想保持具有相同密钥的消息的顺序，Kafka 消息中的密钥就非常有用。

    总而言之，向 Kafka 发送消息时，密钥并不是必须的。基本上，如果我们希望对具有相同密钥的消息保持严格的顺序，那么就一定要在消息中使用密钥。在其他情况下，使用空键可以更好地在分区之间分配消息。

    接下来，让我们直接深入研究一些带有键的 Kafka 消息的实现代码。

3. 设置

    在开始之前，让我们先初始化一个 Kafka 集群，设置依赖关系，并初始化与 Kafka 集群的连接。

    Kafka 的 Java 库提供了易于使用的生产者（Producer）和消费者（Consumer）API，我们可以用它来发布和消费 Kafka 的消息。

    1. 依赖关系

        首先，让我们在项目的 pom.xml 文件中添加 Kafka 客户端 Java 库的 Maven 依赖项：

        ```xml
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>3.4.0</version>
        </dependency>
        ```

    2. 集群和主题初始化

        其次，我们需要一个运行中的 Kafka 集群，以便连接并执行各种 Kafka 操作。本指南假设 Kafka 集群以默认配置运行在本地系统上。

        最后，我们将创建一个带有多个分区的 Kafka 主题，用于发布和消费消息。参考 Kafka [主题创建指南](https://www.baeldung.com/kafka-topic-creation)，让我们创建一个名为 "baeldung" 的主题：

        ```java
        Properties adminProperties = new Properties();
        adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        Admin admin = Admin.create(adminProperties);
        ```

        在此，我们使用 Properties 实例定义的基本配置创建了一个 Kafka Admin 实例。接下来，我们将使用此 Admin 实例创建一个名为 "baeldung" 包含五个分区的主题：

        `admin.createTopics(Collections.singleton(new NewTopic("baeldung", 5, (short) 1)));`

        现在我们已经用一个主题初始化了 Kafka 集群设置，让我们用一个密钥发布一些消息。

4. 使用密钥发布消息

    为了演示我们的编码示例，我们将首先创建一个 KafkaProducer 实例，其中包含一些由 Properties 实例定义的基本生产者属性。接下来，我们将使用创建的 KafkaProducer 实例发布带有密钥的消息，并验证主题分区。

    让我们深入探讨一下每个步骤的细节。

    1. 初始化生产者

        首先，让我们创建一个新的 Properties 实例，保存生产者的属性，以便连接到本地代理：

        ```java
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        ```

        然后，让我们使用创建的生产者属性实例创建一个 KafkaProducer 实例：

        `KafkaProducer <String, String> producer = new KafkaProducer<>(producerProperties);`

        KafkaProducer 类的构造函数接受一个 Properties 对象（或 Map），并返回一个 KafkaProducer 实例。

    2. 发布消息

        Kafka Publisher API 提供了多个构造函数，用于创建带有密钥的 ProducerRecord 实例。我们使用 `ProducerRecord<K,V>(String topic, K key, V value)` 构造函数来创建带 key 的消息：

        `ProducerRecord<String, String> record = new ProducerRecord<>("baeldung", "message-key", "Hello World");`

        在这里，我们为 "baeldung" 主题创建了一个带有关键字的 ProducerRecord 实例。

        现在，让我们向 Kafka 主题发布几条消息并验证分区：

        ```java
        for (int i = 1; i <= 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("baeldung", "message-key", String.valueOf(i));
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();
            logger.info(String.valueOf(metadata.partition()));
        }
        ```

        我们使用 `KafkaProducer.send(ProducerRecord<String, String> record)` 方法向 Kafka 发布消息。该方法返回 RecordMetadata 类型的 [Future](https://www.baeldung.com/java-future) 实例。然后，我们使用阻塞调用 `Future<RecordMetadata>.get()` 方法，该方法会在消息发布时返回一个 RecordMetadata 实例。

        接下来，我们使用 RecordMetadata.partition() 方法获取消息的分区。

        上述代码段会产生以下记录结果：

        ```log
        1
        1
        1
        1
        1
        1
        1
        1
        1
        1
        ```

        由此，我们验证了使用相同密钥发布的信息都发布到了同一个分区。

5. 结论

    在本文中，我们了解了 Kafka 消息中密钥的重要性。

    我们首先了解了如何将带有密钥的消息发布到主题中。然后，我们讨论了如何验证具有相同密钥的消息是否发布到了同一个分区。
