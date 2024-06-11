# [SpringBoot执行器](https://www.baeldung.com/spring-boot-actuators)

1. 概述

    在这篇文章中，我们将介绍Spring Boot Actuator。我们将首先介绍基础知识，然后详细讨论Spring Boot 2.x与1.x中的可用内容。

    我们将学习如何利用反应式编程模型，在Spring Boot 2.x和WebFlux中使用、配置和扩展这个监控工具。然后我们将讨论如何使用Boot 1.x做同样的事情。

    自2014年4月起，Spring Boot Actuator与第一个Spring Boot版本一起提供。

    随着Spring Boot 2的发布，Actuator被重新设计，并增加了新的令人兴奋的端点。

    我们将本指南分为三个主要部分：

    [配置Spring Boot Web应用程序](https://www.baeldung.com/spring-boot-application-configuration)

    一些对Spring Boot应用程序比较有用的配置。

    [用Spring Boot创建自定义启动器](https://www.baeldung.com/spring-boot-custom-starter)

    关于创建自定义Spring Boot启动器的快速实用指南。

    [在Spring Boot中进行测试](https://www.baeldung.com/spring-boot-testing)

    了解Spring Boot如何支持测试，以有效地编写单元测试。

2. 什么是执行器？

    从本质上讲，Actuator为我们的应用程序带来了生产就绪的功能。

    有了这个依赖，监控我们的应用、收集指标、了解流量或数据库的状态就变得微不足道了。

    这个库的主要好处是，我们可以获得生产级的工具，而不需要自己实际实现这些功能。

    Actuator主要用于暴露运行中的应用程序的操作信息--健康、指标、信息、转储、环境等。它使用HTTP端点或JMX豆，使我们能够与它互动。

    一旦这个依赖关系出现在classpath上，就有几个端点可供我们开箱使用。与大多数Spring模块一样，我们可以很容易地以多种方式配置或扩展它。

    1. 开始使用

        要启用Spring Boot Actuator，我们只需将spring-boot-actuator依赖项添加到我们的软件包管理器中。

        在Maven中：spring-boot-starter-actuator

        注意，无论Boot版本如何，这都是有效的，因为版本在Spring Boot材料清单（BOM）中指定。

3. Spring Boot 2.x Actuator

    在2.x中，Actuator保留了其基本意图，但简化了其模型，扩展了其功能，并纳入了更好的默认值。

    首先，这个版本变得与技术无关(technology-agnostic)。它还简化了其安全模型，将其与应用模型合并。

    在各种变化中，重要的是要记住，其中一些变化是破坏性的。这包括HTTP请求和响应，以及Java APIs。

    最后，最新版本现在支持CRUD模型，而不是以前的读/写模型。

    1. 技术支持

        在其第二个主要版本中，Actuator现在是技术无关的，而在1.x中，它与MVC相联系，因此与Servlet API相联系。

        在2.x中，Actuator将其模型定义为可插拔和可扩展的，而不依赖MVC来实现。

        因此，通过这个新的模型，我们能够利用MVC以及WebFlux作为底层网络技术的优势。

        此外，通过实现正确的适配器，可以添加即将到来的技术。

        最后，JMX仍然被支持，无需任何额外的代码就可以暴露端点。

    2. 重要变化

        与以前的版本不同，Actuator的大多数端点都是禁用的。

        因此，默认情况下，只有/health和/info两个可用。

        如果我们想启用所有的端点，我们可以设置 `management.endpoints.web.exposure.include=*` 。另外，我们也可以列出应该启用的端点。

        Actuator现在与常规的App安全规则共享安全配置，所以安全模型被大大简化。

        因此，要调整Actuator的安全规则，我们可以只为/actuator/**添加一个条目：

        ```java
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http) {
            return http.authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .anyExchange().authenticated()
            .and().build();
        }
        ```

        我们可以在全新的[Actuator官方文档](https://docs.spring.io/spring-boot/docs/2.0.x/actuator-api/html/)中找到进一步的细节。

        另外，在默认情况下，所有的Actuator端点现在都被放在/actuator路径下。

        和以前的版本一样，我们可以使用新的属性management.endpoints.web.base-path来调整这个路径。

    3. 预定义的端点

        让我们看看一些可用的端点，其中大部分在1.x中已经可用。

        另外，有些端点被添加了，有些被删除了，有些被重组了：

        /auditevents 列出了与安全审计有关的事件，如用户登录/注销。此外，我们还可以通过本金或类型以及其他字段进行过滤。
        /beans 返回我们BeanFactory中所有可用的bean。与/auditevents不同，它不支持过滤。
        /conditions 以前被称为/autoconfig，围绕自动配置建立一个条件报告。
        /configprops 允许我们获取所有@ConfigurationProperties豆。
        /env 返回当前的环境属性。此外，我们还可以检索单个属性。
        /flyway 提供了关于我们Flyway数据库迁移的细节。
        /health 总结了我们应用程序的健康状态。
        /heapdump 构建并返回我们应用程序使用的 JVM 的堆转储。
        /info 返回一般信息。它可能是自定义数据、构建信息或关于最新提交的细节。
        /liquibase 的行为类似于/flyway，但针对Liquibase。
        /logfile 返回普通的应用程序日志。
        /loggers 使我们能够查询和修改我们应用程序的日志级别。
        /metrics 详细说明我们应用程序的指标。这可能包括通用指标和自定义指标。
        /prometheus 返回与前面一样的指标，但格式化为与Prometheus服务器一起工作。
        /scheduledtasks 提供了关于我们应用程序中每个计划任务的细节。
        /sessions 列出HTTP会话，因为我们使用的是Spring Session。
        /shutdown执行应用程序的优雅关闭。
        /threaddump转储底层JVM的线程信息。

    4. 执行器端点的超媒体

        Spring Boot增加了一个发现端点，可以返回所有可用的执行器端点的链接。这将有助于发现执行器端点及其相应的URL。

        默认情况下，这个发现端点可以通过/actuator端点访问。

        因此，如果我们向这个URL发送一个GET请求，它将返回各种端点的执行器链接：

        ```json
        {
        "_links": {
            "self": {
            "href": "http://localhost:8080/actuator",
            "templated": false
            },
            "features-arg0": {
            "href": "http://localhost:8080/actuator/features/{arg0}",
            "templated": true
            },
            "features": {
            "href": "http://localhost:8080/actuator/features",
            "templated": false
            },
            "beans": {
            "href": "http://localhost:8080/actuator/beans",
            "templated": false
            },
            "caches-cache": {
            "href": "http://localhost:8080/actuator/caches/{cache}",
            "templated": true
            },
            // truncated
        }
        ```

        如上所示，/actuator端点在_linkks字段下报告所有可用的执行器端点。

        此外，如果我们配置了一个自定义的管理基础路径，那么我们应该使用该基础路径作为发现URL。

        例如，如果我们把management.endpoints.web.base-path设置为/mgmt，那么我们应该向/mgmt端点发送请求，以查看链接列表。

        相当有趣的是，当管理基础路径被设置为/时，发现端点被禁用，以防止与其他映射发生冲突的可能性。

    5. 健康指示器

        就像以前的版本一样，我们可以很容易地添加自定义指标。与其他API相反，用于创建自定义健康端点的抽象概念保持不变。然而，一个新的接口，即ReactiveHealthIndicator，已经被添加到实现反应式健康检查。

        让我们来看看一个简单的自定义反应式健康检查：

        ```java
        @Component
        public class DownstreamServiceHealthIndicator implements ReactiveHealthIndicator {

            @Override
            public Mono<Health> health() {
                return checkDownstreamServiceHealth().onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build())
                );
            }

            private Mono<Health> checkDownstreamServiceHealth() {
                // we could use WebClient to check health reactively
                return Mono.just(new Health.Builder().up().build());
            }
        }
        ```

        健康指标的一个方便的特点是，我们可以把它们作为层次结构的一部分来聚合。

        因此，按照前面的例子，我们可以将所有下游服务归入一个下游服务类别。只要每个嵌套的服务都是可到达的，这个类别就会是健康的。

        请查看我们关于[健康指标](https://www.baeldung.com/spring-boot-health-indicators)的文章，以获得更深入的了解。

    6. 健康组

        从Spring Boot 2.2开始，我们可以将健康指标组织成[组](https://github.com/spring-projects/spring-boot/blob/c3aa494ba32b8271ea19dd041327441b27ddc319/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/health/HealthEndpointGroups.java#L30)，并对所有组成员应用相同的配置。

        例如，我们可以通过在application.properties中添加以下内容来创建一个名为custom的健康组：

        `management.endpoint.health.group.custom.include=diskSpace,ping`

        这样，自定义组就包含了diskSpace和ping的健康指标。

        现在，如果我们调用/actuator/health端点，它将在JSON响应中告诉我们关于新的健康组：

        `{"status":"UP","groups":["custom"]}`

        通过健康组，我们可以看到一些健康指标的汇总结果。

        在这种情况下，如果我们向/actuator/health/custom发送一个请求，那么：

        `{"status":"UP"}`

        我们可以通过application.properties配置该组，以显示更多的细节：

        ```properties
        management.endpoint.health.group.custom.show-components=always
        management.endpoint.health.group.custom.show-details=always
        ```

        现在，如果我们向/actuator/health/custom发送同样的请求，我们会看到更多的细节：

        ```json
        {
        "status": "UP",
        "components": {
            "diskSpace": {
            "status": "UP",
            "details": {
                "total": 499963170816,
                "free": 91300069376,
                "threshold": 10485760
            }
            },
            "ping": {
            "status": "UP"
            }
        }
        }
        ```

        也可以只对授权用户显示这些细节：

        ```properties
        management.endpoint.health.group.custom.show-components=when_authorized
        management.endpoint.health.group.custom.show-details=when_authorized
        ```

        我们也可以有一个自定义的状态映射。

        例如，它可以不返回HTTP 200 OK响应，而是返回207状态代码：

        `management.endpoint.health.group.custom.status.http-mapping.up=207`

        这里，我们告诉Spring Boot，如果自定义组的状态是UP，则返回207的HTTP状态代码。

    7. Spring Boot 2中的度量衡

        在Spring Boot 2.0中，内部指标被Micrometer支持所取代，因此我们可以预期会有一些变化。如果我们的应用程序正在使用GaugeService或CounterService等度量衡服务，它们将不再可用。

        相反，我们要与[Micrometer](https://www.baeldung.com/micrometer)直接互动。在Spring Boot 2.0中，我们会得到一个自动配置的MeterRegistry类型的bean。

        此外，Micrometer现在是Actuator依赖的一部分，所以只要Actuator依赖在classpath中，我们就应该可以使用了。

        此外，我们将从/metrics端点得到一个全新的响应：

        ```json
        {
        "names": [
            "jvm.gc.pause",
            "jvm.buffer.memory.used",
            "jvm.memory.used",
            "jvm.buffer.count",
            // ...
        ]
        }
        ```

        正如我们所看到的，没有像我们在1.x中得到的实际度量。

        为了得到一个特定指标的实际值，我们现在可以导航到所需的指标，例如，/actuator/metrics/jvm.gc.pause，然后得到一个详细的响应：

        ```json
        {
        "name": "jvm.gc.pause",
        "measurements": [
            {
            "statistic": "Count",
            "value": 3.0
            },
            {
            "statistic": "TotalTime",
            "value": 7.9E7
            },
            {
            "statistic": "Max",
            "value": 7.9E7
            }
        ],
        "availableTags": [
            {
            "tag": "cause",
            "values": [
                "Metadata GC Threshold",
                "Allocation Failure"
            ]
            },
            {
            "tag": "action",
            "values": [
                "end of minor GC",
                "end of major GC"
            ]
            }
        ]
        }
        ```

        现在的衡量标准更加彻底，不仅包括不同的数值，还包括一些相关的元数据。

    8. 定制/info端点

        /info端点保持不变。和以前一样，我们可以使用相应的Maven或Gradle依赖性添加git细节：

        ```xml
        <dependency>
            <groupId>pl.project13.maven</groupId>
            <artifactId>git-commit-id-plugin</artifactId>
        </dependency>
        ```

        同样地，我们也可以使用Maven或Gradle插件包括构建信息，包括名称、组和版本：

        ```xml
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>build-info</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ```

    9. 创建一个自定义端点

        正如我们之前指出的，我们可以创建自定义端点。然而，Spring Boot 2重新设计了实现的方式，以支持新的技术无关的范式。

        让我们创建一个Actuator端点，在我们的应用程序中查询、启用和禁用功能标志：

        ```java
        @Component
        @Endpoint(id = "features")
        public class FeaturesEndpoint {

            private Map<String, Feature> features = new ConcurrentHashMap<>();

            @ReadOperation
            public Map<String, Feature> features() {
                return features;
            }

            @ReadOperation
            public Feature feature(@Selector String name) {
                return features.get(name);
            }

            @WriteOperation
            public void configureFeature(@Selector String name, Feature feature) {
                features.put(name, feature);
            }

            @DeleteOperation
            public void deleteFeature(@Selector String name) {
                features.remove(name);
            }

            public static class Feature {
                private Boolean enabled;

                // [...] getters and setters 
            }

        }
        ```

        为了获得端点，我们需要一个Bean。在我们的例子中，我们使用@Component来做这个。同时，我们需要用@Endpoint来装饰这个Bean。

        我们的端点的路径是由@Endpoint的id参数决定的。在我们的例子中，它将把请求路由到/actuator/features。

        一旦准备就绪，我们就可以开始使用定义操作了：

        - @ReadOperation： 它将映射到HTTP GET。
        - @WriteOperation： 它将映射到HTTP POST。
        - @DeleteOperation： 它将映射到HTTP DELETE。

        当我们在应用程序中使用前一个端点运行应用程序时，Spring Boot将注册它。

        验证这一点的一个快速方法是检查日志：

        ```log
        [...].WebFluxEndpointHandlerMapping: Mapped "{[/actuator/features/{name}],
        methods=[GET],
        produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}"
        [...].WebFluxEndpointHandlerMapping : Mapped "{[/actuator/features],
        methods=[GET],
        produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}"
        [...].WebFluxEndpointHandlerMapping : Mapped "{[/actuator/features/{name}],
        methods=[POST],
        consumes=[application/vnd.spring-boot.actuator.v2+json || application/json]}"
        [...].WebFluxEndpointHandlerMapping : Mapped "{[/actuator/features/{name}],
        methods=[DELETE]}"[...]
        ```

        在前面的日志中，我们可以看到WebFlux是如何暴露我们的新端点的。如果我们切换到MVC，它就会简单地在该技术上进行委托，而不需要改变任何代码。

        另外，在这种新的方法中，我们有几个重要的考虑因素要记住：

        - MVC没有依赖性。
        - 所有以前作为方法存在的元数据（sensitive, enabled…）都不再存在。然而，我们可以使用`@Endpoint(id = “features”, enableByDefault = false)`启用或禁用端点。
        - 与1.x不同的是，不再需要扩展一个给定的接口。
        - 与以前的读/写模式相比，我们现在可以使用@DeleteOperation来定义DELETE操作。

    10. 扩展现有的端点

        让我们想象一下，我们想确保我们应用程序的生产实例永远不是SNAPSHOT版本。

        我们决定通过改变返回该信息的Actuator端点的HTTP状态代码，即/info来做到这一点。如果我们的应用程序碰巧是SNAPSHOT，我们会得到一个不同的HTTP状态代码。

        我们可以使用@EndpointExtension注解，或其更具体的特殊化@EndpointWebExtension或@EndpointJmxExtension，轻松地扩展一个预定义的端点的行为：

        ```java
        @Component
        @EndpointWebExtension(endpoint = InfoEndpoint.class)
        public class InfoWebEndpointExtension {

            private InfoEndpoint delegate;

            // standard constructor

            @ReadOperation
            public WebEndpointResponse<Map> info() {
                Map<String, Object> info = this.delegate.info();
                Integer status = getStatus(info);
                return new WebEndpointResponse<>(info, status);
            }

            private Integer getStatus(Map<String, Object> info) {
                // return 5xx if this is a snapshot
                return 200;
            }
        }
        ```

    11. 启用所有端点

        为了使用HTTP访问执行机构的端点，我们需要启用和公开它们。

        默认情况下，除了/shutdown，所有的端点都是启用的。默认情况下，只有/health和/info这两个端点是公开的。

        我们需要添加以下配置来暴露所有的端点：

        `management.endpoints.web.exposure.include=*`

        要明确启用一个特定的端点（例如，关闭），我们使用：

        `management.endpoint.shutdown.enabled=true`

        要暴露所有已启用的端点，除了一个（例如，/loggers），我们使用： 拷贝

        ```properties
        management.endpoints.web.exposure.include=*
        management.endpoints.web.exposure.exclude=loggers
        ```

4. Spring Boot 1.x Actuator

    省略...

5. 总结

    在这篇文章中，我们谈到了Spring Boot Actuator。我们首先定义了Actuator的含义，以及它为我们做了什么。

    接下来，我们重点讨论了当前Spring Boot 2.x版本的Actuator，讨论了如何使用它、调整它和扩展它。我们还谈到了在这个新的迭代中我们可以发现的重要的安全变化。我们讨论了一些流行的端点，以及它们是如何变化的。

    然后我们讨论了早期Spring Boot 1版本中的Actuator。

    最后，我们演示了如何定制和扩展Actuator。

## Code

一如既往，本文中使用的代码可以在GitHub上找到[Spring Boot 2.x](https://github.com/eugenp/tutorials/tree/master/spring-reactive-modules/spring-reactive-3)和[Spring Boot 1.x的代码](https://github.com/eugenp/tutorials/tree/master/spring-4)。
