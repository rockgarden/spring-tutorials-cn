# [Java 中的可变参数](https://www.baeldung.com/java-packages)

核心 Java

1. 引言

    可变参数（Varargs）是在 Java 5 中引入的特性，为支持任意数量同类型参数的方法提供了一种简洁的语法。

    在本文中，我们将了解如何使用这一 Java 核心功能。

2. 可变参数出现之前

    在 Java 5 之前，如果我们希望向方法传递任意数量的参数，只能将所有参数封装在一个数组中，或者为不同参数数量分别实现多个重载方法：

    ```java
    public String format() { ... }

    public String format(String value) { ... }

    public String format(String val1, String val2) { ... }
    ```

    这种方式不仅冗长，而且难以维护。

3. 可变参数的使用

    可变参数帮助我们避免编写样板代码，它引入了一种新语法，能够自动处理任意数量的参数——底层实际上仍使用数组实现。

    我们可以通过在标准类型声明后加上省略号（`...`）来定义可变参数：

    ```java
    public String formatWithVarArgs(String... values) {
        // ...
    }
    ```

    现在，我们可以用任意数量的参数调用该方法，例如：

    ```java
    formatWithVarArgs(); // 无参数

    formatWithVarArgs("a", "b", "c", "d"); // 四个参数
    ```

    如前所述，可变参数本质上是数组，因此我们需要像操作普通数组一样处理它们。

    测试代码：`mvn test -Dtest=FormatterUnitTest`

4. 使用规则

    可变参数虽然简单易用，但有几条规则必须遵守：

    - 每个方法最多只能有一个可变参数。
    - 可变参数必须是方法参数列表中的最后一个参数。

5. 堆污染（Heap Pollution）

    使用可变参数可能导致所谓的“堆污染”（[Heap Pollution](https://en.wikipedia.org/wiki/Heap_pollution)）。为了更好地理解这一概念，考虑以下使用泛型可变参数的方法：

    ```java
    static String firstOfFirst(List<String>... strings) {
        List<Integer> ints = Collections.singletonList(42);
        Object[] objects = strings;
        objects[0] = ints; // 堆污染发生！

        return strings[0].get(0); // 抛出 ClassCastException
    }
    ```

    测试代码：`mvn test -Dtest=HeapPollutionUnitTest`

    如果我们用如下方式调用这个奇怪的方法：

    ```java
    String one = firstOfFirst(Arrays.asList("one", "two"), Collections.emptyList());
    assertEquals("one", one);
    ```

    即使代码中没有任何显式的类型转换，程序仍会抛出 `ClassCastException`：

    ```log
    java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String
    ```

    1. 安全使用可变参数

        每次使用可变参数时，Java 编译器都会创建一个数组来保存传入的参数。在泛型场景下，编译器会创建一个具有泛型组件类型的数组。

        然而，由于泛型类型在运行时会被类型擦除，这种数组在运行时实际是 `Object[]`。如果我们在方法内部向该数组写入不兼容的类型，就会导致堆污染。

        正因如此，当使用泛型可变参数时，Java 编译器会发出警告：

        ```log
        warning: [varargs] Possible heap pollution from parameterized vararg type T
        ```

        只有在满足以下两个条件时，可变参数的使用才是安全的：

        1. 不在隐式创建的数组中存储任何内容（上例中我们错误地将 `List<Integer>` 存入了应为 `List<String>` 的数组）。
        2. 不让该数组的引用逃逸出方法作用域（稍后详述）。

        如果我们确信方法对可变参数的使用是安全的（例如仅用于将参数从调用方传递到方法内部，不做其他操作），可以使用 [@SafeVarargs](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/SafeVarargs.html) 注解来抑制编译器警告。

        简而言之：可变参数的安全用法仅限于“将可变数量的参数从调用方传递到方法内部”这一目的。

    2. 可变参数引用的逃逸

        再看一个不安全的可变参数用法示例：

        ```java
        static <T> T[] toArray(T... arguments) {
            return arguments;
        }
        ```

        乍看之下，`toArray` 方法似乎无害。但由于它将可变参数数组的引用返回给了调用方，违反了“**不让数组引用逃逸**”的安全规则。

        为了说明其危险性，假设我们这样使用它：

        ```java
        static <T> T[] returnAsIs(T a, T b) {
            return toArray(a, b);
        }
        ```

        然后调用：

        ```java
        String[] args = returnAsIs("One", "Two");
        ```

        此时仍会抛出 `ClassCastException`。原因如下：

        - 为了将 `a` 和 `b` 传递给 `toArray`，Java 需要创建一个数组。
        - 由于泛型类型擦除，编译器实际创建的是 `Object[]`。
        - `toArray` 方法将这个 `Object[]` 返回给调用方。
        - 调用方期望得到 `String[]`，于是编译器尝试将 `Object[]` 强制转换为 `String[]`，从而引发 `ClassCastException`。

        关于堆污染的更深入讨论，强烈推荐阅读 Joshua Bloch 所著[《Effective Java》](https://learning.oreilly.com/library/view/effective-java-3rd/9780134686097/)第 32 条。

6. 结论

    可变参数能显著减少 Java 中的样板代码。

    此外，得益于其与数组之间的隐式自动装箱/拆箱机制，可变参数也有助于提升代码的未来兼容性和可维护性。
