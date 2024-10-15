# [Spring Data MongoDB中的GridFS](https://www.baeldung.com/spring-data-mongodb-gridfs)

1. 一览表

    本教程将探索Spring Data MongoDB的核心功能之一：与GridFS交互。

    GridFS存储规范主要用于处理超过BSON文档大小限制16MB的文件。Spring Data提供了一个GridFsOperations接口及其实现——GridFsTemplate——以轻松与此文件系统交互。

2. 配置

    1. XML配置

        让我们从GridFsTemplate的简单XML配置开始：

        ```xml
        <bean id="gridFsTemplate" class="org.springframework.data.mongodb.gridfs.GridFsTemplate">
            <constructor-arg ref="mongoDbFactory" />
            <constructor-arg ref="mongoConverter" />
        </bean>
        ```

        GridFsTemplate的构造函数参数包括对mongoDbFactory的bean引用，该bean创建Mongo数据库，以及在Java和MongoDB类型之间转换的mongoConverter。他们的豆子定义如下。

        ```xml
        <mongo:db-factory id="mongoDbFactory" dbname="test" mongo-client-ref="mongoClient" />
        <mongo:mapping-converter id="mongoConverter" base-package="com.baeldung.converter">
            <mongo:custom-converters base-package="com.baeldung.converter"/>
        </mongo:mapping-converter>
        ```

    2. Java配置

        让我们创建一个类似的配置，仅使用Java：

        ```java
        @Configuration
        @EnableMongoRepositories(basePackages = "com.baeldung.repository")
        public class MongoConfig extends AbstractMongoClientConfiguration {
            @Autowired
            private MappingMongoConverter mongoConverter;

            @Bean
            public GridFsTemplate gridFsTemplate() throws Exception {
                return new GridFsTemplate(mongoDbFactory(), mongoConverter);
            }
            
            // ...
        }
        ```

        对于此配置，我们使用了mongoDbFactory（）方法，并自动连接了父类AbstractMongoClientConfiguration中定义的MampingMongoConverter。

3. GridFs模板核心方法

    1. 商店

        存储方法将文件保存到MongoDB中。

        假设我们有一个空的数据库，并希望在其中存储一个文件：

        ```java
        InputStream inputStream = new FileInputStream("src/main/resources/test.png"); 
        gridFsTemplate.store(inputStream, "test.png", "image/png", metaData).toString();
        ```

        请注意，我们可以通过将DBObject传递给存储方法来保存其他元数据和文件。在我们的示例中，DBObject可能看起来像这样：

        ```java
        DBObject metaData = new BasicDBObject();
        metaData.put("user", "alex");
        ```

        GridFS使用两个集合来存储文件元数据及其内容。文件的元数据存储在文件集合中，文件的内容存储在块集合中。两个集合都以fs为前缀。

        如果我们执行MongoDB命令`db['fs.files'].find()`，我们将看到fs.files集合：

        ```json
        {
            "_id" : ObjectId("5602de6e5d8bba0d6f2e45e4"),
            "metadata" : {
                "user" : "alex"
            },
            "filename" : "test.png",
            "aliases" : null,
            "chunkSize" : NumberLong(261120),
            "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
            "length" : NumberLong(855),
            "contentType" : "image/png",
            "md5" : "27c915db9aa031f1b27bb05021b695c6"
        }
        ```

        命令`db['fs.chunks'].find()`检索文件的内容：

        ```java
        {
            "_id" : ObjectId("5602de6e5d8bba0d6f2e45e4"),
            "files_id" : ObjectId("5602de6e5d8bba0d6f2e45e4"),
            "n" : 0,
            "data" :
            {
                "$binary" : "/9j/4AAQSkZJRgABAQAAAQABAAD/4QAqRXhpZgAASUkqAAgAAAABADEBAgAHAAAAGgAAAAAAAABHb29nbGUAAP/bAIQAAwICAwICAwMDAwQDAwQFCAUFBAQFCgcHBggM
                CgwMCwoLCw0OEhANDhEOCwsQFhARExQVFRUMDxcYFhQYEhQVFAEDBAQGBQUJBgYKDw4MDhQUFA8RDQwMEA0QDA8VDA0NDw0MDw4MDA0ODxAMDQ0MDAwODA8MDQ4NDA0NDAwNDAwQ/8AA
                EQgAHAAcAwERAAIRAQMRAf/EABgAAQEBAQEAAAAAAAAAAAAAAAgGBwUE/8QALBAAAgEDAgQFAwUAAAAAAAAAAQIDBAURBiEABwgSIjFBYXEyUYETFEKhw
                f/EABoBAAIDAQEAAAAAAAAAAAAAAAMEAQIFBgD/xAAiEQACAgEDBAMAAAAAAAAAAAAAAQIRAyIx8BJRYYETIUH/2gAMAwEAAhEDEQA/AHDyq1Bb6GjFPMAszLkZHHCTi1I6O
                cXOFRZ1ZqoX6aqzRClkhb9MGVh2SsNyVI/hjG5389tuGcUaLK1GmFfpn5r3rnXpfV82pGtS3a0XmaGOO3zguKV1SWDwBQDH2uUWTOWMZzuM8bS0VQtJKRb2li9LL3l+4VNQPEfQTOB/WO
                G1K0JtUzwad1eZaYBiqzL4S2N8cZUsa7DqlRGdWvMq5aX6b9Tvb5pIZbggt7VcU3YacSkDbfuLNuu3lkk+98GNfIrLt2gK9K/NWl5Z87Ldebj3R0NTa2tVVKhOI0KoQ5AG4DRqSPk+gHGn
                khUPYNOx92vW9PcrdDW0FUJqOp7po5ETIYMxOdyOAK0qAvcgKPWa0oMTo7SEYDKPp98/5wPoJsx3rZ1wLhojS9iinLD9w9W47iSwVe0Z3wfrPoce2eC4I6rCX9MxrpUpWqudNunUosNLR1EkiyIGDqUKF
                fyZB+AeG80riueQdVfObC/tN1pLdaLfSxMiRQ08aIg2CjtGAB9uEyCSqSWujICUXwghT57A5+ePEoMvUdc5a3XlSsgUhZGjGM/TGAqjz+SfuT7DDmGC6WzzeyOv0+2amOrr3KylzTUwjjDeWGbJJ9/COI
                yvRFFv1iRsVGDaqYGWVsIoBZydsDhQGf/Z", 
                "$type" : "00" 
            }
        }
        ```

    2. 找到一个

        findOne正好返回一个满足指定查询条件的文档。

        ```java
        String id = "5602de6e5d8bba0d6f2e45e4";
        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        ```

        上述代码将返回上述示例中添加的结果记录。如果数据库包含多个与查询匹配的记录，它将只返回一个文档。返回的特定记录将根据自然顺序（文档存储在数据库中的顺序）进行选择。

    3. 发现

        查找从集合中选择文档，并将光标返回到选定的文档。

        假设我们有以下数据库，其中包含2条记录：

        ```json
        [
            {
                "_id" : ObjectId("5602de6e5d8bba0d6f2e45e4"),
                "metadata" : {
                    "user" : "alex"
                },
                "filename" : "test.png",
                "aliases" : null,
                "chunkSize" : NumberLong(261120),
                "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
                "length" : NumberLong(855),
                "contentType" : "image/png",
                "md5" : "27c915db9aa031f1b27bb05021b695c6"
            },
            {
                "_id" : ObjectId("5702deyu6d8bba0d6f2e45e4"),
                "metadata" : {
                    "user" : "david"
                },
                "filename" : "test.png",
                "aliases" : null,
                "chunkSize" : NumberLong(261120),
                "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
                "length" : NumberLong(855),
                "contentType" : "image/png",
                "md5" : "27c915db9aa031f1b27bb05021b695c6"
            }
        ]
        ```

        如果我们使用GridFsTemplate来执行以下查询：

        ```java
        List<GridFSFile> fileList = new ArrayList<GridFSFile>();
        gridFsTemplate.find(new Query()).into(fileList);
        ```

        结果列表应该包含两个记录，因为我们没有提供标准。

        当然，我们可以为发现方法提供一些标准。例如，如果我们想要获取元数据包含名为alex的用户的文件，代码将是：

        ```java
        List<GridFSFile> gridFSFiles = new ArrayList<GridFSFile>();
        gridFsTemplate.find(new Query(Criteria.where("metadata.user").is("alex"))).into(gridFSFiles);
        ```

        生成的列表将只包含一个记录。

    4. 删除

        删除从集合中删除文档。

        使用前面示例中的数据库，假设我们有代码：

        ```java
        String id = "5702deyu6d8bba0d6f2e45e4";
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
        ```

        执行删除后，数据库中只剩下一条记录：

        ```java
        {
            "_id" : ObjectId("5702deyu6d8bba0d6f2e45e4"),
            "metadata" : {
                "user" : "alex"
            },
            "filename" : "test.png",
            "aliases" : null,
            "chunkSize" : NumberLong(261120),
            "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
            "length" : NumberLong(855),
            "contentType" : "image/png",
            "md5" : "27c915db9aa031f1b27bb05021b695c6"
        }
        ```

        用大块：

        ```java
        {
            "_id" : ObjectId("5702deyu6d8bba0d6f2e45e4"),
            "files_id" : ObjectId("5702deyu6d8bba0d6f2e45e4"),
            "n" : 0,
            "data" :
            {
                "$binary" : "/9j/4AAQSkZJRgABAQAAAQABAAD/4QAqRXhpZgAASUkqAAgAAAABADEBAgAHAAAAGgAAAAAAAABHb29nbGUAAP/bAIQAAwICAwICAwMDAwQDAwQFCAUFBAQFCgcHBggM
                CgwMCwoLCw0OEhANDhEOCwsQFhARExQVFRUMDxcYFhQYEhQVFAEDBAQGBQUJBgYKDw4MDhQUFA8RDQwMEA0QDA8VDA0NDw0MDw4MDA0ODxAMDQ0MDAwODA8MDQ4NDA0NDAwNDAwQ/8AA
                EQgAHAAcAwERAAIRAQMRAf/EABgAAQEBAQEAAAAAAAAAAAAAAAgGBwUE/8QALBAAAgEDAgQFAwUAAAAAAAAAAQIDBAURBiEABwgSIjFBYXEyUYETFEKhw
                f/EABoBAAIDAQEAAAAAAAAAAAAAAAMEAQIFBgD/xAAiEQACAgEDBAMAAAAAAAAAAAAAAQIRAyIx8BJRYYETIUH/2gAMAwEAAhEDEQA/AHDyq1Bb6GjFPMAszLkZHHCTi1I6O
                cXOFRZ1ZqoX6aqzRClkhb9MGVh2SsNyVI/hjG5389tuGcUaLK1GmFfpn5r3rnXpfV82pGtS3a0XmaGOO3zguKV1SWDwBQDH2uUWTOWMZzuM8bS0VQtJKRb2li9LL3l+4VNQPEfQTOB/WO
                G1K0JtUzwad1eZaYBiqzL4S2N8cZUsa7DqlRGdWvMq5aX6b9Tvb5pIZbggt7VcU3YacSkDbfuLNuu3lkk+98GNfIrLt2gK9K/NWl5Z87Ldebj3R0NTa2tVVKhOI0KoQ5AG4DRqSPk+gHGn
                khUPYNOx92vW9PcrdDW0FUJqOp7po5ETIYMxOdyOAK0qAvcgKPWa0oMTo7SEYDKPp98/5wPoJsx3rZ1wLhojS9iinLD9w9W47iSwVe0Z3wfrPoce2eC4I6rCX9MxrpUpWqudNunUosNLR1EkiyIGDqUKF
                fyZB+AeG80riueQdVfObC/tN1pLdaLfSxMiRQ08aIg2CjtGAB9uEyCSqSWujICUXwghT57A5+ePEoMvUdc5a3XlSsgUhZGjGM/TGAqjz+SfuT7DDmGC6WzzeyOv0+2amOrr3KylzTUwjjDeWGbJJ9/COI
                yvRFFv1iRsVGDaqYGWVsIoBZydsDhQGf/Z",
                "$type" : "00"
            }
        }
        ```

    5. 获取资源

        getResources返回所有具有给定文件名模式的GridFsResource。

        假设我们在数据库中有以下记录：

        ```json
        [
        {
            "_id" : ObjectId("5602de6e5d8bba0d6f2e45e4"),
            "metadata" : {
                "user" : "alex"
            },
            "filename" : "test.png",
            "aliases" : null,
            "chunkSize" : NumberLong(261120),
            "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
            "length" : NumberLong(855),
            "contentType" : "image/png",
            "md5" : "27c915db9aa031f1b27bb05021b695c6"
        },
        {
            "_id" : ObjectId("5505de6e5d8bba0d6f8e4574"),
            "metadata" : {
                "user" : "david"
            },
            "filename" : "test.png",
            "aliases" : null,
            "chunkSize" : NumberLong(261120),
            "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
            "length" : NumberLong(855),
            "contentType" : "image/png",
            "md5" : "27c915db9aa031f1b27bb05021b695c6"
            },
            {
            "_id" : ObjectId("5777de6e5d8bba0d6f8e4574"),
            "metadata" : {
                "user" : "eugen"
            },
            "filename" : "baeldung.png",
            "aliases" : null,
            "chunkSize" : NumberLong(261120),
            "uploadDate" : ISODate("2015-09-23T17:16:30.781Z"),
            "length" : NumberLong(855),
            "contentType" : "image/png",
            "md5" : "27c915db9aa031f1b27bb05021b695c6"
            }
        ]
        ```

        现在让我们使用文件模式执行getResources：

        `GridFsResource[] gridFsResource = gridFsTemplate.getResources("test*");`

        这将返回两个文件名以“test”开头的记录（在这种情况下，它们都名为test.png）。

4. GridFSFile核心方法

    GridFSFile API也相当简单：

    - getFilename – 获取文件的文件名
    - getMetaData – 获取给定文件的元数据
    - containsField – 确定文档是否包含具有给定名称的字段
    - get – 按名称从对象中获取字段
    - getId – 获取文件的对象ID
    - keySet – 获取对象的字段名称

5. 结论

    在本文中，我们研究了MongoDB的GridFS功能，以及如何使用Spring Data MongoDB与它们进行交互。
