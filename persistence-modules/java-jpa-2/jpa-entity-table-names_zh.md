# [使用JPA将实体类名称映射到SQL表名](https://www.baeldung.com/jpa-entity-table-names)

1. 介绍

    在这个简短的教程中，我们将学习如何使用JPA设置SQL表名。

    我们将介绍JPA如何生成默认名称以及如何提供自定义名称。

2. 默认表名

    JPA默认表名生成是特定于其实现的。

    例如，在Hibernate中，默认表名是第一个字母大写的类名。它是通过隐式命名策略合同确定的。

    但我们可以通过实现[PhysicalNamingStrategy](https://www.baeldung.com/hibernate-naming-strategy)接口来改变这种行为。

3. 使用@Table

    设置自定义SQL表名称的最简单方法是使用@jakarta.persistence.Table注释实体并定义其名称参数：

    ```java
    @Entity
    @Table(name = "ARTICLES")
    public class Article {
        // ...
    }
    ```

    我们还可以将表名存储在静态最终变量中：

    ```java
    @Entity
    @Table(name = Article.TABLE_NAME)
    public class Article {
        public static final String TABLE_NAME= "ARTICLES";
        // ...
    }
    ```

4. 在JPQL查询中覆盖表名

    默认情况下，在JPQL查询中，我们使用实体类名称：

    `select * from Article`

    但我们可以通过在@jakarta.persistence.Entity注释中定义名称参数来更改它：

    `@Entity(name = "MyArticle")`

    然后我们将JPQL查询更改为：

    `select * from MyArticle`

5.结论

    在本文中，我们了解了JPA如何生成默认表名，以及如何使用JPA设置SQL表名。
