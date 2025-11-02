# [Java中的继承和组合（Is-a与Has-a关系）](https://www.baeldung.com/java-inheritance-composition)

1. 概述

    继承和组合--与抽象、封装和多态一起--是面向对象编程（OOP）的基石。

    在本教程中，我们将介绍继承和组合的基础知识，我们将重点关注发现这两种关系之间的差异。

2. 继承的基础知识

    继承是一个强大但被过度使用和误用的机制。

    简单地说，通过继承，一个基类（又称基类型）定义了一个特定类型所共有的状态和行为，并让子类（又称子类型）提供该状态和行为的专门版本。

    为了清楚地了解如何使用继承，让我们创造一个例子：一个基类Person定义了一个人的通用字段和方法，而子类Waitress和Actress提供了额外的、细粒度的方法实现。

    这里是Person类：

    ![Person.java](./src/main/java/com/baeldung/inheritancecomposition/model/Person.java)

    而这些是子类：

    ![Waitress.java](./src/main/java/com/baeldung/inheritancecomposition/model/Waitress.java)

    ![Actress.java](./src/main/java/com/baeldung/inheritancecomposition/model/Actress.java)

    此外，让我们创建一个单元测试来验证女服务员和女演员类的实例也是人的实例，从而表明在类型级别上满足 "is-a "条件：

    ![InheritanceUnitTest.java](./src/test/java/com/baeldung/inheritancecomposition/InheritanceUnitTest.java)

    在此必须强调继承的语义方面的问题。除了重复使用Person类的实现之外，我们还在基类型Person和子类型Waitress和Actress之间创建了一个定义明确的 "is-a" 关系。女服务员和女演员，实际上就是人。

    这可能会引起我们的疑问：在哪些用例中，继承是正确的做法？

    如果子类型满足了 "is-a" 的条件，并且主要是为类的层次结构提供附加的功能，那么继承就是正确的做法。

    当然，只要被覆盖的方法保留了[里氏替换原则（Liskov Substitution principle）](https://en.wikipedia.org/wiki/Liskov_substitution_principle)所提倡的基础类型/子类型的可替代性，那么方法的覆盖是允许的。

    此外，我们应该记住，子类型继承了基类型的API，这在某些情况下可能是矫枉过正或者仅仅是不可取(overkill or merely)的。

    否则，我们应该使用组合来代替。

3. 设计模式中的继承性

    虽然大家都认为在可能的情况下，我们应该选择组合而不是继承，但在一些典型的用例中，继承也有其存在的意义。

    1. 层超类型模式

        Layer Supertype Pattern, 在这种情况下，我们使用继承来将普通的代码转移到基类（超类型），在每个层的基础上。

        下面是这种模式在领域层的基本实现：

        ```java
        public class Entity {
            protected long id;
            // setters
        }
        public class User extends Entity {
            // additional fields and methods
        }
        ```

        我们可以将同样的方法应用于系统中的其他层，如服务层和持久化层。

    2. 模板方法模式

        Template Method Pattern, 在模板方法模式中，我们可以用一个基类来定义算法的不变部分，然后在子类中实现变体部分：

        ```java
        public abstract class ComputerBuilder {
            public final Computer buildComputer() {
                addProcessor();
                addMemory();
            }
            public abstract void addProcessor();
            public abstract void addMemory();
        }

        public class StandardComputerBuilder extends ComputerBuilder {
            @Override
            public void addProcessor() {
                // method implementation
            }
            @Override
            public void addMemory() {
                // method implementation
            }
        }
        ```

4. 组合的基本原理

    组合是OOP提供的另一种重用实现的机制。

    简而言之，组合允许我们对由其他对象组成的对象进行建模，从而定义了它们之间的 "has-a" 关系。

    此外，组合是最强的[关联](https://en.wikipedia.org/wiki/Association_(object-oriented_programming))形式，这意味着当一个对象被销毁时，组成或被一个对象包含的对象也会被销毁。

    为了更好地理解构成的作用，让我们假设我们需要与代表计算机的对象一起工作。

    一台计算机是由不同的部分组成的，包括微处理器、内存、声卡等等，所以我们可以把计算机和它的每个部分都建模为单独的类。

    下面是计算机类的一个简单实现：

    ![Computer.java](./src/main/java/com/baeldung/inheritancecomposition/model/Computer.java)

    下面的类对一个微处理器、内存和声卡进行了建模（为了简洁起见，省略了接口）：

    ![StandardProcessor.java](./src/main/java/com/baeldung/inheritancecomposition/model/StandardProcessor.java)

    ![StandardMemory.java](./src/main/java/com/baeldung/inheritancecomposition/model/StandardMemory.java)

    ![StandardSoundCard.java](./src/main/java/com/baeldung/inheritancecomposition/model/StandardSoundCard.java)

    推动“组合优于继承”背后的动机很容易理解。在任何能够为某个类与其他类之间建立语义上正确的“has-a”（拥有）关系的场景中，组合都是正确的选择。

    在上述示例中，Computer 类与其所包含的各个部件类之间就满足了“has-a”的条件。

    还值得注意的是，在这种情况下，如果被包含的对象无法在另一个 Computer 对象中复用，那么包含它们的 Computer 对象就拥有这些对象的所有权；反之，如果这些对象可以被复用，则我们使用的是聚合（aggregation）而非组合（composition），因为此时并不隐含所有权关系。

5. 没有抽象的组合

    另外，我们也可以通过硬编码计算机类的依赖关系来定义组合关系，而不是在构造函数中声明它们：

    ```java
    public class Computer {
        private StandardProcessor processor
            = new StandardProcessor("Intel I3");
        private StandardMemory memory
            = new StandardMemory("Kingston", "1TB");
        // additional fields / methods
    }
    ```

    当然，这将是一个僵化的、紧密耦合的设计，因为我们将使计算机强烈依赖于处理器和内存的具体实现。

    我们将无法利用接口和依赖注入所提供的抽象层次的优势。

    通过基于接口的初始设计，我们得到了一个松散耦合的设计，这也更容易测试。

6. 总结

    在这篇文章中，我们学习了Java中继承和组合的基本原理，并深入探讨了这两种关系（"is-a "与"has-a"）之间的区别。
