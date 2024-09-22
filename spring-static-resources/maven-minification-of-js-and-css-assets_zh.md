# [用Maven对JS和CSS资产进行最小化处理](https://www.baeldung.com/maven-minification-of-js-and-css-assets)

本文介绍了如何在构建步骤中对Javascript和CSS资产进行最小化，并将生成的文件提供给Spring MVC。

我们将使用[YUI Compressor](https://yui.github.io/yuicompressor/)作为底层最小化库，并使用[YUI Compressor Maven插件](https://davidb.github.io/yuicompressor-maven-plugin/)将其集成到构建流程中。

> ==重要：YUI Compressor 不支持 ES6 已经停更废弃！！！==

1. Maven插件配置

    首先，我们需要声明我们将在pom.xml文件中使用压缩器插件并执行压缩目标。这将压缩src/main/webapp下的所有.js和.css文件，这样foo.js将被压缩为foo-min.js，myCss.css将被压缩为myCss-min.css。

    ```xml
    <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>yuicompressor-maven-plugin</artifactId>
        <version>1.5.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>compress</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```

    我们的 src/main/webapp 目录包含以下文件一些静态资源。

    ```log
    js/
    ├── foo.js
    ├── jquery-1.11.1.min.js
    resources/
    └── myCss.css
    ```

    现在当我们执行mvn clean package时，我们将在生成的WAR文件中拥有以下文件。

    ```log
    js/
    ├── foo.js
    ├── foo-min.js
    ├── jquery-1.11.1.min.js
    ├── jquery-1.11.1.min-min.js
    resources/
    ├── myCss.css
    └── myCss-min.css
    ```

2. 保持文件名不变

    在这个阶段，当我们执行mvn clean package时，foo-min.js和myCss-min.css被插件创建。由于我们在引用文件时最初使用了foo.js和myCss.css，我们的页面仍将使用原始的非minified文件，因为minified文件的名称与原始文件不同。

    为了防止同时出现foo.js/foo-min.js和myCss.css/myCss-min.css，并在不改变文件名的情况下将其最小化，我们需要用nosuffix选项对插件进行配置，如下所示。

    ```xml
    <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>yuicompressor-maven-plugin</artifactId>
        <version>1.5.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>compress</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <nosuffix>true</nosuffix>
        </configuration>
    </plugin>
    ```

    现在当我们执行mvn clean package时，我们将在生成的WAR文件中拥有以下文件。

    ```log
    js/
    ├── foo.js
    ├── jquery-1.11.1.min.js
    resources/
    └── myCss.css
    ```

3. WAR插件配置

    保持文件名不变有一个副作用。它使WAR插件用原始文件覆盖已减化的foo.js和myCss.css文件，所以我们在最终输出中没有文件的减化版本。 foo.js文件在减化前包含以下几行。

    ```js
    function testing() {
        alert("Testing");
    }
    ```

    当我们检查生成的WAR文件中foo.js文件的内容时，我们看到它有原始的内容，而不是减化后的内容。为了解决这个问题，我们需要为压缩器插件指定一个webappDirectory，并在WAR插件配置中引用它。

    ```xml
    <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>yuicompressor-maven-plugin</artifactId>
        <version>1.5.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>compress</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <nosuffix>true</nosuffix>
            <webappDirectory>${project.build.directory}/min</webappDirectory>
        </configuration>
    </plugin>
    <plugin>
        <artifactId>maven-war-plugin</artifactId>
        <configuration>
            <webResources>
                <resource>
                    <directory>${project.build.directory}/min</directory>
                </resource>
            </webResources>
        </configuration>
    </plugin>
    ```

    在这里，我们已经指定了min目录作为最小化文件的输出目录，并配置了WAR插件，使其包括在最终输出中。

    现在我们在生成的WAR文件中看到了已被minified的文件，其原始文件名为foo.js和myCss.css。我们可以检查foo.js，看看它现在有以下的最小化内容。

    `function testing(){alert("Testing")};`

4. 排除已经最小化的文件

    第三方的Javascript和CSS库可能有最小化的版本可供下载。如果你碰巧在你的项目中使用了其中的一个，你不需要再次处理它们。

    包括已经最小化的文件会在构建项目时产生警告信息。

    例如，jquery-1.11.1.min.js是一个已经最小化的Javascript文件，它在构建过程中会产生类似以下的警告信息。

    ```log
    [WARNING] .../src/main/webapp/js/jquery-1.11.1.min.js [-1:-1]:
    Using 'eval' is not recommended. Moreover, using 'eval' reduces the level of compression!
    execScript||function(b){a. ---> eval <--- .call(a,b);})
    [WARNING] ...jquery-1.11.1.min.js:line -1:column -1:
    Using 'eval' is not recommended. Moreover, using 'eval' reduces the level of compression!
    execScript||function(b){a. ---> eval <--- .call(a,b);})
    ```

    要把已经被粉碎的文件排除在进程之外，请用排除选项配置压缩器插件，如下所示。

    ```xml
    <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>yuicompressor-maven-plugin</artifactId>
        <version>1.5.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>compress</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <nosuffix>true</nosuffix>
            <webappDirectory>${project.build.directory}/min</webappDirectory>
            <excludes>
                <exclude>**/*.min.js</exclude>
            </excludes>
        </configuration>
    </plugin>
    ```

    这将排除所有文件名以min.js结尾的目录下的所有文件。现在执行mvn clean package不会产生警告信息，而且构建时也不会尝试对已经被粉碎的文件进行粉碎。

5. 总结

    在本文中，我们介绍了一种将Javascript和CSS文件的最小化整合到Maven工作流中的好方法。要在Spring MVC应用中提供这些静态资产，请参阅我们的Spring服务静态资源一文。

## Minify Maven Plugin

yuicompressor-maven-plugin插件，随着es6的到来，已经变得不可用了。网上经过众多搜索后，发现可以使用Minify Maven Plugin/closure-compiler-maven-plugin插件使maven能支持es6的压缩。

Minify Maven Plugin插件集成了yuicompressor 与google 的closure-compiler编译器，可以压缩css/js，但是缺点使用的closure-compiler是2016年版本，压缩后的目标文件不能生产ECMASCRIPT2015（ES6）版本，最高只能生成ES5。

<https://github.com/samaxes/minify-maven-plugin>

<https://samaxes.github.io/minify-maven-plugin/>

closure-compiler-maven-plugin这个插件是Minify Maven Plugin的变种，在其基础上演变而来的。可以支持压缩后的js格式为ES6，但不能支持css的压缩，configuration节点格式与Minify Maven Plugin插件有所不同。

<https://github.com/blutorange/closure-compiler-maven-plugin>

```xml
<plugins>
    <plugin>
        <!-- 此插件只能压缩js，不能压缩css；如果需要压缩css，可以考虑使用yuicompressor -->
        <groupId>com.github.blutorange</groupId>
        <artifactId>closure-compiler-maven-plugin</artifactId>
        <version>2.25.0</version>
        <configuration>
            <!-- TODO: Bug-配置后将导致目标文件路径固化为/js -->
            <!-- <baseSourceDir>${project.basedir}/src/main/webapp/resource</baseSourceDir> -->
            <!-- <baseTargetDir>${project.build.directory}/min</baseTargetDir> -->
        </configuration>
        <executions>
            <execution>
                <id>default-minify</id>
                <configuration>
                    <encoding>UTF-8</encoding>
                    <!-- 压缩webapp目录下的所有js文件，但是排除*.min.js文件； -->
                    <sourceDir>./</sourceDir>
                    <includes>**/*.js</includes>
                    <excludes>**/*.min.js</excludes> 
                    <!-- 输出目录为${project.build.directory}/${project.build.finalName}下的目录，不配置时默认为js. -->
                    <targetDir>./</targetDir>
                    <!-- 跳过js文件合并 -->
                    <skipMerge>true</skipMerge>
                    <!-- js文件输出格式，默认为#{path}/#{basename}.min.#{extension} -->
                    <outputFilename>#{path}/#{basename}.#{extension}</outputFilename>
                    <!-- js源文件版本，直接填最高即可，都能向下兼容 默认 ECMASCRIPT_NEXT -->
                    <closureLanguageIn>ECMASCRIPT_NEXT</closureLanguageIn>
                    <!-- js压缩后的文件格式版本，注意与浏览器的兼容性 默认 ES6/ECMASCRIPT_2015 -->
                    <closureLanguageOut>ECMASCRIPT_2015</closureLanguageOut>
                    <!-- 使用closure压缩时的压缩级别，默认 SIMPLE_OPTIMIZATIONS。可选值： -->
                    <!-- WHITESPACE_ONLY：只压缩空格和转换一些特殊符号 -->
                    <!-- SIMPLE_OPTIMIZATIONS：简单的压缩 -->
                    <!-- ADVANCED_OPTIMIZATIONS：高级压缩，此压缩方式下可能会将引用的第3方框架/其它js文件中的的方法名称修改，导致js报错；慎用。 -->
                    <closureCompilationLevel>SIMPLE_OPTIMIZATIONS</closureCompilationLevel>
                </configuration>
                <goals>
                    <goal>minify</goal>
                </goals>
                <phase>generate-resources</phase>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <configuration>
            <!-- 如果不增加此配置 src/main/webapp 下面的内容 会重新复制到target输出目录 覆盖掉编译后的内容 这样编译的还是未压缩过的内容，增加上此过滤 打war包就不会内容覆盖 -->
            <warSourceExcludes>
                <![CDATA[
                    %regex[^.+(?:(?<!(?:-|\.)min)\.js)], %regex[^.+(?:(?<!(?:-|\.)min)\.css)]
                ]]>
            </warSourceExcludes>
        </configuration>
    </plugin>
<plugins>
```

参考：<https://blutorange.github.io/closure-compiler-maven-plugin/minify-mojo.html>

<https://github.com/tc39/proposals/blob/main/finished-proposals.md>
