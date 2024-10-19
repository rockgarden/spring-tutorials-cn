# 算法-杂项

本模块包含有关算法的文章。某些算法类别，如[排序](/algorithms-sorting)和[遗传算法](/algorithms-genetic)，有自己专门的模块。
[遗传算法](/algorithms-genetic) 有自己的专门模块。

- [Java 双指针技术](https://www.baeldung.com/java-two-pointer-technique)
- [使用 Java 枚举实现简单状态机](https://www.baeldung.com/java-enum-simple-state-machine)
- [在 Java 中转换罗马数字和阿拉伯数字](https://www.baeldung.com/java-convert-roman-arabic)
- [在 Java 中检查列表是否排序](https://www.baeldung.com/java-check-if-list-sorted)
- [Java 中的折叠技术指南](https://www.baeldung.com/folding-hashing-technique)
- [用 Java 中的 for 循环创建三角形](https://www.baeldung.com/java-print-triangle)
- [ ] [Java中的K-Means聚类算法](#java-中的-k-means-聚类算法)

## Java 中的 K-Means 聚类算法

[算法](https://www.baeldung.com/category/algorithms)

1. 聚类概述

    聚类是一类无监督算法的总称，用于发现彼此密切相关的事物、人或想法的群体。

    在这个看似简单的单行定义中，我们看到了一些热门词汇。究竟什么是聚类？什么是无监督算法？

    在本教程中，我们将首先对这些概念做一些说明。然后，我们将看看这些概念在 Java 中是如何体现的。

2. 无监督算法

    在使用大多数学习算法之前，我们应该以某种方式向它们提供一些样本数据，让算法从这些数据中学习。在机器学习术语中，我们将样本数据集称为训练数据。同时，整个过程也被称为训练过程。

    总之，我们可以根据学习算法在训练过程中所需的监督量对其进行分类。这类学习算法主要有以下两种：

    - 监督学习： 在监督算法中，训练数据应包括每个点的实际解。例如，如果我们要训练垃圾邮件过滤算法，就需要向算法输入样本邮件及其标签，即垃圾邮件或非垃圾邮件。从数学上讲，我们要从包括 xs 和 ys 的训练集中推断出 f(x)。
    - 无监督学习： 当训练数据中没有标签时，算法就是无监督算法。例如，我们有大量关于音乐家的数据，我们要从数据中发现相似的音乐家群体。
3. 聚类

    聚类是一种无监督算法，用于发现相似事物、想法或人的群体。与监督算法不同，我们不是用已知标签的示例来训练聚类算法。相反，聚类试图在训练集中找到结构，而数据中的任何一点都不是标签。

    1. K-Means 聚类

        K-Means 是一种聚类算法，它有一个基本特性：聚类的数量是事先定义好的。除 K-Means 外，还有其他类型的聚类算法，如层次聚类（Hierarchical Clustering）、亲和传播（Affinity Propagation）或光谱聚类（Spectral Clustering）。

    2. K-Means 如何工作

        假设我们的目标是在一个数据集中找到几个相似的组，比如

        ![第一步](pic/Date-6.webp)
        K-Means 以随机放置的 k 个中心点开始。顾名思义，中心点就是聚类的中心点。例如，这里我们添加了四个随机中心点：

        ![随机中心点](pic/Date-7.png)
        然后，我们将每个现有数据点分配给与其最近的中心点：

        ![分配](pic/Date-8.webp)
        分配后，我们将中心点移动到分配给它的点的平均位置。记住，中心点应该是聚类的中心点：

        ![日期 10](pic/Date-10.webp)
        每次我们完成中心点的移动后，当前迭代就结束了。我们会重复这些迭代，直到多个连续迭代之间的赋值停止变化：

        ![复制日期](pic/Date-copy.webp)
        当算法结束时，这四个聚类如期找到。现在我们知道了 K-Means 的工作原理，让我们用 Java 来实现它。

    3. 特征表示

        在对不同的训练数据集建模时，我们需要一种数据结构来表示模型属性及其相应的值。例如，音乐家可以有一个流派属性，其值为摇滚。我们通常使用术语"特征"来指代属性及其值的组合。

        为了给特定的学习算法准备数据集，我们通常会使用一组通用的数字属性，用来比较不同的项目。例如，如果我们让用户给每位艺术家贴上流派标签，那么在一天结束时，我们就可以统计每位艺术家被贴上特定流派标签的次数：

        ![Screen-Shot-1398-04-29-at-22.30.58](pic/Screen-Shot-1398-04-29-at-22.30.58.png)
        像 Linkin Park 这样的艺人的特征向量是 [摇滚 -> 7890，新金属 -> 700，另类 -> 520，流行 -> 3]。因此，如果我们能找到一种将属性表示为数值的方法，那么我们就可以通过比较两个不同的项目（例如艺术家）的相应向量项来进行比较。

        既然数值向量是如此通用的数据结构，我们就用它来表示特征。下面是我们如何在 Java 中实现特征向量：

        ```java
        public class Record {
            private final String description;
            private final Map<String, Double> features;
        }
        ```

    4. 查找相似项目

        在 K-Means 的每次迭代中，我们都需要一种方法来找到与数据集中每个项最近的中心点。计算两个特征向量之间距离的最简单方法之一是使用欧氏距离（Euclidean Distance）。两个向量 [p1, q1] 和 [p2, q2] 之间的[欧氏距离](https://en.wikipedia.org/wiki/Euclidean_distance)等于：

        \[d(p,q)=\sqrt{(q1-p1)^2+(q2-p2)^2}\]

        让我们用 Java 来实现这个函数。首先是抽象

        ```java
        public interface Distance {
            double calculate(Map<String, Double> f1, Map<String, Double> f2);
        }
        ```

        除了欧氏距离外，还有其他方法可以计算不同项目之间的距离或相似性，如皮尔逊相关系数。这种抽象使得在不同的距离度量之间切换非常容易。

        让我们来看看欧氏距离的实现：

        algorithms.kmeans\EuclideanDistance.java

        首先，我们计算相应条目之间的平方差之和。然后，通过使用 sqrt 函数，计算出实际的欧氏距离。

    5. 中心点表示法

        中心点与普通特征处于同一空间，因此我们可以用类似于特征的方法来表示它们：

        ```java
        public class Centroid {
            private final Map<String, Double> coordinates;
        }
        ```

        现在我们已经有了一些必要的抽象概念，是时候编写 K-Means 的实现了。下面快速浏览一下我们的方法签名：

        ```java
        public class KMeans {
            private static final Random random = new Random();
            public static Map<Centroid, List<Record>> fit(List<Record> records, 
            int k, 
            Distance distance, 
            int maxIterations) { 
                // omitted
            }
        }
        ```

        让我们分解一下这个方法的签名：

        - 数据集是一组特征向量。由于每个特征向量都是一个记录，因此数据集类型是 `List<Record>`
        - k 参数决定了聚类的数量，我们应该事先提供这个参数
        - 距离表示我们计算两个特征之间差异的方式
        - 当赋值在连续几次迭代中停止变化时，K-Means 就会终止。除了这个终止条件，我们还可以为迭代次数设置一个上限。maxIterations 参数决定了迭代次数的上限。
        - 当 K-Means 终止时，每个中心点都应该有几个分配的特征，因此我们使用 `Map<Centroid, List<Record>>` 作为返回类型。基本上，每个 Map 条目对应一个聚类。
    6. 生成中心点

        第一步是随机生成 k 个中心点。

        虽然每个中心点可以包含完全随机的坐标，但好的做法是在每个属性的最小值和最大值之间生成随机坐标。在不考虑可能值范围的情况下生成随机中心点会导致算法收敛速度变慢。

        首先，我们应该计算每个属性的最小值和最大值，然后在每对值之间生成随机值：

        参见 algorithms.kmeans\kmeans.java

        `private static List<Centroid> randomCentroids(List<Record> records, int k)`

        现在，我们可以将每条记录分配给其中一个随机中心点。

    7. 分配

        代码参见 algorithms.kmeans\kmeans.java

        首先，给定一个记录，我们应该找到离它最近的中心点：

        `private static Centroid nearestCentroid(Record record, List<Centroid> centroids, Distance distance)`

        每条记录都属于与其最近的中心点聚类：

        `private static void assignToCluster(Map<Centroid, List<Record>> clusters, Record record, Centroid centroid)`

    8. 中心点重定位

        如果经过一次迭代后，某个中心点不包含任何赋值，那么我们就不会重新定位它。否则，我们应将每个属性的中心点坐标重定位到所有分配记录的平均位置：

        `private static Centroid average(Centroid centroid, List<Record> records)`

        既然我们可以重新定位单个中心点，那么现在就可以实现 relocateCentroids 方法了：

        `private static List<Centroid> relocateCentroids(Map<Centroid, List<Record>> clusters)`

        这个简单的单行程序会遍历所有中心点，重新定位它们，并返回新的中心点。

    9. 将所有内容放在一起

        在每次迭代中，将所有记录分配到与其最近的中心点后，首先，我们应比较当前分配和上次迭代。

        如果赋值相同，则算法终止。否则，在跳转到下一次迭代之前，我们应该重新定位中心点：

        `public static Map<Centroid, List<Record>> fit(List<Record> records, int k, Distance distance, int maxIterations)`

4. 示例 在 Last.fm 上发现相似的艺术家

    Last.fm 通过记录用户所听音乐的细节，为每个用户建立了详细的音乐品味档案。在本节中，我们将发现相似艺术家的群集。为了建立适合这项任务的数据集，我们将使用 Last.fm 的三个 API：

    - 获取 Last.fm 上顶级艺术家集合的 [API](https://www.last.fm/api/show/chart.getTopArtists)。
    - 另一个 [API](https://www.last.fm/api/show/chart.getTopTags) 用于查找流行标签。每个用户都可以给艺人贴标签，例如摇滚。因此，Last.fm 维护着一个包含这些标签及其频率的数据库。
    - 此外，Last.fm 还提供了一个 [API](https://www.last.fm/api/show/artist.getTopTags)，用于按流行度排序，获取艺人的热门标签。由于此类标签数量众多，我们将只保留全球最热门的标签。
    1. Last.fm 的 API

        要使用这些 API，我们应从 Last.fm 获取一个 [API Key](https://www.last.fm/api/authentication)，并在每次 HTTP 请求中发送该 Key。我们将使用以下 [Retrofit](https://www.baeldung.com/retrofit) 服务来调用这些 API：

        参见 LastFmService.java

        现在，让我们查找 Last.fm 上最受欢迎的艺术家：

        代码参见 LastFm.java

        `private static List<String> getTop100Artists()`

        同样，我们也可以获取热门标签：

        `private static Set<String> getTop100Tags()`

        最后，我们可以建立一个包含艺术家及其标签频率的数据集：

        `private static List<Record> datasetWithTaggedArtists(List<String> artists, Set<String> topTags)`

    2. 形成艺术家集群

        现在，我们可以将准备好的数据集输入 K-Means 实现：

        代码参见 LastFm.java main()

        如果我们运行这段代码，它将以文本输出的形式显示聚类：

        ```log
        ------------------------------ CLUSTER -----------------------------------
        Centroid {classic rock=65.58333333333333, rock=64.41666666666667, british=20.333333333333332, ... }
        David Bowie, Led Zeppelin, Pink Floyd, System of a Down, Queen, blink-182, The Rolling Stones, Metallica, 
        Fleetwood Mac, The Beatles, Elton John, The Clash
        ......
        ```

        由于中心点坐标是按平均标签频率排序的，因此我们可以很容易地发现每个聚类中的主要流派。例如，最后一个聚类是由老牌摇滚乐队组成的聚类，而第二个聚类则充满了说唱明星。

        虽然这种聚类很有意义，但在大多数情况下并不完美，因为数据只是从用户行为中收集的。

5. 可视化

    刚才，我们的算法以终端友好的方式可视化了艺术家集群。如果我们将集群配置转换为 JSON 并将其输入到 D3.js，那么只需几行 JavaScript，我们就能得到一个漂亮的、人性化的径向整齐树（[Radial Tidy-Tree](https://observablehq.com/@d3/radial-tidy-tree?collection=@d3/d3-hierarchy)）：

    ![Screen-Shot-1398-05-04-at-12.09.40](pic/Screen-Shot-1398-05-04-at-12.webp)
    我们必须将 `Map<Centroid, List<Record>>` 转换为模式类似于 d3.js 示例的 JSON。

6. 聚类数量

    K-Means 的基本特性之一是我们应该事先定义聚类的数量。到目前为止，我们使用的是 k 的静态值，但确定这个值可能是一个具有挑战性的问题。计算簇数有两种常见方法：

    - 领域知识
    - 数学启发法
    如果我们足够幸运，对该领域非常了解，那么我们或许可以简单地猜出正确的数字。否则，我们可以应用一些启发式方法，如肘部法或剪影法，来了解聚类的数量。

    在继续深入之前，我们应该知道，这些启发式方法虽然有用，但只是启发式方法，可能无法提供明确的答案。

    1. 肘法

        要使用肘(Elbow)法，我们首先要计算每个聚类中心点与其所有成员之间的差值。当我们将更多不相关的成员归入一个聚类时，中心点与其成员之间的距离就会增加，因此聚类质量就会下降。

        进行距离计算的一种方法是使用平方误差之和。平方误差之和或 SSE 等于中心点与其所有成员之间的平方差之和：

        algorithms.kmeans\Errors.java

        然后，我们可以针对不同的 k 值运行 K-Means 算法，并计算每个值的 SSE：

        ```java
        List<Record> records = // the dataset;
        Distance distance = new EuclideanDistance();
        List<Double> sumOfSquaredErrors = new ArrayList<>();
        for (int k = 2; k <= 16; k++) {
            Map<Centroid, List<Record>> clusters = KMeans.fit(records, k, distance, 1000);
            double sse = Errors.sse(clusters, distance);
            sumOfSquaredErrors.add(sse);
        }
        ```

        最后，我们可以通过绘制聚类数量与 SSE 的对比图来找到合适的 k 值：

        ![Screen-Shot-1398-05-04-at-17.01.36](pic/Screen-Shot-1398-05-04-at-17.webp)
        通常情况下，随着簇数的增加，簇成员之间的距离会减小。不过，我们不能随意选择较大的 k 值，因为多个簇中只有一个成员，这有违聚类的初衷。

        肘法背后的理念是找到一个合适的 k 值，使 SSE 在该值附近急剧下降。例如，k=9 就是一个很好的候选值。

7. 结论

在本教程中，我们首先介绍了机器学习的几个重要概念。然后，我们熟悉了 K-Means 聚类算法的机制。最后，我们为 K-Means 写了一个简单的实现，用 Last.fm 中的一个真实数据集测试了我们的算法，并以一种漂亮的图形方式可视化了聚类结果。

## Code

和往常一样，示例代码可以在我们的 [GitHub](https://github.com/eugenp/tutorials/tree/master/algorithms-modules/algorithms-miscellaneous-3) 项目中找到。

## Relevant Articles

- [Java Two Pointer Technique](https://www.baeldung.com/java-two-pointer-technique)
- [Implementing Simple State Machines with Java Enums](https://www.baeldung.com/java-enum-simple-state-machine)
- [Converting Between Roman and Arabic Numerals in Java](https://www.baeldung.com/java-convert-roman-arabic)
- [Checking If a List Is Sorted in Java](https://www.baeldung.com/java-check-if-list-sorted)
- [A Guide to the Folding Technique in Java](https://www.baeldung.com/folding-hashing-technique)
- [Creating a Triangle with for Loops in Java](https://www.baeldung.com/java-print-triangle)
- [The K-Means Clustering Algorithm in Java](https://www.baeldung.com/java-k-means-clustering-algorithm)
- More articles: [[<-- prev]](/algorithms-miscellaneous-2) [[next -->]](/algorithms-miscellaneous-4)
