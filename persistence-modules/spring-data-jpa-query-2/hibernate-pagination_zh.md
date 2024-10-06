# [hibernate分页](https://www.baeldung.com/hibernate-pagination)

1. 一览表

    本文是对hibernate中的分页的简要介绍。我们将查看标准HQL以及ScrollableResults API，最后，使用Hibernate Criteria进行分页。

2. 使用HQL和setFirstResult、setMaxResults API进行分页

    在Hibernate中进行分页的最简单和最常见的方法是使用HQL：

    ```java
    Session session = sessionFactory.openSession();
    Query<Foo> query = session.createQuery("From Foo", Foo.class);
    query.setFirstResult(0);
    query.setMaxResults(10);
    List<Foo> fooList = fooList = query.list();
    ```

    这个例子使用了一个基本的Foo实体，与具有JQL实现的JPA非常相似——唯一的区别是查询语言。

    如果我们打开Hibernate的日志记录，我们将看到以下SQL正在运行：

    ```log
    Hibernate:
        select
            foo0_.id as id1_1_,
            foo0_.name as name2_1_
        from
            Foo foo0_ limit ?
    ```

    1. 总数和最后一页

        如果不知道实体总数，分页解决方案就不完整：

        ```java
        String countQ = "Select count (f.id) from Foo f";
        Query<Long> countQuery = session.createQuery(countQ, Long.class);
        Long countResults = countQuery.uniqueResult();
        ```

        最后，根据总数和给定的页面大小，我们可以计算最后一页：

        ```java
        int pageSize = 10;
        int lastPageNumber = (int) (Math.ceil(countResults / pageSize));
        ```

        此时，我们可以查看分页的完整示例，其中我们正在计算最后一页，然后检索它：

        ```java
        @Test
        public void givenEntitiesExist_whenRetrievingLastPage_thenCorrectSize() {
            int pageSize = 10;
            String countQ = "Select count (f.id) from Foo f";
            Query<Long> countQuery = session.createQuery(countQ, Long.class);
            Long countResults = (Long) countQuery.uniqueResult();
            int lastPageNumber = (int) (Math.ceil(countResults / pageSize));

            Query<Foo> selectQuery = session.createQuery("From Foo", Foo.class);
            selectQuery.setFirstResult((lastPageNumber - 1) * pageSize);
            selectQuery.setMaxResults(pageSize);
            List<Foo> lastPage = selectQuery.list();

            assertThat(lastPage, hasSize(lessThan(pageSize + 1)));
        }
        ```

3. 使用HQL和ScrollableResults API进行Hibernate的分页

    使用ScrollableResults实现分页有可能减少数据库调用。这种方法在程序滚动时流式传输结果集，因此不需要重复查询来填充每一页：

    ```java
    String hql = "FROM Foo f order by f.name";
    Query query = session.createQuery(hql);
    int pageSize = 10;

    ScrollableResults resultScroll = query.scroll(ScrollMode.FORWARD_ONLY);
    resultScroll.first();
    resultScroll.scroll(0);
    List<Foo> fooPage = Lists.newArrayList();
    int i = 0;
    while (pageSize > i++) {
        fooPage.add((Foo) resultScroll.get(0));
        if (!resultScroll.next())
            break;
    }
    ```

    此方法不仅省时（仅一次数据库调用），而且允许用户在没有额外查询的情况下访问结果集的总计数：

    ```java
    resultScroll.last();
    int totalResults = resultScroll.getRowNumber() + 1;
    ```

    另一方面，请记住，尽管滚动相当高效，但大窗口可能会占用大量内存。

4. 使用标准API进行Hibernate的分页

    最后，让我们看看一个更灵活的解决方案——使用标准：

    ```java
    CriteriaQuery<Foo> selectQuery = session.getCriteriaBuilder().createQuery(Foo.class);
    selectQuery.from(Foo.class);
    SelectionQuery<Foo> query = session.createQuery(selectQuery);
    query.setFirstResult(0);
    query.setMaxResults(pageSize);
    List<Foo> firstPage = query.list();
    ```

    hibernate标准查询API使获取总计数变得非常简单：

    ```java
    HibernateCriteriaBuilder qb = session.getCriteriaBuilder();
    CriteriaQuery<Long> cq = qb.createQuery(Long.class);
    cq.select(qb.count(cq.from(Foo.class)));
    final Long count = session.createQuery(cq).getSingleResult();
    ```

    如您所见，使用此API将产生比普通HQL更详细的代码，但API完全类型安全，更灵活。

5. 结论

    本文简要介绍了在Hibernate中进行分页的各种方法。
