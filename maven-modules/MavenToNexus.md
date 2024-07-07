# Maven To Nexus

## Maven发布到Nexus

在这篇文章中，我们将用Maven配置发布流程--在项目的pom中以及Jenkins作业中都是如此。

1. pom中的存储库

    为了让Maven能够向Nexus版本库服务器发布，我们需要通过distributionManagement元素定义版本库信息。

    ```xml
    <distributionManagement>
        <repository>
            <id>nexus-releases</id>
            <url>http://localhost:8081/nexus/content/repositories/releases</url>
        </repository>
    </distributionManagement>
    ```

    在Nexus上，托管的Release Repository是开箱即用的，所以不需要明确创建它。

2. Maven pom中的SCM

    发布过程将与项目的源代码控制进行交互--这意味着我们首先需要在pom.xml中定义`<scm>`元素。

    ```xml
    <scm>
        <connection>scm:git:https://github.com/user/project.git</connection>
        <url>http://github.com/user/project</url>
        <developerConnection>scm:git:https://github.com/user/project.git</developerConnection>
    </scm>
    ```

    或者，使用git协议。

    ```xml
    <scm>
        <connection>scm:git:git@github.com:user/project.git</connection>
        <url>scm:git:git@github.com:user/project.git</url>
        <developerConnection>scm:git:git@github.com:user/project.git</developerConnection>
    </scm>
    ```

3. 发布插件

    发布流程使用的标准Maven插件是maven-release-plugin，该插件的配置很简单。

    ```xml
    <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-release-plugin</artifactId>
    <version>2.4.2</version>
    <configuration>
        <tagNameFormat>v@{project.version}</tagNameFormat>
        <autoVersionSubmodules>true</autoVersionSubmodules>
        <releaseProfiles>releases</releaseProfiles>
    </configuration>
    </plugin>
    ```

    这里重要的是，releaseProfiles配置实际上会在发布过程中强迫一个Maven配置文件--发布配置文件--变得活跃。

    在这个过程中，nexus-staging-maven-plugin被用来执行部署到nexus-releases Nexus仓库的工作。

    ```xml
    <profiles>
    <profile>
        <id>releases</id>
        <build>
            <plugins>
                <plugin>
                <groupId>org.sonatype.plugins</groupId>
                <artifactId>nexus-staging-maven-plugin</artifactId>
                <version>1.5.1</version>
                <executions>
                    <execution>
                        <id>default-deploy</id>
                        <phase>deploy</phase>
                        <goals>
                            <goal>deploy</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <serverId>nexus-releases</serverId>
                    <nexusUrl>http://localhost:8081/nexus/</nexusUrl>
                    <skipStaging>true</skipStaging>
                </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    </profiles>
    ```

    该插件被配置为在没有暂存机制的情况下执行发布过程，与之前的部署过程一样（skipStaging=true）。

    而且与部署过程类似，释放到Nexus是一个安全的操作--所以我们要再次使用Out of the Box部署用户表单Nexus。

    我们还需要在全局settings.xml（%USER_HOME%/.m2/settings.xml）中配置nexus-releases服务器的凭证。

    ```xml
    <servers>
    <server>
        <id>nexus-releases</id>
        <username>deployment</username>
        <password>the_pass_for_the_deployment_user</password>
    </server>
    </servers>
    ```

    这就是完整的配置

4. 释放过程

    让我们把发布过程分成几个小而集中的步骤。当项目的当前版本是SNAPSHOT版本--比如说0.1-SNAPSHOT，我们就执行一个发布。

    1. Release:clean

        清理一个 Release 将会。

        删除发布描述符（release.properties）。
        删除任何备份的 POM 文件

    2. Release:prepare

        发布过程的下一个部分是准备发布；这将：

        - 执行一些检查--应该没有未提交的修改，项目应该不依赖于任何SNAPSHOT的依赖关系
        - 将pom文件中的项目版本改为完整的版本号（去掉SNAPSHOT的后缀）--在我们的例子中是0.1
        - 运行项目的测试套件
        - 提交并推送修改内容
        - 从这个非SNAPSHOT版本的代码中创建标签
        - 增加项目在pom中的版本 - 在我们的例子中 - 0.2-SNAPSHOT
        - 提交并推送这些变化

    3. release:perform

        发布过程的后半部分是执行发布；这将：

        - 从 SCM 签出发布标签
        - 构建并部署发布的代码
        这个过程的第二步依赖于准备步骤的输出 - Release.properties。

5. 在Jenkins上

    Jenkins可以通过两种方式之一执行发布过程--它可以使用自己的发布插件，也可以简单地通过标准的maven作业运行正确的发布步骤来执行发布。

    现有的专注于发布过程的Jenkins插件有。

    - [发布插件](https://wiki.jenkins-ci.org/display/JENKINS/Release+Plugin)
    - [M2发布插件](https://wiki.jenkins-ci.org/display/JENKINS/M2+Release+Plugin)

    然而，由于执行发布的Maven命令足够简单，我们可以简单地定义一个标准的Jenkins作业来执行该操作--不需要插件。

    因此，对于一个新的Jenkins作业（构建一个maven2/3项目），我们将定义两个字符串参数：releaseVersion=0.1和developmentVersion=0.2-SNAPSHOT。

    在构建配置部分，我们可以简单地配置以下Maven命令来运行。

    ```bash
    release:clean release:prepare release:perform 
    -DreleaseVersion=${releaseVersion} -DdevelopmentVersion=${developmentVersion}
    ```

    当运行一个参数化的作业时，Jenkins会提示用户指定这些参数的值--所以每次运行作业时，我们需要为releaseVersion和developmentVersion填写正确的值。

    另外，值得使用[工作区清理插件](https://wiki.jenkins-ci.org/display/JENKINS/Workspace+Cleanup+Plugin)，并为这次构建勾选在构建开始前删除工作区的选项。但是请记住，Release的执行步骤必须由preparestep的同一命令来运行--这是因为后者的执行步骤将使用prepare所创建的release.properties文件。这意味着我们不能让一个Jenkins作业运行prepare，而另一个运行perform。

6. 总结

    本文展示了如何在有无Jenkins的情况下实现Maven项目的发布过程。与部署类似，该过程使用nexus-staging-maven-plugin与Nexus交互，重点是一个git项目。

## Maven部署到Nexus

在上章节中，我讨论了Maven项目如何在本地安装尚未在Maven中心（或其他大型公共托管仓库）部署的第三方jar。

该方案只适用于小型项目，因为在这些项目中，安装、运行和维护一个完整的Nexus服务器可能是多余的。然而，随着项目的发展。

Nexus很快就会成为唯一真正成熟的选择，用于托管第三方工件，以及跨开发流重复使用内部工件。

本文将介绍如何用Maven将项目的工件部署到Nexus。

1. pom.xml中的Nexus要求

    为了让Maven能够部署它在构建的打包阶段创建的工件，它需要通过distributionManagement元素定义打包后的工件将被部署的仓库信息。

    ```xml
    <distributionManagement>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>http://localhost:8081/nexus/content/repositories/snapshots</url>
    </snapshotRepository>
    </distributionManagement>
    ```

    在Nexus上，一个托管的公共Snapshots仓库是开箱即用的，所以不需要再创建或配置什么。Nexus很容易确定其托管仓库的URL--每个仓库都会在项目pom的`<distributionManagement>`中显示要添加的确切条目，在Summary标签下。

2. 插件

    默认情况下，Maven通过maven-deploy-plugin处理部署机制--这与默认Maven生命周期的部署阶段相匹配。

    ```xml
    <plugin>
    <artifactId>maven-deploy-plugin</artifactId>
    <version>2.8.1</version>
    <executions>
        <execution>
            <id>default-deploy</id>
            <phase>deploy</phase>
            <goals>
                <goal>deploy</goal>
            </goals>
        </execution>
    </executions>
    </plugin>
    ```

    maven-deploy-plugin是处理向Nexus部署项目工件任务的一个可行选择，但它并不是为了充分利用Nexus所能提供的优势而建立。正因为如此，Sonatype建立了一个Nexus专用插件--nexus-staging-maven-plugin--实际上是为了充分利用Nexus提供的更高级的功能--如staging功能。

    虽然在简单的部署过程中，我们不需要暂存功能，但我们将继续使用这个定制的Nexus插件，因为它的目的很明确，就是为了与Nexus很好地对话。

    使用maven-deploy-plugin的唯一原因是为将来使用Nexus的替代方案留有余地，比如[Artifactory](https://www.jfrog.com/open-source/#os-arti)仓库。然而，与其他组件不同的是，Maven Repository Manager在项目的整个生命周期中实际上可能会发生变化，而Maven Repository Manager极不可能发生变化，所以不需要这种灵活性。

    因此，在部署阶段使用另一个部署插件的第一步是禁用现有的、默认的映射。

    ```xml
    <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-deploy-plugin</artifactId>
    <version>${maven-deploy-plugin.version}</version>
    <configuration>
        <skip>true</skip>
    </configuration>
    </plugin>
    ```

    现在，我们可以定义。

    ```xml
    <plugin>
    <groupId>org.sonatype.plugins</groupId>
    <artifactId>nexus-staging-maven-plugin</artifactId>
    <version>1.5.1</version>
    <executions>
        <execution>
            <id>default-deploy</id>
            <phase>deploy</phase>
            <goals>
                <goal>deploy</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <serverId>nexus</serverId>
        <nexusUrl>http://localhost:8081/nexus/</nexusUrl>
        <skipStaging>true</skipStaging>
    </configuration>
    </plugin>
    ```

    该插件的部署目标被映射到Maven构建的部署阶段。

    还要注意的是，如前所述，我们在向Nexus简单部署-SNAPSHOT工件时不需要暂存功能，所以通过`<skipStaging>`元素完全禁用。

    默认情况下，部署目标包括暂存工作流，这是对发布构建的建议。

3. 全局设置.xml

    部署到Nexus是一项安全操作--任何Nexus实例上都有一个部署用户。

    在项目的pom.xml中不能用该部署用户的凭证配置Maven，以便它能与Nexus正确互动。这是因为pom的语法不允许这样做，更何况pom可能是一个公共构件，所以不适合保存凭证信息。

    服务器的凭证必须在全局Maven setting.xml中定义。

    ```xml
    <servers>
    <server>
        <id>nexus-snapshots</id>
        <username>deployment</username>
        <password>the_pass_for_the_deployment_user</password>
    </server>
    </servers>
    ```

    服务器也可以被配置为使用基于密钥的安全，而不是原始和明文凭证。

4. 部署过程

    执行部署过程是一项简单的任务。

    `mvn clean deploy -Dmaven.test.skip=true`

    在部署工作中跳过测试是可以的，因为这个工作应该是项目部署管道的最后一个工作。

    这种部署管道的一个常见例子是连续的Jenkins作业，每个作业只有在成功完成后才会触发下一个。因此，管道中的前一个作业有责任运行项目的所有测试套件 - 在部署作业运行时，所有测试都应该通过。

    如果运行一条命令，那么测试可以在部署阶段执行前保持激活运行。

    `mvn clean deploy`

5. 总结

    这是一个简单而又高效的解决方案，可以将Maven工件部署到Nexus上。

    它也有些主观臆断--使用nexus-staging-maven-plugin代替默认的maven-deploy-plugin；禁用暂存功能等--正是这些选择使该解决方案简单实用。

    激活完整的暂存功能可能是未来文章的主题。

    最后，我们将在下一篇文章中讨论发布流程。

## Relevant Articles

- [x] [Maven Release to Nexus](https://www.baeldung.com/maven-release-nexus)
- [x] [Maven Deploy to Nexus](https://www.baeldung.com/maven-deploy-nexus)
