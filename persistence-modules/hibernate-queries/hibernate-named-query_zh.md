# [休眠命名查询](https://www.baeldung.com/hibernate-named-query)

1. 一览表

    将HQL和SQL分散在数据访问对象中的一个主要缺点是，它使代码无法读取。因此，将所有HQL和SQL分组到一个地方，并在实际数据访问代码中仅使用它们的引用可能是有意义的。幸运的是，Hibernate允许我们使用命名查询来做到这一点。

    命名查询是具有预定义不可更改查询字符串的静态定义查询。它们在创建会话工厂时被验证，因此在出现错误时，应用程序会快速失败。

    在本文中，我们将了解如何使用@NamedQuery和@NamedNativeQuery注释来定义和使用Hibernate命名查询。

2. 实体

    让我们先看看我们将在本文中使用的实体：

    ```java
    @Entity
    public class DeptEmployee {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private long id;

        private String employeeNumber;

        private String designation;

        private String name;

        @ManyToOne
        private Department department;

        // getters and setters
    }
    ```

    在我们的示例中，我们将根据员工编号检索员工。

3. 命名查询

    要将此定义为命名查询，我们将使用org.hibernate.annotations.NamedQuery注释。它通过Hibernate功能扩展了javax.persistence.NamedQuery。

    我们将将其定义为DeptEmployee类的注释：

    ```java
    @org.hibernate.annotations.NamedQuery(name = "DeptEmployee_findByEmployeeNumber", 
    query = "from DeptEmployee where employeeNumber = :employeeNo")
    ```

    需要注意的是，每个@NamedQuery注释都正好附加到一个实体类或映射的超类上。但是，由于命名查询的范围是整个持久性单元，我们应该仔细选择查询名称，以避免冲突。我们通过使用实体名称作为前缀来实现这一目标。

    如果我们为一个实体有多个命名查询，我们将使用@NamedQueries注释对这些进行分组：

    ```java
    @org.hibernate.annotations.NamedQueries({
        @org.hibernate.annotations.NamedQuery(name = "DeptEmployee_FindByEmployeeNumber", 
        query = "from DeptEmployee where employeeNumber = :employeeNo"),
        @org.hibernate.annotations.NamedQuery(name = "DeptEmployee_FindAllByDesgination", 
        query = "from DeptEmployee where designation = :designation"),
        @org.hibernate.annotations.NamedQuery(name = "DeptEmployee_UpdateEmployeeDepartment", 
        query = "Update DeptEmployee set department = :newDepartment where employeeNumber = :employeeNo"),
    ...
    })
    ```

    请注意，HQL查询可以是DML风格的操作。因此，它不需要只是一个选择语句。例如，我们可以有一个更新查询，如上面的DeptEmployee_UpdateEmployeeDesignation。

    1. 配置查询功能

        我们可以使用@NamedQuery注释设置各种查询功能。让我们来看看一个例子：

        ```java
        @org.hibernate.annotations.NamedQuery(
        name = "DeptEmployee_FindAllByDepartment",
        query = "from DeptEmployee where department = :department",
        timeout = 1,
        fetchSize = 10
        )
        ```

        在这里，我们已经配置了超时间隔和获取大小。除了这两个之外，我们还可以设置以下功能：

        - 可缓存——查询（结果）是否可缓存
        - 缓存模式——用于此查询的缓存模式；它可以是GET、IGNORE、NORMAL、PUT或REFRESH之一
        - 缓存区域——如果查询结果是可缓存的，请命名要使用的查询缓存区域
        - 注释-添加到生成的SQL查询中的注释；针对DBA
        - flushMode – 此查询的刷新模式，ALWAYS、AUTO、COMMIT、MANUAL或PERSISTENCE_CONTEXT之一

    2. 使用命名查询

        既然我们已经定义了命名查询，让我们用它来检索员工：

        ```java
        Query<DeptEmployee> query = session.createNamedQuery("DeptEmployee_FindByEmployeeNumber",
        DeptEmployee.class);
        query.setParameter("employeeNo", "001");
        DeptEmployee result = query.getSingleResult();
        ```

        在这里，我们使用了createNamedQuery方法。它使用查询的名称并返回anorg.hibernate.query.Query对象。

4. 命名的本地查询

    除了HQL查询，我们还可以将本机SQL定义为命名查询。为此，我们可以使用@NamedNativeQuery注释。虽然它与@NamedQuery相似，但它需要更多的配置。

    让我们用一个例子来探索这个注释：

    ```java
    @org.hibernate.annotations.NamedNativeQueries(
        @org.hibernate.annotations.NamedNativeQuery(name = "DeptEmployee_FindByEmployeeName",
        query = "select * from deptemployee emp where name=:name",
        resultClass = DeptEmployee.class)
    )
    ```

    由于这是一个本机查询，我们必须告诉Hibernate将结果映射到哪个实体类。因此，我们使用了resultClass属性来完成此工作。

    另一种映射结果的方法是使用resultSetMapping属性。在这里，我们可以指定预定义的SQLResultSetMapping的名称。

    请注意，我们只能使用resultClass和resultSetMapping中的一种。

    1. 使用命名的本机查询

        要使用命名的本地查询，我们可以使用Session.createNamedQuery（）：

        ```java
        Query<DeptEmployee> query = session.createNamedQuery("DeptEmployee_FindByEmployeeName", DeptEmployee.class);
        query.setParameter("name", "John Wayne");
        DeptEmployee result = query.getSingleResult();
        ```

        或者Session.getNamedNativeQuery（）：

        ```java
        NativeQuery query = session.getNamedNativeQuery("DeptEmployee_FindByEmployeeName");
        query.setParameter("name", "John Wayne");
        DeptEmployee result = (DeptEmployee) query.getSingleResult();
        ```

        这两种方法之间的唯一区别在于返回类型。第二种方法返回NativeQuery，这是Query的一个子类。

5. 存储的程序和功能

    我们也可以使用@NamedNativeQuery注释来定义对存储过程和函数的调用：

    ```java
    @org.hibernate.annotations.NamedNativeQuery(
    name = "DeptEmployee_UpdateEmployeeDesignation",
    query = "call UPDATE_EMPLOYEE_DESIGNATION(:employeeNumber, :newDesignation)",
    resultClass = DeptEmployee.class)
    ```

    请注意，尽管这是一个更新查询，但我们使用了resultClass属性。这是因为Hibernate不支持纯原生標量查询。解决这个问题的方法是设置结果类或结果集映射。

6. 结论

    在本文中，我们看到了如何定义和使用命名的HQL和本地查询。
