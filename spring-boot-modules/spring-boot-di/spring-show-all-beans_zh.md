# [如何获得所有Spring-Managed Beans？](https://www.baeldung.com/spring-show-all-beans)

1. 概述

    在本文中，我们将探索在容器中显示所有Spring管理bean的不同技术。

2. IoC容器

    bean是Spring管理应用程序的基础；所有bean都驻留在IOC容器中，IOC容器负责管理它们的生命周期。

    我们可以通过两种方式获取此容器中所有bean的列表：

    - 使用ListableBeanFactory接口
    - 使用Spring Boot Actuator执行器

3. 使用ListableBeanFactory接口

    ListableBeanFactory接口提供getBeanDefinitionNames（）方法，该方法返回此工厂中定义的所有bean的名称。此接口由所有预先加载其bean定义以枚举其所有bean实例的bean工厂实现。

    您可以在[官方文档](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/beans/factory/ListableBeanFactory.html)中找到所有已知子接口及其实现类的列表。

    对于这个例子，我们将使用一个SpringBoot应用程序。

    首先，我们将创建一些Springbean。让我们创建一个简单的Spring Controller FooController：

    ![displayallbeans.controller.FooController.java](./src/main/java/com/baeldung/displayallbeans/controller/FooController.java)

    此控制器依赖于另一个Spring-bean FooService：

    ![FooService.java](./src/main/java/com/baeldung/displayallbeans/service/FooService.java)

    请注意，我们在这里创建了两个不同的bean：

    - fooController
    - fooService

    在执行此应用程序时，我们将使用applicationContext对象并调用其getBeanDefinitionNames()方法，该方法将返回applicationContext容器中的所有bean：

    ![Application.java](./src/main/java/com/baeldung/displayallbeans/Application.java)

    这将打印applicationContext容器中的所有bean：

    ```txt
    fooController
    fooService
    //other beans
    ```

    注意，除了我们定义的bean之外，它还将记录此容器中的所有其他bean。为了清楚起见，我们在这里省略了它们，因为它们有很多。

4. 使用Spring Boot Actuator

    Spring Boot Actuator功能提供用于监控应用程序统计信息的端点。

    它包括许多内置端点，包括/beans。这显示了应用程序中所有Spring托管bean的完整列表。您可以在[官方文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints)上找到现有端点的完整列表。

    现在，我们只需点击URL `http://<address>：<managementport>/beans`。如果没有指定任何单独的管理端口，我们可以使用默认服务器端口。这将返回一个JSON响应，显示Spring IoC容器中的所有bean：

    ```java
    [
        {
            "context": "application:8080",
            "parent": null,
            "beans": [
                {
                    "bean": "fooController",
                    "aliases": [],
                    "scope": "singleton",
                    "type": "com.baeldung.displayallbeans.controller.FooController",
                    "resource": "file [E:/Workspace/tutorials-master/spring-boot/target
                    /classes/com/baeldung/displayallbeans/controller/FooController.class]",
                    "dependencies": [
                        "fooService"
                    ]
                },
                {
                    "bean": "fooService",
                    "aliases": [],
                    "scope": "singleton",
                    "type": "com.baeldung.displayallbeans.service.FooService",
                    "resource": "file [E:/Workspace/tutorials-master/spring-boot/target/
                    classes/com/baeldung/displayallbeans/service/FooService.class]",
                    "dependencies": []
                },
                // ...other beans
            ]
        }
    ]
    ```

    当然，这也由驻留在同一个spring容器中的许多其他bean组成，但为了清晰起见，我们在这里省略了它们。

    如果您想了解更多关于Spring Boot Actuator的信息，可以前往[Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators)主指南。

5. 结论

    在本文中，我们了解了如何使用 ListableBeanFactory 接口和 Spring Boot Actuators 显示 Spring IoC 容器中的所有 Bean。
