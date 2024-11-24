# [带有Spring Boot的Apache RocketMQ](https://www.baeldung.com/apache-rocketmq-spring-boot)

数据 Spring Boot

信息传递

1. 介绍

    在本教程中，我们将使用Spring Boot和Apache RocketMQ（一个开源分布式消息和流媒体数据平台）创建一个消息制作者和消费者。

2. 依赖性

    对于Maven项目，我们需要添加RocketMQ Spring Boot Starter依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.rocketmq</groupId>
        <artifactId>rocketmq-spring-boot-starter</artifactId>
        <version>2.0.4</version>
    </dependency>
    ```

3. 制作消息

    在我们的示例中，我们将创建一个基本消息生成程序，每当用户从购物车中添加或删除项目时，该生成程序都会发送事件。

    首先，让我们在应用程序中设置服务器位置和组名。属性：

    ```properties
    rocketmq.name-server=127.0.0.1:9876
    rocketmq.producer.group=cart-producer-group
    ```

    请注意，如果我们有多个名称服务器，我们可以像host:port;host:port一样列出它们。

    现在，为了保持简单，我们将创建一个CommandLineRunner应用程序，并在应用程序启动期间生成一些事件：

    ![CartEventProducer](src/main/java/com/baeldung/rocketmq/producer/CartEventProducer.java)

    CartItemEvent仅由两个属性组成——项目的ID和数量：

    [CartItemEvent](src/main/java/com/baeldung/rocketmq/event/CartItemEvent.java)

    在上述示例中，我们使用convertAndSend()方法，这是由AbstractMessageSendingTemplate抽象类定义的通用方法来发送我们的购物车事件。它需要两个参数：目的地，在我们的案例中，目的地是一个主题名称和一个消息有效负载。

4. 消费者信息

    使用RocketMQ消息就像创建一个注释为@RocketMQMessageListener的Spring组件并实现RocketMQListener接口一样简单：

    ![CartEventConsumer](src/main/java/com/baeldung/rocketmq/consumer/CartEventConsumer.java)

    我们需要为我们正在收听的每个消息主题创建一个单独的组件。在每个监听器中，我们通过@RocketMQMessageListener注释定义主题名称和消费者组名称。

5. 同步和非同步传输

    在前面的例子中，我们使用convertAndSend方法发送消息。不过，我们还有其他一些选择。

    例如，我们可以调用syncSend，这与convertAndSend不同，因为它返回SendResult对象。

    例如，它可用于验证我们的消息是否已成功发送或获取其ID：

    ```java
    public void run(String... args) throws Exception {
        SendResult addBikeResult = rocketMQTemplate.syncSend("cart-item-add-topic",
        new CartItemEvent("bike", 1));
        SendResult addComputerResult = rocketMQTemplate.syncSend("cart-item-add-topic",
        new CartItemEvent("computer", 2));
        SendResult removeBikeResult = rocketMQTemplate.syncSend("cart-item-removed-topic",
        new CartItemEvent("bike", 1));
    }
    ```

    与convertAndSend一样，此方法仅在发送过程完成后才会返回。

    在需要高可靠性的情况下，我们应该使用同步传输，例如重要通知或短信通知。

    另一方面，我们可能希望异步发送消息，并在发送完成后收到通知。

    我们可以使用asyncSend来完成此操作，它将SendCallback作为参数，并立即返回：

    ```java
    rocketMQTemplate.asyncSend("cart-item-add-topic", new CartItemEvent("bike", 1), new SendCallback() {
        @Override
        public void onSuccess(SendResult sendResult) {
            log.error("Successfully sent cart item");
        }
        @Override
        public void onException(Throwable throwable) {
            log.error("Exception during cart item sending", throwable);
        }
    });
    ```

    在需要高吞吐量的情况下，我们使用异步传输。

    最后，对于我们有非常高的吞吐量要求的场景，我们可以使用sendOneWay而不是asyncSend。sendOneWay与asyncSend不同，因为它不能保证消息被发送。

    单向传输也可用于普通可靠性情况，如收集日志。

6. 在Transaction中发送消息

    RocketMQ为我们提供了在Transaction中发送消息的能力。我们可以使用sendInTransaction()方法来完成它：

    ```java
    MessageBuilder.withPayload(new CartItemEvent("bike", 1)).build();
    rocketMQTemplate.sendMessageInTransaction("test-transaction", "topic-name", msg, null);
    ```

    此外，我们必须实现RocketMQLocalTransactionListener接口：

    ![TransactionListenerImpl](src/main/java/com/baeldung/rocketmq/transaction/TransactionListenerImpl.java)

    在sendMessageInTransaction()中，第一个参数是事务名称。它必须与@RocketMQTransactionListener的成员字段txProducerGroup相同。

7. 消息制作者配置

    我们还可以配置消息生成者本身的各个方面：

    - rocketmq.producer.send-message-timeout：消息发送超时（以毫秒为单位）——默认值为3000
    - rocketmq.producer.compress-message-body-threshold：超过该阈值，RocketMQ将压缩消息——默认值为1024。
    - rocketmq.producer.max-message-size：以字节为为限的最大消息大小——默认值为4096。
    - rocketmq.producer.retry-times-when-send-async-failed：发送失败之前，在异步模式下在内部执行的最大重试次数——默认值为2。
    - rocketmq.producer.retry-next-server：指示是否在内部发送失败时重试另一个经纪人——默认值为false。
    - rocketmq.producer.retry-times-when-send-failed：在发送失败之前，在异步模式下内部执行的最大重试次数——默认值为2。

8. 结论

    在本文中，我们学习了如何使用Apache RocketMQ和Spring Boot发送和消耗消息。
