# MicroK8s简介

1. 简介

    虽然Kubernetes的使用量持续增长，但仍然存在的少数问题之一是它的资源限制。运行一个完全配置的Kubernetes集群通常需要大量的CPU和内存。这使得企业很难以类似运营的方式来构建和测试软件。

    这就是[Microk8s](https://microk8s.io/)出现的地方。在这篇文章中，我们将看看MicroK8s如何通过允许我们以较小的CPU和内存占用来运行全功能的Kubernetes集群来帮助缓解其中的一些问题。

2. 什么是MicroK8s

    MicroK8s是一个完全兼容的Kubernetes发行版，其CPU和内存占用率比其他大多数发行版要小。它从头开始设计，为计算能力和内存有限的设备提供完整的Kubernetes体验。

    MicroK8s拥有许多功能：

    - 尺寸： 它的内存和存储需求只是许多全尺寸Kubernetes集群所需的一小部分。事实上，它被设计为在单个节点/计算机上运行。
    - 简单性： 通过安装最低限度的功能集，MicroK8s使集群的管理变得简单。我们可以在几分钟内创建一个功能齐全的Kubernetes集群，只需一个命令即可。
    - 最新的：MicroK8s在同一天从Kubernetes核心项目中提取所有的修复和更新，确保其集群几乎即时拥有最新的可用变化。
    由于这些特点，在各种使用案例中，MicroK8s是比标准Kubernetes部署更好的选择：

    - 开发人员工作站： 提供新的开发者工作站可以自动化，并确保开发者有一个适当的Kubernetes环境来进行测试。
    - CI/CD服务器： 用可重复和固定的执行环境自动构建。
    - 物联网设备： 具有远程连接的小内存设备可以运行自己的Kubernetes集群。
3. MicroK8s的入门

    MicroK8s为每个主要的操作系统提供了[安装程序](https://microk8s.io/#install-microk8s)： Windows、Linux和macOS。

    默认情况下，MicroK8s在安装时禁用了大部分功能。因此，我们必须使用microk8s enable命令启用我们想要的功能。

    下面是我们可能想要启用的常见附加组件的列表，以获得传统的Kubernetes设置：

    - cert-manager： 云端原生的证书管理
    - dashboard： Kubernetes的仪表盘
    - dns： CoreDNS服务
    - ingress： 用于外部访问服务的[入口控制器](https://www.baeldung.com/ops/kubernetes-ingress-vs-load-balancer)
    - metallb： 负载平衡器控制器
    - metrics-server： 一个Kubernetes度量服务器，用于API访问服务度量。
    - prometheus： 用于监控和记录的Prometheus操作员
    - rbac： 用于授权的基于角色的访问控制
    例如，要启用仪表盘和ingress插件，我们可以运行：

    `$ microk8s enable dashboard ingress`

4. 使用MicroK8s

    安装并配置好MicroK8s后，让我们仔细看看如何使用它。

    1. 检查状态

        我们可以使用microk8s status命令来检查我们的Microk8s集群的状态：

        `$ microk8s status`

        这告诉我们集群是否正在运行，以及哪些功能已经启用。我们还可以使用传统的kubelet命令来检查集群：

        `$ microk8s kubelet get node`

        注意，也可以使用本地的kubelet命令。我们只需要为kubelet生成客户端配置即可：

        `$ microk8s kubectl config view --raw > ${HOME}/.kube/config`

        最后，我们可以停止和启动MicroK8s集群：

        `$ microk8s stop`

        `$ microk8s start`

        当在笔记本电脑或其他没有专用电源的设备上运行时，MicroK8s团队建议在不需要时关闭集群，以节省电力。

    2. 部署应用程序

        随着集群的建立和运行，我们现在可以使用几种不同的方式来部署应用程序。

        首先，我们可以使用传统的YAML文件来部署工作负载：

        `$ microk8s kubelet apply -f /path/to/deployment.yaml`

        此外，在启用Helm功能后，我们可以使用Helm图表部署应用程序：

        `$ microk8s helm install elasticsearch elastic/elasticsearch`

    3. 查看仪表板

        假设我们已经启用了仪表盘插件，我们可以通过首先启动端口转发来查看它：

        `$ microk8s kubectl port-forward -n kube-system service/kubernetes-dashboard 10443:443`

        然后我们可以使用URL <https://localhost:10443> 来查看仪表板。要登录，我们需要一个令牌或完整的kubeconfig：

        ```bash
        # 生成一个令牌
        $ microk8s kubectl create token default

        # 生成kubeconfig
        $ microk8s config
        ```

        注意，集群使用的是自签名的证书，这将导致网络浏览器的警告。

    4. 高可用性

        MicroK8s标榜自己是生产级的，因此，只要有多个节点可用，它也支持高可用性。从命令行中添加一个节点是很容易的：

        `$ microk8s add-node`

        这提供了我们启动新节点并将其加入集群所需的所有信息。

        从我们希望加入这个集群的节点，运行：

        `$ microk8s join 192.168.64.2:25000/16715886fa58dcf561acbd6df44c614d/14b471cb0bb3`

        默认情况下，新的节点是工作者，并运行控制平面，尽管有可能添加新的节点只是作为工作者。工作者节点可以安排工作负载，但不提供高可用性。至少需要三个节点来运行控制平面以获得高可用性。

5. 总结

    在这篇文章中，我们简单介绍了MicroK8s，一个最小的低OPS生产型Kubernetes。MicroK8s支持Kubernetes的全部功能，并可使用大量的附加组件进行扩展。此外，它的小内存和CPU占用率使它成为低CPU和内存环境的良好候选者，如开发人员工作站和DevOps管道。

## 相关文章

- [ ] [Introduction to MicroK8s](https://www.baeldung.com/ops/microk8s-introduction)
