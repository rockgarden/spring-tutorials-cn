# [SqlResultSetMapping指南](https://www.baeldung.com/jpa-sql-resultset-mapping)

1. 介绍

    在本指南中，我们将看看Java Persistence API（JPA）的SqlResultSetMapping。

    这里的核心功能涉及将数据库SQL语句中的结果集映射到Java对象中。

2. 设置

    在我们查看它的用法之前，让我们做一些设置。

    1. Maven依赖性

        我们需要的Maven依赖项是[Hibernate](http://hibernate.org/)和[H2数据库](http://www.h2database.com/html/main.html)。Hibernate为我们提供了JPA规范的实现。我们使用H2数据库作为内存数据库。

    2. 数据库

        接下来，我们将创建两个表格，如上所示：

        ```sql
        CREATE TABLE EMPLOYEE
        (id BIGINT,
        name VARCHAR(10));
        ```

        EMPLOYEE表存储一个结果实体对象。SCHEDULE_DAYS包含通过employeeId列链接到EMPLOYE表的记录：

        ```sql
        CREATE TABLE SCHEDULE_DAYS
        (id IDENTITY,
        employeeId BIGINT,
        dayOfWeek  VARCHAR(10));
        ```

        数据创建脚本可以在本指南的代码中找到。

    3. 实体对象

        我们的实体对象应该看起来相似：

        ```java
        @Entity
        public class Employee {
            @Id
            private Long id;
            private String name;
        }
        ```

        实体对象的命名可能与数据库表不同。我们可以用@Table注释类，以显式映射它们：

        ```java
        @Entity
        @Table(name = "SCHEDULE_DAYS")
        public class ScheduledDay {

            @Id
            @GeneratedValue
            private Long id;
            private Long employeeId;
            private String dayOfWeek;
        }
        ```

3. 标量映射

    现在我们有了数据，我们可以开始映射查询结果。

    1. 列结果

        虽然SqlResultSetMapping和Query注释也适用于存储库类，但在本例中，我们使用实体类上的注释。

        每个SqlResultSetMapping注释只需要一个属性，即名称。然而，如果没有成员类型之一，将不会映射任何东西。成员类型是ColumnResult、ConstructorResult和EntityResult。

        在这种情况下，ColumnResult将任何列映射到一个標量结果类型：

        ```java
        @SqlResultSetMapping(
        name="FridayEmployeeResult",
        columns={@ColumnResult(name="employeeId")})
        ```

        ColumnResult属性名称标识了我们查询中的列：

        ```java
        @NamedNativeQuery(
        name = "FridayEmployees",
        query = "SELECT employeeId FROM schedule_days WHERE dayOfWeek = 'FRIDAY'",
        resultSetMapping = "FridayEmployeeResult")
        ```

        请注意，我们的NamedNativeQuery注释中的resultSetMapping值很重要，因为它与我们ResultSetMapping声明中的name属性相匹配。

        因此，NamedNativeQuery结果集按预期映射。同样，StoredProcedure API需要此关联。

    2. 列结果测试

        我们需要一些特定于Hibernate的对象来运行我们的代码：

        ```java
        @BeforeAll
        public static void setup() {
            emFactory = Persistence.createEntityManagerFactory("java-jpa-scheduled-day");
            em = emFactory.createEntityManager();
        }
        ```

        最后，我们调用命名查询来运行我们的测试：

        ```java
        @Test
        public void whenNamedQuery_thenColumnResult() {
            List<Long> employeeIds = em.createNamedQuery("FridayEmployees").getResultList();
            assertEquals(2, employeeIds.size());
        }
        ```

4. 构造函数映射

    让我们来看看何时需要将结果集映射到整个对象。

    1. 构造函数结果

        与我们的ColumnResult示例类似，我们将在实体类ScheduledDay上添加SqlResultMapping注释。然而，要使用构造函数映射，我们需要创建一个：

        ```java
        public ScheduledDay (
        Long id, Long employeeId,
        Integer hourIn, Integer hourOut,
        String dayofWeek) {
            this.id = id;
            this.employeeId = employeeId;
            this.dayOfWeek = dayofWeek;
        }
        ```

        此外，映射指定了目标类和列（两者都是必需的）：

        ```java
        @SqlResultSetMapping(
            name="ScheduleResult",
            classes={
            @ConstructorResult(
                targetClass=com.baeldung.sqlresultsetmapping.ScheduledDay.class,
                columns={
                @ColumnResult(name="id", type=Long.class),
                @ColumnResult(name="employeeId", type=Long.class),
                @ColumnResult(name="dayOfWeek")})})
        ```

        列结果的顺序非常重要。如果列顺序不正常，构造函数将无法识别。在我们的示例中，排序与表格列相匹配，因此不需要它。

        ```java
        @NamedNativeQuery(name = "Schedules",
        query = "SELECT * FROM schedule_days WHERE employeeId = 8",
        resultSetMapping = "ScheduleResult")
        ```

        ConstructorResult的另一个独特区别是，生成的对象实例化是“新”或“独立”。当实体管理器中存在匹配的主键时，映射的实体将处于分离状态，否则它将是新的。

        有时，由于SQL数据类型与Java数据类型不匹配，我们可能会遇到运行时错误。因此，我们可以用类型明确声明它。

    2. 构造函数结果测试

        让我们在单元测试中测试构造函数结果：

        ```java
        @Test
        public void whenNamedQuery_thenConstructorResult() {
        List<ScheduledDay> scheduleDays
            = Collections.checkedList(
            em.createNamedQuery("Schedules", ScheduledDay.class).getResultList(), ScheduledDay.class);
            assertEquals(3, scheduleDays.size());
            assertTrue(scheduleDays.stream().allMatch(c -> c.getEmployeeId().longValue() == 3));
        }
        ```

5. 实体映射

    最后，对于使用更少代码的简单实体映射，让我们来看看实体结果。

    1. 单一实体

        EntityResult要求我们指定实体类，Employee。我们使用可选字段属性来进行更多控制。结合FieldResult，我们可以映射不匹配的别名和字段：

        ```java
        @SqlResultSetMapping(
        name="EmployeeResult",
        entities={
            @EntityResult(
            entityClass = com.baeldung.sqlresultsetmapping.Employee.class,
                fields={
                @FieldResult(name="id",column="employeeNumber"),
                @FieldResult(name="name", column="name")})})
        ```

        现在我们的查询应该包括别名列：

        ```java
        @NamedNativeQuery(
        name="Employees",
        query="SELECT id as employeeNumber, name FROM EMPLOYEE",
        resultSetMapping = "EmployeeResult")
        ```

        与构造函数结果类似，实体结果需要一个构造函数。然而，默认的在这里有效。

    2. 多个实体

        一旦我们映射了单个实体，映射多个实体就非常简单了：

        ```java
        @SqlResultSetMapping(
        name = "EmployeeScheduleResults",
        entities = {
            @EntityResult(entityClass = com.baeldung.jpa.sqlresultsetmapping.Employee.class),
            @EntityResult(entityClass = com.baeldung.jpa.sqlresultsetmapping.ScheduledDay.class)
        })
        ```

    3. 实体结果测试

        让我们来看看实体结果在行动：

        ```java
        @Test
        public void whenNamedQuery_thenSingleEntityResult() {
            List<Employee> employees = Collections.checkedList(
            em.createNamedQuery("Employees").getResultList(), Employee.class);
            assertEquals(3, employees.size());
            assertTrue(employees.stream().allMatch(c -> c.getClass() == Employee.class));
        }
        ```

        由于多个实体结果连接了两个实体，因此只有一个类的查询注释令人困惑。

        出于这个原因，我们在测试中定义了查询：

        ```java
        @Test
        public void whenNamedQuery_thenMultipleEntityResult() {
            Query query = em.createNativeQuery(
            "SELECT e.id as idEmployee, e.name, d.id as daysId, d.employeeId, d.dayOfWeek "
                + " FROM employee e, schedule_days d "
                + " WHERE e.id = d.employeeId", "EmployeeScheduleResults");

            List<Object[]> results = query.getResultList();
            assertEquals(4, results.size());
            assertTrue(results.get(0).length == 2);

            Employee emp = (Employee) results.get(1)[0];
            ScheduledDay day = (ScheduledDay) results.get(1)[1];

            assertTrue(day.getEmployeeId() == emp.getId());
        }
        ```

6. 结论

    在本指南中，我们查看了使用SqlResultSetMapping注释的不同选项。SqlResultSetMapping是Java Persistence API的关键部分。
