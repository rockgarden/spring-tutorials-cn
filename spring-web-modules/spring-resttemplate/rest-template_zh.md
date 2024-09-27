# [RestTemplate指南](https://www.baeldung.com/rest-template)

1. 一览表

    在本教程中，我们将说明Spring REST客户端（RestTemplate）可以使用的广泛操作，并得到很好的使用。

    对于所有示例的API方面，我们将从这里运行RESTful服务。

2. 弃用通知

    在 Spring Framework 5 中，除了 WebFlux 堆栈，Spring 还引入了一个名为 WebClient 的新 HTTP 客户端。

    WebClient 是一种替代 RestTemplate 的现代 HTTP 客户端。它不仅提供传统的同步 API，还支持高效的非阻塞和异步方法。

    也就是说，如果我们正在开发新应用程序或迁移旧应用程序，使用WebClient是个好主意。展望未来，RestTemplate将在未来版本中被弃用。

3. 使用GET检索资源

    1. 获取纯JSON

        让我们从简单开始，谈谈GET请求，用一个使用getForEntity（）API的快速示例：

        ```java
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
          = "http://localhost:8080/spring-rest/foos";
        ResponseEntity<String> response
          = restTemplate.getForEntity(fooResourceUrl + "/1", String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        ```

        请注意，我们完全可以访问HTTP响应，因此我们可以检查状态代码，以确保操作成功，或与响应的实际主体一起工作：

        ```java
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode name = root.path("name");
        Assertions.assertNotNull(name.asText());
        ```

        我们正在将响应主体作为标准字符串，并使用Jackson（以及Jackson提供的JSON节点结构）来验证一些细节。

    2. 检索POJO而不是JSON

        我们还可以直接将响应映射到资源DTO：

        ```java
        public class Foo implements Serializable {
            private long id;
            private String name;
            // standard getters and setters
        }
        ```

        现在，我们可以简单地在模板中使用getForObject API：

        ```java
        Foo foo = restTemplate
          .getForObject(fooResourceUrl + "/1", Foo.class);
        Assertions.assertNotNull(foo.getName());
        Assertions.assertEquals(foo.getId(), 1L);
        ```

4. 使用HEAD检索标题

    在进入更常见的方法之前，让我们先快速看看使用HEAD。

    我们将在这里使用headForHeaders（）API：

    ```java
    HttpHeaders httpHeaders = restTemplate.headForHeaders(fooResourceUrl);
    Assertions.assertTrue(httpHeaders.getContentType().includes(MediaType.APPLICATION_JSON));
    ```

5. 使用POST创建资源

    为了在API中创建新资源，我们可以很好地利用postForLocation（）、postForObject（）或postForEntity（）API。

    第一个返回新创建的资源的URI，而第二个返回资源本身。

    1. postForObject（）API

        ```java
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Foo> request = new HttpEntity<>(new Foo("bar"));
        Foo foo = restTemplate.postForObject(fooResourceUrl, request, Foo.class);
        Assertions.assertNotNull(foo);
        Assertions.assertEquals(foo.getName(), "bar");
        ```

    2. postForLocation（）API

        同样，让我们来看看操作，该操作不是返回完整资源，而是返回新创建的资源的位置：

        ```java
        HttpEntity<Foo> request = new HttpEntity<>(new Foo("bar"));
        URI location = restTemplate
          .postForLocation(fooResourceUrl, request);
        Assertions.assertNotNull(location);
        ```

    3. exchange() API

        让我们来看看如何使用更通用的交换API进行POST：

        ```java
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Foo> request = new HttpEntity<>(new Foo("bar"));
        ResponseEntity<Foo> response = restTemplate
          .exchange(fooResourceUrl, HttpMethod.POST, request, Foo.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.CREATED);

        Foo foo = response.getBody();

        Assertions.assertNotNull(foo);
        Assertions.assertEquals(foo.getName(), "bar");
        ```

    4. 提交表格数据

        接下来，我们来看看如何使用POST方法提交表格。

        首先，我们需要将内容类型标头设置为application/x-www-form-urlencoded。

        这确保了可以向服务器发送一个大型查询字符串，其中包含以&分隔的名称/值对：

        ```java
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ```

        我们可以将表单变量包装到LinkedMultiValueMap中：

        ```java
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("id", "1");
        ```

        接下来，我们使用HttpEntity实例构建请求：

        `HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);`

        最后，我们可以通过在端点上调用restTemplate.postForEntity()连接到REST服务：/foos/form

        ```java
        ResponseEntity<String> response = restTemplate.postForEntity(
          fooResourceUrl + "/form", request , String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        ```

6. 使用选项来获取允许的操作

    接下来，我们将快速查看使用OPTIONS请求，并使用此类请求在特定URI上探索允许的操作；API是optionsForAllow：

    ```java
    Set<HttpMethod> optionsForAllow = restTemplate.optionsForAllow(fooResourceUrl);
    HttpMethod[] supportedMethods
      = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
    Assertions.assertTrue(optionsForAllow.containsAll(Arrays.asList(supportedMethods)));
    ```

7. 使用PUT来更新资源

    接下来，我们将开始研究PUT，更具体地说是此操作的exchange（）API，因为template.put API非常简单。

    1. 简单的PUT与exchange()

        我们将从针对API的简单PUT操作开始——并记住该操作不会将主体返回给客户端：

        ```java
        Foo updatedInstance = new Foo("newName");
        updatedInstance.setId(createResponse.getBody().getId());
        String resourceUrl =
          fooResourceUrl + '/' + createResponse.getBody().getId();
        HttpEntity<Foo> requestUpdate = new HttpEntity<>(updatedInstance, headers);
        template.exchange(resourceUrl, HttpMethod.PUT, requestUpdate, Void.class);
        ```

    2. 使用exchange()和请求回调的PUT

        接下来，我们将使用请求回调来发出PUT。

        让我们确保我们准备回调，我们可以在其中设置所有我们需要的标头以及请求正文：

        ```java
        RequestCallback requestCallback(final Foo updatedInstance) {
            return clientHttpRequest -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(clientHttpRequest.getBody(), updatedInstance);
                clientHttpRequest.getHeaders().add(
                  HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                clientHttpRequest.getHeaders().add(
                  HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedLogPass());
            };
        }
        ```

        接下来，我们用POST请求创建资源：

        ```java
        ResponseEntity<Foo> response = restTemplate
          .exchange(fooResourceUrl, HttpMethod.POST, request, Foo.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        ```

        然后我们更新资源：

        ```java
        Foo updatedInstance = new Foo("newName");
        updatedInstance.setId(response.getBody().getId());
        String resourceUrl = fooResourceUrl + '/' + response.getBody().getId();
        restTemplate.execute(
          resourceUrl,
          HttpMethod.PUT,
          requestCallback(updatedInstance),
          clientHttpResponse -> null);
        ```

8. 使用DELETE删除资源

    要删除现有资源，我们将快速使用delete（）API：

    ```java
    String entityUrl = fooResourceUrl + "/" + existingResource.getId();
    restTemplate.delete(entityUrl);
    ```

9. 配置超时

    我们只需使用ClientHttpRequestFactory，即可将RestTemplate配置为超时：

    ```java
    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
        = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }
    ```

    我们可以使用HttpClient进行进一步的配置选项：

    ```java
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();
        CloseableHttpClient client = HttpClientBuilder
            .create()
            .setDefaultRequestConfig(config)
            .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }
    ```

10. 结论

    在本文中，我们介绍了主要的HTTP动词，使用RestTemplate来编排使用所有这些的请求。

    如果您想深入了解如何使用模板进行身份验证，请查看我们关于使用RestTemplate的基本身份验证的[文章](https://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring)。
