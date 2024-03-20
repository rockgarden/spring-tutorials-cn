# [Java字符串面试问题](https://www.baeldung.com/java-string-interview-questions)

[Java字符串](https://www.baeldung.com/category/java/java-string)

[面试](https://www.baeldung.com/tag/interview)

1. 简介

    字符串类是 Java 中使用最广泛的类之一，这促使语言设计者对其进行特殊处理。这种特殊行为使其成为 Java 面试中最热门的话题之一。

    在本教程中，我们将讨论一些有关 String 的最常见面试问题。

2. 字符串基础

    本节包含有关字符串内部结构和内存的问题。

    Q1. 什么是 Java 中的字符串？

    在 Java 中，字符串在内部由字节值（在 JDK 9 之前为字符值）数组表示。

    在 Java 8 及 Java 8 之前的版本中，字符串由不可变的 Unicode 字符数组组成。但是，大多数字符只需要 8 位（1 个字节）来表示，而不是 16 位（字符大小）。

    为了改善内存消耗和性能，Java 9 引入了紧凑型字符串。这意味着，如果字符串只包含 1 字节字符，它将使用 Latin-1 编码来表示。如果字符串至少包含 1 个多字节字符，则将使用 UTF-16 编码以每个字符 2 个字节的形式表示。

    在 C 和 C++ 中，字符串也是一个字符数组，但在 Java 中，它是一个独立的对象，有自己的 API。

    Q2. 如何在 Java 中创建字符串对象？

    [java.lang.String](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html) 定义了 13 种[创建字符串](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html#constructor-summary)的不同方法。一般来说，有两种：

    通过字符串定义(literal)：
    `String s = "abc";`
    通过 new 关键字：
    `String s = new String("abc");`

    > Java 中的所有 String 字面量都是 String 类的实例。

    Q3. 字符串是原始(Primitive)类型还是派生(Derived)类型？

    String 是一种派生类型，因为它有状态和行为。例如，它有 substring()、indexOf() 和 equals() 等方法，而基元类型不可能有这些方法。

    但是，由于我们经常使用字符串，因此它具有一些特殊的特性，让人感觉它就像一个基元类型：

    - 字符串不像基元(primitives)那样存储在调用栈中，而是存储在一个名为[字符串池](https://www.baeldung.com/java-string-pool)的特殊内存区域中。
    - 像基元一样，我们可以在字符串上使用 + 运算符
    - 同样，与基元一样，我们可以创建字符串实例，而无需使用 new 关键字

    Q4. 字符串不可变有什么好处？

    根据 James Gosling 的[采访](https://www.artima.com/intv/gosling313.html)，字符串不可变是为了提高性能和安全性。

    实际上，我们可以看到不可变字符串有几个[好处](https://www.baeldung.com/java-string-immutable)：

    - 字符串池只有在字符串一旦创建就永不更改的情况下才有可能存在，因为字符串应该被重复使用
    - 代码可以安全地将字符串传递给另一个方法，因为该方法不会更改字符串
    - 不变性自动使该类成为线程安全类
    - 由于该类是线程安全的，因此无需同步公共数据，从而提高了性能
    - 由于保证了它们不会改变，因此可以轻松地缓存它们的哈希码

    Q5. 字符串如何存储在内存中？

    根据 JVM 规范，字符串字面量存储在[运行时常量池](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.5.5)中，而常量池是从 JVM 的[方法区](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.5.4)分配的。

    虽然方法区在逻辑上是堆内存的一部分，但规范并未规定其位置、内存大小或垃圾回收策略。它可以是特定于实现的。

    类或接口的运行时常量池是在 JVM 创建类或接口时构建的。

    Q6. 在 Java 中，内部字符串有资格被垃圾回收吗？

    是的，如果程序中没有引用，字符串池中的所有字符串都有资格被垃圾回收。

    Q7. 什么是字符串常量池？

    字符串池（又称字符串常量池或字符串实习池）是 JVM 存储字符串实例的特殊内存区域。

    它通过减少字符串的分配频率和数量来优化应用程序性能：

    - JVM 在池中只存储一个特定字符串的副本。
    - 创建新字符串时，JVM 会在池中搜索具有相同值的字符串
    - 如果找到，JVM 将返回对该字符串的引用，而不会分配任何额外内存
    - 如果未找到，JVM 会将其添加到池中（实习）并返回其引用

    Q8. 字符串是线程安全的吗？如何安全？

    字符串确实是完全线程安全的，因为它们是不可变的。任何不可变的类都自动符合线程安全的条件，因为其不可变性保证了其实例不会在多个线程中被更改。

    例如，如果一个线程更改了字符串的值，就会创建一个新的字符串，而不是修改现有的字符串。

    Q9. 对于哪些字符串操作来说，提供一个 Locale 是很重要的？

    通过 Locale 类，我们可以区分不同的文化地域，并对内容进行适当的格式化。

    说到字符串类，我们在以格式呈现字符串或对字符串进行小写或大写时都需要它。

    事实上，如果我们忘记了这一点，就会在可移植性、安全性和可用性方面遇到问题。

    Q10. 字符串的基本字符编码是什么？

    根据 String 的 Javadocs（包括 Java 8 在内的版本），字符串在内部以 UTF-16 格式存储。

    char 数据类型和 java.lang.Character 对象也是[基于最初的 Unicode 规范](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html#unicode)，该规范将字符定义为固定宽度的 16 位实体。

    从 JDK 9 开始，只包含 1 字节字符的字符串使用 Latin-1 编码，而至少包含 1 个多字节字符的字符串使用 UTF-16 编码。

3. 字符串 API

    在本节中，我们将讨论与字符串 API 有关的一些问题。

    Q11. 如何在 Java 中[比较两个字符串](https://www.baeldung.com/java-compare-strings)？str1 == str2 与 str1.equals(str2) 有什么区别？

    我们可以用两种不同的方法比较字符串：使用等于操作符 ( == ) 和使用 equals() 方法。

    这两种方法截然不同：

    - 运算符 (str1 == str2) 检查引用是否相等
    - 方法 (str1.equals(str2)) 检查词法上的等价性

    虽然如果两个字符串在词法上相等，那么 str1.intern() == str2.intern() 也是真的。

    通常，在比较两个字符串的内容时，我们应始终使用 String.equals。

    Q12. 如何在 Java 中分割字符串？

    String 类本身提供了 [String#split](https://www.baeldung.com/string/split) 方法，该方法接受正则表达式分隔符。它会返回一个 String[] 数组：

    ```java
    String[] parts = "john,peter,mary".split(",");
    assertEquals(new String[] { "john", "peter", "mary" }, parts);
    ```

    split 的一个棘手之处是，当分割一个空字符串时，我们可能会得到一个非空数组：

    `assertEquals(new String[] { "" }, "".split(","));`

    当然，split 只是[拆分Java字符串](https://www.baeldung.com/java-split-string)的众多方法之一。

    Q13. 什么是 Stringjoiner？

    [StringJoiner](https://www.baeldung.com/java-string-joiner) 是 Java 8 中引入的一个类，用于将单独的字符串连接成一个字符串，比如将一个颜色列表以逗号分隔的字符串形式返回。我们可以提供分隔符以及前缀和后缀：

    ```java
    StringJoiner joiner = new StringJoiner(",", "[", "]");
    joiner.add("Red")
    .add("Green")
    .add("Blue");

    assertEquals("[Red,Green,Blue]", joiner.toString());
    ```

    Q14. 字符串、字符串缓冲区和字符串生成器之间的区别？

    字符串是不可变的。这意味着，如果我们试图更改或改变其值，Java 会创建一个全新的字符串。

    例如，如果我们在字符串 str1 创建后对其进行添加，则

    `String str1 = "abc";`
    `str1 = str1 + "def";`

    那么 JVM 将不会修改 str1，而是创建一个全新的字符串。

    不过，对于大多数简单的情况，编译器会在内部使用 StringBuilder 并优化上述代码。

    但对于循环等更复杂的代码，编译器会创建一个全新的字符串，从而降低性能。这时，StringBuilder 和 StringBuffer 就派上用场了。

    Java 中的 [StringBuilder 和 StringBuffer](https://www.baeldung.com/java-string-builder-string-buffer) 都能创建保存可变字符序列的对象。StringBuffer 是同步的，因此是线程安全的，而 StringBuilder 则不是。

    由于 StringBuffer 中的额外同步通常是不必要的，我们通常可以通过选择 StringBuilder 来提高性能。

    Q15. 为什么用 Char[] 数组而不是字符串存储密码更安全？

    因为字符串是不可变的，不允许修改。这种特性使我们无法覆盖、修改或清零字符串的内容，因此字符串不适合用于存储敏感信息。

    我们必须依靠垃圾回收器来删除字符串的内容。此外，在 Java 6 及以下版本中，字符串存储在 PermGen 中，这意味着字符串一旦创建，就永远不会被垃圾回收。

    通过使用 char[] 数组，我们可以完全控制该信息。我们甚至无需依赖垃圾回收器，就能修改或完全删除它。

    使用 char[] 而不是 String 并不能完全确保信息的安全；它只是一种额外的措施，减少了恶意用户获取敏感信息的机会。

    Q16. 字符串的 [intern()](https://www.baeldung.com/string/intern) 方法有什么作用？

    intern() 方法会在堆中创建一个字符串对象的精确副本，并将其存储到 JVM 维护的字符串常量池中。

    Java 会自动实习(interns)所有使用字符串字面量创建的字符串，但如果我们使用 new 操作符创建一个字符串，例如，`String str = new String("abc")`，Java 就会将其添加到堆中，就像添加其他对象一样。

    我们可以调用 intern() 方法，告诉 JVM 如果字符串池中还不存在字符串，就将其添加到字符串池中，并返回该字符串的引用：

    ```java
    String s1 = "Baeldung";
    String s2 = new String("Baeldung");
    String s3 = new String("Baeldung").intern();

    assertThat(s1 == s2).isFalse();
    assertThat(s1 == s3).isTrue();
    ```

    Q17. 如何在 Java 中将字符串转换为整数和将整数转换为字符串？

    将字符串[转换为整数](https://www.baeldung.com/java-convert-string-to-int-or-integer)的最直接方法是使用 Integer#parseInt：

    `int num = Integer.parseInt("22");`

    要进行相反操作，我们可以使用 Integer#toString 命令：

    `String s = Integer.toString(num);`

    Q18. 什么是 String.format()？

    [String#format](https://www.baeldung.com/string/format) 使用指定的格式字符串和参数返回格式化字符串。

    ```java
    String title = "Baeldung"; 
    String formatted = String.format("Title is %s", title);
    assertEquals("Title is Baeldung", formatted);
    ```

    我们还需要记住指定用户的 Locale，除非我们可以接受操作系统的默认设置：

    ```java
    Locale usersLocale = Locale.ITALY;
    assertEquals("1.024",
      String.format(usersLocale, "There are %,d shirts to choose from. Good luck.", 1024))
    ```

    Q19. 如何将字符串转换成大写和小写？

    字符串隐式提供了 [String#toUpperCase](https://www.baeldung.com/string/to-upper-case) 来将大小写转换为大写。

    不过，Javadocs 提醒我们需要指定用户的 Locale 以确保正确性：

    ```java
    String s = "Welcome to Baeldung!";
    assertEquals("WELCOME TO BAELDUNG!", s.toUpperCase(Locale.US));
    ```

    同样，要转换为小写，我们可以使用 [String#toLowerCase](https://www.baeldung.com/string/to-lower-case)：

    ```java
    String s = "Welcome to Baeldung!";
    assertEquals("welcome to baeldung!", s.toLowerCase(Locale.UK));
    ```

    Q20. 如何从字符串获取字符数组？

    String 提供了 toCharArray 功能，在 JDK9 之前，它返回内部字符数组的副本（在 JDK9+ 中，它将 String 转换为新的字符数组）：

    ```java
    char[] hello = "hello".toCharArray();
    assertArrayEquals(new String[] { 'h', 'e', 'l', 'l', 'o' }, hello);
    ```

    Q21. 如何将 Java 字符串转换为字节数组？

    默认情况下，方法 [String#getBytes()](https://www.baeldung.com/string/get-bytes) 使用平台的默认字符集将字符串编码为字节数组。

    虽然 API 并不要求我们指定字符集，但为了[确保安全性和可移植性](https://www.baeldung.com/java-char-encoding)，我们还是应该这样做：

    ```java
    byte[] byteArray2 = "efgh".getBytes(StandardCharsets.US_ASCII);
    byte[] byteArray3 = "ijkl".getBytes("UTF-8");
    ```

4. 基于字符串的算法

    在本节中，我们将讨论一些与字符串有关的编程问题。

    Q22. 如何在 Java 中检查两个字符串是否为变位字符串？

    变位词是将另一个给定单词的字母重新排列形成的单词，例如 "car "和 "arc"。

    首先，我们要检查两个字符串是否等长。

    然后将它们[转换为 char[] 数组，排序，然后检查是否相等](https://www.baeldung.com/java-sort-string-alphabetically)。

    Q23. 如何计算字符串中给定字符的出现次数？

    Java 8 确实简化了类似的聚合任务：

    ```java
    long count = "hello".chars().filter(ch -> (char)ch == 'l').count();
    assertEquals(2, count);
    ```

    此外，还有其他一些[计算l](https://www.baeldung.com/java-count-chars)的好方法，包括循环、递归、正则表达式和外部库。

    Q24. 如何在 Java 中反转字符串？

    有很多方法可以做到这一点，最直接的方法是使用 StringBuilder（或 StringBuffer）中的反转方法：

    ```java
    String reversed = new StringBuilder("baeldung").reverse().toString();
    assertEquals("gnudleab", reversed);
    ```

    Q25. 如何检查字符串是否是回文字符串？

    [回文字符串](https://www.baeldung.com/java-palindrome-substrings)是指反向读法与正向读法相同的字符序列，如 "madam"、"radar"或 "level"。

    要[检查一个字符串是否是回文](https://www.baeldung.com/java-palindrome)字符串，我们可以开始在一个循环中前后遍历给定的字符串，每次遍历一个字符。当出现第一个不匹配字符时，循环退出。

5. 结论

    在本文中，我们讨论了一些最常见的字符串面试问题。

    本文使用的所有代码示例均可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-strings) 上获取。
