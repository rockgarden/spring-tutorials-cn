# Kubernetes Java API示例代码

本模块包含用于演示如何使用Kubernetes客户端Java API的示例代码。

在运行这些示例之前，请确保您的环境已正确配置为可以访问一个正在运行的Kubernetes集群。

检查一切是否按预期工作的一种简单方法是发出任何*kubectl get*命令：

```shell
kubectl get nodes
```

如果你得到了有效的回应，那么你就可以出发了。

## Kubernetes Java客户端的快速介绍

1. 简介

    在本教程中，我们将展示如何使用Kubernetes官方客户端库从Java应用程序中使用Kubernetes API。

2. 为什么使用Kubernetes API？

    如今，可以说Kubernetes已经成为管理容器化应用程序的事实标准。它提供了丰富的API，使我们能够部署、扩展和监控应用程序和相关资源，如存储、秘密和环境变量。事实上，有一种方法可以认为这个API是常规操作系统中可用的系统调用的分布式模拟。

    大多数时候，我们的应用程序可以忽略他们正在Kubernetes下运行的事实。这是一件好事，因为它允许我们在本地开发它们，并通过一些命令和YAML咒语，快速将它们部署到多个云供应商，只需稍作改动。

    然而，有一些有趣的用例，我们需要与Kubernetes API对话，以实现特定的功能：

    - 启动一个外部程序来执行某些任务，并在之后检索其完成情况
    - 根据客户的要求，动态地创建/修改一些服务
    - 为在多个Kubernetes集群中运行的解决方案创建一个自定义的监控仪表盘，甚至是跨云供应商的监控仪表盘。
    当然，这些用例并不常见，但由于它的API，我们会发现它们是很容易实现的。

    此外，由于Kubernetes的API是一个开放的规范，我们可以很有信心，我们的代码将在没有任何修改的情况下运行在任何认证的实现上。

3. 本地开发环境

    在我们继续创建一个应用程序之前，我们需要做的第一件事是获得一个正常运行的Kubernetes集群。虽然我们可以使用公共云提供商，但本地环境通常可以对其设置的所有方面提供更多控制。

    有几个轻量级的发行版适合这项任务：

    - K3S
    - Minikube
    - Kind
    实际的设置步骤超出了本文的范围，但是，无论你选择什么方案，只要在开始任何开发之前确保[kubectl](https://kubernetes.io/docs/reference/kubectl/overview/)运行正常即可。

4. Maven的依赖性

    首先，让我们在项目的pom.xml中添加Kubernetes的Java API依赖项：

    ```xml
    <dependency>
        <groupId>io.kubernetes</groupId>
        <artifactId>client-java</artifactId>
        <version>11.0.0</version>
    </dependency>
    ```

    client-java的最新版本可以从Maven Central下载。

5. 你好，Kubernetes

    现在，让我们创建一个非常简单的Kubernetes应用程序，它将列出可用的节点，以及关于它们的一些信息。

    尽管它很简单，但这个应用程序说明了我们必须经历的必要步骤，以连接到一个正在运行的集群并执行API调用。无论我们在实际应用中使用哪种API，这些步骤都是一样的。

    1. ApiClient初始化

        ApiClient类是API中最重要的类之一，因为它包含了对Kubernetes API服务器进行调用的所有逻辑。创建该类实例的推荐方法是使用配置类中的一个可用静态方法。特别是，最简单的方法是使用defaultClient()方法：

        `ApiClient client = Config.defaultClient();`

        使用这个方法可以确保我们的代码在远程和集群场景中都能工作。此外，它将自动遵循kubectl工具所使用的相同步骤来定位配置文件：

        - 配置文件由KUBECONFIG环境变量定义
        - $HOME/.kube/config文件
        - /var/run/secrets/kubernetes.io/serviceaccount下的服务账户令牌
        - 直接访问<http://localhost:8080>
        第三步是使我们的应用程序有可能作为任何pod的一部分在集群内运行，只要向它提供适当的服务账户就可以了。

        另外，请注意，如果我们在配置文件中定义了多个上下文，这个过程将选择 "current" 的上下文，这是用kubectl config set-context命令定义的。

    2. 创建一个API存根

        一旦我们掌握了一个ApiClient实例，我们就可以用它来为任何可用的API创建一个存根。在我们的例子中，我们将使用CoreV1Api类，它包含我们需要的列出可用节点的方法：

        `CoreV1Api api = new CoreV1Api(client);`

        这里，我们使用已经存在的ApiClient来创建API存根。

        注意到还有一个no-args构造函数可用，但一般来说，我们应该避免使用它。不使用它的理由是，在内部，它将使用一个全局的ApiClient，这个ApiClient必须事先通过Configuration.setDefaultApiClient()设置。这就造成了对某人在使用存根前调用该方法的隐性依赖，而这又可能导致运行时错误和维护问题。

        一个更好的方法是使用任何依赖注入框架来完成这个初始接线，在任何需要的地方注入产生的存根。

    3. 调用Kubernetes API

        最后，让我们进入实际的API调用，返回可用节点。CoreApiV1存根有一个方法正是这样做的，所以这变得微不足道：

        ```java
        V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, 10, false);
        nodeList.getItems()
        .stream()
        .forEach((node) -> System.out.println(node));
        ```

        在我们的例子中，我们为该方法的大多数参数传递了null，因为它们是可选的。最后两个参数与所有 listXXX 调用有关，因为它们指定了调用超时和是否是一个观察调用。检查该方法的签名可以发现其余的参数：

        ```java
        public V1NodeList listNode(
        String pretty,
        Boolean allowWatchBookmarks,
        String _continue,
        String fieldSelector,
        String labelSelector,
        Integer limit,
        String resourceVersion,
        String resourceVersionMatch,
        Integer timeoutSeconds,
        Boolean watch) {
            // ... method implementation
        }
        ```

        在这个快速介绍中，我们将忽略分页、观察和过滤参数。在这种情况下，返回值是一个POJO，包含返回文档的Java表示。对于这个API调用，文档包含一个V1Node对象的列表，其中有关于每个节点的几条信息。下面是这段代码在控制台产生的典型输出：

        ```java
        class V1Node {
            metadata: class V1ObjectMeta {
                labels: {
                    beta.kubernetes.io/arch=amd64,
                    beta.kubernetes.io/instance-type=k3s,
                    // ... other labels omitted
                }
                name: rancher-template
                resourceVersion: 29218
                selfLink: null
                uid: ac21e09b-e3be-49c3-9e3a-a9567b5c2836
            }
            // ... many fields omitted
            status: class V1NodeStatus {
                addresses: [class V1NodeAddress {
                    address: 192.168.71.134
                    type: InternalIP
                }, class V1NodeAddress {
                    address: rancher-template
                    type: Hostname
                }]
                allocatable: {
                    cpu=Quantity{number=1, format=DECIMAL_SI},
                    ephemeral-storage=Quantity{number=18945365592, format=DECIMAL_SI},
                    hugepages-1Gi=Quantity{number=0, format=DECIMAL_SI},
                    hugepages-2Mi=Quantity{number=0, format=DECIMAL_SI},
                    memory=Quantity{number=8340054016, format=BINARY_SI}, 
                    pods=Quantity{number=110, format=DECIMAL_SI}
                }
                capacity: {
                    cpu=Quantity{number=1, format=DECIMAL_SI},
                    ephemeral-storage=Quantity{number=19942490112, format=BINARY_SI}, 
                    hugepages-1Gi=Quantity{number=0, format=DECIMAL_SI}, 
                    hugepages-2Mi=Quantity{number=0, format=DECIMAL_SI}, 
                    memory=Quantity{number=8340054016, format=BINARY_SI}, 
                    pods=Quantity{number=110, format=DECIMAL_SI}}
                conditions: [
                    // ... node conditions omitted
                ]
                nodeInfo: class V1NodeSystemInfo {
                    architecture: amd64
                    kernelVersion: 4.15.0-135-generic
                    kubeProxyVersion: v1.20.2+k3s1
                    kubeletVersion: v1.20.2+k3s1
                    operatingSystem: linux
                    osImage: Ubuntu 18.04.5 LTS
                    // ... more fields omitted
                }
            }
        }
        ```

        我们可以看到，有相当多的信息可用。作为比较，这是默认设置下的等效kubectl输出：

        ```bash
        root@rancher-template:~# kubectl get nodes
        NAME               STATUS   ROLES                  AGE   VERSION
        rancher-template   Ready    control-plane,master   24h   v1.20.2+k3s1
        ```

6. 总结

    在这篇文章中，我们对Kubernetes的Java API做了一个快速的介绍。在未来的文章中，我们将深入挖掘这个API，并探索它的一些额外功能：

    - 解释可用的API调用变体之间的区别
    - 使用Watch来实时监控集群事件
    - 如何使用分页来有效地从集群中检索大量的数据

## 在Kubernetes API中使用Watch

1. 简介

    在本教程中，我们将继续探索Java Kubernetes API。这一次，我们将展示如何使用Watch来有效监控集群事件。

2. 什么是Kubernetes手表？

    在我们之前关于[Kubernetes API](https://www.baeldung.com/kubernetes-java-client)的文章中，我们展示了如何恢复关于一个给定资源或其集合的信息。如果我们只想获得这些资源在某个时间点上的状态，这是好的。然而，鉴于Kubernetes集群的性质是高度动态的，这通常是不够的。

    大多数情况下，我们还想监控这些资源，并在它们发生时跟踪事件。例如，我们可能对跟踪pod生命周期事件或部署状态变化感兴趣。虽然我们可以使用轮询，但这种方法会受到一些限制。首先，随着需要监控的资源数量的增加，它的规模不会很大。其次，我们有可能丢失在轮询周期之间偶然发生的事件。

    为了解决这些问题，Kubernetes有一个 "watch" 的概念，所有的资源收集API调用都可以通过观察查询参数来实现。当它的值为false或省略时，GET操作的行为与平常一样：服务器处理请求并返回符合给定条件的资源实例列表。然而，传递watch=true会极大地改变其行为：

    - 响应现在由一系列的修改事件组成，包含修改的类型和受影响的对象
    - 在发送最初的一批事件后，连接将保持开放，使用一种称为长轮询的技术。
3. 创建一个监视

    Java Kubernetes API通过Watch类支持Watch，它有一个静态方法：createWatch。这个方法需要三个参数：

    - 一个[ApiClient](https://www.baeldung.com/kubernetes-java-client#1-apiclient-initialization)，它处理对Kubernetes API服务器的实际REST调用
    - 一个描述要观察的资源集合的调用实例
    - 一个带有预期资源类型的TypeToken
    我们使用库中的任何xxxApi类的listXXXCall()方法来创建一个Call实例。例如，要创建一个检测Pod事件的Watch，我们会使用listPodForAllNamespacesCall()：

    ```java
    CoreV1Api api = new CoreV1Api(client);
    Call call = api.listPodForAllNamespacesCall(null, null, null, null, null, null, null, null, 10, true, null);
    Watch<V1Pod> watch = Watch.createWatch(
    client, 
    call, 
    new TypeToken<Response<V1Pod>>(){}.getType()));
    ```

    这里，我们对大多数参数使用null，意思是 "使用默认值"，只有两个例外：timeout和watch。后者必须被设置为 "true"，以便进行观察调用。否则，这将是一个普通(regular)的休息调用。在这种情况下，超时作为观察的 "time-to-live"，意味着一旦超时，服务器将停止发送事件并终止连接。

    为超时参数（以秒为单位）找到一个好的值，需要一些尝试和错误，因为它取决于客户应用程序的确切要求。另外，检查你的Kubernetes集群配置也很重要。通常情况下，watch有5分钟的硬性限制，所以超过这个时间的传递将不会有预期的效果。

4. 接收事件

    仔细看看Watch类，我们可以看到它同时实现了标准JRE中的Iterator和Iterable，所以我们可以在for-each或hasNext()-next()循环中使用从createWatch()返回的值：

    ```java
    for (Response<V1Pod> event : watch) {
        V1Pod pod = event.object;
        V1ObjectMeta meta = pod.getMetadata();
        switch (event.type) {
        case "ADDED":
        case "MODIFIED":
        case "DELETED":
            // ... process pod data
            break;
        default:
            log.warn("Unknown event type: {}", event.type);
        }
    }
    ```

    每个事件的类型字段告诉我们该对象发生了什么类型的事件--在我们的例子中是一个Pod。一旦我们消耗了所有的事件，我们必须重新调用Watch.createWatch()以再次开始接收事件。在示例代码中，我们将Watch的创建和结果处理放在一个while循环中。其他方法也是可行的，比如使用ExecutorService或类似的方法在后台接收更新。

5. 使用资源版本和书签

    上述代码的一个问题是，每次我们创建一个新的Watch时，都会有一个初始事件流，其中包含所有给定种类的现有资源实例。这是因为服务器认为我们之前没有任何关于它们的信息，所以它只是将它们全部发送。

    然而，这样做违背了有效处理事件的目的，因为我们只需要在初始加载后的新事件。为了防止再次接收所有数据，观察机制支持两个额外的概念：资源版本和书签(resource versions and bookmarks.)。

    1. 资源版本

        Kubernetes中的每个资源在其元数据中都包含一个资源版本字段，这只是一个不透明的字符串，每次有变化时，服务器都会设置。此外，由于资源集合也是一种资源，所以也有一个与之相关的资源版本。当新的资源被添加、移除和/或从一个集合中修改时，这个字段将相应地改变。

        当我们进行一个返回集合的API调用，并包括resourceVersion参数时，服务器将使用其值作为查询的 "starting point"。对于观察API调用，这意味着只有在创建通知版本的时间之后发生的事件将被包括在内。

        但是，我们如何获得一个资源版本以包括在我们的调用中？很简单：我们只需做一个初始的同步调用来检索初始的资源列表，其中包括集合的 resourceVersion，然后在后续的Watch调用中使用它：

        ```java
        String resourceVersion = null;
        while (true) {
            if (resourceVersion == null) {
                V1PodList podList = api.listPodForAllNamespaces(null, null, null, null, null, "false",
                resourceVersion, null, 10, null);
                resourceVersion = podList.getMetadata().getResourceVersion();
            }
            try (Watch<V1Pod> watch = Watch.createWatch(
            client,
            api.listPodForAllNamespacesCall(null, null, null, null, null, "false",
                resourceVersion, null, 10, true, null),
            new TypeToken<Response<V1Pod>>(){}.getType())) {
                
                for (Response<V1Pod> event : watch) {
                    // ... process events
                }
            } catch (ApiException ex) {
                if (ex.getCode() == 504 || ex.getCode() == 410) {
                    resourceVersion = extractResourceVersionFromException(ex);
                }
                else {
                    resourceVersion = null;
                }
            }
        }
        ```

        异常处理代码，在这种情况下，是相当重要的。当由于某种原因，请求的资源版本不存在时，Kubernetes服务器将返回504或410错误代码。在这种情况下，返回的信息通常包含当前的版本。不幸的是，这个信息并不是以任何结构化的方式出现的，而是作为错误信息本身的一部分。

        提取代码（又称丑陋的黑客 a.k.a. ugly hack）使用正则表达式来实现这一意图，但由于错误信息往往与实现有关，所以代码会退回到一个空值。通过这样做，主循环回到它的起点，用新的资源版本恢复一个新的列表，重新开始观察操作。

        不管怎么说，即使有这个注意事项，关键是现在事件列表不会在每次观察时从头开始。

    2. 书签

        书签是一个可选的功能，它在从Watch调用返回的事件流上启用一个特殊的BOOKMARK事件。该事件在其元数据中包含一个资源版本值，我们可以在随后的Watch调用中作为一个新的起点使用。

        由于这是一个选择加入的功能，我们必须通过在API调用中传递true以允许WatchBookmarks来明确启用它。该选项仅在创建Watch时有效，否则将被忽略。另外，服务器可能会完全忽略它，所以客户端根本不应该依赖接收这些事件。

        与之前单独使用资源版本的方法相比，书签允许我们在很大程度上摆脱了昂贵的同步调用：

        ```java
        String resourceVersion = null;

        while (true) {
            // Get a fresh list whenever we need to resync
            if (resourceVersion == null) {
                V1PodList podList = api.listPodForAllNamespaces(true, null, null, null, null,
                "false", resourceVersion, null, null, null);
                resourceVersion = podList.getMetadata().getResourceVersion();
            }

            while (true) {
                try (Watch<V1Pod> watch = Watch.createWatch(
                client,
                api.listPodForAllNamespacesCall(true, null, null, null, null, 
                    "false", resourceVersion, null, 10, true, null),
                new TypeToken<Response<V1Pod>>(){}.getType())) {
                    for (Response<V1Pod> event : watch) {
                        V1Pod pod = event.object;
                        V1ObjectMeta meta = pod.getMetadata();
                        switch (event.type) {
                            case "BOOKMARK":
                                resourceVersion = meta.getResourceVersion();
                                break;
                            case "ADDED":
                            case "MODIFIED":
                            case "DELETED":
                                // ... event processing omitted
                                break;
                            default:
                                log.warn("Unknown event type: {}", event.type);
                        }
                    }
                }
                } catch (ApiException ex) {
                    resourceVersion = null;
                    break;
                }
            }
        ```

        在这里，我们只需要在第一遍和在内循环中获得ApiException时获得完整的列表。注意，BOOKMARK事件与其他事件具有相同的对象类型，所以我们在这里不需要任何特殊的转换。然而，我们关心的唯一字段是resourceVersion，我们将其保存到下一个Watch调用。

6. 总结

    在这篇文章中，我们已经介绍了使用Java API客户端创建Kubernetes手表的不同方法。

## 在Kubernetes Java API中使用命名空间和选择器

1. 简介

    在本教程中，我们将探索使用Kubernetes Java API过滤资源的不同方法。

    在之前的文章中，我们重点介绍了查询、操作和监控集群资源的可用方法，包括Kubernetes Java API。

    这些例子假设我们在寻找特定种类的资源或针对单一资源。然而，在实践中，大多数应用程序需要一种方法来根据一些标准定位资源。

    Kubernetes的API支持三种方式来限制这些搜索的范围：

    - Namespaces：范围限于特定的Kubernetes[命名空间](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)
    - Field Selectors：范围限于具有匹配字段值的资源
    - Label Selectors：范围限于具有匹配标签的资源
    此外，我们可以在一个查询中结合这些方法。这给了我们很大的灵活性，甚至可以解决复杂的需求。

    现在，让我们更详细地看看每种方法。

2. 使用命名空间

    使用命名空间是限制查询范围的最基本方法。顾名思义，命名空间的查询只返回指定命名空间内的项目。

    在Java API中，命名空间的查询方法遵循listNamespacedXXX()模式。例如，要列出特定命名空间的[pods](https://kubernetes.io/docs/concepts/workloads/pods/)，我们会使用listNamespacedPod()：

    ```java
    ApiClient client  = Config.defaultClient();
    CoreV1Api api = new CoreV1Api(client);
    String ns = "ns1";
    V1PodList items = api.listNamespacedPod(ns,null, null, null, null, null, null, null, null, 10, false);
    items.getItems()
    .stream()
    .map((pod) -> pod.getMetadata().getName() )
    .forEach((name) -> System.out.println("name=" + name));
    ```

    这里，[ApliClient](https://www.baeldung.com/kubernetes-java-client#1-apiclient-initialization)和CoreV1Api被用来执行对Kubernetes API服务器的实际访问。我们使用ns1作为命名空间来过滤资源。我们还使用了与非namespaced方法中类似的其余参数。

    正如预期的那样，namespaced查询也有调用变体，因此允许我们[使用之前描述](https://www.baeldung.com/java-kubernetes-watch)的相同技术来创建Watch。[异步调用和分页的工作](https://www.baeldung.com/java-kubernetes-paging-async)方式也与它们的非namespaced版本相同。

3. 使用字段选择器

    命名的API调用很简单，但有一些限制：

    - 它是全有或全无的，意味着我们不能选择超过一个（但不是全部）命名空间
    - 没有办法根据资源属性进行过滤
    - 对每种情况使用不同的方法会导致更复杂/更冗长的客户端代码
    [字段选择器](https://kubernetes.io/docs/concepts/overview/working-with-objects/field-selectors/)提供了一种基于其某个字段的值来选择资源的方法。在Kubernetes的术语中，字段只是与资源的YAML或JSON文档中给定值相关的JSON路径。例如，这是一个典型的Kubernetes YAML，用于运行Apache HTTP服务器的pod：

    ```yml
    apiVersion: v1
    kind: Pod
    metadata:
    labels:
        app: httpd
    name: httpd-6976bbc66c-4lbdp
    namespace: ns1
    spec:
    ... fields omitted
    status:
    ... fields omitted
    phase: Running
    ```

    字段 status.phase 包含现有 Pod 的状态。相应的字段选择器表达式是简单的字段名，后面有一个操作符和值。现在，让我们编写一个查询代码，返回所有命名空间中所有正在运行的Pod：

    ```java
    String fs = "status.phase=Running";
    V1PodList items = api.listPodForAllNamespaces(null, null, fs, null, null, null, null, null, 10, false);
    // ... process items
    ```

    字段选择器表达式只支持平等（'='或'=='）和不平等（'!='）运算符。另外，我们可以在同一个调用中传递多个逗号分隔的表达式。在这种情况下，净效果是它们将被AND在一起，产生最终的结果：

    ```java
    String fs = "metadata.namespace=ns1,status.phase=Running";        
    V1PodList items = api.listPodForAllNamespaces(null, null, fs, null, null, null, null, null, 10, false);
    // ... process items
    ```

    请注意：字段值是区分大小写的! 在前面的查询中，使用 "running" 而不是 "Running"（大写"R"）会产生一个空的结果集。

    字段选择器的一个重要限制是，它们是依赖资源的。只有metadata.name和metadata.namespace字段在所有资源类型中都被支持。

    尽管如此，字段选择器在与动态字段一起使用时特别有用。前面例子中的status.phase就是一个例子。使用字段选择器和Watch，我们可以很容易地创建一个监测应用程序，当pod终止时得到通知。

4. 使用标签选择器

    标签是包含任意键/值对的特殊字段，我们可以将其添加到任何Kubernetes资源中作为其创建的一部分。标签选择器类似于字段选择器，因为它们本质上允许根据其值过滤资源列表，但提供了更多灵活性：

    - 支持额外的操作符：in/notin/exists/not exists
    - 与字段选择器相比，在不同资源类型中的使用是一致的
    回到Java API，我们使用标签选择器是通过构建一个带有所需条件的字符串，并将其作为参数传递给所需的资源API listXXX调用。使用平等和/或不平等来过滤一个特定的标签值，使用的语法与字段选择器相同。

    让我们看看寻找所有标签 "app" 值为 "httpd" 的pod的代码：

    ```java
    String ls = "app=httpd";        
    V1PodList items = api.listPodForAllNamespaces(null, null, null, ls, null, null, null, null, 10, false);
    // ... process items
    ```

    in操作符类似于它的SQL对应符，允许我们在查询中创建一些OR逻辑：

    ```java
    String ls = "app in ( httpd, test )";        
    V1PodList items = api.listPodForAllNamespaces(null, null, null, ls, null, null, null, null, 10, false);
    ```

    另外，我们可以使用labelname或!labelname语法检查一个字段的存在与否：

    ```java
    String ls = "app";
    V1PodList items = api.listPodForAllNamespaces(null, null, null, ls, null, null, null, null, 10, false);
    ```

    最后，我们可以在一次API调用中连锁多个表达式。产生的项目列表只包含满足所有表达式的资源：

    ```java
    String ls = "app in ( httpd, test ),version=1,foo";
    V1PodList items = api.listPodForAllNamespaces(null, null, null, ls, null, null, null, null, 10, false);
    ```

5. 总结

    在这篇文章中，我们已经介绍了使用Java Kubernetes API客户端过滤资源的不同方法。

## 用Java Kubernetes API创建、更新和删除资源

1. 简介

    在本教程中，我们将介绍使用官方Java API对Kubernetes资源进行CRUD操作。

    在之前的文章中，我们已经介绍了该API的基本使用方法，包括[基本的项目设置](https://www.baeldung.com/kubernetes-java-client)和[各种方法](https://www.baeldung.com/java-kubernetes-watch)，我们可以用它来获得运行集群的信息。

    一般来说，Kubernetes的部署大多是静态的。我们创建一些工件（例如YAML文件）来描述我们想要创建的东西，并将它们提交给DevOps管道。然后，我们系统的各个部分保持不变，直到我们添加一个新的组件或升级一个现有的组件。

    然而，在有些情况下，我们需要即时添加资源。一个常见的情况是在响应用户发起的请求时运行[Jobs](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.19/#job-v1-batch)。作为回应，应用程序将启动一个后台作业来处理报告，并使其可供以后检索。

    这里的关键点是，通过使用这些API，我们可以更好地利用可用的基础设施，因为我们可以只在需要时消耗资源，之后再释放它们。

2. 创建一个新的资源

    在这个例子中，我们将在Kubernetes集群中创建一个Job资源。Job是一种Kubernetes工作负载，与其他类型的工作负载不同的是，它可以运行到完成。也就是说，一旦在其pod中运行的程序终止，工作本身就会终止。它的YAML表示法与其他资源没什么不同：

    ```yml
    apiVersion: batch/v1
    kind: Job
    metadata:
    namespace: jobs
    name: report-job
    labels:
        app: reports
    spec:
    template:
        metadata:
        name: payroll-report
        spec:
        containers:
        - name: main
            image: report-runner
            command:
            - payroll
            args:
            - --date
            - 2021-05-01
        restartPolicy: Never
    ```

    Kubernetes API提供了两种方法来创建等效的Java对象：

    - 用new创建POJOS并通过setters填充所有需要的属性
    - 使用fluent API来建立Java资源表示法
    使用哪种方法主要是个人的偏好。在这里，我们将使用fluent方法来创建V1Job对象，因为其构建过程看起来与YAML对应的对象非常相似：

    ```java
    ApiClient client  = Config.defaultClient();
    BatchV1Api api = new BatchV1Api(client);
    V1Job body = new V1JobBuilder()
    .withNewMetadata()
        .withNamespace("report-jobs")
        .withName("payroll-report-job")
        .endMetadata()
    .withNewSpec()
        .withNewTemplate()
        .withNewMetadata()
            .addToLabels("name", "payroll-report")
            .endMetadata()
        .editOrNewSpec()
            .addNewContainer()
            .withName("main")
            .withImage("report-runner")
            .addNewCommand("payroll")
            .addNewArg("--date")
            .addNewArg("2021-05-01")
            .endContainer()
            .withRestartPolicy("Never")
            .endSpec()
        .endTemplate()
        .endSpec()
    .build(); 
    V1Job createdJob = api.createNamespacedJob("report-jobs", body, null, null, null);
    ```

    我们首先创建ApiClient，然后创建API stub实例。工作资源是Batch API的一部分，所以我们创建一个BatchV1Api实例，我们将用它来调用集群的API服务器。

    接下来，我们实例化一个V1JobBuilder实例，它可以引导我们完成填写所有属性的过程。 注意嵌套构建器的使用：要 "close"一个嵌套构建器，我们必须调用它的endXXX()方法，这将使我们回到其父构建器。

    另外，也可以使用withXXX方法来直接注入一个嵌套对象。当我们想重用一组共同的属性，比如元数据、标签和注解时，这很有用。

    最后一步只是对API存根的一个调用。这将使我们的资源对象序列化，并将请求发送到服务器上。正如预期的那样，API有同步的（上面使用的）和异步的版本。

    返回的对象将包含与创建的作业有关的元数据和状态字段。在作业的情况下，我们可以使用其状态字段来检查它何时完成。我们也可以使用我们关于监控资源的文章中介绍的技术之一来接收这个通知。

3. 更新现有的资源

    更新现有资源包括向Kubernetes API服务器发送一个PATCH请求，其中包含我们想要修改的字段。从Kubernetes 1.16版本开始，有四种方法来指定这些字段：

    - JSON补丁（RFC 6092）
    - JSON合并补丁(RFC 7396)
    - 战略合并补丁
    - 应用YAML
    其中，最后一种是最容易使用的，因为它把所有的合并和冲突解决留给了服务器：我们所要做的就是发送一个YAML文档，其中包含我们想要修改的字段。

    不幸的是，Java API没有提供简单的方法来建立这个部分YAML文档。相反，我们必须求助于PatchUtil辅助类来发送一个原始YAML或JSON字符串。然而，我们可以使用通过ApiClient对象提供的内置JSON序列化器来获得它：

    ```java
    V1Job patchedJob = new V1JobBuilder(createdJob)
    .withNewMetadata()
        .withName(createdJob.getMetadata().getName())
        .withNamespace(createdJob.getMetadata().getNamespace())
        .endMetadata()
    .editSpec()
        .withParallelism(2)
    .endSpec()
    .build();

    String patchedJobJSON = client.getJSON().serialize(patchedJob);

    PatchUtils.patch(
    V1Job.class, 
    () -> api.patchNamespacedJobCall(
        createdJob.getMetadata().getName(), 
        createdJob.getMetadata().getNamespace(), 
        new V1Patch(patchedJobJSON), 
        null, 
        null, 
        "baeldung", 
        true, 
        null),
    V1Patch.PATCH_FORMAT_APPLY_YAML,
    api.getApiClient());
    ```

    在这里，我们使用从createNamespacedJob()返回的对象作为模板，我们将从中构建补丁版本。在这种情况下，我们只是将平行度值从1增加到2，其他字段保持不变。这里重要的一点是，当我们构建修改后的资源时，我们必须使用withNewMetadata()。 这可以确保我们不会建立一个包含托管字段的对象，这些字段存在于我们创建资源后得到的返回对象中。关于托管字段的完整描述以及它们在Kubernetes中的使用方式，请[参考文档](https://kubernetes.io/docs/reference/using-api/server-side-apply/#field-management)。

    一旦我们建立了一个带有修改过的字段的对象，我们就会使用序列化方法将其转换为JSON表示。然后我们使用这个序列化的版本来构建一个V1Patch对象，作为PATCH调用的有效载荷。patch方法还需要一个额外的参数，在这里我们告知请求中存在的数据类型。在我们的例子中，这是PATCH_FORMAT_APPLY_YAML，库将其作为HTTP请求中的Content-Type头使用。

    传递给fieldManager参数的 "baeldung" 值定义了操纵资源字段的行为者名称。Kubernetes内部使用这个值来解决两个或多个客户端试图修改同一资源时的最终冲突。我们还在force参数中传递了true，意味着我们将获得任何修改过的字段的所有权。

4. 删除一个资源

    与之前的操作相比，删除一个资源是非常直接的：

    ```java
    V1Status response = api.deleteNamespacedJob(
    createdJob.getMetadata().getName(), 
    createdJob.getMetadata().getNamespace(), 
    null, 
    null, 
    null, 
    null, 
    null, 
    null) ;
    ```

    在这里，我们只是使用deleteNamespacedJob方法，使用这种特定资源的默认选项来删除作业。如果需要，我们可以使用最后一个参数来控制删除过程的细节。这采取了V1DeleteOptions对象的形式，我们可以用它来指定一个宽限期和对任何依赖资源的级联行为。

5. 总结

    在这篇文章中，我们已经介绍了如何使用Java Kubernetes API库来操作Kubernetes资源。

## 用Kubernetes API进行分页和异步调用

1. 简介

    在本教程中，我们将继续探索Kubernetes API for Java。这一次，我们将专注于它的两个功能：分页和异步调用。

2. 分页

    简而言之，分页允许我们在一个大的结果集上分块迭代，也就是[分页](https://www.baeldung.com/spring-data-jpa-pagination-sorting)--这个方法的名字由此而来。在Kubernetes Java API的上下文中，所有返回资源列表的方法都有这个功能。这些方法总是包括两个可选参数，我们可以用它们来遍历结果：

    - limit：在一次API调用中返回的最大项目数
    - continue： 一个延续令牌，告诉服务器返回结果集的起点。
    使用这些参数，我们可以对任意数量的项目进行迭代，而不会给服务器带来太大的压力。甚至更好的是，客户端为保存结果所需的内存量也是有限制的。

    现在，让我们看看如何使用这些参数来获得一个集群中所有可用的pod的列表，使用这个方法：

    ```java
    ApiClient client = Config.defaultClient();
    CoreV1Api api = new CoreV1Api(client);
    String continuationToken = null;
    do {
        V1PodList items = api.listPodForAllNamespaces(
        null,
        continuationToken, 
        null,
        null, 
        2, 
        null, 
        null,
        null,
        10,
        false);
        continuationToken = items.getMetadata().getContinue();
        items.getItems()
        .stream()
        .forEach((node) -> System.out.println(node.getMetadata()));
    } while (continuationToken != null);
    ```

    在这里，listPodForAllNamespaces()API调用的第二个参数包含延续令牌，第五个是限制参数。虽然极限通常只是一个固定值，但继续需要一点额外的努力。

    对于第一次调用，我们发送一个空值，向服务器发出信号，这是分页请求序列的第一次调用。收到响应后，我们从相应的列表元数据字段中获得下一个继续值的新值。

    当没有更多的结果可用时，这个值将是空的，所以我们用这个事实来定义迭代循环的退出条件。

    1. 分页的问题

        分页机制是非常直接的，但有一些细节我们必须记住：

        - 目前，API并不支持服务器端的排序。鉴于目前缺乏存储层对排序的支持，这一点不太可能[很快改变](https://github.com/kubernetes/kubernetes/issues/80602)。
        - 所有的调用参数，除了continue，在不同的调用之间必须是相同的。
        - continue的值必须被视为一个不透明的句柄。我们不应该对它的值做任何假设。
        - 迭代是单向的。我们不能使用之前收到的continue令牌返回到结果集中。
        - 即使返回的列表元数据包含一个剩余项目计数字段，其值既不可靠，也不被所有的实现所支持。
    2. 列表数据的连贯性

        由于Kubernetes集群是一个非常动态的环境，与分页调用序列相关的结果集有可能在被客户端读取时被修改。在这种情况下，Kubernetes的API是如何表现的？

        正如[Kubernetes文档](https://kubernetes.io/docs/reference/using-api/api-concepts/#the-resourceversion-parameter)中所解释的，列表API支持一个资源版本参数，该参数与资源版本匹配一起，定义了如何选择一个特定的版本进行包含。然而，对于分页结果集的情况，其行为总是相同的："Continue Token, Exact"。

        这意味着返回的资源版本对应于分页列表调用开始时的可用版本。虽然这种方法提供了一致性，但它不会包括事后修改的结果。例如，当我们完成对一个大型集群中所有pod的迭代时，其中一些可能已经终止了。

3. 异步调用

    到目前为止，我们以同步的方式使用Kubernetes API，这对简单的程序来说没有问题，但从资源使用的角度来看，效率并不高，因为它阻塞了调用线程，直到我们收到集群的响应并处理它。例如，如果我们开始在GUI线程中进行这些调用，这种行为将严重损害应用程序的响应性。

    幸运的是，该库支持基于回调的异步模式，它可以立即将控制权返回给调用者。

    检查CoreV1Api类，我们会注意到，对于每个同步的xxx()方法，也有一个xxxAsync()变体。例如，listPodForAllNamespaces()的异步方法是listPodForAllNamespacesAsync()。参数是一样的，只是增加了一个用于回调实现的额外参数。

    1. 回调细节

        回调参数对象必须实现通用接口`ApiCallback<T>`，它只包含四个方法：

        - onSuccess： 当且仅当调用成功时被调用。第一个参数类型与同步版本所返回的参数相同
        - onFailure： 在调用服务器时出现错误或回复中包含错误代码时调用。
        - onUploadProgress： 在上传过程中被调用。我们可以使用这个回调来在漫长的操作过程中向用户提供反馈。
        - onDownloadProgress： 与onUploadProgress相同，但用于下载。
        异步调用也不会返回一个常规的结果。相反，它们会返回一个[OkHttp](https://www.baeldung.com/guide-to-okhttp)的（Kubernetes API使用的底层REST客户端）调用实例，该实例作为下行调用的句柄。我们可以使用这个对象来轮询完成状态，或者，如果我们想，在完成之前取消它。

    2. 异步调用实例

        我们可以想象，到处实现回调需要大量的模板代码。为了避免这一点，我们将使用一个[调用助手](https://github.com/eugenp/tutorials/blob/master/kubernetes-modules/k8s-intro/src/main/java/com/baeldung/kubernetes/intro/AsyncHelper.java)来简化这项任务：

        ```java
        // Start async call
        CompletableFuture<V1NodeList> p = AsyncHelper.doAsync(api,(capi,cb) ->
        capi.listNodeAsync(null, null, null, null, null, null, null, null, 10, false, cb)
        );
        p.thenAcceptAsync((nodeList) -> {
            nodeList.getItems()
            .stream()
            .forEach((node) -> System.out.println(node.getMetadata()));
        });
        // ... do something useful while we wait for results
        ```

        在这里，帮助器包装了异步调用调用，并将其调整为一个更标准的CompletableFuture。这使我们能够与其他库一起使用它，例如来自[Reactor项目](https://www.baeldung.com/reactor-core)的库。在这个例子中，我们添加了一个完成阶段，将所有元数据打印到标准输出。

        像往常一样，在处理期货时，我们必须注意可能出现的并发性问题。这段代码的在线版本包含一些调试日志，这些日志清楚地表明，即使对于这段简单的代码，至少也使用了三个线程：

        - 主线程，它启动了异步调用
        - OkHttp的线程，用于进行实际的HTTP调用
        - 完成线程，处理结果的地方
4. 结语

    在这篇文章中，我们已经看到了如何使用Kubernetes Java API的分页和异步调用。

## Relevant Articles

- [ ] [A Quick Intro to the Kubernetes Java Client](https://www.baeldung.com/kubernetes-java-client)
- [ ] [Paging and Async Calls with the Kubernetes API](https://www.baeldung.com/java-kubernetes-paging-async)
- [ ] [Using Watch with the Kubernetes API](https://www.baeldung.com/java-kubernetes-watch)
- [ ] [Using Namespaces and Selectors With the Kubernetes Java API](https://www.baeldung.com/java-kubernetes-namespaces-selectors)
- [ ] [Creating, Updating and Deleting Resources with the Java Kubernetes API](https://www.baeldung.com/java-kubernetes-api-crud)

## Code

像往常一样，这些例子的完整源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/kubernetes-modules/k8s-intro)上找到。
