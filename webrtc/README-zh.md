# WebRTC 指南

1. 概述

当两个浏览器需要通信时，它们之间通常需要一个服务器来协调通信，在它们之间传递信息。但是，中间有一个服务器会导致浏览器之间的通信延迟。

在本教程中，我们将了解 WebRTC，这是一个开源项目，可让浏览器和移动应用程序直接进行实时通信。然后，我们将编写一个简单的应用程序，在两个 HTML 客户端之间创建点对点连接以共享数据，从而了解 WebRTC 的实际应用。

我们将使用 HTML、JavaScript 和 WebSocket 库以及网络浏览器内置的 WebRTC 支持来构建客户端。此外，我们还将使用 Spring Boot 构建一个信号服务器，使用 WebSocket 作为通信协议。最后，我们将了解如何在此连接中添加视频和音频流。

2. WebRTC 的基础和概念

让我们看看在没有 WebRTC 的典型场景中，两个浏览器是如何通信的。

假设我们有两个浏览器，浏览器 1 需要向浏览器 2 发送一条信息。浏览器 1 首先将信息发送到服务器：

Webrtc 2 1
服务器收到信息后进行处理，找到浏览器 2 并将信息发送给它：

 

webrtc 2 2
由于服务器必须先处理信息，然后才能将其发送给浏览器 2，因此通信几乎是实时进行的。当然，我们希望它是实时的。

WebRTC 通过在两个浏览器之间创建一个直接通道来解决这个问题，而无需服务器：

webrtc 2 3
因此，从一个浏览器向另一个浏览器传递信息的时间大大缩短，因为信息现在可以直接从发送方到达接收方。此外，它还省去了服务器的繁重工作并节省了带宽，使相关客户端可以共享带宽。

3. 支持 WebRTC 和内置功能

Chrome、Firefox、Opera 和 Microsoft Edge 等主流浏览器以及 Android 和 iOS 等平台都支持 WebRTC。

WebRTC 无需在浏览器中安装任何外部插件，因为该解决方案与浏览器捆绑在一起。

此外，在涉及视频和音频传输的典型实时应用中，我们必须严重依赖 C++ 库，而且我们必须处理很多问题，包括

隐藏丢包
回声消除
带宽自适应
动态抖动缓冲
自动增益控制
降低和抑制噪音
图像 "净化
但 WebRTC 可以在引擎盖下处理所有这些问题，使客户端之间的实时通信变得更加简单。

4. 点对点连接

与客户端-服务器通信不同，在客户端-服务器通信中，服务器有一个已知地址，客户端已经知道要与之通信的服务器地址，而在点对点（P2P）连接中，没有任何一个对等方有另一个对等方的直接地址。

要建立点对点连接，有几个步骤可以让客户端

使自己可以进行通信
相互识别并共享网络相关信息
共享数据格式、模式和相关协议，并就其达成一致意见
共享数据
WebRTC 为执行这些步骤定义了一套应用程序接口和方法。

WebRTC 使用一种称为信令的机制来让客户端互相发现对方、共享网络细节，然后共享数据格式。

5. 信令

信令指的是网络发现、创建会话、管理会话和交换媒体能力元数据的过程。

这一点至关重要，因为客户端需要事先了解对方才能启动通信。

为了实现所有这些功能，WebRTC 没有指定信令标准，而是让开发人员自行实现。因此，这就为我们提供了灵活性，使我们可以在一系列采用任何技术和支持协议的设备上使用 WebRTC。

5.1. 构建信令服务器

对于信令服务器，我们将使用 Spring Boot 构建一个 WebSocket 服务器。我们可以从 Spring Initializr 生成的空 Spring Boot 项目开始。

要使用 WebSocket 实现，让我们在 pom.xml 中添加依赖关系：

<依赖关系
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
    <版本>2.4.0</版本
</ 依赖关系
复制
我们总能从 Maven Central 找到要使用的最新版本。

信令服务器的实现很简单--我们将创建一个端点，客户端应用程序可以用它来注册 WebSocket 连接。

要在 Spring Boot 中实现这一点，让我们编写一个 @Configuration 类，该类扩展了 WebSocketConfigurer 并覆盖了 registerWebSocketHandlers 方法：

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(), "/socket")
        

复制
请注意，我们已将 /socket 确定为我们将在下一步构建的客户端中注册的 URL。我们还传入了一个 SocketHandler 作为 addHandler 方法的参数--它实际上就是我们下一步要创建的消息处理程序。

5.2. 在信令服务器中创建消息处理程序

下一步是创建一个消息处理程序，用于处理从多个客户端接收到的 WebSocket 消息。

这对于帮助不同客户端交换元数据以建立直接 WebRTC 连接至关重要。

在这里，为了保持简单，当我们从一个客户端接收到消息时，我们将把它发送给除它自己之外的所有其他客户端。

为此，我们可以从 Spring WebSocket 库中扩展 TextWebSocketHandler，并覆盖 handleTextMessage 和 afterConnectionEstablished 方法：

@Component
public class SocketHandler extends TextWebSocketHandler {

    List<WebSocketSession>sessions = new CopyOnWriteArrayList<>()；

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
      抛出 InterruptedException、IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message)；
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session)；
    }
}
复制
正如我们在 afterConnectionEstablished 方法中看到的，我们将接收到的会话添加到会话列表中，以便跟踪所有客户端。

从 handleTextMessage 中可以看到，当我们收到来自任何客户端的消息时，我们会遍历列表中的所有客户端会话，并通过比较发送者的会话 ID 和列表中的会话，将消息发送给除发送者之外的所有其他客户端。

6. 交换元数据

在 P2P 连接中，客户端之间可能存在很大差异。例如，Android 上的 Chrome 浏览器可以连接到 Mac 上的 Mozilla。

因此，这些设备的媒体功能可能会有很大差异。因此，对等设备之间必须进行握手，以商定用于通信的媒体类型和编解码器。

在这一阶段，WebRTC 使用 SDP（会话描述协议）来商定客户端之间的元数据。

为此，发起对等体创建一个要约，其他对等体必须将其设置为远程描述符。此外，其他对等端也会生成一个应答，并被发起对等端接受为远程描述符。

这一过程完成后，连接就建立起来了。

7. 设置客户端

让我们创建 WebRTC 客户端，使其既能充当发起对等方，又能充当远程对等方。

首先，我们将创建一个名为 index.html 的 HTML 文件和一个名为 client.js 的 JavaScript 文件，index.html 将使用该文件。

为了连接到信令服务器，我们将创建一个 WebSocket 连接。假设我们构建的 Spring Boot 信号服务器运行在 http://localhost:8080 上，我们就可以创建连接：

var conn = new WebSocket('ws://localhost:8080/socket')；
复制
为了向信令服务器发送消息，我们将创建一个 send 方法，用于在接下来的步骤中传递消息：

function send(message) {
    conn.send(JSON.stringify(message))；
}
复制
8. 设置简单的 RTCD DataChannel

在 client.js 中设置客户端后，我们需要为 RTCPeerConnection 类创建一个对象：

配置 = null；
var peerConnection = new RTCPeerConnection(configuration)；
复制
在本例中，配置对象的目的是传入 STUN（Session Traversal Utilities for NAT）和 TURN（Traversal Using Relays around NAT）服务器以及本教程后半部分将要讨论的其他配置。在本例中，传递 null 即可。

现在，我们可以创建一个用于消息传递的 dataChannel：

var dataChannel = peerConnection.createDataChannel("dataChannel", { reliable: true })；
复制
随后，我们可以为数据通道上的各种事件创建监听器：

dataChannel.onerror = function(error) {
    console.log("Error:", error)；
};
dataChannel.onclose = function() {
    console.log("Data channel is closed")；
};
复制
9. 使用 ICE 建立连接

建立 WebRTC 连接的下一步涉及 ICE（交互式连接建立）和 SDP 协议，在这两个协议中，对等方的会话描述会被交换并被双方接受。

信令服务器用于在对等方之间发送这些信息。这涉及客户端通过信令服务器交换连接元数据的一系列步骤。

9.1. 创建要约

首先，我们创建一个要约，并将其设置为对等连接的本地描述。然后，我们将要约发送给对方：

peerConnection.createOffer(function(offer


复制
在这里，发送方法调用信令服务器来传递报价信息。

请注意，我们可以使用任何服务器端技术来实现发送方法的逻辑。

9.2. 处理 ICE 候选者

其次，我们需要处理 ICE 候选者。WebRTC 使用 ICE（交互式连接建立）协议来发现对等设备并建立连接。

当我们在 peerConnection 上设置本地描述时，会触发一个 icecandidate 事件。

该事件应将候选对象传送给远程对等设备，以便远程对等设备将其添加到远程候选对象集。

为此，我们要为 onicecandidate 事件创建一个监听器：

peerConnection.onicecandidate = function(event) {
    if (event.candidate) {
        发送（{
            event : "candidate"、
            data : event.candidate
        });
    }
};
复制
当所有候选对象都收集完毕后，icecandidate 事件会再次触发，候选对象字符串为空。

我们必须把这个候选对象也传递给远程对等节点。我们传递这个空候选字符串是为了确保远程对等节点知道所有的 icecandidate 对象都已收集完毕。

此外，我们还会再次触发同一事件，以表明 ICE 候选对象的收集工作已完成，并在事件中将候选对象的值设置为空。这无需传递给远程对等设备。

9.3. 接收候选 ICE

第三，我们需要处理对方发送的 ICE 候选对象。

远程对等体收到候选者后，应将其添加到自己的候选者池中：

peerConnection.addIceCandidate(new RTCIceCandidate(candidate))；
复制
9.4. 接收报价

之后，当另一个对等方收到要约时，必须将其设置为远程描述。此外，它还必须生成一个应答，并发送给发起应答的对等节点：

peerConnection.setRemoteDescription(new RTCSessionDescription(offer))；
peerConnection.createAnswer(function(answer) {
    peerConnection.setLocalDescription(answer)；
        发送（{
            event : "answer"、
            data : answer
        });
}, function(error) {
    // 在此处处理错误
});
复制
9.5. 接收应答

最后，发起对话方接收应答并将其设置为远程描述：

handleAnswer(answer){
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer))；
}
复制
这样，WebRTC 就建立了一个成功的连接。

现在，我们可以在两个对等设备之间直接发送和接收数据，而无需信令服务器。

10. 发送信息

建立连接后，我们就可以使用 dataChannel 的发送方法在对等设备之间发送消息了：

dataChannel.send("message")；
复制
同样，为了在另一个对等设备上接收信息，我们为 onmessage 事件创建一个监听器：

dataChannel.onmessage = function(event) {
    console.log("Message:", event.data)；
};
复制
要在数据通道上接收消息，我们还必须在 peerConnection 对象上添加一个回调：

peerConnection.ondatachannel = function (event) {
    dataChannel = event.channel；
};
复制
通过这一步，我们创建了一个功能完备的 WebRTC 数据通道。现在，我们可以在客户端之间发送和接收数据。此外，我们还可以添加视频和音频通道。

11. 添加视频和音频通道

当 WebRTC 建立了 P2P 连接后，我们就可以轻松地直接传输音频和视频流了。

11.1. 获取媒体流

首先，我们需要从浏览器获取媒体流。WebRTC 为此提供了一个 API：

constraints = {
    视频：true,音频：true
};
navigator.mediaDevices.getUserMedia(constraints).
  then(function(stream) { /* use the stream */ })
    .catch(function(err) { /* 处理错误 */ })；
复制
我们可以使用约束对象指定视频的帧频、宽度和高度。

如果是移动设备，约束对象还可以指定使用的摄像头：

var constraints = {
    视频 ： {
        frameRate : {
            ideal : 10、
            max : 15
        },
        width : 1280、
        height : 720、
        facingMode ： 用户
    }
};
复制
此外，如果我们想启用背部摄像头，也可以将 facingMode 的值设置为 "环境"，而不是 "用户"。

11.2. 发送流

其次，我们必须将流添加到 WebRTC 对等连接对象中：

peerConnection.addStream(stream)；
复制
将流添加到对等连接会触发已连接对等对象的 addstream 事件。

11.3. 接收数据流

第三，要在远程对等设备上接收流，我们可以创建一个监听器。

让我们将此流设置为 HTML 视频元素：

peerConnection.onaddstream = function(event) {
    videoElement.srcObject = event.stream；
};
复制
12. NAT 问题

在现实世界中，防火墙和 NAT（网络地址穿越）设备将我们的设备连接到公共互联网。


NAT 为设备提供一个 IP 地址，供其在本地网络内使用。因此，本地网络之外无法访问该地址。没有公共地址，对等方就无法与我们通信。

为了解决这个问题，WebRTC 使用了两种机制：

STUN
转
13. 使用 STUN

STUN 是解决这一问题的最简单方法。在向对等设备共享网络信息之前，客户端会向 STUN 服务器发出请求。STUN 服务器的职责是返回收到请求的 IP 地址。

因此，通过查询 STUN 服务器，我们可以获得自己面向公众的 IP 地址。然后，我们将此 IP 和端口信息共享给想要连接的对等设备。其他对等设备也可以这样做，共享它们面向公众的 IP 地址。

要使用 STUN 服务器，我们只需在创建 RTCPeerConnection 对象的配置对象中传递 URL 即可：

配置 = {
    "iceServers" : [ {
        "url" : "stun:stun2.1.google.com:19302"
    } ]
};
复制
14. 使用 TURN

相比之下，TURN 是 WebRTC 无法建立 P2P 连接时使用的后备机制。TURN 服务器的作用是在对等方之间直接转发数据。在这种情况下，实际数据流流经 TURN 服务器。在默认情况下，TURN 服务器也充当 STUN 服务器。

TURN 服务器是公开的，客户端即使在防火墙或代理服务器后面也能访问它们。

但是，使用 TURN 服务器并不是真正的 P2P 连接，因为存在一个中间服务器。

注意：TURN 是我们无法建立 P2P 连接时的最后手段。由于数据流要通过 TURN 服务器，因此需要大量带宽，在这种情况下我们并没有使用 P2P。

与 STUN 类似，我们可以在同一个配置对象中提供 TURN 服务器的 URL：

{
  iceServers': [
    {
      urls': "stun:stun.l.google.com:19302
    },
    {
      'urls': 'turn:10.158.29.39:3478?transport=udp',
      凭证': 'XXXXXXXXXXXXX'、
      用户名'：'XXXXXXXXXXXX
    },
    {
      'urls': 'turn:10.158.29.39:3478?transport=tcp',
      凭证'： 'XXXXXXXXXXXXX'、
      用户名'：'XXXXXXXXXXXX
    }
  ]
}
复制
15. 结束语

在本教程中，我们讨论了什么是 WebRTC 项目，并介绍了其基本概念。然后，我们构建了一个在两个 HTML 客户端之间共享数据的简单应用程序。

我们还讨论了创建和建立 WebRTC 连接的步骤。

此外，我们还研究了如何使用 STUN 和 TURN 服务器作为 WebRTC 出现故障时的备用机制。

## Relevant Articles

- [ ] [Guide to WebRTC](https://www.baeldung.com/webrtc)

## Code

您可以在 GitHub 上查看本文提供的[示例](https://github.com/eugenp/tutorials/tree/master/webrtc)。
