# [在 Spring Boot 测试中使用 @Autowired 和 @InjectMocks](https://www.baeldung.com/spring-test-autowired-injectmocks)

1. 概述

    在本教程中，我们将探讨 Spring Boot 的 @Autowired 和 Mockito 的 [@InjectMocks](https://www.baeldung.com/mockito-annotations#injectmocks-annotation) 在 Spring Boot 测试中注入依赖关系时的用法。我们将了解需要使用它们的用例，并查看相关示例。

2. 了解测试注解

    在开始代码示例之前，让我们快速了解一些测试注解的基础知识。

    首先，Mockito 最常用的 [@Mock](https://www.baeldung.com/mockito-annotations#mock-annotation) 注解是为测试创建一个依赖关系的模拟实例。它通常与 [@InjectMocks](https://www.baeldung.com/mockito-annotations#injectmocks-annotation) 结合使用，后者会将标有 @Mock 的模拟注入到被测试的目标对象中。

    除了 Mockito 的注解外，Spring Boot 的注解 [@MockBean](https://www.baeldung.com/java-spring-mockito-mock-mockbean#spring-boots-mockbean-annotation) 也有助于创建一个模拟 Spring Bean。然后，上下文中的其他 Bean 就可以使用被模拟的 Bean。此外，如果 Spring 上下文自行创建了无需模拟即可使用的 Bean，我们可以使用 @Autowired 注解来注入它们。

3. 示例设置

    在我们的代码示例中，我们将创建一个有两个依赖项的服务。然后，我们将探索如何使用上述注解来测试该服务。

    1. 依赖关系

        让我们先添加所需的依赖项。我们将包含 Spring Boot Starter Web 和 Spring Boot Starter Test 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.3.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>3.3.2</version>
            <scope>test</scope>
        </dependency>
        ```

        除此之外，我们还将添加 Mockito Core 依赖项，以模拟我们的服务：

        ```xml
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.12.0</version>
        </dependency>
        ```

    2. DTO

        接下来，让我们创建一个将在服务中使用的 [DTO](https://www.baeldung.com/java-dto-pattern)：

        ```java
        public class Book {
            private String id;
            private String name;
            private String author;

            // constructor, setters/getters
        }
        ```

    3. 服务

        接下来，让我们看看我们的服务。首先，让我们定义一个负责数据库交互的服务：

        ```java
        @Service
        public class DatabaseService {
            public Book findById(String id) {
                // querying a Database and getting a book
                return new Book("id","Name", "Author");
            }
        }
        ```

        由于数据库交互与示例无关，我们将不再赘述。我们使用 @Service 注解将该类声明为服务定型的 Spring Bean。

        接下来，让我们引入一个依赖于上述服务的服务：

        ```java
        @Service
        public class BookService {
            private DatabaseService databaseService;
            private ObjectMapper objectMapper;

            BookService(DatabaseService databaseService, ObjectMapper objectMapper) {
                this.databaseService = databaseService;
                this.objectMapper = objectMapper;
            }

            String getBook(String id) throws JsonProcessingException {
                Book book = databaseService.findById(id);
                return objectMapper.writeValueAsString(book);
            }
        }
        ```

        这里我们有一个小型服务，它有一个 getBook() 方法。该方法利用 DatabaseService 从数据库中获取图书。然后，它使用 Jackson 的 [ObjectMapper API](https://www.baeldung.com/jackson-object-mapper-tutorial) 将图书对象转换为 JSON 字符串并返回。

        因此，该服务有两个依赖项： DatabaseService 和 ObjectMapper。

4. 测试

    现在我们的服务已经建立，让我们来看看如何使用前面定义的注解测试 BookService。

    1. 使用 @Mock 和 @InjectMocks

        第一种方法是使用 @Mock 模拟服务的两个依赖项，然后使用 @InjectMocks 将它们注入到服务中。让我们为此创建一个测试类：

        ```java
        @ExtendWith(MockitoExtension.class)
        class BookServiceMockAndInjectMocksUnitTest {
            @Mock
            private DatabaseService databaseService;

            @Mock
            private ObjectMapper objectMapper;

            @InjectMocks
            private BookService bookService;

            @Test
            void givenBookService_whenGettingBook_thenBookIsCorrect() throws JsonProcessingException {
                Book book1 = new Book("1234", "Inferno", "Dan Brown");
                when(databaseService.findById(eq("1234"))).thenReturn(book1);

                when(objectMapper.writeValueAsString(any())).thenReturn(new ObjectMapper().writeValueAsString(book1));

                String bookString1 = bookService.getBook("1234");
                Assertions.assertTrue(bookString1.contains("Dan Brown"));
            }
        }
        ```

        首先，我们用 @ExtendWith(MockitoExtension.class)注释测试类。MockitoExtenstion 扩展允许我们在测试中模拟和注入对象。

        接下来，我们声明 DatabaseService 和 ObjectMapper 字段，并用 @Mock 对其进行注解。这样就为它们创建了模拟对象。在声明我们要测试的 BookService 实例时，我们添加了 @InjectMocks 注解。这将注入服务所需的任何依赖项，而这些依赖项之前已用 @Mocks 声明过。

        最后，在我们的测试中，我们将模拟被模拟对象的行为，并测试服务的 getBook() 方法。

        使用该方法时，必须模拟服务的所有依赖关系。例如，如果我们不模拟 ObjectMapper，那么在测试方法中调用 ObjectMapper 时就会导致 NullPointerException 异常。

    2. 使用 @Autowired 和 @MockBean

        在上述方法中，我们模拟了两个依赖项。但是，我们可能需要模拟某些依赖项，而不模拟其他依赖项。假设我们不需要模拟 ObjectMapper 的行为，而只需要模拟 DatabaseService。

        由于我们要在测试中加载 Spring 上下文，因此可以使用 @Autowired 和 @MockBean 注解的组合来实现这一目的：

        ```java
        @SpringBootTest
        class BookServiceAutowiredAndInjectMocksUnitTest {
            @MockBean
            private DatabaseService databaseService;

            @Autowired
            private BookService bookService;

            @Test
            void givenBookService_whenGettingBook_thenBookIsCorrect() throws JsonProcessingException {
                Book book1 = new Book("1234", "Inferno", "Dan Brown");
                when(databaseService.findById(eq("1234"))).thenReturn(book1);

                String bookString1 = bookService.getBook("1234");
                Assertions.assertTrue(bookString1.contains("Dan Brown"));
            }
        }
        ```

        首先，要使用 Spring 上下文中的 Bean，我们需要用 @SpringBootTest 来注解我们的测试类，然后用 @MockBean 来注解 DatabaseService。然后，我们使用 @Autowired 从应用程序上下文中获取 BookService 实例。

        当注入 BookService Bean 时，实际的 DatabaseService Bean 将被模拟的 Bean 所取代。相反，ObjectMapper Bean 将保持应用程序最初创建时的状态。

        现在测试该实例时，我们不需要模拟 ObjectMapper 的任何行为。

        当我们需要测试嵌套 Bean 的行为，而又不想模拟每个依赖关系时，这种方法非常有用。

    3. 同时使用 @Autowired 和 @InjectMocks

        在上述用例中，我们还可以使用 @InjectMocks 代替 @MockBean。

        让我们看看代码，看看这两种方法的区别：

        ```java
        @Mock
        private DatabaseService databaseService;

        @Autowired
        @InjectMocks
        private BookService bookService;

        @Test
        void givenBookService_whenGettingBook_thenBookIsCorrect() throws JsonProcessingException {
            Book book1 = new Book("1234", "Inferno", "Dan Brown");

            MockitoAnnotations.openMocks(this);

            when(databaseService.findById(eq("1234"))).thenReturn(book1);
            String bookString1 = bookService.getBook("1234");
            Assertions.assertTrue(bookString1.contains("Dan Brown"));
        }
        ```

        在这里，我们使用 @Mock 而不是 @MockBean 来模拟 DatabaseService。除了 @Autowired 之外，我们还为 BookService 实例添加了 @InjectMocks 注解。

        同时使用这两个注解时，@InjectMocks 不会自动注入被模拟的依赖关系，而自动连线的 BookService 对象会在测试开始时注入。

        不过，我们可以在稍后的测试中通过调用 MockitoAnnotations.openMocks() 方法注入 DatabaseService 的模拟实例。该方法会查找标记有 @InjectMocks 的字段，并将模拟对象注入其中。

        我们在测试中需要模拟 DatabaseService 的行为之前调用该方法。当我们想动态决定何时使用模拟对象，何时使用实际 bean 作为依赖时，该方法就非常有用了。

5. 方法比较

    既然我们已经研究了多种方法，那么让我们总结一下这些方法之间的比较：

    | 方法                          | 说明                                                                           | 使用方法                                                                            |
    |-----------------------------|------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
    | @Mock with @InjectMocks     | 使用 Mockito 的 @Mock 注解创建依赖关系的模拟实例，并使用 @InjectMocks 将这些模拟注入被测试的目标对象。           | 适用于单元测试，我们希望模拟被测类的所有依赖关系。                                                       |
    | @MockBeanwith @Autowired    | 利用 Spring Boot 的 @MockBean 注解创建模拟 Spring Bean，并利用 @Autowired 将这些 Bean 注入。    | Spring Boot 应用程序集成测试的理想选择。它允许模拟一些 Spring Bean，同时从 Spring 的依赖注入中获取其他 Bean。       |
    | @InjectMockswith @Autowired | 使用 Mockito 的 @Mock 注解创建模拟实例，并使用 @InjectMocks 将这些模拟注入已使用 Spring 自动布线的目标 Bean。 | 在我们需要使用 Mockito 临时模拟某些依赖关系以覆盖注入的 Spring Bean 时，它提供了灵活性。有助于测试 Spring 应用程序中的复杂场景。 |

6. 总结

    在本文中，我们探讨了 Mockito 和 Spring Boot 注释的不同用例 - @Mock、@InjectMocks、@Autowired 和 @MockBean。我们探讨了根据测试需要使用注解的不同组合的时机。
