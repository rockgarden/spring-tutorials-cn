# [Java Elasticsearch 指南](https://www.baeldung.com/elasticsearch-java)

1. 概述

    在本文中，我们将深入探讨与全文搜索引擎相关的一些关键概念，重点是 Elasticsearch。

    由于这是一篇面向 Java 的文章，因此我们不会详细介绍如何分步设置 Elasticsearch，也不会展示它在引擎盖下是如何工作的。相反，我们将以 [Java 客户端](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html)为目标，学习如何使用索引、删除、获取和搜索等主要功能。

2. 设置

    为了简单起见，我们将使用一个 docker 镜像，为我们的 Elasticsearch 实例创建一个没有身份验证的 9200 端口。另外，请确保正确[配置](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/connecting.html) Java 客户端，尤其是在 Elasticsearch 需要身份验证的情况下。

    我们先启动 Elasticsearch 实例：

    `docker run -d --name elastic-test -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.8.2`

    默认情况下，Elasticsearch 通过 9200 端口监听 HTTP 查询。我们可以在自己喜欢的浏览器中打开 <http://localhost:9200/> URL，验证它是否已成功启动：

    ```json
    {
    "name" : "739190191b07",
    "cluster_name" : "docker-cluster",
    "cluster_uuid" : "_tUFwsigQW2FKhm_9yLiFQ",
    "version" : {
        "number" : "8.7.2",
        "build_flavor" : "default",
        "build_type" : "docker",
        "build_hash" : "f229ed3f893a515d590d0f39b05f68913e2d9b53",
        "build_date" : "2023-04-27T04:33:42.127815583Z",
        "build_snapshot" : false,
        "lucene_version" : "9.6.0",
        "minimum_wire_compatibility_version" : "7.17.0",
        "minimum_index_compatibility_version" : "7.0.0"
    },
    "tagline" : "You Know, for Search"
    }
    ```

3. Maven 配置

    现在，我们的主 Elasticsearch 集群已经启动并运行，让我们直接进入 Java 客户端。

    首先，我们需要在 pom.xml 文件中添加 Elasticsearch 和 Jackson 库：

    ```xml
    <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>8.9.0</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.16.0</version>
    </dependency>
    ```

    确保使用这些库的最新版本。

4. Elasticsearch Java 客户端

    让我们在项目中设置 Elasticsearch Java 客户端：

    ```java
    RestClient restClient = RestClient
    .builder(HttpHost.create("http://localhost:9200"))
    .build();
    ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    ElasticsearchClient client = new ElasticsearchClient(transport);
    ```

    现在，我们已经准备好与 Elasticsearch 交互了。接下来，我们将检查如何执行最常见的操作，例如为文档建立索引、从索引中删除文档以及在索引中搜索文档。

    1. 编制文档索引

        首先，我们要将数据添加到 Elastic 中，使其具有可搜索性。为此，我们将使用 ElasticseachClient 上的 .index() 方法：

        ```java
        Person person = new Person(20, "Mark Doe", new Date(1471466076564L));
        IndexResponse response = client.index(i -> i
        .index("person")
        .id(person.getFullName())
        .document(person));
        ```

        以上，我们实例化了一个简单的 Java 对象，并将其保存在名为 person 的索引中。此外，我们不必将 Java 对象转换为 JSON 表示形式，因为客户端将使用 JacksonJsonpMapper 来完成转换。

        我们可以进一步检查返回的 IndexReponse，看看对象是否被 Elastic 正确摄取：

        ```java
        log.info("Indexed with version: {}", response.version());
        assertEquals(Result.Created, response.result());
        assertEquals("person", response.index());
        assertEquals("Mark Doe", response.id());
        ```

        Elastic 中的所有数据条目都有一个版本。如果我们更新一个对象，它就会有一个不同的版本。

        此外，我们还可以用同样的方式直接向 Elastic 发送 JSON 字符串：

        ```java
        String jsonString = "{\"age\":10,\"dateOfBirth\":1471466076564,\"fullName\":\"John Doe\"}";
        StringReader stringReader = new StringReader(jsonString);
        IndexResponse response = client.index(i -> i
        .index("person")
        .id("John Doe")
        .withJson(stringReader));
        ```

        我们需要将 JSON 字符串转换为 StringReader 或 InputStream 对象，以便与 .withJson() 方法一起使用。

    2. 查询索引文档

        一旦我们在 Elastic 中获得了一些索引文档，我们就可以使用 .search() 方法对其进行搜索：

        ```java
        String searchText = "John";
        SearchResponse<Person> searchResponse = client.search(s -> s
        .index("person")
        .query(q -> q
            .match(t -> t
            .field("fullName")
            .query(searchText))), Person.class);
        List<Hit<Person>> hits = searchResponse.hits().hits();
        assertEquals(1, hits.size());
        assertEquals("John Doe", hits.get(0).source().getFullName());
        ```

        .search() 方法的结果是一个包含点击数的 SearchResponse。我们可以先从 SearchResponse 对象中获取 HitsMetadata，然后再次调用 .hits() 方法来获取与搜索请求匹配的所有人员对象的列表。

        我们可以通过添加额外参数来增强请求，从而自定义查询连接查询构建器：

        ```java
        SearchResponse<Person> searchResponse = client.search(s -> s
        .index("person")
        .query(q -> q
            .match(t -> t
            .field("fullName").query(searchText)))
        .query(q -> q
            .range(range -> range
            .field("age").from("1").to("10"))),Person.class);
        ```

    3. 按 ID 检索和删除单个文档

        给定单个文档的 id 后，我们要先获取它，然后删除它。例如，我们使用 .get() 来获取文档：

        ```java
        String documentId = "John Doe";
        GetResponse<Person> getResponse = client.get(s -> s
        .index("person")
        .id(documentId), Person.class);
        Person source = getResponse.source();
        assertEquals("John Doe", source.getFullName());
        ```

        然后，我们使用 .delete() 方法从索引中删除文档：

        ```java
        String documentId = "Mark Doe";
        DeleteResponse response = client.delete(i -> i
        .index("person")
        .id(documentId));
        assertEquals(Result.Deleted, response.result());
        assertEquals("Mark Doe", response.id());
        ```

        语法简单明了，您必须在指定对象 id 的同时指定索引。

5. 复杂搜索查询示例

    Elasticsearch Java 客户端库非常灵活，提供了多种[查询构建器](https://artifacts.elastic.co/javadoc/co/elastic/clients/elasticsearch-java/8.1.0/co/elastic/clients/elasticsearch/_types/query_dsl/QueryBuilders.html)来搜索集群中的特定条目。在使用 .search() 方法查找文件时，我们可以使用 RangeQuery 来匹配字段值在特定范围内的文件：

    ```java
    Query ageQuery = RangeQuery.of(r -> r.field("age").from("5").to("15"))._toQuery();
    SearchResponse<Person> response1 = client.search(s -> s.query(q -> q.bool(b -> b
    .must(ageQuery))), Person.class);
    response1.hits().hits().forEach(hit -> log.info("Response 1: {}", hit.source()));
    ```

    .matchQuery() 方法会返回与所提供字段的值相匹配的所有文档：

    ```java
    Query fullNameQuery = MatchQuery.of(m -> m.field("fullName").query("John"))._toQuery();
    SearchResponse<Person> response2 = client.search(s -> s.query(q -> q.bool(b -> b
    .must(fullNameQuery))), Person.class);
    response2.hits().hits().forEach(hit -> log.info("Response 2: {}", hit.source()));
    ```

    我们还可以在查询中使用 regex 和通配符：

    ```java
    Query doeContainsQuery = SimpleQueryStringQuery.of(q -> q.query("*Doe"))._toQuery();
    SearchResponse<Person> response3 = client.search(s -> s.query(q -> q.bool(b -> b
    .must(doeContainsQuery))), Person.class);
    response3.hits().hits().forEach(hit -> log.info("Response 3: {}", hit.source()));
    ```

    尽管我们可以在查询中使用通配符和 regex，但我们必须考虑每个请求的性能和内存消耗。此外，如果大量使用通配符，响应时间可能会缩短。

    此外，如果你更熟悉 Lucene 查询语法，可以使用 SimpleQueryStringQuery 生成器自定义搜索查询：

    ```java
    Query simpleStringQuery = SimpleQueryStringQuery.of(q -> q.query("+John -Doe OR Janette"))._toQuery();
    SearchResponse<Person> response4 = client.search(s -> s.query(q -> q.bool(b -> b
    .must(simpleStringQuery))), Person.class);
    response4.hits().hits().forEach(hit -> log.info("Response 4: {}", hit.source()));
    ```

    此外，我们还可以使用 Lucene 的查询解析器语法来构建简单但功能强大的查询。例如，下面是一些基本的操作符，可与 AND/OR/NOT 操作符一起用于构建搜索查询：

    - 必填操作符 (+)：要求文档字段中的某处存在特定文本。
    - 禁止运算符 (-)：排除所有包含在 (-) 符号后声明的关键字的文档。

6. 结论

    在本文中，我们已经了解了如何使用 Elasticsearch 的 Java API 来执行与全文搜索引擎相关的标准功能。
