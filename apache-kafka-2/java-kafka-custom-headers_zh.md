# [为Kafka报文添加自定义标题](https://www.baeldung.com/java-kafka-custom-headers)

1. 简介

    Apache Kafka 是一个开源的分布式事件存储和容错流处理系统。Kafka 基本上是一个事件流平台，客户端可以发布和订阅事件流。一般来说，生产者应用程序向 Kafka 发布事件，而消费者订阅这些事件，从而实现发布者-订阅者模型。

    在本教程中，我们将学习如何使用 Kafka 生产者在 Kafka 消息中添加自定义标题。

2. 设置

    Kafka 提供了一个易于使用的 Java 库，我们可以用它来创建 Kafka 生产者客户端（Producers）和消费者客户端（Consumer）。

    1. 依赖关系

        首先，让我们在项目的 pom.xml 文件中添加 Kafka 客户端 Java 库的 Maven 依赖项：

        ```xml
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>3.4.0</version>
        </dependency>
        ```

    2. 连接初始化

        本指南假定我们的本地系统上运行着 Kafka 集群。此外，我们还需要创建一个主题并与 Kafka 集群建立连接。

        首先，让我们在集群中创建一个 Kafka 主题。我们可以参考 Kafka 主题创建指南来创建主题 "baeldung"。

        其次，让我们创建一个新的 Properties 实例，其中包含将生产者连接到本地代理所需的最低配置：

        ```java
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        ```

        最后，让我们创建一个 KafkaProducer 实例，用来发布消息：

        `KafkaProducer <String, String> producer = new KafkaProducer<>(producerProperties);`

        KafkaProducer 类的构造函数接受一个带有 [bootstrap.servers](https://kafka.apache.org/documentation/#adminclientconfigs_bootstrap.servers) 属性的 Properties 对象（或 Map），并返回一个 KafkaProducer 实例。

        类似地，让我们创建一个 KafkaConsumer 实例，用来消费消息：

        ```java
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "ConsumerGroup1");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        ```

        我们将使用这些生产者和消费者实例来演示所有的编码示例。

        现在我们已经配置了所有必要的依赖关系和连接，可以编写一个简单的应用程序，在 Kafka 消息中添加自定义头信息。

3. 使用自定义头发布消息

    在 Kafka 0.11.0.0 版本中添加了对 Kafka 消息中自定义标题的支持。 要创建一条 Kafka 消息（Record），我们需要创建一个 `ProducerRecord<K,V>` 的实例。ProducerRecord 基本上标识了消息值、消息要发布的主题以及其他元数据。

    ProducerRecord 类提供了各种构造函数，用于向 Kafka 消息添加自定义头。让我们来看看我们可以使用的几个构造函数：

    - `ProducerRecord(String topic, Integer partition, K key, V value, Iterable<Header> headers)`
    - `ProducerRecord(String topic, Integer partition, Long timestamp, K key, V value, Iterable<Header> headers)`

    ProducerRecord 类的两个构造函数都接受 `Iterable<Header>` 类型形式的自定义头信息。

    为了理解这一点，让我们创建一个 ProducerRecord，向 "baeldung" 主题发布一条消息和一些自定义头信息：

    ```java
    List <Header> headers = new ArrayList<>();
    headers.add(new RecordHeader("website", "baeldung.com".getBytes()));
    ProducerRecord <String, String> record = new ProducerRecord <>("baeldung", null, "message", "Hello World", headers);
    producer.send(record);
    ```

    在这里，我们创建了一个头信息类型列表，作为头信息传递给构造函数。每个头代表 `RecordHeader(String key, byte[] value)` 的一个实例，它接受一个字符串作为头的关键字，并接受一个字节数组作为头的值。

    类似地，我们可以使用第二个构造函数，该构造函数还接受正在发布的记录的时间戳：

    ```java
    List <Header> headers = new ArrayList<>();
    headers.add(new RecordHeader("website", "baeldung.com".getBytes()));
    ProducerRecord <String, String> record = new ProducerRecord <>("baeldung", null, System.currentTimeMillis(), "message", "Hello World", headers);
    producer.send(record);
    ```

    至此，我们创建了一条带有自定义标题的消息，并将其发布到 Kafka。

    接下来，让我们实现消费者代码，以消费消息并验证其自定义头信息。

4. 使用自定义报文头消费报文

    首先，我们将消费者实例订阅到 Kafka 主题 "baeldung"，以便从该主题消费消息：

    `consumer.subscribe(Arrays.asList("baeldung"));`

    其次，我们使用轮询机制从 Kafka 轮询新消息：

    `ConsumerRecords<String, String> records = consumer.poll(Duration.ofMinutes(1));`

    KafkaConsumer.poll(Duration duration) 方法轮询 Kafka 主题中的新消息，直到 Duration 参数指定的时间为止。该方法返回一个 ConsumerRecords 实例，其中包含获取的消息。ConsumerRecords 基本上是 ConsumerRecord 类型的 Iterable 实例。

    最后，我们在获取的记录中循环，获取每条消息的自定义标题：

    ```java
    for (ConsumerRecord<String, String> record : records) {
        System.out.println(record.key());
        System.out.println(record.value());

        Headers consumedHeaders = record.headers();
        for (Header header : consumedHeaders) {
            System.out.println(header.key());
            System.out.println(new String(header.value()));
        }
    }
    ```

    在此，我们使用 ConsumerRecord 类中的各种 getter 方法来获取消息键、值和自定义头信息。ConsumerRecord.headers() 方法返回一个包含自定义头信息的 Headers 实例。Headers 基本上是 Header 类型的 Iterable 实例。然后，我们在每个 Header 实例中循环，并分别使用 Header.key() 和 Header.value() 方法获取头信息的键和值。

5. 结论

    在本文中，我们学习了如何向 Kafka 消息添加自定义头。我们了解了接受自定义头信息的不同构造函数及其相应的实现。

    然后，我们了解了如何使用带有自定义标头的消息并验证它们。
