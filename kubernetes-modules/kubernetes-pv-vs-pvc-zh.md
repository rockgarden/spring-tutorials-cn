# Kubernetes中PV和PVC的区别

1. 概述

    Kubernetes是一个流行的开源平台，用于容器编排。然而，在Kubernetes上运行[有状态的应用程序](https://www.baeldung.com/ops/kubernetes-deployment-vs-statefulsets)时，关键的挑战之一是管理存储。此外，Kubernetes为管理存储提供了两个主要对象： PersistentVolume（PV）和PersistentVolumeClaim（PVC）。

    在本教程中，我们将讨论PV和PVC的区别，并提供例子来说明它们的用法。

2. 什么是PersistentVolume（PV）？

    PersistentVolume（PV）是一种Kubernetes资源，代表集群中的一块存储。它是一种集群范围内的资源，可以被Kubernetes集群中的多个pod使用。它可以通过多种方式创建，包括静态配置卷，动态配置卷，或导入现有卷。

    PV是一种将存储与pod解耦的方式，允许pod独立于pod的生命周期访问存储。PV被设计成可以被多个pod使用，这意味着它们可以有一个独立于任何使用它们的pod的生命周期。

    下面是一个PV YAML定义的例子：

    ```yml
    apiVersion: v1
    kind: PersistentVolume
    metadata:
    name: pv-example
    spec:
    capacity:
        storage: 10Gi
    accessModes:
        - ReadWriteOnce
    persistentVolumeReclaimPolicy: Retain
    storageClassName: slow
    hostPath:
        path: /data
    ```

    在这个例子中，我们创建了一个容量为10GB的PV，并将其访问模式设置为ReadWriteOnce，使单个节点能够将该卷挂载为读写。

    我们可以看到，PV的persistentVolumeReclaimPolicy是Retain，这意味着当PVC被删除时，PV不会被自动删除。另外，我们将存储类名称设置为慢速，使PVC能够使用相同的存储类名称从该PV请求存储。

3. 什么是PersistentVolumeClaim（PVC）？

    PersistentVolumeClaim（PVC）是一种Kubernetes资源，代表一个pod对存储的请求。它可以指定对存储的要求，如大小、访问模式和存储类别。Kubernetes使用PVC来寻找一个满足PVC要求的可用PV。

    PVC是由一个pod创建的，用来请求PV的存储。一旦PVC被创建，它就可以作为一个卷被挂载到一个pod中。然后，pod可以使用挂载的卷来存储和检索数据。

    让我们看看PVC YAML定义的一个例子：

    ```yml
    apiVersion: v1
    kind: PersistentVolumeClaim
    metadata:
    name: pvc-example
    spec:
    accessModes:
        - ReadWriteOnce
    resources:
        requests:
        storage: 5Gi
    storageClassName: slow
    ```

    通过这个YAML文件，我们创建了一个请求5GB存储的PVC，访问模式为ReadWriteOnce。我们将storageClassName设置为slow，使Kubernetes能够将PV与PVC的存储类名称相匹配。

4. PV和PVC之间的区别

    PV和PVC的主要区别是，PV代表集群中的一块存储，而PVC代表一个pod对存储的请求。

    以下是PV和PVC之间的一些其他区别：

    | 属性   | PV                                            | PVC                    |
    |------|-----------------------------------------------|------------------------|
    | 范围   | 一个集群中的多个pod可以使用它                              | 代表一个pod对命名空间中存储的请求     |
    | 配置   | 可以静态、动态地创建它，或者导入它                             | 由一个pod创建，以便从PV请求存储     |
    | 生命周期 | 有一个独立于任何使用pod的生命周期                            | 与pod的生命周期相关联           |
    | 配置   | 可以配置访问模式、persistentVolumeReclaimPolicy和存储类别名称 | 可以指定存储要求，如大小、访问模式和存储类别 |
    | 访问   | 多个pod可以访问它                                    | 只有请求的pod可以访问它          |

5. PV和PVC实例

    让我们看一个如何在Kubernetes中使用PV和PVC的例子。我们将创建一个PVC，从PV中请求存储，然后使用PVC在pod中装载卷。

    首先，我们将通过静态配置一个具有以下规格的卷来生成PV：

    ```yml
    apiVersion: v1
    kind: PersistentVolume
    metadata:
    name: pv-storage
    spec:
    capacity:
        storage: 1Gi
    accessModes:
        - ReadWriteOnce
    hostPath:
        path: /data
    ```

    接下来，我们将创建从PV请求存储的PVC：

    ```yml
    apiVersion: v1
    kind: PersistentVolumeClaim
    metadata:
    name: pvc-storage
    spec:
    accessModes:
        - ReadWriteOnce
    resources:
        requests:
        storage: 1Gi
    ```

    最后，我们将创建一个使用PVC作为卷的pod：

    ```yml
    apiVersion: v1
    kind: Pod
    metadata:
    name: pod-storage
    spec:
    containers:
    - name: nginx
        image: nginx
        volumeMounts:
        - name: storage
        mountPath: /data
    volumes:
    - name: storage
        persistentVolumeClaim:
        claimName: pvc-storage
    ```

    我们首先创建了一个容量为1GB的PV，访问模式为ReadWriteOnce。之后，我们继续创建一个PVC，请求1GB的存储空间，访问模式相同。最后，为了将PVC作为一个卷使用，我们创建了一个pod，将其挂载到/data。

6. 结论

    在这篇文章中，我们看到了两个基本的Kubernetes资源，它们被用于管理存储。首先，PV代表集群中的一块存储，而PVC代表一个pod对存储的请求。

    此外，我们了解到，虽然这两种资源之间有相似之处，但它们在范围、供应、生命周期、配置和访问方面也有很大的区别。

    通过了解PV和PVC之间的区别，我们可以就如何有效管理Kubernetes集群中的存储做出更明智的决定。

## 相关文章

- [ ] [Difference Between PV and PVC in Kubernetes](https://www.baeldung.com/ops/kubernetes-pv-vs-pvc)
