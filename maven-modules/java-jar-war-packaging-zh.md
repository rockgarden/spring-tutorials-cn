# JAR和WAR打包的区别

在本快速教程中，我们将重点讨论Java中JAR和WAR打包之间的差异。

首先，我们将分别定义每个打包选项。之后，我们将总结他们的差异。

1. JAR包

    简单地说，JAR或Java Archive是一种包文件格式。JAR文件具有.JAR扩展名，可能包含库libraries、资源resources和元数据metadata文件。

    本质上，它是一个压缩文件，包含.class文件的压缩版本以及编译的Java库和应用程序的资源。

    例如，这里有一个简单的JAR文件结构：

    ```txt
    META-INF/
        MANIFEST.MF
    com/
        baeldung/
            MyApplication.class
    ```

    [META-INF/MANIFEST.MF](https://www.baeldung.com/java-jar-executable-manifest-main-class)文件可能包含有关存档中存储的文件的其他元数据。

    我们可以使用JAR命令或Maven等工具[创建JAR文件](https://www.baeldung.com/java-create-jar)。

2. WAR包

    WAR代表Web应用程序档案或Web应用程序资源。这些归档文件具有.war扩展名，用于打包我们可以部署在任何Servlet/JSP容器上的web应用程序。

    下面是典型WAR文件结构的布局示例：

    ```txt
    META-INF/
        MANIFEST.MF
    WEB-INF/
        web.xml
        jsp/
            helloWorld.jsp
        classes/
            static/
            templates/
            application.properties
        lib/
            // *.jar files as libs
    ```

    在内部，它有一个META-INF目录，在MANIFEST.MF中保存有关web存档的有用信息。META-INF目录是私有的，无法从外部访问。

    另一方面，它还包含包含所有静态WEB资源的WEB-INF公共目录，包括HTML页面、图像和JS文件。此外，它还包含web.xml文件、servlet类和库。

    我们可以使用与构建JAR时相同的工具和命令来构建.war存档。

3. 关键区别

    那么，这两种归档类型之间的关键区别是什么？

    第一个也是最明显的区别是文件扩展名。jar具有.jar扩展名，而WAR文件具有.WAR扩展名。

    第二个主要区别是它们的目的和作用方式。JAR文件允许我们打包多个文件，以便将其用作库、插件或任何类型的应用程序。另一方面，WAR文件仅用于web应用程序。

    档案的结构也不同。我们可以创建具有任何所需结构的JAR。相反，WAR具有预定义的结构，包含WEB-INF和META-INF目录。

    最后，如果我们将JAR构建为可执行JAR而不使用其他软件，那么我们可以从命令行运行它。或者，我们可以将其用作library。相反，我们需要一个服务器来执行WAR。

## Relevant Articles

- [x] [Differences Between JAR and WAR Packaging](https://www.baeldung.com/java-jar-war-packaging)