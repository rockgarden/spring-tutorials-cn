# [JPA分页](https://www.baeldung.com/jpa-pagination)

1. 一览表

    本文说明了如何在Java持久性API中实现分页。

    它解释了如何使用基本的JQL和更安全的基于标准的API进行分页，讨论了每个实现的优势和已知问题。

2. 使用JQL和setFirstResult（）、setMaxResults（）API进行分页

    实现分页的最简单方法是使用Java查询语言——创建查询并通过setMaxResults和setFirstResult进行配置：

    ```java
    Query query = entityManager.createQuery("From Foo");
    int pageNumber = 1;
    int pageSize = 10;
    query.setFirstResult((pageNumber-1) * pageSize); 
    query.setMaxResults(pageSize);
    List <Foo> fooList = query.getResultList();
    ```

    API很简单：

    - setFirstResult（int）：设置结果集中的偏移位置以启动分页
    - setMaxResults（int）：设置应包含在页面中的最大实体数量

    1. 总数和最后一页

        为了获得更完整的分页解决方案，我们还需要获得总结果计数：

        ```java
        Query queryTotal = entityManager.createQuery
            ("Select count(f.id) from Foo f");
        long countResult = (long)queryTotal.getSingleResult();
        ```

        计算最后一页也非常有用：

        ```java
        int pageSize = 10;
        int pageNumber = (int) ((countResult / pageSize) + 1);
        ```

        请注意，这种获取结果集总计数的方法确实需要额外的查询（用于计数）。

3. 使用实体ID的JQL分页

    一个简单的替代分页策略是首先检索完整的ID，然后——基于这些ID——检索完整的实体。这允许更好地控制实体获取——但这也意味着它需要加载整个表来检索id：

    ```java
    Query queryForIds = entityManager.createQuery(
    "Select f.id from Foo f order by f.lastName");
    List<Integer> fooIds = queryForIds.getResultList();
    Query query = entityManager.createQuery(
    "Select f from Foo e where f.id in :ids");
    query.setParameter("ids", fooIds.subList(0,10));
    List<Foo> fooList = query.getResultList();
    ```

    最后，还要注意，需要2个不同的查询才能检索完整的结果。

4. 使用标准API进行JPA分页

    接下来，让我们看看如何利用JPA标准API来实现分页：

    ```java
    int pageSize = 10;
    CriteriaBuilder criteriaBuilder = entityManager
    .getCriteriaBuilder();
    CriteriaQuery<Foo> criteriaQuery = criteriaBuilder
    .createQuery(Foo.class);
    Root<Foo> from = criteriaQuery.from(Foo.class);
    CriteriaQuery<Foo> select = criteriaQuery.select(from);
    TypedQuery<Foo> typedQuery = entityManager.createQuery(select);
    typedQuery.setFirstResult(0);
    typedQuery.setMaxResults(pageSize);
    List<Foo> fooList = typedQuery.getResultList();
    ```

    当目的是创建动态的、故障安全的查询时，这很有用。与“硬编码”、“基于字符串”的JQL或HQL查询相比，JPA标准减少了运行时失败，因为编译器动态检查查询错误。

    使用JPA标准，获得实体总数足够简单：

    ```java
    CriteriaQuery<Long> countQuery = criteriaBuilder
    .createQuery(Long.class);
    countQuery.select(criteriaBuilder.count(
    countQuery.from(Foo.class)));
    Long count = entityManager.createQuery(countQuery)
    .getSingleResult();
    ```

    最终结果是使用JPA标准API的完整分页解决方案：

    ```java
    int pageNumber = 1;
    int pageSize = 10;
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Long> countQuery = criteriaBuilder
    .createQuery(Long.class);
    countQuery.select(criteriaBuilder
    .count(countQuery.from(Foo.class)));
    Long count = entityManager.createQuery(countQuery)
    .getSingleResult();

    CriteriaQuery<Foo> criteriaQuery = criteriaBuilder
    .createQuery(Foo.class);
    Root<Foo> from = criteriaQuery.from(Foo.class);
    CriteriaQuery<Foo> select = criteriaQuery.select(from);

    TypedQuery<Foo> typedQuery = entityManager.createQuery(select);
    while (pageNumber < count.intValue()) {
        typedQuery.setFirstResult(pageNumber - 1);
        typedQuery.setMaxResults(pageSize);
        System.out.println("Current page: " + typedQuery.getResultList());
        pageNumber += pageSize;
    }
    ```

5. 结论

    本文探讨了JPA中可用的基本分页选项。

    有些有缺点——主要与查询性能有关，但这些缺点通常被改进的控制和整体灵活性所抵消。
