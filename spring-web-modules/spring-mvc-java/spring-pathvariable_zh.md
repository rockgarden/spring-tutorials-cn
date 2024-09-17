# [Spring @PathVariable注释](https://www.baeldung.com/spring-pathvariable)

1. 一览表

    在这个快速教程中，我们将探索Spring的@PathVariable注释。

    简单地说，@PathVariable注释可用于处理请求URI映射中的模板变量，并将其设置为方法参数。

    让我们来看看如何使用@PathVariable及其各种属性。

2. 一个简单的映射

    @PathVariable注释的简单用例是使用主键标识实体的端点：

    ```java
    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public String getEmployeesById(@PathVariable String id) {
        return "ID: " + id;
    }
    ```

    在本例中，我们使用@PathVariable注释来提取URI的模板部分，由变量{id}表示。

    对/api/employees/{id}的简单GET请求将调用带有提取的id值的getEmployeesById：

    ```log
    <http://localhost:8080/api/employees/111>
    ----
    ID: 111
    ```

    现在让我们进一步探索这个注释，并看看它的属性。

3. 指定路径变量名称

    在上一个示例中，我们跳过了定义模板路径变量的名称，因为方法参数和路径变量的名称是相同的。

    然而，如果路径变量名称不同，我们可以在@PathVariable注释的参数中指定它：

    ```java
    @GetMapping("/api/employeeswithvariable/{id}")
    @ResponseBody
    public String getEmployeesByIdWithVariableName(@PathVariable("id") String employeeId) {
        return "ID: " + employeeId;
    }
    ```

    ```log
    <http://localhost:8080/api/employeeswithvariable/1>
    ----
    ID: 1
    ```

    为了清楚起见，我们还可以将路径变量名称定义为@PathVariable(value="id")而不是PathVariable("id")。

4. 单个请求中的多个路径变量

    根据用例，我们可以在控制器方法的请求URI中包含多个路径变量，该方法也具有多个方法参数：

    ```java
    @GetMapping("/api/employees/{id}/{name}")
    @ResponseBody
    public String getEmployeesByIdAndName(@PathVariable String id, @PathVariable String name) {
        return "ID: " + id + ", name: " + name;
    }
    ```

    ```log
    <http://localhost:8080/api/employees/1/bar>
    ----
    ID: 1, name: bar
    ```

    我们还可以使用`typejava.util.Map<String, String>`的方法参数来处理多个@PathVariable参数：

    ```java
    @GetMapping("/api/employeeswithmapvariable/{id}/{name}")
    @ResponseBody
    public String getEmployeesByIdAndNameWithMapVariable(@PathVariable Map<String, String> pathVarsMap) {
        String id = pathVarsMap.get("id");
        String name = pathVarsMap.get("name");
        if (id != null && name != null) {
            return "ID: " + id + ", name: " + name;
        } else {
            return "Missing Parameters";
        }
    }
    ```

    ```log
    <http://localhost:8080/api/employees/1/bar>
    ----
    ID: 1, name: bar
    ```

    然而，当路径变量字符串包含dot(.)字符时，在处理多个@PathVariable参数时，会有一个小的捕获。我们在[这里](https://www.baeldung.com/spring-mvc-pathvariable-dot)详细讨论了那些角落的案例。

5. 可选路径变量

    在Spring中，默认情况下需要用@PathVariable注释的方法参数：

    ```java
    @GetMapping(value = { "/api/employeeswithrequired", "/api/employeeswithrequired/{id}" })
    @ResponseBody
    public String getEmployeesByIdWithRequired(@PathVariable String id) {
        return "ID: " + id;
    }
    ```

    考虑到它的外观，上述控制器应该同时处理/api/employeeswithrequired和/api/employeeswithrequired/1请求路径。然而，由于@PathVariables注释的方法参数默认是强制性的，因此它不会处理发送到/api/employeeswithrequired路径的请求：

    ```log
    <http://localhost:8080/api/employeeswithrequired>
    ----
    {"timestamp":"2020-07-08T02:20:07.349+00:00","status":404,"error":"Not Found","message":"","path":"/api/employeeswithrequired"}
    <http://localhost:8080/api/employeeswithrequired/1>
    ----
    ID: 111
    ```

    我们可以通过两种不同的方式处理这件事。

    1. 将@PathVariable设置为不需要

        我们可以将@PathVariable的必需属性设置为false，使其成为可选属性。因此，修改我们之前的示例，我们现在可以处理带和不带路径变量的URI版本：

        ```java
        @GetMapping(value = { "/api/employeeswithrequiredfalse", "/api/employeeswithrequiredfalse/{id}" })
        @ResponseBody
        public String getEmployeesByIdWithRequiredFalse(@PathVariable(required = false) String id) {
            if (id != null) {
                return "ID: " + id;
            } else {
                return "ID missing";
            }
        }
        ```

        ```log
        <http://localhost:8080/api/employeeswithrequiredfalse>
        ----
        ID missing
        ```

    2. 使用java.util.Optional

        自Spring 4.1推出以来，我们还可以使用`java.util.Optional<T>`（在Java 8+中可用）来处理非强制性路径变量：

        ```java
        @GetMapping(value = { "/api/employeeswithoptional", "/api/employeeswithoptional/{id}" })
        @ResponseBody
        public String getEmployeesByIdWithOptional(@PathVariable Optional<String> id) {
            if (id.isPresent()) {
                return "ID: " + id.get();
            } else {
                return "ID missing";
            }
        }
        ```

        现在，如果我们没有在请求中指定路径变量id，我们将得到默认响应：

        ```log
        <http://localhost:8080/api/employeeswithoptional>
        ----
        ID missing
        ```

    3. 使用Map类型的方法参数`<String，String>`

        如前所述，我们可以使用java.util.Map类型的单一方法参数来处理请求URI中的所有路径变量。我们还可以使用此策略来处理可选路径变量的情况：

        ```java
        @GetMapping(value = { "/api/employeeswithmap/{id}", "/api/employeeswithmap" })
        @ResponseBody
        public String getEmployeesByIdWithMap(@PathVariable Map<String, String> pathVarsMap) {
            String id = pathVarsMap.get("id");
            if (id != null) {
                return "ID: " + id;
            } else {
                return "ID missing";
            }
        }
        ```

6. @PathVariable的默认值

    开箱即用，没有为使用@PathVariable注释的方法参数定义默认值的规定。然而，我们可以使用上面讨论的相同策略来满足@PathVariable的默认值情况，我们只需要在路径变量上检查空值。

    例如，使用`java.util.Optional<String, String>`，我们可以识别路径变量是否为空。如果它是空的，那么我们只需使用默认值来响应请求：

    ```java
    @GetMapping(value = { "/api/defaultemployeeswithoptional", "/api/defaultemployeeswithoptional/{id}" })
    @ResponseBody
    public String getDefaultEmployeesByIdWithOptional(@PathVariable Optional<String> id) {
        if (id.isPresent()) {
            return "ID: " + id.get();
        } else {
            return "ID: Default Employee";
        }
    }
    ```

7. 结论

    在本文中，我们讨论了如何使用Spring的@PathVariable注释。我们还确定了有效使用@PathVariable注释以适应不同用例的各种方法，例如可选参数和处理默认值。
