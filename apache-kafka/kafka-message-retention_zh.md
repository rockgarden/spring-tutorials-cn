# 在 Apache Kafka 中配置消息保留期

1. 概述

    当生产者向 Apache Kafka 发送消息时，它会将消息附加到日志文件中，并在配置的期限内保留该消息。

    在本教程中，我们将学习为 Kafka 主题配置基于时间的消息保留属性。

2. 基于时间的保留

    有了保留期属性，消息就有了 TTL（生存时间）。过期后，消息将被标记为删除，从而释放磁盘空间。

    相同的保留期属性适用于给定 Kafka 主题中的所有消息。此外，我们既可以在创建主题前设置这些属性，也可以在运行时更改已存在主题的这些属性。

    在接下来的章节中，我们将学习如何通过代理配置（为新主题设置保留期）和主题级配置（在运行时控制保留期）来调整这些属性。

3. 服务器级配置

    Apache Kafka 支持服务器级别的保留策略，我们可以通过配置三个基于时间的配置属性中的一个来调整该策略：

    - log.retention.hours
    - log.retention.minutes
    - log.retention.ms

    重要的是要明白，Kafka 会用较高的值覆盖较低精度的值。因此，log.retention.ms 的优先级最高。

    1. 基础知识

        首先，让我们在 Apache Kafka 目录下执行 grep 命令，检查保留的默认值：

        ```bash
        $ grep -i 'log.retention.[hms].*\=' config/server.properties
        log.retention.hours=168
        ```

        我们可以注意到，默认的保留时间是七天。

        要只保留十分钟，我们可以在 config/server.properties 中设置 log.retention.minutes 属性的值：

        `log.retention.minutes=10`

    2. 新主题的保留期

        Apache Kafka 软件包包含几个 shell 脚本，我们可以用它们来执行管理任务。我们将使用它们创建一个辅助脚本 functions.sh，并在本教程中使用。

        首先，让我们在 functions.sh 中添加两个函数，分别用于创建主题和描述其配置：

        ```sh
        function create_topic {
            topic_name="$1"
            bin/kafka-topics.sh --create --topic ${topic_name} --if-not-exists \
            --partitions 1 --replication-factor 1 \
            --zookeeper localhost:2181
        }

        function describe_topic_config {
            topic_name="$1"
            ./bin/kafka-configs.sh --describe --all \
            --bootstrap-server=0.0.0.0:9092 \
            --topic ${topic_name}
        }
        ```

        接下来，让我们创建两个独立脚本：create-topic.sh 和 get-topic-retention-time.sh：

        ```sh
        bash-5.1# cat create-topic.sh
        #!/bin/bash
        . ./functions.sh
        topic_name="$1"
        create_topic "${topic_name}"
        exit $?
        ```

        ```sh
        bash-5.1# cat get-topic-retention-time.sh
        #!/bin/bash
        . ./functions.sh
        topic_name="$1"
        describe_topic_config "${topic_name}" | awk 'BEGIN{IFS="=";IRS=" "} /^[ ]*retention.ms/{print $1}'
        exit $?
        ```

        我们必须注意，describe_topic_config 将给出为主题配置的所有属性。因此，我们使用 awk 单行本为 retention.ms 属性添加了一个过滤器。

        最后，让我们[启动 Kafka 环境](https://kafka.apache.org/documentation/#quickstart_startserver)，为一个新的示例主题验证保留期配置：

        ```bash
        bash-5.1# ./create-topic.sh test-topic
        Created topic test-topic.
        bash-5.1# ./get-topic-retention-time.sh test-topic
        retention.ms=600000
        ```

        创建并描述主题后，我们会发现 retention.ms 被设置为 600000（十分钟）。这实际上来自于我们之前在 server.properties 文件中定义的 log.retention.minutes 属性。

4. 主题级配置

    一旦启动 Broker 服务器，log.retention.{hours|minutes|ms} 服务器级属性就会变成只读。另一方面，我们可以访问 retention.ms 属性，并在主题级对其进行调整。

    让我们在 functions.sh 脚本中添加一个配置主题属性的方法：

    ```sh
    function alter_topic_config {
        topic_name="$1"
        config_name="$2"
        config_value="$3"
        ./bin/kafka-configs.sh --alter \
        --add-config ${config_name}=${config_value} \
        --bootstrap-server=0.0.0.0:9092 \
        --topic ${topic_name}
    }
    ```

    然后，我们就可以在 alter-topic-config.sh 脚本中使用它了：

    ```sh
    #!/bin/sh
    . ./functions.sh

    alter_topic_retention_config $1 $2 $3
    exit $?
    ```

    最后，我们将 test-topic 的保留时间设置为五分钟，并验证一下效果：

    ```sh
    bash-5.1# ./alter-topic-config.sh test-topic retention.ms 300000
    Completed updating config for topic test-topic.

    bash-5.1# ./get-topic-retention-time.sh test-topic
    retention.ms=300000
    ```

5. 验证

    到目前为止，我们已经了解了如何配置 Kafka 主题中消息的保留期。现在是验证消息是否真的在保留超时后过期的时候了。

    1. 生产者-消费者

        让我们在 functions.sh 中添加 produce_message 和 consume_message 函数。在内部，它们分别使用 kafka-console-producer.sh 和 kafka-console-consumer.sh 来生产/消费消息：

        ```sh
        function produce_message {
            topic_name="$1"
            message="$2"
            echo "${message}" | ./bin/kafka-console-producer.sh \
            --bootstrap-server=0.0.0.0:9092 \
            --topic ${topic_name}
        }

        function consume_message {
            topic_name="$1"
            timeout="$2"
            ./bin/kafka-console-consumer.sh \
            --bootstrap-server=0.0.0.0:9092 \
            --from-beginning \
            --topic ${topic_name} \
            --max-messages 1 \
            --timeout-ms $timeout
        }
        ```

        我们必须注意，消费者总是从头开始读取消息，因为我们需要一个能读取 Kafka 中任何可用消息的消费者。

        接下来，让我们创建一个独立的消息生产者：

        ```sh
        bash-5.1# cat producer.sh
        #!/bin/sh
        . ./functions.sh
        topic_name="$1"
        message="$2"

        produce_message ${topic_name} ${message}
        exit $?
        ```

        最后，让我们创建一个独立的消息消费者：

        ```sh
        bash-5.1# cat consumer.sh
        #!/bin/sh
        . ./functions.sh
        topic_name="$1"
        timeout="$2"

        consume_message ${topic_name} $timeout
        exit $?
        ```

    2. 消息到期

        现在我们的基本设置已经就绪，让我们制作一条消息，并立即消耗两次：

        ```sh
        bash-5.1# ./producer.sh "test-topic-2" "message1"
        bash-5.1# ./consumer.sh test-topic-2 10000
        message1
        Processed a total of 1 messages
        bash-5.1# ./consumer.sh test-topic-2 10000
        message1
        Processed a total of 1 messages
        ```

        因此，我们可以看到消费者正在重复消耗任何可用的消息。

        现在，让我们引入 5 分钟的睡眠延迟，然后尝试消费消息：

        ```sh
        bash-5.1# sleep 300 && ./consumer.sh test-topic 10000
        [2021-02-06 21:55:00,896] ERROR Error processing message, terminating consumer process:  (kafka.tools.ConsoleConsumer$)
        org.apache.kafka.common.errors.TimeoutException
        Processed a total of 0 messages
        ```

        正如所料，消费者没有找到任何要消费的消息，因为消息已超过其保留期。

6. 限制

    在内部，Kafka 托管程序维护着另一个名为 log.retention.check.interval.ms 的属性。该属性决定了检查消息过期的频率。

    因此，为了保持保留策略的有效性，我们必须确保任何给定主题的 log.retention.check.interval.ms 值低于 retention.ms 属性值。

7. 结论

    在本教程中，我们探索了 Apache Kafka，以了解基于时间的消息保留策略。在此过程中，我们创建了简单的 shell 脚本来简化管理活动。随后，我们创建了一个独立的消费者和生产者，以验证消息在保留期过后是否过期。
