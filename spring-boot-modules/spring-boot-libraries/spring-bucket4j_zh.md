# [使用Bucket4j限制Spring API的速率](https://www.baeldung.com/spring-bucket4j)

1. 一览表

    在本教程中，我们将重点介绍如何使用[Bucket4j](https://github.com/vladimir-bukhtoyarov/bucket4j)来限制Spring REST API。

    我们将探索API速率限制，了解Bucket4j，然后在Spring应用程序中研究几种速率限制REST API的方法。

2. API速率限制

    速率限制是一种[限制对API访问](https://cloud.google.com/solutions/rate-limiting-strategies-techniques)的策略。它限制了客户端在一定时间范围内可以进行的API调用次数。这有助于保护API免受过度使用，包括无意和恶意的过度使用。

    速率限制通常通过跟踪IP地址或以更特定业务的方式（如API密钥或访问令牌）应用于API。作为API开发人员，当客户端达到极限时，我们有几个选项：

    - 将请求排队，直到剩余时间段过去
    - 立即允许请求，但为此请求收取额外费用
    - 拒绝请求（HTTP 429请求太多）

3. Bucket4j速率限制库

    1. Bucket4j是什么？

        Bucket4j是一个基于[token-bucket](https://en.wikipedia.org/wiki/Token_bucket)算法的Java速率限制库。Bucket4j是一个线程安全库，可以在独立JVM应用程序或集群环境中使用。它还通过[JCache（JSR107）](https://www.jcp.org/en/jsr/detail?id=107)规范支持内存内或分布式缓存。

    2. 令牌桶算法

        让我们在API速率限制的背景下直观地查看算法。

        假设我们有一个存储桶，其容量被定义为它可以容纳的令牌数量。每当消费者想要访问API端点时，它必须从存储桶中获取令牌。如果可用，我们会从存储桶中删除令牌，并接受请求。相反，如果存储桶没有任何令牌，我们会拒绝请求。

        由于请求正在消耗tokens，我们也以某种固定的速度补充它们，这样我们永远不会超过桶的容量。

        让我们考虑一个速率限制为每分钟100个请求的API。我们可以创建一个容量为100的桶，每分钟充值率为100个tokens。

        如果我们收到70个请求，这比给定一分钟的可用令牌少，我们将在下一分钟开始时只添加30个令牌，以使存储桶达到容量。另一方面，如果我们在40秒内用完所有tokens，我们将等待20秒再重新填充桶。

4. 开始使用Bucket4j

    1. Maven配置

        让我们从将bucket4j依赖项添加到我们的pom.xml开始：

        ```xml
        <dependency>
            <groupId>com.bucket4j</groupId>
            <artifactId>bucket4j-core</artifactId>
            <version>8.1.0</version>
        </dependency>
        ```

    2. 术语

        在我们研究如何使用Bucket4j之前，我们将简要讨论一些核心类，以及它们如何表示令牌桶算法的正式模型中的不同元素。

        Bucket接口表示具有最大容量的令牌存储[桶](https://javadoc.io/doc/com.github.vladimir-bukhtoyarov/bucket4j-core/4.10.0/io/github/bucket4j/Bucket.html)。它提供了用于消费令牌的[tryConsume和tryConsumeAndReturnRemaining](https://javadoc.io/doc/com.github.vladimir-bukhtoyarov/bucket4j-core/4.10.0/io/github/bucket4j/Bucket.html#tryConsumeAndReturnRemaining-long-)等方法。如果请求符合限制，并且令牌被消耗，这些方法将返回消耗结果为真。

        [带宽](https://javadoc.io/doc/com.github.vladimir-bukhtoyarov/bucket4j-core/4.10.0/io/github/bucket4j/Bandwidth.html)类是存储桶的关键构建块，因为它定义了存储桶的极限。我们使用带宽来配置桶的容量和填充速率。

        [补充](https://javadoc.io/doc/com.github.vladimir-bukhtoyarov/bucket4j-core/4.10.0/io/github/bucket4j/Refill.html)类用于定义将令牌添加到存储桶的固定速率。我们可以将速率配置为在给定时间段内添加的令牌数量。例如，每秒10个桶或每5分钟200个tokens，以前后。

        Bucket中的tryConsumeAndReturnRemaining方法返回[ConsumindProbe](https://javadoc.io/doc/com.github.vladimir-bukhtoyarov/bucket4j-core/4.10.0/io/github/bucket4j/ConsumptionProbe.html)。ConsumptionProbe包含存储桶的状态，以及消耗结果，例如剩余的令牌，或请求的令牌在存储桶中再次可用之前的剩余时间。

    3. 基本用法

        让我们来测试一些基本的速率限制模式。

        对于每分钟10个请求的速率限制，我们将创建一个容量为10的桶，填充率为每分钟10个令牌：

        ```java
        Refill refill = Refill.intervally(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(10, refill);
        Bucket bucket = Bucket.builder()
            .addLimit(limit)
            .build();
        for (int i = 1; i <= 10; i++) {
            assertTrue(bucket.tryConsume(1));
        }
        assertFalse(bucket.tryConsume(1));
        ```

        补充。在时间窗口开始时间隔地重新填充桶，在这种情况下，在分钟开始时为10个令牌。

        接下来，让我们看看在行动中的补充。

        我们将设置每2秒1个tokens的充值率，并限制我们的请求，以遵守速率限制：

        ```java
        Bandwidth limit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(2)));
        Bucket bucket = Bucket.builder()
            .addLimit(limit)
            .build();
        assertTrue(bucket.tryConsume(1));     // first request
        Executors.newScheduledThreadPool(1)   // schedule another request for 2 seconds later
            .schedule(() -> assertTrue(bucket.tryConsume(1)), 2, TimeUnit.SECONDS); 
        ```

        假设我们的速率限制为每分钟10个请求。与此同时，我们可能希望避免在前5秒内耗尽所有tokens的峰值。Bucket4j允许我们在同一个存储桶上设置多个限制（带宽）。让我们添加另一个限制，在20秒的时间窗口中只允许5个请求：

        ```java
        Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(20))))
            .build();
        for (int i = 1; i <= 5; i++) {
            assertTrue(bucket.tryConsume(1));
        }
        assertFalse(bucket.tryConsume(1));
        ```

5. 使用Bucket4j限制Spring API的速率

    让我们使用Bucket4j在Spring REST API中应用速率限制。

    1. 面积计算器API

        我们将实现一个简单但非常受欢迎的区域计算器REST API。目前，它计算并返回给定尺寸的矩形面积：

        ```java
        @RestController
        class AreaCalculationController {
            @PostMapping(value = "/api/v1/area/rectangle")
            public ResponseEntity<AreaV1> rectangle(@RequestBody RectangleDimensionsV1 dimensions) {
                return ResponseEntity.ok(new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth()));
            }
        }
        ```

        让我们确保我们的API已启动并运行：

        ```bash
        $ curl -X POST http://localhost:9001/api/v1/area/rectangle \
            -H "Content-Type: application/json" \
            -d '{ "length": 10, "width": 12 }'

        { "shape":"rectangle","area":120.0 }
        ```

    2. 应用速率限制

        现在我们将引入一个天真的速率限制，允许API每分钟20个请求。换句话说，如果API在1分钟的时间窗口内已收到20个请求，则API会拒绝请求。

        让我们修改我们的控制器来创建一个存储桶并添加限制（带宽）：

        ```java
        @RestController
        class AreaCalculationController {

            private final Bucket bucket;

            public AreaCalculationController() {
                Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
                this.bucket = Bucket.builder()
                    .addLimit(limit)
                    .build();
            }
            //..
        }
        ```

        在此API中，我们可以通过使用methodtryConsume从存储桶中消耗令牌来检查请求是否允许。如果我们已达到限制，我们可以通过以HTTP 429请求太多状态来拒绝请求：

        ```java
        public ResponseEntity<AreaV1> rectangle(@RequestBody RectangleDimensionsV1 dimensions) {
            if (bucket.tryConsume(1)) {
                return ResponseEntity.ok(new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth()));
            }

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        ```

        ```log
        # 21st request within 1 minute
        $ curl -v -X POST http://localhost:9001/api/v1/area/rectangle \
            -H "Content-Type: application/json" \
            -d '{ "length": 10, "width": 12 }'

        < HTTP/1.1 429
        ```

    3. API客户端和定价计划

        现在我们有一个天真的速率限制，可以限制API请求。接下来，我们将介绍更多以商业为中心的费率限制的定价计划。

        定价计划帮助我们将API货币化。让我们假设我们为我们的API客户端制定了以下计划：

        - 免费：每个API客户端每小时20个请求
        - 基本：每个API客户端每小时40个请求
        - 专业：每个API客户端每小时100个请求

        每个API客户端都会获得一个唯一的API密钥，他们必须与每个请求一起发送。这有助于我们确定与API客户端关联的定价计划。

        让我们定义每个定价计划的费率限制（带宽）：

        ```java
        enum PricingPlan {
            FREE {
                Bandwidth getLimit() {
                    return Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1)));
                }
            },
            BASIC {
                Bandwidth getLimit() {
                    return Bandwidth.classic(40, Refill.intervally(40, Duration.ofHours(1)));
                }
            },
            PROFESSIONAL {
                Bandwidth getLimit() {
                    return Bandwidth.classic(100, Refill.intervally(100, Duration.ofHours(1)));
                }
            };
            //..
        }
        ```

        然后，让我们添加一种方法，从给定的API密钥中解析定价计划：

        ```java
        enum PricingPlan {

            static PricingPlan resolvePlanFromApiKey(String apiKey) {
                if (apiKey == null || apiKey.isEmpty()) {
                    return FREE;
                } else if (apiKey.startsWith("PX001-")) {
                    return PROFESSIONAL;
                } else if (apiKey.startsWith("BX001-")) {
                    return BASIC;
                }
                return FREE;
            }
            //..
        }
        ```

        接下来，我们需要存储每个API密钥的存储桶，并检索存储桶以限制速率：

        ```java
        class PricingPlanService {

            private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

            public Bucket resolveBucket(String apiKey) {
                return cache.computeIfAbsent(apiKey, this::newBucket);
            }

            private Bucket newBucket(String apiKey) {
                PricingPlan pricingPlan = PricingPlan.resolvePlanFromApiKey(apiKey);
                return Bucket.builder()
                    .addLimit(pricingPlan.getLimit())
                    .build();
            }
        }
        ```

        现在，我们每个API密钥都有一个存储桶的内存存储。让我们修改我们的控制器以使用PricingPlanService：

        ```java
        @RestController
        class AreaCalculationController {

            private PricingPlanService pricingPlanService;

            public ResponseEntity<AreaV1> rectangle(@RequestHeader(value = "X-api-key") String apiKey,
                @RequestBody RectangleDimensionsV1 dimensions) {

                Bucket bucket = pricingPlanService.resolveBucket(apiKey);
                ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
                if (probe.isConsumed()) {
                    return ResponseEntity.ok()
                        .header("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()))
                        .body(new AreaV1("rectangle", dimensions.getLength() * dimensions.getWidth()));
                }
                
                long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill))
                    .build();
            }
        }
        ```

        让我们来了解一下变化。API客户端使用X-api-key请求标头发送API密钥。我们使用PricingPlanService获取此API密钥的存储桶，并通过使用存储桶中的令牌来检查是否允许请求。

        为了增强API的客户端体验，我们将使用以下附加响应标头来发送有关速率限制的信息：

        - X-Rate-Limit-Remaining：当前时间窗口中剩余的令牌数量
        - X-Rate-Limit-Retry-After-Seconds：剩余时间，以秒为单位，直到桶被重新填充

        我们可以调用ConsuminingProbe方法getRemainingTokens和getNanosToWaitForRefill，分别获取存储桶中剩余令牌的数量和下一次重新填充的剩余时间。如果我们能够成功使用令牌，getNanosToWaitForRefill方法将返回0。

        让我们调用API：

        ```log
        ## successful request

        $ curl -v -X POST <http://localhost:9001/api/v1/area/rectangle> \
            -H "Content-Type: application/json" -H "X-api-key:FX001-99999" \
            -d '{ "length": 10, "width": 12 }'

        < HTTP/1.1 200
        < X-Rate-Limit-Remaining: 11
        {"shape":"rectangle","area":120.0}

        ## rejected request

        $ curl -v -X POST <http://localhost:9001/api/v1/area/rectangle> \
            -H "Content-Type: application/json" -H "X-api-key:FX001-99999" \
            -d '{ "length": 10, "width": 12 }'

        < HTTP/1.1 429
        < X-Rate-Limit-Retry-After-Seconds: 583
        ```

        5.4。使用SpringMVC拦截器

        假设我们现在必须添加一个新的API端点，该端点在给定三角形的高度和基数下计算并返回三角形的面积：

        ```java
        @PostMapping(value = "/triangle")
        public ResponseEntity<AreaV1> triangle(@RequestBody TriangleDimensionsV1 dimensions) {
            return ResponseEntity.ok(new AreaV1("triangle", 0.5d *dimensions.getHeight()* dimensions.getBase()));
        }
        ```

        事实证明，我们也需要限制我们的新终点。我们只需从之前的端点复制并粘贴速率限制代码。或者，我们可以使用Spring MVC的HandlerInterceptor将速率限制代码与业务代码分离。

        让我们创建一个RateLimitInterceptor，并在preHandle方法中实现速率限制代码：

        ```java
        public class RateLimitInterceptor implements HandlerInterceptor {

            private PricingPlanService pricingPlanService;

            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
                String apiKey = request.getHeader("X-api-key");
                if (apiKey == null || apiKey.isEmpty()) {
                    response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Header: X-api-key");
                    return false;
                }

                Bucket tokenBucket = pricingPlanService.resolveBucket(apiKey);
                ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);
                if (probe.isConsumed()) {
                    response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                    return true;
                } else {
                    long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                    response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "You have exhausted your API Request Quota"); 
                    return false;
                }
            }
        }
        ```

        最后，我们必须将拦截器添加到拦截器注册表中：

        ```java
        public class Bucket4jRateLimitApp implements WebMvcConfigurer {

            private RateLimitInterceptor interceptor;

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                    .addPathPatterns("/api/v1/area/**");
            }
        }
        ```

        RateLimitInterceptor拦截每个请求到我们的区域计算API端点。

        让我们来试试我们的新端点：

        ```log
        ## successful request

        $ curl -v -X POST <http://localhost:9001/api/v1/area/triangle> \
            -H "Content-Type: application/json" -H "X-api-key:FX001-99999" \
            -d '{ "height": 15, "base": 8 }'

        < HTTP/1.1 200
        < X-Rate-Limit-Remaining: 9
        {"shape":"triangle","area":60.0}

        ## rejected request

        $ curl -v -X POST <http://localhost:9001/api/v1/area/triangle> \
            -H "Content-Type: application/json" -H "X-api-key:FX001-99999" \
            -d '{ "height": 15, "base": 8 }'

        < HTTP/1.1 429
        < X-Rate-Limit-Retry-After-Seconds: 299
        { "status": 429, "error": "Too Many Requests", "message": "You have exhausted your API Request Quota" }
        ```

        看起来我们完成了。我们可以继续添加端点，拦截器将对每个请求应用速率限制。

6. Bucket4j弹簧启动器

    让我们来看看在Spring应用程序中使用Bucket4j的另一种方法。[Bucket4j Spring Boot Starter](https://github.com/MarcGiffing/bucket4j-spring-boot-starter)为Bucket4j提供自动配置，帮助我们通过Spring Boot应用程序属性或配置实现API速率限制。

    一旦我们将Bucket4j启动器集成到我们的应用程序中，我们将有一个完全声明的API速率限制实现，而无需任何应用程序代码。

    1. 速率限制过滤器

        在我们的示例中，我们使用请求标头X-api-key的值作为识别和应用速率限制的密钥。

        Bucket4j Spring Boot Starter提供了几个预定义的配置，用于定义我们的速率限制键：

        - 一个天真(naive)的速率限制过滤器，这是默认的
        - 按IP地址过滤
        - 基于表达式的过滤器

        基于表达式的过滤器使用Spring表达式语言（[SpEL](https://www.baeldung.com/spring-expression-language)）。SpEL提供对根对象的访问，如HttpServletRequest，这些对象可用于在IP地址（getRemoteAddr（））、请求标头（getHeader（'X-api-key'））上构建过滤器表达式。

        该库还支持过滤器表达式中的自定义类，这在文档中进行了讨论。

    2. Maven配置

        让我们从将bucket4j-spring-boot-starter依赖项添加到我们的pom.xml开始：

        ```xml
        <dependency>
            <groupId>com.giffing.bucket4j.spring.boot.starter</groupId>
            <artifactId>bucket4j-spring-boot-starter</artifactId>
            <version>0.8.1</version>
        </dependency>
        ```

        在早期的实现中，我们使用内存映射来存储每个API密钥（消费者）的存储桶。在这里，我们可以使用Spring的缓存抽象来配置内存存储，如咖啡因或番石榴。

        让我们添加缓存依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.cache</groupId>
            <artifactId>cache-api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
            <version>2.8.2</version>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>jcache</artifactId>
            <version>2.8.2</version>
        </dependency>
        ```

        注意：我们还添加了[jcache](https://mvnrepository.com/artifact/javax.cache/cache-api)依赖项，以符合Bucket4j的缓存支持。

        我们必须记住通过向任何配置类添加@EnableCaching注释来启用缓存功能。

    3. 应用程序配置

        让我们配置我们的应用程序以使用Bucket4j启动库。首先，我们将配置咖啡因缓存，以存储内存中的API密钥和存储桶：

        ```yml
        spring:
        cache:
            cache-names:
            - rate-limit-buckets
            caffeine:
            spec: maximumSize=100000,expireAfterAccess=3600s
        ```

        接下来，让我们配置Bucket4j：

        ```yml
        bucket4j:
        enabled: true
        filters:

        - cache-name: rate-limit-buckets
            url: /api/v1/area.*
            strategy: first
            http-response-body: "{ \"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"You have exhausted your API Request Quota\" }"
            rate-limits:
        - cache-key: "getHeader('X-api-key')"
            execute-condition: "getHeader('X-api-key').startsWith('PX001-')"
            bandwidths:
            - capacity: 100
                time: 1
                unit: hours
        - cache-key: "getHeader('X-api-key')"
            execute-condition: "getHeader('X-api-key').startsWith('BX001-')"
            bandwidths:
            - capacity: 40
                time: 1
                unit: hours
        - cache-key: "getHeader('X-api-key')"
            bandwidths:
            - capacity: 20
                time: 1
                unit: hours
        ```

        那么，我们刚刚配置了什么？

        - bucket4j.enabled=true – 启用Bucket4j自动配置
        - bucket4j.filters.cache-name – 从缓存中获取API密钥的存储桶
        - bucket4j.filters.url – 表示应用速率限制的路径表达式
        - bucket4j.filters.strategy=first – 停止在第一个匹配速率限制配置
        - bucket4j.filters.rate-limits.cache-key–使用Spring表达式语言（SpEL）检索密钥
        - bucket4j.filters.rate-limits.execute-condition – 决定是否使用SpEL执行速率限制
        - bucket4j.filters.rate-limits.bandwidths – 定义Bucket4j速率限制参数

        我们用顺序评估的速率限制配置列表取代了PricingPlanService和RateLimitInterceptor。

        让我们试试吧：

        ```log
        ## successful request

        $ curl -v -X POST <http://localhost:9000/api/v1/area/triangle> \
            -H "Content-Type: application/json" -H "X-api-key:FX001-99999" \
            -d '{ "height": 20, "base": 7 }'

        < HTTP/1.1 200
        < X-Rate-Limit-Remaining: 7
        {"shape":"triangle","area":70.0}

        ## rejected request

        $ curl -v -X POST <http://localhost:9000/api/v1/area/triangle> \
            -H "Content-Type: application/json" -H "X-api-key:FX001-99999" \
            -d '{ "height": 7, "base": 20 }'

        < HTTP/1.1 429
        < X-Rate-Limit-Retry-After-Seconds: 212
        { "status": 429, "error": "Too Many Requests", "message": "You have exhausted your API Request Quota" }
        ```

7. 结论

    在本文中，我们演示了使用Bucket4j来限制速率的Spring API的几种不同方法。要了解更多信息，请务必查看官方[文档](https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/README.md)。
