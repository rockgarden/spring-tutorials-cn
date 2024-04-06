# [用Java在Kafka中进行精确一次处理](https://www.baeldung.com/kafka-exactly-once)

1. 概述

    在本教程中，我们将了解 Kafka 如何通过新引入的事务 API（Transactional API）确保生产者和消费者应用程序之间的一次交付。

    此外，我们还将在 WordCount 示例中使用该 API 实现事务生产者和消费者，以实现端到端的精确一次交付。

2. Kafka 中的消息交付

    由于各种故障，消息传递系统无法保证生产者和消费者应用程序之间的消息传递。根据客户端应用程序与此类系统的交互方式，可能会出现以下消息语义：

    - 如果消息传递系统永远不会重复消息，但可能会偶尔错过消息，我们称之为最多一次(at-most-once)
    - 或者，如果一个消息传递系统永远不会错过一条消息，但偶尔会重复一条消息，我们称之为最少一次(at-least-once)
    - 但是，如果它总是不重复地传递所有消息，那就是精确地传递一次(actly-once)

    最初，Kafka 只支持最多一次和最少一次消息传递。

    然而，在 Kafka 中间商和客户端应用程序之间引入的事务（Transactions）功能确保了 Kafka 的精确一次性传递。为了更好地理解这一点，让我们快速回顾一下事务客户端 API。

3. Maven 依赖项

    要使用事务 API，我们的 pom 中需要 Kafka 的 Java 客户端：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.4.0</version>
    </dependency>
    ```

4. 事务消耗-转换-产生循环

    在我们的示例中，我们将从输入主题 "句子(sentences)" 中消费消息。

    然后，对于每个句子，我们将计算每个单词，并将单个单词计数发送到输出主题计数。

    在示例中，我们假设句子主题中已经有事务数据。

    1. 事务感知生产者

        让我们先添加一个典型的 Kafka 生产者。

        ```java
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "localhost:9092");
        ```

        此外，我们还需要指定一个事务.id 并启用empotence：

        ```java
        producerProps.put("enable.idempotence", "true");
        producerProps.put("transactional.id", "prod-1");
        KafkaProducer<String, String> producer = new KafkaProducer(producerProps);
        ```

        因为我们已经启用了惰性，所以 Kafka 会使用这个事务 ID 作为其算法的一部分，来重复该生产者发送的任何消息，从而确保惰性。

        简单地说，如果生产者不小心向 Kafka 发送了不止一次相同的消息，这些设置就能让它察觉到。

        我们需要做的就是确保每个生产者的事务 ID 都是不同的，而且在重启时保持一致。

    2. 为事务启用生产者

        准备就绪后，我们还需要调用 initTransaction，让生产者做好使用事务的准备：

        `producer.initTransactions();`

        这样，生产者就会向经纪商注册为可以使用事务的生产者，并通过其事务 ID 和序列号（或纪元）来识别它。反过来，中间商将使用这些信息把任何操作写入事务日志。

        因此，中间商会删除日志中属于具有相同事务 ID 和更早时间的生产者的任何操作，并假定它们来自已失效的事务。

    3. 事务感知消费者

        当我们消费时，我们可以按顺序读取一个主题分区上的所有消息。不过，我们可以用 isolation.level 来表示，我们应该等到相关事务提交后再读取事务消息：

        ```java
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "my-group-id");
        consumerProps.put("enable.auto.commit", "false");
        consumerProps.put("isolation.level", "read_committed");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(singleton(“sentences”));
        ```

        使用 read_committed 的值可确保我们在事务完成前不读取任何事务消息。

        isolation.level 的默认值是 read_uncommitted。

    4. 按事务消费和转换

        既然生产者和消费者都已配置为以事务方式写入和读取，我们就可以从输入主题中消费记录，并计算每条记录中的每个单词：

        ```java
        ConsumerRecords<String, String> records = consumer.poll(ofSeconds(60));
        Map<String, Integer> wordCountMap =
        records.records(new TopicPartition("input", 0))
            .stream()
            .flatMap(record -> Stream.of(record.value().split(" ")))
            .map(word -> Tuple.of(word, 1))
            .collect(Collectors.toMap(tuple -> 
            tuple.getKey(), t1 -> t1.getValue(), (v1, v2) -> v1 + v2));
        ```

        请注意，上述代码没有任何事务性内容。但是，由于我们使用了 read_committed，这意味着在同一事务中被写入输入主题的消息在全部写完之前都不会被该消费者读取。

        现在，我们可以将计算出的字数发送到输出主题。

        让我们看看如何以事务处理的方式生成结果。

    5. 发送 API

        要在同一事务中以新消息形式发送计算结果，我们需要调用 beginTransaction：

        `producer.beginTransaction();`

        然后，我们可以将每条信息写入我们的 "counts" 主题，关键字为单词，计数为值：

        ```java
        wordCountMap.forEach((key,value) -> 
            producer.send(new ProducerRecord<String,String>("counts",key,value.toString())));
        ```

        请注意，由于生产者可以按键划分数据，这意味着事务消息可以跨越多个分区，每个分区由不同的消费者读取。因此，Kafka 代理将存储一个事务的所有更新分区列表。

        还要注意的是，在一个事务中，生产者可以使用多个线程并行发送记录。

    6. 提交偏移量

        最后，我们需要提交刚消费完的偏移量。在事务中，我们会像往常一样将偏移量提交回我们读取它们的输入主题。不过，我们也会将它们发送到生产者的事务中。

        我们可以在一次调用中完成所有这些操作，但首先需要计算每个主题分区的偏移量：

        ```java
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<String, String>> partitionedRecords = records.records(partition);
            long offset = partitionedRecords.get(partitionedRecords.size() - 1).offset();
            offsetsToCommit.put(partition, new OffsetAndMetadata(offset + 1));
        }
        ```

        请注意，我们提交给事务的是即将到来的偏移量，这意味着我们需要加 1。

        然后，我们就可以向事务发送计算出的偏移量了：

        `producer.sendOffsetsToTransaction(offsetsToCommit, "my-group-id");`

    7. 提交或中止事务

        最后，我们可以提交事务，将偏移量以原子方式写入 consumer_offsets 主题和事务本身：

        `producer.commitTransaction();`

        这将把缓冲消息刷新到相应的分区。此外，Kafka 代理会将该事务中的所有消息提供给消费者。

        当然，如果在处理过程中出现任何问题，例如捕获到异常，我们可以调用 abortTransaction：

        ```java
        try {
        // ... read from input topic
        // ... transform
        // ... write to output topic
        producer.commitTransaction();
        } catch ( Exception e ) {
        producer.abortTransaction();
        }
        ```

        然后丢弃所有缓冲消息，并从 broker 中删除事务。

        如果我们在代理配置的 max.transaction.timeout.ms 之前既不提交也不中止，Kafka 代理将自行中止事务。该属性的默认值是 900,000 毫秒或 15 分钟。

5. 其他消耗-转换-产生循环

    我们刚才看到的是一个基本的消耗-转换-产生(consume-transform-produce)循环，它读取和写入同一个 Kafka 集群。

    相反，必须读取和写入不同 Kafka 集群的应用程序必须使用较旧的 commitSync 和 commitAsync API。通常，应用程序会将消费者偏移量存储到外部状态存储中，以保持事务性。

6. 结论

    对于数据关键型应用程序来说，端到端精确一次性处理通常是必须的。

    在本教程中，我们介绍了如何使用 Kafka 通过事务来实现这一点，并通过一个基于事务的单词计数示例来说明这一原理。
