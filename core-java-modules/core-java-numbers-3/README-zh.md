# Java数字烹饪书和示例

本模块包含有关 Java 中数字的文章。

- [用 Java 生成随机数](https://www.baeldung.com/java-generating-random-numbers)
- [Java 中将 Double 转换为 Long](https://www.baeldung.com/java-convert-double-long)
- [在 Double.parseDouble 中调用 Parse 之前检查空值](https://www.baeldung.com/java-check-null-parse-double)
- [在 Java 中生成范围内的随机数](https://www.baeldung.com/java-generating-random-numbers-in-range)
- [在 Java 中列出范围内的数字](https://www.baeldung.com/java-listing-numbers-within-a-range)
- [Java 中的斐波纳契数列](https://www.baeldung.com/java-fibonacci)
- [Java 中的数字类指南](https://www.baeldung.com/java-number-class)
- [Java 中以二进制格式打印整数](https://www.baeldung.com/java-print-integer-binary)
- [x] [Java中的数字格式化](#java中的数字格式化)
- [Java 中的零除法：异常、无穷大或不是数](https://www.baeldung.com/java-division-by-zero)

## Java中的数字格式化

1. 概述

    在本教程中，我们将了解 Java 中数字格式化的不同方法，以及如何实现这些方法。

2. 使用 String#format 进行基本数字格式化

    [String#format](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#format) 方法对于数字格式化非常有用。该方法需要两个参数。第一个参数描述了我们希望看到的小数位数的模式，第二个参数是给定的值：

    ```java
    double value = 4.2352989244d;
    assertThat(String.format("%.2f", value)).isEqualTo("4.24");
    assertThat(String.format("%.3f", value)).isEqualTo("4.235");
    ```

3. 通过四舍五入进行小数格式化

    在 Java 中，我们有两种表示小数的原始类型：float 和 decimal：

    `double myDouble = 7.8723d;`
    `float myFloat = 7.8723f;`

    小数位数可能会因操作的不同而不同。在大多数情况下，我们只对小数点后的[前几位感兴趣](https://www.baeldung.com/java-round-decimal-number)。让我们来看看通过四舍五入格式化小数的一些方法。

    1. 使用 BigDecimal 进行数字格式化

        BigDecimal 类提供了四舍五入到指定小数位数的方法。让我们创建一个辅助方法，返回四舍五入到所需位数的 double：

        FormatNumber.java\withBigDecimal()

        我们先创建一个带有原始十进制值的 BigDecimal 新实例。然后，通过设置刻度，我们将提供所需的小数位数以及四舍五入的方式。使用这种方法，我们可以轻松格式化一个 double 值：

        FormatNumberUnitTest.java\givenDecimalNumber_whenFormatNumberWithBigDecimal_thenGetExpectedResult()

    2. 使用 Math#round

        我们还可以利用 [Math](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Math.html) 类中的静态方法将双数值舍入到指定的小数位。在这种情况下，我们可以通过乘以 10^n 并除以 10^n 来调整小数位数。让我们检查一下我们的辅助方法：

        FormatNumber.java\withMathRound(double value, int places)

        FormatNumberUnitTest.java\givenDecimalNumber_whenFormatNumberWithMathRound_thenGetExpectedResult()

        不过，只有在特殊情况下才会推荐使用此选项，因为有时输出结果在打印前的四舍五入可能与预期不同。

        这是因为 Math#round 正在截断数值。让我们看看这种情况是如何发生的：

        ```java
        System.out.println(withMathRound(1000.0d, 17));
        // Gives: 92.23372036854776 !!
        System.out.println(withMathRound(260.775d, 2));
        // Gives: 260.77 instead of expected 260.78
        ```

        所以请注意，此方法仅供学习之用。

4. 格式化不同类型的数字

    在某些特殊情况下，我们可能需要格式化特定类型的数字，如货币、大整数或百分比。

    1. 用逗号格式化大整数

        当我们的应用程序中出现大整数时，我们可能希望通过使用带有预定义模式的 [DecimalFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/DecimalFormat.html) 来用逗号显示它：

        FormatNumber.java\withLargeIntegers(double value)

        FormatNumberUnitTest.java\givenIntegerNumber_whenFormatNumberWithLargeIntegers_thenGetExpectedResult()

    2. 填充数字

        在某些情况下，我们可能需要在指定长度的数字中填充零。在这种情况下，我们可以使用 String#format 方法，如前所述：

        FormatNumber.java\byPaddingZeros(int value, int paddingLength)

    3. 格式化小数点后有两个零的数字

        为了打印小数点后有两个零的数字，我们将再次使用带有预定义模式的 DecimalFormat 类：

        FormatNumber.java\withTwoDecimalPlaces(double value)

        在本例中，我们创建了一个新格式，其模式指定了小数点后的两个零。

    4. 格式化和百分比

        我们有时可能需要显示百分比。

        在这种情况下，我们可以使用 NumberFormat#getPercentInstance 方法。该方法允许我们提供一个 Locale，以便按照我们指定的国家的正确格式打印数值：

        FormatNumber.java\forPercentages(double value, Locale locale)

    5. 货币数字格式化

        在应用程序中存储货币的常用方法是使用 BigDecimal。如果要向用户显示货币，我们可以使用 NumberFormat 类：

        FormatNumber.java\currencyWithChosenLocalisation(double value, Locale locale)

        我们为给定的 Locale 获取货币实例，然后使用值简单地调用 format 方法。结果就是显示为指定国家货币的数字。

5. 高级格式化用例

    DecimalFormat 是 Java 中最常用的小数格式化方法之一。与前面的示例类似，我们将编写一个辅助方法：

    FormatNumber.java\withDecimalFormatLocal(double value)

    我们的格式化类型将获得特定本地化的默认设置。

    十进制格式在不同国家的数字系统中有不同的处理方式。这包括分组字符（美国为逗号，其他国家为空格或点）、分组大小（美国和大多数国家为 3，印度不同）或小数字符（美国为点，其他国家为逗号）。

    我们还可以扩展此功能，以提供一些特定模式：

    FormatNumber.java\withDecimalFormatPattern(double value, int places)

    在这里，我们允许用户根据空格数选择模式来配置 DecimalFormat。

6. 结论

    在本文中，我们简要探讨了 Java 中数字格式化的不同方法。正如我们所看到的，没有一种最好的方法。我们可以使用多种方法，因为它们各有特点。

## Code

一如既往，这些示例的代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-numbers-3) 上获取。

## 相关文章

## Relevant Articles

- [Generating Random Numbers in Java](https://www.baeldung.com/java-generating-random-numbers)
- [Convert Double to Long in Java](https://www.baeldung.com/java-convert-double-long)
- [Check for null Before Calling Parse in Double.parseDouble](https://www.baeldung.com/java-check-null-parse-double)
- [Generating Random Numbers in a Range in Java](https://www.baeldung.com/java-generating-random-numbers-in-range)
- [Listing Numbers Within a Range in Java](https://www.baeldung.com/java-listing-numbers-within-a-range)
- [Fibonacci Series in Java](https://www.baeldung.com/java-fibonacci)
- [Guide to the Number Class in Java](https://www.baeldung.com/java-number-class)
- [Print an Integer in Binary Format in Java](https://www.baeldung.com/java-print-integer-binary)
- [Number Formatting in Java](https://www.baeldung.com/java-number-formatting)
- [Division by Zero in Java: Exception, Infinity, or Not a Number](https://www.baeldung.com/java-division-by-zero)
- More articles: [[<-- prev]](../core-java-numbers-2) [[next -->]](../core-java-numbers-4)
