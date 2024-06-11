# [WebJars简介](https://www.baeldung.com/maven-webjars)

1. 概述

    本教程介绍了WebJars以及如何在Java应用程序中使用它们。

    简单地说，WebJars是打包成JAR归档文件的客户端依赖项。它们可以与大多数JVM容器和Web框架一起使用。

    下面是几个流行的WebJars： Twitter Bootstrap、jQuery、Angular JS、Chart.js等；完整的[列表](http://www.webjars.org/)可在官方网站上找到。

2. 为什么使用WebJars？

    这个问题有一个非常简单的答案--因为它很容易。

    手动添加和管理客户端的依赖关系往往会导致代码库难以维护。

    而且，大多数Java开发者更愿意使用Maven和Gradle作为构建和依赖管理工具。

    WebJars解决的主要问题是使客户端依赖在Maven中心可用，并可用于任何标准Maven项目。

    以下是WebJars的几个有趣的优点：

    - 我们可以在基于JVM的网络应用中明确而轻松地管理客户端依赖关系
    - 我们可以在任何常用的构建工具中使用它们，例如 Maven、Gradle等。
    - WebJars的行为与其他Maven的依赖关系一样，这意味着我们也能获得横向的依赖关系。

3. Maven依赖关系

    让我们直接开始吧，把Twitter Bootstrap和jQuery添加到pom.xml中：

    ```xml
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>bootstrap</artifactId>
        <version>3.3.7-1</version>
    </dependency>
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>jquery</artifactId>
        <version>3.1.1</version>
    </dependency>
    ```

    现在[Twitter Bootstrap](https://mvnrepository.com/artifact/org.webjars/bootstrap)和[jQuery](https://mvnrepository.com/artifact/org.webjars/jquery)在项目classpath中可用；我们可以简单地引用它们并在我们的应用程序中使用它们。

    > 注意：你可以在Maven中心查看最新版本的Twitter Bootstrap和jQuery依赖项。

4. 简单的应用程序

    在定义了这两个WebJar依赖项之后，我们现在来设置一个简单的Spring MVC项目，以便能够使用客户端依赖项。

    在这之前，我们必须了解WebJar与Spring无关，我们在这里使用Spring是因为它是建立MVC项目的一个非常快速和简单的方法。

    这里是一个开始设置Spring MVC和Spring Boot项目的好地方。

    而且，随着简单项目的建立，我们将为我们的新客户依赖性定义一些映射：

    ```java
    @Configuration
    @EnableWebMvc
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
            .addResourceHandler("/webjars/**")
            .addResourceLocations("/webjars/");
        }
    }
    ```

    当然，我们也可以通过XML来做这件事：

    `<mvc:resources mapping="/webjars/**" location="/webjars/"/>`

5. 版本无关的依赖关系

    当使用Spring Framework 4.2或更高版本时，它将自动检测classpath上的webjars-locator库，并使用它来自动解析任何WebJars资产的版本。

    为了启用这一功能，我们将添加webjars-locator库作为应用程序的依赖项：

    ```xml
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>webjars-locator</artifactId>
        <version>0.30</version>
    </dependency>
    ```

    在这种情况下，我们可以不使用版本来引用WebJars资产；请看下一节的几个实际例子。

6. 客户端上的WebJars

    让我们为我们的应用程序添加一个简单的普通HTML欢迎页（这是index.html）：

    templates/index.html

    现在我们可以在项目中使用Twitter Bootstrap和jQuery了--让我们在欢迎页面中使用这两种工具，首先是Bootstrap：

    `<script src="/webjars/bootstrap/3.3.7-1/js/bootstrap.min.js"></script>`

    以获得一个不受版本限制的方法：

    `<script src="/webjars/bootstrap/js/bootstrap.min.js"></script>`

    添加jQuery：

    `<script src="/webjars/jquery/3.1.1/jquery.min.js"></script>`

    和版本无关的方法：

    `<script src="/webjars/jquery/jquery.min.js"></script>`

7. 测试

    现在我们已经在我们的HTML页面中添加了Twitter Bootstrap和jQuery，让我们来测试它们。

    我们将在我们的页面中添加一个Bootstrap警报：

    ```html
    <div class="container"><br/>
        <div class="alert alert-success">         
            <strong>Success!</strong> It is working as we expected.
        </div>
    </div>
    ```

    注意，这里假定我们对Twitter Bootstrap有一些基本的了解；这里有官方的[入门指南](https://getbootstrap.com/components/)。

    这将显示一个如下所示的警报，这意味着我们已经成功地将Twitter Bootstrap添加到我们的classpath。

    现在让我们使用jQuery。我们将为这个警报添加一个关闭按钮：

    `<a href="#" class="close" data-dismiss="alert" aria-label="close">×</a>`

    现在我们需要添加jQuery和bootstrap.min.js来实现关闭按钮的功能，所以把它们添加到index.html的body标签里，如下图：

    ```html
    <script src="/webjars/jquery/3.1.1/jquery.min.js"></script>
    <script src="/webjars/bootstrap/3.3.7-1/js/bootstrap.min.js"></script>
    ```

    注意：如果你使用的是版本无关的方法，请确保只删除路径中的版本，否则，相对导入可能无法工作。

8. 总结

    在这篇简短的文章中，我们着重介绍了在基于JVM的项目中使用WebJars的基本知识，这使得开发和维护变得更加容易。

    我们实现了一个支持Spring Boot的项目，并在项目中使用了Twitter Bootstrap和jQuery，使用WebJars。
