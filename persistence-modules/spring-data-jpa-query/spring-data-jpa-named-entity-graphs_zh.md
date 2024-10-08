# [Spring Data JPA和命名实体图](https://www.baeldung.com/spring-data-jpa-named-entity-graphs)

1. 一览表

    简单地说，[实体图](https://www.baeldung.com/jpa-entity-graph)是JPA 2.1中描述查询的另一种方式。我们可以利用它们来制定性能更好的查询。

    在本教程中，我们将通过一个简单的示例学习如何使用Spring Data JPA实现实体图。

2. 实体

    首先，让我们创建一个名为Item的模型，它具有多个特征：

    ```java
    @Entity
    public class Item {

        @Id
        private Long id;
        private String name;
        
        @OneToMany(mappedBy = "item")
        private List<Characteristic> characteristics = new ArrayList<>();

        // getters and setters
    }
    ```

    现在让我们来定义特征实体：

    ```java
    @Entity
    public class Characteristic {

        @Id
        private Long id;
        private String type;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn
        private Item item;

        //Getters and Setters
    }
    ```

    正如我们在代码中看到的那样，项目实体中的特征字段和特征实体中的项目字段都是使用获取参数惰性加载的。因此，我们在这里的目标是在运行时急切地加载它们。

3. 实体图

    在Spring Data JPA中，我们可以使用@NamedEntityGraph和@EntityGraphannotations的组合来定义实体图。或者，我们也可以仅使用@EntityGraph注释的属性路径参数来定义临时实体图。

    让我们看看如何完成。

    1. 与@NamedEntityGraph

        首先，我们可以直接在我们的项目实体上使用JPA的@NamedEntityGraph注释：

        ```java
        @Entity
        @NamedEntityGraph(name = "Item.characteristics",
            attributeNodes = @NamedAttributeNode("characteristics")
        )
        public class Item {
        //...
        }
        ```

        然后，我们可以将@EntityGraph注释附加到我们的存储库方法之一：

        ```java
        public interface ItemRepository extends JpaRepository<Item, Long> {

            @EntityGraph(value = "Item.characteristics")
            Item findByName(String name);
        }
        ```

        正如代码所示，我们已经将我们之前在项目实体上创建的实体图的名称传递给@EntityGraph注释。当我们调用该方法时，这就是Spring Data将使用的查询。

        @EntityGraph注释的类型参数的默认值是EntityGraphType.FETCH。当我们使用这个时，Spring Data模块将在指定的属性节点上应用FetchType.EAGER策略。对于其他人，将应用FetchType.LAZY策略。

        因此，在我们的案例中，即使@OneToMany注释的默认获取策略是懒惰的，特征属性也会被急切地加载。

        这里的一个陷阱是，如果定义的获取策略是EAGER，那么我们不能将其行为更改为LAZY。这是有意为之的，因为后续操作可能需要在执行过程中的稍后时间点急切地获取数据。

    2. 没有@NamedEntityGraph

        或者，我们也可以使用属性路径定义一个临时实体图。

        让我们在我们的特征存储库中添加一个临时实体图，该图急切地加载其项目父项：

        ```java
        public interface CharacteristicsRepository
        extends JpaRepository<Characteristic, Long> {

            @EntityGraph(attributePaths = {"item"})
            Characteristic findByType(String type);    
        }
        ```

        这将急切地加载特征实体的项目属性，即使我们的实体为此属性声明了惰性加载策略。

        这很方便，因为我们可以内联定义实体图，而不是引用现有的命名实体图。

4. 测试案例

    既然我们已经定义了实体图，让我们创建一个测试用例来验证它：

    ```java
    @DataJpaTest
    @RunWith(SpringRunner.class)
    @Sql(scripts = "/entitygraph-data.sql")
    public class EntityGraphIntegrationTest {

        @Autowired
        private ItemRepository itemRepo;
        
        @Autowired
        private CharacteristicsRepository characteristicsRepo;
        
        @Test
        public void givenEntityGraph_whenCalled_shouldRetrunDefinedFields() {
            Item item = itemRepo.findByName("Table");
            assertThat(item.getId()).isEqualTo(1L);
        }
        
        @Test
        public void givenAdhocEntityGraph_whenCalled_shouldRetrunDefinedFields() {
            Characteristic characteristic = characteristicsRepo.findByType("Rigid");
            assertThat(characteristic.getId()).isEqualTo(1L);
        }
    }
    ```

    第一个测试将使用使用@NamedEntityGraph注释定义的实体图。

    让我们看看Hibernate生成的SQL：

    ```sql
    select
        item0_.id as id1_10_0_,
        characteri1_.id as id1_4_1_,
        item0_.name as name2_10_0_,
        characteri1_.item_id as item_id3_4_1_,
        characteri1_.type as type2_4_1_,
        characteri1_.item_id as item_id3_4_0__,
        characteri1_.id as id1_4_0__
    from
        item item0_
    left outer join
        characteristic characteri1_
    on
        item0_.id=characteri1_.item_id
    where
        item0_.name=?
    ```

    为了比较，让我们从存储库中删除@EntityGraph注释并检查查询：

    ```sql
    select
        item0_.id as id1_10_,
        item0_.name as name2_10_
    from
        item item0_
    where
        item0_.name=?
    ```

    从这些查询中，我们可以清楚地观察到，在没有@EntityGraph注释的情况下生成的查询不会加载特征实体的任何属性。因此，它只加载项目实体。

    最后，让我们将第二个测试的Hibernate查询与@EntityGraph注释进行比较：

    ```sql
    select
        characteri0_.id as id1_4_0_,
        item1_.id as id1_10_1_,
        characteri0_.item_id as item_id3_4_0_,
        characteri0_.type as type2_4_0_,
        item1_.name as name2_10_1_
    from
        characteristic characteri0_
    left outer join
        item item1_
    on
        characteri0_.item_id=item1_.id
    where
        characteri0_.type=?
    ```

    以及没有@EntityGraph注释的查询：

    ```sql
    select
        characteri0_.id as id1_4_,
        characteri0_.item_id as item_id3_4_,
        characteri0_.type as type2_4_
    from
        characteristic characteri0_
    where
        characteri0_.type=?
    ```

5. 结论

    在本教程中，我们学习了如何在Spring Data中使用JPA实体图。使用Spring Data，我们可以创建多个链接到不同实体图的存储库方法。
