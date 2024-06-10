# [使用 Netflix Ribbon 的 Spring Cloud Rest Client 简介](https://www.baeldung.com/spring-cloud-rest-client-with-netflix-ribbon)

1. 简介

    Netflix [Ribbon](https://github.com/Netflix/ribbon) 是一个进程间通信（IPC）云库。Ribbon 主要提供客户端负载平衡算法。

    除了客户端负载均衡算法，Ribbon 还提供其他功能：

    - 服务发现集成 - Ribbon 负载均衡器可在云等动态环境中提供服务发现功能。Ribbon 库中包括与 Eureka 和 Netflix 服务发现组件的集成。
    - 容错功能 - Ribbon API 可以动态确定服务器在实时环境中是否正常运行，并能检测出宕机的服务器。
    - 可配置的负载平衡规则 - Ribbon 支持 RoundRobinRule、AvailabilityFilteringRule、WeightedResponseTimeRule，也支持自定义规则。
    Ribbon API 基于 “Named Client” 概念工作。在应用程序配置文件中配置 Ribbon 时，我们会为负载平衡所包含的服务器列表提供一个名称。

    让我们试试看。

2. 依赖关系管理

    在 pom.xml 中添加以下依赖项，即可将 Netflix Ribbon API 添加到我们的项目中：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>
    ```

    可在此处找到最新的库。

3. 应用程序示例

    为了了解 Ribbon API 的工作原理，我们使用 Spring RestTemplate 构建了一个示例微服务应用程序，并使用 Netflix Ribbon API 和 Spring Cloud Netflix API 对其进行了增强。

    我们将使用 Ribbon 的负载平衡策略之一 - 加权响应时间规则（WeightedResponseTimeRule），在应用程序中的两个服务器之间实现客户端负载平衡，这两个服务器在配置文件中定义为一个命名的客户端。

4. 功能区配置

    通过 Ribbon API，我们可以配置负载平衡器的以下组件：

    - Rule - 逻辑组件，用于指定应用程序中使用的负载平衡规则
    - Ping--指定我们用于实时确定服务器可用性的机制的组件
    - ServerList - 可以是动态或静态的。在我们的例子中，我们使用的是静态服务器列表，因此我们直接在应用程序配置文件中定义它们

    让我们为库编写一个简单的配置：

    ```java
    public class RibbonConfiguration {

        @Autowired
        IClientConfig ribbonClientConfig;

        @Bean
        public IPing ribbonPing(IClientConfig config) {
            return new PingUrl();
        }

        @Bean
        public IRule ribbonRule(IClientConfig config) {
            return new WeightedResponseTimeRule();
        }
    }
    ```

    请注意我们是如何使用 WeightedResponseTimeRule 规则来确定服务器和 PingUrl 机制来实时确定服务器的可用性的。

    根据该规则，每个服务器会根据其平均响应时间获得一个权重，响应时间越短，权重越低。该规则随机选择一个服务器，其可能性由服务器的权重决定。

    PingUrl 会 ping 每个 URL 以确定服务器的可用性。

5. application.yml

    以下是我们为该示例应用程序创建的 [application.yml](./src/main/resources/application.yml) 配置文件。

    在上述文件中，我们指定了

    - 应用程序名称
    - 应用程序的端口号
    - 服务器列表的命名客户端： "ping-server"
    - 将 eureka: enabled 设为 false，禁用 Eureka 服务发现组件
    - 定义可用于负载平衡的服务器列表，在本例中为 2 台服务器
    - 使用 ServerListRefreshInterval 配置服务器刷新率

6. 功能区客户端

    现在我们来设置主应用程序组件片段--在这里，我们使用 RibbonClient 代替普通的 RestTemplate 来启用负载平衡：

    [ServerLocationApp.java](./src/main/java/com/baeldung/spring/cloud/ribbon/client/ServerLocationApp.java)

    下面是 RestTemplate 配置：

    ```java
    @Configuration
    public class RestTemplateConfiguration{
        @LoadBalanced
        @Bean
        RestTemplate getRestTemplate() {
            return new RestTemplate();
        }
    }
    ```

    我们用注解 @RestController 定义了一个控制器类；我们还用 @RibbonClient 注解了该类的名称和配置类。

    我们在这里定义的配置类与之前定义的类相同，我们在其中为该应用程序提供了所需的 Ribbon API 配置。

    请注意，我们用 @LoadBalanced 对 RestTemplate 进行了注释，这表明我们希望它是负载平衡的，而且在这种情况下是使用 Ribbon。

7. Ribbon 的故障恢复能力

    正如我们在本文前面所讨论的，Ribbon 应用程序接口不仅提供客户端负载均衡算法，还内置了故障恢复能力。

    如前所述，Ribbon API 可以通过定时不断地 ping 服务器来确定服务器的可用性，并能跳过不在线的服务器。

    除此之外，它还实现了断路器模式（Circuit Breaker pattern），可根据指定标准过滤掉服务器。

    断路器模式会迅速拒绝向发生故障的服务器发出的请求，而不会等待超时，从而将服务器故障对性能的影响降至最低。我们可以通过将 niws.loadbalancer.availabilityFilteringRule.filterCircuitTripped 属性设置为 false 来禁用断路器功能。

    当所有服务器都宕机时，即没有服务器可用来为请求提供服务，pingUrl() 将失败，我们将收到 java.lang.IllegalStateException 异常，并显示 “No instances are available to serve the request”（没有实例可用来为请求提供服务）。

8. 结论

    在本文中，我们讨论了 Netflix Ribbon API 及其在一个简单示例应用程序中的实现。
