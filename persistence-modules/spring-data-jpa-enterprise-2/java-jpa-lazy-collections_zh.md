# [在JPA中使用Lazy Element Collections](https://www.baeldung.com/java-jpa-lazy-collections)

1. 一览表

    JPA规范提供了两种不同的获取策略：渴望和懒惰。虽然懒惰方法有助于避免不必要地加载我们不需要的数据，但我们有时需要读取最初没有在封闭的永存在上下文中加载的数据。此外，在封闭的[持久上下文](https://www.baeldung.com/jpa-hibernate-persistence-context)中访问懒惰元素集合是一个常见的问题。

    在本教程中，我们将重点介绍如何从懒惰元素集合中加载数据。我们将探索三种不同的解决方案：一种涉及JPA查询语言，另一种涉及实体图，最后一个涉及事务传播。

2. 元素收集问题

    默认情况下，JPA在@ElementCollection类型的关联中使用懒惰获取策略。因此，在封闭的持久上下文中对集合的任何访问都将导致异常。

    为了理解这个问题，让我们根据员工与其电话列表之间的关系定义一个域模型：

    ```java
    @Entity
    public class Employee {
        @Id
        private int id;
        private String name;
        @ElementCollection
        @CollectionTable(name = "employee_phone", joinColumns = @JoinColumn(name = "employee_id"))
        private List phones;

        // standard constructors, getters, and setters
    }

    @Embeddable
    public class Phone {
        private String type;
        private String areaCode;
        private String number;

        // standard constructors, getters, and setters
    }
    ```

    我们的模型规定，一名员工可以拥有多部手机。电话列表是可嵌入类型的集合。让我们用这个模型使用Spring Repository：

    ```java
    @Repository
    public class EmployeeRepository {

        public Employee findById(int id) {
            return em.find(Employee.class, id);
        }

        // additional properties and auxiliary methods
    }
    ```

    现在，让我们用一个简单的JUnit测试用例重现这个问题：

    ```java
    public class ElementCollectionIntegrationTest {

        @Before
        public void init() {
            Employee employee = new Employee(1, "Fred");
            employee.setPhones(
            Arrays.asList(new Phone("work", "+55", "99999-9999"), new Phone("home", "+55", "98888-8888")));
            employeeRepository.save(employee);
        }

        @After
        public void clean() {
            employeeRepository.remove(1);
        }

        @Test(expected = org.hibernate.LazyInitializationException.class)
        public void whenAccessLazyCollection_thenThrowLazyInitializationException() {
            Employee employee = employeeRepository.findById(1);
    
            assertThat(employee.getPhones().size(), is(2));
        }
    }
    ```

    当我们尝试访问电话列表时，此测试会抛出一个异常，因为持久上下文已关闭。

    我们可以通过改变@ElementCollection的获取策略来解决这个问题，以使用渴望的方法。然而，急切地获取数据不一定是最好的解决方案，因为无论我们是否需要，手机数据总是会加载。

3. 使用JPA查询语言加载数据

    JPA查询语言允许我们自定义预测信息。因此，我们可以在员工存储库中定义一种新的方法来选择员工及其手机：

    ```java
    public Employee findByJPQL(int id) {
        return em.createQuery("SELECT u FROM Employee AS u JOIN FETCH u.phones WHERE u.id=:id", Employee.class)
            .setParameter("id", id).getSingleResult();
    }
    ```

    上述查询使用内部连接操作来获取返回的每个员工的电话列表。

4. 使用实体图加载数据

    另一个可能的解决方案是使用JPA的[实体图特征](https://www.baeldung.com/jpa-entity-graph)。实体图使我们能够选择JPA查询将投射哪些字段。让我们在我们的存储库中再定义一个方法：

    ```java
    public Employee findByEntityGraph(int id) {
        EntityGraph entityGraph = em.createEntityGraph(Employee.class);
        entityGraph.addAttributeNodes("name", "phones");
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.fetchgraph", entityGraph);
        return em.find(Employee.class, id, properties);
    }
    ```

    我们可以看到，我们的实体图包含两个属性：名称和电话。因此，当JPA将其转换为SQL时，它将投射相关列。

5. 在交易范围内加载数据

    最后，我们将探索最后一个解决方案。到目前为止，我们已经看到这个问题与持久性上下文生命周期有关。

    发生的情况是，我们的持久性上下文是[事务作用域](https://www.baeldung.com/jpa-hibernate-persistence-context#transaction_persistence_context)的，并将保持开放，直到交易结束。事务生命周期从执行存储库方法的开始到结束。

    因此，让我们创建另一个测试用例，并配置我们的持久性上下文，以绑定到由我们的测试方法启动的事务。我们将保持持久上下文开放，直到测试结束：

    ```java
    @Test
    @Transactional
    public void whenUseTransaction_thenFetchResult() {
        Employee employee = employeeRepository.findById(1);
        assertThat(employee.getPhones().size(), is(2));
    }
    ```

    @Transactional注释围绕相关测试类的实例配置事务代理。此外，该事务与执行它的线程相关联。考虑到默认事务传播设置，使用此方法创建的每个持久上下文都会加入同一事务。因此，事务持久性上下文与测试方法的事务范围绑定。

6. 结论

    在本教程中，我们评估了三种不同的解决方案，以解决在封闭持久上下文中从懒惰关联中读取数据的问题。

    首先，我们使用JPA查询语言来获取元素集合。接下来，我们定义了一个实体图来检索必要的数据。

    而且，在最终解决方案中，我们使用Spring Transaction来保持持久性上下文的开放，并读取所需的数据。
