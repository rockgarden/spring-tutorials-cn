# Core Java Exceptions

## Java中的异常处理

1. 概述

    在本教程中，我们将了解Java中异常处理的基础知识，以及其中的一些问题。

2. 第一原则

    1. 它是什么？

        为了更好地理解异常和异常处理，让我们做一个现实生活的比较。

        想象一下，我们在网上订购了一个产品，但在途中出现了交付失败的情况。一个好的公司可以处理这个问题，并优雅地重新安排我们的包裹，使它仍然按时到达。

        同样地，在Java中，代码在执行我们的指令时可能会出现错误。好的异常处理可以处理错误，并优雅地重新安排程序，让用户仍然有良好的体验。

    2. 为什么要使用它？

        我们通常在一个理想化的环境中写代码：文件系统总是包含我们的文件，网络是健康的，JVM总是有足够的内存。有时我们把这称为 "happy path"。

        但在生产中，文件系统会损坏，网络会中断，JVM的内存会耗尽。我们的代码的好坏取决于它如何处理 "unhappy paths"。

        我们必须处理这些情况，因为它们会对应用程序的流程产生负面影响并形成异常：

        exceptions.exceptionhandling/Exceptions.java: getPlayers()

        getPlayers方法选择不处理IOException，而是把它传到调用栈中。在一个理想化的环境中，这段代码运行良好。

        但在生产中，如果缺少了player.dat，可能会发生什么？

        ```log
        Exception in thread "main" java.nio.file.NoSuchFileException: players.dat <-- players.dat file doesn't exist
            at sun.nio.fs.WindowsException.translateToIOException(Unknown Source)
            // ... more stack trace
            at java.nio.file.Files.readAllLines(Unknown Source)
            at Exceptions.getPlayers(Exceptions.java:12) <-- Exception arises in getPlayers() method, on line 12
            at Exceptions.main(Exceptions.java:19) <-- getPlayers() is called by main(), on line 19
        ```

        如果不处理这个异常，一个健康的程序可能会完全停止运行! 我们需要确保我们的代码对出错的情况有一个计划。

        还要注意这里的异常还有一个好处，那就是堆栈跟踪本身。因为有了这个堆栈跟踪，我们往往可以在不需要附加调试器的情况下找出违规的代码。

3. 异常的层次结构

    归根结底，异常只是Java对象，它们都是由Throwable扩展而来：

    ```txt
                ---> Throwable <--- 
                |    (checked)     |
                |                  |
                |                  |
        ---> Exception           Error
        |    (checked)        (unchecked)
        |
    RuntimeException
    (unchecked)
    ```

    主要有三类例外情况：

    - 已检查的例外情况 Checked exceptions
    - 未检查的异常/运行时异常 Unchecked exceptions / Runtime exceptions
    - Errors
    运行时异常和未检查的异常指的是同一件事。我们常常可以交替使用它们。

    1. 检查过的异常

        检查过的异常是Java编译器要求我们处理的异常。我们要么声明性地将异常抛上调用栈，要么自己处理它。

        [Oracle的文档](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html)告诉我们，当我们可以合理地期望方法的调用者能够恢复时，就要使用检查过的异常。

        检查异常的几个例子是IOException和ServletException。

    2. 未检查的异常

        未检查的异常是Java编译器不要求我们处理的异常。

        简单地说，如果我们创建了一个扩展了RuntimeException的异常，那么它将是未检查的；否则，它将被检查。

        虽然这听起来很方便，但甲骨文的文档告诉我们，这两个概念都有很好的理由，比如区分情景situational错误（检查的）和使用usage错误（未检查的）。

        一些未检查异常的例子是NullPointerException、IllegalArgumentException和SecurityException。

    3. 错误

        错误代表了严重的、通常是无法恢复的情况，如库不兼容、无限递归或内存泄漏。

        尽管它们没有扩展RuntimeException，但它们也是不被检查的。

        在大多数情况下，我们处理、实例化或扩展Errors是很奇怪的。通常情况下，我们希望这些东西能一直传播下去。

        错误的几个例子是StackOverflowError和OutOfMemoryError。

4. 处理异常

    在Java API中，有很多地方都可能出错，其中一些地方在签名或Javadoc中都标有异常：

    ```java
    /**
     * @exception FileNotFoundException ...
     */
    public Scanner(String fileName) throws FileNotFoundException {
    // ...
    }
    ```

    正如前面所说的，当我们调用这些 "risky" 的方法时，我们必须处理那些被检查过的异常，我们也可以处理那些没有被检查过的异常。Java给了我们几种方法来做到这一点：

    1. 抛出

        "处理" 一个异常的最简单方法是重新抛出rethrow它：

        ```java
        public int getPlayerScore(String playerFile)
        throws FileNotFoundException {
            Scanner contents = new Scanner(new File(playerFile));
            return Integer.parseInt(contents.nextLine());
        }
        ```

        因为 FileNotFoundException 是一个检查过的异常，这是满足编译器的最简单方法，但它确实意味着任何调用我们方法的人现在也需要处理它

        parseInt可以抛出一个NumberFormatException，但是因为它是未检查的，我们不需要处理它。

    2. Try-catch

        如果我们想尝试自己处理这个异常，我们可以使用try-catch块。我们可以通过重新抛出我们的异常来处理它：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                Scanner contents = new Scanner(new File(playerFile));
                return Integer.parseInt(contents.nextLine());
            } catch (FileNotFoundException noFile) {
                throw new IllegalArgumentException("File not found");
            }
        }
        ```

        或者通过执行恢复步骤：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                Scanner contents = new Scanner(new File(playerFile));
                return Integer.parseInt(contents.nextLine());
            } catch ( FileNotFoundException noFile ) {
                logger.warn("File not found, resetting score.");
                return 0;
            }
        }
        ```

    3. 最后

        现在，有些时候我们有一些代码需要执行，而不管是否有异常发生，这就是final关键字的作用。

        在我们到目前为止的例子中，有一个讨厌的错误潜伏在暗处，那就是Java默认不会向操作系统返回文件柄。

        当然，无论我们是否能读到文件，我们都要确保我们做了适当的清理工作。

        让我们先用 "lazy" 的方式试试吧：

        exceptions.exceptionhandling/Exceptions.java: getPlayerScoreFinally()

        在这里，finally块指出了我们希望Java运行的代码，无论试图读取文件的过程发生了什么。

        即使在调用堆栈中抛出了FileNotFoundException，Java也会在这之前调用Final的内容。

        我们也可以既处理异常又确保我们的资源被关闭：

        ```java
        public int getPlayerScore(String playerFile) {
            Scanner contents;
            try {
                contents = new Scanner(new File(playerFile));
                return Integer.parseInt(contents.nextLine());
            } catch (FileNotFoundException noFile ) {
                logger.warn("File not found, resetting score.");
                return 0; 
            } finally {
                try {
                    if (contents != null) {
                        contents.close();
                    }
                } catch (IOException io) {
                    logger.error("Couldn't close the reader!", io);
                }
            }
        }
        ```

        因为close也是一个 "risky" 的方法，所以我们还需要捕捉它的异常!

        这可能看起来很复杂，但我们需要每一块都能正确处理可能出现的每个潜在问题。

    4. Try-with-resources

        幸运的是，从Java 7开始，我们可以在处理扩展AutoCloseable的事物时简化上述语法：

        exceptions.exceptionhandling/Exceptions.java: getPlayerScoreTryWithResources()

        当我们在try声明中放置可自动关闭的引用时，我们就不需要自己关闭资源。

        不过我们仍然可以使用finally块来做任何其他我们想要的清理工作。

        查看我们的文章，了解更多关于[try-with-resources](https://www.baeldung.com/java-try-with-resources)的信息。

    5. 多个 catch 块

        有时，代码会抛出一个以上的异常，我们可以用一个以上的catch块来单独处理：

        exceptions.exceptionhandling/Exceptions.java: getPlayerScoreMultipleCatchBlocks()

        多重捕获使我们有机会在需要时以不同方式处理每个异常。

        这里还要注意，我们没有捕获FileNotFoundException，那是因为它扩展了IOException。因为我们捕捉的是IOException，所以Java会认为它的任何子类也被处理。

        不过，假设我们需要将FileNotFoundException与更一般的IOException区别对待：

        exceptions.exceptionhandling/Exceptions.java: getPlayerScoreMultipleCatchBlocksAlternative()

        Java让我们单独处理子类的异常，记得要把它们放在捕获列表的较高位置。

    6. 联合捕获块

        当我们知道处理错误的方式要相同时，不过，Java 7引入了在同一个块中捕获多个异常的能力：

        Exceptions.java: getPlayerScoreUnionCatchBlocks()

5. 抛出异常

    如果我们不想自己处理异常，或者我们想生成我们的异常让别人来处理，那么我们就需要熟悉throw关键字。

    比方说，我们有以下我们自己创建的检查异常：exceptions.exceptionhandling/TimeoutException.java

    而我们有一个方法有可能需要很长时间才能完成：

    ```java
    public List<Player> loadAllPlayers(String playersFile) {
        // ... potentially long operation
    }
    ```

    1. 抛出一个检查过的异常

        就像从一个方法中返回一样，我们可以在任何时候抛出。

        当然，我们应该在我们试图表明出错的时候抛出：

        Exceptions.java: loadAllPlayersThrowingChecked()

        因为TimeoutException是被检查的，所以我们也必须在签名中使用throws关键字，这样我们方法的调用者就会知道要处理它。

    2. 抛出一个未检查的异常

        如果我们想做一些事情，比如，验证输入，我们可以使用一个未检查的异常来代替：

        Exceptions.java: loadAllPlayersThrowingUnchecked()

        因为IllegalArgumentException没有被选中，所以我们不需要标记这个方法，尽管我们很欢迎这样做。

        有些人还是把方法标记为一种文档形式。

    3. 包裹和重抛

        我们也可以选择重新抛出我们所捕获的异常：

        Exceptions.java: loadAllPlayersWrapping()

        或者做一个wrap并重新抛出：

        Exceptions.java: loadAllPlayersRethrowing()

        这可以很好地将许多不同的异常合并成一个。

    4. 重新抛出Throwable或Exception

        现在是一个特殊情况。

        如果一个给定的代码块唯一可能引发的异常是未检查的异常，那么我们可以捕获并重新抛出Throwable或Exception，而不需要将它们添加到我们的方法签名中：

        Exceptions.java: loadAllPlayersThrowable()

        虽然简单，但上面的代码不能抛出一个被检查的异常，正因为如此，即使我们重新抛出一个被检查的异常，我们也不必在签名中标注throws子句。

        这对于代理类和方法来说是很方便的。关于这一点的更多信息可以在[这里](http://4comprehension.com/sneakily-throwing-exceptions-in-lambda-expressions-in-java/)找到。

    5. 继承性

        当我们用throws关键字标记方法时，它会影响到子类如何覆盖我们的方法。

        在我们的方法抛出一个被检查的异常的情况下：

        ```java
        public class Exceptions {
            public List<Player> loadAllPlayers(String playersFile) 
            throws TimeoutException {
                // ...
            }
        }
        ```

        一个子类可以有一个 "less risky" 的签名：

        ```java
        public class FewerExceptions extends Exceptions {
            @Override
            public List<Player> loadAllPlayers(String playersFile) {
                // overridden
            }
        }
        ```

        但不是 "more riskier" 的签名：

        ```java
        public class MoreExceptions extends Exceptions {
            @Override
            public List<Player> loadAllPlayers(String playersFile) throws MyCheckedException {
                // overridden
            }
        }
        ```

        这是因为合约是在编译时由引用类型决定的。如果我创建一个MoreExceptions的实例，并将其保存到Exceptions：

        ```java
        Exceptions exceptions = new MoreExceptions();
        exceptions.loadAllPlayers("file");
        ```

        那么JVM只会告诉我捕捉TimeoutException，这是错误的，因为我已经说过 MoreExceptions#loadAllPlayers 抛出了一个不同的异常。

        简单地说，子类可以抛出比其超类更少的检查异常，但不能更多。

6. 反模式

    1. 吞噬异常

        现在，还有一种方法可以让我们满足编译器的要求：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                // ...
            } catch (Exception e) {} // <== catch and swallow
            return 0;
        }
        ```

        上面的内容叫做吞噬异常。大多数时候，我们这样做是有点偷懒的，因为它不能解决这个问题，而且还使其他代码也不能解决这个问题。

        有的时候，当有一个被检查的异常，我们确信它永远不会发生。在这种情况下，我们至少还是应该添加一个注释，说明我们故意吃了这个异常：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                // ...
            } catch (IOException e) {
                // this will never happen
            }
        }
        ```

        另一种我们可以 "swallow" 异常的方法是简单地将异常打印到错误流中：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                // ...
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
        ```

        我们已经改善了我们的情况，至少把错误写在了某个地方，以便日后诊断。

        不过，如果我们使用一个记录器，那就更好了：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                // ...
            } catch (IOException e) {
                logger.error("Couldn't load the score", e);
                return 0;
            }
        }
        ```

        虽然我们用这种方式处理异常非常方便，但我们需要确保我们没有吞噬重要的信息，因为我们代码的调用者可以用它来补救问题。

        最后，当我们抛出一个新的异常时，如果不把它作为一个原因，我们就会无意中吞掉一个异常：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                // ...
            } catch (IOException e) {
                throw new PlayerScoreException();
            }
        }
        ```

        在这里，我们为提醒我们的调用者注意错误而拍手称快，但我们没有把IOException作为原因。因为这样，我们失去了调用者或操作者可以用来诊断问题的重要信息。

        我们最好这样做：

        Exceptions.java: getPlayerScoreSwallowingExceptionAntiPatternAlternative()

        注意包括IOException作为PlayerScoreException的原因这一细微的差别。

    2. 在最终块中使用返回

        另一种吞噬异常的方法是从final块中返回。这是不好的，因为通过突然的返回，JVM会放弃这个异常，即使它是由我们的代码抛出的：

        Exceptions.java: getPlayerScoreReturnInFinallyAntiPattern()

        根据《[Java语言规范](https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.20.2)》的规定：

        - 如果try块的执行由于任何其他原因R而突然完成，那么就执行final块，然后就有了选择。
        - 如果final块正常完成，那么try语句就会因为R的原因而突然完成。
        - 如果final块由于原因S而突然完成，那么try语句由于原因S而突然完成（而原因R被丢弃）。

    3. 在最终块中使用throw

        与在finally块中使用return类似，finally块中抛出的异常将优先于catch块中出现的异常。

        这将 "erase" try块中的原始异常，我们将失去所有有价值的信息：

        ```java
        public int getPlayerScore(String playerFile) {
            try {
                // ...
            } catch ( IOException io ) {
                throw new IllegalStateException(io); // <== eaten by the finally
            } finally {
                throw new OtherException();
            }
        }
        ```

    4. 使用throw作为goto

        有些人还屈服于使用throw作为goto语句的诱惑：

        ```java
        public void doSomething() {
            try {
                // bunch of code
                throw new MyException();
                // second bunch of code
            } catch (MyException e) {
                // third bunch of code
            }
        }
        ```

        这很奇怪，因为这段代码试图将异常用于流程控制，而不是错误处理。

7. 常见的异常和错误

    下面是一些常见的异常和错误，我们都会时常遇到：

    1. 被检查的异常

        IOException - 这个异常通常是说网络、文件系统或数据库上的某些东西失败了。
    2. 运行时异常

        ArrayIndexOutOfBoundsException - 这个异常意味着我们试图访问一个不存在的数组索引，比如试图从一个长度为3的数组中获取索引5。
        ClassCastException - 这个异常意味着我们试图执行一个非法的转换，比如试图将一个字符串转换成一个列表。我们通常可以通过在转换前进行防御性的instanceof检查来避免它。
        IllegalArgumentException - 这个异常是一种通用的方式，我们可以说提供的方法或构造函数参数之一是无效的。
        IllegalStateException - 这个异常是一种通用的方式，我们可以说我们的内部状态，比如我们对象的状态，是无效的。
        NullPointerException - 这个异常意味着我们试图引用一个空对象。我们通常可以通过执行防御性的空值检查或者使用Optional来避免它。
        NumberFormatException - 这个异常意味着我们试图将一个字符串转换成一个数字，但是这个字符串包含非法字符，比如试图将 "5f3" 转换成一个数字。
    3. 错误

        StackOverflowError - 这个异常意味着堆栈跟踪太大。这有时会发生在大规模的应用程序中；然而，这通常意味着我们的代码中发生了一些无限递归。
        NoClassDefFoundError - 这个异常意味着类的加载失败，要么是由于不在classpath上，要么是由于静态初始化的失败。
        OutOfMemoryError - 这个异常意味着JVM没有任何可用的内存来分配给更多的对象。有时，这是由于内存泄漏造成的。
8. 总结

    在这篇文章中，我们已经了解了异常处理的基础知识，以及一些好的和不好的实践案例。

## 在Java中创建一个自定义异常

1. 概述

    在本教程中，我们将介绍如何在Java中创建一个自定义异常。

    我们将展示用户定义的异常是如何实现的，并用于检查和未检查的异常。

2. 自定义异常的必要性

    Java 异常几乎涵盖了所有编程中必然发生的一般异常。

    然而，我们有时需要用我们自己的异常来补充这些标准异常。

    这些是引入自定义异常的主要原因：

    - 业务逻辑异常--针对业务逻辑和工作流程的异常。这些有助于应用程序的用户或开发者了解确切的问题是什么。
    - 捕捉并为现有的Java异常的子集提供具体处理方法。

    Java 异常可以被检查和取消检查。

3. 自定义检查的异常

    检查型异常是需要明确处理的异常。

    让我们考虑一段返回文件第一行的代码：

    ```java
    try (Scanner file = new Scanner(new File(fileName))) {
        if (file.hasNextLine()) return file.nextLine();
    } catch(FileNotFoundException e) {
        // Logging, etc 
    }
    ```

    上面的代码是处理Java检查异常的经典方法。虽然代码抛出了FileNotFoundException，但并不清楚具体原因是什么--是文件不存在还是文件名无效。

    为了创建一个自定义的异常，我们必须扩展java.lang.Exception类。

    让我们通过创建一个名为IncorrectFileNameException的自定义检查异常来看看这个例子：

    ```java
    public class IncorrectFileNameException extends Exception { 
        public IncorrectFileNameException(String errorMessage) {
            super(errorMessage);
        }
    }
    ```

    注意，我们还必须提供一个构造函数，这个构造函数接收一个字符串作为错误信息，并调用父类的构造函数。

    这就是我们定义一个自定义异常所需要做的一切。

    接下来，让我们看看如何在我们的例子中使用自定义异常：

    ```java
    try (Scanner file = new Scanner(new File(fileName))) {
        if (file.hasNextLine())
            return file.nextLine();
    } catch (FileNotFoundException e) {
        if (!isCorrectFileName(fileName)) {
            throw new IncorrectFileNameException("Incorrect filename : " + fileName );
        }
        //...
    }
    ```

    我们已经创建并使用了一个自定义的异常，所以用户现在可以知道具体的异常是什么。

    这样做够吗？我们因此而失去了异常的根本原因。

    为了解决这个问题，我们也可以在构造函数中添加一个java.lang.Throwable参数。这样一来，我们就可以把根本的异常传递给方法调用：

    exceptions.customexception/IncorrectFileNameException.java

    现在，IncorrectFileNameException和异常的根本原因一起被使用：

    exceptions.customexception/FileManager.java: getFirstLine()

    这就是我们如何使用自定义异常，而不失去发生异常的根本原因。

4. 自定义未检查的异常

    在我们的同一个例子中，让我们假设，如果文件名不包含任何扩展名，我们需要一个自定义异常。

    在这种情况下，我们需要一个与前面类似的自定义未检查异常，因为这个错误只会在运行时被检测到。

    为了创建一个自定义的未经检查的异常，我们需要扩展java.lang.RuntimeException类：

    exceptions.customexception/IncorrectFileExtensionException.java

    这样，我们就可以在我们的例子中使用这个自定义的未检查的异常：

    exceptions.customexception/FileManager.java: getFirstLine()

5. 总结

    当我们需要处理与业务逻辑相关的特定异常时，自定义异常非常有用。如果使用得当，它们可以作为一个实用的工具来更好地处理异常和记录。

## Java中已检查和未检查的异常情况

1. 概述

    Java的异常主要分为两类：检查性异常和非检查性异常。

    在本教程中，我们将提供一些关于如何使用它们的代码示例。

2. 检验性异常

    一般来说，检查过的异常代表程序控制之外的错误。例如，如果输入文件不存在，[FileInputStream](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/FileInputStream.html#%3Cinit%3E(java.io.File))的构造函数会抛出FileNotFoundException。

    Java在编译时对检查过的异常进行验证。

    因此，我们应该使用 [throws](https://www.baeldung.com/java-throw-throws) 关键字来声明一个被检查的异常：

    exceptions.throwvsthrows/main.java: checkedExceptionWithThrows()

    我们也可以使用一个try-catch块来处理一个检查过的异常：

    main.java: checkedException()

    Java中一些[常见的检查型异常](https://www.baeldung.com/java-common-exceptions)是IOException、SQLException和ParseException。

    [Exception](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Exception.html)类是检查型异常的超类，所以我们可以通过扩展Exception来创建一个[自定义的检查型异常](https://www.baeldung.com/java-new-custom-exception)：

    exceptions.customexception/IncorrectFileNameException.java

3. 未检查的异常

    如果一个程序抛出一个未检查的异常，它反映了程序逻辑中的一些错误。

    例如，如果我们用一个数字除以0，Java会抛出ArithmeticException：

    ```java
    private static void divideByZero() {
        int numerator = 1;
        int denominator = 0;
        int result = numerator / denominator;
    }
    ```

    Java在编译时不会验证未检查的异常。此外，我们不需要在方法中用throws关键字声明未检查的异常。而尽管上述代码在编译时没有任何错误，但在运行时却会抛出ArithmeticException。

    Java中一些[常见的未经检查的异常](https://www.baeldung.com/java-common-exceptions)有NullPointerException、ArrayIndexOutOfBoundsException和IllegalArgumentException。

    [RuntimeException](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/RuntimeException.html)类是所有未经检查的异常的超类，所以我们可以通过扩展RuntimeException来创建一个自定义的[未经检查的异常](https://www.baeldung.com/java-new-custom-exception)：

    ```java
    public class NullOrEmptyException extends RuntimeException {
        public NullOrEmptyException(String errorMessage) {
            super(errorMessage);
        }
    }
    ```

4. 何时使用检查过的异常和未检查过的异常

    在Java中使用异常是一个很好的做法，这样我们就可以把处理错误的代码和常规代码分开。然而，我们需要决定抛出哪种类型的异常。[Oracle Java文档](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html)提供了关于何时使用检查过的异常和未检查过的异常的指导：

    "如果客户可以合理地期望从一个异常中恢复过来，那就把它变成一个检查过的异常。如果客户不能做任何事情来恢复这个异常，就把它变成一个未检查的异常"。

    例如，在我们打开一个文件之前，我们可以首先验证输入的文件名。如果用户输入的文件名是无效的，我们可以抛出一个自定义的检查异常：

    ```java
    if (!isCorrectFileName(fileName)) {
        throw new IncorrectFileNameException("Incorrect filename : " + fileName );
    }
    ```

    通过这种方式，我们可以通过接受另一个用户输入的文件名来恢复系统。

    然而，如果输入的文件名是一个空指针或它是一个空字符串，这意味着我们的代码中存在一些错误。在这种情况下，我们应该抛出一个未经检查的异常：

    ```java
    if (fileName == null || fileName.isEmpty())  {
        throw new NullOrEmptyException("The filename is null or empty.");
    }
    ```

5. 总结

    在这篇文章中，我们讨论了检查过的异常和未检查过的异常之间的区别。我们还提供了一些代码实例来说明何时使用有检查或无检查的异常。

## Java中的链式异常

1. 概述

    在这篇文章中，我们将非常简要地了解什么是异常，并深入地讨论Java中的连锁异常。

    简单地说，异常是一个扰乱程序正常执行流程的事件。现在让我们看看到底如何通过链式异常来获得更好的语义。

2. 链式异常

    链式异常有助于识别一种情况，即在一个应用程序中一个异常会导致另一个异常。

    例如，考虑一个方法因为试图除以0而抛出一个ArithmeticException，但实际的原因是一个I/O错误，导致除数为0，这个方法将抛出ArithmeticException给调用者。调用者不会知道异常的实际原因。在这种情况下就会使用链式异常。

    这个概念是在JDK 1.4中引入的。

    让我们看看Java中是如何支持链式异常的。

3. 可抛出类

    Throwable类有一些构造函数和方法来支持链式异常。首先，让我们看一下构造函数。

    - Throwable(Throwable cause) - Throwable有一个参数，它指定了异常的实际原因。
    - Throwable(String desc, Throwable cause) - 这个构造函数接受一个异常描述，同时也接受异常的实际原因。
    接下来，让我们看看这个类提供的方法：

    - getCause()方法 - 这个方法返回与当前异常相关的实际原因。
    - initCause()方法 - 它为调用的Exception设置一个基本原因。
4. 例子

    现在，让我们看看这个例子，我们将设置我们自己的异常描述并抛出一个连锁的异常：

    ```java
    public class MyChainedException {

        public void main(String[] args) {
            try {
                throw new ArithmeticException("Top Level Exception.")
                .initCause(new IOException("IO cause."));
            } catch(ArithmeticException ae) {
                System.out.println("Caught : " + ae);
                System.out.println("Actual cause: "+ ae.getCause());
            }
        }    
    }
    ```

    正如猜测的那样，这将导致：

    ```log
    Caught: java.lang.ArithmeticException: Top Level Exception.
    Actual cause: java.io.IOException: IO cause.
    ```

5. 为什么是链式异常？

    我们需要对异常进行连锁处理，以使日志可读。让我们写两个例子。首先是没有连锁的异常，其次是有连锁的异常。稍后，我们将比较这两种情况下日志的表现。

    首先，我们将创建一系列的异常：

    exceptions.chainedexception.exceptions/NoLeaveGrantedException.java

    exceptions.chainedexception.exceptions/TeamLeadUpsetException.java

    现在，让我们开始在代码示例中使用上述异常。

    1. 不使用链式结构

        让我们来写一个没有链入我们自定义异常的例子程序。

        LogWithoutChain.java

        在上面的例子中，日志将看起来像这样：

        ```log
        com.baeldung.exceptions.chainedexception.exceptions.TeamLeadUpsetException: Team Lead Upset
                at com.baeldung.exceptions.chainedexception.LogWithoutChain.howIsTeamLead(LogWithoutChain.java:24)
                at com.baeldung.exceptions.chainedexception.LogWithoutChain.getLeave(LogWithoutChain.java:16)
                at com.baeldung.exceptions.chainedexception.LogWithoutChain.main(LogWithoutChain.java:11)
        Exception in thread "main" com.baeldung.exceptions.chainedexception.exceptions.NoLeaveGrantedException: Leave not sanctioned.
                at com.baeldung.exceptions.chainedexception.LogWithoutChain.getLeave(LogWithoutChain.java:19)
                at com.baeldung.exceptions.chainedexception.LogWithoutChain.main(LogWithoutChain.java:11)
        ```

    2. 使用链式

        接下来，让我们写一个例子，用连锁的方式来处理我们的自定义异常：

        LogWithChain.java
        最后，让我们看看用链式异常获得的日志：

        ```log
        Exception in thread "main" com.baeldung.exceptions.chainedexception.exceptions.NoLeaveGrantedException: Leave not sanctioned.
                at com.baeldung.exceptions.chainedexception.LogWithChain.getLeave(LogWithChain.java:18)
                at com.baeldung.exceptions.chainedexception.LogWithChain.main(LogWithChain.java:11)
        Caused by: com.baeldung.exceptions.chainedexception.exceptions.TeamLeadUpsetException: Team lead is not in good mood
                at com.baeldung.exceptions.chainedexception.LogWithChain.howIsTeamLead(LogWithChain.java:26)
                at com.baeldung.exceptions.chainedexception.LogWithChain.getLeave(LogWithChain.java:16)
                ... 1 more
        Caused by: com.baeldung.exceptions.chainedexception.exceptions.ManagerUpsetException: Manager is in bad mood
                at com.baeldung.exceptions.chainedexception.LogWithChain.howIsManager(LogWithChain.java:34)
                at com.baeldung.exceptions.chainedexception.LogWithChain.howIsTeamLead(LogWithChain.java:24)
                ... 2 more
        Caused by: com.baeldung.exceptions.chainedexception.exceptions.GirlFriendOfManagerUpsetException: Girl friend of manager is in bad mood
                at com.baeldung.exceptions.chainedexception.LogWithChain.howIsGirlFriendOfManager(LogWithChain.java:39)
                at com.baeldung.exceptions.chainedexception.LogWithChain.howIsManager(LogWithChain.java:32)
                ... 3 more
        ```

        我们可以很容易地比较显示的日志，并得出结论：链式异常导致更干净的日志。

6. 总结
    在这篇文章中，我们了解了链式异常的概念。

## Java中Throw和Throws的区别

1. 简介

    在本教程中，我们将看一下Java中的throw和throws。我们将解释什么时候应该使用它们中的每一个。

    接下来，我们将展示它们的一些基本用法的例子。

2. 抛出和抛出

    让我们先简单介绍一下。这些关键字与异常处理有关。当我们的应用程序的正常流程被破坏时就会产生异常。

    可能有很多原因。一个用户可能发送了错误的输入数据。我们可能会失去一个连接或发生其他意外情况。良好的异常处理是在这些不愉快的时刻出现后保持我们的应用程序工作的关键。

    我们使用throw关键字来明确地从代码中抛出一个异常。它可以是任何方法或静态块。这个异常必须是Throwable的一个子类。此外，它也可以是一个Throwable本身。我们不能用一个throw来抛出多个异常。

    Throws关键字可以放在方法声明中。它表示哪些异常可以从这个方法中抛出。我们必须用try-catch来处理这些异常。

    这两个关键字是不能互换的!

3. Java中的抛出

    让我们来看看一个基本的例子，从方法中抛出一个异常。

    首先，想象一下，我们正在编写一个简单的计算器。其中一个基本的算术运算是除法: a / b;

    因为我们不能除以0，所以我们需要对现有的代码进行一些修改。似乎这是个引发异常的好时机。

    让我们这样做吧：

    ```java
    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Divider cannot be equal to zero!");
        }
        return a / b;
    }
    ```

    正如你所看到的，我们使用了ArithmeticException，它完全符合我们的需要。我们可以传递一个单一的字符串构造参数，也就是异常信息。

    1. 良好的做法

        我们应该总是选择最特殊的异常。我们需要找到一个最适合我们的异常事件的类。例如，抛出NumberFormatException而不是IllegalArgumentException。我们应该避免抛出一个不具体的异常。

        例如，在java.lang包里有一个Integer类。让我们来看看其中的一个工厂方法声明：

        `public static Integer valueOf(String s) throws NumberFormatException`

        这是一个静态工厂方法，它从String创建Integer实例。如果输入的字符串错误，该方法将抛出NumberFormatException。

        一个好主意是定义我们自己的、更具描述性的异常。在我们的计算器类中，例如，可以是DivideByZeroException。

        让我们来看看示例实现：

        ```java
        public class DivideByZeroException extends RuntimeException {
            public DivideByZeroException(String message) {
                super(message);
            }
        }
        ```

    2. 包裹一个现有的异常

        有时我们想把一个已有的异常包裹到我们所定义的异常中。

        让我们从定义我们自己的异常开始：

        exceptions.throwvsthrows/DataAcessException.java

        构造函数需要两个参数：异常消息和原因，可以是Throwable的任何子类。

        让我们为findAll()函数写一个假的实现：

        PersonRepository.java: findAll()

        现在，在SimpleService中让我们调用一个存储库函数，这可能导致SQLException：

        SimpleService.java: wrappingException()

        我们重新抛出的SQLException被包装成我们自己的异常，称为DataAccessException。一切都由下面的测试来验证：

        SimpleServiceUnitTest.java: whenSQLExceptionIsThrown_thenShouldBeRethrownWithWrappedException()

        有两个原因要这样做。首先，我们使用异常封装，因为代码的其他部分不需要知道系统中每一个可能的异常。

        另外，高层组件不需要知道底层组件，也不需要知道它们抛出的异常。

    3. Java的多重捕获

        有时，我们使用的方法可以抛出许多不同的异常。

        让我们来看看更多的try-catch块：

        ```java
        try {
            tryCatch.execute();
        } catch (ConnectionException | SocketException ex) {
            System.out.println("IOException");
        } catch (Exception ex) {
            System.out.println("General exception");
        }
        ```

        执行方法可以抛出三种异常： SocketException, ConnectionException, Exception。第一个捕获块将捕获ConnectionException或SocketException。第二个捕获块会捕获Exception或其他任何Exception的子类。记住，我们应该总是先捕捉更详细的异常。

        我们可以调换我们的捕获块的顺序。这样，我们就不会捕捉SocketException和ConnectionException了，因为所有的东西都会进入Exception的捕捉区。

4. Java中的抛出

    我们在方法声明中加入throws。

    让我们看一下我们之前的一个方法声明：

    `public static void execute() throws SocketException, ConnectionException, Exception`

    该方法可以抛出多个异常。它们在方法声明的结尾处用逗号隔开。我们可以把检查过的和未检查过的异常都放在throws中。我们在下面描述了它们之间的区别。

    1. 检验过的和未检验过的异常

        一个被检查的异常意味着它在编译时被检查。注意，我们必须处理这个异常。否则，一个方法必须使用throws关键字来指定一个异常。

        最常见的检查异常是IOException, FileNotFoundException, ParseException。当我们从File创建FileInputStream时，可能会抛出FileNotFoundException。

        这里有一个简短的例子：

        exceptions.throwvsthrows/main.java: checkedException()

        我们可以通过在方法声明中加入throws来避免使用try-catch块：

        exceptions.throwvsthrows/main.java: uncheckedException()

        不幸的是，一个更高层次的函数仍然要处理这个异常。否则，我们必须在方法声明中使用throws关键字来处理这个异常。

        与此相反，未检查的异常在编译时并不被检查。

        最常见的未检查的异常是： ArrayIndexOutOfBoundsException, IllegalArgumentException, NullPointerException。

        未检查的异常会在运行时抛出。下面的代码将抛出一个NullPointerException。这可能是Java中最常见的异常之一。

        在一个空引用上调用一个方法将导致这个异常：

        SimpleService.java: runtimeNullPointerException()

        让我们在测试中验证这一行为：

        SimpleServiceUnitTest.java: whenCalled_thenNullPointerExceptionIsThrown()

        请记住，这段代码和测试并没有什么意义。它只是为了解释运行时异常的学习目的。

        在Java中，Error和RuntimeException的每个子类都是一个未检查的异常。被检查的异常是Throwable类下的其他一切。

5. 总结

    在这篇文章中，我们讨论了两个Java关键字的区别：throw和throws。我们浏览了基本的用法，并谈到了一些好的做法。然后我们谈到了有检查和无检查的异常。

## Java中的StackOverflowError

1. 概述

    堆栈溢出错误（StackOverflowError）可能会让 Java 开发人员感到厌烦，因为它是我们可能遇到的最常见的运行时错误之一。

    在本文中，我们将通过查看各种代码示例来了解这种错误是如何发生的，以及如何处理它。

2. 堆栈框架和 StackOverflowError 如何发生

    让我们从最基本的开始。调用方法时，会在[调用堆栈](https://www.baeldung.com/cs/call-stack)上创建一个新的堆栈帧。这个堆栈框架保存着被调用方法的参数、局部变量和方法的返回地址，即被调用方法返回后继续执行方法的起点。

    栈帧的创建将一直持续到嵌套方法内部的方法调用结束为止。

    在此过程中，如果 JVM 遇到没有空间创建新栈框的情况，就会抛出堆栈溢出错误（StackOverflowError）。

    JVM 遇到这种情况的最常见原因是未终止/无限递归(unterminated/infinite recursion)-StackOverflowError 的 Javadoc 说明中提到，该错误是由于特定代码片段中的递归太深而抛出的。

    然而，递归并不是导致该错误的唯一原因。它也可能发生在应用程序不断调用方法内部的方法直到堆栈耗尽的情况下。这种情况很少见，因为没有开发人员会故意遵循不良的编码实践。另一种罕见的原因是方法内部存在大量局部变量。

    当应用程序设计为类之间存在循环关系时，也会抛出 StackOverflowError。在这种情况下，彼此的构造函数会被重复调用，从而导致抛出此错误。这也可以被视为一种递归形式。

    另一种导致此错误的有趣情况是，一个类在同一个类中被实例化为该类的实例变量。这将导致同一个类的构造函数被反复调用（递归），最终导致 StackOverflowError。

    在下一节中，我们将查看一些演示这些情况的代码示例。

3. 运行中的 StackOverflowError

    在下面显示的示例中，由于开发人员忘记为递归行为指定终止条件，因此会产生意外递归，从而抛出 StackOverflowError：

    UnintendedInfiniteRecursion.java

    在这里，对于传递到方法中的任何值，在任何情况下都会抛出错误：

    UnintendedInfiniteRecursionManualTest.java

    然而，在下一个示例中，虽然指定了终止条件，但如果向 calculateFactorial() 方法传递了-1 的值，就永远不会满足终止条件，从而导致未终止/无限递归：

    InfiniteRecursionWithTerminationCondition.java

    这组测试演示了这种情况：

    InfiniteRecursionWithTerminationConditionManualTest.java

    在这个特殊的案例中，如果将终止条件简单地写为以下内容，就可以完全避免错误的发生：

    RecursionWithCorrectTerminationCondition.java

    下面的测试展示了这种情况的实际应用：

    RecursionWithCorrectTerminationConditionManualTest.java

    现在，让我们来看看由于类之间的循环关系而发生 StackOverflowError 的情况。让我们考虑一下 ClassOne 和 ClassTwo，它们在构造函数中相互实例化，从而造成了循环关系：

    ClassOne.java

    ClassTwo.java

    现在，假设我们尝试实例化 ClassOne，如本测试所示：

    CyclicDependancyManualTest.java

    由于 ClassOne 的构造函数实例化了 ClassTwo，而 ClassTwo 的构造函数又实例化了 ClassOne，因此最终出现了 StackOverflowError。这样反复发生，直到堆栈溢出。

    接下来，我们将看看当一个类在同一个类中被实例化为该类的实例变量时会发生什么。

    在下一个示例中，AccountHolder 将自己实例化为实例变量 jointAccountHolder：

    AccountHolder.java

    当实例化 AccountHolder 类时，由于构造函数的递归调用，会抛出 StackOverflowError，如本测试所示：

    AccountHolderManualTest.java

4. 处理 StackOverflowError

    遇到 StackOverflowError 时，最好的办法是谨慎检查堆栈跟踪，识别行号的重复模式。这将使我们能够找到有问题的递归代码。

    让我们来看看前面的代码示例引起的堆栈跟踪。

    如果我们省略预期异常声明，InfiniteRecursionWithTerminationConditionManualTest 将产生此堆栈跟踪：

    ```log
    java.lang.StackOverflowError

    at c.b.s.InfiniteRecursionWithTerminationCondition
    .calculateFactorial(InfiniteRecursionWithTerminationCondition.java:5)
    at c.b.s.InfiniteRecursionWithTerminationCondition
    .calculateFactorial(InfiniteRecursionWithTerminationCondition.java:5)
    ...
    ```

    在这里，可以看到第 5 行在重复。递归调用就是在这里完成的。现在只需检查代码，看看递归是否以正确的方式进行。

    下面是执行 CyclicDependancyManualTest（循环依赖性手动测试）后得到的堆栈跟踪（同样，没有出现预期的异常）：

    ```log
    java.lang.StackOverflowError
    at c.b.s.ClassTwo.<init>(ClassTwo.java:9)
    at c.b.s.ClassOne.<init>(ClassOne.java:9)
    ...
    ```

    此堆栈跟踪显示了在循环关系中的两个类中导致问题的行号。ClassTwo 的第 9 行和 ClassOne 的第 9 行指向构造函数中试图实例化另一个类的位置。

    彻底检查代码后，如果以下情况（或任何其他代码逻辑错误）都不是导致错误的原因：

    - 不正确地执行递归（即没有终止条件）
    - 类之间的循环依赖
    - 将同类中的一个类实例化为该类的实例变量

    尝试增加堆栈大小是个好主意。根据所安装的 JVM，默认堆栈大小可能会有所不同。

    可以在项目配置或命令行中使用 -Xss 标志来增加堆栈大小。

5. 总结

    在本文中，我们仔细研究了 StackOverflowError，包括 Java 代码如何导致该错误，以及如何诊断和修复该错误。

## Relevant articles

- [x] [Chained Exceptions in Java](https://www.baeldung.com/java-chained-exceptions)
- [ClassNotFoundException vs NoClassDefFoundError](https://www.baeldung.com/java-classnotfoundexception-and-noclassdeffounderror)
- [Create a Custom Exception in Java](https://www.baeldung.com/java-new-custom-exception)
- [x] [Exception Handling in Java](https://www.baeldung.com/java-exceptions)
- [Differences Between Final, Finally and Finalize in Java](https://www.baeldung.com/java-final-finally-finalize)
- [x] [Difference Between Throw and Throws in Java](https://www.baeldung.com/java-throw-throws)
- [x] [The StackOverflowError in Java](https://www.baeldung.com/java-stack-overflow-error)
- [x] [Checked and Unchecked Exceptions in Java](https://www.baeldung.com/java-checked-unchecked-exceptions)
- [Common Java Exceptions](https://www.baeldung.com/java-common-exceptions)
- [Will an Error Be Caught by Catch Block in Java?](https://www.baeldung.com/java-error-catch)
- [[Next -->]](../core-java-exceptions-2)

## Code

一如既往，本文中的所有代码都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-exceptions)上找到！
