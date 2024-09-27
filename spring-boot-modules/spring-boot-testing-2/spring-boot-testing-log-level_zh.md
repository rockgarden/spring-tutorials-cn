# [测试时在 Spring Boot 中设置日志级别](https://www.baeldung.com/spring-boot-testing-log-level)

1. 概述

    在本教程中，我们将学习如何在运行 Spring Boot 应用程序的测试时设置日志级别。

    虽然在测试通过时我们可以忽略日志，但如果我们需要诊断失败的测试，选择正确的日志级别可能至关重要。

2. 日志级别的重要性

    正确配置日志级别可为我们节省大量时间。

    例如，如果测试在 CI 服务器上失败，但在我们的开发机器上却通过了，那么除非我们有足够的日志输出，否则就无法诊断失败的测试。相反，如果我们记录了太多细节，可能会更难找到有用的信息。

    为了获得适量的细节，我们可以微调应用程序软件包的日志级别。如果我们发现某个 Java 包对我们的测试更为重要，我们可以给它一个较低的级别，如 DEBUG。同样，为了避免日志中出现过多噪音，我们可以为不太重要的包配置较高的级别，如 INFO 或 ERROR。

    让我们来探讨设置日志级别的各种方法。

3. 在 application.properties 中设置日志

    如果我们想修改测试中的日志级别，可以在 src/test/resources/application.properties 中设置一个属性：

    `logging.level.com.baeldung.testloglevel=DEBUG`

    该属性将专门为 com.baeldung.testloglevel 软件包设置日志级别。

    同样，我们也可以通过设置根日志级别来更改所有软件包的日志级别：

    `logging.level.root=INFO`

    现在，让我们添加一个写入日志的 REST 端点，试试我们的日志设置：

    ```java
    @RestController
    public class TestLogLevelController {

        private static final Logger LOG = LoggerFactory.getLogger(TestLogLevelController.class);

        @Autowired
        private OtherComponent otherComponent;

        @GetMapping("/testLogLevel")
        public String testLogLevel() {
            LOG.trace("This is a TRACE log");
            LOG.debug("This is a DEBUG log");
            LOG.info("This is an INFO log");
            LOG.error("This is an ERROR log");

            otherComponent.processData();

            return "Added some log output to console...";
        }

    }
    ```

    不出所料，如果我们在测试中调用这个端点，就能看到 TestLogLevelController 的 DEBUG 日志：

    ```log
    2019-04-01 14:08:27.545 DEBUG 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is a DEBUG log
    2019-04-01 14:08:27.545  INFO 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an INFO log
    2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an ERROR log
    2019-04-01 14:08:27.546  INFO 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an INFO log from another package
    2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an ERROR log from another package
    ```

    像这样设置日志级别是很容易的，如果我们的测试使用了 @SpringBootTest 注释，我们就一定要这样做。但是，如果不使用该注解，我们就必须以另一种方式配置日志级别。

    1. 基于配置文件的日志设置

        虽然将设置放入 src/test/application.properties 在大多数情况下都可行，但在某些情况下，我们可能希望对一个测试或一组测试进行不同的设置。

        在这种情况下，我们可以使用 ActiveProfiles 注解为测试添加 [Spring 配置文件](https://www.baeldung.com/spring-profiles)：

        ```java
        @RunWith(SpringRunner.class)
        @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = TestLogLevelApplication.class)
        @EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
        @ActiveProfiles("logging-test")
        public class TestLogLevelWithProfileIntegrationTest {...}
        ```

        我们的日志设置将放在 src/test/resources 中一个特殊的 application-logging-test.properties 文件中：

        ```properties
        logging.level.com.baeldung.testloglevel=TRACE
        logging.level.root=ERROR
        ```

        如果我们使用上述设置从测试中调用 TestLogLevelController，现在就能看到来自控制器的 TRACE 日志，而不会再有来自其他软件包的 INFO 日志了：

        ```log
        2019-04-01 14:08:27.545 DEBUG 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is a DEBUG log
        2019-04-01 14:08:27.545  INFO 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an INFO log
        2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an ERROR log
        2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an ERROR log from another package
        ```

4. 配置 Logback

    如果使用 Spring Boot 默认使用的 Logback，我们可以在 src/test/resources 中的 logback-test.xml 文件中设置日志级别：

    ![logback-test.xml](./src/test/resources/logback-test.xml)

    上面的示例展示了如何在 Logback 配置中为测试设置日志级别。根日志级别设置为 INFO，com.baeldung.testloglevel 软件包的日志级别设置为 DEBUG。

    让我们再次检查应用上述设置后的输出：

    ```log
    2019-04-01 14:08:27.545 DEBUG 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is a DEBUG log
    2019-04-01 14:08:27.545  INFO 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an INFO log
    2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an ERROR log
    2019-04-01 14:08:27.546  INFO 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an INFO log from another package
    2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an ERROR log from another package
    ```

    1. 基于配置文件的日志回溯配置

       为测试设置特定配置文件配置的另一种方法是在应用程序属性中为我们的配置文件设置 logging.config 属性：

       `logging.config=classpath:logback-testloglevel.xml`

       或者，如果我们想在 classpath 上设置单一的 Logback 配置，可以使用 logback.xml 中的 springProfile 元素：

       ![logback-multiprofile.xml](./src/test/resources/logback-multiprofile.xml)

       现在，如果我们在测试中使用配置文件 logback-test1 调用 TestLogLevelController，就会得到以下输出：

       ```log
       2019-04-01 14:08:27.545  INFO 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an INFO log
       2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an ERROR log
       2019-04-01 14:08:27.546  INFO 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an INFO log from another package
       2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an ERROR log from another package
       ```

       相反，如果我们将配置文件更改为 logback-test2，输出将是

       ```log
       2019-04-01 14:08:27.545 DEBUG 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is a DEBUG log
       2019-04-01 14:08:27.545  INFO 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an INFO log
       2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an ERROR log
       2019-04-01 14:08:27.546  INFO 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an INFO log from another package
       2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an ERROR log from another package
       ```

5. Log4J 替代方案

    另外，如果使用 [Log4J2](https://www.baeldung.com/log4j2-appenders-layouts-filters)，我们可以在 src/test/resources 中的 log4j2-spring.xml 文件中设置日志级别：

    ```xml
    <Configuration>
        <Appenders>
            <Console name="Console" target="SYSTEM_OUT">
                <PatternLayout
                        pattern="%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n" />
            </Console>
        </Appenders>

        <Loggers>
            <Logger name="com.baeldung.testloglevel" level="debug" />

            <Root level="info">
                <AppenderRef ref="Console" />
            </Root>
        </Loggers>
    </Configuration>
    ```

    我们可以通过设置 application.properties 中的 logging.config 属性来设置 Log4J 配置的路径：

    `logging.config=classpath:log4j-testloglevel.xml`

    最后，让我们检查一下应用上述设置后的输出结果：

    ```log
    2019-04-01 14:08:27.545 DEBUG 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is a DEBUG log
    2019-04-01 14:08:27.545  INFO 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an INFO log
    2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.testloglevel.TestLogLevelController  : This is an ERROR log
    2019-04-01 14:08:27.546  INFO 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an INFO log from another package
    2019-04-01 14:08:27.546 ERROR 56585 --- [nio-8080-exec-1] c.b.component.OtherComponent  : This is an ERROR log from another package
    ```

6. 结论

    在本文中，我们了解了在测试 Spring Boot 应用程序时如何设置日志级别。然后，我们探讨了配置日志级别的多种不同方法。

    在 Spring Boot 的 application.properties 中设置日志级别是最简单的方法，尤其是当我们使用 @SpringBootTest 注解时。
