# [SOLID 原则指南](https://www.baeldung.com/solid-principles)

1. 概述

    在本教程中，我们将讨论面向对象设计的 SOLID 原则。

    首先，我们将探讨这些原则产生的原因，以及为什么我们在设计软件时要考虑这些原则。然后，我们将结合一些示例代码概述每条原则。

2. SOLID 原则的由来

    SOLID 原则是由 Robert C. Martin 在 2000 年发表的论文 "[设计原则和设计模式](https://fi.ort.edu.uy/innovaportal/file/2032/1/design_principles.pdf)"中提出的。后来，迈克尔-费瑟斯（Michael Feathers）在这些概念的基础上，为我们介绍了 SOLID 首字母缩写词。在过去的 20 年中，这五项原则彻底改变了面向对象编程的世界，改变了我们编写软件的方式。

    那么，什么是 SOLID，它如何帮助我们写出更好的代码？简而言之，Martin 和 Feathers 的设计原则鼓励我们创建更易于维护、理解和灵活的软件。因此，当我们的应用程序规模不断扩大时，我们可以降低其复杂性，为以后的工作省去很多麻烦！

    以下五个概念构成了我们的 SOLID 原则：

    - 单一责任 Single Responsibility
    - 开放/封闭 Open/Closed
    - 利斯科夫替代 Liskov Substitution
    - 接口隔离 Interface Segregation
    - 依赖反转 Dependency Inversion

    虽然这些概念看起来令人生畏，但通过一些简单的代码示例就能轻松理解。在下面的章节中，我们将深入探讨这些原则，并通过一个快速的 Java 示例来说明每一个原则。

3. 单一责任

    让我们从单一责任原则开始。正如我们所期望的，该原则规定一个类只能有一个责任。此外，它应该只有一个改变的理由。

    这一原则如何帮助我们构建更好的软件？让我们来看看它的几个好处：

    - 测试--只有一个职责的类的测试用例要少得多。
    - 降低耦合度 - 单个类中的功能越少，依赖性就越小。
    - 组织--组织良好的小类比单体类更容易搜索。

    例如，我们来看一个表示简单书籍的类，我们存储了与图书实例相关的名称、作者和文本。

    再让我们添加几个方法来查询文本：

    main/.s/Book.java

    现在，我们的图书类运行良好，我们可以在应用程序中存储任意数量的图书。

    但是，如果我们不能将文本输出到控制台并进行阅读，那么存储信息又有什么用呢？

    让我们抛开谨慎，添加一个打印方法：

    ```java
    public class BadBook {
        //...
        void printTextToConsole(){
            // our code for formatting and printing the text
        }
    }
    ```

    然而，这段代码违反了我们之前概述的单一责任原则。

    为了解决这个问题，我们应该实现一个单独的类，只处理文本的打印：

    main/.s/BookPrinter.java

    太棒了 我们不仅开发了一个可以减轻 Book 打印任务的类，还可以利用我们的 BookPrinter 类将文本发送到其他媒体。

    无论是电子邮件、日志还是其他任何东西，我们都有一个单独的类来专门处理这个问题。

4. 开放扩展，关闭修改

    现在到了 SOLID 中的 "O" 的时候了，即开放-封闭原则。简单地说，类应该开放用于扩展，但封闭用于修改。这样，我们就可以避免修改现有代码，从而给原本愉快的应用程序带来潜在的新错误。

    当然，在修复现有代码中的错误时，这条规则也有例外。

    让我们通过一个简单的代码示例来探讨这个概念。作为新项目的一部分，假设我们实现了一个吉他类。

    它是完全成熟的，甚至还有一个音量旋钮：

    main/.o/Guitar.java

    我们启动了应用程序，每个人都很喜欢它。但几个月后，我们觉得吉他有点乏味，可以用一个很酷的火焰图案让它看起来更摇滚。

    此时，我们很可能会打开吉他类并添加一个火焰图案，但谁知道这样做会在应用程序中产生什么错误呢？

    相反，让我们坚持开放-封闭原则，简单地扩展我们的吉他类：

    main/.o/SuperCoolGuitarWithFlames.java

    通过扩展 Guitar 类，我们可以确保现有应用程序不会受到影响。

5. 利斯科夫替换

    接下来要介绍的是利斯科夫替换原则，这可以说是五项原则中最复杂的一项。简单地说，如果类 A 是类 B 的子类型，我们就可以用 A 替换掉 B，而不会破坏程序的行为。

    让我们直接来看代码，帮助我们理解这一概念：

    main/.l/Car.java

    上面，我们定义了一个简单的汽车接口，其中包含几个所有汽车都应具备的方法：打开引擎和加速前进。

    让我们来实现我们的接口，并为这些方法提供一些代码：

    main/.l/MotorCar.java

    正如我们的代码所描述的，我们有了一个可以打开的发动机，我们可以增加动力。

    但是等等--我们现在生活在电动汽车时代：

    main/.l/ElectricCar.java

    将一辆没有发动机的汽车加入其中，我们本质上改变了程序的行为。这公然违反了利斯科夫替代原则，而且比前两个原则更难解决。

    一种可能的解决方案是，将我们的模型重新设计为考虑到汽车无引擎状态的接口。

6. 界面隔离

    SOLID 中的 "I" 代表接口隔离（interface segregation），简单来说就是将较大的接口分割成较小的接口。通过这样做，我们可以确保实现类只需关注它们感兴趣的方法。

    在本例中，我们将尝试扮演动物园管理员的角色。更具体地说，我们将在熊的围栏里工作。

    让我们从一个接口开始，这个接口概述了我们作为熊饲养员的角色：

    main/.i/BearKeeper.java

    作为狂热的动物饲养员，我们非常乐意为我们心爱的熊清洗和喂食。但我们也非常清楚抚摸它们的危险。不幸的是，我们的界面相当大，我们别无选择，只能实现抚摸小熊的代码。

    为了解决这个问题，我们将大接口拆分成三个独立的接口：

    main/.i/BearCleaner.java

    main/.i/BearFeeder.java

    main/.i/BearPetter.java

    现在，由于有了接口隔离，我们可以只实现与我们相关的方法：

    main/.i/BearCarer.java

    最后，我们可以把危险的事情留给那些鲁莽的人：

    main/.i/CrazyPerson.java

    我们甚至可以从前面的示例中拆分出 BookPrinter 类，以同样的方式使用接口隔离。通过实现具有单个打印方法的打印机接口，我们可以实例化单独的 ConsoleBookPrinter 和 OtherMediaBookPrinter 类。

    ```java
    public interface ConsoleBookPrinter {
        void consoleBookPrinter();
    }
    public interface MediaBookPrinter {
        void mediaBookPrinter();
    }
    public class BookPrinter implements ConsoleBookPrinter {
        public void consoleBookPrinter() {};
    }
    ```

7. 依赖反转

    依赖反转原则指的是软件模块的解耦。这样，高级模块不再依赖于低级模块，而是两者都依赖于抽象模块。

    为了证明这一点，让我们用老式的代码来演示 Windows 98 计算机：

    `public class Windows98Machine {}`

    但是，没有显示器和键盘的电脑有什么用呢？让我们在构造函数中各添加一个，这样我们实例化的每台 Windows98 计算机都会预装显示器和标准键盘：

    main/.d/Windows98Machine.java

    这段代码将正常工作，我们将能在 Windows98Computer 类中自由使用 StandardKeyboard 和 Monitor。

    问题解决了吗？还没有。通过使用 new 关键字声明 StandardKeyboard 和 Monitor，我们将这三个类紧密地结合在了一起。

    这不仅使我们的 Windows98Computer 难以测试，而且我们也失去了在需要时将 StandardKeyboard 类换成其他类的能力。而且，我们也只能使用 Monitor 类了。

    让我们添加一个更通用的键盘接口，并在我们的类中使用它，从而将我们的机器与 StandardKeyboard 分离开来：

    ```java
    public interface Keyboard { }

    public class Windows98Machine{
        private final Keyboard keyboard;
        private final Monitor monitor;
        public Windows98Machine(Keyboard keyboard, Monitor monitor) {
            this.keyboard = keyboard;
            this.monitor = monitor;
        }
    }
    ```

    在这里，我们使用依赖注入模式将 Keyboard 依赖关系添加到 Windows98Machine 类中。

    我们还要修改 StandardKeyboard 类以实现 Keyboard 接口，使其适合注入到 Windows98Machine 类中：

    `public class StandardKeyboard implements Keyboard { }`

    现在，我们的类已经解耦，并通过 Keyboard 抽象进行通信。如果需要，我们可以通过不同的接口实现，轻松更换机器中的键盘类型。我们可以遵循同样的原则来处理 Monitor 类。

    非常棒 我们已经解耦了依赖关系，可以自由地使用我们选择的任何测试框架来测试 Windows98Machine。

8. 结束语

    在本文中，我们深入探讨了面向对象设计的 SOLID 原则。

    我们首先简要介绍了 SOLID 的历史以及这些原则存在的原因。

    然后，我们逐字逐句地解释了每条原则的含义，并举例说明了违反这些原则的代码。然后，我们看看如何修正我们的代码，使其符合 SOLID 原则。
