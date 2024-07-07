# Jib

## 使用Jib对Java应用程序进行Docker化

1. 概述

    在本教程中，我们将了解Jib以及它如何简化Java应用的容器化。

    我们将采用一个简单的Spring Boot应用程序，并使用Jib构建其Docker镜像。然后，我们还将把镜像发布到远程注册中心。

    请务必参考我们关于使用dockerfile和docker工具对[Spring Boot应用程序进行docker化的教程](https://www.baeldung.com/dockerizing-spring-boot-application)。

2. Jib简介

    Jib是一个开源的Java工具，由Google维护，用于构建Java应用程序的Docker镜像。它简化了容器化，因为有了它，我们不需要编写dockerfile。

    实际上，我们甚至不需要安装docker来创建和发布docker镜像。

    谷歌将Jib作为Maven和Gradle插件发布。这很好，因为这意味着每次构建时，Jib都会捕捉到我们对应用的任何改动。这为我们节省了单独的docker build/push命令，并简化了将其添加到CI管道中。

3. 一个简单的问候应用程序

    让我们来看看一个简单的spring-boot应用，并使用Jib将其docker化。它将暴露一个简单的GET端点：

    <http://localhost:8080/greeting>

    我们可以用一个Spring MVC控制器简单地做到这一点：

    ```java
    @RestController
    public class GreetingController {
        private static final String template = "Hello Docker, %s!";
        private final AtomicLong counter = new AtomicLong();
        @GetMapping("/greeting")
        public Greeting greeting(@RequestParam(value="name", 
            defaultValue="World") String name) {
            return new Greeting(counter.incrementAndGet(),
            String.format(template, name));
        }
    }
    ```

4. 准备部署

    我们还需要在本地进行设置，以便与我们要部署的Docker仓库进行认证。

    在这个例子中，我们将向.m2/settings.xml提供我们的DockerHub凭证：

    ```xml
    <servers>
        <server>
            <id>registry.hub.docker.com</id>
            <username><DockerHub Username></username>
            <password><DockerHub Password></password>
        </server>
    </servers>
    ```

    也有其他方法来提供证书。谷歌推荐的方法是使用辅助工具，它可以在文件系统中以加密的格式存储凭证。在这个例子中，我们可以使用[docker-credential-helpers](https://github.com/docker/docker-credential-helpers#available-programs)，而不是在settings.xml中存储纯文本的凭证，这要安全得多。

    - [ ] **TODO** 配置 docker-credential-helpers

5. 使用Jib部署到Docker Hub

    先配置容器中心的目标路径。

    例如，要将图像baeldungjib/spring-jib-app-xx上传到DockerHub（xx 取 rg），我们应该这样做：

    `export IMAGE_PATH=registry.hub.docker.com/baeldungjib/spring-jib-app-rg`

    现在，我们可以使用jib-maven-plugin或[Gradle等价物](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin)，用一个简单的命令将我们的应用程序容器化：

    `mvn compile com.google.cloud.tools:jib-maven-plugin:3.3.2:build -Dimage=$IMAGE_PATH`

    其中IMAGE_PATH是目标路径，另 jib-maven-plugin:x.x.x 版本尽量取最新。

    就这样了! 这将建立我们应用程序的docker镜像，并将其推送到DockerHub。

    当然，我们也可以用类似的方法将镜像上传到Google Container Registry或Amazon Elastic Container Registry。

    - [ ] **ERROR** Failed to verify the server(<https://registry.hub.docker.com>) because only secure connections are allowed.

      ```log
      Failed to execute goal com.google.cloud.tools:jib-maven-plugin:3.3.2:build (default-cli) on project jib: 
      Build image failed, perhaps you should use a registry that supports HTTPS or set the configuration parameter 'allowInsecureRegistries': 
      Failed to verify the server at https://registry.hub.docker.com/v2/ because only secure connections are allowed. 
      Certificate for <registry.hub.docker.com> doesn't match any of the subject alternative names: 
      [*.facebook.com, *.facebook.net, *.fbcdn.net, *.fbsbx.com, *.m.facebook.com, *.messenger.com, *.xx.fbcdn.net, *.xy.fbcdn.net, *.xz.fbcdn.net, facebook.com, messenger.com]
      ```

6. 简化Maven命令

    此外，我们还可以像其他maven插件一样，在pom中配置该插件，以此来缩短我们的初始命令。

    ```xml
    <project>
        ...
        <build>
            <plugins>
                ...
                <plugin>
                    <groupId>com.google.cloud.tools</groupId>
                    <artifactId>jib-maven-plugin</artifactId>
                    <version>2.5.0</version>
                    <configuration>
                        <to>
                            <image>${image.path}</image>
                        </to>
                    </configuration>
                </plugin>
                ...
            </plugins>
        </build>
        ...
    </project>
    ```

    有了这个改动，我们就可以简化我们的maven命令了：

    `mvn compile jib:build`

7. 定制Docker Aspects

    默认情况下，Jib对我们想要的东西做了一些合理的猜测，比如FROM和ENTRYPOINT。

    让我们对我们的应用程序做一些更具体的改变，以满足我们的需求。

    首先，Spring Boot默认暴露的端口是8080。

    但是，假设我们想让我们的应用程序在8082端口运行，并通过容器使其可被暴露。

    当然，我们会在Boot中做适当的修改。之后，我们可以用Jib来使它在镜像中可公开：

    ```xml
    <configuration>
        ...
        <container>
            <ports>
                <port>8082</port>
            </ports>
        </container>
    </configuration>
    ```

    或者，让我们说我们需要一个不同的FROM。默认情况下，Jib使用无发行版的[java镜像](https://github.com/GoogleContainerTools/distroless/tree/master/java)。

    如果我们想在一个不同的基础镜像上运行我们的应用程序，比如[alpine-java](https://hub.docker.com/r/anapsix/alpine-java/)，我们可以用类似的方式来配置：

    ```xml
    <configuration>
        ...
        <from>                           
            <image>openjdk:alpine</image>
        </from>
        ...
    </configuration>
    ```

    我们以同样的方式配置标签、卷和其他几个Docker指令。

8. 定制Java方面

    通过关联，Jib也支持许多Java运行时配置：

    - jvmFlags用于指示要传递给JVM的启动标志。
    - mainClass用于指示主类，默认情况下，Jib会尝试自动推断。
    - args是我们指定传递给main方法的程序参数的地方。
    当然，请确保查看Jib的文档以了解所有可用的[配置属性](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin)。

9. 总结

    在本教程中，我们看到了如何使用谷歌的Jib构建和发布docker镜像，包括如何通过Maven访问docker指令和Java运行时配置。

## Relevant Articles

- [ ] [Dockerizing Java Apps using Jib](https://www.baeldung.com/jib-dockerizing)

## Code

像往常一样，这个例子的源代码可以在[Github](https://github.com/eugenp/tutorials/tree/master/jib)上找到。
