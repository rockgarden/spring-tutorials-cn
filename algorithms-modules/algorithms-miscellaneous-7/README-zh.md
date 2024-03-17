# 算法-杂项-6

- [Java 中的最小生成树 Boruvka 算法](https://www.baeldung.com/java-boruvka-algorithm)
- [x] [Java中的梯度下降算法](#java中的梯度下降)
- [Kruskal 的生成树算法及 Java 实现](https://www.baeldung.com/java-spanning-trees-kruskal)
- [Java 中的平衡括号算法](https://www.baeldung.com/java-balanced-brackets-algorithm)
- [高效合并排序的 Java 序列](https://www.baeldung.com/java-merge-sorted-sequences)
- [用 Java 介绍贪婪算法](https://www.baeldung.com/java-greedy-algorithms)
- [Java 中的凯撒密码](https://www.baeldung.com/java-caesar-cipher)
- [用 Java 实现 2048 解算器](https://www.baeldung.com/2048-java-solver)
- [查找 Java 数组中的前 K 个元素](https://www.baeldung.com/java-array-top-elements)
- [用 Java 反转链接列表](https://www.baeldung.com/java-reverse-linked-list)
- 更多文章： [[<-- prev]](/algorithms-miscellaneous-5)

## Java中的梯度下降

[算法](https://www.baeldung.com/category/algorithms) [数据](https://www.baeldung.com/category/data)

1. 简介

    在本教程中，我们将学习梯度下降算法。我们将用 Java 实现该算法，并逐步加以说明。

2. 什么是梯度下降算法？

    梯度下降算法是一种优化算法，用于找到给定函数的局部最小值。它广泛应用于高级机器学习算法中，用于最小化损失函数。

    Gradient 是斜率的另一个词，而 descent 则是下降的意思。顾名思义，"梯度下降"就是沿着函数的斜率下降，直到终点。

3. 梯度下降法的特性

    梯度下降法能找到局部最小值，它可能不同于全局最小值。局部起点作为一个参数提供给算法。

    这是一种迭代算法，每一步都试图沿着斜坡向下移动，以接近局部最小值。

    实际上，该算法是一种回溯算法。我们将在本教程中说明并实现回溯梯度下降算法。

4. 逐步说明

    梯度下降算法需要一个函数和一个起点作为输入。让我们定义并绘制一个函数：

    \[y=\left | x^{3}  \right | - 3 x^{2} + x\]

    我们可以从任何需要的点开始。让我们从 x=1 开始。

    在第一步中，梯度下降法以预先设定的步长沿斜坡下降：

    ![GD3](pic/GD3.jpg)

    接下来，它以相同的步长继续前进。不过，这一次它的终点是比上一步更大的 y：

    ![GD4](pic/GD4.jpg)

    这表明算法已经通过了局部最小值，因此它以更小的步长向后退：

    ![GD5](pic/GD5.jpg)

    随后，每当当前的 y 大于上一步的 y 时，步长就会减小并否定。如此迭代下去，直到达到所需的精度。

    我们可以看到，梯度下降法在这里找到了局部最小值，但并不是全局最小值。如果我们从 x=-1 而不是 x=1 开始，就能找到全局最小值。

5. 在 Java 中的实现

    梯度下降法有多种实现方法。在这里，我们不计算函数的导数来寻找斜率的方向，因此我们的实现也适用于不可微函数。

    让我们定义精度和 stepCoefficient，并赋予它们初始值：

    ```java
    double precision = 0.000001;
    double stepCoefficient = 0.1;
    ```

    在第一步中，我们没有前一个 y 供比较。我们可以增加或减少 x 的值，看看 y 是降低还是升高。stepCoefficient 为正表示我们正在增加 x 的值。

    现在让我们执行第一步

    ```java
    double previousX = initialX;
    double previousY = f.apply(previousX);
    currentX += stepCoefficient * previousY;
    ```

    在上述代码中，f 是一个 `Function<Double，Double>`，initialX 是一个 double，两者都作为输入提供。

    另一个需要考虑的关键点是梯度下降法并不能保证收敛。为了避免陷入循环，我们可以限制迭代次数：

    `int iter = 100;`

    稍后，我们将在每次迭代时将 iter 递减一个。因此，我们最多只能在迭代 100 次时退出循环。

    现在有了 previousX，我们就可以设置循环了：

    algorithms.gradientdescent/GradientDescent.java

    ```java
    while (previousStep > precision && iter > 0) {
        iter--;
        double currentY = f.apply(currentX);
        if (currentY > previousY) {
            stepCoefficient = -stepCoefficient/2;
        }
        previousX = currentX;
        currentX += stepCoefficient * previousY;
        previousY = currentY;
        previousStep = StrictMath.abs(currentX - previousX);
    }
    ```

    如果 currentY 大于 previousY，我们就改变方向并减小步长。

    这样循环下去，直到我们的步长小于所需的精度。最后，我们可以将 currentX 作为局部最小值返回：

    `return currentX;`

6. 结论

    在本文中，我们通过逐步说明的方式介绍了梯度下降算法。

## Code

我们还用 Java 实现了梯度下降算法。代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/algorithms-modules/algorithms-miscellaneous-6) 上获取。
