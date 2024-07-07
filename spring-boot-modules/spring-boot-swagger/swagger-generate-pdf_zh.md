# [从 Swagger API 文档生成 PDF](https://www.baeldung.com/swagger-generate-pdf)

1. 概述

    在本教程中，我们将学习从 Swagger API 文档生成 PDF 文件的不同方法。要熟悉 Swagger，请参阅我们的教程：[使用 Spring REST API 设置 Swagger 2](https://www.baeldung.com/spring-rest-openapi-documentation)。

2. 使用 Maven 插件生成 PDF

    从 Swagger API 文档生成 PDF 文件的第一个解决方案是基于一组 Maven 插件。使用这种方法，我们将在构建 Java 项目时获得 PDF 文件。

    生成所需的 PDF 文件的步骤包括在 Maven 构建过程中按照特定顺序应用多个插件。这些插件应配置为选取资源，并将前一阶段的输出传播为下一阶段的输入。让我们来看看它们各自是如何工作的。

    1. swagger-maven-plugin 插件

        我们要使用的第一个插件是 swagger-maven-plugin。该插件为我们的 REST API 生成 swagger.json 文件：

        ```xml
        <plugin>
            <groupId>com.github.kongchen</groupId>
            <artifactId>swagger-maven-plugin</artifactId>
            <version>3.1.3</version>
            <configuration>
                <apiSources>
                    <apiSource>
                        <springmvc>false</springmvc>
                        <locations>com.baeldung.swagger2pdf.controller.UserController</locations>
                        <basePath>/api</basePath>
                        <info>
                            <title>DEMO REST API</title>
                            <description>A simple DEMO project for REST API documentation</description>
                            <version>v1</version>
                        </info>
                        <swaggerDirectory>${project.build.directory}/api</swaggerDirectory>
                        <attachSwaggerArtifact>true</attachSwaggerArtifact>
                    </apiSource>
                </apiSources>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ```

        我们需要指向 API 的位置，并定义插件在构建过程中生成 swagger.json 文件的阶段。在执行标签中，我们指定插件应在打包阶段执行此操作。

    2. swagger2markup-maven-plugin 插件

        我们需要的第二个插件是 swagger2markup-maven-plugin。它将前一个插件输出的 swagger.json 作为输入，生成 [Asciidoc](https://www.baeldung.com/asciidoctor)：

        ```xml
        <plugin>
            <groupId>io.github.robwin</groupId>
            <artifactId>swagger2markup-maven-plugin</artifactId>
            <version>0.9.3</version>
            <configuration>
                <inputDirectory>${project.build.directory}/api</inputDirectory>
                <outputDirectory>${generated.asciidoc.directory}</outputDirectory>
                <markupLanguage>asciidoc</markupLanguage>
            </configuration>
            <executions>
                <execution>
                <phase>package</phase>
                    <goals>
                        <goal>process-swagger</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ```

        在这里，我们指定了 inputDirectory 和 outputDirectory 标记。同样，我们将把软件包定义为生成 REST API 的 Asciidoc 的构建阶段。

    3. asciidoctor-maven-plugin 插件

        第三个也是最后一个插件是 asciidoctor-maven-plugin。作为三个插件中的最后一个，它可以从 [Asciidoc](https://www.baeldung.com/asciidoctor) 生成 PDF 文件：

        ```xml
        <plugin>
            <groupId>io.github.robwin</groupId>
            <artifactId>swagger2markup-maven-plugin</artifactId>
            <version>0.9.3</version>
            <configuration>
                <inputDirectory>${project.build.directory}/api</inputDirectory>
                <outputDirectory>${generated.asciidoc.directory}</outputDirectory>
                <markupLanguage>asciidoc</markupLanguage>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>process-swagger</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ```

        我们提供上一阶段生成 Asciidoc 的位置。然后，我们定义了生成 PDF 文档的位置，并指定了生成 PDF 文档的阶段。我们再次使用软件包阶段。

3. 使用 SwDoc 生成 PDF

    Swagger to PDF 是一个在线工具，可在 [swdoc.org](https://www.swdoc.org/) 上获取，它可使用所提供的 swagger.json 规范将 API 文档生成 PDF 文件。它依赖 [Swagger2Markup](https://github.com/Swagger2Markup/swagger2markup-cli) 转换器和 AsciiDoctor。

    其原理与上一个解决方案类似。首先，Swagger2Markup 将 swagger.json 转换为 AsciiDoc 文件。然后，AsciiDoctor 将这些文件解析为文档模型并转换为 PDF 文件。

    该解决方案简单易用，如果我们已经有了 Swagger 2 API 文档，那么它就是一个不错的选择。

    我们可以通过两种方式生成 PDF

    - 提供指向 swagger.json 文件的 URL
    - 将 swagger.json 文件的内容粘贴到工具的文本框中

    我们将使用 Swagger 上公开的 PetStore API 文档。

    为此，我们将复制 JSON 文件并将其粘贴到文本框中。

    点击 "Generate" 按钮后，我们将得到 PDF 格式的文档。

4. 总结

    在这个简短的教程中，我们讨论了从 Swagger API 文档生成 PDF 的两种方法。

    第一种方法依赖于 Maven 插件。我们可以使用三个插件，在构建应用程序的同时生成 REST API 文档。

    第二种解决方案介绍了使用 SwDoc 在线工具生成 PDF 文档。我们可以通过 swagger.json 的链接生成文档，也可以将 JSON 文件内容粘贴到工具中。
