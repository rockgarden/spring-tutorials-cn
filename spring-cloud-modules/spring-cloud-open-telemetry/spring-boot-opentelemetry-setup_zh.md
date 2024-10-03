# [在 Spring Boot 应用程序中设置 OpenTelemetry](https://www.baeldung.com/spring-boot-opentelemetry-setup)

1. 概述

    在分布式系统中，服务请求时偶尔出错是意料之中的事。中央可[观察性](https://www.baeldung.com/distributed-systems-observability#:~:text=Observability%20is%20the%20ability%20to,basically%20known%20as%20telemetry%20data.)平台可捕获应用程序跟踪/日志，并提供查询特定请求的接口。 [OpenTelemetry](https://opentelemetry.io/) 有助于规范捕获和导出遥测数据的过程。

    在本教程中，我们将学习如何将 Spring Boot 应用程序与 OpenTelemetry 集成。此外，我们还将配置 OpenTelemetry 以捕获应用程序跟踪，并将其发送到中央系统以监控请求。

    首先，让我们了解一些基本概念。

2. OpenTelemetry 简介

    OpenTelemetry (Otel) 是一个标准化的厂商无关工具、API 和 SDK 的集合。它是 CNCF 孵化项目，由 OpenTracing 和 OpenCensus 项目合并而成。

    OpenTracing 是一个厂商中立的应用程序接口，用于向可观测性后端发送遥测数据。OpenCensus 项目提供了一套特定语言库，开发人员可以用它来检测自己的代码，并将其发送到任何受支持的后端。Otel 使用与其前身项目相同的跟踪和跨度概念来表示微服务间的请求流。

    OpenTelemetry 允许我们检测、生成和收集遥测数据，这有助于分析应用程序的行为或性能。遥测数据包括日志、指标和跟踪。我们可以自动或手动检测 HTTP、数据库调用等代码。

    使用 Otel SDK，我们可以轻松覆盖或向跟踪添加更多属性。

    让我们通过一个示例来深入了解一下。

3. 应用程序示例

    假设我们需要构建两个微服务，其中一个服务与另一个服务交互。为了对应用程序进行遥测数据检测，我们将把应用程序与 Spring Cloud 和 OpenTelemetry 集成。

    1. Maven 依赖项

        spring-cloud-starter-sleuth、spring-cloud-sleuth-otel-autoconfigure 和 opentelemetry-exporter-otlp 依赖项将自动捕获并向任何支持的收集器导出跟踪数据。

        首先，我们将创建一个 Spring Boot Web 项目，并在两个应用程序中包含以下 Spring 和 OpenTelemetry 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-sleuth-brave</artifactId>
                </exclusion>
        </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-otel-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-otlp</artifactId>
            <version>1.23.1</version>
        </dependency>
        ```

        需要注意的是，我们排除了 Spring Cloud Brave 依赖项，以便使用 Otel 替换默认跟踪实现。

        此外，我们还需要包含 Spring Cloud Sleuth 的 Spring 依赖关系管理 [BOM](https://www.baeldung.com/spring-maven-bom)：

        ```xml
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-dependencies</artifactId>
                    <version>2021.0.5</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-sleuth-otel-dependencies</artifactId>
                    <version>1.1.2</version>
                    <scope>import</scope>
                    <type>pom</type>
                </dependency>
            </dependencies>
        </dependencyManagement>
        ```

    2. 实施下游应用程序

        我们的下游应用程序将有一个返回价格数据的端点。

        首先，让我们为 Price 类建模：

        ```java
        public class Price {
            private long productId;
            private double priceAmount;
            private double discount;
        }
        ```

        接下来，让我们使用获取价格端点实现 PriceController：

        ```java
        @RestController(value = "/price")
        public class PriceController {

            private static final Logger LOGGER = LoggerFactory.getLogger(PriceController.class);

            @Autowired
            private PriceRepository priceRepository;

            @GetMapping(path = "/{id}")
            public Price getPrice(@PathVariable("id") long productId) {
                LOGGER.info("Getting Price details for Product Id {}", productId);
                return priceRepository.getPrice(productId);
            }
        }
        ```

        然后，我们将在 PriceRepository 中实现 getPrice 方法：

        ```java
        public Price getPrice(Long productId){
            LOGGER.info("Getting Price from Price Repo With Product Id {}", productId);
            if(!priceMap.containsKey(productId)){
                LOGGER.error("Price Not Found for Product Id {}", productId);
                throw new PriceNotFoundException("Price Not Found");
            }
            return priceMap.get(productId);
        }
        ```

    3. 实现上游应用程序

        上游应用程序也将有一个获取产品详细信息的端点，并与上述获取价格端点集成。

        首先，让我们实现 Product 类：

        ```java
        public class Product {
            private long id;
            private String name;
            private Price price;
        }
        ```

        然后，让我们实现 ProductController 类，并为其添加一个用于获取产品的端点：

        ```java
        @RestController
        public class ProductController {

            private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

            @Autowired
            private PriceClient priceClient;

            @Autowired
            private ProductRepository productRepository;

            @GetMapping(path = "/product/{id}")
            public Product getProductDetails(@PathVariable("id") long productId){
                LOGGER.info("Getting Product and Price Details with Product Id {}", productId);
                Product product = productRepository.getProduct(productId);
                product.setPrice(priceClient.getPrice(productId));
                return product;
            }
        }
        ```

        接下来，我们将在 ProductRepository 中实现 getProduct 方法：

        ```java
        public Product getProduct(Long productId){
            LOGGER.info("Getting Product from Product Repo With Product Id {}", productId);
            if(!productMap.containsKey(productId)){
                LOGGER.error("Product Not Found for Product Id {}", productId);
                throw new ProductNotFoundException("Product Not Found");
            }
            return productMap.get(productId);
        }
        ```

        最后，让我们在 PriceClient 中实现 getPrice 方法：

        ```java
        public Price getPrice(@PathVariable("id") long productId){
            LOGGER.info("Fetching Price Details With Product Id {}", productId);
            String url = String.format("%s/price/%d", baseUrl, productId);
            ResponseEntity<Price> price = restTemplate.getForEntity(url, Price.class);
            return price.getBody();
        }
        ```

4. 使用 OpenTelemetry 配置 Spring Boot

    OpenTelemetry 提供了一个称为 Otel 收集器的收集器，用于处理遥测数据并将其导出到任何可观察性后端，如 Jaeger、[Prometheus](https://www.baeldung.com/spring-boot-self-hosted-monitoring) 等。

    使用一些 Spring Sleuth 配置就可以将轨迹导出到 Otel 收集器。

    1. 配置 Spring Sleuth

        我们需要用 Otel 端点配置应用程序，以发送遥测数据。

        让我们在 application.properties 中包含 Spring Sleuth 配置：

        ```properties
        spring.sleuth.otel.config.trace-id-ratio-based=1.0
        spring.sleuth.otel.exporter.otlp.endpoint=http://collector:4317
        ```

        trace-id-ratio-based 属性定义了所收集跨度的采样比率。数值 1.0 表示导出所有跨度。

    2. 配置 OpenTelemetry 收集器

        Otel 收集器是 OpenTelemetry 跟踪的引擎。它由接收器、处理器和输出器组件组成。还有一个可选的扩展组件，可帮助进行健康检查、服务发现或数据转发。扩展组件不涉及遥测数据的处理。

        为了快速启动 Otel 服务，我们将使用托管在 14250 端口的 Jaeger 后端端点。

        让我们用 Otel 管道阶段配置 otel-config.yml：

        ```yml
        receivers:
        otlp:
            protocols:
            grpc:
            http:

        processors:
        batch:

        exporters:
        logging:
            loglevel: debug
        jaeger:
            endpoint: jaeger-service:14250
            tls:
            insecure: true

        service:
        pipelines:
            traces:
            receivers:  [ otlp ]
            processors: [ batch ]
            exporters:  [ logging, jaeger ]
        ```

        需要注意的是，上述处理器配置为可选配置，默认情况下不启用。处理器批处理选项有助于更好地压缩数据，减少传输数据所需的外发连接数。

        此外，我们还需要注意，接收器配置了 GRPC 和 HTTP 协议。

5. 运行应用程序

    现在我们将配置并运行整个设置、应用程序和 Otel 收集器。

    1. 在应用程序中配置 Dockerfile

        让我们为产品服务执行 Dockerfile：

        ```yml
        FROM adoptopenjdk/openjdk11:alpine
        COPY target/spring-cloud-open-telemetry1-1.0.0-SNAPSHOT.jar spring-cloud-open-telemetry.jar
        EXPOSE 8080
        ENTRYPOINT ["java","-jar","/spring-cloud-open-telemetry.jar"]
        ```

        我们应该注意到，价格服务的 Dockerfile 基本相同。

    2. 使用 Docker Compose 配置服务

        现在，让我们用整个设置来配置 [docker-compose.yml](https://www.baeldung.com/ops/docker-compose)：

        ```yml
        version: "4.0"

        services:
        product-service:
            build: spring-cloud-open-telemetry1/
            ports:
            - "8080:8080"

        price-service:
            build: spring-cloud-open-telemetry2/
            ports:
            - "8081"

        collector:
            image: otel/opentelemetry-collector:0.72.0
            command: [ "--config=/etc/otel-collector-config.yml" ]
            volumes:
            - ./otel-config.yml:/etc/otel-collector-config.yml
            ports:
            - "4317:4317"
            depends_on:
            - jaeger-service

        jaeger-service:
            image: jaegertracing/all-in-one:latest
            ports:
            - "16686:16686"
            - "14250"
        ```

        现在让我们通过 docker-compose 运行这些服务：

        `$ docker-compose up`

    3. 验证正在运行的 Docker 服务

        除了 product-service 和 price-service，我们还在整个设置中添加了 collector-service 和 jaeger-service。上述产品服务和价格服务使用收集器服务的 4317 端口发送跟踪数据。收集器服务反过来依靠 jaeger-service 端点将跟踪数据导出到 Jaeger 后端。

        对于 jaeger-service，我们使用的是 jaegertracing/all-in-one 镜像，其中包括后端和用户界面组件。

        让我们用 [docker容器](https://www.baeldung.com/ops/docker-list-containers) 命令来验证服务的状态：

        `$ docker container ls --format "table {{.ID}}\t{{.Names}}\t{{.Status}}\t{{.Ports}}"`

        ```log
        CONTAINER ID   NAMES                                           STATUS         PORTS
        7b874b9ee2e6   spring-cloud-open-telemetry-collector-1         Up 5 minutes   0.0.0.0:4317->4317/tcp, 55678-55679/tcp
        29ed09779f98   spring-cloud-open-telemetry-jaeger-service-1    Up 5 minutes   5775/udp, 5778/tcp, 6831-6832/udp, 14268/tcp, 0.0.0.0:16686->16686/tcp, 0.0.0.0:61686->14250/tcp
        75bfbf6d3551   spring-cloud-open-telemetry-product-service-1   Up 5 minutes   0.0.0.0:8080->8080/tcp, 8081/tcp
        d2ca1457b5ab   spring-cloud-open-telemetry-price-service-1     Up 5 minutes   0.0.0.0:61687->8081/tcp
        ```

6. 监控收集器中的跟踪

    遥测收集器工具（如 Jaeger）提供了监控请求的前端应用程序。我们可以实时或稍后查看请求跟踪。

    让我们监控请求成功和失败时的跟踪。

    1. 监控请求成功时的跟踪

        首先，让我们调用产品端点 <http://localhost:8080/product/100003> 。

        请求将显示一些日志：

        ```log
        spring-cloud-open-telemetry-price-service-1 | 2023-01-06 19:03:03.985 INFO [price-service,825dad4a4a308e6f7c97171daf29041a,346a0590f545bbcf] 1 --- [nio-8081-exec-1] c.b.opentelemetry.PriceRepository : Getting Price from Price With Product Id 100003
        spring-cloud-open-telemetry-product-service-1 | 2023-01-06 19:03:04.432 INFO [,825dad4a4a308e6f7c97171daf29041a,fb9c54565b028eb8] 1 --- [nio-8080-exec-1] c.b.opentelemetry.ProductRepository : Getting Product from Product Repo With Product Id 100003
        spring-cloud-open-telemetry-collector-1 | Trace ID : 825dad4a4a308e6f7c97171daf29041a
        ```

        Spring Sleuth 会自动配置 ProductService，将跟踪 ID 附加到当前线程，并作为 HTTP 标头附加到下游 API 调用。PriceService 也会自动在线程上下文和日志中包含相同的跟踪 ID。Otel 服务将使用此跟踪 id 来确定跨服务的请求流。

        不出所料，上述跟踪 id ....f29041a 在 PriceService 和 ProductService 日志中都是相同的。

        让我们来看看托管在 16686 端口的积家用户界面中的整个请求跨度时间线，显示了请求流的时间轴，并包含表示请求的元数据。

    2. 请求失败时的监控跟踪

        设想一下下游服务抛出异常导致请求失败的情况。

        我们将再次利用相同的用户界面来分析根本原因。

        让我们使用产品端点 /product/100005 调用来测试上述场景，此时下游应用程序中不存在产品。

        现在，让我们可视化失败的请求跨度，我们可以将请求追溯到产生错误的最终 API 调用。

7. 结论

    在本文中，我们了解了 OpenTelemetry 如何帮助微服务实现可观察性模式的标准化。

    我们还通过一个示例了解了如何使用 OpenTelemetry 配置 Spring Boot 应用程序。最后，我们在收集器中跟踪了 API 请求流。
