# [Spring Retry 指南](https://www.baeldung.com/spring-retry)

Spring

Spring Retry

1. 概述

    **Spring Retry** 提供了自动重新调用失败操作的能力。这在处理**短暂性错误**（如瞬时网络故障）时非常有用。

    在本教程中，我们将学习使用 Spring Retry 的多种方式：**注解**、**RetryTemplate** 和 **回调监听器**。

    推荐阅读：

    - [使用指数退避和抖动实现更好的重试](https://www.baeldung.com/spring-retry)  
    了解如何使用 Resilience4j 的退避和抖动机制更好地控制应用重试。
    - [Resilience4j 指南](https://www.baeldung.com/resilience4j)  
    学习如何使用 Resilience4j 库中最实用的模块来构建具备弹性的系统。
    - [在 Spring Batch 中配置重试逻辑](https://www.baeldung.com/spring-batch-retry-listener)  
    Spring Batch 允许我们为任务设置重试策略，使其在出错时自动重试。本文介绍如何配置。

2. Maven 依赖

    首先，在 `pom.xml` 文件中添加 `spring-retry` 依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.retry</groupId>
        <artifactId>spring-retry</artifactId>
        <version>2.0.3</version>
    </dependency>
    ```

    还需要添加 Spring AOP 支持：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aspects</artifactId>
        <version>6.1.5</version>
    </dependency>
    ```

    你可以在 [Maven Central](https://search.maven.org/) 上找到 `spring-retry` 和 `spring-aspects` 的最新版本。

3. 启用 Spring Retry

    要在应用中启用 Spring Retry，需要在 `@Configuration` 类上添加 `@EnableRetry` 注解：

    ```java
    @Configuration
    @EnableRetry
    public class AppConfig { 
        // ...
    }
    ```

4. 使用 Spring Retry

    1. 使用 `@Retryable`（无恢复）

        我们可以使用 `@Retryable` 注解为方法添加重试功能：

        ```java
        @Service
        public interface MyService { 
            @Retryable 
            void retryService(String sql); 
        }
        ```

        由于未指定异常类型，所有异常都会触发重试。当达到最大重试次数后仍失败，则会抛出 `ExhaustedRetryException`。

        根据 `@Retryable` 的默认行为，最多重试 **3 次**，每次间隔 **1 秒**。

    2. `@Retryable` 与 `@Recover`

        我们可以使用 `@Recover` 注解定义一个恢复方法：

        ```java
        @Service
        public interface MyService { 
            @Retryable(retryFor = SQLException.class)
            void retryServiceWithRecovery(String sql) throws SQLException; 

            @Recover
            void recover(SQLException e, String sql); 
        }
        ```

        当抛出 `SQLException` 时会触发重试。如果经过多次重试后仍然失败，则调用 `recover()` 方法进行兜底处理。

        **恢复方法（recovery method）的规则：**
        - 第一个参数可选，为 `Throwable` 类型
        - 返回类型必须与 `@Retryable` 方法一致
        - 后续参数按原方法参数顺序填充

    3. 自定义 `@Retryable` 行为

        可以通过 `maxAttempts` 和 `backoff` 参数自定义重试行为：

        ```java
        @Service
        public interface MyService {
            @Retryable(
                retryFor = SQLException.class, 
                maxAttempts = 2, 
                backoff = @Backoff(delay = 100)
            )
            void retryServiceWithCustomization(String sql) throws SQLException;
        }
        ```

        最多重试 2 次，每次延迟 100 毫秒。

    4. 使用 Spring 属性文件

        我们也可以将重试配置外化到属性文件中。

        首先，在 `retryConfig.properties` 文件中定义配置：

        ```properties
        retry.maxAttempts=2
        retry.maxDelay=100
        ```

        然后在 `@Configuration` 类中加载该文件：

        ```java
        @Configuration
        @EnableRetry
        @PropertySource("classpath:retryConfig.properties")
        public class AppConfig { 
            // ...
        }
        ```

        最后在注解中引用这些属性：

        ```java
        @Service 
        public interface MyService {
            @Retryable(
                retryFor = SQLException.class, 
                maxAttemptsExpression = "${retry.maxAttempts}",
                backoff = @Backoff(delayExpression = "${retry.maxDelay}") 
            )
            void retryServiceWithExternalConfiguration(String sql) throws SQLException; 
        }
        ```

        > ⚠️ 注意：此时应使用 `maxAttemptsExpression` 和 `delayExpression`，而不是 `maxAttempts` 和 `delay`。

    5. 打印重试次数

        要记录当前重试次数，可以使用 `RetrySynchronizationManager.getContext().getRetryCount()`：

        ```java
        @Override
        public void retryService() {
            logger.info("重试次数: " + RetrySynchronizationManager.getContext().getRetryCount());
            logger.info("在 retryService() 方法中抛出 RuntimeException");
            throw new RuntimeException();
        }
        ```

        执行后日志输出如下：

        ```log
        Retry Number: 0 
        throw RuntimeException in method retryService()
        Retry Number: 1 
        throw RuntimeException in method retryService()
        Retry Number: 2 
        throw RuntimeException in method retryService()
        ```

        可以看到每次重试都会打印当前次数。

5. RetryTemplate

    1. RetryOperations 接口

        Spring Retry 提供了 `RetryOperations` 接口，包含一组 `execute()` 方法：

        ```java
        public interface RetryOperations {
            <T> T execute(RetryCallback<T> retryCallback) throws Exception;
            // ...
        }
        ```

        `RetryCallback` 是 `execute()` 的参数，用于封装需要重试的业务逻辑：

        ```java
        public interface RetryCallback<T> {
            T doWithRetry(RetryContext context) throws Throwable;
        }
        ```

    2. 配置 RetryTemplate

        `RetryTemplate` 是 `RetryOperations` 的实现。

        在 `@Configuration` 类中配置一个 `RetryTemplate` Bean：

        ```java
        @Configuration
        public class AppConfig {
            
            // ...

            @Bean
            public RetryTemplate retryTemplate() {
                RetryTemplate retryTemplate = new RetryTemplate();
                
                FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
                fixedBackOffPolicy.setBackOffPeriod(2000L); // 每次重试间隔 2 秒
                retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

                SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
                retryPolicy.setMaxAttempts(2); // 最多重试 2 次
                retryTemplate.setRetryPolicy(retryPolicy);
                
                return retryTemplate;
            }
        }
        ```

        - **RetryPolicy**：决定何时进行重试（如最大次数、特定异常）
        - **BackOffPolicy**：控制重试之间的延迟
        - **FixedBackOffPolicy**：固定延迟策略

    3. 使用 RetryTemplate

        调用 `retryTemplate.execute()` 来执行带重试的代码：

        ```java
        retryTemplate.execute(new RetryCallback<Void, RuntimeException>() {
            @Override
            public Void doWithRetry(RetryContext arg0) {
                myService.templateRetryService();
                return null;
            }
        });
        ```

        也可以使用 Lambda 表达式简化：

        ```java
        retryTemplate.execute(arg0 -> {
            myService.templateRetryService();
            return null;
        });
        ```

6. 监听器（Listeners）

    监听器允许我们在重试过程中插入额外的回调逻辑，适用于跨多个重试的通用操作（如日志、监控）。

    1. 添加回调

        通过实现 `RetryListener` 接口添加回调：

        ```java
        public class DefaultListenerSupport extends RetryListenerSupport {
            
            @Override
            public <T, E extends Throwable> void close(RetryContext context,
            RetryCallback<T, E> callback, Throwable throwable) {
                logger.info("onClose");
                super.close(context, callback, throwable);
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext context,
            RetryCallback<T, E> callback, Throwable throwable) {
                logger.info("onError"); 
                super.onError(context, callback, throwable);
            }

            @Override
            public <T, E extends Throwable> boolean open(RetryContext context,
            RetryCallback<T, E> callback) {
                logger.info("onOpen");
                return super.open(context, callback);
            }
        }
        ```

        - `open()`：重试开始前调用
        - `onError()`：每次重试失败时调用
        - `close()`：重试结束后调用

    2. 注册监听器

        将监听器注册到 `RetryTemplate` Bean 中：

        ```java
        @Bean
        public RetryTemplate retryTemplate() {
            RetryTemplate retryTemplate = new RetryTemplate();
            // ... 配置策略
            retryTemplate.registerListener(new DefaultListenerSupport());
            return retryTemplate;
        }
        ```

        ---

7. 测试结果

    以下是一个完整的集成测试示例：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(
    classes = AppConfig.class,
    loader = AnnotationConfigContextLoader.class)
    public class SpringRetryIntegrationTest {

        @Autowired
        private MyService myService;

        @Autowired
        private RetryTemplate retryTemplate;

        @Test(expected = RuntimeException.class)
        public void givenTemplateRetryService_whenCallWithException_thenRetry() {
            retryTemplate.execute(arg0 -> {
                myService.templateRetryService();
                return null;
            });
        }
    }
    ```

    测试日志输出：

    ```log
    2020-01-09 20:04:10 [main] INFO  o.b.s.DefaultListenerSupport - onOpen 
    2020-01-09 20:04:10 [main] INFO  o.baeldung.springretry.MyServiceImpl - throw RuntimeException in method templateRetryService() 
    2020-01-09 20:04:10 [main] INFO  o.b.s.DefaultListenerSupport - onError 
    2020-01-09 20:04:12 [main] INFO  o.baeldung.springretry.MyServiceImpl - throw RuntimeException in method templateRetryService() 
    2020-01-09 20:04:12 [main] INFO  o.b.s.DefaultListenerSupport - onError 
    2020-01-09 20:04:12 [main] INFO  o.b.s.DefaultListenerSupport - onClose
    ```

    日志表明 `RetryTemplate` 和 `RetryListener` 已正确配置并工作。

8. 结论

    在本文中，我们学习了如何使用 Spring Retry 的三种主要方式：
    - 使用 `@Retryable` 和 `@Recover` 注解实现声明式重试
    - 使用 `RetryTemplate` 实现编程式重试
    - 使用 `RetryListener` 添加重试过程中的监听回调

    Spring Retry 是构建**高可用、容错性强的微服务系统**的重要工具，特别适用于处理网络波动、服务短暂不可用等瞬时故障。
