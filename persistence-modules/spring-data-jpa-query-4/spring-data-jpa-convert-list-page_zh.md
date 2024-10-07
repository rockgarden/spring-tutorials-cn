# [使用Spring Data JPA将列表转换为页面](https://www.baeldung.com/spring-data-jpa-convert-list-page)

1. 一览表

    在本教程中，我们将了解如何使用Spring Data JPA将`List<Object>`转换为`Page<Object>`。在Spring Data JPA应用程序中，通常以可寻页的方式从数据库中检索数据。然而，在某些情况下，我们可能需要将实体列表转换为页面对象，以便在可分页的端点中使用。例如，我们可能想从外部API中检索数据或在内存中处理数据。

    我们将设置一个简单的示例，帮助我们可视化数据流和转换。我们将系统分为RestController、Service和Repository层，并看看如何使用Spring Data JPA提供的分页抽象，将从数据库中检索到的大量数据集转换为更小、更有条理的页面。最后，我们将编写一些测试来观察分页的操作。

2. Spring Data JPA中的关键分页抽象

    让我们简单看一下Spring Data JPA提供的关键抽象，这些抽象用于生成分页数据。

    1. 页面

        Page是Spring Data提供的关键接口之一，以促进分页。它提供了一种以分页格式表示和管理数据库查询返回的大型结果集的方法。

        然后，我们可以使用页面对象向用户显示所需数量的记录，并链接导航到后续页面。

        页面封装了页面内容等细节，以及涉及分页细节的元数据，如页码和页面大小、是否有下一页或上一页、还剩多少元素以及页面和元素的总数。

    2. 可寻页的

        Pageable是分页信息的抽象界面。实现此接口的具体类是PageRequest。它表示分页元数据，如当前页码、每页元素数量和排序标准。这是Spring Data JPA中的一个接口，提供了一种为查询指定分页信息的便捷方式，或者在我们的案例中，将分页信息与内容捆绑在一起，从列表<对象>创建页面<对象>。

    3. 页面Impl

        最后，还有PageImpl类，它提供了Page接口的便捷实现，可用于表示查询结果页面，包括分页元数据。它通常与Spring Data的存储库接口和分页机制一起使用，以可分页的方式检索和操作数据。

        既然我们已经对相关组件有了基本的了解，让我们来设置一个简单的例子。

3. 示例设置

    让我们考虑一个客户信息微服务的简单示例，其REST端点可以根据请求参数获取分页客户数据。我们将首先在POM中设置所需的依赖项。所需的依赖项是spring-boot-starter-data-jpa和spring-boot-starter-web，我们还将添加spring-boot-starter-test用于测试：

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependendencies>
    ```

    接下来。让我们设置[REST控制器](https://www.baeldung.com/spring-controller-vs-restcontroller)。

    1. Customer控制者

        首先，让我们添加带有请求参数的适当方法，这些参数将驱动我们在服务层中的逻辑：

        ```java
        @GetMapping("/api/customers")
        public ResponseEntity<Page<Customer>> getCustomers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

            Page<Customer> customerPage = customerService.getCustomers(page, size);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Page-Number", String.valueOf(customerPage.getNumber()));
            headers.add("X-Page-Size", String.valueOf(customerPage.getSize()));

            return ResponseEntity.ok()
            .headers(headers)
            .body(customerPage);
        }
        ```

        在这里，我们可以看到getCustomers方法期望`Page<Customer>`类型的ResponseEntity。

    2. Customer服务

        接下来，让我们设置我们的服务类，该服务类将与存储库交互，将数据转换为所需的页面，并将其返回给控制器：

        ```java
        public Page<Customer> getCustomers(int page, int size) {
            List<Customer> allCustomers = customerRepository.findAll();
            //... logic to convert the List<Customer> to Page<Customer>
            //... return Page<Customer>
        }
        ```

        在这里，我们省略了细节，只关注该服务调用JPA存储库，并作为`List<Customer>`获取一组潜在的大量客户数据。接下来，让我们来看看如何使用JPA提供的API将此列表转换为`Page<Customer>`的细节。

4. 将`List<Customer>`转换为`Page<Customer>`

    现在，让我们提供客户服务如何将从CustomerRepository收到的`List<Customer>`转换为Page对象的详细信息。基本上，在从数据库中检索所有客户的列表后，我们想使用PageRequest工厂方法创建一个Pageable对象：

    ```java
    private Pageable createPageRequestUsing(int page, int size) {
        return PageRequest.of(page, size);
    }
    ```

    请注意，这些页面和大小参数是作为请求参数从我们的CustomerRestController传递给客户服务的。

    然后，我们将把客户的大列表拆分为一个子列表。我们需要知道开始和结束索引，我们可以据此创建一个子列表。这可以使用Pageable对象的getOffset（）和getPageSize（）方法来计算：

    `int start = (int) pageRequest.getOffset();`

    接下来，让我们来获取结束索引：

    `int end = Math.min((start + pageRequest.getPageSize()), allCustomers.size());`

    此子列表将构成我们Page对象的内容：

    `List<Customer> pageContent = allCustomers.subList(start, end);`

    最后，我们将创建一个PageImpl的实例。它将封装页面内容以及页面请求以及`List<Customer>`的总大小：

    `new PageImpl<>(pageContent, pageRequest, allCustomers.size());`

    让我们把所有碎片放在一个地方：

    ```java
    public Page<Customer> getCustomers(int page, int size) {

        Pageable pageRequest = createPageRequestUsing(page, size);

        List<Customer> allCustomers = customerRepository.findAll();
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), allCustomers.size());

        List<Customer> pageContent = allCustomers.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, allCustomers.size());
    }
    ```

5. 将`Page<Customer>`转换为`List<Customer>`

    分页是处理大结果集时需要考虑的一个重要特征。它可以通过将数据分解为称为页面的更小部分来显著增强数据组织。

    春季数据提供了一种支持分页的便捷方式。要在查询方法中添加分页，我们需要更改签名以接受可寻页对象作为参数，并返回`Page<T>`而不是`List<T>`。

    通常，返回的页面对象表示结果的一部分。它包含有关元素列表、所有元素数量和页数的信息。

    例如，让我们看看我们如何获取给定页面的客户列表：

    ```java
    public List<Customer> getCustomerListFromPage(int page, int size) {
        Pageable pageRequest = createPageRequestUsing(page, size);
        Page<Customer> allCustomers = customerRepository.findAll(pageRequest);

        return allCustomers.hasContent() ? allCustomers.getContent() : Collections.emptyList();
    }
    ```

    正如我们所看到的，我们使用相同的createPageRequestUsing（）工厂方法来创建Pageable对象。然后，我们调用getContent（）方法来获取返回的页面作为客户列表。

    请注意，在调用getContent（）之前，我们使用hasContent（）方法来检查页面是否有内容。

6. 测试服务

    让我们写一个快速测试，看看列表<客户>是否被拆分为页面<客户>，并且页面大小和页码是否正确。我们将模拟customerRepository.findAll（）方法，以返回大小为20的客户列表。

    在设置中，当findAll（）被调用时，我们只需提供此列表：

    ```java
    @BeforeEach
    void setup() {
        when(customerRepository.findAll()).thenReturn(ALL_CUSTOMERS);
    }
    ```

    在这里，我们正在构建一个参数化测试，并断言内容、内容大小、总元素和总页面：

    ```java
    @ParameterizedTest
    @MethodSource("testIO")
    void givenAListOfCustomers_whenGetCustomers_thenReturnsDesiredDataAlongWithPagingInformation(int page, int size, List<String> expectedNames, long expectedTotalElements, long expectedTotalPages) {
        Page<Customer> customers = customerService.getCustomers(page, size);
        List<String> names = customers.getContent()
        .stream()
        .map(Customer::getName)
        .collect(Collectors.toList());

        assertEquals(expectedNames.size(), names.size());
        assertEquals(expectedNames, names);
        assertEquals(expectedTotalElements, customers.getTotalElements());
        assertEquals(expectedTotalPages, customers.getTotalPages());}
    ```

    最后，此参数化测试的测试数据输入和输出是：

    ```java
    private static Collection<Object[]> testIO() {
        return Arrays.asList(
        new Object[][] {
            { 0, 5, PAGE_1_CONTENTS, 20L, 4L },
            { 1, 5, PAGE_2_CONTENTS, 20L, 4L },
            { 2, 5, PAGE_3_CONTENTS, 20L, 4L },
            { 3, 5, PAGE_4_CONTENTS, 20L, 4L },
            { 4, 5, EMPTY_PAGE, 20L, 4L } }
        );
    }
    ```

    每个测试都以不同的页面大小（0,1,2,3,4）运行服务方法，每个测试预计将有5个元素。我们预计总页数为4页，因为原始列表的总大小为20页。最后，每页预计包含5个元素。

    现在，我们将添加将页面<客户>转换为列表<客户>的测试用例。首先，让我们测试页面对象不是空的场景：

    ```java
    @Test
    void givenAPageOfCustomers_whenGetCustomerList_thenReturnsList() {
        Page<Customer> pagedResponse = new PageImpl<Customer>(ALL_CUSTOMERS.subList(0, 5));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(pagedResponse);

        List<Customer> customers = customerService.getCustomerListFromPage(0, 5);
        List<String> customerNames = customers.stream()
        .map(Customer::getName)
        .collect(Collectors.toList());

        assertEquals(PAGE_1_CONTENTS.size(), customers.size());
        assertEquals(PAGE_1_CONTENTS, customerNames);
    }
    ```

    在这里，我们模拟了customerRepository.findAll（pageRequest）方法的调用，以返回包含ALL_CUSTOMERS列表一部分的页面对象。因此，返回的客户列表与给定页面对象中的包装列表相同。

    接下来，让我们看看如果customerRepository.findAll（pageRequest）返回一个空页面会发生什么：

    ```java
    @Test
    void givenAnEmptyPageOfCustomers_whenGetCustomerList_thenReturnsEmptyList() {
        Page<Customer> emptyPage = Page.empty();
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        List<Customer> customers = customerService.getCustomerListFromPage(0, 5);

        assertThat(customers).isEmpty();
    }
    ```

    毫不奇怪，返回的列表是空的。

7. 测试控制器

    最后，让我们测试控制器，以确保我们将`ResponseEntity<Page<Customer>>`恢复为JSON。我们将使用MockMVC向GET端点发送请求，并期望使用预期参数进行寻呼响应：

    ```java
    @Test
    void givenTotalCustomers20_whenGetRequestWithPageAndSize_thenPagedReponseIsReturnedFromDesiredPageAndSize() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/customers?page=1&size=5"))
        .andExpect(status().isOk())
        .andReturn();

        MockHttpServletResponse response = result.getResponse();

        JSONObject jsonObject = new JSONObject(response.getContentAsString());
        assertThat(jsonObject.get("totalPages")).isEqualTo(4);
        assertThat(jsonObject.get("totalElements")).isEqualTo(20);
        assertThat(jsonObject.get("number")).isEqualTo(1);
        assertThat(jsonObject.get("size")).isEqualTo(5);
        assertThat(jsonObject.get("content")).isNotNull();}
    ```

    从本质上讲，我们正在使用MockMvc实例来模拟对/api/customers端点的HTTP GET请求。我们提供查询参数页面=1和大小=5。然后，我们期望有一个成功的响应和一个包含页面元数据和内容的正文。

    最后，让我们快速看看将`List<Customer>`转换为`Page<Customer>`如何有利于API设计和消费。

8. 使用`Page<Customer>`而不是`List<Customer>`的好处

    在我们的示例中，选择在整个`List<Customer>`上返回`Page<Customer>`作为API响应，根据用例可能会有一些好处。好处之一是优化网络流量和处理。基本上，如果底层数据源返回大量客户列表，将其转换为`Page<Customer>`允许客户端仅请求特定页面的结果，而不是整个列表。这可以简化客户端的处理，并减少网络负载。

    此外，通过返回`Page<Customer>`，API提供了客户端可以轻松理解和使用的标准化响应格式。`Page<Customer>`对象包含请求页面的客户列表和元数据，如页面总数和每页项目数。

    最后，将对象列表转换为页面为API设计提供了灵活性。例如，API可以允许客户端按不同的字段对结果进行排序。我们还可以根据标准过滤结果，甚至为每个客户返回一个字段子集。

9. 结论

    在本教程中，我们使用Spring Data JPA来处理`List<Object>`和`Page<Object>`之间的转换。我们使用Spring Data JPA提供的API，包括Page、Pageable和PageImpl类。最后，我们简要地研究了使用`Page<Object>`而不是`List<Object>`的一些好处。

    总之，在REST端点中将`List<Object>`转换为`Page<Object>`提供了一种更高效、标准化和灵活的方式来处理大型数据集并在API中实现分页。
