# [Jackson ObjectMapper简介](https://www.baeldung.com/jackson-object-mapper-tutorial)

1. 一览表

    本教程侧重于了解Jackson ObjectMapper类，以及如何将Java对象序列化为JSON并将JSON字符串反序列化为Java对象。

2. 依赖性

    让我们首先向pom.xml添加以下依赖项：

    ```xml
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.2</version>
    </dependency>
    ```

    此依赖项还将传递性地将以下库添加到类路径中：

    1. jackson-annotations
    2. jackson-core

3. 使用ObjectMapper阅读和写作

    让我们从基本的读写操作开始。

    ObjectMapper的简单readValue API是一个很好的切入点。我们可以用它来解析或反序列化JSON内容到Java对象中。

    此外，在写入方面，我们可以使用writeValue API将任何Java对象序列化为JSON输出。

    我们将在本文中使用以下带有两个字段的汽车类作为序列化或反序列化的对象：

    ```java
    public class Car {

        private String color;
        private String type;

        // standard getters setters
    }
    ```

    1. Java对象到JSON

        让我们看看使用ObjectMapper类的writeValue方法将Java对象序列化到JSON的第一个示例：

        ```java
        ObjectMapper objectMapper = new ObjectMapper();
        Car car = new Car("yellow", "renault");
        objectMapper.writeValue(new File("target/car.json"), car);
        ```

        文件中上述的输出将是：

        `{"color":"yellow","type":"renault"}`

        ObjectMapper类的writeValueAsString和writeValueAsBytes方法从Java对象生成JSON，并将生成的JSON作为字符串或字节数组返回：

        `String carAsString = objectMapper.writeValueAsString(car);`

    2. JSON到Java对象

        以下是使用ObjectMapper类将JSON字符串转换为Java对象的简单示例：

        ```java
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
        Car car = objectMapper.readValue(json, Car.class);
        ```

        readValue（）函数还接受其他形式的输入，例如包含JSON字符串的文件：

        `Car car = objectMapper.readValue(new File("src/test/resources/json_car.json"), Car.class);`

        或URL：

        ```java
        Car car =
            objectMapper.readValue(new URL("file:src/test/resources/json_car.json"), Car.class);
        ```

    3. JSON到Jackson JsonNode

        或者，JSON可以解析为JsonNode对象，并用于从特定节点检索数据：

        ```java
        String json = "{ \"color\" : \"Black\", \"type\" : \"FIAT\" }";
        JsonNode jsonNode = objectMapper.readTree(json);
        String color = jsonNode.get("color").asText();
        // Output: color -> Black
        ```

    4. 从JSON数组字符串创建Java列表

        我们可以使用TypeReference将数组形式的JSON解析到Java对象列表中：

        ```java
        String jsonCarArray =
            "[{ \"color\" : \"Black\", \"type\" : \"BMW\" }, { \"color\" : \"Red\", \"type\" : \"FIAT\" }]";
        List<Car> listCar = objectMapper.readValue(jsonCarArray, new TypeReference<List<Car>>(){});
        ```

        3.5。从JSON字符串创建Java地图

        同样，我们可以将JSON解析为Java地图：

        ```java
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
        Map<String, Object> map
            = objectMapper.readValue(json, new TypeReference<Map<String,Object>>(){});
        ```

4. 高级功能

    Jackson库的最大优势之一是高度可定制的序列化和反序列化过程。

    在本节中，我们将介绍一些高级功能，其中输入或输出JSON响应可能与生成或消耗响应的对象不同。

    1. 配置序列化或反序列化功能

        在将JSON对象转换为Java类时，如果JSON字符串有一些新字段，默认过程将导致异常：

        ```java
        String jsonString
            = "{ \"color\" : \"Black\", \"type\" : \"Fiat\", \"year\" : \"1970\" }";
        ```

        在Class Car的Java对象的默认解析过程中，上述示例中的JSON字符串将导致UnrecognizedPropertyException异常。

        通过配置方法，我们可以扩展默认流程以忽略新字段：

        ```java
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Car car = objectMapper.readValue(jsonString, Car.class);
        JsonNode jsonNodeRoot = objectMapper.readTree(jsonString);
        JsonNode jsonNodeYear = jsonNodeRoot.get("year");
        String year = jsonNodeYear.asText();
        ```

        另一个选项基于FAIL_ON_NULL_FOR_PRIMITIVES，该选项定义了原始值的空值是否允许：

        `objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);`

        同样，FAIL_ON_NUMBERS_FOR_ENUM控制是否允许列举值作为数字序列化/反序列化：

        `objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);`

        您可以在[官方网站](https://github.com/FasterXML/jackson-databind/wiki/Serialization-Features)上找到序列化和反序列化功能的综合列表。

    2. 创建自定义序列化器或反序列化器

        ObjectMapper类的另一个基本功能是能够注册自定义序列化器和反序列化器。

        在输入或输出JSON响应结构与必须序列化或反序列化的Java类不同的情况下，自定义序列化器和反序列化器非常有用。

        以下是自定义JSON序列化器的示例：

        ```java
        public class CustomCarSerializer extends StdSerializer<Car> {

            public CustomCarSerializer() {
                this(null);
            }

            public CustomCarSerializer(Class<Car> t) {
                super(t);
            }

            @Override
            public void serialize(
            Car car, JsonGenerator jsonGenerator, SerializerProvider serializer) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("car_brand", car.getType());
                jsonGenerator.writeEndObject();
            }
        }
        ```

        此自定义序列化器可以像这样调用：

        ```java
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module =
            new SimpleModule("CustomCarSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(Car.class, new CustomCarSerializer());
        mapper.registerModule(module);
        Car car = new Car("yellow", "renault");
        String carJson = mapper.writeValueAsString(car);
        ```

        以下是汽车在客户端的外观（作为JSON输出）：

        `var carJson = {"car_brand":"renault"}`

        以下是自定义JSON反序列化器的示例：

        ```java
        public class CustomCarDeserializer extends StdDeserializer<Car> {

            public CustomCarDeserializer() {
                this(null);
            }

            public CustomCarDeserializer(Class<?> vc) {
                super(vc);
            }

            @Override
            public Car deserialize(JsonParser parser, DeserializationContext deserializer) {
                Car car = new Car();
                ObjectCodec codec = parser.getCodec();
                JsonNode node = codec.readTree(parser);
                
                // try catch block
                JsonNode colorNode = node.get("color");
                String color = colorNode.asText();
                car.setColor(color);
                return car;
            }
        }
        ```

        可以通过以下方式调用此自定义反序列化器：

        ```java
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module =
            new SimpleModule("CustomCarDeserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(Car.class, new CustomCarDeserializer());
        mapper.registerModule(module);
        Car car = mapper.readValue(json, Car.class);
        ```

    3. 处理日期格式

        java.util.Date的默认序列化生成一个数字，即epoch时间戳（自1970年1月1日UTC以来的毫秒数）。但这不太能被人类读取，需要进一步的转换才能以人类可读的格式显示。

        让我们用datePurchased属性在Request类中包装我们迄今为止使用的Car实例：

        ```java
        public class Request
        {
            private Car car;
            private Date datePurchased;

            // standard getters setters
        }
        ```

        要控制日期的字符串格式并将其设置为，例如，yyyy-MM-dd HH:mm a z，请考虑以下片段：

        ```java
        ObjectMapper objectMapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
        objectMapper.setDateFormat(df);
        String carAsString = objectMapper.writeValueAsString(request);
        // output: {"car":{"color":"yellow","type":"renault"},"datePurchased":"2016-07-03 11:43 AM CEST"}
        ```

        想了解更多关于与Jackson连载约会的信息，请阅读我们更深入的文章。

        请注意，在某些情况下，我们在创建SimpleDateFormat时也需要指定Locale，以便无论该机器运行的区域如何，它都能在所有机器上提供一致的输出。

        为了指定Locale我们可以做：

        `SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm a z", Locale.ENGLISH);`

    4. 处理收藏品

        通过DeserializationFeature类提供的另一个小而有用的功能是能够从JSON数组响应中生成我们想要的集合类型。

        例如，我们可以将结果生成为数组：

        ```java
        String jsonCarArray =
            "[{ \"color\" : \"Black\", \"type\" : \"BMW\" }, { \"color\" : \"Red\", \"type\" : \"FIAT\" }]";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        Car[] cars = objectMapper.readValue(jsonCarArray, Car[].class);
        // print cars
        ```

        或者作为列表：

        ```java
        String jsonCarArray =
            "[{ \"color\" : \"Black\", \"type\" : \"BMW\" }, { \"color\" : \"Red\", \"type\" : \"FIAT\" }]";
        ObjectMapper objectMapper = new ObjectMapper();
        List<Car> listCar = objectMapper.readValue(jsonCarArray, new TypeReference<List<Car>>(){});
        // print cars
        ```

        有关与Jackson一起处理收藏品的更多信息，请参阅此处。

5. ObjectMapper的构建器模式

    到目前为止，我们已经学习了配置ObjectMapper实例的不同方法。在本节中，我们将对ObjectMapperBuilder类进行原型，以创建ObjectMapper类的不可变实例。

    1. ObjectMapperBuilder类

        让我们从使用一些配置参数创建ObjectMapperBuilder类开始，即enableIdentation、preserveOrder和dateFormat：

        ```java
        public class ObjectMapperBuilder {
            private boolean enableIndentation;
            private boolean preserveOrder;
            private DateFormat dateFormat;
        }
        ```

        我们必须注意，ObjectMapper实例有几种可能的配置。我们只关注构建器原型的用例的可能配置的子集。

        接下来，让我们添加方法，允许我们在创建ObjectMapper类的实例时将相应的配置属性设置为构建器：

        ```java
        ObjectMapperBuilder enableIndentation() {
            this.enableIndentation = true;
            return this;
        }

        ObjectMapperBuilder dateFormat() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));
            this.dateFormat = simpleDateFormat;
            return this;
        }

        ObjectMapperBuilder preserveOrder(boolean order) {
            this.preserveOrder = order;
            return this;
        }
        ```

        最后，让我们添加build（）方法，以返回带有配置参数的最终ObjectMapper实例：

        ```java
        public ObjectMapper build() {
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, this.enableIndentation);
            objectMapper.setDateFormat(this.dateFormat);
            if (this.preserveOrder) {
                objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
            }

            return objectMapper;
        }
        ```

        太好了！我们已经成功为ObjectMapper实例制作了构建器类的原型。

    2. 行动中的建设者

        让我们从在ObjectMapperBuilder类的帮助下创建ObjectMapper类的单个实例开始：

        ```java
        ObjectMapper mapper = new ObjectMapperBuilder()
            .enableIndentation()
            .dateFormat()
            .preserveOrder(true)
            .build();
        ```

        现在，让我们定义一个Car类的实例及其序列化的JSON字符串：

        ```java
        Car givenCar = new Car("White", "Sedan");
        String givenCarJsonStr = "{ \"color\" : \"White\", \"type\" : \"Sedan\" }";
        ```

        继续，让我们使用映射器对象对给定的CarJsonStr进行反序列化：

        ```java
        Car actual = mapper.readValue(givenCarJsonStr, Car.class);
        Assertions.assertEquals("White", actual.getColor());
        Assertions.assertEquals("Sedan", actual.getType());
        ```

        完美！看起来我们弄对了这个。

        最后，让我们验证请求类实例的序列化流程：

        ```java
        Request request = new Request();
        request.setCar(givenCar);
        Date date = new Date(1684909857000L);
        request.setDatePurchased(date);

        String actual = mapper.writeValueAsString(request);
        String expected = "{\n" + "  \"car\" : {\n" + "    \"color\" : \"White\",\n" +
            "    \"type\" : \"Sedan\"\n" + "  },\n" + "  \"datePurchased\" : \"2023-05-24 12:00 PM IST\"\n" +
            "}";
        Assertions.assertEquals(expected, actual);
        ```

        太好了！我们已经通过ObjectMapperBuilder类成功验证了反序列化和序列化操作。

6. 结论

    Jackson是一个适用于Java的坚实而成熟的JSON序列化/反序列化库。ObjectMapper API提供了一种直接的解析和生成JSON响应对象的方法，非常灵活。此外，我们对ObjectMapperBuilder类进行了原型，以创建ObjectMapper类的不可变实例。本文讨论了使图书馆如此受欢迎的主要特征。
