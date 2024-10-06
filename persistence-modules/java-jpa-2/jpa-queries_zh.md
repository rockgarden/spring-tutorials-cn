# [JPA查询的类型](https://www.baeldung.com/jpa-queries)

1. 一览表

    在本教程中，我们将讨论不同类型的[JPA](https://www.baeldung.com/jpa-hibernate-difference)查询。此外，我们将专注于比较它们之间的差异，并扩展每个优点和缺点。

2. 设置

    首先，让我们定义我们将用于本文所有示例的UserEntity类：

    ```java
    @Table(name = "users")
    @Entity
    public class UserEntity {

        @Id
        private Long id;
        private String name;
        //Standard constructor, getters and setters.

    }
    ```

    JPA查询有三种基本类型：

    - 查询，用Java持久查询语言（JPQL）语法编写
    - NativeQuery，用简单的SQL语法编写
    - 标准API查询，通过不同的方法以编程方式构建

    让我们来探索它们。

3. 查询

    查询在语法上与SQL相似，通常用于执行CRUD操作：

    ```java
    public UserEntity getUserByIdWithPlainQuery(Long id) {
        Query jpqlQuery = getEntityManager().createQuery("SELECT u FROM UserEntity u WHERE u.id=:id");
        jpqlQuery.setParameter("id", id);
        return (UserEntity) jpqlQuery.getSingleResult();
    }
    ```

    此查询从用户表中检索匹配的记录，并将其映射到UserEntity对象。

    还有两种额外的查询子类型：

    - TypedQuery
    - NamedQuery

    让我们看看他们的行动。

    1. 类型查询

        我们需要注意上一个例子中的返回语句。JPA无法推断查询结果类型是什么，因此，我们必须进行铸造。

        但是，JPA提供了一个特殊的查询子类型，称为TypedQuery。如果我们事先知道我们的查询结果类型，这总是首选。此外，它使我们的代码更加可靠和更容易测试。

        让我们看看TypedQuery的替代方案，与我们的第一个例子相比：

        ```java
        public UserEntity getUserByIdWithTypedQuery(Long id) {
            TypedQuery<UserEntity> typedQuery
            = getEntityManager().createQuery("SELECT u FROM UserEntity u WHERE u.id=:id", UserEntity.class);
            typedQuery.setParameter("id", id);
            return typedQuery.getSingleResult();
        }
        ```

        通过这种方式，我们免费获得更强的打字，避免了未来可能的铸造异常。

    2. 命名查询

        虽然我们可以在特定方法上动态定义查询，但它们最终可能会发展成一个难以维护的代码库。如果我们能将一般使用查询保存在一个集中的、易于阅读的地方呢？

        JPA还通过另一种称为NamedQuery的查询子类型向我们提供了这方面的报道。

        我们可以在orm.xml或属性文件中定义NamedQueries。

        此外，我们可以在实体类本身上定义NamedQuery，提供一种集中、快速和简单的方法来读取和查找实体的相关查询。

        所有NamedQueries都必须有一个唯一的名称。

        让我们看看如何将NamedQuery添加到我们的UserEntity类中：

        ```java
        @Table(name = "users")
        @Entity
        @NamedQuery(name = "UserEntity.findByUserId", query = "SELECT u FROM UserEntity u WHERE u.id=:userId")
        public class UserEntity {

            @Id
            private Long id;
            private String name;
            //Standard constructor, getters and setters.

        }
        ```

        如果我们在版本8之前使用Java，@NamedQuery注释必须分组在@NamedQueries注释中。从Java 8开始，我们可以简单地在实体类中重复@NamedQuery注释。

        使用NamedQuery非常简单：

        ```java
        public UserEntity getUserByIdWithNamedQuery(Long id) {
            Query namedQuery = getEntityManager().createNamedQuery("UserEntity.findByUserId");
            namedQuery.setParameter("userId", id);
            return (UserEntity) namedQuery.getSingleResult();
        }
        ```

4. 原生查询

    NativeQuery只是一个SQL查询。这些允许我们释放数据库的全部功能，因为我们可以使用JPQL限制语法中没有的专有功能。

    这是有代价的。我们通过NativeQuery失去了应用程序的数据库可移植性，因为我们的JPA提供商不能再从数据库实现或供应商中提取具体细节。

    让我们看看如何使用NativeQuery，该Query的结果与我们之前的例子相同：

    ```java
    public UserEntity getUserByIdWithNativeQuery(Long id) {
        Query nativeQuery
        = getEntityManager().createNativeQuery("SELECT * FROM users WHERE id=:userId", UserEntity.class);
        nativeQuery.setParameter("userId", id);
        return (UserEntity) nativeQuery.getSingleResult();
    }
    ```

    我们必须始终考虑NativeQuery是否是唯一的选择。大多数时候，一个好的JPQL查询可以满足我们的需求，最重要的是，从实际数据库实现中保持一定程度的抽象。

    使用NativeQuery并不一定意味着将自己锁定在一个特定的数据库供应商上。毕竟，如果我们的查询不使用专有的SQL命令，只使用标准的SQL语法，切换提供商应该不是问题。

5. 查询、命名查询和原生查询

    到目前为止，我们已经了解了Query、NamedQuery和NativeQuery。

    现在，让我们快速重温它们，总结一下它们的利弊。

    1. 查询

        我们可以使用entityManager.createQuery（queryString）创建查询。

        接下来，让我们来探索查询的利弊：

        优点：

        - 当我们使用EntityManager创建查询时，我们可以构建动态查询字符串
        - 查询是用JPQL编写的，所以它们是可移植的

        缺点：

        - 对于动态查询，根据[查询计划缓存](https://www.baeldung.com/hibernate-query-plan-cache)，它可以多次编译成本机SQL语句
        - 查询可能会分散到各种Java类中，并与Java代码混合在一起。因此，如果一个项目包含许多查询，可能很难维护

    2. 命名查询

        一旦定义了NamedQuery，我们就可以使用EntityManager引用它：

        `entityManager.createNamedQuery(queryName);`

        现在，让我们来看看NamedQueries的优缺点：

        优点：

        - 加载持久性单元时，会编译和验证NamedQueries。也就是说，它们只编译了一次
        - 我们可以集中NamedQueries，使其更容易维护——例如，在orm.xml、属性文件或@Entity类中

        缺点：

        - NamedQueries总是静态的
        - NamedQueries可以在Spring Data JPA存储库中引用。然而，不支持动态排序

    3. 原生查询

        我们可以使用EntityManager创建NativeQuery：

        `entityManager.createNativeQuery(sqlStmt);`

        根据结果映射，我们还可以将第二个参数传递给方法，例如实体类，正如我们在上一个例子中看到的那样。

        NativeQueries也有优点和缺点。让我们快速看看它们：

        优点：

        - 随着我们的查询变得复杂，有时JPA生成的SQL语句不是最优化的。在这种情况下，我们可以使用NativeQueries来提高查询效率
        - NativeQueries允许我们使用数据库供应商特定的功能。有时，这些功能可以使我们的查询性能更好

        缺点：

        - 供应商特定功能可以带来便利和更好的性能，但我们通过失去从一个数据库到另一个数据库的可移植性来支付这一好处

6. 标准API查询

    标准API查询是编程构建的、类型安全的查询——在语法上与JPQL查询有些相似：

    ```java
    public UserEntity getUserByIdWithCriteriaQuery(Long id) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> userRoot = criteriaQuery.from(UserEntity.class);
        UserEntity queryResult = getEntityManager().createQuery(criteriaQuery.select(userRoot)
        .where(criteriaBuilder.equal(userRoot.get("id"), id)))
        .getSingleResult();
        return queryResult;
    }
    ```

    直接使用Critrias API查询可能令人生畏，但当我们需要添加动态查询元素或与[JPA元模型](https://www.baeldung.com/hibernate-criteria-queries-metamodel)耦合时，它们可能是一个很好的选择。

7. 结论

    在这篇短文中，我们了解了什么是JPA查询，以及它们的用法。

    JPA查询是从数据访问层抽象业务逻辑的好方法，因为我们可以依赖JPQL语法，并让我们选择的JPA提供商处理查询翻译。
