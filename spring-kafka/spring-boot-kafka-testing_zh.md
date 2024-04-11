# [测试 Kafka 和 Spring Boot](https://www.baeldung.com/spring-boot-kafka-testing)

1. 概述

    Apache Kafka 是一个强大的分布式容错流处理系统。在上一篇[教程](spring-kafka_zh.md)中，我们学习了如何使用 Spring 和 Kafka。

    在本教程中，我们将在上一教程的基础上，学习如何编写可靠、独立的集成测试，而不依赖于外部 Kafka 服务器的运行。

    首先，我们将了解如何使用和配置 Kafka 的嵌入式实例。

    然后，我们将了解如何在测试中使用流行框架 [Testcontainers](https://www.testcontainers.org/)。

2. 依赖关系

    当然，我们需要在 pom.xml 中添加标准的 spring-kafka 依赖关系：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.1.2</version>
    </dependency>
    ```

    然后，我们还需要两个专门用于测试的依赖项。

    首先，我们将添加 spring-kafka-test 工件：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <version>3.1.1</version>
        <scope>test</scope>
    </dependency>
    ```

    最后，我们将添加 Testcontainers Kafka 依赖项，Maven Central 上也有该依赖项：

    ```xml
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>kafka</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    ```

    现在我们已经配置了所有必要的依赖项，可以使用 Kafka 编写一个简单的 Spring Boot 应用程序了。

3. 简单的 Kafka 生产者-消费者应用程序

    在本教程中，我们的测试重点将是一个简单的生产者-消费者 Spring Boot Kafka 应用程序。

    让我们从定义应用程序入口点开始：

    main/.kafka.embedded/KafkaProducerConsumerApplication.java

    正如我们所见，这是一个标准的 Spring Boot 应用程序。

    1. 生产者设置

        接下来，让我们来考虑一个生产者 Bean，我们将用它来向给定的 Kafka 主题发送消息：

        main/.kafka.embedded/KafkaProducer.java

        上面定义的 KafkaProducer Bean 只是 KafkaTemplate 类的一个封装。该类提供高级线程安全操作，例如向提供的主题发送数据，这正是我们在发送方法中要做的。

    2. 消费者设置

        同样，我们现在要定义一个简单的消费者 bean，它将监听 Kafka 主题并接收消息：

        main/.kafka.embedded/KafkaConsumer.java

        我们的简单消费者在接收方法上使用 @KafkaListener 注解来监听给定主题上的消息。稍后我们将看到如何配置测试中的 test.topic。

        此外，receive 方法会将消息内容存储到我们的 bean 中，并递减锁存器变量的计数。该变量是一个简单的[线程安全计数器](https://www.baeldung.com/cs/async-vs-multi-threading)字段，我们稍后将在测试中使用它来确保成功接收到消息。

        现在，我们已经使用 Spring Boot 实现了简单的 Kafka 应用程序，让我们看看如何编写集成测试。

4. 关于测试的一句话

    一般来说，在编写简洁的集成测试时，我们不应依赖于我们可能无法控制或可能突然停止工作的外部服务。这可能会对我们的测试结果产生不利影响。

    同样，如果我们依赖于一个外部服务，在本例中就是一个正在运行的 Kafka 代理，那么我们很可能无法按照我们的测试要求对其进行设置、控制和拆卸。

    1. 应用程序属性

        我们将在测试中使用一组非常简单的应用程序配置属性。

        我们将在 src/test/resources/application.yml 文件中定义这些属性：

        ```yaml
        spring:
        kafka:
            consumer:
            auto-offset-reset: earliest
            group-id: baeldung
        test:
        topic: embedded-test-topic
        ```

        这是我们在使用 Kafka 的嵌入式实例或本地代理时需要的最小属性集。

        其中大部分属性不言自明，但我们需要强调的是消费者属性 auto-offset-reset: earliest。该属性可确保我们的消费者群组收到我们发送的消息，因为容器可能会在发送完成后才启动。

        此外，我们还配置了一个主题属性，其值为 embedded-test-topic，这是我们将在测试中使用的主题。

5. 使用嵌入式 Kafka 进行测试

    在本节中，我们将了解如何使用内存中的 Kafka 实例来运行我们的测试。这也被称为嵌入式 Kafka。

    我们之前添加的依赖 Spring-kafka-test 包含一些有用的实用工具，可协助我们测试应用程序。最值得注意的是，它包含 EmbeddedKafkaBroker 类。

    有鉴于此，让我们继续编写第一个集成测试：

    test/.kafka.embedded/EmbeddedKafkaIntegrationTest.java

    让我们浏览一下测试的关键部分。

    首先，我们用两个非常标准的 Spring 注解来装饰我们的测试类：

    - [@SpringBootTest](https://www.baeldung.com/spring-boot-testing) 注解将确保我们的测试启动 Spring 应用程序上下文。
    - 我们还使用了 [@DirtiesContext](https://www.baeldung.com/spring-dirtiescontext) 注解，它将确保在不同测试之间对上下文进行清理和重置。

    关键部分来了--我们使用 @EmbeddedKafka 注解将 EmbeddedKafkaBroker 的实例注入测试中。

    此外，我们还可以使用几个属性来配置嵌入式 Kafka 节点：

    - partitions - 这是每个主题使用的分区数量。为了让事情简单明了，我们只希望在测试中使用一个。
    - brokerProperties - Kafka 代理的附加属性。我们再次保持简单，指定一个纯文本监听器和一个端口号。

    接下来，我们自动连接消费者和生产者类，并配置一个主题以使用 application.properties 中的值。

    最后，我们只需向测试主题发送一条消息，并验证消息是否已收到，以及是否包含测试主题的名称。

    当我们运行测试时，我们将看到以下 Spring 冗余输出：

    ```log
    ...
    12:45:35.099 [main] INFO  c.b.kafka.embedded.KafkaProducer -
    sending payload='Sending with our own simple KafkaProducer' to topic='embedded-test-topic'
    ...
    12:45:35.103 [org.springframework.kafka.KafkaListenerEndpointContainer#0-0-C-1]
    INFO  c.b.kafka.embedded.KafkaConsumer - received payload=
    'ConsumerRecord(topic = embedded-test-topic, partition = 0, leaderEpoch = 0, offset = 1,
    CreateTime = 1605267935099, serialized key size = -1, 
    serialized value size = 41, headers = RecordHeaders(headers = [], isReadOnly = false),
    key = null, value = Sending with our own simple KafkaProducer key)'
    ```

    这证明我们的测试工作正常。太棒了 我们现在有办法使用内存 Kafka 代理来编写自足、独立的集成测试了。

6. 使用测试容器测试 Kafka

    有时，我们可能会看到真实的外部服务与专门为测试目的而提供的嵌入式服务内存实例之间存在细微差别。虽然可能性不大，但也有可能是我们测试使用的端口被占用，导致测试失败。

    考虑到这一点，在本节中，我们将看到之前使用 Testcontainers 框架进行测试的方法的变体。我们将了解如何通过集成测试实例化和管理托管在 Docker 容器中的外部 Apache Kafka 代理。

    让我们定义另一个集成测试，它与上一节中的测试非常相似：

    test/.kafka.testcontainers/KafkaTestContainersLiveTest.java

    让我们来看看两者的区别。我们正在声明 kafka 字段，这是一个标准的 JUnit @ClassRule。该字段是 KafkaContainer 类的实例，它将准备和管理运行 Kafka 的容器的生命周期。

    为了避免端口冲突，Testcontainers 会在我们的 docker 容器启动时动态分配端口号。

    为此，我们使用 KafkaTestContainersConfiguration（inner KafkaTestContainersLiveTest.java） 类提供了自定义的消费者（consumerConfigs()）和生产者工厂配置（producerFactory()）。

    然后，我们在测试开始时通过 @Import 注解引用该配置。

    这样做的原因是，我们需要一种将服务器地址注入应用程序的方法，如前所述，服务器地址是动态生成的。

    为此，我们需要调用 getBootstrapServers() 方法，该方法将返回引导服务器的位置：

    `bootstrap.servers = [PLAINTEXT://localhost:32789] `

    现在，当我们运行测试时，我们会看到 Testcontainers 做了几件事：

    - 检查本地 Docker 设置
    - 必要时调用 confluentinc/cp-kafka:5.4.3 docker 映像
    - 启动一个新容器，等待它准备就绪
    - 最后，在测试完成后关闭并删除容器

    通过检查测试输出，我们可以再次确认这一点：

    ```log
    13:33:10.396 [main] INFO  ? [confluentinc/cp-kafka:5.4.3]
    - Creating container for image: confluentinc/cp-kafka:5.4.3
    13:33:10.454 [main] INFO  ? [confluentinc/cp-kafka:5.4.3]
    - Starting container with ID: b22b752cee2e9e9e6ade38e46d0c6d881ad941d17223bda073afe4d2fe0559c3
    13:33:10.785 [main] INFO  ? [confluentinc/cp-kafka:5.4.3]
    - Container confluentinc/cp-kafka:5.4.3 is starting: b22b752cee2e9e9e6ade38e46d0c6d881ad941d17223bda073afe4d2fe0559c3
    ```

    预览！使用 Kafka docker 容器完成的集成测试。

7. 总结

    在本文中，我们了解了使用 Spring Boot 测试 Kafka 应用程序的几种方法。

    在第一种方法中，我们了解了如何配置和使用本地内存 Kafka 代理。

    然后，我们了解了如何使用 Testcontainers 设置在 docker 容器内运行的外部 Kafka 代理。
