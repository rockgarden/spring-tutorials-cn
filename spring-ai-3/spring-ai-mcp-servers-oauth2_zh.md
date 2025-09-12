# [使用 OAuth2 保护 Spring AI MCP 服务器](https://www.baeldung.com/spring-ai-mcp-servers-oauth2)

人工智能    Spring AI

Spring Security

OAuth Spring

1. 简介

    MCP（模型上下文协议）是由 Anthropic 提出的一项开放标准，旨在以结构化的方式让 AI 模型与外部工具、数据源和服务进行交互。MCP 服务器是一种轻量级后端应用程序，通过 MCP 接口公开特定功能，例如访问文件、查询数据库或调用 API。

    为了使 MCP 服务器具备生产就绪能力，我们可能会考虑将其分离为独立的应用程序。这有助于我们单独扩展和维护它们。然而，由于这些服务器可能处理敏感任务，我们需要保护其端点，并限制对可信客户端的访问。

    这就是 [OAuth2](https://www.baeldung.com/spring-security-oauth-resource-server) 发挥作用的地方。OAuth2 是一种用于安全、基于令牌的 API 访问委托的知名协议。我们的 MCP 服务器不直接管理用户凭据，而是信任由中央授权服务器颁发的有效访问令牌。我们可以使用 OAuth2 根据范围和角色授予或限制客户端应用程序对特定 MCP 功能的访问。

    在本教程中，我们将学习如何在 Spring AI 应用程序中使用 OAuth2 保护 MCP 服务器。

2. 依赖项

    首先，让我们添加我们将用于获取 HTTP 和 SSE 传输以及核心 MCP 支持的 Spring AI MCP 服务器依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mcp-server-webmvc-spring-boot-starter</artifactId>
    </dependency>
    ```

    现在，让我们添加 OAuth 授权服务器依赖项。我们将使用它来颁发 OAuth2 访问令牌：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
        <version>3.3.3</version>
    </dependency>
    ```

    最后，让我们添加 Spring Security 的资源服务器依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        <version>3.4.2</version>
    </dependency>
    ```

    有了这个依赖项，我们将确保我们的 MCP 端点拒绝无效或缺失的 Bearer 令牌。

3. 创建股票信息 MCP 服务器

    现在，让我们实现一个简单的 MCP 服务器。在其中，我们将有一个返回所提供符号股票价格的工具。

    让我们创建一个 `StockInformationHolder` 类：

    ```java
    public class StockInformationHolder {
        @Tool(description = "根据公司代码获取股票价格")
        public String getStockPrice(@ToolParam String symbol) {
            if ("AAPL".equalsIgnoreCase(symbol)) {
                return "AAPL: $150.00";
            } else if ("GOOGL".equalsIgnoreCase(symbol)) {
                return "GOOGL: $2800.00";
            } else {
                return symbol + ": 数据不可用";
            }
        }
    }
    ```

    在这里，我们有 `getStockPrice()` 方法，用于返回已知公司的股票价格，对于未知符号则返回默认响应。该方法被 `@Tool` 注解标记，因此将用于构建工具定义。此外，我们还用 `@ToolParam` 注解标记了 `symbol` 参数，以确保在构建工具定义时会考虑它。

    接下来，让我们创建 `McpServerConfiguration` 类：

    ```java
    @Configuration
    public class McpServerConfiguration {

        @Bean
        public ToolCallbackProvider stockTools() {
            return MethodToolCallbackProvider
            .builder()
            .toolObjects(new StockInformationHolder())
            .build();
        }
    }
    ```

    在这里，我们提供了 [`ToolCallbackProvider`](https://docs.spring.io/spring-ai/docs/1.0.x/api/org/springframework/ai/tool/ToolCallbackProvider.html) Bean。我们通过附加 `StockInformationHolder` 类来构建它。现在，我们已经拥有一个现成可用的 MCP 服务器，可以启动我们的应用程序并通过调用 `GET /sse` 端点打开 SSE 连接。要向我们的 MCP 服务器发送消息，让我们使用 `POST /mcp/message` 端点和 JSON 正文：

    ```json
    {
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
        "name": "getStockPrice",
        "arguments": {
        "arg0": "AAPL"
        }
    }
    }
    ```

    我们为 `method` 指定了 “tools/call”，表示我们想要调用我们的工具功能。在 `params` 对象中，我们向指定方法发送参数，包括默认为注解方法名称的工具名称以及参数映射。

4. 添加安全配置

    现在，让我们保护我们的 MCP 服务器。首先，我们将使用 `application.yml` 文件配置我们的授权服务器：

    ```yaml
    spring:
    security:
        oauth2:
        authorizationserver:
            client:
            oidc-client:
                registration:
                client-id: mcp-client
                client-secret: "{noop}secret"
                client-authentication-methods: client_secret_basic
                authorization-grant-types: client_credentials
    ```

    我们指定了请求令牌的客户端应用程序的唯一标识符。对于共享密钥，我们使用了 `{noop}secret`，这只适用于演示目的。`{noop}` 前缀告诉 Spring 不要对密钥进行哈希处理，使其适用于测试场景。

    接下来，让我们创建 `McpServerSecurityConfiguration` 类：

    ```java
    @Configuration
    @EnableWebSecurity
    public class McpServerSecurityConfiguration {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/mcp/**").authenticated()
                .requestMatchers("/sse").authenticated()
                .anyRequest().permitAll())
            .with(OAuth2AuthorizationServerConfigurer.authorizationServer(), Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .csrf(CsrfConfigurer::disable)
            .cors(Customizer.withDefaults())
            .build();
        }
    }
    ```

    在这里，我们允许所有经过身份验证的请求访问 `/mcp` 和 `/sse` 端点。所有其他端点将保持开放。这种方法简化了对身份验证端点的访问。然而，在实际应用中，我们会更仔细地限制访问。

    我们使用 `authorizationServer()` 和 `oauth2ResourceServer()` 方法来配置应用程序。此设置表明应用程序提供访问令牌端点。它还充当资源服务器，使用 JWT 令牌验证传入的请求。

5. 测试受保护的 MCP 服务器

    现在，我们需要测试我们受保护的 MCP 服务器。让我们创建 `McpServerOAuth2LiveTest` 类：

    ```java
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class McpServerOAuth2LiveTest {

        private static final Logger log = LoggerFactory.getLogger(McpServerOAuth2LiveTest.class);

        @LocalServerPort
        private int port;

        private WebClient webClient;

        @BeforeEach
        void setup() {
            webClient = WebClient.create("http://localhost:" + port);
        }
    }
    ```

    我们在随机端口上启动我们的应用程序并初始化 `WebClient`。然后，让我们调用 `/sse` 端点以打开服务器发送事件连接：

    ```java
    Flux<String> eventStream = webClient.get()
    .uri("/sse")
    .header("Authorization", obtainAccessToken())
    .accept(MediaType.TEXT_EVENT_STREAM)
    .retrieve()
    .bodyToFlux(String.class);

    eventStream.subscribe(
        data -> {
            log.info("收到响应: {}", data);
            if (!isRequestMessage(data)) {
                assertThat(data).containsSequence("AAPL", "$150");
            }
        },
        error -> log.error("流错误: {}", error.getMessage()),
        () -> log.info("流完成")
    );
    ```

    我们断言响应消息包含预期的数据。接下来，让我们向 `/mcp/message` 端点发送请求：

    ```java
    Flux<String> sendMessage = webClient.post()
    .uri("/mcp/message")
    .header("Authorization", obtainAccessToken())
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.TEXT_EVENT_STREAM)
    .bodyValue("""
        {
            "jsonrpc": "2.0",
            "id": "1",
            "method": "tools/call",
            "params": {
                "name": "getStockPrice",
                "arguments": {
                    "arg0": "AAPL"
                }
            }
        }
        """)
    .retrieve()
    .bodyToFlux(String.class);
    ```

    我们发送一个请求以检索 AAPL 的股票价格。两个请求都包含 `Authorization` 头。现在，让我们实现获取访问令牌的方法：

    ```java
    public String obtainAccessToken() {
        String clientId = "mcp-client";
        String clientSecret = "secret";
        String basicToken = Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return "Bearer " + webClient.post()
        .uri("/oauth2/token")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .header(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
        .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
        .retrieve()
        .bodyToMono(JsonNode.class)
        .map(node -> node.get("access_token").asText())
        .block(Duration.ofSeconds(5));
    }
    ```

    执行后，我们可以看到成功收到了响应数据。这证实了我们已通过安全过滤器。

6. 结论

    在本教程中，我们使用 OAuth2 在 Spring AI 应用程序中保护了我们的 MCP 服务器。为了保护关键的 MCP 端点，OAuth2 通过 Spring Boot 无缝集成。此外，此设置是灵活的，可以进一步扩展。例如，我们可以引入基于角色和范围的访问控制，以将特定工具或操作限制给某些客户端。

    在生产环境中，我们可能会集成像 Keycloak 或 Okta 这样的完整功能的身份提供商。除此之外，我们可以增强我们的令牌，添加自定义声明或范围，以控制对 MCP 平台内单个工具的访问。
