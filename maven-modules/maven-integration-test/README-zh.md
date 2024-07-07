# Apache Maven 集成测试

本模块包含关于用Maven和相关插件进行集成测试的文章。

## Maven Surefire插件的快速指南

1. 概述

    本教程演示了Maven构建工具的核心插件之一--surefire插件。

2. 插件目标

    我们可以使用surefire插件运行项目的测试。默认情况下，该插件会在target/surefire-reports目录下生成XML报告。

    这个插件只有一个目标，即测试。这个目标被绑定到默认构建生命周期的测试阶段，命令mvn test将执行它。

3. 配置

    surefire插件可以与测试框架JUnit和TestNG一起工作。无论我们使用哪个框架，surefire的行为都是一样的。

    默认情况下，surefire会自动包含所有名称以Test开头，或以Test、Tests或TestCase结尾的测试类。

    不过，我们可以使用排除和包括参数来改变这种配置：

    ```xml
    <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.22.2</version>
        <configuration>
            <excludes>
                <exclude>DataTest.java</exclude>
            </excludes>
            <includes>
                <include>DataCheck.java</include>
            </includes>
        </configuration>
    </plugin>
    ```

    有了这个配置，DataCheck类中的测试用例就会被执行，而DataTest中的用例则不会被执行。

4. 总结

    在这篇快速的文章中，我们浏览了surefire插件，描述了它的唯一目标以及如何配置它。

### Relevant Articles

- [Integration Testing with Maven](https://www.baeldung.com/maven-integration-test)
- [Build a Jar with Maven and Ignore the Test Results](https://www.baeldung.com/maven-ignore-test-results)
- [x] [Quick Guide to the Maven Surefire Plugin](https://www.baeldung.com/maven-surefire-plugin)
- [The Maven Failsafe Plugin](https://www.baeldung.com/maven-failsafe-plugin)
- [Difference Between Maven Surefire and Failsafe Plugins](https://www.baeldung.com/maven-surefire-vs-failsafe)

## Code

一如既往，本教程的完整源代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/maven-modules/maven-integration-test)上找到。
