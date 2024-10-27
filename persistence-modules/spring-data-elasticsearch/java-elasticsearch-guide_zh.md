# Elasticsearch

[Tag: Elasticsearch](https://www.baeldung.com/tag/elasticsearch)

- [什么是Elasticsearch？](java-elasticsearch_zh.md)
- [Java中的Elasticsearch指南](elasticsearch-java_zh.md)
- [Elasticsearch中的分片和副本](java-shards-replicas-elasticsearch_zh.md)
- [使用Elasticsearch的简单标签实现](elasticsearch-tagging_zh.md)
- ~~[Jest - Elasticsearch Java 客户端](../elasticsearch/elasticsearch-jest_zh.md)~~

- [Spring Data Elasticsearch 简介](spring-data-elasticsearch-tutorial_zh.md)
- [使用 Spring Data 的 Elasticsearch 查询](spring-data-elasticsearch-queries_zh.md)

- [使用ElasticSearch进行全文搜索的快速入门](elasticsearch-full-text-search-rest-api_zh.md)
- [ElasticSearch中的地理空间支持](elasticsearch-geo-spatial_zh.md)

- [将JMX数据发送到弹性堆栈 (ELK)](tomcat-jmx-elastic-stack_zh.md)
- [将Java应用程序的日志发送到弹性堆栈（ELK）](java-application-logs-to-elastic-stack_zh.md)
- [将操作系统数据发送到弹性堆栈（ELK堆栈）](os-data-into-elastic-stack_zh.md)

## 使用 Docker 部署 Elasticsearch 单节点群集

1. 安装 Docker。访问 Get Docker 为你的环境安装 Docker。

    如果使用 Docker Desktop，请确保分配至少 4GB 内存。你可以在 Docker Desktop 的 **Settings > Resources** 中调整内存使用量。

2. 创建一个新的 docker 网络。

    `docker network create elastic`

3. 拉取 Elasticsearch Docker 镜像。

    `docker pull docker.elastic.co/elasticsearch/elasticsearch:8.15.3`

4. 可选：为你的环境安装 Cosign。然后使用 Cosign 验证 Elasticsearch 映像的签名。

    ```bash
    wget https://artifacts.elastic.co/cosign.pub
    cosign verify --key cosign.pub docker.elastic.co/elasticsearch/elasticsearch:8.15.3
    ```

    cosign 命令会以 JSON 格式打印检查结果和签名有效载荷：

    ```bash
    Verification for docker.elastic.co/elasticsearch/elasticsearch:8.15.3 --
    The following checks were performed on each of these signatures:
    - The cosign claims were validated
    - Existence of the claims in the transparency log was verified offline
    - The signatures were verified against the specified public key
    ```

5. 启动 Elasticsearch 容器。

    `docker run --name es01 --net elastic -p 9200:9200 -it -m 1GB docker.elastic.co/elasticsearch/elasticsearch:8.15.3`

    > 使用 -m 标志为容器设置内存限制。这样就不需要手动设置 JVM 大小了。

    该命令将打印 Kibana 的 elastic 用户密码和注册令牌。

    ```log
    ✅ Elasticsearch security features have been automatically configured!
    ✅ Authentication is enabled and cluster connections are encrypted.

    ℹ️  Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
    sFvtzoOI*gXn*=PqggG=

    ℹ️  HTTP CA certificate SHA-256 fingerprint:
    01596c779bdac3f81c5c262a2bf3f2b7f17b4f57227458a9e610bb7fa5f39a79

    ℹ️  Configure Kibana to use this cluster:
    • Run Kibana and click the configuration link in the terminal when Kibana starts.
    • Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
    eyJ2ZXIiOiI4LjE0LjAiLCJhZHIiOlsiMTcyLjE5LjAuMjo5MjAwIl0sImZnciI6IjAxNTk2Yzc3OWJkYWMzZjgxYzVjMjYyYTJiZjNmMmI3ZjE3YjRmNTcyMjc0NThhOWU2MTBiYjdmYTVmMzlhNzkiLCJrZXkiOiJ0Qjl5ekpJQk03MVhnbTI2TEN4eDpFazBMeExibVRTQ1ctdy14WTB1ZHJRIn0=

    ℹ️ Configure other nodes to join this cluster:
    • Copy the following enrollment token and start new Elasticsearch nodes with `bin/elasticsearch --enrollment-token <token>` (valid for the next 30 minutes):
    eyJ2ZXIiOiI4LjE0LjAiLCJhZHIiOlsiMTcyLjE5LjAuMjo5MjAwIl0sImZnciI6IjAxNTk2Yzc3OWJkYWMzZjgxYzVjMjYyYTJiZjNmMmI3ZjE3YjRmNTcyMjc0NThhOWU2MTBiYjdmYTVmMzlhNzkiLCJrZXkiOiJzeDl5ekpJQk03MVhnbTI2TEN4eDpPeVVQay1CR1RBT1BCNUNUVWl3N2JRIn0=

    If you're running in Docker, copy the enrollment token and run:
    `docker run -e "ENROLLMENT_TOKEN=<token>" docker.elastic.co/elasticsearch/elasticsearch:8.15.3`
    ```

6. 复制生成的elastic密码和注册令牌。这些凭证只会在首次启动 Elasticsearch 时显示。你可以使用以下命令重新生成凭据。

    ```bash
    docker exec -it es01 /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic
    docker exec -it es01 /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token -s kibana
    ```

    我们建议将elastic密码作为环境变量存储在 shell 中。例如：

    `export ELASTIC_PASSWORD="sFvtzoOI*gXn*=PqggG="`

7. 将 http_ca.crt SSL 证书从容器复制到本地计算机。

    `docker cp es01:/usr/share/elasticsearch/config/certs/http_ca.crt .`

8. 对 Elasticsearch 进行 REST API 调用，以确保 Elasticsearch 容器正在运行。

    `curl --cacert http_ca.crt -u elastic:$ELASTIC_PASSWORD https://localhost:9200`

    > http_ca.crt 最好通过完整路径访问，否则要在相同路径下执行
