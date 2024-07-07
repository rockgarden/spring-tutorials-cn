# Java WebSocket

## WebSocket的Java API指南

1. 概述

    WebSocket通过提供双向、全双工、实时的客户/服务器通信，为服务器和网络浏览器之间的高效通信限制提供了一种替代方案。服务器可以在任何时候向客户端发送数据。由于它在TCP上运行，它还提供了一个低延迟的低级通信，并减少了每个消息的开销。

    在这篇文章中，我们将通过创建一个类似于聊天的应用程序来看看WebSockets的Java API。

2. JSR 356

    [JSR 356](https://jcp.org/en/jsr/detail?id=356)或Java API for WebSocket规定了一个API，Java开发者可以用它来将WebSockets集成到他们的应用程序中--包括服务器端和Java客户端。

    这个Java API同时提供了服务器和客户端组件：

    - 服务器：javax.websocket.server包中的所有内容。
    - 客户端：javax.websocket包中的内容，它由客户端API和服务器和客户端的通用库组成。
3. 使用WebSocket建立一个聊天工具

    我们将建立一个非常简单的类似聊天的应用程序。任何用户都可以从任何浏览器打开聊天，输入自己的名字，登录到聊天中，并开始与连接到聊天的每个人进行交流。

    我们将首先在pom.xml文件中添加最新的依赖性：

    ```xml
    <dependency>
        <groupId>javax.websocket</groupId>
        <artifactId>javax.websocket-api</artifactId>
        <version>1.1</version>
    </dependency>
    ```

    为了将Java对象转换为其JSON表示，反之亦然，我们将使用Gson：

    ```xml
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.0</version>
    </dependency>
    ```

    1. 端点配置

        有两种配置端点的方法：基于注解和基于扩展。你可以扩展javax.websocket.Endpoint类，也可以使用专用的方法级注解。由于注解模式与编程模式相比，会带来更简洁的代码，因此注解已成为编码的常规选择。在这种情况下，WebSocket端点生命周期事件由以下注解处理：

        - @ServerEndpoint：如果用@ServerEndpoint装饰，容器就会确保该类作为监听特定URI空间的WebSocket服务器的可用性
        - @ClientEndpoint： 用此注解装饰的类被视为WebSocket客户端。
        - @OnOpen： 当一个新的WebSocket连接启动时，容器会调用一个带有@OnOpen的Java方法。
        - @OnMessage： 当消息被发送到端点时，带有@OnMessage注解的Java方法从WebSocket容器中接收信息。
        - @OnError： 当通信出现问题时，一个带有@OnError的方法被调用。
        - @OnClose： 用于装饰一个Java方法，该方法在WebSocket连接关闭时被容器调用

    2. 编写服务器端点

        我们通过用@ServerEndpoint进行注释来声明一个Java类WebSocket服务器端点。我们还指定了部署该端点的URI。URI是相对于服务器容器的根定义的，必须以正斜杠开头：

        ```java
        @ServerEndpoint(value = "/chat/{username}")
        public class ChatEndpoint {
            @OnOpen
            public void onOpen(Session session) throws IOException {
                // Get session and WebSocket connection
            }
            @OnMessage
            public void onMessage(Session session, Message message) throws IOException {
                // Handle new messages
            }
            @OnClose
            public void onClose(Session session) throws IOException {
                // WebSocket connection closes
            }
            @OnError
            public void onError(Session session, Throwable throwable) {
                // Do error handling here
            }
        }
        ```

        上面的代码是我们类似聊天的应用程序的服务器端点骨架。正如你所看到的，我们有4个注解映射到它们各自的方法。下面你可以看到这些方法的实现：

        ```java
        @ServerEndpoint(value="/chat/{username}")
        public class ChatEndpoint {
        
            private Session session;
            private static Set<ChatEndpoint> chatEndpoints 
            = new CopyOnWriteArraySet<>();
            private static HashMap<String, String> users = new HashMap<>();

            @OnOpen
            public void onOpen(
            Session session, 
            @PathParam("username") String username) throws IOException {
                this.session = session;
                chatEndpoints.add(this);
                users.put(session.getId(), username);
                Message message = new Message();
                message.setFrom(username);
                message.setContent("Connected!");
                broadcast(message);
            }

            @OnMessage
            public void onMessage(Session session, Message message) 
            throws IOException { 
                message.setFrom(users.get(session.getId()));
                broadcast(message);
            }

            @OnClose
            public void onClose(Session session) throws IOException {
                chatEndpoints.remove(this);
                Message message = new Message();
                message.setFrom(users.get(session.getId()));
                message.setContent("Disconnected!");
                broadcast(message);
            }

            @OnError
            public void onError(Session session, Throwable throwable) {}

            private static void broadcast(Message message) 
            throws IOException, EncodeException {
                chatEndpoints.forEach(endpoint -> {
                    synchronized (endpoint) {
                        try {
                            endpoint.session.getBasicRemote().
                            sendObject(message);
                        } catch (IOException | EncodeException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        ```

        当一个新用户登录时（@OnOpen）立即被映射到一个活跃用户的数据结构中。然后，创建一个消息，并使用广播方法发送到所有的端点。

        每当任何一个连接的用户发送新的消息（@OnMessage）时，也会使用这个方法--这是聊天的主要目的。

        如果在某些时候发生了错误，带有注解的@OnError方法会处理它。你可以用这个方法来记录错误的信息并清除端点。

        最后，当一个用户不再连接到聊天时，方法@OnClose会清除端点，并向所有用户广播一个用户已经断开连接。

4. 消息类型

    WebSocket规范支持两种线上数据格式--文本和二进制。该API支持这两种格式，增加了与Java对象和规范中定义的健康检查消息（ping-pong）一起工作的能力：

    - 文本： 任何文本数据（java.lang.String、基元primitives或其等效包装wrapper类）。
    - 二进制： 二进制数据（如音频、图像等），由java.nio.ByteBuffer或byte[]（byte array）表示
    - Java对象： 该API使您可以在代码中使用本地（Java对象）表示，并使用自定义转换器（编码器/解码器）将其转换为WebSocket协议允许的兼容线上格式（文本、二进制）。
    - Ping-Pong： javax.websocket.PongMessage是WebSocket对等体为响应健康检查（ping）请求而发送的确认。

    对于我们的应用程序，我们将使用Java对象。我们将创建用于编码和解码消息的类。

    1. 编码器

        编码器接收一个Java对象并产生适合作为消息传输的典型表示，如JSON、XML或二进制表示。编码器可以通过实现`Encoder.Text<T>`或`Encoder.Binary<T>`接口来使用。

        在下面的代码中，我们定义了要编码的Message类，在方法encode中我们使用Gson将Java对象编码为JSON：

        model/Message.java

        websocket/MessageEncoder.java

    2. 解码器

        解码器与编码器相反，用于将数据转换回一个Java对象。解码器可以使用`Decoder.Text<T>`或`Decoder.Binary<T>`接口来实现。

        正如我们在编码器中所看到的，解码方法是我们把发送到端点的消息中检索到的JSON，用Gson把它转化为一个叫做Message的Java类：

        websocket/MessageDecoder.java

    3. 在服务器端点中设置编码器和解码器

        让我们把所有的东西放在一起，在类的层面上添加为编码和解码数据而创建的类，注解@ServerEndpoint：

        ```java
        @ServerEndpoint( 
        value="/chat/{username}", 
        decoders = MessageDecoder.class, 
        encoders = MessageEncoder.class )
        ```

        每次消息被发送到端点时，它们将自动被转换为JSON或Java对象。

5. 总结

    在这篇文章中，我们看了什么是WebSockets的Java API，以及它如何帮助我们建立像这样的实时聊天应用程序。

    我们看到了创建端点的两种编程模式：注解和程序化。我们使用注解模式为我们的应用程序定义了一个端点，同时也定义了生命周期方法。

    此外，为了能够在服务器和客户端之间来回通信，我们看到我们需要编码器和解码器来将Java对象转换成JSON，反之亦然。

    JSR 356 API非常简单，基于注解的编程模型使构建WebSocket应用变得非常容易。

    要运行我们在示例中构建的应用程序，我们需要做的就是在网络服务器中部署war文件，然后进入URL：<http://localhost:8080/java-websocket/> 。

## Relevant articles

- [ ] [A Guide to the Java API for WebSocket](https://www.baeldung.com/java-websockets)

## Code

你可以在这里找到资源库的[链接](https://github.com/eugenp/tutorials/tree/master/java-websocket)。
