# [开始使用GraphQL和Spring Boot](https://www.baeldung.com/spring-graphql)

1. 简介

    [GraphQL](http://graphql.org/)是Facebook提出的一个相对较新的概念，被称为Web API的REST的替代品。

    在本教程中，我们将学习如何使用Spring Boot设置GraphQL服务器，以便将其添加到现有的应用程序或在新的应用程序中使用。

2. 什么是GraphQL？

    传统的REST API是以服务器管理资源的概念工作的。我们可以按照各种HTTP动词，以一些标准的方式操作这些资源。只要我们的API符合资源的概念，这就非常好，但当我们需要偏离(deviate)它时，很快就会崩溃。

    当客户端同时需要来自多个资源的数据时，这也会受到影响，例如请求一个博客文章和评论。通常情况下，解决这个问题的方法是让客户端发出多个请求，或者让服务器提供可能并不总是需要的额外数据，从而导致更大的响应规模。

    GraphQL为这两个问题提供了一个解决方案。它允许客户端准确地指定它所需要的数据，包括在一次请求中导航子资源，并允许在一次请求中进行多次查询。

    它还以更多的RPC方式工作，使用命名的查询和突变，而不是一套标准的强制性动作。这样做的目的是把控制权放在它所属的地方，由API开发者指定什么是可能的，由API消费者指定什么是需要的。

    例如，一个博客可能允许以下的查询。

    ```GraphQL
    query {
        recentPosts(count: 10, offset: 0) {
            id
            title
            category
            author {
                id
                name
                thumbnail
            }
        }
    }
    ```

    这个查询将:

    - 请求获得最近的十个帖子
    - 对每一篇文章，要求提供ID、标题和类别
    - 对于每个帖子，请求作者，返回ID、名字和缩略图

    在传统的REST API中，这要么需要11个请求，一个请求帖子，10个请求作者，要么需要在帖子的细节中包括作者的细节。

    1. GraphQL模式

        GraphQL服务器暴露了一个描述API的模式。这个模式由类型定义组成。每个类型都有一个或多个字段，每个字段接受零个或多个参数并返回一个特定的类型。

        图(graph)是由这些字段相互嵌套的方式得出的。注意，图不需要是无环的，循环是完全可以接受的，但它是有方向的。客户端可以从一个字段到它的子字段，但它不能自动回到父字段，除非模式明确定义了这一点。

        一个博客的GraphQL模式的例子可能包含以下定义，描述一个帖子，帖子的作者，以及一个根查询，以获得博客上最近的帖子。

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

        # The Root Query for the application
        type Query {
            recentPosts(count: Int, offset: Int): [Post]!
        }

        # The Root Mutation for the application
        type Mutation {
            createPost(title: String!, text: String!, category: String, authorId: String!) : Post!
        }
        ```

        一些名字后面的"!"表示它是一个不可归零的类型。任何没有这个的类型在服务器的响应中都可能是空的。GraphQL服务正确地处理了这些，允许我们安全地请求可空类型的子字段。

        GraphQL服务还使用一组标准的字段来公开模式，允许任何客户端提前查询模式定义。

        这允许客户端自动检测模式的变化，并允许客户端动态地适应模式的工作方式。一个非常有用的例子是GraphiQL工具，它允许我们与任何GraphQL API交互。

3. 介绍GraphQL Spring Boot Starter

    [Spring Boot GraphQL Starter](https://spring.io/projects/spring-graphql)为在很短的时间内运行GraphQL服务器提供了一个绝佳的方法。利用自动配置和基于注解的编程方法，我们只需编写服务所需的代码。

    1. 设置服务

        我们所需要的是正确的依赖关系，这样才能工作。

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-graphql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        ```

        因为GraphQL是不依赖于传输的，所以我们在配置中包含了web starter。这将使用Spring MVC在默认的/graphql端点上通过HTTP暴露GraphQL API。其他启动器可用于其他底层实现，如Spring Webflux。

        如果有必要，我们也可以在application.properties文件中定制这个端点。

    2. 编写模式

        GraphQL Boot启动器通过处理GraphQL模式文件来建立正确的结构，然后将特殊的Bean连接到这个结构。Spring Boot GraphQL启动器会自动找到这些模式文件。

        我们需要将这些".graphqls"或".gqls"模式文件保存在 src/main/resources/graphql/** 位置下，Spring Boot会自动拾取它们。像往常一样，我们可以用spring.graphql.schema.lots定制位置，用spring.graphql.schema.file-extensions配置属性定制文件扩展名。

        一个要求是，必须有一个根查询和最多一个根突变。与模式的其他部分不同，我们不能将其分割到不同的文件中。这是GraphQL模式定义的一个限制，而不是Java实现的限制。

    3. 根查询解析器

        根查询需要有特别注释的方法来处理这个根查询中的各种字段。与模式定义不同，没有限制根查询字段只能有一个Spring Bean。

        我们需要用@QueryMapping注解来注解处理方法，并将这些方法放在我们应用程序中的标准@Controller组件内。这就将注释的类注册为我们GraphQL应用程序中的数据获取组件。

        ```java
        @Controller
        public class PostController {

            private PostDao postDao;

            @QueryMapping
            public List<Post> recentPosts(@Argument int count, @Argument int offset) {
                return postDao.getRecentPosts(count, offset);
            }
        }
        ```

        上面定义了 recentPosts 方法，我们将用它来处理对前面定义的模式中 recentPosts 字段的任何 GraphQL 查询。此外，该方法必须有与模式中的相应参数相对应的@Argument注释的参数。

        它还可以选择其他与GraphQL相关的参数，如GraphQLContext、DataFetchingEnvironment等，用于访问底层上下文和环境。

        该方法还必须为GraphQL方案中的类型返回正确的返回类型，正如我们即将看到的。我们可以使用任何简单的类型，如String、Int、List等，与之等价的Java类型，系统只是自动映射它们。

    4. 使用Bean来表示类型

        GraphQL服务器中的每个复杂类型都由一个Java Bean来表示，无论是从根查询还是从结构中的其他地方加载。相同的Java类必须始终代表相同的GraphQL类型，但类的名称不是必须的。

        Java Bean内的字段将根据字段的名称直接映射到GraphQL响应中的字段。

        ```java
        public class Post {
            private String id;
            private String title;
            private String category;
            private String authorId;
        }
        ```

        Java Bean上没有映射到GraphQL模式的任何字段或方法将被忽略，但不会导致问题。这对字段解析器的工作很重要。

        例如，在这里，字段authorId并不对应于我们先前定义的模式中的任何内容，但它将可用于下一步。

    5. 复杂值的字段解析器

        有时，一个字段的值的加载是不简单的。这可能涉及到数据库查询、复杂的计算，或者其他任何事情。@SchemaMapping注解将处理方法映射到模式中具有相同名称的字段，并将其用作该字段的DataFetcher。

        ```java
        @SchemaMapping
        public Author author(Post post) {
            return authorDao.getAuthor(post.getAuthorId());
        }
        ```

        重要的是，如果客户端没有请求一个字段，那么GraphQL服务器就不会做工作来检索它。这意味着，如果客户端检索了一个post，但没有询问author字段，上面的author()方法就不会被执行，DAO调用也不会进行。

        另外，我们也可以在注释中指定父类型名称和字段名称。

        ```java
        @SchemaMapping(typeName="Post", field="author")
        public Author getAuthor(Post post) {
            return authorDao.getAuthor(post.getAuthorId());
        }
        ```

        这里，注解属性被用来声明这是模式中作者字段的处理程序。

    6. 可归零的值

        GraphQL模式有一个概念，即有些类型是可空的，有些则不是。

        我们在Java代码中通过直接使用null值来处理这个问题。相反，我们可以直接使用Java 8中的新的Optional类型来处理可归零的类型，系统会对这些值做正确的处理。

        这非常有用，因为这意味着我们的Java代码从方法定义上与GraphQL模式更明显的相同。

    7. 变异

        到目前为止，我们所做的一切是关于从服务器上检索数据的。GraphQL也有能力通过突变来更新存储在服务器上的数据。

        从代码的角度来看，查询没有理由不改变服务器上的数据。我们可以很容易地写出接受参数的查询解析器，保存新数据，并返回这些变化。这样做会给API客户端带来令人惊讶的副作用，被认为是不好的做法。

        相反，应该使用突变来通知客户端，这将导致正在存储的数据发生变化。

        与查询类似，突变是在控制器中通过用@MutationMapping注释处理方法来定义的。突变字段的返回值与查询字段的返回值的处理方式完全相同，允许嵌套的值也被检索。

        ```java
        @MutationMapping
        public Post createPost(@Argument String title, @Argument String text,
        @Argument String category, @Argument String authorId) {

            Post post = new Post();
            post.setId(UUID.randomUUID().toString());
            post.setTitle(title);
            post.setText(text);
            post.setCategory(category);
            post.setAuthorId(authorId);

            postDao.savePost(post);

            return post;
        }
        ```

4. GraphiQL

    GraphQL也有一个配套的工具，叫做[GraphiQL](https://github.com/graphql/graphiql)。这个UI工具可以与任何GraphQL服务器通信，并帮助消费和开发GraphQL API。它的可下载版本作为Electron应用程序存在，可以从[这里](https://github.com/skevy/graphiql-app)检索到。

    Spring GraphQL带有一个默认的GraphQL页面，在/graphiql端点上暴露。该端点默认是禁用的，但可以通过启用spring.graphql.graphiql.enabled属性来打开它。这提供了一个非常有用的浏览器内工具来编写和测试查询，特别是在开发和测试期间。

5. 总结

    GraphQL是一项非常令人兴奋的新技术，有可能彻底改变我们开发Web API的方式。

    Spring Boot GraphQL Starter使得将这项技术添加到任何新的或现有的Spring Boot应用程序中变得异常简单。
