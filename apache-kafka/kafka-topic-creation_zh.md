# [使用Java创建Kafka主题](https://www.baeldung.com/kafka-topic-creation)

[Data](https://www.baeldung.com/category/data)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    在本教程中，我们将简要介绍 Apache Kafka，然后了解如何在 Kafka 集群中以编程方式创建和配置主题。

2. Kafka 简介

    Apache Kafka 是一个功能强大、高性能的分布式事件流平台。

    一般来说，生产者应用程序向 Kafka 发布事件，而消费者则订阅这些事件以读取和处理它们。Kafka 使用主题来存储和分类这些事件，例如，在电子商务应用程序中，可能会有一个 "订单" 主题。

    Kafka 主题是分区的，可将数据分布到多个代理，以提高可扩展性。它们可以被复制，以使数据具有容错性和高可用性。即使在数据消耗后，主题也会根据需要长时间保留事件。所有这些都通过 Kafka 命令行工具和键值配置按主题进行管理。

    不过，除了命令行工具，Kafka 还提供了[管理员API](https://kafka.apache.org/28/javadoc/org/apache/kafka/clients/admin/Admin.html)来管理和检查主题、代理和其他 Kafka 对象。在我们的示例中，我们将使用该 API 创建新的主题。

3. 依赖关系

    要使用管理 API，让我们在 pom.xml 中添加 kafka-clients 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.4.0</version>
    </dependency>
    ```

4. 设置 Kafka

    在创建新主题之前，我们至少需要一个单节点 Kafka 集群。

    在本教程中，我们将使用 [Testcontainers](https://www.testcontainers.org/) 框架实例化一个 Kafka 容器。这样，我们就可以运行可靠且独立的集成测试，而无需依赖外部 Kafka 服务器的运行。为此，我们还需要两个专门用于测试的依赖项。

    首先，让我们将 [Testcontainers Kafka](https://mvnrepository.com/artifact/org.testcontainers/kafka) 依赖项添加到 pom.xml 中，接下来，我们将添加用于使用 JUnit 5 运行 Testcontainer 测试的 junit-jupiter 工具：

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

    现在我们已经配置了所有必要的依赖项，可以编写一个简单的应用程序，以编程方式创建新主题。

5. 管理 API

    首先，让我们为本地代理创建一个新的 [Properties](https://www.baeldung.com/java-properties) 实例，并进行最少的配置：

    ```java
    Properties properties = new Properties();
    properties.put(
    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers()
    );
    ```

    现在我们可以获取一个 Admin 实例：

    `Admin admin = Admin.create(properties)`

    创建方法接受一个带有 [bootstrap.servers](https://kafka.apache.org/documentation/#adminclientconfigs_bootstrap.servers) 属性的 Properties 对象（或 Map），并返回一个线程安全的实例。

    管理员客户端使用该属性发现群集中的代理，并随后执行任何管理操作。因此，通常包含两到三个代理地址就足够了，以应对某些实例不可用的可能性。

    [AdminClientConfig](https://kafka.apache.org/28/javadoc/org/apache/kafka/clients/admin/AdminClientConfig.html) 类包含所有[管理员客户端](https://kafka.apache.org/documentation/#adminclientconfigs)配置项的常量。

6. 创建主题

    让我们先用[Testcontainers创建一个JUnit5测试](https://www.testcontainers.org/quickstart/junit_5_quickstart/)，以验证主题创建是否成功。我们将使用 Kafka 模块，该模块使用 [Confluent OSS Platform](https://hub.docker.com/r/confluentinc/cp-kafka/) 的官方 [Kafka Docker](https://www.testcontainers.org/modules/kafka/) 镜像：

    main/.kafka.admin/KafkaTopicApplicationLiveTest.java:givenTopicName_whenCreateNewTopic_thenTopicIsCreated()

    在此，Testcontainers 将在测试执行期间自动实例化和管理 Kafka 容器。我们只需调用应用代码，并验证主题是否已在运行的容器中成功创建。

    1. 使用默认选项创建

        主题分区和复制因子是新主题的主要考虑因素。我们将保持简单，以 1 个分区和 1 个复制因子创建示例主题：

        main/.kafka.admin/KafkaTopicApplication.java:createTopic()

        在这里，我们使用 Admin.createTopics 方法创建了一批带有默认选项的新主题。由于 Admin 接口扩展了 AutoCloseable 接口，我们使用了 [try-with-resources](https://www.baeldung.com/java-try-with-resources) 来执行操作。这样可以确保资源得到适当释放。

        重要的是，该方法与控制器代理通信，并异步执行。返回的 CreateTopicsResult 对象会暴露一个 KafkaFuture，用于访问请求批次中每个项目的结果。这遵循了 Java 异步编程模式，允许调用者使用 [Future.get](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html#get()) 方法获取操作结果。

        对于同步行为，我们可以立即调用该方法来获取操作结果。该方法会阻塞，直到操作完成或失败。如果操作失败，则会产生执行异常（ExecutionException），该异常封装了根本原因。

    2. 使用选项创建

        除了默认选项，我们还可以使用 Admin.createTopics 方法的重载形式，并通过 [CreateTopicsOptions](https://kafka.apache.org/28/javadoc/org/apache/kafka/clients/admin/CreateTopicsOptions.html) 对象提供一些选项。我们可以使用这些选项来修改创建新主题时管理员客户端的行为：

        ```java
        CreateTopicsOptions topicOptions = new CreateTopicsOptions()
        .validateOnly(true)
        .retryOnQuotaViolation(false);
        CreateTopicsResult result = admin.createTopics(
        Collections.singleton(newTopic), topicOptions
        );
        ```

        在这里，我们将 validateOnly 选项设置为 true，这意味着客户端只会进行验证，而不会实际创建主题。同样，retryOnQuotaViolation 选项也被设置为 false，这样在配额违反的情况下就不会重试操作。

    3. 新主题配置

        Kafka 有一系列控制主题行为的[主题配置](https://kafka.apache.org/documentation.html#topicconfigs)，如[数据保留](https://www.baeldung.com/kafka-message-retention)和压缩等。这些配置既有服务器默认值，也有可选的每个主题覆盖值。

        我们可以使用新主题的配置映射来提供主题配置：

        ```java
        // Create a compacted topic with 'lz4' compression codec
        Map<String, String> newTopicConfig = new HashMap<>();
        newTopicConfig.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
        newTopicConfig.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "lz4");

        NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor)
        .configs(newTopicConfig);
        ```

        管理 API 中的 [TopicConfig](https://kafka.apache.org/28/javadoc/org/apache/kafka/common/config/TopicConfig.html) 类包含可用于在创建时配置主题的密钥。

7. 其他主题操作

    除了创建新主题的功能外，Admin API 还有[delete](https://kafka.apache.org/28/javadoc/org/apache/kafka/clients/admin/Admin.html#deleteTopics(java.util.Collection))、[list](https://kafka.apache.org/28/javadoc/org/apache/kafka/clients/admin/Admin.html#listTopics())和[describe](https://kafka.apache.org/28/javadoc/org/apache/kafka/clients/admin/Admin.html#describeTopics(java.util.Collection))主题的操作。所有这些与主题相关的操作都遵循我们在创建主题时看到的相同模式。

    每个操作方法都有一个重载版本，将 xxxTopicOptions 对象作为输入。所有这些方法都会返回相应的 xxxTopicsResult 对象。这反过来又为访问异步操作的结果提供了 KafkaFuture。

    最后，值得一提的是，自 Kafka 0.11.0.0 版引入以来，管理员 API 仍在不断发展，这一点已通过 InterfaceStability.Evolving 注解说明。这意味着 API 在未来可能会发生变化，而一个小版本可能会破坏兼容性。

8. 结束语

    在本教程中，我们了解了如何使用 Java 管理客户端在 Kafka 中创建新主题。

    首先，我们创建了一个默认主题，然后创建了一个明确选项的主题。接着，我们了解了如何使用各种属性配置新主题。最后，我们简要介绍了使用管理员客户端进行的其他与主题相关的操作。

    同时，我们还了解了如何使用 Testcontainers 从测试中建立一个简单的单节点集群。

    本文的完整源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/apache-kafka) 上获取。
