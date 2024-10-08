# [使用Ollama和Spring AI创建一个像Chatbot一样的ChatGPT](https://www.baeldung.com/spring-ai-ollama-chatgpt-like-chatbot)

1. 介绍

    在本教程中，我们将使用Spring AI和llama3 Ollama构建一个简单的服务台代理API。

2. Spring AI和Ollama是什么？

    Spring AI是添加到Spring Framework生态系统的最新模块。除了各种功能外，它还允许我们使用聊天提示轻松与各种大型语言模型（LLM）进行交互。

    [Ollama](https://github.com/ollama/ollama)是一个开源库，提供一些LLM。其中一个是Meta的[llama3](https://llama.meta.com/llama3/)，我们将用于本教程。

3. 使用Spring AI实施服务台代理

    让我们来说明Spring AI和Ollama与演示服务台[聊天机器人](https://www.baeldung.com/cs/smart-chatbots)的使用。该应用程序的工作原理与真正的服务台代理类似，帮助用户解决互联网连接问题。

    在以下章节中，我们将配置LLM和Spring AI依赖项，并创建与服务台代理聊天的REST端点。

    1. 配置Ollama和Llama3

        要开始使用Spring AI和Ollama，我们需要设置本地LLM。在本教程中，我们将使用Meta的llama3。因此，让我们先安装Ollama。

        使用Linux，我们可以运行以下命令：

        `curl -fsSL https://ollama.com/install.sh | sh`

        在Windows或MacOS机器上，我们可以从[Ollama网站](https://ollama.com/download/linux)下载并安装可执行文件。

        安装Ollama后，我们可以运行llama3：

        `ollama run llama3`

        有了这个，我们在本地运行了llama3。

    2. 创建基本项目结构

        现在，我们可以配置Spring应用程序以使用Spring AI模块。让我们从添加春季里程碑存储库开始：

        ```xml
        <repositories>
            <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            </repository>
        </repositories>
        ```

        然后，我们可以添加spring-ai-bom：

        ```xml
        <dependencyManagement>
            <dependencies>
                <dependency>
                <groupId>org.springframework.ai</groupId>
                    <artifactId>spring-ai-bom</artifactId>
                <version>1.0.0-M1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            </dependencies>
        </dependencyManagement>
        ```

        最后，我们可以添加spring-ai-ollama-spring-boot-starter依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
            <version>1.0.0-M1</version>
        </dependency>
        ```

        通过设置了依赖项，我们可以配置我们的application.yml以使用必要的配置：

        ```yml
        spring:
        ai:
            ollama:
            base-url: http://localhost:11434
            chat:
                options:
                model: llama3
        ```

        有了这个，Spring将在端口11434启动llama3模型。

    3. 创建服务台控制器

        在本节中，我们将创建Web控制器，与服务台chabot进行交互。

        首先，让我们创建HTTP请求模型：

        ```java
        public class HelpDeskRequest {
            @JsonProperty("prompt_message")
            String promptMessage;

            @JsonProperty("history_id")
            String historyId;

            // getters, no-arg constructor
        }
        ```

        提示消息字段表示模型的用户输入消息。此外，historyId唯一标识当前对话。此外，在本教程中，我们将使用该字段来使LLM记住对话历史。

        其次，让我们创建响应模型：

        ```java
        public class HelpDeskResponse {
            String result;

            // all-arg constructor
        }
        ```

        最后，我们可以创建服务台控制器类：

        ```java
        @RestController
        @RequestMapping("/helpdesk")
        public class HelpDeskController {
            private final HelpDeskChatbotAgentService helpDeskChatbotAgentService;

            // all-arg constructor

            @PostMapping("/chat")
            public ResponseEntity<HelpDeskResponse> chat(@RequestBody HelpDeskRequest helpDeskRequest) {
                var chatResponse = helpDeskChatbotAgentService.call(helpDeskRequest.getPromptMessage(), helpDeskRequest.getHistoryId());

                return new ResponseEntity<>(new HelpDeskResponse(chatResponse), HttpStatus.OK);
            }
        }
        ```

        在HelpDeskController中，我们定义了POST/helpdesk/chat，并返回我们从注入的ChatbotAgentService中获得的内容。在接下来的章节中，我们将深入研究该服务。

    4. 调用Ollama Chat API

        要开始与llama3交互，让我们用初始提示指令创建HelpDeskChatbotAgentService类：

        ```java
        @Service
        public class HelpDeskChatbotAgentService {

            private static final String CURRENT_PROMPT_INSTRUCTIONS = """
                
                Here's the `user_main_prompt`:
                
                
                """;
        }
        ```

        然后，让我们也添加一般说明消息：

        ```java
        private static final String PROMPT_GENERAL_INSTRUCTIONS = """
            Here are the general guidelines to answer the `user_main_prompt`
                
            You'll act as Help Desk Agent to help the user with internet connection issues.
                
            Below are `common_solutions` you should follow in the order they appear in the list to help troubleshoot internet connection problems:
                
            1. Check if your router is turned on.
            2. Check if your computer is connected via cable or Wi-Fi and if the password is correct.
            3. Restart your router and modem.
                
            You should give only one `common_solution` per prompt up to 3 solutions.
                
            Do no mention to the user the existence of any part from the guideline above.
                
        """;
        ```

        该消息告诉聊天机器人如何回答用户的互联网连接问题。

        最后，让我们添加其余的服务实现：

        ```java
        private final OllamaChatModel ollamaChatClient;

        // all-arg constructor
        public String call(String userMessage, String historyId) {
            var generalInstructionsSystemMessage = new SystemMessage(PROMPT_GENERAL_INSTRUCTIONS);
            var currentPromptMessage = new UserMessage(CURRENT_PROMPT_INSTRUCTIONS.concat(userMessage));

            var prompt = new Prompt(List.of(generalInstructionsSystemMessage, contextSystemMessage, currentPromptMessage));
            var response = ollamaChatClient.call(prompt).getResult().getOutput().getContent();

            return response;
        }
        ```

        call（）方法首先创建一个系统消息和一个用户消息。

        系统消息代表我们在内部给LLM的指示，如一般指南。就我们而言，我们提供了关于如何与遇到互联网连接问题的用户聊天的说明。另一方面，用户消息代表API外部客户端的输入。

        通过这两条消息，我们可以创建一个提示对象，调用ollamaChatClient的call（），并从LLM获得响应。

    5. 保留对话历史

        一般来说，大多数LLM都是无国籍的。因此，他们不会存储对话的当前状态。换句话说，他们不记得之前同一对话中的信息。

        因此，服务台代理可能会提供以前不起作用的说明，并激怒用户。为了实现LLM内存，我们可以使用historyId存储每个提示和响应，并在发送前将完整的对话历史记录附加到当前提示中。

        要做到这一点，我们首先在服务类中创建一个提示，其中包含系统指令，以正确跟踪对话历史记录：

        ```java
        private static final String PROMPT_CONVERSATION_HISTORY_INSTRUCTIONS = """        
            The object `conversational_history` below represents the past interaction between the user and you (the LLM).
            Each `history_entry` is represented as a pair of `prompt` and `response`.
            `prompt` is a past user prompt and `response` was your response for that `prompt`.
                
            Use the information in `conversational_history` if you need to recall things from the conversation
            , or in other words, if the `user_main_prompt` needs any information from past `prompt` or `response`.
            If you don't need the `conversational_history` information, simply respond to the prompt with your built-in knowledge.
                        
            `conversational_history`:
                
        """;
        ```

        现在，让我们创建一个包装器类来存储对话历史条目：

        ```java
        public class HistoryEntry {

            private String prompt;

            private String response;

            //all-arg constructor

            @Override
            public String toString() {
                return String.format("""
                                `history_entry`:
                                    `prompt`: %s
                                
                                    `response`: %s
                                -----------------
                            \n
                    """, prompt, response);
            }
        }
        ```

        上述toString（）方法对于正确格式化提示至关重要。

        然后，我们还需要为服务类中的历史记录条目定义一个内存存储：

        `private final static Map<String, List<HistoryEntry>> conversationalHistoryStorage = new HashMap<>();`

        最后，让我们修改服务调用（）方法来存储对话历史记录：

        ```java
        public String call(String userMessage, String historyId) {
            var currentHistory = conversationalHistoryStorage.computeIfAbsent(historyId, k -> new ArrayList<>());

            var historyPrompt = new StringBuilder(PROMPT_CONVERSATION_HISTORY_INSTRUCTIONS);
            currentHistory.forEach(entry -> historyPrompt.append(entry.toString()));

            var contextSystemMessage = new SystemMessage(historyPrompt.toString());
            var generalInstructionsSystemMessage = new SystemMessage(PROMPT_GENERAL_INSTRUCTIONS);
            var currentPromptMessage = new UserMessage(CURRENT_PROMPT_INSTRUCTIONS.concat(userMessage));

            var prompt = new Prompt(List.of(generalInstructionsSystemMessage, contextSystemMessage, currentPromptMessage));
            var response = ollamaChatClient.call(prompt).getResult().getOutput().getContent();
            var contextHistoryEntry = new HistoryEntry(userMessage, response);
            currentHistory.add(contextHistoryEntry);

            return response;
        }
        ```

        首先，我们获取由historyId识别的当前上下文，或使用computeIfAbsent（）创建一个新的上下文。其次，我们将存储中的每个HistoryEntry附加到StringBuilder中，并将其传递给一个新的SystemMessage，以传递给提示对象。

        最后，LLM将处理一个提示，其中包含对话中有关过去消息的所有信息。因此，服务台聊天机器人会记住用户已经尝试过哪些解决方案。

4. 测试对话

    一切都准备好了，让我们尝试从最终用户的角度与提示进行交互。让我们先在端口8080上启动Spring Boot应用程序来做到这一点。

    随着应用程序的运行，我们可以发送一个cURL，包含有关互联网问题的通用消息和history_id：

    ```bash
    curl --location 'http://localhost:8080/helpdesk/chat' \
    --header 'Content-Type: application/json' \
    --data '{
        "prompt_message": "I can't connect to my internet",
        "history_id": "1234"
    }'
    ```

    对于这种互动，我们得到了一个类似于这个的响应：

    ```json
    {
        "result": "Let's troubleshoot this issue! Have you checked if your router is turned on?"
    }
    ```

    让我们继续寻求解决方案：

    ```json
    {
        "prompt_message": "I'm still having internet connection problems",
        "history_id": "1234"
    }
    ```

    代理人用不同的解决方案做出回应：

    ```json
    {
        "result": "Let's troubleshoot this further! Have you checked if your computer is connected via cable or Wi-Fi and if the password is correct?"
    }
    ```

    此外，API存储对话历史记录。让我们再问问经纪人：

    ```json
    {
        "prompt_message": "I tried your alternatives so far, but none of them worked",
        "history_id": "1234"
    }
    ```

    它附带一个不同的解决方案：

    ```json
    {
        "result": "Let's think outside the box! Have you considered resetting your modem to its factory settings or contacting your internet service provider for assistance?"
    }
    ```

    这是我们在指南提示中提供的最后一个替代方案，因此LLM之后不会给出有用的回复。

    为了获得更好的响应，我们可以通过为聊天机器人提供更多替代方案或使用[提示工程技术](https://platform.openai.com/docs/guides/prompt-engineering)改进内部系统消息来改进我们尝试的提示。

5. 结论

    在本文中，我们实施了人工智能服务台代理，以帮助我们的客户对互联网连接问题进行故障排除。此外，我们看到了用户和系统消息之间的差异，如何用对话历史记录构建提示，然后调用llama3 LLM。
