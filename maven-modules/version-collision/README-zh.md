# 如何解决Maven中工件的版本冲突问题

1. 概述

    多模块Maven项目可能有复杂的依赖关系图。这些模块之间相互导入的内容越多，就会产生不寻常的结果。

    在本教程中，我们将了解如何解决Maven中工件的版本冲突。

    我们将从一个[多模块](https://www.baeldung.com/maven-multi-module)项目开始，故意使用同一工件的不同版本。然后，我们会看到如何通过排除法或依赖性管理来防止获得错误的工件版本。

    最后，我们将尝试使用maven-enforcer-plugin，通过禁止使用横向依赖关系，使事情更容易控制。

2. 工件的版本碰撞

    我们在项目中包含的每个依赖项都可能与其他工件相连接。Maven可以自动引入这些工件，也叫跨度依赖。当多个依赖关系链接到同一个工件，但使用不同的版本时，就会发生版本冲突。

    因此，我们的应用程序在编译阶段和运行时都可能出现错误。

    1. 项目结构

        让我们定义一个多模块的项目结构来进行实验。我们的项目由一个版本冲突的父模块和三个子模块组成：

        ```txt
        version-collision
            project-a
            project-b
            project-collision
        ```

        项目a和项目b的pom.xml几乎是一样的。唯一的区别是它们所依赖的com.google.guava artifact的版本。特别是，项目a使用的是22.0版本。

        但是，project-b使用较新的版本，29.0-jre：

        第三个模块，project-collision，依赖于其他两个：

        project-collision/pom.xml

        那么，哪一个版本的guava将可用于project-collision？

    2. 使用特定依赖版本的功能

        我们可以通过在project-collision模块中创建一个简单的测试，使用guava中的Futures.immediateVoidFuture方法，来了解哪个依赖性被使用：

        version.collision/VersionCollisionUnitTest.java: whenVersionCollisionDoesNotExist_thenShouldCompile()

        这个方法只在29.0-jre版本中可用。我们从其他模块中继承了这一点，但我们只有从project-b中得到反式依赖，才能编译我们的代码。

    3. 版本冲突导致的编译错误

        根据project-collision模块中依赖关系的顺序，在某些组合中，Maven会返回一个编译错误：

        ```log
        [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.1:testCompile (default-testCompile) on project project-collision: Compilation failure
        [ERROR] /tutorials/maven-all/version-collision/project-collision/src/test/java/com/baeldung/version/collision/VersionCollisionUnitTest.java:[12,27] cannot find symbol
        [ERROR]   symbol:   method immediateVoidFuture()
        [ERROR]   location: class com.google.common.util.concurrent.Futures
        ```

        这就是com.google.guava工件的版本碰撞的结果。默认情况下，对于依赖树中处于同一级别的依赖项，Maven会选择它找到的第一个库。在我们的例子中，com.google.guava的两个依赖项都在同一高度，因此选择了较早的版本。

        version-collision-project-a 先引入。

    4. 使用maven-dependency-plugin

        maven-dependency-plugin是一个非常有用的工具，可以展示所有的依赖项及其版本：

        ```bash
        % mvn dependency:tree -Dverbose

        [INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ project-collision ---
        [INFO] com.baeldung:project-collision:jar:0.0.1-SNAPSHOT
        [INFO] +- com.baeldung:project-a:jar:0.0.1-SNAPSHOT:compile
        [INFO] |  \- com.google.guava:guava:jar:22.0:compile
        [INFO] \- com.baeldung:project-b:jar:0.0.1-SNAPSHOT:compile
        [INFO]    \- (com.google.guava:guava:jar:29.0-jre:compile - omitted for conflict with 22.0)
        ```

        -Dverbose标志显示冲突的工件。事实上，我们有一个com.google.guava的依赖，有两个版本： 22.0和29.0-jre。后者是我们想在project-collision模块中使用的那个。

3. 从工件中排除一个过渡性依赖

    解决版本冲突的一种方法是通过从特定的工件中移除冲突的过渡性依赖。在我们的例子中，我们不想让com.google.guava库从project-a工件中过渡性地加入。

    因此，我们可以在project-collision pom中排除它：

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.baeldung</groupId>
            <artifactId>project-a</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>com.google.guava</groupId>
                    <artifactId>guava</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>com.baeldung</groupId>
            <artifactId>project-b</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```

    现在，当我们运行dependency:tree命令时，我们可以看到它已经不存在了：

    结果，编译阶段结束，没有出现错误，我们可以使用29.0-jre版本的类和方法。

4. 使用依赖性管理部分

    Maven的依赖性管理部分是一种集中依赖性信息的机制。它最有用的功能之一是控制作为横向依赖的工件的版本。

    考虑到这一点，让我们在父pom中创建一个依赖管理配置：

    ```xml
    <dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>29.0-jre</version>
        </dependency>
    </dependencies>
    </dependencyManagement>
    ```

    因此，Maven将确保在所有子模块中使用com.google.guava artifact的29.0-jre版本。

5. 防止意外的过渡性依赖

    maven-enforcer-plugin提供了许多内置规则，简化了多模块项目的管理。其中之一是禁止使用来自交叉依赖的类和方法。

    明确的依赖性声明消除了工件版本冲突的可能性。让我们把带有该规则的maven-enforcer-plugin添加到我们的父pom中：

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>3.0.0-M3</version>
        <executions>
            <execution>
                <id>enforce-banned-dependencies</id>
                <goals>
                    <goal>enforce</goal>
                </goals>
                <configuration>
                    <rules>
                        <banTransitiveDependencies/>
                    </rules>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ```

    因此，如果我们想自己使用com.google.guava工件，我们现在必须在我们的project-collision模块中明确声明它。我们必须指定要使用的版本，或者在父级pom.xml中设置dependencyManagement。这使得我们的项目更加防错，但需要我们在pom.xml文件中更加明确。

6. 结论

    在这篇文章中，我们看到了如何解决Maven中工件的版本冲突。

    首先，我们探讨了一个多模块项目中的版本冲突的例子。

    然后，我们展示了如何在pom.xml中排除反相依赖。我们研究了如何通过父pom.xml中的依赖管理部分来控制依赖版本。

    最后，我们尝试使用maven-enforcer-plugin来禁止使用横向依赖，以迫使每个模块控制自己的依赖。

## Relevant Articles

- [x] [How to Resolve a Version Collision of Artifacts in Maven](https://www.baeldung.com/maven-version-collision)

## Code

像往常一样，本文中的代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/maven-modules/version-collision)上找到。
