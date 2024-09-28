# [将Spring Boot WAR部署到Tomcat服务器上](https://www.baeldung.com/spring-boot-war-tomcat-deploy)

1. 简介

    [Spring Boot](https://projects.spring.io/spring-boot/)是一个约定俗成的[配置框架](https://en.wikipedia.org/wiki/Convention_over_configuration)，它允许我们为Spring项目创建一个生产就绪的设置，而Tomcat是最流行的Java Servlet容器之一。

    默认情况下，Spring Boot构建了一个独立的Java应用程序，可以作为桌面应用程序运行，也可以配置为系统服务，但有些环境下我们无法安装新的服务或手动运行应用程序。

    与独立的应用程序相比，Tomcat被安装为一个服务，可以在同一个应用程序中管理多个应用程序，避免了为每个应用程序进行特定的设置。

    在本教程中，我们将创建一个简单的Spring Boot应用程序，并使其在Tomcat内工作。

2. 设置一个Spring Boot应用程序

    让我们使用其中一个可用的启动模板来设置一个简单的Spring Boot网络应用：

    ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <relativePath/> 
    </parent> 
    <dependencies>
        <dependency> 
            <groupId>org.springframework.boot</groupId> 
            <artifactId>spring-boot-starter-web</artifactId> 
        </dependency> 
    </dependencies>
    ```

    除了标准的@SpringBootApplication之外，不需要额外的配置，因为Spring Boot已经处理了默认设置。

    然后我们将添加一个简单的REST EndPoint，为我们返回一些有效的内容：

    springbootsimple/TomcatController.java

    最后，我们将用mvn spring-boot:run执行该应用程序，并在<http://localhost:8080/hello>，启动浏览器检查结果。

3. 创建一个Spring Boot WAR

    Servlet容器期望应用程序满足一些合同才能被部署。对于Tomcat来说，合同是[Servlet API 3.0](https://tomcat.apache.org/tomcat-8.0-doc/servletapi/index.html)。

    为了让我们的应用程序满足这一契约，我们必须对源代码进行一些小的修改。

    首先，我们需要打包一个WAR应用程序而不是JAR。为此，我们将修改pom.xml，内容如下：

    `<packaging>war</packaging>`

    接下来，我们将修改最终的WAR文件名以避免包括版本号：

    `<finalName>${artifactId}</finalName>`

    然后我们将添加Tomcat的依赖性：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
    ```

    最后，我们将通过实现SpringBootServletInitializer接口来初始化Tomcat所需的Servlet上下文：

    springbootsimple/SpringBootTomcatApplication.java

    为了建立我们的Tomcat可部署的WAR应用程序，我们将执行mvn clean包。之后，我们的WAR文件会在target/spring-boot-deployment.war生成（假设Maven的artifactId是 "spring-boot-deployment"）。

    我们应该考虑到，这个新设置使我们的Spring Boot应用程序成为一个非独立的应用程序（如果我们想让它再次以独立模式工作，我们可以从tomcat依赖中删除 provided scope）。

    > 注意：spring-boot-starter-tomcat 配置不影响直接运行 SpringBootApplication （前提是执行过 mvn compile）。

4. 将WAR部署到Tomcat

    为了让我们的WAR文件在Tomcat中部署和运行，我们需要完成以下步骤：

    - 下载[Apache Tomcat](https://tomcat.apache.org/download-90.cgi)，并将其解包到Tomcat文件夹中
    - 将我们的WAR文件从target/spring-boot-deployment.war复制到tomcat/webapps/文件夹中。
    - 从一个终端，导航到tomcat/bin文件夹并执行
    - catalina.bat run (在Windows上)
    - catalina.sh运行（在基于Unix的系统上）。
    - 转到 <http://localhost:8080/spring-boot-deployment/hello>

    这是一个快速的Tomcat设置，所以请查看Tomcat[安装指南](https://www.baeldung.com/tomcat)，以获得完整的设置指南。还有其他的方法可以[将WAR文件部署到Tomcat](https://www.baeldung.com/tomcat-deploy-war)。

5. 总结

    在这篇简短的文章中，我们创建了一个简单的Spring Boot应用程序，并把它变成了一个可在Tomcat服务器上部署的有效WAR应用程序。
