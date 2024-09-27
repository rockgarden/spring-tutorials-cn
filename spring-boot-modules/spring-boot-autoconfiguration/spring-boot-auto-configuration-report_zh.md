# [在 Spring Boot 中显示自动配置报告](https://www.baeldung.com/spring-boot-auto-configuration-report)

1. 概述

    Spring Boot 中的自动配置机制试图根据依赖关系自动配置应用程序。

    在本快速教程中，我们将了解 Spring Boot 如何在启动时记录其自动配置报告。

2. 示例应用程序

    让我们编写一个简单的 Spring Boot 应用程序，在示例中使用：

    ```java
    @SpringBootApplication
    public class App {
        public static void main(String[] args) {
            SpringApplication.run(App.class, args);
        }
    }
    ```

3. 应用程序属性方法

    在启动此应用程序时，我们无法获得有关 Spring Boot 如何或为何决定组成应用程序配置的大量信息。

    不过，我们只需在 application.properties 文件中启用调试模式，即可让 Spring Boot 创建一份报告：

    `debug=true`

    或我们的 application.yml 文件：

    `debug: true`

4. 命令行方法

    如果我们不想使用属性文件方法，也可以通过使用 -debug 开关启动应用程序来触发自动配置报告：

    `$ java -jar myproject-0.0.1-SNAPSHOT.jar --debug`

5. 报告输出

    自动配置报告包含 Spring Boot 在类路径上发现并自动配置的类的相关信息。它还显示了 Spring Boot 已知但未在类路径上找到的类的信息。

    由于我们设置了 debug=true，因此我们可以在输出中看到它：

    ```log
    ============================
    CONDITIONS EVALUATION REPORT
    ============================

    Positive matches
    -----------------

    AopAutoConfiguration matched:
        - @ConditionalOnClass found required classes 'org.springframework.context.annotation.EnableAspectJAutoProxy',
            'org.aspectj.lang.annotation.Aspect', 'org.aspectj.lang.reflect.Advice', 'org.aspectj.weaver.AnnotatedElement';
            @ConditionalOnMissingClass did not find unwanted class (OnClassCondition)
        - @ConditionalOnProperty (spring.aop.auto=true) matched (OnPropertyCondition)

    AopAutoConfiguration.CglibAutoProxyConfiguration matched:
        - @ConditionalOnProperty (spring.aop.proxy-target-class=true) matched (OnPropertyCondition)

    AuditAutoConfiguration#auditListener matched:
        - @ConditionalOnMissingBean (types: org.springframework.boot.actuate.audit.listener.AbstractAuditListener;
            SearchStrategy: all) did not find any beans (OnBeanCondition)

    AuditAutoConfiguration.AuditEventRepositoryConfiguration matched:
        - @ConditionalOnMissingBean (types: org.springframework.boot.actuate.audit.AuditEventRepository;
            SearchStrategy: all) did not find any beans (OnBeanCondition)

    AuditEventsEndpointAutoConfiguration#auditEventsEndpoint matched:
        - @ConditionalOnBean (types: org.springframework.boot.actuate.audit.AuditEventRepository;
            SearchStrategy: all) found bean 'auditEventRepository';
            @ConditionalOnMissingBean (types: org.springframework.boot.actuate.audit.AuditEventsEndpoint;
            SearchStrategy: all) did not find any beans (OnBeanCondition)
        - @ConditionalOnEnabledEndpoint no property management.endpoint.auditevents.enabled found
            so using endpoint default (OnEnabledEndpointCondition)

    Negative matches
    -----------------

    ActiveMQAutoConfiguration:
        Did not match:
            - @ConditionalOnClass did not find required classes 'javax.jms.ConnectionFactory',
            'org.apache.activemq.ActiveMQConnectionFactory' (OnClassCondition)

    AopAutoConfiguration.JdkDynamicAutoProxyConfiguration:
        Did not match:
            - @ConditionalOnProperty (spring.aop.proxy-target-class=false) did not find property
            'proxy-target-class' (OnPropertyCondition)

    ArtemisAutoConfiguration:
        Did not match:
            - @ConditionalOnClass did not find required classes 'javax.jms.ConnectionFactory',
            'org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory' (OnClassCondition)

    AtlasMetricsExportAutoConfiguration:
        Did not match:
            - @ConditionalOnClass did not find required class 'io.micrometer.atlas.AtlasMeterRegistry'
            (OnClassCondition)

    AtomikosJtaConfiguration:
        Did not match:
            - @ConditionalOnClass did not find required class 'com.atomikos.icatch.jta.UserTransactionManager'
            (OnClassCondition)

    ```

6. 结论

    在本快速教程中，我们了解了如何显示和读取 Spring Boot 自动配置报告。
