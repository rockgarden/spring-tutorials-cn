# 使用JavaServer页面的Spring Boot

1. 概述

    在构建Web应用时，JavaServer Pages（[JSP](https://www.baeldung.com/jsp)）是我们可以用来作为HTML页面的模板机制的一种选择。

    另一方面，Spring Boot是一个流行的框架，我们可以用它来引导我们的Web应用。

    在本教程中，我们将看到如何使用JSP和Spring Boot来构建一个Web应用。

    首先，我们将看到如何设置我们的应用程序以在不同的部署场景中工作。然后我们将看看JSP的一些常见用法。最后，我们将探讨在打包我们的应用程序时有哪些选择。

    这里有一个快速的附带说明，就是JSP本身有局限性，如果与Spring Boot结合起来，就更有局限性。所以，我们应该考虑用[Thymeleaf](https://www.baeldung.com/thymeleaf-in-spring-mvc)或[FreeMarker](https://www.baeldung.com/freemarker-in-spring-mvc-tutorial)作为JSP的更好替代品。

2. Maven的依赖性

    让我们看看我们需要哪些依赖性来支持带有JSP的Spring Boot。

    我们还将注意到将我们的应用程序作为独立的应用程序运行和在Web容器中运行之间的微妙关系。

    1. 作为一个独立的应用程序运行

        首先，让我们包括spring-boot-starter-web依赖项。

        这个依赖项提供了所有的核心要求，使Web应用程序与默认的嵌入式Tomcat Servlet容器一起在Spring Boot中运行。

        请查看我们的文章《[比较SpringBoot中的嵌入式Servlet容器](https://www.baeldung.com/spring-boot-servlet-containers)》，了解更多关于如何配置Tomcat以外的嵌入式Servlet容器的信息。

        我们应该特别注意，Undertow在作为嵌入式Servlet容器使用时不支持JSP。

        接下来，我们需要包括[tomcat-embed-jasper](https://search.maven.org/classic/#search%7Cga%7C1%7Cg%3Aorg.apache.tomcat.embed%20a%3Atomcat-embed-jasper)依赖项，以使我们的应用程序能够编译和渲染JSP页面：

        ```xml
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-jasper</artifactId>
        </dependency>
        ```

        虽然上述两个依赖项可以手动提供，但通常让Spring Boot管理这些依赖项的版本，而我们只需管理Spring Boot的版本，效果会更好。

        这种版本管理可以通过使用Spring Boot父级POM来完成，如我们的文章《[Bootstrap一个简单的应用程序](/spring-boot-bootstrap/README-zh.md#spring-boot教程–引导一个简单的应用程序)》中所示，也可以通过使用依赖性管理来完成，如我们的文章《[使用自定义父级的Spring Boot依赖性管理](https://www.baeldung.com/spring-boot-dependency-management-custom-parent)》所示。

        最后，我们需要包括[jstl](https://search.maven.org/classic/#search%7Cga%7C1%7Cg%3Ajavax.servlet%20a%3Ajstl)库，它将为我们的JSP页面提供所需的JSTL标签支持：

        ```xml
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        ```

    2. 在网络容器（Tomcat）中运行

        在Tomcat网络容器中运行时，我们仍然需要上述依赖关系。

        然而，为了避免我们的应用程序提供的依赖与Tomcat运行时提供的依赖发生冲突，我们需要在提供的范围内设置两个依赖：

        ```xml
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-jasper</artifactId>
            <version>9.0.44</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <version>2.4.4</version>
            <scope>provided</scope>
        </dependency>
        ```

        注意，我们必须明确定义[spring-boot-starter-tomcat](https://search.maven.org/classic/#search%7Cga%7C1%7Cg%3Aorg.springframework.boot%20a%3Aspring-boot-starter-tomcat)，并将其标记为所提供的范围。这是因为它已经是一个由 spring-boot-starter-web 提供的横向依赖关系。

3. 视图解析器配置

    按照惯例，我们将JSP文件放在${project.baseir}/main/webapp/WEB-INF/jsp/目录中。

    我们需要通过在application.properties文件中配置两个属性来让Spring知道在哪里找到这些JSP文件：

    ```properties
    spring.mvc.view.prefix: /WEB-INF/jsp/
    spring.mvc.view.suffix: .jsp
    ```

    编译时，Maven将确保生成的WAR文件在WEB-INF目录下有上述jsp目录，然后由我们的应用程序提供服务。

4. 引导我们的应用程序

    我们的主应用程序类将受到我们是打算作为独立应用程序还是在Web容器中运行的影响。

    当作为一个独立的应用程序运行时，我们的应用程序类将是一个简单的@SpringBootApplication注解的类，同时还有主方法：

    ```java
    @SpringBootApplication(scanBasePackages = "com.baeldung.boot.jsp")
    public class SpringBootJspApplication {

        public static void main(String[] args) {
            SpringApplication.run(SpringBootJspApplication.class);
        }
    }
    ```

    然而，如果我们需要在Web容器中进行部署，我们需要扩展SpringBootServletInitializer。

    这将我们的应用程序的Servlet、Filter和ServletContextInitializer绑定到运行时服务器上，这对我们的应用程序的运行是必要的：

    boot.jsp/SpringBootJspApplication.java

5. 服务一个简单的网页

    JSP页面依靠JavaServer Pages标准标签库（JSTL）来提供常见的模板功能，如分支、迭代和格式化，它甚至提供了一组预定义的函数。

    让我们创建一个简单的网页，显示保存在我们应用程序中的书籍列表。

    假设我们有一个BookService，帮助我们查找所有的Book对象：

    boot.jsp.dto/Book.java

    boot.jsp.service/BookService.java

    我们可以写一个Spring MVC控制器，将其作为一个网页公开：

    boot.jsp.controller/BookController.java

    请注意，上面的BookController将返回一个名为view-books的视图模板。根据我们之前在application.properties中的配置，Spring MVC会在/WEB-INF/jsp/目录下寻找view-books.jsp。

    上面的例子向我们展示了如何使用JSTL `<c:url>` 标签来链接到外部资源，如JavaScript和CSS。我们通常把这些资源放在${project.baseir}/main/resources/static/目录下。

    我们还可以看到JSTL `<c:forEach>` 标签如何被用来迭代BookController提供的books模型属性。

6. 处理表单提交

    现在让我们看看如何用JSP来处理表单提交。

    我们的BookController将需要提供MVC端点来服务于添加书籍的表单和处理表单提交：addBookView()、 addBook()。

    我们将创建/WEB-INF/jsp/add-book.jsp文件。

    我们使用`<form:form>`标签提供的modelAttribute参数，将BookController中addBookView()方法中添加的book属性绑定到表单中，进而在提交表单时填写该属性。

    由于使用这个标签，我们需要单独定义表单动作的URL，因为我们不能把标签放在标签里面。我们还使用`<form:input>`标签中的路径属性，将每个输入字段绑定到Book对象中的一个属性。

7. 处理错误

    由于使用Spring Boot和JSP的现有限制，我们不能提供自定义error.html来定制默认的/error映射。相反，我们需要创建自定义错误页面来处理不同的错误。

    1. 静态错误页

        如果我们想为不同的HTTP错误显示一个自定义的错误页面，我们可以提供一个静态错误页面。

        比方说，我们需要为应用程序抛出的所有4xx错误提供一个错误页面。我们可以简单地在${project.baseir}/main/resources/static/error/目录下放置一个名为4xx.html的文件。

        如果我们的应用程序抛出一个4xx HTTP错误，Spring将解决这个错误并返回所提供的4xx.html页面。

    2. 动态错误页面

        我们有多种方法来处理异常，以提供一个定制的错误页面以及上下文的信息。让我们看看Spring MVC如何使用@ControllerAdvice和@ExceptionHandler注解为我们提供这种支持。

        假设我们的应用程序定义了一个DuplicateBookException：

        boot.jsp.exception/DuplicateBookException.java

        另外，假设我们的BookServiceImpl类将抛出上述DuplicateBookException，如果我们试图添加两本具有相同ISBN的书：

        我们的LibraryControllerAdvice类将定义我们要处理的错误，以及我们要如何处理每个错误：

        boot.jsp.exception/LibraryControllerAdvice.java

        我们需要定义error-book.jsp文件，这样上述错误就会在这里解决。确保将其放在${project.baseir}/main/webapp/WEB-INF/jsp/目录下，因为这不再是一个静态HTML，而是一个需要编译的JSP模板。

8. 创建一个可执行文件

    如果我们计划在Tomcat等Web容器中部署我们的应用程序，选择很简单，我们将使用war打包来实现。

    然而，我们应该注意的是，==如果我们使用JSP和Spring Boot与嵌入式Servlet容器，我们就不能使用jar打包==。因此，如果作为一个独立的应用程序运行，我们唯一的选择就是war打包。

    那么，无论在哪种情况下，我们的pom.xml都需要将其打包指令设置为war：

    `<packaging>war</packaging>`

    如果我们没有使用Spring Boot父级POM来管理依赖关系，我们就需要包含[spring-boot-maven-plugin](https://www.baeldung.com/spring-boot-run-maven-vs-executable-jar)，以确保生成的war文件能够作为独立应用运行。

    > 即使用了Spring Boot父级POM来管理，也要在pom.xml添加`<build><plugins><plugin>-spring-boot-maven-plugin`，否则要运行 `mvn clean package spring-boot:repackage` 命令，否则无法生成 fat jar。

    现在我们可以用嵌入式Servlet容器运行我们的独立应用，或者简单地将生成的war文件放入Tomcat，让它为我们的应用服务。

9. 总结

    在本教程中，我们已经触及了各种主题。让我们回顾一下一些关键的考虑因素：

    - JSP包含一些固有的限制。可以考虑用Thymeleaf或FreeMarker代替。
    - 如果在Web容器上部署，记得把必要的依赖关系标记为提供。
    - 如果作为嵌入式Servlet容器使用，Undertow将不支持JSP。
    - 如果在Web容器中部署，我们的@SpringBootApplication注释类应该扩展SpringBootServletInitializer并提供必要的配置选项。
    - 我们不能用JSP覆盖默认的/error页面。相反，我们需要提供自定义的错误页面。
    - 如果我们在Spring Boot中使用JSP，那么JAR打包就不是一个选项。

## Relevant Articles

- [x] [Spring Boot With JavaServer Pages (JSP)](https://www.baeldung.com/spring-boot-jsp)

## Code

和往常一样，[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-jsp)上提供了完整的源代码和我们的示例。
