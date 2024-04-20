# [Java中的命令行参数](https://www.baeldung.com/java-command-line-arguments)

1. 简介

    使用参数从命令行运行应用程序是很常见的。尤其是在服务器端。通常，我们不希望应用程序每次运行都做同样的事情：我们希望以某种方式配置其行为。

    在本教程中，我们将探讨如何在 Java 中处理命令行参数。

2. 在 Java 中访问命令行参数

    由于 main 方法是 Java 应用程序的入口点，因此 JVM 会通过其参数传递命令行参数。

    传统的方法是使用字符串数组：

    ```java
    public static void main(String[] args) {
        // handle arguments
    }
    ```

    不过，Java 5 引入了 varargs，它是披着羊皮的数组。因此，我们可以用一个字符串 vararg 来定义我们的 main：

    ```java
    public static void main(String... args) {
        // handle arguments
    }
    ```

    它们完全相同，因此在它们之间做出选择完全取决于个人的品味和偏好。

    main 方法的方法参数包含命令行参数，其顺序与执行时传递的顺序相同。如果我们想知道得到了多少参数，只需检查数组的长度即可。

    例如，我们可以在标准输出中打印参数的数量及其值：commandlinearguments/CliExample.java

    请注意，在某些语言中，第一个参数将是应用程序的名称。而在 Java 中，该数组只包含参数。

3. 如何传递命令行参数

    现在，我们有了一个可以处理命令行参数的应用程序，我们迫不及待地想试试。让我们看看有哪些选项。

    1. 命令行

        最明显的方法就是命令行。假设我们已经编译了 com.baeldung.commandlinearguments.CliExample 类，其中包含了我们的主方法。

        > 需要切换到 target/classes 目录下执行相应的 .class 文件

        然后，我们可以使用以下命令运行它：

        `java com.baeldung.commandlinearguments.CliExample`

        它会产生以下输出：

        Argument count: 0

        现在，我们可以在类名后面传递参数：

        `java com.baeldung.commandlinearguments.CliExample Hello World!`

        输出结果如下

        ```log
        Argument count: 2
        Argument 0: Hello
        Argument 1: World!
        ```

        通常，我们会将应用程序发布为 jar 文件，而不是一堆 .class 文件。比方说，我们将应用程序打包为 cli-example.jar，并将 com.baeldung.commandlinearguments.CliExample 设为主类。

        现在，我们可以通过以下方式运行它，而无需参数：

        `java -jar cli-example.jar`

        或者使用参数

        `java -jar cli-example.jar Hello World！`

        请注意，Java 会将我们在类名或 jar 文件名之后传递的所有参数都视为应用程序的参数。因此，我们在此之前传递的所有参数都是 JVM 本身的参数。

    2. Eclipse

        在开发应用程序的过程中，我们需要检查它是否按我们的要求运行。

        在 Eclipse 中，我们可以借助运行配置来运行应用程序。例如，运行配置定义了要使用的 JVM、入口点、classpath 等。当然，我们还可以指定命令行参数。

        创建适当运行配置的最简单方法是右键单击我们的主方法，然后从上下文菜单中选择 "Run As">"ava Application"。

        这样，我们就能立即以符合项目设置的设置运行应用程序。

        为了提供参数，我们需要编辑运行配置。我们可以通过 "Run">"Run Configurations…" 菜单选项进行编辑。在此，我们应单击参数选项卡并填写程序参数文本框：

        点击 "run" 将运行应用程序并传递我们刚刚输入的参数。

    3. IntelliJ

        IntelliJ 使用类似的程序来运行应用程序。它将这些选项简单地称为配置。

        首先，我们需要右键单击 main 方法，然后选择 Run 'CliExample.main()'。

        这将运行我们的程序，但也会将其添加到运行列表中，以便进一步配置。

        因此，要配置参数，我们应选择 "Run > Edit Configurations…"，然后编辑程序参数文本框。

        然后点击 "OK（确定）"并重新运行应用程序，例如使用工具栏上的运行按钮。

    4. NetBeans

        NetBeans 的运行和配置过程也是一致的。

        首先，我们应右击main方法并选择运行文件来运行应用程序：Run File

        和之前一样，这将创建一个运行配置并运行程序。

        接下来，我们必须配置运行配置中的参数。我们可以选择 "Run > Set Project Configuration > Customize…" ，然后在左边的 "run" 中填写参数文本框。

        然后点击 "OK" 并启动应用程序。

4. 第三方库

    在简单的情况下，手动处理命令行参数非常简单。但是，随着我们的需求越来越复杂，我们的代码也会越来越复杂。因此，如果我们想创建一个具有多个命令行选项的应用程序，使用第三方库会更方便。

    幸运的是，有大量第三方库支持大多数使用情况。[Picocli](https://www.baeldung.com/java-picocli-create-command-line-program) 和 [Spring Shell](https://www.baeldung.com/spring-shell-cli) 就是两个流行的例子。

5. 总结

    让应用程序的行为可配置始终是个好主意。在本文中，我们介绍了如何使用命令行参数来做到这一点。此外，我们还介绍了传递这些参数的各种方法。
