# [确保 Kafka 中的消息排序：策略与配置](https://www.baeldung.com/kafka-message-ordering)

1. 概述

    在本文中，我们将探讨 Apache Kafka 中围绕消息排序的挑战和解决方案。以正确的顺序处理消息对于维护分布式系统中的数据完整性和一致性至关重要。虽然 Kafka 提供了维护消息顺序的机制，但在分布式环境中实现这一点有其自身的复杂性。

2. 分区内的排序及其挑战

    Kafka 通过为每条消息分配唯一的偏移量来维护单个分区内的顺序。这保证了分区内消息的顺序追加。然而，当我们扩展并使用多个分区时，维护全局顺序就变得复杂了。不同分区接收报文的速度各不相同，这就使它们之间的严格排序变得复杂。

    1. 生产者和消费者定时

        让我们来谈谈 Kafka 是如何处理消息顺序的。生产者发送消息的顺序和消费者接收消息的顺序是有区别的。如果只使用一个[分区](https://www.baeldung.com/kafka-topics-partitions)，我们就会按照消息到达代理的顺序来处理它们。但是，这个顺序可能与我们最初发送信息的顺序不一致。出现这种混淆的原因可能是网络延迟或我们重新发送消息。为了保持一致，我们可以通过确认和重试来实现生产者。这样，我们就能确保消息不仅能按正确的顺序到达 Kafka。

    2. 多分区的挑战

        这种跨分区的分布虽然有利于可扩展性和容错性，但也带来了实现全局消息排序的复杂性。例如，我们按照 M1 和 M2 的顺序发送两条消息。Kafka 会按照我们发送的顺序获取它们，但会把它们放在不同的分区中。问题是，M1 先发送并不意味着它会在 M2 之前被处理。在金融交易等对处理顺序要求很高的场景中，这可能是个挑战。

    3. 单分区消息排序

        我们创建的主题名称为 "single_partition_topic"（有一个分区）和 "multi_partition_topic"（有 5 个分区）。下面是一个具有单分区的主题的示例，生产者正在向主题发送一条消息：

        ```java
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonSerializer.class.getName());
        producer = new KafkaProducer<>(producerProperties);
        for (long sequenceNumber = 1; sequenceNumber <= 10; sequenceNumber++) {
            UserEvent userEvent = new UserEvent(UUID.randomUUID().toString());
            userEvent.setGlobalSequenceNumber(sequenceNumber);
            userEvent.setEventNanoTime(System.nanoTime());
            ProducerRecord<Long, UserEvent> producerRecord = new ProducerRecord<>(Config.SINGLE_PARTITION_TOPIC, userEvent);
            Future<RecordMetadata> future = producer.send(producerRecord);
            sentUserEventList.add(userEvent);
            RecordMetadata metadata = future.get();
            logger.info("User Event ID: " + userEvent.getUserEventId() + ", Partition : " + metadata.partition());
        }
        ```

        UserEvent 是一个实现了 Comparable 接口的 POJO 类，有助于按 globalSequenceNumber（外部序列号）对消息类进行排序。由于生产者发送的是 POJO 消息对象，我们实现了自定义的 Jackson [序列化器](https://www.baeldung.com/jackson-custom-serialization)和[反序列化器](https://www.baeldung.com/jackson-deserialization)。

        0 分区接收所有用户事件，事件 ID 按以下顺序显示：

        ```log
        841e593a-bca0-4dd7-9f32-35792ffc522e
        9ef7b0c0-6272-4f9a-940d-37ef93c59646
        0b09faef-2939-46f9-9c0a-637192e242c5
        4158457a-73cc-4e65-957a-bf3f647d490a
        fcf531b7-c427-4e80-90fd-b0a10bc096ca
        23ed595c-2477-4475-a4f4-62f6cbb05c41
        3a36fb33-0850-424c-81b1-dafe4dc3bb18
        10bca2be-3f0e-40ef-bafc-eaf055b4ee26
        d48dcd66-799c-4645-a977-fa68414ca7c9
        7a70bfde-f659-4c34-ba75-9b43a5045d39
        ```

        在 Kafka 中，每个消费者组都是一个独立的实体。如果两个消费者属于不同的消费者组，他们都会收到主题上的所有消息。这是因为 Kafka 将每个消费者组视为独立的订阅者。

        如果两个消费者属于同一个消费者组，并订阅了一个有多个分区的主题，Kafka 会确保每个消费者从一组独特的分区中读取。这是为了允许并发处理消息。

        Kafka 确保在一个消费者组内，不会有两个消费者读取相同的消息，因此每个消费者组只处理一次消息。

        下面的代码用于消费者从同一个主题中消费消息：

        ```java
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(Config.CONSUMER_VALUE_DESERIALIZER_SERIALIZED_CLASS, UserEvent.class);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singletonList(Config.SINGLE_PARTITION_TOPIC));
        ConsumerRecords<Long, UserEvent> records = consumer.poll(TIMEOUT_WAIT_FOR_MESSAGES);
        records.forEach(record -> {
            UserEvent userEvent = record.value();
            receivedUserEventList.add(userEvent);
            logger.info("User Event ID: " + userEvent.getUserEventId());
        });
        ```

        在这种情况下，我们得到的输出显示消费者以相同的顺序消费消息，以下是输出中的顺序事件 ID：

        ```log
        841e593a-bca0-4dd7-9f32-35792ffc522e
        9ef7b0c0-6272-4f9a-940d-37ef93c59646
        0b09faef-2939-46f9-9c0a-637192e242c5
        4158457a-73cc-4e65-957a-bf3f647d490a
        fcf531b7-c427-4e80-90fd-b0a10bc096ca
        23ed595c-2477-4475-a4f4-62f6cbb05c41
        3a36fb33-0850-424c-81b1-dafe4dc3bb18
        10bca2be-3f0e-40ef-bafc-eaf055b4ee26
        d48dcd66-799c-4645-a977-fa68414ca7c9
        7a70bfde-f659-4c34-ba75-9b43a5045d39
        ```

    4. 多分区消息排序

        对于具有多个分区的主题，消费者和生产者配置是相同的。唯一不同的是消息所去的主题和分区，生产者将消息发送到主题 "multi_partition_topic"：

        ```java
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(Config.MULTI_PARTITION_TOPIC, sequenceNumber, userEvent));
        sentUserEventList.add(userEvent);
        RecordMetadata metadata = future.get();
        logger.info("User Event ID: " + userEvent.getUserEventId() + ", Partition : " + metadata.partition());
        ```

        消费者从同一主题消费信息：

        ```java
        consumer.subscribe(Collections.singletonList(Config.MULTI_PARTITION_TOPIC));
        ConsumerRecords<Long, UserEvent> records = consumer.poll(TIMEOUT_WAIT_FOR_MESSAGES);
        records.forEach(record -> {
            UserEvent userEvent = record.value();
            receivedUserEventList.add(userEvent);
            logger.info("User Event ID: " + userEvent.getUserEventId());
        });
        ```

        生产者输出会列出事件 ID 及其各自的分区，如下所示：

        ```log
        939c1760-140e-4d0c-baa6-3b1dd9833a7d, 0
        47fdbe4b-e8c9-4b30-8efd-b9e97294bb74, 4
        4566a4ec-cae9-4991-a8a2-d7c5a1b3864f, 4
        4b061609-aae8-415f-94d7-ae20d4ef1ca9, 3
        eb830eb9-e5e9-498f-8618-fb5d9fc519e4, 2
        9f2a048f-eec1-4c68-bc33-c307eec7cace, 1
        c300f25f-c85f-413c-836e-b9dcfbb444c1, 0
        c82efad1-6287-42c6-8309-ae1d60e13f5e, 4
        461380eb-4dd6-455c-9c92-ae58b0913954, 4
        43bbe38a-5c9e-452b-be43-ebb26d58e782, 3
        ```

        对于消费者来说，输出结果显示消费者并没有按照相同的顺序消费报文。输出结果中的事件 ID 如下：

        ```log
        939c1760-140e-4d0c-baa6-3b1dd9833a7d
        47fdbe4b-e8c9-4b30-8efd-b9e97294bb74
        4566a4ec-cae9-4991-a8a2-d7c5a1b3864f
        c82efad1-6287-42c6-8309-ae1d60e13f5e
        461380eb-4dd6-455c-9c92-ae58b0913954
        eb830eb9-e5e9-498f-8618-fb5d9fc519e4
        4b061609-aae8-415f-94d7-ae20d4ef1ca9
        43bbe38a-5c9e-452b-be43-ebb26d58e782
        c300f25f-c85f-413c-836e-b9dcfbb444c1
        9f2a048f-eec1-4c68-bc33-c307eec7cace
        ```

3. 信息排序策略

    1. 使用单一分区

        我们可以在 Kafka 中使用单个分区，如前面使用 "single_partition_topic" 的示例所示，它可以确保消息的排序。不过，这种方法也有其利弊：

        - 吞吐量限制(Throughput Constraint)：想象一下，我们在一家繁忙的披萨店里。如果我们只有一个厨师（生产者）和一个服务员（消费者）在一张桌子（分区）上工作，他们只能提供这么多披萨，然后事情就会开始倒退。在 Kafka 的世界里，当我们处理大量消息时，坚持使用单一分区就好比使用一张表。单个分区在大容量场景中会成为瓶颈，消息处理速度会受到限制，因为一次只能有一个生产者和一个消费者在单个分区上操作。
        - 减少并行性(Reduced Parallelism)：在上面的例子中，如果我们有多个厨师（生产者）和服务员（消费者）在多个表（分区）上工作，那么完成的订单数量就会增加。Kafka 的优势在于多个分区之间的并行处理。如果只有一个分区，这一优势就会丧失，导致顺序处理并进一步限制消息流

        从本质上讲，虽然单个分区可以保证订单，但它是以降低吞吐量为代价的。

    2. 带时间窗缓冲的外部排序

        在这种方法中，生产者为每条信息标上一个全局序列号。多个消费者实例同时消费来自不同分区的报文，并使用这些序列号对报文重新排序，以确保全局顺序。

        在有多个生产者的实际场景中，我们将通过所有生产者进程都能访问的共享资源（如数据库序列或分布式计数器）来管理全局序列。这样就能确保序列号在所有信息中都是唯一和有序的，无论哪个生产者发送信息：

        ```java
        for (long sequenceNumber = 1; sequenceNumber <= 10 ; sequenceNumber++) {
            UserEvent userEvent = new UserEvent(UUID.randomUUID().toString());
            userEvent.setEventNanoTime(System.nanoTime());
            userEvent.setGlobalSequenceNumber(sequenceNumber);
            Future<RecordMetadata> future = producer.send(new ProducerRecord<>(Config.MULTI_PARTITION_TOPIC, sequenceNumber, userEvent));
            sentUserEventList.add(userEvent);
            RecordMetadata metadata = future.get();
            logger.info("User Event ID: " + userEvent.getUserEventId() + ", Partition : " + metadata.partition());
        }
        ```

        在消费者端，我们将消息按时间窗口分组，然后按顺序处理。在特定时间段内到达的消息，我们会将其批处理在一起，窗口一过，我们就会处理这批消息。这样可以确保该时间段内的报文按顺序处理，即使它们在窗口内的不同时间到达也是如此。在处理之前，消费者会根据序列号对报文进行缓冲和重新排序。我们需要确保以正确的顺序处理消息，为此，消费者应该有一个缓冲期，在处理缓冲消息之前多次轮询消息，而且这个缓冲期要足够长，以应对潜在的消息排序问题：

        ```java
        consumer.subscribe(Collections.singletonList(Config.MULTI_PARTITION_TOPIC));
        List<UserEvent> buffer = new ArrayList<>();
        long lastProcessedTime = System.nanoTime();
        ConsumerRecords<Long, UserEvent> records = consumer.poll(TIMEOUT_WAIT_FOR_MESSAGES);
        records.forEach(record -> {
            buffer.add(record.value());
        });
        while (!buffer.isEmpty()) {
            if (System.nanoTime() - lastProcessedTime > BUFFER_PERIOD_NS) {
                processBuffer(buffer, receivedUserEventList);
                lastProcessedTime = System.nanoTime();
            }
            records = consumer.poll(TIMEOUT_WAIT_FOR_MESSAGES);
            records.forEach(record -> {
                buffer.add(record.value());
            });
        }

        void processBuffer(List buffer, List receivedUserEventList) {
            Collections.sort(buffer);
            buffer.forEach(userEvent -> {
                receivedUserEventList.add(userEvent);
                logger.info("Processing message with Global Sequence number: " + userEvent.getGlobalSequenceNumber() + ", User Event Id: " + userEvent.getUserEventId());
            });
            buffer.clear();
        }
        ```

        每个事件 ID 都与其对应的分区一起出现在输出中，如下所示：

        ```log
        d6ef910f-2e65-410d-8b86-fa0fc69f2333, 0
        4d6bfe60-7aad-4d1b-a536-cc735f649e1a, 4
        9b68dcfe-a6c8-4cca-874d-cfdda6a93a8f, 4
        84bd88f5-9609-4342-a7e5-d124679fa55a, 3
        55c00440-84e0-4234-b8df-d474536e9357, 2
        8fee6cac-7b8f-4da0-a317-ad38cc531a68, 1
        d04c1268-25c1-41c8-9690-fec56397225d, 0
        11ba8121-5809-4abf-9d9c-aa180330ac27, 4
        8e00173c-b8e1-4cf7-ae8c-8a9e28cfa6b2, 4
        e1acd392-db07-4325-8966-0f7c7a48e3d3, 3
        ```

        带有全局序列号和事件 ID 的消费者输出：

        ```log
        1, d6ef910f-2e65-410d-8b86-fa0fc69f2333
        2, 4d6bfe60-7aad-4d1b-a536-cc735f649e1a
        3, 9b68dcfe-a6c8-4cca-874d-cfdda6a93a8f
        4, 84bd88f5-9609-4342-a7e5-d124679fa55a
        5, 55c00440-84e0-4234-b8df-d474536e9357
        6, 8fee6cac-7b8f-4da0-a317-ad38cc531a68
        7, d04c1268-25c1-41c8-9690-fec56397225d
        8, 11ba8121-5809-4abf-9d9c-aa180330ac27
        9, 8e00173c-b8e1-4cf7-ae8c-8a9e28cfa6b2
        10, e1acd392-db07-4325-8966-0f7c7a48e3d3
        ```

    3. 带缓冲的外部排序注意事项

        在这种方法中，每个消费者实例都会缓冲报文，并根据报文的序列号按顺序进行处理。但也有一些注意事项：

        - 缓冲区大小： 缓冲区的大小会根据传入报文的数量而增加。在按照序列号严格排序的优先级实现中，我们可能会看到缓冲区的显著增长，尤其是在消息传递出现延迟的情况下。例如，如果我们每分钟处理 100 条报文，但由于延迟突然收到 200 条，缓冲区就会意外增长。因此，我们必须有效地管理缓冲区大小，并准备好应对策略，以防缓冲区超过预期限制。
        - 延迟： 当我们对信息进行缓冲时，实质上是让它们在处理前稍作等待（引入延迟）。一方面，它可以帮助我们保持有序；另一方面，它也会拖慢整个流程。关键是要在维持秩序和减少延迟之间找到合适的平衡点。
        - 故障： 如果消费者失效，我们可能会丢失缓冲信息，为了防止这种情况，我们可能需要定期保存缓冲区的状态
        - 延迟信息： 在窗口处理后到达的信息将不按顺序排列。根据使用情况，我们可能需要处理或丢弃此类消息的策略
        - 状态管理： 如果处理涉及有状态的操作，我们就需要有机制来管理和持续跨窗口的状态。
        - 资源利用： 在缓冲区中保存大量报文需要内存。我们需要确保有足够的资源来处理这些信息，尤其是当信息在缓冲区中停留的时间较长时。

    4. 惰性生产者

        Kafka 的惰性生产者（idempotent producer）功能旨在精确地传递一次消息，从而避免任何重复。在生产者可能会因网络错误或其他瞬时故障而重试发送消息的情况下，这一点至关重要。虽然idempotency的主要目标是防止消息重复，但它也间接影响了消息的排序。Kafka 使用两样东西来实现idempotency：一个是生产者 ID（PID），另一个是序列号，序列号作为idempotency密钥，在特定分区的上下文中是唯一的。

        - 序列号：Kafka 为生产者发送的每条消息分配序列号。这些序列号在每个分区中都是唯一的，确保生产者按特定顺序发送的消息在被 Kafka 接收后，在特定分区内按相同顺序写入。序列号保证了单个分区内的顺序。但是，在向多个分区发送消息时，就不能保证跨分区的全局顺序了。例如，如果生产者将消息 M1、M2 和 M3 分别发送到分区 P1、P2 和 P3，则每条消息在其分区内都会收到唯一的序列号。但是，这并不能保证这些分区之间的相对消费顺序
        - 生产者 ID (PID)： 启用幂等性后，代理将为每个生产者分配一个唯一的生产者 ID（PID）。该 PID 与序列号相结合，使 Kafka 能够识别并丢弃因生产者重试而产生的任何重复消息。

        借助序列号，Kafka 可以按照消息产生的顺序将其写入分区，从而保证消息的排序，并利用 PID 和惰性功能防止重复。要启用empotent 生产者，我们需要在生产者配置中将 "enable.idempotence" 属性设为 true：

        `props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");`

4. 生产者和消费者的关键配置

    Kafka 生产者和消费者的一些关键配置会影响消息排序和吞吐量。

    1. 生产者配置

        - MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION：如果我们要发送大量消息，那么 Kafka 中的这一设置有助于决定我们可以发送多少消息而无需等待 "读取 "收据。如果我们在不开启幂等性的情况下将其设置为大于 1，那么如果我们要重新发送消息，就可能会打乱消息的顺序。但是，如果我们开启了幂等性，Kafka 就会保持消息的顺序，即使我们一次发送了很多消息。如果要实现超级严格的顺序，比如确保在发送下一条消息之前读完每一条消息，我们应该把这个值设为 1。如果我们想优先考虑速度，而不是完美的顺序，那么我们可以把这个值设为 5，但这有可能带来顺序问题。
        - BATCH_SIZE_CONFIG 和 LINGER_MS_CONFIG：Kafka 以字节为单位控制默认批量大小，目的是将同一分区的记录分组为更少的请求，以获得更好的性能。如果我们把这个限制设得太低，我们就会发送大量的小群，这可能会拖慢我们的速度。但如果设置得太高，又可能无法充分利用内存。如果分组还没有满，Kafka 可以等待一段时间再发送。这个等待时间由 LINGER_MS_CONFIG 控制。 如果有更多的消息快速进来，足以填满我们设定的上限，它们就会立即发送，但如果没有，Kafka 不会继续等待--它会在时间到时发送我们有的任何消息。这就像是在速度和效率之间取得平衡，确保我们每次发送的消息数量足够多，而不会出现不必要的延迟。

        ```java
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5");
        ```

    2. 消费者配置

        - MAX_POLL_RECORDS_CONFIG：这是 Kafka 消费者每次请求数据时抓取记录数量的限制。如果我们把这个数字设置得很高，就可以一次性抓取大量数据，提高吞吐量。但有一个问题--我们抓取的数据越多，就越难保证所有数据都井然有序。因此，我们需要找到一个既高效又不被淹没的最佳点
        - FETCH_MIN_BYTES_CONFIG：如果我们把这个数字设置得很高，Kafka 就会一直等待，直到有足够的数据达到我们的最小字节数，才会把数据发送过去。这可能意味着更少的传输次数（或获取次数），对提高效率大有裨益。但是，如果我们很着急，想要快速获得数据，我们可以把这个数字设置得低一些，这样 Kafka 就能更快地把它所拥有的数据发送给我们。例如，如果我们的消费者应用程序是资源密集型的，或者需要保持严格的消息顺序，特别是在多线程的情况下，较小的批次可能会有好处
        - FETCH_MAX_WAIT_MS_CONFIG：这将决定我们的消费者等待 Kafka 收集足够的数据以满足我们的 FETCH_MIN_BYTES_CONFIG 的时间。 如果我们将这个时间设置得较高，我们的消费者就愿意等待更长的时间，从而有可能一次性获得更多的数据。但如果我们很着急，就可以将时间设置得低一些，这样消费者就能更快地获得数据，即使数据量没有那么多。这是在等待更多数据和快速获取数据之间的一种平衡行为

        ```java
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500");
        ```

5. 结论

    在本文中，我们深入探讨了 Kafka 中消息排序的复杂性。我们探讨了面临的挑战，并提出了应对策略。无论是通过单个分区、带有时间窗口缓冲的外部排序，还是idempotent producers，Kafka 都能提供定制的解决方案来满足消息排序的需求。
