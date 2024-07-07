# Spring Boot Flowable

## Flowable简介

1. 概述

    [Flowable](https://www.flowable.org/) 是一个用 Java 编写的业务流程引擎。在本教程中，我们将详细介绍业务流程，并了解如何利用 Flowable Java API 创建和部署示例业务流程。

2. 了解业务流程

    简单地说，业务流程是一组任务，这些任务按照规定的顺序完成后，就能实现规定的目标。业务流程中的每个任务都有明确定义的输入和输出。这些任务可能需要人工干预，也可能完全自动化。

    OMG（对象管理组织）定义了一种名为 "业务流程模型和符号"（[BPMN](http://www.bpmn.org/)）的标准，供企业定义和交流其流程。BPMN 已得到业界的广泛支持和认可。Flowable API 完全支持创建和部署 BPMN 2.0 流程定义。

3. 创建流程定义

    假设我们有一个出版前文章审核的简单流程。

    这个流程的要点是，作者提交文章，编辑要么接受要么拒绝。如果接受，文章就会立即发表；如果拒绝，就会通过电子邮件通知作者：

    ![流程实例](/pic/Screenshot-2019-04-15-at-05.49.51.png)

    我们使用 BPMN 2.0 XML 标准将流程定义创建为 XML 文件。

    让我们按照 BPMN 2.0 标准定义我们的简单流程：

    resources\processes\article-workflow.bpmn20.xml

    现在，这里有很多元素都是标准的 XML 元素，而其他元素则是 BPMN 2.0 特有的：

    - 整个流程被包裹在一个名为 "process" 的标记中，而流程又是一个名为 "definitions" 的标记的一部分
    - 流程由事件、流程、任务和网关组成
    - 事件可以是开始事件，也可以是结束事件
    - 流程（本例中为顺序流程）连接其他元素，如事件和任务
    - 任务是完成实际工作的地方；可以是 "user tasks" 或 "service tasks" 等
    - 用户任务要求人类用户与 Flowable API 交互并采取行动
    - 服务任务代表自动任务，可以是对 Java 类的调用，甚至是 HTTP 调用
    - 网关根据 "approved" 属性执行；这被称为流程变量，我们稍后会看到如何设置它们

    虽然我们可以在任何文本编辑器中创建流程定义文件，但这并不总是最方便的方法。不过幸运的是，Flowable 也提供了用户界面选项，可以使用 Eclipse 插件或 [Web应用程序](https://www.flowable.org/docs/userguide/index.html#flowableApps) 来完成这项工作。如果您使用 IntelliJ，也可以使用 IntelliJ 插件。

4. 使用 Flowable API

    现在，我们已经按照 BPMN 2.0 标准在 XML 文件中定义了简单流程，我们需要一种方法来提交和运行它。Flowable 提供了流程引擎 API，用于与 Flowable 引擎交互。Flowable非常灵活，提供了多种部署API的方法。

    鉴于 Flowable 是一种 Java API，我们只需在任何 Java 应用程序中加入必要的 JAR 文件，就能将流程引擎纳入其中。我们完全可以利用 Maven 来管理这些依赖关系。

    此外，Flowable 捆绑了通过 HTTP 与 Flowable 交互的 API。我们可以使用这些 API 来做任何通过 Flowable API 实现的事情。

    最后，Flowable 在与 Spring 和 Spring Boot 集成方面提供了出色的支持！我们将在教程中使用 Flowable 与 Spring Boot 的集成。

5. 使用 Process Engine 创建演示应用程序

    现在让我们创建一个简单的应用程序，它封装了 Flowable 的流程引擎，并提供基于 HTTP 的 API 来与 Flowable API 交互。在 API 的基础上还可以添加网络或移动应用程序，让用户获得更好的体验，但本教程将跳过这一环节。

    我们将以 Spring Boot 应用程序的形式创建演示。

    1. 依赖关系

        首先，让我们看看需要从 Maven 提取的依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flowable</groupId>
            <artifactId>flowable-spring-boot-starter</artifactId>
            <version>6.4.1</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        ```

        我们所需的依赖项均可从 Maven Central 获取：

        - Spring Boot Starter for Web - 这是 Spring Boot 的标准启动器
        - Spring Boot 的 Flowable Starter - Spring Boot Flowable 引擎需要它
        - H2 Database - Flowable 需要数据库来存储数据，而 H2 是默认的内存数据库

    2. 流程定义

        当我们启动 Spring Boot 应用程序时，它会尝试自动加载 "resources/processes" 文件夹下的所有进程定义。因此，让我们用上面创建的流程定义创建一个 XML 文件，文件名为 "article-workflow.bpmn20.xml"，并将其放在该文件夹中。

    3. 配置

        我们都知道，Spring Boot 对应用程序的配置采取了一种极具主见的方法，Flowable 作为 Spring Boot 的一部分也是如此。例如，当检测到 H2 是类路径上唯一的数据库驱动程序时，Flowable 会自动配置使用。

        显然，每个可配置的方面都可以通过[应用程序属性](https://www.flowable.org/docs/userguide/index.html#springBootFlowableProperties)以自定义方式进行配置。但在本教程中，我们将坚持使用默认值！

    4. Java 代理

        在流程定义中，我们使用了几个 Java 类，它们应该作为服务任务的一部分被调用。这些类实现了 JavaDelegate 接口，在 Flowable 中被称为 Java Delegates。现在我们将为这些 Java 委托定义虚拟类：

        service\PublishArticleService.java

        service\SendMailService.java

        显然，我们必须用实际的服务来取代这些虚拟类，以发布文章或发送电子邮件。

    5. HTTP API

        最后，让我们创建一些端点，以便与流程引擎交互并使用我们定义的流程。

        首先，我们将定义一个控制器，并公开三个端点：

        controller\ArticleWorkflowController.java

        我们的控制器提供了多个端点，用于提交文章以供审阅、获取要审阅的文章列表以及最后提交文章审阅。文章和批准是标准的 POJO，可以在资源库中找到。

        实际上，我们将大部分工作委托给了 ArticleWorkflowService：

        controller\ArticleWorkflowService.java

        现在，这里的大部分代码都非常直观，但让我们来了解一下其中的要点：

        - RuntimeService 用于实例化特定提交的流程
        - 任务服务用于查询和更新任务
        - 在 Spring 支持的事务中封装所有数据库调用
        - 在 Map 中存储作者和 URL 等详细信息，并与流程实例一起保存；这些被称为流程变量，我们可以在流程定义中访问它们，正如我们之前看到的那样

        现在，我们可以测试应用程序和流程引擎了。启动应用程序后，我们可以使用 curl 或任何 REST 客户端（如 Postman）与我们创建的端点进行交互。

6. 单元测试流程

    Flowable 支持不同版本的 JUnit，包括 JUnit 5，用于创建业务流程的单元测试。Flowable 与 Spring 的集成也为此提供了适当的支持。让我们看看 Spring 中流程的典型单元测试：

    processes\ArticleWorkflowUnitTest.java

    除了 @Deployment 等几个注解之外，这看起来就像 Spring 中的标准单元测试。现在，Flowable 提供了 @Deployment 注解，用于创建和删除测试方法周围的流程部署。

7. 了解流程部署

    虽然我们不会在本教程中介绍流程部署的细节，但还是值得介绍一些重要的方面。

    通常，流程会以业务归档（BAR）的形式存档并部署到应用程序中。在部署过程中，该归档会被扫描以查找工件（如流程定义）并进行处理。您可能已经注意到流程定义文件以".bpmn20.xml" 结尾的惯例。

    虽然我们在教程中使用了默认的内存 H2 数据库，但这实际上不能用于实际应用中，原因很简单，内存数据库不会在启动时保留任何数据，而且实际上不可能在集群环境中使用！因此，我们必须使用生产级关系数据库，并在应用程序中提供所需的配置。

    虽然BPMN 2.0本身并没有版本的概念，但Flowable为流程创建了一个版本属性，并将其部署在数据库中。如果部署了同一流程的更新版本（由属性 "id" 标识），则会创建一个新条目，并递增版本。当我们尝试按 "id" 启动流程时，流程引擎会获取已部署流程定义的最新版本。

    如果我们使用前面讨论过的设计器之一来创建流程定义，我们就已经有了流程的可视化。我们可以将流程图导出为图像，并将其与 XML 流程定义文件放在一起。如果我们遵循 Flowable 建议的标准[命名约定](https://www.flowable.org/docs/userguide/index.html#providingProcessDiagram)，流程引擎就会连同流程本身一起处理该图像。此外，我们还可以通过 API 获取该图像！

8. 浏览进程实例的历史记录

    在业务流程中，了解过去发生的事情往往至关重要。我们可能需要它来进行简单的调试，也可能需要它来进行复杂的法律审计。

    Flowable 记录了流程执行过程中发生的情况，并将其保存在数据库中。此外，Flowable 还通过应用程序接口（API）提供这些历史记录，以供查询和分析。Flowable 记录这些内容的实体有六个，HistoryService 拥有查询所有实体的方法。

    让我们来看一个简单的查询，以获取已完成的流程实例：

    ```java
    HistoryService historyService = processEngine.getHistoryService();
    List<HistoricActivityInstance> activities = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .finished()
        .orderByHistoricActivityInstanceEndTime()
        .asc()
        .list();
    ```

    正如我们所看到的，查询记录数据的 API 非常容易组合。在本示例中，我们按 ID 查询已完成的进程实例，并按其结束时间升序排列。

9. 监控流程

    监控是任何关键业务应用程序的一个关键方面，对于处理企业业务流程的应用程序来说更是如此。Flowable 提供了多个选项，让我们可以实时监控流程。

    Flowable 提供了我们可以通过 JMX 访问的特定 MBeans，不仅可以收集用于监控的数据，还可以执行许多其他活动。我们可以将其与任何标准的 JMX 客户端集成，包括与标准 Java 发行版一起提供的 jconsole。

    使用 JMX 进行监控提供了很多选择，但相对复杂且耗时。不过，由于我们使用的是 Spring Boot，所以我们很幸运！

    Spring Boot 提供了 [Actuator Endpoints](https://www.baeldung.com/spring-boot-actuators)，可通过 HTTP 收集应用程序指标。我们可以将其与 [Prometheus 和 Grafana](https://prometheus.io/docs/visualization/grafana/) 等工具栈无缝集成，以最小的工作量创建生产级监控工具。

    Flowable 提供了一个额外的 Actuator Endpoint，用于公开有关运行进程的信息。这虽然比不上通过 JMX 收集信息，但它快速、简单，而且最重要的是，足够用了。

10. 总结

    在本教程中，我们讨论了业务流程以及如何在 BPMN 2.0 标准中定义业务流程。然后，我们讨论了 Flowable 流程引擎和 API 在部署和执行流程方面的功能。我们了解了如何将其集成到 Java 应用程序中，特别是在 Spring Boot 中。

    接着，我们进一步讨论了流程的其他重要方面，如流程的部署、可视化和监控。不用说，我们只是触及了业务流程和像 Flowable 这样强大引擎的表面。Flowable 拥有非常丰富的 API 和充足的文档。不过，本教程应该会激起我们对这一主题的兴趣！

## Code

一如既往，示例代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-flowable) 上获取。

## 相关文章

- [x] [Introduction to Flowable](https://www.baeldung.com/flowable)
