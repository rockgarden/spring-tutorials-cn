# [@JoinColumn注释解释](https://www.baeldung.com/jpa-join-column)

1. 一览表

    注释jakarta.persistence.JoinColumn将列标记为实体关联或元素集合的连接列。

    在本快速教程中，我们将展示一些基本@JoinColumn用法的示例。

2. @OneToOne映射示例

    @JoinColumn注释与@OneToOne映射相结合，表示所有者实体中的给定列引用了引用实体中的主键：

    ```java
    @Entity
    public class Office {
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "addressId")
        private Address address;
    }
    ```

    上述代码示例将创建一个外来键，将Office实体与地址实体的主键链接。Office实体中外键列的名称由名称属性指定。

3. @OneToMany 映射示例

    使用@OneToMany映射时，我们可以使用mappedBy参数来指示给定列由另一个实体拥有：

    ```java
    @Entity
    public class Employee {

        @Id
        private Long id;

        @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee")
        private List<Email> emails;
    }

    @Entity
    public class Email {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id")
        private Employee employee;
    }
    ```

    在上述示例中，电子邮件（所有者实体）有一个连接列employee_id，该列存储id值，并具有员工实体的外键。

4. @JoinColumns

    当我们想要创建多个连接列时，我们可以使用@JoinColumns注释：

    ```java
    @Entity
    public class Office {
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
            @JoinColumn(name="ADDR_ID", referencedColumnName="ID"),
            @JoinColumn(name="ADDR_ZIP", referencedColumnName="ZIP")
        })
        private Address address;
    }
    ```

    上述示例将创建两个外键，指向地址实体中的ID和ZIP列。

5. 结论

    在本文中，我们学习了如何使用@JoinColumn注释。我们研究了如何创建单个实体关联和元素集合。
