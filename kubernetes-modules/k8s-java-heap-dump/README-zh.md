# 如何从Kubernetes Pod中获取Java Heap Dump？

1. 概述

    Java因其优秀的[垃圾收集器算法](https://www.baeldung.com/jvm-garbage-collectors)而闻名。然而，这并不意味着在JVM应用程序中不会发生内存泄漏。获取和分析堆转储是在我们的应用程序中找到潜在泄漏的第一步。

    在这个简短的教程中，我们将看到如何从作为Kubernetes pod运行的应用中获取Java堆转储。

    首先，我们将研究什么是堆转储。然后，我们将创建一个小的测试应用程序，以后将作为一个pod部署到Kubernetes。最后，我们将看看如何从它那里获得堆转储。

2. 什么是堆转储

    堆转储是JVM应用内存中的所有对象在某一时刻的快照。

    通过查看堆并使用特殊工具对其进行分析，我们可以找到对象被创建的位置，并在源代码中找到对这些对象的引用。我们还可以看到显示整个时间段内存分配的图表。

    正因为如此，[堆转储](https://www.baeldung.com/java-heap-dump-capture)有助于检测内存泄漏问题和优化JVM应用程序的内存使用。

3. 创建一个测试程序

    在捕获堆转储之前，我们需要一个在Kubernetes pod中工作的JVM应用程序。

    1. 创建一个长期运行的应用程序

        让我们创建一个简单的应用程序，[搜索指定范围内的素数](https://www.baeldung.com/java-generate-prime-numbers)。我们将其称为素数搜索器（prime-number-finder）。

        我们的小程序包括一个main()方法和一个isPrime()方法，用来运行一个暴力素数检测器：

        kubernetes.heap.dump/PrimeNumberFinder.java

        接下来就是将我们的代码编译成一个jar文件。它将被称为prime-number-finder.jar，并将位于目标目录中。

    2. 为Kubernetes容器化应用程序

        为了能够将其部署到 Kubernetes 堆栈中，我们来创建一个 dockerfile，参见代码。

        > 注意：image 命名与 prime-deploy.yaml 中保持一致 （baeldung/prime-number:latest）

        我们要将构建的jar文件复制到容器中，并使用标准的java -jar命令运行它。

        接下来，我们需要一个prime-deploy.yaml Kubernetes部署文件，指定如何将我们的应用部署到Kubernetes堆栈中，参见代码。

        我们需要做的最后一件事是将其部署到 Kubernetes：

        $ kubectl apply -f prime-deploy.yaml

        我们可以使用 kubectl get pods 命令来检查部署是否成功：

        - [ ] **ERROR**: ErrImagePull

        ```log
        NAME                      READY   STATUS             RESTARTS   AGE   IP           NODE
        prime-number-finder-pod   1/1     Running             0         1m   172.17.0.3   minikube   
        ```

4. 获取堆转储

    我们需要做的第一件事是获得正在运行的pod的名字。我们可以从上一章的kubectl get pods命令中得到它。在我们的例子中，它是prime-number-finder-pod。

    下一步是使用这个名字进入我们正在运行的pod。我们将使用Kubernetes exec命令来做这件事：

    `$ kubectl exec -it prime-number-finder-pod bash`

    现在，我们需要获得我们正在运行的JVM应用程序的进程ID。我们可以使用JDK中内置的jps命令。

    下一步是创建堆转储。再一次，我们将使用JDK的内置工具：

    `$ jmap -dump:live,format=b,file=prime_number_heap_dump.bin <process_id>`

    我们需要做的最后一件事是将新创建的堆转储从 pod 复制到我们的本地机器：

    `$ kubectl cp prime-number-finder-pod:prime_number_heap_dump.bin <our local destination directory>`

    现在，我们可以使用任何[内存分析工具](https://www.baeldung.com/java-analyze-thread-dumps)，如JDK提供的JvisualVM，或第三方应用程序，如JProfiler或JStack Review，来分析堆转储。

5. 总结

    在这篇文章中，我们学习了如何从Kubernetes pod中获取Java堆转储。

    首先，我们了解了什么是堆转储以及它的用途。然后，我们创建了一个简单的素数查找程序，并将其部署到Kubernetes。

    最后，我们展示了如何登录正在运行的pod，创建一个堆转储并将其复制到本地机器上。

## Relevant Articles

- [ ] [How to Get Java Heap Dump From Kubernetes Pod?](https://www.baeldung.com/ops/java-heap-dump-from-kubernetes-pod)

## Code

像往常一样，本文的完整源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/kubernetes-modules/k8s-java-heap-dump)上找到。
