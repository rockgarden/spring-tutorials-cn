# 轻量级的Kubernetes分布

1. 概述

    在本教程中，我们将了解Kubernetes的不同轻量级版本。

    使用这些轻量级版本可以解决多种用例。我们可以使用它们在本地机器上运行Kubernetes集群，测试我们的变化，运行CI管道，并在物联网和边缘设备中使用它们。

2. MiniKube

    [MiniKube](https://www.baeldung.com/docker-local-images-minikube)是最常用的本地Kubernetes集群。它使用一个虚拟机，我们可以根据自己的需要轻松地进行手动配置，如内存和存储。此外，它还提供了对系统的完全控制。

    启动时间有点慢，比其他发行版使用更多资源。它只能在单节点集群中使用。

3. 微K8S

    [MicroK8S](https://www.baeldung.com/ops/microk8s-introduction)是一个由Canonical开发的CNCF认证的开源发行版。它是一个完全兼容的Kubernetes发行版，具有较小的CPU和内存占用率。它为自定义配置提供了低访问量的系统。

    它为在各种操作系统上快速、轻松地安装单节点或多节点集群进行了优化。它可以用于本地开发，CI管道，或物联网设备。

4. Kind

    [Kind](https://www.baeldung.com/ops/kubernetes-kind)（Kubernetes in Docker）是一个经CNCF认证的开源Kubernetes安装器。它主要是Kubernetes的一个测试工具。

    我们也可以用它来在本地和CI管道中运行Kubernetes集群。它使用Docker容器作为一个节点。它有最快的启动时间。

5. K3S

    [K3S](https://www.baeldung.com/ops/k3s-getting-started)是一个经CNCF认证的开源轻量级容器运行时。它由Rancher实验室维护。它可以很容易地安装成一个多节点集群，配置非常少。

    大多数传统组件、可选的驱动程序和插件在K3S中是不可用的。我们可以将其用于本地开发、CI管道或物联网设备。它能自动配置一切；进行自定义配置很困难，需要良好的系统知识。

6. K3D

    [K3D](https://k3d.io/v5.4.9/)是一个围绕K3S的轻量级包装器，可以在Docker中运行它。它使K3S集群非常容易在Docker上安装和运行。我们可以使用K3D轻松启动单节点或多节点的K3S集群。

7. 比较

    现在让我们总结一下所有发行版的优点和缺点：

    | 分布版本    | 优点                        | 缺点                       |
    |----------|---------------------------|--------------------------|
    | Minikube | 容易进行手动配置                  | 只有单节点集群                  |
    |          | 给予系统更多的访问权                | 难以设置                     |
    |          | 使用最广泛和最古老的发行版             | 使用更多资源                   |
    | MicroK8S | 易于设置                      | 不能安装在ARM32 CPU的机器上       |
    |          | 使用的资源较少                   | 对系统的访问量不大                |
    |          | 良好的发布周期；与Kubernetes版本保持同步 | 定制配置变更非常困难               |
    |          | 可以被设置为多节点集群               |                          |
    | 样的       | 非常容易安装                    | 除了Docker，很难与其他发行版一起设置    |
    |          | 使用较少的资源                   | 手动修改配置很困难                |
    |          | 很容易使用dockercommands进行访问   |                          |
    |          | 容器被认为是节点                  |                          |
    | K3S      | 非常容易设置                    | 在设置过程中的自动配置中可以进行不需要的网络更改 |
    |          | 使用的资源较少                   | 手动更改配置很困难                |
    |          | 允许多节点集群设置                 |                          |

8. 总结

在这篇文章中，我们已经了解了Kubernetes的不同轻量级分布，以及何时可以使用它们。没有什么银弹可以解决我们所有的问题。我们需要分析我们的需求并做出明智的决定。

## 相关文章

- [ ] [Lightweight Kubernetes Distributions](https://www.baeldung.com/ops/kubernetes-lightweight-distributions)
