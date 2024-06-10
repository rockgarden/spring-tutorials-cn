# [使用 Spring 云应用程序启动器](https://www.baeldung.com/spring-cloud-app-starter)

1. 简介

    在本文中，我们将演示如何使用 Spring Cloud 应用程序启动器（Spring Cloud App Starter），这些启动器提供引导型和即用型应用程序，可作为未来开发的起点。

    简单地说，任务应用程序启动器专门用于数据库迁移和分布式测试等用例，而流应用程序启动器则提供与外部系统的集成。

    总的来说，有超过 55 个启动器；有关这两个启动器的更多信息，请查看[这里](https://github.com/spring-attic/spring-cloud-task-app-starters)和[这里](https://docs.spring.io/spring-cloud-stream-app-starters/docs/current/reference/htmlsingle/#starters)的官方文档。

    接下来，我们将构建一个小型分布式 Twitter 应用程序，将 Twitter 帖子流式传输到 Hadoop 分布式文件系统。

2. 设置

    我们将使用消费者密钥和访问令牌创建一个简单的 Twitter 应用程序。

    然后，我们将设置 Hadoop，以便为将来的大数据目的持久化 Twitter 流。

    最后，我们可以选择使用提供的 Spring GitHub 资源库，使用 Maven 编译和组装源-处理器-汇架构模式的独立组件，或者通过 Spring Stream 绑定接口组合源、处理器和汇(sinks)。

    我们将介绍这两种方法。

    值得注意的是，以前所有的流应用启动器都被整理到一个大 repo 中，地址是 [github.com/spring-cloud/spring-cloud-stream-app-starters](https://github.com/spring-cloud/spring-cloud-stream-app-starters/tree/master/hdfs/spring-cloud-starter-stream-sink-hdfs)。每个启动器都已简化并独立出来。

3. Twitter 认证

    首先，我们来设置 Twitter 开发者凭证。要获得 Twitter 开发者证书，请按照 Twitter 官方开发者[文档](https://apps.twitter.com/)中的步骤设置应用程序并创建访问令牌。

    具体来说，我们需要

    - 消费者密钥
    - 消费者密钥秘密
    - 访问令牌秘密
    - 访问令牌

    确保打开该窗口或记下这些信息，因为我们将在下面使用它们！

4. 安装 Hadoop

    接下来，让我们安装 Hadoop！我们可以按照[官方文档](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Installing_Software)或者直接使用 Docker：

    `$ sudo docker run -p 50070:50070 sequenceiq/hadoop-docker:2.4.1`

5. 编译我们的应用程序启动器

    要使用独立的、完全独立的组件，我们可以从 GitHub 存储库中下载并编译所需的 Spring Cloud Stream 应用程序启动器。

    1. Twitter Spring 云流应用程序启动器

        让我们将 Twitter Spring Cloud Stream App Starter（org.springframework.cloud.stream.app.twitterstream.source）添加到项目中：

        `git clone https://github.com/spring-cloud-stream-app-starters/twitter.git`

        然后运行 Maven：

        `./mvnw clean install -PgenerateApps`

        编译后的 Starter App 将出现在本地项目根目录的“/target”中。

        然后，我们可以运行编译后的 .jar 并传入相关应用程序属性，如下所示

        ```shell
        java -jar twitter_stream_source.jar --consumerKey=<CONSUMER_KEY> --consumerSecret=<CONSUMER_SECRET> \
            --accessToken=<ACCESS_TOKEN> --accessTokenSecret=<ACCESS_TOKEN_SECRET>
        ```

        我们还可以使用熟悉的 Spring application.properties 来传递凭据：

        ```properties
        twitter.credentials.access-token=...
        twitter.credentials.access-token-secret=...
        twitter.credentials.consumer-key=...
        twitter.credentials.consumer-secret=...
        ```

    2. HDFS Spring 云流应用程序启动器

        现在（Hadoop 已设置完毕），让我们将 HDFS Spring Cloud Stream App Starter（org.springframework.cloud.stream.app.hdfs.sink）依赖添加到我们的项目中。

        首先，克隆相关的 repo：

        `git clone https://github.com/spring-cloud-stream-app-starters/hdfs.git`

        然后，运行 Maven 作业

        `./mvnw clean install -PgenerateApps`

        编译后的 Starter App 将出现在本地项目根目录的“/target”中。然后，我们可以运行编译后的 .jar 并输入相关应用程序属性：

        `java -jar hdfs-sink.jar --fsUri=hdfs://127.0.0.1:50010/`

        `hdfs://127.0.0.1:50010/` 是 Hadoop 的默认设置，但你的默认 HDFS 端口可能会有所不同，这取决于你如何配置你的实例。

        我们可以在 <http://0.0.0.0:50070> 中看到数据节点的列表（及其当前端口），这里有我们之前传入的配置。

        我们还可以在编译前使用熟悉的 Spring application.properties 来传递凭据，这样就不必总是通过 CLI 来传递了。

        让我们将 application.properties 配置为使用默认的 Hadoop 端口：

        `hdfs.fs-uri=hdfs://127.0.0.1:50010/`

6. 使用 AggregateApplicationBuilder

    另外，我们还可以通过 org.springframework.cloud.stream.aggregate.AggregateApplicationBuilder 将 Spring Stream Source 和 Sink 结合到一个简单的 Spring Boot 应用程序中！

    首先，我们将在 pom.xml 中添加两个流应用程序启动器：

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud.stream.app</groupId>
            <artifactId>spring-cloud-starter-stream-source-twitterstream</artifactId>
            <version>2.1.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud.stream.app</groupId>
            <artifactId>spring-cloud-starter-stream-sink-hdfs</artifactId>
            <version>2.1.2.RELEASE</version>
        </dependency>
    </dependencies>
    ```

    然后，我们将开始合并两个 Stream App Starter 依赖项，把它们封装到各自的子应用中。

    1. 构建应用程序组件

        我们的 SourceApp 指定了要转换或消费的源：

        ```java
        @SpringBootApplication
        @EnableBinding(Source.class)
        @Import(TwitterstreamSourceConfiguration.class)
        public class SourceApp {
            @InboundChannelAdapter(Source.OUTPUT)
            public String timerMessageSource() {
                return new SimpleDateFormat().format(new Date());
            }
        }
        ```

        请注意，我们将 SourceApp 与 org.springframework.cloud.stream.messaging.Source 绑定，并注入相应的配置类，以便从环境属性中获取所需的设置。

        接下来，我们设置一个简单的 org.springframework.cloud.stream.messaging.Processor 绑定：

        ```java
        @SpringBootApplication
        @EnableBinding(Processor.class)
        public class ProcessorApp {
            @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
            public String processMessage(String payload) {
                log.info("Payload received!");
                return payload;
            }
        }
        ```

        然后，我们创建消费者（Sink）：

        ```java
        @SpringBootApplication
        @EnableBinding(Sink.class)
        @Import(HdfsSinkConfiguration.class)
        public class SinkApp {
            @ServiceActivator(inputChannel= Sink.INPUT)
            public void loggerSink(Object payload) {
                log.info("Received: " + payload);
            }
        }
        ```

        在此，我们将 SinkApp 与 org.springframework.cloud.stream.messaging.Sink 绑定，并再次注入正确的配置类以使用指定的 Hadoop 设置。

        最后，我们在 AggregateApp 主方法中使用 AggregateApplicationBuilder 将 SourceApp、ProcessorApp 和 SinkApp 结合起来：

        ```java
        @SpringBootApplication
        public class AggregateApp {
            public static void main(String[] args) {
                new AggregateApplicationBuilder()
                .from(SourceApp.class).args("--fixedDelay=5000")
                .via(ProcessorApp.class)
                .to(SinkApp.class).args("--debug=true")
                .run(args);
            }
        }
        ```

        与任何 Spring Boot 应用程序一样，我们可以通过 application.properties 或编程方式将指定设置作为环境属性注入。

        由于我们使用的是 Spring Stream 框架，因此还可以将参数传递 AggregateApplicationBuilder 构造函数中。

    2. 运行完成的应用程序

        然后，我们可以使用以下命令行指令编译并运行应用程序：

        ```shell
            mvn install
            java -jar twitterhdfs.jar
        ```

        请记住，每个 @SpringBootApplication 类都要放在一个单独的包中（否则会抛出多个不同的绑定异常）！关于如何使用 AggregateApplicationBuilder 的更多信息，请参阅[官方文档](https://github.com/spring-cloud/spring-cloud-stream-samples/)。

        编译并运行应用程序后，我们应该在控制台中看到如下内容（当然，内容会因 Tweet 而异）：

        ```log
        2018-01-15 04:38:32.255  INFO 28778 --- [itterSource-1-1] 
        c.b.twitterhdfs.processor.ProcessorApp   : Payload received!
        2018-01-15 04:38:32.255  INFO 28778 --- [itterSource-1-1] 
        com.baeldung.twitterhdfs.sink.SinkApp    : Received: {"created_at":
        "Mon Jan 15 04:38:32 +0000 2018","id":952761898239385601,"id_str":
        "952761898239385601","text":"RT @mighty_jimin: 180114 ...
        ```

        这些演示了处理器和汇接收源数据时的正确操作！在这个示例中，我们没有配置 HDFS Sink 做太多事情--它只会打印信息 "Payload received!"

7. 结论

    在本教程中，我们已经学会了如何将两个超棒的 Spring Stream 应用程序启动器组合成一个贴心的 Spring Boot 示例！

    这里还有一些关于 [Spring Boot 启动程序](https://www.baeldung.com/spring-boot-starters) 以及[如何创建自定义启动程序](https://www.baeldung.com/spring-boot-custom-starter)的精彩官方文章！
