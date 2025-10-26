# [Java中的抽象类](https://www.baeldung.com/java-abstract-class)

1. 概述

    在实现一个合同时，有很多情况下我们想把实现的某些部分推迟到以后完成。在Java中，我们可以通过抽象类轻松实现这一目标。

    在本教程中，我们将学习Java中的抽象类的基本知识，以及在哪些情况下它们会有帮助。

2. 抽象类的关键概念

    在深入探讨何时使用抽象类之前，让我们看看它们最相关的特征：

    - 我们用class关键字前面的abstract修饰词来定义一个抽象类。
    - 抽象类可以被子类化，但它不能被实例化。
    - 如果一个类定义了一个或多个抽象方法，那么这个类本身就必须被声明为抽象的。
    - 一个抽象类可以同时声明抽象和具体方法
    - 从一个抽象类派生出来的子类必须实现基类的所有抽象方法，或者本身就是抽象的。

    为了更好地理解这些概念，我们将创建一个简单的例子。

    让我们让我们的基抽象类定义一个棋盘游戏的抽象API：

    abstractclasses.overview/BoardGame.java

    然后，我们可以创建一个实现play方法的子类：

    abstractclasses.overview/Checkers.java

3. 何时使用抽象类

    现在，让我们来分析一下几个典型的场景，在这些场景中，我们应该选择抽象类而不是接口和具体类：

    - 我们想在一个地方封装一些共同的功能（代码重用），让多个相关的子类共享这些功能。
    - 我们需要部分地定义一个API，使我们的子类可以很容易地扩展和完善。
    - 子类需要继承一个或多个带有受保护访问修饰符的公共方法或字段

    让我们记住，所有这些情况都是完全的、基于继承的遵守[开放/封闭原则](https://en.wikipedia.org/wiki/Open–closed_principle)的好例子。

    此外，由于使用抽象类隐含地处理了基类型和子类型，我们也在利用多态性(Polymorphism)。

    请注意，代码重用是使用抽象类的一个非常有说服力的理由，只要类层次结构中的 "is-a" 关系被保留下来。

    而[Java 8又增加了另一个问题](https://www.baeldung.com/java-static-default-methods)，即默认方法，它有时可以取代需要创建一个抽象类的位置。

    > With Java 8’s "default method" feature, any abstract class without direct or inherited field should be converted into an interface. However, this change may not be appropriate in libraries or other applications where the class is intended to be used as an API.

4. 文件读取器的层次结构样本

    为了更清楚地了解抽象类带来的功能，让我们再看一个例子。

    1. 定义一个基础抽象类

        因此，如果我们想拥有几种类型的文件阅读器，我们可能会创建一个抽象类来封装文件阅读的通用功能：

        abstractclasses.filereader/BaseFileReader.java

        请注意，我们已经将filePath设置为保护状态，以便子类在需要时可以访问它。更重要的是，我们留下了一些未完成的工作：如何从文件的内容中实际解析出一行文本。

        我们的计划很简单：虽然我们的具体类没有各自的特殊方式来存储文件路径或浏览文件，但它们将各自有特殊的方式来转换每一行。

        乍一看，BaseFileReader 似乎没有必要。然而，它是一个简洁、易于扩展的设计的基础。从它开始，我们可以很容易地实现不同版本的文件阅读器，可以专注于它们独特的业务逻辑。

    2. 定义子类

        一个自然的实现可能是将文件的内容转换为小写字母：

        abstractclasses.filereader/LowercaseFileReader.java

        或者另一个可能是将文件内容转换为大写字母的：

        abstractclasses.filereader/UppercaseFileReader.java

        正如我们从这个简单的例子中所看到的，每个子类都可以专注于其独特的行为，而不需要指定文件阅读的其他方面。

    3. 使用一个子类

        最后，使用一个继承自抽象类的类与其他具体类没有什么不同：

        abstractclasses/LowercaseFileReaderUnitTest.java

        为简单起见，目标文件位于 src/main/resources/files 文件夹下。因此，我们使用了一个应用程序类加载器来获取示例文件的路径。请随时查看我们[关于Java中类加载器的教程](https://www.baeldung.com/java-classloaders)。

5. 总结

    在这篇简短的文章中，我们了解了Java中抽象类的基本知识，以及何时使用它们来实现抽象并将通用的实现封装在一个地方。
