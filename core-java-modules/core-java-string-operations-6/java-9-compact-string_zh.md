# 在Java9中压缩字符串

[Java字符串](https://www.baeldung.com/category/java/java-string)

1. 概述

    Java中的字符串在内部是由一个包含字符串字符的char[]表示的。而且，每个char都由2个字节组成，因为Java内部使用UTF-16。
    例如，如果一个字符串包含一个英语单词，每个char的前8位都是0，因为一个ASCII字符可以用一个字节表示。
    许多字符需要16位来表示，但据统计，大多数字符只需要8位--LATIN-1字符表示。因此，在内存消耗和性能方面有改善的余地。
    同样重要的是，字符串通常会占据JVM堆空间的很大一部分。而且，由于JVM对它们的存储方式，在大多数情况下，一个字符串实例会占用它实际需要的双倍空间。
    在这篇文章中，我们将讨论JDK6中引入的压缩字符串选项和最近在JDK9中引入的新的紧凑字符串。这两个选项都是为了优化JMV上的字符串的内存消耗。

2. 压缩字符串 - Java 6

    JDK 6更新21性能版本，引入了一个新的VM选项：

    `-XX:+UseCompressedStrings`

    启用该选项后，字符串会以`byte[]`的形式存储，而不是`char[]`--因此，可以节省大量的内存。然而，这个选项最终在JDK 7中被删除，主要是因为它有一些意想不到的性能后果。
3. 紧凑字符串 - Java 9

    Java 9带回了紧凑字符串的概念。
    这意味着当我们创建一个字符串时，如果该字符串的所有字符都可以用字节-LATIN-1表示，内部将使用一个字节数组，这样一个字符就有一个字节。
    在其他情况下，如果任何字符需要超过8位来表示，所有的字符将使用两个字节来存储--UTF-16表示。
    所以基本上，只要有可能，它就会为每个字符使用一个字节。
    现在，问题是--所有的字符串操作将如何工作？它将如何区分LATIN-1和UTF-16的表示？
    好吧，为了解决这个问题，我们对String的内部实现做了另一个改变。我们有一个最终的字段编码器，它保留了这个信息。

    1. Java 9中的字符串实现

        到目前为止，String是作为char[]来存储的：

        `private final char[] value;`

        从现在开始，它将是一个byte[] ：

        `private final byte[] value;`

        变量coder：

        `private final byte coder;`

        编码器可以在哪里：

        ```java
        static final byte LATIN1 = 0;
        static final byte UTF16 = 1;
        ```

        现在大多数的字符串操作都会检查编码，并分派给具体的实现：

        ```java
        public int indexOf(int ch, int fromIndex) {
            return isLatin1() 
            ? StringLatin1.indexOf(value, ch, fromIndex) 
            : StringUTF16.indexOf(value, ch, fromIndex);
        }  
        private boolean isLatin1() {
            return COMPACT_STRINGS && coder == LATIN1;
        }
        ```

        由于JVM需要的所有信息都已准备好并可用，CompactString VM选项在默认情况下被启用。要禁用它，我们可以使用：

        `+XX:-CompactStrings`

    2. 编码器如何工作

        在Java 9的字符串类实现中，长度的计算方法是：

        如果字符串只包含LATIN-1，编码器的值将是0，所以字符串的长度将与字节数组的长度相同。
        在其他情况下，如果字符串是UTF-16表示法，编码器的值将是1，因此长度将是实际字节数组的一半。
        请注意，所有为Compact String所做的改变，都在String类的内部实现中，对于使用String的开发者来说是完全透明的。
4. 紧凑字符串与压缩字符串

    在JDK 6压缩字符串的情况下，面临的一个主要问题是，字符串构造函数只接受`char[]`作为参数。除此之外，许多字符串操作都依赖于`char[]`的表示，而不是字节数组。由于这个原因，必须做大量的解包工作，这影响了性能。
    而在紧凑型字符串的情况下，维护额外的字段 "coder" 也会增加开销。为了减轻编码器的成本和将字节解包为字符的成本（在UTF-16表示法的情况下），一些方法被[内化](https://en.wikipedia.org/wiki/Intrinsic_function)了，JIT编译器生成的ASM代码也得到了改进。
    这一变化导致了一些反直觉的结果。LATIN-1 indexOf(String)调用了一个内在的方法，而indexOf(char)则没有。在UTF-16的情况下，这些方法都会调用一个内在的方法。这个问题只影响到LATIN-1字符串，并将在未来的版本中被修复。
    因此，就性能而言，紧凑型字符串比压缩型字符串更好。
    为了弄清使用紧凑字符串可以节省多少内存，我们分析了各种Java应用程序的堆转储。而且，虽然结果在很大程度上取决于具体的应用程序，但整体上的改进几乎都是相当大的。
    1. 性能上的差异

        让我们看一个非常简单的例子，说明启用和禁用紧凑字符串的性能差异：

        ```java
        long startTime = System.currentTimeMillis();
        List strings = IntStream.rangeClosed(1, 10_000_000)
        .mapToObj(Integer::toString) 
        .collect(toList());
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(
        "Generated " + strings.size() + " strings in " + totalTime + " ms.");
        startTime = System.currentTimeMillis();
        String appended = (String) strings.stream()
        .limit(100_000)
        .reduce("", (l, r) -> l.toString() + r.toString());
        totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Created string of length " + appended.length() 
        + " in " + totalTime + " ms.");
        ```

        在这里，我们正在创建1000万个字符串，然后以一种天真的方式对它们进行追加。当我们运行这段代码时（Compact Strings默认是启用的），我们得到了这样的输出：

        ```log
        Generated 10000000 strings in 854 ms.
        Created string of length 488895 in 5130 ms.
        ```

        同样地，如果我们通过使用以下方法禁用紧凑字符串来运行它： `-XX:-CompactStrings` 选项，输出结果是：

        ```log
        Generated 10000000 strings in 936 ms.
        Created string of length 488895 in 9727 ms.
        ```

        显然，这只是一个表面的测试，它不可能有很强的代表性--它只是一个快照，说明在这个特定的情况下，新选项可能会改善性能。
5. 总结

    在本教程中，我们看到了在JVM上优化性能和内存消耗的尝试--通过以一种有效的内存方式存储字符串。

    本文中所有方法的代码都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-string-operations-6)上找到。
