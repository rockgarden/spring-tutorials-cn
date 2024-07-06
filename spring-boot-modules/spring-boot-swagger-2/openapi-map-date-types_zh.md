# [使用 OpenAPI 生成器映射日期类型](https://www.baeldung.com/openapi-map-date-types)

1. 概述

    在本教程中，我们将了解如何使用 OpenAPI 映射日期。我们将学习如何处理各种日期格式。

    有两种不同的 Maven 插件可以根据 OpenAPI 规范生成代码：swagger-codegen 和 openapi-generator。我们将讨论如何使用这两个插件。

2. 示例设置

    首先，让我们建立一个示例。我们将编写初始 YAML 文件和 Maven 插件的基本配置。

    1. 基础 YAML 文件

        我们将使用 YAML 文件来描述我们的 API。请注意，我们将使用 OpenAPI 规范的第三个版本。

        我们需要为文件添加标题和版本，以符合规范。此外，我们将路径部分留空。不过，在组件部分，我们将定义一个事件对象，它暂时只有一个属性，即组织者：

        ```yml
        openapi: 3.0.0
        info:
        title: an example api with dates
        version: 0.1.0
        paths:
        components:
        schemas:
            Event:
            type: object
            properties:
                organizer:
                type: string
        ```

    2. swagger-codegen 插件配置

        swagger-codegen 插件的最新版本可在 [Maven Central Repository](https://mvnrepository.com/artifact/io.swagger/swagger-codegen-maven-plugin) 中找到。让我们从插件的基本配置开始：

        ```xml
        <plugin>
            <groupId>io.swagger.codegen.v3</groupId>
            <artifactId>swagger-codegen-maven-plugin</artifactId>
            <version>3.0.52</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <inputSpec>${project.basedir}/src/main/resources/static/event.yaml</inputSpec>
                        <language>spring</language>
                        <configOptions>
                            <java8>true</java8>
                        </configOptions>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        ```

        现在我们可以执行插件了：

        `mvn clean compile`

        事件类是根据 OpenAPI 规范生成的，包括构造函数、获取器和设置器。它的 [equals()](https://www.baeldung.com/java-equals-hashcode-contracts#equals)、hashcode() 和 [toString()](https://www.baeldung.com/java-tostring) 方法也被重写。

    3. openapi-generator 插件配置

        同样，openapi-generator 插件的最新版本可从 [Maven Central Repository](https://mvnrepository.com/artifact/org.openapitools/openapi-generator-maven-plugin) 获取。现在让我们对其进行基本配置：

        ```xml
        <plugin>
            <groupId>org.openapitools</groupId>
            <artifactId>openapi-generator-maven-plugin</artifactId>
            <version>6.2.1</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <skipValidateSpec>true</skipValidateSpec>
                        <inputSpec>${project.basedir}/src/main/resources/static/event.yaml</inputSpec>
                        <generatorName>spring</generatorName>
                        <configOptions>
                            <java8>true</java8>
                            <openApiNullable>false</openApiNullable>
                            <interfaceOnly>true</interfaceOnly>
                        </configOptions>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        ```

        根据 OpenAPI 规范，我们的 YAML 文件中路径部分为空是没有问题的。但是，openapi-generator 默认会拒绝它。因此，我们将 skipValidateSpec 标志设置为 true。

        我们还在选项列表中将 openApiNullable 属性设置为 false，否则，插件就会要求我们添加 jackson-databing-nullable 的依赖关系，而我们并不需要这样做。

        我们还将 interfaceOnly 设置为 true，主要是为了避免生成不必要的 Spring Boot 集成测试。

        在这种情况下，运行编译 Maven 阶段也会生成包含所有方法的 Event 类。

3. OpenAPI 标准日期映射

    OpenAPI 定义了几种基本数据类型：字符串是其中之一。在字符串数据类型中，OpenAPI 定义了两种处理日期的默认格式：date 和 date-time。

    1. 日期

        日期格式指的是 [RFC 3339 第 5.6 节](https://www.rfc-editor.org/rfc/rfc3339#section-5.6) 定义的全日期符号。例如，2023-02-08 就是这样的日期。

        现在，让我们在事件定义中添加一个日期格式的 startDate 属性：

        ```yml
        startDate:
        type: string
        format: date
        ```

        我们不需要更新 Maven 插件的配置。让我们再次生成事件类。我们可以看到，生成的文件中出现了一个新属性：

        ```java
        @JsonProperty("startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        ```

        这两个插件的主要区别在于，swagger-codegen 没有用 [@DateTimeFormat](https://www.baeldung.com/spring-date-parameters#convert-date-parameters-on-request-level) 来注解 startDate 属性。两个插件还以相同的方式创建了相关的 getter、setter 和构造函数。

        我们可以看到，生成器的默认行为是使用 [LocalDate](https://www.baeldung.com/java-8-date-time-intro#1-working-with-localdate) 类来设置日期格式。

    2. 日期-时间

        date-time 格式指的是 RFC 3339 第 5.6 节定义的 date-time 符号。例如，2023-02-08T18:04:28Z 就符合这种格式。

        现在，让我们为事件添加一个日期-时间格式的 endDate 属性：

        ```yml
        endDate:
        type: string
        format: date-time
        ```

        再一次，我们不需要修改任何插件的配置。当我们再次生成 Event 类时，一个新属性就会出现：

        ```java
        @JsonProperty("endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private OffsetDateTime endDate;
        ```

        我们对日期格式的评论仍然有效：与 openapi-generator 相反，swagger-codegen 并未使用 @DateTimeFormat 对属性进行注解。此外，插件还创建了相关的 getter、setter 和构造函数。

        我们可以看到，生成器默认使用 [OffsetDateTime](https://www.baeldung.com/java-convert-date-to-offsetdatetime) 类来表示日期时间格式。

4. 使用其他标准日期类

    现在，我们不再生成默认类，而是强制插件为每种格式使用特定的类。

    让我们编辑 swagger-codegen Maven 插件配置：

    ```xml
    <configuration>
        <inputSpec>${project.basedir}/src/main/resources/static/event.yaml</inputSpec>
        <language>spring</language>
        <configOptions>
            <java8>true</java8>
            <dateLibrary>custom</dateLibrary>
        </configOptions>
        <typeMappings>
            <typeMapping>DateTime=Instant</typeMapping>
            <typeMapping>Date=Date</typeMapping>
        </typeMappings>
        <importMappings>
            <importMapping>Instant=java.time.Instant</importMapping>
            <importMapping>Date=java.util.Date</importMapping>
        </importMappings>
    </configuration>
    ```

    让我们仔细看看新的一行：

    - 我们使用带有自定义值的 dateLibrary 选项：这意味着我们将定义自己的日期类，而不是使用标准类
    - 在 importMappings 部分，我们告诉插件导入 Instant 和 Date 类，并告诉插件在哪里查找它们
    - typeMappings 部分是所有神奇之处：我们告诉插件使用 Instant 来处理日期时间格式，使用 Date 来处理日期格式。

    对于 openapi-generator，我们需要在完全相同的位置添加完全相同的行。结果只是略有不同，因为我们一开始就定义了更多的选项：

    ```xml
    <configuration>
        <skipValidateSpec>true</skipValidateSpec>
        <inputSpec>${project.basedir}/src/main/resources/static/event.yaml</inputSpec>
        <generatorName>spring</generatorName>
        <configOptions>
            <java8>true</java8>
            <dateLibrary>custom</dateLibrary>
            <openApiNullable>false</openApiNullable>
            <interfaceOnly>true</interfaceOnly>
        </configOptions>
        <typeMappings>
            <typeMapping>DateTime=Instant</typeMapping>
            <typeMapping>Date=Date</typeMapping>
        </typeMappings>
        <importMappings>
            <importMapping>Instant=java.time.Instant</importMapping>
            <importMapping>Date=java.util.Date</importMapping>
        </importMappings>
    </configuration>
    ```

    现在让我们生成文件并看一看：

    ```java
    import java.time.Instant;
    import java.util.Date;

    (...)
        @JsonProperty("startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private Date startDate;

        @JsonProperty("endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Instant endDate;
    ```

    插件确实用 Date 对象替换了日期格式，用 Instant 替换了日期时间格式。和之前一样，两个插件的唯一区别是 swagger-codegen 没有用 @DateTimeFormat 对属性进行注释。

    最后，但并非最不重要的一点是，插件不进行任何验证。例如，我们可以使用 [java.lang.Math](https://www.baeldung.com/java-lang-math) 类来模拟日期格式，代码仍然可以成功生成。

5. 使用自定义日期模式

    现在我们来讨论最后一种可能性。如果出于某种原因，我们确实无法依赖任何标准的日期 API，那么我们可以使用 String 来处理日期。在这种情况下，我们需要定义希望字符串遵循的验证模式。

    例如，让我们在事件对象规范中添加 ticketSales 日期。ticketSales 的格式为 DD-MM-YYYY，如 18-07-2024：

    ```yml
    ticketSales:
    type: string
    description: Beginning of the ticket sales
    example: "01-01-2023"
    pattern: "[0-9]{2}-[0-9]{2}-[0-9]{4}"
    ```

    我们可以看到，我们定义了 ticketSales 必须匹配的正则表达式。请注意，该模式不能区分 DD-MM-YYYY 和 MM-DD-YYYY。此外，我们还为该字段添加了说明和示例：由于我们不按标准方式处理日期，因此说明似乎很有帮助。

    我们不需要对插件的配置进行任何更改。让我们使用 openapi-generator 生成事件类：

    ```java
    @JsonProperty("ticketSales")
    private String ticketSales;

    (...)

    /**
     * Beginning of the ticket sales
     * @return ticketSales
    */
    @Pattern(regexp = "[0-9]{2}-[0-9]{2}-[0-9]{4}") 
    @Schema(name = "ticketSales", example = "01-01-2023", description = "Beginning of the ticket sales", required = false)
    public String getTicketSales() {
    return ticketSales;
    }
    ```

    正如我们所见，getter 被注释为已定义的 Pattern。因此，我们需要为 [javax.validation](https://www.baeldung.com/javax-validation#1-validation-api) 添加一个依赖关系，以使其工作：

    ```xml
    <dependency>
        <groupId>javax.validation</groupId>
        <artifactId>validation-api</artifactId>
        <version>2.0.1.Final</version>
    </dependency>
    ```

    swagger-codegen 插件生成的代码非常相似。

6. 结论

    在本文中，我们看到 swagger-codegen 和 openapi-generator Maven 插件都为日期和时间处理提供了内置格式。如果我们想使用其他标准的 Java 日期 API，可以覆盖插件的配置。如果实在无法使用任何日期 API，我们可以将日期存储为字符串，然后手动指定验证模式。
