# [JPA查询参数使用](https://www.baeldung.com/jpa-query-parameters)

1. 介绍

    使用JPA构建查询并不难；然而，我们有时会忘记那些会带来巨大变化的简单事情。

    其中一件事是JPA查询参数，这就是我们将在本教程中重点关注的内容。

2. 查询参数是什么？

    让我们从解释什么是查询参数开始。

    查询参数是构建和执行参数化查询的一种方式。所以，而不是：

    `SELECT * FROM employees e WHERE e.emp_number = '123';`

    我们会做：

    `SELECT * FROM employees e WHERE e.emp_number = ?;`

    通过使用JDBC准备的语句，我们需要在执行查询之前设置参数：

    `pStatement.setString(1, 123);`

3. 我们为什么要使用查询参数？

    我们本可以使用字面意思，而不是使用查询参数，尽管这不是推荐的方法，正如我们现在将看到的。

    让我们重写之前的查询，使用JPA API通过emp_number获取员工，但我们不使用参数，而是使用字面值，以便我们可以清楚地说明情况：

    ```java
    String empNumber = "A123";
    TypedQuery<Employee> query = em.createQuery(
    "SELECT e FROM Employee e WHERE e.empNumber = '" + empNumber + "'", Employee.class);
    Employee employee = query.getSingleResult();
    ```

    这种方法有一些缺点：

    - 嵌入参数会带来安全风险，使我们容易受到JPQL注入攻击。攻击者可能会注入任何意外且可能危险的JPQL表达式，而不是预期值。
    - 根据我们使用的JPA实现和我们应用程序的启发式，查询缓存可能会耗尽。每次我们使用每个新值/参数时，都会构建、编译和缓存一个新的查询。至少，它不会有效，还可能导致意外的OutOfMemoryError。

4. JPA查询参数

    与JDBC准备的语句参数类似，JPA指定了两种不同的写入参数化查询的方式，使用：

    - 位置(Positional)参数
    - 命名(Named)参数

    我们可以使用位置参数或命名参数，但不得在同一查询中混合它们。

    1. 位置参数

        使用位置参数是避免前面列出的上述问题的一种方式。

        让我们看看我们如何在位置参数的帮助下编写这样的查询：

        ```java
        TypedQuery<Employee> query = em.createQuery(
        "SELECT e FROM Employee e WHERE e.empNumber = ?1", Employee.class);
        String empNumber = "A123";
        Employee employee = query.setParameter(1, empNumber).getSingleResult();
        ```

        正如我们在前面的例子中看到的，我们通过键入问号，后跟一个正整数来在查询中声明这些参数。我们将从1开始并向前推进，每次增加一个。

        我们可能会在同一查询中多次使用相同的参数，这使得这些参数与命名参数更相似。

        参数编号是一个非常有用的功能，因为它提高了可用性、可读性和维护性。

        值得一提的是，本机SQL查询也支持位置参数绑定。

    2. 集合值位置参数

        如前所述，我们也可以使用集合值参数：

        ```java
        TypedQuery<Employee> query = entityManager.createQuery(
        "SELECT e FROM Employee e WHERE e.empNumber IN (?1)" , Employee.class);
        List<String> empNumbers = Arrays.asList("A123", "A124");
        List<Employee> employees = query.setParameter(1, empNumbers).getResultList();
        ```

    3. 命名参数

        命名参数与位置参数非常相似；然而，通过使用它们，我们使参数更加明确，查询也变得更加可读：

        ```java
        TypedQuery<Employee> query = em.createQuery(
        "SELECT e FROM Employee e WHERE e.empNumber = :number" , Employee.class);
        String empNumber = "A123";
        Employee employee = query.setParameter("number", empNumber).getSingleResult();
        ```

        之前的示例查询与第一个相同，但我们使用了：数字，一个命名的参数，而不是？1.

        我们可以看到，我们用冒号声明参数，后跟字符串标识符（JPQL标识符），这是我们将在运行时设置的实际值的占位符。在执行查询之前，我们必须通过发布setParameter方法来设置一个或多个参数。

        值得评论的一件有趣的事情是，TypedQuery支持方法链式，当必须设置多个参数时，这非常有用。

        让我们继续使用两个命名参数来创建之前查询的变体来说明方法链：

        ```java
        TypedQuery<Employee> query = em.createQuery(
        "SELECT e FROM Employee e WHERE e.name = :name AND e.age = :empAge" , Employee.class);
        String empName = "John Doe";
        int empAge = 55;
        List<Employee> employees = query
        .setParameter("name", empName)
        .setParameter("empAge", empAge)
        .getResultList();
        ```

        在这里，我们正在检索所有具有指定姓名和年龄的员工。正如我们清楚地看到的，人们可以预期的那样，我们可以构建具有多个参数的查询，并根据需要进行尽可能多的参数。

        如果出于某种原因，我们确实需要在同一查询中多次使用相同的参数，我们只需要通过发布“setParameter”方法来设置一次。在运行时，指定值将替换参数的每次出现。

        最后，值得一提的是，Java Persistence API规范没有强制要求本机查询支持命名参数。即使一些实现，如Hibernate，确实支持它，我们也需要考虑到，如果我们确实使用它，查询将不会那么可移植。

    4. 集合值命名参数

        为了清楚起见，我们还演示一下这如何与集合值参数一起工作：

        ```java
        TypedQuery<Employee> query = entityManager.createQuery(
        "SELECT e FROM Employee e WHERE e.empNumber IN (:numbers)" , Employee.class);
        List<String> empNumbers = Arrays.asList("A123", "A124");
        List<Employee> employees = query.setParameter("numbers", empNumbers).getResultList();
        ```

        正如我们所看到的，它的工作方式与位置参数相似。

5. 标准查询参数

    可以使用[JPA标准API构建](https://www.baeldung.com/hibernate-criteria-queries-metamodel)JPA查询，Hibernate的[官方文档](https://docs.jboss.org/hibernate/orm/5.2/topical/html_single/metamodelgen/MetamodelGenerator.html)对此进行了非常详细的解释。

    在这种类型的查询中，我们通过使用对象而不是名称或索引来表示参数。

    让我们再次构建相同的查询，但这次使用Criteria API来演示如何在处理CritriadQuery时处理查询参数：

    ```java
    CriteriaBuilder cb = em.getCriteriaBuilder();

    CriteriaQuery<Employee> cQuery = cb.createQuery(Employee.class);
    Root<Employee> c = cQuery.from(Employee.class);
    ParameterExpression<String> paramEmpNumber = cb.parameter(String.class);
    cQuery.select(c).where(cb.equal(c.get(Employee_.empNumber), paramEmpNumber));

    TypedQuery<Employee> query = em.createQuery(cQuery);
    String empNumber = "A123";
    query.setParameter(paramEmpNumber, empNumber);
    Employee employee = query.getResultList();
    ```

    对于这种类型的查询，参数的机制略有不同，因为我们使用参数对象，但本质上没有区别。

    在前面的例子中，我们可以看到Employee_类的用法。我们用Hibernate元模型生成器生成了这个类。这些组件是静态JPA元模型的一部分，它允许以强类型化的方式构建标准查询。

6. 结论

    在本文中，我们重点介绍了使用JPA查询参数或输入参数构建查询的机制。

    我们了解到，我们有两种类型的查询参数，位置参数和命名参数，由我们决定哪种查询参数最适合我们的目标。

    同样值得注意的是，除表达式外，所有查询参数都必须是单值的。对于表达式，我们可能会使用集合值的输入参数，如数组或列表，如前几个例子所示。
