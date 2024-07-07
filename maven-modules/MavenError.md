# Maven Error

## JAVA_HOME 应指向 JDK 而不是 JRE

1. 简介

    在本教程中，我们将讨论Maven在配置错误时抛出的一个异常。JAVA_HOME应指向JDK，而非JRE。

    Maven是构建代码的强大工具。我们将深入了解这个错误发生的原因，并将看到如何解决这个问题。

2. JAVA_HOME问题

    安装Maven后，我们必须设置JAVA_HOME环境变量，以便该工具知道在哪里找到要执行的JDK命令。Maven的目标是针对项目的源代码运行适当的Java命令。

    例如，最常见的情况是通过执行javac命令来编译代码。

    如果JAVA_HOME没有指向有效的JDK安装，Maven会在每次执行时抛出一个错误。

    ```shell
    mvn -version
    # Output... 
    The JAVA_HOME environment variable is not defined correctly
    This environment variable is needed to run this program
    NB: JAVA_HOME should point to a JDK, not a JRE
    ```

3. JDK或JRE

    Maven是如何验证JAVA_HOME路径的？

    在运行任何目标之前，Maven会检查JAVA_HOME指定的路径中是否存在java命令，或者通过询问操作系统是否有默认的JDK安装。如果找不到该可执行文件，Maven就会以错误结束。

    以下是Linux下的mvn可执行文件检查（Apache Maven v3.5.4）。

    ```shell
    if [ -z "$JAVA_HOME" ] ; then
        JAVACMD=`which java`
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
     
    if [ ! -x "$JAVACMD" ] ; then
        echo "The JAVA_HOME environment variable is not defined correctly" >&2
        echo "This environment variable is needed to run this program" >&2
        echo "NB: JAVA_HOME should point to a JDK not a JRE" >&2
        exit 1
    fi
    ```

    这个检查乍看之下似乎很合理，但我们必须考虑到 JDK 和 JRE 都有一个 bin 文件夹，并且都包含一个可执行的 java 文件。

    因此，有可能将JAVA_HOME配置为指向JRE的安装，从而隐藏了这个特殊的错误，并在以后的阶段造成问题。虽然JRE的主要目的是运行Java代码，但JDK也可以进行编译和调试，以及其他重要的区别。

    由于这个原因，mvn编译命令会失败--正如我们将在下一小节看到的。

    1. 由于JRE而不是JDK导致的编译失败

        像通常的默认设置一样，如果我们有一个 "standard" 配置，它们会非常有帮助。

        例如，如果我们在Ubuntu 18.04系统上安装了Java 11，但没有设置JAVA_HOME环境变量，Maven还是会很高兴地找到我们的JDK，并将其用于不同的目标，包括编译。

        但如果我们设法在系统上设置了一个非标准的配置（更不用说弄得一团糟），Maven的帮助性就不再足够了。它甚至会产生误导。

        让我们假设我们在Ubuntu 18.04上有以下配置。

        - JDK 11
        - JRE 8
        - JAVA_HOME设置为JRE 8的安装路径
        如果我们做基本的检查。

        `mvn --version`

        我们会得到这样一个有意义的输出。

        ```log
        Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-24T18:41:47Z)
        Maven home: /opt/apache-maven-3.6.0
        Java version: 1.8.0_191, vendor: Oracle Corporation, runtime: /usr/lib/jvm/java-8-openjdk-amd64/jre
        Default locale: en, platform encoding: UTF-8
        OS name: "linux", version: "4.15.0-42-generic", arch: "amd64", family: "unix"
        ```

        让我们看看如果我们尝试编译一个项目会发生什么。

        `mvn clean compile`

        现在，我们得到一个错误。

        ```log
        [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.7.0:compile (default-compile) on project my-app: Compilation failure
        [ERROR] No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
        ```

    2. 在项目层面上修复编译错误

        与Maven的许多其他情况一样，建议在全系统范围内进行有意义的设置--在本例中，我们将按照第5节所述改变JAVA_HOME变量的值，使其指向JDK而不是JRE。

        然而，如果我们由于某种原因不能设置默认值，我们仍然可以在项目层面上覆盖这些设置。让我们看看如何做到这一点。

        首先，我们打开项目的pom.xml，进入build/pluginManagement/plugins部分，看一下maven-compiler-plugin的条目。

        ```xml
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.7.0</version>
        </plugin>
        ```

        然后，我们给它添加一个配置，让它使用一个自定义的可执行文件，并跳过搜索JAVA_HOME/bin目录中的javac。

        ```xml
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.7.0</version>
            <configuration>
                <fork>true</fork>
                <executable>/usr/lib/jvm/java-11-openjdk-amd64/bin/javac</executable>
            </configuration>
        </plugin>
        ```

        在Unix系统上，这个可执行文件应该是一个有足够权限的脚本。在Windows系统上，它应该是一个.exe文件。

        接下来，我们将尝试再次编译该项目。

        现在，构建--包括使用maven-compiler-plugin进行的编译阶段--已经成功。

4. 检查JAVA_HOME配置

    检查JAVA_HOME是否指向一个实际的JDK非常简单。我们可以在终端中打印其内容，也可以运行下面的一个shell命令。

    1. 在 Linux 上检查 JAVA_HOME

        只需打开一个终端并输入

        `> $JAVA_HOME/bin/javac -version`

        如果 JAVA_HOME 指向一个 JDK，输出结果应该是这样的

        `> javac 1.X.0_XX`

        如果 JAVA_HOME 没有指向某个 JDK，操作系统将抛出一条错误信息。

        `> bash: /bin/javac: No such file or directory`

    2. 在 Windows 上检查 JAVA_HOME

        打开一个命令提示符，并输入

        `%JAVA_HOME%\bin\javac -version`

        如果 JAVA_HOME 指向一个 JDK，输出结果应该是这样的。

        `> javac 1.X.0_XX`

        如果 JAVA_HOME 没有指向某个 JDK，操作系统将抛出一条错误信息。

        `> The system cannot find the path specified.`

5. 如何解决这个问题

    首先，我们需要知道在哪里可以找到我们的 JDK。

    - 如果我们使用软件包安装程序安装了我们的 JDK 分发版，我们应该能够使用操作系统搜索工具找到该路径
    - 如果是可移植的发行版，让我们检查一下我们提取它的文件夹。
    一旦我们知道了JDK的路径，我们就可以设置JAVA_HOME环境变量，使用[适合我们特定操作系统的结果](https://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux)。

6. 总结

    在这个简短的教程中，我们讨论了 "JAVA_HOME应指向JDK而非JRE" 的Maven错误，并检查了其根本原因。

    最后，我们讨论了如何检查你的JAVA_HOME环境变量，以及如何确保它指向JDK。

## 处理Maven无效的LOC头错误

1. 简介

    有时，当我们本地Maven repo中的jar损坏时，我们会看到错误：Invalid LOC Header。

    在本教程中，我们将学习何时发生这种情况，以及如何处理甚至有时防止这种情况。

2. 什么时候会出现 "无效的LOC头"？

    Maven将项目的依赖项下载到文件系统中的一个已知位置，称为本地仓库。Maven下载的每个工件都有其SHA1和MD5校验文件。

    这些校验和的目的是为了确保相关工件的完整性。由于网络和文件系统会出现故障，就像其他东西一样，有时工件会被损坏，使工件内容与签名不一致。

    在这种情况下，Maven构建时会出现 "无效的LOC头" 错误。

    解决办法是将损坏的jar从版本库中删除。让我们来看看几种方法。

3. 删除本地版本库

    解决该错误的快速方法是删除整个Maven本地仓库，然后重新构建项目：

    `rm -rf ${LOCAL_REPOSITORY}`

    这将删除本地缓存并重新下载所有项目的依赖项--效率不高。

    注意，默认的本地仓库在 ${user.home}/.m2/repository，除非我们在 settings.xml `<localRepository>` 标签中指定它。

    我们也可以通过以下命令找到本地仓库：`mvn help:evaluate -Dexpression=settings.localRepository`

4. 找到损坏的jar

    另一个解决方案是识别特定的损坏的jar，并从本地仓库中删除它。

    当我们使用Maven输出堆栈跟踪命令时，当它处理失败时就会包含损坏的jar细节。

    我们可以通过在构建命令中添加-X来启用调试级别的日志记录：

    `mvn -X package`

    由此产生的堆栈跟踪将在日志的结尾处指出损坏的jar。确定损坏的jar后，我们可以在本地资源库中找到它并删除。现在构建时，Maven会重新下载该jar。

    此外，我们还可以用zip -T命令测试存档的完整性：

    `find ${LOCAL_REPOSITORY} -name "*.jar" | xargs -L 1 zip -T | grep error`

5. 验证校验和

    前面提到的两种解决方案只会迫使Maven重新下载jar。当然，这个问题可能在以后的下载中再次出现。我们可以通过配置Maven，使其在从远程仓库下载工件时验证校验和来防止这种情况。

    我们可以在Maven命令中添加-strict-checksums或-C选项。如果计算出的校验和与校验文件中的值不一致，这将导致Maven构建失败。

    有两个选项，可以在校验和不匹配的情况下导致构建失败：

    `-C,--strict-checksums`

    或警告，这是默认选项：

    `-c,--lax-checksums`

    今天，Maven在将工件上传到中央仓库时需要[签名文件](https://central.sonatype.org/pages/requirements.html#sign-files-with-gpgpgp)。但中央版本库中可能有一些工件没有签名文件，尤其是历史性的工件。这就是为什么默认选项是警告。

    为了获得更持久的解决方案，我们可以在Maven的settings.xml文件中配置 checksumPolicy。该属性规定了工件校验失败时的行为。为了避免将来出现问题，让我们编辑settings.xml文件，以便在校验失败时无法下载：

    ```xml
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <id>codehausSnapshots</id>
                    <name>Codehaus Snapshots</name>
                    <releases>
                        <enabled>false</enabled>
                        <updatePolicy>always</updatePolicy>
                        <checksumPolicy>fail</checksumPolicy>
                    </releases>
                </repository>
            </repository>
        </profile>
    </profiles>
    ```

    当然，我们需要对每一个配置好的存储库都这样做。

6. 总结

    在这篇快速的文章中，我们已经看到了什么时候会发生无效的LOC头错误，以及如何处理它的选项。

## Relevant Articles

- [x] [Maven Error “JAVA_HOME should point to a JDK not a JRE”](https://www.baeldung.com/maven-java-home-jdk-jre)
- [x] [Handling Maven Invalid LOC Header Error](https://www.baeldung.com/maven-invalid-loc-header-error)
