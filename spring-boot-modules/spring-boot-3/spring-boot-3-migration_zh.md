# [将应用程序从Spring Boot 2迁移到Spring Boot 3](https://www.baeldung.com/spring-boot-3-migration)

1. 一览表

    在本教程中，我们将学习如何将Spring Boot应用程序迁移到3.0版本。要成功将应用程序迁移到Spring Boot 3，我们必须确保其当前的Spring Boot版本为2.7，Java版本为17。

    进一步阅读：

    [Spring Boot 3和Spring Framework 6.0 - 最新消息](https://www.baeldung.com/spring-boot-3-spring-6-new)

    了解Spring Boot 3和Spring 6附带的新功能。

    [Spring Boot的可观察性](https://www.baeldung.com/spring-boot-3-observability)

    Spring Boot 3的可观察性快速实用指南。

    [Spring Boot中的自定义WebFlux异常](https://www.baeldung.com/spring-boot-custom-webflux-exceptions)

    了解Spring Framework提供的ProblemDetail RFC7807异常格式，以及如何在Spring WebFlux中创建和处理自定义异常。

2. 核心变化

    Spring Boot 3.0标志着该框架的一个重要里程碑，为其核心组件带来了一些重要的修改。

    1. 配置属性

        一些属性密钥已被修改：

        - spring.redis已移至spring.data.redis
        - spring.data.cassandra已移至spring.cassandra
        - 删除了spring.jpa.hibernate.use-new-id-generator
        - [server.max.http.header.size](https://www.baeldung.com/spring-boot-max-http-header-size)已移至server.max-http-request-header-size
        - `spring.security.saml2.relyingparty.registration.{id}.identity-provider`支持被删除

        为了识别这些属性，我们可以在pom.xml中添加spring-boot-properties-migrator：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-properties-migrator</artifactId>
            <scope>runtime</scope>
        </dependency>
        ```

        最新版本的spring-boot-properties-migrator可从Maven Central获得。

        此依赖项生成报告，在启动时打印弃用属性名称，并在运行时临时迁移属性。

    2. Jakarta EE 10

        新版本的Jakarta EE 10更新了Spring Boot 3的相关依赖项：

        - Servlet规范更新到6.0版本
        - JPA规范更新到3.1版本

        因此，如果我们通过将它们排除在spring-boot-starter依赖项之外来管理这些依赖项，我们应该确保更新它们。

        让我们从更新JPA依赖项开始：

        ```xml
        <dependency>
            <groupId>jakarta.persistence</groupId>
            <artifactId>jakarta.persistence-api</artifactId>
            <version>3.1.0</version>
        </dependency>
        ```

        最新版本的jakarta.persistence-api可从Maven Central获得。

        接下来，让我们更新Servlet依赖项：

        ```xml
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
        </dependency>
        ```

        最新版本的jakarta.servlet-api可从Maven Central获得。

        除了更改依赖坐标外，雅加达EE现在使用“jakarta”软件包而不是“javax”。因此，在我们更新依赖项后，我们可能需要更新导入语句。

    3. Hibernate

        如果我们通过将Hibernate依赖项排除在spring-boot-starter依赖项之外来管理它，请务必更新它很重要：

        ```xml
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.1.4.Final</version>
        </dependency>
        ```

    4. 其他变化

        此外，此版本中还包含核心层面的其他重大变化：

        - 图像横幅支持删除：要定义[自定义横幅](https://www.baeldung.com/spring-boot-custom-banners)，只有banner.txt文件被视为有效文件。
        - 日志日期格式化程序：Logback和Log4J2的新默认日期格式是yyyy-MM-dd'T'HH:mm:ss.SSSXXX。
            如果我们想要恢复旧的默认格式，我们需要将属性logging.pattern.dateformaton application.yaml的值设置为旧值。
        - [@ConstructorBinding](https://docs.spring.io/spring-boot/docs/3.0.13-SNAPSHOT/api/org/springframework/boot/context/properties/ConstructorBinding.html)仅在构造函数级别：不再需要在[@ConfigurationProperties](https://www.baeldung.com/configuration-properties-in-spring-boot)类的类型级别设置@ConstructorBinding，并且应该将其删除。
            然而，如果一个类或记录有多个构造函数，我们必须在所需的构造函数上使用@ConstructorBinding来指定哪一个将用于属性绑定。

3. Web应用程序更改

    最初，假设我们的应用程序是一个Web应用程序，我们应该考虑某些更改。

    1. 尾斜杠匹配配置

        新的Spring Boot版本弃用配置尾斜杠匹配的选项，并将其默认值设置为false。

        例如，让我们用一个简单的GET端点定义一个控制器：

        ```java
        @RestController
        @RequestMapping("/api/v1/todos")
        @RequiredArgsConstructor
        public class TodosController {
            @GetMapping("/name")
            public List<String> findAllName(){
                return List.of("Hello","World");
            }
        }
        ```

        现在，默认情况下，“GET /api/v1/todos/name/”不再匹配，将导致HTTP 404错误。

        我们可以通过定义实现[WebMvcConfigurer](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html)或[WebFluxConfigurer](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/config/WebFluxConfigurer.html)的新配置类（如果是反应性服务）来启用所有端点的尾随斜杠匹配：

        ```java
        public class WebConfiguration implements WebMvcConfigurer {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                configurer.setUseTrailingSlashMatch(true);
            }
        }
        ```

    2. 响应标题大小

        正如我们已经提到的，属性[server.max.http.header.size](https://www.baeldung.com/spring-boot-max-http-header-size)被弃用，支持server.max-http-request-header-size，它只检查请求标头的大小。为了定义响应标头的限制，我们将定义一个新的bean：

        ```java
        @Configuration
        public class ServerConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
            @Override
            public void customize(TomcatServletWebServerFactory factory) {
                factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
                    @Override
                    public void customize(Connector connector) {
                        connector.setProperty("maxHttpResponseHeaderSize", "100000");
                    }
                });
            }
        }
        ```

        如果我们使用Jetty而不是Tomcat，我们应该将[TomcatServletWebServerFactory](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/web/embedded/tomcat/TomcatServletWebServerFactory.html)更改为[JettyServletWebServerFactory](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/web/embedded/jetty/JettyServletWebServerFactory.html)。需要注意的是，其他嵌入式网络容器不支持此功能。

    3. 其他变化

        在此版本中，Web应用程序级别还有其他重大变化：

        - 优美关机的阶段：优美关机的 SmartLifecycle 实现更新了阶段。现在，Spring 会在 SmartLifecycle.DEFAULT_PHASE - 2048 阶段启动优雅关机，并在 SmartLifecycle.DEFAULT_PHASE - 1024 阶段停止网络服务器。
        - [RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)上的HttpClient升级：Rest Template更新其Apache [HttpClient5](https://hc.apache.org/httpcomponents-client-5.2.x/)版本

4. 执行器变更

    [执行器](https://www.baeldung.com/spring-boot-actuators)模块中引入了一些重大变化。

    1. 执行器端点净化(Sanitization)

        在以前的版本中，Spring Framework会自动屏蔽端点/env和/configprops上敏感键的值，这些键会显示配置属性和环境变量等敏感信息。在这个版本中，Spring将方法更改为默认更安全。

        它现在默认屏蔽所有键的值，而不是只屏蔽某些键。我们可以通过使用以下值之一设置属性management.endpoint.env.show-values（用于/env端点）或management.endpoint.configprops.show-values（用于/configprops端点）来更改此配置：

        - NEVER：未显示任何值
        - ALWAYS：显示的所有值
        - WHEN_AUTHORIZED：如果用户获得授权，则会显示所有值。对于JMX，所有用户都是授权的。对于HTTP，只有特定角色才能访问数据。

    2. 其他变化

        Spring执行器模块上发生了其他相关更新：

        - Jmx端点曝光：JMX仅处理运行状况端点。通过配置属性management.endpoints.jmx.exposure.include和management.endpoints.jmx.exposure.exclude，我们可以对其进行自定义。
        - httptrace端点重命名：此版本将“/httptrace”端点重命名为“/httpexchanges”
        - 隔离对象映射器：此版本现在隔离了负责序列化执行器端点响应的对象映射器实例。我们可能会通过将management.endpoints.jackson.isolated-object-mapper属性设置为false来更改此功能。

5. Spring安全

    Spring Boot 3仅与Spring Security 6兼容。

    在升级到Spring Boot 3.0之前，我们应该首先将[Spring Boot 2.7应用程序升级到Spring Security 5.8](https://docs.spring.io/spring-security/reference/5.8/migration/index.html)。之后，我们可以将Spring Security升级到第6版和Spring Boot 3。

    此版本中引入了一些重要的更改：

    - [ReactiveUserDetailsService](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/ReactiveUserDetailsService.html)未自动配置：在存在[AuthenticationManagerResolver](https://www.baeldung.com/spring-security-authenticationmanagerresolver)的情况下，ReactiveUserDetailsService将不再自动配置。
    - [SAML2](https://www.baeldung.com/cs/saml-introduction)依赖方配置：我们之前提到，新版本的Spring boot不再支持位于spring.security.saml2.relyingparty.registration.{id}.identity-provider下的属性。相反，我们应该使用spring.security.saml2.relyingparty.registration.{id}.asserting-party下的新属性。

6. Spring批次

    现在让我们来看看[Spring Batch](https://www.baeldung.com/spring-boot-spring-batch)模块中引入的一些重大变化。

    1. 不鼓励使用 @EnableBatchProcessing

        以前，我们可以启用Spring Batch的自动配置，用[@EnableBatchProcessing](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/configuration/annotation/EnableBatchProcessing.html)注释配置类。如果我们想使用自动配置，新版本的Spring Boot不鼓励使用此注释。

        事实上，使用此注释（或定义实现[DefaultBatchConfiguration](https://docs.spring.io/spring-batch/docs/5.0.3/api/org/springframework/batch/core/configuration/support/DefaultBatchConfiguration.html)的bean）会告诉自动配置退后。

    2. 运行多个工作

        以前，可以使用Spring Batch同时运行多个批处理作业。然而，情况不再是这样了。如果自动配置检测到单个作业，它将在应用程序启动时自动执行。

        因此，如果上下文中存在多个作业，我们需要使用spring.batch.job.name属性提供作业名称来指定应在启动时执行哪个作业。因此，如果我们想运行多个作业，我们必须为每个作业创建一个单独的应用程序。

        或者，我们可以使用Quartz、Spring Scheduler等调度器或其他替代方案来安排作业。

7. 结论

    在本文中，我们学习了如何将2.7 Spring Boot应用程序迁移到第3版，重点是Spring环境的核心组件。其他模块也发生了一些[变化](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)，如Spring Session、Micrometer、Dependency管理等。
