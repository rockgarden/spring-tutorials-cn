# [连接在 Docker 中运行的 Apache Kafka](https://www.baeldung.com/kafka-docker-connection)

1. 概述

    Apache Kafka 是一个非常流行的事件流平台，经常与 Docker 一起使用。通常，人们在使用 Kafka 时会遇到连接建立问题，尤其是当客户端不在同一个 Docker 网络或同一台主机上运行时。这主要是由于 Kafka 的广告侦听器配置错误造成的。

    在本教程中，我们将学习如何配置侦听器，以便客户端可以连接到运行在 Docker 中的 Kafka 代理。

2. 设置 Kafka

    在尝试建立连接之前，我们需要使用 Docker 运行 Kafka 代理。下面是我们的 docker-compose.yaml 文件片段：

    ```yaml
    version: '2'
    services:
    zookeeper:
        container_name: zookeeper
        networks: 
        - kafka_network
        ...
    
    kafka:
        container_name: kafka
        networks: 
        - kafka_network
        ports:
        - 29092:29092
        environment:
        KAFKA_LISTENERS: EXTERNAL_SAME_HOST://:29092,INTERNAL://:9092
        KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:9092,EXTERNAL_SAME_HOST://localhost:29092
        KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL_SAME_HOST:PLAINTEXT
        KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
        ... 

    networks:
    kafka_network:
        name: kafka_docker_example_net
    ```

    在这里，我们定义了两个必备服务--Kafka 和 Zookeeper。我们还定义了一个自定义网络 - kafka_docker_example_net，我们的服务将使用它。

    稍后我们将详细介绍 KAFKA_LISTENERS、KAFKA_ADVERTISED_LISTENERS 和 KAFKA_LISTENER_SECURITY_PROTOCOL_MAP 属性。

    有了上面的 docker-compose.yaml 文件，我们就可以启动服务了：

    ```bash
    docker-compose up -d
    Creating network "kafka_docker_example_net" with the default driver
    Creating zookeeper ... done
    Creating kafka ... done
    ```

    此外，我们还将使用 Kafka [控制台生产者](https://kafka-tutorials.confluent.io/kafka-console-consumer-producer-basics/kafka.html)工具作为示例客户端，测试与 Kafka 代理的连接。要在没有 Docker 的情况下使用 Kafka-console-producer 脚本，我们需要下载 Kafka。

3. 监听器

    监听器、广告监听器和监听器协议在连接 Kafka 代理时发挥着重要作用。

    我们使用 KAFKA_LISTENERS 属性来管理监听器，在该属性中，我们声明了一个以逗号分隔的 URI 列表，其中指定了经纪商应该监听传入 TCP 连接的套接字。

    每个 URI 包含一个协议名称、一个接口地址和一个端口：

    `EXTERNAL_SAME_HOST://0.0.0.0:29092,INTERNAL://0.0.0.0:9092`

    在这里，我们指定了 0.0.0.0 元地址，以便将套接字绑定到所有接口。此外，EXTERNAL_SAME_HOST 和 INTERNAL 是我们在以 URI 格式定义监听器时需要指定的自定义监听器名称。

    1. 引导

        对于初始连接，Kafka 客户端需要一个引导服务器列表，我们在其中指定了代理服务器的地址。该列表应至少包含一个指向集群中随机代理的有效地址。

        客户端将使用该地址连接到代理。如果连接成功，代理将返回集群的元数据，包括集群中所有代理的广告监听器列表。在随后的连接中，客户机将使用该列表来连接代理。

    2. 广告监听器

        仅仅声明监听器是不够的，因为这只是对代理的套接字配置。我们需要一种方法来告诉客户端（消费者和生产者）如何连接到 Kafka。

        这就是借助 KAFKA_ADVERTISED_LISTENERS 属性的广告监听器（advertised listeners）。它的格式与监听器属性类似：

        `<listener protocol>://<advertised host name>:<advertised port>`

        客户端会在初始引导过程后使用指定为广告监听器的地址。

    3. 监听器安全协议图

        除了侦听器和广告侦听器，我们还需要告诉客户端在连接到 Kafka 时使用的安全协议。在 KAFKA_LISTENER_SECURITY_PROTOCOL_MAP 中，我们将自定义协议名称映射到有效的安全协议。

        在上一节的配置中，我们声明了两个自定义协议名称--INTERNAL 和 EXTERNAL_SAME_HOST。我们可以随意命名它们，但需要将它们映射到有效的安全协议。

        我们指定的安全协议之一是 PLAINTEXT，这意味着客户端不需要与 Kafka 代理进行身份验证。此外，交换的数据也不会加密。

4. 从同一个 Docker 网络连接客户端

    让我们从另一个容器启动 Kafka 控制台生产者，并尝试向代理发送消息：

    ```bash
    docker run -it --rm --network kafka_docker_example_net confluentinc/cp-kafka /bin/kafka-console-producer --bootstrap-server kafka:9092 --topic test_topic
    >hello
    >world
    ```

    在这里，我们将这个容器附加到现有的 kafka_docker_example_net 网络上，以便与我们的代理自由通信。我们还指定了代理的地址--kafka:9092，以及将自动创建的主题名称。

    我们能够向主题发送消息，这意味着与代理的连接成功了。

5. 从同一主机连接客户端

    让我们在客户端未被容器化的情况下从主机连接到代理。对于外部连接，我们公布了 EXTERNAL_SAME_HOST 监听器，我们可以用它从主机建立连接。从公布的监听器属性中，我们知道必须使用 localhost:29092 地址才能连接到 Kafka 代理。

    为了测试从同一主机的连接性，我们将使用一个非坞化的 Kafka 控制台生产者：

    ```bash
    kafka-console-producer --bootstrap-server localhost:29092 --topic test_topic_2
    >hi
    >there
    ```

    既然我们成功生成了主题，就意味着与代理的初始引导和后续连接（客户端使用广告侦听器）都成功了。

    我们之前在 docker-compose.yaml 中配置的端口号 29092 使 Kafka 代理可以在 Docker 外部到达。

6. 从不同主机连接客户端

    如果 Kafka 代理运行在不同的主机上，我们该如何连接它呢？不幸的是，我们无法重新使用现有的监听器，因为它们只能用于相同的 Docker 网络或主机连接。因此，我们需要定义一个新的监听器并对其进行宣传：

    ```bash
    KAFKA_LISTENERS: EXTERNAL_SAME_HOST://:29092,EXTERNAL_DIFFERENT_HOST://:29093,INTERNAL://:9092
    KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:9092,EXTERNAL_SAME_HOST://localhost:29092,EXTERNAL_DIFFERENT_HOST://157.245.80.232:29093
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL_SAME_HOST:PLAINTEXT,EXTERNAL_DIFFERENT_HOST:PLAINTEXT
    ```

    我们创建了一个名为 EXTERNAL_DIFFERENT_HOST 的新监听器，其安全协议为 PLAINTEXT，端口为 29093。在 KAFKA_ADVERTISED_LISTENERS 中，我们还添加了运行 Kafka 的云计算机器的 IP 地址。

    我们必须记住，我们不能使用 localhost，因为我们是从不同的机器（本例中是本地工作站）连接的。此外，29093 端口已发布在端口部分，因此在 Docker 外部也可以访问。

    让我们试着生成一些信息：

    ```bash
    kafka-console-producer --bootstrap-server 157.245.80.232:29093 --topic test_topic_3
    >hello
    >REMOTE SERVER
    ```

    我们可以看到，我们能够成功连接到 Kafka 代理并生成消息。

7. 结论

    在本文中，我们学习了如何配置侦听器，以便客户端可以连接到在 Docker 中运行的 Kafka 代理。我们研究了客户端运行在同一 Docker 网络、同一主机、不同主机等不同场景。我们看到，侦听器、广告侦听器和安全协议映射的配置决定了连接性。
