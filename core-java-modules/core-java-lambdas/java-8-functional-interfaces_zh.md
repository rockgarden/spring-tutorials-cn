# [Java 中的函数式接口](https://www.baeldung.com/java-8-functional-interfaces)

核心 Java（≥ Java 8）| Java 接口 | Lambda 表达式

1. 引言

    本教程将介绍 Java 8 中提供的各类函数式接口、它们的通用场景，以及在 JDK 标准库中的实际应用。

2. Java 8 中的 Lambda 表达式

    Java 8 引入了强大的语法改进——**Lambda 表达式**。Lambda 是一种匿名函数，可作为“一等公民”在语言中传递，例如作为方法参数或返回值。

    在 Java 8 之前，每当需要封装单一功能时，通常要创建一个完整的类，导致大量不必要的样板代码，仅用于表示一个原始的函数行为。

    本指南则聚焦于 `java.util.function` 包中一些特定的函数式接口。

3. 函数式接口

    建议所有函数式接口都使用具有说明性的 `@FunctionalInterface` 注解。这不仅清晰表达了接口的用途，还能让编译器在标注了该注解的接口不满足函数式接口条件时生成错误。

    **任何只包含一个抽象方法（SAM, Single Abstract Method）的接口都是函数式接口**，其实现可被视为 Lambda 表达式。

    注意：Java 8 的默认方法（default methods）不是抽象的，因此不计入抽象方法数量。函数式接口仍可包含多个默认方法。这一点可在 `Function` 接口的[文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/Function.html)中得到验证。

4. Function（函数）

    Lambda 最简单、最通用的形式是一个接收一个值并返回另一个值的函数。这种单参数函数由 `Function<T, R>` 接口表示，其泛型参数分别代表输入类型 `T` 和返回类型 `R`：

    ```java
    public interface Function<T, R> { … }
    ```

    `Function` 在标准库中的一个典型用法是 `Map.computeIfAbsent` 方法。该方法根据键从 Map 中获取值；若键不存在，则使用传入的 `Function` 计算并存入新值：

    ```java
    Map<String, Integer> nameMap = new HashMap<>();
    Integer value = nameMap.computeIfAbsent("John", s -> s.length());
    ```

    此处，我们将键 `"John"` 传给函数 `s -> s.length()`，计算结果存入 Map 并返回。

    我们也可以用**方法引用**替代 Lambda，只要其签名匹配：

    ```java
    Integer value = nameMap.computeIfAbsent("John", String::length);
    ```

    注意：调用方法的对象实际上是该方法的隐式第一个参数，因此实例方法 `String::length` 可被转换为 `Function<String, Integer>`。

    `Function` 接口还提供了一个默认的 `compose` 方法，用于将多个函数组合并按顺序执行：

    ```java
    Function<Integer, String> intToString = Object::toString;
    Function<String, String> quote = s -> "'" + s + "'";

    Function<Integer, String> quoteIntToString = quote.compose(intToString);

    assertEquals("'5'", quoteIntToString.apply(5));
    ```

    `quoteIntToString` 是先执行 `intToString`，再将结果传给 `quote` 的组合函数。

5. 原始类型 Function 特化

    由于原始类型不能作为泛型参数，JDK 为最常用的原始类型（`double`、`int`、`long`）及其组合提供了特化版本：

    - `IntFunction<R>`、`LongFunction<R>`、`DoubleFunction<R>`：参数为指定原始类型，返回值为泛型
    - `ToIntFunction<T>`、`ToLongFunction<T>`、`ToDoubleFunction<T>`：返回值为指定原始类型，参数为泛型
    - `DoubleToIntFunction`、`IntToLongFunction` 等：参数和返回值均为原始类型（名称已表明类型）

    例如，JDK 未提供 `short → byte` 的函数接口，但我们可以自定义：

    ```java
    @FunctionalInterface
    public interface ShortToByteFunction {
        byte applyAsByte(short s);
    }
    ```

    然后编写一个使用该接口的转换方法：

    ```java
    public byte[] transformArray(short[] array, ShortToByteFunction function) {
        byte[] transformedArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            transformedArray[i] = function.applyAsByte(array[i]);
        }
        return transformedArray;
    }
    ```

    使用示例：

    ```java
    short[] array = {(short) 1, (short) 2, (short) 3};
    byte[] transformedArray = transformArray(array, s -> (byte) (s * 2));

    byte[] expectedArray = {(byte) 2, (byte) 4, (byte) 6};
    assertArrayEquals(expectedArray, transformedArray);
    ```

6. 双参数函数特化（Two-Arity）

    对于接受两个参数的 Lambda，需使用名称中包含 “Bi” 的接口：

    - `BiFunction<T, U, R>`：两个泛型参数，一个泛型返回值
    - `ToIntBiFunction<T, U>`、`ToDoubleBiFunction<T, U>` 等：返回原始类型

    标准 API 中的一个典型用例是 `Map.replaceAll`，它使用 `BiFunction` 根据键和旧值计算新值：

    ```java
    Map<String, Integer> salaries = new HashMap<>();
    salaries.put("John", 40000);
    salaries.put("Freddy", 30000);
    salaries.put("Samuel", 50000);

    salaries.replaceAll((name, oldValue) ->
        name.equals("Freddy") ? oldValue : oldValue + 10000);
    ```

7. Supplier（供给者）

    `Supplier<T>` 是一种不接受参数、仅返回值的函数式接口，常用于**惰性求值**。

    例如，定义一个接收 `Supplier<Double>` 的平方函数：

    ```java
    public double squareLazy(Supplier<Double> lazyValue) {
        return Math.pow(lazyValue.get(), 2);
    }
    ```

    若值的生成耗时较长（如模拟 1 秒延迟）：

    ```java
    Supplier<Double> lazyValue = () -> {
        Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
        return 9d;
    };
    Double valueSquared = squareLazy(lazyValue);
    ```

    另一个常见用途是**序列生成**。例如，使用 `Stream.generate` 生成斐波那契数列：

    ```java
    int[] fibs = {0, 1};
    Stream<Integer> fibonacci = Stream.generate(() -> {
        int result = fibs[1];
        int fib3 = fibs[0] + fibs[1];
        fibs[0] = fibs[1];
        fibs[1] = fib3;
        return result;
    });
    ```

    > 注意：Lambda 中使用的外部变量必须是“有效 final”的，因此我们使用数组（引用不可变，但内容可变）来维护状态。

    此外还有原始类型特化：`BooleanSupplier`、`IntSupplier`、`LongSupplier`、`DoubleSupplier`。

8. Consumer（消费者）

    与 Supplier 相反，Consumer 接收一个泛型参数，不返回任何值。它是一种表示副作用（side effects）的函数。

    例如，我们可以通过在控制台打印问候语来向名单中的每个人打招呼。传递给 `List.forEach` 方法的 Lambda 表达式实现了 Consumer 函数式接口：

    ```java
    List<String> names = Arrays.asList("John", "Freddy", "Samuel");
    names.forEach(name -> System.out.println("Hello, " + name));
    ```

    此外，还有 Consumer 的特化版本——`DoubleConsumer`、`IntConsumer` 和 `LongConsumer`——它们接收原始类型值作为参数。更有趣的是 `BiConsumer` 接口。它的一个使用场景是遍历 Map 的条目：

    ```java
    Map<String, Integer> ages = new HashMap<>();
    ages.put("John", 25);
    ages.put("Freddy", 24);
    ages.put("Samuel", 30);

    ages.forEach((name, age) -> System.out.println(name + " is " + age + " years old"));
    ```

    另一组特化的 `BiConsumer` 版本包括 `ObjDoubleConsumer`、`ObjIntConsumer` 和 `ObjLongConsumer`，它们接收两个参数：其中一个参数是泛型类型，另一个是原始类型。

9. Predicate（断言）

    在数理逻辑中，谓词是一个接收值并返回布尔值的函数。

    `Predicate<T>` 是 `Function<T, Boolean>` 的特化，广泛用于**过滤**操作：

    ```java
    List<String> names = Arrays.asList("Angela", "Aaron", "Bob", "Claire", "David");

    List<String> namesWithA = names.stream()
        .filter(name -> name.startsWith("A"))
        .collect(Collectors.toList());
    ```

    此处的 Lambda 实现了 `Predicate<String>`，封装了过滤逻辑。

    原始类型特化：`IntPredicate`、`LongPredicate`、`DoublePredicate`。

10. Operator（操作符）

    Operator 是一种**输入与输出类型相同**的特殊函数。

    - `UnaryOperator<T>`：单参数操作符
        常用于 `List.replaceAll`：

        ```java
        List<String> names = Arrays.asList("bob", "josh", "megan");
        names.replaceAll(String::toUpperCase); // 等价于 name -> name.toUpperCase()
        ```

    - `BinaryOperator<T>`：双参数操作符，常用于**归约（reduction）**：

        ```java
        List<Integer> values = Arrays.asList(3, 5, 8, 9, 12);
        int sum = values.stream().reduce(0, (i1, i2) -> i1 + i2);
        ```

        `reduce` 方法接收初始值和 `BinaryOperator`。该函数必须满足**结合律**：

        ```java
        op.apply(a, op.apply(b, c)) == op.apply(op.apply(a, b), c)
        ```

    结合律使得归约操作可安全地并行化。

    原始类型特化包括：

    - `IntUnaryOperator`、`LongUnaryOperator`、`DoubleUnaryOperator`
    - `IntBinaryOperator`、`LongBinaryOperator`、`DoubleBinaryOperator`

11. 遗留的函数式接口

    并非所有函数式接口都诞生于 Java 8。许多旧版接口（如并发 API 中的 `Runnable` 和 `Callable`）天然符合 SAM 原则，因此也可作为 Lambda 使用。Java 8 起，这些接口也添加了 `@FunctionalInterface` 注解：

    ```java
    Thread thread = new Thread(() -> System.out.println("Hello From Another Thread"));
    thread.start();
    ```

    这极大简化了并发代码。

12. 结论

    本文详细探讨了 Java 8 API 中各类函数式接口，以及如何将它们作为 Lambda 表达式使用。
