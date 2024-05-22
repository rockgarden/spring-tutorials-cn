# Kubernetes部署vs.StatefulSets

1. 概述
    Kubernetes（K8s）是一个开源的容器编排系统。它允许我们自动部署、扩展和管理容器化应用程序。
    在本教程中，我们将讨论使用不同的Kubernetes资源在Kubernetes上部署应用程序（pods）的两种不同方法。以下是Kubernetes为部署pod提供的两种不同资源：

    - [部署Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
    - [状态集StatefulSet](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/)
    让我们先来看看有状态和无状态的应用程序之间的区别。
2. 有状态和无状态的应用程序
    有状态和无状态应用程序之间的关键区别是，无状态应用程序不"store"数据。另一方面，有状态的应用程序需要备份存储。例如，像Cassandra、MongoDB和MySQL数据库这样的应用需要某种类型的持久性存储，以便在服务重启后继续生存。
    保持状态对于运行有状态的应用程序是至关重要的。但对于无状态服务来说，任何数据流通常都是短暂的。而且，状态只存储在一个单独的后端服务中，如数据库。任何相关的存储通常都是短暂的。例如，如果容器重新启动，任何存储都会丢失。当组织采用容器时，他们倾向于从无状态容器开始，因为它们更容易采用。
    Kubernetes以管理无状态服务而闻名。部署工作负载更适合与无状态应用程序一起工作。就部署而言，pod是可以互换的。而StatefulSet为其管理的每个pod保持一个唯一的身份。每当它需要重新安排这些pod时，它都会使用相同的身份。
3. 部署
    1. 了解部署：基础知识
        Kubernetes部署提供了管理一组pod的方法。这些可以是一个或多个正在运行的容器，也可以是一组重复的pod，被称为ReplicaSets。部署使我们能够轻松地保持一组相同的吊舱以共同的配置运行。
        首先，我们定义我们的Kubernetes部署，然后部署它。然后，Kubernetes将努力确保部署所管理的所有pod符合我们所设定的任何要求。部署是pods的监督者。它让我们对如何以及何时推出新的pod版本进行细粒度的控制。当我们不得不回滚到以前的版本时，它也提供控制。
        在Kubernetes部署中，如果副本为1，控制器将验证当前状态是否等于ReplicaSet的期望状态，即1，如果当前状态为0，它将创建一个ReplicaSet。ReplicaSet将进一步创建pods。当我们创建一个名为web-app的Kubernetes部署时，它将创建一个名为`web-app-<replica-set-id>`的ReplicaSet。这个副本将进一步创建一个名为`web-app-<replica-set->-<pod-id>`的pod。

        Kubernetes部署通常用于无状态的应用程序。然而，我们可以通过给它附加一个持久化卷来保存部署的状态，并使其具有状态。部署的pods将共享同一个Volume，而且所有pods的数据都是一样的。
    2. Kubernetes中的部署组件
        以下是Kubernetes部署的主要组成部分：

        - Deployment Template 部署模板
        - PersistentVolume 持久卷
        - Service 服务
        首先，让我们制作我们的部署模板并将其保存为 "deployment.yaml"。在下面的模板中，我们还附加了一个持久化卷：

        ```yml
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: web-app-deployment
        spec:
        strategy:
            type: RollingUpdate
            rollingUpdate:
            maxSurge: 2
            maxUnavailable: 1
        selector:
            matchLabels:
            app: web-app
        replicas: 3
        template:
            metadata:
            labels:
                app: web-app
            spec:
            containers:
                - name: web-app
                image: hello-world:nanoserver-1809
                volumeMounts:
                - name: counter
                    mountPath: /app/
            volumes:
            - name: counter
                persistentVolumeClaim:
                claimName: counter
        ```

        在下面的模板中，我们有我们的PersistentVolumeClaim：

        ```yml
        apiVersion: v1
        kind: PersistentVolumeClaim
        metadata:
        name: counter
        spec:
        accessModes:
        - ReadWriteMany
        resources:
            requests:
            storage: 50Mi
        storageClassName: default
        ```

    3. 在Kubernetes中执行部署

        在我们执行部署之前，我们需要一个服务来访问上述部署。让我们创建一个NodePort类型的服务，并将其保存为'service.yaml' ：

        ```yml
        apiVersion: v1
        kind: Service
        metadata:
        name: web-app-service
        spec:
        ports:
            - name: http
            port: 80
            nodePort: 30080
        selector:
            name: web-app
        type: NodePort
        ```

        首先，我们用下面的kubectl apply命令运行服务模板：
        `kubectl apply -f service.yaml`
        然后我们对部署模板运行同样的命令：
        `kubectl apply -f deployment.yaml`
        此外，为了获得部署的详细描述，让我们运行kubectl describe命令：
        `kubectl describe deployment web-app-deployment`

        输出结果将与此类似：

        ```log
        Name:                web-app-deployment
        Namespace:          default
        CreationTimestamp:  Tue, 30 Aug 2016 18:11:37 -0700
        Labels:             app=web-app
        Annotations:        deployment.kubernetes.io/revision=1
        Selector:           app=web-app
        Replicas:           3 desired | 3 updated | 3 total | 3 available | 0 unavailable
        StrategyType:       RollingUpdate
        MinReadySeconds:    0
        RollingUpdateStrategy:  1 max unavailable, 2 max surge
        Pod Template:
        Labels:               app=web-app
        Containers:
            web-app:
            Image:              spring-boot-docker-project:1
            Port:               80/TCP
            Environment:        <none>
            Mounts:             <none>
        Volumes:              <none>
        Conditions:
        Type          Status  Reason
        ----          ------  ------
        Available     True    MinimumReplicasAvailable
        Progressing   True    NewReplicaSetAvailable
        OldReplicaSets:   <none>
        NewReplicaSet:    web-app-deployment-1771418926 (3/3 replicas created)
        No events.
        ```

        在上面的部分，我们观察到，部署在内部创建了一个ReplicaSet。然后，它在内部创建了 ReplicaSet 内的 Pod。在未来，当我们更新当前部署时，它将创建一个新的 ReplicaSet。然后，它将以可控的速度逐步将Pod从旧的ReplicaSet转移到新的。
        如果更新时发生错误，新的ReplicaSet将永远不会处于就绪状态。旧的ReplicaSet将不会再次终止，以确保在更新失败的情况下有100%的正常运行时间。在Kubernetes部署中，我们还可以手动回滚到之前的ReplicaSet，以防我们的新功能没有达到预期效果。
4. 状态集（StatefulSets）
    1. 基础知识
        在Kubernetes部署中，我们把pod当作牛，而不是宠物。如果其中一个牛成员生病或死亡，我们可以通过购买一个新的牛头来轻松取代它。这样的行动并不引人注意。同样地，如果一个pod在部署中倒下了，它就会带来另一个。在StatefulSets中，pod被赋予名字，并被当作宠物对待。如果你的一个宠物生病了，它马上就会被注意到。StatefulSets的情况也是如此，因为它通过名字与pod进行交互。
        StatefulSets为它的每个pod提供了两个稳定的唯一身份。首先，网络身份使我们能够为pod分配相同的DNS名称，无论重启的次数如何。IP地址可能仍然是不同的，所以消费者应该依赖DNS名称（或者观察变化并更新内部缓存）。
        其次，存储身份保持不变。网络身份总是接收相同的Storage实例，无论它在哪个节点上重新安排。

        StatefulSet也是一个控制器，但与Kubernetes部署不同，它不创建ReplicaSet，而是以独特的命名方式创建pod。每个pod根据模式接收DNS名称：`<statefulset name>-<ordinal index>`。例如，对于名称为mysql的StatefulSet，它将是mysql-0。
        一个有状态的集合的每个副本都会有自己的状态，每个pod都会创建自己的PVC（Persistent Volume Claim）。因此，一个有3个副本的StatefulSet将创建3个pod，每个pod都有自己的卷，所以总共有3个PVC。由于StatefulSets是与数据一起工作的，我们在停止pod实例时应该小心，要留出必要的时间将数据从内存中持久化到磁盘上。仍然可能有合理的理由进行强制删除，例如，当Kubernetes节点失败时。
    2. Headless服务
        一个有状态的应用程序需要具有唯一身份（主机名）的pod。一个pod应该能够到达其他具有明确名称的pod。一个StatefulSet需要一个Headless Service来工作。一个Headless Service是一个具有服务IP的服务。因此，它直接返回我们相关的pod的IP。这使得我们可以直接与pods交互，而不是通过代理。这就像为.spec.clusterIP指定None一样简单。
        无头服务没有一个IP地址。在内部，它创建了必要的端点，用DNS名称暴露pod。StatefulSet定义包括对无头服务的引用，但我们必须单独创建它。
    3. Kubernetes中的StatefulSets组件
        以下是StatefulSets的主要组件：

        - 状态集 StatefulSet
        - 持久卷（PersistentVolume）
        - 无头服务 Headless Service
        首先，我们创建一个StatefulSet模板：

        ```yml
        apiVersion: apps/v1
        kind: StatefulSet
        metadata:
        name: web
        spec:
        selector:
            matchLabels:
            app: nginx
        serviceName: "nginx"
        replicas: 3
        template:
            metadata:
            labels:
                app: nginx
            spec:
            containers:
            - name: nginx
                image: nginx
                ports:
                - containerPort: 80
                name: web
                volumeMounts:
                - name: www
                mountPath: /usr/share/nginx/html
            volumes:
            - name: www
                persistentVolumeClaim:
                claimName: myclaim
        ```

        其次，我们创建一个在StatefulSet模板中提到的PersistentVolume：

        ```yml
        apiVersion: v1
        kind: PersistentVolumeClaim
        metadata:
        name: myclaim
        spec:
        accessModes:
            - ReadWriteMany
        resources:
            requests:
            storage: 5Gi
        ```

        最后，我们现在为上述StatefulSet创建一个无头服务：

        ```yml
        apiVersion: v1
        kind: Service
        metadata:
        name: nginx
        labels:
            app: nginx
        spec:
        ports:
        - port: 80
            name: web
        clusterIP: None
        selector:
            app: nginx
        ```

    4. 在Kubernetes中执行StatefulSets
        我们已经为所有三个组件准备好了模板。现在，让我们运行create kubectl命令来创建StatefulSet：
        `kubectl create -f statefulset.yaml`

        它将创建三个名为web-0、web-1、web-2的pod。我们可以用get pods来验证创建是否正确：

        ```zsh
        kubectl get pods
        NAME      READY     STATUS    RESTARTS   AGE
        web-0     1/1       Running   0          1m
        web-1     1/1       Running   0          46s
        web-2     1/1       Running   0          18s
        ```

        我们还可以验证正在运行的服务：

        ```zsh
        kubectl get svc nginx
        NAME      TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
        nginx     ClusterIP   None         < none >      80/TCP    2m
        ```

        StatefulSets不创建ReplicaSet，所以我们不能将StatefulSet回滚到以前的版本。我们只能删除或扩大/缩小StatefulSet的规模。如果我们更新一个StatefulSet，它也会执行RollingUpdate，也就是说，一个副本pod会宕机，而更新后的pod会上来。类似地，那么下一个副本pod也会以同样的方式宕机。

        例如，我们改变上述StatefulSet的镜像。web-2将终止，一旦它完全终止，那么web-2将被重新创建，而web-1也将同时被终止。同样的情况也发生在下一个副本，即web-0。如果在更新时发生错误，那么只有web-2会被关闭，web-1和web-0仍然会被启动，运行在之前的稳定版本上。与部署不同，我们不能回滚到StatefulSet的任何先前版本。
        删除和/或缩小StatefulSet的规模不会删除与StatefulSet相关的卷。这确保了数据安全，这通常比自动清除所有相关的 StatefulSet 资源更有价值。在删除StatefulSets之后，对pod的终止没有任何保证。为了实现StatefulSet中的pod的有序和优雅的终止，可以在删除之前将StatefulSet的规模缩小到0。
    5. 状态集的使用
        StatefulSets使我们能够部署有状态的应用程序和集群应用程序。它们将数据保存到持久性存储中，例如计算引擎的持久性磁盘。它们适用于部署Kafka、MySQL、Redis、ZooKeeper和其他应用（需要唯一的、持久的身份和稳定的主机名）。
        例如，一个Solr数据库集群是由几个Zookeeper实例管理的。为了使这样的应用正常运行，每个Solr实例必须知道控制它的Zookeeper实例。同样，Zookeeper实例本身也会在彼此之间建立连接，选出一个主节点。由于这样的设计，Solr集群是一个有状态应用的例子。其他有状态应用的例子包括MySQL集群、Redis、Kafka、MongoDB，以及其他。在这种情况下，要使用StatefulSets。
5. 部署vs.StatefulSets
    让我们来看看部署与StatefulSet的主要区别：

    | 部署                                            | StatefulSet                                                                 |
    |-----------------------------------------------|-----------------------------------------------------------------------------|
    | 部署是用来部署无状态的应用程序的                              | StatefulSets是用来部署有状态的应用程序的                                                  |
    | Pod是可以互换的                                     | Pod是不能互换的。每个Pod都有一个持久的标识符，它在任何重新安排的情况下都能保持。                                 |
    | Pod的名字是唯一的                                    | Pod的名字是按顺序排列的                                                               |
    | 需要服务来与部署中的pod进行交互                             | 无头服务负责pod的网络标识                                                              |
    | 指定的PersistentVolumeClaim是由所有pod副本共享的。换句话说，共享卷 | 指定的volumeClaimTemplate使每个复制的pod得到一个唯一的PersistentVolumeClaim与它相关联。换句话说，没有共享卷 |

    Kubernetes部署是Kubernetes中的一个资源对象，为应用程序提供声明性更新。部署允许我们描述一个应用程序的生命周期。例如，应用程序要使用哪些镜像，应该有多少个pod，以及如何更新它们。
    StatefulSets更适合于有状态的应用程序。一个有状态的应用程序需要有独特身份的pod（例如，主机名）。一个pod将能够接触到具有明确定义的名称的其他pod。它需要一个无头服务来与pod连接。一个无头服务没有一个IP地址。在内部，它创建必要的端点，以暴露具有DNS名称的pod。
    StatefulSet的定义包括对无头服务的引用，但我们必须单独创建它。StatefulSet需要持久化存储，以便托管的应用程序在重新启动时保存其状态和数据。一旦StatefulSet和Headless Service被创建，一个pod就可以通过以服务名称为前缀的名称来访问另一个pod。
6. 结语
    在本教程中，我们已经了解了在Kubernetes中进行部署的两种方式：状态集（Statefulset）和部署（Deployment）。我们看到了它们的主要特点和组成部分，最后对它们进行了比较。

## 相关文章

- [x] [Kubernetes Deployment vs. StatefulSets](https://www.baeldung.com/ops/kubernetes-deployment-vs-statefulsets)
