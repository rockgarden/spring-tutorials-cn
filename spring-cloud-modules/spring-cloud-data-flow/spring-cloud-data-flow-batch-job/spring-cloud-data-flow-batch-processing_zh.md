# [使用 Spring 云数据流进行批量处理](https://www.baeldung.com/spring-cloud-data-flow-batch-processing)

1. 概述

    在本系列的第一篇文章中，我们介绍了 Spring Cloud Data Flow 的架构组件以及如何使用它来创建流数据管道。

    与处理无限制数据量的流式管道不同，批处理流程可轻松创建按需执行任务的短期服务。

2. 本地数据流服务器和外壳

    本地数据流服务器（Local Data Flow Server）是一个负责部署应用程序的组件，而数据流外壳（Data Flow Shell）则允许我们执行与服务器交互所需的 DSL 命令。

    在上一篇文章中，我们使用 Spring Initilizr 将它们都设置为 Spring Boot 应用程序。

    分别在服务器的主类中添加 @EnableDataFlowServer 注解和在 shell 的主类中添加 @EnableDataFlowShell 注解后，执行以下操作即可启动它们：

    `mvn spring-boot:run`

    服务器将在 9393 端口启动，shell 也将在提示符下准备好与之交互。

    关于如何获取和使用本地数据流服务器及其 shell 客户端，请参阅上一篇文章。

3. 批处理应用程序

    与服务器和 shell 一样，我们也可以使用 Spring Initilizr 来设置 Spring Boot 批处理应用程序。

    进入网站后，只需选择一个组、一个工件名称，并从依赖关系搜索框中选择云任务。

    完成后，点击生成项目按钮开始下载 Maven 构件。

    该工件已预先配置并带有基本代码。让我们看看如何编辑它以构建我们的批处理应用程序。

    1. Maven 依赖项

        首先，让我们添加几个 Maven 依赖项。由于这是一个批处理应用程序，我们需要从 Spring Batch 项目中导入库：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-batch</artifactId>
        </dependency>
        ```

        此外，由于 Spring 云任务使用关系数据库存储任务执行结果，我们需要添加 RDBMS 驱动程序的依赖关系：

        ```xml
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
        ```

        我们选择使用 Spring 提供的 H2 内存数据库。这为我们提供了一种简单的引导开发方法。但是，在生产环境中，您需要配置自己的 DataSource。

        请记住，工件的版本将从 Spring Boot 的父 pom.xml 文件继承。

    2. 主类

        启用所需功能的关键点是在 Spring Boot 的主类中添加 @EnableTask 和 @EnableBatchProcessing 注解。该类级注解告诉 Spring Cloud Task 引导一切：

        ```java
        @EnableTask
        @EnableBatchProcessing
        @SpringBootApplication
        public class BatchJobApplication {
            public static void main(String[] args) {
                SpringApplication.run(BatchJobApplication.class, args);
            }
        }
        ```

    3. 任务配置

        最后，让我们来配置一个作业--在本例中就是简单地将字符串打印到日志文件：

        ```java
        @Configuration
        public class JobConfiguration {

            private static Log logger
            = LogFactory.getLog(JobConfiguration.class);

            @Autowired
            public JobBuilderFactory jobBuilderFactory;

            @Autowired
            public StepBuilderFactory stepBuilderFactory;

            @Bean
            public Job job() {
                return jobBuilderFactory.get("job")
                .start(stepBuilderFactory.get("jobStep1")
                .tasklet(new Tasklet() {
                    
                    @Override
                    public RepeatStatus execute(StepContribution contribution, 
                        ChunkContext chunkContext) throws Exception {
                        
                        logger.info("Job was run");
                        return RepeatStatus.FINISHED;
                    }
                }).build()).build();
            }
        }
        ```

        关于如何配置和定义作业的详细信息不在本文讨论范围之内。如需了解更多信息，请参阅我们的 [Spring Batch 简介](https://www.baeldung.com/introduction-to-spring-batch)文章。

        最后，我们的应用程序已经准备就绪。让我们将其安装到本地 Maven 资源库中。为此，请将 cd 插入项目的根目录并发出以下命令：

        `mvn clean install`

        现在该把应用程序放到数据流服务器中了。

4. 注册应用程序

    要在应用程序注册中心注册应用程序，我们需要提供一个唯一的名称、一个应用程序类型和一个可以解析到应用程序构件的 URI。

    转到 Spring Cloud Data Flow Shell 并在提示符下发出命令：

    ```bash
    app register --name batch-job --type task 
    --uri maven://com.baeldung.spring.cloud:batch-job:jar:0.0.1-SNAPSHOT
    ```

5. 创建任务

    可使用以下命令创建任务定义：

    `task create myjob --definition batch-job`

    这将创建一个名称为 myjob 的新任务，指向之前注册的 batch-job 应用程序。

    使用以下命令可以获得当前任务定义的列表：

    `task list`

6. 启动任务

    要启动一项任务，我们可以使用以下命令

    `task launch myjob`

    任务启动后，其状态将存储在关系数据库中。我们可以使用以下命令查看任务的执行状态：

    `task execution list`

7. 查看结果

    在本例中，任务只是在日志文件中打印了一个字符串。日志文件位于数据流服务器日志输出中显示的目录内。

    要查看结果，我们可以尾随日志：

    ```bash
    tail -f PATH_TO_LOG\spring-cloud-dataflow-2385233467298102321\myjob-1472827120414\myjob
    [...] --- [main] o.s.batch.core.job.SimpleStepHandler: Executing step: [jobStep1]
    [...] --- [main] o.b.spring.cloud.JobConfiguration: Job was run
    [...] --- [main] o.s.b.c.l.support.SimpleJobLauncher:
    Job: [SimpleJob: [name=job]] completed with the following parameters: 
        [{}] and the following status: [COMPLETED]
    ```

8. 结论

    在本文中，我们展示了如何通过使用 Spring Cloud Data Flow 来处理批处理。
