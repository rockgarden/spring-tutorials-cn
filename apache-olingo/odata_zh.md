# [OData 协议指南](https://www.baeldung.com/odata)

1. 简介

    在本教程中，我们将探讨 [OData](https://www.odata.org/)，这是一种标准协议，允许使用 RESTFul API 轻松访问数据集。

2. 什么是 OData？

    OData 是使用 RESTful API 访问数据的 OASIS 和 ISO/IEC 标准。因此，它允许用户使用标准 HTTP 调用发现和浏览数据集。

    例如，我们可以用一个简单的curl单行程序访问一个[公开可用的OData服务](https://www.odata.org/odata-services/)：

    ```bash
    curl -s https://services.odata.org/V2/Northwind/Northwind.svc/Regions
    <?xml version="1.0" encoding="utf-8" standalone="yes"?>
    <feed xml:base="https://services.odata.org/V2/Northwind/Northwind.svc/" 
    xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" 
    xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" 
    xmlns="http://www.w3.org/2005/Atom">
        <title type="text">Regions</title>
        <id>https://services.odata.org/V2/Northwind/Northwind.svc/Regions</id>
    ... rest of xml response omitted
    ```

    截至本文撰写之时，OData 协议已进入第四个版本--更准确地说，是 4.01。OData V4 于 2014 年达到 OASIS 标准水平，但它的历史却更为悠久。我们可以追溯到微软一个名为 [Astoria](https://devblogs.microsoft.com/odata/ado-net-data-services-project-astoria/) 的项目，该项目于 2007 年更名为 ADO.Net Data Services。在微软的 OData [博客](https://devblogs.microsoft.com/odata/welcome/)中，仍然可以找到宣布该项目的原始博客条目。

    与 JDBC 或 ODBC 等标准 API 相比，使用基于标准的协议访问数据集会带来一些好处。作为终端用户级别的消费者，我们可以使用 Excel 等常用工具从任何兼容的提供商处检索数据。大量可用的 REST 客户端库也为编程提供了便利。

    作为提供商，采用 OData 也有好处：一旦我们创建了兼容的服务，我们就可以专注于提供有价值的数据集，最终用户可以使用他们选择的工具来使用这些数据集。由于它是基于 HTTP 的协议，我们还可以利用安全机制、监控和日志记录等方面的优势。

    这些特点使得 OData 成为政府机构实施公共数据服务时的热门选择，我们可以通过[查看该目录](https://pragmatiqa.com/xodata/odatadir.html)来了解这一点。

3. OData 概念

    OData 协议的核心是实体数据模型（简称 EDM）的概念。EDM 通过包含大量元实体的元数据文档来描述 OData 提供者公开的数据：

    - 实体类型及其属性（如个人、客户、订单等）和键
    - 实体之间的关系
    - 复杂类型，用于描述嵌入到实体中的结构化类型（例如，地址类型是客户类型的一部分）
    - 实体集，用于聚合给定类型的实体

    规范规定，该元数据文档必须在用于访问服务的根 URL 的标准位置 `$metadata` 上提供。例如，如果我们在 `http://example.org/odata.svc/` 上提供了一个 OData 服务，那么其元数据文档就可以在 `http://example.org/odata.svc/$metadata` 上找到。

    返回的文档包含大量描述该服务器支持的模式的 XML：

    ```xml
    <?xml version="1.0"?>
    <edmx:Edmx 
    xmlns:edmx="http://schemas.microsoft.com/ado/2007/06/edmx" 
    Version="1.0">
        <edmx:DataServices 
        xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" 
        m:DataServiceVersion="1.0">
        ... schema elements omitted
        </edmx:DataServices>
    </edmx:Edmx>
    ```

    让我们将此文档拆分成几个主要部分。

    顶层元素 `<edmx:Edmx>` 只能有一个子元素，即 `<edmx:DataServices>` 元素。这里需要注意的是命名空间 URI，因为它允许我们识别服务器使用的 OData 版本。在本例中，命名空间表明我们有一个 OData V2 服务器，它使用微软的标识符。

    一个 DataServices 元素可以有一个或多个 Schema 元素，每个元素描述一个可用的数据集。由于对 Schema 中可用元素的全面描述超出了本文的范围，我们将重点介绍最重要的元素： 实体类型（EntityType）、关联（Associations）和实体集（EntitySets）。

    1. 实体类型元素

        该元素定义了给定实体的可用属性，包括其主键。它还可能包含有关与其他模式类型之间关系的信息，通过观察一个示例（CarMaker），我们可以发现它与其他 ORM 技术（如 JPA）中的描述并无太大区别：

        ````xml
        <EntityType Name="CarMaker">
            <Key>
                <PropertyRef Name="Id"/>
            </Key>
            <Property Name="Id" Type="Edm.Int64" 
            Nullable="false"/>
            <Property Name="Name" Type="Edm.String" 
            Nullable="true" 
            MaxLength="255"/>
            <NavigationProperty Name="CarModelDetails" 
            Relationship="default.CarModel_CarMaker_Many_One0" 
            FromRole="CarMaker" 
            ToRole="CarModel"/>
        </EntityType>
        ```

        在这里，我们的 CarMaker 只有两个属性--Id 和 Name--以及与另一个实体类型的关联。Key 子元素将实体的主键定义为其 Id 属性，而每个 Property 元素都包含有关实体属性的数据，如名称、类型或无效性。

        导航属性（NavigationProperty）是一种特殊的属性，它描述了通向相关实体的 "接入点"。

    2. 关联元素

        关联元素描述两个实体之间的关联，包括两端的多重性和可选的参照完整性约束：

        ```xml
        <Association Name="CarModel_CarMaker_Many_One0">
            <End Type="default.CarModel" Multiplicity="*" Role="CarModel"/>
            <End Type="default.CarMaker" Multiplicity="1" Role="CarMaker"/>
            <ReferentialConstraint>
                <Principal Role="CarMaker">
                    <PropertyRef Name="Id"/>
                </Principal>
                <Dependent Role="CarModel">
                    <PropertyRef Name="Maker"/>
                </Dependent>
            </ReferentialConstraint>
        </Association>
        ```

        在这里，关联元素定义了 CarModel 和 CarMaker 实体之间的一对多关系，其中前者作为从属方。

    3. 实体集元素

        我们要探讨的最后一个模式概念是 EntitySet 元素，它表示给定类型的实体集合。虽然我们很容易将它们与表格类比--在很多情况下，它们就是表格(table)--但更好的类比是视图。原因是我们可以为同一 EntityType 设置多个 EntitySet 元素，每个元素代表可用数据的不同子集。

        EntityContainer 元素是一个顶层模式元素，它将所有可用的 EntitySets 组合在一起：

        ```xml
        <EntityContainer Name="defaultContainer" 
        m:IsDefaultEntityContainer="true">
            <EntitySet Name="CarModels" 
            EntityType="default.CarModel"/>
            <EntitySet Name="CarMakers" 
            EntityType="default.CarMaker"/>
        </EntityContainer>
        ```

        在我们的简单示例中，我们只有两个实体集，但我们也可以添加其他视图，如 ForeignCarMakers 或 HistoricCarMakers。

4. OData URL 和方法

    为了访问 OData 服务公开的数据，我们使用常规的 HTTP 动词：

    - GET 返回一个或多个实体
    - POST 向现有实体集添加一个新实体
    - PUT 替换给定实体
    - PATCH 替换给定实体的特定属性
    - DELETE 删除给定实体

    所有这些操作都需要一个资源路径来执行。资源路径可以定义实体集、实体，甚至实体中的属性。

    让我们来看一个用于访问我们以前的 OData 服务的 URL 实例：

    `http://example.org/odata/CarMakers`

    该 URL 的第一部分（从协议开始到 odata/ 路径段）称为服务根 URL，该服务的所有资源路径都是相同的。 由于服务根 URL 始终是相同的，我们将在下面的 URL 示例中用省略号（"..."）代替它。

    在这种情况下，CarMakers 指的是服务元数据中声明的实体集之一。我们可以使用普通浏览器访问该 URL，它将返回一个包含该类型所有现有实体的文档：

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <feed xmlns="http://www.w3.org/2005/Atom" 
    xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" 
    xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" 
    xml:base="http://localhost:8080/odata/">
        <id>http://localhost:8080/odata/CarMakers</id>
        <title type="text">CarMakers</title>
        <updated>2019-04-06T17:51:33.588-03:00</updated>
        <author>
            <name/>
        </author>
        <link href="CarMakers" rel="self" title="CarMakers"/>
        <entry>
        <id>http://localhost:8080/odata/CarMakers(1L)</id>
        <title type="text">CarMakers</title>
        <updated>2019-04-06T17:51:33.589-03:00</updated>
        <category term="default.CarMaker" 
            scheme="http://schemas.microsoft.com/ado/2007/08/dataservices/scheme"/>
        <link href="CarMakers(1L)" rel="edit" title="CarMaker"/>
        <link href="CarMakers(1L)/CarModelDetails" 
            rel="http://schemas.microsoft.com/ado/2007/08/dataservices/related/CarModelDetails" 
            title="CarModelDetails" 
            type="application/atom+xml;type=feed"/>
            <content type="application/xml">
                <m:properties>
                    <d:Id>1</d:Id>
                    <d:Name>Special Motors</d:Name>
                </m:properties>
            </content>
        </entry>  
    ... other entries omitted
    </feed>
    ```

    返回的文档包含每个 CarMaker 实例的条目元素。

    让我们仔细看看有哪些信息可供使用：

    - id：指向此特定实体的链接
    - title/author/updated：有关此条目元数据
    - link elements： 用于指向用于编辑实体的资源（rel="edit"）或相关实体的链接。在本例中，我们有一个链接，指向与该特定汽车制造商相关联的 CarModel 实体集。
    - content：CarModel 实体的属性值

    这里需要注意的一个要点是，使用键值对来标识实体集中的特定实体。在我们的示例中，键值是数字，因此 CarMaker(1L) 这样的资源路径指的是主键值等于 1 的实体，这里的 "L" 只是表示长值，可以省略。

5. 查询选项

    我们可以向资源 URL 传递查询选项，以修改返回数据的多个方面，例如限制返回集合的大小或排序。OData 规范定义了丰富的选项集，但在此我们将重点介绍最常见的选项。

    一般来说，查询选项可以相互组合，因此客户端可以轻松实现分页、过滤和对结果列表排序等常用功能。

    1. $top 和 $skip

        我们可以使用 $top 和 $skip 查询选项浏览大型数据集：

        `.../CarMakers?$top=10&$skip=10`

        $top 告知服务，我们只需要 CarMakers 实体集的前 10 条记录。在 $top 之前应用的 $skip 则告诉服务器跳过前 10 条记录。

        了解给定实体集的大小通常很有用，为此，我们可以使用 $count 子资源：

        `.../CarMakers/$count`

        该资源会生成一个文本/纯文本文件，其中包含相应实体集的大小。在这里，我们必须注意提供商支持的特定 OData 版本。OData V2 支持将 $count 作为集合的子资源，而 V4 则允许将其用作查询参数。在本例中，$count 是布尔值，因此我们需要相应更改 URL：

        `.../CarMakers?$count=true`

    2. $filter

        我们使用 $filter 查询选项，将给定实体集中返回的实体限制为符合给定条件的实体。$filter 的值是一个逻辑表达式，支持基本运算符、分组和许多有用的功能。例如，让我们建立一个查询，返回名称属性以字母 "B" 开头的所有 CarMaker 实例：

        `.../CarMakers?$filter=startswith(Name,'B')`

        现在，让我们结合几个逻辑运算符来搜索特定年份和制造商的 CarModels：

        `.../CarModels?$filter=Year eq 2008 and CarMakerDetails/Name eq 'BWM'`

        在这里，我们使用相等运算符 eq 为属性指定值。我们还可以看到如何在表达式中使用相关实体的属性。

    3. $expand

        默认情况下，OData 查询不会返回相关实体的数据，这通常是正常的。我们可以使用 $expand 查询选项来请求将给定相关实体的数据与主内容一起包含在内。

        使用我们的示例域，让我们创建一个 URL，返回给定模型及其制造商的数据，从而避免额外的服务器往返：

        `.../CarModels(1L)?$expand=CarMakerDetails`

        现在，返回的文档将CarMaker数据作为相关实体的一部分：

        ```xml
        <?xml version="1.0" encoding="utf-8"?>
        <entry xmlns="http://www.w3.org/2005/Atom" 
        xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" 
        xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" 
        xml:base="http://localhost:8080/odata/">
            <id>http://example.org/odata/CarModels(1L)</id>
            <title type="text">CarModels</title>
            <updated>2019-04-07T11:33:38.467-03:00</updated>
            <category term="default.CarModel" 
            scheme="http://schemas.microsoft.com/ado/2007/08/dataservices/scheme"/>
            <link href="CarModels(1L)" rel="edit" title="CarModel"/>
            <link href="CarModels(1L)/CarMakerDetails" 
            rel="http://schemas.microsoft.com/ado/2007/08/dataservices/related/CarMakerDetails" 
            title="CarMakerDetails" 
            type="application/atom+xml;type=entry">
                <m:inline>
                    <entry xml:base="http://localhost:8080/odata/">
                        <id>http://example.org/odata/CarMakers(1L)</id>
                        <title type="text">CarMakers</title>
                        <updated>2019-04-07T11:33:38.492-03:00</updated>
                        <category term="default.CarMaker" 
                        scheme="http://schemas.microsoft.com/ado/2007/08/dataservices/scheme"/>
                        <link href="CarMakers(1L)" rel="edit" title="CarMaker"/>
                        <link href="CarMakers(1L)/CarModelDetails" 
                        rel="http://schemas.microsoft.com/ado/2007/08/dataservices/related/CarModelDetails" 
                        title="CarModelDetails" 
                        type="application/atom+xml;type=feed"/>
                        <content type="application/xml">
                            <m:properties>
                                <d:Id>1</d:Id>
                                <d:Name>Special Motors</d:Name>
                            </m:properties>
                        </content>
                    </entry>
                </m:inline>
            </link>
            <content type="application/xml">
                <m:properties>
                    <d:Id>1</d:Id>
                    <d:Maker>1</d:Maker>
                    <d:Name>Muze</d:Name>
                    <d:Sku>SM001</d:Sku>
                    <d:Year>2018</d:Year>
                </m:properties>
            </content>
        </entry>
        ```

    4. $select

        我们使用 $select 查询选项通知 OData 服务只返回给定属性的值。这在我们的实体有大量属性，但我们只对其中部分感兴趣的情况下非常有用。

        让我们在仅返回 Name 和 Sku 属性的查询中使用该选项：

        `.../CarModels(1L)?$select=Name,Sku`

        现在生成的文档只有所要求的属性：

        ```xml
        ... xml omitted
            <content type="application/xml">
                <m:properties>
                    <d:Name>Muze</d:Name>
                    <d:Sku>SM001</d:Sku>
                </m:properties>
            </content>
        ... xml omitted
        ```

        我们还可以看到，甚至相关的实体也被省略了。为了包含它们，我们需要在 $select 选项中包含关系的名称。

    5. $orderBy

        `$orderBy` 选项的工作原理与 SQL 对应选项类似。我们用它来指定服务器返回给定实体集的顺序。简单地说，它的值只是所选实体的属性名称列表，并可选择告知顺序方向：

        `.../CarModels?$orderBy=Name asc,Sku desc`

        该查询将产生一个按名称和 SKU 排序的 CarModels 列表，分别按升序和降序排列。

        这里的一个重要细节是给定属性的方向部分所使用的大小写：虽然规范规定服务器必须支持关键字 asc 和 desc 的任何大小写字母组合，但也规定客户端只能使用小写字母。

    6. $format

        该选项定义了服务器应使用的数据表示格式，它优先于任何 HTTP 内容协商头（如 Accept）。其值必须是完整的 MIME-Type 或特定格式的简写。

        例如，我们可以使用 json 作为 application/json 的缩写：

        `.../CarModels?$format=json`

        该 URL 将指示我们的服务使用 JSON 格式返回数据，而不是我们之前看到的 XML 格式。如果不存在该选项，服务器将使用接受标头（如果存在）的值。当两者都不存在时，服务器可自由选择任何表示方式--通常是 XML 或 JSON。

        具体到 JSON，它从根本上是无模式的。不过，OData 4.01 也为[元数据端点定义了 JSON 模式](http://docs.oasis-open.org/odata/odata-csdl-json/v4.01/odata-csdl-json-v4.01.html)。这意味着我们现在可以编写完全摆脱 XML 处理的客户端。

6. 结论

    在这篇 OData 简介中，我们介绍了 OData 的基本语义以及如何执行简单的数据集导航。我们的后续文章将继续上次的内容，直接介绍 Olingo 库。然后，我们将了解如何使用该库实现示例服务。
