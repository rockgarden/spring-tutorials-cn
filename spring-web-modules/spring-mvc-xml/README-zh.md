# Spring MVC XML

本模块包含关于Spring MVC与XML配置的文章。

## JSP指南

1. 概述

    JavaServer Pages（JSP）允许使用Java和Java Servlet将动态内容注入静态内容中。我们可以向Java Servlet发出请求，执行相关的逻辑，并在服务器端渲染一个特定的视图，供客户端使用。本文将对使用Java 8和Jave 7 EE的JavaServer Pages进行全面的介绍。

    我们将首先探讨与JSP相关的几个关键概念：即动态和静态内容的区别、JSP生命周期、JSP语法以及指令和编译时创建的隐含对象

2. Java服务器页面

    JavaServer Pages（JSP）使Java特定的数据能够被传入或置于一个.jsp视图中，并在客户端消费。

    JSP文件本质上是.html文件，有一些额外的语法，以及一些微小的初始差异：

    - .html的后缀被替换为.jsp（它被认为是一个.jsp文件类型）和
    - 以下标签被添加到.html标记元素的顶部：
    `<%@ page contentType="text/html;charset=UTF-8" language="java" %>`

    让我们回顾一下JSP中的一些关键概念。

    1. JSP 语法

        有两种方法可以在.jsp中添加Java代码。首先，我们可以使用基本的Java Scriptlet语法，即把Java代码块放在两个Scriptlet标签中：

        `<% Java code here %>`

        第二种方法是专门针对XML的：

        ```xml
        <jsp:scriptlet>
            Java code here
        </jsp:scriptlet>
        ```

        重要的是，通过使用if、then和else子句，然后用这些括号包装相关的标记块，可以在JSP的客户端使用条件逻辑。

        ```html
        <% if (doodad) {%>
            <div>Doodad!</div>
        <% } else { %>
            <p>Hello!</p>
        <% } %>
        ```

        例如，如果doodad为真，我们就会显示第一个div元素，否则我们就会显示第二个p元素!

    2. 静态和动态内容

        静态网页内容是独立于RESTful、SOAP、HTTP、HTTPS请求或其他用户提交的信息而被消费的固定资产。

        然而，静态内容是固定的，不被用户输入所修改。动态网页内容是那些对用户行为或信息做出反应、被其修改或改变的资产。

        JSP技术允许在动态和静态内容之间进行明确的责任分离。

        服务器（servlet）管理动态内容，而客户端（实际的.jsp页面）是动态内容被注入的静态环境。

        让我们来看看由JSP创建的隐含对象，这些对象允许你在服务器端访问与JSP有关的数据!

    3. 隐式对象

        隐式对象是由JSP引擎在编译过程中自动生成的。

        隐式对象包括HttpRequest和HttpResponse对象，并暴露了各种服务器端的功能，以便在你的Servlet中使用，并与你的.jsp进行交互！下面是创建的隐式对象的列表：

        **请求**
        request属于javax.servlet.http.HttpServletRequest类。request对象暴露了所有用户输入的数据，并使其在服务器端可用。

        **响应**
        response属于javax.servlet.http.HttpServletResponse类，它决定了在一个请求发出后，客户端会传回什么。

        让我们仔细看看请求和响应的隐含对象，因为它们是最重要和最大量使用的对象。

        下面的例子演示了一个非常简单的、不完整的、处理GET请求的servlet方法。我省略了大部分的细节，这样我们就可以专注于如何使用请求和响应对象：

        ```java
        protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
            String message = request.getParameter("message");
            response.setContentType("text/html");
            . . .
        }
        ```

        首先，我们看到request和response对象被作为参数传入方法，使它们在方法的范围内可用。

        我们可以使用.getParameter()函数访问请求参数。上面，我们抓取了消息参数并初始化了一个字符串变量，这样我们就可以在服务器端逻辑中使用它。我们还可以访问响应对象，它决定了传递到视图中的数据是什么以及如何传递。

        上面我们对它设置了内容类型。我们不需要返回响应对象来让它的有效载荷在渲染时显示在JSP页面上!

        **out**
        out属于javax.servlet.jsp.JspWriter类，用于向客户端写入内容。

        至少有两种方法可以打印到你的JSP页面，这里值得讨论这两种方法。out是自动创建的，允许你写入内存，然后写入响应对象：

        ```java
        out.print("hello")；
        out.println("world")；
        ```

        第二种方法可以有更高的性能，因为它允许你直接写到响应对象! 这里，我们使用PrintWriter：

        ```java
        PrintWriter out = response.getWriter();
        out.println("Hello World");
        ```

    4. 其他隐式对象

        这里有一些其他的Implicit对象，也是值得我们了解的!

        **会话**
        session属于javax.servlet.http.HttpSession类，在会话期间保持用户数据。

        **应用**
        application属于javax.servlet.ServletContext类，存储初始化时设置的应用范围内的参数或需要在应用范围内访问的参数。

        **异常**
        exception属于javax.servlet.jsp.JspException类，用于在有`<%@ page isErrorPage="true" %>`标签的JSP页面上显示错误信息。

        **页**
        page属于java.lang.Object类，允许人们访问或引用当前的Servlet信息。

        **pageContext**
        pageContext属于java.servlet.jsp.PageContext类，默认为页面范围，但可用于访问请求、应用程序和会话属性。

        **配置**
        config属于javax.servlet.ServletConfig类，是Servlet的配置对象，允许人们获取Servlet的上下文、名称和配置参数。

        现在我们已经涵盖了JSP提供的隐含对象，让我们转向允许.jsp页面（间接地）访问这些对象的指令。

    5. 指令

        JSP提供了开箱即用的指令，可以用来为我们的JSP文件指定核心功能。JSP指令有两个部分：（1）指令本身和（2）该指令的属性，该属性被赋予了一个值。

        可以使用指令标签引用的三种指令是：`<%@ page ... %>`，它定义了JSP的依赖性和属性，包括内容类型和语言；`<%@ include ... %>`，它指定了要使用的导入或文件；`<%@ taglib ... %>`，它指定了一个定义自定义动作的标签库，供页面使用。

        因此，作为一个例子，一个页面指令将使用JSP标签以如下方式指定： `<%@ page attribute="value" %>`

        而且，我们可以用XML来做，如下： `<jsp:directive.page attribute="value" />`

    6. 页面指令的属性

        有很多属性可以在一个页面指令中声明：

        **autoFlush** <%@ page autoFlush="false" %>

        autoFlush控制缓冲区的输出，当达到缓冲区的大小时将其清除。默认值是true。

        **buffer** <%@ page buffer="19kb" %>

        buffer设置我们的JSP页面所使用的缓冲区的大小。默认值是8kb。

        **errorPage** <%@ page errorPage="errHandler.jsp" %> >。

        errorPage指定一个JSP页面为错误页面。

        **extends** <%@ page extends="org.apache.jasper.runtime.HttpJspBase" %>

        extends指定了相应的Servlet代码的超级类。

        **info** <%@ page info="This is my JSP!" %>

        info用于为JSP设置一个基于文本的描述。

        **isELIgnored** <%@ page isELIgnored="true" %>

        isELIgnored说明页面是否会忽略JSP中的表达式语言（EL）。EL使表现层能够与Java管理Bean进行通信，并使用${...}语法，虽然我们不会在这里讨论EL的细枝末节，但下面有几个例子足以建立我们的JSP应用实例 isELIgnored的默认值是false。

        **isErrorPage** <%@ page isErrorPage="true" %>。

        isErrorPage表示一个页面是否是一个错误页面。如果我们在应用程序中为我们的页面创建一个错误处理程序，我们必须指定一个错误页面。

        **isThreadSafe** <%@ page isThreadSafe="false" %>

        isThreadSafe的默认值是true。isThreadSafe决定了JSP是否可以使用Servlet多线程。一般来说，你永远不会想关掉这个功能。

        **language** <%@ page language="java" %>

        language决定了在JSP中使用什么脚本语言。默认值是Java。

        **session** <%@ page session="value"%>

        session决定了是否要保持HTTP会话。它的默认值是true，可以接受true或false的值。

        **trimDirectiveWhitespaces** <%@ page trimDirectiveWhitespaces ="true"%>

        trimDirectiveWhitespaces将JSP页面中的白色空间剥离出来，在编译时将代码压缩成一个更紧凑的块。将此值设置为 "true" 可能有助于减少JSP代码的大小。默认值是false。

3. 三个例子

    现在我们已经回顾了JSP的核心概念，让我们把这些概念应用到一些基本的例子中，这将有助于你启动和运行你的第一个JSP服务的servlet!

    有三种主要方式可以将Java注入到.jsp中，下面我们将使用Java 8和Jakarta EE中的本地功能来探讨这些方式。

    首先，我们将在服务器端渲染我们的标记以显示在客户端。第二，我们将研究如何在独立于javax.servlet.http的请求和响应对象的.jsp文件中直接添加Java代码。

    第三，我们将演示如何将HttpServletRequest转发到一个特定的.jsp，并将服务器端处理的Java绑定到它。

    让我们在Eclipse中使用File/New/Project/Web/Dynamic web project/类型来设置我们的项目，以便在Tomcat中进行托管! 创建项目后，你应该看到：

    ```txt
    |-project
    |- WebContent
        |- META-INF
        |- MANIFEST.MF
        |- WEB-INF
        |- lib
        |- src
    ```

    我们要在应用程序结构中添加一些文件，这样我们最终会有：

    ```txt
    |-project
    |- WebContent
        |- META-INF
        |- MANIFEST.MF
        |- WEB-INF
        |-lib
        *-web.xml
            |- ExampleTree.jsp
            |- ExampleTwo.jsp
            *- index.jsp
        |- src
            |- com
            |- baeldung
                *- ExampleOne.java
                *- ExampleThree.java
    ```

    让我们来设置index.jsp，当我们在Tomcat 8中访问URL上下文时将会显示出来：

    这里有三个a，每个都链接到我们将在下面4.1到4.4节中讨论的一个例子。

    我们还需要确保我们的web.xml已经设置好了：

    ```xml
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
        <welcome-file>index.htm</welcome-file>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
    <servlet>
        <servlet-name>ExampleOne</servlet-name>
        <servlet-class>com.baeldung.ExampleOne</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>ExampleOne</servlet-name>
        <url-pattern>/ExampleOne</url-pattern>
    </servlet-mapping>
    ```

    这里的一个主要注意点是--如何正确地将我们的每个servlet映射到一个特定的servlet-mapping。这样做可以将每个servlet与一个特定的端点联系起来，在那里它可以被消费 现在，我们再来看看下面的其他文件吧

    在这个例子中，我们实际上将跳过建立一个.jsp文件!

    1. 在Servlet中渲染HTML

        在这个例子中，我们将跳过建立一个.jsp文件!

        相反，我们将为我们的标记创建一个字符串表示，然后在ExampleOne Servlet收到一个GET请求后用PrintWriter将其写入GET响应：

        jsp/ExampleOne.java

        我们在这里所做的是通过我们的Servlet请求处理直接注入我们的标记。我们没有使用JSP标签，而是纯粹在服务器端生成我们的HTML，以及要插入的任何和所有Java特定的数据，没有静态的JSP!

        早些时候，我们回顾了out对象，这是JspWriter的一个特征。

        上面，我使用了PrintWriter对象，它直接写到了响应对象。

        JspWriter实际上缓冲了要写进内存的字符串，然后在内存缓冲区被刷新后写到响应对象。

        PrintWriter已经附在响应对象上了。由于这些原因，在上面和下面的例子中，我倾向于直接写到响应对象。

    2. JSP静态内容中的Java

        这里我们创建了一个名为ExampleTwo.jsp的JSP文件，带有JSP标签。如上所述，这允许将Java直接添加到我们的标记中。在这里，我们随机地打印一个String[]的元素：

        webapp/jsp/ExampleTwo.java

        在上面，你会看到JSP标签内的变量声明对象：type variableName 和 initialization，就像普通的Java。

        我把上面的例子包括在内，是为了演示如何在不求助于特定的Servlet的情况下将Java添加到静态页面中。在这里，Java被简单地添加到一个页面中，而JSP的生命周期则负责处理其余的事情。

    3. 带有转发功能的JSP

        现在，我们的最后一个例子，也是涉及最多的一个例子! 在这里，我们将在ExampleThree上使用@WebServlet注解，这样就不需要在server.xml中进行Servlet映射了。

        jsp/ExampleThree.java

        ExampleThree接收一个作为消息传递的URL参数，将该参数绑定到请求对象上，然后将该请求对象重定向到ExampleThree.jsp。

        因此，我们不仅实现了真正的动态网络体验，而且还在一个包含多个.jsp文件的应用程序中实现了这一点。

        getRequestDispatcher().forward()是一个简单的方法来确保正确的.jsp页面被呈现。

        所有与请求对象绑定的数据以其（.jsp文件）的方式发送，然后将被显示出来 下面是我们如何处理最后一部分的内容：

        webapp/jsp/ExampleThree.jsp

        注意在ExampleThree.jsp的顶部添加的JSP标签。你会注意到，我在这里调换了JSP标签。我正在使用表达式语言（我之前提到过）来渲染我们的设定参数（它被绑定为${text}）!

    4. 试试吧!

        现在，我们将把我们的应用程序导出到一个.war中，以便在Tomcat 8中启动和托管! 找到你的server.xml，我们将更新我们的Context到：

        `<Context path="/spring-mvc-xml" docBase="${catalina.home}/webapps/spring-mvc-xml"></Context>`

        > TODO: IDE附带的Tomcat无法配置server.xml。

        这将允许我们在 <http://localhost:8080/spring-mvc-xml/jsp/index.jsp> 上访问我们的servlets和JSP!

4. 总结

    我们已经覆盖了相当多的领域! 我们已经了解了什么是JavaServer Pages，它们被引入的目的是什么，它们的生命周期，如何创建它们，最后还有一些实现它们的不同方法。

## Relevant Articles

- [Java Session Timeout](https://www.baeldung.com/servlet-session-timeout)
- [Returning Image/Media Data with Spring MVC](https://www.baeldung.com/spring-mvc-image-media-data)
- [Geolocation by IP in Java](https://www.baeldung.com/geolocation-by-ip-with-maxmind)
- [x] [Guide to JavaServer Pages (JSP)](https://www.baeldung.com/jsp)
- [web.xml vs Initializer with Spring](https://www.baeldung.com/spring-xml-vs-java-config)
- [A Java Web Application Without a web.xml](https://www.baeldung.com/java-web-app-without-web-xml)
- [Introduction to Servlets and Servlet Containers](https://www.baeldung.com/java-servlets-containers-intro)
- More articles: [[more -->]](../spring-mvc-xml-2)

## Spring MVC with XML Configuration Example Project

- access a sample jsp page at: `http://localhost:8080/spring-mvc-xml/sample.html`

## Code

从以下网站获取工作副本：[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-web-modules/spring-mvc-xml)。
