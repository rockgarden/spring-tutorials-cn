# [使用Mistral AI API在Java和Spring AI中调用函数](https://www.baeldung.com/spring-ai-mistral-api-function-calling)

1. 一览表

    使用大型语言模型，我们可以检索到许多有用的信息。我们可以了解许多关于任何事情的新事实，并根据互联网上的现有数据获得答案。我们可以要求他们处理输入数据并执行各种操作。但是，如果我们要求模型使用API来准备输出呢？

    为此，我们可以使用函数调用。函数调用允许LLM与数据交互和操作，执行计算或检索其固有文本功能以外的信息。

    在本文中，我们将探索什么是函数调用，以及如何使用它将LLM与我们的内部逻辑集成。作为模型提供商，我们将使用[Mistral AI API](https://mistral.ai/)。

2. Mistral人工智能API

    [Mistral AI](https://www.baeldung.com/cs/top-llm-comparative-analysis#4-mistral-by-mistral-ai)专注于为开发人员和企业提供开放和可移植的生成人工智能模型。我们可以用它来进行简单的提示以及函数调用集成。

    1. 检索API密钥

        要开始使用[Mistral API](https://www.baeldung.com/cs/top-llm-comparative-analysis#4-mistral-by-mistral-ai)，我们首先需要检索API密钥。让我们进入[API密钥管理控制台](https://console.mistral.ai/api-keys/)：

        要激活任何密钥，我们必须设置[账单配置](https://console.mistral.ai/billing/)或使用试用期（如果有）：

        解决所有事情后，我们可以按下“创建新密钥”按钮来获取Mistral API密钥。

    2. 用法示例

        让我们从一个简单的提示开始。我们将要求Mistral API向我们返回一份患者状态列表。让我们实施这样的呼吁：

        ```java
        @Test
        void givenHttpClient_whenSendTheRequestToChatAPI_thenShouldBeExpectedWordInResponse() throws IOException, InterruptedException {

            String apiKey = System.getenv("MISTRAL_API_KEY");
            String apiUrl = "https://api.mistral.ai/v1/chat/completions";
            String requestBody = "{"
            + "\"model\": \"mistral-large-latest\","
            + "\"messages\": [{\"role\": \"user\", "
            + "\"content\": \"What the patient health statuses can be?\"}]"
            + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            logger.info("Model response: " + responseBody);

            Assertions.assertThat(responseBody)
            .containsIgnoringCase("healthy");
        }
        ```

        我们创建了一个HTTP请求，并将其发送到/chat/completions端点。然后，我们使用API密钥作为授权标头值。不出所料，在响应中，我们同时看到了元数据和内容本身：

        ```log
        Model response: {"id":"585e3599275545c588cb0a502d1ab9e0","object":"chat.completion",
        "created":1718308692,"model":"mistral-large-latest",
        "choices":[{"index":0,"message":{"role":"assistant","content":"Patient health statuses can be
        categorized in various ways, depending on the specific context or medical system being used.
        However, some common health statuses include:
        1.Healthy: The patient is in good health with no known medical issues.
        ...
        10.Palliative: The patient is receiving care that is focused on relieving symptoms and improving quality of life, rather than curing the underlying disease.",
        "tool_calls":null},"finish_reason":"stop","logprobs":null}],
        "usage":{"prompt_tokens":12,"total_tokens":291,"completion_tokens":279}}
        ```

        [函数调用](https://docs.mistral.ai/capabilities/function_calling/)的例子更加复杂，在调用之前需要做很多准备。我们将在下一节中发现它。

3. Spring人工智能集成

    让我们来看看Mistral API在函数调用中使用的几个示例。使用Spring AI，我们可以避免大量的准备工作，让框架为我们做。

    1. 依赖性

        所需的依赖项位于[Spring里程碑](https://repo.spring.io/milestone)存储库中。让我们把它添加到我们的pom.xml中：

        ```xml
        <repositories>
            <repository>
                <id>spring-milestones</id>
                <name>Spring milestones</name>
                <url>https://repo.spring.io/milestone</url>
            </repository>
        </repositories>
        ```

        现在，让我们添加Mistral API集成的依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-mistral-ai-spring-boot-starter</artifactId>
            <version>0.8.1</version>
        </dependency>
        ```

    2. 配置

        现在让我们将之前获得的API密钥添加到属性文件中：

        ```yml
        spring:
        ai:
            mistralai:
            api-key: ${MISTRAL_AI_API_KEY}
            chat:
                options:
                model: mistral-small-latest
        ```

        这就是我们开始使用Mistral API所需要的一切。

    3. 具有一个功能的用例

        在我们的演示示例中，我们将创建一个功能，根据患者的ID返回患者的健康状况。

        让我们从创建患者[记录](https://www.baeldung.com/java-record-keyword)开始：

        `public record Patient(String patientId) {}`

        现在让我们为患者的健康状况创建另一个记录：

        `public record HealthStatus(String status) {}`

        在下一步中，我们将创建一个配置类：

        ```java
        @Configuration
        public class MistralAIFunctionConfiguration {
            public static final Map<Patient, HealthStatus> HEALTH_DATA = Map.of(
            new Patient("P001"), new HealthStatus("Healthy"),
            new Patient("P002"), new HealthStatus("Has cough"),
            new Patient("P003"), new HealthStatus("Healthy"),
            new Patient("P004"), new HealthStatus("Has increased blood pressure"),
            new Patient("P005"), new HealthStatus("Healthy"));

            @Bean
            @Description("Get patient health status")
            public Function<Patient, HealthStatus> retrievePatientHealthStatus() {
                return (patient) -> new HealthStatus(HEALTH_DATA.get(patient).status());
            }
        }
        ```

        在这里，我们指定了带有患者健康数据的数据集。此外，我们创建了retetelPatientHealthStatus（）函数，该函数返回给定患者ID的健康状况。

        现在，让我们通过在集成中调用来测试我们的函数：

        ```java
        @Import(MistralAIFunctionConfiguration.class)
        @ExtendWith(SpringExtension.class)
        @SpringBootTest
        public class MistralAIFunctionCallingManualTest {

            @Autowired
            private MistralAiChatModel chatClient;

            @Test
            void givenMistralAiChatClient_whenAskChatAPIAboutPatientHealthStatus_thenExpectedHealthStatusIsPresentInResponse() {

                var options = MistralAiChatOptions.builder()
                .withFunction("retrievePatientHealthStatus")
                .build();

                ChatResponse paymentStatusResponse = chatClient.call(
                new Prompt("What's the health status of the patient with id P004?",  options));

                String responseContent = paymentStatusResponse.getResult().getOutput().getContent();
                logger.info(responseContent);

                Assertions.assertThat(responseContent)
                .containsIgnoringCase("has increased blood pressure");
            }
        }
        ```

        我们已经导入了我们的MistralAIFunctionConfiguration类，将我们的retetePatientHealthStatus（）函数添加到测试Spring上下文中。我们还注入了MistralAiChatClient，它将由Spring AI启动器自动实例化。

        在对聊天API的请求中，我们指定了包含其中一个患者的ID和检索健康状况的函数名称的提示文本。然后我们调用API，并验证响应包含预期的健康状态。

        此外，我们已经记录了整个响应文本，以下是我们在那里看到的内容：

        `The patient with id P004 has increased blood pressure.`

    4. 具有多种功能的用例

        我们还可以指定多个功能，人工智能根据我们发送的提示来决定使用哪一个。

        为了证明它，让我们扩展我们的健康状态记录：

        `public record HealthStatus(String status, LocalDate changeDate) {}`

        我们添加了上次状态更改的日期。

        现在让我们修改配置类：

        ```java
        @Configuration
        public class MistralAIFunctionConfiguration {
            public static final Map<Patient, HealthStatus> HEALTH_DATA = Map.of(
            new Patient("P001"), new HealthStatus("Healthy",
                LocalDate.of(2024,1, 20)),
            new Patient("P002"), new HealthStatus("Has cough",
                LocalDate.of(2024,3, 15)),
            new Patient("P003"), new HealthStatus("Healthy",
                LocalDate.of(2024,4, 12)),
            new Patient("P004"), new HealthStatus("Has increased blood pressure",
                LocalDate.of(2024,5, 19)),
            new Patient("P005"), new HealthStatus("Healthy",
                LocalDate.of(2024,6, 1)));

            @Bean
            @Description("Get patient health status")
            public Function<Patient, String> retrievePatientHealthStatus() {
                return (patient) -> HEALTH_DATA.get(patient).status();
            }

            @Bean
            @Description("Get when patient health status was updated")
            public Function<Patient, LocalDate> retrievePatientHealthStatusChangeDate() {
                return (patient) -> HEALTH_DATA.get(patient).changeDate();
            }
        }
        ```

        我们已经为每个状态项目填充了更改日期。我们还创建了theretrievePatientHealthStatusChangeDate（）函数，该函数返回有关状态更改日期的信息。

        让我们看看我们如何将两个新功能与Mistral API一起使用：

        ```java
        @Test
        void givenMistralAiChatClient_whenAskChatAPIAboutPatientHealthStatusAndWhenThisStatusWasChanged_thenExpectedInformationInResponse() {
            var options = MistralAiChatOptions.builder()
            .withFunctions(
                Set.of("retrievePatientHealthStatus",
                "retrievePatientHealthStatusChangeDate"))
            .build();

            ChatResponse paymentStatusResponse = chatClient.call(
            new Prompt(
                "What's the health status of the patient with id P005",
                options));

            String paymentStatusResponseContent = paymentStatusResponse.getResult()
            .getOutput().getContent();
            logger.info(paymentStatusResponseContent);

            Assertions.assertThat(paymentStatusResponseContent)
            .containsIgnoringCase("healthy");

            ChatResponse changeDateResponse = chatClient.call(
            new Prompt(
                "When health status of the patient with id P005 was changed?",
                options));

            String changeDateResponseContent = changeDateResponse.getResult().getOutput().getContent();
            logger.info(changeDateResponseContent);

            Assertions.assertThat(paymentStatusResponseContent)
            .containsIgnoringCase("June 1, 2024");
        }
        ```

        在这种情况下，我们指定了两个函数名称并发送了两个提示。首先，我们询问了病人的健康状况。然后我们问这个状态是什么时候改变的。我们已经验证了结果包含预期的信息。除此之外，我们已经记录了所有回复，如下所示：

        ```log
        The patient with id P005 is currently healthy.
        The health status of the patient with id P005 was changed on June 1, 2024.
        ```

4. 结论

    函数调用是扩展LLM功能的绝佳工具。我们还可以用它来将LLM与我们的逻辑相结合。

    在本教程中，我们探讨了如何通过调用一个或多个函数来实现基于LLM的流程。使用这种方法，我们可以实现与AI API集成的现代应用程序。
