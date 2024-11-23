# [Apache Flink with Java简介](https://www.baeldung.com/apache-flink)

1. 一览表

    Apache Flink是一个大数据处理框架，允许程序员以非常高效和可扩展的方式处理大量数据。

    在本文中，我们将介绍[Apache Flink Java API](https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/)中可用的一些核心API概念和标准数据转换。此API的流畅风格使其易于使用Flink的中心结构——分布式集合。

    首先，我们将看看Flink的DataSet API转换，并用它们来实现字数计数程序。然后，我们将简要了解Flink的DataStream API，它允许您实时处理事件流。

2. Maven依赖性

    要开始，我们需要将Maven依赖项添加到flink-java和flink-test-utils库中：

    ```xml
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-java</artifactId>
        <version>1.16.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-test-utils</artifactId>
        <version>1.16.1</version>
        <scope>test<scope>
    </dependency>
    ```

    Flink[连接器](https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/datastream/overview/)可以和多种多样的第三方系统进行交互。

    目前支持以下系统。 Currently these systems are supported as part of the Apache Flink project:

    - Apache Kafka (source/sink)
    - Apache Cassandra (source/sink)
    - Amazon DynamoDB (sink)
    - Amazon Kinesis Data Streams (source/sink)
    - Amazon Kinesis Data Firehose (sink)
    - DataGen (source)
    - [Elasticsearch](https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/datastream/elasticsearch/) (sink)：ES6/7
    - Opensearch (sink)
    - FileSystem (sink)
    - RabbitMQ (source/sink)
    - Google PubSub (source/sink)
    - Hybrid Source (source)
    - Apache Pulsar (source)
    - [JDBC](https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/datastream/jdbc/) (sink)
    - [MongoDB](https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/datastream/mongodb/) (source/sink)
    - Prometheus (sink)

    请记住，在使用一种连接器时，通常需要额外的第三方组件，比如：数据存储服务器或者消息队列。 要注意这些列举的连接器是 Flink 工程的一部分，包含在发布的源码中，但是不包含在二进制发行版中。 更多说明可以参考对应的子部分。

    Flink 还有些一些额外的连接器通过 Apache Bahir 发布, 包括:

    - Apache ActiveMQ (source/sink)
    - Apache Flume (sink)
    - Redis (sink)
    - Akka (sink)
    - Netty (source)

3. 核心API概念

    在使用Flink时，我们需要知道一些与其API相关的事情：

    - 每个Flink程序都对分布式数据集合进行转换。提供了各种数据转换功能，包括过滤、映射、连接、分组和聚合
    - Flink中的接收器操作触发流的执行，以产生程序的预期结果，例如将结果保存到文件系统或将其打印到标准输出
    - Flink转换是懒惰的，这意味着在调用汇操作之前不会执行它们
    - Apache Flink API支持两种操作模式——批处理和实时。如果您正在处理可以在批处理模式下处理的有限数据源，您将使用DataSet API。如果您想实时处理无限数据流，您需要使用DataStream API。

4. 数据集API转换

    Flink程序的入口点是[ExecutionEnvironment](https://ci.apache.org/projects/flink/flink-docs-release-1.2/api/java/org/apache/flink/api/scala/ExecutionEnvironment.html)类的实例——这定义了程序执行的上下文。

    让我们创建一个执行环境来开始我们的处理：

    ```java
    ExecutionEnvironment env
    = ExecutionEnvironment.getExecutionEnvironment();
    ```

    请注意，当您在本地计算机上启动应用程序时，它将在本地JVM上执行处理。如果您想在计算机集群上开始处理，您需要在这些计算机上安装[Apache Flink](https://nightlies.apache.org/flink/flink-docs-release-1.14//docs/try-flink/local_installation/)，并相应地配置执行环境。

    1. 创建数据集

        要开始执行数据转换，我们需要为我们的程序提供数据。

        让我们使用我们的ExecutionEnvironement创建一个DataSet类的实例：

        `DataSet<Integer> amounts = env.fromElements(1, 29, 40, 50);`

        您可以从多个来源创建数据集，如Apache Kafka、CSV、文件或几乎任何其他数据源。

    2. 过滤和减少

        创建DataSet类的实例后，您可以对其进行转换。

        假设你想过滤超过一定阈值的数字，然后将它们全部相加。您可以使用thefilter（）和reduce（）转换来实现这一点：

        ```java
        int threshold = 30;
        List<Integer> collect = amounts
        .filter(a -> a > threshold)
        .reduce((integer, t1) -> integer + t1)
        .collect();
        assertThat(collect.get(0)).isEqualTo(90);
        ```

        请注意，collect（）方法是一种触发实际数据转换的汇操作。

    3. Map

        假设您有一个人员对象的数据集：

        ```java
        private static class Person {
            private int age;
            private String name;
            // standard constructors/getters/setters
        }
        ```

        接下来，让我们创建一个这些对象的数据集：

        ```java
        DataSet<Person> personDataSource = env.fromCollection(
        Arrays.asList(
            new Person(23, "Tom"),
            new Person(75, "Michael")));
        ```

        假设您只想从集合的每个对象中提取年龄字段。您可以使用map（）变换仅获取Person类的特定字段：

        ```java
        List<Integer> ages = personDataSource
        .map(p -> p.age)
        .collect();
        assertThat(ages).hasSize(2);
        assertThat(ages).contains(23, 75);
        ```

    4. 连接

        当您有两个数据集时，您可能想在某个id字段中加入它们。为此，您可以使用join（）转换。

        让我们创建用户的交易和地址集合：

        ```java
        Tuple3<Integer, String, String> address
        = new Tuple3<>(1, "5th Avenue", "London");
        DataSet<Tuple3<Integer, String, String>> addresses
        = env.fromElements(address);

        Tuple2<Integer, String> firstTransaction 
        = new Tuple2<>(1, "Transaction_1");
        DataSet<Tuple2<Integer, String>> transactions 
        = env.fromElements(firstTransaction, new Tuple2<>(12, "Transaction_2"));
        ```

        两个元组中的第一个字段是整数类型，这是一个id字段，我们想要在其中连接两个数据集。

        为了执行实际的连接逻辑，我们需要为地址和事务实现KeySelector接口：

        ```java
        private static class IdKeySelectorTransaction
        implements KeySelector<Tuple2<Integer, String>, Integer> {
            @Override
            public Integer getKey(Tuple2<Integer, String> value) {
                return value.f0;
            }
        }

        private static class IdKeySelectorAddress 
        implements KeySelector<Tuple3<Integer, String, String>, Integer> {
            @Override
            public Integer getKey(Tuple3<Integer, String, String> value) {
                return value.f0;
            }
        }
        ```

        每个选择器只返回应该执行连接的字段。

        不幸的是，这里无法使用lambda表达式，因为Flink需要通用类型信息。

        接下来，让我们使用这些选择器实现合并逻辑：

        ```java
        List<Tuple2<Tuple2<Integer, String>, Tuple3<Integer, String, String>>>
        joined = transactions.join(addresses)
        .where(new IdKeySelectorTransaction())
        .equalTo(new IdKeySelectorAddress())
        .collect();

        assertThat(joined).hasSize(1);
        assertThat(joined).contains(new Tuple2<>(firstTransaction, address));
        ```

    5. 排序

        假设你有以下Tuple2的集合：

        ```java
        Tuple2<Integer, String> secondPerson = new Tuple2<>(4, "Tom");
        Tuple2<Integer, String> thirdPerson = new Tuple2<>(5, "Scott");
        Tuple2<Integer, String> fourthPerson = new Tuple2<>(200, "Michael");
        Tuple2<Integer, String> firstPerson = new Tuple2<>(1, "Jack");
        DataSet<Tuple2<Integer, String>> transactions = env.fromElements(
        fourthPerson, secondPerson, thirdPerson, firstPerson);
        ```

        如果您想按元组的第一个字段对此集合进行排序，您可以使用sortPartitions（）转换：

        ```java
        List<Tuple2<Integer, String>> sorted = transactions
        .sortPartition(new IdKeySelectorTransaction(), Order.ASCENDING)
        .collect();

        assertThat(sorted)
        .containsExactly(firstPerson, secondPerson, thirdPerson, fourthPerson);
        ```

5. 字数

    字数问题通常用于展示大数据处理框架的能力。基本解决方案涉及计算文本输入中的单词出现次数。让我们使用Flink来解决这个问题。

    作为我们解决方案的第一步，我们创建了一个LineSplitter类，该类将我们的输入拆分为令牌（单词），为每个令牌收集一个键值对的Tuple2。在每个元组中，键是文本中发现的单词，值是整数一（1）。

    该类实现了[FlatMapFunction](https://ci.apache.org/projects/flink/flink-docs-release-1.1/api/java/org/apache/flink/api/common/functions/FlatMapFunction.html)接口，该接口将String作为输入，并生成一个[Tuple2<String, Integer>](https://nightlies.apache.org/flink/flink-docs-release-1.3/api/java/org/apache/flink/api/java/tuple/Tuple2.html)：

    ![LineSplitter](src/main/java/com/baeldung/flink/LineSplitter.java)

    我们在[Collecter](https://ci.apache.org/projects/flink/flink-docs-release-1.0/api/java/org/apache/flink/util/class-use/Collector.html)类上调用collect（）方法，在处理管道中向前推进数据。

    我们的下一步也是最后一步是按其第一个元素（单词）对元组进行分组，然后在第二个元素上执行总和聚合，以生成单词出现次数的计数：

    ```java
    public static DataSet<Tuple2<String, Integer>> startWordCount(
    ExecutionEnvironment env, List<String> lines) throws Exception {
        DataSet<String> text = env.fromCollection(lines);

        return text.flatMap(new LineSplitter())
        .groupBy(0)
        .aggregate(Aggregations.SUM, 1);
    }
    ```

    我们正在使用三种类型的Flink变换：flatMap（）、groupBy（）和aggregation（）。

    让我们编写一个测试来断言字数实现是否按预期工作：

    ```java
    List<String> lines = Arrays.asList(
    "This is a first sentence",
    "This is a second sentence with a one word");

    DataSet<Tuple2<String, Integer>> result = WordCount.startWordCount(env, lines);

    List<Tuple2<String, Integer>> collect = result.collect();
    
    assertThat(collect).containsExactlyInAnyOrder(
    new Tuple2<>("a", 3), new Tuple2<>("sentence", 2), new Tuple2<>("word", 1),
    new Tuple2<>("is", 2), new Tuple2<>("this", 2), new Tuple2<>("second", 1),
    new Tuple2<>("first", 1), new Tuple2<>("with", 1), new Tuple2<>("one", 1));
    ```

6. 数据流API

    1. 创建数据流

        Apache Flink还支持通过其DataStream API处理事件流。如果我们想开始消耗事件，我们首先需要使用StreamExecutionEnvironment类：

        ```java
        StreamExecutionEnvironment executionEnvironment
        = StreamExecutionEnvironment.getExecutionEnvironment();
        ```

        接下来，我们可以使用来自各种来源的执行环境创建事件流。它可能是一些消息总线，如Apache Kafka，但在本例中，我们将简单地从几个字符串元素中创建一个源代码：

        ```java
        DataStream<String> dataStream = executionEnvironment.fromElements(
        "This is a first sentence", 
        "This is a second sentence with a one word");
        ```

        我们可以像在正常DataSet类中一样对DataStream的每个元素进行转换：

        `SingleOutputStreamOperator<String> upperCase = text.map(String::toUpperCase);`

        要触发执行，我们需要调用一个汇操作，如print（），该操作将仅将转换结果打印到标准输出，然后是StreamExectionEnvironmentclass上的execute（）方法：

        ```java
        upperCase.print();
        env.execute();
        ```

        它将产生以下输出：

        ```log
        1> THIS IS A FIRST SENTENCE
        2> THIS IS A SECOND SENTENCE WITH A ONE WORD
        ```

    2. 事件窗口

        在实时处理事件流时，您有时可能需要将事件分组在一起，并在这些事件的窗口上应用一些计算。

        假设我们有一个事件流，其中每个事件都是由事件编号和事件发送到我们系统时的时间戳组成的一对，我们可以容忍不按顺序的事件，但前提是它们延迟不超过20秒。

        在这个例子中，让我们首先创建一个模拟相隔几分钟的两个事件的流，并定义一个时间戳提取器，该提取器指定我们的延迟阈值：

        ```java
        SingleOutputStreamOperator<Tuple2<Integer, Long>> windowed
        = env.fromElements(
        new Tuple2<>(16, ZonedDateTime.now().plusMinutes(25).toInstant().getEpochSecond()),
        new Tuple2<>(15, ZonedDateTime.now().plusMinutes(2).toInstant().getEpochSecond()))
        .assignTimestampsAndWatermarks(
            new BoundedOutOfOrdernessTimestampExtractor
            <Tuple2<Integer, Long>>(Time.seconds(20)) {
                @Override
                public long extractTimestamp(Tuple2<Integer, Long> element) {
                return element.f1 * 1000;
                }
            });
        ```

        接下来，让我们定义一个窗口操作，将我们的事件分组到五秒的窗口中，并对这些事件进行转换：

        ```java
        SingleOutputStreamOperator<Tuple2<Integer, Long>> reduced = windowed
        .windowAll(TumblingEventTimeWindows.of(Time.seconds(5)))
        .maxBy(0, true);
        reduced.print();
        ```

        它将获得每五秒窗口的最后一个元素，因此它打印出来：

        `1> (15,1491221519)`

        请注意，我们没有看到第二个事件，因为它比指定的延迟阈值晚到达。

7. 结论

    在本文中，我们介绍了Apache Flink框架，并查看了其API提供的一些转换。

    我们使用Flink流畅且实用的DataSet API实施了一个单词计数程序。然后，我们查看了DataStream API，并在事件流上实现了简单的实时转换。
