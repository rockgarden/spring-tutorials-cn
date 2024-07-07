# gRPC

## gRPC简介

1. 简介

    [gRPC](http://www.grpc.io/)是一个高性能的、开源的RPC框架，最初由谷歌开发。它有助于消除模板代码，并在数据中心内和跨数据中心连接多语言服务。

2. 概述

    该框架是基于远程过程调用的客户-服务器模型。客户端应用程序可以直接调用服务器应用程序上的方法，就像它是一个本地对象一样。

    在本教程中，我们将使用以下步骤，使用gRPC创建一个典型的客户-服务器应用程序：

    - 在一个.proto文件中定义一个服务
    - 使用协议缓冲区编译器生成服务器和客户端代码
    - 创建服务器应用程序，实现生成的服务接口并生成gRPC服务器
    - 创建客户端应用程序，使用生成的存根(stubs)进行RPC调用
    让我们定义一个简单的HelloService，返回问候语以换取名字和姓氏。

3. Maven依赖项

    我们将添加grpc-netty、grpc-protobuf和grpc-stub依赖项：

    ```xml
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-netty</artifactId>
        <version>1.55.1</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-protobuf</artifactId>
        <version>1.55.1</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-stub</artifactId>
        <version>1.55.1</version>
    </dependency>
    ```

4. 定义服务

    我们将首先定义一个服务，指定可以被远程调用的方法，以及它们的参数和返回类型。

    这是在.proto文件中使用[协议缓冲区](https://www.baeldung.com/google-protocol-buffer)完成的。它们也被用来描述有效载荷消息的结构。

    1. 基本配置

        让我们为我们的样本HelloService创建一个HelloService.proto文件。我们首先添加一些基本的配置细节：

        ```proto
        syntax = "proto3";
        option java_multiple_files = true;
        package org.baeldung.grpc;
        ```

        第一行告诉编译器这个文件使用哪种语法。默认情况下，编译器会将所有的Java代码生成一个单一的Java文件。第二行覆盖了这一设置，意味着所有的东西都将在单个文件中生成。

        最后，我们将指定我们要为我们生成的Java类使用的包。

    2. 定义消息结构

        接下来，我们将定义消息：

        ```proto
        message HelloRequest {
            string firstName = 1;
            string lastName = 2;
        }
        ```

        这定义了请求的有效载荷。在这里，定义了进入消息的每个属性以及其类型。

        需要为每个属性分配一个唯一的数字，称为标签。协议缓冲区使用这个标签来表示属性，而不是使用属性名称。

        因此，与JSON不同，我们每次都会传递属性名称firstName，协议缓冲区将使用数字1来表示firstName。响应有效载荷的定义与请求类似。

        注意，我们可以在多种消息类型中使用同一个标签：

        ```proto
        message HelloResponse {
            string greeting = 1;
        }
        ```

    3. 定义服务合同

        最后，我们来定义服务合同。对于我们的HelloService，我们将定义一个hello()操作：

        ```proto
        service HelloService {
            rpc hello(HelloRequest) returns (HelloResponse);
        }
        ```

        hello()操作接受一个单数请求，并返回一个单数响应。gRPC还支持流式传输，在请求和响应前加一个stream关键字。

5. 生成代码

    现在我们将HelloService.proto文件传递给协议缓冲区编译器protoc，以生成Java文件。有多种方法可以触发这一点。

    1. 使用协议缓冲区编译器

        首先，我们需要协议缓冲区编译器。我们可以从这里提供的许多预编译的二进制文件中选择。

        此外，我们还需要获得[gRPC Java Codegen插件](https://github.com/grpc/grpc-java/tree/master/compiler)。

        最后，我们可以使用以下命令来生成代码：

        ```bash
        protoc --plugin=protoc-gen-grpc-java=$PATH_TO_PLUGIN -I=$SRC_DIR 
        --java_out=$DST_DIR --grpc-java_out=$DST_DIR $SRC_DIR/HelloService.proto
        ```

    2. 使用Maven插件

        作为开发者，我们希望代码生成能与我们的构建系统紧密结合。gRPC为Maven构建系统提供了一个[protobuf-maven-plugin](https://search.maven.org/classic/#search%7Cga%7C1%7Cg%3A%22org.xolstice.maven.plugins%22%20AND%20a%3A%22protobuf-maven-plugin%22)：

        ```xml
        <build>
        <extensions>
            <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.6.1</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>
                com.google.protobuf:protoc:3.3.0:exe:${os.detected.classifier}
                </protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>
                io.grpc:protoc-gen-grpc-java:1.4.0:exe:${os.detected.classifier}
                </pluginArtifact>
            </configuration>
            <executions>
                <execution>
                <goals>
                    <goal>compile</goal>
                    <goal>compile-custom</goal>
                </goals>
                </execution>
            </executions>
            </plugin>
        </plugins>
        </build>
        ```

        [os-maven-plugin](https://search.maven.org/classic/#search%7Cgav%7C1%7Cg%3A%22kr.motd.maven%22%20AND%20a%3A%22os-maven-plugin%22) extension/plugin 会生成各种有用的、与平台相关的项目属性，如${os.detected.classifier}。

6. 创建服务器

    无论我们使用哪种方法生成代码，都会生成以下关键文件：

    - HelloRequest.java - 包含HelloRequest的类型定义
    - HelloResponse.java - 包含HelleResponse类型定义
    - HelloServiceImplBase.java - 这包含抽象类HelloServiceImplBase，它提供了我们在服务接口中定义的所有操作的实现。

    1. 重写服务基类

        抽象类HelloServiceImplBase的默认实现是抛出运行时异常io.grpc.StatusRuntimeException，它表示该方法未被实现。

        我们将扩展这个类，并覆盖我们的服务定义中提到的hello()方法：

        grpc.server/HelloServiceImpl.java

        如果我们将hello()的签名与我们在HellService.proto文件中写的签名进行比较，我们会发现它并没有返回HelloResponse。相反，它的第二个参数是`StreamObserver<HelloResponse>`，它是一个响应观察器，是服务器用其响应进行的回调。

        这样，客户端就可以选择进行阻塞式调用或非阻塞式调用。

        gRPC使用构建器来创建对象。我们将使用HelloResponse.newBuilder()并设置问候语文本来创建一个HelloResponse对象。我们将把这个对象设置到responseObserver的onNext()方法中，将其发送给客户端。

        最后，我们需要调用onCompleted()来指定我们已经完成了对RPC的处理；否则，连接将被挂起，而客户端将只是等待更多的信息进来。

    2. 运行Grpc服务器

        接下来，我们需要启动gRPC服务器来监听传入的请求：

        grpc.server/GrpcServer.java

        这里，我们再次使用构建器在8080端口创建一个gRPC服务器，并添加我们定义的HelloServiceImpl服务。 start()将启动服务器。在我们的例子中，我们将调用 awaitTermination() 来保持服务器在前台运行，阻断提示。

7. 创建客户端

    gRPC提供了一个通道结构，它抽象出底层的细节，如连接、连接池、负载平衡等。

    我们将使用ManagedChannelBuilder创建一个通道。在这里，我们将指定服务器地址和端口。

    我们将使用纯文本，不做任何加密：

    grpc.client/GrpcClient.java

    然后我们需要创建一个存根，用它来对hello()进行实际的远程调用。存根是客户端与服务器交互的主要方式。当使用自动生成的存根时，存根类将有用于包装通道的构造函数。

    这里我们使用的是一个阻塞/同步存根，这样RPC调用就会等待服务器的响应，并将返回一个响应或引发一个异常。gRPC还提供了另外两种类型的存根，以促进非阻塞/异步调用。

    现在是进行hello()RPC调用的时候了。我们将传递HelloRequest。我们可以使用自动生成的设置器来设置HelloRequest对象的firstName和lastName属性。

    最后，服务器返回HelloResponse对象。

8. 总结

    在这篇文章中，我们学习了如何使用gRPC来简化两个服务之间的通信开发，重点是定义服务，并让gRPC处理所有的模板代码。

## Relevant Articles

- [x] [Introduction to gRPC](https://www.baeldung.com/grpc-introduction)
- [Streaming with gRPC in Java](https://www.baeldung.com/java-grpc-streaming)
- [Error Handling in gRPC](https://www.baeldung.com/grpcs-error-handling)

## Code

像往常一样，文章中包含的代码可以在[网上](https://github.com/eugenp/tutorials/tree/master/grpc)找到。
