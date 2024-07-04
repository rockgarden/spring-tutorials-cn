# Core Java Lang OOP - Constructors

This module contains article about constructors in Java

## Java中的构造函数指南

1. 简介

    构造函数是[面向对象设计](https://www.baeldung.com/java-polymorphism)的守门员。

    在本教程中，我们将看到它们如何作为一个单一的位置来初始化被创建对象的[内部状态](https://www.baeldung.com/java-inheritance-composition)。

    让我们继续前进，创建一个代表银行账户的简单对象。

2. 设置一个银行账户

    想象一下，我们需要创建一个代表银行账户的类。它将包含一个名称、创建日期和余额。

    同时，让我们覆盖toString方法，将细节打印到控制台：

    constructors/BankAccount.java

    现在，这个类包含了存储银行账户信息所需的所有必要字段，但它还不包含构造函数。

    这意味着，如果我们创建一个新的对象，字段的值不会被初始化：

    ```java
    BankAccount account = new BankAccount();
    account.toString();
    ```

    运行上面的toString方法将导致一个异常，因为对象的名称和打开的仍然是空的：

    ```log
    java.lang.NullPointerException
        at com.baeldung.constructors.BankAccount.toString(BankAccount.java:12)
        at com.baeldung.constructors.ConstructorUnitTest
        .givenNoExplicitContructor_whenUsed_thenFails(ConstructorUnitTest.java:23)
    ```

3. 一个没有参数的构造函数

    让我们用一个构造函数来解决这个问题：

    ```java
    class BankAccount {
        public BankAccount() {
            this.name = "";
            this.opened = LocalDateTime.now();
            this.balance = 0.0d;
        }
    }
    ```

    请注意我们刚刚写的构造函数的几件事。首先，它是一个方法，但它没有返回类型。这是因为构造函数隐含地返回它所创建对象的类型。现在调用new BankAccount()将调用上面的构造函数。

    其次，它不需要参数。这种特殊的构造函数被称为无参数构造函数。

    不过，为什么我们第一次不需要它呢？这是因为当我们没有明确地写任何构造函数时，编译器会添加一个默认的无参数构造函数。

    这就是为什么我们第一次能够构造对象，尽管我们没有明确写一个构造函数。默认的无参数构造函数会简单地将所有成员设置为[默认值](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html)。

    对于对象来说，就是null，这就导致了我们前面看到的异常。

4. 一个参数化的构造函数

    现在，构造函数的一个真正的好处是，在向对象注入状态时，它可以帮助我们保持封装。

    因此，为了对这个银行账户做一些真正有用的事情，我们需要能够真正向该对象注入一些初始值。

    要做到这一点，让我们写一个参数化的构造函数，也就是一个需要一些参数的构造函数：

    ```java
    class BankAccount {
        public BankAccount() { ... }
        public BankAccount(String name, LocalDateTime opened, double balance) {
            this.name = name;
            this.opened = opened;
            this.balance = balance;
        }
    }
    ```

    现在我们可以用我们的BankAccount类做一些有用的事情：

    ```java
        LocalDateTime opened = LocalDateTime.of(2018, Month.JUNE, 29, 06, 30, 00);
        BankAccount account = new BankAccount("Tom", opened, 1000.0f); 
        account.toString();
    ```

    注意，我们的类现在有两个构造函数。一个显式的、无参数的构造函数和一个参数化的构造函数。

    我们可以创建任意多的构造函数，但我们可能不希望创建太多的构造函数。这样会有点混乱。

    如果我们在代码中发现有太多的构造函数，一些[创造型设计模式](https://www.baeldung.com/creational-design-patterns)可能会有帮助。

5. 拷贝构造函数

    构造函数不需要仅仅局限于初始化。它们也可以被用来以其他方式创建对象。想象一下，我们需要能够从一个现有的账户中创建一个新的账户。

    新账户应该有与旧账户相同的名称，今天的创建日期和没有资金。我们可以使用一个复制构造函数来实现：

    ```java
    public BankAccount(BankAccount other) {
        this.name = other.name;
        this.opened = LocalDateTime.now();
        this.balance = 0.0f;
    }
    ```

    现在我们有以下行为：

    ```java
    LocalDateTime opened = LocalDateTime.of(2018, Month.JUNE, 29, 06, 30, 00);
    BankAccount account = new BankAccount("Tim", opened, 1000.0f);
    BankAccount newAccount = new BankAccount(account);

    assertThat(account.getName()).isEqualTo(newAccount.getName());
    assertThat(account.getOpened()).isNotEqualTo(newAccount.getOpened());
    assertThat(newAccount.getBalance()).isEqualTo(0.0f);
    ```

6. 一个链式构造函数

    当然，我们可以推断出一些构造函数的参数，或者给其中一些参数以默认值。

    例如，我们可以只用名字来创建一个新的银行账户。

    因此，让我们创建一个带有名称参数的构造函数，并给其他参数以默认值：

    ```java
    public BankAccount(String name, LocalDateTime opened, double balance) {
        this.name = name;
        this.opened = opened;
        this.balance = balance;
    }
    public BankAccount(String name) {
        this(name, LocalDateTime.now(), 0.0f);
    }
    ```

    通过关键字this，我们正在调用另一个构造函数。

    我们必须记住，如果我们想连锁一个超类构造函数，我们必须使用super而不是this。

    另外，请记住，this或super表达式应该永远是第一条语句。

7. 值类型

    Java中构造函数的一个有趣的用途是创建值对象。值对象是一个在初始化后不改变其内部状态的对象。

    也就是说，该对象是不可变的。Java中的不可变性有一些[细微](https://www.baeldung.com/java-immutable-object)的差别，在创建对象时要注意。

    让我们继续创建一个不可变的类：

    ```java
    class Transaction {
        final BankAccount bankAccount;
        final LocalDateTime date;
        final double amount;

        public Transaction(BankAccount account, LocalDateTime date, double amount) {
            this.bankAccount = account;
            this.date = date;
            this.amount = amount;
        }
    }
    ```

    注意，我们现在在定义类的成员时使用了final关键字。这意味着每个成员只能在类的构造函数中被初始化。它们不能在以后的任何其他方法中被重新分配。我们可以读取这些值，但不能改变它们。

    如果我们为Transaction类创建多个构造函数，每个构造函数都需要初始化每个最终变量。不这样做会导致编译错误。

8. 结语

    我们已经参观了构造函数构建对象的不同方式。如果明智地使用，构造函数构成了Java中面向对象设计的基本构件。

## Relevant Articles

- [x] [A Guide to Constructors in Java](https://www.baeldung.com/java-constructors)
- [Java Copy Constructor](https://www.baeldung.com/java-copy-constructor)
- [Cannot Reference “X” Before Supertype Constructor Has Been Called](https://www.baeldung.com/java-cannot-reference-x-before-supertype-constructor-error)
- [Private Constructors in Java](https://www.baeldung.com/java-private-constructors)
- [Throwing Exceptions in Constructors](https://www.baeldung.com/java-constructors-exceptions)
- [Constructors in Java Abstract Classes](https://www.baeldung.com/java-abstract-classes-constructors)
- [Java Implicit Super Constructor is Undefined Error](https://www.baeldung.com/java-implicit-super-constructor-is-undefined-error)
- [Constructor Specification in Java](https://www.baeldung.com/java-constructor-specification)
- [Static vs. Instance Initializer Block in Java](https://www.baeldung.com/java-static-instance-initializer-blocks)
- [Accessing Private Constructor in Java](https://www.baeldung.com/java-private-constructor-access)
- [Different Ways to Create an Object in Java](https://www.baeldung.com/java-different-ways-to-create-objects)

## Code

像往常一样，代码样本可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-oop-constructors)上找到。
