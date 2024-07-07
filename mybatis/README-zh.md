# MyBatis

## MyBatis快速指南

1. 简介

    [MyBatis](http://www.mybatis.org/mybatis-3/index.html)是一个开源的持久性框架，它简化了Java应用程序中数据库访问的实现。它提供了对自定义SQL、存储过程和不同类型的映射关系的支持。

    简单地说，它是JDBC和Hibernate的替代品。

2. Maven的依赖性

    为了利用MyBatis，我们需要在我们的pom.xml中添加依赖项：

    ```xml
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.4.4</version>
    </dependency>
    ```

3. Java APIs

    1. SQLSessionFactory

        SQLSessionFactory是每个MyBatis应用程序的核心类。该类通过使用SQLSessionFactoryBuilder的builder()方法进行实例化，该方法加载了一个XML配置文件：

        ```java
        String resource = "mybatis-config.xml";
        InputStream inputStream Resources.getResourceAsStream(resource);
        SQLSessionFactory sqlSessionFactory
        = new SqlSessionFactoryBuilder().build(inputStream);
        ```

        Java配置文件包括设置，如数据源定义、事务管理器细节和定义实体间关系的映射器列表，这些都被用来构建SQLSessionFactory实例：

        ```java
        public static SqlSessionFactory buildqlSessionFactory() {
            DataSource dataSource 
            = new PooledDataSource(DRIVER, URL, USERNAME, PASSWORD);

            Environment environment 
            = new Environment("Development", new JdbcTransactionFactory(), dataSource);
                
            Configuration configuration = new Configuration(environment);
            configuration.addMapper(PersonMapper.class);
            // ...

            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            return builder.build(configuration);
        }
        ```

    2. SQLSession

        SQLSession包含执行数据库操作、获取映射器和管理事务的方法。它可以从SQLSessionFactory类中实例化。这个类的实例不是线程安全的。

        在执行完数据库操作后，该会话应被关闭。由于SqlSession实现了AutoCloseable接口，我们可以使用try-with-resources块：

        `try(SqlSession session = sqlSessionFactory.openSession()) { // do work }`

4. 映射器

    映射器是Java接口，将方法映射到相应的SQL语句。MyBatis为定义数据库操作提供注解：

    ```java
    public interface PersonMapper {

        @Insert("Insert into person(name) values (#{name})")
        public Integer save(Person person);

        // ...

        @Select(
        "Select personId, name from Person where personId=#{personId}")
        @Results(value = {
        @Result(property = "personId", column = "personId"),
        @Result(property="name", column = "name"),
        @Result(property = "addresses", javaType = List.class,
            column = "personId", many=@Many(select = "getAddresses"))
        })
        public Person getPersonById(Integer personId);

        // ...
    }
    ```

5. MyBatis注解

    让我们看看MyBatis提供的一些主要注释：

    @Insert, @Select, @Update, @Delete - 这些注解表示通过调用注解方法来执行的SQL语句：

    ```java
    @Insert("Insert into person(name) values (#{name})")
    public Integer save(Person person);

    @Update("Update Person set name= #{name} where personId=#{personId}")
    public void updatePerson(Person person);

    @Delete("Delete from Person where personId=#{personId}")
    public void deletePersonById(Integer personId);

    @Select("SELECT person.personId, person.name FROM person 
    WHERE person.personId = #{personId}")
    Person getPerson(Integer personId);
    ```

    @Results - 它是一个结果映射列表，包含了数据库列与Java类属性的映射细节：

    ```java
    @Select("Select personId, name from Person where personId=#{personId}")
    @Results(value = {
    @Result(property = "personId", column = "personId")
        // ...   
    })
    public Person getPersonById(Integer personId);
    ```

    @Result - 它代表了从@Results检索到的结果列表中的一个单独的结果实例。它包括一些细节，如从数据库列到Java Bean属性的映射，属性的Java类型以及与其他Java对象的关联：

    ```java
    @Results(value = {
    @Result(property = "personId", column = "personId"),
    @Result(property="name", column = "name"),
    @Result(property = "addresses", javaType =List.class) 
        // ... 
    })
    public Person getPersonById(Integer personId);
    ```

    @Many - 它指定了一个对象到其他对象集合的映射：

    ```java
    @Results(value ={
    @Result(property = "addresses", javaType = List.class, 
        column = "personId", many=@Many(select = "getAddresses"))
    })
    ```

    这里getAddresses是通过查询地址表返回地址集合的方法。

    ```java
    @Select("select addressId, streetAddress, personId from address 
    where personId=#{personId}")
    public Address getAddresses(Integer personId);
    ```

    与@Many注解类似，我们还有@One注解，它指定了对象之间的一对一映射关系。

    @MapKey - 这是用来将记录列表转换为记录的地图，其键值由属性定义：

    ```java
    @Select("select * from Person")
    @MapKey("personId")
    Map<Integer, Person> getAllPerson();
    ```

    @Options - 这个注解指定了一系列要定义的开关和配置，因此我们可以@Options来定义它们，而不是在其他语句上定义它们：

    ```java
    @Insert("Insert into address (streetAddress, personId) 
    values(#{streetAddress}, #{personId})")
    @Options(useGeneratedKeys = false, flushCache=true)
    public Integer saveAddress(Address address);
    ```

6. 动态SQL

    动态SQL是MyBatis提供的一个非常强大的功能。有了它，我们可以准确地构造我们复杂的SQL。

    在传统的JDBC代码中，我们必须编写SQL语句，将它们串联起来，并在它们之间准确地添加空格，将逗号放在正确的位置。这很容易出错，而且在大型SQL语句的情况下很难进行调试。

    让我们探讨一下如何在我们的应用程序中使用动态SQL：

    ```java
    @SelectProvider(type=MyBatisUtil.class, method="getPersonByName")
    public Person getPersonByName(String name);
    ```

    在这里，我们指定了一个类和一个方法名，它实际上构造并生成了最终的SQL：

    ```java
    public class MyBatisUtil {
        // ...
        public String getPersonByName(String name){
            return new SQL() {{
                SELECT("*");
                FROM("person");
                WHERE("name like #{name} || '%'");
            }}.toString();
        }
    }
    ```

    动态SQL将所有的SQL结构作为一个类提供，例如SELECT、WHERE等。有了这个，我们可以动态地改变WHERE子句的生成。

7. 存储过程支持

    我们也可以使用@Select注解来执行存储过程。在这里，我们需要传递存储过程的名称，参数列表，并使用显式调用该过程：

    ```java
    @Select(value= "{CALL getPersonByProc(#{personId,
    mode=IN, jdbcType=INTEGER})}")
    @Options(statementType = StatementType.CALLABLE)
    public Person getPersonByProc(Integer personId);
    ```

8. 总结

    在这个快速教程中，我们已经看到了MyBatis提供的不同功能，以及它是如何简化面向数据库的应用程序的开发的。我们也看到了该库所提供的各种注释。

## Relevant Articles

- [x] [Quick Guide to MyBatis](https://www.baeldung.com/mybatis)

## Code

这篇文章的完整代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/mybatis)上找到。
