# [Java中的KafkaStreams简介](https://www.baeldung.com/java-kafka-streams)

1. 概述

    本文将介绍 [KafkaStreams](https://kafka.apache.org/documentation/streams/) 库。

    KafkaStreams 由 Apache Kafka 的创建者设计。该软件的主要目标是让程序员能够创建高效、实时、流式的应用程序，这些应用程序可以作为微服务（Microservices）使用。

    KafkaStreams 使我们能够从 Kafka 主题中消费数据、分析或转换数据，并有可能将数据发送到另一个 Kafka 主题。

    为了演示 KafkaStreams，我们将创建一个简单的应用程序，从一个主题中读取句子，计算单词的出现次数，并打印每个单词的计数。

    需要注意的是，KafkaStreams 库不是反应式的，不支持异步操作和背压处理。

2. Maven 依赖

    要开始使用 KafkaStreams 编写流处理逻辑，我们需要为 kafka-streams 和 kafka-clients 添加依赖：

    ```xml
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-streams</artifactId>
        <version>3.4.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.4.0</version>
    </dependency>
    ```

    我们还需要安装并启动 Apache Kafka，因为我们将使用一个 Kafka 主题。该主题将是我们流作业的数据源。

    我们可以从[官方网站](https://www.confluent.io/download/)下载 Kafka 和其他所需的依赖项。

3. 配置 KafkaStreams 输入

    我们要做的第一件事就是定义输入的 Kafka 主题。

    我们可以使用下载的 Confluent 工具--它包含一个 Kafka 服务器。它还包含 kafka-console-producer，我们可以用它将消息发布到 Kafka。

    要开始使用，让我们运行 Kafka 集群：

    `./confluent start`

    Kafka 启动后，我们可以使用 APPLICATION_ID_CONFIG 定义数据源和应用程序名称：

    ```java
    String inputTopic = "inputTopic";

    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(
    StreamsConfig.APPLICATION_ID_CONFIG, 
    "wordcount-live-test");
    ```

    一个关键的配置参数是 BOOTSTRAP_SERVER_CONFIG，它是我们刚启动的本地 Kafka 实例的 URL：

    ```java
    private String bootstrapServers = "localhost:9092";
    streamsConfiguration.put(
    StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 
    bootstrapServers);
    ```

    接下来，我们需要传递将从 inputTopic 消耗的消息的键和值的类型：

    ```java
    streamsConfiguration.put(
    StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, 
    Serdes.String().getClass().getName());
    streamsConfiguration.put(
    StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, 
    Serdes.String().getClass().getName());
    ```

    流处理通常是有状态的。当我们要保存中间结果时，需要指定 STATE_DIR_CONFIG 参数。

    在我们的测试中，我们使用的是本地文件系统：

    ```java
    this.stateDirectory = Files.createTempDirectory("kafka-streams");
    streamsConfiguration.put(
    StreamsConfig.STATE_DIR_CONFIG, this.stateDirectory.toAbsolutePath().toString());
    ```

4. 构建流拓扑

    定义输入主题后，我们就可以创建流媒体拓扑结构（Streaming Topology），即定义如何处理和转换事件。

    在我们的示例中，我们要实现一个单词计数器。对于发送到 inputTopic 的每个句子，我们都要将其拆分成单词，并计算每个单词的出现次数。

    我们可以使用 KStreamsBuilder 类的实例来开始构建拓扑结构：

    ```java
    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> textLines = builder.stream(inputTopic);
    Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

    KTable<String, Long> wordCounts = textLines
    .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
    .groupBy((key, word) -> word)
    .count();
    ```

    要实现字数统计，首先需要使用正则表达式分割值。

    split 方法将返回一个数组。我们使用 flatMapValues() 将其扁平化。否则，我们最终会得到一个数组列表，使用这种结构编写代码会很不方便。

    最后，我们将汇总每个单词的值，并调用 count() 计算特定单词的出现次数。

5. 处理结果

    我们已经计算了输入信息的字数。现在，让我们使用 foreach() 方法在标准输出中打印结果：

    ```java
    wordCounts.toStream()
    .foreach((word, count) -> System.out.println("word: " + word + " -> " + count));
    ```

    在生产中，此类流作业通常会将输出发布到另一个 Kafka 主题。

    我们可以使用 to() 方法做到这一点：

    ```java
    String outputTopic = "outputTopic";
    wordCounts.toStream()
    .to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));
    ```

    Serde 类为我们提供了 Java 类型的预配置序列化器，用于将对象序列化为字节数组。然后，字节数组将被发送到 Kafka 主题。

    我们使用 String 作为主题的键，Long 作为实际计数的值。to() 方法将把生成的数据保存到 outputTopic 中。

6. 启动 KafkaStream 作业

    到此为止，我们构建了一个可以执行的拓扑结构。但是，作业还没有开始。

    我们需要在 KafkaStreams 实例上调用 start() 方法来显式启动作业：

    ```java
    Topology topology = builder.build();
    KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);
    streams.start();

    Thread.sleep(30000);
    streams.close();
    ```

    请注意，我们正在等待 30 秒钟来完成作业。在实际场景中，该作业会一直运行，处理 Kafka 到达的事件。

    我们可以通过向 Kafka 主题发布一些事件来测试我们的作业。

    让我们启动一个 kafka-console-producer 并手动发送一些事件到我们的 inputTopic：

    ```log
    ./kafka-console-producer --topic inputTopic --broker-list localhost:9092
    >"this is a pony"
    >"this is a horse and pony"
    ```

    这样，我们就向 Kafka 发布了两个事件。我们的应用程序将消耗这些事件，并打印以下输出：

    ```java
    word:  -> 1
    word: this -> 1
    word: is -> 1
    word: a -> 1
    word: pony -> 1
    word:  -> 2
    word: this -> 2
    word: is -> 2
    word: a -> 2
    word: horse -> 1
    word: and -> 1
    word: pony -> 2
    ```

    我们可以看到，当第一条信息到达时，单词 pony 只出现了一次。但当我们发送第二条信息时，"pony" 这个词第二次打印出来： "word: pony -> 2"。

7. 结论

    本文讨论了如何使用 Apache Kafka 作为数据源和 KafkaStreams 库作为流处理库来创建一个主要的流处理应用程序。
