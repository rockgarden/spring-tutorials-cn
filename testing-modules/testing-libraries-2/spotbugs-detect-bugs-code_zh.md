# [SpotBugs 简介](https://www.baeldung.com/spotbugs-detect-bugs-code)

测试

Eclipse IntelliJ    静态分析

1. 概述

    识别 Java 程序中的 bug 是软件开发中的一个关键挑战。[SpotBugs](https://github.com/spotbugs/spotbugs) 是一个开源的静态分析工具，用于查找 Java 代码中的 bug。它通过对 Java 字节码进行分析来识别潜在问题（如 bug、性能问题或不良编码实践），而不是直接分析源代码。SpotBugs 是 FindBugs 的继任者，并在其基础上进行了增强，提供了更详细和精确的 bug 检测能力。

    在本文中，我们将探讨如何在一个 Java 项目上设置 SpotBugs，并将其集成到 IDE 和 Maven 构建流程中。

2. Bug 模式（Bug Patterns）

    SpotBugs 可以检查超过 400 种 [bug 模式](https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html#bug-descriptions)，例如空指针解引用、无限递归循环、对 Java 库的错误使用、死锁等。

    在 SpotBugs 中，bug 模式通过多个变量进行分类，包括违规类型、类别、bug 的等级以及发现过程的可信度。SpotBugs 将 bug 分为以下十大类：

    1. Bad Practice（不良实践）：检测可能导致未来问题的不良编码习惯，例如 hashCode 和 equals 方法的问题、Cloneable 使用不当、异常被忽略、Serializable 问题、finalize 方法误用。
    2. Correctness（正确性）：识别可能导致运行时错误的代码，如可能的 bug。
    3. Experimental（实验性）：相对新出现的、正在试验中或尚未完全验证的 bug 模式。
    4. Internationalization（国际化）：检测与国际化和区域相关的潜在问题。
    5. Malicious Code Vulnerability（恶意代码漏洞）：标记可能被攻击者利用的代码。
    6. Multithreaded Correctness（多线程正确性）：检查多线程代码中的潜在问题，如竞态条件和死锁。
    7. Bogus Random Noise（随机噪声）：主要用于数据挖掘实验中的控制组，而非实际 bug 检测。
    8. Performance（性能）：识别不一定错误但可能效率低下的代码。
    9. Security（安全性）：突出显示代码中的安全漏洞。
    10. Dodgy Code（可疑代码）：寻找虽然不一定是错误但可疑且可能有问题的代码。例如死局部变量赋值、switch 穿透、未经确认的类型转换、冗余的 null 检查等。

    SpotBugs 的一个特点是它可以将 bug 分为不同的严重程度等级。SpotBugs 使用 1 到 20 的数值范围来表示问题的严重性，数值越高表示越严重。数值等级可以分为以下几类：

    - 最可怕（高优先级）：1 至 4
    - 可怕（中优先级）：5 至 9
    - 令人担忧（低优先级）：10 至 14
    - 需要注意（信息类）：15 至 20

3. SpotBugs Maven 插件

    SpotBugs 可以作为独立应用程序使用，也可以通过多种集成方式使用，包括 Maven、Gradle、Eclipse 和 IntelliJ。本节重点介绍其与 Maven 的集成。

    1. Maven 配置

        首先，在 `pom.xml` 文件的 `<plugins>` 部分中添加 SpotBugs Maven 插件：

        ```xml
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.8.5.0</version>
            <dependencies>
                <dependency>
                    <groupId>com.github.spotbugs</groupId>
                    <artifactId>spotbugs</artifactId>
                    <version>4.8.5</version>
                </dependency>
            </dependencies>
        </plugin>
        ```

    2. 生成报告

        插件添加完成后，打开终端并运行以下命令：

        ```bash
        mvn spotbugs:check
        ```

        这将对我们的源代码进行分析，并输出需要修复的警告列表。为了生成 [bug 报告](https://www.baeldung.com/cs/write-good-bug-reports)，我们需要一些示例代码。我们假设使用以下类：

        ```java
        public class Application {
            public static final String NAME = "Name: ";
            
            private Application() {
            }
            
            public static String readName() {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.next();
                return NAME.concat(input);
            }
        }
        ```

        运行 `mvn spotbugs:check` 后，可能会看到如下输出：

        ```log
        [INFO] BugInstance size is 1
        [INFO] Error size is 0
        [INFO] Total bugs: 1
        [ERROR] High: 在 com.baeldung.systemin.Application.readName() 中发现依赖默认编码的问题：
        new java.util.Scanner(InputStream) [com.baeldung.systemin.Application] 
        At Application.java:[line 13] DM_DEFAULT_ENCODING
        ```

        从该报告可以看出，我们的 `Application` 类有一个高优先级 bug。

    3. 查看结果

        SpotBugs 默认会在 `target/spotbugsXml.xml` 中生成 XML 格式的报告。要获得更美观的 HTML 报告，可以在 SpotBugs 插件中添加以下配置：

        ```xml
        <configuration>
            <htmlOutput>true</htmlOutput>
        </configuration>
        ```

        现在运行：

        ```bash
        mvn clean install
        mvn spotbugs:check
        ```

        然后打开 `target/spotbugs.html` 文件即可在浏览器中查看报告。

        此外，我们还可以通过使用 Maven 命令 `mvn spotbugs:gui` 查看 SpotBugs GUI 中的 bug 详情。

        通过这一反馈，我们现在可以主动更新代码以修复该漏洞。

    4. 修复 Bug

        我们的 `Application` 类存在 [`DM_DEFAULT_ENCODING`](https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html#dm-reliance-on-default-encoding-dm-default-encoding) 错误。该错误表示在执行 I/O 操作时使用了默认字符编码，这可能导致在不同环境或平台下行为不一致。通过显式指定字符编码，我们可以确保无论平台默认编码如何，都能保持一致性。

        修复方法是为 `Scanner` 显式指定编码：

        ```java
        public static String readName() {
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.displayName());
            String input = scanner.next();
            return NAME.concat(input);
        }
        ```

        再次运行 `mvn spotbugs:check`，应该会看到没有 bug 的报告：

        ```log
        [INFO] BugInstance size is 0
        [INFO] Error size is 0
        [INFO] No errors/warnings found
        ```

4. SpotBugs IntelliJ IDEA 插件

    IntelliJ 的 SpotBugs 插件提供静态字节码分析功能，可在 IntelliJ IDEA 内部查找 Java 代码中的 bug。

    1. 安装

        1. 打开 IntelliJ IDEA。
        2. 如果有项目打开，点击 “File -> Settings”（macOS 上为 “IntelliJ IDEA -> Preferences”）。
        3. 在设置窗口中选择 “Plugins”，然后进入 “Marketplace” 标签页。
        4. 使用搜索栏查找 “SpotBugs”，找到后点击 “Install” 安装。
        5. 安装完成后重启 IntelliJ IDEA。

        如果你希望手动安装插件，可以从 [JetBrains 插件仓库](https://plugins.jetbrains.com/plugin/14014-spotbugs/versions#tabs)下载插件 ZIP 文件，然后通过 “Install Plugin from Disk” 进行安装。

    2. 浏览报告

        在 IDEA 中启动静态分析，点击 SpotBugs-IDEA 面板中的 “Analyze Current File”，然后检查结果。

        你可以使用左侧命令栏的第二列按钮按各种因素对 bug 进行分组，例如 bug 类别、类名、包名、优先级或 bug 等级。

        还可以通过第三列的 “export” 按钮将报告导出为 XML 或 HTML 格式。

    3. 配置

        SpotBugs 插件提供多种偏好设置，允许用户自定义分析流程和 bug 报告方式。典型配置包括：

        - Bug 分类与优先级：设置要检测的 bug 类别和最低报告等级。
        - 过滤文件：定义哪些代码部分应被包含或排除。
        - 注解：配置类、方法和字段的注解，用于抑制特定警告。
        - 探测器：启用或禁用特定 bug 探测器，例如专注于安全相关 bug。

        这些设置使开发者能够灵活控制 SpotBugs 如何分析代码并报告潜在问题。

5. SpotBugs Eclipse 插件

    1. 安装

        1. 打开 Eclipse IDE。
        2. 点击顶部菜单栏的 “Help”，选择 “Eclipse Marketplace”。
        3. 在搜索框中输入 “SpotBugs”，按下回车键。
        4. 找到 SpotBugs 并点击 “Install”。
        5. 按照向导完成安装，最后重启 Eclipse。

        如果无法在 Marketplace 中找到 SpotBugs，可以通过更新站点安装：

        1. 点击 “Help -> Install New Software”。
        2. 点击 “Add”，输入名称（如 “SpotBugs”）和地址：`https://spotbugs.github.io/eclipse`。
        3. 添加后勾选 SpotBugs，继续安装流程。

    2. 浏览报告

        右键点击项目资源管理器中的项目 → 选择 “SpotBugs -> Find Bugs”。Eclipse 将在 “Bug Explorer” 窗口中显示分析结果。

    3. 配置

        进入 “Window -> Preferences -> Java -> SpotBugs” 可以调整配置：

        - 勾选/取消勾选 bug 类别
        - 设置最小报告等级和可信度
        - 自定义 bug 等级的标记
        - 在 “Detector configuration” 中启用或禁用特定探测器
        - 在 “Filter files” 中创建自定义文件过滤规则

        这些配置帮助开发者精细控制哪些代码部分参与分析，避免无关文件影响 bug 检测。

6. 总结

    在本文中，我们讨论了在 Java 项目中使用 SpotBugs 的基本要点。SpotBugs 是一种静态分析工具，通过检查字节码帮助我们识别代码中的潜在 bug。

    我们学习了如何在 Maven 中配置 SpotBugs —— 这是在 Java 生态系统中最常用的构建自动化工具之一，以便在构建过程中自动检测问题。

    此外，我们还介绍了如何将 SpotBugs 集成到流行的开发环境（如 IntelliJ IDEA 和 Eclipse）中，确保我们能够轻松地将 bug 检测整合到日常开发流程中。
