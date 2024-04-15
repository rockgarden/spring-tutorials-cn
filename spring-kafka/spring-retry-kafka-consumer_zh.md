# [在 Kafka 消费者中实现重试](https://www.baeldung.com/spring-retry-kafka-consumer)

1. 概述

    在本教程中，我们将讨论在 Kafka 中实施重试的重要性。我们将探讨在 Spring Boot 上实施重试的各种可用选项，并学习最大限度提高 Kafka Consumer 的可靠性和弹性的最佳实践。

2. 项目设置

    让我们创建一个新的 Spring Boot 项目，并添加 spring-kafka 依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>3.1.2</version>
    </dependency>
    ```

    现在让我们创建一个对象：main/.spring.retryable/Greeting.java

3. 卡夫卡消费者

    [Kafka 消费者](https://kafka.apache.org/documentation/#consumerapi)是从 Kafka 集群中读取数据的客户端应用程序。它订阅一个或多个主题，并消费已发布的消息。生产者向主题发送消息，主题是存储和发布记录的类别名称。主题被分为多个分区，以便横向扩展。每个分区都是一个不可更改的消息序列。

    [消费者](https://docs.confluent.io/platform/current/clients/consumer.html)可以通过指定偏移量（即消息在分区中的位置）来读取特定分区中的消息。ack（确认）是消费者发送给 Kafka 代理的消息，表示它已成功处理了一条记录。消费者偏移量将在收到回执后更新。

    这可以确保消息已被消耗，不会再传递给当前监听者。

    1. 应答模式

        确认模式决定了代理何时更新消费者偏移量。

        有三种确认模式：

        - 自动提交（auto-commit）：消费者在收到消息后立即向代理发送确认信息
        - 处理后(after-processing)：消费者只有在成功处理报文后才向代理发送确认信息
        - 手动(manual)：消费者在收到特定指令后才向代理发送确认。

        确认模式(Ack mode)决定了消费者如何处理从 Kafka 集群读取的消息。

        让我们创建一个新的 Bean，创建一个新的 ConcurrentKafkaListenerContainerFactory：

        main/.spring.retryable/KafkaConsumerConfig.java:greetingKafkaListenerContainerFactory()

        我们可以配置几种可用的 ack 模式：

        - AckMode.RECORD： 在这种处理后模式下，消费者会为其处理的每条消息发送确认。
        - AckMode.BATCH： 在这种手动模式下，消费者为一批报文发送确认，而不是为每条报文发送确认。
        - AckMode.COUNT：在这种手动模式下，用户在处理完特定数量的信息后发送确认。
        - AckMode.MANUAL（手动）： 在这种手动模式下，用户不对其处理的报文发送确认。
        - AckMode.TIME：在这种手动模式下，消费者会在一定时间后发送确认。

        要在 Kafka 中实现消息处理的重试逻辑，我们需要选择一种 AckMode。

        这种 AckMode 应该允许消费者向代理指出哪些特定消息已被成功处理。这样，经纪商就可以将任何未确认的消息重新传递给另一个消费者。

        在阻塞重试的情况下，这可能是 RECORD 或 MANUAL（手动）模式。

4. 阻塞重试

    阻塞重试使消费者能够在初次尝试因临时错误而失败时再次尝试消费报文。用户在再次尝试消费信息前会等待一定的时间（即重试延迟时间）。

    此外，用户还可以使用固定延迟或指数延迟策略自定义重试延迟时间。它还可以设置在放弃并将信息标记为失败之前的最大重试次数。

    1. 错误处理程序

        让我们在 Kafka 配置类上定义两个属性：

        ```java
        @Value(value = "${kafka.backoff.interval}")
        private Long interval;

        @Value(value = "${kafka.backoff.max_failure}")
        private Long maxAttempts;
        ```

        为了处理消费过程中抛出的所有异常，我们将定义一个新的错误处理程序：

        ```java
        @Bean
        public DefaultErrorHandler errorHandler() {
            BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
            DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
                // logic to execute when all the retry attemps are exhausted
            }, fixedBackOff);
            return errorHandler;
        }
        ```

        FixedBackOff 类需要两个参数：

        - interval：两次重试之间等待的时间，以毫秒为单位
        - maxAttempts：在放弃之前重试操作的最大次数。

        在此策略中，消费者在重试消息消费之前会等待固定的时间。

        DefaultErrorHandler 正在使用一个 lambda 函数进行初始化，该函数代表了当所有重试尝试都用尽时要执行的逻辑。

        该 lambda 函数有两个参数：

        - consumerRecord：表示导致错误的 Kafka 记录
        - exception：表示抛出的异常

    2. 容器工厂

        让我们在错误处理程序中添加一个容器工厂 Bean：

        main/.spring.retryable/KafkaConsumerConfig.java:greetingKafkaListenerContainerFactory()

        如果存在重试策略，我们会将应答模式设置为 AckMode.RECORD，以确保在处理过程中发生错误时，消费者会重新发送信息。

        我们不应将应答模式设置为 AckMode.BATCH 或 AckMode.TIME，因为消费者会同时应答多条信息。这是因为如果在处理报文时发生错误，消费者不会将批次或时间窗口中的所有报文都重新发送给自己。

        因此重试策略无法正确处理错误。

    3. 可重试异常和不可重试异常

        我们可以指定哪些异常是可重试的，哪些是不可重试的。

        让我们修改 ErrorHandler：

        main/.spring.retryable/KafkaConsumerConfig.java:errorHandler()

        在这里，我们指定了哪些异常类型应在消费者中触发重试策略。

        SocketTimeoutException 被认为是可重试的，而 NullPointerException 被认为是不可重试的。

        如果我们不设置任何可重试异常，则将使用默认的可重试异常集：

        [MessagingException(消息异常)](https://docs.oracle.com/javaee/7/api/javax/mail/MessagingException.html)
        [RetryableException(可重试异常)](https://javadoc.io/static/io.github.openfeign/feign-core/9.2.0/feign/RetryableException.html)
        [ListenerExecutionFailedException监听器执行失败异常](https://docs.spring.io/spring-kafka/docs/current/api/org/springframework/kafka/listener/ListenerExecutionFailedException.html)

    4. 优缺点

        在阻塞重试中，当消息处理失败时，消费者会阻塞，直到重试机制完成重试，或重试次数达到最大值。

        使用阻塞重试有几个优缺点。

        阻塞重试可以提高消息处理管道的可靠性，允许消费者在出现错误时重试消息的消费。这有助于确保即使发生瞬时错误，也能成功处理报文。

        通过抽象重试机制，阻塞重试可以简化消息处理逻辑的实现。用户可以专注于处理报文，让重试机制来处理可能出现的任何错误。

        最后，如果用户需要等待重试机制完成重试，阻塞重试可能会在消息处理管道中引入延迟。这会影响系统的整体性能。阻塞重试还可能导致消费者在等待重试机制完成重试时消耗更多资源，如 CPU 和内存。这会影响系统的整体可扩展性。

5. 非阻塞重试

    非阻塞重试允许消费者异步重试消息的消费，而不会阻塞消息监听器方法的执行。

    1. @RetryableTopic

        让我们在 KafkaListener 中添加 [@RetryableTopic](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/annotation/RetryableTopic.html) 注解：

        main/.spring.retryable/MultiTypeKafkaListener.java

        我们通过修改多个属性自定义了重试行为，例如

        - backoff： 该属性指定重试失败消息时使用的回退策略。
        - attempts尝试次数： 该属性指定在放弃之前重试消息的最大次数。
        - autoCreateTopics（自动创建主题）： 此属性用于指定是否在重试主题和 DLT(Dead Letter Topic) 不存在时自动创建它们。
        - include包括： 此属性指定应触发重试的异常。
        - exclude排除： 该属性指定了不应触发重试的异常情况。

        当邮件无法发送到预定的主题时，它会自动发送到重试主题进行重试。

        如果在最大尝试次数后，信息仍无法送达，它将被发送到 DLT 进行进一步处理。

    2. 优缺点

        实施非阻塞重试有几个优点：

        - 提高性能： 非阻塞重试允许在不阻塞调用线程的情况下重试失败的报文，这可以提高应用程序的整体性能。
        - 提高可靠性： 非阻塞重试可帮助应用程序从故障中恢复，并继续处理报文，即使某些报文未能送达。

        不过，在实施非阻塞重试时，也要考虑一些潜在的缺点：

        - 复杂性增加： 非阻塞重试会增加应用程序的复杂性，因为我们需要处理重试逻辑和 DLT。
        - 信息重复的风险： 如果消息在重试后成功交付，那么在原始交付和重试都成功的情况下，消息可能会被交付多次。我们需要考虑这一风险，并采取措施防止信息重复。
        - 信息的顺序： 重试的信息会异步发送到重试主题，可能会比未重试的信息更晚发送到原始主题。

6. 结论

    在本文中，我们分析了如何在 Kafka 主题上实现重试逻辑，包括阻塞和非阻塞方法。
