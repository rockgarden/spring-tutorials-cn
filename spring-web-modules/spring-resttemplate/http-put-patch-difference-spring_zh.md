# [REST API中的HTTP PUT与HTTP PATCH](https://www.baeldung.com/http-put-patch-difference-spring)

1. 一览表

    在本快速教程中，我们研究了[HTTP PUT和PATCH动词](https://www.baeldung.com/cs/http-put-vs-patch)之间的差异，以及这两种操作的语义。

    我们将使用Spring实现两个支持这两种操作的REST端点，以便更好地了解差异和正确使用它们的方法。

2. 何时使用Put和何时使用Patch？

    让我们从一个简单的和稍微简单的陈述开始。

    当客户端需要完全替换现有资源时，他们可以使用PUT。当他们进行部分更新时，他们可以使用HTTP PATCH。

    例如，在更新资源的单个字段时，发送完整的资源表示可能很麻烦，并且会消耗大量不必要的带宽。在这种情况下，PATCH的语义更有意义。

    这里需要考虑的另一个重要方面是幂等性(idempotence)。PUT是等效的；PATCH可以是等效的，但不需要是。因此，根据我们正在实施的操作的语义，我们也可以根据这一特征选择一种或另一种。

3. 实现PUT和PATCH逻辑

    假设我们想实现REST API来更新具有多个字段的HeavyResource：

    ```java
    public class HeavyResource {
        private Integer id;
        private String name;
        private String address;
        // ...
    }
    ```

    首先，我们需要创建使用PUT处理资源全面更新的端点：

    ```java
    @PutMapping("/heavyresource/{id}")
    public ResponseEntity<?> saveResource(@RequestBody HeavyResource heavyResource,
    @PathVariable("id") String id) {
        heavyResourceRepository.save(heavyResource, id);
        return ResponseEntity.ok("resource saved");
    }
    ```

    这是更新资源的标准端点。

    现在假设地址字段通常会由客户端更新。在这种情况下，我们不想发送包含所有字段的wholeHeavyResource对象，但我们确实希望能够仅通过PATCH方法更新地址字段。

    我们可以创建一个HeavyResourceAddressOnly DTO来表示地址字段的部分更新：

    ```java
    public class HeavyResourceAddressOnly {
        private Integer id;
        private String address;
        // ...
    }
    ```

    接下来，我们可以利用PATCH方法发送部分更新：

    ```java
    @PatchMapping("/heavyresource/{id}")
    public ResponseEntity<?> partialUpdateName(
    @RequestBody HeavyResourceAddressOnly partialUpdate, @PathVariable("id") String id) {
        heavyResourceRepository.save(partialUpdate, id);
        return ResponseEntity.ok("resource address updated");
    }
    ```

    有了这个更精细的DTO，我们可以只发送我们需要更新的字段，而不需要发送整个HeavyResource的开销。

    如果我们有大量的这些部分更新操作，我们也可以跳过为每个out创建自定义DTO——只使用Map：

    ```java
    @RequestMapping(value = "/heavyresource/{id}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> partialUpdateGeneric(
    @RequestBody Map<String, Object> updates,
    @PathVariable("id") String id) {
        heavyResourceRepository.save(updates, id);
        return ResponseEntity.ok("resource updated");
    }
    ```

    这个解决方案将使我们在实现API时更加灵活，但我们也确实失去了一些东西，比如验证。

4. 测试PUT和PATCH

    最后，让我们为两种HTTP方法编写测试。

    首先，我们想通过PUT方法测试完整资源的更新：

    ```java
    mockMvc.perform(put("/heavyresource/1")
    .contentType(MediaType.APPLICATION_JSON_VALUE)
    .content(objectMapper.writeValueAsString(
        new HeavyResource(1, "Tom", "Jackson", 12, "heaven street")))
    ).andExpect(status().isOk());
    ```

    部分更新的执行是通过使用PATCH方法实现的：

    ```java
    mockMvc.perform(patch("/heavyrecource/1")
    .contentType(MediaType.APPLICATION_JSON_VALUE)
    .content(objectMapper.writeValueAsString(
        new HeavyResourceAddressOnly(1, "5th avenue")))
    ).andExpect(status().isOk());
    ```

    我们还可以编写一个更通用的方法的测试：

    ```java
    HashMap<String, Object> updates = new HashMap<>();
    updates.put("address", "5th avenue");
    mockMvc.perform(patch("/heavyresource/1")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(objectMapper.writeValueAsString(updates))
    ).andExpect(status().isOk());
    ```

5. 处理空值的部分请求

    当我们为PATCH方法编写实现时，我们需要指定一份合约，说明当我们在HavyResourceAddressOnly中将空作为地址字段的值时如何处理案例。

    假设客户端发送以下请求：

    ```json
    {
        "id" : 1,
        "address" : null
    }
    ```

    然后，我们可以将其设置为地址字段的值为空，或者通过将其视为无更改来忽略此类请求。

    我们应该选择一种处理空值的策略，并在每次PATCH方法实现中坚持使用。

6. 结论

    在这篇简短的文章中，我们专注于了解HTTP PATCH和PUT方法之间的区别。

    我们实现了一个简单的Spring REST控制器，通过PUT方法更新资源，并使用PATCH进行部分更新。
