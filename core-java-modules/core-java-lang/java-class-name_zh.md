# [在Java中检索一个类的名称](https://www.baeldung.com/java-class-name)

1. 概述

    在本教程中，我们将学习从Class API的方法中检索类的名称的四种方法：getSimpleName()、[getName()](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/lang/Class.html#getName())、[getTypeName()](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/lang/Class.html#getTypeName())和[getCanonicalName()](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/lang/Class.html#getCanonicalName())。

    这些方法可能会令人困惑，因为它们的名字很相似，而且它们的Javadocs有些模糊。在涉及到原始类型、对象类型、内部或匿名类以及数组时，它们也有一些细微的差别。

2. 检索简单名称

    让我们从getSimpleName()方法开始。

    在Java中，有两种名字：简单的和限定的。一个简单的名字由一个唯一的标识符组成，而一个合格的名字是由点分隔的简单名字的序列。

    顾名思义，getSimpleName()返回底层类的简单名称，也就是它在源代码中被赋予的名称。

    我们还可以获得原始类型和数组的简单名称。对于原始类型，这将是它们的名字，如int、boolean或float。

    而对于数组，该方法将返回数组类型的简单名称，后面是数组每个维度的一对开括号和闭括号（[]）。

    ```java
    RetrieveClassName[] names = new RetrieveClassName[];
    assertEquals("RetrieveClassName[]", names.getClass().getSimpleName())。
    ```

    因此，对于一个二维的字符串数组，对其类调用getSimpleName()将返回String[][]。

    最后，还有一个匿名类的特殊情况。在一个匿名类上调用getSimpleName()将返回一个空字符串。

3. 检索其他名称

    现在是时候看看我们如何获得一个类的名称、类型名称或规范名称。与getSimpleName()不同，这些名字旨在提供更多关于类的信息。

    getCanonicalName()方法总是返回[Java语言规范](https://docs.oracle.com/javase/specs/jls/se11/html/jls-6.html#jls-6.7)中定义的规范名称。

    至于其他方法，根据使用情况，输出可能会有一些不同。我们将看到这对不同的基元和对象类型意味着什么。

    1. 原始类型

        让我们从原始类型开始，因为它们很简单。对于原始类型，所有三个方法 getName(), getTypeName() 和 getCanonicalName() 将返回与 getSimpleName() 相同的结果。

    2. 对象类型

        我们现在将看到这些方法如何与对象类型一起工作。它们的行为通常是相同的：它们都返回类的经典名称。

        在大多数情况下，这是一个限定的名字，包含了所有类包的简单名称以及类的简单名称。

    3. 内部类

        我们在上一节看到的是这些方法调用的一般行为，但也有一些例外。

        内部类就是其中之一。getName()和getTypeName()方法的行为与内类的getCanonicalName()方法不同。

        getCanonicalName()仍然返回类的经典名称，也就是包围类的经典名称加上内层类的简单名称，用一个点分开。

        另一方面，getName()和getTypeName()方法的返回值基本相同，但是在包围类的经典名称和内层类的简单名称之间使用了一个美元作为分隔符。

        参见RetrieveClassName的内类InnerClass。

    4. 匿名类

        匿名类是另一个例外。

        正如我们已经看到的，它们没有简单的名字，但是它们也没有一个典型的名字。因此，getCanonicalName()并不返回任何东西。与getSimpleName()相反，getCanonicalName()在调用匿名类时将返回null而不是一个空字符串。

        至于getName()和getTypeName()，它们将返回调用类的经典名称，后面是一个美元和一个数字，代表该匿名类在调用类中创建的所有匿名类中的位置。

    5. 数组

        最后，让我们看看上述三个方法是如何处理数组的。

        为了表明我们在处理数组，每个方法都会更新其标准结果。getTypeName()和getCanonicalName()方法将把成对的括号附加到其结果中。

        让我们看看下面的例子，我们在一个二维的InnerClass数组上调用getTypeName（）和getCanonicalName（）。

        ```java
        assertEquals("com.baeldung.RetrieveClassName$InnerClass[][]" 。
        RetrieveClassName.InnerClass[][].class.getTypeName())。
        assertEquals("com.baeldung.RetrieveClassName.InnerClass[][]"。
        RetrieveClassName.InnerClass[][].class.getCanonicalName())。
        ```

        注意第一个调用是如何使用美元而不是点来分隔内类部分和名字的其他部分的。

        现在让我们看看getName()方法是如何工作的。在一个原始类型数组上调用时，它将返回一个开括号和一个代表原始类型的字母。让我们用下面的例子来检查，在一个二维原始整数数组上调用该方法。

        `assertEquals("[I", int[][].class.getName());`

        另一方面，当在一个对象数组上调用时，它将在其标准结果中加入一个开头的括号和L字母，并以一个分号结束。让我们在RetrieveClassName的数组上试试。

        `assertEquals("[Lcom.baeldung.className.RetrieveClassName;", RetrieveClassName[].class.getName());`

4. 总结

    在这篇文章中，我们看了在Java中访问类名的四个方法。这些方法是：getSimpleName()、getName()、getTypeName()和getCanonicalName()。

    我们了解到，第一个方法只是返回一个类的源代码名称，而其他方法则提供了更多的信息，如包名和指示该类是内部类还是匿名类。
