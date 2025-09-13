# [使用 Spring 的 WebSockets 介绍](https://www.baeldung.com/websockets-spring)

1. 概述

   在本教程中，我们将创建一个简单的 Web 应用程序，使用 Spring Framework 4.0 引入的新的 WebSocket 功能实现消息传递。

   WebSocket 是 Web 浏览器和服务器之间的一种双向、全双工、持久的连接。一旦建立了 WebSocket 连接，该连接就会保持开放，直到客户端或服务器决定关闭该连接。

   一个典型的用例可能是，当一个应用程序涉及多个用户相互沟通时，例如在聊天中。在我们的例子中，我们将建立一个简单的聊天客户端。

2. Maven 依赖性

   由于这是一个基于 Maven 的项目，我们首先在 pom.xml 中添加必要的依赖项：

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

   此外，我们需要添加 Jackson 的依赖项，因为我们将使用 JSON 来构建消息的主体。

   这允许 Spring 将我们的 Java 对象转换 to/from JSON。

3. 在 Spring 中启用 WebSocket

   首先，我们启用 WebSocket 功能。为了做到这一点，我们需要为我们的应用程序添加一个配置，并用@EnableWebSocketMessageBroker 来注释这个类。

   顾名思义，它可以在消息代理的支持下启用 WebSocket 消息处理：

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

   在这里，我们可以看到 configureMessageBroker 方法是用来配置消息代理的。

   首先，我们启用一个内存中的消息代理，在以"/topic"为前缀的目的地上将消息传回给客户端。

   我们通过指定"/app"前缀来完成我们的简单配置，以过滤针对应用程序注释的方法的目的地（通过 @MessageMapping ）。

   registerStompEndpoints 方法注册了"/chat"端点，启用 Spring 的[STOMP](http://stomp.github.io/stomp-specification-1.2.html#Abstract)支持。请记住，我们在这里也添加了一个端点，为了弹性起见，它在没有 SockJS 的情况下工作。

   这个端点，如果前缀为"/app"，就是 ChatController.send()方法被映射为处理的端点。

   它还启用了[SockJS](https://github.com/sockjs/sockjs-protocol)回退选项，以便在 WebSocket 不可用的情况下可以使用其他的消息传递选项。这很有用，因为 WebSocket 还不被所有浏览器支持，而且可能被限制性的网络代理排除在外。

   回退选项让应用程序使用 WebSocket API，但在运行时，必要时可以优雅地降级为非 WebSocket 替代方案。

4. 创建消息模型

   现在我们已经建立了项目并配置了 WebSocket 功能，我们需要创建一个消息来发送。

   该端点将接受包含发件人姓名和文本的 STOMP 消息，其主体是一个 JSON 对象。

   该消息可能看起来像这样：

   ```json
   {
     "from": "John",
     "text": "Hello!"
   }
   ```

   为了对携带文本的消息进行建模，我们可以创建一个简单的具有 from 和 text 属性的 Java 对象：

   websockets/Message.java

   默认情况下，Spring 将使用 Jackson 库将我们的模型对象转换为 JSON。

5. 创建一个消息处理控制器

   正如我们所看到的，Spring 处理 STOMP 消息的方法是将一个控制器方法与配置的端点相关联。我们可以通过@MessageMapping 注解做到这一点。

   端点和控制器之间的关联使我们有能力在需要时处理消息：

   ```java
   @MessageMapping("/chat")
   @SendTo("/topic/messages")
   public OutputMessage send(Message message) throws Exception {
       String time = new SimpleDateFormat("HH:mm").format(new Date());
       return new OutputMessage(message.getFrom(), message.getText(), time);
   }
   ```

   在我们的例子中，我们将创建另一个名为 OutputMessage 的模型对象来表示发送到配置的目的地的输出消息。我们用发件人和从传入消息中提取的消息文本来填充我们的对象，并用时间戳来充实它。

   在处理完我们的消息后，我们把它发送到用@SendTo 注解定义的适当目的地。所有在"/topic/messages"目的地的订阅者都将收到该消息。

6. 创建一个浏览器客户端

   在服务器端进行配置后，我们将使用[sockjs-client](https://github.com/sockjs/sockjs-client)库来建立一个简单的 HTML 页面，与我们的信息传递系统进行交互。

   首先，我们需要导入 sockjs 和 stomp JavaScript 客户端库。

   接下来，我们可以创建一个 connect()函数来打开与我们端点的通信，一个 sendMessage()函数来发送我们的 STOMP 消息，一个 disconnect()函数来关闭通信：

   参见：webapp/index.html

7. 测试该例子

   为了测试我们的例子，我们可以打开几个浏览器窗口，访问聊天页面：

   <http://localhost:8080>

   一旦完成，我们就可以通过输入一个昵称并点击连接按钮加入聊天。如果我们编写并发送一条消息，我们可以在所有加入聊天的浏览器会话中看到它。

8. 总结

    在这篇文章中，我们探讨了 Spring 的 WebSocket 支持。我们看到了它的服务器端配置，并使用 sockjs 和 stomp JavaScript 库构建了一个简单的客户端对应。
