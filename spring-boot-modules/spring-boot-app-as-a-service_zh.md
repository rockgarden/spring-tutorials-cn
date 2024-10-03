# [Spring Boot 应用程序即服务](https://www.baeldung.com/spring-boot-app-as-a-service)

1. 概述

    本文探讨了将 Spring Boot 应用程序作为服务运行的一些选项。

    首先，我们将解释网络应用程序的打包选项和系统服务。在随后的章节中，我们将探讨为基于 Linux 和 Windows 的系统设置服务时的不同选择。

    最后，我们将引用一些其他信息来源作为结束语。

2. 项目设置和构建说明

    1. 打包

        传统上，Web 应用程序被打包为 Web Application aRchives (WAR)，并部署到 Web 服务器上。

        Spring Boot 应用程序既可以打包为 WAR 文件，也可以打包为 JAR 文件。后者在 JAR 文件中嵌入了网络服务器，因此无需安装和配置应用服务器即可运行应用程序。

    2. Maven 配置

        让我们先定义 pom.xml 文件的配置：

        ```xml
        <packaging>jar</packaging>

        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>1.4.0.RELEASE</version>
        </parent>

        <dependencies>
            ....
        </dependencies>

        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <executable>true</executable>
                    </configuration>
                </plugin>
            </plugins>
        </build>
        ```

        打包必须设置为 jar。在撰写本文时，我们使用的是 Spring Boot 的最新稳定版本，但 1.3 之后的任何版本都已足够。有关可用版本的更多信息，请点击[此处](https://spring.io/projects/spring-boot)。

        请注意，我们已将 Spring-boot-maven-plugin 工具的 `<executable>` 参数设置为 true。这将确保在 JAR 包中添加 MANIFEST.MF 文件。该清单包含一个 Main-Class 条目，用于指定哪个类定义了应用程序的主方法。

    3. 构建应用程序

        在应用程序的根目录下运行以下命令：

        `$ mvn clean package`

        现在目标目录中已经有了可执行的 JAR 文件，我们可以通过在命令行上执行以下命令来启动应用程序：

        `$ java -jar your-app.jar`

        此时，您仍需使用 -jar 选项调用 Java 解释器。将应用程序作为服务调用启动会更好，原因有很多。

3. 在 Linux 上

    为了将程序作为后台进程运行，我们可以简单地使用 nohup Unix 命令，但由于种种原因，这也不是首选方法。本主题提供了很好的[解释](http://stackoverflow.com/questions/958249/whats-the-difference-between-nohup-and-a-daemon)。

    取而代之的是守护进程。在 Linux 下，我们可以选择使用传统的 System V init 脚本或 Systemd 配置文件来配置守护进程。前者历来是最著名的选择，但现在正逐渐被后者取代。

    有关这一区别的更多详情，请点击[此处](http://www.tecmint.com/systemd-replaces-init-in-linux/)。

    为增强安全性，我们首先创建一个特定用户来运行服务，并相应更改可执行 JAR 文件的权限：

    ```bash
    sudo useradd baeldung
    sudo passwd baeldung
    sudo chown baeldung:baeldung your-app.jar
    sudo chmod 500 your-app.jar
    ```

    1. 系统 V 初始化

        Spring Boot 可执行 JAR 文件让服务设置过程变得非常简单：

        `$ sudo ln -s /path/to/your-app.jar /etc/init.d/your-app`

        上述命令将创建一个指向可执行 JAR 文件的符号链接。您必须使用可执行 JAR 文件的完整路径，否则符号链接将无法正常工作。通过该链接，您可以将应用程序作为服务启动：

        `$ sudo service your-app start`

        该脚本支持标准的服务启动、停止、重启和状态命令。此外

        - 它会启动在我们刚刚创建的用户 baeldung 下运行的服务
        - 在 /var/run/your-app/your-app.pid 中跟踪应用程序的进程 ID
        - 它将控制台日志写入 /var/log/your-app.log，如果应用程序无法正常启动，您可能需要检查该日志。

    2. 系统服务

        systemd 服务的设置也非常简单。首先，我们使用以下示例创建名为 your-app.service 的脚本，并将其放入 /etc/systemd/system 目录：

        ```service
        [Unit]
        Description=A Spring Boot application
        After=syslog.target

        [Service]
        User=baeldung
        ExecStart=/path/to/your-app.jar SuccessExitStatus=143

        [Install]
        WantedBy=multi-user.target
        ```

        记住要修改描述、用户和 ExecStart 字段，使其与你的应用程序相匹配。此时，您应该也能执行上述标准服务命令。

        与上一节所述的 System V 启动方法不同，进程 ID 文件和控制台日志文件应使用服务脚本中的适当字段明确配置。有关选项的详尽列表，请参见[此处](http://www.freedesktop.org/software/systemd/man/systemd.service.html)。

    3. 启动

        [Upstart](https://wiki.ubuntu.com/Upstart/) 是一个基于事件的服务管理器，是 System V init 的潜在替代品，能对不同守护进程的行为提供更多控制。

        网站上有很好的[安装说明](https://help.ubuntu.com/community/UbuntuBootupHowto)，几乎适用于所有 Linux 发行版。使用 Ubuntu 时，你可能已经安装并配置了它（检查 /etc/init 中是否有名称以 “upstart” 开头的任务）。

        我们创建一个 job your-app.conf，用于启动 Spring Boot 应用程序：

        ```bash
        # Place in /home/{user}/.config/upstart
        description "Some Spring Boot application"
        respawn # attempt service restart if stops abruptly
        exec java -jar /path/to/your-app.jar
        ```

        现在运行 “start your-app”，你的服务就会启动。

        Upstart 提供了许多作业配置选项，你可以在这里找到大部分选项。

4. 在 Windows 上

    在本节中，我们将介绍几个可用于将 Java JAR 作为 Windows 服务运行的选项。

    1. Windows 服务封装器

        由于 Java 服务封装器 的 GPL 许可证（见下一小节）与 Jenkins 的 MIT 许可证（等）难以兼容，因此 [Windows 服务封装器](https://github.com/kohsuke/winsw) 项目（又称 winsw）应运而生。

        Winsw 提供了安装/卸载/启动/停止服务的编程方法。此外，它还可用于在 Windows 下以服务形式运行任何类型的可执行文件，而 Java Service Wrapper（如其名称所示）仅支持 Java 应用程序。

        首先，在[这里](http://repo.jenkins-ci.org/releases/com/sun/winsw/winsw/)下载二进制文件。然后，定义 Windows 服务的配置文件 MyApp.xml 应如下所示：

        ```xml
        <service>
            <id>MyApp</id>
            <name>MyApp</name>
            <description>This runs Spring Boot as a Service.</description>
            <env name="MYAPP_HOME" value="%BASE%"/>
            <executable>java</executable>
            <arguments>-Xmx256m -jar "%BASE%\MyApp.jar"</arguments>
            <logmode>rotate</logmode>
        </service>
        ```

        最后，你必须将 winsw.exe 重命名为 MyApp.exe，使其名称与 MyApp.xml 配置文件相匹配。之后就可以像这样安装服务了：

        `$ MyApp.exe install`

        类似地，您还可以使用卸载、启动、停止等操作。

    2. Java 服务封装器

        如果您不介意 [Java Service Wrapper](http://wrapper.tanukisoftware.org/doc/english/index.html) 项目的 GPL 许可，这个替代方案同样可以满足您将 JAR 文件配置为 Windows 服务的需求。基本上，Java Service Wrapper 还要求您在配置文件中指定如何在 Windows 下将进程作为服务运行。

        [本文](https://medium.com/@lk.snatch/jar-file-as-windows-service-bonus-jar-to-exe-1b7b179053e4)非常详细地介绍了如何在 Windows 下将 JAR 文件设置为服务执行，因此无需重复介绍。

5. 其他参考资料

    Spring Boot 应用程序也可以使用 [Apache Commons Daemon](http://commons.apache.org/daemon/index.html) 项目的 [Procrun](http://commons.apache.org/proper/commons-daemon/procrun.html) 作为 Windows 服务启动。Procrun 是一套允许 Windows 用户将 Java 应用程序封装为 Windows 服务的应用程序。这样的服务可以设置为在机器启动时自动启动，并在没有任何用户登录的情况下继续运行。

    有关在 Unix 下启动 Spring Boot 应用程序的更多详情，请点击[此处](http://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html)。还有关于如何修改[基于 Redhat 系统的 Systemd 单元文件](https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/7/html/system_administrators_guide/chap-managing_services_with_systemd)的详细说明。最后

    最后，本快速[指南](https://coderwall.com/p/ssuaxa/how-to-make-a-jar-file-linux-executable)介绍了如何在 JAR 文件中加入 Bash 脚本，使其本身成为一个可执行文件！

6. 总结

    服务允许您非常高效地管理应用程序状态，正如我们所看到的，Spring Boot 应用程序的服务设置现在比以往任何时候都要简单。

    只需记住，在运行服务时遵循重要而简单的用户权限安全措施即可。
