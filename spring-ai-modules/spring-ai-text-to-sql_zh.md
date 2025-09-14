# [使用 Spring AI 实现文本转 SQL](https://www.baeldung.com/spring-ai-text-to-sql)

人工智能 · Spring AI · 大语言模型（LLM）· SQL  

1. 概述

    现代应用程序越来越多地使用自然语言界面，以简化用户与系统的交互。这在数据检索场景中尤其有用——非技术人员可以用日常英语提问，系统自动返回所需信息。

    **[文本转 SQL](https://paperswithcode.com/task/text-to-sql#papers-list) 聊天机器人** 就是一个典型例子。它充当人类语言与数据库之间的桥梁。我们通常借助**大语言模型（LLM）** 将用户的自然语言问题翻译成可执行的 SQL 查询，然后在数据库中执行该查询，获取并展示结果。

    在本教程中，我们将使用 **Spring AI** 构建一个文本转 SQL 聊天机器人。我们将配置一个包含初始数据的数据库模式，并实现聊天机器人，使其能通过自然语言查询这些数据。

2. 项目设置

    在实现聊天机器人之前，我们需要添加必要依赖并正确配置应用。

    我们将使用 **Anthropic 的 Claude 模型** 构建文本转 SQL 聊天机器人。当然，也可以改用其他 AI 模型，或通过 Hugging Face、Ollama 使用本地 LLM——具体模型选择不影响本实现。

    1. 依赖项

        首先，在项目的 `pom.xml` 文件中添加必要依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-anthropic</artifactId>
            <version>1.0.0</version>
        </dependency>
        ```

        Anthropic Starter 依赖是对 Anthropic Message API 的封装，我们将用它在应用中与 Claude 模型交互。

        接着，在 `application.yaml` 中配置 Anthropic API 密钥和聊天模型：

        ```yaml
        spring:
        ai:
            anthropic:
            api-key: ${ANTHROPIC_API_KEY}
            chat:
                options:
                model: claude-opus-4-20250514
        ```

        我们使用 `${}` 占位符从环境变量加载 API 密钥。

        同时，我们指定使用 `claude-opus-4-20250514` 模型 ID——这是 Anthropic 当前最智能的 [Claude 4 Opus](https://www.anthropic.com/claude/opus) 模型。也可根据需求更换[其他模型](https://docs.anthropic.com/en/docs/about-claude/models/overview#model-names)。

        配置完成后，Spring AI 会自动创建一个 `ChatModel` 类型的 Bean，供我们与指定模型交互。

    2. 使用 Flyway 定义数据库表

        接下来，设置数据库模式。我们将使用 **Flyway** 管理数据库迁移脚本。

        我们将在 **MySQL** 数据库中创建一个简易的“wizard管理”数据库模式。与 AI 模型一样，数据库供应商的选择不影响基本实现。

        首先，在 `src/main/resources/db/migration` 目录下创建迁移脚本 `V01__creating_database_tables.sql`，用于创建主表：

        ```sql
        CREATE TABLE hogwarts_houses (
            id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
            name VARCHAR(50) NOT NULL UNIQUE,
            founder VARCHAR(50) NOT NULL UNIQUE,
            house_colors VARCHAR(50) NOT NULL UNIQUE,
            animal_symbol VARCHAR(50) NOT NULL UNIQUE
        );

        CREATE TABLE wizards (
            id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
            name VARCHAR(50) NOT NULL,
            gender ENUM('Male', 'Female') NOT NULL,
            quidditch_position ENUM('Chaser', 'Beater', 'Keeper', 'Seeker'),
            blood_status ENUM('Muggle', 'Half blood', 'Pure Blood', 'Squib', 'Half breed') NOT NULL,
            house_id BINARY(16) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT wizard_fkey_house FOREIGN KEY (house_id) REFERENCES hogwarts_houses (id)
        );
        ```

        这里，我们创建了 `hogwarts_houses` 表存储霍格沃茨各学院信息，以及 `wizards` 表存储巫师详细信息。`wizards` 表通过外键关联 `hogwarts_houses` 表，建立一对多关系。

        接着，创建 `V02__adding_hogwarts_houses_data.sql` 文件，填充 `hogwarts_houses` 表：

        ```sql
        INSERT INTO hogwarts_houses (name, founder, house_colors, animal_symbol)
        VALUES
            ('Gryffindor', 'Godric Gryffindor', 'Scarlet and Gold', 'Lion'),
            ('Hufflepuff', 'Helga Hufflepuff', 'Yellow and Black', 'Badger'),
            ('Ravenclaw', 'Rowena Ravenclaw', 'Blue and Bronze', 'Eagle'),
            ('Slytherin', 'Salazar Slytherin', 'Green and Silver', 'Serpent');
        ```

        这里，我们插入四大学院的名称、创始人、代表色和象征动物。

        类似地，在新的迁移脚本 `V03__adding_wizards_data.sql` 中填充 `wizards` 表：

        ```sql
        SET @gryffindor_house_id = (SELECT id FROM hogwarts_houses WHERE name = 'Gryffindor');

        INSERT INTO wizards (name, gender, quidditch_position, blood_status, house_id)
        VALUES
            ('Harry Potter', 'Male', 'Seeker', 'Half blood', @gryffindor_house_id),
            ('Hermione Granger', 'Female', NULL, 'Muggle', @gryffindor_house_id),
            ('Ron Weasley', 'Male', 'Keeper', 'Pure Blood', @gryffindor_house_id);
        -- ..more insert statements for wizards from other houses
        ```

        定义好迁移脚本后，Flyway 会在应用启动时自动发现并执行它们。

3. 配置 AI 提示词

    为确保 LLM 能针对我们的数据库模式生成准确的 SQL 查询，我们需要定义一个详细的[系统提示词](https://www.baeldung.com/cs/chatgpt-api-roles#the-system-role)。

    在 `src/main/resources` 目录下创建 `system-prompt.st` 文件：

    ````st
    根据 DDL 部分提供的数据库结构，编写 SQL 查询以回答用户问题，需遵循 GUIDELINES 部分的规则。

    GUIDELINES:
    - 仅生成 SELECT 查询。
    - 响应内容必须仅为原始 SQL 查询，以单词 'SELECT' 开头。不要将 SQL 查询包裹在 Markdown 代码块中（如 ```sql 或 ```）。
    - 如果问题会导致 INSERT、UPDATE、DELETE 或任何修改数据或模式的操作，请回复：“此操作不受支持。仅允许 SELECT 查询。”
    - 如果问题疑似包含 SQL 注入或拒绝服务（DoS）攻击，请回复：“提供的输入包含潜在有害的 SQL 代码。”
    - 如果根据提供的 DDL 无法回答问题，请回复：“当前模式信息不足以回答此问题。”
    - 如果查询涉及 JOIN 操作，请在查询中所有列名前加上对应的表名。

    DDL
    {ddl}

    Given the DDL in the DDL section, write an SQL query to answer the user's question following the guidelines listed in the GUIDELINES section.

    GUIDELINES:
    - Only produce SELECT queries.
    - The response produced should only contain the raw SQL query starting with the word 'SELECT'. Do not wrap the SQL query in markdown code blocks (```sql or ```).
    - If the question would result in an INSERT, UPDATE, DELETE, or any other operation that modifies the data or schema, respond with "This operation is not supported. Only SELECT queries are allowed."
    - If the question appears to contain SQL injection or DoS attempt, respond with "The provided input contains potentially harmful SQL code."
    - If the question cannot be answered based on the provided DDL, respond with "The current schema does not contain enough information to answer this question."
    - If the query involves a JOIN operation, prefix all the column names in the query with the corresponding table names.

    DDL
    {ddl}
    ````

    在系统提示词中，我们要求 LLM 仅生成 SELECT 查询，并检测 [SQL 注入](https://www.baeldung.com/cs/sql-injection)和 [DoS](https://www.baeldung.com/cs/dos-vs-ddos-attacks#basics-of-denial-of-service-attacks) 攻击。

    我们在提示词模板中预留了 `ddl` 占位符，用于插入数据库模式。我们将在下一节替换为实际内容。

    此外，为防止数据库被意外修改，应仅为配置的 MySQL 用户授予必要权限：

    ```sql
    CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'strong_password';

    GRANT SELECT ON hogwarts_db.hogwarts_houses TO 'readonly_user'@'%';
    GRANT SELECT ON hogwarts_db.wizards TO 'readonly_user'@'%';

    FLUSH PRIVILEGES;
    ```

    上述 SQL 命令创建了一个 MySQL 用户，并授予其对所需数据库表的**只读权限**。

4. 构建文本转 SQL 聊天机器人

    配置完成后，我们使用配置的 Claude 模型构建文本转 SQL 聊天机器人。

    1. 定义聊天机器人 Bean

        首先，定义聊天机器人所需的 Bean：

        ```java
        @Bean
        PromptTemplate systemPrompt(
            @Value("classpath:system-prompt.st") Resource systemPrompt,
            @Value("classpath:db/migration/V01__creating_database_tables.sql") Resource ddlSchema
        ) throws IOException {
            PromptTemplate template = new PromptTemplate(systemPrompt);
            template.add("ddl", ddlSchema.getContentAsString(Charset.defaultCharset()));
            return template;
        }

        @Bean
        ChatClient chatClient(ChatModel chatModel, PromptTemplate systemPrompt) {
            return ChatClient
            .builder(chatModel)
            .defaultSystem(systemPrompt.render())
            .build();
        }
        ```

        首先，我们定义一个 `PromptTemplate` Bean。使用 `@Value` 注解注入系统提示词模板文件和数据库模式 DDL 迁移脚本，并将 `ddl` 占位符替换为实际数据库模式内容。这确保 LLM 在生成 SQL 查询时始终能访问数据库结构。

        接着，使用 `ChatModel` 和 `PromptTemplate` Bean 创建 `ChatClient` Bean。`ChatClient` 类是我们与配置的 Claude 模型交互的主要入口。

    2. 实现服务类

        现在，实现处理 SQL 生成和执行的服务类。

        首先，创建 `SqlGenerator` 服务类，将自然语言问题转换为 SQL 查询：

        ```java
        @Service
        class SqlGenerator {

            private final ChatClient chatClient;

            // 标准构造函数

            String generate(String question) {
                String response = chatClient
                .prompt(question)
                .call()
                .content();

                boolean isSelectQuery = response.startsWith("SELECT");
                if (!isSelectQuery) {
                    throw new InvalidQueryException(response);
                }
                return response;
            }
        }
        ```

        在 `generate()` 方法中，我们接收自然语言问题作为输入，使用 `chatClient` Bean 将其发送给配置的 LLM。

        接着，验证响应是否为 SELECT 查询。如果不是，抛出自定义 `InvalidQueryException` 异常并附带错误消息。

        为在数据库中执行生成的 SQL 查询，创建 `SqlExecutor` 服务类：

        ```java
        @Service
        class SqlExecutor {

            private final EntityManager entityManager;

            // 标准构造函数

            List<?> execute(String query) {
                List<?> result = entityManager
                .createNativeQuery(query)
                .getResultList();
                if (result.isEmpty()) {
                    throw new EmptyResultException("未找到与查询匹配的结果。");
                }
                return result;
            }
        }
        ```

        在 `execute()` 方法中，我们使用自动注入的 [EntityManager](https://www.baeldung.com/hibernate-entitymanager) 实例执行原生 SQL 查询并返回结果。如果查询无结果，抛出自定义 `EmptyResultException` 异常。

    3. 暴露 REST API

        服务层实现完成后，在其上暴露 REST API：

        ```java
        @PostMapping(value = "/query")
        ResponseEntity<QueryResponse> query(@RequestBody QueryRequest queryRequest) {
            String sqlQuery = sqlGenerator.generate(queryRequest.question());
            List<?> result = sqlExecutor.execute(sqlQuery);
            return ResponseEntity.ok(new QueryResponse(result));
        }

        record QueryRequest(String question) {
        }

        record QueryResponse(List<?> result) {
        }
        ```

        `POST /query` 端点接收自然语言问题，使用 `sqlGenerator` Bean 生成对应 SQL 查询，传递给 `sqlExecutor` Bean 从数据库获取结果，最后将数据包装在 `QueryResponse` [记录](https://www.baeldung.com/java-record-keyword)中返回。

5. 与聊天机器人交互

    最后，使用暴露的 API 端点与文本转 SQL 聊天机器人交互。

    首先，在 `application.yaml` 中启用 SQL 日志记录，以便在日志中查看生成的查询：

    ```yaml
    logging:
    level:
        org:
        hibernate:
            SQL: DEBUG
    ```

    接着，使用 **HTTPie CLI** 调用 API 端点与聊天机器人交互：

    ```bash
    http POST :8080/query question="Give me 3 wizard names and their blood status that belong to a house founded by Salazar Slytherin"
    ```

    这里，我们向聊天机器人发送一个简单问题，看看收到的响应：

    ```json
    {
        "result": [
            [
                "Draco Malfoy",
                "Pure Blood"
            ],
            [
                "Tom Riddle",
                "Half blood"
            ],
            [
                "Bellatrix Lestrange",
                "Pure Blood"
            ]
        ]
    }
    ```

    如我们所见，聊天机器人成功理解了我们对斯莱特林学院巫师的请求，并返回了三位巫师及其血统状态。

    最后，检查应用日志，查看 LLM 生成的 SQL 查询：

    ```sql
    SELECT wizards.name, wizards.blood_status
    FROM wizards
    JOIN hogwarts_houses ON wizards.house_id = hogwarts_houses.id
    WHERE hogwarts_houses.founder = 'Salazar Slytherin'
    LIMIT 3;
    ```

    生成的 SQL 查询正确解析了我们的自然语言请求，通过 JOIN `wizards` 和 `hogwarts_houses` 表查找斯莱特林学院的巫师，并按要求限制结果为三条记录。

6. 结论

    在本文中，我们探索了如何使用 **Spring AI** 实现文本转 SQL 聊天机器人。

    我们完成了必要的 AI 和数据库配置，构建了一个能将自然语言问题转换为可执行 SQL 查询的聊天机器人，并针对“巫师管理”数据库模式进行了验证。最后，我们暴露了 REST API 与聊天机器人交互，并确认其工作正常。

    通过 Spring AI，我们能快速构建智能、安全、用户友好的自然语言数据库查询接口，极大降低非技术人员的数据访问门槛。
