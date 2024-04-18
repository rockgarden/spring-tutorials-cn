# [将 Java 应用程序的日志发送到弹性堆栈 (ELK)](https://www.baeldung.com/java-application-logs-to-elastic-stack)

1. 概述

    在本快速教程中，我们将逐步讨论如何将应用程序日志发送到 Elastic Stack (ELK)。

2. 配置日志回传

    让我们从配置 Logback 开始，使用 FileAppender 将应用程序日志写入文件：

    ```xml
    <appender name="STASH" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logback/redditApp.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logback/redditApp.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>  
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="DEBUG">
        <appender-ref ref="STASH" />        
    </root>
    ```

    请注意：

    - 我们通过使用 RollingFileAppender 和 TimeBasedRollingPolicy（基于时间的滚动策略）将每天的日志保存在一个单独的文件中（有关该应用程序的更多信息，请参见[此处](https://www.baeldung.com/java-logging-rolling-file-appenders)）
    - 通过将 maxHistory 设置为 7，我们只保留一周（7 天）的旧日志。

    此外，请注意我们是如何使用 [LogstashEncoder](https://github.com/logstash/logstash-logback-encoder) 将日志编码为 JSON 格式的--这在 Logstash 中更容易使用。

    要使用该编码器，我们需要在 pom.xml 中添加以下依赖项：

    ```xml
    <dependency> 
        <groupId>net.logstash.logback</groupId> 
        <artifactId>logstash-logback-encoder</artifactId> 
        <version>4.11</version> 
    </dependency>
    ```

    最后，确保应用程序有访问日志目录的权限：

    `sudo chmod a+rwx /var/lib/tomcat8/logback`

3. 配置 Logstash

    现在，我们需要配置 Logstash，以便从应用程序创建的日志文件中读取数据并将其发送到 ElasticSearch。

    下面是我们的配置文件 logback.conf：

    ```conf
    input {
        file {
            path => "/var/lib/tomcat8/logback/*.log"
            codec => "json"
            type => "logback"
        }
    }

    output {
        if [type]=="logback" {
            elasticsearch {
                hosts => [ "localhost:9200" ]
                index => "logback-%{+YYYY.MM.dd}"
            }
        }
    }
    ```

    请注意：

    - 输入文件被使用，因为 Logstash 这次将从日志文件中读取日志
    - 路径设置为日志目录，所有扩展名为 .log 的文件都将被处理
    - 索引设置为新索引 "logback-%{+YYYY.MM.dd}"，而不是默认的 "logstash-%{+YYYY.MM.dd}"。

    使用新配置运行 Logstash 时，我们将使用

    `bin/logstash -f logback.conf`

4. 使用 Kibana 可视化日志

    现在，我们可以在 "logback-*" 索引中查看 Logback 数据。

    我们将创建一个新的搜索 "Logback logs"，确保通过使用以下查询将 Logback 数据分开：

    `type:logback`

    最后，我们可以创建一个简单的 Logback 数据可视化：

    导航至 "Visualize" 选项卡
    选择 "Vertical Bar Chart"
    选择 "From Saved Search"
    选择我们刚刚创建的 "Logback logs" 搜索
    对于 Y 轴，确保选择Aggregation（聚合）：Count

    对于 X 轴，选择

    - Aggregation: Terms
    - Field: level

    运行可视化后，您会看到多个条形图表示每个级别（DEBUG、INFO、ERROR...）的日志计数。

5. 结论

    在本文中，我们学习了在系统中设置 Logstash 以将其生成的日志数据推送到 Elasticsearch 的基础知识，并在 Kibana 的帮助下对这些数据进行了可视化。
