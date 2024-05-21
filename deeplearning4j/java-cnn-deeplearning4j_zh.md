# [如何使用 Deeplearning4j 实现 CNN](https://www.baeldung.com/java-cnn-deeplearning4j)

1. 概述

    在本教程中，我们将使用 Java 中的 Deeplearning4j 库构建并训练一个卷积神经网络模型。

    有关如何设置库的详细信息，请参阅我们的 Deeplearning4j 指南。

2. 图像分类

    1. 问题陈述

        假设我们有一组图像。每幅图像代表一个特定类别的物体。此外，图像上的物体属于唯一已知的类别。因此，问题的关键在于建立一个能够识别给定图像上物体类别的模型。

        例如，假设我们有一组包含十个手势的图像。我们建立一个模型，并训练它对这些手势进行分类。训练完成后，我们可以通过其他图像，对其中的手势进行分类。当然，给定的手势应属于已知类别。

    2. 图像表示

        在计算机内存中，图像可以用数字矩阵来表示。每个数字是一个像素值，范围从 0 到 255。

        灰度图像是一个二维矩阵。同样，RGB 图像是一个具有宽度、高度和深度维度的三维矩阵。

        我们可以看到，图像就是一组数字。因此，我们可以建立多层网络模型，训练它们对图像进行分类。

3. 卷积神经网络

    卷积神经网络（CNN）是一种具有特定结构的多层网络模型。CNN 的结构可分为两个部分：卷积层和全连接（或密集）层。让我们逐一了解它们。

    1. 卷积层

        每个卷积层都是一组方矩阵，称为核。首先，我们需要它们对输入图像进行卷积。它们的数量和大小可能会有所不同，这取决于给定的数据集。我们通常使用 3×3 或 5×5 的内核，很少使用 7×7 的内核。具体的大小和数量是通过试验和误差来选择的。

        此外，我们会在训练开始时随机选择内核矩阵的变量。它们就是网络的权重。

        为了进行卷积，我们可以使用内核作为滑动窗口。我们将内核权重与相应的图像像素相乘，计算出总和。然后，我们可以使用跨距（向右移动）和填充（向下移动）来移动内核，以覆盖图像的下一块。这样，我们就能得到用于进一步计算的值。

        简而言之，通过这一层，我们得到了一幅卷积图像。有些变量可能小于零。这通常意味着这些变量不如其他变量重要。因此，应用 [ReLU](https://en.wikipedia.org/wiki/Rectifier_(neural_networks)) 函数是减少进一步计算的好方法。

    2. 子采样层

        子采样（或池化）层是网络中的一层，通常在卷积层之后使用。卷积之后，我们会得到大量的计算变量。然而，我们的任务是从中选出最有价值的变量。

        我们的方法是对卷积后的图像采用[滑动窗口算法](https://www.baeldung.com/cs/sliding-window-algorithm)。每一步，我们都会选择预定义大小（通常在 2×2 和 5×5 像素之间）的正方形窗口中的最大值。这样，我们计算的参数就会减少。因此，这将减少计算量。

    3. 密集层

        密集层（或全连接层）由多个神经元组成。我们需要这一层来进行分类。此外，可能会有两个或更多这样的后续层。重要的是，最后一层的大小应与分类的类别数相等。

        网络的输出是图像属于每个类别的概率。为了预测概率，我们将使用 [Softmax](https://en.wikipedia.org/wiki/Softmax_function) 激活函数。

    4. 优化技术

        为了进行训练，我们需要优化权重。请记住，我们最初是随机选择这些变量的。神经网络是一个大函数。而且，它有很多未知参数，也就是我们的权重。

        当我们将图像传递给网络时，它会给出答案。然后，我们可以根据这个答案建立一个损失函数。就监督学习而言，我们也有一个实际的答案--真实类别。我们的任务就是最小化这个损失函数。如果我们成功了，那么我们的模型就训练有素了。

        为了最小化这个函数，我们必须更新网络的权重。为此，我们可以计算损失函数相对于每个未知参数的导数。然后，我们就可以更新每个权重。

        我们可以增加或减少权重值，以找到损失函数的局部最小值，因为我们知道斜率。此外，这个过程是迭代的，称为[梯度下降](https://www.baeldung.com/java-gradient-descent)。反向传播利用梯度下降将权重更新从网络的末端传播到网络的起点。

        在本教程中，我们将使用随机梯度下降（[SGD](https://en.wikipedia.org/wiki/Stochastic_gradient_descent)）优化算法。其主要思路是在每一步随机选择一批训练图像。然后应用反向传播。

    5. 评估指标

        最后，在训练网络之后，我们需要了解模型的性能如何。

        最常用的指标是准确率。这是正确分类的图像与所有图像的比率。同时，召回率、精确率和 F1 分数也是非常重要的[图像分类指标](https://medium.com/analytics-vidhya/confusion-matrix-accuracy-precision-recall-f1-score-ade299cf63cd)。

4. 数据集准备

    在本节中，我们将准备图像。让我们在本教程中使用嵌入式 [CIFAR10](https://en.wikipedia.org/wiki/CIFAR-10) 数据集。我们将创建迭代器来访问图像：

    ```java
    public class CifarDatasetService implements IDataSetService {
        private CifarDataSetIterator trainIterator;
        private CifarDataSetIterator testIterator;
        public CifarDatasetService() {
            trainIterator = new CifarDataSetIterator(trainBatch, trainImagesNum, true);
            testIterator = new CifarDataSetIterator(testBatch, testImagesNum, false);
        }
        // other methods and fields declaration
    }
    ```

    我们可以自行选择一些参数。TrainBatch 和 testBatch 分别是每个训练和评估步骤的图像数量。TrainImagesNum 和 testImagesNum 是训练和测试的图像数量。一个历元持续 trainImagesNum / trainBatch 步骤。因此，如果训练图像为 2048 张，批量大小 = 32，则一个 epoch 的步骤数为 2048 / 32 = 64。

5. Deeplearning4j 中的卷积神经网络

    1. 构建模型

        接下来，让我们从头开始构建我们的 CNN 模型。为此，我们将使用卷积层、子采样（池化）层和全连接（密集）层。

        ```java
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
        .seed(1611)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .learningRate(properties.getLearningRate())
        .regularization(true)
        .updater(properties.getOptimizer())
        .list()
        .layer(0, conv5x5())
        .layer(1, pooling2x2Stride2())
        .layer(2, conv3x3Stride1Padding2())
        .layer(3, pooling2x2Stride1())
        .layer(4, conv3x3Stride1Padding1())
        .layer(5, pooling2x2Stride1())
        .layer(6, dense())
        .pretrain(false)
        .backprop(true)
        .setInputType(dataSetService.inputType())
        .build();
        network = new MultiLayerNetwork(configuration);
        ```

        在这里，我们指定了学习率、更新算法、模型的输入类型和分层架构。我们可以对这些配置进行实验。因此，我们可以使用不同的架构和训练参数训练多个模型。此外，我们还可以比较结果，选择最佳模型。

    2. 训练模型

        然后，我们将训练建立的模型。这只需几行代码即可完成：

        ```java
        public void train() {
            network.init();    
            IntStream.range(1, epochsNum + 1).forEach(epoch -> {
                network.fit(dataSetService.trainIterator());
            });
        }
        ```

        epochs 的数量是我们可以自己指定的参数。我们的数据集很小。因此，几百个 epoch 就足够了。

    3. 评估模型

        最后，我们可以对训练好的模型进行评估。Deeplearning4j 库提供了轻松完成评估的功能：

        ```java
        public Evaluation evaluate() {
        return network.evaluate(dataSetService.testIterator());
        }
        ```

        Evaluation 是一个对象，其中包含模型训练后的计算指标。这些指标包括准确率、精确度、召回率和 F1 分数。此外，它还有一个友好的可打印界面：

        ```log
        ==========================Scores=====================
        # of classes: 11
        Accuracy: 0,8406
        Precision: 0,7303
        Recall: 0,6820
        F1 Score: 0,6466
        =====================================================
        ```

6. 结论

    在本教程中，我们了解了 CNN 模型的架构、优化技术和评估指标。此外，我们还使用 Java 中的 Deeplearning4j 库实现了模型。
