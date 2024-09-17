# [Spring @RequestParam 注解](https://www.baeldung.com/spring-request-param)

1. 概述

    在本快速教程中，我们将探索 Spring 的 @RequestParam 注解及其属性。

    简单地说，我们可以使用 @RequestParam 从请求中提取查询参数、表单参数甚至文件。

2. 简单映射

    假设我们有一个端点 /api/foos，它需要一个名为 id 的查询参数：

    ```java
    @GetMapping("/api/foos")
    @ResponseBody
    public String getFoos(@RequestParam String id) {
        return "ID: " + id;
    }
    ```

    在本例中，我们使用 @RequestParam 来提取 id 查询参数。

    一个简单的 GET 请求将调用 getFoos：

    ```log
    http://localhost:8080/spring-mvc-basics/api/foos?id=abc
    ----
    ID: abc
    ```

    @RequestParam 支持的属性包括：name、value、required 和 defaultValue，下面我们分别进行讲解

3. 指定请求参数名

    默认请求参数名和变量名一样，可以使用 name 指定：

    ```java
    @PostMapping("/api/foos")
    @ResponseBody
    public String addFoo(@RequestParam(name = "id") String fooId, @RequestParam String name) { 
        return "ID: " + fooId + " Name: " + name;
    }
    ```

    使用 @RequestParam(value = "id") 和 @RequestParam("id") 作用是一样的

4. 可选参数

    @RequestParam 注解的参数默认是必传，如果缺失会报错：

    ```log
    GET /api/foos HTTP/1.1
    -----
    400 Bad Request
    Required String parameter 'id' is not present
    ```

    可设置 required 为false，非必传

    ```java
    @GetMapping("/api/foos")
    @ResponseBody
    public String getFoos(@RequestParam(required = false) String id) { 
        return "ID: " + id;
    }
    ```

    测试：

    ```log
    http://localhost:8080/spring-mvc-basics/api/foos?id=abc
    ----
    ID: abc

    http://localhost:8080/spring-mvc-basics/api/foos
    ----
    ID: null
    ```

    1. 使用 Java 8 Optional

        也可以利用 Java8 Optional* 实现可选参数，而不用指定 required 属性：

        ```java
        @GetMapping("/api/foos")
        @ResponseBody
        public String getFoos(@RequestParam Optional<String> id){
            return "ID: " + id.orElseGet(() -> "not provided");
        }
        ```

        测试：

        ```log
        http://localhost:8080/spring-mvc-basics/api/foos 
        ---- 
        ID: not provided
        ```

5. 参数默认值

    通过 defaultValue 属性设置默认值:

    ```java
    @GetMapping("/api/foos")
    @ResponseBody
    public String getFoos(@RequestParam(defaultValue = "test") String id) {
        return "ID: " + id;
    }
    ```

    与 required=false 类似，用户不再需要提供参数。

    不过，我们还是可以提供的id参数：

    ```log
    http://localhost:8080/spring-mvc-basics/api/foos?id=abc
    ----
    ID: abc
    ```

    请注意，当我们设置 defaultValue 属性时，required 确实被设置为 false。

6. 映射全部参数

    我们还可以使用映射（Map）来设置多个参数，而无需定义参数名称或数量：

    ```java
    @PostMapping("/api/foos")
    @ResponseBody
    public String updateFoos(@RequestParam Map<String,String> allParams) {
        return "Parameters are " + allParams.entrySet();
    }
    ```

    就能将发送的任何参数反射回来：

    ```log
    curl -X POST -F 'name=abc' -F 'id=123' http://localhost:8080/spring-mvc-basics/api/foos
    -----
    Parameters are {[name=abc], [id=123]}
    ```

7. 接受多值参数

    一个 @RequestParam 可以有多个值：

    ```java
    @GetMapping("/api/foos")
    @ResponseBody
    public String getFoos(@RequestParam List<String> id) {
        return "IDs are " + id;
    }
    ```

    多个值之间以逗号分割:

    ```log
    http://localhost:8080/spring-mvc-basics/api/foos?id=1,2,3
    ----
    IDs are [1,2,3]
    ```

    或者直接:

    ```log
    http://localhost:8080/spring-mvc-basics/api/foos?id=1&id=2
    ----
    IDs are [1,2]
    ```

8. 结论

    在本文中，我们学习了如何使用 @RequestParam。
