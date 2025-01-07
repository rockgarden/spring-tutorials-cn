# [使用 Solr 进行全文搜索](https://www.baeldung.com/full-text-search-with-solr)

1. 概述

    本文将探讨 [Apache Solr](http://lucene.apache.org/solr/) 搜索引擎的一个基本概念--全文搜索。

    Apache Solr 是一个开源框架，设计用于处理数以百万计的文档。我们将使用 Java 库 [SolrJ](https://cwiki.apache.org/confluence/display/solr/Solrj) 举例说明其核心功能。

2. Maven 配置

    鉴于 Solr 是开源的，我们只需下载二进制文件，然后与应用程序分开启动服务器即可。

    为了与服务器通信，我们将为 SolrJ 客户端定义 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.solr</groupId>
        <artifactId>solr-solrj</artifactId>
        <version>6.4.2</version>
    </dependency>
    ```

3. 索引数据

    要索引和搜索数据，我们需要创建一个核心；我们将创建一个名为 item 的核心来索引我们的数据。

    在此之前，我们需要在服务器上为数据建立索引，这样数据才能被搜索到。

    有许多不同的方法可以为数据建立索引。我们可以使用数据导入处理程序直接从关系数据库导入数据，使用 Apache Tika 通过 Solr Cell 上传数据，或者使用索引处理程序上传 XML/XSLT、JSON 和 CSV 数据。

    1. 索引 Solr 文档

        我们可以通过创建 SolrInputDocument 将数据索引到核心中。首先，我们需要用数据填充文档，然后只需调用 SolrJ 的 API 就能为文档建立索引：

        ```java
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", id);
        doc.addField("description", description);
        doc.addField("category", category);
        doc.addField("price", price);
        solrClient.add(doc);
        solrClient.commit();
        ```

        请注意，不同项目的 id 自然应该是唯一的。拥有已索引文档的 id 将更新该文档。

    2. 索引类

        SolrJ 提供了索引 Java Bean 的 API。要索引一个 Bean，我们需要用 @Field 注解来注解它：

        ```java
        public class Item {
            @Field
            private String id;
            @Field
            private String description;
            @Field
            private String category;
            @Field
            private float price;
        }
        ```

        有了 Bean 后，编制索引就很简单了：

        ```java
        solrClient.addBean(item); 
        solrClient.commit();
        ```

4. Solr 查询

    搜索是 Solr 最强大的功能。在资源库中建立文档索引后，我们就可以搜索关键字、短语、日期范围等。搜索结果按相关性（得分）排序。

    1. 基本查询

        服务器为搜索操作提供了一个 API。我们可以调用 /select 或 /query 请求处理程序。

        让我们做一个简单的搜索：

        ```java
        SolrQuery query = new SolrQuery();
        query.setQuery("brand1");
        query.setStart(0);
        query.setRows(10);
        QueryResponse response = solrClient.query(query);
        List<Item> items = response.getBeans(Item.class);
        ```

        SolrJ 将在内部使用主查询参数 q 向服务器发出请求。返回的记录数为 10，在未指定 start 和 rows 时，索引从 0 开始。

        上述搜索查询将查找任何索引字段中包含完整单词 “brand1” 的文档。请注意，简单搜索不区分大小写。

        我们再来看一个例子。我们想搜索任何包含 “rand” 的单词，该单词以任意数量的字符开始，仅以一个字符结束。我们可以在查询中使用通配符 * 和 ?

        `query.setQuery("*rand?");`

        Solr 查询也支持布尔操作符，就像在 SQL 中一样：

        `query.setQuery("brand1 AND (Washing OR Refrigerator)");`

        所有布尔操作符都必须使用大写字母；查询解析器支持的布尔操作符有 AND、OR、NOT、+ 和 -。

        此外，如果我们想搜索特定字段而不是所有索引字段，可以在查询中指定这些字段：

        `query.setQuery("description:Brand* AND category:*Washing*");`

    2. 短语查询

        到此为止，我们的代码在索引字段中查找关键词。我们还可以在索引字段中进行短语搜索：

        `query.setQuery("Washing Machine");`

        当我们使用 “Washing Machine” 这样的短语时，Solr 的标准查询解析器会将其解析为 “Washing OR Machine”。要搜索整个短语，我们只能在双引号内添加表达式：

        `query.setQuery("\"Washing Machine\"");`

        我们可以使用近似搜索来查找特定距离内的单词。如果我们想查找相距至少两个词的单词，可以使用下面的查询：

        `query.setQuery("\"Washing equipment\"~2");`

    3. 范围查询

        范围查询允许获取字段位于特定范围之间的文档。

        比方说，我们要查找价格在 100 到 300 之间的项目：

        `query.setQuery("price:[100 TO 300]");`

        上面的查询将查找价格在 100 到 300 之间（包括 100 和 300）的所有元素。我们可以使用“`}`”和“`{`”来排除端点：

        `query.setQuery("price:{100 TO 300]");`

    4. 过滤查询

        过滤查询可用于限制返回结果的超集。过滤查询不会影响得分：

        ```java
        SolrQuery query = new SolrQuery();
        query.setQuery("price:[100 TO 300]");
        query.addFilterQuery("description:Brand1","category:Home Appliances");
        ```

        一般来说，过滤查询将包含常用查询。由于这些查询通常可以重复使用，因此会被缓存起来，以提高搜索效率。

5. 分面搜索

    分面有助于将搜索结果排列成组计数。我们可以对字段、查询或范围进行分面。

    1. 字段分面

        例如，我们想获得搜索结果中类别的汇总计数。我们可以在查询中添加类别字段：

        ```java
        query.addFacetField("category");
        QueryResponse response = solrClient.query(query);
        List<Count> facetResults = response.getFacetField("category").getValues();
        ```

        facetResults 将包含结果中每个类别的计数。

    2. 查询分面

        当我们想返回子查询的计数时，查询分面非常有用：

        ```java
        query.addFacetQuery("Washing OR Refrigerator");
        query.addFacetQuery("Brand2");
        QueryResponse response = solrClient.query(query);
        Map<String,Integer> facetQueryMap = response.getFacetQuery();
        ```

        结果，facetQueryMap 将包含面查询的计数。

    3. 范围分面

        范围分面用于获取搜索结果中的范围计数。下面的查询将返回价格范围在 100 到 251 之间的计数，间距为 25：

        ```java
        query.addNumericRangeFacet("price", 100, 275, 25);
        QueryResponse response = solrClient.query(query);
        List<RangeFacet> rangeFacets =  response.getFacetRanges().get(0).getCounts();
        ```

        除数字范围外，Solr 还支持日期范围、区间切面和透视切面。

6. 命中高亮

    我们可能希望搜索结果中能够高亮显示搜索查询中的关键词。这将有助于更好地理解搜索结果。让我们索引一些文档并定义要高亮显示的关键词：

    ```java
    itemSearchService.index("hm0001", "Brand1 洗衣机", "家用电器", 100f);
    itemSearchService.index("hm0002", "Brand1 冰箱", "家用电器", 300f);
    itemSearchService.index("hm0003", "Brand2 吊扇", "家用电器", 200f);
    itemSearchService.index("hm0004", "Brand2 洗碗机", "洗涤设备", 250f);

    SolrQuery query = new SolrQuery();
    query.setQuery("电器");
    query.setHighlight(true);
    query.addHighlightField("category");
    QueryResponse response = solrClient.query(query);

    Map<String, Map<String, List<String>>> hitHighlightedMap = response.getHighlighting();
    Map<String, List<String>> highlightedFieldMap = hitHighlightedMap.get("hm0001");
    List<String> highlightedList = highlightedFieldMap.get("category");
    String highLightedText = highlightedList.get(0);
    ```

    我们将得到 highLightedText 为 "`Home <em>Appliances</em>`"。请注意，搜索关键词“Appliances”被 `<em>` 标签包裹。Solr 默认使用 `<em>` 作为高亮标签，但我们可以通过设置前置和后置标签来更改：

    ```java
    query.setHighlightSimplePre("<strong>");
    query.setHighlightSimplePost("</strong>");
    ```

    标准搜索处理程序不包含拼写检查组件；必须手动配置。配置方法有三种。你可以在官方维基页面找到配置细节。在我们的示例中，我们将使用 IndexBasedSpellChecker，它使用索引数据进行关键词拼写检查。

7. 搜索建议

    Solr 支持的一个重要功能是搜索建议。如果查询中的关键词存在拼写错误，或者我们想要自动补全搜索关键词，可以使用建议功能。

    1. 拼写检查

        标准搜索处理程序不包含拼写检查组件，需要手动配置。有三种方式可以实现。你可以在官方 [wiki](https://cwiki.apache.org/confluence/display/solr/Spell+Checking) 页面找到配置细节。在我们的示例中，我们将使用 IndexBasedSpellChecker，它使用索引数据进行关键词拼写检查。

        让我们搜索一个拼写错误的关键词：

        ```java
        query.setQuery("hme");
        query.set("spellcheck", "on");
        QueryResponse response = solrClient.query(query);

        SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
        Suggestion suggestion = spellCheckResponse.getSuggestions().get(0);
        List<String> alternatives = suggestion.getAlternatives();
        String alternative = alternatives.get(0);
        ```

        对于关键词“hme”，预期的替代词应该是“home”，因为我们的索引中包含“home”这个词。请注意，在执行搜索之前必须激活拼写检查。

    2. 自动建议

        我们可能希望获取不完整关键词的建议以辅助搜索。Solr 的建议组件需要手动配置。你可以在其官方 [wiki](https://cwiki.apache.org/confluence/display/solr/Suggester) 页面找到配置细节。

        我们已经配置了一个名为 /suggest 的请求处理程序来处理建议。让我们获取关键词“Hom”的建议：

        ```java
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/suggest");
        query.set("suggest", "true");
        query.set("suggest.build", "true");
        query.set("suggest.dictionary", "mySuggester");
        query.set("suggest.q", "Hom");
        QueryResponse response = solrClient.query(query);
                
        SuggesterResponse suggesterResponse = response.getSuggesterResponse();
        Map<String,List<String>> suggestedTerms = suggesterResponse.getSuggestedTerms();
        List<String> suggestions = suggestedTerms.get("mySuggester");
        ```

        suggestions 列表应包含所有单词和短语。请注意，我们在配置中配置了一个名为 mySuggester 的建议器。

8. 结论

    本文是对 Solr 搜索引擎功能和特性的快速介绍。

    我们涉及了许多功能，但这些当然只是 Solr 这样一个先进且成熟的搜索服务器所能实现的冰山一角。
