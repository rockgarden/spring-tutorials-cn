# [Spring Boot 中的日志记录](https://www.baeldung.com/spring-boot-logging)

1. 一览表

    在这个简短的教程中，我们将探索Spring Boot中可用的主要日志记录选项。

    有关Logback的更深入信息可在Logback[指南](https://www.baeldung.com/logback)中找到，而Log4j2在Log4j2[简介](https://www.baeldung.com/log4j2-appenders-layouts-filters)-Appenders、Layouts和Filters中介绍。

2. 初始设置

    让我们先创建一个Spring Boot模块。推荐这样做的方法是使用[Spring Initializr](https://start.spring.io/)，我们在Spring Boot教程中涵盖了它。

    现在让我们创建我们唯一的类文件，LoggingController：

    ```java
    @RestController
    public class LoggingController {

        Logger logger = LoggerFactory.getLogger(LoggingController.class);

        @RequestMapping("/")
        public String index() {
            logger.trace("A TRACE Message");
            logger.debug("A DEBUG Message");
            logger.info("An INFO Message");
            logger.warn("A WARN Message");
            logger.error("An ERROR Message");

            return "Howdy! Check out the Logs to see the output...";
        }
    }
    ```

    一旦我们加载了Web应用程序，我们只需访问 <http://localhost:8080/> 即可触发这些日志记录行。

3. 零配置日志

    Spring Boot是一个非常有用的框架。它允许我们忘记大多数配置设置，其中许多设置是有主见的自动调整。

    在日志记录的情况下，唯一强制性的依赖项是Apache Commons日志记录。

    我们仅在使用Spring 4.x（Spring Boot 1.x）时需要导入它，因为它是由Spring 5（Spring Boot 2.x）中Spring Framework的spring-jcl模块提供的。

    如果我们使用Spring Boot Starter（我们几乎总是这样做），我们根本不应该担心导入spring-jcl。这是因为每个启动器，就像我们的spring-boot-starter-web一样，都依赖于spring-boot-starter-logging，它已经为我们拉入了spring-jcl。

    1. 默认回溯日志

        使用启动器时，默认情况下，Logback用于日志记录。

        Spring Boot用模式和ANSI颜色预配置了它，以使标准输出更具可读性。

        现在让我们运行应用程序并访问 <http://localhost:8080/> 页面，看看控制台中发生了什么：记录器的默认日志记录级别是预设为INFO，这意味着TRACE和DEBUG消息不可见。

        为了在不更改配置的情况下激活它们，我们可以在命令行上传递-debug或-trace参数：

        `java -jar target/spring-boot-logging-0.0.1-SNAPSHOT.jar --trace`

    2. 日志级别

        Spring Boot还允许我们通过环境变量访问更精细的日志级别设置。我们有几种方法可以做到这一点。

        首先，我们可以在虚拟机选项中设置日志记录级别：

        ```bash
        -Dlogging.level.org.springframework=TRACE
        -Dlogging.level.com.baeldung=TRACE
        ```

        或者，如果我们使用Maven，我们可以通过命令行定义日志设置：

        ```bash
        mvn spring-boot:run 
        -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE
        ```

        使用Gradle时，我们可以通过命令行传递日志设置。这将需要设置bootRun任务。

        完成后，我们运行应用程序：

        `./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE`

        如果我们想永久更改详细性，我们可以在application.properties文件中进行操作，如下所述：

        ```properties
        logging.level.root=WARN
        logging.level.com.baeldung=TRACE
        ```

        最后，我们可以使用我们的日志框架配置文件永久更改日志级别。

        我们提到，Spring Boot Starter默认使用Logback。让我们看看如何定义Logback配置文件的片段，我们在其中为两个单独的软件包设置了级别：

        ```xml
        <logger name="org.springframework" level="INFO" />
        <logger name="com.baeldung" level="INFO" />
        ```

        请记住，如果使用上述不同选项多次定义软件包的日志级别，但日志级别不同，则将使用最低级别。

        因此，如果我们同时使用Logback、Spring Boot和环境变量设置日志级别，日志级别将是TRACE，因为它是请求级别中最低的。

4. 回溯配置日志

    尽管默认配置很有用（例如，在POC或快速实验期间在零时间内启动），但它很可能不足以满足我们的日常需求。

    让我们看看如何包含具有不同颜色和日志记录模式的回溯配置，控制台和文件输出具有单独的规范，并具有体面的滚动策略，以避免生成庞大的日志文件。

    首先，我们应该找到一个解决方案，允许单独处理我们的日志记录设置，而不是污染应用程序。属性，后者通常用于许多其他应用程序设置。

    当类路径中的文件具有以下名称之一时，Spring Boot将自动在默认配置上加载它：

    - logback-spring.xml
    - logback.xml
    - logback-spring.groovy
    - logback.groovy

    如此处所述，Spring建议尽可能使用-spring变体而不是普通变体。

    让我们写一个简单的logback-spring.xml：

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

        <property name="LOGS" value="./logs" />

        <appender name="Console"
            class="ch.qos.logback.core.ConsoleAppender">
            <layout class="ch.qos.logback.classic.PatternLayout">
                <Pattern>
                    %black(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %yellow(%C{1}): %msg%n%throwable
                </Pattern>
            </layout>
        </appender>

        <appender name="RollingFile"
            class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>${LOGS}/spring-boot-logger.log</file>
            <encoder
                class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
                <Pattern>%d %p %C{1} [%t] %m%n</Pattern>
            </encoder>

            <rollingPolicy
                class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <!-- rollover daily and when the file reaches 10 MegaBytes -->
                <fileNamePattern>${LOGS}/archived/spring-boot-logger-%d{yyyy-MM-dd}.%i.log
                </fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>10MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
            </rollingPolicy>
        </appender>
        
        <!-- LOG everything at INFO level -->
        <root level="info">
            <appender-ref ref="RollingFile" />
            <appender-ref ref="Console" />
        </root>

        <!-- LOG "com.baeldung*" at TRACE level -->
        <logger name="com.baeldung" level="trace" additivity="false">
            <appender-ref ref="RollingFile" />
            <appender-ref ref="Console" />
        </logger>

    </configuration>
    ```

    当我们运行应用程序时，以下是输出：现在记录了TRACE和DEBUG消息，整体控制台模式在文本和色彩上都与以前不同。

    它现在还记录在当前路径下创建的/logs文件夹中的文件，并通过滚动策略将其存档。

5. Log4j2配置日志

    虽然Apache Commons Logging是核心，Logback是提供的参考实现，但其他日志库的所有路由都已包括在内，以便于切换到它们。

    然而，为了使用除日志记录以外的任何日志库，我们需要将其排除在我们的依赖项之外。

    对于每个像这样的初学者（这是我们示例中唯一的一个，但我们可以有很多）：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ```

    我们需要将其转化为精简版，并（仅一次）通过启动器本身添加我们的替代库：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>
    ```

    此时，我们需要在类路径中放置一个名为以下文件之一的文件：

    - log4j2-spring.xml
    - log4j2.xml

    我们将通过Log4j2（通过SLF4J）进行打印，无需进一步修改。

    让我们写一个简单的log4j2-spring.xml：

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <Configuration>
        <Appenders>
            <Console name="Console" target="SYSTEM_OUT">
                <PatternLayout
                    pattern="%style{%d{ISO8601}}{black} %highlight{%-5level }[%style{%t}{bright,blue}] %style{%C{1}}{bright,yellow}: %msg%n%throwable" />
            </Console>

            <RollingFile name="RollingFile"
                fileName="./logs/spring-boot-logger-log4j2.log"
                filePattern="./logs/$${date:yyyy-MM}/spring-boot-logger-log4j2-%d{-dd-MMMM-yyyy}-%i.log.gz">
                <PatternLayout>
                    <pattern>%d %p %C{1} [%t] %m%n</pattern>
                </PatternLayout>
                <Policies>
                    <!-- rollover on startup, daily and when the file reaches 
                        10 MegaBytes -->
                    <OnStartupTriggeringPolicy />
                    <SizeBasedTriggeringPolicy
                        size="10 MB" />
                    <TimeBasedTriggeringPolicy />
                </Policies>
            </RollingFile>
        </Appenders>

        <Loggers>
            <!-- LOG everything at INFO level -->
            <Root level="info">
                <AppenderRef ref="Console" />
                <AppenderRef ref="RollingFile" />
            </Root>

            <!-- LOG "com.baeldung*" at TRACE level -->
            <Logger name="com.baeldung" level="trace"></Logger>
        </Loggers>

    </Configuration>
    ```

    当我们运行应用程序时，以下是输出：证明我们现在完全使用Log4j2。

    除了XML配置外，Log4j2还允许我们使用此处描述的YAML或JSON配置。

6. 没有SLF4J的Log4j2

    我们也可以在不经过SLF4J的情况下原生使用Log4j2。

    为了做到这一点，我们只需使用本机类：

    ```java
    import org.apache.logging.log4j.Logger;
    import org.apache.logging.log4j.LogManager;
    // [...]
    Logger logger = LogManager.getLogger(LoggingController.class);
    ```

    我们不需要对标准Log4j2 Spring Boot配置进行任何其他修改。

    我们现在可以利用Log4j2的全新功能，而不会陷入旧的SLF4J接口。但我们也与此实现有关，在切换到另一个日志记录框架时，我们需要重写代码。

7. 使用龙目岛日志

    在我们迄今为止看到的示例中，我们必须从我们的日志记录框架中声明一个记录器实例。

    这个模板代码可能会很烦人。我们可以使用Lombok引入的各种注释来避免它。

    我们首先需要在构建脚本中添加Lombok依赖项才能与之配合使用：

    ```xml
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    ```

    1. @Slf4j和@CommonsLog

        SLF4J和Apache Commons日志API允许我们灵活地更改日志记录框架，而不会影响我们的代码。

        我们可以使用Lombok的@Slf4j和@CommonsLog注释将正确的记录器实例添加到我们的class:org.slf4j.Logger for SLF4J和org.apache.commons.logging.log for Apache Commons Logging。

        要查看这些注释的运行情况，让我们创建一个类似于LoggingController但没有记录器实例的类。我们将其命名为LombokLoggingController，并用@Slf4j注释：

        ```java
        @RestController
        @Slf4j
        public class LombokLoggingController {

            @RequestMapping("/lombok")
            public String index() {
                log.trace("A TRACE Message");
                log.debug("A DEBUG Message");
                log.info("An INFO Message");
                log.warn("A WARN Message");
                log.error("An ERROR Message");
        
                return "Howdy! Check out the Logs to see the output...";
            }
        }
        ```

        请注意，我们稍微调整了一下代码段，使用 log 作为我们的日志记录器实例。这是因为添加注解 @Slf4j 会自动添加一个名为 log 的字段。

        使用零配置日志记录时，应用程序将使用底层日志记录实现 Logback 来记录日志。同样，Log4j2-Configuration Logging 会使用 Log4j2 实现来记录日志。

        当我们用 @CommonsLog 替换注解 @Slf4j 时，也会得到相同的行为。

    2. @Log4j2

        我们可以使用注释@Log4j2直接使用Log4j2。因此，我们对LombokLoggingController进行了简单的更改，使用@Log4j2而不是@Slf4j或@CommonsLog：

        ```java
        @RestController
        @Log4j2
        public class LombokLoggingController {

            @RequestMapping("/lombok")
            public String index() {
                log.trace("A TRACE Message");
                log.debug("A DEBUG Message");
                log.info("An INFO Message");
                log.warn("A WARN Message");
                log.error("An ERROR Message");
        
                return "Howdy! Check out the Logs to see the output...";
            }
        }
        ```

        除日志记录外，Lombok 还提供了其他注解，有助于保持代码的干净整洁。有关这些注释的更多信息，请参阅项目 [Lombok](https://www.baeldung.com/intro-to-project-lombok) 简介，我们还提供了关于使用 Eclipse 和 IntelliJ 设置 [Lombok](https://www.baeldung.com/lombok-ide) 的教程。

8. 小心Java Util日志

    Spring Boot 还通过 logging.properties 配置文件支持 JDK 日志。

    不过在某些情况下，使用它并不是一个好主意。从文档中可以看出

    > Java Util Logging 存在已知的类加载问题，会在从 “executable jar” 运行时造成问题。我们建议您尽可能避免在从 “executable jar” 运行时使用它。

    使用 Spring 4 时，在 pom.xml 中手动排除 commons-logging 也是一种好的做法，这样可以避免日志库之间的潜在冲突。Spring 5 会自动处理该问题，因此我们在使用 Spring Boot 2 时不需要做任何事情。

9. Windows上的JANSI

    虽然基于Unix的操作系统（如Linux和Mac OS X）默认支持ANSI颜色代码，但在Windows控制台上，一切都将是单色的。

    Windows可以通过一个名为JANSI的库获取ANSI颜色。

    不过，我们应该注意可能的class加载缺点。

    我们必须在配置中导入并明确激活它，如下所示：

    日志：

    ```xml
    <configuration debug="true">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <withJansi>true</withJansi>
            <encoder>
                <pattern>[%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n</pattern>
            </encoder>
        </appender>
        <!-- more stuff -->
    </configuration>
    ```

    [Log4j2](https://logging.apache.org/log4j/2.x/manual/layouts.html#enable-jansi)：

    - 许多平台都支持 ANSI 转义序列，但 Windows 默认不支持。要启用 ANSI 支持，请将 Jansi jar 添加到我们的应用程序，并将属性 log4j.skipJansi 设置为 false。这将允许 Log4j 在写入控制台时使用 Jansi 添加 ANSI 转义码。

    > 注：在 Log4j 2.10 之前，Jansi 是默认启用的。Jansi 需要本地代码，这意味着 Jansi 只能由单个类加载器加载。对于网络应用程序，这意味着 Jansi jar 必须位于网络容器的类路径中。为避免给网络应用程序带来问题，从 Log4j 2.10 开始，Log4j 不再在没有明确配置的情况下自动尝试加载 Jansi。

    还有一点值得注意：

    布局文档页面的 highlight{pattern}{style} 部分包含有用的 Log4j2 JANSI 信息。
    JANSI 可以为输出着色，而 Spring Boot 的 Banner（原生或通过 banner.txt 文件自定义）将保持单色。

10. 结论

    我们已经看到了从Spring Boot项目中与主要日志记录框架接口的主要方法。

    我们还探讨了每种解决方案的主要优点和陷阱。
