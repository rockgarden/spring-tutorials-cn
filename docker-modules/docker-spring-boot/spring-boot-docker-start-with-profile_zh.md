# [在Docker中用配置文件启动Spring Boot应用程序](https://www.baeldung.com/spring-boot-docker-start-with-profile)

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
