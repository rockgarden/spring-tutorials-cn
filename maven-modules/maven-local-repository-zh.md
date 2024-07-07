# Maven本地仓库在哪里？

我们将重点讨论Maven在本地存储所有本地依赖项的地方，也就是Maven本地仓库(Maven local repository)。

简单地说，当我们运行Maven构建时，项目的依赖项（jars、插件jars、其他工件）都会存储在本地，以便日后使用。

另外请记住，除了这种类型的本地仓库，Maven还支持三种类型的仓库。

- 本地Local--本地开发机器上的文件夹位置
- 中央Central--由Maven社区提供的资源库
- 远程Remote--组织拥有的自定义资源库

1. 本地仓库

    Maven的本地仓库是本地机器上的一个目录，储存了所有项目工件。

    当我们执行Maven构建时，Maven会自动将所有依赖的jars下载到本地仓库。通常情况下，这个目录被命名为.m2。

    以下是基于操作系统的默认本地资源库的位置。

    `Windows: C:\Users\<User_Name>\.m2`

    `Linux: /home/<User_Name>/.m2`

    `Mac: /Users/<user_name>/.m2`

    而对于Linux和Mac，我们可以用简短的形式来写。

    `~/.m2`

2. 在 settings.xml 中自定义本地版本库

    如果 repo 没有出现在这个默认位置，很可能是因为一些预先存在的配置。

    该配置文件位于Maven安装目录下一个名为conf的文件夹，名称为settings.xml。

    以下是决定我们丢失的本地 repo 的位置的相关配置。

    ```xml
    <settings>
        <localRepository>C:/maven_repository</localRepository>
    ```

    这基本上就是我们改变本地版本库位置的方法。当然，如果我们改变了这个位置，就不会再在默认位置找到版本库了。

    存储在早期位置的文件不会被自动移动。

3. 通过命令行传递本地版本库的位置

    除了在Maven的settings.xml中设置自定义本地仓库外，mvn命令还支持maven.repo.local属性，它允许我们将本地仓库位置作为命令行参数传递。

    `mvn -Dmaven.repo.local=/my/local/repository/path clean install`

    这样一来，我们就不必改变Maven的settings.xml。

## Relevant Articles

- [x] [Where is the Maven Local Repository?](https://www.baeldung.com/maven-local-repository)
