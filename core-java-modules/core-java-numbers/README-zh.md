# Java数字烹饪书和示例

本模块包含有关 Java 中数字的文章。

- [Java 中整数的位数](https://www.baeldung.com/java-number-of-digits-in-int)
- [x] [如何在Java中将一个数字四舍五入到N个小数位](#如何在java中将一个数字四舍五入到n个小数位)
- [Java 中的 BigDecimal 和 BigInteger](https://www.baeldung.com/java-bigdecimal-biginteger)
- [Java 中查找数组中相加等于给定和的所有数对](https://www.baeldung.com/java-algorithm-number-pairs-sum)
- [x] [Java - 随机长数、浮点数、整数和双数](#java随机长型浮点型整型和双型)
- [十进制格式实用指南](https://www.baeldung.com/java-decimalformat)
- [Java 中计算 n 次根](https://www.baeldung.com/java-nth-root)
- [将双倍值转换为字符串，去掉小数位](https://www.baeldung.com/java-double-to-string)
- [改变求和操作的顺序会产生不同的结果吗？](https://www.baeldung.com/java-floating-point-sum-order)
- [在度数中使用 Math.sin](https://www.baeldung.com/java-math-sin-degrees)
- 更多文章： [[下-->]](../core-java-numbers-2)

## Java随机长型、浮点型、整型和双型

本快速教程将说明如何使用普通 Java 和 Apache Commons 数学库首先生成一个长。

测试代码：JavaRandomUnitTest.java

1. 生成一个无界长数

    让我们从生成一个 Long 开始：

    givenUsingPlainJava_whenGeneratingRandomLongUnbounded_thenCorrect()

2. 在一定范围内生成长文本

    1. 使用普通 Java 创建随机 Long

        接下来，让我们看看如何创建一个随机有界的 Long 值，即在给定范围或区间内的 Long 值：

        givenUsingPlainJava_whenGeneratingRandomLongBounded_thenCorrect()

    2. 使用 Apache Commons Math 随机生成长文本

        让我们看看如何使用更简洁的 API 和 Commons Math 生成随机 Long：

        givenUsingApacheCommons_whenGeneratingRandomLongBounded_thenCorrect()

3. 生成无界整数

    让我们继续生成一个无边界的随机整数：

    givenUsingPlainJava_whenGeneratingRandomIntegerUnbounded_thenCorrect()

    正如您所看到的，它非常接近于生成一个 long。

4. 在一定范围内生成整数

    1. 使用普通 Java 生成随机整数

        下一步 - 在给定范围内生成一个随机整数：

        givenUsingPlainJava_whenGeneratingRandomIntegerBounded_thenCorrect()

    2. 使用公共数学生成随机整数

        使用 Common Math 也是如此：

        givenUsingApache_whenGeneratingRandomIntegerBounded_thenCorrect()

5. 生成无界浮点数

    现在，让我们开始生成随机浮点数--首先是无界浮点数：

    givenUsingPlainJava_whenGeneratingRandomFloatUnbouned_thenCorrect()

6. 在一定范围内生成浮点数

    1. 使用普通 Java 生成随机浮点数

        以及一个有界随机浮点数：

        givenUsingPlainJava_whenGeneratingRandomFloatBouned_thenCorrect()

    2. 带有下议院数学的随机浮点数

        现在，使用 Commons Math 创建一个有界随机浮点数：

        givenUsingApache_whenGeneratingRandomFloatBounded_thenCorrect()

7. 生成无界双

    1. 使用普通 Java 生成随机无界双倍值

        最后，我们将生成随机双数值--首先，使用 Java 数学 API：

        givenUsingPlainJava_whenGeneratingRandomDoubleUnbounded_thenCorrect()

    2. 使用下议院数学生成随机无界双倍值

        以及使用 Apache Commons 数学库生成随机双数值：

        givenUsingApache_whenGeneratingRandomDoubleUnbounded_thenCorrect()

8. 在一定范围内生成一个 double

    1. 使用普通 Java 生成有界随机双倍值

        在这个示例中，让我们看看用 Java 在区间内生成随机双倍值的情况：

        givenUsingPlainJava_whenGeneratingRandomDoubleBounded_thenCorrect()

    2. 使用下议院数学的随机有界双倍值

        最后，使用 Apache Commons Math 库在一个区间内随机生成一个双数值：

        givenUsingApache_whenGeneratingRandomDoubleBounded_thenCorrect()

        就是这样--快速、直观地举例说明如何为 Java 中最常见的数字原语生成无界值和有界值。

9. 总结

    本教程介绍了如何使用不同的技术和库生成有界或无界随机数。

## 如何在Java中将一个数字四舍五入到N个小数位

1. 概述

    在这个简短的教程中，我们将学习如何在 Java 中将一个数字四舍五入到小数点后 n 位。

2. Java 中的小数

    Java 提供了两种用于存储小数的原始类型：float 和 double。double 是默认类型：

    `double PI = 3.1415;`
    但是，我们绝不能将这两种类型用于精确数值，如货币。为此，我们可以使用 BigDecimal 类进行四舍五入。

    ```java
    public static void test() {
        double d1 = 11000;
        double d2 = 0.35;
        // 错误的：3849.9999999999995
        System.out.println("错误的：" + d1 * d2);
        BigDecimal bigDecimal1 = new BigDecimal(11000);
        BigDecimal bigDecimal2 = BigDecimal.valueOf(0.35);
        // multiply 乘法；正确的：3850.00
        System.out.println("正确的：" + bigDecimal1.multiply(bigDecimal2));
    }
    ```

3. 小数的格式化

    如果我们只想打印一个小数点后有 n 位数字的小数，我们可以简单地格式化输出字符串：

    `System.out.printf("Value with 3 digits after decimal point %.3f %n", PI);`

    输出：小数点后三位数的值 3.142。

    或者，我们也可以使用 DecimalFormat 类来格式化数值：math/Round.java\main()

    与上面使用的 String.format() 相比，DecimalFormat 允许我们明确设置四舍五入行为，从而对输出进行更多控制。

4. 使用 BigDecimal 对二进制进行四舍五入

    要将二进制数舍入到小数点后 n 位，我们可以编写一个辅助方法：

    math/Round.java\double()

    在此解决方案中有一点需要注意：在构造 BigDecimal 时，我们必须始终使用 BigDecimal(String) 构造函数。这样可以防止出现表示不精确值的问题。

    我们可以通过使用 [Apache Commons math](http://commons.apache.org/proper/commons-math/)库实现同样的结果：

    `<groupId>org.apache.commons</groupId><artifactId>commons-math3</artifactId>`

    最新版本可在此处找到。

    将该库添加到项目中后，我们就可以使用 Precision.round() 方法，该方法需要两个参数--值和刻度：

    `Precision.round(PI, 3);`

    默认情况下，它使用与我们的辅助方法相同的 HALF_UP 舍入方法；因此，结果应该是相同的。

    请注意，我们可以通过将所需的舍入方法作为第三个参数传递来改变舍入行为。

5. 使用 DoubleRounder 对双倍数进行四舍五入

    DoubleRounder 是 [decimal4j](https://github.com/tools4j/decimal4j) 库中的一个工具。它提供了一种快速、无垃圾的方法，用于对小数点后 0 到 18 位的二进制数进行四舍五入。

    我们可以通过在 pom.xml 中添加依赖关系来获取该库（最新版本可在此处找到）：

    `<groupId>org.decimal4j</groupId><artifactId>decimal4j</artifactId>`

    现在我们可以简单地使用

    DoubleRounder.round(PI,3);

    然而，DoubleRounder 在一些情况下会失败：

    `System.out.println(DoubleRounder.round(256.025d, 2));`

    输出：256.02，而不是预期的 256.03
6. Math.round() 方法

    另一种数字四舍五入的方法是使用 Math.Round() 方法。

    在这种情况下，我们可以通过乘除 10^n 来控制小数点后的 n 位数：

    math/Round.java\roundAvoid()

    不推荐使用此方法，因为它会截断数值。在许多情况下，数值的四舍五入是不正确的。

    因此，在此列出此方法仅供学习之用。

7. 结论

    在本文中，我们介绍了将数字四舍五入到小数点后 n 位的不同技巧。

    我们可以简单地格式化输出而不改变数值，或者使用辅助方法对变量进行四舍五入。我们还讨论了一些处理该问题的库。

## Code

本文中使用的代码可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-numbers) 上找到。

## Relevant Articles

- [Number of Digits in an Integer in Java](https://www.baeldung.com/java-number-of-digits-in-int)
- [How to Round a Number to N Decimal Places in Java](https://www.baeldung.com/java-round-decimal-number)
- [BigDecimal and BigInteger in Java](https://www.baeldung.com/java-bigdecimal-biginteger)
- [Find All Pairs of Numbers in an Array That Add Up to a Given Sum in Java](https://www.baeldung.com/java-algorithm-number-pairs-sum)
- [Java – Random Long, Float, Integer and Double](https://www.baeldung.com/java-generate-random-long-float-integer-double)
- [A Practical Guide to DecimalFormat](https://www.baeldung.com/java-decimalformat)
- [Calculating the nth Root in Java](https://www.baeldung.com/java-nth-root)
- [Convert Double to String, Removing Decimal Places](https://www.baeldung.com/java-double-to-string)
- [Changing the Order in a Sum Operation Can Produce Different Results?](https://www.baeldung.com/java-floating-point-sum-order)
- [Using Math.sin with Degrees](https://www.baeldung.com/java-math-sin-degrees)
- More articles: [[next -->]](../core-java-numbers-2)
