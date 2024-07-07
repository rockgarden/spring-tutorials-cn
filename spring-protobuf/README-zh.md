# Spring and Protobuf

## 使用协议缓冲区的 Spring REST API

1. 概述

    协议缓冲区（Protocol Buffers）是一种语言和平台中立的结构化数据序列化和反序列化机制，其创建者 Google 声称它比 XML 和 JSON 等其他类型的有效载荷更快、更小、更简单。

    本教程将指导您设置 REST API，以利用这种基于二进制的消息结构。

2. 协议缓冲区

    本节将介绍有关协议缓冲区的一些基本信息，以及它们在 Java 生态系统中的应用。

    1. 协议缓冲区简介

        为了使用协议缓冲区，我们需要在 .proto 文件中定义消息结构。每个文件都描述了可能从一个节点传输到另一个节点或存储在数据源中的数据。下面是 .proto 文件的一个示例，文件名为 baeldung.proto，位于 src/main/resources 目录中。本教程稍后将使用该文件：

        baeldung.proto

        在本教程中，我们使用协议缓冲区编译器和协议缓冲区语言的第 3 版，因此 .proto 文件必须以 syntax = "proto3" 声明开始。如果使用的是版本 2 的编译器，则可以省略该声明。接下来是包声明，即该报文结构的命名空间，以避免与其他项目的命名冲突。

        下面两个声明仅用于 Java：java_package 选项指定了我们生成的类所处的包，而 java_outer_classname 选项则指明了包含该 .proto 文件中定义的所有类型的类名。

        下面的 2.3 小节将介绍其余元素以及如何将这些元素编译成 Java 代码。

    2. 使用 Java 的协议缓冲区

        在定义了消息结构后，我们需要一个编译器将这些语言中立的内容转换为 Java 代码。您可以按照[Protocol Buffers 资源库](https://github.com/google/protobuf)中的说明获取合适的编译器版本。或者，您也可以从 Maven 中央资源库中搜索 com.google.protobuf:protoc 工具，下载预编译的二进制编译器，然后根据您的平台选择合适的版本。

        然后，将编译器复制到项目的 src/main 目录，并在命令行中执行以下命令：

        `protoc --java_out=java resources/baeldung.proto`

        这将在 com.baeldung.protobuf 包中生成 BaeldungTraining 类的源文件，正如 baeldung.proto 文件的选项声明中所指定的那样。

        除编译器外，还需要协议缓冲运行时。这可以通过在 Maven POM 文件中添加以下依赖项来实现：

        ```xml
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>xxxx</version>
        </dependency>
        ```

        我们可以使用其他版本的运行时，前提是它与编译器的版本相同。

    3. 编译消息描述

        通过使用编译器，.proto 文件中的消息会被编译成静态嵌套的 Java 类。在上例中，Course 和 Student 消息分别被转换为 Course 和 Student Java 类。同时，消息的字段也被编译成这些生成类型中 JavaBeans 风格的获取器和设置器。由等号和数字组成的标记位于每个字段声明的末尾，是用于将相关字段编码为二进制形式的唯一标记。

        我们将浏览消息的类型字段，看看这些字段是如何转换为访问方法的。

        让我们从课程消息开始。它有两个简单的字段，包括 id 和 course_name。它们的协议缓冲区类型（int32 和 string）被转换为 Java int 和 String 类型。下面是编译后的相关获取器（为简洁起见，不包括实现）：

        ```java
        public int getId();
        public java.lang.String getCourseName();
        ```

        请注意，键入字段的名称应使用蛇形大小写（单个单词用下划线分隔），以便与其他语言保持一致。编译器将根据 Java 惯例将这些名称转换为驼峰大小写。

        课程信息的最后一个字段 student 属于 Student 复杂类型，下文将对此进行说明。该字段的前缀是重复关键字，这意味着它可以重复任意次数。编译器会生成与 student 字段相关的一些方法如下（不含实现）：

        ```java
        public java.util.List<com.baeldung.protobuf.BaeldungTraining.Student> getStudentList();
        public int getStudentCount();
        public com.baeldung.protobuf.BaeldungTraining.Student getStudent(int index);
        ```

        现在我们来看看 Student 消息，它是 Course 消息中 student 字段的复杂类型。其简单字段（包括 id、first_name、last_name 和 email）用于创建 Java 访问器方法：

        ```java
        public int getId();
        public java.lang.String getFirstName();
        public java.lang.String getLastName();
        public java.lang.String.getEmail();
        ```

        最后一个字段电话属于 PhoneNumber 复杂类型。与课程消息的学生字段类似，该字段也是重复的，并且有几个相关的方法：

        ```java
        public java.util.List<com.baeldung.protobuf.BaeldungTraining.Student.PhoneNumber> getPhoneList();
        public int getPhoneCount();
        public com.baeldung.protobuf.BaeldungTraining.Student.PhoneNumber getPhone(int index);
        ```

        PhoneNumber 消息被编译成 BaeldungTraining.Student.PhoneNumber 嵌套类型，其中有两个获取器与消息的字段相对应：

        ```java
        public java.lang.String getNumber();
        public com.baeldung.protobuf.BaeldungTraining.Student.PhoneType getType();
        ```

        PhoneType 是 PhoneNumber 消息类型字段的复合类型，它是一个枚举类型，将被转换为嵌套在 BaeldungTraining.Student 类中的 Java 枚举类型：

        ```java
        public enum PhoneType implements com.google.protobuf.ProtocolMessageEnum {
            MOBILE(0),
            LANDLINE(1),
            UNRECOGNIZED(-1),
            ;
            // Other declarations
        }
        ```

3. Spring REST API 中的 Protobuf

    本节将指导你使用 Spring Boot 设置 REST 服务。

    1. Bean 声明

        让我们从 @SpringBootApplication 的定义开始：

        ```java
        @SpringBootApplication
        public class Application {
            @Bean
            ProtobufHttpMessageConverter protobufHttpMessageConverter() {
                return new ProtobufHttpMessageConverter();
            }

            @Bean
            public CourseRepository createTestCourses() {
                Map<Integer, Course> courses = new HashMap<>();
                Course course1 = Course.newBuilder()
                .setId(1)
                .setCourseName("REST with Spring")
                .addAllStudent(createTestStudents())
                .build();
                Course course2 = Course.newBuilder()
                .setId(2)
                .setCourseName("Learn Spring Security")
                .addAllStudent(new ArrayList<Student>())
                .build();
                courses.put(course1.getId(), course1);
                courses.put(course2.getId(), course2);
                return new CourseRepository(courses);
            }

            // Other declarations
        }
        ```

        ProtobufHttpMessageConverter Bean 用于将 @RequestMapping 注解方法返回的响应转换为协议缓冲区消息。

        另一个 Bean，即 CourseRepository，包含了我们 API 的一些测试数据。

        这里最重要的是，我们使用的是特定于协议缓冲区的数据，而不是标准的 POJO。

        下面是 CourseRepository 的简单实现：

        protobuf/CourseRepository.java

    2. 控制器配置

        我们可以如下定义测试 URL 的 @Controller 类：

        protobuf/CourseController.java

        再次重申，重要的是我们从控制器层返回的课程 DTO 不是标准的 POJO。这将是它在传输回客户端之前被转换为协议缓冲消息的触发器。

4. REST 客户端和测试

    既然我们已经了解了简单的 API 实现，现在让我们用两种方法来说明客户端协议缓冲区消息的反序列化。

    第一种是利用 RestTemplate API 和预配置的 ProtobufHttpMessageConverter Bean 自动转换消息。

    第二种是使用 protobuf-java-format 将协议缓冲区的响应手动转换为 JSON 文档。

    首先，我们需要为集成测试设置上下文，并指示 Spring Boot 在 Application 类中查找配置信息，方法是声明一个测试类，如下所示：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = Application.class)
    @WebIntegrationTest
    public class ApplicationTest {
        // Other declarations
    }
    ```

    本节中的所有代码片段都将放在 ApplicationTest 类中。

    1. 预期响应

        访问 REST 服务的第一步是确定请求 URL：

        `private static final String COURSE1_URL = "http://localhost:8080/courses/1";`

        这个 COURSE1_URL 将用于从我们之前创建的 REST 服务中获取第一个测试双课程。向上述 URL 发送 GET 请求后，将使用以下断言验证相应的响应：

        ```java
        private void assertResponse(String response) {
            assertThat(response, containsString("id"));
            assertThat(response, containsString("course_name"));
            assertThat(response, containsString("REST with Spring"));
            assertThat(response, containsString("student"));
            assertThat(response, containsString("first_name"));
            assertThat(response, containsString("last_name"));
            assertThat(response, containsString("email"));
            assertThat(response, containsString("john.doe@baeldung.com"));
            assertThat(response, containsString("richard.roe@baeldung.com"));
            assertThat(response, containsString("jane.doe@baeldung.com"));
            assertThat(response, containsString("phone"));
            assertThat(response, containsString("number"));
            assertThat(response, containsString("type"));
        }
        ```

        我们将在后续小节涉及的两个测试用例中使用该辅助方法。

    2. 使用 RestTemplate 进行测试

        下面是我们如何创建客户端，向指定目标发送 GET 请求，以协议缓冲区消息的形式接收响应，并使用 RestTemplate API 进行验证：

        ```java
        @Autowired
        private RestTemplate restTemplate;

        @Test
        public void whenUsingRestTemplate_thenSucceed() {
            ResponseEntity<Course> course = restTemplate.getForEntity(COURSE1_URL, Course.class);
            assertResponse(course.toString());
        }
        ```

        要使该测试用例正常工作，我们需要在配置类中注册一个 RestTemplate 类型的 Bean：

        ```java
        @Bean
        RestTemplate restTemplate(ProtobufHttpMessageConverter hmc) {
            return new RestTemplate(Arrays.asList(hmc));
        }
        ```

        还需要另一个 ProtobufHttpMessageConverter 类型的 Bean 来自动转换接收到的协议缓冲区消息。该 bean 与第 3.1 小节中定义的 bean 相同。由于本教程中客户端和服务器共享同一个应用上下文，我们可以在应用程序类中声明 RestTemplate Bean，并重新使用 ProtobufHttpMessageConverter Bean。

    3. 使用 HttpClient 进行测试

        使用 HttpClient API 并手动转换协议缓冲区消息的第一步是在 Maven POM 文件中添加以下两个依赖项：

        ```xml
        <dependency>
            <groupId>com.googlecode.protobuf-java-format</groupId>
            <artifactId>protobuf-java-format</artifactId>
            <version>1.4</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.2</version>
        </dependency>
        ```

        有关这些依赖项的最新版本，请查看 Maven 中央仓库中的 protobuf-java-format 和 httpclient 工件。

        让我们继续创建客户端，执行 GET 请求，并使用给定的 URL 将相关响应转换为 InputStream 实例：

        ```java
        private InputStream executeHttpRequest(String url) throws IOException {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(request);
            return httpResponse.getEntity().getContent();
        }
        ```

        现在，我们将把 InputStream 对象形式的协议缓冲区消息转换为 JSON 文档：

        ```java
        private String convertProtobufMessageStreamToJsonString(InputStream protobufStream) throws IOException {
            JsonFormat jsonFormat = new JsonFormat();
            Course course = Course.parseFrom(protobufStream);
            return jsonFormat.printToString(course);
        }
        ```

        下面是一个测试用例如何使用上面声明的私有辅助方法并验证响应：

        ```java
        @Test
        public void whenUsingHttpClient_thenSucceed() throws IOException {
            InputStream responseStream = executeHttpRequest(COURSE1_URL);
            String jsonOutput = convertProtobufMessageStreamToJsonString(responseStream);
            assertResponse(jsonOutput);
        }
        ```

    4. JSON 中的响应

        为了清楚起见，我们在前面各小节中描述的测试中收到的响应的 JSON 格式都包含在此：

        ```json
        id: 1
        course_name: "REST with Spring"
        student {
            id: 1
            first_name: "John"
            last_name: "Doe"
            email: "john.doe@baeldung.com"
            phone {
                number: "123456"
            }
        }
        student {
            id: 2
            first_name: "Richard"
            last_name: "Roe"
            email: "richard.roe@baeldung.com"
            phone {
                number: "234567"
                type: LANDLINE
            }
        }
        student {
            id: 3
            first_name: "Jane"
            last_name: "Doe"
            email: "jane.doe@baeldung.com"
            phone {
                number: "345678"
            }
            phone {
                number: "456789"
                type: LANDLINE
            }
        }
        ```

5. 结论

    本教程快速介绍了协议缓冲区，并说明了如何使用 Spring 格式设置 REST API。然后，我们介绍了客户端支持和序列化-解序列化机制。

## 在 JSON 和 Protobuf 之间转换

1. 概述

    在本教程中，我们将演示如何将 JSON 转换为 Protobuf，以及如何将 Protobuf 转换为 JSON。

    Protobuf 是一种免费开源的跨平台数据格式，用于序列化结构化数据。

2. Maven 依赖

    首先，让我们创建一个 Spring Boot 项目，加入 protobuf-java-util 依赖：

    ```xml
    <dependency>
        <groupId>com.google.protobuf</groupId>
        <artifactId>protobuf-java-util</artifactId>
        <version>3.21.5</version>
    </dependency>
    ```

3. 将 JSON 转换为 Protobuf

    我们可以使用 [JsonFormat](https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat) 将 JSON 转换为 protobuf 消息。JsonFormat 是一个实用工具类，用于将 protobuf 消息转换为/从 JSON 格式。JsonFormat 的 parser() 创建了一个 [Parser](https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.Parser.html)，该 Parser 使用 merge() 方法将 JSON 解析为 protobuf 消息。

    让我们创建一个接收 JSON 并生成 protobuf 消息的方法：

    ```java
    public static Message fromJson(String json) throws IOException {
        Builder structBuilder = Struct.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(json, structBuilder);
        return structBuilder.build();
    }
    ```

    让我们使用下面的 JSON 示例：

    ```json
    {
        "boolean": true,
        "color": "gold",
        "object": {
        "a": "b",
        "c": "d"
        },
        "string": "Hello World"
    }
    ```

    现在，让我们编写一个简单的测试来验证从 JSON 到 protobuf 消息的转换：

    ```java
    @Test
    public void givenJson_convertToProtobuf() throws IOException {
        Message protobuf = ProtobufUtil.fromJson(jsonStr);
        Assert.assertTrue(protobuf.toString().contains("key: \"boolean\""));
        Assert.assertTrue(protobuf.toString().contains("string_value: \"Hello World\""));
    }
    ```

4. 将 protobuf 转换为 JSON

    我们可以使用 JsonFormat 的 printer() 方法将 protobuf 消息转换为 JSON，该方法接受作为 MessageOrBuilder 的 protobuf：

    ```java
    public static String toJson(MessageOrBuilder messageOrBuilder) throws IOException {
        return JsonFormat.printer().print(messageOrBuilder);
    }
    ```

    让我们编写一个简单的测试来验证从 protobuf 到 JSON 消息的转换：

    ```java
    @Test
    public void givenProtobuf_convertToJson() throws IOException {
        Message protobuf = ProtobufUtil.fromJson(jsonStr);
        String json = ProtobufUtil.toJson(protobuf);
        Assert.assertTrue(json.contains("\"boolean\": true"));
        Assert.assertTrue(json.contains("\"string\": \"Hello World\""));
        Assert.assertTrue(json.contains("\"color\": \"gold\""));
    }
    ```

5. 结束语

    在本文中，我们演示了如何将 JSON 转换为 protobuf，反之亦然。

## Relevant Articles

- [ ] [Spring REST API with Protocol Buffers](https://www.baeldung.com/spring-rest-api-with-protocol-buffers)
- [ ] [Convert between JSON and Protobuf](https://www.baeldung.com/java-convert-json-protobuf)

## Code

所有示例和代码片段的实现都可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-protobuf) 项目中找到。
