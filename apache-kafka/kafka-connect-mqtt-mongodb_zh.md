# [使用 MQTT 和 MongoDB 的 Kafka 连接示例](https://www.baeldung.com/kafka-connect-mqtt-mongodb)

[Data](https://www.baeldung.com/category/data)

1. 概述

    在[上一篇文章](kafka-connectors-guide_zh.md)中，我们快速介绍了 Kafka Connect，包括不同类型的连接器、Connect 的基本功能以及 REST API。

    在本教程中，我们将使用 Kafka 连接器构建一个更 "真实" 的示例。

    我们将使用连接器通过 MQTT 收集数据，并将收集到的数据写入 MongoDB。

2. 使用 Docker 进行设置

    我们将使用 [Docker Compose](https://docs.docker.com/compose/) 设置基础设施。这包括作为源的 MQTT 代理、作为中间件的 Zookeeper、一个 Kafka 代理和 Kafka Connect，以及作为汇的 MongoDB 实例（包括一个 GUI 工具）。

    1. 连接器安装

        我们的示例所需的连接器（一个 [MQTT](https://www.confluent.io/connector/kafka-connect-mqtt/) 源和一个 [MongoDB](https://www.confluent.io/connector/kafka-connect-mongodb-sink/) 汇连接器）并不包含在纯 Kafka 或 Confluent Platform 中。

        正如我们在上一篇文章中所讨论的，我们可以从 Confluent 中心下载连接器（MQTT 和 MongoDB）。之后，我们必须将 jars 解压缩到一个文件夹中，并在下一节中将其挂载到 Kafka Connect 容器中。

        为此，我们使用 /tmp/custom/jars 文件夹。由于 Kafka Connect 会在启动过程中在线加载连接器，因此我们必须在下一节启动编译栈之前将 jars 移到该文件夹中。

    2. Docker 编译文件

        我们用一个简单的 Docker 组件文件来描述我们的设置，它由六个容器组成：

        main/resources/kafka-connect/04_Custom/docker-compose.yaml

        mosquitto 容器提供了一个基于 Eclipse Mosquitto 的简单 MQTT 代理。

        容器 zookeeper 和 kafka 定义了一个单节点 Kafka 集群。

        kafka-connect 定义了分布式模式下的 Connect 应用程序。

        最后，mongo-db 定义了我们的汇数据库，以及基于 Web 的 mongoclient，它可以帮助我们验证发送的数据是否正确到达数据库。

        我们可以使用以下命令启动堆栈：

        `docker-compose up`

3. 连接器配置

    由于 Kafka Connect 已经启动并运行，我们现在可以配置连接器了。

    1. 配置源连接器

        让我们使用 REST API 配置源连接器：

        `curl -d @<path-to-config-file>/connect-mqtt-source.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`

        参见 main/resources/kafka-connect/04_Custom/connect-mqtt-source.json 文件。

        有几个属性是我们之前没有使用过的：

        - mqtt.server.uri 是我们的连接器将连接的端点
        - mqtt.topics 是我们的连接器将订阅的 MQTT 主题
        - kafka.topic 定义了连接器将接收到的数据发送到的 Kafka 主题
        - value.converter 定义了将应用于接收到的有效负载的转换器。我们需要 ByteArrayConverter，因为 MQTT 连接器默认使用 Base64，而我们希望使用纯文本
        - 最新版本的连接器要求使用 confluent.topic.bootstrap.servers
        - confluent.topic.replication.factor 也是如此：它定义了 Confluent 内部主题的复制因子，由于我们的集群中只有一个节点，因此必须将该值设为 1。

    2. 测试源连接器

        让我们运行一个快速测试，向 MQTT 代理发布一条短消息：

        ```bash
        docker run \
        -it --rm --name mqtt-publisher --network 04_custom_default \
        efrecon/mqtt-client \
        pub -h mosquitto  -t "baeldung" -m "{\"id\":1234,\"message\":\"This is a test\"}"
        ```

        如果我们监听主题，connect-custom：

        ```bash
        docker run \
        --rm \
        confluentinc/cp-kafka:5.1.0 \
        kafka-console-consumer --network 04_custom_default --bootstrap-server kafka:9092 --topic connect-custom --from-beginning
        ```

        然后我们就能看到测试消息了。

    3. 设置汇连接器

        接下来，我们需要我们的汇连接器。让我们再次使用 REST API：

        `curl -d @<path-to-config file>/connect-mongodb-sink.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors`

        参见 main/resources/kafka-connect/04_Custom/connect-mongodb-sink.json 文件。

        这里有以下 MongoDB 特有的属性：

        - mongodb.connection.uri 包含 MongoDB 实例的连接字符串
        - mongodb.collection 定义了集合
        - 由于 MongoDB 连接器期待使用 JSON 格式，我们必须为 key.converter 和 value.converter 设置 JsonConverter。
        - 我们还需要 MongoDB 的无模式(schemaless) JSON，因此必须将 key.converter.schemas.enable 和 value.converter.schemas.enable 设置为 false。

    4. 测试水槽连接器

        由于我们的主题 connect-custom 已经包含了来自 MQTT 连接器测试的消息，因此 MongoDB 连接器在创建后应该已经直接获取了这些消息。

        因此，我们应该立即在 MongoDB 中找到它们。为此，我们可以使用网络界面，打开 URL <http://localhost:3000/>。登录后，我们可以选择左侧的 MyCollection，点击 Execute，测试信息就会显示出来。

    5. 端到端测试

        现在，我们可以使用 MQTT 客户端发送任何 JSON 结构：

        ```json
        {
            "firstName": "John",
            "lastName": "Smith",
            "age": 25,
            "address": {
                "streetAddress": "21 2nd Street",
                "city": "New York",
                "state": "NY",
                "postalCode": "10021"
            },
            "phoneNumber": [{
                "type": "home",
                "number": "212 555-1234"
            }, {
                "type": "fax",
                "number": "646 555-4567"
            }],
            "gender": {
                "type": "male"
            }
        }
        ```

        MongoDB 支持无模式 JSON 文档，由于我们禁用了转换器的模式，任何结构都会立即通过我们的连接器链并存储到数据库中。

        同样，我们可以使用 <http://localhost:3000/> 上的 Web 界面。

    6. 清理

        完成后，我们可以清理实验并移除两个连接器：

        ```bash
        curl -X DELETE http://localhost:8083/connectors/mqtt-source
        curl -X DELETE http://localhost:8083/connectors/mongodb-sink
        ```

        之后，我们就可以用 Ctrl + C 关闭Compose堆栈了。

4. 结论

    在本教程中，我们使用 Kafka Connect 构建了一个示例，通过 MQTT 收集数据，并将收集到的数据写入 MongoDB。
