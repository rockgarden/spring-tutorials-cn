# [在Spring Data JPA中启用交易锁定](https://www.baeldung.com/java-jpa-transaction-locks)

1. 一览表

    在本快速教程中，我们将讨论在Spring Data JPA中为自定义查询方法和预定义的存储库CRUD方法启用事务锁。

    我们还将了解不同的锁类型和设置交易锁超时。

2. 锁类型

    JPA定义了两种主要锁类型，悲观锁和乐观锁。

    1. 悲观的锁定

        当我们在交易中使用悲观锁定并访问实体时，它将立即被锁定。交易通过提交或回滚交易来释放锁。

    2. 乐观的锁定

        在乐观锁定中，交易不会立即锁定实体。相反，事务通常保存实体的状态，并分配给它一个版本号。

        当我们尝试在不同事务中更新实体的状态时，该事务会在更新期间将保存的版本号与现有版本号进行比较。

        此时，如果版本号不同，则表示我们无法修改实体。如果有活动事务，则该事务将被回滚，底层JPA实现将抛出[OptimisticLockException](https://docs.oracle.com/javaee/7/api/javax/persistence/OptimisticLockException.html)。

        根据最适合我们当前开发环境的方法，我们还可以使用其他方法，如时间戳、散列值计算或序列化校验和。

3. 在查询方法上启用事务锁

    要获取实体上的锁，我们可以通过传递所需的[锁](https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/Lock.html)模式类型来用锁注释对目标查询方法进行注释。

    [锁定模式类型](https://docs.oracle.com/javaee/7/api/javax/persistence/LockModeType.html)是在锁定实体时指定的枚举值。然后将指定的锁模式传播到数据库，以对实体对象应用相应的锁。

    要在Spring Data JPA存储库的自定义查询方法上指定锁定，我们可以用@Lock注释该方法，并指定所需的锁定模式类型：

    ```java
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT c FROM Customer c WHERE c.orgId = ?1")
    public List<Customer> fetchCustomersByOrgId(Long orgId);
    ```

    为了对预定义的存储库方法（如findAll或findById（id））强制执行锁定，我们必须在存储库中声明该方法，并用Lock注释注释该方法：

    ```java
    @Lock(LockModeType.PESSIMISTIC_READ)
    public Optional<Customer> findById(Long customerId);
    ```

    当明确启用锁，并且没有活动事务时，底层JPA实现将抛出[TransactionRequiredException](https://docs.oracle.com/javaee/7/api/javax/persistence/TransactionRequiredException.html)。

    如果无法授予锁定，并且锁定冲突不会导致事务回滚，JPA会抛出一个[LockTimeoutException](https://docs.oracle.com/javaee/7/api/javax/persistence/LockTimeoutException.html)，但它不会标记活动事务进行回滚。

4. 设置交易锁定超时

    使用悲观锁定时，数据库将尝试立即锁定实体。当无法立即获得锁时，底层JPA实现会抛出LockTimeoutException。为了避免此类异常，我们可以指定锁定超时值。

    在Spring Data JPA中，可以通过在查询方法上放置QueryHint来使用QueryHints注释来指定锁定超时：

    ```java
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    public Optional<Customer> findById(Long customerId);
    ```

    我们可以在这篇[ObjectDB文章](https://www.objectdb.com/java/jpa/persistence/lock#Pessimistic_Locking_)中找到有关在不同范围内设置锁定超时提示的更多详细信息。

5. 结论

    在本文中，我们探讨了不同类型的交易锁定模式。然后我们学习了如何在Spring Data JPA中启用事务锁。我们还涵盖了设置锁定超时。

    在正确的地方应用正确的事务锁有助于在大批量并发使用应用程序中保持数据完整性。

    当交易需要严格遵守ACID规则时，我们应该使用悲观锁定。当我们需要允许多次并发读取时，以及在应用程序上下文中最终一致性是可以接受的时，应应用乐观锁定。
