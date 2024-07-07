# 参考pom.xml中的环境变量

在本快速教程中，我们将了解如何从Maven的pom.xml中读取环境变量来定制构建过程。

要从pom.xml中引用环境变量，我们可以使用${env.VARIABLE_NAME}语法。

例如，让我们在构建过程中使用它来[外部化Java版本](https://www.baeldung.com/maven-java-version)。

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>${env.JAVA_VERSION}</source>
                <target>${env.JAVA_VERSION}</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

我们应该记得通过环境变量传递Java版本信息。如果我们没有这样做，那么我们将无法构建项目。

要针对这样的构建文件运行Maven目标或阶段，我们应首先导出环境变量。比如说

```bash
export JAVA_VERSION=9
mvn clean package
```

在Windows上，我们应该使用 "set VAR=value" 语法来导出环境变量。

为了在缺少JAVA_VERSION环境变量时提供一个默认值，我们可以使用Maven配置文件。

```xml
<profiles>
    <profile>
        <id>default-java</id>
        <activation>
            <property>
                <name>!env.JAVA_VERSION</name>
            </property>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.8.1</version>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

如上所示，我们正在创建一个配置文件，并仅在 JAVA_VERSION 环境变量缺失的情况下使其处于激活状态 - !env.JAVA_VERSION 部分。如果发生这种情况，那么这个新的插件定义将覆盖现有的插件。

## Relevant Articles

- [x] [Refer to Environment Variables in pom.xml](https://www.baeldung.com/maven-env-variables)
