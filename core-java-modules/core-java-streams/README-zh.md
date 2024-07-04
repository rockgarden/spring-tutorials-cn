# Core Java streams

## 使用Lambda表达式的Java流过滤器

1. 简介

    在本快速教程中，我们将探讨在使用 Java 流时如何使用 Stream.filter() 方法。

    我们将了解如何使用该方法，以及如何处理已检查异常的特殊情况。

2. 使用 Stream.filter()

    filter()方法是流接口的一个中间操作，它允许我们过滤流中与给定谓词匹配的元素：

    `Stream<T> filter(Predicate<? super T> predicate)`

    让我们创建一个客户（Customer）类，看看它是如何工作的：

    stream.filter/Customer.java

    此外，让我们创建一个客户集合：

    `List<Customer> customers = Arrays.asList(john, sarah, charles, mary);`

    1. 过滤集合

        filter() 方法的一个常见用例是[处理集合](https://www.baeldung.com/java-collection-filtering)。

        让我们制作一个积分超过 100 分的客户列表。为此，我们可以使用 lambda 表达式：

        givenListOfCustomers_whenFilterByPoints_thenGetTwo()

        我们还可以使用[方法引用](https://www.baeldung.com/java-8-double-colon-operator)，它是 lambda 表达式的简写：

        givenListOfCustomers_whenFilterByMethodReference_thenGetTwo()

        在本例中，我们在客户类Customer中添加了 hasOverHundredPoints 方法：

        hasOverHundredPoints()

        在这两种情况下，我们都得到了相同的结果。

    2. 使用多个条件过滤集合

        此外，我们还可以在 filter() 中使用多个条件。例如，我们可以通过积分和姓名进行筛选：

        givenListOfCustomers_whenFilterByPointsAndName_thenGetOne()

3. 处理异常

    到目前为止，我们一直将过滤器用于不会抛出异常的谓词。事实上，Java 的函数式接口并没有声明任何已检查或未检查的异常。

    接下来，我们将展示一些在[lambda表达式中处理异常](https://www.baeldung.com/java-lambda-exceptions)的不同方法。

    1. 使用自定义封装器

        首先，我们将在 Customer 中添加 profilePhotoUrl 属性。

        此外，让我们添加一个简单的 hasValidProfilePhoto() 方法来检查配置文件是否可用。

        我们可以看到 hasValidProfilePhoto() 方法抛出了一个 IOException。现在，如果我们尝试用该方法过滤客户，我们可以使用

        givenListOfCustomers_whenFilterWithThrowingFunction_thenThrowException()

        我们将看到以下错误：

        `Incompatible thrown types java.io.IOException in functional expression`

        要处理这个问题，我们可以使用的替代方法之一是用 try-catch 块封装它：

        givenListOfCustomers_whenFilterWithTryCatch_thenGetTwo()

        如果我们需要从谓词中抛出异常，可以将其封装在一个未选中的异常中，如 RuntimeException。

    2. 使用 ThrowingFunction

        我们也可以使用 ThrowingFunction 库。

        ThrowingFunction 是一个开源库，允许我们在 Java 函数接口中处理检查异常。

        让我们先在 pom 中添加 Throwing-function 依赖项：

        ```xml
        <dependency>
            <groupId>pl.touk</groupId>
            <artifactId>throwing-function</artifactId>
            <version>1.3</version>
        </dependency>
        ```

        为了处理谓词中的异常，该库为我们提供了 ThrowingPredicate 类，该类拥有 unchecked() 方法来封装已检查的异常。

        让我们来看看它的实际应用：

        givenListOfCustomers_whenFilterWithThrowingFunction_thenThrowException()

4. 结论

    在本文中，我们举例说明了如何使用 filter() 方法处理数据流。我们还探讨了一些处理异常的替代方法。

## Relevant Articles

- [Java 8 and Infinite Streams](https://www.baeldung.com/java-inifinite-streams)
- [How to Get the Last Element of a Stream in Java?](https://www.baeldung.com/java-stream-last-element)
- [“Stream has already been operated upon or closed” Exception in Java](https://www.baeldung.com/java-stream-operated-upon-or-closed-exception)
- [Iterable to Stream in Java](https://www.baeldung.com/java-iterable-to-stream)
- [How to Iterate Over a Stream With Indices](https://www.baeldung.com/java-stream-indices)
- [Stream Ordering in Java](https://www.baeldung.com/java-stream-ordering)
- [x] [Java Stream Filter with Lambda Expression](https://www.baeldung.com/java-stream-filter-lambda)
- [Counting Matches on a Stream Filter](https://www.baeldung.com/java-stream-filter-count)
- [Summing Numbers with Java Streams](https://www.baeldung.com/java-stream-sum)
- [How to Find All Getters Returning Null](https://www.baeldung.com/java-getters-returning-null)
- More articles: [[next -->]](/../core-java-streams-2)

## Code

完整的代码可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-streams) 上找到。
