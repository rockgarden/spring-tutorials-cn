# [使用Kubernetes和Spring Boot的自修复应用程序](https://www.baeldung.com/spring-boot-kubernetes-self-healing-apps)

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
