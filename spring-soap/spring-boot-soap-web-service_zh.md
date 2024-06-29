# [使用 Spring 创建 SOAP Web 服务](https://www.baeldung.com/spring-boot-soap-web-service)

1. 概述

    在本教程中，我们将学习如何使用 Spring Boot Starter Web Services 创建基于 SOAP 的 Web 服务。

2. SOAP 网络服务

    简而言之，网络服务是一种机器对机器、与平台无关的服务，允许通过网络进行通信。

    SOAP 是一种消息传递协议。信息（请求和响应）是通过 HTTP 传输的 XML 文档。XML 合同由 WSDL（网络服务描述语言）定义。它提供了一套规则来定义服务的信息、绑定、操作和位置。

    SOAP 中使用的 XML 可能会变得非常复杂。因此，最好将 SOAP 与 JAX-WS 或 Spring 等框架一起使用，我们将在本教程中看到这一点。

3. 契约优先的开发风格

    创建网络服务时有两种可能的方法： 最后签订合同（Contract-Last）和先签订合同（[Contract-First](https://docs.spring.io/spring-ws/sites/1.5/reference/html/why-contract-first.html)）。当我们使用"合同后置"方法时，我们从 Java 代码开始，然后根据类生成网络服务合同（WSDL）。使用合同先行方法时，我们从 WSDL 合同开始，然后生成 Java 类。

    Spring-WS 仅支持契约优先的开发方式。

4. 设置 Spring Boot 项目

    我们将创建一个 Spring Boot 项目，在其中定义我们的 SOAP WS 服务器。

    1. Maven 依赖项

        让我们先将 spring-boot-starter-parent 添加到项目中：

        ```xml
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
        </parent>
        ```

        接下来，让我们添加 spring-boot-starter-web-services 和 wsdl4j 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web-services</artifactId>
        </dependency>
        <dependency>
            <groupId>wsdl4j</groupId>
            <artifactId>wsdl4j</artifactId>
        </dependency>
        ```

    2. XSD 文件

        契约优先方法要求我们首先为服务创建域（方法和参数）。我们将使用 Spring-WS 自动导出为 WSDL 的 XML 架构文件 (XSD)：

        ![countries.xsd](/src/main/resources/countries.xsd)

        在该文件中，我们可以看到 getCountryRequest 网络服务请求的格式。我们将定义它接受一个字符串类型的参数。

        接下来，我们将定义响应的格式，其中包含一个 country 类型的对象。

        最后，我们可以看到国家对象中使用的货币对象。

    3. 生成域 Java 类

        现在，我们将根据上一节定义的 XSD 文件生成 Java 类。jaxb2-maven-plugin 会在构建时自动完成这项工作。该插件使用 XJC 工具作为代码生成引擎。XJC 会将 XSD 模式文件编译成完全注释的 Java 类。

        让我们在 pom.xml 中添加并配置该插件：

        ```xml
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>jaxb2-maven-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>xjc</id>
                    <goals>
                        <goal>xjc</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <sources>
                    <source>src/main/resources/countries.xsd</source>
                </sources>
                <outputDirectory>src/main/java</outputDirectory>
                <clearOutputDir>false</clearOutputDir>
            </configuration>
        </plugin>
        ```

        这里我们注意到两个重要的配置：

        - `<source>src/main/resources/countries.xsd</source>` - XSD 文件的位置
        - `<outputDirectory>src/main/java</outputDirectory>` - 生成 Java 代码的位置

        要生成 Java 类，我们可以使用 Java 安装中的 XJC 工具。在我们的 Maven 项目中就更简单了，因为类将在通常的 Maven 构建过程中自动生成：

        `mvn compile`

    4. 添加 SOAP 网络服务端点

        SOAP 网络服务端点类将处理所有传入的服务请求。它将启动处理并发送响应。

        在定义之前，我们先创建一个 Country 资源库，以便向网络服务提供数据：

        ![CountryRepository](/src/main/java/com/baeldung/springsoap/CountryRepository.java)

        接下来，我们将配置端点：

        ![CountryEndpoint](/src/main/java/com/baeldung/springsoap/CountryEndpoint.java)

        下面是一些需要注意的细节：

        - @Endpoint - 将该类作为 Web 服务端点在 Spring WS 中注册
        - @PayloadRoot - 根据命名空间和 localPart 属性定义处理程序方法
        - @ResponsePayload - 表示该方法返回一个要映射到响应有效载荷的值
        - @RequestPayload - 表示此方法接受从传入请求中映射的参数

    5. SOAP 网络服务配置 Bean

        现在让我们创建一个类，用于配置 Spring 消息派发器 servlet 以接收请求：

        ```java
        @EnableWs
        @Configuration
        public class WebServiceConfig extends WsConfigurerAdapter {
            // bean definitions
        }
        ```

        @EnableWs 在此 Spring Boot 应用程序中启用了 SOAP Web 服务功能。WebServiceConfig 类扩展了 WsConfigurerAdapter 基类，它配置了注解驱动的 Spring-WS 编程模型。

        让我们创建一个用于处理 SOAP 请求的 MessageDispatcherServlet：

        ```java
        @Bean
        public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
            MessageDispatcherServlet servlet = new MessageDispatcherServlet();
            servlet.setApplicationContext(applicationContext);
            servlet.setTransformWsdlLocations(true);
            return new ServletRegistrationBean<>(servlet, "/ws/*");
        }
        ```

        我们将设置 servlet 注入的 ApplicationContext 对象，以便 Spring-WS 可以找到其他 Spring Bean。

        我们还将启用 WSDL location servlet 转换。这将转换 WSDL 中 soap:address 的 location 属性，使其反映传入请求的 URL。

        最后，我们将创建一个 DefaultWsdl11Definition 对象。该对象将使用 XsdSchema 公开标准 WSDL 1.1。WSDL 名称将与 Bean 名称相同：

        ```java
        @Bean(name = "countries")
        public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
            DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
            wsdl11Definition.setPortTypeName("CountriesPort");
            wsdl11Definition.setLocationUri("/ws");
            wsdl11Definition.setTargetNamespace("http://www.baeldung.com/springsoap/gen");
            wsdl11Definition.setSchema(countriesSchema);
            return wsdl11Definition;
        }

        @Bean
        public XsdSchema countriesSchema() {
            return new SimpleXsdSchema(new ClassPathResource("countries.xsd"));
        }
        ```

5. 测试 SOAP 项目

    完成项目配置后，我们就可以对其进行测试了。

    1. 构建并运行项目

        我们可以创建一个 WAR 文件并将其部署到外部应用服务器上。但我们将使用 Spring Boot，它是一种更快、更简单的启动和运行应用程序的方法。

        首先，我们将添加以下类，使应用程序可执行：

        ![Application](/src/main/java/com/baeldung/springsoap/Application.java)

        请注意，我们没有使用任何 XML 文件（如 web.xml）来创建此应用程序。这一切都是纯 Java 的。

        现在，我们可以构建并运行应用程序了：

        `mvn spring-boot:run`

        要检查应用程序是否正常运行，我们可以通过 URL 打开 WSDL： <http://localhost:8080/ws/countries.wsdl>

    2. 测试 SOAP 请求

        要测试一个请求，我们将创建以下文件并命名为 request.xml：

        ```xml
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:gs="http://www.baeldung.com/springsoap/gen">
            <soapenv:Header/>
            <soapenv:Body>
                <gs:getCountryRequest>
                    <gs:name>Spain</gs:name>
                </gs:getCountryRequest>
            </soapenv:Body>
        </soapenv:Envelope>
        ```

        要向测试服务器发送请求，我们可以使用外部工具，如 SoapUI 或 Google Chrome 浏览器扩展 Wizdler。另一种方法是在 shell 中运行以下命令：

        `curl --header "content-type: text/xml" -d @request.xml http://localhost:8080/ws`

        如果没有缩进或换行，生成的响应可能不容易阅读。

        要查看其格式，我们可以将其复制并粘贴到集成开发环境或其他工具中。如果我们安装了xmllib2，就可以将curl命令的输出导入xmllint：

        `curl [command-line-options] | xmllint --format -`

        响应应包含有关西班牙的信息：

        ```xml
        <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
        <SOAP-ENV:Header/>
        <SOAP-ENV:Body>
            <ns2:getCountryResponse xmlns:ns2="http://www.baeldung.com/springsoap/gen">
                <ns2:country>
                    <ns2:name>Spain</ns2:name>
                    <ns2:population>46704314</ns2:population>
                    <ns2:capital>Madrid</ns2:capital>
                    <ns2:currency>EUR</ns2:currency>
                </ns2:country>
            </ns2:getCountryResponse>
        </SOAP-ENV:Body>
        </SOAP-ENV:Envelope>
        ```

6. 总结

    在本文中，我们学习了如何使用 Spring Boot 创建 SOAP 网络服务。我们还演示了如何从 XSD 文件生成 Java 代码。最后，我们配置了处理 SOAP 请求所需的 Spring Bean。
