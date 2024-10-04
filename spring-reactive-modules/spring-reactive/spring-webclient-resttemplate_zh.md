# [Spring WebClient vs. RestTemplate](https://www.baeldung.com/spring-webclient-resttemplate)

1. 概述

    在本教程中，我们将比较Spring的两种Web客户端实现--RestTemplate和新的Spring 5的反应式替代方案WebClient。

2. 阻塞式与非阻塞式客户端

    在Web应用程序中，对其他服务进行HTTP调用是一个常见的需求。因此，我们需要一个Web客户端工具。

    1. RestTemplate阻塞式客户端

        长期以来，Spring一直提供RestTemplate作为Web客户端的抽象。在引擎盖下，RestTemplate使用Java Servlet API，它是基于线程-每-请求模型的。

        这意味着线程会阻塞，直到网络客户端收到响应。阻塞代码的问题是由于每个线程都会消耗一定量的内存和CPU周期。

        让我们考虑有很多传入的请求，这些请求正在等待一些需要产生结果的缓慢服务。

        迟早有一天，等待结果的请求会堆积起来。因此，应用程序将创建许多线程，这将耗尽线程池或占用所有可用内存。由于频繁的CPU上下文（线程）切换，我们也会遇到性能下降的问题。

    2. WebClient非阻塞式客户端

        另一方面，WebClient使用Spring Reactive框架提供的异步、非阻塞解决方案。

        RestTemplate为每个事件（HTTP调用）使用调用者线程，而WebClient将为每个事件创建类似 "任务"的东西。在幕后，Reactive框架将对这些 "任务"进行排队，并在适当的响应可用时才执行。

        Reactive框架使用一个事件驱动的架构。它提供了通过[Reactive Streams API](https://www.baeldung.com/java-9-reactive-streams)组成异步逻辑的手段。因此，与同步/阻塞方法相比，反应式方法可以处理更多的逻辑，同时使用更少的线程和系统资源。

        WebClient是Spring WebFlux库的一部分。因此，我们也可以使用功能化、流畅的API与反应式类型（Mono和Flux）作为声明式的组合来编写客户端代码。

3. 比较实例

    为了证明这两种方法之间的差异，我们需要用许多并发的客户端请求来进行性能测试。

    在一定数量的并行客户端请求之后，我们会发现阻塞式方法的性能会明显下降。

    然而，反应式/非阻塞式方法应该提供恒定的性能，无论请求的数量如何。

    在这篇文章中，我们将实现两个REST端点，一个使用RestTemplate，另一个使用WebClient。它们的任务是调用另一个缓慢的REST网络服务，该服务返回一个推文列表。

    要开始，我们需要Spring Boot WebFlux启动器的依赖：`org.springframework.boo.spring-boot-starter-webflux`

    这里是我们的慢速服务REST端点。

    TweetsSlowServiceController.getAllTweets()

    1. 使用RestTemplate来调用慢速服务

        现在让我们实现另一个REST端点，它将通过Web客户端调用我们的慢速服务。

        首先，我们将使用RestTemplate。

        WebController.getTweetsBlocking()

        当我们调用这个端点时，由于RestTemplate的同步性，代码将阻塞，等待来自我们慢速服务的响应。这个方法中的其他代码只有在收到响应后才会被运行。

        运行 WebControllerIntegrationTest.whenEndpointWithBlockingClientIsCalled_thenThreeTweetsAreReceived() 将在日志中看到的内容。

        ```log
        Starting BLOCKING Controller!
        Tweet(text=RestTemplate rules, username=@user1)
        Tweet(text=WebClient is better, username=@user2)
        Tweet(text=OK, both are useful, username=@user1)
        Exiting BLOCKING Controller!
        ```

    2. 使用WebClient来调用一个慢速服务

        其次，让我们使用WebClient来调用慢速服务。

        WebController.getTweetsNonBlocking()

        在这种情况下，WebClient返回一个Flux发布器，方法的执行得到完成。一旦有了结果，发布者将开始向其订阅者发出推文。

        请注意，调用这个/tweets-non-blocking端点的客户端（在这种情况下，是一个web浏览器）也将被订阅到返回的Flux对象。

        运行 whenEndpointWithNonBlockingClientIsCalled_thenThreeTweetsAreReceived() 观察一下这次的日志。

        ```log
        Starting NON-BLOCKING Controller!
        Exiting NON-BLOCKING Controller!
        Tweet(text=RestTemplate rules, username=@user1)
        Tweet(text=WebClient is better, username=@user2)
        Tweet(text=OK, both are useful, username=@user1)
        ```

        请注意，这个端点方法在收到响应之前完成。

4. 总结

    在这篇文章中，我们探讨了在Spring中使用Web客户端的两种不同方式。

    RestTemplate使用Java Servlet API，因此是同步的和阻塞的。

    相反，WebClient是异步的，在等待响应回来时不会阻塞执行线程。只有当响应准备好时，才会产生通知。

    RestTemplate仍然会被使用。但在某些情况下，非阻塞式方法与阻塞式方法相比，使用的系统资源要少得多。所以，在这些情况下，WebClient是一个比较好的选择。

> FIXME: Run WebClientIntegrationTest observed an error
  Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class java.lang.Object and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS)
