# [获取 Apache Kafka 主题中的消息数量](https://www.baeldung.com/java-kafka-count-topic-messages)

1. 概述

    Apache Kafka 是一个开源的分布式事件流平台。

    在本快速教程中，我们将学习获取 Kafka 主题中消息数量的技术。我们将演示编程和本地命令技术。

2. 编程技术

    一个 Kafka 主题可能有多个分区。我们的技术应该确保我们已经计算了每个分区的消息数量。

    我们必须逐个分区检查它们的最新偏移量。为此，我们将引入一个消费者：

    `KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);`

    第二步是从消费者那里获取所有分区：

    ```java
    List<TopicPartition> partitions = consumer.partitionsFor(topic).stream().map(p -> new TopicPartition(topic, p.partition()))
        .collect(Collectors.toList());
    ```

    第三步是在每个分区结束时偏移消费者，并将结果记录在分区地图中：

    ```java
    consumer.assign(partitions);
    consumer.seekToEnd(Collections.emptySet());
    Map<TopicPartition, Long> endPartitions = partitions.stream().collect(Collectors.toMap(Function.identity(), consumer::position));
    ```

    最后一步是取每个分区中的最后一个位置，求和后得到主题中的消息数：

    `numberOfMessages = partitions.stream().mapToLong(p -> endPartitions.get(p)).sum();`

3. Kafka 本地命令

    如果我们想对 Kafka 主题上的消息数量执行一些自动化任务，程序化技术是很好的选择。不过，如果只是为了分析目的，那么创建这些服务并在机器上运行它们将是一笔开销。一个直接的选择是使用本地 Kafka 命令。这样可以快速得到结果。

    1. 使用 GetoffsetShell 命令

        在执行本地命令之前，我们必须先导航到机器上的 Kafka 根目录。以下命令将返回主题 baeldung 上正在发布的消息数量：

        ```bash
        $ bin/kafka-run-class.sh kafka.tools.GetOffsetShell   --broker-list localhost:9092   
        --topic baeldung   | awk -F  ":" '{sum += $3} END {print "Result: "sum}'
        Result: 3
        ```

    2. 使用消费者控制台

        如前所述，在执行任何命令之前，我们都要先导航到 Kafka 的根文件夹。以下命令将返回主题 baeldung 上正在发布的消息数量：

        ```bash
        $ bin/kafka-console-consumer.sh  --from-beginning  --bootstrap-server localhost:9092 
        --property print.key=true --property print.value=false --property print.partition 
        --topic baeldung --timeout-ms 5000 | tail -n 10|grep "Processed a total of"
        Processed a total of 3 messages
        ```

4. 结论

    在本文中，我们研究了获取 Kafka 主题中消息数量的技术。我们学习了一种将所有分区分配给消费者并检查最新偏移量的编程技术。

    我们还学习了两种本地 Kafka 命令技术。一个是 Kafka 工具中的 GetoffsetShell 命令。另一个是在控制台上运行一个消费者，并从头开始打印消息的数量。
