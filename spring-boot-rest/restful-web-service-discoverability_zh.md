# [REST API 可发现性与 HATEOAS](https://www.baeldung.com/restful-web-service-discoverability)

1. 概述

    本文将重点介绍 REST API 的可发现性、HATEOAS 和由测试驱动的实际场景。

2. 为什么要使 API 具有可发现性

    API 的可发现性是一个没有得到足够重视的话题。因此，很少有应用程序接口能做到这一点。但如果操作得当，它不仅能让 API 变得 RESTful、可用，还能让 API 变得优雅。

    要理解可发现性，我们需要理解 “超媒体作为应用状态引擎”（Hypermedia As The Engine Of Application State, HATEOAS）约束。REST 应用程序接口的这一约束条件是，作为应用状态的[唯一驱动因素](http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven)，超媒体（实际上是超文本）资源上的操作/转换具有完全的可发现性。

    如果交互是由 API 通过对话本身来驱动的，具体来说就是通过超文本，那么就不可能有文档。这将迫使客户端做出事实上超出 API 上下文的假设。

    总之，服务器应该有足够的描述性来指导客户端如何通过超文本使用应用程序接口。在 HTTP 对话中，我们可以通过链接头来实现这一点。

3. 可发现性场景（由测试驱动）

    那么，REST 服务的可发现性意味着什么呢？

    在本节中，我们将使用 Junit、[rest-assured](https://github.com/rest-assured/rest-assured) 和 [Hamcrest](https://code.google.com/archive/p/hamcrest/) 测试可发现性的各个特征。由于 [REST 服务之前已经过安全处理](https://www.baeldung.com/securing-a-restful-web-service-with-spring-security)，因此每个测试在使用 API 之前都需要先进行身份[验证](https://gist.github.com/1341570)。

    1. 发现有效的 HTTP 方法

        当使用无效 HTTP 方法访问 REST 服务时，响应应为 405 METHOD NOT ALLOWED。

        API 还应帮助客户端发现允许用于特定资源的有效 HTTP 方法。为此，我们可以在响应中使用 Allow HTTP 标头：

        ```java
        @Test
        public void
        whenInvalidPOSTIsSentToValidURIOfResource_thenAllowHeaderListsTheAllowedActions(){
            // Given
            String uriOfExistingResource = restTemplate.createResource();

            // When
            Response res = givenAuth().post(uriOfExistingResource);

            // Then
            String allowHeader = res.getHeader(HttpHeaders.ALLOW);
            assertThat( allowHeader, AnyOf.anyOf(
            containsString("GET"), containsString("PUT"), containsString("DELETE") ) );
        }
        ```

    2. 发现新创建资源的 URI

        创建新资源的操作应始终在响应中包含新创建资源的 URI。为此，我们可以使用位置 HTTP 头。

        现在，如果客户端对该 URI 进行 GET，资源就会可用：

        ```java
        @Test
        public void whenResourceIsCreated_thenUriOfTheNewlyCreatedResourceIsDiscoverable() {
            // When
            Foo newResource = new Foo(randomAlphabetic(6));
            Response createResp = givenAuth().contentType("application/json")
            .body(unpersistedResource).post(getFooURL());
            String uriOfNewResource= createResp.getHeader(HttpHeaders.LOCATION);

            // Then
            Response response = givenAuth().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .get(uriOfNewResource);

            Foo resourceFromServer = response.body().as(Foo.class);
            assertThat(newResource, equalTo(resourceFromServer));
        }
        ```

        该测试遵循一个简单的场景：创建一个新的 Foo 资源，然后使用 HTTP 响应发现该资源现在可用的 URI。然后在该 URI 上执行 GET 来检索资源，并将其与原始资源进行比较。这是为了确保资源保存正确。

    3. 发现 URI 以获取该类型的所有资源

        当我们 GET 任何特定的 Foo 资源时，我们应该能够发现下一步可以做什么：我们可以列出所有可用的 Foo 资源。因此，检索资源的操作应始终在其响应中包含获取该类型所有资源的 URI。

        为此，我们可以再次使用链接头：

        ```java
        @Test
        public void whenResourceIsRetrieved_thenUriToGetAllResourcesIsDiscoverable() {
            // Given
            String uriOfExistingResource = createAsUri();

            // When
            Response getResponse = givenAuth().get(uriOfExistingResource);

            // Then
            String uriToAllResources = HTTPLinkHeaderUtil
            .extractURIByRel(getResponse.getHeader("Link"), "collection");

            Response getAllResponse = givenAuth().get(uriToAllResources);
            assertThat(getAllResponse.getStatusCode(), is(200));
        }
        ```

        请注意，[这里](https://gist.github.com/eugenp/8269915)显示的是 extractURIByRel 的完整底层代码，它负责按 rel 关系提取 URI。

        该测试涉及 REST 中链接关系的棘手问题：检索所有资源的 URI 使用 rel="collection" 语义。

        这种类型的链接关系尚未标准化，但已被多个微格式使用，并被提议进行标准化。非标准链接关系的使用开启了关于微格式和 RESTful 网络服务中更丰富语义的讨论。

4. 其他潜在的可发现 URI 和微格式

    其他 URI 有可能通过链接标头被发现，但如果不转向更丰富的语义标记，如[定义自定义链接关系](http://tools.ietf.org/html/rfc5988#section-6.2.1)、[Atom 发布协议](https://datatracker.ietf.org/doc/html/rfc5023)或[微格式](https://en.wikipedia.org/wiki/Microformat)（这将是另一篇文章的主题），现有类型的链接关系所允许的范围就有限了。

    例如，在对特定资源进行 GET 时，客户端应能发现用于创建新资源的 URI。遗憾的是，创建语义模型没有链接关系。

    幸运的是，创建的 URI 与 GET 该类型所有资源的 URI 是相同的，唯一的区别在于 POST HTTP 方法，这是一种标准做法。

5. 结论

    我们已经了解了 REST API 是如何在没有任何先验知识的情况下从根目录完全被发现的--这意味着客户端可以通过在根目录上执行 GET 来浏览它。展望未来，所有的状态变化都是由客户端使用 REST API 在表征中提供的可用和可发现的转换（因此称为表征状态转移）来驱动的。

    本文介绍了 REST 网络服务中可发现性的一些特征，讨论了 HTTP 方法发现、创建和获取之间的关系、发现获取所有资源的 URI 等。
