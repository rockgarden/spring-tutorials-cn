# [将操作系统数据发送到弹性堆栈（ELK 堆栈）](https://www.baeldung.com/ops/os-data-into-elastic-stack)

[日志记录](https://www.baeldung.com/ops/category/logging)

1. 概述

    在本快速教程中，我们将讨论如何将操作系统级指标发送到 Elastic Stack。作为参考，我们将使用 Ubuntu 服务器。

    我们将使用 Metricbeat 从操作系统收集数据，并定期将其发送到 Elasticsearch。

2. 安装 Metricbeat

    首先，我们需要在 Ubuntu 机器上下载并安装标准的 [Metricbeat](https://www.elastic.co/guide/en/beats/metricbeat/current/metricbeat-installation.html) 代理：

    ```bash
    curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-6.0.1-amd64.deb
    sudo dpkg -i metricbeat-6.0.1-amd64.deb
    ```

    安装完成后，我们需要修改位于"/etc/metricbeat/"（Ubuntu）的 metricbeat.yml，配置 Metricbeat 向 Elasticsearch 发送数据：

    ```yml
    output.elasticsearch:
    hosts: ["localhost:9200"]
    ```

    然后，我们可以通过修改 /etc/metricbeat/modules.d/system.yml 来定制要跟踪的指标：

    ```yml
    - module: system
    period: 10s
    metricsets:
        - cpu
        - load
        - memory
        - network
        - process
        - process_summary
    ```

    最后，我们将启动 Metricbeat 服务：

    `sudo service metricbeat start`

3. 快速检查

    为了确保 Metricbeat 正在向 Elasticsearch 发送数据，请对索引进行快速检查：

    `curl -X GET 'http://localhost:9200/_cat/indices'`

    下面是你应该得到的结果：

    `yellow open metricbeat-6.0.1-2017.12.11 1 1  2185 0   1.7mb   1.7mb`

    现在，我们将从 "Settings" 选项卡创建新索引，模式为 "metricbeat-*" 4.

4. 可视化操作系统指标

    现在，我们将可视化随时间变化的内存使用情况。

    首先，我们将在 "metricbeat-*" 索引上创建一个新的搜索，以 "System Memory" 为名，使用以下查询来分离内存指标：

    `metricset.name:memory`

    最后，我们可以创建一个简单的内存数据可视化：

    - 导航至 "Visualize" 选项卡
    - 选择 "Line Chart"
    - 选择 "From Saved Search"
    - 选择我们刚刚创建的 "System Memory"搜索

    为 Y 轴选择

    - Aggregation：Average
    - Field：system.memory.used.pct

    对于 X 轴，选择Aggregation（聚合）：Date Histogram

5. 结论

    在这篇简明扼要的文章中，我们学会了如何使用 Metricbeat 向 Elastic Stack 实例发送操作系统级数据。
