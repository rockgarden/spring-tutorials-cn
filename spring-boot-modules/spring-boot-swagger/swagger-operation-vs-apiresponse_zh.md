# [Swagger 中的 @Operation 与 @ApiResponse](https://www.baeldung.com/swagger-operation-vs-apiresponse)

1. 概述

    在本教程中，我们将讨论 Swagger 的 @Operation 和 @ApiResponse 注释之间的主要区别。

2. 使用 Swagger 编写描述性文档

    当我们创建 REST API 时，创建适当的规范也很重要。此外，这样的规范应该可读、易懂，并提供所有基本信息。

    此外，文档还应描述对 API 所做的每一项更改。手动创建 REST API 文档会很累，更重要的是会很耗时。幸运的是，Swagger 等工具可以帮助我们完成这一过程。

    [Swagger](https://swagger.io/tools/swagger-ui) 是一套围绕 [OpenAPI 规范](https://www.baeldung.com/spring-rest-openapi-documentation)构建的开源工具。它可以帮助我们设计、构建、记录和使用 REST API。

    Swagger 规范是记录 REST API 的标准。使用 Swagger 规范，我们可以描述整个 API，如暴露的端点、操作、参数、验证方法等。

    Swagger 提供了各种注解，可以帮助我们记录 REST API。此外，它还提供了 @Operation 和 @ApiResponse 注释，用于记录 REST API 的响应。在本教程的剩余部分，我们将使用下面的控制器类，看看如何使用这些注解：

    ```java
    @RestController
    @RequestMapping("/customers")
    class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
    }
    ```

3. @Operation

    @Operation 注解用于描述单个操作。操作是路径和 HTTP 方法的唯一组合。

    此外，我们还可以使用 @Operation 来描述 REST API 调用成功后的结果。换句话说，我们可以使用此注解指定一般的返回类型。

    让我们将注解添加到我们的方法中：

    ```java
    @Operation(summary = "Gets customer by ID", 
            description= "Customer must exist")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
    ```

    接下来，我们将介绍 @Operation 中最常用的一些属性。

    1. 摘要属性

        必填的 summary 属性包含操作的摘要字段。简单地说，它提供了对操作的简短描述。不过，我们应使该参数少于 120 个字符。

        下面是我们如何在 @Operation 注解中定义摘要属性：

        `@Operation(summary= "Gets customer by ID")`

    2. 描述属性

        使用 description 属性，我们可以提供有关操作的更多详细信息。例如，我们可以使用一段文字描述端点的限制条件：

        `@Operation(summary= "Gets customer by ID", description= "Customer must exist")`

    3.  隐藏属性

        hidden 属性表示此操作是否隐藏。

4. @ApiResponse

    使用 HTTP 状态代码返回错误是一种常见做法。我们可以使用 @ApiResponse 注解来描述操作的具体可能响应。

    @Operation 注解描述了操作和一般返回类型，而 @ApiResponse 注解则描述了其他可能的返回代码。

    此外，注解既可应用于方法层，也可应用于类层。此外，只有在方法层尚未定义具有相同代码的 @ApiResponse 注释时，类层的注解才会被解析。换句话说，方法注解优先于类注解。

    无论有一个还是多个响应，我们都应在 @ApiResponses 注解中使用 [@ApiResponse](https://www.baeldung.com/java-swagger-set-list-response) 注解。如果我们直接使用该注解，Swagger 将不会对其进行解析。

    让我们在方法中定义 @ApiResponses 和 @ApiResponse 注解：

    ```java
    @ApiResponses(value = {
            @ApiResponse(responseCode = 400, description = "Invalid ID supplied"),
            @ApiResponse(responseCode = 404, description = "Customer not found")})
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
    ```

    我们还可以使用注解指定成功响应：

    ```java
    @Operation(summary = "Gets customer by ID", description = "Customer must exist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = 
            { @Content(mediaType = "application/json", schema = 
                @Schema(implementation = CustomerResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"), 
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = 
            { @Content(mediaType = "application/json", schema = 
                @Schema(implementation = ErrorResponse.class)) }) })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
    ```

    现在，让我们来看看 @ApiResponse 中使用的一些属性。

    1. 响应代码和描述属性

        responseCode 和 description 属性都是 @ApiResponse 注解中的必填参数。值得一提的是，我们不能用相同的代码属性定义多个 @ApiResponse。

        message 属性通常包含与响应一起的可由人阅读的信息：

        `@ApiResponse(responseCode = 400, message = "Invalid ID supplied")`

    2. 内容属性

        有时，端点会使用不同的响应类型。例如，我们可以为成功响应设置一种类型，为错误响应设置另一种类型。我们可以使用可选的内容属性将响应类作为模式来描述它们。

        首先，让我们定义一个在服务器内部出错时返回的类：

        ```java
        class ErrorResponse {
            private String error;
            private String message;
            // getters and setters
        }
        ```

        其次，让我们为内部服务器错误添加一个新的 @ApiResponse：

        ```java
        @Operation(summary = "Gets customer by ID", description = "Customer must exist")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "400", description = "Invalid ID supplied"), 
                @ApiResponse(responseCode = "404", description = "Customer not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = { @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)) }) })
        @GetMapping("/{id}")
        public ResponseEntity<CustomerResponse> getCustomer(@PathVariable("id") Long id) {
            return ResponseEntity.ok(customerService.getById(id));
        }
        ```

5. @Operation 和 @ApiResponse 之间的区别

    总之，下表显示了 @Operation 和 @ApiResponse 注释之间的主要区别：

    | @Operation |@ApiResponse       |
    |-------------|------------------|
    | 用于描述操作 |用于描述操作的可能响应            |
    | 用于成功响应 |用于成功和错误响应              |
    | 仅可在方法层定义| 可在方法或类层定义            |
    | 可直接使用 |可仅在 @ApiResponses 注解中使用 |

6. 结论

    在本文中，我们了解了 @Operation 和 @ApiResponse 注解之间的区别。
