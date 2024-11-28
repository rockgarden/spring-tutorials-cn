#[ Netty简介](https://www.baeldung.com/netty)

Java+

Java NIO软件  网状的  参考

1. 介绍

    在本文中，我们将看看Netty——一个异步事件驱动的网络应用程序框架。

    Netty的主要目的是基于NIO（或可能是NIO.2）构建高性能协议服务器，并对网络和业务逻辑组件进行分离和松散耦合。它可能实现一个广为人知的协议，如HTTP，或您自己的特定协议。

2. 核心概念

    Netty是一个非阻塞框架。与阻塞IO相比，这导致了高吞吐量。了解非阻塞IO对于了解Netty的核心组件及其关系至关重要。

    1. Channel

        Channel是Java NIO的基础。它代表一个开放的连接，能够进行读取和写入等IO操作。

    2. 未来

        Netty中通道上的每个IO操作都是非阻塞的。

        这意味着每个操作都会在调用后立即返回。标准Java库中有一个Future接口，但它对Netty目的来说不方便——我们只能询问Future操作的完成情况，或者在操作完成之前阻止当前线程。

        这就是为什么Netty有自己的ChannelFuture界面。我们可以将回调传递给ChannelFuture，该回调将在操作完成后调用。

    3. 事件和处理程序

        Netty使用事件驱动的应用程序范式，因此数据处理的管道是经过处理程序的事件链。事件和处理程序可以与入站和出站数据流相关。入站事件可以是以下：

        - 通道激活和停用
        - 读取操作事件
        - 异常事件
        - 用户事件

        出站事件更简单，通常与打开/关闭(opening/closing)连接和写入/冲洗(writing/flushing)数据有关。

        Netty应用程序由几个网络和应用程序逻辑事件及其处理程序组成。通道事件处理程序的基础接口是ChannelHandler及其继任者ChannelOutboundHandler和ChannelInboundHandler。

        Netty提供了ChannelHandler的大量实现层次结构。值得注意的是，适配器只是空的实现，例如ChannelInboundHandler适配器和ChannelOutboundHandler适配器。当我们只需要处理所有事件的子集时，我们可以扩展这些适配器。

        此外，还有许多特定协议的实现，如HTTP，例如HttpRequestDecoder、HttpResponseEncoder、HttpObjectAggregator。在Netty的Javadoc中认识他们会很好。

    4. 编码器和解码器

        当我们使用网络协议时，我们需要执行数据序列化和反序列化。为此，Netty为能够解码传入数据的解码器引入了ChannelInboundHandler的特殊扩展。大多数解码器的基础类是ByteToMessageDecoder。

        对于编码出站数据，Netty有名为encoders的ChannelOutboundHandler扩展。MessageToByteEncoder是大多数编码器实现的基础。我们可以使用编码器和解码器将消息从字节序列转换为Java对象，反之亦然。

3. 服务器应用程序示例

    让我们创建一个项目，代表一个简单的协议服务器，该服务器接收请求、执行计算并发送响应。

    1. 依赖性

        首先，我们需要在pom.xml中提供Netty依赖项：

        ```xml
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
            <version>4.1.10.Final</version>
        </dependency>
        ```

    2. 数据模型

        请求数据类将具有以下结构：

        ```java
        RequestData {
            private int intValue;
            private String stringValue;
            
            // standard getters and setters
        }
        ```

        让我们假设服务器收到请求并返回intValue乘以2。响应将具有单个int值：

        ```java
        public class ResponseData {
            private int intValue;

            // standard getters and setters
        }
        ```

    3. 请求解码器

        现在我们需要为我们的协议消息创建编码器和解码器。

        应该注意的是，Netty与套接字接收缓冲区一起工作，它不是表示为队列，而只是表示为一束字节。这意味着，当服务器没有收到完整消息时，可以调用我们的入站处理程序。

        在处理之前，我们必须确保我们已收到完整的消息，有很多方法可以做到这一点。

        首先，我们可以创建一个临时的ByteBuf，并附加所有入站字节，直到我们获得所需的字节数量：

        ![SimpleProcessingHandler](src/main/java/com/baeldung/netty/SimpleProcessingHandler.java)

        上面显示的示例看起来有点奇怪，但有助于我们了解Netty的工作原理。当相应的事件发生时，我们的处理程序的每个方法都会被调用。因此，我们在添加处理程序时初始化缓冲区，在接收新字节时填充数据，并在获得足够的数据时开始处理。

        我们故意不使用字符串值——以这种方式解码将变得不必要地复杂。这就是为什么Netty提供有用的解码器类，这些类是ChannelInboundHandler:ByteToMessageDecoder和ReplayingDecoder的实现。

        正如我们上面所指出的，我们可以使用Netty创建一个通道处理管道。因此，我们可以将解码器作为第一个处理程序，处理逻辑处理程序可以在它之后出现。

        如下所示的是RequestData的解码器：

        ![RequestDecoder](src/main/java/com/baeldung/netty/RequestDecoder.java)

        这个解码器的想法非常简单。它使用ByteBuf的实现，当缓冲区中没有足够的数据进行读取操作时，它会抛出异常。

        当异常被捕获时，缓冲区被倒回开头，解码器等待新的数据部分。当解码执行后out列表不是空时，解码停止。

    4. 响应编码器

        除了解码RequestData外，我们还需要对消息进行编码。此操作更简单，因为当写入操作发生时，我们拥有完整的消息数据。

        我们可以在主处理程序中将数据写入通道，或者我们可以分离逻辑并创建一个处理程序endingMessageToByteEncoder，该处理程序将捕获写入响应数据操作：

        ![ResponseDataEncoder](src/main/java/com/baeldung/netty/ResponseDataDecoder.java)

    5. 请求处理

        由于我们在单独的处理程序中进行了解码和编码，我们需要更改我们的ProcessingHandler：

        ![ProcessingHandler](src/main/java/com/baeldung/netty/ProcessingHandler.java)

    6. 服务器引导

        现在让我们把它全部放在一起，然后运行我们的服务器：

        ![NettyServer](src/main/java/com/baeldung/netty/NettyServer.java)

        上述服务器引导示例中使用的类的详细信息可以在他们的Javadoc中找到。最有趣的部分是这行：

        ```java
        ch.pipeline().addLast(
        new RequestDecoder(), 
        new ResponseDataEncoder(), 
        new ProcessingHandler());
        ```

        在这里，我们定义了入站和出站处理程序，这些处理程序将按照正确的顺序处理请求和输出。

4. 客户端应用程序

    客户端应该执行反向编码和解码，因此我们需要一个RequestDataEncoder和ResponseDataDecoder：

    ![RequestDataEncoder](src/main/java/com/baeldung/netty/ResponseDataEncoder.java)

    此外，我们需要定义一个客户端处理程序，该处理程序将发送请求并接收来自服务器的响应：

    ![ClientHandler](src/main/java/com/baeldung/netty/ClientHandler.java)

    现在让我们引导客户端：

    ![NettyClient](src/main/java/com/baeldung/netty/NettyClient.java)

    正如我们所看到的，与服务器引导有许多共同的细节。

    现在我们可以运行客户端的主要方法，并查看控制台输出。不出所料，我们得到了响应数据，intValue等于246。

5. 结论

    在这篇文章中，我们简要介绍了Netty。我们展示了其核心组件，如Channel和ChannelHandler。此外，我们为它制作了一个简单的非阻塞协议服务器和客户端。
