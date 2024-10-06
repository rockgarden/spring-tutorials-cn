# [JPA中的悲观锁定](https://www.baeldung.com/jpa-pessimistic-locking)

1. 一览表

    有很多情况下，我们想从数据库中检索数据。有时我们想把它锁起来，以便进一步处理，这样其他人就无法打断我们的行动。

    我们可以想到两种并发控制机制，允许我们做到这一点：设置适当的事务隔离级别或设置我们目前需要的数据锁定。

    事务隔离是为数据库连接定义的。我们可以对其进行配置，以保留不同程度的锁定数据。

    然而，一旦创建连接，隔离级别就会被设置，它会影响该连接中的每个语句。幸运的是，我们可以使用悲观锁定，它使用数据库机制来保留对数据的更精细的独家访问。

    我们可以使用悲观锁来确保没有其他交易可以修改或删除保留的数据。

    我们可以保留两种类型的锁：专用锁和共享锁。当其他人持有共享锁时，我们可以读取但不能写入数据。为了修改或删除保留的数据，我们需要有一个专属锁。

    我们可以使用“SELECT ... FOR UPDATE”语句来获取独家锁。

2. 锁定模式

    JPA规范定义了我们将要讨论的三种悲观锁定模式：

    - PESSIMISTIC_READ允许我们获得共享锁，并防止数据被更新或删除。
    - PESSIMISTIC_WRITE允许我们获得专属锁，并防止数据被读取、更新或删除。
    - PESSIMISTIC_FORCE_INCREMENT的工作方式与PESSIMISTIC_WRITE类似，它还增加了版本化实体的版本属性。

    它们都是LockModeType类的静态成员，允许事务获取数据库锁。它们都被保留，直到交易提交或回滚。

    值得注意的是，我们一次只能获得一把锁。如果不可能，则会抛出一个持久性异常。

    1. PESSIMISTIC_READ

        每当我们只想读取数据并且不会遇到脏读时，我们都可以使用PESSIMISTIC_READ（共享锁）。不过，我们无法进行任何更新或删除。

        有时，我们使用的数据库不支持PESSIMISTIC_READ锁，因此我们可以获取PESSIMISTIC_WRITE锁。

    2. PESSIMISTIC_WRITE

        任何需要获取数据锁并更改数据的交易都应获得PESSIMISTIC_WRITE锁。根据JPA规范，保持PESSIMISTIC_WRITE锁将防止其他事务读取、更新或删除数据。

        请注意，一些数据库系统实现了多版本并发控制，允许读者获取已被阻止的数据。

    3. PESSIMISTIC_FORCE_INCREMENT

        此锁的工作原理与PESSIMISTIC_WRITE相似，但它是为与版本化实体合作而引入的——具有@Version注释属性的实体。

        版本化实体的任何更新都可以在获取PESSIMISTIC_FORCE_INCREMENT锁之前。获取该锁会导致更新版本列。

        由持久性提供商来决定它是否支持未版本化实体的PESSIMISTIC_FORCE_INCREMENT。如果没有，它会抛出持久性异常。

    4. 例外

        最好知道在使用悲观锁定时可能会出现哪些异常。JPA规范提供了不同类型的例外：

        - PessimisticLockException表示获取锁或将共享锁转换为排他性锁失败，并导致事务级回滚。
        - LockTimeoutException表示获取锁或将共享锁转换为排他性超时，并导致语句级回滚。
        - 持久性异常表明发生了持久性问题。PersistenceException及其子类型，NoResultException、NonUniqueResultException、LockTimeoutException和QueryTimeoutException除外，标记要回滚的活动事务。

3. 使用悲观锁

    有几种可能的方法可以在单个记录或一组记录上配置悲观锁。让我们看看如何在JPA中做到这一点。

    1. 查找

        找到可能是最直接的方法。

        将LockModeType对象作为参数传递给查找方法就足够了：

        `entityManager.find(Student.class, studentId, LockModeType.PESSIMISTIC_READ);`

    2. 查询

        此外，我们还可以使用查询对象，并以锁定模式作为参数调用setLockMode设置器：

        ```java
        Query query = entityManager.createQuery("from Student where studentId = :studentId");
        query.setParameter("studentId", studentId);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.getResultList()
        ```

    3. 明确锁定

        也可以手动锁定通过查找方法检索到的结果：

        ```java
        Student resultStudent = entityManager.find(Student.class, studentId);
        entityManager.lock(resultStudent, LockModeType.PESSIMISTIC_WRITE);
        ```

    4. 刷新

        如果我们想通过刷新方法覆盖实体的状态，我们也可以设置锁：

        ```java
        Student resultStudent = entityManager.find(Student.class, studentId);
        entityManager.refresh(resultStudent, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        ```

        3.5。命名查询

        @NamedQuery注释也允许我们设置锁定模式：

        ```java
        @NamedQuery(name="lockStudent",
            query="SELECT s FROM Student s WHERE s.id LIKE :studentId",
            lockMode = PESSIMISTIC_READ)
        ```

4. 锁定范围

    锁定范围参数定义了如何处理锁定实体的锁定关系。有可能仅在查询中定义的单个实体上获取锁，或额外阻止其关系。

    为了配置范围，我们可以使用PessimisticLockScope enum。它包含两个值：正常值和扩展值。

    我们可以通过将带有PessimisticLockScope值的参数“jakarta.persistence”作为参数传递给EntityManager、Query、TypedQuery或NamedQuery的适当方法来设置范围：

    ```java
    Map<String, Object> properties = new HashMap<>();
    map.put("jakarta.persistence", PessimisticLockScope.EXTENDED);

    entityManager.find(
    Student.class, 1L, LockModeType.PESSIMISTIC_WRITE, properties);
    ```

    1. PessimisticLockScope.NORMAL

        PessimisticLockScope.NORMAL是默认范围。有了这个锁定范围，我们锁定了实体本身。当与联合继承一起使用时，它也会锁定祖先。

        让我们来看看包含两个实体的示例代码：

        ```java
        @Entity
        @Inheritance(strategy = InheritanceType.JOINED)
        public class Person {

            @Id
            private Long id;
            private String name;
            private String lastName;

            // getters and setters
        }

        @Entity
        public class Employee extends Person {

            private BigDecimal salary;

            // getters and setters
        }
        ```

        当我们想要获得员工的锁定时，我们可以观察跨越这两个实体的SQL查询：

        ```sql
        SELECT t0.ID, t0.DTYPE, t0.LASTNAME, t0.NAME, t1.ID, t1.SALARY
        FROM PERSON t0, EMPLOYEE t1
        WHERE ((t0.ID = ?) AND ((t1.ID = t0.ID) AND (t0.DTYPE = ?))) FOR UPDATE
        ```

    2. PessimisticLockScope.EXTENDED

        扩展范围涵盖了与正常相同的功能。此外，它能够阻止连接表中的相关实体。

        简单地说，它与@ElementCollection或@OneToOne、@OneToMany等与@JoinTable注释的实体一起工作。

        让我们来看看带有@ElementCollection注释的示例代码：

        ```java
        @Entity
        public class Customer {

            @Id
            private Long customerId;
            private String name;
            private String lastName;
            @ElementCollection
            @CollectionTable(name = "customer_address")
            private List<Address> addressList;

            // getters and setters
        }

        @Embeddable
        public class Address {

            private String country;
            private String city;

            // getters and setters
        }
        ```

        让我们在搜索客户实体时分析一些查询：

        ```sql
        SELECT CUSTOMERID, LASTNAME, NAME
        FROM CUSTOMER WHERE (CUSTOMERID = ?) FOR UPDATE

        SELECT CITY, COUNTRY, Customer_CUSTOMERID
        FROM customer_address
        WHERE (Customer_CUSTOMERID = ?) FOR UPDATE
        ```

        我们可以看到，有两个FOR UPDATE查询，在客户表中锁定一行和连接表中锁定一行。

        另一个需要注意的有趣事实是，并非所有持久性提供商都支持锁定范围。

5. 设置锁定超时

    除了设置锁定范围外，我们还可以调整另一个锁定参数——超时。超时值是我们希望等待获取锁的毫秒数，直到LockTimeoutException发生。

    我们可以通过使用属性“jakarta.persistence.lock.timeout”来更改超时值，该值与锁定范围类似。

    也可以通过将超时值更改为零来指定“无等待”锁定。

    然而，我们应该记住，有些数据库驱动程序不支持以这种方式设置超时值：

    ```java
    Map<String, Object> properties = new HashMap<>();
    map.put("jakarta.persistence.lock.timeout", 1000L);

    entityManager.find(
    Student.class, 1L, LockModeType.PESSIMISTIC_READ, properties);
    ```

6. 结论

    当设置适当的隔离水平不足以应对并发交易时，JPA给了我们悲观的锁定。它使我们能够隔离和协调不同的交易，这样它们就不会同时访问相同的资源。

    为了实现这一点，我们可以在讨论的锁类型之间进行选择，并相应地修改其范围或超时等参数。

    另一方面，我们应该记住，了解数据库锁与了解底层数据库系统的机制一样重要。

    同样重要的是要记住，悲观锁的行为取决于与我们合作的持久性提供商。

    最后，本教程的源代码可在GitHub上获得Hibernate和[EclipseLink](/persistence-modules/spring-data-eclipselink)。
