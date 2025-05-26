# Java随机长型、浮点型、整型和双型

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
