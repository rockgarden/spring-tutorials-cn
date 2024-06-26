# [自定义Keycloak登录页面](https://www.baeldung.com/keycloak-custom-login-page)

1. 概述

    Keycloak 是第三方授权服务器，用于管理网络或移动应用程序的身份验证和授权要求。它使用默认登录页面代表我们的应用程序登录用户。

    在本教程中，我们将重点介绍如何自定义 Keycloak 服务器的登录页面，以便拥有不同的外观和感觉。无论是独立服务器还是嵌入式服务器，我们都将看到这一点。

    我们将在[自定义Keycloak主题教程](https://www.baeldung.com/spring-keycloak-custom-themes)的基础上实现这一点。

2. 自定义单机版 Keycloak 服务器

    继续以[自定义](https://www.baeldung.com/spring-keycloak-custom-themes#default-themes)主题为例，让我们先看看独立服务器。

    1. 管理控制台设置

        要启动服务器，让我们导航到 Keycloak 发行版所在的目录，然后在 bin 文件夹中运行此命令：

        `kc.[sh|bat] start-dev --spi-theme-static-max-age=-1 --spi-theme-cache-themes=false --spi-theme-cache-templates=false`

        服务器启动后，我们只需刷新页面，就能看到我们的更改，这要归功于上述命令。

        现在，让我们在 themes/custom 目录中新建一个名为 login 的文件夹。为了简单起见，我们先将 themes/keycloak/login 目录中的所有内容复制到这里。这是默认的登录页面主题。

        然后，我们将进入[管理控制台](http://localhost:8080/admin/master/console)，键入 initial1/zaq1!QAZ 凭据，并转到我们的领域的主题选项卡。

        我们将选择自定义登录主题，然后保存更改。

        设置完成后，我们就可以尝试一些自定义设置了。但在此之前，我们先来看看默认[登录页面](http://localhost:8080/realms/SpringBootKeycloak/protocol/openid-connect/auth?response_type=code&client_id=login-app&scope=openid&redirect_uri=http://localhost:8081/)。

    2. 添加自定义

        现在，假设我们需要更改背景。为此，我们将打开 login/resources/css/login.css 并更改类定义：

        ```css
        .login-pf body {
            background: #39a5dc;
            background-size: cover;
            height: 100%;
        }
        ```

        为了看到效果，让我们刷新页面。

        接下来，让我们尝试更改用户名和密码的标签。

        为此，我们需要在 theme/login/messages 文件夹中创建一个新文件 messages_en.properties。该文件将覆盖用于给定属性的默认信息包：

        ```properties
        usernameOrEmail=Enter Username:
        password=Enter Password:
        ```

        要测试，请再次刷新页面。

        如果我们想更改整个 HTML 或其中的一部分，则需要覆盖 Keycloak 默认使用的自由标记模板。登录页面的默认模板保存在 base/login 目录中。

        比方说，我们想让 WELCOME TO BAELDUNG 取代 SPRINGBOOTKEYCLOAK。

        为此，我们需要将 base/login/template.ftl 复制到自定义/登录文件夹。

        在复制的文件中，修改以下一行

        ```ftl
        <div id="kc-header-wrapper" class="${properties.kcHeaderWrapperClass!}">
            ${kcSanitize(msg("loginTitleHtml",(realm.displayNameHtml!'')))?no_esc}
        </div>
        ```

        至：

        ```ftl
        <div id="kc-header-wrapper" class="${properties.kcHeaderWrapperClass!}">
            WELCOME TO BAELDUNG
        </div>
        ```

        登录页面现在将显示我们的自定义信息，而不是领域名称。

3. 定制嵌入式 Keycloak 服务器

    第一步是在嵌入式授权服务器的源代码中添加我们为独立服务器修改的所有工件。

    因此，让我们在 src/main/resources/themes/custom 中创建一个包含这些内容的新文件夹 login。

    现在我们需要做的就是在我们的领域定义文件 baeldung-realm.json 中添加指令，以便自定义登录主题类型：

    `"loginTheme": "custom",`

    我们已经[重定向到自定义主题目录](https://www.baeldung.com/spring-keycloak-custom-themes#redirection)，这样服务器就知道从哪里获取登录页面的主题文件。

    为了进行测试，让我们点击[登录页面](http://localhost:8083/auth/realms/baeldung/protocol/openid-connect/auth?response_type=code&&scope=openid%20write%20read&client_id=newClient&redirect_uri=http://localhost:8089/)。

    我们可以看到，之前为独立服务器所做的所有自定义，如背景、标签名称和页面标题，都出现在了这里。

4. 绕过 Keycloak 登录页面

    从技术上讲，我们可以通过使用[密码或直接访问授权](https://oauth.net/2/grant-types/password/)流程完全绕过 Keycloak 登录页面。不过，我们强烈建议不要使用这种授权类型。

    在这种情况下，没有获取授权码、然后接收访问令牌作为交换的中间步骤。相反，我们可以通过 REST API 调用直接发送用户凭据，并在响应中获得访问令牌。

    这实际上意味着，我们可以使用登录页面收集用户的用户名和密码，然后连同客户端用户名和密码，通过 POST 发送到 Keycloak 的令牌端点。

    但同样，由于 Keycloak 提供了丰富的登录选项功能，如记住我、密码重置和 MFA 等，因此没有什么理由绕过它。

5. 总结

    在本教程中，我们学习了如何更改 Keycloak 的默认登录页面并添加自定义功能。

    我们在独立实例和嵌入实例中都看到了这一点。

    最后，我们简要介绍了如何完全绕过 Keycloak 的登录页面，以及为什么不能这么做。
