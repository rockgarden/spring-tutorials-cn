# [使用 Kafka MockProducer](https://www.baeldung.com/kafka-mockproducer)

[Data](https://www.baeldung.com/category/data) [测试](https://www.baeldung.com/category/testing)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    Kafka 是一个围绕分布式消息队列构建的消息处理系统。它提供了一个 Java 库，使应用程序可以向 Kafka 主题写入数据或从 Kafka 主题读取数据。

    现在，由于大部分业务领域逻辑都是通过单元测试验证的，因此应用程序通常会在 JUnit 中模拟所有 I/O 操作。Kafka 还提供了一个 MockProducer 来模拟生产者应用程序。

    在本教程中，我们将首先实现一个 Kafka 生产者应用程序。随后，我们将使用 MockProducer 实现一个单元测试，以验证常见的生产者操作。

2. Maven 依赖项

    在实现生产者应用程序之前，我们要为 kafka-clients 添加一个 Maven 依赖：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.4.0</version>
    </dependency>
    ```

3. 模拟生产者

    kafka-clients 库包含一个用于在 Kafka 中发布和消费消息的 Java 库。生产者应用程序可以使用这些 API 向 Kafka 主题发送键值记录：

    main/.kafka.producer/KafkaProducer.java

    任何 Kafka 生产者都必须实现客户端库中的生产者接口。Kafka 还提供了一个 KafkaProducer 类，这是一个具体的实现，用于执行面向 Kafka 代理的 I/O 操作。

    此外，Kafka 还提供了一个 MockProducer，它实现了相同的 Producer 接口，并模拟了在 KafkaProducer 中实现的所有 I/O 操作：

    test/.kafka.producer/givenKeyValue_whenSend_thenVerifyHistory()

    虽然这些 I/O 操作也可以用 [Mockito](https://www.baeldung.com/mockito-series) 来模拟，但 MockProducer 让我们可以访问许多我们需要在模拟的基础上实现的功能。其中一个功能就是 history() 方法。MockProducer 会缓存调用 send() 的记录，因此我们可以验证生产者的发布行为。

    此外，我们还可以验证主题名称、分区、记录键或值等元数据：

    ```java
    assertTrue(mockProducer.history().get(0).key().equalsIgnoreCase("data"));
    assertTrue(recordMetadataFuture.get().partition() == 0);
    ```

4. 模拟 Kafka 集群

    在迄今为止的模拟测试中，我们假设主题只有一个分区。然而，为了在生产者和消费者线程之间实现最大并发性，Kafka 主题通常被分成多个分区。

    这允许生产者将数据写入多个分区。这通常是通过基于键的记录分区和将特定键映射到特定分区来实现的：

    main/.kafka.producer/KafkaProducerUnitTest.java:EvenOddPartitioner.java

    因此，所有偶数长度的键都将发布到分区 "0"，同样，奇数长度的键也将发布到分区 "1"。

    MockProducer 使我们能够通过模拟具有多个分区的 Kafka 集群来验证这种分区分配算法：

    test/.kafka.producer/KafkaProducerUnitTest.java:givenKeyValue_whenSendWithPartitioning_thenVerifyPartitionNumber()

    我们模拟了一个有两个分区（0 和 1）的集群，然后可以验证 EvenOddPartitioner 是否将记录发布到了分区 1。

5. 用 MockProducer 模拟错误

    到目前为止，我们只模拟了生产者将记录成功发送到 Kafka 主题。但是，如果在写记录时出现异常会怎样呢？

    应用程序通常会通过重试或向客户端抛出异常来处理此类异常。

    MockProducer 允许我们在 send() 过程中模拟异常，从而验证异常处理代码：

    test/.kafka.producer/KafkaProducerUnitTest.java:givenKeyValue_whenSend_thenReturnException()

    这段代码中有两处值得注意。

    首先，我们在调用 MockProducer 构造函数时将 autoComplete 设为 false。这就告诉 MockProducer 在完成 send() 方法之前等待输入。

    其次，我们将调用 mockProducer.errorNext(e)，这样 MockProducer 就会为最后一次 send() 调用返回一个异常。

6. 使用 MockProducer 模拟事务写入

    Kafka 0.11 在 Kafka 中间商、生产者和消费者之间引入了事务。这允许在 Kafka 中实现端到端精确一次性消息传递语义。简而言之，这意味着事务生产者只能通过两阶段提交协议向代理发布记录。

    MockProducer 也支持事务性写入，并允许我们验证这种行为：

    test/.kafka.producer/KafkaProducerUnitTest.java:givenKeyValue_whenSendWithTxn_thenSendOnlyOnTxnCommit()

    由于 MockProducer 也支持与具体的 KafkaProducer 相同的 API，因此它只会在我们提交事务后更新历史记录。这种模拟行为可以帮助应用程序验证每个事务都调用了 commitTransaction()。

7. 结论

    在本文中，我们介绍了 kafka-client 库中的 MockProducer 类。我们讨论了 MockProducer 实现了与具体的 KafkaProducer 相同的层次结构，因此我们可以模拟 Kafka 代理的所有 I/O 操作。

    我们还讨论了一些复杂的模拟场景，并使用 MockProducer 测试了异常、分区和事务。
