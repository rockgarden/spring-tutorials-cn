# [Jest - Elasticsearch Java 客户端](https://www.baeldung.com/elasticsearch-jest)

1. 简介

    使用过 Elasticsearch 的人都知道，使用其 RESTful 搜索 API 构建查询既繁琐又容易出错。

    在本教程中，我们将介绍 Elasticsearch 的 HTTP Java 客户端 [Jest](https://github.com/searchbox-io/Jest)。虽然 Elasticsearch 提供了自己的本地 Java 客户端，但 Jest 提供了更流畅的 API 和更易于使用的接口。

2. Maven 依赖

    我们首先要做的是将 Jest 库导入 POM：

    ```xml
    <dependency>
        <groupId>io.searchbox</groupId>
        <artifactId>jest</artifactId>
        <version>6.3.1</version>
    </dependency>
    ```

    Jest 的版本与 Elasticsearch 主产品的版本一致。这有助于确保客户端和服务器之间的兼容性。

    通过包含 Jest 依赖关系，相应的 Elasticsearch 库也将作为传递依赖关系包含在内。

3. 使用 Jest 客户端

    在本节中，我们将了解如何使用 Jest 客户端来执行 Elasticsearch 的常见任务。

    要使用 Jest 客户端，我们只需使用 JestClientFactory 创建一个 JestClient 对象。这些对象的创建成本很高，而且是线程安全的，因此我们将创建一个可以在整个应用程序中共享的单例：

    ```java
    public JestClient jestClient() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(
        new HttpClientConfig.Builder("http://localhost:9200")
            .multiThreaded(true)
            .defaultMaxTotalConnectionPerRoute(2)
            .maxTotalConnection(10)
            .build());
        return factory.getObject();
    }
    ```

    这将创建一个连接到本地运行的 Elasticsearch 客户端的 Jest 客户端。虽然这个连接示例微不足道，但 Jest 也完全支持代理、SSL、身份验证甚至节点发现。

    JestClient 类是通用类，只有少数几个公共方法。我们要用到的主要方法是 execute，它需要一个 Action 接口的实例。Jest 客户端提供了多个构建器类，帮助创建与 Elasticsearch 交互的不同操作。

    所有 Jest 调用的结果都是一个 JestResult 实例。我们可以调用 isSucceeded 来检查是否成功。对于不成功的操作，我们可以调用 getErrorMessage 获取更多细节：

    ```java
    JestResult jestResult = jestClient.execute(new Delete.Builder("1").index("employees").build());
    if (jestResult.isSucceeded()) {
        System.out.println("Success!");
    } else {
        System.out.println("Error: " + jestResult.getErrorMessage());
    }
    ```

    1. 管理索引

        要检查索引是否存在，我们使用 IndicesExists 操作：

        `JestResult result = jestClient.execute(new IndicesExists.Builder("employees").build())`

        要创建索引，我们使用 CreateIndex 操作：

        `jestClient.execute(new CreateIndex.Builder("employees").build());`

        这将以默认设置创建一个索引。我们可以在创建索引时覆盖特定设置：

        ```java
        Map<String, Object> settings = new HashMap<>();
        settings.put("number_of_shards", 11);
        settings.put("number_of_replicas", 2);
        jestClient.execute(new CreateIndex.Builder("employees").settings(settings).build());
        ```

        使用 ModifyAliases 动作创建或更改别名也很简单：

        ```java
        jestClient.execute(new ModifyAliases.Builder(
        new AddAliasMapping.Builder("employees", "e").build()).build());
        jestClient.execute(new ModifyAliases.Builder(
        new RemoveAliasMapping.Builder("employees", "e").build()).build());
        ```

    2. 创建文档

        使用 Index 操作类，Jest 客户端可以轻松索引或创建新文档。Elasticsearch 中的文档只是 JSON 数据，有多种方法可将 JSON 数据传递给 Jest 客户端以编制索引。

        在本例中，我们使用一个假想的雇员文档：

        ```json
        {
            "name": "Michael Pratt",
            "title": "Java Developer",
            "skills": ["java", "spring", "elasticsearch"],
            "yearsOfService": 2
        }
        ```

        表示 JSON 文档的第一种方法是使用 Java 字符串。虽然我们可以手动创建 JSON 字符串，但必须注意正确的格式、大括号和转义引号字符。

        因此，使用 JSON 库（如 Jackson）来构建 JSON 结构，然后转换为字符串会更方便：

        ```java
        ObjectMapper mapper = new ObjectMapper();
        JsonNode employeeJsonNode = mapper.createObjectNode()
        .put("name", "Michael Pratt")
        .put("title", "Java Developer")
        .put("yearsOfService", 2)
        .set("skills", mapper.createArrayNode()
            .add("java")
            .add("spring")
            .add("elasticsearch"));
        jestClient.execute(new Index.Builder(employeeJsonNode.toString()).index("employees").build());
        ```

        我们还可以使用 Java Map 来表示 JSON 数据，并将其传递给 Index 操作：

        ```java
        Map<String, Object> employeeHashMap = new LinkedHashMap<>();
        employeeHashMap.put("name", "Michael Pratt");
        employeeHashMap.put("title", "Java Developer");
        employeeHashMap.put("yearsOfService", 2);
        employeeHashMap.put("skills", Arrays.asList("java", "spring", "elasticsearch"));
        jestClient.execute(new Index.Builder(employeeHashMap).index("employees").build());
        ```

        最后，Jest 客户端可以接受任何表示要索引的文档的 POJO。比方说，我们有一个 Employee 类：

        ```java
        public class Employee {
            String name;
            String title;
            List<String> skills;
            int yearsOfService;
        }
        ```

        我们可以直接将该类的实例传递给索引生成器：

        ```java
        Employee employee = new Employee();
        employee.setName("Michael Pratt");
        employee.setTitle("Java Developer");
        employee.setYearsOfService(2);
        employee.setSkills(Arrays.asList("java", "spring", "elasticsearch"));
        jestClient.execute(new Index.Builder(employee).index("employees").build());
        ```

    3. 读取文档

        使用 Jest 客户端从 Elasticsearch 访问文档有两种主要方法。首先，如果我们知道文档 ID，就可以直接使用 Get 操作访问它：

        `jestClient.execute(new Get.Builder("employees", "17").build());`

        要访问返回的文档，我们必须调用各种 getSource 方法之一。我们可以获取原始 JSON 格式的结果，也可以将其反序列化为 DTO：

        ```java
        Employee getResult = jestClient.execute(new Get.Builder("employees", "1").build())
            .getSourceAsObject(Employee.class);
        ```

        访问文档的另一种方式是使用搜索查询，这在 Jest 中是通过搜索操作实现的。

        Jest 客户端支持完整的 Elasticsearch 查询 DSL。就像索引操作一样，查询也是以 JSON 文档的形式表达的，而且有多种方式来执行搜索。

        首先，我们可以传递一个表示搜索查询的 JSON 字符串。需要提醒的是，我们必须确保字符串已正确转义，并且是有效的 JSON：

        ```java
        String search = "{" +
        "  \"query\": {" +
        "    \"bool\": {" +
        "      \"must\": [" +
        "        { \"match\": { \"name\":   \"Michael Pratt\" }}" +
        "      ]" +
        "    }" +
        "  }" +
        "}";
        jestClient.execute(new Search.Builder(search).build());
        ```

        与上面的索引操作一样，我们可以使用 Jackson 等库构建 JSON 查询字符串。

        此外，我们还可以使用本地 Elasticsearch 查询操作 API。这样做的一个缺点是，我们的应用程序必须依赖于完整的 [Elasticsearch 库](https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch)。

        通过搜索操作，可以使用 getSource 方法访问匹配的文档。不过，Jest 还提供了 Hit 类，该类封装了匹配文档并提供了有关结果的元数据。使用 Hit 类，我们可以访问每个结果的附加元数据：分数、路由和解释结果等：

        ```java
        List<SearchResult.Hit<Employee, Void>> searchResults = 
        jestClient.execute(new Search.Builder(search).build())
            .getHits(Employee.class);
        searchResults.forEach(hit -> {
            System.out.println(String.format("Document %s has score %s", hit.id, hit.score));
        });
        ```

    4. 更新文档

        Jest 为更新文档提供了一个简单的 Update 操作：

        ```java
        employee.setYearOfService(3);
        jestClient.execute(new Update.Builder(employee).index("employees").id("1").build());
        ```

        它与我们前面看到的索引操作接受相同的 JSON 表示形式，因此很容易在这两个操作之间共享代码。

        3.5. 删除文档

        从索引中删除文档可使用 Delete 操作完成。它只需要索引名称和文档 ID：

        ```java
        jestClient.execute(new Delete.Builder("17")
        .index("employees")
        .build());
        ```

4. 批量操作

    Jest 客户端还支持批量操作(Bulk Operations)。这意味着我们可以通过同时发送多个操作来节省时间和带宽。

    使用批量操作，我们可以将任意数量的请求合并到一个调用中。我们甚至可以将不同类型的请求合并在一起：

    ```java
    jestClient.execute(new Bulk.Builder()
    .defaultIndex("employees")
    .addAction(new Index.Builder(employeeObject1).build())
    .addAction(new Index.Builder(employeeObject2).build())
    .addAction(new Delete.Builder("17").build())
    .build());
    ```

5. 异步操作

    Jest 客户端还支持异步操作，这意味着我们可以使用非阻塞 I/O 执行上述任何操作。

    要异步调用操作，只需使用客户端的 executeAsync 方法即可：

    ```java
    jestClient.executeAsync(
    new Index.Builder(employeeObject1).build(),
    new JestResultHandler<JestResult>() {
        @Override public void completed(JestResult result) {
            // handle result
        }
        @Override public void failed(Exception ex) {
            // handle exception
        }
    });
    ```

    请注意，除了操作（本例中为索引）外，异步流程还需要一个 JestResultHandler。当操作完成后，Jest 客户端将调用该对象。该接口有两个方法--completed 和 failed--可分别处理操作的成功或失败。

6. 结论

    在本教程中，我们简要介绍了 Elasticsearch 的 RESTful Java 客户端 Jest 客户端。

    虽然我们只介绍了它的一小部分功能，但很明显，Jest 是一个强大的 Elasticsearch 客户端。其流畅的构建器类和 RESTful 接口使其易于学习，对 Elasticsearch 接口的全面支持使其成为原生客户端的有力替代品。
