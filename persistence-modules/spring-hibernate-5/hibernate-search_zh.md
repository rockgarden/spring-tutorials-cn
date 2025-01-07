# [Hibernate Search 简介](https://www.baeldung.com/hibernate-search)

1. 概述

    在本文中，我们将讨论 Hibernate Search 的基础知识、如何配置它，并实现一些简单的查询。

2. Hibernate Search 的基础知识

    每当我们需要实现全文搜索功能时，使用我们已经熟悉的工具总是一个加分项。

    如果我们已经在使用 Hibernate 和 JPA 进行 ORM，那么我们离 Hibernate Search 只有一步之遥。

    Hibernate Search 集成了 Apache Lucene，这是一个用 Java 编写的高性能且可扩展的全文搜索引擎库。它将 Lucene 的强大功能与 Hibernate 和 JPA 的简单性结合在一起。

    简单来说，我们只需要在领域类中添加一些额外的注解，工具就会处理数据库/索引同步等事情。

    Hibernate Search 还提供了 Elasticsearch 集成；然而，由于它仍处于实验阶段，我们将在这里重点介绍 Lucene。

3. 配置

    1. Maven 依赖

        在开始之前，我们首先需要在 pom.xml 中添加必要的依赖项：

        ```xml
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-search-orm</artifactId>
            <version>5.8.2.Final</version>
        </dependency>
        ```

        为了简单起见，我们将使用 H2 作为数据库：

        ```xml
        <dependency>
            <groupId>com.h2database</groupId> 
            <artifactId>h2</artifactId>
            <version>1.4.196</version>
        </dependency>
        ```

    2. 配置

        我们还需要指定 Lucene 存储索引的位置。

        这可以通过属性 `hibernate.search.default.directory_provider` 来完成。

        我们将选择 `filesystem`，这是我们用例中最直接的选项。更多选项列在[官方文档](https://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/#search-configuration-directory)中。对于集群应用程序，`filesystem-master/filesystem-slave` 和 `infinispan` 值得注意，因为索引必须在节点之间同步。

        我们还需要定义一个默认的基本目录来存储索引：

        ```properties
        hibernate.search.default.directory_provider = filesystem
        hibernate.search.default.indexBase = /data/index/default
        ```

4. 模型类

    配置完成后，我们现在可以指定我们的模型。

    在 JPA 注解 `@Entity` 和 `@Table` 之上，我们必须添加一个 `@Indexed` 注解。它告诉 Hibernate Search 实体 `Product` 应该被索引。

    之后，我们必须通过添加 `@Field` 注解来定义所需的属性为可搜索的：

    ```java
    @Entity
    @Indexed
    @Table(name = "product")
    public class Product {

        @Id
        private int id;

        @Field(termVector = TermVector.YES)
        private String productName;

        @Field(termVector = TermVector.YES)
        private String description;

        @Field
        private int memory;

        // getters, setters, and constructors
    }
    ```

    `termVector = TermVector.YES` 属性将在后面的“More Like This”查询中需要。

5. 构建 Lucene 索引

    在开始实际查询之前，我们必须触发 Lucene 初始构建索引：

    ```java
    FullTextEntityManager fullTextEntityManager 
        = Search.getFullTextEntityManager(entityManager);
    fullTextEntityManager.createIndexer().startAndWait();
    ```

    在此初始构建之后，Hibernate Search 将负责保持索引的最新状态。也就是说，我们可以像往常一样通过 `EntityManager` 创建、操作和删除实体。

    注意：我们必须确保实体在提交到数据库后才能被 Lucene 发现和索引（顺便说一下，这也是为什么我们的示例代码测试用例中的初始测试数据导入在一个专用的 JUnit 测试用例中，并用 `@Commit` 注解的原因）。

6. 构建和执行查询

    现在，我们准备好创建我们的第一个查询。

    在以下部分中，我们将展示准备和执行查询的一般工作流程。

    之后，我们将为最重要的查询类型创建一些示例查询。

    1. 创建和执行查询的一般工作流程

        准备和执行查询通常包括四个步骤：

        在步骤 1 中，我们必须获取一个 JPA `FullTextEntityManager`，然后从中获取一个 `QueryBuilder`：

        ```java
        FullTextEntityManager fullTextEntityManager 
            = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory() 
            .buildQueryBuilder()
            .forEntity(Product.class)
            .get();
        ```

        在步骤 2 中，我们将通过 Hibernate 查询 DSL 创建一个 Lucene 查询：

        ```java
        org.apache.lucene.search.Query query = queryBuilder
            .keyword()
            .onField("productName")
            .matching("iphone")
            .createQuery();
        ```

        在步骤 3 中，我们将 Lucene 查询包装成 Hibernate 查询：

        ```java
        org.hibernate.search.jpa.FullTextQuery jpaQuery
            = fullTextEntityManager.createFullTextQuery(query, Product.class);
        ```

        最后，在步骤 4 中，我们将执行查询：

        ```java
        List<Product> results = jpaQuery.getResultList();
        ```

        注意：默认情况下，Lucene 按相关性对结果进行排序。

        步骤 1、3 和 4 对于所有查询类型都是相同的。

        在下面，我们将重点介绍步骤 2，即如何创建不同类型的查询。

    2. 关键词查询

        最基本的用例是搜索特定的单词。

        这实际上是我们在前一节中已经做过的事情：

        ```java
        Query keywordQuery = queryBuilder
            .keyword()
            .onField("productName")
            .matching("iphone")
            .createQuery();
        ```

        在这里，`keyword()` 指定我们正在寻找一个特定的单词，`onField()` 告诉 Lucene 在哪里查找，`matching()` 告诉 Lucene 查找什么。

    3. 模糊查询

        模糊查询的工作方式类似于关键词查询，只是我们可以定义一个“模糊度”限制，超过这个限制，Lucene 将接受两个术语为匹配。

        通过 `withEditDistanceUpTo()`，我们可以定义一个术语可以偏离另一个术语的程度。它可以设置为 0、1 和 2，默认值为 2（注意：这个限制来自 Lucene 的实现）。

        通过 `withPrefixLength()`，我们可以定义前缀的长度，该前缀将被模糊度忽略：

        ```java
        Query fuzzyQuery = queryBuilder
            .keyword()
            .fuzzy()
            .withEditDistanceUpTo(2)
            .withPrefixLength(0)
            .onField("productName")
            .matching("iPhaen")
            .createQuery();
        ```

    4. 通配符查询

        Hibernate Search 还使我们能够执行通配符查询，即查询单词的一部分未知。

        为此，我们可以使用“?” 表示单个字符，使用“*” 表示任意字符序列：

        ```java
        Query wildcardQuery = queryBuilder
            .keyword()
            .wildcard()
            .onField("productName")
            .matching("Z*")
            .createQuery();
        ```

    5. 短语查询

        如果我们想搜索多个单词，我们可以使用短语查询。我们可以使用 `phrase()` 和 `withSlop()`（如果需要）来查找精确或近似的句子。slop 因子定义了句子中允许的其他单词的数量：

        ```java
        Query phraseQuery = queryBuilder
            .phrase()
            .withSlop(1)
            .onField("description")
            .sentence("with wireless charging")
            .createQuery();
        ```

    6. 简单查询字符串查询

        对于之前的查询类型，我们必须明确指定查询类型。

        如果我们想给用户更多的权力，我们可以使用简单查询字符串查询：通过这种方式，用户可以在运行时定义自己的查询。

        支持以下查询类型：

        - 布尔值（AND 使用“+”，OR 使用“|”，NOT 使用“-”）
        - 前缀（prefix*）
        - 短语（“some phrase”）
        - 优先级（使用括号）
        - 模糊（fuzy~2）
        - 短语查询的近运算符（“some phrase”~3）

        以下示例将结合模糊、短语和布尔查询：

        ```java
        Query simpleQueryStringQuery = queryBuilder
            .simpleQueryString()
            .onFields("productName", "description")
            .matching("Aple~2 + \"iPhone X\" + (256 | 128)")
            .createQuery();
        ```

    7. 范围查询

        范围查询搜索给定边界之间的值。这可以应用于数字、日期、时间戳和字符串：

        ```java
        Query rangeQuery = queryBuilder
            .range()
            .onField("memory")
            .from(64).to(256)
            .createQuery();
        ```

    8. 更多类似此查询

        我们的最后一个查询类型是“More Like This”查询。为此，我们提供一个实体，Hibernate Search 返回一个包含类似实体的列表，每个实体都有一个相似度分数。

        如前所述，我们的模型类中的 `termVector = TermVector.YES` 属性在这种情况下是必需的：它告诉 Lucene 在索引期间存储每个术语的频率。

        基于此，相似度将在查询执行时计算：

        ```java
        Query moreLikeThisQuery = queryBuilder
            .moreLikeThis()
            .comparingField("productName").boostedTo(10f)
            .andField("description").boostedTo(1f)
            .toEntity(entity)
            .createQuery();
        List<Object[]> results = (List<Object[]>) fullTextEntityManager
            .createFullTextQuery(moreLikeThisQuery, Product.class)
            .setProjection(ProjectionConstants.THIS, ProjectionConstants.SCORE)
            .getResultList();
        ```

    9. 搜索多个字段

        到目前为止，我们只创建了使用 `onField()` 搜索一个属性的查询。

        根据用例，我们还可以搜索两个或多个属性：

        ```java
        Query luceneQuery = queryBuilder
            .keyword()
            .onFields("productName", "description")
            .matching(text)
            .createQuery();
        ```

        此外，我们可以单独指定每个要搜索的属性，例如，如果我们想为一个属性定义 boost：

        ```java
        Query moreLikeThisQuery = queryBuilder
            .moreLikeThis()
            .comparingField("productName").boostedTo(10f)
            .andField("description").boostedTo(1f)
            .toEntity(entity)
            .createQuery();
        ```

    10. 组合查询

        最后，Hibernate Search 还支持使用各种策略组合查询：

        - `SHOULD`：查询应包含子查询的匹配元素
        - `MUST`：查询必须包含子查询的匹配元素
        - `MUST NOT`：查询不得包含子查询的匹配元素

        聚合类似于布尔值的 AND、OR 和 NOT。然而，名称不同是为了强调它们也会影响相关性。

        例如，两个查询之间的 `SHOULD` 类似于布尔 OR：如果两个查询中的一个有匹配项，则将返回此匹配项。

        然而，如果两个查询都匹配，则匹配项的相关性将比只有一个查询匹配时更高：

        ```java
        Query combinedQuery = queryBuilder
            .bool()
            .must(queryBuilder.keyword()
                .onField("productName").matching("apple")
                .createQuery())
            .must(queryBuilder.range()
                .onField("memory").from(64).to(256)
                .createQuery())
            .should(queryBuilder.phrase()
                .onField("description").sentence("face id")
                .createQuery())
            .must(queryBuilder.keyword()
                .onField("productName").matching("samsung")
                .createQuery())
            .not()
            .createQuery();
        ```

7. 结论

    在本文中，我们讨论了 Hibernate Search 的基础知识，并展示了如何实现最重要的查询类型。更多高级主题可以在[官方文档](https://docs.jboss.org/hibernate/stable/search/reference/en-US/html_single/)中找到。
