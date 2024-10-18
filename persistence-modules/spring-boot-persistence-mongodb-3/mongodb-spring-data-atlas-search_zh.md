# [使用Java驱动程序和Spring Data进行MongoDB Atlas搜索](https://www.baeldung.com/mongodb-spring-data-atlas-search)

1. 介绍

    在本教程中，我们将学习如何使用Java MongoDB驱动程序API使用[Atlas搜索](https://www.mongodb.com/docs/atlas/atlas-search/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)功能。最后，我们将掌握创建查询、分页结果和检索元信息。此外，我们将涵盖使用过滤器细化结果、调整结果分数以及选择要显示的特定字段。

2. 场景和设置

    MongoDB Atlas有一个免费的永久[集群](https://www.mongodb.com/cloud/atlas/register/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)，我们可以用它来测试所有功能。为了展示Atlas Search功能，我们只需要一个服务类。我们将使用MongoTemplate连接到我们的收藏。

    1. 依赖性

        首先，要连接到MongoDB，我们需要spring-boot-starter-data-mongodb：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
            <version>3.1.2</version>
        </dependency>
        ```

    2. 样本数据集

        在本教程中，我们将使用MongoDB Atlas的sample_mflix[样本数据](https://www.mongodb.com/docs/atlas/sample-data/#std-label-sample-data/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)集中的电影集合来简化示例。它包含自20世纪以来的电影数据，这将帮助我们展示Atlas Search的过滤功能。

    3. 使用动态映射创建索引

        为了让Atlas Search工作，我们需要索引。这些可以是静态的，也可以是动态的。静态索引有助于微调，而动态索引是一个很好的通用解决方案。所以，让我们从动态索引开始。

        有[几种方法](https://www.mongodb.com/docs/atlas/atlas-search/create-index/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)可以创建搜索索引（包括[编程](https://www.mongodb.com/docs/atlas/atlas-search/create-index/#create-an-fts-index-programmatically/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)）；我们将使用Atlas UI。在那里，我们可以通过从菜单访问Search，选择我们的集群，然后单击“Go to Atlas Search”来完成此操作：创建一个索引。

        单击“Create Search Index”后，我们将选择JSON编辑器来创建索引。

        最后，在下一个屏幕上，我们选择目标集合，索引名称，并输入索引定义：

        ```json
        {
            "mappings": {
                "dynamic": true
            }
        }
        ```

        在本教程中，我们将为这个索引使用idx-queries这个名字。请注意，如果我们将索引命名为默认值，那么在创建查询时，我们不需要指定其名称。最重要的是，动态映射是更灵活、更频繁更改的[模式](https://www.mongodb.com/docs/atlas/app-services/schemas/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)的简单选择。

        通过将mappings.dynamic设置为true，Atlas Search会自动索引文档中所有动态可索引和支持的[字段类型](https://www.mongodb.com/docs/atlas/atlas-search/define-field-mappings/#std-label-bson-data-chart/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)。虽然动态映射提供了便利，特别是当模式未知时，它们往往会消耗更多的磁盘空间，与静态映射相比，效率可能较低。

    4. 我们的电影搜索服务

        我们将以包含我们电影的一些搜索查询的服务类为基础，从中提取有趣的信息。我们将慢慢将它们构建到更复杂的查询：

        ```java
        @Service
        public class MovieAtlasSearchService {
            private final MongoCollection<Document> collection;
            public MovieAtlasSearchService(MongoTemplate mongoTemplate) {
                MongoDatabase database = mongoTemplate.getDb();
                this.collection = database.getCollection("movies");
            }
            // ...
        }
        ```

        我们所需要的只是为未来的方法提供我们的集合参考。

3. 构建查询

    Atlas搜索查询通过管道阶段创建，由`List<Bson>`表示。最重要的阶段是[Aggregates.search()](https://www.mongodb.com/docs/atlas/atlas-search/query-syntax/#std-label-query-syntax-ref/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)，它接收SearchOperator和可选的SearchOptions对象。由于我们调用了索引idx-queries而不是default，我们必须包含其名称withSearchOptions.searchOptions().index()。否则，我们不会得到错误和结果。

    许多搜索运算符可以定义我们想要如何进行查询。在本例中，我们将使用SearchOperator.text（）执行全文搜索的标签查找电影。我们将使用它来使用SearchPath.fieldPath()搜索全图字段的内容。为了可读性，我们将省略静态导入：

    ```java
    public Collection<Document> moviesByKeywords(String keywords) {
        List<Bson> pipeline = Arrays.asList(
            search(
            text(
                fieldPath("fullplot"), keywords
            ),
            searchOptions()
                .index("idx-queries")
            ),
            project(fields(
            excludeId(),
            include("title", "year", "fullplot", "imdb.rating")
            ))
        );
        return collection.aggregate(pipeline)
        .into(new ArrayList<>());
    }
    ```

    此外，我们管道的第二阶段是[Aggregates.project()](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)，它代表一个[投影](https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/builders/projections/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)。如果未指定，我们的查询结果将包含我们文档中的所有字段。但我们可以设置它，并选择我们希望（或不想）出现在结果中的字段。请注意，指定要包含的字段隐式排除了除_id字段以外的所有其他字段。因此，在这种情况下，我们排除了_id字段，并传递了我们想要的字段列表。请注意，我们还可以指定嵌套字段，如imdb.rating。

    为了执行管道，我们在集合上调用aggrege()。这返回一个我们可以用来迭代结果的对象。最后，为了简单起见，我们调用into（）来迭代结果，并将其添加到集合中，我们返回该集合。请注意，足够大的集合可能会耗尽我们JVM中的内存。稍后，我们将看看如何通过分页结果来消除这种担忧。

    最重要的是，管道阶段顺序很重要。如果我们将项目（）阶段放在搜索（）之前，我们将出现错误。

    让我们来看看在我们的服务上调用`moviesByKeywords(“space cowboy”)`的前两个结果：

    ```json
    [
        {
            "title": "Battle Beyond the Stars",
            "fullplot": "Shad, a young farmer, assembles a band of diverse mercenaries in outer space to defend his peaceful planet from the evil tyrant Sador and his armada of aggressors. Among the mercenaries are Space Cowboy, a spacegoing truck driver from Earth; Gelt, a wealthy but experienced assassin looking for a place to hide; and Saint-Exmin, a Valkyrie warrior looking to prove herself in battle.",
            "year": 1980,
            "imdb": {
                "rating": 5.4
            }
        },
        {
            "title": "The Nickel Ride",
            "fullplot": "Small-time criminal Cooper manages several warehouses in Los Angeles that the mob use to stash their stolen goods. Known as \"the key man\" for the key chain he always keeps on his person that can unlock all the warehouses. Cooper is assigned by the local syndicate to negotiate a deal for a new warehouse because the mob has run out of storage space. However, Cooper's superior Carl gets nervous and decides to have cocky cowboy button man Turner keep an eye on Cooper.",
            "year": 1974,
            "imdb": {
                "rating": 6.7
            }
        },
        (...)
    ]
    ```

    1. 结合搜索运算符

        可以使用SearchOperator.compound（）合并搜索运算符。在本例中，我们将使用它来包含必须和应该的子句。必须条款包含匹配文档的一个或多个条件。另一方面，should条款包含一个或多个条件，我们希望我们的结果包含这些条件。

        这会改变分数，因此首先出现符合这些条件的文件：

        ```java
        public Collection<Document> late90sMovies(String keywords) {
            List<Bson> pipeline = asList(
                search(
                compound()
                    .must(asList(
                    numberRange(
                        fieldPath("year"))
                        .gteLt(1995, 2000)
                    ))
                    .should(asList(
                    text(
                        fieldPath("fullplot"), keywords
                    )
                    )),
                searchOptions()
                    .index("idx-queries")
                ),
                project(fields(
                excludeId(),
                include("title", "year", "fullplot", "imdb.rating")
                ))
            );

            return collection.aggregate(pipeline)
            .into(new ArrayList<>());
        }
        ```

        我们从第一次查询开始保留了相同的searchOptions（）和投影字段。但是，这次，我们将text（）移动到should子句，因为我们希望关键字代表偏好，而不是要求。

        然后，我们创建了一个必须条款，包括SearchOperator.numberRange（），通过限制年份字段中的值，只显示1995年至2000年（独家）的电影。这样，我们只返回那个时代的电影。

        让我们看看黑客刺客的前两个结果：

        ```json
        [
            {
                "title": "Assassins",
                "fullplot": "Robert Rath is a seasoned hitman who just wants out of the business with no back talk. But, as things go, it ain't so easy. A younger, peppier assassin named Bain is having a field day trying to kill said older assassin. Rath teams up with a computer hacker named Electra to defeat the obsessed Bain.",
                "year": 1995,
                "imdb": {
                    "rating": 6.3
                }
            },
            {
                "fullplot": "Thomas A. Anderson is a man living two lives. By day he is an average computer programmer and by night a hacker known as Neo. Neo has always questioned his reality, but the truth is far beyond his imagination. Neo finds himself targeted by the police when he is contacted by Morpheus, a legendary computer hacker branded a terrorist by the government. Morpheus awakens Neo to the real world, a ravaged wasteland where most of humanity have been captured by a race of machines that live off of the humans' body heat and electrochemical energy and who imprison their minds within an artificial reality known as the Matrix. As a rebel against the machines, Neo must return to the Matrix and confront the agents: super-powerful computer programs devoted to snuffing out Neo and the entire human rebellion.",
                "imdb": {
                    "rating": 8.7
                },
                "year": 1999,
                "title": "The Matrix"
            },
            (...)
        ]
        ```

4. 对结果集进行评分

    当我们使用search（）查询文档时，结果会按照相关顺序显示。这种相关性基于[计算分数](https://www.mongodb.com/docs/atlas/atlas-search/atlas-search-overview/#fts-queries/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)，从最高到最低。这一次，我们将修改late90sMovies（）以接收SearchScore修饰符，以提高should子句中情节关键字的相关性：

    ```java
    public Collection<Document> late90sMovies(String keywords, SearchScore modifier) {
        List<Bson> pipeline = asList(
            search(
            compound()
                .must(asList(
                numberRange(
                    fieldPath("year"))
                    .gteLt(1995, 2000)
                ))
                .should(asList(
                text(
                    fieldPath("fullplot"), keywords
                )
                .score(modifier)
                )),
            searchOptions()
                .index("idx-queries")
            ),
            project(fields(
            excludeId(),
            include("title", "year", "fullplot", "imdb.rating"),
            metaSearchScore("score")
            ))
        );

        return collection.aggregate(pipeline)
        .into(new ArrayList<>());
    }
    ```

    此外，我们在字段列表中包含`metaSearchScore(“score”)`，以查看结果中每个文档的分数。例如，我们现在可以将“should”子句的相关性乘以imdb.votes字段的值，如下所示：

    ```java
    late90sMovies(
    "hacker assassin",
    SearchScore.boost(fieldPath("imdb.votes"))
    )
    ```

    这一次，我们可以看到，由于提升，黑客帝国是第一位的：

    ```json
    [
        {
            "fullplot": "Thomas A. Anderson is a man living two lives (...)",
            "imdb": {
                "rating": 8.7
            },
            "year": 1999,
            "title": "The Matrix",
            "score": 3967210.0
        },
        {
            "fullplot": "(...) Bond also squares off against Xenia Onatopp, an assassin who uses pleasure as her ultimate weapon.",
            "imdb": {
                "rating": 7.2
            },
            "year": 1995,
            "title": "GoldenEye",
            "score": 462604.46875
        },
        (...)
    ]
    ```

    1. 使用分数函数

        我们可以通过使用函数来改变结果的分数来实现更大的控制。让我们将一个函数传递给我们的方法，将年字段的值添加到自然分数上。这样，新电影最终会获得更高的分数：

        ```java
        late90sMovies(keywords, function(
        addExpression(asList(
            pathExpression(
            fieldPath("year"))
            .undefined(1),
            relevanceExpression()
        ))
        ));
        ```

        该代码以SearchScore.function（）开头，SearchScoreExpression.addExpression（），因为我们想要添加操作。然后，由于我们想从字段中添加一个值，我们使用SearchScoreExpression.pathExpression（）并指定我们想要的字段：年份。此外，我们调用undefined（）来确定年份的回退值，以防它缺失。最后，我们调用相关性表达（）来返回文档的相关性分数，该分数被添加到年份的值中。

        当我们执行它时，我们将看到“黑客帝国”现在首先出现，以及它的新乐谱：

        ```json
        [
            {
                "fullplot": "Thomas A. Anderson is a man living two lives (...)",
                "imdb": {
                    "rating": 8.7
                },
                "year": 1999,
                "title": "The Matrix",
                "score": 2003.67138671875
            },
            {
                "title": "Assassins",
                "fullplot": "Robert Rath is a seasoned hitman (...)",
                "year": 1995,
                "imdb": {
                    "rating": 6.3
                },
                "score": 2003.476806640625
            },
            (...)
        ]
        ```

        这有助于定义在为我们的结果评分时，什么应该有更大的分量。

5. 从元数据中获取总行数

    如果我们需要在查询中获取结果总数，我们可以使用Aggregates.searchMeta（）而不是search（）来仅检索元数据信息。使用此方法，不会返回任何文档。因此，我们将用它来统计90年代末也包含我们关键词的电影数量。

    为了有意义的过滤，我们还将把关键字包含在我们的must子句中：

    ```java
    public Document countLate90sMovies(String keywords) {
        List<Bson> pipeline = asList(
            searchMeta(
            compound()
                .must(asList(
                numberRange(
                    fieldPath("year"))
                    .gteLt(1995, 2000),
                text(
                    fieldPath("fullplot"), keywords
                )
                )),
            searchOptions()
                .index("idx-queries")
                .count(total())
            )
        );
        return collection.aggregate(pipeline)
        .first();
    }
    ```

    这一次，searchOptions（）包含对SearchOptions.count（SearchCount.total（））的调用，这确保我们获得准确的总计数（而不是下限，根据集合大小更快）。此外，由于我们期望结果中有一个对象，我们在aggregate（）上调用first（）。

    最后，让我们看看countLate90sMovies（“黑客刺客”）的回报是什么：

    ```json
    {
        "count": {
            "total": 14
        }
    }
    ```

    这对于获取有关我们收藏的信息很有用，而无需在我们的结果中包含文件。

6. 结果的刻面

    在MongoDB Atlas Search中，[面(fact)](https://www.mongodb.com/docs/atlas/atlas-search/tutorial/facet-tutorial/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)查询是一种允许检索有关我们搜索结果的汇总和分类信息的功能。它帮助我们根据不同的标准分析和总结数据，提供对搜索结果分布的见解。

    此外，它允许将搜索结果分组到不同的类别或桶中，并检索有关每个类别的计数或其他信息。这有助于回答“有多少文件与特定类别匹配？”等问题。或者“结果中特定字段最常见的值是什么？”

    1. 创建静态索引

        在我们的第一个例子中，我们将创建一个面查询，以向我们提供自20世纪以来电影类型以及这些类型之间的关系的信息。我们需要一个具有面类型的索引，我们在使用动态索引时无法拥有。

        因此，让我们从在我们的集合中创建一个新的搜索索引开始，我们将称之为idx-facets。请注意，我们将保持动态为真，因此我们仍然可以查询未明确定义的字段：

        ```json
        {
        "mappings": {
            "dynamic": true,
            "fields": {
            "genres": [
                {
                "type": "stringFacet"
                },
                {
                "type": "string"
                }
            ],
            "year": [
                {
                "type": "numberFacet"
                },
                {
                "type": "number"
                }
            ]
            }
        }
        }
        ```

        我们首先指定我们的映射不是动态的。然后，我们选择了我们感兴趣的字段来索引多面信息。由于我们也想在查询中使用过滤器，对于每个字段，我们指定一个标准类型的索引（如字符串）和一个面类型（如stringFacet）。

    2. 运行Facet查询

        创建面查询涉及使用searchMeta（）并启动SearchCollector.facet（）方法，以包含我们的面和用于过滤结果的运算符。在定义面时，我们必须选择一个名称，并使用与我们创建的索引类型相对应的SearchFacet方法。在我们的案例中，我们定义了stringFacet（）和numberFacet（）：

        ```java
        public Document genresThroughTheDecades(String genre) {
            List pipeline = asList(
            searchMeta(
                facet(
                text(
                    fieldPath("genres"), genre
                ), 
                asList(
                    stringFacet("genresFacet", 
                    fieldPath("genres")
                    ).numBuckets(5),
                    numberFacet("yearFacet", 
                    fieldPath("year"), 
                    asList(1900, 1930, 1960, 1990, 2020)
                    )
                )
                ),
                searchOptions()
                .index("idx-facets")
            )
            );

            return collection.aggregate(pipeline)
            .first();
        }
        ```

        我们使用text（）运算符过滤特定类型的电影。由于电影通常包含多种类型，stringFacet（）还将显示五个（由numBuckets（）指定）相关类型，按频率排名。对于numberFacet（），我们必须设置分隔汇总结果的边界。我们至少需要两个，最后一个是独家的。

        最后，我们只返回第一个结果。让我们看看如果我们按“horror”类型过滤，它是什么样子：

        ```json
        {
            "count": {
                "lowerBound": 1703
            },
            "facet": {
                "genresFacet": {
                    "buckets": [
                        {
                            "_id": "Horror",
                            "count": 1703
                        },
                        {
                            "_id": "Thriller",
                            "count": 595
                        },
                        {
                            "_id": "Drama",
                            "count": 395
                        },
                        {
                            "_id": "Mystery",
                            "count": 315
                        },
                        {
                            "_id": "Comedy",
                            "count": 274
                        }
                    ]
                },
                "yearFacet": {
                    "buckets": [
                        {
                            "_id": 1900,
                            "count": 5
                        },
                        {
                            "_id": 1930,
                            "count": 47
                        },
                        {
                            "_id": 1960,
                            "count": 409
                        },
                        {
                            "_id": 1990,
                            "count": 1242
                        }
                    ]
                }
            }
        }
        ```

        由于我们没有指定总计数，我们得到一个下限计数，然后是我们的面名称及其各自的桶。

    3. 包括分页结果的刻面阶段

        让我们回到我们的late90sMovies（）方法，并在我们的管道中包含一个[$facet](https://www.mongodb.com/docs/manual/reference/operator/aggregation/facet/?utm_source=email&utm_campaign=java_influencer_baeldung&utm_medium=influencers)阶段。我们将用它来进行分页和总行数。search() 和project() 阶段将保持不变：

        ```java
        public Document late90sMovies(int skip, int limit, String keywords) {
            List<Bson> pipeline = asList(
                search(
                // ...
                ),
                project(fields(
                // ...
                )),
                facet(
                new Facet("rows",
                    skip(skip),
                    limit(limit)
                ),
                new Facet("totalRows",
                    replaceWith("$$SEARCH_META"),
                    limit(1)
                )
                )
            );

            return collection.aggregate(pipeline)
            .first();
        }
        ```

        我们首先调用Aggregates.facet（），它接收一个或多个面。然后，我们实例化一个Facet，以包含来自Aggregates类的skip（）和limit（）。当skip（）定义我们的偏移时，limit（）将限制检索的文档数量。请注意，我们可以给我们的面命名任何我们喜欢的东西。

        此外，我们调用replaceWith(“$$SEARCH_META“)”来获取此字段中的元数据信息。最重要的是，为了不让我们的元数据信息对每个结果重复，我们包含一个限制（1）。最后，当我们的查询有元数据时，结果会变成单个文档而不是数组，因此我们只返回第一个结果。

7. 结论

    在本文中，我们看到了MongoDB Atlas Search如何为开发人员提供多功能且强大的工具集。将其与Java MongoDB驱动程序API集成可以增强搜索功能、数据聚合和结果定制。我们的实践示例旨在对其能力提供实际的了解。无论是实施简单的搜索还是寻求复杂的数据分析，Atlas Search都是MongoDB生态系统中宝贵的工具。
