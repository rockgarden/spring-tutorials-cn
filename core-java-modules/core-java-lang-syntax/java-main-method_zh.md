# [Java的main()方法详解](https://www.baeldung.com/java-main-method)

1. 概述

    每个程序都需要一个地方来开始执行；说到Java程序，那就是main方法。
    我们在编写代码时习惯于写main方法，以至于我们甚至没有注意到它的细节。在这篇简短的文章中，我们将分析这个方法，并展示一些其他的编写方法。

2. 常见的签名

    最常见的main方法模板是。

    `public static void main(String[] args) { }`

    这就是我们学习的方式，这就是IDE为我们自动完成代码的方式。但这并不是这个方法的唯一形式，还有一些有效的变体我们可以使用，而不是每个开发者都会注意到这个事实。

    在我们深入研究这些方法的签名之前，让我们回顾一下普通签名中每个关键词的含义。

    - public--访问修改器，意味着全局可见
    - static--该方法可以直接从类中访问，我们不必实例化一个对象来获得一个引用并使用它
    - void - 意味着这个方法不会返回一个值
    - main - 方法的名称，这是JVM在执行Java程序时寻找的标识符。

    至于args参数，它表示方法所接收的值。这就是我们在第一次启动程序时向其传递参数的方式。

    参数args是一个字符串数组。在下面的例子中。

    `java CommonMainMethodSignature foo bar`

    我们正在执行一个名为CommonMainMethodSignature的Java程序，并传递两个参数：foo和bar。这些值可以在main方法中作为`args[0]`（以foo为值）和`args[1]`（以bar为值）被访问。

    在下一个例子中，我们要检查args来决定是加载测试还是生产参数。

    ```java
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("test")) {
                // load test parameters
            } else if (args[0].equals("production")) {
                // load production parameters
            }
        }
    }
    ```

    记住，IDE也可以向程序传递参数，这一点总是很好。

3. 写main()方法的不同方法

    让我们看看写main方法的一些不同方式。虽然它们不是很常见，但都是有效的签名。

    请注意，这些都不是专门针对main方法的，它们可以用于任何Java方法，但它们也是main方法的有效部分。

    方括号可以放在String附近，就像常见的模板一样，也可以放在两侧的args附近。

    `public static void main(String []args) { }`

    `public static void main(String args[]) { }`

    参数可以用varargs来表示。

    `public static void main(String...args) { }`

    我们甚至可以为main()方法添加strictfp，在处理浮点值时，它用于处理器之间的兼容。

    `public strictfp static void main(String[] args) { }`

    synchronized和final也是main方法的有效关键字，但它们在这里不会产生影响。

    另一方面，final可以应用于args，以防止数组被修改。

    `public static void main(final String[] args) { }`

    为了结束这些例子，我们也可以用上述所有的关键字来写main方法（当然，在实际应用中你可能永远不会用到这些关键字）。

    final static synchronized strictfp void main(final String[] args) { }

4. 拥有一个以上的main()方法

    我们也可以在我们的应用程序中定义一个以上的main方法。

    事实上，有些人把它作为一种原始的测试技术来验证单个的类（尽管像JUnit这样的测试框架更适合这种活动）。

    为了指定JVM应该执行哪个主方法作为我们应用程序的入口，我们使用MANIFEST.MF文件。在清单中，我们可以指明主类。

    主类： mypackage.ClassWithMainMethod

    这主要是在创建可执行的.jar文件时使用。我们通过位于META-INF/MANIFEST.MF的清单文件（以UTF-8编码），指出哪个类有主方法来启动执行。

5. 总结

    本教程描述了main方法的细节以及它可以采取的一些其他形式，甚至是那些对大多数开发者来说并不常见的形式。

    请记住，尽管我们所展示的所有例子在语法上都是有效的，但它们只是起到教育作用，大多数时候我们会坚持使用常见的签名来完成我们的工作。
