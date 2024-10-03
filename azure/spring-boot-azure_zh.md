# [将 Spring Boot 应用程序部署到 Azure](https://www.baeldung.com/spring-boot-azure)

1. 简介

    Microsoft Azure 现在提供了相当可靠的 Java 支持。

    在本教程中，我们将逐步演示如何让 Spring Boot 应用程序在 Azure 平台上运行。

2. Maven 依赖和配置

    首先，我们需要一个 Azure 订阅才能使用那里的云服务；目前，我们可以在[这里](https://azure.microsoft.com/en-us/free/)注册一个免费账户。

    接下来，登录平台并使用 [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/index?view=azure-cli-latest) 创建一个服务委托：

    ```bash
    > az login
    To sign in, use a web browser to open the page \
    https://microsoft.com/devicelogin and enter the code XXXXXXXX to authenticate.
    > az ad sp create-for-rbac --name "app-name" --password "password"
    {
        "appId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "displayName": "app-name",
        "name": "http://app-name",
        "password": "password",
        "tenant": "tttttttt-tttt-tttt-tttt-tttttttttttt"
    }
    ```

    现在，我们借助 `<servers>` 下的以下部分，在 Maven [settings.xml](https://maven.apache.org/settings.html) 中配置 Azure 服务 principal 身份验证设置：

    ```xml
    <server>
        <id>azure-auth</id>
        <configuration>
            <client>aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa</client>
            <tenant>tttttttt-tttt-tttt-tttt-tttttttttttt</tenant>
            <key>password</key>
            <environment>AZURE</environment>
        </configuration>
    </server>
    ```

    在使用 azure-webapp-maven-plugin 将 Spring Boot 应用程序上传到 Microsoft 平台时，我们将依赖上述身份验证配置。

    让我们在 pom.xml 中添加以下 Maven 插件：

    ```xml
    <plugin>
        <groupId>com.microsoft.azure</groupId>
        <artifactId>azure-webapp-maven-plugin</artifactId>
        <version>1.1.0</version>
        <configuration>
            <!-- ... -->
        </configuration>
    </plugin>
    ```

    我们可以在[这里](https://mvnrepository.com/artifact/com.microsoft.azure/azure-webapp-maven-plugin)查看最新的发布版本。

    该插件有许多可配置的属性，将在下面的介绍中介绍。

3. 将 Spring Boot 应用程序部署到 Azure

    现在我们已经设置好了环境，让我们尝试将 Spring Boot 应用程序部署到 Azure。

    访问 “/hello” 时，我们的应用程序会回复 “hello azure!”：

    ```java
    @GetMapping("/hello")
    public String hello() {
        return "hello azure!";
    }
    ```

    该平台现在允许为 Tomcat 和 Jetty 部署 Java Web App。使用 azure-webapp-maven-plugin，我们可以将应用程序作为默认（ROOT）应用程序直接部署到支持的 Web 容器上，也可以通过 FTP 进行部署。

    请注意，由于我们要将应用程序部署到 Web 容器，因此应将其打包为 WAR 存档。作为快速提醒，我们有一篇[文章](https://www.baeldung.com/spring-boot-war-tomcat-deploy)介绍了如何将 Spring Boot WAR 部署到 Tomcat。

    1. 网络容器部署

        如果我们打算部署到 Windows 实例上的 Tomcat，我们将为 azure-webapp-maven-plugin 使用以下配置：

        ```xml
        <configuration>
            <javaVersion>1.8</javaVersion>
            <javaWebContainer>tomcat 8.5</javaWebContainer>
            <!-- ... -->
        </configuration>
        ```

        对于 Linux 实例，请尝试以下配置：

        ```xml
        <configuration>
            <linuxRuntime>tomcat 8.5-jre8</linuxRuntime>
            <!-- ... -->
        </configuration>
        ```

        别忘了 Azure 身份验证：

        ```xml
        <configuration>
            <authentication>
                <serverId>azure-auth</serverId>
            </authentication>
            <appName>spring-azure</appName>
            <resourceGroup>baeldung</resourceGroup>
            <!-- ... -->
        </configuration>
        ```

        当我们将应用程序部署到 Azure 时，我们会看到它以应用程序服务的形式出现。因此，我们在这里指定了 `<appName>` 属性来命名应用程序服务。此外，App 服务作为一种资源，需要由[资源组容器](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-overview)持有，因此 `<resourceGroup>` 也是必需的。

        现在，我们准备使用 azure-webapp:deploy Maven 目标触发器，我们将看到输出结果：

        ```bash
        > mvn clean package azure-webapp:deploy
        ...
        [INFO] Start deploying to Web App spring-baeldung...
        [INFO] Authenticate with ServerId: azure-auth
        [INFO] [Correlation ID: cccccccc-cccc-cccc-cccc-cccccccccccc] \
        Instance discovery was successful
        [INFO] Target Web App doesn't exist. Creating a new one...
        [INFO] Creating App Service Plan 'ServicePlanssssssss-bbbb-0000'...
        [INFO] Successfully created App Service Plan.
        [INFO] Successfully created Web App.
        [INFO] Starting to deploy the war file...
        [INFO] Successfully deployed Web App at \
        https://spring-baeldung.azurewebsites.net
        ...
        ```

        现在我们可以访问 <https://spring-baeldung.azurewebsites.net/hello> 并查看响应：‘hello azure!’。

        在部署过程中，Azure 自动为我们创建了一个应用程序服务计划。有关 Azure 应用服务计划的详细信息，请查看[官方文档](https://docs.microsoft.com/en-us/azure/app-service/azure-web-sites-web-hosting-plans-in-depth-overview)。如果我们已经有一个应用程序服务计划，可以设置属性 `<appServicePlanName>` 以避免创建新计划：

        ```xml
        <configuration>
            <!-- ... -->
            <appServicePlanName>ServicePlanssssssss-bbbb-0000</appServicePlanName>
        </configuration>
        ```

    2. FTP 部署

        要通过 FTP 部署，我们可以使用以下配置：

        ```xml
        <configuration>
            <authentication>
                <serverId>azure-auth</serverId>
            </authentication>
            <appName>spring-baeldung</appName>
            <resourceGroup>baeldung</resourceGroup>
            <javaVersion>1.8</javaVersion>

            <deploymentType>ftp</deploymentType>
            <resources>
                <resource>
                    <directory>${project.basedir}/target</directory>
                    <targetPath>webapps</targetPath>
                    <includes>
                        <include>*.war</include>
                    </includes>
                </resource>
            </resources>
        </configuration>
        ```

        在上面的配置中，我们让插件在 ${project.basedir}/target 目录中定位 WAR 文件，并将其部署到 Tomcat 容器的 webapps 目录中。

        假设我们的最终工件名为 azure-0.1.war，那么一旦开始部署，我们就会看到如下输出：

        ```bash
        > mvn clean package azure-webapp:deploy
        ...
        [INFO] Start deploying to Web App spring-baeldung...
        [INFO] Authenticate with ServerId: azure-auth
        [INFO] [Correlation ID: cccccccc-cccc-cccc-cccc-cccccccccccc] \
        Instance discovery was successful
        [INFO] Target Web App doesn't exist. Creating a new one...
        [INFO] Creating App Service Plan 'ServicePlanxxxxxxxx-xxxx-xxxx'...
        [INFO] Successfully created App Service Plan.
        [INFO] Successfully created Web App.
        ...
        [INFO] Finished uploading directory: \
        /xxx/.../target/azure-webapps/spring-baeldung --> /site/wwwroot
        [INFO] Successfully uploaded files to FTP server: \
        xxxx-xxxx-xxx-xxx.ftp.azurewebsites.windows.net
        [INFO] Successfully deployed Web App at \
        https://spring-baeldung.azurewebsites.net
        ```

        请注意，这里我们没有将应用程序部署为 Tomcat 的默认 Web 应用程序，因此我们只能通过 “<https://spring-baeldung.azurewebsites.net/azure-0.1/hello>” 访问它。服务器将如期响应 “hello azure!”。

4. 使用自定义应用程序设置进行部署

    大多数情况下，我们的 Spring Boot 应用程序需要访问数据来提供服务。Azure 现在支持 SQL Server、MySQL 和 PostgreSQL 等数据库。

    为简单起见，我们将使用其应用程序内 MySQL 作为数据源，因为其配置与其他 Azure 数据库服务非常相似。

    1. 在 Azure 上启用应用程序内 MySQL

        由于没有创建启用 In-App MySQL 的 Web 应用程序的单行程序，我们必须首先使用 CLI 创建 Web 应用程序：

        ```bash
        az group create --location japanwest --name bealdung-group
        az appservice plan create --name baeldung-plan --resource-group bealdung-group --sku B1
        az webapp create --name baeldung-webapp --resource-group baeldung-group \
            --plan baeldung-plan --runtime java|1.8|Tomcat|8.5
        ```

        然后在门户中启用应用程序中的 MySQL。

        启用应用程序内 MySQL 后，我们可以在文件系统 /home/data/mysql 目录下名为 MYSQLCONNSTR_xxx.txt 的文件中找到默认数据库、数据源 URL 和默认账户信息。

    2. 使用 Azure 应用程序内 MySQL 的 Spring Boot 应用程序

        在此，为了演示需要，我们创建了一个用户实体和两个用于注册和列出用户的端点：

        ```java
        @PostMapping("/user")
        public String register(@RequestParam String name) {
            userRepository.save(userNamed(name));
            return "registered";
        }

        @GetMapping("/user")
        public Iterable<User> userlist() {
            return userRepository.findAll();
        }
        ```

        我们将在本地环境中使用 H2 数据库，然后将其切换到 Azure 上的 MySQL。通常，我们在 application.properties 文件中配置数据源属性：

        ```properties
        spring.datasource.url=jdbc:h2:file:~/test
        spring.datasource.username=sa
        spring.datasource.password=
        ```

        对于 Azure 部署，我们需要在 `<appSettings>` 中配置 azure-webapp-maven-plugin：

        ```xml
        <configuration>
            <authentication>
                <serverId>azure-auth</serverId>
            </authentication>
            <javaVersion>1.8</javaVersion>
            <resourceGroup>baeldung-group</resourceGroup>
            <appName>baeldung-webapp</appName>
            <appServicePlanName>bealdung-plan</appServicePlanName>
            <appSettings>
                <property>
                    <name>spring.datasource.url</name>
                    <value>jdbc:mysql://127.0.0.1:55738/localdb</value>
                </property>
                <property>
                    <name>spring.datasource.username</name>
                    <value>uuuuuu</value>
                </property>
                <property>
                    <name>spring.datasource.password</name>
                    <value>pppppp</value>
                </property>
            </appSettings>
        </configuration>
        ```

        现在我们可以开始部署了：

        ```log
        > mvn clean package azure-webapp:deploy
        ...
        [INFO] Start deploying to Web App custom-webapp...
        [INFO] Authenticate with ServerId: azure-auth
        [INFO] [Correlation ID: cccccccc-cccc-cccc-cccc-cccccccccccc] \
        Instance discovery was successful
        [INFO] Updating target Web App...
        [INFO] Successfully updated Web App.
        [INFO] Starting to deploy the war file...
        [INFO] Successfully deployed Web App at \
        https://baeldung-webapp.azurewebsites.net
        ```

        我们可以从日志中看到部署已经完成。

        让我们测试一下新的端点：

        ```bash
        > curl -d "" -X POST https://baeldung-webapp.azurewebsites.net/user\?name\=baeldung
        registered
        > curl https://baeldung-webapp.azurewebsites.net/user
        [{"id":1,"name":"baeldung"}]
        ```

        服务器的响应说明了一切。运行正常！

5. 将容器化 Spring Boot 应用程序部署到 Azure

    在前面的章节中，我们介绍了如何将应用程序部署到 servlet 容器（本例中为 Tomcat）。那么作为独立的可运行 jar 部署如何呢？

    现在，我们可能需要将 [Spring Boot 应用程序容器化](https://www.baeldung.com/dockerizing-spring-boot-application)。具体来说，我们可以对其进行 dockerize，然后将映像上传到 Azure。

    我们已经有一篇文章介绍了如何将 Spring Boot 应用程序容器化，但在这里，我们将使用另一个 maven 插件：docker-maven-plugin，为我们自动进行容器化：

    ```xml
    <plugin>
        <groupId>com.spotify</groupId>
        <artifactId>docker-maven-plugin</artifactId>
        <version>1.1.0</version>
        <configuration>
            <!-- ... -->
        </configuration>
    </plugin>
    ```

    最新版本可在[此处](https://mvnrepository.com/artifact/com.spotify/docker-maven-plugin)找到。

    1. Azure 容器注册中心

        首先，我们需要在 Azure 上创建一个容器注册表来上传我们的 docker 镜像。

        因此，让我们创建一个：

        ```bash
        az acr create --admin-enabled --resource-group baeldung-group \
        --location japanwest --name baeldungadr --sku Basic
        ```

        我们还需要容器注册表的身份验证信息，这可以通过以下方式查询：

        ```bash
        > az acr credential show --name baeldungadr --query passwords[0]
        {
        "additionalProperties": {},
        "name": "password",
        "value": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        }
        ```

        然后在 Maven 的 settings.xml 中添加以下服务器验证配置：

        ```xml
        <server>
            <id>baeldungadr</id>
            <username>baeldungadr</username>
            <password>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</password>
        </server>
        ```

    2. Maven 插件配置

        让我们在 pom.xml 中添加以下 Maven 插件配置：

        ```xml
        <properties>
            <!-- ... -->
            <azure.containerRegistry>baeldungadr</azure.containerRegistry>
            <docker.image.prefix>${azure.containerRegistry}.azurecr.io</docker.image.prefix>
        </properties>

        <build>
            <plugins>
                <plugin>
                    <groupId>com.spotify</groupId>
                    <artifactId>docker-maven-plugin</artifactId>
                    <version>1.0.0</version>
                    <configuration>
                        <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
                        <registryUrl>https://${docker.image.prefix}</registryUrl>
                        <serverId>${azure.containerRegistry}</serverId>
                        <dockerDirectory>docker</dockerDirectory>
                        <resources>
                            <resource>
                                <targetPath>/</targetPath>
                                <directory>${project.build.directory}</directory>
                                <include>${project.build.finalName}.jar</include>
                            </resource>
                        </resources>
                    </configuration>
                </plugin>
                <!-- ... -->
            </plugins>
        </build>
        ```

        在上面的配置中，我们指定了 docker 镜像名称、注册表 URL 和一些与 FTP 部署类似的属性。

        请注意，插件将使用 `<dockerDirectory>` 中的值来定位 Dockerfile。我们把 Dockerfile 放在 docker 目录中，其内容如下

        ```bash
        FROM frolvlad/alpine-oraclejdk8:slim
        VOLUME /tmp
        ADD azure-0.1.jar app.jar
        RUN sh -c 'touch /app.jar'
        EXPOSE 8080
        ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
        ```

    3. 在 Docker 实例中运行 Spring Boot 应用程序

        现在，我们可以构建 Docker 映像并将其推送到 Azure 注册表：

        ```bash
        > mvn docker:build -DpushImage
        ...
        [INFO] Building image baeldungadr.azurecr.io/azure-0.1
        ...
        Successfully built aaaaaaaaaaaa
        Successfully tagged baeldungadr.azurecr.io/azure-0.1:latest
        [INFO] Built baeldungadr.azurecr.io/azure-0.1
        [INFO] Pushing baeldungadr.azurecr.io/azure-0.1
        The push refers to repository [baeldungadr.azurecr.io/azure-0.1]
        ...
        latest: digest: sha256:0f0f... size: 1375
        ```

        上传完成后，让我们检查一下 baeldungadr 注册表。我们将在版本库列表中看到映像。

        现在我们准备运行映像的实例。

        实例启动后，我们可以通过其公共 IP 地址访问应用程序提供的服务：

        ```bash
        > curl http://a.x.y.z:8080/hello
        hello azure!
        ```

    4. Docker 容器部署

        假设我们有一个容器注册表，无论是来自 Azure、Docker Hub 还是我们的私有注册表。

        借助 azure-webapp-maven-plugin 的以下配置，我们也可以将 Spring Boot Web 应用程序部署到容器中：

        ```xml
        <configuration>
            <containerSettings>
                <imageName>${docker.image.prefix}/${project.artifactId}</imageName>
                <registryUrl>https://${docker.image.prefix}</registryUrl>
                <serverId>${azure.containerRegistry}</serverId>
            </containerSettings>
            <!-- ... -->
        </configuration>
        ```

        一旦我们运行 mvn azure-webapp:deploy，该插件就会帮助我们将网络应用存档部署到指定映像的实例中。

        然后，我们就可以通过实例的 IP 地址或 Azure 应用服务的 URL 访问网络服务了。

6. 总结

    在本文中，我们介绍了如何将 Spring Boot 应用程序作为容器中可部署的 WAR 或可运行的 JAR 部署到 Azure。虽然我们已经介绍了 azure-webapp-maven-plugin 的大部分功能，但还有一些丰富的功能有待开发。请点击[此处](https://github.com/Microsoft/azure-maven-plugins/tree/master/azure-webapp-maven-plugin)了解更多详情。
