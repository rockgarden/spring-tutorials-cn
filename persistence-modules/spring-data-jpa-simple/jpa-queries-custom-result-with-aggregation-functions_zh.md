# [使用聚合函数自定义JPA查询的结果](https://www.baeldung.com/jpa-queries-custom-result-with-aggregation-functions)

1. 一览表

    虽然Spring Data JPA可以抽象创建查询，在特定情况下从数据库中检索实体，但我们有时需要自定义查询，例如当我们添加聚合函数时。

    在本教程中，我们将重点介绍如何将这些查询的结果转换为对象。我们将探索两种不同的解决方案，一种涉及JPA规范和POJO，另一种使用Spring Data Projection。

2. JPA查询和聚合问题

    JPA查询通常将其结果生成为映射实体的实例。然而，具有聚合函数的查询通常将结果返回为对象[]。

    为了理解这个问题，让我们根据帖子和评论之间的关系定义一个域模型：

    ```java
    @Entity
    public class Post {
        @Id
        private Integer id;
        private String title;
        private String content;
        @OneToMany(mappedBy = "post")
        private List comments;

        // additional properties
        // standard constructors, getters, and setters
    }

    @Entity
    public class Comment {
        @Id
        private Integer id;
        private Integer year;
        private boolean approved;
        private String content;
        @ManyToOne
        private Post post;

        // additional properties
        // standard constructors, getters, and setters
    }
    ```

    我们的模型定义了一个帖子可以有很多评论，每个评论都属于一个帖子。让我们将Spring数据存储库与此模型一起使用：

    ```java
    @Repository
    public interface CommentRepository extends JpaRepository<Comment, Integer> {
        // query methods
    }
    ```

    现在让我们数一数按年份分组的评论：

    ```java
    @Query("SELECT c.year, COUNT(c.year) FROM Comment AS c GROUP BY c.year ORDER BY c.year DESC")
    List<Object[]> countTotalCommentsByYear();
    ```

    之前的JPA查询的结果无法加载到注释实例中，因为结果是不同的形状。查询中指定的年份和COUNT与我们的实体对象不匹配。

    虽然我们仍然可以访问列表中返回的通用对象[]中的结果，但这样做将导致混乱、容易出错的代码。

3. 使用类构造函数自定义结果

    JPA规范允许我们以面向对象的方式自定义结果。因此，我们可以使用JPQL构造函数表达式来设置结果：

    ```java
    @Query("SELECT new com.baeldung.aggregation.model.custom.CommentCount(c.year, COUNT(c.year)) "
    + "FROM Comment AS c GROUP BY c.year ORDER BY c.year DESC")
    List<CommentCount> countTotalCommentsByYearClass();
    ```

    这将SELECT语句的输出绑定到POJO。指定的类需要有一个与投影属性完全匹配的构造函数，但不需要用@Entity进行注释。

    我们还可以看到，在JPQL中声明的构造函数必须有一个完全限定的名称：

    ```java
    package com.baeldung.aggregation.model.custom;

    public class CommentCount {
        private Integer year;
        private Long total;

        public CommentCount(Integer year, Long total) {
            this.year = year;
            this.total = total;
        }
        // getters and setters
    }
    ```

4. 使用Spring Data Projection自定义结果

    另一个可能的解决方案是使用 Spring Data Projection 自定义JPA查询的结果。此功能允许我们用更少的代码来投影查询结果。

    1. 自定义JPA查询的结果

        要使用基于接口的投影，我们必须定义一个由与投影属性名称匹配的getter方法组成的Java接口。让我们为我们的查询结果定义一个接口：

        ```java
        public interface ICommentCount {
            Integer getYearComment();
            Long getTotalComment();
        }
        ```

        现在让我们用返回的结果来表达我们的查询列表`<ICommentCount>`：

        ```java
        @Query("SELECT c.year AS yearComment, COUNT(c.year) AS totalComment "
        + "FROM Comment AS c GROUP BY c.year ORDER BY c.year DESC")
        List<ICommentCount> countTotalCommentsByYearInterface();
        ```

        为了允许Spring将投影值绑定到我们的接口上，我们需要使用接口中的属性名称为每个投影属性提供别名。

        然后，Spring Data将实时构建结果，并为结果的每行返回一个[代理实例](https://www.baeldung.com/java-dynamic-proxies)。

    2. 自定义本机查询的结果

        我们可能会面临JPA查询不如原生SQL快的情况，或者无法使用我们数据库引擎的特定功能。为了解决这个问题，我们将使用原生查询。

        基于接口的投影的一个优点是，我们可以将其用于本地查询。让我们再次使用ICommentCount，并将其绑定到SQL查询中：

        ```java
        @Query(value = "SELECT c.year AS yearComment, COUNT(c.*) AS totalComment "
        + "FROM comment AS c GROUP BY c.year ORDER BY c.year DESC", nativeQuery = true)
        List<ICommentCount> countTotalCommentsByYearNative();
        ```

        这与JPQL查询的工作方式相同。

5. 结论

    在本文中，我们评估了两种不同的解决方案，以解决使用聚合函数映射JPA查询结果的问题。首先，我们使用了涉及POJO类的JPA标准。

    对于第二个解决方案，我们使用了带有接口的轻量级Spring数据投影。Spring Data投影允许我们在Java和JPQL中编写更少的代码。
