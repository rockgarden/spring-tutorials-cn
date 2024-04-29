# [如何在 Spring-Kafka 中捕获反序列化错误？](https://www.baeldung.com/spring-kafka-deserialization-errors)

1. 概述

    在本文中，我们将了解 Spring-Kafka 的 RecordDeserializationException。然后，我们将创建一个自定义错误处理程序来捕获该异常，并跳过无效消息，允许消费者继续处理下一个事件。

    本文依赖 Spring Boot 的 Kafka 模块，这些模块提供了与代理交互的便捷工具。 要深入了解 Kafka 的内部结构，我们可以重温一下该平台的基本概念。

2. 创建 Kafka 监听器

    在本文的代码示例中，我们将使用一个小型应用程序来监听主题 "baeldung.articles.published" 并处理传入的消息。为了展示自定义错误处理，我们的应用程序应在遇到反序列化异常后继续消费消息。  

    Spring-Kafka 的版本将由父 Spring Boot pom 自动解决。因此，我们只需添加模块依赖关系即可：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    ```

    该模块使我们能够使用 @KafkaListener 注解，它是对 [Kafka 的 Consumer API](https://www.baeldung.com/kafka-create-listener-consumer-api) 的抽象。让我们利用这个注解来创建 ArticlesPublishedListener 组件。此外，我们还将引入另一个组件--EmailService，它将为每一条传入的消息执行一个操作：

    main/.spring.kafka.deserialization.exception/ArticlesPublishedListener.java

    对于消费者配置，我们将只专注于定义对我们的示例至关重要的属性。在开发生产应用程序时，我们可以根据具体需要调整这些属性，或者将它们外部化到单独的配置文件中：

    main/.spring.kafka.deserialization.exception/KafkaConfig.java:onsumerFactory() 

    ```java
    @Bean
    KafkaListenerContainerFactory<String, ArticlePublishedEvent> kafkaListenerContainerFactory(
    ConsumerFactory<String, ArticlePublishedEvent> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ArticlePublishedEvent>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
    ```

3. 设置测试环境

    要设置测试环境，我们可以利用 Kafka Testcontainer，它可以无缝启动 Kafka Docker 容器进行测试：

    test/.spring.kafka.deserialization.exception/DeserializationExceptionLiveTest.java

    除此之外，我们还需要 KafkaProducer 和 EmailService 来验证监听器的功能。这些组件将向监听器发送消息，并验证消息的准确处理。为了简化测试并避免模拟，我们先将所有传入的文章保存在内存中的列表中，之后再使用 getter 访问它们：

    main/.spring.kafka.deserialization.exception/EmailService.java

    因此，我们只需将 EmailService 注入测试类即可。让我们继续创建 testKafkaProducer：

    ```java
    @Autowired
    EmailService emailService;

    static KafkaProducer<String, String> testKafkaProducer;

    @BeforeAll
    static void beforeAll() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        testKafkaProducer = new KafkaProducer<>(props);
    }
    ```

    有了这些设置，我们就可以测试快乐流程了。让我们

    有了这些设置，我们就可以测试快乐流程了。让我们用有效的 JSON 发布两篇文章，并验证应用程序是否成功调用了每篇文章的电子邮件服务：

    test/.spring.kafka.deserialization.exception/DeserializationExceptionLiveTest.java:whenPublishingValidArticleEvent_thenProcessWithoutErrors()

4. 引发记录反序列化异常

    如果配置的反序列化器无法正确解析消息的键或值，Kafka 就会抛出 RecordDeserializationException（记录反序列化异常）。要重现这一错误，我们只需发布一条包含无效 JSON 主体的消息：

    test/.spring.kafka.deserialization.exception/DeserializationExceptionLiveTest.java:whenPublishingInvalidArticleEvent_thenCatchExceptionAndContinueProcessing()

    如果我们运行该测试并检查控制台，就会发现一个重复出现的错误日志：

    ```log
    ERROR 7716 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Consumer exception

    **java.lang.IllegalStateException: This error handler cannot process 'SerializationException's directly; please consider configuring an 'ErrorHandlingDeserializer' in the value and/or key deserializer**
    at org.springframework.kafka.listener.DefaultErrorHandler.handleOtherException(DefaultErrorHandler.java:151) ~[spring-kafka-2.8.11.jar:2.8.11]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.handleConsumerException(KafkaMessageListenerContainer.java:1815) ~[spring-kafka-2.8.11.jar:2.8.11]
    ...
    **Caused by: org.apache.kafka.common.errors.RecordDeserializationException: Error deserializing key/value for partition baeldung.articles.published-0 at offset 1. If needed, please seek past the record to continue consumption.**
    at org.apache.kafka.clients.consumer.internals.Fetcher.parseRecord(Fetcher.java:1448) ~[kafka-clients-3.1.2.jar:na]
    ...
    **Caused by: org.apache.kafka.common.errors.SerializationException: Can't deserialize data** [[32, 33, 33, 32, 73, 110, 118, 97, 108, 105, 100, 32, 74, 83, 79, 78, 32, 33, 33, 32]] from topic [baeldung.articles.published]
    at org.springframework.kafka.support.serializer.JsonDeserializer.deserialize(JsonDeserializer.java:588) ~[spring-kafka-2.8.11.jar:2.8.11]
    ...
    **Caused by: com.fasterxml.jackson.core.JsonParseException: Unexpected character ('!' (code 33))**: expected a valid value (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
    **at [Source: (byte[])" !! Invalid JSON !! "; line: 1, column: 3]**
    at com.fasterxml.jackson.core.JsonParser._constructError(JsonParser.java:2391) ~[jackson-core-2.13.5.jar:2.13.5]
    ...
    ```

    然后，测试最终会超时并失败。如果我们检查断言错误，就会发现只有第一条消息被成功处理：

    ```log
    org.awaitility.core.ConditionTimeoutException: Assertion condition 
    Expecting actual:
    ["Introduction to Kafka"]
    to contain exactly in any order:
    ["Introduction to Kafka", "Kafka Streams Tutorial"]
    but could not find the following elements:
    ["Kafka Streams Tutorial"]
    within 5 seconds.
    ```

    不出所料，第二条信息的反序列化失败了。因此，监听器继续尝试消费相同的消息，导致错误重复发生。

5. 创建错误处理程序

    如果我们仔细分析故障日志，会发现两个建议：

    - 考虑配置一个 "ErrorHandlingDeserializer"；
    - 如果需要，请搜索过记录继续消费；

    换句话说，我们可以创建一个自定义错误处理程序来处理反序列化异常，并增加消费者偏移量。这样我们就可以跳过无效信息，继续消费。

    1. 实现 CommonErrorHandler

        要实现 CommonErrorHandler 接口，我们必须覆盖两个没有默认实现的公共方法：

        - handleOne() - 调用该方法处理一条失败记录；
        - handleOtherException() - 当出现异常时调用，但不是针对某条记录；

        我们可以使用类似的方法处理这两种情况。让我们从捕获异常和记录错误信息开始：

        main/.spring.kafka.deserialization.exception/KafkaErrorHandler.java

    2. Kafka 消费者的 seek() 和 commitSync()

        我们可以使用消费者接口中的 seek() 方法，手动更改主题中特定分区的当前偏移位置。简单地说，我们可以使用它根据偏移量重新处理或跳过所需的消息。

        在我们的例子中，如果异常是 RecordDeserializationException 的实例，我们就会调用 seek() 方法来获取主题分区和下一个偏移量：

        ```java
        void handle(Exception exception, Consumer<?, ?> consumer) {
            log.error("Exception thrown", exception);
            if (exception instanceof RecordDeserializationException ex) {
                consumer.seek(ex.topicPartition(), ex.offset() + 1L);
                consumer.commitSync();
            } else {
                log.error("Exception not handled", exception);
            }
        }
        ```

        我们可以注意到，我们需要调用 Consumer 接口中的 commitSync()。这将提交偏移量，并确保新位置被 Kafka 代理确认和持久化。这一步至关重要，因为它更新了消费者组提交的偏移量，表明截至调整位置的消息已被成功处理。

    3. 更新配置

        最后，我们需要在消费者配置中添加自定义错误处理程序。首先将其声明为 @Bean：

        ```java
        @Bean
        CommonErrorHandler commonErrorHandler() {
            return new KafkaErrorHandler();
        }
        ```

        之后，我们将使用 ConcurrentKafkaListenerContainerFactory 的专用设置器将新 bean 添加到 ConcurrentKafkaListenerContainerFactory 中：

        main/.spring.kafka.deserialization.exception/kafkaListenerContainerFactory()

        就是这样！现在我们可以重新运行测试，并期待监听器跳过无效消息，继续消费消息。

6. 总结

    在本文中，我们讨论了 Spring Kafka 的 RecordDeserializationException，发现如果处理不当，就会阻塞给定分区的消费者组。

    随后，我们深入研究了 Kafka 的 CommonErrorHandler 接口并实现了它，使我们的监听器能够在继续处理消息的同时处理反序列化失败。我们利用消费者的 API 方法（即 seek() 和 commitSync()），通过相应调整消费者偏移量来绕过无效消息。
