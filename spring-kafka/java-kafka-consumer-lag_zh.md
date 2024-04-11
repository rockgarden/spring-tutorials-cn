# [监控 Apache Kafka 中的消费者滞后情况](https://www.baeldung.com/java-kafka-consumer-lag)

1. 概述

    Kafka 消费者组滞后是任何基于 Kafka 的事件驱动系统的关键性能指标。

    在本教程中，我们将构建一个分析器应用程序来监控 Kafka 消费者滞后。

2. 消费者滞后

    消费者滞后是指消费者最后提交的偏移量与生产者在日志中的结束偏移量之间的差值。换句话说，消费者滞后衡量的是任何生产者-消费者系统中生产和消费消息之间的延迟。

    在本节中，我们将了解如何确定偏移值。

    1. Kafka 管理客户端

        要检查消费者组的偏移值，我们需要 Kafka 管理客户端。因此，让我们在 LagAnalyzerService 类中编写一个方法，创建一个 AdminClient 类的实例：

        main/..monitoring.service/LagAnalyzerService.java:getAdminClient()

        我们必须注意使用 [@Value](https://www.baeldung.com/spring-value-annotation) 注解从属性文件中获取引导服务器列表。同样，我们将使用此注解获取其他值，如 groupId 和 topicName。

    2. 消费者组偏移

        首先，我们可以使用 AdminClient 类的 listConsumerGroupOffsets() 方法获取特定消费者组 id 的偏移信息。

        接下来，我们的重点主要是偏移值，因此可以调用 partitionsToOffsetAndMetadata() 方法来获取 TopicPartition 与 OffsetAndMetadata 值的映射：

        main/..monitoring.service/LagAnalyzerService.java:getConsumerGrpOffsets()

        最后，我们可以注意到对 topicPartitionOffsetAndMetadataMap 的迭代，以便将获取的结果限制为每个主题和分区的偏移值。

    3. 生产者偏移

        查找消费者组滞后的唯一方法就是获取末端偏移值。为此，我们可以使用 KafkaConsumer 类的 endOffsets() 方法。

        让我们先在 LagAnalyzerService 类中创建一个 [KafkaConsumer](https://kafka.apache.org/25/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html) 类的实例：

        main/..monitoring.service/LagAnalyzerService.java:getKafkaConsumer()

        接下来，让我们从需要计算滞后的消费者组偏移量中汇总所有相关的 TopicPartition 值，以便将其作为参数提供给 endOffsets() 方法：

        main/..monitoring.service/LagAnalyzerService.java:getProducerOffsets()

        最后，让我们编写一个方法，使用消费者偏移量和生产者 endoffsets 为每个 TopicPartition 生成滞后：

        main/..monitoring.service/LagAnalyzerService.java:computeLags()

3. 滞后分析器

    现在，让我们在 LagAnalyzerService 类中编写 analyzeLag() 方法来协调滞后分析：

    main/..monitoring.service/LagAnalyzerService.java:analyzeLag()

    不过，在监控滞后指标时，我们需要一个几乎实时的滞后值，以便采取任何管理措施恢复系统性能。

    实现这一点的一个直接方法是定期轮询滞后值。因此，让我们创建一个 LiveLagAnalyzerService 服务，它将调用 LagAnalyzerService 的 analyzeLag() 方法：

    main/..monitoring.service/LagAnalyzerService.java:liveLagAnalysis()

    为此，我们使用 [@Scheduled](https://www.baeldung.com/spring-scheduled-tasks) 注解将轮询频率设置为 5 秒。不过，如果要进行实时监控，我们可能需要通过 [JMX](https://www.baeldung.com/java-management-extensions) 来访问此功能。

4. 模拟

    在本节中，我们将模拟本地 Kafka 设置的 Kafka 生产者和消费者，这样我们就可以看到 LagAnalyzer 的运行情况，而无需依赖外部 Kafka 生产者和消费者。

    1. 模拟模式

        由于模拟模式仅用于演示目的，因此当我们要在真实场景中运行 Lag Analyzer 应用程序时，应该有一种机制来关闭模拟模式。

        我们可以在 application.properties 资源文件中将其作为可配置属性保留：

        ```properties
        monitor.producer.simulate=true
        monitor.consumer.simulate=true
        ```

        我们将把这些属性插入 Kafka 生产者和消费者，并控制它们的行为。

        此外，让我们定义生产者的 startTime、endTime 和一个辅助方法 time() 来获取监控期间的当前时间：

        main/..monitoring.util/MonitoringUtil.java

    2. 生产者-消费者配置

        我们需要定义一些核心配置值，用于实例化 Kafka 消费者和生产者模拟器。

        首先，让我们在 main/.monitoring.simulation/KafkaConsumerConfig.java 类中定义消费者模拟器的配置：

        consumerFactory()

        kafkaListenerContainerFactory()

        接下来，我们可以在 KafkaProducerConfig 类中定义生产者模拟器的配置：

        ```java
        @Bean
        public ProducerFactory<String, String> producerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }
        ```

        此外，让我们使用 [@KafkaListener](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/annotation/KafkaListener.html) 注解来指定目标监听器，当然，只有当 monitor.consumer.simulate 设置为 true 时，目标监听器才会启用：

        main/.monitoring.simulation/ConsumerSimulator.java

        因此，我们添加了 10 毫秒的睡眠时间，以人为制造消费者滞后。

        最后，让我们编写一个 sendMessage() 方法来模拟生产者：

        main/.monitoring.simulation/ProducerSimulator.java:sendMessage()

        我们可以注意到，生产者将以每秒 1 条消息的速度生成消息。此外，在模拟开始时间（startTime）之后的 30 秒结束时间（endTime）之后，它将停止生成消息。

    3. 实时监控

        现在，让我们运行 main/..monitoring/LagAnalyzerApplication.java 的主方法main()：

        我们将在每 30 秒后查看主题每个分区的当前滞后情况：

        ```log
        Time=2021/06/06 11:07:24 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 93
        Time=2021/06/06 11:07:29 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 290
        Time=2021/06/06 11:07:34 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 776
        Time=2021/06/06 11:07:39 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 1159
        Time=2021/06/06 11:07:44 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 1559
        Time=2021/06/06 11:07:49 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 2015
        Time=2021/06/06 11:07:54 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 1231
        Time=2021/06/06 11:07:59 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 731
        Time=2021/06/06 11:08:04 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 231
        Time=2021/06/06 11:08:09 | Lag for topic = baeldungTopic, partition = 0, groupId =baeldungGrp is 0
        ```

        因此，生产者产生信息的速率为 1 条/毫秒，高于消费者消费信息的速率。因此，滞后将在最初的 30 秒内开始形成，之后生产者停止生产，滞后将逐渐降至 0。

5. 通过执行器端点监控消费者滞后

    对于带有 Kafka 消费者的 Spring Boot 应用程序，我们可以使用 Micrometer 获取消费者滞后指标，并将其暴露给执行器端点。让我们看看如何做到这一点。

    1. 启用执行器端点

        首先，我们需要在项目的 pom.xml 文件中添加 spring-boot-starter-actuator 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
            <version>3.2.2.</version>
        </dependency>
        ```

        现在，让我们通过配置 application.properties 文件来启用 /actuator 端点：

        ```properties
        management.endpoints.web.base-path=/actuator
        management.endpoints.web.exposure.include=*
        management.endpoint.health.show-details=always
        ```

        最后，我们还要为应用程序设置不同于 8080 的端口：

        `server.port=8081`

        我们必须注意，Zookeeper 进程在 8080 端口上运行网络控制台。因此，如果在本地计算机上运行 Zookeeper，我们必须为 Spring Boot 应用程序使用不同的端口。

    2. 使用 Micrometer 配置指标

        我们可以使用 [Micrometer](https://www.baeldung.com/micrometer) 库获取 Kafka 消费者指标。在本节中，我们将为 Prometheus 监控系统公开消费者指标。

        首先，我们必须在项目的 pom.xml 文件中添加 micrometer-registry-prometheus 依赖项：

        ```xml
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
            <version>1.12.2</version>
        </dependency>
        ```

        接下来，让我们为应用程序启用 JMX 和 /actuator/prometheus 端点：

        ```properties
        management.endpoint.prometheus.enabled=true
        spring.jmx.enabled=false
        ```

        接下来，让我们添加一个 MeterRegistry 类实例，作为 KafkaConsumerConfig 类的成员：

        ```java
        @Autowired
        private MeterRegistry meterRegistry;
        ```

        最后，我们准备在 consumerFactory bean 中添加 MicrometerConsumerListener：

        ```java
        @Bean
        public ConsumerFactory<String, String> consumerFactory() {
            Map<String, Object> props = getConsumerConfig(this.groupId);
            DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
            consumerFactory.addListener(new MicrometerConsumerListener<>(this.meterRegistry));
            return consumerFactory;
        }
        ```

        就是这样！我们可以运行应用程序并监控 Kafka 消费者指标了。

    3. 监控消费者滞后指标

        我们可以启动应用程序并访问 /actuator/prometheus 端点，查看 kafka_consumer_* 指标。在其他指标中，kafka_consumer_fetch_manager_records_lag 指标显示了当前的滞后信息：

        ```log
        kafka_consumer_fetch_manager_records_lag{client_id="consumer-baeldungGrp-2",kafka_version="3.3.1",partition="0",spring_id="consumerFactory.consumer-baeldungGrp-2",topic="baeldung",} 21447.0
        ```

        此外，让我们编写一个脚本，定期获取滞后情况，并以几乎实时的方式显示当前的滞后情况：

        ```bash
        $ while true
        do
        curl --silent -XGET http://localhost:8081/actuator/prometheus | \
        awk '/kafka_consumer_fetch_manager_records_lag{/{print "Current lag is:",$2}'; 
        sleep 5;
        done
        Current lag is: 11375.0
        Current lag is: 10875.0
        Current lag is: 10375.0
        Current lag is: 9875.0
        ```

        太好了！我们已经成功集成了 Kafka 消费者指标，并通过执行器端点将其公开。

6. 总结

    在本教程中，我们了解了如何查找 Kafka 主题上的消费者滞后。此外，我们还利用这些知识在 spring 中创建了一个 LagAnalyzer 应用程序，它几乎可以实时显示滞后情况。
