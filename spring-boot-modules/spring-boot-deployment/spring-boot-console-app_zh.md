# [Spring Boot控制台应用程序](https://www.baeldung.com/spring-boot-console-app)

1. 概述

    在这个快速教程中，我们将探讨如何使用Spring Boot创建一个简单的基于控制台的应用程序。

2. Maven依赖性

    我们的项目依赖于spring-boot的父类：spring-boot-starter-parent

    需要的初始依赖性是：spring-boot-starter

3. 控制台应用

    我们的控制台应用程序由一个单一的类SpringBootConsoleApplication.java组成，它是Spring Boot控制台应用程序的主类。

    我们在主类上使用Spring的@SpringBootApplication注解来启用自动配置。

    这个类还实现了Spring的CommandLineRunner接口。CommandLineRunner是一个简单的Spring Boot接口，有一个运行方法。在应用上下文加载完毕后，Spring Boot会自动调用所有实现该接口的Bean的运行方法。

    下面是我们的控制台应用程序：springbootnonwebapp/SpringBootConsoleApplication.java

    我们还应该指定`spring.main.web-application-type=NONE` [Spring属性](https://www.baeldung.com/properties-with-spring)。这个属性将明确告知Spring，这不是一个Web应用。

    当我们执行SpringBootConsoleApplication时，我们可以看到以下记录：

    ```log
    00:48:51.888 [main] INFO  c.b.s.SpringBootConsoleApplication - STARTING THE APPLICATION
    00:48:52.752 [main] INFO  c.b.s.SpringBootConsoleApplication - No active profile set, falling back to default profiles: default
    00:48:52.851 [main] INFO  o.s.c.a.AnnotationConfigApplicationContext 
    - Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@6497b078: startup date [Sat Jun 16 00:48:52 IST 2018]; root of context hierarchy
    00:48:53.832 [main] INFO  o.s.j.e.a.AnnotationMBeanExporter - Registering beans for JMX exposure on startup
    00:48:53.854 [main] INFO  c.b.s.SpringBootConsoleApplication - EXECUTING : command line runner
    00:48:53.854 [main] INFO  c.b.s.SpringBootConsoleApplication - args[0]: Hello World!
    00:48:53.860 [main] INFO  c.b.s.SpringBootConsoleApplication - Started SpringBootConsoleApplication in 1.633 seconds (JVM running for 2.373)
    00:48:53.860 [main] INFO  c.b.s.SpringBootConsoleApplication - APPLICATION FINISHED
    00:48:53.868 [Thread-2] INFO  o.s.c.a.AnnotationConfigApplicationContext 
    - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@6497b078: startup date [Sat Jun 16 00:48:52 IST 2018]; root of context hierarchy
    00:48:53.870 [Thread-2] INFO  o.s.j.e.a.AnnotationMBeanExporter - Unregistering JMX-exposed beans on shutdown
    ```

    请注意，run方法是在应用上下文加载后，但在main方法的执行完成前被调用的。

    大多数控制台应用程序只会有一个实现CommandLineRunner的类。如果我们的应用程序有多个实现CommandLineRunner的类，可以使用Spring的@Order注解来指定执行的顺序。

4. 总结

    在这篇简短的文章中，我们学习了如何使用Spring Boot创建一个简单的基于控制台的应用程序。
