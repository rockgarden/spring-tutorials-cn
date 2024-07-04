# Core Java Console

## 用Java读写用户输入

1. 简介

    在本快速教程中，我们将演示在 Java 中使用控制台进行用户输入和输出的几种方法。

    我们将学习 [Scanner](https://www.baeldung.com/java-scanner) 类处理输入的几个方法，然后使用 System.out 演示一些简单的输出。

    最后，我们将了解如何使用 Java 6 中的 Console 类来处理控制台输入和输出。

2. 从 System.in 中读取

    在第一个示例中，我们将使用 java.util 包中的 Scanner 类从 System.in -- "standard" 输入流中获取输入：

    `Scanner scanner = new Scanner(System.in);`

    让我们使用 nextLine() 方法以字符串形式读取整行输入内容，并前进到下一行：

    `String nameSurname = scanner.nextLine();`

    我们还可以使用 next() 方法从数据流中获取下一个输入标记：

    `String gender = scanner.next();`

    如果我们期待的是数字输入，我们可以使用 nextInt() 获取下一个 int 原始码输入，同样，我们也可以使用 nextDouble() 获取一个 double 类型的变量：

    ```java
    int age = scanner.nextInt();
    double height = scanner.nextDouble();
    ```

    扫描器类还提供 hasNext_Prefix() 方法，如果下一个标记可以被解释为相应的数据类型，这些方法就会返回 true。

    例如，我们可以使用 hasNextInt() 方法检查下一个标记是否可以解释为整数：

    ```java
    while (scanner.hasNextInt()) {
        int nmbr = scanner.nextInt();
        //...
    }
    ```

    此外，我们还可以使用 hasNext(Pattern pattern) 方法来检查下面的输入标记是否与某个模式匹配：

    `if (scanner.hasNext(Pattern.compile("www.baeldung.com"))) {...}`

    除了使用 Scanner 类，我们还可以使用 System.in 的 InputStreamReader 从控制台获取输入信息：

    `BufferedReader buffReader = new BufferedReader(new InputStreamReader(System.in));`

    然后我们就可以读取输入内容并将其解析为整数：

    `int i = Integer.parseInt(buffReader.readLine());`

3. 写入 System.out

    对于控制台输出，我们可以使用 System.out - PrintStream 类的实例，它是 OutputStream 的一种类型。

    在我们的示例中，我们将使用控制台输出来提示用户输入，并向用户显示最终信息。

    让我们使用 println() 方法打印一个字符串并结束一行：

    `System.out.println("Please enter your name and surname: ");`

    另外，我们还可以使用 print() 方法，其工作原理与 println() 类似，但不终止行：

    ```java
    System.out.print("Have a good");
    System.out.print(" one!");
    ```

4. 使用控制台类进行输入和输出

    在 JDK 6 及更高版本中，我们可以使用 java.io 包中的 Console 类从控制台读取数据或向控制台写入数据。

    要获得 Console 对象，我们将调用 System.console()：

    `Console console = System.console();`

    接下来，让我们使用 Console 类的 readLine() 方法向控制台写一行，然后从控制台读一行：

    `String progLanguauge = console.readLine("Enter your favourite programming language: ");`

    如果需要读取密码等敏感信息，我们可以使用 readPassword() 方法提示用户输入密码，然后从控制台读取密码，并禁用回声：

    `char[] pass = console.readPassword("To finish, enter password: ");`

    我们还可以使用 Console 类将输出写入控制台，例如，使用带有字符串参数的 printf() 方法：

    `console.printf(progLanguauge + " is very interesting!");`

5. 结束语

    在本文中，我们展示了如何使用几个 Java 类来执行控制台用户输入和输出。

## Relevant Articles

- [x] [Read and Write User Input in Java](http://www.baeldung.com/java-console-input-output)
- [Formatting Output with printf() in Java](https://www.baeldung.com/java-printstream-printf)
- [ASCII Art in Java](http://www.baeldung.com/ascii-art-in-java)
- [System.console() vs. System.out](https://www.baeldung.com/java-system-console-vs-system-out)
- [How to Log to the Console in Color](https://www.baeldung.com/java-log-console-in-color)

## Code

与往常一样，本教程的代码示例在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-console) 上提供。
