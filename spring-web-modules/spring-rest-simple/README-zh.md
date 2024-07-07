# Spring REST Simple

## Spring REST API 中的二进制数据格式

1. 概述

    在 REST API 中，JSON 和 XML 是广受欢迎的数据传输格式，但它们并不是唯一的选择。

    还有许多其他格式，它们的序列化速度和序列化数据大小各不相同。

    在本文中，我们将探讨如何配置 Spring REST 机制以使用二进制数据格式，并以 Kryo 为例进行说明。

    此外，我们还展示了如何通过添加对 Google 协议缓冲区的支持来支持多种数据格式。

2. HttpMessageConverter

    HttpMessageConverter 接口基本上是 Spring 用于转换 REST 数据格式的公共 API。

    有不同的方法来指定所需的转换器。在这里，我们实现了 WebMvcConfigurer，并在重载的 configureMessageConverters 方法中明确提供了要使用的转换器：

    ```java
    @Configuration
    @EnableWebMvc
    @ComponentScan({ "com.baeldung.web" })
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
            //...
        }
    }
    ```

3. 克里奥

    1. Kryo 概述和 Maven

        Kryo 是一种二进制编码格式，与基于文本的格式相比，它具有良好的序列化和反序列化速度以及较小的传输数据大小。

        虽然理论上它可以用于在不同类型的系统之间传输数据，但它主要是为 Java 组件设计的。

        我们通过以下 Maven 依赖关系添加必要的 Kryo 库：

        ```xml
        <dependency>
            <groupId>com.esotericsoftware</groupId>
            <artifactId>kryo</artifactId>
            <version>4.0.0</version>
        </dependency>
        ```

    2. Spring REST 中的 Kryo

        为了使用 Kryo 作为数据传输格式，我们创建了一个自定义的 HttpMessageConverter，并实现了必要的序列化和反序列化逻辑。此外，我们还为 Kryo 定义了自定义 HTTP 标头：application/x-kryo。下面是一个完整的简化工作示例，用于演示目的：

        ```java
        public class KryoHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

            public static final MediaType KRYO = new MediaType("application", "x-kryo");

            private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
                @Override
                protected Kryo initialValue() {
                    Kryo kryo = new Kryo();
                    kryo.register(Foo.class, 1);
                    return kryo;
                }
            };

            public KryoHttpMessageConverter() {
                super(KRYO);
            }

            @Override
            protected boolean supports(Class<?> clazz) {
                return Object.class.isAssignableFrom(clazz);
            }

            @Override
            protected Object readInternal(
            Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException {
                Input input = new Input(inputMessage.getBody());
                return kryoThreadLocal.get().readClassAndObject(input);
            }

            @Override
            protected void writeInternal(
            Object object, HttpOutputMessage outputMessage) throws IOException {
                Output output = new Output(outputMessage.getBody());
                kryoThreadLocal.get().writeClassAndObject(output, object);
                output.flush();
            }

            @Override
            protected MediaType getDefaultContentType(Object object) {
                return KRYO;
            }
        }
        ```

        请注意，我们在这里使用 ThreadLocal 只是因为创建 Kryo 实例的成本很高，我们希望尽可能多地重新利用这些实例。

        控制器方法非常简单（注意无需任何自定义协议数据类型，我们使用的是普通的 Foo DTO）：

        ```java
        @RequestMapping(method = RequestMethod.GET, value = "/foos/{id}")
        @ResponseBody
        public Foo findById(@PathVariable long id) {
            return fooRepository.findById(id);
        }
        ```

        再做一个快速测试，以证明我们已将所有内容正确连接在一起：

        ```java
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new KryoHttpMessageConverter()));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(KryoHttpMessageConverter.KRYO));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<Foo> response = restTemplate.exchange("http://localhost:8080/spring-rest/foos/{id}",
        HttpMethod.GET, entity, Foo.class, "1");
        Foo resource = response.getBody();

        assertThat(resource, notNullValue());
        ```

4. 支持多种数据格式

    通常情况下，您需要为同一服务提供多种数据格式支持。客户端在接受 HTTP 标头中指定所需的数据格式，然后调用相应的消息转换器将数据序列化。

    通常情况下，你只需注册另一个转换器，就能让事情开箱即用。Spring 会根据 Accept 标头中的值和转换器中声明的支持媒体类型自动选择合适的转换器。

    例如，要添加对 JSON 和 Kryo 的支持，请同时注册 KryoHttpMessageConverter 和 MappingJackson2HttpMessageConverter：

    ```java
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        messageConverters.add(new KryoHttpMessageConverter());
        super.configureMessageConverters(messageConverters);
    }
    ```

    现在，假设我们还想在列表中添加 Google 协议缓冲区。在本例中，我们假设有一个基于以下 proto 文件、由 protoc 编译器生成的类 FooProtos.Foo：

    ```proto
    package baeldung;
    option java_package = "com.baeldung.web.dto";
    option java_outer_classname = "FooProtos";
    message Foo {
        required int64 id = 1;
        required string name = 2;
    }
    ```

    Spring 内置了对协议缓冲的支持。我们只需在支持的转换器列表中加入 ProtobufHttpMessageConverter，就能使其正常工作：

    ```java
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        messageConverters.add(new KryoHttpMessageConverter());
        messageConverters.add(new ProtobufHttpMessageConverter());
    }
    ```

    但是，我们必须定义一个单独的控制器方法来返回 FooProtos.Foo 实例（JSON 和 Kryo 都处理 Foos，所以控制器中不需要做任何改动来区分两者）。

    有两种方法可以解决调用哪种方法的模糊问题。第一种方法是为 protobuf 和其他格式使用不同的 URL。例如，对于 protobuf

    ```java
    @RequestMapping(method = RequestMethod.GET, value = "/fooprotos/{id}")
    @ResponseBody
    public FooProtos.Foo findProtoById(@PathVariable long id) { … }
    ```

    而其他格式则使用

    ```java
    @RequestMapping(method = RequestMethod.GET, value = "/foos/{id}")
    @ResponseBody
    public Foo findById(@PathVariable long id) { … }
    ```

    注意，对于 protobuf，我们使用 value = "/fooprotos/{id}"，而对于其他格式，我们使用 value = "/foos/{id}"。

    第二种更好的方法是使用相同的 URL，但在 protobuf 的请求映射中明确指定生成的数据格式：

    ```java
    @RequestMapping(
    method = RequestMethod.GET, 
    value = "/foos/{id}", 
    produces = { "application/x-protobuf" })
    @ResponseBody
    public FooProtos.Foo findProtoById(@PathVariable long id) { … }
    ```

    请注意，通过在 produces 注解属性中指定媒体类型，我们向 Spring 的底层机制提示了需要根据客户端提供的 Accept 标头中的值使用哪种映射，因此在 "foos/{id}" URL 需要调用哪种方法的问题上不会有歧义。URL 需要调用哪种方法的问题上不会产生歧义。

    第二种方法使我们能够为客户端提供统一一致的 REST API，适用于所有数据格式。

    最后，如果您有兴趣深入了解如何在 Spring REST API 中使用协议缓冲区，请参阅参考[文章](https://www.baeldung.com/spring-rest-api-with-protocol-buffers)。

5. 注册额外的消息转换器

    需要注意的是，当你覆盖 configureMessageConverters 方法时，你将失去所有默认的消息转换器。只有您提供的转换器才会被使用。

    虽然有时这正是你想要的，但在很多情况下，你只是想添加新的转换器，同时保留默认的转换器，因为默认转换器已经可以处理 JSON 等标准数据格式。为此，请覆盖 extendMessageConverters 方法：

    ```java
    @Configuration
    @EnableWebMvc
    @ComponentScan({ "com.baeldung.web" })
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
            messageConverters.add(new ProtobufHttpMessageConverter());
            messageConverters.add(new KryoHttpMessageConverter());
        }
    }
    ```

6. 总结

    在本教程中，我们了解了在 Spring MVC 中使用任何数据传输格式有多简单，并以 Kryo 为例进行了检验。

    我们还展示了如何添加对多种格式的支持，以便不同的客户端能够使用不同的格式。

## Relevant articles

- [ ] [Spring and Apache FileUpload](https://www.baeldung.com/spring-apache-file-upload)
- [ ] [Test a REST API with curl](https://www.baeldung.com/curl-rest)
- [ ] [Best Practices for REST API Error Handling](https://www.baeldung.com/rest-api-error-handling-best-practices)
- [ ] [Binary Data Formats in a Spring REST API](https://www.baeldung.com/spring-rest-api-with-binary-data-formats)

## Code

Spring REST API 教程中二进制数据格式的实现当然在 [Github](https://github.com/eugenp/tutorials/tree/master/spring-web-modules/spring-rest-simple) 上。
