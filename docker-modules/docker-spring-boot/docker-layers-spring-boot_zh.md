# [用Spring Boot重复使用Docker层](https://www.baeldung.com/docker-layers-spring-boot)

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
