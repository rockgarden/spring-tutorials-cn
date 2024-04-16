# Spring Kafka： 在同一主题上配置多个监听器

1. 概述

    在本文中，我们将通过一个实际示例来学习如何为同一个 Kafka 主题配置多个监听器。

2. 项目设置

    让我们构建一个图书消费者服务，它可以监听图书馆中新到的图书，并将其用于不同的目的，如全文内容搜索、价格索引或用户通知。

    首先，让我们创建一个 Spring Boot 服务并使用 spring-kafka 依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    ```

    此外，让我们定义侦听器将使用的 BookEvent：

    main/.spring.kafka.multiplelisteners/BookEvent.java

3. 生产消息

    [Kafka 生产者](https://docs.confluent.io/platform/current/clients/producer.html#)对生态系统至关重要，因为生产者会向 Kafka 集群写入消息。考虑到这一点，首先，我们需要定义一个生产者，将消息写入主题，然后由消费者应用程序消费。

    按照我们的示例，让我们编写一个简单的 Kafka 生产者函数，将新的 BookEvent 对象写入 "books" 主题。

    ```java
    private static final String TOPIC = "books";
    @Autowired
    private KafkaTemplate<String, BookEvent> bookEventKafkaTemplate;
    public void sentBookEvent(BookEvent book){
        bookEventKafkaTemplate.send(TOPIC, UUID.randomUUID().toString(), book);
    }
    ```

4. 从多个监听器消费同一个 Kafka 主题

    Kafka 消费者是订阅一个或多个 Kafka 集群主题的客户端应用程序。稍后，我们将了解如何在同一主题上设置多个监听器。

    1. 消费者配置

        首先，要配置消费者，我们需要定义监听器所需的 ConcurrentKafkaListenerContainerFactory Bean。

        现在，让我们定义用于消费 BookEvent 对象的容器工厂：

        main/.spring.kafka.multiplelisteners/KafkaConsumerConfig.java

        接下来，我们将了解监听传入消息的不同策略。

    2. 同一消费者组中的多个监听器

        向同一消费者组添加多个监听器的策略之一是提高同一消费者组内的并发级别。 因此，我们可以在 @KafkaListener 注解中简单地指定这一点。

        为了解其工作原理，让我们为库定义一个通知监听器：

        ```java
        @KafkaListener(topics = "books", groupId = "book-notification-consumer", concurrency = "2")
        public void bookNotificationConsumer(BookEvent event) {
            logger.info("Books event received for notification => {}", event);
        }
        ```

        接下来，我们将看到发布三条消息后的控制台输出。此外，我们还将了解为什么消息只消耗一次：

        ```log
        Books event received for notification => BookEvent(title=book 1, description=description 1, price=1.0)
        Books event received for notification => BookEvent(title=book 2, description=description 2, price=2.0)
        Books event received for notification => BookEvent(title=book 3, description=description 3, price=3.0)
        ```

        出现这种情况的原因是，在内部，对于每个并发级别，Kafka 都会在同一消费者组中实例化一个新的监听器。此外，同一消费者组中所有监听器实例的作用范围都是在彼此间分发消息，以更快地完成工作并提高吞吐量。

    3. 具有不同消费者组的多个监听器

        如果我们需要多次消费相同的消息，并为每个监听器应用不同的处理逻辑，我们必须配置 @KafkaListener 以获得不同的组 ID。这样，Kafka 就会为每个监听器创建专用的消费者组，并将所有发布的消息推送到每个监听器。

        为了了解这一策略的实际效果，让我们定义一个监听器用于全文搜索索引，另一个负责价格索引。这两个监听器将监听同一个 "书籍 "主题：

        ```java
        @KafkaListener(topics = "books", groupId = "books-content-search")
        public void bookContentSearchConsumer(BookEvent event) {
            logger.info("Books event received for full-text search indexing => {}", event);
        }

        @KafkaListener(topics = "books", groupId = "books-price-index")
        public void bookPriceIndexerConsumer(BookEvent event) {
            logger.info("Books event received for price indexing => {}", event);
        }
        ```

        现在，让我们运行上面的代码并分析输出结果：

        ```java
        Books event received for price indexing => BookEvent(title=book 1, description=description 1, price=1.0)
        Books event received for full-text search indexing => BookEvent(title=book 1, description=description 1, price=1.0)
        Books event received for full-text search indexing => BookEvent(title=book 2, description=description 2, price=2.0)
        Books event received for price indexing => BookEvent(title=book 2, description=description 2, price=2.0)
        Books event received for full-text search indexing => BookEvent(title=book 3, description=description 3, price=3.0)
        Books event received for price indexing => BookEvent(title=book 3, description=description 3, price=3.0)
        ```

        正如我们所看到的，两个监听器都能接收每个 BookEvent，并能对所有传入信息应用独立的处理逻辑。

5. 何时使用不同的监听器策略

    正如我们已经了解到的，我们可以通过配置 @KafkaListener 注解的并发属性，使其值大于 1，或者通过定义多个 @KafkaListener 方法来设置多个监听器，这些方法监听同一个 Kafka 主题，并分配不同的消费者 ID。

    选择哪种策略取决于我们想要实现的目标。只要我们解决了性能问题，通过更快地处理消息来提高吞吐量，正确的策略就是增加同一消费者组中的监听器数量。

    但是，如果要多次处理同一条信息以满足不同的要求，我们就应该定义具有不同消费者组的专用监听器来监听同一主题。

    根据经验，我们应该为每个需要满足的要求使用一个消费者组，如果我们需要使该监听器更快，可以增加同一消费者组内的监听器数量。

6. 总结

    在本文中，我们学习了如何使用 Spring Kafka 库为同一主题配置多个监听器，并以图书库为例进行了实际讲解。我们从 "生产者" 和 "消费者" 的配置开始，接着介绍了为同一主题添加多个监听器的不同方法。
