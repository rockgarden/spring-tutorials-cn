# [JPA标准查询](https://www.baeldung.com/hibernate-criteria-queries)

1. 一览表

    在本教程中，我们将讨论一个非常有用的JPA功能——标准查询。

    它使我们能够在不进行原始SQL的情况下编写查询，并让我们对查询进行一些面向对象的控制，这是Hibernate的主要功能之一。标准API允许我们以编程方式构建标准查询对象，在那里我们可以应用不同类型的过滤规则和逻辑条件。

    自Hibernate 5.2以来，Hibernate Criteria API被弃用，新开发侧重于JPA Criteria API。我们将探索如何使用Hibernate和JPA来构建标准查询。

2. Maven附属机构

    为了说明API，我们将使用参考JPA实现Hibernate。

    要使用Hibernate，我们将确保将其最新版本添加到我们的pom.xml文件中：

    ```xml
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.5.2.Final</version>
    </dependency>
    ```

    我们可以在这里找到最新版本的Hibernate。

3. 使用标准的简单示例

    让我们先看看如何使用标准查询来检索数据。我们将研究如何从数据库中获取特定类的所有实例。

    我们有一个项目类，代表数据库中的元组“ITEM”：

    ```java
    public class Item implements Serializable {

        private Integer itemId;
        private String itemName;
        private String itemDescription;
        private Integer itemPrice;

    // standard setters and getters
    }
    ```

    让我们来看看一个简单的标准查询，它将从数据库中检索“ITEM”的所有行：

    ```java
    Session session = HibernateUtil.getHibernateSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Item> cr = cb.createQuery(Item.class);
    Root<Item> root = cr.from(Item.class);
    cr.select(root);

    Query<Item> query = session.createQuery(cr);
    List<Item> results = query.getResultList();
    ```

    上述查询是如何获取所有项目的简单演示。让我们一步一步地看看：

    - 从SessionFactory对象创建一个会话实例
    - 通过调用getCriteriaBuilder（）方法创建一个CriteriaBuilder的实例
    - 通过调用CriteriaBuilder createQuery（）方法创建CriteriaQuery的实例
    - 通过调用Session createQuery（）方法创建查询实例
    - 调用查询对象的getResultList（）方法，它给我们结果

    既然我们已经涵盖了基础知识，让我们继续研究标准查询的一些功能。

    1. 使用表达式

        通过使用CritiareQuery where（）方法并提供CritriateBuilder创建的表达式，CriteriaBuilder可用于根据特定条件限制查询结果。

        让我们来看看一些常用表达式的例子。

        为了获得价格超过1000的商品：

        `cr.select(root).where(cb.gt(root.get("itemPrice"), 1000));`

        接下来，获取项目价格低于1000的项目：

        `cr.select(root).where(cb.lt(root.get("itemPrice"), 1000));`

        具有项目名称的项目包含椅子：

        `cr.select(root).where(cb.like(root.get("itemName"), "%chair%"));`

        项目价格在100到200之间的记录：

        `cr.select(root).where(cb.between(root.get("itemPrice"), 100, 200));`

        滑板、油漆和胶水中具有项目名称的项目：

        `cr.select(root).where(root.get("itemName").in("Skate Board", "Paint", "Glue"));`

        检查给定的属性是否为空：

        `cr.select(root).where(cb.isNull(root.get("itemDescription")));`

        检查给定的属性是否为空：

        `cr.select(root).where(cb.isNotNull(root.get("itemDescription")));`

        我们还可以使用isEmpty（）和isNotEmpty（）方法来测试类中的列表是否为空。

        此外，我们可以结合上述两个或更多比较。标准API允许我们轻松地链式：

        ```java
        Predicate[] predicates = new Predicate[2];
        predicates[0] = cb.isNull(root.get("itemDescription"));
        predicates[1] = cb.like(root.get("itemName"), "chair%");
        cr.select(root).where(predicates);
        ```

        添加两个带有逻辑操作的表达式：

        ```java
        Predicate greaterThanPrice = cb.gt(root.get("itemPrice"), 1000);
        Predicate chairItems = cb.like(root.get("itemName"), "Chair%");
        ```

        具有上述定义条件的项目与逻辑OR连接：

        `cr.select(root).where(cb.or(greaterThanPrice, chairItems));`

        要获得与上述定义条件匹配的项目，并加入逻辑AND：

        `cr.select(root).where(cb.and(greaterThanPrice, chairItems));`

    2. Sorting

        既然我们了解了标准的基本用法，让我们来看看标准的排序功能。

        在以下示例中，我们按名称升序排列列表，然后按价格降序排列：

        ```java
        cr.orderBy(
        cb.asc(root.get("itemName")),
        cb.desc(root.get("itemPrice")));
        ```

        在下一节中，我们将看看如何执行聚合函数。

    3. 投影、聚合和分组功能

        现在让我们看看不同的聚合函数。

        获取行数：

        ```java
        CriteriaQuery<Long> cr = cb.createQuery(Long.class);
        Root<Item> root = cr.from(Item.class);
        cr.select(cb.count(root));
        Query<Long> query = session.createQuery(cr);
        List<Long> itemProjected = query.getResultList();
        ```

        以下是聚合函数的示例——平均值的聚合函数：

        ```java
        CriteriaQuery<Double> cr = cb.createQuery(Double.class);
        Root<Item> root = cr.from(Item.class);
        cr.select(cb.avg(root.get("itemPrice")));
        Query<Double> query = session.createQuery(cr);
        List avgItemPriceList = query.getResultList();
        ```

        其他有用的聚合方法有sum（）、max（）、min（）、count（等。

    4. 标准更新

        从JPA 2.1开始，支持使用标准API执行数据库更新。

        CriteriaUpdate有一个set（）方法，可用于为数据库记录提供新值：

        ```java
        CriteriaUpdate<Item> criteriaUpdate = cb.createCriteriaUpdate(Item.class);
        Root<Item> root = criteriaUpdate.from(Item.class);
        criteriaUpdate.set("itemPrice", newPrice);
        criteriaUpdate.where(cb.equal(root.get("itemPrice"), oldPrice));

        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaUpdate).executeUpdate();
        transaction.commit();
        ```

        在上述片段中，我们从CriteriariBuilder创建了`CriteriaUpdate<Item>`的实例，并使用其set（）方法为ItemPrice提供新值。为了更新多个属性，我们只需要多次调用set（）方法。

    5. 标准删除

        CriteriaDelete使用Creriate API启用删除操作。

        我们只需要创建一个CriteriaDelete的实例，并使用where（）方法来应用限制：

        ```java
        CriteriaDelete<Item> criteriaDelete = cb.createCriteriaDelete(Item.class);
        Root<Item> root = criteriaDelete.from(Item.class);
        criteriaDelete.where(cb.greaterThan(root.get("itemPrice"), targetPrice));

        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        ```

    6. 使用标准定义实用程序

        Hibernate 6.3.0提供了一个CriteriarDefinition类，以简化构建标准查询。它允许定义匿名子类的初始化器块，其中CritriaterBuilder和CriteriaQuery的所有操作都可以调用，而无需指定目标对象。

        这是一个使用CritriaseDefinition检索“ITEM”所有行的标准查询：

        ```java
        final Session session = HibernateUtil.getHibernateSession();
        final SessionFactory sessionFactory = HibernateUtil.getHibernateSessionFactory();
        CriteriaDefinition<Item> query = new CriteriaDefinition<>(sessionFactory, Item.class) {
            {
                JpaRoot<Item> item = from(Item.class);
            }
        };
        List<Item> items = session.createSelectionQuery(query).list();
        ```

        在上述代码中，我们创建Criteriandefinition对象来构建查询，将SessionFactory和Item类作为参数传递。

        此外，我们定义了一个初始化器块，在那里我们构建查询。

        最后，我们通过在会话对象上调用createSelectionQuery（）方法，将查询的数据存储在列表中。

        接下来，让我们看看一些使用CriteriarDefinition的常见标准查询示例。首先，让我们购买价格大于1000的物品：

        ```java
        CriteriaDefinition<Item> query = new CriteriaDefinition<>(sessionFactory, Item.class) {
            {
                JpaRoot<Item> item = from(Item.class);
                where(gt(item.get("itemPrice"), 1000));
            }
        };
        List<Item> items = session.createSelectionQuery(query).list();
        ```

        首先，我们选择项目。接下来，我们调用名为where（）和gt（）的方法，以进一步指定项目价格必须大于1000。

        接下来，让我们购买价格在100到200之间的物品：

        `where(between(item.get("itemPrice"), 100, 200));`

        在上述代码中，我们使用between（）方法将检索到的数据放在一个范围内。

        我们可以通过在CriteriarDefinition初始化器块中构建标准查询来轻松构建它们。

4. 使用JPA标准API获取加入

    获取连接操作从相关实体中检索数据，并在单个数据库往返中初始化这些相关实体。与可能导致额外数据库查询的Join关键字不同，fetch join执行单个查询，其中包括主实体及其关联实体。这可以防止N+1问题并提高性能。

    1. 实体

        为了演示如何执行获取连接，让我们定义一个播放器实体：

        ```java
        @Entity
        class Player {
            @Id
            private int id;
            private String playerName;
            private String teamName;
            private int age;

            @ManyToOne
            @JoinColumn(name = "league_id")
            private League league;

            // constructor, getters, setters, toString  
        }
        ```

        玩家实体与联盟实体有@ManyToOne关系。

        这是联盟实体：

        ```java
        @Entity
        class League {
            @Id
            private int id;
            private String name;

            @OneToMany(mappedBy = "league")
            private List<Player> players = new ArrayList<>();
            // constructor, getters, setters, toString  
        }
        ```

        我们使用leage_id作为外键将联盟实体映射到玩家实体。

    2. 执行获取加入操作

        让我们根据他们所属的联赛来获取球员：

        ```java
        CriteriaQuery<Player> query = cb.createQuery(Player.class);
        Root<Player> root = query.from(Player.class);
        root.fetch("league", JoinType.LEFT);
        query.where(cb.equal(root.get("league").get("name"), "Premier League"));
        List<Player> playerList = session.createQuery(query).getResultList();
        ```

        在上述代码中，我们创建了一个根对象，该对象选择播放器实体。接下来，我们调用fetch（）方法onroot，并指定定义玩家和联盟之间关系的联盟字段。我们还指定JoinType.LEFT来执行左连接。

        最后，我们执行查询来选择英超联赛的球员。此单个查询获取玩家实体及其关联的联盟实体。

5. 比HQL的优势

    在前几节中，我们涵盖了如何使用标准查询。

    显然，与HQL相比，标准查询的主要和最强硬的优势是漂亮、干净、面向对象的API。

    与普通的HQL相比，我们可以简单地编写更灵活、更动态的查询。逻辑可以使用IDE进行重构，并具有Java语言本身的所有类型安全优势。

    当然，也有一些缺点，特别是在更复杂的连接方面。

    因此，我们通常必须使用最好的工具来完成这项工作——在大多数情况下，它可以是标准API，但肯定有的情况我们必须走低级别。

6. 调用用户定义的函数

    在JPA的Criteria API中，我们可以使用CriteriaBuilder的函数方法在查询中直接调用用户定义的函数。

    假设我们的数据库中有一个名为comate_discount的用户定义函数，该函数将itemPrice作为输入，并返回折扣价格：

    ```java
    CREATE ALIAS calculate_discount AS $$
        double calculateDiscount(double itemPrice) {
            double discountRate = 0.1;
            return itemPrice - (itemPrice * discountRate);
        }
    $$;
    ```

    以下是我们如何在CritriareQuery中调用此函数来获取折扣价格低于特定阈值的项目：

    ```java
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Item> query = cb.createQuery(Item.class);
    Root<Item> root = query.from(Item.class);
    Expression<Double> discountPrice = cb.function("calculate_discount", Double.class, root.get("itemPrice"));
    query.select(root).where(cb.lt(discountPrice, 500.0));

    List<Item> items = session.createQuery(query).getResultList();

    assertNotNull(items, "Discounted items should not be null");
    assertFalse(items.isEmpty(), "There should be discounted items returned");
    assertEquals(3, items.size());
    ```

    首先，我们为项目实体创建一个CreriteriaQuery。然后，我们使用CriteriaBuilder的function（）方法来调用我们的自定义函数ccate_discount。此方法允许我们指定函数的名称、预期的返回类型和要传递给函数的参数。

    在我们的示例中，我们将项目价格字段作为参数传递给用户定义的计算_折扣函数。最后，我们使用select（）方法来指定我们想要检索此函数调用的结果，作为查询的一部分。构建查询后，我们使用getResultList（）方法执行它，该方法返回折扣价格列表。

    在实践中，在CriteriaQuery中调用用户定义的函数对于应用复杂的业务逻辑或计算特别有用，这些逻辑或计算由数据库本身更有效地处理。

7. 结论

    在本文中，我们专注于Hibernate和JPA中标准查询的基础知识，以及API的一些高级功能。
