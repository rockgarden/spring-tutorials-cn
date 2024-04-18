# [ElasticSearch 中的地理空间支持](https://www.baeldung.com/elasticsearch-geo-spatial)

1. 简介

    在本文中，我们将学习如何使用 Elasticsearch 的地理空间功能。

    我们不会深入探讨如何设置 Elasticsearch 实例和 Java 客户端。相反，我们将介绍如何保存地理数据以及如何使用地理查询进行搜索。

    让我们深入了解可用的地理数据类型。

2. 地理数据类型

    在 Elasticsearch 中，我们可以处理两种主要的地理数据类型：geo_point 由经纬度坐标组成，geo_shape 可以描述矩形、直线和多边形等不同形状。我们必须手动创建索引映射，并明确设置字段映射以使用地理查询才能继续。此外，需要注意的是，在为地理类型设置映射时，动态映射将不起作用。

    接下来，让我们深入了解每种地理数据类型的具体细节。

    1. 地理点数据类型

        最简单的类型是地理点（geo_point），它表示地图上的一对经纬度。我们可以通过多种方式使用点，例如查看它是否在一个方框内，或搜索距离所代表的特定范围内的对象。此外，我们还可以在表示复杂地理形状的查询中搜索索引点。举例来说，我们可以将地理点视为地图上的一个针尖。

        此外，地理点还允许我们按位置（包括特定区域内或与指定点的距离）对文档进行分组，然后对文档进行相应的排序。例如，将对象从离我们的点较近的地方排到较远的地方。

        首先要记住的是，使用索引数据类型的属性创建 geo_point 的映射：

        ```json
        PUT /index_name
        {
            "mappings": {
                "TYPE_NAME": {
                    "properties": {
                        "location": { 
                            "type": "geo_point" 
                        } 
                    } 
                } 
            } 
        }
        ```

        这样，我们就可以将数据点发送到 Elasticsearch 了。

    2. 地理形状数据类型

        与地理点不同，地理形状提供了保存和搜索多边形和矩形等复杂形状的功能。要搜索包含地理点以外形状的文档，我们必须使用 geo_shape 数据类型。

        同样，让我们在索引的数据类型中映射地理形状：

        ```json
        PUT /index_name
        {
            "mappings": {
                "TYPE_NAME": {
                    "properties": {
                        "location": {
                            "type": "geo_shape"
                        }
                    }
                }
            }
        }
        ```

        Elasticsearch 将 geo_shape 表示为三角形网格，这样就可以提供非常高的空间分辨率。

        接下来，我们将了解如何在索引中保存数据。

3. 保存地理点数据的不同方法

    假设我们在索引中将一个位置类型映射为一个地理点，那么我们可以通过以下方式保存地理点数据：

    1. 纬度经度对象

        我们可以明确定义点的经度和纬度，将它们作为位置类型的键：

        ```json
        PUT index_name/_doc
        {
            "location": { 
                "lat": 23.02,
                "lon": 72.57
            }
        }
        ```

        这是最可读的方法，不会增加任何歧义。

    2. 纬度-经度对

        我们可以减少前一种方法的冗长，用纯字符串格式定义纬度-经度对：

        ```json
        {
            "location": "23.02,72.57"
        }
        ```

        需要强调的是，字符串地理坐标的排序方式是 lat、lon，而数组地理坐标、GeoJSON 和 WKT 的排序方式正好相反：lon、lat。

    3. 经度纬度数组

        或者，我们也可以以数组的形式提供点：

        ```json
        {
            "location": [72.57, 23.02]
        }
        ```

        需要注意的是，以数组形式提供纬度和经度时，纬度-经度的顺序是相反的。

        最初，纬度-经度对在字符串和数组中都使用，但后来为了与 [GeoJSON](https://en.wikipedia.org/wiki/GeoJSON) 使用的格式相匹配，纬度-经度对的顺序被颠倒了。

    4. 地理散列

        最后，我们可以使用 Geo Hash 代替显式的对值来表示我们的点：

        ```json
        {
            "location": "tsj4bys"
        }
        ```

        尽管哈希值简洁明了，非常适合近似搜索，但它们的可读性并不高。例如，我们可以使用[在线工具](http://www.movable-type.co.uk/scripts/geohash.html)将纬度-经度转换为地理哈希值。

4. 保存地理形状数据的不同方法

    假设我们在索引中将一个区域类型映射为地理形状。

    1. 点

        我们首先创建最简单的形状，即点：

        ```json
        POST /index/_doc
        {
            "region" : {
                "type" : "point",
                "coordinates" : [72.57, 23.02]
            }
        }
        ```

        简而言之，在区域字段内部，我们有一个由字段类型和坐标组成的嵌套对象。这些元字段尤其有助于 Elasticsearch 识别数据。

    2. 行字符串

        然后，我们要插入一个行字符串：

        ```json
        POST /index/_doc
        {
            "region" : {
                "type" : "linestring",
                "coordinates" : [[77.57, 23.02], [77.59, 23.05]]
            }
        }
        ```

        简而言之，LineString 的坐标是代表线段起点和终点的两个点。在创建导航系统时，聚合多个 LineString 会很有帮助。

    3. 多边形

        接下来，我们将插入一个多边形地理形状：

        ```json
        POST /index/_doc
        {
            "region" : {
                "type" : "polygon",
                "coordinates" : [
                    [ [10.0, 0.0], [11.0, 0.0], [11.0, 1.0], [10.0, 1.0], [10.0, 0.0] ]
                ]
            }
        }
        ```

        注意本例的首尾坐标，我们必须确保匹配的是一个封闭的多边形。

    4. 其他 GeoJSON/WKT 格式

        最后，Elasticsearch 支持的 GeoJSON/WKT 结构列表非常丰富：

        - MultiPoint
        - MultiLineString
        - MultiPolygon
        - GeometryCollection
        - Envelope（这不是有效的 GeoJSON，但 Elasticsearch 和 WKT 支持它）

        此外，我们还可以在 ES 官方[网站](https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html#input-structure)上查看所有支持的格式。

        总之，我们必须提供内部类型和坐标字段才能正确索引文档。此外，由于地理形状字段结构复杂，Elasticsearch 目前无法对其进行排序和检索。因此，检索地理字段的唯一方法就是从源字段开始。

5. 在 ElasticSearch 中插入地理数据

    现在，让我们插入一些文档，并学习如何使用地理查询来获取它们。首先，我们必须添加 Elastic search 的 Java 客户端：

    ```xml
    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>8.9.0</version>
    </dependency>
    ```

    1. 使用显式映射创建索引

        在插入数据之前，我们需要定义索引的映射：

        ```java
        client.indices().create(builder -> builder.index(WONDERS_OF_WORLD)
        .mappings(bl -> bl
            .properties("region", region -> region.geoShape(gs -> gs))
            .properties("location", location -> location.geoPoint(gp -> gp))
        )
        );
        ```

        在这里，客户端是 ElasticsearchClient 对象的一个实例。简而言之，我们正在创建两种数据类型。第一个是名为区域的地理形状，第二个是名为位置的地理点。

    2. 插入地理点文档

        首先，我们创建一个 Java 类来表示我们的地理点数据：

        main/.elasticsearch/Location.java

        详细来说，name 指的是位置的名称，而 list location 则是代表位置的两个值。此外，我们使用 Lombok 来保持代码的简洁。

        现在，我们可以使用 index() 方法为新文档建立索引：

        ```java
        Location pyramidsOfGiza = new Location("Pyramids of Giza", List.of(31.1328, 29.9761));
        IndexResponse response = client.index(builder -> builder
        .index(WONDERS_OF_WORLD)
        .document(pyramidsOfGiza));
        ```

        此外，.document() 会自动将 Location 对象转换为有效的 JSON 格式。或者，我们也可以直接处理 JSON 字符串，使用 .withJson() 并将字符串作为字符串阅读器提供：

        ```java
        String jsonObject = """
            {
                "name":"Lighthouse of alexandria",
                "location":{ "lat": 31.2139, "lon": 29.8856 }
            }
            """;
        IndexResponse response = client.index(idx -> idx
        .index(WONDERS_OF_WORLD)
        .withJson(new StringReader(jsonObject)));
        ```

    3. 插入地理形状文档

        接下来，要插入 geo_shape 文档，我们可以直接使用 JSON 字符串：

        ```java
        String jsonObject = """
            {
                "name":"Agra",
                "region":{
                    "type":"envelope",
                    "coordinates":[[75,30.2],[80.1,25]]
                }
            }
            """;
        IndexResponse response = client.index(idx -> idx
            .index(WONDERS_OF_WORLD)
            .withJson(new StringReader(jsonObject)));
        ```

        现在，我们可以进行一些查询来搜索数据了。

6. 在 ElasticSearch 中查询地理数据

    1. 地理边框查询

        首先，假设我们在地图上有一堆地理点，我们想在一个矩形区域内找到它们。那么，我们必须使用边界框查询来获取所有点：

        ```json
        {
        "query":{
            "geo_bounding_box":{
                "location":{
                    "top_left":[30.0,31.0],
                    "bottom_right":[32.0,28.0]
                }
            }
        }
        }
        ```

        此外，我们还可以为此在项目中创建一个 SearchRequest：

        ```java
        SearchRequest.Builder builder = new SearchRequest.Builder().index(WONDERS_OF_WORLD);
        builder.query(query -> query
        .geoBoundingBox(geoBoundingBoxQuery ->
            geoBoundingBoxQuery.field("location")
            .boundingBox(geoBounds -> geoBounds.tlbr(bl4 -> bl4
                .topLeft(geoLocation -> geoLocation.coords(List.of(30.0, 31.0)))
                .bottomRight(geoLocation -> geoLocation.coords(List.of(32.0, 28.0))))
            )
        )
        );
        ```

        此外，地理边框查询支持与 geo_point 数据类型类似的格式。此外，我们还可以在[官方网站](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-bounding-box-query.html#_accepted_formats)上找到所支持格式的查询示例。

        最后，我们使用 SearchRequest 来查询 ElasticSearch：

        ```java
        SearchResponse<Location> searchResponse = client.search(build, Location.class);
        log.info("Search response: {}", searchResponse);
        ```

    2. 地理形状查询

        接下来，要查询地理形状文档，我们必须使用 GeoJSON。

        例如，我们可能想查找位于特定坐标范围内的所有文档：

        ```java
        {
        "query":{
            "bool":{
            "filter":[
                {
                "geo_shape":{
                    "region":{
                    "shape":{
                        "type":"envelope",
                        "coordinates":[[74.0,31.2],[81.1,24.0]]
                    },
                    "relation":"within"
                    }
                }
                }
            ]
            }
        }
        }
        ```

        具体来说，查询中的关系字段决定了搜索时使用的空间关系操作符。因此，我们可以从运算符列表中进行选择：

        - INTERSECTS -（默认）返回 geo_shape 字段与查询几何图形相交的所有文档
        - DISJOINT - 检索地理形状字段与查询几何图形没有共同点的所有文档
        - WITHIN - 获取地理形状字段位于查询几何图形内的所有文档
        - CONTAINS - 返回地理形状字段包含查询几何图形的所有文档

        同样，我们也可以使用不同的 GeoJSON 形状进行查询。

        例如，上述查询可以通过以下 SearchRequest 来实现：

        ```java
        StringReader jsonData = new StringReader("""
            {
                "type":"envelope",
                "coordinates": [[74.0, 31.2], [81.1, 24.0 ] ]
            }
            """);

        SearchRequest searchRequest = new SearchRequest.Builder()
        .query(query -> query.bool(boolQuery -> boolQuery
            .filter(query1 -> query1
            .geoShape(geoShapeQuery -> geoShapeQuery.field("region")
                .shape(
                geoShapeFieldQuery -> geoShapeFieldQuery.relation(GeoShapeRelation.Within)
                    .shape(JsonData.from(jsonData))
        ))))).build();
        ```

        同样，要查询数据，我们可以使用 SearchRequest 调用 search()：

        ```java
        SearchResponse<Object> search = client.search(searchRequest, Object.class);
        log.info("Search response: {}", search);
        ```

        要指出的是，SearchResponse 的源映射到一个通用对象类。鉴于此，ge_shapes 可以有多种形式，而我们事先并不知道查询可能会返回什么。

    3. 地理距离查询

        接下来，为了查找某个点指定范围内的所有文档，我们使用geo_distance（地理距离）查询：

        ```json
        {
            "query":{
                "geo_distance":{
                "location":{
                    "lat":29.976,
                    "lon":31.131
                },
                "distance":"10 miles"
                }
            }
        }
        ```

        同样，我们可以使用 SearchRequest 在 Java 中实现上述查询：

        ```java
        SearchRequest searchRequest = new SearchRequest.Builder().index(WONDERS_OF_WORLD)
        .query(query -> query
            .geoDistance(geoDistanceQuery -> geoDistanceQuery
            .field("location").distance("10 miles")
            .location(geoLocation -> geoLocation
                .latlon(latLonGeoLocation -> latLonGeoLocation
                .lon(29.88).lat(31.21)))
            )
        ).build();
        ```

        与 geo_point 一样，geo distance query 也支持[多种位置坐标格式](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-query.html#_accepted_formats_2)。

    4. 地理多边形查询

        接下来，我们将创建一个多边形，并在其中查找所有点。具体来说，我们将创建一个形状为 geo_polygon 的 geo_shape 查询：

        ```json
        {
            "query":{
                "bool":{
                "filter":[
                    {
                        "geo_shape":{
                            "location":{
                                "shape":{
                                    "type":"polygon",
                                    "coordinates":[[[68.859, 22.733],[68.859, 24.733],[70.859, 23]]]
                                },
                                "relation":"within"
                            }
                        }
                    }
                ]
                }
            }
        }
        ```

        同样，我们可以用 Java 重写查询：

        ```json
        JsonData jsonData = JsonData.fromJson("""
            {
                "type":"polygon",
                "coordinates":[[[68.859,22.733],[68.859,24.733],[70.859,23]]]
            }
            """);

        SearchRequest build = new SearchRequest.Builder()
        .query(query -> query.bool(
            boolQuery -> boolQuery.filter(
            query1 -> query1.geoShape(geoShapeQuery -> geoShapeQuery.field("location")
                .shape(
                geoShapeFieldQuery -> geoShapeFieldQuery.relation(GeoShapeRelation.Within)
                    .shape(jsonData)))))
        ).build();
        ```

        此查询仅支持 geo_point 数据类型。

7. 结论

    在本文中，我们讨论了索引地理数据的不同映射选项，即 geo_point 和 ge_shape。

    我们还讨论了存储地理数据的不同方法，最后，我们观察了地理查询和使用地理查询过滤结果的 Java API。
