# [JPA @Basic 注释](https://www.baeldung.com/jpa-basic-annotation)

1. 一览表

    在这个快速教程中，我们将探索JPA @Basic注释。我们还将讨论@Basic和@Column JPA注释之间的区别。

2. 基本类型

    JPA支持各种Java数据类型作为实体的持久字段，通常被称为基本类型。

    基本类型直接映射到数据库中的一列。这些包括Java原语及其包装器类、String、java.math.BigInteger和java.math.BigDecimal、各种可用的日期和时间类、枚举以及实现java.io.Serializable的任何其他类型。

    Hibernate和任何其他ORM供应商一样，维护基本类型的注册表，并用它来解析列的specificorg.hibernate.type.Type。

3. @Basic注释

    我们可以使用@Basic注释来标记基本类型属性：

    ```java
    @Entity
    public class Course {

        @Basic
        @Id
        private int id;

        @Basic
        private String name;
        ...
    }
    ```

    换句话说，字段或属性上的@Basic注释表示它是一个基本类型，Hibernate应该使用标准映射来持久化。

    请注意，这是一个可选的注释。因此，我们可以将我们的课程实体重写为：

    ```java
    @Entity
    public class Course {

        @Id
        private int id;

        private String name;
        ...
    }
    ```

    当我们不为基本类型属性指定@Basic注释时，会隐式假设，并应用此注释的默认值。

4. 为什么要使用@Basic Annotation？

    @Basic注释有两个属性，可选和获取。让我们仔细看看每一个。

    可选属性是一个布尔参数，它定义了标记的字段或属性是否允许空值。它默认为真。因此，如果该字段不是原始类型，则默认情况下，默认情况下，底层列为空。

    获取属性接受枚举获取的成员，该成员指定标记的字段或属性是懒惰地加载还是急切地获取。它默认为FetchType.EAGER，但我们可以通过将其设置为FetchType.LAZY来允许延迟加载。

    只有当我们将大型可序列化对象映射为基本类型时，惰性加载才有意义，因为在这种情况下，字段访问成本可能很大。

    我们有一个详细的[教程](https://www.baeldung.com/hibernate-lazy-eager-loading)，涵盖了Hibernate中的Eager/Lazy加载，更深入地探讨了该主题。

    现在，假设不想允许我们的课程名称使用空值，也想懒惰地加载该属性。然后，我们将课程实体定义为：

    ```java
    @Entity
    public class Course {

        @Id
        private int id;
        
        @Basic(optional = false, fetch = FetchType.LAZY)
        private String name;
        ...
    }
    ```

    当愿意偏离可选和获取参数的默认值时，我们应该明确使用@Basic注释。我们可以根据我们的需求指定其中一个或两个属性。

5. JPA @Basic 对 @Column

    让我们来看看@Basic和@Column注释之间的区别：

    - @Basic注释的属性适用于JPA实体，而@Column的属性适用于数据库列
    - @Basic注释的可选属性定义了实体字段是否可以为空；另一方面，@Column注释的可空属性指定了相应的数据库列是否可以为空
    - 我们可以使用@Basic来表示字段应该懒惰地加载
    - @Column注释允许我们指定映射数据库列的名称

6. 结论

    在本文中，我们了解了何时以及如何使用JPA的@Basic注释。我们还讨论了它与@Column注释有何不同。
