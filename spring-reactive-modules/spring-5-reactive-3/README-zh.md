# Spring 5 Reactive Project

## Spring Boot FeignClient vs. WebClient

1. 概述

    在本教程中，我们将比较[Spring Feign](https://www.baeldung.com/spring-cloud-openfeign)（一种声明式REST客户端）和[Spring WebClient](https://www.baeldung.com/spring-5-webclient)（Spring 5中引入的一种反应式Web客户端）。

2. 阻塞式与非阻塞式客户端

    在当今的微服务生态系统中，通常要求后端服务使用HTTP调用其他Web服务。因此，Spring应用程序需要一个Web客户端来执行这些请求。

    接下来，我们将研究阻塞式Feign客户端和非阻塞式WebClient实现之间的区别。

    1. Spring Boot阻塞式Feign客户端

        Feign客户端是一个声明性的REST客户端，它使编写Web客户端更容易。使用Feign时，开发者只需定义接口并对其进行相应的注释。然后，实际的Web客户端实现由Spring在运行时提供。

        在幕后，用@FeignClient注释的接口会根据每个请求的线程模型生成一个同步的实现。因此，对于每个请求，指定的线程都会阻塞，直到它收到响应。保持多个线程存活的缺点是，每个开放的线程都会占用内存和CPU周期。

        接下来，让我们想象一下，我们的服务受到了流量高峰的冲击，每秒收到成千上万的请求。除此之外，每个请求都需要等待几秒钟，等待上游服务返回结果。

        根据分配给托管服务器的资源和流量高峰的长度，一段时间后，所有创建的线程将开始堆积并占据所有分配的资源。因此，这一连串的事件将降低服务的性能，最终使服务瘫痪。

    2. Spring Boot非阻塞式WebClient

        WebClient是[Spring WebFlux](https://www.baeldung.com/spring-webflux)库的一部分。它是Spring Reactive框架提供的一个非阻塞解决方案，用于解决Feign客户端等同步实现的性能瓶颈问题。

        Feign客户端为每个请求创建一个线程，并将其阻塞，直到收到响应为止，而WebClient则执行HTTP请求，并将一个 "waiting for response" 的任务加入队列。之后，"等待响应" 任务在收到响应后从队列中执行，最终将响应传递给订阅者函数。

        Reactive框架实现了一个由[Reactive Streams API](https://www.baeldung.com/java-9-reactive-streams)驱动的事件驱动架构。正如我们所看到的，这使我们能够编写服务，以最少的阻塞线程执行HTTP请求。

        因此，WebClient可以帮助我们建立在恶劣环境下性能稳定的服务，用较少的系统资源处理更多的请求。

3. 比较实例

    为了了解Feign客户端和WebClient之间的差异，我们将实现两个HTTP端点，它们都调用相同的慢速端点，返回一个产品列表。

    我们将看到，在阻塞式Feign实现的情况下，每个请求线程都会阻塞两秒钟，直到收到响应为止。

    另一方面，非阻塞式的WebClient将立即关闭请求线程。

    首先，我们需要添加三个依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    ```

    接下来，我们有慢速端点的定义：

    ```java
    @GetMapping("/slow-service-products")
    private List<Product> getAllProducts() throws InterruptedException {
        Thread.sleep(2000L); // delay
        return Arrays.asList(
        new Product("Fancy Smartphone", "A stylish phone you need"),
        new Product("Cool Watch", "The only device you need"),
        new Product("Smart TV", "Cristal clean images")
        );
    }
    ```

    1. 使用Feign来调用慢速服务

        现在，让我们开始使用Feign来实现第一个端点。

        第一步是定义接口，并用@FeignCleint来注解：

        ```java
        @FeignClient(value = "productsBlocking", url = "http://localhost:8080")
        public interface ProductsFeignClient {
            @RequestMapping(method = RequestMethod.GET, value = "/slow-service-products", produces = "application/json")
            List<Product> getProductsBlocking(URI baseUrl);
        }
        ```

        最后，我们将使用定义的ProductsFeignClient接口来调用慢速服务：

        ```java
        @GetMapping("/products-blocking")
        public List<Product> getProductsBlocking() {
            log.info("Starting BLOCKING Controller!");
            final URI uri = URI.create(getSlowServiceBaseUri());
            List<Product> result = productsFeignClient.getProductsBlocking(uri);
            result.forEach(product -> log.info(product.toString()));
            log.info("Exiting BLOCKING Controller!");
            return result;
        }
        ```

        接下来，让我们执行一个请求，看看日志的情况如何：

        ```log
        Starting BLOCKING Controller!
        Product(title=Fancy Smartphone, description=A stylish phone you need)
        Product(title=Cool Watch, description=The only device you need)
        Product(title=Smart TV, description=Cristal clean images)
        Exiting BLOCKING Controller!
        ```

        正如预期的那样，在同步实现的情况下，请求线程正在等待接收所有产品。之后，它将把它们打印到控制台，并在最后关闭请求线程之前退出控制器函数。

    2. 使用WebClient来调用一个慢速服务

        其次，让我们实现一个非阻塞的WebClient来调用同一个端点：

        ```java
        @GetMapping(value = "/products-non-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<Product> getProductsNonBlocking() {
            log.info("Starting NON-BLOCKING Controller!");
            Flux<Product> productFlux = WebClient.create()
            .get()
            .uri(getSlowServiceBaseUri() + SLOW_SERVICE_PRODUCTS_ENDPOINT_NAME)
            .retrieve()
            .bodyToFlux(Product.class);
            productFlux.subscribe(product -> log.info(product.toString()));
            log.info("Exiting NON-BLOCKING Controller!");
            return productFlux;
        }
        ```

        控制器函数没有返回产品列表，而是返回Flux发布者并迅速完成方法。在这种情况下，消费者将订阅Flux实例并在产品可用时进行处理。

        现在，让我们再看一下日志：

        ```log
        Starting NON-BLOCKING Controller!
        Exiting NON-BLOCKING Controller!
        Product(title=Fancy Smartphone, description=A stylish phone you need)
        Product(title=Cool Watch, description=The only device you need)
        Product(title=Smart TV, description=Cristal clean images)
        ```

        正如预期的那样，控制器功能立即完成，通过这个，它也完成了请求线程。一旦产品可用，订阅函数就会处理它们。

4. 总结

    在这篇文章中，我们比较了在Spring中编写网络客户端的两种风格。

    首先，我们探讨了Feign client，一种编写同步和阻塞网络客户端的声明式风格。

    其次，我们探讨了WebClient，它可以实现网络客户端的异步实现。

    尽管Feign客户端在许多情况下是一个很好的选择，而且所产生的代码具有较低的认知复杂度，但WebClient的非阻塞风格在高流量高峰期使用的系统资源要少得多。考虑到这一点，在这些情况下，最好选择WebClient。

## 相关文章

- [Logging a Reactive Sequence](https://www.baeldung.com/spring-reactive-sequence-logging)
- [Reading Flux Into a Single InputStream Using Spring Reactive WebClient](https://www.baeldung.com/spring-reactive-read-flux-into-inputstream)
- [ ] [Spring Boot FeignClient vs. WebClient](https://www.baeldung.com/spring-boot-feignclient-vs-webclient)
- [Cancel an Ongoing Flux in Spring WebFlux](https://www.baeldung.com/spring-webflux-cancel-flux)
- More articles: [[<-- prev]](../spring-5-reactive-2)

## Code

本文的代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-reactive-modules/spring-5-reactive-3)上找到。
