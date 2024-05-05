# [清除 Apache Kafka 主题指南](https://www.baeldung.com/kafka-purge-topic)

1. 概述

    在本文中，我们将探讨从 Apache Kafka 主题中清除数据的几种策略。

2. 清理场景

    在学习清理数据的策略之前，让我们先熟悉一下需要进行清理活动的简单场景。

    1. 场景

        Apache Kafka 中的消息会在配置的保留时间后自动过期。不过，在某些情况下，我们可能希望立即删除消息。

        假设在 Kafka 主题中产生消息的应用代码中引入了一个缺陷。当漏洞修复被集成时，Kafka 主题中已经有许多损坏的消息可供使用。

        这种问题在开发环境中最常见，我们希望快速得到结果。因此，批量删除消息是一种合理的做法。

    2. 模拟

        为了模拟场景，我们先从 Kafka 安装目录中创建一个 purge-scenario 主题：

        ```bash
        $ bin/kafka-topics.sh \
        --create --topic purge-scenario --if-not-exists \
        --partitions 2 --replication-factor 1 \
        --zookeeper localhost:2181
        ```

        接下来，让我们使用 shuf 命令生成随机数据，并将其输入 kafka-console-producer.sh 脚本：

        ```bash
        $ /usr/bin/shuf -i 1-100000 -n 50000000 \
        | tee -a /tmp/kafka-random-data \
        | bin/kafka-console-producer.sh \
        --bootstrap-server=0.0.0.0:9092 \
        --topic purge-scenario
        ```

        我们必须注意，我们使用了 [tee](https://www.baeldung.com/linux/tee-command) 命令来保存模拟数据，以便日后使用。

        最后，让我们验证一下消费者是否可以从主题中消费消息：

        ```bash
        $ bin/kafka-console-consumer.sh \
        --bootstrap-server=0.0.0.0:9092 \
        --from-beginning --topic purge-scenario \
        --max-messages 3
        76696
        49425
        1744
        Processed a total of 3 messages
        ```

3. 消息过期

    在 purge-scenario 主题中生成的报文默认保留期为七天。要清除消息，我们可以暂时将 retention.ms 主题级属性重置为十秒，然后等待消息过期：

    ```bash
    $ bin/kafka-configs.sh --alter \
    --add-config retention.ms=10000 \
    --bootstrap-server=0.0.0.0:9092 \
    --topic purge-scenario \
    && sleep 10
    ```

    接下来，让我们验证一下主题中的消息是否已经过期：

    ```txt
    $ bin/kafka-console-consumer.sh  \
    --bootstrap-server=0.0.0.0:9092 \
    --from-beginning --topic purge-scenario \
    --max-messages 1 --timeout-ms 1000
    [2021-02-28 11:20:15,951] ERROR Error processing message, terminating consumer process:  (kafka.tools.ConsoleConsumer$)
    org.apache.kafka.common.errors.TimeoutException
    Processed a total of 0 messages
    ```

    最后，我们可以为主题恢复原来的 7 天保留期：

    ```bash
    $ bin/kafka-configs.sh --alter \
    --add-config retention.ms=604800000 \
    --bootstrap-server=0.0.0.0:9092 \
    --topic purge-scenario
    ```

    通过这种方法，Kafka 将在所有分区中为 purge-scenario 主题清除消息。

4. 选择性删除记录

    有时，我们可能想要有选择地删除特定主题中一个或多个分区内的记录。我们可以使用 kafka-delete-records.sh 脚本来满足这种要求。

    首先，我们需要在 delete-config.json 配置文件中指定分区级偏移量。

    让我们使用 offset=-1 来清除 partition=1 中的所有消息：

    ```json
    {
    "partitions": [
        {
        "topic": "purge-scenario",
        "partition": 1,
        "offset": -1
        }
    ],
    "version": 1
    }
    ```

    接下来，让我们继续删除记录：

    ```bash
    $ bin/kafka-delete-records.sh \
    --bootstrap-server localhost:9092 \
    --offset-json-file delete-config.json
    ```

    我们可以验证我们仍然能够从 partition=0 读取数据：

    ```bash
    $ bin/kafka-console-consumer.sh \
    --bootstrap-server=0.0.0.0:9092 \
    --from-beginning --topic purge-scenario --partition=0 \
    --max-messages 1 --timeout-ms 1000
    44017
    Processed a total of 1 messages
    ```

    然而，当我们从 partition=1 读取时，将没有记录需要处理：

    ```bash
    $ bin/kafka-console-consumer.sh \
    --bootstrap-server=0.0.0.0:9092 \
    --from-beginning --topic purge-scenario \
    --partition=1 \
    --max-messages 1 --timeout-ms 1000
    [2021-02-28 11:48:03,548] ERROR Error processing message, terminating consumer process:  (kafka.tools.ConsoleConsumer$)
    org.apache.kafka.common.errors.TimeoutException
    Processed a total of 0 messages
    ```

5. 删除并重新创建主题

    清除 Kafka 主题中所有消息的另一个变通方法是删除并重新创建主题。不过，这只有在启动 Kafka 服务器时将 delete.topic.enable 属性设置为 true 时才能实现：

    `$ bin/kafka-server-start.sh config/server.properties --override delete.topic.enable=true`

    要删除主题，我们可以使用 kafka-topics.sh 脚本：

    ```bash
    $ bin/kafka-topics.sh \
    --delete --topic purge-scenario \
    --zookeeper localhost:2181
    Topic purge-scenario is marked for deletion.
    Note: This will have no impact if delete.topic.enable is not set to true.
    ```

    注意：如果 delete.topic.enable 未设置为 true，则不会产生任何影响。
    让我们列出主题来验证一下：

    `$ bin/kafka-topics.sh --zookeeper localhost:2181 --list`

    确认主题不再列出后，我们就可以继续重新创建它了。

6. 结论

    在本教程中，我们模拟了一个需要清除 Apache Kafka 主题的场景。此外，我们还探索了多种策略来完全或有选择地跨分区清除主题。
