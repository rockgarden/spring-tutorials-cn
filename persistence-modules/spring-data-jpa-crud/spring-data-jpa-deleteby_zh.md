# [Spring Data JPA – 派生删除方法](https://www.baeldung.com/spring-data-jpa-deleteby)

1. 介绍

    Spring Data JPA允许我们定义从数据库中读取、更新或删除记录的派生方法。这非常有用，因为它减少了数据访问层的模板代码。

    在本教程中，我们将重点通过实际代码示例来定义和使用Spring Data衍生的删除方法。

2. 派生删除方法

    首先，让我们树立榜样。我们将定义一个水果实体，以保存水果商店中可用物品的名称和颜色：

    ```java
    @Entity
    public class Fruit {
        @Id
        private long id;
        private String name;
        private String color;
        // standard getters and setters
    }
    ```

    接下来，我们将通过扩展JpaRepository接口并将我们的派生方法添加到该类中，添加我们的存储库以在Fruit实体上操作。

    派生方法可以定义为在实体中定义的VERB +属性。几个允许的动词是findBy、deleteBy和removeBy。

    让我们得出一个方法，通过水果的名字删除它们：

    ```java
    @Repository
    public interface FruitRepository extends JpaRepository<Fruit, Long> {
        Long deleteByName(String name);
    }
    ```

    在本例中，deleteByName方法返回已删除记录的计数。

    同样，我们也可以推导出一种形式的删除方法：

    `List<Fruit> deleteByColor(String color);`

    在这里，deleteByColor方法删除所有具有给定颜色的水果，并返回已删除记录的列表。

    让我们来测试一下派生的删除方法。首先，我们将通过在test-fruit-data.sql中定义数据，在Fruit表中插入一些记录：

    ```java
    insert into fruit(id,name,color) values (1,'apple','red');
    insert into fruit(id,name,color) values (2,'custard apple','green');
    insert into fruit(id,name,color) values (3,'mango','yellow');
    insert into fruit(id,name,color) values (4,'guava','green');
    ```

    然后，我们将删除所有“green”水果：

    ```java
    @Transactional
    @Test
    @Sql(scripts = { "/test-fruit-data.sql" })
    public void givenFruits_WhenDeletedByColor_ThenDeletedFruitsShouldReturn() {
        List<Fruit> fruits = fruitRepository.deleteByColor("green");

        assertEquals("number of fruits are not matching", 2, fruits.size());
        fruits.forEach(fruit -> assertEquals("It's not a green fruit", "green", fruit.getColor()));
    }
    ```

    此外，请注意，我们需要使用@Transactional注释来删除方法。

    接下来，让我们为第二个deleteBy方法添加一个类似的测试用例：

    ```java
    @Transactional
    @Test
    @Sql(scripts = { "/test-fruit-data.sql" })
    public void givenFruits_WhenDeletedByName_ThenDeletedFruitCountShouldReturn() {

        Long deletedFruitCount = fruitRepository.deleteByName("apple");

        assertEquals("deleted fruit count is not matching", 1, deletedFruitCount.intValue());
    }
    ```

3. 衍生的移除方法

    我们还可以使用removeBy动词来推导删除方法：

    ```java
    Long removeByName(String name);
    List<Fruit> removeByColor(String color);
    ```

    请注意，这两种方法的行为没有区别。

    最终的界面将如下：

    ```java
    @Repository
    public interface FruitRepository extends JpaRepository<Fruit, Long> {

        Long deleteByName(String name);

        List<Fruit> deleteByColor(String color);

        Long removeByName(String name);

        List<Fruit> removeByColor(String color);
    }
    ```

    让我们为removeBy方法添加类似的单元测试：

    ```java
    @Transactional
    @Test
    @Sql(scripts = { "/test-fruit-data.sql" })
    public void givenFruits_WhenRemovedByColor_ThenDeletedFruitsShouldReturn() {
        List<Fruit> fruits = fruitRepository.removeByColor("green");

        assertEquals("number of fruits are not matching", 2, fruits.size());
    }

    @Transactional
    @Test
    @Sql(scripts = { "/test-fruit-data.sql" })
    public void givenFruits_WhenRemovedByName_ThenDeletedFruitCountShouldReturn() {
        Long deletedFruitCount = fruitRepository.removeByName("apple");

        assertEquals("deleted fruit count is not matching", 1, deletedFruitCount.intValue());
    }
    ```

4. 派生删除方法与@Query

    我们可能会遇到一种场景，这种场景使派生方法的名称太大，或者涉及不相关实体之间的SQL JOIN。

    在这种情况下，我们还可以使用@Query和@Modifying注释来实现删除操作。

    让我们看看我们派生删除方法的等效代码，使用自定义查询：

    ```java
    @Modifying
    @Query("delete from Fruit f where f.name=:name or f.color=:color")
    int deleteFruits(@Param("name") String name, @Param("color") String color);
    ```

    虽然这两种解决方案似乎相似，并且它们确实实现了相同的结果，但它们采取了略有不同的方法。@Query方法针对数据库创建单个JPQL查询。相比之下，deleteBy方法执行读取查询，然后逐一删除每个项目。

    此外，deleteBy方法可以返回已删除记录的列表，而自定义查询将返回已删除的记录数量。

5. 结论

    在本文中，我们重点介绍了衍生的Spring数据衍生删除方法。
