# [final关键字](https://www.baeldung.com/java-final)

1. 概述

    虽然继承使我们能够重用现有的代码，但有时我们确实需要出于各种原因对可扩展性进行限制；final关键字正好允许我们这样做。

    在本教程中，我们将看看final关键字对类、方法和变量意味着什么。

    测试代码参见：FinalUnitTest.java

2. 最终类

    标记为final的类不能被扩展。如果我们看一下Java核心库的代码，我们会发现那里有许多final类。一个例子是String类。

    考虑一下这种情况，如果我们可以扩展String类，覆盖它的任何方法，并且用我们特定的String子类的实例代替所有的String实例。

    那么，对String对象的操作结果将变得不可预测。鉴于String类到处都在使用，这是不可以接受的。这就是为什么String类被标记为final。

    任何试图继承一个最终类的行为都会导致编译器错误。为了证明这一点，我们来创建最终类Cat：

    finalkeyword/Cat.java

    让我们试着去扩展它：

    `public class BlackCat extends Cat {}`

    我们会看到编译器的错误：

    `The type BlackCat cannot subclass the final class Cat`

    注意，类声明中的final关键字并不意味着这个类的对象是不可改变的。我们可以自由改变Cat对象的字段：

    whenChangedFinalClassProperties_thenChanged()

    我们只是不能扩展它。

    如果我们严格遵循良好的设计规则，我们应该谨慎地创建和记录一个类，或者为了安全起见声明它是最终的。然而，在创建final类时，我们应该谨慎行事。

    请注意，将一个类定为final意味着其他程序员不能改进它。想象一下，我们正在使用一个类，但没有它的源代码，而且有一个方法出现了问题。

    如果这个类是最终的，我们就不能扩展它来覆盖这个方法并解决这个问题。换句话说，我们失去了可扩展性，这是面向对象编程的好处之一。

3. 最终方法

    标记为final的方法不能被重写。当我们设计一个类，觉得某个方法不应该被重写时，我们可以把这个方法定为最终方法。我们也可以在Java核心库中找到许多最终方法。

    有时我们不需要完全禁止一个类的扩展，而只需要防止对某些方法的重写。这方面的一个好例子是Thread类。对它进行扩展是合法的，因此可以创建一个自定义的线程类。但是它的isAlive()方法是最终的。

    这个方法检查一个线程是否是活的。由于许多原因，不可能正确地重写isAlive()方法。其中之一就是这个方法是本地的。原生代码是用另一种编程语言实现的，而且往往是针对它所运行的操作系统和硬件的。

    让我们创建一个狗类，并使其sound()方法成为最终方法：

    finalkeyword/Dog.java

    现在让我们扩展狗类并尝试覆盖它的sound()方法：

    ```java
    public class BlackDog extends Dog {
        public void sound() {
        }
    }
    ```

    我们会看到编译器的错误：

    ```log
    - overrides
    com.baeldung.finalkeyword.Dog.sound
    - Cannot override the final method from Dog
    sound() method is final and can’t be overridden
    ```

    如果我们类的某些方法被其他方法调用，我们应该考虑将被调用的方法变成final。否则，覆盖它们会影响调用者的工作，并导致令人惊讶的结果。

    如果我们的构造函数调用了其他方法，出于上述原因，我们一般应该将这些方法声明为final。

    把类的所有方法都定为final和把类本身定为final有什么区别？在第一种情况下，我们可以扩展该类并为其添加新方法。

    在第二种情况下，我们不能这样做。

4. 最终变量

    标记为final的变量不能被重新分配。一旦一个最终变量被初始化，它就不能被改变。

    1. 最终原始变量

        让我们声明一个原始的最终变量i，然后给它赋值1。

        让我们试着给它赋值为2：

        ```java
        public void whenFinalVariableAssign_thenOnlyOnce() {
            final int i = 1;
            //...
            i=2;
        }
        ```

        编译器说：

        `The final local variable i may already have been assigned`

    2. 最终参考变量

        如果我们有一个最终引用变量，我们也不能重新分配它。但这并不意味着它所指的对象是不可改变的。我们可以自由地改变这个对象的属性。

        为了证明这一点，让我们声明最终引用变量cat并初始化它：

        `final Cat cat = new Cat();`

        如果我们试图重新赋值它，我们会看到一个编译器错误：

        `The final local variable cat cannot be assigned. It must be blank and not using a compound assignment`

        但是我们可以改变Cat实例的属性：

        ```java
        cat.setWeight(5);
        assertEquals(5, cat.getWeight());
        ```

    3. 最终字段

        最终字段可以是常量，也可以是一次性写入的字段。为了区分它们，我们应该问一个问题--如果我们要对对象进行序列化，我们会包括这个字段吗？如果不会，那么它就不是对象的一部分，而是一个常量。

        注意，根据命名惯例，类常量应该是大写的，组件之间用下划线（"_"）字符分隔：

        `static final int MAX_WIDTH = 999;`

        注意，任何最终字段必须在构造函数完成之前被初始化。

        对于静态最终字段，这意味着我们可以初始化它们：

        - 如上例所示，在声明中
        - 在静态初始化块中

        对于实例最终字段，这意味着我们可以初始化它们：

        - 在声明时
        - 在实例初始化程序块中
        - 在构造函数中

        否则，编译器会给我们一个错误。

    4. 最终参数

        final关键字也是合法的，可以放在方法参数之前。一个最终参数不能在方法中被改变：

        ```java
        public void methodWithFinalArguments(final int x) {
            x=1;
        }
        ```

        上述赋值导致了编译器错误：

        `The final local variable x cannot be assigned. It must be blank and not using a compound assignment`

5. 总结

    在这篇文章中，我们学习了final关键字对类、方法和变量的意义。尽管我们在内部代码中可能不会经常使用final关键字，但它可能是一个好的设计方案。
