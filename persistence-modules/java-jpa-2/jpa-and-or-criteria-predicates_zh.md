# [结合JPA And/Or 标准谓詞](https://www.baeldung.com/jpa-and-or-criteria-predicates)

1. 一览表

    在查询数据库中的记录时，JPA标准API可以轻松用于添加多个AND/OR条件。在本教程中，我们将探索结合多个AND/OR谓词的JPA标准查询的快速示例。

    如果您还不熟悉谓词，我们建议您先阅读[基本的JPA标准查询](https://www.baeldung.com/spring-data-criteria-queries)。

2. 示例应用程序

    对于我们的示例，我们将考虑项目实体的清单，每个实体都有一个id、名称、颜色和等级：

    ```java
    @Entity
    public class Item {

        @Id
        private Long id;
        private String color;
        private String grade;
        private String name;
        
        // standard getters and setters
    }
    ```

3. 使用AND谓詞组合两个OR谓詞

    让我们考虑一个场景，我们需要找到两者兼有之的项目：

    ```txt
    red or blue color
    AND
    A or B grade
    ```

    我们可以使用JPA Criteria API的and（）和or（）复合谓词轻松完成此操作。

    首先，我们将设置我们的查询：

    ```java
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);
    Root<Item> itemRoot = criteriaQuery.from(Item.class);
    ```

    现在我们需要构建一个谓詞来查找蓝色或红色的项目：

    ```java
    Predicate predicateForBlueColor
    = criteriaBuilder.equal(itemRoot.get("color"), "blue");
    Predicate predicateForRedColor
    = criteriaBuilder.equal(itemRoot.get("color"), "red");
    Predicate predicateForColor
    = criteriaBuilder.or(predicateForBlueColor, predicateForRedColor);
    ```

    接下来，我们将构建一个谓詞来查找A级或B级项目：

    ```java
    Predicate predicateForGradeA
    = criteriaBuilder.equal(itemRoot.get("grade"), "A");
    Predicate predicateForGradeB
    = criteriaBuilder.equal(itemRoot.get("grade"), "B");
    Predicate predicateForGrade
    = criteriaBuilder.or(predicateForGradeA, predicateForGradeB);
    ```

    最后，我们将定义这两个的AND谓詞，应用where（）方法，并执行我们的查询：

    ```java
    Predicate finalPredicate
    = criteriaBuilder.and(predicateForColor, predicateForGrade);
    criteriaQuery.where(finalPredicate);
    List<Item> items = entityManager.createQuery(criteriaQuery).getResultList();
    ```

4. 使用OR谓詞组合两个和谓詞

    相反，让我们考虑一个场景，我们需要找到具有以下任何项的项目：

    ```txt
    red color and grade D
    OR
    blue color and grade B
    ```

    逻辑非常相似，但在这里我们首先创建两个AND谓詞，然后使用OR谓詞将它们组合起来：

    ```java
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Item> criteriaQuery = criteriaBuilder.createQuery(Item.class);
    Root<Item> itemRoot = criteriaQuery.from(Item.class);

    Predicate predicateForBlueColor
    = criteriaBuilder.equal(itemRoot.get("color"), "red");
    Predicate predicateForGradeA
    = criteriaBuilder.equal(itemRoot.get("grade"), "D");
    Predicate predicateForBlueColorAndGradeA
    = criteriaBuilder.and(predicateForBlueColor, predicateForGradeA);

    Predicate predicateForRedColor
    = criteriaBuilder.equal(itemRoot.get("color"), "blue");
    Predicate predicateForGradeB
    = criteriaBuilder.equal(itemRoot.get("grade"), "B");
    Predicate predicateForRedColorAndGradeB
    = criteriaBuilder.and(predicateForRedColor, predicateForGradeB);

    Predicate finalPredicate
    = criteriaBuilder
    .or(predicateForBlueColorAndGradeA, predicateForRedColorAndGradeB);
    criteriaQuery.where(finalPredicate);
    List<Item> items = entityManager.createQuery(criteriaQuery).getResultList();
    ```

5. 结论

    在本文中，我们使用JPA标准API来实现需要结合AND/OR谓词的用例。
