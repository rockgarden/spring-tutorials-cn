# 春之云系列--网关模式

1. 概述

到目前为止，在我们的云应用程序中，我们使用网关模式支持两个主要功能。

首先，我们将客户端与每项服务隔离，从而消除了跨源支持的需要。其次，我们使用 Eureka 实现了服务实例的定位。

在本文中，我们将探讨如何使用网关模式通过单个请求从多个服务中检索数据。为此，我们将在网关中引入 Feign，以帮助编写对服务的 API 调用。

要了解如何使用 OpenFeign 客户端，请参阅本文。

Spring Cloud 现在也提供了实现此模式的 Spring Cloud Gateway 项目。

2. 安装

打开网关服务器的 pom.xml，添加 OpenFeign 的依赖关系：

<依赖关系
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</依赖关系
作为参考，我们可以在 Maven Central 上找到最新版本（spring-cloud-starter-feign）。

现在我们已经支持构建 OpenFeign 客户端，让我们在 GatewayApplication.java 中启用它：

@EnableFeignClients
public class GatewayApplication { ... }
现在，让我们为图书和评级服务设置 OpenFeign 客户端。

3. OpenFeign 客户端

3.1. 图书客户端

让我们创建一个名为 BooksClient.java 的新界面：

@FeignClient(“book-service”)
公共接口 BooksClient {
 
    @RequestMapping(value = “/books/{bookId}”, method = RequestMethod.GET)
    Book getBookById(@PathVariable(“bookId”) Long bookId)；
}
有了这个接口，我们就可以指示 Spring 创建一个 OpenFeign 客户端来访问“/books/{bookId}”端点。调用 getBookById 方法时，该方法将对端点进行 HTTP 调用，并使用 bookId 参数。

为了实现这一功能，我们需要添加一个 Book.java DTO：

@JsonIgnoreProperties(ignoreUnknown = true)
公共类 Book {
 
    private Long id；
    private String author；
    private String title；
    private List<Rating> ratings；
    
    // 获取器和设置器
}
下面我们来看看 RatingsClient。

3.2. 评分客户端

让我们创建一个名为 RatingsClient 的接口：

@FeignClient(“rating-service”)
公共接口 RatingsClient {
 
    @RequestMapping(value = “/ratings”, method = RequestMethod.GET)
    List<Rating> getRatingsByBookId(
      @RequestParam(“bookId”) Long bookId、 
      @RequestHeader(“Cookie”) String session)；
    
}
与 BookClient 一样，这里暴露的方法将对我们的评级服务进行其余调用，并返回一本书的评级列表。

不过，这个端点是安全的。为了能够正确访问该端点，我们需要在请求中传递用户的会话。

为此，我们使用 @RequestHeader 注解。这将指示 OpenFeign 将该变量的值写入请求头。在本例中，我们将写入 Cookie 头信息，因为 Spring Session 将在 Cookie 中查找我们的会话。

在本例中，我们将把变量值写入 Cookie 头信息，因为 Spring Session 会在 Cookie 中查找我们的会话。

最后，让我们添加一个 Rating.java DTO：

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rating {
    private Long id；
    private Long bookId；
    private int stars；
}
现在，两个客户端都已完成。让我们开始使用它们！

4. 组合请求

网关模式的一个常见用例是拥有封装常用服务的端点。这可以减少客户端请求的数量，从而提高性能。

为此，让我们创建一个控制器，并将其命名为 CombinedController.java：

@RestController
@RequestMapping(“/combined”)
public class CombinedController { ... }
接下来，让我们为新创建的 OpenFeign 客户端布线：

private BooksClient booksClient；
private RatingsClient ratingsClient；

@Autowired
public CombinedController(
  BooksClient booksClient、 
  评分客户端 ratingsClient) {
 
    this.booksClient = booksClient；
    this.ratingsClient = ratingsClient；
}
最后，让我们创建一个 GET 请求，将这两个端点结合起来，并返回一本书及其加载的评分：

@GetMapping
public Book getCombinedResponse(
  @RequestParam Long bookId、
  @CookieValue(“SESSION”) String session) {
 
    Book book = booksClient.getBookById(bookId)；
    List<Rating> ratings = ratingsClient.getRatingsByBookId(bookId, “SESSION=”+session)；
    book.setRatings(ratings)；
    返回 book；
}
请注意，我们是使用 @CookieValue 注解设置会话值的，该注解可从请求中提取会话值。

就是这样！我们在网关中组合了一个端点，减少了客户端和系统之间的网络调用！

5. 测试

让我们确保新端点正常工作。

导航至 LiveTest.java，为我们的组合端点添加一个测试：

@Test
public void accessCombinedEndpoint() {
    响应 response = RestAssured.given()
      .auth()
      .form(“user”, “password”, formConfig)
      .get(ROOT_URI + “/combined?bookId=1”)；


启动 Redis，然后运行应用程序中的每个服务：config、discovery、zipkin、gateway、book 和评级服务。

一切就绪后，运行新测试以确认其正常工作。

6. 总结

我们已经了解了如何将 OpenFeign 集成到我们的网关中，从而构建一个专门的端点。我们可以利用这些信息来构建我们需要支持的任何 API。最重要的是，我们看到了我们并没有被只能公开单个资源的一刀切 API 所困。

使用网关模式，我们可以根据每个客户的不同需求设置网关服务。这样就形成了解耦，让我们的服务可以根据需要自由发展，保持精简并专注于应用程序的一个领域。

请一如既往地访问 GitHub 上的代码片段。