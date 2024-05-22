# 使用Spring Boot的Kong Ingress控制器

1. 概述

    Kubernetes（K8s）是一个协调器，可以自动进行软件开发和部署，是目前API托管的一个潮流选择，可以在企业内部或云服务上运行，如谷歌云Kubernetes服务（GKS）或亚马逊弹性Kubernetes服务（EKS）。另一方面，Spring已经成为最受欢迎的Java框架之一。

    在本教程中，我们将演示如何建立一个受保护的环境，使用Kong Ingress Controller（KIC）在Kubernetes上部署我们的Spring Boot应用程序。我们还将通过为我们的应用程序实现一个简单的速率限制器来演示KIC的高级使用，而无需任何编码。

2. 改进安全和访问控制

    现代应用部署，特别是API，有许多挑战需要处理，如：隐私法（例如，GPDR），安全问题（DDOS），以及使用跟踪（例如，API配额和速率限制）。在这种情况下，现代应用程序和API需要额外的保护级别来应对所有这些挑战，如防火墙、反向代理、速率限制器和相关服务。尽管K8s环境保护我们的应用程序免受许多这样的威胁，但我们仍然需要采取一些措施来保证我们的应用程序的安全。这些措施之一是部署一个入口控制器，并设置其对你的应用程序的访问规则。

    ingress是一个管理外部访问K8s集群和部署在其上的应用程序的对象，它将HTTP / HTTPS路由暴露给部署的应用程序并对其执行访问规则。为了暴露应用程序以允许外部访问，我们需要定义入口规则并使用入口控制器，它是一个专门的反向代理和负载平衡器。一般来说，Ingress控制器由第三方公司提供，功能各不相同，如本文中使用的[Kong Ingress控制器](https://docs.konghq.com/kubernetes-ingress-controller/latest/)。

3. 设置环境

    为了演示Kong Ingress Controller（KIC）与Spring Boot应用程序的使用，我们需要访问一个K8s集群，因此我们可以使用完整的Kubernetes，在企业内部安装或云端提供，或者使用Minikube开发我们的示例应用程序。在启动我们的K8s环境后，我们需要在集群上部署[Kong Ingress Controller](https://docs.konghq.com/kubernetes-ingress-controller/2.7.x/guides/getting-started/)。Kong暴露了一个外部IP，我们需要用它来访问我们的应用程序，所以用这个地址创建一个环境变量是一个好的做法：

    `export PROXY_IP=$(minikube service -n kong kong-proxy --url | head -1)`

    这就是了! Kong Ingress Controller已经安装，我们可以通过访问PROXY_IP来测试它是否在运行：

    `curl -i $PROXY_IP`

    响应应该是一个404错误，这是正确的，因为我们还没有部署任何应用程序，所以它应该说没有路由与这些值相匹配。现在是时候创建一个示例应用程序了，但在此之前，如果没有Docker，我们可能需要安装它。为了将我们的应用程序部署到K8s，我们需要一种方法来创建一个容器镜像，我们可以使用Docker来实现。

4. 创建一个Spring Boot应用样本

    现在我们需要一个Spring Boot应用程序，并将其部署到K8s集群上。由Spring Initializer网站 <https://start.spring.io/>。

    为了生成一个简单的HTTP服务器应用程序，至少有一个暴露的网络资源，我们可以这样做：

    `curl https://start.spring.io/starter.tgz -d dependencies=webflux,actuator -d type=maven-project | tar -xzvf -`

    有一件事很重要，就是选择默认的Java版本。如果我们需要使用旧版本，那么就需要一个javaVersion属性：

    `curl https://start.spring.io/starter.tgz -d dependencies=webflux,actuator -d type=maven-project -d javaVersion=11 | tar -xzvf -`

    在这个示例程序中，我们选择了webflux，它用Spring WebFlux和Netty生成了一个反应式Web应用。但又增加了一个重要的依赖项。 Actuator，它是Spring应用的监控工具，已经暴露了一些网络资源，这正是我们需要用Kong来测试的。这样一来，我们的应用程序已经暴露了一些我们可以使用的网络资源。让我们来构建它：

    `./mvnw install`

    生成的jar是可执行的，所以我们可以通过运行它来测试该应用程序：

    `java -jar target/*.jar`

    为了测试该应用程序，我们需要打开另一个终端并输入这个命令：

    `curl -i http://localhost:8080/actuator/health`

    响应必须是应用程序的健康状态，由执行器actuator提供：

    ```log
    HTTP/1.1 200 OK
    Content-Type: application/vnd.spring-boot.actuator.v3+json
    Content-Length: 15

    {"status":"UP"}
    ```

5. 从应用程序生成一个容器镜像

    将应用程序部署到Kubernetes集群的过程包括创建和部署一个容器镜像到集群可访问的存储库。在现实生活中，我们会把镜像推送到DockerHub或我们自己的私有容器镜像注册中心。但是，由于我们正在使用Minikube，让我们把我们的Docker客户端环境变量指向Minikube的Docker：

    `$(minikube docker-env)`

    然后我们就可以建立应用程序的镜像了：

    `./mvnw spring-boot:build-image`

6. 部署应用程序

    现在是在我们的K8s集群上部署应用程序的时候了。我们需要创建一些K8s对象来部署和测试我们的应用程序，所有需要的文件都可以在演示的资源库中找到：

    带有容器规格的应用程序的部署对象

    - 一个服务定义，为我们的Pod分配一个集群IP地址
    - 一个使用Kong的代理IP地址和我们的路由的入口规则。
    - 一个部署对象只是创建必要的pod来运行我们的镜像，这是创建它的YAML文件：

    ```yml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
    creationTimestamp: null
    labels:
        app: demo
    name: demo
    spec:
    replicas: 1
    selector:
        matchLabels:
        app: demo
    strategy: {}
    template:
        metadata:
        creationTimestamp: null
        labels:
            app: demo
        spec:
        containers:
        - image: docker.io/library/demo:0.0.1-SNAPSHOT
            name: demo
            resources: {}
            imagePullPolicy: Never

    status: {}
    ```

    我们正指向在Minikube内部创建的image，得到它的全名。注意，有必要将imagePullPolicy属性指定为Never，因为我们没有使用镜像注册服务器，所以我们不希望K8s尝试下载镜像，而是使用已经在其内部Docker档案中的镜像。我们可以用kubectl来部署它：

    `kubectl apply -f serviceDeployment.yaml`

    如果部署成功，我们可以看到这样的信息：

    `deployment.apps/demo created`

    为了给我们的应用程序提供一个统一的IP地址，我们需要创建一个服务，为其分配一个内部集群范围内的IP地址，这就是创建它的YAML文件：

    ```yml
    apiVersion: v1
    kind: Service
    metadata:
    creationTimestamp: null
    labels:
        app: demo
    name: demo
    spec:
    ports:
    - name: 8080-8080
        port: 8080
        protocol: TCP
        targetPort: 8080
    selector:
        app: demo
    type: ClusterIP
    status:
    loadBalancer: {}
    ```

    现在我们也可以用kubectl进行部署了：

    `kubectl apply -f clusterIp.yaml`

    注意，我们正在选择指向我们部署的应用程序的标签演示。为了能被外部访问（在K8s集群之外），我们需要创建一个入口规则，在我们的例子中，我们把它指向路径/actuator/health和端口8080：

    ```yml
    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
    name: demo
    spec:
    ingressClassName: kong
    rules:
    - http:
        paths:
        - path: /actuator/health
            pathType: ImplementationSpecific
            backend:
            service:
                name: demo
                port:
                number: 8080
    ```

    最后，我们用kubectl部署它：

    `kubectl apply -f ingress-rule.yaml`

    现在我们可以使用Kong的代理IP地址进行外部访问：

    ```bash
    $ curl -i $PROXY_IP/actuator/health
    HTTP/1.1 200 OK
    Content-Type: application/vnd.spring-boot.actuator.v3+json
    Content-Length: 49
    Connection: keep-alive
    X-Kong-Upstream-Latency: 325
    X-Kong-Proxy-Latency: 1
    Via: kong/3.0.0
    ```

7. 演示速率限制器

    我们设法在Kubernetes上部署了一个Spring Boot应用，并使用Kong Ingress Controller提供访问。但KIC的功能远不止这些：认证、负载均衡、监控、速率限制和其他功能。为了展示Kong的真正威力，我们将为我们的应用程序实现一个简单的速率限制器，将访问限制在每分钟只有五个请求。要做到这一点，我们需要在我们的K8s集群中创建一个名为KongClusterPlugin的对象。这个YAML文件就是这样做的：

    ```yml
    apiVersion: configuration.konghq.com/v1
    kind: KongClusterPlugin
    metadata:
    name: global-rate-limit
    annotations:
        kubernetes.io/ingress.class: kong
    labels:
        global: true
    config:
    minute: 5
    policy: local
    plugin: rate-limiting
    ```

    该插件配置允许我们为我们的应用程序指定额外的访问规则，我们将对它的访问限制在每分钟五个请求。让我们应用这个配置并测试一下结果：

    `kubectl apply -f rate-limiter.yaml`

    为了测试，我们可以在一分钟内重复之前使用的CURL命令超过五次，就会出现429错误：

    ```bash
    curl -i $PROXY_IP/actuator/health
    HTTP/1.1 429 Too Many Requests
    Date: Sun, 06 Nov 2022 19:33:36 GMT
    Content-Type: application/json; charset=utf-8
    Connection: keep-alive
    RateLimit-Reset: 24
    Retry-After: 24
    X-RateLimit-Remaining-Minute: 0
    X-RateLimit-Limit-Minute: 5
    RateLimit-Remaining: 0
    RateLimit-Limit: 5
    Content-Length: 41
    X-Kong-Response-Latency: 0
    Server: kong/3.0.0

    {
    "message":"API rate limit exceeded"
    }
    ```

    我们可以看到响应的HTTP头信息，告知客户端速率限制的情况。

8. 清理资源

    为了清理演示，我们需要按后进先出的顺序删除所有的对象：

    ```bash
    kubectl delete -f rate-limiter.yaml
    kubectl delete -f ingress-rule.yaml
    kubectl delete -f clusterIp.yaml
    kubectl delete -f serviceDeployment.yaml
    ```

    并停止Minikube集群：

    `minikube stop`

9. 结论

    在这篇文章中，我们演示了如何使用Kong Ingress Controller来管理对部署在K8s集群上的Spring Boot应用程序的访问。

## Relevant Articles

- [ ] [Kong Ingress Controller with Spring Boot](https://www.baeldung.com/spring-boot-kong-ingress)

## Code

一如既往，源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/kubernetes-modules/kubernetes-spring)上找到。

### 运行演示的步骤

1. 安装 minikube: <https://www.baeldung.com/spring-boot-minikube>

2. 在minikube上安装Kong Ingress控制器: <https://docs.konghq.com/kubernetes-ingress-controller/latest/deployment/minikube/>
    - 测试哪一个 echo-server
    - 创建环境变量: export PROXY_IP=$(minikube service -n kong kong-proxy --url | head -1)

3. 构建该Spring boot服务
    - Run `./mvnw install`
    - Run `jar: java -jar target/*.jar`

4. 创建一个Docker镜像：
    如果使用minikube并且不想把镜像推送到仓库，那么就把你的本地Docker客户端指向Minikube的实现：eval $(minikube -p minikube docker-env) ---使用相同的shell。
    - Run: `./mvnw spring-boot:build-image`

5. 部署应用程序，创建一个服务和一个入口规则：

    ```bash
    kubectl apply -f serviceDeployment.yaml
    kubectl apply -f clusterIp.yaml
    kubectl apply -f ingress-rule.yaml
    ```

6. 使用代理IP测试访问：

    `curl -i $PROXY_IP/actuator/health`

### 在你的api上设置一个速率限制器

1. 创建一个插件：

    `kubectl apply -f rate-limiter.yaml`

2. 现在测试资源。每分钟尝试5次以上：

    `curl -i $PROXY_IP/actuator/health`
