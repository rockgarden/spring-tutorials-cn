# [使用 Kafka MockConsumer](https://www.baeldung.com/kafka-mockconsumer)

[Data](https://www.baeldung.com/category/data) [测试](https://www.baeldung.com/category/testing)

[Kafka](https://www.baeldung.com/tag/kafka)

1. 概述

    在本教程中，我们将探讨 Kafka 消费者实现之一的 MockConsumer。

    首先，我们将讨论测试 Kafka Consumer 时需要考虑的主要事项。然后，我们将了解如何使用 MockConsumer 来实现测试。

2. 测试 Kafka 消费者

    从 Kafka 消费数据包括两个主要步骤。首先，我们必须订阅主题或手动分配主题分区。其次，我们使用轮询方法对记录批次进行轮询。

    轮询通常是在无限循环中进行的。这是因为我们通常希望持续消耗数据。

    例如，让我们考虑一下仅由订阅和轮询循环组成的简单消费逻辑：

    ```java
    void consume() {
        try {
            consumer.subscribe(Arrays.asList("foo", "bar"));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                records.forEach(record -> processRecord(record));
            }
        } catch (WakeupException ex) {
            // ignore for shutdown
        } catch (RuntimeException ex) {
            // exception handling
        } finally {
            consumer.close();
        }
    }
    ```

    观察上面的代码，我们可以发现有几件事可以测试：

    - 订阅(subscription)
    - 轮询循环
    - 异常处理
    - 消费者是否正确关闭

    我们有多种选择来测试消费逻辑。

    我们可以使用内存中的 Kafka 实例。但是，这种方法有一些缺点。一般来说，内存中的 Kafka 实例会使测试变得非常繁重和缓慢。此外，设置它并不是一项简单的任务，可能会导致测试不稳定。

    或者，我们可以使用 mocking 框架来模拟 Consumer。虽然使用这种方法可以使测试变得轻量级，但设置起来可能有些棘手。

    最后一个选择，或许也是最好的选择，就是使用 MockConsumer，它是一个用于测试的 Consumer 实现。它不仅能帮助我们构建轻量级测试，而且易于设置。

    让我们来看看它提供的功能。

3. 使用 MockConsumer

    MockConsumer 实现了 kafka-clients 库提供的 Consumer 接口。因此，它可以模拟真实消费者的全部行为，而无需编写大量代码。

    让我们看看 MockConsumer 的一些使用示例。特别是，我们将采用在测试消费者应用程序时可能会遇到的一些常见场景，并使用 MockConsumer 来实现它们。

    在我们的示例中，让我们考虑一个从 Kafka 主题中消费国家人口更新的应用程序。更新只包含国家名称及其当前人口：

    main/kafka.consumer/CountryPopulation.java

    我们的消费者只需使用 Kafka 消费者实例轮询更新，处理更新，最后使用 commitSync 方法提交偏移：

    main/kafka.consumer/CountryPopulationConsumer.java

    1. 创建 MockConsumer 实例

        接下来，让我们看看如何创建 MockConsumer 实例：

        test/kafka.consumer/CountryPopulationConsumerUnitTest.java:setUp()

        基本上，我们只需提供偏移重置策略。

        请注意，我们使用更新来收集 countryPopulationConsumer 将收到的记录。这将有助于我们断言预期结果。

        同样，我们使用 pollException 来收集和断言异常。

        对于所有测试用例，我们都将使用上述设置方法。现在，让我们来看看消费者应用程序的几个测试用例。

    2. 分配主题分区

        首先，让我们为 startByAssigning 方法创建一个测试：

        test/kafka.consumer/CountryPopulationConsumerUnitTest.java:whenStartingByAssigningTopicPartition_thenExpectUpdatesAreConsumedCorrectly()

        首先，我们设置了 MockConsumer。首先，我们使用 addRecord 方法向消费者添加一条记录。

        首先要记住的是，我们不能在分配或订阅主题之前添加记录。这就是我们使用 schedulePollTask 方法安排投票任务的原因。我们安排的任务将在获取记录之前的第一次轮询时运行。因此，记录的添加将在分配之后进行。

        同样重要的是，我们不能向 MockConsumer 添加不属于分配给它的主题和分区的记录。

        然后，为了确保消费者不会无限期运行，我们将其配置为在第二次轮询时关闭。

        此外，我们还必须设置起始偏移量。我们使用 updateBeginningOffsets 方法来设置。

        最后，我们检查更新是否正确消耗，并关闭消费者。

    3. 订阅主题

        现在，让我们为 startBySubscribing 方法创建一个测试：

        test/kafka.consumer/CountryPopulationConsumerUnitTest.java:whenStartingBySubscribingToTopic_thenExpectUpdatesAreConsumedCorrectly()

        在这种情况下，添加记录之前首先要做的是重新平衡。我们通过调用 rebalance 方法来模拟再平衡。

        其余操作与 startByAssigning 测试用例相同。

    4. 控制轮询循环

        我们可以通过多种方式控制轮询循环。

        第一种方法是安排轮询任务，就像我们在上述测试中所做的那样。我们可以通过 schedulePollTask 来实现，它将一个 Runnable 作为参数。当我们调用 poll 方法时，我们安排的每个任务都将运行。

        我们的第二个选择是调用 wakeup 方法。通常，我们会通过这种方式中断长时间的轮询调用。实际上，我们就是这样在 CountryPopulationConsumer 中实现 stop 方法的。

        最后，我们可以使用 setPollException 方法设置要抛出的异常：

        test/kafka.consumer/whenStartingBySubscribingToTopicAndExceptionOccurs_thenExpectExceptionIsHandledCorrectly()

    5. 模拟末端偏移和分区信息

        如果我们的消费逻辑是基于终点偏移或分区信息，我们也可以使用 MockConsumer 来模拟这些信息。

        当我们要模拟末端偏移时，可以使用 addEndOffsets 和 updateEndOffsets 方法。

        如果要模拟分区信息，我们可以使用 updatePartitions 方法。

4. 结论

    在本文中，我们探讨了如何使用 MockConsumer 测试 Kafka 消费者应用程序。

    首先，我们举例说明了消费者逻辑，以及哪些是需要测试的重要部分。然后，我们使用 MockConsumer 测试了一个简单的 Kafka 消费者应用程序。

    在此过程中，我们了解了 MockConsumer 的功能以及如何使用它。
