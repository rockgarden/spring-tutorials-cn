# 如何在Java中将一个数字四舍五入到N个小数位

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
