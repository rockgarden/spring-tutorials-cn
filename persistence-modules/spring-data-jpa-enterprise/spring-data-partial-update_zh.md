# [使用Spring Data进行部分数据更新](https://www.baeldung.com/spring-data-partial-update)

1. 一览表

    Spring Data的CrudRespository#save无疑很简单，但有一个功能可能是一个缺点：它更新表中的每列。这就是CRUD中U的语义，但如果我们想做一个PATCH呢？

    在本教程中，我们将介绍执行部分更新而不是完整更新的技术和方法。

2. 问题

    如前所述，save（）将用提供的数据覆盖任何匹配的实体，这意味着我们无法提供部分数据。这可能会变得不方便，特别是对于具有许多字段的大型物体来说。

    我们可以以两种不同的方式直接针对我们需要更新的特定实体采取行动：

    - 我们可以依靠Hibernate的@[DynamicUpdate](https://www.baeldung.com/spring-data-jpa-dynamicupdate)注释，它动态重写更新查询
    - 我们可以在JPA的@Column注释上使用可更新参数，这将不允许对特定列进行更新

    这种方法会有一些缺点。我们直接在实体上指定更新逻辑，从而将业务逻辑和持久性层联系起来。更具体地说，注释实体上的每次更新都会以类似的方式进行。

    在本文中，我们将看看如何在不直接依赖数据库实体上的Hibernate或JPA注释的情况下以最佳方式解决这个问题。

3. 我们的案例

    首先，让我们建立一个客户实体：

    ```java
    @Entity
    public class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        public long id;
        public String name;
        public String phone;
    }
    ```

    然后我们定义一个简单的CRUD存储库：

    ```java
    @Repository
    public interface CustomerRepository extends CrudRepository<Customer, Long> {
        Customer findById(long id);
    }
    ```

    最后，我们准备了客户服务：

    ```java
    @Service
    public class CustomerService {
        @Autowired
        CustomerRepository repo;

        public void addCustomer(String name) {
            Customer c = new Customer();
            c.name = name;
            repo.save(c);
        } 
    }
    ```

4. 加载和保存方法

    让我们先看看一个可能很熟悉的方法：从数据库加载我们的实体，然后只更新我们需要的字段。这是我们能使用的最直接的方法之一。

    让我们在我们的服务中添加一种方法来更新客户的联系信息。

    ```java
    public void updateCustomer(long id, String phone) {
        Customer myCustomer = repo.findById(id);
        myCustomer.phone = phone;
        repo.save(myCustomer);
    }
    ```

    我们将调用findById方法并检索匹配的实体。然后我们继续并更新所需的字段，并保留数据。

    当要更新的字段数量相对较少时，这种基本技术是有效的，而我们的实体相当简单。

    数十个要更新的字段会发生什么？

    1. Mapping Strategy

        当我们的对象有许多具有[不同访问级别](https://www.baeldung.com/java-access-modifiers)的字段时，实现[DTO模式](https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application)是很常见的。

        现在假设我们的对象中有一百多个电话字段。编写一种将数据从DTO注入我们的实体的方法，就像我们以前所做的那样，可能会令人讨厌且无法维护。

        尽管如此，我们可以使用映射策略解决这个问题，特别是[MapStruct](https://www.baeldung.com/mapstruct)实现。

        让我们创建一个CustomerDto：

        ```java
        public class CustomerDto {
            private long id;
            public String name;
            public String phone;
            //...
            private String phone99;
        }
        ```

        我们还将创建一个CustomerMapper：

        ```java
        @Mapper(componentModel = "spring")
        public interface CustomerMapper {
            void updateCustomerFromDto(CustomerDto dto, @MappingTarget Customer entity);
        }
        ```

        @MappingTarget注释允许我们更新现有对象，为我们省去编写大量代码的麻烦。

        MapStruct有一个@BeanMapping方法装饰器，允许我们定义在映射过程中跳过空值的规则。

        让我们把它添加到我们的updateCustomerFromDto方法界面中：

        `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`

        有了这个，我们可以加载存储的实体，并在调用JPA保存方法之前将它们与DTO合并——事实上，我们将只更新修改后的值。

        因此，让我们在我们的服务中添加一种方法，它将调用我们的映射器：

        ```java
        public void updateCustomer(CustomerDto dto) {
            Customer myCustomer = repo.findById(dto.id);
            mapper.updateCustomerFromDto(dto, myCustomer);
            repo.save(myCustomer);
        }
        ```

        这种方法的缺点是，在更新期间，我们无法将空值传递给数据库。

    2. 更简单的实体

        最后，请记住，我们可以从应用程序的设计阶段开始解决这个问题。

        必须将我们的实体定义为尽可能小。

        让我们来看看我们的客户实体。

        我们将稍微构建它，并将所有电话字段提取到ContactPhone实体，并在一对多关系下：

        ```java
        @Entity public class CustomerStructured {
            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            public Long id;
            public String name;
            @OneToMany(fetch = FetchType.EAGER, targetEntity=ContactPhone.class, mappedBy="customerId")
            private List<ContactPhone> contactPhones;
        }
        ```

        代码是干净的，更重要的是，我们取得了一些成就。现在，我们可以更新我们的实体，而无需检索和填写所有电话数据。

        处理小型和有界实体允许我们只更新必要的字段。

        这种方法唯一的不便之处在于，我们应该在不陷入过度设计的陷阱的情况下，有意识地设计我们的实体。

5. 自定义查询

    我们可以实施的另一种方法是为部分更新定义自定义查询。

    事实上，JPA定义了两个注释，@Modifying和@Query，允许我们明确地编写更新语句。

    我们现在可以告诉我们的应用程序在更新期间如何表现，而不会给ORM留下负担。

    让我们将我们的自定义更新方法添加到存储库中：

    ```java
    @Modifying
    @Query("update Customer u set u.phone = :phone where u.id = :id")
    void updatePhone(@Param(value = "id") long id, @Param(value = "phone") String phone);
    ```

    现在我们可以重写我们的更新方法：

    ```java
    public void updateCustomerWithCustomQuery(long id, String phone) {
        repo.updatePhone(id, phone);
    }
    ```

    我们现在能够进行部分更新。我们只用几行代码就实现了我们的目标，而且没有改变我们的实体。

    这种技术的缺点是，我们必须为对象的每次可能的部分更新定义一种方法。

6. 结论

    部分数据更新是一项相当基本的操作；虽然我们可以让我们的ORM处理它，但有时完全控制它是有利可图的。

    正如我们所看到的，我们可以预加载数据，然后更新它或定义我们的自定义语句，但请记住注意这些方法所暗示的缺点以及如何克服它们。
