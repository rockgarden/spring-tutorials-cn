# 使用 Apache Spark 的 Spring 云数据流

1. 简介

    Spring Cloud Data Flow 是用于构建数据集成和实时数据处理管道的工具包。

    本例中的管道是使用 [Spring Cloud Stream](https://cloud.spring.io/spring-cloud-stream/) 或 [Spring Cloud Task](https://spring.io/projects/spring-cloud-task) 框架构建的 Spring Boot 应用程序。

    在本教程中，我们将介绍如何将 Spring Cloud Data Flow 与 [Apache Spark](https://www.baeldung.com/apache-spark) 结合使用。

2. 数据流本地服务器

    首先，我们需要运行 Data Flow Server 才能部署作业。

    要在本地运行数据流服务器，我们需要创建一个新项目，并添加 spring-cloud-starter-dataflow-server-local 依赖：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-dataflow-server-local</artifactId>
        <version>1.7.4.RELEASE</version>
    </dependency>
    ```

    之后，我们需要用 @EnableDataFlowServer 对服务器中的主类进行注解：

    ```java
    @EnableDataFlowServer
    @SpringBootApplication
    public class SpringDataFlowServerApplication {
        public static void main(String[] args) {
            SpringApplication.run(
            SpringDataFlowServerApplication.class, args);
        }
    }
    ```

    运行此应用程序后，我们将在 9393 端口拥有一个本地数据流服务器。

3. 创建项目

    我们将创建一个 [Spark Job](https://www.baeldung.com/apache-spark) 作为独立的本地应用程序，这样就不需要任何集群来运行它了。

    1. 依赖关系

        首先，我们将添加 Spark 依赖项：

        ```xml
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.10</artifactId>
            <version>2.4.0</version>
        </dependency>
        ```

    2. 创建任务

        对于我们的工作，让我们来近似一下 Pi：

        ![PiApproximation.java](/spring-cloud-data-flow-apache-spark-job/src/main/java/com/baeldung/spring/cloud/PiApproximation.java)

4. 数据流shell

    数据流外壳（Data Flow Shell）是一个能让我们与服务器交互的应用程序。Shell 使用 DSL 命令来描述数据流。

    要使用[数据流外壳](https://www.baeldung.com/spring-cloud-data-flow-stream-processing)，我们需要创建一个允许我们运行它的项目。首先，我们需要 spring-cloud-dataflow-shell 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dataflow-shell</artifactId>
        <version>1.7.4.RELEASE</version>
    </dependency>
    ```

    添加依赖关系后，我们就可以创建运行数据流外壳的类了：

    ```java
    @EnableDataFlowShell
    @SpringBootApplication
    public class SpringDataFlowShellApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringDataFlowShellApplication.class, args);
        }
    }
    ```

5. 部署项目

    为了部署我们的项目，我们将使用所谓的任务运行程序，Apache Spark 有三种版本：cluster、yarn和client。我们将使用本地客户端版本。

    任务运行程序将运行我们的 Spark 作业。

    为此，我们首先需要使用 Data Flow Shell 注册任务：

    ```txt
    app register --type task --name spark-client --uri maven://org.springframework.cloud.task.app:spark-client-task:1.0.0.BUILD-SNAPSHOT
    ```

    该任务允许我们指定多个不同的参数，其中有些参数是可选的，但有些参数是正确部署 Spark 作业所必需的：

    - spark.app-class，我们提交作业的主类
    - spark.app-jar，包含作业的胖 jar 的路径
    - spark.app-name，我们的作业将使用的名称
    - spark.app-args，将传递给作业的参数。

    我们可以使用已注册的任务 spark-client 提交我们的作业，并记住提供所需的参数：

    ```bash
    task create spark1 --definition "spark-client \
    --spark.app-name=my-test-pi --spark.app-class=com.baeldung.spring.cloud.PiApproximation \
    --spark.app-jar=/apache-spark-job-0.0.1-SNAPSHOT.jar --spark.app-args=10"
    ```

    请注意，spark.app-jar 是包含我们任务的 fat-jar 的路径。

    成功创建任务后，我们可以使用以下命令运行它：

    `task launch spark1`

    这将调用任务的执行。

6. 总结

    在本教程中，我们展示了如何使用 Spring Cloud Data Flow 框架与 Apache Spark 一起处理数据。有关 Spring Cloud Data Flow 框架的更多信息，请参阅[文档](https://cloud.spring.io/spring-cloud-dataflow/)。
