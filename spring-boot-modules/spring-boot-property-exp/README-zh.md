# 使用Spring Boot的自动属性扩展

1. 概述

    在这篇文章中，我们将探讨Spring通过Maven和Gradle构建方法提供的属性扩展机制。
2. Maven

    1. 默认配置

        对于使用spring-boot-starter-parent的Maven项目，不需要额外的配置来利用属性扩展

        现在我们可以使用@...@占位符扩展我们项目的属性。下面是一个例子，说明我们如何将Maven中的项目版本保存到我们的属性中：

        ```properties
        expanded.project.version=@project.version@
        expanded.project.property=@custom.property@
        ```

        我们只能在符合这些模式的配置文件中使用这些扩展：

        - `**/application*.yml`
        - `**/application*.yaml`
        - `**/application*.properties`
    2. 手动配置

        在没有spring-boot-starter-parent parent的情况下，我们需要手动配置这个过滤和扩展。我们需要在pom.xml文件的`<build>`部分包含资源元素：

        ```xml
        <resources>
            <resource>
                <directory>${basedir}/src/main/resources</directory>
                <filtering>true</filtering>
                <includes>
                    <include>**/application*.yml</include>
                    <include>**/application*.yaml</include>
                    <include>**/application*.properties</include>
                </includes>
            </resource>
        </resources>     
        ```

        并在`<plugins>`中：

        ```xml
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>2.7</version>
            <configuration>
                <delimiters>
                    <delimiter>@</delimiter>
                </delimiters>
                <useDefaultDelimiters>false</useDefaultDelimiters>
            </configuration>
        </plugin>
        ```

        在需要使用`${variable.name}`类型的标准占位符的情况下，我们需要将 useDefaultDelimeters (使用默认分隔符)设置为true，你的application.properties将看起来像这样：

        ```properties
        expanded.project.version=${project.version}
        expanded.project.property=${custom.property}
        ```

        - [x] **ERROR** org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'propertyLoggerBean': Injection of autowired dependencies failed; nested exception is java.lang.IllegalArgumentException: Could not resolve placeholder 'project.version' in value "${project.version}"

            当 useDefaultDelimeters = false 引起

3. Gradle

    1. 标准Gradle解决方案

        Spring Boot文档中的Gradle[解决方案](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-properties-and-configuration.html)与Maven的属性过滤和扩展并非100%兼容。
        为了让我们能够使用属性扩展机制，我们需要在build.gradle中加入以下代码：

        ```groovy
        processResources {
            expand(project.properties)
        }
        ```

        这是一个有限的解决方案，与Maven的默认配置有以下区别：

        - 不支持带圆点的属性（如user.name）。Gradle将圆点理解为对象属性分隔符。
        - 过滤所有的资源文件，而不仅仅是一组特定的配置文件
        - 使用默认的美元符号占位符${...}，因此与标准的Spring占位符冲突。
    2. 与Maven兼容的解决方案

        为了复制标准的Maven解决方案并利用@...@风格的占位符，我们需要在build.gradle中添加以下代码：

        ```groovy
        import org.apache.tools.ant.filters.ReplaceTokens
        processResources {
            with copySpec {
                from 'src/main/resources'
                include '**/application*.yml'
                include '**/application*.yaml'
                include '**/application*.properties'
                project.properties.findAll().each {
                prop ->
                    if (prop.value != null) {
                        filter(ReplaceTokens, tokens: [ (prop.key): prop.value])
                        filter(ReplaceTokens, tokens: [ ('project.' + prop.key): prop.value])
                    }
                }
            }
        }
        ```

        这将解决项目的所有属性。我们仍然不能在build.gradle中定义带点的属性（如`user.name`），但现在我们可以使用gradle.properties文件，以标准的Java属性格式定义属性，它也支持带点的属性（如database.url）。
        这种构建方式只过滤项目配置文件，不过滤所有资源，而且与Maven方案100%兼容。
4. 总结

    在这个快速教程中，我们看到了如何使用Maven和Gradle构建方法自动扩展Spring Boot属性，以及如何从一个方法轻松迁移到另一个方法。

## Relevant Articles

- [x] [Automatic Property Expansion with Spring Boot](https://www.baeldung.com/spring-boot-auto-property-expansion)

## Code

完整的源码实例可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-property-exp)上找到。

有两个子模块：

1. property-exp-default-config

    该模块包含标准的Maven Spring Boot构建和与Maven兼容的Gradle构建。

    为了执行Maven示例，请运行以下命令：

    `mvn spring-boot:run`

    要执行Gradle的例子：

    `gradle bootRun`

    - [ ] **Error** A problem occurred evaluating root project 'property-exp-default'.
      `> Plugin with id 'maven' not found.`
    - [ ] **Error** Failed to apply plugin 'org.springframework.boot'.
      `> Configuration with name 'runtime' not found.`

2. property-exp-custom-config

    这个项目没有使用标准的Spring Boot父体，是手动配置的。运行以下命令：

    `mvn spring-boot:run`
