# [Spring Data MongoDB：投影和聚合](https://www.baeldung.com/spring-data-mongodb-projections-aggregations)

1. 一览表

    Spring Data MongoDB为MongoDB原生查询语言提供了简单的高级抽象。在本文中，我们将探讨对投影和聚合框架的支持。

2. 投影

    在MongoDB中，投影是一种只从数据库中获取文档的必填字段的方法。这减少了必须从数据库服务器传输到客户端的数据量，从而提高了性能。

    使用Spring Data MongDB，投影可以与MongoTemplate和MongoRepository一起使用。

    在我们继续之前，让我们先看看我们将要使用的数据模型：

    ```java
    @Document
    public class User {
        @Id
        private String id;
        private String name;
        private Integer age;
        
        // standard getters and setters
    }
    ```

    1. 使用MongoTemplate的投影

        字段类上的include（）和exclude（）方法分别用于包含和排除字段：

        ```java
        Query query = new Query();
        query.fields().include("name").exclude("id");
        List<User> john = mongoTemplate.find(query, User.class);
        ```

        这些方法可以链在一起，以包含或排除多个字段。除非明确排除，否则标记为@Id（数据库中的_id）的字段总是被获取。

        当用投影获取记录时，在模型类实例中排除的字段为空。如果字段是原始类型或其包装器类，则排除字段的值是原始类型的默认值。

        例如，String将是空的，int/Integer将是0，布尔/布尔将是false。

        因此，在上述示例中，名称字段为John，id为空，年龄为0。

    2. 使用MongoRepository的预测

        在使用MongoRepositories时，@Query注释的字段可以用JSON格式定义：

        ```java
        @Query(value="{}", fields="{name : 1, _id : 0}")
        List<User> findNameAndExcludeId();
        ```

        结果与使用MongoTemplate相同。值=”{}”表示没有过滤器，因此所有文档都将被获取。

3. 聚合

    构建MongoDB中的聚合是为了处理数据并返回计算结果。数据分阶段处理，一个阶段的输出作为下一阶段的输入提供。这种分阶段应用转换和对数据进行计算的能力使聚合成为非常强大的分析工具。

    Spring Data MongoDB使用三个类聚合（包装聚合查询）、包装单个管道阶段的聚合操作和聚合结果（聚合生成结果的容器）为本地聚合查询提供抽象。

    要执行和聚合，首先，在聚合类上使用静态构建器方法创建聚合管道，然后使用聚合类上的newAggregation（）方法创建聚合实例，最后使用MongoTemplate运行聚合：

    ```java
    MatchOperation matchStage = Aggregation.match(new Criteria("foo").is("bar"));
    ProjectionOperation projectStage = Aggregation.project("foo", "bar.baz");

    Aggregation aggregation
    = Aggregation.newAggregation(matchStage, projectStage);

    AggregationResults<OutType> output
    = mongoTemplate.aggregate(aggregation, "foobar", OutType.class);
    ```

    请注意，MatchOperation和ProjectionOperation都实现了AggregationOperation。其他聚合管道也有类似的实现。OutType是预期输出的数据模型。

    现在，我们将看看几个例子及其解释，以涵盖主要的聚合管道和运营商。

    我们将在本文中使用的数据集列出了可以从[MongoDB存储库](http://media.mongodb.org/zips.json)下载的美国所有邮政编码的详细信息。

    在将示例文档导入到测试数据库中称为zips的集合后，让我们看一下。

    ```json
    {
        "_id" : "01001",
        "city" : "AGAWAM",
        "loc" : [
            -72.622739,
            42.070206
        ],
        "pop" : 15338,
        "state" : "MA"
    }
    ```

    为了简单起见和简洁化，在下一个代码片段中，我们将假设聚合类的所有静态方法都是静态导入的。

    1. 按人口下降排序，获取所有人口超过1000万的州

        在这里，我们将有三条管道：

        1. $group 阶段总结所有邮政编码的人口
        2. $match 阶段，以筛选出人口超过1000万的州
        3. $sort 阶段按人口降序对所有文件进行排序

        预期输出将有一个字段_id作为状态，一个字段statePop与总状态人口。让我们为此创建一个数据模型并运行聚合：

        ```java
        public class StatePoulation {
            @Id
            private String state;
            private Integer statePop;
            // standard getters and setters
        }
        ```

        @Id注释将把_id字段从输出映射到模型中的状态：

        ```java
        GroupOperation groupByStateAndSumPop = group("state")
        .sum("pop").as("statePop");
        MatchOperation filterStates = match(new Criteria("statePop").gt(10000000));
        SortOperation sortByPopDesc = sort(Sort.by(Direction.DESC, "statePop"));

        Aggregation aggregation = newAggregation(
        groupByStateAndSumPop, filterStates, sortByPopDesc);
        AggregationResults<StatePopulation> result = mongoTemplate.aggregate(
        aggregation, "zips", StatePopulation.class);
        ```

        AggregationResults类实现了Iterable，因此我们可以迭代并打印结果。

        如果输出数据模型未知，可以使用标准的MongoDB类文档。

    2. 按城市平均人口计算获得最小的州

        对于这个问题，我们需要四个阶段：

        1. $group 将每个城市的总人口相加
        2. $group 来计算每个州的平均人口
        3. $sort 按平均城市人口升序对各州进行排序
        4. $limit 获得第一个平均城市人口最低的州

        虽然不一定需要，但我们将使用额外的$项目阶段根据outStatePopulation数据模型重新格式化文档。

        ```java
        GroupOperation sumTotalCityPop = group("state", "city")
        .sum("pop").as("cityPop");
        GroupOperation averageStatePop = group("_id.state")
        .avg("cityPop").as("avgCityPop");
        SortOperation sortByAvgPopAsc = sort(Sort.by(Direction.ASC, "avgCityPop"));
        LimitOperation limitToOnlyFirstDoc = limit(1);
        ProjectionOperation projectToMatchModel = project()
        .andExpression("_id").as("state")
        .andExpression("avgCityPop").as("statePop");

        Aggregation aggregation = newAggregation(
        sumTotalCityPop, averageStatePop, sortByAvgPopAsc,
        limitToOnlyFirstDoc, projectToMatchModel);

        AggregationResults<StatePopulation> result = mongoTemplate
        .aggregate(aggregation, "zips", StatePopulation.class);
        StatePopulation smallestState = result.getUniqueMappedResult();
        ```

        在本例中，我们已经知道结果中只有一个文档，因为我们在最后阶段将输出文档的数量限制在1。因此，我们可以调用getUniqueMappedResult（）来获取requiredStatePopulation实例。

        另一件需要注意的事情是，我们没有依赖@Id注释将_id映射到状态，而是在投影阶段明确地做到了这一点。

    3. 获取具有最大和最小邮政编码的州

        对于这个例子，我们需要三个阶段：

        1. $group 用于计算每个州的邮政编码数量
        2. $sort 按邮政编码数量对各州进行排序
        3. $group 使用$first和$last运算符查找具有最大和最小邮政编码的状态

        ```java
        GroupOperation sumZips = group("state").count().as("zipCount");
        SortOperation sortByCount = sort(Direction.ASC, "zipCount");
        GroupOperation groupFirstAndLast = group().first("_id").as("minZipState")
        .first("zipCount").as("minZipCount").last("_id").as("maxZipState")
        .last("zipCount").as("maxZipCount");

        Aggregation aggregation = newAggregation(sumZips, sortByCount, groupFirstAndLast);

        AggregationResults<Document> result = mongoTemplate
        .aggregate(aggregation, "zips", Document.class);
        Document document= result.getUniqueMappedResult();
        ```

        在这里，我们没有使用任何模型，而是使用了MongoDB驱动程序中已经提供的文档。

4. 结论

    在本文中，我们学习了如何使用Spring Data MongoDB中的预测在MongoDB中获取文档的指定字段。

    我们还了解了Spring Data中的MongoDB聚合框架支持。我们涵盖了主要的聚合阶段——分组、项目、排序、限制和匹配，并查看了其实际应用的一些示例。
