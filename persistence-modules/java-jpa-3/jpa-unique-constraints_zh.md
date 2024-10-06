# [在JPA中定义独特的约束](https://www.baeldung.com/jpa-unique-constraints)

1. 介绍

    在本教程中，我们将讨论使用JPA和Hibernate定义唯一约束。

    首先，我们将探索独特的约束，以及它们与主键约束有何不同。

    然后，我们将看看JPA的重要注释，@Column(unique=true)和@UniqueConstraint。我们将实现它们来定义单列和多列的唯一约束。

    最后，我们将学习如何在引用的表列上定义唯一约束。

2. 独特的约束

    让我们从快速总结开始。唯一密钥是表的一组单列或多列，用于唯一识别数据库表中的记录。

    唯一和主键约束都为列或列集的唯一性提供了保证。

    1. 它与主键约束有何不同？

        唯一约束确保列或列组合中的数据对每行都是唯一的。例如，表的主键作为隐式唯一约束发挥作用。因此，主键约束自动具有唯一的约束。

        此外，我们每个表只能有一个主键约束。然而，每个表可能有多个独特的约束。
        简单地说，除了主键映射所带来的任何约束外，还适用唯一的约束。

        我们定义的唯一约束在创建表期间用于生成适当的数据库约束，也可以在运行时用于订购插入、更新或删除语句。

    2. 什么是单列和多列约束？

        唯一的约束可以是列约束或表约束。在表级别，我们可以在多列中定义独特的约束。

        JPA允许我们使用@Column(unique=true)和@UniqueConstraint在代码中定义唯一的约束。这些注释由模式生成过程解释，自动创建约束。

        在其他任何事情之前，我们应该强调列级约束适用于单个列，表级约束适用于整个表。

        我们将在下一节中更详细地讨论这个问题。

3. 建立一个实体

    JPA中的实体表示存储在数据库中的表。实体的每个实例代表表中的一行。

    让我们从创建一个域实体并将其映射到数据库表开始。在这个例子中，我们将创建一个人格：

    ```java
    @Entity
    @Table
    public class Person implements Serializable {
        @Id
        @GeneratedValue
        private Long id;  
        private String name;
        private String password;
        private String email;
        private Long personNumber;
        private Boolean isActive;
        private String securityNumber;
        private String departmentCode;
        @JoinColumn(name = "addressId", referencedColumnName = "id")
        private Address address;
    //getters and setters
    }
    ```

    地址字段是来自地址实体的引用字段：

    ```java
    @Entity
    @Table
    public class Address implements Serializable {
        @Id
        @GeneratedValue
        private Long id;
        private String streetAddress;
        //getters and setters
    }
    ```

    在本教程中，我们将使用此人实体来演示我们的示例。

4. 列约束

    当我们准备好模型时，我们可以实现我们的第一个独特约束。

    让我们考虑一下持有个人信息的个人实体。我们有一个id列的主键。此实体还持有PersonNumber，该编号不包含任何重复值。此外，我们无法定义主键，因为我们的表已经有它了。

    在这种情况下，我们可以使用列唯一约束来确保在aPersonNumber字段中没有输入重复值。JPA允许我们使用具有唯一属性的@Column注释来实现这一点。

    在接下来的章节中，我们将看看@Column注释，然后学习如何实现它。

    1. @Column(unique=true)

        注释类型[列](https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/Column.html)用于指定持久属性或字段的映射列。

        让我们来看看定义：

        ```java
        @Target(value={METHOD,FIELD})
        @Retention(value=RUNTIME)
        public @interface Column {
            boolean unique;
        //other elements
        }
        ```

        唯一属性指定列是否为唯一密钥。这是UniqueConstraint注释的快捷方式，当唯一键约束仅对应于一列时很有用。

        我们将在下一节中了解如何定义它。

    2. 定义列约束

        每当唯一约束仅基于一个字段时，我们可以在该列上使用@Column（unique=true）。

        让我们在personNumber字段上定义一个唯一的约束：

        ```java
        @Column(unique=true)
        private Long personNumber;
        ```

        当我们执行模式创建过程时，我们可以从日志中验证它：

        ```log
        [main] DEBUG org.hibernate.SQL -
            alter table Person add constraint UK_d44q5lfa9xx370jv2k7tsgsqt unique (personNumber)
        ```

        同样，如果我们想限制一个人使用唯一电子邮件注册，我们可以在电子邮件字段上添加一个唯一的约束：

        ```java
        @Column(unique=true)
        private String email;
        ```

        让我们执行模式创建过程并检查约束：

        ```log
        [main] DEBUG org.hibernate.SQL -
            alter table Person add constraint UK_585qcyc8qh7bg1fwgm1pj4fus unique (email)
        ```

        虽然当我们想在单列上放置唯一约束时，这很有用，但有时我们可能想在复合键上添加唯一约束，复合键是列的组合。为了定义一个复合唯一键，我们可以使用表约束。我们将在下一节中讨论这个问题。

5. 表约束

    复合唯一密钥是由列组合组成的唯一密钥。要定义一个复合唯一键，我们可以在表上添加约束，而不是在列上。JPA帮助我们使用@UniqueConstraint注释来实现这一点。

    1. @UniqueConstraint注释

        注释类型[UniqueConstraint](https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/UniqueConstraint.html)指定了唯一约束将包含在表生成的DDL（数据定义语言）中。

        让我们来看看定义：

        ```java
        @Target(value={})
        @Retention(value=RUNTIME)
        public @interface UniqueConstraint {
            String name() default "";
            String[] columnNames();
        }
        ```

        正如我们所看到的，类型String和String[]的名称和列名称分别是可以为UniqueConstraint注释指定的注释元素。

        我们将在下一节中更好地查看每个参数，通过示例。

    2. 定义独特的约束

        让我们考虑一下我们的个人实体。一个人不应该有任何重复的活跃状态记录。换句话说，由personNumber和isActive组成的键不会有任何重复值。在这里，我们需要添加跨越多列的独特约束。

        JPA通过@UniqueConstraint注释帮助我们实现这一目标。我们在theuniqueConstraints属性下的[@Table](https://javaee.github.io/javaee-spec/javadocs/javax/persistence/Table.html)注释中使用它。让我们记住指定列的名称：

        `@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "personNumber", "isActive" }) })`

        一旦模式生成，我们就可以验证它：

        ```log
        [main] DEBUG org.hibernate.SQL -
            alter table Person add constraint UK5e0bv5arhh7jjhsls27bmqp4a unique (personNumber, isActive)
        ```

        这里需要注意的一点是，如果我们不指定名称，它就是提供商生成的值。自JPA 2.0以来，我们可以为我们的独特约束提供一个名称：

        `@Table(uniqueConstraints = { @UniqueConstraint(name = "UniqueNumberAndStatus", columnNames = { "personNumber", "isActive" }) })`

        我们可以验证相同的：

        ```log
        [main] DEBUG org.hibernate.SQL -
            alter table Person add constraint UniqueNumberAndStatus unique (personNumber, isActive)
        ```

        在这里，我们在一组列上添加了独特的约束。我们还可以添加多个唯一约束，即对多列集的唯一约束。我们将在下一节中这样做。

    3. 单个实体的多个唯一约束

        一个表可以有多个独特的约束。在最后一节中，我们在复合键上定义了唯一的约束：personNumber和isActive状态。在本节中，我们将对安全编号和部门代码的组合添加约束。

        让我们收集我们独特的索引，并立即指定它们。我们通过用大括号重复@UniqueConstraint注释并用逗号隔开来做到这一点：

        ```java
        @Table(uniqueConstraints = {
        @UniqueConstraint(name = "UniqueNumberAndStatus", columnNames = {"personNumber", "isActive"}),
        @UniqueConstraint(name = "UniqueSecurityAndDepartment", columnNames = {"securityNumber", "departmentCode"})})
        ```

        现在让我们看看日志，并检查约束：

        ```log
        [main] DEBUG org.hibernate.SQL -
            alter table Person add constraint UniqueNumberAndStatus unique (personNumber, isActive)
        [main] DEBUG org.hibernate.SQL -
        alter table Person add constraint UniqueSecurityAndDepartment unique (securityNumber, departmentCode)
        ```

        到目前为止，我们在同一实体中定义了唯一的约束。然而，在某些情况下，我们可能引用了其他实体的字段，需要确保这些字段的唯一性。我们将在下一节中讨论这个问题。

6. 引用表列上的唯一约束

    当我们创建两个或多个相互关联的表时，它们通常由一个表中的一列关联，引用另一个表的主键。那列被称为“外键”。例如，Person和Address实体通过addressId字段连接。因此，addressId充当引用的表列。

    我们可以在引用的列上定义唯一的约束。我们将首先在单列上实现它，然后在多列上实现它。

    1. 单列约束

        在我们的人员实体中，我们有一个地址字段，该字段指向地址实体。一个人应该有一个独特的地址。

        因此，让我们在人员的地址字段上定义一个唯一的约束：

        ```java
        @Column(unique = true)
        private Address address;
        ```

        现在让我们快速检查这个约束：

        ```log
        [main] DEBUG org.hibernate.SQL -
        alter table Person add constraint UK_7xo3hsusabfaw1373oox9uqoe unique (address)
        ```

        我们还可以在引用的表列上定义多列约束，我们将在下一节中看到。

    2. 多列约束

        我们可以在列组合上指定唯一的约束。如前所述，我们可以使用表约束来做到这一点。

        让我们在personNumber和address上定义唯一的约束，并将其添加到uniqueConstraints数组中：

        ```java
        @Entity
        @Table(uniqueConstraints =
        { //other constraints
        @UniqueConstraint(name = "UniqueNumberAndAddress", columnNames = { "personNumber", "address" })})
        ```

        最后，让我们看看独特的约束：

        ```log
        [main] DEBUG org.hibernate.SQL -
            alter table Person add constraint UniqueNumberAndAddress unique (personNumber, address)
        ```

7. 结论

    唯一的约束阻止两个记录在列或一组列中具有相同的值。

    在本文中，我们学习了如何在JPA中定义唯一约束。首先，我们稍微回顾了独特的限制。然后，我们讨论了@Column（unique=true）和@UniqueConstraint注释，分别定义单列和多列的唯一约束。
