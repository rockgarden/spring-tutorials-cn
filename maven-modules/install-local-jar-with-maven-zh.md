# 用Maven安装本地jar

1. 问题与选择

    Maven是一个非常通用的工具，其可用的公共资源库首屈一指。然而，总有一些工件要么不在任何地方托管，要么托管的仓库有风险，因为当你需要它时，它可能不会出现。

    当这种情况发生时，有几个选择。

    - 咬紧牙关，安装一个成熟的版本库管理解决方案，如Nexus
    - 尝试将工件上传到一个有信誉的公共仓库中
    - 使用maven插件在本地安装该工件

    Nexus当然是更成熟的解决方案，但也更复杂。对于使用单个jar这样简单的问题来说，配置一个实例来运行Nexus，设置Nexus本身，配置和维护Nexus，可能都是多余的。如果这种情况--托管自定义工件--是一种常见的情况，那么资源库管理器就有很大的意义。

    将工件直接上传到公共仓库或Maven中心也是一个很好的解决方案，但通常需要很长时间。此外，该库可能根本没有启用Maven，这使得这一过程更加困难，所以这不是一个能够使用工件NOW的现实解决方案。

    这就留下了第三种选择--在源码控制中添加工件，并使用maven插件--在这种情况下，[maven-install-plugin](https://maven.apache.org/plugins/maven-install-plugin/)可以在构建过程需要它之前将其安装到本地。这是迄今为止最简单、最可靠的选择。

2. 用maven-install-plugin安装本地罐子

    让我们先来看看将工件安装到本地仓库所需的全部配置。

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-install-plugin</artifactId>
        <version>2.5.1</version>
        <configuration>
            <groupId>org.somegroup</groupId>
            <artifactId>someartifact</artifactId>
            <version>1.0</version>
            <packaging>jar</packaging>
            <file>${basedir}/dependencies/someartifact-1.0.jar</file>
            <generatePom>true</generatePom>
        </configuration>
        <executions>
            <execution>
                <id>install-jar-lib</id>
                <goals>
                    <goal>install-file</goal>
                </goals>
                <phase>validate</phase>
            </execution>
        </executions>
    </plugin>
    ```

    现在，让我们来分解和分析一下这个配置的细节。

    1. Artifact信息

        Artifact信息被定义为`<configuration>`元素的一部分。实际的语法与声明依赖关系非常相似--一个groupId、artifactId和版本元素。

        配置的下一部分需要定义工件的包装 - 这被指定为jar。

        接下来，我们需要提供实际要安装的jar文件的位置--可以是绝对文件路径，也可以是相对路径，使用Maven中的[可用属性](https://cwiki.apache.org/confluence/display/MAVEN/Maven+Properties+Guide)。本例中，${basedir}属性代表项目的根，即pom.xml文件所在的位置。这意味着someartifact-1.0.jar文件需要放置在根目录下的/dependencies/目录中。

        最后，还有其他几个[可选细节](https://maven.apache.org/plugins/maven-install-plugin/install-file-mojo.html)也可以配置。

    2. 执行

        install-file 目标的执行与标准Maven构建生命周期中的验证阶段相联系。因此，在尝试编译之前，你需要明确运行验证阶段。

        `mvn validate`

        这一步之后，标准的编译就可以工作了。

        `mvn clean install`

        一旦编译阶段执行，我们的someartifact-1.0.jar就会正确地安装在本地仓库中，就像其他可能从Maven中心本身获取的工件一样。

    3. 生成POM与提供POM

        我们是否需要为工件提供一个pom.xml文件的问题主要取决于工件本身的运行时依赖性。简单地说，如果工件在运行时依赖其他的jar，这些jar在运行时也需要出现在classpath上。对于一个简单的工件来说，这不应该是一个问题，因为它在运行时可能没有依赖关系（依赖图中的一个叶子）。

        install-file目标中的generatePom选项对于这些类型的工件来说应该足够了。

        `<generatePom>true</generatePom>`

        然而，如果工件更复杂，并且确实有非琐碎的依赖，那么，如果这些依赖还没有在classpath中，就必须添加它们。一种方法是在项目的pom文件中手动定义这些新的依赖项。一个更好的解决方案是与安装的工件一起提供一个自定义的pom.xml文件。

        ```xml
        <generatePom>false</generatePom>
        <pomFile>${basedir}/dependencies/someartifact-1.0.pom</pomFile>
        ```

        这将使Maven能够解决该自定义pom.xml中定义的神器的所有依赖关系，而不必在项目的主pom文件中手动定义它们。

3. 总结

    本文介绍了如何通过使用maven-install-plugin在本地安装未在Maven项目中托管的jar。

## Relevant Articles

- [x] [Install local jar with Maven](https://www.baeldung.com/install-local-jar-with-maven)
