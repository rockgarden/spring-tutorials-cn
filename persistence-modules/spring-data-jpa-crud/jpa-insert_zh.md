# [在JPA中插入声明](https://www.baeldung.com/jpa-insert)

1. 一览表

    在本快速教程中，我们将学习如何在JPA对象上执行INSERT语句。

2. JPA中的持久对象

    在JPA中，从瞬态到托管状态的每个实体都由[EntityManager](https://www.baeldung.com/hibernate-entitymanager)自动处理。

    实体管理器检查给定的实体是否已经存在，然后决定是否应该插入或更新它。由于这种自动管理，JPA唯一允许的语句是SELECT、UPDATE和DELETE。

    在下面的例子中，我们将研究管理和绕过这一限制的不同方法。

3. 定义一个通用模型

    现在，让我们从定义一个简单的实体开始，我们将在本教程中使用：

    ```java
    @Entity
    public class Person {

        @Id
        private Long id;
        private String firstName;
        private String lastName;

        // standard getters and setters, default and all-args constructors
    }
    ```

    此外，让我们定义一个存储库类，我们将用于我们的实现：

    ```java
    @Repository
    public class PersonInsertRepository {

        @PersistenceContext
        private EntityManager entityManager;

    }
    ```

    此外，我们将应用@Transactional注释，在Spring之前自动处理交易。这样，我们不必担心使用我们的实体管理器创建交易，提交更改，或在异常的情况下手动执行回滚。

4. createNativeQuery

    对于手动创建的查询，我们可以使用EntityManager#createNativeQuery方法。它允许我们创建任何类型的SQL查询，而不仅仅是JPA支持的查询。让我们在我们的存储库类中添加一个新方法：

    ```java
    @Transactional
    public void insertWithQuery(Person person) {
        entityManager.createNativeQuery("INSERT INTO person (id, first_name, last_name) VALUES (?,?,?)")
        .setParameter(1, person.getId())
        .setParameter(2, person.getFirstName())
        .setParameter(3, person.getLastName())
        .executeUpdate();
    }
    ```

    通过这种方法，我们需要定义一个字面查询，包括列的名称并设置相应的值。

    我们现在可以测试我们的存储库：

    ```java
    @Test
    public void givenPersonEntity_whenInsertedTwiceWithNativeQuery_thenPersistenceExceptionExceptionIsThrown() {
        Person person = new Person(1L, "firstname", "lastname");

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() -> {
            personInsertRepository.insertWithQuery(PERSON);
            personInsertRepository.insertWithQuery(PERSON);
        });
    }
    ```

    在我们的测试中，每个操作都尝试将一个新条目插入到我们的数据库中。由于我们尝试插入两个具有相同id的实体，第二次插入操作因抛出PersistenceException而失败。

    如果我们使用Spring Data的@Query，这里的原则是一样的。

5. persist

    在上一个示例中，我们创建了插入查询，但我们必须为每个实体创建字面查询。这种方法效率不高，导致大量模板代码。

    相反，我们可以使用EntityManager的持久方法。

    就像我们之前的例子一样，让我们用自定义方法扩展我们的存储库类：

    ```java
    @Transactional
    public void insertWithEntityManager(Person person) {
        this.entityManager.persist(person);
    }
    ```

    现在，我们可以再次测试我们的方法：

    ```java
    @Test
    public void givenPersonEntity_whenInsertedTwiceWithEntityManager_thenEntityExistsExceptionIsThrown() {
        assertThatExceptionOfType(EntityExistsException.class).isThrownBy(() -> {
            personInsertRepository.insertWithEntityManager(new Person(1L, "firstname", "lastname"));
            personInsertRepository.insertWithEntityManager(new Person(1L, "firstname", "lastname"));
        });
    }
    ```

    与使用本机查询不同，我们不必指定列名和相应的值。相反，EntityManager为我们处理。

    在上述测试中，我们还期望抛出EntityExistsException，而不是其超类PersistenceException，后者更专业化，并由persistent抛出。

    另一方面，在本例中，我们必须确保每次使用新的Person实例调用插入方法。否则，它将由EntityManager管理，导致更新操作。

6. 结论

    在本文中，我们说明了在JPA对象上执行插入操作的方法。我们查看了使用本机查询以及使用EntityManager#persist创建自定义INSERT语句的示例。
