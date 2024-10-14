# [Spring Data Couchbase中的多个存储桶和空间视图查询](https://www.baeldung.com/spring-data-couchbase-buckets-and-spatial-view-queries)

1. 介绍

    在关于Spring Data Couchbase的第三个教程中，我们演示了支持跨越多个存储桶的Couchbase数据模型所需的配置，并介绍了使用空间视图来查询多维数据。

2. 数据模型

    除了第一个教程中的Person实体和第二个教程中的Student实体外，我们还为本教程定义了Campus实体：

    ```java
    @Document
    public class Campus {
        @Id
        private String id;

        @Field
        @NotNull
        private String name;

        @Field
        @NotNull
        private Point location;

        // standard getters and setters
    }
    ```

3. 多个Couchbase桶的Java配置

    要在项目中使用多个桶，您需要使用Spring Data Couchbase模块的2.0.0或更高版本，并且需要使用基于Java的配置，因为基于XML的配置仅支持单桶场景。

    以下是我们在Maven pom.xml文件中包含的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-couchbase</artifactId>
        <version>2.1.1.RELEASE</version>
    </dependency>
    ```

    1. 定义Bucket Bean

        我们指定“baeldung”作为与Spring Data一起使用的默认Couchbase桶的名称。

        我们将校园实体存储在“baeldung2”桶中。

        要使用第二个存储桶，我们首先必须在Couchbase配置类中为存储桶本身定义@Bean：

        ```java
        @Bean
        public Bucket campusBucket() throws Exception {
            return couchbaseCluster().openBucket("baeldung2", "");
        }
        ```

    2. 配置模板Bean

        接下来，我们为CouchbaseTemplate定义了一个@Bean，以便与此存储桶一起使用：

        ```java
        @Bean
        public CouchbaseTemplate campusTemplate() throws Exception {
            CouchbaseTemplate template = new CouchbaseTemplate(
            couchbaseClusterInfo(), campusBucket(),
            mappingCouchbaseConverter(), translationService());
            template.setDefaultConsistency(getDefaultConsistency());
            return template;
        }
        ```

    3. 映射存储库

        最后，我们定义了Couchbase存储库操作的自定义映射，以便Campus实体类将使用新的模板和存储桶，而其他实体类将继续使用默认模板和存储桶：

        ```java
        @Override
        public void configureRepositoryOperationsMapping(
        RepositoryOperationsMapping baseMapping) {
            try {
                baseMapping.mapEntity(Campus.class, campusTemplate());
            } catch (Exception e) {
                //custom Exception handling
            }
        }
        ```

4. 查询空间或多维数据

    Couchbase使用一种称为空间视图的特殊视图，对二维数据（如地理数据）运行边界框查询提供了原生支持。

    边界框查询是一种范围查询，使用框的最西南`[x,y]`点作为其startRange参数，使用最西北的`[x,y]`点作为其endRange参数。

    Spring Data使用一种试图消除假阳性匹配的算法将Couchbase的原生边界框查询功能扩展到涉及圆和多边形的查询，它还支持涉及两个以上维度的查询。

    Spring Data通过一组关键字简化了多维查询的创建，这些关键字可用于在Couchbase存储库中定义派生查询。

    1. 支持的数据类型

        Spring Data Couchbase存储库查询支持org.springframework.data.geo软件包中的数据类型，包括点、框、圆、多边形和距离。

    2. 派生查询关键词

        除了标准的Spring Data存储库关键字外，Couchbase存储库在涉及两个维度的派生查询中支持以下关键字：

        - Within，InWithin（需要两个定义边界框的点参数）
        - Near，IsNear（以点和距离作为参数）

        以下关键字可用于涉及两个以上维度的查询：

        - Between（用于在startRange和endRange上添加单个数值）
        - GreaterThan，GreaterThanEqual，After（用于向startRange添加单个数值）
        - LessThan，LessThanEqual，Before（用于向endRange添加单个数值）

        以下是一些使用这些关键字的派生查询方法的示例：

        - findByLocationNear
        - findByLocationWithin
        - findByLocationNearAndPopulationGreaterThan
        - findByLocationWithinAndAreaLessThan
        - findByLocationNearAndTuitionBetween

5. 定义存储库

    由空间视图支持的存储库方法必须使用@Dimensional注释进行装饰，该注释指定用于定义视图键的设计文档名称、视图名称和维度数量（除非另行指定，否则默认为2）。

    1. CampusRespository接口

        在我们的CampusRepository界面中，我们声明了两种方法——一种使用传统的Spring Data关键字，由MapReduce视图支持，一种使用维度Spring Data关键字，由空间视图支持：

        ```java
        public interface CampusRepository extends CrudRepository<Campus, String> {

            @View(designDocument="campus", viewName="byName")
            Set<Campus> findByName(String name);

            @Dimensional(dimensions=2, designDocument="campus_spatial",
            spatialViewName="byLocation")
            Set<Campus> findByLocationNear(Point point, Distance distance);
        }
        ```

    2. 空间视图

        空间视图被写成JavaScript函数，很像MapReduce视图。与由地图函数和还原函数组成的MapReduce视图不同，空间视图仅由空间函数组成，可能与MapReduce视图在同一Couchbase设计文档中共存。

        对于我们的校园实体，我们将创建一个名为“campus_spatial”的设计文档，其中包含一个名为“byLocation”的空间视图，具有以下功能：

        ```java
        function (doc) {
        if (doc.location &&
            doc._class == "com.baeldung.spring.data.couchbase.model.Campus") {
            emit([doc.location.x, doc.location.y], null);
        }
        }
        ```

        正如本例所示，当您编写空间视图函数时，发射函数调用中使用的键必须是两个或更多值的数组。

    3. MapReduce Views

        为了为我们的存储库提供全面支持，我们必须创建一个名为“校园”的设计文档，其中包含两个MapReduce视图：“all”和“byName”。

        这是“all”视图的地图功能：

        ```java
        function (doc, meta) {
            if(doc._class == "com.baeldung.spring.data.couchbase.model.Campus") {    
                emit(meta.id, null);
            }
        }
        ```

        这是“byName”视图的地图功能：

        ```java
        function (doc, meta) {
            if(doc._class == "com.baeldung.spring.data.couchbase.model.Campus" &&
                doc.name) {    
                emit(doc.name, null);
            }
        }
        ```

6. 结论

    我们展示了如何配置Spring Data Couchbase项目以支持使用多个存储桶，并演示了如何使用存储库抽象针对多维数据编写空间视图查询。
