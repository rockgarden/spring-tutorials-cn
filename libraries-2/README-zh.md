# Libraries

本模块包含有关各种 Java 库的文章。
这些库都是小型库，使用起来相对简单，不需要单独的模块。

与不同库相关的代码示例都在各自的模块中。

请记住，对于 [Jackson](/jackson) 和 [JUnit](/testing-modules) 等高级库，我们已经有了单独的模块。在这种情况下，请务必查看现有模块。

- [ ] [使用Java的jBPM指南](https://www.baeldung.com/jbpm-java)
- [类图库指南](https://www.baeldung.com/classgraph)
- [使用 Picocli 创建 Java 命令行程序](https://www.baeldung.com/java-picocli-create-command-line-program)
- [Java 并行收集器库指南](https://www.baeldung.com/java-parallel-collectors)
- [使用 Handlebars 进行模板化](https://www.baeldung.com/handlebars)
- [Crawler4j 指南](https://www.baeldung.com/crawler4j)
- [使用 Chronicle Map 的键值存储](https://www.baeldung.com/java-chronicle-map)
- [MapDB 指南](https://www.baeldung.com/mapdb)
- [Apache Mesos 指南](https://www.baeldung.com/apache-mesos)
- [使用Spring的JasperReports](https://www.baeldung.com/spring-jasper)
- 更多文章 [[<-- prev]](/libraries) [[next -->]](/libraries-3)

## 使用Java的jBPM指南

1. 简介

    在本教程中，我们将讨论业务流程管理（BPM）系统及其在Java中作为[jBPM](https://docs.jboss.org/jbpm/release/7.20.0.Final/jbpm-docs/html_single/)系统的实现。

2. 业务流程管理系统

    我们可以把[业务流程管理](https://en.wikipedia.org/wiki/Business_process_management)定义为一个领域，其范围超越了开发，延伸到公司的所有方面。

    BPM提供了对公司功能流程的可见性。这使我们能够通过使用迭代改进，找到一个由流程图描述的最佳流程。改进后的流程增加了利润，降低了成本。

    BPM定义了自己的目标、生命周期、实践以及所有参与者之间的共同语言，即业务流程。

3. jBPM系统

    jBPM是用Java实现的一个BPM系统。它允许我们创建一个业务流程流，执行它，并监测其生命周期。jBPM的核心是一个用Java编写的工作流引擎，它为我们提供了一个使用最新的业务流程建模符号（BPMN）2.0规范来创建和执行一个流程的工具。

    jBPM主要关注于可执行的业务流程。这些流程有足够的细节，这样它们就可以在工作流引擎上执行。

    下面是一个关于我们的BPMN流程模型的执行顺序的图形化流程图例子，以帮助我们理解：

    ![流程模型](pic/processModel.jpg)

    我们使用初始上下文开始执行流程，由绿色的开始节点表示
    - 首先，任务1将被执行
    - 在任务1完成后，我们将继续执行任务2
    - 在遇到红色结束节点时，执行停止。
4. jBPM项目的IDE插件

    让我们看看如何安装插件，以便在Eclipse和IntelliJ IDEA中创建一个jBPM项目和一个BPMN 2.0流程。

    1. Eclipse插件

        我们需要安装一个插件来创建jBPM项目。让我们按照下面的步骤进行：

        1. 在帮助部分，点击安装新软件
        2. 添加[Drools和jBPM的更新站点](https://docs.jbpm.org/7.64.0.Final/jbpm-docs/html_single/#jbpmreleasenotes)
        3. 接受许可协议的条款并完成插件的安装
        4. 重新启动Eclipse

        一旦Eclipse重新启动，我们就需要进入Windows -> Preferences -> Drools -> Drools Flow Nodes：选择完所有的选项后，我们可以点击 "Apply and Close"。现在，我们已经准备好创建我们的第一个jBPM项目了。

    2. IntelliJ IDEA插件

        IntelliJ IDEA默认安装了jBPM插件，但它只存在于终极版，而不是社区版。

        我们只需要通过点击配置->设置->插件->安装->JBoss jBPM来启用它：目前，这个IDE没有BPMN 2.0流程设计器，不过我们可以从任何其他设计器中导入*.bpmn文件并运行它们。

5. 你好世界 "实例"

    让我们动手创建一个简单的Hello World项目。

    1. 创建一个jBPM项目

        要在Eclipse中创建一个新的jBPM项目，我们将进入文件->新建->其他->jBPM项目（Maven）。在提供了我们项目的名称之后，我们就可以点击完成。Eclipse将为我们做所有艰苦的工作，并将下载所需的Maven依赖项，为我们创建一个jBPM样本项目。

        要在IntelliJ IDEA中创建同样的项目，我们可以进入文件->新建->项目->JBoss Drools。IDE会下载所有需要的依赖项，并把它们放在项目的lib文件夹中。

    2. 创建Hello World流程模型

        让我们创建一个小的BPM流程模型，在控制台打印 "Hello World"。

        为此，我们需要在src/main/resources下创建一个新的BPMN文件。

        文件的扩展名是.bpmn，它可以在BPMN设计器中打开：设计器的左侧面板列出了我们之前在设置Eclipse插件时选择的节点。我们将使用这些节点来创建我们的流程模型。中间的面板是工作区，我们将在这里创建流程模型。右边是属性标签，我们可以在这里设置流程或节点的属性。

        在这个HelloWorld模型中，我们将使用：

        - 启动事件--启动进程实例所需
        - 脚本任务--启用Java片断
        - 结束事件--结束进程实例时需要

        如前所述，IntelliJ IDEA没有BPMN设计器，但我们可以导入Eclipse或网页设计器中设计的.bpmn文件。

    3. 声明和创建知识库(kbase)

        所有的BPMN文件都作为进程加载到kbase中。我们需要把各自的进程ID传递给jBPM引擎，以便执行它们。

        我们将在资源/META-INF下创建kmodule.xml，其中包含我们的kbase和BPMN文件包声明：

        ```xml
        <kmodule xmlns="http://jboss.org/kie/6.0.0/kmodule">
            <kbase name="kbase" packages="com.baeldung.bpmn.process" />
        </kmodule>
        ```

        声明完成后，我们可以使用KieContainer来加载kbase：

        ```java
        KieServices kService = KieServices.Factory.get();
        KieContainer kContainer = kService.getKieClasspathContainer();
        KieBase kbase = kContainer.getKieBase(kbaseId);
        ```

    4. 创建jBPM运行时管理器

        我们将使用org.jbpm.test包中的JBPMHelper来建立一个样本运行环境。

        我们需要两样东西来创建环境：第一，创建EntityManagerFactory的数据源，第二，我们的KBASE。

        JBPMHelper具有启动内存中H2服务器和设置数据源的方法。利用这些方法，我们可以创建EntityManagerFactory：

        ```java
        JBPMHelper.startH2Server();
        JBPMHelper.setupDataSource();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
        ```

        一旦我们准备好了一切，我们就可以创建我们的RuntimeEnvironment：

        ```java
        RuntimeEnvironmentBuilder runtimeEnvironmentBuilder = 
        RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder();
        RuntimeEnvironment runtimeEnvironment = runtimeEnvironmentBuilder.
        entityManagerFactory(emf).knowledgeBase(kbase).get();
        ```

        使用RuntimeEnvironment，我们可以创建我们的jBPM运行时管理器：

        ```java
        RuntimeManager runtimeManager = RuntimeManagerFactory.Factory.get()
        .newSingletonRuntimeManager(runtimeEnvironment);
        ```

    5. 执行进程实例

        最后，我们将使用RuntimeManager来获得RuntimeEngine：

        `RuntimeEngine engine = manager.getRuntimeEngine(initialContext);`

        使用RuntimeEngine，我们将创建一个知识会话并启动该过程：

        ```java
        KieSession ksession = engine.getKieSession();
        ksession.startProcess(processId);
        ```

        进程将启动并在IDE控制台打印Hello World。

6. 结语

    在这篇文章中，我们介绍了BPM系统，使用其Java实现--[jBPM](https://www.jbpm.org/)。

    这是一个启动jBPM项目的快速指南。这里演示的例子在jbpm目录下。

    要执行流程，我们只需要运行WorkflowProcessMain类中的main方法。

## Relevant articles

- [x] [A Guide to jBPM with Java](https://www.baeldung.com/jbpm-java)
- [Guide to Classgraph Library](https://www.baeldung.com/classgraph)
- [Create a Java Command Line Program with Picocli](https://www.baeldung.com/java-picocli-create-command-line-program)
- [Guide to Java Parallel Collectors Library](https://www.baeldung.com/java-parallel-collectors)
- [Templating with Handlebars](https://www.baeldung.com/handlebars)
- [A Guide to Crawler4j](https://www.baeldung.com/crawler4j)
- [Key Value Store with Chronicle Map](https://www.baeldung.com/java-chronicle-map)
- [Guide to MapDB](https://www.baeldung.com/mapdb)
- [A Guide to Apache Mesos](https://www.baeldung.com/apache-mesos)
- [JasperReports with Spring](https://www.baeldung.com/spring-jasper)
- More articles [[<-- prev]](/libraries) [[next -->]](/libraries-3)
