# [用Spring服务静态资源](https://www.baeldung.com/spring-mvc-static-resources)

本教程探讨了如何使用XML和Java配置为Spring提供静态资源。

进一步阅读:

- WebJars简介
  关于在Spring中使用WebJars的快速实用指南。
  [阅读更多](https://www.baeldung.com/maven-webjars)

1. 使用Spring Boot

    Spring Boot预设了一个[ResourceHttpRequestHandler](https://github.com/spring-projects/spring-framework/blob/master/spring-webmvc/src/main/java/org/springframework/web/servlet/resource/ResourceHttpRequestHandler.java)的实现，以方便为静态资源提供服务。

    默认情况下，该处理程序会从classpath上的任何/static、/public、/resources和/META-INF/resources目录中提供静态内容。由于 src/main/resources 通常默认在 classpath 上，我们可以把这些目录中的任何一个放在那里。

    例如，如果我们把about.html文件放在classpath中的/static目录内，那么我们就可以通过<http://localhost:8080/about.html>，访问该文件。同样，我们也可以通过在其他提到的目录中添加该文件来达到同样的效果。

    1. 自定义路径模式

        默认情况下，Spring Boot在请求的根目录下提供所有的静态内容，即/**。虽然这似乎是一个很好的默认配置，但我们可以通过[spring.mvc.static-path-pattern](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/web/servlet/WebMvcProperties.java#L105)配置属性改变它。

        例如，如果我们想通过 <http://localhost:8080/content/about.html> 来访问同一个文件，我们可以在我们的application.properties中这样声明：

        `spring.mvc.static-path-pattern=/content/**`

        在WebFlux环境中，我们应该使用[spring.webflux.static-path-pattern](https://github.com/spring-projects/spring-boot/blob/c71ed407e43e561333719573d63464ae9db388c1/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/web/reactive/WebFluxProperties.java#L38)属性。

    2. 定制目录

        与路径模式类似，我们可以通过[spring.web.resources.static-locations](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/web/WebProperties.java#L86)配置属性改变默认资源位置。该属性可以接受多个逗号分隔的资源位置。

        `spring.web.resources.static-locations=classpath:/files/,classpath:/static-files`

        这里我们从classpath里面的/files和/static-files目录提供静态内容。此外，Spring Boot可以从classpath之外提供静态文件。

        `spring.web.resources.static-locations=file:/opt/files`

        这里我们使用文件[资源签名](https://docs.spring.io/spring/docs/5.2.4.RELEASE/spring-framework-reference/core.html#resources-resourceloader)，file:/，来提供本地磁盘上的文件。

2. XML配置

    如果我们需要使用基于XML的配置，我们可以很好地利用`mvc:resources`元素，以特定的公共URL模式指向资源的位置。

    例如，通过在应用程序根文件夹下的“/resources/”目录中搜索，以下行将为所有以公共URL模式（如“/resources/**”）传入的资源请求提供服务：

    `<mvc:resources mapping="/resources/**" location="/resources/" />`

    现在我们可以访问如下HTML页面中的CSS文件：

    ```html
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <html>
    <head>
        <link href="<c:url value="/resources/myCss.css" />" rel="stylesheet">
        <title>Home</title>
    </head>
    <body>
        <h1>Hello world!</h1>
    </body>
    </html>
    ```

3. ResourceHttpRequestHandler

    Spring 3.1 引入了ResourceHandlerRegistry来配置ResourceHttpRequestHandlers，以从类路径、WAR或文件系统提供静态资源。我们可以在web上下文配置类中以编程方式配置ResourceHandlerRegistry。

    1. 服务WAR中存储的资源

        为了说明这一点，我们将使用与之前相同的URL指向myCs.css，但现在实际的文件将位于WAR的webapp/resources文件夹中，在部署Spring 3.1+应用程序时，应该在该文件夹中放置静态资源：

        ```java
        @Configuration
        @EnableWebMvc
        public class MvcConfig implements WebMvcConfigurer {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                .addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
            }
        }
        ```

        让我们分析一下这个例子。首先，我们通过定义资源处理程序来配置面向外部的URI路径(external-facing URI)。然后我们将面向外部的URI路径内部映射到资源实际所在的物理路径。

        当然，我们可以使用这个简单但灵活的API定义多个资源处理程序。

        现在，html页面中的以下行将为我们获取webapp/resources目录中的myCs.css资源：

        `<link href="<c:url value="/resources/myCss.css" />" rel="stylesheet">`

    2. 服务存储在文件系统中的资源

        让我们假设，每当请求匹配/files/**模式的公共URL时，我们都希望为存储在/opt/files/目录中的资源提供服务。我们只需配置URL模式并将其映射到磁盘上的特定位置：

        ```java
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
            .addResourceHandler("/files/**")
            .addResourceLocations("file:/opt/files/");
        }
        ```

        对于Windows用户，此示例中传递给addResourceLocations的参数为“file:///C:/opt/files/“.

        配置资源位置后，我们可以使用home.html中的映射URL模式加载存储在文件系统中的图像：

        ```html
        <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <html>
        <head>
            <link href="<c:url value="/resources/myCss.css" />" rel="stylesheet">
            <title>Home</title>
        </head>
        <body>
            <h1>Hello world!</h1>
            <img alt="image"  src="<c:url value="files/myImage.png" />">
        </body>
        </html>
        ```

    3. 为资源配置多个位置

        如果我们想在多个位置查找资源，该怎么办？

        我们可以使用addResourceLocations方法包含多个位置。将按顺序搜索位置列表，直到找到资源：

        ```java
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
            .addResourceHandler("/resources/**")
            .addResourceLocations("/resources/","classpath:/other-resources/");
        }
        ```

        以下curl请求将显示存储在应用程序的webapp/resources或类路径中的其他资源文件夹中的Hello.html页面：

        `curl -i http://localhost:8080/handling-spring-static-resources/resources/Hello.html`

4. 新的资源解析器

    Spring 4.1 通过新的ResourcesResolver，提供了不同类型的资源解析器，可用于在加载静态资源时优化浏览器性能。这些解析器可以链接并缓存在浏览器中，以优化请求处理。

    1. PathResourceResolver

        这是最简单的解析器，其目的是查找给定公共URL模式的资源。事实上，如果我们不向ResourceChainRegistry添加ResourceResolver，这就是默认的解析器。

        我们来看一个示例：

        ```java
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
            .addResourceHandler("/resources/**")
            .addResourceLocations("/resources/","/other-resources/")
            .setCachePeriod(3600)
            .resourceChain(true)
            .addResolver(new PathResourceResolver());
        }
        ```

        注意事项：

        - 我们将资源链中的PathResourceResolver注册为其中唯一的ResourceResolver。我们可以参考第3.3节。我们可以参考第3.3节。了解如何链接多个ResourceResolver。
        - 服务的资源将在浏览器中缓存3600秒。
        - 链最终使用方法resourceChain(true)进行配置。

        现在查看HTML代码，该代码与PathResourceResolver一起在webapp/resources或webapp/other-resources文件夹中查找foo.js脚本：

        `<script type="text/javascript" src="<c:url value="/resources/foo.js" />">`

    2. 编码资源解析器

        此解析器尝试根据Accept Encoding请求标头值查找编码资源。

        例如，我们可能需要通过使用gzip内容编码服务静态资源的压缩版本来优化带宽。

        要配置EncodedResourceResolver，我们需要在ResourceChain中配置它，就像我们配置PathResourceResolver一样：

        ```java
        registry
            .addResourceHandler("/other-files/**")
            .addResourceLocations("file:/Users/Me/")
            .setCachePeriod(3600)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver());
        ```

        默认情况下，EncodedResourceResolver配置为支持br和gzip编码。

        因此，以下curl请求将获取位于Users/Me/目录中文件系统中Home.html文件的压缩版本：

        `curl -H  "Accept-Encoding:gzip" http://localhost:8080/handling-spring-static-resources/other-files/Hello.html`

        注意我们是如何将标头的“Accept Encoding”值设置为gzip的。这很重要，因为只有当gzip内容对响应有效时，这个特定的解析器才会生效。

        最后，请注意，与之前一样，压缩版本将在浏览器中缓存的时间段内保持可用，在本例中为3600秒。

    3. 链接资源解析器

        为了优化资源查找，ResourceResolver可以将资源的处理委托给其他解析器。唯一不能委托给链的解析器是PathResourceResolver，我们应该将其添加到链的末尾。

        事实上，如果resourceChain未设置为true，那么默认情况下将仅使用PathResourceResolver来服务资源。在这里，如果GzipResourceResolver失败，我们将链接PathResourceResolver以解析资源：

        ```java
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
            .addResourceHandler("/js/**")
            .addResourceLocations("/js/")
            .setCachePeriod(3600)
            .resourceChain(true)
            .addResolver(new GzipResourceResolver())
            .addResolver(new PathResourceResolver());
        }
        ```

        现在我们已经将/js/**模式添加到ResourceHandler中，让我们包含foo.js资源，该资源位于home.html页面的webapp/js/目录中：

        ```html
        <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <html>
        <head>
            <link href="<c:url value="/resources/bootstrap.css" />" rel="stylesheet" />
            <script type="text/javascript"  src="<c:url value="/js/foo.js" />"></script>
            <title>Home</title>
        </head>
        <body>
            <h1>This is Home!</h1>
            <img alt="bunny hop image"  src="<c:url value="files/myImage.png" />" />
            <input type = "button" value="Click to Test Js File" onclick = "testing();" />
        </body>
        </html>
        ```

        值得一提的是，从Spring Framework 5.1开始，GzipResourceResolver已经被弃用，取而代之的是EncodedResourceResolver。因此，我们应该避免在将来使用它。

5. 其他安全配置

    如果使用SpringSecurity，允许访问静态资源是很重要的。我们需要添加访问资源URL的相应权限：

    ```xml
    <intercept-url pattern="/files/**" access="permitAll" />
    <intercept-url pattern="/other-files/**/" access="permitAll" />
    <intercept-url pattern="/resources/**" access="permitAll" />
    <intercept-url pattern="/js/**" access="permitAll" />
    ```

6. 结论

    在本文中，我们介绍了Spring应用程序服务静态资源的各种方式。

    基于XML的资源配置是一个遗留选项，如果我们还不能遵循Java配置路线，我们可以使用它。

    Spring 3.1。通过其ResourceHandlerRegistry对象提供了一个基本的编程替代方案。

    最后，Spring 4.1 附带的新的现成ResourceResolver和ResourceChainRegistry对象。提供资源加载优化功能，如缓存和资源处理程序链接，以提高静态资源的服务效率。

    此外，本项目中还提供了与Spring Boot相关的[源代码](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-mvc-2)。
