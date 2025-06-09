# 了解 Java 中的 NumberFormatException

1. 简介

    Java 在无法将[字符串转换为数字类型](https://www.baeldung.com/java-convert-string-to-int-or-integer)时会抛出 NumberFormatException（[未选中异常](https://www.baeldung.com/java-exceptions)）。

    由于它未被选中，Java 不会强制我们处理或声明它。

    在本快速教程中，我们将描述并演示在 Java 中导致 NumberFormatException 的原因，以及如何避免或处理它。

2. 导致 NumberFormatException 的原因

    导致 NumberFormatException 的原因有很多。例如，Java 中的某些构造函数和方法会抛出此异常。

    我们将在下面的章节中讨论其中的大部分。

    1. 传递给构造函数的非数字数据

        让我们来看看使用非数字数据构造整数或双倍对象的尝试。

        这两条语句都会抛出 NumberFormatException：

        ```java
        Integer aIntegerObj = new Integer("one");
        Double doubleDecimalObj = new Double("two.2");
        ```

        让我们看看第 1 行中向整数构造函数传递无效输入 "one" 时的堆栈跟踪：

        ```log
        Exception in thread "main" java.lang.NumberFormatException: For input string: "one"
            at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
            at java.lang.Integer.parseInt(Integer.java:580)
            at java.lang.Integer.<init>(Integer.java:867)
            at MainClass.main(MainClass.java:11)
        ```

        它抛出了 NumberFormatException。在尝试使用 parseInt() 在内部理解输入时，Integer 构造函数失败了。

        Java Number API 不会将单词解析为数字，因此我们只需将其更改为预期值即可纠正代码：

        ```java
        Integer aIntegerObj = new Integer("1");
        Double doubleDecimalObj = new Double("2.2");
        ```

    2. 解析包含非数字数据的字符串

        与 Java 在构造函数中支持解析类似，我们也有专门的解析方法，如 parseInt()、parseDouble()、valueOf() 和 decode()。

        如果我们尝试用这些方法进行相同类型的转换，可以得到以下结果

        ```java
        int aIntPrim = Integer.parseInt("two");
        double aDoublePrim = Double.parseDouble("two.two");
        Integer aIntObj = Integer.valueOf("three");
        Long decodedLong = Long.decode("64403L");
        ```

        然后，我们会看到同样的错误行为。

        我们可以用类似的方法解决这些问题：

        ```java
        int aIntPrim = Integer.parseInt("2");
        double aDoublePrim = Double.parseDouble("2.2");
        Integer aIntObj = Integer.valueOf("3");
        Long decodedLong = Long.decode("64403");
        ```

    3. 传递带有外来字符的字符串

        或者，如果我们尝试将字符串转换为数字，但输入中包含了不相干的数据，如空白或特殊字符：

        ```java
        Short shortInt = new Short("2 ");
        int bIntPrim = Integer.parseInt("_6000");
        ```

        然后，我们会遇到和以前一样的问题。

        我们可以通过字符串操作来纠正这些问题：

        ```java
        Short shortInt = new Short("2 ".trim());
        int bIntPrim = Integer.parseInt("_6000".replaceAll("_", ""));
        int bIntPrim = Integer.parseInt("-6000");
        ```

        请注意，第 3 行允许使用连字符作为减号来表示负数。

    4. 特定地区的数字格式

        让我们来看看本地特定数字的一个特例。在欧洲地区，逗号可以代表小数位。例如，"4000,1" 可以表示十进制数 "4000.1"。

        默认情况下，如果尝试解析包含逗号的数值，我们将收到 NumberFormatException 异常：

        `double aDoublePrim = Double.parseDouble("4000,1");`

        在这种情况下，我们需要允许逗号并避免异常。为此，Java 需要将此处的逗号理解为小数。

        我们可以使用 NumberFormat 来允许欧洲地区使用逗号并避免异常。

        让我们以法国的 Locale 为例，看看它的实际应用：

        ```java
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);
        Number parsedNumber = numberFormat.parse("4000,1");
        assertEquals(4000.1, parsedNumber.doubleValue());
        assertEquals(4000, parsedNumber.intValue());
        ```

3. 最佳实践

    让我们来谈谈可以帮助我们处理 NumberFormatException 的一些良好做法：

    - 不要尝试将字母或特殊字符转换成数字--Java Number API 无法做到这一点。
    - 我们可能希望使用正则表达式[验证输入字符串](https://www.baeldung.com/java-check-string-number)，并抛出无效字符异常。
    - 我们可以使用 trim() 和 replaceAll() 等方法针对可预见的已知问题对输入进行消毒。
    - 在某些情况下，输入中的特殊字符可能是有效的。因此，我们使用支持多种格式的 NumberFormat 对其进行特殊处理。

4. 总结

    在本教程中，我们讨论了 Java 中的 NumberFormatException 及其产生原因。了解这种异常有助于我们创建更健壮的应用程序。

    此外，我们还学习了在输入无效字符串时避免异常的策略。

    最后，我们还了解了一些处理 NumberFormatException 的最佳实践。

## 相关文章

- [x] [Understanding the NumberFormatException in Java](https://www.baeldung.com/java-number-format-exception)
