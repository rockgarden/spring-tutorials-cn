# Kubernetes中的Ingress与负载平衡器

1. 简介

    对于许多软件应用程序来说，与外部服务进行通信是完成其任务的必要条件。无论是发送消息还是消费API，大多数应用程序都依赖其他系统来正常运行。

    然而，随着越来越多的用户将他们的应用程序转移到Kubernetes中，为他们提供安全可靠的访问变得更具挑战性。在各种部署和服务中导航会使网络流量难以到达正确的地方。

    幸运的是，有一些不同的机制可以帮助管理网络流量，并确保请求到达集群内的理想目的地。在本教程中，我们将研究其中的两种机制：入口和负载平衡器。

2. 工作负载和服务

    在讨论这两种机制之前，我们必须先退一步，看看应用程序如何在Kubernetes中部署和管理。

    1. 工作负载和Pod

        我们首先将我们的应用程序打包成docker镜像。然后，这些docker镜像被用来创建预先定义的[工作负载](https://kubernetes.io/docs/concepts/workloads/)类型之一，例如：

        - ReplicaSet： 确保在任何时候都有最低数量的pod可用。
        - 状态集（StatefulSet）： 当增加或减少pod的数量时，提供一个可预测的和独特的排序。
        - DameonSet： 确保特定数量的pod在任何时候都在一些或所有节点上运行
        所有这些工作负载都创建了一个或多个正在集群中部署的pod。一个pod是我们在Kubernetes中可以使用的最小的可部署单元。它本质上代表了一个在集群中某处运行的应用程序。

        默认情况下，每个pod都有一个唯一的IP，可以被集群的其他成员访问。然而，由于多种原因，使用其IP地址访问一个pod并不是一个好的做法。

        首先，不容易知道什么IP将被提前分配给一个pod。这使得它几乎不可能将IP信息存储在其他应用程序可以访问的配置中。其次，许多工作负载创建多个pod--在某些情况下是动态的。这意味着，在任何时间点，我们可能不知道一个特定的应用程序有多少个pod在运行。

        最后，pod是非永久性的资源。它们要随着时间的推移而启动和停止，每次发生这种情况时，它们很可能会得到一个新的IP地址。

        由于所有这些原因，使用pod的IP地址与之通信是一个坏主意。相反，我们应该使用服务。

    2. 服务

        Kubernetes服务是一个抽象概念，它将一组pod作为网络服务公开。该服务处理所有识别运行中的pod及其IP地址的复杂性。每个服务都有一个唯一的URL，可以在集群中访问。因此，pod不需要使用IP进行通信，而只需要使用提供的服务URL。

        让我们来看看一个服务定义的例子：

        ```yml
        apiVersion: v1
        kind: Service
        metadata:
        name: api-service
        spec:
        selector:
            app: api-app
        ports:
            - protocol: TCP
            port: 80
            targetPort: 8080
        ```

        这将在集群中创建一个名为api-service的服务。这个服务被绑定到任何运行应用程序api-app的pod上，不管这些pod有多少，也不管它们在哪里运行。而且，当这种类型的新荚启动时，该服务将自动发现它们。

        使用服务是将pods与使用它们的应用程序解耦的一个良好开端。但是，就其本身而言，它们并不总是能实现我们的预期目标。这就是 ingresses 和负载平衡器的作用。

3. 摄入器

    默认情况下，Kubernetes服务对集群是私有的。这意味着只有集群内的应用程序可以访问它们。有很多方法可以解决这个问题，其中一个最好的方法是入口ingress。

    在Kubernetes中，入口让我们将流量从集群外路由到集群内的一个或多个服务。通常情况下，入口处作为所有传入流量的单一入口点。

    一个入口收到一个公共IP，意味着它可以在集群外被访问。然后，使用一套规则，它将所有的流量转发到一个适当的服务。反过来，该服务将把请求发送到可以实际处理该请求的pod。

    在创建一个入口时，有几件事需要记住。首先，它们被设计用来处理网络流量（HTTP或HTTPS）。虽然有可能在其他类型的协议中使用ingress，但它通常需要额外的配置。

    第二，一个入口处可以做的不仅仅是路由。其他一些用例包括负载平衡和SSL终止。

    最重要的是，ingress对象本身并不实际做任何事情。因此，为了让ingress实际做任何事情，我们需要有一个ingress控制器可用。

    1. 入站(Ingress)控制器

        与大多数Kubernetes对象一样，ingress需要一个相关的控制器来管理它。然而，虽然Kubernetes为大多数对象（如部署和服务）提供了控制器，但它默认不包括入口控制器。因此，这取决于集群管理员，以确保有一个适当的控制器。

        大多数云平台提供他们自己的入口控制器，但也有很多开源的选择。也许最流行的是[nginx ingress控制器](https://www.nginx.com/products/nginx-ingress-controller/)，它是建立在流行的同名网络服务器之上的。

        让我们看看使用nginx ingress控制器的配置样本：

        ```yml
        apiVersion: networking.k8s.io/v1
        kind: Ingress
        metadata:
        name: ingress-example
        annotations:
            nginx.ingress.kubernetes.io/rewrite-target: /
        spec:
        ingressClassName: nginx-example
        rules:
        - http:
            paths:
            - path: /api
                pathType: Prefix
                backend:
                service:
                    name: api-service
                    port:
                    number: 80
        ```

        在这个例子中，我们创建了一个入口，将任何以/api开头的请求路由到一个名为api-service的Kubernetes服务。

        注意，注释字段包含了nginx的特定值。因为所有的入口控制器都使用相同的API对象，我们通常使用注解字段来传递特定的配置到入口控制器。

        在Kubernetes生态系统中，有几十个可用的[入口控制器](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/)，涵盖所有这些控制器远远超出了本文的范围。然而，由于它们使用相同的API对象，它们共享一些共同的特征：

        - Ingress Rules 入站规则：定义如何将流量路由到特定服务的规则集（通常基于URL或主机名）
        - Default Backend 默认后端： 一个默认资源，处理不符合任何规则的流量
        - TLS：一个定义私钥和证书的秘密，以允许TLS终止
        不同的入口控制器建立在这些概念之上，并添加自己的功能和特性。

4. 负载平衡器

    Kubernetes中的[负载平衡器](https://kubernetes.io/docs/concepts/services-networking/service/#loadbalancer)与入口控制器有相当多的重合。这是因为它们主要用于将服务暴露在互联网上，正如我们在上面看到的，这也是ingresses的一个特点。

    然而，负载均衡器具有与 ingresses 不同的特征。负载平衡器不像 ingress 那样是一个独立的对象，它只是一个服务的扩展。

    让我们看一个带有负载均衡器的服务的简单例子：

    ```yml
    apiVersion: v1
    kind: Service
    metadata:
    name: api-service
    spec:
    selector:
        app: api-app
    ports:
        - protocol: TCP
        port: 80
        targetPort: 8080
    type: LoadBalancer
    ```

    就像我们之前的服务例子一样，在这里，我们要创建一个服务，将流量路由到任何运行我们API应用的pod。在这种情况下，我们已经包括了一个LoadBalancer配置。

    为了使其发挥作用，集群必须运行在一个支持外部负载平衡器的供应商上。所有主要的云提供商都支持使用他们自己的资源类型的外部负载平衡器：

    - AWS使用一个[网络负载平衡器](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)
    - GKE也使用[网络负载平衡器](https://cloud.google.com/load-balancing/docs/network)
    - Azure使用[公共负载平衡器](https://docs.microsoft.com/en-us/azure/aks/load-balancer-standard)
    就像我们看到的不同的入口控制器一样，不同的负载平衡器供应商有自己的设置。通常情况下，我们直接用CLI或基础设施的特定工具来管理这些设置，而不是用YAML。不同的负载平衡器实现也会提供额外的功能，如SSL终止。

    因为负载均衡器是按服务定义的，所以它们只能路由到一个服务。这与入口不同，入口有能力路由到集群内的多个服务。

    此外，请记住，无论哪个供应商，使用外部负载平衡器通常会带来额外的成本。这是因为，与接入点及其控制器不同，外部负载平衡器存在于Kubernetes集群之外。正因为如此，大多数云供应商会对集群本身以外的额外资源使用进行收费。

5. 总结

    在这篇文章中，我们已经看了Kubernetes的一些核心概念，包括部署、服务和入口。这些对象中的每一个都在各个pod之间的网络流量路由中起着关键作用。

    虽然ingresses和负载均衡器在功能上有很多重叠，但它们的行为是不同的。主要的区别是ingresses是集群内部的本地对象，可以路由到多个服务，而负载均衡器是集群外部的，只能路由到一个服务。

## 相关文章

- [ ] [Ingress vs. Load Balancer in Kubernetes](https://www.baeldung.com/ops/kubernetes-ingress-vs-load-balancer)
