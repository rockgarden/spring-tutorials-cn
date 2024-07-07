# Spring WebSockets

## 使用Spring的WebSockets介绍

1. 概述

    在本教程中，我们将创建一个简单的Web应用程序，使用Spring Framework 4.0引入的新的WebSocket功能实现消息传递。

    WebSocket是Web浏览器和服务器之间的一种双向、全双工、持久的连接。一旦建立了WebSocket连接，该连接就会保持开放，直到客户端或服务器决定关闭该连接。

    一个典型的用例可能是，当一个应用程序涉及多个用户相互沟通时，例如在聊天中。在我们的例子中，我们将建立一个简单的聊天客户端。

2. Maven依赖性

    由于这是一个基于Maven的项目，我们首先在pom.xml中添加必要的依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-websocket</artifactId>
        <version>5.2.2.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-messaging</artifactId>
        <version>5.2.2.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>2.10.2</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId> 
        <version>2.10.2</version>
    </dependency>
    ```

    此外，我们需要添加Jackson的依赖项，因为我们将使用JSON来构建消息的主体。

    这允许Spring将我们的Java对象转换 to/from JSON。

3. 在Spring中启用WebSocket

    首先，我们启用WebSocket功能。为了做到这一点，我们需要为我们的应用程序添加一个配置，并用@EnableWebSocketMessageBroker来注释这个类。

    顾名思义，它可以在消息代理的支持下启用WebSocket消息处理：

    ```java
    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/chat");
            registry.addEndpoint("/chat").withSockJS();
        }
    }
    ```

    在这里，我们可以看到configureMessageBroker方法是用来配置消息代理的。

    首先，我们启用一个内存中的消息代理，在以"/topic"为前缀的目的地上将消息传回给客户端。

    我们通过指定"/app"前缀来完成我们的简单配置，以过滤针对应用程序注释的方法的目的地（通过 @MessageMapping ）。

    registerStompEndpoints方法注册了"/chat"端点，启用Spring的[STOMP](http://stomp.github.io/stomp-specification-1.2.html#Abstract)支持。请记住，我们在这里也添加了一个端点，为了弹性起见，它在没有SockJS的情况下工作。

    这个端点，如果前缀为"/app"，就是ChatController.send()方法被映射为处理的端点。

    它还启用了[SockJS](https://github.com/sockjs/sockjs-protocol)回退选项，以便在WebSocket不可用的情况下可以使用其他的消息传递选项。这很有用，因为WebSocket还不被所有浏览器支持，而且可能被限制性的网络代理排除在外。

    回退选项让应用程序使用WebSocket API，但在运行时，必要时可以优雅地降级为非WebSocket替代方案。

4. 创建消息模型

    现在我们已经建立了项目并配置了WebSocket功能，我们需要创建一个消息来发送。

    该端点将接受包含发件人姓名和文本的STOMP消息，其主体是一个JSON对象。

    该消息可能看起来像这样：

    ```json
    {
        "from": "John",
        "text": "Hello!"
    }
    ```

    为了对携带文本的消息进行建模，我们可以创建一个简单的具有from和text属性的Java对象：

    websockets/Message.java

    默认情况下，Spring将使用Jackson库将我们的模型对象转换为JSON。

5. 创建一个消息处理控制器

    正如我们所看到的，Spring处理STOMP消息的方法是将一个控制器方法与配置的端点相关联。我们可以通过@MessageMapping注解做到这一点。

    端点和控制器之间的关联使我们有能力在需要时处理消息：

    ```java
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(Message message) throws Exception {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getText(), time);
    }
    ```

    在我们的例子中，我们将创建另一个名为OutputMessage的模型对象来表示发送到配置的目的地的输出消息。我们用发件人和从传入消息中提取的消息文本来填充我们的对象，并用时间戳来充实它。

    在处理完我们的消息后，我们把它发送到用@SendTo注解定义的适当目的地。所有在"/topic/messages"目的地的订阅者都将收到该消息。

6. 创建一个浏览器客户端

    在服务器端进行配置后，我们将使用[sockjs-client](https://github.com/sockjs/sockjs-client)库来建立一个简单的HTML页面，与我们的信息传递系统进行交互。

    首先，我们需要导入sockjs和stomp JavaScript客户端库。

    接下来，我们可以创建一个connect()函数来打开与我们端点的通信，一个sendMessage()函数来发送我们的STOMP消息，一个disconnect()函数来关闭通信：

    参见：webapp/index.html

7. 测试该例子

    为了测试我们的例子，我们可以打开几个浏览器窗口，访问聊天页面：

    <http://localhost:8080>

    一旦完成，我们就可以通过输入一个昵称并点击连接按钮加入聊天。如果我们编写并发送一条消息，我们可以在所有加入聊天的浏览器会话中看到它。

8. 总结

在这篇文章中，我们探讨了Spring的WebSocket支持。我们看到了它的服务器端配置，并使用sockjs和stomp JavaScript库构建了一个简单的客户端对应。

## Spring Websockets的@SendToUser注释的一个快速例子

1. 概述

    在这个快速教程中，我们将说明如何使用Spring WebSockets向特定会话或特定用户发送消息。

2. WebSocket配置

    首先，我们需要配置我们的消息代理和WebSocket应用端点：

    ```java
    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig
    extends AbstractWebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic/", "/queue/");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/greeting");
        }
    }
    ```

    通过@EnableWebSocketMessageBroker，我们使用STOMP启用了WebSocket上的经纪人支持的消息传递，STOMP代表面向文本的流式消息传递协议。值得注意的是，这个注解需要与@Configuration结合使用。

    并非一定要扩展AbstractWebSocketMessageBrokerConfigurer，但对于这个快速的例子来说，定制导入的配置会更容易。

    在第一个方法中，我们设置了一个简单的基于内存的消息代理，将消息传回客户端，目的地前缀为"/topic"和"/queue"。

    而在第二种方法中，我们在"/greeting"注册了stomp端点。

    如果我们想启用SockJS，我们必须修改注册部分：

    `registry.addEndpoint("/greeting").withSockJS();`

3. 通过拦截器获取会话ID

    获取会话ID的一个方法是添加一个Spring拦截器，它将在握手过程中被触发并从请求数据中获取信息。

    这个拦截器可以直接添加到WebSocketConfig中：

    ```java
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/greeting").setHandshakeHandler(new DefaultHandshakeHandler() {
            public boolean beforeHandshake(
            ServerHttpRequest request, 
            ServerHttpResponse response, 
            WebSocketHandler wsHandler,
            Map attributes) throws Exception {
    
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest
                    = (ServletServerHttpRequest) request;
                    HttpSession session = servletRequest
                    .getServletRequest().getSession();
                    attributes.put("sessionId", session.getId());
                }
                    return true;
            }}).withSockJS();
    }
    ```

4. WebSocket端点

    从Spring 5.0.5.RELEASE开始，由于@SendToUser注解的改进，我们不需要做任何定制，它允许我们通过"/user/{sessionId}/... "而不是"/user/{user}/... "向用户目的地发送消息。

    这意味着该注解依靠输入消息的会话ID来工作，有效地将回复发送到该会话私有的目的地：

    ```java
    @Controller
    public class WebSocketController {

        @Autowired
        private SimpMessageSendingOperations messagingTemplate;

        private Gson gson = new Gson();
    
        @MessageMapping("/message")
        @SendToUser("/queue/reply")
        public String processMessageFromClient(
        @Payload String message, 
        Principal principal) throws Exception {
            return gson
                .fromJson(message, Map.class)
                .get("name").toString();
        }

        @MessageExceptionHandler
        @SendToUser("/queue/errors")
        public String handleException(Throwable exception) {
            return exception.getMessage();
        }
    }
    ```

    值得注意的是，@SendToUser表示一个消息处理方法的返回值应该作为一个消息发送到指定的目的地，前面加上"/user/{username}"。

5. WebSocket客户端

    ```js
    function connect() {
        var socket = new WebSocket('ws://localhost:8080/greeting');
        ws = Stomp.over(socket);

        ws.connect({}, function(frame) {
            ws.subscribe("/user/queue/errors", function(message) {
                alert("Error " + message.body);
            });

            ws.subscribe("/user/queue/reply", function(message) {
                alert("Message " + message.body);
            });
        }, function(error) {
            alert("STOMP error " + error);
        });
    }

    function disconnect() {
        if (ws != null) {
            ws.close();
        }
        setConnected(false);
        console.log("Disconnected");
    }
    ```

    为WebSocketConfiguration中的映射创建一个新的WebSocket，指向"/greeting"。

    当我们将客户端订阅到"/user/queue/errors"和"/user/queue/reply"时，我们会使用上一节中的备注信息。

    我们可以看到，@SendToUser指向 "queue/errors"，但消息将被发送到"/user/queue/errors"。

6. 总结

    WebSocket直接向用户或会话ID发送消息的方法。

## 用Spring Boot进行预定的WebSocket推送

1. 概述

    在本教程中，我们将看到如何使用[WebSockets](https://www.baeldung.com/java-websockets)从服务器向浏览器发送预定消息。另一种方法是使用服务器发送的事件（[SSE](https://www.baeldung.com/spring-server-sent-events)），但我们不会在本文中涉及。

    Spring提供了多种调度选项。首先，我们将介绍[@Scheduled](https://www.baeldung.com/spring-scheduling-annotations#scheduled)注解。然后，我们将看到一个由Project Reactor提供的[Flux::interval](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#interval-java.time.Duration-)方法的例子。这个库对Webflux应用来说是开箱即用的，它可以作为一个独立的库在任何Java项目中使用。

    另外，还有更高级的机制存在，比如[Quartz调度器](https://www.baeldung.com/quartz)，但我们不会涉及它们。

2. 一个简单的聊天程序

    在之前的文章中，我们用WebSockets建立了一个聊天应用程序。让我们用一个新的功能来扩展它：聊天机器人。这些机器人是服务器端的组件，可以向浏览器推送预定信息。

    1. Maven的依赖性

        让我们先在Maven中设置必要的依赖项。要构建这个项目，我们的pom.xml应该有：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.javafaker</groupId>
            <artifactId>javafaker</artifactId>
            <version>1.0.2</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </dependency>
        ```

    2. JavaFaker依赖项

        我们将使用[JavaFaker](https://www.baeldung.com/java-faker)库来生成机器人的信息。这个库经常被用来生成测试数据。在这里，我们将向我们的聊天室添加一个名为 "Chuck Norris" 的客人。

        让我们看看代码：

        ```java
        Faker faker = new Faker();
        ChuckNorris chuckNorris = faker.chuckNorris();
        String messageFromChuck = chuckNorris.fact();
        ```

        Faker将为各种数据生成器提供工厂方法。我们将使用[ChuckNorris](https://dius.github.io/java-faker/apidocs/com/github/javafaker/ChuckNorris.html)生成器。对chuckNorris.fact()的调用将从预定义的信息列表中显示一个随机句子。

    3. 数据模型

        聊天应用程序使用一个简单的POJO作为消息包装器：

        websockets/OutputMessage.java

        把这一切放在一起，下面是一个我们如何创建聊天信息的例子：

        ```java
        OutputMessage message = new OutputMessage(
            "Chatbot 1", "Hello there!", new SimpleDateFormat("HH:mm").format(new Date())));
        ```

    4. 客户端

        我们的聊天客户端是一个简单的HTML页面。它使用一个SockJS客户端和STOMP消息协议。

        ```html
        <head>
            <script src="./js/sockjs-0.3.4.js"></script>
            <script type="text/javascript">
                // ...
                stompClient = Stomp.over(socket);        
                stompClient.connect({}, function(frame) {
                    // ...
                    stompClient.subscribe('/topic/pushmessages', function(messageOutput) {
                        showMessageOutput(JSON.parse(messageOutput.body));
                    });
                });
                // ...
            </script>
        </head>
        ```

        首先，我们通过SockJS协议创建了一个Stomp客户端。然后，主题订阅作为服务器和连接的客户端之间的通信渠道。

        在我们的资源库中，这段代码在webapp/bots.html中。我们在本地运行时访问它，<http://localhost:8080/bots.html> 。当然，我们需要根据我们部署应用程序的方式来调整主机和端口。

    5. 服务器端

        我们在之前的文章中已经看到了如何在Spring中配置WebSockets。让我们稍微修改一下这个配置：

        ```java
        @Configuration
        @EnableWebSocketMessageBroker
        public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
            // ...
            @Override
            public void registerStompEndpoints(StompEndpointRegistry registry) {
                // ...
                registry.addEndpoint("/chatwithbots");
                registry.addEndpoint("/chatwithbots").withSockJS();
            }
        }
        ```

        为了推送我们的消息，我们使用实用类[SimpMessagingTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/messaging/simp/SimpMessagingTemplate.html)。默认情况下，它作为Spring Context中的一个@Bean可用。我们可以看到当[AbstractMessageBrokerConfiguration](https://github.com/spring-projects/spring-framework/blob/5b910a87c38386e870eba1f3f8154db2de4df026/spring-messaging/src/main/java/org/springframework/messaging/simp/config/AbstractMessageBrokerConfiguration.java#L392)在classpath中时，它是如何通过自动配置来声明的。因此，我们可以将它注入任何Spring组件中。

        之后，我们用它来发布消息到主题/topic/pushmessages。我们假设我们的类在一个名为simpMessagingTemplate的变量中注入了该bean：

        ```java
        simpMessagingTemplate.convertAndSend("/topic/pushmessages", 
            new OutputMessage("Chuck Norris", faker.chuckNorris().fact(), time));
        ```

        正如之前在我们的客户端例子中显示的那样，客户端订阅了该主题，以便在消息到达时进行处理。

3. 调度推送消息

    在Spring生态系统中，我们可以选择各种调度方法。如果我们使用Spring MVC，@Scheduled注解因其简单性而成为自然选择。如果我们使用Spring Webflux，我们也可以使用Project Reactor的Flux::interval方法。我们将看到各自的一个例子。

    1. 配置

        我们的聊天机器人将使用JavaFaker的Chuck Norris发生器。我们将把它配置成一个Bean，这样我们就可以把它注入我们需要的地方。

        ```java
        @Configuration
        class AppConfig {
            @Bean
            public ChuckNorris chuckNorris() {
                return (new Faker()).chuckNorris();
            }
        }
        ```

    2. 使用@Scheduled

        我们的示例机器人是预定方法。当它们运行时，它们通过使用SimpMessagingTemplate的WebSocket发送我们的OutputMessage POJOs。

        正如它的名字所暗示的，[@Scheduled](https://www.baeldung.com/spring-scheduling-annotations#scheduled)注解允许重复执行方法。有了它，我们可以使用简单的基于速率的调度或更复杂的 "cron" 表达式。

        让我们来编写我们的第一个聊天工具：

        ```java
        @Service
        public class ScheduledPushMessages {
            @Scheduled(fixedRate = 5000)
            public void sendMessage(SimpMessagingTemplate simpMessagingTemplate, ChuckNorris chuckNorris) {
                String time = new SimpleDateFormat("HH:mm").format(new Date());
                simpMessagingTemplate.convertAndSend("/topic/pushmessages", 
                new OutputMessage("Chuck Norris (@Scheduled)", chuckNorris().fact(), time));
            }
        }
        ```

        我们用@Scheduled(fixedRate = 5000)来注解sendMessage方法。这使得sendMessage每五秒运行一次。然后，我们使用simpMessagingTemplate实例向主题发送一个OutputMessage。simpMessagingTemplate和chuckNorris实例被作为方法参数从Spring上下文中注入。

    3. 使用Flux::interval()

        如果我们使用WebFlux，我们可以使用Flux::interval操作。它将发布一个由选定的[Duration](https://www.baeldung.com/java-period-duration#duration-class)分隔的无限流long items。

        现在，让我们将Flux用于我们之前的例子。我们的目标是每五秒钟发送一条来自Chuck Norris的报价。首先，我们需要实现InitializingBean接口，在[应用程序启动时](https://www.baeldung.com/running-setup-logic-on-startup-in-spring)订阅Flux：

        ```java
        @Service
        public class ReactiveScheduledPushMessages implements InitializingBean {

            private SimpMessagingTemplate simpMessagingTemplate;
            private ChuckNorris chuckNorris;

            @Autowired
            public ReactiveScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate, ChuckNorris chuckNorris) {
                this.simpMessagingTemplate = simpMessagingTemplate;
                this.chuckNorris = chuckNorris;
            }

            @Override
            public void afterPropertiesSet() throws Exception {
                Flux.interval(Duration.ofSeconds(5L))
                    // discard the incoming Long, replace it by an OutputMessage
                    .map((n) -> new OutputMessage("Chuck Norris (Flux::interval)", 
                                    chuckNorris.fact(), 
                                    new SimpleDateFormat("HH:mm").format(new Date()))) 
                    .subscribe(message -> simpMessagingTemplate.convertAndSend("/topic/pushmessages", message));
            }
        }
        ```

        这里，我们使用构造函数注入来设置simpMessagingTemplate和chuckNorris实例。这一次，调度逻辑在afterPropertiesSet()中，我们在实现InitializingBean时覆盖了这个方法。该方法将在服务启动后立即运行。

        [间隔](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#interval-java.time.Duration-)操作符每隔五秒就会发出一个Long。然后，[map](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-)操作者丢弃该值，并将其替换为我们的消息。最后，我们[订阅](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#subscribe-java.util.function.Consumer-)Flux以触发我们对每个消息的逻辑。

4. 总结

    在本教程中，我们看到实用类SimpMessagingTemplate使我们能够轻松地通过WebSocket推送服务器消息。此外，我们还看到了两种调度执行一段代码的方法。

## Relevant articles

- [x] [Intro to WebSockets with Spring](https://www.baeldung.com/websockets-spring)
- [A Quick Example of Spring Websockets’ @SendToUser Annotation](https://www.baeldung.com/spring-websockets-sendtouser)
- [x] [Scheduled WebSocket Push with Spring Boot](https://www.baeldung.com/spring-boot-scheduled-websocket)
- [Test WebSocket APIs With Postman](https://www.baeldung.com/postman-websocket-apis)
- [Debugging WebSockets](https://www.baeldung.com/debug-websockets)

## Code

示例代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-websockets)项目中找到。
