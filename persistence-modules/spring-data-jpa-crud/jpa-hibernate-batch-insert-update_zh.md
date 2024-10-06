# [使用Hibernate/JPA批量插入/更新](https://www.baeldung.com/jpa-hibernate-batch-insert-update)

1. 一览表

    在本教程中，我们将学习如何使用[Hibernate/JPA](https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa)批量插入和更新实体。

    批处理允许我们在一次网络调用中向数据库发送一组SQL语句。通过这种方式，我们可以优化应用程序的网络和内存使用情况。

2. 设置

    1. 样本数据模型

        让我们来看看我们将在示例中使用的示例数据模型。

        首先，我们将创建一个学校实体：

        ```java
        @Entity
        public class School {

            @Id
            @GeneratedValue(strategy = GenerationType.SEQUENCE)
            private long id;

            private String name;

            @OneToMany(mappedBy = "school")
            private List<Student> students;

            // Getters and setters...
        }
        ```

        每所学校将有零或更多学生：

        ```java
        @Entity
        public class Student {

            @Id
            @GeneratedValue(strategy = GenerationType.SEQUENCE)
            private long id;

            private String name;

            @ManyToOne
            private School school;

            // Getters and setters...
        }
        ```

    2. 追踪SQL查询

        在运行示例时，我们需要验证插入/更新语句是否确实是批量发送的。不幸的是，我们无法从Hibernate日志语句中判断SQL语句是否被批处理。因此，我们将使用数据源代理来跟踪Hibernate/JPA SQL语句：

        ```java
        private static class ProxyDataSourceInterceptor implements MethodInterceptor {
            private final DataSource dataSource;
            public ProxyDataSourceInterceptor(final DataSource dataSource) {
                this.dataSource = ProxyDataSourceBuilder.create(dataSource)
                    .name("Batch-Insert-Logger")
                    .asJson().countQuery().logQueryToSysOut().build();
            }
            
            // Other methods...
        }
        ```

3. 默认行为

    默认情况下，Hibernate不启用批处理。这意味着它将为每个插入/更新操作发送一个单独的SQL语句：

    ```java
    @Transactional
    @Test
    public void whenNotConfigured_ThenSendsInsertsSeparately() {
        for (int i = 0; i < 10; i++) {
            School school = createSchool(i);
            entityManager.persist(school);
        }
        entityManager.flush();
    }
    ```

    在这里，我们坚持了10个学校实体。如果我们查看查询日志，我们可以看到Hibernate单独发送每个插入语句：

    ```log
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School1","1"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School2","2"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School3","3"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School4","4"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School5","5"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School6","6"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School7","7"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School8","8"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School9","9"]]
    "querySize":1, "batchSize":0, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School10","10"]]
    ```

    因此，我们应该配置Hibernate以启用批处理。为此，我们应该将hibernate.jdbc.batch_size属性设置为大于0的数字。

    如果我们手动创建实体管理器，我们应该将hibernate.jdbc.batch_size添加到Hibernate属性中：

    ```java
    public Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.jdbc.batch_size", "5");
        
        // Other properties...
        return properties;
    }
    ```

    如果我们使用Spring Boot，我们可以将其定义为应用程序属性：

    `spring.jpa.properties.hibernate.jdbc.batch_size=5`

4. 单表的批量插入

    1. 批量插入，没有明确的冲洗

        让我们先看看当我们只处理一种实体类型时，如何使用批处理插入。

        我们将使用之前的代码示例，但这次启用了批处理：

        ```java
        @Transactional
        @Test
        public void whenInsertingSingleTypeOfEntity_thenCreatesSingleBatch() {
            for (int i = 0; i < 10; i++) {
                School school = createSchool(i);
                entityManager.persist(school);
            }
        }
        ```

        在这里，我们坚持了10个学校实体。当我们查看日志时，我们可以验证Hibernate是否批量发送插入语句：

        ```log
        "batch":true, "querySize":1, "batchSize":5, "query":["insert into school (name, id) values (?, ?)"], 
        "params":[["School1","1"],["School2","2"],["School3","3"],["School4","4"],["School5","5"]]
        "batch":true, "querySize":1, "batchSize":5, "query":["insert into school (name, id) values (?, ?)"], 
        "params":[["School6","6"],["School7","7"],["School8","8"],["School9","9"],["School10","10"]]
        ```

        这里要提到的一件重要的事情是内存消耗。当我们持久化实体时，Hibernate会将其存储在持久性上下文中。例如，如果我们在一次事务中持续100,000个实体，我们最终在内存中将有100,000个实体实例，这可能会导致OutOfMemoryException。

    2. 带显式冲洗的批量插入(Batch Insert With Explicit Flush)

        现在我们来看看如何优化批处理操作过程中的内存使用。让我们深入了解持久化上下文的作用。

        首先，持久化上下文将新创建和修改的实体存储在内存中。当事务同步时，Hibernate 会将这些更改发送到数据库。这通常发生在事务结束时。不过，调用 EntityManager.flush() 也会触发事务同步。

        其次，持久化上下文充当实体缓存，也称为一级缓存。要清除持久化上下文中的实体，我们可以调用 EntityManager.clear()。

        因此，为了减少批处理过程中的内存负载，我们可以在批处理量达到一定大小时，在应用代码中调用 EntityManager.flush() 和 EntityManager.clear()：

        ```java
        @Transactional
        @Test
        public void whenFlushingAfterBatch_ThenClearsMemory() {
            for (int i = 0; i < 10; i++) {
                if (i > 0 && i % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
                School school = createSchool(i);
                entityManager.persist(school);
            }
        }
        ```

        在这里，我们正在刷新持久性上下文中的实体，从而使Hibernate向数据库发送查询。此外，通过清除持久性上下文，我们正在从内存中删除学校实体。批处理行为将保持不变。

5. 多个表的批量插入

    现在让我们看看在一次事务中处理多个实体类型时，我们如何配置批处理插入。

    当我们想要持久化几种类型的实体时，Hibernate会为每个实体类型创建不同的批处理。这是因为单个批次中只能存在一种类型的实体。

    此外，当Hibernate收集插入语句时，每当遇到与当前批处理中的实体类型不同的实体类型时，它都会创建一个新批处理。即使该实体类型已经有一个批次，情况也是如此：

    ```java
    @Transactional
    @Test
    public void whenThereAreMultipleEntities_ThenCreatesNewBatch() {
        for (int i = 0; i < 10; i++) {
            if (i > 0 && i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            School school = createSchool(i);
            entityManager.persist(school);
            Student firstStudent = createStudent(school);
            Student secondStudent = createStudent(school);
            entityManager.persist(firstStudent);
            entityManager.persist(secondStudent);
        }
    }
    ```

    在这里，我们插入一所学校，为其分配两名学生，并重复这个过程10次。

    在日志中，我们看到Hibernate发送了几批1号的学校插入语句，而我们只期待2批5号。此外，学生插入语句也分几批2号发送，而不是4批5号：

    ```log
    "batch":true, "querySize":1, "batchSize":1, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School1","1"]]
    "batch":true, "querySize":1, "batchSize":2, "query":["insert into student (name, school_id, id) 
    values (?, ?, ?)"], "params":[["Student-School1","1","2"],["Student-School1","1","3"]]
    "batch":true, "querySize":1, "batchSize":1, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School2","4"]]
    "batch":true, "querySize":1, "batchSize":2, "query":["insert into student (name, school_id, id) 
    values (?, ?, ?)"], "params":[["Student-School2","4","5"],["Student-School2","4","6"]]
    "batch":true, "querySize":1, "batchSize":1, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School3","7"]]
    "batch":true, "querySize":1, "batchSize":2, "query":["insert into student (name, school_id, id) 
    values (?, ?, ?)"], "params":[["Student-School3","7","8"],["Student-School3","7","9"]]
    Other log lines...
    ```

    要批处理同一实体类型的所有插入语句，我们应该配置hibernate.order_inserts属性。

    我们可以使用EntityManagerFactory手动配置Hibernate属性：

    ```java
    public Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.order_inserts", "true");
        
        // Other properties...
        return properties;
    }
    ```

    如果我们使用Spring Boot，我们可以在application.properties中配置属性：

    `spring.jpa.properties.hibernate.order_inserts=true`

    添加此属性后，我们将有1批用于学校插入物，2批用于学生插入物：

    ```log
    "batch":true, "querySize":1, "batchSize":5, "query":["insert into school (name, id) values (?, ?)"], 
    "params":[["School6","16"],["School7","19"],["School8","22"],["School9","25"],["School10","28"]]
    "batch":true, "querySize":1, "batchSize":5, "query":["insert into student (name, school_id, id) 
    values (?, ?, ?)"], "params":[["Student-School6","16","17"],["Student-School6","16","18"],
    ["Student-School7","19","20"],["Student-School7","19","21"],["Student-School8","22","23"]]
    "batch":true, "querySize":1, "batchSize":5, "query":["insert into student (name, school_id, id) 
    values (?, ?, ?)"], "params":[["Student-School8","22","24"],["Student-School9","25","26"],
    ["Student-School9","25","27"],["Student-School10","28","29"],["Student-School10","28","30"]]
    ```

6. 批量更新

    现在让我们继续进行批量更新。与批量插入类似，我们可以对几个更新语句进行分组，并一次性将它们发送到数据库。

    要启用此功能，我们将配置hibernate.order_updates和hibernate.batch_versioned_data属性。

    如果我们手动创建实体管理器工厂，我们可以以编程方式设置属性：

    ```java
    public Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.batch_versioned_data", "true");
        
        // Other properties...
        return properties;
    }
    ```

    如果我们使用Spring Boot，我们将将它们添加到application.properties中：

    ```properties
    spring.jpa.properties.hibernate.order_updates=true
    spring.jpa.properties.hibernate.batch_versioned_data=true
    ```

    配置这些属性后，Hibernate应该分批对更新语句进行分组：

    ```java
    @Transactional
    @Test
    public void whenUpdatingEntities_thenCreatesBatch() {
        TypedQuery<School> schoolQuery = 
        entityManager.createQuery("SELECT s from School s", School.class);
        List<School> allSchools = schoolQuery.getResultList();
        for (School school : allSchools) {
            school.setName("Updated_" + school.getName());
        }
    }
    ```

    在这里，我们更新了学校实体，Hibernate以2批大小5的SQL语句发送。

    ```log
    "batch":true, "querySize":1, "batchSize":5, "query":["update school set name=? where id=?"], 
    "params":[["Updated_School1","1"],["Updated_School2","2"],["Updated_School3","3"],
    ["Updated_School4","4"],["Updated_School5","5"]]
    "batch":true, "querySize":1, "batchSize":5, "query":["update school set name=? where id=?"], 
    "params":[["Updated_School6","6"],["Updated_School7","7"],["Updated_School8","8"],
    ["Updated_School9","9"],["Updated_School10","10"]]
    ```

7. @Id 生成策略

    当我们想使用批量插入时，我们应该注意主键生成策略。如果我们的实体使用 GenerationType.IDENTITY 标识符生成器，[Hibernate 会默默地禁止批量插入](http://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#batch-session-batch)。

    由于示例中的实体使用的是 GenerationType.SEQUENCE 标识符生成器，因此 Hibernate 会启用批处理操作：

    ```java
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    ```

8. 摘要

    在本文中，我们研究了使用Hibernate/JPA的批量插入和更新。
