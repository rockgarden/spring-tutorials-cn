# [Netty中的异常情况](https://www.baeldung.com/netty-exception-handling)

例外  网状的

公告-图标

1. 一览表

    在这篇简短的文章中，我们将研究Netty中的异常处理。

    简单地说，Netty是一个用于构建高性能异步和事件驱动网络应用程序的框架。I/O操作使用回调方法在其生命周期内处理。

2. 在 Netty 中处理异常

    如前所述， Netty 是一个事件驱动的系统，并有针对特定事件的回调方法。异常也是此类事件。

    在处理从客户端接收到的数据或进行 I/O 操作时，可能会出现异常。发生这种情况时，会触发一个专门的异常捕获事件。

    1. 处理通道中的异常

        异常捕获事件在触发时由ChannelInboundHandler或其适配器和子类的exceptionsCaught（）方法处理。

        请注意，回调已在ChannelHandler接口中被弃用。它现在仅限于ChannelInboudHandler接口。

        该方法接受可抛对象和ChannelHandlerContext对象作为参数。Throwable对象可用于打印堆栈跟踪或获取本地化错误消息。

        因此，让我们创建一个通道处理程序ChannelHandlerA，并用我们的实现覆盖其exceptionCaught（）：

        ```java
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
            logger.info(cause.getLocalizedMessage());
            //do more exception handling
            ctx.close();
        }
        ```

        在上面的代码片段中，我们记录了异常消息，还调用了theChannelHandlerContext的close（）。

        这将关闭服务器和客户端之间的通道。基本上导致客户端断开连接并终止。

    2. 传播异常

        在上一节中，我们在源通道中处理了异常。不过，我们实际上可以将异常传播到管道中的另一个通道处理程序。

        我们将使用ChannelHandlerContext对象手动触发另一个异常捕获事件，而不是记录错误信息并调用ctx.close()。

        这将导致调用管道中下一个通道处理程序的exceptionCaught()。

        让我们修改ChannelHandlerA中的代码片段，通过调用ctx.fireExceptionCaught() 来传播该事件：

        ```java
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
            logger.info("Exception Occurred in ChannelHandler A");
            ctx.fireExceptionCaught(cause);
        }
        ```

        此外，让我们创建另一个通道处理程序ChannelHandlerB，并使用此实现覆盖其exceptionCaught（）：

        ```java
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
            logger.info("Exception Handled in ChannelHandler B");
            logger.info(cause.getLocalizedMessage());
            //do more exception handling
            ctx.close();
        }
        ```

        在服务器类中，通道按以下顺序添加到管道中：

        `ch.pipeline().addLast(new ChannelHandlerA(), new ChannelHandlerB());`

        在所有异常由一个指定的通道处理程序处理的情况下，手动传播异常捕获事件很有用。

3. 结论

    在本教程中，我们研究了如何使用回调方法在Netty中处理异常，以及如何在需要时传播异常。
