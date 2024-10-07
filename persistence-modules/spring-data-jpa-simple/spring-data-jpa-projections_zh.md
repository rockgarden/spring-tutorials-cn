# [Spring Data JPA预测](https://www.baeldung.com/spring-data-jpa-projections)

1. 一览表

    使用Spring Data JPA实现持久性层时，存储库通常会返回一个或多个根类的实例。然而，通常我们不需要返回对象的所有属性。

    在这种情况下，我们可能希望将数据作为自定义类型的对象检索。这些类型反映了根类的部分视图，只包含我们关心的属性。这就是投影派上用场的地方。

2. 初始设置

    第一步是设置项目并填充数据库。

    1. Maven附属机构

        有关依赖项，请查看[本教程的第2部分](https://www.baeldung.com/spring-data-case-insensitive-queries)。

    2. 实体类

        让我们定义两个实体类：

        ```java
        @Entity
        public class Address {

            @Id
            private Long id;
        
            @OneToOne
            private Person person;
        
            private String state;
        
            private String city;
        
            private String street;
        
            private String zipCode;

            // getters and setters
        }
        ```

        并且：

        ```java
        @Entity
        public class Person {

            @Id
            private Long id;
        
            private String firstName;
        
            private String lastName;
        
            @OneToOne(mappedBy = "person")
            private Address address;

            // getters and setters
        }
        ```

        人员和地址实体之间的关系是双向一对一的；地址是拥有方，人是反向方。

        请注意，在本教程中，我们使用嵌入式数据库H2。

        配置嵌入式数据库时，Spring Boot会自动为我们定义的实体生成底层表。

    3. SQL脚本

        我们将使用projection-insert-data.sql脚本来填充两个备份表：

        ```sql
        INSERT INTO person(id,first_name,last_name) VALUES (1,'John','Doe');
        INSERT INTO address(id,person_id,state,city,street,zip_code)
        VALUES (1,1,'CA', 'Los Angeles', 'Standford Ave', '90001');
        ```

        为了在每次测试运行后清理数据库，我们可以使用另一个脚本，projection-clean-up-data.sql：

        ```sql
        DELETE FROM address;
        DELETE FROM person;
        ```

    4. 测试类

        然后，为了确认预测产生正确的数据，我们需要一个测试类：

        ```java
        @DataJpaTest
        @RunWith(SpringRunner.class)
        @Sql(scripts = "/projection-insert-data.sql")
        @Sql(scripts = "/projection-clean-up-data.sql", executionPhase = AFTER_TEST_METHOD)
        public class JpaProjectionIntegrationTest {
            // injected fields and test methods
        }
        ```

        使用给定的注释，Spring Boot创建数据库，注入依赖项，并在每个测试方法执行前后填充和清理表。

3. 基于接口的投影

    在投影实体时，依赖接口是很自然的，因为我们不需要提供实现。

    1. 封闭投影

        回顾地址类，我们可以看到它有很多属性，但并非所有属性都有帮助。例如，有时邮政编码足以表示地址。

        让我们为地址类声明一个投影接口：

        ```java
        public interface AddressView {
            String getZipCode();
        }
        ```

        然后，我们将在存储库界面中使用它：

        ```java
        public interface AddressRepository extends Repository<Address, Long> {
            List<AddressView> getAddressByState(String state);
        }
        ```

        不难看出，使用投影接口定义存储库方法与实体类定义几乎相同。

        唯一的区别是，投影接口，而不是实体类，被用作返回集合中的元素类型。

        让我们快速测试一下地址投影：

        ```java
        @Autowired
        private AddressRepository addressRepository;

        @Test
        public void whenUsingClosedProjections_thenViewWithRequiredPropertiesIsReturned() {
            AddressView addressView = addressRepository.getAddressByState("CA").get(0);
            assertThat(addressView.getZipCode()).isEqualTo("90001");
            // ...
        }
        ```

        在幕后，Spring为每个实体对象创建投影接口的代理实例，所有对代理的调用都转发到该对象。

        我们可以递归地使用投影。例如，这是Person类的投影接口：

        ```java
        public interface PersonView {
            String getFirstName();

            String getLastName();
        }
        ```

        现在，我们将在地址投影中添加一个返回类型为PersonView的方法，即嵌套投影：

        ```java
        public interface AddressView {
            // ...
            PersonView getPerson();
        }
        ```

        请注意，返回嵌套投影的方法必须与返回相关实体的根类中的方法具有相同的名称。

        我们将通过在我们刚刚编写的测试方法中添加一些语句来验证嵌套投影：

        ```java
        // ...
        PersonView personView = addressView.getPerson();
        assertThat(personView.getFirstName()).isEqualTo("John");
        assertThat(personView.getLastName()).isEqualTo("Doe");
        ```

        请注意，递归投影只有在我们从拥有侧穿越到反向侧时才有效。如果我们反过来做，嵌套投影将设置为空。

    2. 开放投影

        到此为时，我们已经经历了封闭式投影，这些投影表示其方法与实体属性名称完全匹配的投影接口。

        还有另一种基于接口的投影，即开放投影。这些投影使我们能够定义具有不匹配名称和运行时计算的返回值的接口方法。

        让我们回到人物投影界面，并添加一种新的方法：

        ```java
        public interface PersonView {
            // ...

            @Value("#{target.firstName + ' ' + target.lastName}")
            String getFullName();
        }
        ```

        @Value注释的参数是一个SpEL表达式，其中目标指定符表示支持实体对象。

        现在我们将定义另一个存储库接口：

        ```java
        public interface PersonRepository extends Repository<Person, Long> {
            PersonView findByLastName(String lastName);
        }
        ```

        为了简单起以简单，我们将只返回单个投影对象，而不是一个集合。

        该测试证实了开放式投影的工作符合预期：

        ```java
        @Autowired
        private PersonRepository personRepository;

        @Test
        public void whenUsingOpenProjections_thenViewWithRequiredPropertiesIsReturned() {
            PersonView personView = personRepository.findByLastName("Doe");

            assertThat(personView.getFullName()).isEqualTo("John Doe");
        }
        ```

        不过，开放预测确实有一个缺点；Spring Data无法优化查询执行，因为它事先不知道将使用哪些属性。因此，只有当封闭投影无法满足我们的要求时，我们才应该使用开放投影。

4. 基于类的预测

    我们可以定义自己的投影类，而不是使用Spring Data从投影接口创建的代理。

    例如，以下是Person实体的投影类：

    ```java
    public class PersonDto {
        private String firstName;
        private String lastName;

        public PersonDto(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        // getters, equals and hashCode
    }
    ```

    要使投影类与存储库接口协同工作，其构造函数的参数名称必须与根实体类的属性匹配。

    我们还必须定义等值和哈希代码实现；它们允许Spring Data处理集合中的投影对象。

    上述要求可以通过java记录来解决，从而使我们的代码更加精确和富有表现力：

    `public record PersonDto(String firstName, String lastName) {}`

    现在让我们向Person存储库添加一个方法：

    ```java
    public interface PersonRepository extends Repository<Person, Long> {
        // ...
        PersonDto findByFirstName(String firstName);
    }
    ```

    该测试验证了我们基于类的预测：

    ```java
    @Test
    public void whenUsingClassBasedProjections_thenDtoWithRequiredPropertiesIsReturned() {
        PersonDto personDto = personRepository.findByFirstName("John");
        assertThat(personDto.getFirstName()).isEqualTo("John");
        assertThat(personDto.getLastName()).isEqualTo("Doe");
    }
    ```

    请注意，使用基于类的方法，我们不能使用嵌套投影。

    1. 使用JPA原生查询

        JPA原生查询允许我们直接编写SQL查询，而不是使用JPQL。我们还可以将本机查询的结果映射到DTO类。

        @NamedNativeQuery用于使用设置为映射的DTO注释的resultSetMapping参数定义SQL查询。@SqlResultSetMapping注释用于定义查询结果与DTO类的映射：

        ```java
        @Entity
        @NamedNativeQuery(
        name = "person_native_query_dto",
        query = "SELECT p.first_name, p.last_name From Person p where p.first_name LIKE :firstNameLike",
        resultSetMapping = "person_query_dto"
        )
        @SqlResultSetMapping(
        name = "person_query_dto",
        classes = @ConstructorResult(
            targetClass = PersonDto.class,
            columns = {
                @ColumnResult(name = "first_name", type = String.class),
                @ColumnResult(name = "last_name", type = String.class),
            }
        )
        )
        public class Person {
        // properties, getters and setters
        }
        ```

        在上述逻辑中，我们将本机查询的结果映射到PersonDto类，其中我们定义映射到PersonDto类上的属性的列。

        一旦我们对实体进行注释，我们就可以定义存储库方法：

        ```java
        public interface PersonRepository extends Repository<Person, Long> {
            @Query(name = "person_native_query_dto", nativeQuery = true)
            List<PersonDto> findByFirstNameLike(@Param("firstNameLike") String firstNameLike);
        }
        ```

        在findByFirstNameLike（）方法中，我们用@Query进行注释，它接收我们在实体上定义的本机查询的名称。

        我们可以编写一个简单的单元测试来验证结果：

        ```java
        @Test
        void whenUsingClassBasedProjectionsAndJPANativeQuery_thenDtoWithRequiredPropertiesIsReturned() {
            List<PersonDto> personDtos = personRepository.findByFirstNameLike("Jo%");
            assertThat(personDtos.size()).isEqualTo(2);
            assertThat(personDtos).isEqualTo(Arrays.asList(new PersonDto("John", "Doe"), new PersonDto("Job", "Doe")));
        }
        ```

5. 动态投影

    实体类可能有许多预测。在某些情况下，我们可能会使用某种类型，但在其他情况下，我们可能需要另一种类型。有时，我们还需要使用实体类本身。

    定义单独的存储库接口或方法只是为了支持多种返回类型是繁琐的。为了解决这个问题，Spring Data提供了一个更好的解决方案，即动态投影。

    我们只需声明带有类参数的存储库方法即可应用动态投影：

    ```java
    public interface PersonRepository extends Repository<Person, Long> {
        // ...
        <T> T findByLastName(String lastName, Class<T> type);
    }
    ```

    通过将投影类型或实体类传递给这样的方法，我们可以检索所需类型的对象：

    ```java
    @Test
    public void whenUsingDynamicProjections_thenObjectWithRequiredPropertiesIsReturned() {
        Person person = personRepository.findByLastName("Doe", Person.class);
        PersonView personView = personRepository.findByLastName("Doe", PersonView.class);
        PersonDto personDto = personRepository.findByLastName("Doe", PersonDto.class);

        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(personView.getFirstName()).isEqualTo("John");
        assertThat(personDto.getFirstName()).isEqualTo("John");
    }
    ```

6. 结论

    在本文中，我们讨论了各种类型的Spring Data JPA预测。
