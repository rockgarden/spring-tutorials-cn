# [用Spring Boot创建Docker镜像](https://www.baeldung.com/spring-boot-docker-images)

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
