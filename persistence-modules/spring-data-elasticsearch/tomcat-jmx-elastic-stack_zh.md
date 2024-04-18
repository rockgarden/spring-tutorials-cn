# [将 JMX 数据导入弹性堆栈 (ELK)](https://www.baeldung.com/tomcat-jmx-elastic-stack)

1. 概述

    在本快速教程中，我们将了解如何将 JMX 数据从 Tomcat 服务器发送到 Elastic Stack（以前称为 ELK）。

    我们将讨论如何配置 Logstash 从 JMX 读取数据并将其发送到 Elasticsearch。

2. 安装弹性堆栈

    首先，我们需要安装 Elastic 栈（[Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/current/install-elasticsearch.html) - [Logstash](https://www.elastic.co/guide/en/logstash/current/installing-logstash.html) - [Kibana](https://www.elastic.co/guide/en/kibana/current/install.html)）

    然后，为了确保一切连接和工作正常，我们将把 JMX 数据发送到 Logstash，并在 Kibana 上对其进行可视化。

    1. 测试 Logstash

        首先，我们将进入 Logstash 的安装目录，该目录因操作系统（本例中为 Ubuntu）而异：

        `cd /opt/logstash`

        我们可以在命令行中为 Logstash 设置一个简单的配置：

        `bin/logstash -e 'input { stdin { } } output { elasticsearch { hosts => ["localhost:9200"] } }'`

        然后，我们只需在控制台中键入一些示例数据，完成后使用 CTRL-D 命令关闭管道即可。

    2. 测试 Elasticsearch

        添加示例数据后，Elasticsearch 上的 Logstash 索引就应该可用了，我们可以按如下步骤进行检查：

        `curl -X GET 'http://localhost:9200/_cat/indices'`

        样本输出：

        ```log
        yellow open logstash-2017.11.10 5 1 3531 0 506.3kb 506.3kb 
        yellow open .kibana             1 1    3 0   9.5kb   9.5kb 
        yellow open logstash-2017.11.11 5 1 8671 0   1.4mb   1.4mb
        ```

    3. 测试 Kibana

        Kibana 默认运行于 5601 端口 - 我们可以访问主页：

        <http://localhost:5601/app/kibana>

        我们应该能够以 "logstash-*" 模式创建一个新索引，并在其中看到我们的示例数据。

3. 配置 Tomcat

    接下来，我们需要在 CATALINA_OPTS 中添加以下内容来启用 JMX：

    ```yml
    -Dcom.sun.management.jmxremote
    -Dcom.sun.management.jmxremote.port=9000
    -Dcom.sun.management.jmxremote.ssl=false
    -Dcom.sun.management.jmxremote.authenticate=false
    ```

    请注意：

    - 您可以通过修改 setenv.sh 配置 CATALINA_OPTS
    - 对于 Ubuntu 用户，setenv.sh 可在"/usr/share/tomcat8/bin" 中找到。

4. 连接 JMX 和 Logstash

    现在，让我们将 JMX 指标连接到 Logstash，为此我们需要安装 JMX 输入插件（稍后详述）。

    1. 配置 JMX 指标

        首先，我们需要配置要存储的 JMX 指标；我们将以 JSON 格式提供配置。

        下面是我们的 jmx_config.json：

        ```json
        {
        "host" : "localhost",
        "port" : 9000,
        "alias" : "reddit.jmx.elasticsearch",
        "queries" : [
        {
            "object_name" : "java.lang:type=Memory",
            "object_alias" : "Memory"
        }, {
            "object_name" : "java.lang:type=Threading",
            "object_alias" : "Threading"
        }, {
            "object_name" : "java.lang:type=Runtime",
            "attributes" : [ "Uptime", "StartTime" ],
            "object_alias" : "Runtime"
        }]
        }
        ```

        请注意：

        - 我们使用了与 CATALINA_OPTS 相同的 JMX 端口
        - 我们可以提供任意数量的配置文件，但需要将它们保存在同一目录下（在我们的例子中，我们将 jmx_config.json 保存在"/monitor/jmx/"目录下）。

    2. JMX 输入插件

        接下来，让我们在 Logstash 安装目录下运行以下命令来安装 JMX 输入插件：

        `bin/logstash-plugin install logstash-input-jmx`

        然后，我们需要创建一个 Logstash 配置文件 (jmx.conf)，其中输入为 JMX 指标，输出指向 Elasticsearch：

        ```conf
        input {
        jmx {
            path => "/monitor/jmx"
            polling_frequency => 60
            type => "jmx"
            nb_thread => 3
        }
        }

        output {
            elasticsearch {
                hosts => [ "localhost:9200" ]
            }
        }
        ```

        最后，我们需要运行 Logstash 并指定配置文件：

        `bin/logstash -f jmx.conf`

        请注意，我们的 Logstash 配置文件 jmx.conf 保存在 Logstash 的主目录中（在我们的例子中为 /opt/logstash）

5. 可视化 JMX 指标

    最后，让我们在 Kibana 上创建一个简单的 JMX 指标数据可视化。我们将创建一个简单的图表来监控堆内存的使用情况。

    1. 创建新的搜索

        首先，我们将创建一个新的搜索，以获取与堆内存使用相关的指标：

        - 点击搜索栏中的 "New Search" 图标
        - 键入以下查询
        `metric_path:reddit.jmx.elasticsearch.Memory.HeapMemoryUsage.used`
        - 按回车键
        - 确保添加侧边栏中的 "metric_path" 和 "metric_value_number" 字段
        - 点击搜索栏中的 "Save Search" 图标
        - 将搜索命名为 "used memory"

        如果侧边栏中有任何字段标记为未索引，请转到 "Settings" 选项卡并刷新 "logstash-*"索引中的字段列表。

    2. 创建折线图

        接下来，我们将创建一个简单的折线图来监控堆内存的使用情况：

        - 转到 "Visualize" 选项卡
        - 选择 "‘Line Chart"
        - 选择 "From saved search"
        - 选择我们之前创建的 "used memory" 搜索

        对于 Y 轴，确保选择

        Aggregation: Average
        Field: metric_value_number

        对于 X 轴，选择 "Date Histogram"，然后保存可视化。

    3. 使用脚本字段

        由于内存使用量以字节为单位，因此可读性不高。我们可以通过在 Kibana 中添加脚本字段来转换度量类型和值：

        - 从 "Settings" 转到索引，选择 "logstash-*"索引
        - 转到 "Scripted fields" 选项卡，点击 "Add Scripted Field"
        - 名称：metric_value_formatted
        - 格式：Bytes
        - 对于脚本，我们只需使用 "metric_value_number" 的值：
        `doc['metric_value_number'].value`

        现在，您可以将搜索和可视化改为使用字段 "metric_value_formatted"，而不是 "metric_value_number"，这样数据就能正确显示了。

6. 结论

    我们完成了。正如你所看到的，配置并不特别困难，而让 JMX 数据在 Kibana 中可见，可以让我们做很多有趣的可视化工作，创建一个美妙的生产监控仪表盘。
