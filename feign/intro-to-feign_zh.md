# [Feign介绍](https://www.baeldung.com/intro-to-feign)

1. 概述

    在本教程中，我们将介绍[Feign](https://github.com/OpenFeign/feign) - 一个由Netflix开发的声明式HTTP客户端。

    Feign的目标是简化HTTP API客户端。简单地说，开发者只需要声明和注释一个接口，而实际的实现是在运行时提供的。

2. 例子

    在本教程中，我们将使用一个暴露了REST API端点的[书店应用](https://github.com/Baeldung/spring-hypermedia-api)实例。

    我们可以很容易地克隆该项目并在本地运行：

    `mvn install spring-boot:run`

3. 设置

    首先，让我们添加需要的依赖项：

    ```xml
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
        <version>10.11</version>
    </dependency>
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-gson</artifactId>
        <version>10.11</version>
    </dependency>
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-slf4j</artifactId>
        <version>10.11</version>
    </dependency>
    ```

    除了feign-core依赖（也被拉进来了），我们将使用一些插件，特别是feign-okhttp用于内部使用Square的OkHttp客户端来进行请求，feign-gson用于使用谷歌的GSON作为JSON处理器，feign-slf4j用于使用简单日志界面来记录请求。

    为了真正得到一些日志输出，我们需要在classpath上有我们最喜欢的支持SLF4J的日志器实现。

    在我们继续创建我们的客户端接口之前，首先我们要建立一个用于保存数据的Book模型：

    feign.models/book.java

    注意：一个JSON处理器至少需要一个 "无参数构造器"。

    事实上，我们的REST提供者是一个[超媒体驱动的API](https://www.baeldung.com/spring-hateoas-tutorial)，所以我们将额外需要一个简单的封装类：

    feign.models/BookResource.java

    > 注意：我们将保持BookResource的简单性，因为我们的样本Feign客户端并没有受益于超媒体功能。

4. 服务器端

    为了了解如何定义一个Feign客户端，我们先来看看我们的REST提供者所支持的一些方法和响应。

    让我们用一个简单的curl shell命令来试试，列出所有的书。

    我们需要记住在所有的调用前加上/api，也就是应用程序的servlet-context：

    `curl http://localhost:8081/api/books`

    结果是，我们将得到一个完整的图书库，以JSON格式表示：

    ```json
    [
    {
        "book": {
        "isbn": "1447264533",
        "author": "Margaret Mitchell",
        "title": "Gone with the Wind",
        "synopsis": null,
        "language": null
        },
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8081/api/books/1447264533"
        }
        ]
    },

    ...

    {
        "book": {
        "isbn": "0451524934",
        "author": "George Orwell",
        "title": "1984",
        "synopsis": null,
        "language": null
        },
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8081/api/books/0451524934"
        }
        ]
    }
    ]
    ```

    我们也可以通过在获取请求中附加ISBN来查询单个图书资源：

    `curl http://localhost:8081/api/books/1447264533`

5. Feign客户端

    最后，让我们来定义我们的Feign客户端。

    我们将使用@RequestLine注解来指定HTTP动词和一个路径部分作为参数。

    参数将使用@Param注解进行建模：

    feign.clientsk/BookClient.java

    注意：Feign客户端只能用于消费基于文本的HTTP API，这意味着它们不能处理二进制数据，如文件上传或下载。

    这就是全部! 现在我们将使用Feign.builder()来配置我们基于接口的客户端。

    实际的实现将在运行时进行配置：

    ```java
    BookClient bookClient = Feign.builder()
    .client(new OkHttpClient())
    .encoder(new GsonEncoder())
    .decoder(new GsonDecoder())
    .logger(new Slf4jLogger(BookClient.class))
    .logLevel(Logger.Level.FULL)
    .target(BookClient.class, "http://localhost:8081/api/books");
    ```

    Feign支持各种插件，如JSON/XML编码器和解码器或用于提出请求的底层HTTP客户端。

6. 单元测试

    让我们创建三个测试案例来测试我们的客户端。

    注意，我们对org.hamcrest.CoreMatchers.*和org.junit.Assert.*使用静态导入：

    ```java
    @Test
    public void givenBookClient_shouldRunSuccessfully() throws Exception {
    List<Book> books = bookClient.findAll().stream()
        .map(BookResource::getBook)
        .collect(Collectors.toList());
    assertTrue(books.size() > 2);
    }

    @Test
    public void givenBookClient_shouldFindOneBook() throws Exception {
        Book book = bookClient.findByIsbn("0151072558").getBook();
        assertThat(book.getAuthor(), containsString("Orwell"));
    }

    @Test
    public void givenBookClient_shouldPostBook() throws Exception {
        String isbn = UUID.randomUUID().toString();
        Book book = new Book(isbn, "Me", "It's me!", null, null);
        bookClient.create(book);
        book = bookClient.findByIsbn(isbn).getBook();
        assertThat(book.getAuthor(), is("Me"));
    }
    ```

7. 进一步阅读

    如果我们需要在服务不可用的情况下有某种退路，我们可以将HystrixFeign添加到classpath中，并使用HystrixFeign.builder()构建我们的客户端。

    请看这个专门的[教程系列](https://www.baeldung.com/introduction-to-hystrix)，以了解更多关于Hystrix的信息。

    此外，如果我们想将Spring Cloud Netflix Hystrix与Feign整合，这里有一篇专门的[文章](https://www.baeldung.com/spring-cloud-netflix-hystrix)。

    此外，也可以在我们的客户端添加客户端的负载均衡和/或服务发现。

    我们可以通过将Ribbon添加到我们的classpath并使用构建器来实现：

    ```java
    BookClient bookClient = Feign.builder()
    .client(RibbonClient.create())
    .target(BookClient.class, "http://localhost:8081/api/books");
    ```

    对于服务发现，我们必须在启用Spring Cloud Netflix Eureka后建立我们的服务。然后我们简单地与Spring Cloud Netflix Feign集成。因此，我们可以免费获得Ribbon负载均衡。关于这一点的更多信息可以在[这里](https://www.baeldung.com/spring-cloud-netflix-eureka)找到。

8. 总结

    在这篇文章中，我们已经解释了如何使用Feign构建一个声明式的HTTP客户端，以消费基于文本的API。
