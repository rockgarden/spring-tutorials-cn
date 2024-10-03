# [用Spring Boot处理GraphQL中的错误](https://www.baeldung.com/spring-graphql-error-handling)

1. 概述

    在本教程中，我们将学习 GraphQL 中的错误处理选项。我们将研究GraphQL规范中关于错误响应的内容。因此，我们将使用Spring Boot开发一个GraphQL错误处理的例子。

2. 根据GraphQL规范的响应

    根据GraphQL规范，收到的每个请求都必须返回一个格式良好的响应。这个格式良好的响应包括数据map或来自各自成功或不成功的请求操作的错误。此外，一个响应可能包含部分成功的结果数据和字段错误。

    响应map的关键组成部分是错误、数据和扩展。

    响应中的错误部分描述了在请求的操作过程中的任何失败。如果没有发生错误，响应中就不能出现错误部分。在下一节，我们将研究规范中描述的不同种类的错误。

    数据部分描述了成功执行请求操作的结果。如果操作是一个查询，这个部分是一个查询根操作类型的对象。另一方面，如果操作是一个突变，这个组件是突变根操作类型的一个对象。

    如果由于信息缺失、验证错误或语法错误，请求的操作在执行前就已经失败了，那么数据组件就不能出现在响应中。而如果在操作执行过程中操作失败，并出现不成功的结果，那么数据组件必须为空。

    响应map可以包含一个额外的组件，称为扩展，它是一个map对象。该组件方便实现者在响应中提供他们认为合适的其他自定义内容。因此，对其内容格式没有额外的限制。

    如果数据组件没有出现在响应中，那么错误组件必须出现，并且必须至少包含一个错误。此外，它应该指出失败的原因。

    下面是一个GraphQL错误的例子:

    ```GraphQL
    mutation {
    addVehicle(vin: "NDXT155NDFTV59834", year: 2021, make: "Toyota", model: "Camry", trim: "XLE",
                location: {zipcode: "75024", city: "Dallas", state: "TX"}) {
        vin
        year
        make
        model
        trim
    }
    }
    ```

    违反唯一约束时的错误响应将看起来像。

    ```json
    {
    "data": null,
    "errors": [
        {
        "errorType": "DataFetchingException",
        "locations": [
            {
            "line": 2,
            "column": 5,
            "sourceName": null
            }
        ],
        "message": "Failed to add vehicle. Vehicle with vin NDXT155NDFTV59834 already present.",
        "path": [
            "addVehicle"
        ],
        "extensions": {
            "vin": "NDXT155NDFTV59834"
        }
        }
    ]
    }
    ```

3. 根据GraphQL规范的错误响应组件

    响应中的错误部分是一个非空的错误列表，每个错误都是一个map。

    1. 请求错误

        顾名思义，如果请求本身有任何问题，请求错误可能在操作执行之前发生。它可能是由于请求数据解析失败，请求文档验证，不支持的操作，或者无效的请求值。

        当请求错误发生时，这表明执行还没有开始，这意味着响应中的数据部分一定不会出现在响应中。换句话说，响应中只包含错误部分。

        让我们看一个演示无效输入语法情况的例子。

        ```GraphQL
        query {
        searchByVin(vin: "error) {
            vin
            year
            make
            model
            trim
        }
        }
        ```

        下面是一个语法错误的请求错误响应，在本例中是缺少一个引号：

        ```json
        {
        "data": null,
        "errors": [
            {
            "message": "Invalid Syntax",
            "locations": [
                {
                "line": 5,
                "column": 8,
                "sourceName": null
                }
            ],
            "errorType": "InvalidSyntax",
            "path": null,
            "extensions": null
            }
        ]
        }
        ```

    2. 字段错误

        字段错误，顾名思义，可能是由于未能将值强制变成预期的类型，或者在特定字段的值解析过程中出现内部错误。这意味着字段错误是在执行要求的操作过程中发生的。

        在发生字段错误的情况下，继续执行请求的操作并返回部分结果，这意味着响应的数据部分必须与错误部分的所有字段错误一起出现。

        让我们看看另一个例子。

        ```GraphQL
        query {
        searchAll {
            vin
            year
            make
            model
            trim
        }
        }
        ```

        这一次，我们包含了车辆装饰字段，根据我们的GraphQL模式，它应该是不可为空的。

        然而，其中一个车辆的信息有一个空的修饰值，所以我们只得到了部分数据--修饰值不为空的车辆--以及错误。

        ```json
        {
        "data": {
            "searchAll": [
            null,
            {
                "vin": "JTKKU4B41C1023346",
                "year": 2012,
                "make": "Toyota",
                "model": "Scion",
                "trim": "Xd"
            },
            {
                "vin": "1G1JC1444PZ215071",
                "year": 2000,
                "make": "Chevrolet",
                "model": "CAVALIER VL",
                "trim": "RS"
            }
            ]
        },
        "errors": [
            {
            "message": "Cannot return null for non-nullable type: 'String' within parent 'Vehicle' (/searchAll[0]/trim)",
            "path": [
                "searchAll",
                0,
                "trim"
            ],
            "errorType": "DataFetchingException",
            "locations": null,
            "extensions": null
            }
            ]
        }
        ```

    3. 错误响应格式

        正如我们前面看到的，响应中的错误是一个或多个错误的集合。而且，每个错误都必须包含一个描述失败原因的消息键，以便客户端开发人员可以进行必要的修正以避免错误。

        每个错误还可能包含一个叫做location的键，这是一个位置列表，指向请求的GraphQL文档中与错误相关的一行。每个位置是一个带有键的地图：行和列，分别提供相关元素的行号和起始列号。

        另一个可能是错误的一部分的键被称为路径。它提供了从根元素追踪到响应中出现错误的特定元素的值的列表。路径值可以是一个字符串，代表字段名，如果字段值是一个列表，则是错误元素的索引。如果错误与一个有别名的字段有关，那么路径中的值应该是别名。

    4. 处理字段错误

        无论字段错误是发生在可归零还是不可归零的字段上，我们都应该像字段返回空值一样处理它，并且该错误必须被添加到错误列表中。

        在可置空字段的情况下，该字段在响应中的值将是空的，但错误必须包含这个字段的错误，描述失败的原因和其他信息，正如在前面的章节中看到的。

        另一方面，父字段处理非空字段的错误。如果父字段是不可空的，那么错误处理就会被传播，直到我们到达一个可空的父字段或根元素。

        类似地，如果一个列表字段包含一个不可置空的类型，并且一个或多个列表元素返回null，整个列表解析为null。此外，如果包含列表字段的父字段是不可置空的，那么错误处理会被传播，直到我们到达一个可置空的父字段或根元素。

        出于任何原因，如果在解析过程中对同一字段提出了多个错误，那么对于该字段，我们必须只将一个字段的错误加入错误中。

4. Spring Boot GraphQL库

    我们的Spring Boot应用实例使用spring-boot-starter-graphql模块，它带来了所需的GraphQL依赖。

    我们还使用spring-graphql-test模块进行相关测试。

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-graphql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.graphql</groupId>
        <artifactId>spring-graphql-test</artifactId>
        <scope>test</scope>
    </dependency>
    ```

5. Spring Boot GraphQL错误处理

    在本节中，我们将主要介绍Spring Boot应用程序本身的GraphQL错误处理。我们将不涉及GraphQL Java和GraphQL Spring Boot应用的开发。

    在我们的Spring Boot应用实例中，我们将根据位置或VIN（车辆识别码）对车辆进行变异或查询。我们将看到使用这个例子实现错误处理的不同方法。

    在以下各小节中，我们将看到Spring Boot模块如何处理异常或错误。

    1. 带有标准异常的GraphQL响应

        一般来说，在REST应用程序中，我们通过扩展RuntimeException或Throwable来创建一个自定义的运行时异常类。

        ```java
        public class InvalidInputException extends RuntimeException {
            public InvalidInputException(String message) {
                super(message);
            }
        }
        ```

        通过这种方法，我们可以看到GraphQL引擎返回以下响应。

        ```json
        {
        "errors": [
            {
            "message": "INTERNAL_ERROR for 2c69042a-e7e6-c0c7-03cf-6026b1bbe559",
            "locations": [
                {
                "line": 2,
                "column": 5
                }
            ],
            "path": [
                "searchByLocation"
            ],
            "extensions": {
                "classification": "INTERNAL_ERROR"
            }
            }
        ],
        "data": null
        }
        ```

        在上面的错误响应中，我们可以看到它并没有包含任何错误的细节。

        默认情况下，请求处理过程中的任何异常都由 ExceptionResolversExceptionHandler 类处理，该类实现了GraphQL API的 DataFetcherExceptionHandler 接口。它允许应用程序注册一个或多个 DataFetcherExceptionResolver 组件。

        这些解析器被依次调用，直到其中一个能够处理该异常并将其解析为GraphQLError。如果没有解析器能够处理该异常，那么该异常将被归类为INTERNAL_ERROR。如上图所示，它还包含执行ID和通用错误信息。

    2. 带有处理过的异常的GraphQL响应

        现在让我们看看，如果我们实现了我们的自定义异常处理，响应会是什么样子。

        首先，我们有另一个自定义异常。

        ```java
        public class VehicleNotFoundException extends RuntimeException {
            public VehicleNotFoundException(String message) {
                super(message);
            }
        }
        ```

        DataFetcherExceptionResolver提供了一个异步合约。然而，在大多数情况下，扩展DataFetcherExceptionResolverAdapter并重载它的resolveToSingleError或resolveToMultipleErrors方法之一就足够了，这些方法可以同步地解决异常。

        现在，让我们来实现这个组件，我们可以返回一个NOT_FOUND分类和异常信息，而不是一般的错误。

        ```java
        @Component
        public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

            @Override
            protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
                if (ex instanceof VehicleNotFoundException) {
                    return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
                } else {
                    return null;
                }
            }
        }
        ```

        在这里，我们创建了一个GraphQLError，带有适当的分类和其他错误细节，以便在JSON响应的错误部分创建一个更有用的响应。

        ```json
        {
        "errors": [
            {
            "message": "Vehicle with vin: 123 not found.",
            "locations": [
                {
                "line": 2,
                "column": 5
                }
            ],
            "path": [
                "searchByVin"
            ],
            "extensions": {
                "classification": "NOT_FOUND"
            }
            }
        ],
        "data": {
            "searchByVin": null
        }
        }
        ```

        这个错误处理机制的一个重要细节是，未解决的异常会被记录在ERROR级别，同时记录与发送到客户端的错误相关的executionId。任何已解决的异常，如上所示，在日志中被记录在DEBUG级别。

6. 总结

    在本教程中，我们学习了不同类型的GraphQL错误。我们还研究了如何根据规范格式化GraphQL错误。后来我们在Spring Boot应用程序中实现了错误处理。
