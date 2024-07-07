# Maven插件

## Maven部署插件

1. 概述

    本教程介绍了部署插件，它是Maven构建工具的核心插件之一。

2. 插件目标

    我们在部署阶段使用deploy插件，将工件推送到远程仓库，与其他开发者分享。

    除了工件本身，这个插件还能确保所有相关的信息，如POMs、元数据或哈希值，都是正确的。

    部署插件是在超级POM中指定的，因此没有必要在POM中添加这个插件。

    这个插件最引人注目的目标是deploy，默认与deploy阶段绑定。[本教程](/MavenToNexus.md#maven部署到nexus)详细介绍了deploy插件，因此我们不再赘述。

    部署插件还有一个名为deploy-file的目标，在远程仓库中部署一个工件。不过这个目标并不常用。

3. 总结

    这篇文章对deploy插件进行了简单的描述。

    我们可以在Maven网站上找到关于该插件的[更多信息](https://maven.apache.org/plugins/maven-deploy-plugin/)。

## Maven网站插件

1. 概述

    本教程介绍网站插件，它是Maven构建工具的核心插件之一。

2. 插件目标

    Maven site生命周期有两个阶段默认与site插件的目标绑定：site阶段与站点site绑定，site-deploy阶段与deploy目标绑定。

    下面是这些目标的描述：

    - site - 为单个项目生成一个站点；生成的站点只显示POM中指定的工件的信息
    - deploy - 将生成的站点部署到POM的distributionManagement元素中指定的URL上。

    除了site和deploy，site插件还有其他几个目标，以定制生成文件的内容和控制部署过程。

3. 目标执行

    我们可以使用这个插件，而不需要把它添加到POM中，因为超级POM已经包含了它。

    要生成一个站点，只需运行`mvn site:site`或`mvn site`。

    要在本地机器上查看生成的站点，运行`mvn site:run`。该命令将把网站部署到地址为localhost:8080的Jetty网络服务器上。

    这个插件的运行目标并没有隐含地与网站生命周期的某个阶段绑定，因此我们需要直接调用它。

    如果我们想停止服务器，我们可以简单地按Ctrl + C。

4. 总结

    这篇文章涵盖了网站插件和如何执行其目标。

    我们可以在Maven网站上找到更多关于这个[插件的信息](https://maven.apache.org/plugins/maven-site-plugin/)。

## Relevant Articles

- [x] [The Maven Deploy Plugin](https://www.baeldung.com/maven-deploy-plugin)
- [x] [The Maven Site Plugin](https://www.baeldung.com/maven-site-plugin)
