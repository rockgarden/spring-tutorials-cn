# [Kafka中的提交偏移](https://www.baeldung.com/kafka-commit-offsets)

1. 概述

    在 Kafka 中，消费者从分区读取消息。在读取消息时，需要考虑一些问题，例如确定从分区中读取哪些消息，或者防止重复读取消息或在发生故障时丢失消息。解决这些问题的方法就是使用偏移量。

    在本教程中，我们将学习 Kafka 中的偏移量。我们将了解如何提交偏移量来管理消息消耗，并讨论其方法和缺点。

2. 什么是偏移量？

    我们知道，Kafka 将消息存储在主题中，每个主题可以有多个分区。每个消费者从一个主题的一个分区中读取消息。在这里，Kafka 借助偏移量来跟踪消费者读取的消息。偏移量是一个从零开始的整数，随着消息被存储，偏移量会以一为单位递增。

    假设一个消费者从一个分区中读取了五条消息。然后，根据配置，Kafka 会将直到 4 的偏移量标记为已提交（基于零的序列）。消费者在下一次尝试读取消息时，就会消耗偏移量为 5 以后的消息。

    没有偏移量，就无法避免重复处理或数据丢失。这就是它如此重要的原因。

    我们可以将其与数据库存储进行类比。在数据库中，我们会在执行 SQL 语句后提交，以保持更改。同样，从分区读取数据后，我们要提交偏移量，以标记已处理信息的位置。

3. 提交偏移量的方法

    提交偏移量有四种方法。我们将详细介绍每种方法，并讨论它们的用例、优缺点。

    首先，让我们在 pom.xml 中添加 Kafka 客户端 API 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.6.1</version>
    </dependency>
    ```

    1. 自动提交

        这是提交偏移量的最简单方法。Kafka 默认使用自动提交--每五秒提交 poll() 方法返回的最大偏移量。poll() 返回一组消息，超时 10 秒，我们可以在代码中看到：

        ```java
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(KafkaConfigProperties.getTopic());
        ConsumerRecords<Long, String> messages = consumer.poll(Duration.ofSeconds(10));
        for (ConsumerRecord<Long, String> message : messages) {
        // processed message
        }
        ```

        自动提交的问题在于，如果应用程序发生故障，数据丢失的几率非常高。当 poll() 返回消息时，Kafka 可能会在处理消息之前提交最大的偏移量。

        假设 poll() 返回 100 条消息，消费者在自动提交时处理了 60 条消息。然后，由于某些故障，消费者崩溃了。当一个新的消费者上线读取消息时，它会从偏移量 101 开始读取，导致 61 到 100 之间的消息丢失。

        因此，我们需要其他不存在这种缺陷的方法。答案就是手动提交。

    2. 手动同步提交

        无论是同步提交还是异步提交，手动提交时都需要将默认属性（enabled.auto.commit 属性）设为 false，从而禁用自动提交：

        ```java
        Properties props = new Properties();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        ```

        禁用手动提交后，让我们来了解一下 commitSync() 的用法：

        ```java
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(KafkaConfigProperties.getTopic());
        ConsumerRecords<Long, String> messages = consumer.poll(Duration.ofSeconds(10));
        //process the messages
        consumer.commitSync();
        ```

        这种方法只有在处理完报文后才提交偏移量，从而防止数据丢失。但是，当消费者在提交偏移量之前崩溃时，它并不能防止重复读取。除此之外，它还会影响应用程序的性能。

        commitSync() 会阻塞代码，直到完成为止。此外，如果出现错误，它会不断重试。这会降低应用程序的吞吐量，这是我们不希望看到的。因此，Kafka 提供了另一种解决方案--异步提交，以解决这些缺点。

    3. 手动异步提交

        Kafka 提供了 commitAsync() 来异步提交偏移量。它通过在不同的线程中提交偏移量，克服了手动同步提交的性能开销。让我们实现一个异步提交来理解这一点：

        ```java
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props); 
        consumer.subscribe(KafkaConfigProperties.getTopic()); 
        ConsumerRecords<Long, String> messages = consumer.poll(Duration.ofSeconds(10));
        //process the messages
        consumer.commitAsync();
        ```

        异步提交的问题在于，它不会在失败时重试。它依赖于下一次调用 commitAsync()，这将提交最新的偏移量。

        假设 300 是我们要提交的最大偏移量，但由于某些问题，我们的 commitAsync() 失败了。可能在重试之前，另一次调用 commitAsync() 会提交最大偏移量 400，因为它是异步的。当 commitAsync() 重试失败时，如果它成功提交了 300 个偏移量，就会覆盖之前提交的 400 个偏移量，导致重复读取。这就是 commitAsync() 不重试的原因。

    4. 提交特定的偏移量

        有时，我们需要对偏移量进行更多控制。比方说，我们正在小批量处理报文，并希望在报文处理完毕后立即提交偏移量。我们可以使用带有 map 参数的 commitSync() 和 commitAsync() 的重载方法来提交特定的偏移量：

        mian/.kafka.commitoffset/SpecificOffsetCommit.java

        在这段代码中，我们管理一个 currentOffsets 映射，它将 TopicPartition 作为键，将 OffsetAndMetadata 作为值。在处理信息时，我们会将已处理信息的 TopicPartition 和 OffsetAndMetadata 插入 currentOffsets 映射。当处理的消息数量达到 50 条时，我们会调用带有 currentOffsets 映射的 commitSync() 来标记这些消息已提交。

        这种方式的行为与同步和异步提交相同。唯一不同的是，这里是我们决定要提交的偏移量，而不是 Kafka。

4. 结论

    在本文中，我们了解了偏移量及其在 Kafka 中的重要性。此外，我们还探讨了手动和自动提交偏移量的四种方法。最后，我们分析了它们各自的优缺点。我们可以得出这样的结论：在 Kafka 中并没有明确的最佳提交方式；相反，它取决于具体的使用情况。
