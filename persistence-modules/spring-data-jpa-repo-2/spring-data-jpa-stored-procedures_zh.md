# [从Spring Data JPA存储库调用存储过程](https://www.baeldung.com/spring-data-jpa-stored-procedures)

1. 一览表

    [存储过程](https://www.baeldung.com/jpa-stored-procedures)是存储在数据库中的一组预定义的SQL语句。在Java中，有几种方法可以访问存储过程。在本教程中，我们将学习如何从Spring Data JPA存储库调用存储过程。

2. 项目设置

    我们将使用Spring Boot Starter Data JPA模块作为数据访问层。我们还将使用MySQL作为我们的后端数据库。因此，我们需要项目pom.xml文件中的Spring Data JPA、Spring Data JDBC和MySQL Connector依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    ```

    一旦我们有了MySQL依赖定义，我们就可以在application.properties文件中配置数据库连接：

    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/baeldung
    spring.datasource.username=baeldung
    spring.datasource.password=baeldung
    ```

3. 实体类

    在Spring Data JPA中，实体表示存储在数据库中的表。因此，我们可以构建一个实体类来映射汽车数据库表：

    ```java
    @Entity
    public class Car {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column
        private long id;

        @Column
        private String model;

        @Column
        private Integer year;

    // standard getters and setters
    }
    ```

4. 存储过程创建

    存储过程可以有参数，这样我们就可以根据输入获得不同的结果。例如，我们可以创建一个存储过程，该过程需要整数类型的输入参数并返回汽车列表：

    ```sql
    CREATE PROCEDURE FIND_CARS_AFTER_YEAR(IN year_in INT)
    BEGIN
        SELECT * FROM car WHERE year >= year_in ORDER BY year;
    END
    ```

    存储过程还可以使用输出参数将数据返回给调用应用程序。例如，我们可以创建一个存储过程，该过程将字符串类型的输入参数存储在输出参数中：

    ```sql
    CREATE PROCEDURE GET_TOTAL_CARS_BY_MODEL(IN model_in VARCHAR(50), OUT count_out INT)
    BEGIN
        SELECT COUNT(*) into count_out from car WHERE model = model_in;
    END
    ```

5. 引用存储在存储库中的过程

    在Spring Data JPA中，存储库是我们提供数据库操作的地方。我们可以为汽车实体上的数据库操作构建一个存储库，并引用此存储库中的存储过程：

    ```java
    @Repository
    public interface CarRepository extends JpaRepository<Car, Integer> {
        // ...
    }
    ```

    接下来，让我们在我们的存储库中添加一些调用存储过程的方法。

    1. 直接映射存储的过程名称

        我们可以使用@Procedure注释定义存储过程方法，并直接映射存储过程名称。

        有四种等效的方法可以做到这一点。例如，我们可以直接使用存储过程名称作为方法名称：

        ```java
        @Procedure
        int GET_TOTAL_CARS_BY_MODEL(String model);
        ```

        如果我们想要定义不同的方法名称，我们可以将存储过程名称作为@Procedure注释的元素：

        ```java
        @Procedure("GET_TOTAL_CARS_BY_MODEL")
        int getTotalCarsByModel(String model);
        ```

        我们还可以使用procurealName属性来映射存储的过程名称：

        ```java
        @Procedure(procedureName = "GET_TOTAL_CARS_BY_MODEL")
        int getTotalCarsByModelProcedureName(String model);
        ```

        最后，我们可以使用值属性来映射存储过程名称：

        ```java
        @Procedure(value = "GET_TOTAL_CARS_BY_MODEL")
        int getTotalCarsByModelValue(String model);
        ```

    2. 引用实体中定义的存储过程

        我们还可以使用@NamedStoredProcedureQuery注释在实体类中定义存储过程：

        ```java
        @Entity
        @NamedStoredProcedureQuery(name = "Car.getTotalCardsbyModelEntity", 
        procedureName = "GET_TOTAL_CARS_BY_MODEL", parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "model_in", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "count_out", type = Integer.class)})
        public class Car {
            // class definition
        }
        ```

        然后我们可以在存储库中引用此定义：

        ```java
        @Procedure(name = "Car.getTotalCardsbyModelEntity")
        int getTotalCarsByModelEntiy(@Param("model_in") String model);
        ```

        我们使用名称属性来引用实体类中定义的存储过程。对于存储库方法，我们使用@Param来匹配存储过程的输入参数。我们还将存储过程的输出参数与存储库方法的返回值相匹配。

    3. 使用@Query注释引用存储过程

        我们还可以使用@Query注释直接调用存储过程：

        ```java
        @Query(value = "CALL FIND_CARS_AFTER_YEAR(:year_in);", nativeQuery = true)
        List<Car> findCarsAfterYear(@Param("year_in") Integer year_in);
        ```

        在此方法中，我们使用本机查询来调用存储过程。我们将查询存储在注释的值属性中。

        同样，我们使用@Param来匹配存储过程的输入参数。我们还将存储过程输出映射到实体汽车对象列表。

6. 摘要

    在本文中，我们探讨了如何通过JPA存储库访问存储过程。我们还讨论了在JPA存储库中引用存储过程的两种简单方法。
