# 开始使用K3s

1. 概述

    我们知道Kubernetes（或K8s）是一个用于管理容器化工作负载和服务的便携式开源平台。它有助于在一个安全的集群中协调和管理吊舱中的应用。它还有效地改善了重新部署的零停机时间或容器的自我修复等任务。它的熟练程度与云端自动化有关，它在本地开发或低资源环境中的使用仍然不太适用。

    如果我们想在嵌入式系统中使用K8s，或者快速建立一个带有节点的本地Kubernetes集群，我们可能想看看[K3s](https://docs.k3s.io/)。

    在本教程中，我们将讨论K3s的主要功能，并做一个简单的集群例子。

2. K3s ： 一个轻量级的K8s

    K3s是CNCF认证的Kubernetes发行版和[沙盒项目](https://www.cncf.io/sandbox-projects/)，为低资源环境设计。[Rancher实验室](https://ranchermanager.docs.rancher.com/)负责维护K3s。

    总的来说，K3s提供了一个开销较少的Kubernetes集群设置，但仍然整合了K8s的大部分架构和功能。

    以下是K3s成为一个轻量级发行版的原因：

    - 打包成单一的二进制文件，对外部的依赖性最小化
    - [低硬件要求](https://docs.k3s.io/installation/requirements#hardware)和内存占用
    - 能够作为一个单一的以及[高可用性](https://docs.k3s.io/architecture#high-availability-k3s-server-with-an-external-db)的服务器运行

    K3s将标准的Kubernetes[组件](https://kubernetes.io/docs/concepts/overview/components/)打包成一个小于100MB的二进制文件。这是通过删除额外的驱动程序、可选的体积插件和第三方云集成来实现的。

    它包含更少的不是安装和运行Kubernetes所必需的部分。然而，我们仍然可以使用附加组件与AWS或GCP等云提供商集成。

    K3s应该能够在一个至少有512M内存（当然建议1GB）和一个CPU的Linux操作系统中运行。

    尽管K3s是Kubernetes的轻量级版本，但它并没有改变Kubernetes的核心工作方式。K3s架构由一个主服务器和在集群中运行的代理（或工作节点）组成。它仍然有CoreDNS和Ingress Controller作为核心网络的一部分。它有一个内置的SQLite数据库来存储所有的服务器信息。

    尽管如此，如果我们正在寻找一个高可用性的服务器，我们可以插入一个外部数据库，如[ETCD](https://etcd.io/)、[MySQL](https://www.mysql.com/)或[Postgres](https://www.postgresql.org/)。

    [Flannel](https://github.com/flannel-io/flannel#flannel)作为默认的[CNI](https://github.com/containernetworking/cni#cni---the-container-network-interface)插件用于集群网络。

    最后，作为一个完全认证的K8s版本，我们可以写我们的YAML来对K3s集群进行操作，就像我们对K8s一样。这适用于，例如，当我们管理一个[工作负载](https://kubernetes.io/docs/concepts/workloads/)或用服务和负载平衡定义pod的[网络](https://kubernetes.io/docs/concepts/services-networking/)。我们将使用[kubectl](https://kubernetes.io/docs/reference/kubectl/)来与集群进行交互。

3. 设置

    让我们看看如何安装K3s，访问集群并向主站添加节点。

    1. 安装

        一个基本的[安装](https://docs.k3s.io/installation)命令很简单：

        `$ curl -sfL https://get.k3s.io | sh -`

        这将执行一个来自 <https://get.k3s.io./> 的脚本，并在我们的Linux主机中作为服务运行K3s。

        作为一种选择，我们可以下载一个[版本](https://github.com/k3s-io/k3s/releases)并安装它。无论哪种方式，都有一些[服务器配置](https://docs.k3s.io/reference/server-config)的选项，我们可以与[环境变量](https://docs.k3s.io/reference/env-variables)混合使用。

        例如，我们可能想禁用Flannel并使用一个不同的CNI提供者。

        我们可以通过运行脚本来做到这一点：

        `$ curl -sfL https://get.k3s.io | sh -s - --flannel-backend none`

        如果我们已经安装了K3s二进制文件，我们可以在命令行中加上环境变量的前缀：

        `$ INSTALL_K3S_EXEC="--flannel-backend none" k3s server`

    2. 集群访问

        默认情况下，K3s将在/etc/rancher/k3s目录下安装一个配置文件。安装后，与K8s类似，我们需要定义一个配置文件的位置。

        我们可以通过导出一个环境变量使K3s指向该配置文件：

        `$ export KUBECONFIG=/etc/rancher/k3s/k3s.yaml`

        作为一种选择，我们可以在我们的主目录中定义配置文件，K8s默认会指向这里：

        ```bash
        mkdir -p ~/.kube
        sudo k3s kubectl config view --raw | tee ~/.kube/config
        chmod 600 ~/.kube/config
        ```

        我们可以检查我们的集群是否在运行：

        `$ kubectl get nodes`

        值得注意的是，我们可以看到，控制平面将与主节点一起运行。

        现在让我们看看哪些容器（pods）被创建了：

        `$ kubectl get pods --all-namespaces`

        我们可以看到集群上的可用pod的列表。

        我们可以看到一个基本的K3s设置，包括：

        - [Traefik](https://traefik.io/)作为一个入口控制器，用于HTTP反向代理和负载平衡。
        - [CoreDns](https://coredns.io/)管理集群和节点内的DNS解析。
        - [Local Path Provisioner](https://github.com/rancher/local-path-provisioner#local-path-provisioner)提供了一种利用每个节点的本地存储的方法。
        - [Helm](https://helm.sh/)，我们可以用它来[定制打包的组件](https://docs.k3s.io/helm#customizing-packaged-components-with-helmchartconfig)。
        K3s不是在不同的进程中运行组件，而是在一个单一的服务器或代理进程中运行所有组件。由于它被打包在一个文件中，我们也可以使用[Air-gap](https://docs.k3s.io/installation/airgap)安装，进行离线工作。有趣的是，我们也可以使用[K3d](https://k3d.io/)在Docker中运行K3s。

    3. 添加节点

        如果我们想向我们的集群添加节点，我们只需要执行指向节点主机的相同命令：

        `$ curl -sfL https://get.k3s.io | K3S_URL=https://<node-host>:6443 K3S_TOKEN=mynodetoken sh -`

        K3S_TOKEN被存储在本地：

        `$ cat /var/lib/rancher/k3s/server/node-token`

        一旦工人节点加入主站，控制平面就会识别并为节点安排工作。

4. 集群实例

    让我们做一个简单的集群例子，我们将在其中安装一个Nginx镜像。

    让我们从创建前面提到的集群开始：

    `$ curl -sfL https://get.k3s.io | sh -`

    首先，让我们从Nginx镜像创建一个部署，在80端口有三个副本：

    `$ kubectl create deployment nginx --image=nginx --port=80 --replicas=3`

    接下来，让我们检查一下我们的pods：

    `$ kubectl get pods`

    我们应该看到三个正在运行的容器。

    Pod不是永久性的资源，会不断地被创建和销毁。因此，我们需要一个服务来动态地将pod的IP映射到外部世界。

    服务可以是不同[类型](https://kubernetes.io/docs/concepts/services-networking/service/#publishing-services-service-types)的。我们将选择一个ClusterIp：

    `$ kubectl create service clusterip nginx --tcp=80:80`

    让我们看一下我们的服务定义：

    `$ kubectl describe service nginx`

    我们可以看到对应于pods（或容器）地址的Endpoints，在那里我们可以接触到我们的应用程序。

    服务没有直接访问。一个入口控制器通常在它们前面，用于缓存、负载平衡和安全原因，如过滤掉恶意请求。

    最后，让我们在YAML文件中定义一个Traefik控制器。这将把传入请求的流量路由到服务中：

    ```yml
    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
    name: nginx
    annotations:
        ingress.kubernetes.io/ssl-redirect: "false"
    spec:
    rules:
    - http:
        paths:
        - path: /
            pathType: Prefix
            backend:
            service:
                name: nginx
                port:
                number: 80
    ```

    我们可以通过向集群应用这个资源来创建ingress：

    `$ kubectl apply -f <nginx-ingress-file>.yaml`

    让我们描述一下我们的ingress控制器：

    `$ kubectl describe ingress nginx`

    我们的后端将在可用服务的端口上运行Nginx应用程序。

    现在我们可以从主机或浏览器向192.168.1.103地址发出GET请求，访问Nginx主页。

    我们可能想在入口控制器上添加一个负载平衡器。K3s默认使用[ServiceLB](https://github.com/k3s-io/klipper-lb)。

5. K8s和K3s有什么不同

    K3s和K8s之间最显著的区别是包装。K3s是一个不到100MB的单一包装的二进制文件。K8s有多个组件作为进程运行。

    此外，作为一个较轻的版本，K3s可以在几秒钟内启动一个Kubernetes集群。我们可以用更低的资源更快地运行操作。

    K3s支持AMD64、ARM64和ARMv7架构，等等。这意味着我们可以在任何地方运行它，例如在Raspberry PI Zero中。K3s还可以处理连接有限的环境。

    我们可能还想用K3s来测试CI管道，检查我们的集群是否顺利运行。

    在学习K3s时，我们的起步较快，需要掌握的命令较少。例如，如果我们还没有使用分布式集群的背景，那么开始使用它的努力要比K8s少。

    然而，对于复杂的集群或重型工作负载，我们仍然应该考虑K8s。诚然，K3s提供了一个高可用性的选择，但它需要更多的工作来插入，例如，不同的数据库或整合云供应商。

    如果要在K3s和K8s之间做出决定，可能会归结为可用资源。然而，K3s是持续集成测试或为项目启动建立Kubernetes集群的一个选择。

6. 结语

    在这篇文章中，我们已经看到K3s是一个轻量级的发行版，是K8s的有效替代品。它需要的资源少，设置快。我们在创建一个简单集群的例子时已经看到了这一点。

    尽管如此，它仍然与K8s完全兼容，也是高可用性服务器的一个潜在用途。最后，我们讨论了K8s和K3s的区别，以及什么时候我们应该优先使用K3s。

## 相关文章

- [ ] [Getting Started With K3s](https://www.baeldung.com/ops/k3s-getting-started)
