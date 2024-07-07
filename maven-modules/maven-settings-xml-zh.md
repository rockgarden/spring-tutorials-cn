# Maven中的settings.xml文件

1. 概述

    使用Maven时，我们将大部分项目的特定配置放在pom.xml中。

    Maven提供了一个设置文件settings.xml，它允许我们指定它将使用哪些本地和远程仓库。我们还可以用它来存储我们不希望在源代码中出现的设置，如凭证。

    在本教程中，我们将学习如何使用settings.xml。我们将看一下代理、镜像和配置文件。我们还将讨论如何确定适用于我们项目的当前设置。

    [Maven属性的默认值](https://www.baeldung.com/maven-properties-defaults)

    在这篇短文中，我们将了解如何配置Maven属性默认值，以及如何使用它们。

    [Maven中的附加源码目录](https://www.baeldung.com/maven-add-src-directories)

    了解如何在Maven中配置额外的源码目录。

    [Maven打包类型](https://www.baeldung.com/maven-packaging-types)

    在这篇文章中，我们将探讨Maven中的不同打包类型。

2. 配置

    settings.xml文件可以配置[Maven](https://www.baeldung.com/maven)的安装。它类似于pom.xml文件，但可以全局或按用户定义。

    ```xml
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
        <localRepository/>
        <interactiveMode/>
        <offline/>
        <pluginGroups/>
        <servers/>
        <mirrors/>
        <proxies/>
        <profiles/>
        <activeProfiles/>
    </settings>
    ```

    1. 简单的值

        一些顶层的配置元素包含简单的值。

        ```xml
        <settings>
            <localRepository>${user.home}/.m2/repository</localRepository>
            <interactiveMode>true</interactiveMode>
            <offline>false</offline>
        </settings>
        ```

        > `~/.m2/repository` 将在每项目根目录缓存artifact。

        localRepository 元素指向系统的本地版本库的路径。[本地存储库](https://www.baeldung.com/maven-local-repository)是我们项目的所有依赖关系被缓存的地方。默认是使用用户的主目录。然而，我们可以改变它，让所有登录的用户从一个共同的本地资源库构建。

        interactiveMode标志定义了我们是否允许Maven与用户互动，征求用户的意见。该标志的默认值为 "true"。

        离线标志决定了构建系统是否可以在离线模式下运行。默认为false；但在构建服务器无法连接到远程仓库的情况下，我们可以将其切换为true。

    2. 插件组

        pluginGroups元素包含一个指定groupId的子元素列表。groupId是创建特定Maven工件的组织的唯一标识符。

        ```xml
        <settings>
            <pluginGroups>
                <pluginGroup>org.apache.tomcat.maven</pluginGroup>
            </pluginGroups>
        </settings>
        ```

        当使用一个没有在命令行中提供groupId的插件时，Maven会搜索插件组的列表。该列表默认包含org.apache.maven.plugins和org.codehaus.mojo组。

        上面定义的settings.xml文件允许我们执行截短的Tomcat插件命令。

        mvn tomcat7:help
        mvn tomcat7:deploy
        mvn tomcat7:run

    3. 代理

        我们可以为Maven的部分或全部HTTP请求配置一个代理。代理元素允许一个子代理元素的列表，但每次只能有一个代理处于活动状态。

        ```xml
        <settings>
            <proxies>
                <proxy>
                    <id>baeldung-proxy</id>
                    <active>true</active>
                    <protocol>http</protocol>
                    <host>baeldung.proxy.com</host>
                    <port>8080</port>
                    <username>demo-user</username>
                    <password>dummy-password</password>
                    <nonProxyHosts>*.baeldung.com|*.apache.org</nonProxyHosts>
                </proxy>
            </proxies>
        </settings>
        ```

        我们通过active标志来定义当前活动的代理。然后通过nonProxyHosts元素，我们指定哪些主机不被代理。使用的分隔符取决于具体的代理服务器。最常见的定界符是管道和逗号。

    4. 镜像

        <https://maven.apache.org/guides/mini/guide-mirror-settings.html>

        Repositories可以在项目的pom.xml里面声明。这意味着共享项目代码的开发者可以得到正确的版本库设置。

        我们可以在以下情况下使用镜像，即我们想为某个部分定义另一个镜像。

        在我们想为某个特定的版本库定义一个替代镜像的情况下，我们可以使用镜像。这将覆盖pom.xml中的内容。

        例如，我们可以通过镜像所有版本库的请求来强制Maven使用一个版本库：

        ```xml
        <settings>
            <mirrors>
                <mirror>
                    <id>internal-baeldung-repository</id>
                    <name>Baeldung Internal Repo</name>
                    <url>https://baeldung.com/repo/maven2/</url>
                    <!-- 使用单一版本库 -->
                    <mirrorOf>*</mirrorOf>
                </mirror>
            </mirrors>
        </settings>
        ```

        通过镜像所有版本库请求，强制 Maven 使用单一版本库。该版本库必须包含所有需要的工件，或者能够将请求代理到其他版本库。在使用公司内部版本库和 Maven 版本库管理器代理外部请求时，该设置最为有用。

        通常情况下，我们应该使用通过CDN分发到世界各地的官方版本库。

        我们可以为一个给定的版本库只定义多个镜像，Maven会选择第一个匹配的。

        mirrorOf 指定镜像的规则，就是什么情况会从镜像仓库拉取，而不是从原本的仓库拉取。

        可选项参考链接：
        - `*` 匹配所有，配置的repository不起作用；
        - `external:*` 除了本地缓存之后的所有仓库；
        - `repo,repo1` repo 或者 repo1，这里repo指的是仓库的id；
        - `*,!repo1` 除了repo1的所有仓库

    5. 服务器

        在项目pom.xml中定义版本库是个好做法。然而，我们不应该把安全设置，如证书，放入我们的源代码库与pom.xml。相反，我们在settings.xml文件中定义这些安全信息。

        ```xml
        <settings>
            <servers>
                <server>
                    <id>internal-baeldung-repository</id>
                    <username>demo-user</username>
                    <password>dummy-password</password>
                    <privateKey>${user.home}/.ssh/bael_key</privateKey>
                    <passphrase>dummy-passphrase</passphrase>
                    <filePermissions>664</filePermissions>
                    <directoryPermissions>775</directoryPermissions>
                    <configuration></configuration>
                </server>
            </servers>
        </settings>
        ```

        我们应该注意，settings.xml中服务器的ID需要与pom.xml中提到的版本库的ID元素相匹配。XML还允许我们使用占位符来从环境变量中提取凭证。

3. 配置文件

    profiles元素使我们能够创建多个profile子元素，以其ID子元素来区分。settings.xml中的profile元素是pom.xml中相同元素的一个截断版本。

    它只能包含四个子元素：Activation、repositories、pluginRepositories 和 properties。这些元素将构建系统作为一个整体进行配置，而不是任何特定的项目。

    值得注意的是，settings.xml中的激活配置文件的值将覆盖pom.xml或profiles.xml文件中的任何相等的配置文件值。配置文件是通过ID来匹配的。

    1. 激活

        我们可以使用配置文件只在特定情况下修改某些值。我们可以使用激活元素来指定这些情况。因此，当所有指定的标准被满足时，配置文件的激活就会发生。

        ```xml
        <settings>
            <profiles>
                <profile>
                    <id>baeldung-test</id>
                    <activation>
                        <activeByDefault>false</activeByDefault>
                        <jdk>1.8</jdk>
                        <os>
                            <name>Windows 10</name>
                            <family>Windows</family>
                            <arch>amd64</arch>
                            <version>10.0</version>
                        </os>
                        <property>
                            <name>mavenVersion</name>
                            <value>3.0.7</value>
                        </property>
                        <file>
                            <exists>${basedir}/activation-file.properties</exists>
                            <missing>${basedir}/deactivation-file.properties</missing>
                        </file>
                    </activation>
                </profile>
            </profiles>
        </settings>
        ```

        有四种可能的激活器，并非所有的激活器都需要被指定：

        - jdk：根据指定的JDK版本激活（支持范围）。
        - os：根据操作系统属性激活
        - property：如果Maven检测到一个特定的属性值，则激活配置文件。
        - file：如果给定的文件名存在或丢失，则激活配置文件。
        为了检查哪个配置文件会激活某个构建，我们可以使用Maven帮助插件。

        `mvn help:active-profiles`

        输出结果将显示当前某个项目的活跃配置文件。

        ```log
        [INFO] --- maven-help-plugin:3.2.0:active-profiles (default-cli) @ core-java-streams-3 ---
        [INFO]
        Active Profiles for Project 'com.baeldung.core-java-modules:core-java-streams-3:jar:0.1.0-SNAPSHOT':
        The following profiles are active:
        - baeldung-test (source: com.baeldung.core-java-modules:core-java-streams-3:0.1.0-SNAPSHOT)
        ```

    2. 属性

        Maven属性可以被认为是某一数值的命名占位符。这些值可以在pom.xml文件中用${property_name}符号来访问。

        ```xml
        <settings>
            <profiles>
                <profile>
                    <id>baeldung-test</id>
                    <properties>
                        <user.project.folder>${user.home}/baeldung-tutorials</user.project.folder>
                    </properties>
                </profile>
            </profiles>
        </settings>
        ```

        在 pom.xml 文件中，有四种不同类型的属性可用。

        - 使用 env 前缀的属性会返回一个环境变量值，例如 ${env.PATH}。
        - 使用 project 前缀的属性会返回 pom.xml 的项目元素中设置的属性值，例如 ${project.version}。
        - 使用settings前缀的属性会返回settings.xml中相应元素的值，例如${settings.localRepository}。
        - 我们可以直接引用Java中通过System.getProperties方法获得的所有属性，例如${java.home}。
        - 我们可以使用不含前缀的属性元素中设置的属性，比如${junit.version}。

    3. 资源库

        远程仓库包含工件的集合，Maven用它来填充我们的本地仓库。对于特定的工件，可能需要不同的远程仓库。Maven会搜索活动配置文件下启用的资源库。

        ```xml
        <settings>
            <profiles>
                <profile>
                    <id>adobe-public</id>
                    <repositories>
                        <repository>
                            <id>adobe-public-releases</id>
                            <name>Adobe Public Repository</name>
                            <url>https://repo.adobe.com/nexus/content/groups/public</url>
                            <releases>
                                <enabled>true</enabled>
                                <updatePolicy>never</updatePolicy>
                            </releases>
                            <snapshots>
                                <enabled>false</enabled>
                            </snapshots>
                        </repository>
                    </repositories>
                </profile>
            </profiles>
        </settings>
        ```

        我们可以使用存储库元素，只启用特定存储库的工件的发布或快照版本。

    4. 插件存储库

        Maven有两种标准类型的工件(artifacts)，即依赖性(dependencies)和插件(plugins)。由于Maven插件是一种特殊类型的工件，我们可以将插件仓库与其他仓库分开。

        ```xml
        <settings>
            <profiles>
                <profile>
                    <id>adobe-public</id>
                    <pluginRepositories>
                    <pluginRepository>
                        <id>adobe-public-releases</id>
                        <name>Adobe Public Repository</name>
                        <url>https://repo.adobe.com/nexus/content/groups/public</url>
                        <releases>
                            <enabled>true</enabled>
                            <updatePolicy>never</updatePolicy>
                        </releases>
                        <snapshots>
                            <enabled>false</enabled>
                        </snapshots>
                        </pluginRepository>
                    </pluginRepositories>
                </profile>
            </profiles>
        </settings>
        ```

        值得注意的是，pluginRepositories元素的结构与repositories元素非常相似。

    5. 活动配置文件

        activeProfiles元素包含引用特定配置文件ID的子元素。Maven会自动激活这里引用的任何配置文件。

        ```xml
        <settings>
            <activeProfiles>
                <activeProfile>baeldung-test</activeProfile>
                <activeProfile>adobe-public</activeProfile>
            </activeProfiles>
        </settings>
        ```

        在这个例子中，每一次调用mvn都是在命令行中加入`-P baeldung-test,adobe-public`的情况下运行。

4. 设置级别

    settings.xml文件通常可以在几个地方找到。

    - Mavens主目录下的全局设置：${maven.home}/conf/settings.xml
    - 用户主目录下的用户设置：${user.home}/.m2/settings.xml
    如果两个文件都存在，它们的内容会被合并。来自用户设置的配置优先考虑。

    1. 确定文件位置

        为了确定全局和用户设置的位置，我们可以使用调试标志运行Maven，在输出中搜索 "settings"。

        ```shell
        $ mvn -X clean | grep "settings"
        [DEBUG] Reading global settings from C:\Program Files (x86)\Apache\apache-maven-3.6.3\bin\..\conf\settings.xml
        [DEBUG] Reading user settings from C:\Users\Baeldung\.m2\settings.xml
        ```

    2. 确定有效设置

        我们可以使用Maven的帮助插件来找出全局和用户设置的组合内容。

        `mvn help:effective-settings`

        这是以XML格式描述的设置。

        ```xml
        <settings>
            <localRepository>C:\Users\Baeldung\.m2\repository</localRepository>
            <pluginGroups>
                <pluginGroup>org.apache.tomcat.maven</pluginGroup>
                <pluginGroup>org.apache.maven.plugins</pluginGroup>
                <pluginGroup>org.codehaus.mojo</pluginGroup>
            </pluginGroups>
        </settings>
        ```

    3. 覆盖默认位置

        Maven还允许我们通过命令行覆盖全局和用户设置的位置。

        `$ mvn clean --settings c:\user\user-settings.xml --global-settings c:\user\global-settings.xml`

        我们也可以使用同一命令的较短的-s版本。

        `$ mvn clean --s c:\user\user-settings.xml --gs c:\user\global-settings.xml`

5. 总结

    在本文中，我们探讨了Maven的settings.xml文件中可用的配置。

    我们学习了如何配置代理、存储库和配置文件。接下来，我们研究了全局和用户设置文件的区别，以及如何确定哪些文件正在使用。

    最后，我们研究了如何确定所使用的有效设置，以及如何覆盖默认文件位置。

## Relevant Articles

- [x] [The settings.xml File in Maven](https://www.baeldung.com/maven-settings-xml)
