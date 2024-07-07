# 谷歌协议缓冲器简介

1. 概述

    本文将介绍谷歌协议缓冲区（[protobuf](https://developers.google.com/protocol-buffers/)）--一种著名的与语言无关的二进制数据格式。我们可以用协议定义一个文件，然后使用该协议生成 Java、C++、C#、Go 或 Python 等语言的代码。

    [Google Protocol Buffer](https://github.com/protocolbuffers/protobuf) 是 Google 公司内部的混合语言数据标准，用于 RPC 系统和持续数据存储系统。

    Protocol Buffers 是一种轻便高效的结构化数据存储格式，可以用于结构化数据串行化，或者说序列化。它很适合做数据存储或 RPC 数据交换格式。可用于通讯协议、数据存储等领域的语言无关、平台无关、可扩展的序列化结构数据格式。

    本文只是介绍这种格式本身；如果你想了解如何在 Spring Web 应用程序中使用这种格式，请[参阅本文](https://www.baeldung.com/spring-rest-api-with-protocol-buffers)。

2. 定义 Maven 依赖项

    要在 Java 中使用协议缓冲区，我们需要在 protobuf-java 中添加 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>com.google.protobuf</groupId>
        <artifactId>protobuf-java</artifactId>
        <version>${protobuf.version}</version>
    </dependency>

    <properties>
        <protobuf.version>3.2.0</protobuf.version>
    </properties>
    ```

3. 定义协议

    让我们从一个例子开始。我们可以用 protobuf 格式定义一个非常简单的协议：

    ```protobuf
    message Person {
        required string name = 1;
    }
    ```

    这是一个 Person 类型的简单消息协议，它只有一个必填字段，即字符串类型的 name。

    让我们看看定义协议的更复杂示例。比方说，我们需要以 protobuf 格式存储个人详细信息：

    resources/addressbook.proto

    我们的协议包括两类数据：Person 和 AddressBook。生成代码后，这些类将成为 AddressBookProtos 类的内部类。

    当我们要定义一个必填字段时--这意味着创建一个没有该字段的对象将导致异常，我们需要使用必填关键字。

    使用可选关键字创建字段意味着不需要设置该字段。重复关键字是大小可变的数组类型。

    所有字段都有索引--标有数字 1 的字段将作为第一个字段保存在二进制文件中。标有 2 的字段将作为下一个字段保存，以此类推。这样我们就能更好地控制字段在内存中的布局。

4. 从 Protobuf 文件生成 Java 代码

    定义文件后，我们就可以从中生成代码了。

    首先，我们需要在机器上安装 [protobuf](https://github.com/google/protobuf/releases)。

    MacOS 安装

    ```bash
    brew search protobuf
    protoc --version
    libprotoc 3.21.12
    ```

    安装完成后，我们就可以执行 protoc 命令来生成代码了：

    `protoc -I=. --java_out=. addressbook.proto`

    protoc 命令将从 addressbook.proto 文件生成 Java 输出文件。-I选项指定了proto文件所在的目录。java-out 指定生成类的目录。

    生成的类将为我们定义的信息提供设置器、获取器、构造器和构建器。它还将包含一些 util 方法，用于保存 protobuf 文件并将其从二进制格式反序列化为 Java 类。

5. 创建 Protobuf 定义消息的实例

    我们可以使用生成的代码轻松创建一个 Person 类的 Java 实例：

    ```java
    String email = "j@baeldung.com";
    int id = new Random().nextInt();
    String name = "Michael Program";
    String number = "01234567890";
    AddressBookProtos.Person person =
    AddressBookProtos.Person.newBuilder()
        .setId(id)
        .setName(name)
        .setEmail(email)
        .addNumbers(number)
        .build();

    assertEquals(person.getEmail(), email);
    assertEquals(person.getId(), id);
    assertEquals(person.getName(), name);
    assertEquals(person.getNumbers(0), number);
    ```

    我们可以在所需的消息类型上使用 newBuilder() 方法创建流畅的生成器。设置完所有必填字段后，我们可以调用 build() 方法创建 Person 类的实例。

6. 序列化和反序列化 Protobuf

    一旦创建了 Person 类的实例，我们就想把它以与所创建协议兼容的二进制格式保存到磁盘上。比方说，我们想创建一个 AddressBook 类的实例，并在该对象中添加一个人。

    接下来，我们要将该文件保存到光盘上--在自动生成的代码中有一个 writeTo() util 方法可供使用：

    ```java
    AddressBookProtos.AddressBook addressBook 
    = AddressBookProtos.AddressBook.newBuilder().addPeople(person).build();
    FileOutputStream fos = new FileOutputStream(filePath);
    addressBook.writeTo(fos);
    ```

    执行该方法后，我们的对象将序列化为二进制格式并保存到光盘中。要从光盘加载数据并将其反序列化回 AddressBook 对象，我们可以使用 mergeFrom() 方法：

    ```java
    AddressBookProtos.AddressBook deserialized
    = AddressBookProtos.AddressBook.newBuilder()
        .mergeFrom(new FileInputStream(filePath)).build();
    
    assertEquals(deserialized.getPeople(0).getEmail(), email);
    assertEquals(deserialized.getPeople(0).getId(), id);
    assertEquals(deserialized.getPeople(0).getName(), name);
    assertEquals(deserialized.getPeople(0).getNumbers(0), number);
    ```

7. 结论

    在这篇短文中，我们介绍了一种以二进制格式描述和存储数据的标准--谷歌协议缓冲区。

    我们创建了一个简单的协议，并创建了符合定义协议的 Java 实例。接下来，我们了解了如何使用 protobuf 序列化和反序列化对象。

## Relevant articles

- [x] [Introduction to Google Protocol Buffer](https://www.baeldung.com/google-protocol-buffer)

## Code

所有这些示例和代码片段的实现都可以在[GitHub项目](https://github.com/eugenp/tutorials/tree/master/protobuffer)中找到 - 这是一个 Maven 项目，因此很容易导入和运行。
