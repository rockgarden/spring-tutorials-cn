# [在 Java 中创建对象的指南](https://www.baeldung.com/java-initialization)

1. 概述

   简单地说，在我们能够在 JVM 上使用一个对象之前，它必须被初始化。

   在本教程中，我们将研究初始化原始类型和对象的各种方法。

2. 声明与初始化

   首先，让我们先明确这两个概念。

   声明（Declaration）是指定义变量及其类型和名称的过程。

   这里我们要声明 id 变量：

   `int id;`

   另一方面，初始化是指分配一个值：

   `id = 1;`

   为了演示，我们将创建一个带有名字和 id 属性的用户类：

   ![user](./src/main/java/com/baeldung/initializationguide/User.java)

   接下来，我们将看到初始化的工作方式因我们要初始化的字段类型而不同。

3. 对象与基元

   Java 提供了两种类型的数据表示方式：基本类型（primitive types）和引用类型（reference types）。在本节中，我们将讨论这两种类型在初始化方面的区别。

   Java 有八种内置的数据类型，称为 Java 基本类型；这类类型的变量直接存储其值。

   引用类型则存储对对象（类的实例）的引用。与基本类型将值直接存储在变量所分配的内存位置不同，引用类型并不直接保存其所指向对象的值。

   相反，引用通过存储对象在内存中的地址来指向该对象。

   需要注意的是，Java 不允许我们直接获取该物理内存地址。我们只能通过引用去访问或操作对应的对象。

   让我们看一个例子，它声明并初始化了一个来自用户类的引用类型：

   参见 givenUserInstance_whenIntializedWithNew_thenInstanceIsNotNull()

   ![UserUnitTest](./src/test/java/com/baeldung/initializationguide/UserUnitTest.java)

   我们可以看到，通过使用关键字 new，可以将一个引用分配给一个新的对象，它负责创建新的用户对象。

4. 创建对象

   与基元不同，对象的创建要更复杂一些。这是因为我们不只是向字段添加值；相反，我们使用 new 关键字触发初始化。作为回报，这将调用一个构造函数并初始化内存中的对象。

   new 关键字负责通过构造函数为新对象分配内存。

   构造函数通常用于初始化代表创建对象主要属性的实例变量。

   如果我们没有明确提供一个构造函数，编译器会创建一个默认的构造函数，它没有参数，只是为对象分配内存。

   一个类可以有很多构造函数，只要它们的参数列表是不同的（重载）。每一个不调用同一类中另一个构造函数的构造函数都有一个对其父级构造函数的调用，不管它是明确写的还是由编译器通过 super()插入的。

   让我们给我们的 User 类添加一个构造函数：

   ```java
   public User(String name, int id) {
       this.name = name;
       this.id = id;
   }
   ```

   现在我们可以使用我们的构造函数来创建一个具有初始属性值的用户对象：

   `User user = new User("Alice", 1);`

5. 变量范围

   在下面的章节中，我们将看一下 Java 中变量可以存在的不同类型的作用域，以及这对初始化过程的影响。

   1. 实例和类变量

      实例变量和类变量不需要我们初始化它们。一旦我们声明这些变量，它们就会被赋予一个默认值。

      | Type                   | Default Value |
      | ---------------------- | ------------- |
      | boolean                | false         |
      | byte, short, int, long | 0             |
      | float, double          | 0.0           |
      | char                   | '\u0000'      |
      | Reference Type         | null          |

      现在让我们试着定义一些与实例和类相关的变量，并测试它们是否有一个默认值：

      参见上面 UserUnitTest.java givenUserInstance_whenValuesAreNotInitialized_thenUserNameAndIdReturnDefault() 方法。

   2. 本地变量

      本地变量在使用前必须被初始化，因为它们没有默认值，而且编译器不会让我们使用一个未初始化的值。

      例如，下面的代码会产生一个编译器错误：

      ```java
      public void print(){
          int i;
          System.out.println(i);
      }
      ```

6. 最终关键字

   应用于一个字段的 final 关键字意味着该字段的值在初始化后不能再被改变。通过这种方式，我们可以在 Java 中定义常量。

   让我们给我们的用户类添加一个常量：

   `private static final int YEAR = 2000;`

   常量必须在声明时或在构造函数中被初始化。

7. Java 中的初始化器

   在 Java 中，初始化器是一个没有相关名称或数据类型的代码块，它被置于任何方法、构造函数或其他代码块之外。

   Java 提供两种类型的初始化器：静态初始化器和实例初始化器。

   1. 实例初始化器

      我们可以用这些来初始化实例变量。

      为了演示，我们将在 User 类中使用一个实例初始化器为用户 ID 提供一个值：

      ```java
      {
          id = 0;
      }
      ```

   2. 静态初始化块

      静态初始化器，或静态块，是一个用于初始化静态字段的代码块。换句话说，它是一个简单的初始化器，标有关键字 static：

      ```java
      private static String forum;
      static {
          forum = "Java";
      }
      ```

8. 初始化的顺序

   在编写用于初始化不同类型字段的代码时，我们必须注意初始化的顺序。

   在 Java 中，初始化语句的执行顺序如下：

   - 静态变量（static variables）
   - 实例变量（instance variables）
   - 构造函数（constructors）

9. 对象的生命周期

   现在我们已经了解了如何声明和初始化对象，接下来探讨一下当对象不再被使用时会发生什么。

   与其他一些需要我们手动管理对象销毁的语言不同，Java 通过其**垃圾回收器**（garbage collector）自动处理不再使用的对象。

   在 Java 中，所有对象都存储在程序的**堆内存**（heap memory）中。实际上，堆内存代表了一大块为 Java 应用程序分配的、可供使用的内存池。

   而垃圾回收器是一个 Java 内置的程序，它通过删除**不再可达**（unreachable）的对象来实现自动内存管理。

   一个 Java 对象要变为“不可达”，通常会遇到以下情况之一：

   - 指向该对象的**所有引用都已被移除**（即没有引用再指向它）；
   - 指向该对象的**所有引用都已超出作用域**（out of scope）。
     - **程序中没有任何处于有效作用域内的变量再指向该对象** → 对象“失联” → 成为垃圾回收的候选对象。

   总结来说，一个对象通常通过 `new` 关键字从类创建而来；随后在其生命周期中，为我们提供对其方法和字段的访问；最终，当它不再被需要时，垃圾回收器会自动将其回收销毁。

10. 创建对象的其他方法

    在这一节中，我们将简要地看一下除了 new 关键字以外的创建对象的方法，并学习如何应用它们，特别是反射、克隆和序列化。

    反射是一种机制，我们可以用来在运行时检查类、字段和方法。下面是一个使用反射创建我们的用户对象的例子：

    UserUnitTest.java givenUserInstance_whenInitializedWithReflection_thenInstanceIsNotNull()

    在这种情况下，我们使用反射来寻找和调用 User 类的构造函数。

    下一个方法，克隆，是一种创建一个对象的精确拷贝的方法。为此，我们的用户类必须实现 Cloneable 接口：

    `public class User implements Cloneable { //... }`

    现在我们可以使用 clone()方法来创建一个新的 clonedUser 对象，它的属性值与用户对象相同：

    UserUnitTest.java givenUserInstance_whenCopiedWithClone_thenExactMatchIsCreated()

    我们还可以使用 sun.misc.Unsafe 类来为一个对象分配内存，而不调用构造函数：

    `User u = (User) unsafeInstance.allocateInstance(User.class);`

11. 总结

    在这篇文章中，我们介绍了 Java 中字段的初始化。然后我们研究了 Java 中不同的数据类型以及如何使用它们。我们还探讨了在 Java 中创建对象的几种方法。

## “作用域”（Scope）

在 Java 中，**作用域**指的是一个变量（包括对象引用）在程序中**可以被访问的范围**。通常由一对大括号 `{}` 界定。

例如：

```java
public void someMethod() {
    {
        String s = "Hello";  // s 的作用域从这里开始
        System.out.println(s);
    } // s 的作用域在这里结束
    // 在这里，s 已经不可访问（超出作用域）
}
```

假设你创建了一个对象，并用一个局部变量引用它：

```java
public void createObject() {
    MyClass obj = new MyClass(); // obj 是一个引用，指向堆中的对象
    obj.doSomething();
} // 方法结束，局部变量 obj 超出作用域
```

- 在 `createObject()` 方法执行期间，`obj` 是一个有效的引用，指向堆中的 `MyClass` 实例。
- 当方法执行完毕，局部变量 `obj` **被销毁**（因为它只存在于方法的栈帧中），**不再存在**。
- 此时，如果**没有其他变量引用这个对象**，那么该对象就变成了**不可达**（unreachable）。
- 于是，这个对象就符合垃圾回收的条件，可能在未来的某个时间被垃圾回收器回收。

> 所以，“所有引用都已超出作用域” 意味着：曾经指向该对象的所有变量，都已经离开了它们的有效范围（比如方法结束、代码块结束等），导致程序中再也无法访问到这个对象。

举个对比例子

情况 1：仍有引用在作用域内 → **不会被回收**

```java
public class Example {
    private static MyClass globalRef;

    public void method() {
        MyClass localRef = new MyClass();
        globalRef = localRef; // 把引用赋给静态变量（作用域更广）
    } // localRef 超出作用域，但 globalRef 仍然引用该对象
}
```

→ 对象**不会被回收**，因为 `globalRef` 仍在作用域内并持有引用。

情况 2：所有引用都超出作用域 → **可被回收**

```java
public void method() {
    MyClass obj = new MyClass();
    obj.doSomething();
} // 方法结束，obj 超出作用域，且无其他引用
```

→ 对象**变为不可达**，可以被垃圾回收。
