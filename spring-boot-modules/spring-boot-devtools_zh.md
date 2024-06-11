# [Spring Boot开发工具概述](https://www.baeldung.com/spring-boot-devtools)

1. 简介

    Spring Boot为我们提供了快速设置和运行服务的能力。

    为了进一步提升开发体验，Spring发布了spring-boot-devtools工具--作为Spring Boot-1.3的一部分。本文将尝试介绍我们使用新功能可以获得的好处。

    我们将涵盖以下主题：

    - 属性默认值
    - 自动重启
    - 实时重新加载
    - 全局设置
    - 远程应用程序

    1. 在项目中添加Spring-Boot-Devtools

        在项目中添加spring-boot-devtools和添加任何其他spring-boot模块一样简单。在一个现有的spring-boot项目中，添加以下依赖关系：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
            <scope>runtime</scope>
        </dependency>
        ```

        对项目进行清洁构建，现在你已经与spring-boot-devtools集成。

        scope = runtime 不能于生成包，避免线上执行包，启动文件监听线程File Watcher，耗费大量的内存资源。

        optional = true 防止将依赖传递到其他模块中。

2. 缓存静态资源

    Spring-boot做了很多自动配置，包括默认启用缓存以提高性能。其中一个例子是对模板引擎使用的模板进行缓存，例如thymeleaf。但是在开发过程中，尽可能快地看到变化是更重要的。

    可以在application.properties文件中使用spring.thymeleaf.cache=false属性来禁用thymeleaf的默认缓存行为。

    我们不需要手动操作，介绍这个spring-boot-devtools会自动为我们做这个。

    为了提高性能，开发工具对静态内容/模板文件进行缓存，以便更快地将它们提供给浏览器/客户端。

    缓存在生产中是一个非常好的功能，每一毫秒的性能改进都很重要。但在开发环境中，这可能是一个问题，会导致缓存过期，我们可能无法在浏览器中立即看到我们的变化。开发工具模块可以通过设置一些属性来定制这种能力。

    默认情况下，缓存功能是禁用的。我们可以通过设置一个属性使其在生产环境中使用。

    有很多这样的UI模板库支持这个功能。例如，thymleaf, freemarker, groovy, mustache等。

    ```properties
    # set true in production environment
    # set false in development environment; It is false by default.
    spring.freemarker.cache = false
    # Other cache properties
    spring.thymeleaf.cache = false
    spring.mustache.cache = false
    spring.groovy.template.cache = false
    ```

3. 自动重启

    在一个典型的应用程序开发环境中，开发者会做一些修改，构建项目并部署/启动应用程序以使新的修改生效，否则会尝试利用JRebel等。

    使用spring-boot-devtools，这个过程也是自动化的。每当classpath中的文件发生变化时，使用spring-boot-devtools的应用程序将导致应用程序重新启动。这个功能的好处是验证所做的改变所需的时间大大减少：

    ```log
    19:45:44.804 ... - Included patterns for restart : []
    19:45:44.809 ... - Excluded patterns for restart : [/spring-boot-starter/target/classes/, /spring-boot-autoconfigure/target/classes/, /spring-boot-starter-[\w-]+/, /spring-boot/target/classes/, /spring-boot-actuator/target/classes/, /spring-boot-devtools/target/classes/]
    19:45:44.810 ... - Matching URLs for reloading : [file:/.../target/test-classes/, file:/.../target/classes/]
    :: Spring Boot ::        (v1.5.2.RELEASE)
    2017-03-12 19:45:45.174  ...: Starting Application on machine with PID 7724 (<some path>\target\classes started by user in <project name>)
    2017-03-12 19:45:45.175  ...: No active profile set, falling back to default profiles: default
    2017-03-12 19:45:45.510  ...: Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@385c3ca3: startup date [Sun Mar 12 19:45:45 IST 2017]; root of context hierarchy
    ```

    从日志中可以看出，催生(spawned)应用程序的线程不是main，而是一个重启的Main线程。项目中的任何变化，无论是java文件的变化，都会导致项目的自动重启：

    ```log
    2017-03-12 19:53:46.204  ...: Closing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@385c3ca3: startup date [Sun Mar 12 19:45:45 IST 2017]; root of context hierarchy
    2017-03-12 19:53:46.208  ...: Unregistering JMX-exposed beans on shutdown
    :: Spring Boot ::        (v1.5.2.RELEASE)
    2017-03-12 19:53:46.587  ...: Starting Application on machine with PID 7724 (<project path>\target\classes started by user in <project name>)
    2017-03-12 19:53:46.588  ...: No active profile set, falling back to default profiles: default
    2017-03-12 19:53:46.591  ...: Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@acaf4a1: startup date [Sun Mar 12 19:53:46 IST 2017]; root of context hierarchy
    ```

4. 实时重新加载

    spring-boot-devtools模块包括一个嵌入式LiveReload服务器，当资源发生变化时，它被用来触发浏览器刷新。前提条件是，浏览器应该支持该扩展。

    为了在浏览器中实现这一点，我们需要安装LiveReload插件，其中一个实现是Chrome的[Remote Live Reload](https://chrome.google.com/webstore/detail/remotelivereload/jlppknnillhjgiengoigajegdpieppei?hl=en-GB)。

    默认情况下，实时重载是启用的。如果你出于某种原因希望禁用该功能，那么将spring.devtools.livereload.enabled属性设置为false，以禁用实时重载。

    1. 从自动重新加载中排除资源

        默认情况下，自动重载在这些路径上工作：

        - /META-INF/maven
        - /META-INF/resources
        - /resources
        - /static
        - /public
        - /templates

        如果我们想在浏览器中禁用这些路径中少数文件的自动重载功能，则使用spring.devtools.restart.exclude属性。 例如

        `spring.devtools.restart.exclude=static/**,public/**`

    2. 包括/排除其他路径

        可能有一些文件不在资源或classpath中，但我们仍然可能想观察这些附加文件/路径来重新加载应用程序。要做到这一点，可以使用spring.devtools.restart.extra-paths属性。

        `spring.devtools.restart.additional-paths=script/**`

        同样地，如果你想保留默认值并添加额外的排除项，请使用spring.devtools.restart.extra-exclude属性代替。

        `spring.devtools.restart.additional-exclude=styles/**`

5. 自动服务器重启

    自动重启意味着在服务器端重新加载Java类和配置。在服务器端发生变化后，这些变化被动态地重新部署，服务器重启发生并加载修改后的代码和配置。

    1. 记录自动配置Delta的变化

        默认情况下，每次你的应用程序重新启动时，都会记录一份显示条件评估delta的报告。该报告显示了你的应用程序的自动配置的变化，因为我们做了一些改变，如添加或删除Bean和设置配置属性。

        要禁用报告的记录，请设置以下属性：

        `spring.devtools.restart.log-condition-evaluation-delta = false`

    2. 禁用重启

        要禁止在非静态代码更改时重启服务器，请使用属性 spring.devtools.restart.enabled。

        `spring.devtools.restart.enabled = false`

    3. 使用触发器文件重新启动

        自动重启可能不适合在每个文件变更时进行，有时会因为频繁重启而减慢开发时间。为了解决这个问题，我们可以使用一个触发文件。

        Spring boot将持续监控该文件，一旦发现该文件有任何修改，它将重启服务器并重新加载所有之前的修改。

        使用spring.devtools.restart.trigger-file属性来为你的应用程序提到触发文件。它可以是任何外部或内部文件。

        `spring.devtools.restart.trigger-file = c:/workspace/restart-trigger.txt`

        自动重新加载与自动重启

        - 自动刷新（或自动加载）是指在浏览器上重新加载UI，以看到静态内容的变化。
        - 自动重启是指重新加载服务器端的代码和配置，然后再重新启动服务器。

6. 全局设置

    每次我们创建一个新的Spring boot项目时，设置所有喜欢的配置选项可能会成为一种重复的工作。我们可以使用全局设置文件将其最小化。

    各个projects/module将从全局文件中继承所有的自定义设置，如果需要的话，他们可以在每个项目的基础上覆盖任何特定的设置。

    spring-boot-devtools提供了一种配置全局设置的方法，这些设置不与任何应用程序相联系。

    要创建全局文件，请到你系统的用户主目录下，创建一个名为 .spring-boot-devtools.properties 的文件，它位于$HOME。(请注意，文件名以点开始）。

    > ? 不要使用这个全局属性文件来配置全局可用的选项。

7. 远程应用程序

    1. 通过HTTP进行远程调试(远程调试隧道)

        spring-boot-devtools通过HTTP提供开箱即用的远程调试功能，要拥有这个功能，需要将spring-boot-devtools打包成应用程序的一部分。这可以通过在maven的插件中禁用excludeDevtools配置来实现。

        下面是一个快速示例：

        ```xml
        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludeDevtools>false</excludeDevtools>
                    </configuration>
                </plugin>
            </plugins>
        </build>
        ```

        现在要想通过HTTP进行远程调试，必须采取以下步骤：

        1.在服务器上部署和启动的应用程序，应该在启用远程调试的情况下启动：

        `-Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n`

        我们可以看到，这里没有提到远程调试的端口。因此，java会选择一个随机的端口

        2.对于同一个项目，打开启动配置，选择以下选项：

        选择主类：org.springframework.boot.devtools.RemoteSpringApp

        在程序参数中，添加应用程序的URL，例如：<http://localhost:8080>

        3.通过spring-boot应用程序调试器的默认端口是8000，可以通过以下方式重写：

        `spring.devtools.remote.debug.local-port=8010`

        4.现在创建一个远程调试配置，将端口设置为通过属性配置的8010，如果坚持使用默认值，则设置为8000。

    2. 远程更新

        远程客户端监控应用程序classpath的变化，就像远程重启功能那样。classpath的任何变化都会导致更新的资源被推送到远程应用程序，并触发重启。

        更改是在远程客户端启动和运行时推送的，因为只有在那时才能监测到更改的文件。

        下面是日志中的情况：

        ```log
        2017-03-12 22:33:11.613  INFO 1484 ...: Remote debug connection opened
        2017-03-12 22:33:21.869  INFO 1484 ...: Uploaded 1 class resource
        ```

8. 总结

    通过这篇简短的文章，我们刚刚演示了如何利用spring-boot-devtools模块使开发者体验更好，并通过自动化大量的活动来减少开发时间。
