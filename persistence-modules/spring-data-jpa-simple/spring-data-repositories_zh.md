# [Spring Data中的CrudRepository、JpaRepository和PagingAndSortingRepository](https://www.baeldung.com/spring-data-repositories)

1. 一览表

    在这篇简短的文章中，我们将重点介绍不同类型的Spring Data存储库接口及其功能。我们将接触：

    - CrudRepository
    - PagingAndSortingRepository
    - JpaRepository

    简单地说，[Spring Data](http://projects.spring.io/spring-data/)中的每个存储库都扩展了通用存储库界面，但除此之外，它们都有不同的功能。

2. Spring数据存储库

    让我们从[JpaRepository](http://static.springsource.org/spring-data/data-jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html)开始——它扩展了[PagingAndSortingRepository](http://static.springsource.org/spring-data/data-commons/docs/current/api/org/springframework/data/repository/PagingAndSortingRepository.html)，进而扩展了CrudRepository。

    这些都定义了其功能：

    - [CrudRepository](https://docs.spring.io/spring-data/data-commons/docs/2.7.9/api/org/springframework/data/repository/CrudRepository.html)提供CRUD功能
    - [PagingAndSortingRepository](https://docs.spring.io/spring-data/data-commons/docs/2.7.9/api/org/springframework/data/repository/PagingAndSortingRepository.html)提供了对记录进行分页和排序的方法
    - [JpaRepository](https://docs.spring.io/spring-data/jpa/docs/2.7.9/api/org/springframework/data/jpa/repository/JpaRepository.html)提供与JPA相关的方法，如刷新持久上下文和删除批处理中的记录

    因此，由于这种继承关系，JpaRepository包含CrudRepository和PagingAndSortingRepository的完整API。

    当我们不需要JpaRepository和PagingAndSortingRepository提供的全部功能时，我们可以使用CrudRepository。

    现在让我们来看看一个快速示例，以更好地了解这些API。

    我们将从一个简单的产品实体开始：

    ```java
    @Entity
    public class Product {

        @Id
        private long id;
        private String name;

        // getters and setters
    }
    ```

    让我们实施一个简单的操作——根据其名称找到一个产品：

    ```java
    @Repository
    public interface ProductRepository extends JpaRepository<Product, Long> {
        Product findByName(String productName);
    }
    ```

    就这些。Spring数据存储库将根据我们提供的名称自动生成实现。

    当然，这是一个非常简单的例子；你可以在这里更深入地了解Spring Data JPA。

3. Crud存储库

    现在让我们来看看CrudRepository接口的代码：

    ```java
    public interface CrudRepository<T, ID extends Serializable>
    extends Repository<T, ID> {

        <S extends T> S save(S entity);

        T findOne(ID primaryKey);

        Iterable<T> findAll();

        Long count();

        void delete(T entity);

        boolean exists(ID primaryKey);
    }
    ```

    注意典型的CRUD功能：

    - save(...) – 保存可更可的实体。在这里，我们可以传递多个对象，将它们保存在批次中
    - findOne(...) – 根据传递的主键值获取单个实体
    - findAll（）-获取数据库中所有可用实体的可Iterable
    - count（）-返回表中实体总数的计数
    - delete(...) – 删除基于传递对象的实体
    - exists(...) -根据传递的主键值验证实体是否存在

    这个接口看起来非常通用和简单，但实际上，它提供了应用程序中所需的所有基本查询抽象。

4. 分页和分页存储库

    现在，让我们来看看另一个存储库接口，它扩展了CrudRepository：

    ```java
    public interface PagingAndSortingRepository<T, ID extends Serializable> 
    extends CrudRepository<T, ID> {

        Iterable<T> findAll(Sort sort);

        Page<T> findAll(Pageable pageable);
    }
    ```

    此接口提供了一个方法findAll（Pageable pageable），这是实现分页的关键。

    使用Pageable时，我们创建一个具有特定属性的Pageable对象，我们必须至少指定以下内容：

    - 页面大小
    - 当前页码
    - 分拣

    因此，让我们假设我们想显示结果集的第一页，按姓氏排序，升序，每个记录不超过五条。这就是我们如何使用PageRequest和Sort定义来实现这一点：

    ```java
    Sort sort = new Sort(new Sort.Order(Direction.ASC, "lastName"));
    Pageable pageable = new PageRequest(0, 5, sort);
    ```

    将可分页对象传递给Spring数据查询将返回相关结果（PageRequest的第一个参数是零基的）。

5. Jpa存储库

    最后，我们将看看JpaRepository接口：

    ```java
    public interface JpaRepository<T, ID extends Serializable> extends
    PagingAndSortingRepository<T, ID> {

        List<T> findAll();

        List<T> findAll(Sort sort);

        List<T> save(Iterable<? extends T> entities);

        void flush();

        T saveAndFlush(T entity);

        void deleteInBatch(Iterable<T> entities);
    }
    ```

    再次，让我们简要地看看这些方法中的每一个：

    - findAll() – 获取数据库中所有可用实体的列表
    - findAll(...) – 获取所有可用实体的列表，并使用提供的条件对它们进行排序
    - save(...) – 保存可更可的实体。在这里，我们可以传递多个对象，将它们保存在批次中
    - flush（）-将所有待定任务刷新到数据库
    - saveAndFlush(...) – 保存实体并立即刷新更改
    - deleteInBatch(...) – 删除实体的可Iterable。在这里，我们可以传递多个对象来批量删除它们

    显然，上述接口扩展了PagingAndSortingRepository，这意味着它还具有CrudRepository中存在的所有方法。

6. Spring Data 3中的Spring Data存储库

    在新版本的Spring Data中，一些存储库类的内部略有变化，增加了新功能，并提供了更简单的开发体验。

    我们现在可以访问有利的基于列表的CRUD存储库界面。此外，一些spring-data存储库类的类层次结构基于不同的结构。

    所有详细信息都可以在[Spring Data 3文章](https://www.baeldung.com/spring-data-3-crud-repository-interfaces)中的新CRUD存储库接口中找到。

7. Spring数据存储库的缺点

    除了这些存储库的所有非常有用的优势外，直接依赖这些也有一些基本的缺点：

    1. 我们将代码与库及其特定抽象耦合，例如“Page”或“Pageable”；当然，这不是该库独有的——但我们确实必须小心不要暴露这些内部实现细节
    2. 通过扩展，例如CrudRepository，我们一次性公开了一整套持久化方法。在大多数情况下，这可能也没问题，但我们可能会遇到这样一种情况，即我们希望对暴露的方法进行更精细的控制，例如创建一个不包含CrudRepository的save(…)和delete(…)方法的ReadOnlyRepository

8. 结论

    本文涵盖了Spring Data JPA存储库接口的一些简短但重要的差异和功能。
