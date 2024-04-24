# [如何将 Kafka 消费者订阅到多个主题](https://www.baeldung.com/kafka-subscribe-consumer-multiple-topics)

1. 概述

    在本教程中，我们将学习如何将 Kafka 消费者订阅到多个主题。当不同的主题使用相同的业务逻辑时，这是一个常见的需求。

2. 创建模型类

    我们将考虑一个简单的支付系统，它有两个 Kafka 主题，一个用于银行卡支付，另一个用于银行转账。让我们创建模型类：

    main/.spring.kafka.multipletopics/PaymentData.java

3. 使用 Kafka 消费者 API 订阅多个主题

    我们要讨论的第一个方法使用的是 Kafka Consumer API。让我们添加所需的 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.5.1</version>
    </dependency>
    ```

    让我们来配置 Kafka 消费者：

    ```java
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "payments");
    kafkaConsumer = new KafkaConsumer<>(properties);
    ```

    在消费消息之前，我们需要使用 subscribe() 方法将 kafkaConsumer 订阅到两个主题：

    `kafkaConsumer.subscribe(Arrays.asList("card-payments", "bank-transfers"));`

    现在我们可以测试我们的配置了。让我们在每个主题上发布一条消息：

    ```java
    void publishMessages() throws Exception {
        ProducerRecord<String, String> cardPayment = new ProducerRecord<>("card-payments", 
        "{\"paymentReference\":\"A184028KM0013790\", \"type\":\"card\", \"amount\":\"275\", \"currency\":\"GBP\"}");
        kafkaProducer.send(cardPayment).get();
        ProducerRecord<String, String> bankTransfer = new ProducerRecord<>("bank-transfers",
        "{\"paymentReference\":\"19ae2-18mk73-009\", \"type\":\"bank\", \"amount\":\"150\", \"currency\":\"EUR\"}");
        kafkaProducer.send(bankTransfer).get();
    }
    ```

    最后，我们可以编写集成测试：

    main/.spring.kafka.multipletopics/KafkaMultipleTopicsIntegrationTest.java:whenSendingMessagesOnTwoTopics_thenConsumerReceivesMessages()

4. 使用 Spring Kafka 订阅多个主题

    我们要讨论的第二种方法使用 Spring Kafka。

    让我们在 pom.xml 中添加 spring-kafka 和 jackson-databind 依赖项：

    ```xml
    <dependency> 
        <groupId>org.springframework.kafka</groupId> 
        <artifactId>spring-kafka</artifactId>
        <version>3.1.2</version>
    </dependency>
    <dependency> 
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    ```

    让我们同时定义 ConsumerFactory 和 ConcurrentKafkaListenerContainerFactory Bean：

    ```java
    @Bean
    public ConsumerFactory<String, PaymentData> consumerFactory() {
        List<String, String> config = new HashMap<>();
        config.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        config.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(
        config, new StringDeserializer(), new JsonDeserializer<>(PaymentData.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentData> containerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentData> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
    ```

    我们需要使用 [@KafkaListener](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/annotation/KafkaListener.html) 注解的 topics 属性来订阅这两个主题：

    `@KafkaListener(topics = { "card-payments", "bank-transfers" }, groupId = "payments")`

    最后，我们可以创建消费者。此外，我们还加入了 Kafka 头信息，以识别接收到消息的主题：

    ```java
    @KafkaListener(topics = { "card-payments", "bank-transfers" }, groupId = "payments")
    public void handlePaymentEvents(
    PaymentData paymentData, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Event on topic={}, payload={}", topic, paymentData);
    }
    ```

    让我们验证一下配置：

    main/.spring.kafka.multipletopics/KafkaMultipleTopicsIntegrationTest.java:whenSendingMessagesOnTwoTopics_thenConsumerReceivesMessages()

5. 使用 Kafka CLI 订阅多个主题

    Kafka CLI 是我们要讨论的最后一种方法。

    首先，让我们在每个主题上发送一条消息：

    ```bash
    $ bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic card-payments
    >{"paymentReference":"A184028KM0013790", "type":"card", "amount":"275", "currency":"GBP"}

    $ bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic bank-transfers
    >{"paymentReference":"19ae2-18mk73-009", "type":"bank", "amount":"150", "currency":"EUR"}
    ```

    现在，我们可以启动 Kafka CLI 消费者。include 选项允许我们指定要包含在消息消费中的主题列表：

    `$ bin/kafka-console-consumer.sh --from-beginning --bootstrap-server localhost:9092 --include "card-payments|bank-transfers"`

    下面是我们运行上一条命令时的输出结果：

    ```log
    {"paymentReference":"A184028KM0013790", "type":"card", "amount":"275", "currency":"GBP"}
    {"paymentReference":"19ae2-18mk73-009", "type":"bank", "amount":"150", "currency":"EUR"}
    ```

6. 结论

    在本文中，我们学习了将 Kafka 消费者订阅到多个主题的三种不同方法。这在为多个主题实现相同功能时非常有用。

    前两种方法基于 Kafka Consumer API 和 Spring Kafka，可以集成到现有应用程序中。最后一种方法使用 Kafka CLI，可用于快速验证多个主题。
