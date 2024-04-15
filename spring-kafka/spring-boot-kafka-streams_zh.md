# [使用 Spring Boot 开发 Kafka 流](https://www.baeldung.com/spring-boot-kafka-streams)

1. 简介

    在本文中，我们将了解如何使用 Spring Boot 设置 [Kafka Streams](https://www.baeldung.com/java-kafka-streams)。Kafka Streams 是构建在 Apache Kafka 基础上的客户端库。它能以声明的方式处理无限制的事件流。

    流数据在现实生活中的一些例子包括传感器数据、股票市场事件流和系统日志。在本教程中，我们将构建一个简单的字数统计流应用程序。让我们先来了解一下 Kafka Streams，然后在 Spring Boot 中设置示例及其测试。

2. 概述

    Kafka Streams 在 Kafka 主题和关系数据库表之间提供了二元性。它使我们能够对一个或多个流事件进行连接、分组、聚合和过滤等操作。

    Kafka Streams 的一个重要概念是处理器拓扑。处理器拓扑是在一个或多个事件流上进行 Kafka 流操作的蓝图。从本质上讲，处理器拓扑可视为[有向无环图](https://www.baeldung.com/cs/dag-topological-sort)。在这个图中，节点分为源节点、处理器节点和汇节点，而边则代表流事件的流向。

    位于拓扑结构顶端的源节点从 Kafka 接收流数据，向下传递到执行自定义操作的处理器节点，并通过汇节点流出到新的 Kafka 主题。在进行核心处理的同时，还使用检查点定期保存流的状态，以实现容错和弹性。

3. 依赖关系

    我们将首先在 POM 中添加 spring-kafka 和 kafka-streams 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.1.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-streams</artifactId>
        <version>3.6.1</version>
    </dependency>
    ```

4. 示例

    我们的示例应用程序从输入的 Kafka 主题中读取流式事件。读取记录后，应用程序会对记录进行处理，分割文本并计算单个字数。随后，它将更新的字数发送到 Kafka 输出。除了输出主题外，我们还将创建一个简单的 REST 服务，通过 HTTP 端点公开该计数。

    总之，输出主题将不断更新从输入事件中提取的单词及其更新的计数。

    1. 配置

        首先，让我们在 Java 配置类中定义 Kafka 流配置：

        main/.kafka.streams/KafkaConfig.java

        在这里，我们使用 @EnableKafkaStreams 注解来自动配置所需的组件。这种自动配置需要一个 KafkaStreamsConfiguration Bean，其名称由 DEFAULT_STREAMS_CONFIG_BEAN_NAME 指定。因此，Spring Boot 会使用此配置并创建 KafkaStreams 客户端来管理应用程序生命周期。

        在我们的示例中，我们为配置提供了应用程序 ID、引导服务器连接详情和 SerDes（序列器/反序列器）。

    2. 拓扑结构

        配置设置完成后，让我们为应用程序构建拓扑(topology)结构，以记录输入消息的字数：

        main/.kafka.streams/WordCountProcessor.java

        在这里，我们定义了一个配置方法，并用 @Autowired 对其进行了注解。Spring 会处理此注解，并将容器中匹配的 Bean 连接到 StreamsBuilder 参数中。或者，我们也可以在配置类中创建一个 Bean 来生成拓扑结构。

        StreamsBuilder 允许我们访问所有 Kafka Streams API，它就像一个普通的 Kafka Streams 应用程序。在我们的示例中，我们使用了这个高级 DSL 来定义应用程序的转换：

        - 使用指定的键和值 SerDes 从输入主题创建 KStream。
        - 通过数据转换、拆分、分组和计数创建 KTable。
        - 将结果物化为输出流。

        从本质上讲，Spring Boot 在管理 KStream 实例生命周期的同时，为 Streams API 提供了一个非常薄的封装。它为拓扑创建和配置所需的组件，并执行我们的 Streams 应用程序。重要的是，在 Spring 管理生命周期的同时，我们可以专注于核心业务逻辑。

    3. REST 服务

        通过声明步骤定义管道后，让我们创建 REST 控制器。这将提供端点，以便向输入主题 POST 消息和获取指定单词的计数。但重要的是，应用程序将从 Kafka Streams 状态存储而非输出主题中检索数据。

        首先，让我们修改之前的 KTable，并将聚合计数具体化为本地状态存储。然后就可以通过 REST 控制器进行查询了：

        ```java
        KTable<String, Long> wordCounts = textStream
        .mapValues((ValueMapper<String, String>) String::toLowerCase)
        .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
        .groupBy((key, value) -> value, Grouped.with(STRING_SERDE, STRING_SERDE))
        .count(Materialized.as("counts"));
        ```

        之后，我们可以更新控制器，从计数状态存储中检索计数值：

        main/.kafka.streams/WordCountRestService.java:getWordCount()

        在这里，factoryBean 是 StreamsBuilderFactoryBean 的一个实例，它被连接到控制器中。这提供了由该工厂 Bean 管理的 KafkaStreams 实例。因此，我们可以获得之前创建的由 KTable 表示的键/值计数状态存储。此时，我们可以使用它从本地状态存储中获取请求的单词的当前计数。

5. 测试

    测试是开发和验证应用程序拓扑的关键部分。[Spring Kafka 测试库](https://www.baeldung.com/spring-boot-kafka-testing)和 [Testcontainers](https://www.baeldung.com/docker-test-containers) 都为在不同层面测试应用程序提供了出色的支持。

    1. 单元测试

        首先，让我们使用 TopologyTestDriver 为拓扑结构设置单元测试。这是测试 Kafka Streams 应用程序的主要测试工具：

        main/.kafka.streams/WordCountProcessorUnitTest.java:givenInputMessages_whenProcessed_thenWordCountIsProduced() 

        在这里，我们首先需要的是 Topology，它封装了来自 WordCountProcessor 的待测业务逻辑。现在，我们可以将其与 TopologyTestDriver 结合使用，为测试创建输入和输出主题。最重要的是，这样就不需要运行代理，同时还能验证管道行为。换句话说，它让我们无需使用真正的 Kafka 代理就能快速、轻松地验证管道行为。

    2. 集成测试

        最后，让我们使用 Testcontainers 框架来测试我们的端到端应用程序。这将使用一个正在运行的 Kafka 代理，并启动我们的应用程序进行完整的测试：

        test/.kafka.streams/KafkaStreamsApplicationLiveTest.java

        在这里，我们向 REST 控制器发送了 POST，而 REST 控制器又将消息发送到 Kafka 输入主题。作为设置的一部分，我们还启动了 Kafka 消费者。它异步监听输出 Kafka 主题，并用接收到的字数更新 BlockingQueue。

        在测试执行期间，应用程序应处理输入信息。随后，我们可以使用 REST 服务验证主题和状态存储的预期输出。

6. 总结

    在本教程中，我们了解了如何使用 Kafka Streams 和 Spring Boot 创建一个简单的事件驱动应用程序来处理消息。

    在简要介绍了流的核心概念后，我们了解了流拓扑的配置和创建。然后，我们了解了如何将其与 Spring Boot 提供的 REST 功能集成。最后，我们介绍了一些有效测试和验证拓扑和应用程序行为的方法。
