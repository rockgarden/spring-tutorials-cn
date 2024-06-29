# Wire Tap Pattern

该应用向你展示了如何使用Wire Tap来监控、调试或排除流经系统的消息，而不需要永久地将其消耗掉，也不需要对输出通道中的预期消息做任何改变。

这个例子展示了如何使用Spring Boot和Apache ActiveMq用一个简单的Apache Camel应用程序来实现这一点。
为了方便起见，我们使用的是内存中的ActiveMq。

## 配置和使用连接工厂

1. 创建CamelContext。
2. 连接到嵌入式（或远程）ActiveMQ JMS代理。
3. 向CamelContext添加JMS队列。
4. 将src/data中的文件订单（xml/csv）加载到JMS队列中。
5. 根据传入的文件消息的扩展名，将其路由到相应的队列。
6. 测试目的地路由是否正常。
7. 审核从线程队列中收到的文件（顺序）。

## Wire Tap 企业集成模式

1. 概述

    在本教程中，我们将介绍Wire Tap企业集成模式（EIP），它可以帮助我们监控流经系统的消息。

    这种模式允许我们拦截消息，而不把它们永久性地消耗在通道之外。

2. Wire Tap模式

    Wire Tap检查在点对点通道上传播的消息。它接收消息，制作一个副本，并将其发送到 Tap 目的地：

    ![Wire tap EnterpriseIntegrationPattern](pic/Wire-tap-EnterpriseIntegrationPattern.png)

    为了更好地理解这一点，让我们用ActiveMQ和Camel创建一个Spring Boot应用程序。

3. Maven依赖性

    让我们添加camel-spring-boot-dependencies：

    ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.apache.camel.springboot</groupId>
                <artifactId>camel-spring-boot-dependencies</artifactId>
                <version>${camel.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

    现在，我们将添加 camel-spring-boot-starter：

    ```xml
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-spring-boot-starter</artifactId>
    </dependency>
    ```

    为了查看流经路由的消息，我们还需要包括ActiveMQ：

    ```xml
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-activemq-starter</artifactId>
    </dependency>
    ```

4. 消息交换

    让我们来创建一个消息对象：参见 MyPayload.java

    我们将向direct:source发送这个消息以启动路由：参见 AmqApplication.java main()

    接下来，我们将添加一个路由和点选目的地。

5. Tapping an Exchange

    我们将使用wireTap方法来设置Tap Destination的端点URI。Camel不等待wireTap的响应，因为它将消息交换模式设置为InOnly。wire Tap处理器在一个单独的线程上处理：

    `wireTap("direct:tap").delay(1000)`

    Camel的Wire Tap节点在Tapping an Exchange时支持两种flavors：

    1. Traditional Wire Tap

        让我们添加一个传统的Wire Tap路由：参见 AmqApplication.java traditionalWireTapRoute()

        在这里，Camel只会复制Exchange - 它不会做深度克隆。所有的副本都可以共享原始交易所的对象。

        在并发处理多个消息时，有可能会破坏最终的有效载荷。我们可以在把有效载荷传递给Tap Destination之前创建一个深度克隆来防止这种情况。

    2. 发送一个新的交换

        Wire Tap EIP 支持表达式或处理器，预先填入交换的副本。表达式只能用于设置消息正文。

        处理器的变体可以完全控制交换的填充方式（设置属性、头文件等）。

        让我们在payload中实现深度克隆：MyPayload.java deepClone()

        现在，让我们用原始交易所的副本作为输入来实现处理器类：MyPayloadClonePrepare.java

        我们将在wireTap之后使用onPrepare调用它：参见 AmqApplication.java newExchangeRoute()

6. 总结

    在这篇文章中，我们实现了一个Wire Tap模式来监控通过某些消息端点的消息。使用Apache Camel的wireTap，我们复制消息并将其发送到不同的端点而不改变现有的流程。

    Camel支持两种方式来窃听一个交换。在传统的Wire Tap中，原始交换被复制。在第二种情况下，我们可以创建一个新的交换。我们可以使用表达式用新的消息体值来填充这个新的交换，或者我们可以使用处理器来设置头信息--也可以选择设置消息体。

## 相关文章

- [Wire Tap企业集成模式](https://www.baeldung.com/wiretap-pattern)

## Code

代码样本可以在GitHub上找到。

1. 如何运行这个例子

    `mvn spring-boot:run`

    默认情况下，Wire Tap处理器会对Camel Exchange实例进行浅层拷贝。交换的副本被发送到wireTap语句中指定的端点。线路窃听消息的主体包含与原始消息中相同的对象，这意味着在线路窃听路线中对该对象的内部状态的任何改变也可能最终改变主消息的主体。

    为了解决这个问题，我们需要在把对象传递给线程窃听目的地之前，创建一个对象的深度拷贝。通过实现org.apache.camel.Processor类，Wire Tap EIP为我们提供了一种机制来执行消息的 "深度(deep)" 拷贝。这需要在wireTap之后立即使用onPrepare语句来调用。
    更多细节，请查看AmqApplicationUnitTest.class。
