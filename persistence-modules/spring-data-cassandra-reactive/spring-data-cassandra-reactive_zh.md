# [带有Reactive Cassandra的Spring Data](https://www.baeldung.com/spring-data-cassandra-reactive)

1. 介绍

    在本教程中，我们将学习如何使用Spring Data Cassandra的无功数据访问功能。

    在这篇文章中，我们将使用REST API公开Cassandra数据库。

2. Maven附属机构

    事实上，Spring Data Cassandra支持Project Reactor和RxJava反应类型。为了演示，我们将在本教程中使用项目反应堆的活性类型通量和单声道。

    首先，让我们添加教程所需的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-cassandra</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-core</artifactId>
    </dependency>
    ```

    现在，我们将通过REST API从数据库中公开SELECT操作。因此，让我们也添加RestController的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ```

3. 实施我们的应用程序

    由于我们将持续数据，让我们先定义我们的实体对象：

    ```java
    @Table
    public class Employee {
        @PrimaryKey
        private int id;
        private String name;
        private String address;
        private String email;
        private int age;
    }
    ```

    接下来，是时候创建一个从ReactiveCassandraRepository扩展的EmployeeRepository了。需要注意的是，此接口支持反应类型：

    ```java
    public interface EmployeeRepository extends ReactiveCassandraRepository<Employee, Integer> {
        @AllowFiltering
        Flux<Employee> findByAgeGreaterThan(int age);
    }
    ```

    1. CRUD操作的休息控制器

        为了说明，我们将使用简单的休息控制器来展示一些基本的SELECT操作：

        ```java
        @RestController
        @RequestMapping("employee")
        public class EmployeeController {

            @Autowired
            EmployeeService employeeService;

            @PostConstruct
            public void saveEmployees() {
                List<Employee> employees = new ArrayList<>();
                employees.add(new Employee(123, "John Doe", "Delaware", "jdoe@xyz.com", 31));
                employees.add(new Employee(324, "Adam Smith", "North Carolina", "asmith@xyz.com", 43));
                employees.add(new Employee(355, "Kevin Dunner", "Virginia", "kdunner@xyz.com", 24));
                employees.add(new Employee(643, "Mike Lauren", "New York", "mlauren@xyz.com", 41));
                employeeService.initializeEmployees(employees);
            }

            @GetMapping("/list")
            public Flux<Employee> getAllEmployees() {
                Flux<Employee> employees = employeeService.getAllEmployees();
                return employees;
            }

            @GetMapping("/{id}")
            public Mono<Employee> getEmployeeById(@PathVariable int id) {
                return employeeService.getEmployeeById(id);
            }

            @GetMapping("/filterByAge/{age}")
            public Flux<Employee> getEmployeesFilterByAge(@PathVariable int age) {
                return employeeService.getEmployeesFilterByAge(age);
            }
        }
        ```

        最后，让我们添加一个简单的员工服务：

        ```java
        @Service
        public class EmployeeService {

            @Autowired
            EmployeeRepository employeeRepository;

            public void initializeEmployees(List<Employee> employees) {
                Flux<Employee> savedEmployees = employeeRepository.saveAll(employees);
                savedEmployees.subscribe();
            }

            public Flux<Employee> getAllEmployees() {
                Flux<Employee> employees =  employeeRepository.findAll();
                return employees;
            }

            public Flux<Employee> getEmployeesFilterByAge(int age) {
                return employeeRepository.findByAgeGreaterThan(age);
            }

            public Mono<Employee> getEmployeeById(int id) {
                return employeeRepository.findById(id);
            }
        }
        ```

    2. 数据库配置

        然后，让我们在application.properties中指定用于与Cassandra连接的键空间和端口：

        ```properties
        spring.data.cassandra.keyspace-name=practice
        spring.data.cassandra.port=9042
        spring.data.cassandra.local-datacenter=datacenter1
        ```

        > 注意：datacenter1是默认的数据中心名称。

4. 测试端点

    最后，是时候测试我们的API端点了。

    1. 手动测试

        首先，让我们从数据库中获取员工记录：

        `curl localhost:8080/employee/list`

        因此，我们得到了所有员工：

        ```json
        [
            {
                "id": 324,
                "name": "Adam Smith",
                "address": "North Carolina",
                "email": "asmith@xyz.com",
                "age": 43
            },
            {
                "id": 123,
                "name": "John Doe",
                "address": "Delaware",
                "email": "jdoe@xyz.com",
                "age": 31
            },
            {
                "id": 355,
                "name": "Kevin Dunner",
                "address": "Virginia",
                "email": "kdunner@xyz.com",
                "age": 24
            },
            {
                "id": 643,
                "name": "Mike Lauren",
                "address": "New York",
                "email": "mlauren@xyz.com",
            "age": 41
            }
        ]
        ```

        继续，让我们尝试通过他的ID找到一个特定的员工：

        `curl localhost:8080/employee/643`

        结果，我们让迈克·劳伦先生回来了：

        ```json
        {
            "id": 643,
            "name": "Mike Lauren",
            "address": "New York",
            "email": "mlauren@xyz.com",
            "age": 41
        }
        ```

        最后，让我们看看我们的年龄过滤器是否有效：

        `curl localhost:8080/employee/filterByAge/35`

        不出所料，我们得到了所有年龄在35岁以上的员工：

        ```json
        [
            {
                "id": 324,
                "name": "Adam Smith",
                "address": "North Carolina",
                "email": "asmith@xyz.com",
                "age": 43
            },
            {
                "id": 643,
                "name": "Mike Lauren",
                "address": "New York",
                "email": "mlauren@xyz.com",
                "age": 41
            }
        ]
        ```

    2. 集成测试

        此外，让我们通过编写测试用例来测试相同的功能：

        ```java
        @RunWith(SpringRunner.class)
        @SpringBootTest
        public class ReactiveEmployeeRepositoryIntegrationTest {

            @Autowired
            EmployeeRepository repository;

            @Before
            public void setUp() {
                Flux<Employee> deleteAndInsert = repository.deleteAll()
                .thenMany(repository.saveAll(Flux.just(
                    new Employee(111, "John Doe", "Delaware", "jdoe@xyz.com", 31),
                    new Employee(222, "Adam Smith", "North Carolina", "asmith@xyz.com", 43),
                    new Employee(333, "Kevin Dunner", "Virginia", "kdunner@xyz.com", 24),
                    new Employee(444, "Mike Lauren", "New York", "mlauren@xyz.com", 41))));

                StepVerifier
                .create(deleteAndInsert)
                .expectNextCount(4)
                .verifyComplete();
            }

            @Test
            public void givenRecordsAreInserted_whenDbIsQueried_thenShouldIncludeNewRecords() {
                Mono<Long> saveAndCount = repository.count()
                .doOnNext(System.out::println)
                .thenMany(repository
                    .saveAll(Flux.just(
                    new Employee(325, "Kim Jones", "Florida", "kjones@xyz.com", 42),
                    new Employee(654, "Tom Moody", "New Hampshire", "tmoody@xyz.com", 44))))
                .last()
                .flatMap(v -> repository.count())
                .doOnNext(System.out::println);

                StepVerifier
                .create(saveAndCount)
                .expectNext(6L)
                .verifyComplete();
            }

            @Test
            public void givenAgeForFilter_whenDbIsQueried_thenShouldReturnFilteredRecords() {
                StepVerifier
                .create(repository.findByAgeGreaterThan(35))
                .expectNextCount(2)
                .verifyComplete();
            }
        }
        ```

5. 结论

    总之，我们学习了如何使用Spring Data Cassandra来构建非阻塞应用程序的无反应类型。
