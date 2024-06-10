
# [Spring Cloud - 添加 Angular 4](https://www.baeldung.com/spring-cloud-angular)

1. 概述

    在上一篇 Spring Cloud 文章中，我们为应用程序添加了 [Zipkin](https://www.baeldung.com/tracing-services-with-zipkin) 支持。在本文中，我们将在堆栈中添加一个前端应用程序。

    到目前为止，我们一直完全在后端构建我们的云应用程序。但是，如果没有用户界面，网络应用程序又有什么用呢？在本文中，我们将通过在项目中集成一个单页面应用程序来解决这个问题。

    我们将使用 Angular 和 Bootstrap 编写此应用程序。Angular 4 的代码风格感觉很像编写 Spring 应用程序，这对于 Spring 开发人员来说是一个自然的交叉！虽然前端代码将使用 Angular，但本文的内容可以轻松地扩展到任何前端框架，只需极少的工作量。

    在本文中，我们将构建一个 Angular 4 应用程序，并将其连接到我们的云服务。我们将演示如何在 SPA 和 Spring Security 之间集成登录。我们还将演示如何使用 Angular 对 HTTP 通信的支持来访问应用程序的数据。

2. 网关更改

    在前端就位后，我们将切换到基于表单的登录方式，并确保用户界面的部分内容对特权用户的安全。这就需要更改网关安全配置。

    1. 更新 HttpSecurity

        首先，更新网关 SecurityConfig.java 类中的 configure(HttpSecurity http) 方法：

        ```java
        @Bean
        public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
            http.formLogin()
                .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/home/index.html"))
                .and().authorizeExchange()
                .pathMatchers("/book-service/**", "/rating-service/**", "/login*", "/").permitAll()
                .pathMatchers("/eureka/**").hasRole("ADMIN")
                .anyExchange().authenticated()
                .and().logout().and().csrf().disable()
                .httpBasic(withDefaults());
            return http.build();
        }
        ```

        首先，我们添加一个默认的成功 URL，指向 /home/index.html，因为这将是我们的 Angular 应用程序所在的位置。接下来，我们配置 ant 匹配器，以允许除 Eureka 资源外的任何请求通过网关。这将把所有安全检查委托给后端服务。接下来，我们删除注销成功 URL，因为默认重定向回到登录页面就可以了。

    2. 添加 Principal 端点

        接下来，我们添加一个端点来返回通过验证的用户。这将在我们的 Angular 应用程序中用于登录和识别用户的角色。这将有助于我们控制用户在网站上的操作。

        在网关项目中，添加 AuthenticationController 类：

        main/.spring.cloud.bootstrap.gateway/AuthenticationController.java

        该控制器将当前登录的用户对象返回给调用者。这就为我们提供了控制 Angular 应用程序所需的全部信息。

    3. 添加登陆页面

        让我们添加一个非常简单的登陆页面，这样用户在进入应用程序根目录时就能看到一些内容。

        在 src/main/resources/static 中添加一个 [index.html](/spring-cloud-bootstrap/gateway/src/main/resources/static/index.html) 文件，其中包含登录页面的链接。

3. Angular CLI 和启动项目

    在开始新的 Angular 项目之前，请确保安装了最新版本的 [Node.js 和 npm](https://nodejs.org/en/download/)。

    1. 安装 Angular CLI

        首先，我们需要使用 npm 下载并安装 Angular 命令行界面。打开终端并运行

        `npm install -g @angular/cli`

        这将在全局下载并安装 CLI。

    2. 安装新项目

        仍然在终端中，导航到 gateway 项目，然后进入 gateway/src/main 文件夹。创建一个名为 “angular” 的目录并导航至该目录。在此运行

        `ng new ui`

        请耐心等待；CLI 会建立一个全新的项目，并使用 npm 下载所有 JavaScript 依赖项。这个过程通常需要几分钟。

        ng 命令是 Angular CLI 的快捷方式，new 参数指示 CLI 创建一个新项目，ui 命令为我们的项目命名。

    3. 运行项目

        完成新命令后。导航到创建的 ui 文件夹并运行：

        `ng serve`

        项目构建完成后，导航至 <http://localhost:4200> 。

        恭喜您，我们刚刚构建了一个 Angular 应用程序！

    4. 安装 Bootstrap

        让我们使用 npm 来安装 Bootstrap。在 ui 目录下运行以下命令

        `npm install bootstrap@4.0.0-alpha.6 --save`

        这将把 bootstrap 下载到 node_modules 文件夹中。

        在 ui 目录中，打开 .angular-cli.json 文件。该文件配置了项目的一些属性。找到 apps > styles 属性，添加 Bootstrap CSS 类的文件位置：

        ```txt
        "styles": [
            "styles.css",
            "../node_modules/bootstrap/dist/css/bootstrap.min.css"
        ],
        ```

        这将指示 Angular 将 Bootstrap 包含在与项目一起构建的编译 CSS 文件中。

    5. 设置构建输出目录

        接下来，我们需要告诉 Angular 将构建文件放在哪里，以便 Spring Boot 应用程序可以提供这些文件。Spring Boot 可以从资源文件夹中的两个位置提供文件：

        - src/main/resources/static
        - src/main/resource/public

        由于我们已经使用 static 文件夹为 Eureka 提供了一些资源，而 Angular 会在每次运行构建时删除该文件夹，因此让我们将 Angular 应用程序构建到 public 文件夹中。

        再次打开 .angular-cli.json 文件，找到 apps > outDir 属性。更新该字符串：

        `"outDir": "../../resources/static/home",`

        如果 Angular 项目位于 src/main/angular/ui，那么它将构建到 src/main/resources/public 文件夹。如果应用程序位于其他文件夹中，则需要修改此字符串以正确设置位置。

    6. 使用 Maven 自动构建

        最后，我们将设置自动构建，以便在编译代码时运行。只要运行 “mvn compile”，ant 任务就会运行 Angular CLI 构建任务。将此步骤添加到网关的 POM.xml 中，以确保每次编译时都能获得最新的 UI 更改：

        ```xml
        <plugin>
            <artifactId>maven-antrun-plugin</artifactId>
            <executions>
                <execution>
                    <phase>generate-resources</phase>
                    <configuration>
                        <tasks>
                            <exec executable="cmd" osfamily="windows"
                            dir="${project.basedir}/src/main/angular/ui">
                                <arg value="/c"/>
                                <arg value="ng"/>
                                <arg value="build"/>
                            </exec>
                            <exec executable="/bin/sh" osfamily="mac"
                            dir="${project.basedir}/src/main/angular/ui">
                                <arg value="-c"/>
                                <arg value="ng build"/>
                            </exec>
                        </tasks>
                    </configuration>
                    <goals>
                        <goal>run</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ```

        我们需要注意的是，这种设置要求类路径中必须有 Angular CLI。如果将此脚本推送到没有该依赖关系的环境，则会导致编译失败。

        现在，让我们开始构建 Angular 应用程序！

4. Angular

    在本节教程中，我们将在页面中构建一个身份验证机制。我们使用基本身份验证，并按照简单的流程使其运行。用户有一个登录表单，可以在其中输入用户名和密码。

    接下来，我们使用他们的凭据创建一个 base64 身份验证令牌，并请求“/me”端点。端点会返回一个 Principal 对象，其中包含该用户的角色。最后，我们将在客户端存储凭据和 principal，以便在后续请求中使用。

    让我们来看看如何做到这一点！

    1. 模板

        在网关项目中，导航到 src/main/angular/ui/src/app，打开 [app.component.html](./gateway/src/main/angular/ui/src/app/app.component.html) 文件。这是 Angular 加载的第一个模板，也是用户登录后登陆的地方。在这里，我们要添加一些代码来显示带有登录表单的导航栏：

        ```html
        <nav class="navbar navbar-toggleable-md navbar-inverse fixed-top bg-inverse">
            <button class="navbar-toggler navbar-toggler-right" type="button" 
            data-toggle="collapse" data-target="#navbarCollapse" 
            aria-controls="navbarCollapse" aria-expanded="false" 
            aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
            </button>
            <a class="navbar-brand" href="#">Book Rater 
                <span *ngIf="principal.isAdmin()">Admin</span></a>
            <div class="collapse navbar-collapse" id="navbarCollapse">
            <ul class="navbar-nav mr-auto">
            </ul>
            <button *ngIf="principal.authenticated" type="button" 
            class="btn btn-link" (click)="onLogout()">Logout</button>
            </div>
        </nav>

        <div class="jumbotron">
            <div class="container">
                <h1>Book Rater App</h1>
                <p *ngIf="!principal.authenticated" class="lead">
                Anyone can view the books.
                </p>
                <p *ngIf="principal.authenticated && !principal.isAdmin()" class="lead">
                Users can view and create ratings</p>
                <p *ngIf="principal.isAdmin()"  class="lead">Admins can do anything!</p>
            </div>
        </div>
        ```

        这段代码使用 Bootstrap 类设置了一个导航栏。导航栏中嵌入了一个内联登录表单。Angular 使用此标记与 JavaScript 进行动态交互，以呈现页面的各个部分并控制表单提交等操作。

        (ngSubmit)=“onLogin(f)” 这样的语句只是表示当表单提交时，调用方法 “onLogin(f)” 并将表单传递给该函数。在 jumbotron div 中，我们将根据 principal 对象的状态动态显示段落标签。接下来，让我们编写支持该模板的 Typescript 文件。

    2. Typescript

        在同一目录下打开 [app.component.ts](./gateway/src/main/angular/ui/src/app/app.component.ts) 文件。在该文件中，我们将添加所有需要的 typescript 属性和方法，以使我们的模板发挥作用。

        该类挂钩 Angular 生命周期方法 ngOnInit()。在该方法中，我们调用 /me 端点来获取用户的当前角色和状态。这决定了用户在主页面上看到的内容。只要创建了这个组件，这个方法就会被触发，这也是我们在应用程序中检查用户权限属性的好时机。

        我们还有一个 onLogout() 方法，用于注销用户并将页面状态恢复为原始设置。

        不过，这里还有些神奇之处。在构造函数中声明的 httpService 属性。Angular 会在运行时将该属性注入我们的类。Angular 管理服务类的单例，并使用构造函数注入的方式将其注入，就像 Spring 一样！

        接下来，我们需要定义 HttpService 类。

    3. HttpService

        在同一目录下创建一个名为 “[http.service.ts](./gateway/src/main/angular/ui/src/app/http.service.ts)” 的文件。在该文件中添加代码，以支持登录和注销方法。

        在这个类中，我们使用 Angular 的 DI 结构注入另一个依赖关系。这次是 Http 类。该类处理所有 HTTP 通信，由框架提供给我们。

        这些方法分别使用 angular 的 HTTP 库执行 HTTP 请求。每个请求还会在头部指定一个内容类型。

        现在，我们还需要做一件事，让 HttpService 在依赖注入系统中注册。打开 app.module.ts 文件，找到 providers 属性。将 HttpService 添加到该数组中。结果应该是这样的

        `providers: [HttpService],`

    4. 添加 Principal

        接下来，让我们在 Typescript 代码中添加 Principal DTO 对象。在同一目录下添加一个名为 “[principal.ts](./gateway/src/main/angular/ui/src/app/principal.ts)” 的文件，并添加相应代码。

        我们添加了 Principal 类和 Authority 类。这是两个 DTO 类，很像 Spring 应用程序中的 POJO。因此，我们不需要在 angular 的 DI 系统中注册这些类。

        接下来，让我们配置一个重定向规则，将未知请求重定向到应用程序的根目录。

    5. 404 处理

        让我们回到网关服务的 Java 代码中。在 GatewayApplication 类所在的位置添加一个名为 [ErrorPageConfig.java](./gateway/src/main/java/com/baeldung/spring/cloud/bootstrap/gateway/ErrorPageConfig.java) 的新类。

        该类将识别任何 404 响应，并将用户重定向到“/home/index.html”。在单页面应用程序中，我们将通过这种方式处理所有不指向专用资源的流量，因为客户端应该处理所有可导航的路径。

        现在我们可以启动这个应用程序，看看我们创建了什么！

    6. 构建和查看

        现在从 gateway 文件夹中运行 “mvn compile”。这将编译我们的 java 源代码，并在公共文件夹中构建 Angular 应用程序。让我们启动其他云应用程序：config、discovery 和 zipkin。然后运行网关项目。服务启动后，导航到 <http://localhost:8080> 查看我们的应用程序。

        接下来，让我们跟随链接进入登录页面。

        使用 user/password 凭据登录。点击 “Login”，我们将被重定向到 /home/index.html，在那里加载我们的单页应用程序。

        看起来，我们的广告牌显示我们已作为用户登录！现在点击右上角的链接退出登录，然后使用 admin/admin 凭证登录。

        看起来不错！现在我们以管理员身份登录了。

5. 总结

    在本文中，我们看到了将单页应用程序集成到云系统中是多么容易。我们采用了一个现代框架，并在应用程序中集成了一个有效的安全配置。

    使用这些示例，尝试编写一些代码来调用图书服务或评级服务。由于我们现在已经有了进行 HTTP 调用和将数据连接到模板的示例，这应该相对容易。
