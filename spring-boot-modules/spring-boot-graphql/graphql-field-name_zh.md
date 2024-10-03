# [用不同的名字暴露GraphQL字段](https://www.baeldung.com/graphql-field-name)

1. 概述

    GraphQL已被广泛用作网络服务中的一种通信模式。GraphQL的基本前提是在客户端应用程序中灵活使用。

    在本教程中，我们将研究灵活性的另一个方面。我们还将探讨如何用不同的名称来暴露GraphQL字段。

2. GraphQL模式

    让我们举个例子，一个[博客](https://www.baeldung.com/spring-graphql)有不同作者的帖子。GraphQL模式看起来是这样的。

    ```GraphQL
    query {
        recentPosts(count: 1, offset: 0){
            id
            title
            text
            category
            author {
                id
                name
                thumbnail
            }
        }
    }

    type Post {
        id: ID!
        title: String!
        text: String!
        category: String
        authorId: Author!
    }

    type Author {
        id: ID!
        name: String!
        thumbnail: String
        posts: [Post]!
    }
    ```

    这里我们可以获取最近的帖子。每一个帖子都将伴随着它的作者。查询的结果如下。

    ```json
    {
        "data": {
            "recentPosts": [
                {
                    "id": "Post00",
                    "title": "Post 0:0",
                    "text": "Post 0 + by author 0",
                    "category": null,
                    "author": {
                        "id": "Author0",
                        "name": "Author 0",
                        "thumbnail": "http://example.com/authors/0"
                    }
                }
            ]
        }
    }
    ```

3. 用不同的名字暴露GraphQL字段

    一个客户端应用程序可能需要使用字段first_author。现在，它正在使用作者。为了适应这一要求，我们有两个解决方案。

    - 改变GraphQL服务器中模式的定义
    - 利用GraphQL中的[别名](https://graphql.org/learn/queries/#aliases)概念
    让我们逐一看一下这两种方法。

    1. 改变模式

        让我们来更新帖子的模式定义。

        ```GraphQL
        type Post {
            id: ID!
            title: String!
            text: String!
            category: String
            first_author: Author!
        }
        ```

        作者不是一个微不足道的字段。它是一个复杂的字段。我们还需要更新处理方法来适应这一变化。

        PostController中用@SchemaMapping标记的author(Post post)方法，需要更新为getFirst_author(Post post)。另外，还需要在@SchemaMapping中添加字段属性，以反映新的字段名。

        下面是查询的内容。

        ```GraphQL
        query{
            recentPosts(count: 1,offset: 0){
                id
                title
                text
                category
                first_author{
                    id
                    name
                    thumbnail
                }
            }
        }
        ```

        上述查询的结果如下：

        ```json
        {
            "data": {
                "recentPosts": [
                    {
                        "id": "Post00",
                        "title": "Post 0:0",
                        "text": "Post 0 + by author 0",
                        "category": null,
                        "first_author": {
                            "id": "Author0",
                            "name": "Author 0",
                            "thumbnail": "http://example.com/authors/0"
                        }
                    }
                ]
            }
        }
        ```

        这个解决方案有两个主要问题。

        - 它引入了对模式和服务器端实现的改变
        - 它迫使其他客户端应用程序遵循这个更新的模式定义。
        这些问题与GraphQL提供的灵活性功能相矛盾。

    2. GraphQL别名

        在GraphQL中，别名让我们把一个字段的结果重命名为我们想要的任何东西，而不需要改变模式定义。要在查询中引入一个别名，别名和冒号（:）必须在GraphQL字段之前。

        下面是查询的演示。

        ```GraphQL
        query {
            recentPosts(count: 1, offset: 0) {
                id
                title
                text
                category
                first_author: author {
                    id
                    name
                    thumbnail
                }
            }
        }
        ```

        上述查询的结果如下：

        ```json
        {
            "data": {
                "recentPosts": [
                    {
                        "id": "Post00",
                        "title": "Post 0:0",
                        "text": "Post 0 + by author 0",
                        "category": null,
                        "first_author": {
                            "id": "Author0",
                            "name": "Author 0",
                            "thumbnail": "http://example.com/authors/0"
                        }
                    }
                ]
            }
        }
        ```

        让我们注意到，查询本身是在请求第一个帖子。另一个客户端应用程序可能会要求得到first_post而不是recentPosts。这时，Aliases又会来帮忙了。

        ```GraphQL
        query {
            first_post: recentPosts(count: 1, offset: 0) {
                id
                title
                text
                category
                author {
                    id
                    name
                    thumbnail
                }
            }
        }
        ```

        上述查询的结果如下。

        ```json
        {
            "data": {
                "first_post": [
                    {
                        "id": "Post00",
                        "title": "Post 0:0",
                        "text": "Post 0 + by author 0",
                        "category": null,
                        "author": {
                            "id": "Author0",
                            "name": "Author 0",
                            "thumbnail": "http://example.com/authors/0"
                        }
                    }
                ]
            }
        }
        ```

        这两个例子清楚地表明了使用GraphQL是多么灵活。每一个客户端的应用程序都可以根据需求来更新自己。同时，服务器端的模式定义和实现保持不变。

4. 总结

    在这篇文章中，我们研究了用不同的名字暴露graphQL字段的两种方法。我们用例子介绍了Aliases的概念，并解释了它是如何正确的方法。
