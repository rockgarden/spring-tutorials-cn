# [Spring Data JPA存储库中的派生查询方法](https://www.baeldung.com/spring-data-derived-queries)

1. 一览表

    对于简单的查询，只需查看我们代码中的相应方法名称，就很容易得出查询应该是什么。

    在本教程中，我们将探索Spring Data JPA如何以方法命名惯例的形式利用这个想法。

2. Spring中派生查询方法的结构

    派生方法名称有两个主要部分，由第一个By关键字分隔：

    `List<User> findByName(String name)`

    第一部分——如查找——是介绍人，其余部分——如ByName——是标准。

    Spring Data JPA支持查找、读取、查询、计数和获取。因此，我们本可以完成queryByName，Spring Data也会有同样的行为。

    我们还可以使用Distinct、First或Top来删除重复项或限制我们的结果集：

    `List<User> findTop3ByAge()`

    标准部分包含查询的实体特定条件表达式。我们可以将条件关键字与实体的属性名称一起使用。

    我们还可以将表达式与And和Or连接在一起，我们一会儿就会看到。

3. 示例应用程序

    首先，我们当然需要一个使用Spring Data JPA的应用程序。

    在该应用程序中，让我们定义一个实体类：

    ```java
    @Table(name = "users")
    @Entity
    class User {
        @Id
        @GeneratedValue
        private Integer id;
        
        private String name;
        private Integer age;
        private ZonedDateTime birthDate;
        private Boolean active;

        // standard getters and setters
    }
    ```

    让我们也定义一个存储库。

    它将扩展JpaRepository，这是Spring数据存储库类型之一：

    `interface UserRepository extends JpaRepository<User, Integer> {}`

    我们将在这里放置所有派生查询方法。

4. 平等条件关键词

    精确等式是查询中最常用的条件之一。我们有几个选项可以在查询中表达=或IS运算符。

    对于完全匹配条件，我们可以在没有任何关键字的情况下附加属性名称：

    `List<User> findByName(String name);`

    我们可以添加“Is”或“Equals”来增加可读性：

    ```java
    List<User> findByNameIs(String name);
    List<User> findByNameEquals(String name);
    ```

    当我们需要表达不平等时，这种额外的可读性就派上用场了：

    `List<User> findByNameIsNot(String name);`

    这比findByNameNot（String）更可读！

    由于空等式是一个特殊情况，我们不应该使用=运算符。默认情况下，Spring Data JPA处理[空参数](https://www.baeldung.com/spring-data-jpa-null-parameters)。因此，当我们为等式条件传递空值时，Spring在生成的SQL中将查询解释为IS NULL。

    我们还可以使用IsNull关键字将IS NULL标准添加到查询中：

    ```java
    List<User> findByNameIsNull();
    List<User> findByNameIsNotNull();
    ```

    请注意，IsNull和IsNotNull都不需要方法参数。

    还有两个不需要任何参数的关键词。

    我们可以使用True和False关键字来为布尔类型添加相等条件：

    ```java
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
    ```

    当然，我们有时想要比完全平等更宽容的东西，所以让我们看看我们还能做什么。

5. 相似性条件关键词

    当我们需要用属性模式查询结果时，我们有几个选项。

    我们可以使用StartingWith找到以值开头的名称：

    `List<User> findByNameStartingWith(String prefix);`

    大致上，这翻译为“WHERE name LIKE 'value%'”。

    如果我们想要以值结尾的名字，EndingWith就是我们想要的：

    `List<User> findByNameEndingWith(String suffix);`

    或者我们可以找到哪些名称包含包含的值：

    `List<User> findByNameContaining(String infix);`

    请注意，上述所有条件都称为预定义模式表达式。因此，当调用这些方法时，我们不需要在参数中添加%运算符。

    但让我们假设我们正在做一些更复杂的事情。假设我们需要获取名称以ana开头、包含b、以c结尾的用户。

    为此，我们可以用“LIKE”关键字添加我们自己的“Like”：

    `List<User> findByNameLike(String likePattern);`

    然后，当我们调用该方法时，我们可以交出我们的LIKE模式：

    ```java
    String likePattern = "a%b%c";
    userRepository.findByNameLike(likePattern);
    ```

    现在名字已经足够了。让我们在用户中尝试一些其他值。

6. 比较条件关键词

    此外，我们可以使用LessThan和LessThanEqual关键字，使用<和<=运算符将记录与给定值进行比较：

    ```java
    List<User> findByAgeLessThan(Integer age);
    List<User> findByAgeLessThanEqual(Integer age);
    ```

    在相反的情况下，我们可以使用GreaterThan和GreaterThanEqual关键字：

    ```java
    List<User> findByAgeGreaterThan(Integer age);
    List<User> findByAgeGreaterThanEqual(Integer age);
    ```

    或者我们可以找到年龄介于两个年龄之间的用户：

    `List<User> findByAgeBetween(Integer startAge, Integer endAge);`

    我们还可以提供一个年龄集合，以匹配使用In：

    `List<User> findByAgeIn(Collection<Integer> ages);`

    由于我们知道用户的出生日期，我们可能想要查询在给定日期之前或之后出生的用户。

    我们会使用Before和After：

    ```java
    List<User> findByBirthDateAfter(ZonedDateTime birthDate);
    List<User> findByBirthDateBefore(ZonedDateTime birthDate);
    ```

7. 多个条件表达式

    我们可以使用And和Or关键字组合尽可能多的表达式：

    ```java
    List<User> findByNameOrAge(String name, Integer age);
    List<User> findByNameOrAgeAndActive(String name, Integer age, Boolean active);
    ```

    优先顺序是And，然后是Or，就像Java一样。

    虽然Spring Data JPA对我们可以添加的表达式数量没有限制，但我们不应该在这里发疯。长名字是不可读的，而且很难维护。对于复杂的查询，请转而查看@Query注释。

8. 对结果进行排序

    接下来，我们来看看排序。

    我们可以要求使用OrderBy按名字按字母顺序对用户进行排序：

    ```java
    List<User> findByNameOrderByName(String name);
    List<User> findByNameOrderByNameAsc(String name);
    ```

    升序是默认排序选项，但我们可以使用Desc反向排序：

    `List<User> findByNameOrderByNameDesc(String name);`

9. 在CrudRepository中找到一个vsfindById

    Spring团队使用Spring Boot 2.x在CrudRepository中进行了一些重大更改。其中一个是重命名findOne到findById。

    以前，使用Spring Boot 1.x，当我们想通过其主键检索实体时，我们会调用findOne：

    `User user = userRepository.findOne(1);`

    自Spring Boot 2.x以来，我们可以对findById做同样的事情：

    `User user = userRepository.findById(1);`

    请注意，findById（）方法已经在CrudRepository中为我们定义了。因此，我们不必在扩展CrudRepository的自定义存储库中明确定义它。

10. 结论

    在本文中，我们解释了Spring Data JPA中的查询推导机制。我们使用属性条件关键字在Spring Data JPA存储库中编写派生查询方法。
