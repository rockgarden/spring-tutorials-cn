# 核心Java网络（第2部分）

- [用 Java 检查 URL 是否存在](https://www.baeldung.com/java-check-url-exists)
- [使用 HttpURLConnection 发出 JSON POST 请求](https://www.baeldung.com/httpurlconnection-post)
- [在 Java 中使用 Curl](https://www.baeldung.com/java-curl)
- [ ] [用Java进行简单的HTTP请求](#用java进行简单的http请求)
- [使用Java发送电子邮件](https://www.baeldung.com/java-email)
- [使用 HttpUrlConnection 进行身份验证](https://www.baeldung.com/java-http-url-connection)
- [用 Java 从 URL 下载文件](https://www.baeldung.com/java-download-file)
- [处理 java.net.ConnectException](https://www.baeldung.com/java-net-connectexception)
- [用 Java 获取 MAC 地址](https://www.baeldung.com/java-mac-address)
- [ ] [用Java发送带附件的电子邮件](#用java发送带有附件的电子邮件)
- [[<-- 上一页]](.../core-java-networking/README.md)

## 用Java进行简单的HTTP请求

1. 概述

    在本快速教程中，我们将介绍一种在 Java 中执行 HTTP 请求的方法--使用内置的 Java 类 HttpUrlConnection。

    请注意，从 JDK 11 开始，Java 为执行 HTTP 请求提供了一个新的 API，即 [HttpClient API](https://www.baeldung.com/java-9-http-client)，用来替代 HttpUrlConnection。

2. HttpUrlConnection

    HttpUrlConnection 类允许我们执行基本的 HTTP 请求，而无需使用任何额外的库。我们需要的所有类都是 java.net 包的一部分。

    使用这种方法的缺点是，代码可能比其他 HTTP 库更繁琐，而且它不提供更高级的功能，如添加标头或身份验证的专用方法。

3. 创建请求

    我们可以使用 URL 类的 openConnection() 方法创建一个 HttpUrlConnection 实例。请注意，该方法只创建了一个连接对象，但还没有建立连接。

    通过将 requestMethod 属性设置为以下值之一，HttpUrlConnection 类可用于所有类型的请求： get、post、head、options、put、delete、trace。

    让我们使用 GET 方法创建一个与给定 URL 的连接：

    ```java
    URL url = new URL("http://example.com");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    ```

4. 添加请求参数

    如果要在请求中添加参数，我们必须将 doOutput 属性设为 true，然后向 HttpUrlConnection 实例的 OutputStream 写入参数形式为 param1=value&param2=value 的字符串：

    ```java
    Map<String, String> parameters = new HashMap<>();
    parameters.put("param1", "val");
    con.setDoOutput(true);
    DataOutputStream out = new DataOutputStream(con.getOutputStream());
    out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
    out.flush();
    out.close();
    ```

    为了方便参数 Map 的转换，我们编写了一个名为 ParameterStringBuilder 的实用程序类，其中包含一个静态方法 getParamsString()，可将 Map 转换为所需格式的字符串：

    httprequest/ParameterStringBuilder.java

5. 设置请求标题

    使用 setRequestProperty() 方法可为请求添加标头：

    `con.setRequestProperty("Content-Type", "application/json");`
    要从连接中读取头的值，我们可以使用 getHeaderField() 方法：

    `String contentType = con.getHeaderField("Content-Type");`
6. 配置超时

    HttpUrlConnection 类允许设置连接和读取超时。这些值定义了等待与服务器建立连接或等待读取数据的时间间隔。

    要设置超时值，我们可以使用 setConnectTimeout() 和 setReadTimeout() 方法：

    ```java
    con.setConnectTimeout(5000);
    con.setReadTimeout(5000);
    ```

    在示例中，我们将两个超时值都设置为 5 秒。

7. 处理Cookie

    java.net 软件包中包含的 CookieManager 和 HttpCookie 等类可以方便地处理 Cookie。

    首先，要从响应中读取 cookie，我们可以检索 Set-Cookie 标头的值，并将其解析为 HttpCookie 对象列表：

    ```java
    String cookiesHeader = con.getHeaderField("Set-Cookie");
    List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
    ```

    接下来，我们将把 cookie 添加到 cookie 存储中：

    `cookies.forEach(cookie -> cookieManager.getCookieStore().add(null, cookie));`
    让我们检查一下是否存在名为 username 的 cookie，如果没有，我们就将其添加到 cookie 存储中，值为 "john"：

    ```java
    Optional<HttpCookie> usernameCookie = cookies.stream()
      .findAny().filter(cookie -> cookie.getName().equals("username"));
    if (usernameCookie == null) {
        cookieManager.getCookieStore().add(null, new HttpCookie("username", "john"));
    }
    ```

    最后，为了将 cookie 添加到请求中，我们需要在关闭并重新打开连接后设置 Cookie 头信息：

    ```java
    con.disconnect();
    con = (HttpURLConnection) url.openConnection();
    con.setRequestProperty("Cookie", 
      StringUtils.join(cookieManager.getCookieStore().getCookies(), ";"));
    ```

8. 处理重定向

    我们可以使用带有 true 或 false 参数的 setInstanceFollowRedirects() 方法启用或禁用特定连接的自动跟随重定向：

    `con.setInstanceFollowRedirects(false);`
    还可以启用或禁用所有连接的自动重定向：

    `HttpUrlConnection.setFollowRedirects(false);`
    默认情况下，该行为已启用。

    当请求返回状态代码 301 或 302（表示重定向）时，我们可以检索位置标头，并创建指向新 URL 的新请求：

    ```java
    if (status == HttpURLConnection.HTTP_MOVED_TEMP
    || status == HttpURLConnection.HTTP_MOVED_PERM) {
        String location = con.getHeaderField("Location");
        URL newUrl = new URL(location);
        con = (HttpURLConnection) newUrl.openConnection();
    }
    ```

9. 读取响应

    可以通过解析 HttpUrlConnection 实例的 InputStream 来读取请求的响应。

    要执行请求，我们可以使用 getResponseCode()、connect()、getInputStream() 或 getOutputStream() 方法：

    `int status = con.getResponseCode();`
    最后，让我们读取请求的响应并将其放入内容字符串中：

    ```java
    BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer content = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
    }
    in.close();
    ```

    要关闭连接，我们可以使用 disconnect() 方法：

    `con.disconnect();`
10. 读取请求失败时的响应

    如果请求失败，试图读取 HttpUrlConnection 实例的 InputStream 将不起作用。相反，我们可以使用 HttpUrlConnection.getErrorStream() 提供的流。

    我们可以通过比较[HTTP状态代码](https://www.baeldung.com/cs/http-status-codes)来决定使用哪个 InputStream：

    ```java
    int status = con.getResponseCode();
    Reader streamReader = null;
    if (status > 299) {
        streamReader = new InputStreamReader(con.getErrorStream());
    } else {
        streamReader = new InputStreamReader(con.getInputStream());
    }
    ```

    最后，我们可以用与上一节相同的方法读取 streamReader。

11. 构建完整的响应

    使用 HttpUrlConnection 实例无法获得完整的响应表示。

    不过，我们可以使用 HttpUrlConnection 实例提供的一些方法来构建它：

    httprequest/FullResponseBuilder.java

    在此，我们将读取响应的各个部分，包括状态代码、状态信息和头信息，并将其添加到 StringBuilder 实例中。

    首先，让我们添加响应状态信息：

    ```java
    fullResponseBuilder.append(con.getResponseCode())
    .append(" ")
    .append(con.getResponseMessage())
    .append("\n");
    ```

    接下来，我们将使用 getHeaderFields() 获取头信息，并按照 HeaderName： HeaderValues：

    ```java
    con.getHeaderFields().entrySet().stream()
    .filter(entry -> entry.getKey() != null)
    .forEach(entry -> {
        fullResponseBuilder.append(entry.getKey()).append(": ");
        List headerValues = entry.getValue();
        Iterator it = headerValues.iterator();
        if (it.hasNext()) {
            fullResponseBuilder.append(it.next());
            while (it.hasNext()) {
                fullResponseBuilder.append(", ").append(it.next());
            }
        }
        fullResponseBuilder.append("\n");
    });
    ```

    最后，我们将按照之前的方法读取响应内容并进行追加。

    请注意，getFullResponse 方法将验证请求是否成功，以便决定是否需要使用 con.getInputStream() 或 con.getErrorStream() 来获取请求内容。

12. 结论

    在本文中，我们展示了如何使用 HttpUrlConnection 类执行 HTTP 请求。

## 用Java发送带有附件的电子邮件

1. 概述

    在本快速教程中，我们将学习如何使用JavaMail API在Java中发送带有单个和多个附件的电子邮件。

2. 项目设置

    在本文中，我们将使用javax创建一个简单的Maven项目。邮件相关依赖：javax.mail.mail

3. 发送带有附件的邮件

    首先，我们需要配置[电子邮件服务提供商的凭据](https://www.baeldung.com/java-email#sending-a-plain-text-and-an-html-email)。然后，通过提供电子邮件主机、端口、用户名和密码来创建会话对象。所有这些详细信息都由电子邮件主机服务提供。我们可以为代码使用任何虚假的SMTP测试服务器。

    会话对象将作为连接工厂来处理JavaMail的配置和身份验证。

    现在我们有了一个Session对象，让我们进一步创建MimeMessage和MimeBodyPart对象。我们使用这些对象创建电子邮件：

    ```java
    Message message = new MimeMessage(session); 
    message.setFrom(new InternetAddress(from)); 
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); 
    message.setSubject("Test Mail Subject"); 

    BodyPart messageBodyPart = new MimeBodyPart(); 
    messageBodyPart.setText("Mail Body");
    ```

    在上面的代码片段中，我们创建了MimeMessage对象，其中包含from、to和subject等所需的详细信息。然后，我们得到了一个带有电子邮件正文的MimeBodyPart对象。

    现在，我们需要创建另一个MimeBodyPart以在邮件中添加附件：

    ```java
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile(new File("path/to/file"));
    ```

    现在，一个邮件会话有两个MimeBodyPart对象。因此，我们需要创建一个MimeMultipart对象，然后将两个MimeBodyPart对象都添加到其中：

    ```java
    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);
    multipart.addBodyPart(attachmentPart);
    ```

    最后，MimeMultiPart作为邮件内容和传输添加到MimeMessage对象。调用send（）方法发送消息：

    ```java
    message.setContent(multipart);
    Transport.send(message);
    ```

    总之，消息包含MimeMultiPart，它还包含多个MimeBodyPart。这就是我们组装完整电子邮件的方式。

    此外，要发送多个附件，只需添加另一个MimeBodyPart即可。

    超时参数：“默认无穷大”，出现异常后（比如邮箱服务器繁忙）有可能导致核心业务逻辑在等待往服务器读写消息，形成阻塞。

    | 参数                          | 类型  | 解释                                                                                                                                                                                                                                                                                                                      |
    |-----------------------------|-----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | mail.smtp.connectiontimeout | int | Socket connection timeout value in milliseconds. This timeout is implemented by java.net.Socket. Default is infinite timeout.                                                                                                                                                                                           |
    | mail.smtp.timeout           | int | Socket read timeout value in milliseconds. This timeout is implemented by java.net.Socket. Default is infinite timeout.                                                                                                                                                                                                 |
    | mail.smtp.writetimeout      | int | Socket write timeout value in milliseconds. This timeout is implemented by using a java.util.concurrent.ScheduledExecutorService per connection that schedules a thread to close the socket if the timeout expires. Thus, the overhead of using this timeout is one thread per connection. Default is infinite timeout. |

    新版 JAKARTA MAIL 可代替 Java mail。

    <https://eclipse-ee4j.github.io/mail/#Download_Jakarta_Mail_Release>

## Code

示例的完整源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-networking-2) 上找到。

## Relevant Articles

- [Checking if a URL Exists in Java](https://www.baeldung.com/java-check-url-exists)
- [Making a JSON POST Request With HttpURLConnection](https://www.baeldung.com/httpurlconnection-post)
- [Using Curl in Java](https://www.baeldung.com/java-curl)
- [Do a Simple HTTP Request in Java](https://www.baeldung.com/java-http-request)
- [Sending Emails with Java](https://www.baeldung.com/java-email)
- [Authentication with HttpUrlConnection](https://www.baeldung.com/java-http-url-connection)
- [Download a File From an URL in Java](https://www.baeldung.com/java-download-file)
- [Handling java.net.ConnectException](https://www.baeldung.com/java-net-connectexception)
- [Getting MAC Addresses in Java](https://www.baeldung.com/java-mac-address)
- [Sending Emails with Attachments in Java](https://www.baeldung.com/java-send-emails-attachments)
- [[<-- Prev]](../core-java-networking/README.md)
