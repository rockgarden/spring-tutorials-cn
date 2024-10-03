# [带Spring Boot的Apache Camel](https://www.baeldung.com/apache-camel-spring-boot)

1. 一览表

    Apache Camel的核心是一个集成引擎，简单来说，它可用于促进广泛多样的技术之间的交互。

    服务和技术之间的这些桥梁被称为路线。路由在引擎（CamelContext）上实现，它们通过所谓的“exchange messages”进行通信。

2. Maven附属机构

    首先，我们需要包含Spring Boot、Camel、Rest API的依赖项，以及Swagger和JSON：

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-servlet-starter</artifactId>
            <version>4.3.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-jackson-starter</artifactId>
            <version>4.3.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-swagger-java-starter</artifactId>
            <version>3.22.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.camel.springboot</groupId>
            <artifactId>camel-spring-boot-starter</artifactId>
            <version>4.3.0</version>
        </dependency>    
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
    ```

3. 主Class

    让我们先创建一个Spring Boot应用程序：

    ```java
    @SpringBootApplication
    @ComponentScan(basePackages="com.baeldung.camel")
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    ```

4. Spring Boot的骆驼配置

    现在让我们用Spring配置我们的应用程序，从配置文件（属性）开始。

    例如，让我们在src/main/resources中的application.properties文件中配置应用程序的日志：

    ```properties
    logging.config=classpath:logback.xml
    camel.springboot.name=MyCamel
    server.address=0.0.0.0
    management.address=0.0.0.0
    management.port=8081
    endpoints.enabled = true
    endpoints.health.enabled = true
    ```

    此示例显示了一个application.properties文件，该文件还设置了回溯配置的路径。通过将IP设置为“0.0.0.0”，我们完全限制了对Spring Boot提供的Web服务器的管理员和管理访问。此外，我们启用了对应用程序端点以及运行状况检查端点所需的网络访问。

    另一个配置文件是application.yml。在其中，我们将添加一些属性，以帮助我们将值注入应用程序路由：

    ```yml
    server:
    port: 8080
    camel:
    springboot:
        name: ServicesRest
    management:
    port: 8081
    endpoints:
    enabled: false
    health:
        enabled: true
    quickstart:
    generateOrderPeriod: 10s
    processOrderPeriod: 30s
    ```

5. 设置骆驼Servlet

    开始使用Camel的一种方法是将其注册为servlet，这样它就可以拦截HTTP请求并将其重定向到我们的应用程序。

    如前所述，使用Camel的2.18及以下版本，我们可以利用我们的application.yml——通过为我们的最终URL创建一个参数。稍后它将被注入我们的Java代码中：

    ```yml
    baeldung:
    api:
        path: '/camel'
    ```

    回到我们的应用程序类，我们需要在上下文路径的根目录处注册Camel servlet，当应用程序启动时，该路径将从application.yml中的引用baeldung.api.path注入：

    ```java
    @Value("${baeldung.api.path}")
    String contextPath;

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean
            (new CamelHttpTransportServlet(), contextPath+"/*");
        servlet.setName("CamelServlet");
        return servlet;
    }
    ```

    从Camel的2.19版本开始，此配置已被删除，因为CamelServlet默认设置为“/camel”。

6. 修建一条Route

    让我们通过从Camel扩展RouteBuilder类开始制作路由，并将其设置为@Component，以便组件扫描例程可以在Web服务器初始化期间找到它：

    ```java
    @Component
    class RestApi extends RouteBuilder {
        @Override
        public void configure() {
            CamelContext context = new DefaultCamelContext();
            restConfiguration()...
            rest("/api/")... 
            from("direct:remoteService")...
        }
    }
    ```

    在本类中，我们覆盖了Camel的RouteBuilder类中的configure（）方法。

    Camel总是需要一个CamelContext实例——保存传入和传出消息的核心组件。

    在这个简单的例子中，DefaultCamelContext就足够了，因为它只是将消息绑定并路由到其中，就像我们将要创建的REST服务一样。

    1. restConfiguration（）路由

        接下来，我们为计划在restConfiguration（）方法中创建的端点创建一个REST声明：

        ```java
        restConfiguration()
            .contextPath(contextPath)
            .port(serverPort)
            .enableCORS(true)
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "Test REST API")
            .apiProperty("api.version", "v1")
            .apiContextRouteId("doc-api")
            .component("servlet")
            .bindingMode(RestBindingMode.json)
        ```

        在这里，我们用从YAML文件中注入的属性注册上下文路径。同样的逻辑也适用于我们应用程序的端口。启用了CORS，允许跨站点使用此网络服务。绑定模式允许并转换参数到我们的API。

        接下来，我们将Swagger文档添加到我们之前设置的URI、标题和版本中。当我们为REST网络服务创建方法/端点时，Swagger文档将自动更新。

        这个Swagger上下文本身就是一个骆驼路线，在启动过程中，我们可以在服务器日志中看到一些关于它的技术信息。默认情况下，我们的示例文档在<http://localhost:8080/camel/api-doc>上提供。

    2. rest() 路线

        现在，让我们从上面列出的configure（）方法实现rest（）方法调用：

        ```java
        rest("/api/")
            .id("api-route")
            .consumes("application/json")
            .post("/bean")
            .bindingMode(RestBindingMode.json_xml)
            .type(MyBean.class)
            .to("direct:remoteService");
        ```

        对于那些熟悉API的人来说，这种方法非常简单。id是CamelContext内路由的标识。下一行定义了MIME类型。此处定义了绑定模式，以表明我们可以在restConfiguration（）上设置模式。

        post（）方法向API添加操作，生成“POST /bean”端点，而MyBean（具有整数id和字符串名称的常规Java bean）定义了预期参数。

        同样，HTTP操作，如GET、PUT和DELETE，都可以以get（）、put（）、delete（）的形式使用。

        最后，to（）方法创建了通往另一条路线的桥梁。在这里，它告诉Camel在其上下文/引擎内搜索我们将要创建的另一条路线——该路线由值/id“direct: ...”命名和检测，与from（）方法中定义的路线相匹配。

    3. from（）路由与transform（）

        使用Camel时，路由接收参数，然后转换、转换和处理这些参数。之后，它会将这些参数发送到另一个路由，该路由将结果转发到所需的输出（文件、数据库、SMTP服务器或REST API响应）。

        在本文中，我们只在我们正在覆盖的configure（）方法中创建另一个路由。这将是我们最后一个to（）路线的目的地路线：

        ```java
        from("direct:remoteService")
            .routeId("direct-route")
            .tracing()
            .log(">>> ${body.id}")
            .log(">>> ${body.name}")
            .transform().simple("Hello ${in.body.name}")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
        ```

        from（）方法遵循相同的原则，并且有许多与rest（）方法相同的方法，除了它从Camel上下文消息中消耗。这就是参数“直接路由”的原因，该参数创建了与上述方法rest().to()的链接。

        许多其他转换都可用，包括提取为Java原语（或对象）并将其发送到持久层。请注意，路由总是从传入消息中读取，因此链式路由将忽略传出消息。

        我们的例子已经准备好了，我们可以尝试一下：

        - 运行提示命令：mvn spring-boot:run
        - 向 <http://localhost:8080/camel/api/bean> 发送 POST 请求，带头参数： 内容类型：application/json，有效载荷 {“id”： 1, “name”： “World"}
        - 我们应该收到201的退货代码和回复：Hello, World

    4. 简单的脚本语言

        示例使用tracing（）方法输出日志记录。请注意，我们使用了${}占位符；这些是属于Camel的名为SIMPLE的脚本语言的一部分。它适用于在路由中交换的消息，如消息中的正文。

        在我们的示例中，我们使用SIMPLE将Camel消息正文中的bean属性输出到日志中。

        我们也可以用它来进行简单的转换，如transform（）方法所示。

    5. from（）路由与process（）

        让我们做一些更有意义的事情，例如调用服务层来返回已处理的数据。SIMPLE不是用于繁重数据处理的，所以让我们用process（）方法替换transform（）：

        ```java
        from("direct:remoteService")
            .routeId("direct-route")
            .tracing()
            .log(">>> ${body.id}")
            .log(">>> ${body.name}")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    MyBean bodyIn = (MyBean) exchange.getIn().getBody();
                    ExampleServices.example(bodyIn);
                    exchange.getIn().setBody(bodyIn);
                }
            })
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
        ```

        这允许我们将数据提取到bean中，与之前在type（）方法上定义的bean相同，并在我们的ExampleServices层中处理。

        由于我们之前将bindingMode（）设置为JSON，因此响应已经采用适当的JSON格式，基于我们的POJO生成。这意味着对于ExampleServices类：

        ```java
        public class ExampleServices {
            public static void example(MyBean bodyIn) {
                bodyIn.setName( "Hello, " + bodyIn.getName() );
                bodyIn.setId(bodyIn.getId() * 10);
            }
        }
        ```

        相同的HTTP请求现在返回响应代码201和正文：{“id”: 10,”name”: “Hello, World”}。

7. 结论

    通过几行代码，我们设法创建了一个相对完整的应用程序。所有依赖项都通过单个命令自动构建、管理和运行。此外，我们可以创建将各种技术联系在一起的API。

    这种方法也非常对容器友好，从而产生了一个非常精简的服务器环境，可以按需轻松复制。额外的配置可能性可以很容易地被纳入容器模板配置文件中。

    最后，除了filter（）、process（）、transform（）和marshall（）API外，Camel中还有许多其他集成模式和数据操作：

    - [骆驼整合模式](http://camel.apache.org/enterprise-integration-patterns.html)
    - [骆驼用户指南](http://camel.apache.org/user-guide.html)
    - [骆驼简单语言](http://camel.apache.org/simple.html)
