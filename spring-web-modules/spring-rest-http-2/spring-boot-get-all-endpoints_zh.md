# 在 Spring Boot 中获取所有端点

1. 概述

    在使用 REST API 时，通常需要检索所有 REST 端点。例如，我们可能需要在数据库中保存所有请求映射端点。在本教程中，我们将了解如何获取 Spring Boot 应用程序中的所有 REST 端点。

2. 映射端点

    在 Spring Boot 应用程序中，我们通过在控制器类中使用 @RequestMapping 注解来公开 REST API 端点。要获取这些端点，有三种选择：事件监听器、Spring Boot Actuator 或 SpringDoc 库。

3. 事件监听器方法

    为创建 REST API 服务，我们在控制器类中使用 [@RestController](https://www.baeldung.com/building-a-restful-web-service-with-spring-and-java-based-configuration#controller) 和 @RequestMapping。这些类在 Spring 应用上下文中注册为 Spring Bean。因此，当应用程序上下文在启动时准备就绪，我们就可以使用事件监听器获取端点。定义监听器有两种方法。我们可以实现 [ApplicationListener](https://www.baeldung.com/spring-events#listener) 接口，或者使用 [@EventListener](https://www.baeldung.com/spring-events#annotation-driven) 注解。

    1. 应用程序监听器接口

        在实现 ApplicationListener 时，我们必须定义 onApplicationEvent() 方法：

        ```java
        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            ApplicationContext applicationContext = event.getApplicationContext();
            RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping
                .getHandlerMethods();
            map.forEach((key, value) -> LOGGER.info("{} {}", key, value));
        }
        ```

        这样，我们就使用了 [ContextRefreshedEvent](https://www.baeldung.com/spring-context-events#1-contextrefreshedevent) 类。该事件在 ApplicationContext 初始化或刷新时发布。Spring Boot 提供了许多 HandlerMapping 实现。其中，RequestMappingHandlerMapping 类可检测请求映射，并被 @RequestMapping 注解所使用。因此，我们在 ContextRefreshedEvent 事件中使用了该 Bean。

    2. @EventListener 注解

        映射端点的另一种方法是使用 @EventListener 注解。我们直接在处理 ContextRefreshedEvent 的方法中使用该注解：

        ```java
        @EventListener
        public void handleContextRefresh(ContextRefreshedEvent event) {
            ApplicationContext applicationContext = event.getApplicationContext();
            RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping
                .getHandlerMethods();
            map.forEach((key, value) -> LOGGER.info("{} {}", key, value));
        }
        ```

4. 执行器方法

    检索所有端点列表的第二种方法是使用 [Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators) 功能。

    1. Maven 依赖项

        为启用此功能，我们将在 pom.xml 文件中添加 spring-boot-actuator Maven 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        ```

    2. 配置

        添加 spring-boot-actuator 依赖关系时，默认情况下只有 /health 和 /info 端点可用。要启用所有执行器[端点](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-endpoints)，我们可以在 application.properties 文件中添加一个属性来公开它们：

        `management.endpoints.web.exposure.include=*`

        或者，我们可以简单地暴露用于检索映射的端点：

        `management.endpoints.web.exposure.include=mappings`

        启用后，我们应用程序的 REST API 端点就可以在 <http://host/actuator/mappings> 上使用了。

5. SpringDoc

    [SpringDoc](https://www.baeldung.com/spring-rest-openapi-documentation) 库也可用于列出 REST API 的所有端点。

    1. Maven 依赖

        要将 SpringDoc 添加到项目中，我们需要在 pom.xml 文件中添加 springdoc-openapi-ui 依赖关系：

        ```xml
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
            <version>1.7.0</version>
        </dependency>
        ```

    2. 配置

        让我们通过定义 OpenAPI Bean 来创建配置类：

        ```java
        @Bean
        public OpenAPI openAPI() {
            return new OpenAPI().info(new Info().title("SpringDoc example")
                .description("SpringDoc application")
                .version("v0.0.1"));
        }
        ```

        要访问 REST API 端点，我们可以在浏览器中访问此 URL：

        <http://localhost:8080/swagger-ui/index.html>

6. 结论

    本文介绍了如何使用事件监听器、Spring Boot Actuator 和 SpringDoc 库在 Spring Boot 应用程序中检索请求映射端点。
