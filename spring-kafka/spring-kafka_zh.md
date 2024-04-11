# [使用 Spring 的 Apache Kafka 简介](https://www.baeldung.com/spring-kafka)

[Spring](https://www.baeldung.com/category/spring)

[Kafka](https://www.baeldung.com/tag/kafka) [Messaging](https://www.baeldung.com/tag/messaging)

1. 概述

    Apache Kafka 是一个分布式容错流处理系统。

    在本教程中，我们将介绍 Spring 对 Kafka 的支持，以及它为原生 Kafka Java 客户端 API 提供的抽象级别。

    Spring Kafka 通过 @KafkaListener 注解为 KafkaTemplate 和消息驱动的 POJOs 带来了简单而典型的 Spring 模板编程模型。

    进一步阅读：

    [使用 Flink 和 Kafka 构建数据管道](https://www.baeldung.com/kafka-flink-data-pipeline)

    了解如何使用 Flink 和 Kafka 处理流数据

    [使用 MQTT 和 MongoDB 的 Kafka 连接示例](https://www.baeldung.com/kafka-connect-mqtt-mongodb)

    看看使用 Kafka 连接器的实用示例。

2. 安装和设置

    要下载和安装 Kafka，请参阅此处的官方[指南](https://kafka.apache.org/quickstart)。

    我们还需要在 pom.xml 中添加 spring-kafka 依赖关系：

    `<groupId>org.springframework.kafka</groupId><artifactId>spring-kafka</artifactId>`

    然后按如下方式配置 spring-boot-maven-plugin

    ```xml
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <mainClass>com.baeldung.spring.kafka.KafkaApplication</mainClass>
        </configuration>
    </plugin>
    ```

    我们的示例应用程序将是 Spring Boot 应用程序。

    本文假定使用默认配置启动服务器，并且不更改服务器端口。

3. 配置主题

    之前，我们运行命令行工具在 Kafka 中创建主题：

    ```bash
    $ bin/kafka-topics.sh --create \
    --zookeeper localhost:2181 \
    --replication-factor 1 --partitions 1 \
    --topic mytopic
    ```

    但随着 Kafka 引入 AdminClient，我们现在可以通过编程创建主题了。

    我们需要添加 KafkaAdmin Spring Bean，它会自动为所有 NewTopic 类型的 Bean 添加主题：

    ```java
    @Configuration
    public class KafkaTopicConfig {
        @Value(value = "${spring.kafka.bootstrap-servers}")
        private String bootstrapAddress;
        @Bean
        public KafkaAdmin kafkaAdmin() {
            Map<String, Object> configs = new HashMap<>();
            configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
            return new KafkaAdmin(configs);
        }
        @Bean
        public NewTopic topic1() {
            return new NewTopic("baeldung", 1, (short) 1);
        }
    }
    ```

4. 生成消息

    要创建消息，我们首先需要配置 [ProducerFactory](http://docs.spring.io/spring-kafka/api/org/springframework/kafka/core/ProducerFactory.html)。这将设定创建 Kafka [生产者](https://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/Producer.html)实例的策略。

    然后，我们需要一个 [KafkaTemplate](http://docs.spring.io/spring-kafka/api/org/springframework/kafka/core/KafkaTemplate.html)，它封装了一个生产者实例，并提供向 Kafka 主题发送消息的便捷方法。

    生产者实例是线程安全的。因此，在整个应用上下文中使用单个实例会带来更高的性能。因此，KakfaTemplate 实例也是线程安全的，建议使用一个实例。

    1. 生产者配置

        ```java
        @Configuration
        public class KafkaProducerConfig {

            @Bean
            public ProducerFactory<String, String> producerFactory() {
                Map<String, Object> configProps = new HashMap<>();
                configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                bootstrapAddress);
                configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
                StringSerializer.class);
                configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                StringSerializer.class);
                return new DefaultKafkaProducerFactory<>(configProps);
            }

            @Bean
            public KafkaTemplate<String, String> kafkaTemplate() {
                return new KafkaTemplate<>(producerFactory());
            }
        }
        ```

    2. 发布消息

        我们可以使用 KafkaTemplate 类发送消息：

        ```java
        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        public void sendMessage(String msg) {
            kafkaTemplate.send(topicName, msg);
        }
        ```

        send API 返回一个 CompletableFuture 对象。如果我们想阻塞发送线程并获取发送消息的结果，可以调用 CompletableFuture 对象的 get API。线程会等待结果，但这会减慢生产者的速度。

        Kafka 是一个快速流处理平台。因此，最好以异步方式处理结果，这样后续消息就不会等待前一条消息的结果。

        我们可以通过回调来实现这一点：

        ```java
        public void sendMessage(String message) {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message=[" + message + 
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
                } else {
                    System.out.println("Unable to send message=[" + 
                        message + "] due to : " + ex.getMessage());
                }
            });
        }
        ```

5. 消耗信息

    1. 消费者配置

        为了消费消息，我们需要配置 [ConsumerFactory](http://docs.spring.io/autorepo/docs/spring-kafka-dist/1.1.3.RELEASE/api/org/springframework/kafka/core/ConsumerFactory.html) 和 [KafkaListenerContainerFactory](http://docs.spring.io/autorepo/docs/spring-kafka-dist/1.1.3.RELEASE/api/org/springframework/kafka/config/KafkaListenerContainerFactory.html)。一旦 Spring Bean Factory 中的这些 Bean 可用，就可以使用 [@KafkaListener](http://docs.spring.io/autorepo/docs/spring-kafka-dist/1.1.3.RELEASE/api/org/springframework/kafka/annotation/KafkaListener.html) 注解配置基于 POJO 的消费者。

        配置类上需要使用 [@EnableKafka](http://docs.spring.io/autorepo/docs/spring-kafka-dist/1.1.3.RELEASE/api/org/springframework/kafka/annotation/EnableKafka.html) 注解，以便在 Spring 管理的 Bean 上检测 @KafkaListener 注解：

        ```java
        @EnableKafka
        @Configuration
        public class KafkaConsumerConfig {

            @Bean
            public ConsumerFactory<String, String> consumerFactory() {
                Map<String, Object> props = new HashMap<>();
                props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                bootstrapAddress);
                props.put(
                ConsumerConfig.GROUP_ID_CONFIG, 
                groupId);
                props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                StringDeserializer.class);
                props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                StringDeserializer.class);
                return new DefaultKafkaConsumerFactory<>(props);
            }

            @Bean
            public ConcurrentKafkaListenerContainerFactory<String, String> 
            kafkaListenerContainerFactory() {
        
                ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
                factory.setConsumerFactory(consumerFactory());
                return factory;
            }
        }
        ```

    2. 消费消息

        ```java
        @KafkaListener(topics = "topicName", groupId = "foo")
        public void listenGroupFoo(String message) {
            System.out.println("Received Message in group foo: " + message);
        }
        ```

        我们可以为一个主题实现多个监听器，每个监听器都有不同的组 Id。此外，一个消费者可以监听来自不同主题的消息：

        `@KafkaListener(topics = "topic1, topic2", groupId = "foo")`

        Spring 还支持在监听器中使用 [@Header](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/messaging/handler/annotation/Header.html) 注解检索一个或多个消息头：

        ```java
        @KafkaListener(topics = "topicName")
        public void listenWithHeaders(
        @Payload String message, 
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
            System.out.println(
                "Received Message: " + message"
                + "from partition: " + partition);
        }
        ```

    3. 从特定分区消费消息

        请注意，我们创建的主题 baeldung 只有一个分区。

        但是，对于有多个分区的主题，@KafkaListener 可以通过初始偏移显式地订阅主题的特定分区：

        ```java
        @KafkaListener(
        topicPartitions = @TopicPartition(topic = "topicName",
        partitionOffsets = {
            @PartitionOffset(partition = "0", initialOffset = "0"), 
            @PartitionOffset(partition = "3", initialOffset = "0")}),
        containerFactory = "partitionsKafkaListenerContainerFactory")
        public void listenToPartition(
        @Payload String message, 
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
            System.out.println(
                "Received Message: " + message"
                + "from partition: " + partition);
        }
        ```

        由于此监听器中的 initialOffset 已设置为 0，因此每次初始化此监听器时，都会重新消耗之前从分区 0 和分区 3 收到的所有消息。

        如果不需要设置偏移量，我们可以使用 @TopicPartition 注解的 partitions 属性，只设置不带偏移量的分区：

        `@KafkaListener(topicPartitions = @TopicPartition(topic = "topicName", partitions = { "0", "1" }))`

    4. 为监听器添加消息过滤器

        我们可以通过添加自定义过滤器来配置监听器，使其接收特定的消息内容。这可以通过向 KafkaListenerContainerFactory 设置 [RecordFilterStrategy](http://docs.spring.io/spring-kafka/api/org/springframework/kafka/listener/adapter/RecordFilterStrategy.html) 来实现：

        ```java
        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String>
        filterKafkaListenerContainerFactory() {

            ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());
            factory.setRecordFilterStrategy(
            record -> record.value().contains("World"));
            return factory;
        }
        ```

        然后，我们可以配置一个监听器来使用该容器工厂：

        ```java
        @KafkaListener(
        topics = "topicName", 
        containerFactory = "filterKafkaListenerContainerFactory")
        public void listenWithFilter(String message) {
            System.out.println("Received Message in filtered listener: " + message);
        }
        ```

        在此监听器中，所有与过滤器匹配的消息都将被丢弃。

6. 自定义消息转换器

    到目前为止，我们只介绍了以消息形式发送和接收字符串。不过，我们也可以发送和接收自定义 Java 对象。这需要在 ProducerFactory 中配置适当的序列化器，并在 ConsumerFactory 中配置反序列化器。

    让我们来看一个简单的 bean 类，我们将把它作为消息发送：

    src/.spring.kafka/Greeting.java

    1. 生成自定义消息

        在本例中，我们将使用 [JsonSerializer](http://docs.spring.io/spring-kafka/api/org/springframework/kafka/support/serializer/JsonSerializer.html)。

        让我们看看 ProducerFactory 和 KafkaTemplate 的代码：

        src/.spring.kafka/KafkaProducerConfig.java: greetingProducerFactory(), greetingKafkaTemplate()

        我们可以使用这个新的 KafkaTemplate 发送问候信息：

        `kafkaTemplate.send(topicName, new Greeting("Hello", "World"));`

    2. 消费自定义消息

        同样，让我们修改 ConsumerFactory 和 KafkaListenerContainerFactory，以正确反序列化问候信息：

        src/.spring.kafka/KafkaConsumerConfig.java: greetingConsumerFactory(), greetingKafkaListenerContainerFactory()

        spring-kafka JSON 序列化器和反序列化器使用的是 [Jackson](https://www.baeldung.com/jackson) 库，它也是 spring-kafka 项目的可选 Maven 依赖项。

        因此，让我们将其添加到 pom.xml 中：

        ```xml
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.3</version>
        </dependency>
        ```

        建议使用添加到 spring-kafka 的 pom.xml 中的版本，而不是使用最新版本的 Jackson。

        最后，我们需要编写一个监听器来接收问候信息：

        src/.spring.kafka/KafkaApplication.java: MessageListener{}

        ```java
        @KafkaListener(
        topics = "topicName", 
        containerFactory = "greetingKafkaListenerContainerFactory")
        public void greetingListener(Greeting greeting) {
            // process greeting message
        }
        ```

7. 多方法监听器

    现在让我们来看看如何配置应用程序，将各种对象发送到同一个主题，然后消费它们。

    首先，我们将添加一个新类 Farewell：

    src/.spring.kafka/Farewell.java

    我们需要一些额外的配置，以便能够向同一个主题发送问候和告别对象。

    1. 在生产者中设置映射类型

        在生产者中，我们必须配置 [JSON](https://www.baeldung.com/java-json) 类型映射：

        `configProps.put(JsonSerializer.TYPE_MAPPINGS, "greeting:com.baeldung.spring.kafka.Greeting, farewell:com.baeldung.spring.kafka.Farewell");`

        这样，类库就会在类型头中填写相应的类名。

        因此，ProducerFactory 和 KafkaTemplate 看起来就像这样：

        src/.spring.kafka/KafkaProducerConfig.java: multiTypeProducerFactory(), multiTypeKafkaTemplate()

        我们可以使用此 KafkaTemplate 向主题发送问候语、告别语或任何[对象](https://www.baeldung.com/java-classes-objects)：

        src/.spring.kafka/KafkaApplicationMultiListener.java: MessageProducer{sendMessages()}

    2. 在消费者中使用自定义消息转换器

        为了能反序列化传入的消息，我们需要为消费者提供一个自定义的消息转换器（MessageConverter）。

        在后台，MessageConverter 依赖于 Jackson2JavaTypeMapper。默认情况下，映射器会推断接收到的对象的类型：相反，我们需要明确告诉它使用类型标头来确定反序列化的目标类：

        `typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);`

        我们还需要提供反向映射信息。在类型标头中查找 "greeting"（问候）可识别问候对象，而 "farewell"（告别）则对应Farewell对象：

        ```java
        Map<String, Class<?>> mappings = new HashMap<>(); 
        mappings.put("greeting", Greeting.class);
        mappings.put("farewell", Farewell.class);
        typeMapper.setIdClassMapping(mappings);
        ```

        最后，我们需要配置映射器信任的包。我们必须确保它包含目标类的位置：

        `typeMapper.addTrustedPackages("com.baeldung.spring.kafka");`

        因此，下面是该 MessageConverter 的最终定义：

        src/.spring.kafka/KafkaConsumerConfig.java: multiTypeConverter()

        现在，我们需要告诉 ConcurrentKafkaListenerContainerFactory 使用 MessageConverter 和一个相当基本的 ConsumerFactory：

        src/.spring.kafka/KafkaConsumerConfig.java: multiTypeConsumerFactory(),multiTypeKafkaListenerContainerFactory()

    3. 在监听器中使用 @KafkaHandler

        最后，我们将在 KafkaListener 中创建一个处理程序方法，以检索每个可能的对象。每个处理程序都需要注释 @KafkaHandler。

        最后，我们还可以为那些无法绑定到"Greeting"或"Farewell"类的对象定义一个默认处理程序：

        src/.spring.kafka/MultiTypeKafkaListener.java

8. 结论

    在本文中，我们介绍了 Spring 支持 Apache Kafka 的基础知识。我们简要介绍了用于发送和接收消息的类。

    本文的完整源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-kafka) 上找到。

    在运行代码之前，请确保 Kafka 服务器正在运行，并且主题是手动创建的。
