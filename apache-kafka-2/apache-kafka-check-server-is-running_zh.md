# [检查 Apache Kafka 服务器是否正在运行的指南](https://www.baeldung.com/apache-kafka-check-server-is-running)

[Data](https://www.baeldung.com/category/data)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    使用 Apache Kafka 的客户端应用程序通常分为两类，即生产者和消费者。生产者和消费者都要求底层的 Kafka 服务器启动并运行，然后才能分别开始生产和消费工作。

    在本文中，我们将学习一些确定 Kafka 服务器是否正在运行的策略。

2. 使用 Zookeeper 命令

    使用 Zookeeper 的 dump 命令是查明是否有活跃的经纪商的最快方法之一。dump 命令是用于管理 Zookeeper 服务器的 [4LW](https://zookeeper.apache.org/doc/r3.4.10/zookeeperAdmin.html#sc_zkCommands) 命令之一。

    让我们使用 nc 命令向监听端口为 2181 的 Zookeeper 服务器发送 dump 命令：

    ```bash
    $ echo dump | nc localhost 2181 | grep -i broker | xargs
    /brokers/ids/0
    ```

    执行命令后，我们会看到在 Zookeeper 服务器上注册的短暂代理 ID 列表。如果没有短暂id，则说明没有一个代理节点在运行。

    此外，需要注意的是，通常在 zookeeper.properties 或 zoo.cfg 配置文件中的配置需要明确允许 dump 命令：

    `lw.commands.whitelist=dump`

    或者，我们也可以使用 Zookeeper API 查找[活动代理](https://www.baeldung.com/ops/kafka-list-active-brokers-in-cluster#zookeeper-apis)列表。

3. 使用 Apache Kafka 的 AdminClient

    如果我们的生产者或消费者是 Java 应用程序，那么我们可以使用 Apache Kafka 的 AdminClient 类来查找 Kafka 服务器是否正常运行。

    让我们定义 KafkaAdminClient 类来封装 AdminClient 类的实例，以便快速测试我们的代码：

    .kafka/KafkaAdminClient.java:KafkaAdminClient()

    接下来，让我们在 KafkaAdminClient 类中定义 verifyConnection() 方法，以验证客户端是否可以连接正在运行的代理服务器：

    .kafka/KafkaAdminClient.java:verifyConnection()

    最后，让我们连接到正在运行的 Kafka 集群，对代码进行测试：

    KafkaConnectionLiveTest.java:givenKafkaIsRunning_whenCheckedForConnection_thenConnectionIsVerified()

4. 使用 kcat 工具

    我们可以使用 [kcat](https://manpages.ubuntu.com/manpages/focal/man1/kafkacat.1.html)（原名 kafkacat）命令来查找是否有正在运行的 Kafka 代理节点。为此，让我们使用 -L 选项来显示现有主题的元数据：

    ```java
    $ kcat -b localhost:9092 -t demo-topic -L
    Metadata for demo-topic (from broker -1: localhost:9092/bootstrap):
    1 brokers:
    broker 0 at 192.168.1.53:9092 (controller)
    1 topics:
    topic "demo-topic" with 1 partitions:
        partition 0, leader 0, replicas: 0, isrs: 0
    ```

    接下来，让我们在代理节点宕机时执行相同的命令：

    ```java
    $ kcat -b localhost:9092 -t demo-topic -L -m 1
    %3|1660579562.937|FAIL|rdkafka#producer-1| [thrd:localhost:9092/bootstrap]: localhost:9092/bootstrap: Connect to ipv4#127.0.0.1:9092 failed: Connection refused (after 1ms in state CONNECT)
    % ERROR: Failed to acquire metadata: Local: Broker transport failure (Are the brokers reachable? Also try increasing the metadata timeout with -m <timeout>?)
    ```

    在这种情况下，我们会收到"Connection refused"错误，因为没有正在运行的代理节点。此外，我们必须注意，通过使用 -m 选项将请求超时限制为 1 秒，我们能够快速失败。

5. 使用用户界面工具

    对于不需要自动检查的实验性 POC 项目，我们可以使用用户界面工具（如 [Offset Explorer](https://www.kafkatool.com/)）。但是，如果我们想验证企业级 Kafka 客户端的代理节点状态，则不建议使用这种方法。

    让我们使用 Offset Explorer，使用 Zookeeper 主机和端口详细信息连接到 Kafka 集群：使用 Offset Explorer 检查连接情况。

    我们可以在左侧窗格中看到正在运行的代理列表。

6. 总结

    在本教程中，我们探索了使用 Zookeeper 命令、Apache 的 AdminClient 和 kcat 工具的几种命令行方法，然后使用基于 UI 的方法来确定 Kafka 服务器是否正常运行。

    一如既往，本教程的完整源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/apache-kafka-2) 上获取。
