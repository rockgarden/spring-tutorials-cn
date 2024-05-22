# Kubernetes简介

Kubernetes生态系统非常庞大，而且相当复杂，所以在尝试所有令人兴奋的工具时，很容易忘记成本问题。
为了避免在你的Kubernetes集群上超支，一定要看看自动化平台CAST AI的免费K8s成本监控工具。你可以实时查看你的成本，分配它们，计算项目的燃烧率，发现异常情况或峰值，并获得有洞察力的报告，你可以与你的团队分享。
连接你的集群并立即开始监控你的K8s成本：
> [免费的Kubernetes成本监控](https://www.baeldung.com/cast-ai-npi-ea-cVbnm)

1. 概述

    在本教程中，我们将对Kubernetes进行简单的理论介绍。特别是，我们将讨论以下主题：

    - 对容器编排工具的需求
    - Kubernetes的特点
    - Kubernetes架构
    - Kubernetes的API
    为了更深入的了解，我们也可以看看[官方文档](https://kubernetes.io/docs/home/)。
2. 容器编排

    在之前的[文章](https://www.baeldung.com/dockerizing-spring-boot-application)中，我们已经讨论了一些Docker的基础知识，以及如何打包和部署自定义应用程序。
    简而言之，Docker是一个容器运行时间：它提供的功能是以标准化的方式打包、运输和运行应用程序的单个实例，也被称为容器。
    然而，随着复杂性的增加，新的需求出现了；自动化部署、容器的协调、调度应用程序、授予高可用性、管理几个应用程序实例的集群等等。
    市场上有相当多的工具可用。然而，Kubernetes正越来越像一个实质性的竞争者。
3. Kubernetes的特点

    简而言之，Kubernetes是一个系统，用于协调跨节点集群的容器化应用程序，包括网络和存储基础设施。其中最重要的一些功能是：

    - 资源调度：它确保Pod在所有可用的节点上得到最佳的分布。
    - 自动扩展：随着负载的增加，集群可以动态地分配额外的节点，并在其上部署新的Pod。
    - 自愈：集群监督容器，并在必要时根据定义的策略重新启动它们。
    - 服务发现： Pod和服务通过DNS注册和发布
    - 滚动更新/回滚：支持基于Pod和容器的顺序重新部署的滚动更新。
    - 秘密/配置管理：支持安全处理敏感数据，如密码或API密钥
    - 存储协调：支持多个第三方存储解决方案，可作为外部卷来持久保存数据。
4. 了解Kubernetes

    主站维护集群的理想状态。当我们与集群进行交互时，例如通过使用kubectl命令行接口，我们总是在与集群的主站进行通信。
    集群中的节点是运行我们应用程序的机器（虚拟机、物理服务器等）。主控器控制每个节点。
    一个节点(node)需要一个容器运行时间(container runtime)。Docker是Kubernetes最常用的运行时间。

    Minikube是一个Kubernetes发行版，它使我们能够在工作站的虚拟机内运行一个单节点集群，用于开发和测试。
    Kubernetes API通过将Kubernetes概念包装成对象，提供了Kubernetes概念的抽象。
    kubectl是一个命令行工具，我们可以用它来创建、更新、删除和检查这些API对象。
5. Kubernetes API对象

    一个API对象是一个 "record of intent" - 一旦我们创建了这个对象，集群系统就会持续工作以确保这个对象的存在。
    每个对象由两部分组成：对象规格和对象状态。规格描述了该对象的期望状态。状态描述了对象的实际状态，由集群提供和更新。

    在下一节中，我们将讨论最重要的对象。之后，我们将看一个例子，看看规格和状态在现实中是怎样的。
    1. 基本对象
        Pod是Kubernetes处理的一个基本单元。它封装了一个或多个密切相关的容器、存储资源、唯一的网络IP，以及关于容器如何运行的配置，从而代表了一个应用程序的单一实例。
        服务Service是一个抽象的概念，它将Pod的逻辑集合在一起，并定义了如何访问它们。服务是一组容器的接口，这样消费者就不必担心单一访问位置以外的东西。
        使用Volumes，容器可以访问外部存储资源（因为它们的文件系统是短暂的），它们可以读取文件或永久地存储它们。卷也支持容器之间的文件共享。支持一长串[卷的类型](https://kubernetes.io/docs/concepts/storage/volumes/#types-of-volumes)。
        通过命名空间Namespaces，Kubernetes提供了在一个物理集群上运行多个虚拟集群的可能性。命名空间提供了资源名称的范围，这些名称在一个命名空间内必须是唯一的。
    2. 控制者
        此外，还有一些更高层次的抽象，称为控制器controllers。控制器建立在基本对象的基础上，提供额外的功能：

        - 部署Deployment控制器为Pod和ReplicaSets提供声明性更新。我们在部署对象中描述所需的状态，而部署控制器将实际状态改为所需的状态。
        - ReplicaSet确保在任何时候都有指定数量的Pod副本在运行。
        - 通过StatefulSet，我们可以运行有状态的应用程序：与Deployment不同的是，Pod将有一个独特而持久的身份。使用StatefulSet，我们可以实现具有唯一网络标识符的应用程序，或持久性存储，并可以保证有序、优雅的部署、扩展、删除和终止，以及有序和自动滚动更新。
        - 通过DaemonSet，我们可以确保集群中的所有或一组特定的节点运行一个特定Pod的副本。如果我们需要在每个节点上运行一个守护程序，例如用于应用程序监控，或用于收集日志，这可能会很有用。
        - GarbageCollection确保某些对象被删除，这些对象曾经有一个所有者，但现在不再有了。这有助于通过删除不再需要的对象来节省资源。
        - 一个Job创建一个或多个Pod，确保其中特定数量的Pod成功终止，并跟踪成功完成的情况。工作有助于平行处理一组独立但相关的工作项目，如发送电子邮件、渲染框架、转码文件等。
    3. 对象元数据
        元数据Metadata是属性，它提供了关于对象的额外信息。
        强制性的属性有：

        - 每个对象必须有一个命名空间（我们之前已经讨论过）。如果没有明确指定，一个对象属于默认的Namespace。
        - Name是一个对象在其Namespace中的唯一标识符。
        - Uid是一个在时间和空间上唯一的值。它有助于区分已经被删除和重新创建的对象。

        还有一些可选的元数据属性。其中最重要的一些是：

        - 标签(Labels)是一个键/值对，它可以被附加到对象上，以对它们进行分类。它帮助我们识别对象的集合，这些对象满足特定的条件。它们帮助我们以松散耦合的方式在对象上映射我们的组织结构。
        - 标签选择器(Label selectors)帮助我们通过它们的标签来识别一组对象。
        - 注释(Annotations)也是键/值对。与标签不同的是，它们不是用来识别对象的。相反，它们可以持有关于各自对象的信息，如构建、发布或图像信息。
    4. 例子
        在讨论了Kubernetes API的理论之后，我们现在来看看一个例子。

        API对象可以被指定为JSON或YAML文件。然而，文档中推荐YAML用于手动配置。
        在下文中，我们将为无状态应用程序的部署定义规范部分。之后，我们将看看从集群返回的状态会是什么样子。
        一个名为demo-backend的应用程序的规范可以是这样的：

        ```yml
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: demo-backend
        spec:
        selector:
            matchLabels:
                app: demo-backend
                tier: backend
        replicas: 3
        template:
            metadata:
            labels:
                app: demo-backend
                tier: backend
            spec:
            containers:
                - name: demo-backend
                image: demo-backend:latest
                ports:
                    - containerPort: 8080
        ```

        我们可以看到，我们指定了一个名为demo-backend的部署对象。下面的spec:部分实际上是一个嵌套结构，包含了前几节中讨论过的以下API对象：

        - replicas： 3指定一个复制因子为3的ReplicationSet（即我们将有三个Deployment的实例）
        - template模板：指定一个Pod
        - 在这个Pod中，我们可以使用spec: containers: 来为我们的Pod指定一个或多个容器。在这种情况下，我们有一个叫demo-backend的容器，它是从一个也叫demo-backend的镜像中实例化出来的，版本是最新的，它监听端口为8080。
        - 我们还为我们的Pod附加了标签：app: demo-backend and tier: backend
        - 通过selector选择器：matchLabels:，我们将我们的Pod链接到部署控制器（映射到标签app: demo-backend 和 tier: backend）
        如果我们从集群中查询我们的部署的状态，响应将看起来像这样：

        ```log
        Name:                   demo-backend
        Namespace:              default
        CreationTimestamp:      Thu, 22 Mar 2018 18:58:32 +0100
        Labels:                 app=demo-backend
        Annotations:            deployment.kubernetes.io/revision=1
        Selector:               app=demo-backend
        Replicas:               3 desired | 3 updated | 3 total | 3 available | 0 unavailable
        StrategyType:           RollingUpdate
        MinReadySeconds:        0
        RollingUpdateStrategy:  25% max unavailable, 25% max surge
        Pod Template:
        Labels:  app=demo-backend
        Containers:
        demo-backend:
            Image:        demo-backend:latest
            Port:         8080/TCP
            Environment:  <none>
            Mounts:       <none>
        Volumes:        <none>
        Conditions:
        Type           Status  Reason
        ----           ------  ------
        Progressing    True    NewReplicaSetAvailable
        Available      True    MinimumReplicasAvailable
        OldReplicaSets:  <none>
        NewReplicaSet:   demo-backend-54d955ccf (3/3 replicas created)
        Events:          <none>
        ```

        正如我们所看到的，部署似乎已经启动并运行，我们可以从我们的规范中识别出大部分元素。
        我们有一个复制系数为3的部署，其中一个pod包含一个容器，从image demo-backend:latest实例化。
        响应中出现的所有属性，但在我们的规范中没有定义，都是默认值。

6. 开始使用Kubernetes

    我们可以在不同的平台上运行Kubernetes：从我们的笔记本电脑到云提供商的虚拟机，或者裸机服务器的机架。
    要开始使用，[Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/)可能是最简单的选择：它使我们能够在本地工作站上运行一个单节点集群进行开发和测试。
    请看看[官方文档](https://kubernetes.io/docs/setup/learning-environment/minikube/)，了解更多的本地机器解决方案、托管解决方案、在IaaS云上运行的发行版，以及其他一些。
7. 结语

在这篇文章中，我们快速浏览了一些Kubernetes的基础知识。
简单地说，我们涵盖了以下几个方面：

- 为什么我们可能需要一个容器编排工具
- Kubernetes的一些最重要的功能
- Kubernetes架构及其最重要的组成部分
- Kubernetes API以及我们如何使用它来指定我们集群的理想状态

## Relevant Articles

- [ ] [Introduction to Kubernetes](https://www.baeldung.com/ops/kubernetes)
