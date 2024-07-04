# Core Java Exceptions 2

## 尝试利用资源

1. 概述

    对带资源的尝试的支持--在Java 7中引入--允许我们声明在尝试块中使用的资源，并保证这些资源在该块执行后将被关闭。

    声明的资源需要实现AutoCloseable接口。

2. 使用try-with-resources

    简单地说，要想自动关闭，必须在try里面同时声明和初始化一个资源：

    ```java
    try (PrintWriter writer = new PrintWriter(new File("test.txt"))) {
        writer.println("Hello World");
    }
    ```

3. 用 try-with-resources 取代 try-catch-finally

    使用新的try-with-resources功能的简单而明显的方法是取代传统的、冗长的try-catch-finally块。

    让我们比较一下下面的代码样本。

    第一个是一个典型的try-catch-finally块：

    ```java
    Scanner scanner = null;
    try {
        scanner = new Scanner(new File("test.txt"));
        while (scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } finally {
        if (scanner != null) {
            scanner.close();
        }
    }
    ```

    下面是使用try-with-resources的新的超级简洁的解决方案：

    ```java
    try (Scanner scanner = new Scanner(new File("test.txt"))) {
        while (scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }
    } catch (FileNotFoundException fnfe) {
        fnfe.printStackTrace();
    }
    ```

    这里是进一步探索扫描器类的地方。

4. 有多个资源的try-with-resources

    我们可以在try-with-resources块中声明多个资源，只要用分号将它们分开就可以了：

    ```java
    try (Scanner scanner = new Scanner(new File("testRead.txt"));
        PrintWriter writer = new PrintWriter(new File("testWrite.txt"))) {
        while (scanner.hasNext()) {
        writer.print(scanner.nextLine());
        }
    }
    ```

5. 带有AutoCloseable的自定义资源

    为了构建一个能被try-with-resources块正确处理的自定义资源，该类应该实现Closeable或AutoCloseable接口并重载close方法：

    trywithresource/MyResource.java

6. 资源关闭顺序

    先定义/获取的资源将被最后关闭。让我们看一下这种行为的例子：

    资源1：trywithresource/AutoCloseableResourcesFirst.java

    资源2：trywithresource/AutoCloseableResourcesSecond.java

    代码：trywithresource/AutoCloseableMain.java

7. 捕获和最后

    一个带资源的尝试块仍然可以有catch和final块，它们的工作方式与传统的尝试块相同。

8. Java 9 - 有效的最终变量

    在Java 9之前，我们只能在try-with-resources块内使用新鲜变量：

    ```java
    try (Scanner scanner = new Scanner(new File("testRead.txt")); 
        PrintWriter writer = new PrintWriter(new File("testWrite.txt"))) { 
        // omitted
    }
    ```

    如上所示，在声明多个资源时，这样做特别啰嗦。从Java 9开始，作为JEP 213的一部分，我们现在可以在try-with-resources块中使用final甚至是有效的final变量：

    ```java
    final Scanner scanner = new Scanner(new File("testRead.txt"));
    PrintWriter writer = new PrintWriter(new File("testWrite.txt"))
    try (scanner;writer) { 
        // omitted
    }
    ```

    简单地说，如果一个变量在第一次赋值后没有变化，那么它实际上就是最终变量，即使它没有明确地被标记为最终变量。

    如上所示，scanner变量被明确声明为final，所以我们可以在try-with-resources块中使用它。尽管写作者变量没有明确的final，但它在第一次赋值后并没有改变。所以，我们也可以使用writer变量。

9. 结论

    在这篇文章中，我们讨论了如何使用try-with-resources，以及如何用try-with-resources替换try、catch和finally。

    我们还研究了用AutoCloseable构建自定义资源以及资源关闭的顺序。

## Relevant Articles

- [Is It a Bad Practice to Catch Throwable?](https://www.baeldung.com/java-catch-throwable-bad-practice)
- [Wrapping vs Rethrowing Exceptions in Java](https://www.baeldung.com/java-wrapping-vs-rethrowing-exceptions)
- [java.net.UnknownHostException: Invalid Hostname for Server](https://www.baeldung.com/java-unknownhostexception)
- [How to Handle Java SocketException](https://www.baeldung.com/java-socketexception)
- [Java Suppressed Exceptions](https://www.baeldung.com/java-suppressed-exceptions)
- [x] [Java – Try with Resources](https://www.baeldung.com/java-try-with-resources)
- [Java Global Exception Handler](https://www.baeldung.com/java-global-exception-handler)
- [How to Find an Exception’s Root Cause in Java](https://www.baeldung.com/java-exception-root-cause)
- [Java IOException “Too many open files”](https://www.baeldung.com/java-too-many-open-files)
- [When Does Java Throw the ExceptionInInitializerError?](https://www.baeldung.com/java-exceptionininitializererror)
- More articles: [[<-- prev]](../core-java-exceptions) [[next -->]](../core-java-exceptions-3)

## Code

这个例子的完整源代码可以在这个[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-exceptions-2)项目中找到。
