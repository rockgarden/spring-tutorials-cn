# [Spring MVC中的HttpMediaTypeNotAcceptableException](https://www.baeldung.com/spring-httpmediatypenotacceptable)

1. 一览表

    在这篇短文中，我们将看看HttpMediaTypeNotAcceptableException异常，并了解我们可能遇到它的情况。

2. 问题

    使用Spring实现API端点时，我们通常需要指定消耗/生成的媒体类型（通过消耗和生成参数）。这缩小了API将返回客户端进行该特定操作的可能格式。

    HTTP还有专用的“Accept”标头——用于指定客户端识别和可以接受的媒体类型。简单地说，服务器将使用客户端请求的媒体类型之一回送资源表示。

    然而，如果没有双方都可以使用的常见类型，Spring将抛出HttpMediaTypeNotAcceptableException异常。

3. 实际例子

    让我们创建一个简单的例子来演示这个场景。

    我们将使用POST端点——它只能与“application/json”一起使用，并返回JSON数据：

    ```java
    @PostMapping(
    value = "/test", 
    consumes = MediaType.APPLICATION_JSON_VALUE, 
    produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> example() {
        return Collections.singletonMap("key", "value");
    }
    ```

    然后，让我们使用CURL发送具有未识别内容类型的请求：

    ```bash
    curl -X POST --header "Accept: application/pdf" http://localhost:8080/test -v

    > POST /test HTTP/1.1
    > Host: localhost:8080
    > User-Agent: curl/7.51.0
    > Accept: application/pdf
    ```

    我们得到的回应是：

    ```log
    < HTTP/1.1 406 
    < Content-Length: 0
    ```

4. 解决方案

    解决问题的方法只有一个——发送/接收受支持的类型之一。

    我们所能做的就是提供一条更具描述性的消息（默认情况下，Spring返回一个空正文），并带有customExceptionHandler通知客户端所有可接受的媒体类型。

    在我们的案例中，它只是“application/json”：

    ```java
    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public String handleHttpMediaTypeNotAcceptableException() {
        return "acceptable MIME type:" + MediaType.APPLICATION_JSON_VALUE;
    }
    ```

5. 结论

    在本教程中，当客户端请求的内容与服务器实际可以生成的内容不匹配时，我们考虑了Spring MVC抛出的HttpMediaTypeNotAcceptableException异常。
