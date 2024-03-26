# 使用消费者 API 创建 Kafka 监听器

[Data](https://www.baeldung.com/category/data)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    在本教程中，我们将学习如何创建 Kafka 监听器，并使用 Kafka 的 Consumer API 从主题中消费消息。之后，我们将使用 Producer API 和 [Testcontainers](https://www.baeldung.com/docker-test-containers) 测试我们的实现。

    我们将专注于在不依赖 Spring Boot 模块的情况下设置 KafkaConsumer。

2. 创建自定义 Kafka 监听器

    我们的自定义监听器将在内部使用 kafka-clients 库中的 Producer 和 Consumer API。让我们先在 pom.xml 文件中添加这个依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.6.1</version>
    </dependency>
    ```

    在本文的代码示例中，我们将创建一个 CustomKafkaListener 类，该类将监听名为 "baeldung.articles.published" 的主题。在内部，我们的类将围绕一个 KafkaConsumer，并利用它来订阅主题：

    ```java
    class CustomKafkaListener {
        private final String topic;
        private final KafkaConsumer<String, String> consumer;

        // ...
    }
    ```

    1. 创建 KafkaConsumer

        要创建 KafkaConsumer，我们需要通过 Properties 对象提供有效的配置。让我们创建一个简单的消费者，作为创建 CustomKafkaListener 实例时的默认配置：

        main/.kafka.consumer/CustomKafkaListener.java:defaultKafkaConsumer()

        在这个示例中，我们硬编码了大部分属性，但理想情况下，这些属性应该从配置文件中加载。让我们快速了解一下每个属性的含义：

        - Boostrap Servers：用于建立与 Kafka 集群的初始连接的主机和端口对列表。
        - 组 ID：允许一组消费者共同使用一组主题分区的 ID
        - 自动偏移重置：当没有初始偏移时，在 Kafka 日志中开始读取数据的位置
        - 键/值反序列化器：键和值的反序列化器类。在我们的示例中，我们将使用字符串键和值以及以下反序列化器：org.apache.kafka.common.serialization.StringDeserializer

        有了这些最基本的配置，我们就能订阅主题并轻松测试实现。有关可用属性的完整列表，请查阅[官方文档](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html)。

    2. 订阅主题

        现在，我们需要提供一种方法来订阅主题并开始轮询消息。这可以使用 KafkaConsumer 的 subscribe() 方法，然后无限循环调用 poll() 方法来实现。此外，由于该方法会阻塞线程，我们可以实现 Runnable 接口，以便与 CompletableFuture 很好地集成：

        main/.kafka.consumer/CustomKafkaListener.java:run()

        现在，我们的 CustomKafkaListener 可以像这样启动，而不会阻塞主线程：

        ```java
        String topic = "baeldung.articles.published";
        String bootstrapServers = "localhost:9092";

        var listener = new CustomKafkaListener(topic, bootstrapServers)
        CompletableFuture.runAsync(listener);
        ```

    3. 消耗活动

        目前，我们的应用程序只监听主题并记录所有传入消息。让我们进一步改进它，以允许更复杂的应用场景，并使其更具可测试性。例如，我们可以定义一个 `Consumer<String>` 来接受来自主题的每个新事件：

        main/.kafka.consumer/CustomKafkaListener.java: recordConsumer

        将 recordConsumer 声明为 `Consumer<String>` 允许我们使用默认方法 andThen() 链接多个函数。这些函数将针对每一条传入的消息逐一调用。

3. 测试

    为了测试我们的实现，我们将创建一个 KafkaProducer，并用它向 "baeldung.articles.published" 主题发布一些消息。然后，我们将启动 CustomKafkaListener 并验证它是否能准确处理所有活动。

    1. 设置 Kafka 测试容器

        我们可以利用 Testcontainers 库在测试环境中启动一个 Kafka 容器。首先，我们需要为 JUnit5 扩展和 Kafka 模块添加 Testcontainer 依赖项：

        ```xml
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        ```

        现在，我们可以创建一个带有特定 Docker 映像名称的 KafkaContainer。然后，我们将添加 @Container 和 @Testcontainers 注解，以允许 Testcontainers JUnit5 扩展管理容器的生命周期：

        test/.kafka.consumer/CustomKafkaListenerLiveTest.java: KAFKA_CONTAINER

    2. 创建并启动监听器

        首先，我们将把主题名称定义为硬编码字符串，并从 KAFKA_CONTAINER 中提取 bootstrapServers。此外，我们还将创建一个 `ArrayList<String>`，用于收集消息：

        ```java
        String topic = "baeldung.articles.published";
        String bootstrapServers = KAFKA_CONTAINER.getBootstrapServers();
        List<String> consumedMessages = new ArrayList<>();
        ```

        我们将使用这些属性创建一个 CustomKafkaListener 实例，并指示它捕获新消息并将其添加到 consumedMessages 列表中：

        ```java
        CustomKafkaListener listener = new CustomKafkaListener(topic, bootstrapServers).onEach(consumedMessages::add);
        listener.run();
        ```

        不过，需要注意的是，按原样运行可能会阻塞线程并冻结测试。为了避免这种情况，我们将使用 CompletableFuture 异步执行：

        `CompletableFuture.runAsync(listener);`

        虽然这对测试并不重要，但我们也可以首先在 try-with-resources 块中实例化监听器：

        ```java
        var listener = new CustomKafkaListener(topic, bootstrapServers).onEach(consumedMessages::add);
        CompletableFuture.runAsync(listener);
        ```

    3. 发布消息

        为了将文章名称发送到 "baeldung.articles.published" 主题，我们将使用一个 Properties 对象来设置一个 KafkaProducer，其方法与我们为消费者所做的类似。

        test/.kafka.consumer/CustomKafkaListenerLiveTest.java:testKafkaProducer()

        该方法将允许我们发布消息，以测试我们的实现。让我们创建另一个测试助手，它将为收到的每篇作为参数的文章发送一条消息：

        test/.kafka.consumer/CustomKafkaListenerLiveTest.java:publishArticles()

    4. 验证

        让我们把所有部分组合起来，运行我们的测试。我们已经讨论过如何创建 CustomKafkaListener 并开始发布数据：

        test/.kafka.consumer/CustomKafkaListenerLiveTest.java:givenANewCustomKafkaListener_thenConsumesAllMessages()

        我们的最后一项任务是等待异步代码完成，并确认 consumedMessages 列表包含预期内容。为此，我们将使用 [Awaitility](https://www.baeldung.com/awaitility-testing) 库，利用其 await().untilAsserted()：

        ```java
        // then
        await().untilAsserted(() -> 
        assertThat(consumedMessages).containsExactlyInAnyOrder(
            "Introduction to Kafka",
            "Kotlin for Java Developers",
            "Reactive Spring Boot",
            "Deploying Spring Boot Applications",
            "Spring Security"
        ));
        ```

4. 总结

    在本教程中，我们学习了如何在不依赖更高级 Spring 模块的情况下使用 Kafka 的消费者和生产者 API。首先，我们使用封装了 KafkaConsumer 的 CustomKafkaListener 创建了一个消费者。为了进行测试，我们实现了一个 KafkaProducer，并使用 Testcontainers 和 Awaitility 验证了我们的设置。

    一如既往，这些示例的源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/apache-kafka-2) 上获取。
