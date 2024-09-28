# [用Minikube运行Spring Boot应用程序](https://www.baeldung.com/spring-boot-minikube)

1. 概述

    在之前的[文章](https://www.baeldung.com/kubernetes)中，我们对Kubernetes进行了理论上的介绍。

    在本教程中，我们将讨论如何在本地Kubernetes环境（也称为Minikube）中部署Spring Boot应用程序。

    作为本文的一部分，我们将。

    - 在我们的本地机器上安装Minikube
    - 开发一个由两个Spring Boot服务组成的示例应用程序
    - 使用Minikube在一个单节点集群上设置该应用程序
    - 使用配置文件部署该应用程序

2. 安装Minikube

    Minikube的安装基本上包括三个步骤：安装Hypervisor（如VirtualBox），CLI kubectl，以及Minikube本身。

    [官方文档](https://kubernetes.io/docs/tasks/tools/install-minikube/)为每一个步骤提供了详细的说明，并且适用于所有流行的操作系统。

    完成安装后，我们可以启动Minikube，将VirtualBox设置为Hypervisor，并配置kubectl与名为minikube的集群对话。

    ```bash
    $> minikube start
    $> minikube config set vm-driver virtualbox
    $> kubectl config use-context minikube
    ```

    之后，我们可以验证kubectl是否与我们的集群正确通信。

    `$> kubectl cluster-info`

    输出应该是这样的：

    ```log
    Kubernetes master is running at https://192.168.99.100:8443
    To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
    ```

    在这个阶段，我们将保持响应中的IP接近（在我们的例子中是192.168.99.100）。我们以后会把它称为NodeIP，从集群外部调用资源时需要它，例如从我们的浏览器。

    ```log
    // macOS `brew install minikube`
    Kubernetes control plane is running at https://127.0.0.1:63965
    CoreDNS is running at https://127.0.0.1:63965/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
    To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
    ```

    最后，我们可以检查我们集群的状态。

    `$> minikube dashboard`

    这个命令在我们的默认浏览器中打开一个网站，它提供了一个关于我们集群状态的广泛概述。

3. 演示应用程序

    由于我们的集群现在正在运行并准备部署，我们需要一个演示应用程序。

    为此，我们将创建一个简单的 "Hello world" 应用程序，由两个Spring Boot服务组成，我们称之为前端和后端。

    后台在8080端口提供一个REST端点，返回一个包含其主机名的字符串。前端在8081端口提供，它将简单地调用后端端点并返回其响应。

    之后，我们必须从每个应用程序中构建一个Docker镜像。所有必要的文件也可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-kubernetes)上找到。

    关于如何构建Docker镜像的详细说明，请看[Dockerizing a Spring Boot Application](https://www.baeldung.com/dockerizing-spring-boot-application#Dockerize)。

    在这里，我们必须确保在Minikube集群的Docker主机上触发构建过程，否则，Minikube在以后的部署过程中不会找到这些图像。此外，我们主机上的工作空间必须被挂载到Minikube虚拟机中。

    ```bash
    $> minikube ssh
    $> cd /c/workspace/tutorials/spring-cloud/spring-cloud-kubernetes/demo-backend
    $> docker build --file=Dockerfile --tag=demo-backend:latest --rm=true .
    ```

    之后，我们可以注销Minikube虚拟机，所有进一步的步骤将在我们的主机上使用kubectl和minikube命令行工具执行。

    ```bash
    // macOS `minikube mount <source directory>:<target directory>`
    // 先在工程中运行 mvn package 生成 jar 包再加载
    % minikube mount /Users/wangkan/git/spring-tutorials-cn/spring-cloud-modules/spring-cloud-kubernetes/kubernetes-minikube:/kubernetes-minikube
    📁  Mounting host path /Users/wangkan/git/spring-tutorials-cn/spring-cloud-modules/spring-cloud-kubernetes/kubernetes-minikube into VM as /kubernetes-minikube ...
    ▪ Mount type:   
    ▪ 用户 ID：      docker
    ▪ Group ID:     docker
    ▪ 版本：      9p2000.L
    ▪ Message Size: 262144
    ▪ Options:      map[]
    ▪ 绑定地址：127.0.0.1:64188
    🚀  Userspace file server: ufs starting
    ✅  Successfully mounted /Users/wangkan/git/spring-tutorials-cn/spring-cloud-modules/spring-cloud-kubernetes/kubernetes-minikube to /kubernetes-minikube
    📌  NOTE: This process must stay alive for the mount to be accessible ...
    ```

    ```bash
    // macOS 启用另一个终端进程
    $> minikube ssh
    $> cd /kubernetes-minikube/demo-backend
    $> docker build --file=Dockerfile --tag=demo-backend:latest --rm=true .
    ```

4. 使用Imperative命令进行简单部署

    第一步，我们将为我们的演示后端应用程序创建一个部署，只由一个Pod组成。在此基础上，我们将讨论一些命令，以便我们能够验证部署，检查日志，并在最后清理它。

    1. 创建部署

        我们将使用kubectl，将所有需要的命令作为参数传递。

        `$> kubectl run demo-backend --image=demo-backend:latest --port=8080 --image-pull-policy Never`

        正如我们所看到的，我们创建了一个名为demo-backend的部署，它是从一个同样名为demo-backend的镜像中实例化出来的，版本为最新。

        通过-port，我们指定该部署为其Pod打开8080端口（因为我们的demo-backend应用程序监听8080端口）。

        标志-image-pull-policy Never确保Minikube不会尝试从注册中心中提取image，而是从本地Docker主机中提取。

        - [x] **ERROR** no main manifest attribute, in /app.jar
          项目基于maven pom多模块的开发的，需要设置goal-repackage属性为true，否则打包后文件依赖文件没有一起打包，然后镜像内没有可以运行的程序文件。

    2. 验证部署

        现在，我们可以检查部署是否成功。

        `$> kubectl get deployments`

        输出看起来像这样。

        ```log
        NAME           DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
        demo-backend   1         1         1            1           19s
        ```

        如果我们想看一下应用程序的日志，我们首先需要Pod ID。

        ```bash
        $> kubectl get pods
        $> kubectl logs <pod id>.
        ```

    3. 为部署创建一个服务

        为了使我们的后端应用程序的REST端点可用，我们需要创建一个服务。

        `$> kubectl expose deployment demo-backend --type=NodePort`

        *-type=NodePort* 使该服务在集群外可用。它将在`<NodeIP>:<NodePort>`上可用，也就是说，该服务将任何从`<NodePort>`传入的请求映射到其分配的Pod的8080端口。

        我们使用expose命令，所以NodePort将由集群自动设置（这是一个技术限制），默认范围是30000-32767。为了获得我们选择的端口，我们可以使用配置文件，我们将在下一节看到。

        我们可以验证该服务是否创建成功。

        `$> kubectl get services`

        输出看起来像这样。

        ```log
        NAME           TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
        demo-backend   NodePort    10.106.11.133   <none>        8080:30117/TCP   11m
        ```

        我们可以看到，我们有一个叫做demo-backend的服务，类型是NodePort，它在集群内部IP 10.106.11.133 上可用。

        我们必须仔细看看PORT(S)列：由于端口8080是在部署中定义的，该服务将流量转发到这个端口。然而，如果我们想从我们的浏览器中调用演示后台，我们必须使用30117端口，它可以从集群外部到达。

    4. 调用服务

        现在，我们可以第一次调用我们的后端服务了。

        `$> minikube service demo-backend`

        这个命令将启动我们的默认浏览器，打开`<NodeIP>:<NodePort>`。在我们的例子中，这将是<http://192.168.99.100:30117>。

    5. 清理服务和部署

        之后，我们可以删除服务和部署。

        ```log
        $> kubectl delete service demo-backend
        $> kubectl delete deployment demo-backend
        ```

5. 使用配置文件的复杂部署

    对于更复杂的设置，配置文件是一个更好的选择，而不是通过命令行参数传递所有参数。

    配置文件是记录我们的部署的一个很好的方式，而且它们可以被版本控制。

    1. 我们的后端应用程序的服务定义

        让我们用配置文件来重新定义我们的后端服务。

        ```yaml
        kind: Service
        apiVersion: v1
        metadata:
        name: demo-backend
        spec:
        selector:
            app: demo-backend
        ports:
        - protocol: TCP
            port: 8080
        type: ClusterIP
        ```

        我们创建一个名为 *demo-backend* 的服务，由 `metadata: name` 字段表示。

        它的目标是任何带有 *app=demo-backend* 标签的Pod上的TCP 8080端口。

        最后，*type: ClusterIP* 表示它只在集群内部可用（因为我们这次想从我们的 *demo-frontend* 应用中调用端点，而不是像之前的例子那样直接从浏览器调用）。

    2. 后端应用程序的部署定义

        接下来，我们可以定义实际的部署了：

        ```yml
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: demo-backend
        spec:
        selector:
            matchLabels:
                app: demo-backend
        replicas: 3
        template:
            metadata:
            labels:
                app: demo-backend
            spec:
            containers:
                - name: demo-backend
                image: demo-backend:latest
                imagePullPolicy: Never
                ports:
                    - containerPort: 8080
        ```

        我们创建一个名为 *demo-backend* 的部署，由 *metadata: name* 字段表示。

        *spec: selector* 字段定义了部署如何找到要管理的Pod。在这种情况下，我们只是选择Pod模板中定义的一个标签（*app: demo-backend*）。

        我们希望有三个复制的Pod，我们用 *replicas* 字段表示。

        模板字段定义了实际的Pod：

        - 这些Pod被标记为 *app: demo-backend*
        - *template* 模板：规格 spec 字段表明，每个Pod复制运行一个容器，*demo-backend*，版本为 *latest* 的
        - 该Pod打开端口8080

    3. 后台应用程序的部署

        我们现在可以触发部署了。

        `$> kubectl create -f backend-deployment.yaml`

        让我们验证一下部署是否成功。

        `$> kubectl get deployments`

        输出看起来像这样。

        ```log
        NAME           DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
        demo-backend   3         3         3            3           25s
        ```

        我们还可以检查该服务是否可用。

        `$> kubectl get services`

        输出看起来像这样。

        ```log
        NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
        demo-backend    ClusterIP   10.102.17.114   <none>        8080/TCP         30s
        ```

        我们可以看到，该服务属于 ClusterIP 类型，它没有提供30000-32767范围内的外部端口，这与我们之前在第5节中的例子不同。

    4. 为我们的前端应用程序进行部署和服务定义

        之后，我们可以为前台定义服务和部署。

        ```yaml
        kind: Service
        apiVersion: v1
        metadata:
        name: demo-frontend
        spec:
        selector:
            app: demo-frontend
        ports:
        - protocol: TCP
            port: 8081
            nodePort: 30001
        type: NodePort
        ---
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: demo-frontend
        spec:
        selector:
            matchLabels:
                app: demo-frontend
        replicas: 3
        template:
            metadata:
            labels:
                app: demo-frontend
            spec:
            containers:
                - name: demo-frontend
                image: demo-frontend:latest
                imagePullPolicy: Never
                ports:
                    - containerPort: 8081
        ```

        前台和后台几乎都是一样的，后台和前台的唯一区别是服务的规格。

        对于前台，我们将类型定义为 *NodePort*（因为我们想让前台在集群外部可用）。后端只需要在集群内就可以到达，因此，*type* 是 *ClusterIP*。

        如前所述，我们也可以使用 *NodePort* 字段，手动指定 *nodePort* 。

    5. 部署前端应用程序

        现在我们可以用同样的方式来触发这个部署。

        `$> kubectl create -f frontend-deployment.yaml`

        让我们快速验证部署是否成功，服务是否可用。

        ```bash
        $> kubectl get deployments
        $> kubectl get services
        ```

        之后，我们可以最终调用前端应用程序的REST端点。

        `$> minikube service demo-frontend`

        这个命令将再次启动我们的默认浏览器，打开`<NodeIP>:<NodePort>`，在这个例子中是<http://192.168.99.100:30001>。

    6. 清理服务和部署

        最后，我们可以通过删除服务和部署来进行清理。

        ```bash
        $> kubectl delete service demo-frontend
        $> kubectl delete deployment demo-frontend
        $> kubectl delete service demo-backend
        $> kubectl delete deployment demo-backend
        ```

6. 总结

    在这篇文章中，我们快速了解了如何使用Minikube在本地Kubernetes集群上部署Spring Boot "Hello world" 应用程序。

    我们详细地讨论了如何。

    - 在我们的本地机器上安装Minikube
    - 开发并构建一个由两个Spring Boot应用组成的例子
    - 在一个单节点集群上部署服务，使用kubectl的命令以及配置文件。
    - 像往常一样，这些例子的完整源代码可以在GitHub上找到。
