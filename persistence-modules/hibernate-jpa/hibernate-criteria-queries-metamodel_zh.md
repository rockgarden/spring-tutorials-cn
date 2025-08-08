# [使用 JPA 元模型进行 Criteria 查询](https://www.baeldung.com/hibernate-criteria-queries-metamodel)

持久化

Hibernate

1. 概述
    在本教程中，我们将讨论在 Hibernate 中编写 Criteria 查询时如何使用 JPA 静态元模型类。
    我们需要对 Hibernate 中的 Criteria 查询 API 有基本的了解，如果需要，可以查看我们关于 Criteria 查询的[教程](https://www.baeldung.com/hibernate-criteria-queries)以获取更多相关信息。

2. 为什么使用 JPA 元模型？
    通常，当我们编写一个 Criteria 查询时，需要引用实体类及其属性。
    现在，实现这一点的一种方法是将属性名称作为字符串提供。但这有几个缺点。
    首先，我们必须查找实体属性的名称。而且，如果在项目生命周期的后期更改了列名，我们必须重构每个使用该名称的查询。
    [JPA 元模型](https://docs.jboss.org/hibernate/orm/5.0/topical/html/metamodelgen/MetamodelGenerator.html#_canonical_metamodel)由社区引入，以避免这些缺点，并为托管实体类的元数据提供静态访问。

3. 实体类
    让我们考虑一个场景，我们正在为客户构建一个学生门户网站管理系统，并且出现了一个需求，即根据学生的毕业年份提供搜索功能。
    首先，让我们看看我们的 Student 类：

    ```java
    @Entity
    @Table(name = "students")
    public class Student {
        
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private int id;

        @Column(name = "first_name")
        private String firstName;

        @Column(name = "last_name")
        private String lastName;

        @Column(name = "grad_year")
        private int gradYear;

        // 标准的 getter 和 setter 方法
    }
    ```

4. 生成 JPA 元模型类
    接下来，我们需要生成元模型类，为此，我们将使用 [JBoss](https://docs.jboss.org/hibernate/orm/5.0/topical/html/metamodelgen/MetamodelGenerator.html) 提供的元模型生成器工具。JBoss 只是可用于生成元模型的众多工具之一。其他合适的工具包括 [EclipseLink](http://wiki.eclipse.org/UserGuide/JPA/Using_the_Canonical_Model_Generator_(ELUG))、[OpenJPA](http://openjpa.apache.org/builds/2.4.1/apache-openjpa/docs/ch13s04.html) 和 [DataNucleus](https://www.datanucleus.org/products/accessplatform_5_2/jpa/query.html#metamodel)。

    要使用 JBoss 工具，我们需要在 pom.xml 文件中添加最新的依赖项，一旦我们触发 Maven 构建命令，该工具就会生成元模型类：

    ```xml
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-jpamodelgen</artifactId>
        <version>6.1.7.Final</version>
    </dependency>
    ```

    注意，我们需要将 target/generated-classes 文件夹添加到 IDE 的类路径中，因为默认情况下，类将仅在此文件夹中生成。

5. 静态 JPA 元模型类
    根据 JPA 规范，生成的类将位于与相应实体类相同的包中，并且名称相同，但末尾添加了“_”（下划线）。因此，为 Student 类生成的元模型类将是 Student_，看起来像这样：

    ```java
    @Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
    @StaticMetamodel(Student.class)
    public abstract class Student_ {

        public static volatile SingularAttribute<Student, String> firstName;
        public static volatile SingularAttribute<Student, String> lastName;
        public static volatile SingularAttribute<Student, Integer> id;
        public static volatile SingularAttribute<Student, Integer> gradYear;

        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String ID = "id";
        public static final String GRAD_YEAR = "gradYear";
    }
    ```

6. 使用 JPA 元模型类
    我们可以像使用属性的字符串引用一样使用静态元模型类。Criteria 查询 API 提供了重载方法，可以接受字符串引用和 Attribute 接口实现。
    让我们看看将获取所有在 2015 年毕业的学生的 Criteria 查询：

    ```java
    // session 设置代码
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Student> criteriaQuery = cb.createQuery(Student.class);

    Root<Student> root = criteriaQuery.from(Student.class);
    criteriaQuery.select(root).where(cb.equal(root.get(Student_.gradYear), 2015));

    Query<Student> query = session.createQuery(criteriaQuery);
    List<Student> results = query.getResultList();
    ```

    请注意，我们是如何使用 Student_.gradYear 引用而不是使用传统的 grad_year 列名的。

7. 结论
    在这篇简短的文章中，我们学习了如何使用静态元模型类，以及为什么它们可能比之前描述的传统使用字符串引用的方式更受青睐。
