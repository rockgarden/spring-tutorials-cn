# [JPA中的乐观锁定](https://www.baeldung.com/jpa-optimistic-locking)

1. 一览表

    当涉及到企业应用程序时，正确管理对数据库的并发访问至关重要。这意味着我们应该能够以有效的、最重要的是防错的方式处理多笔交易。

    此外，我们需要确保数据在并发读取和更新之间保持一致。

    为了实现这一目标，我们可以使用Java Persistence API提供的乐观锁定机制。这样，同时对同一数据进行多次更新就不会相互干扰。

2. 了解乐观锁定

    为了使用乐观锁定，我们需要有一个实体，包括一个带有@Version注释的属性。在使用它时，每个读取数据的事务都保留版本属性的值。

    在交易想要进行更新之前，它会再次检查版本属性。

    如果值在此期间发生变化，则会抛出一个OptimisticLockException。否则，事务会提交更新并增加值版本属性。

3. 悲观锁定与乐观锁定

    与乐观锁定相反，JPA给了我们悲观的锁定。这是处理数据并发访问的另一个机制。

    我们在之前的一篇文章中涵盖了悲观锁定——JPA中的悲观锁定。让我们来了解一下它们之间的区别，以及我们如何从每种类型的锁定中受益。

    正如我们之前所说，乐观锁定是基于通过检查实体的版本属性来检测实体的变化。如果发生任何并发更新，就会发生OptimisticLockException。在那之后，我们可以再次尝试更新数据。

    我们可以想象，这种机制适用于读取比更新或删除多得多的应用程序。在实体必须分离一段时间且无法保持锁的情况下，它也很有用。

    相反，悲观锁定机制涉及数据库级别的锁定实体。

    每笔交易都可以获得数据锁定。只要它保持锁定，任何交易都无法读取、删除或对锁定的数据进行任何更新。

    我们可以假设，使用悲观锁定可能会导致僵局。然而，它确保了比乐观锁定更大的数据完整性。

4. 版本属性

    版本属性是带有@Version注释的属性。它们对于实现乐观锁定是必要的。

    让我们看看一个示例实体类：

    ```java
    @Entity
    public class Student {

        @Id
        private Long id;

        private String name;

        private String lastName;

        @Version
        private Integer version;

        // getters and setters

    }
    ```

    在声明版本属性时，我们应该遵循几个规则：

    - 每个实体类必须只有一个版本属性。
    - 对于映射到多个表的实体，它必须放置在主表中。
    - 版本属性的类型必须是以下之一：int、Integer、long、Long、short、Short、java.sql.Timestamp。

    我们应该知道，我们可以通过实体检索版本属性的值，但我们不应该更新或增加它。只有持久性提供程序才能做到这一点，因此数据保持一致。

    请注意，持久性提供程序能够支持对没有版本属性的实体进行乐观锁定。然而，在使用乐观锁定时，始终包含版本属性是个好主意。

    如果我们试图锁定一个不包含此类属性的实体，而持久性提供程序不支持它，这将导致持久性异常。

5. 锁定模式

    JPA为我们提供了两种不同的乐观锁定模式（和两个别名）：

    - OPTIMISTIC为包含版本属性的所有实体获取乐观的读取锁。
    - OPTIMISTIC_FORCE_INCREMENT获得与OPTIMISTIC相同的乐观锁，并额外增加版本属性值。
    - READ是OPTIMISTIC的同义词。
    - WRITE是OPTIMISTIC_FORCE_INCREMENT的同义词。

    我们可以在LockModeType类中找到上面列出的所有类型。

    1. OPTIMISTIC (READ)

        正如我们已经知道的，OPSTIC和READ锁定模式是同义词。然而，JPA规范建议我们在新应用程序中使用OPTIMISTIC。

        每当我们请求OPTIMISTIC锁定模式时，持久性提供商将防止我们的数据被肮脏的读取以及不可重复的读取。

        简单地说，它应该确保任何交易都未能对另一笔交易的数据进行任何修改

        - 已更新或删除，但未提交
        - 在此期间已成功更新或删除

    2. OPTIMISTIC_INCREMENT (WRITE)

        和以前一样，OPTIMISTIC_INCREMENT和WRITE是同义词，但前者更可取。

        OPTIMISTIC_INCREMENT必须满足与OPISTIC锁定模式相同的条件。此外，它增加了版本属性的值。然而，没有具体说明是否应该立即完成，还是可以推遲到提交或刷新。

        值得注意的是，当请求优化锁定模式时，持久性提供商可以提供OPTIMISTIC_INCREMENT功能。

6. 使用乐观锁定

    我们应该记住，对于版本化实体，默认情况下可以使用乐观锁定。但有几种方法可以明确地请求它。

    1. 查找

        要请求乐观锁定，我们可以传递适当的LockModeType作为参数，以查找EntityManager的方法：

        `entityManager.find(Student.class, studentId, LockModeType.OPTIMISTIC);`

    2. 查询

        另一种启用锁定的方法是使用查询对象的setLockMode方法：

        ```java
        Query query = entityManager.createQuery("from Student where id = :id");
        query.setParameter("id", studentId);
        query.setLockMode(LockModeType.OPTIMISTIC_INCREMENT);
        query.getResultList()
        ```

    3. 显式锁定

        我们可以通过调用EntityManager的锁定方法来设置锁定：

        ```java
        Student student = entityManager.find(Student.class, id);
        entityManager.lock(student, LockModeType.OPTIMISTIC);
        ```

    4. 刷新

        我们可以以与上一个方法相同的方式调用刷新方法：

        ```java
        Student student = entityManager.find(Student.class, id);
        entityManager.refresh(student, LockModeType.READ);
        ```

    5. 命名查询

        最后一个选项是将@NamedQuery与lockMode属性一起使用：

        ```java
        @NamedQuery(name="optimisticLock",
            query="SELECT s FROM Student s WHERE s.id LIKE :id",
            lockMode = WRITE)
        ```

7. 乐观锁定异常

    当持久性提供程序在实体上发现乐观的锁定冲突时，它会抛出OptimisticLockException。我们应该知道，由于例外情况，活动事务总是被标记为回滚。

    很高兴知道我们如何应对OptimisticLockException。方便的是，此异常包含对冲突实体的引用。然而，持久性提供商并非强制要求在每种情况下提供它。不能保证该对象会可用。

    不过，有一种建议的方法来处理所述的异常。我们应该通过重新加载或刷新来再次检索实体，最好是在新交易中。在那之后，我们可以尝试再次更新它。

8. 结论

    在本文中，我们熟悉了一个可以帮助我们协调并发交易的工具。

    乐观锁定使用实体中包含的版本属性来控制对实体的并发修改。

    因此，它确保任何更新或删除都不会被覆盖或无声丢失。与悲观锁定相反，它不会锁定数据库级别的实体，因此，它不会受到数据库死锁的伤害。

    我们了解到，默认情况下，对版本化实体启用乐观锁定。然而，有几种方法可以通过使用各种锁定模式类型来明确请求它。

    我们还应该记住，每次实体有冲突的更新时，我们应该期待一个OptimisticLockException。
