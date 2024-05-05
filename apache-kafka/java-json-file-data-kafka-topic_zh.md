# [将 JSON 文件数据转化为 Kafka 主题](https://www.baeldung.com/java-json-file-data-kafka-topic)

1. 概述

    有时，我们需要向 Kafka 主题发送 JSON 数据类型，以便进行数据处理和分析。

    在本教程中，我们将学习如何将 JSON 数据流导入 Kafka 主题。此外，我们还将了解如何为 JSON 数据配置 Kafka 生产者和消费者。

2. JSON 数据在 Kafka 中的重要性

    从架构上讲，Kafka 系统支持消息流。因此，我们也可以向 Kafka 服务器发送 JSON 数据。如今，在现代应用系统中，每个应用程序都主要只处理 JSON 格式的数据，因此以 JSON 格式进行通信变得非常重要。通过发送 JSON 格式的数据，可以实时跟踪用户在网站和应用程序上的活动和行为。

    将 JSON 类型的数据蒸发(Steaming)到 Kafka 服务器有助于实时数据分析。它促进了事件驱动架构，其中每个微服务都会订阅相关主题并实时提供更改。有了 Kafka 主题和 JSON 格式，就可以轻松交付物联网数据、在微服务之间进行通信以及汇总指标。

3. Kafka 设置

    要将 JSON 流导入 Kafka 服务器，我们首先需要设置 [Kafka代理](https://www.baeldung.com/ops/kafka-list-active-brokers-in-cluster)和 [Zookeeper](https://www.baeldung.com/kafka-shift-from-zookeeper-to-kraft)。我们可以按照本教程设置一个完整的 Kafka 服务器。现在，让我们检查一下创建 Kafka 主题 baeldung 的命令，我们将在该主题上生产和消费 JSON 数据：

    ```bash
    $ docker-compose exec kafka kafka-topics.sh --create --topic baeldung
    --partitions 1 --replication-factor 1 --bootstrap-server kafka:9092
    ```

    上述命令创建了一个复制因子为 1 的 Kafka 主题 baeldung。在这里，我们只创建了一个复制因子为 1 的 Kafka 主题，因为它只是用于演示目的。在实际场景中，我们可能需要多复制因子，因为它有助于系统故障切换。此外，它还能提供数据的高可用性和可靠性。

4. 生产数据

    Kafka 生产者是整个 Kafka 生态系统中最基本的组件，它提供了向 Kafka 服务器生产数据的功能。为了演示，让我们看看使用 docker-compose 命令启动生产者的命令：

    ```bash
    $ docker-compose exec kafka kafka-console-producer.sh --topic baeldung
    --broker-list kafka:9092
    ```

    在上述命令中，我们创建了一个 Kafka 生产者，用于向 Kafka 代理发送消息。此外，为了发送 JSON 数据类型，我们还需要对命令进行调整。在继续之前，让我们先创建一个示例 JSON 文件 sampledata.json：

    ```json
    {
        "name": "test",
        "age": 26,
        "email": "test@baeldung.com",
        "city": "Bucharest",
        "occupation": "Software Engineer",
        "company": "Baeldung Inc.",
        "interests": ["programming", "hiking", "reading"]
    }
    ```

    上述 sampledata.json 文件包含 JSON 格式的用户基本信息。要将 JSON 数据发送到 Kafka 主题中，我们需要 jq 库，因为它在处理 JSON 数据方面非常强大。为了演示，让我们安装 jq 库，将 JSON 数据传递给 Kafka 生产者：

    `$ sudo apt-get install jq`

    上述命令只是在 Linux 机器上安装 jq 库。此外，让我们看看发送 JSON 数据的命令：

    ```bash
    $ jq -rc . sampledata.json | docker-compose exec -T kafka kafka-console-producer.sh --topic baeldung --broker-list kafka:9092
    ```

    上述命令是在 Docker 环境中处理 JSON 数据并将其流到 Kafka 主题的单行命令。首先，jq 命令处理 sampledata.json，然后使用 -r 选项确保 JSON 数据是行格式和无引号格式。之后，-c 选项确保数据以单行形式呈现，以便数据能轻松流向相应的 Kafka 主题。

5. 消费者数据

    到目前为止，我们已经成功地将 JSON 数据发送到了 baeldung Kafka 主题。现在，让我们看看如何使用该命令来读取数据：

    ```bash
    $ docker-compose exec kafka kafka-console-consumer.sh --topic baeldung  --from-beginning --bootstrap-server kafka:9092
    {"name":"test","age":26,"email":"test@baeldung.com","city":"Bucharest","occupation":"Software Engineer","company":"Baeldung Inc.","interests":["programming","hiking","reading"]}
    ```

    上述命令会消耗从一开始发送到 baeldung 主题的所有数据。在上一节中，我们发送了 JSON 数据。因此，它也会消耗这些 JSON 数据。简而言之，上述命令允许用户主动监控发送到 baeldung 主题的所有消息。它有助于使用基于 Kafka 的消息系统实时消费数据。

6. 结论

    在本文中，我们探讨了如何将 JSON 数据流导入 Kafka 主题。首先，我们创建了一个 JSON 样本，然后使用生产者（producer）将 JSON 数据流导入 Kafka 主题。之后，我们使用 docker-compose 命令消耗这些数据。

    简而言之，我们涵盖了使用 Kafka 生产者和消费者向主题发送 JSON 格式数据的所有必要步骤。此外，它还提供了模式演进功能，因为 JSON 可以处理优雅的更新，而不会影响现有数据。
