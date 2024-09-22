# [使用Spring MVC的可缓存静态资产](https://www.baeldung.com/cachable-static-assets-with-spring-mvc)

本文主要介绍在使用Spring Boot和Spring MVC提供静态资产（如Javascript和CSS文件）时的缓存。

我们还将讨论 "perfect caching" 的概念，主要是确保当文件被更新时，旧版本不会从缓存中被错误地提供。

1. 缓存静态资产

    为了使静态资产可以被缓存，我们需要配置其相应的资源处理程序。

    下面是一个如何做的简单例子--将响应中的Cache-Control头设置为max-age=31536000，这将导致浏览器使用该文件的缓存版本一年之久。

    ```java
    @EnableWebMvc
    public class MvcConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/js/**") 
                    .addResourceLocations("/js/") 
                    .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
        }
    }
    ```

    我们之所以为缓存有效期设置这么长的时间段，是因为我们希望客户端使用缓存的文件版本，直到该文件被更新，而根据[RFC对Cache-Control头](https://www.ietf.org/rfc/rfc2616.txt)的规定，365天是我们可以使用的最长期限。

    于是，当客户端第一次请求foo.js时，他将通过网络收到整个文件（本例中为37字节），状态码为200 OK。响应中会有以下头信息来控制缓存行为。

    `Cache-Control: max-age=31536000`

    这就指示浏览器以一年的过期时间缓存该文件，其结果是下面的响应。

    `status Code: 200 OK`

    当客户端第二次请求同一文件时，浏览器不会再向服务器发出请求。相反，它将直接从其缓存中提供该文件，并避免网络往返，因此页面的加载速度会快很多。

    `status Code: 200 OK (from cache)`

    Chrome浏览器用户在测试时需要注意，因为如果你按屏幕上的刷新按钮或按F5键刷新页面，Chrome就不会使用缓存。你需要在地址栏上按回车键来观察缓存行为。更多相关信息请点击[这里](http://stackoverflow.com/questions/3401049/chrome-doesnt-cache-images-js-css/16510707#16510707)。

    1. Spring Boot

        要在Spring Boot中定制Cache-Control头文件，我们可以使用[spring.resources.cache.cachecontrol](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/web)属性命名空间下的属性。例如，要将最大年龄改为一年，我们可以在application.properties中添加以下内容。

        `spring.resources.cache.cachecontrol.max-age=365d`

        `spring.web.resources.cache.cachecontrol.max-age=365d`

        这适用于由Spring Boot提供的[所有静态资源](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration.java#L307)。因此，如果我们只是想对请求的一个子集应用缓存策略，我们应该使用普通的Spring MVC方法。

        除了max-age之外，也可以用类似的配置属性来定制其他的[Cache-Control](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control)参数，如no-store或no-cache。

2. 静态资产的版本管理

    使用缓存为静态资产提供服务可以使页面的加载速度非常快，但它有一个重要的注意事项。当你更新文件时，客户端不会得到该文件的最新版本，因为它没有向服务器检查该文件是否是最新的，而只是从浏览器缓存中提供该文件。

    以下是我们需要做的，以使浏览器只在文件被更新时才从服务器上获取文件。

    - 在一个带有版本的URL下提供文件。例如，foo.js应该在/js/foo-46944c7e3a9bd20cc30fdc085cae46f2.js下提供。
    - 用新的URL来更新文件的链接
    - 每当文件被更新时，更新URL的版本部分。例如，当foo.js被更新时，它现在应该在/js/foo-a3d8d7780349a12d739799e9aa7d2623.js下提供。

    当文件被更新时，客户端将从服务器请求该文件，因为该页面将有一个指向不同URL的链接，所以浏览器不会使用其缓存。如果一个文件没有被更新，它的版本（因此它的URL）将不会改变，客户端将继续使用该文件的缓存。

    通常情况下，我们需要手动完成所有这些工作，但Spring支持这些开箱即用，包括计算每个文件的哈希值并将其追加到URL中。让我们看看如何配置我们的Spring应用程序来为我们做这些事。

    1. MVC在一个带有版本的URL下提供服务

        我们需要为路径添加一个VersionResourceResolver，以便在URL中使用更新的版本字符串来提供该路径下的文件。

        ```java
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/js/**")
                    .addResourceLocations("/js/")
                    .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                    .resourceChain(false)
                    .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
        }
        ```

        这里我们使用一个内容版本策略。/js文件夹中的每个文件都将在一个URL下提供，该URL有一个从其内容计算出来的版本。这就是所谓的指纹识别。例如，foo.js现在将在URL /js/foo-46944c7e3a9bd20cc30fdc085cae46f2.js下提供。

        有了这个配置，当客户端对<http://localhost:8080/js/46944c7e3a9bd20cc30fdc085cae46f2.js> 的请求。

        `curl -i http://localhost:8080/js/foo-46944c7e3a9bd20cc30fdc085cae46f2.js`

        服务器会用一个Cache-Control头来响应，告诉客户端浏览器将该文件缓存一年。

        ```log
        HTTP/1.1 200 OK
        Server: Apache-Coyote/1.1
        Last-Modified: Tue, 09 Aug 2016 06:43:26 GMT
        Cache-Control: max-age=31536000
        ```

    2. Spring Boot

        要在Spring Boot中启用同样的基于内容的版本管理，我们只需在[spring.resources.chain.strategy.content](https://github.com/spring-projects/spring-boot/blob/bb568c5bffcf70169245d749f3642bfd9dd33143/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration.java#L532)属性命名空间下使用一些配置。例如，我们可以通过添加以下配置来实现与之前相同的结果。

        ```properties
        spring.resources.chain.strategy.content.enabled=true
        spring.resources.chain.strategy.content.paths=/**
        ```

        与Java配置类似，这可以为所有与/**路径模式匹配的资产启用基于内容的版本控制。

    3. 用新的URL更新链接

        在我们将版本插入到URL中之前，我们可以使用一个简单的脚本标签来导入foo.js。

        `<script type="text/javascript" src="/js/foo.js">`

        现在，我们在一个带有版本的URL下提供相同的文件，我们需要在页面上反映它。

        `<script type="text/javascript" src="<em>/js/foo-46944c7e3a9bd20cc30fdc085cae46f2.js</em>">`

        处理所有这些长路径会变得很乏味。Spring为这个问题提供了一个更好的解决方案。我们可以使用ResourceUrlEncodingFilter和JSTL的url标签来重写带版本的链接的URL。

        ResourceURLEncodingFilter可以像往常一样在web.xml下注册。

        ```xml
        <filter>
            <filter-name>resourceUrlEncodingFilter</filter-name>
            <filter-class>
                org.springframework.web.servlet.resource.ResourceUrlEncodingFilter
            </filter-class>
        </filter>
        <filter-mapping>
            <filter-name>resourceUrlEncodingFilter</filter-name>
            <url-pattern>/*</url-pattern>
        </filter-mapping>
        ```

        在我们使用url标签之前，JSTL核心标签库需要被导入我们的JSP页面。

        `<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>`

        然后，我们可以使用url标签来导入foo.js，如下所示。

        `<script type="text/javascript" src="<c:url value="/js/foo.js" />">`

        当这个JSP页面被渲染时，文件的URL被正确改写，以包含其中的版本。

        `<script type="text/javascript" src="/js/foo-46944c7e3a9bd20cc30fdc085cae46f2.js">`

    4. 更新URL的版本部分

        每当一个文件被更新时，它的版本会被再次计算，并且文件会在一个包含新版本的URL下提供。我们不需要为此做任何额外的工作，VersionResourceResolver为我们处理这个问题。

3. 修复CSS链接

    CSS文件可以通过使用@import指令导入其他CSS文件。例如，myCss.css文件导入另一个.css文件。

    `@import "another.css";`

    这通常会给版本化的静态资产带来问题，因为浏览器会对another.css文件进行请求，但该文件是在一个版本化的路径下提供的，例如another-9556ab93ae179f87b178cfad96a6ab72.css。

    为了解决这个问题并向正确的路径发出请求，我们需要在资源处理程序配置中引入CssLinkResourceTransformer。

    ```java
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/", "classpath:/other-resources/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(false)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))
                .addTransformer(new CssLinkResourceTransformer());
    }
    ```

    这修改了myCss.css的内容，并将导入语句换成了以下内容。

    `@import "another-9556ab93ae179f87b178cfad96a6ab72.css";`

4. 结论

    利用HTTP缓存是对网站性能的巨大提升，但在使用缓存时要避免提供陈旧的资源可能会很麻烦。

    在这篇文章中，我们实现了一个很好的策略，在用Spring MVC提供静态资产时使用HTTP缓存，并在文件更新时破坏缓存。
