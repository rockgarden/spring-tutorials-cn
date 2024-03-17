# Spring Cloud Kubernetes指南

1. 概述

    当我们构建一个微服务解决方案时，[Spring Cloud](https://www.baeldung.com/spring-cloud-series)和[Kubernetes](https://www.baeldung.com/kubernetes)都是最佳解决方案，因为它们提供了解决最常见挑战的组件。然而，如果我们决定选择Kubernetes作为我们解决方案的主要容器管理器和部署平台，我们仍然可以主要通过[Spring Cloud Kubernetes](https://cloud.spring.io/spring-cloud-static/spring-cloud-kubernetes/2.1.0.RC1/single/spring-cloud-kubernetes.html)项目来使用Spring Cloud的有趣功能。

    这个相对较新的项目无疑为[Spring Boot](https://www.baeldung.com/spring-boot-start)应用提供了与Kubernetes的轻松集成。

    在本教程中，我们将

    - 在我们的本地机器上安装Minikube
    - 开发一个微服务架构的例子，有两个独立的Spring Boot应用程序通过REST进行通信
    - 使用Minikube在一个单节点集群上设置应用程序
    - 使用YAML配置文件部署该应用程序

2. 场景

    在我们的例子中，我们使用的场景是旅行社向客户提供各种交易，客户会不时地查询旅行社服务。我们将用它来演示。

    - 通过Spring Cloud Kubernetes发现服务
    - 配置管理以及使用Spring Cloud Kubernetes Config注入Kubernetes ConfigMaps和secrets到应用pod中
    - 使用Spring Cloud Kubernetes Ribbon进行负载均衡

3. 环境设置

    首先，我们需要在本地机器上安装Minikube，最好是虚拟机驱动，如[VirtualBox](https://www.virtualbox.org/)。另外，建议在进行这个环境设置之前，先看看Kubernetes及其主要功能。

    让我们启动本地单节点[Kubernetes](https://kubernetes.io/)集群。

    `minikube start --vm-driver=virtualbox`

    该命令创建了一个虚拟机，使用VirtualBox驱动运行Minikube集群。kubectl中的默认上下文现在将是minikube。然而，为了能够在上下文之间切换，我们使用。

    `kubectl config use-context minikube`

    启动Minikube后，我们可以连接到Kubernetes仪表板，以访问日志并轻松监控我们的服务、pod、ConfigMaps和Secrets。

    `minikube dashboard`

    1. 部署

        首先，让我们从GitHub获取我们的例子。

        这时，我们可以从父文件夹中运行 "deployment-travel-client.sh" 脚本，或者逐一执行每条指令，以便很好地掌握程序。

        ```bash
        ### build the repository
        mvn clean install

        ### set docker env
        eval $(minikube docker-env)

        ### build the docker images on minikube
        cd travel-agency-service
        docker build -t travel-agency-service .
        cd ../client-service
        docker build -t client-service .
        cd ..

        ### secret and mongodb
        kubectl delete -f travel-agency-service/secret.yaml
        kubectl delete -f travel-agency-service/mongo-deployment.yaml

        kubectl create -f travel-agency-service/secret.yaml
        kubectl create -f travel-agency-service/mongo-deployment.yaml

        ### travel-agency-service
        kubectl delete -f travel-agency-service/travel-agency-deployment.yaml
        kubectl create -f travel-agency-service/travel-agency-deployment.yaml

        ### client-service
        kubectl delete configmap client-service
        kubectl delete -f client-service/client-service-deployment.yaml

        kubectl create -f client-service/client-config.yaml
        kubectl create -f client-service/client-service-deployment.yaml

        # Check that the pods are running
        kubectl get pods
        ```

4. 服务发现

    这个项目为我们提供了Kubernetes中ServiceDiscovery接口的实现。在微服务环境中，通常有多个pod在运行同一个服务。Kubernetes将服务暴露为一个端点集合，可以从运行在同一Kubernetes集群的pod中的Spring Boot应用中获取和到达。

    例如，在我们的例子中，我们有多个旅行社服务的副本，可以从我们的客户端服务访问<http://travel-agency-service:8080>。然而，这在内部会转化为访问不同的pod，如travel-agency-service-7c9cfff655-4hxnp。

    Spring Cloud Kubernetes Ribbon使用这一功能在服务的不同端点之间进行负载平衡。

    我们可以通过在客户端应用程序中添加[spring-cloud-starter-kubernetes](https://search.maven.org/search?q=g:org.springframework.cloud%20a:spring-cloud-starter-kubernetes)依赖项来轻松使用服务发现。

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-kubernetes</artifactId>
    </dependency>
    ```

    此外，我们应该添加@EnableDiscoveryClient，并通过在我们的类中使用@Autowired将DiscoveryClient注入到ClientController中。

    ```java
    @SpringBootApplication
    @EnableDiscoveryClient
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }

    @RestController
    public class ClientController {
        @Autowired
        private DiscoveryClient discoveryClient;
    }
    ```

5. ConfigMaps

    通常，微服务需要某种配置管理。例如，在SpringCloud应用程序中，我们将使用SpringCloudConfigServer。

    然而，我们可以通过使用Kubernetes提供的[ConfigMaps](https://kubernetes.io/docs/tasks/configure-pod-container/configure-pod-configmap/)来实现这一点，前提是我们打算将其仅用于非敏感、未加密的信息。或者，如果我们想要共享的信息是敏感的，那么我们应该选择使用[机密](https://kubernetes.io/docs/concepts/configuration/secret/)。

    在我们的示例中，我们在client-service Spring Boot应用程序上使用ConfigMaps。让我们创建一个client-config.yaml文件来定义client-service的ConfigMap：

    ```yml
    apiVersion: v1 by d
    kind: ConfigMap
    metadata:
    name: client-service
    data:
    application.properties: |-
        bean.message=Testing reload! Message from backend is: %s <br/> Services : %s
    ```

    ConfigMap的名称必须与“application.properties”文件中指定的应用程序名称匹配，这一点很重要。在这种情况下，它是客户端服务。接下来，我们应该为Kubernetes上的client-service创建ConfigMap：

    `kubectl create -f client-config.yaml`

    现在，让我们使用@configuration和@ConfigurationProperties创建一个配置类ClientConfig，并将其注入ClientController：

    ```java
    @Configuration
    @ConfigurationProperties(prefix = "bean")
    public class ClientConfig {
        private String message = "Message from backend is: %s <br/> Services : %s";
        // getters and setters
    }

    @RestController
    public class ClientController {
        @Autowired
        private ClientConfig config;
        @GetMapping
        public String load() {
            return String.format(config.getMessage(), "", "");
        }
    }
    ```

    如果我们没有指定ConfigMap，那么我们应该看到默认消息，该消息在类中设置。但是，当我们创建ConfigMap时，该属性将覆盖此默认消息。

    此外，每次我们决定更新ConfigMap时，页面上的消息都会相应地发生变化：

    `kubectl edit configmap client-service`

6. 秘密

    让我们通过看看我们的例子中MongoDB连接设置的规范来看看[Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)是如何工作的。我们将在Kubernetes上创建环境变量，然后将其注入Spring Boot应用程序中。

    1. 创建一个Secret

        第一步是创建一个secret.yaml文件，将用户名和密码编码为Base 64。

        ```yml
        apiVersion: v1
        kind: Secret
        metadata:
        name: db-secret
        data:
        username: dXNlcg==
        password: cDQ1NXcwcmQ=
        ```

        让我们在Kubernetes集群上应用Secret配置。

        `kubectl apply -f secret.yaml`

    2. 创建一个MongoDB服务

        我们现在应该创建MongoDB服务和部署Travel-agency-deployment.yaml文件。特别是，在部署部分，我们将使用之前定义的Secret用户名和密码。

        ```yml
        apiVersion: extensions/v1beta1
        kind: Deployment
        metadata:
        name: mongo
        spec:
        replicas: 1
        template:
            metadata:
            labels:
                service: mongo
            name: mongodb-service
            spec:
            containers:
            - args:
                - mongod
                - --smallfiles
                image: mongo:latest
                name: mongo
                env:
                - name: MONGO_INITDB_ROOT_USERNAME
                    valueFrom:
                    secretKeyRef:
                        name: db-secret
                        key: username
                - name: MONGO_INITDB_ROOT_PASSWORD
                    valueFrom:
                    secretKeyRef:
                        name: db-secret
                        key: password
        ```

        默认情况下，mongo:latest镜像将在名为admin的数据库上创建一个具有用户名和密码的用户。

    3. 在Travel Agency Service上设置MongoDB

        更新应用程序属性以添加数据库相关信息是很重要的。虽然我们可以自由指定数据库名称admin，但在这里我们要隐藏最敏感的信息，如用户名和密码。

        ```properties
        spring.cloud.kubernetes.reload.enabled=true
        spring.cloud.kubernetes.secrets.name=db-secret
        spring.data.mongodb.host=mongodb-service
        spring.data.mongodb.port=27017
        spring.data.mongodb.database=admin
        spring.data.mongodb.username=${MONGO_USERNAME}
        spring.data.mongodb.password=${MONGO_PASSWORD}
        ```

        现在，让我们看看我们的travel-agency-deployment属性文件，用连接到mongodb服务所需的用户名和密码信息来更新服务和部署。

        下面是文件的相关部分，其中与MongoDB连接有关的部分。

        ```yml
        env:
        - name: MONGO_USERNAME
            valueFrom:
            secretKeyRef:
                name: db-secret
                key: username
        - name: MONGO_PASSWORD
            valueFrom:
            secretKeyRef:
                name: db-secret
                key: password
        ```

7. 与Ribbon的通信

    在微服务环境中，我们通常需要我们的服务被复制的pod的列表，以执行负载平衡。这可以通过使用Spring Cloud Kubernetes Ribbon提供的机制来实现。该机制可以自动发现并到达特定服务的所有端点，随后，它将端点的信息填充到Ribbon ServerList中。

    让我们先把[spring-cloud-starter-kubernetes-ribbon](https://search.maven.org/search?q=g:org.springframework.cloud%20a:spring-cloud-starter-kubernetes-ribbon)依赖性添加到我们的客户端服务pom.xml文件中。

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-kubernetes-ribbon</artifactId>
    </dependency>
    ```

    下一步是将注解@RibbonClient添加到我们的 client-service 应用程序。

    `@RibbonClient(name = "travel-agency-service")`

    当端点列表被填充后，Kubernetes客户端将搜索生活在当前命名空间/项目中的注册端点，这些端点与使用@RibbonClient注解定义的服务名称相匹配。

    我们还需要在应用程序属性中启用ribbon客户端。

    `ribbon.http.client.enabled=true`

8. 附加功能

    1. Hystrix

        [Hystrix](https://www.baeldung.com/introduction-to-hystrix) 有助于建立一个容错和弹性的应用程序。它的主要目的是快速故障和快速恢复。

        特别是在我们的例子中，我们通过用@EnableCircuitBreaker注释Spring Boot应用类，使用Hystrix在 client-server 上实现断路器(circuit breaker)模式。

        此外，我们通过在TravelAgencyService.getDeals()方法上注解@HystrixCommand()来使用回退功能。这意味着在回退的情况下，getFallBackName()将被调用并返回 "Fallback" 信息。

        ```java
        @HystrixCommand(fallbackMethod = "getFallbackName", commandProperties = { 
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") })
        public String getDeals() {
            return this.restTemplate.getForObject("http://travel-agency-service:8080/deals", String.class);
        }

        private String getFallbackName() {
            return "Fallback";
        }
        ```

    2. Pod健康指示器(Pod Health Indicator)

        我们可以利用Spring Boot HealthIndicator和[Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators)来向用户公开健康相关信息。

        特别是，Kubernetes健康指标提供了：

        - pod name
        - IP地址
        - 名称空间
        - 服务账户
        - 节点名称
        - 表示Spring Boot应用程序是在Kubernetes内部还是外部的一个标志

9. 总结

    在本文中，我们将全面介绍 Spring Cloud Kubernetes 项目。

    那么，我们为什么要使用它呢？如果我们既喜欢 Kubernetes 作为微服务平台，又欣赏 Spring Cloud 的功能，那么 Spring Cloud Kubernetes 就能让我们两全其美。

    该示例的完整源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-kubernetes) 上获取。

10. 其它

    官方文档：<https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/>

    官方示例代码：<https://github.com/spring-cloud/spring-cloud-kubernetes>
