# [为Keycloak定制主题](https://www.baeldung.com/spring-keycloak-custom-themes)

1. 概述

    Keycloak 是一个开源身份和访问管理（IAM）解决方案，可用作第三方授权服务器来管理网络或移动应用程序的身份验证和授权要求。

    在本教程中，我们将重点介绍如何自定义 Keycloak 服务器的主题，以便为面向最终用户的网页提供不同的外观和感觉。

    首先，我们将从独立 Keycloak 服务器的角度建立一个背景。

2. Keycloak 中的主题

    1. 默认主题

        Keycloak 中预置了几个主题，并与发行版捆绑在一起。

        对于独立服务器，这些主题可以在 ./lib/lib/main/org.keycloak.keycloak-themes-20.0.3.jar 的主题目录下的不同文件夹中找到。

        标准 ZIP 压缩包工具打开：

        - base：包含 HTML 模板和信息包的骨架主题；所有主题，包括自定义主题，通常都继承自它
        - keycloak：包含用于美化页面的图片和样式表；如果我们不提供自定义主题，则默认使用该主题
        - keycloak.v2：基于反应的主题；新版管理控制台的一部分；旧版控制台已过时，将在 Keycloak 21 中移除。

        不建议修改现有主题。相反，我们应该创建一个新主题，扩展上述两个主题中的一个。

        要创建一个新的自定义主题，我们需要在主题目录中添加一个新文件夹，姑且称之为自定义文件夹。如果我们想彻底修改，从基础文件夹中复制内容是最好的方法。

        在我们的演示中，我们并不打算替换所有内容，因此从 keycloak 目录中获取内容是最实际的做法。

        我们将在下一节看到，自定义只需要我们要覆盖的主题类型的内容，而不需要整个 keycloak 文件夹。

    2. 主题类型

        Keycloak 支持五种类型的主题：

        - Welcome：用于登陆页面
        - Login：用于登录、OTP、授权、注册和忘记密码页面
        - Account：用于用户账户管理页面
        - Admin Console：用于管理控制台
        - Email：用于服务器发送的电子邮件

        上述列表中的最后四个主题可以通过独立服务器的管理控制台进行设置。在主题目录下创建新文件夹后，服务器重启后就可以选择该文件夹。

        让我们使用 initial1/zaq1!QAZ（在上一教程中设置）登录管理控制台，并转到我们的领域的主题选项卡。

        值得注意的是，主题是按领域设置的，因此我们可以为不同的领域设置不同的主题。这里我们为 SpringBootKeycloak 领域设置用户账户管理的自定义主题。

    3. 主题类型的结构

        除了默认主题部分概述的 HTML 模板、消息捆绑包、图片和样式表外，Keycloak 中的主题还包括其他一些元素--主题属性和脚本。

        每种主题类型都包含一个 theme.properties 文件。举例来说，让我们看看account类型的这个文件：

        ```properties
        parent=base
        import=common/keycloak

        styles=css/account.css
        stylesCommon=node_modules/patternfly/dist/css/patternfly.min.css node_modules/patternfly/dist/css/patternfly-additions.min.css
        ```

        正如我们所看到的，该主题从基本主题扩展而来，获得了所有 HTML 和信息捆绑包，还导入了通用主题以包含其中的一些样式。除此之外，它还定义了自己的样式 css/account.css。

        脚本是一个可选功能。如果我们需要为特定主题类型的模板包含量身定制的 JavaScript 文件，我们可以创建一个 resources/js 目录，并将它们保存在其中。接下来，我们需要在 theme.properties 中包含这些文件：

        `scripts=js/script1.js js/script2.js`

    4. 添加自定义

        现在开始有趣的部分！

        让我们以账户管理页面为例，看看如何更改其外观。准确地说，我们要更改页面上出现的徽标。

        在进行所有更改之前，下面是原始模板，可从 <http://localhost:8080/realms/master/account/#/personal-info> 获取：

        ![之前的 Keycloak 账户](/pic/keycloak-account-before.jpg)

        让我们试着把徽标改成自己的。为此，我们需要在 themes/custom 目录中添加一个新文件夹 account。我们可以从 theme/keycloak.v2 目录中复制该文件夹，这样就能获得所有需要的元素。

        现在，我们只需将新的徽标文件（如 baeldung.png 文件）添加到自定义/账户目录下的 resources/public 文件夹中，并修改账户文件夹中的 theme.properties 文件即可：

        ```properties
        # This is the logo in upper lefthand corner.
        # It must be a relative path.
        logo=/public/baeldung.png
        ```

        重要的是，在开发阶段，我们希望立即看到更改的效果，而无需重启服务器。要做到这一点，我们需要使用以下选项运行 Keycloak：

        `bin/kc.[sh|bat] start --spi-theme-static-max-age=-1 --spi-theme-cache-themes=false --spi-theme-cache-templates=false`

        与我们在此自定义账户主题的方法类似，要更改其他主题类型的外观和感觉，我们需要添加名为 admin、email 或 login 的新文件夹，并遵循相同的流程。

    5. 自定义欢迎页面

        要自定义欢迎页面，首先要在 themes/custom 下创建一个文件夹 welcome。同样，为了谨慎起见，应将 index.ftl 和 theme.properties 以及现有资源从 theme/keycloak/welcome 目录中复制出来。

        其次，我们必须使用以下选项运行 Keycloak：

        `bin/kc.[sh|bat] start-dev --spi-theme-welcome-theme=custom`

        现在，让我们尝试更改该页面的背景。

        要更改背景图片，请在 themes/custom/welcome/resources 中保留新图片，例如 geo.png，然后编辑 resources/css/welcome.css：

        ```css
        body {
            background: #fff url(../geo.png);
            background-size: cover;
        }
        ```

3. 总结

    在本教程中，我们了解了 Keycloak 中的主题--它们的类型和结构。

    然后，我们学习了几个预置主题，以及如何扩展它们，在独立实例中创建自己的自定义主题。
