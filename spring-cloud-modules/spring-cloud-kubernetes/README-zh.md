# Spring Cloud Kubernetes

本模块包含有关Spring Cloud Kubernetes的文章。

## 用Minikube运行Spring Boot应用程序

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

    一如既往，这些例子的完整源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-kubernetes)上找到。

## 使用Kubernetes和Spring Boot的自修复应用程序

1. 简介

    在本教程中，我们将讨论Kubernetes的[探针](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/)，并演示如何利用Actuator的[HealthIndicator](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/health/HealthIndicator.html)来准确了解我们应用程序的状态。

    在本教程中，我们将假设对[Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators)、[Kubernetes](https://www.baeldung.com/kubernetes)和[Docker](https://www.baeldung.com/dockerizing-spring-boot-application)有一些已有的经验。

2. Kubernetes探针

    Kubernetes定义了两种不同的探针，我们可以用它们来定期检查一切是否按预期工作：liveness和readiness。

    1. 有效性和就绪性

        有了Liveness和Readiness探针，[Kubelet](https://kubernetes.io/docs/reference/command-line-tools-reference/kubelet/)可以在检测到某些东西不正常时立即采取行动，并尽量减少我们应用程序的停机时间。

        两者的配置方式相同，但它们有不同的语义，Kubelet会根据哪一个被触发而执行不同的行动。

        - Readiness - Readiness验证我们的Pod是否准备好开始接收流量。当所有的容器都准备好时，我们的Pod就准备好了。
        - Liveness - 与Readiness相反，Liveness检查我们的Pod是否应该被重新启动。它可以发现我们的应用程序正在运行，但处于无法取得进展的状态，例如，它处于死锁状态的用例。

        我们在容器级别配置这两种探测类型。

        ```yml
        apiVersion: v1
        kind: Pod
        metadata:
        name: goproxy
        labels:
            app: goproxy
        spec:
        containers:
        - name: goproxy
            image: k8s.gcr.io/goproxy:0.1
            ports:
            - containerPort: 8080
            readinessProbe:
            tcpSocket:
                port: 8080
            initialDelaySeconds: 5
            periodSeconds: 10
            timeoutSeconds: 2
            failureThreshold: 1
            successThreshold: 1
            livenessProbe:
            tcpSocket:
                port: 8080
            initialDelaySeconds: 15
            periodSeconds: 20
            timeoutSeconds: 2
            failureThreshold: 1
            successThreshold: 1
        ```

        我们可以配置一些字段，以便更精确地控制我们的探测行为。

        - initialDelaySeconds - 创建容器后，在启动探针前等待n秒
        - periodSeconds - 这个探测应该多长时间运行一次，默认为10秒；最小为1秒
        - timeoutSeconds - 在探针超时前我们要等待多长时间，默认为1秒；最小也是1秒
        - failureThreshold - 在放弃之前尝试n次。在就绪的情况下，我们的Pod将被标记为未就绪，而在有效性的情况下放弃意味着重新启动Pod。这里的默认值是3次失败，最小是1次
        - successThreshold - 这是探针在失败后被认为成功的最小连续成功次数。它的默认值是1次成功，最小值也是1。

        在这种情况下，我们选择了tcp探针，但是，我们也可以使用其他类型的探针。

    2. 探针类型

        根据我们的使用情况，一种探针类型可能被证明比其他类型更有用。例如，如果我们的容器是一个网络服务器，使用http探测可能比tcp探测更可靠。

        幸运的是，Kubernetes有三种不同类型的探针，我们可以使用：

        - exec - 在我们的容器中执行bash指令。例如，检查一个特定的文件是否存在。如果该指令返回一个失败代码，探针就会失败。
        - tcpSocket - 试图建立一个与容器的tcp连接，使用指定的端口。如果它不能建立连接，探测就会失败。
        - httpGet - 向运行在容器中并在指定端口上监听的服务器发送一个HTTP GET请求。任何大于或等于200且小于400的代码都表示成功。

        值得注意的是，除了我们前面提到的那些，HTTP探针还有其他字段。

        - host - 要连接的主机名，默认为我们的pod的IP
        - scheme - 连接时应使用的方案，HTTP或HTTPS，默认为HTTP
        - path - 在网络服务器上访问的路径
        - httpHeaders - 在请求中设置的自定义头信息
        - port - 容器中要访问的端口的名称或编号

3. Spring Actuator和Kubernetes的自愈能力

    现在我们对Kubernetes如何检测我们的应用程序是否处于故障状态有了大致的了解，让我们看看如何利用Spring的Actuator来密切关注我们的应用程序，以及它的依赖关系

    为了这些例子的目的，我们将依靠Minikube。

    1. 执行器及其健康指示器

        考虑到Spring有许多HealthIndicators可供使用，通过Kubernetes的探针反映我们应用程序的一些依赖关系的状态，就像在我们的pom.xml中添加[Actuator](https://www.baeldung.com/spring-boot-actuators)依赖一样简单。

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        ```

    2. Liveness实例

        让我们从一个应用程序开始，它将正常启动，并在30秒后过渡到一个破碎状态。

        我们将通过创建一个验证布尔变量是否为真的[HealthIndicator](https://www.baeldung.com/spring-boot-actuators)来模拟一个破碎状态。我们将初始化该变量为真，然后安排一个任务，在30秒后将其改为假。

        ```java
        @Component
        public class CustomHealthIndicator implements HealthIndicator {

            private boolean isHealthy = true;

            public CustomHealthIndicator() {
                ScheduledExecutorService scheduled =
                Executors.newSingleThreadScheduledExecutor();
                scheduled.schedule(() -> {
                    isHealthy = false;
                }, 30, TimeUnit.SECONDS);
            }

            @Override
            public Health health() {
                return isHealthy ? Health.up().build() : Health.down().build();
            }
        }
        ```

        有了我们的HealthIndicator，我们需要对我们的应用程序进行dockerize。

        ```log
        FROM openjdk:8-jdk-alpine
        RUN mkdir -p /usr/opt/service
        COPY target/*.jar /usr/opt/service/service.jar
        EXPOSE 8080
        ENTRYPOINT exec java -jar /usr/opt/service/service.jar
        ```

        接下来，我们创建我们的Kubernetes模板。

        ```yml
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: liveness-example
        spec:
        ...
            spec:
            containers:
            - name: liveness-example
                image: dbdock/liveness-example:1.0.0
                ...
                readinessProbe:
                httpGet:
                    path: /health
                    port: 8080
                initialDelaySeconds: 10
                timeoutSeconds: 2
                periodSeconds: 3
                failureThreshold: 1
                livenessProbe:
                httpGet:
                    path: /health
                    port: 8080
                initialDelaySeconds: 20
                timeoutSeconds: 2
                periodSeconds: 8
                failureThreshold: 1
        ```

        我们正在使用一个httpGet探针，指向Actuator的健康端点。我们的应用程序状态（及其依赖关系）的任何变化都将反映在我们的部署的健康性上。

        将我们的应用程序部署到Kubernetes后，我们将能够看到这两个探针的作用：大约30秒后，我们的Pod将被标记为未准备好，并从旋转中移除；几秒钟后，Pod被重新启动。

        我们可以看到我们的Pod在执行kubectl describe pod liveness-example时的事件。

        ```log
        Warning  Unhealthy 3s (x2 over 7s)   kubelet, minikube  Readiness probe failed: HTTP probe failed ...
        Warning  Unhealthy 1s                kubelet, minikube  Liveness probe failed: HTTP probe failed ...
        Normal   Killing   0s                kubelet, minikube  Killing container with id ...
        ```

    3. Readiness示例

        在前面的例子中，我们看到了如何使用HealthIndicator来反映我们的应用程序在Kubernetes部署中的健康状态。

        让我们在一个不同的用例中使用它：假设我们的应用程序在能够接收流量之前需要一些时间。例如，它需要将一个文件加载到内存中并验证其内容。

        这是一个很好的例子，说明我们可以利用准备就绪探针的优势。

        让我们修改前面例子中的HealthIndicator和Kubernetes模板，使其适应这个用例。

        ```java
        @Component
        public class CustomHealthIndicator implements HealthIndicator {

            private boolean isHealthy = false;

            public CustomHealthIndicator() {
                ScheduledExecutorService scheduled =
                Executors.newSingleThreadScheduledExecutor();
                scheduled.schedule(() -> {
                    isHealthy = true;
                }, 40, TimeUnit.SECONDS);
            }

            @Override
            public Health health() {
                return isHealthy ? Health.up().build() : Health.down().build();
            }
        }
        ```

        我们将该变量初始化为false，40秒后，将执行一个任务并将其设置为true。

        接下来，我们使用以下模板对我们的应用程序进行dockerize和部署。

        ```yml
        apiVersion: apps/v1
        kind: Deployment
        metadata:
        name: readiness-example
        spec:
        ...
            spec:
            containers:
            - name: readiness-example
                image: dbdock/readiness-example:1.0.0
                ...
                readinessProbe:
                httpGet:
                    path: /health
                    port: 8080
                initialDelaySeconds: 40
                timeoutSeconds: 2
                periodSeconds: 3
                failureThreshold: 2
                livenessProbe:
                httpGet:
                    path: /health
                    port: 8080
                initialDelaySeconds: 100
                timeoutSeconds: 2
                periodSeconds: 8
                failureThreshold: 1
        ```

        虽然相似，但我们需要指出探针配置中的一些变化。

        - 由于我们知道我们的应用程序需要40秒左右的时间来准备接收流量，所以我们将就绪性探测器的initialDelaySeconds增加到40秒
        - 同样地，我们将有效性探针的initialDelaySeconds增加到100秒，以避免被Kubernetes过早地杀死。

        如果40秒后仍未完成，它仍有60秒左右的时间完成。在那之后，我们的失效探针将启动并重新启动Pod。

4. 总结

    在这篇文章中，我们谈到了Kubernetes探针，以及我们如何使用Spring的Actuator来改善我们应用程序的健康监测。

    这些例子的完整实现可以在[Github](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-kubernetes)上找到。
