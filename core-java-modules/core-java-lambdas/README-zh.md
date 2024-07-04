# lambdas

## 为什么在 Lambdas 中使用的局部变量必须是最终变量或有效的最终变量？

1. 简介

    Java 8 为我们提供了 lambdas 以及有效最终变量的概念。有没有想过为什么在 lambdas 中捕获的局部变量必须是最终变量或有效最终变量？

    JLS 给了我们一点提示："对有效最终变量的限制禁止访问动态变化的局部变量，因为捕获这些局部变量可能会带来并发问题"。但这又是什么意思呢？

    在接下来的章节中，我们将深入探讨这一限制，并了解 Java 引入它的原因。我们将举例说明它对单线程和并发应用程序的影响，我们还将揭穿一种绕过此限制的常见反模式。

    相关代码：lambdas/LambdaVariables.java

2. 捕获 Lambda

    Lambda 表达式可以使用在外层作用域中定义的变量。我们将这些 lambdas 称为捕获 lambdas。它们可以捕获静态变量、实例变量和局部变量，但只有局部变量必须是最终变量或有效的最终变量。

    在早期的 Java 版本中，当匿名内部类捕获包围它的方法的局部变量时，我们就会遇到这种情况--我们需要在局部变量前添加 final 关键字，编译器才会满意。

    作为一种语法糖，编译器现在可以识别这样的情况：虽然没有出现 final 关键字，但引用并没有发生任何变化，这意味着它实际上是最终的。我们可以说，如果我们声明一个变量为 final，编译器不会抱怨，那么这个变量就是有效的 final 变量。

3. 捕获 Lambdas 中的局部变量

    简单地说，这不会编译：

    ```java
    Supplier<Integer> incrementer(int start) {
    return () -> start++;
    }
    ```

    start 是一个局部变量，我们试图在 lambda 表达式中修改它。

    无法编译的根本原因是，lambda 正在捕获 start 的值，也就是复制它。强制变量为最终变量，可以避免给人留下在 lambda 中递增 start 实际上会修改 start 方法参数的印象。

    但是，为什么要复制呢？请注意，我们正在从方法中返回 lambda。因此，在 start 方法参数被垃圾回收之前，lambda 不会运行。Java 必须复制 start，才能让 lambda 在方法之外存活。

    1. 并发问题

        为了好玩，让我们想象一下 Java 确实允许局部变量以某种方式与其捕获的值保持连接。

        我们应该怎么做呢？

        ```java
        public void localVariableMultithreading() {
            boolean run = true;
            executor.execute(() -> {
                while (run) {
                    // do operation
                }
            });
            
            run = false;
        }
        ```

        虽然这看起来很简单，但却存在 "可见性" 的隐患。回想一下，每个线程都有自己的栈，那么我们如何确保 while 循环能看到其他栈中 run 变量的变化呢？在其他情况下，答案可能是使用同步块(synchronized blocks)或 volatile 关键字。

        不过，由于 Java 实施了有效的最终限制，我们不必担心类似的复杂问题。

4. 捕获 lambdas 中的静态变量或实例变量

    如果我们将前面的示例与在 lambda 表达式中使用静态变量或实例变量进行比较，就会发现一些问题。

    我们只需将 start 变量转换为实例变量，就能使第一个示例顺利编译：

    ```java
    private int start = 0;
    Supplier<Integer> incrementer() {
        return () -> start++;
    }
    ```

    但是，为什么我们可以在这里改变 start 的值呢？

    简单地说，这与成员变量的存储位置有关。局部变量在栈上，而成员变量在堆上。由于我们处理的是堆内存，编译器可以保证 lambda 可以访问 start 的最新值。

    我们可以用同样的方法修复第二个示例：

    ```java
    private volatile boolean run = true;
    public void instanceVariableMultithreading() {
        executor.execute(() -> {
            while (run) {
                // do operation
            }
        });
        run = false;
    }
    ```

    由于我们添加了 volatile 关键字，即使在另一个线程中执行，lambda 也能看到 run 变量。

    一般来说，在捕获实例变量时，我们可以将其视为捕获最终变量 this。无论如何，编译器没有抱怨并不意味着我们不应该采取预防措施，尤其是在多线程环境中。

5. 避免变通

    为了绕过对局部变量的限制，有人可能会想到使用变量持有者来修改局部变量的值。

    让我们来看一个在单线程应用程序中使用数组存储变量的示例：

    workaroundSingleThread()

    我们可能会认为数据流在对每个值求和 2，但实际上它在对 0 求和，因为这是 lambda 执行时的最新值。

    让我们更进一步，在另一个线程中执行求和：

    workaroundMultithreading()

    - [ ] 我们在这里求和的是什么值？这取决于我们的模拟处理需要多长时间。如果时间短到足以让方法的执行在其他线程执行之前结束，那么就会打印 6，否则就会打印 12。

    一般来说，这类变通方法容易出错，而且会产生不可预知的结果，所以我们应该尽量避免。

6. 结论

    在本文中，我们解释了为什么 lambda 表达式只能使用最终局部变量或有效的最终局部变量。正如我们所看到的，这种限制来自于这些变量的不同性质以及 Java 如何将它们存储在内存中。我们还说明了使用常见变通方法的危险。

## Java8使用Lambdas进行功能强大的比较

1. 概述

    在本教程中，我们将初步了解 Java 8 中的 Lambda 支持，特别是如何利用它来编写比较器和对集合进行排序。

    [Java 8 流 API 教程](https://www.baeldung.com/java-8-streams)

    本文通过大量实例介绍了 Java 8 Stream API 提供的可能性和操作。

    [Java 8 收集器指南](https://www.baeldung.com/java-8-collectors)

    本文讨论了 Java 8 收集器，展示了内置收集器的示例，并介绍了如何构建自定义收集器。

    [Lambda 表达式和功能接口： 使用技巧和最佳实践](https://www.baeldung.com/java-8-lambda-expressions-tips)

    使用 Java 8 lambda 表达式和功能接口的技巧和最佳实践。

    首先，让我们定义一个简单的实体类：

    java8.entity/Human.java

    测试代码参见：Java8SortUnitTest.java

2. 无 lambdas 的基本排序

    在 Java 8 之前，对集合进行排序需要为排序中使用的比较器创建一个匿名内部类：

    ```java
    new Comparator<Human>() {
        @Override
        public int compare(Human h1, Human h2) {
            return h1.getName().compareTo(h2.getName());
        }
    }
    ```

    这只是用来对 Human 实体列表进行排序：

    givenPreLambda_whenSortingEntitiesByName_thenCorrectlySorted()

3. 支持 Lambda 的基本排序

    引入 Lambda 后，我们现在可以绕过匿名内部类，用简单的函数语义实现相同的结果：

    `(final Human h1, final Human h2) -> h1.getName().compareTo(h2.getName());`

    同样，我们现在可以像以前一样测试行为：

    whenSortingEntitiesByName_thenCorrectlySorted()

    请注意，我们还使用了 Java 8 中添加到 java.util.List 中的新排序 API，而不是旧的 Collections.sort API。

    我们可以通过不指定类型定义来进一步简化表达式；编译器能够自行推断出这些定义：

    `(h1, h2) -> h1.getName().compareTo(h2.getName())`

    同样，测试仍然非常相似：

    givenLambdaShortForm_whenSortingEntitiesByName_thenCorrectlySorted()

4. 使用静态方法引用进行排序

    接下来，我们将使用引用静态方法的 Lambda 表达式执行排序。

    首先，我们在 Human 定义 compareByNameThenAge 方法，其签名与 `Comparator<Human>` 对象中的比较方法完全相同。

    然后，我们将使用此引用调用 humans.sort 方法：

    `humans.sort(Human::compareByNameThenAge);`

    最终结果是使用静态方法作为比较器对集合进行排序：

    givenMethodDefinition_whenSortingEntitiesByNameThenAge_thenCorrectlySorted()

5. 对提取的比较器进行排序

    我们还可以通过使用实例方法引用和 Comparator.comparing 方法来避免定义比较逻辑本身，后者会根据该函数提取并创建一个 Comparable。

    我们将使用 getter getName() 来构建 Lambda 表达式，并按名称对列表进行排序：

    givenInstanceMethod_whenSortingEntitiesByName_thenCorrectlySorted()

6. 反向排序

    JDK 8 还引入了反转比较器的辅助方法。我们可以快速使用它来反转排序：

    whenSortingEntitiesByNameReversed_thenCorrectlySorted()

7. 使用多个条件排序

    比较 lambda 表达式不必如此简单。我们还可以编写更复杂的表达式，例如首先按名称排序，然后按年龄排序：

    whenSortingEntitiesByNameThenAge_thenCorrectlySorted()

8. 使用多个条件排序 - 合成

    同样的比较逻辑（先按名称排序，再按年龄排序）也可以通过 Comparator 的新组合支持来实现。

    从 JDK 8 开始，我们现在可以将多个比较器连锁起来，构建更复杂的比较逻辑：

    givenComposition_whenSortingEntitiesByNameThenAge_thenCorrectlySorted()

9. 使用 Stream.sorted() 对列表排序

    我们还可以使用 Java 8 的 Stream sorted() API 对集合进行排序。

    我们可以使用自然排序和比较器提供的排序对流进行排序。为此，我们提供了两种重载的 sorted() API 变体：

    - sorted() - 使用自然排序对流元素进行排序；元素类必须实现 Comparable 接口。
    - sorted(Comparator<? super T> comparator) - 根据比较器实例对元素排序

    下面我们来看一个如何使用自然排序的 sorted() 方法的示例：

    givenStreamNaturalOrdering_whenSortingEntitiesByName_thenCorrectlySorted()

    现在让我们看看如何使用自定义比较器和 sorted() API：

    givenStreamCustomOrdering_whenSortingEntitiesByName_thenCorrectlySorted()

    如果使用 Comparator.comparing() 方法，我们可以进一步简化上述示例：

    givenStreamComparatorOrdering_whenSortingEntitiesByName_thenCorrectlySorted()

10. 使用 Stream.sorted() 对列表进行反向排序

    我们还可以使用 Stream.sorted() 对集合进行反向排序。

    首先，我们来看一个示例，说明如何将 sorted() 方法与 Comparator.reverseOrder() 方法相结合，按相反的自然顺序对列表排序：

    givenStreamNaturalOrdering_whenSortingEntitiesByNameReversed_thenCorrectlySorted()

    现在让我们看看如何使用 sorted() 方法和自定义比较器：

    givenStreamCustomOrdering_whenSortingEntitiesByNameReversed_thenCorrectlySorted()

    请注意，compareTo 的调用是翻转的，这就是反转的原因。

    最后，让我们使用 Comparator.comparing() 方法来简化上述示例：

    givenStreamComparatorOrdering_whenSortingEntitiesByNameReversed_thenCorrectlySorted()

11. 空值

    到目前为止，我们实现比较器的方式使其无法对包含空值的集合进行排序。也就是说，如果集合中至少包含一个空元素，那么排序方法就会抛出 NullPointerException：

    givenANullElement_whenSortingEntitiesByName_thenThrowsNPE()

    最简单的解决方案是在比较器实现中手动处理空值：

    givenANullElement_whenSortingEntitiesByNameManually_thenMovesTheNullToLast()

    在这里，我们要将所有空元素推到集合的末尾。为此，比较器认为空值大于非空值。当两个值都为空时，它们被视为相等。

    此外，我们还可以将任何非 null 安全的比较器传递到 Comparator.nullsLast() 方法中，从而获得相同的结果：

    givenANullElement_whenSortingEntitiesByName_thenMovesTheNullToLast()

    同样，我们可以使用 Comparator.nullsFirst() 将空元素移到集合的起始位置：

    givenANullElement_whenSortingEntitiesByName_thenMovesTheNullToStart()

    强烈建议使用 nullsFirst() 或 nullsLast() 装饰器，因为它们更灵活、更易读。

12. 结束语

    本文介绍了使用 Java 8 Lambda 表达式对 List 进行排序的各种令人兴奋的方法，这些方法超越了语法糖，进入了真正的、强大的函数语义。

## Java8 中的功能接口

1. 简介

   本教程介绍 Java 8 中的各种功能接口、一般用例以及标准 JDK 库中的用法。

   [Java 中将 Iterable 转换为流](https://www.baeldung.com/java-iterable-to-stream)

   这篇文章解释了如何将 Iterable 转换为 Stream 以及 Iterable 接口不直接支持它的原因。

   [如何在 Java 8 流中使用 if/else 逻辑](https://www.baeldung.com/java-8-streams-if-else-logic)

   了解如何在 Java 8 Streams 中应用 if/else 逻辑。

2. Java 8 中的 Lambdas

   Java 8 以 lambda 表达式的形式带来了强大的新语法改进。lambda 是一种匿名函数，我们可以将其作为一等语言公民来处理。例如，我们可以将它传递给方法或从方法中返回它。

   在 Java 8 之前，我们通常会为每一种需要封装单一功能的情况创建一个类。这就意味着要定义一些作为原始函数表示的东西，从而产生大量不必要的模板代码。

   文章 "[Lambda 表达式和功能接口：提示和最佳实践](https://www.baeldung.com/java-8-lambda-expressions-tips)" 一文更详细地介绍了功能接口和使用 lambda 的最佳实践。本指南重点介绍 java.util.function 包中的一些特定函数接口。

3. 功能接口

   建议所有函数接口都使用内容丰富的 @FunctionalInterface 注解。这可以清楚地传达接口的目的，还可以让编译器在注解的接口不满足条件时产生错误。

   任何带有 SAM（Single Abstract Method 单抽象方法）的接口都是功能接口，其实现可被视为 lambda 表达式。

   请注意，Java 8 的默认方法不是抽象方法，不算在内；一个功能接口仍然可以有多个默认方法。我们可以通过查看函数的[文档](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/function/Function.html)来了解这一点。

4. 函数

   lambda 最简单、最一般的情况是一个功能接口有一个接收一个值并返回另一个值的方法。这种只有一个参数的函数用 Function 接口来表示，它的参数是参数类型和返回值：

   `public interface Function<T, R> { … }`

   标准库中使用 Function 类型的方法之一是 Map.computeIfAbsent 方法。该方法通过 key 从 map 返回值，但如果 key 在 map 中不存在，则计算值。计算值时，它使用传递的 Function 实现：

   ```java
   Map<String, Integer> nameMap = new HashMap<>();
   Integer value = nameMap.computeIfAbsent("John", s -> s.length());
   ```

   在这种情况下，我们将对一个键应用一个函数来计算一个值，这个键放在一个 map 中，也是从一个方法调用中返回的。我们可以将 lambda 替换为与传递和返回值类型相匹配的方法引用。

   请记住，我们调用方法的对象实际上是方法的隐式第一个参数。这样，我们就可以将实例方法长度引用转换为函数接口：

   `Integer value = nameMap.computeIfAbsent("John", String::length);`

   Function 接口还有一个默认的 compose 方法，允许我们将多个函数合并为一个函数并按顺序执行：

   ```java
   Function<Integer, String> intToString = Object::toString;
   Function<String, String> quote = s -> "'" + s + "'";
   Function<Integer, String> quoteIntToString = quote.compose(intToString);
   assertEquals("'5'", quoteIntToString.apply(5));
   ```

   quoteIntToString 函数是 quote 函数应用于 intToString 函数结果的组合。

5. 基元函数特化

   由于基元类型不能作为通用类型的参数，因此针对最常用的基元类型 double、int、long 以及它们在参数和返回类型中的组合提供了不同版本的函数接口：

   - IntFunction、LongFunction、DoubleFunction：参数为指定类型，返回类型参数化
   - ToIntFunction、ToLongFunction、ToDoubleFunction：返回类型为指定类型，参数被参数化
   - DoubleToIntFunction、DoubleToLongFunction、IntToDoubleFunction、IntToLongFunction、LongToIntFunction、LongToDoubleFunction：参数和返回类型都被定义为原始类型，如其名称所示

   例如，对于接收 short 并返回字节的函数，没有现成的函数接口，但我们可以编写自己的接口：

   functionalinterface/ShortToByteFunction.java

   现在我们可以编写一个方法，使用 ShortToByteFunction 定义的规则将 short 数组转换为字节数组：

   transformArray()

   下面我们可以用它将一个短数组转换为一个乘以 2 的字节数组：

   whenUsingCustomFunctionalInterfaceForPrimitives_thenCanUseItAsLambda()

   代码参见：FunctionalInterfaceUnitTest.java

6. 双参数函数特化(Two-Arity Function Specializations)

   要定义具有两个参数的 lambdas，我们必须使用名称中包含 "Bi" 关键字的附加接口： BiFunction、ToDoubleBiFunction、ToIntBiFunction 和 ToLongBiFunction。

   BiFunction 同时生成了参数和返回类型，而 ToDoubleBiFunction 和其他接口允许我们返回一个原始值。

   在标准 API 中使用该接口的典型示例之一是 Map.replaceAll 方法，该方法允许用某个计算值替换 map 中的所有值。

   让我们使用 BiFunction 实现来接收一个键和一个旧值，计算出一个新的工资值并返回。

   whenUsingBiFunction_thenCanUseItToReplaceMapValues()

7. 供应商

   Supplier 功能接口是另一个不带任何参数的函数特化。我们通常用它来懒散地生成值。例如，让我们定义一个函数，用于平方一个 double 值。它接收的不是值本身，而是该值的一个 Supplier：

   squareLazy()

   这样，我们就可以使用一个 Supplier 实现，为调用此函数轻松生成参数。如果生成参数需要花费大量时间，这将非常有用。我们将使用 Guava 的 sleepUninterruptibly 方法来模拟这种情况：

   whenUsingSupplierToGenerateValue_thenValueIsGeneratedLazily()

   供应商的另一个用例是定义序列生成的逻辑。为了演示这一点，让我们使用静态 Stream.generate 方法来创建斐波那契数流：

   whenUsingSupplierToGenerateNumbers_thenCanUseItInStreamGenerate()

   我们传递给 Stream.generate 方法的函数实现了 Supplier 功能接口。请注意，要成为有用的生成器，Supplier 通常需要某种外部状态。在本例中，它的状态包括最后两个斐波那契数列数字。

   为了实现这种状态，我们使用了一个数组而不是几个变量，因为在 lambda 中使用的所有外部变量都必须是有效的最终变量。

   Supplier 功能接口的其他特殊化包括 BooleanSupplier、DoubleSupplier、LongSupplier 和 IntSupplier，它们的返回类型是相应的基元。

8. 消费者

   与 Supplier 不同，Consumer 只接受一个生成的参数，不返回任何内容。它是一个表示副作用的函数。

   例如，让我们通过在控制台中打印问候语来问候姓名列表中的每个人。传递给 List.forEach 方法的 lambda 实现了 Consumer 功能接口：

   whenUsingConsumerInForEach_thenConsumerExecutesForEachListElement()

   消费者还有一些专门版本--DoubleConsumer、IntConsumer 和 LongConsumer--可以接收原始值作为参数。更有趣的是 BiConsumer 接口。其用例之一是遍历 map 的条目：

   whenUsingBiConsumerInForEach_thenConsumerExecutesForEachMapElement()

   另一组专门的 BiConsumer 版本包括 ObjDoubleConsumer、ObjIntConsumer 和 ObjLongConsumer，它们接收两个参数，其中一个参数是生成的，另一个是原始类型。

9. 谓词

   在数理逻辑中，谓词是一个接收值并返回布尔值的函数。

   谓词功能接口是接收生成值并返回布尔值的函数的特化。Predicate lambda 的一个典型用例是过滤值集合：

   whenUsingPredicateInFilter_thenListValuesAreFilteredOut()

   在上面的代码中，我们使用流 API 过滤列表，只保留以字母 "A "开头的名称。谓词实现封装了过滤逻辑。

   与前面的所有示例一样，该函数也有接收基元值的 IntPredicate、DoublePredicate 和 LongPredicate 版本。

10. 操作符

    运算符接口是接收和返回相同值类型的函数的特例。UnaryOperator 接口只接收一个参数。它在集合 API 中的一个用例是用相同类型的计算值替换列表中的所有值：

    `names.replaceAll(name -> name.toUpperCase());`

    List.replaceAll 函数返回 void，因为它会就地替换值。为了达到目的，用于转换 list 值的 lambda 必须返回与接收到的结果类型相同的结果。这就是为什么 UnaryOperator 在这里很有用。

    当然，我们可以简单地使用方法引用来代替 name -> name.toUpperCase()：

    `names.replaceAll(String::toUpperCase);`

    二进制操作符最有趣的用例之一是还原操作。假设我们要将整数集合汇总为所有值的总和。通过流 API，我们可以使用收集器来实现这一目的，但更通用的方法是使用 reduce 方法：

    whenUsingBinaryOperatorWithStreamReduce_thenResultIsSumOfValues()

    reduce 方法接收一个初始累加器值和一个 BinaryOperator 函数。该函数的参数是一对相同类型的值；函数本身还包含将它们合并为一个相同类型值的逻辑。传递的函数必须是关联(associative)函数，这意味着值聚合的顺序并不重要，即以下条件应成立：

    `op.apply(a, op.apply(b, c)) == op.apply(op.apply(a, b), c)`

    二元操作符运算函数的关联属性使我们可以轻松地并行化简过程。

    当然，UnaryOperator 和 BinaryOperator 也有可以与初值一起使用的特殊化，即 DoubleUnaryOperator、IntUnaryOperator、LongUnaryOperator、DoubleBinaryOperator、IntBinaryOperator 和 LongBinaryOperator。

11. 遗留功能接口

    并非所有功能接口都出现在 Java 8 中。Java 以前版本中的许多接口都符合 FunctionalInterface 的约束条件，我们可以将它们用作 lambdas。突出的例子包括并发 API 中使用的 Runnable 和 Callable 接口。在 Java 8 中，这些接口也标有 @FunctionalInterface 注解。这让我们可以大大简化并发代码：

    whenPassingLambdaToThreadConstructor_thenLambdaInferredToRunnable()

12. 结论

    在本文中，我们研究了 Java 8 API 中的不同功能接口，我们可以将其用作 lambda 表达式。

## Lambda 表达式和功能接口：提示和最佳实践

1. 概述

   随着 Java 8 的广泛使用，其一些主要功能的模式和最佳实践也开始出现。在本教程中，我们将仔细研究函数式接口和 lambda 表达式。

   [为什么在 lambda 中使用的局部变量必须是最终变量或有效的最终变量？](https://www.baeldung.com/java-lambda-effectively-final-local-variables)

   了解为什么 Java 要求在 lambda 中使用的局部变量必须是有效最终变量。

   [Java8-利用 Lambda 进行强大的比较](https://www.baeldung.com/java-8-sort-lambda)

   Java8 中的优雅排序-Lambda 表达式超越语法糖，为 Java 带来强大的函数语义。

   测试代码：Java8FunctionalInteracesLambdasUnitTest.java

2. 首选标准功能接口

   [java.util.function](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/function/package-summary.html)包中的函数接口能满足大多数开发人员的需求，为 lambda 表达式和方法引用提供目标类型。每个接口都是通用和抽象的，因此很容易适应几乎所有的 lambda 表达式。开发人员在创建新的函数式接口之前，应该先了解一下这个软件包。

   让我们来看看接口 Foo：

   java8.lambda.tips/Foo.java

   此外，我们在某个类 UseFoo 中有一个方法 add()，该方法以该接口为参数：

   add(String string, Foo foo)

   要执行这个方法，我们可以写

   functionalInterfaceInstantiation_whenReturnDefiniteString_thenCorrect()

   如果我们仔细观察，就会发现 Foo 只不过是一个接受一个参数并产生一个结果的函数。Java 8 已经在 java.util.function 包中的 [Function<T,R>](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/function/Function.html) 中提供了这样一个接口。

   现在，我们可以完全删除接口 Foo，并将 UseFoo 代码改为

   `addWithStandardFI(final String string, final UnaryOperator<String> fn)`

   要执行此操作，我们可以写

   standardFIParameter_whenReturnDefiniteString_thenCorrect()

3. 使用 @FunctionalInterface 注释

   现在，让我们用 [@FunctionalInterface](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/FunctionalInterface.html) 来注解我们的功能接口。起初，这个注解似乎没什么用。即使没有它，只要我们的接口只有一个抽象方法，就会被视为功能接口。

   然而，让我们想象一下一个拥有多个接口的大项目，很难手动控制所有的接口。一个设计为功能性的接口，可能会因为添加了另一个或多个抽象方法而被意外更改，从而无法作为功能性接口使用。

   通过使用 @FunctionalInterface 注解，编译器会对任何试图破坏功能接口预定义结构的行为触发错误。这也是一个非常方便的工具，可以让其他开发人员更容易理解我们的应用程序架构。

   因此，我们可以在 Foo.java 中使用它。

4. 不要在功能接口中过度使用默认方法

    我们可以轻松地在功能接口中添加默认方法。只要只有一个抽象方法声明，这对于功能接口契约来说是可以接受的：见 Foo.java 。

    如果其他功能接口的抽象方法具有相同的签名，则功能接口可以被其他功能接口扩展：

    ```java
    @FunctionalInterface
    public interface FooExtended extends Baz, Bar {}

    @FunctionalInterface
    public interface Baz {
        String method(String string);
        default String defaultBaz() {}
    }

    @FunctionalInterface
    public interface Bar {
        String method(String string);
        default String defaultBar() {}
    }
    ```

    与普通接口一样，使用相同的默认方法扩展不同的功能接口也会有问题。

    例如，让我们为 Bar 和 Baz 接口添加 defaultCommon() 方法。

    在这种情况下，我们会出现编译时错误：

    `interface FooExtended inherits unrelated defaults for defaultCommon() from types Baz and Bar...`

    要解决这个问题，应在 FooExtended 接口中重载 defaultCommon() 方法。我们可以为该方法提供自定义实现；不过，我们也可以重用父接口中的实现。

    需要注意的是，我们必须小心谨慎。在接口中添加过多的默认方法并不是一个很好的架构决策。这应该被视为一种折衷方案，只有在需要升级现有接口而又不破坏向后兼容性时才会使用。

5. 使用 Lambda 表达式实例化功能接口

    编译器允许我们使用内部类来实例化功能接口，但这会导致代码非常冗长。我们更倾向于使用 lambda 表达式：

    `Foo foo = parameter -> parameter + " from Foo";`

    而不是内部类：fooByIC

    lambdaAndInnerClassInstantiation_whenReturnSameString_thenCorrect()

    lambda 表达式方法可用于旧库中任何合适的接口。它适用于 Runnable、Comparator 等接口；但这并不意味着我们应该回顾整个旧代码库并修改所有内容。

6. 避免重载以功能接口为参数的方法

    我们应该使用不同名称的方法，以避免碰撞：

    ```java
    public interface Processor {
        String process(Callable<String> c) throws Exception;
        String process(Supplier<String> s);
    }

    public class ProcessorImpl implements Processor {
        @Override
        public String process(Callable<String> c) throws Exception {
            // implementation details
        }

        @Override
        public String process(Supplier<String> s) {
            // implementation details
        }
    }
    ```

    乍一看，这似乎是合理的，但如果尝试执行 ProcessorImpl 的任何一个方法，都会出现以下问题：

    `String result = processor.process(() -> "abc");`

    都会以错误结束，错误信息如下：

    ```java
    reference to process is ambiguous
    both method process(java.util.concurrent.Callable<java.lang.String>) 
    in com.baeldung.java8.lambda.tips.ProcessorImpl 
    and method process(java.util.function.Supplier<java.lang.String>) 
    in com.baeldung.java8.lambda.tips.ProcessorImpl match
    ```

    要解决这个问题，我们有两个选择。第一个选择是使用不同名称的方法：

    Processor.java

    第二种选择是手动执行铸造，这并不可取：

    `String result = processor.process((Supplier<String>) () -> "abc");`

7. 不要将 lambda 表达式视为内类

    尽管在前面的示例中，我们基本上用 lambda 表达式替代了内类类，但这两个概念在一个重要方面是不同的：scope 作用域。

    当我们使用内层类时，它会创建一个新的作用域。我们可以通过实例化具有相同名称的新局部变量，从外层作用域中隐藏局部变量。我们还可以在内部类中使用关键字 this 作为对其实例的引用。

    然而，Lambda 表达式只适用于外层作用域。我们不能将外层作用域中的变量隐藏在 lambda 主体中。在这种情况下，关键字 this 就是对外层实例的引用。

    例如，在类 UseFoo 中，我们有一个实例变量 value：

    `private String value = "Enclosing scope value";`

    然后，在该类的某个方法中插入 scopeExperiment() 代码并执行该方法，将得到以下结果：

    `Results: resultIC = Inner class value, resultLambda = Enclosing scope value`

    正如我们所看到的，通过在 IC 中调用 this.value，我们可以访问其实例中的局部变量。在 lambda 中，调用 this.value 可以访问变量 value（定义在 UseFoo 类中），但不能访问在 lambda 主体中定义的变量 value。

8. 让 lambda 表达式简短且不言自明

    如果可能，我们应该使用一行结构，而不是一大段代码。请记住，lambda 应该是一种表达式，而不是一种叙述。尽管语法简洁，但 lambdas 应具体表达其提供的功能。

    这主要是风格上的建议，因为性能不会有很大变化。不过，一般来说，这样的代码更容易理解和使用。

    实现这一点的方法有很多，让我们仔细看看。

    1. 避免在 lambda 主体中出现代码块

        在理想情况下，lambda 应该以一行代码的形式编写。在这种情况下，lambda 是一种不言自明的结构，它声明了应使用哪些数据（如果是带参数的 lambdas）执行哪些操作。

        如果我们有一大段代码，lambda 的功能就无法立即明确。

        考虑到这一点，请执行以下操作：

        shorteningLambdas_whenReturnEqualsResults_thenCorrect()

        需要注意的是，我们不应该把 "one-line lambda" 规则当作教条。如果 lambda 的定义中有两三行代码，那么将这些代码提取到另一个方法中可能就没有价值了。

    2. 避免指定参数类型

        编译器在大多数情况下都能通过[类型推断](https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html)来确定 lambda 参数的类型。因此，为参数添加类型是可选的，可以省略。

        我们可以这样做

        (a, b) -> a.toLowerCase() + b.toLowerCase();

        而不是这样

        (String a, String b) -> a.toLowerCase() + b.toLowerCase();

    3. 避免在单个参数周围使用括号

        Lambda 语法只要求在多个参数或没有参数时使用括号。因此，我们可以将代码缩短一点，在只有一个参数时不使用括号。

        因此，我们可以这样做

        a -> a.toLowerCase();

        而不是这样

        (a) -> a.toLowerCase();

    4. 避免使用返回语句和括号

        在单行 lambda body 中，括号和返回语句是可选的。这意味着，为了清晰和简洁，可以省略它们。

        我们可以这样做

        a -> a.toLowerCase();

        而不是这样

        a -> {return a.toLowerCase()};

    5. 使用方法引用

        很多时候，即使在我们前面的示例中，lambda 表达式也只是调用其他地方已经实现的方法。在这种情况下，使用 Java 8 的另一个特性 - [方法引用](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html)就非常有用了。

        lambda 表达式如下

        a -> a.toLowerCase();

        我们可以用

        String::toLowerCase;

        这样做不一定更简短，但会使代码更易读。

9. 使用 "有效最终" 变量

    在 lambda 表达式中访问非最终变量会导致编译时错误，但这并不意味着我们应该将每个目标变量都标记为最终变量。

    根据 "[Effectively Final](https://docs.oracle.com/javase/tutorial/java/javaOO/localclasses.html)" 的概念，编译器会将每个变量视为最终变量，只要它只被赋值一次。

    在 lambdas 中使用此类变量是安全的，因为编译器会控制它们的状态，并在试图更改它们时立即触发编译时错误。

    例如，以下代码将无法编译：

    ```java
    public void method() {
        String localVariable = "Local";
        Foo foo = parameter -> {
            String localVariable = parameter;
            return localVariable;
        };
    }
    ```

    编译器会通知我们

    变量 "localVariable" 已在作用域中定义。

    `Variable 'localVariable' is already defined in the scope.`

    这种方法可以简化 lambda 执行的线程安全过程。

10. 保护对象变量免于突变

    lambda 的主要用途之一是用于并行计算，这意味着它们对线程安全非常有帮助。

    "effectively final" 范式在这方面有很大帮助，但并非在所有情况下都如此。Lambdas 无法改变外层作用域中对象的值。但在对象变量可变的情况下，状态可以在 lambda 表达式中改变。

    请看代码 mutatingOfEffectivelyFinalVariable_whenNotEquals_thenCorrect()

    这段代码是合法的，因为 total 变量仍然是 "有效的最终变量"，但在执行 lambda 之后，它所引用的对象会有相同的状态吗？不会！

    请保留此示例，提醒自己避免使用可能导致意外突变的代码。

11. 结论

    在本文中，我们探讨了 Java 8 lambda 表达式和函数式接口的一些最佳实践和陷阱。尽管这些新功能非常实用和强大，但它们只是工具而已。每个开发人员在使用它们时都应注意。

## Java 中的方法引用

1. 概述

   Java 8 中最受欢迎的变化之一是引入了 [lambda](https://www.baeldung.com/java-8-lambda-expressions-tips) 表达式，因为它允许我们放弃匿名类，从而大大减少了模板代码并提高了可读性。

   方法引用是 lambda 表达式的一种特殊类型。它们通常用于通过引用现有的方法来创建简单的 lambda 表达式。

   有四种方法引用：

   - 静态方法
   - 特定对象的实例方法
   - 特定类型的任意对象的实例方法
   - 构造函数

   在本教程中，我们将探讨 Java 中的方法引用。

2. 静态方法引用

   我们将从一个非常简单的示例开始，大写并打印一个字符串列表：

   `List<String> messages = Arrays.asList("hello", "baeldung", "readers!");`

   我们可以通过直接调用 [StringUtils.capitalize()](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html#capitalize-java.lang.String-) 方法的简单 lambda 表达式来实现这一功能：

   `messages.forEach(word -> StringUtils.capitalize(word));`

   或者，我们可以使用方法引用来简单地引用静态大写方法：

   `messages.forEach(StringUtils::capitalize);`

   请注意，方法引用总是使用 :: 操作符。

3. 对特定对象实例方法的引用

   为了演示这种方法引用类型，让我们考虑两个类：

   ```java
   public class Bicycle {
       private String brand;
       private Integer frameSize;
       // standard constructor, getters and setters
   }

   public class BicycleComparator implements Comparator {
       @Override
       public int compare(Bicycle a, Bicycle b) {
           return a.getFrameSize().compareTo(b.getFrameSize());
       }
   }
   ```

   让我们创建一个 BicycleComparator 对象来比较自行车车架尺寸：

   `BicycleComparator bikeFrameSizeComparator = new BicycleComparator();`

   我们可以使用 lambda 表达式按车架尺寸对自行车进行排序，但需要指定两辆自行车进行比较：

   `createBicyclesList().stream().sorted((a, b) -> bikeFrameSizeComparator.compare(a, b));`

   相反，我们可以使用方法引用，让编译器为我们处理参数传递：

   `createBicyclesList().stream().sorted(bikeFrameSizeComparator::compare);`

   方法引用更加简洁易读，因为我们的意图已通过代码清楚地显示出来。

4. 对特定类型的任意对象的实例方法的引用

   这种方法引用与上例类似，但无需创建自定义对象来执行比较。

   让我们创建一个要排序的整数列表：

   `List<Integer> numbers = Arrays.asList(5, 3, 50, 24, 40, 2, 9, 18);`

   如果使用经典的 lambda 表达式，两个参数都需要显式传递，而使用方法引用则简单得多：

   ```java
   numbers.stream().sorted((a, b) -> a.compareTo(b));
   numbers.stream().sorted(Integer::compareTo);
   ```

   虽然仍是单行，但方法引用更易于阅读和理解。

5. 引用构造函数

   我们引用构造函数的方式与第一个示例中引用静态方法的方式相同。唯一不同的是，我们将使用 new 关键字。

   让我们从一个包含不同品牌的字符串列表中创建一个自行车数组：

   `List<String> bikeBrands = Arrays.asList("Giant", "Scott", "Trek", "GT");`

   首先，我们为自行车类添加一个新的构造函数：

   ```java
   public Bicycle(String brand) {
       this.brand = brand;
       this.frameSize = 0;
   }
   ```

   接下来，我们将使用方法引用中的新构造函数，并从原始字符串列表中创建一个 Bicycle 数组：

   ```java
   bikeBrands.stream()
   .map(Bicycle::new)
   .toArray(Bicycle[]::new);
   ```

   请注意我们是如何使用方法引用调用自行车和数组构造函数的，从而使我们的代码看起来更加简洁明了。

6. 其他示例和限制

   正如我们到目前为止所看到的，方法引用是使我们的代码和意图非常清晰易读的好方法。但是，我们不能用它来代替所有的 lambda 表达式，因为它有一些局限性。

   方法引用的主要限制来自于它的最大优势：前一个表达式的输出必须与引用方法签名的输入参数相匹配。

   让我们看一个例子来说明这种限制：

   ```java
   createBicyclesList().forEach(b -> System.out.printf(
   "Bike brand is '%s' and frame size is '%d'%n",
   b.getBrand(),
   b.getFrameSize()));
   ```

   这个简单的例子不能用方法引用来表达，因为在我们的例子中，printf 方法需要 3 个参数，而使用 createBicyclesList().forEach() 方法引用只能推断出一个参数（自行车对象）。

   最后，让我们来探讨如何创建一个可以从 lambda 表达式引用的无操作函数。

   在这种情况下，我们希望使用 lambda 表达式而不使用其参数。

   首先，让我们创建 doNothingAtAll 方法：

   `private static <T> void doNothingAtAll(Object... o) {}`

   由于它是一个 [varargs](https://www.baeldung.com/java-varargs) 方法，因此可以在任何 lambda 表达式中使用，无论引用的对象或推断的参数数量是多少。

   现在，让我们看看它的实际效果：

   `createBicyclesList().forEach((o) -> MethodReferenceExamples.doNothingAtAll(o));`

7. 总结

   在这篇快速教程中，我们了解了 Java 中的方法引用是什么，以及如何使用它们来替代 lambda 表达式，从而提高可读性并明确程序员的意图。

## Relevant articles

- [x] [Why Do Local Variables Used in Lambdas Have to Be Final or Effectively Final?](https://www.baeldung.com/java-lambda-effectively-final-local-variables)
- [x] [Java 8 – Powerful Comparison with Lambdas](http://www.baeldung.com/java-8-sort-lambda)
- [x] [Functional Interfaces in Java 8](http://www.baeldung.com/java-8-functional-interfaces)
- [x] [Lambda Expressions and Functional Interfaces: Tips and Best Practices](http://www.baeldung.com/java-8-lambda-expressions-tips)
- [Exceptions in Java 8 Lambda Expressions](http://www.baeldung.com/java-lambda-exceptions)
- [ ] [Method References in Java](https://www.baeldung.com/java-method-references)
- [The Double Colon Operator in Java 8](https://www.baeldung.com/java-8-double-colon-operator)
- [Serialize a Lambda in Java](https://www.baeldung.com/java-serialize-lambda)
- [Convert Anonymous Class into Lambda in Java](https://www.baeldung.com/java-from-anonymous-class-to-lambda)
- [When to Use Callable and Supplier in Java](https://www.baeldung.com/java-callable-vs-supplier)

## Code

本文介绍的所有代码均可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lambdas) 上获取。
