# [Spring 云连接器和 Heroku](https://www.baeldung.com/spring-cloud-heroku)

1. 概述

    在本文中，我们将介绍如何使用 Spring Cloud Connectors 在 Heroku 上设置 Spring Boot 应用程序。

    Heroku 是一种为网络服务提供托管的服务。此外，它们还提供大量第三方服务（称为附加组件），可提供从系统监控到数据库存储的所有服务。

    除此以外，他们还有一个自定义的 CI/CD 管道，可与 Git 无缝集成，从而加快将开发转化为生产的速度。

    Spring 通过 Spring Cloud Connectors 库支持 Heroku。我们将使用它在应用程序中自动配置 PostgreSQL 数据源。

    让我们开始编写应用程序。

2. Spring Boot 图书服务

    首先，让我们创建一个新的简单 [Spring Boot 服务](https://www.baeldung.com/spring-boot-start)。

3. 注册 Heroku

    现在，我们需要注册一个 Heroku 账户。让我们访问 [heroku.com](https://www.heroku.com/home)，点击页面右上角的注册按钮。

    有了账户后，我们需要获取 CLI 工具。我们需要导航到 [heroku-cli](https://devcenter.heroku.com/articles/heroku-cli) 安装页面并安装该软件。这将为我们提供完成本教程所需的工具。

4. 创建 Heroku 应用程序

    现在我们有了 Heroku CLI，让我们回到应用程序。

    1. 初始化 Git 仓库

        使用 git 作为源代码控制时，Heroku 的运行效果最佳。

        首先，进入应用程序的根目录，也就是 pom.xml 文件所在的目录，运行 git init 命令创建一个 Git 仓库。然后运行 git add . 和 `git commit -m “first commit”`。

        现在我们已经将应用程序保存到本地的 git 仓库了。

    2. 配置 Heroku Web 应用程序

        接下来，让我们使用 Heroku CLI 在账户上配置网络服务器。

        首先，我们需要验证 Heroku 账户。在命令行中运行 heroku login，然后按照说明登录并创建 SSH 密钥。

        接下来，运行 heroku create。这将配置网络服务器，并添加一个远程仓库，我们可以将代码推送到该仓库进行部署。我们还会在控制台中看到一个域，复制这个域，以便稍后访问。

    3. 将代码推送到 Heroku

        现在，我们将使用 git 把代码推送到新的 Heroku 仓库。

        运行 git push heroku master 命令将代码推送到 Heroku。

        在控制台输出中，我们应该能看到上传成功的日志，然后他们的系统会下载所有依赖项，构建我们的应用程序，运行测试（如果有的话），如果一切顺利，就会部署应用程序。

        就这样，我们的应用程序就公开部署到网络服务器上了。

5. 在 Heroku 上测试内存

    让我们确保应用程序正常运行。使用创建步骤中的域，测试一下我们的实时应用程序。

    让我们发出一些 HTTP 请求：

    ```shell
    POST https://{heroku-domain}/books HTTP
    {"author":"baeldung","title":"Spring Boot on Heroku"}
    ```

    我们应该得到返回：

    ```json
    {
        "title": "Spring Boot on Heroku",
        "author": "baeldung"
    }
    ```

    现在，让我们尝试读取刚刚创建的对象：

    `GET https://{heroku-domain}/books/1 HTTP`

    应该会返回

    ```json
    {
        "id": 1,
        "title": "Spring Boot on Heroku",
        "author": "baeldung"
    }
    ```

    这一切看起来都不错，但在生产中，我们应该使用永久数据存储。

    让我们来配置一个 PostgreSQL 数据库，并配置 Spring 应用程序来使用它。

6. 添加 PostgreSQL

    要添加 PostgreSQL 数据库，请运行以下命令 `heroku addons:create heroku-postgresql:hobby-dev`

    这将为我们的网络服务器提供一个数据库，并添加一个提供连接信息的环境变量。

    Spring Cloud Connector 配置为检测该变量，并自动设置数据源，因为 Spring 可以检测到我们要使用 PostgreSQL。

    为了让 Spring Boot 知道我们使用的是 PostgreSQL，我们需要做两处更改。

    首先，我们需要添加一个依赖关系，以包含 PostgreSQL 驱动程序：

    ```xml
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.2.10</version>
    </dependency>
    ```

    接下来，让我们添加属性，以便 Spring Data Connectors 可以根据可用资源配置数据库。

    在 src/main/resources 中创建一个 application.properties 文件，并添加以下属性：

    ```properties
    spring.datasource.driverClassName=org.postgresql.Driver
    spring.datasource.maxActive=10
    spring.datasource.maxIdle=5
    spring.datasource.minIdle=2
    spring.datasource.initialSize=5
    spring.datasource.removeAbandoned=true
    spring.jpa.hibernate.ddl-auto=create
    ```

    这将汇集我们的数据库连接，并限制应用程序的连接数。Heroku 将开发层数据库中的活动连接数限制为 10，因此我们将最大连接数设置为 10。此外，我们还将 hibernate.ddl 属性设置为 create，以便自动创建书籍表。

    最后，提交这些更改并运行 git push heroku master。这将把这些更改推送到我们的 Heroku 应用程序中。应用程序启动后，请尝试运行上一节中的测试。

    我们需要做的最后一件事是更改 ddl 设置。让我们也更新一下这个值：

    `spring.jpa.hibernate.ddl-auto=update`

    这将指示应用程序在重新启动时，当实体发生变化时更新模式。像之前一样提交并推送此更改，以便将更改推送到 Heroku 应用程序。

    我们无需为此编写自定义数据源集成。这是因为 Spring Cloud Connectors 会检测到我们正在使用 Heroku 和 PostgreSQL，并自动连接 Heroku 数据源。

7. 结束语

    现在，我们已经在 Heroku 中运行了 Spring Boot 应用程序。

    最重要的是，从一个想法到一个正在运行的应用程序的简单性使得 Heroku 成为一种可靠的部署方式。

    要了解有关 Heroku 及其提供的所有工具的更多信息，请访问 heroku.com。
