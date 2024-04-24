# [使用 Spring Boot 配置 Kafka SSL](https://www.baeldung.com/spring-boot-kafka-ssl)

1. 简介

    在本教程中，我们将介绍使用 SSL 身份验证将 Spring Boot 客户端连接到 Apache Kafka 代理的基本设置。

    实际上，安全套接字层（SSL）自 2015 年起已被弃用，取而代之的是传输层安全（TLS）。不过，由于历史原因，Kafka（和 Java）仍然使用 "SSL"，我们在本文中也将遵循这一惯例。

2. SSL 概述

    默认情况下，Apache Kafka 以明文形式发送所有数据，不需要任何身份验证。

    首先，我们可以在代理和客户端之间配置 SSL 进行加密。默认情况下，这需要使用公钥加密进行单向身份验证，即客户端对服务器证书进行身份验证。

    此外，服务器还可以使用单独的机制（如 SSL 或 SASL）对客户端进行身份验证，从而实现双向身份验证或相互 TLS（mTLS）。基本上，双向 SSL 验证确保客户端和服务器都使用 SSL 证书来验证对方的身份，并在两个方向上相互信任。

    在本文中，代理将使用 SSL 对客户端进行身份验证，[密钥库(keystore)](https://www.baeldung.com/java-keystore-truststore-difference#java-keystore)和[信任库(truststore)](https://www.baeldung.com/java-keystore-truststore-difference#java-keystore)将用于保存证书和密钥。

    每个代理都需要自己的密钥库，其中包含私钥和公共证书。客户端使用其信任存储来验证该证书并信任服务器。同样，每个客户机也需要自己的密钥库，其中包含私人密钥和公共证书。服务器使用其信任库来验证和信任客户端的证书，并建立安全连接。

    信任库可以包含一个可以[签署证书](https://www.baeldung.com/openssl-self-signed-cert)的证书颁发机构（CA）。在这种情况下，代理或客户端会信任由信任库中的 CA 签发的任何证书。这就简化了证书验证，因为添加新客户或代理无需更改信任库。

3. 依赖和设置

    我们的示例应用程序将是一个简单的 Spring Boot 应用程序。

    为了连接到 Kafka，让我们在 POM 文件中添加 spring-kafka 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.1.2</version>
    </dependency>
    ```

    我们还将使用 Docker Compose 文件来配置和测试 Kafka 服务器设置。一开始，我们先不配置任何 SSL：

    ```yml
    ---
    version: '2'
    services:
    zookeeper:
        image: confluentinc/cp-zookeeper:6.2.0
        environment:
        ZOOKEEPER_CLIENT_PORT: 2181
        ZOOKEEPER_TICK_TIME: 2000

    kafka:
        image: confluentinc/cp-kafka:6.2.0
        depends_on:
        - zookeeper
        ports:
        - 9092:9092
        environment:
        KAFKA_BROKER_ID: 1
        KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
        KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
        KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
        KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ```

    现在，让我们启动容器：

    `docker-compose up`

    这样就能以默认配置启动代理了。

4. Broker Configuration

    我们先来看看建立安全连接所需的最低配置。

    1. 独立代理

        虽然在本例中我们没有使用独立的代理实例，但了解启用 SSL 身份验证所需的配置更改还是很有用的。

        首先，我们需要在 server.properties 中配置代理监听端口 9093 上的 SSL 连接：

        ```properties
        listeners=PLAINTEXT://kafka1:9092,SSL://kafka1:9093
        advertised.listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
        ```

        接下来，需要用证书位置和证书配置 keystore 和 truststore 相关属性：

        ```properties
        ssl.keystore.location=/certs/kafka.server.keystore.jks
        ssl.keystore.password=password
        ssl.truststore.location=/certs/kafka.server.truststore.jks
        ssl.truststore.password=password
        ssl.key.password=password
        ```

        最后，必须配置代理对客户端进行身份验证，以实现双向身份验证：

        `ssl.client.auth=required`

    2. Docker Compose

        由于我们使用 Compose 来管理我们的代理环境，让我们把上述所有属性添加到 docker-compose.yml 文件中：

        在这里，我们在配置的端口部分公开了 SSL 端口（9093）。此外，我们还在配置的卷部分挂载了证书项目文件夹。其中包含所需的证书和相关凭证。

        现在，使用 Compose 重新启动堆栈就会在代理日志中显示相关的 SSL 详情：

        ```log
        ...
        kafka_1      | uid=1000(appuser) gid=1000(appuser) groups=1000(appuser)
        kafka_1      | ===> Configuring ...
        <strong>kafka_1      | SSL is enabled.</strong>
        ....
        kafka_1      | [2021-08-20 22:45:10,772] INFO KafkaConfig values:
        <strong>kafka_1      |  advertised.listeners = PLAINTEXT://localhost:9092,SSL://localhost:9093
        kafka_1      |  ssl.client.auth = required</strong>
        <strong>kafka_1      |  ssl.enabled.protocols = [TLSv1.2, TLSv1.3]</strong>
        kafka_1      |  ssl.endpoint.identification.algorithm = https
        kafka_1      |  ssl.key.password = [hidden]
        kafka_1      |  ssl.keymanager.algorithm = SunX509
        <strong>kafka_1      |  ssl.keystore.location = /etc/kafka/secrets/certs/kafka.server.keystore.jks</strong>
        kafka_1      |  ssl.keystore.password = [hidden]
        kafka_1      |  ssl.keystore.type = JKS
        kafka_1      |  ssl.principal.mapping.rules = DEFAULT
        <strong>kafka_1      |  ssl.protocol = TLSv1.3</strong>
        kafka_1      |  ssl.trustmanager.algorithm = PKIX
        kafka_1      |  ssl.truststore.certificates = null
        <strong>kafka_1      |  ssl.truststore.location = /etc/kafka/secrets/certs/kafka.server.truststore.jks</strong>
        kafka_1      |  ssl.truststore.password = [hidden]
        kafka_1      |  ssl.truststore.type = JKS
        ....
        ```

5. Spring Boot 客户端

    服务器设置完成后，我们将创建所需的 Spring Boot 组件。这些组件将与现在需要 SSL 进行双向身份验证的代理进行交互。

    1. 生产者

        首先，让我们使用 [KafkaTemplate](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/core/KafkaTemplate.html) 向指定主题发送消息：

        ```java
        public class KafkaProducer {
            private final KafkaTemplate<String, String> kafkaTemplate;
            public void sendMessage(String message, String topic) {
                log.info("Producing message: {}", message);
                kafkaTemplate.send(topic, "key", message)
                .addCallback(
                    result -> log.info("Message sent to topic: {}", message),
                    ex -> log.error("Failed to send message", ex)
                );
            }
        }
        ```

        发送方法是一个异步操作。因此，我们附加了一个简单的回调，在代理收到消息后记录一些信息。

    2. 消费者

        接下来，让我们使用 [@KafkaListener](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/annotation/KafkaListener.html) 创建一个简单的消费者。 它将连接到代理，并从与生产者使用的主题相同的主题中消费消息：

        main/.spring.kafka.ssl/KafkaConsumer.java

        在我们的演示应用程序中，我们将事情简单化，消费者只需将消息存储在 List 中。在实际系统中，消费者会接收信息，并根据应用程序的业务逻辑对其进行处理。

    3. 配置

        最后，让我们在 application.yml 中添加必要的配置：

        ```yml
        spring:
        kafka:
            security:
            protocol: "SSL"
            bootstrap-servers: localhost:9093
            ssl:
            trust-store-location: classpath:/client-certs/kafka.client.truststore.jks
            trust-store-password: <password>
            key-store-location:  classpath:/client-certs/kafka.client.keystore.jks
            key-store-password: <password>
            
            # additional config for producer/consumer
        ```

        在这里，我们设置了 Spring Boot 提供的所需属性，以配置生产者和消费者。由于这两个组件都连接到同一个代理，我们可以在 spring.kafka 部分声明所有基本属性。不过，如果生产者和消费者连接的是不同的经纪商，我们将分别在 spring.kafka.producer 和 spring.kafka.consumer 部分中指定这些属性。

        在配置的 ssl 部分，我们指向 JKS 信任存储，以验证 Kafka 代理。其中包含 CA 证书，CA 也签署了代理证书。此外，我们还提供了 Spring 客户端密钥库的路径，其中包含由 CA 签发的证书，该证书应存在于代理端的信任库中。

    4. 测试

        由于我们使用的是 Compose 文件，因此让我们使用 [Testcontainers](https://www.baeldung.com/spring-boot-kafka-testing#testing-kafka-with-testcontainers) 框架来创建一个带有生产者和消费者的端到端测试：

        test/.spring.kafka.ssl/KafkaSslApplicationLiveTest.java

        运行测试时，Testcontainers 会使用我们的 Compose 文件启动 Kafka 代理，包括 SSL 配置。应用程序也会使用 SSL 配置启动，并通过加密和认证连接连接到代理。由于这是一连串异步事件，我们使用 [Awaitlity](https://www.baeldung.com/awaitlity-testing) 轮询消费者消息存储中的预期消息。这将验证所有配置以及代理和客户端之间成功的双向身份验证。

6. 结论

    在本文中，我们介绍了 Kafka 代理和 Spring Boot 客户端之间所需的 SSL 身份验证设置的基础知识。

    首先，我们介绍了启用双向身份验证所需的代理设置。然后，我们查看了客户端所需的配置，以便通过加密和验证连接连接到代理。最后，我们使用集成测试来验证代理和客户端之间的安全连接。
