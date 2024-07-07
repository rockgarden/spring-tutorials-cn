# Maven插件

## Maven资源插件

1. 概述

    本教程介绍资源插件，它是Maven构建工具的核心插件之一。

2. 插件目标

    资源插件将文件从输入资源目录复制到输出目录。该插件有三个目标，只在如何指定资源和输出目录方面有所不同。

    这个插件的三个目标是：

    - resources - 将属于主源代码的资源复制到主输出目录中
    - testResources - 将属于测试源代码的资源复制到测试输出目录中
    - copy-resources - 将任意的资源文件复制到输出目录，要求我们指定输入文件和输出目录

    让我们看一下pom.xml中的资源插件：

    ```xml
    <plugin>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.0.2</version>
        <configuration>
            ...
        </configuration>
    </plugin>
    ```

3. 例子

    假设我们想把资源文件从input-resources目录复制到output-resources目录，并且我们想排除所有以.png结尾的文件。

    这个配置可以满足这些要求：

    ```xml
    <configuration>
        <outputDirectory>output-resources</outputDirectory>
        <resources>
            <resource>
                <directory>input-resources</directory>
                <excludes>
                    <exclude>*.png</exclude>
                </excludes>
                <filtering>true</filtering>
            </resource>
        </resources>
    </configuration>
    ```

    该配置适用于资源插件的所有执行。

    例如，当使用命令`mvn resources:resources`执行该插件的resources目标时，input-resources目录下的所有资源（PNG文件除外）将被复制到output-resources。

    由于默认情况下，资源目标与Maven默认生命周期中的process-resources阶段绑定，我们可以通过运行命令`mvn process-resources`来执行该目标和前面的所有阶段。

    在给定的配置中，有一个名为过滤的参数，其值为true。过滤参数用于替换资源文件中的占位符变量。

    例如，如果我们在POM中有一个属性：

    ```xml
    <properties>
        <resources.name>Baeldung</resources.name>
    </properties>
    ```

    而其中一个资源文件包含

    `Welcome to ${resources.name}!`

    运行 `mvn process-resources` 则该变量将在输出资源中被评估，结果文件将包含：

    `Welcome to Baeldung!`

4. 总结

    在这篇快速的文章中，我们介绍了资源插件，并给出了使用和定制它的说明。

## Maven Enforcer 插件

1. 概述

    在本教程中，我们将了解[Maven Enforcer插件](https://maven.apache.org/enforcer/maven-enforcer-plugin/)，以及如何利用它来保证我们项目的合规性水平。

    当我们有分布在全球各地的团队时，该插件尤其方便。

2. 依赖性

    为了在我们的项目中使用该插件，我们需要在pom.xml中添加以下依赖项：

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>3.0.0-M2</version>
    </plugin>
    ```

    该插件的最新版本可在Maven Central找到。

3. 插件配置和目标

    Maven Enforcer有两个目标：enforcer:enforce和enforcer:display-info。

    enforce目标在项目构建过程中运行，执行配置中指定的规则，而display-info目标则显示项目pom.xml中的内置规则的当前信息。

    让我们在 executions 标签中定义 enforce 目标。此外，我们将添加容纳项目规则定义的配置标签：

    ```xml
    <executions>
        <execution>
            <id>enforce</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <banDuplicatePomDependencyVersions/>
                </rules>
            </configuration>
        </execution>
    </executions>
    ```

4. Maven执行器规则

    关键字enforce微妙地暗示了需要遵守的规则的存在。这就是Maven Enforcer插件的工作原理。我们用一些规则来配置它，这些规则将在项目的构建阶段被强制执行。

    在这一节中，我们将看看有哪些可用的规则，我们可以将这些规则应用于我们的项目，以提高其质量。

    1. 禁止重复的依赖性

        在一个多模块的项目中，POM之间存在着父子关系，确保一个项目的有效最终POM中没有重复的依赖关系是一项棘手的任务。但是，有了banDuplicatePomDependencyVersions规则，我们可以很容易地确保我们的项目没有这种故障。

        我们需要做的就是在插件配置的规则部分添加banDuplicatePomDependencyVersions标签：

        `<rules><banDuplicatePomDependencyVersions/></rules>`

        为了检查该规则的行为，我们可以在pom.xml中复制一个依赖，然后运行mvn clean compile。它将在控制台产生以下错误行：

        ```log
        [WARNING] Rule 0: org.apache.maven.plugins.enforcer.BanDuplicatePomDependencyVersions failed with message:
        Found 1 duplicate dependency declaration in this project:
        - dependencies.dependency[io.vavr:vavr:jar] ( 2 times )

        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD FAILURE
        [INFO] ------------------------------------------------------------------------
        [INFO] Total time:  1.370 s
        [INFO] Finished at: 2019-02-19T10:17:57+01:00
        ```

    2. 要求Maven和Java版本

        requireMavenVersion和requireJavaVersion规则分别实现了项目范围内对所需Maven和Java版本的锁定。这将有助于消除开发环境中使用不同版本的Maven和JDK可能产生的差异。

        让我们更新一下插件配置中的规则部分：

        ```xml
        <requireMavenVersion>
            <version>3.0</version>
        </requireMavenVersion>
        <requireJavaVersion>
            <version>1.8</version>
        </requireJavaVersion>
        ```

        这些规则允许我们以灵活的方式指定版本号，只要它们符合插件的[版本范围规范模式](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html)。

        此外，这两条规则还接受一个消息参数，用于指定一个自定义的消息：

        ```xml
        <requireMavenVersion>
            <version>3.0</version>
            <message>Invalid Maven version. It should, at least, be 3.0</message>
        </requireMavenVersion>
        ```

    3. 要求环境变量

        通过requireEnvironmentVariable规则，我们可以确保在执行环境中设置某个环境变量。

        它可以被重复使用，以适应一个以上的所需变量：

        ```xml
        <requireEnvironmentVariable>
            <variableName>ui</variableName>
        </requireEnvironmentVariable>
        <requireEnvironmentVariable>
            <variableName>cook</variableName>
        </requireEnvironmentVariable>
        ```

    4. 要求活动配置文件

        Maven的配置文件帮助我们配置应用程序部署到不同环境时的活动属性。

        因此，当我们需要确保一个或多个指定的配置文件处于活动状态时，我们可以使用requireActiveProfile规则，从而保证我们的应用程序成功执行：

        ```xml
        <requireActiveProfile>
            <profiles>local,base</profiles>
            <message>Missing active profiles</message>
        </requireActiveProfile>
        ```

        在上面的片段中，我们使用消息属性来提供一个自定义消息，以便在规则检查失败时显示。

    5. 其他规则

        Maven Enforcer插件还有许多[其他规则](https://maven.apache.org/enforcer/enforcer-rules/index.html)，以促进项目质量和一致性，而不考虑开发环境。

        此外，该插件还有一条命令，用于显示当前配置的一些规则的信息：

        `mvn enforcer:display-info`

5. 自定义规则

    到目前为止，我们一直在探索该插件的内置规则。现在，是时候看看如何创建我们自己的自定义规则了。

    首先，我们需要创建一个新的Java项目，包含我们的自定义规则。自定义规则是一个实现了EnforceRule接口的Object类，并重写了execute()方法：

    enforcer/MyCustomRule.java: execute()

    我们的自定义规则只是检查目标项目的groupId是否以com.baeldung开头。

    请注意，我们不需要返回布尔值或任何类似的东西来表明该规则未被满足。我们只是抛出一个EnforcerRuleException，并说明问题所在。

    我们可以把我们的自定义规则作为Maven Enforcer插件的一个依赖项来使用：

    `<rules><myCustomRule implementation="com.baeldung.enforcer.MyCustomRule"/></rules>`

    请注意，如果自定义规则项目不是Maven Central上发布的工件，我们可以通过运行mvn clean install将其安装到本地Maven repo。

    这将使其在编译拥有Maven Enforcer插件的目标项目时可用。请参阅该插件的[自定义规则文档](https://maven.apache.org/enforcer/enforcer-api/writing-a-custom-rule.html)以了解更多信息。

    为了看到它的实际效果，我们可以将带有Enforcer插件的项目的groupId属性设置为 "com.baeldung" 以外的其他属性，然后运行mvn clean compile。

6. 总结

    在这个快速教程中，我们看到了Maven Enforcer插件是如何成为我们现有插件库的一个有用的补充。编写自定义规则的能力增强了其应用范围。

## Relevant Articles

- [Guide to the Core Maven Plugins](https://www.baeldung.com/core-maven-plugins)
- [x] [Maven Resources Plugin](https://www.baeldung.com/maven-resources-plugin)
- [The Maven Verifier Plugin](https://www.baeldung.com/maven-verifier-plugin)
- [The Maven Clean Plugin](https://www.baeldung.com/maven-clean-plugin)
- [x] [Maven Enforcer Plugin](https://www.baeldung.com/maven-enforcer-plugin)

## Code

请注意，在[GitHub](https://github.com/eugenp/tutorials/tree/master/maven-modules/maven-plugins)上提供的完整示例源代码中，我们需要取消对自定义规则示例的依赖性和规则的注释。
