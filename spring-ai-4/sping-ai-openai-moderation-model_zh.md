# [Spring AI 中使用 OpenAI 内容审核模型指南](https://www.baeldung.com/sping-ai-openai-moderation-model)

Spring +    Spring AI

OpenAI  

1. 引言

    我们使用 **Spring AI** 配合 **[OpenAI 的内容审核模型](https://platform.openai.com/docs/guides/moderation)** 来检测文本中的有害或敏感内容。该审核模型会分析输入内容，并标记诸如自残、暴力、仇恨或色情等类别。

    在本教程中，我们将学习如何构建一个内容审核服务，并将其与 OpenAI 的审核模型集成。

2. 依赖项

    添加 `spring-ai-starter-model-openai` 依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>
    ```

    通过该依赖，我们可以获得聊天客户端，包括对审核模型的调用支持。

3. 配置

    接下来，配置 Spring AI 客户端：

    ```yaml
    spring:
    ai:
        openai:
        api-key: ${OPEN_AI_API_KEY}
        moderation:
            options:
            model: omni-moderation-latest
    ```

    我们指定了 [API 密钥](https://platform.openai.com/api-keys)和审核模型名称。现在，我们可以开始使用审核 API 了。

4. 审核类别

    让我们回顾一下可用的审核类别：

    - **Hate（仇恨）**：用于检测基于受保护特征（如种族、性别等）表达或宣扬仇恨的内容。
    - **Hate/Threatening（仇恨/威胁）**：用于检测包含暴力或严重伤害威胁的仇恨内容。
    - **Harassment（骚扰）**：当语言骚扰、欺凌或针对个人或群体时触发。
    - **Harassment/Threatening（骚扰/威胁）**：当骚扰内容包含明确威胁或伤害意图时触发。
    - **Self-harm（自残）**：用于识别宣扬或描绘自残行为的内容。
    - **Self-harm/Intent（自残/意图）**：当内容表达自残意图时触发。
    - **Self-harm/Instructions（自残/指导）**：当内容提供自残方法、步骤或鼓励时触发。
    - **Sexual（色情）**：用于标记明确的色情内容或性服务推广。
    - **Sexual/Minors（涉及未成年人的色情内容）**：严格禁止，用于标记任何涉及未成年人的性相关内容。
    - **Violence（暴力）**：当内容描绘或描述死亡、暴力或身体伤害时触发。
    - **Violence/Graphic（暴力/血腥）**：用于检测对伤害、死亡或严重创伤的生动或血腥描绘。
    - **Illicit（非法）**：用于标记提供非法活动建议、指导或推广的内容。
    - **Illicit/Violent（非法/暴力）**：当非法内容包含暴力元素时触发。

5. 构建审核服务

    现在，我们来构建内容审核服务。该服务将接收用户输入消息，并使用审核模型验证其是否违反各类别。

    1. TextModerationService

        首先构建 `TextModerationService`：

        ```java
        @Service
        public class TextModerationService {

            private final OpenAiModerationModel openAiModerationModel;

            @Autowired
            public TextModerationService(OpenAiModerationModel openAiModerationModel) {
                this.openAiModerationModel = openAiModerationModel;
            }

            public String moderate(String text) {
                ModerationPrompt moderationRequest = new ModerationPrompt(text);
                ModerationResponse response = openAiModerationModel.call(moderationRequest);
                Moderation output = response.getResult().getOutput();

                return output.getResults().stream()
                .map(this::buildModerationResult)
                .collect(Collectors.joining("\n"));
            }
        }
        ```

        这里我们使用了 `OpenAiModerationModel`，向其发送包含待审核文本的 `ModerationPrompt`，并从模型响应中构建结果。接着，我们创建 `buildModerationResult()` 方法：

        ```java
        private String buildModerationResult(ModerationResult moderationResult) {

            Categories categories = moderationResult.getCategories();

            String violations = Stream.of(
                Map.entry("Sexual", categories.isSexual()),
                Map.entry("Hate", categories.isHate()),
                Map.entry("Harassment", categories.isHarassment()),
                Map.entry("Self-Harm", categories.isSelfHarm()),
                Map.entry("Sexual/Minors", categories.isSexualMinors()),
                Map.entry("Hate/Threatening", categories.isHateThreatening()),
                Map.entry("Violence/Graphic", categories.isViolenceGraphic()),
                Map.entry("Self-Harm/Intent", categories.isSelfHarmIntent()),
                Map.entry("Self-Harm/Instructions", categories.isSelfHarmInstructions()),
                Map.entry("Harassment/Threatening", categories.isHarassmentThreatening()),
                Map.entry("Violence", categories.isViolence()))
            .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(", "));

            return violations.isEmpty()
            ? "未检测到任何违规类别。"
            : "违规类别：" + violations;
        }
        ```

        我们获取审核结果中的各类别，并创建映射以列出每个违规类别。若无任何违规，则返回默认提示文本。

    2. TextModerationController

        在构建控制器前，先创建 `ModerateRequest` 类，用于发送待审核文本：

        ```java
        public class ModerateRequest {

            private String text;

            // getter 和 setter 方法
        }
        ```

        接着，创建 `TextModerationController`：

        ```java
        @RestController
        public class TextModerationController {

            private final TextModerationService service;

            @Autowired
            public TextModerationController(TextModerationService service) {
                this.service = service;
            }

            @PostMapping("/moderate")
            public ResponseEntity<String> moderate(@RequestBody ModerateRequest request) {
                return ResponseEntity.ok(service.moderate(request.getText()));
            }
        }
        ```

        这里我们从 `ModerateRequest` 中获取文本，并将其传递给 `TextModerationService` 进行审核。

    3. 测试行为

        最后，测试我们的审核服务：

        ```java
        @AutoConfigureMockMvc
        @ExtendWith(SpringExtension.class)
        @EnableAutoConfiguration
        @SpringBootTest
        @ActiveProfiles("moderation")
        class ModerationApplicationLiveTest {

            @Autowired
            private MockMvc mockMvc;

            @Test
            void givenTextWithoutViolation_whenModerating_thenNoCategoryViolationsDetected() throws Exception {
                String moderationResponse = mockMvc.perform(post("/moderate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"text\": \"请审核我\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

                assertThat(moderationResponse).contains("未检测到任何违规类别");
            }
        }
        ```

        我们发送了一条不违反任何类别的文本，并验证服务确认无违规。接下来，测试存在违规类别的情况：

        ```java
        @Test
        void givenHarassingText_whenModerating_thenHarassmentCategoryShouldBeFlagged() throws Exception {
            String moderationResponse = mockMvc.perform(post("/moderate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"你真是个坏人！我讨厌你！\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

            assertThat(moderationResponse).contains("违规类别：Harassment");
        }
        ```

        如预期，系统标记了“骚扰”类别。现在，我们检查服务是否能同时识别多个违规类别：

        ```java
        @Test
        void givenTextViolatingMultipleCategories_whenModerating_thenAllCategoriesShouldBeFlagged() throws Exception {
            String moderationResponse = mockMvc.perform(post("/moderate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"我恨你，我要伤害你！\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

            assertThat(moderationResponse).contains("违规类别：Harassment, Harassment/Threatening, Violence");
        }
        ```

        我们发送了一条包含多个违规内容的文本。服务响应确认了三个类别被触发。

6. 结论

    本文介绍了如何在 Spring AI 中集成 OpenAI 的内容审核模型。我们探索了审核类别，并构建了一个用于审核输入文本的服务。该服务可作为更复杂系统的一部分，用于处理用户生成内容。例如，我们可以将其接入聊天审核机器人，帮助我们控制文章评论区的对话质量。
