# [Spring AI 中使用 OpenAI 文本转语音（TTS）指南](https://www.baeldung.com/spring-ai-openai-tts)

人工智能 · Spring AI · OpenAI  

1. 引言

    如今，应用程序通过集成神经网络（如知识库、智能助手或分析引擎）获得了巨大优势。一个实用场景是**将文本转换为语音**——这一过程称为**文本转语音（Text-to-Speech, TTS）**，它能自动生成自然、拟人化的声音内容。

    现代 TTS 系统利用深度学习技术处理发音、节奏、语调，甚至情感表达。与早期基于规则的方法不同，这些模型在大型数据集上训练，能够生成富有表现力、支持多语言的语音，非常适合虚拟助手、无障碍教育平台等全球化应用。

    在本教程中，我们将探索如何在 **Spring AI** 中使用 **OpenAI 的文本转语音服务**。

2. 依赖与配置

    首先，添加 `spring-ai-starter-model-openai` 依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
        <version>1.1.0</version>
    </dependency>
    ```

    接着，在配置文件中设置 OpenAI 模型相关属性：

    ```yaml
    spring:
    ai:
        openai:
        api-key: ${OPENAI_API_KEY}
        audio:
            speech:
            options:
                model: tts-1
                voice: alloy
                response-format: mp3
                speed: 1.0
    ```

    - 必须设置 `api-key` 以使用 OpenAI API。
    - 我们还需指定 TTS [模型](https://platform.openai.com/docs/models)名称（`tts-1`）、[语音](https://platform.openai.com/docs/guides/text-to-speech#voice-options)角色（`alloy`）、响应格式（`mp3`）和语速（`1.0`）。

3. 构建文本转语音应用

    现在，我们开始构建文本转语音应用。

    1. 创建 TextToSpeechService

        ```java
        @Service
        public class TextToSpeechService {

            private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

            @Autowired
            public TextToSpeechService(OpenAiAudioSpeechModel openAiAudioSpeechModel) {
                this.openAiAudioSpeechModel = openAiAudioSpeechModel;
            }

            public byte[] makeSpeech(String text) {
                SpeechPrompt speechPrompt = new SpeechPrompt(text);
                SpeechResponse response = openAiAudioSpeechModel.call(speechPrompt);
                return response.getResult().getOutput();
            }
        }
        ```

        这里，我们使用 Spring AI 根据配置自动预配置的 `OpenAiAudioSpeechModel`。`makeSpeech()` 方法接收文本，将其转换为音频字节数据。

    2. 创建控制器 TextToSpeechController

        ```java
        @RestController
        public class TextToSpeechController {
            private final TextToSpeechService textToSpeechService;

            @Autowired
            public TextToSpeechController(TextToSpeechService textToSpeechService) {
                this.textToSpeechService = textToSpeechService;
            }

            @GetMapping("/text-to-speech")
            public ResponseEntity<byte[]> generateSpeechForText(@RequestParam String text) {
                return ResponseEntity.ok(textToSpeechService.makeSpeech(text));
            }
        }
        ```

    3. 测试端点

        ```java
        @SpringBootTest
        @ExtendWith(SpringExtension.class)
        @AutoConfigureMockMvc
        @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
        class TextToSpeechLiveTest {

            @Autowired
            private MockMvc mockMvc;

            @Autowired
            private TextToSpeechService textToSpeechService;

            @Test
            void givenTextToSpeechService_whenCallingTextToSpeechEndpoint_thenExpectedAudioFileBytesShouldBeObtained() throws Exception {
                byte[] audioContent = mockMvc.perform(get("/text-to-speech")
                .param("text", "Hello from Baeldung"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

                assertNotEquals(0, audioContent.length);
            }
        }
        ```

        我们调用 `/text-to-speech` 端点，验证响应状态码为 200 且内容非空。若将内容保存为文件，即可获得包含语音的 MP3 文件。

4. 添加实时音频流式传输端点

    当生成大型音频内容时，一次性加载整个字节数组可能导致内存占用过高。有时，我们希望在音频完全生成前就开始播放——为此，OpenAI 支持**流式传输 TTS 响应**。

    1. 扩展服务以支持流式传输

        ```java
        public Flux<byte[]> makeSpeechStream(String text) {
            SpeechPrompt speechPrompt = new SpeechPrompt(text);
            Flux<SpeechResponse> responseStream = openAiAudioSpeechModel.stream(speechPrompt);

            return responseStream
            .map(SpeechResponse::getResult)
            .map(Speech::getOutput);
        }
        ```

        新增 `makeSpeechStream()` 方法，使用 `OpenAiAudioSpeechModel` 的 `stream()` 方法生成字节块流。

    2. 创建流式 HTTP 端点

        ```java
        @GetMapping(value = "/text-to-speech-stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<StreamingResponseBody> streamSpeech(@RequestParam("text") String text) {
            Flux<byte[]> audioStream = textToSpeechService.makeSpeechStream(text);

            StreamingResponseBody responseBody = outputStream -> {
                audioStream.toStream().forEach(bytes -> {
                    try {
                        outputStream.write(bytes);
                        outputStream.flush();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            };

            return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(responseBody);
        }
        ```

        此处我们遍历字节流，逐块写入 `StreamingResponseBody`。若使用 WebFlux，可直接从端点返回 `Flux<byte[]>`。我们使用 `application/octet-stream` 内容类型表明响应为流式数据。

    3. 测试流式端点

        ```java
        @Test
        void givenStreamingEndpoint_whenCalled_thenReceiveAudioFileBytes() throws Exception {
            String longText = """
                Hello from Baeldung!
                Here, we explore the world of Java,
                Spring, and web development with clear, practical tutorials.
                Whether you're just starting out or diving deep into advanced
                topics, you'll find guides to help you write clean, efficient,
                and modern code.
                """;

            mockMvc.perform(get("/text-to-speech-stream")
                .param("text", longText)
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andDo(result -> {
                byte[] response = result.getResponse().getContentAsByteArray();
                assertNotNull(response);
                assertTrue(response.length > 0);
            });
        }
        ```

        我们调用流式端点，验证其返回字节数组。MockMvc 会收集完整响应体，但实际场景中也可逐块读取流。

5. 为特定调用自定义模型参数

    有时我们需要为特定请求覆盖默认模型参数。为此，可使用 `OpenAiAudioSpeechOptions`。

    1. 更新服务以支持自定义参数

        ```java
        public byte[] makeSpeech(String text, OpenAiAudioSpeechOptions speechOptions) {
            SpeechPrompt speechPrompt = new SpeechPrompt(text, speechOptions);
            SpeechResponse response = openAiAudioSpeechModel.call(speechPrompt);
            return response.getResult().getOutput();
        }
        ```

        我们重载了 `makeSpeech()` 方法，新增 `speechOptions` 参数。若传入空对象，则使用默认配置。

    2. 创建支持参数的端点

        ```java
        @GetMapping("/text-to-speech-customized")
        public ResponseEntity<byte[]> generateSpeechForTextCustomized(
            @RequestParam("text") String text,
            @RequestParam Map<String, String> params) {

            OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
            .model(params.get("model"))
            .voice(OpenAiAudioApi.SpeechRequest.Voice.valueOf(params.get("voice").toUpperCase()))
            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.valueOf(params.get("responseFormat").toUpperCase()))
            .speed(Float.parseFloat(params.get("speed")))
            .build();

            return ResponseEntity.ok(textToSpeechService.makeSpeech(text, speechOptions));
        }
        ```

        此处我们从请求参数中提取语音配置，构建 `OpenAiAudioSpeechOptions` 对象。

    3. 测试自定义端点

        ```java
        @Test
        void givenTextToSpeechService_whenCallingTextToSpeechEndpointWithAnotherVoiceOption_thenExpectedAudioFileBytesShouldBeObtained() throws Exception {
            byte[] audioContent = mockMvc.perform(get("/text-to-speech-customized")
            .param("text", "Hello from Baeldung")
            .param("model", "tts-1")
            .param("voice", "NOVA")
            .param("responseFormat", "MP3")
            .param("speed", "1.0"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

            assertNotEquals(0, audioContent.length);
        }
        ```

        我们调用端点并指定使用 `NOVA` 语音，成功接收到使用指定语音生成的音频字节数据。

6. 结论

    文本转语音 API 使我们能够从文本生成自然语音。通过简单配置和现代模型，我们可以为应用程序添加动态语音交互功能。

    在本文中，我们探索了如何使用 Spring AI 集成 OpenAI TTS 模型。同样，我们也可以轻松集成其他 TTS 模型，或构建自己的语音合成服务。

    借助 Spring AI 的抽象层，开发者能以最小成本快速接入强大的 AI 语音能力，为应用注入更自然、更人性化的交互体验。
