# Docker-Compose和Kubernetes之间的区别

1. 概述
    在处理容器化应用程序时，我们可能想知道Docker Compose和Kubernetes在这种情况下扮演什么角色。
    在本教程中，我们将讨论一些最常见的用例，看看两者之间的区别。
2. Docker Compose
    [Docker Compose](https://www.baeldung.com/ops/docker-compose)是一个命令行工具，可以用YAML模板定义运行多个Docker容器。我们可以从现有的镜像或特定的环境中构建容器。
    我们可以添加一个版本的compose文件格式和至少一个服务。我们可以选择性地添加卷和网络。此外，我们还可以定义依赖关系和环境变量。
    1. Docker Compose模板

        让我们为一个连接到PostgreSQL数据库的API创建一个docker-compose.yml文件：

        ```yml
        version: '3.8'
        services:
        db:
            image: postgres:latest
            restart: always
            environment:
            - POSTGRES_USER=postgres
            - POSTGRES_PASSWORD=postgres
            ports:
            - '5432:5432'
            volumes: 
            - db:/var/lib/postgresql/data
            networks:
            - mynet

        api:
            container_name: my-api
            build:
            context: ./
            image: my-api
            depends_on:
            - db
            networks:
            - mynet
            ports:
            - 8080:8080
            environment:
            DB_HOST: db
            DB_PORT: 5432
            DB_USER: postgres
            DB_PASSWORD: postgres
            DB_NAME: postgres

        networks:
        mynet:
            driver: bridge

        volumes:
        db:
            driver: local
        ```

        最后，我们可以在本地或生产中通过运行开始工作：
        `docker-compose up`

    2. Docker Compose的常见用例

        我们通常使用Docker Compose来创建一个微服务基础设施环境，通过网络连接不同的服务。
        此外，Docker Compose被广泛用于为我们的测试套件创建和销毁隔离的测试环境。
        此外，如果我们对可扩展性感兴趣，我们可以看看[Docker Swarm](https://docs.docker.com/engine/swarm/)--一个由Docker创建的项目，像Kubernetes一样在协调层面工作。
        然而，与Kubernetes相比，Docker Swarm的产品有限。
3. Kubernetes
    通过Kubernetes（也被称为K8s），我们在一个容器化和集群化的环境中自动部署和管理应用程序。谷歌最初在K8s上的工作从开源到捐赠给Linux基金会，并最终作为种子技术启动了Cloud Native Computing Foundation（[CNCF](https://www.cncf.io/)）。
    在容器时代，Kubernetes得到了极大的关注，以至于它现在是最流行的分布式系统编排器。
    一个完整的[API](https://kubernetes.io/docs/concepts/overview/kubernetes-api/)可用于描述Kubernetes的对象的规格和状态。它还允许与第三方软件集成。
    在Kubernetes中，不同的[组件](https://kubernetes.io/docs/concepts/overview/components/)是集群的一部分，集群由一组称为Nodes的工作机组成。这些节点在Pod内运行我们的容器化应用程序。
    Kubernetes是关于管理部署在虚拟机或Nodes上的Pod的工件。节点和它们运行的容器都被分组为一个集群，每个容器都有端点、DNS、存储和可扩展性。
    Pod是非永久性的资源。例如，一个部署可以动态地创建和销毁它们。通常情况下，我们可以将应用程序暴露为[服务](https://kubernetes.io/docs/concepts/services-networking/)，以便始终在同一个端点上可用。
    1. Kubernetes模板
        Kubernetes提供了一种[声明式或命令式](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/)的方法，因此我们可以使用模板来创建、更新、删除甚至是扩展对象。作为一个例子，让我们为一个部署定义一个模板：

        ```yml
        -- Postgres Database
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: postgresql
        namespace: Database
        spec:
        selector:
            matchLabels:
            app: postgresql
        replicas: 1
        template:
            metadata:
            labels:
                app: postgresql
            spec:
            containers:
                - name: postgresql
                image: postgresql:latest
                ports:
                    - name: tcp
                    containerPort: 5432
                env:
                    - name: POSTGRES_USER
                    value: postgres
                    - name: POSTGRES_PASSWORD
                    value: postgres
                    - name: POSTGRES_DB
                    value: postgres
                volumeMounts:
                - mountPath: /var/lib/postgresql/data
                name: postgredb
            volumes:
                - name: postgredb
                persistentVolumeClaim:
                    claimName: postgres-data

        -- My Api
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: my-api
        namespace: Api
        spec:
        selector:
            matchLabels:
            app: my-api
        replicas: 1
        template:
            metadata:
            labels:
                app: my-api
            spec:
            containers:
                - name: my-api
                image: my-api:latest
                ports:
                    - containerPort: 8080
                    name: "http"
                volumeMounts:
                    - mountPath: "/app"
                    name: my-app-storage
                env:
                    - name: POSTGRES_DB
                    value: postgres
                    - name: POSTGRES_USER
                    value: postgres
                    - name: POSTGRES_PASSWORD
                    value: password
                resources:
                    limits:
                    memory: 2Gi
                    cpu: "1"
            volumes:
                - name: my-app-storage
                persistentVolumeClaim:
                    claimName: my-app-data
        ```

        然后我们可以通过kubectl命令行在网络上使用对象。
    2. Kubernetes和云供应商
        Kubernetes本身不是基础设施即代码Infrastructure-as-Code（IaC）。然而，它与云供应商的容器服务相整合--例如，亚马逊的ECS或EKS，谷歌的GKE，以及RedHat的OpenShift。
        或者我们可以使用它，例如，与[Helm](https://www.baeldung.com/ops/kubernetes-helm)等工具一起使用。
        我们确实经常在公共云基础设施中看到Kubernetes。然而，我们可以建立Minikube或本地Kubeadm集群。
        同样经过CNCF的批准，我们可以查看[K3s](https://k3s.io/)，这是K8s的一个轻量级版本。
4. Kubernetes和Docker Compose之间的区别
    Docker Compose是关于创建和启动一个或多个容器，而Kubernetes更多的是作为一个平台来创建一个网络，我们可以协调容器。
    Kubernetes已经能够促进应用管理中的许多关键问题：

    - 资源优化
    - 容器的自我修复
    - 应用程序重新部署期间的停机时间
    - 自动扩展

    最后，Kubernetes将多个孤立的容器带到了一个阶段，在这个阶段，资源总是可以得到潜在的最佳分配。
    然而，当涉及到开发时，Docker Compose可以配置所有应用程序的服务依赖，以开始进行，例如，我们的自动化测试。所以，它是本地开发的一个强大的工具。
5. 总结

    在这篇文章中，我们已经看到了Docker Compose和Kubernetes之间的区别。当我们需要定义和运行多容器Docker应用程序时，Docker Compose可以提供帮助。
    Kubernetes是一个强大而复杂的框架，用于管理集群环境中的容器化应用。

## Relevant Articles

- [x] [Difference Between Docker-Compose and Kubernetes](https://www.baeldung.com/ops/docker-compose-vs-kubernetes)
