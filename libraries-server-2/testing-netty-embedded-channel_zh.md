# 使用EmbeddedChannel测试Netty

测试

Netty

1. 介绍

    在本文中，我们将了解如何使用EmbeddedChannel来测试我们的入站和出站通道处理程序的功能。

    Netty是一个非常通用的框架，用于编写高性能异步应用程序。如果没有正确的工具，对此类应用程序进行单元测试可能很棘手。

    值得庆幸的是，该框架为我们提供了EmbeddedChannel类——这便于对ChannelHandlers的测试。

2. 设置

    EmbeddedChannel是Netty框架的一部分，因此唯一需要的依赖是Netty本身的依赖。

    ```xml
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.24.Final</version>
    </dependency>
    ```

3. 嵌入式频道概述

    EmbeddedChannelclass只是AbstractChannel的另一个实现——它无需真正的网络连接即可传输数据。

    这很有用，因为我们可以通过在入站通道上写入数据来模拟传入消息，也可以检查出站通道上生成的响应。通过这种方式，我们可以单独测试每个ChannelHandler或整个渠道管道。

    要测试一个或多个ChannelHandlers，我们首先必须使用其构造函数之一创建一个EmbeddedChannel实例。

    初始化EmbeddedChannel的最常见方法是将ChannelHandlers列表传递给其构造函数：

    ```java
    EmbeddedChannel channel = new EmbeddedChannel(
    new HttpMessageHandler(), new CalculatorOperationHandler());
    ```

    如果我们想对将处理程序插入管道的顺序进行更多控制，我们可以使用默认构造函数创建一个嵌入式通道，并直接添加处理程序：

    ```java
    channel.pipeline()
    .addFirst(new HttpMessageHandler())
    .addLast(new CalculatorOperationHandler());
    ```

    此外，当我们创建嵌入式通道时，它将具有由DefaultChannelConfig类给出的默认配置。

    当我们想要使用自定义配置时，例如从默认值降低连接超时值，我们可以使用config（）方法访问ChannelConfig对象：

    ```java
    DefaultChannelConfig channelConfig = (DefaultChannelConfig) channel.config();
    channelConfig.setConnectTimeoutMillis(500);
    ```

    嵌入式通道包括我们可以用来读取和写入数据到ChannelPipeline的方法。最常用的方法是：

    - readInbound()
    - readOutbound()
    - writeInbound(Object… msgs)
    - writeOutbound(Object… msgs)

    读取方法检索并删除入站/出站(inbound/outbound)队列中的第一个元素。当我们需要在不删除任何元素的情况下访问整个消息队列时，我们可以使用outboundMessages()方法：

    ```java
    Object lastOutboundMessage = channel.readOutbound();
    Queue<Object> allOutboundMessages = channel.outboundMessages();
    ```

    当消息成功添加到通道的inbound/outbound管道中时，写入方法返回true：

    `channel.writeInbound(httpRequest)`

    这个想法是，我们在入站管道上编写消息，以便出站ChannelHandlers处理它们，我们希望结果可以从出站管道中读取。

4. 测试渠道处理程序

    让我们看看一个简单的例子，在这个例子中，我们想测试一个由两个ChannelHandlers组成的管道，该管道接收HTTP请求，并期望包含计算结果的HTTP响应：

    ```java
    EmbeddedChannel channel = new EmbeddedChannel(
    new HttpMessageHandler(), new CalculatorOperationHandler());
    ```

    第一个，HttpMessageHandler将从HTTP请求中提取数据，并将其传递给管道中的secondsChannelHandler，CalculatorOperationHandler，以对数据进行处理。

    现在，让我们编写HTTP请求，看看入站管道是否处理它：

    ```java
    FullHttpRequest httpRequest = new DefaultFullHttpRequest(
    HttpVersion.HTTP_1_1, HttpMethod.GET, "/calculate?a=10&b=5");
    httpRequest.headers().add("Operator", "Add");

    assertThat(channel.writeInbound(httpRequest)).isTrue();
    long inboundChannelResponse = channel.readInbound();
    assertThat(inboundChannelResponse).isEqualTo(15);
    ```

    我们可以看到，我们已经使用writeInbound()方法在入站管道上发送了HTTP请求，并使用readInbound()读取结果；inboundChannelResponse是入站管道中的所有ChannelHandlers处理后我们发送的数据所产生的消息。

    现在，让我们检查一下我们的Netty服务器是否响应了正确的HTTP响应消息。为此，我们将检查出站管道上是否存在消息：

    `assertThat(channel.outboundMessages().size()).isEqualTo(1);`

    在这种情况下，出站消息是HTTP响应，因此让我们检查内容是否正确。我们通过阅读出站管道中的最后一条消息来做到这一点：

    ```java
    FullHttpResponse httpResponse = channel.readOutbound();
    String httpResponseContent = httpResponse.content()
    .toString(Charset.defaultCharset());
    assertThat(httpResponseContent).isEqualTo("15");
    ```

5. 测试异常处理

    另一个常见的测试场景是异常处理。

    我们可以通过实现exceptionCaught()方法在ChannelInboundHandlers中处理异常，但在某些情况下，我们不想处理异常，而是将其传递给管道中的下一个ChannelHandler。

    我们可以使用EmbeddedChannelclass中的checkException()方法来检查管道上是否收到任何Throwable对象并重新抛出它。

    通过这种方式，我们可以捕获异常，并检查ChannelHandler是否应该抛出它：

    ```java
    assertThatThrownBy(() -> {
        channel.pipeline().fireChannelRead(wrongHttpRequest);
        channel.checkException();
    }).isInstanceOf(UnsupportedOperationException.class)
    .hasMessage("HTTP method not supported");
    ```

    我们可以在上面的例子中看到，我们已经发送了一个HTTP请求，我们希望触发一个异常。通过使用checkException()方法，我们可以重新抛出管道中存在的最后一个异常，这样我们就可以从中断言需要什么。

6. 结论

    EmbeddedChannel是Netty框架提供的一个很好的功能，帮助我们测试outChannelHandler管道的正确性。它可用于单独测试每个ChannelHandler，更重要的是测试整个管道。
