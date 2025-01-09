# [Java中的JSON](https://www.baeldung.com/java-json)

Jackson

Gson

1. 概述  

    在Java中处理JSON数据可以很简单，但就像Java中的大多数事情一样，我们有很多选项和库可以选择。  

    本指南旨在帮助您更轻松地做出选择，并让您对当前的生态系统有一个扎实的理解。我们将讨论Java中最常见的JSON处理库：  

    - Jackson  
    - Gson  
    - json-io  
    - Genson  

    我们为每个库遵循一个简单的结构——首先是一些有用的资源（包括Baeldung上的内容以及外部资源），然后我们将通过一个基本的代码示例来了解如何使用该库。  

2. 流行度和基本统计  

    首先，让我们从一些统计数据开始，作为每个库流行度的参考：  

    1. [Jackson](https://github.com/FasterXML/jackson)

        - Maven使用量：[data-bind](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind) (2362), [core](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core) (1377)

    2. [Gson](https://github.com/google/gson)

        - [Maven](https://mvnrepository.com/artifact/com.google.code.gson/gson)使用量：1588

    3. [json-io](https://github.com/jdereg/json-io)

        - [Maven](https://mvnrepository.com/artifact/com.cedarsoftware/json-io)使用量：11

    4. [Genson](https://github.com/owlike/genson)

        - [Maven](https://mvnrepository.com/artifact/com.owlike/genson)使用量：8

    5. [JSON-P](https://github.com/jakartaee/jsonp-api)

3. Jackson  

    接下来，让我们看看其中最流行的库——Jackson。Jackson是一个多用途的Java库，用于处理JSON数据。  

    1. 有用资源  

        以下是一些官方资源：  

        - [Jackson官方Wiki](https://github.com/FasterXML/jackson-docs)  
        - [Github上的Jackson](https://github.com/FasterXML/jackson)  

        在Baeldung上的内容：  

        - [Jackson教程](https://www.baeldung.com/jackson)  
        - [Jackson日期处理](https://www.baeldung.com/jackson-serialize-dates)  
        - [Jackson JSON视图](https://www.baeldung.com/jackson-json-view-annotation)  
        - [Jackson注解指南](https://www.baeldung.com/jackson-annotations)  
        - [Jackson异常处理](https://www.baeldung.com/jackson-exception-handling)  
        - [Jackson自定义序列化/反序列化](https://www.baeldung.com/jackson-custom-serialization)  

        其他有趣的教程：  

        - [Java中的Jackson JSON处理API示例教程](https://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial)  
        - [Jackson ObjectMapper](https://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial)  
        - [Jackson 2 – Java对象与JSON的转换](https://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial)  

    2. Maven依赖  

        要使用该库，请在`pom.xml`中添加以下Maven依赖项：  

        ```xml
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.2</version>
        </dependency>
        ```  

        确保获取最新版本的Jackson。  

    3. Jackson的简单示例  

        现在，让我们看一个使用Jackson的简单示例：  

        ```java
        @Test
        public void whenSerializeAndDeserializeUsingJackson_thenCorrect() throws IOException {
            Foo foo = new Foo(1, "first");
            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(foo);
            Foo result = mapper.readValue(jsonStr, Foo.class);
            assertEquals(foo.getId(), result.getId());
        }
        ```  

        注意：  

        - `ObjectMapper.writeValueAsString()`用于将对象序列化为JSON字符串。  
        - `ObjectMapper.readValue()`用于将JSON字符串反序列化为Java对象。  

        示例JSON输出：  

        ```json
        {
            "id": 1,
            "name": "first"
        }
        ```  

4. Gson  

    Gson是我们要看的下一个Java JSON库。  

    1. 有用资源  

        以下是一些官方资源：  

        - [Github上的Gson](https://github.com/google/gson)  
        - [Gson用户指南](https://github.com/google/gson/blob/master/UserGuide.md)  

        在Baeldung上的内容：  

        - [Gson序列化指南](https://www.baeldung.com/gson-serialization-guide)  
        - [Gson反序列化指南](https://www.baeldung.com/gson-deserialization-guide)  

        其他有趣的教程：  

        - [Gson排除策略](https://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial)  
        - [Gson自定义序列化/反序列化](https://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial)  
        - [Java Gson + JSON教程](https://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial)  

    2. Maven依赖  

        ```xml
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>${gson.version}</version>
        </dependency>
        ```  

        注意：Gson的最新版本是2.8.8。  

    3. Gson的简单示例  

        以下是一个简单的示例，展示了如何使用Gson进行序列化/反序列化：  

        ```java
        @Test
        public void whenSerializeAndDeserializeUsingGson_thenCorrect() {
            Gson gson = new Gson();
            Foo foo = new Foo(1, "first");
            String jsonStr = gson.toJson(foo);
            Foo result = gson.fromJson(jsonStr, Foo.class);
            assertEquals(foo.getId(), result.getId());
        }
        ```  

        注意：  

        - `Gson.toJson()`用于将对象序列化为JSON。  
        - `Gson.fromJson()`用于将JSON反序列化为Java对象。  

5. Json-io  

    Json-io是一个简单的Java库，用于序列化/反序列化JSON。  

    1. 有用资源  

        以下是一些官方资源：  

        - [Google Code上的json-io](https://code.google.com/archive/p/json-io/)  
        - [Github上的json-io](https://github.com/jdereg/json-io)  

    2. Maven依赖  

        ```xml
        <dependency>
            <groupId>com.cedarsoftware</groupId>
            <artifactId>json-io</artifactId>
            <version>${json-io.version}</version>
        </dependency>
        ```  

        注意：json-io的最新版本是4.13.0。  

    3. Json-io的简单示例  

        现在，让我们看一个使用json-io的简单示例：  

        ```java
        @Test
        public void whenSerializeAndDeserializeUsingJsonio_thenCorrect() {
            Foo foo = new Foo(1, "first");

            String jsonStr = JsonWriter.objectToJson(foo);
            Foo result = (Foo) JsonReader.jsonToJava(jsonStr);
            assertEquals(foo.getId(), result.getId());
        }
        ```  

        注意：  

        - `JsonWriter.objectToJson()`用于将对象序列化为JSON。  
        - `JsonReader.jsonToJava()`用于将JSON反序列化为Java对象。  

        示例JSON输出：  

        ```json
        {
            "@type": "org.baeldung.Foo",
            "id": 1,
            "name": "first"
        }
        ```  

6. Genson  

    Genson是一个用于Java和Scala的JSON转换库，提供完整的数据绑定和流处理功能。  

    1. 有用资源  

        以下是一些官方资源：  

        - [Genson官方网站](http://genson.io/)  
        - [Github上的Genson](https://github.com/owlike/genson)  
        - [Genson用户指南](http://genson.io/Documentation/UserGuide/)  
        - [Genson字节数组的JSON格式](http://genson.io/Documentation/UserGuide/#byte-arrays)  

    2. Maven依赖  

        ```xml
        <dependency>
            <groupId>com.owlike</groupId>
            <artifactId>genson</artifactId>
            <version>${genson.version}</version>
        </dependency>
        ```  

        注意：Genson的最新版本是1.6。  

    3. Genson的简单示例  

        以下是一个使用Genson的简单示例：  

        ```java
        @Test
        public void whenSerializeAndDeserializeUsingGenson_thenCorrect() {
            Genson genson = new Genson();
            Foo foo = new Foo(1, "first");

            String jsonStr = genson.serialize(foo);
            Foo result = genson.deserialize(jsonStr, Foo.class);
            assertEquals(foo.getId(), result.getId());
        }
        ```  

        注意：  

        - `Genson.serialize()`用于将对象序列化为JSON。  
        - `Genson.deserialize()`用于将JSON反序列化为Java对象。  

7. JSON-P  

    JSON-P是一个用于解析、构建、转换和查询JSON消息的Java API。[Java规范请求（JSR）353](https://jcp.org/en/jsr/detail?id=353)提出了该API。JSR 353旨在开发一个处理JSON的Java API。大多数流行的库（如Jackson、Gson等）并未直接实现该规范。  

    1. 有用资源  

        以下是一些官方资源：  

        - [JSON-P官方网站](https://javaee.github.io/jsonp/)  
        - [Github上的JSON-P](https://github.com/javaee/jsonp)  

    2. Maven依赖  

        ```xml
        <dependency>
            <groupId>jakarta.json</groupId>
            <artifactId>jakarta.json-api</artifactId>
            <version>${jsonp.version}</version>
        </dependency>

        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>jakarta.json</artifactId>
            <version>${provider.version}</version>
            <classifier>module</classifier>
        </dependency>
        ```  

        注意：我们需要API模块和默认提供者的依赖项。最新版本分别为2.1.1和2.0.1。  

    3. JSON-P的简单示例  

        让我们看一个使用JSON-P的简单示例：  

        ```java
        @Test
        public void whenTransformingAndParsingUsingJsonp_thenCorrect() {
            Foo foo = new Foo(1, "First");
            JsonObject json = Json.createObjectBuilder()
            .add("id", foo.getId())
            .add("name", foo.getName())
            .build();
            String result = json.toString();
                
            JsonParser parser = Json.createParser(new StringReader(result));
            while (parser.hasNext()) {
                Event event = parser.next();
                switch (event) {
                    case VALUE_STRING:
                        String value = parser.getString();
                        assertEquals(foo.getName(), value);
                        break;
                }
            }
            parser.close();
        }
        ```  

        注意：  

        - `Json.createObjectBuilder()`用于将对象转换为JSON。  
        - `Json.createParser()`用于解析JSON消息。  
        - JSON-P API相比其他Java JSON处理库更为底层。  

8. 结论  

    在这篇快速概述文章中，我们了解了Java中最常见的JSON处理库。  
