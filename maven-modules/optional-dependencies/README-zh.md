# Maven中的可选依赖性

1. 概述

    本教程将介绍Maven的`<optional>`标签，以及我们如何利用它来缩小Maven项目的工件（如WAR、EAR或JAR）的大小和范围。
    如需了解Maven的最新情况，请查看我们的[综合指南](https://www.baeldung.com/maven)。
2. 什么是`<optional>`？

    有时我们会创建一个Maven项目，作为其他Maven项目的依赖项。在处理这样的项目时，可能需要包含一个或多个依赖项，而这些依赖项只对该项目的某个子集的功能有用。
    如果最终用户不使用该功能子集，该项目仍会顺便拉入这些依赖项。这不必要地扩大了用户的项目规模，甚至可能引入与其他项目依赖的冲突的依赖版本。
    理想情况下，我们应该将项目的功能子集分割成自己的模块，从而不对项目的其他部分造成污染。然而，这并不总是实用的。
    为了将这些特殊依赖从主项目中排除，我们可以对它们应用Maven的`<optional>`标签。这迫使任何想使用这些依赖的用户明确声明它们。然而，它并不能将这些依赖项强加到不需要它们的项目中。
3. 如何使用`<optional>`？

    我们将看到，我们可以把`<optional>`元素的值设为true，使任何Maven依赖都是可选的。
    假设我们有以下的项目pom：

    ```xml
    <project>
        ...
        <artifactId>project-with-optionals</artifactId>
        ...
        <dependencies>
            <dependency>
                <groupId>com.baeldung</groupId>
                <artifactId>optional-project</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <optional>true</optional>
            </dependency>
        </dependencies>
    </project>
    ```

    在这个例子中，尽管 optional-project 被标记为可选，但它仍然是 project-with-optionals 的可用依赖，就像 `<optional>` 标签从未出现过一样。
    为了看到 `<optional>` 标签的效果，我们需要创建一个依赖 project-with-optionals 的新项目：

    ```xml
    <project>
        ...
        <artifactId>main-project</artifactId>
        ...
        <dependencies>
            <dependency>
                <groupId>com.baeldung</groupId>
                <artifactId>project-with-optionals</artifactId>
                <version>0.0.1-SNAPSHOT</version>
            </dependency>
        </dependencies>
    </project>
    ```

    现在，如果我们试图从main-project中引用optional-project，我们会发现optional-project并不存在。这是因为`<optional>`标签阻止了它被过渡性地包含。
    如果我们发现在我们的主项目中需要optional-project，我们只需要把它声明为一个依赖关系。
4. 总结

    本文中，我们研究了Maven的`<optional>`标签。使用该标签的主要好处是，它可以减少项目的大小，并有助于防止版本冲突。我们还看到，该标签并不影响使用它的项目。

## Relevant Articles

- [x] [Optional Dependency in Maven](https://www.baeldung.com/maven-optional-dependency)

## Code

本文的源代码可以在[Github](https://github.com/eugenp/tutorials/tree/master/maven-modules/optional-dependencies)上找到。
