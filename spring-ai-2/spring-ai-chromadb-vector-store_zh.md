# [使用 Spring AI 与 ChromaDB 向量数据库](https://www.baeldung.com/spring-ai-chromadb-vector-store)

人工智能 · Spring AI · Testcontainers · 向量数据库

1. 概述

    在传统数据库中，我们通常依赖精确的关键词匹配或基础模式匹配来实现搜索功能。虽然这对简单应用已足够，但这种方法无法充分理解自然语言查询背后的含义和上下文。

    向量数据库通过将数据存储为捕捉其语义的数值向量来解决这一局限。语义相近的词在向量空间中彼此靠近，从而支持语义搜索——即使结果中不包含查询的确切关键词，也能返回相关结果。

    在本教程中，我们将探索如何将开源向量数据库 [ChromaDB](https://www.trychroma.com/) 与 Spring AI 集成。

    为了将文本数据转换为 ChromaDB 可存储和搜索的向量，我们需要一个嵌入模型。我们将使用 Ollama 在本地运行嵌入模型。

2. 依赖项

    首先，在项目的 `pom.xml` 文件中添加必要的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-chroma-store-spring-boot-starter</artifactId>
        <version>1.0.0-M6</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        <version>1.0.0-M6</version>
    </dependency>
    ```

    ChromaDB Starter 依赖项使我们能够与 ChromaDB 向量数据库建立连接并进行交互。

    此外，我们导入了 Ollama Starter 依赖项，用于运行我们的嵌入模型。

    由于当前版本 `1.0.0-M6` 是里程碑版本，我们还需要在 `pom.xml` 中添加 Spring Milestones 仓库：

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

    此仓库用于发布里程碑版本，而非标准的 Maven Central 仓库。

    由于我们在项目中使用了多个 Spring AI Starter，我们还可以在 `pom.xml` 中引入 Spring AI 的物料清单（BOM）：

    ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>1.0.0-M6</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

    添加 BOM 后，我们可以从两个 Starter 依赖项中移除 `version` 标签。

    BOM 消除了版本冲突的风险，并确保我们的 Spring AI 依赖项相互兼容。

3. 使用 Testcontainers 设置本地测试环境

    为便于本地开发和测试，我们将使用 Testcontainers 来设置 ChromaDB 向量数据库和 Ollama 服务。

    通过 Testcontainers 运行所需服务的前提是本地已安装并运行 Docker。

    1. 测试依赖项

        首先，在 `pom.xml` 中添加必要的测试依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>chromadb</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>ollama</artifactId>
            <scope>test</scope>
        </dependency>
        ```

        这些依赖项为我们提供了启动两个外部服务临时 Docker 实例所需的类。

    2. 定义 Testcontainers Bean

        接下来，创建一个 `@TestConfiguration` 类来定义 Testcontainers Bean：

        ```java
        @TestConfiguration(proxyBeanMethods = false)
        class TestcontainersConfiguration {

            @Bean
            @ServiceConnection
            public ChromaDBContainer chromaDB() {
                return new ChromaDBContainer("chromadb/chroma:0.5.20");
            }

            @Bean
            @ServiceConnection
            public OllamaContainer ollama() {
                return new OllamaContainer("ollama/ollama:0.4.5");
            }
        }
        ```

        我们为容器指定了最新的稳定版本。

        我们还使用 [@ServiceConnection](https://www.baeldung.com/spring-boot-built-in-testcontainers#using-serviceconnection-for-dynamic-properties) 注解标注了 Bean 方法。这会动态注册连接两个外部服务所需的所有属性。

        即使不使用 Testcontainers 支持，Spring AI 在本地运行时也会自动连接到默认端口分别为 8000 和 11434 的 ChromaDB 和 Ollama。

        但在生产环境中，我们可以使用相应的 Spring AI 属性覆盖连接详情：

        ```yaml
        spring:
        ai:
            vectorstore:
            chroma:
                client:
                host: ${CHROMADB_HOST}
                port: ${CHROMADB_PORT}
            ollama:
            base-url: ${OLLAMA_BASE_URL}
        ```

        一旦正确配置了连接详情，Spring AI 会自动为我们创建 `VectorStore` 和 `EmbeddingModel` 类型的 Bean，分别用于与向量数据库和嵌入模型交互。我们将在教程后面的部分介绍如何使用这些 Bean。

        虽然 `@ServiceConnection` 会自动定义必要的连接详情，但我们仍需在 `application.yml` 文件中配置一些额外属性：

        ```yaml
        spring:
        ai:
            vectorstore:
            chroma:
                initialize-schema: true
            ollama:
            embedding:
                options:
                model: nomic-embed-text
            init:
                chat:
                include: false
                pull-model-strategy: WHEN_MISSING
        ```

        这里，我们启用了 ChromaDB 的模式初始化。然后，我们将 [nomic-embed-text](https://ollama.com/library/nomic-embed-text) 配置为嵌入模型，并指示 Ollama 在模型不存在时拉取它。

        或者，我们可以根据需要使用 Ollama 提供的其他嵌入模型或 [Hugging Face 模型](https://spring.io/blog/2024/10/22/leverage-the-power-of-45k-free-hugging-face-models-with-spring-ai-and-ollama)。

    3. 在开发过程中使用 Testcontainers

        虽然 Testcontainers 主要用于集成测试，但我们也可以在本地开发过程中使用它。

        为此，我们在 `src/test/java` 目录中创建一个单独的主类：

        ```java
        class TestApplication {

            public static void main(String[] args) {
                SpringApplication.from(Application::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
            }
        }
        ```

        我们创建了 `TestApplication` 类，并在其 `main` 方法中启动主 `Application` 类，并附加 `TestcontainersConfiguration` 类。

        此设置帮助我们在本地设置和管理外部服务。我们可以运行 Spring Boot 应用程序，并让它连接到通过 Testcontainers 启动的外部服务。

4. 在应用程序启动时填充 ChromaDB

    现在我们已设置好本地环境，让我们在应用程序启动时用一些示例数据填充 ChromaDB 向量数据库。

    1. 从 PoetryDB 获取诗歌记录

        为演示目的，我们将使用 [PoetryDB API](https://poetrydb.org/index.html) 获取诗歌。

        让我们为此创建一个 `PoetryFetcher` 工具类：

        ```java
        class PoetryFetcher {

            private static final String BASE_URL = "https://poetrydb.org/author/";
            private static final String DEFAULT_AUTHOR_NAME = "Shakespeare";

            public static List<Poem> fetch() {
                return fetch(DEFAULT_AUTHOR_NAME);
            }

            public static List<Poem> fetch(String authorName) {
                return RestClient
                .create()
                .get()
                .uri(URI.create(BASE_URL + authorName))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
            }

        }

        record Poem(String title, List<String> lines) {}
        ```

        我们使用 `RestClient` 调用 PoetryDB API，并传入指定的 `authorName`。为将 API 响应反序列化为 `Poem` 记录列表，我们使用了 `ParameterizedTypeReference`，无需显式指定泛型响应类型，Java 会自动推断类型。

        我们还重载了无参的 `fetch()` 方法，用于获取莎士比亚的诗歌。我们将在下一节中使用此方法。

    2. 将文档存储到 ChromaDB 向量数据库

        现在，为了在应用程序启动时用诗歌填充 ChromaDB 向量数据库，我们创建一个实现 `ApplicationRunner` 接口的 `VectorStoreInitializer` 类：

        ```java
        @Component
        class VectorStoreInitializer implements ApplicationRunner {

            private final VectorStore vectorStore;

            // standard constructor

            @Override
            public void run(ApplicationArguments args) {
                List<Document> documents = PoetryFetcher
                .fetch()
                .stream()
                .map(poem -> {
                    Map<String, Object> metadata = Map.of("title", poem.title());
                    String content = String.join("\n", poem.lines());
                    return new Document(content, metadata);
                })
                .toList();
                vectorStore.add(documents);
            }

        }
        ```

        在 `VectorStoreInitializer` 中，我们自动装配了一个 `VectorStore` 实例。

        在 `run()` 方法中，我们使用 `PoetryFetcher` 工具类获取 `Poem` 记录列表。然后，我们将每首诗映射为一个 `Document`，将诗行作为内容，标题作为元数据。

        最后，我们将所有文档存储到向量数据库中。当我们调用 `add()` 方法时，Spring AI 会自动将我们的纯文本内容转换为向量表示后再存储到向量数据库中，无需我们显式使用 `EmbeddingModel` Bean 进行转换。

        默认情况下，Spring AI 使用 `SpringAiCollection` 作为集合名称在向量数据库中存储数据，但我们可以通过 `spring.ai.vectorstore.chroma.collection-name` 属性覆盖它。

5. 测试语义搜索

    ChromaDB 向量数据库填充完成后，让我们验证语义搜索功能：

    ```java
    private static final int MAX_RESULTS = 3;

    @ParameterizedTest
    @ValueSource(strings = {"Love and Romance", "Time and Mortality", "Jealousy and Betrayal"})
    void whenSearchingShakespeareTheme_thenRelevantPoemsReturned(String theme) {
        SearchRequest searchRequest = SearchRequest
        .builder()
        .query(theme)
        .topK(MAX_RESULTS)
        .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        assertThat(documents)
        .hasSizeLessThanOrEqualTo(MAX_RESULTS)
        .allSatisfy(document -> {
            String title = String.valueOf(document.getMetadata().get("title"));
            assertThat(title)
                .isNotBlank();
            });
    }
    ```

    这里，我们使用 [@ValueSource](https://www.baeldung.com/parameterized-tests-junit-5#1-simple-values) 向测试方法传入一些常见的莎士比亚主题。然后，我们创建一个 `SearchRequest` 对象，以主题作为查询，`MAX_RESULTS` 作为期望结果数量。

    接着，我们使用 `searchRequest` 调用 `vectorStore` Bean 的 `similaritySearch()` 方法。与 `VectorStore` 的 `add()` 方法类似，Spring AI 会在查询向量数据库前将我们的查询转换为其向量表示。

    返回的文档将包含与给定主题语义相关的诗歌，即使它们不包含确切的关键词。

6. 结论

    在本文中，我们探索了如何将 ChromaDB 向量数据库与 Spring AI 集成。

    通过 Testcontainers，我们启动了 ChromaDB 和 Ollama 服务的 Docker 容器，创建了一个本地测试环境。

    我们介绍了如何在应用程序启动时从 PoetryDB API 填充诗歌数据到向量数据库。然后，我们使用常见的诗歌主题验证了语义搜索功能。
