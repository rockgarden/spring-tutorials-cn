# [Spring MVC 中的处理程序适配器](https://www.baeldung.com/spring-mvc-handler-adapters)

1. 概述

    在本文中，我们将重点介绍 Spring 框架中的各种处理程序适配器实现。

2. 什么是处理程序适配器？

    处理程序适配器（HandlerAdapter）基本上是一个接口，它有助于在 Spring MVC 中以非常灵活的方式处理 HTTP 请求。

    它与 HandlerMapping 结合使用，后者将方法映射到特定 URL。

    然后，DispatcherServlet 使用 HandlerAdapter 来调用该方法。Servlet 并不直接调用方法 - 它基本上是自身与处理程序对象之间的桥梁，从而实现松散耦合设计。

    让我们来看看该接口中可用的各种方法：

    ```java
    public interface HandlerAdapter {
        boolean supports(Object handler);
        ModelAndView handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception;
        long getLastModified(HttpServletRequest request, Object handler);
    }
    ```

    supports API 用于检查特定处理程序实例是否受支持。在调用该接口的 handle() 方法之前，应首先调用该方法，以确定处理程序实例是否受支持。

    句柄 API 用于处理特定的 HTTP 请求。该方法负责通过传递 HttpServletRequest 和 HttpServletResponse 对象作为参数来调用处理程序。然后，处理程序执行应用逻辑并返回一个 ModelAndView 对象，然后由 DispatcherServlet 进行处理。

3. Maven 依赖

    让我们从需要添加到 pom.xml 的 Maven 依赖开始：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>6.1.5</version>
    </dependency>
    ```

    可在此处找到最新版本的 spring-webmvc 工具。

4. 处理程序适配器类型

    1. 简单控制器适配器 SimpleControllerHandlerAdapter

        这是 Spring MVC 注册的默认处理程序适配器。它处理实现控制器接口的类，用于将请求转发给控制器对象。

        如果网络应用程序只使用控制器，那么我们就不需要配置任何 HandlerAdapter，因为框架会将该类用作处理请求的默认适配器。

        让我们使用旧式控制器（实现控制器接口）定义一个简单的控制器类：

        ```java
        public class SimpleController implements Controller {
            @Override
            public ModelAndView handleRequest(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
                ModelAndView model = new ModelAndView("Greeting");
                model.addObject("message", "Dinesh Madhwal");
                return model;
            }
        }
        ```

        类似的 XML 配置：

        ```xml
        <beans ...>
            <bean name="/greeting.html"
            class="com.baeldung.spring.controller.SimpleControllerHandlerAdapterExample"/>
            <bean id="viewResolver"
            class="org.springframework.web.servlet.view.InternalResourceViewResolver">
                <property name="prefix" value="/WEB-INF/" />
                <property name="suffix" value=".jsp" />
            </bean>
        </beans>
        ```

        BeanNameUrlHandlerMapping 类是此处理程序适配器的映射类。

        > 注意：如果在 BeanFactory 中定义了自定义处理程序适配器，则该适配器不会自动注册。因此，我们需要在上下文中明确定义它。如果没有定义该适配器，而我们又定义了自定义处理程序适配器，那么我们将收到一个异常，显示未指定处理程序的适配器。

    2. SimpleServletHandlerAdapter

        此处理程序适配器允许使用任何 Servlet 与 DispatcherServlet 一起处理请求。它通过调用 SendcherServlet 的 service() 方法，将请求从 DispatcherServlet 转发到相应的 Servlet 类。

        该适配器会自动处理实现 Servlet 接口的 Bean。该适配器默认未注册，我们需要在 DispatcherServlet 的配置文件中像注册其他普通 Bean 一样注册该适配器：

        ```xml
        <bean name="simpleServletHandlerAdapter" 
        class="org.springframework.web.servlet.handler.SimpleServletHandlerAdapter" />
        ```

    3. 注解方法处理程序适配器

        该适配器类用于执行带有 @RequestMapping 注解的方法。它用于根据 HTTP 方法和 HTTP 路径映射方法。

        该适配器的映射类是 DefaultAnnotationHandlerMapping，用于在类型级处理 @RequestMapping 注解，而 AnnotationMethodHandlerAdaptor 则用于在方法级处理。

        这两个类已在 DispatcherServlet 初始化时由框架注册。不过，如果已经定义了其他处理程序适配器，那么我们也需要在配置文件中定义它。

        让我们定义一个控制器类：

        ```java
        @Controller
        public class AnnotationHandler {
            @RequestMapping("/annotedName")
            public ModelAndView getEmployeeName() {
                ModelAndView model = new ModelAndView("Greeting");
                model.addObject("message", "Dinesh");
                return model;  
            }  
        }
        ```

        @Controller 注解表明该类扮演控制器的角色。

        @RequestMapping 注解将 getEmployeeName() 方法映射到 URL /name。

        根据应用程序使用的是基于 Java 的配置还是基于 XML 的配置，有两种不同的方法来配置该适配器。让我们看看使用 Java 配置的第一种方法：

        ```java
        @ComponentScan("com.baeldung.spring.controller")
        @Configuration
        @EnableWebMvc
        public class ApplicationConfiguration implements WebMvcConfigurer {
            @Bean
            public InternalResourceViewResolver jspViewResolver() {
                InternalResourceViewResolver bean = new InternalResourceViewResolver();
                bean.setPrefix("/WEB-INF/");
                bean.setSuffix(".jsp");
                return bean;
            }
        }
        ```

        如果应用程序使用 XML 配置，那么在网络应用上下文 XML 中配置此处理程序适配器有两种不同的方法。让我们看看文件 spring-servlet_AnnotationMethodHandlerAdapter.xml 中定义的第一种方法：

        ```xml
        <beans ...>
            <context:component-scan base-package="com.baeldung.spring.controller" />
            <bean 
            class="org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping"/>
            <bean 
            class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter"/>
            <bean id="viewResolver"
            class="org.springframework.web.servlet.view.InternalResourceViewResolver">
                <property name="prefix" value="/WEB-INF/" />
                <property name="suffix" value=".jsp" />
            </bean>
        </beans>
        ```

        `<context:component-scan />` 标记用于指定扫描控制器类的包。

        让我们看看第二种方法：

        ```xml
        <beans ...>
            <mvc:annotation-driven/>
            <context:component-scan base-package="com.baeldung.spring.controller" />
            <bean id="viewResolver"
            class="org.springframework.web.servlet.view.InternalResourceViewResolver">
                <property name="prefix" value="/WEB-INF/" />
                <property name="suffix" value=".jsp" />
            </bean>
        </beans>
        ```

        `<mvc:annotation-driven>` 标记将自动向 spring MVC 注册这两个类。该适配器在 Spring 3.2 中被弃用，Spring 3.1 中引入了名为 RequestMappingHandlerAdapter 的新处理程序适配器。

    4. 请求映射处理程序适配器

        该适配器类在 Spring 3.1 中引入，废弃了 Spring 3.2 中的 AnnotationMethodHandlerAdaptor 处理程序适配器。

        它与 RequestMappingHandlerMapping 类一起使用，后者执行用 @RequestMapping 注解的方法。

        RequestMappingHandlerMapping 用于维护请求 URI 与处理程序的映射。获得处理程序后，DispatcherServlet 会将请求分派给相应的处理程序适配器，然后调用处理程序方法（）。

        在 3.1 之前的 Spring 版本中，类型级映射和方法级映射是分两个不同阶段处理的。

        第一阶段是通过 DefaultAnnotationHandlerMapping 选择控制器，第二阶段是通过 AnnotationMethodHandlerAdapter 调用实际方法。

        从 Spring 3.1 版开始，只有一个阶段，即确定控制器以及需要调用哪个方法来处理请求。

        让我们定义一个简单的控制器类：

        ```java
        @Controller
        public class RequestMappingHandler {
            @RequestMapping("/requestName")
            public ModelAndView getEmployeeName() {
                ModelAndView model = new ModelAndView("Greeting");
                model.addObject("message", "Madhwal");
                return model;  
            }  
        }
        ```

        根据应用程序是使用基于 Java 的配置还是基于 XML 的配置，有两种不同的方法来配置此适配器。

        让我们看看使用 Java 配置的第一种方法：

        ```java
        @ComponentScan("com.baeldung.spring.controller")
        @Configuration
        @EnableWebMvc
        public class ServletConfig implements WebMvcConfigurer {
            @Bean
            public InternalResourceViewResolver jspViewResolver() {
                InternalResourceViewResolver bean = new InternalResourceViewResolver();
                bean.setPrefix("/WEB-INF/");
                bean.setSuffix(".jsp");
                return bean;
            }
        }
        ```

        如果应用程序使用 XML 配置，那么在网络应用上下文 XML 中配置此处理程序适配器有两种不同的方法。让我们看看文件 spring-servlet_RequestMappingHandlerAdapter.xml 中定义的第一种方法：

        ```xml
        <beans ...>
            <context:component-scan base-package="com.baeldung.spring.controller" />
            <bean 
            class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>
            <bean
            class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter"/>
            <bean id="viewResolver"
            class="org.springframework.web.servlet.view.InternalResourceViewResolver">
                <property name="prefix" value="/WEB-INF/" />
                <property name="suffix" value=".jsp" />
            </bean>
        </beans>
        ```

        下面是第二种方法：

        ```xml
        <beans ...>
            <mvc:annotation-driven />
            <context:component-scan base-package="com.baeldung.spring.controller" />
            <bean id="viewResolver"
            class="org.springframework.web.servlet.view.InternalResourceViewResolver">
                <property name="prefix" value="/WEB-INF/" />
                <property name="suffix" value=".jsp" />
            </bean>
        </beans>
        ```

        此标记将自动向 Spring MVC 注册这两个类。

        如果我们需要自定义 RequestMappingHandlerMapping，则需要从应用程序上下文 XML 中移除此标记，并在应用程序上下文 XML 中手动配置它。

    5. HttpRequestHandler 适配器

        该处理程序适配器用于处理 HttpRequests 的处理程序。它实现了 HttpRequestHandler 接口，其中包含一个用于处理请求和生成响应的 handleRequest() 方法。

        该方法的返回类型为 void，不会像其他处理程序适配器那样生成 ModelAndView 返回类型。它基本上用于生成二进制响应，不会生成视图进行渲染。

5. 运行应用程序

    如果应用程序部署在端口号为 8082 的 localhost 上，且上下文根目录为 Spring-mvc-handlers，则可以运行应用程序：

    <http://localhost:8082/spring-mvc-handlers/>

6. 结论

    在本文中，我们讨论了 Spring 框架中各种类型的处理程序适配器。

    大多数开发者可能会坚持使用默认设置，但当我们需要超越基本功能时，了解 Spring 框架的灵活性还是很有必要的。
