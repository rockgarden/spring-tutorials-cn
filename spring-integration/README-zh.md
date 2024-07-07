# Spring Integration

This module contains articles about Spring Integration

## Spring集成简介

1. 简介

    本文将主要通过一些小的、实际的例子来介绍Spring Integration的核心概念。

    Spring Integration提供了很多强大的组件，可以大大增强企业架构中系统和流程的互连性。

    它体现了一些最好的和最流行的设计模式，帮助开发者避免自己动手。

    我们将看看这个库在企业应用中填补的具体需求，以及为什么它比一些替代方案更值得推荐。我们还将研究一些可用的工具，以进一步简化基于Spring集成的应用的开发。

2. 设置

    ```xml
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-core</artifactId>
        <version>5.1.13.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-file</artifactId>
        <version>5.1.13.RELEASE</version>
    </dependency>
    ```

    你可以从Maven中心下载最新版本的[Spring集成核心](https://search.maven.org/classic/#search%7Cga%7C1%7Ca%3A%22spring-integration-core%22)和[Spring集成文件支持](https://search.maven.org/classic/#search%7Cga%7C1%7Cspring-integration-file)。

3. 消息传递模式

    本库中的一个基本模式是消息传递。该模式以消息为中心--离散(discrete)的数据有效载荷，通过预定义的渠道从一个源系统或流程移动到一个或多个系统或流程。

    从历史上看，该模式是以一种最灵活的方式来整合多个不同的系统，即：

    - 几乎完全解耦参与集成的系统
    - 允许参与集成的系统对彼此的底层协议、格式化或其他实施细节完全不了解
    - 鼓励参与集成的组件的开发和重复使用

4. 消息集成的作用

    让我们考虑一个基本的例子，将一个MPEG视频文件从一个指定的文件夹复制到另一个配置的文件夹：

    ```java
    @Configuration
    @EnableIntegration
    public class BasicIntegrationConfig{
        public String INPUT_DIR = "the_source_dir";
        public String OUTPUT_DIR = "the_dest_dir";
        public String FILE_PATTERN = "*.mpeg";

        @Bean
        public MessageChannel fileChannel() {
            return new DirectChannel();
        }

        @Bean
        @InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "1000"))
        public MessageSource<File> fileReadingMessageSource() {
            FileReadingMessageSource sourceReader= new FileReadingMessageSource();
            sourceReader.setDirectory(new File(INPUT_DIR));
            sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
            return sourceReader;
        }

        @Bean
        @ServiceActivator(inputChannel= "fileChannel")
        public MessageHandler fileWritingMessageHandler() {
            FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
            handler.setFileExistsMode(FileExistsMode.REPLACE);
            handler.setExpectReply(false);
            return handler;
        }
    }
    ```

    上面的代码配置了一个服务激活器(service activator)、一个集成通道(integration channel)和一个入站通道适配器(inbound channel adapter)。

    @EnableIntegration注解将该类指定为Spring集成配置。

    让我们开始我们的Spring集成应用上下文：

    ```java
    public static void main(String... args) {
        AbstractApplicationContext context 
        = new AnnotationConfigApplicationContext(BasicIntegrationConfig.class);
        context.registerShutdownHook();
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter q and press <enter> to exit the program: ");
        
        while (true) {
        String input = scanner.nextLine();
        if("q".equals(input.trim())) {
            break;
        }
        }
        System.exit(0);
    }
    ```

    上面的main方法启动了集成上下文；它还接受来自命令行的 "q" 字符输入以退出程序。让我们更详细地检查这些组件。

    - [ ] 测试运行？

5. Spring集成组件

    1. 消息

        org.springframework.integration.Message接口定义了Spring Message：Spring Integration上下文中的数据传输单位。

        ```java
        public interface Message<T> {
            T getPayload();
            MessageHeaders getHeaders();
        }
        ```

        它定义了两个关键元素的访问器：

        - Message headers 消息头，本质上是一个可用于传输元数据的键值容器，如org.springframework.integration.MessageHeaders类中所定义。
        - The message payload 消息的有效载荷，也就是要传输的实际有价值的数据--在我们的案例中，视频文件就是有效载荷。

    2. 通道

        在Spring Integration（乃至EAI）中，通道是集成架构中的基本管道。它是消息从一个系统传递到另一个系统的管道。

        你可以把它看作是一个字面意义上的管道，一个集成系统或流程可以通过它向其他系统推送消息（或接收消息）。

        在Spring Integration中，通道有多种形式，这取决于你的需求。它们在很大程度上是可配置的，可以开箱即用，不需要任何自定义代码，但如果你有自定义需求，也有一个强大的框架可用。

        点对点 Point-to-Point（P2P）通道被用来在系统或组件之间建立1对1的通信线路。一个组件将消息发布到通道上，这样另一个组件就可以接收到它。通道的两端只能有一个组件。

        正如我们所看到的，配置一个通道就像返回一个DirectChannel的实例一样简单：

        ```java
        @Bean
        public MessageChannel fileChannel1() {
            return new DirectChannel();
        }

        @Bean
        public MessageChannel fileChannel2() {
            return new DirectChannel();
        }

        @Bean
        public MessageChannel fileChannel3() {
            return new DirectChannel();
        }
        ```

        在这里，我们定义了三个独立的通道，都是由它们各自的getter方法的名称来标识的。

        发布-订阅 Publish-Subscribe（Pub-Sub）通道是用来在系统或组件之间建立一对多的通信线路。这将使我们能够发布到我们先前创建的所有3个直接通道。

        所以按照我们的例子，我们可以用一个pub-sub通道来代替P2P通道：

        ```java
        @Bean
        public MessageChannel pubSubFileChannel() {
            return new PublishSubscribeChannel();
        }

        @Bean
        @InboundChannelAdapter(value = "pubSubFileChannel", poller = @Poller(fixedDelay = "1000"))
        public MessageSource<File> fileReadingMessageSource() {
            FileReadingMessageSource sourceReader = new FileReadingMessageSource();
            sourceReader.setDirectory(new File(INPUT_DIR));
            sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
            return sourceReader;
        }
        ```

        我们现在已经将入站通道适配器转换为发布到一个Pub-Sub通道。这将使我们能够把从源文件夹中读取的文件发送到多个目的地。

    3. 桥接

        在Spring Integration中，如果两个消息通道或适配器由于某种原因不能直接连接，那么桥接器(Bridge)就被用来连接它们。

        在我们的例子中，我们可以使用桥接器将我们的Pub-Sub通道连接到三个不同的P2P通道（因为P2P和Pub-Sub通道不能直接连接）：

        ```java
        @Bean
        @BridgeFrom(value = "pubSubFileChannel")
        public MessageChannel fileChannel1() {
            return new DirectChannel();
        }

        @Bean
        @BridgeFrom(value = "pubSubFileChannel")
        public MessageChannel fileChannel2() {
            return new DirectChannel();
        }

        @Bean
        @BridgeFrom(value = "pubSubFileChannel")
        public MessageChannel fileChannel3() {
            return new DirectChannel();
        }
        ```

        上面的bean配置现在将pubSubFileChannel与三个P2P通道连接起来。@BridgeFrom注解是定义桥梁的内容，可以应用于任何数量的需要订阅Pub-Sub通道的通道。

        我们可以把上面的代码理解为 "创建一个从pubSubFileChannel到fileChannel1、fileChannel2和fileChannel3的桥，这样pubSubFileChannel的消息就可以同时反馈到这三个通道。"

    4. 服务激活器

        服务激活器是任何在给定方法上定义了@ServiceActivator注解的POJO。这允许我们在收到来自入站通道的消息时执行我们POJO上的任何方法，也允许我们向出站通道写入消息。

        在我们的例子中，我们的服务激活器从配置的输入通道接收一个文件，并把它写到配置的文件夹中。

    5. 适配器

        适配器(Adapter)是一个基于企业集成模式的组件，允许人们 "插入plug-in" 到一个系统或数据源。它几乎就是我们所知的插在墙上的插座或电子设备上的适配器。

        它允许可重复使用的连接到其他 "黑盒子" 系统，如数据库、FTP服务器和消息传递系统，如JMS、AMQP和Twitter等社交网络。连接这些系统的需求无处不在，这意味着适配器是非常可移植和可重复使用的（事实上，有一个小型的[适配器目录](https://docs.spring.io/spring-integration/docs/5.1.0.M1/reference/html/endpoint-summary.html)，免费提供给任何人使用）。

        适配器可分为两大类--入站和出站。

        让我们在我们的示例场景中使用的适配器的背景下检查这些类别：

        入站适配器(Inbound adapters)，正如我们所看到的，用于从外部系统（在本例中是文件系统目录）引入消息。

        我们的入站适配器配置由以下部分组成：

        - 一个@InboundChannelAdapter注解，它将bean配置标记为一个适配器--我们配置了适配器将送入其消息的通道（在我们的例子中，是一个MPEG文件）和一个轮询器(poller)，一个帮助适配器在指定的时间间隔内轮询配置的文件夹的组件
        - 一个标准的Spring java配置类，它返回一个FileReadingMessageSource，即处理文件系统轮询的Spring集成类实现。

        出站适配器(Outbound adapters)是用来向外发送消息的。Spring Integration支持大量的开箱即用的适配器，用于各种常见的使用情况。

6. 总结

    我们研究了Spring Integration的一个基本用例，展示了基于java的配置库和可用组件的可重用性。

    Spring Integration的代码可以在JavaSE中作为独立项目部署，也可以在Jakarta EE环境中作为大项目的一部分部署。虽然它没有直接与其他以EAI为中心的产品和模式（如企业服务总线（ESB））竞争，但它是一个可行的、轻量级的替代方案，可以解决ESB所要解决的许多相同问题。

## Spring集成中的安全问题

1. 简介

    在这篇文章中，我们将重点讨论如何在集成流程中一起使用Spring Integration和Spring Security。

    因此，我们将建立一个简单的安全消息流来演示Spring Integration中Spring Security的使用。此外，我们还将提供在多线程消息通道中传播SecurityContext的例子。

2. 集成配置

    1. 依赖关系

        首先，我们需要在我们的项目中添加Spring Integration的依赖项。

        由于我们将用DirectChannel、PublishSubscribeChannel和ServiceActivator建立一个简单的消息流，我们需要spring-integration-core依赖。

        此外，我们还需要spring-integration-security依赖项，以便能够在Spring Integration中使用Spring Security：

        ```xml
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-security</artifactId>
            <version>5.1.13.RELEASE</version>
        </dependency>
        ```

        我们也在使用Spring Security，所以我们要把spring-security-config添加到我们的项目中：

        ```xml
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-config</artifactId>
            <version>5.1.13.RELEASE</version>
        </dependency>
        ```

        我们可以在Maven中心查看上述所有依赖的最新版本：spring-integration-security, spring-security-config。

    2. 基于Java的配置

        我们的例子将使用基本的Spring集成组件。因此，我们只需要通过使用@EnableIntegration注解在项目中启用Spring集成：

        ```java
        @Configuration
        @EnableIntegration
        public class SecuredDirectChannel {}
        ```

3. 安全的消息通道

    首先，我们需要一个ChannelSecurityInterceptor的实例，它将拦截通道上的所有发送和接收调用，并决定该调用是否可以被执行或拒绝：

    si.security/SecuredDirectChannel.java: channelSecurityInterceptor()

    AuthenticationManager和AccessDecisionManager的Bean被定义为：

    参见：si.security/SecurityConfig.java

    这里，我们使用两个AccessDecisionVoter： RoleVoter和一个自定义的 UsernameAccessDecisionVoter 。

    现在，我们可以使用 ChannelSecurityInterceptor 来保护我们的通道。我们需要做的是用 @SecureChannel 注解来装饰这个通道：

    si.security/SecuredDirectChannel.java

    ```java
    @Bean(name = "startDirectChannel")
    @SecuredChannel(
    interceptor = "channelSecurityInterceptor", 
    sendAccess = { "ROLE_VIEWER","jane" })
    public DirectChannel startDirectChannel() {
        return new DirectChannel();
    }

    @Bean(name = "endDirectChannel")
    @SecuredChannel(
    interceptor = "channelSecurityInterceptor", 
    sendAccess = {"ROLE_EDITOR"})
    public DirectChannel endDirectChannel() {
        return new DirectChannel();
    }
    ```

    @SecureChannel接受三个属性：

    - 拦截器属性：指的是一个ChannelSecurityInterceptor Bean。
    - sendAccess和receiveAccess属性：包含在通道上调用发送或接收动作的策略。

    在上面的例子中，我们希望只有拥有ROLE_VIEWER的用户或者拥有jane的用户可以从startDirectChannel中发送消息。

    同时，只有拥有ROLE_EDITOR的用户可以向endDirectChannel发送消息。

    我们通过自定义的AccessDecisionManager的支持来实现这一点：无论是RoleVoter还是UsernameAccessDecisionVoter都返回肯定的响应，访问权被授予。

4. 安全的服务激活器

    值得一提的是，我们也可以通过Spring方法安全来保护我们的ServiceActivator。因此，我们需要启用方法安全注解：

    si.security/SecurityConfig.java

    ```java
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class SecurityConfig extends GlobalMethodSecurityConfiguration {}
    ```

    为了简单起见，在这篇文章中，我们将只使用Spring的前注解和后注解，所以我们将在配置类中添加@EnableGlobalMethodSecurity注解，并将prePostEnabled设置为true。

    现在我们可以用@PreAuthorization注解来保护我们的ServiceActivator：

    si.security/SecuredDirectChannel.java:

    ```java
    @ServiceActivator(
    inputChannel = "startDirectChannel", 
    outputChannel = "endDirectChannel")
    @PreAuthorize("hasRole('ROLE_LOGGER')")
    public Message<?> logMessage(Message<?> message) {
        Logger.getAnonymousLogger().info(message.toString());
        return message;
    }
    ```

    这里的ServiceActivator从startDirectChannel接收消息并将消息输出到endDirectChannel。

    此外，该方法只有在当前认证委托人具有ROLE_LOGGER角色时才能访问。

5. 安全上下文的传播

    Spring SecurityContext默认是线程绑定的。这意味着SecurityContext不会被传播给子线程。

    在上述所有的例子中，我们都使用了DirectChannel和ServiceActivator--它们都在一个线程中运行；因此，SecurityContext在整个流程中都是可用的。

    然而，当使用QueueChannel、ExecutorChannel和带有Executor的PublishSubscribeChannel时，消息将从一个线程传输到其他线程。在这种情况下，我们需要将SecurityContext传播给所有接收消息的线程。

    让我们创建另一个消息流，它以一个PublishSubscribeChannel通道开始，两个ServiceActivator订阅该通道：

    si.security/SecurityPubSubChannel.java

    ```java
    @Bean(name = "startPSChannel")
    @SecuredChannel(
    interceptor = "channelSecurityInterceptor", 
    sendAccess = "ROLE_VIEWER")
    public PublishSubscribeChannel startChannel() {
        return new PublishSubscribeChannel(executor());
    }

    @ServiceActivator(
    inputChannel = "startPSChannel", 
    outputChannel = "finalPSResult")
    @PreAuthorize("hasRole('ROLE_LOGGER')")
    public Message<?> changeMessageToRole(Message<?> message) {
        return buildNewMessage(getRoles(), message);
    }

    @ServiceActivator(
    inputChannel = "startPSChannel", 
    outputChannel = "finalPSResult")
    @PreAuthorize("hasRole('ROLE_VIEWER')")
    public Message<?> changeMessageToUserName(Message<?> message) {
        return buildNewMessage(getUsername(), message);
    }
    ```

    在上面的例子中，我们有两个ServiceActivator订阅了startPSChannel。该通道需要一个具有ROLE_VIEWER角色的认证委托人，以便能够向其发送消息。

    同样地，只有当认证主体具有 ROLE_LOGGER 角色时，我们才能调用 changeMessageToRole 服务。

    另外，只有当认证主体具有ROLE_VIEWER角色时，才能调用changeMessageToUserName服务。

    同时，startPSChannel将在ThreadPoolTaskExecutor的支持下运行：

    ```java
    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(10);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }
    ```

    因此，两个ServiceActivator将在两个不同的线程中运行。为了将SecurityContext传播给这些线程，我们需要向我们的消息通道添加一个SecurityContextPropagationChannelInterceptor：

    ```java
    @Bean
    @GlobalChannelInterceptor(patterns = { "startPSChannel" })
    public ChannelInterceptor securityContextPropagationInterceptor() {
        return new SecurityContextPropagationChannelInterceptor();
    }
    ```

    注意我们是如何用@GlobalChannelInterceptor注解来装饰SecurityContextPropagationChannelInterceptor的。我们还将我们的startPSChannel添加到它的模式属性中。

    因此，上述配置指出，当前线程的SecurityContext将被传播到任何从startPSChannel派生的线程。

6. 测试

    让我们开始使用一些JUnit测试来验证我们的消息流。

    1. 依赖性

        当然，在这一点上我们需要spring-security-test的依赖：

        ```xml
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <version>5.0.3.RELEASE</version>
            <scope>test</scope>
        </dependency>
        ```

        同样地，可以从Maven中心检查出最新版本：spring-security-test。

    2. 测试安全通道

        首先，我们尝试向startDirectChannel发送一条消息：

        TestSpringIntegrationSecurityIntegrationTest.java

        givenNoUser_whenSendToDirectChannel_thenCredentialNotFound()

        由于该通道是安全的，我们期望在不提供认证对象的情况下发送消息时出现AuthenticationCredentialsNotFoundException异常。

        接下来，我们提供一个拥有ROLE_VIEWER角色的用户，并向我们的startDirectChannel发送一个消息：

        TestSpringIntegrationSecurityIntegrationTest.java

        givenRoleViewer_whenSendToDirectChannel_thenAccessDenied()

        现在，尽管我们的用户可以向startDirectChannel发送消息，因为他有ROLE_VIEWER的角色，但他不能调用logMessage服务，因为它要求有ROLE_LOGGER角色的用户。

        在这种情况下，将抛出一个MessageHandlingException，其原因是AcessDeniedException。

        测试将抛出MessageHandlingException，原因是AccessDeniedExcecption。因此，我们使用ExpectedException规则的一个实例来验证异常的原因。

        接下来，我们提供一个用户名为jane和两个角色的用户： ROLE_LOGGER和ROLE_EDITOR。

        然后尝试再次向startDirectChannel发送一个消息：

        TestSpringIntegrationSecurityIntegrationTest.java

        givenJaneLoggerEditor_whenSendToDirectChannel_thenFlowCompleted()

        消息将成功地贯穿我们的流程，从startDirectChannel开始到logMessage激活器，然后到endDirectChannel。这是因为所提供的认证对象拥有访问这些组件的所有必要权限。

    3. 测试 SecurityContext 传播

        在声明测试用例之前，我们可以回顾一下我们的例子与PublishSubscribeChannel的整个流程：

        - 该流程从一个startPSChannel开始，该通道的策略是sendAccess = "ROLE_VIEWER"
        - 两个ServiceActivator订阅该通道：一个有安全注释@PreAuthorize("hasRole('ROLLE_LOGGER')") 一个有安全注释 @PreAuthorize("hasRole('ROLLE_VIEWER')")

        因此，首先我们提供一个拥有ROLE_VIEWER角色的用户，并尝试向我们的频道发送一个消息：

        TestSpringIntegrationSecurityExecutorIntegrationTest.java

        givenRoleUser_whenSendMessageToPSChannel_thenNoMessageArrived()

        由于我们的用户只有ROLE_VIEWER的角色，消息只能通过startPSChannel和一个ServiceActivator。

        因此，在流程的最后，我们只收到一条消息。

        让我们提供一个同时拥有ROLE_VIEWER和ROLE_LOGGER角色的用户：

        TestSpringIntegrationSecurityExecutorIntegrationTest.java

        givenRoleUserAndLogger_whenSendMessageToPSChannel_then2GetMessages()

        现在，我们可以在流程结束时收到这两条消息，因为用户拥有它所需要的所有权限。

7. 总结

    在本教程中，我们已经探索了在Spring Integration中使用Spring Security来保护消息通道和ServiceActivator的可能性。

## Spring Integration Java DSL

1. 简介

    在本教程中，我们将学习用于创建应用程序集成的Spring Integration Java DSL。

    我们将使用在《Spring Integration简介》中建立的文件移动集成，并使用DSL来代替。

2. 依赖关系

    Spring Integration Java DSL是[Spring Integration Core](https://search.maven.org/classic/#search%7Cgav%7C1%7Cg%3A%22org.springframework.integration%22%20AND%20a%3A%22spring-integration-core%22)的一部分。

    为了完成我们的文件移动应用，我们需要Spring集成核心与文件包：

    ```xml
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-file</artifactId>
    </dependency>
    ```

3. Spring Integration Java DSL

    在使用Java DSL之前，用户需要在XML中配置Spring Integration组件。

    DSL引入了一些流畅的构建器，我们可以轻松地用Java创建一个完整的Spring Integration管道。

    所以，假设我们想创建一个通道，把通过管道的任何数据大写。

    在过去，我们可能会这样做：

    ```xml
    <int:channel id="input"/>
    <int:transformer input-channel="input" expression="payload.toUpperCase()" />
    ```

    而现在我们可以改成这样：

    ```java
    @Bean
    public IntegrationFlow upcaseFlow() {
        return IntegrationFlows.from("input")
        .transform(String::toUpperCase)
        .get();
    }
    ```

4. 文件移动应用程序

    为了开始我们的文件移动集成，我们需要一些简单的构建模块。

    1. 集成流程

        我们需要的第一个构件是一个集成流，我们可以从IntegrationFlows构建器中获得：

        `IntegrationFlows.from(...)`

        from可以有几种类型，但在本教程中，我们将只看三种：

        - MessageSources 消息源
        - MessageChannels
        - Strings 字符串

        我们很快就会讨论这三种类型。

        在我们调用了from之后，一些定制的方法现在可以使用了：

        ```java
        IntegrationFlow flow = IntegrationFlows.from(sourceDirectory())
            .filter(onlyJpgs())
            .handle(targetDirectory())
            // add more components
            .get();
        ```

        最终，IntegrationFlows 总是会产生 IntegrationFlow 的实例，它是任何 Spring Integration 应用程序的最终产品。

        这种接受输入，执行适当的转换，并发出结果的模式是所有 Spring Integration 应用程序的基础。

    2. 描述一个输入源

        首先，为了移动文件，我们需要向我们的集成流程指出它应该在哪里寻找这些文件，为此，我们需要一个MessageSource：

        ```java
        @Bean
        public MessageSource<File> sourceDirectory() {
        // .. create a message source
        }
        ```

        简单地说，MessageSource是一个可以[发出应用程序外部消息](http://joshlong.com/jl/blogPost/spring_integration_adapters_gateways_and_channels.html)的地方。

        更具体地说，我们需要的是能够将外部消息源改编为Spring的消息传递表示。由于这种适应是针对输入的，所以这些通常被称为输入通道适配器(Input Channel Adapters)。

        Spring-integration-file依赖关系为我们提供了一个输入通道适配器，非常适合我们的用例： FileReadingMessageSource：

        ```java
        @Bean
        public MessageSource<File> sourceDirectory() {
            FileReadingMessageSource messageSource = new FileReadingMessageSource();
            messageSource.setDirectory(new File(INPUT_DIR));
            return messageSource;
        }
        ```

        在这里，我们的FileReadingMessageSource将读取一个由INPUT_DIR给出的目录，并从中创建一个MessageSource。

        让我们在 IntegrationFlows.from 调用中指定它作为我们的源：

        `IntegrationFlows.from(sourceDirectory());`

    3. 配置一个输入源

        现在，如果我们考虑将其作为一个长期存在的应用程序，我们可能希望能够在文件进入时注意到它们，而不仅仅是在启动时移动已经存在的文件。

        为了方便这一点，from也可以采取额外的配置器作为输入源的进一步定制：

        `IntegrationFlows.from(sourceDirectory(), configurer -> configurer.poller(Pollers.fixedDelay(10000)));`

        在这种情况下，我们可以通过告诉Spring Integration每隔10秒轮询一次该源--本例中是我们的文件系统--来使我们的输入源更具弹性。

        当然，这并不只适用于我们的文件输入源，我们可以将这个轮询器添加到任何MessageSource中。

    4. 过滤输入源的消息

        接下来，让我们假设我们的文件移动程序只移动特定的文件，例如扩展名为jpg的图像文件。

        为此，我们可以使用GenericSelector：

        ```java
        @Bean
        public GenericSelector<File> onlyJpgs() {
            return new GenericSelector<File>() {
                @Override
                public boolean accept(File source) {
                return source.getName().endsWith(".jpg");
                }
            };
        }
        ```

        那么，让我们再次更新我们的集成流程：

        `IntegrationFlows.from(sourceDirectory()).filter(onlyJpgs());`

        或者，因为这个过滤器是如此简单，我们可以用一个lambda来定义它：

        `IntegrationFlows.from(sourceDirectory()).filter(source -> ((File) source).getName().endsWith(".jpg"));`

    5. 用服务激活器处理消息

        现在我们有了一个经过过滤的文件列表，我们需要把它们写到一个新的位置。

        当我们考虑Spring Integration中的输出时，服务激活器是我们所求助的对象。

        让我们使用spring-integration-file中的FileWritingMessageHandler服务激活器：

        ```java
        @Bean
        public MessageHandler targetDirectory() {
            FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
            handler.setFileExistsMode(FileExistsMode.REPLACE);
            handler.setExpectReply(false);
            return handler;
        }
        ```

        在这里，我们的FileWritingMessageHandler将把它收到的每个Message payload写到OUTPUT_DIR。

        再次，让我们来更新：

        ```java
        IntegrationFlows.from(sourceDirectory())
        .filter(onlyJpgs())
        .handle(targetDirectory());
        ```

        顺便注意，setExpectReply的用法。因为集成流可以是双向的，这个调用表明这个特定的管道是单向的。

    6. 激活我们的集成流程

        当我们添加了所有的组件后，我们需要将我们的集成流注册为一个Bean来激活它：

        ```java
        @Bean
        public IntegrationFlow fileMover() {
            return IntegrationFlows.from(sourceDirectory(), c -> c.poller(Pollers.fixedDelay(10000)))
            .filter(onlyJpgs())
            .handle(targetDirectory())
            .get();
        }
        ```

        get方法提取了一个IntegrationFlow实例，我们需要将其注册为一个Spring Bean。

        一旦我们的应用程序上下文加载，我们的 IntegrationFlow 中包含的所有组件就会被激活。

        现在，我们的应用程序将开始从源目录向目标目录移动文件。

5. 附加组件

    在我们基于DSL的文件移动应用程序中，我们创建了一个入站通道适配器，一个消息过滤器，和一个服务激活器。

    让我们看看其他几个常见的Spring集成组件，看看我们如何使用它们。

    1. 消息通道

        如前所述，消息通道(Message Channels)是另一种初始化流程的方式：

        `IntegrationFlows.from("anyChannel")`

        我们可以把它理解为 "请找到或创建一个名为anyChannel的通道Bean。然后，读取从其他流程中输入到anyChannel的任何数据"。

        但是，实际上它比这更具有通用性。

        简单地说，通道将生产者与消费者抽象开来，我们可以把它看成是一个Java队列。一个通道可以在流程中的任何一点插入。

        比如说，我们想在文件从一个目录移到另一个目录时对它们进行优先排序：

        ```java
        @Bean
        public PriorityChannel alphabetically() {
            return new PriorityChannel(1000, (left, right) -> 
            ((File)left.getPayload()).getName().compareTo(
                ((File)right.getPayload()).getName()));
        }
        ```

        然后，我们可以在我们的流程中插入一个对通道的调用：

        ```java
        @Bean
        public IntegrationFlow fileMover() {
            return IntegrationFlows.from(sourceDirectory())
            .filter(onlyJpgs())
            .channel("alphabetically")
            .handle(targetDirectory())
            .get();
        }
        ```

        有几十个通道可供选择，其中一些更方便的是用于并发、审计或中间持久化（想想Kafka或JMS缓冲区）。

        另外，当通道与Bridges结合时，也可以很强大。

    2. Bridge

        当我们想结合两个通道时，我们使用一个桥接。

        让我们想象一下，我们不是直接写到一个输出目录，而是让我们的文件移动应用写到另一个通道：

        ```java
        @Bean
        public IntegrationFlow fileReader() {
            return IntegrationFlows.from(sourceDirectory())
            .filter(onlyJpgs())
            .channel("holdingTank")
            .get();
        }
        ```

        现在，因为我们已经简单地把它写入一个通道，我们可以从那里桥接到其他流量。

        让我们创建一个桥，轮询我们的保温箱的消息，并将它们写入一个目的地：

        ```java
        @Bean
        public IntegrationFlow fileWriter() {
            return IntegrationFlows.from("holdingTank")
            .bridge(e -> e.poller(Pollers.fixedRate(1, TimeUnit.SECONDS, 20)))
            .handle(targetDirectory())
            .get();
        }
        ```

        同样，因为我们写到了一个中间通道，现在我们可以添加另一个流程，以不同的速度获取这些相同的文件并写入它们：

        ```java
        @Bean
        public IntegrationFlow anotherFileWriter() {
            return IntegrationFlows.from("holdingTank")
            .bridge(e -> e.poller(Pollers.fixedRate(2, TimeUnit.SECONDS, 10)))
            .handle(anotherTargetDirectory())
            .get();
        }
        ```

        正如我们所看到的，个别桥梁可以控制不同处理程序的轮询配置。

        一旦我们的应用程序上下文被加载，我们现在有一个更复杂的应用程序在运行，它将开始从源目录向两个目标目录移动文件。

6. 总结

    在这篇文章中，我们看到了使用Spring Integration Java DSL来构建不同集成管道的各种方法。

    从本质上讲，我们能够重新创建之前教程中的文件移动应用，这次使用的是纯java。

    此外，我们还看了一些其他组件，如通道和桥接。

## Relevant Articles

- [x] [Introduction to Spring Integration](https://www.baeldung.com/spring-integration)
- [x] [Security In Spring Integration](https://www.baeldung.com/spring-integration-security)
- [x] [Spring Integration Java DSL](https://www.baeldung.com/spring-integration-java-dsl)
- [Using Subflows in Spring Integration](https://www.baeldung.com/spring-integration-subflows)
- [Transaction Support in Spring Integration](https://www.baeldung.com/spring-integration-transaction)

## Running the Sample

Executing the `mvn exec:java` maven command (either from the command line or from an IDE) will start up the application. Follow the command prompt for further instructions.

## Code

你可以在[Github项目](https://github.com/eugenp/tutorials/tree/master/spring-integration)中找到本文的源代码。
