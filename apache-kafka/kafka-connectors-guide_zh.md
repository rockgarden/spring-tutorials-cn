# [Kafka连接器介绍](https://www.baeldung.com/kafka-connectors-guide)

1. 概述

    Apache Kafka® 是一个分布式流平台。在上一篇教程中，我们讨论了如何使用[Spring实现Kafka消费者和生产者](https://www.baeldung.com/spring-kafka)。

    在本教程中，我们将学习如何使用 Kafka 连接器。

    我们将了解

    - 不同类型的 Kafka 连接器
    - Kafka 连接器的功能和模式
    - 使用属性文件和 REST API 配置连接器 2.

2. Kafka Connect 和 Kafka 连接器的基础知识

    Kafka Connect 是一个框架，用于使用所谓的连接器将 Kafka 与数据库、键值存储、搜索索引和文件系统等外部系统连接起来。

    Kafka 连接器是即用型组件，可以帮助我们将数据从外部系统导入到 Kafka 主题，并将数据从 Kafka 主题导出到外部系统。我们可以使用常见数据源和数据汇的现有连接器实现，也可以实现自己的连接器。

    源连接器从系统中收集数据。源系统可以是整个数据库、流表或消息代理。源连接器还可以将应用服务器中的度量数据收集到 Kafka 主题中，从而以较低的延迟将数据用于流处理。

    汇连接器将数据从 Kafka 主题传送到其他系统，这些系统可能是 Elasticsearch 等索引、Hadoop 等批处理系统或任何类型的数据库。

    有些连接器由社区维护，有些则由 Confluent 或其合作伙伴提供支持。实际上，我们可以找到适用于大多数流行系统的连接器，如 S3、JDBC 和 Cassandra 等。

3. 功能

    Kafka Connect 的功能包括

    - 将外部系统与 Kafka 连接起来的框架--它简化了连接器的开发、部署和管理
    - 分布式和独立模式--通过利用 Kafka 的分布式特性，它可以帮助我们部署大型集群，也可以为开发、测试和小型生产部署进行设置
    - REST 接口--我们可以使用 REST API 管理连接器
    - 自动偏移管理--Kafka Connect 帮助我们处理偏移提交过程，省去了手动执行连接器开发中这一容易出错的部分的麻烦
    - 默认情况下是分布式和可扩展的--Kafka Connect使用现有的组管理协议；我们可以添加更多的工作者来扩展Kafka Connect集群
    - 流和批处理集成--Kafka Connect 是连接流和批处理数据系统与 Kafka 现有功能的理想解决方案
    - 转换 - 这使我们能够对单个消息进行简单、轻量级的修改

4. 设置

    我们将下载由 Kafka 背后的公司 Confluent, Inc. 提供的 Kafka 发行版 Confluent Platform，而不是使用普通的 Kafka 发行版。与普通 Kafka 相比，Confluent Platform 附带了一些额外的工具和客户端，以及一些额外的预置连接器。

    对于我们来说，开源版就足够了，可以在 Confluent 的[网站](https://www.confluent.io/download/)上找到。

5. 快速启动 Kafka Connect

    首先，我们将讨论 Kafka Connect 的原理，使用其最基本的连接器，即文件源连接器和文件汇连接器。

    Confluent Platform 自带这两个连接器以及参考配置，非常方便。

    1. 源连接器配置

        对于源连接器，参考配置可在 `$CONFLUENT_HOME/etc/kafka/connect-file-source.properties` 中找到：

        ```properties
        name=local-file-source
        connector.class=FileStreamSource
        tasks.max=1
        topic=connect-test
        file=test.txt
        ```

        此配置具有一些所有源连接器共有的属性：

        - name 是用户为连接器实例指定的名称
        - connector.class 指定实现类，基本上就是连接器的种类
        - tasks.max 指定源连接器应并行运行的实例数量
        - topic 定义了连接器应向其发送输出的主题

        在这种情况下，我们还有一个特定于连接器的属性：

        - file 定义了连接器读取输入的文件

        为了实现这一功能，让我们创建一个包含一些内容的基本文件：

        `echo -e "foo\nbar\n" > $CONFLUENT_HOME/test.txt`

        注意，工作目录是 $CONFLUENT_HOME。

    2. 水槽连接器配置

        对于汇连接器，我们将使用 $CONFLUENT_HOME/etc/kafka/connect-file-sink.properties 中的参考配置：

        ```properties
        name=local-file-sink
        connector.class=FileStreamSink
        tasks.max=1
        file=test.sink.txt
        topics=connect-test
        ```

        从逻辑上讲，它包含完全相同的参数，不过这次 connector.class 指定了汇连接器的实现，而 file 则是连接器应写入内容的位置。

    3. 工作程序配置

        最后，我们必须配置 Connect Worker，它将整合两个连接器，并完成从源连接器读取数据并写入汇连接器的工作。

        为此，我们可以使用 $CONFLUENT_HOME/etc/kafka/connect-standalone.properties：

        ```properties
        bootstrap.servers=localhost:9092
        key.converter=org.apache.kafka.connect.json.JsonConverter
        value.converter=org.apache.kafka.connect.json.JsonConverter
        key.converter.schemas.enable=false
        value.converter.schemas.enable=false
        offset.storage.file.filename=/tmp/connect.offsets
        offset.flush.interval.ms=10000
        plugin.path=/share/java
        ```

        请注意，plugin.path 可以包含连接器实现所在的路径列表。

        由于我们将使用与 Kafka 捆绑的连接器，因此可以将 plugin.path 设置为 $CONFLUENT_HOME/share/java。在使用 Windows 时，这里可能需要提供绝对路径。

        至于其他参数，我们可以保留默认值：

        - bootstrap.servers 包含 Kafka 代理的地址
        - key.converter 和 value.converter 定义了转换器类，当数据从源流向 Kafka，再从 Kafka 流向水槽(sink)时，它们会对数据进行序列化和反序列化处理
        - key.converter.schemas.enable 和 value.converter.schemas.enable 是转换器的特定设置
        - offset.storage.file.filename 是在独立模式下运行 Connect 时最重要的设置：它定义了 Connect 应在何处存储偏移数据
        - offset.flush.interval.ms 定义了 Worker 尝试提交任务偏移量的时间间隔。

        参数列表相当成熟，请查看[官方文档](http://kafka.apache.org/documentation/#connectconfigs)了解完整列表。

    4. 单机模式下的 Kafka 连接

        这样，我们就可以开始第一个连接器的设置了：

        ```bash
        $CONFLUENT_HOME/bin/connect-standalone \
        $CONFLUENT_HOME/etc/kafka/connect-standalone.properties \
        $CONFLUENT_HOME/etc/kafka/connect-file-source.properties \
        $CONFLUENT_HOME/etc/kafka/connect-file-sink.properties
        ```

        首先，我们可以使用命令行检查主题的内容：

        `$CONFLUENT_HOME/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic connect-test --from-beginning`

        我们可以看到，源连接器从 test.txt 文件中获取数据，将其转换为 JSON 格式，然后将其发送到 Kafka：

        ```json
        {"schema":{"type":"string","optional":false},"payload":"foo"}
        {"schema":{"type":"string","optional":false},"payload":"bar"}
        ```

        如果我们查看一下 $CONFLUENT_HOME 文件夹，就会发现这里创建了一个 test.sink.txt 文件：

        ```bash
        cat $CONFLUENT_HOME/test.sink.txt
        foo
        bar
        ```

        由于 sink 连接器从 payload 属性中提取值并将其写入目标文件，因此 test.sink.txt 中的数据与原始 test.txt 文件的内容相同。

        现在，让我们向 test.txt 添加更多行。

        我们会发现源连接器会自动检测到这些更改。

        我们只需确保在末尾插入换行符，否则源连接器不会考虑最后一行。

        此时，让我们停止 Connect 进程，因为我们将在几行中以分布式模式启动 Connect。

6. Connect 的 REST API

    到目前为止，我们都是通过命令行传递属性文件进行配置的。不过，由于 Connect 被设计为以服务形式运行，因此还提供了 REST API。

    默认情况下，它在 <http://localhost:8083> 上可用。以下是几个端点

    - GET /connectors - 返回包含所有正在使用的连接器的列表
    - GET /connectors/{name} - 返回特定连接器的详细信息
    - POST /connectors - 创建一个新连接器；请求体应是一个 JSON 对象，其中包含一个字符串名称字段和一个包含连接器配置参数的对象配置字段
    - GET /connectors/{name}/status - 返回连接器的当前状态，包括运行、失败或暂停、分配给哪个 Worker、失败时的错误信息以及所有任务的状态。
    - DELETE /connectors/{name} - 删除连接器，从容停止所有任务并删除其配置
    - GET /connector-plugins - 返回安装在 Kafka Connect 集群中的连接器插件列表。

    [官方文档](http://kafka.apache.org/documentation/#connect_rest)提供了一份包含所有端点的列表。

    我们将在下一节中使用 REST API 创建新的连接器。

7. 分布式模式下的 Kafka Connect

    独立模式非常适合开发和测试，以及较小的设置。但是，如果我们想充分利用 Kafka 的分布式特性，就必须以分布式模式启动 Connect。

    这样，连接器设置和元数据就会存储在 Kafka 主题中，而不是文件系统中。因此，工作节点实际上是无状态的。

    1. 启动连接

        分布式模式的参考配置见 $CONFLUENT_HOME/etc/kafka/connect-distributed.properties。

        参数与独立模式的参数基本相同。只有几处不同：

        - group.id 定义连接群集组的名称。该值必须不同于任何消费者组 ID
        - offset.storage.topic、config.storage.topic 和 status.storage.topic 定义了这些设置的主题。我们还可以为每个主题定义复制因子。

        [官方文档](http://kafka.apache.org/documentation/#connectconfigs)再次提供了包含所有参数的列表。

        我们可以在分布式模式下启动 Connect，方法如下

        `$CONFLUENT_HOME/bin/connect-distributed $CONFLUENT_HOME/etc/kafka/connect-distributed.properties`

    2. 使用 REST API 添加连接器

        现在，与独立启动命令相比，我们没有传递任何连接器配置作为参数。相反，我们必须使用 REST API 创建连接器。

        要设置之前的示例，我们必须向 <http://localhost:8083/connectors> 发送两个 POST 请求，其中包含以下 JSON 结构。

        首先，我们需要将源连接器 POST 的主体创建为一个 JSON 文件。在这里，我们将其称为 main/resources/02_Distributed/connect-file-source.json：

        请注意，这看起来与我们第一次使用的参考配置文件非常相似。

        然后我们把它传送出去：

        ```bash
        curl -d @"$CONFLUENT_HOME/connect-file-source.json" \
        -H "Content-Type: application/json" \
        -X POST http://localhost:8083/connectors
        ```

        然后，我们将对 sink 连接器执行同样的操作，调用 main/resources/02_Distributed/connect-file-sink.json 文件：

        然后像之前一样执行 POST

        ```bash
        curl -d @$CONFLUENT_HOME/connect-file-sink.json \
        -H "Content-Type: application/json" \
        -X POST http://localhost:8083/connectors
        ```

        如果需要，我们可以验证设置是否正确：

        ```bash
        $CONFLUENT_HOME/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic connect-distributed --from-beginning
        {"schema":{"type":"string","optional":false},"payload":"foo"}
        {"schema":{"type":"string","optional":false},"payload":"bar"}
        ```

        如果我们查看一下 $CONFLUENT_HOME 文件夹，就会发现这里创建了一个文件 test-distributed.sink.txt：

        ```bash
        cat $CONFLUENT_HOME/test-distributed.sink.txt
        foo
        bar
        ```

        测试完分布式设置后，我们来清理一下，删除两个连接器：

        ```bash
        curl -X DELETE http://localhost:8083/connectors/local-file-source
        curl -X DELETE http://localhost:8083/connectors/local-file-sink
        ```

8. 转换数据

    1. 支持的转换

        转换使我们能够对单个消息进行简单、轻量级的修改。

        Kafka Connect 支持以下内置转换：

        - InsertField - 使用静态数据或记录元数据添加字段
        - ReplaceField - 过滤或重命名字段
        - MaskField - 使用类型的有效空值（例如零或空字符串）替换字段
        - HoistField - 将整个事件封装为结构或映射中的单个字段
        - ExtractField - 从 struct 和 map 中提取特定字段，并在结果中只包含该字段
        - SetSchemaMetadata - 修改模式名称或版本
        - TimestampRouter - 根据原始主题和时间戳修改记录的主题
        - RegexRouter - 根据原始主题、替换字符串和正则表达式修改记录主题

        转换使用以下参数配置：

        - transforms - 以逗号分隔的转换别名列表
        - `transforms.$alias.type` - 转换的类名
        - `transforms.$alias.$transformationSpecificConfig` - 各转换的配置

    2. 应用变换器

        为了测试一些变换功能，让我们设置以下两种变换：

        - 首先，将整个信息封装为一个 JSON 结构。
        - 然后，为该结构添加一个字段

        在应用转换之前，我们必须通过修改 connect-distributed.properties 配置 Connect 使用无模式 JSON：

        ```properties
        key.converter.schemas.enable=false
        value.converter.schemas.enable=false
        ```

        之后，我们必须再次以分布式模式重启 Connect：

        `$CONFLUENT_HOME/bin/connect-distributed $CONFLUENT_HOME/etc/kafka/connect-distributed.properties`

        同样，我们需要将源连接器 POST 的正文创建为 JSON 文件。在这里，我们将其称为 main/resources/02_Transform/connect-file-source-transform.json。

        除了已知的参数外，我们还为两个必要的转换添加了几行。

        然后，让我们执行 POST：

        ```bash
        curl -d @$CONFLUENT_HOME/connect-file-source-transform.json \
        -H "Content-Type: application/json" \
        -X POST http://localhost:8083/connectors
        ```

        让我们在 test-transformation.txt 中写几行：

        ```txt
        Foo
        Bar
        ```

        如果我们现在检查 connect-transformation 主题，就会看到以下几行：

        ```json
        {"line":"Foo","data_source":"test-file-source"}
        {"line":"Bar","data_source":"test-file-source"}
        ```

9. 使用就绪连接器

    使用完这些简单的连接器后，让我们来看看更高级的即用连接器，以及如何安装它们。

    1. 在哪里可以找到连接器

        预置连接器可从不同渠道获得：

        - 一些连接器捆绑在普通 Apache Kafka（文件和控制台的源和汇）
        - 还有一些连接器与 Confluent Platform（ElasticSearch、HDFS、JDBC 和 AWS S3）捆绑在一起。
        - 还可以查看 [Confluent Hub](https://www.confluent.io/hub/)，它有点像 Kafka 连接器的应用商店。提供的连接器数量在持续增长：
        - Confluent 连接器（由 Confluent 开发、测试、记录并提供全面支持）
        - 经过认证的连接器（由第三方实施并经过 Confluent 认证）
        - 社区开发和支持的连接器
        - 除此以外，Confluent 还提供了一个[连接器页面](https://www.confluent.io/product/connectors/)，其中包括一些在 Confluent Hub 上可用的连接器，以及更多的社区连接器。
        - 最后，还有一些供应商提供连接器，作为其产品的一部分。例如，Landoop 提供了一个名为 [Lenses](https://www.landoop.com/downloads/) 的流库，其中也包含一组约 25 个开源连接器（其中许多还在其他地方交叉列出）。

    2. 从 Confluent Hub 安装连接器

        Confluent 企业版提供了一个脚本，用于从 Confluent Hub 安装连接器和其他组件（开源版不包含该脚本）。如果使用的是企业版，我们可以使用以下命令安装连接器：

        `$CONFLUENT_HOME/bin/confluent-hub install confluentinc/kafka-connect-mqtt:1.0.0-preview`

    3. 手动安装连接器

        如果我们需要的连接器在 Confluent Hub 上不可用，或者我们使用的是开源版本的 Confluent，我们可以手动安装所需的连接器。为此，我们必须下载并解压缩连接器，并将包含的库移动到 plugin.path 指定的文件夹中。

        对于每个连接器，压缩包中应包含两个我们感兴趣的文件夹：

        - lib 文件夹包含连接器的 jar，例如 kafka-connect-mqtt-1.0.0-preview.jar，以及连接器所需的其他一些 jar。
        - etc 文件夹包含一个或多个参考配置文件

        我们必须将 lib 文件夹移至 $CONFLUENT_HOME/share/java，或在 connect-standalone.properties 和 connect-distributed.properties 中指定为 plugin.path 的路径。在此过程中，将文件夹重命名为有意义的名称也很有意义。

        我们可以通过在单机模式下启动时引用配置文件，或者直接抓取属性并创建 JSON 文件。

10. 总结

    在本教程中，我们了解了如何安装和使用 Kafka Connect。

    我们了解了连接器的类型，包括源连接器和汇连接器。我们还了解了 Connect 可以运行的一些功能和模式。然后，我们回顾了变压器。最后，我们了解了从哪里获取以及如何安装自定义连接器。
