# [Spring MVC内容协商](https://www.baeldung.com/spring-mvc-content-negotiation-json-xml)

这篇文章描述了如何在Spring MVC项目中实现内容协商。

一般来说，有三种选择来确定请求的媒体类型。

- （已废弃）在请求中使用URL后缀（扩展名）（例如.xml/.json）。
- 在请求中使用URL参数(如?format=json)
- 在请求中使用接受头

默认情况下，这是Spring内容协商管理器将尝试使用这三种策略的顺序。如果这些都没有启用，我们可以指定回退到一个默认的内容类型。

1. 内容协商策略

    让我们从必要的依赖性开始--我们正在使用JSON和XML表示法，所以在这篇文章中，我们将使用Jackson来表示JSON。

    ```xml
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>2.17.2</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.2</version>
    </dependency>
    ```

    对于XML支持，我们可以使用JAXB、XStream或较新的Jackson-XML支持。

    由于我们已经在前面关于[HttpMessageConverters](https://www.baeldung.com/spring-httpmessageconverter-rest)的文章中解释了Accept头的使用，所以让我们深入关注前两种策略。

2. URL后缀策略

    在Spring Boot 2.6.x版本中，针对注册的Spring MVC处理程序映射匹配请求路径的默认策略已经从AntPathMatcher变为PathPatternParser。

    由于PathPatternParser不支持后缀模式匹配，在使用这个策略之前，我们首先需要使用传统的路径匹配器。

    我们可以在application.properties文件中添加spring.mvc.pathmatch.match-strategy，将默认值调回AntPathMatcher。

    默认情况下，该策略是禁用的，我们需要在application.properties中设置spring.mvc.pathmatch.use-suffix-pattern为true来启用它。

    ```properties
    spring.mvc.pathmatch.use-suffix-pattern=true
    spring.mvc.pathmatch.matching-strategy=ant-path-matcher
    ```

    一旦启用，框架可以从URL中检查路径扩展，以确定输出内容类型。

    在进行配置之前，让我们快速看一下一个例子。在一个典型的Spring控制器中，我们有以下简单的API方法实现。

    ```java
    @RequestMapping(
    value = "/employee/{id}", 
    produces = { "application/json", "application/xml" }, 
    method = RequestMethod.GET)
    public @ResponseBody Employee getEmployeeById(@PathVariable long id) {
        return employeeMap.get(id);
    }
    ```

    让我们通过利用JSON扩展指定资源的媒体类型来调用它。

    `curl http://localhost:8080/spring-mvc-basics/employee/10.json`

    下面是我们使用JSON扩展可能得到的结果。

    ```json
    {
        "id": 10,
        "name": "Test Employee",
        "contactNumber": "999-999-9999"
    }
    ```

    下面是用XML表示的请求-响应的样子。

    `curl http://localhost:8080/spring-mvc-basics/employee/10.xml`

    响应主体。

    ```xml
    <employee>
        <contactNumber>999-999-9999</contactNumber>
        <id>10</id>
        <name>Test Employee</name>
    </employee>
    ```

    现在，如果我们不使用任何扩展或使用未配置的扩展，将返回默认的内容类型。

    `curl http://localhost:8080/spring-mvc-basics/employee/10`

    现在让我们来看看如何设置这个策略--同时使用Java和XML配置。

    1. Java配置

        ```java
        // com.baeldung.spring.web.config.WebConfig.java
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
            configurer.favorPathExtension(true)。
            favorParameter(false).
            ignoreAcceptHeader(true)。
            useJaf(false)。
            defaultContentType（MediaType.APPLICATION_JSON）。
        }
        ```

        让我们来看看细节。

        首先，我们要启用路径扩展策略。值得一提的是，从[Spring Framework 5.2.4](https://github.com/spring-projects/spring-framework/issues/24179)开始，favorPathExtension(boolean)方法已被弃用，以便不鼓励使用路径扩展来进行内容协商。

        然后，我们将禁用URL参数策略和接受头策略--因为我们只想依靠路径扩展的方式来确定内容的类型。

        然后，我们要关闭Java激活框架；如果传入的请求不符合我们配置的任何策略，JAF可以作为一种回退机制来选择输出格式。我们关闭它是因为我们要将JSON配置为默认的内容类型。请注意，从Spring Framework 5开始，useJaf()方法已被废弃。

        最后--我们要把JSON设置为默认的。这意味着如果两个策略都不匹配，所有传入的请求将被映射到提供JSON的控制器方法。

    2. XML配置

        我们也来看看同样的配置，只是使用XML。

        ```xml
        <bean id="contentNegotiationManager" 
        class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
            <property name="favorPathExtension" value="true" />
            <property name="favorParameter" value="false"/>
            <property name="ignoreAcceptHeader" value="true" />
            <property name="defaultContentType" value="application/json" />
            <property name="useJaf" value="false" />
        </bean>
        ```

3. URL参数策略

    我们在上一节中已经使用了路径扩展--现在让我们设置Spring MVC来利用路径参数。

    我们可以通过将 favorParameter 属性的值设置为 true 来启用这一策略。

    让我们快速看一下这将如何与我们之前的例子一起工作。

    `curl http://localhost:8080/spring-mvc-basics/employee/10?mediaType=json`

    以下是JSON响应主体的内容：

    ```java
    {
        "id": 10,
        "name": "Test Employee",
        "contactNumber": "999-999-9999"
    }
    ```

    如果我们使用XML参数，输出将是XML形式的。

    `curl http://localhost:8080/spring-mvc-basics/employee/10?mediaType=xml`

    响应机构：

    ```xml
    <employee>
        <contactNumber>999-999-9999</contactNumber>
        <id>10</id>
        <name>Test Employee</name>
    </employee>
    ```

    现在我们来做配置--同样，先用Java，然后用XML。

    1. Java配置

        ```java
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
            configurer.favorPathExtension(false).
            favorParameter(true).
            parameterName("mediaType").
            ignoreAcceptHeader(true).
            useJaf(false).
            defaultContentType(MediaType.APPLICATION_JSON).
            mediaType("xml", MediaType.APPLICATION_XML). 
            mediaType("json", MediaType.APPLICATION_JSON); 
        }
        ```

        让我们阅读一下这个配置。

        首先，当然，路径扩展和接受头的策略被禁用（以及JAF）。

        其余的配置都是一样的。

    2. XML配置

        ```xml
        <bean id="contentNegotiationManager" 
        class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
            <property name="favorPathExtension" value="false" />
            <property name="favorParameter" value="true"/>
            <property name="parameterName" value="mediaType"/>
            <property name="ignoreAcceptHeader" value="true" />
            <property name="defaultContentType" value="application/json" />
            <property name="useJaf" value="false" />

            <property name="mediaTypes">
                <map>
                    <entry key="json" value="application/json" />
                    <entry key="xml" value="application/xml" />
                </map>
            </property>
        </bean>
        ```

        此外，我们可以同时启用两种策略（扩展和参数）：

        ```java
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
            configurer.favorPathExtension(true).
            favorParameter(true).
            parameterName("mediaType").
            ignoreAcceptHeader(true).
            useJaf(false).
            defaultContentType(MediaType.APPLICATION_JSON).
            mediaType("xml", MediaType.APPLICATION_XML). 
            mediaType("json", MediaType.APPLICATION_JSON); 
        }
        ```

        在这种情况下，Spring将首先查找路径扩展，如果路径扩展不存在，则将查找路径参数。如果在输入请求中这两个都不可用，那么将返回默认的内容类型。

4. 接受Header策略

    如果启用了Accept Header，SpringMVC将在传入请求中查找其值，以确定表示类型。

    我们必须将ignoreAcceptHeader的值设置为false以启用此方法，并且我们禁用了其他两个策略，以便我们知道我们只依赖Accept头。

    1. Java配置

        ```java
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
            configurer.favorPathExtension(true).
            favorParameter(false).
            parameterName("mediaType").
            ignoreAcceptHeader(false).
            useJaf(false).
            defaultContentType(MediaType.APPLICATION_JSON).
            mediaType("xml", MediaType.APPLICATION_XML). 
            mediaType("json", MediaType.APPLICATION_JSON); 
        }
        ```

    2. XML配置

        ```xml
        <bean id="contentNegotiationManager" 
        class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
            <property name="favorPathExtension" value="true" />
            <property name="favorParameter" value="false"/>
            <property name="parameterName" value="mediaType"/>
            <property name="ignoreAcceptHeader" value="false" />
            <property name="defaultContentType" value="application/json" />
            <property name="useJaf" value="false" />

            <property name="mediaTypes">
                <map>
                    <entry key="json" value="application/json" />
                    <entry key="xml" value="application/xml" />
                </map>
            </property>
        </bean>
        ```

   最后，我们需要通过将内容协商管理器插入到整体配置中来打开它：

    `<mvc:annotation-driven content-negotiation-manager="contentNegotiationManager" />`

5. 结论

    我们完成了。我们研究了Spring MVC中内容协商的工作原理，并重点讨论了几个设置示例，以使用各种策略来确定内容类型。
