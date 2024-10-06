# [Spring JDBC](https://www.baeldung.com/spring-jdbc-jdbctemplate)

1. 一览表

    在本教程中，我们将介绍Spring JDBC模块的实际用例。

    Spring JDBC中的所有课程都分为四个独立的软件包：

    - 核心——JDBC的核心功能。此软件包下的一些重要类包括JdbcTemplate、SimpleJdbcInsert、SimpleJdbcCall和NamedParameterJdbcTemplate。
    - 数据源——用于访问数据源的实用类。它还有各种数据源实现，用于在雅加达EE容器之外测试JDBC代码。
    - 对象——以面向对象的方式访问数据库。它允许运行查询并将结果作为业务对象返回。它还映射了业务对象的列和属性之间的查询结果。
    - 支持——支持核心和对象包下的类，例如，提供SQLException翻译功能

2. 配置

    让我们从数据源的一些简单配置开始。

    我们将使用MySQL数据库：

    ```java
    @Configuration
    @ComponentScan("com.baeldung.jdbc")
    public class SpringJdbcConfig {
        @Bean
        public DataSource mysqlDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://localhost:3306/springjdbc");
            dataSource.setUsername("guest_user");
            dataSource.setPassword("guest_password");

            return dataSource;
        }
    }
    ```

    或者，我们也可以很好地利用嵌入式数据库进行开发或测试。

    这是一个快速配置，它创建了H2嵌入式数据库的实例，并用简单的SQL脚本预先填充它：

    ```java
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .addScript("classpath:jdbc/schema.sql")
        .addScript("classpath:jdbc/test-data.sql").build();
    }
    ```

    最后，同样的事情也可以使用XML对数据源进行配置：

    ```xml
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" 
    destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/springjdbc"/>
        <property name="username" value="guest_user"/>
        <property name="password" value="guest_password"/>
    </bean>
    ```

3. Jdbc模板和正在运行的查询

    1. 基本查询

        JDBC模板是主要的API，我们将通过它访问我们感兴趣的大多数功能：

        - 连接的创建和关闭
        - 运行语句和存储过程调用
        - 迭代结果集并返回结果

        首先，让我们从一个简单的例子开始，看看JdbcTemplate可以做什么：

        ```java
        int result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM EMPLOYEE", Integer.class);
        ```

        这里有一个简单的插入：

        ```java
        public int addEmplyee(int id) {
            return jdbcTemplate.update(
            "INSERT INTO EMPLOYEE VALUES (?, ?, ?, ?)", id, "Bill", "Gates", "USA");
        }
        ```

        注意使用？提供参数的标准语法性格。

        接下来，让我们来看看这个语法的替代方案。

    2. 带有命名参数的查询

        为了获得对命名参数的支持，我们将使用框架提供的其他JDBC模板——命名参数Jdbc模板。

        此外，这包裹了JbdcTemplate，并提供了传统语法的替代方案，使用？指定参数。

        在引擎盖下，它将命名参数替换为JDBC？占位符和委托给wrappedJDCtemplate来运行查询：

        ```java
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", 1);
        return namedParameterJdbcTemplate.queryForObject(
        "SELECT FIRST_NAME FROM EMPLOYEE WHERE ID = :id", namedParameters, String.class);
        ```

        注意我们如何使用MapSqlParameterSource来提供命名参数的值。

        让我们来看看如何使用bean的属性来确定命名参数：

        ```java
        Employee employee = new Employee();
        employee.setFirstName("James");

        String SELECT_BY_ID = "SELECT COUNT(*) FROM EMPLOYEE WHERE FIRST_NAME = :firstName";

        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(employee);
        return namedParameterJdbcTemplate.queryForObject(
        SELECT_BY_ID, namedParameters, Integer.class);
        ```

        请注意，我们现在如何使用BeanPropertySqlParameterSource实现，而不是像以前那样手动指定命名参数。

    3. 将查询结果映射到Java对象

        另一个非常有用的功能是通过实现RowMapper接口将查询结果映射到Java对象的能力。

        例如，对于查询返回的每行，Spring使用行映射器填充java bean：

        ```java
        public class EmployeeRowMapper implements RowMapper<Employee> {
            @Override
            public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
                Employee employee = new Employee();

                employee.setId(rs.getInt("ID"));
                employee.setFirstName(rs.getString("FIRST_NAME"));
                employee.setLastName(rs.getString("LAST_NAME"));
                employee.setAddress(rs.getString("ADDRESS"));

                return employee;
            }
        }
        ```

        随后，我们现在可以将行映射器传递给查询API，并获得完全填充的Java对象：

        ```java
        String query = "SELECT * FROM EMPLOYEE WHERE ID = ?";
        Employee employee = jdbcTemplate.queryForObject(query, new EmployeeRowMapper(), id);
        ```

4. 例外翻译

    Spring开箱即用，具有自己的数据异常层次结构——DataAccessException作为根异常——并将所有底层原始异常转换为它。

    因此，我们通过不处理低级持久性异常来保持理智。我们还受益于Spring包装DataAccessException或其子类之一中的低级异常这一事实。

    这也使异常处理机制独立于我们正在使用的底层数据库。

    除了默认的SQLErrorCodeSQLExceptionTranslator外，我们还可以提供我们自己的SQLExceptionTranslator实现。

    以下是自定义实现的快速示例——当重复密钥违规时自定义错误消息，这会导致使用H2时出现[错误代码23505](https://www.h2database.com/javadoc/org/h2/api/ErrorCode.html#c23505)：

    ```java
    public class CustomSQLErrorCodeTranslator extends SQLErrorCodeSQLExceptionTranslator {
        @Override
        protected DataAccessException
        customTranslate(String task, String sql, SQLException sqlException) {
            if (sqlException.getErrorCode() == 23505) {
            return new DuplicateKeyException(
                "Custom Exception translator - Integrity constraint violation.", sqlException);
            }
            return null;
        }
    }
    ```

    要使用此自定义异常翻译器，我们需要通过调用setExceptionTranslator（）方法将其传递到JdbcTemplate：

    ```java
    CustomSQLErrorCodeTranslator customSQLErrorCodeTranslator = 
    new CustomSQLErrorCodeTranslator();
    jdbcTemplate.setExceptionTranslator(customSQLErrorCodeTranslator);
    ```

5. 使用SimpleJdbc类的JDBC操作

    SimpleJdbc类提供了一种配置和运行SQL语句的简单方法。这些类使用数据库元数据来构建基本查询。因此，SimpleJdbcInsert和SimpleJdbcCall类提供了一种更简单的方法来运行插入和存储过程调用。

    1. 简单Jdbc插入

        让我们来看看以最少的配置运行简单的插入语句。

        INSERT语句是根据SimpleJdbcInsert的配置生成的。我们只需要提供表名、列名和值。

        首先，让我们创建一个SimpleJdbcInsert：

        ```java
        SimpleJdbcInsert simpleJdbcInsert =
        new SimpleJdbcInsert(dataSource).withTableName("EMPLOYEE");
        拷贝
        接下来，让我们提供列名和值，然后运行操作：

        public int addEmplyee(Employee emp) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("ID", emp.getId());
            parameters.put("FIRST_NAME", emp.getFirstName());
            parameters.put("LAST_NAME", emp.getLastName());
            parameters.put("ADDRESS", emp.getAddress());

            return simpleJdbcInsert.execute(parameters);
        }
        ```

        此外，我们可以使用executeAndReturnKey（）API来允许数据库生成主键。我们还需要配置实际的自动生成列：

        ```java
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                                                .withTableName("EMPLOYEE")
                                                .usingGeneratedKeyColumns("ID");

        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        System.out.println("Generated id - " + id.longValue());
        ```

        最后，我们还可以使用BeanPropertySqlParameterSource和MapSqlParameterSource传递此数据。

    2. 使用SimpleJdbcCall存储的过程

        让我们也来看看运行存储过程。

        我们将使用SimpleJdbcCall抽象：

        ```java
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(dataSource)
                            .withProcedureName("READ_EMPLOYEE");

        public Employee getEmployeeUsingSimpleJdbcCall(int id) {
            SqlParameterSource in = new MapSqlParameterSource().addValue("in_id", id);
            Map<String, Object> out = simpleJdbcCall.execute(in);

            Employee emp = new Employee();
            emp.setFirstName((String) out.get("FIRST_NAME"));
            emp.setLastName((String) out.get("LAST_NAME"));

            return emp;
        }
        ```

6. 批量操作

    另一个简单的用例是将多个操作批量处理在一起。

    1. 使用JdbcTemplate的基本批处理操作

        使用JdbcTemplate，可以通过batchUpdate（）API运行批处理操作。

        这里有趣的部分是简洁但非常有用的BatchPreparedStatementSetter实现：

        ```java
        public int[] batchUpdateUsingJdbcTemplate(List<Employee> employees) {
            return jdbcTemplate.batchUpdate("INSERT INTO EMPLOYEE VALUES (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, employees.get(i).getId());
                        ps.setString(2, employees.get(i).getFirstName());
                        ps.setString(3, employees.get(i).getLastName());
                        ps.setString(4, employees.get(i).getAddress();
                    }
                    @Override
                    public int getBatchSize() {
                        return 50;
                    }
                });
        }
        ```

    2. 使用NamedParameterJdbcTemplate的批处理操作

        我们还可以选择使用NamedParameterJdbcTemplate – batchUpdate（）API进行批处理操作。

        这个API比上一个更简单。因此，无需实现任何额外的接口来设置参数，因为它有一个内部准备的语句设置器来设置参数值。

        相反，参数值可以作为SqlParameterSource的数组传递给batchUpdate（）方法。

        ```java
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(employees.toArray());
        int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(
            "INSERT INTO EMPLOYEE VALUES (:id, :firstName, :lastName, :address)", batch);
        return updateCounts;
        ```

7. 带有SpringBoot的弹簧JDBC

    Spring Boot提供了一个启动器spring-boot-starter-jdbc，用于将JDBC与关系数据库一起使用。

    与每个Spring Boot启动器一样，这个启动器帮助我们快速启动和运行应用程序。

    1. Maven依赖性

        我们需要spring-boot-starter-jdbc依赖项作为主要依赖项。我们还需要一个将要使用的数据库的依赖项。就我们而言，这是MySQL：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        ```

    2. 配置

        Spring Boot自动为我们配置数据源。我们只需要在属性文件中提供属性：

        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/springjdbc
        spring.datasource.username=guest_user
        spring.datasource.password=guest_password
        ```

        就是这样。我们的应用程序只需执行这些配置即可启动并运行。我们现在可以将其用于其他数据库操作。

        我们在上一节中看到的标准Spring应用程序的显式配置现在包含在Spring Boot自动配置中。

8. 结论

    在本文中，我们研究了Spring框架中的JDBC抽象。我们用实际的例子涵盖了Spring JDBC提供的各种功能。

    我们还研究了如何使用Spring Boot JDBC启动器快速启动Spring JDBC。
