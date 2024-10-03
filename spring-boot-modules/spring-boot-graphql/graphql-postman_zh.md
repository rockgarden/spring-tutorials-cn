# [如何使用Postman测试GraphQL](https://www.baeldung.com/graphql-postman)

1. 概述

    在这个简短的教程中，我们将展示如何使用Postman测试GraphQL端点。

2. 模式概述和方法

    我们将使用在GraphQL教程中创建的端点。作为提醒，该模式包含描述帖子和作者的定义。

    ```GraphQL
    type Post {
        id: ID!
        title: String!
        text: String!
        category: String
        author: Author!
    }
    
    type Author {
        id: ID!
        name: String!
        thumbnail: String
        posts: [Post]!
    }
    ```

    另外，我们还有显示帖子和编写新帖子的方法。

    ```GraphQL
    type Query {
        recentPosts(count: Int, offset: Int): [Post]!
    }
    
    type Mutation {
        createPost(title: String!, text: String!, category: String) : Post!
    }
    ```

    当使用突变来保存数据时，所需的字段会用感叹号来标记。还要注意的是，在我们的Mutation中，返回的类型是Post，但在Query中，我们会得到一个Post对象的列表。

    上述模式可以在Postman API部分加载--只需添加具有GraphQL类型的新API，然后按生成集合。

    配置入口 APIs-Define

    一旦我们加载了我们的模式，我们就可以使用Postman对GraphQL的自动完成支持，轻松地编写样本查询。

3. Postman中的GraphQL请求

    首先，Postman允许我们以GraphQL格式发送主体--我们只需选择GraphQL选项。

    配置入口 request-Body-QUERY

    然后，我们可以写一个原生的GraphQL查询，比如在QUERY部分获取标题、类别和作者姓名。

    ```GraphQL
    query {
        recentPosts(count: 1, offset: 0) {
            title
            category
            author {
                name
            }
        }
    }
    ```

    结果是，我们会得到：

    ```json
    {
        "data": {
            "recentPosts": [
                {
                    "title": "Post",
                    "category": "test",
                    "author": {
                        "name": "Author 0"
                    }
                }
            ]
        }
    }
    ```

    也可以使用原始格式发送请求，但我们必须在头文件部分添加Content-Type: application/graphql。而且，在这种情况下，正文看起来是一样的。

    例如，我们可以更新标题、文本、类别，得到一个ID和标题作为响应。

    ```GraphQL
    mutation {
        createPost (
            title: "Post", 
            text: "test", 
            category: "test",
        ) {
            id
            title
        }
    }
    ```

    操作的类型--如查询和突变--可以在查询体中省略，只要我们使用速记的语法。在这种情况下，我们不能使用操作的名称和变量，但建议使用操作名称，以方便记录和调试。

4. 使用变量

    在变量部分，我们可以创建一个JSON格式的模式，为变量赋值。这就避免了在查询字符串中输入参数。

    配置入口 request-Body-QUERY

    因此，我们可以在QUERY部分修改 recentPosts主体，以动态地分配变量的值。

    ```GraphQL
    query recentPosts ($count: Int, $offset: Int) {
        recentPosts (count: $count, offset: $offset) {
            id
            title
            text
            category
        }
    }
    ```

    然后我们可以在GRAPHQL VARIABLES部分编辑我们希望的变量设置：

    配置入口 request-Body-GRAPHQL VARIABLES

    ```json
    {
    "count": 1,
    "offset": 0
    }
    ```

5. 总结

    我们可以使用Postman轻松地测试GraphQL，它还允许我们导入模式并生成查询。
