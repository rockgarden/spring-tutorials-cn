# [Spring Data JPA @查询](https://www.baeldung.com/spring-data-jpa-query)

1. 一览表

    Spring Data提供了多种方法来定义我们可以执行的查询。其中之一是@Query注释。

    在本教程中，我们将演示如何使用Spring Data JPA中的@Query注释来执行JPQL和本机SQL查询。

    当@Query注释不足时，我们还将展示如何构建动态查询。

2. 选择查询

    为了定义要执行的Spring Data存储库方法的SQL，我们可以用@Query注释对方法进行注释——其值属性包含要执行的JPQL或SQL。

    @Query注释优先于命名查询，这些查询使用@NamedQuery注释或在orm.xml文件中定义。

    将查询定义放在存储库内的方法上方，而不是作为命名查询在我们的域模型中，这是一种很好的方法。存储库负责持久性，因此它是存储这些定义的更好场所。

    1. JPQL

        默认情况下，查询定义使用JPQL。

        让我们来看看一个简单的存储库方法，它从数据库中返回活动用户实体：

        ```java
        @Query("SELECT u FROM User u WHERE u.status = 1")
        Collection<User> findAllActiveUsers();
        ```

    2. 本地的

        我们还可以使用原生SQL来定义我们的查询。我们要做的就是将nativeQuery属性的值设置为true，并在注释的值属性中定义本机SQL查询：

        ```java
        @Query(
        value = "SELECT * FROM USERS u WHERE u.status = 1", 
        nativeQuery = true)
        Collection<User> findAllActiveUsersNative();
        ```

3. 在查询中定义顺序

    我们可以将Sort类型的附加参数传递给具有@Query注释的Spring Data方法声明。它将被翻译成传递给数据库的ORDER BY子句。

    1. JPA提供和衍生方法的分类

        对于我们开箱即用的方法，如findAll（排序）或解析方法签名生成的方法，我们只能使用对象属性来定义我们的排序：

        `userRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));`

        现在想象一下，我们想按名称属性的长度进行排序：

        `userRepository.findAll(Sort.by("LENGTH(name)"));`

        当我们执行上述代码时，我们将收到一个异常：

        `org.springframework.data.mapping.PropertyReferenceException: No property LENGTH(name) found for type User!`

    2. JPQL

        当我们使用JPQL进行查询定义时，Spring Data可以毫无问题地处理排序——我们所要做的就是添加一个Sort类型的方法参数：

        ```java
        @Query(value = "SELECT u FROM User u")
        List<User> findAllUsers(Sort sort);
        ```

        我们可以调用此方法并传递一个排序参数，该参数将根据用户对象的名称属性对结果进行排序：

        `userRepository.findAllUsers(Sort.by("name"));`

        由于我们使用了@Query注释，我们可以使用相同的方法按用户名称的长度获取排序的用户列表：

        `userRepository.findAllUsers(JpaSort.unsafe("LENGTH(name)"));`

        至关重要的是，我们使用JpaSort.unsafe()来创建Sort对象实例。

        当我们使用时：

        `Sort.by("LENGTH(name)");`

        然后，我们将收到与上面看到的findAll（）方法相同的异常。

        当Spring Data发现使用@Query注释的方法的不安全排序顺序时，它只是将排序子句附加到查询中——它跳过检查要排序的属性是否属于域模型。

    3. Native

        当@Query注释使用本机SQL时，则无法定义排序。

        如果我们这样做，我们将收到一个例外：

        `org.springframework.data.jpa.repository.query.InvalidJpaQueryMethodException: Cannot use native queries with dynamic sorting and/or pagination`

        正如例外情况所说，原生查询不支持排序。错误消息给了我们一个提示，分页也会导致异常。

        然而，有一个实现分页的变通方法，我们将在下一节中介绍它。

4. 在查询中连接表

    当我们在@Query注释中的查询中使用join子句时，在处理多个表的列时，有多种方法可以定义select子句。

    1. JPQL

        使用JPQL查询时，我们需要创建投影/DTO来返回连接表中的必填字段。例如：

        ```java
        @Query(value = "SELECT new ResultDTO(c.id, o.id, p.id, c.name, c.email, o.orderDate, p.productName, p.price) "
        + " from Customer c, CustomerOrder o ,Product p "
        + " where c.id=o.customer.id "
        + " and o.id=p.customerOrder.id "
        + " and c.id=?1 ")
        List<ResultDTO> findResultDTOByCustomer(Long id);
        ```

        DTO和ResultDTO类定义如下：

        ```java
        class DTO {
            private Long customer_id;
            private Long order_id;
            private Long product_id;

            public DTO(Long customer_id, Long order_id, Long product_id) {
                this.customer_id = customer_id;
                this.order_id = order_id;
                this.product_id = product_id;
            }
        }

        @Entity
        @IdClass(DTO.class)
        public class ResultDTO {
            @Id
            private Long customer_id;
            @Id
            private Long order_id;
            @Id
            private Long product_id;
            private String customerName;
            private String customerEmail;
            private LocalDate orderDate;
            private String productName;
            private Double productPrice;
            
            // getters, setters, constructors etc
        }
        ```

        如所见，ResultDTO类有一个复合主键，该主键在单独的类DTO中定义，并通过@IdClass注释引用。仅当存储库方法返回以Id为复合键的DTO时，才需要此操作。

    2. Native

        当我们想使用本机查询使用Spring Data JPA API连接表并获取结果时，我们无法指定要为select子句中的对象选择的列子集，相反，我们必须创建如下存储库方法：

        ```java
        @Query(value = "SELECT c.*, o.*, p.* "
        + " from Customer c, CustomerOrder o ,Product p "
        + " where c.id=o.customer_id "
        + " and o.id=p.customerOrder_id "
        + " and c.id=?1 "
        , nativeQuery = true)
        List<Map<String, Object>> findByCustomer(Long id);
        ```

        我们可以看到，findByCustomer方法的返回类型是映射列表，其中映射中的键与nativeQuery中的列名相对应。列表本身对应于该方法返回的对象列表。

        在nativeQuery方法中，我们不需要创建任何DTO类，但同时，nativeQuery从@Query中加入的所有表中获取所有列。

5. 分页

    分页允许我们仅返回页面中整个结果的子集。例如，在浏览网页上的几页数据时，这很有用。

    分页的另一个优点是，从服务器发送到客户端的数据量最小化。通过发送较小的数据，我们通常可以看到性能的提高。

    1. JPQL

        在JPQL查询定义中使用分页很简单：

        ```java
        @Query(value = "SELECT u FROM User u ORDER BY id")
        Page<User> findAllUsersWithPagination(Pageable pageable);
        ```

        我们可以传递PageRequest参数来获取一页数据。

        分页也支持本机查询，但需要一点额外的工作。

    2. 本地的

        我们可以通过声明附加属性countQuery来启用本地查询的分页。

        这定义了要执行的SQL来计算整个结果中的行数：

        ```java
        @Query(
        value = "SELECT * FROM Users ORDER BY id",
        countQuery = "SELECT count(*) FROM Users",
        nativeQuery = true)
        Page<User> findAllUsersWithPagination(Pageable pageable);
        ```

    3. 2.0.4之前的Spring Data JPA版本

        上述原生查询解决方案适用于Spring Data JPA版本2.0.4及更高版本。

        在该版本之前，当我们尝试执行此类查询时，我们将收到与上一节中描述的相同异常。

        我们可以通过在查询中添加额外的分页参数来克服这一点：

        ```java
        @Query(
        value = "SELECT * FROM Users ORDER BY id \n-- #pageable\n",
        countQuery = "SELECT count(*) FROM Users",
        nativeQuery = true)
        Page<User> findAllUsersWithPagination(Pageable pageable);
        ```

        在上述示例中，我们添加了

        `\n-- #pageable\n`

        作为分页参数的占位符。这告诉Spring Data JPA如何解析查询并注入可寻页参数。此解决方案适用于H2数据库。

        我们已经涵盖了如何通过JPQL和本机SQL创建简单的选择查询。接下来，我们将展示如何定义其他参数。

6. 索引查询参数

    有两种可能的方式可以将方法参数传递给我们的查询：索引参数和命名参数。

    在本节中，我们将介绍索引参数。

    1. JPQL

        对于JPQL中的索引参数，Spring Data将按照方法声明中显示的相同顺序将方法参数传递给查询：

        ```java
        @Query("SELECT u FROM User u WHERE u.status = ?1")
        User findUserByStatus(Integer status);

        @Query("SELECT u FROM User u WHERE u.status = ?1 and u.name = ?2")
        User findUserByStatusAndName(Integer status, String name);
        ```

        对于上述查询，状态方法参数将分配给索引1的查询参数，名称方法参数将分配给索引2的查询参数。

    2. Native

        本机查询的索引参数的工作方式与JPQL完全相同：

        ```java
        @Query(
        value = "SELECT * FROM Users u WHERE u.status = ?1", 
        nativeQuery = true)
        User findUserByStatusNative(Integer status);
        ```

        在下一节中，我们将展示一种不同的方法：通过名称传递参数。

7. 命名参数

    我们还可以使用命名参数将方法参数传递给查询。我们使用存储库方法声明中的@Param注释来定义这些。

    每个用@Param注释的参数必须有一个与相应的JPQL或SQL查询参数名称匹配的值字符串。带有命名参数的查询更容易阅读，在需要重构查询的情况下，错误的发生性较小。

    1. JPQL

        如上所述，我们在方法声明中使用@Param注释，将JPQL中按名称定义的参数与方法声明中的参数匹配：

        ```java
        @Query("SELECT u FROM User u WHERE u.status = :status and u.name = :name")
        User findUserByStatusAndNameNamedParams(
        @Param("status") Integer status,
        @Param("name") String name);
        ```

        请注意，在上述示例中，我们定义了SQL查询和方法参数具有相同的名称，但只要值字符串相同，就不需要：

        ```java
        @Query("SELECT u FROM User u WHERE u.status = :status and u.name = :name")
        User findUserByUserStatusAndUserName(@Param("status") Integer userStatus, 
        @Param("name") String userName);
        ```

    2. 本地的

        对于本机查询定义，与JPQL相比，我们通过名称将参数传递给查询的方式没有区别——我们使用@Param注释：

        ```java
        @Query(value = "SELECT * FROM Users u WHERE u.status = :status and u.name = :name", 
        nativeQuery = true)
        User findUserByStatusAndNameNamedParamsNative(
        @Param("status") Integer status, @Param("name") String name);
        ```

8. 集合参数

    让我们考虑一下JPQL或SQL查询的where子句包含IN（或NOT IN）关键字的情况：

    `SELECT u FROM User u WHERE u.name IN :names`

    在这种情况下，我们可以定义一个将Collection作为参数的查询方法：

    ```java
    @Query(value = "SELECT u FROM User u WHERE u.name IN :names")
    List<User> findUserByNameList(@Param("names") Collection<String> names);
    ```

    由于参数是一个集合，它可以与列表、散列集等一起使用。

    接下来，我们将展示如何使用@Modifying注释修改数据。

9. 使用@Modifying更新查询

    我们可以使用@Query注释通过向存储库方法添加@Modifying注释来修改数据库的状态。

    1. JPQL

        与选择查询相比，修改数据的存储库方法有两个区别——它有@修改注释，当然，JPQL查询使用更新而不是选择：

        ```java
        @Modifying
        @Query("update User u set u.status = :status where u.name = :name")
        int updateUserSetStatusForName(@Param("status") Integer status, 
        @Param("name") String name);
        ```

        返回值定义了执行查询时更新的行数。索引和命名参数都可以在更新查询中使用。

    2. Native

        我们也可以通过本机查询来修改数据库的状态。我们只需要添加@Modifying注释：

        ```java
        @Modifying
        @Query(value = "update Users u set u.status = ? where u.name = ?", 
        nativeQuery = true)
        int updateUserSetStatusForNameNative(Integer status, String name);
        ```

    3. Inserts

        要执行插入操作，我们必须同时应用@Modifying并使用本机查询，因为INSERT不是JPA接口的一部分：

        ```java
        @Modifying
        @Query(
        value =
            "insert into Users (name, age, email, status) values (:name, :age, :email, :status)",
        nativeQuery = true)
        void insertUser(@Param("name") String name, @Param("age") Integer age, 
        @Param("status") Integer status, @Param("email") String email);
        ```

10. 动态查询

    通常，我们会遇到需要根据条件或数据集构建SQL语句，其值仅在运行时为人所知。在这些情况下，我们不能只使用静态查询。

    1. 动态查询的示例

        例如，让我们想象一下，我们需要从运行时定义的集合中选择所有电子邮件LIKE one的用户——email1、email2、...、emailn：

        ```sql
        SELECT u FROM User u WHERE u.email LIKE '%email1%' 
            or  u.email LIKE '%email2%'
            ... 
            or  u.email LIKE '%emailn%'
        ```

        由于集合是动态构建的，我们在编译时无法知道要添加多少个LIKE子句。

        在这种情况下，我们不能只使用@Query注释，因为我们无法提供静态SQL语句。

        相反，通过实现自定义复合存储库，我们可以扩展基础JpaRepository功能，并提供我们自己的逻辑来构建动态查询。让我们来看看怎么做这个。

    2. 自定义存储库和JPA标准API

        对我们来说，幸运的是，Spring提供了一种通过使用自定义片段接口来扩展基础存储库的方法。然后，我们可以将它们链接在一起，以创建一个[复合存储库](https://www.baeldung.com/spring-data-composable-repositories)。

        我们将从创建一个自定义片段界面开始：

        ```java
        public interface UserRepositoryCustom {
            List<User> findUserByEmails(Set<String> emails);
        }
        ```

        然后我们将实施它：

        ```java
        public class UserRepositoryCustomImpl implements UserRepositoryCustom {

            @PersistenceContext
            private EntityManager entityManager;

            @Override
            public List<User> findUserByEmails(Set<String> emails) {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<User> query = cb.createQuery(User.class);
                Root<User> user = query.from(User.class);

                Path<String> emailPath = user.get("email");

                List<Predicate> predicates = new ArrayList<>();
                for (String email : emails) {
                    predicates.add(cb.like(emailPath, email));
                }
                query.select(user)
                    .where(cb.or(predicates.toArray(new Predicate[predicates.size()])));

                return entityManager.createQuery(query)
                    .getResultList();
            }
        }
        ```

        如上所示，我们利用JPA标准API来构建我们的动态查询。

        此外，我们需要确保在类名中包含Impl后缀。Spring将搜索UserRepositoryCustom实现为UserRepositoryCustomImpl。由于片段本身不是存储库，Spring依靠此机制来查找片段实现。

    3. 扩展现有存储库

        请注意，从第2节到第7节的所有查询方法都在用户存储库中。

        因此，现在我们将通过扩展用户存储库中的新界面来整合我们的片段：

        ```java
        public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryCustom {
            //  query methods from section 2 - section 7
        }
        ```

    4. 使用存储库

        最后，我们可以调用我们的动态查询方法：

        ```java
        Set<String> emails = new HashSet<>();
        // filling the set with any number of items

        userRepository.findUserByEmails(emails);
        ```

        我们已成功创建了一个复合存储库，并调用了我们的自定义方法。

11. 结论

    在本文中，我们介绍了使用@Query注释在Spring Data JPA存储库方法中定义查询的几种方法。

    我们还学习了如何实现自定义存储库和创建动态查询。
