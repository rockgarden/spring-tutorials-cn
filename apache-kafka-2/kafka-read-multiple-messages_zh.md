# 使用 Apache Kafka 读取多条消息

[Data](https://www.baeldung.com/category/data)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    在本教程中，我们将探讨 Kafka Consumer 如何从代理中检索消息。我们将了解直接影响 Kafka Consumer 一次读取多少消息的可配置属性。最后，我们将探讨调整这些设置会如何影响 Consumer 的行为。

2. 设置环境

    Kafka 消费者以可配置大小的批次获取给定分区的记录。我们无法配置在一个批次中获取记录的确切数量，但可以配置这些批次的大小（以字节为单位）。

    对于本文中的代码片段，我们需要一个简单的 Spring 应用程序，使用 [kafka-clients](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients) 库与 Kafka 代理交互。我们将创建一个 Java 类，在内部使用 KafkaConsumer 来订阅主题并记录传入的消息。如果你想深入了解，可以阅读我们专门介绍 [Kafka Consumer API](https://www.baeldung.com/kafka-create-listener-consumer-api) 的文章，然后跟上我们的脚步。

    在我们的示例中，日志记录是关键区别之一： 我们不再一次记录一条消息，而是将它们收集起来，记录整批消息。这样，我们就能清楚地看到每次 poll() 抓取了多少条消息。此外，我们还可以在日志中加入批次的初始和最终偏移量以及消费者的 groupId 等详细信息：

    src/.kafka.consumer/VariableFetchSizeKafkaListener.java

    [Testcontainers](https://www.baeldung.com/docker-test-containers) 库将通过启动一个带有运行中的 Kafka 代理的 Docker 容器来帮助我们设置测试环境。如果你想了解有关设置 Testcontainer 的 Kafka 模块的更多信息，请点击此处查看我们是[如何配置测试环境](https://www.baeldung.com/kafka-create-listener-consumer-api#testing)的，然后跟上我们的脚步。

    在我们的特殊情况下，我们可以定义一个额外的方法，在给定的主题上发布多条消息。例如，假设我们要将温度传感器读取的值流式传输到名为 "engine.sensor.temperature" 的主题上：

    ```java
    void publishTestData(int recordsCount, String topic) {
        List<ProducerRecord<String, String>> records = IntStream.range(0, recordsCount)
        .mapToObj(__ -> new ProducerRecord<>(topic, "key1", "temperature=255F"))
        .collect(toList());
        // publish all to kafka
    }
    ```

    我们可以看到，所有消息都使用了相同的键。因此，所有记录都将发送到同一个分区。对于有效载荷，我们使用了一个简短、固定的文本来描述温度测量值。

3. 测试默认行为

    让我们先使用默认的消费者配置创建一个 Kafka 监听器。然后，我们将发布一些消息，看看我们的监听器消耗了多少批次。正如我们所见，我们的自定义监听器在内部使用了消费者 API。因此，要实例化 VariableFetchSizeKafkaListener，我们必须先配置并创建一个 KafkaConsumer：

    `src/.kafka.consumer/CustomKafkaListener.java:defaultKafkaConsumer()`

    目前，我们将使用 KafkaConsumer 的默认值作为最小和最大获取大小。基于这个消费者，我们可以实例化监听器并异步运行它，以避免阻塞主线程：

    ```java
    CompletableFuture.runAsync(
    new VariableFetchSizeKafkaListener(topic, kafkaConsumer)
    );
    ```

    最后，让我们阻塞测试线程几秒钟，给监听器一些时间来读取消息。本文的目的是启动监听器并观察它们的运行情况。我们将使用 Junit5 测试作为设置和探索其行为的便捷方法，但为了简单起见，我们不会包含任何特定的断言。因此，这将是我们的起始 @Test：

    test/.kafka.consumer/VariableFetchSizeKafkaListenerLiveTest.java:whenUsingDefaultConfiguration_thenProcessInBatchesOf()

    现在，让我们运行测试并检查日志，看看一次批处理将获取多少条记录：

    `10:48:46.958 [ForkJoinPool.commonPool-worker-2] INFO  c.b.k.c.VariableFetchSizeKafkaListener - groupId: default_config, poll: #1, fetched: #300 records, offsets: 0 -> 299`

    我们可以看到，由于我们的信息量很小，所以我们一次就获取了所有 300 条记录。密钥和正文都是短字符串：密钥长度为 4 个字符，正文长度为 16 个字符。总共 20 个字节，外加一些记录元数据。另一方面，最大批处理大小的默认值为 1 mebibyte（1.024 x 1.024 字节），即 1,048,576 字节。

4. 配置最大分区获取大小

    Kafka 中的 "max.partition.fetch.bytes" 决定了消费者在单个请求中从单个分区获取的最大数据量。因此，即使是少量的短消息，我们也可以通过更改该属性来强制监听器分多批获取记录。

    为了观察这一点，让我们再创建两个 VariableFetchSizeKafkaListeners，并将它们分别配置为仅 500B 和 5KB。首先，让我们在一个专用方法中提取所有常用的消费者属性，以避免代码重复：

    test/.kafka.consumer/VariableFetchSizeKafkaListenerLiveTest.java:commonConsumerProperties()

    然后，让我们创建第一个监听器并异步运行它：

    ```java
    Properties fetchSize_500B = commonConsumerProperties();
    fetchSize_500B.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "max_fetch_size_500B");
    fetchSize_500B.setProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "500");
    CompletableFuture.runAsync(
    new VariableFetchSizeKafkaListener("engine.sensors.temperature", new KafkaConsumer<>(fetchSize_500B))
    );
    ```

    正如我们所看到的，我们为不同的监听器设置了不同的消费者组 ID。这将允许它们消费相同的测试数据。现在，让我们继续使用第二个监听器并完成测试：

    test/.kafka.consumer/VariableFetchSizeKafkaListenerLiveTest.java:whenChangingMaxPartitionFetchBytesProperty_thenAdjustBatchSizesWhilePolling()

    如果运行这个测试，我们可以假设第一个消费者获取的批次大约是第二个消费者的十倍。让我们分析一下日志：

    ```log
    [worker-3] INFO - groupId: max_fetch_size_5KB, poll: #1, fetched: #56 records, offsets: 0 -> 55
    [worker-2] INFO - groupId: max_fetch_size_500B, poll: #1, fetched: #5 records, offsets: 0 -> 4
    [worker-2] INFO - groupId: max_fetch_size_500B, poll: #2, fetched: #5 records, offsets: 5 -> 9
    [worker-3] INFO - groupId: max_fetch_size_5KB, poll: #2, fetched: #56 records, offsets: 56 -> 111
    [worker-2] INFO - groupId: max_fetch_size_500B, poll: #3, fetched: #5 records, offsets: 10 -> 14
    [worker-3] INFO - groupId: max_fetch_size_5KB, poll: #3, fetched: #56 records, offsets: 112 -> 167
    [worker-2] INFO - groupId: max_fetch_size_500B, poll: #4, fetched: #5 records, offsets: 15 -> 19
    [worker-3] INFO - groupId: max_fetch_size_5KB, poll: #4, fetched: #51 records, offsets: 168 -> 218
    [worker-2] INFO - groupId: max_fetch_size_500B, poll: #5, fetched: #5 records, offsets: 20 -> 24
    [...]
    ```

    不出所料，其中一个监听器获取的数据量几乎是另一个监听器的十倍。此外，重要的是要了解批次内的记录数取决于这些记录及其元数据的大小。为了突出这一点，我们可以观察到，在第四次轮询时，groupId 为 "max_fetch_size_5KB" 的消费者获取的记录更少。

5. 配置最小撷取大小

    消费者 API 还允许通过 "fetch.min.bytes" 属性自定义最小获取大小。我们可以更改该属性，指定代理需要响应的最小数据量。如果没有达到这个最小值，代理在发送对消费者获取请求的响应之前会等待更长的时间。为了强调这一点，我们可以在测试辅助方法中为测试发布者添加延迟。这样，生产者将在发送每条消息之间等待特定的毫秒数：

    test/.kafka.consumer/VariableFetchSizeKafkaListenerLiveTest.java:whenChangingMinFetchBytesProperty_thenAdjustWaitTimeWhilePolling()

    test/.kafka.consumer/VariableFetchSizeKafkaListenerLiveTest.java:publishTestData()

    让我们先创建一个 VariableFetchSizeKafkaListener，它将使用默认配置，"fetch.min.bytes" 等于一个字节。与之前的示例类似，我们将在 CompletableFuture 中异步运行此消费者：

    ```java
    // fetch.min.bytes = 1 byte (default)
    Properties minFetchSize_1B = commonConsumerProperties();
    minFetchSize_1B.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "min_fetch_size_1B");
    CompletableFuture.runAsync(
    new VariableFetchSizeKafkaListener(topic, new KafkaConsumer<>(minFetchSize_1B))
    );
    ```

    有了这样的设置，再加上我们引入的延迟，我们可以预期每条记录都会被逐一检索。换句话说，我们可以预期一条记录会有很多批次。此外，我们还希望这些批次的消耗速度与 KafkaProducer 发布数据的速度相似，在我们的情况下是每 100 毫秒一次。让我们运行测试并分析日志：

    ```log
    14:23:22.368 [worker-2] INFO - groupId: min_fetch_size_1B, poll: #1, fetched: #1 records, offsets: 0 -> 0
    14:23:22.472 [worker-2] INFO - groupId: min_fetch_size_1B, poll: #2, fetched: #1 records, offsets: 1 -> 1
    14:23:22.582 [worker-2] INFO - groupId: min_fetch_size_1B, poll: #3, fetched: #1 records, offsets: 2 -> 2
    14:23:22.689 [worker-2] INFO - groupId: min_fetch_size_1B, poll: #4, fetched: #1 records, offsets: 3 -> 3
    [...]
    ```

    此外，我们还可以通过将 "fetch.min.bytes" 值调整到更大，迫使消费者等待更多数据的积累：

    ```java
    // fetch.min.bytes = 500 bytes
    Properties minFetchSize_500B = commonConsumerProperties();
    minFetchSize_500B.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "mim_fetch_size_500B");
    minFetchSize_500B.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "500");
    CompletableFuture.runAsync(
    new VariableFetchSizeKafkaListener(topic, new KafkaConsumer<>(minFetchSize_500B))
    );
    ```

    将属性设置为 500 字节后，我们可以预计消费者会等待更长时间并获取更多数据。让我们也运行这个示例并观察结果：

    ```log
    14:24:49.303 [worker-3] INFO - groupId: mim_fetch_size_500B, poll: #1, fetched: #6 records, offsets: 0 -> 5
    14:24:49.812 [worker-3] INFO - groupId: mim_fetch_size_500B, poll: #2, fetched: #4 records, offsets: 6 -> 9
    14:24:50.315 [worker-3] INFO - groupId: mim_fetch_size_500B, poll: #3, fetched: #5 records, offsets: 10 -> 14
    14:24:50.819 [worker-3] INFO - groupId: mim_fetch_size_500B, poll: #4, fetched: #5 records, offsets: 15 -> 19
    [...]
    ```

6. 结论

    在本文中，我们讨论了 Kafka 消费者从代理获取数据的方式。我们了解到，默认情况下，如果至少有一条新记录，消费者就会获取数据。另一方面，如果来自分区的新数据超过 1,048,576 字节，它就会被分割成最大大小为 1,048,576 字节的多个批次。我们发现，自定义 "fetch.min.bytes" 和 "max.partition.fetch.bytes" 属性可以让我们定制 Kafka 的行为，以满足我们的特定需求。

    一如既往，我们可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/apache-kafka-2) 上获取示例的源代码。
