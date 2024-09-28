# [对Spring Boot应用程序进行Docker化](https://www.baeldung.com/dockerizing-spring-boot-application)

1. 概述
    在本教程中，我们将重点讨论如何对Spring Boot应用程序进行dockerize，使其在一个隔离的环境（又称容器）中运行。
    我们将学习如何创建一个容器的组合，这些容器相互依赖，并在虚拟专用网络中相互链接。我们还将看到如何用单个命令来管理它们。
    让我们从创建一个简单的Spring Boot应用程序开始，然后在一个运行[Alpine Linux](https://hub.docker.com/_/alpine/)的轻量级基础镜像中运行。

    [用Spring Boot创建Docker镜像](https://www.baeldung.com/spring-boot-docker-images)

    了解如何为Spring Boot应用程序创建Docker镜像

    [用配置文件在Docker中启动Spring Boot应用程序](https://www.baeldung.com/spring-boot-docker-start-with-profile)

    如何使用Docker启动具有特定配置文件的应用程序。

    [在Docker中访问Spring Boot日志](https://www.baeldung.com/ops/spring-boot-logs-docker)

    一个快速而实用的教程，用于访问Spring Boot Docker日志。

2. Dockerize一个独立的Spring Boot应用程序

    作为一个可以Dockerize的应用程序的例子，我们将创建一个简单的Spring Boot应用程序，docker-message-server，它暴露了一个端点并返回一个静态消息：

    ```java
    @RestController
    public class DockerMessageController {
        @GetMapping("/messages")
        public String getMessage() {
            return "Hello from Docker!";
        }
    }
    ```

    有了正确配置的Maven文件，我们就可以创建一个可执行的jar文件：

    `$> mvn clean package`

    接下来，我们将启动Spring Boot应用程序：

    `$> java -jar target/docker-message-server-1.0.0.jar`

    现在我们有了一个可以工作的Spring Boot应用程序，我们可以在localhost:8888/messages访问。
    为了对应用程序进行docker化，我们首先创建一个名为Dockerfile的文件，内容如下：

    ```yml
    FROM openjdk:8-jdk-alpine
    MAINTAINER baeldung.com
    COPY target/docker-message-server-1.0.0.jar message-server-1.0.0.jar
    ENTRYPOINT ["java","-jar","/message-server-1.0.0.jar"]
    ```

    这个文件包含以下信息：

    - FROM：作为我们镜像的基础，我们将采用上一节中创建的支持Java的Alpine Linux。
    - MAINTAINER：该镜像的维护者。
    - COPY：我们让Docker把我们的jar文件复制到镜像中。
    - ENTRYPOINT：这将是容器启动时要启动的可执行文件。我们必须把它们定义为JSON-Array，因为我们会把ENTRYPOINT和CMD一起用于一些应用参数。
    为了从我们的Docker文件中创建一个镜像，我们必须像以前一样运行 "docker build"：
    `$> docker build --tag=message-server:latest .`

    最后，我们就可以从我们的镜像中运行容器了：
    `$> docker run -p8887:8888 message-server:latest`

    这将在Docker中启动我们的应用程序，我们可以从主机上的localhost:8887/messages访问它。在这里，定义端口映射很重要，它将主机上的一个端口（8887）映射到Docker内部的端口（8888）。这就是我们在Spring Boot应用程序的属性中定义的端口。

    注意：在我们启动容器的机器上，8887端口可能不可用。在这种情况下，映射可能不起作用，我们需要选择一个仍然可用的端口。
    如果我们在分离模式下运行容器，我们可以检查它的细节，停止它，并通过以下命令删除它：

    ```zsh
    $> docker inspect message-server
    $> docker stop message-server
    $> docker rm message-server
    ```

    1. 改变基础镜像
        我们可以很容易地改变基础镜像，以使用不同的Java版本。例如，如果我们想使用亚马逊的Corretto发行版，我们可以简单地改变Docker文件：

        ```yml
        FROM amazoncorretto:11-alpine-jdk
        MAINTAINER baeldung.com
        COPY target/docker-message-server-1.0.0.jar message-server-1.0.0.jar
        ENTRYPOINT ["java","-jar","/message-server-1.0.0.jar"]
        ```

        此外，我们可以使用一个自定义的基本镜像。我们将在本教程的后面看看如何做到这一点。
3. 在一个综合体Composite中对应用程序进行Docker化
    Docker命令和Docker文件特别适用于创建单个容器。然而，如果我们想在一个孤立的应用程序网络上操作，容器管理很快就会变得杂乱无章。
    为了解决这个问题，Docker提供了一个名为Docker Compose的工具。这个工具带有自己的YAML格式的构建文件，更适合管理多个容器。例如，它能够在一个命令中启动或停止一个复合服务，或者将多个服务的日志输出合并到一个伪tty中。
    1. 第二个Spring Boot应用程序
        让我们建立一个例子，在不同的Docker容器中运行两个应用程序。它们将相互通信，并作为一个 "single unit" 呈现给主机系统。作为一个简单的例子，我们将创建第二个Spring Boot应用程序docker-product-server：

        ```java
        @RestController
        public class DockerProductController {
            @GetMapping("/products")
            public String getMessage() {
                return "A brand new product";
            }
        }
        ```

        我们可以用与我们的消息服务器相同的方式建立和启动应用程序。
    2. Docker Compose文件

        我们可以在一个名为docker-compose.yml的文件中合并两个服务的配置：

        ```yml
        version: '2'
        services:
            message-server:
                container_name: message-server
                build:
                    context: docker-message-server
                    dockerfile: Dockerfile
                image: message-server:latest
                ports:
                    - 18888:8888
                networks:
                    - spring-cloud-network
            product-server:
                container_name: product-server
                build:
                    context: docker-product-server
                    dockerfile: Dockerfile
                image: product-server:latest
                ports:
                    - 19999:9999
                networks:
                    - spring-cloud-network
        networks:
            spring-cloud-network:
                driver: bridge
        ```

        - version： 指定应该使用哪个格式的版本。这是一个强制字段。这里我们使用较新的版本，而传统的格式是'1'。
        - services： 这个键中的每个对象都定义了一个服务，也就是容器。这一部分是强制性的。
        - build： 如果给出，docker-compose能够从Docker文件中构建一个镜像。
            - context： 如果给定，它指定了构建目录，Dockerfile在那里被查找。
            - dockerfile： 如果给定，它为Dockerfile设置一个备用名称。
        - image： 告诉Docker在使用build-features时应该给镜像起什么名字。否则，它将在库或远程注册表中搜索这个镜像。
        - networks： 这是要使用的命名网络的标识符。一个给定的名称-值必须在networks部分列出。
        - networks： 在这一节中，我们要指定我们的服务可用的网络。在这个例子中，我们让docker-compose为我们创建一个 "bridge" 类型的命名网络。如果外部选项被设置为 "true"，它将使用一个现有的、给定名称的网络。

        在我们继续之前，我们将检查我们的构建文件是否有语法错误：
        `$> docker-compose config`

        然后我们就可以构建我们的镜像，创建定义好的容器，并在一个命令中启动它：
        `$> docker-compose up --build`

        这将一次性地启动消息服务器和产品服务器。
        要想停止这些容器，就要把它们从Docker中删除，并把连接的网络从其中删除。要做到这一点，我们可以使用相反的命令：
        `$> docker-compose down`

        关于docker-compose的详细介绍，我们可以阅读我们的文章《[Docker Compose简介](https://www.baeldung.com/docker-compose)》。
    3. 扩展服务
        docker-compose的一个很好的功能是能够扩展服务。例如，我们可以告诉Docker为消息服务器运行三个容器，为产品服务器运行两个容器。
        然而，为了使其正常工作，我们必须从docker-compose.yml中删除container_name，这样Docker就可以选择名称，并改变暴露的端口配置以避免冲突。
        对于端口，我们可以告诉Docker将主机上的一系列端口映射到Docker内部的一个特定端口：

        ```yaml
        ports:
            - 18800-18888:8888
        ```

        之后，我们就可以像这样扩展我们的服务了（注意，我们使用的是修改过的yml-file）：
        `$> docker-compose --file docker-compose-scale.yml up -d --build --scale message-server=1 product-server=1`

        这个命令将启动一个单一的消息服务器和一个单一的产品服务器。
        为了扩展我们的服务，我们可以运行以下命令：
        `$> docker-compose --file docker-compose-scale.yml up -d --build --scale message-server=3 product-server=2`

        这个命令将启动两个额外的消息服务器和一个额外的产品服务器。运行中的容器将不会被停止。
4. 自定义基础镜像
    到目前为止，我们使用的基础镜像（openjdk:8-jdk-alpine）包含一个已经安装了JDK 8的Alpine操作系统的发行版。另外，我们也可以建立自己的基础镜像（基于 Alpine 或任何其他操作系统）。
    为此，我们可以使用一个以Alpine为基础镜像的Docker文件，并安装我们选择的JDK：

    ```yml
    FROM alpine:edge
    MAINTAINER baeldung.com
    RUN apk add --no-cache openjdk8
    ```

    - FROM： 关键字FROM告诉Docker使用一个给定的镜像及其标签作为构建基础。如果这个镜像不在本地库中，就会在[DockerHub](https://hub.docker.com/explore/)或任何其他配置的远程注册表上进行在线搜索。
    - MAINTAINER：MAINTAINER通常是一个电子邮件地址，用于识别一个镜像的作者。
    - RUN：通过RUN命令，我们在目标系统中执行一个shell命令行。这里我们利用Alpine Linux的软件包管理器apk来安装Java 8 OpenJDK。
    为了最终构建镜像并将其存储在本地库中，我们必须运行：
    `docker build --tag=alpine-java:base --rm=true .`

    > 注意：-tag选项将给镜像命名，而-rm=true将在构建成功后删除中间镜像。这个shell命令的最后一个字符是一个点，充当构建目录的参数。
    现在我们可以使用创建的镜像，而不是openjdk:8-jdk-alpine。
5. Spring Boot 2.3中的Buildpacks支持
    Spring Boot 2.3 增加了对 [buildpacks](https://buildpacks.io/) 的支持。简单地说，我们不需要创建自己的Docker文件并使用docker build之类的东西来构建它，而只需发出以下命令：
    `$ mvn spring-boot:build-image`

    类似地，在Gradle中：
    `$ ./gradlew bootBuildImage`

    为了使其发挥作用，我们需要安装并运行Docker。
    buildpacks背后的主要动机是创建与一些著名的云服务，如Heroku或Cloud Foundry，已经提供了一段时间的相同部署体验。我们只需运行build-image目标，然后由平台本身负责构建和部署工件。
    此外，它可以帮助我们[更有效](https://www.baeldung.com/spring-boot-docker-images)地改变我们构建Docker镜像的方式。我们所要做的是改变或调整buildpacks镜像构建器，而不是对不同项目中的大量Dockerfiles应用同样的改变。
    除了易用性和更好的整体开发者体验之外，它还可以更有效率。例如，buildpacks方法将创建一个分层的Docker镜像，并使用Jar文件的exploded版本。
    让我们看看在我们运行上述命令后会发生什么。
    当我们列出可用的docker镜像时：
    `docker image ls -a`

    我们看到有一行是关于我们刚刚创建的镜像的：
    `docker-message-server 1.0.0 b535b0cc0079`

    这里，镜像的名称和版本与我们在Maven或Gradle配置文件中定义的名称和版本一致。哈希代码是镜像哈希值的简写。
    然后为了启动我们的容器，我们可以简单地运行：
    `docker run -it -p9099:8888 docker-message-server:1.0.0`

    与我们构建的镜像一样，我们需要映射端口，使我们的Spring Boot应用可以从Docker外部访问。
6. 总结

    在这篇文章中，我们学习了如何构建自定义Docker镜像，将Spring Boot应用作为Docker容器运行，以及用docker-compose创建容器。
    关于构建文件的进一步阅读，我们参考了官方的[Dockerfile](https://docs.docker.com/engine/reference/builder/)参考和[docker-compose.yml](https://docs.docker.com/compose/compose-file/)参考。
