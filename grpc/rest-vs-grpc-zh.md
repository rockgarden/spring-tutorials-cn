# REST vs. gRPC

1. 概述

    在这篇文章中，我们将比较REST和gRPC这两种网络API的架构风格。

2. 什么是REST？

    REST（Representational State Transfer）是一种架构风格，为设计网络API提供了指导方针。

    它使用标准的HTTP 1.1方法，如GET、POST、PUT和DELETE来处理服务器端的资源。此外，[REST API提供了预定义的URL](https://www.baeldung.com/building-a-restful-web-service-with-spring-and-java-based-configuration)，客户端必须使用这些URL来与服务器连接。

3. 什么是gRPC？

    gRPC（远程过程调用）是谷歌使用[HTTP/2](https://www.baeldung.com/netty-http2)协议开发的一种开源数据交换技术。

    它使用协议缓冲区二进制格式（[Protobuf](https://www.baeldung.com/spring-rest-api-with-protocol-buffers#1-introduction-to-protocol-buffers)）进行数据交换。同时，这种架构风格执行了开发人员必须遵循的规则，以开发或消费网络API。

4. REST vs. gRPC

    1. 准则与规则

        REST是一套设计网络API的指南，没有强制执行任何东西。另一方面，gRPC通过定义一个客户端和服务器都必须遵守的.proto文件来执行规则，进行数据交换。

    2. 底层HTTP协议

        REST提供了一个建立在HTTP 1.1协议上的请求-响应的通信模型。因此，当多个请求到达服务器时，它必然会逐一处理。

        然而，gRPC在设计[依赖HTTP/2的网络API时](https://www.baeldung.com/java-9-http-client)，遵循的是客户-响应的通信模式。因此，gRPC允许流式通信并同时提供多个请求。除此之外，gRPC还支持类似于REST的单项通信。

    3. 数据交换格式

        REST通常使用[JSON](https://www.baeldung.com/java-json)和[XML](https://www.baeldung.com/jackson-xml-serialization-and-deserialization)格式进行数据传输。然而，gRPC依赖于Protobuf，通过HTTP/2协议进行数据交换。

    4. 序列化与强类型化

        在大多数情况下，REST使用JSON或XML，需要将其序列化并转换为客户端和服务器的目标编程语言，从而增加响应时间和解析请求/响应时出错的可能性。

        然而，gRPC提供了强类型的消息，使用Protobuf交换格式自动转换为所选择的编程语言。

    5. 延迟

        利用HTTP 1.1的REST需要对每个请求进行TCP握手。因此，使用HTTP 1.1的REST APIs可能会出现延迟问题。

        另一方面，gRPC依赖于HTTP/2协议，它使用复用流。因此，几个客户端可以同时发送多个请求，而无需为每个请求建立新的TCP连接。另外，服务器可以通过已建立的连接向客户端发送推送通知。

    6. 浏览器支持

        HTTP 1.1上的REST APIs有普遍的浏览器支持。

        然而，gRPC对浏览器的支持是有限的，因为众多浏览器（通常是旧版本）对HTTP/2没有成熟的支持。所以，它可能需要gRPC-web和代理层来进行HTTP 1.1和HTTP/2之间的转换。因此，目前，gRPC主要用于内部服务。

    7. 代码生成功能

        REST没有提供内置的代码生成功能。然而，我们可以使用第三方工具，如Swagger或Postman来生成API请求的代码。

        另一方面，[gRPC使用其protoc编译器](https://www.baeldung.com/grpc-introduction#1-using-protocol-buffer-compiler)，带有本地代码生成功能，与几种编程语言兼容。

5. 总结

    在这篇文章中，我们比较了两种API的架构风格，REST和gRPC。

    我们的结论是，REST在整合微服务和第三方应用与核心系统方面很方便。

    然而，gRPC可以在各种系统中找到它的应用，比如需要轻量级消息传输的物联网系统、没有浏览器支持的移动应用以及需要多路流的应用。

## 相关文章

- [x] [REST vs. gRPC](https://www.baeldung.com/rest-vs-grpc)
