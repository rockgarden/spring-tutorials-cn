# [GraphQL与REST](https://www.baeldung.com/graphql-vs-rest)

1. 概述

    当创建网络服务来支持我们的应用程序时，我们可以选择使用[REST](https://www.baeldung.com/category/rest/)或GraphQL作为通信模式。虽然两者都最有可能通过HTTP使用JSON，但它们有不同的优点和缺点。

    在本教程中，我们将比较GraphQL与REST。我们将创建一个产品数据库的例子，并比较这两种解决方案在执行相同的客户端操作时有何不同。

2. 服务实例

    我们的示例服务将允许我们：

    - 创建一个处于草稿状态的产品
    - 更新产品的详细信息
    - 获取产品列表
    - 获取单个产品及其订单的详细信息
    让我们开始使用REST创建应用程序。

3. REST

    REST（Representational State Transfer）是分布式超媒体系统的一种[架构风格](https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm)。REST的主要数据元素被称为[资源](https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#tab_5_1)。在这个例子中，资源是 "product"。

    1. 创建产品

        为了创建一个产品，我们将在我们的API上使用POST方法。

        ```shell
        curl --request POST 'http://localhost:8081/product' \
        --header 'Content-Type: application/json' \
        --data '{
            "name": "Watch",
            "description": "Special Swiss Watch",
            "status": "Draft",
            "currency": "USD",
            "price": null,
            "imageUrls": null,
            "videoUrls": null,
            "stock": null,
            "averageRating": null
        }'
        ```

        结果是，系统将创建一个新的产品。

    2. 更新产品

        在REST中，我们习惯性地使用PUT方法来更新产品。

        ```shell
        curl --request PUT 'http://localhost:8081/product/{product-id}' \
        --header 'Content-Type: application/json' \
        --data '{
            "name": "Watch",
            "description": "Special Swiss Watch",
            "status": "Draft",
            "currency": "USD",
            "price": 1200.0,
            "imageUrls": [
                "https://graphqlvsrest.com/imageurl/product-id"
            ],
            "videoUrls": [
                "https://graphqlvsrest.com/videourl/product-id"
            ],
            "stock": 10,
            "averageRating": 0.0
        }'
        ```

        结果，{product-id}对象上会有一个更新。

    3. 获取产品列表

        列出产品通常是一个GET操作，我们可以使用一个查询字符串来指定分页。

        `curl --request GET 'http://localhost:8081/product?size=10&page=0'`

        第一个响应对象是：

        ```json
        {
        "id": 1,
        "name": "T-Shirt",
        "description": "Special beach T-Shirt",
        "status": Published,
        "currency": "USD",
        "price": 30.0,
        "imageUrls": ["https://graphqlvsrest.com/imageurl/1"], 
        "videoUrls": ["https://graphqlvsrest.com/videourl/1"], 
        "stock": 10, 
        "averageRating": 3.5 
        }
        ```

    4. 获取带有订单的单个产品

        要获得一个产品和它的订单，我们通常可能期望通过之前的API获得一个产品列表，然后调用一个订单资源来找到相关的订单。

        `curl --request GET 'localhost:8081/order?product-id=1'`

        第一个响应对象是。

        ```json
        {
        "id": 1,
        "productId": 1,
        "customerId": "de68a771-2fcc-4e6b-a05d-e30a8dd0d756",
        "status": "Delivered",
        "address": "43-F 12th Street",
        "creationDate": "Mon Jan 17 01:00:18 GST 2022"
        }
        ```

        由于我们是通过产品ID来查询订单，因此将ID作为查询参数提供给GET操作是合理的。然而，我们应该注意到，我们需要为我们感兴趣的每个产品执行一次这个操作，在原始操作的基础上，获取所有产品。这与N+1选择问题有关。

4. GraphQL

    GraphQL是一种用于API的查询语言，它带有一个使用现有数据服务来完成这些查询的框架。

    GraphQL的构建模块是[查询和变异](https://graphql.org/learn/queries/)。查询负责获取数据，而突变则是用于创建和更新。

    查询和突变都定义了一个[模式](https://graphql.org/learn/schema/)。该模式定义了可能的客户端请求和响应。

    让我们使用GraphQL服务器重新实现我们的例子。

    1. 创建产品

        让我们使用一个名为saveProduct的突变。

        ```bash
        curl --request POST 'http://localhost:8081/graphql' \
        --header 'Content-Type: application/json' \
        --data \
        '{
        "query": "mutation {saveProduct (
            product: {
            name: \"Bed-Side Lamp\",
            price: 24.0,
            status: \"Draft\",
            currency: \"USD\"
            }){ id name currency price status}
        }"
        }'
        ```

        在这个saveProduct函数中，圆括号内的内容是输入类型模式。后面的大括号描述了将由服务器返回的字段。

        当我们运行突变时，我们应该期待一个带有所选字段的响应。

        ```json
        {
        "data": {
            "saveProduct": {
            "id": "12",
            "name": "Bed-Side Lamp",
            "currency": "USD",
            "price": 24.0,
            "status": "Draft"
            }
        }
        }
        ```

        这个请求与我们在REST版本中的POST请求非常相似，尽管我们现在可以对响应进行一些定制。

    2. 更新产品

        同样地，我们可以使用另一个名为updateProduct的突变来修改一个产品。

        ```shell
        curl --request POST 'http://localhost:8081/graphql' \
        --header 'Content-Type: application/json' \
        --data \
        '{"query": "mutation {updateProduct(
            id: 11
            product: {
            price: 14.0,
            status: \"Publish\"
            }){ id name currency price status }  
        }","variables":{}}'
        ```

        我们在响应中收到所选择的字段。

        ```json
        {
        "data": {
            "updateProduct": {
            "id": "12",
            "name": "Bed-Side Lamp",
            "currency": "USD",
            "price": 14.0,
            "status": "Published"
            }
        }
        }
        ```

        正如我们所看到的，GraphQL对响应的格式提供了灵活性。

    3. 获取产品列表

        为了从服务器上获取数据，我们将利用一个查询。

        ```shell
        curl --request POST 'http://localhost:8081/graphql' \
        --header 'Content-Type: application/json' \
        --data \
        '{
            "query": "query {products(size:10,page:0){id name status}}"
        }'
        ```

        在这里，我们也描述了我们想要看到的结果页面。

        ```json
        {
        "data": {
            "products": [
            {
                "id": "1",
                "name": "T-Shirt",
                "status": "Published"
            },
            ...
            ]
        }
        }
        ```

    4. 获取单一产品与订单

        通过GraphQL，我们可以要求GraphQL服务器将产品和订单连接起来。

        ```shell
        curl --request POST 'http://localhost:8081/graphql' \
        --header 'Content-Type: application/json' \
        --data \
        '{
            "query": "query {product(id:1){ id name orders{customerId address status creationDate}}}"
        }'
        ```

        在这个查询中，我们获取id等于1的产品和它的订单。这使我们能够在一个单一的操作中进行请求，让我们检查一下响应。

        ```json
        {
        "data": {
            "product": {
            "id": "1",
            "name": "T-Shirt",
            "orders": [
                {
                "customerId": "de68a771-2fcc-4e6b-a05d-e30a8dd0d756",
                "status": "Delivered",
                "address": "43-F 12th Street",
                "creationDate": "Mon Jan 17 01:00:18 GST 2022"
                }, 
                ...
            ]
            }
        }
        }
        ```

        正如我们在这里看到的，响应有产品的细节和它各自的订单。

        为了用REST实现这一点，我们需要发送几个请求--第一个请求是获取产品，第二个请求是获取相应的订单。

5. 比较

    这些例子显示了GraphQL的使用如何减少了客户端和服务器之间的流量，并允许客户端为响应提供一些格式化的规则。

    值得注意的是，这些API背后的数据源可能仍然需要执行相同的操作来修改或获取数据，但是客户端和服务器之间丰富的接口允许客户端使用GraphQL做更少的工作。

    让我们进一步比较这两种方法。

    1. 灵活和动态

        GraphQL允许灵活和动态的查询。

        - 客户端应用程序可以只请求所需的字段
        - 可以使用[别名](https://graphql.org/learn/queries/#aliases)来请求具有自定义键的字段
        - 客户端可以使用查询来管理结果顺序
        - 客户端可以更好地与API的任何变化脱钩，因为没有单一版本的响应对象结构需要遵循。

    2. 更加廉价的操作

        每个服务器请求都有一个往返时间和有效载荷大小的代价。

        在REST中，我们最终可能会发送多个请求来实现所需的功能。这些多次请求将是一个昂贵的操作。另外，响应的有效载荷可能有不必要的数据，而这些数据可能不是客户端应用程序所需要的。

        GraphQL倾向于避免昂贵的操作。我们通常可以使用GraphQL在一个请求中获取我们需要的所有数据。

    3. 什么时候使用REST？

        GraphQL不是REST的替代品。两者甚至可以在同一个应用程序中共存。根据使用情况，托管GraphQL端点所增加的复杂性可能是值得的。

        在以下情况下，我们可能更喜欢REST：

        - 我们的应用程序自然是资源驱动的，其中的操作是非常直接和完全与个别资源实体相关的
        - 需要网络缓存，因为GraphQL并不支持它。
        - 我们需要文件上传，因为GraphQL本身并不支持它。

6. 结论

    在这篇文章中，我们用一个实际例子比较了REST和GraphQL。

    我们看到了我们是如何按惯例使用每种方法的。

    然后，我们讨论了两种方法都没有明显的优势。我们的需求将是在它们之间做出选择的驱动力。偶尔，两者也可以共存。
