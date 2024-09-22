# [在Spring REST API中使用JSON Patch](https://www.baeldung.com/spring-rest-json-patch)

1. 介绍

    在各种可用的HTTP方法中，HTTP PATCH方法发挥着独特的作用。它允许我们对HTTP资源进行部分更新。

    在本教程中，我们将研究如何使用HTTP PATCH方法和JSON Patch文档格式来对我们的RESTful资源进行部分更新。

2. 用例

    让我们从考虑JSON文档表示的HTTP客户资源示例开始：

    ```json
    {
        "id":"1",
        "telephone":"001-555-1234",
        "favorites":["Milk","Eggs"],
        "communicationPreferences": {"post":true, "email":true}
    }
    ```

    让我们假设这位客户的电话号码已经更改，并且客户在他们最喜欢的产品列表中添加了一个新项目。这意味着我们只需要更新客户的电话和收藏夹字段。

    我们要怎么做？

    首先想到流行的HTTP PUT方法。然而，由于PUT完全替换了资源，因此这不是优雅地应用部分更新的合适方法。此外，在应用和保存更新之前，客户端必须执行GET。

    这就是[HTTP PATCH](https://tools.ietf.org/html/rfc5789#page-8)方法派上用场的地方。

    让我们来了解一下HTTP PATCH方法和JSON PATCH格式。

3. HTTP PATCH方法和JSON PATCH格式

    HTTP PATCH方法提供了对资源应用部分更新的好方法。因此，客户只需要发送他们请求中的差异。

    让我们来看看HTTP PATCH请求的简单示例：

    ```log
    PATCH /customers/1234 HTTP/1.1
    Host: <www.example.com>
    Content-Type: application/example
    If-Match: "e0023aa4e"
    Content-Length: 100

    [description of changes]
    ```

    HTTP PATCH请求主体描述了如何修改目标资源以生成新版本。此外，用于表示`[description of changes]`的格式因资源类型而异。对于JSON资源类型，用于描述更改的格式是[JSON Patch](https://tools.ietf.org/html/rfc6902)。

    简单地说，JSON Patch格式使用“series of operations”来描述如何修改目标资源。JSON PATCH文档是JSON对象的数组。数组中的每个对象正好代表一个JSON PATCH操作。

    现在让我们来看看JSON PATCH操作和一些示例。

4. JSON PATCH操作

    JSON Patch操作由单个操作对象表示。

    例如，在这里，我们正在定义一个JSON Patch操作来更新客户的电话号码：

    ```json
    {
        "op":"replace",
        "path":"/telephone",
        "value":"001-555-5678"
    }
    ```

    每个操作都必须有一个路径成员。此外，一些操作对象也必须包含一个来自成员。路径和来自成员的值是[JSON指针](https://tools.ietf.org/html/rfc6901)。它指的是目标文档中的位置。此位置可以指向目标对象中的特定键或数组元素。

    现在让我们简单看一下可用的JSON Patch操作。

    1. 添加操作

        我们使用添加操作向对象添加新成员。此外，我们可以使用它来更新现有成员，并在指定索引的数组中插入一个新值。

        例如，让我们在索引0处将“Bread”添加到客户的收藏夹列表中：

        ```json
        {
            "op":"add",
            "path":"/favorites/0",
            "value":"Bread"
        }
        ```

        添加操作后修改的客户详细信息将是：

        ```json
        {
            "id":"1",
            "telephone":"001-555-1234",
            "favorites":["Bread","Milk","Eggs"],
            "communicationPreferences": {"post":true, "email":true}
        }
        ```

    2. 移除操作

        删除操作会删除目标位置的值。此外，它可以从指定索引的数组中删除元素。

        例如，让我们删除客户的通信偏好：

        ```json
        {
            "op":"remove",
            "path":"/communicationPreferences"
        }
        ```

        删除操作后修改的客户详细信息将是：

        ```json
        {
            "id":"1",
            "telephone":"001-555-1234",
            "favorites":["Bread","Milk","Eggs"],
            "communicationPreferences":null
        }
        ```

    3. 替换操作

        替换操作使用新值更新目标位置的值。

        例如，让我们更新客户的电话号码：

        ```json
        {
            "op":"replace",
            "path":"/telephone",
            "value":"001-555-5678"
        }
        ```

        更换操作后修改的客户详细信息为：

        ```json
        {
            "id":"1",
            "telephone":"001-555-5678",
            "favorites":["Bread","Milk","Eggs"],
            "communicationPreferences":null
        }
        ```

    4. 移动操作

        移动操作删除指定位置的值，并将其添加到目标位置。

        例如，让我们将“面包”从客户收藏夹列表的顶部移动到列表的底部：

        ```json
        {
            "op":"move",
            "from":"/favorites/0",
            "path":"/favorites/-"
        }
        ```

        移动操作后修改的客户详细信息将是：

        ```json
        {
            "id":"1",
            "telephone":"001-555-5678",
            "favorites":["Milk","Eggs","Bread"],
            "communicationPreferences":null
        }
        ```

        上述示例中的/favorites/0和/favorites/-是指向favorites数组的开始和结束索引的JSON指针。

    5. 复制操作

        复制操作将指定位置的值复制到目标位置。

        例如，让我们在收藏夹列表中复制“牛奶”：

        ```json
        {
            "op":"copy",
            "from":"/favorites/0",
            "path":"/favorites/-"
        }
        ```

        复制操作后修改的客户详细信息将是：

        ```json
        {
            "id":"1",
            "telephone":"001-555-5678",
            "favorites":["Milk","Eggs","Bread","Milk"],
            "communicationPreferences":null
        }
        ```

    6. 测试操作

        测试操作测试“path”的值是否等于“value”。由于PATCH操作是原子操作，如果其任何操作失败，则应丢弃PATCH。测试操作可用于验证先决条件和后条件是否已得到满足。

        例如，让我们测试客户电话字段的更新是不是成功的：

        ```json
        {
            "op":"test",
            "path":"/telephone",
            "value":"001-555-5678"
        }
        ```

        现在让我们看看如何将上述概念应用于我们的示例。

5. 使用JSON Patch格式的HTTP补丁请求

    我们将重新审视我们的客户用例。

    这是HTTP PATCH请求，使用JSON Patch格式对客户的电话和收藏夹列表进行部分更新：

    ```bash
    curl -i -X PATCH <http://localhost:8080/customers/1> -H "Content-Type: application/json-patch+json" -d '[
        {"op":"replace","path":"/telephone","value":"+1-555-56"},
        {"op":"add","path":"/favorites/0","value":"Bread"}
    ]'
    ```

    最重要的是，JSON Patch请求的内容类型是application/json-patch+json。此外，请求主体是一个JSON Patch操作对象的数组：

    ```json
    [
        {"op":"replace","path":"/telephone","value":"+1-555-56"},
        {"op":"add","path":"/favorites/0","value":"Bread"}
    ]
    ```

    我们如何在服务器端处理这样的请求？

    一种方法是编写一个自定义框架，该框架按顺序评估操作，并将其作为原子单元应用于目标资源。显然，这种方法听起来很复杂。此外，它可能导致一种非标准化的消耗Patch文档的方式。

    幸运的是，我们不必手工处理JSON Patch请求。

    最初在[JSR 353](https://jcp.org/en/jsr/detail?id=353)中定义的JSON Processing 1.0或JSON-P 1.0的Java API在[JSR 374](https://www.jcp.org/en/jsr/detail?id=374)中引入了对JSON Patch的支持。JSON-P API提供JsonPatch类型来表示JSON Patch实现。

    然而，JSON-P只是一个API。要使用JSON-P API，我们需要使用一个实现它的库。我们将使用一个名为[json-patch](https://github.com/java-json-tools/json-patch)的库作为本文中的示例。

    现在让我们来看看如何使用上述JSON Patch格式构建使用HTTP PATCH请求的REST服务。

6. 在Spring Boot应用程序中实现JSON Patch

    1. 依赖性

        最新版本的json-patch可以从Maven Central存储库中找到。

        首先，让我们将依赖项添加到pom.xml中：

        ```xml
        <dependency>
            <groupId>com.github.java-json-tools</groupId>
            <artifactId>json-patch</artifactId>
            <version>1.12</version>
        </dependency>
        ```

        现在，让我们定义一个模式类来表示客户JSON文档：

        ```java
        public class Customer {
            private String id;
            private String telephone;
            private List<String> favorites;
            private Map<String, Boolean> communicationPreferences;

            // standard getters and setters
        }
        ```

        接下来，我们将看看我们的控制器方法。

    2. REST控制器方法

        然后，我们可以为客户用例实现HTTP PATCH：

        ```java
        @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
        public ResponseEntity<Customer> updateCustomer(@PathVariable String id, @RequestBody JsonPatch patch) {
            try {
                Customer customer = customerService.findCustomer(id).orElseThrow(CustomerNotFoundException::new);
                Customer customerPatched = applyPatchToCustomer(patch, customer);
                customerService.updateCustomer(customerPatched);
                return ResponseEntity.ok(customerPatched);
            } catch (JsonPatchException | JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            } catch (CustomerNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        ```

        现在让我们来了解一下这种方法发生了什么：

        - 首先，我们使用@PatchMapping注释将该方法标记为PATCH处理程序方法
        - 当带有application/json-patch+json“Content-Type”的补丁请求到达时，Spring Boot使用默认MappingJackson2HttpMessageConverter将请求有效负载转换为JsonPatch实例。因此，我们的控制器方法将以JsonPatch实例的形式接收请求主体

        在方法内：

        - 首先，我们调用customerService.findCustomer(id)方法来查找客户记录
        - 随后，如果找到客户记录，我们将调用applyPatchToCustomer(patch, customer)方法。这将JsonPatch应用于客户（稍后会有更多）
        - 然后我们调用customerService.updateCustomer(customerPatched)来保存客户记录
        - 最后，我们向客户返回200个OK回复，并在回复中附上修补的客户详细信息

        最重要的是，真正的魔法发生在applyPatchToCustomer(patch, customer)方法中：

        ```java
        private Customer applyPatchToCustomer(
        JsonPatch patch, Customer targetCustomer) throws JsonPatchException, JsonProcessingException {
            JsonNode patched = patch.apply(objectMapper.convertValue(targetCustomer, JsonNode.class));
            return objectMapper.treeToValue(patched, Customer.class);
        }
        ```

        - 首先，我们有JsonPatch实例，其中包含要应用于目标客户的操作列表
        - 然后，我们将目标客户转换为com.fasterxml.jackson.databind.JsonNode的实例，并将其传递给JsonPatch.apply方法以应用补丁。在幕后，JsonPatch.apply处理将操作应用于目标。补丁的结果也是一个com.fasterxml.jackson.databind.JsonNode实例
        - 然后，我们调用objectMapper.treeToValue方法，该方法将patchedcom.fasterxml.jackson.databind.JsonNode中的数据绑定到客户类型。这是我们修补的客户实例
        - 最后，我们返回修补后的客户实例

        现在让我们对我们的API进行一些测试。

    3. 测试

        首先，让我们使用对我们的API的POST请求创建一个客户：

        ```bash
        curl -i -X POST <http://localhost:8080/customers> -H "Content-Type: application/json"
        -d '{"telephone":"+1-555-12","favorites":["Milk","Eggs"],"communicationPreferences":{"post":true,"email":true}}'
        ```

        我们收到201创建的回复：

        ```log
        HTTP/1.1 201
        Location: <http://localhost:8080/customers/1>
        ```

        位置响应标头被设置为新资源的位置。它表示新客户的ID是1。

        接下来，让我们使用PATCH请求向该客户请求部分更新：

        ```bash
        curl -i -X PATCH <http://localhost:8080/customers/1> -H "Content-Type: application/json-patch+json" -d '[
            {"op":"replace","path":"/telephone","value":"+1-555-56"},
            {"op":"add","path":"/favorites/0","value": "Bread"}
        ]'
        ```

        我们收到200个OK回复，并附上修补的客户详细信息：

        ```log
        HTTP/1.1 200
        Content-Type: application/json
        Transfer-Encoding: chunked
        Date: Fri, 14 Feb 2020 21:23:14 GMT

        {"id":"1","telephone":"+1-555-56","favorites":["Bread","Milk","Eggs"],"communicationPreferences":{"post":true,"email":true}}
        ```

7. 结论

    在本文中，我们研究了如何在Spring REST API中实现JSON Patch。

    首先，我们研究了HTTP PATCH方法及其执行部分更新的能力。

    然后，我们研究了什么是JSON Patch，并了解了各种JSON Patch操作。

    最后，我们讨论了如何使用json-patch库在Spring Boot应用程序中处理HTTP PATCH请求。
