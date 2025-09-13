# [Spring Websockets 的@SendToUser 注释的一个快速例子](https://www.baeldung.com/spring-boot-scheduled-websocket)

1. 概述

   在这个快速教程中，我们将说明如何使用 Spring WebSockets 向特定会话或特定用户发送消息。

2. WebSocket 配置

   首先，我们需要配置我们的消息代理和 WebSocket 应用端点：

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

   通过@EnableWebSocketMessageBroker，我们使用 STOMP 启用了 WebSocket 上的经纪人支持的消息传递，STOMP 代表面向文本的流式消息传递协议。值得注意的是，这个注解需要与@Configuration 结合使用。

   并非一定要扩展 AbstractWebSocketMessageBrokerConfigurer，但对于这个快速的例子来说，定制导入的配置会更容易。

   在第一个方法中，我们设置了一个简单的基于内存的消息代理，将消息传回客户端，目的地前缀为"/topic"和"/queue"。

   而在第二种方法中，我们在"/greeting"注册了 stomp 端点。

   如果我们想启用 SockJS，我们必须修改注册部分：

   `registry.addEndpoint("/greeting").withSockJS();`

3. 通过拦截器获取会话 ID

   获取会话 ID 的一个方法是添加一个 Spring 拦截器，它将在握手过程中被触发并从请求数据中获取信息。

   这个拦截器可以直接添加到 WebSocketConfig 中：

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

4. WebSocket 端点

   从 Spring 5.0.5.RELEASE 开始，由于@SendToUser 注解的改进，我们不需要做任何定制，它允许我们通过"/user/{sessionId}/... "而不是"/user/{user}/... "向用户目的地发送消息。

   这意味着该注解依靠输入消息的会话 ID 来工作，有效地将回复发送到该会话私有的目的地：

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

   值得注意的是，@SendToUser 表示一个消息处理方法的返回值应该作为一个消息发送到指定的目的地，前面加上"/user/{username}"。

5. WebSocket 客户端

   ```js
   function connect() {
     var socket = new WebSocket("ws://localhost:8080/greeting");
     ws = Stomp.over(socket);

     ws.connect(
       {},
       function (frame) {
         ws.subscribe("/user/queue/errors", function (message) {
           alert("Error " + message.body);
         });

         ws.subscribe("/user/queue/reply", function (message) {
           alert("Message " + message.body);
         });
       },
       function (error) {
         alert("STOMP error " + error);
       }
     );
   }

   function disconnect() {
     if (ws != null) {
       ws.close();
     }
     setConnected(false);
     console.log("Disconnected");
   }
   ```

   为 WebSocketConfiguration 中的映射创建一个新的 WebSocket，指向"/greeting"。

   当我们将客户端订阅到"/user/queue/errors"和"/user/queue/reply"时，我们会使用上一节中的备注信息。

   我们可以看到，@SendToUser 指向 "queue/errors"，但消息将被发送到"/user/queue/errors"。

6. 总结

   WebSocket 直接向用户或会话 ID 发送消息的方法。

## 用 Spring Boot 进行预定的 WebSocket 推送

1. 概述

   在本教程中，我们将看到如何使用[WebSockets](https://www.baeldung.com/java-websockets)从服务器向浏览器发送预定消息。另一种方法是使用服务器发送的事件（[SSE](https://www.baeldung.com/spring-server-sent-events)），但我们不会在本文中涉及。

   Spring 提供了多种调度选项。首先，我们将介绍[@Scheduled](https://www.baeldung.com/spring-scheduling-annotations#scheduled)注解。然后，我们将看到一个由 Project Reactor 提供的[Flux::interval](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#interval-java.time.Duration-)方法的例子。这个库对 Webflux 应用来说是开箱即用的，它可以作为一个独立的库在任何 Java 项目中使用。

   另外，还有更高级的机制存在，比如[Quartz 调度器](https://www.baeldung.com/quartz)，但我们不会涉及它们。

2. 一个简单的聊天程序

   在之前的文章中，我们用 WebSockets 建立了一个聊天应用程序。让我们用一个新的功能来扩展它：聊天机器人。这些机器人是服务器端的组件，可以向浏览器推送预定信息。

   1. Maven 的依赖性

      让我们先在 Maven 中设置必要的依赖项。要构建这个项目，我们的 pom.xml 应该有：

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

   2. JavaFaker 依赖项

      我们将使用[JavaFaker](https://www.baeldung.com/java-faker)库来生成机器人的信息。这个库经常被用来生成测试数据。在这里，我们将向我们的聊天室添加一个名为 "Chuck Norris" 的客人。

      让我们看看代码：

      ```java
      Faker faker = new Faker();
      ChuckNorris chuckNorris = faker.chuckNorris();
      String messageFromChuck = chuckNorris.fact();
      ```

      Faker 将为各种数据生成器提供工厂方法。我们将使用[ChuckNorris](https://dius.github.io/java-faker/apidocs/com/github/javafaker/ChuckNorris.html)生成器。对 chuckNorris.fact()的调用将从预定义的信息列表中显示一个随机句子。

   3. 数据模型

      聊天应用程序使用一个简单的 POJO 作为消息包装器：

      websockets/OutputMessage.java

      把这一切放在一起，下面是一个我们如何创建聊天信息的例子：

      ```java
      OutputMessage message = new OutputMessage(
          "Chatbot 1", "Hello there!", new SimpleDateFormat("HH:mm").format(new Date())));
      ```

   4. 客户端

      我们的聊天客户端是一个简单的 HTML 页面。它使用一个 SockJS 客户端和 STOMP 消息协议。

      ```html
      <head>
        <script src="./js/sockjs-0.3.4.js"></script>
        <script type="text/javascript">
          // ...
          stompClient = Stomp.over(socket);
          stompClient.connect({}, function (frame) {
            // ...
            stompClient.subscribe(
              "/topic/pushmessages",
              function (messageOutput) {
                showMessageOutput(JSON.parse(messageOutput.body));
              }
            );
          });
          // ...
        </script>
      </head>
      ```

      首先，我们通过 SockJS 协议创建了一个 Stomp 客户端。然后，主题订阅作为服务器和连接的客户端之间的通信渠道。

      在我们的资源库中，这段代码在 webapp/bots.html 中。我们在本地运行时访问它，<http://localhost:8080/bots.html> 。当然，我们需要根据我们部署应用程序的方式来调整主机和端口。

   5. 服务器端

      我们在之前的文章中已经看到了如何在 Spring 中配置 WebSockets。让我们稍微修改一下这个配置：

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

      为了推送我们的消息，我们使用实用类[SimpMessagingTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/messaging/simp/SimpMessagingTemplate.html)。默认情况下，它作为 Spring Context 中的一个@Bean 可用。我们可以看到当[AbstractMessageBrokerConfiguration](https://github.com/spring-projects/spring-framework/blob/5b910a87c38386e870eba1f3f8154db2de4df026/spring-messaging/src/main/java/org/springframework/messaging/simp/config/AbstractMessageBrokerConfiguration.java#L392)在 classpath 中时，它是如何通过自动配置来声明的。因此，我们可以将它注入任何 Spring 组件中。

      之后，我们用它来发布消息到主题/topic/pushmessages。我们假设我们的类在一个名为 simpMessagingTemplate 的变量中注入了该 bean：

      ```java
      simpMessagingTemplate.convertAndSend("/topic/pushmessages",
          new OutputMessage("Chuck Norris", faker.chuckNorris().fact(), time));
      ```

      正如之前在我们的客户端例子中显示的那样，客户端订阅了该主题，以便在消息到达时进行处理。

3. 调度推送消息

   在 Spring 生态系统中，我们可以选择各种调度方法。如果我们使用 Spring MVC，@Scheduled 注解因其简单性而成为自然选择。如果我们使用 Spring Webflux，我们也可以使用 Project Reactor 的 Flux::interval 方法。我们将看到各自的一个例子。

   1. 配置

      我们的聊天机器人将使用 JavaFaker 的 Chuck Norris 发生器。我们将把它配置成一个 Bean，这样我们就可以把它注入我们需要的地方。

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

      我们的示例机器人是预定方法。当它们运行时，它们通过使用 SimpMessagingTemplate 的 WebSocket 发送我们的 OutputMessage POJOs。

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

      我们用@Scheduled(fixedRate = 5000)来注解 sendMessage 方法。这使得 sendMessage 每五秒运行一次。然后，我们使用 simpMessagingTemplate 实例向主题发送一个 OutputMessage。simpMessagingTemplate 和 chuckNorris 实例被作为方法参数从 Spring 上下文中注入。

   3. 使用 Flux::interval()

      如果我们使用 WebFlux，我们可以使用 Flux::interval 操作。它将发布一个由选定的[Duration](https://www.baeldung.com/java-period-duration#duration-class)分隔的无限流 long items。

      现在，让我们将 Flux 用于我们之前的例子。我们的目标是每五秒钟发送一条来自 Chuck Norris 的报价。首先，我们需要实现 InitializingBean 接口，在应用程序启动时订阅 Flux：

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

      这里，我们使用构造函数注入来设置 simpMessagingTemplate 和 chuckNorris 实例。这一次，调度逻辑在 afterPropertiesSet()中，我们在实现 InitializingBean 时覆盖了这个方法。该方法将在服务启动后立即运行。

      [间隔](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#interval-java.time.Duration-)操作符每隔五秒就会发出一个 Long。然后，[map](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-)操作者丢弃该值，并将其替换为我们的消息。最后，我们[订阅](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#subscribe-java.util.function.Consumer-)Flux 以触发我们对每个消息的逻辑。

4. 总结

   在本教程中，我们看到实用类 SimpMessagingTemplate 使我们能够轻松地通过 WebSocket 推送服务器消息。此外，我们还看到了两种调度执行一段代码的方法。
