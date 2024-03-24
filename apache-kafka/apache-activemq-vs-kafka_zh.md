# Apache ActiveMQ 与 Kafka

[构架](https://www.baeldung.com/category/architecture)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    在分布式架构中，应用程序之间通常需要交换数据。一方面，可以通过直接相互通信来实现。另一方面，为了实现高可用性和分区容忍性，并在应用程序之间实现松散耦合，消息传递是一种合适的解决方案。

    因此，我们可以选择多种产品。Apache 基金会提供了 ActiveMQ 和 Kafka，我们将在本文中对它们进行比较。

2. 概况

    1. Active MQ

        Active MQ 是传统的消息代理之一，其目标是以安全可靠的方式确保应用程序之间的数据交换。它处理的数据量较小，因此专门用于定义明确的消息格式和事务消息传递。

        我们必须注意到，除了这个 "classic"版本外，还有另一个版本： Active MQ Artemis。这款下一代代理基于 HornetQ，其代码于 2015 年由 RedHat 提供给 Apache 基金会。[Active MQ 网站称](https://activemq.apache.org/)：

        > 一旦 Artemis 达到与"classic"代码库相当的功能水平，它将成为 ActiveMQ 的下一个主要版本。

        因此，为了进行比较，我们需要考虑这两个版本。我们将使用 "Active MQ "和 "Artemis"来区分它们。

    2. 卡夫卡

        与 Active MQ 不同，Kafka 是一个用于处理海量数据的分布式系统。我们可以将其用于传统的消息传递，也可以用于

        - 网站活动跟踪
        - 度量
        - 日志汇总
        - 流处理
        - 事件源
        - 提交日志

        随着使用微服务构建的典型云架构的出现，这些要求变得越来越重要。

    3. JMS 的作用和消息传递的演变

        Java 消息服务（JMS）是在 Java EE 应用程序中发送和接收消息的通用 API。它是消息传递系统早期演进的一部分，如今仍是一项标准。在 Jakarta EE 中，它被采用为 Jakarta 消息服务。因此，了解其核心概念可能会有所帮助：

        - Java 原生但独立于供应商的 API
        - 需要一个 JCA 资源适配器来实现特定于供应商的通信协议
        - 消息目的地模型：
        - 队列（P2P），以确保消息排序和一次性消息处理，即使在有多个消费者的情况下也是如此
        - 主题 (PubSub)，作为发布-订阅模式的实现，这意味着多个消费者在订阅主题期间将收到消息
        - 信息格式：
        - 标题作为中介处理的标准化元信息（如优先级或过期日期）
        - 作为非标准化元信息的属性，消费者可用于信息处理
        - 包含有效载荷的正文（Body）--JMS 声明了五种类型的消息，但这只与使用 API 有关，与本比较无关。

        不过，其发展方向是开放和独立的--独立于消费者和生产者的平台，也独立于消息传递中介的供应商。有一些协议定义了自己的目的地模型：

        - [AMQP](https://www.amqp.org/) - 独立于供应商的二进制消息传输协议 - 使用通用节点
        - [MQTT](https://mqtt.org/) - 用于嵌入式系统和物联网的轻量级二进制协议 - 使用主题
        - [STOMP](https://stomp.github.io/) - 一种基于文本的简单协议，甚至可以从浏览器发送消息 - 使用通用目的地

        另一项发展是通过云架构的普及，将以前可靠的单个信息传输（"traditional messaging"）增加到根据"Fire and Forget"原则处理大量数据的过程中。我们可以说，Active MQ 和 Kafka 之间的比较是这两种方法的典范代表的比较。例如，Kafka 的替代品可以是[NATS](https://nats.io/)。

3. 比较

    在本节中，我们将比较 Active MQ 和 Kafka 在架构和开发方面最有趣的特点。

    1. 消息目的地模型、协议和应用程序接口

        Active MQ 完全实现了队列和主题的 JMS 消息目的地模型，并将 AMQP、MQTT 和 STOMP 消息映射到它们。例如，在一个主题中，一个STOMP消息被映射到一个JMS BytesMessage。此外，它还支持 [OpenWire](https://activemq.apache.org/openwire)，允许跨语言访问 Active MQ。

        Artemis 独立于标准应用程序接口和协议，定义了自己的消息目的地模型，并需要将它们映射到这一模型中：

        - 消息被发送到一个地址，该地址有一个唯一的名称、一个路由类型和零个或多个队列。
        - 路由类型决定了信息如何从地址路由到与该地址绑定的队列。定义了两种类型：
        - ANYCAST：信息被路由到地址上的单个队列
        - MULTICAST：消息被路由到地址上的每个队列。

        Kafka 只定义了主题（Topic），它由多个分区（至少 1 个）和副本（Replicas）组成，这些分区和副本可以放在不同的代理服务器上。为主题分区寻找最佳策略是一项挑战。我们必须注意：

        - 一条信息被分配到一个分区。
        - 只确保一个分区中的信息有序。
        - 默认情况下，后续信息会在主题分区之间循环分发。
        - 如果我们使用消息密钥，那么具有相同密钥的消息将进入同一个分区。

        Kafka 有自己的 [API](https://kafka.apache.org/documentation/#api)。虽然 JMS 也有一个资源适配器（[JMS Resource Adapter](https://docs.payara.fish/enterprise/docs/documentation/ecosystem/cloud-connectors/apache-kafka.html)），但我们应该意识到这两个概念并不完全兼容。官方不支持 AMQP、MQTT 和 STOMP，但有 [AMQP](https://github.com/ppatierno/kafka-connect-amqp) 和 MQTT 的[连接器](https://www.baeldung.com/kafka-connectors-guide)。

    2. 消息格式和处理

        Active MQ 支持由标题、属性和主体（如上所述）组成的 JMS 标准消息格式。代理必须维护每条消息的发送状态，从而降低了吞吐量。在 JMS 的支持下，消费者可以从目的地同步获取消息，也可以由代理异步推送消息。

        Kafka 没有定义任何消息格式--这完全是生产者的责任。每条消息没有任何交付状态，只有每个消费者和分区的 Offset。Offset 是最后一条消息的索引。这样不仅速度更快，而且还可以通过重置偏移量来重新发送信息，而无需询问生产者。

    3. Spring 与 CDI 集成

        JMS 是 Java/Jakarta EE 标准，因此已完全集成到 Java/Jakarta EE 应用程序中。因此，应用程序服务器可以轻松管理与 Active MQ 和 Artemis 的连接。对于 Artemis，我们甚至可以使用[嵌入式代理](https://activemq.apache.org/components/artemis/documentation/latest/cdi-integration.html)。至于 Kafka，只有在使用 JMS 资源适配器或 [Eclipse MicroProfile Reactive](https://dzone.com/articles/using-jakarta-eemicroprofile-to-connect-to-apache) 时才能使用托管连接。

        Spring 集成了 [JMS](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#jms) 以及 [AMQP](https://spring.io/projects/spring-amqp)、[MQTT](https://docs.spring.io/spring-integration/reference/mqtt.html) 和 [STOMP](https://docs.spring.io/spring-integration/reference/stomp.html)。此外还支持 [Kafka](https://spring.io/projects/spring-kafka)。借助 Spring Boot，我们可以使用 [Active MQ](https://memorynotfound.com/spring-boot-embedded-activemq-configuration-example/)、[Artemis](https://activemq.apache.org/components/artemis/documentation/1.0.0/spring-integration.html) 和 [Kafka](https://www.baeldung.com/spring-boot-kafka-testing) 的嵌入式代理。

4. Active MQ/Artemis 和 Kafka 的使用案例

    以下几点为我们指明了使用哪种产品的最佳时机。

    1. Active MQ/Artemis 的使用案例

        - 每天只处理少量信息
        - 高可靠性和事务性
        - 即时数据转换、ETL 工作

    2. Kafka 使用案例

        - 处理大量数据
        - 实时数据处理
        - 应用活动跟踪
        - 日志记录和监控
        - 无需数据转换的消息传送（有可能，但不容易）
        - 无需传输保证的信息传输（有可能实现，但并不容易）

5. 结论

正如我们所看到的，Active MQ/Artemis 和 Kafka 都有各自的目的，因此也有各自的理由。重要的是要了解它们之间的差异，以便根据具体情况选择合适的产品。下表再次简要说明了这些差异：

| 标准            | Active MQ Classic             | Active MQ Artemis             | 卡夫卡                                 |
|---------------|-------------------------------|-------------------------------|-------------------------------------|
| 使用案例          | 传统消息传递（可靠、事务性）                | 传统消息传送（可靠、事务性）                | 分布式事件流                              |
| 点对点信息传送       | 队列                            | 路由类型为 ANYCAST 的地址             | -                                   |
| PubSub 消息传送   | 主题                            | 路由类型为 MULTICAST 的地址           | 主题                                  |
| 应用程序接口/协议     | JMS、AMQP. MQTT、STOMP、OpenWire | JMS、AMQP. MQTT、STOMP、OpenWire | Kafka 客户端、AMQP 和 MQTT 连接器、JMS 资源适配器 |
| 拉式信息传送与推式信息传送 | 基于推送                          | 基于推送                          | 基于拉                                 |
| 消息传送责任        | 生产者必须确保消息已送达                  | 生产者必须确保消息已送达                  | 消费者消费它应该消费的消息                       |
| 事务支持          | JMS、XA                        | JMS、XA                        | [自定义事务管理](https://www.baeldung.com/kafka-exactly-once)器                            |
| 可扩展性          | [经纪人网络](https://activemq.apache.org/networks-of-brokers.html)                         | [集群](https://activemq.apache.org/components/artemis/documentation/1.0.0/clusters.html)                            | 高度可扩展（分区和副本）                        |
| 消费者越多      | 性能越低                       | 性能越低                       | 不会减慢                          |
