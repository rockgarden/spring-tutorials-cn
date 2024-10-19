# [Java数组列表与矢量](https://www.baeldung.com/java-arraylist-vs-vector)

1. 一览表

    在本教程中，我们将重点介绍ArrayList和Vector类之间的差异。它们都属于Java集合框架，并实现了java.util.List接口。

    然而，这些类在实现方面存在显著差异。

2. 有什么不同？

    作为快速的开始，让我们来介绍ArrayList和Vector的关键区别。然后，我们将更详细地讨论一些要点：

    - 同步——两者之间的第一个主要区别。Vector是同步的，而ArrayList不是。
    - 规模增长——两者之间的另一个区别是它们在达到容量的同时调整尺寸的方式。矢量的大小增加了一倍。相比之下，ArrayList只增加了其长度的一半
    - 迭代——Vector可以使用迭代器和枚举来遍历元素。另一方面，ArrayList只能使用迭代器。
    - 性能——主要由于同步，与ArrayList相比，矢量操作速度较慢
    - 框架——此外，ArrayList是Collections框架的一部分，并在JDK 1.2中引入。与此同时，Vector作为遗留类出现在早期版本的Java中。

3. Vector

    我们将介绍一些关于Vector的核心细节。

    简单地说，矢量是一个可调整大小的数组。当我们添加或删除元素时，它可以生长和缩小。

    我们可以以典型的方式创建一个矢量：

    `Vector<String> vector = new Vector<>();`

    默认构造函数创建一个初始容量为10的空向量。

    让我们添加一些值：

    ```java
    vector.add("baeldung");
    vector.add("Vector");
    vector.add("example");
    ```

    最后，让我们使用迭代器接口迭代值：

    ```java
    Iterator<String> iterator = vector.iterator();
    while (iterator.hasNext()) {
        String element = iterator.next();
        // ...
    }
    ```

    或者，我们可以使用枚举遍历矢量：

    ```java
    Enumeration e = vector.elements();
    while(e.hasMoreElements()) {
        String element = e.nextElement();
        // ... 
    }
    ```

    现在，让我们更深入地探索他们的一些独特特征。

4. 并发

    我们已经提到过，ArrayList和Vector在并发策略上是不同的，但让我们仔细看看。如果我们深入研究Vector的方法签名，我们会看到每个签名都有同步的关键字：

    `public synchronized E get(int index)`

    简单地说，这意味着一次只有一个线程可以访问给定的向量。

    然而，无论如何，这种操作级同步都需要与我们自己的复合操作同步叠加。

    因此，相比之下，ArrayList采取了不同的方法。它的方法不同步，这种关注被分离为专门用于并发的类。

    例如，我们可以使用[CopyOnWriteArrayList](https://www.baeldung.com/java-copy-on-write-arraylist)或[Collections.synchronizedList](https://www.baeldung.com/java-synchronized-collections)来获得与Vector类似的效果：

    ```java
    vector.get(1); // synchronized
    Collections.synchronizedList(arrayList).get(1); // also synchronized
    ```

5. 性能

    正如我们上面已经讨论过的，Vector是同步的，这直接影响了性能。

    为了了解Vector和ArrayList操作之间的性能差异，让我们编写一个简单的JMH基准测试。

    过去，我们研究了ArrayList操作的时间复杂性，所以让我们为Vector添加测试用例。

    首先，让我们测试一下get（）方法：

    ```java
    @Benchmark
    public Employee testGet(ArrayListBenchmark.MyState state) {
        return state.employeeList.get(state.employeeIndex);
    }

    @Benchmark
    public Employee testVectorGet(ArrayListBenchmark.MyState state) {
        return state.employeeVector.get(state.employeeIndex);
    }
    ```

    我们将配置JMH使用三个线程和10次预热迭代。

    而且，让我们报告一下纳秒级别的每次操作的平均时间：

    ```log
    Benchmark                         Mode  Cnt   Score   Error  Units
    ArrayListBenchmark.testGet        avgt   20   9.786 ± 1.358  ns/op
    ArrayListBenchmark.testVectorGet  avgt   20  37.074 ± 3.469  ns/op
    ```

    我们可以看到，ArrayList#get的工作速度大约是Vector#get的三倍。

    现在，让我们比较一下contain（）操作的结果：

    ```java
    @Benchmark
    public boolean testContains(ArrayListBenchmark.MyState state) {
        return state.employeeList.contains(state.employee);
    }

    @Benchmark
    public boolean testContainsVector(ArrayListBenchmark.MyState state) {
        return state.employeeVector.contains(state.employee);
    }
    ```

    并打印结果：

    ```log
    Benchmark                              Mode  Cnt  Score   Error  Units
    ArrayListBenchmark.testContains        avgt   20  8.665 ± 1.159  ns/op
    ArrayListBenchmark.testContainsVector  avgt   20  36.513 ± 1.266  ns/op
    ```

    正如我们所看到的，对于contains（）操作，Vector的性能时间比ArrayList长得多。

6. 摘要

    在本文中，我们查看了Java中Vector和ArrayList类之间的差异。此外，我们还更详细地介绍了矢量功能。
