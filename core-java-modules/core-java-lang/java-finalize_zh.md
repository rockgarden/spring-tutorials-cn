# [Java中finalize方法的指南](https://www.baeldung.com/java-finalize)

1. 概述

    在本教程中，我们将重点讨论Java语言的一个核心方面--由根对象类提供的finalize方法。

    简单地说，这是在对某一特定对象进行垃圾收集之前调用的。

2. 使用终结者

    finalize()方法被称为finalizer。

    当JVM发现这个特定的实例应该被垃圾回收时，就会调用终结者。这样的终结者可以执行任何操作，包括让对象复活。

    然而，终结者的主要目的是在对象从内存中删除之前释放其使用的资源。终结器可以作为清理操作的主要机制，也可以作为其他方法失败时的安全网。

    为了理解终结器是如何工作的，我们来看看一个类的声明。

    finalize/Finalizable.java

    Finalizable类有一个字段阅读器，它引用一个可关闭的资源。当从这个类中创建一个对象时，它会构造一个新的BufferedReader实例，从classpath中的一个文件读取。

    这样一个实例被用于readFirstLine方法，以提取给定文件中的第一行。

    我们可以用一个终结者来做到这一点。

    很容易看出，最终器的声明就像任何普通的实例方法一样。

    实际上，垃圾收集器调用最终器的时间取决于JVM的实现和系统的条件，这些都是我们无法控制的。

    为了使垃圾收集在现场发生，我们将利用System.gc方法。在现实世界的系统中，我们不应该显式地调用该方法，原因有很多。

    它的成本很高

    - 它不会立即触发垃圾收集 - 它只是提示JVM开始GC
    - JVM更清楚什么时候需要调用GC
    - 如果我们需要强制GC，我们可以使用jconsole来实现。

    下面是一个测试案例，演示了一个终结者的操作。

    finalize/FinalizeUnitTest.whenGC_thenFinalizerExecuted()

    在第一条语句中，一个Finalizable对象被创建，然后它的readFirstLine方法被调用。这个对象没有分配给任何变量，因此当System.gc方法被调用时，它就有资格进行垃圾回收。

    测试中的断言验证了输入文件的内容，只是用来证明我们的自定义类如预期那样工作。

    当我们运行所提供的测试时，在控制台中会打印出一条关于缓冲的阅读器在finalizer中被关闭的信息。这意味着finalize方法被调用，它已经清理了资源。

    到此为止，终结者看起来是一个预销毁操作的好方法。然而，这并不完全正确。

    在下一节，我们将看到为什么要避免使用它们。

3. 避免终结者

    尽管终结者带来了很多好处，但也有很多弊端。

    1. 终结器的缺点

        让我们来看看在使用终结者来执行关键动作时，我们会遇到的几个问题。

        第一个明显的问题是缺乏及时性。我们无法知道终结者何时运行，因为垃圾回收可能随时发生。

        就其本身而言，这并不是一个问题，因为终结者还是会执行的，或早或晚。然而，系统资源并不是无限的。因此，我们可能在清理发生之前就耗尽了资源，这可能会导致系统崩溃。

        终结器对程序的可移植性也有影响。由于垃圾收集算法与JVM的实现有关，一个程序可能在一个系统上运行得非常好，而在另一个系统上的表现却不同。

        性能成本是终结者带来的另一个重要问题。具体来说，JVM在构造和销毁包含非空终结者的对象时必须执行更多的操作。

        我们要讨论的最后一个问题是在最终化过程中缺乏对异常的处理。如果一个终结者抛出一个异常，终结过程就会停止，使对象处于损坏的状态而没有任何通知。

    2. 终结器的效果演示

        现在是时候抛开理论，看看终结者在实践中的效果了。

        让我们定义一个带有非空终结器的新类。

        finalize/CrashedFinalizable.java

        注意finalize()方法 - 它只是向控制台打印一个空字符串。如果这个方法完全是空的，JVM就会把这个对象当作没有终结器的对象。因此，我们需要为finalize()提供一个实现，在这种情况下它几乎什么都不做。

        在main方法中，一个新的CrashedFinalizable实例在for循环的每个迭代中被创建。这个实例没有分配给任何变量，因此有资格进行垃圾回收。

        让我们添加一些语句，看看在运行时内存中存在多少对象。

        给出的语句访问JVM内部类中的一些字段，并在每百万次迭代后打印出对象引用的数量。

        让我们通过执行main方法来启动程序。我们可能期望它无限期地运行，但事实并非如此。几分钟后，我们应该看到系统崩溃，出现类似这样的错误。

        ```log
        ...
        There are 26231621 references in the queue
        There are 26975913 references in the queue
        Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
            at java.lang.ref.Finalizer.register(Finalizer.java:91)
            at java.lang.Object.<init>(Object.java:37)
            at com.baeldung.finalize.CrashedFinalizable.<init>(CrashedFinalizable.java:6)
            at com.baeldung.finalize.CrashedFinalizable.main(CrashedFinalizable.java:9)
        Process finished with exit code 1
        ```

        看起来垃圾收集器没有做好它的工作--对象的数量不断增加，直到系统崩溃。

        如果我们去掉终结器，引用的数量通常为0，程序将永远运行下去。

    3. 解释

        为了理解为什么垃圾收集器没有按规定丢弃对象，我们需要看一下JVM的内部工作方式。

        当创建一个具有终结器的对象（也称为引用）时，JVM会创建一个附带的java.lang.ref.Finalizer类型的引用对象。在引用对象准备好进行垃圾回收后，JVM将引用对象标记为准备好进行处理，并将其放入一个引用队列中。

        我们可以通过java.lang.ref.Finalizer类中的静态字段队列访问这个队列。

        同时，一个名为Finalizer的特殊守护线程不断运行，并在引用队列中寻找对象。当它找到一个时，它会从队列中移除引用对象，并调用引用者的finalizer。

        在下一个垃圾收集周期中，参考对象将被丢弃--当它不再被参考对象引用时。

        如果一个线程一直在高速生产对象，这就是我们的例子中发生的情况，Finalizer线程无法跟上。最终，内存将无法存储所有的对象，我们最终会出现OutOfMemoryError。

        请注意，像本节中所示的对象以极快的速度被创建的情况在现实生活中并不经常发生。然而，它展示了一个重要的观点--终结者是非常昂贵的。

4. 无终结器实例

    让我们探讨一个提供相同功能但不使用finalize()方法的解决方案。请注意，下面的例子并不是取代最终处理程序的唯一方法。

    相反，它被用来证明一个重要的观点：总是有一些选项可以帮助我们避免使用终结者。

    下面是我们新类的声明。

    finalize/CloseableResource.java

    不难看出，新的CloseableResource类与我们之前的Finalizable类的唯一区别是实现了AutoCloseable接口，而不是定义了一个终结器。

    注意到CloseableResource的close方法的主体与Finalizable类中finalizer的主体几乎一样。

    下面是一个测试方法，它读取一个输入文件并在完成工作后释放资源。

    FinalizeUnitTest.whenTryWResourcesExits_thenResourceClosed()

    在上面的测试中，一个CloseableResource实例是在try-with-resources语句的try块中创建的，因此，当try-with-resources块执行完毕时，该资源会自动关闭。

    运行给定的测试方法，我们会看到从CloseableResource类的close方法中打印出来的信息。

5. 总结

    在本教程中，我们关注了Java中的一个核心概念--finalize方法。这在纸面上看起来很有用，但在运行时可能会产生丑陋的副作用。而且，更重要的是，除了使用finalizer之外，总是有其他的解决方案。

    需要注意的一个关键点是，finalize已经从Java 9开始被废弃了--而且最终会被删除。
