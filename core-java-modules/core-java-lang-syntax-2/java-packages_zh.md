# [Java包指南](https://www.baeldung.com/java-packages)

1. 简介

    在这个快速教程中，我们将介绍Java中包的基础知识。我们将看到如何创建包和访问我们放在包里的类型。

    我们还将讨论命名规则以及它与底层目录结构的关系。

    最后，我们将编译和运行我们打包的Java类。

2. Java包的概述

    在Java中，我们使用包来分组相关的类、接口和子包。

    这样做的主要好处是。

    - 使得相关的类型更容易找到--包通常包含逻辑上相关的类型
    - 避免命名冲突--包将帮助我们唯一地识别一个类；例如，我们可以有一个com.baeldung.Application，以及com.example.Application类
    - 控制访问--我们可以通过结合包和访问修饰符来控制类型的可见性和访问。

    接下来，让我们看看我们如何创建和使用Java包。

3. 创建一个包

    要创建一个包，我们必须使用包声明，把它作为文件中的第一行代码加入。

    让我们把一个类型放在一个名为com.baeldung.packages的包中。

    package com.baeldung.packages;

    强烈建议将每个新类型放在一个包中。如果我们定义了类型却不把它们放在一个包里，它们就会进入默认的或未命名的包里。使用默认包会有一些缺点。

    - 我们失去了拥有包结构的好处，我们不能拥有子包
    - 我们不能从其他包中导入默认包中的类型
    - [受保护的和包专用](https://www.baeldung.com/java-access-modifiers)的访问范围将毫无意义

    正如[Java语言规范](https://docs.oracle.com/javase/specs/jls/se14/html/jls-7.html#jls-7.4.2)所述，未命名包由Java SE平台提供，主要是为了在开发小型或临时应用程序或刚开始开发时提供方便。

    因此，我们应该避免在现实世界的应用中使用未命名的或默认的包。

    1. 命名规则

        为了避免同名的包，我们遵循一些命名约定。

        - 我们用所有的小写字母来定义我们的包名
        - 包的名字是以句号为界限的
        - 名称也由创建它们的公司或组织决定

        要根据一个组织来确定包的名称，我们通常会从颠倒公司的URL开始。之后，命名规则由公司定义，可能包括部门名称和项目名称。

        例如，要把www.baeldung.com，让我们反过来做一个包。

        com.baeldung

        然后我们可以进一步定义其中的子包，如com.baeldung.packages或com.baeldung.packages.domain。

    2. 目录结构

        Java中的包与目录结构相对应。

        每个包和子包都有自己的目录。因此，对于com.baeldung.packages这个包，我们应该有一个com -> baeldung -> packages的目录结构。

        大多数IDE会根据我们的包名来帮助创建这个目录结构，所以我们不必手工创建这些目录。

4. 使用包的成员

    让我们先在一个名为domain的子包中定义一个TodoItem类。

    com.baeldung.core.packages.domain/TodoItem.java

    1. 导入

        为了从另一个包的类中使用我们的TodoItem类，我们需要导入它。一旦它被导入，我们就可以通过名字来访问它。

        我们可以从一个包中导入一个单一的类型，或者使用星号来导入一个包中的所有类型。

        让我们来导入整个域子包。

        `import com.baeldung.packages.domain.*;`

        现在，让我们只导入TodoItem类。

        `import com.baeldung.packages.domain.TodoItem;`

        JDK和其他Java库也有自己的包。我们可以用同样的方式导入我们想在项目中使用的预先存在的类。

        例如，让我们导入Java核心List接口和ArrayList类。

        然后我们可以在我们的应用程序中使用这些类型，只需使用它们的名字。

        com.baeldung.core.packages/TodoList.java

        在这里，我们用我们的新类和Java核心类一起，创建了一个ToDoItems的列表。

    2. 完全合格的名称

        有时，我们可能会使用来自不同包的两个同名的类。例如，我们可能同时使用 java.sql.Date 和 java.util.Date。当我们遇到命名冲突时，我们需要至少为其中一个类使用一个完全合格的类名。

        让我们使用具有完全限定名称的TodoItem。

        ```java
        public class TodoList {
            private List<com.baeldung.packages.domain.TodoItem> todoItems;

            public void addTodoItem(com.baeldung.packages.domain.TodoItem todoItem) {
                if (todoItems == null) {
                    todoItems = new ArrayList<com.baeldung.packages.domain.TodoItem>();
                }todoItems.add(todoItem);
            }
            ...
        }
        ```

5. 用javac编译

    当我们要编译打包好的类时，我们需要记住我们的目录结构。从源文件夹开始，我们需要告诉javac在哪里可以找到我们的文件。

    我们需要先编译我们的TodoItem类，因为我们的TodoList类依赖于它。

    让我们先打开一个命令行或终端，导航到我们的源文件目录/src/main/java。

    现在，让我们编译我们的com.baeldung.packages.domain.TodoItem类。

    >javac com/baeldung/core/packages/domain/TodoItem.java

    如果我们的类编译得很干净，我们就不会看到错误信息，并且在com/baeldung/packages/domain目录下应该出现一个TodoItem.class文件。

    对于引用其他包中的类型，我们应该使用-classpath标志来告诉javac命令在哪里找到其他编译的类。

    现在我们的TodoItem类已经编译完成，我们可以编译TodoList和TodoApp类。

    >javac -classpath . com/baeldung/core/packages/*.java

    同样的，我们应该没有看到错误信息，我们应该在com/baeldung/packages目录下找到两个类文件。

    让我们使用TodoApp类的全称来运行我们的应用程序。

    >java com.baeldung.core.packages.TodoApp

    我们的输出应该看起来像这样。

    ```log
    TodoItem [id=0, description=Todo item 1, dueDate=2023-02-16]
    TodoItem [id=1, description=Todo item 2, dueDate=2023-02-17]
    TodoItem [id=2, description=Todo item 3, dueDate=2023-02-18]
    ```

6. 总结

    在这篇短文中，我们了解了什么是包以及为什么我们应该使用它们。

    我们讨论了命名规则以及包与目录结构的关系。我们还看到了如何创建和使用包。

    最后，我们讨论了如何使用javac和java命令来编译和运行一个带有包的应用程序。
