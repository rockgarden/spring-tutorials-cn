# Core Java 11

## Java8可选指南

1. 概述

    在本教程中，我们将介绍 Java 8 中引入的 Optional 类。

    该类的目的是为表示可选值而不是空引用提供一种类型级解决方案。

    要深入了解我们为什么要关注 Optional 类，请参阅Oracle官方[文章](http://www.oracle.com/technetwork/articles/java/java8-optional-2175753.html)。

    [作为返回类型的 Java 可选类](https://www.baeldung.com/java-optional-return)

    了解最佳实践以及何时在 Java 中返回 Optional 类型。

    [Java 可选 - orElse() 与 orElseGet()](https://www.baeldung.com/java-optional-or-else-vs-or-else-get)

    探索可选的 orElse() 和 OrElseGet() 方法之间的区别。

    [在 Java 中过滤可选项流](https://www.baeldung.com/java-filter-stream-of-optional)

    在 Java 8 和 Java 9 中过滤可选项流的快速实用指南

    测试代码：optional/OptionalUnitTest.java

2. 创建可选对象

    创建可选对象有几种方法。

    要创建一个空的可选对象，我们只需使用 empty() 静态方法：

    whenCreatesEmptyOptional_thenCorrect()

    请注意，我们使用 isPresent() 方法检查了 Optional 对象内部是否存在值。只有当我们创建的 Optional 具有非空值时，值才会存在。

    我们还可以使用静态方法 of() 创建一个 Optional 对象：

    givenNonNull_whenCreatesNonNullable_thenCorrect()

    但是，传递给 of() 方法的参数不能为空。否则，我们将收到 NullPointerException 异常：

    givenNull_whenThrowsErrorOnCreate_thenCorrect()

    但是，如果我们预计会出现一些空值，我们可以使用 ofNullable() 方法：

    givenNonNull_whenCreatesNullable_thenCorrect()

    通过这种方法，如果我们传入一个空引用，它不会抛出异常，而是返回一个空的 Optional 对象：

    givenNull_whenCreatesNullable_thenCorrect()

3. 检查值是否存在：isPresent() 和 isEmpty()

    当我们有一个从方法返回或由我们创建的可选对象时，我们可以使用 isPresent() 方法检查其中是否有值：

    givenOptional_whenIsPresentWorks_thenCorrect()

    如果封装的值不是 null，此方法将返回 true。

    此外，从 Java 11 开始，我们可以使用 isEmpty 方法做相反的事情：

    givenAnEmptyOptional_thenIsEmptyBehavesAsExpected_11()

4. 使用 ifPresent() 进行条件操作

    通过 ifPresent() 方法，我们可以在发现封装值非空的情况下对其运行一些代码。在使用 Optional 之前，我们会这样做

    ```java
    if(name != null) {
        System.out.println(name.length());
    }
    ```

    这段代码先检查 name 变量是否为空，然后再执行代码。这种方法很冗长，而且这还不是唯一的问题--它还容易出错。

    事实上，我们如何保证在打印该变量后，不会再次使用它却忘记执行空值检查？

    如果代码中出现空值，运行时就会出现 NullPointerException 异常。当程序因输入问题而失败时，往往是编程实践不当造成的。

    可选项使我们可以明确地处理可空值，以此来执行良好的编程实践。

    现在让我们看看如何在 Java 8 中重构上述代码。

    在典型的函数式编程风格中，我们可以对实际存在的对象执行操作：

    givenOptional_whenIfPresentWorks_thenCorrect()

    在上面的示例中，我们只用了两行代码就取代了第一个示例中的五行代码：一行将对象包装成一个 Optional 对象，下一行执行隐式验证并执行代码。

5. 使用 orElse() 创建默认值

    orElse() 方法用于获取封装在可选实例中的值。它需要一个参数，作为默认值。如果存在封装值，orElse() 方法将返回封装值，否则将返回其参数。

6. 使用 orElseGet() 获取默认值

    orElseGet() 方法与 orElse() 方法类似。不过，该方法不是在可选值不存在时获取一个返回值，而是获取一个供应商功能接口，调用该接口并返回调用的值：

    whenOrElseGetWorks_thenCorrect()

7. orElse 和 orElseGet() 的区别

    对于很多刚接触 Optional 或 Java 8 的程序员来说，orElse() 和 orElseGet() 之间的区别并不明显。事实上，这两种方法给人的印象是功能上相互重叠。

    然而，两者之间有一个微妙但非常重要的区别，如果不能很好地理解，可能会严重影响代码的性能。

    让我们在测试类中创建一个名为 getMyDefault() 的方法，该方法不需要参数并返回一个默认值：

    getMyDefault()

    让我们来看两个测试并观察它们的副作用，以确定 orElse() 和 orElseGet() 的重叠和不同之处：

    whenOrElseGetAndOrElseOverlap_thenCorrect()

    在上面的示例中，我们在一个 Optional 对象中封装了一个空文本，并尝试使用这两种方法中的每一种获取被封装的值。

    在每种情况下都会调用 getMyDefault() 方法。碰巧的是，当封装值不存在时，orElse() 和 orElseGet() 的工作方式完全相同。

    现在，让我们运行另一个测试，在该测试中，值是存在的，理想情况下，甚至不应创建默认值：

    whenOrElseGetAndOrElseDiffer_thenCorrect()

    在上面的示例中，我们不再封装空值，代码的其余部分保持不变。

    请注意，在使用 orElseGet() 获取封装值时，由于包含的值已经存在，所以根本不会调用 getMyDefault() 方法。

    但是，在使用 orElse() 时，无论包装值是否存在，都会创建默认对象。因此，在这种情况下，我们只是创建了一个从未使用过的多余对象。

    在这个简单的示例中，创建默认对象的代价并不大，因为 JVM 知道如何处理这种情况。但是，当 getMyDefault() 这样的方法需要调用网络服务甚至查询数据库时，代价就非常明显了。

8. 使用 orElseThrow() 出现异常

    orElseThrow() 方法沿用了 orElse() 和 orElseGet() 方法，并增加了一种处理缺失值的新方法。

    当包装值不存在时，该方法不会返回默认值，而是抛出异常：

    whenOrElseThrowWorks_thenCorrect()

    Java 8 中的方法引用在这里派上了用场，可以将异常构造函数传递进来。

    Java 10 引入了简化的无参数版本 orElseThrow() 方法。如果是空的可选项，它将抛出 NoSuchElementException：

    whenNoArgOrElseThrowWorks_thenCorrect()

9. 使用 get() 返回值

    获取封装值的最后一种方法是 get() 方法：

    givenOptional_whenGetsValue_thenCorrect()

    然而，与前三种方法不同的是，get() 只有在封装对象不为 null 时才能返回值，否则会抛出无此元素异常：

    givenOptionalWithNull_whenGetThrowsException_thenCorrect()

    这是 get() 方法的主要缺陷。理想情况下，Optional 应该帮助我们避免此类不可预见的异常。因此，这种方法与 Optional 的目标背道而驰，很可能在未来的版本中被弃用。

    因此，我们建议使用其他变体，这些变体可以让我们做好准备并显式地处理 null 情况。

10. 使用 filter() 进行有条件返回

    我们可以使用 filter 方法对封装值进行内联测试。该方法将谓词作为参数，并返回一个可选对象。如果封装值通过了谓词的测试，则按原样返回可选对象。

    但是，如果谓词返回 false，那么它将返回一个空的 Optional 对象：

    whenOptionalFilterWorks_thenCorrect()

    过滤器方法通常用于根据预定义规则剔除封装值。我们可以用它来拒绝错误的电子邮件格式或不够强的密码。

    让我们来看另一个有意义的例子。假设我们想买一个调制解调器，而我们只关心它的价格。

    我们从某个网站接收调制解调器价格的推送通知，并将其存储在对象中：optional/Modem.java

    然后，我们将这些对象输入一些代码，这些代码的唯一目的就是检查调制解调器的价格是否在我们的预算范围之内。

    现在让我们看看不带 Optional 的代码：

    priceIsInRange1(Modem modem)

    请注意我们需要编写多少代码才能实现这一目标，尤其是在 if 条件中。if 条件中唯一对应用程序至关重要的部分是最后的价格范围检查；其余的检查都是防御性的：

    whenFiltersWithoutOptional_thenCorrect()

    除此以外，我们还可以在很长一段时间内忘记 null 检查，而不会出现任何编译时错误。

    现在让我们看看使用 Optional#filter 的变体：

    priceIsInRange2(Modem modem2)

    map 调用只是用来将一个值转换为其他值。请记住，此操作不会修改原始值。

    在本例中，我们从模型类中获取一个价格对象。我们将在下一节详细介绍 map() 方法。

    首先，如果传递给该方法的是一个空对象，我们预计不会有任何问题。

    其次，我们在其主体中编写的唯一逻辑正是方法名称所描述的--价格范围检查。剩下的就交给可选方法来处理吧：

    whenFiltersWithOptional_thenCorrect()

    前面的方法承诺检查价格范围，但要抵御其固有的脆弱性，还必须做更多工作。因此，我们可以使用过滤器方法替换不必要的 if 语句，并拒绝不需要的值。

11. 使用 map() 转换值

    在上一节中，我们了解了如何根据过滤器拒绝或接受一个值。

    我们可以使用类似的语法，用 map() 方法转换可选值：

    givenOptional_whenMapWorks_thenCorrect()

    在这个示例中，我们将字符串列表封装在一个 Optional 对象中，并使用其 map 方法对包含的列表执行操作。我们执行的操作是获取列表的大小。

    map 方法会返回封装在 Optional 中的计算结果。然后，我们必须在返回的 Optional 上调用适当的方法来获取其值。

    请注意，filter 方法只是对值进行检查，只有当值符合给定的谓词时，才返回描述该值的 Optional。否则返回一个空的 Optional。而 map 方法会获取现有值，使用该值执行计算，并将计算结果封装在一个 Optional 对象中返回：

    givenOptional_whenMapWorks_thenCorrect2()

    我们可以将 map 和 filter 串联起来，实现更强大的功能。

    假设我们要检查用户输入密码的正确性。我们可以使用 map 转换清除密码，然后使用过滤器检查其正确性：

    givenOptional_whenMapWorksWithFilter_thenCorrect()

    正如我们所看到的，如果不先清理输入内容，输入内容就会被过滤掉，但用户可能会想当然地认为前导空格和尾部空格都是输入内容。因此，我们在过滤掉不正确的密码之前，先用映射把脏密码转换成干净的密码。

12. 用 flatMap() 转换值

    与 map() 方法一样，我们也可以使用 flatMap() 方法来转换数值。不同之处在于，map 仅在值被解包时才对其进行转换，而 flatMap 则是在转换之前先获取已包装的值并将其解包。

    之前，我们创建了简单的字符串和整数对象，用于在可选实例中进行封装。但是，我们经常会从复杂对象的访问器中接收这些对象。

    为了更清楚地了解两者的区别，让我们来看看一个 Person 对象，它包含一个人的详细信息，如姓名、年龄和密码：optional/Person.java

    通常，我们会创建这样一个对象，并将其封装在一个 Optional 对象中，就像处理 String 一样。

    或者，也可以通过另一个方法调用将其返回给我们：

    `Person person = new Person("john", 26);`

    `Optional<Person> personOptional = Optional.of(person);`

    现在请注意，当我们封装 Person 对象时，它将包含嵌套的 Optional 实例：

    givenOptional_whenFlatMapWorks_thenCorrect2()

    在这里，我们试图检索 Person 对象的 name 属性以执行断言。

    请注意我们是如何在第三条语句中使用 map() 方法实现这一目的的，然后再注意我们是如何在之后使用 flatMap() 方法实现同样目的的。

    Person::getName 方法的引用类似于上一节中清理密码的 String::trim 调用。

    唯一不同的是，getName() 返回的是一个可选项，而不是 trim() 操作中的字符串。这一点，再加上 map 转换将结果包裹在一个可选对象中，导致了嵌套的可选对象。

    因此，在使用 map() 方法时，我们需要添加一个额外的调用，以便在使用转换后的值之前检索该值。这样，Optional 包装器就会被移除。在使用 flatMap 时，这一操作是隐式执行的。

13. Java 8 中的可选项链

    有时，我们可能需要从多个可选项中获取第一个非空的可选项对象。在这种情况下，使用 orElseOptional() 这样的方法会非常方便。遗憾的是，Java 8 并不直接支持此类操作。

    参见：OptionalChainingUnitTest.java

    让我们先介绍一下本节中将会用到的几个方法：

    - getEmpty()
    - getHello()
    - getBye()
    - createOptional(String input)

    在 Java 8 中，为了连锁多个可选对象并获取第一个非空对象，我们可以使用流 API：

    givenThreeOptionals_whenChaining_thenFirstNonEmptyIsReturned()

    这种方法的缺点是，无论非空 Optional 出现在 Stream 的哪个位置，所有 get 方法都会被执行。

    如果我们想懒散地评估传递给 Stream.of() 的方法，就需要使用方法引用和 Supplier 接口：

    givenThreeOptionals_whenChaining_thenFirstNonEmptyIsReturnedAndRestNotEvaluated()

    如果我们需要使用包含参数的方法，就必须使用 lambda 表达式：

    givenTwoOptionalsReturnedByOneArgMethod_whenChaining_thenFirstNonEmptyIsReturned()

    通常，我们希望在所有链式可选项都为空的情况下返回一个默认值。我们只需调用 orElse() 或 OrElseGet() 即可：

    @Test
    public void givenTwoEmptyOptionals_whenChaining_thenDefaultIsReturned()

14. JDK 9 可选 API

    Java 9 的发布为可选 API 增加了更多新方法：

    - or()方法，用于提供创建替代可选项的供应商(supplier)
    - ifPresentOrElse() 方法，允许在可选项存在时执行一个操作，或在不存在时执行另一个操作
    - stream() 方法，用于将可选项转换为流

    以下是完整[文章](https://www.baeldung.com/java-9-optional)，供进一步阅读。

15. 滥用可选项

    最后，让我们来看看使用可选项的一种诱人但危险的方式：将可选参数传递给方法。

    想象一下，我们有一个 Person 列表，我们想要一个方法在列表中搜索具有给定名字的人。此外，如果指定了年龄，我们希望该方法至少匹配一定年龄的条目。

    由于这个参数是可选的，所以我们使用了这个方法：

    Person.java

    `search(List<Person> people, String name, Optional<Integer> age)`

    然后我们发布我们的方法，另一个开发者尝试使用它：

    `someObject.search(people, "Peter", null);`

    现在，该开发者执行代码，并得到一个 NullPointerException 异常。在这种情况下，我们不得不对可选参数进行 null 检查，这就违背了我们希望避免这种情况发生的初衷。

    为了更好地处理这种情况，我们可以这样做：

    Person.java

    `search(List<Person> people, String name, Integer age)`

    在这里，参数仍然是可选的，但我们只用一次检查就能处理它。

    另一种方法是创建两个重载方法：

    Person.java

    `doSearch(List<Person> people, String name, int age)`

    这样，我们就提供了一个清晰的 API，其中两个方法做不同的事情（尽管它们共享实现）。

    因此，有一些解决方案可以避免将 Optional 用作方法参数。Java 在发布 Optional 时的意图是将其用作返回类型，从而表明方法可以返回空值。事实上，一些[代码检查员](https://rules.sonarsource.com/java/RSPEC-3553)甚至不鼓励将 Optional 用作方法参数。

16. 可选项和序列化

    如上所述，Optional 是作为返回类型使用的。不建议将其用作字段类型。

    此外，在可序列化类中使用 Optional 会导致 NotSerializableException。我们的文章《[Java Optional 作为返回类型](https://www.baeldung.com/java-optional-return)》进一步讨论了序列化问题。

    此外，在《[使用 Jackson 中的 Optional](https://www.baeldung.com/jackson-optional)》一文中，我们解释了在序列化 Optional 字段时会发生的情况，以及实现预期结果的一些变通方法。

17. 总结

    在本文中，我们介绍了 Java 8 可选类的大部分重要功能。

    我们简要探讨了选择使用 Optional 而不是显式空检查和输入验证的一些原因。

    我们还学习了如何使用 get()、orElse() 和 orElseGet()方法获取可选项的值或默认值（如果为空）（并了解了[后两者之间的重要区别](https://www.baeldung.com/java-filter-stream-of-optional)）。

    然后，我们了解了如何使用 map()、flatMap() 和 filter() 转换或过滤我们的可选项。我们讨论了流畅的 API Optional 所提供的功能，因为它允许我们轻松地连锁不同的方法。

    最后，我们了解了为什么将 Optionals 用作方法参数不是一个好主意，以及如何避免这种情况。

## Java8的收集器指南

1. 概述

    在本教程中，我们将介绍 Java 8 的收集器，它用于处理流的最后一步。

    要了解有关 Stream API 本身的更多信息，我们可以查看这篇[文章](https://www.baeldung.com/java-8-streams)。

    如果我们想了解如何利用收集器的强大功能进行并行处理，可以查看此项目。

    Java 8 流 API 教程

    这篇文章通过大量示例介绍了 Java 8 Stream API 提供的可能性和操作。

    [Java 8 groupingBy 收集器指南](https://www.baeldung.com/java-groupingby-collector)

    Java 8 groupingBy 收集器指南（含使用示例）。

    [Java 9 中的新流收集器](https://www.baeldung.com/java9-stream-collectors)

    在本文中，我们将探讨 JDK 9 中引入的新流收集器。

2. Stream.collect() 方法

    Stream.collect() 是 Java 8 的流 API 的终端方法之一。它允许我们对 Stream 实例中的数据元素执行可变折叠操作（将元素重新打包到某些数据结构中，并应用一些附加逻辑、将它们连接起来等）。

    该操作的策略通过收集器接口实现提供。

3. 收集器

    所有预定义的实现都可以在收集器类中找到。通常的做法是使用以下静态导入，以提高可读性：

    `import static java.util.stream.Collectors.*;`

    我们也可以选择使用单个导入收集器：

    ```java
    import static java.util.stream.Collectors.toList;
    import static java.util.stream.Collectors.toMap;
    import static java.util.stream.Collectors.toSet;
    ```

    示例代码：Java8CollectorsUnitTest.java

    1. 收集器.toList()

        toList 收集器可用于将所有 Stream 元素收集到一个 List 实例中。需要记住的重要一点是，我们不能使用此方法假设任何特定的 List 实现。如果我们想对此有更多控制，可以使用 toCollection 代替。

        让我们创建一个代表元素序列的 Stream 实例，然后将它们收集到 List 实例中：

        whenCollectingToList_shouldCollectToList()

        1. Collectors.toUnmodifiableList()

            Java 10 引入了一种方便的方法，可将 Stream 元素累加到不可修改的 [List](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html#unmodifiable) 中：

            whenCollectingToUnmodifiableList_shouldCollectToUnmodifiableList()

            现在，如果我们尝试修改结果 List，就会收到 UnsupportedOperationException 异常。

    2. 收集器.toSet()

        toSet 收集器可用于将所有 Stream 元素收集到一个 Set 实例中。需要记住的重要一点是，我们不能使用此方法假设任何特定的 Set 实现。如果我们想对此有更多控制，可以使用 toCollection 代替。

        让我们创建一个代表元素序列的 Stream 实例，然后将它们收集到一个 Set 实例中：

        whenCollectingToSet_shouldCollectToSet()

        集合不包含重复的元素。如果我们的集合中包含彼此相等的元素，它们只会在生成的 Set 中出现一次：

        givenContainsDuplicateElements_whenCollectingToSet_shouldAddDuplicateElementsOnlyOnce()

        1. Collectors.toUnmodifiableSet()

            自 Java 10 起，我们可以使用 [toUnmodifiableSet()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Set.html#unmodifiable) 集合轻松创建不可修改的集合：

            whenCollectingToUnmodifiableSet_shouldCollectToUnmodifiableSet()

            任何修改结果集的尝试都会导致 UnsupportedOperationException。

    3. 收集器.toCollection()

        正如我们已经指出的，在使用 toSet 和 toList 收集器时，我们不能对它们的实现做任何假设。如果我们想使用自定义实现，就需要使用 toCollection 收集器和我们选择的所提供的集合。

        让我们创建一个代表元素序列的 Stream 实例，然后将它们收集到一个 LinkedList 实例中：

        whenCollectingToCollection_shouldCollectToCollection()

        请注意，这对任何不可变的集合都不起作用。在这种情况下，我们需要编写一个自定义的收集器实现，或者使用 collectingAndThen。

    4. 收集器 toMap()

        toMap 收集器可用于将流元素收集到 Map 实例中。为此，我们需要提供两个函数：

        - keyMapper
        - valueMapper

        我们将使用 keyMapper 从流元素中提取 Map key，并使用 valueMapper 提取与给定 key 相关的值。

        让我们将这些元素收集到一个 Map 中，该 Map 将字符串存储为键，将字符串的长度存储为值：

        whenCollectingToMap_shouldCollectToMap()

        Function.identity() 只是定义接受和返回相同值的函数的快捷方式。

        那么，如果我们的集合包含重复的元素会怎样呢？与 toSet 相反，toMap 不会静默地过滤重复元素，这是可以理解的，因为它怎么知道该为这个键选择哪个值呢？

        givenContainsDuplicateElements_whenCollectingToMap_shouldThrowException()

        请注意，toMap 甚至不评估值是否相等。如果看到重复的键，它会立即抛出 IllegalStateException。

        在这种键冲突的情况下，我们应该使用另一种签名的 toMap：

        whenCollectingToMapwWithDuplicates_shouldCollectToMapMergingTheIdenticalItems()

        这里的第三个参数是一个 BinaryOperator，我们可以在这里指定如何处理碰撞。在本例中，我们将从这两个碰撞值中任选一个，因为我们知道相同的字符串也总是具有相同的长度。

        1. Collectors.toUnmodifiableMap()

            与列表和集合类似，Java 10 引入了一种将 Stream 元素收集到不可修改 Map 中的简便方法：

            whenCollectingToUnmodifiableMap_shouldCollectToUnmodifiableMap()

            正如我们所看到的，如果我们尝试在结果 Map 中放入一个新条目，就会收到 UnsupportedOperationException 异常。

    5. 收集器.CollectingAndThen()

        CollectingAndThen 是一种特殊的收集器，允许我们在收集结束后直接对结果执行另一个操作。

        让我们将 Stream 元素收集到 List 实例中，然后将结果转换为 ImmutableList 实例：

        whenCollectingAndThen_shouldCollect()

    6. Collectors.joining()

        Joining 收集器可用于连接 `Stream<String>` 元素。

        我们可以通过以下方法将它们连接在一起：

        whenJoining_shouldJoin()

        我们还可以指定自定义分隔符、前缀和后缀：

        whenJoiningWithSeparator_shouldJoinWithSeparator()

        我们还可以写：

        whenJoiningWithSeparatorAndPrefixAndPostfix_shouldJoinWithSeparatorPrePost()

    7. 收集器.计数()

        Counting 是一个简单的收集器，可以对所有流元素进行计数。

        现在我们可以写

        whenCounting_shouldCount()

    8. 收集器.summarizingDouble/Long/Int()

        SummarizingDouble/Long/Int 是一个收集器，它返回一个特殊的类，其中包含提取元素流中数字数据的统计信息。

        我们可以通过以下方法获取字符串长度的信息：

        whenSummarizing_shouldSummarize()

    9. Collectors.averagingDouble/Long/Int()

        AveragingDouble/Long/Int 是一种收集器，可简单地返回提取元素的平均值。

        我们可以通过以下方法获得字符串的平均长度：

        whenAveraging_shouldAverage()

    10. Collectors.summingDouble/Long/Int()

        SummingDouble/Long/Int 是一种收集器，可简单地返回提取元素的总和。

        我们可以通过以下方法获得所有字符串长度之和：

        whenSumming_shouldSum()

    11. 收集器 maxBy()/minBy()

        最大/最小收集器根据提供的比较器实例返回流中最大/最小的元素。

        我们可以通过以下方法选出最大的元素

        whenMaxingBy_shouldMaxBy()

        我们可以看到，返回值被包裹在一个 Optional 实例中。这迫使用户重新考虑空集合的角情况。

    12. Collectors.groupingBy()

        GroupingBy 收集器用于根据某些属性对对象进行分组，然后将结果存储在 Map 实例中。

        我们可以按字符串长度分组，并将分组结果存储在 Set 实例中：

        whenGroupingBy_shouldGroupBy()

        我们可以看到，groupingBy 方法的第二个参数是一个收集器。此外，我们还可以自由选择使用任何收集器。

    13. Collector.partitioningBy()

        PartitioningBy 是 groupingBy 的一种特殊情况，它接受一个 Predicate 实例，然后将流元素收集到一个 Map 实例中，该 Map 将布尔值作为键存储，将集合作为值存储。在 "true" 键下，我们可以找到与给定谓词匹配的元素集合；在 "false" 键下，我们可以找到与给定谓词不匹配的元素集合。

        我们可以这样写

        whenPartitioningBy_shouldPartition()

    14. Collectors.teeing()

        让我们使用迄今所学的收集器从给定的流中找出最大和最小数字：

        ```java
        List<Integer> numbers = Arrays.asList(42, 4, 2, 24);
        Optional<Integer> min = numbers.stream().collect(minBy(Integer::compareTo));
        Optional<Integer> max = numbers.stream().collect(maxBy(Integer::compareTo));
        // do something useful with min and max
        ```

        在这里，我们使用了两个不同的收集器，然后将这两个收集器的结果组合起来，创建出一些有意义的东西。在 Java 12 之前，为了涵盖此类用例，我们不得不对给定的流操作两次，将中间结果存储到临时变量中，然后再将这些结果合并。

        幸运的是，Java 12 提供了一个内置收集器，它可以代我们完成这些步骤；我们所要做的就是提供两个收集器和组合函数。

        由于这种新的收集器会将给定的流向两个不同的方向[Tee](https://en.wikipedia.org/wiki/Tee_(command))，因此称为发球：

        ```java
        numbers.stream().collect(teeing(
          minBy(Integer::compareTo), // The first collector
          maxBy(Integer::compareTo), // The second collector
          (min, max) -> // Receives the result from those collectors and combines them
          ));
        ```

        此示例可在 GitHub 上的 [core-java-12](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-12) 项目中找到。

4. 自定义收集器

    如果我们想编写自己的收集器实现，就需要实现收集器接口，并指定其三个通用参数：

    `public interface Collector<T, A, R> {...}`

    - T - 可供收集的对象类型
    - A - 可变累加器对象的类型
    - R - 最终结果的类型

    让我们编写一个收集器示例，将元素收集到一个 ImmutableSet 实例中。首先，我们要指定正确的类型：

    ```java
    private class ImmutableSetCollector<T>
    implements Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> {...}
    ```

    由于我们需要一个可变集合来处理内部集合操作，所以我们不能使用 ImmutableSet。相反，我们需要使用其他一些可变集合，或任何其他可以暂时为我们积累对象的类。在这种情况下，我们将使用 ImmutableSet.Builder，现在我们需要实现 5 个方法：

    - `Supplier<ImmutableSet.Builder<T>> supplier()`
    - `BiConsumer<ImmutableSet.Builder<T>, T> accumulator()`
    - `BinaryOperator<ImmutableSet.Builder<T>> combiner()`
    - `Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher()`
    - `Set<Characteristics> characteristics()`

    supplier() 方法返回一个生成空累加器实例的 Supplier 实例。

    accumulator() 方法返回一个函数，用于向现有的累加器对象添加新元素。因此，我们只需使用 Builder 的 add 方法即可。

    combiner() 方法返回一个用于将两个累加器合并在一起的函数。

    finisher() 方法返回一个用于将累加器转换为最终结果类型的函数。因此在本例中，我们只需使用 Builder 的 build 方法。

    characteristics() 方法用于向 Stream 提供一些额外信息，这些信息将用于内部优化。在本例中，我们不会关注集合中元素的顺序，因为我们将使用 Characteristics.UNORDERED。要获取更多相关信息，请查看 Characteristics 的 JavaDoc：

    完整的实现和用法：ImmutableSetCollector{}

    最后，请看这里的操作：

    whenCreatingCustomCollector_shouldCollect()

5. 结论

    在本文中，我们深入探讨了 Java 8 的收集器，并展示了如何实现收集器。请务必查看我的一个项目，它[增强了Java的并行处理能力](https://github.com/pivovarit/parallel-collectors)。

## Relevant articles

- [ ] [Guide To Java 8 Optional](https://www.baeldung.com/java-optional)
- [Guide to Java Reflection](http://www.baeldung.com/java-reflection)
- [x] [Guide to Java 8’s Collectors](https://www.baeldung.com/java-8-collectors)
- [New Features in Java 11](https://www.baeldung.com/java-11-new-features)
- [Getting the Java Version at Runtime](https://www.baeldung.com/get-java-version-runtime)
- [Invoking a SOAP Web Service in Java](https://www.baeldung.com/java-soap-web-service)
- [Java HTTPS Client Certificate Authentication](https://www.baeldung.com/java-https-client-certificate-authentication)
- [Call Methods at Runtime Using Java Reflection](https://www.baeldung.com/java-method-reflection)
- [Java HttpClient Basic Authentication](https://www.baeldung.com/java-httpclient-basic-auth)
- [Java HttpClient With SSL](https://www.baeldung.com/java-httpclient-ssl)

## Code

所有代码示例均可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-11-2) 上获取。您还可以在我的网站上阅读更多有趣的[文章](http://4comprehension.com/)。
