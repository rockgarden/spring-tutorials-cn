# [使用 Java 实现概率](https://www.baeldung.com/java-probability)

Java+

1. 概述

    在本教程中，我们将通过几个例子来了解如何用 Java 来实现概率。

2. 模拟基础概率

    要在 Java 中模拟概率，我们需要做的第一件事就是生成随机数。幸运的是，Java 为我们提供了大量的随机数生成器。

    在这种情况下，我们将使用 [SplittableRandom](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/SplittableRandom.html) 类，因为它提供了高质量的随机性，并且速度相对较快：

    `SplittableRandom random = new SplittableRandom();`

    然后我们需要在一个范围内生成一个数字，并将其与从该范围中选择的另一个数字进行比较。范围内的每个数字被抽取的机会是均等的。由于我们知道了范围，因此我们知道抽到所选数字的概率。这样我们就控制了概率：

    `boolean probablyFalse = random.nextInt(10) == 0;`

    在这个例子中，我们从 0 到 9 的范围内抽取数字。因此，抽到 0 的概率等于 10%。现在，让我们获取一个随机数并测试所选数字是否小于抽到的数字：

    `boolean whoKnows = random.nextInt(1, 101) <= 50;`

    在这里，我们从 1 到 100 的范围内抽取数字。我们的随机数小于或等于 50 的机会正好是 50%。

3. 均匀分布

    到目前为止生成的值属于均匀分布。这意味着每个事件（例如掷骰子出现某个数字）发生的概率都是相同的。

    1. 以给定概率调用函数

        现在，假设我们想偶尔执行一项任务并控制其概率。例如，我们运营着一个电子商务网站，我们希望给 10% 的用户打折。

        为此，我们实现一个方法，它将接受三个参数：一个在一定比例的情况下调用的供应商，一个在其余情况下调用的供应商，以及概率。

        首先，我们使用 [Vavr](https://www.baeldung.com/vavr) 将我们的 SplittableRandom 声明为 [Lazy](https://javadoc.io/doc/io.vavr/vavr/0.9.2/io/vavr/Lazy.html)。这样，我们只会在第一次请求时实例化它一次：

        `private final Lazy<SplittableRandom> random = Lazy.of(SplittableRandom::new);`

        然后，我们将实现管理概率的函数：

        ```java
        public <T> T withProbability(Supplier<T> positiveCase, Supplier<T> negativeCase, int probability) {
            SplittableRandom random = this.random.get();
            if (random.nextInt(1, 101) <= probability) {
                return positiveCase.get();
            } else {
                return negativeCase.get();
            }
        }
        ```

    2. 使用蒙特卡洛方法采样概率

        让我们逆转我们在上一节中看到的过程。为此，我们将使用蒙特卡洛方法来测量概率。它会生成大量随机事件，并计算满足所提供条件的数量。当概率难以或无法解析计算时，这非常有用。

        例如，如果我们看六面骰子，我们知道掷出某个数字的概率是 1/6。但是，如果我们有一个未知面数的神秘骰子，就很难说出概率是多少。与其分析这个骰子，不如多次掷它并统计某些事件发生的次数。

        让我们看看如何实现这种方法。首先，我们将尝试生成一百万次 1 的概率为 10%，并进行计数：

        ```java
        int numberOfSamples = 1_000_000;
        int probability = 10;
        int howManyTimesInvoked = 
        Stream.generate(() -> randomInvoker.withProbability(() -> 1, () -> 0, probability))
            .limit(numberOfSamples)
            .mapToInt(e -> e)
            .sum();
        ```

        然后，生成的数字之和除以样本数量将是事件概率的近似值：

        `int monteCarloProbability = (howManyTimesInvoked * 100) / numberOfSamples;`

        请注意，计算出的概率是近似的。样本数量越高，近似结果越好。

4. 其他分布

    均匀分布适用于建模诸如游戏之类的事物。为了保证游戏公平，所有事件通常需要具有相同的发生概率。

    然而，在现实生活中，分布通常更为复杂。不同事情发生的几率并不相等。

    例如，个子特别矮的人和个子特别高的人都非常少。大多数人身高处于平均水平，这意味着人的身高遵循[正态分布](https://en.wikipedia.org/wiki/Normal_distribution)。如果我们要生成随机的人类身高，则仅生成一个随机英尺数是不够的。

    幸运的是，我们不需要自己实现底层数学模型。我们需要知道使用哪种分布以及如何配置它，例如使用统计数据。

    Apache Commons 库为我们提供了几种分布的实现。让我们使用它来实现正态分布：

    ```java
    private static final double MEAN_HEIGHT = 176.02;
    private static final double STANDARD_DEVIATION = 7.11;
    private static NormalDistribution distribution = new NormalDistribution(MEAN_HEIGHT, STANDARD_DEVIATION);
    ```

    使用这个 API 非常简单 —— sample 方法从分布中抽取一个随机数：

    ```java
    public static double generateNormalHeight() {
        return distribution.sample();
    }
    ```

    最后，让我们反转这个过程：

    ```java
    public static double probabilityOfHeightBetween(double heightLowerExclusive, double heightUpperInclusive) {
        return distribution.probability(heightLowerExclusive, heightUpperInclusive);
    }
    ```

    结果，我们将得到一个人身高介于两个边界之间的概率。在这种情况下，是较低和较高的身高。

5. 结论

    在本文中，我们学习了如何生成随机事件以及如何计算它们发生的概率。我们使用了均匀分布和正态分布来模拟不同的情况。
