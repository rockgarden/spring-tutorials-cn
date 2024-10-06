# [Jpa @Embedded和@Embeddable](https://www.baeldung.com/jpa-embedded-embeddable)

1. 一览表

    在本教程中，我们将了解如何将包含嵌入式属性的实体映射到单个数据库表。

    为此，我们将使用Java Persistence API（[JPA](https://www.baeldung.com/jpa-hibernate-difference)）提供的@Embeddable和@Embedde注释。

2. 数据模型上下文

    首先，让我们定义一个名为公司的表格。

    公司表将存储公司名称、地址和电话等基本信息，以及联系人的信息：

    ```java
    public class Company {

        private Integer id;

        private String name;

        private String address;

        private String phone;

        private String contactFirstName;

        private String contactLastName;

        private String contactPhone;

        // standard getters, setters
    }
    ```

    然而，似乎应该将联系人抽象到一个单独的班级。

    问题是，我们不想为这些细节创建一个单独的表格。

    所以，让我们看看我们能做什么。

3. @Embeddable

    JPA提供@Embeddable注释来声明一个类将由其他实体嵌入。

    让我们定义一个类来抽象联系人的详细信息：

    ```java
    @Embeddable
    public class ContactPerson {

        private String firstName;

        private String lastName;

        private String phone;

        // standard getters, setters
    }
    ```

4. @Embedded

    JPA注释@Embedded用于将类型嵌入到另一个实体中。

    接下来让我们修改一下我们的公司课程。

    我们将添加JPA注释，我们还将更改为使用ContactPerson而不是单独的字段：

    ```java
    @Entity
    public class Company {

        @Id
        @GeneratedValue
        private Integer id;

        private String name;

        private String address;

        private String phone;

        @Embedded
        private ContactPerson contactPerson;

        // standard getters, setters
    }
    ```

    因此，我们有我们的实体公司，嵌入联系人详细信息并映射到单个数据库表。

    不过，我们仍然还有一个问题，那就是JPA如何将这些字段映射到数据库列。

5. 属性覆盖

    我们的字段被称为contactFirstName，就像我们最初的公司类中的contactFirstName，现在我们的ContactPerson类中的firstName。因此，JPA将希望将这些分别映射到contact_first_name和first_name。

    除了不太理想之外，它实际上会用我们现在重复的电话栏来破坏我们。

    因此，我们可以使用@AttributeOverrides和@AttributeOverride来覆盖我们嵌入式类型的列属性。

    让我们将此添加到我们公司实体的联系人字段中：

    ```java
    @Embedded
    @AttributeOverrides({
    @AttributeOverride( name = "firstName", column = @Column(name = "contact_first_name")),
    @AttributeOverride( name = "lastName", column = @Column(name = "contact_last_name")),
    @AttributeOverride( name = "phone", column = @Column(name = "contact_phone"))
    })
    private ContactPerson contactPerson;
    ```

    请注意，由于这些注释在字段中，我们可以为每个包含的实体提供不同的覆盖。

6. 结论

    在本文中，我们配置了一个具有一些嵌入式属性的实体，并将其映射到与封闭实体相同的数据库表。

    为此，我们使用了Java Persistence API提供的@Embedded、@Embeddable、@AttributeOverrides和@AttributeOverride注释。
