# Kubernetes with kind

1. 概述

    在使用Kubernetes时，我们缺少一个有助于本地开发的工具--一个可以使用Docker容器作为节点运行本地Kubernetes集群的工具。

    在本教程中，我们将探讨Kubernetes的种类。[kind](https://kind.sigs.k8s.io/)主要是Kubernetes的测试工具，对于本地开发和CI也很方便。

2. 设置

    作为先决条件，我们应该确保Docker已经安装在我们的系统中。安装Docker的一个简单方法是使用适合我们操作系统（和处理器）的[Docker桌面](https://www.docker.com/products/docker-desktop)。

    1. 安装Kubernetes的命令行

        首先，让我们安装Kubernetes命令行，[kubectl](https://kubernetes.io/docs/tasks/tools/)。在macOS上，我们可以使用Homebrew来安装它：

        `$ brew install kubectl`

        我们可以用命令来验证安装是否成功：

        `$ kubectl version --short`

        同样地，我们可以在Windows上使用curl来下载：

        `curl -LO https://dl.k8s.io/v1.21.0/bin/windows/amd64/kubectl.exe.sha256`

        然后，我们应该把kubectl命令的二进制位置添加到我们的PATH变量中。

    2. 安装kind

        接下来，我们将在macOS上使用Homebrew安装kind：

        `$ brew install kind`

        为了验证安装是否成功，我们可以试试这个命令：

        `$ kind version`

        然而，如果kind版本命令不起作用，请将其位置添加到PATH变量中。

        同样地，对于Windows操作系统，我们可以用curl下载kind：

        ```bash
        curl -Lo kind-windows-amd64.exe https://kind.sigs.k8s.io/dl/v0.11.1/kind-windows-amd64
        Move-Item .\kind-windows-amd64.exe c:\kind\kind.exe
        ```

3. Kubernetes集群

    现在，我们已经准备好使用kind来为Kubernetes准备本地开发环境。

    1. 创建集群

        首先，让我们用默认配置创建一个本地Kubernetes集群：

        `$ kind create cluster`

        默认情况下，一个名为kind的集群将被创建。然而，我们可以使用-name参数为集群提供一个名称：

        `$ kind create cluster --name baeldung-kind`

        此外，我们还可以使用YAML配置文件来配置集群。例如，让我们在baeldungConfig.yaml文件中写一个简单的配置：

        ```yml
        kind: Cluster
        apiVersion: kind.x-k8s.io/v1
        name: baeldung-kind
        ```

        然后，让我们使用该配置文件创建集群：

        `$ kind create cluster --config baeldungConfig.yaml`

        此外，我们还可以在创建集群时提供一个特定版本的Kubernetes镜像：

        `$ kind create cluster --image kindest/node:v1.20.7`

    2. 获取集群

        让我们使用get命令来检查创建的集群：

        `$ kind get clusters`

        同时，我们可以确认相应的docker容器：

        `$ docker ps`

        或者，我们可以通过kubectl来确认这些节点：

        ```bash
        $ kubectl get nodes
        NAME                          STATUS   ROLES           AGE   VERSION
        baeldung-kind-control-plane   Ready    control-plane   67s   v1.25.3
        ```

    3. 集群细节

        一旦集群准备好了，我们可以使用kubectl上的cluster-info命令检查细节：

        `$ kubectl cluster-info --context kind-baeldung-kind`

        此外，我们还可以使用dump参数和cluster-info命令来提取集群的详细信息：

        `$ kubectl cluster-info dump --context kind-baeldung-kind`

    4. 删除集群

        与get命令类似，我们可以使用delete命令来删除一个特定的集群：

        `$ kind delete cluster --name baeldung-kind`

4. Ingress控制器

    1. 配置

        我们需要一个Ingress控制器来建立本地环境和Kubernetes集群之间的连接。

        因此，我们可以使用kind的配置选项，如extraPortMappings和node-labels：

        ```yml
        kind: Cluster
        apiVersion: kind.x-k8s.io/v1alpha4
        name: baeldung-kind
        nodes:
        - role: control-plane
        kubeadmConfigPatches:
        - |
            kind: InitConfiguration
            nodeRegistration:
            kubeletExtraArgs:
                node-labels: "ingress-ready=true"    
        extraPortMappings:
        - containerPort: 80
            hostPort: 80
            protocol: TCP
        - containerPort: 443
            hostPort: 443
            protocol: TCP
        ```

        在这里，我们已经更新了我们的baeldungConfig.yaml文件，以设置ingress控制器的配置，将容器端口映射到主机端口。此外，我们还通过定义ingress-ready=true来启用ingress的节点。

        然后，我们必须用修改后的配置重新创建我们的集群：

        `kind create cluster --config baeldungConfig.yaml`

    2. 部署

        然后，我们将部署Kubernetes支持的[ingress NGINX控制器](https://git.k8s.io/ingress-nginx/README.md#readme)，作为反向代理和负载平衡器工作：

        `kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml`

        此外，我们还可以使用[AWS](https://github.com/kubernetes-sigs/aws-load-balancer-controller#readme)和[GCE的负载平衡器控制器](https://git.k8s.io/ingress-gce/README.md#readme)。

5. 在本地部署一个服务

    最后，我们就可以部署我们的服务了。在本教程中，我们可以使用一个简单的[http-echo](https://hub.docker.com/r/hashicorp/http-echo/)网络服务器，以docker镜像的形式提供。

    1. 配置

        那么，让我们创建一个定义服务的配置文件，并使用ingress在本地托管它：

        ```yml
        kind: Pod
        apiVersion: v1
        metadata:
          name: baeldung-app
          labels:
            app: baeldung-app
        spec:
          containers:
          - name: baeldung-app
            image: hashicorp/http-echo:0.2.3
            args:
            - "-text=Hello World! This is a Baeldung Kubernetes with kind App"
        ---
        kind: Service
        apiVersion: v1
        metadata:
          name: baeldung-service
        spec:
          selector:
            app: baeldung-app
          ports:
          # Default port used by the image
          - port: 5678
        ---
        apiVersion: networking.k8s.io/v1
        kind: Ingress
        metadata:
          name: baeldung-ingress
        spec:
          rules:
          - http:
              paths:
              - pathType: Prefix
                path: "/baeldung"
                backend:
                  service:
                    name: baeldung-service
                    port:
                      number: 5678
        ---
        ```

        在这里，我们创建了一个名为baeldung-app的pod，带有文本参数和一个名为baeldung-service的服务。

        然后，我们在5678端口并通过/baeldung URI设置了与baeldung-服务的ingress网络。

    2. 部署

        现在我们已经准备好了所有的配置，而且我们的集群已经与入口的NGINX控制器集成，让我们来部署我们的服务：

        `$ kubectl apply -f baeldung-service.yaml`

        我们可以在kubectl上检查服务的状态：

        `$ kubectl get services`

        这就是了! 我们的服务已经部署完毕，应该可以在localhost/baeldung上使用：

        `$ curl localhost/baeldung`

        - [ ] **ERROR**

        ```log
        <html>
        <head><title>503 Service Temporarily Unavailable</title></head>
        <body>
        <center><h1>503 Service Temporarily Unavailable</h1></center>
        <hr><center>nginx</center>
        </body>
        </html>
        ```

        注意：如果我们遇到任何与validate.nginx.ingress.kubernetes.io webhook有关的错误，我们应该删除ValidationWebhookConfiguration：

        `$ kubectl delete -A ValidatingWebhookConfiguration ingress-nginx-admission`

        然后，再次部署该服务。

6. 结语

    在这篇文章中，我们用实物探索了Kubernetes。

    首先，我们做了一个设置，包括安装Kubernetes命令行kubectl和kind。然后，我们通过kind的一些功能来创建/更新一个Kubernetes本地集群。

    最后，我们集成了入口控制器，并在Kubernetes集群上部署了一个私有的可访问服务。

## 相关文章

- [ ] [Kubernetes with kind](https://www.baeldung.com/ops/kubernetes-kind)
