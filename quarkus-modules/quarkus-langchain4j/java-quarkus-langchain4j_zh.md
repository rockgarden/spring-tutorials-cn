# [使用 Quarkus 和 LangChain4j](https://www.baeldung.com/java-quarkus-langchain4j)

**最后更新**：2025年9月19日

**作者**：Thiago dos Santos Hora
**审阅**：Michal Aibin
**标签**：人工智能、LangChain、OpenAI、Quarkus

1. 概述

    在本教程中，我们将了解 LangChain，这是一个用于开发由大型语言模型（LLM）驱动的应用程序的框架。更具体地说，我们将使用 [langchain4j](https://github.com/langchain4j/langchain4j)，这是一个简化了与 LangChain 集成的 Java 框架，允许开发者轻松地将 LLM 集成到他们的应用程序中。

    该框架在构建检索增强生成（[RAG](https://www.promptingguide.ai/techniques/rag)）应用方面非常受欢迎。在本文中，我们将理解所有这些相关术语，并了解如何利用 Quarkus 来构建此类应用程序。

2. 大型语言模型（LLM）

    大型语言模型（LLM）是使用海量数据训练的 AI 系统。它们的目标是生成类似人类的输出。来自 OpenAI 的 GPT-4 可能是当今最著名的 LLM 之一。LLM 可以回答问题并执行各种自然语言处理任务。

    这些模型是强大的聊天机器人、内容生成、文档分析、视频和图像生成等应用的核心基石。

    1. LangChain

        LangChain 是一个流行的开源框架，可帮助开发者构建由 LLM 驱动的应用程序。它最初是为 Python 开发的，简化了使用 LLM（如 [GPT-4](https://en.wikipedia.org/wiki/GPT-4)、[Llama-3](https://llama.meta.com/) 等）创建多步骤工作流程的过程，例如：

        * **与不同的 LLM 集成**：LangChain 不提供自己的 LLM，而是提供了一个标准接口，用于与许多不同的 LLM 进行交互。
        * **集成外部工具**：LangChain 能够与其他对此类应用程序非常重要的工具集成，如向量数据库、Web 浏览器和其他 API。
        * **管理记忆**：需要对话流的应用程序需要历史记录或上下文持久化。LangChain 具有记忆功能，允许模型在交互过程中“记住”信息。

        LangChain 是 Python 开发者构建 AI 应用程序的首选框架之一。然而，对于 Java 开发者来说，LangChain4j 框架提供了这个强大框架的 Java 风格适配版本。

    2. LangChain4j

        LangChain4j 是一个受 LangChain 启发的 Java 库，旨在帮助使用 LLM 构建 AI 驱动的应用程序。项目创建者旨在填补 Java 框架与众多 Python 和 JavaScript AI 系统选项之间的空白。

        LangChain4j 为 Java 开发者提供了与 LangChain 为 Python 开发者提供的相同工具和灵活性，从而能够开发聊天机器人、摘要引擎或智能搜索系统等应用程序。

        结合其他框架（如 [Quarkus](https://quarkus.io/)），可以快速构建出强大的应用程序。

    3. Quarkus

        Quarkus 是一个专为云原生应用程序设计的 Java 框架。它能够显著减少内存使用量和启动时间，使其非常适合微服务、无服务器架构和 Kubernetes 环境。

        将 Quarkus 整合到您的 AI 应用程序中，可确保您的系统具有可扩展性、高效性，并达到企业级的生产就绪状态。此外，Quarkus 提供了一个易于使用且学习曲线平缓的方式，将 LangChain4j 集成到我们的 Quarkus 应用程序中。

3. 聊天机器人应用

    为了展示 Quarkus 与 LangChain4j 集成的强大功能，我们将创建一个简单的聊天机器人应用来回答有关 Quarkus 的问题。为此，我们将使用 GPT-4。这会对我们有所帮助，因为它使用了互联网上的大量数据进行训练，因此我们不需要自己去训练模型。

    该应用将仅回答有关 Quarkus 的问题，并会记住特定聊天中的先前对话上下文。

    1. 依赖项

        既然我们已经介绍了主要概念和工具，让我们使用 LangChain4j 和 Quarkus 构建我们的第一个应用程序。首先，让我们使用 Rest 和 Redis 创建我们的 Quarkus 应用程序，然后添加以下依赖项：

        ```xml
        <dependency>
            <groupId>io.quarkiverse.langchain4j</groupId>
            <artifactId>quarkus-langchain4j-openai</artifactId>
            <version>0.18.0.CR1</version>
        </dependency>

        <dependency>
            <groupId>io.quarkiverse.langchain4j</groupId>
            <artifactId>quarkus-langchain4j-memory-store-redis</artifactId>
            <version>0.18.0.CR1</version>
        </dependency>
        ```

        第一步是将最新的 `quarkus-langchain4j-openai` 和 `quarkus-langchain4j-memory-store-redis` 依赖项添加到我们的 `pom.xml` 中。

        第一个依赖项将引入与 Quarkus 兼容的 LangChain4J 版本。此外，它还提供了开箱即用的组件，将 LangChain4J 配置为使用 [OpenAI](https://platform.openai.com/docs/models) LLM 模型作为我们服务的骨干。有许多 LLM 可供选择，但为了简单起见，让我们使用当前的默认模型，即 [GPT-4o-mini](https://platform.openai.com/docs/models/gpt-4o-mini)。为了使用此服务，我们需要在 OpenAI 中创建一个 ApiKey。完成此操作后，我们可以继续。稍后我们将讨论第二个依赖项。

    2. 设置

        现在，让我们设置我们的应用程序，使其能够与 GPT-4 和 Redis 通信，以便我们开始实现聊天机器人。为此，我们需要在 Quarkus 中配置一些应用程序属性，因为它具有开箱即用的组件来执行此操作。我们只需将以下属性添加到 `application.properties` 文件中：

        ```properties
        quarkus.langchain4j.openai.api-key=${OPEN_AI_KEY}
        quarkus.langchain4j.openai.organization-id=${OPEN_AI_ORG}

        quarkus.redis.hosts=${REDIS_HOST}
        ```

        Quarkus 还有许多其他对此应用程序有用的配置。请参阅 Quarkus [官方文档](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)；但是，上述配置对于我们的聊天机器人来说已经足够了。

        可选地，我们还可以使用环境变量来设置此类属性，例如：

        ```bash
        QUARKUS_REDIS_HOSTS: redis://localhost:6379
        QUARKUS_LANGCHAIN4J_OPENAI_API_KEY: changeme
        QUARKUS_LANGCHAIN4J_OPENAI_ORGANIZATION_ID: changeme
        ```

    3. 组件

        LangChain4j 提供了丰富的功能组件来帮助我们实现复杂的流程，例如[文档检索](https://docs.quarkiverse.io/quarkus-langchain4j/dev/retrievers.html)。尽管如此，在本文中，我们将重点关注简单的对话流程。话虽如此，让我们创建我们的简单聊天机器人：

        ```java
        @Singleton
        @RegisterAiService
        public interface ChatBot {

            @SystemMessage("""
            During the whole chat please behave like a Quarkus specialist and only answer directly related to Quarkus,
            its documentation, features and components. Nothing else that has no direct relation to Quarkus.
            """)
            @UserMessage("""
            From the best of your knowledge answer the question below regarding Quarkus. Please favor information from the following sources:
            - https://docs.quarkiverse.io/
            - https://quarkus.io/
            
            And their subpages. Then, answer:
            
            ---
            {question}
            ---
            """)
            String chat(@MemoryId UUID memoryId, String question);
        }
        ```

        在 `ChatBot` 接口中，我们注册了一个将在对话期间使用 LLM 的服务。为此，Quarkus 提供了 [@RegisterAiService](https://docs.quarkiverse.io/quarkus-langchain4j/dev/ai-services.html) 注解，它抽象了集成 LangChain4J 和 GPT-4 的底层设置。接下来，我们应用一些**提示词工程（Prompt Engineering）** 来构建我们希望聊天机器人表现出的行为。

        简而言之，提示词工程是一种塑造指令的技术，这些指令将帮助 LLM 在解释和处理用户请求时调整其行为。

        因此，使用这些提示词，我们实现了机器人的期望行为。LangChain4J 使用消息（Messages）来做到这一点，其中：

        * `SystemMessage`（系统消息）是影响模型响应但对最终用户隐藏的内部指令；
        * `UserMessage`（用户消息）是最终用户发送的消息。LangChain4J 允许我们在这些消息之上应用模板以增强提示效果，从而为开发者提供便利。

        接下来让我们讨论 `@MemoryId`。

    4. 记忆

        尽管 LLM 在生成文本和提供相关问题的答案方面表现出色，但它们本身不足以实现完整的聊天机器人。LLM 自身无法做到的一个关键方面是**记住先前消息的上下文或数据**。这就是我们需要记忆功能的原因。

        LangChain4J 提供了一组抽象的 `ChatMemoryStore` 和 `ChatMemory`。这些抽象允许不同的实现来存储和管理表示聊天的消息列表。此外，需要一个标识符来存储和检索聊天记忆，而 `@MemoryId` 注解正是用于标记该 ID。

        Quarkus 提供了开箱即用的基于 Redis 的聊天记忆实现，这就是为什么我们在前面添加了第二个依赖项和 Redis 设置的原因。

    5. API

        最后，我们的示例聊天机器人应用程序缺少的部分是一个允许用户与系统通信的接口。让我们创建一个 API，以便用户可以向我们的聊天机器人发送问题。

        ```java
        @Path("/chat")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        public class ChatAPI {
            
            private ChatBot chatBot;

            public ChatAPI(ChatBot chatBot) {
                this.chatBot = chatBot;
            }

            @POST
            public Answer mesage(@QueryParam("q") String question, @QueryParam("id") UUID chatId) {

                chatId = chatId == null ? UUID.randomUUID() : chatId;

                String message = chatBot.chat(chatId, question);

                return new Answer(message, chatId);
            }

        }
        ```

        该 API 接收一个问题，以及可选的聊天 ID。如果未提供 ID，系统将自动创建一个。这允许用户控制他们是想继续现有的聊天还是从头开始创建一个新的。然后，它将这两个参数发送到我们的 `ChatBot` 类，该类负责处理对话。可选地，我们还可以实现一个端点来检索特定聊天中的所有历史消息。

        运行以下 `curl` 命令将向我们的聊天机器人发送第一个问题：

        ```bash
        # 让我们问：quarkus 支持 redis 吗？
        curl --location --request POST 'http://localhost:8080/chat?q=Does%20quarkus%20support%20redis%3F'
        ```

        我们将得到以下响应：

        ```json
        {
            "message": "Yes, Quarkus supports Redis through the Quarkus Redis client extension...",
            "chatId": "d3414b32-454e-4577-af81-8fb7460f13cd"
        }
        ```

        请注意，由于这是我们的第一个问题，未提供 ID，因此系统创建了一个新的。现在，我们可以使用此 ID 来跟踪我们的对话历史记录。接下来，让我们测试聊天机器人是否将我们的历史记录保留为对话的上下文：

        ```bash
        # 将 id 参数替换为我们从上次调用中获取的值。 
        # 所以我们问：我上一个关于 Quarkus 的问题是什么？
        curl --location --request POST 'http://localhost:8080/chat?q=What%20was%20my%20last%20Quarkus%20question%3F&id=d3414b32-454e-4577-af81-8fb7460f13cd'
        ```

        太棒了。正如预期的那样，我们的聊天机器人正确地回答了：

        ```json
        {
            "message": "Your last Quarkus question was asking whether Quarkus supports Redis.",
            "chatId": "d3414b32-454e-4577-af81-8fb7460f13cd"
        }
        ```

        这意味着只需创建几个类并设置一些属性，我们的简单聊天机器人应用程序就准备就绪了。

4. 结论

    使用 Quarkus 和 LangChain4J 极大地简化了构建基于 Quarkus 的聊天机器人的过程，该机器人可以与 OpenAI 的语言模型交互并保留对话记忆。Quarkus 和 LangChain4j 的这种强大组合使得开发 AI 驱动的应用程序成为可能，在保持极低系统开销的同时，依然提供了丰富的功能。

    基于此基础设置，我们可以通过添加更多特定领域的知识并提高其回答复杂问题的能力，来继续扩展聊天机器人的功能。Quarkus 和 LangChain4j 生态系统中还提供了许多其他工具来支持这些扩展。

    在本文中，我们了解了 Quarkus 和 LangChain4J 的结合为使用 Java 开发 AI 系统带来了多大的生产力和效率提升。此外，我们还梳理了开发此类应用程序所涉及的核心概念。
