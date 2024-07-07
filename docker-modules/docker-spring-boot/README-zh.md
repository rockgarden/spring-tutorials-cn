# Spring Boot Docker

## 用Spring Boot创建Docker镜像

1. 简介

    随着越来越多的组织转向容器和虚拟服务器，Docker正在成为软件开发工作流程中更重要的一部分。为此，Spring Boot 2.3中的一个伟大的新功能是能够为Spring Boot应用程序轻松创建Docker镜像。
    在本教程中，我们将看看如何为Spring Boot应用程序创建Docker镜像。
2. 传统的Docker构建
    用Spring Boot构建Docker镜像的传统方法是使用Dockerfile。下面是一个简单的例子：

    ```yml
    FROM openjdk:8-jdk-alpine
    EXPOSE 8080
    ARG JAR_FILE=target/demo-app-1.0.0.jar
    ADD ${JAR_FILE} app.jar
    ENTRYPOINT ["java","-jar","/app.jar"]
    ```

    然后我们可以使用docker build命令来创建一个Docker镜像。这对大多数应用程序来说都很好用，但也有几个缺点。
    首先，我们使用的是Spring Boot创建的胖jar。这可能会影响启动时间，特别是在容器化环境中。我们可以通过添加jar文件的爆炸性内容来节省启动时间。
    第二，Docker镜像是分层构建的。Spring Boot fat jars的性质导致所有的应用程序代码和第三方库都被放到一个层中。这意味着即使只有一行代码发生变化，整个层也必须重新构建。
    通过在构建前对jar进行爆炸，应用程序代码和第三方库都有自己的层。这使我们能够利用Docker的缓存机制。现在，当一行代码被改变时，只有那个相应的层需要被重建。
    考虑到这一点，让我们看看Spring Boot是如何改进创建Docker镜像的过程的。
3. Buildpacks
    Buildpacks是一种提供框架和应用程序依赖性的工具。
    例如，给定一个Spring Boot fat jar，buildpack就会为我们提供Java运行时。这使得我们可以跳过Dockerfile，自动获得一个合理的Docker镜像。
    Spring Boot包括Maven和Gradle对构建包的支持。例如，用Maven构建，我们可以运行以下命令：
    `./mvnw spring-boot:build-image`

    让我们看看一些相关的输出，看看正在发生什么：

    ```log
    [INFO] Building jar: target/demo-0.0.1-SNAPSHOT.jar
    ...
    [INFO] Building image 'docker.io/library/demo:0.0.1-SNAPSHOT'
    ...
    [INFO]  > Pulling builder image 'gcr.io/paketo-buildpacks/builder:base-platform-api-0.3' 100%
    ...
    [INFO]     [creator]     ===> DETECTING
    [INFO]     [creator]     5 of 15 buildpacks participating
    [INFO]     [creator]     paketo-buildpacks/bellsoft-liberica 2.8.1
    [INFO]     [creator]     paketo-buildpacks/executable-jar    1.2.8
    [INFO]     [creator]     paketo-buildpacks/apache-tomcat     1.3.1
    [INFO]     [creator]     paketo-buildpacks/dist-zip          1.3.6
    [INFO]     [creator]     paketo-buildpacks/spring-boot       1.9.1
    ...
    [INFO] Successfully built image 'docker.io/library/demo:0.0.1-SNAPSHOT'
    [INFO] Total time:  44.796 s
    ```

    第一行显示我们构建了标准的fat jar，就像任何典型的maven包一样。
    下一行开始构建Docker镜像。紧接着，我们看到构建过程中使用了[Packeto](https://paketo.io/)构建器。
    Packeto是云原生构建包的一个实现。它的工作是分析我们的项目，确定所需的框架和库。在我们的例子中，它确定我们有一个Spring Boot项目，并加入了所需的构建包。
    最后，我们看到生成的Docker镜像和总构建时间。请注意，在第一次构建时，我们花了相当多的时间下载buildpacks并创建不同的层。
    buildpacks的一个很大的特点是，Docker镜像是多层的。因此，如果我们只改变我们的应用程序代码，后续的构建就会快得多：

    ```log
    ...
    [INFO]     [creator]     Reusing layer 'paketo-buildpacks/executable-jar:class-path'
    [INFO]     [creator]     Reusing layer 'paketo-buildpacks/spring-boot:web-application-type'
    ...
    [INFO] Successfully built image 'docker.io/library/demo:0.0.1-SNAPSHOT'
    ...
    [INFO] Total time:  10.591 s
    ```

4. 分层的Jars

    在某些情况下，我们可能不愿意使用buildpacks--也许我们的基础设施已经与另一个工具绑定，或者我们已经有了想要重新使用的自定义Dockerfiles。
    由于这些原因，Spring Boot也支持使用分层jars构建Docker镜像。为了了解它是如何工作的，让我们看看一个典型的Spring Boot fat jar布局：

    ```txt
    org/
    springframework/
        boot/
    loader/
    ...
    BOOT-INF/
    classes/
    ...
    lib/
    ...
    ```

    fat jar由3个主要区域组成：

    - 启动Spring应用程序所需的Bootstrap类
    - 应用程序代码
    - 第三方库
    对于分层jar，其结构看起来类似，但我们得到一个新的layer.idx文件，将fat jar中的每个目录映射到一个层：

    ```idx
    - "dependencies":
    - "BOOT-INF/lib/"
    - "spring-boot-loader":
    - "org/"
    - "snapshot-dependencies":
    - "application":
    - "BOOT-INF/classes/"
    - "BOOT-INF/classpath.idx"
    - "BOOT-INF/layers.idx"
    - "META-INF/"
    ```

    开箱即用，Spring Boot提供了四个层：

    - dependencies：来自第三方的典型依赖项
    - snapshot-dependencies：来自第三方的快照依赖。
    - resources：静态资源
    - application：应用程序代码和资源
    我们的目标是将应用程序代码和第三方库放到反映它们变化频率的层中。
    例如，应用程序代码可能是变化最频繁的，所以它有自己的层。此外，每个层都可以自行发展，只有当一个层发生变化时，才会为Docker镜像重新构建。
    现在我们了解了新的分层jar结构，让我们来看看如何利用它来制作Docker镜像。
    1. 创建分层jar
        首先，我们要对项目进行设置，以创建分层jar。在Maven中，这意味着在POM的Spring Boot插件部分添加一个新配置：

        ```xml
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <layers>
                    <enabled>true</enabled>
                </layers>
            </configuration>
        </plugin>
        ```

        有了这种配置，Maven打包命令（连同其任何依赖命令）将使用之前提到的四个默认层生成一个新的分层jar。
    2. 查看和提取图层
        接下来，我们需要从jar中提取层，这样Docker镜像就会有合适的层。
        要检查任何分层jar的层，我们可以运行以下命令：
        `java -Djarmode=layertools -jar demo-0.0.1.jar list`

        然后，为了提取它们，我们会运行：
        `java -Djarmode=layertools -jar demo-0.0.1.jar extract`

    3. 创建Docker镜像
        将这些层纳入Docker镜像的最简单方法是使用Docker文件：

        ```yml
        FROM adoptopenjdk:11-jre-hotspot as builder
        ARG JAR_FILE=target/*.jar
        COPY ${JAR_FILE} application.jar
        RUN java -Djarmode=layertools -jar application.jar extract

        FROM adoptopenjdk:11-jre-hotspot
        COPY --from=builder dependencies/ ./
        COPY --from=builder snapshot-dependencies/ ./
        COPY --from=builder spring-boot-loader/ ./
        COPY --from=builder application/ ./
        ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
        ```

        这个Docker文件从我们的fat jar中提取各层，然后将各层复制到Docker镜像中。每条COPY指令都会在最终的Docker镜像中产生一个新的层。
        如果我们构建这个Dockerfile，我们可以看到分层jar中的每一层都被添加到Docker镜像中，成为自己的层：

        ```log
        ...
        Step 6/10 : COPY --from=builder dependencies/ ./
        ---> 2c631b8f9993
        Step 7/10 : COPY --from=builder snapshot-dependencies/ ./
        ---> 26e8ceb86b7d
        Step 8/10 : COPY --from=builder spring-boot-loader/ ./
        ---> 6dd9eaddad7f
        Step 9/10 : COPY --from=builder application/ ./
        ---> dc80cc00a655
        ...
        ```

5. 总结

    在本教程中，我们看到了用Spring Boot构建Docker镜像的各种方法。使用buildpacks，我们可以得到合适的Docker镜像，不需要模板或自定义配置。或者，只要多花点力气，我们就可以使用分层jar来获得更有针对性的Docker镜像。

    关于使用Java和Docker的进一步信息，请查看[jib](https://www.baeldung.com/jib-dockerizing)上的教程。

## 在Docker中用配置文件启动Spring Boot应用程序

1. 简介

    我们都知道Docker有多流行，而且对Java开发者来说，将Spring Boot应用容器化是多么的时尚。然而，对于一些开发人员来说，如何在Docker化的Spring Boot应用程序中设置配置文件是一个问题。

    在本教程中，我们将解释如何在Docker容器中启动带有配置文件的Spring Boot应用程序。

2. 基本的Docker文件

    一般来说，要对Spring Boot应用程序进行docker化，我们只需提供一个Dockerfile。

    让我们看看我们的Spring Boot应用程序的最小Docker文件：

    ```yml
    FROM openjdk:11
    COPY target/*.jar app.jar
    ENTRYPOINT ["java", "-jar", "/app.jar"]
    ```

    当然，我们可以通过docker build来构建我们的Docker镜像：

    `docker build --tag=docker-with-spring-profile:latest .`

    这样，我们就可以从docker-with-spring-profile镜像中运行我们的应用程序了：

    `docker run docker-with-spring-profile:latest`

    正如我们注意到的，我们的Spring Boot应用程序以 "默认 "配置文件启动：

    ```log
    2022-04-22 22:34:25.268 INFO 1 --- [main] c.b.docker.spring.DemoApplication: Starting DemoApplication using Java 11.0.14.1 on ea8851bea75f with PID 1 (/app.jar started by root in /)
    2022-04-22 22:34:25.270 INFO 1 --- [main] c.b.docker.spring.DemoApplication: No active profile set, falling back to 1 default profile: "default"
    //...
    ```

3. 在Dockerfile中设置配置文件

    为我们的docker化应用程序设置配置文件的一种方法是使用Spring Boot的命令行参数"-Dspring.profiles.active"。

    因此，为了将配置文件设置为 "test"，我们在Dockerfile的ENTRYPOINT行中添加一个新参数"-Dspring.profiles.active=test"：

    `ENTRYPOINT ["java", "-Dspring.profiles.active=test", "-jar", "/app.jar"]`

    为了看到配置文件的变化，让我们用同样的命令再次运行我们的容器：

    `docker run docker-with-spring-profile:latest`

    据此，我们可以看到配置文件 "test "成功地被我们的应用程序接收了：

    ```yml
    2022-04-22 22:39:33.210 INFO 1 --- [main] c.b.docker.spring.DemoApplication: Starting DemoApplication using Java 11.0.14.1 on 227974fa84b2 with PID 1 (/app.jar started by root in /)
    2022-04-22 22:39:33.212 INFO 1 --- [main] c.b.docker.spring.DemoApplication: The following 1 profile is active: "test"
    //...
    ```

4. 使用环境变量设置配置文件

    有时，在我们的Docker文件中使用一个硬编码的配置文件并不方便。如果我们需要一个以上的配置文件，当我们运行我们的容器时，选择其中一个可能是很麻烦的。

    尽管如此，还是有一个更好的选择。在启动过程中，Spring Boot会寻找一个特殊的环境变量：SPRING_PROFILES_ACTIVE。

    因此，我们实际上可以通过docker run命令来利用它，在启动时设置Spring配置文件：

    `docker run -e "SPRING_PROFILES_ACTIVE=test" docker-with-spring-profile:latest`

    此外，根据我们的使用情况，我们可以通过逗号分隔的字符串一次设置多个配置文件：

    `docker run -e "SPRING_PROFILES_ACTIVE=test1,test2,test3" docker-with-spring-profile:latest`

    然而，我们应该注意，Spring Boot在属性之间有一个特定的顺序。命令行参数比环境变量有优先权。由于这个原因，为了使SPRING_PROFILES_ACTIVE工作，我们需要恢复我们的Dockerfile。

    因此，我们从Dockerfile的ENTRYPOINT行中删除"-Dspring.profiles.active=test"参数：

    `ENTRYPOINT ["java", "-jar", "/app.jar"]`

    最后，我们可以看到，我们通过SPRING_PROFILES_ACTIVE设置的配置文件被考虑进去了：

    ```log
    2022-04-22 22:50:28.924 INFO 1 --- [main] c.b.docker.spring.DemoApplication: Starting DemoApplication using Java 11.0.14.1 on 18eacb6362f8 with PID 1 (/app.jar started by root in /)
    2022-04-22T22:50:28.926562249Z 2022-04-22 22:50:28.926 INFO 1 --- [main] c.b.docker.spring.DemoApplication: The following 3 profiles are active: "test1", "test2", "test3"
    //..
    ```

5. 在Docker组合文件中设置配置文件

    作为一种替代方法，环境变量也可以在docker-compose文件中提供。

    此外，为了更好地利用我们的docker运行操作，我们可以为每个配置文件创建一个docker-compose文件。

    让我们为 "test" 配置文件创建一个docker-compos-test.yml文件：

    ```yml
    version: "3.5"
    services:
    docker-with-spring-profile:
        image: docker-with-spring-profile:latest
        environment:
        - "SPRING_PROFILES_ACTIVE=test"
    ```

    同样，我们为 "prod" 配置文件创建另一个文件，docker-compos-prod.yml - 唯一的区别是第二个文件中的配置文件 "prod"：

    ```yml
    //...
    environment:
    - "SPRING_PROFILES_ACTIVE=prod"
    ```

    因此，我们可以通过两个不同的docker-compose文件运行我们的容器：

    ```bash
    # for the profile 'test'
    docker-compose -f docker-compose-test.yml up
    # for the profile 'prod'
    docker-compose -f docker-compose-prod.yml up
    ```

6. 总结

    在本教程中，我们描述了在docker化的Spring Boot应用程序中设置配置文件的不同方法，还展示了一些使用Docker和Docker Compose的例子。

## 用Spring Boot重复使用Docker层

1. 简介

    Docker是创建独立应用程序的事实上的标准。从2.3.0版本开始，Spring Boot包括一些增强功能，以帮助我们创建高效的Docker镜像。因此，它允许将应用程序分解为不同的层。

    换句话说，源代码驻留在它自己的层中。因此，它可以独立重建，提高了效率和启动时间。在本教程中，我们将看到如何利用Spring Boot的新功能来重新使用Docker层。

2. Docker中的分层Jars

    Docker容器由一个基本镜像和附加层组成。一旦构建了这些层，它们就会保持缓存状态。因此，后续生成的速度会快很多：

    ![docker层](pic/docker-layers.jpg)

    低层的变化也会重建上层的。因此，不经常变化的层应该保持在底部，而经常变化的层应该放在顶部。

    以同样的方式，Spring Boot允许将工件的内容映射到层中。让我们看看层的默认映射：

    ![Spring Boot层](pic/spring-boot-layers.jpg)

    我们可以看到，应用程序有自己的层。在修改源代码时，只有独立层被重建。装载器和依赖项保持缓存，减少Docker镜像的创建和启动时间。让我们看看如何用Spring Boot做到这一点!

3. 用Spring Boot创建高效的Docker镜像

    在构建Docker镜像的传统方式中，Spring Boot使用[fat jar](https://www.baeldung.com/spring-boot-docker-images#traditional-docker-builds)方法。因此，一个单一的工件嵌入了所有的依赖性和应用程序的源代码。因此，我们的源代码的任何变化都会迫使我们重新构建整个层。

    1. 使用Spring Boot的层配置

        Spring Boot 2.3.0版本引入了两个新功能，以改善Docker镜像的生成：

        - [Buildpack支持](https://www.baeldung.com/spring-boot-docker-images#buildpacks)为应用程序提供Java运行时，所以现在可以跳过Dockerfile，自动构建Docker镜像了
        - [分层jar](https://www.baeldung.com/spring-boot-docker-images#layered-jars)帮助我们最大限度地利用Docker层的生成
        在本教程中，我们将扩展分层jar的方法。

        最初，我们将在[Maven中设置分层jar](https://www.baeldung.com/spring-boot-docker-images#1-creating-layered-jars)。在打包工件时，我们将生成层。让我们检查一下jar文件：

        `jar tf target/spring-boot-docker-0.0.1-SNAPSHOT.jar`

        我们可以看到，在fat jar里面的BOOT-INF文件夹中，新的layer.idx文件被创建。当然，它将依赖关系、资源和应用程序源代码映射到独立的层：

        `BOOT-INF/layers.idx`

        同样，该文件的内容也将不同的层分解存储：

        ```txt
        - "dependencies":
        - "BOOT-INF/lib/"
        - "spring-boot-loader":
        - "org/"
        - "snapshot-dependencies":
        - "application":
        - "BOOT-INF/classes/"
        - "BOOT-INF/classpath.idx"
        - "BOOT-INF/layers.idx"
        - "META-INF/"
        ```

    2. 与图层交互

        让我们列出工件里面的层：

        `java -Djarmode=layertools -jar target/docker-spring-boot-0.0.1.jar list`

        结果提供了一个简单的图层.idx文件的内容：

        ```txt
        dependencies
        spring-boot-loader
        snapshot-dependencies
        application
        ```

        我们也可以将图层提取到文件夹中：

        `java -Djarmode=layertools -jar target/docker-spring-boot-0.0.1.jar extract`

        然后，我们可以重复使用Docker文件里面的文件夹，我们将在下一节看到：

        ```bash
        $ ls
        application/
        snapshot-dependencies/
        dependencies/
        spring-boot-loader/
        ```

    3. Docker文件配置

        为了充分利用Docker的功能，我们需要在我们的镜像中添加各层。

        首先，让我们把fat jar文件添加到基础镜像中：

        ```dockerfile
        FROM adoptopenjdk:11-jre-hotspot as builder
        ARG JAR_FILE=target/*.jar
        COPY ${JAR_FILE} application.jar
        ```

        其次，让我们提取工件的层：

        `RUN java -Djarmode=layertools -jar application.jar extract`

        最后，让我们复制提取的文件夹来添加相应的Docker层：

        ```dockerfile
        FROM adoptopenjdk:11-jre-hotspot
        COPY --from=builder dependencies/ ./
        COPY --from=builder snapshot-dependencies/ ./
        COPY --from=builder spring-boot-loader/ ./
        COPY --from=builder application/ ./
        ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
        ```

        有了这个配置，当我们改变我们的源代码时，我们将只重建应用层。其余的将保持缓存。

4. 自定义层

    看起来一切都工作得很顺利。但是，如果我们仔细观察，依赖层在我们的构建中是不共享的。也就是说，所有的都是一个层，即使是内部的也是如此。因此，如果我们改变了一个内部库的类，我们将再次重建所有的依赖层。

    1. 使用Spring Boot的自定义层配置

        在Spring Boot中，我们可以通过一个单独的配置文件来调整[自定义层](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/#repackage-layers-configuration)：

        ```xml
        <layers xmlns="http://www.springframework.org/schema/boot/layers"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.springframework.org/schema/boot/layers
                            https://www.springframework.org/schema/boot/layers/layers-2.3.xsd">
            <application>
                <into layer="spring-boot-loader">
                    <include>org/springframework/boot/loader/**</include>
                </into>
                <into layer="application" />
            </application>
            <dependencies>
                <into layer="snapshot-dependencies">
                    <include>*:*:*SNAPSHOT</include>
                </into>
                <into layer="dependencies" />
            </dependencies>
            <layerOrder>
                <layer>dependencies</layer>
                <layer>spring-boot-loader</layer>
                <layer>snapshot-dependencies</layer>
                <layer>application</layer>
            </layerOrder>
        </layers>
        ```

        正如我们所看到的，我们正在将依赖关系和资源映射并排序到层中。此外，我们可以根据自己的需要添加任意多的自定义层。

        我们将文件命名为layer.xml。然后，在Maven中，我们可以配置这个文件来定制层：

        ```xml
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <layers>
                    <enabled>true</enabled>
                    <configuration>${project.basedir}/src/layers.xml</configuration>
                </layers>
            </configuration>
        </plugin>
        ```

        如果我们将工件打包，其结果将与默认行为类似。

    2. 添加新的层

        让我们创建一个内部依赖，添加我们的应用程序类：

        ```xml
        <into layer="internal-dependencies">
            <include>com.baeldung.docker:*:*</include>
        </into>
        ```

        此外，我们将对新层进行排序：

        ```xml
        <layerOrder>
            <layer>internal-dependencies</layer>
        </layerOrder>
        ```

        结果是，如果我们列出胖子罐子里面的层，就会出现新的内部依赖关系：

        ```txt
        dependencies
        spring-boot-loader
        internal-dependencies
        snapshot-dependencies
        application
        ```

    3. Dockerfile配置

        一旦提取完毕，我们就可以将新的内部层添加到我们的Docker镜像中：

        `COPY --from=builder internal-dependencies/ ./`

        因此，如果我们生成镜像，我们会看到Docker是如何将内部依赖关系构建为一个新层的：

        ```bash
        $ mvn package
        $ docker build -f src/main/docker/Dockerfile . --tag spring-docker-demo
        ....
        Step 8/11 : COPY --from=builder internal-dependencies/ ./
        ---> 0e138e074118
        .....
        ```

        之后，我们可以在历史中检查Docker镜像中的层的组成：

        ```bash
        $ docker history --format "{{.ID}} {{.CreatedBy}} {{.Size}}" spring-docker-demo
        c0d77f6af917 /bin/sh -c #(nop)  ENTRYPOINT ["java" "org.s… 0B
        762598a32eb7 /bin/sh -c #(nop) COPY dir:a87b8823d5125bcc4… 7.42kB
        80a00930350f /bin/sh -c #(nop) COPY dir:3875f37b8a0ed7494… 0B
        0e138e074118 /bin/sh -c #(nop) COPY dir:db6f791338cb4f209… 2.35kB
        e079ad66e67b /bin/sh -c #(nop) COPY dir:92a8a991992e9a488… 235kB
        77a9401bd813 /bin/sh -c #(nop) COPY dir:f0bcb2a510eef53a7… 16.4MB
        2eb37d403188 /bin/sh -c #(nop)  ENV JAVA_HOME=/opt/java/o… 0B
        ```

        正如我们所看到的，该层现在包括项目的内部依赖。

5. 总结

    在本教程中，我们展示了如何生成高效的Docker镜像。简而言之，我们使用了Spring Boot的新功能来创建分层jar。对于简单的项目，我们可以使用默认配置。我们还演示了一种更高级的配置，以重用各层。

## Relevant Articles

- [x] [Creating Docker Images with Spring Boot](https://www.baeldung.com/spring-boot-docker-images)
- [x] [Starting Spring Boot Application in Docker With Profile](https://www.baeldung.com/spring-boot-docker-start-with-profile)
- [ ] [Reusing Docker Layers with Spring Boot](https://www.baeldung.com/docker-layers-spring-boot)

## Code

本教程中的所有例子都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/docker-modules/docker-spring-boot)上找到。
