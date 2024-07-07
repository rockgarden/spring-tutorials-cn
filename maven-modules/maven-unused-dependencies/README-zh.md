# 查找未使用的Maven依赖项

1. 概述

    使用Maven管理项目的依赖关系时，我们可能会失去对应用程序中使用的依赖关系的跟踪。

    在这个简短的教程中，我们将介绍如何使用Maven依赖性插件，这个插件可以帮助我们找到项目中未使用的依赖性。

2. 项目设置

    我们首先添加几个依赖项，slf4j-api（我们将使用的依赖项）和mon-collections（我们不会使用的依赖项）：

    ```xml
        <dependencies>
        <dependency>
            <groupId>commons-collections</groupId>
            <artifactId>commons-collections</artifactId>
            <version>3.2.2</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        </dependencies>
    ```

    我们可以访问Maven的依赖性插件(maven-dependency-plugin)，而不必在pom中指定它。在任何情况下，我们都可以使用pom.xml定义来指定版本，必要时还可以指定一些属性：

    ```xml
    <build>
    <plugins>
        <plugin>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.2</version>
        </plugin>
    </plugins>
    </build>
    ```

3. 代码示例

    现在我们已经设置好了项目，让我们写一行代码，使用我们之前定义的一个依赖项：

    mavendependencyplugin/UnusedDependenciesExample.java: getLogger()

    来自Slf4J库的LoggerFactory在一个方法中被返回，但没有使用到通用集合库，因此它是一个可以被删除的候选者。

4. 查找未使用的依赖项

    使用Maven的依赖性插件，我们可以找到项目中未使用的依赖性。为此，我们调用了该插件的分析目标：

    ```log
    $ mvn dependency:analyze

    [INFO] --- maven-dependency-plugin:3.1.1:analyze (default-cli) @ maven-unused-dependencies ---
    [WARNING] Unused declared dependencies found:
    [WARNING]    commons-collections:commons-collections:jar:3.2.2:compile
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 1.225 s
    [INFO] Finished at: 2020-04-01T16:10:25-04:00
    [INFO] ------------------------------------------------------------------------
    ```

    对于我们项目中没有使用的每一个依赖，Maven都在分析报告中发出了警告。

5. 将依赖关系指定为已使用

    根据项目的性质，有时我们可能需要在运行时加载类，比如说在面向插件的项目中。

    由于依赖关系在编译时没有被使用，所以maven-dependency-plugin会发出警告，称某个依赖关系没有被使用，而事实上它是被使用的。为此，我们可以强制并指示该插件正在使用某个库。

    我们通过在 usedDependencies 属性中列出依赖关系来做到这一点：

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <configuration>
            <usedDependencies>
                <dependency>commons-collections:commons-collections</dependency>
            </usedDependencies>
        </configuration>
    </plugin>
    ```

    再次运行分析目标，我们看到报告中不再考虑未使用的依赖关系了。

6. 总结

    在这个简短的教程中，我们学习了如何找到未使用的maven依赖。定期检查未使用的依赖关系是个好办法，因为它能提高项目的可维护性并减少库的大小。

## Relevant Articles

- [x] [Find Unused Maven Dependencies](https://www.baeldung.com/maven-unused-dependencies)

## Code

像往常一样，所有例子的完整源代码都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/maven-modules/maven-unused-dependencies)上找到。
