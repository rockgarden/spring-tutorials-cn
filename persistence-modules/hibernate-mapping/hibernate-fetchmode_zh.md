# [hibernate中的FetchMode](https://www.baeldung.com/hibernate-fetchmode)

1. 介绍

    在这个简短的教程中，我们将看看我们可以在@org.hibernate.annotations.Fetch注释中使用的不同的FetchMode值。

2. 设置示例

    例如，我们将使用以下客户实体，只有两个属性——一个id和一组订单：

    ```java
    @Entity
    public class Customer {

        @Id
        @GeneratedValue
        private Long id;

        @OneToMany(mappedBy = "customer")
        @Fetch(value = FetchMode.SELECT)
        private Set<Order> orders = new HashSet<>();

        // getters and setters
    }
    ```

    此外，我们将创建一个由ID、名称和客户引用组成的订单实体。

    ```java
    @Entity
    public class Order {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @ManyToOne
        @JoinColumn(name = "customer_id")
        private Customer customer;

        // getters and setters
    }
    ```

    在接下来的每个部分中，我们将从数据库中获取客户，并获取其所有订单：

    ```java
    Customer customer = customerRepository.findById(id).get();
    Set<Order> orders = customer.getOrders();
    ```

3. FetchMode.SELECT

    在我们的客户实体上，我们用@Fetch注释注释了订单属性：

    ```java
    @OneToMany
    @Fetch(FetchMode.SELECT)
    private Set<Orders> orders;
    ```

    我们使用@Fetch来描述当我们查找客户时，Hibernate应该如何检索属性。

    使用SELECT表示该属性应该懒惰地加载。

    这意味着第一行：

    `Customer customer = customerRepository.findById(id).get();`

    我们不会看到与订单表的连接：

    ```log
    Hibernate:
        select ...from customer
        where customer0_.id=?
    ```

    下一行：

    `Set<Order> orders = customer.getOrders();`

    我们将看到相关订单的后续查询：

    ```log
    Hibernate:
        select ...from order
        where order0_.customer_id=?
    ```

    Hibernate FetchMode.SELECT为每个需要加载的订单生成一个单独的查询。

    在我们的示例中，这提供了一个加载客户的查询和五个额外的查询来加载订单集合。

    这被称为n + 1选择问题。执行一个查询将触发n个额外的查询。

    1. @BatchSize

        FetchMode.SELECT有一个使用@BatchSize注释的可选配置注释：

        ```java
        @OneToMany
        @Fetch(FetchMode.SELECT)
        @BatchSize(size=10)
        private Set<Orders> orders;
        ```

        Hibernate将尝试以大小参数定义的批次加载订单集合。

        在我们的示例中，我们只有五个订单，所以一个查询就足够了。

        我们仍将使用相同的查询：

        ```log
        Hibernate:
            select ...from order
            where order0_.customer_id=?
        ```

        但它只会运行一次。现在我们只有两个查询：一个是加载客户，一个是加载订单集合。

4. FetchMode.JOIN

    当FetchMode.SELECT懒惰地加载关系时，FetchMode.JOIN急切地加载它们，例如通过连接：

    ```java
    @OneToMany
    @Fetch(FetchMode.JOIN)
    private Set<Orders> orders;
    ```

    这导致客户及其订单只需要一个查询：

    ```sql
    Hibernate:
        select ...
        from
            customer customer0_
        left outer join
            order order1
                on customer.id=order.customer_id
        where
            customer.id=?
    ```

5. FetchMode.SUBSELECT

    由于订单属性是一个集合，我们也可以使用FetchMode.SUBSELECT：

    ```java
    @OneToMany
    @Fetch(FetchMode.SUBSELECT)
    private Set<Orders> orders;
    ```

    我们只能将SUBSELECT与集合一起使用。

    通过此设置，我们回到对客户的一个查询：

    ```log
    Hibernate:
        select ...
        from customer customer0_
    ```

    订单的一个查询，这次使用子选择：

    ```log
    Hibernate:
        select ...
        from
            order order0_
        where
            order0_.customer_id in (
                select
                    customer0_.id
                from
                    customer customer0_
            )
    ```

6. FetchMode vs. FetchType

    一般来说，FetchMode定义了Hibernate如何获取数据（通过选择、加入或子选择）。另一方面，FetchType定义了Hibernate是急切还是懒惰地加载数据。

    两者之间的确切规则如下：

    - 如果代码没有设置FetchMode，默认为JOIN，FetchType按定义工作
    - 使用FetchMode.SELECT或FetchMode.SUBSELECT集，FetchType也按定义工作
    - 设置了FetchMode.JOIN，FetchType会被忽略，查询总是急切的

    有关更多信息，请参阅Hibernate中的[Eager/Lazy Loading](https://www.baeldung.com/hibernate-lazy-eager-loading)。

7. 结论

    在本教程中，我们了解了FetchMode的不同值，以及它们与FetchType的关系。
