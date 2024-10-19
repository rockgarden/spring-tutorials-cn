# [使用Java断言](https://www.baeldung.com/java-assert)

1. 简介

    Java断言关键字允许开发人员快速验证程序的某些假设或状态。

    在这篇文章中，我们将看看如何使用Java assert关键字。

2. Java断言的历史

    Java assert关键字是在Java 1.4中引入的，所以它已经存在了很长时间。然而，它仍然是一个鲜为人知的关键字，它可以大大减少模板，使我们的代码更加可读。

    例如，在我们的代码中，很多时候我们需要验证某些可能妨碍我们的应用程序正常工作的条件。通常我们会这样写。

    ```java
    Connection conn = getConnection();
        if(conn == null) {
        throw new RuntimeException("Connection is null");
    }
    ```

    使用断言，我们可以用一个断言语句删除if和throw语句。

3. 启用Java断言

    因为Java断言使用assert关键字，所以不需要库或包来导入。

    请注意，在Java 1.4之前，使用 "assert"一词来命名变量、方法等是完全合法的。当使用较旧的代码和较新的JVM版本时，这有可能造成命名冲突。

    因此，为了向后兼容，JVM默认禁用断言验证。必须使用-enableassertions命令行参数或其简写-ea来明确启用它们。

    `java -ea com.baeldung.assertion.Assertion`

    在这个例子中，我们启用了所有类的断言。

    我们还可以为特定的包和类启用断言。

    `java -ea:com.baeldung.assertion... com.baeldung.assertion.Assertion`

    这里，我们为 com.baeldung.assertion 包中的所有类启用了断言。

    同样地，也可以使用-disableassertions命令行参数，或其简写-da，为特定的包和类禁用它们。我们也可以同时使用这四个参数。

4. 使用Java断言

    要添加断言，只需使用assert关键字并给它一个布尔条件。

    ```java
    public void setup() {
        Connection conn = getConnection();
        assert conn != null;
    }
    ```

    Java还为断言提供了第二种语法，它需要一个字符串，如果抛出一个断言，它将被用来构造断言错误。

    Assertion.setup()

    在这两种情况下，代码都在检查与外部资源的连接是否返回一个非空值。如果该值为空，JVM将自动抛出一个AssertionError。

    在第二种情况下，异常会有额外的细节，会显示在堆栈跟踪中，可以帮助调试问题。

    让我们来看看启用断言后运行我们的类的结果。

    ```log
    Exception in thread "main" java.lang.AssertionError: Connection is null
            at com.baeldung.assertion.Assertion.setup(Assertion.java:15)
            at com.baeldung.assertion.Assertion.main(Assertion.java:10)
    ```

5. 处理一个断言错误

    AssertionError类扩展了Error，而Error本身又扩展了Throwable。这意味着AssertionError是一个未经检查的异常。

    因此，使用断言的方法不需要声明它们，而且进一步的调用代码不应该尝试和捕捉它们。

    断言错误（AssertionErrors）是用来指示应用程序中不可恢复的情况，所以千万不要尝试处理它们或试图恢复。

6. 最佳实践

    关于断言，最重要的一点是它们可以被禁用，所以千万不要认为它们会被执行。

    因此，在使用断言时要记住以下几点。

    - 在适当的时候总是检查空值和空的Optionals。
    - 避免使用断言来检查公共方法的输入，而是使用未检查的异常，如IllegalArgumentException或NullPointerException。
    - 不要在断言条件下调用方法，而是将方法的结果分配给一个局部变量，然后用断言使用该变量。
    - 断言非常适合用于代码中永远不会被执行的地方，例如switch语句的默认情况或永远不会结束的循环之后。

7. 结论

    Java的断言关键字已经存在了很多年，但仍然是该语言中鲜为人知的特性。它可以帮助删除大量的模板代码，使代码更具可读性，并有助于在程序开发的早期识别错误。

    只要记住，断言在默认情况下是不启用的，所以千万不要认为在代码中使用断言时它们会被执行。
