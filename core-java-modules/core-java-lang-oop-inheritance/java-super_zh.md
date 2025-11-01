# [Super关键词指南](https://www.baeldung.com/java-super)

1. 简介

    在这个快速教程中，我们将看一下super Java关键字。

    简单地说，我们可以使用super关键字来访问父类。

    让我们来探索这个核心关键字在语言中的应用。

2. 带有构造函数的super关键字

    我们可以使用super()来调用父类的默认构造函数。它应该是构造函数中的第一个语句。

    在我们的例子中，我们使用super(message)和String参数：

    superkeyword/SuperSub.java: SuperSub(String)

    让我们创建一个子类实例，看看后面发生了什么：

    `SuperSub child = new SuperSub("message from the child class");`

    new关键字调用了SuperSub的构造函数，它本身首先调用了父类的构造函数，并将String参数传递给它。

3. 访问父类变量

    让我们创建一个带有消息实例变量的父类：

    superkeyword/SuperBase.java

    现在，我们用同名的变量创建一个子类：

    superkeyword/SuperSub.java: getParentMessage()

    我们可以通过使用super关键字从子类中访问父类变量。

4. 使用方法覆盖的super关键字

    在进一步讨论之前，我们建议回顾我们的方法覆盖指南。

    让我们为我们的父类添加一个实例方法：

    superkeyword/SuperBase.java: printMessage()

    并在我们的子类中覆盖printMessage()方法：

    superkeyword/SuperSub.java: printMessage()

    我们可以使用super来访问子类中的重载方法。构造函数中的super.printMessage()调用SuperBase中的父方法。
