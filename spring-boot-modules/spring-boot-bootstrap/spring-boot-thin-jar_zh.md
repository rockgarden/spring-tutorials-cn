# [使用Spring Boot的瘦身JARs](https://www.baeldung.com/spring-boot-thin-jar)

1. 简介

    在本教程中，我们将探讨如何使用[spring-boot-thin-launcher](https://github.com/dsyer/spring-boot-thin-launcher)项目，将Spring Boot项目构建为一个瘦JAR文件。

    Spring Boot以其 "fat" JAR部署而闻名，其中一个可执行工件包含了应用程序代码和所有的依赖关系。

    Boot也被广泛用于开发微服务。这有时会与 "fat JAR" 方法相抵触，因为在许多工件中反复包括相同的依赖关系会成为一种重要的资源浪费。

2. 先决条件

    首先，我们当然需要一个Spring Boot项目。在这篇文章中，我们将看看Maven构建，以及Gradle构建的最常见配置。

    我们不可能涵盖所有的构建系统和构建配置，但希望我们能看到足够多的一般原则，使你能够将它们应用于你的特定设置。

    1. Maven项目

        在用Maven构建的Boot项目中，我们应该在项目的pom.xml文件、其父级文件或其父级文件中配置Spring Boot Maven插件：

        ```xml
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>    
        </plugin>
        ```

        Spring Boot依赖的版本通常是通过使用BOM或从父级POM中继承来决定的，就像我们的参考项目一样：

        ```xml
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <relativePath/>
        </parent>
        ```

    2. Gradle项目

        在用Gradle构建的Boot项目中，我们会有Boot Gradle插件：

        ```groovy
        buildscript {
            ext {
                springBootPlugin = 'org.springframework.boot:spring-boot-gradle-plugin'
                springBootVersion = '2.4.0'
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                classpath("${springBootPlugin}:${springBootVersion}")
            }
        }
        // elided
        apply plugin: 'org.springframework.boot'
        apply plugin: 'io.spring.dependency-management'
        springBoot {
            mainClassName = 'com.baeldung.DemoApplication'
        }
        ```

        注意，在本文中，我们将只考虑Boot 2.x及以后的项目。Thin Launcher也支持早期的版本，但它需要一个稍微不同的Gradle配置，为了简单起见，我们省略了这个配置。请看项目的主页以了解更多细节。

3. 如何创建一个Thin JAR？

    Spring Boot Thin Launcher是一个小库，它从归档文件本身捆绑的文件中读取工件的依赖性，从Maven仓库中下载，最后启动应用程序的主类。

    因此，当我们用该库构建一个项目时，我们会得到一个包含代码的JAR文件，一个列举其依赖关系的文件，以及来自该库的执行上述任务的主类。

    当然，事情比我们的简化解释要细微得多；我们将在文章的后面深入讨论一些话题。

4. 基本用法

    现在让我们看看如何从我们普通的Spring Boot应用程序中构建一个 "thin" JAR。

    我们将用通常的`java -jar <my-app-1.0.jar>`来启动应用程序，可选择附加命令行参数来控制Thin Launcher。我们将在下面的章节中看到其中的几个参数；项目的主页上有完整的清单。

    1. Maven项目

        在Maven项目中，我们必须修改Boot插件的声明（见2.1节），加入对自定义 "thin" 布局的依赖：

        ```xml
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <dependencies>
                <!-- The following enables the "thin jar" deployment option. -->
                <dependency>
                    <groupId>org.springframework.boot.experimental</groupId>
                    <artifactId>spring-boot-thin-layout</artifactId>
                    <version>1.0.11.RELEASE</version>
                </dependency>
            </dependencies>
        </plugin>
        ```

        [启动器](https://search.maven.org/classic/#search%7Cga%7C1%7Ca%3A%22spring-boot-thin-layout%22)将从Maven存储在META-INF/maven目录下生成的JAR中的pom.xml文件中读取依赖项。

        我们将像往常一样进行构建，比如用mvn install。

        如果我们希望能够同时产生瘦版和胖版的构建（例如在有多个模块的项目中），我们可以在一个专门的Maven配置文件中声明自定义布局。

    2. Maven和依赖性：thin.properties

        除了pom.xml之外，我们还可以让Maven生成一个thin.properties文件。在这种情况下，该文件将包含完整的依赖项列表，包括跨期的依赖项，而且启动器会优先选择它而不是pom.xml。

        这样做的mojo（插件）是spring-boot-thin-maven-plugin:properties，默认情况下，它在 src/main/resources/META-INF 中输出 thin.properties 文件，但我们可以通过 thin.output 属性指定其位置：

        `$ mvn org.springframework.boot.experimental:spring-boot-thin-maven-plugin:properties -Dthin.output=.`

        请注意，输出目录必须存在，目标才能成功，即使我们保留了默认目录。

    3. Gradle项目

        在Gradle项目中，我们反而要添加一个专门的插件：

        ```groovy
        buildscript {
            ext {
                //...
                thinPlugin = 'org.springframework.boot.experimental:spring-boot-thin-gradle-plugin'
                thinVersion = '1.0.11.RELEASE'
            }
            //...
            dependencies {
                //...
                classpath("${thinPlugin}:${thinVersion}")
            }
        }
        //elided
        apply plugin: 'maven'
        apply plugin: 'org.springframework.boot.experimental.thin-launcher'
        ```

        为了获得瘦版构建，我们要告诉Gradle执行thinJar任务：

        `~/projects/baeldung/spring-boot-gradle $ ./gradlew thinJar`

    4. Gradle和依赖关系：pom.xml

        在上一节的代码示例中，除了Thin Launcher（以及我们在先决条件部分已经看到的Boot和依赖管理插件）之外，我们还声明了Maven插件。

        这是因为，就像我们之前看到的Maven案例一样，该工件将包含并使用列举应用程序依赖关系的pom.xml文件。pom.xml文件是由一个叫thinPom的任务生成的，它是任何jar任务的隐性依赖。

        我们可以用一个专门的任务来定制生成的pom.xml文件。在这里，我们只是复制thin插件已经自动做的事情：

        ```groovy
        task createPom {
            def basePath = 'build/resources/main/META-INF/maven'
            doLast {
                pom {
                    withXml(dependencyManagement.pomConfigurer)
                }.writeTo("${basePath}/${project.group}/${project.name}/pom.xml")
            }
        }
        ```

        为了使用我们自定义的pom.xml文件，我们将上述任务添加到jar任务的依赖项中：

        `bootJar.dependsOn = [createPom]`

    5. Gradle和依赖关系：thin.properties

        我们还可以让Gradle生成一个thin.properties文件，而不是pom.xml，就像我们之前对Maven所做的那样。

        生成thin.properties文件的任务叫thinProperties，默认情况下不使用它。我们可以把它作为jar任务的一个依赖项：

        `bootJar.dependsOn = [thinProperties]`

5. 存储依赖关系

    瘦jar的全部意义在于避免将依赖性与应用程序捆绑在一起。然而，依赖性并没有神奇地消失，它们只是被存储在其他地方。

    特别是，瘦身启动器使用Maven基础架构来解决依赖关系，因此：

    - 它检查本地Maven仓库，默认位于~/.m2/repository，但也可以移动到其他地方；
    - 然后，它从Maven中心（或任何其他配置的仓库）下载缺失的依赖项；
    - 最后，它在本地仓库中缓存这些依赖，这样在我们下次运行应用程序时就不必再下载了。

    当然，下载阶段是该过程中缓慢且容易出错的部分，因为它需要通过互联网访问Maven Central，或访问本地代理，而我们都知道这些东西通常是不可靠的。

    幸运的是，有多种方法可以将依赖项与应用程序一起部署，例如在预包装的容器中进行云部署。

    1. 运行应用程序进行预热

        缓存依赖关系的最简单方法是在目标环境中对应用程序进行热身运行。正如我们之前所见，这将导致依赖项被下载并缓存在本地Maven仓库中。如果我们运行一个以上的应用程序，仓库最终会包含所有的依赖项，不会出现重复。

        由于运行应用程序会产生不必要的副作用，我们也可以执行 "dry run"，只解析和下载依赖项，不运行任何用户代码：

        `$ java -Dthin.dryrun=true -jar my-app-1.0.jar`

        注意，按照Spring Boot的惯例，我们也可以通过应用程序的-thin.dryrun命令行参数或THIN_DRYRUN系统属性来设置-Dthin.dryrun属性。除了false以外的任何值都会指示Thin Launcher执行干运行。

    2. 在构建过程中打包依赖项

        另一个选择是在构建过程中收集依赖项，而不把它们捆绑在JAR中。然后，我们可以把它们作为部署程序的一部分复制到目标环境中。

        这通常比较简单，因为没有必要在目标环境中运行应用程序。然而，如果我们要部署多个应用程序，我们将不得不手动或用脚本合并它们的依赖关系。

        Thin Plugin for Maven和Gradle在构建过程中打包依赖项的格式与Maven本地仓库相同：

        ```txt
        root/
            repository/
                com/
                net/
                org/
                ...
        ```

        事实上，我们可以在运行时用 thin.root 属性将使用 Thin Launcher 的应用程序指向任何此类目录（包括本地 Maven 仓库）：

        `$ java -jar my-app-1.0.jar --thin.root=my-app/deps`

        我们还可以安全地合并多个这样的目录，把它们一个一个地复制过来，从而获得一个包含所有必要依赖的Maven资源库。

    3. 用Maven打包依赖项

        为了让Maven为我们打包依赖项，我们使用spring-boot-thin-maven-plugin的解析目标。我们可以在pom.xml中手动或自动调用它：

        ```xml
        <plugin>
            <groupId>org.springframework.boot.experimental</groupId>
            <artifactId>spring-boot-thin-maven-plugin</artifactId>
            <version>${thin.version}</version>
            <executions>
                <execution>
                <!-- Download the dependencies at build time -->
                <id>resolve</id>
                <goals>
                    <goal>resolve</goal>
                </goals>
                <inherited>false</inherited>
                </execution>
            </executions>
        </plugin>
        ```

        构建项目后，我们会发现一个目录target/thin/root/，其结构与我们在上一节讨论的一样。

    4. 用Gradle打包依赖项

        如果我们使用Gradle和thin-launcher插件，我们有一个thinResolve任务可用。该任务将在build/thin/root/目录下保存应用程序及其依赖项，与上一节的Maven插件类似：

        `$ gradlew thinResolve`

6. 结论和进一步阅读

    在本文中，我们已经了解了如何制作我们的瘦肉精。我们还看到了如何使用Maven基础架构来下载和存储其依赖关系。

    瘦身启动器的主页上还有一些针对云部署到Heroku等场景的HOW-TO指南，以及支持的命令行参数的完整列表。
