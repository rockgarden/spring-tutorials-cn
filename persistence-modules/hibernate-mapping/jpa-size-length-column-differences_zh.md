# [@Size、@Length和@Column（length=value）之间的差异](https://www.baeldung.com/jpa-size-length-column-differences)

1. 一览表

    在这个快速教程中，我们将看看[JSR-330](https://www.baeldung.com/javax-validation)的@Size注释、[Hibernate](https://www.baeldung.com/hibernate-4-spring)的@Length注释和JPA的@Column的长度属性。

    乍一看，这些可能看起来是一样的，但它们的功能不同。

2. 起源

    简单地说，所有这些注释都是为了传达字段的大小。

    @Size和@Length是相似的。我们可以使用任一注释来验证字段的大小。前者是[Java标准注释](https://docs.oracle.com/javaee/7/tutorial/bean-validation001.htm)，而后者是[特定于Hibernate](http://docs.jboss.org/ejb3/app-server/HibernateAnnotations/api/org/hibernate/validator/Length.html)的。

    不过，@Column是我们用来控制DDL语句的[JPA注释](https://docs.oracle.com/javaee/7/api/javax/persistence/Column.html)。

    现在让我们详细地了解一下他们每个人。

3. @Size

    对于验证，我们将使用@Size，一个bean验证注释。我们将使用用@Size注释的属性middleName来验证其在属性min和max之间的值：

    ```java
    public class User {

        // ...

        @Size(min = 3, max = 15)
        private String middleName;

        // ...

    }
    ```

    最重要的是，@Size使bean独立于JPA及其供应商，如Hibernate。因此，它比@Length更便携。

4. @Length

    正如我们之前提到的，@Length是@Size的Hibernate特定版本。我们将使用@Length强制执行lastName的范围：

    ```java
    @Entity
    public class User {

        // ...
        
        @Length(min = 3, max = 15)
        private String lastName;

        // ...

    }
    ```

5. @Column(length=value)

    @Column与前两个注释有很大不同。

    我们将使用@Column来指示物理数据库列的具体特征。我们将使用@Column注释的长度属性来指定字符串值列长度：

    ```java
    @Entity
    public class User {

        @Column(length = 3)
        private String firstName;

        // ...

    }
    ```

    生成的列将生成为VARCHAR(3)，尝试插入更长的字符串将导致SQL错误。

    请注意，我们将仅使用@Column来指定表列属性，因为它不提供验证。

    当然，我们可以将@Column与@Size一起使用，通过bean验证来指定数据库列属性：

    ```java
    @Entity
    public class User {

        // ... 
        
        @Column(length = 5)
        @Size(min = 3, max = 5)
        private String city;

        // ...

    }
    ```

6. 结论

    在本文中，我们了解了@Size注释、@Length注释和@Column'slength属性之间的区别。然后我们分别检查了它们的使用领域。
