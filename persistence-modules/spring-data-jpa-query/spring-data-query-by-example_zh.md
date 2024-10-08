# [按示例进行Spring Data JPA查询](https://www.baeldung.com/spring-data-query-by-example)

1. 介绍

    在本教程中，我们将学习如何通过示例API使用[Spring Data Query查询数据](https://docs.spring.io/spring-data/jpa/reference/repositories/query-by-example.html)。

    首先，我们将定义要查询的数据模式。接下来，我们将检查Spring Data中的几个相关类。最后，我们将浏览几个例子。

    让我们开始吧！

2. 测试数据

    我们的测试数据是乘客姓名列表，以及他们占据的座位：

    | First Name | Last Name | Seat Number |
    |------------|-----------|-------------|
    | Jill       | Smith     | 50          |
    | Eve        | Jackson   | 94          |
    | Fred       | Bloggs    | 22          |
    | Ricki      | Bobbie    | 36          |
    | Siya       | Kolisi    | 85          |

3. 域

    让我们创建我们需要的Spring数据存储库，并提供我们的域类和id类型。

    首先，我们将把乘客建模为JPA实体：

    ```java
    @Entity
    class Passenger {

        @Id
        @GeneratedValue
        @Column(nullable = false)
        private Long id;

        @Basic(optional = false)
        @Column(nullable = false)
        private String firstName;

        @Basic(optional = false)
        @Column(nullable = false)
        private String lastName;

        @Basic(optional = false)
        @Column(nullable = false)
        private int seatNumber;

        // constructor, getters etc.
    }
    ```

    我们本可以将其建模为另一种抽象，而不是使用JPA。

4. 通过示例API进行查询

    首先，让我们来看看JpaRepository界面。正如我们所看到的，它扩展了[QueryByExampleExecutorinterface](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/query/QueryByExampleExecutor.html)，以支持示例查询：

    ```java
    public interface JpaRepository<T, ID>
    extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {}
    ```

    此接口引入了我们从Spring Data中熟悉的find（）方法的更多变体。然而，每种方法也接受示例实例：

    ```java
    public interface QueryByExampleExecutor<T> {
        <S extends T> Optional<S> findOne(Example<S> var1);
        <S extends T> Iterable<S> findAll(Example<S> var1);
        <S extends T> Iterable<S> findAll(Example<S> var1, Sort var2);
        <S extends T> Page<S> findAll(Example<S> var1, Pageable var2);
        <S extends T> long count(Example<S> var1);
        <S extends T> boolean exists(Example<S> var1);
    }
    ```

    接下来，示例接口公开访问探针和示例匹配器的方法。

    重要的是要意识到，探针是我们实体的实例：

    ```java
    public interface Example<T> {

        static <T> org.springframework.data.domain.Example<T> of(T probe) {
            return new TypedExample(probe, ExampleMatcher.matching());
        }

        static <T> org.springframework.data.domain.Example<T> of(T probe, ExampleMatcher matcher) {
            return new TypedExample(probe, matcher);
        }

        T getProbe();

        ExampleMatcher getMatcher();

        default Class<T> getProbeType() {
            return ProxyUtils.getUserClass(this.getProbe().getClass());
        }
    }
    ```

    简而言之，我们的探针和ExampleMatcher一起指定了我们的查询。

5. 限制

    像所有事情一样，通过示例API查询有一些限制：

    - 不支持嵌套和分组语句。例如：(firstName = ?0 and lastName = ?1) or seatNumber = ?2
    - 字符串匹配仅包括精确、不区分大小写、开始、结束、包含和正则表示式
    - 除字符串外，所有类型都是完全匹配的

    既然我们对API及其限制有了更多的了解，让我们深入了解一些例子。

6. 实例

    1. 大小写匹配

        让我们从一个简单的例子开始，然后谈谈默认行为：

        ```java
        @Test
        public void givenPassengers_whenFindByExample_thenExpectedReturned() {
            Example<Passenger> example = Example.of(Passenger.from("Fred", "Bloggs", null));

            Optional<Passenger> actual = repository.findOne(example);

            assertTrue(actual.isPresent());
            assertEquals(Passenger.from("Fred", "Bloggs", 22), actual.get());
        }
        ```

        特别是，静态Example.of（）方法使用ExampleMatcher.matching（）构建示例。

        换句话说，将对乘客的所有非空属性进行完全匹配。因此，匹配在字符串属性上区分大小写。

        然而，如果我们能做的就是对所有非空属性进行完全匹配，那就不太有用了。

        这就是ExampleMatcher的用处。通过构建我们自己的ExampleMatcher，我们可以根据自己的需求定制行为。

    2. 不区分大小写匹配

        考虑到这一点，让我们看看另一个例子，这次使用withIgnoreCase（）来实现不区分大小写的匹配：

        ```java
        @Test
        public void givenPassengers_whenFindByExampleCaseInsensitiveMatcher_thenExpectedReturned() {
            ExampleMatcher caseInsensitiveExampleMatcher = ExampleMatcher.matchingAll().withIgnoreCase();
            Example<Passenger> example = Example.of(Passenger.from("fred", "bloggs", null),
            caseInsensitiveExampleMatcher);

            Optional<Passenger> actual = repository.findOne(example);

            assertTrue(actual.isPresent());
            assertEquals(Passenger.from("Fred", "Bloggs", 22), actual.get());
        }
        ```

        在本例中，请注意我们首先调用ExampleMatcher.matchingAll（）；它具有与我们在上一个示例中使用的ExampleMatcher.matching（）相同的行为。

    3. 定制匹配

        我们还可以在每个属性的基础上调整匹配器的行为，并使用ExampleMatcher.matchingAny()匹配任何属性：

        ```java
        @Test
        public void givenPassengers_whenFindByExampleCustomMatcher_thenExpectedReturned() {
            Passenger jill = Passenger.from("Jill", "Smith", 50);
            Passenger eve = Passenger.from("Eve", "Jackson", 95);
            Passenger fred = Passenger.from("Fred", "Bloggs", 22);
            Passenger siya = Passenger.from("Siya", "Kolisi", 85);
            Passenger ricki = Passenger.from("Ricki", "Bobbie", 36);

            ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny()
            .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
            .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

            Example<Passenger> example = Example.of(Passenger.from("e", "s", null), customExampleMatcher);

            List<Passenger> passengers = repository.findAll(example);

            assertThat(passengers, contains(jill, eve, fred, siya));
            assertThat(passengers, not(contains(ricki)));
        }
        ```

    4. 忽略属性

        我们可能也只想查询我们属性的子集。

        我们通过忽略使用ExampleMatcher.ignorePaths(String… paths)的一些属性来实现这一点：

        ```java
        @Test
        public void givenPassengers_whenFindByIgnoringMatcher_thenExpectedReturned() {
            Passenger jill = Passenger.from("Jill", "Smith", 50); 
            Passenger eve = Passenger.from("Eve", "Jackson", 95); 
            Passenger fred = Passenger.from("Fred", "Bloggs", 22);
            Passenger siya = Passenger.from("Siya", "Kolisi", 85);
            Passenger ricki = Passenger.from("Ricki", "Bobbie", 36);

            ExampleMatcher ignoringExampleMatcher = ExampleMatcher.matchingAny()
            .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.startsWith().ignoreCase())
            .withIgnorePaths("firstName", "seatNumber");

            Example<Passenger> example = Example.of(Passenger.from(null, "b", null), ignoringExampleMatcher);

            List<Passenger> passengers = repository.findAll(example);

            assertThat(passengers, contains(fred, ricki));
            assertThat(passengers, not(contains(jill)));
            assertThat(passengers, not(contains(eve))); 
            assertThat(passengers, not(contains(siya))); 
        }
        ```

7. 结论

    在本文中，我们演示了如何使用示例API查询。

    我们学习了如何使用[Example](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Example.html)和[ExampleMatcher](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/ExampleMatcher.html)，以及[QueryByExampleExecutor](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/query/QueryByExampleExecutor.html)接口，使用示例数据实例查询表。
