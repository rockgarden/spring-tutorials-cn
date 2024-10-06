# [JPA加入类型](https://www.baeldung.com/jpa-join-types)

1. 一览表

    在本教程中，我们将研究JPA支持的不同连接类型。

    为此，我们将使用JPQL，一种JPA的查询语言。

2. 样本数据模型

    让我们来看看我们将在示例中使用的示例数据模型。

    首先，我们将创建一个员工实体：

    ```java
    @Entity
    public class Employee {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        private String name;

        private int age;

        @ManyToOne
        private Department department;

        @OneToMany(mappedBy = "employee")
        private List<Phone> phones;

        // getters and setters...
    }
    ```

    每位员工将只分配到一个部门：

    ```java
    @Entity
    public class Department {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;

        private String name;

        @OneToMany(mappedBy = "department")
        private List<Employee> employees;

        // getters and setters...
    }
    ```

    最后，每位员工将拥有多部手机：

    ```java
    @Entity
    public class Phone {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        private String number;

        @ManyToOne
        private Employee employee;

        // getters and setters...
    }
    ```

3. 内部连接

    我们将从内部连接开始。当两个或多个实体被内接时，结果中只收集与连接条件匹配的记录。

    1. 隐式内部连接与单值关联导航

        内部连接可以是隐式的。顾名思义，开发人员没有指定隐式内部连接。每当我们浏览单个值关联时，JPA都会自动创建一个隐式连接：

        ```java
        @Test
        public void whenPathExpressionIsUsedForSingleValuedAssociation_thenCreatesImplicitInnerJoin() {
            TypedQuery<Department> query
            = entityManager.createQuery(
                "SELECT e.department FROM Employee e", Department.class);
            List<Department> resultList = query.getResultList();

            // Assertions...
        }
        ```

        在这里，员工实体与部门实体有着多对一的关系。如果我们从员工实体导航到她的部门，指定e.部门，我们将导航一个单值关联。因此，JPA将创建一个内部连接。此外，连接条件将从映射元数据中得出。

    2. 带有单一值关联的明确内部连接

        接下来，我们将查看在JPQL查询中使用JOIN关键字的显式内部连接：

        ```java
        @Test
        public void whenJoinKeywordIsUsed_thenCreatesExplicitInnerJoin() {
            TypedQuery<Department> query
            = entityManager.createQuery(
                "SELECT d FROM Employee e JOIN e.department d", Department.class);
            List<Department> resultList = query.getResultList();

            // Assertions...
        }
        ```

        在此查询中，我们在FROM子句中指定了JOIN关键字和关联的部门实体，而在之前的查询中，它们根本没有指定。然而，除了这种语法差异外，生成的SQL查询将非常相似。

        我们还可以指定一个可选的INNER关键字：

        ```java
        @Test
        public void whenInnerJoinKeywordIsUsed_thenCreatesExplicitInnerJoin() {
            TypedQuery<Department> query
            = entityManager.createQuery(
                "SELECT d FROM Employee e INNER JOIN e.department d", Department.class);
            List<Department> resultList = query.getResultList();

            // Assertions...
        }
        ```

        因此，既然JPA自动创建了隐式内部连接，我们什么时候需要显式？

        首先，当我们指定路径表达式时，JPA只会创建隐式内部连接。例如，当我们只想选择有部门的员工，并且我们不使用像e.department这样的路径表达式时，我们应该在查询中使用JOIN关键字。

        其次，当我们明确时，更容易知道发生了什么。

    3. 与收藏价值关联的明确内部加入

        我们需要明确的另一个地方是集合值关联。

        如果我们看一下我们的数据模型，员工与电话有一对多的关系。与之前的例子一样，我们可以尝试编写一个类似的查询：

        `SELECT e.phones FROM Employee e`

        然而，这可能不会像我们预期的那样奏效。由于所选关联e.phones是集合值的，我们将获得一个集合列表，而不是电话实体：

        ```java
        @Test
        public void whenCollectionValuedAssociationIsSpecifiedInSelect_ThenReturnsCollections() {
            TypedQuery<Collection> query
            = entityManager.createQuery(
                "SELECT e.phones FROM Employee e", Collection.class);
            List<Collection> resultList = query.getResultList();

            //Assertions
        }
        ```

        此外，如果我们想在WHERE子句中过滤电话实体，JPA将不允许。这是因为路径表达式不能从集合值关联中继续。因此，例如，e.phones.number无效。

        相反，我们应该创建一个显式内部连接，并为电话实体创建一个别名。然后，我们可以在SELECT或WHERE子句中指定电话实体：

        ```java
        @Test
        public void whenCollectionValuedAssociationIsJoined_ThenCanSelect() {
            TypedQuery<Phone> query
            = entityManager.createQuery(
                "SELECT ph FROM Employee e JOIN e.phones ph WHERE ph LIKE '1%'", Phone.class);
            List<Phone> resultList = query.getResultList();

            // Assertions...
        }
        ```

4. 外部连接

    当两个或多个实体被外部连接时，满足连接条件的记录以及左侧实体中的记录将在结果中收集：

    ```java
    @Test
    public void whenLeftKeywordIsSpecified_thenCreatesOuterJoinAndIncludesNonMatched() {
        TypedQuery<Department> query
        = entityManager.createQuery(
            "SELECT DISTINCT d FROM Department d LEFT JOIN d.employees e", Department.class);
        List<Department> resultList = query.getResultList();

        // Assertions...
    }
    ```

    在这里，结果将包含有关联员工的部门，以及没有关联员工的部门。

    这也被称为左外连接。JPA不提供右连接，我们也从正确的实体那里收集不匹配的记录。尽管，我们可以通过在FROM子句中交换实体来模拟右连接。

5. 加入WHERE条款

    1. 有一个条件

        我们可以在FROM子句中列出两个实体，然后在WHERE子句中指定连接条件。

        这可能很方便，特别是当数据库级外键不到位时：

        ```java
        @Test
        public void whenEntitiesAreListedInFromAndMatchedInWhere_ThenCreatesJoin() {
            TypedQuery<Department> query
            = entityManager.createQuery(
                "SELECT d FROM Employee e, Department d WHERE e.department = d", Department.class);
            List<Department> resultList = query.getResultList();

            // Assertions...
        }
        ```

        在这里，我们正在加入员工和部门实体，但这次在WHERE子句中指定了一个条件。

    2. 无条件（Cartesian产品）

        同样，我们可以在FROM子句中列出两个实体，而无需指定任何连接条件。在这种情况下，我们将拿回Cartesian产品。这意味着第一个实体中的每个记录都与第二个实体中的所有其他记录配对：

        ```java
        @Test
        public void whenEntitiesAreListedInFrom_ThenCreatesCartesianProduct() {
            TypedQuery<Department> query
            = entityManager.createQuery(
                "SELECT d FROM Employee e, Department d", Department.class);
            List<Department> resultList = query.getResultList();

            // Assertions...
        }
        ```

        正如我们可以猜到的那样，这类查询不会很好地执行。

6. 多个连接

    到目前为止，我们已经使用两个实体来执行连接，但这不是规则。我们还可以在单个JPQL查询中加入多个实体：

    ```java
    @Test
    public void whenMultipleEntitiesAreListedWithJoin_ThenCreatesMultipleJoins() {
        TypedQuery<Phone> query
        = entityManager.createQuery(
            "SELECT ph FROM Employee e
        JOIN e.department d
        JOIN e.phones ph
        WHERE d.name IS NOT NULL", Phone.class);
        List<Phone> resultList = query.getResultList();

        // Assertions...
    }
    ```

    在这里，我们正在选择所有拥有部门的员工的所有电话。与其他内部连接类似，我们没有指定条件，因为JPA从映射元数据中提取此信息。

7. 获取加入

    现在让我们来谈谈获取连接。它们的主要用途是为当前查询[急切地获取懒加载(fetching lazy-loaded associations eagerly)](https://www.baeldung.com/hibernate-lazy-eager-loading)的关联。

    在这里，我们将急切地加载员工协会：

    ```java
    @Test
    public void whenFetchKeywordIsSpecified_ThenCreatesFetchJoin() {
        TypedQuery<Department> query
        = entityManager.createQuery(
            "SELECT d FROM Department d JOIN FETCH d.employees", Department.class);
        List<Department> resultList = query.getResultList();

        // Assertions...
    }
    ```

    虽然这个查询看起来与其他查询非常相似，但有一个区别；员工们急切地加载。这意味着，一旦我们在上述测试中调用getResultList，部门实体将加载其员工字段，从而节省我们再次访问数据库。

    然而，我们必须意识到记忆权衡。我们可能更有效率，因为我们只执行了一个查询，但我们也一次将所有部门及其员工加载到内存中。

    我们还可以以类似于外部连接的方式执行外部获取连接，我们从左侧实体收集与连接条件不匹配的记录。此外，它急切地加载指定的关联：

    ```java
    @Test
    public void whenLeftAndFetchKeywordsAreSpecified_ThenCreatesOuterFetchJoin() {
        TypedQuery<Department> query
        = entityManager.createQuery(
            "SELECT d FROM Department d LEFT JOIN FETCH d.employees", Department.class);
        List<Department> resultList = query.getResultList();

        // Assertions...
    }
    ```

8. 摘要

    在本文中，我们介绍了JPA加入类型。
