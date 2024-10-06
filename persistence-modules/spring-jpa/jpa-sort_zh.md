# [用JPA分类](https://www.baeldung.com/jpa-sort)

1. 一览表

    本文说明了JPA可用于分类的各种方式。

2. 使用JPA/JQL API进行排序

    在Order By子句的帮助下，使用JQL进行排序：

    ```java
    String jql ="Select f from Foo as f order by f.id";
    Query query = entityManager.createQuery (jql);
    ```

    基于此查询，JPA生成以下直截了当的SQL语句：

    ```java
    Hibernate: select foo0_.id as id1_4_, foo0_.name as name2_4_ 
        from Foo foo0_ order by foo0_.id
    ```

    请注意，JQL字符串中的SQL关键字不区分大小写，但实体及其属性的名称是区分大小写的。

    1. 设置排序顺序

        默认情况下，排序顺序是升序，但可以在JQL字符串中显式设置。就像在纯SQL中一样，排序选项是asc和desc：

        ```java
        String jql = "Select f from Foo as f order by f.id desc";
        Query sortQuery = entityManager.createQuery(jql);
        ```

        然后生成的SQL查询将包括顺序方向：

        ```log
        Hibernate: select foo0_.id as id1_4_, foo0_.name as name2_4_ 
            from Foo foo0_ order by foo0_.id desc
        ```

    2. 按多个属性排序

        要按多个属性排序，这些属性被添加到JQL字符串的按子句排序中：

        ```java
        String jql ="Select f from Foo as f order by f.name asc, f.id desc";
        Query sortQuery = entityManager.createQuery(jql);
        ```

        两个排序条件都将出现在生成的SQL查询语句中：

        ```log
        Hibernate: select foo0_.id as id1_4_, foo0_.name as name2_4_ 
            from Foo foo0_ order by foo0_.name asc, foo0_.id desc
        ```

    3. 设置空值的排序优先级

        空值的默认优先级是特定于数据库的，但可以通过HQL查询字符串中的NULLS FIRST或NULLS LAST子句进行自定义。

        这里有一个简单的例子——按Foo的名字降序排序，并在末尾放置Nulls：

        ```java
        Query sortQuery = entityManager.createQuery
            ("Select f from Foo as f order by f.name desc NULLS LAST");
        ```

        生成的SQL查询包括为空的1 else 0结束子句（第3行）：

        ```log
        Hibernate: select foo0_.id as id1_4_, foo0_.BAR_ID as BAR_ID2_4_, 
            foo0_.bar_Id as bar_Id2_4_, foo0_.name as name3_4_,from Foo foo0_ order 
            by case when foo0_.name is null then 1 else 0 end, foo0_.name desc
        ```

    4. 对一到多关系进行分类

        超越基本示例，现在让我们看看一个用例，该用例涉及在包含Foo实体集合的一对多关系-Bar中对实体进行排序。

        我们想对Bar实体及其Foo实体的集合进行排序——JPA对于这项任务来说特别简单：

        1. 对集合进行排序：在Bar实体的Foo集合之前添加OrderBy注释：

            ```java
            @OrderBy("name ASC")
            List <Foo> fooList;
            ```

        2. 对包含集合的实体进行排序：

            ```java
            String jql = "Select b from Bar as b order by b.id";
            Query barQuery = entityManager.createQuery(jql);
            List<Bar> barList = barQuery.getResultList();
            ```

        请注意，@OrderBy注释是可选的，但在这种情况下，我们使用它是因为我们想对每个Bar的Foo集合进行排序。

        让我们来看看发送到RDMS的SQL查询：

        ```log
        Hibernate: select bar0_.id as id1_0_, bar0_.name as name2_0_ from Bar bar0_ order by bar0_.id

        Hibernate:
        select foolist0_.BAR_ID as BAR_ID2_0_0_, foolist0_.id as id1_4_0_, 
        foolist0_.id as id1_4_1_, foolist0_.BAR_ID as BAR_ID2_4_1_, 
        foolist0_.bar_Id as bar_Id2_4_1_, foolist0_.name as name3_4_1_ 
        from Foo foolist0_
        where foolist0_.BAR_ID=? order by foolist0_.name asc
        ```

        第一个查询对父条形实体进行排序。生成第二个查询是为了对属于Bar的子Foo实体的集合进行排序。

3. 使用JPA标准查询对象API进行排序

    使用JPA标准-orderBy方法是设置所有排序参数的“一站式”替代方案：可以设置顺序方向和排序属性。以下是该方法的API：

    - orderBy（CriteriaBuilder.asc）：按升序排序。
    - orderBy（CriteriaBuilder.desc）：按降序排序。

    每个Order实例都是通过其asc或desc方法使用CriteriaBuilder对象创建的。

    这里有一个简短的例子——按名称对Foos进行排序：

    ```java
    CriteriaQuery<Foo> criteriaQuery = criteriaBuilder.createQuery(Foo.class);
    Root<Foo> from = criteriaQuery.from(Foo.class);
    CriteriaQuery<Foo> select = criteriaQuery.select(from);
    criteriaQuery.orderBy(criteriaBuilder.asc(from.get("name")));
    ```

    get方法的参数区分大小写，因为它需要与属性名称匹配。

    与简单的JQL不同，JPA标准查询对象API在查询中强制使用显式顺序方向。请注意，在此代码片段的最后一行中，criteriasBuilder对象通过调用其asc方法指定要升序的排序顺序。

    当上述代码被执行时，JPA会生成如下所示的SQL查询。JPA标准对象生成一个带有显式asc子句的SQL语句：

    ```log
    Hibernate: select foo0_.id as id1_4_, foo0_.name as name2_4_
        from Foo foo0_ order by foo0_.name asc
    ```

    1. 按多个属性排序

        要按多个属性排序，只需将Order实例传递给orderBy方法，即可对每个属性进行排序。

        这里有一个快速的例子——按名称和id排序，分别按asc和desc顺序排列：

        ```java
        CriteriaQuery<Foo> criteriaQuery = criteriaBuilder.createQuery(Foo.class);
        Root<Foo> from = criteriaQuery.from(Foo.class); 
        CriteriaQuery<Foo> select = criteriaQuery.select(from); 
        criteriaQuery.orderBy(criteriaBuilder.asc(from.get("name")),
            criteriaBuilder.desc(from.get("id")));
        ```

        相应的SQL查询如下所示：

        ```log
        Hibernate: select foo0_.id as id1_4_, foo0_.name as name2_4_ 
            from Foo foo0_ order by foo0_.name asc, foo0_.id desc
        ```

4. 结论

    本文探讨了Java持久性API中的简单实体以及一对多关系中的实体的排序替代方案。这些方法将分类工作的负担委托给数据库层。
