# [Spring Data Elasticsearch 简介](https://www.baeldung.com/spring-data-elasticsearch-tutorial)

1. 概述

    在本教程中，我们将以注重代码和实用的方式探索 Spring Data Elasticsearch 的基础知识。

    我们将学习如何使用 Spring Data Elasticsearch 在 Spring 应用程序中索引、搜索和查询 Elasticsearch。Spring Data Elasticseach 是一个实现 Spring Data 的 Spring 模块，它提供了一种与流行的基于 Lucene 的开源搜索引擎交互的方法。

    虽然 Elasticsearch 可以在没有定义模式的情况下工作，但设计一个模式并创建映射以指定我们期望在某些字段中使用的数据类型仍然是一种常见的做法。索引文档时，会根据字段的类型对其进行处理。例如，文本字段将根据映射规则进行标记化和过滤。我们还可以创建自己的过滤器和标记化器。

    为了简单起见，我们将使用一个 docker 镜像作为我们的 Elasticsearch 实例，不过任何监听端口为 9200 的 Elasticsearch 实例都可以。

    我们先启动 Elasticsearch 实例：

    `docker run -d --name es762 -p 9200:9200 -e "discovery.type=single-node" elasticsearch:7.6.2`

2. Spring 数据

    Spring Data 有助于避免模板代码。例如，如果我们定义了一个存储库接口，该接口扩展了 Spring Data Elasticsearch 提供的 ElasticsearchRepository 接口，那么相应文档类的 CRUD 操作将默认可用。

    此外，我们只需用预定义格式的名称声明方法，就能生成方法实现。我们无需编写存储库接口的实现。

    有关 [Spring Data](https://www.baeldung.com/spring-data) 的 Baeldung 指南为我们提供了入门的基本要素。

    1. Maven 依赖

        Spring Data Elasticsearch 为搜索引擎提供了 Java API。为了使用它，我们需要在 pom.xml 中添加一个新的依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-elasticsearch</artifactId>
            <version>4.0.0.RELEASE</version>
        </dependency>
        ```

    2. 定义存储库接口

        为了定义新的版本库，我们将扩展所提供的版本库接口之一，用我们实际的文档和主键类型替换通用类型。

        值得注意的是，ElasticsearchRepository 扩展自 PagingAndSortingRepository。这允许对分页和排序提供内置支持。

        在我们的示例中，我们将在自定义搜索方法中使用分页功能：

        main/spring.data.es.repository/ArticleRepository.java

        通过 findByAuthorsName 方法，版本库代理将根据方法名称创建一个实现。解析算法将确定它需要访问 authors 属性，然后搜索每个条目的 name 属性。

        第二种方法是 findByAuthorsNameUsingCustomQuery，它使用的是使用 @Query 注解定义的自定义 Elasticsearch 布尔查询，要求严格匹配作者姓名和提供的姓名参数。

    3. Java 配置

        在 Java 应用程序中配置 Elasticsearch 时，我们需要定义如何连接到 Elasticsearch 实例。为此，我们将扩展 AbstractElasticsearchConfiguration 类：

        main/.spring.data.es.config/Config.java

        我们使用了标准的 Spring-enabled 风格注解。@EnableElasticsearchRepositories 将使 Spring Data Elasticsearch 扫描所提供的包中的 Spring Data 资源库。

        为了与 Elasticsearch 服务器通信，我们将使用一个简单的 [RestHighLevelClient](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)。虽然 Elasticsearch 提供了多种类型的客户端，但使用 RestHighLevelClient 是与服务器进行未来验证通信的好方法。

        基类已经提供了在服务器上执行操作所需的 ElasticsearchOperations bean。

3. 映射

    我们使用映射为文档定义模式。通过为文档定义模式，我们可以防止文档出现意外结果，例如映射到不需要的类型。

    我们的实体是一个简单的文档--Article，其中 id 的类型是字符串。我们还将指定此类文档必须存储在文章类型中名为 blog 的索引中。

    ```java
    @Document(indexName = "blog", type = "article")
    public class Article {
        @Id
        private String id;
        private String title;
        @Field(type = FieldType.Nested, includeInParent = true)
        private List<Author> authors;
        // standard getters and setters
    }
    ```

    索引可以有多种类型，我们可以用它们来实现层次结构。

    我们将把 authors 字段标记为 FieldType.Nested。这样，我们就可以单独定义 Author 类，但在 Elasticsearch 中对文章文档进行索引时，仍然可以在其中嵌入作者的单个实例。

4. 索引文档

    Spring Data Elasticsearch 通常会根据项目中的实体自动创建索引。不过，我们也可以通过操作模板以编程方式创建索引：

    `elasticsearchOperations.indexOps(Article.class).create();`

    然后，我们就可以向索引中添加文档了：

    ```java
    Article article = new Article("Spring Data Elasticsearch");
    article.setAuthors(asList(new Author("John Smith"), new Author("John Doe")));
    articleRepository.save(article);
    ```

5. 查询

    1. 基于方法名的查询

        当我们使用基于方法名的查询时，我们编写的方法定义了我们要执行的查询。在设置过程中，Spring Data 将解析方法签名并创建相应的查询：

        ```java
        String nameToFind = "John Smith";
        Page<Article> articleByAuthorName
        = articleRepository.findByAuthorsName(nameToFind, PageRequest.of(0, 10));
        ```

        通过使用 PageRequest 对象调用 findByAuthorsName，我们将获得结果的第一页（页码为零），该页最多包含 10 篇文章。页面对象还提供了查询的总点击数以及其他方便的分页信息。

    2. 自定义查询

        有几种方法可以为 Spring Data Elasticsearch 资源库定义自定义查询。一种方法是使用 @Query 注解，如第 2.2 节所示。

        另一种方法是使用查询生成器创建自定义查询。

        如果我们想搜索标题中包含 "data" 一词的文章，只需创建一个 NativeSearchQueryBuilder，并在标题上添加过滤器即可：

        ```java
        Query searchQuery = new NativeSearchQueryBuilder()
        .withFilter(regexpQuery("title", ".*data.*"))
        .build();
        SearchHits<Article> articles = 
        elasticsearchOperations.search(searchQuery, Article.class, IndexCoordinates.of("blog");
        ```

6. 更新和删除

    要更新文档，我们必须先检索该文档：

    ```java
    String articleTitle = "Spring Data Elasticsearch";
    Query searchQuery = new NativeSearchQueryBuilder()
    .withQuery(matchQuery("title", articleTitle).minimumShouldMatch("75%"))
    .build();
    SearchHits<Article> articles = 
    elasticsearchOperations.search(searchQuery, Article.class, IndexCoordinates.of("blog");
    Article article = articles.getSearchHit(0).getContent();
    ```

    然后，我们就可以使用对象的评估器编辑其内容，对文档进行更改：

    ```java
    article.setTitle("Getting started with Search Engines");
    articleRepository.save(article);
    ```

    至于删除，有几种选择。我们可以检索文档，然后使用删除方法将其删除：

    `articleRepository.delete(article);`

    我们还可以在知道id后通过id删除它：

    `articleRepository.deleteById("article_id");`

    还可以创建自定义的 deleteBy 查询，并使用 Elasticsearch 提供的批量删除功能：

    `articleRepository.deleteByTitle("title");`

7. 结论

    在本文中，我们探讨了如何连接和使用 Spring Data Elasticsearch。我们讨论了如何查询、更新和删除文档。最后，我们学习了如何在 Spring Data Elasticsearch 提供的功能无法满足我们的需求时创建自定义查询。
