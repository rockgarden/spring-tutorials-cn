# 核心 Java 流

本模块包含有关 Java 中流 API 的文章。

## Java8流API教程

1. 概述

    在本综合教程中，我们将介绍 Java 8 Streams 从创建到并行执行的实际应用。

    要理解本资料，读者需要具备 Java 8（lambda 表达式、可选、方法引用）和流 API 的基本知识。为了更加熟悉这些主题，请阅读我们以前的文章： [Java 8 中的新特性](https://www.baeldung.com/java-8-new-features)和《Java 8 流简介》。

    [Lambda表达式和功能接口：提示和最佳实践](https://www.baeldung.com/java-8-lambda-expressions-tips)

    使用 Java 8 lambda 表达式和功能接口的技巧和最佳实践。

    [Java 8 收集器指南](https://www.baeldung.com/java-8-collectors)

    文章讨论了 Java 8 收集器，展示了内置收集器的示例，并介绍了如何构建自定义收集器。

    测试代码：Java8StreamApiUnitTest.java

2. 创建流

    有许多方法可以创建不同来源的流实例。一旦创建，实例将不会修改其源，因此允许从一个源创建多个实例。

    1. 空流

        如果要创建一个空流，我们应该使用 empty() 方法：

        `Stream<String> streamEmpty = Stream.empty();`

        我们经常在创建时使用 empty() 方法，以避免在没有元素的流中返回 null：

        streams/Product.java: streamOf()

    2. 集合流

        我们还可以为任何类型的集合（Collection、List、Set）创建流：

        ```java
        Collection<String> collection = Arrays.asList("a", "b", "c");
        Stream<String> streamOfCollection = collection.stream();
        ```

    3. 数组流

        数组也可以是流的源：

        `Stream<String> streamOfArray = Stream.of("a", "b", "c");`

        我们还可以从现有数组或数组的一部分创建流：

        ```java
        String[] arr = new String[]{"a", "b", "c"};
        Stream<String> streamOfArrayFull = Arrays.stream(arr);
        Stream<String> streamOfArrayPart = Arrays.stream(arr, 1, 3);
        ```

    4. Stream.builder()

        使用生成器时，应在语句的右侧部分额外指定所需的类型，否则 build() 方法将创建一个 `Stream<Object>` 的实例：

        ```java
        Stream<String> streamBuilder =
          Stream.<String>builder().add("a").add("b").add("c").build();
        ```

    5. Stream.generate()

        generate() 方法接受一个 `Supplier<T>` 来生成元素。由于生成的流是无限的，开发人员应指定所需的大小，否则 generate() 方法将一直工作到达到内存限制为止：

        `Stream<String> streamGenerated = Stream.generate(() -> "element").limit(10);`

        上面的代码创建了一个由十个字符串组成的序列，其值为 "element"。

    6. Stream.iterate()

        另一种创建无限流的方法是使用 iterate() 方法：

        `Stream<Integer> streamIterated = Stream.iterate(40, n -> n + 2).limit(20);`

        结果流的第一个元素是 iterate() 方法的第一个参数。在创建接下来的每个元素时，指定函数将应用于前一个元素。在上面的示例中，第二个元素将是 42。

    7. 基元流

        Java 8 提供了用三种基元类型创建流的可能性：int、long 和 double。由于 `Stream<T>` 是一个泛型接口，而在泛型中无法使用基元作为类型参数，因此创建了三个新的特殊接口： IntStream、LongStream 和 DoubleStream。

        使用新接口可以减少不必要的自动包装(auto-boxing)，从而提高工作效率：

        ```java
        IntStream intStream = IntStream.range(1, 3);
        LongStream longStream = LongStream.rangeClosed(1, 3);
        ```

        range(int startInclusive, int endExclusive) 方法创建一个从第一个参数到第二个参数的有序流。其结果不包括最后一个参数，只是序列的上限。

        rangeClosed(int startInclusive, int endInclusive) 方法做了同样的事情，只有一个区别，即包含了第二个元素。我们可以使用这两种方法生成三种类型的基元流中的任何一种。

        自 Java 8 起，[Random](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Random.html) 类提供了大量用于生成基元流的方法。例如，下面的代码创建了一个有三个元素的 DoubleStream：

        ```java
        Random random = new Random();
        DoubleStream doubleStream = random.doubles(3);
        ```

    8. 字符串流

        借助 String 类的 chars() 方法，我们还可以使用 String 作为创建流的源。由于 JDK 中没有 CharStream 接口，我们使用 IntStream 来表示字符流。

        `IntStream streamOfChars = "abc".chars();`

        下面的示例根据指定的 RegEx 将字符串分解为子串：

        `Stream<String> streamOfString = Pattern.compile(", ").splitAsStream("a, b, c");`

    9. 文件流

        此外，Java NIO 类 Files 允许我们通过 lines() 方法生成文本文件的 `Stream<String>`。文本的每一行都会成为流的一个元素：

        ```java
        Path path = Paths.get("C:\\file.txt");
        Stream<String> streamOfStrings = Files.lines(path);
        Stream<String> streamWithCharset = 
          Files.lines(path, Charset.forName("UTF-8"));
        ```

        可以将 Charset 指定为 lines() 方法的参数。

3. 引用流

    只要只调用中间操作，我们就可以实例化一个流，并对其进行可访问引用。执行终端操作会导致流不可访问。

    为了演示这一点，我们将暂时忘记最好的做法是链式操作顺序。除了不必要的冗长之外，从技术上讲，以下代码也是有效的：

    ```java
    Stream<String> stream = Stream.of("a", "b", "c").filter(element -> element.contains("b"));
    Optional<String> anyElement = stream.findAny();
    ```

    但是，如果在调用终端操作后试图重复使用同一引用，则会触发 IllegalStateException：

    `Optional<String> firstElement = stream.findFirst();`

    由于 IllegalStateException 是运行时异常，编译器不会发出问题信号。因此，记住 Java 8 的流不能重复使用是非常重要的。

    这种行为是合乎逻辑的。我们设计流的目的是以函数式的方式对元素源应用有限的操作序列，而不是存储元素。

    因此，要使前面的代码正常工作，需要做一些改动：

    ```java
    List<String> elements =
      Stream.of("a", "b", "c").filter(element -> element.contains("b"))
        .collect(Collectors.toList());
    Optional<String> anyElement = elements.stream().findAny();
    Optional<String> firstElement = elements.stream().findFirst();
    ```

4. 流流水线(Stream Pipeline)

    要对数据源元素执行一系列操作并汇总其结果，我们需要三个部分：源、中间操作和终端操作。

    中间操作会返回一个新的修改流。例如，要在现有数据流的基础上创建一个不含少量元素的新数据流，应使用 skip() 方法：

    ```java
    Stream<String> onceModifiedStream =
      Stream.of("abcd", "bbcd", "cbcd").skip(1);
    ```

    如果我们需要不止一次修改，可以采用链式中间操作。假设我们还需要将当前 `Stream<String>` 的每个元素替换为包含前几个字符的子字符串。我们可以通过连锁使用 skip() 和 map() 方法来实现这一目的：

    ```java
    Stream<String> twiceModifiedStream =
      stream.skip(1).map(element -> element.substring(0, 3));
    ```

    我们可以看到，map() 方法将 lambda 表达式作为参数。

    流本身没有价值；用户感兴趣的是终端操作的结果，它可以是某个类型的值，也可以是应用于流中每个元素的操作。每个数据流只能使用一个终端操作。

    使用流的正确和最方便的方法是使用流流水线，它是一个由流源、中间操作和终端操作组成的链：

    ```java
    List<String> list = Arrays.asList("abc1", "abc2", "abc3");
    long size = list.stream().skip(1)
      .map(element -> element.substring(0, 3)).sorted().count();
    ```

5. 懒调用(Lazy Invocation)

    中间操作是懒惰的。这意味着只有在执行终端操作时才会调用它们。

    例如，让我们调用 wasCalled() 方法，该方法每次调用都会递增一个内部计数器：

    ```java
    private long counter;
    private void wasCalled() {
        counter++;
    }
    ```

    现在让我们调用操作 filter() 中的 wasCalled() 方法：

    ```java
    List<String> list = Arrays.asList(“abc1”, “abc2”, “abc3”);
    counter = 0;
    Stream<String> stream = list.stream().filter(element -> {
        wasCalled();
        return element.contains("2");
    });
    ```

    由于我们有一个包含三个元素的源，因此可以假设 filter() 方法将被调用三次，计数器变量的值将是 3。然而，运行这段代码后，计数器没有任何变化，仍然是 0，因此 filter() 方法甚至没有被调用一次。原因是缺少终端操作。

    让我们重写一下这段代码，添加 map() 操作和终端操作 findFirst()。我们还将添加借助日志跟踪方法调用顺序的功能：

    ```java
    Optional<String> stream = list.stream().filter(element -> {
        log.info("filter() was called");
        return element.contains("2");
    }).map(element -> {
        log.info("map() was called");
        return element.toUpperCase();
    }).findFirst();
    ```

    结果日志显示，我们调用了两次 filter() 方法和一次 map() 方法。这是因为管道是垂直执行的。在我们的示例中，流的第一个元素不满足过滤器的谓词。然后，我们调用了第二个元素的 filter() 方法，它通过了过滤器。在不调用第三个元素的 filter() 方法的情况下，我们通过流水线进入 map() 方法。

    findFirst() 操作只满足一个元素。因此，在这个例子中，懒调用让我们避免了两次方法调用，一次是 filter() 方法，一次是 map() 方法。

6. 执行顺序

    从性能的角度来看，正确的顺序是流流水线中链式操作最重要的方面之一：

    ```java
    long size = list.stream().map(element -> {
        wasCalled();
        return element.substring(0, 3);
    }).skip(2).count();
    ```

    执行此代码后，计数器的值将增加 3。这意味着我们调用了三次流的 map() 方法，但 size 的值是 1。因此，结果流中只有一个元素，而我们在三次中的两次都无缘无故地执行了昂贵的 map() 操作。

    如果我们改变 skip() 和 map() 方法的顺序，计数器将只增加一个。因此，我们将只调用一次 map() 方法：

    ```java
    long size = list.stream().skip(2).map(element -> {
        wasCalled();
        return element.substring(0, 3);
    }).count();
    ```

    这就引出了以下规则：减少流大小的中间操作应放在应用于每个元素的操作之前。因此，我们需要将 skip()、filter() 和 distinct() 等方法放在流流水线的顶端。

7. 流缩减

    API 有许多终端操作，可以将流聚合为一种类型或一个基元：count()、max()、min() 和 sum()。不过，这些操作都是按照预定义的实现方式进行的。那么，如果开发人员需要自定义流的还原机制，该怎么办呢？有两种方法允许我们这样做，即 reduce() 和 collect() 方法。

    1. reduce()方法

        该方法有三种变体，它们的签名和返回类型各不相同。它们的参数如下

        identity - 累加器的初始值，如果数据流是空的，没有什么可累加的，则为默认值

        accumulator（累加器） - 指定元素聚合逻辑的函数。由于累加器每减少一步就会创建一个新值，因此新值的数量等于数据流的大小，只有最后一个值才有用。这对性能影响很大。

        combiner - 一个汇总累加器结果的函数。我们只在**并行模式**下调用 combiner 来减少来自不同线程的累加器结果。

        现在让我们来看看这三种方法的实际应用：

        `OptionalInt reduced = IntStream.range(1, 4).reduce((a, b) -> a + b);`

        reduced = 6 (1 + 2 + 3)

        `int reducedTwoParams = IntStream.range(1, 4).reduce(10, (a, b) -> a + b);`

        reducedTwoParams = 16 (10 + 1 + 2 + 3)

        ```java
        int reducedParams = Stream.of(1, 2, 3)
        .reduce(10, (a, b) -> a + b, (a, b) -> {
            log.info("combiner was called");
            return a + b;
        });
        ```

        结果与上一个示例（16）相同，没有login，这意味着组合器没有被调用。要使组合器工作，流应该是并行的：

        ```java
        int reducedParallel = Arrays.asList(1, 2, 3).parallelStream()
            .reduce(10, (a, b) -> a + b, (a, b) -> {
            log.info("combiner was called");
            return a + b;
            });
        ```

        这里的结果不同（36），组合器被调用了两次。这里的还原是通过以下算法进行的：累加器运行了三次，将数据流中的每个元素都加到同一值上。这些操作是并行进行的。结果是（10 + 1 = 11；10 + 2 = 12；10 + 3 = 13；）。现在，组合器可以合并这三个结果。这需要两次迭代（12 + 13 = 25; 25 + 11 = 36）。

    2. collect()方法

        对流的还原也可以通过另一个终端操作，即 collect() 方法来执行。它接受一个收集器类型的参数，该参数指定了还原机制。大多数常见操作都有已创建的预定义收集器。可以借助收集器类型访问它们。

        在本节中，我们将使用以下 List 作为所有流的源：

        ```java
        List<Product> productList = Arrays.asList(new Product(23, "potatoes"),
        new Product(14, "orange"), new Product(13, "lemon"),
        new Product(23, "bread"), new Product(13, "sugar"));
        ```

        将数据流转换为集合（Collection、List 或 Set）：

        ```java
        List<String> collectorCollection = 
          productList.stream().map(Product::getName).collect(Collectors.toList());
        ```

        还原为字符串：

        ```java
        String listToString = productList.stream().map(Product::getName)
          .collect(Collectors.joining(", ", "[", "]"));
        ```

        joiner() 方法可以有一到三个参数（分隔符、前缀、后缀）。使用 joiner() 方法最方便的地方在于，开发人员无需检查数据流是否到达终点，也无需应用后缀和分隔符。收集器会处理这些工作。

        处理数据流中所有数字元素的平均值：

        ```java
        double averagePrice = productList.stream()
          .collect(Collectors.averagingInt(Product::getPrice));
        ```

        处理数据流中所有数字元素的总和：

        ```java
        int summingPrice = productList.stream()
          .collect(Collectors.summingInt(Product::getPrice));
        ```

        方法 averagingXX()、summingXX() 和 summarizingXX() 可以处理基元（int、long、double）及其包装类（Integer、Long、Double）。这些方法的另一个强大功能是提供映射。因此，开发人员无需在 collect() 方法之前使用额外的 map() 操作。

        收集流元素的统计信息：

        ```java
        IntSummaryStatistics statistics = productList.stream()
          .collect(Collectors.summarizingInt(Product::getPrice));
        ```

        通过使用生成的 IntSummaryStatistics 类型实例，开发人员可以应用 toString() 方法创建统计报告。结果将是一个字符串，与此字符串 "IntSummaryStatistics{count=5, sum=86, min=13, average=17,200000, max=23}" 相同。

        通过使用 getCount()、getSum()、getMin()、getAverage() 和 getMax() 方法，也很容易从该对象中提取计数、总和、最小值和平均值的单独值。所有这些值都可以从一个管道中提取。

        根据指定函数对流元素进行分组：

        ```java
        Map<Integer, List<Product>> collectorMapOfLists = productList.stream()
          .collect(Collectors.groupingBy(Product::getPrice));
        ```

        在上面的示例中，数据流被简化为 Map，Map 根据价格对所有产品进行分组。

        根据某个谓词将流元素分成若干组：

        ```java
        Map<Boolean, List<Product>> mapPartioned = productList.stream()
          .collect(Collectors.partitioningBy(element -> element.getPrice() > 15));
        ```

        推动收集器执行附加转换：

        ```java
        Set<Product> unmodifiableSet = productList.stream()
          .collect(Collectors.collectingAndThen(Collectors.toSet(),
          Collections::unmodifiableSet));
        ```

        在这种特殊情况下，收集器将流转换为集合，然后从中创建了不可更改的集合。

        自定义收集器：

        如果出于某种原因需要创建自定义收集器，最简单、最省事的方法就是使用收集器类型的方法 of()。

        ```java
        Collector<Product, ?, LinkedList<Product>> toLinkedList =
        Collector.of(LinkedList::new, LinkedList::add, 
            (first, second) -> { 
            first.addAll(second); 
            return first; 
            });
        LinkedList<Product> linkedListOfPersons =
        productList.stream().collect(toLinkedList);
        ```

        在这个示例中，收集器的实例被简化为 `LinkedList<Persone>`。

8. 并行流

    在 Java 8 之前，并行化非常复杂。[ExecutorService](https://www.baeldung.com/java-executor-service-tutorial) 和 [ForkJoin](https://www.baeldung.com/java-fork-join) 的出现稍微简化了开发人员的工作，但如何创建特定的执行器、如何运行执行器等问题仍然值得记忆。Java 8 引入了一种以函数式风格实现并行的方法。

    API 允许我们创建并行流，以并行模式执行操作。当流的源是集合或数组时，可以借助 parallelStream() 方法来实现：

    ```java
    Stream<Product> streamOfCollection = productList.parallelStream();
    boolean isParallel = streamOfCollection.isParallel();
    boolean bigPrice = streamOfCollection
      .map(product -> product.getPrice() * 12)
      .anyMatch(price -> price > 200);
    ```

    如果流的源不是集合或数组，则应使用 parallel() 方法：

    ```java
    IntStream intStreamParallel = IntStream.range(1, 150).parallel();
    boolean isParallel = intStreamParallel.isParallel();
    ```

    在引擎盖下，Stream API 会自动使用 ForkJoin 框架来并行执行操作。默认情况下，将使用通用线程池，无法（至少目前无法）为其分配自定义线程池。[这可以通过使用一组自定义的并行收集器来解决](https://github.com/pivovarit/parallel-collectors)。

    在并行模式下使用流时，应避免阻塞操作。当任务执行时间相近时，最好也使用并行模式。如果一个任务持续的时间比另一个任务长很多，就会拖慢整个应用程序的工作流程。

    并行模式下的流可以通过 sequential() 方法转换回顺序模式：

    ```java
    IntStream intStreamSequential = intStreamParallel.sequential();
    boolean isParallel = intStreamSequential.isParallel();
    ```

9. 结论

    Stream API 是一套功能强大但简单易懂的工具，用于处理元素序列。如果使用得当，它能让我们减少大量模板代码，创建更可读的程序，并提高应用程序的工作效率。

    在本文展示的大部分代码示例中，我们都没有使用流（我们没有使用 close() 方法或终端操作）。在实际应用程序中，不要让实例化的流处于未调用状态，否则会导致内存泄漏。

## Java8流简介

1. 概述

    在本文中，我们将快速了解 Java 8 新增的主要功能之一 - 流。

    我们将解释流的含义，并通过简单的示例展示流的创建和基本操作。

    代码参见：streams/Java8StreamsUnitTest.java

2. 流 API

    Java 8 的主要新功能之一是引入了流功能--[java.util.stream](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/package-summary.html)，其中包含用于处理元素序列的类。

    其核心 API 类是 [`Stream<T>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Stream.html)。

    1. 创建流

        借助 stream() 和 of() 方法，可以从不同的元素源（如集合或数组）创建流：

        ```java
        String[] arr = new String[]{"a", "b", "c"};
        Stream<String> stream = Arrays.stream(arr);
        stream = Stream.of("a", "b", "c");
        ```

        集合接口中添加了一个 stream() 默认方法，允许使用任何集合作为元素源创建一个 `Stream<T>`：

        `Stream<String> stream = list.stream();`

    2. 使用流进行多线程处理

        流 API 还通过提供 parallelStream() 方法简化了多线程操作，该方法可在并行模式下对流的元素运行操作。

        下面的代码允许对流的每个元素并行运行 doWork() 方法：

        `list.parallelStream().forEach(element -> doWork(element));`

3. 流操作

    对流可以执行许多有用的操作。

    它们分为中间操作（返回 `Stream<T>`）和终端操作（返回确定类型的结果）。中间操作允许链式操作。

    值得注意的是，对流的操作不会改变源。

    下面是一个快速示例

    `long count = list.stream().distinct().count();`

    因此，distinct() 方法代表一个中间操作，它创建了一个包含前一个流中唯一元素的新流。而 count() 方法是一个终端操作，返回流的大小。

    1. 迭代

        流 API 可以替代 for、for-each 和 while 循环。它允许将注意力集中在操作的逻辑上，而不是元素序列的迭代上。例如

        ```java
        for (String string : list) {
            if (string.contains("a")) {
                return true;
            }
        }
        ```

        只需一行 Java 8 代码即可更改此代码：

        `boolean isExist = list.stream().anyMatch(element -> element.contains("a"));`

    2. 过滤

        使用 filter() 方法，我们可以挑选出满足谓词的元素流。

        例如，请看下面的列表：Java8StreamsUnitTest.init()

        下面的代码创建了一个 `List<String>` 的 `Stream<String>`，查找该流中包含字符 "d" 的所有元素，并创建了一个只包含过滤后元素的新流：

        `Stream<String> stream = list.stream().filter(element -> element.contains("d"));`

    3. 映射

        我们可以使用 map() 方法，通过应用特殊函数转换流中的元素，并将这些新元素收集到流中：

        ```java
        List<String> uris = new ArrayList<>();
        uris.add("C:\\My.txt");
        Stream<Path> stream = uris.stream().map(uri -> Paths.get(uri));
        ```

        因此，上面的代码通过对初始流的每个元素应用特定的 lambda 表达式，将 `Stream<String>` 转换为 `Stream<Path>`。

        如果您有一个流，其中每个元素都包含自己的元素序列，并且您想创建一个包含这些内部元素的流，则应使用 flatMap() 方法：

        ```java
        List<Detail> details = new ArrayList<>();
        details.add(new Detail());
        Stream<String> stream
        = details.stream().flatMap(detail -> detail.getParts().stream());
        ```

        在这个示例中，我们有一个 Detail 类型的元素列表。Detail 类包含一个字段 PARTS，它是一个 `List<String>`。在 flatMap() 方法的帮助下，字段 PARTS 中的每个元素都将被提取并添加到新生成的流中。之后，初始的 `Stream<Detail>` 将丢失。

    4. 匹配

        流 API 提供了一套方便的工具，用于根据某个谓词验证序列中的元素。为此，可以使用以下方法之一：anyMatch()、allMatch()、noneMatch()。它们的名称不言自明。这些都是返回布尔值的终端操作：

        ```java
        boolean isValid = list.stream().anyMatch(element -> element.contains("h")); // true
        boolean isValidOne = list.stream().allMatch(element -> element.contains("h")); // false
        boolean isValidTwo = list.stream().noneMatch(element -> element.contains("h")); // false
        ```

        对于空流，带有任何给定谓词的 allMatch() 方法将返回 true：

        `Stream.empty().allMatch(Objects::nonNull); // true`

        这是一个合理的默认值，因为我们找不到任何不满足谓词的元素。

        同样，对于空流，anyMatch() 方法总是返回 false：

        `Stream.empty().anyMatch(Objects::nonNull); // false`

        同样，这也是合理的，因为我们找不到满足这一条件的元素。

    5. 还原

        在 Stream 类型的 reduce() 方法的帮助下，Stream API 允许根据指定函数将元素序列还原为某个值。该方法需要两个参数：第一个参数是起始值，第二个参数是累加器函数。

        试想一下，如果您有一个 `List<Integer>`，并希望将所有这些元素与某个初始整数（本例中为 23）相加。因此，您可以运行以下代码，结果将是 26（23 + 1 + 1 + 1）。

        ```java
        List<Integer> integers = Arrays.asList(1, 1, 1);
        Integer reduced = integers.stream().reduce(23, (a, b) -> a + b);
        ```

    6. 收集

        流类型的 collect() 方法也可以提供还原。在将流转换为集合或 Map 以及以单个字符串的形式表示流时，该操作非常方便。收集器（Collectors）这一实用类为几乎所有典型的收集操作提供了解决方案。对于一些非同小可的任务，可以创建自定义的收集器。

        ```java
        List<String> resultList 
        = list.stream().map(element -> element.toUpperCase()).collect(Collectors.toList());
        ```

        此代码使用终端 collect() 操作将 `Stream<String>` 还原为 `List<String>`。

4. 结论

    在本文中，我们简要介绍了 Java 流--这绝对是 Java 8 最有趣的功能之一。

    使用流还有很多更高级的示例；本文的目的只是提供一个快速实用的介绍，让您了解如何开始使用该功能，并作为探索和进一步学习的起点。

## 使用数据流处理Map

1. 简介

    在本教程中，我们将讨论如何使用 [Java Streams](https://www.baeldung.com/java-8-streams-introduction) 处理Map的一些示例。值得注意的是，其中一些练习可以使用双向 [Map](https://www.baeldung.com/java-hashmap) 数据结构来解决，但我们感兴趣的是函数式方法。

    首先，我们将解释使用 Maps 和 Streams 的基本思想。然后，我们将介绍几个与Map相关的不同问题以及使用 Streams 的具体解决方案。

    [使用 Java 8 合并两个Map](https://www.baeldung.com/java-merge-maps)

    学习在 Java 8 中合并Map的不同技术

    [Java 8 收集器处理 Map](https://www.baeldung.com/java-collectors-tomap)

    了解如何使用收集器类的 toMap() 方法。

    [Java 8 流 API 教程](https://www.baeldung.com/java-8-streams)

    本文通过大量示例介绍了 Java 8 Stream API 提供的可能性和操作。

2. 基本思想

    首先要注意的是，流是元素序列，可以很容易地从集合（Collection）中获取。

    映射具有不同的结构，它是键到值的映射，没有序列。但是，这并不意味着我们不能将映射结构转换成不同的序列，这样我们就能以自然的方式使用流 API。

    让我们来看看从 Map 获取不同集合的方法，然后我们可以将这些集合转为流：

    `Map<String, Integer> someMap = new HashMap<>();`

    我们可以获得一组键值对：

    `Set<Map.Entry<String, Integer>> entries = someMap.entrySet();`

    我们还可以获取与 Map 关联的键集：

    `Set<String> keySet = someMap.keySet();`

    或者，我们可以直接使用值集：

    `Collection<Integer> values = someMap.values();`

    这些方法都为我们提供了一个入口点，通过从这些集合中获取流来处理这些集合：

    ```java
    Stream<Map.Entry<String, Integer>> entriesStream = entries.stream();
    Stream<Integer> valuesStream = values.stream();
    Stream<String> keysStream = keySet.stream();
    ```

3. 使用流获取映射的键值

    代码参见：StreamMapUnitTest.java

    1. 输入数据

        假设我们有一个 Map：

        setup()

        我们希望找到名为 "Effective Java" 一书的 ISBN。

    2. 检索匹配

        由于书名不可能存在于我们的 Map 中，因此我们希望能够表明没有相关的 ISBN。我们可以使用[Optional](https://www.baeldung.com/java-optional)来表达这一点：

        在这个示例中，我们假设对与书名匹配的图书的任何键都感兴趣：

        whenOptionalVersionCalledForExistingTitle_thenReturnOptionalWithISBN()

        让我们分析一下代码。首先，如前所述，我们从 Map 中获取 entrySet。

        我们只考虑标题为 "Effective Java" 的条目，因此第一个中间操作是过滤。

        我们感兴趣的不是整个 Map 条目，而是每个条目的 key。因此，下一个链式中间操作就是这样做的：它是一个映射操作，将生成一个新的流作为输出，其中只包含与我们正在寻找的标题相匹配的条目的键值。

        由于我们只想要一个结果，因此可以应用 findFirst() 终端操作，它将在流中提供作为可选对象的初始值。

        让我们来看看标题不存在的情况：

        whenOptionalVersionCalledForNonExistingTitle_thenReturnEmptyOptionalForISBN()

    3. 检索多个结果

        现在，让我们改变一下问题，看看如何处理返回多个结果而不是一个结果的问题。

        为了返回多个结果，让我们在Map中添加书籍：

        `books.put("978-0321356680", "Effective Java: Second Edition");`

        现在，如果我们查找所有以 "Effective Java" 开头的书籍，就会得到多个结果：

        whenMultipleResultsVersionCalledForExistingTitle_aCollectionWithMultipleValuesIsReturned()

        在本例中，我们将过滤条件改为验证 Map 中的值是否以 "Effective Java" 开头，而不是比较字符串是否相等。

        这次我们收集结果，而不是只选择第一个，并将匹配结果放入 List。

4. 使用流获取 Map 的值

    现在我们来关注一下Map的另一个问题。我们将尝试根据 ISBN 获取书名，而不是根据书名获取 ISBN。

    让我们使用原始Map。我们要查找 ISBN 以 "978-0" 开头的书目。

    `whenKeysFollowingPatternReturnsAllValuesForThoseKeys()`

    此解决方案与前一组问题的解决方案类似：我们先将条目集流化，然后进行过滤、映射和收集。

    和之前一样，如果我们只想返回第一个匹配结果，那么在 map 方法之后，我们可以调用 findFirst() 方法，而不是将所有结果都收集到 List 中。

5. 结论

    在本文中，我们演示了如何以函数方式处理 Map。

    特别是，我们已经看到，一旦我们将关联集合转换为Map，使用流处理就会变得更加简单和直观。

## 相关文章

- [x] [The Java 8 Stream API Tutorial](https://www.baeldung.com/java-8-streams)
- [x] [Introduction to Java 8 Streams](https://www.baeldung.com/java-8-streams-introduction)
- [Java 8 Stream findFirst() vs. findAny()](https://www.baeldung.com/java-stream-findfirst-vs-findany)
- [Guide to Stream.reduce()](https://www.baeldung.com/java-stream-reduce)
- [Java IntStream Conversions](https://www.baeldung.com/java-intstream-convert)
- [Java 8 Streams peek() API](https://www.baeldung.com/java-streams-peek-api)
- [x] [Working With Maps Using Streams](https://www.baeldung.com/java-maps-streams)
- [Collect a Java Stream to an Immutable Collection](https://www.baeldung.com/java-stream-immutable-collection)
- [How to Add a Single Element to a Stream](https://www.baeldung.com/java-stream-append-prepend)
- [Operating on and Removing an Item from Stream](https://www.baeldung.com/java-use-remove-item-stream)
- More articles: [[<-- prev>]](/../core-java-streams) [[next -->]](/../core-java-streams-3)

## Code

当然，本文中的所有示例都可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-streams-2) 项目中找到。
