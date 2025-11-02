# [this关键词的指南](https://www.baeldung.com/java-this)

1. 简介

    在本教程中，我们将看一下this Java关键字。

    在Java中，这个关键字是对当前对象的引用，其方法正在被调用。

    让我们来探讨如何以及何时使用该关键字。

2. 歧义场影射

    Disambiguating Field Shadowing

    这个关键字对于区分实例变量和局部参数很有用。最常见的原因是当我们有与实例字段同名的构造器参数时：

    ```java
    public class KeywordTest {
        private String name;
        private int age;
        public KeywordTest(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
    ```

    正如我们在这里看到的，我们在name和age实例字段上使用这个--将它们与参数区分开来。

    另一种用法是在局部范围内的参数隐藏或阴影中使用它。一个使用的例子可以在[变量和方法隐藏](https://www.baeldung.com/java-variable-method-hiding)的文章中找到。

3. 引用同一类的构造函数

    在构造函数中，我们可以使用this()来调用同一类别的不同构造函数。在这里，我们使用this()进行构造函数链，以减少代码的使用。

    最常见的用例是从参数化构造函数中调用一个默认的构造函数：

    ```java
    public KeywordTest(String name, int age) {
        this();
    }
    ```

    或者，我们可以从无参数构造函数中调用参数化构造函数并传递一些参数：

    ```java
    public KeywordTest() {
        this("John", 27);
    }
    ```

    注意，this()应该是构造函数的第一个语句，否则会发生编译错误。

4. 将this作为参数传递

    这里我们有printInstance()方法，其中定义了this关键字参数：

    ```java
    public KeywordTest() {
        printInstance(this);
    }

    public void printInstance(KeywordTest thisKeyword) {
        System.out.println(thisKeyword);
    }
    ```

    在构造函数中，我们调用了printInstance()方法。通过这个方法，我们传递一个对当前实例的引用。

5. 返回this

    我们也可以使用这个关键字来从方法中返回当前的类实例。

    为了不重复代码，这里有一个完整的实际例子，说明它是如何在构建器设计模式中实现的。

6. 内层类中的this关键字

    我们也用它来从内类中访问外类实例：

    ```java
    public class KeywordTest {
        private String name;
        class ThisInnerClass {
            boolean isInnerClass = true;
            public ThisInnerClass() {
                KeywordTest thisKeyword = KeywordTest.this;
                String outerString = KeywordTest.this.name;
            }
        }
    }
    ```

    在这里，在构造函数内部，我们可以通过KeywordTest.this调用获得对KeywordTest实例的引用。我们可以更深入地访问实例变量，如KeywordTest.this.name字段。

7. 总结

    在这篇文章中，我们探讨了Java中的this关键字。
