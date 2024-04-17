# [使用 Elasticsearch 实现简单的标记功能](https://www.baeldung.com/elasticsearch-tagging)

本文是系列文章的一部分：

- 使用 Elasticsearch 实现简单标记（当前文章）
- [使用 JPA 实现简单标记](https://www.baeldung.com/jpa-tagging)
- [使用 JPA 实现高级标记](https://www.baeldung.com/jpa-tagging-advanced)
- [使用 MongoDB 实现简单标记](https://www.baeldung.com/mongodb-tagging)

1. 概述

    标签是一种常见的设计模式，它允许我们对数据模型中的项目进行分类和过滤。

    在本文中，我们将使用 Spring 和 Elasticsearch 实现标记。我们将同时使用 Spring Data 和 Elasticsearch API。

2. 添加标签

    标签的最简单实现是字符串数组。我们可以像这样在数据模型中添加一个新字段来实现：

    ```java
    @Document(indexName = "blog", type = "article")
    public class Article {
        // ...
        @Field(type = Keyword)
        private String[] tags;
        // ...
    }
    ```

    请注意关键字字段类型的使用。我们只希望筛选出与标签完全匹配的结果。这样我们就可以使用类似但独立的标签，如 elasticsearchIsAwesome 和 elasticsearchIsTerrible。

    分析字段会返回部分命中，这在本例中是错误的行为。

3. 构建查询

    标签允许我们以有趣的方式操作查询。我们可以像搜索其他字段一样搜索标签，也可以使用标签过滤 match_all 查询的结果。我们还可以将它们与其他查询一起使用，以强化我们的结果。

    1. 搜索标签

        我们在模型上创建的新标签字段就像索引中的其他字段一样。我们可以像这样搜索具有特定标签的任何实体：

        ```java
        @Query("{\"bool\": {\"must\": [{\"match\": {\"tags\": \"?0\"}}]}}")
        Page<Article> findByTagUsingDeclaredQuery(String tag, Pageable pageable);
        ```

        本示例使用 Spring Data Repository(数据存储库)构建我们的查询，但我们同样可以快速使用 [Rest Template](https://docs.spring.io/spring/docs/3.0.x/javadoc-api/org/springframework/web/client/RestTemplate.html) 手动查询 Elasticsearch 集群。

        同样，我们也可以使用 Elasticsearch API：

        `boolQuery().must(termQuery("tags", "elasticsearch"));`

        假设我们在索引中使用了以下文档：

        ```java
        [
            {
                "id": 1,
                "title": "Spring Data Elasticsearch",
                "authors": [ { "name": "John Doe" }, { "name": "John Smith" } ],
                "tags": [ "elasticsearch", "spring data" ]
            },
            {
                "id": 2,
                "title": "Search engines",
                "authors": [ { "name": "John Doe" } ],
                "tags": [ "search engines", "tutorial" ]
            },
            {
                "id": 3,
                "title": "Second Article About Elasticsearch",
                "authors": [ { "name": "John Smith" } ],
                "tags": [ "elasticsearch", "spring data" ]
            },
            {
                "id": 4,
                "title": "Elasticsearch Tutorial",
                "authors": [ { "name": "John Doe" } ],
                "tags": [ "elasticsearch" ]
            },
        ]
        ```

        现在我们可以使用这个查询了：

        ```java
        Page<Article> articleByTags 
        = articleService.findByTagUsingDeclaredQuery("elasticsearch", PageRequest.of(0, 10));
        // articleByTags will contain 3 articles [ 1, 3, 4]
        assertThat(articleByTags, containsInAnyOrder(
        hasProperty("id", is(1)),
        hasProperty("id", is(3)),
        hasProperty("id", is(4)))
        );
        ```

    2. 过滤所有文档

        一种常见的设计模式是在用户界面中创建一个过滤列表视图(Filtered List View )，显示所有实体，但也允许用户根据不同的条件进行过滤。

        比方说，我们想返回所有经过过滤的文章，无论用户选择的是什么标签：

        ```java
        @Query("{\"bool\": {\"must\": " +
        "{\"match_all\": {}}, \"filter\": {\"term\": {\"tags\": \"?0\" }}}}")
        Page<Article> findByFilteredTagQuery(String tag, Pageable pageable);
        ```

        我们再次使用 Spring Data 来构建我们声明的查询。

        因此，我们使用的查询分为两部分。评分查询是第一个项，在本例中是 match_all。下一个是过滤查询，它告诉 Elasticsearch 哪些结果需要放弃。

        下面是我们使用该查询的方法：

        ```java
        Page<Article> articleByTags =
        articleService.findByFilteredTagQuery("elasticsearch", PageRequest.of(0, 10));
        // articleByTags will contain 3 articles [ 1, 3, 4]
        assertThat(articleByTags, containsInAnyOrder(
        hasProperty("id", is(1)),
        hasProperty("id", is(3)),
        hasProperty("id", is(4)))
        );
        ```

        需要注意的是，虽然返回的结果与上面的示例相同，但这个查询的性能会更好。

    3. 过滤查询

        有时，搜索返回的结果太多，无法使用。在这种情况下，最好能提供一种过滤机制，可以重新运行相同的搜索，只是将结果范围缩小。

        下面是一个例子，我们将作者所写的文章缩小到只有带有特定标签的文章：

        ```java
        @Query("{\"bool\": {\"must\": " + 
        "{\"match\": {\"authors.name\": \"?0\"}}, " +
        "\"filter\": {\"term\": {\"tags\": \"?1\" }}}}")
        Page<Article> findByAuthorsNameAndFilteredTagQuery(
        String name, String tag, Pageable pageable);
        ```

        同样，Spring Data 为我们完成了所有工作。

        我们也来看看如何自己构建这个查询：

        ```java
        QueryBuilder builder = boolQuery().must(
        nestedQuery("authors", boolQuery().must(termQuery("authors.name", "doe")), ScoreMode.None))
        .filter(termQuery("tags", "elasticsearch"));
        ```

        当然，我们也可以使用同样的技术来过滤文档中的任何其他字段。但标签尤其适合这种使用情况。

        下面是使用上述查询的方法：

        ```java
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder)
        .build();
        List<Article> articles = 
        elasticsearchTemplate.queryForList(searchQuery, Article.class);
        // articles contains [ 1, 4 ]
        assertThat(articleByTags, containsInAnyOrder(
        hasProperty("id", is(1)),
        hasProperty("id", is(4)))
        );
        ```

4. 过滤器上下文

    在创建查询时，我们需要区分查询上下文（Query Context）和筛选上下文（Filter Context）。Elasticsearch 中的每个查询都有一个查询上下文，因此我们应该习惯于看到它们。

    并非每种查询类型都支持过滤上下文。因此，如果我们想对标签进行过滤，就需要知道我们可以使用哪些查询类型。

    bool 查询有两种访问过滤上下文的方式。第一个参数 filter 就是我们上面使用的参数。我们还可以使用 must_not 参数来激活上下文。

    我们可以过滤的下一个查询类型是 constant_score。当我们想用筛选器的结果替换查询上下文，并给每个结果分配相同的分数时，这个参数非常有用。

    最后一种可以基于标签进行筛选的查询类型是筛选聚合。它允许我们根据筛选结果创建聚合组。换句话说，我们可以在聚合结果中按标签对所有文章进行分组。

5. 高级标签

    到目前为止，我们只用最基本的实现方式讨论了标签。下一个合理的步骤是创建本身就是键值对的标签。这将使我们的查询和过滤器变得更加复杂。

    例如，我们可以将标签字段改成这样

    ```java
    @Field(type = Nested)
    private List<Tag> tags;
    ```

    然后，我们只需将过滤器改为使用嵌套查询类型即可。

    一旦我们了解了如何使用键值对，就可以使用复杂对象作为我们的标记了。并不是很多实现都需要一个完整的对象作为标签，但如果我们需要的话，知道我们有这个选项还是很不错的。

6. 总结

    在本文中，我们介绍了使用 Elasticsearch 实现标签的基础知识。
