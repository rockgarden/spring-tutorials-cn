# [Spring AI 中的聊天记忆](https://www.baeldung.com/spring-ai-chat-memory)

人工智能 Spring AI

Spring Boot

OpenAI  

1. 概述

    与 AI 应用对话时，我们通常需要类似人类的交互体验。因此，有必要在与 LLM 模型对话时维持上下文连贯性——Spring AI 通过其**聊天记忆（Chat Memory）**功能满足了这一需求。

    在本教程中，我们将探索 Spring AI 提供的不同聊天记忆选项，并演示如何将其集成到聊天客户端中。

2. 聊天记忆

    大型语言模型（LLM）本身是无状态的，不会记忆任何内容。每次发送给 LLM 的提示都被视为独立查询，模型不会记住之前的任何消息。

    在 AI 应用中，保留之前的对话记录对于让 LLM 生成有意义的回复至关重要。这就是聊天记忆发挥作用的地方，它提供：

    - **上下文理解** —— 使 LLM 能够基于整个对话生成回复。
    - **个性化体验** —— 基于聊天记忆提供定制化响应。
    - **持久化能力** —— 根据实现方式，聊天记忆可在多个会话间持久保存。

3. 聊天记忆存储库

    Spring AI 提供了 `ChatMemory` 接口及若干开箱即用的实现，帮助我们轻松将聊天记忆集成到应用中。

    首先，添加 Maven 依赖 `spring-ai-starter-model-openai` 以启用 OpenAI 集成。该依赖会自动传递引入 Spring AI 核心库：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

    创建聊天记忆时，我们需要提供 `ChatMemoryRepository` 的实现，它负责将聊天消息持久化到存储中：

    ```java
    ChatMemoryRepository chatMemoryRepository;

    ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
    ```

    Spring AI 提供了多种聊天记忆存储库实现，可根据项目技术栈选择。下面我们介绍其中两种。

    1. 内存存储库（In-Memory Repository）

        如果我们未显式定义聊天记忆，Spring AI 默认使用内存存储。它内部使用 `ConcurrentHashMap` 存储聊天消息，其中对话 ID 为键，对应消息列表为值：

        ```java
        public final class InMemoryChatMemoryRepository implements ChatMemoryRepository {
            Map<String, List<Message>> chatMemoryStore = new ConcurrentHashMap();

            // 其他方法
        }
        ```

        内存存储库非常简单，适用于无需长期持久化的场景。如需持久化，应选择其他存储方式。

    2. JDBC 存储库

        JDBC 存储库将聊天消息持久化到关系型数据库中。Spring AI 内置支持多种数据库，包括 MySQL、PostgreSQL、SQL Server 和 HSQLDB。

        如需将聊天记忆存储在关系数据库中，需添加以下 Maven 依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-model-chat-memory-repository-jdbc</artifactId>
            <version>1.0.0</version>
        </dependency>
        ```

        每种内置支持的数据库都有对应的方言实现，提供对聊天记忆表的 CRUD 操作 SQL 语句。初始化 `JdbcChatMemoryRepository` 时需指定方言：

        ```java
        JdbcChatMemoryRepositoryDialect dialect = ...; // 选择存储库方言
        ChatMemoryRepository repository = JdbcChatMemoryRepository.builder()
        .jdbcTemplate(jdbcTemplate)
        .dialect(dialect)
        .build();
        ```

        对于未内置支持的数据库，需自行实现 `JdbcChatMemoryRepositoryDialect` 接口，提供各 CRUD 操作的 SQL 语句：

        ```java
        public interface JdbcChatMemoryRepositoryDialect {
            String getSelectMessagesSql();

            String getInsertMessageSql();

            String getSelectConversationIdsSql();

            String getDeleteMessagesSql();
        }
        ```

        Spring AI 中已实现的方言使用标准 SQL，不依赖特定数据库厂商。因此，我们可以直接使用如 `MysqlChatMemoryRepositoryDialect` 等现成实现，无需自定义。

        使用前需初始化数据库模式。对于支持的方言，Spring AI 也提供了建表脚本，位于类路径：`classpath:org/springframework/ai/chat/memory/repository/jdbc`。

4. 将聊天记忆应用于聊天客户端

    Spring AI 通过 `ChatMemoryAutoConfiguration` 自动配置聊天记忆。如果使用内存存储库，则无需显式定义，因为这是默认选项。

    但若想改用 JDBC 存储库，需提供 `ChatMemoryRepository` 的 Bean 方法，以覆盖默认的内存实现：

    ```java
    @Configuration
    public class ChatConfig {
        @Bean
        public ChatMemoryRepository getChatMemoryRepository(JdbcTemplate jdbcTemplate) {
            return JdbcChatMemoryRepository.builder()
            .jdbcTemplate(jdbcTemplate)
            .dialect(new HsqldbChatMemoryRepositoryDialect())
            .build();
        }
    }
    ```

    注意：我们无需显式定义 `ChatMemory` Bean，因为 `ChatMemoryAutoConfiguration` 已经提供了。

    接着，在 Spring Boot 中创建 `ChatService`：

    ```java
    @Component
    @SessionScope
    public class ChatService {
        private final ChatClient chatClient;
        private final String conversationId;

        public ChatService(ChatModel chatModel, ChatMemory chatMemory) {
            this.chatClient = ChatClient.builder(chatModel)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
            this.conversationId = UUID.randomUUID().toString();
        }

        public String chat(String prompt) {
            return chatClient.prompt()
            .user(userMessage -> userMessage.text(prompt))
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
        }
    }
    ```

    在构造函数中，Spring Boot 会自动注入 `ChatMemory` 实现。我们通过 `MessageChatMemoryAdvisor` 将其绑定到 `ChatClient`。

    我们定义 `chat` 方法接收提示并发送给聊天模型。同时，我们将对话 ID 作为聊天顾问参数传入，以便基于当前会话唯一标识对话。

    **重要提示**：必须使用 `@SessionScope` 注解该服务，以确保其实例在多个请求间保持持久。

5. 与 OpenAI 集成

    在演示中，我们将聊天记忆与 OpenAI 集成，并使用内存型 HSQLDB 作为持久化存储。

    在 `application.yml` 中添加属性，配置 OpenAI API 密钥、数据库连接，并在应用启动时初始化模式：

    ```yaml
    spring:
    ai:
        openai:
        api-key: "<YOUR-API-KEY>"

    datasource:
        url: jdbc:hsqldb:mem:chatdb
        driver-class-name: org.hsqldb.jdbc.JDBCDriver
        username: sa
        password:

    sql:
        init:
        mode: always
        schema-locations: classpath:org/springframework/ai/chat/memory/repository/jdbc/schema-hsqldb.sql
    ```

    配置完成后，创建一个 REST 端点，调用之前定义的 `ChatService`：

    ```java
    @RestController
    public class ChatController {
        private final ChatService chatService;

        public ChatController(ChatService chatService) {
            this.chatService = chatService;
        }

        @PostMapping("/chat")
        public ResponseEntity<String> chat(@RequestBody @Valid ChatRequest request) {
            String response = chatService.chat(request.getPrompt());
            return ResponseEntity.ok(response);
        }
    }
    ```

    `ChatRequest` 是一个简单的 DTO，包含提示字符串：

    ```java
    public class ChatRequest {
        @NotNull
        private String prompt;

        // getter 和 setter
    }
    ```

6. 测试运行

    现在，我们可以向 REST 端点发送请求了。我们将使用 [Postman](https://www.baeldung.com/java-postman) 发送请求，并通过 [HTTP Toolkit](https://httptoolkit.com/) 拦截 Spring Boot 应用与 OpenAI 之间的 HTTP 请求，观察其工作原理。

    1. 第一次请求

        在 Postman 中发送请求，要求讲个笑话，观察响应。

        在 HTTP Toolkit 中拦截到的请求如下：

        ```json
        {
        "messages": [
            {
            "content": "Tell me a joke",
            "role": "user"
            }
        ],
        "model": "gpt-4o-mini",
        "stream": false,
        "temperature": 0.7
        }
        ```

        这是一个简单的请求，仅以用户角色发送提示内容。

    2. 第二次请求

        再发送一次请求，对比差异。

        这次拦截到的 HTTP 请求显示，Spring AI 不仅发送了当前提示，还一并发送了之前的提示和回复：

        ```json
        {
        "messages": [
            {
            "content": "Tell me a joke",
            "role": "user"
            },
            {
            "content": "Why did the scarecrow win an award? \n\nBecause he was outstanding in his field!",
            "role": "assistant"
            },
            {
            "content": "Tell me another one",
            "role": "user"
            }
        ],
        "model": "gpt-4o-mini",
        "stream": false,
        "temperature": 0.7
        }
        ```

        在此示例中，我们看到 Spring AI 将整个聊天历史发送给聊天模型。这种方式帮助模型维持对话上下文，使交互体验更自然流畅。

7. 结论

    在本文中，我们学习了 Spring AI 如何通过聊天记忆功能，在多次聊天请求间维持对话历史，从而增强对话体验。

    我们探索了不同的记忆存储库，演示了如何将聊天记忆集成到 Spring AI 与 OpenAI 中，并深入观察了 Spring AI 聊天记忆在后台与 OpenAI 协同工作的机制。
