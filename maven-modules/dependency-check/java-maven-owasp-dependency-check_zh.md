# [使用 OWASP Dependency-Check 检查组件漏洞](https://www.baeldung.com/java-maven-owasp-dependency-check)

安全

1. 概述  

    在应用程序开发过程中，我们通常需要向项目中添加一些第三方库或框架。这些库能够简化我们的开发工作。然而，它们可能因其自身存在的漏洞而带来潜在的安全风险。

    在本教程中，我们将介绍一个插件，它可以帮助我们识别应用程序中已知的漏洞。

2. Dependency-Check  

    我们将使用的插件是 OWASP Dependency-Check。该插件是一个软件组件分析（SCA）工具，旨在检测项目依赖项中包含的已公开漏洞。它通过将依赖项与通用平台枚举（[CPE](https://nvd.nist.gov/products/cpe)）标识符和通用漏洞与披露（CVE）条目进行关联，来识别具有已知漏洞的应用程序依赖项。

    CPE 是一种用于软件或包的结构化命名方案，而 CVE 则为公开已知的漏洞和暴露提供了一个参考标准。

    该插件通过 NIST 提供的国家漏洞数据库（[NVD](https://nvd.nist.gov/)）数据源自动更新这些条目。除了 Maven 插件外，它还提供了其他集成插件，例如 Gradle。

    11.0.0 版本开始的重大变更：
    - **Java 11 现在是运行 Dependency-Check 11.0.0 或更高版本的必需条件**。
    - **H2 数据库升级**：11.0.0 版本中包含使用本地 H2 数据库的重大变更。将进行完整的 NVD 数据下载。请注意，如果您使用共享数据目录，H2 数据库文件与旧版本的 Dependency-Check 不兼容。如果遇到问题，可能需要运行 purge 命令：
    - Gradle: `./gradlew dependencyCheckPurge`
    - Maven: `mvn org.owasp:dependency-check-maven:9.0.0:purge`
    - CLI: `dependency-check.sh --purge`
    - 最低 Java 版本：Java 11

    OWASP Dependency-Check 需要访问几个外部托管资源。

    为了分析某些技术栈，Dependency-Check 可能需要安装其他开发工具。以下列出的一些分析可能是实验性的，并需要启用实验性分析器。

    - 要分析 .NET 程序集，必须安装 dotnet 8 运行时或 SDK。
    - 可以分析针对其他运行时的程序集——但分析时必须运行 8 版本。
    - 如果分析 GoLang 项目，必须安装 go。
    - 分析 Elixir 项目需要 mix_audit。
    - 分析 npm、pnpm 和 yarn 项目需要安装 npm、pnpm 或 yarn。
    - 分析使用各自审计功能进行分析。
    - Ruby 的分析是一个对 bundle-audit 的包装，必须安装。

    有关使用 Jenkins 插件的说明，请参见 [OWASP Dependency-Check Plugin 页面](https://plugins.jenkins.io/dependency-check-jenkins-plugin/)。

    下载最新版本：

    ```bash
    VERSION=$(curl -s https://dependency-check.github.io/DependencyCheck/current.txt)
    curl -Ls "https://github.com/dependency-check/DependencyCheck/releases/download/v$VERSION/dependency-check-$VERSION-release.zip" --output dependency-check.zip
    ```

    在 *nix 系统上：

    ```bash
    ./bin/dependency-check.sh -h
    ./bin/dependency-check.sh --out . --scan [jar 文件路径]
    ```

    在 Windows 上：

    ```bash
    > .\bin\dependency-check.bat -h
    > .\bin\dependency-check.bat --out . --scan [jar 文件路径]
    ```

    在 Mac 上使用 Homebrew（注意：从 5.x 升级到 6.0.0 的 Homebrew 用户需要运行 `dependency-check.sh --purge`）：

    ```bash
    brew update && brew install dependency-check
    dependency-check -h
    dependency-check --out . --scan [jar 文件路径]
    ```

3. Maven 配置  

    在本教程中，我们将重点介绍如何将该插件集成到 Maven 项目中。首先，我们需要在 pom.xml 文件的 plugins 部分中添加 [Dependency-Check](https://mvnrepository.com/artifact/org.owasp/dependency-check-maven) 插件：

    更详细的说明可以在 [dependency-check-maven GitHub](https://dependency-check.github.io/DependencyCheck/dependency-check-maven) 页面上找到。

    ```xml
    <plugin>
        <!-- 用户手册 https://jeremylong.github.io/DependencyCheck/ -->
        <groupId>org.owasp</groupId>
        <artifactId>dependency-check-maven</artifactId>
        <version>12.1.3</version>
        <configuration>
            <failBuildOnCVSS>7</failBuildOnCVSS>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```

    一旦我们添加了这个插件，可以通过调用 verify 阶段来运行它，因为插件默认已经集成到该阶段中：

    ```bash
    mvn verify
    ```

    或者，我们也可以直接调用：

    ```bash
    mvn org.owasp:dependency-check-maven:check
    ```

    如果我们的应用程序中存在任何漏洞，Maven 会在控制台输出提示信息，告知我们哪些包存在漏洞。例如：

    ```log
    [WARNING] 

    One or more dependencies were identified with known vulnerabilities in dependency-check:

    logback-core-1.5.6.jar (pkg:maven/ch.qos.logback/logback-core@1.5.6, cpe:2.3:a:qos:logback:1.5.6:*:*:*:*:*:*:*) : CVE-2024-12798, CVE-2024-12801
    ```

    此外，插件还会生成一个 HTML 报告，其中包含它发现的漏洞详细信息。报告文件名为 `dependency-check-report.html`，可以在构建目录下找到。

4. CVSS 分数  

    让我们打开报告，深入了解某个组件漏洞的细节。我们可以看到，每个漏洞都关联了一个通用漏洞评分系统（[CVSS](https://nvd.nist.gov/vuln-metrics/cvss)）分数。

    CVSS 是衡量漏洞严重程度的标准。分数范围从 0 到 10，分数越高表示漏洞越严重。

    Maven 插件提供了一个名为 `failBuildOnCVSS` 的选项，可以配置为当任何组件的 CVSS 分数超过设定的阈值时构建失败。在我们的示例中，我们使用了 7，因此当前依赖项不会导致构建失败。CVSS 分数大于或等于 7 通常被认为是高严重性漏洞。

    在之前的报告中，我们看到的最高 CVSS 分数是 5.9。现在，我们将 `failBuildOnCVSS` 设置为 5.0 并再次运行插件。这次 Maven 会阻止构建成功：

    ```log
    [ERROR] Failed to execute goal org.owasp:dependency-check-maven:11.1.1:check (default-cli) on project dependency-check:
    [ERROR]
    [ERROR] One or more dependencies were identified with vulnerabilities that have a CVSS score greater than or equal to '5.0': 
    [ERROR]
    [ERROR] logback-core-1.5.6.jar: CVE-2024-12798(5.900000095367432)
    [ERROR]
    [ERROR] See the dependency-check report for more details.
    ```

    这对我们来说非常重要，因为当 CVSS 分数超过指定阈值时，构建会失败。这样，我们永远不会部署包含高严重性漏洞依赖项的应用程序。

5. Docker

    在以下示例中，假设要检查的源位于当前工作目录中，报告将写入 `$(pwd)/odc-reports`。使用持久化数据和缓存目录，允许您在运行后销毁容器。

    对于 Linux：

    ```bash
    #!/bin/sh
    DC_VERSION="latest"
    DC_DIRECTORY=$HOME/OWASP-Dependency-Check
    DC_PROJECT="dependency-check scan: $(pwd)"
    DATA_DIRECTORY="$DC_DIRECTORY/data"
    CACHE_DIRECTORY="$DC_DIRECTORY/data/cache"
    if [ ! -d "$DATA_DIRECTORY" ]; then
        echo "Initially creating persistent directory: $DATA_DIRECTORY"
        mkdir -p "$DATA_DIRECTORY"
    fi
    if [ ! -d "$CACHE_DIRECTORY" ]; then
        echo "Initially creating persistent directory: $CACHE_DIRECTORY"
        mkdir -p "$CACHE_DIRECTORY"
    fi
    # Make sure we are using the latest version
    docker pull owasp/dependency-check:$DC_VERSION
    docker run --rm \
        -e user=$USER \
        -u $(id -u ${USER}):$(id -g ${USER}) \
        --volume $(pwd):/src:z \
        --volume "$DATA_DIRECTORY":/usr/share/dependency-check/data:z \
        --volume $(pwd)/odc-reports:/report:z \
        owasp/dependency-check:$DC_VERSION \
        --scan /src \
        --format "ALL" \
        --project "$DC_PROJECT" \
        --out /report
        # Use suppression like this: (where /src == $pwd)
        # --suppression "/src/security/dependency-check-suppression.xml"
    ```

    对于 Windows：

    ```bash
    @echo off
    set DC_VERSION="latest"
    set DC_DIRECTORY=%USERPROFILE%\OWASP-Dependency-Check
    SET DC_PROJECT="dependency-check scan: %CD%"
    set DATA_DIRECTORY="%DC_DIRECTORY%\data"
    set CACHE_DIRECTORY="%DC_DIRECTORY%\data\cache"
    IF NOT EXIST %DATA_DIRECTORY% (
        echo Initially creating persistent directory: %DATA_DIRECTORY%
        mkdir %DATA_DIRECTORY%
    )
    IF NOT EXIST %CACHE_DIRECTORY% (
        echo Initially creating persistent directory: %CACHE_DIRECTORY%
        mkdir %CACHE_DIRECTORY%
    )
    rem Make sure we are using the latest version
    docker pull owasp/dependency-check:%DC_VERSION%
    docker run --rm ^
        --volume %CD%:/src ^
        --volume %DATA_DIRECTORY%:/usr/share/dependency-check/data ^
        --volume %CD%/odc-reports:/report ^
        owasp/dependency-check:%DC_VERSION% ^
        --scan /src ^
        --format "ALL" ^
        --project "%DC_PROJECT%" ^
        --out /report
        rem Use suppression like this: (where /src == %CD%)
        rem --suppression "/src/security/dependency-check-suppression.xml"
    ```

6. 总结  

    引入第三方库可以加快应用程序的开发速度，但也可能引入安全漏洞。OWASP Dependency-Check 插件可以根据 CPE 和 CVE 数据帮助我们识别存在漏洞的依赖项。

    我们可以将其集成到 Maven 中，自动检测存在漏洞的组件，并在存在关键 CVSS 分数的依赖项时使构建失败。

## NVD API 密钥

详情请见：<https://github.com/jeremylong/DependencyCheck?tab=readme-ov-file#nvd-api-key-highly-recommended>

从 2022 年开始，**NIST 的 NVD（美国国家标准与技术研究院的国家漏洞数据库）** 对其漏洞数据接口（数据源）进行了访问限制：

- **不再允许匿名访问或频繁访问**；
- 每个用户必须注册并申请一个 **免费的 API Key**；
- 否则会受到访问频率限制甚至被拒绝访问。
- 没有 NVD API 密钥，Dependency-Check 的更新将非常缓慢。
- NVD API 已实施速率限制
  - 如果您使用单个 API 密钥并进行多次构建，可能会达到速率限制并收到 403 错误。在 CI 环境中，必须使用缓存策略。

如果你不提供 API Key，Dependency-Check 插件在运行时将无法下载最新的漏洞数据，从而无法正确执行漏洞扫描。

**获取 NVD API Key：**

- 前往 [NVD 官网](https://nvd.nist.gov/developers/request-an-api-key) 注册账户并申请 API Key。

**配置 Dependency-Check 使用该 Key：**

可以通过命令行参数或环境变量设置：

`mvn dependency-check:check -Ddependencycheck.nvd.api.key=your_api_key_here`

或设置环境变量：

`export OWASP_DEPENDENCY_CHECK_NVD_API_KEY=your_api_key_here`

> 升级到 10.0.2 或更高版本是强制性的。旧版本的 Dependency-Check 导致了大量重复请求，最终导致处理失败，并对 NVD API 造成了不必要的负载。Dependency-Check 10.0.2 使用更新的 User-Agent 标头，允许 NVD 阻止来自旧客户端的调用。

## [Command Line Arguments](https://jeremylong.github.io/DependencyCheck/dependency-check-cli/arguments.html)

## [配置](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/configuration.html?spm=a2ty_o01.29997173.0.0.3611c921JmReEL)

1. failOnError

    该参数用于控制当 Dependency-Check 执行过程中发生错误（例如网络问题、数据源不可用等）时，是否让构建失败：

    - failOnError=true：构建失败
    - failOnError=false：仅输出警告信息，构建继续

    命令：`mvn dependency-check:check -Ddependencycheck.failOnError=false`

## 全局 NVD 缓存

在 OWASP Dependency-Check Maven 插件版本 12.1.3 中，全局 NVD 缓存 （Global NVD Cache）是默认启用的，用于缓存从 NIST NVD 下载的漏洞数据，以提高后续扫描的速度和效率。

不过，你可以通过配置来显式启用、控制或共享 NVD 缓存目录 ，特别是在 CI/CD 环境或多个项目之间复用缓存，从而避免重复下载数据。

1. **默认缓存目录**

    在 12.1.3 版本中，Dependency-Check 默认会在以下路径创建本地缓存目录：

    - **Linux/macOS**：`~/.dependency-cache/`

    你可以在这个目录下看到 NVD 的 JSON 数据缓存。

2. **自定义缓存目录（推荐用于 CI/CD）**

    你可以通过设置系统属性 `data.directory` 来指定全局 NVD 缓存目录：

    方法一：Maven 命令行设置

    ```bash
    mvn dependency-check:check -Ddependencycheck.data.directory=/path/to/shared/cache
    ```

    方法二：插件配置中设置（`pom.xml`）

    ```xml
    <plugin>
        <groupId>org.owasp</groupId>
        <artifactId>dependency-check-maven</artifactId>
        <version>12.1.3</version>
        <configuration>
            <dataDirectory>/path/to/shared/cache</dataDirectory>
        </configuration>
    </plugin>
    ```

    > 这样可以指定一个共享的缓存路径，多个项目或构建节点可以复用这个缓存，减少网络请求和扫描时间。

3. **启用/禁用缓存行为（通过参数）**

    虽然缓存默认是启用的，但你可以通过以下方式控制它的行为：

    强制更新缓存（忽略本地数据）

    ```bash
    mvn dependency-check:check -Ddependencycheck.update=true
    ```

    禁用缓存（强制从网络下载）

    ```bash
    mvn dependency-check:check -Ddependencycheck.disable.cache=true
    ```

4. **使用本地 NVD 镜像**

    如果你在内网或无法访问 NVD，可以使用本地 NVD 镜像：

    ```bash
    mvn dependency-check:check -Ddependencycheck.nvd.url.base=file:///path/to/nvd.mirror/
    ```

    这样可以完全使用本地数据源，不依赖网络。
