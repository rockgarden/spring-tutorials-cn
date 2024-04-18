# [使用 Spring Data 进行 Elasticsearch 查询](https://www.baeldung.com/spring-data-elasticsearch-queries)

1. 简介

    在本文中，我们将研究 Elasticsearch 提供的几种查询类型，还将讨论字段分析器及其对搜索结果的影响。

2. 分析器

    默认情况下，所有存储的字符串字段都由分析器处理。分析器由一个令牌化器和多个令牌过滤器组成，前面通常还有一个或多个字符过滤器。

    默认的分析器会用常见的单词分隔符（如空格或标点符号）分割字符串，并将每个标记符小写。它还会忽略常见的英文单词。

    Elasticsearch 也可以配置为同时将一个字段视为已分析和未分析字段。

    例如，在文章类中，假设我们将标题字段存储为标准分析字段。带有后缀 verbatim 的同一字段将被存储为未分析字段：

    ```java
    @MultiField(
    mainField = @Field(type = Text, fielddata = true),
    otherFields = {
        @InnerField(suffix = "verbatim", type = Keyword)
    }
    )
    private String title;
    ```

    在此，我们应用 @MultiField 注解来告诉 Spring Data，我们希望以多种方式对该字段进行索引。主要字段将使用 title 名称，并将根据上述规则进行分析。

    但我们还提供了第二个注解 @InnerField，用于描述标题字段的附加索引。我们使用 FieldType.keyword 表示，在对字段进行附加索引时，我们不想使用分析器，该值应使用后缀为 verbatim 的嵌套字段存储。

    1. 分析字段

        我们来看一个例子。假设有一篇标题为 "Spring Data Elasticsearch" 的文章被添加到我们的索引中。默认分析器会在空格字符处拆分字符串并生成小写标记： "spring"、"data" 和 "elasticsearch"。

        现在，我们可以使用这些术语的任意组合来匹配文档：

        ```java
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title", "elasticsearch data"))
        .build();
        ```

    2. 非分析字段

        非分析字段没有标记，因此只能在使用匹配或术语查询时作为一个整体进行匹配：

        ```java
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title.verbatim", "Second Article About Elasticsearch"))
        .build();
        ```

        使用匹配查询，我们可以只搜索标题全文，这也是大小写敏感的。

3. 匹配查询

    匹配查询接受文本、数字和日期。

    "match"查询有三种类型：

    - 布尔 boolean
    - 短语和 phrase and
    - 短语前缀 phrase_prefix

    在本节中，我们将探讨布尔匹配查询。

    1. 使用布尔操作符匹配

        布尔操作符是匹配查询的默认类型；你可以指定使用哪种布尔操作符（或将其作为默认类型）：

        ```java
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title","Search engines").operator(Operator.AND))
        .build();
        SearchHits<Article> articles = elasticsearchTemplate()
        .search(searchQuery, Article.class, IndexCoordinates.of("blog"));
        ```

        通过使用和运算符指定标题中的两个术语，该查询将返回标题为 "“Search engines" 的文章。但是，如果我们使用默认（或）操作符进行搜索，当只有一个词匹配时，会发生什么情况呢？

        ```java
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title", "Engines Solutions"))
        .build();
        SearchHits<Article> articles = elasticsearchTemplate()
        .search(searchQuery, Article.class, IndexCoordinates.of("blog"));
        assertEquals(1, articles.getTotalHits());
        assertEquals("Search engines", articles.getSearchHit(0).getContent().getTitle());
        ```

        "Search engines"文章仍然匹配，但分数较低，因为并非所有术语都匹配。

        每个匹配词的得分加起来就是每个结果文档的总得分。

        在某些情况下，查询中包含一个罕见术语的文档的排名可能会高于包含几个常见术语的文档。

    2. 模糊性

        当用户在某个词中输入错别字时，仍然可以通过指定模糊度参数进行搜索匹配，模糊度参数允许不精确匹配。

        对于字符串字段，模糊度(fuzziness)指的是编辑距离：要使一个字符串与另一个字符串相同，需要对一个字符进行修改的次数。

        ```java
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(matchQuery("title", "spring date elasticsearch")
        .operator(Operator.AND)
        .fuzziness(Fuzziness.ONE)
        .prefixLength(3))
        .build();
        ```

        前缀长度参数用于提高性能。在本例中，我们要求前三个字符必须完全匹配，从而减少了可能的组合数量。

4. 相位搜索

    相位(Phase)搜索更为严格，但可以通过斜率参数进行控制。该参数告诉短语查询允许术语之间相隔多远，同时仍然认为文档是匹配的。

    换句话说，它表示为了使查询和文档匹配，需要移动术语的次数：

    ```java
    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
    .withQuery(matchPhraseQuery("title", "spring elasticsearch").slop(1))
    .build();
    ```

    这里的查询将匹配标题为 "Spring Data Elasticsearch" 的文档，因为我们将斜率设置为 1。

5. 多重匹配查询

    当你想搜索多个字段时，可以使用 QueryBuilders#multiMatchQuery() 来指定要匹配的所有字段：

    ```java
    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
    .withQuery(multiMatchQuery("tutorial")
        .field("title")
        .field("tags")
        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
    .build();
    ```

    在这里，我们搜索标题和标签字段以查找匹配。

    请注意，这里我们使用的是 "best fields" 评分策略。它将以字段中的最大得分作为文档得分。

6. 聚合

    在我们的Article类中，我们还定义了一个tags字段，该字段未进行分析。我们可以通过聚合轻松创建一个标签云。

    请记住，由于该字段是非分析字段，因此不会对标签进行标记化：

    ```java
    TermsAggregationBuilder aggregation = AggregationBuilders.terms("top_tags")
    .field("tags")
    .order(Terms.Order.count(false));
    SearchSourceBuilder builder = new SearchSourceBuilder().aggregation(aggregation);

    SearchRequest searchRequest = 
    new SearchRequest().indices("blog").types("article").source(builder);
    SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

    Map<String, Aggregation> results = response.getAggregations().asMap();
    StringTerms topTags = (StringTerms) results.get("top_tags");

    List<String> keys = topTags.getBuckets()
    .stream()
    .map(b -> b.getKeyAsString())
    .collect(toList());
    assertEquals(asList("elasticsearch", "spring data", "search engines", "tutorial"), keys);
    ```

7. 总结

    在本文中，我们讨论了分析字段和非分析字段的区别，以及这种区别对搜索的影响。

    我们还了解了 Elasticsearch 提供的几种查询类型，如匹配查询、短语匹配查询、全文搜索查询和布尔查询。

    Elasticsearch 还提供许多其他类型的查询，如地理查询、脚本查询和复合查询。您可以在 Elasticsearch [文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html)中阅读相关信息，并探索 Spring Data Elasticsearch API，以便在代码中使用这些查询。
