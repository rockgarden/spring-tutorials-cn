# [标准API – IN表达式的示例](https://www.baeldung.com/jpa-criteria-api-in-expressions)

1. 一览表

    我们经常遇到需要根据单值属性是否是给定集合的成员来查询实体的问题。

    在本教程中，我们将学习如何在标准API的帮助下解决这个问题。

2. 样本实体

    在我们开始之前，让我们来看看我们将在文章中使用的实体。

    我们有一个部门员工班，与部门班有多对一的关系：

    ```java
    @Entity
    public class DeptEmployee {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private long id;

        private String title;

        @ManyToOne
        private Department department;
    }
    ```

    此外，映射到多个部门员工的部门实体：

    ```java
    @Entity
    public class Department {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private long id;

        private String name;

        @OneToMany(mappedBy="department")
        private List<DeptEmployee> employees;
    }
    ```

3. CriteriaBuilder.In

    首先，让我们使用CriteriaBuilder接口。in（）方法接受表达式，并返回CriteriaBuilder.In类型的newPredicate。它可用于测试给定的表达式是否包含在值列表中：

    ```java
    CriteriaQuery<DeptEmployee> criteriaQuery = 
    criteriaBuilder.createQuery(DeptEmployee.class);
    Root<DeptEmployee> root = criteriaQuery.from(DeptEmployee.class);
    In<String> inClause = criteriaBuilder.in(root.get("title"));
    for (String title : titles) {
        inClause.value(title);
    }
    criteriaQuery.select(root).where(inClause);
    ```

4. Expression.In

    或者，我们可以使用表达式接口中的一组过载的in（）方法：

    ```java
    criteriaQuery.select(root)
    .where(root.get("title")
    .in(titles));
    ```

    与CriterareBuilder.in（）相反，Expression.in（）接受值的集合。正如我们所看到的，它也稍微简化了我们的代码。

5. 使用子查询的IN表达式

    到目前为止，我们使用了具有预定义值的集合。现在，让我们来看看一个集合是从子查询的输出中派生的示例。

    例如，我们可以获取所有属于部门的部门员工，其名称中包含指定关键字：

    ```java
    Subquery<Department> subquery = criteriaQuery.subquery(Department.class);
    Root<Department> dept = subquery.from(Department.class);
    subquery.select(dept)
    .distinct(true)
    .where(criteriaBuilder.like(dept.get("name"), "%" + searchKey + "%"));

    criteriaQuery.select(emp)
    .where(criteriaBuilder.in(emp.get("department")).value(subquery));
    ```

    在这里，我们创建了一个子查询，然后将其传递到value（）中，作为搜索部门实体的表达式。

6. 结论

    在这篇简短的文章中，我们学习了使用标准API实现IN操作的不同方法。我们还探索了如何将标准API与子查询一起使用。
