# [管理 Kafka 消费者组](https://www.baeldung.com/kafka-manage-consumer-groups)

1. 简介

    消费者组允许多个消费者从同一主题读取数据，有助于创建更具可扩展性的 Kafka 应用程序。

    在本教程中，我们将了解消费者组及其如何在消费者之间重新平衡分区。

2. 什么是消费者组？

    消费者组是一组与一个或多个主题相关联的唯一消费者。每个消费者可以从零个、一个或多个分区读取数据。此外，每个分区在给定时间内只能分配给一个消费者。分区分配会随着群成员的变化而变化。这就是所谓的组重新平衡。

    消费者组是 Kafka 应用程序的重要组成部分。它允许将类似的消费者分组，使他们可以并行地从分区主题中读取数据。因此，它提高了 Kafka 应用程序的性能和可扩展性。

    1. 组协调器和组长

        当我们实例化一个消费者组时，Kafka 也会创建组协调器。组协调器会定期接收来自消费者的请求（称为心跳）。如果某个消费者停止发送心跳，协调者就会认为该消费者已经离开群组或崩溃。这是分区重新平衡的一个可能触发因素。

        第一个请求组协调者加入组的消费者将成为组长。当因任何原因发生重新平衡时，组长都会从组协调者那里收到一份组员列表。然后，组长会使用 [partition.assignment.strategy](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html#partition-assignment-strategy) 配置中的自定义策略在列表中的消费者之间重新分配分区。

    2. 承诺偏移量

        Kafka 使用已提交偏移量来跟踪从主题读取的最后位置。已提交偏移量是消费者确认已成功处理的主题中的位置。换句话说，它是自己和其他消费者在后续轮次中读取事件的起点。

        Kafka 将所有分区的已提交偏移量存储在一个名为 __consumer_offsets 的内部主题中。我们可以放心地信任它的信息，因为对于复制的经纪人来说，主题是持久和容错的。

    3. 分区再平衡

        分区再平衡（partition rebalance）将分区所有权从一个消费者转移到另一个消费者。当有新的消费者加入群组或群组中的消费者成员崩溃或取消订阅时，Kafka 会自动执行重新平衡。

        为了提高可扩展性，当有新的消费者加入组时，Kafka 会公平地与新加入的消费者共享其他消费者的分区。此外，当一个消费者崩溃时，其分区必须分配给组中的其余消费者，以避免丢失任何未处理的消息。

        分区重新平衡使用 __consumer_offsets 主题，使消费者从正确的位置开始读取重新分配的分区。

        在重新平衡期间，消费者不能消费报文。换句话说，在重新平衡完成之前，代理变得不可用。此外，消费者会丢失他们的状态，需要重新计算他们的缓存值。分区再平衡期间的不可用性和缓存重新计算会降低事件消耗的速度。

3. 设置应用程序

    在本节中，我们将为 Spring Kafka 应用程序的启动和运行进行基础配置。

    1. 创建基本配置

        首先，让我们配置主题及其分区：

        main/.spring.kafka.managingkafkaconsumergroups/KafkaTopicConfiguration.java

        上述配置简单明了。我们只需配置一个名为 topic-1 的新主题和两个分区。

        现在，让我们来配置生产者：

        main/.spring.kafka.managingkafkaconsumergroups/KafkaProducerConfiguration.java

        在上面的 Kafka 生产者配置中，我们设置了代理地址和用于写入消息的[序列化器](https://www.baeldung.com/kafka-custom-serializer)。

        最后，让我们来配置消费者：

        main/.spring.kafka.managingkafkaconsumergroups/KafkaConsumerConfiguration.java

    2. 设置消费者

        在我们的演示应用程序中，我们将从两个消费者开始，它们属于同一个组，名为 group-1，来自 topic-1：

        main/.spring.kafka.managingkafkaconsumergroups/MessageConsumerService.java

        MessageConsumerService 类使用 @KafkaListener 注解注册了两个消费者，以监听 group-1 中的 topic-1。

        现在，让我们在 MessageConsumerService 类中定义一个字段和一个方法来跟踪已消费的分区：

        ```java
        Map<String, Set<Integer>> consumedPartitions = new ConcurrentHashMap<>();

        private void trackConsumedPartitions(String key, ConsumerRecord<?, ?> record) {
            consumedPartitions.computeIfAbsent(key, k -> new HashSet<>());
            consumedPartitions.computeIfPresent(key, (k, v) -> {
                v.add(record.partition());
                return v;
            });
        }
        ```

        在上面的代码中，我们使用 [ConcurrentHashMap](https://www.baeldung.com/concurrenthashmap-reading-and-writing) 将每个消费者名称映射到该消费者消耗的所有分区的 HashSet。

4. 可视化消费者离开时的分区再平衡

    现在，我们已经设置了所有配置并注册了消费者，我们可以直观地看到当其中一个消费者离开 group-1 时 Kafka 会做什么。为此，让我们为使用嵌入式代理的 [Kafka 集成测试](https://www.baeldung.com/spring-boot-kafka-testing)定义骨架：

    ```java
    @SpringBootTest(classes = ManagingConsumerGroupsApplicationKafkaApp.class)
    @EmbeddedKafka(partitions = 2, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
    public class ManagingConsumerGroupsIntegrationTest {

        private static final String CONSUMER_1_IDENTIFIER = "org.springframework.kafka.KafkaListenerEndpointContainer#1";
        private static final int TOTAL_PRODUCED_MESSAGES = 50000;
        private static final int MESSAGE_WHERE_CONSUMER_1_LEAVES_GROUP = 10000;

        @Autowired
        KafkaTemplate<String, Double> kafkaTemplate;

        @Autowired
        KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

        @Autowired
        MessageConsumerService consumerService;
    }
    ```

    在上面的代码中，我们注入了生成和消费消息所需的 Bean：kafkaTemplate 和 consumerService。我们还注入了 bean kafkaListenerEndpointRegistry 来操作已注册的消费者。

    最后，我们定义了将在测试用例中使用的三个常量。

    现在，让我们定义测试用例方法：

    main/.spring.kafka.managingkafkaconsumergroups/ManagingConsumerGroupsIntegrationTest.java

    在上面的测试中，我们创建了一个消息流，在某一点上，我们删除了其中一个消费者，这样 Kafka 就会将其分区重新分配给剩余的消费者。让我们分解一下逻辑，使其更加透明：

    - 主循环使用 kafkaTemplate，使用 Apache Commons 的 [RandomUtils](https://commons.apache.org/proper/commons-lang/javadocs/api-3.9/org/apache/commons/lang3/RandomUtils.html) 生成 50,000 个随机数事件。当产生任意数量的消息（在我们的例子中为 10,000 条）时，我们就停止并从代理中取消注册一个消费者。
    - 要取消注册一个消费者，我们首先使用流在容器中搜索匹配的消费者，并使用 getListenerContainer() 方法检索它。然后，我们调用 stop() 停止容器 Spring 组件的执行。最后，我们调用 unregisterListenerContainer() 从 Kafka Broker 中以编程方式取消注册与容器变量相关联的监听器。

    在讨论测试断言之前，我们先来看看 Kafka 在测试执行过程中生成的几行日志。

    第一行非常重要，它显示了 consumer-1 向组协调器发出的离开组（LeaveGroup）请求：

    ```log
    INFO o.a.k.c.c.i.ConsumerCoordinator - [Consumer clientId=consumer-group-1-1, groupId=group-1] Member consumer-group-1-1-4eb63bbc-336d-44d6-9d41-0a862029ba95 sending LeaveGroup request to coordinator localhost:9092
    ```

    然后，组协调器会自动触发重新平衡，并显示背后的原因：

    ```log
    INFO  k.coordinator.group.GroupCoordinator - [GroupCoordinator 0]: Preparing to rebalance group group-1 in state PreparingRebalance with old generation 2 (__consumer_offsets-4) (reason: Removing member consumer-group-1-1-4eb63bbc-336d-44d6-9d41-0a862029ba95 on LeaveGroup)
    ```

    回到我们的测试，我们将断言分区重新平衡正确进行。由于我们取消了以 1 结尾的消费者的注册，它的分区应该重新分配给剩余的消费者，也就是consumer-0。因此，我们使用已跟踪消耗记录的映射来检查consumer-1是否只从一个分区消耗，而consumer-0是否从两个分区消耗。

5. 有用的消费者配置

    现在，让我们来谈谈影响分区再平衡的一些消费者配置，以及为它们设置特定值的权衡。

    1. 会话超时和心跳频率

        会话超时（session.timeout.ms）参数表示组协调器在触发分区再平衡之前等待消费者发送心跳的最长时间（以毫秒为单位）。除了 [session.timeout.ms](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html#heartbeat-interval-ms) 参数，[heartbeat.interval.ms](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html#heartbeat-interval-ms) 参数还表示用户向组协调器发送心跳的频率（以毫秒为单位）。

        我们应该同时修改用户超时和心跳频率，使心跳间隔毫秒数始终低于会话超时毫秒数。这是因为我们不想让消费者在发送心跳之前就因为超时而死亡。通常，我们会将心跳间隔设为会话超时的 33%，以确保在消费者死亡前发送一次以上的心跳。

        默认消费者会话超时设置为 45 秒。我们可以修改该值，只要我们了解修改该值的利弊。

        当我们把会话超时设置得比默认值低时，就能提高消费者组从故障中恢复的速度，从而提高组的可用性。不过，在 0.10.1.0 之前的 Kafka 版本中，如果消费者的主线程在消费超过会话超时的消息时被阻塞，消费者就无法发送心跳。因此，消费者被视为死亡，组协调器会触发不必要的分区再平衡。[KIP-62](https://cwiki.apache.org/confluence/display/KAFKA/KIP-62%3A+Allow+consumer+to+send+heartbeats+from+a+background+thread) 解决了这个问题，引入了一个只发送心跳的后台线程。

        如果我们为会话超时设置更高的值，我们就无法更快地检测到故障。不过，对于 o.10.1.0 以上的 Kafka 版本，这可能会解决上述不必要的分区再平衡问题。

    2. 最大轮询间隔时间

        另一项配置是最大轮询间隔时间（max.poll.interval.ms），表示代理等待空闲消费者的最长时间。过了这段时间，消费者就会停止发送心跳，直到达到配置的会话超时并离开群组。max.poll.interval.ms 的[默认等待时间](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html#max-poll-interval-ms)为五分钟。

        如果我们为 max.poll.interval.ms 设置更高的值，就会给消费者更多的空闲时间，这可能有助于避免再平衡。不过，如果没有信息要消耗，增加该时间也可能会增加空闲消费者的数量。这在低吞吐量环境中可能会造成问题，因为消费者闲置的时间会更长，从而增加基础设施成本。

6. 结论

    在本文中，我们了解了群组领导者和群组协调者的基本角色。我们还了解了 Kafka 如何管理消费者组和分区。

    在实践中，我们看到了当一个消费者离开组时，Kafka 如何自动重新平衡组内的分区。

    了解 Kafka 何时触发分区再平衡并相应调整消费者配置至关重要。
