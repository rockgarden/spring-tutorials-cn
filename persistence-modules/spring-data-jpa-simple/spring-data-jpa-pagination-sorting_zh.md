# [使用Spring Data JPA进行分页和排序](https://www.baeldung.com/spring-data-jpa-pagination-sorting)

1. 一览表

    当我们拥有大型数据集，并希望将其以较小块的形式呈现给用户时，分页通常很有帮助。

    此外，在分页时，我们通常需要根据一些标准对数据进行排序。

    在本教程中，我们将学习如何使用Spring Data JPA轻松分页和排序。

2. 初始设置

    首先，假设我们有一个产品实体作为我们的域类：

    ```java
    @Entity
    public class Product {
        
        @Id
        private long id;
        private String name;
        private double price; 

        // constructors, getters and setters 

    }
    ```

    我们的每个产品实例都有一个唯一的标识符：id、其名称和与之相关的价格。

3. 创建存储库

    要访问我们的产品，我们需要一个产品存储库：

    ```java
    public interface ProductRepository extends PagingAndSortingRepository<Product, Integer> {
        List<Product> findAllByPrice(double price, Pageable pageable);
    }
    ```

    通过扩展[PagingAndSortingRepository](https://docs.spring.io/spring-data/data-commons/docs/current/api/org/springframework/data/repository/PagingAndSortingRepository.html)，我们获得用于分页和排序的findAll（可分页）和findAll（排序）方法。

    相反，我们本可以选择扩展JpaRepository，因为它也扩展了PagingAndSortingRepository。

    一旦我们扩展了PagingAndSortingRepository，我们就可以添加自己的方法，这些方法将Pageable和Sort作为参数，就像我们在这里使用findAllByPrice所做的那样。

    让我们来看看如何使用我们的新方法对我们的产品进行分页。

4. 分页

    一旦我们从PagingAndSortingRepository扩展了存储库，我们只需要：

    - 创建或获取PageRequest对象，这是Pageable接口的实现
    - 将PageRequest对象作为参数传递给我们打算使用的存储库方法

    我们可以通过传递请求的页码和页面大小来创建PageRequest对象。

    这里的页数从零开始：

    ```java
    Pageable firstPageWithTwoElements = PageRequest.of(0, 2);
    Pageable secondPageWithFiveElements = PageRequest.of(1, 5);
    ```

    在Spring MVC中，我们还可以选择使用Spring Data Web Support在控制器中获取Pageable实例。

    一旦我们有了PageRequest对象，我们就可以在调用存储库的方法时将其传递进去：

    ```java
    Page<Product> allProducts = productRepository.findAll(firstPageWithTwoElements);
    List<Product> allTenDollarProducts = 
    productRepository.findAllByPrice(10, secondPageWithFiveElements);
    ```

    findAll（Pageable pageable）方法默认返回`Page<T>`对象。

    然而，我们可以选择从任何返回分页数据的自定义方法中返回`Page<T>`、`Slice<T`>或`List<T>`。

    `Page<T>`实例除了拥有产品列表外，还了解可用页面的总数。它触发了额外的计数查询来实现它。为了避免这种开销成本，我们可以返回`Slice<T>`或`List<T>`。

    切片只知道下一个切片是否可用。

5. 分页和分类

    同样，为了对我们的查询结果进行排序，我们可以简单地将Sort的实例传递给该方法：

    `Page<Product> allProductsSortedByName = productRepository.findAll(Sort.by("name"));`

    然而，如果我们想对数据进行排序和分页呢？

    我们可以通过将排序详细信息传递到我们的PageRequest对象本身来做到这一点：

    ```java
    Pageable sortedByName =
    PageRequest.of(0, 3, Sort.by("name"));

    Pageable sortedByPriceDesc =
    PageRequest.of(0, 3, Sort.by("price").descending());

    Pageable sortedByPriceDescNameAsc =
    PageRequest.of(0, 5, Sort.by("price").descending().and(Sort.by("name")));
    ```

    根据我们的排序要求，我们可以在创建我们的PageRequest实例时指定排序字段和排序方向。

    和往常一样，我们可以将此Pageable类型实例传递给存储库的方法。

6. 结论

    在本文中，我们学习了如何在Spring Data JPA中对查询结果进行分页和排序。
