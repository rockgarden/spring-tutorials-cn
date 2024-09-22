# [使用Spring MVC返回图像/媒体数据](https://www.baeldung.com/spring-mvc-image-media-data)

1. 一览表

    在本教程中，我们将说明如何使用Spring MVC框架返回图像和其他媒体。

    我们将讨论几种方法，从直接操作HttpServletResponse开始，然后转向受益于消息转换、内容协商和Spring资源抽象的方法。我们将仔细观察他们每个方式，并讨论他们的优缺点。

2. 使用HttpServletResponse

    图像下载最基本的方法是直接针对响应对象工作，并模仿纯Servlet实现，并使用以下片段演示：

    ```java
    @RequestMapping(value = "/image-manual-response", method = RequestMethod.GET)
    public void getImageAsByteArray(HttpServletResponse response) throws IOException {
        InputStream in = servletContext.getResourceAsStream("/WEB-INF/images/image-example.jpg");
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        IOUtils.copy(in, response.getOutputStream());
    }
    ```

    发布以下请求将在浏览器中渲染图像：

    <http://localhost:8080/spring-mvc-xml/image-manual-response.jpg>

    由于来自org.apache.commons.io软件包的IOUtils，实现相当直接和简单。然而，这种方法的缺点是，它对潜在变化的抵御力不强。mime类型是硬编码的，更改转换逻辑或外部化图像位置需要更改代码。

3. 使用HttpMessageConverter

    上一节讨论了一种不利用Spring MVC框架的[消息转换](https://www.baeldung.com/spring-httpmessageconverter-rest)和内容协商功能的基本方法。要引导这些功能，我们需要：

    - 用@ResponseBody注释注释控制器方法
    - 根据控制器方法的返回类型注册适当的消息转换器（例如，将字节数组正确转换为图像文件所需的ByteArrayHttpMessageConverter）

    1. 配置

        为了展示转换器的配置，我们将使用内置的ByteArrayHttpMessageConverter，每当方法返回字节[]类型时，该转换器都会转换消息。

        ByteArrayHttpMessageConverter默认注册，但配置与任何其他内置或自定义转换器相似。

        应用消息转换器bean需要在Spring MVC上下文中注册适当的MessageConverter bean，并设置它应该处理的媒体类型。您可以使用`<mvc:message-converters>`标签通过XML来定义它。

        此标签应在`<mvc:annotation-driven>`标签中定义，如以下示例所示：

        ```xml
        <mvc:annotation-driven>
            <mvc:message-converters>
                <bean class="org.springframework.http.converter.ByteArrayHttpMessageConverter">
                    <property name="supportedMediaTypes">
                        <list>
                            <value>image/jpeg</value>
                            <value>image/png</value>
                        </list>
                    </property>
                </bean>
            </mvc:message-converters>
        </mvc:annotation-driven>
        ```

        上述配置部分将注册图像/jpeg和图像/png响应内容类型的ByteArrayHttpMessageConverter。如果`<mvc:message-converters>`标签在mvc配置中不存在，那么默认的转换器集将被注册。

        此外，您可以使用Java配置注册消息转换器：

        ```java
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(byteArrayHttpMessageConverter());
        }

        @Bean
        public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
            ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
            arrayHttpMessageConverter.setSupportedMediaTypes(getSupportedMediaTypes());
            return arrayHttpMessageConverter;
        }

        private List<MediaType> getSupportedMediaTypes() {
            List<MediaType> list = new ArrayList<MediaType>();
            list.add(MediaType.IMAGE_JPEG);
            list.add(MediaType.IMAGE_PNG);
            list.add(MediaType.APPLICATION_OCTET_STREAM);
            return list;
        }
        ```

    2. 实施

        现在我们可以实施处理媒体请求的方法。如上所述，您需要用@ResponseBody注释标记控制器方法，并使用字节[]作为返回类型：

        ```java
        @RequestMapping(value = "/image-byte-array", method = RequestMethod.GET)
        public @ResponseBody byte[] getImageAsByteArray() throws IOException {
            InputStream in = servletContext.getResourceAsStream("/WEB-INF/images/image-example.jpg");
            return IOUtils.toByteArray(in);
        }
        ```

        要测试该方法，请在浏览器中发出以下请求：

        <http://localhost:8080/spring-mvc-xml/image-byte-array.jpg>

        在优势方面，该方法对HttpServletResponse一无所知，转换过程高度可配置，从使用可用的转换器到指定自定义转换器。响应的内容类型不必是硬编码的，而是将根据请求路径后缀.jpg进行协商。

        这种方法的缺点是，您需要明确实现从数据源（本地文件、外部存储等）检索图像的逻辑，并且您无法控制响应的标头或状态代码。

4. 使用ResponseEntity类

    您可以将图像返回为字节[]包裹在响应实体中。Spring MVC ResponseEntity不仅可以控制HTTP响应的主体，还可以控制标头和响应状态代码。按照这种方法，您需要将方法的返回类型定义为`ResponseEntity<byte[]>`，并在方法正文中创建returningResponseEntity对象。

    ```java
    @RequestMapping(value = "/image-response-entity", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImageAsResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        InputStream in = servletContext.getResourceAsStream("/WEB-INF/images/image-example.jpg");
        byte[] media = IOUtils.toByteArray(in);
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
        return responseEntity;
    }
    ```

    使用ResponseEntity，您可以为给定请求配置响应代码。

    在特殊事件（例如未找到图像（FileNotFoundException）或损坏（IOException））时，明确设置响应代码特别有用。在这些情况下，只需要在充分的捕获块中设置响应代码，例如`new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND)`。

    此外，如果您需要在响应中设置一些特定的标头，这种方法比通过HttpServletResponse对象设置标头更直接，该对象被方法接受为参数。它使方法签名清晰而专注。

5. 使用资源类返回图像

    最后，您可以以资源对象的形式返回图像。

    资源接口是用于抽象访问低级资源的接口。它在Spring中引入，作为标准java.net.URL类的更强大的替代品。它允许轻松访问不同类型的资源（本地文件、远程文件、类路径资源），而无需编写显式检索它们的代码。

    要使用这种方法，方法的返回类型应设置为资源，您需要用@ResponseBody注释注释方法。

    1. 实施

        ```java
        @ResponseBody
        @RequestMapping(value = "/image-resource", method = RequestMethod.GET)
        public Resource getImageAsResource() {
        return new ServletContextResource(servletContext, "/WEB-INF/images/image-example.jpg");
        }
        ```

        或者，如果我们想要对响应标头进行更多控制：

        ```java
        @RequestMapping(value = "/image-resource", method = RequestMethod.GET)
        @ResponseBody
        public ResponseEntity<Resource> getImageAsResource() {
            HttpHeaders headers = new HttpHeaders();
            Resource resource =
            new ServletContextResource(servletContext, "/WEB-INF/images/image-example.jpg");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        }
        ```

        使用此方法，您将图像视为可以使用ResourceLoader接口实现加载的资源。在这种情况下，您从图像的确切位置进行抽象，ResourceLoader决定从哪里加载它。

        它提供了一种使用配置控制图像位置的通用方法，并消除了编写文件加载代码的需要。

6. 结论

    在上述方法中，我们从基本方法开始，然后使用受益于框架消息转换功能的方法。我们还讨论了如何在不直接交出响应对象的情况下获取响应代码和响应标头的设置。

    最后，我们从图像位置的角度增加了灵活性，因为从哪里检索图像是在更容易即时更改的配置中定义的。

    [使用Spring下载图像或文件](https://www.baeldung.com/spring-controller-return-image-file)解释了如何使用Spring Boot实现相同的操作。
