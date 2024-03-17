# 在Spring Cloud配置中重写远程属性的值

[Spring云](../spring-cloud_zh.md)

1. 概述

    Spring Cloud Config是Spring Cloud伞式项目的一部分。它通过集中式服务管理应用程序的配置数据，从而将其与你部署的微服务截然分开。Spring Cloud Config有自己的属性管理库，但也与Git、Consul和Eureka等开源项目集成。

    在这篇文章中，我们将看到在Spring Cloud Config中覆盖远程属性值的不同方式，Spring从2.4版本开始施加的限制，以及3.0版本的变化。在本教程中，我们将使用spring-boot 2.7.2版本。

2. 创建一个Spring配置服务器

    为了将配置文件外部化，让我们使用Spring Cloud Config创建我们的边缘服务。它被定位为一个配置文件的分发服务器。在本文中，我们将使用一个文件系统仓库。

    1. 创建配置文件

        在application.properties文件中定义的配置可与所有客户端应用程序共享。也可以为一个应用程序或一个特定的配置文件定义特定的配置。

        让我们从创建配置文件开始，该文件包含提供给我们的客户端应用程序的属性。我们将把我们的客户端应用程序命名为 "baeldung"。在spring-cloud-config-overriding-properties-server项目的 /resources/config文件夹中，让我们创建一个baeldung.properties文件。

    2. 添加属性

        让我们在baeldung.properties文件中添加一些属性，然后在我们的客户端应用程序中使用这些属性。

        让我们也在resources/config/application.properties文件中添加一个共享属性shared-property，Spring将在所有客户端中共享这个属性。

    3. Spring-Boot配置服务器应用程序

        现在，让我们创建一个Spring应用程序，为我们的配置服务。我们还需要spring-cloud-config-server这个依赖项。

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        ```

        一旦安装完毕，让我们创建应用程序并启用配置服务器。

        spring.cloud.config.overridingproperties/ConfigServer.java

        让我们在应用程序的application.properties文件中添加以下属性，告诉它在8081端口启动，并加载之前定义的配置。

        ```properties
        server.port=8081
        spring.cloud.config.server.native.searchLocations=classpath:/config
        ```

        现在，让我们启动我们的服务器应用程序，激活本地配置文件并允许我们使用文件系统作为配置库。

        `mvn spring-boot:run -Drun.profiles=native`

        我们的服务器现在正在运行并为我们的配置服务。让我们验证一下我们的共享属性是否可以访问。

        ```zsh
        $ curl localhost:8081/unknownclient/default
        {
        "name": "unknownclient",
        "profiles": [
            "default"
        ],
        "label": null,
        "version": null,
        "state": null,
        "propertySources": [
            {
            "name": "classpath:/config/application.properties",
            "source": {
                "shared-property": "This property is shared accross all client applications"
            }
            }
        ]
        }
        ```

        以及特定于我们应用程序的属性。

        ```zsh
        $ curl localhost:8081/baeldung/default
        {
        "name": "baeldung",
        "profiles": [
            "default"
        ],
        "label": null,
        "version": null,
        "state": null,
        "propertySources": [
            {
            "name": "classpath:/config/baeldung.properties",
            "source": {
                "hello": "Hello Jane Doe!",
                "welcome": "Welcome Jane Doe!"
            }
            },
            {
            "name": "classpath:/config/application.properties",
            "source": {
                "shared-property": "This property is shared accross all client applications"
            }
            }
        ]
        }
        ```

        我们向服务器指出我们的应用程序的名称以及使用的配置文件，即默认。

        我们没有禁用 spring.cloud.config.server.accept-empty 属性，其默认值为 true。如果应用程序是未知的（unknownclient），配置服务器无论如何都会返回共享属性。

3. 客户端应用程序

    现在让我们创建一个客户端应用程序，在启动时加载由服务器提供的配置。

    1. 项目设置和依赖关系

        让我们在 pom.xml 中添加 spring-cloud-starter-config 依赖项来加载配置，以及 spring-boot-starter-web 来创建控制器。

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        ```

    2. 创建客户端应用程序

        接下来，让我们创建客户端应用程序，从spring-cloud-config服务器读取配置。

        ```java
        @SpringBootApplication
        public class Client {
            public static void main(String[] args) {
                SpringApplication.run(Client.class, args);
            }
        }
        ```

    3. 获取配置

        让我们修改我们的application.properties文件，告诉spring-boot从我们的服务器获取配置。

        ```properties
        spring.cloud.config.name=baeldung
        spring.config.import=optional:configserver:http://localhost:8081
        ```

        我们还指定我们的应用程序的名称为 "baeldung"，以利用专用属性。

    4. 添加一个简单的控制器

        现在让我们创建一个控制器，负责显示我们配置的特定属性，以及共享属性。

        spring.cloud.config.overridingproperties/HelloController.java

        现在我们可以导航到这三个URL，以验证我们的配置是否被考虑到了。

        ```bash
        $ curl http://localhost:8080/hello
        Hello Jane Doe!
        $ curl http://localhost:8080/welcome
        Welcome Jane Doe!
        $ curl http://localhost:8080/shared
        This property is shared accross all client applications
        ```

4. 覆盖服务器上的属性

    我们可以通过修改服务器配置来覆盖为特定应用程序定义的属性。

    让我们编辑服务器的 resources/application.properties 文件以覆盖 hello 属性。

    `spring.cloud.config.server.overrides.hello=Hello Jane Doe – application.properties!`

    让我们再次测试对/hello控制器的调用，以验证重载是否被考虑在内。

    ```bash
    $ curl http://localhost:8080/hello
    Hello Jane Doe - application.properties!
    ```

    我们可以在resources/config/application.properties文件的共享配置层添加这个重载。在这种情况下，它将优先于上面定义的那个。

5. 在客户端重写属性

    自Spring Boot 2.4版以来，不再可能通过客户端应用程序的application.properties文件来覆盖属性了。

    1. 使用Spring配置文件

        不过，我们可以使用[Spring配置文件](https://www.baeldung.com/spring-profiles)。在配置文件中本地定义的属性比在服务器级别为应用程序定义的优先级更高。

        让我们为我们的客户端应用程序添加一个application-development.properties配置文件，并重写hello属性。

        `hello=Hello local property!`

        现在，让我们通过激活开发配置文件来启动我们的客户端。

        ```bash
        mvn spring-boot:run -Drun.profiles=development
        ```

        我们现在可以再次测试我们的控制器/hello，以验证重载是否正确工作。

        ```bash
        $ curl http://localhost:8080/hello
        Hello local property!
        ```

    2. 使用占位符

        我们可以使用占位符对属性进行估值。因此，服务器将提供一个默认值，它可以被客户端定义的属性所覆盖。

        让我们从服务器的resources/application.properties文件中删除hello属性的重载，并修改config/baeldung.properties中的重载，以纳入占位符的使用。

        `hello=${app.hello:Hello Jane Doe!}`

        这样，服务器提供了一个默认值，如果客户端声明了一个名为app.hello的属性，这个默认值可以被覆盖。

        让我们编辑客户端的resources/application.properties文件来添加该属性。

        `app.hello=Hello, overriden local property!`

        让我们再次测试我们的控制器hello，以验证是否正确地考虑到了超限因素。

        ```bash
        $ curl http://localhost:8080/hello
        Hello, overriden local property!
        ```

        注意，如果hello属性也是在配置文件配置中定义的，后者将优先考虑。

6. 遗留配置

    从spring-boot 2.4开始，可以使用 "legacy configuration遗留配置"。这允许我们在spring-boot 2.4版本的修改之前使用旧的属性管理系统。

    1. 加载外部配置

        在2.4版本之前，外部配置的管理是由一个引导程序来保证的。我们需要 spring-cloud-starter-bootstrap 这个依赖项。让我们把它添加到我们的 pom.xml 中。

        ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        ```

        接下来，我们将在资源文件夹中创建 bootstrap.properties 文件，并配置到我们服务器的访问 URL。

        ```properties
        spring.cloud.config.name=baeldung
        spring.cloud.config.uri=http://localhost:8081
        ```

        让我们在application.properties中启用遗留配置。

        `spring.config.use-legacy-processing=true`

    2. 启用重写能力

        在服务器端，我们需要表明属性重载是可能的。让我们以这种方式修改我们的baeldung.properties文件。

        `spring.cloud.config.overrideNone=true`

        这样，外部属性就不会比应用JAR中定义的属性有优先权。

    3. 覆盖服务器的属性

        现在我们可以通过 application.properties 文件在客户端应用程序中重写 hello 属性。

        `hello=localproperty`

        让我们测试一下对我们控制器的调用。

        ```bash
        $ curl http://localhost:8080/hello
        localproperty
        ```

    4. 遗留配置的贬值

        自Spring Boot 3.0版本以来，启用遗留配置已不再可能。在这种情况下，我们应该使用上面提出的其他方法。

7. 总结

    在这篇文章中，我们看到了在Spring Cloud Config中覆盖远程属性值的不同方法。

    我们有可能从服务器上覆盖那些为特定应用程序定义的属性。也可以在客户端使用配置文件或占位符来覆盖属性。我们还研究了如何激活传统的配置以返回到旧的属性管理系统。

    像往常一样，源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-config)上找到。
