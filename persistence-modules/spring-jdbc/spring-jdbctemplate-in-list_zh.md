# [在JdbcTemplate IN子句中使用值列表](https://www.baeldung.com/spring-jdbctemplate-in-list)

1. 介绍

    在SQL语句中，我们可以使用IN运算符来测试表达式是否与列表中的任何值匹配。因此，我们可以使用IN运算符，而不是多个OR条件。

    在本教程中，我们将学习如何将值列表传递到Spring JDBC模板查询的IN子句中。

2. 将列表参数传递给IN子句

    IN运算符允许我们在WHERE子句中指定多个值。例如，我们可以使用它来查找ID在指定ID列表中的所有员工：

    `SELECT * FROM EMPLOYEE WHERE id IN (1, 2, 3)`

    通常，IN子句中的值总数是可变的。因此，我们需要创建一个可以支持动态值列表的占位符。

    1. 使用JdbcTemplate

        使用[JdbcTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html)，我们可以使用“？”字符作为值列表的占位符。“?”的数量字符将与列表的大小相同：

        ```java
        List<Employee> getEmployeesFromIdList(List<Integer> ids) {
            String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));

            List<Employee> employees = jdbcTemplate.query(
            String.format("SELECT * FROM EMPLOYEE WHERE id IN (%s)", inSql), 
            ids.toArray(), 
            (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"), 
                rs.getString("last_name")));

            return employees;
        }
        ```

        在此方法中，我们首先生成一个包含ids.size() ['?'的占位符字符串用逗号分隔的字符](https://www.baeldung.com/java-strings-concatenation)。然后我们将此字符串放入SQL语句的IN子句中。例如，如果我们的ids列表中有三个数字，SQL语句是：

        `SELECT * FROM EMPLOYEE WHERE id IN (?,?,?)`

        在查询方法中，我们将ids列表作为参数传递给IN子句中的占位符。通过这种方式，我们可以根据输入值列表执行动态SQL语句。

    2. 带有NamedParameterJdbcTemplate

        处理动态值列表的另一种方法是使用[NamedParameterJdbcTemplate](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/namedparam/NamedParameterJdbcTemplate.html)。例如，我们可以直接为输入列表创建一个命名参数：

        ```java
        List<Employee> getEmployeesFromIdListNamed(List<Integer> ids) {
            SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);

            List<Employee> employees = namedJdbcTemplate.query(
            "SELECT * FROM EMPLOYEE WHERE id IN (:ids)", 
            parameters, 
            (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"),
                rs.getString("last_name")));

            return employees;
        }
        ```

        在此方法中，我们首先构建一个包含输入ID列表的[MapSqlParameterSource](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/namedparam/MapSqlParameterSource.html)对象。然后我们只使用一个命名参数来表示动态值列表。

        在引擎盖下，NamedParameterJdbcTemplate用命名参数代替“?”占位符，并使用JdbcTemplate来执行查询。

3. 处理一个大列表

    当我们在列表中有大量值时，我们应该考虑将它们传递到JdbcTemplate查询中的替代方法。

    例如，甲骨文数据库在IN子句中不支持超过1000个字面值。

    做到这一点的一个方法是为列表创建一个临时表。然而，不同的数据库可以有不同的方法来创建临时表。例如，我们可以使用[CREATE GLOBAL TEMPORARY TABLE](https://docs.oracle.com/cd/B28359_01/server.111/b28310/tables003.htm#ADMIN11633)语句在Oracle数据库中创建一个临时表。

    让我们为H2数据库创建一个临时表：

    ```java
    List<Employee> getEmployeesFromLargeIdList(List<Integer> ids) {
        jdbcTemplate.execute("CREATE TEMPORARY TABLE IF NOT EXISTS employee_tmp (id INT NOT NULL)");

        List<Object[]> employeeIds = new ArrayList<>();
        for (Integer id : ids) {
            employeeIds.add(new Object[] { id });
        }
        jdbcTemplate.batchUpdate("INSERT INTO employee_tmp VALUES(?)", employeeIds);

        List<Employee> employees = jdbcTemplate.query(
        "SELECT * FROM EMPLOYEE WHERE id IN (SELECT id FROM employee_tmp)", 
        (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"),
        rs.getString("last_name")));

        jdbcTemplate.update("DELETE FROM employee_tmp");
    
        return employees;
    }
    ```

    在这里，我们首先创建一个临时表来包含输入列表的所有值。然后我们将输入列表的值插入到表格中。

    在我们生成的SQL语句中，IN子句中的值来自临时表，我们避免构建具有大量占位符的IN子句。

    最后，在我们完成查询后，我们可以清理临时表以备将来使用。

4. 结论

    在本文中，我们演示了如何使用JdbcTemplate和NamedParameterJdbcTemplate来传递SQL查询的IN子句的值列表。我们还提供了一种使用临时表来处理大量列表值的替代方法。
